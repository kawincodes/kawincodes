# 8.3 Offline mode: download, upload and sync services

[← brain.md](../../brain.md) · Module 8.3 of 20 · Previous: [8.2 Navigation hub, routers and line/branch selection](02-navigation-hub-routers-line-branch-selection.md) · Next: [8.4 Offline collection entry (area-wise collection list & individual collection)](04-offline-collection-entry-area-wise-collection.md)

This module is the backbone of the **offline executive workflow** (`softsettingsvalue.vss_executivehandlingmethod` = 1). **Download Data** (`downloaddata`) wipes the phone's working tables and pulls one line's collection list, parties, sub-areas, ledgers, loan methods, user rights and collection history for a chosen date and pair of collection executives into SQLite; the executive then works offline in other screens (collections, new parties/loans, expenses/incomes) on those tables. **Upload Data** (`uploadcollection`) shows a proposed cash book, optionally uploads and e-mails a PDF day report, then writes new parties, new loans, collections and expenses to SQL Server in four independent, re-runnable phases and SMS-alerts the admins. Administrators open or close upload/download per line and date in `uploaddownloadcontroling`. `MyService_updatecollection` is the ONLINE-mode background pusher of individually confirmed collections, `MyIntentService` is an unused template, and two small viewers list today's offline collections and an archived (closed) loan.

## Download Data — `downloaddata`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`, implements `View.OnClickListener`); manifest label "Download Collection List " (default theme with action bar) |
| **Layout(s)** | `activity_downloaddata.xml`; executive pickers via `spinnersearch_list`; `DatePickerDialog`; horizontal `ProgressDialog` |
| **Opened from** | Drawer → Executive Entries (offline) → **Download Data** (`nav_import`) with `calledfrom`=102 — only when a line is selected (`globalvars.finid`>0), else toast "Please Select Line First". Also `editing_collections_act.loadInitialData()` and `editing_collectionswithfulloptions_act.onCreate()` with `calledfrom`=101 (`startActivityForResult`, rc 1000) when their `calledfrom`==9907 and no last upload is known (`!globalvars.isLastUpdateFound`) — there it is only an executive/date picker. |
| **Opens** | `spinnersearch_list` (rc 2001 executive-1, then automatically rc 2002 executive-2; extras `title`, `searchlist`). For `calledfrom`=101 it returns `setResult(1)` with extras `CollectionDate` (java.sql.Date), `currentcollectionexecutivelederid1`, `currentcollectionexecutivelederid2`. |
| **Data** | SQLite: reads `CollectionExecutives`, `FinanceDetails` (`getfinanceobj`), `SMSTemplates` (STId 1/4); drops/clears and refills `MobileAppCollData`, `Parties`, `SubAreaDetails`, `LoanMethods`, `ExpHeaders`, `PrevCollections`, `loanindex`, inventory `stockgrp`/`stockcat`/`stockUOM`/`ItemMaster`; clears `LastUpload`, `NewParties`, `NewMCLoans`, `NewItems`, `ExpensesEntries` (dropped + recreated), `RelatedDocCollection`; creates `StaffLocationQueue`; appends `SpecialAccessAllowed`, `tblSalesDetailsforAndroid` · Server (`selbranchconnString`): `FinanceAccessAllowed` ⟕ `DownloadControling`, `MobileDevlicesAllowed`, `softsettings` (SSId 32), `FinanceMaster` (+ function `dbo.showLedgerBalance`), `SpecialAccessAllowed`, `AndroidMenus` ⟕ `tblSalesDetailsforAndroid`, `LoanMethods`, `financemaster`⋈`LoanMethods`, `subareadetails`, `tblLedgers`, `PattiExpLedgers`, `PattiIncomeLedgers`, `stockgrp`, `stockcat`, `stockUOM`, `ItemMaster` · procs `APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport`, `APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport`, `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport`, `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport`, `Get_PartiesListforweeklyline`, `Get_PartiesListforweeklylineonlyactiveparties`, `PrepareLast5CollectionsforPrev5dates`, `PrepareLast5CollectionsforPrev5weeks`, `PrepareLast5Collections`, `PrepareLast5Collectionsforweeklyline_withautoloanclosure`, `Get_LoanindexwiseLoans`, `Get_LoanindexwiseLoansforweeklyline` · prefs read `FinId`, `FinanceName`, `CashBookLedgerId`, `bankaccountledgerid`, `MobileUserLedgerId`, `appusername`, `currentcollectionexecutivename`, `LastDownloadDatetime`, `AdminMobileNos`; written `currentweekdayid`, `CollectionDate`, `isoverallcollectionenabled`, `currentcollectionexecutivelederid1/2`, `openingbalancedownloaded`, `app_userroleid`, `newmcloanid`, `newpartyledgerid`, `LastDownloadDatetime` |

**Purpose.** Start of the executive's day: choose the two collection executives and the collection date for the selected line, then download that day's collection list plus all reference data the offline screens need. Also reused (calledfrom 101) by the online collection-entry screens just to choose executives and date.

**UI.**
- Line name `tvRouteNameid` ("Line Name") = `globalvars.financename`.
- "Collection Executive-1" / "Collection Executive-2" (வசூலுக்கு செல்பவர்-1 / -2): non-editable EditTexts `downloaddata_etexecutive1id` / `downloaddata_etexecutive2id`; tap opens a searchable picker.
- "Collection Date" (வசூல் தேதி) `dcl_collectiondateid` (dd-MM-yyyy, tap → date picker) and weekday `dcl_collectiondayid` (localized name, e.g. புதன்).
- CheckBox "Overall Collection List" `dcl_enableoverallcollectionlistid` — visible only when `allowoveralllistdownload_4` (SSId 30).
- Button "Download" `btnDownload`.

**Menu / actions.** No options menu. Taps on the two executive fields, the date, and the Download button.

