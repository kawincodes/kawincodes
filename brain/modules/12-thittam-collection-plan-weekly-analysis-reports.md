# 8.12 Thittam (collection plan) and weekly analysis reports

[← brain.md](../../brain.md) · Module 8.12 of 20 · Previous: [8.11 Daily Patti (outstanding) reports and meeting report](11-daily-patti-outstanding-reports-meeting-report.md) · Next: [8.13 Accounts transactions, cash/bank book, expenses & incomes, hand loans and ledger statements](13-accounts-transactions-cash-bank-book-expenses.md)

"Thittam" (திட்டம், "plan") is the line-level cash and collection plan that managers check or print each day or week. The module has three report families: **Daily Thittam**, one day's cash-book plan for a line from stored procedure `singleFinanceCashBook` (`dailythittamforallowedlines` is the live screen; two older variants remain); **Simple Thittam**, a period summary of cash and bank movements from `LedgerwiseBalance` + `SimpleThittam` (`simplethittamlist_act`); and the **weekly reports** driven by `Rep24WeekCollection` (the 24-week collection analysis list `weeklycollectionanalysis_act`, its party-wise weekly patti grid `weeklycollectionpatti_dashboard` + custom view `weeklycollectionpatti_dashboardlayout`, and the one-page A4 "Varathittam" sheet `varathittam`). SQL Server stored procedures do almost all of the arithmetic; the app runs them in order, keeps the results in lists or local SQLite temp tables, and renders lists and PDFs. Most screens can print, show as PDF or share, and nothing is written back to the server except the procedures' own work tables.

## Daily Thittam per line (swipe pager) — `dailythittamforallowedlines`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), manifest theme `AppTheme.NoActionBar`. Has its own Toolbar. A `ViewPager` with a `FragmentPagerAdapter` (`SectionsPagerAdapter`) holds one `PlaceholderFragment` page per finance line. |
| **Layout(s)** | `activity_dailythittamforallowedlines.xml`; page `fragment_allowedfinancedailythittam.xml`; rows `allfinancedailythittamrow.xml` (via `allfinancedailuthittamadapter`); menu `menuwithoutsearchandprintoptions` |
| **Opened from** | Drawer › Reports › "Daily Thittam / தினத்திட்டம்" (`nav_allfinancedailythittam`) → `refreshorreloadfinancelist` (`calledfrom`=3). That screen is titled "Daily Thittam", shows a report-date picker (default today) and lists local `FinanceDetails` ordered by `ShowId`. Tapping a line starts this screen with extras `passedfinid` (FinId), `listindex` (tapped position), `financelist` (Serializable `List<financemaster_data>`) and `reportdate` (`java.sql.Date`). |
| **Opens** | No app screens. Uses the Android print dialog, a PDF viewer (`ACTION_VIEW`) and the share chooser. |
| **Data** | Remote (`globalvars.selbranchconnString`, through helper `ASYNCGetsingleFinanceCashBook`): `exec singleFinanceCashBook @IMEI='<globalvars.device_id>',@ReportDate='<yyyy-mm-dd>', @FinId=<FinId>`. Opens SQLite into `globalvars.db` but does not query it. In-memory static cache `pagedatastore` (SparseArray: page index → rows). Files: `<getExternalFilesDir(null)>/reports/dt__<yyyyMMdd_HHmmss>_<rand>.pdf`. |

**Purpose.** Shows the Daily Thittam for the chosen date: the line's cash-book or plan rows as returned by the server. The user swipes between all lines held on the device and can print, PDF or share the current line.

**UI.** The toolbar title is `திட்டம்-dd-MM-yyyy` ("Thittam-date"), with an Up arrow. Each page has:
- an indeterminate progress bar while loading;
- `section_label`, showing the line's `FinMailingName`;
- a header row "Ledger · Particulars · Credit · Debit";
- a RecyclerView whose rows show Ledger name · Particulars · Debit amount · Credit amount. Amounts are shown only when > 0, as integers.

The header labels are crossed with the data fields. The column titled "Credit" sits over the row's `Debit` value and "Debit" sits over `Credit`, and the PDF does the same. This appears to follow the Tamil வரவு/பற்று convention: receipts (cash debit) are shown as "Credit" and payments as "Debit". Row colours come from `IsNewLoan`: 1 → red `#DC190C` (`colorfornewloannewparty`), 2 → dark green, anything else → black. `ShowId` colours are dead code (see adapter).

**Menu / actions.**
- Search is hidden (`visible=false` in the XML). If shown, it would filter the static `mAdapter` by ledger name or particulars.
- "Direct Print", "Show as PDF" and "Share" act on the visible page.
- "Refresh Ledgers" is hidden in code.
- Up/Home → back.
- Row tap does nothing (`onContactSelected` is empty).

**How it works.**
1. `onCreate` reads `reportdate`, `financelist` and `listindex`. It also reads `passedfinanceid`, but the caller sends `passedfinid`, so that field stays 0 (it is unused). It then clears `pagedatastore`, builds the pager (page count = `financelist.size()`), jumps to `listindex`, sets the title and opens SQLite.
2. `PlaceholderFragment.newInstance(pos+1, reportdate)` stores `section_number` and `date` (the `sql.Date` as a string). `onCreateView` shows `FinMailingName`, creates a new adapter and stores it in the static `mAdapter`. It then executes an anonymous `ASYNCGetsingleFinanceCashBook(FinId, Date.valueOf(date))`. Its `onPostExecute` fills the list, saves a copy in `pagedatastore[section-1]`, and hides the label and progress bar.
3. `setUserVisibleHint()` detaches and re-attaches the fragment. As a result, every time a page becomes visible or invisible its view is rebuilt and the procedure runs again.
4. The helper maps these columns: `SNo, ShowId, TransId, FinId, LedgerId` (string), `Ledgername, Particulars, IsNewLoan, TransDate` (string), `TransType, Debit, Credit, Narration`. `Debit` and `Credit` are read with `getInt`, so paise are truncated. The client does no arithmetic: the procedure defines every row, including any totals.
5. Print, PDF and share all call `preparePrintData()` first:
   - It takes `pagedatastore[currentItem]`. If that is empty, it shows the toast "பட்டியல் காலியாக உள்ளது" ("The list is empty") and stops.
   - It sets `printheadername = FinMailingName` and `totalpages = ceil(rows/30)` (minimum 1).
   - It stamps `currentDate` (`dd-MMM-yyyy`) and `currentTime` (`hh:mm a`).
