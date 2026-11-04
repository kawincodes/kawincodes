# 8.17 Inventory module: stock masters, items, customers/suppliers, purchase and stock in hand

[← brain.md](../../brain.md) · Module 8.17 of 20 · Previous: [8.16 Admin masters: employees, branch employees, access control, loan methods and finance lines](16-admin-masters-employees-branch-employees-access.md) · Next: [8.18 Printing: collection list prints, receipts and Bluetooth printers](18-printing-collection-list-prints-receipts-bluetooth.md)

This is a small online inventory add-on. It covers master CRUD for stock categories, stock groups (with HSN code), units of measure and items, a supplier ledger list, a purchase-note entry screen with item lines (posted as an accounting voucher plus `InvTransMaster`/`InvTransDetails` rows), a purchase-bill list and a read-only stock-in-hand report (proc `StockinHand`). Every screen needs a live jTDS connection to the branch SQL Server. The lists mirror the masters into local SQLite tables (`Stockcat`, `StockGrp`, `StockUOM`, `Itemmaster`, `Suppliers`; SQLite table names are case-insensitive, so `stockcat`/`StockCat` and `ItemMaster`/`Itemmaster` are the same table), and the pickers read from those tables.

**How the menu is enabled.** The drawer group `nav_inventorycontrols` ("Inventory Master") is `android:visible="false"` in `activity_navigation_drawer.xml`. `navigationdrawactivity.onCreate` makes it visible only when two conditions hold:
1. The role's menu-permission rows (`globalvars.salesdetailsforandroid_datalist`, from local `tblSalesDetailsforAndroid`, synced by `syncresources` from server `AndroidMenus` LEFT JOIN `tblSalesDetailsforAndroid`) contain `nav_inventorycontrols` with `ShowState`=1.
2. `globalvars.curfin.isEnableinventory()` is true for the selected line. This is the `enableinventory` column of local `FinanceDetails`, filled from server proc `GetAllowedFinances @IMEI='<device_id>'`.

`globalvars.isinventoryenabled` (pref `isinventoryenabled`) mirrors that flag but the drawer does not read it.

**Rights inside the screens.** Each screen looks up the same permission row for its own menu name (`nav_StockCat`, `nav_StockGrp`, `nav_StockUOM`, `nav_itemmaster`, `nav_supplier`, `nav_stockpurchase`):
- AddState shows the FAB.
- ViewState lets the user open a row.
- UpDateState shows the Update button; DeleteState shows the Delete button.
- `globalvars.isFullAccessAllowed` grants all rights. It is read from `UserRoles.isFullAccessAllowed` for this device's role (`MobileDevlicesAllowed` joined on the device IMEI).

**Unreachable code.** Sales (TTID 11), delivery note (75) and receipt note (16) branches, automatic bill numbering, and printing/PDF all exist in code but cannot be reached from the UI in this build.

## Stock Categories list — `stockcat_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` (own Toolbar) |
| **Layout(s)** | `activity_employeesearch.xml` (includes `customersearchlist` RecyclerView) + row `stockcat_row.xml` via `stockcat_adapter`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer `nav_StockCat` ("Stock Cats") |
| **Opens** | `stockcategories`. FAB → extras `actiontype`=9014 (`stockcat_addnewrequestid`), `displayindex`=list size+1, rc 9014. Row tap → `stockcatobj`, `actiontype`=9015 (`stockcat_editrequestid`), rc 9015 |
| **Data** | Server (`selbranchconnString`): `Select CatId, CatDesc, DisplayIndex, IsActive from Stockcat order by Catid` · local SQLite `Stockcat(CatId Double, CatDesc String, DisplayIndex Double, IsActive Boolean)`, wiped and re-filled on every open · rights row `nav_StockCat` |

**Purpose.** Browse the stock categories (called "Category/Brand" in the item picker) and add or edit them.

**UI.** Title "Stock Categories". Search icon (SearchView). List of category names. FAB "+" (`addnewemployeeid`), shown only with AddState. ProgressDialog "downloading, please wait ...".

**Menu / actions.** Search filters with case-sensitive `CatDesc.contains(text)`. FAB adds a category. Row tap edits it, only with ViewState. Back closes an open search first (OnBackPressedDispatcher callback). Up arrow (android.R.id.home 16908332) goes back.

**How it works.**
1. `onCreate` wires the adapter, starts `ASYNCAccountsLedgers` (misnamed), then resolves rights. Full access sets all rights true; otherwise the `nav_StockCat` row is used. With no row, a fresh `salesdetailsforandroid_data` means all rights are false.
2. `ASYNCAccountsLedgers.doInBackground` connects with jTDS, runs `CREATE TABLE IF NOT EXISTS Stockcat`, then `delete from Stockcat`, then selects every server row. Each row is inserted locally and added to the list as a `stockcat_data`. Exceptions only set `ErrorText`, which is never shown.
3. `onActivityResult` handles three cases:
   - (9014, 9014): inserts the returned `stockobj` at the top.
   - (9015, 9015): replaces the row with the same CatId.
   - (9015, 9016): removes that row, only when the extra `noofrowsdeleted` > 0.

**Functions.**
- `onCreate()`: rights, adapter, FAB, initial load.
- `onCreateOptionsMenu()`: SearchView setup and filter hookup.
- `onOptionsItemSelected()`: home/up → `onBackPressed()`.
- `onContactSelected()`: opens the editor if ViewState.
- `onActivityResult()`: merges add, edit and delete results into the list.
- `ASYNCAccountsLedgers`: server → local cache → list.
- `whiteNotificationBar()`: light status bar (flag 8192 = `SYSTEM_UI_FLAG_LIGHT_STATUS_BAR`) with a white status-bar colour (-1). The same helper exists in every list screen.

**Rules & edge cases.** A connection or SQL failure just shows an empty list. The local `Stockcat` table is what `itemmaster_activity`'s picker reads (`IsActive=1 order by DisplayIndex`). `downloaddata` (only when the line has inventory enabled) and `syncresources` also refill it. `syncresources` always refreshes `stockgrp`, but refreshes `stockcat`, `stockUOM` and `ItemMaster` only when the server has at least one stock group.

## Stock Category editor — `stockcategories`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, app theme `AppTheme` = DarkActionBar) |
| **Layout(s)** | `activity_stockcat.xml` |
| **Opened from** | `stockcat_list` (9014 new with `displayindex`; 9015 edit with `stockcatobj`) |
| **Opens** | — (an `onActivityResult` for rc 2013 `stockcat_requestcode` is dead: no picker is ever launched) |
| **Data** | Server (`selbranchconnString`): `{call dbo.Insertstockcat(CatDesc, DisplayIndex, IsActive, OUT catid)}` · `update stockcat set CatDesc=?,DisplayIndex=?,IsActive=? where Catid=?` · `DELETE From stockcat where Catid=?` · local `stockcat` insert / `StockCat` update · rights `nav_StockCat` |

**Purpose.** Create, rename, re-order, (de)activate or delete one stock category.

**UI.** Title "Stock Cat". Fields: "Stock Name" (`stockcat_categoriesnameid`); "Show Index" (`stockcat_Showindexd`, prefilled for a new category); checkbox "Active". Buttons: "UPDATE" (save) and "DELETE".

**Menu / actions.** In new mode UPDATE is shown and DELETE hidden. In edit mode UPDATE needs UpDateState and DELETE needs DeleteState.

**How it works.**
1. Save copies the name, `Integer.parseInt(Show Index)` and the Active flag, then runs `ASYNCupdatestockcat`:
   - 9014: the proc returns the new `catid`, then a local insert.
   - 9015: server update, then local update.