**How it works.**
1. `onCreate`: reads `calledfrom`; default date = today (`globalvars.collectiondate`); `globalvars.currentweekdayid` = Calendar DAY_OF_WEEK (1 = Sunday) saved to pref `currentweekdayid`; both executive ids reset to 0.
2. Executive pickers: list from local `CollectionExecutives` `order by LedgerName` (`where FinId=<line>` when `enablelinewisestaff`, SSId 55; `LedgerId not in(<other executive>)` when `strictontwocollectionexecutives_3`, SSId 31) → `spinnersearch_list`, result code 1 with `displaymember`/`valuemember`. Picking executive-1 auto-opens the executive-2 picker (title "வசூலுக்கு செல்பவர்-2").
3. Date picker: unless special access 6 (unconditional download) or `isFullAccessAllowed`, max = today and min = today − (`noofbackdaysallowedfordownloaddata_2` (SSId 58) − 1) days. The chosen date sets `globalvars.collectiondate` and the weekday label.
4. Download: saves prefs `currentweekdayid`, `CollectionDate` (yyyy-MM-dd), `isoverallcollectionenabled`. `SyncWithServer()` requires both executives ≠ 0 (alert "Collection Executives not selected" / வசூலுக்கு செல்பவர்கள் தேர்ந்தெடுக்கப்படவில்லை, title "Attention Please…" / தங்கள் கவனத்திற்கு…), reloads line/ledger globals from prefs, saves the executives and the current `openingbalancedownloaded`; `calledfrom` 102 → `AsyncDownloaddata`, 101 → returns the selection and finishes.
5. `onPreExecute` — **local reset before any server check**: `isLastUpdateFound=false`; ensures `LastUpload`, `NewMCLoans`, `NewParties`, `NewItems`, `ExpHeaders` exist; `drop table MobileAppCollData` + recreate; `delete from` `NewItems`, `LastUpload`, `NewParties`, `NewMCLoans`; drops and recreates `ExpensesEntries(ID autoincrement, FinId, LedgerId, Amount, ServerGenTransId, TTId, SourceLedgerId, Narration)`; creates `StaffLocationQueue(QId, FinId, StaffLedgerId, TrackDate, Lat, Lng, Accuracy, Speed, BatteryLevel, IsMockLocation, DeviceTime)`; deletes `Parties`, `ExpHeaders`, `PrevCollections`, `SubAreaDetails`, `RelatedDocCollection`. Shows "downloading, please wait ..." (0–100).
6. Access check: `select top 1 x.IMEI, convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(),'+05:30'),0) as DownloadTime, case when (select bitvalue from softsettings where SSId=32)='True' and isAllowed='True' then 'True' when (…SSId=32)='False' then 'True' else 'False' end as IsAllowed from FinanceAccessAllowed x left join DownloadControling z on x.FinId=z.FinId and TransDate='<date>', MobileDevlicesAllowed y where x.IMEI=y.IMEI and x.FinId=<line> and y.isActive=1 and x.IMEI='<ANDROID_ID>'`. A row ⇒ active user; `IsAllowed` ⇒ download allowed (forced true for full access); `DownloadTime` (server IST) ⇒ `LastDownloadDatetime`. Server SSId 32 is the same setting as `enablemoniterondownloadandupload_1`.
7. Subscription: `FinanceMaster.ExpiryDate`/`RenewalNoticeDate` of the line — collection date after expiry ⇒ expired (nothing more is downloaded); after the notice date ⇒ warning at the end.
8. Rights refresh: `MobileDevlicesAllowed.userroleid` ⇒ `globalvars.app_userroleid` (pref); server `SpecialAccessAllowed` rows of the role ⇒ local table ⇒ `globalvars.specialaccessrules` (SARId 1–13 and 20); `AndroidMenus a left join tblSalesDetailsforAndroid b … where a.isAllowed=1 and (isonlyformasterdb='FALSE' or =<ismasterdb>)` ⇒ local `tblSalesDetailsforAndroid` (Show/View/Update/Add/DeleteState, mnustyle); local `LoanMethods` replaced by all server loan methods (20 columns incl. CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, WarningDays, DueSupportAmount).
9. Weekly line: the line's default loan method (`financemaster.defaultloanmethodid`) has `CollectionBreakString` "W" or "ww" ⇒ `globalvars.isselectedlineisweeklyline`.
10. Only if allowed and not expired:
    - Sub areas `subareadetails where IsVisible=1 and FinId=<line>` (+ `and WeekDayId=<weekday>` on weekly lines) ⇒ `SubAreaDetails`; the first SubAreaNo is remembered.
    - Collection list `exec <proc> @ColDate='<yyyy-MM-dd>', @FinId=<line>`: loan-closure monitoring (`enablemoniteringonloanclosoure_5`, SSId 14) on ⇒ `APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport` (weekly) / `…_withoutWeekdayssupport` (daily); off ⇒ `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport` / `…_WithOUTWeekdaysSupport`. Each row ⇒ `MobileAppCollData` (MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, PrevCollected, interestamount, LoanIndex, Noofloansallowed, Doccharges, IsAttentionRequired) with Collected/PenaltyCollected/SavingsCollected = 0, IsMarked/IsVisited = false, TargetLedgerId = cash-book ledger. "Overall Collection List" ticked ⇒ every row gets the first SubAreaNo and that sub area is renamed "Over all List".
    - Opening balance: unless `showopeningBalASZeroforexecutive` (SSId 57), `select dbo.showLedgerBalance(CashLedgerId,FinId) as openingbalance from FinanceMaster where finid=<line> and IsVisible='True'` ⇒ `globalvars.openingbalancedownloaded` (pref); else 0.
    - Parties ⇒ `Parties`: daily line `tblLedgers where ScreenType=3 and SubAreaNo in (sub areas of the line) order by SubAreaNo, UDLedgerId` (adds `isactive=1` unless full access or special access 11); weekly line `Get_PartiesListforweeklyline` (all) / `Get_PartiesListforweeklylineonlyactiveparties` (same rule).
    - `ExpHeaders`: ledgers in `PattiExpLedgers` (TTId 7, expenses) and in `PattiIncomeLedgers` (TTId 8, incomes).
    - `PrevCollections(RowNo, MCLoanId, CollDate, Paid)` for the last-5-collections display: `fivedayscollectiondetailsmethod_8` (SSId 47) = 1 ⇒ `PrepareLast5CollectionsforPrev5dates` / `PrepareLast5CollectionsforPrev5weeks`; = 2 ⇒ `PrepareLast5Collections` / `PrepareLast5Collectionsforweeklyline_withautoloanclosure` (daily / weekly).
    - `loanindex(LedgerId, LoanIndex, MCLoanId, MCLoanAmountCollectable, collected, Balance)` from `Get_LoanindexwiseLoans @Finid` / `Get_LoanindexwiseLoansforweeklyline @ColDate, @FinId`.
    - If the line's `FinanceDetails` enables inventory: `stockgrp`, `stockcat`, `stockUOM`, `ItemMaster` copied (and `StockUOM`, the same SQLite table, again).
11. `onPostExecute`: error ⇒ "Error Info..." dialog with the message (OK finishes); not active / not allowed ⇒ "ACCESS Denied"; notice period ⇒ "Subscription will be expired on dd-MM-yyyy\nKindly pay the renewal charges" (title "தங்கள் கவனத்திற்கு..."), OK completes normally; expired ⇒ " Subscription expired \n Kindly pay the renewal charges". Success: `newmcloanid`/`newpartyledgerid` reset to 0, `LastDownloadDatetime` saved, activity closed, toast "Download Completed".
12. Download SMS (`ASYNCSendSMS`) when `isSendSMSonDataDownload_32` (SSId 5) and `AdminMobileNos` is longer than 9 characters: template STId 4 if `vss_IsthisDBunderBranchManagement` else 1; `{BranchName}` → `vss_branchname`, `{LineName}` → `smsidentifier` + "Dt: dd-MM-yyyy" + "@ dd-MM-yy hh:mm aa", `{executive1}`, `{executive2}`, `{Opening Balance}` → "Balance Carried: Rs.<opening balance>"; sent with `SmsSender.sendMultipartSMS` only when `smsmethod` (SSId 50) = 1; toast "SMS Sent" either way.

**Functions.**
- `onCreate()` – widgets, default date/weekday, overall-list visibility, listeners.
- `lambda$onCreate$0()` – Download click: save prefs, `SyncWithServer()`.
- `lambda$onCreate$1()` / `lambda$onCreate$2()` – build executive list, open picker 2001 / 2002.
- `onActivityResult()` – 2001: set executive-1 and open executive-2 picker; 2002: set executive-2.
- `onClick()` / `lambda$onClick$3()` – date picker with back-day limits; sets date and weekday.
- `SyncWithServer()` – validate executives, persist, branch on `calledfrom`.
- `AsyncDownloaddata.onPreExecute()` / `doInBackground()` / `onProgressUpdate()` / `onPostExecute()` (+ `lambda$onPostExecute$0..3` dialog handlers) – local reset, server download, progress, result dialogs.
- `ASYNCSendSMS.doInBackground()` / `onPostExecute()` – download alert SMS.
- `loadcollectiondayslist()` – fills an unused `MonthDaysList` with "1".."28".