6. `buildPdfDocument()` → `drawPage()` builds A4 portrait pages (595×842 pt):
   - top right: "Page n / N"; title: `R.string.dailythittam_` ("தினத்திட்டம் - ") + finance name + "  (dd-MMM-yyyy)";
   - a ruled grid with columns S.No · Ledger Name · Particulars · Credit · Debit, 30 rows per page;
   - `Debit` (if > 0) is printed under "Credit" and `Credit` under "Debit", formatted `##,##,##,##0`;
   - rows with `IsNewLoan == 1` use fake-bold; footer "Printed @ <date>, <time>".
7. **Show as PDF**: `writePdfToFile("dt__…")` saves to `<external files>/reports/` (failure toast "PDF உருவாக்க முடியவில்லை", "Could not create PDF"). It then opens the file with `ACTION_VIEW application/pdf` through FileProvider `<package>.provider`. If no viewer exists: toast `R.string.noapptoopenpdf` ("PDF திறக்க செயலி இல்லை", "No app to open PDF").
8. **Share**: same file, `ACTION_SEND application/pdf` with `EXTRA_SUBJECT` "தினத்திட்டம் - <finance>" and chooser "Share PDF".
9. **Direct Print**: `PrintManager` job `Thittam_<finance>`, A4, no margins. `onWrite` rebuilds the whole document and reports `ALL_PAGES`, so it ignores the page range the user selected.

**Functions.**
- `onCreate()` — reads extras, builds the pager, sets the title.
- `onCreateOptionsMenu()` — inflates the menu, hides Refresh, wires the (hidden) search.
- `onOptionsItemSelected()` — dispatches Print, PDF and Share.
- `preparePrintData()` — picks the visible page's rows; validates; sets the page count and timestamp.
- `buildPdfDocument()` — creates all pages.
- `drawPage(page, n)` — draws the header, grid, rows and footer.
- `drawCenterwithgivenwidth()` — centres text in a column.
- `writePdfToFile()` — writes the PDF to app storage.
- `getFileUri()` — returns the FileProvider URI.
- `createPdf()` — opens the PDF in a viewer.
- `sharePdf()` — shares the PDF.
- `printDocument()` — starts the print job with an inline adapter.
- `whiteNotificationBar()` — sets a light status bar (unused here).
- `PlaceholderFragment.newInstance/onCreateView/setUserVisibleHint/onContactSelected` (the last one is empty).
- `SectionsPagerAdapter.getItem/getCount`.

**Rules & edge cases.**
- The query uses the currently selected branch connection (`selbranchconnString`).
- The line list comes from local `FinanceDetails`, so only lines downloaded to this device appear.
- The static `mAdapter` always points to the most recently created page, which is often the neighbouring page, not the visible one. Search, if it were enabled, would filter the wrong page.
- Pages re-query on every swipe.
- Connections are never closed.
- Errors inside the helper are only printed and give an empty page.

## Daily Thittam for one line (legacy) — `singlefinancedailythittam_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_simplethittamlist.xml` (reused) + rows `allfinancedailythittamrow.xml`; menu `menuwithoutsearchandprintoptions` |
| **Opened from** | `selectbranchandfinance` when `requestcode` = 9023 (`requestcodes.dailythittam_requestcode`; JADX shows the literal as `constantvalues.stockuom_editrequestid`). That screen shows a "Thittam Date" picker and SHOW, then passes extras `passedfinanceid`, `LineName`, `fromdate`. **No drawer item or screen starts `selectbranchandfinance` with 9023 in this build, so this screen is unreachable.** It has been superseded by `dailythittamforallowedlines`. |
| **Opens** | — |
| **Data** | Remote `branchlevelReport_branchconnString`: `exec singleFinanceCashBook @IMEI='<device_id>',@ReportDate='<fromdate>', @FinId=<passedfinanceid>` (its own inner AsyncTask, same column mapping as the helper). |

**Purpose.** Single-line version of Daily Thittam for one chosen date.

**UI.**
- Title "Daily Thittam  (dd-MM-yy)".
- A label shows `LineName`.
- The header row comes from the Simple Thittam layout ("Particulars · Credit · Debit", 3 columns). It sits over 4-column rows (Ledger · Particulars · Debit · Credit), so the headers do not line up with the data.
- Progress dialog "Loading Details" / "Processing Step I".

**Menu / actions.**
- Direct Print, Show as PDF, Share and Refresh Ledgers are visible but not handled, so they do nothing.
- Search is hidden.
- Back collapses search first.
- Row tap does nothing.

**How it works.**
1. `onCreate` reads `passedfinanceid`, `LineName` and `fromdate`, sets the title, creates the adapter and starts the inner `ASYNCGetsingleFinanceCashBook`.
2. `doInBackground` connects via `branchlevelReport_branchconnString` and runs `singleFinanceCashBook`. It maps the same 13 columns as the shared helper, with `Debit`/`Credit` read via `getInt`.
3. `onPostExecute` hides the progress dialog and refreshes the list. There are no totals, printing or drill-down.

**Functions.**
- `onCreate()`
- `onCreateOptionsMenu()`, `onOptionsItemSelected()` (Home only)
- `onContactSelected()` (empty)
- `ASYNCGetsingleFinanceCashBook.doInBackground/onPostExecute/onPreExecute/onProgressUpdate`
- `whiteNotificationBar()`
- `onActivityResult()` (pass-through)

**Rules & edge cases.**
- `ErrorText` is captured but never shown.
- Amounts are read as int.
- The connection is never closed.

## Daily Thittam all finances (orphan) — `allfinancedailythittam`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), manifest label `allfinancedailythittam`, theme `AppTheme.NoActionBar`. A ViewPager holds static `PlaceholderFragment` pages, each with a static inner `ASYNCGetsingleFinanceCashBook` task. |
| **Layout(s)** | `activity_allfinancedailythittam.xml`; page `fragment_allfinancedailythittam.xml`; rows `allfinancedailythittamrow.xml` |
| **Opened from** | **Nothing.** No class starts it (no `const-class` reference in smali). It expects extra `fromdate`. |
| **Opens** | — |
| **Data** | Local SQLite `FinanceDetails` (`select FinId,FinMailingName,CashLedgerId,AdminMobileNos,LedgerName,LId,DefaultLoanMethodId`; page count from `select count(FinId) from FinanceDetails`). Remote `selbranchconnString`: `exec singleFinanceCashBook @IMEI,@ReportDate=<fromdate>,@FinId=<page FinId>`. |

**Purpose.** Older pager version of Daily Thittam, with one page per finance in local `FinanceDetails`.

**UI.**
- Toolbar title `R.string.dhinathittamtitle` ("Daily Thittam" / "தினத்திட்டம்"), replaced at runtime by "Daily Thittam  (dd-MM-yy)".
- Each page shows the finance's `FinMailingName`, a RecyclerView of rows, and a centred spinner (`listprogressbarid`).
- The FAB is hidden.