2. DELETE sets actiontype 9016 (`stockcat_deleterequestid`) and runs the server delete; `noofrowsdeleted` = rows affected. It asks for no confirmation.
3. `onPostExecute`, when 9016 deleted 0 rows, shows an alert:
   - Icon `ic_action_warning`; title "தங்கள் கவனத்திற்கு.... " ("For your attention…").
   - Message "  பதிவுகள் இருப்பதால் நீக்க இயலாது..." ("Cannot delete because records exist…"); button "சரி" (OK).
   - The screen stays open.
4. Otherwise it calls `setResult(actiontype, {actiontype, stockobj, noofrowsdeleted})` and finishes.

**Functions.** `onCreate()` (mode setup and rights) · `lambda$onCreate$1` (save) · `lambda$onCreate$2` (delete) · `onActivityResult()` (dead picker hook) · `ASYNCupdatestockcat` (insert/update/delete).

**Rules & edge cases.**
- An empty Show Index throws `NumberFormatException` and the app crashes. A blank name is accepted.
- All SQL exceptions are swallowed (`printStackTrace`), so a failed insert still returns "success" and the list gets a row with CatId 0.
- A deleted category stays in local `Stockcat` until the list is reloaded.

## Stock Groups list — `stockgrp_list`
| | |
|---|---|
| **Type** | Activity, theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_employeesearch.xml` + row `stockcat_row.xml` via `stockgrp_adapter`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer `nav_StockGrp` ("Stock Groups") |
| **Opens** | `stockgroup_act`: FAB → `actiontype`/rc 9018 (`stockgrorup_addnewrequestid`); row tap → `stockgrpobj`, `actiontype`/rc 9019 (`stockgrorup_editrequestid`) |
| **Data** | Server (`selbranchconnString`): `Select GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex from StockGrp order by Grpid` · local `StockGrp(GrpId int, GrpDesc String, HSNSACCode int, IsActive Boolean, DisplayIndex int)`, wiped and re-filled · rights `nav_StockGrp` |

**Purpose.** Browse, add and edit stock groups (product groups carrying an HSN/SAC code).

**UI.** Title "Stock Groups". Search box. List of group names (`GrpDesc`). FAB shown with AddState.

**Menu / actions.** Same as `stockcat_list`: case-sensitive `GrpDesc.contains` search; row tap needs ViewState; the adapter callback is `onstockgroupselected()`.

**How it works.**
1. `asyncstockgroupsfromserver` downloads and caches the groups exactly as in `stockcat_list`.
2. Results:
   - (9018, 9018): add the returned `stockgrpobj` at the top.
   - (9019, 9019): replace by GrpId.
   - (9019, 9020): remove, only if `deletedrowscount` > 0. Because of the `stockgroup_act` bug below, a blocked delete leaves the row in place silently.

**Functions.** `onCreate()` · `onCreateOptionsMenu()` · `onOptionsItemSelected()` · `onstockgroupselected()` · `onActivityResult()` · `asyncstockgroupsfromserver` · `whiteNotificationBar()`.

**Rules & edge cases.** Errors are silent. Unlike the category list, no display index is suggested for a new group.

## Stock Group editor — `stockgroup_act`
| | |
|---|---|
| **Type** | Activity (DarkActionBar app theme) |
| **Layout(s)** | `activity_stockgroup.xml` |
| **Opened from** | `stockgrp_list` (9018 new; 9019 edit + `stockgrpobj`) |
| **Opens** | — (dead `onActivityResult` for rc 9017 `stockgrorup_requestcode`) |
| **Data** | Server (`selbranchconnString`): `{call dbo.InsertStockGrp(GrpDesc, HSNSACCode, DisplayIndex, IsActive, OUT GrpId)}` · `update stockgrp set GrpDesc=?,HSNSACCode=?,IsActive=?,DisplayIndex=? where GrpId=?` · `DELETE From stockgrp where GrpId=?` · local `stockgrp` insert/update · rights `nav_StockGrp` |

**Purpose.** Maintain one stock group: name, HSN code, active flag and display order.

**UI.** Title "New Stock group" or "Edit Stock Group". Fields: "Group Name"; "HSN Code" (numberSigned); checkbox "Active"; "Show Index" (numberSigned). Buttons "SAVE" and "DELETE" (ta "நீக்கு") are `gone` in the XML and shown by code: SAVE always in new mode; in edit mode SAVE needs UpDateState and DELETE needs DeleteState.

**How it works.**
1. Save reads the name, `Integer.parseInt` of HSN and of Show Index, and the Active flag, then runs `ASYNCupdatestockgrp`:
   - 9018: insert via the proc, which returns GrpId.
   - 9019: server update plus local update.
2. Delete sets actiontype 9020 and stores `deletedrowscount`.
3. The result returns `stockgrpobj` and `deletedrowscount` with resultCode = actiontype.

**Functions.** `onCreate()` · `lambda$onCreate$1` (save) · `lambda$onCreate$2` (delete) · `onActivityResult()` (dead) · `ASYNCupdatestockgrp`.

**Rules & edge cases.**
- **Bug:** `onPostExecute` tests `actiontype == 9016`, the stock-category delete code (confirmed in smali as `0x2338`), instead of 9020. The "பதிவுகள் இருப்பதால் நீக்க இயலாது..." warning therefore never appears for groups: a refused delete just closes the screen.
- An empty HSN Code or Show Index crashes the app (`NumberFormatException`).
- Exceptions are swallowed. There is no delete confirmation.
- Field names such as `etDesignationlevelname` and `designationlevel_*` show the screen was copied from a designation-level form.

## Stock UOM list — `stockUOM_list`
| | |
|---|---|
| **Type** | Activity, theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_employeesearch.xml` + row `stockcat_row.xml` via `stockUOM_adapter`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer `nav_StockUOM` ("Stock UOM") |
| **Opens** | `stockUOM_act`: FAB → `actiontype`/rc 9022 (`stockuom_addnewrequestid`); row tap → `stockuomobj`, `actiontype`/rc 9023 (`stockuom_editrequestid`) |
| **Data** | Server (`selbranchconnString`): `Select UOMId, UOMDesc, DisplayIndex, IsActive from StockUOM order by UOMid` · local `StockUOM(UOMId Double, UOMDesc String, DisplayIndex Double, IsActive Boolean)`, wiped and re-filled · rights `nav_StockUOM` |

**Purpose.** Browse, add and edit units of measure.

**UI.** Title "Stock UOM". Search box. List of `UOMDesc`. FAB shown with AddState.

**Menu / actions.** Case-sensitive `UOMDesc.contains` search. Row tap needs ViewState. Back is handled by a legacy `onBackPressed()` override (collapses search first) rather than the dispatcher.

**How it works.** `ASYNCAccountsLedgers` downloads and caches as in `stockcat_list`. Results:
- (9022, 9022): add at the top.
- (9023, 9023): replace by UOMId.
- (9023, 9024): remove if `noofrowsdeleted` > 0.

**Functions.** `onCreate()` · `onCreateOptionsMenu()` · `onOptionsItemSelected()` · `onBackPressed()` · `onContactSelected()` · `onActivityResult()` · `ASYNCAccountsLedgers` · `whiteNotificationBar()`.

**Rules & edge cases.** Errors are silent. The back override dereferences `searchView`, which is null if the menu has not been created yet.

