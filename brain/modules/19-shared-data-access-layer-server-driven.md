# 8.19 Shared data-access layer, server-driven settings and utilities

[← brain.md](../../brain.md) · Module 8.19 of 20 · Previous: [8.18 Printing: collection list prints, receipts and Bluetooth printers](18-printing-collection-list-prints-receipts-bluetooth.md) · Next: [8.20 Core constants, global state and network helpers](20-core-constants-global-state-network-helpers.md)

This module is the plumbing shared by almost every screen. `defaulttables` is a singleton DAO over the local SQLite cache `venusmicrofinv005`: about 110 helpers that fill pickers, build finance/party/loan objects, create the offline tables (`CREATE TABLE IF NOT EXISTS`), and hold fixed code lists (collection breaks, print layouts, financial years, transaction-type labels) plus the stored-procedure strings of the ST/OF loan analysis report. `softsettingsvalue` is a set of static fields holding the server-driven feature flags. They are read from the SQL Server tables `SoftSettings` and `venussoftsettings` by the sync screens, cached in SharedPreferences, and restored when the drawer starts. The remaining classes are utilities: Tamil amount-in-words (`tamilnumberlists` + `nototamilwords`), month names, an SMS sender, HTTP multipart upload helpers, the generated Glide API, a drag-to-reorder ListView and its adapter, RecyclerView dividers, fragment base classes and small data models. Several of them are dead code: `FileUpload`, `sortComparator`, `GlideApp`, `FileListAdapter`, `GetMonthName.GetMonthNo` and 12 `defaulttables` methods.

## defaulttables — shared DAO & business helpers
| | |
|---|---|
| **Type** | Plain Java singleton (`private static final mInstance`, `getInstance()`), no UI. `getDayNumberOld` is static. |
| **Used by** | 89 classes (every list/entry screen, sync screens, adapters, services `MyIntentService`, `MyService_updatecollection`). |
| **Data** | **Local SQLite only.** Every DB method takes the caller's `SQLiteDatabase`. No JDBC calls. SQL is built by string concatenation of int ids. Tables read: `stockitems`, `ItemMaster`, `Suppliers`, `StockUOM`, `Stockcat`, `Stockgrp`, `SubAreaDetails`, `tblLedgers`, `Parties`, `NewParties`, `MobileAppCollData`, `FinanceDetails`, `SelectedBranchFinanceDetails`, `UserRoles`, `EMPLedgers`, `BranchEMPLedgers`, `qatledgers`, `CashBankLedgers`, `LoanMethods`, `LoansAllowed`, `SMSTemplates`, `NewMCLoans`, `tblGroups`, `ExpHeaders`, `BankLedgers`, `loanindex`. Tables created: see the schema table below. |

**Inventory (stock) lookups**

| Method | Reads / returns | Notable SQL / logic · callers |
|---|---|---|
| `fetchitemslist(db)` | `ArrayList<spinnersearch_data>` from `stockitems`: display ItemName, value ItemCode, 2nd int = `PRate as Price` | ordered by ItemName · `invtrans_rowdetails` |
| `fetchitemslistforpurchase(db)` | same from `ItemMaster` with Price 0 | `invtrans_rowdetails` |
| `getitemnameforitemcode(db, ItemCode)` | ItemName from `Itemmaster` | **unused** |
| `getitemnameforitemcodefromstok(db, ItemCode)` | ItemName from `ItemMaster`. This is the same table, because SQLite table names are case-insensitive. | inventory adapters, `invtrans_rowdetails` |
| `fetchsuppliersandcustomers(db)` | `Suppliers`: display ledgername, 2nd display City, value ledgerid, 2nd int GroupId, 3rd display ledgername | `invtransactnew` |
| `fetch_stockUOM(db)` / `fetch_stockcat(db)` / `fetch_stockgrp(db)` | UOM / category / group lists (`IsActive=1 order by DisplayIndex`) | `itemmaster_activity` |
| `getstockuomname(db, uomid)` / `getstockcatname(db, catid)` / `getstockgrpname(db, grpid)` | description for an id | `itemmaster_activity` |

**Parties, sub-areas and the offline collection data**

| Method | Reads / returns | Notable SQL / logic · callers |
|---|---|---|
| `partyobject(db, ledgerid)` | `partydetails_data` (LedgerId, CWLedgerId, LedgerName, MobileNo1/2, Tag, SubAreaNo, MaximumAmountAllowed, LiveParty) | `Parties UNION ALL NewParties` (new parties get `1000 as UDLedgerId`) LEFT JOIN `MobileAppCollData` on PartyLedgerId. LiveParty = the party has a row in the downloaded collection data. · `loanslist_act`, `newloanslist_act`, `tabbedcollectionlist` |
| `getsubareaname(db, SubAreaNo)` | SubAreaName | **unused** |
| `fetchallsubareas(db, finid)` | sub areas of a line `order by ShowId` | `partytransfer` |
| `fetchallsubareaswithexceptsubarea(db, except, finid)` | same, excluding one area | `partytransfer` (target areas) |
| `fetchallpartiesinsubarea(db, subareano)` | value PartyLedgerId, display "PartyName, tag" | `MobileAppCollData` LEFT JOIN `Parties` on PartyLedgerId = LedgerId · `partytransfer` |
| `fetchpartybalance(db, ledgerid)` | `MCLoanAmountCollectable − PrevCollected − Collected` from MobileAppCollData | **unused** |
| `getselectedpartiesloanindexwiseloans(db, partyledgerid, list)` | appends one `loanindexwiseloans_data` per loan slot | `loanindex a` LEFT JOIN `NewMCLoans b` (same party and LoanIndex) LEFT JOIN `MobileAppCollData c`. CurLoanId = c.MCLoanId. Balance = a.Balance + new-loan amount. MCLoanAmountCollectable falls back to NewLoanAmount when it is 0. NewLoan = NewLoanAmount > 0. Closed = TodayCollection > 0 AND NewLoanAmount = Balance. · `loanindexloanswiselist_act` |
| `fetchnoofloansallowed(db)` | `LoansAllowed` (LId, LoanIndex) | `partydetails_act`, `settings_act` |
| `fetchlistfornoofloansallowedfortheparty(db, partyledgerid)` | LoansAllowed rows with `LoanIndex <= Parties.NoOfLoansAllowed` of that party | `newloancreation` (loan-slot picker) |
| `getnewloanlist(db)` | `ArrayList<mcloanobj>`: all offline new loans | see "New-loan list" below · `navigationdrawactivity`, `newloanslist_act`, `tabbedcollectionlist(+mviewpager)` |

**Loans and loan methods**

| Method | Reads / returns | Notable SQL / logic · callers |
|---|---|---|
| `prepareandreturnduelist(ldt, loanamount, noofdues, interestrate, roundingvalue, fdt, schduletype, ismanualdueamount, totaldueamount, PDue, IDue)` | `ArrayList<dueslist_data>` (sno, principledue, interestdue, totaldue, duedate, dueperiodfromdate, dueperiodtodate) | EMI/due schedule, see below · `newloancreation` |
| `getloanmthodparticularsforselectedloanmethod(LoanMethodId, db)` | full `LoanMethods` row as `loanmethods_data` (LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays, DueSupportAmount) | `newloancreation`, `todaycollectionlist` |
| `fetchallloanmethods(db)` | LoanMethods (id, desc) `order by ShowId` | `financelinesaddandedit`, `newloancreation` |
| `getloanmethodname(db, id)` | LoanMethodDesc | same |
| `getdefaultloanmthodidforfinance(db, Double finid)` | `FinanceDetails.DefaultLoanMethodId` | The Double is concatenated as "3.0", which SQLite still compares numerically. · `newloancreation` |

**Ledgers and accounts**

| Method | Reads / returns | Notable SQL / logic · callers |
|---|---|---|
| `fetchsubcollectiontargetledgers(db, finid)` | `tblLedgers` where `isreserved=0`, the finance, `GroupId in (5,6)` (bank + cash). 2nd int = GroupId. | ST/OF loan screens and interest-calculation screens |
| `fetchgroupid(db, ledgerid)` | tblLedgers.GroupId | `ofloans`, `stloans` |
| `fetchstledgername(db, ledgerid)` | tblLedgers.LedgerName | `ofloans`, `partyledgers_list` |
| `fetchledgername(db, ledgerid)` | LedgerName from `qatledgers` (the account-transaction ledger cache) | 8 classes (ST/OF loans, loan ledgers, partners) |
| `fetchsupplierledgername(db, ledgerid)` | Suppliers.ledgername | `invtransactnew` |
| `fetchcashbankledgername(db, id)` / `fetchcashbankledgergroupid(db, id)` | CashBankLedgers name / GroupId | collection & loan entry screens |
| `fetchtargetledgers(db, finid)` | all CashBankLedgers of a finance (3rd int = GroupId) | **unused** |
| `fetchtargetledgersforcashandbank(db, finid)` | CashBankLedgers of the finance. Non-full-access users get only `Alloted=1`. | `directcollectionentry`, `newloancreation`, `tabbedindividualcollectionentry` |
| `fetchtargetledgersforcashandbanktobeverified(db, finid)` | `Alloted=1` books with 2nd int `istobeVerified` and 3rd int GroupId. Appends the pseudo-book "Combained Cash Book" (value 0) when special access 17 is granted. | `cashbook`, `expensesandreceipt_offlineentry` |
| `fetchsourceledgersforaccountstransactions(db, atype, curttid, finid, excludeledgerid)` | ledgers for one side of an account transaction (atype 1 = source, 2 = target), excluding the other side's ledger | Creates `qatledgers` if missing. Unrestricted list (`(finid=0 or finId=X) and AType and TTId`) when full access, special access 19, Payment(7)+source, Receipt(8)+target or Contra(9). Payment+target and Receipt+source are limited to `isExecutiveLevelExpensesLedger=1`. The receipts case also uses the *Expenses* flag, not `isExecutiveLevelReceiptsLedger`. · `accounttransactions_act` |
| `fetchaccountgroupname(db, groupid)` / `fetchaccountgroups(db)` | tblGroups name / all groups by name | `accountledgerdetails`, `accledgerssearch_adapter` |
| `getexpensesheadername(db, ledgerid)` | ExpHeaders.LedgerName | `expensesentry_searchlist` |
| `fetchexpledgers(db, ttid)` | expense (7) or income (8) heads | creates `ExpHeaders(LedgerId Double, LedgerName nvarchar, TTId int)` · `expensesandreceipt_offlineentry` |
| `fetchbankledgerlistforselectedfinance(db, finid)` / `getbankledgername(db, id)` | BankLedgers of a finance / name | `financelinesaddandedit` |
| `fetchmobileno2(db, ledgerid)` | tblLedgers.MobileNo2 | `meetingreport` |