**Menu / actions.** None: `onCreateOptionsMenu` returns true without inflating anything. Row taps do nothing.

**How it works.**
1. `onCreate` reads `fromdate`, sets the title and loads `financelist` from local `FinanceDetails`. The pager count comes from `count(FinId)`.
2. Each page's `onCreateView` puts `financelist[n-1]` into the static `newdata`, sets the label, builds the static list and adapter, and runs the inner task.
3. The inner task connects via `selbranchconnString`, clears the static list, and runs `exec singleFinanceCashBook @IMEI='<device_id>',@ReportDate='<fromdate>', @FinId=<newdata.FinId>`. `onPostExecute` hides `ProgressBar1` and refreshes the static adapter.
4. `setUserVisibleHint` detaches and re-attaches the page, so it reloads every time it becomes visible.

**Functions.**
- `onCreate()` — loads the finance list.
- `onResumeFragments()`
- `PlaceholderFragment.newInstance/onCreateView/setUserVisibleHint` (detach and re-attach) / `onContactSelected` (empty)
- inner `ASYNCGetsingleFinanceCashBook` (clears and fills the static `allfinancethittamlist`; toggles `ProgressBar1`)
- `SectionsPagerAdapter.getItem/getCount`

**Rules & edge cases.**
- `newdata`, `allfinancethittamlist`, `mAdapter` and `ProgressBar1` are static and shared by all pages. Because the ViewPager pre-creates neighbouring pages, a page can show another line's rows or the wrong spinner.
- `progressBar = new ProgressDialog(getBaseContext())` is never used.
- Dead code: kept only for reference.

## Simple Thittam (period cash/bank flow) — `simplethittamlist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`. JADX could not decompile `drawPage`; it was read from smali. |
| **Layout(s)** | `activity_simplethittamlist.xml` + rows `simplethittam_row.xml`; menu `menuwithoutsearchandprintoptions` |
| **Opened from** | (1) Drawer › Reports › "Simple Thittam / திட்டம்" (`nav_simplethittam`) → `selectbranchandfinance` (`requestcode` 9006 = `requestcodes.simplethittam_requestcode`; JADX shows it as `constantvalues.accountsledgers_newledgerrequest`). There the user picks a branch and line, From/To dates, then SHOW. Extras passed: `passedfinanceid`, `LineName`, `fromdate`, `todate`. (2) `selectbranchandfinance` rc 304 (drawer "Varathittam Analysis") → choose a Sunday → "Thittam" button (`btnselectedweekthittam`), with `fromdate` = Sunday − 6 days and `todate` = Sunday. |
| **Opens** | `ledgerstatement_act` (three variants, see below); PDF viewer; share chooser; print dialog |
| **Data** | Remote `globalvars.branchlevelReport_branchconnString`: `tblLedgers` (read), `tblLedgerDetforSimpleThittam` (delete + insert), `tblledgerDet` (read; filled by proc `LedgerwiseBalance`), proc `SimpleThittam`. Global `globalvars.acctransactionslit_datalist`. On drill-down, sets `globalvars.selbranchconnString/selbranchdbusername/selbranchdbpassword` to the `branchlevelReport_*` values. Files: public `Documents/venusmfc/st__<ts>_<rand>.pdf` (Show), and `getExternalFilesDir(DOCUMENTS)/venusmfc/st__….pdf` (Share). |

**Purpose.** A simple cash-flow plan for one line over a date range:
- opening balance of all cash and bank ledgers;
- all cash and bank movements in the period, grouped by the server;
- net closing balance.

Tap a figure to see the underlying ledger statement.

**UI.**
- Title `ST:dd-MM-yy to dd-MM-yy`, with Up.
- A label shows `LineName`.
- Header "Particulars · Credit · Debit".
- Each row shows Particulars (15 sp) with the ledger name under it (12 sp), then two amount columns. The left one (`simplethittamrow_debitid`, under the "Credit" header) shows `debit`. The right one (`…creditid`, under "Debit") shows `credit`. Format `##,##,##0.00`; zero shows blank.
- Row colour depends on `ShowType`: 0 and 1000 red, 999 blue, 1001 dark green, 1002 maroon. Any other value sets only Particulars to black.

**Menu / actions.**
- Search is hidden: `searchView.setVisibility(GONE)`, and the item is invisible in the XML.
- "Direct Print", "Show as PDF", "Share".
- "Refresh Ledgers" is visible but unhandled, so it does nothing.
- Back collapses search first.
- Row tap opens a drill-down (step 9).

**How it works.**
1. `onCreate` reads the extras, sets the title and starts `ASYNCGetAbstractlist`. Progress dialog messages: "Loading Details", "Processing Step I", "Processing Step 1…n", "Processing Step VI".
2. **Ledger set:** `select LedgerId,Ledgername,GroupId from tblLedgers where FinId=<fin> and GroupId in (5,6)`. Group 5 = bank accounts, group 6 = cash-in-hand.
3. `delete from tblLedgerDetforSimpleThittam` empties the **whole** server work table.
4. **Closing balances (end of ToDate).** For each ledger:
   - run `exec LedgerwiseBalance @CCom=<fin>,@LedId=<id> ,@FDate='<to>' ,@TDate='<to>'`;
   - then `select sum(debit) as debit ,sum(Credit) as credit from tblledgerDet` into a `simplethittamopbalobj`;
   - net it: keep only the larger side, holding the difference;
   - add it to `closingbaldetailslist`.
5. **Opening balances and movements.** For each ledger:
   - run `exec LedgerwiseBalance … @FDate='<from>' ,@TDate='<to>'`;
   - `select debit,Credit from tblledgerDet where RowId=1` (row 1 = opening balance), netted, goes to `openingbaldetailslist`;
   - then `Insert into tblLedgerDetforSimpleThittam (SourceLedgerId,TransId,TransDate,TTId,Particulars,Debit,Credit,Narration,LedSNo,LedgerId) select <cash/bank id>,TransId,… from tblLedgerDet where RowId>1` copies every transaction of that cash or bank ledger in the period.
