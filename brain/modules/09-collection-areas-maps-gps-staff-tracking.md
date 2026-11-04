# 8.9 Collection areas, maps, GPS staff tracking and attendance

[← brain.md](../../brain.md) · Module 8.9 of 20 · Previous: [8.8 Loan lists, loan & savings closures, party transfer and rearrangement](08-loan-lists-loan-savings-closures-party.md) · Next: [8.10 Accounts reports: trial balance, balance sheet, P&L, month-wise P&L](10-accounts-reports-trial-balance-balance-sheet.md)

Collection areas ("sub areas", table `SubAreaDetails`; relabelled "SH Groups"/"குழுக்கள்" when soft setting `GroupnameinsteadofAreaname_401` is on) are the route groups into which the parties of a line (finance) are organised. On weekly lines each area is tied to a weekday (`WeekdayId`, 1 = Sunday … 7 = Saturday), and `ShowId` sets the route order. This module lists and searches areas from the local SQLite copy, falling back to the server when that copy is empty. It creates, edits and deletes areas directly on SQL Server (`dbo.InsertSubAreaDetails`, `UPDATE`/`DELETE SubAreaDetails`), with an optional GPS fix, re-orders them by drag-and-drop (special access 20), and plots areas or parties on Google Maps. It also contains a foreground GPS-tracking service and a route-replay map. Both are fully coded, but nothing in this build launches them. Finally, it holds the executive "Attendance" grid (executive × date → line worked), whose data comes from `selectbranchandfinance`.

## Collection Areas list — `collectionareassearchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest label "Collection Area", theme `AppTheme.NoActionBar` (own Toolbar), `windowSoftInputMode=stateAlwaysHidden`; JADX partial-decompile warning only on the filter spinner switch |
| **Layout(s)** | `activity_collectionareasearch.xml`; rows `collectionarea_row.xml` (via `collectionarea_adapter`); `weekday_selection_sheet.xml` + `android.R.layout.simple_list_item_1` (bottom sheet, never shown); menu `menucollectionareasearchlist.xml` |
| **Opened from** | Drawer `nav_collectionareas` "Collection Areas"/"வசூல் ஏரியா" (no extras → `calledfromloanmethod` = 2); `loanslist_act` new-loan button (extras `CallType` = 7107 `getLoan_newloanrequest`, `receviedfinanceid`, `calledfromloanmethod` = 1; plain `startActivity`) |
| **Opens** | `collectionareadetails_act` (FAB: `requestcode`=1, forResult 1; row EDIT: `passedarea` + `requestcode`=2, forResult 2); `partylistwithprevloansfornewloanselection` (row tap: `SubAreaNo`, `AreaName`, `calledfrom` 9901 or 9017, forResult 24); `collectionarearearrangeact` (forResult 23); `maps_collectionareadisplay` |
| **Data** | SQLite `SubAreaDetails` (read `where FinId = globalvars.finid order by ShowId`; wiped and refilled from the server when empty) · SQL Server `subareadetails` (SELECT via `selbranchconnString`) · globals `finid`, `isselectedlineisweeklyline`, `currentweekdayid`, `collectiondate`, `isFullAccessAllowed`, `salesdetailsforandroid_datalist`, `specialaccessrules` · soft settings `GroupnameinsteadofAreaname_401`, `vss_executivehandlingmethod` · no prefs |

**Purpose.** Shows the collection areas of the currently selected line. From here the user can open an area's parties (to add parties or loans), add or edit areas, re-order them, and view them on a map.

**UI.** Toolbar title "Collection Areas"/"வசூல் ஏரியா" ("SH Groups"/"குழுக்கள்" when `GroupnameinsteadofAreaname_401`). RecyclerView `recycler_view` lists area names; each row has an "EDIT"/"திருத்து" button, shown only when editing is allowed. FAB `addnewemployeeid` (content description "add new area", + icon). Filter bar `filterlayoutid`: Spinner `filterSpinner` with array `filter_options` "All Areas / Weekday / Monthday" and value TextView `selectedfiltervalue`. The layout declares this bar `gone` and no code ever makes it visible. ProgressBar `capbarid` shows during the server fetch.

**Menu / actions.**
- Search (SearchView, always shown): filters by area name, case-insensitive substring. Back first collapses an open search view.
- "Rearrange Areas": visible only if `globalvars.specialaccessrules.getAllowtoRearrangeCollectionAreaandParties_20()` (special access 20). Opens `collectionarearearrangeact` (rc 23).
- "Show in Map": opens `maps_collectionareadisplay`, only when the list is non-empty.
- FAB opens a new area. A row tap opens that area's parties. EDIT opens the area in edit mode.