**Finance / line / branch objects and SMS text**

| Method | Reads / returns | Notable SQL / logic · callers |
|---|---|---|
| `fetchfinancelist(db)` | FinanceDetails (FinId, FinMailingName) | 6 classes |
| `fetchfinancename(db, finid)` / `fetchfinanceName(db, finid)` | FinMailingName / FinName | 7 / 4 classes |
| `getfinanceobj(db, finid)` | `financemaster_data`: FinId, FinName, FinMailingName, address, City, PINcode, RegistrationNo, CashLedgerId, ShortName, AdminMobileNos, DefaultLoanMethodId, FinanceCashBookLedgerId, SMSIdentifier, EmailIdentifier, BooksFrom, BankLedgerid (default bank), enableinventory | Calls `createtablefinancedetails` first. · `navigationdrawactivity` (sets `globalvars.curfin`), `downloaddata`, `todaycollectionlist`, `varathittam`, `selectedloancollectiondetails` |
| `getbranchfinanceobj(db, finid)` | the same object from `SelectedBranchFinanceDetails`, plus CollectionBreakString and IsWeeklyFinance | `selectbranchandfinance` |
| `fetchstaffcashbookledgerid(db, finid)` | CashLedgerId from SelectedBranchFinanceDetails, falling back to FinanceDetails | `closingsavingsaccount` |
| `fetchfinancecashbookledgerid(db, finid)` | FinanceCashBookLedgerId with the same fallback | **unused** |
| `emailidentifierforselectedfinance(db, finid)` | FinanceDetails.EmailIdentifier | `cashbook`, `uploadcollection` |
| `fetchCollectionBreakStringforselectedfinance(db, finid)` | upper-cased CollectionBreakString, with "WW" mapped to "W" (JADX shows the "W" as `ExifInterface.LONGITUDE_WEST`) | **unused.** Its fallback query selects FinanceCashBookLedgerId but reads CollectionBreakString, which would throw. |
| `fetchsmstemplatetext(db, stid)` | `SMSTemplates.SMSDesc` | SMS senders (`cashbook`, `cashreceiveandreturns`, `downloaddata`, `tabbedindividualcollectionentry`, `uploadcollection`) |

**Users / roles**

| Method | Reads / returns | Notable SQL / logic · callers |
|---|---|---|
| `fetchuserroles(db)` | Full-access users get all `UserRoles` by DisplayIndex. Other users get only roles with `DisplayIndex >` their own (`globalvars.app_userroleid`), so they can assign lower roles only. | `mobiledeviceaccescontrol_act` |
| `fetchuserrolesforspecialaccessrules(db)` | non-full-access roles with DisplayIndex ≥ the user's own | `specialaccesscontrolact`, `userroleprevilages_act` |
| `fetchuserrolename(db, urid)` / `fetchuserroledispindex(db, urid)` | URName / DisplayIndex | drawer header (`navigationdrawactivity`), access-control lists |
| `getemps(db)` | `EMPLedgers where desigstatusid=1` (active staff) | `mobiledeviceaccescontrol_act` |
| `getBranchEMPLedgers(db)` | BranchEMPLedgers | `branchmobiledeviceaccescontrol_act` |

**Static code lists** (details below the tables)

| Method | Returns | Callers |
|---|---|---|
| `fetchguardiantype()` | 1 "த.பெ" (father's name, S/o), 2 "க.பெ" (husband's name, W/o) | `partyandpartnerdetails` |
| `fetchmoneygivenmode(id)` / `fetchtranstypelist()` | payment modes 1 Cheque, 2 RTGS, 3 NEFT, 4 Others ("" for others) | `ofloans`, `stloans` |
| `getlanguagelist()` / `getlanguagename(lid)` / `getlanguageshortname(lid)` | 1 தமிழ் / "ta", 2 English / "en" | `deviceidentity`, `splash` (sets the locale) |
| `fetchtranstype(ttid)` / `fetchtranstype_inorout(ttid)` | label / cash direction | **unused** / `accounttransactions_act` |
| `fetchcollectionbreaks()` | 4 `collectionbreaks_data` | `loanmethods` |
| `fetchdocumentcharges()` | 1 Percentage, 2 Fixed (`documentchargemethods_data`, used for `LoanMethods.DocChargesMethod`) | `loanmethods` |
| `fetchweekdayslist(ctx)` | 1..7 = Sunday..Saturday from `R.string.sunday…saturday` (localized EN/TA) | `collectionareadetails_act`, `collectionareassearchlist`, `loanclosoureentry`, `newloancreation`, `selectbranchandfinance` |
| `fetchTamilweekdayslist()` | 1 ஞாயிறு … 7 சனி, hard-coded | **unused** |
| `fetchmonthdayslist()` | 1..28 (month-day ids for monthly loans; 29–31 are excluded) | `collectionareassearchlist`, `newloancreation` |
| `loanstatusmethods()` / `loanstatusmethodname(id)` | two status display modes | `settings_act` |
| `userlevelsmethods()` | 1 Super Admin, 2 Admin, 3 Executive | **unused** |
| `executivehandlingmethods()` | 1 Offline, 2 Online | **unused** (the value comes from VSS 22) |
| `fivedayscollectionmethods()` / `fivecollectionsmethodname(id)` | 1 "LAST 5 DAYS COLLECTIONS", 2 "LAST COLLECTED 5 DAYS COLLECTION" ("" otherwise) | `settings_act` |
| `collectionreportprintingmethods()` / `collectionreportprintingmethodname(id)` | the 10 print layouts / one layout by id (an empty object if not found) | `collectionreportprintingmethods_searchlist` / `collectionlistprints`, `settings_act` |
| `getsettingdescription(ssid)` | Tamil help text for SoftSettings 14 ("each loan must be uploaded as fully collected or closed as bad debt; until then it keeps showing in the collection list") and 20 ("choose this to show the party number in the mobile collection list"). Any other id throws `IllegalStateException`. | `settings_act` |

**Dates and financial years**

| Method | Returns | Notes · callers |
|---|---|---|
| `getDayNumberOld(Date)` *(static)* | `Calendar.DAY_OF_WEEK` (1 = Sunday … 7 = Saturday) | `newloancreation` (loan WeekDayId) |
| `getWeekdayid(Date)` | 1..7 from `DateFormat.format("EEE")` | Smali confirms the case label is **"Son"**, so Sunday returns 0. The format is locale-dependent, so in the Tamil locale every day returns 0. · `editing_collectionlist_act` |
| `gettamilweekdaynameforid(id, ctx)` | localized name from `["", Sunday…Saturday]` | IndexOutOfBounds for ids outside 0–7 · `collectionareadetails_act`, `collectionlistprints`, `downloaddata`, `newloancreation` |
| `getdatestringddMMyyyy(Date)` | "dd-MM-yyyy" | 5 classes |
| `gettodaydate()` | `java.sql.Date` of `LocalDate.now()` | `refreshorreloadfinancelist` |
| `convertToLocalDateViaMilisecond(Date)` | LocalDate in the system zone | internal |
| `fetchaccountyearmonth(calendarMonth)` | Indian FY month index: Apr→0 … Dec→8, Jan→9, Feb→10, Mar→11, anything else→0 | `monthwisepandl` |
| `fetchfinancialyears()` | `ArrayList<finyears_data>` | see below; internal |
| `fetchfinancialyearsforselection()` | spinner list (FYDesc, FYId) | `accountsreports_parameters`, `settings_act` |
| `fetchfydesc(fyid)` / `getsdforfinyear(fyid)` / `getedforfinyear(fyid)` | description / start date / end date by `list.get(fyid−1)` | `getsdforfinyear` maps 0 to 1; `getedforfinyear` has no such guard. · 6 accounts screens (with `softsettingsvalue.currentfinancialyearid_20`) |

**Table creation, analysis settings, misc**

| Method | Returns / does | Callers |
|---|---|---|
| `createtablefinancedetails`, `createtableseFinancesinSelectedBranch`, `createtableBranchConnStrs`, `createtabletblSalesDetailsforAndroid`, `createsubareadetails`, `createmobilappcoldata`, `createRelatedDocCollection`, `createnewmcloanstable`, `createlastuploadtable`, `createnewpartiestable`, `createpartiestable` (all `(db)`) | `CREATE TABLE IF NOT EXISTS` (schemas below) | sync screens, `downloaddata`, `editing_collectionswithfulloptions_act`, `uploadcollection`, services |
| `fetchanalysisreportsettings()` | 2 `loananddepositanalysisreportsettings_data` entries: [ST loans (ttid 3), OF loans (ttid 5)] | `navigationdrawactivity` (nav_STLoanAnalysis uses `.get(0)`) |
| `fetchanalysisreportsettingsforofloans()` | [OF loans (ttid 5)] | `navigationdrawactivity` (nav_OFLoanAnalysis) |
| `isValidPhoneNumber(s)` | `Patterns.PHONE` matches AND `^[6-9]\d{9}$` (Indian 10-digit mobile) | `newloancreation` |
| `generateUniqueFileName(prefix)` | `prefix_yyyyMMdd_HHmmss_NNNN` (NNNN = random 1000–9999) | PDF exports in `dailypattinew_act`, `dailypatti_nadapunippuphakkilist_act`, `dailythittamforallowedlines`, `simplethittamlist_act` |
| `getInstance()` | singleton | 89 classes |

**Due schedule — `prepareandreturnduelist`.** The inputs are the loan date `ldt` and the first due date `fdt`.
1. `principaldue = loanamount / noofdues` (integer division, so the remainder is dropped). `interestdue = loanamount × interestrate / 100 × n / n`, which is flat interest per installment.
2. If `ismanualdueamount`, then principaldue = `PDue` and interestdue = `IDue`. `totaldue = (int)interestdue + principaldue`. The `totaldueamount` argument is overwritten and `roundingvalue` is never used, so there is **no rounding**. There is also **no holiday, Sunday or weekday adjustment**: dues fall on plain calendar offsets.
3. Schedule types:
   - **4 (monthly):** the first due date becomes `fdt + 1 month`. Due 1 covers loan date → that date. Due k (2..n) falls on `first + (k−1) months`, period `first + (k−2) months + 1 day` → `first + (k−1) months`. Smali confirms this; JADX printed the loop variable as `r16`.
   - **5 and 6 (identical code):** 30-day cycles. Due 1 on `fdt`. Due k on `fdt + 30(k−1)` days, period `fdt + 30(k−2) + 1` → `fdt + 30(k−1)`.
   - **2 (weekly):** interest per due = `(int)interestdue / 4` (the monthly rate spread over 4 weeks). Due 1 on `fdt` with total = principal + interest/4. Due k on `fdt + (k−1)` weeks. **Bug:** dues 2..n keep `totaldue = principal + full interest` while their interest column shows interest/4.
   - **1 (daily), 3 (twice monthly), any other:** empty list.