6. **Opening row.** The netted openings are summed and inserted as `Particulars = N'முன் இருப்பு'` ("opening balance"), with Debit = Σ net debit balances and Credit = Σ net credit balances. The object fields are swapped (field `credit` holds the server `debit`), and the insert swaps them back, so the work-table row keeps normal orientation.
7. `exec SimpleThittam @FromDate='<from>',@ToDate='<to>',@FinId=<fin>` returns rows (`ShowType, TypeId, TTId, Particulars, debit, credit, ledgerid, Ledgername`) that become the list. All grouping, subtotals and the closing row "நிகர இருப்பு" ("net balance") are produced by the procedure.
8. `onPostExecute` refreshes the list and toasts any exception message.
9. **Drill-downs** (`onContactSelected`):
   - **"முன் இருப்பு":** builds `globalvars.acctransactionslit_datalist` from the opening list, one row per cash or bank ledger (Particulars = ledger name; Debit/Credit cast to int; Finid = financeid). Starts `ledgerstatement_act` with `selectedledgername` = `R.string.openingbalance`, `ledgeridpassed` = −1, `fromdate`, `todate`, `reportfinid`, and `list`. That screen just lists the passed rows.
   - **"நிகர இருப்பு":** same, built from the closing list. It passes extra key `ledgername` = `R.string.Closingamount` ("Closing Balance" / "நிகர இருப்பு"), but `ledgerstatement_act` reads only `selectedledgername`, so the title shows "null(…)". Finid = `globalvars.finid`.
   - **Any other row:** switches the global selected-branch connection to the report branch, then opens `ledgerstatement_act` for the row's `ledgerid` (title = particulars) over the same period.
10. **Print and PDF.** Selecting a menu item sets `totalpages = ceil(rows/30)` and resets the row counter. `drawPage` draws A4 portrait:
    - "Page n" top right; title "Simple Thittam the Period from dd-MMM-yyyy to dd-MMM-yyyy";
    - columns Ledger Name · Particulars · Credit · Debit, with 20-pt rows;
    - `debit` (> 0) under "Credit" and `credit` (> 0) under "Debit", formatted `##,##,##,##0` (no decimals);
    - after the 30th row of a page it prints the footer "Printed @ null, null": `currentDate`/`currentTime` are never assigned, and a partial last page gets no footer.
    - **Show as PDF:** writes public `Documents/venusmfc/` (with mkdirs), then `ACTION_VIEW`.
    - **Share:** writes app-external Documents, then `ACTION_SEND application/pdf` + ClipData, chooser "Share PDF".
    - **Direct Print:** A4 portrait, job "FinSoft II Document", document "ST.pdf". With zero rows the print fails with "Page count is zero.".

**Functions.**
- `onCreate()`
- `onCreateOptionsMenu()`, `onOptionsItemSelected()` (pages, Print, PDF, Share)
- `onContactSelected()` (three drill-downs)
- `ASYNCGetAbstractlist.doInBackground/onPostExecute/onPreExecute/onProgressUpdate`
- `simplethittamopbalobj`: model with `ledgerid, ledgername, debit, credit, openingbalance` (the last is unused)
- `printDocument()`, `createPdf()`, `sharePdf()`
- `MyPrintDocumentAdapter.onLayout/onWrite/pageInRange`. JADX places a `finally` inside the loop; smali shows it wraps only the final write.
- `drawPage()`, `drawCenterwithgivenwidth()`
- `whiteNotificationBar()`, `onActivityResult()` (pass-through)

**Rules & edge cases.**
- Soft setting `divisiblevalueforthittam_16` (SoftSettings SSId 3, "Divisible Value for Thittam", default 20) is **not used here or anywhere else in the client**. It is only loaded and saved (`settings_act`, `syncresources`, `refreshorreloadbrancheslist`, `otpverificationact`, `navigationdrawactivity`). Any rounding by it must happen server-side, which is unclear from the APK.
- No NIP-break setting is used here.
- `tblLedgerDetforSimpleThittam` is wiped in full, and `tblledgerDet` is a fixed table name. Two devices running Simple Thittam at the same time would overwrite each other's work data.
- Drill-down amounts are truncated to int.
- The adapter filter upper-cases the query but compares it with lower-cased particulars, so search is broken (and hidden anyway).
- Colours are not reset for "other" `ShowType` values, so recycled rows can keep red or blue text.
- The row drill-down permanently changes the global selected-branch connection.

## Weekly Thittam sheet (வாரத்திட்டம்) — `varathittam`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity) with the app theme (`AppTheme`, Light.DarkActionBar → standard action bar), title "வாரத்திட்டம்". JADX garbled `doInBackground`; the flow below was verified in smali. |
| **Layout(s)** | `activity_varathittam.xml`: Spinner, "SHOW" and "SEND EMAIL" buttons, all `gone` and never wired; full-screen `pdfImageView`. A "Print Options" AlertDialog is built in code. |
| **Opened from** | Drawer › Reports › "Varathittam Analysis / வார திட்டம்" (`nav_varathittamanalysis`) → `selectbranchandfinance` rc 304. That screen offers a spinner of the Sundays in the last 175 days (newest first) and buttons "Collection Analysis / Thittam / Patti / Print". **"Print"** (`btnprintselectedweekpattiandthittam`) starts this screen with `passedfinanceid`, `LineName`, `fromdate` (Sunday − 6) and `todate` (Sunday, used as `thittamdate`). The hidden drawer item "Varathittam" (`nav_varathittam`, `visible=false`) starts it with only `requestcode`=9025, so `thittamdate` is null and `financeid` is 0. The server call then fails and `DateFormat.format(…, null)` in `drawPage` would most likely crash. |
| **Opens** | "Print Options" dialog → Android print / PDF viewer / share chooser |
| **Data** | Remote (`globalvars.selbranchconnString`), in order: procs `Rep24WeekCollection`, `UpdateCollectedAmount`, `select top 1 * from tempcollweeks order by WeekNo desc`, `GetThittamItems`, `ShowExpensesoftheWeek`, `ShowIncomesoftheWeek`, `PrepareNIPLiveStatus`, then `select … from tempCollWeeks`. Local SQLite (create if not exists + delete + insert): `ExpensesoftheWeek(LedgerName varchar, Amount varchar, LedgerAmount double)`, `IncomesoftheWeek(same)`, `tempCollWeeks(WeekNo Integer, WDate varchar, CollAmount double, PendingAmount double, ColWeekFromDate, ColWeekToDate, RSDate, REDate date)`. `FinanceDetails` through `defaulttables.getfinanceobj()` (for `FinMailingName`). Soft setting `softsettingsvalue.nipbreakforweeklythittam_14`. |

**Purpose.** Produces the one-page A4-landscape weekly thittam for a line, as of the chosen Sunday. It combines three blocks:
- the 24-week collection and outstanding analysis;
- the week's cash statement, ending in closing cash in hand;
- the live/NIP outstanding reconciliation, with account counts.

**UI.**
- A horizontal progress dialog (0–100, "Working, please wait ...") shows step messages.
- The screen then shows the preview ImageView, which is always blank (see Rules).

**Menu / actions.**
- There is no options menu.
- After loading, the AlertDialog **"Print Options"** offers **Print** (Android print, A4 landscape), **Show as PDF** and **Share** (step 5).
- The layout's Spinner and its SHOW / SEND EMAIL buttons are hidden and have no listeners.

