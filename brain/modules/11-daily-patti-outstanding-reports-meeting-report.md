# 8.11 Daily Patti (outstanding) reports and meeting report

[← brain.md](../../brain.md) · Module 8.11 of 20 · Previous: [8.10 Accounts reports: trial balance, balance sheet, P&L, month-wise P&L](10-accounts-reports-trial-balance-balance-sheet.md) · Next: [8.12 Thittam (collection plan) and weekly analysis reports](12-thittam-collection-plan-weekly-analysis-reports.md)

The Daily Patti (தினப்பட்டி) is the line-level outstanding (OS) statement for MC loans over a chosen period. It shows the opening live (nadappu) and pending (nippu) balances, new loans (kadan patru), collections (kadan / nippu varavu), the transfer of matured unpaid loans from live to nippu (nipputhakkal), bad-debt write-offs (varakadan illappu), and the resulting closing OS and OS increase/decrease. `dailypattinew_act` runs four server procedures (`PattiProc_01…04`), builds the 21-line statement on the device, and lets the user drill from each amount line into a per-loan list (four list screens), then into that loan's collection history. A legacy all-finance pager (`allfinancedailypatti` → `pattidetails_loans_act`) is still compiled in, but nothing opens it. The module also contains `meetingreport`, the ST/OF-loan "interest pending" review report: an expandable party → loan list with PDF/print, call/SMS/WhatsApp reminders and a jump to interest entry. In this build no menu leads to it. The group has 22 classes: 8 screens (one with an inner Fragment), 7 adapters and 7 models.

## Daily Patti statement — `dailypattinew_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, default `AppTheme` with action bar); implements `allfinancedailypattinew_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_dailypattinew.xml` + row `allfinancedailypattirow.xml`; menu `menuwithoutsearchandprintoptions` |
| **Opened from** | (1) Drawer Reports → "Daily Patti / தினப்பட்டி" (`nav_allfinancedailypatti`) → `selectbranchandfinance` with `requestcode` 9004 (`requestcodes.dailypatti_requestcode`; JADX shows it as `constantvalues.parties_updatepartyrequest`, which has the same value). The user picks branch/line, From Date and To Date, then taps **SHOW** (`btnshowtodaycollectionabstract`). (2) Drawer "Varathittam Analysis / வார திட்டம்" (`nav_varathittamanalysis`) → `selectbranchandfinance` rc 304 → **Patti** button (`btnselectedweekpatti`). The period is one week: Sunday − 6 days to the chosen Sunday (one of the last ~25 Sundays). Extras in both cases: `passedfinanceid` (int FinId), `LineName`, `fromdate`, `todate` (`java.sql.Date`). |
| **Opens** | `dailypatti_nadapunippuphakkilist_act` (rows 1, 9), `dailypatti_nadapunippucollectionlist_act` (rows 2, 3, 10, 12, 18–21), `dailypatti_varakadanillapulist_act` (rows 4, 11), `dailypatti_kadanpatrulist_act` (row 5); system print dialog, PDF viewer, share chooser |
| **Data** | Remote SQL Server via `globalvars.branchlevelReport_branchconnString` (+ `branchlevelReport_branchdbusername/password`; `selectbranchandfinance` sets it to the chosen branch's `Appconstr`, default `branchconnString`) · procs `PattiProc_01NadappuandNippuPhakki`, `PattiProc_02NadapuNippuCollection`, `PattiProc_03VarakadanIllappu`, `PattiProc_04KadanPatru` · no local tables, no SharedPreferences (the dossier's "prefs read" names are ResultSet columns) |

**Purpose.** Shows how one line's outstanding moved over a date range: the opening OS, what was added and removed, the closing OS, and whether the OS rose or fell. Each figure has a per-loan list behind it.

**UI.** Title `Patti:` / `பட்டி:` + `dd-MM-yy to dd-MM-yy`. A DarkGoldenrod strip `dailypattinew_financenameid` shows the line name (layout default "FINANCE NAME"). RecyclerView `recycler_view` (divider `customersearchlistdivider`) holds 21 rows. Each row has four columns:
- Particulars.
- Left value: reductions (the varavu/credit side).
- Right value: balances and additions (the patru/debit side).
- Count.