## Stock UOM editor — `stockUOM_act`
| | |
|---|---|
| **Type** | Activity (DarkActionBar app theme) |
| **Layout(s)** | `activity_stockcat.xml` (shared with the category editor) |
| **Opened from** | `stockUOM_list` (9022 new; 9023 edit + `stockuomobj`) |
| **Opens** | — |
| **Data** | Server (`selbranchconnString`): `{call dbo.InsertStockUOM(UOMDesc, DisplayIndex, IsActive, OUT UOMId)}` · `update StockUOM set UOMDesc=?,DisplayIndex=?,IsActive=? where UOMId=?` · `DELETE From StockUOM where UOMId=?` · local `StockUOM` insert/update · rights `nav_StockUOM` |

**Purpose.** Maintain one unit of measure.

**UI.** Title "Stock UOM". Because the category layout is reused, the description field is labelled "Stock Name". Also "Show Index", checkbox "Active", and buttons "UPDATE"/"DELETE". Button visibility follows the same rules as the category editor.

**How it works.**
1. Save runs `ASYNCupdatestockuom`: 9022 inserts via the proc and caches locally; 9023 updates the server and the local table.
2. DELETE sets 9024 and needs no confirmation. Zero rows deleted shows the Tamil "records exist" alert (this check is correct here).
3. Otherwise the result `{stockuomobj, actiontype, noofrowsdeleted}` is returned with resultCode = actiontype.

**Functions.** `onCreate()` · `lambda$onCreate$1` (save) · `lambda$onCreate$2` (delete) · `ASYNCupdatestockuom`.

**Rules & edge cases.** Show Index is **not** prefilled for a new UOM, so saving without typing it crashes the app (`Integer.parseInt("")`). Exceptions are swallowed.

## Item master list — `itemmaster_list`
| | |
|---|---|
| **Type** | Activity, theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_employeesearch.xml` + row `itemmaster_row.xml` via `itemmaster_adapter`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer `nav_itemmaster` ("Item Master") |
| **Opens** | `itemmaster_activity`: FAB → `actiontype`/rc 2018 (`itemmaster_addnewrequestid`); row tap → `itemmasterobj`, `actiontype`/rc 2019 (`itemmaster_editrequestid`) |
| **Data** | Server (`selbranchconnString`): `Select ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel from Itemmaster order by itemcode` · local `Itemmaster(ItemCode Int, ItemName String, PriUOMid Int, Catid Int, Grpid Int, MinimumOrderIdQty Int, ReOrderLevel Int)`, wiped and re-filled · rights `nav_itemmaster` |

**Purpose.** Browse the product catalogue and add or edit items.

**UI.** Title "Items". Search box. List of item names only. FAB shown with AddState. ProgressDialog while loading.

**Menu / actions.** Case-sensitive `ItemName.contains` search. Row tap needs ViewState.

**How it works.** `ASYNCAccountsLedgers` downloads and caches. Results:
- (2018, 2018): add at the top.
- (2019, 2019): replace by ItemCode.
- (2019, 2020): remove if `noofrowsdeleted` > 0.

The cached `Itemmaster` table feeds the purchase line picker (`fetchitemslistforpurchase`) and item-name lookups.

**Functions.** `onCreate()` · `onCreateOptionsMenu()` · `onOptionsItemSelected()` · `onContactSelected()` · `onActivityResult()` · `ASYNCAccountsLedgers` · `whiteNotificationBar()`.

**Rules & edge cases.** Errors are silent. A deleted item is not removed from local `Itemmaster` until the next reload.

## Item editor — `itemmaster_activity`
| | |
|---|---|
| **Type** | Activity (DarkActionBar app theme) |
| **Layout(s)** | `activity_itemmaster.xml` |
| **Opened from** | `itemmaster_list` (2018 new; 2019 edit + `itemmasterobj`) |
| **Opens** | `spinnersearch_list` three times: Stock Cat (title "Category/Brand", rc 2021 `fetchingstockcatlist_requestcode`); UOM (title "UOM", rc 2022 `fetchingstockuomlist_requestcode`); Stock Group (title "Stock Group", rc 2023 `fetchingstockgrplist_requestcode`) |
| **Data** | Local pickers: `defaulttables.fetch_stockcat` (`Select CatId, CatDesc from Stockcat where IsActive=1 order by DisplayIndex`), `fetch_stockUOM` (`StockUOM where IsActive=1 order by DisplayIndex`), `fetch_stockgrp` (`Stockgrp where IsActive=1 order by DisplayIndex`); name lookups `getstockuomname/getstockgrpname/getstockcatname` · Server (`selbranchconnString`): `{call dbo.Insertitemmaster(ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel, OUT itemcode)}` · `update Itemmaster set ItemName=?,PriUOMid=?,Catid=?,Grpid=?,MinimumOrderIdQty=?,ReOrderLevel=? where itemcode=?` · `DELETE From Itemmaster where itemcode=?` · local `Itemmaster` insert/update · rights `nav_itemmaster` |

**Purpose.** Define an item with its unit, group, category and reorder parameters.

**UI.**
- Title "Item Details".
- "Item Name" (free text).
- "UOM", "Stock Group", "Stock Cat": read-only `readonlybox` fields (not focusable or editable); one tap opens the picker.
- "Reorder Level " and "Minimum Order Qty " (numberSigned).
- Buttons "UPDATE" (save, labelled the same even for a new item) and "DELETE".

**Menu / actions.** Each picker returns resultCode 1 with extras `displaymember`/`valuemember`, which set the name shown and `Catid`/`PriUOMid`/`Grpid`. In new mode only UPDATE is visible; in edit mode UPDATE needs UpDateState and DELETE needs DeleteState.

**How it works.**
1. Edit mode shows the stored values and resolves the master names from local tables.
2. Save sets ItemName. MinimumOrderIdQty and ReOrderLevel default to 0 and are parsed if non-empty.
3. Validation: Catid, Grpid and PriUOMid must all be non-zero, otherwise Toast "Incomplete Data".
4. `ASYNCupdatestockcat` (misnamed):
   - 2018: proc insert returns `itemcode`, then a local insert.
   - 2019: server update plus local update.
   - 2020 (Delete, no confirmation): server delete; `noofrowsdeleted`. Zero rows deleted shows the Tamil "records exist" alert (warning icon, "சரி").
5. Otherwise the result `{actiontype, itemmasterobj, noofrowsdeleted}` is returned with resultCode = actiontype.

**Functions.** `onCreate()` · save `onClick` (`itemmaster_activity$1`) · three picker `onClick`s · delete `onClick` · `onActivityResult()` · `ASYNCupdatestockcat`.

**Rules & edge cases.**
- An empty name is allowed. A lone "-" in a numeric field crashes the app (`NumberFormatException`).
- Exceptions are swallowed, so a failed save still returns success.
- Pickers list only **active** masters that already exist in the local cache (filled by the master lists, `downloaddata` or `syncresources`). No price is stored on an item.

## Suppliers list — `customers_list`
| | |
|---|---|
| **Type** | Activity, theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_partieslist.xml` + row `partieslistrow` via the shared `partieslist_adapter` (outside this group); menu `menuemptylistwithsearchbutton` |
| **Opened from** | Drawer `nav_supplier` ("Suppliers") with `screentype`=17 (`suppliers_screentype`), `groupid`=11 (`supplier_groupid`), `requestcode`=3003 (`supplierdetails_AccessRequest`, read but unused). The screen also supports screentype 6 (`customers_ScreenType`, title "Customers"), but no caller passes it |
| **Opens** | `customerdetails_act`. FAB → `actiontype`/rc 9003 (JADX shows the constant `parties_newpartyrequest`), `screentype`, `groupid`=**36**, plus `ledgerdata` = an empty `customerdetails_data` that is never read. Row tap → `customerobject` (`partydetails_data`), `actiontype`/rc 9004 (`parties_updatepartyrequest`); no screentype or groupid is passed |
| **Data** | Server (`selbranchconnString`): `Select ledgerid, ledgername, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, GroupId, IsDefault, IsReserved, ScreenType from tblledgers where screentype=<screentype> order by LedgerId` · local `Suppliers` (13 columns, `CREATE TABLE IF NOT EXISTS`, wiped and re-filled) · rights `nav_supplier` |