**How it works.**
1. `onCreate`: `thittamdate = extras "todate"`, `financeid = extras "passedfinanceid"`, then `AsyncWeeklyThittam.execute()`.
2. `doInBackground` runs inside one outer try/catch. On any exception it only logs the stack trace, skips the remaining steps, and still draws the sheet with whatever was loaded (zeros elsewhere). No error is shown.
   1. Open SQLite and the jTDS connection. Progress "User Verification" 5 (no verification is actually done), then "Preparing Collection Weeks" 15.
   2. `exec Rep24WeekCollection @NipLimit=<nipbreakforweeklythittam_14>,@FinIdN=<fin>, @AreaNo=0, @RDate='<d>',@ColWeekToDate='<d>',@ColWeekFromDate='<d>'`. All three dates are the selected Sunday; `@AreaNo=0` means all collection areas. This fills the server table `tempCollWeeks` with week buckets.
   3. "Update Collected Amount" 25: `exec UpdateCollectedAmount @FinId=<fin>`.
   4. "Get Collection Data" 35: `select top 1 * from tempcollweeks order by WeekNo desc` sets `NipThakkalLoansStDate = RSDate` and `NipThakkalEndDate = REDate` from the highest week bucket.
   5. 45: `exec GetThittamItems @CurFinId=<fin>` returns one row with Tamil-named columns. These are the sheet's inputs:

      | Column | Meaning |
      |---|---|
      | `முன்கையிருப்பு` | opening cash in hand |
      | `கடன்வரவு` | loan collection |
      | `நிப்புவரவு` | NIP collection |
      | `ஆதாயவரவு` | interest/profit income |
      | `மகிமைவரவு` | magimai / document-charge income |
      | `எழுத்துக்கூலி` | writing fee |
      | `புதியகடன்` | new loans disbursed |
      | `முன்நடப்பு` | opening live outstanding |
      | `முன்நிப்பு` | opening NIP outstanding |

   6. "Get Expenses Data" 55: `exec ShowExpensesoftheWeek @FinId=<fin>`. Rows (`LedgerName, Amount, LedgerAmount`) are inserted into local `ExpensesoftheWeek` with string-concatenated SQL.
   7. "Get Income Data" 55: the same with `exec ShowIncomesoftheWeek @FinId=<fin>` into `IncomesoftheWeek`.
   8. "Get  NIP Live Status  " 70: `PrepareNIPLiveStatus @NIPCutOffDate='<NipThakkalEndDate>', @FinId=<fin>` (no `exec`, legal because it is the first statement of its batch) returns `NoOfNIPLoans` and `NoOfLiveLoans`.
   9. "Preparing Print  " 75+i: `select WeekNo,convert(varchar,WDate,105) as 'Date',CollAmount,PendingAmount,ColWeekFromDate,ColWeekToDate,RSDate,REDate from tempCollWeeks` is copied row by row into local `tempCollWeeks`.
3. `onPostExecute` hides the progress dialog, resets `totalpages`=1 and the row counter, sets the preview bitmap and shows Print Options.
4. **The sheet (`drawPage`).**
   - **Header:** "வாரத்திட்டம்" at the left, the date `dd-MM-yyyy` at the right, and `FinMailingName` centred at 20 pt.
   - **Body:** 9 pt, monospaced ("Courier New"), amounts `%10.2f`. Three side-by-side blocks share one header row: எண் · வாரம் · வசூல் · நிலுவை │ விபரம் · வரவு · பற்று │ விபரம் · வரவு · பற்று.
   - **Block A, 24-week analysis.**
     - One row per local `tempCollWeeks` row:
       - எண் (No.) = `WeekNo`, blank if ≤ 0;
       - வாரம் (Week) = `WDate`;
       - வசூல் (Collection) = `CollAmount`, blank if ≤ 0;
       - நிலுவை (Outstanding) = `PendingAmount`, blank if ≤ 0.
     - Footer rows:
       - **மொத்தம் (Total)** = ΣCollAmount and ΣPendingAmount.
       - **நிப்புதாக்கல் (NIP transfer)** = `PendingAmount` of the **last** row. This is normally the highest-`WeekNo` bucket, the loans crossing the NIP limit.
       - **நிகர நடப்பு பாக்கி (net live balance)** = ΣPendingAmount − நிப்புதாக்கல் (red box).
   - **Block B, weekly cash statement (விபரம் / வரவு receipts / பற்று payments).**
     - **Receipts:** முன்கையிருப்பு + கடன்வரவு + நிப்புவரவு + ஆதாயவரவு + மகிமைவரவு + எழுத்துக்கூலி.
     - **Payments:** புதியகடன், then each `ExpensesoftheWeek` ledger (`LedgerAmount`).
     - Each `IncomesoftheWeek` ledger is printed in the **receipts** column but **added to the payments total** (see Rules).
     - **Totals row:** Σpayments under பற்று, Σreceipts under வரவு.
     - **கையிருப்பு (closing cash in hand)** = Σreceipts − Σpayments (red box).
   - **Block C, outstanding reconciliation.**
     - **Live (நடப்பு):** debit side முன்நடப்பு + புதியகடன்; credit side கடன்வரவு + நிப்பு தாக்கல் (from Block A). Total row, then **நிகர நடப்பு பாக்கி** = (முன்நடப்பு + புதியகடன்) − (கடன்வரவு + நிப்புதாக்கல்).
     - **NIP (நிப்பு):** முன் நிப்பு + நிப்பு தாக்கல் = மொத்தம்; less நிப்பு வரவு. **நிகர நிப்பு பாக்கி** = (நிப்புதாக்கல் + முன்நிப்பு) − நிப்புவரவு.
     - **மொத்த பாக்கி (total outstanding, blue box)** = நிகர நடப்பு பாக்கி + நிகர நிப்பு பாக்கி.
     - **Counts:** நடப்பு புள்ளிகள் (live accounts) = `NoOfLiveLoans`; நிப்பு புள்ளிகள் (NIP accounts) = `NoOfNIPLoans`; மொத்த புள்ளிகள் (total accounts) = their sum, formatted `%4d`.
     - Block A's net live balance is computed from the week buckets, Block C's from ledger figures. Having both presumably serves as a cross-check.
5. **Outputs.**
   - **Print:** `printDocument()` sets pages = rows/38 + 1 and requests A4 landscape. Job "FinSoft II Document", document name `varathittam_<yyyy-mm-dd>.pdf`.
   - **Show as PDF:** `createPdf("VT_ddMMyyHHmmss")` creates one 842×595 page in public `Documents/venusmfc/` and opens it with `ACTION_VIEW`. The folder is not created if it is missing.
   - **Share:** `selectedweekthittamandpattisharePdf("vt_ddMMyyyyHHmmss")` saves to the same folder (with mkdirs) and sends `ACTION_SEND` with MIME `text/csv` (wrong type), chooser "Share".