**Rules & edge cases.**
- Pending offline work is destroyed on every Download tap: `MobileAppCollData`, `NewParties`, `NewMCLoans`, `ExpensesEntries`, `LastUpload` are wiped in `onPreExecute`, before the access/subscription checks, so a denied, expired or failed download also loses un-uploaded entries.
- Download gating: device active in `MobileDevlicesAllowed` and listed in `FinanceAccessAllowed` for the line; when server SSId 32 is True the `DownloadControling` row for (line, date) must have `isAllowed='True'` (maintained in `uploaddownloadcontroling`). Full access skips only the date control; special access 6 only lifts the date-picker limits. When download is not allowed, rights and loan methods are still refreshed but no list is loaded.
- Special-access and menu rows are appended without deleting old rows (duplicates accumulate until `syncresources` clears them); `globalvars.salesdetailsforandroid_datalist` is reset to an empty list and not refilled here, so screens that look up menu rights in it (e.g. `uploaddownloadcontroling`) can hit a null until the drawer reloads.
- After a connection failure the "Connection Error,Please try again..." text is usually overwritten by the follow-up NullPointerException message (the second block still runs with a null statement).
- `onProgressUpdate` expects (message, percent); one-value calls ("Getting Opening Balance", "Done") only change the text. The decompiled `ExifInterface.LONGITUDE_WEST` is simply the constant "W".
- Prev-5 method values other than 1/2 import nothing. `CollectionExecutives` and `prevlocations` are not refreshed here (filled by sync / collection screens).

## Upload Data — `uploadcollection`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`), portrait, `AppTheme.NoActionBar`, EdgeToEdge; label `@string/title_activity_uploadcollection` (placeholder text "uploadcollection"). JADX mis-decompiled `onCreate`, `ASYNCUpload.doInBackground` and `onPostExecute` (several conditions inverted) — the logic below was checked against smali. |
| **Layout(s)** | `activity_uploadcollection.xml` (+ rows `proposedcashbookrow.xml` via `proposedcashbook_adapter`) |
| **Opened from** | Drawer → Executive Entries (offline) → **Upload Data** (`nav_UploadData`), no extras |
| **Opens** | `todaycollectionlist` (no extras) from the success dialog button "Go to Today Collection"; dead code for the Android print framework (`printDocument()`) |
| **Data** | SQLite: `MobileAppCollData`, `NewMCLoans`, `NewParties`, `Parties`, `SubAreaDetails`, `ExpensesEntries`, `ExpHeaders`, `NewItems`, `RelatedDocCollection`, `LastUpload` (r/w), `CollectionExecutives`, `FinanceDetails` (EmailIdentifier), `SMSTemplates` (STId 2/5) · Server: `FinanceAccessAllowed`, `MobileDevlicesAllowed`, `UploadControling`, `softsettings` (SSId 32), `tblLedgers`, `tblMCLoans`, `tblTransactionsMaster`, `tblTransactionsDetails`, `InvTransDetails`, `RelatedDocCollection`, `tblMCLoanGroupCollectionMaster`, `tblMCLoanDueCol`, `PartyVisitStatus` · procs `checkisanyoneofcashandorbooksverified`, `InsertNewParties`, `InsertNewPartieswithautocwledgerid`, `InserttblTransactionsMaster` (12- and 13-parameter forms), `InserttblMCLoanswithWeekdayId`, `InserttblMCLoanGroupCollectionMasterWithTWOStaffs`, `UpdatetblTransactionsMaster` · HTTP (OkHttp): soft settings `daily_report_file_upload_url` (SSId 26), `email_sending_url` (SSId 27) · prefs read `FinId`, `isinventoryenabled`, `FinanceName`, `CashBookLedgerId`, `bankaccountledgerid`, `MobileUserLedgerId`, `currentcollectionexecutivelederid1/2`, `openingbalancedownloaded`, `appusername`, `currentcollectionexecutivename`, `LastDownloadDatetime`, `AdminMobileNos`, `CollectionDate`, `daily_report_file_upload_url`, `email_sending_url` |

**Purpose.** End of the executive's day: review the proposed cash book for the downloaded line/date, then press **Update** to push everything captured offline to the server — new parties, new loans (with products, documents and on-day collection), all loan collections (cash/bank, penalty, savings, visit status) and expenses/incomes — after optionally uploading and e-mailing a PDF day report to the administrator; admins then get an upload SMS.

**UI.**
- Heading "Upload Collection Details ", line name `tvCollectionRoute`, collection date `ucl_collectiondateid` (dd-MM-yyyy).
- "Collection Executive-1 / -2" (வசூலுக்கு செல்பவர்-1 / -2) names `ucl_collectionstaff1id` / `ucl_collectionstaff2id` (looked up in `CollectionExecutives`).
- "Estimaated Thittam" (உத்தேச திட்டம்) – RecyclerView `recycler_view` = proposed cash book (Particulars / Debit / Credit).
- "Cash Collection:" `tvcashcollection`, "Bank Collection:" `tvbankcollection` = Σ `CashCollection` / Σ `BankCollection` in `MobileAppCollData`.
- "Abstract" (சுருக்கம்): `tvNewLoanAmount` "New Loans :<Σ NewMCLoans.MCLoanAmountCollectable>, Expenses : <Σ ttid 7>, Receipts: <Σ ttid 8>"; `collectedamouttv` "Collected Amount :<cash + bank + Σ NewMCLoans.ondaycollection>".
- `tvlastsubmitdate` ("not submited yet") / `tvlastsubmitamount` ("Prev Collection Upload Amount:-" / முன்னதாக அப்லோட் செய்யப்பட்ட வசூல் தொகை:-) become "Last Submited At :<server IST time>" / "Last Submited Amount :<amount>" once a `LastUpload` row exists.
- CheckBox "Send EMail to Administrator" `ucl_mailsendcheckbox` (ticked by default, editable only when `isFullAccessAllowed`, unticked if there is no reporting e-mail) and `mailaddressid` (reporting e-mail, SSId 15, or "Reporting EMail Not found"; later the mail server reply + PDF name).
- Button "Update" `btnsavecollection`; hidden `fileupload_progressBar` + `fileupload_txtPercentage` ("Report file Uploading..N%", "Sending Mail").

**Menu / actions.** No options menu. "Update" → conflict check → upload chain. Result dialog buttons "OK" (finish) and "Go to Today Collection".