**Purpose.** List the supplier ledgers (account ledgers with ScreenType 17) and add or edit them. Opening this screen is also what fills the local `Suppliers` cache that the purchase screen's supplier picker reads.

**UI.** Title "Parties", replaced by "Suppliers" (or "Customers" for screentype 6). Search box. Rows show name, AddLine1, AddLine2, City and MobileNo1. FAB shown with AddState.

**Menu / actions.** Search matches upper-case name contains, or AddLine2 contains. Row tap needs ViewState. Back collapses search first.

**How it works.**
1. Rights for `nav_supplier` are computed twice (duplicated code).
2. `ASYNCAccountsLedgers` downloads the rows, inserts each into `Suppliers` and builds `partydetails_data` objects.
3. Results:
   - (9003, 9003): insert the returned `customerobject` at the top and smooth-scroll.
   - (9004, 9004): replace by LedgerId.
   - (9004, 9005): remove.

**Functions.** `onCreate()` · `lambda$onCreate$1` (FAB) · `onCreateOptionsMenu()` · `onOptionsItemSelected()` · `onPartyselected()` · `onActivityResult()` · `ASYNCAccountsLedgers` · `whiteNotificationBar()`.

**Rules & edge cases.** Errors are silent. `onProgressUpdate` touches the static `sevendayscollectionforselline.progressBar`, a copy-paste leftover; its NPE is caught. The 9003/9004/9005 codes are the generic "party" request codes reused for suppliers.

## Supplier / customer details — `customerdetails_act`
| | |
|---|---|
| **Type** | Activity (DarkActionBar app theme) |
| **Layout(s)** | `activity_customerdetails.xml` |
| **Opened from** | `customers_list` (9003 new; 9004 edit + `customerobject`) |
| **Opens** | — (`onActivityResult` for rc 9011 is an empty leftover) |
| **Data** | Server (**`branchconnString`**, the logged-in branch, not the selected one): `{call dbo.InsertSuppliers(OUT ledgerid, ledgername, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, GroupId, IsDefault, IsReserved, ScreenType)}` · `update tblledgers set ledgername=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,MobileNo1=?,MobileNo2=?,EMail=? where Ledgerid=?` · `delete from tblLedgers where ledgerid=?` · local `Suppliers` insert/update/delete · rights `nav_supplier` |

**Purpose.** Create, update or delete a supplier ledger and its address and contact fields.

**UI.**
- Title starts as "Supplier", then becomes "New Supplier Details"/"Edit Supplier Details" ("…Customer Details" for screentype 6). Editing from the list passes no screentype, so the title stays "Supplier".
- Fields: "Customer Name", "Add Line 1", "Add Line 2", "City" (textCapWords); "Pincode", "Mobile  No1", "Mobile No2" (number); "EMail".
- Hidden: a block of groupid/isdefault/isreserved/screentype fields and an "ACTIVE" checkbox.
- Buttons "SAVE" and "DELETE".

**Menu / actions.** New mode shows SAVE only. Edit mode shows SAVE with UpDateState and DELETE with DeleteState.

**How it works.**
1. Save runs only if the object's GroupId ≠ 0. It copies all fields, then forces `GroupId=11`, `IsDefault=0`, `IsReserved=0`, `ScreenType=<screentype>`. It also builds an unused `compwiseop_data` list (one row, CId 1).
2. `ASYNCupdateAccLedgers` blocks touches with window flag 16 (`FLAG_NOT_TOUCHABLE`):
   - 9003: the proc returns `ledgerid`, then a local `Suppliers` insert.
   - 9004: server update, then local update.
   - 9005 (DELETE; JADX shows `parties_deletrepartyrequest`): server delete. If rows > 0 it deletes the local row too.
3. Delete with 0 rows affected shows an alert, and the screen stays open:
   - Title "தங்கள் கவனத்திற்கு.... ".
   - Message "தொடர்பான பதிவுகள் இருப்பதால் நீக்க இயலாது..." ("Cannot delete because related records exist…"); button "சரி".
4. Success calls `setResult(actiontype, customerobject)` and finishes.
5. An exception shows "Error Info" with "<line>.<message>"; OK closes the screen.

**Functions.** `onCreate()` · `lambda$onCreate$2` (save) · `lambda$onCreate$3` (delete) · `onActivityResult()` · `ASYNCupdateAccLedgers`.

**Rules & edge cases.**
- No validation at all: blank name, unchecked mobile and e-mail. No delete confirmation.
- The delete is a raw `delete from tblLedgers`; a foreign-key violation surfaces as "Error Info", not the friendly alert.
- Every save is stored with GroupId 11, even the "customer" variant and despite the FAB passing 36.
- Uses `partydetails_data`, not `customerdetails_data`.

## Purchase note entry — `invtransactnew`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), `exported=false`, theme `AppTheme.NoActionBar` (own Toolbar) |
| **Layout(s)** | `activity_invtransactnew.xml` (includes `invtransdetails_row` as the column header; lines rendered by `invtransdetails_adapter` with `invtransdetails_row`); menu `menu_listingicononly` |
| **Opened from** | (1) Drawer `nav_stockpurchase` ("Stock Purchase"): `ttid`=116, `screentype`=17, `transtypedesc`="PURCHASE" (`PURCHASE_TRANSACTION_DESC`), `actiontype`=1006, `listheading`="Purchase Notes" (never read); plain `startActivity`, so the result is ignored. (2) `inventorytransactionslist_act` FAB: rc 1006, actiontype 1006, plus `qty` and `screentype`. (3) The same list's row tap: rc 1007, actiontype 1007, `transdata` |
| **Opens** | `spinnersearch_list` (supplier, rc 1001 `suppliername_requestcode`) · `invtrans_rowdetails` (line edit, rc 1003) · `inventorytransactionslist_act` (LIST icon, rc 1000, extras `ttid`, `caltype`="app") · unreachable: PDF viewer, share chooser, `PrintManager` |
| **Data** | Local SQLite: `Suppliers` (picker `fetchsuppliersandcustomers`; name `fetchsupplierledgername`), `ItemMaster` (line item names), `RecentPartieswithContractVehicles` (dead) · Server: proc `dbo.InsertAPPAccTransactionsMaster`; `tblTransactionsMaster`, `tblTransactionsDetails`, `InvTransMaster`, `InvTransDetails`. Saving uses `selbranchconnString`; reading lines uses `branchconnString`. Dead: `transactionsmaster` next-bill query and proc `GetRecentPartieswithContractVehicles` · rights `nav_stockpurchase` · no SharedPreferences |

**Purpose.** Record a supplier's purchase bill: bill number, date, supplier and item lines (qty × price). Saving posts an accounting voucher (TransType 116) against purchase ledger 151 plus inventory header and line rows on the server. An existing bill can be edited or deleted.

