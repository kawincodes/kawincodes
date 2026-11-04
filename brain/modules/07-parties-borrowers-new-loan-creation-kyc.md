# 8.7 Parties (borrowers), new loan creation and KYC documents

[← brain.md](../../brain.md) · Module 8.7 of 20 · Previous: [8.6 Collection reports (collection for a day, abstract, 7-days, unpaid parties, day-wise pending, periodic loans)](06-collection-reports-collection-day-abstract-7.md) · Next: [8.8 Loan lists, loan & savings closures, party transfer and rearrangement](08-loan-lists-loan-savings-closures-party.md)

This module is where a field executive (or an admin) registers a borrower ("party") with KYC data and photos, and creates, edits or deletes a micro-credit (MC) loan for that party. Every screen works in one of two modes. **Offline** mode writes to the local SQLite tables `NewParties`, `NewMCLoans`, `NewItems` and `RelatedDocCollection`, which are uploaded later by `uploadcollection`. **Online** mode writes straight to the branch SQL Server through `dbo.InsertNewParties`, `dbo.InserttblTransactionsMaster` + `dbo.InserttblMCLoanswithWeekdayId`, and posts a double-entry voucher in `tblTransactionsDetails`. The loan form takes its figures from the selected **loan method** (`LoanMethods` row), using either an interest-added or an interest-deducted formula and either a fixed or a proportional document-charge rule. It applies per-party limits (`MaximumAmountAllowed`, `NoOfLoansAllowed`), role rights for menus `nav_loans` and `nav_loanparties`, and special-access rules 1, 2, 8, 9, 11, 12, 13, 14 and 20 (rule 18 is checked by the caller `loanindexloanswiselist_act`). The module also holds the generic party/partner ledger master used by the ST/OF loan books (`partyandpartnerdetails`, screen types 21/22), the loan-document photo list and upload screens, a shared camera/gallery picker with uCrop, and a zoomable full-screen image viewer.

## New / edit MC loan — `newloancreation`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), implements `invtransdetails_adapter.ContactsAdapterListener`. Manifest label `@string/title_activity_newloandetails` ("newloandetails" / ta "புதிய கடன் விவரங்கள்"); the title is overwritten at runtime ("New Loan-OFFLine", "New Loan-online", "Edit Loan-OFFLine", "Edit Loan-Online"). Edge-to-edge. |
| **Layout(s)** | `activity_newloandetails.xml` (+ `invtransdetails_row.xml` rows through `invtransdetails_adapter`) |
| **Opened from** | The extra `CallType` sets the mode. **9901** (`partyscreen_newpartyrequestfrom_partyselectionscreen_offlinepartycreation`, offline new loan) and **9017** (`newloanrequestcode_online`, online new loan) come from three places: (a) `partylistwithprevloansfornewloanselection` when the user taps a new offline party (LedgerId ≤ 200) that has no loan yet; (b) `loanindexloanswiselist_act` when the user picks a loan-index slot of an existing party; (c) `partydetails_act` right after a party is created (`loanindex` 1, plain `startActivity` with flag `FLAG_ACTIVITY_FORWARD_RESULT` 0x2000000). All three pass `passedparty` (partydetails_data) and `loanindex`; (a) and (b) use request code 8001 (JADX prints it as `constantvalues.interestcalcualationdetails_gettargetledger`). **9008** (`requestcodes.editloanrequestcodefromOFFLINEdata`, offline edit) comes from `newloanslist_act` (nav_newloans, needs the View right) and from `tabbedcollectionlist` rows whose Remarks marker is "00000" (new-loan rows); extras `passedparty`, `passedloandata` (mcloanobj); request code 9008. **301** (`EDITLOANMETHOD_DirectREQUEST`, online edit) comes from `loanslist_act`: directly when `calledfrom`=1022 (nav_loansfortoday), otherwise through the option "See Loan Details"; extras `passednewloan` and `passedparty`; request code 301. `daywisependingreport_act` and `newpartiesnewloanslist_act` build a `CallType`=2 intent but never start it (dead code). |
| **Opens** | `spinnersearch_list` for Loan Method (rc 21), Collection Day (rc 22), Due Date (rc 23), "Choose Ledger" (rc 11002, extra `calledfrom`=11002) and "Loan Index" (rc 12003). `dueschdule` (Show Schedule, extra `dueslist_dataList`). `invtrans_rowdetails` (product line; extras `actiontype`=1003, `ttid`, `receiveddata`, `Currentlotno`=0, `hideprice`=false; rc 1003). `documentsphotossearchlist` (extra `docslist`, rc 1100). `DatePickerDialog`. |
| **Data** | SQLite read: `LoanMethods`, `FinanceDetails.DefaultLoanMethodId`, `CashBankLedgers` (name and GroupId; the picker lists rows of the current FinId, only `Alloted=1` unless full access), `LoansAllowed` + `Parties.NoOfLoansAllowed`, `NewItems`, `ItemMaster`, `RelatedDocmenttypes`/`RelatedDocCollection`, `SelectedBranchFinanceDetails`, `SMSTemplates` (it also creates `SMSTemplates` and `SMSGateWay` if they are missing). SQLite write: `NewMCLoans`, `NewItems`, `RelatedDocCollection`. Server (`selbranchconnString`): `dbo.InserttblTransactionsMaster`, `dbo.InserttblMCLoanswithWeekdayId`, `tblTransactionsMaster`, `tblMCLoans`, `tblTransactionsDetails`, `InvTransDetails`, `RelatedDocCollection`. Server (`branchconnString`): `RelatedDocmenttypes`/`RelatedDocCollection` (read). Prefs: writes `newmcloanid`. |

**Purpose.** The user enters a daily, weekly or monthly MC loan for a party. The user picks a loan method and types a loan amount; the screen derives interest, document charges, total collectable, number of instalments, instalment amount and loan-in-hand. The user chooses whether the money is paid from cash or bank and saves the loan locally (offline) or on the server (online). An existing loan can be edited or deleted from the same screen.

**UI.** All labels are shown as English / Tamil.
- Header: party name (`tvpartyname`, placeholder "Party"/"பார்ட்டி") and business/tag (`newloan_tvtagid`).
- Date row "Date : dd-MM-yyyy" ("தேதி"). The row is visible only when `globalvars.isFullAccessAllowed`. Tapping the date opens a date picker; the field is enabled for full access or special access 14.
- "Loan Method": read-only field, hint "Select Loan Method"; tap to pick a method.
- "Collection Day" ("வசூல் நாள்"): shown only for weekly methods (CollectionBreak 2) and editable only with special access 8. For monthly methods (CollectionBreak 4) the label becomes "Due Date"/"தவணை தேதி" and the values are 1–28. Daily methods show nothing.
- "Loan Index": shown only in modes 9017 and 301; tap to pick.
- "Products" block, shown only when `globalvars.curfin.isEnableinventory()`: header SN/Product/Qty/Price/Amount, product RecyclerView, FAB "add new item", "Product Value :" total, Advance, Balance and Int Rate.
- Amount fields: "Loan Amount" ("கடன் தொகை"), "Interest Amount" ("வட்டி"), "Document Charges" ("ஆவண கட்டணம்"), "Loan in Hand" ("கையில் தரும் தொகை", read-only), "Total Collectable" ("வசூலிக்கப்படவேண்டிய தொகை", read-only), "No Of Instalments" ("தவனைகள்"; plus a "Show Schedule" button for inventory finances only) and "Instalment Amount" ("தவனை தொகை"). Interest, document charges, instalments and instalment amount are editable only for full access or special access 14.
- Optional fields: "On Day Collection" (setting id 36, `enableondaycollection_27`; hidden in modes 9017 and 301); "On Day Savings Collection" (always hidden, unused); "Remarks" ("குறிப்பு", setting id 38); "Mobile No" (setting id 37).
- Payment: "Paid as/In" (the cash or bank ledger; tap to choose) and "Payment Reference" (shown only when the chosen ledger has GroupId 5, bank).
- Buttons: "Related Documents" (setting id 72, `EnableUploadLoanRelatedDocuments`), "Update" ("சேமி"), "DELETE" ("நீக்கு"); a progress bar.

**Menu / actions.** The screen has no options menu. Update → `SaveLoan()`. DELETE → the delete flow (step 14). Related Documents → `ASYNCRelatedDocslist`. Show Schedule → `dueschdule`. FAB → appends an empty product row and opens `invtrans_rowdetails`; tapping a product row edits it. Field taps → the pickers listed under **Opens**.