**How it works.**
1. `onCreate`: prefs → globals (see Data; `CollectionDate` → `globalvars.collectiondate`); the four statuses `newparties/newloans/collections/expenses_uploadstatus` start as "Success".
2. Proposed cash book rows in order: **Opening Balance** (தொடக்க இருப்புத் தொகை, Dr `openingbalancedownloaded`); **Line Collection** (லைன் வசூல், Dr `select sum(Collected) from MobileAppCollData where MCLoanId not in (select ServerGenMCLoanId from NewMCLoans)`, only if > 0); **Savings Collection** (சேமிப்பு திட்ட வசூல், Dr, if `issavingscollectionallowedincollectionscreen_13` and > 0); **Penalty/Interest Collection** (தாமதக்கட்டணம்/வட்டி வசூல், Dr, if `ispenaltyallowedincollectionscreen_12` and > 0); one **Cr** row per expense (`ExpensesEntries ⋈ ExpHeaders`, ttid 7; text = Narration, else ledger name); one **Dr** row per income (ttid 8); per new loan — first new-party loans (`NewMCLoans ⋈ NewParties ⋈ SubAreaDetails`), then existing-party loans (`⋈ Parties`) — "<party>, <area>" Cr = MCLoanAmountCollectable, **Interest** (ஆதாய வரவு) Dr InterestAmount, **Doc Charges** (மகிமை) Dr DocCharges, **Onday Collection** (வசூல்) Dr ondaycollection; **Closing Balance** (நிறைவு இருப்புத் தொகை) = ΣDr − ΣCr, shown in the Debit column when ≥ 0, otherwise the shortfall in the Credit column (JADX shows these branches swapped; smali is authoritative).
3. Totals as in UI; `ShowLastUpdate()` reads `LastUpload(SubmitAmount, SubmitedAt, MCLGCollId, TransId)` ⇒ fields `MCLGCollId`, `CollTransId` and `globalvars.isLastUpdateFound`.
4. **Update**: `select PartyName, a.LoanIndex from NewMCLoans a, MobileAppCollData b, Parties c where a.PartyLedgerId=b.PartyLedgerId and a.PartyLedgerId=c.LedgerId and a.LoanIndex=b.LoanIndex and b.MCLoanAmountCollectable-PrevCollected-Collected>0` — any row (a new loan reusing the loan index of a still-unpaid loan of the same party) ⇒ alert "Loan Conflicts": "A New Loan is Conflict with Exsisting Loan / Party:… / Loan Index:…" and stop. A query exception ⇒ alert "<message>ONClick-Press OK to Continue" ("Unhandled Error") and continue anyway.
5. `SyncWithServer()`: mail ticked and `reportingemailaddress` not null ⇒ `createPdf()` → `ASYNCUploadDailyReportFile` → `ASYNCsendmailtoadmins` → `ASYNCUpload`; otherwise `ASYNCUpload` directly.
6. **Day-report PDF** (`createPdf`): A4 595×842 pt `PdfDocument`, 72-pt margins, black text, 35 rows per page. (a) one page "New Loan Details-dd-MM-yyyy" (header = line name): SNo, Area, Party, Loan (MCLoanAmount), Interest, DC, Dues (TotalMonths) for new loans of new and existing parties; (b) rows/35 + 3 pages "Collection Details-dd-MM-yyyy" (header "<businessnameinapp> - <line>"): blue centred sub-area headings, then SNo, Party ("<CWLedgerId>.<name>"), Loan Date, Loan (LAmount), Balance (LAmount − PrevCollected), Collection (Collected) for every `MobileAppCollData` row ordered by sub-area ShowId, UDLedgerId; (c) one page "Expenses and Incomes-dd-MM-yyyy": SNo, ledger name (narration below, italic serif), amount in (ttid 8), amount out (ttid 7). Saved as `Documents/venusmfc/mf_<dbidentificationno>_<collection date>-<emailidentifier>_<yyyyMMdd_HHmmss>_<ANDROID_ID>.pdf`.
7. **Report upload** (`ASYNCUploadDailyReportFile`): OkHttp multipart POST to `daily_report_file_upload_url` with parts `image` (the PDF, `application/pdf`, streamed by `ProgressRequestBody` with % progress), `website`="www.venussolutions.net", `email`="murugesanrvenus@gmail.com" (hard-coded vendor address); the reply is ignored and the local PDF is force-deleted.
8. **Mail** (`ASYNCsendmailtoadmins`): OkHttp form POST to `email_sending_url`: `receipantadrees`=reporting e-mail, `emailbody`="REFER ATTACHMENT", `filenametobesent`=PDF name (the server attaches the uploaded file), `emailsubject`="<FinanceDetails.EmailIdentifier>-<dd-MM-yyyy>- Collection,Loans and Expenses- Sent by:<APPUserName>"; first line of the reply (or "EMail Failed") shown in `mailaddressid`; then `ASYNCUpload` starts whatever the outcome.
9. **ASYNCUpload checks** (horizontal dialog "Connecting, please wait ..."; steps "Connecting", "Access Verification", "Cash and Bank Book Verification", then one message per phase): if `enablemoniterondownloadandupload_1` is off, or special access 6, or full access ⇒ `select top 1 … 'True' as IsAllowed from FinanceAccessAllowed x, MobileDevlicesAllowed y where x.IMEI=y.IMEI and x.FinId=<line> and y.isActive=1 and x.IMEI='<ANDROID_ID>'`; else the same with `left join UploadControling z on x.FinId=z.FinId and TransDate='<date>'` and the SSId-32 CASE used by the download. Not active/allowed ⇒ "This Device may deactivated or Upload may closed for this date". Then, if `enablecashbookverification` (SSId 101): `exec checkisanyoneofcashandorbooksverified @FinId=<line>, @VerfiedDate='<date>'` ⇒ `verifiedCount` > 0 ⇒ "Cash and Bank Book Verification is partialy completed\ncannot upload once again". Inactive, not allowed or verified ⇒ stop.
10. **Phase 1 – new parties** (`NewParties order by LedgerId`): no `ServerGenLedgerId` ⇒ `{call dbo.InsertNewParties(19 params)}` when the party has a CWLedgerId, else `{call dbo.InsertNewPartieswithautocwledgerid}` (CWLedgerId as OUT): LedgerName, Tag, AreaNo = FinId, SubAreaNo, UDLedgerId null, MobileNo1/2, AddLine1, SpouseName, FamilyName, ImagefileName, IdProoffileName, IdProofFileNameII, lat, lng, NoOfLoansAllowed, MaximumAmountAllowed ⇒ OUT LedgerId ⇒ local `NewParties.ServerGenLedgerId` and `NewMCLoans.ServerGenLedgerId` (by PartyLedgerId). Already uploaded ⇒ `update tblLedgers set CWLedgerId, LedgerName, Tag, MobileNo1, MobileNo2, AddLine1, SpouseName, FamilyName, ImagefileName, IdProoffileName, lat, lng, IdProofFileNameII, NoOfLoansAllowed, MaximumAmountAllowed where LedgerId=<ServerGenLedgerId>`. Only image/ID-proof file names and GPS are sent; image files are not uploaded here.
11. **Phase 2 – new loans** (`NewMCLoans order by MCLoanId`): half-done loan (ServerGenTransId > 0, ServerGenMCLoanId = 0) ⇒ `delete from tblMCLoans where TransId=?` and redo; new loan with a 10-digit `mobileno` ⇒ `update tblledgers set MobileNo1=…`; no TransId ⇒ `InserttblTransactionsMaster` (TransDate = MCLoanDate, TransType 21, FinId, TransAmount = MCLoanAmount, TransLedgerId = party, RouteId = FinId) ⇒ `ServerGenTransId`; no server loan ⇒ `{call dbo.InserttblMCLoanswithWeekdayId(32 params)}` (MCLoanDate, FinId, PartyLedgerId, MCLoanAmount, TotalMonths, IntRate, LoanType 0, CollectionLineId 0, TransId, DCPercent 0, DocCharges, LoanMethodId, InterestAmount, MCLoanAmountCollectable, InsAmount, MCLoanAmountEstimated = collectable, Remarks, WeekDayId, MonthDayId, mobileno, Sourceledgerid, PartyReferenced, loanindex, Productvalue, AdvanceAmount; agent/guarantor/partner ids null) ⇒ `ServerGenMCLoanId`; products `NewItems` ⇒ `INSERT INTO InvTransDetails(SNo, ItemCode, BilledQty = −Qty, TransId, Rate, Total)`; local `RelatedDocCollection` rows ⇒ server `Update RelatedDocCollection set MCLoanId=<server loan> where DCUId=?`. Already uploaded ⇒ update `tblTransactionsMaster` (TransAmount, Transdate), mobile no., `update tblMCLoans set … where TransId=?` (16 columns), delete + re-insert `InvTransDetails`, re-point documents. The loan voucher is always rebuilt: `delete tblTransactionsDetails where TransId=…`, then rows (SNo: ledger, side, amount, Dr/Cr, against-SNo) 1: party, Target, collectable, Dr, 2 · 2: Sourceledgerid, Source, collectable, Cr, 1 · 3: Sourceledgerid, Target, interest, Dr, 4 · 4: ledger 108, Source, interest, Cr, 3 · if DocCharges > 0: 5: Sourceledgerid, Target, doc charges, Dr, 6 · 6: ledger 104, Source, doc charges, Cr, 5.
12. **Phase 2b – on-day collection** of new loans: if `MobileAppCollData` has no row for the server loan id and `ondaycollection` > 0, a synthetic row is inserted (MCLoanId = FWUDMCLoanId = server loan; PartyLedgerId/CWLedgerId/UDLedgerId = server party; dates = collection date; PartyName "PartyName", SubAreaNo 0, Collected = on-day amount); otherwise the existing row is updated. The amount goes to `CashCollection` when `Sourceledgerid` ≤ 100, else to `BankCollection`.
13. **Phase 3 – collections**: totals Σ Collected (due), CashCollection, BankCollection, PenaltyCollected, SavingsCollected. First upload of the day (no `LastUpload` or MCLGCollId = 0) with due, penalty or savings > 0 ⇒ `InserttblTransactionsMaster` (TransDate, TransType 22 = `constantvalues.MCLoanCollectionTTId`, TransAmount = due + penalty + savings, RouteId = FinId) ⇒ `CollTransId`, then `{call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(CollDate, CollAmount = due, PenaltyCollected, SavingsCollected, CollStaffLedgerId = executive-1, CollStaff2LedgerId = executive-2, TransId, NIPAmountCollected 0, FinId, BankCollection, CashCollection)}` ⇒ `MCLGCollId`. Re-upload ⇒ `update tblMCLoanGroupCollectionMaster set CollDate, CollAmount, CollStaffLedgerId, NIPAmountCollected=0, CollStaff2LedgerId, PenaltyCollected, SavingsCollected, CashCollection, BankCollection where MCLGCollId=?`, `Update tblTransactionsmaster set TransAmount=<bank + cash>`, `delete from tblMCLoanDueCol where GColId=?`, `delete from tblTransactionsDetails where TransId=?`, and with visit check-up `delete from PartyVisitStatus where MCLGCollId=?`. Then one SQL batch: per row with any amount `insert into tblMCLoanDueCol (GColId, MCLDueColDate, MCLoanId, TTId=22, FinId, Paid, PenaltyCollected, SavingsCollected, BankCollection, CashCollection)`; when `enablevisitcheckup_6a` (SSId 23) all rows are read and each gets `insert into PartyVisitStatus(MCLGCollId, PartyLedgerId, MCLoanId, IsVisited)` (otherwise only rows with amounts are read). Voucher for `CollTransId`: cash > 0 ⇒ ledger 114 Source Cr (SNo 1, against 2) + cash-book ledger Target Dr (SNo 2); bank > 0 ⇒ ledger 114 Source Cr (SNo 3) + bank ledger Target Dr (SNo 4). Finally the server IST time ⇒ local `LastUpload` replaced by (MCLGCollId, FWGColId, TransId, SubmitAmount = cash + bank, SubmitedAt).
14. **Phase 4 – expenses/incomes** (`ExpensesEntries order by ID`): new ⇒ `InserttblTransactionsMaster(13 params)` (TransDate = collection date, TransType = TTId 7/8, Narration, FinId, TransAmount, GColId = MCLGCollId if > 0 else null, TransLedgerId = ledger, RouteId = FinId, Sourceledgerid = cash-book ledger) ⇒ local `ServerGenTransId`; existing ⇒ `UpdatetblTransactionsMaster(9 params)` and the old details deleted. Details: TTId 7 ⇒ 1: ledger Target Dr, 2: SourceLedgerId Source Cr; TTId 8 ⇒ 1: ledger Source Cr, 2: SourceLedgerId Target Dr.
15. Each phase has its own try/catch: an exception sets that status to "error <message>" and the next phase still runs. `onPostExecute`: allowed ⇒ dialog "Upload Status" listing "New Parties / New Loans / Collections / Expenses :<status>" plus either "All Components Uploaded Successfully\nYou have to confirm the collection details in Today Collection" (OK = finish, "Go to Today Collection") or "One or more Components failed to upload try again" (OK = finish); refused ⇒ dialog "Upload Status" with the refusal text (OK = finish). Then `ShowLastUpdate()`.
16. **Upload SMS** when `isSendSMSonDataUpload_33` (SSId 6), all four statuses "Success" and upload allowed: template STId 5 (branch-managed DB) else 2; `{BranchName}`, `{LineName}` (+ "Dt: date" and "@ dd-MM-yy hh:mm aa"), `{executive1}`, `{executive2}`, `{LoanAmount}`, `{CollAmt}` (cash + bank), `{expenses}`, `{Receipts}` (each "Rs.<n>"); `SmsSender.sendMultipartSMS(AdminMobileNos, …)` when `smsmethod` = 1; toast "SMS Sent".