**Functions.**
- `onCreate()`
- `AsyncWeeklyThittam.onPreExecute/doInBackground/onProgressUpdate/onPostExecute`, and `lambda$onPostExecute$0` (dispatches the dialog choice)
- `drawPage(page, n, canvas)` — draws the whole sheet
- `drawCenter()` — centres the title
- `createPdfPageAsBitmap()` — intended preview
- `printDocument()`, `createPdf()`, `selectedweekthittamandpattisharePdf()`
- `MyPrintDocumentAdapter.onStart` (opens SQLite) `/onLayout/onWrite/pageInRange`

**Rules & edge cases.**
- **NIP limit.**
  - `nipbreakforweeklythittam_14` is SoftSettings SSId 1, Settings item "14. NIP Break for Weekly Thittam" (2 digits, default 20). Buckets beyond this many weeks are treated as NIP.
  - Startup bug: `navigationdrawactivity` loads it from pref key `nipbreakformonthlythittam` (the monthly value). The key `nipbreakforweeklythittam` is written by settings and sync but never read. After an app restart, the weekly sheet therefore uses the *monthly* NIP break until settings are re-synced.
- **Apparent bug:** income ledgers from `ShowIncomesoftheWeek` are drawn under வரவு but summed into the பற்று total, so they *reduce* கையிருப்பு. Verified in smali: they are added to the `TotalPattruI` register.
- **Blank preview:** `drawPage` draws on `page.getCanvas()` and ignores the bitmap canvas, so `pdfImageView` is always white. The preview `PdfDocument` is never closed.
- Once Print Options is dismissed, it cannot be reopened (the buttons are hidden); the user must leave and come back.
- The row counter is not reset before Print or Show as PDF. The page-break test uses (pagenumber+1)×38, and every printed page redraws from row 1, so multi-page output would duplicate rows. Normally the sheet fits one page.
- **Wrong branch possible:** the sheet uses `selbranchconnString`, but `selectbranchandfinance` sets `branchlevelReport_*` for the branch picked there. In master or multi-branch setups the sheet may run against a different branch DB from the selected line.
- `GetThittamItems`, `ShowExpenses…` and `ShowIncomes…` receive no date. They must rely on server state left by `Rep24WeekCollection` in the shared table `tempCollWeeks`, so concurrent users can overwrite each other's data.
- A ledger name containing `'` breaks the SQLite insert and aborts the load.
- If `PrepareNIPLiveStatus` fails, the counts print as "null".

## Weekly Collection Analysis — `weeklycollectionanalysis_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_weeklycollanalysis.xml` (includes `customersearchlist.xml`); rows `weeklycollectionanalysis_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | `selectbranchandfinance` rc 304 (drawer "Varathittam Analysis / வார திட்டம்") → pick a Sunday → **"Collection Analysis"** (`btnsAnalysis`). Extras: `thittamdate` (the Sunday) and `financeid`. |
| **Opens** | `weeklycollectionpatti_dashboard`, with extras `loanweek` (the tapped `weeklycollectionanalysis_data`, Serializable), `financeid`, `weekno` |
| **Data** | Remote: `branchlevelReport_branchconnString` if `globalvars.ismasterdb && iscurrentuserhavemultibranchaccess`, otherwise `branchconnString`. Calls `exec Rep24WeekCollection` (same parameters as `varathittam`: NipLimit = `nipbreakforweeklythittam_14`, AreaNo 0, all three dates = Sunday), then `exec UpdateCollectedAmount @FinId=<fin>`, then `select * from tempCollWeeks`. |

**Purpose.** Lists the 24-week collection analysis behind the weekly thittam. Each row is a week bucket of the line's loans with the amount collected and the amount outstanding. Tapping a row opens that bucket's party-wise weekly patti.

**UI.**
- Title "Weekly Collection Analysis".
- The column headers are Tamil only: **எண்** (No.) · **வாரம்** (Week) · **வசூல்** (Collection) · **நிலுவை** (Outstanding).
- Each row shows `WeekNo`, `WDate` (`dd-MM-yyyy`), `CollAmount` and `PendingAmount`. The amounts are read with `getInt` but stored as float, so they display like "12500.0".
- The hidden fields `RSDate`/`REDate` and `ColWeekFromDate`/`ColWeekToDate` travel to the dashboard. Inferred meaning: RSDate/REDate are the loan-date range of the bucket (`varathittam` uses REDate as the NIP cut-off), and ColWeekFrom/To is the collection window.

**Menu / actions.**
- Search: the adapter's filter loop is empty, so any text empties the list and clearing it restores the list.
- Back collapses search first.
- Row tap → dashboard.

**How it works.** `AsyncWeeklyThittam` runs with progress dialog "Loading Details". On an exception it shows AlertDialog "Error Info..." with the message; OK closes the screen. On success the list is refreshed.

**Functions.**
- `onCreate()`
- `onCreateOptionsMenu()`, `onOptionsItemSelected()`
- `onContactSelected(row, pos)` → dashboard
- `AsyncWeeklyThittam.doInBackground/onPostExecute/onPreExecute/onProgressUpdate`, `lambda$onPostExecute$0` (OK → finish)
- `whiteNotificationBar()`
- back-press callback

**Rules & edge cases.**
- Rebuilds the same shared server table `tempCollWeeks` as `varathittam`, so concurrent runs can overwrite each other.
- The NIP break has the same startup-pref bug described under `varathittam`.
- The connection is never closed.

## Weekly patti dashboard (loader) — `weeklycollectionpatti_dashboard`
| | |
|---|---|
| **Type** | Plain `android.app.Activity`, manifest **`android:exported="true"`** (no intent filter), app theme. It has no XML layout: the content view is a `weeklycollectionpatti_dashboardlayout` built in code. |
| **Layout(s)** | — (programmatic view) |
| **Opened from** | `weeklycollectionanalysis_act` row tap (extras `loanweek`, `financeid`, `weekno`) |
| **Opens** | — |
| **Data** | Remote `branchlevelReport_branchconnString`, all through PreparedStatements:<br>• `EXEC WeeklyAnalysisstage1 ?,?,?,?,?` with (1) `RSDate`, (2) `REDate`, (3) `RSDate` again, (4) `ColWeekToDate`, (5) FinId; query timeout 3000 s. Returns `ColDateH`, `ColDate`.<br>• `EXEC PreparePattiIIWeekly` returns `PartyName, Subareaname, Loan, MCLoanId, Balance, Collected`.<br>• `EXEC WeeklypattiStageII` returns `MCLoanId, ColDate, Paid`.<br>Results go into static lists that are handed to the view class. |

