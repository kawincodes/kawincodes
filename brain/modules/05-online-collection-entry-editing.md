# 8.5 Online collection entry & editing

[← brain.md](../../brain.md) · Module 8.5 of 20 · Previous: [8.4 Offline collection entry (area-wise collection list & individual collection)](04-offline-collection-entry-area-wise-collection.md) · Next: [8.6 Collection reports (collection for a day, abstract, 7-days, unpaid parties, day-wise pending, periodic loans)](06-collection-reports-collection-day-abstract-7.md)

These screens read and write the day's micro-loan (MC loan) collection directly on the SQL Server, with no offline download/upload step. `editing_collectionswithfulloptions_act` has two jobs. With `calledfrom` 9907 (`collectionlistrequest_fromtodaycollectionentry`) it is the executive's live **Collection Entry** for today's line. With `calledfrom` 9908 (`collectionlistrequest_fromtodaycollectionlist`) it is the admin's full-list editor for a day that is already saved. `editing_collectionlist_act` is the admin **Loans Collection** register: one row per `tblMCLoanGroupCollectionMaster` record in a date range. It opens either the full-options editor or the simpler `editing_collections_act`, depending on soft setting SSId 61. On the server, each collection day for a line is stored as one group-collection master (`MCLGCollId`, with the finance-wise number `FWGColId`), one TTId-22 voucher (`tblTransactionsMaster` + `tblTransactionsDetails`) and one `tblMCLoanDueCol` row per loan paid.

## Collection Entry (online) / full-options collection editor — `editing_collectionswithfulloptions_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) implementing `editing_collectionsadapter.AdapterListener`. Manifest: theme `AppTheme.NoActionBar`, not exported. JADX failed on `AsyncDownloaddata.doInBackground`, `ASYNCSavingCollections.doInBackground` and `drawPage_method01`; the logic below was checked against smali. |
| **Layout(s)** | `activity_editingfullcollections.xml` (includes `customersearchlist.xml`); rows `tabbedcollectionlistrow.xml` via `editing_collectionsadapter`; menu `menu_collectionediting` |
| **Opened from** | (1) Drawer *Executive Entries (online) → Collection Entry* (`nav_collectionentry`), extra `calledfrom`=9907, plain `startActivity`. (2) `editing_collectionlist_act.onContactSelected` when `Enablefulloptioonsincollectionlist_11` (SSId 61) is on: `calledfrom`=9908, extras `selectedcollection` (`groupcollectionslist_data`), `selectedcollectiondate`, `receviedfinanceid`, `receivedfinancename`; forResult 101. |
| **Opens** | `downloaddata` (`calledfrom` 101, rc 1000; picks the date and executives when no session exists). `tabbedindividualcollectionentry` ("Collection Entry" pager; rc 1, row tap in 9907). Its extras are `SubAreaName` (actually the party name), `SubAreaNo`, `MCLoanId`, `collectionareaname` (the party Tag), `Islocallyfixed` and `listindex` (position within the area); the list itself is shared through `globalvars.tabbedcollectionlistdataList`. `collectionedit_individualcollection` (cash/bank split editor; rc 2, row tap in 9908; extra `selectedcollection`). `partylistwithprevloansfornewloanselection` (sub-area header tap, 9907 only; extras `AreaName`, `SubAreaNo`, `calledfrom` 9017 = new-loan-from-area mode). Starts service `MyService_updatecollection`. Also the system print dialog, a PDF viewer (ACTION_VIEW) and the share chooser. |
| **Data** | **Local SQLite:** `MobileAppCollData` (rebuilt and updated), `Parties`, `SubAreaDetails`, `LastUpload`, `selectedcollectiondetails`, `PrevCollections`, `PrevLocations`, `loanindex`. Clears `NewItems`, `ExpHeaders`, `NewParties`, `NewMCLoans`, `ExpensesEntries`, `RelatedDocCollection`. Reads `CollectionExecutives`, `SMSTemplates`, `SelectedBranchFinanceDetails` (`SMSGateWay` is created but never read). **Remote** (`selbranchconnString`; loading switches to `branchlevelReport_branchconnString` on a master DB): `FinanceAccessAllowed`, `DownloadControling`, `MobileDevlicesAllowed`, `softsettings` (SSId 14, 32), `financemaster`⨝`LoanMethods`, `subareadetails`, `tblLedgers`, `tblMCLoanDueCol`, `tblMCLoanGroupCollectionMaster`, `tblTransactionsMaster`, `tblTransactionsDetails`. **Procedures:** `Get_LoanindexwiseLoans`, `Get_LoanindexwiseLoansforweeklylines`, `APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport` / `…_withoutWeekdayssupport`, `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport` / `…_WithOUTWeekdaysSupport`, `PrepareLast5CollectionsforPrev5dates`, `PrepareLast5CollectionsforPrev5weeks`, `PrepareLast5Collections`, `PrepareLast5Locations`, `dbo.InserttblTransactionsMaster`, `dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs`, `dbo.InserttblMCLoanDueCol`. **Prefs:** writes `userroleid` (file `venusmicrofinpref011`). The other "prefs" listed in the dossier are cursor column names, not preferences. |

**Purpose.** In 9907 mode the field executive sees every loan due on the line for the collection date, grouped by collection area. They type each party's installment (plus penalty or savings if enabled), "fix" the entries so a background service posts them to the server, and finally **Finish this Collection**. In 9908 mode an admin reopens a saved day, sees the whole due list with that day's paid amounts filled in, and can correct, add, delete, print or share it.

How the three editing modes differ:

| | Full options, 9907 (live entry) | Full options, 9908 (admin edit) | `editing_collections_act` (basic, 9908) |
|---|---|---|---|
| Reached from | drawer Collection Entry | Loans Collection list, SSId 61 on | Loans Collection list, SSId 61 off; loan collection history |
| Rows shown | the whole due list from an `APP_MCLoancollectionData…` procedure, cached in `MobileAppCollData` | the same procedure for the saved date, merged with that day's `tblMCLoanDueCol` rows | only the rows `MCLoanCollectionDisplay @GColId` returns |
| Can add a payment for an unpaid loan | yes (background service, and again on Finish) | yes (insert on Save) | no (update and delete only) |
| Last-5-collections strip, GPS capture | yes / yes | no / yes | no / no |
| How changes are saved | FAB "fix" → sync every 10 s → menu **Finish** | menu **Save** / **Delete** | menu **Save** / **Delete** |
| SMS sent | template 2 on Finish | 11 on save, 10 on delete | 11 / 10 |
| Print / PDF / Share | menu hidden | yes (33 rows per A4 page) | yes (45 rows per page) |

**UI.**
- **Toolbar title:** the collection date `dd-MM-yyyy`. In 9908 it first reads `dd-MM-yyyy-Coll List`, then the plain date once loading finishes.
- **Search:** a SearchView filters by party name, sub-area name, or an exact party number (CWLedgerId).
- **List:** rows from `editing_collectionsadapter`, grouped under dark-blue collection-area headers. See *Adapters* below for row contents and colours.
- **Bottom bar** (dark blue):
  - `lastupdatelbl`: time of the last background sync (`h:mm:ss a`) or the sync error text.
  - "Total:" followed by `editingcollection_totalid` (sum of collected amounts).
  - Unlabelled penalty and savings totals, shown when `selectedfinance_ispenaltyallowedincollectionscreen` / `selectedfinance_issavingscollectionallowedincollectionscreen` are on. These load from the same preference keys as SSId 45/46.
- **FAB** (save icon): shown only when `vss_executivehandlingmethod`==2 (VenusSoftSettings VSSId 22, online mode) and in 9907.
- **Progress:** a centred spinner plus status text (white bold on dark green).
- **Strings:** Tamil resources reuse the English text for every label here.

**Menu / actions** (`menu_collectionediting`; everything except Search sits in the overflow menu)
- **Search**: always shown.
- **Save Collections**: shown if the role has Update rights on menu `nav_loanscollection` and the mode is not 9907. Runs the save with requestcode 101.
- **Delete Collections**: shown if the role has Delete rights and the mode is not 9907. Asks for confirmation first: title "Warning...", message "you about to delete entire collection of the day, if it is deleted then it can not be get back", buttons Cancel/OK. OK runs the delete with requestcode 102.
- **Finish this Collection**: shown only in online mode and 9907. Re-reads `LastUpload`, then runs the save with requestcode 101. There is no confirmation dialog.
- **Print Share Options**: shown when not 9907. Opens the dialog "Print and Share Options" with "Direct Print", "Show as PDF" and "Share".
- **FAB**: "fixes" the entered rows (step 7 below).
- **Row gestures:** tap a row, tap a sub-area header, double-tap the Due amount, tick the visited checkbox, edit the amount boxes (see the adapter).
- **Back:** closes the search box first. Otherwise it returns `selectedcollection` with resultCode = `requestcode` (0 if nothing was saved).

**How it works.**
1. **onCreate**
   - Opens SQLite and starts `MyService_updatecollection`.
   - Access limits: full-access devices get every right. Otherwise the code takes the `AndroidMenus` row for `nav_loanscollection`, even in executive mode.
   - 9908: reads the extras.
   - 9907: finance = `globalvars.finid` / `financename`. If `LastUpload` has a row, `selectedcollection` is rebuilt from it (staff 1/2, MCLGCollId, FWGColId, TransId, CollDate = `globalvars.collectiondate`) and `isLastUpdateFound` is set to true.
   - Then one of three paths:
     - 9907 with a session in progress → `DisplayIndexData()` (resume from the local cache).
     - 9908 → `AsyncDownloaddata`.
     - 9907 with no session → opens `downloaddata`; its result `CollectionDate` (resultCode 1) starts `AsyncDownloaddata`. If the user backs out of `downloaddata`, the screen stays empty.