**Functions.**
- `onCreate()` – load prefs, build proposed cash book and totals, wire Update.
- `lambda$onCreate$1()` – Update click: loan-index conflict check, then `SyncWithServer()`.
- `SyncWithServer()` – report-then-upload chain or direct upload.
- `ShowLastUpdate()` – read `LastUpload`, show last submit amount/time, set `MCLGCollId`, `CollTransId`, `isLastUpdateFound`.
- `ASYNCUpload.onPreExecute()` / `onProgressUpdate()` / `doInBackground()` / `onPostExecute()` (+ `lambda$onPostExecute$0..3`) – checks, four phases, status dialogs, SMS trigger.
- `ASYNCSendSMS.doInBackground()` / `onPostExecute()` – upload alert SMS.
- `createPdf(name)` – build the three-section PDF, save, start report upload (argument ignored).
- `drawloandetailsPage()`, `drawcollectiondetailsPage()`, `drawexpensesdetailsPage()` – Canvas drawing of each section; `drawCenter()` – horizontally centred text.
- `ASYNCUploadDailyReportFile.uploadFile()` / `onProgressUpdate()` / `onPostExecute()` – multipart upload with % progress, delete PDF, start mail.
- `ASYNCsendmailtoadmins.doInBackground()` / `onPreExecute()` / `onPostExecute()` – mail API call, show reply, start `ASYNCUpload`.
- `ProgressRequestBody.contentType()` / `contentLength()` / `writeTo()` – OkHttp body streaming the file in 2 KB chunks with a progress callback.
- `showAlert()` – OK-only dialog; `whiteNotificationBar()` – light status bar, white colour.
- Dead code: `printDocument()` + `MyPrintDocumentAdapter` (`onStart`, `onLayout`, `onWrite`, `pageInRange`) – A4 print of collection pages; `exportDB()` – copy the SQLite file to `DirectoryName` (never set); `getfilepath()`.

**Rules & edge cases.**
- Re-upload is the retry mechanism: nothing is deleted locally after an upload; the `ServerGen*` ids in `NewParties`/`NewMCLoans`/`ExpensesEntries` and `LastUpload` (MCLGCollId/TransId) make a second "Update" update the same server rows and rebuild their details, until a cash/bank book verification exists for that line/date. Local data is only reset by the next download.
- The PDF/e-mail chain runs before the access and verification checks, so the report is mailed even when the upload is then refused. The drawer loads `reportingemailaddress` from prefs with default "Not Found", so it is practically never null and the report path runs unless a full-access user unticks the box.
- If the verification procedure itself fails after the device check passed, the upload still runs but the final dialog reports failure (`iserroroccured` is set).
- PDF limits: the new-loan and expense sections are one page each (rows beyond 35 are dropped); collection pages = rows/35 + 3 and sub-area headings also use rows, so long lists can be cut and trailing pages blank.
- Hard-coded ledger ids: 108 (interest), 104 (document charges), 114 (credit side of the collection voucher); cash vs bank for on-day collection decided by `Sourceledgerid` ≤ 100.
- Inconsistencies: the first upload sets the collection `TransAmount` = due + penalty + savings, a re-upload sets cash + bank; `FWGColId` is never read from the procedure, so `LastUpload.FWGColId` is null; after a first upload the "Collected Amount" label double-counts on-day collections (they now also sit in `MobileAppCollData`); with visit check-up and no collection, `PartyVisitStatus` rows are written with MCLGCollId 0.
- Phases do not depend on each other: if a party insert fails, that party's loans are still sent with whatever `ServerGenLedgerId` they hold.
- Messages: the failure dialog appends `ErrorTest`, which is never set (shows "null"); the refusal dialog uses `msgtext` read before its default is assigned, so a connection error shows an empty message.
- No check that executives were chosen (ids come from the download prefs). SQL is built by string concatenation (e.g. `MobileNo1='<mobileno>'`). The upload SMS has no `AdminMobileNos` length check.
- Not handled here: savings-account closures, party transfers and the staff GPS queue (`StaffLocationQueue`) — they are not uploaded by this screen.

