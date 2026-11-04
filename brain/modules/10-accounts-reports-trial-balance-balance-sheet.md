# 8.10 Accounts reports: trial balance, balance sheet, P&L, month-wise P&L

[← brain.md](../../brain.md) · Module 8.10 of 20 · Previous: [8.9 Collection areas, maps, GPS staff tracking and attendance](09-collection-areas-maps-gps-staff-tracking.md) · Next: [8.11 Daily Patti (outstanding) reports and meeting report](11-daily-patti-outstanding-reports-meeting-report.md)

The drawer's Accounts Reports group produces three statements from a single screen, `tbbspl_list` (TB = trial balance, BS = balance sheet, PL = profit and loss), plus a month-by-month P&L pager. Everything is computed live on SQL Server (selected-branch connection, `selbranchconnString`) by stored procedures, for one or more lines (finances) chosen in `accountsreports_parameters`.
- **Balance sheet:** ledger components (capital, `BSComps` groups, cash, bank) plus the loan outstanding register ("patti"), split into Nadappu (live) and Nippu (overdue) loans.
- **P&L:** income and expense ledgers (via the `ComparedPL` scratch table) plus "extra collection" (கூடுதல் வசூல்) earned on loans.
- **Trial balance:** the union of both, closed by a "Difference" row.

Rows drill down to component particulars, loan collection history or ledger statements. Reports can be printed through the Android print framework or exported and shared as A4 PDFs.

## Accounts report parameters — `accountsreports_parameters`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default theme (action bar) |
| **Layout(s)** | `activity_partyledgers_parameters.xml` (shared with `partyledgers_parameters`); rows `financelist_withselectablecheckbox_row.xml` (via `financelist_selectable_adabpter`) |
| **Opened from** | Drawer Accounts Reports: `nav_Balancesheet` "Balancesheet" (`requestcode` 6018 `accountsreports_balancesheet`), `nav_TrailBalance` "Trail Balance" (6019 `accountsreports_trailbalance`), `nav_ProfitandLossStatements` "Profit and Loss Statements" (6020), `nav_MonthwisePanL` "Monthwise P and L" (6021) |
| **Opens** | `spinnersearch_list` (`title` "Financial Year", `searchlist`, rc 7022 `financialyear_requestcode`); DatePickerDialog; `tbbspl_list` (6018/6019/6020); `monthwisepandl` (6021) |
| **Data** | SQLite `FinanceDetails` (`select FinId, FinMailingName from FinanceDetails`) · hard-coded FY list `defaulttables.fetchfinancialyears()` · sets globals `accyearfromdate`/`accyeartodate` from soft setting `currentfinancialyearid_20` · calls `StrictMode.setVmPolicy(new VmPolicy.Builder().build())` (clears the VM policy process-wide) · no prefs |

**Purpose.** Choose the lines (finances) to consolidate, the financial year, and the report date (not asked for month-wise P&L).

**UI.**
- Title: "Balancesheet", "Trailbalance", "Profit and Loss Statement" or "Monthwise P&L".
- "Select Finances" label with a RecyclerView of checkboxes (FinMailingName, multi-select).
- Financial-year TextView `partyledgersparameters_selectfinancialyear`: shows "Financial Year :" in the layout, replaced at runtime by "dd-MM-yyyy-dd-MM-yyyy"; tap to pick another year.
- "Select Report Date" and "Date"/"தேதி" with EditText `partyledgersparameters_fromdateid` (defaults to today, dd-MM-yyyy). Both are hidden for 6021.
- The party-selection layout is always hidden. Button "SHOW". The ProgressBar is unused.

**How it works.**
1. `onCreate`:
   - clears the StrictMode VM policy; this is what lets the later `file://` PDF intents work;
   - loads finances from SQLite;
   - sets `globalvars.accyearfromdate/accyeartodate` to the start and end of FY id `softsettingsvalue.currentfinancialyearid_20` and displays them;
   - reads `ttid` (0 from the drawer) and `requestcode`.