**UI.**
- Toolbar: "No" plus the bill-number field (`receiptnote_invoicenoetid`, number); "Date" (ta "தேதி"), a tappable text showing "Date:dd-MM-yyyy".
- Party label (`@string/customer` = "Party"), renamed "Supplier" for TTID 116 or "Customer" for 11, above a read-only supplier box.
- Column header S.No / Product / Qty / Price / Amount, then the list of lines.
- Footer "Total" (ta "ஆக மொத்தம்") with the total formatted `##,##,##,##,###.00`.
- Buttons "Update" (ta "சேமி") and "DELETE" (ta "நீக்கு").
- FAB "+" (contentDescription "add new item") and a ProgressBar.
- Title: "PURCHASE", then "New Purchase" or "Edit Purchase" ("New Sales"/"Edit Sales" if TTID 11).

**Menu / actions.**
- Toolbar icon "LIST" (`translistid`) opens the bill list, only with ViewState. Without ViewState the tap does nothing visible.
- FAB appends an empty line (SNo = count+1). The editor does not open automatically; the user taps the new line.
- Line tap opens `invtrans_rowdetails`. Supplier box opens the supplier picker. Date opens a `DatePickerDialog`.
- "Update" validates and saves. "DELETE" (edit mode only) asks for confirmation, then deletes.
- Handlers for `menuwithprintingoptions_refreshheaders`, `_directprint`, `_showaspdf` and `_shareaspdf` exist, but those items are not in `menu_listingicononly`, so they are unreachable.

**How it works.**
1. **`onCreate`** opens local DB `venusmicrofinv005` and reads the extras. Rights come from `nav_stockpurchase`, but only ViewState is ever used. It builds `invtransdetails_adapter(…, hideprice=false)` and sets `defaulttransdate` = today.
2. **New mode (1006):** a `transactionsmaster_data` dated today, one empty line with SNo 1, DELETE hidden, total 0.00.
3. **Edit mode (1007):** the `transdata` object fills the bill number (`FWVWUDTransId`) and date, and the supplier name comes from local `Suppliers`. `ASYNCGetInvTransDetails` (on `branchconnString`) runs `Select SNo ,ItemCode ,Rate ,BilledQty ,Total, TransId from InvTransDetails where Transid=<TransId> order by SNo`. Every value is read with `getInt` (fractions truncated) and ActualQty = BilledQty. The total is recalculated.
4. **Supplier picker:** `select ledgerid,ledgername,City,GroupId from Suppliers` (name, with city as the second line). Result rc 1001 with resultCode 1 sets `TransLedgerId` (valuemember), `Particulars` (name) and the box text.
5. **Line results:**
   - (1003, 1003): replace the line with the same SNo.
   - (1003, 1004): remove that line.
   - After any result, `calctotal` logic runs: total = Σ TotalAmt, qty = Σ BilledQty (integers).
6. **Update validations**, each shown in an "Error..." dialog with OK:
   - Empty bill no → "Please enter invoice number".
   - No supplier → "Please Select Supplier".
   - No lines → "Please enter Produts" (sic).
   - Two lines with the same ItemCode → "<code> found in <i> and <j> positions". Blank lines count as ItemCode 0, so two blank lines trigger this.
7. **Before saving**, the object is set to: Totqty; `CId`=`globalvars.finid`; TransAmount = total; **TTId = 11, hard-coded** (smali `0xb`; only affects the returned object); TransDate = `defaulttransdate`; FWVWUDTransId = bill number.
8. **`ASYNC_SaveSalesentry`** (on `selbranchconnString`):
   - **New (1006):** call `{call dbo.InsertAPPAccTransactionsMaster(...)}` with OUT `TransId`, OUT `FWVWTransId`, `FWVWUDTransId`=bill no, `TransDate`, `TransType`=ttid (116), `Narration`="", `FinId`=`globalvars.finid`, `TransAmount`, `TransLedgerId`=`globalvars.PURCHASE_LEDGERID` (151, a hard-coded constant), `SourceLedgerId`=supplier, `ImageFileName`=NULL (type 4 = INTEGER). Then insert two `tblTransactionsDetails` rows: (Sno 1, supplier, "Target", amount, "Dr", AgnstLedgerSNo 2) and (Sno 2, 151, "Source", amount, "Cr", AgnstLedgerSNo 1). Then `INSERT INTO InvTransMaster(NetTotal,TotQty,PartyLedgerId,roundoff,TransId)` with roundoff 0. Finally one `INSERT INTO InvTransDetails(SNo,ItemCode,Rate,BilledQty,Total,TransId)` per line, SNo = position+1, for **every** line including blank ones.
   - **Edit (1007):** `update tbltransactionsmaster set FWVWTransId=?,FWVWUDTransId=?,TransDate=?,TransAmount=? where TransId=?` (both numbers set to the bill no). Then `update InvTransMaster set NetTotal=?,TotQty=?,PartyLedgerId=?,roundoff=? where TransId=?`. Then `delete from InvTransDetails` and `delete from tblTransactionsDetails` (plain Statement), re-insert the two detail rows as above, and re-insert only lines with BilledQty > 0.
   - **Delete (1008):** delete from `InvTransDetails`, `tblTransactionsDetails`, `InvTransMaster` and `tblTransactionsMaster` where TransId=?.
   - **Success:** `setResult(actiontype, transdata)` and finish. There is no toast.
   - **Error:** "Error Info" dialog with "<line>.<message>"; OK finishes and the typed data is lost.