4. Real call (`newloancreation` due-schedule button): `ldt = fdt = MCLoanDate`, loan amount = `getBalanceamount()`, n = `TotalMonths`, rate = `Intrate`, rounding 5, schedule type = the loan method's `CollectionBreak`, `ismanual = true`, `PDue = InsAmount`, `IDue = 0`. So every due equals the installment amount with zero interest. The list is passed as the Serializable extra `dueslist_dataList` to `dueschdule`. Because collection breaks are 1–4, only weekly and monthly loans get a schedule; daily and twice-monthly loans show an empty one, and types 5/6 cannot be reached.

**New-loan list — `getnewloanlist`.** It first creates NewParties, Parties and NewMCLoans, then unions two result sets into `mcloanobj`:
- Query 1: `NewMCLoans a, NewParties b, SubAreaDetails c` (new loans for new parties, with Tag).
- Query 2: `NewMCLoans a, Parties b, SubAreaDetails c` (new loans for existing parties; Tag is not selected and stays null).

Both join on `a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo`. Fields: MCLoanDate, SubAreaName→AreaName, CWLedgerId, ondaycollection, Ledgername→PartyName, MCLoanId, FinId, PartyLedgerId, MCLoanAmount, TotalMonths, DocCharges, LoanMethodId, InterestAmount, MCLoanAmountCollectable, InsAmount, MCLoanAmountEstimated, SubAreaNo, ServerGenMCLoanId, ServerGenLedgerId, Remarks, ServerGenTransId, WeekDayId, MonthDayId, mobileno, Sourceledgerid, LoanIndex, PartyReferenced, ProductValue, AdvanceAmount, IntRate (read with `getInt`, so decimals are truncated).

**Transaction types — `fetchtranstype(ttid)` (labels, unused) and `fetchtranstype_inorout(ttid)` (cash direction).** `accounttransactions_act` uses the direction to choose which ledger name becomes the Particulars.

| ttid | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 19 | 20 | 24 | 25 | 26 | 27 | 45 | 46 | other |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| label | Deposit | DEP Int | ST லோன் | STL Int | OF லோன் | OFL Int | Payment | Receipt | Contra | Journal | CAP Coll | CAP RP | Chit Coll | Chit Bid Pay | STL RP | DEP RP | OFL RP | — | — | — | — | HL Pay | HL Rec | "" |
| in/out | +1 | −1 | −1 | +1 | +1 | −1 | −1 | +1 | +1 | +1 | +1 | −1 | +1 | −1 | +1 | −1 | −1 | −1 | +1 | −1 | +1 | −1 | +1 | 0 |

This implies that ST loans are money paid **out** (interest and repayment come in: STL Int +1, STL RP +1), while OF loans are money taken **in** (interest and repayment go out: OFL Int −1, OFL RP −1). The 13/14 (chit), 19 and 45/46 codes are legacy desktop codes; the app's hand loans use 24/25.

**Financial years — `fetchfinancialyears()`.** 18 entries, FYId 1 = 2011-04-01..2012-03-31 ("2011-2012") through FYId 18 = 2028-04-01..2029-03-31 ("2028-2029"), all visible, ShowId = FYId. A 19th object ("2029-2030") is built with the typo **FYId 29** and **never added** (confirmed in smali). Consequences:
- From April 2029, a current-FY id of 19 makes `getsdforfinyear` / `getedforfinyear` throw `IndexOutOfBoundsException` in every accounts screen.
- `getedforfinyear(0)` also crashes.
- The restore default `currentfinancialyearid = 1` means "2011-2012".

**Collection breaks — `fetchcollectionbreaks()`** (`collectionbreaks_data`, used by the loan-method screen):

| cbid | cbstring | Display | interval (days) |
|---|---|---|---|
| 1 | d | தினசரி வசூல் (Daily) | 1 |
| 2 | ww | வார வசூல் (Weekly) | 7 |
| 3 | m | மாதமிருமுறை (twice a month) | 30 (quirk: same code and interval as monthly) |
| 4 | m | மாதந்திர வசூல் (Monthly) | 30 |

The cbstring codes match the T-SQL DATEADD dateparts and `FinanceDetails.CollectionBreakString` (this is an inference).

**Collection-list print layouts — `collectionreportprintingmethods()`** (`collectionreportprintingmethodsobj`; the default comes from SoftSettings 11). "Prev" is passed as the 4th argument of `GetLastNCollectionforCollReportPrint` in `collectionlistprints`; methods 7/8 instead read the month's `tblMCLoanDueCol`.

| id | cols | Name | include-collections option | prev/entry breaks |
|---|---|---|---|---|
| 1 | 1 | 1-Collection List for Next Seven Days | no | 7 |
| 2 | 2 | 2-Collection List for Single Day | yes | −1 |
| 3 | 2 | 3-For today with Prev two days collection | yes | −2 |
| 4 | 2 | 4-LoanAmount,Balance,Status,PrevDayCollection,Today Collection Entry (Two Columns) | yes | −1 |
| 5 | 2 | 5-Two columns with Loan Amount and Three Days Prev Collection | yes | −3 |
| 6 | 2 | 6-Two columns with Loan Amount,Status and Prev Two Days Collection and Today Collection Entry | yes | −2 |
| 7 | 1 | 7-Monthly Collection Report with LoanDate Prev Balance and next Month Balance (A4 Size) | yes | 0 |
| 8 | 1 | 8-Monthly Collection Report with LoanDate,Status,LoanMethod, Prev Balance and next Month Balance (Legal Size) | yes | 0 |
| 9 | 2 | 9-Status and Balance with  Two Days Entry | no | 2 |
| 10 | 2 | 10-Status and Loan Amount with Four Days Entry | no | 4 |

**Other code lists.**
- `loanstatusmethods()`: 1 "வசூலான தவணைகள்/ வசூலாக வேண்டிய தவணைகள். குறைவான தொகை வரவு" (installments collected / installments due, plus shortfall amount); 2 "வசூலான தவணைகள்/மொத்த தவணைகள்." (installments collected / total installments). `loanstatusmethodname(1)` returns text 1, and any other id returns text 2.
- `userlevelsmethods()`: 1 Super Admin, 2 Admin, 3 Executive.
- `executivehandlingmethods()`: 1 Offline, 2 Online.
- `fivedayscollectionmethods()`: 1 = "last 5 days" (download procs `PrepareLast5CollectionsforPrev5dates` / `…forPrev5weeks`), 2 = "last 5 collected" (`PrepareLast5Collections` / `PrepareLast5Collectionsforweeklyline_withautoloanclosure`).

**Local schemas created by the `create*` helpers** (SQLite type names as written):

| Helper | Table | Columns |
|---|---|---|
| `createtablefinancedetails` | `FinanceDetails` | FinId int, CollectionBreakString, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, CashLedgerId Double, PhoneNo, ShortName, MobileNo, Showid Double, IsVisible Boolean, AdminMobileNos, DefaultLoanMethodId int, FinanceCashBookLedgerId Double, SMSIdentifier, EmailIdentifier, YearFrom date, BooksFrom date, BankLedgerid int, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage, LicenseDate date, RecentRenewal date, ExpiryDate date, RenewalAmount int, RenewalNoticeDate date, IsWeeklyFinance boolean, enableinventory boolean (34 columns) |
| `createtableseFinancesinSelectedBranch` | `SelectedBranchFinanceDetails` | the same 34 columns |
| `createtableBranchConnStrs` | `BranchConnStrs` | BId int, BranchName, ConStr, FinCashLedgerId int, CollectionTeamCashLedgerId int, ServerName, DatabaseName, DBUserId, DBPassword, appconstr, dbidentificationno, ShowId int (branch DB credentials are stored in plaintext in the app-private SQLite) |
| `createtabletblSalesDetailsforAndroid` | `tblSalesDetailsforAndroid` | mnuname, mnudisplayname, UserLevelId Int, mnuId Int, showid int, ShowState, ViewState, UpdateState, AddState, DeleteState Boolean, mnustyle int. Despite the name, this is the per-role Android menu privilege cache, filled from server `AndroidMenus LEFT JOIN tblSalesDetailsforAndroid`. |
| `createsubareadetails` | `SubAreaDetails` | SubAreaNo Double, SubAreaName varchar, FinId int, WeekdayId int, IsVisible Boolean, ShowId Double, Latitude REAL, Longitude REAL |
| `createmobilappcoldata` | `MobileAppCollData` (downloaded collection list) | MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, Collected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, IsMarked, PrevCollected, IsVisited, InterestAmount, PenaltyCollected, SavingsCollected, SubAreaName, MCLDueCollId, lng, lat, islocallyfixed, TargetLedgerId, LoanIndex, NoofLoansAllowed, CashCollection, BankCollection, DocCharges, MaximumAmountAllowed, IsAttentionRequired, ShowId (44 columns) |
| `createRelatedDocCollection` | `RelatedDocCollection` | DCUId int, ServerGenMCLoanId int, MCLoanId int, DUID int, Remarks, ImagePath |
| `createnewmcloanstable` | `NewMCLoans` (offline new loans) | MCLoanId, MCLoanDate date, FinId, PartyLedgerId, MCLoanAmount, TotalMonths, DocCharges, LoanMethodId, InterestAmount, MCLoanAmountCollectable Double, InsAmount Double, MCLoanAmountEstimated, SubAreaNo, ServerGenMCLoanId, ServerGenLedgerId, Remarks nvarchar, ServerGenTransId, WeekdayId, MonthdayId, ondaycollection, mobileno, Sourceledgerid, PartyReferenced, LoanIndex, ProductValue, AdvanceAmount, IntRate double |
| `createlastuploadtable` | `LastUpload` | MCLGCollId, FWGColId, TransId, SubmitAmount (Double), SubmitedAt DATETIME DEFAULT CURRENT_TIMESTAMP, CollStaff2LedgerId, CollStaffLedgerId |
| `createnewpartiestable` | `NewParties` | LedgerId, CWLedgerId, LedgerName, Tag, AreaNo, SubAreaNo, UDLedgerId, MobileNo1, MobileNo2, ServerGenLedgerId Double, AddLine1, SpouseName, FamilyName, ImagefileName, IdProoffileName, lat, lng, IdProofFileNameII, NoOfLoansAllowed, MaximumAmountAllowed, IsActive boolean |
| `createpartiestable` | `Parties` | LedgerId, CWLedgerId, LedgerName, Tag, SubAreaNo, UDLedgerId, MobileNo1, MobileNo2, AddLine1, SpouseName, FamilyName, ImagefileName, IdProofFileName, lat, lng, IdProofFileNameII, NoOfLoansAllowed, MaximumAmountAllowed, IsActive boolean |
| *(inline)* `fetchsourceledgersforaccountstransactions` / `fetchexpledgers` | `qatledgers` / `ExpHeaders` | FinId, TTId, AType, LedgerId Double, LedgerName, isExecutiveLevelExpensesLedger bit, isExecutiveLevelReceiptsLedger bit / LedgerId Double, LedgerName nvarchar, TTId int |