## Background collection sync — `MyService_updatecollection`
| | |
|---|---|
| **Type** | Service (started; `onBind` returns null); manifest `enabled=true`, `exported=true`, no permission |
| **Layout(s)** | none |
| **Opened from** | `editing_collectionswithfulloptions_act.onCreate()` → `startService(...)` every time the full-options collection screen opens; never stopped by the app |
| **Opens** | none; sends broadcast action `com.example.andy.myapplication` with extra `DATAPASSED`, received by `editing_collectionswithfulloptions_act` (registered in `onStart` as not exported, unregistered in `onStop`) |
| **Data** | SQLite `MobileAppCollData` ⋈ `Parties` ⋈ `SubAreaDetails` (rows `islocallyfixed='true' and MCLDueCollId is Null`), `LastUpload` · Server proc `dbo.InserttblMCLoanDueCol` (15 params, OUT `MCLDueCollId`), table `tblMCLoanGroupCollectionMaster` (update) · globals `finid`, `collectiondate`, `selbranchconnString` |

**Purpose.** Near-real-time upload of collections confirmed one by one in the ONLINE collection screen. That screen's save FAB (shown only when `vss_executivehandlingmethod`=2 and `calledfrom`=9907) marks rows that have an amount and no `MCLDueCollId` as `islocallyfixed='true'`; this service then posts them in the background against the day's group collection.

**UI.** None (no system notification). Feedback is the broadcast: the collection screen writes `DATAPASSED` into its `lastupdatelbl` and refreshes each row's `MCLDueCollId` from `MobileAppCollData`, so uploaded rows are marked.

**Menu / actions.** None.

**How it works.**
1. `onCreate`: `Timer.scheduleAtFixedRate(TimeDisplayTimerTask, 0, 10 000 ms)` (`NOTIFY_INTERVAL`).
2. Each tick (posted to the main `Handler`): counts pending rows; if any, runs `ASYNCSavingCollections`.
3. `doInBackground`: connects; first `LastUpload` row ⇒ `groupcollectionslist_data` (MCLGCollId, TransId, FinId = `globalvars.finid`, CollDate = `globalvars.collectiondate`); loads the pending rows (MCLoanId, Collected, SubAreaNo, InsAmount, PaidDues, PassedDues, collection lat/lng, LAmount, PartyLedgerId, SubAreaName, CashCollection, BankCollection).
4. For each row with Collected > 0: `{call dbo.InserttblMCLoanDueCol}` — MCLDueColDate, MCLoanId, TTId 22, SourceLedgerId = party, TargetLedgerId = 1, FinId, Paid = Collected, GColId = MCLGCollId, PenaltyCollected, SavingsCollected, CashCollection, BankCollection, lng, lat (GPS of the collection) ⇒ OUT `MCLDueCollId` ⇒ `update MobileAppCollData set MCLDueCollId=… where MCLoanId=…`.
5. Totals of uploaded rows (`MCLDueCollId>0`: Σ Collected, PenaltyCollected, SavingsCollected) ⇒ `Update tblMCLoanGroupCollectionMaster set CollAmount=?, NIPAmountCollected=0, PenaltyCollected=?, SavingsCollected=? where MCLGCollId=? and TransId=?`.
6. `onPostExecute`: broadcast `DATAPASSED` = current time "h:mm:ss a" on success, or the exception text on error.

**Functions.**
- `onCreate()` – start the 10-second timer; `onBind()` – returns null.
- `TimeDisplayTimerTask.run()` / `lambda$run$0()` – pending-row check on the UI thread, launches the upload task.
- `TimeDisplayTimerTask.getDateTime()` – formats "[yyyy/MM/dd - HH:mm:ss]"; unused.
- `ASYNCSavingCollections.doInBackground()` / `onPostExecute()` – upload and broadcast.