2. Tapping the financial year opens `fetchfinancialyearsforselection()` (FYDesc "2011-2012" … "2029-2030"). The returned `valuemember` updates only the local `fromdate`/`todate`; the globals are left unchanged.
3. Tapping the date field opens a DatePicker and sets `reportdate`.
4. SHOW:
   - if no finance is ticked → `showincompletedataalert()`: "Incomplete data..." / "Finance not Selected";
   - otherwise builds the comma-joined `selectecdfinanceids` (e.g. "1,3") and `selectedfinancenames`;
   - 6018/6019/6020 → `tbbspl_list` with `requestcode`, `fromdate` (FY start), `todate` (FY end), `reportdate`, `ttid`, the ids and the names;
   - 6021 → `monthwisepandl` with `fromdate`, `todate`, `requestcode`, `ttid`, the ids and the names.

**Functions.**
- `onCreate()`: views, finances, FY defaults, title per report.
- `lambda$onCreate$1()`: FY picker. `lambda$onCreate$3()`/`$2()`: DatePicker and `onDateSet`. `lambda$onCreate$4()`: SHOW validation and routing.
- `onActivityResult()`: rc 7022 / result 1 → FY dates.
- `showincompletedataalert()`, `whiteNotificationBar()`.
- `fetchfinancelist()`: SQLite read. `onfinancechecked()`: stores the selected flag on `financemaster_data`.

**Rules & edge cases.**
- The report date is not checked against the chosen FY.
- The FY choice affects only `tbbspl_list`'s FY label and the extra-collection start date. Month-wise P&L ignores it and always uses the globals (current FY). The printed PERIOD and the drill-down dates also use the globals.
- **Bug:** in the hard-coded list, "2029-2030" has FYId 29 (should be 19). Selecting it calls `finyears.get(28)` on a 19-item list → IndexOutOfBoundsException crash.
- `getedforfinyear(0)` has no guard for 0 and would crash if setting 20 were 0. Normally `navigationdrawactivity` initialises it from pref `currentfinancialyearid` (default 1).
- The finance list is every line in the local `FinanceDetails`; there is no search box. The branch that checks 6021 inside the 6018–6020 block is dead.

## Balance Sheet / Trial Balance / P&L Statement — `tbbspl_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` (own Toolbar); JADX partial-decompile (`drawPage` checked in smali) |
| **Layout(s)** | `activity_tbbspl.xml`; rows `tbbspl_row.xml` (via `tbbspl_adapter`); menu `menuwithsearchandprintoptions.xml` |
| **Opened from** | `accountsreports_parameters` SHOW (`requestcode` 6018 BS / 6019 TB / 6020 P&L, plus `fromdate`, `todate`, `reportdate`, `ttid`, `selectecdfinanceids`, `selectedfinancenames`). `partyledgers_parameters` also contains 6018–6020 branches, but it is only ever launched with 7013/7014/7027, and those branches omit `reportdate`. |
| **Opens** | `tbbspl_particulars` (component row; extras `details`, `cdesc`, `fromdate`, `todate`, `reportdate`; rc 100); `ledgerstatement_act` (P&L ledger row; rc 100); system print dialog; PDF viewer (ACTION_VIEW) or share chooser |
| **Data** | SQL Server via `selbranchconnString`: procs `BSAPP_GetBalanceSheetComponentParticulars`, `APPPattiProc_forBalancesheet`, `APPBS_GetCashBalance`, `APPBS_GetBankBalance`, `APPListPLIncomes`, `APPPattiProc_GetExtraCollection`, `APP_Transactionslist`, and `APPBS_GetBalancefortheLedger` (dead code); tables `BSComps`, `ComparedPL` · globals `finid`, `accyearfromdate/accyeartodate`, `acctransactionslit_datalist` · soft setting `allowdecimalinamounts` (adapter) · output file `/sdcard/mlfinsoftversion/anarep<millis>.pdf` |

**Purpose.** Shows the consolidated balance sheet, trial balance or P&L of the selected lines as on the report date, with drill-down and print/PDF/share.

