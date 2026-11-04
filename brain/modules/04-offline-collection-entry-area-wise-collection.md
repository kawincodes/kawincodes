# 8.4 Offline collection entry (area-wise collection list & individual collection)

[← brain.md](../../brain.md) · Module 8.4 of 20 · Previous: [8.3 Offline mode: download, upload and sync services](03-offline-mode-download-upload-sync-services.md) · Next: [8.5 Online collection entry & editing](05-online-collection-entry-editing.md)

This module is the field executive's daily collection workspace. `downloaddata` first fills the local SQLite tables (`MobileAppCollData`, `Parties`, `PrevCollections`, `SubAreaDetails`). Tapping an area on the home screen then opens **`tabbedcollectionlistmviewpager`**, which shows one swipeable page per collection area listing every loan, with an inline amount box. Tapping a party opens **`tabbedindividualcollectionentry`**, a swipeable per-party form. It writes every amount to `MobileAppCollData` on each keystroke (there is no Save button), and `uploadcollection` uploads those rows later. The module also has three online helpers that talk to SQL Server directly. **`directcollectionentry`** books one collection straight into the server accounts. **`selectedloancollectiondetails`** shows one loan's full collection history, with print/PDF/share and a hand-off to collection editing. **`collectionedit_individualcollection`** lets the user re-split one party's collection between cash and bank while a saved group collection is being edited. `tabbedcollectionlist` and `tabbedcollectionlistadaptermviewpager` are unreachable legacy code.

## Area-wise collection list (party pages per collection area) — `tabbedcollectionlistmviewpager`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) hosting a classic `ViewPager` + `FragmentPagerAdapter` of static inner `PlaceholderFragment` pages, one per collection area. Manifest label `@string/title_activity_tabbedcollectionlist` (placeholder text "tabbedcollectionlist"), theme `AppTheme.NoActionBar`; action-bar title "Parties" with Up arrow. |
| **Layout(s)** | `activity_tabbedcollectionlistforviewpager1.xml`; per page `fragment_tabbedcollectionlist.xml` (includes `customersearchlist.xml` → `recycler_view`); rows `tabbedcollectionlistrow.xml` (through `tabbedcollectionlistadapter`); menu `menucustomersearchlist`. |
| **Opened from** | Home screen area list: `navigationdrawactivity.onContactSelected(listpos)` for a row of `areawisecollectionlistadapter` whose `SubAreaNo > 0` (the summary rows −1 "Live Loans Collection"/நடப்பு வசூல், −3 "On day Collection" and −2 "Pending Loans Collection"/நிப்பு வசூல் do nothing). Extra `listindex` = row position. Before starting it, navigationdrawactivity loads every `MobileAppCollData ⋈ Parties` row (ordered `x.SubAreaNo, x.MCLoanDate desc`) into `globalvars.tabbedcollectionlistdataList`. |
| **Opens** | `tabbedindividualcollectionentry` (row tap, rc 1); `partylistwithprevloansfornewloanselection` (FAB; `AreaName`, `SubAreaNo`, `calledfrom`=9901 = offline party/loan creation; rc 1); `partyrearrange` (menu "Rearrange Parties"; `PLID`=SubAreaNo, `areaname`; rc 1); `maps_partydisplay` (menu "Show Parties in MAP"; `PLID`, `areaname`; rc 1). |
| **Data** | In memory: `globalvars.areawisecollectionlistdataList` (the pages) and `globalvars.tabbedcollectionlistdataList` (the rows). SQLite `MobileAppCollData`: this class writes `isMarked`; the adapter writes `Collected`, `CashCollection` and `IsVisited` and reads `PrevCollections`. Soft setting `AreawisePartiesDisplayOrder` (id 102; pref default 1) plus the row flags listed under the adapter. Special access 13 (add new loans) and 20 (rearrange), plus 3/4/5 for row columns. |

**Purpose.** This is the executive's main offline working list. Swiping left/right moves from one collection area (sub area) to the next. Each page lists that area's downloaded loans with today's amount box and a running "Total Collection". From here the executive can quickly key in amounts, mark parties, open the full per-party form, add a new loan, re-order parties or see them on a map.

**UI.**
- Page header `section_label` (layout default "Collection Area Name"/"வசூல் ஏரியா பெயர்") shows `SubAreaName`; its tag holds `SubAreaNo` (no click action in this class).
- Party list (`recycler_view`, divider `customersearchlistdivider`). Each row shows an optional photo, an optional party no. (`CWLedgerId`) + ":", the colour-coded name, **Loan** (`LAmount`) and **Due** (`InsAmount`). It also has an editable amount box with today's `Collected`, an optional loan method (`LoanMethodDesc`) and an optional "Visited" checkbox. Special access 3 adds **Coll** (`PrevCollected`), 4 adds **Stas** (`LoanStatus`, or "PaidDues/PassedDues" when `loanstatusmethod_10` ≠ 1) and 5 adds **bal** (`Balance` = MCLoanAmountCollectable − PrevCollected). An optional strip shows the last 5 collections (dd-MM-yy + amount).
- Name colour: `markedcolor` #061DED (blue) when marked or `IsAttentionRequired`; otherwise `nipcolor` #DC0000 (red) when `IsNIP`; otherwise `warningcolor` #FF4E0C (orange) when `istobeWarned`; otherwise black.
- Footer `tcl_tvtotalcollectionamountid`: "Total Collection:<n>" = Σ `Collected` of the page's loans (due collection only; penalty/savings are not included).
- FAB `tcl_fab` (new loan) is visible only with special access 13 or `globalvars.isFullAccessAllowed`.

**Menu / actions.**
- Search (`action_search` SearchView, always shown) filters rows whose party name contains the text (case-insensitive) or whose `CWLedgerId` equals it exactly.
- "Rearrange Parties" (`partyrearrangemenu`) is visible only with special access 20 and opens `partyrearrange`. "Show Parties in MAP" (`showpartiesinmapmenu`) opens `maps_partydisplay`. Up/Home first collapses an open search view, otherwise acts as Back.
- Row tap opens the entry form. Long-press on the name toggles "marked". Double-tap on the Due text puts the installment amount in today's box. Typing in the box saves at once. Ticking "Visited" saves `IsVisited`.

**How it works.**
1. `onCreate`: sets the toolbar, builds the `FragmentPagerAdapter` and opens SQLite `venusmicrofinv005`. It reads `listindex` and calls `setCurrentItem(listindex)`, then sets the static `curscreensubareano`/`curscreensubareaname` from `areawisecollectionlistdataList[listindex]`. `onPageSelected` updates both fields on every swipe.
2. Page count = `areawisecollectionlistdataList.size() − 3`, which excludes the three trailing summary rows. Page *p* belongs to `areawisecollectionlistdataList[p]`.
3. `PlaceholderFragment.onCreateView`:
   - Fills the label and decides FAB visibility.
   - Builds the page list from every `globalvars.tabbedcollectionlistdataList` row whose `SubAreaNo` equals the page tag, and sums `Collected` into the total.
   - Sorts by `AreawisePartiesDisplayOrder`: 1 = `UDLedgerId` ascending; 2 = `MCLoanDate` **descending**; 3 = `CWLedgerId` ascending; any other value keeps the load order.
   - Also contains a loop that should merge offline new loans, but it iterates a freshly created empty list, so new loans created offline never appear on this screen.