There are no on-screen column headers, and zero values are left blank. Numbers use `DecimalFormat("##,##,##0")`. Java applies the last group size to the whole number, so this prints 3-digit Western grouping, not lakh grouping. Row colours are set by the adapter: 1 and 9 Navy; 3 and 12 `nipcolor` (#DC0000); 6 and 13 DarkOliveGreen; 7, 14 and 17 blue.

**Menu / actions.** `menuwithoutsearchandprintoptions` offers **Direct Print**, **Show as PDF** and **Share**. Search is declared `visible=false` in the menu, and "Refresh Ledgers" is hidden in `onCreateOptionsMenu`. Tapping a row opens its drill-down (see the table below). Back/Home closes the screen.

**How it works.**
1. `onCreate` reads the extras, sets the title and line name, creates the five lists and the adapter, and starts `ASYNCGetAbstractlist` (ProgressDialog "Loading....").
2. `doInBackground` opens a jTDS connection and runs four calls built by string concatenation. Each fills one Serializable model list. The procedures' own SQL is server-side and not in the APK. All amounts are read with `getInt`, so paise are truncated.
   - **P1** `exec PattiProc_01NadappuandNippuPhakki @FinId=<id>,@ReportDate='<fromdate>'` → `dailypatti_nadapuandnippuphakki_data`, one row per loan outstanding at the from-date. Columns: `MCLoanId, MCLoanDate, ReportIdentification, LedgerName, MCLoanAmountCollectable, Collected, Balance, ClosingDate, IsNIP, SubAreaNo, SubAreaName`. `IsNIP` 0 = live, 1 = nippu (past term, still unpaid).
   - **P2** `exec PattiProc_02NadapuNippuCollection @FinId=…,@FromColDate='<from>',@ToColDate='<to>'` → `dailypatti_nadapuandnippucollection_data`. Same identity columns plus `PrevCollection` (collected before the period), `Collection` (collected in the period), `ExtraCollection` (signed: > 0 excess, < 0 shortfall), `ClosingDate`, `IsNIP`, `CollCount`.
   - **P3** `exec PattiProc_03VarakadanIllappu @FinId=…,@FromColDate,@ToColDate` → `dailypatti_varakadanillapu_data`: loans written off in the period, with `ClosingDate, ClosedDate, collected, Pending` (`Pending` = amount lost) and `IsNIP`.
   - **P4** `exec PattiProc_04KadanPatru @FinId=…,@FromDate,@ToDate` → `dailypatti_kadanpatru_data`: new loans, with `SNo, SubAreaName, SubAreaNo, MCLoanDate, Party, LT, Loan, Dues, IntRate, Interest, Mahimai`. No `MCLoanId` is read from P4.
3. It then builds the 21 `dailypattinew_data` lines and computes them on the device (L = left side, R = right side):

| Sno | Particulars (EN / TA) | Side | Value | Tap opens |
|---|---|---|---|---|
| 1 | Live Loans Balance / நடப்பு பட்டி பாக்கி | R | Σ P1.Balance where IsNIP=0 (count = loans). This is the **opening live OS**. | balance list, IsNIP=0 rows |
| 2 | Live Loans Collection / கடன் வரவு | L | Σ P2.Collection where IsNIP=0 and Collection>0 | collection list, same filter |
| 3 | Nippu Posting / நிப்புத்தாககல் | L | Σ (MCLoanAmountCollectable − (Collection + PrevCollection)) where IsNIP=0, ExtraCollection<0 and ClosingDate ≤ to-date. This is the unpaid remainder of live loans whose term has ended; it moves to nippu. | collection list, same filter |
| 4 | Bad Debts Loss / வாராக் கடன் இழப்பு | L | Σ P3.Pending where IsNIP=0 and Pending>0 | bad-debt list |
| 5 | New Loans / கடன் பற்று | R | Σ P4.Loan where Loan>0 | new-loans list (all P4 rows) |
| 6 | Total / ஆக மொத்தம் | L, R | L = Σ L(1–5); R = Σ R(1–5) | – |
| 7 | "Total Pending Loans Balance" / நிகர நடப்பு பட்டி பாக்கி | R | R6 − L6 = **closing live OS** | – |
| 8 | (blank) | | | – |
| 9 | Net Pending Loans Balance / நிப்பு பட்டி பாக்கி | R | Σ P1.Balance where IsNIP=1 and Balance>0. This is the **opening nippu OS**. | balance list, all IsNIP=1 rows |
| 10 | Pending Loans Collection / நிப்பு வரவு | L | Σ P2.Collection where IsNIP=1 and Collection>0 | collection list |
| 11 | Bad Debts Loss / வாராக் கடன் இழப்பு | L | Σ P3.Pending where IsNIP=1 and Pending>0 | bad-debt list |
| 12 | Nippu Posting / நிப்புத்தாககல் | R | Same amount and count as row 3 (the transfer into nippu) | same list as row 3 |
| 13 | Total / ஆக மொத்தம் | L, R | L = Σ L(9–12); R = Σ R(9–12) | – |
| 14 | Net Pending Loans Balance / நிப்பு பட்டி பாக்கி | R | R13 − L13 = **closing nippu OS** | – |
| 15 | Loan Balance Difference / பட்டி குறைவு/அதிகம் (relabelled) | R | Opening = R1 + R9; closing = R17. If closing > opening: "OS Increases / பட்டி அதிகம்" = closing − opening. If closing < opening: "OS Decreases / பட்டி குறைவு" = opening − closing. If equal: "No Changes in OS / பட்டி மாற்றம் இல்லை". | – |
| 16 | (blank) | | | – |
| 17 | Total Balance / மொத்த பாக்கி | R | R7 + R14 = **closing total OS** | – |
| 18 | Extra Collection on Live Loans / கூடுதல் வசூல் | L | Σ P2.ExtraCollection where IsNIP=0 and >0 | collection list |
| 19 | Extra Collection on Pending Loans / நிப்பு கூடுதல் வசூல் | L | Σ P2.ExtraCollection where IsNIP=1 and >0 | collection list |
| 20 | Live Loans Closed / நடப்புக் கணக்கு முடிவு | count | Count of P2 rows with IsNIP=0, Collection>0 and MCLoanAmountCollectable ≤ Collection + PrevCollection | collection list |
| 21 | Pending Loans Closed / நிப்பு கணக்கு முடிவு | count | Same rule with IsNIP=1 | collection list |

   The resulting accounting model:
   - **Closing live** = opening live + new loans − live collection − nippu posting − live bad debts.
   - **Closing nippu** = opening nippu + nippu posting − nippu collection − nippu bad debts.
   - **Total closing (17)** = row 7 + row 14. Line 15 shows the change against the opening total (row 1 + row 9).
   - Extra collections (18, 19) and closed-loan counts (20, 21) are memo lines; they do not enter the balance.
   - No interest (ஆதாய வரவு / athaya varavu) or document-charge (மகிமை / magimai) totals are computed. P4's IntRate, Interest and Mahimai appear only per loan in the new-loans list.
   - P1 reports the OS as Collectable/Collected/Balance, so the OS is presumably held in collectable terms and `Loan` is presumably the collectable amount. This is not determinable from the client.
4. `onPostExecute` dismisses the dialog and refreshes the adapter. Errors only set `ErrorText`, which is never shown, so a failure produces an empty or partial statement.
5. `onpattirow_selected(row)` filters the in-memory lists by `Sno` (as in the table) and starts the drill-down. Extras: `list` (the filtered ArrayList) and `title` (the row's particulars); rows 1 and 9 also pass `fromdate_patti` and `todate_patti`. Rows 6–8 and 13–17 do nothing.
6. Printing. `onOptionsItemSelected` sets `totalpages = ceil(rows/30)` and `curprintingrowcount = 0`. `drawPage` draws an A4 portrait page (595×842 pt): "Page n" at the top right, the heading "Daily Patti the Period from dd-MMM-yyyy to dd-MMM-yyyy", and columns S.No, Particulars, Value, Value, Count. Only values > 0 are printed (`##,##,##,##0`). Row 3 is red (JADX shows `SupportMenu.CATEGORY_MASK` = 0xFFFF0000; `ViewCompat.MEASURED_STATE_MASK` = black).
   - **Direct Print** → `PrintManager` job "FinSoft II Document" through `MyPrintDocumentAdapter`. The document name is `patti__<yyyyMMdd_HHmmss>_<rand>.pdf`; layout fails with "Page count is zero." when there are no rows.
   - **Show as PDF** → `createPdf` writes `Documents/venusmfc/st__<ts>_<rand>.pdf` to public storage (name from `defaulttables.generateUniqueFileName("st_")`) and opens it with ACTION_VIEW via FileProvider `…clone.provider`.
   - **Share** → `sharePdf` writes the same name under `getExternalFilesDir(DOCUMENTS)/venusmfc/` and opens an ACTION_SEND chooser "Share PDF" (`application/pdf`).

**Functions.**
- `onCreate()` — reads extras, sets title, adapter, starts the loader.
- `onCreateOptionsMenu()` — inflates the print menu and hides "Refresh Ledgers".
- `onOptionsItemSelected()` — computes the page count; dispatches Direct Print / Show as PDF / Share / back.
- `onpattirow_selected(dailypattinew_data)` — maps a row to its filtered drill-down list.
- `ASYNCGetAbstractlist.doInBackground()` — runs P1–P4 and builds and computes the 21 lines; `onPreExecute()/onPostExecute()` — progress dialog and refresh.
- `drawPage(page, n)` — renders one PDF page (30 rows per page); `drawCenterwithgivenwidth()` — centres header text in a column.
- `printDocument()`; `MyPrintDocumentAdapter.onLayout()/onWrite()/pageInRange()` — Android print framework.
- `createPdf(name)` — saves the PDF to public Documents and opens it; `sharePdf(name)` — saves it to app storage and shares it.
- `whiteNotificationBar()` — light status bar; `onDestroy()` — dismisses the dialog.

**Rules & edge cases.**
- The statement covers a single line (`@FinId` is one finance), despite the "allfinance" adapter name.
- The English label of row 7 is wrong: "Total Pending Loans Balance" is actually the closing **live** balance (the Tamil "நிகர நடப்பு பட்டி பாக்கி" is correct). "Net Pending Loans Balance" labels both the opening (9) and closing (14) nippu balance.
- Totals and drill-downs do not always match. Row 9 sums only Balance > 0, but its list shows every IsNIP=1 loan. Row 5 sums only Loan > 0, but its list shows every P4 row.
- The footer "Printed @ …" uses `currentDate/currentTime`, which are never assigned, so it would print "null, null". It is drawn only when a 30-row page fills (checked in smali), so the 21-line statement never shows it.
- **Show as PDF** has no `resolveActivity` check, so it crashes on a device with no PDF viewer. The `ISO_A5` media set in `sharePdf` is unused; the page is still 595×842.
- The lists are filled on the worker thread while bound to the adapter.
- Drill-down lists travel as Serializable intent extras, so a very large line risks `TransactionTooLargeException`.
- Rows 3 and 12 compare `ClosingDate` with no null check.
- The drill-downs for rows 4/11 and row 5 are broken (see those screens).

## Live / nippu balance list — `dailypatti_nadapunippuphakkilist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar` + Toolbar with Up) |
| **Layout(s)** | `activity_nadapunippuphakkilist.xml` (includes `customersearchlist`), row `dailypattinew_nadapunippuphakki_row.xml`; menu `menuwithoutsearchandprintoptions` |
| **Opened from** | `dailypattinew_act` row 1 (IsNIP=0 loans) and row 9 (IsNIP=1 loans). Extras: `list` (ArrayList<`dailypatti_nadapuandnippuphakki_data`>), `title`, `fromdate_patti`, `todate_patti`. |
| **Opens** | `selectedloancollectiondetails` (G04, individual loan collection details) with `MCLoanId`, `PartyName` = LedgerName, `Remarks` "", `Tag` "", `iscollectioneditallowed` "0" |
| **Data** | None; the in-memory list comes from the intent. PDFs are written to `Documents/venusmfc/` or app storage. |

**Purpose.** Printable per-loan list behind the opening live balance or the opening nippu balance.

**UI.** Toolbar title = the row's particulars. Each row shows Party · Loan (collectable) · Balance · Collected, then Area (sub-area) · Loan Method (`ReportIdentification`) · `LDate/CDate` (loan date / closing date, dd-MM-yy).

**Menu / actions.** Direct Print, Show as PDF and Share work as in `dailypattinew_act`. "Refresh Ledgers" is visible but does nothing. The Search item is declared invisible in this menu, so the adapter's working case-insensitive name filter can never be used. Tapping a row opens read-only loan details.

**How it works.**
1. `onCreate` reads the extras and binds `dailypatti_nadapunippuphakki_adapter`.
2. Print/PDF computes `totalpages = ceil(n/30)`. `drawPage` prints the heading "<title> Patti Period dd-MMM-yyyy to dd-MMM-yyyy" and 9-pt columns No, Loan Id, Loan Date, Ledger Name, Collectable, Collected, Balance, Closing Date. Nippu rows (IsNIP=1) are red, and amounts print only when > 0.
3. Files and job names follow the patti screen (`st__…pdf`, print document `patti__…pdf`). The whole list is printed, not the filtered view.

**Functions.**
- `onCreate()`, `onCreateOptionsMenu()` (SearchView wiring), `onOptionsItemSelected()`.
- `onContactSelected()` — opens loan details; `onthumnailclicked()` — empty; `onActivityResult()` — pass-through.
- `drawPage()`, `drawCenterwithgivenwidth()`, `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.onLayout()/onWrite()/pageInRange()`, `whiteNotificationBar()`.

**Rules & edge cases.**
- The footer reads "Printed @ null, null" on full pages.
- Print pagination uses a running row counter that only `onLayout` resets, and `drawPage(n)` keeps drawing until the counter reaches (n+1)×30. Printing a page range that does not start at page 1 is therefore wrong: page 2 alone restarts at row 1 and tries to draw 60 rows, overrunning the page.
- `iscollectioneditallowed` is passed as the String "0", but the target reads it with `getBoolean`, which returns false, so editing is off.
- Show as PDF has no `resolveActivity` check.

## Collection-type list (collection, nippu posting, extra collection, closed loans) — `dailypatti_nadapunippucollectionlist_act`
| | |
|---|---|
| **Type** | Activity (NoActionBar + Toolbar, Up) |
| **Layout(s)** | `activity_nadapunippucollectionlist.xml` (includes `customersearchlist`), row `dailypattinew_nadapunippucollection_row.xml`; menu `menucustomersearchlist` |
| **Opened from** | `dailypattinew_act` rows 2 and 10 (collections), 3/12 (nippu posting), 18 and 19 (extra collection), 20 and 21 (closed loans). Extras: `list` (ArrayList<`dailypatti_nadapuandnippucollection_data`>), `title`. |
| **Opens** | `selectedloancollectiondetails` (`MCLoanId`, `PartyName` = LedgerName, `Remarks` "", `Tag` "", `iscollectioneditallowed` "0") |
| **Data** | None (list from the intent). An `SQLiteDatabase db` field is declared but never used. |

**Purpose.** Lists the loans behind a collection-type patti figure.

**UI.** The title starts as "Parties" and is replaced by the row's particulars. Each row shows Party · Loan (collectable) · Prev Collection · Collection, then Area · Method (`ReportIdentification`) · `LDate/CDate`. `ExtraCollection` and `CollCount` are not shown, so for rows 3, 12, 18 and 19 the actual amount is not visible.

**Menu / actions.** Search filters by name. "Rearrange Parties" and "Show Parties in MAP" come with the shared menu but are not handled, so they do nothing. Tapping a row opens loan details. Back closes an open SearchView first.

**How it works.** `onCreate` reads `title` and `list` inside a try/catch and binds `dailypatti_nadapunippucollection_adapter`. `onCreateOptionsMenu` wires the SearchView to the adapter filter.

**Functions.** `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, `onContactSelected()`, `onthumnailclicked()` (empty), `onActivityResult()`, `whiteNotificationBar()`.

**Rules & edge cases.** Search does not work with English letters: the adapter upper-cases the query but compares it with the lower-cased name, so only digits and Tamil text match. This screen has no print or PDF.

## Bad-debt loss list — `dailypatti_varakadanillapulist_act`
| | |
|---|---|
| **Type** | Activity (NoActionBar + Toolbar, Up) |
| **Layout(s)** | `activity_varakadanillapulist.xml` (includes `customersearchlist`), row `dailypattinew_varakadanillapu_row.xml`; menu `menucustomersearchlist` |
| **Opened from** | `dailypattinew_act` row 4 (live, IsNIP=0) and row 11 (nippu, IsNIP=1), loans with Pending > 0. Extras: `list` (ArrayList<`dailypatti_varakadanillapu_data`>), `title`. |
| **Opens** | `selectedloancollectiondetails` (`MCLoanId`, `PartyName`, `Remarks`, `Tag`, `iscollectioneditallowed` "0") |
| **Data** | Local SQLite `Parties` (`select LedgerName,Tag,MobileNo1,MobileNo2 from Parties where LedgerId=<passetpartyledgerid>`) and `MobileAppCollData` (`select Remarks from MobileAppCollData where MCLoanId=<id>`) |

**Purpose.** Lists loans written off as bad debts (varakadan illappu) in the period.

**UI.** The default title "Parties TEST" is replaced by the row's particulars. Each row shows Party · Loan (collectable) · Collected · Pending (amount lost), then Area · Loan Method · `LDate/CDate/Closed on` (loan date, closing date, closed date).

**Menu / actions.** Search; Rearrange Parties and Show Parties in MAP do nothing. Tapping a row opens loan details.

**How it works.** `onCreate` opens SQLite `venusmicrofinv005` and binds `dailypatti_varakadanillapu_adapter`. `onContactSelected` looks up the party name and tag in `Parties` and the remarks in the offline `MobileAppCollData`, then starts loan details.

**Functions.** `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, `onContactSelected()`, `onthumnailclicked()` (empty), `onActivityResult()`, `whiteNotificationBar()`.

**Rules & edge cases.**
- **The screen crashes on open.** Its layout (0x7f0c0085) has no `@id/main` view, so `findViewById(R.id.main)` returns null. The bundled `ViewCompat.setOnApplyWindowInsetsListener` then dereferences the view (`v.getTag`/`setTag`) and throws a NullPointerException. Rows 4 and 11 therefore cannot be drilled.
- Even with that fixed:
  - `passetpartyledgerid` is never set, so the `Parties` query (`LedgerId=null`) leaves the party name and tag empty.
  - Remarks exist only if the loan is in the offline download.
  - Search has the same upper/lower-case bug.

## New loans (kadan patru) list — `dailypatti_kadanpatrulist_act`
| | |
|---|---|
| **Type** | Activity (NoActionBar + Toolbar, Up) |
| **Layout(s)** | Reuses `activity_nadapunippucollectionlist.xml`; row `dailypattinew_kadanpatru_row.xml`; menu `menucustomersearchlist` |
| **Opened from** | `dailypattinew_act` row 5. Extras: `list` (all P4 rows, ArrayList<`dailypatti_kadanpatru_data`>), `title`. |
| **Opens** | `selectedloancollectiondetails` (intended) |
| **Data** | Intended: local `Parties` and `MobileAppCollData` lookups (the same queries as the bad-debt list) |

**Purpose.** Lists loans disbursed in the period with their interest and document charges.

**UI.** Title = the row's particulars. Each row shows Party · Loan · Dues · Interest · Mahimai (document charges), then Area · Loan Method (`LT`) · LDate. "Dues" is shown exactly as returned by P4.

**Menu / actions.** Search on `Party` (same case bug). Rearrange Parties and Show Parties in MAP do nothing. Tapping a row is meant to open loan details but crashes (see below).

**How it works.** `onCreate` binds `dailypatti_kadanpatru_adapter`. `onContactSelected` sets `MCLoanId` and `iscollectioneditallowed` "0", queries `Parties` and `MobileAppCollData`, then starts loan details.

**Functions.** `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, `onContactSelected()`, `onthumnailclicked()` (empty), `onActivityResult()`, `whiteNotificationBar()`.

**Rules & edge cases.** **Tapping any row crashes.** `db` is never opened (there is no `openOrCreateDatabase` call), so `db.rawQuery` throws a NullPointerException. Even with that fixed, `MCLoanId` is never filled from P4 (always 0) and `passetpartyledgerid` is never set, so the screen would open loan 0 with no party name.

## All-finance daily patti pager (legacy) — `allfinancedailypatti`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity; manifest label `title_activity_allfinancedailypatti` = "allfinancedailypatti", theme `AppTheme.NoActionBar`, `stateAlwaysHidden`). Inner `PlaceholderFragment` (Fragment) and `SectionsPagerAdapter` (FragmentPagerAdapter). |
| **Layout(s)** | `activity_allfinancedailypatti.xml` (Toolbar + ViewPager `container`), `fragment_allfinancedailypatti.xml` (one per page), row `allfinancedailypattirow.xml` |
| **Opened from** | Nothing: no class or menu starts it in this build (checked in Java and smali) |
| **Opens** | `pattidetails_loans_act` (`pcno` = line SNo, `particulars`) |
| **Data** | Local SQLite `FinanceDetails` (`select FinId,FinMailingName,CashLedgerId,AdminMobileNos,LedgerName,LId,DefaultLoanMethodId from FinanceDetails`, plus `select count(FinId) from FinanceDetails` for the page count) · `globalvars.allfinancedailypattiJSON` (Gson `List<allfinancedailypattidata>`) |

**Purpose.** An older design: one swipeable page per finance, showing that finance's patti lines from a pre-computed JSON.

**UI.** Toolbar title `dhinappatti` ("Dhinnappatti" / "தினப்பட்டி"). Each page has:
- `section_label`, first set to "Hello World from section: N", then to the finance's FinMailingName.
- RecyclerView `afdp_recycler_view` with Particulars, left value and right value. SNo 1, 2, 3, 4, 8, 9, 18 and 19 append " (count)" to the particulars, and the count column is hidden. SNo 14 is blue, 6 is `warningcolor` (#FF4E0C) and 12 is `nipcolor`.

There is no options menu: `onCreateOptionsMenu` inflates nothing, and the `action_settings` branch never runs.

**How it works.**
1. `onCreate` loads all `FinanceDetails` rows into the static `financelist` (`financemaster_data`).
2. `SectionsPagerAdapter.getCount()` reopens SQLite and returns the finance count; `getItem(p)` returns `PlaceholderFragment.newInstance(p+1)`.
3. `onCreateView` parses `allfinancedailypattiJSON`, keeps the items whose `FinId` matches the page's finance, and binds `allfinancedailypattiadapter`.
4. Tapping a row opens `pattidetails_loans_act`.

The fragment also implements `allfinancedailuthittamadapter.ContactsAdapterListener` (which would open `pattidetails_loans_act` with `pcno`=1), but no thittam adapter is attached.

**Functions.** `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, `PlaceholderFragment.newInstance()`, `.onCreateView()`, `.onContactSelected(allfinancedailypattidata)`, `.onContactSelected(allfinancedailythittamdata)`, `SectionsPagerAdapter.getItem()`, `.getCount()`.

**Rules & edge cases.**
- This is dead code. `globalvars.allfinancedailypattiJSON` is declared `null` and nothing ever assigns it (smali has no `sput`).
- If the screen were opened, `Gson.fromJson(null)` would return null and `items.size()` would throw.
- The static fields (`financelist`, `newdata`, `mAdapter`, `allfinancepattilist`) are shared across pages and overwritten by whichever page the ViewPager creates last.

## Patti line loan details (legacy) — `pattidetails_loans_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, NoActionBar + Toolbar) |
| **Layout(s)** | `activity_customersearch.xml` (FAB hidden), row `pattidetails_loans_row.xml`; menu `menucustomersearchlist` |
| **Opened from** | Only `allfinancedailypatti`, which is itself unreachable, with `pcno` (int) and `particulars` |
| **Opens** | `selectedloancollectiondetails` (`MCLoanId` as a Double, `PartyName`, `Remarks` "", `Tag` "", `iscollectioneditallowed` "0") |
| **Data** | Remote `globalvars.selbranchconnString` · procedure `[PattiDetails_LoanDetails] @PCNo=<pcno>` → `MCLoanId, LedgerId, SubAreaNo, SubAreaName, LedgerName, Imagefilename, CWLedgerId, MCLoanAmountCollectable, Balance, MCLoanDate, ClosingDate, Paid, InsAmount` |

**Purpose.** Lists the loans that make up one line ("patti component number") of the legacy all-finance patti.

**UI.** Title = `particulars`. Rows come from `pattidetails_loans_adapter` (photo, party number, name, loan, balance, collected, due).

**Menu / actions.** Search matches name, closing-date text or loan-date text (case-sensitive). Rearrange Parties and Show Parties in MAP are not handled. The SwipeRefreshLayout has no listener. Tapping a row opens loan details.

**How it works.** `onCreate` reads the extras, hides the FAB, binds the adapter and runs `ASYNCGetPrevLoanList` (ProgressDialog "Connecting"). That task calls the procedure and appends `pattidetails_loansdata` rows; `onPostExecute` hides the dialog and refreshes. Exceptions are swallowed, leaving an empty list. The procedure takes only the component number, so it must rely on server-side state computed elsewhere; what that is cannot be seen from the client.

**Functions.** `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, `onContactSelected()`, `onthumnailclicked()` (empty), `onActivityResult()`, `whiteNotificationBar()`, `ASYNCGetPrevLoanList.doInBackground()/onPreExecute()/onPostExecute()`.

**Rules & edge cases.**
- The balance appears only with special access 5 (`isShowbalanceincollectionlist_5`).
- The party number appears only when soft setting `showpartynoincollectionlist_6` is on.
- Photos load only when `enablepartyimageloading` is on.

## ST/OF loan interest-pending meeting report — `meetingreport`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme`, `stateAlwaysHidden`); action bar ForestGreen |
| **Layout(s)** | `activity_meetingreport.xml`; group/child rows `mettingreport_partylevel_row` / `meetingreport_loanlevel_row` through `meetingreports_expandablelist_adapter` (G14); menu `menu_meetingreports` |
| **Opened from** | `partyledgers_parameters` (G14) with `requestcode` 7013 (`constantvalues.parametersselection_requestcodeformeetingreports`). That screen is titled "STL Interest Pending" / "OFL Interest Pending" and shows a finance check-list plus an "Interest Upto" date that defaults to the last day of the previous month (party and from-date fields hidden). Extras: `ttid` (3 ST / 5 OF), `requestcode` 7013, `fromdate` (accounting-year start; unused), `todate`, `selectecdfinanceids` (comma-separated FinIds), `selectedfinancenames`. The parameter screen is opened with 7013 only by this screen's **Refresh List**. The drawer passes only 7014 and 7027 (checked in smali), so neither screen is reachable in this build. |
| **Opens** | `partyledgers_parameters` (refresh), `stloaninterestcalculationandpayments` (G13; `ledgerdata`, `ttid` 4), `ofloans_interestcalculationandpayments` (G13; `ledgerdata`, `ttid` 6), dialer, SMS app, WhatsApp / WhatsApp Business, PDF viewer, print dialog |
| **Data** | **Remote** (`globalvars.selbranchconnString`): procs `STLIntColPending`, `STLIntColPendingList`, `GetSTLAbstract` / `OFIntColPending`, `OFIntColPendingList`, `GetOFLAbstract`; server tables `tempstlIntColPenidnglistnew`, `tempOFLIntColPenidnglistnew`. **Local SQLite:** `tempPartyLevelPending`, `tempstlIntColPenidnglistnew`, `OFLtempPartyLevelPending`, `tempoflIntColPenidnglistnew`, `SMSTemplates`, `tblLedgers`; also `tempdepIntColPenidnglistnew`, `deptempPartyLevelPending` and the misspelt `tempofllIntColPenidnglistnew`, which are created but never used. **Prefs** (`venusmicrofinpref011`, mirrored in `globalvars`): `STLoans_meetingreport_recentdownloadinterestuptodate`, `STLoans_meetingreport_recentdownloaddateandtime`, `STLoans_meetingreport_selectedfinancenames` and the matching `OFLoans_meetingreport_*` keys. |

**Purpose.** A report for review or partner meetings. For ST loans or OF loans, it lists every party with interest outstanding up to a date, across the selected finances, with party totals and per-loan detail. The user can print or save it as PDF, remind a party, or settle the interest. The action labels in this screen indicate that ST-loan interest is *collected* (ttid 4, receivable) and OF-loan interest is *paid* (ttid 6, payable).

**UI.**
- Title: "ST Loan Interest Pending" (ttid 3) or "OF Loan Interest Pending" (ttid 5).
- Top DarkBlue strip `meetingreports_includedfinanceinfoid`: the finance names from the last download.
- ExpandableListView `lvExp`. Each group row shows the party name (bold), total balance and total pending interest (`##,##,##,##0`). Each child row shows the finance short name, loan balance, interest from, interest upto (dd-MM-yy), days, and interest (CInt when PaidInt = 0, otherwise 0).
- Bottom DarkBlue strip `meetingreports_lastrefreshinfoid`: "CD:<interest-up-to date, dd-MM-yyyy>, DT:<server download time with '+05:30' removed>".
- During a refresh, a ProgressBar and the text `meetingreport_progresstextid` step through Connecting → Preparing List 1 → Preparing List 2 → Getting List 1 → Getting List 2. The window is made untouchable meanwhile (window flag 16 = `FLAG_NOT_TOUCHABLE`).

**Menu / actions.** **Refresh List** → `partyledgers_parameters` (`ttid`, `requestcode` 7013) and `finish()`. **Download List** builds the printable rows, writes a PDF and opens it. **Print List** sends the same rows to the Android print framework. **Long-pressing a party (group) row** opens the contact/interest dialog, for ST only. Group and child taps only expand or collapse.

**How it works.**
1. **Setup.** `onCreate` opens SQLite, resets the StrictMode VM policy, reads `ttid` and `requestcode`, and calls `prepareListData()`, so the cached last download appears first. It then attaches the adapter and listeners. If `requestcode == 7013`, it reads the finance ids/names and dates and starts `ASYNC_GetInterestPendingList`.
2. **Load from local cache.** `prepareListData()` creates the local temp tables if missing and loads the three prefs for the ttid into `globalvars` (`Date.valueOf` of the stored interest date). It fills both strips, then builds the list:
   - ST: groups from `Select PartyLedgerId, Ledgername, Balance, Interest, MobileNo1, intupto, intFrom from tempPartyLevelPending where Interest>0`. Mobile and dates are read only when PartyLedgerId > 0. Children come from `select ShortName,Balance,IntFrom,Intupto,CInt,Days,PaidInt from tempstlIntColPenidnglistnew where TypeId=1 and PaidInt=0 and PartyLedgerId=<id>`.
   - OF: the same pattern from `ofltempPartyLevelPending` (no mobile or dates) and `tempoflIntColPenidnglistnew`.
   - Exceptions are swallowed.
3. **Download.** `ASYNC_GetInterestPendingList.doInBackground` uses jTDS on `selbranchconnString` and first clears the group list.
   - **ST:**
     - Recreate and clear the local `tempstlIntColPenidnglistnew`; drop `tempPartyLevelPending` and recreate it with `MobileNo1, intupto, intFrom`.
     - Run `exec STLIntColPending @IUPDate='<todate>', @IntPaidDate='<todate>', @SelectedFinanceIds='<ids>'`; its `DOWNLOADDatetime` goes to `globalvars.STLoans_meetingreport_recentdownloaddateandtime`.
     - Run `exec STLIntColPendingList`.
     - Run `exec GetSTLAbstract`; its rows (`PartyLedgerId, Ledgername, MobileNo1, Intupto, IntFrom, Balance, Interest`) are inserted into local `tempPartyLevelPending`.
     - Run `Select typeid, FinId, STLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, STLoanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, STLoanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno from tempstlIntColPenidnglistnew order by PartyLedgerId` on the server and `insertOrThrow` each row into the local table of the same name. Dates are stored as `String.valueOf(date)`.
   - **OF:**
     - Clear the local `tempoflIntColPenidnglistnew` and `ofltempPartyLevelPending`.
     - Run `exec OFIntColPending …` with the same parameters (→ `DOWNLOADDatetime`), then `exec OFIntColPendingList`.
     - Run `exec GetOFLAbstract`; `PartyLedgerId, Ledgername, Balance, Interest` go into `OFLtempPartyLevelPending`.
     - Read the server's `tempOFLIntColPenidnglistnew` aggregated per OF loan (`min(IntFrom)`, `max(Intupto)`, `max(Days)`, `sum(CInt)`, `sum(PaidInt)`, `GROUP BY OFLOANID, …`) into local `tempOFLIntColPenidnglistnew`. SQLite table names are case-insensitive, so these are the same tables `prepareListData` reads.
   - Any exception makes the task return silently.
4. **Save.** `onPostExecute` stores `todate`, the download time and the finance names in prefs and `globalvars` for the ttid (even after a failed download). It then calls `prepareListData()`, refreshes, hides the progress and restores touch.
5. **Download/Print.**
   - Rows: `stloans_fillprintablerows()` reads `… from tempstlIntColPenidnglistnew where Paidint=0 order by RecPartnerId,typeId,LedgerName`; `ofloans_fillprintablerows()` uses the same order with no PaidInt filter. Either one builds the `meetingreport_data` list, sets `totalpages = ceil(n/23)`, captures the print date/time, and builds `ReportHeader` = "STL Interest Pending , Fin :<names>, CD:<dd-MM-yyyy>, DT:<download time>" ("OFL …" for OF).
   - `drawPage` lays out a 792×576 pt landscape page: the header, "Page n", and columns Party, FIN, No, Date, Loan, Paid, Balance, Int From, Int upto, Days, CalInt, Paid, Date, Recpt.
   - Row types come from the server proc's `typeid`:
     - 1 = loan line. The party name prints only on that party's first loan; amounts use `##,##,##,##0`, and paid amounts are blank when 0.
     - 2 = centred heading text.
     - 3 = sub-total line (name, short name, balance, calculated interest).
     - 4 = total line (name, balance, calculated interest).
   - Pages hold 23 rows; "Printed @ dd-MM-yyyy, HH:mm:ss" is printed at the foot of each full page.
   - **Download List** saves `Documents/venusmfc/<ReportHeader without ':'>.pdf.pdf` and opens it. If no viewer resolves, it toasts "Error while opening the document".
   - **Print List** creates the job "FinSoft II Document" (`MyPrintDocumentAdapter`; page `i` starts at row `i*23`, page 0 at −1).
6. **Long-press reminder (ST only, group rows).**
   - Reads `Select STId, SMSTitle, SMSDesc from SMSTemplates Where STId=1` locally and replaces `<<PartyName>>`, `<<LoanBalance>>`, `<<InterestBalance>>`, `<<FromDate>>` and `<<ToDate>>` (dd-MM-yyyy) with the party's values.
   - `voicecallandsmsoptions()` calls `fetchdata()`, which loads the party's full `tblLedgers` record into `partydetails_data`; mobile 2 comes from `defaulttables.fetchmobileno2`.
   - It then shows a dialog titled with the party name:
     - "Call:<mobile1>" / "Call:<mobile2>": ACTION_CALL with flag 0x04000000 (`FLAG_ACTIVITY_CLEAR_TOP`, shown by JADX as `AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL`). Toasts "Call Failed, please try again later." on ActivityNotFoundException.
     - "Send SMS": sends immediately with `SmsManager.sendMultipartTextMessage` and toasts "Message Sent".
     - "Compose SMS": opens `sms:` with `sms_body`.
     - "Whatsapp" / "Whatsapp Business": opens `https://api.whatsapp.com/send?phone=91<mobile>&text=<URL-encoded>` in package `com.whatsapp` / `com.whatsapp.w4b`. Toasts "Sharing tools have not been installed." on error.
     - "Collect Interest": opens `stloaninterestcalculationandpayments` (`ledgerdata`, ttid 4). For OF the label would be "Pay Interest", opening `ofloans_interestcalculationandpayments` (ttid 6).
   - If an exception occurs, the party name is toasted.

**Functions.**
- `onCreate()` — setup; refreshes automatically when arriving with 7013.
- `lambda$onCreate$3` (item long-click) — builds the reminder text and opens the contact dialog (ST only).
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — Refresh / Download / Print.
- `prepareListData()` — rebuilds the expandable list from local tables and prefs.
- `ASYNC_GetInterestPendingList.doInBackground()/onPreExecute()/onProgressUpdate()/onPostExecute()` — server download → local cache → prefs.
- `stloans_fillprintablerows()` / `ofloans_fillprintablerows()` — rows, page count and header for printing.
- `drawPage()`, `drawCenter()`, `drawCenterwithgivenwidth()` — PDF rendering.
- `createPdf(name)` — saves and opens the PDF; `printDocument()` + `MyPrintDocumentAdapter.onStart()/onLayout()/onWrite()/pageInRange()` — printing.
- `voicecallandsmsoptions(...)` + `lambda$voicecallandsmsoptions$7` — call, SMS, WhatsApp and interest-entry actions.
- `fetchdata(partyledgerid)` — loads the `tblLedgers` record passed as `ledgerdata` (the JADX constant `HttpHeaders.AGE` is just the column name "Age").

**Rules & edge cases.**
- Reachability: the drawer has no route here in this build. The sibling `partnerwiseloansandperformance_fragment` (G14) uses the same procedures per partner.
- **Reminder text:**
  - The template id 1 is also `smstemplateids.DownloadAlert`, so unless the server's template 1 contains the interest placeholders, the wrong text is sent.
  - The long-press uses the flat ExpandableListView position as the party index, so with any group above expanded it picks the wrong party. Child rows are ignored.
  - OF reports never open the dialog (the handler is gated to ttid 3), so "Pay Interest" is unreachable.
- **Messaging and calls:**
  - "Send SMS" sends without confirmation and toasts "Message Sent" even when sending fails.
  - WhatsApp assumes a bare 10-digit Indian number (it prepends 91).
  - The manifest's `<queries>` lists only Google Maps. With targetSdk ≥ 30, `resolveActivity()` will probably return null, so the WhatsApp options silently do nothing and **Download List** probably shows the error toast even though the file was written.
  - ACTION_CALL needs a CALL_PHONE grant obtained elsewhere; only ActivityNotFoundException is caught.
- **Failures and first use:**
  - On first use there is no stored interest date, so `Date.valueOf("")` throws; the exception is caught and the screen stays empty.
  - Failed downloads are silent but still overwrite the CD/DT/finance-name prefs.
- **Shared data:**
  - The server staging tables have no `#` prefix, so they are shared permanent tables. Two users refreshing at the same time may mix results (risk).
  - SQL is built by string concatenation; the values are ids and dates.
  - The group list is cleared on the worker thread while the adapter is attached.
- **PDF and print:**
  - The PDF file gets a double `.pdf.pdf` extension.
  - The "Recpt" column repeats the paid-on date when `Receno` = 0.
  - The OF PDF includes interest that has already been paid.
  - **Print List** skips the first row of every page after page 1 (0-based rows 23, 46, …). The offset is set to `i*23` and then `drawPage` pre-increments it (checked in smali); the saved PDF path is continuous.
  - Print defaults to A4 portrait, but rows are laid out 782 pt wide, so the right-hand columns are clipped unless the user chooses landscape.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `allfinancedailypattinew_adapter` | RecyclerView adapter (Filterable) | `dailypattinew_act` | Row `allfinancedailypattirow`: Particulars, LeftSide, RightSide, TransCount (`##,##,##0`; blank when 0). Colours by Sno: 1/9 Navy (#000080), 3/12 `nipcolor` (#DC0000), 6/13 DarkOliveGreen (#556B2F), 7/14/17 blue (−16776961 = 0xFF0000FF). Other rows never reset the colour, so a recycled holder can keep it. Click → `ContactsAdapterListener.onpattirow_selected(dailypattinew_data)`. Particulars filter exists but is unused. |
| `allfinancedailypattiadapter` | RecyclerView adapter (Filterable) | `allfinancedailypatti.PlaceholderFragment` | Row `allfinancedailypattirow`: Particulars (+ " (TransCount)" for SNo 1, 2, 3, 4, 8, 9, 18, 19), LeftSide / RightSide as text (blank on null), count column GONE. SNo 14 blue, 6 `warningcolor` (#FF4E0C), 12 `nipcolor`. Click → `onContactSelected(allfinancedailypattidata)`. Particulars filter. |
| `dailypatti_nadapunippuphakki_adapter` | RecyclerView adapter (Filterable) | `dailypatti_nadapunippuphakkilist_act` | Row `dailypattinew_nadapunippuphakki_row`: LedgerName, collectable, Balance, Collected, SubAreaName, ReportIdentification, MCLoanDate/ClosingDate. Click → `onContactSelected`. Name filter is case-insensitive and works. |
| `dailypatti_nadapunippucollection_adapter` | RecyclerView adapter (Filterable) | `dailypatti_nadapunippucollectionlist_act` | Row `dailypattinew_nadapunippucollection_row`: LedgerName, collectable, PrevCollection, Collection, SubAreaName, ReportIdentification, MCLoanDate/ClosingDate. Click → `onContactSelected`. Filter bug: upper-cased query compared with the lower-cased name. |
| `dailypatti_varakadanillapu_adapter` | RecyclerView adapter (Filterable) | `dailypatti_varakadanillapulist_act` | Row `dailypattinew_varakadanillapu_row`: LedgerName, collectable, collected, Pending, SubAreaName, ReportIdentification, MCLoanDate/ClosingDate/ClosedDate. Same filter bug. |
| `dailypatti_kadanpatru_adapter` | RecyclerView adapter (Filterable) | `dailypatti_kadanpatrulist_act` | Row `dailypattinew_kadanpatru_row`: Party, Loan, Dues, Interest, Mahimai, SubAreaName, LT, MCLoanDate. Filter on Party with the same bug. |
| `pattidetails_loans_adapter` | RecyclerView adapter (Filterable) | `pattidetails_loans_act` | Row `pattidetails_loans_row`: name, loan (collectable), "TC" = Paid and party no. = CWLedgerId (both shown as Double text, e.g. "12.0"), due = InsAmount, and balance (special access 5 only); status hidden. Sub-area is never bound, so the layout placeholder "Gandhigramam" stays. The photo is a Glide circle crop from `softsettingsvalue.partiesimagepath` + Imagefilename when `enablepartyimageloading` is on (fallback `ic_action_aboutus`), otherwise hidden. Sets `globalvars.iscollectionamountisbeingdisplayed=false`. Filter matches name, closing-date or loan-date text (case-sensitive). |
| `dailypattinew_data` | Model (Serializable) | `dailypattinew_act`, its adapter | `sno`, `Particulars`, `LeftSide`, `RightSide`, `TransCount` (int); `filterstr` (unused). |
| `dailypatti_nadapuandnippuphakki_data` | Model (Serializable) | P1 result, balance list | `MCLoanId`, `MCLoanDate`, `ReportIdentification`, `LedgerName`, `MCLoanAmountCollectable`, `Collected`, `Balance`, `ClosingDate`, `IsNIP`, `SubAreaNo`, `SubAreaName` (dates `java.sql.Date`, amounts int). |
| `dailypatti_nadapuandnippucollection_data` | Model (Serializable) | P2 result, collection list | `MCLoanId`, `MCLoanDate`, `ReportIdentification`, `LedgerName`, `MCLoanAmountCollectable`, `PrevCollection`, `Collection`, `ExtraCollection`, `ClosingDate`, `IsNIP`, `CollCount`, `SubAreaNo`, `SubAreaName`. |
| `dailypatti_varakadanillapu_data` | Model (Serializable) | P3 result, bad-debt list | `MCLoanId`, `MCLoanDate`, `ClosingDate`, `ClosedDate`, `ReportIdentification`, `LedgerName`, `MCLoanAmountCollectable`, `collected`, `Pending`, `IsNIP`, `SubAreaNo`, `SubAreaName`. |
| `dailypatti_kadanpatru_data` | Model (Serializable) | P4 result, new-loans list | `SNo`, `SubAreaName`, `SubAreaNo`, `MCLoanDate`, `MCLoanId` (never set), `Party`, `LT`, `Loan`, `Dues`, `IntRate`, `Interest`, `Mahimai`. |
| `allfinancedailypattidata` | Model (Gson target) | `allfinancedailypatti` | `FinId`, `SNo` (int), `Particulars`, `LeftSide`, `RightSide`, `TransCount` (Integer). |
| `pattidetails_loansdata` | Model | `pattidetails_loans_act` | `MCLoanId`, `LedgerId`, `CWLedgerId`, `SubAreaNo`, `MCLoanAmountCollectable`, `Balance`, `Paid` (Double); `SubAreaName`, `LedgerName`, `Imagefilename`, `MCLoanDate`, `ClosingDate` (String); `InsAmount` (Integer). |

The drill-down adapters format their date columns with `DateFormat.format("dd-MM-yy", …)`, which throws on a null date, so they assume the procedures never return NULL dates. The meeting report's helpers (`meetingreports_expandablelist_adapter`, `meetingreport_data`, `meetingreport_partylevel_data`, `meetingreport_loanlevel_data`) belong to G14.

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_dailypattinew.xml` | `dailypattinew_act` | CoordinatorLayout `main`; DarkGoldenrod TextView `dailypattinew_financenameid` (white text, "FINANCE NAME"); RecyclerView `recycler_view` |
| `allfinancedailypattirow.xml` | `allfinancedailypattinew_adapter`, `allfinancedailypattiadapter` | Selectable row with black TextViews `afdp_particularsid` ("Particulars"), `afdp_leftsideid` ("Leftside"), `afdp_rightsideid` ("right"), `afdp_countid` ("Count") |
| `activity_nadapunippuphakkilist.xml` | `dailypatti_nadapunippuphakkilist_act` | CoordinatorLayout `main`, AppBarLayout + Toolbar `toolbar`, `include customersearchlist` (RelativeLayout with RecyclerView `recycler_view`) |
| `dailypattinew_nadapunippuphakki_row.xml` | `dailypatti_nadapunippuphakki_adapter` | Line 1: Party, Loan, Balance, Collected; line 2: Area, Loan Method, "LDate/CDate" (`dtnew_nadapunippuphakkirow_*`) |
| `activity_nadapunippucollectionlist.xml` | `dailypatti_nadapunippucollectionlist_act`, `dailypatti_kadanpatrulist_act` | CoordinatorLayout `main`, AppBarLayout + Toolbar, `include customersearchlist` |
| `dailypattinew_nadapunippucollection_row.xml` | `dailypatti_nadapunippucollection_adapter` | Line 1: Party, Loan, Prev Collection, Collection; line 2: Area, Method, "LDate/CDate" (`dtnew_nadapunippucollectionrow_*`) |
| `activity_varakadanillapulist.xml` | `dailypatti_varakadanillapulist_act` | CoordinatorLayout **without** `@id/main` (cause of the crash), AppBarLayout + Toolbar, `include customersearchlist` |
| `dailypattinew_varakadanillapu_row.xml` | `dailypatti_varakadanillapu_adapter` | Line 1: Party, Loan, Collected, Pending; line 2: Area, Loan Method, "LDate/CDate/Closed on" (`dtnew_varakadanillapurow_*`) |
| `dailypattinew_kadanpatru_row.xml` | `dailypatti_kadanpatru_adapter` | Line 1: Party, Loan, Dues, Interest, Mahimai; line 2: Area, Loan Method, "LDate" (`dtnew_kadanpatrurow_*`) |
| `activity_allfinancedailypatti.xml` | `allfinancedailypatti` | CoordinatorLayout `main`, AppBarLayout + Toolbar (`app_name` "FinSoft II" / "FinSoft"), ViewPager `container` |
| `fragment_allfinancedailypatti.xml` | `allfinancedailypatti.PlaceholderFragment` | ConstraintLayout: TextView `section_label` ("asdf" placeholder), RecyclerView `afdp_recycler_view` |
| `activity_customersearch.xml` | `pattidetails_loans_act` (shared generic list layout) | CoordinatorLayout `main`, Toolbar, SwipeRefreshLayout `refreshLayout` → RecyclerView `recycler_view`, FAB `fab` (`ic_input_add`; hidden here), ProgressBar `cs_pbarid` (gone), hidden TextView |
| `pattidetails_loans_row.xml` | `pattidetails_loans_adapter` | ImageView `pattidetailsloansrow_thumbnail`; party no. (red) + "." + party name; sub-area ("Gandhigramam" placeholder); loan, balance (orange), status (DarkGoldenrod), "TC"; separate due column |
| `activity_meetingreport.xml` | `meetingreport` | ConstraintLayout `main`: DarkBlue strip `meetingreports_includedfinanceinfoid`, ExpandableListView `lvExp`, DarkBlue strip `meetingreports_lastrefreshinfoid`; ProgressBar `meetingreport_pbarid` and TextView `meetingreport_progresstextid` ("progress"), both gone by default |

---
[← brain.md](../../brain.md) · Module 8.11 of 20 · Previous: [8.10 Accounts reports: trial balance, balance sheet, P&L, month-wise P&L](10-accounts-reports-trial-balance-balance-sheet.md) · Next: [8.12 Thittam (collection plan) and weekly analysis reports](12-thittam-collection-plan-weekly-analysis-reports.md)