**ST/OF loan analysis report procedures.** These are only strings in defaulttables. `navigationdrawactivity` stores the settings object in `globalvars.curanalysisreport` and opens `loananddepositanalyisreport` with ttid 3 (ST) or 5 (OF). Each fragment then runs `executeQuery(<SQLStr> + "'" + globalvars.todaydate + "'")`, so the only parameter is `@RDate = today`, on `globalvars.selbranchconnString`. Report titles (ST / OF) are the header strings.

| Report (title) | ST procedure | OF procedure | Fragment | Columns read |
|---|---|---|---|---|
| Finance-wise ("Financewise STLoan Analysis" / "Financewise OFLoan Analysis") | `Rep_FinWiseSTLoans` | `Rep_FinWiseOFLoans` | `analysisrep_financewiseabstract_fragment` | FinId, Finance, Total |
| Partner-wise ("Partnerwise STLoan Analysis" / "Partnerwise OFLoan Analysis") | `Rep_ShowPartnerWiseSTLoanBalance` | `Rep_ShowPartnerWiseOFLoanBalance` | `analysisrep_partnerwiseabstract_fragment` (its PDF header wrongly uses the finance-and-partner title) | RecPartnerId, Partner, Total |
| Finance & partner-wise ("Finance and Partner Wise STLoan Analysis" / "Finance and Partnerwise OFLoan Analysis") | `Rep_ShowPartnerFinanceWiseSTLoanBalance` | `Rep_ShowPartnerFinanceWiseOFLoanBalance` | `analysisrepfin_partnerwiseabstract_fragment` | FinId, Finance, RecPartnerId, Partner, Total |
| Party-wise ("Partywise STLoan Analysis" / "PartyWise OFLoan Analysis") | `RPT_PartywiseSTLoans` | `RPT_PartywiseOFLoans` | `analysisrepfin_partyrwiseabstract_fragment` | FinId, Finance, RecPartnerId, Partner, Party, IntRate, Total |
| Interest-rate distribution ("STLoan Int Rate Distribution Analysis" / "OFLoan …") | `Rep_ShowSTLoanIntDistribution` | `Rep_ShowOFLoanIntDistribution` | `analysisrepfin_interestratedist_fragment` | Finance, IntRate, count, loan, minimum, maximum, average |
| Overall interest-rate distribution ("Overall STLoan Int Rate Distibution Analysis" / "Overall OFLoan …") | `[Rep_ShowSTLoanIntDistributionOverAll]` | `[Rep_ShowOFLoanIntDistributionOverAll]` | `analysisrepfin_overallinterestratedist_fragment` | IntRate, count, loan, minimum, maximum, average |

**Rules & edge cases.**
- **Unused methods** (confirmed in smali): `getitemnameforitemcode`, `getsubareaname`, `fetchtranstype`, `fetchtargetledgers`, `fetchpartybalance`, `userlevelsmethods`, `executivehandlingmethods`, `fetchTamilweekdayslist`, `fetchCollectionBreakStringforselectedfinance`, `fetchfinancecashbookledgerid`. `fetchfinancialyears` and `convertToLocalDateViaMilisecond` are only used internally.
- `fetchsourceledgersforaccountstransactions` leaves its cursor `null` for any combination it does not handle, and smali has no null check. A restricted user opening, for example, Journal (10) or another ttid crashes with a NullPointerException.
- `getWeekdayid`: "Son" typo and locale dependence (see the table).
- Financial-year list ends in March 2029 (see above).
- `fetchuserroles` uses `>` and `fetchuserrolesforspecialaccessrules` uses `>=`. A non-full-access user can therefore assign only roles below their own, but can edit the special-access and menu privileges of their own level and below. Full-access roles are excluded from the second list.
- Only `fetchmobileno2`, `fetchuserrolename`, `fetchuserroledispindex`, `fetchstledgername`, `fetchledgername`, `fetchsupplierledgername`, `fetchcashbankledgername`, `fetchcashbankledgergroupid`, `fetchpartybalance` and `getloanmthodparticularsforselectedloanmethod` catch exceptions (returning "", 0 or an empty object). Every other method lets SQLite exceptions such as "no such table" reach the caller.

## softsettingsvalue — server-driven feature flags
| | |
|---|---|
| **Type** | Holder class of `public static` fields: 65 `id_*` and 7 `vssid_*` int constants, plus the value fields. The constants are compile-time constants inlined by javac, so the loaders contain literal numbers; all of them match. The `_1…_38` suffixes on field names are an old internal sequence, **not** the SSId (e.g. `isSendSMSonDataDownload_32` is SSId 5). |
| **Sources** | SQL Server `SoftSettings(SSId, SSDesc, SSValue, Datevalue, BitValue, NumericValue)` (customer settings, editable in `settings_act`) and `venussoftsettings(VSSId, VSSName, CharValue, NumericValue, BitValue, DateValue)` (vendor settings). There is **no local SQLite softsettings table**; the cache is these fields plus prefs `venusmicrofinpref011`. |

**Loaders.** Each loader is an `if/else-if` chain on SSId/VSSId inside an AsyncTask that reads BitValue, NumericValue or SSValue/CharValue.
1. **`otpverificationact`** runs the first sync after registration on `globalvars.branchconnString`.
   - SSIds: 1–8, 11, 12, 14, 15, 18–27, 30–32, 34, 36–43, 45–51, 53, 55–62, 64, 65, 201, 401. VSS: 22–25.
   - It is the **only** loader of 55, 56, 57, 59 and 60.
   - It persists about 60 pref keys. JADX output of this loop is garbled (exception handlers printed inside the loop); the mapping above comes from the branch constants.
2. **`syncresources`** runs from the drawer "Sync" item (forResult 9026) on `selbranchconnString`.
   - SSIds: 1–8, 11, 12, 14, 15, 18–27, 30–32, 34, 36–43, 45–51, 53, 58, 61–65, 68, 69, 71–74, 101, 102, 201, 401. VSS: 1, 22–27.
   - It persists prefs, including `vss_*`, `LoanRelatedDocuments*`, `enablecashbookverification`, `AreawisePartiesDisplayOrder` and `enabletsendSMSonCollection_38`.
   - When 72 is on, it also downloads `RelatedDocmenttypes(DUID, DocDesc, isvisible, showid, isImageAllowed)`.
   - A second `SSId==39` branch (`selectedfinance_…`) can never run.
3. **`refreshorreloadbrancheslist`** runs on branch reload on `selbranchconnString`. It maps the same ids as syncresources except 71–74, 101, 102, 201 and 401. VSS: 1, 22–25.
4. **`settings_act`** (the Settings screen, `selbranchconnString`) loads the ids including 70, 101 and 102 into its widgets. Save writes each value back with `Update SoftSettings set bitvalue|NumericValue=? where SSId=?` and assigns the fields from the widgets.
5. **`selectbranchandfinance`** (`branchlevelReport_branchconnString`) loads SSIds 14, 20, 34, 39, 45 and 46 of the picked branch into `selectedfinance_*` fields and their prefs.
6. **Inline server SQL** reads two settings directly:
   - SSId 32: `(select bitvalue from softsettings where SSId=32)` inside the device-permission queries of `downloaddata`, `uploadcollection`, `otpverificationact` and `editing_collectionswithfulloptions_act`.
   - SSId 14: `select bitvalue from softsettings where ssid=14` in `editing_collectionswithfulloptions_act`.
7. **Restore:** `navigationdrawactivity.syncsettings()` runs in onCreate and on return from syncresources. It reads about 70 fields back from prefs with defaults. `uploadcollection` re-reads the two report URLs.
8. **In-memory overrides:**
   - `collectionlistprints` sets `collectionreportprintingmethod_21` to the user-picked layout.
   - `editing_collectionsadapter` forces `selectedfinance_showlast5days…` to true.

**Readers** below exclude `settings_act`, which binds almost every field to its UI.