4. `setUserVisibleHint(true)` detaches and re-attaches the fragment, so a page is rebuilt (fresh rows and total) each time it becomes visible.
5. `oncollectionamountchanged(colamount, SubAreaNo, row, ismarked)` (called by the adapter after it has written SQLite): in memory it sets `Collected`=colamount, `CashCollection`=colamount and the marked flag, then recomputes the area total ("Total Collection:%s").
6. `onmarkchanged(row, ismarked)`: sets `IsMarked` in memory, runs `Update MobileAppCollData set isMarked=<true|false> Where MCLoanId=<id>` and refreshes the list (name turns blue).
7. `onContactSelected(row, pos)` starts `tabbedindividualcollectionentry` with these extras:
   - `SubAreaName` (actually the party name), `SubAreaNo`, `MCLoanId`, `collectionareaname` (= `Tag`).
   - `listindex` = the row's index in this page's sorted list.
8. `onActivityResult` (any result): rebuilds the page from the in-memory global list, which the entry screen has updated in place. It then re-sorts (order 2 is now **ascending**), recomputes the total and refreshes.
9. `onPostResume()` computes the current area total and throws it away (dead code).

**Functions.**
- `onCreate()` — pager, extras, toolbar, page-change listener.
- `PlaceholderFragment()` / `newInstance(int)` — fragment with `section_number`; the constructor enables the options menu.
- `PlaceholderFragment.setUserVisibleHint(boolean)` — forces a page rebuild when the page becomes visible.
- `PlaceholderFragment.onCreateOptionsMenu()` — inflates `menucustomersearchlist`, gates rearrange on access 20, wires the SearchView to the adapter filter.
- `PlaceholderFragment.onOptionsItemSelected()` — Home, rearrange and map actions.
- `PlaceholderFragment.onCreateView()` — builds the page list, total, sort and FAB.
- `lambda$onCreateView$0()` — FAB → `partylistwithprevloansfornewloanselection`.
- `oncollectionamountchanged()` / `onmarkchanged()` — adapter callbacks (in-memory sync, total, `isMarked`).
- `onBackPressed()` (fragment) — collapses search or calls activity Back.
- `onContactSelected()` — opens the entry form with the in-area list index.
- `onActivityResult()` — reloads, re-sorts and re-totals the page.
- `SectionsPagerAdapter.getItem()` / `getCount()` — page factory and count (size − 3).
- `onPostResume()` — no effect.

**Rules & edge cases.**
- **Wrong party opens after a return (sort bug).** With `AreawisePartiesDisplayOrder`=2 the first display sorts newest-first and so does the entry screen, but `onActivityResult` re-sorts oldest-first. Until the page is rebuilt (swipe away and back), later taps send a `listindex` that opens a different party in the entry form. Verified in smali (`lambda$onCreateView$2` vs `lambda$onActivityResult$5`).
- **Warning colour never shows.** navigationdrawactivity sets `istobeWarned` from `Boolean.parseBoolean(c.getString(21))`, and column 21 of its query is `LoanMethodDesc`. The orange warning colour therefore effectively never appears in this list.
- **SQL boolean literals.** Marked/visited writes put unquoted `true`/`false` into SQL. SQLite only accepts these keywords from version 3.23 (system SQLite on Android 11+); on older system SQLite (minSdk 28 = Android 9) the statement would fail with "no such column".
- **Shared static fields.** `curscreensubareano`/`curscreensubareaname` are static and shared by all pages. Menu actions and `onActivityResult` use the currently selected page's area.

## Legacy area-wise collection list (ViewPager2) — `tabbedcollectionlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) with `ViewPager2` + `FragmentStateAdapter` of `PlaceholderFragment`s; manifest label placeholder "tabbedcollectionlist", theme `AppTheme.NoActionBar`; green action bar (`@color/Green` #008000), title "Parties". **Not started by any class** (declared in the manifest only; no Java, smali or resource reference), so it is unreachable legacy code. |
| **Layout(s)** | `activity_tabbedcollectionlist.xml`, `fragment_tabbedcollectionlist.xml`, rows `tabbedcollectionlistrow.xml`; menu `menucustomersearchlist`. |
| **Opened from** | None (would expect extra `listindex`). |
| **Opens** | `tabbedindividualcollectionentry` (same extras as above, rc 1); `newloancreation` for offline new-loan rows (`CallType`=9008 `requestcodes.editloanrequestcodefromOFFLINEdata`, `passedparty`, `passedloandata`; rc 9008); `partyrearrange` (tap on area label: `PLID`, `areaname`; rc 1); `partylistwithprevloansfornewloanselection` (FAB, `calledfrom` 9901). |
| **Data** | Same as the live list, plus `defaulttables.getnewloanlist()` (`NewMCLoans ⋈ NewParties ⋈ SubAreaDetails`) and `defaulttables.partyobject()` (`Parties`). Writes `MobileAppCollData.BankCollection`, `cashcollection`, `ismarked`. |

**Purpose.** It is an older version of the area list. Unlike the live version, it shows offline-created loans in the area list.

**How it works.**
1. `onCreate` performs a fake drag (`beginFakeDrag`/`fakeDragBy(0.01f)`) to force page layout and then jumps to `listindex`. Page count = `areawisecollectionlistdataList.size() − 2`, which leaves an empty extra page for the −1 summary row.
2. Each page merges new loans of the area from `getnewloanlist`:
   - Fields: `Collected` = `ondaycollection`, `Balance`/`LAmount` = `MCLoanAmountCollectable`, `Remarks` = marker `"00000"`.
   - Then the downloaded rows are added; the total includes on-day collections of new loans.
   - New-loan rows show in `newloancolor` #0D54E3 with the amount box disabled; tapping one opens `newloancreation` in edit mode.
   - Sorting is applied in `onActivityResult` only (same 3 orders; order 2 = newest first).