**Purpose.** Loads the party-wise weekly patti (installment register) for the loans in the selected week bucket: each loan's amount, collected/pending, and the amount paid on each collection date.

**UI.** A progress dialog ("Preparing...") shows while loading. The whole window is then replaced by the grid view described in the next block. There is no toolbar code, so the title is the app label.

**Menu / actions.** None: no options menu, no print or share. Back closes the screen.

**How it works.**
1. `onCreate` reads the extras and builds `loanweekdate = "dd-MMM-yyyy\n<weekno>"` from `WDate`. It creates the three static lists, assigns them to the view's static fields, and runs `asyncthittamstage1`.
2. **Header list.** It starts with fixed headers "Loan" and "Collected". For each stage-1 row with a non-null `ColDateH` it adds a header, parsing `ColDateH` as an underscore-delimited string:
   - `header` = part[1].substring(0,5) (a date such as `dd-MM`);
   - `weekday` = part[2] first 3 letters, upper-cased;
   - `weekno` = int(part[3]);
   - `ColDate` is kept as well.
3. **Parties.** Rows from `PreparePattiIIWeekly` become parties (`ledgerid` is fixed to 1). Rows from `WeeklypattiStageII` become receipts.
4. `onPostExecute` → `setContentView(new weeklycollectionpatti_dashboardlayout(this))`, then dismisses the progress dialog.

**Functions.**
- `onCreate()`
- `asyncthittamstage1.onPreExecute/doInBackground/onProgressUpdate/onPostExecute`

**Rules & edge cases.**
- The progress message is always "Preparing...". The step texts, and the `ErrorText` sent through `publishProgress`, are ignored, so errors are never shown. A failure produces an empty grid with only the Loan/Collected headers.
- The second and third procedures take no parameters, so they read server work state left by stage 1, and concurrent users can overwrite each other.
- Parameter 3 repeats `RSDate`; `ColWeekFromDate` is never passed (possible bug).
- Because it is exported, any app can launch it. Without extras it crashes (NPE). With crafted extras it would run report queries using the in-process DB credentials.
- No print or share. The connection is never closed.

## Weekly patti grid view — `weeklycollectionpatti_dashboardlayout`
| | |
|---|---|
| **Type** | Custom dashboard View (`extends RelativeLayout`): a 4-pane table with frozen headers. Inner classes `MyHorizontalScrollView` and `MyScrollView` keep the panes scrolling together. |
| **Layout(s)** | Built in code. Pane A `tableloanweek` (corner); pane B `tablepattiperiod` inside `horizontalScrollViewB` (column headers); pane C `tableloanparties` inside `scrollViewC` (row headers); pane D `tablecollectiondetails` inside `horizontalScrollViewD`, inside `scrollViewD` (body). View ids 1–4 are set for the RelativeLayout rules. |
| **Opened from** | Constructed by `weeklycollectionpatti_dashboard.onPostExecute` |
| **Opens** | — |
| **Data** | Static fields `partieslist`, `pattidateheaderlist`, `receiptslist`, `loanweekdate` |

**Purpose.** Draws the weekly patti as a spreadsheet-like grid, with a frozen header row (dates) and a frozen first column (parties).