| SSId (`id_…`) | Field | Type | Meaning | Readers | Effect |
|---|---|---|---|---|---|
| 1 `nipbreakforweeklythittam` | `nipbreakforweeklythittam_14` | int | overdue (NIP) break, weekly thittam | `varathittam`, `weeklycollectionanalysis_act` | `@NipLimit` of `Rep24WeekCollection`. Restored from the **wrong pref key** `nipbreakformonthlythittam` (default 20). |
| 2 `nipbreakformonthlythittam` | `nipbreakformonthlythittam_15` | int | NIP break, monthly thittam | — | no functional reader |
| 3 `divisiblevalueforthittam` | `divisiblevalueforthittam_16` | int | thittam rounding divisor | — | no functional reader |
| 4 `businessnameinapp` | `businessnameinapp` | String | business / finance-group name | `navigationdrawactivity`, `refreshorreloadbrancheslist`, `uploadcollection` | drawer header `txtFinanaceGroupNameId`, dialog title, "<name> - <finance>" report text. Pref `FinanceGroupName` (default "GroupName"). |
| 5 `isSendSMSonDataDownload_32` | `isSendSMSonDataDownload_32` | bool | SMS admins on download | `downloaddata` | After download, if `AdminMobileNos` is longer than 9 characters, sends template 1 (4 under branch management) when smsmethod = 1. |
| 6 `isSendSMSonDataUpload_33` | `isSendSMSonDataUpload_33` | bool | SMS admins on upload | `uploadcollection` | When new loans, expenses, collections and new parties all report "Success" and upload is allowed, runs `ASYNCSendSMS` (template 2, or 5 under branch management). JADX shows the test negated; smali `if-eqz` proves the SMS is sent when the flag is **true**. |
| 7 `showdashboardcashabstract` | `showdashboardcashabstract_19` | bool | dashboard cash abstract | — | no effect; persisted but never restored |
| 8 `currentfinancialyearid` | `currentfinancialyearid_20` | int | current FY id (1 = 2011-12 … 18 = 2028-29) | `accountsreports_parameters`, `accounttrans_list`, `accounttransactions_act`, `handloans_list`, `navigationdrawactivity`, `prevloanclosure_list_act` | sets `globalvars.accyearfromdate/accyeartodate`. Restore default 1. |
| 11 `collectionreportprintingmethod` | `collectionreportprintingmethod_21` | int 1–10 | default print layout | `collectionlistprints` | layout switch; 7/8 are monthly layouts. Restore default 1. |
| 12 `deletedataaftercashbookverification_37` | `deletedataaftercashbookverification_37` | bool | wipe offline data once all books are verified | `cashbook` | After verification, if `exec checkisallcashandbankbooksverified` returns UnverifiedCount 0, deletes local LastUpload, NewParties, NewMCLoans, ExpensesEntries and MobileAppCollData. |
| 14 `enablemoniteringonloanclosoure` | `enablemoniteringonloanclosoure_5` | bool | keep loans listed until their closure is uploaded | `downloaddata`, `collectionlistprints`, `otpverificationact`, `loanindexloanswiselist_act/_adapter`, `navigationdrawactivity` | Chooses `APP_MCLoancollectionDatawithLoanClosoureMonitering_with/withoutWeekdayssupport` vs `APP_MCLoancollectionDataAUTOLOANCLOSOURE_With/WithOUTWeekdaysSupport` (the weekday variant for weekly lines). Shows `nav_LoanClosures`. Pref `isEnableMoniteringonLoanclosoure`. |
| 15 `reportingemailaddress` | `reportingemailaddress` | String | report recipient | `cashbook`, `uploadcollection` | `receipantadrees` field posted to the mailer; shown on the upload screen. Default "Not Found". |
| 18 `partiesimagepath` | `partiesimagepath` | String | base URL of party photos | `directcollectionentry`, `partydetails_act`, `partydetails_adapter`, `pattidetails_loans_adapter`, `tabbedcollectionlistadapter(+mviewpager)`, `tabbedindividualcollectionentry` | Glide loads `<path><ImagefileName>` |
| 19 `idproofimagepath` | `idproofimagepath` | String | base URL of ID-proof images | `partydetails_act` | loads and opens ID proof I/II |
| 20 `showpartynoincollectionlist` | `showpartynoincollectionlist_6` | bool | show party no. (help text `getsettingdescription(20)`) | `directcollectionentry`, `editing_collectionsadapter`, `pattidetails_loans_adapter`, `tabbedcollectionlistadapter(+mviewpager)`, `tabbedindividualcollectionentry` | party-number column visible |
| 21 `enablepartyimageloading` | `enablepartyimageloading` | bool | party photos on/off | same 7 classes as 18 | photo capture/upload and list thumbnails |
| 22 `enableidproofimageloading` | `enableidproofimageloading` | bool | ID proof I | `partydetails_act` | show/capture/upload ID proof I |
| 23 `enablevisitcheckup` | `enablevisitcheckup_6a` | bool | "visited" check | `editing_collectionsadapter`, `tabbedcollectionlistadapter(+mviewpager)`, `uploadcollection` | visited checkbox in lists; upload rewrites `PartyVisitStatus` |
| 24 `idproof_file_upload_url` | `idproof_file_upload_url` | String | ID-proof upload endpoint | `partydetails_act` | `HttpURLConnection` POST |
| 25 `party_image_file_upload_url` | `party_image_file_upload_url` | String | photo upload endpoint | `partydetails_act` | same |
| 26 `daily_report_file_upload_url` | `daily_report_file_upload_url` | String | PDF upload endpoint | `cashbook`, `uploadcollection` | OkHttp multipart POST of the day's PDF |
| 27 `email_sending_url` | `email_sending_url` | String | mailer endpoint | `cashbook`, `uploadcollection` | form POST (receipantadrees, emailbody "REFER ATTACHMENT", filenametobesent, emailsubject) |
| 30 `allowoveralllistdownload` | `allowoveralllistdownload_4` | bool | allow "overall list" download | `downloaddata` | Shows the overall-collection checkbox. The pref `AllowOverallListDownload` is written but **never restored**. |
| 31 `strictontwocollectionexecutives` | `strictontwocollectionexecutives_3` | bool | the two executives must differ | `downloaddata` | each executive picker excludes the other selected executive |
| 32 `enablemoniterondownloadandupload` | `enablemoniterondownloadandupload_1` | bool | per-device download/upload control | `navigationdrawactivity`, `uploadcollection` (+ inline server SQL) | Shows `nav_uploadcontrol` and `nav_downloadcontrol`. Download/upload need `isAllowed`, unless special access 6 or full access. |
| 34 `loanstatusmethod` | `loanstatusmethod_10` | int 1/2 | loan-status format | `directcollectionentry`, `editing_collectionsadapter`, `tabbedcollectionlistadapter(+mviewpager)`, `tabbedindividualcollectionentry` | 1 = collected / due + shortfall, 2 = collected / total |
| 36 `enableondaycollection` | `enableondaycollection_27` | bool | same-day collection at loan creation | `newloancreation`, `newloanslist_adapter` | shows the on-day collection field and amount |
| 37 `enablemobilenoinnewloans` | `enablemobilenoinnewloans_28` | bool | mobile no. in new loan | `newloancreation` | shows the field; new-party loans (mode 9901) validate it with `isValidPhoneNumber` |
| 38 `enableremarksinnewloans` | `enableremarksinnewloans_29` | bool | remarks in new loan | `newloancreation` | shows remarks |
| 39 `showlast5dayscollectioninareawisecollectionlist` | `showlast5dayscollectioninareawisecollectionlist_7` | bool | last-5 panel | `tabbedcollectionlistadapter(+mviewpager)` | reads local `PrevCollections(CollDate, Paid)` by MCLoanId |
| 40 `enableuserdefinedcustomernumberinnewcustomercreationscreen` | `enableuserdefinedcustomernumberinnewcustomercreationscreen_30` | bool | user-defined party no. | `partydetails_act` | shows the UD number field |
| 41 `showloanmethodinappcollectionlist` | `showloanmethodinappcollectionlist_9` | bool | loan-method label | `editing_collectionsadapter`, `tabbedcollectionlistadapter(+mviewpager)` | label visible |
| 42 `showzeropartiessubareasin7dayscollectionreport` | `showzeropartiessubareasin7dayscollectionreport_18` | bool | include empty areas | `selectbranchandfinance` | sub areas with 0 parties are inserted into `SelectedFinanceSubAreaDetails` (7-days report) |
| 43 `makeallvaluesnecessaryinnewpartycreationscreen` | `makeallvaluesnecessaryinnewpartycreationscreen_31` | bool | mandatory party fields | `partydetails_act` | e.g. error "Tag Cannot be Empty" |
| 45 `ispenaltyallowedincollectionscreen` | `ispenaltyallowedincollectionscreen_12` | bool | penalty collection | `collectionlist_adapter`, `directcollectionentry`, `editing_collectionlist_act`, `editing_collectionsadapter`, `selectedloancollectionadapter`, `selectedloancollectiondetails`, `tabbedindividualcollectionentry`, `uploadcollection` | penalty input/column, uploaded with the collections |
| 46 `issavingscollectionallowedincollectionscreen` | `issavingscollectionallowedincollectionscreen_13` | bool | savings collection | the same 8 classes + `navigationdrawactivity` | savings input; when off, hides `nav_SavingsClosures` and `nav_prevsavingclosure` |
| 47 `fivedayscollectiondetailsmethod` | `fivedayscollectiondetailsmethod_8` | int 1/2 | last-5 method | `downloaddata`, `editing_collectionswithfulloptions_act`, `otpverificationact` | picks the `PrepareLast5…` procedure (see code lists) |
| 48 `enableIIidproofimageloading` | `enableIIidproofimageloading` | bool | 2nd ID proof | `partydetails_act` | show/capture/upload ID proof II |
| 49 `allowdecimalinamounts` | `allowdecimalinamounts` | bool | decimals in amounts | `ledgerstatement_adapter`, `tbbspl_adapter`, `tbbspl_particulars_adapter` | amount format in ledger / TB / BS / P&L |
| 50 `smsmethod` | `smsmethod` | int 0/1/2 | SMS channel | `cashbook`, `cashreceiveandreturns`, `downloaddata`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `newloancreation`, `tabbedindividualcollectionentry`, `uploadcollection` | 1 = device SMS (`SmsSender`/`SmsManager`); 2 = HTTP gateway (see SmsSender, effectively broken); 0 = none. Restore default 0. |
| 51 `iszerointerestallowed` | `iszerointerestallowed_26` | bool | allow zero/low interest | `newloancreation` | bypasses the check `MCLoanAmount/100 < InterestAmount` |
| 53 `noofdaysallowedinstaffcashbook` | `noofdaysallowedinstaffcashbook_17` | int | back-days window | `cashbook`, `todaycollectionlist` | date picker limited to [today−N, today] unless full access or special access 6 |
| 55 `enablelinewisestaff` | `enablelinewisestaff` | bool | staff assigned per line | `downloaddata`, `employeedetails_act` | executive pickers filtered `FinId = globalvars.finid`; line field on the employee screen |
| 56 `DisabletodayCollectionforIndividualcollectionEntry` | `disabletodayCollectionforIndividualcollectionEntry` | bool | hide current collection | `directcollectionentry`, `tabbedindividualcollectionentry` | Hides the current-collection amount block. Restored from a **wrong-case key**, so it is false after a restart. |
| 57 `Disableshowopeningbalanceaszero` | `showopeningBalASZeroforexecutive` | bool | executive opening balance shown as zero | `downloaddata` | When false, downloads `dbo.showLedgerBalance(CashLedgerId,FinId)` from FinanceMaster into `globalvars.openingbalancedownloaded`. Restored from a key that is never written, so it is false after a restart. |
| 58 `noofbackdaysallowedfordownloaddata` | `noofbackdaysallowedfordownloaddata_2` | int | download back-days | `downloaddata` | picker range [today−(N−1), today] unless special access 6 or full access |
| 59 `STLIntRate` / 60 `OFIntRate` | `STLIntRate` / `OFIntRate` | int | default ST/OF interest rate | — | dead; loaded only at registration |
| 61 `Enablefulloptioonsincollectionlist` | `Enablefulloptioonsincollectionlist_11` | bool | full-option collection editor | `editing_collectionlist_act` | opens `editing_collectionswithfulloptions_act` instead of `editing_collections_act` |
| 62 `MaximumAmountLoanAllowed` | `MaximumLoanAmountAllowed_23` | int | default max loan per party | `partydetails_act`, `partylistwithprevloansfornewloanselection` | default `MaximumAmountAllowed` for new parties (if the field is blank/0). Restore default 1. |
| 63 `MaximumNoofLoansallowedforAllPartieswhenIndividualRestrictionisoff` | `MaximumnoofloansallowedwhilenewpartycreationORnoofloansallowedforallparties_25` | int | default loans per party | `partydetails_act`, `partylistwithprevloansfornewloanselection` | `NoOfLoansAllowed` default. **Never persisted**, so it is 0 after a process restart until the next sync. |
| 64 `EnableMaximumAmountLoanAllowed` | `enableMaximumAmountLoanAllowed_22` | bool | enforce the loan cap | `newloancreation` | An amount above the party's MaximumAmountAllowed is reset to the cap with toast "Loan Amount Exceed". The pref is written only by `settings_act`. |
| 65 `IsRestrictinEnabledonNoofLoansAllowed_IndividualRestriction` | `IsRestrictinEnabledonNoofLoansAllowed_IndividualRestriction_24` | bool | per-party loan count | `partydetails_act` | when on, a new party (mode 9902) gets NoOfLoansAllowed 1; otherwise the value of SSId 63 |
| 68 / 69 / 70 `sendsmsoncashreceipts_34` / `sendsmsontodaycollectionverification_35` / `sendsmsoncashandbankbookverification_36` | same names | bool | SMS on cash receipts / collection verification / book verification | — | settings UI only; never persisted or used |
| 71 `enabletsendSMSonCollection_38` | `enabletsendSMSonCollection_38` | bool | SMS the party on collection | `tabbedindividualcollectionentry` | Tapping a party mobile number sends template 16 through SmsSender; otherwise shows toast "This feature is not enabled". |
| 72 `EnableUploadLoanRelatedDocuments` | `EnableUploadLoanRelatedDocuments` | bool | loan documents feature | `newloancreation`, `tabbedindividualcollectionentry`, `syncresources` | "related documents" buttons; sync of `RelatedDocmenttypes` |
| 73 `LoanRelatedDocumentsUploadURL` | `LoanRelatedDocumentsUploadURL` | String | document upload endpoint | `documentsupload` | `AndroidMultiPartEntity` POST target |
| 74 `LoanRelatedDocumentsUploadPATH` | `LoanRelatedDocumentsUploadPATH` | String | document image base URL | `documentsupload`, `documentsphotossearchlist_adapter` | Glide display |
| 78 `EnableCostCentersforExpenses` | — | — | cost centres | — | constant only; never loaded |
| 101 `enablecashbookverification` | `enablecashbookverification` | bool | cash-book verification | `cashbook`, `uploadcollection` | Enables the Verify button. Upload is blocked once any book of the date is verified (`exec checkisanyoneofcashandorbooksverified` → verifiedCount > 0). |
| 102 `AreawisePartiesDisplayOrder` | `AreawisePartiesDisplayOrder` | int 1–3 | party order inside an area | `tabbedcollectionlist(+mviewpager)`, `tabbedindividualcollectionentry` | 1 = by UDLedgerId (party no.), 2 = by MCLoanDate newest first, 3 = by CWLedgerId. The individual-entry screen handles only 1 and 3. Default 1. |
| 201 `LimitAccountTransactionstoCurrentLine` | `LimitAccountTransactionstoCurrentLine_201` | bool | account entries locked to the current line | `accounttrans_list`, `accounttransactions_act` | finance field locked to `globalvars.curfin`; list via `APP_TransactionslistforSelectedLine` (restricted users get a narrower range) |
| 401 `GroupnameinsteadofAreaname` | `GroupnameinsteadofAreaname_401` | bool | SHG wording | `collectionareadetails_act`, `collectionareassearchlist`, `navigationdrawactivity` | titles "SH Groups" / "குழுக்கள்" instead of "Collection Areas" |
| VSS 1 `vssid_branchname` | `vss_branchname` | String | branch name | `cashbook`, `cashreceiveandreturns`, `downloaddata`, `uploadcollection` | `{BranchName}` placeholder in SMS templates |
| VSS 22 `vssid_executivehandlingmethod` | `vss_executivehandlingmethod` | int 1/2 | Offline / Online executive mode | `navigationdrawactivity`, `collectionareassearchlist`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `tabbedindividualcollectionentry` | 1 = offline drawer group, receive/return money items and last-download time; 2 = online group, "finish collection" menu and finish SMS (calledfrom 9907). Default 0 (neither group). |
| VSS 23 `vssid_enablelivelocationduringcollectionentry` | `vss_enablelivelocationduringcollectionentry` | bool | live location | `editing_collectionswithfulloptions_act` | imports `PrepareLast5Locations @FinId` |
| VSS 24 / 25 `vssid_enablestloansincollection` / `…ofloans…` | `vss_enablestloansincollection` / `vss_enableofloansincollection` | bool | ST / OF loan modules | `navigationdrawactivity` | show `nav_stloanfinancetransactions` / `nav_ofloanfinancetransactions` |
| VSS 26 `vssid_enablefinancecashbook` | `vss_enablefinancecashbook` | bool | finance cash book | — | no reader |
| VSS 27 `vssid_IsthisDBunderBranchManagement` | `vss_IsthisDBunderBranchManagement` | bool | multi-branch DB | `cashbook`, `downloaddata`, `navigationdrawactivity`, `uploadcollection` | picks the branch SMS template variants: download 4/1, upload 5/2, cash book 7/3, receive 14/12, return 15/13 |
| (14/20/34/39) | `selectedfinance_enablemoniteringonloanclosoure`, `selectedfinance_showpartynoincollectionlist`, `selectedfinance_loanstatusmethod`, `selectedfinance_showlast5dayscollectioninareawisecollectionlist` | bool/int | values of the branch picked in sbf | — | written but never read |
| (45/46) | `selectedfinance_ispenaltyallowedincollectionscreen`, `selectedfinance_issavingscollectionallowedincollectionscreen` | bool | same | `editing_collections_act`, `editing_collectionswithfulloptions_act` | penalty/savings fields when editing a selected line's collections. The restore reads the non-`selectedfinance_` keys. |
| (21/18) | `selectedfinance_enablepartyimageloading`, `selectedfinance_partiesimagepath` | bool/String | restored copies of 21/18 | `editing_collectionsadapter` (photo flag only) | — |
| — | `selectedfinance_allowoveralllistdownload`, `selectedfinance_enablevisitcheckup`, `selectedfinance_showloanmethodinappcollectionlist` | bool | — | — | never written or read |
| — | `editingcollections_showpartyphoto`, `vss_isgpstrackingenabled` | `final` bool = false | compile-time switches | — | inlined constants; branches guarded by them are compiled out |
| — | `vss_gpstrackingintervalminutes`=1, `vss_gpsuploadintervalminutes`=1, `vss_gpstrackingstarthour`=6, `vss_gpstrackingendhour`=22 | int | GPS tracking schedule | `staffgpstracking_service` | Interval = minutes × 60,000 ms (JADX shows the multiplier as `RealWebSocket.CANCEL_AFTER_CLOSE_MILLIS`). Tracking runs only between 06:00 and 22:00 (0/0 = always). Never loaded from the server. |