**UI.** Toolbar title "BALANCE SHEET", "TRAILBALANCE" or "P & L STATEMENT". Three toolbar TextViews: the finance names, "RD:dd-MM-yyyy" (report date) and "FY:dd-MM-yyyy-dd-MM-yyyy". RecyclerView rows show particulars, Cr and Dr; zero or null amounts are blank, and amounts have 2 decimals only when `allowdecimalinamounts`. ProgressBar while loading.

**Menu / actions.**
- Search: keeps rows whose `ComponentDesc` contains the upper-cased query, so in practice only upper-case text matches. Back collapses the search view first.
- "Direct Print", "Show as PDF", "Share".
- Row tap: drill-down (step 5).

**How it works.**
1. `onCreate` reads the extras, sets the title and `ReportHeader` = "<TITLE>-<finance names>", and starts `ASYNCGetAccountReportDetails`. Errors show the dialog "Error Info..." with the exception message; OK closes the screen.
2. Balance-sheet part (6018 and 6019). The report is built from abstract rows (component totals, drill level 1) and detail rows (per ledger or loan). Abstract rows, in order:

| Row (ComponentId) | Source | Shown as |
|---|---|---|
| Capital Accounts (2) | `exec BSAPP_GetBalanceSheetComponentParticulars @ComponentId=2, @SelectedFinanceIds='<ids>', @RDate='<reportdate>'` → LedgerName, LedgerId, Cr, dr (ledgers with both zero are skipped) | gross ΣCr and gross ΣDr, both shown |
| நடப்பு கணக்கு, Nadappu live-loan OS (9) | `exec APPPattiProc_forBalancesheet @SelectedFinanceIds='<ids>', @ReportDate='<reportdate>'` → LedgerName, LedgerId, MCLoanId, Balance, IsNip = 0 | Dr = Σ Balance (row only if > 0) |
| நிப்பு கணக்கு, Nippu overdue-loan OS (10) | same procedure, rows with IsNip = 1 | Dr = Σ Balance (row only if > 0) |
| `BSComps` groups 12, 13, 15, 18, 19, 20 | `select ComponentId,ComponentDesc from BSComps where ComponentId in (12,13,20,15,18,19,20)`, then the component procedure for each id (descriptions come from the server table) | net: Cr − Dr on the Cr side, or Dr − Cr on the Dr side (row only if any amount > 0) |
| Cash in Hand (29) | `exec APPBS_GetCashBalance @SelectedFinanceIds, @RDate` → LedgerName, LedgerId, Value | Dr |
| Cash in Bank (30) | `exec APPBS_GetBankBalance @SelectedFinanceIds, @RDate` → LedgerName, LedgerId, Value | Dr |

3. P&L part (6020 and 6019):
   - `exec APPListPLIncomes @SelectedFinanceIds, @RDate` fills the scratch table. Only the report date is passed; the period logic lives inside the procedure.
   - Then `select … LedgerId as ComponentId, LedgerName as ComponentDesc, case when ΣCAmount > ΣDAmount then ΣCAmount − ΣDAmount else 0 end as Cr, case when ΣDAmount >= ΣCAmount then ΣDAmount − ΣCAmount else 0 end as Dr from ComparedPL where CAmount<>0 or DAmount<>0 group by LedgerId, LedgerName` gives one row per income or expense ledger (income on Cr, expense on Dr; drill level 2).
   - Then `exec APPPattiProc_GetExtraCollection @SelectedFinanceIds, @FromColDate=<FY start>, @ToColDate=<reportdate>` gives per-loan `ExtraCollection`, totalled as "கூடுதல் வசூல்" (extra collection, ComponentId 101) on the Cr side, row only if > 0.