2. **AsyncDownloaddata.onPreExecute** creates and empties these local tables in **both** modes: `MobileAppCollData`, `LastUpload`, `NewParties`, `NewMCLoans`, `NewItems`, `ExpHeaders`, `ExpensesEntries`, `Parties`, `PrevCollections`, `SubAreaDetails`, `RelatedDocCollection`.
3. **AsyncDownloaddata.doInBackground** (progress steps "Connecting" … "Done")
   - **a. Connection.** Opens `selbranchconnString`. On a master DB it reconnects to `branchlevelReport_branchconnString`, but passes the *password* as the user name.
   - **b. Access check** (online mode and 9907 only):
     - Query: `select top 1 … case when (select bitvalue from softsettings where SSId=32)='True' and isAllowed='True' then 'True' when (…SSId=32)='False' then 'True' else 'False' end as IsAllowed from FinanceAccessAllowed x left join DownloadControling z on x.FinId=z.FinId and TransDate='<date>', MobileDevlicesAllowed y where x.IMEI=y.IMEI and x.FinId=<fin> and y.isActive=1 and x.IMEI='<ANDROID_ID>'`.
     - The device must be active and allowed for the line. When SSId 32 (`enablemoniterondownloadandupload_1`) is True, the admin must also have released that date in `DownloadControling`. Full-access devices skip the check.
     - Reads `MobileDevlicesAllowed.userroleid` into `globalvars.app_userroleid` and the `userroleid` preference.
     - Rebuilds local `loanindex` (LedgerId, LoanIndex, MCLoanId, MCLoanAmountCollectable, collected, Balance) from `Get_LoanindexwiseLoans @FinId` (daily lines) or `Get_LoanindexwiseLoansforweeklylines @ColDate,@FinId` (weekly lines). Progress shows "Sync Loan Indexing-n".
   - **c. Finance details** (only if the check passed):
     - SSId 14 → closure-monitoring flag.
     - `financemaster`⨝`LoanMethods` (`defaultloanmethodid`) → `cashledgerid`, used as the voucher's cash ledger. `CollectionBreakString` "W" or "ww" means a weekly line.
     - Copies `select MCLDueCollId,MCLoanId,GColId,Paid,PenaltyCollected,SavingsCollected,CashCollection,BankCollection from tblMCLoanDueCol where GColId=<MCLGCollId>` into `selectedcollectiondetails`.
   - **d. Area and party masters** for the executive's current line (`globalvars.finid`, not the edited finance):
     - `subareadetails where FinId=…`, with weekly lines also filtered by `WeekDayId=globalvars.currentweekdayid`, goes into `SubAreaDetails`.
     - `tblLedgers where ScreenType=3 and isactive=1 and SubAreaNo in (…)` goes into `Parties`. Both branches of the special-access-11 test run the same query.
   - **e. Due list.** The procedure depends on SSId 14 and on whether the line is weekly:
     - monitoring + weekly: `APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport @ColDate,@FinId`
     - monitoring + daily: `…_withoutWeekdayssupport @ColDate,@CollectionListfromDate,@FinId`
     - no monitoring + weekly: `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport @ColDate,@FinId`
     - no monitoring + daily: `…_WithOUTWeekdaysSupport @ColDate,@CollectionListfromDate,@FinId`

     Loan closure (automatic, or monitored) happens inside these procedures. The app never closes a loan itself.
   - **f. Rows.** Each row becomes a `tabbedcollectionlistdata` with Balance = MCLoanAmountCollectable − PrevCollected.
     - 9907: also inserted into `MobileAppCollData` with Collected/Penalty/Savings = 0 and IsMarked/IsVisited = false, plus LoanIndex, Noofloansallowed, Doccharges and IsAttentionRequired.
     - 9908: merged with `selectedcollectiondetails` (Collected = Cash + Bank, penalty, savings, MCLDueCollId).
     - Rows are sorted by ShowId. 9907 with no rows → error "No Data Found for this date".
   - **g. History** (9907 online). `fivedayscollectiondetailsmethod_8` (SSId 47) chooses the source:
     - 1 → `PrepareLast5CollectionsforPrev5dates @FinId`, or `PrepareLast5CollectionsforPrev5weeks @ColDate,@FinId` for weekly lines.
     - 2 → `PrepareLast5Collections @FinId`.
     - Results go into `PrevCollections`. If `vss_enablelivelocationduringcollectionentry` (VSS 23) is on, `PrepareLast5Locations @FinId` fills `PrevLocations`.
   - **h. Initialize Collection** (9907 online, first time only):
     - `{call dbo.InserttblTransactionsMaster(12)}`: OUT `TransId`, `FWVWTransId`; TransDate; TransType 22; Narration ''; FinId; TransAmount 0; FWTransId, FWVWUDTransId, GColId and TransLedgerId NULL; RouteId = FinId.
     - `{call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(13)}`: OUT `MCLGCollId`, `FWGColId`; CollDate; every amount 0 (Coll, Penalty, Savings, NIP, Bank, Cash); CollStaffLedgerId / CollStaff2LedgerId = `currentcollectionexecutivelederid1/2`; TransId; FinId.
     - Two zero voucher lines: (114, 'Source','Cr', SNo 1, AgnstLedgerSNo 2) and (finance cash ledger, 'Target','Dr', 2, 1).
     - Errors in this block are swallowed silently.
     - Server IST time (`SWITCHOFFSET(SYSDATETIMEOFFSET(),'+05:30')`) is read, and a new `LastUpload` row stores MCLGCollId, FWGColId, TransId, SubmitAmount 0, SubmitedAt and the staff ids.
4. **onPostExecute**
   - Error → dialog "Error Info..." → finish.
   - Access denied → dialog titled "தங்கள் கவனத்திற்கு..." ("For your attention...") with " Access not Allowed" → finish.
   - Otherwise refreshes the list and totals, and shows the penalty and savings totals if enabled.
5. **`DisplayIndexData()`** rebuilds the list from `MobileAppCollData`⨝`Parties`⨝`SubAreaDetails`, ordered by ShowId then UDLedgerId.
6. **Inline entry callbacks**
   - `oncollectionamountchanged`: calls `getLastLocation()`; sets Collected = x and Cash = x − Bank; stores lat/lng; updates `MobileAppCollData` (Collected, CashCollection, BankCollection, lat, lng); refreshes totals.
   - `ondueamounttabbed` (double-tap on Due): sets Collected = due, but writes lng←latitude and lat←longitude (swapped).
   - Penalty and savings edits: `update MobileAppCollData set PenaltyCollected=… / SavingsCollected=…`.
   - Visited checkbox: `update MobileAppCollData set Visited=…`. That column does not exist (the table has `IsVisited`), so the update throws.
7. **FAB "fix" and background sync**
   - The FAB marks every row that is not yet fixed, has `MCLDueCollId` 0 and has any amount > 0: `update MobileAppCollData set islocallyfixed='true'`.
   - `MyService_updatecollection` (another module) checks every 10 s for rows that are fixed but have no `MCLDueCollId` and Collected > 0. For each it calls `dbo.InserttblMCLoanDueCol` and stores the returned id locally. It then updates `tblMCLoanGroupCollectionMaster` (CollAmount, Penalty, Savings, NIP 0) from the local sums.
   - The service then broadcasts `com.example.andy.myapplication` with `DATAPASSED`. The activity's receiver (registered not-exported in onStart, removed in onStop) puts the value in `lastupdatelbl` and reloads `MCLDueCollId` values, so synced rows turn white.
