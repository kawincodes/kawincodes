# 8.2 Navigation hub, routers and line/branch selection

[← brain.md](../../brain.md) · Module 8.2 of 20 · Previous: [8.1 App start, registration, device identity, settings and access control](01-app-start-registration-device-identity-settings.md) · Next: [8.3 Offline mode: download, upload and sync services](03-offline-mode-download-upload-sync-services.md)

This module is the app's spine. `navigationdrawactivity` is the home screen after registration/login. Its navigation drawer opens every other module, filtered by role rights, server soft settings and DB type. Its body is the offline collection dashboard: one card per collection area plus three totals. `refreshorreloadbrancheslist` (multi-branch master DB only) picks a branch and re-syncs all branch-level master data. `refreshorreloadfinancelist` picks the current *line* (finance) and doubles as a line chooser for daily thittam, line editing and other callers. `selectbranchandfinance` ("sbf") is the generic branch + line + date/weekday picker that sits in front of most admin lists and reports. It also loads the data for the 7-days-collection and attendance reports itself.

## Home dashboard & navigation drawer — `navigationdrawactivity`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`), implements `areawisecollectionlistadapter.ContactsAdapterListener` and `NavigationView.OnNavigationItemSelectedListener`. Manifest label `@string/title_activity_main2` = "Micro FinSoft", theme `AppTheme.NoActionBar`, no intent filter. Edge-to-edge: system-bar insets are applied as padding to `drawer_layout`. |
| **Layout(s)** | `activity_navigationdrawactivity.xml`, which includes toolbar `addnewloanbar`, drawer header `nav_header_main2` and drawer menu `activity_navigation_drawer`. Row layout `areawisecollectionlistrow.xml`. Options menu `navigationdrawer` (empty `<menu/>`). |
| **Opened from** | `splash` (registered device, master or branch DB). `collectdbinfofromweb` (web registration verified, distribution type 1). `otpverificationact` (after its data download, when the user is active and allowed to upload). `syncresources` (after a sync that was started with `issyncneeded=true`; it passes `issyncneeded=true` on, which triggers `ASYNCfirstautosyncprocess`). |
| **Opens** | Every drawer target (routing map in the brief; corrections under *Rules*). `tabbedcollectionlistmviewpager` on an area-card tap (`listindex`). `uploaddownloadcontroling` after `ASYNCGetDownloadUploadAllowedDetails`. `syncresources` forResult 9026 after the first auto-sync. The system App-info screen (`android.settings.APPLICATION_DETAILS_SETTINGS`, `package:<pkg>`) after Clear My Data. |
| **Data** | **SQLite `venusmicrofinv005`.** Reads `tblSalesDetailsforAndroid`, `SpecialAccessAllowed`, `userroles`, `FinanceDetails`, `MobileAppCollData`, `SubAreaDetails`, `NewMCLoans`+`NewParties`, `Parties`. Writes `FinanceDetails` and `BankLedgers` (drop/recreate), both from the first auto-sync. Clears `MobileAppCollData`, `LastUpload`, `NewParties`, `NewMCLoans`, `SubAreaDetails`, `ExpensesEntries` (first auto-sync). Creates and clears `CashBook` (before the staff cash book) and `closableliveloans` (before loan closures). **Remote, selected branch DB `selbranchconnString`.** SP `GetAllowedFinances`; table `tblLedgers` (Groupid=5); SPs `Get_UploadLimitationsDateRange`, `Get_UploadLimitations`, `Get_DownloadLimitations`. **Web.** `GET http://microfinapp.venussolutions.co.in/removemfinstalationdetails.aspx?deviceid=<ANDROID_ID>`. **Prefs `venusmicrofinpref011`.** Reads about 90 keys (see `syncsettings`). Writes the current-line keys and `isfirstsyncdone`. |

**Purpose.** This is the landing page. The executive or admin opens any module from the drawer. In offline mode the executive watches progress per collection area (parties, marked, pending, warned, amount collected) and taps an area to start collecting.

**UI.**
- Toolbar `toolbar` (style `MyToolbar`) with the hamburger toggle ("Open navigation drawer" / "Close navigation drawer"). The title is the current line name (`globalvars.financename`). In offline mode it is `"<line> [dd-MM-yyyy]"`, using the collection date.
- Drawer header (`nav_header_main2`, `side_nav_bar` background):
  - Launcher icon.
  - `txtFinanaceGroupNameId`: business name (`softsettingsvalue.businessnameinapp`, pref `FinanceGroupName`, which comes from SoftSettings SSId 4).
  - `txtFinanceNameid`: line name, or `"<BranchName>-<line>"` for a master-DB user with multi-branch access.
  - `txtLoggedUserNameid`: `APPUserName` (pref `appusername`, default "USER").
  - `txtuserrolenameid`: role name from local `select URName from userroles where URId=<userroleid>`.
- `recycler_view`: area cards from `areawisecollectionlistadapter`, separated by `customersearchlistdivider`.
- `downloaddateidnew`: bottom strip, default text "Data Download time". In offline mode only, it shows `LastDownloadDatetime` with "+05:30" stripped, in white text. Otherwise it is hidden.
- The status bar is forced white with dark icons (`whiteNotificationBar`).

**Menu / actions.**
- **Drawer.** Routing is as mapped in the brief. Visibility rules and corrections are below.
- **Options menu.** `navigationdrawer` is empty, so the `action_settings` handler is dead code.
- **Card tap.** Calls `onContactSelected`. Total rows are ignored.
- **Back.** Closes the drawer if it is open. Otherwise the first press toasts "Please click BACK again to exit" and arms a 2000 ms window; a second press inside it disables the callback and exits.

**How it works.**
1. **`onCreate`.** Enables edge-to-edge and registers the back callback (`AnonymousClass1`). Sets the toolbar and `ActionBarDrawerToggle`, creates a `ProgressDialog` and loads prefs. If extra `issyncneeded` is true it runs `ASYNCfirstautosyncprocess`. Then it calls `syncsettings()`.
2. **`syncsettings()`.** Also re-run when `syncresources` returns (rc 9026).
   - **a. Restore globals from prefs.**
     - `finid`←`FinId`; `financename`←`FinanceName` (default "Please Login"); `cashbookledgerid`←`CashBookLedgerId`.
     - `MobileUserLedgerId`, `newmcloanid`, `newpartyledgerid`, `DefaultLoanMethodId`, `bankaccountledgerid`, `iscurrentuserhavemultibranchaccess`.
     - `collectiondate`←`CollectionDate`; `AdminMobileNos` (default "0,0"); `APPUserName`←`appusername`.
     - `selbranchconnString`/`…dbusername`/`…dbpassword` (default "USER"); `emailidentifier`, `smsidentifier`, `LastDownloadDatetime`.
     - `isselectedlineisweeklyline`; `app_userroleid`←`userroleid`; `isFullAccessAllowed`, `currentweekdayid`, `BranchId`, `isbranchusercontrolallowed`.
     - `cutoffdateforloanupdateblock`←`CutOffDateforLoanUpdateBlock` (default 2025-03-31).
   - **About 60 `softsettingsvalue` statics** are restored from same-named keys. Examples: `vss_executivehandlingmethod`, `vss_IsthisDBunderBranchManagement`, `vss_enablefinancecashbook`, `vss_enablestloansincollection`/`vss_enableofloansincollection`, `vss_enablelivelocationduringcollectionentry`, `enablecashbookverification`, `enablemoniterondownloadandupload`, `isEnableMoniteringonLoanclosoure`, `ispenaltyallowedincollectionscreen`, `issavingscollectionallowedincollectionscreen`, `GroupnameinsteadofAreaname_401`, `collectionreportprintingmethod`, `loanstatusmethod`, `currentfinancialyearid`, `LimitAccountTransactionstoCurrentLine`, `EnableMaximumAmountLoanAllowed`/`MaximumLoanAmountAllowed`, the SMS-on-download/upload/collection flags, the image/ID-proof flags, paths and upload URLs, `email_sending_url`, `daily_report_file_upload_url`, `LoanRelatedDocumentsUpload*`, `STLIntRate`/`OFIntRate` and `noofbackdaysallowedfordownloaddata`. `selectedfinance_*` copies of the penalty, savings and party-image flags are set from the global ones.
   - **b. Connection strings.** On a master DB: `BranchName` (default "Branch Not Selected") and `selbranch*` ← prefs `branchconnString`/`branchdbusername`/`branchdbpassword`. On a branch DB: `selbranch*` and `branchlevelReport_*` ← `globalvars.branch*` (set by `splash`).
   - **c. Setup.** Sets `todaydate` to today if null. Creates `Documents/venusmfc`. Opens SQLite. Sets the accounting year `accyearfromdate`/`accyeartodate` from `currentfinancialyearid`, where fin-year id *n* = 1-Apr-(2010+*n*) to 31-Mar-(2011+*n*). The hard-coded 1-Apr-2024 start is overwritten.
   - **d. Role menu rights.** `Select UserLevelId, mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState, mnuname, mnudisplayname, showid from tblSalesDetailsforAndroid where UserLevelId=<role>` is loaded into `globalvars.salesdetailsforandroid_datalist`.
   - **e. Special access.** `Select SAAId, UserLevelId, SARId, bitvalue from SpecialAccessAllowed` builds `globalvars.specialaccessrules`: SARId 1–20 map to the 20 rights listed in the brief.
   - **f. Drawer visibility**, applied in this order:

     | Item / group | Rule |
     |---|---|
     | `nav_finwisepartners`, `nav_partnerdetails`, `nav_SavingsClosures` | Hidden first. Only a role row can show them again. |
     | Every item whose resource-id name equals a role row's `mnuname` | Visible = `ShowState`. The server LEFT JOIN gives NULL (hidden) for menus not granted. Unknown names are skipped. |
     | Groups `reports`, `nav_admincontrols`, `nav_accounttransactions`, `nav_accountsreports`, `settingsmenu` | Hidden when the role has **no** rows at all. |
     | `nav_branchemployee` | If already shown: only for a master DB with multi-branch access. |
     | `nav_inventorycontrols` (hidden in XML) | If already shown: `curfin.isEnableinventory()`. |
     | `nav_SavingsClosures` | Hidden unless `issavingscollectionallowedincollectionscreen_13`. |
     | `executive_trans`, `nav_sync`, `nav_selectline`, `nav_collectionareas`, `navaboutus`, `nav_exit`, `nav_closeapp`, `nav_venuswebsite`, `nav_deviceid` | Always visible. The `BranchId>0` test on `nav_selectline` is immediately overridden. |
     | `nav_collectionareas` title | Becomes "SH Groups" / "குழுக்கள்" when `GroupnameinsteadofAreaname_401`. |
     | `nav_receivemoney`, `nav_returnmoney` | If already shown: offline handling only (`vss_executivehandlingmethod`=1). |
     | `nav_selectbranch`, `nav_branchaccesscontrol` | Master DB with multi-branch access. |
     | `nav_offlineexecutivehanlding` / `nav_onlineexecutivehanlding` | Handling method 1 / 2. |
     | `nav_uploadcontrol`, `nav_downloadcontrol` | If already shown: `enablemoniterondownloadandupload_1`. |
     | `nav_LoanClosures` | If already shown: `enablemoniteringonloanclosoure_5`. |
     | `nav_prevsavingclosure` | If already shown: `issavingscollectionallowedincollectionscreen_13`. |
     | `nav_stloanfinancetransactions` / `nav_ofloanfinancetransactions` | `vss_enablestloansincollection` / `vss_enableofloansincollection`. |

   - **g. Finish.** Fills the header texts, download time and title. Binds the RecyclerView to `globalvars.areawisecollectionlistdataList`, then calls `DisplayIndexData()`.