**How it works.**
1. `onCreate`: opens SQLite. Access limits are all true when `globalvars.isFullAccessAllowed`; otherwise they come from the `salesdetailsforandroid_data` row whose `mnuname` is `nav_collectionareas`. If extras are present it reads `calledfromloanmethod`. Value 1 means "picker for a new loan": the filter bar is hidden and AddState/UpDateState are forced false. `UpDateState` is passed to the adapter as "edit allowed".
2. FAB: `fab.hide()`, then `fab.show()` is called unconditionally, so the add button is always visible whatever `AddState` says.
3. `fetchcollectionareadetails()`: ensures the table exists (`defaulttables.createsubareadetails`: `SubAreaNo Double, SubAreaName, FinId, WeekdayId, IsVisible Boolean, ShowId Double, Latitude REAL, Longitude REAL`). It then runs `select SubAreaNo, SubAreaName, WeekdayId, FinId, IsVisible, ShowId, Latitude, Longitude from SubAreaDetails where FinId=<globalvars.finid> order by ShowId` (IsVisible is true for "true" or "1"). If no rows come back it runs `AYNCGetCollectionArea`.
4. `AYNCGetCollectionArea`: if `globalvars.collectiondate`/`currentweekdayid` are null it sets them to today. It connects with `selbranchconnString` and reads `subareadetails` ordered by ShowId, filtered as follows:
   - called from the loan screen → `FinId = receviedfinanceid`;
   - otherwise → `FinId = globalvars.finid`;
   - weekly lines additionally `and WeekDayId = <currentweekdayid>`.
   It then **deletes every local row** (`SubAreaNo>0`, all lines), inserts the fetched rows and fills the list.
5. Row tap opens `partylistwithprevloansfornewloanselection`. `calledfrom` is 9901 (`partyscreen_newpartyrequestfrom_partyselectionscreen_offlinepartycreation`) when the executive handling method is offline (`vss_executivehandlingmethod`==1) and the screen was launched normally. Otherwise it is 9017 (`requestcodes.newloanrequestcode_online`).
6. `onActivityResult`:
   - rc 1 / result 1 → appends the returned `passedcollectionarea`.
   - rc 2 / result 2 → replaces the row with the same SubAreaNo.
   - rc 2 / result 3 → if `deletedrowscount` is 0, shows the alert "Warning..." / "தொடர்பான பதிவுகள் இருப்பதால் நீக்க இயலாது.." ("cannot delete, related records exist"); otherwise removes the row.
   - rc 23 / result 1 → reloads from SQLite.
7. Filter dialogs, reachable only through the hidden bar:
   - `showWeekdaySelectionDialog()` lists `fetchweekdayslist` (1 Sunday … 7 Saturday) and filters with `"weekday|<id>"`.
   - `showmonthdaySelectionDialog()` lists 1–28 but also calls `filterByWeekday`. Both dialogs are titled "Select a Weekday".

**Functions.**
- `onCreate()`: access limits, extras, FAB, filter and RecyclerView wiring, initial load.
- `fetchcollectionareadetails()`: local read; starts the server fetch when the result is empty.
- `AYNCGetCollectionArea.doInBackground()/onPreExecute()/onPostExecute()`: server read and local cache rewrite; refreshes the list and hides the filter bar in loan mode.
- `onCreateOptionsMenu()`: inflates the menu and gates "Rearrange Areas" on special access 20. `onOptionsItemSelected()`: rearrange, map, search, home.
- `onContactSelected()`: opens the area's party list. `oncollectionareaedit()`: opens the edit screen.
- `onActivityResult()`: applies add, update, delete and rearrange results. `showalert()`: the delete-refused warning.
- `showWeekdaySelectionDialog()`, `showmonthdaySelectionDialog()`, `filterByWeekday()`. Never called: `filterBymonthday()`, and `showWeekdayBottomSheet()` (whose selection is discarded anyway).
- `whiteNotificationBar()`: white status bar with dark icons. The back-press callback collapses search first.

**Rules & edge cases.**
- The FAB ignores `AddState` and is always shown, even in loan-picker mode. EDIT respects `UpDateState`.
- In loan-picker mode the local read still filters on `globalvars.finid`, not `receviedfinanceid`; the server is queried with `receviedfinanceid` only when the local copy is empty.
- A server refresh deletes the cached areas of all lines, not just the current one.
- Month-day filtering cannot work, for three reasons: `collectionareas_data.monthdayid` is never populated, the month-day dialog calls the weekday filter, and the adapter's `split("|")` test for "monthday" never matches.

## Add / Edit Collection Area — `collectionareadetails_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default `AppTheme` (action bar) |
| **Layout(s)** | `activity_newcollectionareacreation.xml` |
| **Opened from** | `collectionareassearchlist`: FAB (`requestcode`=1, rc 1) and row EDIT (`requestcode`=2 + `passedarea` (`collectionareas_data`), rc 2) |
| **Opens** | `spinnersearch_list` (`title` "Collection Day", `searchlist` = weekdays, rc 22 `CollectiondayRequestcode`); system location-settings resolution dialog (rc 1002) |
| **Data** | SQL Server via `selbranchconnString`: `{call dbo.InsertSubAreaDetails(?,?,?,?,?,?,?,?)}`, `update SubAreaDetails set SubAreaName=?,IsVisible=?,ShowId=?,FinId=?,WeekdayId=?,Latitude=?,Longitude=? where SubAreaNo=?`, `delete from SubAreaDetails where SubAreaNo=?` · SQLite `SubAreaDetails` insert/update/delete · FusedLocationProviderClient · globals `finid`, `currentweekdayid`, `isselectedlineisweeklyline` |