**Rules & quirks.**
- Values live only in static fields. Anything not restored by `syncsettings()` reverts to its Java default after the process is killed: 7, 30, 63, 68–70, 64 (unless saved in Settings) and the `selectedfinance_*` copies of 14/20/34/39.
- There are three pref-key mismatches: the weekly NIP break is restored from the monthly key; 56 uses a wrong-case key; 57 uses a key that is never written.
- Only `otpverificationact` loads 55–57, 59 and 60. A later Sync therefore never refreshes them, but it re-persists the in-memory values.

## Tamil number-word tables — `tamilnumberlists`
| | |
|---|---|
| **Type** | Singleton data class (≈460 KB of UTF-8 source, about 7,000 lines of generated `add()` calls). The odd imports (`TypedValues`, `FrameMetricsAggregator`, `InputDeviceCompat`, `ItemTouchHelper`, `ViewUtils`, `JtdsXid`, `HttpStatus`) are JADX substitutions for numeric literals in `setNumericValue(...)`; they are not real library use. |
| **Used by** | `nototamilwords` only |

Six fetchers return `ArrayList<notowords_data>` (NumericValue, Remaind, TamilCharString):
- `fetch_onetothousandlist()`: 1000 entries, 1 "ஒன்று" … 999 "தொள்ளாயிரத்து தொன்னூற்று ஒன்பது", 1000 "ஆயிரம்".
- `fetch_thousandslist()`: 18 entries, 1–9 thousand. Each value has `Remaind 0` (exact, e.g. "இரண்டாயிரம்") and `Remaind 1` (continuation, e.g. "இரண்டாயிரத்து").
- `fetch_tenthousandslist()`: 180 entries, 10–99 thousand × {exact, continuation}, e.g. "பத்தாயிரம்" / "பத்தாயிரத்து".
- `fetch_lakhslist()`: 199 entries, 1–99 lakh, e.g. "ஒரு இலட்சம்" / "ஒரு இலட்சத்து" … "தொண்ணூற்றொன்பது இலட்சம்".
- `fetch_crorelist()`: 199 entries, 1–99 crore, e.g. "ஒரு கோடி" / "ஒரு கோடியே".
- `fetch_hundredcroreslist()`: 2 entries, "நூறு கோடி" / "நூறு கோடியே".

**Quirks.**
- Only `thousandslist` is re-created on each call. The other five lists are `final` fields that grow by about 1,400 objects on **every** call, so memory grows each time words are computed. Lookups stay correct because the first matching copy is found first.
- `fetch_hundredcroreslist()` resets `thousandslist` instead of its own list (copy-paste bug).

## Amount in Tamil words — `nototamilwords`
| | |
|---|---|
| **Type** | Singleton helper; `tamilcharstringvalue(Double)` |
| **Used by** | `stloans` and `ofloans` (live "amount in words" TextView `stloanamountinwordstv` on every keystroke; printed slips "ரூபாய் … "), `stloaninterestcalculationandpayments`, `ofloans_interestcalculationandpayments` (printed vouchers) |

**How it works.**
1. It reloads all six tables, then peels the amount from the largest unit down:
   - crore: only when 1e7 ≤ N ≤ 1e8;
   - lakh: 1e5–9,999,999;
   - ten-thousands: 10,000–99,999, using `floor(N/1000)` = 10–99;
   - thousands: 1,000–9,999;
   - units: 1–999, via `onetothousand[N−1]`.
2. For each unit it picks the entry with `NumericValue = floor` and `Remaind = (remainder ≠ 0 ? 1 : 0)`, so that "…ஆயிரத்து" is used when a smaller part follows. It reduces N by the modulus only while scanning, which works because index 0 is always the exact form.
3. A non-empty result gets the suffix " மட்டும்" ("only").