**How it works.**
1. **onCreate.** Opens SQLite and binds the views. Cleans up with `delete from RelatedDocCollection Where MCLoanId=0 and ServerGenMCLoanId=0`. Applies the soft-setting visibility rules above. Reads role rights from `globalvars.salesdetailsforandroid_datalist` for menu `nav_loans`; full access grants all rights. Update is visible if the role has the Update right or special access 13 (`isAllowtoaddNewLoans_13`). Delete is visible if the role has the Delete right; the mode rules below then override it.
2. **New loan (9901/9017).**
   - "No Of Instalments" starts with `NoOfIns` of `globalvars.DefaultLoanMethodId`.
   - A new `mcloanobj` is built:
     - From the party: `loanindex`, SubAreaNo, `PartyLedgerId`, and `Mobileno` = the party's MobileNo1 (the Mobile field itself stays empty).
     - From globals: FinId = `globalvars.finid`; source ledger = `globalvars.cashbookledgerid` (the executive's cash book), whose name is shown in "Paid as/In".
     - Loan date and weekday: loan date = `globalvars.collectiondate` (today if null); WeekDayId = `Calendar.DAY_OF_WEEK` of the loan date (1=Sun…7=Sat).
     - Loan method: `FinanceDetails.DefaultLoanMethodId`.
   - Mode 9017 also shows the Loan Index row, forces Update visible and hides On Day Collection. Delete is hidden in both new modes.
3. **Offline edit (9008).** Loads the passed `NewMCLoans` object into the form. Loan in Hand is shown as Collectable − Interest. For inventory finances it also restores Advance, Int Rate, Balance and Product Value, and loads product rows with `Select SNo, MCLoanId, Qty, ItemCode,PRate,TotalAmt from NewItems where MCLoanId=…`. Delete is visible only while `ServerGenMCLoanId`=0 (the loan has not been uploaded yet). Update is forced visible.
4. **Online edit (301).** Loads `passednewloan` and keeps it as `loan_olddata` for the SMS. Loan in Hand is shown as Collectable − Interest − DocCharges. Monthly methods show "Due Date" = MonthDayId. `ASYNCGetInvTransDetails` loads `Select TransId, SNo, ItemCode, abs(BilledQty) as BilledQty, Rate, Total from invtransdetails where Transid=<ServerGenTransId>`. Update and Delete follow the role rights only.
5. **Live calculation.** The text watchers are registered at the end of onCreate and are suppressed while `isdisp` or `calculationisgoingon` is set. Notation: LM = the selected `LoanMethods` row, A = loan amount, n = number of instalments.
   - **Loan amount changed.** If setting id 64 (`enableMaximumAmountLoanAllowed_22`) is on and A > the party's `MaximumAmountAllowed`, A is clamped to that maximum and the toast "Loan Amount Exceed" appears. n = the "No Of Instalments" field (or LM.NoOfIns). Then the values are recomputed:
     - Interest-added method (`IntAddition`>0): Interest = A×IntAddition/LM.LoanAmount; Collectable = A + Interest; Loan in Hand = A − DocCharges.
     - Interest-deducted method (`IntDeduction`>0, non-inventory finance): Interest = A×IntDeduction/LM.LoanAmount; Collectable = A×TotAmountCollectable/LM.LoanAmount; Loan in Hand = A − (Interest + DocCharges).
     - Inventory finance: Interest = (Balance + DocCharges)×IntRate×n/100; A = Balance + Interest; Collectable = A.
     - Document charges (recomputed only for new loans, never in 9008 or 301): 0 if A=0; `DocChargesMethod` 2 → the fixed LM.DocCharges; `DocChargesMethod` 1 → proportional, LM.DocCharges/LM.LoanAmount×A.
     - Instalment = Collectable ÷ n (integer division).
   - **Instalments changed.** Recomputes Collectable, document charges, Loan in Hand and Instalment.
   - **Interest edited.** Collectable = A + Interest for interest-added methods. For interest-deducted methods Collectable is recomputed proportionally, so a manual interest change does not alter Collectable there.
   - **Instalment amount edited.** n = Collectable ÷ Instalment.
   - **Document charges edited.** Recomputes Loan in Hand. For inventory finances, Loan in Hand = A and Collectable = A + DocCharges.
   - **Advance edited** (inventory): Balance = Product value − Advance. Editing Int Rate also triggers a recalculation.
6. **Picker results** (`onActivityResult`):
   - rc 21 (loan method): reloads LM and sets n = LM.NoOfIns. Weekly methods show the weekday row; monthly methods show the "Due Date" row with the text "1". Then recalculates.
   - rc 22: WeekDayId. rc 23: MonthDayId. rc 12003: Loan Index.
   - rc 11002: `Sourceledgerid`, plus Payment Reference visibility when GroupId is 5.
   - rc 1003: result 1003 replaces a product row, result 1004 removes it; the product total is recalculated (`calctotal`).
   - rc 1100: result 1 returns the document list. For loans not yet on the server, the local `RelatedDocCollection` rows of this MCLoanId are replaced with the returned list.
7. **Date picker.** Accepts any past date, today, or a date up to one day ahead. A later date shows the alert "Info.... " / "Future Date cannot be used". The chosen date also resets WeekDayId.
8. **SaveLoan validation**, in this order:
   - (a) Instalment and n must be non-zero, else "Invalid Input" / "No of Installments and Installment amount cannot be zero."
   - (b) |Instalment×n − Collectable| must be ≤ 1000, else "Invalid manual Calculation" / "Chec Installments and Installment amount ".
   - (c) Interest must exceed A/100 (integer division, i.e. more than 1%) unless setting id 51 (`iszerointerestallowed_26`) is on, else `@string/insuffinterest` ("Minimum Interest not found"; ta "வட்டி குறைவாக உள்ளது, லோன் பதிவாகவில்லை").
   - (d) A must be ≤ the party's `MaximumAmountAllowed`, else `@string/amountexceed` "Loan Amount Exceeds the Limit".
   - (e) In mode 9901 with the Mobile field enabled, the number must match `Patterns.PHONE` and `^[6-9]\d{9}$`, else "Invalid phone number".
9. **Prepare the loan object.** Sets PartyReferenced, ondaycollection, loanindex (0 becomes 1), MCLoanDate, Remarks, mobile and DocCharges (parsed from the fields). `ServerGenLedgerId` = party LedgerId and `ServerGenMCLoanId` = 0.
10. **Offline new (9901).**
    - Numbering: `globalvars.newmcloanid`+1 becomes the new MCLoanId and is saved to pref `newmcloanid`. The counter is reset to 0 by `downloaddata` and at registration, so offline loan ids restart at 1 after every download.
    - INSERT `NewMCLoans` with these columns:
      - Identity and party: MCLoanId, FinId, PartyLedgerId, SubAreaNo, ServerGenLedgerId, ServerGenMCLoanId=0, ServerGenTransId=0.
      - Amounts: MCLoanAmount, TotalMonths, DocCharges, InterestAmount, MCLoanAmountCollectable, InsAmount, MCLoanAmountEstimated, ondaycollection.
      - Loan terms: LoanMethodId, MCLoanDate, WeekdayId, MonthdayId, LoanIndex.
      - Payment and contact: Sourceledgerid, PartyReferenced, Remarks, mobileno.
      - Inventory: ProductValue, IntRate, AdvanceAmount.
    - Links pending documents: `update RelatedDocCollection set MCLoanId=<new> where MCLoanId=0`.
    - Shows the toast "New Loan Added" and returns `setResult(9901)` with `isdeleted`=false, `isnewloan`=true, `loanindex` and `loanamount`=Collectable, then calls `finish()`.
    - After `finish()` it inserts `NewItems` (SNo, MCLoanid, ItemCode, Qty, PRate, TotalAmt) for product rows with qty > 0.
11. **Offline edit (9008).** UPDATE `NewMCLoans` where MCLoanId = the passed id. Links documents that still have MCLoanId=0 to this loan. Returns the result (`isdeleted`=false, `receivedloandata`), finishes, and then upserts the `NewItems` rows.
12. **Online new (9017)**, `ASYNCUpdateLoaninSQLDB` on `selbranchconnString`:
    - (a) `{call dbo.InserttblTransactionsMaster(12)}`. Inputs: TransDate = loan date, TransType=21 (MicroLoans), Narration '', FinId, TransAmount = Collectable, FWTransId/FWVWUDTransId/GColId NULL, TransLedgerId = ServerGenMCLoanId (still 0), RouteId = FinId. Outputs: TransId and FWVWTransId.
    - (b) `{call dbo.InserttblMCLoanswithWeekdayId(32)}`, which returns MCLoanId. Its parameters:
      - Ids and links: FinId, PartyLedgerId, TransId, LoanMethodId, Sourceledgerid, loanindex. FWMCLoanId/FWUDMCLoanId (finance-wise and user-defined loan numbers) are NULL, as are AgentLedgerId, GuaranderLedgerID, RecPartnerLedgerID and RecPartyLedgerId.
      - Amounts: MCLoanAmount, InterestAmount, DocCharges, MCLoanAmountCollectable, InsAmount, and MCLoanAmountEstimated = Collectable (not the loan-in-hand figure).
      - Terms: MCLoanDate, TotalMonths, IntRate, LoanType 0, CollectionLineId 0, DCPercent 0, WeekDayId, MonthDayId.
      - Other: Remarks, mobileno, PartyReferenced, Productvalue, AdvanceAmount.
    - (c) Voucher: `delete tblTransactionsDetails where TransId=T; insert into tblTransactionsDetails select …` with these lines (the last column is the SNo of the paired line):

      | SNo | Ledger | Side | Amount | Dr/Cr |
      |---|---|---|---|---|
      | 1 | party ledger | Target | Collectable | Dr |
      | 2 | cash/bank | Source | Collectable | Cr |
      | 3 | cash/bank | Target | Interest | Dr |
      | 4 | ledger 108 (interest) | Source | Interest | Cr |
      | 5 (only if DocCharges>0) | cash/bank | Target | DocCharges | Dr |
      | 6 (only if DocCharges>0) | ledger 104 (document charges) | Source | DocCharges | Cr |

      Net cash paid out = Collectable − Interest − DocCharges.
    - (d) Products: INSERT `InvTransDetails` (SNo, ItemCode, BilledQty = −qty, TransId, Rate, Total).
    - (e) Documents: for each local `RelatedDocCollection` row with ServerGenMCLoanId=0 and MCLoanId=0, runs `Update RelatedDocCollection set MCLoanId=? where DCUId=?` on the server and stores the server loan id locally.
    - (f) Shows the toast "Loan is saved" and returns `setResult(9017)` with `receivedloandata` and `nooftransfound`.
13. **Online edit (301).** Runs `update tblTransactionsMaster set TransAmount=?,TransDate=? where TransId=?` and `update tblMCLoans set MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,Remarks,WeekDayId,MonthDayId,MCLoanDate,Sourceledgerid,PartyReferenced,Loanindex where MCLoanId=?`. Rebuilds the voucher lines as in step 12c, deletes and re-inserts `InvTransDetails`, then runs `Update_sendsms` (template 8).
14. **Delete.**
    - Online loan (301): the mode switches to 303 and a dialog shows `@string/removeloandialog` ("Do not delete loan in between download and upload"; ta "கடன் அழிப்பது, வசூல் அப்லோட் செய்வதில் சிக்கலை ஏற்படுத்தும்…") with buttons "Do not delete" and "Delete Loan". On confirm it runs `delete from tblMCLoans where MCloanId=?`; if a row was deleted it also deletes the `tblTransactionsDetails` and `tblTransactionsMaster` rows of ServerGenTransId. It then runs `delete_sendsms` (template 9) and returns result 303 with `nooftransfound` = rows deleted.
    - Offline loan (9008): runs `delete from NewMCLoans where MCLoanId=…` immediately, with no confirmation, and returns `isdeleted`=true.
15. **SMS** (`Update_sendsms` / `delete_sendsms`).
    - Recipients: `globalvars.AdminMobileNos`, with `globalvars.smsidentifier` as the prefix. For the edit SMS on a master DB, both come from `SelectedBranchFinanceDetails` for the loan's FinId instead.
    - Text: the `SMSDesc` of local `SMSTemplates` STId 8 (edit) or 9 (delete), with placeholders filled:
      - `{collarea}` = smsidentifier-AreaName.
      - `{newdata}` and `{olddata}` = date/amount/interest/instalment/on-day-collection joined by a vertical bar.
      - `{party}`, `{loandate}`, and `{staffname}` = `globalvars.APPUserName`.
      - The suffix "\n@ dd-MM-yy hh:mm aa" is appended.
    - No template means no SMS.
    - Sending: `softsettingsvalue.smsmethod` (setting id 50) = 1 sends through the SIM with `SmsSender.sendMultipartSMS`. Value 2 sends an HTTP POST to `<SMSAPI>?type=unicode&smstext=…&smsto=…&senderid=&routeid=8&api_key=`.
    - The toast "SMS Sent" is always shown.
16. **Related Documents** (`ASYNCRelatedDocslist`).
    - Loan already on the server: queries `branchconnString` with `select MCLoanId,DCUId,a.DUId, convert(bit, case when b.DCUId is null then 0 else 1 end) as 'Collected',DocDesc,Remarks,ImagePath from RelatedDocmenttypes a Left Join RelatedDocCollection b on (a.DUId=b.DUId and MCLoanId=<id>) and a.isImageAllowed=1 order by showid`.
    - Otherwise: runs the same join on SQLite with `b.ServerGenMCLoanId = 0`.
    - Opens `documentsphotossearchlist` with the resulting list.
17. **Show Schedule.** Calls `defaulttables.prepareandreturnduelist(date, Balance, n, IntRate, 5, date, CollectionBreak, true, Instalment, Instalment, 0)`. Because the manual flag is true, principal per due = Instalment and interest = 0. Only weekly (first due = loan date, then +1 week each) and monthly (first due = loan date +1 month) produce rows; daily and bi-weekly methods give an empty schedule. There is no separate first-due-date field.

**Functions.**
- `onCreate()`: mode set-up, visibility and rights rules, field watchers, click handlers.
- `ChangeLoanValuesbasedonloanamount()`, `…basedonnoofdues()`, `…basedoninterestamount()`, `…basedoninstallmentamount()`, `…basedonidoccharges()`: the recalculation rules in step 5.
- `onActivityResult()`: picker, product-row and document-list results.
- `SaveLoan()`: validation, then the offline insert/update or the online task.
- `deleteloan()`: offline delete from `NewMCLoans` and return `isdeleted`.
- `onContactSelected()`: opens the product row editor.
- `calctotal()`: product value total and "Product Value :" label.
- `whiteNotificationBar()`: white status bar with light icons.
- `ASYNCRelatedDocslist`: builds the document list from the server or local DB.
- `ASYNCUpdateLoaninSQLDB`: online insert (9017), update (301) or delete (303) plus the voucher.
- `ASYNCGetInvTransDetails`: loads the products of an online loan.
- `Update_sendsms` / `delete_sendsms`: SMS alerts 8 and 9.

**Rules & edge cases.**
- **Limits.** The party limit `MaximumAmountAllowed` defaults to setting id 62 `MaximumLoanAmountAllowed_23`. The amount field clamps to it only when setting 64 is on, but SaveLoan rejects any amount over the limit regardless of that setting. A party whose limit is 0 can therefore never get a loan.
- **Number of loans per party** is not checked here. Callers enforce it: an offline new party may have one loan (`partylistwithprevloansfornewloanselection`); existing parties use loan-index slots in `loanindexloanswiselist_act` (special access 18, setting `enablemoniteringonloanclosoure_5`); the Loan Index picker offers only `LoansAllowed` rows ≤ `Parties.NoOfLoansAllowed`.
- **Special access.** 13 shows Update; 14 enables interest, charges, instalments and date; 8 allows changing the weekday. The date row itself is visible only to full-access users, so special access 14 alone cannot change the loan date.
- **Bugs.**
  - After a cancelled online delete the mode stays 303. Update then does nothing, and a second DELETE runs the offline `deleteloan()`: it deletes the local `NewMCLoans` row with that id and reports `isdeleted`=true while the server loan stays.
  - SQL errors in `ASYNCUpdateLoaninSQLDB` are only logged. The screen still shows "Loan is saved" or "SMS Sent" and returns success. The validation inside `onPreExecute` is unreachable (it only disables the button).
  - The HTTP SMS path cannot work: `SMSAPI` and `api_key` stay "" because `SMSGateWay` is never read. Template 17 (`smstemplateids.smsonnewloan`) is defined but never used, so no SMS is sent for a new loan.
  - Offline edit upserts `NewItems` by `ItemCode=` only (not MCLoanId), so it can overwrite product lines of other loans. `NewItems` writes also run after `finish()`.
  - The local document query is not filtered by MCLoanId (only `ServerGenMCLoanId=0`), so documents of different offline loans can mix.
  - Monthly methods: the default "1" due day is only text, so MonthDayId stays 0 unless picked. A finance whose default method is monthly does not show the Due Date row on a new loan. The Due Date picker title is passed as an int resource id instead of a string.
  - The party mobile is put into the loan object but not into the field, so the saved value is whatever the user typed.
  - A date one day in the future is accepted. `ttid` sent to `invtrans_rowdetails` is always 0. Empty numeric fields raise NumberFormatException, which is shown as a raw toast.

## Party (borrower) details & KYC — `partydetails_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest label "Party Details"; edge-to-edge; Google `FusedLocationProviderClient`. JADX marked `onActivityResult` and the three `uploadFile()` methods as decompiled incorrectly; they were checked against smali and the logic below matches. |
| **Layout(s)** | `activity_newpartycreation.xml` (includes `layout_toolbar_profile.xml`: circular photo `memberimageid` + edit icon `img_plus`) |
| **Opened from** | The extra `CallType` sets the mode. **9901** "Create Party (Offline)" and **9017** "Create Party (Online)": FAB (+) of `partylistwithprevloansfornewloanselection`, with `AreaName`, `SubAreaNo`, and `receivedparty` pre-set to MaximumAmountAllowed = setting 62 and NoOfLoansAllowed = setting 63; rc 1001. **9902** "Edit Party (Offline)": `newpartieslist_act` (nav_newparties), with `passedparty`, `SubAreaNo`, `AreaName`; rc 100. **9903** "Edit Party (Online-Admin)": `partydetails_searchlist`; rc 9903. **9904** "Edit Party (Online)": `directcollectionentry` and `tabbedindividualcollectionentry` (collection-entry screens), with `ledgerid` and `SubAreaNo`; rc 2. |
| **Opens** | `ImagePickerActivity` (rc 100) through the "Set image" dialog; `fullimageview` (tap an image); `spinnersearch_list` "No of Loans Allowed" (rc 12002); `newloancreation` after a create; the Google location-settings resolution dialog (rc 1002). |
| **Data** | SQLite: reads `Parties`⋈`SubAreaDetails` (9904), `LoansAllowed`, `NewMCLoans`. Writes `NewParties` (insert/update/delete), `Parties` (insert/update/delete), `MobileAppCollData`. Server `selbranchconnString`: `dbo.InsertNewParties`, `update tblledgers …`, `delete from tblLedgers where LedgerId=?`. Server `ImagesuploadconnString`: `dbo.InsertImageUploads`. HTTP: `party_image_file_upload_url` (setting 25) and `idproof_file_upload_url` (24); images are viewed from `partiesimagepath` (18) and `idproofimagepath` (19). Prefs: `newpartyledgerid`. Globals: `imgtobepassed`, `idproofimgtobepassed`, `idproofimgiitobepassed`, `isPartyImagefromDB`, `isIdproofImagefromDB`, `isIdproofImageIIfromDB`, `tabbedcollectionlistdataList`. |

**Purpose.** Create or maintain a borrower record: number, name, business, spouse/father name, family name, address, two mobiles, active flag, GPS position, a profile photo and up to two ID-proof images. An admin can also change the borrower's loan limits. Creating a party leads straight into the new-loan form.

**UI.**
- Area name at the top. Profile photo and edit icon, shown if setting 21 (`enablepartyimageloading`).
- "No of Loans Allowed" (tap to pick) and "Maximum Amount Allowed": hidden; shown only in mode 9903 with special access 2 and 1 respectively.
- "No" (`etPartyNoid`, the party number CWLedgerId): shown if setting 40 (`enableuserdefinedcustomernumberinnewcustomercreationscreen_30`), and always in modes 9017 and 9904.
- "Party Name" ("பார்ட்டி பெயர்"), "Business" ("தொழில்", the Tag), "Spouse Name" (ta "மனைவி/கணவர்/தந்தை பெயர்", i.e. wife/husband/father), "Family Name", "House No/Street" (AddLine1), "Mobile No 1", "Mobile No 2".
- "ACTIVE" checkbox: visible only in mode 9903.
- Location button (`ic_action_location`) with lat/lng text.
- "ID Proof-1" button and image (setting 22). "ID Proof-2" button and image (settings 22 and 48).
- "Update" ("சேமி") and "DELETE" ("நீக்கு"); a progress bar with a percentage label.

**Menu / actions.** No options menu.
- Edit icon / ID Proof-1 / ID Proof-2 → the "Set image" dialog (CAMERA / GALLERY) → `ImagePickerActivity` with a 1:1 crop; the camera path is limited to 1000×1000.
- Tapping the photo or an ID image opens `fullimageview` with the **server** image (`partiesimagepath`/`idproofimagepath` + file name).
- Location button, Update and DELETE are described below.

**How it works.**
1. **onCreate.** Clears the image globals and reads `CallType`, `SubAreaNo` and `AreaName`. Role rights come from menu `nav_loanparties`.
   - Update is visible if the role has the Update right or special access 11 (`isAllowtochangeExisitingPartiesDetails_11`). It is always visible in the create modes 9901 and 9017.
   - Delete follows the role's Delete right in mode 9903, is always shown in 9902, and is hidden in 9901, 9017 and 9904.
   - Mode 9017 forces ACTIVE on and hides it.
2. **Per-mode load.**
   - 9901: uses the passed party and loads no images.
   - 9017: uses a new empty party.
   - 9904: reads the party from local `Parties a, SubAreaDetails b … where LedgerId=<ledgerid>`; MaximumAmountAllowed is not selected.
   - 9903 and 9902: use the passed `passedparty`.
   - In all edit modes, `showreceivedpartydetails()` fills the fields and loads the existing images with Glide.
3. **Validation on Update.**
   - Party Name is always required; the error is "பார்ட்டி பெயர் நிரப்பப்படவில்லை".
   - If setting 43 (`makeallvaluesnecessaryinnewpartycreationscreen_31`) is on, these are also required: Tag ("Tag Cannot be Empty"), Mobile 1 ("Mobile No Cannot be Empty"), Spouse ("Spouse Name No Cannot be Empty") and location ("Location No Cannot be Empty").
   - With setting 43 on, missing images show the toasts "Image cannot be Empty" (photo), "IdProof Image cannot be Empty" or "IdProof II Image cannot be Empty" (each only if its setting is on), followed by the dialog "Incomplete Input..." / "Kinldy Fill necessary values".
   - There is no uniqueness check on number, name or mobile and no mobile-format check.
4. **Limits.**
   - MaximumAmountAllowed = setting 62, unless the Maximum Amount field holds a value > 0.
   - NoOfLoansAllowed = setting 63 (`MaximumnoofloansallowedwhilenewpartycreationORnoofloansallowedforallparties_25`).
   - If setting 65 (`IsRestrictinEnabledonNoofLoansAllowed_IndividualRestriction_24`) is on and the mode is not 9901: mode 9017 gets 1 and ACTIVE is forced on; other modes take the value of the No of Loans field.
   - CWLedgerId = the No field, or 0 if empty.
5. **`updatepartiesinremoteandlocal`** (touch is blocked while it runs):
   - (a) Image ids, only if setting 21 is on: for each image that was changed and not loaded from the server, `{call dbo.InsertImageUploads(?,?)}` (Dummy='Dummy') returns IUId. File names become `member_<IUId>.jpg`, `idproof<IUId>.jpg` and `idproofii<IUId>.jpg`. Errors are ignored.
   - (b) 9903: server `update tblledgers set CWLedgerId,LedgerName,Tag,SubAreaNo,UDLedgerId,MobileNo1,MobileNo2,AddLine1,SpouseName,FamilyName,ImagefileName,IdProoffileName,IdProofFileNameII,lat,lng,NoOfLoansAllowed,MaximumAmountAllowed,IsActive where ledgerid=?`.
   - (c) 9902: UPDATE `NewParties`; the maximum is reset to setting 62, NoOfLoansAllowed becomes 1 if setting 65 is on (else setting 63), and ServerGenLedgerId = 0.
   - (d) 9901: `globalvars.newpartyledgerid`+1 becomes the LedgerId and is saved to pref `newpartyledgerid` (reset to 0 at each download). INSERT `NewParties` with AreaNo = finid, SubAreaNo, the maximum from setting 62, NoOfLoansAllowed = 1 and ServerGenLedgerId = 0.
   - (e) 9017: `{call dbo.InsertNewParties(19)}`. Inputs: CWLedgerId, LedgerName, Tag, AreaNo = finid, SubAreaNo, UDLedgerId NULL, MobileNo1/2, AddLine1, SpouseName, FamilyName, ImagefileName, IdProoffileName, lat, lng, IdProofFileNameII, NoOfLoansAllowed, MaximumAmountAllowed. Output: LedgerId. Then INSERT local `Parties` with the server LedgerId.
   - (f) 9904: the same server update as (b) without UDLedgerId; failure is ignored. Then it updates local `Parties`, `MobileAppCollData` (CWLedgerId, PartyName, UDLedgerId) and the in-memory collection list (number, tag, image names, NoOfLoansAllowed).
6. **Uploads** (`onPostExecute`).
   - Chain: party photo → ID proof 1 → ID proof 2, then `returntobackornextscreen()`.
   - Each upload copies the cropped file to its generated name and sends an `HttpURLConnection` POST, multipart/form-data with boundary "*****" and part "image". The ID-proof uploads also send `website`=www.venussolutions.net and a hard-coded vendor e-mail address.
   - Progress shows as "Party Image Uploading..N%", "Id Proof Uploading N%" and "Id Proof II Uploading N%".
   - A failure shows "Error Info", "Id Proof I Image Upload error.." or "Id Proof II Image Uploade error..", and OK closes the screen.
7. **`returntobackornextscreen()`.**
   - 9901/9017: opens `newloancreation` (`passedparty`, `CallType`, `loanindex` 1, FLAG_ACTIVITY_FORWARD_RESULT) and finishes.
   - 9904: result 2 with `passedparty`.
   - 9903: result 5.
   - Otherwise: the toast "Party Information Updated" and finish.
8. **Delete.** There is no confirmation dialog.
   - 9903 → `deletepartiesinremoteandlocal`: server `delete from tblLedgers where LedgerId=?`. If a row was deleted, the local `Parties` row is deleted too. Returns result 6 with `rowsdeleted`.
   - Other modes → `deleteparty()`: if `NewMCLoans` has a loan for this party, the dialog "Error Info" / "Loan Exisits\n Party cannot be deleted" appears. Otherwise it runs `delete from NewParties where LedgerId=…`, shows the toast "Deleted" and goes back.
9. **Location.**
   - If ACCESS_FINE_LOCATION is missing, the app requests it (rc 1001).
   - Otherwise it checks location settings (high accuracy, 10 s interval, 5 s fastest; JADX shows the 5000 ms as `DeviceOrientationRequest.OUTPUT_PERIOD_FAST`) and calls `getLastLocation()`, which fills the lat and lng labels.
   - Failure messages: "Unable to get location. Try again later.", "Failed to retrieve location: …" and "Unable to resolve location settings.".

**Functions.**
- `onCreate()`: modes, rights and handlers.
- `showreceivedpartydetails()`: fills the form and loads images.
- `loadProfile()` / `loadidproof()` / `loadidproofii()`: Glide load plus the global image Uri. `loadProfileDefault()` marks the default photo.
- `showImagePickerOptions()`, `launchCameraIntent()`, `launchGalleryIntent()`: start the picker.
- `onActivityResult()`: rc 100 image result (switch on `imagecalledfrom`), rc 12002 No of Loans, rc 5000 finish.
- `deleteparty()`: offline delete.
- `returntobackornextscreen()`: navigation after save.
- `checkLocationSettingsAndFetchLocation()`, `fetchLocationnew()`, `getLastLocation()`, `requestLocationPermission()`, `onRequestPermissionsResult()`: GPS capture.
- AsyncTasks: `updatepartiesinremoteandlocal`, `deletepartiesinremoteandlocal`, `UploadpartyimageFileToServer`, `UploadidproofimageFileToServer`, `UploadidproofiiimageFileToServer` (each with `uploadFile()`).

**Rules & edge cases.**
- ID-proof file names are generated only when setting 21 is on. With only setting 22 on, the ID-proof upload fails.
- The upload chain after the photo does not check the "changed" flags. If an existing ID-proof image was loaded from the server URL and not changed, the app tries to upload that URL path as a file and shows the "Id Proof I Image Upload error.." dialog. The data is already saved by then.
- A failed upload leaves the new file name in the DB, pointing at an image that does not exist.
- Mode 9904 does not load MaximumAmountAllowed, so saving from a collection screen resets that party's limit to setting 62 on the server and locally.
- Mode 9017 inserts the local `Parties` row without IsActive, so the party selection list (`isactive=1`) does not show the party until the next download.
- In mode 9017 the default avatar counts as a photo, so the required-photo check is bypassed.
- "Offline" creation still needs the network for image ids when photos are enabled; failures leave empty file names.
- The mode-9902 branch in the delete handler is unreachable and contains malformed SQL (`…NewMCLoans a  PartyLedgerId=`).
- Permission result 1001 and settings result 1002 are not handled; only rc 1 is, from the unused `requestLocationPermission()`. The user has to tap the location button again.
- Picking ID-proof II resets `isIdproofImagefromDB` instead of `…IIfromDB` (confirmed in smali; harmless).
- The `ImageUploadstatus` text is built but never shown. The ID-proof II messages say "Idproof I".

## Party / partner ledger master — `partyandpartnerdetails`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest label "Party Details"; title "Party Details" or "Partner Details". |
| **Layout(s)** | `activity_partyandpartnerdetailsnew.xml` (includes `layout_toolbar_profile.xml`, hidden) |
| **Opened from** | `parties_list` (screen types 21 and 22, used by the ST-loan and OF-loan books). Its FAB (+), shown when the role has the Add right, opens **create** with `actiontype` 9003 (`parties_newpartyrequest`), the current `screentype` and an empty `ledgerdata`; rc 9003. A row tap opens **edit** with `actiontype` 9004 (`parties_updatepartyrequest`) when `parties_list` has `requestcode` 9001 (screen type 21), or `requestcode` 3001 (nav_partnerdetails, screen type 22, needs the View right; extra `menuname`). |
| **Opens** | `spinnersearch_list` "Guardian Type" (rc 9010); `parties_list` "Select Party" (screen type 21, `requestcode` 7032 = `masterpartyselectionrequest`, `finid` 1) to choose the master party. |
| **Data** | Create: `ImagesuploadconnString` (the branch DB in single-branch installs, the master DB in multi-branch ones) runs `dbo.InsertPartiesfromAPP` (26 params); `dbo.InsertImageUploads` is never reached. Edit/delete: `selbranchconnString` runs `update tblLedgers …` / `delete from tblLedgers where ledgerid=?`. SQLite `tblLedgers`: insert, update, delete. `qatledgers` is read for the master-party name (`defaulttables.fetchledgername`). |

**Purpose.** Maintain the generic ledger record for a "party" (screen type 21) or a "partner" (screen type 22): name, tag, guardian (father or husband), address, PIN, mobiles and an optional master party. These records are used by the ST/OF loan and interest screens, not by the MC collection flow.

**UI.** Fields:
- "Name", "Tag".
- "Guardian Type": tap to pick த.பெ (father's name, value 1) or க.பெ (husband's name, value 2); the text itself is stored.
- "Guardian Name", "Address Line 1", "Address Line 2", "City/Area", "Pincode", "Mobile No 1", "Mobile No 2".
- "Master Party" (parties only): tap to pick; the remove icon clears it.

The "additional details" block is always hidden: Landmark, Age, Marital Status spinner, Profession, Monthly Income, PAN, Aadhaar No, Guardian Contact No 1/2, Guardian Profession and Guardian Monthly Income. The photo header and the task label ("Create New Party", "Edit Party Details", "Create New Partner", "Edit Partner Details") are hidden too. Update and Delete buttons are at the bottom.

**Menu / actions.** Update → save; Delete → the Tamil confirmation dialog "அழித்தப் பின் திரும்ப பெற இயலாது என்பதை கவனத்தில் கொள்ளவும்..." ("cannot be recovered after deletion") with buttons "அழிக்கலாம்" (delete) and "அழிக்க வேண்டாம்" (don't).

**How it works.**
1. **onCreate.** Reads the extras and the rights for `menuname` (full access grants all rights). Create shows Update. Edit fills the fields and shows Update/Delete according to the Update/Delete rights.
2. **Save.** An empty Name shows "NAME CANNOT BE EMPTY..." with the title "தங்கள் கவனத்திற்கு.... " ("for your attention"). The visible fields plus PAN and Aadhaar are copied into the object and `updatepartiesinremoteandlocal` runs.
3. **Create (9003).** `{call dbo.InsertPartiesfromAPP}` with Ledgername, tag, groupid (the object's GroupId, 0), GType, GuardianName, AddLine1/2, City, Pincode, MobileNo1/2, MasterPartyLedgerId (NULL when 0), PanNo, AadhaarNo, Imagefilename, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1/2, GuardianProfession, LandMark and ScreenType; it returns the ledgerid. The app then inserts into SQLite `tblLedgers` (GroupId 1, Title, Tag, GType, guardian, address, PinCode, mobiles, EMail, MasterPartyLedgerId, ScreenType, AadhaarNo).
4. **Edit (9004).** Server `update tblLedgers set Ledgername,tag,groupid=1,GType,GuardianName,AddLine1,AddLine2,City,Pincode,MobileNo1,MobileNo2,MasterPartyLedgerId,PanNo,AadhaarNo,Imagefilename,MartialStatus,Qualification,Profession,MonthlyIncome,GuardianMonthlyIncome,GuardianMobileNo1,GuardianMobileNo2,GuardianProfession,LandMark where ledgerid=?`, then a local SQLite update of 15 columns.
5. **Delete (9005).** Server delete; if a row was deleted, the local row is deleted too. If nothing was deleted, the alert "தொடர்பான பதிவுகள் இருப்பதால் நீக்க இயலாது..." ("cannot delete, related records exist") appears.
6. **Result.** `setResult(actiontype)` with the extra `receivedparty`, then finish.

**Functions.**
- `onCreate()`: set-up and field fill.
- `onActivityResult()`: rc 9010 guardian type, rc 7032 master party, rc 7033 contact pick.
- `updatepartiesinremoteandlocal`: create, update or delete.
- `showSettingsDialog()`, `openSettings()`, `showAlert()`: helpers that are never used.

**Rules & edge cases.**
- Create runs on the image-upload connection, while edit and delete use the selected branch DB. In a multi-branch install, create therefore runs on the master DB.
- `etimagefilename` is never bound, so filling the edit form throws a NullPointerException (caught) after the PAN/Aadhaar fields; the hidden profession, income, guardian and landmark fields stay empty.
- Age and the Marital Status spinner have no ids, so they are never captured.
- A handler for rc 7033 (`partyselectionfromphonecontactsrequest`: name ← `displaymember`, Mobile 1 ← `seconddisplaymember` without "+91") exists, but nothing in the app launches a contact picker, so the feature is dead.
- No image picker is wired in this screen.
- Server errors are swallowed, so the screen closes as if it had succeeded.

## Party picker for a new loan (per collection area) — `partylistwithprevloansfornewloanselection`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` (own Toolbar), implements `customersearchadapter.ContactsAdapterListener`. |
| **Layout(s)** | `activity_customersearch.xml` (+ `partysearchrowfornewloan.xml` rows); menu `menucustomersearchlist` |
| **Opened from** | Extras `AreaName`, `SubAreaNo` and `calledfrom` (9901 offline / 9017 online). Missing extras crash the screen with AssertionError. Callers: (a) `collectionareassearchlist` (drawer nav_collectionareas): 9901 when `vss_executivehandlingmethod`=1 and `calledfromloanmethod`=2, else 9017; rc 24. (b) Area-name tap in `tabbedcollectionlist` / `tabbedcollectionlistmviewpager` (offline collection), 9901, rc 1. (c) Sub-area tap in `editing_collectionswithfulloptions_act` (online collection entry, `calledfrom` 9907) and `editing_collections_act`, 9017. |
| **Opens** | `partydetails_act` (FAB; rc 1001); `loanindexloanswiselist_act` (existing party); `newloancreation` (offline new party; rc 8001); `prevloanslist_act`; `partyrearrange` and `maps_partydisplay` (rc 1). |
| **Data** | SQLite only: `Parties`, `NewParties` (both created if missing), `MobileAppCollData`, `NewMCLoans`. |

**Purpose.** Shows the parties of one collection area so the executive can pick a borrower for a new loan, or add a new borrower.

**UI.** Toolbar title = area name, subtitle "Parties". Each row shows the party No, name, Business (tag) and mobiles 1 and 2. The name is blue if the party has a live loan in the downloaded collection data (`MobileAppCollData`), otherwise black. The FAB (+) is visible only with special access 12 (`isAllowtoaddNewParties_12`) or full access. The SwipeRefreshLayout and progress bar are not wired.

**Menu / actions.** Search (name, tag or exact number). "Rearrange Parties" (visible only with special access 20) → `partyrearrange` (`PLID`=SubAreaNo, `areaname` ""). "Show Parties in MAP" → `maps_partydisplay`. Row tap → see below. "Prev Loans" button → `prevloanslist_act` (`partyledgerid`, `partyname`) with special access 9, else "ACCES DENIED"; the button's container is `gone` in the layout, so it is never shown.

**How it works.**
1. **`fetchContacts()`, query 1.** `select distinct(LedgerId) … from (select a.*, ifnull(b.PartyLedgerId,0) as PartyLedgerId … from (select … from Parties where isactive=1 and SubAreaNo=<n>) a left Join MobileAppCollData b on a.LedgerId=b.PartyLedgerId) x`. A PartyLedgerId > 0 marks the party as live.
2. **`fetchContacts()`, query 2.** The same join over `NewParties` of the area, with UDLedgerId shown as 1000. The results are appended after query 1.
3. **FAB.** Builds a party with MaximumAmountAllowed = setting 62 and NoOfLoansAllowed = setting 63, opens `partydetails_act` (`CallType`=`calledfrom`) and immediately finishes, so the result is never received.
4. **Row tap.**
   - LedgerId > 200 is treated as a downloaded/server party: sets ServerGenLedgerId = LedgerId and opens `loanindexloanswiselist_act` (`curpartyobj`, `CallType`). That screen applies the loan-slot and special access 18 rules.
   - Otherwise (an offline new party): if `Select count(MCLoanId) … from NewMCLoans where PartyLedgerId=…` is 0, opens `newloancreation` (`passedparty`, `loanindex` 1); else shows "New Loans cannot be added..".
5. **`onActivityResult`.** rc 1 with data appends a blank party holding only `ledgerid` (quirk). rc 1001 reloads the list (never fires).

**Functions.** `onCreate()`, `fetchContacts()`, `onCreateOptionsMenu()`/`onOptionsItemSelected()`, `onContactSelected()`, `onprevloansbuttonclicked()`, `onActivityResult()`, back handler (closes the search first), `whiteNotificationBar()`.

**Rules & edge cases.**
- The id-200 threshold assumes offline party ids (1, 2, …; reset at each download) never reach 200 and server ids are always above 200.
- Only one offline loan is allowed per new offline party.
- Online-created parties without a local IsActive value are not listed.

## Loan parties admin list — `partydetails_searchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`, implements `partydetails_adapter.ContactsAdapterListener`. |
| **Layout(s)** | `activity_employeesearch.xml` (includes `customersearchlist.xml`) + `partydetailssingle_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | `selectbranchandfinance` with requestcode 113 (drawer admin nav_loanparties), extras `passedfinanceid`, `LineName`, `menuname`. |
| **Opens** | `partydetails_act` with `CallType` 9903 (`partyscreen_onlineupdate`) and extras `passedparty`, `SubAreaNo`, `AreaName`; rc 9903. |
| **Data** | Server `branchlevelReport_branchconnString`: `SubAreaDetails`, `tblLedgers`. SQLite: `BranchSubAreaDetails` (recreated). |

**Purpose.** Admin list of all MC-loan parties of a line (finance), used to open a party for online admin edit or delete.

**UI.** Title = line name. Each row: circular photo (Glide from `partiesimagepath`; fallback `ic_action_aboutus`; hidden unless setting 21), party No, name and sub-area. The FAB is hidden. A progress overlay shows while loading.

**Menu / actions.** Search filters by exact number, name or sub-area. Home/back returns, closing the search first. Row tap opens the editor if the role has the View right for nav_loanparties.

**How it works.**
1. `ASYNCParties` deletes and refills local `BranchSubAreaDetails` from `select SubAreaNo,SubAreaName,isVisible,ShowId,FinId from SubAreaDetails where finid=<globalvars.finid> and IsVisible='True' order by ShowId`. The inserts are built by string concatenation.
2. It loads the parties with `select LedgerId,CWLedgerId,SubAreaName,LedgerName,Tag,AddLine1,AddLine2,MobileNo1,MobileNo2,Imagefilename,a.SubAreaNo,SpouseName,FamilyName,IdProoffileName,IdProofFileNameII,lat,lng,NoOfLoansAllowed,MaximumAmountAllowed,IsActive from tblLedgers a,SubAreaDetails b where a.SubAreaNo=b.SubAreaNo and Screentype=3 and b.FinId=<passedfinanceid> order by CWLedgerId`. Screentype 3 marks MC-loan parties.
3. **Results.**
   - rc 9903 / result 5: replaces the updated row.
   - Result 6: if `rowsdeleted` > 0, removes the row and shows "பார்ட்டி விவரம் நீக்கப்பட்டது." ("party deleted"); else "பதிவுகள் இருப்பதால்\nஇந்த பார்ட்டியை நீக்க இயலாது" ("cannot delete, records exist").
   - Result 10 (`nooftransfound`) is handled but never produced.

**Functions.** `onCreate()`, `ASYNCParties`, `onActivityResult()`, `onCreateOptionsMenu()`/`onOptionsItemSelected()`, `onContactSelected()`, `whiteNotificationBar()`.

**Rules & edge cases.** The sub-area cache uses `globalvars.finid`, while the party query uses the passed finance id. Errors are silent and simply leave the list empty.

## Loan documents (photo list) — `documentsphotossearchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`, implements `documentsphotossearchlist_adapter.ContactsAdapterListener`. |
| **Layout(s)** | `activity_documentsearchlist.xml` + `documents_row.xml` (edit mode) / `documentsdisplay_row.xml` (display mode); menu `menuonlywithsearchoption` |
| **Opened from** | `newloancreation` "Related Documents" (extra `docslist`; edit mode; rc 1100). `tabbedindividualcollectionentry` (extras `receivedloan` and `calledfor`=100; display mode; plain `startActivity`). |
| **Opens** | `documentsupload` (extras `passeddocument`, `calltype`="edit"; rc 2). |
| **Data** | Server `branchconnString`: reads `RelatedDocmenttypes`⋈`RelatedDocCollection`; `update RelatedDocCollection set MCLoanId=?,DUID=?,Remarks=?,ImagePath=? where DCUId=?`. |

**Purpose.** Shows one row per required loan document type (for example agreement or ID copies) with its photo and remarks. The user can take or replace a photo, or remove it.

**UI.** Toolbar "Photos" on `titlebarcolor`. Each row shows the image (from `LoanRelatedDocumentsUploadPATH` + ImagePath, or "noimage"), the document type and the remarks. In edit mode the row also has a remove (X) button.

**Menu / actions.** Search filters by document description. Row tap → `documentsupload`; this also happens in display mode. X → remove the photo. Back → returns `docslist` with result 1.

**How it works.**
1. Display mode (`calledfor`=100) runs `ASYNCRelatedDocslist`: `select MCLoanId,DCUId,a.DUId,DocDesc,Remarks,ImagePath from RelatedDocmenttypes a, RelatedDocCollection b where (a.DUId=b.DUId and MCLoanId=<loan> and a.isImageAllowed=1) order by showid`. This shows collected documents only.
2. Edit mode uses the list passed in by `newloancreation`.
3. rc 2 / result 2 replaces the document that has the same DUId.
4. X clears ImagePath on the document with that DCUId and runs `ASYNCUploaddocumentinfo`, which updates the server row (ProgressDialog shown).

**Functions.** `onCreate()`, `onActivityResult()`, `onContactSelected()`, `onImageRemoveClicked()`, `ASYNCUploaddocumentinfo`, `ASYNCRelatedDocslist`, `onCreateOptionsMenu()`/`onOptionsItemSelected()`, back handler, `whiteNotificationBar()`.

**Rules & edge cases.** The search filter is broken for Latin text (see the adapter table). Remove only updates the server; the local `RelatedDocCollection` is refreshed by the caller from the returned list.

## Upload one loan document — `documentsupload`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); title = document description. |
| **Layout(s)** | `activity_documentupload.xml` |
| **Opened from** | `documentsphotossearchlist` row tap (extra `passeddocument`, a documentscollected_data object). |
| **Opens** | `ImagePickerActivity` (rc 100; camera crop 3:4 with a maximum of 8000×11000, gallery crop 8:11); `fullimageview`. |
| **Data** | Server `branchconnString`: `dbo.InsertImageUploads`, `dbo.InsertRelatedDocCollection(?,?,?,?,?)`, `update RelatedDocCollection …`. SQLite `RelatedDocCollection` insert. HTTP POST to `LoanRelatedDocumentsUploadURL` (setting 73); images are viewed from `LoanRelatedDocumentsUploadPATH` (74). |

**Purpose.** Capture or replace the photo of one loan document, add remarks, and upload it.

**UI.** A large image (the current document, or "noimage"); tap it for full screen. A "+" icon chooses a new photo. "Remarks" field. "Save and Upload" button, which reads "Pleas Wait" while busy. Progress bar and percentage label.

**Menu / actions.** "+" → picker dialog. Image tap → `fullimageview`. Save and Upload → `ASYNCUploaddocumentinfo`.

**How it works.**
1. If the image changed, `InsertImageUploads` returns IUId and ImagePath becomes `document_<IUId>.jpg`.
2. If DCUId = 0, `InsertRelatedDocCollection` (MCLoanId, DUID, Remarks, ImagePath) returns the new DCUId. Otherwise the existing server row is updated.
3. If the loan is not on the server yet (`servergenmcloanid`=0), a local `RelatedDocCollection` row is inserted (DCUId, MCLoanId, DUID, Remarks, ImagePath).
4. If the image changed, `UploadgroupphototoServer` POSTs a multipart request with Apache `DefaultHttpClient`: part `image` (the file renamed to the new name, field `fname`="NEWNAMEOFILE.jpg"), `website`, and a hard-coded vendor e-mail address.
5. Returns result 2 with `passeddocument` and finishes.

**Functions.** `onCreate()`, `loadProfile()`/`loadProfileDefault()`, `showImagePickerOptions()`, `launchCameraIntent()`/`launchGalleryIntent()`, `onActivityResult()` (rc 100 image, rc 5000 finish), `ASYNCUploaddocumentinfo`, `UploadgroupphototoServer.uploadFile()`, `transferred` progress callback; `openSettings()` and `showAlert()` are unused.

**Rules & edge cases.**
- Errors are only printed, so the screen returns success either way.
- The progress percentage uses integer division and stays at 0% until the upload completes.
- The picked image is fully decoded with `MediaStore.getBitmap` only as a check, which costs a lot of memory for 8000×11000 images.
- The local row's `ServerGenMCLoanId` is left NULL. Duplicate local rows are later replaced by `newloancreation`.

## Image picker & cropper — `ImagePickerActivity`
| | |
|---|---|
| **Type** | Plain `Activity` with no UI of its own (`activity_image_picker.xml` is an empty ConstraintLayout); no theme in the manifest. Uses Dexter (permissions), uCrop and `androidx.core.content.FileProvider` (authority `<package>.provider`). |
| **Layout(s)** | `activity_image_picker.xml` |
| **Opened from** | `partydetails_act` (1:1), `documentsupload` (3:4 / 8:11), `accounttransactions_act` (16:9). The static `showImagePickerOptions()` dialog is called by the same screens. |
| **Opens** | System camera (`android.media.action.IMAGE_CAPTURE`, rc 0), gallery (`ACTION_PICK` on MediaStore images, rc 1), `UCropActivity` (rc 69 = `UCrop.REQUEST_CROP`). |
| **Data** | Files `<externalCacheDir>/camera/<millis>.jpg` and `<cacheDir>/<source display name>`. No DB access. |

**Purpose.** Shared helper that takes a camera photo or picks a gallery image, crops and compresses it, and returns its Uri.

**UI.** The dialog "Set image" offers the items "CAMERA" and "GALLERY".

**Menu / actions.** None.

**How it works.**
1. Reads the intent extras: `image_picker_option` (0 = camera, else gallery), `aspect_ratio_x`/`aspect_ratio_Y` (default 16:9), `lock_aspect_ratio`, `compression_quality` (default 80), `set_bitmap_max_width_height`, `max_width`/`max_height` (default 1000).
2. Asks for CAMERA (plus READ_MEDIA_IMAGES on API ≥ 33). The gallery path asks for the same permissions.
3. Camera: EXTRA_OUTPUT is a FileProvider Uri. Gallery: `data.getData()`.
4. `cropImage()` runs uCrop with the compression, `colorPrimary` toolbar and status bar, and the optional aspect lock and max size.
5. rc 69 OK → `setResultOk()` returns the extra "path" (Uri) with RESULT_OK. Cancel, or rc 96 (`UCrop.RESULT_ERROR`, logged), returns RESULT_CANCELED.

**Functions.** `onCreate()`, `showImagePickerOptions()` (static), `takeCameraImage()`, `chooseImageFromGallery()`, `onActivityResult()`, `cropImage()`, `handleUCropResult()`, `setResultOk()`, `setResultCancelled()`, `getCacheImagePath()`, `queryName()`, `clearCache()` (static, never called).

**Rules & edge cases.**
- Denied permissions leave a blank activity open until the user presses Back.
- The camera cache is never cleared.
- `file_paths.xml` exposes `external-path "."` (all of shared storage), which is broader than needed (the provider itself is not exported).
- `toast_image_intent_null` is the literal "nULL".

## Full-screen image viewer — `fullimageview`
| | |
|---|---|
| **Type** | Plain `Activity`; no manifest label. |
| **Layout(s)** | `activity_fullimageview.xml` |
| **Opened from** | `partydetails_act` (party photo and ID proofs), `documentsupload` (document), `accounttransactions_act` (transaction photo, `globalvars.groupphotospath`). Extra `passedimage` (URL). |
| **Opens** | — |
| **Data** | Image URL only. |

**Purpose.** Show one server image full screen with pinch-zoom.

**UI.** `ZoomageView` (`imgfacebook`), loaded by Glide with `centerInsideTransform`.

**Menu / actions.** Back only.

**How it works.** `onCreate()` reads `passedimage` (a missing bundle causes a NullPointerException through `requireNonNull`) and loads it into the view.

**Functions.** `onCreate()`.

**Rules & edge cases.** Callers always pass the server URL (path + file name), so a photo that was just picked but not yet uploaded is not shown.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `partydetails_data` | Serializable model (getters/setters only) | All party screens, `newloancreation`, `parties_list`, ST/OF loan screens, collection screens, `uploadcollection` (27 classes) | **Identity:** LedgerId, ServerGenLedgerId, CWLedgerId (party No, boxed Integer), UDLedgerId, LedgerName, Title, Tag (business). **Grouping:** GroupId, PartyGroupId, ScreenType (3 = MC party, 21 = party, 22 = partner), FinId, SubAreaNo (boxed Integer), SubAreaName. **Address/contact:** AddLine1/2, City, PinCode, LandMark, MobileNo1/2, EMail. **Family/guardian:** SpouseName, FamilyName, GType, GuardianName, GuardianMobileNo1/2, GuardianProfession, GuardianMonthlyIncome, MasterPartyLedgerId. **KYC:** PanNo, AadhaarNo, Imagefilename, IdProofFileName, IdProofImageIIfileName, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome. **Accounting/misc:** OpeningBalance, OpBalType, IsDefault, IsReserved, ListSNo, DesignationId, StatusId, AndroidDeviceId. **Limits/flags:** NoOfLoansAllowed, MaximumAmountAllowed, isactive (and a separate isActive via getActive/setActive), isLiveParty (the party has a live loan in the downloaded collection list). |
| `documentscollected_data` | Serializable model | `newloancreation`, `documentsphotossearchlist`(+adapter), `documentsupload` | DCUId (server document-collection id), duid (document type from `RelatedDocmenttypes`), docdesc, remarks, imagepath (file name), iscollected, mcloanid, servergenmcloanid. |
| `invtransdetails_data` | Serializable model | `newloancreation`, `invtrans_rowdetails`, `invtransactnew`, `directcollectionentry`, `tabbedindividualcollectionentry`, adapters | Product line: SNo, Itemcode, Itemname, BilledQty, ActualQty, Rate, TotalAmt, Transid. |
| `customersearchadapter` | RecyclerView adapter, Filterable | `partylistwithprevloansfornewloanselection` | Row `partysearchrowfornewloan`: No (CWLedgerId), name (blue 0xFF0000FF if live, else black; JADX shows black as `ViewCompat.MEASURED_STATE_MASK`), tag, mobile 1/2. Row tap → `onContactSelected(party)`; "Prev Loans" → `onprevloansbuttonclicked(party)` (button hidden in the layout). Filter: name contains (case-sensitive), tag contains, or the number equals the query. |
| `partydetails_adapter` | RecyclerView adapter, Filterable | `partydetails_searchlist` | Row `partydetailssingle_row`: circular-cropped Glide thumbnail (`partiesimagepath`+Imagefilename, fallback `ic_action_aboutus`; hidden unless setting 21), No, name, sub-area name. Tap → `onContactSelected(party, pos)`. Filter: exact No, name contains (lower-case or raw), or sub-area contains. |
| `documentsphotossearchlist_adapter` | RecyclerView adapter, Filterable | `documentsphotossearchlist` | `documents_row` (edit) or `documentsdisplay_row` (display, remove button hidden). Shows document type, remarks, and the image `LoanRelatedDocumentsUploadPATH`+imagepath with a spinner placeholder (or "noimage"). Row tap → `onContactSelected`; X → `onImageRemoveClicked`. Filter bug: the query is upper-cased but compared with the lower-cased description, so Latin letters never match (Tamil works). |
| `invtransdetails_adapter` | RecyclerView adapter, Filterable | `newloancreation`, `invtransactnew`, `directcollectionentry` | Row `invtransdetails_row`: S.No, product name (looked up per bind in `ItemMaster` through `getitemnameforitemcodefromstok`, opening SQLite each time), Qty, Price, Amount. Price and Amount are hidden when `hideprice`. Tap → `onContactSelected(row, pos)`. The filter does nothing (returns all rows). |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_newloandetails.xml` | `newloancreation` | Party name/tag header; Date row; Loan Method; Collection Day/Due Date; hidden Loan Index; Products block (header, RecyclerView, FAB "add new item", "Total:"/Product Value, Advance, Balance, Int Rate); Loan Amount, Interest Amount, Document Charges, Loan in Hand, Total Collectable, No Of Instalments + "Show Schedule", Instalment Amount; On Day Collection; hidden On Day Savings; Remarks; Mobile No; Paid as/In; Payment Reference; Related Documents; Update (சேமி) / DELETE (நீக்கு); progress bar. |
| `activity_newpartycreation.xml` | `partydetails_act` | Area name; include `layout_toolbar_profile` (photo `memberimageid` + `img_plus`); hidden No of Loans Allowed / Maximum Amount Allowed rows; No; Party Name; Business; Spouse Name; Family Name; House No/Street; Mobile No 1/2; ACTIVE checkbox; location button + lat/lng labels; ID Proof-1 / ID Proof-2 buttons with images; Update / DELETE; percentage text; progress bar. |
| `activity_partyandpartnerdetailsnew.xml` | `partyandpartnerdetails` | Hidden task label; hidden photo header include; Name, Tag, Guardian Type, Guardian Name, Address Line 1/2, City/Area, Pincode, Mobile No 1/2; Master Party + remove icon; hidden additional block (Landmark, Age, Marital Status spinner, Profession, Monthly Income, PAN, Aadhaar No, Guardian Contact No 1/2, Guardian Profession, Guardian Monthly Income); Update/Delete (gone by default); progress bar and percentage text. |
| `activity_customersearch.xml` | `partylistwithprevloansfornewloanselection` | CoordinatorLayout: Toolbar; SwipeRefreshLayout with RecyclerView; FAB (+); hidden progress bar and TextView. |
| `activity_employeesearch.xml` | `partydetails_searchlist` (shared with other list screens) | Toolbar; include `customersearchlist` (RecyclerView `recycler_view`); FAB `addnewemployeeid` (hidden here); progress overlay with ProgressBar. |
| `activity_documentsearchlist.xml` | `documentsphotossearchlist` | Toolbar; RecyclerView; hidden progress bar and status text. |
| `activity_documentupload.xml` | `documentsupload` | Document ImageView; circular "+" (`ic_plus`); "Remarks" label and field; "Save and Upload" button; hidden progress bar and percentage text. |
| `activity_fullimageview.xml` | `fullimageview` | ZoomageView `imgfacebook` (pinch-zoom). |
| `activity_image_picker.xml` | `ImagePickerActivity` | Empty ConstraintLayout (no widgets). |
| `documents_row.xml` | `documentsphotossearchlist_adapter` (edit mode) | Document image, "Document Name", "Remarks", remove ImageButton (`ic_action_remove`). |
| `documentsdisplay_row.xml` | `documentsphotossearchlist_adapter` (display mode) | Same as `documents_row` with the remove button gone. |
| `invtransdetails_row.xml` | `invtransdetails_adapter` | S.No, Product, Qty, Price, Amount; hidden ImageView. |
| `partydetailssingle_row.xml` | `partydetails_adapter` | Thumbnail; No "." Party Name; collection-area name; hidden amount TextView ("2500"). |
| `partysearchrowfornewloan.xml` | `customersearchadapter` | No "." Party Name (பார்ட்டி பெயர்); Business (தொழில்); mobile 1 / mobile 2; hidden container with a "Prev Loans" button. |

---
[← brain.md](../../brain.md) · Module 8.7 of 20 · Previous: [8.6 Collection reports (collection for a day, abstract, 7-days, unpaid parties, day-wise pending, periodic loans)](06-collection-reports-collection-day-abstract-7.md) · Next: [8.8 Loan lists, loan & savings closures, party transfer and rearrangement](08-loan-lists-loan-savings-closures-party.md)