**Purpose.** Create or rename a collection area, mark it active or inactive, set its weekday (weekly lines), capture its GPS coordinates, or delete it.

**UI.**
- Title: add mode "New Collection Area"/"புதிய வசூல் பகுதி" (or "New SH Group"/"புதிய குழு"); edit mode "Edit Area" (or "Edit SH Group"/"குழு திருத்தம்").
- Label "Collection Area Name"/"வசூல் ஏரியா பெயர்" (or "SH Group Name"/"குழு பெயர்") with EditText `etareanameod`.
- "Collection Day"/"வசூல் நாள்": read-only field, shown only for weekly lines; tapping it opens the weekday picker.
- CheckBox "ACTIVE" (IsVisible; ticked by default for new areas).
- Location row: location-icon Button `btngetcoOrdid` and TextViews `latid`/`lngid`.
- Buttons "Update"/"சேமி" (save) and "DELETE"/"நீக்கு" (edit mode only). ProgressBar.

**How it works.**
1. Add mode builds a new `collectionareas_data`: WeekdayId = `currentweekdayid`, FinId = `finid`, ShowId = 1, ACTIVE ticked. The weekday text comes from `defaulttables.gettamilweekdaynameforid`. Edit mode fills name, latitude/longitude, active flag and weekday from `passedarea`.
2. GPS button:
   - Needs ACCESS_FINE_LOCATION. If missing, it requests it (code 1001); there is no `onRequestPermissionsResult`, so the user has to tap again.
   - `checkLocationSettingsAndFetchLocation()` checks settings for a LocationRequest with priority 100 (high accuracy), interval 10 000 ms and fastest interval 5 000 ms. A resolvable failure shows the system dialog (rc 1002; the result is not handled). Any other failure shows the toast "Unable to resolve location settings.".
   - On success `fetchLocationnew()` calls `getLastLocation()` and fills the latitude/longitude fields and the model. A null location shows "Unable to get location. Try again later."; a failure shows "Failed to retrieve location: <msg>". Only the last known fix is used; no fresh fix is requested.
3. Save trims the name and takes IsVisible from the checkbox, then runs `AYNCUploadCollectionArea` (buttons disabled, progress shown):
   - `requestcode` 1 → `dbo.InsertSubAreaDetails` with named parameters `SubAreaName`, `IsVisible`, `ShowId`, `FinId` (= `globalvars.finid`), `Latitude`, `Longitude`, `WeekdayId` (the chosen weekday on weekly lines, else NULL) and OUT `SubAreaNo` (registered as DOUBLE, read as int). It then inserts the same values locally.
   - `requestcode` 2 → updates the local row first, then runs the server UPDATE above.
4. Delete runs with no confirmation prompt. It sets `requestcode` = 3 and runs the server DELETE; `deletedrowscount` is the number of rows affected. On `SQLException` (for example a foreign key from parties or loans) the count is 0 and the local row is kept; otherwise the local row is deleted too.
5. `onPostExecute` calls `setResult(requestcode, {passedcollectionarea, deletedrowscount})` and finishes (result 1 = added, 2 = updated, 3 = delete attempted).

**Functions.**
- `onCreate()`: binds views, sets up add/edit mode, shows the weekday row only for weekly lines, attaches listeners.
- GPS-button lambda: permission check → `checkLocationSettingsAndFetchLocation()`, which checks settings; success → `fetchLocationnew()`, failure → resolution dialog or toast.
- `fetchLocationnew()`: last known location → latitude/longitude fields and model; toasts on null or failure.
- Save lambda, Delete lambda (switches `requestcode` to 3), Collection-day lambda (`spinnersearch_list`, rc 22).
- `onActivityResult()`: rc 22 / result 1 → `WeekdayId = valuemember`; the field shows `displaymember`.
- `AYNCUploadCollectionArea.onPreExecute()/doInBackground()/onPostExecute()`: insert, update or delete, then return the result.

**Rules & edge cases.**
- No validation: an empty name is accepted. Exceptions during insert or update are only logged, and the caller still receives a success result (a new area can come back with SubAreaNo 0).
- The local copy stores `WeekdayId = globalvars.currentweekdayid`, not the weekday the user picked, on both insert and update. The server gets the picked weekday, so the two disagree until the next download.
- For daily and monthly lines the collection-day row is hidden and WeekdayId is saved as NULL. New areas always get ShowId 1.

## Rearrange Areas — `collectionarearearrangeact`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), manifest label "Rearrange Areas", default theme (action bar) |
| **Layout(s)** | `activity_collectionareaarangment.xml` (rows `collectionarearearangelist_row` via `collectionarearearrangeAdapter`, outside this group); menu `menu_partyrearrange.xml` |
| **Opened from** | `collectionareassearchlist` "Rearrange Areas" (rc 23; needs special access 20); `closed_renewedloanslist_act` options item id 0 (which launches it twice: `startActivityForResult(…, 23)` and `startActivity`) |
| **Opens** | nothing (returns result 1) |
| **Data** | SQLite `SubAreaDetails` (read all rows `order by showid`; `update SubAreaDetails set ShowId=… where SubAreaNo=…`) · SQL Server `SubAreaDetails` (one batch of `update … set ShowId` via `selbranchconnString`) |