**UI.**
- **Corner (A):** the week date and week number, in white on DarkOrange (`chitdarkbackground`), minimum 200×150.
- **Column headers (B):** "Loan", then "Collected\nPending", then one column per collection date: `dd-MM\nDAY\n<weekno>`. White on DarkOrange, minimum 150×150, up to 5 lines.
- **Row headers (C):** `PartyName\n(Subarea)` for each loan, or "Total" when `PartyName` is null.
- **Body cells (D):** white, on light-grey (#CCCCCC) row backgrounds that show as grid lines. The corner table and the header scroller have a green background.

**Menu / actions.** None. Touch scrolling only; the party-cell click listener does nothing.

**How it works.**
1. **Body cells.** `taleRowForTableD()` filters the receipts by the party's `MCLoanId`. For each header it fills the cell as follows:
   - "Loan" → the party's `Loan`;
   - "Collected" → `Collected\nBalance`;
   - a date column → `Paid` for that `ColDate`;
   - otherwise blank.
   The loop was verified in smali. Loan and Collected are filled **only if the loan has at least one receipt row**, so loans with no receipts show blank cells.
2. **Sizing.** Header widths are measured into `headerCellsWidth[210]`, header heights are matched between panes A and B, and body row heights are matched between C and D.
3. **Scroll sync.** B and D scroll together horizontally; C and D scroll together vertically.
4. Tapping a party cell does nothing.

**Functions.**
- `initComponents()`, `setComponentsId()`, `setScrollViewAndHorizontalScrollViewTag()`, `addComponentToMainLayout()`
- `addTableRowToTableA/B()`, `componentATableRow()`, `componentBTableRow()`
- `generateTable_chitbiddingdetails()` — misnamed; builds body pane D
- `genrateTable_partiesdetails()` — builds pane C
- `tableRowForpartiestable()`, `taleRowForTableD()`
- `taleRowForBiddingTable()` — unused leftover from a chit-bidding grid
- `bodyTextView()`, `headerTextView()`
- `resizeHeaderHeight()`, `getTableRowHeaderCellWidth()`, `resizeBodyTableRowHeight()`, `matchLayoutHeight()`, `isTheHeighestLayout()`, `viewHeight()`, `viewWidth()`
- `MyHorizontalScrollView.onScrollChanged()`, `MyScrollView.onScrollChanged()`

**Rules & edge cases.**
- Headers are compared with `==` against the literals "Loan" and "Collected". This works only because both strings are interned literals.
- More than 209 header columns would throw `ArrayIndexOutOfBoundsException`.
- No totals are computed on the client; only the server's "Total" row, if any, is shown.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `allfinancedailuthittamadapter` | RecyclerView adapter (Filterable) + `ContactsAdapterListener` | `dailythittamforallowedlines`, `singlefinancedailythittam_act`, `allfinancedailythittam` | Row `allfinancedailythittamrow`: ledger name · particulars · `Debit` (only if > 0, plain integer) · `Credit` (only if > 0). Colours: `ShowId` 1 brown, 3 orange-red, 4 dark green, but always overwritten by the `IsNewLoan` chain (1 → `#DC190C`, 2 → dark green, anything else incl. −2 → black; verified in smali). Click → `onContactSelected(row)`; every host's handler is empty. Filter: ledger name contains the query (case-insensitive) OR particulars contains it (case-sensitive). |
| `simplethittam_adapter` | RecyclerView adapter (Filterable) | `simplethittamlist_act` | Row `simplethittam_row`: particulars with ledger name under it; `debit`/`credit` formatted `##,##,##0.00`, blank when 0. `ShowType` colours: 0/1000 red, 999 blue, 1001 dark green, 1002 maroon. Click → `onContactSelected` (drill-down). Filter is broken: it upper-cases the query and compares with lower-cased particulars. |
| `weeklycollectionanalysis_adapter` | RecyclerView adapter (Filterable) | `weeklycollectionanalysis_act` | Row `weeklycollectionanalysis_row`: `WeekNo`, `WDate` (`dd-MM-yyyy`), `CollAmount`, `PendingAmount` (shown with `String.valueOf(float)`). Click → `onContactSelected(row, position)`. The filter loop body is empty (smali-verified), so any query gives an empty list. |
| `ASYNCGetsingleFinanceCashBook` | Reusable `AsyncTask<Object,String,ArrayList<allfinancedailythittamdata>>` | `dailythittamforallowedlines` (anonymous subclass supplies `onPostExecute`) | `doInBackground(FinId, reportDate)`: connects via `selbranchconnString`, runs `exec singleFinanceCashBook @IMEI='<device_id>',@ReportDate='<date>', @FinId=<id>` and maps 13 columns. An error gives an empty list (stack trace only). The constructor ignores its `Context`. The connection is not closed. |
| `allfinancedailythittamdata` | Data model | Daily Thittam screens and adapter | `SNo, ShowId, TransId, FinId, TransType` (Integer); `LedgerId` (String); `LedgerName, Particulars, TransDate, Narration` (String); `Debit, Credit, isnewloan` (int). |
| `weeklycollectionanalysis_data` | Data model (Serializable) | `weeklycollectionanalysis_act` → dashboard extra `loanweek` | `sno, WeekNo` (int); `CollAmount, PendingAmount` (float); `WDate, ColWeekFromDate, ColWeekToDate, RSDate, REDate` (`java.sql.Date`). |
| `patticollectionparties_data` | Data model (Serializable) | dashboard and grid `partieslist` | `ledgerid` (always 1), `ledgername` (= PartyName), `subareaname`, `loan`, `mcloanid`, `Balance`, `Collected` (int); `LoanDate` (unused). |
| `simplethittam_data` | Data model | `simplethittamlist_act` and adapter | `ShowType` (row-type/colour code), `TypeId`, `TTId` (Double), `Particulars`, `Debit`, `Credit` (double), `ledgerid`, `ledgername`. |
| `patticollobj` | Data model | dashboard `receiptslist` | Used: `MCLoanId`, `ColDate`, `Paid`. Unused: `Loan`, `Collected`, `Balance`. |
| `pattiheaderdatesobj` | Data model (Serializable) | dashboard and grid header list | `header` ("Loan", "Collected" or `dd-MM`), `weekday` (3 letters), `weekno`, `ColDate`. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_allfinancedailythittam.xml` | `allfinancedailythittam` | CoordinatorLayout `#main`; AppBar + Toolbar `#toolbar` (title `@string/dhinathittamtitle`, "Daily Thittam" / "தினத்திட்டம்"); ViewPager `#container`; FAB `#fab` (gone, email icon). |
| `activity_dailythittamforallowedlines.xml` | `dailythittamforallowedlines` | CoordinatorLayout `#main`; Toolbar `#toolbar` (title `@string/app_name`, "FinSoft II" / "FinSoft"; replaced at runtime by `திட்டம்-<date>`); ViewPager `#container`. |
| `activity_simplethittamlist.xml` | `simplethittamlist_act`, `singlefinancedailythittam_act` | Toolbar (`MyToolbarwithlongtitle`); TextView `#simplethittamfinancenameid` (line name); header row: `#simplethittamrow_particularsid` "Particulars" (weight 1.5), `#simplethittamrow_debitid` "Credit" (0.8), `#simplethittamrow_creditid` "Debit" (0.8); RecyclerView `#recycler_view`. |
| `activity_varathittam.xml` | `varathittam` | ConstraintLayout `#main`; Spinner `#spinner`, Button `#btnPrepareandPrint` "SHOW" and Button `#btnsendmaiiid` "SEND EMAIL" (all gone, unwired); full-screen ImageView `#pdfImageView` (fitCenter) for the preview. |
| `activity_weeklycollanalysis.xml` | `weeklycollectionanalysis_act` | Toolbar; bold 14 sp header row (Tamil only): `#weeklyanalysis_weeknoid` "எண்" (60 dp), `#weeklyanalysis_weekdateid` "வாரம்", `#weeklyanalysis_collectionid` "வசூல்", `#weeklyanalysis_balanceid` "நிலுவை"; `include customersearchlist` (RecyclerView `#recycler_view`). |
| `allfinancedailythittamrow.xml` | `allfinancedailuthittamadapter` | Clickable 13 sp row: `#afdailythittamledgernameid` "Ledger" (50 dp), `#afdailythittamparticularsid` "Particulars" (weight 1), `#afdailythittamdebitid` "Debit" (0.5, end), `#afdailythittamcreditid` "Credit" (0.5, end). |
| `fragment_allfinancedailythittam.xml` | `allfinancedailythittam.PlaceholderFragment` | ConstraintLayout; TextView `#section_label` "Finance Name" (blue; replaced by `FinMailingName`); `include customersearchlist`; centred ProgressBar `#listprogressbarid` (gone). |
| `fragment_allowedfinancedailythittam.xml` | `dailythittamforallowedlines.PlaceholderFragment` | `#loading_label` "Loading......." (gone); ProgressBar `#progress_bar` (indeterminate, horizontal); `#section_label` (line name, bold white); header row: Ledger · Particulars · "Credit" (`#simplethittamrow_debitid`) · "Debit" (`#simplethittamrow_creditid`); RecyclerView `#recycler_view` (bottom margin 50 sp). |
| `simplethittam_row.xml` | `simplethittam_adapter` | 50 sp clickable row: a vertical block (weight 1.7) with `#simplethittamrow_particularsid` (15 sp) over `#simplethittamrow_ledgernameid` (12 sp); `#simplethittamrow_debitid` (0.8, end); `#simplethittamrow_creditid` (0.8). |
| `weeklycollectionanalysis_row.xml` | `weeklycollectionanalysis_adapter` | 30 dp row, bold 14 sp: `#weeklyanalysis_weeknoid` (60 dp), `#weeklyanalysis_weekdateid`, `#weeklyanalysis_collectionid` (end), `#weeklyanalysis_balanceid` (end). |

---
[← brain.md](../../brain.md) · Module 8.12 of 20 · Previous: [8.11 Daily Patti (outstanding) reports and meeting report](11-daily-patti-outstanding-reports-meeting-report.md) · Next: [8.13 Accounts transactions, cash/bank book, expenses & incomes, hand loans and ledger statements](13-accounts-transactions-cash-bank-book-expenses.md)