3. **`DisplayIndexData()`.** Also runs on every `onResume`.
   - **Prefs re-read:** `FinId`, `FinanceName`, `CashBookLedgerId`, `FinanceCashBookLedgerId`, `MobileUserLedgerId`, `newmcloanid`, `userroleid`, `isFullAccessAllowed`, the multi-branch flag, `newpartyledgerid`, `CollectionDate`, `isoverallcollectionenabled`, `AdminMobileNos`, `todaydate`, `appusername` and `LastDownloadDatetime`.
   - **Current line:** `globalvars.curfin` = `defaulttables.getfinanceobj` (`FinanceDetails where FinId=`).
   - **Offline mode only** (`vss_executivehandlingmethod == 1`), it rebuilds the dashboard:
     - **Area rows.** `select a.SubAreaNo, count(MCLoanId) as 'AreaNoCount', b.SubAreaName, sum(Collected) as Collected, count(IsMarked) … from MobileAppCollData a, SubAreaDetails b where a.SubAreaNo=b.SubAreaNo group by a.SubAreaNo,b.SubAreaName,b.Showid order by b.ShowId`. Collected is increased by the `ondaycollection` of today's new loans in that area (`defaulttables.getnewloanlist`: NewMCLoans ⋈ NewParties ⋈ SubAreaDetails, re-queried per area). Marked = `count(IsMarked) … where IsMarked=1 and SubAreaNo=n`. NIP = `count(IsNIP) … IsNIP=1`. Warn = `count(istobeWarned) … IsNIP=0 and istobeWarned=1`.
     - **"Live Loans Collection" / "நடப்பு வசூல்"** (SubAreaNo −1). Collectable = `sum(InsAmount)`, collected = `sum(Collected)`, parties = `count(MCLoanId)`, all where `ISNIP=0 and subareano>0`. "Paid" = count with `Collected>0`.
     - **"On day Collection"** (−3, English only). Count and sum of new loans with `ondaycollection>0`.
     - **"Pending Loans Collection" / "நிப்பு வசூல்"** (−2). Same as live, for `ISNIP=1`.
   - **Online mode** (2): the list stays empty and the download strip is hidden.
4. **`onContactSelected(pos)`.** Only for area rows (SubAreaNo>0).
   - Loads the **whole** offline list: `select PaidDues, PassedDues, x.MCLoanId, … x.IsAttentionRequired from MobileAppCollData x, Parties y where x.PartyLedgerId=y.LedgerId order by x.SubAreaNo, x.MCLoanDate desc`, into `globalvars.tabbedcollectionlistdataList`.
   - Each `tabbedcollectionlistdata` row holds: dues; balance = `MCLoanAmountCollectable − PrevCollected`; interest; loan and closing dates; party name, tag, CW and UD ids; NIP/marked/visited/attention flags; image and ID-proof file names; lat/lng; penalty, savings, cash and bank collection; doc charges.
   - Starts `tabbedcollectionlistmviewpager` with `listindex`=pos. That screen's tabs are the dashboard rows and it filters loans by SubAreaNo.
5. **`onActivityResult`** (resultCode is ignored):
   - 9025 (`branchselectionraised`): shows "Select Line" if `BranchId>0` and refreshes the header line text.
   - 9028 (`lineselectionraised`): refreshes the header line text.
   - 9026 (`syncresourcescalled`): calls `syncsettings()`.
6. **`ASYNCGetDownloadUploadAllowedDetails`.** Started by `nav_uploadcontrol`/`nav_downloadcontrol`, which set `globalvars.calledfromuploadordownload` to "upload"/"download". Shows a ProgressDialog "Working".
   - On the selected branch DB it runs `exec Get_UploadLimitationsDateRange @IMEI='<device_id>'` and concatenates column 1 of every row into `globalvars.uploadallowedperiodJSON`.
   - If there were rows, it runs `exec Get_UploadLimitations …` or `exec Get_DownloadLimitations …` (same `@IMEI`) into `globalvars.uploadinglimitationsJSON`.
   - Post: if the limitations JSON is non-empty, Gson parses the period JSON into `List<uploadallowedperioddata>` → `globalvars.uploadperioddateslist`, sorted by date descending (null dates first), and `uploaddownloadcontroling` opens. Otherwise, and on any error, it toasts "Access Denied".
7. **`ASYNCfirstautosyncprocess`.** Shows a ProgressDialog "Working".
   - On the selected branch DB it runs `GetAllowedFinances @IMEI='<device_id>'` and inserts each allowed line into local `FinanceDetails` (FinId, FinName, FinMailingName, address, PINcode, RegistrationNo, CashLedgerId, YearFrom, BooksFrom, ShortName, phones, AdminMobileNos, enableinventory, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString, Email/SMS identifiers, BankLedgerid, Showid, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage). The table is not cleared first.
   - If there is at least one line, it drops and recreates `BankLedgers` from `Select LedgerId, Ledgername, FinId from tblLedgers where Groupid=5` (group 5 = bank accounts).
   - It reads `FinanceDetails order by FinId` and makes the **first** line current. It sets the same globals as line selection in `refreshorreloadfinancelist` (except `curfin`) and writes the same prefs.
   - It clears the offline tables.
   - Post: starts `syncresources` forResult 9026 and sets pref `isfirstsyncdone`=true.
8. **Clear My Data** (`nav_clearmydata`). Confirm dialog "Info..." / "Sure to Remove Data.." with No / Yes. Yes runs `asyncremovethisdevicefromMobileDevicesAllowedTable`:
   - ProgressDialog "Removing your information ...".
   - HTTP GET to `removemfinstalationdetails.aspx?deviceid=<ANDROID_ID>` (5 s connect/read timeouts); the JSON reply carries `isdeviceremoved`.
   - Post, **always**, whatever the reply: `clearInternalStorage` (filesDir, recursive), `clearAppCache` (cacheDir), `clearSharedPreferences` (only `venusmicrofinpref011`; printer prefs `MyPref` survive), `deleteDatabase("venusmicrofinv005")`.
   - Then, on success: "Your Data Removed from our Records. You may hold it for future use or uninstall this app.... " with **Yes, Keep the APP** (`finishAffinity` + `System.exit(0)`) or **Want to Uninstall the APP** (App-info screen, then exit).
   - On failure: "Failed to remove your data, Please try again later.... " [OK].