4. Closing rows ("Total" is always ComponentId −2, with the same figure on both sides):
   - **P&L:** diff = ΣDr − ΣCr. If diff ≥ 0 → "Net Loss" with Cr = diff, Total = ΣDr. Otherwise → "Net Profit" with Dr = |diff|, Total = ΣCr.
   - **BS:** diff = ΣCr − ΣDr. If diff ≥ 0 → "Net Loss" on the Dr side, Total = ΣCr. Otherwise → "Net Profit" on the Cr side, Total = ΣCr + |diff|.
   - **TB:** diff = ΣCr − ΣDr. If diff ≥ 0 → "Difference" (ComponentId 0) on the Dr side, but Total = ΣDr (**bug**: it should be ΣCr). Otherwise → "Difference" on the Cr side, Total = ΣDr.
   - ΣCr and ΣDr accumulate gross component sums (Capital and `BSComps` on both sides). So when a `BSComps` component has balances on both sides, the Total exceeds the sum of the displayed, netted rows.
5. Drill-down (`onTransactionselected`):
   - Component rows (level 1: Capital, Nadappu, Nippu, BSComps, Cash, Bank, Extra collection) → `tbbspl_particulars` with that component's detail rows.
   - P&L ledger rows (level 2) → `ASYNCGetLedgerStatement` runs `exec APP_Transactionslist @FinId=<globalvars.finid>,@LedId=<id>,@FromDate='<globalvars.accyearfromdate>',@ToDate='<reportdate>'` into `globalvars.acctransactionslit_datalist`, then opens `ledgerstatement_act` with `ledgername`, `fromdate`, `todate` = reportdate, `reportfinid` = `globalvars.finid` and `receivedledgerid`.
   - Net, Total and Difference rows do nothing.