8. **ASYNCSavingCollections** (`selbranchconnString`, autocommit, no transaction)
   - Recomputes Cash, Bank, CollAmount (sum of Collected), Penalty and Savings. In 9907 it first refreshes each row's `MCLDueCollId` from the local table.
   - **101 (Save / Finish):**
     - `update tblMCLoanGroupCollectionMaster set CollDate=?,CollAmount=?,CollStaffLedgerId=?,NIPAmountCollected=0,CollStaff2LedgerId=?,PenaltyCollected=?,SavingsCollected=?,CashCollection=?,BankCollection=? where MCLGCollId=?`
     - `update tblTransactionsMaster set TransAmount=<CollAmount> where TransId=?`
     - Rebuilds the voucher in one batch: `delete from tblTransactionsDetails where TransId=…;`, then:
       - if cash > 0: (114, cash, 'Source','Cr', 1, 2) and (finance `cashledgerid`, falling back to `globalvars.cashbookledgerid` when 0; cash; 'Target','Dr'; 2, 1).
       - if bank > 0: (114, bank, 'Source','Cr', 3, 4) and (`globalvars.bankaccountledgerid`, bank, 'Target','Dr', 4, 3).
     - Ledger 114 is hard-coded as the credited (Source) ledger for every collection, the same value `uploadcollection` and `directcollectionentry` use. Penalty and savings are not posted to any ledger.
     - Per row, when role ≠ 3:
       - `MCLDueCollId`≠0 and any of Collected/Penalty/Savings > 0 → `update tblMCLoanDueCol set Paid=?,SavingsCollected=?,PenaltyCollected=?,CashCollection=?,BankCollection=? where MCLoanId=? and MCLDueCollId=?`
       - `MCLDueCollId`≠0 and all three = 0 → `delete from tblMCLoanDueCol where MCLoanId=? and GColId=?`
       - `MCLDueCollId`=0 and Collected > 0 → `{call dbo.InserttblMCLoanDueCol(15)}`: OUT MCLDueCollId; MCLDueColDate = CollDate; MCLoanId; TTId 22; SourceLedgerId = party ledger; TargetLedgerId = 1; FinId; Paid; GColId = MCLGCollId; PenaltyCollected; SavingsCollected; CashCollection; BankCollection; lng; lat. The new id is written back to `MobileAppCollData`.
     - Role 3: insert branch only. FinId falls back to the row's finance. An XML document "mcloancollections" is built and never used.
   - **102 (Delete):** `delete from tblMCLoanDueCol where GColId=?`, then `delete from tblMCLoanGroupCollectionMaster where MCLGCollId=?`, `delete from tblTransactionsDetails where TransId=?`, `delete from tblTransactionsMaster where TransId=?`.
   - **onPostExecute:**
     - Error → "Error Info".
     - 101 in 9908 → `Update_sendsms`, message "Successfully Updated".
     - 102 in 9908 → `delete_sendsms`, message "Successfully Deleted".
     - 9907 → `onlinefinishingcollectionSendSMS`, clears local `MobileAppCollData` and `LastUpload`, message "Collection Finished".
     - Pressing OK on the "Info" dialog returns `setResult(requestcode, selectedcollection)` and finishes.
9. **SMS** (the toast "SMS Sent" always appears)
   - **Recipients:** `globalvars.AdminMobileNos`. On a master DB, Update and Delete use `SelectedBranchFinanceDetails.AdminMobileNos` for the FinId instead.
   - **Finish uses template 2** (upload alert):
     - `{LineName}` → `smsidentifier` + "\nDt: " + date; "\n@ " + time is appended to the end of the message.
     - `{executive1}` / `{executive2}` → names from local `CollectionExecutives`.
     - `{CollAmt}` → the literal "Collections Finished"; the amount is not sent.
     - `{LoanAmount}`, `{expenses}`, `{Receipts}` → blank.
   - **Update uses template 11, Delete uses template 10:**
     - `{Finance}` (`fetchfinanceName`), `{coldate}`, `{staffname}` (= `APPUserName`), followed by "@ dd-MM-yy hh:mm aa". If the template is empty, nothing is sent.
   - **Delivery** depends on `smsmethod` (SSId 50):
     - 1 → `SmsManager.sendMultipartTextMessage` with the whole number string.
     - 2 → HTTP POST to an **empty** base URL + `?type=unicode&smstext=…&smsto=…&senderid=…&routeid=8&api_key=…`. The Finish task hard-codes sender id `STARCB` and an API key literal ([REDACTED]).
   - Template 16 is not used here.
10. **Print, PDF and Share**
    - Page layout: "COLLECTION LIST", "Page n/N", "FINANCE : <receivedfinancename>", "Collection Date : dd-MM-yyyy".
    - Columns: No, Party Name, Loan (LAmount), Due (InsAmount), Status (DuesCount/PassedDues), Paid (LAmount − Balance), Balance, Coll (Collected).
    - Each new sub-area gets a centred heading row. Rows are 20 pt; 33 rows per page (`calculatenoofpages`).
    - "Printed @ dd-MM-yyyy, HH:mm:ss" appears only on pages that fill to 33 rows.
    - **Direct Print:** `PrintManager` job "<app_name> Document", A4; the adapter uses 800×1140 pages.
    - **Show as PDF:** `Documents/venusmfc/taxinv_<millis>.pdf` (595×842), opened through FileProvider `<package>.provider`.
    - **Share:** writes the same file, then sends ACTION_SEND (application/pdf, subject and text "Collection Details") via the chooser "Share PDF using...".
11. **onActivityResult**
    - rc 1000 / result 1 → new date, reload.
    - rc 2 / result 1 → replace that row with the returned `selectedcollection`.
    - Anything else (including rc 1 from the pager) → refresh the list and totals. The pager edits the shared `globalvars.tabbedcollectionlistdataList` directly.