9. **Close APP.** `Process.killProcess(myPid)` + `System.exit(1)`.

**Functions.**
- `onCreate()`: toolbar, drawer toggle, back callback, optional first auto-sync, `syncsettings()`.
- `AnonymousClass1.handleOnBackPressed()`: drawer close / double-back exit (2 s).
- `syncsettings()`: restores prefs into globals, connection strings, fin-year, role menu rights, special-access rules, drawer visibility, header, title, adapter; then `DisplayIndexData()`.
- `DisplayIndexData()`: rebuilds the area dashboard and total rows (offline mode only); sets title and download time.
- `onResume()`: `DisplayIndexData()`.
- `onNavigationItemSelected(MenuItem)`: drawer routing; closes the drawer.
- `onContactSelected(Integer)`: loads every offline loan, opens the tabbed area collection screen.
- `onActivityResult()`: handles 9025 / 9028 / 9026 (above).
- `whiteNotificationBar(View)`: light status bar.
- `clearSharedPreferences(Context)`, `clearAppCache(Context)`, `clearInternalStorage(Context)`, `deleteRecursive(File)`: local wipe helpers for Clear My Data.
- `asyncremovethisdevicefromMobileDevicesAllowedTable`: web unregistration, local wipe, exit/uninstall dialogs.
- `ASYNCGetDownloadUploadAllowedDetails`: fetches upload/download limitation JSON and opens `uploaddownloadcontroling`.
- `ASYNCfirstautosyncprocess`: first-run line download, default line, opens Sync.
- `onCreateOptionsMenu` / `onOptionsItemSelected`: empty menu, dead code. `onthumnailclicked`, `onPointerCaptureChanged`: no-ops.

**Rules & edge cases.**
- **Routing corrections to the brief.**
  - `nav_sevendayscollection` sends requestcode **9001** (`requestcodes.sevendayscollection_requestcode_frommenu`), not 3001. It also sends `calledfor`="sevendayscollection". 3001 is `constantvalues.Partnerdetails_AccessRequest`, which `nav_partnerdetails` sends to `parties_list`.
- **Extras the brief does not list.**
  - `nav_todaycollection` puts `requestcode` 9029 and then overwrites it with 9003. It also sends `ctid` 7, `financeid`=`curfin.FinId`, `collectiontype` "All Parties" and `collectiondate`=pref `CollectionDate`.
  - `nav_supplier` sends requestcode 3003; `nav_stloanint`/`nav_ofloanint` send 9002; `nav_varathittam` sends 9025.
  - `nav_loanscollection` also sends `calledfrom` 9908, and `nav_SavingsClosures` also sends `finidforloanclose`/`weekdayid` −1. sbf ignores both.
  - `nav_STLoanAnalysis`/`nav_OFLoanAnalysis` set `globalvars.curanalysisreport` from `defaulttables.fetchanalysisreportsettings()[0]` / `…forofloans()[0]`.
  - `nav_branchaccesscontrol` passes `calledfrom`="true" (a string). Both `nav_branchemployee` and `nav_branchaccesscontrol` re-check master DB + multi-branch access before opening.
- **Drawer items with special behaviour.**
  - `nav_help` has no handler; tapping it only closes the drawer.
  - `nav_import` requires `finid>0`, otherwise it toasts "Please Select Line First".
- **Pref-key mismatches:** these settings do not survive an app restart.
  - The cutoff date is read as `CutOffDateforLoanUpdateBlock` but written as `cutoffdateforloanupdateblock` → always 2025-03-31.
  - `DisabletodayCollectionforIndividualcollectionEntry` is read but `disabletodayCollectionforIndividualcollectionEntry` is written → always false.
  - `nipbreakforweeklythittam_14` is restored from key `nipbreakformonthlythittam`.
- **Defaults before the first sync or download.**
  - `currentfinancialyearid` defaults to 1, so the accounting year is 2011-12 until a sync stores the real id.
  - In `DisplayIndexData`, `Date.valueOf(CollectionDate)` throws while the pref is empty (no download yet). The rest of that try block (`todaydate`, `LastDownloadDatetime`, `curfin`, …) is then skipped.
- **Warning-flag bug.** `onContactSelected` calls `setIstobeWarned(Boolean.parseBoolean(c.getString(21)))`. Column 21 is `LoanMethodDesc` (smali `0x15`), so the warned flag is always false in the area screen opened from the dashboard.
- **Dashboard vs area screen.** The dashboard counts all `MobileAppCollData` loans, but the area list inner-joins `Parties`, so a loan without a local party row is counted but not listed. `SpecialAccessAllowed` is read without a `UserLevelId` filter; this relies on sync storing only the current role's rows.
- **Data loss.** The first auto-sync clears the offline tables (unsynced collections, new parties, new loans, expenses) without asking.
- **Clear My Data** wipes local data even when the server removal fails. The endpoint is unauthenticated cleartext HTTP keyed only by ANDROID_ID.
- **First auto-sync quirks.**
  - It sets `APPUserName` = `financemaster_data.getLedgerName()`, which is never populated. `putString(null)` removes pref `appusername`, so the header falls back to "USER".
  - It starts before `syncsettings()` opens `db` (a race; exceptions are caught).
- **Upload/download control.** `noofdaysallowed` counts result rows, so it is only a "has data" test.

## Branch / line / period picker — `selectbranchandfinance`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`), plain manifest entry, edge-to-edge. Requires extras; it throws `AssertionError` without them. |
| **Layout(s)** | `activity_selectbranchandfinance.xml`. The weekday spinner uses `spinner_item.xml` with framework `simple_spinner_dropdown_item`. The Sunday spinner uses framework `simple_spinner_item`. |
| **Opened from** | Only `navigationdrawactivity`, via extra `requestcode` (113, 114, 115, 304, 9001, 9004, 9006, 9010, 9012, 9013, 9021, 9022, 9024, 9025) plus optional `menuname` and `calledfor`. |
| **Opens** | The target screen for each request code (table below). `spinnersearch_list` pickers: branch ("Select Branch Name", rc 9905), line ("Select Line Name", rc 9906) and "Collection Day" (rc 2001/200/300/400/500). |
| **Data** | **Local.** Rewrites `SelectedBranchFinanceDetails`. Rewrites `LoanMethods` (multi-branch users only). Reads `BranchConnStrs`. Creates `FinanceDetails` if missing. Rewrites `SelectedFinanceSubAreaDetailsALL` and `SelectedFinanceSubAreaDetails`. Creates and clears `closableliveloans`. **Remote** (report connection `branchlevelReport_*`): SP `GetAllowedFinances`; tables `LoanMethods`, `SoftSettings`; SP `GetAllowedFinanceSubAreas`; SPs `APP_Get7daysCollectionfordailyloans`, `APP_Get7daysCollectionforweeklyloans`, `APP_Get7daysCollectionforweeklyloanswithWeekdayid`, `APP_Get7daysCollectionforMonthlyloans`; `tblledgers` (ScreenType 8); `TwenDuration`; SP `CollectionDayandCollectionExecutiveforapp`. **Prefs written:** `selectedfinance_enablemoniteringonloanclosoure`, `selectedfinance_showpartynoincollectionlist`, `selectedfinance_loanstatusmethod`, `selectedfinance_ispenaltyallowedincollectionscreen`, `selectedfinance_issavingscollectionallowedincollectionscreen`, `selectedfinance_showlast5dayscollectioninareawisecollectionlist`. **Globals:** `branchlevelReport_branchconnString`/`…dbusername`/`…dbpassword`, `sevendayscollectiondetails_list`, `SevenDaysCollectionJSON` (reset). |

**Purpose.** A single front end for picking the branch, line and period (or weekday and loan types) before opening an admin list or report on the chosen branch DB. It can report on a different branch than the one the device is working in.

**UI.**
- **"Select Branch"** (dark-green header) with read-only `branchnameet`; tap opens the branch picker. Shown only to master-DB users with multi-branch access.
- **"Select Line"** with read-only `linenameet`; tap opens the line picker, but only when there is more than one line. The first line is preselected.
- **From-date row.** Label `ds_fromdatelabelid` (XML "Date" / "தேதி", relabelled per request code), value `ds_fromdateid`. Tap opens a DatePicker. Format dd-MM-yyyy.
- **To-date row.** Label `ds1_todatelabelid`, value `ds1_todateid`, same behaviour. Both dates default to today.
- **7DC block `lytsevendayscollectionid`:**
  - Checkboxes "Daily Collection", "Weekly Collection" and "Monthly Collection", and button "SHOW 7DC".
  - Weekday spinner `spcollectiondays`: Tamil weekday names ஞாயிறு…சனி, giving WeekDayId = position+1 (1 = Sunday … 7 = Saturday).
  - The weekday spinner is shown only when Weekly is ticked and the line's `CollectionBreakString` is "W" or "ww". Unticking Weekly sets WeekDayId to −1.