**Edge cases.**
- Amounts above 10 crore return "". The 11–99 crore and hundred-crore entries are never used.
- 0 or negative returns "".
- Paise are ignored. A whole-thousand amount with paise, for example 1000.50, reaches `onetothousand.get(−1)` and throws `IndexOutOfBoundsException`.

## Month names — `GetMonthName`
| | |
|---|---|
| **Type** | Static helpers |
| **Used by** | `monthwisepandl` (tab titles: `GetMonthName(position+4)` for Apr–Dec of the from-year, `position−8` for Jan–Mar of the to-year) |

- `GetMonthName(int)` maps 1→"Jan" … 12→"Dec" and anything else to "ERROR".
- `GetMonthNo(String)` failed to decompile in JADX. Smali shows "Jan"→**0** … "Dec"→11 (zero-based, Calendar-style), with unknown values also returning 0. It has no callers.

## SMS sending — `SmsSender`
| | |
|---|---|
| **Type** | Helper constructed with a Context; `sendMultipartSMS(mobileNumbers, text)`, `hasSmsPermission()`, private `openDefaultSmsApp()` |
| **Used by** | `cashbook` (template 3, or 7 under branch management), `cashreceiveandreturns` (template id from intent: 12/14 receive, 13/15 return), `downloaddata` (1/4), `editing_collections_act` (10 delete, 11 update), `newloancreation` (8 loan edit, 9 loan delete), `tabbedindividualcollectionentry` (16, to the party), `uploadcollection` (2/5). `editing_collectionswithfulloptions_act` (templates 2/10/11) calls `SmsManager` directly. |
| **Data** | text from local `SMSTemplates(STId, SMSTitle, SMSDesc)` (downloaded by `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`) via `defaulttables.fetchsmstemplatetext`, with placeholders such as `{BranchName}`, `{LineName}`, `{Finance}`, `{Amount}`, `{executive1}`, `{executive2}`, `{CollAmt}`, `{coldate}`, `{staffname}`, `{partyname}`, `{loanamount}`, `{loandate}`. Recipients are `globalvars.AdminMobileNos` (comma list) or the party mobile. |

**How it works.**
1. If `SEND_SMS` (declared in the manifest, requested in `splash`) is not granted, it opens the default SMS app instead: `ACTION_SENDTO "smsto:<numbers>"`, extra `sms_body`, flag 0x10000000 (NEW_TASK). The user has to press send.
2. Otherwise it calls `SmsManager.getDefault().divideMessage(text)`. One `PendingIntent.getActivity(ctx, 0, Intent(ctx, SmsSender.class), 0x02000000 = FLAG_IMMUTABLE)` is reused as the sent and delivery intent for every part. It targets a non-Activity class, so **no sent or delivery result is ever handled**.
3. Numbers are split on "," and trimmed, and `sendMultipartTextMessage` is called per number. A `SecurityException` falls back to the SMS app; other exceptions are swallowed. Callers toast "SMS Sent" regardless of the outcome.

**smsmethod (SSId 50).** Only 1 uses this class. Method 2 (HTTP gateway) exists only in `editing_collections_act`, `editing_collectionswithfulloptions_act` and `newloancreation`:
- It builds `HttpPost(SMSAPI + "?type=unicode&smstext=…&smsto=…&senderid=…&routeid=8&api_key=…")`, but `SMSAPI` is always "". The `SMSGateWay(SMSGId, SMSAPI, APIKey, SMSSenderId, SMSRoute, SMSType)` table is created and never filled or read, so gateway SMS cannot be delivered.
- One task hard-codes an API key `[REDACTED]` and sender id "STARCB".

## HTTP upload helpers — `FileUpload`, `AndroidMultiPartEntity`
| | |
|---|---|
| **Type** | `FileUpload`: Retrofit wrapper. `AndroidMultiPartEntity`: subclass of Apache HttpMime `MultipartEntity`. |
| **Used by** | `FileUpload`: **nobody** (never instantiated, confirmed in smali). `AndroidMultiPartEntity`: `documentsupload` only. |

**`FileUpload`.**
- `FileUpload(baseUrl)` builds Retrofit (default OkHttpClient, Gson converter) for `ApiService.uploadDatabase(@Part MultipartBody.Part)` = `POST /fileupload.php`.
- `uploadDatabaseFile(path, UploadCallback)` sends form part `file` with media type `multipart/form-data`. The file name is `<dbidentificationno>_<curfin.FinName without spaces>_<device_id>_<yyyyMMdd_HHmmss>.db`. This is apparently meant to back up the local SQLite database to the vendor server, but the feature is unreachable.
- Callbacks: `onSuccess()`, or `onFailure("Upload failed with code: N" | "Error: msg")`.

**`AndroidMultiPartEntity`.**
- `writeTo()` wraps the stream in `CountingOutputStream`, which reports cumulative bytes to `ProgressListener.transferred(long)`.
- `documentsupload` uses it to POST a loan-related document image to `softsettingsvalue.LoanRelatedDocumentsUploadURL`:
  - part `image`: the photo copied to a file named after the document's ImagePath, with field `fname=NEWNAMEOFILE.jpg`;
  - part `website`: "www.venussolutions.net";
  - part `email`: a hard-coded vendor Gmail address.
- HTTP 200 returns the body; anything else returns "Error occurred! Http Status Code: N".
- Quirk: progress is computed as `(long)bytes / (long)total * 100`, so the bar jumps from 0 % to 100 %.

## Glide image loading — `MyGlideModule`, `GlideApp`, `GlideRequests`, `GlideRequest`, `GlideOptions`
| | |
|---|---|
| **Type** | `MyGlideModule`: empty `AppGlideModule`. The rest are classes generated by Glide's annotation processor. |
| **Used by** | `Glide.with(...)` in `directcollectionentry`, `documentsphotossearchlist_adapter`, `documentsupload`, `fullimageview`, `partydetails_act`, `partydetails_adapter`, `pattidetails_loans_adapter`, `tabbedcollectionlistadapter(+mviewpager)`, `tabbedindividualcollectionentry` |

- `com.bumptech.glide.GeneratedAppGlideModuleImpl` discovers `MyGlideModule` and **`OkHttpLibraryGlideModule`** and registers its components, so all http(s) images load through OkHttp 3.
- `GeneratedRequestManagerFactory` makes `Glide.with()` return `GlideRequests`, a `RequestManager` subclass with `as/asBitmap/asDrawable/asGif/asFile/download/downloadOnly/load…` returning the typed `GlideRequest<T>` (a `RequestBuilder` subclass).
- `GlideOptions` is a `RequestOptions` subclass with static factories (`circleCropTransform`, `placeholderOf`, …).
- There are no custom `@GlideOption` extensions. `GlideApp` (the static facade: with/get/init/tearDown/enableHardwareBitmaps/getPhotoCacheDir) is never referenced, because app code calls `Glide.with`.
- Typical call: `Glide.with(ctx).load(partiesimagepath + ImagefileName).apply(RequestOptions.circleCropTransform()).into(thumbnail)`, with drawable `ic_action_aboutus` as the no-photo fallback. `load2` in JADX output is a renamed `load` overload.

## Drag-to-reorder list — `CustomListView`
| | |
|---|---|
| **Type** | Custom View (`ListView` subclass), declared as `@id/listView1` in `activity_collectionareaarangment.xml` and `activity_partyrearrange.xml` |
| **Used by** | `collectionarearearrangeact` (reorder collection areas, saving `update SubAreaDetails set ShowId=…`) and `partyrearrange` (reorder parties, saving `update Parties` / `update MobileAppCollData set UDLedgerId=…`). Both use the adapter `collectionarearearrangeAdapter`. |