**Purpose.** Drag-and-drop the route order of collection areas, save it locally and push it to the server.

**UI.** `CustomListView` `listView1` (background #EEEEEE) of every local area. Row colours cycle green/purple/red/blue by row number (`rowno % 4`, `% 3`, `% 2`). Each row has a grab handle; dragging swaps two list elements. Overflow menu item "Save and Sync".

**How it works.**
1. `onCreate` loads every `SubAreaDetails` row, with no line or weekday filter, as `dataforrearrangelist(name, "", colour, SubAreaNo)`.
2. "Save and Sync", for each position i:
   - reads SubAreaNo from the tag of the row's `textSecondaryDisplayMemberId` (`getViewByPosition` builds off-screen rows through the adapter);
   - new ShowId = `currentweekdayid × 1000 + i + 1` on weekly lines, else `i + 1`;
   - updates the local row;
   - then calls `SyncWithServer()`, **inside the loop**, so one upload task starts per row.
3. `AsyncUpdateShowindexvalues` shows ProgressDialog "uploading ...", reads every local `SubAreaNo, ShowId`, and runs a single batch of `update SubAreaDetails set ShowId=<n.0> where SubAreaNo=<n.0>;`. On error it stores "Connection Error,Please try again...", but that text is never shown. It always calls `setResult(1)` and finishes.

**Functions.**
- `onCreate()`: loads the list, wires the adapter and the `swapElements` lambda (swaps list items) and the `onGrab` lambda (passes the drag to `CustomListView.onGrab`).
- `getViewByPosition()`: returns the visible child, or inflates the row for an off-screen position.
- `onCreateOptionsMenu()`, `onOptionsItemSelected()`: Save and Sync.
- `SyncWithServer()` → `AsyncUpdateShowindexvalues.onPreExecute()/doInBackground()/onPostExecute()/onProgressUpdate()`.

**Rules & edge cases.**
- On weekly lines the ShowId encodes weekday × 1000 + position. It is applied to every listed area, including areas of other lines and weekdays, because the list is not filtered.
- Likely crash: the local `SubAreaNo` column is declared `Double`, so the tag string reads like "12.0", and `Integer.parseInt` on it would throw NumberFormatException. Save and Sync will very probably crash (code reading; not run).
- Redundant uploads: N upload tasks for N rows.

## Collection areas on map — `maps_collectionareadisplay`
| | |
|---|---|
| **Type** | Activity (FragmentActivity + `OnMapReadyCallback`), exported=false |
| **Layout(s)** | `activity_maps.xml` |
| **Opened from** | `collectionareassearchlist` "Show in Map" |
| **Opens** | nothing |
| **Data** | SQLite `select SubAreaNo, SubAreaName, WeekdayId, FinId, IsVisible, ShowId, Latitude, Longitude from SubAreaDetails where FinId=<globalvars.finid> order by ShowId` · Google Maps SDK (manifest meta-data `com.google.android.geo.API_KEY` = `[REDACTED]`) |

**Purpose.** Shows each area's saved coordinates on Google Maps, in route (ShowId) order.

**How it works.** `onMapReady` registers an `onMapLoaded` callback that logs "Map loaded successfully". It adds one marker per area at (Latitude, Longitude), titled with the area name. A blue (0xFF0000FF) 5-px polyline joins the areas in ShowId order. The camera moves to the first area at zoom 16. There is no other UI.

**Functions.** `onCreate()` (map fragment, `getMapAsync`); `onMapReady()` (query, markers, polyline, camera).

**Rules & edge cases.** Areas with no coordinates are plotted at (0, 0) and joined into the polyline. If there are no local rows, `locations.get(0)` throws IndexOutOfBounds; the caller only checks its own list, which can differ.

## Parties on map — `maps_partydisplay`
| | |
|---|---|
| **Type** | Activity (FragmentActivity + `OnMapReadyCallback`), exported=false |
| **Layout(s)** | `activity_maps_partydisplay.xml` |
| **Opened from** | `partylistwithprevloansfornewloanselection` menu `showpartiesinmapmenu` (`PLID` = SubAreaNo, `areaname` = "", rc 1); `tabbedcollectionlistmviewpager` same menu item (`PLID` = `curscreensubareano`, `areaname` = `curscreensubareaname`, rc 1) |
| **Opens** | nothing (no result set) |
| **Data** | SQLite `Parties a left join MobileAppCollData b on a.LedgerId=b.PartyLedgerId` · global `isoverallcollectionenabled` |

**Purpose.** Plots the parties (borrowers) of one area, or of all areas, on Google Maps.

**How it works.**
1. Sets the window title to "Rearrange-<areaname>". As a FragmentActivity it shows no AppCompat toolbar.
2. If `globalvars.isoverallcollectionenabled`, all parties are read; otherwise the query adds `where a.SubAreaNo=<PLID>`. The query is `select distinct(LedgerId), LedgerName, a.lng, a.lat … order by a.UDLedgerId`.
3. Adds a marker per party titled with the party name, then a blue 5-px polyline in party-number (UDLedgerId) order. The camera moves to the first party at zoom 16.

**Functions.** `onCreate()`; `onMapReady()`.

**Rules & edge cases.** Crashes (IndexOutOfBounds) when there are no parties. Null coordinates become (0, 0). The join to `MobileAppCollData` has no effect because of DISTINCT.

## GPS staff-tracking service — `staffgpstracking_service`
| | |
|---|---|
| **Type** | Service (foreground, manifest `foregroundServiceType="location"`, exported=false); `onStartCommand` returns 1 (START_STICKY); `onBind` returns null; JADX partial-decompile in the upload task's try/finally (logic still readable); the interval literals were checked in smali |
| **Layout(s)** | none (ongoing notification, icon `ic_action_location`) |
| **Opened from** | **Nothing in this build.** No `startService`/`startForegroundService`/`stopService` reference exists anywhere (Java or smali), and the manifest has no app boot receiver (only merged WorkManager library receivers). |
| **Opens** | nothing |
| **Data** | SQLite `StaffLocationQueue` (created by `downloaddata`: `QId INTEGER PRIMARY KEY AUTOINCREMENT, FinId int, StaffLedgerId int, TrackDate varchar, Lat varchar, Lng varchar, Accuracy int, Speed int, BatteryLevel int, IsMockLocation int, DeviceTime varchar`) · SQL Server `tblStaffLocationTrack` (batch INSERT via `selbranchconnString`) · static settings `softsettingsvalue.vss_gpstrackingintervalminutes`=1, `vss_gpsuploadintervalminutes`=1, `vss_gpstrackingstarthour`=6, `vss_gpstrackingendhour`=22 (never assigned anywhere, so these defaults always apply) · globals `finid`, `currentcollectionexecutivelederid1` |

**Purpose.** Record the collection executive's route during duty hours and upload it to the server, so that `staffgpstrackmap_act` can replay it.

**How it works.**
1. `onCreate`:
   - opens SQLite and creates notification channel `staffgpstracking` "Collection Tracking" (importance LOW, description "Records route during collection duty");
   - calls `startForeground(9101, …)` (type 8 = location on API 29+);
   - starts location updates and the upload timer.
2. Notification: title "Collection duty active"; text "Your route is being recorded", or "<n> point(s) pending upload" while the queue is non-empty. It is ongoing, low priority, and refreshed after every saved fix and every upload.
3. `startLocationUpdates()`:
   - FusedLocationProviderClient on the main looper, `LocationRequest.Builder(102 = balanced power accuracy, interval)`;
   - interval = `vss_gpstrackingintervalminutes × 60 000 ms`, falling back to 300 000 ms if ≤ 0 (literals from smali);
   - min update interval = interval / 2, min distance 50 m, `setWaitForAccurateLocation(false)`;
   - a fix is kept only if accuracy ≤ 100 m and `isWithinDutyHours()` (current hour in [start, end); start = end = 0 means always).
4. `savePingLocally()` inserts one row into `StaffLocationQueue`, then refreshes the queued count:
   - FinId = `globalvars.finid`, StaffLedgerId = `globalvars.currentcollectionexecutivelederid1`;
   - TrackDate `yyyy-MM-dd`; Lat/Lng stored as text;
   - Accuracy (int metres), Speed (int m/s);
   - BatteryLevel (% from `BatteryManager.getIntProperty(4)` = capacity; −1 on error);
   - IsMockLocation (`isMock()` on API 31+, else `isFromMockProvider()`);
   - DeviceTime `yyyy-MM-dd HH:mm:ss`.
5. `startUploadTimer()`: a Handler on the main looper repeats every `vss_gpsuploadintervalminutes × 60 000 ms` (fallback 600 000 ms). `triggerUpload()` skips the round if an upload is already running, if `isNetworkAvailable()` is false (no active network with capability 12, INTERNET) or if the queue is empty.
6. `AsyncUploadLocationQueue`:
   - reads `select * from StaffLocationQueue order by QId limit 200`;
   - sends one JDBC batch `insert into tblStaffLocationTrack(FinId,StaffLedgerId,TrackDate,Lat,Lng,Accuracy,Speed,BatteryLevel,IsMockLocation,DeviceTime) values(?,…)`, then `executeBatch()`;
   - runs `delete from StaffLocationQueue where QId=<id>` for each uploaded row;
   - `onPostExecute` clears the running flag and refreshes the count and notification.
7. `onDestroy` removes the location callback and the timer.

**Functions.** `onCreate()`, `onStartCommand()`, `onDestroy()`, `onBind()`, `createNotificationChannel()`, `buildNotification()`, `startForegroundCompat()`, `refreshNotification()`, `startLocationUpdates()` (+ `LocationCallback.onLocationResult()`), `isWithinDutyHours()`, `savePingLocally()`, `getBatteryLevel()`, `getQueuedCount()`, `startUploadTimer()` (+ `Runnable.run()`), `triggerUpload()`, `isNetworkAvailable()`, `AsyncUploadLocationQueue.onPreExecute()/doInBackground()/onPostExecute()`.

**Rules & edge cases.**
- The service is dormant: there are no start or stop triggers, and the interval and duty-hour settings are hard-coded statics with no soft-setting id.
- Uploads are not transactional. A failed batch deletes nothing; a partly successful batch would be re-sent, creating duplicates. Failures are silent.
- The queue table exists only after a data download (`downloaddata`); inserts before that fail silently.
- After a START_STICKY restart in a fresh process, the static globals (finid, executive id, connection string) are empty: fixes get NULL/0 ids and uploads fail.
- Collects precise location, battery level and a fake-GPS flag, which is privacy-sensitive. The manifest declares ACCESS_FINE/COARSE/BACKGROUND_LOCATION, FOREGROUND_SERVICE(_LOCATION) and POST_NOTIFICATIONS for it.

## Staff Route (GPS track replay) — `staffgpstrackmap_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity + `OnMapReadyCallback`), exported=false, default theme; no toolbar in the layout, so `setupToolbar()` does nothing and the title "Staff Route" is never applied |
| **Layout(s)** | `activity_staffgpstrackmap01.xml` |
| **Opened from** | **Nothing in this build** (no references). Accepts an optional extra `TrackDate` (`java.sql.Date`). |
| **Opens** | DatePickerDialog |
| **Data** | SQL Server proc `StaffLocationTrackDisplay @FinId=<globalvars.finid>, @StaffLedgerId=<staffledgerid>, @TrackDate='yyyy-MM-dd'` via `branchlevelReport_branchconnString` (columns Lat, Lng, Accuracy, Speed, BatteryLevel, IsMockLocation, DeviceTime) |

**Purpose.** Replay one staff member's recorded route for a chosen day on Google Maps, with stops, distance and fake-GPS detection.

**UI.**
- Header: staff name `sgt_staffnameid`, "Last seen …" `sgt_lastseenid`, and a tappable track date `sgt_trackdateid` (dd-MM-yyyy).
- Summary bar: "Distance" (`%.1f km`), "Points", "Collections", and "Fake GPS" (that column is visible only when the count > 0).
- Map fragment `sgt_mapid` (zoom controls and map toolbar on).
- Text "No location recorded for this day"; progress bar.

**How it works.**
1. `handleExtras()`: staffledgerid = 0 and staffname = "aa" (hard-coded placeholders); the date is the `TrackDate` extra or today.
2. `onMapReady()` starts `AsyncLoadTrack`. Tapping the date opens a DatePicker (max date today), and choosing a date reloads.
3. `AsyncLoadTrack` runs the procedure and fills `tracklist` (DeviceTime from `getTimestamp`). Collection points are never loaded (`LOAD_COLLECTION_POINTS = false`). An error shows the dialog "Error Info" with the message.
4. `drawEverything()` clears the map. With no data it shows "No location recorded…" and zeroes the summary. Otherwise:
   - a green (#2E7D32) 8-px geodesic polyline runs through all valid points ((0, 0) and unparsable values are skipped);
   - each mock point gets a magenta marker (hue 330) "MOCK LOCATION  HH:mm";
   - `drawStops()` adds the stop markers;
   - the header shows "Last seen HH:mm  ·  Battery N%" from the last point;
   - collection points would get azure markers (hue 210) "<party>" / "Collected <paid>", but the list is always empty;
   - camera: bounds span < 100 m → zoom 17 on the last point; otherwise fit bounds with padding 120.
5. `drawStops()` groups consecutive points within 40 m (`STOP_RADIUS_METERS`) of an anchor point into one stop. Each stop gets a marker: green for the first stop, red for the last, orange for the rest. Title "Stop N  HH:mm - HH:mm  (M min)" ("Stop N  HH:mm" for a single point); snippet "Battery X%  ·  ±Am".
6. `updateSummary()`:
   - Points = point count;
   - Collections = collection-point count (always 0);
   - Distance = Σ `Location.distanceBetween` over consecutive points ÷ 1000;
   - Fake GPS = number of mock points.

**Functions.** `onCreate()`, `initializeWidgets()`, `setupToolbar()`, `handleExtras()`, `addListeners()`, `setupMap()`, `onMapReady()`, `showDatePicker()`, `drawEverything()`, `drawStops()`, `minutesBetween()` (divides by 60 000 ms), `updateSummary()`, `clearSummary()`, `calculateDistanceKm()`, `toLatLng()` (parses strings; (0, 0) → null), `timeOnly()` (characters 11–16 of the timestamp), `onSupportNavigateUp()`, `AsyncLoadTrack.onPreExecute()/doInBackground()/onPostExecute()`; inner model `collectionpoint_data` (PartyName, Lat, Lng, Paid).

**Rules & edge cases.** The screen is unfinished: the staff id is fixed at 0 and the name at "aa", nothing launches it, and there are no paid/unpaid collection markers (only an unused "Collected" snippet). While the staff member is moving, every point becomes its own one-point "stop" marker, so markers can be dense.

## Executive Attendance board — `executiveattendance_displayboard`
| | |
|---|---|
| **Type** | Activity (plain `android.app.Activity`); manifest **`exported="true"`** |
| **Layout(s)** | none; the content is a programmatic `executiveattendance_dashboard` view |
| **Opened from** | `selectbranchandfinance` with `requestcode` 9024 (`requestcodes.attendance_requestcode`; drawer `nav_attendance` "Attendance") after its `ASYNCDataforAttendance` finishes; extra `actiontype` = 9007, forResult 9007 |
| **Opens** | nothing |
| **Data** | Static lists `partieslist` (`chitcol_parties_data`, employees), `biddinglist` (`chitcol_biddings_data`, dates) and `receiptslist` (`employeeattendancedataobj`), filled by the caller from SQL Server over the branch-report connection with: `select LedgerId,LedgerName,designationid from tblledgers where ScreenType=8 and designationid in (1,2) and desigstatusid=1 order by LedgerName`; `select IntCalDate from TwenDuration where IntCalDate >= From and <= To order by IntCalDate`; `exec CollectionDayandCollectionExecutiveforapp @FromDate, @ToDate` (attdate, finname, ledgerid) |

**Purpose.** For the From–To date range chosen in `selectbranchandfinance`, shows which line (finance) each active collection executive worked on each day. This is the app's "attendance".

**How it works.** `onCreate` copies the three static lists into the dashboard's statics and adds a new `executiveattendance_dashboard` inside a RelativeLayout. `onResume` rebuilds the dashboard and replaces the content again, so the first display builds it twice. `onActivityResult` rebuilds on rc 2500, which nothing uses.

**Functions.** `onCreate()`, `onResume()`, `onActivityResult()`.

**Rules & edge cases.** Attendance is not punched in. It is inferred server-side by `CollectionDayandCollectionExecutiveforapp`, and no times are shown. Because the activity is exported, launching it externally, or recreating it after process death, finds the static lists null and crashes with an NPE in the dashboard constructor.

## Attendance grid (scrollable table view) — `executiveattendance_dashboard`
| | |
|---|---|
| **Type** | Custom dashboard View (`RelativeLayout` subclass built entirely in code) |
| **Layout(s)** | none (four TableLayouts in synchronised `MyScrollView`/`MyHorizontalScrollView`) |
| **Opened from** | `executiveattendance_displayboard` |
| **Opens** | nothing (tapping a name does nothing) |
| **Data** | static `partieslist`, `biddinglist`, `receiptslist` |

**Purpose.** A spreadsheet-style grid with a frozen first row and first column: executives down the side, dates across the top.

**UI.**
- Top-left cell "Attendance": white text on `R.color.chitdarkbackground`; table background 0xFF00FF00.
- Header row (table B): one column per date (`Attdate.toString()` = yyyy-MM-dd), plus "Total".
- Left column (table C): one row per executive name.
- Body (table D): one cell per executive × date showing the finance (line) name of the first record whose ledger id and date match; blank otherwise. Rows are grey (0xFFCCCCCC), cells white.
- Scrolling is synchronised: header and body scroll together horizontally; names and body scroll together vertically.

**How it works.** The constructor runs, in order:
1. `initComponents()`, `setComponentsId()` (ids 1–4), `setScrollViewAndHorizontalScrollViewTag()`, `addComponentToMainLayout()`;
2. header rows A/B;
3. `resizeHeaderHeight()`, `getTableRowHeaderCellWidth()` (measures each header cell into `headerCellsWidth[100]`);
4. `generateTable_chitbiddingdetails()` (body rows via `taleRowForTableD`);
5. `genrateTable_partiesdetails()` (name rows via `tableRowForpartiestable`);
6. `resizeBodyTableRowHeight()`.

**Functions.**
- Construction: `initComponents()`, `setComponentsId()`, `setScrollViewAndHorizontalScrollViewTag()`, `addComponentToMainLayout()`.
- Header cells: `addTableRowToTableA()`/`componentATableRow()` ("Attendance" cell), `addTableRowToTableB()`/`componentBTableRow()` (date and "Total" headers).
- Body and names: `generateTable_chitbiddingdetails()`, `genrateTable_partiesdetails()`, `tableRowForpartiestable()`, `taleRowForTableD()`; `taleRowForBiddingTable()` is never called.
- Text cells: `bodyTextView()`, `headerTextView()`.
- Sizing: `resizeHeaderHeight()`, `getTableRowHeaderCellWidth()`, `resizeBodyTableRowHeight()`, `matchLayoutHeight()`, `isTheHeighestLayout()`, `viewHeight()`, `viewWidth()`.
- Scroll sync: `MyHorizontalScrollView.onScrollChanged()`, `MyScrollView.onScrollChanged()`.

**Rules & edge cases.**
- The "Total" column has a header but no cells and no sums. Only the first line per executive per day is shown.
- `headerCellsWidth` is `int[100]`, so a range of more than 98 dates throws ArrayIndexOutOfBoundsException.
- JADX renders an endless `while(true)` in `taleRowForTableD`; smali shows the loop exits when the records run out (decompile artefact).
- `matchLayoutHeight()` returns inside its loop, so only one cell per row is resized (cosmetic height mismatch).
- Class and field names (`chitcol_*`, "bidding") are reused from a chit-fund template.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `collectionarea_adapter` | RecyclerView adapter + Filterable | collectionareassearchlist | Row `collectionarea_row`: area name + "EDIT" button (visible only when `iseditallowed`). Callbacks `ContactsAdapterListener.onContactSelected(area)` (row tap) and `oncollectionareaedit(area, position)` (EDIT). Filter: `weekday\|<id>` keeps rows with that WeekdayId; the `monthday\|<id>` branch tests with `split("\|")` (regex alternation) and never matches; an empty query shows all rows; any other query is a case-insensitive substring match on SubAreaName. |
| `collectionareas_data` | Serializable model | areas list, details, maps | SubAreaNo, SubAreaName, FinId, WeekdayId, IsVisible (`getVisible()`/`isVisible()`), ShowId, latitude, longitude, monthdayid (never populated). Passed as extras `passedarea`/`passedcollectionarea`. |
| `staffgpstrack_data` | Serializable model | staffgpstrackmap_act | Lat, Lng (String); Accuracy, Speed, BatteryLevel (int); IsMockLocation (boolean, `getIsMockLocation()`); DeviceTime (String "yyyy-MM-dd HH:mm:ss…"). |
| `employeeattendancedataobj` | model | selectbranchandfinance → attendance board/dashboard | Ledgerid (executive), AttDate (`java.sql.Date`), FinId (never set), FinName (line worked that day). |
| `chitcol_parties_data` | model | selectbranchandfinance → attendance board/dashboard | ledgerid, ledgername. Here it is one executive or employee row (the class name comes from chit-fund code). |
| `Gpshelper` | helper (unused) | none | Wraps `LocationManager`. `getMyLocation()` returns the first non-null last-known location across enabled providers; it ignores the result of its permission check and falls back to the previous or (0, 0) values. Also `isGPSenabled()` (gps or network provider on), `getLatitude()`, `getLongitude()`. Not referenced anywhere. |
| `getAddressFromLatLng` | empty class | none | No members; placeholder for reverse geocoding. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_collectionareasearch.xml` | collectionareassearchlist | CoordinatorLayout `main`: Toolbar `toolbar`; hidden filter bar `filterlayoutid` (Spinner `filterSpinner`, entries `@array/filter_options` = All Areas/Weekday/Monthday; TextView `selectedfiltervalue`); RecyclerView `recycler_view`; FAB `addnewemployeeid` (content description "add new area", `ic_input_add`); ProgressBar `capbarid` (gone) |
| `collectionarea_row.xml` | collectionarea_adapter | TextView `collarea_collectionareanameid` (placeholder "Collection Area Name"/"வசூல் பகு") + Button `collarea_editbuttonid` "EDIT"/"திருத்து" |
| `weekday_selection_sheet.xml` | collectionareassearchlist (`showWeekdayBottomSheet`, never called) | Title "Select a Weekday" + ListView `weekdayListView` |
| `simple_list_item_1.xml` | collectionareassearchlist (ArrayAdapter row of the unused bottom sheet) | Android framework layout `android.R.layout.simple_list_item_1` (a single TextView); not part of the APK's own res |
| `activity_newcollectionareacreation.xml` | collectionareadetails_act | ScrollView card: label `labelcollectionareanameid` "Collection Area Name"/"வசூல் ஏரியா பெயர்" + EditText `etareanameod`; `collectiondaylayoutid` ("Collection Day"/"வசூல் நாள்" `tcollectiondayslabelid` + read-only `collectionareacreation_collectiondayid`); CheckBox `chkcollectionarea_statusid` "ACTIVE"; `locationlayoutid` (location-icon Button `btngetcoOrdid`, TextViews `latid`, `lngid`); Buttons `btnsavenewareaid` "Update"/"சேமி" and `btndeleteareaid` "DELETE"/"நீக்கு"; ProgressBar `newcollectionarea_pbarid` |
| `activity_collectionareaarangment.xml` | collectionarearearrangeact | Single drag-reorderable `CustomListView` `listView1` (#EEEEEE) |
| `activity_maps.xml` | maps_collectionareadisplay | RelativeLayout `main` with SupportMapFragment `map` |
| `activity_maps_partydisplay.xml` | maps_partydisplay | RelativeLayout `main` with SupportMapFragment `map` |
| `activity_staffgpstrackmap01.xml` | staffgpstrackmap_act | Header `sgt_headerid` (`sgt_staffnameid`, `sgt_lastseenid`, `sgt_trackdateid`); summary bar `sgt_summarybarid`: "Distance" `sgt_distanceid`, "Points" `sgt_pointcountid`, "Collections" `sgt_collectioncountid`, "Fake GPS" `sgt_mockcountid` inside `sgt_mockcolid` (invisible); SupportMapFragment `sgt_mapid`; TextView `sgt_nodataid` "No location recorded for this day" (gone); ProgressBar `sgt_pbarid` (gone) |

---
[← brain.md](../../brain.md) · Module 8.9 of 20 · Previous: [8.8 Loan lists, loan & savings closures, party transfer and rearrangement](08-loan-lists-loan-savings-closures-party.md) · Next: [8.10 Accounts reports: trial balance, balance sheet, P&L, month-wise P&L](10-accounts-reports-trial-balance-balance-sheet.md)