- **`lyttodaycollectionabstract`:** a single "SHOW" button.
- **`lytweeklyanalysis`** (304 only):
  - Spinner `spinner` in dialog mode, listing the Sundays of the last 175 days.
  - Picking a Sunday sets thittamdate = todate = that Sunday, and fromdate = Sunday − 6.
  - Buttons "Collection Analysis", "Thittam", "Patti", "Print".
- A centred ProgressBar `branchandfinanceselection_pbarid` with step text `…_progresstextid` shows while lines load.
- **Title:** "Select Branch and Finance" on a master DB, otherwise "Select Finance".

**Menu / actions.** No options menu. Buttons behave as listed in step 5.

**How it works.**
1. **`onCreate`.** Opens SQLite. Calls `loadcollectiondayslist()` and `getbranchnames()`, which builds the branch list and starts `ASYNCGetFinancelist`. Reads `requestcode`/`menuname` and shows the blocks and labels for that code (table). Sets fromdate = todate = today. For a branch-DB or single-branch user it hides the branch field, sets `branchlevelReport_*` ← `branch*`, and immediately shows the cached lines (`fetchloadfinancelist`).
2. **`getbranchnames()`.**
   - Master DB with multi-branch access: reads local `BranchConnStrs` order by ShowId. The default connection is row 0; the row whose `DBUserId` equals `globalvars.branchdbusername` (the current branch) replaces it. The connection used is `appconstr` + DBUserId + DBPassword.
   - Otherwise: one pseudo-branch (BId 1, name = current line name, the device's branch connection).
   - Inside its loop it starts `ASYNCGetFinancelist` once **per branch entry**.
3. **`ASYNCGetFinancelist`.** Shows the progress bar with steps "Connecting", "Load Finance List", "Load LoanMethods", "Soft Settings".
   - Clears `SelectedBranchFinanceDetails`.
   - Runs `GetAllowedFinances @IMEI='<device_id>'` and inserts each row (FinId, FinName, address, PinCode, RegistrationNo, YearFrom, BooksFrom, ShortName, MobileNo, PhoneNo, EmailIdentifier, FinMailingName, CashLedgerId, AdminMobileNos, smsidentifier, BankLedgerid, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString, IsWeeklyFinance).
   - Multi-branch users only:
     - It reloads local `LoanMethods` from that branch: Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays, DueSupportAmount.
     - It reads `Select SSId,SSDesc,SSValue,Datevalue,BitValue from SoftSettings` into `softsettingsvalue.selectedfinance_*`: SSId 14 loan-closure monitoring, 20 party number in list, 34 loan-status method, 45 penalty, 46 savings, 39 last-5-days. These are persisted to the matching prefs.
   - Post: no lines → toast "Authentication Failed" and the line is cleared. Otherwise `fetchloadfinancelist()` (reads local `select FinId, FinMailingName, CollectionBreakString, IsWeeklyFinance from SelectedBranchFinanceDetails`) preselects the first line and resets the three checkboxes.
4. **Pickers (`onActivityResult`, resultCode 1):**
   - 9905 (`constantvalues.branchnamerequestcode`): switches the report connection to that branch and reloads its lines.
   - 9906 (`linenamerequestcode`): `defaulttables.getbranchfinanceobj(SelectedBranchFinanceDetails where finid)` sets LineName, IsWeeklyFinance and CollectionBreakString, and resets the checkboxes.
   - 2001/200/300/400/500: weekday result (`valuemember`) for 114 / 9011 / 9012 / 9021 / 9022.
   - 100 opens `closableloanssearchlistact`, but nothing in this class requests 100, so this is dead code.
5. **Request codes.** "SHOW" (`lyttodaycollectionabstract`) routes by code. "Weekly line" means the selected line's `IsWeeklyFinance`; for those codes a weekly line first opens the "Collection Day" picker (`defaulttables.fetchweekdayslist`, values 1–7), and a daily line passes `weekdayid` −1.

   | rc (constant) | Drawer item | Fields shown | Target and extras |
   |---|---|---|---|
   | 9001 (`sevendayscollection_requestcode_frommenu`) | `nav_sevendayscollection` | "From Date", "To Date", 7DC block | `ASYNCGet7DaysCollection` → `collectionarealistforsevendayscollection` (`calledfrom` "customerbranch", `passedfinanceid`, `LineName`) |
   | 9004 (`dailypatti_requestcode`) | `nav_allfinancedailypatti` | From/To, SHOW | `dailypattinew_act` (`passedfinanceid`, `LineName`, `fromdate`, `todate`) |
   | 9006 (`simplethittam_requestcode`) | `nav_simplethittam` | From/To, SHOW | `simplethittamlist_act` (same four extras) |
   | 9021 (`partiestobereviewed_requestcode`) | `nav_closedandrenewedloans` | From/To, SHOW | weekly line → picker rc 400 (also passes the dates); → `closed_renewedloanslist_act` (`selectedfinanceid`, `weekdayid`, `fromdate`, `todate`, `calledfrom` "closedloans") |
   | 9022 (`newpartyloans_requestcode`) | `nav_newpartyloans` | From/To, SHOW | weekly line → picker rc 500; → `newpartiesnewloanslist_act` (`financeid`, `weekdayid`, `fromdate`, `todate`) |
   | 9024 (`attendance_requestcode`) | `nav_attendance` | From/To, SHOW | `ASYNCDataforAttendance` → `executiveattendance_displayboard` (forResult 9007, `actiontype` 9007) |
   | 9025 (`daywisepending_requestcode`) | `nav_Daywisepending` | "From Date", "Report Date", SHOW | `daywisependingreport_act` (`selectedfinanceid`, `weekdayid` −1, `fromdate`, `todate`) |
   | 113 | `nav_loanparties` | SHOW | `partydetails_searchlist` (`passedfinanceid`, `LineName`, `menuname`) |
   | 114 | `nav_loans` | SHOW | daily line → `loanslist_act` (`passedfinanceid`, `LineName`, `isweeklyfinance`=false, `weekdayid` 0, `calledfrom` 2); weekly line → picker rc 2001 → same with `isweeklyfinance`=true and the chosen `weekdayid` |
   | 115 | `nav_loanscollection` | SHOW | `editing_collectionlist_act` (`selectedfinanceobj` = Serializable `financemaster_data`, `calledfrom` 9908) |
   | 9010 (`loanclosurerequestfrom_menu`) | `nav_LoanClosures` | SHOW | recreates and clears `closableliveloans` → `loanclosoureentry` (`finidforloanclose`, `IsWeeklyFinance`) |
   | 9012 (`prevsavingclosures_requestfrom_menu`) | `nav_prevsavingclosure` | SHOW | weekly line → picker rc 300; → `prevsavingclosure_list_act` (`finidforloanclose`, `weekdayid`) |
   | 9013 (`savingaccountclosure_requestfrom_menu`) | `nav_SavingsClosures` | SHOW | `closablesavings_list` (`finidforloanclose`, `weekdayid` −1) |
   | 304 (`weeklythittam_requestcode`) | `nav_varathittamanalysis` | Sunday spinner and 4 buttons (no SHOW) | "Collection Analysis" → `weeklycollectionanalysis_act` (`thittamdate`, `financeid`). "Thittam" → `simplethittamlist_act`, "Patti" → `dailypattinew_act`, "Print" → `varathittam`; each gets `passedfinanceid`, `LineName`, `fromdate`, `todate`. |
   | 9002 (`todaycollectionabstract_requestcode_frommenu`) | none (unreachable) | From/To, SHOW | `todaycollectionabstractlist` (`passedfinanceid`, `LineName`, `sqlDate`=fromdate) |
   | 9023 (`dailythittam_requestcode`) | none (unreachable; the drawer uses `refreshorreloadfinancelist` calledfrom 3) | "Thittam Date", SHOW | `singlefinancedailythittam_act` (`passedfinanceid`, `LineName`, `fromdate`) |
   | 9005 (`periodicloans_requestcode`) | none (unreachable) | From/To, SHOW | `periodicloareportlist_act` (four extras) |
   | 9011 (`prevclosedloans_requestfrom_menu`) | none (unreachable) | SHOW | weekly line → picker rc 200; → `prevloanclosure_list_act` (`finidforloanclose`, `weekdayid`) |

6. **`ASYNCGet7DaysCollection`** (ProgressDialog "Working"), on the report connection:
   - **a. Sub-areas.** Recreates `SelectedFinanceSubAreaDetailsALL` and `SelectedFinanceSubAreaDetails`. Inserts every row of `GetAllowedFinanceSubAreas @IMEI='<device_id>'` (FinId, SubAreaNo, SubAreaName) into …ALL.
   - **b. Daily ticked.** `exec [APP_Get7daysCollectionfordailyloans] @EndDate='<fromdate>', @Daystoadd=0, @FinId=<line>`.
   - **c. Weekly ticked.** `exec [APP_Get7daysCollectionforweeklyloans] …` when WeekDayId < 0. Otherwise `exec [APP_Get7daysCollectionforweeklyloanswithWeekdayid] @WeekdayId=<n>, @EndDate=…, @Daystoadd=0, @FinId=…`.
   - **d. Monthly ticked.** `exec [APP_Get7daysCollectionforMonthlyloans] …`.
   - **e. Row mapping.** Each row becomes a `sevendayscollectiondata`: UdLedgerId, CWLedgerId (not set for monthly), MCLoanId, FWUDMCLoanId, FinId, PartyLedgerId, SubAreaNo, LedgerName, MCLoanDate, MCLoanAmountCollectable, PreCollected, ClosingDate, IsNIP, LoanStatus, ReportIdentification, Tag, IsAttentionRequired. `loanmethodname` is "d", "w" or "m".
   - **f. Day columns.** `Day1`–`Day7` are result columns 18–24 (amounts). `Day1date`–`Day7date` are parsed from those columns' **names**: yyyy-MM-dd for the daily SP, dd-MM-yyyy for the weekly and monthly SPs. All rows go into `globalvars.sevendayscollectiondetails_list`.
   - **g. Post.** On error: dialog "Error Info" with "<source line number>.<exception message>"; OK closes the screen. Otherwise, for each sub-area of the line in …ALL it counts that area's rows. If the count is above 0, or SoftSettings 42 `showzeropartiessubareasin7dayscollectionreport_18` is set, it inserts `"<SubAreaName>-<count>"` into `SelectedFinanceSubAreaDetails`. Then it opens `collectionarealistforsevendayscollection`.
7. **`ASYNCDataforAttendance`.** Disables SHOW and shows the progress bar ("Connecting", "Loading Employee Names", "Loading Attendance Dates", "Loading Attendance Details", "Completed").
   - Staff: `select LedgerId,LedgerName,designationid from tblledgers where ScreenType=8 and designationid in (1,2) and desigstatusid=1 order by LedgerName` → `chitcol_parties_data`.
   - Dates: `select IntCalDate from TwenDuration where IntCalDate >='<from>' and IntCalDate <='<to>' order by IntCalDate` → `chitcol_biddings_data`.
   - Attendance: `exec CollectionDayandCollectionExecutiveforapp @FromDate='<from>', @ToDate='<to>'`, mapping (attdate, finname, ledgerid) → `employeeattendancedataobj`.
   - The three lists are stored in static fields of `executiveattendance_displayboard`, which then opens. Errors are only kept in `ErrorText`, so the board opens with whatever loaded.

**Functions.**
- `onCreate()`: widget binding, per-code layout, date defaults, listeners, title.
- `getbranchnames()`: branch list and default report connection; starts line loading.
- `fetchloadfinancelist()`: cached lines from `SelectedBranchFinanceDetails`; preselects the first.
- `loadcollectiondayslist()`: Tamil weekday names; also builds month days 1–28 (`MonthDaysList`, never used). `LoadWeekdaysincollectiondaysspinner()`: binds the weekday spinner.
- `onActivityResult()`: branch, line and weekday picker results (step 4).
- Branch/line field click handlers: open `spinnersearch_list` (title, `searchlist` of `spinnersearch_data`).
- Weekly checkbox listener: shows the weekday spinner for "W"/"ww" lines; unticked → −1.
- Date text click handlers: DatePickerDialog for from and to.
- "SHOW 7DC": captures the three checkboxes and runs `ASYNCGet7DaysCollection` (or opens `collectionarealistforloanpartyandloans` when `calledfor`="loanparties").
- "SHOW": request-code router (step 5).
- 304 button handlers: four targets.
- `ASYNCGetFinancelist`, `ASYNCGet7DaysCollection`, `ASYNCDataforAttendance`: see steps 3, 6 and 7.

**Rules & edge cases.**
- **Brief correction:** seven-days collection is requestcode 9001. Only `navigationdrawactivity` starts this screen, so 9002, 9005, 9011 and 9023 are unreachable. 9010 opens `loanclosoureentry`; `closableloanssearchlistact` (rc 100 result) is never reached. 304 shows four buttons, not a single route.
- **Dead `calledfor` branches.** `calledfor`="loanparties" → `collectionarealistforloanpartyandloans` (`passedfinanceid`, `LineName`, `WeekDayId`) is never passed. Neither is a `calledfor` ending "sevendayscollectionforexecutive", which would auto-run 7DC for the current line and also has a date-text bug (`mDay + "-" + mMonth + "1-" + mYear`).
- **7DC dates.** The 7-day window ends at the "From Date"; the "To Date" is shown but unused.
- **WeekDayId starts at 0, not −1.** With Weekly ticked on a non-"W" line (spinner hidden), the `…withWeekdayid` SP is called with `@WeekdayId=0` until Weekly has been unticked once.
- **SSId 34 read bug.** It reads column `NumericValue`, which the SoftSettings SELECT does not return. This most likely throws, aborting the loop before 39/45/46 and before the prefs are applied.
- **Concurrency.** `getbranchnames()` starts one `ASYNCGetFinancelist` per branch; they run serially and all reload the same connection.
- **Injection risk.** Sub-area names are inserted into SQLite by string concatenation, so an apostrophe breaks the insert. All remote SQL is concatenated too.
- **Branch credentials.** Connections use the branch credentials cached in local `BranchConnStrs` (plaintext).

## Branch selection & branch re-sync — `refreshorreloadbrancheslist`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`); manifest label `@string/app_name` ("FinSoft II"); edge-to-edge. |
| **Layout(s)** | `activity_refreshoreloadfinancelist.xml` (shared with the line list; the date row stays hidden). Row `financelistrow.xml` via `branchlistadabpter`. Menu `menu_reloadrefreshbranches`. |
| **Opened from** | `navigationdrawactivity` `nav_selectbranch` (forResult 9025 `branchselectionraised`). Visible only on a master DB with multi-branch access. |
| **Opens** | `refreshorreloadfinancelist` (`calledfrom` 1) after a successful branch sync, then finishes. |
| **Data** | **Master DB** (`MasterDBconnString`): `BranchMobileDevlicesAllowed`, SP `GetAllowedBranchsDetails`, SP `BRANCH_SetReceivedDeviceasPrimeDevice` (unused). **Selected branch DB:** `FinanceAccessAllowed`/`MobileDevlicesAllowed`/`UserRoles`/`tblLedgers`, `SMSTemplates`, `AndroidMenus` ⟕ `tblSalesDetailsforAndroid`, `userroles`, SP `getCashandBankLedgers`, SP `Get_LedgersforQuickTransactions`, `SpecialAccessRules`, `SpecialAccessAllowed`, `LoanMethods`, `tblGroups`, `LoansAllowed`, `SoftSettings`, `venussoftsettings`, `tblLedgers` (executives). **Local:** rewrites `BranchConnStrs`, `CollectionExecutives`, `LoanMethods`, `tblGroups`, `LoansAllowed`, `tblSalesDetailsforAndroid`, `SMSTemplates`, `userroles`, `CashBankLedgers`, `qatledgers`, `SpecialAccessRules`, `SpecialAccessAllowed`. Only clears `FinanceDetails`, `BranchAccessAllowed`, `tblSTLoans`, `tblOFLoans`, `tblLedgers`, `tblFinwisePartners` and the offline tables. **Prefs:** `BranchId`, `BranchName`, `dbidentificationno`, `branchconnString`/`branchdbusername`/`branchdbpassword`, `FinanceName`, `AdminMobileNos`, `CashBookLedgerId`, `appusername`, `FinanceCashBookLedgerId`, `userroleid`, `isFullAccessAllowed`, `isbranchusercontrolallowed`, and the soft-setting keys listed in step 4. |

**Purpose.** For a multi-branch master DB: the user picks the branch DB the device will work on. The app then downloads that branch's users, rights, ledgers and settings and asks the user to pick a line.

**UI.**
- **Title:** `"Branch:<pref BranchName>"` (default "Branch not selected").
- **List:** `financelistid` with one row per allowed branch (branch name only). Tap a row to select it.
- **Dialogs:** a ProgressDialog "Loading Branches", then a horizontal progress dialog "Sync Begins ..." whose steps are listed in step 4.

**Menu / actions.** "Refresh/Load Branch list" (`action_Refresh`) re-runs `ASYNCGetbranchlist`. Row tap → `onContactSelected`.

**How it works.**
1. **`onCreate`.** Opens SQLite and ensures `FinanceDetails` and `BranchConnStrs` exist. Binds the adapter (`recyclerviewdivider`) and runs `ASYNCGetbranchlist`.
2. **`ASYNCGetbranchlist`.**
   - Clears local `FinanceDetails`, `BranchAccessAllowed` (created if missing) and `BranchConnStrs`.
   - On the **master** DB it runs `select count(IMEI) as imeicount from BranchMobileDevlicesAllowed Where IMEI='<device_id>'`, which sets `isdeviceadded`.
   - It runs `GetAllowedBranchsDetails @IMEI='<device_id>'` and inserts each row (BId, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr, dbidentificationno, ShowId) into local `BranchConnStrs`.
   - Post:
     - Error → "Error..." dialog with the message. OK closes the screen; Cancel stays.
     - Device not added → `ASYNC_checkisdbhasadminuser`.
     - 0 branches → dialog titled with the business name, "No Branches Allowed"; OK closes the screen.
     - Otherwise → `fetchandloadbranchlist()` (local `BranchConnStrs` order by showid).
3. **`onContactSelected(branch)`.** Sets the selected branch, then re-syncs; nothing asks for confirmation.
   - **Globals:** `BranchId`, `BranchName`, `selbranchconnString`=`appconstr`, `selbranchdbusername`/`password`.
   - **Prefs:**
     - `BranchId`, `BranchName`, `dbidentificationno` (from `globalvars`, not the branch row), `branchconnString`, `branchdbusername`, `branchdbpassword`.
     - `FinanceName`="Finance Not Selected" (the global `finid` is set to 0 in memory only).
     - `AdminMobileNos`, `CashBookLedgerId`, `appusername`, `FinanceCashBookLedgerId`: re-saved from the current globals.
   - **Local deletes:** `MobileAppCollData`, `LastUpload`, `NewParties`, `NewMCLoans`, `SubAreaDetails`, `ExpensesEntries`, `FinanceDetails`, `CollectionExecutives`.
   - It then runs `ASYNCSettings`.
4. **`ASYNCSettings`.** It works on the selected branch DB. Its progress steps are "Clearing temp data", "Connecting", "Sync User rights", "Sync Loan Methods", "Account Groups...", "Sync Soft Settings", "Sync Collection Executives" and "Sync Completed".
   - **a. Clear.** It creates (if missing) and clears the local tables listed under Data.
   - **b. Active-user check.** `select top 1 x.IMEI, userroleid, c.isFullAccessAllowed, d.LedgerName from FinanceAccessAllowed x, MobileDevlicesAllowed y, UserRoles c, tblLedgers d where d.LedgerId=y.LId and y.userroleid=c.URId and x.IMEI=y.IMEI and y.isActive=1 and x.IMEI='<device_id>'`. This sets `isactiveuser`, `APPUserName`, `app_userroleid` and `isFullAccessAllowed`, saved as prefs `userroleid`/`appusername`/`isFullAccessAllowed`. (JADX shows an early `return` here; smali loops normally.)
   - **c. If the user is active, it downloads:**
     - `SMSTemplates` (STId, SMSTitle, SMSDesc).
     - Role menu rights: `Select mnuname, mnudisplayname, UserLevelId, a.mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState, mnustyle from AndroidMenus a left join tblSalesDetailsforAndroid b on a.mnuid=b.mnuid and b.UserLevelId=<role> where a.isAllowed=1 and isonlyformasterdb='<true/false>' order by mnuId` → local `tblSalesDetailsforAndroid`.
     - `userroles` (URId, URName, isvisible, DisplayIndex, isFullAccessAllowed).
     - `exec getCashandBankLedgers @URId=<role>` → `CashBankLedgers` (LedgerId, LedgerName, Finid, Alloted, LedgerType, istobeVerified).
     - `exec Get_LedgersforQuickTransactions` → `qatledgers` (TTId, FinId, AType, LedgerId, LedgerName, isExecutiveLevelExpensesLedger, isExecutiveLevelReceiptsLedger).
     - `SpecialAccessRules` (SARId, SAName, ValueType, isAllowed).
     - `SpecialAccessAllowed where UserLevelId=<role>`; this rebuilds `globalvars.specialaccessrules` (ids 1–13 here).
     - `LoanMethods` (all columns), `tblGroups` (GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId), `LoansAllowed` (LId, LoanIndex).
   - **d. SoftSettings** (`Select SSId,SSDesc,SSValue,Datevalue,bitvalue,NumericValue from SoftSettings`). Each SSId maps to a `softsettingsvalue` field:
     - SSId 1–20: 1 `nipbreakforweeklythittam_14`, 2 `nipbreakformonthlythittam_15`, 3 `divisiblevalueforthittam_16`, 4 `businessnameinapp`, 5 `isSendSMSonDataDownload_32`, 6 `isSendSMSonDataUpload_33`, 7 `showdashboardcashabstract_19`, 8 `currentfinancialyearid_20`, 11 `collectionreportprintingmethod_21`, 12 `deletedataaftercashbookverification_37`, 14 `enablemoniteringonloanclosoure_5`, 15 `reportingemailaddress`, 18 `partiesimagepath`, 19 `idproofimagepath`, 20 `showpartynoincollectionlist_6`.
     - SSId 21–43: 21 `enablepartyimageloading`, 22 `enableidproofimageloading`, 23 `enablevisitcheckup_6a`, 24 `idproof_file_upload_url`, 25 `party_image_file_upload_url`, 26 `daily_report_file_upload_url`, 27 `email_sending_url`, 30 `allowoveralllistdownload_4`, 31 `strictontwocollectionexecutives_3`, 32 `enablemoniterondownloadandupload_1`, 34 `loanstatusmethod_10`, 36 `enableondaycollection_27`, 37 `enablemobilenoinnewloans_28`, 38 `enableremarksinnewloans_29`, 39 `showlast5dayscollectioninareawisecollectionlist_7`, 40 `enableuserdefinedcustomernumberinnewcustomercreationscreen_30`, 41 `showloanmethodinappcollectionlist_9`, 42 `showzeropartiessubareasin7dayscollectionreport_18`, 43 `makeallvaluesnecessaryinnewpartycreationscreen_31`.
     - SSId 45–69: 45 `ispenaltyallowedincollectionscreen_12`, 46 `issavingscollectionallowedincollectionscreen_13`, 47 `fivedayscollectiondetailsmethod_8`, 48 `enableIIidproofimageloading`, 49 `allowdecimalinamounts`, 50 `smsmethod`, 51 `iszerointerestallowed_26`, 53 `noofdaysallowedinstaffcashbook_17`, 58 `noofbackdaysallowedfordownloaddata_2`, 61 `Enablefulloptioonsincollectionlist_11`, 62 `MaximumLoanAmountAllowed_23`, 63 `MaximumnoofloansallowedwhilenewpartycreationORnoofloansallowedforallparties_25`, 64 `enableMaximumAmountLoanAllowed_22`, 65 `IsRestrictinEnabledonNoofLoansAllowed_IndividualRestriction_24`, 68 `sendsmsoncashreceipts_34`, 69 `sendsmsontodaycollectionverification_35`.
   - **e. `venussoftsettings`** (`VSSId`): 1 `vss_branchname` (CharValue), 22 `vss_executivehandlingmethod` (NumericValue), 23 `vss_enablelivelocationduringcollectionentry`, 24 `vss_enablestloansincollection`, 25 `vss_enableofloansincollection`.
   - **f. Persist.** All of the above are written to their same-named pref keys, plus `FinanceGroupName`, `AllowOverallListDownload`, `StrictonTwoCollectionExecutives`, `isEnableMoniteringonLoanclosoure` and `enablevisitcheckup`. SSIds 63, 64, 65, 68 and 69 are not saved to prefs. Current static values of `disabletodayCollectionforIndividualcollectionEntry`, `enablelinewisestaff`, `OFIntRate` and `STLIntRate` are re-saved.
   - **g. Collection executives.** `select LedgerId,LedgerName,FinId from tblLedgers where ScreenType=8 and designationid=1 and desigstatusid=1` → `CollectionExecutives`.
   - **h. Branch-user control.** Master DB with multi-branch access: `select isbranchusercontrolallowed from BranchMobileDevlicesAllowed where IMEI='<device_id>'` on the master DB → pref `isbranchusercontrolallowed` (default false).
   - **i. Post.**
     - Not an active user → "Info": "Permission Denied..\nThis device is not added in selected branch\nKindly try with Select Line and then do Sync"; OK closes the screen.
     - Otherwise → "Sync Completed..\nKindly Select Your Line...."; OK opens `refreshorreloadfinancelist` (calledfrom 1) and finishes.
5. **`ASYNC_checkisdbhasadminuser`** ("Verifying User.."). On the master DB it runs `select isnull(IMEI,0) as AndroidDeviceId from BranchMobileDevlicesAllowed Where isbranchusercontrolallowed=1`.
   - If any IMEI is present: "Venus Solutions Welcomes You" / "This device has no permission to access\n Kindly contact Admin".
   - Otherwise it asks "Do you want to save this device as branch level admin device" with Cancel and OK. **Both buttons do nothing**, so `ASYNCSaveAdminDevice` (`BRANCH_SetReceivedDeviceasPrimeDevice @AndroidDeviceId='<device_id>'`, toasts "Device Added Successfuly\nClose and reopen the app" / "Unable to add thils Device\n contact VENUS SOLUTIONS, 9790070227\n9655842840\n9790070226") is never executed.

**Functions.**
- `onCreate()`: table setup, adapter, starts `ASYNCGetbranchlist`.
- `fetchandloadbranchlist()`: local `BranchConnStrs` → list.
- `onContactSelected(branchlistdata)`: switches branch, wipes local data, starts `ASYNCSettings`.
- `onCreateOptionsMenu` / `onOptionsItemSelected`: refresh.
- `whiteNotificationBar`: light status bar.
- `ASYNCGetbranchlist`, `ASYNCSettings`, `ASYNC_checkisdbhasadminuser`: see steps 2, 4 and 5. `ASYNCSaveAdminDevice`: unused.

**Rules & edge cases.**
- **Silent data loss.** Selecting a branch deletes unsynced offline collection data without warning.
- **Stale line id.** Pref `FinId` is not reset (only `finid` in memory and `FinanceName`). When the hub next resumes (`DisplayIndexData`) or restarts, it reloads the old line id under the name "Finance Not Selected" if the user left the line list without choosing.
- **Ledger tables left empty.** `tblSTLoans`, `tblOFLoans`, `tblLedgers` and `tblFinwisePartners` are cleared but not refilled here; they are empty until Sync Resources runs.
- **Errors are swallowed.** Exceptions after the active-user check are only logged (`iserroroccured` is never set). A partial sync still ends with "Sync Completed".
- **Wrong password argument.** `ASYNC_checkisdbhasadminuser` passes `MasterDBusername` as the password, so it works only if the URL already carries the credentials.
- **Plaintext credentials.** Branch DB credentials from `GetAllowedBranchsDetails` are stored in plaintext in local `BranchConnStrs` and in prefs (values not reproduced here).
- **Table-shape mismatch.** `onContactSelected` creates `CollectionExecutives` with 2 columns when it is missing. `ASYNCSettings` later inserts 3 columns with `db.insert`, which then fails silently.
- **Hub refresh ordering.** The hub gets its 9025 result as soon as this screen finishes, before the line is chosen, and fills the header with the old in-memory line name. The newly chosen line name reaches the drawer header only on the next `syncsettings()` or 9028 result; the toolbar title updates on resume.

## Line (finance) list & line chooser — `refreshorreloadfinancelist`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`), plain manifest entry, edge-to-edge. Behaviour depends on extra `calledfrom`. |
| **Layout(s)** | `activity_refreshoreloadfinancelist.xml`. Row `financelistrow.xml` via `financelistadabpter`. Menu `menu_routelogin` ("Refresh/Load Financelist"). |
| **Opened from** | Hub `nav_selectline` (calledfrom 1, forResult 9028), `nav_allfinancedailythittam` (3) and `nav_financedetalis` (4). `refreshorreloadbrancheslist` after a branch sync (1). `todaycollectionlist` and `todaycollectionabstractlist` (5, forResult 5). |
| **Opens** | `dailythittamforallowedlines` (3). `financelinesaddandedit` (4, forResult 1001). `loanclosoureentry` (2; no caller found). `closableloanssearchlistact` (rc 100 result; dead). |
| **Data** | **Local:** rewrites `FinanceDetails`; clears `SelectedBranchFinanceDetails` (branch DB only, not refilled); drops and recreates `BankLedgers`; clears `MobileAppCollData`, `LastUpload`, `NewParties`, `NewMCLoans`, `SubAreaDetails`, `ExpensesEntries` on line selection; creates and clears `closableliveloans`. **Remote** (`selbranchconnString`): SP `GetAllowedFinances`, `tblLedgers` (Groupid=5), `MobileDevlicesAllowed` + `UserRoles`, SP `SetReceivedDeviceasPrimeDevice`. **Prefs:** `FinId`, `isinventoryenabled`, `MobileUserLedgerId`, `FinanceName`, `AdminMobileNos`, `CashBookLedgerId`, `appusername`, `FinanceCashBookLedgerId`, `DefaultLoanMethodId`, `smsidentifier`, `emailidentifier`, `bankaccountledgerid`, `cutoffdateforloanupdateblock`, `isselectedlineisweeklyline`, `app_userroleid`. |

**Purpose.** Lists the lines (finances) this device may access. Tapping a line makes it the working line, or, depending on the caller, opens daily thittam, opens line editing, or returns the line to the caller.

**UI.**
- **Title:** pref `FinanceName` (default "Finance not selected"); "Select Line" for calledfrom 4 and 5; "Daily Thittam" for 3.
- **Date row** `reportdatelayoutid` (calledfrom 3 only): "Date" / "தேதி" `:` `reportdatetvid`, defaulting to today in dd-MM-yyyy. Tap opens a DatePicker.
- **List** `financelistid`: `FinMailingName` per row. A centred `lineselectionprogressbarid` shows while loading.

**Menu / actions.** "Refresh/Load Financelist" (`action_Refresh`) runs `ASYNCGetFinancelist` for any `calledfrom`. Row tap → `onContactSelected`. The edit icon (`onEditSelected` → `financelinesaddandedit` for full-access users) is never displayed (adapter bug).

**How it works.**
1. **`calledfrom` matrix:**

   | calledfrom | Caller | List source | Row tap |
   |---|---|---|---|
   | 1 | hub "Select Line"; branch list after sync | server (`ASYNCGetFinancelist`) | make the line current (step 3), toast "Cur Finance Changed", finish (no `setResult`) |
   | 2 | none found | nothing loaded (empty until Refresh) | clear `closableliveloans` → `loanclosoureentry` (`findata`) |
   | 3 | hub `nav_allfinancedailythittam` | local `FinanceDetails` | `dailythittamforallowedlines` (`passedfinid`, `listindex`=row, `financelist`=whole list, `reportdate`) |
   | 4 | hub `nav_financedetalis` | server | `financelinesaddandedit` (`passedfinance`) forResult 1001. On result 1 the returned `passedfinance` replaces the matching FinId row. |
   | 5 | today-collection screens (rc 5) | local `FinanceDetails` | `setResult(1, passedfinance)` + finish |

2. **`ASYNCGetFinancelist`.**
   - Clears `FinanceDetails` (and `SelectedBranchFinanceDetails` when not on a master DB).
   - On the selected branch DB it runs `GetAllowedFinances @IMEI='<device_id>'` and inserts the same columns as the hub's first auto-sync.
   - If there is at least one line, it drops and recreates `BankLedgers` from `Select LedgerId, Ledgername, FinId from tblLedgers where Groupid=5`.
   - Post: error → "Error Info" dialog; OK closes. 0 lines → `ASYNC_checkisdbhasadminuser`. Otherwise `fetchandloadfinancelist()` runs (local `FinanceDetails order by ShowId`) and toasts "Done".
3. **Line selection** (calledfrom 1).
   - **Globals:**
     - Identity and books: `finid`, `curfin`, `financename`=FinMailingName, `cashbookledgerid`=CashLedgerId, `AdminMobileNos`, `FinanceCashBookLedgerId`.
     - `APPUserName`=`getLedgerName()` (null), `DefaultLoanMethodId`, `emailidentifier`, `smsidentifier`, `bankaccountledgerid`=BankLedgerid, `cutoffdateforloanupdateblock`=BooksFrom.
     - `isselectedlineisweeklyline` = true when `CollectionBreakString` is "W" or "ww". The `IsWeeklyFinance` column is never loaded into `FinanceDetails`.
     - `isinventoryenabled`.
   - The prefs listed under Data are written.
   - It deletes the offline tables `MobileAppCollData`, `LastUpload`, `NewParties`, `NewMCLoans`, `SubAreaDetails`, `ExpensesEntries`.
4. **`ASYNC_checkisdbhasadminuser`.** Runs `select count(*) as AdminCount From MobileDevlicesAllowed a, UserRoles b where a.UserRoleId=b.URId and isFullAccessAllowed=1`. Any returned row sets "has admin", which leads to "Venus Solutions Welcomes You" / "This device has no permission to access\n Kindly contact Admin". Otherwise it asks "Do you want to save this device as admin device"; OK runs `ASYNCSaveAdminDevice`.
5. **`ASYNCSaveAdminDevice`.** Runs `SetReceivedDeviceasPrimeDevice @AndroidDeviceId='<device_id>'` on the selected branch DB. On success it saves pref `app_userroleid` and toasts "Device Added Successfuly\nClose and reopen the app"; otherwise "Unable to add thils Device\n contact VENUS SOLUTIONS, 9790070227\n9655842840\n9790070226".

**Functions.**
- `onCreate()`: per-`calledfrom` setup; date picker on `reportdatetvid`.
- `fetchandloadfinancelist()`: local `FinanceDetails` → list.
- `onContactSelected(financemaster_data, int)`: per-caller action (matrix).
- `onEditSelected(financemaster_data)`: full-access edit; unreachable.
- `onActivityResult()`: 1001 replaces the edited row; 100 is dead (`curselectedfinance` is never set).
- `onCreateOptionsMenu` / `onOptionsItemSelected`: refresh.
- `whiteNotificationBar`: light status bar.
- `ASYNCGetFinancelist`, `ASYNC_checkisdbhasadminuser`, `ASYNCSaveAdminDevice`: see steps 2, 4 and 5.

**Rules & edge cases.**
- **Silent data loss.** Selecting a line wipes unsynced offline data with no confirmation.
- **Lost user name.** `appusername` is nulled (the pref is removed) because `financemaster_data.LedgerName` is never filled.
- **Ineffective admin check.** `count(*)` always returns one row, so `AdminCount` is never examined. The "save as admin device" offer appears only when the check itself fails (for example a connection error). Accepting it then calls `SetReceivedDeviceasPrimeDevice` with no further client-side authorization, which is a privilege-escalation risk.
- **Pref keys that are never read back.**
  - `cutoffdateforloanupdateblock` is written lowercase while the hub reads `CutOffDateforLoanUpdateBlock`.
  - `app_userroleid` is written while the hub reads `userroleid`.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `areawisecollectionlistadapter` | RecyclerView adapter + `Filterable` | `navigationdrawactivity` | Row `areawisecollectionlistrow`. **Card text:** area name; "Parties" = AreaNoCount; "Marked", "NIP" and "Warn" counts, each column made INVISIBLE when 0; collected-amount chip. **Total rows** (SubAreaNo < 0): background `bg_row_card_total`, colour `totallinecolor`, the NIP label becomes "Paid". **−1/−2 rows** show "collected/collectable". **Click:** `ContactsAdapterListener.onContactSelected(position)`; `onthumnailclicked` is never fired. `getFilter()` (SubAreaName contains) is unused, as the hub has no SearchView. Quirk: the "Paid" label is not reset to "NIP" when a view is recycled for an area row. |
| `financelistadabpter` | RecyclerView adapter + `Filterable` | `refreshorreloadfinancelist` | Row `financelistrow`; shows `FinMailingName`. **Callbacks** (`financelistadapterlistner`): row click → `onContactSelected(finance, position)`; edit icon → `onEditSelected(finance)`. **Bug:** the constructor ignores its `iseditvisible` argument (field never assigned, confirmed in smali), so the icon is always hidden. `getFilter()` uses `getLedgerName()`, which is null for lines (NPE if it were used; it is not). |
| `branchlistadabpter` | RecyclerView adapter + `Filterable` | `refreshorreloadbrancheslist` | Row `financelistrow`; shows `BranchName`; edit icon hidden. Row click → `financelistadapterlistner.onContactSelected(branch)`. It looks up `R.id.finidid`, which is not in the row (null, unused). `getFilter()` (BranchName contains) is unused. |
| `financemaster_data` | Serializable model | hub, both lists, sbf, `defaulttables`, many report screens | One line/finance. **Identity and contact:** `FinId`, `FinName`, `FinMailingName` (display name), `ShortName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `RegistrationNo`. **Books and defaults:** `BooksFrom` (sql Date), `CashLedgerId`, `FinanceCashBookLedgerId`, `DefaultBankledgersid`, `DefaultLoanMethodId`, `AdminMobileNos`, `EmailIdentifier`, `SMSIdentifier`. **Collection:** `CollectionBreakString` ("W"/"ww" = weekly line), `IsWeeklyFinance`, `enableinventory`. **Print customisation** (purpose not visible in this group): `HLinecolor`, `LoanNoSuffix`, `SCopyImage`, `FCopyImage`. **Misc:** `LId`, `LedgerName` (never filled for lines), and two selection flags (`Isselected` boolean and `isselected` Boolean, with overloaded `setIsselected`). `getisWeeklyFinance()`/`isWeeklyFinance()` and `getselected()`/`isIsselected()` are duplicate accessors. |
| `branchlistdata` | Model | `refreshorreloadbrancheslist`, `branchlistadabpter`, sbf | One branch DB from `BranchConnStrs`: `BId`, `BranchName`, `ConStr`, `appconstr` (JDBC URL actually used), `ServerName`, `DatabaseName`, `DBUserId`, `DBPassword`, `FinCashLedgerId`, `CollectionTeamCashLedgerId`, `dbidentificationno`, `ShowId`. |
| `areawisecollectionlistdata` | Model (held in `globalvars.areawisecollectionlistdataList`) | hub, adapter, `tabbedcollectionlistmviewpager` | One dashboard card: `SubAreaNo` (−1 live total, −2 pending/NIP total, −3 on-day total), `SubAreaName`, `AreaNoCount` (loans), `Collected`, `Collectable` (totals only), `MarkedCount`, `NipCount` (shown as "Paid" on total rows), `WarningCount`. |
| `uploadallowedperioddata` | Gson model | hub `ASYNCGetDownloadUploadAllowedDetails`, `uploaddownloadcontroling` | One allowed upload/download date from the `Get_UploadLimitationsDateRange` JSON: `IntCalDate` (java.util.Date, getter `getAlloweddate()`) and `dayid`. Stored sorted, newest first, in `globalvars.uploadperioddateslist`; `uploaddownloadcontroling` makes one tab per entry. |
| `chitcol_biddings_data` | Model | sbf `ASYNCDataforAttendance`, `executiveattendance_displayboard`/`_dashboard` | One attendance-board date (`Attdate`, sql Date) taken from `TwenDuration.IntCalDate`. The class name is left over from a chit-fund module. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_navigationdrawactivity.xml` | `navigationdrawactivity` | **Drawer:** DrawerLayout `drawer_layout`. **Main pane:** CoordinatorLayout with AppBarLayout `appbar` including `addnewloanbar` (Toolbar `toolbar`, style `MyToolbar`); RecyclerView `recycler_view` (area dashboard); bottom TextView `downloaddateidnew` ("Data Download time", bold italic, `side_nav_bar` background). **Side drawer:** NavigationView `nav_view` with header `nav_header_main2` (launcher icon, `txtFinanaceGroupNameId`, `txtFinanceNameid`, `txtLoggedUserNameid`, `txtuserrolenameid`) and menu `activity_navigation_drawer` (Olive icon tint, `mnutextstyle`). |
| `activity_refreshoreloadfinancelist.xml` | `refreshorreloadfinancelist`, `refreshorreloadbrancheslist` | ConstraintLayout `main`. A hidden `reportdatelayoutid` row ("Date" / "தேதி" `:` `reportdatetvid`, sample "01-Jan-2019", brown divider), shown only for daily thittam. RecyclerView `financelistid`. A centred ProgressBar `lineselectionprogressbarid` (gone). |
| `activity_selectbranchandfinance.xml` | `selectbranchandfinance` | **Pickers:** "Select Branch" + read-only `branchnameet`; "Select Line" + read-only `linenameet` (dark-green headers, `readonlybox` style). **Dates:** hidden from-date row (`ds_fromdatelabelid` "Date"/"தேதி", `ds_fromdateid`) and to-date row (`ds1_todatelabelid`, `ds1_todateid`). **Hidden blocks:** `lytsevendayscollectionid` (`chkshowdailyloans` "Daily Collection", `chkshowweeklyloans` "Weekly Collection", Spinner `spcollectiondays`, `chkshowmonthlyloans` "Monthly Collection", `btnshow7dayscollectionid` "SHOW 7DC"); `lyttodaycollectionabstract` (`btnshowtodaycollectionabstract` "SHOW"); `lytweeklyanalysis` (Spinner `spinner`, `btnsAnalysis` "Collection Analysis", `btnselectedweekthittam` "Thittam", `btnselectedweekpatti` "Patti", `btnprintselectedweekpattiandthittam` "Print"). **Loading:** centred ProgressBar `branchandfinanceselection_pbarid` + TextView `branchandfinanceselection_progresstextid`. |
| `areawisecollectionlistrow.xml` | `areawisecollectionlistadapter` | Card `awcl_rowcardid` (`bg_row_card`, elevation 2dp). **Left:** area name `awcl_areanameid` (green, bold); four mini-columns "Parties" `awcl_NoOfPartiesId`, "Marked" `awcl_NoOfmarkedPartiesId` (col `awcl_markedcolid`, amber), "NIP" `niplabelid`/`awcl_NoOfNipPartiesId` (col `awcl_nipcolid`, red), "Warn" `awcl_NoOfWarningPartiesId` (col `awcl_warncolid`, orange). **Right:** collected chip `awcl_collectedamountId` (`bg_due_chip`), a hidden divider, hidden `awcl_collectableamountId`. |
| `financelistrow.xml` | `financelistadabpter`, `branchlistadabpter` | 50dp clickable row: TextView `financelist_financenameid` (17sp, sample "Finance Name") and ImageView `financelist_editimageviewid` (`ic_action_editprofile`, gone). |
| `simple_spinner_dropdown_item.xml` | `selectbranchandfinance` (weekday spinner dropdown) | Android framework layout `android.R.layout.simple_spinner_dropdown_item`; not packaged in the APK. |
| `simple_spinner_item.xml` | `selectbranchandfinance` (Sunday-date spinner, request code 304) | Android framework layout `android.R.layout.simple_spinner_item`; not packaged in the APK. |
| `spinner_item.xml` | `selectbranchandfinance` (weekday spinner items) | A single TextView: 20sp, bold, white (#ffffffff), start gravity, 5dp padding. |

---
[← brain.md](../../brain.md) · Module 8.2 of 20 · Previous: [8.1 App start, registration, device identity, settings and access control](01-app-start-registration-device-identity-settings.md) · Next: [8.3 Offline mode: download, upload and sync services](03-offline-mode-download-upload-sync-services.md)