**Rules & edge cases.**
- Needs a `LastUpload` row (the day's group collection); without it the group object is null ⇒ exception ⇒ the error text is broadcast every 10 s.
- Per-loan `PenaltyCollected`/`SavingsCollected` are always sent as 0 (never read from the cursor) while the group totals use the local values; rows with only penalty/savings (Collected = 0) are never sent, keep `MCLDueCollId` null and re-trigger the task on every tick.
- Does not touch `tblTransactionsMaster`/`tblTransactionsDetails`. Opens a new SQLite handle every tick without closing it; relies on static globals that are lost if the process dies; subject to Android background-service limits.
- Exported without a permission, and the status broadcast is implicit with a template action name, so other apps could start the service or listen to its messages.

## Intent service template — `MyIntentService`
| | |
|---|---|
| **Type** | IntentService (worker thread "MyIntentService"); manifest `exported=false` |
| **Layout(s)** | none |
| **Opened from** | nothing — its static starters `startActionFoo()` / `startActionBaz()` are never called |
| **Opens** | none |
| **Data** | none in practice (the unused inner task would use `LastUpload`, `MobileAppCollData`, `InserttblMCLoanDueCol`, `tblMCLoanGroupCollectionMaster`) |

**Purpose.** Android Studio "IntentService" template left in the app; it has no live function.

**UI.** None.

**Menu / actions.** None.

**How it works.**
1. `onHandleIntent` dispatches action `net.venussolutions.venusmicrofindailyweeklycollection.action.FOO` / `.action.BAZ` (extras `…extra.PARAM1`, `…extra.PARAM2`) to `handleActionFoo()` / `handleActionBaz()`, which both throw `UnsupportedOperationException("Not yet implemented")`.
2. The private `ASYNCSavingCollections` is never instantiated; it is an older copy of `MyService_updatecollection`'s upload (all `islocallyfixed` rows, totals summed locally, `InserttblMCLoanDueCol` per unsent row, group-master update). Its decompiled loop is garbled.

**Functions.**
- `startActionFoo()`, `startActionBaz()` – build and start the template intents (unused).
- `onHandleIntent()` – action dispatch; `handleActionFoo()`, `handleActionBaz()` – throw.
- `ASYNCSavingCollections` – dead upload code.

**Rules & edge cases.** Dead code. In the copy, GPS is read into `lat/lng` but sent from `collectionlat/lng` (null) and cash/bank amounts are never read (0).

## Upload Control / Download Control — `uploaddownloadcontroling`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`) hosting a `ViewPager` with `SectionsPagerAdapter` (FragmentPagerAdapter) of inner `PlaceholderFragment` pages; `AppTheme.NoActionBar`, EdgeToEdge; manifest label `@string/title_activity_uploadalloweddetails` ("uploadalloweddetails") |
| **Layout(s)** | `activity_uploadalloweddetails.xml`, page `fragment_uploadalloweddetails.xml`, rows `uploadcontrolingrow.xml`; menu `menu_uploadalloweddetails` |
| **Opened from** | Drawer → Admin → **Upload Control** (`nav_uploadcontrol`, `globalvars.calledfromuploadordownload`="upload") / **Download Control** (`nav_downloadcontrol`, "download"); both items visible only when `enablemoniterondownloadandupload_1` (SSId 32). The drawer task `ASYNCGetDownloadUploadAllowedDetails` first runs `exec Get_UploadLimitationsDateRange @IMEI='<ANDROID_ID>'` (JSON rows ⇒ `globalvars.uploadperioddateslist`, newest date first) and, if any date came back, `exec Get_UploadLimitations @IMEI` or `exec Get_DownloadLimitations @IMEI` (JSON ⇒ `globalvars.uploadinglimitationsJSON`); empty ⇒ toast "Access Denied" |
| **Opens** | none |
| **Data** | globals `uploadperioddateslist`, `uploadinglimitationsJSON`, `calledfromuploadordownload`, `salesdetailsforandroid_datalist` · Server tables `UploadControling` / `DownloadControling` (FinId, TransDate, isAllowed) — delete + insert |

**Purpose.** Admin screen to open or close uploading (or downloading) for each line on each date; `uploadcollection` and `downloaddata` check these rows when server soft setting 32 is on.

**UI.** Toolbar title "Upload Controling" or "Download Controling"; one swipeable page per allowed date with heading `section_label` (dd-MM-yyyy) and a list of lines (`uploaddetailsfinancenameid`) each with an allow switch `swallowupload`.

**Menu / actions.** Overflow **Save** (`action_saveuploadcontrol`) — visible only when the role's `AndroidMenus` rights for `nav_uploadcontrol` / `nav_downloadcontrol` include UpdateState (forced on for full access). Switch toggles change only the in-memory value until Save.

**How it works.**
1. `onCreate`: sets the title and menu name, finds the matching `salesdetailsforandroid_data`, parses `uploadinglimitationsJSON` with Gson into the static `uploaddetailslist` (`uploadalloweddetailsdata`), sorted by `IntCalDate` descending.
2. `PlaceholderFragment.onCreateView`: page n shows date n of `uploadperioddateslist`, filters `uploaddetailslist` to that date and binds `uploadcontrolingadabter` (kept in static `mAdapter`).
3. Toggling a switch ⇒ `oncheckchanged` sets `isAllowed`; the adapter also updates the matching (date, FinId) item of `uploaddetailslist`.
4. Save builds one batch — `delete from <UploadControling or DownloadControling>;` repeated once per date, then for every item `insert into <table> (FinId,TransDate,isAllowed) values (<FinId>,'yyyy-MM-dd','true' or 'false');` — and `ASYNCDetails` runs it with `Statement.execute` (spinner "uploading ...").
5. `onPostExecute` only hides the spinner.

**Functions.**
- `onCreate()`, `onCreateOptionsMenu()` (Save visibility), `onOptionsItemSelected()` (build SQL, start task).
- `PlaceholderFragment.newInstance(section)`, `onCreateView()`, `oncheckchanged()`.
- `SectionsPagerAdapter.getItem()` / `getCount()` (= number of dates).
- `ASYNCDetails.onPreExecute()` / `doInBackground()` / `onPostExecute()` / `onProgressUpdate()`.

**Rules & edge cases.**
- The delete has no WHERE clause: Save wipes the whole table and re-inserts only the rows returned for this device's IMEI, so rows of other lines/dates are lost.
- No success or error feedback ("Connection Error,Please try again..." is stored in `ErrorText` but never shown).
- NullPointerException if the menu-rights list has no `nav_uploadcontrol` / `nav_downloadcontrol` entry (e.g. right after a download emptied it).
- The fragment's extra sort works on a throw-away copy (no effect); the adapter filter is a stub; `onProgressUpdate` would throw NumberFormatException (never called).
- JSON field names must match the models: `FinId`, `FinName`, `IntCalDate`, `isAllowed`; periods `IntCalDate`, `dayid`.

## Today Collection (offline list) — `newtodaycollectionofflinedata_list`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`, implements `todaycollectionlistadapter.ContactsAdapterListener`); `AppTheme.NoActionBar`, EdgeToEdge |
| **Layout(s)** | `activity_newloanslist.xml` (shared list layout) + rows `todaycollectionlist_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer → Executive Entries (offline) → **Today Collection** / இன்றைய வசூல் (`nav_newcollection`), no extras |
| **Opens** | nothing (row tap does nothing) |
| **Data** | SQLite `MobileAppCollData` ⋈ `SubAreaDetails` |

**Purpose.** Read-only, searchable list of the loans that have a collection entered in the current offline download, with the cash/bank split.

**UI.** Toolbar title "Collection for a Day" (நாளது வசூல்) with up arrow; FAB `newloan_fab` and date-range bar `lytdaterangeid` hidden; totals bars stay hidden. Row: "<CW no>. " + party name, loan date / loan amount / installment, sub area, cash amount and bank amount.

**Menu / actions.** Search (SearchView): filters on submit and on every change; keywords BANK, CASH, PAID, UNPAID or party/sub-area text (see `todaycollectionlistadapter`). Back closes an open search first; Up = back.

**How it works.**
1. `onCreate`: adapter with `ctid`=7 (hides the "extra collected" box), sort method 0, ascending.
2. `fetchdata_fromnewpartiestable()` (misnamed): `Select MCLoanId, FWUDMCLoanId, CWLedgerId, PartyLedgerId, MCLoanDate, PartyName, b.SubAreaName, LAmount, Collected, CashCollection, BankCollection from MobileAppCollData a, SubAreaDetails b where a.SubAreaNo=b.SubAreaNo and Collected>0` (no ORDER BY) ⇒ `todaycollectionsdata` (LDate = `Date.valueOf(MCLoanDate)`).
3. `onActivityResult` reloads the list (nothing is launched from here).

**Functions.**
- `onCreate()`, `fetchdata_fromnewpartiestable()`, `onCreateOptionsMenu()` (SearchView wiring), `onOptionsItemSelected()` (home/back), `onActivityResult()` (reload), `onContactSelected()` (empty), `handleOnBackPressed()` (close search first), `whiteNotificationBar()`.

**Rules & edge cases.** The installment is not selected, so rows show 0 for it; rows with only penalty/savings are excluded; synthetic on-day rows created by an upload (SubAreaNo 0) don't join `SubAreaDetails` and are not listed; with "Overall Collection List" the area shows "Over all List". `MCLoanDate` must be in yyyy-MM-dd form.

## Archived loan details — `archievedloandisplay`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`), `exported=false`, default theme; title "Archieved Loans"; EdgeToEdge |
| **Layout(s)** | `activity_archievedloandisplay.xml` |
| **Opened from** | `prevloanslist_act.onContactSelected()` when the tapped previous loan has status "A-Closed": its task `ASYNCGetSelectedArchievedloandetails` reads server `ArchivedMCLoans where Mcloanid=<loan>` and passes extras `prevloan` (`archievedloandisplay_data`) and `partyname` |
| **Opens** | none |
| **Data** | intent extras only (no DB access) |

**Purpose.** Shows the summary of a closed loan that has been moved to the archive table: amounts, dates and collection-behaviour statistics.

**UI.** Party name `cd_arcpartynameid`; values: Loan Amount (MCLoanAmount), Interest Amount, No of Dues (shows `ReportIdentification`), No of Days Taken, Loan Date, Closing Date, Last Payment Date (dd-MM-yyyy), Loan Collectable (MCLoanAmountCollectable), Min / Max / Average Collection, Minimum / Maximum / Average Gap, Loan index.

**Menu / actions.** None.

**How it works.**
1. Reads the extras and fills the TextViews.
2. "No of Days Taken" = `getDaysBetweenDates(LastPaymentDate, MCLoanDate)` = absolute difference in whole days.

**Functions.**
- `onCreate()` – bind values; `getDaysBetweenDates(d1, d2)` – |d2 − d1| in days (−1 on error).