3. `oncollectionamountchanged` updates `Collected` and recomputes `CashCollection = Collected + PenaltyCollected + SavingsCollected − BankCollection`. It writes `Update MobileAppCollData set BankCollection=…` and `set cashcollection=…` for that `MCLoanId`, then shows the total. `onmarkchanged` writes `ismarked`.
4. The fragment only handles search. "Rearrange Parties"/"Show Parties in MAP" have no handler (the activity's only branch, for item id 0, is unreachable). `onBackPressed()` (adapter callback) is empty.

**Functions.**
- `onCreate()`, `lambda$onCreate$0/1` — insets and page transformer (no-op).
- `onCreateOptionsMenu()` — returns true without inflating.
- `onOptionsItemSelected()` — Home/Back.
- `PlaceholderFragment.newInstance()` / `setUserVisibleHint()` / `onCreateView()` — page build with new-loan merge.
- `lambda$onCreateView$3` — area label → `partyrearrange`.
- `lambda$onCreateView$4` — FAB.
- `onCreateOptionsMenu()` (fragment) — search.
- `onActivityResult()` — reload + sort.
- `oncollectionamountchanged()`, `onmarkchanged()`, `onContactSelected()`, `onBackPressed()`.
- `SectionsPagerAdapter.createFragment()` / `getItemCount()` / `addFragment()` (empty).

**Rules & edge cases.**
- The page list, total TextView and current area are static fields shared by all fragments. Because ViewPager2 pre-creates the neighbouring pages, totals and the current area can belong to a neighbouring page.
- New-loan detection compares `getRemarks() == "00000"` by reference, so it works only for the in-code literal.

## Individual collection entry (per-party swipe form) — `tabbedindividualcollectionentry`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) hosting a classic `ViewPager` (`FragmentPagerAdapter`) of static inner `PlaceholderFragment` pages, one page per loan of the selected area. Manifest: label `@string/title_activity_tabbedindividualcollectionentry` (placeholder text "uploadalloweddetails"), `logo=@color/Orchid`, theme `AppTheme.NoActionBar`, `windowSoftInputMode=stateAlwaysHidden`. Toolbar title "Collection Entry". JADX mis-decompiled `onCreateView`; the conditions below were verified in smali. |
| **Layout(s)** | `activity_tabbedindividualcollectionentry.xml` (toolbar + ViewPager `container`); each page `fragment_tabbedindividualcollectionentry.xml`; product rows `invtransdetails_row.xml` (through `invtransdetailsdisplay_adapter`). |
| **Opened from** | `tabbedcollectionlistmviewpager` row tap (extras `SubAreaName` (= party name), `SubAreaNo`, `MCLoanId`, `collectionareaname` (= Tag), `listindex`; rc 1). `editing_collectionswithfulloptions_act.onrowselected()` when `calledfrom`=9907 (online "Collection entry", drawer `nav_collectionentry`): it first sets `globalvars.tabbedcollectionlistdataList` to its own list and also passes `Islocallyfixed`; rc 1. Also the unreachable `tabbedcollectionlist`. |
| **Opens** | See the list after this table. |
| **Data** | SQLite `MobileAppCollData`: updates `Collected`, `CashCollection`, `PenaltyCollected`, `SavingsCollected`, `BankCollection`, `TargetLedgerId` by `MCLoanId`, and reads the row count per `SubAreaNo`. Also reads `Parties` (`Tag, MobileNo1, MobileNo2`), `PrevCollections`, `PrevLocations` (created if missing), `CashBankLedgers` and `SMSTemplates` (STId 16), the last two through `defaulttables`. Remote (`globalvars.selbranchconnString`): `getItemDetailsforloans @MCLoanid=<id>` (inventory products). In memory: rows of `globalvars.tabbedcollectionlistdataList`, edited in place. |

Screens and apps this one opens:
- `selectedloancollectiondetails` (FAB).
- `partydetails_act` (tap on the name; `ledgerid`, `SubAreaNo`, `CallType`=9904; rc 2).
- `spinnersearch_list` (target-ledger picker; title "வரவு வைக்கப்படும் கணக்கு" = "account to be credited"; `searchlist`, `selloandata`, `calledfrom`=11002; rc 11002).
- `documentsphotossearchlist` (`receivedloan` = mcloanobj with MCLoanId + PartyName, `calledfor`=100).
- External apps: Google Maps (`com.google.android.maps.MapsActivity`), the phone dialer (`ACTION_DIAL`), SMS (SmsManager or the default SMS app).
- The NGX Bluetooth device list (`BluetoothPrinter.INSTANCE.showDeviceList`).

**Purpose.** This is the full collection form for one party. The executive swipes through all parties of the area in list order. For each party they record today's loan-due collection, optional penalty/interest and savings, and the cash vs bank split. The form also shows the loan facts, last 5 collections and previous collection locations, and lets the executive call or SMS the party, open the party location, print, and open the full loan history.

**UI.**
- **Header.** Round party photo (`ilc_thumbnail`, only if `enablepartyimageloading`, id 21; URL = `partiesimagepath` (id 18) + `ImageFileName`, fallback icon). Name (`ilc_partynameid`, "Party Name"/"பார்ட்டி பெயர்") as `CWLedgerId.PartyName` when `showpartynoincollectionlist_6` (id 20), colour-coded marked→#061DED, NIP→#DC0000, warned→#FF4E0C, else black. TAG (overwritten from `Parties.Tag`) and REMARKS.
- **Loan facts.**
  - Loan Amount (`LAmount`) and Interest/Document (`interestamount` / `DocCharges`).
  - Date (`MCLoanDate`, dd-MM-yyyy) and Closing Date. `ilc_overduedays` appears beside the closing date only after it has passed, showing the number of days past it.
  - Due = "`InsAmount`/`DuesCount`" (the tag holds `InsAmount`).
  - Collected/வசூலிக்கப்பட்டது (`PrevCollected`) and Pending (`Balance`); these rows are shown only with special access 5.
  - Status row (only with special access 5): `LoanStatus` when `loanstatusmethod_10`=1, otherwise "PaidDues/PassedDues". In the second case, `ilc_additionaldue` = `PrevCollected mod InsAmount` (amount paid beyond whole installments) is shown in green; tapping it toggles to `InsAmount − extra` (amount still short of the next installment, DarkRed) and back (DarkGreen).
- **"Collection Entry" block.**
  - Loan Due Collection (`ilc_curcollectionamountid`; the whole row is hidden when pref `DisabletodayCollectionforIndividualcollectionEntry` is set).
  - Pen/Int Collection (only if `ispenaltyallowedincollectionscreen_12`, id 45) and Savings Collection (only if `issavingscollectionallowedincollectionscreen_13`, id 46).
  - Hidden row "Collected as/ In" (`ilc_curTargetLedgerId`, the target cash/bank ledger).
  - Cash Collection (computed; a disabled, non-editable box, `enabled="false"` in the XML) and Bank Collection, both shown only when `globalvars.bankaccountledgerid` > 0.
- **Products** (`inventorylayoutid`; only when `globalvars.curfin.isEnableinventory()`): S.No, Product, Qty, Price, Amount.
- **Contacts.** Mobile 1 and Mobile 2, each with Call and Message image buttons.
- **Action icons.** Location pin `ilc_googlemaplocatorid`, related documents `ilc_relateddocuments` (only if `EnableUploadLoanRelatedDocuments`, id 72) and print `ilc_printreceipt`. A layout row with "No Not Found", "No Not Found" and "Locate" buttons is `gone` and has no ids or code.
- **History tables.** "Prev 5 Collections" (`tblpcolid`: date dd-MM-yyyy, paid). "Prev 5 Locations" (`tblprevloationsid`: one date per row); its label is visible only when `vss_executivehandlingmethod`=2 (online).
- **FAB** `ilc_fab` opens the full loan history.

**Menu / actions.** There is no options menu (`onCreateOptionsMenu` returns true without inflating; the `action_settings` branch is unreachable). Actions:
- Double-tap in the Loan Due Collection box fills it with the installment (`InsAmount`) and saves.
- Tap on the name opens party details, but only with full access or special access 11. Tapping the Status label does nothing.
- The location pin opens a "Choose Action"/"What do you want to do?" dialog with three buttons. "Connect Printer" shows the Bluetooth device list. "Get Location" opens Maps at `http://maps.google.com/maps?q=loc:<lat>,<lng> (<PartyName>)`, using the party's stored `Parties.lat/lng` from the list query. "Cancel" closes it.
- Print, Call, Message, Related documents, FAB and the history tables are described under How it works.

**How it works.**
1. **`onCreate`.**
   - `initPrinterSafely()`: on API ≥ 31 without `BLUETOOTH_CONNECT` it requests `BLUETOOTH_CONNECT` + `BLUETOOTH_SCAN` (rc 101; no result handler, so the printer service starts only on a later visit). Otherwise it calls `mBtp.initService(this, mHandler)` (`mBtp` = shared `BluetoothPrinterMain.mBtp`).
   - It throws an `AssertionError` if there are no extras. It reads `listindex`, `SubAreaNo` and `Islocallyfixed` and opens SQLite.
   - It builds the static `items` list (global rows with this `SubAreaNo`) and sorts it: order 1 = `UDLedgerId` ascending, 3 = `CWLedgerId` ascending, otherwise (including 2) `MCLoanDate` descending (`Collections.reverseOrder(new Sortbyroll())`). The pager opens at `listindex`.
2. **Pages.** `SectionsPagerAdapter.getCount()` = row count of `select MCLoanId from MobileAppCollData where SubAreaNo=<n>` (runs on every call). Page *i* shows `items[i]`.
3. **Field state.**
   - Due, Savings and Penalty are first enabled only when `MCLDueCollId == 0`, but the next lines override that check: Due, Savings, Penalty and Bank enabled = `!Islocallyfixed`. Rows the online screen has already "fixed" are therefore read-only.
   - Bank and Cash are disabled while Due is empty or "0". Once Due is non-zero, Bank is enabled (Cash stays computed).
   - All fields are prefilled from the row: today's Collected, Bank, Cash, Savings, Penalty, and the target-ledger name from `CashBankLedgers`. Due, Bank and Cash select all their text on focus.
4. **Live save.** Every change recomputes `Cash = Penalty + Savings + Due − Bank`, writes it to `ilc_cashcollectionid` and saves immediately. Re-entrancy flags (`isduecollectionbeingchanged`, `ispenaltybeingchanged`, `issavingscollectionbeingchanged`) stop the watchers from triggering each other. Per field:
   - Due: `Update MobileAppCollData set Collected=<due> Where MCLoanId=<id>` + `set CashCollection=<cash>`; in-memory `Collected`.
   - Pen/Int: `set PenaltyCollected=…` + `set CashCollection=…`; in-memory penalty and cash.
   - Savings: `set SavingsCollected=…`, `set BankCollection=…`, `set CashCollection=…`; in-memory cash and savings.
   - Bank: `set BankCollection=…` + `set CashCollection=…`; in-memory savings, cash and bank. If Bank > Due + Penalty + Savings, the field shows the error "Bank Collection cannot be greater then Due Collection", but the value is still saved.
   - Target ledger: the picker result (rc 11002) runs `Update MobileAppCollData set TargetLedgerId=<valuemember>`. The ledger list is all `CashBankLedgers` of the finance for full-access users, otherwise only those with `Alloted=1`.
5. **Contacts and tags.** `select Tag,MobileNo1,MobileNo2 from Parties where LedgerId=<PartyLedgerId>`.
6. **Prev 5 Collections.** `select CollDate,Paid from PrevCollections where MCLoanid=<id> order by RowNo` builds TableRows. `PrevCollections` is filled by `downloaddata` (`PrepareLast5Collections…` procedures).
7. **Prev Locations.** `Create table IF NOT EXISTS PrevLocations (MCLoanId int, MCLDueColDate date,lat nvarchar,lng nvarchar)`, then `select MCLoanId,MCLDueColDate,lat,lng from PrevLocations where MCLoanid=<id>`. Each row shows a date; tapping it opens Maps at `q=loc:<lat>,<lng>, (<PartyName>)`. Only the online `editing_collectionswithfulloptions_act` fills `PrevLocations`.
8. **Products.** `ASYNCGetInvTransDetails` connects with `selbranchconnString` and runs `getItemDetailsforloans @MCLoanid=<id>` (columns SNo, ItemCode, ItemName, BilledQty, Rate, Itotal). It shows the list and hides `itemdetailspbarid`. Load errors are swallowed; `onPostExecute` rethrows a `RuntimeException` if the view is gone.
9. **Print.** If `mBtp.getState()` ≠ 3 (connected) it shows the toast "Printer is not connected". Otherwise it sets 72 mm width and NGX custom commands and prints only the bundled bitmap `assets/image/img.png` (the "Fins" app logo). No receipt text is built: a font is loaded and a dashed separator string is defined, but neither is used. In effect this is a printer test.
10. **Call.** `ACTION_DIAL tel:<number>` with flag `0x4000000` (`FLAG_ACTIVITY_CLEAR_TOP`; JADX shows the same-value constant `AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL`). On failure it shows "Unable to open phone app".
11. **SMS** (manual only; nothing is sent automatically on entry).
    - If `enabletsendSMSonCollection_38` (soft setting 71) is off, the toast "This feature is not enabled" appears.
    - Otherwise `sendsms()` fills template 16 (`SMSTemplates.SMSDesc`): `{partyname}`, `{loanamount}`=MCLoanAmountCollectable, `{loandate}` (dd-MM-yyyy), `{todaycollection}`=Collected, `{totalpaid}`=PrevCollected+Collected, `{balance}`=MCLoanAmountCollectable−(PrevCollected+Collected), `{datetime}`=now ("dd-MM-yyyy hh:mm a").
    - It sends only when `smsmethod`=1, through `SmsSender.sendMultipartSMS`: direct SIM SMS if `SEND_SMS` is granted, else the default SMS app opens prefilled. It then shows the toast "SMS Sent". With `smsmethod`=2 (HTTP gateway) nothing happens.
12. **Party details** (rc 2, resultCode 2, extra `passedparty`). The screen updates the global rows of that party (`PartyName`, `CWLedgerId`, `ImageFileName`) and redraws the name as "CW.Name" (always with the number), the tag and the photo.
13. **FAB.** With full access or special access 15 it copies `selbranch*` into `branchlevelReport_branchconnString/dbusername/dbpassword` and opens `selectedloancollectiondetails` with `MCLoanId`, `PartyName`, `Remarks`, `Tag` and `iscollectioneditallowed`=true (boolean). Otherwise it shows the Snackbar "Access Denied".
14. **Bluetooth.** `mHandler` shows the toasts "Not connected" (state 0/1), "Connecting..." (2) and "Connected: <device>" (3), and remembers the device name (message 4).

**Functions.**
- `onCreate()` — printer init, extras, builds and sorts `items`, pager.
- `initPrinterSafely()` — Bluetooth permission request or printer service start.
- `mHandler.handleMessage()` — printer state toasts.
- `saveBitmap(Context, Bitmap)` — writes `cache/prints/receipt.png`; never called.
- `onActivityResult()` (activity) — passes results to the fragments.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — no real menu.
- `Sortbyroll.compare()` — `MCLoanDate` ascending (used reversed).
- `SectionsPagerAdapter.getItem()` / `getCount()`.
- `PlaceholderFragment.newInstance(int)`.
- `PlaceholderFragment.onCreateView()` — visibility rules, prefill, watchers, contacts, history tables, listeners.
- TextWatchers `PlaceholderFragment.1`–`4` (due / penalty / savings / bank) — live recompute and SQLite save.
- `PlaceholderFragment.5.onDoubleTap()` — fill installment and save.
- `PlaceholderFragment.6.onTouch()` — routes touches to the gesture detector.
- `lambda$onCreateView$0` — toggle the extra/short due display.
- `lambda$onCreateView$4` — location/printer chooser; `$1` connect printer; `$2` open Maps; `$3` cancel.
- `lambda$onCreateView$5` — related documents.
- `lambda$onCreateView$6` — print logo.
- `lambda$onCreateView$7` — target-ledger picker.
- `lambda$onCreateView$8` — party details.
- `lambda$onCreateView$9` — no-op.
- `lambda$onCreateView$10` / `$13` — dial mobile 1/2.
- `lambda$onCreateView$11` / `$12` — SMS to mobile 1/2.
- `lambda$onCreateView$14` — full history (access 15).
- `lambda$onCreateView$15` — open a previous location.
- `getImageFromAssetsFile(String)` — decodes an asset into an ARGB_8888 bitmap.
- `PlaceholderFragment.onActivityResult()` — party-edit refresh (rc 2) and `TargetLedgerId` save (rc 11002).
- `makeCall(String)`, `sendsms(row, String)`.
- `ASYNCGetInvTransDetails.doInBackground()` / `onPostExecute()`.

**Rules & edge cases.**
- **Not done on this screen.** No GPS capture: the screen only shows stored party and previous locations. No visit-status or no-payment reason (only the list's "Visited" checkbox). No auto-closure: a zero balance does nothing here (weekly-line auto closure is handled by the server procedure `PrepareLast5Collectionsforweeklyline_withautoloanclosure` during download, and manual closure by the loan-closure screens).
- **No validation before saving.** Bank greater than the total only shows a field error, and negative Cash is possible. Cash is display-only. The Due watcher does not refresh the in-memory `CashCollection` (SQLite is correct).
- **Possible crashes.**
  - `InsAmount` = 0 with `loanstatusmethod_10` ≠ 1 divides by zero (`PrevCollected % InsAmount`).
  - The page count comes from SQLite while page content comes from `items`, so a loan whose party is missing from `Parties` gives more pages than items, and swiping to the end would throw `IndexOutOfBounds`.
  - The `Parties` cursor is closed inside its own read loop; this only works when exactly one row matches.
- **Access and visibility quirks.** The Status row is controlled by special access 5 (balance), not 4. The `MCLDueCollId` check has no effect. In online mode (opened from `editing_collectionswithfulloptions_act`) the edits are also written to local `MobileAppCollData`.

## Direct collection entry (online, single loan) — `directcollectionentry`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, edge-to-edge); manifest `exported=false`; title "Direct Collection"; implements `selectedloancollectionadapter.ContactsAdapterListener` and `invtransdetails_adapter.ContactsAdapterListener` (both callbacks are no-ops). |
| **Layout(s)** | `activity_directcollectionentry.xml`; history rows `selectedloancollection_row.xml`; product rows `invtransdetails_row.xml` (through `invtransdetails_adapter`, another group). |
| **Opened from** | `loanslist_act` when started with `calledfrom`=2: Admin → "Loans" (drawer `nav_loans` → `selectbranchandfinance` rc 114; for a weekly finance, rc 2001 after picking the weekday). A row tap there shows "Choose an Option" with "See Loan Details" and "New Collection Entry". The second passes `CallType`=301, `passednewloan` (mcloanobj) and `passedparty` (partydetails_data); rc 301. With `calledfrom`=1022 (`nav_loansfortoday`) a row opens `newloancreation` directly, so this screen cannot be reached from there. |
| **Opens** | `DatePickerDialog`; `spinnersearch_list` (target ledger, rc 11002; the result is ignored because there is no `onActivityResult`, and the row is hidden); `partydetails_act` (tap on the name with full access or special access 11; rc 2; result ignored). |
| **Data** | Remote reads (`selbranchconnString`): `InvTransDetails`, proc `GetDetailsforDirectCollection`, `tblMCLoanDueCol ⋈ tblMCLoans`. Remote writes (`branchconnString`): `dbo.InserttblTransactionsMaster`, `dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs`, `tblMCLoanDueCol`, `tblTransactionsDetails`. Local: `CashBankLedgers` (ledger name); `MobileAppCollData` updates that match no row (see Rules). |

**Purpose.** From the admin loans list, lets the user enter one collection for one loan directly into the server books for a chosen date. It bypasses the offline download/upload cycle and shows the loan's collection history beneath the form.

**UI.**
- Header: photo (from the party object), Party Name ("CW.Name" with setting 20), TAG, REMARKS.
- Loan facts: Loan Amount (shows `MCLoanAmountCollectable`), Interest/Document, Date (loan date), Closing Date (overdue days never shown), Due "InsAmount/DuesCount".
- Collected / Pending / Status rows (special access 5). Products block (inventory finances only).
- Collection **Date** (`newloancreation_loandateid`, "Date"/"தேதி", today by default; editable only with full access or special access 14).
- Amount fields: Loan Due Collection; Pen/Int and Savings, which never appear because their parent rows are `gone` in the XML and the code only toggles the child views (so both are always 0); hidden "Collected as/ In"; Cash (disabled, computed) and Bank Collection (only when `bankaccountledgerid` > 0).
- **SAVE** and **PRINT** (PRINT has no id or handler).
- "Collection Details" table (Date, Paid, Balance, Savings, Interest) with a red Total row; progress bar `dcparid`.

**How it works.**
1. `onCreate`: reads the loan and party objects and sets `collectiondate` = today. It hides penalty, savings and status, then installs the same watchers and double-tap fill as the entry screen (formula `Cash = Penalty + Savings + Due − Bank`; Bank and Cash are disabled while Due is empty or 0). It then starts `asyncgetloandetails`.
2. Date picker: a date at or before now, or at most 1 day ahead, is accepted. Anything later shows "Future Date cannot be used" (title "Info.... "). Changing the date does not reload the loan details.
3. `asyncgetloandetails` (selbranch):
   - If inventory is enabled: `Select SNo ,ItemCode ,Rate ,BilledQty ,Total, TransId from InvTransDetails where Transid=<ServerGenTransId> order by SNo`.
   - `GetDetailsforDirectCollection @ColDate='<date>' ,@MCLoanId=<id>` fills the loan object: FWUDMCLoanId, Remarks, PartyLedgerId, dates, UDLedgerId, PartyName, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PaidDues/PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, PrevCollected, interestamount, LoanIndex, Noofloansallowed, Doccharges and IsAttentionRequired. Today's amounts are set to 0 and `TargetLedgerId` = `cashbookledgerid`.
   - The history query `select GColId, Row_Number() over(Order by MCLDueCollId) as SNo, MCLDueColDate, isnull(Paid,0) as Collected, MCLoanAmount − (running Σ Paid) as Balance, PenaltyCollected, SavingsCollected from tblMCLoanDueCol x right join tblMCLoans y …` builds the history, plus a Total row (Σ Paid, Savings, Interest).
   - JADX shows a `return` inside the row loop; smali loops back (`goto/16`), so every row is read.
4. `onPostExecute`: applies the visibility rules and fills the fields. Status always ends as `LoanStatus` because a later `setText` overwrites the method-based value.
5. **SAVE** → `asyncsavecollection` (no checks at all) on `branchconnString`:
   - a. `{call dbo.InserttblTransactionsMaster(?×12)}`: TransDate = collection date, TransType = 22 (MicroLoan Collection), Narration "", FinId, TransAmount = Due + Penalty + Savings, FWTransId/FWVWUDTransId/GColId/TransLedgerId NULL, RouteId = FinId; out `TransId`, `FWVWTransId`.
   - b. `{call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?×13)}`: CollDate, CollAmount = Due, PenaltyCollected, SavingsCollected, CollStaffLedgerId = CollStaff2LedgerId = `MobileUserLedgerId`, TransId, NIPAmountCollected 0, FinId, BankCollection, CashCollection; out `MCLGCollId`, `FWGColId`.
   - c. `insert into tblMCLoanDueCol (GColId,MCLDueColDate,MCLoanId,TTId=22,FinId,Paid,PenaltyCollected,SavingsCollected,BankCollection,CashCollection)`.
   - d. `tblTransactionsDetails` double entry. Cash > 0: ledger 114 'Source' 'Cr' (SNo 1, against 2) + `cashbookledgerid` 'Target' 'Dr' (2/1). Bank > 0: 114 'Source' 'Cr' (3/4) + `bankaccountledgerid` 'Target' 'Dr' (4/3).
   - On success the dialog "Collection Entry Updated" (title "Info ") appears and OK closes the screen. An SQL error shows its message under the title "Unable to Complete.. ".

**Functions.**
- `onCreate()`.
- TextWatchers `directcollectionentry.1`–`4`.
- `5.onDoubleTap()` — fill installment.
- `lambda$onCreate$1` — Save.
- `lambda$onCreate$4` / `$3` — date picker and future-date check; `$2` — dialog OK.
- `lambda$onCreate$5` — ledger picker.
- `lambda$onCreate$6` — party details.
- `lambda$onCreate$7` — no-op.
- `OnCollectionSelected()` / `onContactSelected()` — no-ops.
- `asyncgetloandetails.doInBackground()` / `onPostExecute()` / `onPreExecute()`.
- `asyncsavecollection.doInBackground()` / `onPostExecute()` (OK → `finish()`).
- `whiteNotificationBar(View)` — light status bar (flag 8192 = `SYSTEM_UI_FLAG_LIGHT_STATUS_BAR`, white colour).

**Rules & edge cases.**
- **Stale amounts on save (verified in smali).** Only the Due watcher updates the fields that Save uses (`duecollection`, `bankcollection`, `penaltycollection`, `savingscollection`, `cashcollecton`); the other watchers work on local variables. Editing Bank, Pen/Int or Savings after Due is therefore not saved; for example the bank split is saved as 0 and cash as the whole amount.
- **False success on connection failure (verified in smali).** A failed connection only sets `ErrorText`, not `iserroroocured`, so "Collection Entry Updated" is still shown. The prepared message "Connection Error,Please try again..." for load failures is never displayed.
- **No transaction.** If Cash and Bank are both 0, an empty SQL batch is executed after the master rows were already inserted. There is no duplicate-date check and no zero-amount check.
- **Hard-coded ledger.** Ledger 114 is hard-coded as the collection credit ledger. `editing_collections_act` uses the same literal, and ledger statements always include it.
- **Display quirks.** `selloandata.MCLoanId` is never set, so the inherited `Update MobileAppCollData … Where MCLoanId=0` statements do nothing. The Tag is blanked after loading (`getTag()`=null). Pending shows 0 because `Balance` is never set. Collected shows today's 0 instead of `PrevCollected`.

## Individual loan collection details (history, print & share) — `selectedloancollectiondetails`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, edge-to-edge), title `@string/individualloancollectiondetails` "Details for a Individual Loan"/"தனிக்கடன் விவரம்"; static `ProgressDialog`. `drawPagewithdiscountline` is flagged as decompiled incorrectly. |
| **Layout(s)** | `activity_selectedloancollectiondetails.xml`; rows `selectedloancollection_row.xml` (through `selectedloancollectionadapter`); menu `menu_selectedloancollecteddetails`. |
| **Opened from** | `tabbedindividualcollectionentry` FAB (`iscollectioneditallowed`=true) and `loanslist_act.oncollectiondetailslinkclicked`. Also `todaycollectionlist`, `prevloanslist_act` (non-archived loans), `sevendayscollectionforselline`, `daywisependingreport_act`, `tbbspl_particulars`, `dailypatti_kadanpatrulist_act`, `dailypatti_nadapunippucollectionlist_act`, `dailypatti_nadapunippuphakkilist_act`, `dailypatti_varakadanillapulist_act`, `newpartiesnewloanslist_act`, `pattidetails_loans_act`, `periodicloareportlist_act` and `closed_renewedloanslist_act`; these pass `"0"` or nothing, so editing is off. Extras: `MCLoanId` and `PartyName` (both required), `Tag`, `Remarks`, `iscollectioneditallowed` (boolean). |
| **Opens** | `editing_collections_act` (row tap when editing is allowed; `selectedcollection` (groupcollectionslist_data), `selectedcollectiondate`, `receviedfinanceid`, `mcloanidcalledforcollection`, `selectedfinanceobj`, `calledfrom`=9908; rc 101); the Android print dialog; a PDF viewer (`ACTION_VIEW`); the share chooser. |
| **Data** | Remote (`globalvars.branchlevelReport_branchconnString`/user/password): the `tblMCLoanDueCol` RIGHT JOIN `tblMCLoans` running-balance history; proc `APP_GetLoanDetailsforALL @MCLoanId=`; `tblMCLoanGroupCollectionMaster` by `MCLGCollId`. Local: `FinanceDetails` (through `getfinanceobj`); creates `SelectedLoanCollectionDetails(SNo int,ColDate varchar,Amount int,Balance int)` but never writes to it. PDFs go to `Documents/venusmfc/`. |

**Purpose.** Shows the complete collection history of one loan (server data) with a loan summary. It can print it, show it as a PDF or share it. When opened from the entry screen it also lets the user pick a past collection to correct it.

**UI.**
- Party name, TAG and REMARKS (from the extras).
- Loan summary:
  - Loan Amount/கடன் தொகை (`LAmount`; red when IsNIP, orange when to-be-warned, else black).
  - Interest Amount/வட்டி and Document Charges/ஆவண கட்டணம்.
  - Loan Date and Closing Date (server strings).
  - Collected/வசூலிக்கப்பட்டது (`PrevCollected`); a hidden "Collected" field holds `InsAmount`.
  - Balance/பாக்கி (`MCLoanAmountCollectable − PrevCollected`) and Status (`LoanStatus`).
- "Collection Details" header with Date, Paid, Balance, Savings (setting 13) and Interest (setting 12), then one row per collection (dd-MM-yy) and a red "Total" row.

**Menu / actions.** "Close Loan" is `visible=false` and has no id or handler, so it is not available. "Print and Share Options" opens a list:
- "Direct Print": `PrintManager.print("<app_name> Document")`, A4 portrait, `MyPrintDocumentAdapter`.
- "Show as PDF": writes `cnpr_<ms>.pdf` and opens it through FileProvider `<package>.provider`.
- "Share": writes `taxinv_<ms>.pdf` and sends `ACTION_SEND application/pdf` with subject/text "Collection Details" through the chooser "Share PDF using...".

Row tap → edit (see step 3).

**How it works.**
1. `onCreate`: reads the extras. `iscollectioneditallowed` is read with `getBoolean`, so the String `"0"` passed by most callers reads as false. It shows or hides the Savings/Interest headers and runs `ASYNCGetCollectionDetails`, which shows the progress title "Working".
2. `ASYNCGetCollectionDetails`:
   - Runs the history query: `select GColId, Row_Number() over(Order by MCLDueCollId) as 'SNo', MCLDueColDate, isnull(Paid,0) as 'Collected', MCLoanAmount-isnull((select sum(Paid) … MCLDueCollId<=x.MCLDueCollId),0) as 'Balance', PenaltyCollected, SavingsCollected from tblMCLoanDueCol x right join tblMCLoans y on x.MCLoanId=y.McLoanId where y.MCLoanId=<id> order by sno`.
   - Appends a Total row (Σ Paid, Savings, Interest).
   - Runs `APP_GetLoanDetailsforALL @MCLoanId=<id>` for the header (MCLoanId, FWUDMCLoanId, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, PaidDues, PassedDues, interestamount, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, PartyName, DocCharges).
   - Exceptions are only logged, so a failure leaves an empty screen.
3. Row tap → `OnCollectionSelected`. If the row's `GColId` ≠ 0 and editing is allowed, `ASYNCCollections` loads `Select MCLGCollId, FWGColId, CollDate, CollAmount, CollStaffLedgerId, TransId, FinId, NIPAmountCollected, CollStaff2LedgerId, PenaltyCollected, SavingsCollected from tblMCLoanGroupCollectionMaster where MCLGCollId=<GColId>` into `groupcollectionslist_data`. It then opens `editing_collections_act`, where the whole group collection can be edited. An error shows the "Error Info" dialog, and OK closes this screen. No refresh happens on return.
4. PDF/print (`drawPagewithdiscountline`): 800×1140 pages with a border and the title "Loan Collection Details". The header shows Party Name, Loan Amount (`LAmount`), Loan Date, Balance, Closing Date and Paid (`PrevCollected`); here Balance = `LAmount − PrevCollected`. Below that is a table with S.No, Coll Date (yyyy-MM-dd) and Paid, in two side-by-side columns of 40 lines per page, plus the footer "Printed @ dd-MM-yyyy, HH:mm:ss". The page counter stops at 2 pages, so very long histories are cut off. The exact drawing flow is unclear from the decompiled code.

**Functions.**
- `onCreate()`, `whiteNotificationBar()`.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` and `lambda$onOptionsItemSelected$1` (option dispatch).
- `printDocument()` — counts pages and starts the print job.
- `MyPrintDocumentAdapter.onStart()` / `onLayout()` ("Page count is zero." on failure) / `onWrite()` / `pageInRange()`.
- `sharePdf(String)`, `createPdfA4(String)`.
- `drawCenterwithgivenwidth()` — centres text in a column.
- `drawPagewithdiscountline(Page, int)` — draws one page.
- `OnCollectionSelected()`.
- `ASYNCGetCollectionDetails.*`.
- `ASYNCCollections.*` — progress "downloading, please wait ..." / "Loading Loans Collection".

**Rules & edge cases.**
- **Editing.** Editing is possible only when opened from the entry screen, which also needs full access or special access 15. There is no loan closure here.
- **"Share" fails silently.** It uses the hard-coded FileProvider authority `net.venussolutions.venusmicrofindailyweeklycollection.provider`, but the manifest declares `…clone.provider`. `getUriForFile` throws, the exception is caught, and nothing happens in this build. "Show as PDF" works.
- **Inconsistent balances.** `LoanStatus`, `PrevCollected` and the Balance are read as doubles and shown with ".0". The running balance uses `tblMCLoans.MCLoanAmount`, the on-screen header uses `MCLoanAmountCollectable`, and the PDF uses `LAmount`; these can differ.
- **Loans with no collections.** The RIGHT JOIN returns one row with no date, which the adapter renders as a second "Total" row.

## Edit one party's collection (cash/bank split) — `collectionedit_individualcollection`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, edge-to-edge); manifest `exported=false`; title = party name; Back is handled by an `OnBackPressedCallback`. |
| **Layout(s)** | `activity_collectionedit_individualcollection.xml`. |
| **Opened from** | `editing_collections_act.onrowselected()` (editing a saved group collection; rc 2) and `editing_collectionswithfulloptions_act.onrowselected()` when `calledfrom` ≠ 9907 (rc 2). Extra `selectedcollection` (`tabbedcollectionlistdata`). |
| **Opens** | Nothing. An `onActivityResult` for rc 11002 (target ledger) exists, but nothing launches the picker. |
| **Data** | SQLite `CashBankLedgers` (target ledger name only). No database writes; the result is returned to the caller. |

**Purpose.** Inside collection editing, the user re-enters how one party's collected amount was received, in cash and by bank, and the edited row goes back to the editing list. The screen has no delete function and does not touch offline data.

**UI.** Thumbnail (never loaded), Party Name, Loan Amount (`LAmount`), Balance, Loan Date, Due (`InsAmount`), and Loan Due Collection (`ilc_editLoanDuecollectionamountid` = Collected). The Pen/Int and Savings rows are `gone` and have no ids. Cash Collection and Bank Collection are editable. "Collected as/ In" is hidden.

**How it works.**
1. `onCreate` fills the fields from the passed object, including the target-ledger name from `CashBankLedgers`.
2. The Cash and Bank watchers set `CashCollection`/`BankCollection` (0 when empty), recompute `Collected = Cash + Bank` and show it. Typing directly in the Due box is ignored.
3. Back (`handleOnBackPressed`) always returns `setResult(1, selectedcollection)` and finishes. The caller replaces the row with the same `MCLoanId`, refreshes it, recomputes the totals and saves later in its own flow.

**Functions.**
- `onCreate()`.
- TextWatchers `.2` (cash) / `.3` (bank).
- `handleOnBackPressed()` — returns the edited object.
- `onActivityResult()` — rc 11002 ledger result (unused).
- `lambda$onCreate$0` — insets.

**Rules & edge cases.** There is no Cancel: Back always accepts the changes. There is no validation. Penalty and savings are ignored (Collected = Cash + Bank), unlike `Cash = Due + Pen + Sav − Bank` elsewhere. Nothing is saved until the caller saves.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `tabbedcollectionlistadapter` | RecyclerView.Adapter + Filterable; interfaces `CollectedAmountChangeLiatener` (`oncollectionamountchanged`, `onmarkchanged`) and `ContactsAdapterListener` (`onBackPressed`, `onContactSelected`) | `tabbedcollectionlistmviewpager`, `tabbedcollectionlist` | Row `tabbedcollectionlistrow`. Binds the name (colour: marked/attention blue, NIP red, warned orange, legacy new loan blue with the amount box disabled), Loan, Due, the amount box (`Collected`), Coll/bal/Stas (special access 3/5/4; status per `loanstatusmethod_10`), loan method (setting 41), party no. (setting 20), Visited checkbox (`enablevisitcheckup_6a`, id 23) and photo (setting 21, Glide circle crop). The last-5 strip (`showlast5dayscollectioninareawisecollectionlist_7`, id 39) comes from `select CollDate,Paid from PrevCollections where MCLoanid=<id> order by RowNo desc`. Typing in the box runs `update MobileAppCollData set Collected = <n>` + `set CashCollection = ifnull(Collected,0)+ifnull(PenaltyCollected,0)+ifnull(SavingsCollected,0)-ifnull(BankCollection,0)` and then the callback; the flag `globalvars.iscollectionamountisbeingdisplayed` suppresses saves while binding. Double-tap on Due does the same with `InsAmount`. The Visited box writes `IsVisited`. Long-press on the name toggles the mark through the callback. Row click → `onContactSelected`. Filter: name contains (case-insensitive) or `CWLedgerId` equals. A row disabled for a new loan stays disabled when its view holder is reused. |
| `tabbedcollectionlistadaptermviewpager` | RecyclerView.Adapter + Filterable (own copies of the two interfaces) | **None.** No Java or smali reference anywhere, so it is dead code. | Near copy of the adapter above. Differences: loan method and balance columns are always visible (both branches set VISIBLE); no new-loan or attention colour; fallback image `globalvars.noimagepath`; double-tap sets `CashCollection = InsAmount`; typing writes only `Collected`. Long-press writes `isMarked='true'/'false'` as quoted text (a later `1 == getInt(IsMarked)` would read false) and passes the unfiltered list's item at the filtered position, so it picks the wrong row while a search is active. |
| `tabbedcollectionlistdata` | Serializable model (getters and setters only) | Every class in this module, plus `navigationdrawactivity`, `editing_collections*`, `MyService_updatecollection` and others | One downloaded loan/collection row with 51 fields. Amounts: Balance, BankCollection, CashCollection, Collected, PrevCollected, PenaltyCollected, SavingsCollected, InsAmount, LAmount, MCLoanAmountCollectable, DocCharges, interestamount. Dues and method: DuesCount, PaidDues, PassedDues, NoOfIns, CBCount, CollectionBreakString, LoanMethodDesc, LoanStatus. Ids: MCLoanId, FWUDMCLoanId, MCLDueCollId, PartyLedgerId, CWLedgerId, UDLedgerId, SubAreaNo, SubAreaName, TargetLedgerId, finid, loanindex, NoofLoansallowed, showid. Dates: MCLoanDate, ClosingDate. Party: PartyName, Tag, Remarks, ImageFileName, IdProoffileName, IdProoffileIIName, lat, lng, collectionlat, collectionlng. Flags: IsMarked, IsNIP, IsVisited, istobeWarned, isAttentionrequired, islocallyfixed, iscalledforcorrection. |
| `selectedloancollectionadapter` | RecyclerView.Adapter + Filterable; `ContactsAdapterListener.OnCollectionSelected` | `selectedloancollectiondetails`, `directcollectionentry` | Row `selectedloancollection_row`: Date (dd-MM-yy), Paid (`Amount`), Balance, Savings (only if setting 13), Interest (= `PenaltyCollected`, only if setting 12). A row with no date is drawn as a red "Total" row with the balance blank (red = 0xFFFF0000; JADX shows `SupportMenu.CATEGORY_MASK`). Row click → callback. The filter returns an empty list for any non-empty text and is not used. |
| `invtransdetailsdisplay_adapter` | RecyclerView.Adapter + Filterable (empty `ContactsAdapterListener`) | `tabbedindividualcollectionentry` (inventory products) | Row `invtransdetails_row`: S.No, product name looked up in local `ItemMaster` by `ItemCode` (falls back to the server `ItemName` only on an exception), Qty (`BilledQty`), Price (`Rate`), Amount (`TotalAmt`). It opens SQLite on every bind. The filter returns every row (no effect). |
| `selectedloancollection_data` | Serializable model | `selectedloancollectionadapter`, `selectedloancollectiondetails`, `directcollectionentry` | One history row: `SNo`, `ColDate` (java.util.Date; null for the Total row), `Amount` (paid), `Balance`, `GColId` (group collection id, used for editing), `interest` (penalty/interest), `savings`. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_tabbedcollectionlistforviewpager1.xml` | `tabbedcollectionlistmviewpager` | Toolbar `loanpartytoolbar` + classic ViewPager `container`. |
| `activity_tabbedcollectionlist.xml` | `tabbedcollectionlist` (legacy) | CoordinatorLayout `main`, Toolbar `loanpartytoolbar`, ViewPager2 `container`. |
| `fragment_tabbedcollectionlist.xml` | `tabbedcollectionlistmviewpager`, `tabbedcollectionlist` | `section_label` (area name; default "Collection Area Name"/"வசூல் ஏரியா பெயர்"); `include @layout/customersearchlist` (RecyclerView `recycler_view`); `tcl_tvtotalcollectionamountid` "Total Collection"; FAB `tcl_fab`. |
| `tabbedcollectionlistrow.xml` | `tabbedcollectionlistadapter`, `tabbedcollectionlistadaptermviewpager`, `editing_collectionsadapter` (other group) | Hidden `tcl_subareanameid`; `collectionlistrow_thumbnail`; `tcl_partynoid` + ":"; `tcl_partynameid`; `tcl_loanamountId` (Loan), `tcl_collectedamtId` (Coll), `tcl_balanceId` (bal), `tcl_statusId` (Stas); CheckBox `tcl_isvisitedid`; EditText `tcl_collectedamountId` (number); `tcl_loanmethodid`; `tcl_dueamountId` (DUE); last-5 strip `tcl_prevcollectiondetilslnrlayout` (first…fifth day label/value pairs); hidden Savings `tcl_savingsetid` and Penalty `tcl_penaltyetid` (numberSigned; not bound by this group's adapters). |
| `activity_tabbedindividualcollectionentry.xml` | `tabbedindividualcollectionentry` | Toolbar `toolbar` ("FinSoft II"/"FinSoft"), ViewPager `container`. |
| `fragment_tabbedindividualcollectionentry.xml` | `tabbedindividualcollectionentry` | Hidden `section_label`; header (`ilc_thumbnail`, `ilc_partynameid` "Party Name"/"பார்ட்டி பெயர்", `ilc_tagid`, `ilc_remarksid`); Loan Amount, Interest/Document (`ilc_interestamountid`, `ilc_documentcharge`), Date, Closing Date + `ilc_overduedays`, Due `ilc_dueamountid`; Collected (`ilc_prevcollectedamountid`)/Pending (`ilc_balanceid`)/Status (`ilc_statusid`, `ilc_additionaldue`) rows; "Collection Entry": `ilc_curcollectionamountid` (Loan Due Collection), `ilc_curcollectionpenaltyamountid` (Pen/Int), `ilc_curcollectionsavingsamountid` (Savings), hidden `ilc_curTargetLedgerId` ("Collected as/ In"), `ilc_cashcollectionid`, `ilc_bankcollectionid`; hidden products block `inventorylayoutid` (S.No/Product/Qty/Price/Amount, `recycler_view`, `itemdetailspbarid`); `ilc_mobileno1id`/`ilc_mobileno2id` with call/msg ImageButtons; `ilc_googlemaplocatorid`, `ilc_relateddocuments`, `ilc_printreceipt`; hidden buttons "No Not Found"×2 and "Locate"; "Prev 5 Collections" `tblpcolid`; "Prev 5 Locations" `prev5locationstextviewid`/`tblprevloationsid`; FAB `ilc_fab`. |
| `invtransdetails_row.xml` | `invtransdetailsdisplay_adapter`, `invtransdetails_adapter` (other group) | `invtransdetailsrow_snoid` (S.No), `_proudctid` (Product), `_qtyid` (Qty), `_priceid` (Price), `_totalamountid` (Amount); hidden ImageView. |
| `activity_directcollectionentry.xml` | `directcollectionentry` | `dcol_thumbnail`, `dcol_partynameid` ("Party Name"/"பார்ட்டி பெயர்"), `dcol_tagid`, `dcol_remarksid`; Loan Amount, "Interest/Docoument" (sic), Date, Closing Date + `dcol_overduedays`, Due; Collected ("வசூலிக்கப்பட்டது")/Pending/Status (+`dcol_additionaldue`) rows; hidden products block `dcol_productlytid` (`precycler_view`); collection Date `newloancreation_loandateid` ("Date"/"தேதி"); `dcol_curcollectionamountid`; rows with `gone` parents holding `dcol_curcollectionpenaltyamountid`, `dcol_curcollectionsavingsamountid` and `dcol_curTargetLedgerId`; `dcol_cashcollectionid` (disabled), `dcol_bankcollectionid`; buttons SAVE (`dcol_savebuttonid`) and PRINT (no id); "Collection Details" header (Date, Paid, Balance, Savings, Interest) + `recyclerviewcollectiomndetailsid`; ProgressBar `dcparid`. |
| `activity_selectedloancollectiondetails.xml` | `selectedloancollectiondetails` | `cd_partynameid`, `cd_tagid`, `cd_remarksid`; labels Loan Amount/கடன் தொகை, Interest Amount/வட்டி, Document Charges/ஆவண கட்டணம், Loan Date, Closing Date, Collected/வசூலிக்கப்பட்டது (one hidden), Balance/பாக்கி, Status; values `ilc_loanamountid`, `ilc_interestamountid`, `ilc_documentcharge`, `ilc_loandateid`, `ilc_closingdateid`, `ilc_collectedid` (gone), `ilc_collectedamountid`, `ilc_balanceid`, `ilc_statusid`; "Collection Details" header row + `recyclerviewcollectiomndetailsid`. |
| `selectedloancollection_row.xml` | `selectedloancollectionadapter` | `selectedloancoll_transdateid` (Date), `_collectedamountid` (Paid), `_Balanceamountid` (Balance), `_savingscollectedamountid` (Savings), `_interestcollectedamountid` (Interest). |
| `activity_collectionedit_individualcollection.xml` | `collectionedit_individualcollection` | `ilc_thumbnail`, `ilc_editpartynameid`; Loan Amount `ilc_editloanamountid`, Balance `ilc_editbalanceid`, Loan Date `ilc_editloandateid`; Due `ilc_editdueamountid`; Loan Due Collection `ilc_editLoanDuecollectionamountid`; hidden Pen/Int and Savings rows; Cash Collection `ilc_editcashcollId`; Bank Collection `ilc_editbankcollId`; hidden "Collected as/ In" `ilc_editcollTargetLedgerId`. |

---
[← brain.md](../../brain.md) · Module 8.4 of 20 · Previous: [8.3 Offline mode: download, upload and sync services](03-offline-mode-download-upload-sync-services.md) · Next: [8.5 Online collection entry & editing](05-online-collection-entry-editing.md)
