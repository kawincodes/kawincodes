# 8.6 Collection reports (collection for a day, abstract, 7-days, unpaid parties, day-wise pending, periodic loans)

[← brain.md](../../brain.md) · Module 8.6 of 20 · Previous: [8.5 Online collection entry & editing](05-online-collection-entry-editing.md) · Next: [8.7 Parties (borrowers), new loan creation and KYC documents](07-parties-borrowers-new-loan-creation-kyc.md)

Read-only collection reports that query the branch SQL Server live over jTDS (string-built `exec` calls on stored procedures). They do not read the offline download tables, except for Tag/Remarks look-ups, and the unpaid-parties report, which copies server rows into a local SQLite table before showing them. The module covers: **Collection for a Day** (per-party cash/bank collected for a line and date, with Paid/Cash/Bank/Unpaid filter buttons, search and persisted sorting); the **Collection Abstract** (one row per collection category, which drills down into the day list); the **7-days collection** report (area list → swipeable per-area pages of loans with 7 day-columns and an "attention required" flag saved back to the server); **Unpaid parties** for a date (area → party expandable list); **Day-wise pending**; and **Periodic loans** (loans issued in a date range). No screen in this module prints, shares or creates PDFs. Several screens are unreachable or partly broken in this build; each case is noted under its screen.

## Collection for a Day — `todaycollectionlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest label `@string/app_name`, theme `AppTheme.NoActionBar`; implements `todaycollectionlistadapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_todaycollectionlist.xml` (includes `customersearchlist.xml` → `recycler_view`); rows `todaycollectionlist_row.xml` via `todaycollectionlistadapter` (adapter documented with its own module); menu `menu_todaycollection_sort` |
| **Opened from** | (1) Drawer Reports → `nav_todaycollection` "Collection for a Day / நாளது வசூல்". Extras: `ctid`=7; `financeid`=`globalvars.curfin.getFinId()`; `requestcode`=9003 (`requestcodes.todaycollectiondetails_requestcode_fromabstract`); `collectiontype`="All Parties"; `collectiondate`=`java.sql.Date.valueOf(pref CollectionDate)`, default today. The drawer first puts `requestcode`=9029 (`todaycollectioncalledfromadmin`), then overwrites it with 9003. (2) `todaycollectionabstractlist` row tap (rows with ct ≤ 5): `financeid`, `requestcode`=9003, `collectiondate`, `ctid`=ct, `collectiontype`=Particulars, `Totalamount`, `Partycount`. This screen never reads the last two. (3) `uploadcollection`: the "Upload Status" success dialog has a "Go to Today Collection" button that opens this screen **without extras**. |
| **Opens** | `refreshorreloadfinancelist` ("Select Line", `calledfrom`=5, forResult 5) from the Line label. A row tap builds an Intent for `selectedloancollectiondetails` but never starts it (see Rules). |
| **Data** | Remote, selected-branch DB (`globalvars.selbranchconnString`). With extras: `exec DailyCollectionDetails @ctid=<ctid>, @FinId =<financeid>,@collDate='<yyyy-MM-dd>'`, or `exec DailyCollectionDetailsforweeklyLines` with the same parameters when the line's default loan method has `CBCount` = 7 (weekly line). Without extras: `exec APP_GetTodayCollections @IMEI='<globalvars.device_id>',@FinId=<globalvars.finid>`. Local SQLite (via `defaulttables`): `FinanceDetails` (FinName, DefaultLoanMethodId) and `LoanMethods` (CBCount). On row tap it also reads `Parties` (LedgerName, Tag) and `MobileAppCollData` (Remarks). Prefs `venusmicrofinpref011`: reads and writes `orderByMethod` (int, default 0) and `orderByAsc` (bool, default true). |

**Purpose.** Shows, for one line and one date, every party (or one collection category from the abstract) with the amount collected in cash and through the bank. Staff use it to check collections against installment amounts. They can switch the line or date, filter paid/unpaid parties, search and sort.

