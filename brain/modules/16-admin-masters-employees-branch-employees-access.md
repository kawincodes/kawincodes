# 8.16 Admin masters: employees, branch employees, access control, loan methods and finance lines

[← brain.md](../../brain.md) · Module 8.16 of 20 · Previous: [8.15 Finance transactions lists, party/loan ledgers and loan & deposit analysis reports](15-finance-transactions-lists-party-loan-ledgers.md) · Next: [8.17 Inventory module: stock masters, items, customers/suppliers, purchase and stock in hand](17-inventory-module-stock-masters-items-customers.md)

This module holds the Admin-menu master screens for employees, device access, loan methods and line details. **Employees** are account ledgers in the current line/branch database's `tblLedgers` (ScreenType 8, GroupId 29); the active ones are what sync downloads as `CollectionExecutives`. **Mobile Device Access Control** binds each device (the `IMEI` column stores `globalvars.device_id`, the ANDROID_ID) to an employee and user role in `MobileDevlicesAllowed`, with per-device line rights in `FinanceAccessAllowed`; multi-branch groups keep the same data at master-DB level (`BranchEMPLedgers`, `BranchMobileDevlicesAllowed`, `BranchAccessAllowed`, `BranchConnStrs`). **Loan Methods** is the loan-product master (`LoanMethods`) whose amounts act as a template that the new-loan screen scales, and **Edit Line Details** edits one `FinanceMaster` row. All screens work online against SQL Server (jTDS) and none uses SharedPreferences; the list screens also re-fill local SQLite caches that other screens read, and record rights come from the menu-permission object described under `employeesearchlist` (menu names `nav_employee`, `nav_accesscontrol`, `nav_branchemployee`, `nav_branchaccesscontrol`, `nav_loanmethods`, `nav_financedetalis`).

## Employees list — `employeesearchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest theme `@style/AppTheme.NoActionBar` (own Toolbar); not exported |
| **Layout(s)** | `activity_employeesearch.xml` (includes `customersearchlist.xml`) + row `employeesearchrow.xml`; menu `menuemployeesearchlist` |
| **Opened from** | Drawer Admin → `nav_employee` (extra `menuname`="nav_employee") |
| **Opens** | `employeedetails_act`: add from the FAB (`CallType`=1 `requestcodes.PASSEDADDNEWEMPLOYEEREQUESTCODE`, rc 1) or edit from a row tap (`CallType`=2 `Edit_EMPLOYEEREQUESTCODE` + `passedemployee`=`employees_data`, rc 2) |
| **Data** | Remote line/branch DB (`globalvars.selbranchconnString`, `selbranchdbusername`, `selbranchdbpassword`): reads `tblLedgers` (ScreenType=8), `MobileDevlicesAllowed`, `FinanceAccessAllowed`. Local SQLite: `EMPLedgers`, `MobileDevlicesAllowed`, `FinanceAccessAllowed` (each is emptied, then re-filled). Reads globals `isFullAccessAllowed` and `salesdetailsforandroid_datalist`. |

**Purpose.** Browse and search the company's employees (staff ledgers), open one to edit it, or add a new one.

**UI.** Toolbar "Employees" with an Up arrow. The RecyclerView rows (`employeesearchrow`) show the employee name (`empsarch_partynameid`) and the ledger `Tag` (`empsearch_tagid`, usually blank because the app saves Tag as ''). The user-role line stays hidden. The FAB `addnewemployeeid` (+) appears only with AddState. While loading, a modal ProgressDialog shows "downloading, please wait ..." with step messages "Connecting", "Loading Employees", "Loading MobileDevlicesAllowed" and "Loading FinanceAccessAllowed".

**Menu / actions.** `menuemployeesearchlist`: Search (SearchView, always shown, filters on every keystroke), "Show all Employees" and "Show Active Employees" (the default, desigstatusid=1). Back first collapses an open SearchView. A row tap opens edit mode if the user has ViewState; otherwise it shows the Toast "No View Permission". The FAB opens add mode.

