# 8.18 Printing: collection list prints, receipts and Bluetooth printers

[← brain.md](../../brain.md) · Module 8.18 of 20 · Previous: [8.17 Inventory module: stock masters, items, customers/suppliers, purchase and stock in hand](17-inventory-module-stock-masters-items-customers.md) · Next: [8.19 Shared data-access layer, server-driven settings and utilities](19-shared-data-access-layer-server-driven.md)

This module turns the selected line's live collection list (read from SQL Server) into printable "collection sheets" that field executives carry on their rounds, in one of ten fixed layouts (collection-list printing methods 1–10). The app draws every sheet itself with `android.graphics.pdf.PdfDocument` on A4-portrait, A4-landscape or legal-width pages, and the user opens the PDF in a viewer or shares it. These sheets have no Bluetooth or thermal-printer path, no hard-coded Tamil text, and use the default system typeface rather than any bundled asset font. Bluetooth printing exists only through the bundled NGX Technologies SDK (`com.ngx.BluetoothPrinter`): its demo-style setup screen `BluetoothPrinterMain` is not declared in the manifest, and its only live use is a "print receipt" button in the tabbed collection entry that prints just the app-logo bitmap. About 30 other report screens share a common PdfDocument + Android Print Framework template; they are listed in the call-site table below.