**Functions.**
- `onCreate()`: sets up widgets, mode, access limits, the service and the first load (step 1).
- `lambda$onCreate$1` (FAB): marks entered, unsynced rows `islocallyfixed`.
- `onStart()` / `onStop()` and `broadcastReceiver.onReceive()`: listen for sync broadcasts and refresh `MCLDueCollId` and the last-sync label.
- `DisplayIndexData()`: rebuilds the list from the local cache; the comparator orders by ShowId then UDLedgerId.
- `onCreateOptionsMenu()`: decides which menu items are visible and wires the SearchView filter.
- `onOptionsItemSelected()`: handles Save, Finish, Delete (with confirmation) and Print/Share.
- `printDocument()` and `MyPrintDocumentAdapter.onLayout/onWrite/pageInRange()`: system printing.
- `createPdfA4()`, `sharePdf()`: write the PDF, then view or share it.
- `drawPage_method01()`, `drawCenter()`, `drawCenterwithgivenwidth()`, `calculatenoofpages()`: page rendering and pagination.
- `oncollectionamountchanged()`, `ondueamounttabbed()`, `onpenaltyamountchange()`, `onsavingsamountchange()`, `onvisitstatuschanged()`: save inline edits to memory and `MobileAppCollData`.
- `onrowselected()`: opens the pager (9907) or the cash/bank editor (9908).
- `onsubareanameclicked()`: opens the party list for a new loan (9907).
- `oncollectionboxlostfocus()`: notifies the adapter (never triggered by it).
- `settotalcollection()`: sums Collected, Penalty and Savings into the footer and `selectedcollection`.
- `AsyncDownloaddata`: access check, master data and due-list load, day initialisation (steps 2–4).
- `ASYNCSavingCollections`: save, finish and delete (step 8).
- `onlinefinishingcollectionSendSMS`, `Update_sendsms`, `delete_sendsms`: SMS templates 2, 11 and 10.
- `onActivityResult()`: handles returns from `downloaddata`, the cash/bank editor and the pager.
- `getLastLocation()`: reads the fused last-known location.
- `whiteNotificationBar()`: light status bar with a white background.

**Rules & edge cases.**
- **Rights:** Update and Delete come from the role's `AndroidMenus` row for `nav_loanscollection`, even in executive mode. Without a matching row, all rights are false. An executive whose role has Update rights can change rows that are already synced.
- **Penalty/savings-only entries are never saved.** The service, Finish and Save all insert only when Collected > 0, so a penalty-only or savings-only entry for an unpaid loan is fixed locally but never reaches the server. Negative penalty/savings values (`numberSigned`) do not count as "> 0", so a synced row zeroed except for a negative amount is neither updated nor deleted.
- **Role 3 cannot change existing rows.** In the save it only inserts new rows; in the adapter it may double-tap Due only on rows that are not fixed.
- **Opening the 9908 admin editor wipes the device's local working tables:** `MobileAppCollData`, `LastUpload`, `NewParties`, `NewMCLoans`, `ExpensesEntries` and the rest. Any in-progress online session or pending offline data on that device is lost.
- **Resumed sessions post with FinId 0.** When the screen is reopened on a session already in progress (a `LastUpload` row exists), `selectedcollection.FinId` is never set, so rows inserted at Finish by role ≠ 3 users get FinId 0. The voucher's cash ledger also falls back to `globalvars.cashbookledgerid` instead of the finance's cash ledger.
- **Visited checkbox likely crashes.** Toggling it (SSId 23) writes the nonexistent column `Visited`.
- **Double-tap locations are uploaded swapped.** Double-tap rows store lat/lng swapped, and the service uploads those values.
- **GPS is captured only if location permission was already granted.** The app never asks for it; without it lat/lng are sent as "0.0".
- **Flags are lost on resume.** IsMarked, IsVisited and istobeWarned come back false because Boolean 1/0 values are parsed with `parseBoolean` and `istobeWarned` is read from column index 21.
- **Direct Print fails on first use.** `totalpages` is still 0 unless a PDF was made earlier, so the print layout fails with "Page count is zero.".
- **Share does nothing.** It uses the hard-coded authority `net.venussolutions.venusmicrofindailyweeklycollection.provider`, but the manifest declares `….clone.provider`; the exception is swallowed. The PDF file is still written.
- **Page 1 can miss its area heading.** `LastSubAreaName` is not reset after page counting.
- **No safety checks around saving.** Finish has no confirmation, so it can mark an empty session "finished" and still send the SMS. Initialisation errors are swallowed. The 10-s service and Finish run without any lock between them, so a row could be inserted twice. Nothing on the client prevents a second master for the same date after Finish.
- **The HTTP SMS path cannot work** because its base URL is empty.