**How it works.**
1. `onCreate` reads the `menuname` extra and builds the **menu-permission object** (`curitemaccesslimits`, a `salesdetailsforandroid_data`). With `globalvars.isFullAccessAllowed` all of Add, Delete, View, Update and Show are true. Otherwise it is the entry of `globalvars.salesdetailsforandroid_datalist` (rows of local `tblSalesDetailsforAndroid` for the user's UserLevelId) whose `mnuname` equals the menu name. With no matching row, every flag is false. The FAB is hidden unless AddState. `onCreate` then starts `ASYNCEmployees`.
2. `ASYNCEmployees.doInBackground` (JADX output was broken; the flow was checked in smali):
   - It connects to the line/branch DB. On failure, ErrorText is set to "Connection Error,Please try again...".
   - It then runs one try block. First `delete from EMPLedgers`, then it copies `Select LedgerId, LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid,ScreenType from tblLedgers where ScreenType=8` into local `EMPLedgers`.
   - Next it re-fills local `MobileDevlicesAllowed` from `Select MDAId, IMEI, LId, isActive, userroleid from MobileDevlicesAllowed order by MDAId`, and local `FinanceAccessAllowed` from `Select distinct(IMEI), FinId from FinanceAccessAllowed order by FinId`.
   - Any exception sets ErrorText to the exception message.
3. `onPostExecute` calls `fetchemployeesandotherdetails()` and hides the dialog. That method reads local `EMPLedgers ... where desigstatusid=1 order by LedgerName`, or all rows `order by LedgerName` in show-all mode.
4. `onActivityResult`: `employeedetails_act` always returns resultCode 1, for both add (rc 1) and edit/delete (rc 2). The list patches its in-memory copy, then re-reads local `EMPLedgers`, which the detail screen has already updated. This is how a deleted employee disappears. Branches for (rc 2, result 2) and (rc 3, result 2) exist but nothing ever produces those codes.

**Functions.**
- `onCreate()`: layout and insets, back-press callback, toolbar, permissions, FAB, RecyclerView with `employeesearchadapter` and `customersearchlistdivider`; starts the sync task.
- `lambda$onCreate$1()` (FAB): opens `employeedetails_act` in add mode.
- `onActivityResult()`: merges the returned `employees_data`, then reloads the local list.
- `fetchemployeesandotherdetails()`: local active/all query → `employees_data` list → `notifyDataSetChanged()`.
- `onCreateOptionsMenu()`: inflates the menu and binds the SearchView to `mAdapter.getFilter()`.
- `onOptionsItemSelected()`: show-all/show-active toggles; Home → back.
- `whiteNotificationBar()`: sets light status-bar icons (flag 8192) and a white status bar.
- `onContactSelected(employees_data)`: checks ViewState, then opens edit mode or shows the Toast.
- `ASYNCEmployees`: `onPreExecute` (dialog), `doInBackground` (refreshes the 3 caches), `onProgressUpdate` (shows `values[1]`), `onPostExecute` (loads the list).

**Rules & edge cases.**
- Errors are never shown. The final `publishProgress(ErrorText)` passes one value but `onProgressUpdate` reads `values[1]`, and the exception is swallowed.
- When offline, `EMPLedgers` is deleted before the failing remote query runs. The list therefore turns empty, and so does the active-employee picker of the access-control editor (`defaulttables.getemps` reads `EMPLedgers where desigstatusid=1`).
- `userroleid` is read with `getBoolean` and cached as a Boolean in local `MobileDevlicesAllowed` (confirmed in smali), so the role id collapses to 1/0. No local reader of that column was found.
- Search keeps rows where `LedgerName.contains(query)` or `LedgerName.toLowerCase().contains(query)`. Lower-case queries are therefore case-insensitive; other queries are case-sensitive.

## Employee add / edit — `employeedetails_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); default theme (system title bar); not exported |
| **Layout(s)** | `activity_newemployee.xml` |
| **Opened from** | `employeesearchlist`: FAB (`CallType`=1, rc 1) or row tap (`CallType`=2 + `passedemployee`, rc 2). Also referenced by the unreachable `branchemployeesearchlist`. |
| **Opens** | `spinnersearch_list` titled "Finance Name". Its `searchlist` comes from `defaulttables.fetchfinancelist` (local `select FinId, FinMailingName From FinanceDetails`). Request code 9012 (`constantvalues.fetchingfinancelist_requestcode`). |
| **Data** | Remote line/branch DB: `{call dbo.InsertEmpLedgers(?,…15)}`, `update tblLedgers …`, `delete from tblLedgers where LedgerId=?`. Local: `EMPLedgers` insert/update/delete; reads `FinanceDetails`. Soft setting `softsettingsvalue.enablelinewisestaff` (id 55). |

**Purpose.** Create, modify, activate/deactivate or delete one employee ledger.

**UI.** Title "New Employee" or "Edit Employee". The labels are English only:
- Employee Name (`newemp_employeenameid`), Address Line 1, Address Line 2, City.
- Pincode (`inputType=number`, max 6), Mobile No 1 (max 10, any characters), Mobile No 2 (number, max 10), EMail.
- "Ledger for the finance": a read-only picker `newaccled_finnameid` inside `newemp_financenamelyt`, shown only when `enablelinewisestaff` is on.
- CheckBox "Active".
- Buttons Update ("Update" / "சேமி") and DELETE ("DELETE" / "நீக்கு"), plus a centred ProgressBar.

**Menu / actions.** Update saves. DELETE deletes immediately; there is no confirmation. Tapping the finance field opens the picker.

**How it works.**
1. `onCreate` builds the menu-permission object for `nav_employee`.
   - Edit mode (CallType 2): the fields are filled from `passedemployee`, and Active is ticked when desigstatusid==1. Update is visible only with UpdateState and DELETE only with DeleteState.
   - Add mode (CallType 1): a new `employees_data` is created, DELETE is hidden and Update is shown.
2. Update copies the fields into the object and forces GroupId=29, designationid=1 and ScreenType=8. desigstatusid becomes 1 if Active is ticked, else 2. Then `ASYNCupdateemployee` runs.
3. **Add** calls `dbo.InsertEmpLedgers` with named parameters:
   - Output: `LedgerId` (`java.sql.Types.DOUBLE`), which is saved into the object.
   - Inputs: `LedgerName`; `GroupId` 29; `FinId`; `Tag` ''; `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`; `designationid` 1; `desigstatusid`; `ScreenType` 8.
   - `FinId` is the picked finance when line-wise staff is on, otherwise NULL INTEGER.
   - The row is then inserted into local `EMPLedgers`. FinId and Tag are not copied locally.
4. **Edit** runs `update tblLedgers set LedgerName=?,GroupId=?,FinId=?,Tag=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,MobileNo1=?,MobileNo2=?,EMail=?,designationid=?,desigstatusid=?,ScreenType=? where LedgerId=?`. Tag is written as '' and FinId as NULL unless line-wise staff is on. The local `EMPLedgers` row is then updated (name, group, address, mobiles, e-mail, desigstatusid).
5. **Delete** runs `delete from tblLedgers where LedgerId=?`. If rows were deleted, the local row is deleted too. If 0 rows were deleted, or a SQL error occurred (for example the ledger is in use), the Toast "Unable to delete," appears and the screen stays open.
6. `onPostExecute` calls `setResult(1, {passedemployee, deletedrowscount})` and finishes.
7. Finance picker result (rc 9012, result 1): FinId = `valuemember`; the field text = `displaymember`.

**Functions.**
- `onCreate()`: binds views, sets permissions and the mode, attaches listeners.
- `lambda$onCreate$1()` (DELETE): sets caltype=3 (`Delete_EMPLOYEEREQUESTCODE`) and runs the task.
- `lambda$onCreate$2()` (Update): collects fields and the fixed codes, then runs the task.
- `lambda$onCreate$3()`: opens the finance picker.
- `onActivityResult()`: applies the picked finance.
- `ASYNCupdateemployee`: `doInBackground` (server insert/update/delete plus the local mirror); `onPreExecute`/`onPostExecute` (progress bar, result, finish).

**Rules & edge cases.**
- There is no validation. A blank name or invalid mobile number is accepted; `defaulttables.isValidPhoneNumber` exists but is not used here.
- "Active" is unticked by default. A new employee is therefore saved inactive (desigstatusid 2) and vanishes from the default active-only list unless the user ticks it.
- Edit always overwrites `Tag` with ''. When line-wise staff is off it also overwrites `FinId` with NULL. In edit mode the finance field is not pre-filled.
- Errors on add/edit are swallowed (`printStackTrace`) and success is still returned to the list.
- Downstream use:
  - Active employees (designationid 1, desigstatusid 1) are what `syncresources` copies into local `CollectionExecutives` (LedgerId, LedgerName, FinId). With `enablelinewisestaff` on, executives are filtered by the line's FinId.
  - The employee's LedgerId is the `LId` bound to a device in access control.
  - The employee master has no role field; user roles are assigned per device.

## Mobile device access control — `employeesaccessearchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); theme `AppTheme.NoActionBar`; not exported |
| **Layout(s)** | `activity_employeesearch.xml` + `employeesearchrow.xml` (user-role line made visible); menu `simplemenu` |
| **Opened from** | Drawer Admin → `nav_accesscontrol` (no extras) |
| **Opens** | `mobiledeviceaccescontrol_act` (documented in the boot/registration/settings section): add from the FAB (`calltype` 7011 with an empty `mobiledeviceaccescontrolobj`, rc 7011) or edit from a row (`calltype` 7012 `constantvalues.accessobj_editrequest` + the selected object, rc 7012) |
| **Data** | Remote line/branch DB: `MobileDevlicesAllowed` joined with `tblLedgers`; `tblLedgers` (ScreenType=8); `MobileDevlicesAllowed`; `FinanceAccessAllowed`. Local: `EMPLedgers`, `MobileDevlicesAllowed`, `FinanceAccessAllowed` re-filled; reads `userroles` (URName, DisplayIndex). |

**Purpose.** List every registered device with its employee and user role. Add a device, or open one to change its employee, role, active flag and allowed lines.

**UI.** Toolbar "Mobile Device Access Control". Each row shows the employee ledger name, the device id (IMEI column) on the second line, and the role name (`userroles.URName`) with an edit-employee icon. The FAB is shown with AddState. A translucent `progress_Overlay` covers the list while it loads.

**Menu / actions.** `simplemenu` contains only a hidden Search item, so there is no search. Home → back. A row tap opens the editor after the role-hierarchy check. The FAB adds a device.

**How it works.**
1. `onCreate` builds the permission object for `nav_accesscontrol`. It is computed twice, with the same result. The FAB shows with AddState, and `ASYNCEmployees` starts.
2. `doInBackground` connects to the line/branch DB (on failure: "Connection Error,Please try again...").
   - It first runs `select Lid, IMEI ,a.isActive,userroleid ,Ledgername,MDAId from MobileDevlicesAllowed a, tblLedgers b where lid=ledgerid order by Lid` and builds one `mobiledeviceaccescontrolobj` per row (LId, IMEI, LedgerName, userroleid, isActive, MDAId). Devices whose LId matches no ledger are not listed.
   - It then refreshes the same three local caches as `employeesearchlist`, with identical SQL and the same Boolean `userroleid` bug.
   - Exceptions are stored in ErrorText, which is never displayed.
3. `onPostExecute` calls `notifyDataSetChanged()` and hides the overlay.
4. `onContactSelected` opens the editor if the user has ViewState and either `globalvars.isFullAccessAllowed` or `fetchuserroledispindex(app_userroleid) < fetchuserroledispindex(device's userroleid)`. Otherwise it shows the Toast "ACCESS DENIED". Without ViewState nothing happens.
5. `onActivityResult`: the editor returns its calltype as the resultCode, with extra `curmaobj`.
   - (rc 7012, result 7012): replace the row with the same MDAId.
   - (7011, 7011): insert the new row at the top.
   - (7012, 7013): remove the row (deleted in the editor).

**Functions.**
- `onCreate()`: views, permissions, FAB, adapter (`employeesaccessearchadapter`), starts the task.
- `lambda$onCreate$1()` (FAB): opens the editor in add mode.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: inflate `simplemenu`; Home → back.
- `whiteNotificationBar()`: status-bar styling.
- `onActivityResult()`: list patching by MDAId.
- `onContactSelected(mobiledeviceaccescontrolobj)`: ViewState and role-hierarchy check.
- `ASYNCEmployees`: `onPreExecute` (overlay), `doInBackground` (list plus caches), `onPostExecute` (refresh).

**Rules & edge cases.**
- Role hierarchy: a lower `userroles.DisplayIndex` means a higher rank. A user can open only devices whose role is strictly lower than their own, never equal roles, unless they have full access.
- Inside the editor:
  - The role field is enabled only with special access 7 (`specialaccessrules.isAllowtochangeuserroles_7()`).
  - Its employee picker uses the local active `EMPLedgers` refreshed here.
  - Its line checklist joins local `FinanceDetails` with `FinanceAccessAllowed`.
  - Its "recent request" lookup reads `NewIMEIRequests`.
- Rows are added to the adapter's list from the background thread and the RecyclerView is notified only later, so concurrent layout can crash.
- The adapter opens SQLite and queries `userroles` on every row bind.

## Branch employees (master DB) — `branchemployeelist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_employeesearch.xml` + `employeesearchrow.xml`; menu `menuemployeesearchlist` |
| **Opened from** | Drawer Admin → `nav_branchemployee`, only when `globalvars.ismasterdb && globalvars.iscurrentuserhavemultibranchaccess` |
| **Opens** | `branch_employee`: add from the FAB (`CallType` 1, rc 1) or edit from a row, which needs ViewState (`CallType` 2 + `passedemployee`, rc 2) |
| **Data** | Remote master DB (`MasterDBconnString`, `MasterDBusername`, `MasterDBpassword`): reads `BranchMobileDevlicesAllowed`, `BranchEMPLedgers`, `BranchAccessAllowed`, `BranchConnStrs`. Local: the same four tables; `BranchConnStrs` is dropped, re-created and re-filled. Global `globalvars.isbranchusercontrolallowed`. |

**Purpose.** Group-level staff list for multi-branch installations, open only to devices that have been granted branch user control.

**UI.** Toolbar "Branch Emp-" followed by `globalvars.BranchName`. Rows, FAB and dialog work as in Employees; the step messages are "Loading Employees", "Loading Branch Mobile Devices Allowed" and "Loading BranchFinanceAccessAllowed".

**Menu / actions.** Same as Employees: Search, "Show all Employees", "Show Active Employees". A row tap opens the editor only with ViewState and is silently ignored otherwise. FAB requires AddState.

**How it works.**
1. `doInBackground` connects to the master DB. It runs `Select MDAId, IMEI, LId, isActive, isbranchusercontrolallowed from BranchMobileDevlicesAllowed where IMEI='<globalvars.device_id>'` (built by string concatenation) and copies the row's flag into `globalvars.isbranchusercontrolallowed`.
2. If that flag is true, it re-fills the local tables:
   - `BranchEMPLedgers` from `Select LedgerId, LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid,desigstatusid,ScreenType from BranchEMPLedgers`.
   - `BranchMobileDevlicesAllowed` from `Select MDAId, IMEI, LId, isActive, isbranchusercontrolallowed from BranchMobileDevlicesAllowed order by MDAId`.
   - `BranchAccessAllowed` from `Select distinct(IMEI), BId from BranchAccessAllowed order by BId`.
   - `BranchConnStrs`: `createtableBranchConnStrs`, then `drop table BranchConnStrs`, re-create, then fill from `Select BId, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr,ShowId from BranchConnStrs order by BId`. The local `dbidentificationno` column stays NULL.
3. `onPostExecute`:
   - If allowed, `fetchemployeesandotherdetails()` reads local `BranchEMPLedgers` (active only by default, or all rows).
   - If not allowed, it tries to show the AlertDialog "Info" / "Permission Denied.." whose OK button finishes the screen.
4. `onActivityResult`: the resultCode equals the editor's CallType.
   - (rc 1, result 1): append the employee and call `notifyItemInserted`.
   - (2, 2): replace the row with the same LedgerId.
   - (2, 3) with `noofrowsdeleted` > 0: remove the row.

**Functions.**
- `onCreate()`: toolbar, FAB, adapter (`employeesearchadapter`), permissions for `nav_branchemployee`, starts the task.
- `lambda$onCreate$1()`: opens `branch_employee` in add mode.
- `onActivityResult()`: list patching.
- `fetchemployeesandotherdetails()`: creates the local table if missing and runs the active/all query.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: search and filters.
- `getContextOfApplication()` (static): returns the static `contextOfApplication`.
- `whiteNotificationBar()`: status-bar styling.
- `onContactSelected()`: ViewState gate, then edit.
- `ASYNCEmployees`: `doInBackground` (access check plus the four-table refresh), `onPostExecute` (list or dialog), `lambda$onPostExecute$0` (close the dialog and finish).

**Rules & edge cases.**
- **Crash:** the "Permission Denied.." dialog is built with `getContextOfApplication()`. That static field is never assigned anywhere (checked in smali), so `new AlertDialog.Builder(null)` throws. A device without branch user control crashes instead of seeing the message.
- The flag is not reset before the check. If this device has no row, or the device is offline, the previous process-wide value of `isbranchusercontrolallowed` is reused.
- Every branch's connection data is cached in local SQLite, including the `DBUserId` and `DBPassword` columns, and not only the branches this device is allowed. The Select Branch screen later replaces them via `GetAllowedBranchsDetails @IMEI`.
- Errors are not displayed (same `values[1]` issue as Employees).

## Branch employee add / edit — `branch_employee`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); default theme |
| **Layout(s)** | `activity_newemployee.xml` (the "Ledger for the finance" block stays hidden) |
| **Opened from** | `branchemployeelist`: FAB (`CallType` 1, rc 1) or row tap (`CallType` 2 + `passedemployee`, rc 2) |
| **Opens** | — |
| **Data** | Remote master DB: `{call dbo.InsertBranchEmpLedgers(?,…15)}`, `update BranchEMPLedgers set LedgerName=?,GroupId=?,FinId=?,Tag=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,MobileNo1=?,MobileNo2=?,EMail=?,designationid=?,desigstatusid=?,ScreenType=? where LedgerId=?`, `delete from BranchEMPLedgers where LedgerId=?`. Local `BranchEMPLedgers`. |

**Purpose.** Add, edit, activate/deactivate or delete a group-level (master DB) employee.

**UI.** The title is always "New Employee(Branch)". The form is the same as the employee form, without the finance field. Update is always visible; DELETE is visible in edit mode.

**How it works.**
1. Save works like `employeedetails_act`: GroupId 29, designationid 1, ScreenType 8, desigstatusid 1 or 2 from Active.
2. Add calls `dbo.InsertBranchEmpLedgers` with the same 15 named parameters as `InsertEmpLedgers`, but `FinId` is always NULL and `Tag` is ''. The returned `LedgerId` is used for the local insert.
3. Edit runs the UPDATE above with FinId NULL and Tag '', then updates the local row.
4. Delete runs immediately, with no confirmation. It sets `noofrowsdeleted`.
5. `onPostExecute` calls `setResult(caltype, {passedemployee, noofrowsdeleted})` (1 add, 2 edit, 3 delete) and finishes.

**Functions.**
- `onCreate()`: binds views and sets up the mode.
- `lambda$onCreate$1()`: delete.
- `lambda$onCreate$2()`: save.
- `ASYNCupdateemployee`: `doInBackground`, `onPreExecute`, `onPostExecute`.

**Rules & edge cases.**
- The Update and Delete buttons are not permission-gated, unlike the line-level screen.
- The local row is deleted only when `noofrowsdeleted > 1`, so a normal one-row delete leaves the stale local row until the next refresh. The on-screen list still removes it.
- There is no validation. Active is unticked by default. Errors are swallowed, but a result is still returned.

## Branch mobile device access control — `branchemployeesaccessearchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_employeesearch.xml` + `employeesearchrow.xml` (role line hidden); no options menu |
| **Opened from** | Drawer Admin → `nav_branchaccesscontrol` (master DB plus a multi-branch user only), extra `calledfrom`="true" (unused here) |
| **Opens** | `branchmobiledeviceaccescontrol_act` (boot/registration/settings section): add from the FAB, which needs ViewState (`calltype` 7011 + empty `branchaccess_obj`, rc 7011), or edit from a row (`calltype` 7012 + the selected `branchaccess_obj`, rc 7012). That editor shows the latest `BranchNewIMEIRequests` entry, raised from the Device Identity screen's "Raise Request for Branch Level Access" button, and saves `BranchMobileDevlicesAllowed` and `BranchAccessAllowed`. |
| **Data** | Remote master DB: `BranchEMPLedgers`, `BranchMobileDevlicesAllowed`, `BranchAccessAllowed`, `BranchConnStrs`, plus a join for the list. Local: the same four tables are re-filled. |

**Purpose.** Branch-level counterpart of Mobile Device Access Control. It lists devices allowed at group level and lets an admin open one to set its employee, active flag, branch-user-control flag and allowed branches.

**UI.** Toolbar "Mobile Device Access Control" (same text as the line-level screen). Rows show the employee name and, on the second line, the IMEI. Only the name text is clickable. The FAB shows with AddState for `nav_branchaccesscontrol`, and the overlay is brought to the front while loading.

**Menu / actions.** There is no options menu and therefore no search. `onOptionsItemSelected` only calls super, and with no parent activity the Up arrow does nothing; use Back. Tapping a name opens the editor if the user has ViewState.

**How it works.**
1. `doInBackground` connects to the master DB, then runs one try block:
   - Re-fill local `BranchEMPLedgers`, `BranchMobileDevlicesAllowed` and `BranchAccessAllowed` (same SQL as `branchemployeelist`).
   - `delete from BranchConnStrs`, then re-fill it from the same master `BranchConnStrs` SELECT.
   - Finally `select Lid, IMEI ,a.isActive,Ledgername,MDAId from BranchMobileDevlicesAllowed a, BranchEMPLedgers b where lid=ledgerid order by Lid` builds the `branchaccess_obj` list.
2. `onPostExecute` calls `notifyDataSetChanged()` and hides the overlay.
3. `onActivityResult` expects resultCode 1 with `curmaobj`: rc 7012 replaces the row by MDAId, rc 7013 removes it, rc 7011 appends it.

**Functions.**
- `onCreate()`: toolbar, overlay, FAB, adapter (`branchemployeesaccessearch_adapter`), permissions, starts the task.
- `lambda$onCreate$1()`: FAB → add (gated by ViewState).
- `onOptionsItemSelected()`: calls super only.
- `whiteNotificationBar()`: status-bar styling.
- `onActivityResult()`: list patching.
- `onContactSelected(branchaccess_obj)`: ViewState gate, then edit.
- `ASYNCEmployees`: `onPreExecute`, `doInBackground`, `onPostExecute`.

**Rules & edge cases.**
- **Bug, confirmed in smali:** the `BranchConnStrs` copy loop calls `getString("dbidentificationno")`, but that column is not in the SELECT. jTDS throws "Invalid column name", which aborts the block before the device-list query runs.
  - Whenever the master has any branch row, the list therefore stays empty, and local `BranchConnStrs` is left empty until Select Branch reloads it.
  - When offline, `BranchEMPLedgers` is wiped before the first remote query fails.
- `branchmobiledeviceaccescontrol_act` never calls `setResult` (none in its smali), and nothing starts this screen with rc 7013. As a result none of the `onActivityResult` branches fire, and the list is not refreshed after an add, edit or delete.
- The branch level has no role-hierarchy check and no user-role column; it stores only `isActive` and `isbranchusercontrolallowed`. The adapter's filter body is empty, but it is never reachable.

## Branch employee list clone (unreachable) — `branchemployeesearchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) class that is **not declared in AndroidManifest.xml** and is referenced by no other class (Java and smali checked). Dead code: starting it would throw ActivityNotFoundException. |
| **Layout(s)** | `activity_employeesearch.xml` + `employeesearchrow.xml`; menu `menuemployeesearchlist` |
| **Opened from** | Nothing |
| **Opens** | `employeedetails_act` (FAB with `CallType` 1, rc 1; row with `CallType` 2 + `passedemployee`, rc 2). It edits line-level employees, not branch employees. |
| **Data** | Identical to `employeesearchlist`: line/branch DB `tblLedgers` (ScreenType=8), `MobileDevlicesAllowed`, `FinanceAccessAllowed` → local `EMPLedgers`, `MobileDevlicesAllowed`, `FinanceAccessAllowed`. |

**Purpose.** An older copy of the Employees list, despite its name.

**How it works.** Same sync, list, menu and search code as `employeesearchlist` (methods `onCreate`, `lambda$onCreate$1`, `onActivityResult`, `fetchemployeesandotherdetails`, `onCreateOptionsMenu`, `onOptionsItemSelected`, `whiteNotificationBar`, `onContactSelected`, `ASYNCEmployees`).

**Functions.** As listed above. Each method behaves like its `employeesearchlist` counterpart, except for the differences below.

**Rules & edge cases.** The differences from `employeesearchlist`:
- The permission menu name is "nav_loanscollection", a copy-paste error.
- The FAB is never hidden.
- A row tap has no ViewState check.
- There is no handler for request code 3.

## Loan methods list — `loanmethods_searchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); theme `AppTheme.NoActionBar`. The manifest label is "Account Ledgers" (copy-paste); the toolbar title is set to "Loan Methods". |
| **Layout(s)** | `activity_employeesearch.xml` + row `loanmethods_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer Admin → `nav_loanmethods` |
| **Opens** | `loanmethods`: add from the FAB (extra `actiontype`="new", rc 1 `ADDNEWLOANMETHOD_REQUEST`) or edit from a row, which needs ViewState (`passedmethod`=`loanmethods_data`, `curcaltypeid`=2, rc 2 `EDITLOANMETHOD_REQUEST`) |
| **Data** | Remote line/branch DB: reads `LoanMethods`. Local: `LoanMethods` is created, emptied and re-filled with `insertOrThrow`. |

**Purpose.** List the loan products (loan methods) and open one to edit, or add a new one.

**UI.** Toolbar "Loan Methods". Each row shows the method name (`LoanMethodDesc`) and below it the "Abstract" short code (`ReportIdentification`). FAB with AddState. A ProgressDialog shows "downloading, please wait ..." with the message "Loading Loan Methods".

**Menu / actions.** Search (SearchView, always shown). Back first collapses the search.

**How it works.**
1. `onCreate` builds the permissions for `nav_loanmethods`, sets the FAB, and starts `ASYNCAccountsLedgers`, which loads loan methods despite its name.
2. `doInBackground` connects to the line/branch DB, clears the list, runs `delete from LoanMethods` locally, then runs `Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount from LoanMethods order by LoanMethodId`.
   - Each row is inserted into local `LoanMethods` and added to the list. Ids, counts, grace period and doc fields are read as int; amounts as double.
3. Any exception sets `iserroroccured` and ErrorText = "<stack line no>.<message>" (smali confirms every catch does this). `onPostExecute` then shows the AlertDialog "Error Info", whose OK button finishes the screen. On success it calls `notifyDataSetChanged()`.
4. `onActivityResult`:
   - (rc 1, result 1): append the returned `receiveddata`.
   - (2, 2): replace the row with the same LoanMethodId.
   - (2, 3) with `norowsdeleted` > 0: remove the row and show the Toast "Deleted".
   - (2, 3) with `norowsdeleted` = 0: show the AlertDialog "Transactions found cannot be deleted", titled "தங்கள் கவனத்திற்கு..." (For your attention), with OK.

**Functions.**
- `onCreate()`: toolbar, back callback, permissions, FAB, adapter (`loanmethods_adapter`), starts the task.
- `lambda$onCreate$1()`: FAB → add.
- `onActivityResult()` and `lambda$onActivityResult$2()` (no-op OK button).
- `onCreateOptionsMenu()`: SearchView → adapter filter.
- `onOptionsItemSelected()`: Home → back.
- `whiteNotificationBar()`: status-bar styling.
- `onContactSelected(loanmethods_data, position)`: ViewState gate, then edit.
- `ASYNCAccountsLedgers`: `onPreExecute`, `doInBackground`, `onProgressUpdate`, `onPostExecute`, `lambda$onPostExecute$0` (finish).

**Rules & edge cases.**
- Opening this screen offline wipes the local `LoanMethods` cache before the query fails. New-loan creation (`defaulttables.getloanmthodparticularsforselectedloanmethod`) and the loan-method pickers then find no methods until the next successful open or sync.
- Search upper-cases the query and compares it with the lower-cased description, so only queries without letters match the name. It also matches a case-sensitive `ReportIdentification.contains(query)`.
- The list is ordered by LoanMethodId, while the pickers elsewhere order by ShowId (SNo).

## Loan method add / edit — `loanmethods`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); default theme; title "Loan Methods" |
| **Layout(s)** | `activity_loanmethods.xml` |
| **Opened from** | `loanmethods_searchlist`: FAB (add, rc 1) or row (edit, `curcaltypeid` 2, rc 2) |
| **Opens** | `spinnersearch_list`, twice:<br>• "எழுத்துக்கூலி கணக்கிடும் முறை" (document-charge method), rc 102. List from `defaulttables.fetchdocumentcharges`: 1 "Percentage", 2 "Fixed".<br>• "வசூல் இடைவெளி" (collection break), rc 101. List from `fetchcollectionbreaks`: 1 "தினசரி வசூல் (Daily)" (cbstring "d", interval 1); 2 "வார வசூல் (Weekly)" ("ww", 7); 3 "மாதமிருமுறை" (twice monthly; "m", 30); 4 "மாதந்திர வசூல் (Monthly)" ("m", 30). |
| **Data** | Remote line/branch DB: `{call dbo.InsertLoanMethods(?,…20)}`, `update LoanMethods …`, `delete from LoanMethods where LoanMethodId=?`. Local: **nothing is written**. |

**Purpose.** Define a loan product: the reference loan amount and the matching interest, hand-over, document charge, number and size of instalments, collection frequency and warning/grace rules.

**UI.** One field per row, in screen order:

| Label (EN / TA) | Widget | Column | Parse / required message |
|---|---|---|---|
| SNo / வரிசை எண் | `loanmethods_etshowidid` | ShowId | int; "Please enter SNo" |
| Loan Type / கடன் வகை | `loanmethods_etloanmethoddescid` | LoanMethodDesc | text; "Please enter loan method description" |
| Loan Amount / கடன் தொகை | `loanmethods_etloanamountid` | LoanAmount | double; "Please enter loan amount" |
| Loan Given in Hand / கையில் தரும் தொகை | `loanmethods_etloanamountgivenid` | LoanAmountGiven | double; "Please enter loan amount given in hand" |
| Interest Added / வட்டி கூடுதல் (hidden) | `loanmethods_etintadditionid` | IntAddition | never read on save; 0 for new methods |
| Interest Deduction / வட்டி பிடித்தம் | `loanmethods_etintdeductionid` | IntDeduction | int; "Please enter Interest Deduction" |
| Total Collectable / மொத்த வசூல் தொகை | `loanmethods_ettotamountcollectableid` | TotAmountCollectable | double; "Please enter Total Amount collectable" |
| Collection Break / வசூல் இடைவெளி | `loanmethods_etcollectionbreakid` (read-only picker) | CollectionBreak + CollectionBreakString | id + cbstring (d / ww / m); not validated |
| Collection Breaks to be added / கூட்ட வேண்டிய இடைவெளிகள் | `loanmethods_etcbcountid` | CBCount | int, must be > 0; "Please enter collection break count" |
| Total Installments / மொத்த தவணைகள் | `loanmethods_etnoofinsid` | NoOfIns | int; "Please enter no of instalments" |
| Installment Amount / தவணை தொகை | `loanmethods_etinsamountid` | InsAmount | int; "Please enter no of instalment amount" |
| Warning Days / எச்சரிக்கை நிறமாற்ற நாட்கள் | `loanmethods_etwarningdaysid` | WarningDays | int; "Please enter warning days" |
| Grace Period / சலுகைக்காலம் | `loanmethods_etgraceperiodid` (no numeric inputType) | GracePeriod | int; "Please enter grace period in days" |
| Document Charges Fixation / எழுத்துக் கூலி நிர்ணய முறை | `loanmethods_etdocchargesmethodid` (picker) | DocChargesMethod | 1 Percentage / 2 Fixed; not validated |
| Document Charge / எழுத்துக் கூலி | `loanmethods_etdocchargesid` | DocCharges | int; "Please enter document charges" |
| Abstract / சுருக்கம் | `loanmethods_etreportidentificationid` | ReportIdentification | text; "Please enter report identification" |
| Status / பயன்பாட்டு நிலை (CheckBox) | `loanmethods_chkisvisibleid` | IsVisible | boolean, unticked by default |
| Additional Due Amount / துணைத் தொகை | `loanmethods_etduesupportamountid` | DueSupportAmount | int; "Please enter Due Support Amount" |

Below the fields are the SAVE (`loanmethods_savebuttonid`) and DELETE (`loanmethods_deletebuttonid`) buttons and a centred ProgressBar.

**Menu / actions.** SAVE validates and saves. DELETE deletes immediately; there is no confirmation. Tapping the Collection Break or Document Charges Fixation field opens its picker.

**How it works.**
1. `onCreate` loads the two fixed lists and builds the permissions for `nav_loanmethods`.
   - If the `curcaltypeid` extra is 2 (edit), the fields are filled from `passedmethod`. Amounts are shown as int. The collection-break and doc-method texts are looked up at index id−1 of the fixed lists. SAVE is visible only with UpdateState and DELETE only with DeleteState.
   - Otherwise (add; the FAB passes only `actiontype`), a new `loanmethods_data` is created with IntAddition=0, DELETE is hidden and `curcaltypeid` is set to 1.
2. SAVE calls `setError` on every empty field, using the messages in the table. When all fields are filled it parses them. CBCount must be > 0, otherwise the Toast "Please enter collection break count" appears. Then `ASYNCUpdateLoanmethods` runs.
3. **Insert** (`curcaltypeid` 1) calls `dbo.InsertLoanMethods` with named parameters. `LoanMethodId` is the output parameter: it is registered as `Types.DOUBLE`, read back with `getInt`, and becomes the object's id. The inputs are:
   - `LoanMethodDesc`, `LoanAmount`, `LoanAmountGiven`, `IntDeduction`, `IntAddition`, `TotAmountCollectable`;
   - `CollectionBreak`, `CollectionBreakString`, `NoOfIns`, `InsAmount`, `GracePeriod`;
   - `DocChargesMethod`, `DocCharges`, `ReportIdentification`, `CBCount`, `IsVisible`, `ShowId`, `WarningDays`, `DueSupportAmount`.
4. **Update** (2): `update LoanMethods set LoanMethodDesc=?,LoanAmount=?,LoanAmountGiven=?,IntDeduction=?,IntAddition=?,TotAmountCollectable=?,CollectionBreak=?,CollectionBreakString=?,NoOfIns=?,InsAmount=?,GracePeriod=?,DocChargesMethod=?,DocCharges=?,ReportIdentification=?,CBCount=?,IsVisible=?,ShowId=?,WarningDays=?,DueSupportAmount=? where LoanMethodId=?`.
5. **Delete** (3): `delete from LoanMethods where LoanMethodId=?` sets `norowsdeleted`. A foreign-key error (the method is used by loans) leaves it at 0.
6. `onPostExecute` calls `setResult(curcaltypeid, {receiveddata, norowsdeleted})` and finishes.
7. Picker results:
   - rc 101: `CollectionBreak` = the picked id and `CollectionBreakString` = its cbstring.
   - rc 102: `DocChargesMethod` = the picked id.

**Totals and auto-computation.** This screen computes nothing: there are no TextWatchers and every amount is typed by hand.

The expected consistency rules are not enforced:
- Loan Given in Hand = Loan Amount − Interest Deduction − document charge.
- Total Collectable = Total Installments × Installment Amount.

The method acts as a template that `newloancreation.ChangeLoanValuesbasedonloanamount()` scales to the real loan amount A:

| Value on the new loan | Formula |
|---|---|
| Interest | A × IntDeduction / LoanAmount. If IntAddition > 0, A × IntAddition / LoanAmount is used instead. |
| Total collectable | A × TotAmountCollectable / LoanAmount. When interest is added instead, it is A + interest. |
| Document charges | Fixed: DocCharges. "Percentage": DocCharges / LoanAmount × A, so the charge is proportional to the amount rather than a true percent. |
| Loan in hand | A − interest − document charges. When interest is added, A − document charges. |
| Instalment | Collectable / NoOfIns |

`todaycollectionlist` checks the line's default method: CBCount = 7 makes it call the `DailyCollectionDetailsforweeklyLines` procedure instead of `DailyCollectionDetails`.

**Functions.**
- `onCreate()`: fixed lists, permissions, mode setup, listeners.
- `lambda$onCreate$1()`: document-charge method picker.
- `lambda$onCreate$2()`: collection-break picker.
- `lambda$onCreate$3()`: SAVE validation and parsing.
- `lambda$onCreate$4()`: DELETE (`curcaltypeid`=3).
- `onActivityResult()`: applies the picked values.
- `ASYNCUpdateLoanmethods`: `doInBackground` (procedure, update or delete), `onPreExecute`/`onPostExecute` (progress, result).

**Rules & edge cases.**
- Collection Break and Document Charges Fixation are not validated. A new method saved without them hits a NullPointerException at `getCollectionBreak().intValue()` inside the task. The error is swallowed and result 1 is still returned, so the list shows a phantom row that was never saved. Opening that row later crashes, because the edit pre-fill looks up index null−1.
- Possible crashes on the UI thread:
  - Grace Period accepts non-numeric text.
  - The `numberSigned` fields accept a lone "-", which `Integer.parseInt` rejects with a NumberFormatException.
  - IntDeduction, InsAmount and DocCharges are parsed as int, so decimals are not possible.
  - The edit pre-fill crashes if CollectionBreak or DocChargesMethod is 0 or out of range.
- The hidden IntAddition is never read from the form, so existing values are preserved and new methods get 0.
- "Status" (IsVisible) is unticked by default. `defaulttables.fetchallloanmethods` (the default-method and new-loan pickers) does not filter on it.
- Local `LoanMethods` is not updated on add/edit/delete. Offline features see changes only after the list is reopened or data is synced.
- "மாதமிருமுறை" (twice monthly) has the same cbstring "m" and 30-day interval as Monthly.

## Edit line (finance) details — `financelinesaddandedit`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); default theme; title "Edit Line Details" |
| **Layout(s)** | `activity_financelinesaddandedit.xml` |
| **Opened from** | `refreshorreloadfinancelist`, in two ways, both passing `passedfinance` (`financemaster_data`) with rc 1001:<br>• Drawer `nav_financedetalis` opens it with `calledfrom`=4; tapping a line opens this screen.<br>• The line row's edit icon (`financelist_editimageviewid` → `onEditSelected`), available in any mode, only when `globalvars.isFullAccessAllowed`. |
| **Opens** | `spinnersearch_list`, twice:<br>• "Loan Method", rc 21: `fetchallloanmethods`, local `LoanMethods` ordered by ShowId.<br>• "Bank Ledgers", rc 22: `fetchbankledgerlistforselectedfinance`, local `BankLedgers where FinId=<line>` ordered by Ledgername. |
| **Data** | Remote line/branch DB: `update FinanceMaster …`. Local: `Update FinanceDetails …`; reads `LoanMethods` and `BankLedgers`. |

**Purpose.** Edit an existing line's master data. Nothing can be added here, despite the class name: only UPDATE is implemented.

**UI.** Labelled EditTexts (English only), each mapped to a `FinanceMaster` column:

| Label | Column | Notes |
|---|---|---|
| Line Name | FinName | |
| Mailing Name | FinMailingName | |
| Add Line1 / Add Line2 | AddLine1 / AddLine2 | |
| City | City | |
| Pincode | PinCode | |
| Registration No | RegistrationNo | |
| Short Name | ShortName | max 3 characters |
| Admin Mobile Nos | AdminMobileNos | |
| Default Loanmethod | DefaultLoanMethodId | read-only picker |
| Default Bank Name | BankLedgerid | read-only picker |
| Email Identifier | EmailIdentifier | max 30 characters |
| SMS Identifier | SMSIdentifier | max 20 characters |

Below the fields are an "UPDATE" button and a ProgressBar.

**Menu / actions.** UPDATE saves. Tapping either picker field opens its list.

**How it works.**
1. `onCreate` builds the permissions for `nav_financedetalis`. If extras are present it fills the fields. The loan-method name comes from `getloanmethodname` (local `LoanMethods`) and the bank name from `getbankledgername` (local `BankLedgers`). UPDATE is hidden unless the user has UpdateState.
2. The picker results set `DefaultLoanMethodId` (rc 21) or `DefaultBankledgersid` (rc 22).
3. UPDATE copies the text fields and runs `ASYNCupdatefinance`:
   - Server: `update FinanceMaster set FinName=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,RegistrationNo=?,ShortName=?,EmailIdentifier=?,SMSIdentifier=?,FinMailingName=?,AdminMobileNos=?,DefaultLoanMethodId=?,BankLedgerid=? where FinId=?`.
   - Local: the same 13 columns via a compiled `Update FinanceDetails … where FinId=?`.
4. `onPostExecute` calls `setResult(1, {passedfinance})` and finishes. The caller replaces that line in its in-memory list.

**Functions.**
- `onCreate()`: binds views, permissions, pre-fill, listeners.
- `lambda$onCreate$1()`: loan-method picker.
- `lambda$onCreate$2()`: bank-ledger picker.
- `lambda$onCreate$3()`: collects the fields and saves.
- `onActivityResult()`: applies the picked ids.
- `ASYNCupdatefinance`: `doInBackground` (remote and local UPDATE), `onPreExecute`/`onPostExecute`.

**Rules & edge cases.**
- Several fields are carried through untouched and cannot be edited here:
  - `CollectionBreakString`: a line is weekly when it is "W" or "ww". JADX shows "W" as `ExifInterface.LONGITUDE_WEST`.
  - `IsWeeklyFinance`, `CashLedgerId` (executive cash book), `FinanceCashBookLedgerId`, `BooksFrom`.
  - `HLinecolor`, `LoanNoSuffix`, `SCopyImage`/`FCopyImage`, `enableinventory`.
  - There are no executive fields on this screen.
- When a line is selected, `refreshorreloadfinancelist` copies these values into globals and prefs:
  - FinMailingName → `globalvars.financename`.
  - AdminMobileNos → the recipients of admin SMS alerts.
  - SMSIdentifier → the `{LineName}` placeholder in SMS templates.
  - EmailIdentifier → part of report file names, for example the cash-book verification PDF.
  - DefaultLoanMethodId → `globalvars.DefaultLoanMethodId`, the default method for new loans.
  - BankLedgerid → `globalvars.bankaccountledgerid`.
- Editing the currently selected line does not refresh those globals until the line is selected again.
- There is no validation. Errors (for example a null DefaultLoanMethodId) are swallowed and success is still returned.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `employeesearchadapter` | RecyclerView.Adapter + Filterable | `employeesearchlist`, `branchemployeelist`, `branchemployeesearchlist` | Row `employeesearchrow`: name = LedgerName, second line = Tag; the role line stays hidden. A whole-row click calls `ContactsAdapterListener.onContactSelected(employees_data)`. Filter: LedgerName contains the query, or the lower-cased LedgerName contains it. |
| `employeesaccessearchadapter` | RecyclerView.Adapter + Filterable | `employeesaccessearchlist` | Row `employeesearchrow` with `empsearch_userrolename` made visible: name = LedgerName, second line = IMEI, role = `defaulttables.fetchuserrolename(userroleid)`. It opens the local DB into `globalvars.db` on every bind. A whole-row click calls `onContactSelected(mobiledeviceaccescontrolobj)`. The filter loop body is empty, so any non-empty query returns nothing; it is never reachable. |
| `branchemployeesaccessearch_adapter` | RecyclerView.Adapter + Filterable | `branchemployeesaccessearchlist` | Row `employeesearchrow`: name = ledgername, second line = IMEI. The click listener is on the name TextView only and calls `onContactSelected(branchaccess_obj)`. Filter loop empty (unused). |
| `loanmethods_adapter` | RecyclerView.Adapter + Filterable | `loanmethods_searchlist` | Row `loanmethods_row`: LoanMethodDesc, then ReportIdentification ("Abstract"); the hidden amount view is unused. A row click calls `onContactSelected(loanmethods_data, position)`. Filter: the upper-cased query is checked against the lower-cased description, or against ReportIdentification case-sensitively. |
| `employees_data` | Serializable model (extra `passedemployee`) | Employee and branch-employee screens | LedgerId (Double), LedgerName, GroupId (Double, 29), FinId (int), Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid (1), desigstatusid (1 active / 2 inactive), ScreenType (8). |
| `loanmethods_data` | Serializable model (extras `passedmethod` / `receiveddata`) | Loan-method screens; `defaulttables.getloanmthodparticularsforselectedloanmethod` → `newloancreation`, `todaycollectionlist` | LoanMethodId; LoanMethodDesc; doubles LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, InsAmount; Integers CollectionBreak, NoOfIns, GracePeriod, DocChargesMethod, DocCharges, ShowId, WarningDays; CollectionBreakString; ReportIdentification; CBCount and DueSupportAmount (int); IsVisible (`getVisible`/`setVisible`). `getWarningDays()` unboxes and so throws a NullPointerException if the value was never set. |
| `mobiledeviceaccescontrolobj` | Serializable model (extras `mobiledeviceaccescontrolobj` / `curmaobj`) | `employeesaccessearchlist` and its adapter, `mobiledeviceaccescontrol_act`, `specialaccesscontrolact` | MDAId, IMEI (device id), LId (employee LedgerId), LedgerName, isActive (`getActive()` returns a Boolean), userroleid. |
| `branchaccess_obj` | Serializable model (extras `branchaccess_obj` / `curmaobj`) | `branchemployeesaccessearchlist` and its adapter, `branchmobiledeviceaccescontrol_act` | MDAId, IMEI, LId, ledgername, isActive, isbranchusercontrolallowed. The list query does not fill the last field, so it stays false. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_employeesearch.xml` | `employeesearchlist`, `branchemployeelist`, `branchemployeesearchlist`, `employeesaccessearchlist`, `branchemployeesaccessearchlist`, `loanmethods_searchlist` | CoordinatorLayout `#main` holding:<br>• AppBarLayout with Toolbar `#toolbar`.<br>• FrameLayout with `include #content_layout` → `customersearchlist.xml` (RecyclerView `#recycler_view`).<br>• FAB `#addnewemployeeid` (`ic_input_add`).<br>• A hidden full-screen FrameLayout `#progress_Overlay` (#80d3cece) containing ProgressBar `#capbarid`, used by the two access screens. |
| `activity_newemployee.xml` | `employeedetails_act`, `branch_employee` | ScrollView form with:<br>• Employee Name, Address Line 1/2, City, Pincode (number, max 6), Mobile No 1 (max 10), Mobile No 2 (number, max 10), EMail.<br>• A hidden block `#newemp_financenamelyt` "Ledger for the finance" (read-only `#newaccled_finnameid`).<br>• CheckBox `#newemp_activecheckboxid` "Active".<br>• Buttons `#newemp_submitid` ("Update" / "சேமி") and `#newemp_deletebtnid` ("DELETE" / "நீக்கு").<br>• ProgressBar `#newemp_pbarid`. |
| `activity_loanmethods.xml` | `loanmethods` | ScrollView with the 18 labelled inputs of the loan-method field table, including the hidden Interest Added pair and the Status CheckBox; the Collection Break and Document Charges Fixation fields use the read-only `readonlyboxwithsmallfont` style. Below them: SAVE and DELETE buttons and ProgressBar `#loanmethods_progressbarid`. |
| `activity_financelinesaddandedit.xml` | `financelinesaddandedit` | ScrollView with 13 labelled EditTexts from Line Name to SMS Identifier. Default Loanmethod and Default Bank Name are `readonlybox` pickers; Short Name has max 3 characters, Email Identifier max 30, SMS Identifier max 20. Below them: Button "UPDATE" and ProgressBar `#financedetails_progressbarid`. |
| `employeesearchrow.xml` | `employeesearchadapter`, `employeesaccessearchadapter`, `branchemployeesaccessearch_adapter` | 80dp clickable row:<br>• `#empsarch_partynameid` (17sp; placeholder "Party Name" / "பார்ட்டி பெயர்").<br>• `#empsearch_tagid` (placeholder "Business" / "தொழில்").<br>• Hidden bold `#empsearch_userrolename` ("User Role", edit-employee icon). |
| `loanmethods_row.xml` | `loanmethods_adapter` | 70dp row: `#loanmethodsrow_loanmethodnameid` (17sp, placeholder "100 days col"), `#loanmethodsrow_shortnameid` (placeholder "Group Name"), hidden `#tvcollectedamountnewid`. |

---
[← brain.md](../../brain.md) · Module 8.16 of 20 · Previous: [8.15 Finance transactions lists, party/loan ledgers and loan & deposit analysis reports](15-finance-transactions-lists-party-loan-ledgers.md) · Next: [8.17 Inventory module: stock masters, items, customers/suppliers, purchase and stock in hand](17-inventory-module-stock-masters-items-customers.md)