## Collection List Print — `collectionlistprints`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `EdgeToEdge` insets); manifest label "Collection List Print", theme `AppTheme` (DarkActionBar), `exported=false` |
| **Layout(s)** | `activity_collectionlistprints.xml` (no row layouts; "Print Options" is a plain `AlertDialog` list) |
| **Opened from** | Drawer › Reports › `nav_collectionreporstprinting` "Collection List Print" / "வசூல் பட்டியல் அச்சு" (`navigationdrawactivity`, no extras) |
| **Opens** | `collectionreportprintingmethods_searchlist` (forResult `constantvalues.collectionreportprintingmethodrequestfrom_settingsscreen` = 7021; returns Serializable extra `method`, resultCode 1); external PDF viewer (`ACTION_VIEW`, `application/pdf`); share chooser "Share" (`ACTION_SEND`) |
| **Data** | Remote selected-branch DB (`globalvars.selbranchconnString` / `selbranchdbusername` / `selbranchdbpassword`, jTDS): one of 4 `APP_MCLoancollectionData*` procs, `GetLastNCollectionforCollReportPrint`, table `tblMCLoanDueCol` (all read-only) · no SQLite · no SharedPreferences (the dossier's "prefs read" names are ResultSet columns) · soft settings `collectionreportprintingmethod_21` (SoftSettings SSId 11, cached in pref `collectionreportprintingmethod`), `enablemoniteringonloanclosoure_5` (SSId 14) · globals read `financename`, `curfin.FinId`, `isselectedlineisweeklyline`; written `currentweekdayid` · file `Documents/venusmfc/cr_<ddMMyyyyHHmmss>.pdf` |

**Purpose.** The user (an executive or office staff member) picks a collection date and one of the ten sheet formats, and can choose to include collections already recorded. The app then builds a multi-page PDF. It lists every MC loan in the selected line, grouped by collection area. Depending on the format, it shows loan amount, balance, installment, a paid/elapsed-dues status, past collections, and blank boxes for hand-writing the day's collections. The PDF can be viewed (and printed from the viewer app) or shared.

**UI.**
- `tvRouteNameid` — the selected line name (`globalvars.financename`); placeholder "Line Name", 18sp bold magenta.
- Label "Collection Date" / "வசூல் தேதி". `dcl_collectiondateid` shows the report date as `dd-MM-yyyy` (25sp white on black); tap it to pick a date.
- `dcl_collectiondayid` (white on chocolate) shows the weekday. On open it shows the Java enum name (e.g. "WEDNESDAY"). After a date is picked it shows the localized name from `defaulttables.gettamilweekdaynameforid()` (string resources `sunday`…`saturday`, Tamil under `values-ta`).
- Label "Collection Report Printing Method". `reportprint_collectionreportprintingmethodid` is a read-only 3-line box showing the method name; tap it to open the method picker.
- `chkincludecollectionid` "Include Collection Details" — shown only for methods with `iscollectioncanbeincluded` (2–8); for methods 7/8 it is forced checked and disabled.
- `btnprint` "Print". `collectionreportprint_pbarid` is a centred spinner, shown while data loads.

**Menu / actions.** There is no options menu.
- Tap the date → `DatePickerDialog`, which always opens on today.
- Tap the method box → method picker.
- "Print" → loads the data, then shows a "Print Options" dialog with two items: "Show as PDF" → `createPdf()`; "Share" → `sharePdf()`.
- If loading fails, a Toast shows "Error Occured".

**How it works.**
1. **`onCreate`.** The report date is set to today. The break string `curfinancedefaultcollectionbreakstring` is "week" if `globalvars.isselectedlineisweeklyline`, else "day". `curmethod = defaulttables.getInstance().collectionreportprintingmethodname(softsettingsvalue.collectionreportprintingmethod_21)` — the default from the server or the Settings screen; the pref default is 1. Checkbox rules:
   - method allows collections → checkbox shown, unchecked;
   - methods 7/8 → shown, checked and disabled;
   - all others → hidden.
2. **Method picker result** (request 7021, resultCode 1, extra `method`). Sets `curmethod` **and** `softsettingsvalue.collectionreportprintingmethod_21`. This lasts for the session only; it is not written to prefs or the server. Checkbox rules after picking:
   - includable methods → visible, enabled and checked;
   - methods 7/8 → checked and disabled;
   - methods 1/9/10 → hidden.
3. **Date picker.** Sets `reportdate` and refreshes both labels. As a side effect it also sets the global `globalvars.currentweekdayid` (Calendar day-of-week, 1 = Sunday).
4. **"Print".** Sets `isincludecollectionsselected` to the checkbox state, then runs `AsyncDownloaddata` with the spinner shown:
   1. Loads the driver `net.sourceforge.jtds.jdbc.Driver` and connects to the selected-branch DB.
   2. Builds `collectionperiod` (a list of `LocalDate`) from `N = noofbreaksforprevcollection`. Steps are days on daily lines and weeks on weekly lines:
      - N < 0 → for i = |N|…1, adds `reportdate − i`. These are the previous |N| breaks, stored ascending.
      - N > 0 → for i = N…1, adds `reportdate + (i−1)`. These are the next N breaks starting today, stored descending.
      - Methods 7/8 (N = 0) → every day of the report month (`withDayOfMonth(1).plusDays(i−1)`, or `plusWeeks` on weekly lines).
   3. Picks the collection-list proc from two flags (table below). It runs `exec <proc> ?,?,?` with (date1, `curfin.FinId`, date3), query timeout 3000 s:
      - methods 1–6/9/10 → date1 = date3 = report date;
      - methods 7/8 → date1 = last period date (month end for daily lines), date3 = first day of the month − 1.

      | `enablemoniteringonloanclosoure_5` | Weekly line | Procedure |
      |---|---|---|
      | true | yes | `APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport` |
      | false | yes | `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport` |
      | true | no | `APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport` |
      | false | no | `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport` |

      The same procs are called with named parameters `@ColDate`, `@FinId` (and `@CollectionListfromDate` for the without-weekday-support variants) in `downloaddata`, `otpverificationact` and `editing_collectionswithfulloptions_act`. The positional parameter names are not visible in the APK.
   4. Each result row becomes a `collectionreport_data` with these fields: `UDLedgerId`, `ShowId`, `SubAreaName`, `CWLedgerId` (printed as party number "PN"), `PartyName`→`LedgerName`, `Tag`, `MCLoanAmountCollectable`, `MobileNo1`, `MCLoanId`, `MCLoanDate`, `InsAmount`→`CurDue`, `DuesCount`, `PrevCollected`→`Collected`, `CollectionBreakString`. One field is computed: `LoanBalance = MCLoanAmountCollectable − PrevCollected`. `Tag` and `MobileNo1` are loaded but never printed.
   5. If "Include Collection Details" is checked, paid amounts are loaded into a `daywisecollforreportobj` list (`MCLoanId`, `MCLDueColDate`, `Paid`). If it is unchecked the list stays empty and the date cells stay blank, as hand-entry boxes.
      - Methods 7/8 → `select MCLoanId,MCLDueColDate ,FinId,Paid  From tblMCLoanDueCol where FinId=? and MCLDueColDate>=? and MCLDueColDate <=?`, from the 1st to the last day of the report month.
      - Other methods → `exec GetLastNCollectionforCollReportPrint ?,?,?,?` with (report date, FinId, "day"/"week", N).
   6. Sorts the loans by `ShowId`, then `UDLedgerId`. Any exception sets `iserroroccured`, which leads to the "Error Occured" toast; otherwise the "Print Options" dialog opens. The progress messages "Connecting" and "Importing Collection List" are published but `onProgressUpdate` is empty, so they are never shown.
5. **`createPdf(name)` / `sharePdf(name)`.** The file name is `cr_` + `ddMMyyyyHHmmss`.
   1. Page size: 595×842 pt (A4 portrait); 842×595 (A4 landscape) for method 7; 1000×595 for method 8.
   2. `totalpages = calculatenoofpages()`, and the "printed" date/time is captured.
   3. For each page: `startPage` → `drawPage_methodNN(page, i)` → `finishPage`.
   4. The PDF is written to `getExternalStoragePublicDirectory(DOCUMENTS)/venusmfc/<name>.pdf`; the directory is created if missing.
   5. Show → `ACTION_VIEW` with a FileProvider URI (`<package>.provider`, read grant). Share → `ACTION_SEND` + `EXTRA_STREAM`, MIME `text/csv`, chooser "Share".
6. **Common page layout** (all methods):
   - "Page n" at top right (10pt).
   - Centred 12pt title: "COLLECTION LIST" for methods 1/7/8; the action-bar title "Collection List Print" for the others.
   - Second line: `FINANCE : <line>` (1/7/8) or `FIN : <line>` on the left. On the right, `PERIOD: <date>` (method 1), `PERIOD: 01-MM-yyyy to  <month end>` (7/8) or `Date: dd-MM-yyyy` (others).
   - A ruled grid of 20-pt rows in black 1-pt lines. Body text is 10pt; dense cells use 8/7/6pt.
   - A full-width row with the centred `SubAreaName` wherever the area changes.
   - Footer "Printed @ dd-MM-yyyy, HH:mm:ss" (8pt), only on pages that fill up.
   - All labels are English. Party and area names print as stored in the DB; Tamil names render with the system font. **No method prints totals.**
7. **Pagination.**
   - One-column pages hold 33 lines (`numberoflinesperpage`); methods 7/8 hold 20 (`numberoflinesperlandscapepage`). Area-heading lines count toward the limit.
   - Two-column methods fill the left half of the page (x 20–270) for 33 lines, then the right half (offset 290 pt). The column header and the current area heading repeat at the top of each half.
   - The "No" serial (methods 1/7/8) runs continuously across areas and pages.
8. **Status formula** (the "D/S" column in 4, "Due" in 6, "Sts" in 10): the text is `CollectedDues/CompletedDues`.
   - `CollectedDues = PrevCollected ÷ InsAmount` (integer division).
   - `CompletedDues` = whole days from `MCLoanDate` to the report date, divided by 7 when the loan's `CollectionBreakString` is "W" or "ww" (weekly).
   - Example: "40/45" means 40 installments paid out of 45 elapsed.

**The ten printing methods.** Definitions come from `defaulttables.collectionreportprintingmethods()`; the ids are `constantvalues.collectionlistprintingmethods.method01…method10`. [d−k] means a column headed with the `dd-MM` date k breaks before the report date; it holds the paid amount when collections are included and is blank otherwise. "Col"/"Coll" is always a blank box for writing today's collection.

| Id · picker name | `columns` · N · includable | Page / grid | Columns (left → right) |
|---|---|---|---|
| 1 · "1-Collection List for Next Seven Days" | 1 · +7 · no | A4 portrait, 1 column × 33 | No (serial) · Party Name · Date (loan date dd-MM-yy) · Loan (`MCLoanAmountCollectable`) · Balance (`LoanBalance`) · 7 blank boxes headed with the report date … +6 (weeks on weekly lines) |
| 2 · "2-Collection List for Single Day" | 2 · −1 · yes | A4 portrait, 2 columns × 33 | PN (`CWLedgerId`) · Party Name (7pt) · Date (loan dd-MM) · Due (`InsAmount`) · [d−1] · Bal (`LoanBalance`) · Col |
| 3 · "3-For today with Prev two days collection" | 2 · −2 · yes | 2 × 33 | PN · Party Name · LA (`MCLoanAmountCollectable`) · [d−2] · [d−1] · Coll |
| 4 · "4-LoanAmount,Balance,Status,PrevDayCollection,Today Collection Entry (Two Columns)" | 2 · −1 · yes | 2 × 33 | PN · Party Name · LA · D/S (status) · [d−1] · Bal · Col |
| 5 · "5-Two columns with Loan Amount and Three Days Prev Collection" | 2 · −3 · yes | 2 × 33 | PN · Party Name · LA · [d−3] · [d−2] · [d−1] · Coll |
| 6 · "6-Two columns with Loan Amount,Status and Prev Two Days Collection and Today Collection Entry" | 2 · −2 · yes | 2 × 33 | PN · Party Name · LA · Due (status x/y, 6pt) · [d−2] · [d−1] · Coll |
| 7 · "7-Monthly Collection Report with LoanDate Prev Balance and next Month Balance (A4 Size)" | 1 · 0 · forced | A4 landscape 842×595, 1 column × 20 | No · Party Name · Date (loan) · Loan · Balance (balance before the month: collectable − `PrevCollected`) · one 7pt cell per day 1…28–31 (paid that day from `tblMCLoanDueCol`, "----" before the loan date, else blank) · Bal = Balance − month's collections, or "Closed" if ≤ 0 |
| 8 · "8-Monthly Collection Report with LoanDate,Status,LoanMethod, Prev Balance and next Month Balance (Legal Size) " | 1 · 0 · forced | 1000×595 pt (legal-width landscape), 1 column × 20 | Same as 7, with Due (`InsAmount`) after Loan |
| 9 · "9-Status and Balance with  Two Days Entry" | 2 · +2 · no | 2 × 33 | PN · Party Name · Due (`InsAmount`) · Sts (`DuesCount` from the proc) · Bal · [today] · [tomorrow] (entry boxes) |
| 10 · "10-Status and Loan Amount with Four Days Entry" | 2 · +4 · no | 2 × 33 (≈270-pt halves) | PN · Party Name · LA · Sts (status x/y) · 4 entry boxes headed today … +3 |

**Functions.**
- `onCreate()` — binds views and sets the default date, method and checkbox state; wires the click listeners.
- `lambda$onCreate$1()` — opens the method picker (request 7021).
- `lambda$onCreate$3()` / `lambda$onCreate$2()` — show the date picker / apply the picked date (labels and `globalvars.currentweekdayid`).
- `onActivityResult()` — applies the picked method session-wide and re-applies the checkbox rules.
- `calculatenoofpages()` — simulates the chosen method's pagination (20/33 lines, 1 or 2 columns, area-heading lines) to get `totalpages`.
- `createPdf(String)` — renders all pages, saves the PDF and opens it in a viewer.
- `sharePdf(String)` — renders the same PDF and shares it.
- `drawPage_method01()` … `drawPage_method10()` — draw one page in the matching layout from the table. They advance the shared cursors `curprintingrowcount`, `CurPageRow`, `curcol` and `LastSubAreaName`. Methods 7/8 build a `Map<Date, Map<loanId, paid>>` per row to fill the day cells.
- `lambda$drawPage_method07$4()` / `lambda$drawPage_method08$5()` — `HashMap` factories for `computeIfAbsent`.
- `drawCenter()` — centred text (+25 pt) for area headings.
- `drawCenterwithgivenwidth()` — centres text inside a column.
- `drawCenterwithbox()` — centred text in a rounded box; unused.
- `AsyncDownloaddata.onPreExecute()` / `doInBackground()` / `onPostExecute()` — show the spinner, load the data, show "Print Options". `onProgressUpdate()` is empty. `lambda$onPostExecute$0()` dispatches Show/Share.
- `MyPrintDocumentAdapter.onLayout()` / `onWrite()` / `pageInRange()` — an Android Print Framework adapter that is never instantiated.

**Rules & edge cases.**
- **Prerequisites and access.**
  - A line must be selected; a null `globalvars.curfin` raises an exception and the "Error Occured" toast.
  - There are no role or special-access checks inside the screen.
  - The JDBC connection is never closed. Tapping "Print" twice runs two loads in parallel on the same list.
- **Method setting.**
  - A method picked here overrides the global soft setting for the session only.
  - The permanent default is SoftSettings SSId 11, edited in `settings_act`. That screen's handler for request 7021 reads the extra `spinnerserchobjectname` as `spinnersearch_data`, but the picker returns `method`, so choosing a method in Settings throws a NullPointerException (a bug outside this class).
  - A method id outside 1–10 (e.g. a soft setting of 0) throws `IllegalStateException("Unexpected value: …")` inside the page loop and leaves a page open; the following `PdfDocument.writeTo` then throws uncaught.
- **"Include Collection Details" checkbox.**
  - It is unchecked when the screen opens but checked after a method is picked.
  - A hidden checkbox keeps its old state. For example, switching from method 7 to method 9 leaves it checked, so `GetLastNCollectionforCollReportPrint` is still called, with a positive N.
- **Layout-specific quirks.**
  - **Monthly methods on weekly lines are broken.** The period is built with `plusWeeks`, giving 28–31 weekly dates (about 7 months). The header still shows day numbers 1…N, `tblMCLoanDueCol` is read only for the month, and the proc's first date also falls months ahead. The monthly grid is only correct for daily lines.
  - **Method 8** promises "Status" and "LoanMethod" in its name but adds only a Due column. Unlike method 7 (and like method 1), it does not repeat the area heading after a page break.
  - **Method 9** matches collections by day-of-month only and draws every match at the same x position. The cells are normally blank because collections are not fetched for this method.
  - **Status divides by `InsAmount`.** A loan with a zero installment throws an `ArithmeticException`, which is caught for that row, so the row prints incomplete. Method 2 also computes the status (and `% 365` day counts) but never prints it.
- **Pagination quirks.**
  - The last page never gets the "Printed @" footer, because the footer is drawn only when a page fills.
  - For methods 1/7/8, `calculatenoofpages()` reserves one more line per page than the drawing code uses. This can produce a trailing page that has only headings.
- **Output.**
  - Share sends the PDF with MIME `text/csv`.
  - `ACTION_VIEW` is not wrapped in try/catch, so the app crashes if no PDF viewer is installed.
  - Files go to public `Documents/venusmfc/`. The FileProvider `@xml/file_paths` exposes `external-path "."`, i.e. all of external storage, to granted URIs.
  - The media-size `PrintAttributes` set in `createPdf`/`sharePdf` (A4, A4 landscape, `NA_LEGAL` landscape) are unused.
- **Dead print adapter.** `MyPrintDocumentAdapter` is never used (there is no `PrintManager` call). If it were, it would draw method 1 underneath every other method and close the document inside its loop.
- **Decompile caveats.**
  - JADX flags `drawPage_method02…10`, `createPdf` and `sharePdf` as decompiled incorrectly, and mis-nests the `doInBackground` loop. The logic above was reconstructed from smali (`.line` numbers and branches); conditions JADX shows as `33 > CurPageRow` are really `33 <= CurPageRow`.
  - JADX also substitutes constants that merely have the same value. Black paint appears as `ViewCompat.MEASURED_STATE_MASK` (0xFF000000), the weekly code "W" as `ExifInterface.LONGITUDE_WEST`, and the driver literal as `globalvars.driver`. These are constant-folding artefacts, not library usage.

## NGX Bluetooth printer setup (unreachable) — `BluetoothPrinterMain`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity). **Not declared in AndroidManifest.xml**, and no Intent anywhere targets it, so it can never be opened. It looks like the NGX SDK demo's main screen copied into the app. |
| **Layout(s)** | `btp_main.xml`; options menu `res/menu/bluetooth_printer_main.xml`; hosts the empty `UnicodeFragment` |
| **Opened from** | Nowhere. Its static field `mBtp` (= `com.ngx.BluetoothPrinter.INSTANCE`) is reused by `tabbedindividualcollectionentry`. |
| **Opens** | NGX "List of devices" dialog (`mBtp.showDeviceList`), the system enable-Bluetooth prompt (from `initService`), confirmation `AlertDialog`s |
| **Data** | No DB. The SDK stores the preferred printer's MAC address in default SharedPreferences under key `mBtpDevice`. |

**Purpose.** It would let the user pick and connect an NGX Bluetooth thermal printer, forget the preferred printer, or unpair printers, with the connection status shown at the bottom of the screen.

**UI.** FrameLayout `container` (light pink #fffff2ff) holds an empty `UnicodeFragment` on back stack "home". `tvStatus` at the bottom starts as "Status" and changes to "not connected", "connecting...", `connected: <device name>`, or SDK status text.

**Menu / actions.** All items sit in the overflow menu with raw, untranslated titles:
- **"connect_device"** (`action_connect_device`, order 99) → `mBtp.showDeviceList(this)`. This first clears any preferred printer. It then shows the NGX dialog "List of devices" with "Paired Devices", "Other Available Devices" and a "scan devices" button. Choosing a device connects over RFCOMM SPP (UUID 00001101-0000-1000-8000-00805F9B34FB) and saves it as preferred.
- **"clear_device"** (`action_clear_device`, order 100) → confirmation "NGX Bluetooth Printer" / "Are you sure you want to delete your preferred Bluetooth printer ?" → `clearPreferredPrinter()` → toast "Preferred Bluetooth printer cleared".
- **"unpair_device"** (`action_unpair_device`, order 200) → confirmation "Bluetooth Printer unpair" / "Are you sure you want to unpair all Bluetooth printers ?" → `unPairBluetoothPrinters()` → toast "All NGX Bluetooth printer(s) unpaired".
- **Back key** → finishes if the fragment back stack has ≤ 1 entry, otherwise pops it.

**How it works.**
1. **`onCreate`** calls `mBtp.initService(this, mHandler)`. The SDK gets the BluetoothAdapter and loads `mBtpDevice`. If Bluetooth is off it asks to enable it (request code 2), and it requests location, storage and Bluetooth-scan/connect permissions. The activity then calls `setDebugService(true)` and `setPrintFinishStatus(true)`, and adds `UnicodeFragment` to the container.
2. **`mHandler`** handles SDK message codes:
   - 1 `MESSAGE_STATE_CHANGE`: arg1 0 → toast "Not connected" and the same status text; 1 → "not connected"; 2 → "connecting..."; 3 → "connected: " + device name.
   - 4 `MESSAGE_DEVICE_NAME`: stores the device name and MAC (`mConnectedDeviceMacID`) and toasts `Device: <name>\nMac ID: <mac>`.
   - 5 `MESSAGE_STATUS`: shows the status text.
   - 9 (`PRINTER_SWITCHED_OFF`): toasts the status text.
   - 10 (`PRINT_FINISH`): toasts `print_status`.
3. **Lifecycle.**
   - `onActivityResult` forwards to `mBtp.onActivityResult(...)`: once Bluetooth is enabled, the SDK connects to the preferred printer, or shows the device list if there is none.
   - `onDestroy` → `mBtp.onActivityDestroy()`, which closes the service.
   - `onResume` only logs via `DebugLog`.

**Functions.**
- `onCreate()`, `onCreateOptionsMenu()` (inflates `bluetooth_printer_main`), `onOptionsItemSelected()` (the three actions), `onKeyDown()` (fragment back stack), `onActivityResult()`, `onDestroy()`, `onPause()` / `onResume()` (no-op / log).
- `handleMessage()` — status and toast handling, as above.
- `changeFragment(int, Fragment, boolean)` — hides the current fragment in `container` and adds another; unused.
- `lockOrientation(Activity)` — locks the current orientation; unused.

**Rules & edge cases.**
- **Unpair all clears every pairing on the phone.** The SDK's `unPairBluetoothPrinters()` calls the hidden `BluetoothDevice.removeBond` on every bonded device, not only NGX printers, so headsets and car kits are unpaired too.
- The screen is unreachable. Bluetooth printing elsewhere relies only on its static `mBtp`.
- The SDK's own permission check passes literal strings such as `"Manifest.permission.ACCESS_FINE_LOCATION"` to `checkSelfPermission`, so it always re-requests permissions (an SDK bug).
- Status texts and menu titles are hard-coded English.

## Printing call sites across the app
**Common template.** Most classes below share the same three pieces:
- An inner `MyPrintDocumentAdapter extends PrintDocumentAdapter` that draws pages on a `PrintedPdfDocument`.
- `printDocument()`, which calls `PrintManager.print("<app_name> Document", adapter, attrs)`. This is "Direct Print": the system print dialog, which offers any installed print service or "Save as PDF".
- `createPdf()` and `sharePdf()`, which draw the same pages on a `PdfDocument`, save it, and open it (`ACTION_VIEW` through the FileProvider) or share it (`ACTION_SEND`).

Only `tabbedindividualcollectionentry` uses the NGX Bluetooth SDK. No app class writes raw ESC/POS bytes or opens Bluetooth sockets itself, and no report loads the asset fonts.

| Class | What is printed | Method / SDK and outputs |
|---|---|---|
| `collectionlistprints` | Collection sheets, 10 layouts (documented above) | PdfDocument → Show as PDF / Share (no direct print) |
| `tabbedindividualcollectionentry` | "Print receipt" ImageButton `ilc_printreceipt` (in `fragment_tabbedindividualcollectionentry`). It prints only the bitmap `assets/image/img.png` (the round "Fins" app logo), with **no collection or receipt data**. | NGX SDK via `BluetoothPrinterMain.mBtp`: if not connected (state ≠ 3), toast "Printer is not connected"; otherwise `setPrinterWidth(PRINT_WIDTH_72MM)` (3", 576 dots), `setPrinterType(NGX_CUSTOM_COMMANDS)`, `addImage()`, `print()`. The printer is connected from the map-locator button's "Choose Action" dialog → "Connect Printer" (`showDeviceList`). `initPrinterSafely()` requests BLUETOOTH_CONNECT/SCAN (request 101) on Android 12+, but nothing initialises the service after permission is granted. A typeface is created from `fonts/Cousine-Regular.ttf` but never used; the helper `saveBitmap()` (`cache/prints/receipt.png`) is also unused. |
| `editing_collections_act`, `editing_collectionswithfulloptions_act` | Collection list for a date ("COLLECTION LIST", "Collection Date  :  ") | "Print and Share Options": Direct Print / Show as PDF / Share; 800×1140-pt page |
| `selectedloancollectiondetails` | One loan's collection history ("Collection Details") | Same three options, 800×1140 |
| `cashbook` | Cash book (SNo, Particulars, Debit, Credit, from local SQLite table `Cashbook`) | Menu `action_printcashbookid` → PrintManager (A4, 38 lines/page). During cash-book verification `createPdf()` → `ASYNCUploadDailyReportFile` uploads the PDF (OkHttp multipart) to soft setting `daily_report_file_upload_url`, then sends a mail via `email_sending_url`. |
| `uploadcollection` | Daily collection report `dailyreport-<ddMMyyyy>.pdf` (serif-italic headings), created during upload | `createPdf()` → upload to `daily_report_file_upload_url` + mail |
| `dailypattinew_act`, `dailypatti_nadapunippuphakkilist_act` | Daily patti ("Daily Patti the Period from … to …"; nadappu/nippu/balance lists) | Menu `menuwithprintingoptions_directprint / showaspdf / shareaspdf` ("Share PDF"), A4 |
| `dailythittamforallowedlines` | Daily thittam for the allowed lines (new loans in bold); job name `Thittam_<header>` | Same menu |
| `simplethittamlist_act` | "Simple Thittam the Period from …" | Same menu |
| `varathittam` | Weekly thittam (A4 landscape; requests typeface "Courier New") | "Print Options": Print / Show as PDF / Share |
| `handloans_list` | "HAND LOANS REPORT" | `menuwithprintingoptions_*` |
| `invtransactnew` | Inventory "DELIVERY NOTE" (800×1140) | `menuwithprintingoptions_*` |
| `loanledgers_list` | ST/OF loan ledger ("Party: …", Opening/Closing Balance), A4 landscape | `menuwithprintingoptions_*` |
| `partyledgers_list` | Party ledger ("Party: …") | `menuwithprintingoptions_*` |
| `partyledgers_parameters` | Ledger statements, incl. "OFL Interest Pending" and "Int Colleted Upto" (A4/A5 landscape) | "Print Options": Direct Print / Show as PDF / Share |
| `meetingreport` | "STL/OFL Interest Pending , Fin :" lists (792×576 pt) | Menu `action_printlist` → PrintManager / PDF |
| `stloans`, `ofloans` | ST / OF loan voucher "கடன் வவுச்சர்", including document charges "ஆவணக் கட்டணம்" (Tamil text) | `stloan_printbuttonid` → "Print Options" (Direct Print / Show as PDF / Share), A5 landscape; `ofloan_printbuttonid` → direct print only |
| `stloaninterestcalculationandpayments`, `ofloans_interestcalculationandpayments` | Interest calculation / payment receipt ("… முதல் … வரை" period, "Collectable :"), A5 landscape | Direct Print / Show as PDF / Share; the ST screen adds "Share with Whatsapp" / "Share with Whatsapp Business" |
| `tbbspl_list` | "TRAILBALANCE-…", "P & L STATEMENT-…", "BALANCE SHEET-…" | `menuwithprintingoptions_*` |
| `monthlypandl_fragment` | `Monthly P and L -<month>` | Menu `partnerwiseperformancemenu_directprint / showaspdf / shareaspdf` |
| `partnerwiseloansandperformance_fragment` | Partner performance ("Partner Name: …") | "Print Options" + `partnerwiseperformancemenu_*` |
| `analysisrep_financewiseabstract_fragment`, `analysisrep_partnerwiseabstract_fragment`, `analysisrepfin_interestratedist_fragment`, `analysisrepfin_overallinterestratedist_fragment` ("Overall ROI Analysis"), `analysisrepfin_partnerwiseabstract_fragment`, `analysisrepfin_partyrwiseabstract_fragment` | ST/OF loan analysis abstracts (header strings from `globalvars.curanalysisreport`) | `partnerwiseperformancemenu_*`, A4 |

## Third-party printing and imaging SDKs bundled
| Package | What it is | App classes that use it |
|---|---|---|
| `com.ngx` — `BluetoothPrinter` (enum singleton), `Enums.PrinterTypes`, `Enums.NGXBarcodeCommands`, `PrinterWidth`, `DebugLog`, obfuscated `a`…`n` | NGX Technologies Bluetooth thermal-printer SDK. Features: SPP/RFCOMM connection; device-picker dialog; preferred printer stored as `mBtpDevice` in default prefs; text and Unicode text (rendered through `TextPaint` to a bitmap), images, raster images, barcodes/QR; paper/battery status; auto-switch-off. `PrinterTypes` selects `NGX_ESC_COMMANDS` (ESC/POS) or `NGX_CUSTOM_COMMANDS`. `PrinterWidth` offers 48 mm (2", 384 dots), 72 mm (3", 576 dots) and 104 mm (4"; present in the enum but rejected by `setPrinterWidth`). | `BluetoothPrinterMain` (unreachable); `tabbedindividualcollectionentry` (connect + logo print) |
| `com.ngxtech.selector` — `FileData`, `FileOperation`, `FileUtils`, `OnHandleFileListener`, `TextViewWithImage` | File-chooser helpers from the NGX demo app (for picking a file or image to print) | Only `net.venussolutions.selector.FileListAdapter`, which nothing references (dead code) |
| `com.mikhaellopez.circularimageview.CircularImageView` | Circular ImageView with border and shadow; not printing-related | XML only: `activity_documentupload` (`documentsupload`); `layout_toolbar_profile` (included by `activity_newpartycreation` and `activity_partyandpartnerdetailsnew`); `activity_quickacccounttransactions` (`accounttransactions_act`) |
| `com.jsibbold.zoomage.ZoomageView` | Pinch-zoom / pan ImageView; not printing-related | XML only: `activity_fullimageview` (`fullimageview`) |
| `assets/fonts/*.ttf` — 20 fonts: F25_Bank_Printer and its Bold, NotoSansTamil-Regular, akshar, Cousine Regular/Bold, CourierPrime, RobotoMono, Roboto, DejaVuSerif + Condensed, DroidSerif, DroidSansMono, andale-mono, calibri, CALIST, BOOKOS, CENTURY, CENSCBK, LBRITE. Also `assets/image/img.png` and `assets/image/ngx.jpg`. | Printer and demo fonts and images | Only `fonts/Cousine-Regular.ttf` (loaded but not applied) and `image/img.png` (the printed logo) are referenced, both from `tabbedindividualcollectionentry`. Every PDF report uses system fonts. |

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `collectionreportprintingmethodsobj` | Serializable model | `defaulttables.collectionreportprintingmethods()` / `collectionreportprintingmethodname()`, `collectionreportprintingmethods_searchlist` + its adapter, `collectionlistprints`, `settings_act` | Fields: `methodid` (1–10); `methodname` (picker text); `columns` (1 or 2 columns per page); `iscollectioncanbeincluded` (shows "Include Collection Details"); `noofbreaksforprevcollection` (N < 0 = previous N days/weeks, N > 0 = next N starting today, 0 = whole month). Getters and setters only. |
| `collectionlistprints.AsyncDownloaddata` | `AsyncTask<String,String,String>` | `btnprint` | Loads loans and collections (How it works, step 4); `iserroroccured` flag; "Print Options" dialog → `createPdf` / `sharePdf` |
| `collectionlistprints.MyPrintDocumentAdapter` | PrintDocumentAdapter (dead code) | nothing | `onLayout`: new `PrintedPdfDocument` sized from the print attributes, page count `totalpages`, job `<ReportHeader without ':'>.pdf`. `onWrite`: draws the requested pages. `pageInRange()` |
| `BluetoothPrinterMain` anonymous `Handler` (`$1`) and dialog listeners (`$2`–`$5`) | Handler / `DialogInterface.OnClickListener` | `BluetoothPrinterMain` | NGX status messages → status text and toasts; Yes/No handlers for clear-preferred and unpair-all |
| `UnicodeFragment` (outside the dossier list) | Empty `android.app.Fragment` | `BluetoothPrinterMain` | Placeholder: the NGX demo's "Unicode print" page with all code stripped |
| `collectionreport_data`, `daywisecollforreportobj` (outside group) | Models | `collectionlistprints` | One loan row (fields in How it works, step 4.4); one paid entry (`MCLoanId`, `MCLDueColDate`, `Paid`, plus `dayorweekno`, unused here) |
| `App` (cross-cutting, outside group) | `Application` subclass | none | Prefs file `MyPref`: `PRINTER_SELECTION` (`getPrinerSize()`, default 2), `PRINTER_TYPE` (`getPrinterType()`, default 1), plus their setters. Nothing calls them, and `App` is not set as `android:name` on `<application>`, so it is never instantiated and `getInstance()` would return null. Printer width and type are therefore hard-coded at the call site (72 mm, NGX custom commands). |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_collectionlistprints.xml` | `collectionlistprints` | ConstraintLayout `main` → vertical LinearLayouts containing: `tvRouteNameid` ("Line Name", 18sp bold #c015eb); label "Collection Date" / "வசூல் தேதி" (teal); `dcl_collectiondateid` (sample "01-06-2001", 25sp white on black, tappable); `dcl_collectiondayid` ("WEDNESDAY", 25sp white on chocolate); label "Collection Report Printing Method"; `reportprint_collectionreportprintingmethodid` (read-only 3-line EditText, style `readonlybox`, tappable); `chkincludecollectionid` "Include Collection Details"; `btnprint` "Print". Plus the centred `collectionreportprint_pbarid` ProgressBar (gone by default). |
| `btp_main.xml` | `BluetoothPrinterMain` (unreachable) | FrameLayout `drawer_layout` → FrameLayout `container` (background #fffff2ff, fragment host) + bottom TextView `tvStatus` "Status". Its menu `bluetooth_printer_main.xml` has `action_connect_device` "connect_device" (order 99), `action_clear_device` "clear_device" (100) and `action_unpair_device` "unpair_device" (200), all in the overflow. |

---
[← brain.md](../../brain.md) · Module 8.18 of 20 · Previous: [8.17 Inventory module: stock masters, items, customers/suppliers, purchase and stock in hand](17-inventory-module-stock-masters-items-customers.md) · Next: [8.19 Shared data-access layer, server-driven settings and utilities](19-shared-data-access-layer-server-driven.md)