## Collection editor (basic) — `editing_collections_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) implementing `editing_collectionsadapter.AdapterListener`; theme `AppTheme.NoActionBar`, not exported. |
| **Layout(s)** | `activity_editingcollections.xml` (includes `customersearchlist.xml`); rows `tabbedcollectionlistrow.xml`; menu `menu_collectionediting` |
| **Opened from** | (1) `editing_collectionlist_act.onContactSelected` when SSId 61 is off: extras `selectedcollection`, `selectedfinanceobj` (`financemaster_data`), `calledfrom` 9908; forResult 101. (2) `selectedloancollectiondetails` (a single loan's collection history) when a collection row is tapped and editing is allowed there: extras `selectedcollection`, `selectedcollectiondate`, `receviedfinanceid`, `mcloanidcalledforcollection`, `selectedfinanceobj`, `calledfrom` 9908; forResult 101. |
| **Opens** | `collectionedit_individualcollection` (rc 2, row tap: cash/bank split editor); `partylistwithprevloansfornewloanselection` (sub-area header tap, `calledfrom` 9017); `downloaddata` (9907 path, never used); print, PDF and share intents. |
| **Data** | Remote load: procedure `MCLoanCollectionDisplay @GColId=<MCLGCollId>` on `branchlevelReport_branchconnString`. Remote save (`selbranchconnString`): `tblMCLoanGroupCollectionMaster`, `tblTransactionsMaster`, `tblTransactionsDetails`, `tblMCLoanDueCol`. Local: `SMSTemplates`, `SelectedBranchFinanceDetails`. Penalty and savings edits also update `MobileAppCollData` by MCLoanId, which has no effect unless the device holds a live session for the same loans. The dead `DisplayIndexData()` reads that table. |

**Purpose.** A lighter admin editor for one saved day. It lists only the loans that `MCLoanCollectionDisplay` returns for that group collection. The admin can change amounts (inline, or split cash/bank in the per-row editor), zero rows out to delete them, delete the whole day, or print or share the list. It cannot add a payment for a loan that has no entry.

**UI.** Same layout as the full-options screen, except the FAB is always hidden (`configureSaveButtonVisibility()` forces it off). Title "dd-MM-yyyy-Coll List". The loan passed as `mcloanidcalledforcollection` is flagged `iscalledforcorrection`, so its name shows in BlueViolet. There is no last-5 strip and no visit or double-tap handling: the callbacks are empty, but the adapter's text change still updates Collected.

**Menu / actions.**
- **Save Collections:** shown with Update rights → requestcode 101.
- **Delete Collections:** shown with Delete rights → same confirmation dialog as above → requestcode 102.
- **Print Share Options:** always shown.
- **Finish this Collection:** shown in online mode but has **no handler**, so it does nothing.
- **Search:** filters the list.
- **Back:** returns `selectedcollection` with resultCode = `requestcode`.

**How it works.**
1. **`onCreate`** runs helper methods in order: `applyEdgeToEdgeInsets`, `initializeWidgets`, `setupToolbar`, `handleExtras` (reads extras, opens SQLite, sets the title), `initializeAccessLimits` (`nav_loanscollection` rights), `configureSaveButtonVisibility`, `setupRecyclerView` (adapter with `showprevcollectiondetails` false), `addListeners` (back handling), `loadInitialData` (→ `AsyncDownloaddata`) and `settotalcollection`.
2. **AsyncDownloaddata** runs `MCLoanCollectionDisplay @GColId=…` and maps its columns:
   - MCLoanId, FWUDMCLoanid, PartyLedgerId, LDate→MCLoanDate, CWLedgerId, PartyName, SubAreaName/No, LAmount, DuesCount, LoanStatus
   - DueCollection→Collected, Balance, CurDue→InsAmount, MCLDueCollId, SavingsCollected, PenaltyCollected
   - Completed→PassedDues, Duescount→PaidDues, CashCollection, BankCollection
   - PrevCollected = LAmount − Balance

   Errors are not shown: no dialog, the text only flashes in the progress label.
3. **Inline edits.** `oncollectionamountchanged` sets Collected and Cash = Collected − Bank (memory only). Penalty and savings edits change memory and issue a no-op `MobileAppCollData` update. Row tap opens `collectionedit_individualcollection`; its result (rc 2, resultCode 1) replaces the row.
4. **Save (101)** on `selbranchconnString`:
   - Totals: Collected, Bank and Cash sums. The header object gets **Cash := Σ Bank and Bank := Σ Cash** (swapped), then CollAmount = Cash + Bank.
   - `update tblMCLoanGroupCollectionMaster set CollDate=?,CollAmount=?,CollStaffLedgerId=?,TransId=?,FinId=?,NIPAmountCollected=?,CollStaff2LedgerId=?,PenaltyCollected=?,SavingsCollected=?,BankCollection=?,CashCollection=? where MCLGCollId=?`. Because the getters are passed in reverse order, the swap cancels out and the stored cash/bank values are correct.
   - `update tblTransactionsMaster set TransAmount = CollAmount + Cash + Bank`, which is **double** the real total (bug).
   - Voucher rebuilt with `delete from tblTransactionsDetails where TransId=…`:
     - cash > 0: (114 'Source' Cr 1,2) and (`selectedfinanceobj.getCashLedgerId()` 'Target' Dr 2,1)
     - bank > 0: (114 Cr 3,4) and (`selectedfinanceobj.getDefaultBankledgersid()` Dr 4,3)
   - Per row:
     - `MCLDueCollId`≠0 and any amount > 0 → `update tblMCLoanDueCol set Paid=?,SavingsCollected=?,PenaltyCollected=?,CashCollection=?,BankCollection=? where MCLoanId=? and GColId=?`
     - `MCLDueCollId`≠0 and all = 0 → `delete from tblMCLoanDueCol where MCLoanId=? and GColId=?` (JADX shows `getCollected()==null` here; smali shows `!=null`).
     - No insert path; no role-3 rule.
5. **Delete (102)** runs the same four deletes as the full-options screen.
6. **onPostExecute:** in 9908, 101 → `Update_sendsms` (template 11, `{Finance}` = `selectedfinanceobj.getFinMailingName()`) and 102 → `delete_sendsms` (template 10, `{Finance}` = `fetchfinanceName(receviedfinanceid)`). `receviedfinanceid` is never assigned (0), so the finance name is wrong or blank. Then the "Info" dialog with "Successfully Updated" or "Successfully Deleted" → `setResult(requestcode, selectedcollection)` and finish.
   - SMS delivery: `smsmethod` 1 uses `SmsSender.sendMultipartSMS` (splits the numbers by comma; opens the SMS app if SEND_SMS is denied). Method 2 is HTTP POST to an empty URL with empty sender and key, so it is broken.
7. **Print, PDF and Share**
   - Pages are counted up front: 45 lines per page, area headings included.
   - Pages are 800×1140 for print, PDF and share.
   - Header: "Page n" (no total), "FINANCE : " + `globalvars.financename` (the current line, not necessarily the edited finance).
   - Columns: No, Party Name, LAmount, Due, Status, Paid = LAmount − Balance − Collected, Balance, Coll.
   - "Printed @" appears only on full pages. File names and intents are as above; Share has the same wrong FileProvider authority.

**Functions.**
- `onCreate()`: runs the helper methods listed in step 1.
- `DisplayIndexData()`: local-cache rebuild for 9907. Unreachable: `handleExtras` resets `isLastUpdateFound` and no caller passes 9907.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: menu visibility and actions.
- `printDocument()`, `MyPrintDocumentAdapter`, `sharePdf()`, `createPdfA4()`, `drawPage_method01()`, `drawCenter()`, `drawCenterwithgivenwidth()`: printing and PDFs.
- `oncollectionamountchanged()`, `onpenaltyamountchange()`, `onsavingsamountchange()`: inline edits. `onvisitstatuschanged()` and `ondueamounttabbed()` are empty.
- `onrowselected()`: opens the cash/bank editor. `onsubareanameclicked()`: opens the party list. `oncollectionboxlostfocus()`: refreshes the adapter.
- `settotalcollection()`: footer totals and the header object's cash/bank totals.
- `AsyncDownloaddata`, `ASYNCSavingCollections`, `Update_sendsms`, `delete_sendsms`: load, save/delete and SMS.
- `onActivityResult()`: rc 1000 reload (dead path), rc 2 row replace, otherwise refresh.

**Rules & edge cases.**
- Load and save use different connections: the branch-report connection for reading, the selected-branch connection for writing.
- Tapping a sub-area header always opens the new-loan party list, even when editing an old day. That list reads the device's local `Parties` table.
- No GPS capture and no access check against `FinanceAccessAllowed`.

## Loans Collection register — `editing_collectionlist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) implementing `collectionlist_adapter.ContactsAdapterListener`; theme `AppTheme.NoActionBar`, not exported. |
| **Layout(s)** | `activity_newloanscollectionlist.xml` (includes `collectiondetails_row.xml` as the column header and `customersearchlist.xml`); rows `collectiondetails_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | `selectbranchandfinance` with requestcode 115. Reached from the drawer *Admin → Loans Collection* (`nav_loanscollection`; Tamil "கடன் வசூல் விவரம்"). Extras `selectedfinanceobj`, `calledfrom` 9908. |
| **Opens** | `editing_collectionswithfulloptions_act` (SSId 61 on) or `editing_collections_act` (off), both with `calledfrom` 9908 and forResult 101; DatePickerDialogs. |
| **Data** | Remote (`branchlevelReport_branchconnString`): `Select MCLGCollId, FWGColId, CollDate, CollAmount, CollStaffLedgerId, TransId, FinId, NIPAmountCollected, CollStaff2LedgerId, PenaltyCollected, SavingsCollected from tblMCLoanGroupCollectionMaster where CollDate>='<from>' and CollDate<='<to>' and finId=<FinId> order by CollDate desc`. No local writes. |

**Purpose.** Lets an admin browse one finance line's daily group collections between two dates and open a day to edit or delete it.

**UI.**
- Title: "<FinMailingName>-Coll List".
- Date bar: "From :" `loanlist_fromdateid` and "To :" `loanlist_todateid` (bold chocolate text, `dd-MM-yyyy`; default one month ago → today). Tapping a date opens a DatePickerDialog. The refresh ImageButton `loanslistrefreshid` reloads the list.
- Column header (included `collectiondetails_row`): S.No / Date / Collection / Penalty / Savings. Penalty and Savings show only if SSId 45 / 46 are on.
- List of `collectionlist_adapter` rows.
- Progress dialog "downloading, please wait ..." / "Loading Loans Collection".

**Menu / actions.** Search only (`menuonlywithsearchoption`, always shown). Digits match the S.No (FWGColId) exactly; any other text matches part of the date in `yyyy-MM-dd` form. Back closes the search box first.

**How it works.**
1. `onCreate`: reads `selectedfinanceobj`, sets the default date range, applies SSId 45/46 visibility to the header, loads `nav_loanscollection` rights and runs `ASYNCCollections`.
2. `ASYNCCollections` runs the query above. On error the "Error Info" dialog closes the screen.
3. `onContactSelected`: needs View rights. With SSId 61 it opens the full-options editor (extras `selectedcollection`, `selectedcollectiondate`, `receviedfinanceid`, `receivedfinancename`); otherwise the basic editor (extras `selectedcollection`, `selectedfinanceobj`).
4. `onActivityResult` (rc 101): resultCode 101 replaces the row (matched by MCLGCollId) with the returned totals; resultCode 102 removes it; then the list refreshes.

**Functions.**
- `onCreate()`: setup and first load.
- `lambda$onCreate$1/$2/$4/$5`: From/To date pickers. `lambda$onCreate$3`: refresh.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: SearchView and Home.
- `onContactSelected()`: chooses which editor to open.
- `ASYNCCollections`: loads the list.
- `onActivityResult()`: updates or removes the edited row.
- `whiteNotificationBar()`: light status bar.

**Rules & edge cases.**
- Only View rights are checked here; Update and Delete are checked inside the editors.
- `selectedcollectionday_weekdayid` is computed and never used.
- The list query does not load Cash or Bank; the editors recompute them on save.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `editing_collectionsadapter` | RecyclerView adapter + Filterable; inner `MyViewHolder`; interface `AdapterListener` (8 callbacks) | `editing_collectionswithfulloptions_act`, `editing_collections_act` | **Row layout:** `tabbedcollectionlistrow`. Constructor flag `showprevcollectiondetails` = (calledfrom 9907). **Area header:** DarkBlue sub-area heading on the first row of each SubAreaNo; tap → `onsubareanameclicked`. **Party line:** party no = CWLedgerId (SSId 20), then name. **Name colours:** #061DED if IsMarked/attention; BlueViolet if `iscalledforcorrection`; #DC0000 if NIP; #FF4E0C if `istobeWarned`; #0D54E3 if Remarks "00000" (new loan; amount box disabled); otherwise black. **Amounts line:** Loan (LAmount); "Coll" = PrevCollected (special access 3); Balance (special access 5); Status (special access 4: LoanStatus if `loanstatusmethod_10` (SSId 34)=1, else PaidDues/PassedDues); visited checkbox (SSId 23). **Amount EditText:** shows Collected when > 0; enabled if Update right, full access, or (MCLDueCollId=0 and not fixed). **Amount background:** #C0C0C0 when entered but not fixed (and while typing in 9907); #F08080 when fixed but not yet synced; otherwise white. **Right column:** loan method (SSId 41) and Due (InsAmount). Double-tap on Due fills the amount and calls `ondueamounttabbed` when (MCLDueCollId=0 and not fixed) or role≠3. **Last-5 strip** (9907): five date (dd-MM-yy) / amount cells from local `PrevCollections where MCLoanid=… order by RowNo desc`; one query per bind. **Savings / Penalty boxes:** shown with SSId 46 / 45. **TextWatchers** on collected, penalty and savings call `oncollectionamountchanged` / `onpenaltyamountchange` / `onsavingsamountchange` with `parseAmount()` (blank or invalid → 0). They are muted while binding through `globalvars.iscollectionamountisbeingdisplayed`. **Other callbacks:** checkbox → `onvisitstatuschanged`; row tap → `onrowselected`; `oncollectionboxlostfocus` is declared but never fired. **Filter:** sub-area or party name (case-insensitive substring) or exact CWLedgerId. **Side effects:** forces `selectedfinance_showlast5dayscollectioninareawisecollectionlist=true`; the party image is always hidden. |
| `collectionlist_adapter` | RecyclerView adapter + Filterable; interface `ContactsAdapterListener` | `editing_collectionlist_act` | **Row layout:** `collectiondetails_row`. **Columns:** S.No = FWGColId; Date `dd-MM-yy`; Collection = CollAmount; Penalty / Savings (SSId 45 / 46). **Click:** row tap → `onContactSelected(item, position)`. **Filter:** digits match FWGColId exactly; other text is a substring match on `CollDate.toString()`. |
| `groupcollectionslist_data` | Serializable model | the three screens; also `MyService_updatecollection` and `selectedloancollectiondetails` | **Purpose:** one `tblMCLoanGroupCollectionMaster` row (one day's collection for a line). **Fields:** int MCLGCollId, FWGColId, CollAmount, CollStaffLedgerId, CollStaff2LedgerId, TransId, FinId, NIPAmountCollected, PenaltyCollected, SavingsCollected, CashCollection, BankCollection; `java.sql.Date` CollDate. Getters and setters only. **Use:** passed and returned as the `selectedcollection` extra. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_editingfullcollections.xml` | `editing_collectionswithfulloptions_act` | **Root:** CoordinatorLayout `main`; AppBar with Toolbar `toolbar`. **List:** `customersearchlist` (RecyclerView `recycler_view`), margins 60dip top / 50sp bottom. **Bottom bar** (DarkBlue #00008B): `lastupdatelbl` (white 18sp), "Total:", `editingcollection_totalid`, plus `editingcollection_penaltytotalid` and `editingcollection_savingstotalid` (placeholder "1,00,000", hidden). **FAB** `editingcollection_updatecollection` (ic_menu_save; visible in XML, hidden in code unless online 9907). **Progress:** ProgressBar `collectionentry_pbarid` and `collectionentry_progresstextid` (bold white on DarkGreen), both hidden. |
| `activity_editingcollections.xml` | `editing_collections_act` | Same structure. The label reads "Total: ", the FAB is hidden (also forced hidden in code), and the progress text is unstyled. |
| `activity_newloanscollectionlist.xml` | `editing_collectionlist_act` | **Root:** CoordinatorLayout `main`; Toolbar. **Date bar** `lytdaterangeid`: "From" ": " `loanlist_fromdateid`, "To" ": " `loanlist_todateid` (Chocolate #D2691E, bold), ImageButton `loanslistrefreshid` (ic_action_refresh). **Body:** grey 2dip dividers; included `collectiondetails_row` as the column header; included `customersearchlist` RecyclerView. |
| `collectiondetails_row.xml` | `collectionlist_adapter` rows; header in `activity_newloanscollectionlist` | Clickable RelativeLayout → LinearLayout `row3`: `collectionlist_snoid` "S.No" (`@string/s_no`), `collectionlist_transdateid` "Date", `collectionlist_collectedamountid` "Collection" (`@string/collection`, Tamil "collection"), `collectionlist_penaltycollectedamountid` "Penalty", `collectionlist_Savingscollectedamountid` "Savings". |
| `tabbedcollectionlistrow.xml` | `editing_collectionsadapter` | **Header:** `tcl_subareanameid` sub-area heading (white bold 16sp on DarkBlue, hidden). **Main block** `tcl_collectiondetailslnrlayout`: thumbnail `collectionlistrow_thumbnail`; `tcl_partynoid` (red bold) + `tcl_partynoseparaterid` ":" + `tcl_partynameid` (`@string/party_name12` "Party name"); `tcl_loanamountId` "Loan", `tcl_collectedamtId` "Coll" (DarkGreen), `tcl_balanceId` "bal" (DarkRed), `tcl_statusId` "Stas" (#DD2C00), CheckBox `tcl_isvisitedid`, EditText `tcl_collectedamountId` (number, maxLength 6, tooltip "Collected"); right column `tcl_loanmethodid` "100d" (Indigo) and `tcl_dueamountId` "DUE" (SeaGreen 18sp). **Last-5 strip** `tcl_prevcollectiondetilslnrlayout`: five cells, each a `tcl_{first..fifth}daylabelid` header (white on DarkGoldenrod) over a `tcl_{first..fifth}dayvalueid` value (DarkBlue). **Savings box** `tcl_savingslnrlayout`: "Savings" (`@string/savings1`) + EditText `tcl_savingsetid` (numberSigned, maxLength 5). **Penalty box** `tcl_penaltylnrlayout`: "Penalty" (`@string/penalty1`) + EditText `tcl_penaltyetid` (numberSigned, maxLength 5). Both boxes are hidden by default. |

Menus used by this module: `menu_collectionediting` contains `action_search` "Search" (SearchView, ifRoom), `collectionediting_save` "Save Collections", `collectionediting_delete` "Delete Collections", `collectionediting_finish` "Finish this Collection" and `action_printandshareCollectionlist` "Print Share Options"; the Tamil strings are the same English text. `menuonlywithsearchoption` contains only `action_search` (always shown).

---
[← brain.md](../../brain.md) · Module 8.5 of 20 · Previous: [8.4 Offline collection entry (area-wise collection list & individual collection)](04-offline-collection-entry-area-wise-collection.md) · Next: [8.6 Collection reports (collection for a day, abstract, 7-days, unpaid parties, day-wise pending, periodic loans)](06-collection-reports-collection-day-abstract-7.md)