**Rules & edge cases.** "No of Dues" displays `ReportIdentification` (the loan-method report code), not a count. If the server query failed or found nothing, the caller still opens the screen with an empty object and the null dates crash `DateFormat.format`.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `todaycollectionlistadapter` | RecyclerView adapter + `Filterable`; inner `MyViewHolder`; interface `ContactsAdapterListener.onContactSelected(todaycollectionsdata)` | `todaycollectionlist` (ctid from caller, sort from prefs `orderByMethod`/`orderByAsc`), `newtodaycollectionofflinedata_list` (ctid 7, sort 0) | Row `todaycollectionlist_row`: `partynoid` "<CWLedgerId>. ", `partynameid` party, `loannoloandateloanamountid` loan date + LAmount + InsAmount joined by vertical bars, `tagid` sub area (these three only when the loan date is set), `tvcollectedamountnewid` cash collected, `tvbankcollectionid` bank collected. Box `extraamtcalc` shows `tvextracollected` = cash collected − installment (green if positive, red if negative) when ≠ 0, installment > 0 and ctid ≠ 7. Row tap → listener. Filter: "BANK" (bank > 0 and MCLoanId > 0), "CASH" (cash > 0), "PAID" (cash + bank > 0), "UNPAID" (= 0), else party name (case-insensitive) or sub area (case-sensitive) contains the text; filtering re-applies the sort. `updateOrder(method, asc)` / `applySorting()`: 3 = CW ledger no., 2 = loan date, other = sub-area ShowId then UDLedgerId; ascending or descending. |
| `uploadcontrolingadabter` | RecyclerView adapter + `Filterable`; interface `uploadalloweddetailslistner.oncheckchanged(uploadalloweddetailsdata, boolean)` | `uploaddownloadcontroling.PlaceholderFragment` | Row `uploadcontrolingrow`: line name + switch = `isAllowed`. Switch change → listener and update of the matching (IntCalDate, FinId) item in the static `uploaddownloadcontroling.uploaddetailslist` (skipped while binding via the `isdisp` flag). Filter is a stub (any non-empty query gives an empty list) and is not wired to a search box. |
| `proposedcashbook_adapter` | RecyclerView adapter + `Filterable` | `uploadcollection` | Row `proposedcashbookrow`: Particulars, Debit (blank when 0), Credit (blank when 0). Its unused filter upper-cases the query but compares it with lower-cased text, so it only matches non-letters. |
| `proposedcashbookdata` | Model | `uploadcollection`, `proposedcashbook_adapter` | `sno`, `particulars`, `debit`, `credit` (int). |
| `uploadalloweddetailsdata` | Model (Gson target) | `uploaddownloadcontroling`, `uploadcontrolingadabter` | `FinId` (Integer), `FinName`, `IntCalDate` (java.util.Date), `isAllowed` (Boolean; accessors `getAllowed`/`setAllowed`) — one row per line per date from the `Get_UploadLimitations` / `Get_DownloadLimitations` JSON. |
| `archievedloandisplay_data` | Model (Serializable) | `prevloanslist_act` (filled from `ArchivedMCLoans`), `archievedloandisplay` | AMCLoanId, MCLoanId, FWMCLoanId, FWUDMCLoanId, FinId, PartyLedgerId, GuaranderLedgerID, MCLoanAmount, TotalMonths, DCPercent, DocCharges, InterestAmount, MCLoanAmountCollectable, InsAmount, Remarks, WeekDayId, MonthDayId, mobileno, Sourceledgerid, LoanIndex, ReportIdentification, MCLoanDate / ClosingDate / LastPaymentDate (java.sql.Date), MinCollection, MaxCollection, AvgCollection, MinGap, MaxGap, AvgGap, TotalCollection. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_downloaddata.xml` | `downloaddata` | Line name `tvRouteNameid` ("Line Name"); "Collection Executive-1/-2" (வசூலுக்கு செல்பவர்-1/-2) with read-only EditTexts `downloaddata_etexecutive1id` / `downloaddata_etexecutive2id`; "Collection Date" (வசூல் தேதி) `dcl_collectiondateid` and weekday `dcl_collectiondayid`; CheckBox `dcl_enableoverallcollectionlistid` "Overall Collection List"; Button `btnDownload` "Download". |
| `activity_uploadcollection.xml` | `uploadcollection` | RelativeLayout `main`: title "Upload Collection Details ", `tvCollectionRoute`, `ucl_collectiondateid`; executive rows `ucl_collectionstaff1id` / `ucl_collectionstaff2id`; "Estimaated Thittam" (உத்தேச திட்டம்) + RecyclerView `recycler_view`; "Cash Collection:" `tvcashcollection`, "Bank Collection:" `tvbankcollection`; "Abstract" (சுருக்கம்) with `tvNewLoanAmount`, `collectedamouttv`, `tvlastsubmitdate`, `tvlastsubmitamount`; CheckBox `ucl_mailsendcheckbox` "Send EMail to Administrator", `mailaddressid`; Button `btnsavecollection` "Update"; hidden `fileupload_progressBar`, `fileupload_txtPercentage`. |
| `proposedcashbookrow.xml` | `proposedcashbook_adapter` | Particulars `ppcb_particularsid`, Debit `ppcb_debitid`, Credit `ppcb_creditid`; hidden unused `tvcollectedamountnewid`. |
| `todaycollectionlist_row.xml` | `todaycollectionlistadapter` | `partynoid`, `partynameid`, `loannoloandateloanamountid`, `tagid` (sub area); hidden box `extraamtcalc` with `tvtotalcollection`, `tvcurdue` (never filled) and `tvextracollected`; amount group `amountGroup2` with `tvcollectedamountnewid` (cash) and `tvbankcollectionid` (bank). |
| `activity_uploadalloweddetails.xml` | `uploaddownloadcontroling` | CoordinatorLayout `main`; Toolbar (layout text "Upload Controling", title set in code); ViewPager `container`; include `customersearchlist` (RecyclerView `recycler_view`, unused at activity level). |
| `fragment_uploadalloweddetails.xml` | `uploaddownloadcontroling.PlaceholderFragment` | ConstraintLayout `constraintLayout`: date heading `section_label` + include `customersearchlist` (RecyclerView `recycler_view` of lines). |
| `uploadcontrolingrow.xml` | `uploadcontrolingadabter` | Line name `uploaddetailsfinancenameid`, hidden `finidid` (not bound), SwitchCompat `swallowupload`. |
| `activity_newloanslist.xml` | `newtodaycollectionofflinedata_list` (shared with `newloanslist_act`, `newpartieslist_act`, `loanslist_act`, `newpartiesnewloanslist_act`, `ledgerstatement_act`) | CoordinatorLayout `main`; Toolbar; hidden line bar `lytselectlineid`; date-range bar `lytdaterangeid` (From `loanlist_fromdateid`, To `loanlist_todateid`, refresh `loanslistrefreshid`) — hidden here; RecyclerView `loansslist_recycler_view`; hidden totals `lyttotallytid` (Total Loan / Interest / DC) and `lyttotalcollectionlytid` (Count / Collection / Balance); FAB `newloan_fab` "Add New" — hidden here. |
| `activity_archievedloandisplay.xml` | `archievedloandisplay` | ConstraintLayout `main`: party name `cd_arcpartynameid` ("PARTY NAME") and 15 label/value pairs — Loan Amount, Interest Amount, No of Dues, No of Days Taken, Loan Date, Closing Date, Last Payment Date, Loan Collectable, Min / Max / Average Collection, Minimum / Maximum / Average Gap, Loan index (`ilc_arc…id` value views). |

---
[← brain.md](../../brain.md) · Module 8.3 of 20 · Previous: [8.2 Navigation hub, routers and line/branch selection](02-navigation-hub-routers-line-branch-selection.md) · Next: [8.4 Offline collection entry (area-wise collection list & individual collection)](04-offline-collection-entry-area-wise-collection.md)