9. **Delete button:** AlertDialog with title "தங்கள் கவனத்திற்கு.... " and message "அழித்தப் பின் திரும்ப பெற இயலாது என்பதை கவனத்தில் கொள்ளவும்..." ("Please note it cannot be recovered after deletion…"). Buttons "அழிக்கலாம்" (delete) and "அழிக்க வேண்டாம்" (don't delete). Delete sets actiontype 1008 and runs the save task.
10. **Dead print/PDF path** ("DELIVERY NOTE"):
    - Pages: `totalpages` = ceil(lines/25). Pages are `PdfDocument` 800×1140. The "logo" is the `ic_action_warning` drawable stretched across the top.
    - Header: "TO," plus the party name, "DC No: <bill no>", "Date:".
    - Columns S.No / HUID / ITEM / G.WEIGHT / N.WEIGHT / MC / P.WEIGHT, filled with SNo, item code, item code, qty, qty, amount, amount. Then a "TOTAL" row.
    - Footer: "Printed @ null, null" (`currentDate`/`currentTime` are never assigned) and "For SNK Jewllers,". This is a jewellery-shop template left in the code.
    - No pagination: every line is drawn on page 1.
    - Output: saved as `<external storage>/finsoftliteappversion/DELIVERY NOTE.pdf`, then opened (VIEW) or shared (SEND chooser "Share Image"; Toast "Sharing tools have not been installed." if no target) via a `file://` URI. The flags are set with `setFlags(0x40000000)`: JADX shows `BasicMeasure.EXACTLY`, but the value is `FLAG_ACTIVITY_NO_HISTORY`, and it overwrites the earlier read-grant flag. Since `Uri.fromFile` throws on API 24+ (caught), this path would not work even if reachable.
    - `printDocument` sends an A4 job "<app_name> Document".

**Functions.**
- `onCreate()`: extras, rights, adapter, new/edit init, all listeners.
- `lambda$onCreate$1()`: FAB, appends a blank line and recalculates.
- `lambda$onCreate$2()`: opens the supplier picker (rc 1001).
- `lambda$onCreate$4()` / `lambda$onCreate$3()`: date picker; stores the picked date in `defaulttransdate` and the label.
- `invtransactnew$1.onClick()`: Update button, runs the validations, fills the object and starts the save.
- `lambda$onCreate$7()` / `lambda$onCreate$5()`: delete confirmation, then actiontype 1008.
- `onCreateOptionsMenu()`: inflates `menu_listingicononly` (LIST only).
- `onOptionsItemSelected()`: LIST → list (ViewState); home → back; dead refresh/print/PDF/share branches set `ReportHeader="DELIVERY NOTE"`.
- `onActivityResult()`: supplier pick, line replace/remove, total recalculation.
- `calctotal()`: Σ amount and Σ qty, and formats the total.
- `onContactSelected()`: opens `invtrans_rowdetails` (actiontype 1003, `ttid`, `receiveddata`, `Currentlotno`=0).
- `showincompletedataalert()`: "Error..." dialog.
- `printDocument()`, `createPdf()`, `sharePdf()`, `drawPage()`: dead delivery-note output.
- `drawCenter()`, `drawCenterwithbox()`, `drawCenterwithgivenwidth()`: text-centring helpers; paint colour `0xFF000000` (JADX shows `ViewCompat.MEASURED_STATE_MASK`) = black.
- `roundTwoDecimals()`: "#.##" rounding, unused here.
- `whiteNotificationBar()`: status-bar styling.
- `ASYNC_SaveSalesentry`: insert, update or delete as above.
- `ASYNCGetInvTransDetails`: loads lines.
- `ASYNCGetNewBillNo`: `select isnull( max(CWVWUDTransIdforDisp),0)+1 as NewBillNo From transactionsmaster where TTId=11`, writes the bill-no field. Never executed.
- `ASYNCRefreshaccountsledgers`: runs `GetRecentPartieswithContractVehicles` into local `RecentPartieswithContractVehicles(Transid, VId, PartyLedgerId)`, a vehicle-contract leftover. Unreachable.
- `MyPrintDocumentAdapter.onLayout()`, `onWrite()`, `pageInRange()`: print adapter (doc name "receipt_test.pdf"). In the decompiled output a `finally` closes the document inside the page loop, so it looks broken; unclear from decompiled code, and unreachable anyway.

**Rules & edge cases.**
- **Accounting inconsistency.** By the app's convention (`TransLedgerId` = Target/Dr, `SourceLedgerId` = Source/Cr, as in `accounttransactions_act`), the header debits ledger 151 and credits the supplier. The inserted detail rows do the opposite: supplier "Target"/Dr and 151 "Source"/Cr. Ledger 151 is used for any TTID, including the sales branch.
- **Editing changes the date.** Edit mode never copies the stored date into `defaulttransdate`. Saving without re-picking the date silently moves the bill to **today**.
- **Edit overwrites numbering and ledgers.** Edit sets `FWVWTransId` (the system voucher number) to the user's bill number. It does not update the header's TransLedgerId/SourceLedgerId when the supplier is changed; only `InvTransMaster` and the detail rows change.
- **Update and Delete are not permission-checked** here (no UpDateState/DeleteState test); only the list's FAB (AddState) and the LIST icon (ViewState) are gated.
- **Bill numbers** are typed by hand, with no uniqueness check.
- **Amounts** are `int` throughout: no paise and no rounding (roundoff is always 0).
- **Supplier picker.** The local `Suppliers` table is created only by `customers_list`. On a device where the Suppliers screen was never opened, tapping the supplier box raises an uncaught SQLite "no such table" error and the app crashes. An empty cache gives an empty picker.
- **Connections are mixed:** saving uses the selected-branch connection, reading uses the login-branch connection.
- **Unused extras:** `qty`, `caltype`, `screentype` and `listheading` are never used. The TTID 11/16/75 branches are unreachable from the UI.

## Item line editor — `invtrans_rowdetails`
| | |
|---|---|
| **Type** | Activity (DarkActionBar app theme) |
| **Layout(s)** | `activity_invtrans_rowdetails.xml` |
| **Opened from** | `invtransactnew.onContactSelected` (rc 1003; `actiontype`=1003, `ttid`, `receiveddata`, `Currentlotno`=0) · `newloancreation` (product lines of a new MC loan on inventory-enabled lines; same extras plus `hideprice`=false). `newloancreation.ttid` is never assigned, so it always sends 0 |
| **Opens** | `spinnersearch_list` (title "Select Item", rc 1002 `product_requestcode`) |
| **Data** | Local SQLite only. For ttid ≠ 21: `defaulttables.fetchitemslistforpurchase` → `select ItemCode,ItemName,0 as Price from ItemMaster order by ItemName`. For ttid 21 only: `fetchitemslist` → `select ItemCode,ItemName,PRate as Price from stockitems a order by ItemName`; no caller passes 21, and `stockitems` is never created anywhere in this APK. Name lookup: `getitemnameforitemcodefromstok` (`ItemMaster`) |

**Purpose.** Edit one line of a purchase note or a loan's product list: item, quantity and price; the line total is computed.

**UI.** Title "Item Details". Hidden S.No. "Product": read-only box; one tap opens the picker. "Qty" (numberSigned, max 6 chars). "Price" (max 6). "Total Amount": read-only box. Buttons "CHANGE" and "REMOVE".

**Menu / actions.** "CHANGE" returns the edited line. "REMOVE" asks the caller to delete it. Back returns nothing, so the line is unchanged.

**How it works.**
1. It loads `receiveddata` (`invtransdetails_data`).
   - Existing item (ItemCode ≠ 0): fills the name, qty, rate and total, then focuses and selects Qty.
   - Blank line: focuses Product.
2. The extra `hideprice`=true would hide Price, Total and their labels. No caller passes true. `globalvars.hideprice` is a `static final false` that nothing reads, so prices are always shown.
3. Picker result (rc 1002, resultCode 1) sets ItemCode and Itemname, sets Price to the returned price (always **0** for the ItemMaster list, because items carry no price), then focuses Qty and opens the keyboard.
4. Qty watcher:
   - Digits only: BilledQty = qty, Rate = price (if digits), TotalAmt = BilledQty × Rate, shown through `roundTwoDecimals`.
   - Otherwise (empty or "-5"): BilledQty = 0 and the total is recalculated.
5. Price watcher: when Qty is non-empty, the same recalculation runs. It parses Qty without a digits check.
6. CHANGE calls `setResult(1003, receiveddata)`; REMOVE calls `setResult(1004, receiveddata)`. Both then `finish()`.

**Functions.** `onCreate()` · `lambda$onCreate$1` (item picker) · qty/price `TextWatcher.onTextChanged()` · focus listeners (`lambda$onCreate$2/3/4`, reset the "being changed" flags) · `lambda$onCreate$5` (CHANGE) · `lambda$onCreate$6` (REMOVE) · `onActivityResult()` · `showincompletedataalert()` (never called; would NPE on the null `etlotno`) · `roundTwoDecimals()`.

**Rules & edge cases.**
- Quantity, price and total are integers (`Integer.parseInt`, `int` fields).
- No validation: an empty item or zero quantity is accepted.
- A Qty of "-" followed by a price edit crashes the app (`NumberFormatException`). A negative Qty typed before editing Price can be stored as negative through the price watcher.
- The "Total Amount" box cannot be typed into.

## Purchase bills list — `inventorytransactionslist_act`
| | |
|---|---|
| **Type** | Activity, theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_transactionslist.xml` + row `translist_row.xml` via `transist_adapter`; menu `menuonlywithsearchoption` |
| **Opened from** | `invtransactnew` LIST icon (rc 1000; `ttid`, `caltype`="app"), only with ViewState |
| **Opens** | `invtransactnew`. FAB (AddState) for ttid 116: `actiontype`=1006, `qty`=1, `screentype`=6, rc 1006. The ttid 16 branch (`qty` −1, screentype 17) and ttid 75 branch (`qty` 1, screentype 6) are unreachable. Row tap for ttid 116: `actiontype`=1007, `qty`=−1, `screentype`=17, `transdata`, rc 1007. The ttid 11 branch is unreachable |
| **Data** | Server (`branchconnString`): `select a.Transid, PartyLedgerId as TransLedgerId, FWVWUDTransId, TransDate, LedgerName as Particulars, TransAmount From tblTransactionsmaster a, InvTransMaster c, tblLedgers b where a.TransId=c.TransId and c.PartyLedgerId=b.LedgerId and TransDate>='<from>' and TransDate<='<to>' and Transtype=<ttid> order by TransId desc`. No local writes. Rights `nav_stockpurchase` (AddState only) |

**Purpose.** Browse purchase bills in a date range, open one to edit or delete it, or add a new one.

**UI.** Title "Purchase Bills" (no title is set for other TTIDs). Date bar "From : <date>" and "To : <date>"; tap either to open a DatePicker. Refresh ImageButton. Header No / Date / Party / Amount. Rows show bill no, date (dd-MM-yyyy), supplier name and amount (`##,##,##,##0`). FAB.

**Menu / actions.** Search matches supplier name (case-insensitive) or bill no. Changing a date does **not** reload the list; tap refresh. Back collapses search first.

**How it works.**
1. The default range starts at `globalvars.accyearfromdate` minus 7 days (`Calendar.add(6 = DAY_OF_YEAR, -7)`) and ends at `globalvars.accyeartodate` (current financial year, soft setting 20).
2. `ASYNCGetTranslist` shows the ProgressDialog "downloading, please wait ..." and fills `transactionsmaster_data` (TransId, FWVWUDTransId, Particulars, TransDate, TransAmount as int, TransLedgerId).
3. Results:
   - (1006, 1006): append the returned `transdata` at the **bottom**, even though the list is sorted descending.
   - (1007, 1007): replace by TransId.
   - (1007, 1008): remove.

**Functions.** `onCreate()` · `lambda$onCreate$1` (refresh) · `lambda$onCreate$3/2` (from-date) · `lambda$onCreate$5/4` (to-date) · `lambda$onCreate$6` (FAB, per-TTID extras) · `onCreateOptionsMenu()` · `onOptionsItemSelected()` · `onActivityResult()` · `onContactSelected()` (per-TTID edit extras) · `ASYNCGetTranslist` · `whiteNotificationBar()`.

**Rules & edge cases.**
- Row taps are not ViewState-gated here.
- Errors are silent.
- In practice only TTID 116 reaches this screen.
- Every nested entry screen has its own LIST icon, so the activity stack can keep growing.

## Stock in hand — `stockinhand_list`
| | |
|---|---|
| **Type** | Activity, theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_employeesearch.xml` + row `stockinhand_row.xml` via `stockinhand_adapter`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer `nav_StockinHand` ("Stock in Hand") |
| **Opens** | — |
| **Data** | Server (`selbranchconnString`): `exec StockinHand` (stored procedure, no parameters) returning `Itemcode, ItemName, Purchase, Issued, Balance`. No local tables |

**Purpose.** Read-only report of the current stock balance per item. The calculation is done server-side and the procedure body is not in the APK.

**UI.** Title "Stock in Hand". FAB hidden. Rows show item name and Balance as an integer. Search matches case-sensitive `ItemName.contains`. ProgressDialog while loading.

**Menu / actions.** Search only. Row tap is a no-op (`onContactSelected` is empty).

**How it works.** `ASYNCAccountsLedgers` runs the proc and maps Purchase → `BilledQty`, Issued → `SoldQty`, Balance → `Balance` in `stockinhand_data`. Only name and balance are displayed.

**Functions.** `onCreate()` · `onCreateOptionsMenu()` · `onOptionsItemSelected()` · `onContactSelected()` (empty) · `ASYNCAccountsLedgers` · `whiteNotificationBar()`.

**Rules & edge cases.** No per-menu rights check beyond the drawer item's visibility. Errors are silent. The balance is truncated to an integer.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `transist_adapter` | RecyclerView.Adapter + Filterable | `inventorytransactionslist_act` | Row `translist_row`: bill no (`FWVWUDTransId`), date dd-MM-yyyy, Particulars, value `##,##,##,##0`. S.No = position+1 is set but its view is gone; the lot-no and type views are unused. Click → `onContactSelected(transactionsmaster_data, pos)`. Filter: Particulars lower-case contains, or bill no contains |
| `stockinhand_adapter` | RecyclerView.Adapter + Filterable | `stockinhand_list` | Row `stockinhand_row`: ItemName + `Balance.intValue()`. Click → `onContactSelected` (no-op in the screen). Filter: case-sensitive ItemName contains |
| `itemmaster_adapter` | RecyclerView.Adapter + Filterable | `itemmaster_list` | Row `itemmaster_row`: ItemName only. Click → `onContactSelected(itemmaster_data, pos)`. Filter: ItemName contains |
| `stockgrp_adapter` | RecyclerView.Adapter + Filterable | `stockgrp_list` | Row `stockcat_row`: GrpDesc. Click → `onstockgroupselected(stockgrp_data, pos)`. Filter: GrpDesc contains |
| `stockcat_adapter` | RecyclerView.Adapter + Filterable | `stockcat_list` | Row `stockcat_row`: CatDesc. Click → `onContactSelected(stockcat_data, pos)`. Filter: CatDesc contains |
| `stockUOM_adapter` | RecyclerView.Adapter + Filterable | `stockUOM_list` | Row `stockcat_row`: UOMDesc. Click → `onContactSelected(stockUOM_data, pos)`. Filter: UOMDesc contains |
| `transactionsmaster_data` | Serializable model (extra `transdata`) | `invtransactnew`, `inventorytransactionslist_act`, `transist_adapter` | TransId, FWVWUDTransId (user bill no), TransDate (`java.sql.Date`), TransAmount (int), TransLedgerId (party ledger), Particulars (party name), TTId, CId, totqty, roundoff (Double). Also IntentNo/IntentValue, Create/ModifyUserLedgerId, CreatedAt/ModifiedAt (unused here) |
| `customerdetails_data` | Serializable model | `customers_list` only | LedgerId, LedgerName, AddLine1–3, City, PinCode, MobileNo1/2, EMail, GroupId, IsDefault, IsReserved, Screentype. Only an empty instance is passed as `ledgerdata` and nothing reads it; suppliers actually use `partydetails_data` |
| `salesdetailsforandroid_data` | Plain model (not Serializable) | 12 screens here (all except `invtrans_rowdetails` and `stockinhand_list`) plus 35 other classes, e.g. `navigationdrawactivity`, `syncresources`, `userroleprevilages_act` | One row of local `tblSalesDetailsforAndroid` = one role's rights on one drawer menu: UserLevelId, mnuId, mnuname, mnudisplayname, showid, ShowState, ViewState, UpDateState, AddState, DeleteState. `globalvars.salesdetailsforandroid_datalist` holds the current role's rows. A new instance has every right false |
| `itemmaster_data` | Serializable model (extra `itemmasterobj`) | `itemmaster_list`, `itemmaster_activity`, `itemmaster_adapter` | ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel, Transid (unused) |
| `compwiseop_data` | Serializable model | `customerdetails_act` | Company-wise opening balance: CId, CWOPUId, LedgerId (Double), OPBalType, OpeningBalance (Double). A one-row list (CId 1) is built on save and never used |
| `stockinhand_data` | Serializable model | `stockinhand_list`, `stockinhand_adapter` | Itemcode, ItemName, BilledQty (= Purchase), SoldQty (= Issued), Balance (Double) |
| `stockgrp_data` | Serializable model (extra `stockgrpobj`) | `stockgrp_list`, `stockgrp_adapter`, `stockgroup_act` | GrpId, GrpDesc, HSNSACCode (int), IsActive (`getActive/setActive`), DisplayIndex |
| `stockcat_data` | Serializable model (extras `stockcatobj` in, `stockobj` out) | `stockcat_list`, `stockcat_adapter`, `stockcategories` | CatId, CatDesc, DisplayIndex, IsActive |
| `stockUOM_data` | Serializable model (extra `stockuomobj`) | `stockUOM_list`, `stockUOM_adapter`, `stockUOM_act` | UOMId, UOMDesc, DisplayIndex, IsActive |

Helpers used here that belong to other groups:
- `invtransdetails_adapter` / `invtransdetails_data`: the line model, all int except the name (SNo, Itemcode, Itemname, BilledQty, ActualQty, Rate, TotalAmt, Transid). The adapter shows S.No, item name (looked up in `ItemMaster`), qty, price and total, with an optional `hideprice` flag.
- `partieslist_adapter` / `partydetails_data`.
- `spinnersearch_list`: returns resultCode 1 with `spinnerserchobjectname`, `displaymember` and `valuemember`.
- `defaulttables` lookups listed above.
- `customersearchlistdivider`: list divider.

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_customerdetails.xml` | `customerdetails_act` | CoordinatorLayout. Labelled EditTexts: "Customer Name" (`customerdetails_etledgernameid`, textCapWords), "Add Line 1", "Add Line 2", "City", "Pincode" (number), "Mobile  No1", "Mobile No2" (number), "EMail" (textEmailAddress). Hidden LinearLayoutCompat with groupid/isdefault/isreserved/screentype EditTexts. Hidden CheckBox "ACTIVE". Buttons "SAVE" (`customerdetails_submitid`) and "DELETE" (`customerdetails_deleteid`). ProgressBar `customerdetails_pbar` (gone) |
| `activity_employeesearch.xml` | `itemmaster_list`, `stockgrp_list`, `stockcat_list`, `stockUOM_list`, `stockinhand_list` (also 12 list screens in other modules, e.g. `employeesearchlist`, `accledgers_searchlist`) | Toolbar; `include` of `customersearchlist` (RecyclerView `recycler_view`); FAB `addnewemployeeid` (ic_input_add); hidden overlay `progress_Overlay` with ProgressBar `capbarid` (these lists use a ProgressDialog instead) |
| `activity_invtrans_rowdetails.xml` | `invtrans_rowdetails` | S.No + `boxdetails_etsnoid` (gone). "Product" read-only box `boxdetails_productnameetid`. "Qty" `boxdetails_etqtyid` (numberSigned, maxLength 6). "Price" label `invrow_pricelabelid` + `boxdetails_etpriceid` (maxLength 6). "Total Amount" label `invrow_totallabelid` + read-only `boxdetails_ettotalamount`. Buttons "CHANGE" (`boxdetails_savebuttonid`) and "REMOVE" (`boxdetails_deletebuttonid`) |
| `activity_invtransactnew.xml` | `invtransactnew` | Toolbar holding "No" + bill no `receiptnote_invoicenoetid` (number) and "Date"/"தேதி" `invtransactions_transdateid`. "Party" label `nametextid` + read-only supplier box `receiptnote_partynameid`. Included `invtransdetails_row` as the column header (S.No/Product/Qty/Price/Amount). RecyclerView `recycler_view`. "Total"/"ஆக மொத்தம்" + `invtransdetails_totalamountid`. Buttons "Update"/"சேமி" (`invtransdetails_savebuttonid`) and "DELETE"/"நீக்கு" (`invtransdetails_deletebuttonid`). FAB `receiptnote_fab` ("add new item"). ProgressBar `receiptnote_pbarid` (gone) |
| `activity_itemmaster.xml` | `itemmaster_activity` | ScrollView: "Item Name" `stockcat_itemnameid`; read-only picker boxes "UOM" `stockcat_stockuomid`, "Stock Group" `stockcat_stockgrpid`, "Stock Cat" `stockcat_Stockcatid`; "Reorder Level " `stockcat_reorderid` and "Minimum Order Qty " `stockcat_minimumorderid` (numberSigned). Buttons "UPDATE" `newaccled_submitid` and "DELETE" `newaccled_deleteid`. ProgressBar `newaccled_pbarid` (gone) |
| `activity_partieslist.xml` | `customers_list` (also `parties_list`, `fintrans_list`) | Toolbar; RecyclerView `partieslist_recycler_view` (rows `partieslistrow`); ProgressBar `partieslist_pbar` (gone); FAB `partieslist_addnewpartyfabid` |
| `activity_stockcat.xml` | `stockcategories`, `stockUOM_act` | "Stock Name" `stockcat_categoriesnameid`; "Show Index" `stockcat_Showindexd`; CheckBox "Active" `stockcat_isActiveid`; buttons "UPDATE" `newaccled_submitid` and "DELETE" `newaccled_deleteid`; ProgressBar `newaccled_pbarid` (gone) |
| `activity_stockgroup.xml` | `stockgroup_act` | "Group Name" `stockgroupname_id`; "HSN Code" `stockgrphsncode_id` (numberSigned); CheckBox "Active" `stockgroup_chkisvisibleid`; "Show Index" `stockgroup_etshowidid` (numberSigned); buttons "SAVE" `designationlevel_submitbuttonid` and "DELETE"/"நீக்கு" `designationlevel_deletebuttonid` (both gone until code shows them); ProgressBar `profilequalifier_progressBar`; hidden TextView |
| `activity_transactionslist.xml` | `inventorytransactionslist_act` | Toolbar. Date bar `lytdaterangeid`: "From : " `ds_fromdateid`, "To : " `ds_todateid` (placeholder dates from 2020), refresh ImageButton `acctranslistrefreshid`. Header S.No (gone) / "No" / Colour (gone) / "Date" / Type (gone) / "Party" / "Amount". RecyclerView `recycler_view`. FAB `transactionslist_fab` |
| `itemmaster_row.xml` | `itemmaster_adapter` | One TextView `itemmaster_nameid` (item name) |
| `stockcat_row.xml` | `stockcat_adapter`, `stockgrp_adapter`, `stockUOM_adapter` | TextView `accledgers_ledgername` (description); hidden TextView `tvcollectedamountnewid` |
| `stockinhand_row.xml` | `stockinhand_adapter` | `accledgers_ledgername` (item name), `accledgers_itembalance` (balance) |
| `translist_row.xml` | `transist_adapter` | S No (gone), Bill No `translistrow_billnoid`, Lot No (gone), Date `translistrow_dateid`, Type (gone), Particulars `translistrow_particulars`, Value `translistrow_transvalue` |

---
[← brain.md](../../brain.md) · Module 8.17 of 20 · Previous: [8.16 Admin masters: employees, branch employees, access control, loan methods and finance lines](16-admin-masters-employees-branch-employees-access.md) · Next: [8.18 Printing: collection list prints, receipts and Bluetooth printers](18-printing-collection-list-prints-receipts-bluetooth.md)