**How it works.**
1. `init()` sets single-choice mode and its own scroll listener; the edge auto-scroll step is `15/density` px.
2. Grab: touching a row's grab icon or text makes the adapter call `Listener.onGrab(position, row)`. The host forwards this to `listView.onGrab()`, which:
   - snapshots the row into a hover `BitmapDrawable` (white card, 8px shadow #999999, 1px #EEEEEE border);
   - hides the real row and records the ids of the rows above and below.
3. Moving: `onTouchEvent` moves the hover cell. Vertical movement runs `handleCellSwitch()`: when the hover passes a neighbour's top, it calls `Listener.swapElements(i, j)` (the host swaps its list), then `notifyDataSetChanged`, and animates the neighbour over 150 ms. `handleMobileCellScroll()` auto-scrolls at the edges.
4. Horizontal drag beyond half the row width sets `isTrasboxHover`, a vestigial swipe-to-trash flag. No delete is implemented.
5. Release: `touchEventsEnded()` animates the hover back into place, or `touchEventsCancelled()` drops it.

**Rules.** The view hard-casts its adapter to `collectionarearearrangeAdapter`, so it cannot host any other adapter.

## Fragment base classes — `BaseFragment`, `AbsListViewBaseFragment`, `UnicodeFragment`
| | |
|---|---|
| **Type** | `BaseFragment`, `AbsListViewBaseFragment`: abstract androidx Fragments. `UnicodeFragment`: concrete empty Fragment. |
| **Used by** | `AbsListViewBaseFragment` is extended by the six ST/OF analysis fragments inside `loananddepositanalyisreport`. `BaseFragment` is extended by `AbsListViewBaseFragment` and `partnerwiseperformanceandloans_BaseFragment` (→ `monthlypandl_fragment`, `partnerwiseloansandperformance_fragment`). `BluetoothPrinterMain` places a `UnicodeFragment` in `R.id.container`. |
| **Menu** | `partnerwiseperformance_menu`: `partnerwiseperformancemenu_directprint` "Direct Print" (hidden), `…_showaspdf` "Show as PDF", `…_shareaspdf` "Share"; subclasses handle the clicks |

- `BaseFragment.onCreate` calls `setHasOptionsMenu(true)`, and `onCreateOptionsMenu` inflates the menu above.
- `AbsListViewBaseFragment` is a Universal-Image-Loader sample leftover. Its fields (`listView`, `pauseOnScroll=false`, `pauseOnFling=true`, `STATE_PAUSE_ON_*`) are unused, and `applyScrollListener`, `startImagePagerActivity`, `startcartImagePagerActivity`, `startImageGridActivity`, `productdesignImageGridActivity` and `onPrepareOptionsMenu` are empty.
- `UnicodeFragment` has no `onCreateView`, so it shows nothing. It is a leftover of the NGX printer SDK sample.

## File-selector adapter — `FileListAdapter`
| | |
|---|---|
| **Type** | `BaseAdapter` in package `net.venussolutions.selector` |
| **Used by** | nobody (confirmed in smali) |

It wraps a list of `com.ngxtech.selector.FileData`. `getView` returns a `com.ngxtech.selector.TextViewWithImage` showing the file name. The `com.ngxtech.selector` classes (FileData, FileOperation, FileUtils, OnHandleFileListener, TextViewWithImage) come from the NGX Bluetooth-printer SDK file chooser. Its layouts `dialog.xml` and `file_selector_main.xml` are unused.

## RecyclerView dividers — `customersearchlistdivider`, `recyclerviewdivider`
| | |
|---|---|
| **Type** | `RecyclerView.ItemDecoration`; the two classes are byte-for-byte identical apart from the class name |
| **Used by** | `customersearchlistdivider`: about 90 list screens (`new customersearchlistdivider(ctx, 1, 0)`; margin 5 dp or 36 dp in a few). `recyclerviewdivider`: `refreshorreloadbrancheslist`, `tbbspl_list`, `userroleprevilages_act`. |

- The constructor `(context, orientation, marginDp)` loads the theme's `android:listDivider` drawable. Orientation must be `VERTICAL_LIST = 1` or `HORIZONTAL_LIST = 0`; anything else throws `IllegalArgumentException("invalid orientation")`.
- Vertical: draws the drawable under each child, indented by `margin` dp on the left.
- Horizontal: draws it right of each child, inset top and bottom by `margin` dp.
- `getItemOffsets` reserves the drawable's height or width.

## Sorting helper — `sortComparator`
| | |
|---|---|
| **Type** | Container of `SortBy_Date.Ascending` (non-static inner class) and `SortBy_Date.Descending` (static), both `Comparator<tabbedcollectionlistdata>` |
| **Used by** | nobody (dead; collection lists sort inline per SSId 102) |

Both comparators parse `getMCLoanDate().toString()` (yyyy-MM-dd) with `DateFormat.getDateInstance(FULL, Locale.ENGLISH)`. That parse always fails and the result is cast to `java.sql.Date`, so `compare()` would always return 0. Ascending returns 1/−1 and Descending the reverse.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `collectionarearearrangeAdapter` | `ArrayAdapter<dataforrearrangelist>` | `collectionarearearrangeact`, `partyrearrange` (with `CustomListView`) | Row `collectionarearearangelist_row`: colour strip `lytPatternColorDraw` = item.color, `textPrimaryDisplayMemberId` = primarydisplayvalue, `textSecondaryDisplayMemberId` = secondarydisplayvalue (tag = valuemember). That view is `gone` in this layout, so it never shows, even for parties. Touching the grab icon or either text calls `Listener.onGrab(position, row)` to start a drag. Item ids are the original positions (`mIdMap`); `hasStableIds()` is false. |
| `loananddepositanalysisreportsettings_data` | Serializable model | `defaulttables`, `globalvars.curanalysisreport`, the six analysis fragments | `ttid` plus six SQL/title pairs: `FinWiseAbs*`, `PartnerwiseAbs*`, `FinandPartnerWiseAbs*`, `PartyWiseAbs*`, `IntRateDisAbs*`, `OverallIntRateDisAbs*` (`…SQLStr`, `…HeadStr`); `LoanReportsHead` is never set |
| `collectionreport_data` | model | `collectionlistprints` | One loan from `APP_MCLoancollectionData*`: UDLedgerId, ShowId, SubAreaName, CWLedgerId, LedgerName (PartyName), Tag, MCLoanAmountCollectable, MobileNo1, MCLoanId, MCLoanDate, CurDue (InsAmount), DuesCount, Collected (PrevCollected), LoanBalance (= collectable − collected), CollectionBreakString |
| `daywisecollforreportobj` | model | `collectionlistprints` | MCLoanId, MCLDueColDate, Paid, dayorweekno. Rows come from `GetLastNCollectionforCollReportPrint` or, for layouts 7/8, from `tblMCLoanDueCol`. |
| `spinnersearch_data` | Serializable generic picker row | about every screen; passed to `spinnersearch_list` as extra `searchlist` | displaymember, seconddisplaymember, thirddisplaymember, valuemember (int), secondvaluemember_inttype (int), thirdvaluemember (int), fourthvaluemember (Double), fithvaluemember (double) |
| `finyears_data` | model | `defaulttables` | FYId, FYDesc, SD, ED, isVisible, ShowId |
| `collectionbreaks_data` | model | `defaulttables`, `loanmethods` | cbid, cbstring, cbdisplaystring, collectionintervel (days) |
| `documentchargemethods_data` | model | `defaulttables`, `loanmethods` | methodid, methodname (1 Percentage, 2 Fixed) |
| `notowords_data` | model | `tamilnumberlists`, `nototamilwords` | NumericValue, Remaind (0 = exact word, 1 = continuation form), TamilCharString |
| `defaulttables`, `softsettingsvalue` | DAO / settings holder | (see H3 blocks) | — |
| `tamilnumberlists`, `nototamilwords`, `GetMonthName`, `SmsSender` | helpers | (see H3 blocks) | — |
| `FileUpload`, `AndroidMultiPartEntity` | upload helpers | (see H3 block) | — |
| `MyGlideModule`, `GlideApp`, `GlideRequests`, `GlideRequest`, `GlideOptions` | Glide module / generated API | (see H3 block) | — |
| `CustomListView`, `BaseFragment`, `AbsListViewBaseFragment`, `UnicodeFragment`, `FileListAdapter`, `customersearchlistdivider`, `recyclerviewdivider`, `sortComparator` | view / base / adapter / decoration / comparator | (see H3 blocks) | — |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `collectionarearearangelist_row.xml` | `collectionarearearrangeAdapter` (screens `collectionarearearrangeact`, `partyrearrange`) | RelativeLayout `#lytPattern` (bg #f9f9f9): 16dp colour strip `#lytPatternColor`/`#lytPatternColorDraw`; `#textPrimaryDisplayMemberId` (16sp black, single line); `#textSecondaryDisplayMemberId` (14sp grey, **gone**); `#lytGrab` with `#imageViewGrab` (`@mipmap/ic_drawer` drag handle) |
| `addnewloanbar.xml` | **Included** via `<include>` by `activity_navigationdrawactivity.xml` (navigationdrawactivity) | CoordinatorLayout → AppBarLayout → Toolbar `#toolbar` (style MyToolbar): the main screen's app bar |
| `content_main2.xml` | **Unused** | empty RelativeLayout with activity margins and `appbar_scrolling_view_behavior` (template leftover) |
| `customersearchlist.xml` | **Included** by 20 layouts: activity_spinnersearchlist, activity_sevendayscollectionsubarealist, activity_periodicloanreportlist, activity_uploadalloweddetails, fragment_uploadalloweddetails, fragment_tabbedcollectionlist, activity_nadapunippuphakkilist, activity_employeesearch, activity_daywisependingreport, fragment_sevendayscollection, activity_weeklycollanalysis, activity_todaycollectionlist, fragment_allfinancedailythittam, activity_editingcollections, activity_newloanscollectionlist, activity_nadapunippucollectionlist, activity_closed_remevedloanslist, activity_varakadanillapulist, activity_ledgerstatmentsdateselection, activity_editingfullcollections | RelativeLayout with RecyclerView `#recycler_view` (vertical scrollbars): the common list body |
| `dialog.xml` | **Unused**; only `R$layout` references it (NGX printer SDK file-selector leftover) | `#saveFileDialog`: ListView `#fileList`; label "enterFileName"; EditText `#fileName`; buttons `#fileSaveLoad` "saveButtonText", `#fileCancel` "cancelButtonText", `#newFolder` "newFolderButtonText"; Spinner `#fileFilter` (placeholder texts, no Tamil) |
| `file_selector_main.xml` | **Unused** (same SDK demo) | Buttons `#button_load` "loadButtonText", `#button_save` "saveButtonText" |
| `layout_toolbar_profile.xml` | **Included** by `activity_newpartycreation.xml` (`partydetails_act`) and `activity_partyandpartnerdetailsnew.xml` (`partyandpartnerdetails`; that include is `visibility=gone`) | CircularImageView `#memberimageid` (party photo, default `baseline_account_circle_black_48`, green border) with CircularImageView `#img_plus` edit badge (`ic_action_editprofile`); hidden TextViews "Member Name" (`@string/profile_title`) and "Son" |
| `m3expressive_alert_dialog.xml` | Material Components **library** layout: `android:layout` of style `MaterialAlertDialog.Material3Expressive` (not app code) | AlertDialogLayout `#parentPanel`: include title, `#contentPanel` (scroll indicators, NestedScrollView with `@android:id/message`), `#customPanel`/`#custom`, include `m3_alert_dialog_actions` |
| `m3expressive_alert_dialog_title.xml` | **Included** by `m3expressive_alert_dialog.xml` (library) | `#topPanel` → `#title_template` (`@android:id/icon`, `#alertTitle`), Space `#titleDividerNoCustom` |
| `nav_header_main2.xml` | Navigation drawer **header** (`app:headerLayout` of NavigationView `#nav_view` in `activity_navigationdrawactivity.xml`), filled by `navigationdrawactivity` | bg `side_nav_bar`, launcher icon; `#txtFinanaceGroupNameId` = `softsettingsvalue.businessnameinapp` (placeholder `@string/finaancegroupname` "financegrouname", same in Tamil); `#txtFinanceNameid` = `globalvars.financename` ("BranchName-financename" for multi-branch users); `#txtLoggedUserNameid` = APPUserName; `#txtuserrolenameid` = `fetchuserrolename(app_userroleid)` |
| `partyrearrangelist_row.xml` | **Unused**: `partyrearrange` reuses the adapter above with `collectionarearearangelist_row` | same structure, but the secondary text is visible and there is a hidden "No." prefix (`#textPrimaryDisplayNoId`, orange). It appears intended for the party rearrange screen. |
| `staffgpstrackmap.xml` | **Unused**: byte-identical duplicate of `activity_staffgpstrackmap01.xml`, which `staffgpstrackmap_act` uses | header `#sgt_headerid` (`#sgt_staffnameid`, `#sgt_lastseenid`, `#sgt_trackdateid`); summary bar `#sgt_summarybarid` ("Distance" `#sgt_distanceid`, "Points" `#sgt_pointcountid`, "Collections" `#sgt_collectioncountid`, invisible "Fake GPS" `#sgt_mockcountid` in `#sgt_mockcolid`); map fragment `#sgt_mapid`; "No location recorded for this day" `#sgt_nodataid`; ProgressBar `#sgt_pbarid` |

---
[← brain.md](../../brain.md) · Module 8.19 of 20 · Previous: [8.18 Printing: collection list prints, receipts and Bluetooth printers](18-printing-collection-list-prints-receipts-bluetooth.md) · Next: [8.20 Core constants, global state and network helpers](20-core-constants-global-state-network-helpers.md)