6. Print/PDF. `onOptionsItemSelected` first builds `listtobeprinted`: each abstract row (printing level 1), followed by its detail rows (level 2). Components 9 and 10 are the exception; their per-loan rows are left out. `totalpages` = ceil(rows / 30). `drawPage()` then lays out each A4 page (595 × 842 pt):
   - "Page N" top right; the toolbar title centred (12 pt); "FINANCE : <names>" on the left; "PERIOD: <accyearfromdate>-<accyeartodate>" on the right;
   - a boxed header "Particulars / Credit / Debit" (column edges at x 36, 406, 481, 545);
   - level-1 rows: component name in 10 pt, amount in the Credit or Debit column;
   - level-2 rows: ledger name in 8 pt indented 20 pt, amount right-aligned at x ≈ 333 inside the particulars column;
   - amounts formatted "##,##,##,##0" (no decimals); 30 rows per page;
   - footer "Printed @ dd-MM-yyyy, HH:mm:ss" only on pages that fill all 30 rows (confirmed in smali).

   The three actions:
   - **Direct Print:** `PrintManager.print("<app_name> Document", MyPrintDocumentAdapter, A4 portrait)`, document name "<ReportHeader>.pdf". The adapter is the standard pattern: `onLayout` reports the page count, `onWrite` draws the requested pages and then writes (JADX's `finally` placement is an artefact).
   - **Show as PDF:** writes `/sdcard/mlfinsoftversion/anarep<millis>.pdf` and opens ACTION_VIEW `application/pdf` with a `file://` URI. The flags are replaced by 0x40000000 (FLAG_ACTIVITY_NO_HISTORY), which drops the read grant.
   - **Share:** writes the same kind of file and sends ACTION_SEND with `EXTRA_STREAM` through the chooser "Share Image"; the toast "Sharing tools have not been installed." appears if no target exists.

**Functions.**
- Screen: `onCreate()`; back-press callback (collapses search); `onCreateOptionsMenu()`; `onOptionsItemSelected()` (builds the print list, then print/PDF/share); `whiteNotificationBar()`; `onActivityResult()` (empty: nothing refreshes after a drill-down).
- `onTransactionselected()`: drill-down router.
- `ASYNCGetAccountReportDetails.doInBackground()/onPreExecute()/onPostExecute()`: builds all three statements.
- `ASYNCGetLedgerStatement`: prefetch, then opens the ledger statement.
- `ASYNCGetselectedLedgerAmount`: **never instantiated**. It would re-read one ledger with `APPBS_GetBalancefortheLedger` and recompute the component, Net and Total rows.
- Output: `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.onLayout()/onWrite()/pageInRange()`, `drawPage()`, `drawCenterwithgivenwidth()`.

**Rules & edge cases.**
- **Drill-down to the ledger statement is mis-wired.** `ledgerstatement_act` reads the extras `selectedledgername` and `ledgeridpassed`, but this screen passes `ledgername` and `receivedledgerid`. The statement therefore opens for ledger 0 with the title "null(…)" and runs its own query; it never uses the prefetched `globalvars.acctransactionslit_datalist`.
- The drill-down always uses `globalvars.finid` (the current line), even for multi-line reports.
- `drawPage()` overwrites `reportdate` with the `fromdate` extra. A P&L drill-down made after printing therefore uses the FY start as its "to" date.
- The printed PERIOD comes from the globals (current FY), not from the FY chosen on the parameters screen.
- P&L rows reuse LedgerId as ComponentId. A ledger whose id equals a component id (2, 9, 10, 12–20, 29, 30, 101) would pull in that component's detail rows when printing or drilling down.
- Pages are filled from a running row counter, so printing only a subset of pages prints the wrong rows.
- The PDF goes to the external-storage root. With targetSdk 37, WRITE_EXTERNAL_STORAGE capped at API 32 and no MANAGE_EXTERNAL_STORAGE, the write probably fails on Android 11+; the IOException is only logged, and the viewer is then opened on a missing file.
- SQL is built by string concatenation; the ids and dates come from the pickers.

## Component particulars (drill-down) — `tbbspl_particulars`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` (own Toolbar) |
| **Layout(s)** | `activity_tbbspl_particulars.xml`; rows `tbbspl_particulars_row.xml` (via `tbbspl_particulars_adapter`); menu `menuonlywithsearchoption.xml` |
| **Opened from** | `tbbspl_list` component row (extras `details` = `ArrayList<tbbspl_data>`, `cdesc`, `fromdate`, `todate`, `reportdate`; rc 100) |
| **Opens** | `selectedloancollectiondetails` (loan rows: `MCLoanId`, `PartyName`, `Remarks` = "", `Tag` = ""); `ledgerstatement_act` (other rows; rc 100) |
| **Data** | SQL Server `exec APP_Transactionslist @FinId=<globalvars.finid>,@LedId=<id>,@FromDate='<FY start>',@ToDate='<reportdate>'` → `globalvars.acctransactionslit_datalist` (via `selbranchconnString`) |

**Purpose.** Lists the ledgers or loans behind one statement line, for example every bank ledger, or every live loan with its outstanding balance.

**UI.** Toolbar title = component description (`cdesc`), with an up arrow. Rows show name, Debit and Credit (blank when 0 or null). Search filters by name.

**How it works.**
1. Rows come from the `details` extra; `received_componentid` is the first row's component.
2. Row tap:
   - component 9 (Nadappu), 10 (Nippu) or 101 (extra collection) → `selectedloancollectiondetails`, the loan's collection history;
   - any other row → `ASYNCGetLedgerStatement`, then `ledgerstatement_act` with `ledgername`, `fromdate`/`todate` = `globalvars.accyearfromdate/accyeartodate`, `receivedledgerid` and `reportfinid` = `globalvars.finid`.
3. `onActivityResult` (rc 100, result 1) replaces that ledger's Cr/Dr with the `resultamountCr`/`resultamountDr` returned by the statement, or removes the row when both are 0. It also reads `related_ledger*` extras (unused). The parent `tbbspl_list` totals are not updated.

**Functions.** `onCreate()`, back-press callback, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, `whiteNotificationBar()`, `onTransactionselected()`, `ASYNCGetLedgerStatement.doInBackground()/onPostExecute()`, `onActivityResult()`.

**Rules & edge cases.** The same extra-key mismatch with `ledgerstatement_act` applies (it opens ledger 0). Only the current line's transactions are covered.

## Month-wise P&L pager — `monthwisepandl`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), manifest label "PROFIT AND LOSS STATEMENT", default theme (its action bar hosts the fragment menu) |
| **Layout(s)** | `monthwisepl_home.xml` |
| **Opened from** | `accountsreports_parameters` SHOW with 6021 (`selectecdfinanceids`, `selectedfinancenames`, `fromdate`, `todate`, `requestcode`, `ttid`) |
| **Opens** | 12 × `monthlypandl_fragment` pages |
| **Data** | globals `accyearfromdate`, `accyeartodate`; `defaulttables.fetchaccountyearmonth()`; no DB access of its own |

**Purpose.** A swipeable P&L, one page per month from April to March of the current financial year.

**How it works.**
1. Reads the extras. The dates go into `reportdate`/`interestcollectionuptodate` and are never used.
2. `ImagePagerAdapter` (FragmentPagerAdapter, 12 fixed fragment instances): pages 0–8 are April–December of the `accyearfromdate` year, pages 9–11 are January–March of the `accyeartodate` year. Each fragment receives these arguments:
   - `monthname`, e.g. "Apr-2026" (`GetMonthName`);
   - `fromdate`/`todate`, the month bounds as "yyyy-MM-dd". September is written non-padded as "yyyy-9-01"/"yyyy-9-30", which `java.sql.Date.valueOf` accepts. The February end date is computed with Calendar, so leap years are handled;
   - `selectecdfinanceids`.
3. Opens on the page of the current calendar month (`fetchaccountyearmonth`: April → 0 … March → 11), or on the saved `STATE_POSITION`.
4. The `PagerTitleStrip` is hidden. `getPageTitle()` would return `GetMonthName(position+1)` ("Jan" for the April page), which is wrong but never visible. The activity also tries to colour the parent of the legacy window title red (0xFFFF0000), if that view exists.

**Functions.** `onCreate()`, `onSaveInstanceState()` (saves the pager position), `ImagePagerAdapter()` (creates the 12 fragments), `getCount()` (12), `getItem()`, `getPageTitle()`.

**Rules & edge cases.** The FY chosen on the parameters screen is ignored: the months always come from the globals, i.e. the current FY from soft setting 20. The selected finance names are not shown anywhere.

## Monthly P&L page — `monthlypandl_fragment`
| | |
|---|---|
| **Type** | Fragment (extends `partnerwiseperformanceandloans_BaseFragment` → `BaseFragment`, which enables the options menu) |
| **Layout(s)** | `fr_monthlypandl.xml`; rows `tbbspl_row.xml` (via `tbbspl_adapter`); menu `partnerwiseperformance_menu.xml` (inflated by `BaseFragment`) |
| **Opened from** | `monthwisepandl` pager (arguments `monthname`, `fromdate`, `todate`, `selectecdfinanceids`) |
| **Opens** | `ledgerstatement_act` (rc 100); print dialog, PDF viewer or share chooser |
| **Data** | SQL Server via `selbranchconnString`: `exec [MonthlyPLIncomes] @SelectedFinanceIds='<ids>', @FDate='<month start>', @RDate='<month end>'`, then `select … from ComparedPL group by LedgerId,LedgerName` (the same Cr/Dr netting as `tbbspl_list`, but without the non-zero filter); `exec APP_Transactionslist @FinId=<selected id>,…` · output file `/sdcard/mlfinsoftversion/Monthly P and L -<Mon-yyyy>.pdf` |

**Purpose.** The P&L for one calendar month.

**UI.** Title TextView `fragmenttitleid` showing the month name. List rows show particulars, Cr and Dr. ProgressBar. Overflow menu "Show as PDF" and "Share"; "Direct Print" is declared `visible=false`.

**How it works.**
1. `ASYNCGetMonthlypandl` builds one row per ledger. It then adds "Net Loss" (Cr = ΣDr − ΣCr when that is ≥ 0) or "Net Profit" (Dr = ΣCr − ΣDr), followed by "Total" (both sides = ΣDr for a loss, ΣCr for a profit). The summary rows use ComponentId 0. Errors are only logged and leave the page empty.
2. Tapping a ledger row (ComponentId > 0):
   - exactly one finance selected → `ASYNCGetLedgerStatement` (`APP_Transactionslist` with that FinId and the month's dates), then `ledgerstatement_act` with `ledgername`, `fromdate`, `todate`, `receivedledgerid` and `reportfinid` = `globalvars.finid`;
   - otherwise → toast "Multiple Finance Report, cannot show details".
3. `onActivityResult` updates or removes the ledger row from `resultamountCr/Dr` and recomputes the totals. It then looks for the Net and Total rows by ComponentId −1/−2, which this fragment never uses, so the summary rows are not recalculated.
4. Menu. `ReportHeader` = "Monthly P and L -<month>" and `totalpages` = ceil(rows / 23).
   - **Show as PDF:** writes "<ReportHeader>.pdf" and opens ACTION_VIEW.
   - **Share:** sends the same file through the chooser "Share Image" (toast if no target).
   - **Direct Print** (hidden): PrintManager job "<app_name> Document", document name "receipt_test.pdf".
5. `drawPage()`:
   - "Page N" top right; the header centred in 15 pt;
   - columns "Particulars / Credit / Debit" (edges at x 36, 336, 456, 565); rows in 10 pt;
   - amounts "##,##,##,##0"; 23 rows per page;
   - footer "Printed @ null, null" on full pages, because `currentDate`/`currentTime` are never set in this class.

**Functions.**
- Lifecycle and list: `onCreateView()`, `onDestroy()`, `onOptionsItemSelected()`, `onTransactionselected()`, `onActivityResult()`.
- Data: `ASYNCGetMonthlypandl.doInBackground()/onPreExecute()/onPostExecute()`, `ASYNCGetLedgerStatement.doInBackground()/onPostExecute()`.
- Output: `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.onLayout()/onWrite()/pageInRange()`, `drawPage()`, `drawCenter()`, `drawCenterwithgivenwidth()`. `drawCenterwithbox()` is never called.

**Rules & edge cases.**
- Extra collection (கூடுதல் வசூல்) is not included, so the 12 months do not add up to the annual P&L from `tbbspl_list`.
- The same `ledgerstatement_act` extra-key mismatch applies. The statement returns ledger id 0 with zero amounts, so `onActivityResult` removes rows whose LedgerId is 0, which deletes the Net Profit/Net Loss row (code reading).
- `onActivityResult` uses `data` without a null check (NPE if the statement returns no intent).
- `ComparedPL` appears to be a permanent server table that the procedures rewrite, so simultaneous report runs from several devices could interfere (unclear from app code). Inside the app, the pager's preloaded neighbouring pages run one after another (AsyncTask serial executor).

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `tbbspl_adapter` | RecyclerView adapter + Filterable | tbbspl_list, monthlypandl_fragment | Row `tbbspl_row`: `tbbsplrow_ledgername` = ComponentDesc, `tbbsplrow_crvalueid` = Cr, `tbbsplrow_drvalueid` = Dr. Zero or null amounts are blank. Format "##,##,##,##0.00" if `softsettingsvalue.allowdecimalinamounts`, else "##,##,##,##0". Click → `ContactsAdapterListener.onTransactionselected(row, position)`. Filter: query upper-cased, then `ComponentDesc.contains(query)`. |
| `tbbspl_particulars_adapter` | RecyclerView adapter + Filterable | tbbspl_particulars | Row `tbbspl_particulars_row`: LedgerName, Debit (`tbbsplpartrow_debitid`), Credit (`tbbsplpartrow_creditid`). Debit comes before Credit, the opposite of the main list. Same number formats. Click → `onTransactionselected`. Filter: upper-case contains on LedgerName. |
| `financelist_selectable_adabpter` | RecyclerView adapter + Filterable | accountsreports_parameters | Row `financelist_withselectablecheckbox_row`: a CheckBox labelled with FinMailingName; `onCheckedChanged` → `financelistadapterlistner.onfinancechecked(finance, isChecked)`. The checked state is not re-bound in `onBindViewHolder`, so recycled rows can show stale ticks. The filter compares an upper-cased query with a lower-cased name and never matches letters; it is unused anyway. |
| `tbbspl_data` | Serializable model | tbbspl_list, tbbspl_particulars, monthlypandl_fragment | SNo; ComponentId (the component id, equal to LedgerId for P&L rows; −1 Net Profit/Loss, −2 Total, 0 Difference or monthly summary rows); ComponentDesc; LedgerId; LedgerName; MCLoanId; Cr and dr (Double; null shows blank); nextdetaillevel (1 → particulars, 2 → ledger statement, 3 → no action); printinglevel (1 abstract, 2 detail). |
| `partnerwiseperformanceandloans_BaseFragment` | abstract Fragment base | monthlypandl_fragment | Extends `BaseFragment`, which calls `setHasOptionsMenu(true)` and inflates `partnerwiseperformance_menu` (Direct Print hidden, Show as PDF, Share). Holds `listView`, `pauseOnScroll` = false, `pauseOnFling` = true and the keys `STATE_PAUSE_ON_SCROLL/FLING`. `onResume` calls an empty `applyScrollListener()`; `onPrepareOptionsMenu()` and `startImagePagerActivity()` are also empty (leftovers from an image-loader sample). |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_partyledgers_parameters.xml` | accountsreports_parameters (also partyledgers_parameters) | ConstraintLayout `main`: label "Select Finances" + RecyclerView `partyledgersparameters_finanelistrecyclerviewid`; `partyledgersparameters_selectfinancialyear` "Financial Year :"; `partyledgersparameters_selectperiodlabelid` "Select Report Date"; `partyledgersparameters_fromdatelayoutid` (`partyledgersparameters_fromdatelabelid` "Date"/"தேதி", ": ", EditText `partyledgersparameters_fromdateid` with sample "06-Jun-2020"); `partyledgerparameters_partyselectionid` (hidden by this screen); Button `partyledgersparameters_showbuttonid` "SHOW"; ProgressBar `partyledgersparameters_pbarid` (gone) |
| `financelist_withselectablecheckbox_row.xml` | financelist_selectable_adabpter | CheckBox `financelistwithselectablecheckbox_checkboxid` (placeholder "Finance Name") |
| `activity_tbbspl.xml` | tbbspl_list | CoordinatorLayout `main` / AppBarLayout Toolbar holding `tbbspl_financenamesid`, `tbbspl_reportdateid`, `tbbspl_accountperiodtvid` (placeholders "FIN"); RecyclerView `tbbspl__recyclerviewid`; ProgressBar `tbbspl_pbar` (gone) |
| `tbbspl_row.xml` | tbbspl_adapter | `tbbsplrow_ledgername` (placeholder "Acc Ledger Name"), then right-aligned `tbbsplrow_crvalueid` and `tbbsplrow_drvalueid` |
| `activity_tbbspl_particulars.xml` | tbbspl_particulars | CoordinatorLayout `main` / AppBarLayout Toolbar; RecyclerView `acctranslist_recycler_view`; ProgressBar `acctranslist_pbar` (gone) |
| `tbbspl_particulars_row.xml` | tbbspl_particulars_adapter | `tbbsplpartrow_particularsid` (placeholder "SALARY EXPENSES"), then right-aligned `tbbsplpartrow_debitid` and `tbbsplpartrow_creditid` |
| `monthwisepl_home.xml` | monthwisepandl | ViewPager `pager` with a hidden PagerTitleStrip |
| `fr_monthlypandl.xml` | monthlypandl_fragment | Title TextView `fragmenttitleid` ("Fragment Title"), RecyclerView `@android:id/list`, ProgressBar `fragment_progressbarid` (gone) |

---
[← brain.md](../../brain.md) · Module 8.10 of 20 · Previous: [8.9 Collection areas, maps, GPS staff tracking and attendance](09-collection-areas-maps-gps-staff-tracking.md) · Next: [8.11 Daily Patti (outstanding) reports and meeting report](11-daily-patti-outstanding-reports-meeting-report.md)