**UI.**
- Toolbar `customersarchtoolbar`. Title is "Collection for a Day" (ta "நாளது வசூல்") for ctid 7 or when there are no extras; otherwise it is the passed `collectiontype` (the abstract's category name). Up arrow → back.
- Header `lytdaterangeid`, visible only when ctid = 7:
  - "`Line : <FinanceDetails.FinName>`" (`ds_linename`, chocolate); tap opens the line picker.
  - "Date / தேதி : dd-MM-yyyy" (`ds_fromdateid`, chocolate); tap opens a DatePicker.
  - Refresh ImageButton `acctranslistrefreshid`, which spins during the load.
- Filter bar `lytselectionfilter`, made visible after load only when ctid = 7. It has buttons Paid / Cash / Bank / Unpaid, each with a count label:
  - Paid shows "`<cash total + bank total>/<cashpaid + bankpaid>`".
  - Cash shows "`<cash total>/<rows with cash > 0>`".
  - Bank shows "`<bank total>/<rows with bank > 0>`".
  - Unpaid shows "`<rows with BankCollection = 0>`".
  - The pressed button turns blue (0xFF0000FF) with white text. The others reset to light grey (0xFFCCCCCC) with black text (JADX shows black as `ViewCompat.MEASURED_STATE_MASK` = 0xFF000000).
- Rows (`todaycollectionlistadapter`):
  - "`CWLedgerId. `" + party name.
  - Line 2: "LDate| LAmount| InsAmount".
  - Sub-area name (dark blue, view `tagid`).
  - Cash collected (brown) and bank collected (green).
  - Extra box `extraamtcalc` shows diff = CashCollected − InsAmount when diff ≠ 0, InsAmount > 0 and ctid ≠ 7. Green when positive (excess), red when negative (short).
  - The synthetic "Total" row has a null LDate, so it shows only its name and amounts.
- ProgressBar `todaycollectionlist_progressbarid` replaces the refresh spin when ctid ≠ 7.

**Menu / actions.**
- **Search** (SearchView, always shown) filters live through the adapter:
  - Typing exactly paid/cash/bank/unpaid (any case) acts like the matching filter button.
  - Other text matches PartyName (case-insensitive contains) or SubAreaName (case-sensitive contains).
  - Back closes an open search first.
- **Sort items:**
  - "Party No ↑" / "Party No ↓": method 3, by CWLedgerId.
  - "Regular": method 0, by area ShowId then UdLedgerId. It is ascending only.
  - "Loan Date ↑" / "Loan Date ↓": method 2, by LDate.
  - Each choice calls `mAdapter.updateOrder()` and `savepreferance()`. The saved choice is re-applied after every load and every filter.
- **Filter buttons** send the lower-cased button text to the adapter filter:
  - Paid: cash + bank > 0.
  - Cash: CashCollected > 0 and MCLoanId > 0.
  - Bank: BankCollected > 0 and MCLoanId > 0.
  - Unpaid: cash + bank = 0.
- Line label opens the line picker; Date label opens the DatePicker; Refresh reloads.

**How it works.**
1. `onCreate`:
   - Opens SQLite `venusmicrofinv005` into `globalvars.db` and loads the sort prefs.
   - Reads extras `financeid`, `ctid`, `requestcode`, `collectiontype` and `collectiondate`.
   - Sets the date label, title and line name (`defaulttables.fetchfinanceName` → `FinanceDetails.FinName`), then runs `ASYNCGetCollectionDetails`.
   - With no extras it runs `ASYNCTodayCollection` instead.
   - The header is hidden unless ctid = 7.
2. `ASYNCGetCollectionDetails.doInBackground`:
   - Connects with `selbranchconnString`.
   - `getfinanceobj` loads the line from `FinanceDetails`; `getloanmthodparticularsforselectedloanmethod(DefaultLoanMethodId)` loads its default loan method from `LoanMethods`.
   - CBCount = 7 → weekly SP, otherwise daily SP.
   - Clears the list and maps columns MCLoanId, FWUDMCLoanid, CWLedgerId, PartyLedgerId, LDate, PartyName, Tag, SubAreaName, SubAreaNo, LAmount, Collected, InsAmount, CashCollection, BankCollection and ShowId. `UdLedgerId` is not read on this path, so it is 0.
3. Totals:
   - Sums BankCollection, CashCollection and InsAmount.
   - Counters: `cashpaid` (cash > 0), `bankpaid` (bank > 0) and `unpaid` (bank = 0).
   - An unused `diff` = Σ(Collected − InsAmount) is also computed.
   - When ctid ≠ 7 it appends a synthetic row: PartyName "Total", CWLedgerId 10000, MCLoanId 0, Cash/Bank = the totals, Collected = cash + bank, InsAmount = Σ InsAmount.
4. `onPostExecute`:
   - ctid = 7: shows the filter bar with the counts and stops the spin.
   - Otherwise: hides the progress bar.
   - Then re-applies the saved sort (`updateOrder`) and calls `notifyDataSetChanged`.
5. `ASYNCTodayCollection` (the post-upload path):
   - Runs `APP_GetTodayCollections` with the device's ANDROID_ID and the current line. The result is this executive's own collections for the day.
   - Columns are as above but named `cashCollected` / `BankCollected`, plus `UdLedgerId`.
   - InsAmount is not read, so the excess/short box never appears.
   - No Total row, no header, no filter bar.
6. Line change: `onActivityResult(requestCode 5, resultCode 1)` takes `passedfinance` (`financemaster_data`), sets financeid and the label (`FinMailingName`), then reloads. Choosing a new date also reloads.
7. Date picker:
   - Unrestricted if `globalvars.isFullAccessAllowed` or special access 6 (`allowunconditioanldownlioad_6`).
   - Otherwise min date = today and max date = today + `softsettingsvalue.noofdaysallowedinstaffcashbook_17` days. The value comes from pref `noofdaysallowedinstaffcashbook`, default 1; the static default is 0.

**Functions.**
- `onCreate()`: setup, extras, header visibility, listeners and first load.
- `lambda$onCreate$1/$2/$4/$3/$5`: refresh / line picker / open DatePicker / date chosen / filter-button click.
- `onActivityResult()`: applies the line chosen in `refreshorreloadfinancelist`.
- `resetButtonStyles()`, `highlightButton()`: filter-button colours.
- `onCreateOptionsMenu()`: inflates the sort menu and wires the SearchView.
- `onOptionsItemSelected()`: Up → `onBackPressed`; sort ids → `updateOrder` + save.
- `savepreferance(int,boolean)`: writes `orderByMethod` / `orderByAsc`.
- `sortByPartyNo()`, `sortByUDPosition()`, `sortByLoanDate()`: local comparators. They are never called; the adapter's `applySorting()` does the sorting.
- `startRefreshAnimation()` / `stopRefreshAnimation()`: endless 400 ms rotation of the refresh icon.
- `onContactSelected()`: row tap (see Rules).
- `ASYNCGetCollectionDetails`, `ASYNCTodayCollection`: the loaders above.
- `whiteNotificationBar()`: white status bar with dark icons.

**Rules & edge cases.**
- **A row tap does nothing.** `onContactSelected` builds a `selectedloancollectiondetails` Intent but never calls `startActivity` (verified in smali). With requestcode 9003, which both real launchers pass, it skips even the extras.
- **Date-window bug.** `cashbook` limits the picker to [today−N, today]. This screen omits the subtraction, so restricted users can pick only today to today+N. The same `Calendar c` is also mutated on every opening, so each re-open moves the window another N days forward.
- **Counter bugs:**
  - The Unpaid count counts rows with BankCollection = 0, which includes cash payers. The Unpaid filter itself correctly uses cash + bank = 0.
  - The Paid count double-counts rows paid by both cash and bank.
- **Stale filtered list.** If a filter or search is active, Refresh / line change / date change refill the base list, but the adapter keeps showing the old filtered list until the filter is applied again.
- **Likely crash in drill-down search.** In the drill-down list, which includes the "Total" row with a null SubAreaName, any search text that is not a substring of "total" throws an NPE inside `performFiltering`. Android's Filter catches it and publishes null values, which set the adapter's filtered list to null, so `getItemCount()` is expected to crash.
- The Total row is sorted together with the data rows. CWLedgerId 10000 puts it last under "Party No ↑"; under "Regular" it sorts by ShowId 0.
- The drill-down (ctid ≠ 7) has no refresh, line or date control.
- Errors are only printed (`printStackTrace`); the user sees an empty list.

## Collection Abstract for a Day — `todaycollectionabstractlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); label `@string/app_name`, theme `AppTheme.NoActionBar`; implements `todaycollectionabstract_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_todaycollectionlist.xml` (shared; filter bar and progress bar stay hidden); rows `todaycollectionabstract_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer Reports → `nav_todaycollectionabstract` "Collection Abstract for a Day / நாளது வசூல் சுருக்கம்". Extras: `passedfinanceid`=`globalvars.finid`, `LineName`=`curfin.getFinName()`, `sqlDate`=pref `CollectionDate`. The drawer also sets `globalvars.collectiondate` and `globalvars.isoverallcollectionenabled`. `selectbranchandfinance` has a route for rc 9002 (`todaycollectionabstract_requestcode_frommenu`): its Show button (`btnshowtodaycollectionabstract`) would pass `passedfinanceid`, `LineName` and `sqlDate`=fromdate, but no launcher sends 9002 in this build. |
| **Opens** | `refreshorreloadfinancelist` (`calledfrom` 5, forResult 5); `todaycollectionlist` (drill-down) |
| **Data** | Remote (`selbranchconnString`): `exec DailyCollectionAnalysis @FinId =<financeid>,@collDate='<yyyy-MM-dd>'`. Columns `ct`, `Particulars`, `PartiesCount`, `Collected`. Local `FinanceDetails.FinName` (header). |

**Purpose.** A day summary for one line: one row per collection category (server-defined `Particulars`) with the number of parties and the amount collected. Tapping a category opens the party list.

**UI.** Header (Line / Date / Refresh) as in `todaycollectionlist`. Title "Collection Abstract for a Day" (ta "நாளது வசூல் சுருக்கம்"). Each row shows Particulars, PartiesCount and Collected as three black columns.

**Menu / actions.** Search filters on Particulars (case-insensitive contains); Back closes the search first. Up → back. Line label opens the line picker. Date label opens a DatePicker with no limits; choosing a date only updates the label, and the user must press Refresh to reload. Refresh reloads.

**How it works.**
1. `onCreate` reads the extras, shows the line name (from local `FinanceDetails`) and the date, then runs `ASYNCGetAbstractlist`.
2. `ASYNCGetAbstractlist`:
   - Spins the refresh icon, clears the list and runs `DailyCollectionAnalysis`.
   - Maps each row to `todaycollectionabstract_data` (ctid ← ct, collectiontype ← Particulars, partycount, totalamount).
   - Exceptions only set `ErrorText`, which is never shown, so a failure leaves an empty list.
3. Choosing a line: `onActivityResult(5, 1)` sets financeid and the label from `passedfinance`, then reloads.
4. Row tap: only when `ct ≤ 5`. It opens `todaycollectionlist` with `financeid`, `requestcode`=9003, `collectiondate`=current date, `ctid`, `collectiontype`, `Totalamount` and `Partycount`. Rows with ct > 5 do nothing.

**Functions.**
- `onCreate()`.
- `lambda$onCreate$1` (line picker), `$3` (date picker), `$2` (date set, label only), `$4` (refresh).
- `onActivityResult()`.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`.
- `onContactSelected()` (drill-down).
- `onthumnailclicked()`: empty.
- `animateRefreshButton()`: unused 2-turn spin.
- `startRefreshAnimation()` / `stopRefreshAnimation()`.
- `ASYNCGetAbstractlist`.
- `whiteNotificationBar()`.

**Rules & edge cases.** Rows are shown exactly as the SP returns them; no client-side totals or sorting. Category ids are server-defined; the drawer's "All Parties" detail uses ctid 7, and the abstract drills down only for ct 1–5. Errors fail silently.

## 7-Days Collection — area list — `collectionarealistforsevendayscollection`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default app theme; implements `collectionaarealistadapterforsevendayscollection.ContactsAdapterListener` |
| **Layout(s)** | `activity_sevendayscollectionsubarealist.xml` (includes `customersearchlist`); rows `subarealistforsevendayscollection_row.xml` |
| **Opened from** | Drawer Reports → `nav_sevendayscollection` "Seven Days Collection / ஏழுநாள் வசூல்" opens `selectbranchandfinance` with `requestcode`=9001 (`requestcodes.sevendayscollection_requestcode_frommenu`) and `calledfor`="sevendayscollection". There, the "SHOW 7DC" button (`btnshow7dayscollectionid`, `@string/show_7dc`) runs `ASYNCGet7DaysCollection`, which on success opens this screen with `calledfrom`="customerbranch", `passedfinanceid` and `LineName`. |
| **Opens** | `sevendayscollectionforselline` (`passedfinid`, `listindex` = tapped position) |
| **Data** | Local SQLite `SelectedFinanceSubAreaDetails` (FinId, SubAreaId, SubAreaName), filled by sbf. In-memory `globalvars.sevendayscollectiondetails_list` (list of `sevendayscollectiondata`). No remote call in this class. |

**Purpose.** The entry list of the 7-day collection report. It shows each collection area of the line that has loans in the result, with the loan count.

**UI.** Title "`<LineName> [7DC]`". Each row shows the area name (`@color/areaname`) and the loan count. There is no options menu.

**Menu / actions.** Tapping an area opens the per-area pages, positioned at that area.

**How it works.** The fetch runs upstream, in `selectbranchandfinance.ASYNCGet7DaysCollection`. It is documented with sbf and summarised here because it defines this report.
1. Connects with `branchlevelReport_branchconnString`.
2. Local tables: re-creates and clears `SelectedFinanceSubAreaDetailsALL` and `SelectedFinanceSubAreaDetails`. `GetAllowedFinanceSubAreas @IMEI='<device_id>'` fills the ALL table with (FinId, SubAreaNo, SubAreaName).
3. Stored procedures, one per ticked loan-type checkbox ("Daily Collection" / "Weekly Collection" / "Monthly Collection"; all three are unticked whenever the line changes). All are run as `exec [<proc>] @EndDate='<From Date>', @Daystoadd=0, @FinId=<line>`:
   - Daily: `[APP_Get7daysCollectionfordailyloans]`.
   - Weekly, when `WeekDayId < 0`: `[APP_Get7daysCollectionforweeklyloans]`.
   - Weekly otherwise: `[APP_Get7daysCollectionforweeklyloanswithWeekdayid]`, with `@WeekdayId=<WeekDayId>` added.
   - Monthly: `[APP_Get7daysCollectionforMonthlyloans]`.
4. Weekday parameter:
   - `WeekDayId` = collection-day spinner position + 1. The spinner is shown only for lines whose CollectionBreakString is "W" (JADX prints this as `ExifInterface.LONGITUDE_WEST`) or "ww".
   - The field defaults to 0 and becomes −1 only when the Weekly box is unticked. So the plain weekly SP runs only in that case; otherwise the *withWeekdayid* SP runs, possibly with 0.
   - The "To Date" field is ignored.
5. Row mapping (each row → `sevendayscollectiondata`):
   - Named columns: UdLedgerId, CWLedgerId (not read for monthly), MCLoanId, FWUDMCLoanId, FinId, PartyLedgerId, SubAreaNo, LedgerName, MCLoanDate, MCLoanAmountCollectable, PreCollected, ClosingDate, IsNIP, LoanStatus, ReportIdentification, Tag, IsAttentionRequired.
   - `loanmethodname` = "d" / "w" / "m".
   - Columns 18–24 hold the 7 day amounts. Their column **names** are the dates, parsed as `yyyy-MM-dd` for daily and `dd-MM-yyyy` for weekly/monthly.
6. Area list: for each allowed area of the line, sbf counts the loans. If the count > 0, or soft setting `showzeropartiessubareasin7dayscollectionreport_18` is on, it inserts `'<SubAreaName>-<count>'` into `SelectedFinanceSubAreaDetails`.
7. Errors show an "Error Info" dialog with line number and message; OK closes sbf.
8. `globalvars.SevenDaysCollectionJSON` is only reset to "" and never filled (vestigial).

In this screen, `DisplayIndexData()` reads the areas for `passedfinid`, recounts the loans per area from the global list and appends "-count" again. The adapter shows `split("-")[0]` as the name and `split("-")[1]` as the count.

**Functions.**
- `onCreate()`.
- `DisplayIndexData()`: builds the area list and counts.
- `onContactSelected(Integer pos)`: opens the pages.
- `onthumnailclicked()`: empty.
- `whiteNotificationBar()`.

**Rules & edge cases.**
- An area name containing "-" breaks the display: the name is cut at the first hyphen and the count column shows the next fragment.
- The adapter is Filterable, but no search is wired.
- The data is an in-memory snapshot. If the process was killed, the global list is null, the per-area count throws (caught), and the list shows no areas.
- In sbf, area names are concatenated into the INSERT. A name containing an apostrophe would make the main-thread `execSQL` throw outside any try/catch, so it would likely crash.

## 7-Days Collection — area pages — `sevendayscollectionforselline`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`. Inner classes: `PlaceholderFragment` (Fragment, implements `sevendaycollectionlistadapter.ContactsAdapterListener`), `SectionsPagerAdapter` (FragmentPagerAdapter), `ASYNCupdateemployee` (AsyncTask) |
| **Layout(s)** | `activity_sevendayscollection.xml` (Toolbar + ViewPager `container`); page `fragment_sevendayscollection.xml` (includes `customersearchlist`); rows `sevendayscollectionrow.xml`; menu `menu_sevendayscollection` |
| **Opened from** | `collectionarealistforsevendayscollection` and `collectionarealistforloanpartyandloans` (the latter unreachable): extras `passedfinid`, `listindex` |
| **Opens** | `selectedloancollectiondetails` (`MCLoanId`, `PartyName`=LedgerName, `Tag`) |
| **Data** | Local `SelectedFinanceSubAreaDetails` (one page per area). Also calls `defaulttables.createtableseFinancesinSelectedBranch` (creates `SelectedBranchFinanceDetails`, otherwise unused here). In-memory `globalvars.sevendayscollectiondetails_list`. Remote (`selbranchconnString`): `{call dbo.SetAttentionRequired(?)}` with string parameter `MCLoanIdArray`, run on Back. |

**Purpose.** One swipeable page per collection area. Each page lists the area's loans with the amounts collected on each of the last 7 collection days (or months, for monthly loans). The user can tick an "attention required" flag on a loan, and the flags are saved to the server when leaving.

**UI.**
- Toolbar title ends up as the hard-coded Tamil "7 நாள் வசூல்" ("7-day collection"); the earlier titles "Collection List" / "Parties" are overwritten. The Up arrow is shown but nothing handles it (no parent activity declared).
- Page header `section_label`: the area name as stored (including its "-count" suffix), in blue.
- Row (`sevendayscollectionrow`), sorted by MCLoanDate ascending:
  - Attention checkbox.
  - "`CWLedgerId.LedgerName`" (only LedgerName for monthly rows), in blue when IsNIP = 1.
  - ReportIdentification (loan-method code).
  - "`L:<MCLoanAmountCollectable>`".
  - "`B:<MCLoanAmountCollectable − PreCollected>`" (balance). Visible only with special access 3 (show collected amount) or full access.
  - "LD:dd-MM-yy" (loan date) and "CD:dd-MM-yy" (closing date).
  - "`PD:<LoanStatus>`". Visible only with special access 4 (show loan status) or full access.
  - 7 date headers (dd-MM-yy, or MM-yyyy when loanmethodname = "m") above 7 amounts Day1–Day7.

**Menu / actions.**
- Search filters the static `mAdapter`. A row matches if LedgerName contains the text (case-insensitive, then case-sensitive) or CWLedgerId == `Integer.parseInt(text)`.
- "Prev Week" / "Next Week" items exist but are `visible=false`, have no id and no handler. They suggest week paging through `@Daystoadd` was planned but never built.
- Row tap opens loan details. The checkbox toggles the flag.

**How it works.**
1. `onCreate`:
   - Registers the Back callback and sets up the ViewPager with `SectionsPagerAdapter`, then `setCurrentItem(listindex)`.
   - Reads `passedfinanceid` (never passed), then `passedfinid`.
   - Loads the line's areas (SubAreaId, SubAreaName) from `SelectedFinanceSubAreaDetails` into the static `financelist`. It reuses `financemaster_data`: FinId = SubAreaId, FinMailingName = SubAreaName.
2. `getCount()` re-queries the same table on every call and returns the number of areas. `getItem(p)` → `PlaceholderFragment.newInstance(p+1)`.
3. `PlaceholderFragment.onCreateView`:
   - Takes the area `financelist[section−1]`.
   - Creates a new `sevendaycollectionlistadapter` and stores it in the static `mAdapter`.
   - Fills it with the global loans whose SubAreaNo equals the area id, sorted by MCLoanDate.
4. `setUserVisibleHint()` detaches and re-attaches the fragment on every visibility change. This forces the view to be rebuilt, so `mAdapter` points at the visible page.
5. Row tap: if `specialaccessrules.isAllowtoseepreviousloandetails_9()` (special access 9), it opens `selectedloancollectiondetails`; otherwise Toast "Access Denied".
6. Checkbox change → `onattentionrequiredchanged()` sets `isattentionrequired` on the matching loan in the global list. It compares Integer objects with `==`, which works only because the row object is the same instance.
7. Back → `ASYNCupdateemployee`:
   - Joins the MCLoanIds of every flagged loan in the whole global list into "id1,id2,…," (trailing comma).
   - Calls `SetAttentionRequired`. The result and any error are ignored.
   - The activity finishes immediately.

**Functions.**
- `onCreate()`, `onCreateOptionsMenu()`.
- `whiteNotificationBar()`: never called.
- `PlaceholderFragment.newInstance()`, `.setUserVisibleHint()`, `.onResume()` (trivial), `.onCreateView()`, `.onContactSelected()`, `.onattentionrequiredchanged()`.
- `SectionsPagerAdapter.getItem()` / `.getCount()`.
- `ASYNCupdateemployee.doInBackground()`.

**Rules & edge cases.**
- Flags are saved only through the system Back button (Up does nothing). They are sent on every exit, even with no changes, and the whole flagged set for the line is sent. Server-side semantics are not visible; presumably the set is replaced.
- **Connection mismatch.** Data is read from `branchlevelReport_branchconnString` (the branch chosen in sbf), but flags are written to `selbranchconnString` (the drawer-selected branch). The two differ when a master-DB user picks another branch in sbf.
- **Likely crash in search.** Text that no party name contains reaches `Integer.parseInt`, which throws NumberFormatException; a numeric query hits an NPE on the null CWLedgerId of monthly rows. In both cases Android's Filter publishes null values, which nulls the filtered list → `getItemCount()` NPE. A numeric search works only for daily/weekly rows.
- The static `mAdapter` means search can filter a neighbouring, pre-created page.
- The blue NIP colour is never reset, so recycled rows can show non-NIP parties in blue.
- If the global list is null (process killed), pages render empty; the exception is caught.

## Seven-days tabbed stub (unused template) — `newsevendayscollection`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest label `@string/title_activity_newsevendayscollection` ("newsevendayscollection"), theme `AppTheme.NoActionBar`. Inner `PlaceholderFragment` and `SectionsPagerAdapter` |
| **Layout(s)** | `activity_newsevendayscollection.xml`, `fragment_newsevendayscollection.xml`; menu `menu_newsevendayscollection` |
| **Opened from** | Nothing. Only the manifest references it; no Intent targets it. |
| **Opens** | — |
| **Data** | None |

**Purpose.** The Android Studio "Tabbed Activity" template, left in the APK. It has no business function.

**UI.** A ViewPager with 3 fixed pages, each showing "Hello World from section: N" (`@string/section_format`). The FAB (email icon) shows the Snackbar "Replace with your own action" with an inert "Action".

**Menu / actions.** One overflow item, `action_settings`. Its string resource reads "Cash Book", and tapping it does nothing (it is consumed).

**How it works.** `onCreate` sets up the toolbar, ViewPager and FAB. Pages are `PlaceholderFragment.newInstance(position+1)`, and `getCount()` returns 3. It uses the same detach/attach `setUserVisibleHint` hack as above.

**Functions.**
- `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`.
- `PlaceholderFragment.newInstance()` / `.onCreateView()` / `.setUserVisibleHint()`.
- `SectionsPagerAdapter.getItem()` / `.getCount()`.

**Rules & edge cases.** Unreachable dead code.

## Unpaid Parties — date selection — `selectdateforreport`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default theme |
| **Layout(s)** | `activity_selectdateforreport.xml` |
| **Opened from** | Drawer Reports → `nav_unpaidparties` "Unpaid Parties / கட்டாதவர்கள்" (no extras) |
| **Opens** | `unpaidpartiesonselectedday` (`CALLEDFROM`=2; the target never reads it) |
| **Data** | Remote (`selbranchconnString`): `exec APP_unpaidpartiesforselecteddate @ColDate='<yyyy-MM-dd>',@FinId=<globalvars.finid>`. Local SQLite table `UnpaidParties`, dropped and re-created on every run. Columns: MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, SubAreaName, ShowId, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus. |

**Purpose.** Pick a date, copy the current line's unpaid loans for that date into SQLite, then show them grouped by area.

**UI.** Title "Unpaid Parties for the date". A large bold date (30sp, default today, dd-MM-yyyy); tapping it opens a DatePicker with no limits. A large "Show" button. While loading, a hidden ProgressBar and a status text (DarkGoldenrod background) appear.

**Menu / actions.** Show → load, then open the result.

**How it works.**
1. Show runs `ASYNCcollectiondetailsr`. The progress text reads "Connecting" (5), then "Loading Collection Groups" (15).
2. SQLite steps:
   - `CREATE TABLE IF NOT EXISTS Unpaidparties(<old 24-column schema>)`
   - `drop table Unpaidparties`
   - `CREATE TABLE IF NOT EXISTS UnpaidParties(<26-column schema with SubAreaName, ShowId>)`
   - `delete from UnpaidParties`
   - SQLite table names are case-insensitive, so this always re-creates a single table with the new schema.
3. Runs the SP. Each row → `ContentValues` → `insert("Unpaidparties", …)`; dates are stored as `yyyy-MM-dd` text.
4. `onPostExecute` always opens `unpaidpartiesonselectedday`, even after a failure, and hides the progress views.

**Functions.**
- `onCreate()`.
- `lambda$onCreate$2` (open DatePicker), `lambda$onCreate$1` (date set), `lambda$onCreate$3` (Show).
- `ASYNCcollectiondetailsr.onPreExecute()` / `.doInBackground()` / `.onProgressUpdate()` / `.onPostExecute()`.

**Rules & edge cases.**
- Always uses the current line `globalvars.finid`; there is no line or branch selector.
- Connection and SQL errors are swallowed. The local table has already been cleared, so the result screen is empty.
- Show is not disabled during a load, so repeated taps start parallel loads and open several result screens.

## Unpaid Parties (result) — `unpaidpartiesonselectedday`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest label "Unpaid Parties" |
| **Layout(s)** | `activity_unpaidpartiesselectedday.xml` (ExpandableListView `lv`). Groups `item_collectionareaunpaid.xml`; the nested `unpaidpartiesonselecteddayview` uses `item_partiesunpaid.xml` and `item_collectiondetailsunpaid.xml` |
| **Opened from** | `selectdateforreport` (`CALLEDFROM`=2) |
| **Opens** | — |
| **Data** | Local SQLite only. Groups: `select SubAreaNo,SubAreaName,count(MCLoanId) as UnpaidPartiesCount from UnPaidParties group by SubAreaNo,SubAreaName,ShowId order by ShowId`. Per area: `Select MCLoanId, … LoanStatus from UnpaidParties Where SubAreaNo=<n> order by UDLedgerId` |

**Purpose.** A read-only list of the parties who did not pay on the chosen date, grouped by collection area in route (ShowId) order.

**UI.**
- Level 1: "N. " (ordinal, 16sp) + area name (14sp).
- Expanding an area shows one child: a nested, fully expanded list of unpaid party names (indented 40dp), in UDLedgerId order.
- Level 3 (80dp "details") is never filled. All checkboxes are `visibility=gone`.

**Menu / actions.** Expand and collapse areas only. No menu, search, refresh or drill-down.

**How it works.** `initDate()` builds one `collectionareas_unpaiddata` per area (SubAreaNo, SubAreaName, UnpaidPartiesCount, party list). For each loan row it adds a `parties_unpaiddata` with only PartyName set; every other column is read and discarded. The third-level loop `while (k < 0)` never runs. `onCreate` attaches `unpaidpartiesonselectedday_adapter` to `lv`.

**Functions.**
- `onCreate()`.
- `initDate()`: loads groups and parties.
- `btnOnclick(View)`: leftover sample code that collects the checked level-3 items with Chinese labels "一级 / 二级 / 三级" (level 1/2/3). No view binds to it.

**Rules & edge cases.** The per-area unpaid count is computed but never displayed. No amounts, phone numbers or loan details are shown. To change the date, go back to the previous screen.

## Day-wise Pending — `daywisependingreport_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`; implements `daywisependingreport_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_daywisependingreport.xml` (includes `customersearchlist`); rows `daywisependingreport_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer Reports → `nav_Daywisepending` "Daywise Pending / நிலுவை(நாள் வரியாக)" → `selectbranchandfinance` rc 9025 (`daywisepending_requestcode`; date labels "From Date" / "Report Date") → Show (`btnshowtodaycollectionabstract`). Extras: `selectedfinanceid`, `weekdayid`=−1, `fromdate`, `todate` |
| **Opens** | Nothing in practice. A row tap builds an unused Intent for `newloancreation` (`CallType`=2, `MCLoanId`, `position`, `PartyName`). `oncollectiondetailslinkclicked` would open `selectedloancollectiondetails` (`MCLoanId`, `PartyName`, `Remarks`, `Tag` from local `Parties`), but nothing triggers it. |
| **Data** | Remote (`branchlevelReport_branchconnString`): `exec APP_PendingReportAUTOLOANCLOSOURE_WithOUTWeekdaysSupport @ColDate='<todate>', @FinId=<financeid>` |

**Purpose.** Lists the line's loans with pending dues as of the report date, with installment, balance and collected-so-far figures.

**UI.** Title "Daywise Pending". Each row, in SP order:
- "`CWLedgerId.PartyName`" and the area name (goldenrod).
- "Loan: / கடன்:" MCLoanAmountCollectable.
- "Ins: / தவணை:" InsAmount.
- "Dues:" LoanStatusType2 (the pending-dues count).
- "Date: / தேதி:" MCLoanDate "/" ClosingDate (dd-MM-yy).
- "Balance:" Balance.
- "Coll:" PrevCollected.
- A red loan-method tag that is never bound, so it always shows the layout placeholder "100D". The collection-details icon stays hidden.

**Menu / actions.** Search:
- Digits-only text shows rows whose LoanStatusType2 equals the number ("loans with exactly N pending dues").
- Other text matches SubAreaName (lower-cased) containing the text, or PartyName containing it (case-sensitive).

Back closes the search first. Up handles both the app id `R.id.home` and `android.R.id.home`. A row tap does nothing.

**How it works.**
1. `onCreate` reads the extras and runs `ASYNCGetNewpartiesNewLoans` (ProgressDialog "Working").
2. It runs the SP and maps 35 columns into `daywisependingreport_data` (see the model table).
3. `onPostExecute` dismisses the dialog and refreshes the list.
4. Only `todate` ("Report Date") is sent. `fromdate` and `weekdayid` are ignored.

**Functions.**
- `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`.
- `onContactSelected()`: builds an Intent that is never started (verified in smali).
- `oncollectiondetailslinkclicked()`: unreachable.
- `ASYNCGetNewpartiesNewLoans`.
- `whiteNotificationBar()`.

**Rules & edge cases.** The catch block sets `iserroroccured = false` (bug), so errors never show the "Error..." dialog. That dialog would read `ErrorText`, which is never set, while the message goes into `Errormsg`. The result is a silent empty list. Neither the row tap nor the details link works.

## Periodic Loans — `periodicloareportlist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`; implements `periodicloanreport_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_periodicloanreportlist.xml` (includes `customersearchlist`); rows `periodicloanreport_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | `selectbranchandfinance` rc 9005 (`periodicloans_requestcode`; "From Date" / "To Date", Show). Extras: `passedfinanceid`, `LineName`, `fromdate`, `todate`. **No menu item or screen sends 9005 in this build, so the UI cannot reach this screen.** |
| **Opens** | `selectedloancollectiondetails` (`MCLoanId`, `PartyName`=Party, `Remarks`, `Tag`, `iscollectioneditallowed`="0") |
| **Data** | Remote: on a master DB, `branchlevelReport_branchconnString`; otherwise `branchconnString`. `exec GetLoanDetailsforPeriod @FinId =<id>,@FromDate='<from>' ,@ToDate='<to>'` returns TypeId, MCLoanId, SNo, SubAreaName, Date, Party, LT, Loan, Dues, IntRate, Interest, PartyLedgerId, Mahimai, SubAreaNo, showid, Balance. TypeId, IntRate and showid are read but unused. Local `Parties` (Tag) and `MobileAppCollData` (Remarks). |

**Purpose.** Lists the loans issued on a line in a date range: loan amount, dues, interest, document charges (Mahimai) and balance.

**UI.** Title "Periodic Loans". Row line 1: Party, Loan, Dues, Interest, Mahimai. Line 2: Area, LT (loan type/method code), Balance and loan date (dd-MM-yy). A row whose Party is "Total" (sent by the SP) shows a blank date and LT.

**Menu / actions.** Search on Party: the lower-cased party name must contain the raw query, so upper-case letters never match. Up → back. A row tap opens the loan's collection details.

**How it works.**
1. `ASYNCGetAbstractlist` shows ProgressDialog "Loading Details" and maps the rows into `periodicloanreport_data`.
2. On error: "Error Info..." dialog with the exception text; OK cancels and finishes the screen.
3. Otherwise it hides the dialog and refreshes the list.
4. Row tap reads Tag from local `Parties` and Remarks from local `MobileAppCollData`, then opens `selectedloancollectiondetails`. `iscollectioneditallowed` is passed as the String "0", so the target's `getBoolean` returns false and editing is disabled.

**Functions.**
- `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`.
- `onActivityResult()`: no-op.
- `onContactSelected()`.
- `onthumnailclicked()`: empty.
- `ASYNCGetAbstractlist`.
- `whiteNotificationBar()`.

**Rules & edge cases.** Tapping the "Total" row also opens details, using that row's MCLoanId (probably 0). The app computes no totals of its own.

## Collection areas for Loan Parties/Loans — `collectionarealistforloanpartyandloans`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default theme; implements `collectionarealistadapterforloanpartiesandloans.ContactsAdapterListener` |
| **Layout(s)** | `activity_sevendayscollectionsubarealist.xml`; rows `areawisecollectionlistrow.xml` |
| **Opened from** | `selectbranchandfinance` "SHOW 7DC" when `calledfor == "loanparties"` (extras `passedfinanceid`, `LineName`, `WeekDayId`). No caller ever passes that value: sbf reads `calledfor` only for rc 9001, the drawer sends "sevendayscollection", and `nav_loanparties` uses rc 113 → `partydetails_searchlist`. **Unreachable in this build.** |
| **Opens** | `sevendayscollectionforselline` (`passedfinid`, `listindex`) |
| **Data** | Local `SelectedFinanceSubAreaDetails`: whatever the last 7-day run left there |

**Purpose.** Intended as the collection-area picker for the admin Loan Parties / Loans flows. As coded, it lists areas and opens the 7-day pages.

**UI.** Title "`Collection Areas in <LineName>`". Each row shows the area name in bold green. The value views (parties, marked, NIP, warn, collected, collectable) are hidden, but their captions "Parties", "Marked", "NIP" and "Warn" still show.

**Menu / actions.** Tapping an area opens `sevendayscollectionforselline` at that index. There is no menu; the `searchView` field is never initialised.

**How it works.** `DisplayIndexData()` loads the area rows for `passedfinid` without recounting, then calls `notifyDataSetChanged()`.

**Functions.**
- `onCreate()`, `DisplayIndexData()`.
- `onContactSelected(Integer)`.
- `onthumnailclicked()`: empty.
- `whiteNotificationBar()`.

**Rules & edge cases.** Depends on data from an earlier 7-day report, so it is stale or empty otherwise; area names still carry the "-count" suffix. `WeekDayId` is never read. The adapter's filter upper-cases the query but compares it with the lower-cased name, so letters never match (unused anyway).

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `todaycollectionabstract_adapter` | RecyclerView adapter, Filterable | `todaycollectionabstractlist` | Row `todaycollectionabstract_row`: collectiontype, partycount, totalamount. Row click → `onContactSelected(data)`; `onthumnailclicked` is declared but never called. Filter: collectiontype contains query (case-insensitive). |
| `collectionaarealistadapterforsevendayscollection` | RecyclerView adapter, Filterable | `collectionarealistforsevendayscollection` | Row `subarealistforsevendayscollection_row`: area = `SubAreaName.split("-")[0]`, count = `split("-")[1]` (always visible). Click → `onContactSelected(adapterPosition)`. Filter: lower-cased name contains query; not wired to any SearchView. |
| `sevendaycollectionlistadapter` | RecyclerView adapter, Filterable | `sevendayscollectionforselline.PlaceholderFragment` | Row `sevendayscollectionrow`: name (blue if IsNIP = 1), L:, B: (special access 3 or full access), LD:, CD:, PD: (special access 4 or full access), ReportIdentification, 7 date labels (MM-yyyy for "m", else dd-MM-yy) and 7 day amounts. Row click → `onContactSelected(data)`; checkbox → `onattentionrequiredchanged(data, checked)`. Filter: name contains, or `CWLedgerId == Integer.parseInt(query)` (crash-prone, see screen). |
| `unpaidpartiesonselectedday_adapter` | BaseExpandableListAdapter; inner `SecondAdapter` (BaseExpandableListAdapter), holders `FirstHolder`/`SecondHolder`/`ThirdHolder` | `unpaidpartiesonselectedday` | Groups = areas (`item_collectionareaunpaid`: "N. " + SubAreaName). Each group has exactly one child: a new `unpaidpartiesonselecteddayview` whose `SecondAdapter` (re-created on every `getChildView`) lists parties (`item_partiesunpaid`: PartyName) and their details (`item_collectiondetailsunpaid`: title; always empty). Checkbox clicks cascade the check state (area → all its details, party → its details, detail → itself) and log to tag "bigname", but every checkbox is hidden. `isChildSelectable` is false. |
| `daywisependingreport_adapter` | RecyclerView adapter, Filterable | `daywisependingreport_act` | Row `daywisependingreport_row`: CWLedgerId.PartyName, area, Loan (MCLoanAmountCollectable), Ins (InsAmount), Dues (LoanStatusType2), Date/Close date, Balance, Coll (PrevCollected). The loan-method view is never set. Click → `onContactSelected(data, position)`. `oncollectiondetailslinkclicked` is never invoked because the icon is not bound. Filter: digits → LoanStatusType2 equals the number; text → area or party contains. |
| `periodicloanreport_adapter` | RecyclerView adapter, Filterable | `periodicloareportlist_act` | Row `periodicloanreport_row`: Party, Loan, Dues, Interest, Mahimai, Area, LT, Balance, date. For the "Total" row the date and LT are blank. Click → `onContactSelected(data)`. Filter: lower-cased Party contains the raw query. |
| `collectionarealistadapterforloanpartiesandloans` | RecyclerView adapter, Filterable | `collectionarealistforloanpartyandloans` | Row `areawisecollectionlistrow`: sets only the area name and hides all value TextViews. Click → `onContactSelected(adapterPosition)`. Filter upper-cases the query against the lower-cased name (never matches letters). |
| `unpaidpartiesonselecteddayview` | Custom view (ExpandableListView subclass, 4 standard constructors) | `unpaidpartiesonselectedday_adapter` | `onMeasure` passes `MeasureSpec.makeMeasureSpec(536870911 = 0x1FFFFFFF, Integer.MIN_VALUE = AT_MOST)`, so the nested list measures its full height inside the parent ExpandableListView (no inner scrolling). |
| `todaycollectionsdata` | Plain data model | `todaycollectionlist`, `todaycollectionlistadapter` | int MCLoanId, FWUDMCLoanId, CWLedgerId, PartyLedgerId, LAmount, Collected, CashCollected, BankCollected, SubAreaNo, subareashowid, udledgerid, targetledgerid (unused); `dueamount` (backing field of `get/setInsAmount`); `java.sql.Date` LDate; String PartyName, Tag, SubAreaName. |
| `todaycollectionabstract_data` | Plain data model | `todaycollectionabstractlist`, its adapter | int ctid, partycount, totalamount; String collectiontype. |
| `sevendayscollectiondata` | Data model (Serializable) | sbf `ASYNCGet7DaysCollection`, 7-day screens/adapter | Integer CWLedgerId, UDLedgerId, MCLoanId, FWUDMCLoanId, FinId, PartyLedgerId, SubAreaNo, MCLoanAmountCollectable, PreCollected, Day1–Day7; int IsNIP; String LedgerName, LoanStatus, ReportIdentification, Tag, loanmethodname ("d"/"w"/"m"); `java.sql.Date` MCLoanDate, ClosingDate, day1date–day7date; boolean isattentionrequired. |
| `sevendaycollectionlabel_data` | Plain data model (unused) | — | String Label1–Label7, apparently meant for the 7 day headers. Never referenced. |
| `collectionareaslistdataforsevendayscolletion` | Plain data model | 7-day area list, loan-party area list, sbf | Integer SubAreaNo; String SubAreaName (may carry "-count" suffixes). |
| `collectionareas_unpaiddata` | Plain data model | Unpaid parties | Double SubAreaNo; String SubAreaName; int UnpaidPartiesCount; `List<parties_unpaiddata>` listPartiesunpaiddata; no-arg and 4-arg constructors. |
| `parties_unpaiddata` | Plain data model | Unpaid parties | Double MCLoanId, FWUDMCLoanId, PartyLedgerId, CWLedgerId, SubAreaNo, LAmount, DuesCount, MCLoanAmountCollectable, PrevCollected, NoOfIns, LoanStatus; int UDLedgerId, ShowId, InsAmount, PaidDues, PassedDues, CBCount, istobeWarned, IsNIP; String Remarks, MCLoanDate, ClosingDate, PartyName, SubAreaName, LoanMethodDesc, CollectionBreakString; `List<collectiondetails_unpaiddata>`. Only PartyName and the list are ever set. |
| `collectiondetails_unpaiddata` | Plain data model | Unpaid parties (level 3) | boolean isCheck; String title. Never instantiated at runtime (the loop runs 0 times). |
| `daywisependingreport_data` | Data model (Serializable) | `daywisependingreport_act`, its adapter | Double MCLoanId, FWUDMCLoanId, PartyLedgerId, CWLedgerId, SubAreaNo, LAmount, DuesCount, MCLoanAmountCollectable, PrevCollected, Balance, NoOfIns, LoanStatus, InterestAmount; int ShowId, UDLedgerId, InsAmount, NoofCollections, PaidDues, PassedDues, CBCount, istobeWarned, IsNIP, LoanStatusType2; String Remarks, PartyName, MobileNo1, MobileNo2, Imagefilename, SubAreaName, LoanMethodDesc, ReportIdentification, CollectionBreakString; `java.sql.Date` MCLoanDate, ClosingDate, LastCollDate. |
| `periodicloanreport_data` | Data model (Serializable) | `periodicloareportlist_act`, its adapter | int SNo, MCLoanId, PartyLedgerId, Loan, Dues, Interest, IntRate (never set), Mahimai, SubAreaNo, Balance; String Party, LT, SubAreaName; `java.sql.Date` MCLoanDate. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_todaycollectionlist.xml` | `todaycollectionlist`, `todaycollectionabstractlist` | CoordinatorLayout `main`: Toolbar `customersarchtoolbar`. Header `lytdaterangeid` with "Line" ":" `ds_linename` (Chocolate), "Date/தேதி" ":" `ds_fromdateid` (sample 01-06-2001) and refresh ImageButton `acctranslistrefreshid`. Filter bar `lytselectionfilter` (gone): buttons `btnpaidparties` "Paid", `btncashpaidparties` "Cash", `btnbankpaidparties` "Bank", `btnunpaidparties` "Unpaid", each with a count TextView `paidpartiescounttvid` / `cashpartiescounttvid` / `bankpartiescounttvid` / `unpaidpartiescounttvid` ("..."). Include `customersearchlist` (RecyclerView `recycler_view`). ProgressBar `todaycollectionlist_progressbarid` (gone). |
| `todaycollectionabstract_row.xml` | `todaycollectionabstract_adapter` | Selectable row with three black TextViews: `todaycollectionabstractrow_collectiontypeid` ("100 days col"), `todaycollectionabstractrow_partiescountid` ("250"), `todaycollectionabstractrow_collectedamountid` ("20050") |
| `activity_sevendayscollectionsubarealist.xml` | `collectionarealistforsevendayscollection`, `collectionarealistforloanpartyandloans` | ConstraintLayout `main` with include `customersearchlist` |
| `subarealistforsevendayscollection_row.xml` | `collectionaarealistadapterforsevendayscollection` | Selectable row: `subarealistforsevendayscollection_areanameid` (`@color/areaname`, "Area Name"), `subarealistforsevendayscollection_noofpartiesid` (black, "Collected") |
| `activity_sevendayscollection.xml` | `sevendayscollectionforselline` | CoordinatorLayout; AppBarLayout `main` holding Toolbar `toolbar` (app:title "Sevendays Collection", replaced in code); ViewPager `container` |
| `fragment_sevendayscollection.xml` | `sevendayscollectionforselline.PlaceholderFragment` | `section_label` ("Finance Name"; set to blue in code) + include `customersearchlist` |
| `sevendayscollectionrow.xml` | `sevendaycollectionlistadapter` | `firstrowid`: CheckBox `isattentionrequiredcheckboxid`, `sevendayscollectionpartynameid`, `sevendayscollectionreportidentificatonid` ("LM"). `lytloandetailsid`: `sdcloanamountid`, `sdccollectedamountid`, `sdcloandateid`, `sdcloanclosingdateid`, `sdcloanstatusidd`. `lytdatedetails`: `day1dateid`–`day7dateid` ("12-10-20") and `day1id`–`day7id` ("\|\|"). Hidden `tvcollectedamountnewid` ("2500") |
| `activity_newsevendayscollection.xml` | `newsevendayscollection` | CoordinatorLayout `main`: Toolbar `toolbar` (title app_name), ViewPager `container`, FAB `fab` (`ic_dialog_email`) |
| `fragment_newsevendayscollection.xml` | `newsevendayscollection.PlaceholderFragment` | ConstraintLayout `constraintLayout` with TextView `section_label` |
| `activity_selectdateforreport.xml` | `selectdateforreport` | ConstraintLayout `main`: `seldate_dateid` (30sp bold, "01-12-2019"), Button `seldate_showbuttonid` "Show" (30sp bold), ProgressBar `pbarid` (gone), TextView `txtprogressid` ("prgress", DarkGoldenrod background, gone) |
| `activity_unpaidpartiesselectedday.xml` | `unpaidpartiesonselectedday` | LinearLayout `main` with ExpandableListView `lv` |
| `item_collectionareaunpaid.xml` | `unpaidpartiesonselectedday_adapter` (level 1) | `uparea_showid` ("01. ", 16sp black), `tv` (14sp black), CheckBox `cb` (gone) |
| `item_partiesunpaid.xml` | `unpaidpartiesonselectedday_adapter.SecondAdapter` groups (level 2) | RelativeLayout with paddingLeft 40dp: TextView `tv`, CheckBox `cb` (gone) |
| `item_collectiondetailsunpaid.xml` | `unpaidpartiesonselectedday_adapter.SecondAdapter` children (level 3, never filled) | RelativeLayout with paddingLeft 80dp: TextView `tv`, CheckBox `cb` (gone) |
| `activity_daywisependingreport.xml` | `daywisependingreport_act` | CoordinatorLayout `main`: Toolbar `toolbar`; include `customersearchlist` |
| `daywisependingreport_row.xml` | `daywisependingreport_adapter` | `newloanlistpartynameid` (black, "Party Name"); `row2`: `newloanlistareanameid` (Goldenrod). `row3`: "Loan:/கடன்:" (DarkBlue) `newloanlist_loanamountid`, "Ins:/தவணை:" (DarkGreen) `newloanlist_dues1id`, "Dues:" (DarkViolet) `newloanlist_duesid`. Then "Date:/தேதி:" `newloanlist_loandateid` "/" `newloanlist_Closeloandateid`, "Balance:" `newloanlist_balanceid`, `newloanlist_ondaycollectionlayoutid` with "Coll:" (FireBrick) `newloanlistcollectionamountid`. `newloanlist_loanmethodid` (Red, "100D"); ImageView `newloanlist_collectiondetailsid` (`ic_action_collection`, gone) |
| `activity_periodicloanreportlist.xml` | `periodicloareportlist_act` | CoordinatorLayout `main`: Toolbar `toolbar`; include `customersearchlist` |
| `periodicloanreport_row.xml` | `periodicloanreport_adapter` | Selectable row, all text black. Line 1: `periodicloanreportrow_partyid`, `_loanid`, `_duesid`, `_interestid`, `_mahimaiid`. Line 2: `_areaid`, `_loanmethodid`, `_balanceid`, `_loandateid` |
| `areawisecollectionlistrow.xml` | `collectionarealistadapterforloanpartiesandloans` (also used by `areawisecollectionlistadapter`, another module) | Card `awcl_rowcardid` (`bg_row_card`): `awcl_areanameid` (bold green). Counters "Parties" `awcl_NoOfPartiesId`; `awcl_markedcolid` "Marked" `awcl_NoOfmarkedPartiesId` (gold); `awcl_nipcolid` "NIP" (`niplabelid`) `awcl_NoOfNipPartiesId` (red); `awcl_warncolid` "Warn" `awcl_NoOfWarningPartiesId` (orange). `awcl_collectedamountId` (green chip `bg_due_chip`); divider View (gone); `awcl_collectableamountId` (grey, gone) |

Menus used: `menu_todaycollection_sort` (search + 5 sort items), `menuonlywithsearchoption` (search only), `menu_sevendayscollection` (search + hidden "Prev Week" / "Next Week"), `menu_newsevendayscollection` (`action_settings`, titled "Cash Book"). Layouts from other modules that appear here: `customersearchlist.xml` (RecyclerView `recycler_view`) and `todaycollectionlist_row.xml` (row of `todaycollectionlistadapter`).

---
[← brain.md](../../brain.md) · Module 8.6 of 20 · Previous: [8.5 Online collection entry & editing](05-online-collection-entry-editing.md) · Next: [8.7 Parties (borrowers), new loan creation and KYC documents](07-parties-borrowers-new-loan-creation-kyc.md)
