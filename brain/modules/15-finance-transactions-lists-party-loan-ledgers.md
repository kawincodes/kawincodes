# 8.15 Finance transactions lists, party/loan ledgers and loan & deposit analysis reports

[← brain.md](../../brain.md) · Module 8.15 of 20 · Previous: [8.14 ST Loans and OF Loans (interest-bearing loan books)](14-st-loans-of-loans-interest-bearing.md) · Next: [8.16 Admin masters: employees, branch employees, access control, loan methods and finance lines](16-admin-masters-employees-branch-employees-access.md)

This module holds the screens behind the drawer's **ST Loans** and **OF Loans** groups and the partner masters. ST loans (TTID 3, interest TTID 4) and OF loans (TTID 5, interest TTID 6) are two separate interest-bearing loan books. Each loan belongs to a party (`PartyLedgerId`), a finance/line (`FinId`) and a recommending partner (`RecPartnerId`). The module covers:
- the ST/OF loan list (`fintrans_list`), read from the local SQLite mirror;
- the generic party/partner list and picker (`parties_list`);
- the finance-to-partner mapping editor (`financewisepartners_list`);
- one parameter screen (`partyledgers_parameters`) that leads to a party's loan-account statement (`partyledgers_list`) or the loan-ledger register (`loanledgers_list`);
- the 6-page **Analysis Reports** pager (`loananddepositanalyisreport`).

Every figure comes from SQL Server stored procedures (`Rep_*`, `RPT_*`, `STLoanLedgers`, `GetSTLoanPartyLedgers`, …), called by string-built `exec` statements on the selected-branch DB. Apart from showing the stored daily rate × 30 in the loan list, the app does no arithmetic: it formats rows, filters them, and draws print/PDF/share output with Android `PdfDocument`. A partner-wise "loans & performance" (interest-pending) pager also exists in code, but its activity is missing from the manifest, so it cannot open. `parties_list` crashes for every caller except **Partner details** (see that screen).

## ST / OF Loans list — `fintrans_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` (own Toolbar), edge-to-edge; implements `fintranslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_partieslist.xml` (also used by `parties_list` and `customers_list`) + row `stloanslist_row.xml`; menu `menucustomersearchlist` |
| **Opened from** | Drawer ST Loans → "ST Loans" (`nav_stloans`, extra `ttid`=3). Drawer OF Loans → "OF Loans" (`nav_ofloans`, `ttid`=5). |
| **Opens** | FAB → `stloans` (ttid 3) or `ofloans` (ttid 5) with `ttid`, `actiontype`=7010 (new loan; the target calls `preparescreenfornewdeposit()`) and an empty `transdata` (`fintrans_data`), forResult 7010. Row tap → the same screen with `transdata`=row, `calltype`=2 and `actiontype`=7011 (open the existing party's loan; the target loads it with `ASYNCindividualloandetails`), forResult 100. `ttid` is not passed on a row tap. Both codes are bare literals in the code. |
| **Data** | Local SQLite only: `tblSTLoans` LEFT JOIN `STLoanStatusforAPP`, or `tblOFLoans` LEFT JOIN `OFLoanStatusforAPP`, each joined to `tblLedgers`. `FinanceDetails` supplies the finance name per row (`defaulttables.fetchfinancename`). No server call. |

**Purpose.** Lists every ST (or OF) loan in the local mirror, newest first. From here the user opens a loan or adds a new one.

**UI.**
- Toolbar title "ST Loans" on a DarkGoldenrod bar, or "OF Loans" on a DarkCyan bar. Without a ttid the title stays "Parties".
- Each row (`stloanslist_row`) shows:
  - party name and AddLine1;
  - "`<FinMailingName>-<FWUDSTLoanId|FWUDOFLoanId>`" (finance plus the finance-wise user loan number);
  - `Status` from the status table, e.g. "Live";
  - loan date as `dd-MM-yyyy`;
  - amount formatted `##,##,##,##0`;
  - interest rate = `…IntRateinDM × 30` to 2 decimals, i.e. the stored daily rate shown as a 30-day rate.
- ST rows use goldenrod text; OF rows use dark cyan / cadet blue.
- The FAB "+" (`partieslist_addnewpartyfabid`) is always visible.

**Menu / actions.**
- Search (SearchView) filters on party name or AddLine1, case-sensitive `contains`.
- "Rearrange Parties" and "Show Parties in MAP" come from the shared menu but have no handler, so they do nothing.
- Up → back. Back first closes an open SearchView.

**How it works.**
1. `onCreate` reads `ttid`, colours the action bar, opens SQLite `venusmicrofinv005`, sets up the RecyclerView and calls `fetchdata_stloans()` (ttid 3) or `fetchdata_OFLoans()` (ttid 5).
2. The ST query: `select b.LedgerId,b.LedgerName,b.AddLine1,b.AddLine2,b.City,a.STLoanId,a.FWUDSTLoanId,a.FinId,a.STLoanAmount,a.STLIntRateinDM,a.TransDate,c.Status from tblSTLoans a left join STLoanStatusforAPP c on a.STLoanId=c.STLoanId , tblLedgers b where a.PartyLedgerId=b.LedgerId order by a.STLoanId desc`. The OF query is the same on `tblOFLoans`/`OFLoanStatusforAPP` (`OFLoanId`, `FWUDOFLoanId`, `OFLoanAmount`, `OFLIntRateinDM`). Each row becomes a `fintrans_data`. The amount is read with `getInt`, so any fraction is dropped.
3. `onActivityResult` re-runs the fetch for any request or result code. A loan saved in `stloans`/`ofloans` therefore appears at once, because those screens also insert into `tblSTLoans`/`tblOFLoans` and the status tables (Status "Live").

**Functions.**
- `onCreate()`: setup, title and colour by ttid, first fetch, FAB → new-loan intent.
- `fetchdata_stloans()` / `fetchdata_OFLoans()`: run the local SQL above into `fintrans_dataList`.
- `onTransactionselected(transdata, pos)`: opens the existing loan (actiontype 7011, calltype 2).
- `onthumnailclicked()`: empty.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: SearchView wiring; Up (16908332 = `android.R.id.home`) → back.
- `onActivityResult()`: reloads the list. `onPostResume()`: super only.
- `whiteNotificationBar()`: white status bar with dark icons (flag 8192).

**Rules & edge cases.**
- No role or special-access checks; the FAB is always shown.
- Data is only as fresh as the local mirror. `tblSTLoans`/`tblOFLoans` and the status tables (server procs `GetSTLoansStatus`/`GetOFLoansStatus`) are loaded during registration/OTP sync (`otpverificationact`). Saves in `stloans`/`ofloans` append to them. A branch switch (`refreshorreloadbrancheslist`) clears them.
- The inner join drops loans whose party is not in local `tblLedgers`.
- The adapter opens the SQLite DB on every row bind.
- The filter throws an NPE when a row's AddLine1 is null and its name does not match.

## Parties / Partners list and party picker — `parties_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`; implements `partieslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_partieslist.xml` + row `partieslistrow.xml`; menu `menucustomersearchlist` |
| **Opened from** | **Drawer:** "ST Loans Int" (`nav_stloanint`: `screentype` 21, `ttid` 4, `requestcode` 9002 = `Interestdetails_AccessRequest`); "OF Loans Int" (`nav_ofloanint`: screentype 21, ttid 6, rc 9002); "Partner details" (`nav_partnerdetails`: screentype 22, rc 3001 = `Partnerdetails_AccessRequest`, `menuname`="nav_partnerdetails"). The drawer hides that item by default and shows it only if the role's `tblSalesDetailsforAndroid` row says ShowState.<br>**As a picker (forResult):**<br>• `stloans`: rc 7002 "Party Recomendation" (screentype 21, `finid`); rc 7001 partner recommendation (screentype 22, `finid`); rc 7004 "Select Party" (screentype 21, finid 1).<br>• `ofloans`: rc 7001 and rc 7004.<br>• `partyandpartnerdetails`: rc 7032 "Select Master Party".<br>• `partyledgers_parameters`: rc 7014/7013 "Select Party" (screentype 21) or rc 7015 "Select Partner" (screentype 22), plus `ttid`, forResult 21. |
| **Opens** | **FAB** → `partyandpartnerdetails` to add a party/partner (`ledgerdata`=empty `partydetails_data`, `screentype`, `actiontype`=9003, forResult 9003).<br>**Row tap:**<br>• rc 9001 → `partyandpartnerdetails` (screentype 21, actiontype 9004, forResult 9004);<br>• rc 3001 → `partyandpartnerdetails` (screentype 22, actiontype 9004, `menuname`), only if the role has ViewState;<br>• rc 9002 + ttid 4 → `stloaninterestcalculationandpayments` (`ledgerdata`, `ttid`);<br>• rc 9002 + ttid 6 → `ofloans_interestcalculationandpayments`;<br>• picker codes → `setResult(1, …)` and finish. |
| **Data** | **Actually run:** remote selected-branch DB (`globalvars.selbranchconnString`), `tblLedgers where Screentype=22 order by LedgerId`.<br>**Dead code `fetchdata()`:** local `tblLedgers`, `tblSTLoans`, `tblOFLoans`, `tblFinwisePartners`.<br>**Access rights:** `globalvars.salesdetailsforandroid_datalist`, loaded from local `tblSalesDetailsforAndroid` per user role. |

**Purpose.** A generic party/partner list. From the drawer it is meant to be:
- the **Partner** master (view or edit partners, add new ones);
- the start of **ST/OF loan interest collection**: pick a party, then open its interest screen.

Other screens use it as a selection list that returns the chosen ledger.

**UI.**
- Toolbar title:
  - "Parties" by default or for screentype 21; "Partners" for screentype 22.
  - rc 9002/9013/7014: "ST Loan Parties" (goldenrod bar) when ttid 4, "OF Loan Parties" (dark cyan bar) when ttid 6.
  - rc 7002 "Party Recomendation"; rc 7032 "Select Master Party"; rc 7001 "Partner Recomendation" (also reads `finid`).
- Rows (`partieslistrow`): name, AddLine1, AddLine2, City, MobileNo1.
- The FAB "+" is visible only when the role's AddState is set or `globalvars.isFullAccessAllowed`. The code first hides it for rc 9002/9013/7015, but the AddState check that follows overrides that.

**Menu / actions.**
- Search matches the upper-cased name, or `AddLine2.contains(<raw query>)`.
- "Rearrange Parties" and "Show Parties in MAP" have no handler.

**How it works.**
1. `onCreate` reads `screentype`, `requestcode`, `menuname`, `ttid` and `finid`, and sets the title. It then picks `curitemaccesslimits`: the `salesdetailsforandroid_data` whose `mnuname` equals `menuname`, ignoring case. If `isFullAccessAllowed`, it forces Add/Delete/View/Update/Show to true.
2. `ASYNCRefreshpartnerslist` makes the window untouchable (flag 16) and shows the progress bar. It connects over JDBC and runs `Select LedgerId, CWLedgerId, LedgerName, GroupId, …, MaximumAmountAllowed,isactive from tblLedgers where Screentype=22 order by LedgerId`, mapping all columns into `partydetails_data`. The JADX constant `HttpHeaders.AGE` is just the column name "Age". On an exception it shows an "Error Info" dialog with "`<line>.<message>`", and OK closes the screen.
3. `onPartyselected` dispatches by requestcode, as listed under Opens. Picker results use resultCode 1:
   - 7001, 7002, 7032, 9013 → `displaymember`=LedgerName, `valuemember`=LedgerId;
   - 7037 → the same plus `ledgerdata`;
   - 7004 → the same plus `receivedparty`;
   - 7014, 7015 → `displaymember`="LedgerName, AddLine1", `valuemember`=LedgerId.
4. `onActivityResult` updates the list:
   - (9003, 9003): inserts the returned `receivedparty` at the top;
   - (9004, 9004): replaces the row with the same LedgerId and scrolls to it;
   - (9004, 9005): removes that row (the party was deleted).
5. The intended per-caller filters sit in `fetchdata()`, which is never called:
   - rc 7015/9001/3001 → `ScreenType=<screentype>`;
   - rc 9002/9013/7014 → parties that have loans, `LedgerId in (select PartyLedgerId from tblSTLoans)` for ttid 4 or `tblOFLoans` for ttid 6;
   - rc 7001 → partners active for the finance, `Ledgerid in (select PartnerLedgerId from tblFinwisePartners where isActive=1 and FinId=<finid>)`;
   - rc 7002 → ScreenType 21;
   - rc 7032, 7037 and the default → ScreenType 21 or 22. Rc 7037 also hides the FAB.

**Functions.**
- `onCreate()`: extras, title and colour, access-limit lookup, FAB visibility, starts `ASYNCRefreshpartnerslist`.
- `ASYNCRefreshpartnerslist.doInBackground` / `onPostExecute` / `onPreExecute` / `onProgressUpdate`: remote partner load, error dialog, UI lock and unlock.
- `fetchdata()`: dead local loader with the per-requestcode SQL above.
- `onPartyselected(ledgerdata, pos)`: routing and picker results.
- `onActivityResult()`: insert, replace or remove a row after the detail screen returns.
- FAB lambda: new party or partner. `onCreateOptionsMenu()` / `onOptionsItemSelected()`: search. `handleOnBackPressed()`: closes search first. `whiteNotificationBar()`.

**Rules & edge cases.**
- **Crash:** `curitemaccesslimits` stays null when no `menuname` extra is passed, and every caller except `nav_partnerdetails` omits it. `String.equalsIgnoreCase(null)` is false, so `onCreate` throws a NullPointerException on `setAddState`/`getAddState`. Smali confirms there is no try/catch. As built, "ST Loans Int", "OF Loans Int" and every picker use (the ST/OF loan entry party/partner pickers, the master-party picker, the party picker on the ledgers parameter screen) crash when opened.
- Even when the screen opens, it always shows the remote **partner** list (ScreenType 22), never the requestcode-specific local list.
- An rc 3001 row tap does nothing without ViewState.

## Finance-wise Partners — `financewisepartners_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`, edge-to-edge; implements `financewisepartners_adabpter.financelistadapterlistner` |
| **Layout(s)** | `activity_financewisepartners.xml` + row `financewisepartners_row.xml`; menu `menuemptylist` (its search item is invisible) |
| **Opened from** | Drawer Admin → "Finwise partners" (`nav_finwisepartners`), no extras. The item is hidden unless the role's menu rights show it. |
| **Opens** | `spinnersearch_list` (finance picker: `title`="Finance", `searchlist`=`defaulttables.fetchfinancelist(db)` from local `FinanceDetails`; forResult 7005 = `loan_getfinancename`) |
| **Data** | Remote selected-branch DB: `exec getfinwisepartners @FinId=<id>`; INSERT/UPDATE/DELETE on `tblFinwisePartners`. Local SQLite `tblFinwisePartners` is mirrored on save. Rights come from `salesdetailsforandroid_datalist` entry "nav_finwisepartners". |

**Purpose.** Decides which partners belong to which finance (line), and whether each link is active. These links feed the partner-recommendation pickers on ST/OF loan entry.

**UI.**
- Title "Finance wise Parnters" (`R.string.finance_wise_parnters`; the Tamil file has the same text).
- Toolbar holds a "Finance" label and a read-only EditText (`financewisepartners_financenameid`); tap it to pick a finance.
- Each row has two CheckBoxes:
  - Partner selected, with the partner name as its text. It is disabled when `RecTransCount` > 0, i.e. the partner already has transactions in this finance.
  - "Active".
- "UPDATE" button (`financewisepartners_savebuttonid`), shown only with the role's UpdateState (or full access). A progress bar shows during loads.

**Menu / actions.**
- The finance field opens the picker.
- UPDATE validates and saves.
- Up → back.

**How it works.**
1. `onCreate` finds `curitemaccesslimits` for "nav_finwisepartners" (all rights forced on if full access) and shows or hides UPDATE from UpdateState.
2. When the finance picker returns (rc 7005, resultCode 1), the screen stores `selectedfinanceid`, shows `displaymember` and runs `ASYNCGetFinancelist`: `exec getfinwisepartners @FinId=<id>`. That returns every partner with `IsSelected`, `PartnerLedgerId`, `UID` (the link id), `LedgerName`, `Active` and `RecTransCount`.
3. UPDATE checks that at least one row is both selected and active. Otherwise it shows the alert "Incompeted Data..." / "Minimum one Partner should be selected and active."
4. `ASYNCSaveFinancewisepartners` walks every row:
   - selected with no UID → `INSERT INTO tblFinwisePartners (PartnerLedgerId,FinId,isActive) output inserted.[UID] values(?,?,?)`, then a local SQLite insert;
   - selected with a UID → `update tblFinwisePartners set PartnerLedgerId=?,FinId=?, isActive=? where UID=?`, then a local update;
   - not selected with a UID → `delete from tblFinwisePartners where UID=?`, then a local delete.
   The activity then finishes without any confirmation.

**Functions.**
- `onCreate()`: access check and the finance-picker click.
- `onActivityResult()`: receives the finance, loads its partners.
- UPDATE lambda: validation, then save.
- `showincompletedataalert()`.
- `onfinancewisepartnerselected()` / `onactivechecked()`: copy the checkbox state into the list by PartnerLedgerId.
- `ASYNCGetFinancelist.*`: load; re-applies UpdateState to the button.
- `ASYNCSaveFinancewisepartners.*`: the save loop above.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`, `handleOnBackPressed()`, `whiteNotificationBar()`.

**Rules & edge cases.**
- **Bug:** after the INSERT…OUTPUT, the code calls `getInt("UID")` on the ResultSet without `next()` (confirmed in smali). jTDS then throws "no current row". The server row is already inserted (autocommit), but the local mirror is not updated, the rest of the loop is skipped, and `ErrorText` is set but never shown. The screen closes as if the save had worked.
- Load errors are swallowed too, leaving an empty list.
- An NPE is possible if the role has no "nav_finwisepartners" access row.

## ST/OF loan accounts & ledgers — parameters — `partyledgers_parameters`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default `AppTheme` (action bar), edge-to-edge; implements `financelist_selectable_adabpter.financelistadapterlistner` |
| **Layout(s)** | `activity_loanpartyledgers_parameters.xml`. Finance rows use `financelist_withselectablecheckbox_row` through `financelist_selectable_adabpter` (both documented in another module). |
| **Opened from** | **Drawer, ST Loans group:**<br>• "ST Loans Parties" (`nav_PartyLedgersSTLoans`: `ttid` 4, `requestcode` 7014);<br>• "ST Loans Ledgers" (`nav_LoanLedgersSTLoans`: ttid 3, rc 7027 = `parametersselection_requestcodeforloanledgers`).<br>**Drawer, OF Loans group:**<br>• "OF Loans Parties" (`nav_PartyLedgersOFLoans`: ttid 6, rc 7014);<br>• "OF Loans Ledgers" (`nav_LoanLedgersOFLoans`: ttid 5, rc 7027).<br>**Other:** `meetingreport` Refresh menu (rc 7013, the report's ttid). No other caller of `meetingreport` was found, so this path looks unreachable. |
| **Opens** | **Pickers:** `parties_list` ("Select Party" with screentype 21 for rc 7013/7014; "Select Partner" with screentype 22 for rc 7015; plus `requestcode`, `finid` 1, `ttid`; forResult 21). This crashes, see `parties_list`.<br>**Reports:**<br>• rc 7014 → `partyledgers_list`;<br>• rc 7027 → `loanledgers_list`;<br>• rc 7013 → `meetingreport` (then finish);<br>• rc 7015 → `partnerwiseloansandperformancehome` (then finish). That activity is missing from the manifest and no caller sends 7015.<br>**Dead branches:** rc 6018/6019/6020 → `tbbspl_list`, rc 6021 → `monthwisepandl`; no caller passes these codes.<br>**Output:** system print, PDF viewer and share chooser. |
| **Data** | Local `FinanceDetails` (`select FinId, FinMailingName from FinanceDetails`); `SubAreaDetails` is created if missing (`createsubareadetails`). Remote selected-branch DB, rc 7027 only: `exec STLoanLedgers @FromDate='<from>', @ToDate='<to>' , @SelectedFinanceIds='<ids>'` or `exec OFLoanLedgers @CFD='<globalvars.accyearfromdate>' , @SelectedFinanceIds='<ids>'`. Globals: `accyearfromdate`/`accyeartodate`, `todaydate`. |

**Purpose.** Pick the finances (lines), the period and, for party ledgers, the party. Then open either one party's ST/OF loan-account statement or the register of all ST/OF loans and returns for the period.

**UI** (labels are hard-coded; the Tamil ones are noted).
- "Select Finances" with a RecyclerView of finance checkboxes; none are pre-ticked.
- "Select Period" with two EditTexts that open DatePickers when tapped:
  - "ஆரம்ப தேதி" (Start date), default = financial-year start `globalvars.accyearfromdate`;
  - "நிறைவு தேதி" (End date), default = financial-year end `globalvars.accyeartodate`.
- "Select Party" row (`partyledgerparameters_partyselectionid`) with a read-only party EditText that opens the picker when tapped.
- "SHOW" button and a progress bar.

**Variations by requestcode** (titles come from the action bar):
- **7014**: "ST Loan Accounts" (ttid 4) or "OF Loan Accounts" (ttid 6). All fields are visible.
- **7027**: "ST Loans" (ttid 3) or "OF Loans" (ttid 5). The period is visible; the party row is hidden.
- **7013**: "STL Interest Pending" / "OFL Interest Pending". Party row and start date are hidden. End date = last day of the previous month, labelled "Interest Upto".
- **7015**: "Partnerwise Loans" (ttid 3) or "OFL Interest Pending" (ttid 5). The label becomes "Select Partner", the start date is hidden, the end date is the previous month-end ("Interest Upto").
- **6018 / 6019 / 6020 / 6021** (dead branches):
  - 6018 "Balancesheet" and 6020 "Profit and Loss Statement": start = today, labelled "Report Date"; end = previous month-end, labelled "Int Colleted Upto".
  - 6019 "Trailbalance".
  - 6021 "Monthwise P&L", with every input hidden.

**Menu / actions.**
- SHOW validates, then routes by requestcode.
- For rc 7027 a "Print Options" dialog ("Direct Print" / "Show as PDF" / "Share") pops up later on this screen.

**How it works.**
1. `onCreate` loads the finance list (`fetchemployeesandotherdetails()`), sets the default dates and applies the per-requestcode layout.
2. Finance ticks update `financemaster_data.isselected` (`onfinancechecked`). The party picker returns `displaymember` / `valuemember` with resultCode 1, which become the party text and `partyledgerid`.
3. SHOW runs these checks in order:
   - no finance ticked → alert "Incomplete data..." / "Finance not Selected";
   - otherwise it builds CSV strings `selectecdfinanceids` (FinIds) and `selectedfinancenames` (FinMailingNames);
   - rc 7014 and 7015 also require a party ("Party is not selected..") and repeat the finance check ("Finance is not selected..").
4. Routing:
   - **7014** → `partyledgers_list` with `partyledgerid`, `requesttype`=7030 (`individualloandetailsrequestfromloanaccountsreportscreen`), `fromdate`, `todate`, `ttid`, `requestcode`=9013 (ignored by the target), `selectecdfinanceids`, `selectedfinancenames`.
   - **7013** → `meetingreport` with `fromdate`, `requestcode`, `todate`, `ttid`, and the finance ids and names.
   - **7015** → `partnerwiseloansandperformancehome` with `partnerledgerid`, `todate`, `ttid`, finance ids and names, `requestcode`.
   - **7027** → starts `ASYNC_GetInterestPendingList` and, at the same moment, `loanledgers_list` with the same parameters. The task (misnamed: it loads loan ledgers) fills `loanledgers_data` from `STLoanLedgers` or `OFLoanLedgers`. It sets `ReportHeader` = "`<fin names>: STLoan Ledgers as on <todaydate>`" (or "…OF Loan Ledgers as on…"), computes pages at 23 rows each, then calls `showprintandshareoptions()`. That dialog appears on this screen behind `loanledgers_list` and is waiting when the user comes back.
5. Print options (rc 7027 data, A4 landscape 842×595):
   - **Direct Print**: `PrintManager` job "`<app_name> Document`".
   - **Show as PDF**: writes `<ReportHeader>.pdf` to `<external storage root>/venusmfc/` and opens it with ACTION_VIEW on a `file://` URI. The code's `setFlags(0x40000000)` = FLAG_ACTIVITY_NO_HISTORY (JADX shows `BasicMeasure.EXACTLY`) replaces the read-grant flag.
   - **Share**: ACTION_SEND with a `file://` URI, type `application/pdf`, chooser "Share Image". If no app can handle it, toast "Sharing tools have not been installed.".
   - Page layout: "Page N" top-right and ReportHeader top-left. Columns: S.No | Date (`dd-MM-yy`) | Party | No | IR (`0.00`) | Return | Balance.
     - Return is printed only when > 0. The "Balance" column holds the `Loan` value.
     - S.No, No and IR are blank on rows whose Party contains "Opening Balance" or "Closing Balance".
     - Footer "Printed @ dd-MM-yyyy, HH:mm:ss".

**Functions.**
- `onCreate()`: views, default dates, per-requestcode layout and titles, click handlers.
- Party lambda: opens the `parties_list` picker. Date lambdas / `onDateSet`: change the from/to dates.
- SHOW lambda: validation and routing (above). `showincompletedataalert(msg)`: OK-only alert.
- `fetchemployeesandotherdetails()`: loads the local finance list.
- `onActivityResult()`: receives the party. `onfinancechecked()`: tick state.
- `ASYNC_GetInterestPendingList.*`: loan-ledger fetch, then the print dialog.
- `showprintandshareoptions()`: the 3-option dialog.
- `printDocument()`, `createPdf()`, `sharePdf()`: output as above.
- `MyPrintDocumentAdapter.onLayout` / `onWrite` / `pageInRange`: print pagination.
- `drawPage_loanledgers(page, n)`: draws one page.
- `drawCenterwithgivenwidth()`: centred header text. `drawCenter()` and `drawCenterwithbox()` are unused.
- `whiteNotificationBar()`.

**Rules & edge cases.**
- `fromdate`/`todate` are the same `java.sql.Date` objects as `globalvars.accyearfromdate`/`accyeartodate`. The pickers mutate them in place (`setDate`/`setMonth`/`setYear`), so picking a date here changes the app-wide financial-year bounds for the rest of the session.
- The parameter-side OF query passes `@CFD` = financial-year start and ignores the chosen period. `loanledgers_list` calls `OFLoanLedgers` with `@FromDate/@ToDate`. One of the two signatures must be wrong; client code alone cannot tell which.
- PDFs here use `Environment.getExternalStorageDirectory()` and `file://` URIs, which may fail on Android 10+ scoped storage. Errors are only logged.
- Errors in the rc 7027 fetch are swallowed, with two outcomes:
  - If the connection itself fails, `loanledgers_dataList` is still null and `onPostExecute` throws an NPE (app crash).
  - A later SQL error leaves 0 rows, and the dialog's Direct Print then fails with "Page count is zero.".
- `singlevalueselection_data selectedfinancelist` is declared but never used.

## Party loan-account statement — `partyledgers_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`, edge-to-edge; implements `partyledgers_adapter.partyledgerlistlistner`. JADX could not fully decompile `drawPage`; it was checked against smali. |
| **Layout(s)** | `activity_partyledgerslist.xml` + row `partyledgers_row.xml`; menu `menuwithsearchandprintoptions` |
| **Opened from** | (1) `partyledgers_parameters` rc 7014 SHOW: `partyledgerid`, `requesttype` 7030, `fromdate`, `todate`, `ttid` 4/6, `selectecdfinanceids`, `selectedfinancenames`. (2) Loan row tap in `stloaninterestcalculationandpayments` / `ofloans_interestcalculationandpayments`: `loanordepositid`, `partyledgerid`, `requesttype` 7029 (`individualloandetailsrequestfrominterestcollectionscreen`), `selectedfinancenames` (the loan's finance), `partyname` (ignored), `loanno`, `ttid`. |
| **Opens** | Print service, PDF viewer, share chooser. Nothing else. |
| **Data** | Remote selected-branch DB:<br>• rc 7030: `exec GetSTLoanPartyLedgers` (ttid 4) or `exec GetOFLoanPartyLedgers` (ttid 6), each with `@FromDate='<from>', @ToDate ='<to>', @PartyLedgerId =<id> , @SelectedFinanceIds='<ids>'`;<br>• rc 7029: `exec GetSTLoanPartyLedgersforselectedLoan @STLoanId=<id>` or `exec GetOFLoanPartyLedgersforselectedLoan @OFLoanId=<id>`.<br>Hidden delete: `DeleteSelectedLoanandRelatedEntries @STLoanId=?` on `globalvars.branchconnString`, plus local `delete from tblSTLoans where STLoanId=?`. Local `tblLedgers` supplies the party name (`fetchstledgername`). |

**Purpose.** A ledger statement of one party's ST/OF loan account: each loan given, each interest posting and each repayment. It covers either a period across the selected finances, or one specific loan.

**UI.**
- Toolbar title "ST Loan Accounts" (ttid 4) or "OF Loan Accounts" (ttid 6).
- Header:
  - "PARTY : <name>" from local tblLedgers;
  - "FINANCE : <finance names>";
  - "PERIOD : dd-MM-yyyy to dd-MM-yyyy" for rc 7030, or "Loan No : <loanno>" for rc 7029.
- Column strip "Particulars | Interest | Loan | Return", tinted goldenrod (ST) or dark cyan (OF). The Loan header text is set to "Loan".
- Rows (`partyledgers_row`): date `dd-MM-yyyy`, Particulars (`TransType`, e.g. "NEW LOAN"), Interest, Loan and Return. Amounts use `##,##,##,##0` and are blank when 0.
- A 25 dp delete icon (`partyledger_deleteloanpartybuttonid`, `ic_action_deletechitparty`) exists but is always GONE.

**Menu / actions.**
- Search filters on Particulars.
- **Direct Print**: A4 portrait print job.
- **Show as PDF**: writes `anarep<millis>.pdf` and opens it. With no viewer, toast "Error while opening the document".
- **Share**: ACTION_SEND.
- Up → back.
- Row tap does nothing (`onContactSelected` is empty).

**How it works.**
1. `onCreate` hides the delete button, reads the extras and builds `exeprocname` and the header by `requesttype` and ttid. It then starts `ASYNCGetAccountstransactionslist`.
2. The task runs `exec <exeprocname>`, adding the date, party and finance parameters for rc 7030. It maps the columns `Typeid`, `TransId`, `TransDate`, `TransType`, `Interest`, `Loan` (double) and `Repayment` into `partyledgers_data`. Errors are only printed.
3. On a menu tap, `ReportHeader` is set and pages are computed at 23 rows each:
   - rc 7029: "`Party: <name>'s Loan Statement, Loan No <n>`";
   - rc 7030: "`<name>'s Statement for the Period from dd-MM-yyyy to dd-MM-yyyy`".
4. The PDF is 595×842 (A4 portrait) and goes to `Documents/venusmfc/`. Each page has "Page N", the ReportHeader, and columns Date | Particulars | Interest | Loan | Return, with each amount printed only when > 0 (smali `if-lez`). The footer reads "Printed @ <currentDate>, <currentTime>", but those fields are never set in this class, so it prints "null, null". Share sends a FileProvider URI (`…clone.provider`) with MIME "text/csv" through the chooser "Share".
5. Hidden delete flow. The Tamil dialog is titled "தங்கள் கவனத்திற்கு.... " (For your attention) and says that deleting this loan and its related entries changes cash and bank balances and cannot be undone. Buttons are "வேண்டாம்" (Don't) and "அழிக்கவும்" (Delete). Delete runs `ASYNCDeleteselectedstloan`: on success it removes the local `tblSTLoans` row and shows "deleted..."; otherwise "error occurs..". OK closes the screen.

**Functions.**
- `onCreate()`: header, procedure selection, delete-button wiring, starts the load.
- `ASYNCGetAccountstransactionslist.*`: ledger fetch.
- `ASYNCDeleteselectedstloan.*`: server and local delete plus result dialog (unreachable).
- `onCreateOptionsMenu()`: search. `onOptionsItemSelected()`: header, pages, print/PDF/share.
- `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.*`, `drawPage()`, `drawCenterwithgivenwidth()`. `drawCenter()` and `drawCenterwithbox()` are unused.
- `onContactSelected()`: empty. `handleOnBackPressed()`, `whiteNotificationBar()`.

**Rules & edge cases.**
- The search filter upper-cases the query and compares it with the lower-cased text, so any query containing letters matches nothing.
- The hidden delete always passes `@STLoanId` and deletes from `tblSTLoans`, even for OF loans.
- The `requestcode` (9013) and `partyname` extras are ignored. The party name is always re-read from local `tblLedgers`, so it is blank if the party is not in the local mirror.
- Load errors are only printed, leaving an empty list.

## ST/OF loan ledgers register — `loanledgers_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar`, edge-to-edge; implements `loanledgers_adapter.partyledgerlistlistner`. JADX partial decompile of `drawPage`; checked against smali. |
| **Layout(s)** | `activity_loanledgerslist.xml` + row `loanledgers_row.xml`; menu `menuwithsearchandprintoptionsforloanledgers` |
| **Opened from** | `partyledgers_parameters` rc 7027 SHOW (from drawer "ST Loans Ledgers" or "OF Loans Ledgers"): `fromdate`, `requestcode` 7027, `todate`, `ttid` 3/5, `selectecdfinanceids`, `selectedfinancenames`. |
| **Opens** | Print service, PDF viewer, share chooser. |
| **Data** | Remote selected-branch DB: `exec STLoanLedgers @FromDate = '<from>', @ToDate = '<to>' , @SelectedFinanceIds='<ids>'` (ttid 3) or `exec OFLoanLedgers` with the same parameters (ttid 5). Local `qatledgers` (`fetchledgername`) is used only in header branches that never run. |

**Purpose.** A chronological register of every ST (or OF) loan given and every return (repayment) in the period, across the selected finances. It has opening and closing balance lines and can be filtered to loans only or returns only.

**UI.**
- Toolbar title "ST Loan Accounts" (ttid 3) or "OF Loan Accounts" (ttid 5).
- Header "FINANCE : <names>" and "PERIOD : dd-MM-yyyy to dd-MM-yyyy".
- Column strip: S.No | Date (ta "தேதி") | Party/No | IR | Return | Loan. The XML default "Balance" is overwritten with "Loan". The strip is tinted goldenrod or dark cyan.
- Rows (`loanledgers_row`):
  - SNO, shown only when it is non-zero and < 1000;
  - date `dd-MM-yyyy`;
  - party name and loan no, the no blank when 0;
  - IR formatted `0.000`;
  - Return, and Loan (shown under the Balance id), each blank when 0.

**Menu / actions.**
- Search filters on Party.
- **Direct Print**: A4 landscape.
- **Show as PDF**: `loanrep<millis>.pdf`.
- **Share**: the file name is ReportHeader with ":" removed.
- **Show Loans only**, **Show Returns only**, **Show Loans and Returns** (see step 3).
- Up → back.

**How it works.**
1. `ASYNC_GetLoanLedgers` fills both `loanledgers_dataList` (shown) and `Source_loanledgers_dataList` (master) from the columns `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan` and `Date`. It sets `ReportHeader`:
   - ST: "`<fin names>: STLoan Ledgers for the Period from <yyyy-MM-dd> to <yyyy-MM-dd>`";
   - OF: "`<fin names>: OF Loan Ledgers as on <todaydate>`".
   It also stamps the print date and time.
2. Row roles are inferred from client logic only; the procedure body is not in the APK:
   - row 0 = "Opening Balance";
   - SNO 1–999 = transactions;
   - SNO 1001 = totals;
   - SNO ≥ 1002 = "Closing Balance".
3. The filters rebuild the shown list from master index 1 onward, so the Opening Balance row is always dropped:
   - **Loans only** keeps rows with Loan > 0 and SNO < 1002, zeroes Return on the 1001 row, and renumbers SNO from 1.
   - **Returns only** keeps rows with Return > 0 and SNO < 1002, zeroes Loan on the 1001 row, and renumbers.
   - **Loans and Returns** copies every row from index 1.
4. PDF and print pages are drawn on a 792-wide landscape canvas, 23 rows per page:
   - columns S.No | Date (`dd-MM-yy`) | Party | No | IR (`0.00`) | Return (only when > 0) | Balance (the `Loan` value);
   - S.No, No and IR are suppressed on Opening/Closing Balance rows;
   - footer "Printed @ …";
   - files go to `Documents/venusmfc/` and open or share through FileProvider `…clone.provider` (share MIME "text/csv").

**Functions.**
- `onCreate()`: header, title and colour by ttid, starts the load.
- `ASYNC_GetLoanLedgers.*`: fetch and header.
- `onCreateOptionsMenu()`: search.
- `onOptionsItemSelected()`: output plus the three filters. Its 7029/7030 header code never runs here because `requesttype` is not passed.
- `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.*`, `drawPage()`, `drawCenterwithgivenwidth()`. `drawCenter()` and `drawCenterwithbox()` are unused.
- `onContactSelected()`: empty. `handleOnBackPressed()`, `whiteNotificationBar()`.

**Rules & edge cases.**
- The filters change the shared row objects (setReturn(0), setLoan(0), renumbered SNO). After "Loans only", "Loans and Returns" no longer shows the original totals or serial numbers.
- The search compares an upper-cased query with lower-cased text, so letters never match.
- `createPdf` starts ACTION_VIEW without checking for a viewer, so it crashes if none is installed.
- `sharePdf` uses a 595×842 portrait page for a 792-wide layout, so the IR, Return and Balance columns are cut off in shared PDFs.
- `exeprocname` ("STLoanLedgers" / "GetOFLoanPartyLedgers") is set but never used.

## Loan & Deposit Analysis Reports (host) — `loananddepositanalyisreport`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default `AppTheme` (DarkActionBar). Manifest label "aaaa", overridden by `setTitle("Analysis Reports")`. |
| **Layout(s)** | `ac_complex.xml` (ViewPager `pager`; its PagerTitleStrip is `gone`, so page titles never show) |
| **Opened from** | Drawer ST Loans → "ST Loan Analysis" (`nav_STLoanAnalysis`, `ttid` 3). Before starting, the drawer sets `globalvars.curanalysisreport = defaulttables.fetchanalysisreportsettings().get(0)`, the ST definition. Drawer OF Loans → "OF Loan Analysis" (`nav_OFLoanAnalysis`, `ttid` 5) uses `fetchanalysisreportsettingsforofloans().get(0)`. The `ttid` extra itself is never read. |
| **Opens** | Hosts the 6 fragments below in a `FragmentPagerAdapter` (`ImagePagerAdapter`). |
| **Data** | None directly. Report headings and SQL prefixes come from `globalvars.curanalysisreport` (`loananddepositanalysisreportsettings_data`), which `defaulttables` hard-codes. |

**Purpose.** Six swipeable portfolio analyses of the ST (or OF) loan book, as on the app's working date.

| Pos | Page title (hidden) | Fragment | ST heading / procedure | OF heading / procedure |
|---|---|---|---|---|
| 0 | FINANCE WISE ABSTRACT | `analysisrep_financewiseabstract_fragment` | "Financewise STLoan Analysis" / `Rep_FinWiseSTLoans` | "Financewise OFLoan Analysis" / `Rep_FinWiseOFLoans` |
| 1 | PARTNER WISE ABSTRACT | `analysisrep_partnerwiseabstract_fragment` | "Partnerwise STLoan Analysis" / `Rep_ShowPartnerWiseSTLoanBalance` | "Partnerwise OFLoan Analysis" / `Rep_ShowPartnerWiseOFLoanBalance` |
| 2 | FINANCE AND PARTNER WISE | `analysisrepfin_partnerwiseabstract_fragment` | "Finance and Partner Wise STLoan Analysis" / `Rep_ShowPartnerFinanceWiseSTLoanBalance` | "Finance and Partnerwise OFLoan Analysis" / `Rep_ShowPartnerFinanceWiseOFLoanBalance` |
| 3 | PARTY WISE | `analysisrepfin_partyrwiseabstract_fragment` | "Partywise STLoan Analysis" / `RPT_PartywiseSTLoans` | "PartyWise OFLoan Analysis" / `RPT_PartywiseOFLoans` |
| 4 | INT RATE DISTRIBUTION | `analysisrepfin_interestratedist_fragment` | "STLoan Int Rate Distribution Analysis" / `Rep_ShowSTLoanIntDistribution` | "OFLoan Int Rate Distribution Analysis" / `Rep_ShowOFLoanIntDistribution` |
| 5 | OVER ALL INT RATE DISTRIBUTION | `analysisrepfin_overallinterestratedist_fragment` | "Overall STLoan Int Rate Distibution Analysis" / `[Rep_ShowSTLoanIntDistributionOverAll]` | "Overall OFLoan Int Rate Distibution Analysis" / `[Rep_ShowOFLoanIntDistributionOverAll]` |

**Common mechanics** (all six pages):
- **Class & menu.** Each page extends `AbsListViewBaseFragment` → `BaseFragment`. `BaseFragment` calls `setHasOptionsMenu(true)` and inflates `partnerwiseperformance_menu`: "Direct Print" (`visible=false`, hidden), "Show as PDF" and "Share". Only the visible page's menu is active.
- **Layout.** Each page uses `fr_image_list`: heading TextView `fragmenttitleid`, RecyclerView `@android:id/list`, progress bar.
- **Query.** `onCreateView` runs `ASYNCGetFinancelist` on `globalvars.selbranchconnString`, executing `<SQL prefix>'<globalvars.todaydate>'`, e.g. `exec Rep_FinWiseSTLoans @RDate='2026-11-04'`. There is no date or finance selector: the report is "as on" the working date and covers every finance in the selected branch DB. Errors leave the list empty without a message.
- **When pages load.** The pager keeps neighbours alive, so opening the screen runs pages 0 and 1 together. Each later swipe runs the next page's procedure, and a page whose view was destroyed re-queries when it comes back.
- **Output.** "Show as PDF" writes `Documents/venusmfc/anarep<millis>.pdf` and opens it with ACTION_VIEW through FileProvider `…clone.provider`, without checking for a viewer. "Share" sends ACTION_SEND with MIME "text/csv" through the chooser "Share". Pages are A4 portrait 595×842 with 23 rows each, "Page N" top-right and the heading top-left.
- **Footer.** "Printed @ …" is printed only when a page fills. The date/time fields are never assigned in pages 0, 1, 2, 4 and 5, so it reads "Printed @ null, null".
- **Rows.** Row taps call an empty `onContactSelected`. The adapters' search filters are never used, because these pages have no SearchView.

**Functions.**
- `onCreate()`: sets the title, restores the page from `STATE_POSITION`, and tries to tint the legacy window title red. The value 0xFFFF0000 shows in JADX as `SupportMenu.CATEGORY_MASK`. This is likely a no-op with an AppCompat action bar.
- `onSaveInstanceState()`: stores the current page.
- `ImagePagerAdapter.getCount()` (6), `getItem(pos)`, `getPageTitle(pos)`.

**Rules & edge cases.**
- An unused `ProgressDialog` is created.
- The ST/OF choice lives only in the static `globalvars.curanalysisreport`. If the process is restored without the drawer having run, it is null and every page throws an NPE.

## Finance-wise abstract (analysis page 0) — `analysisrep_financewiseabstract_fragment`
| | |
|---|---|
| **Type** | Fragment (androidx), extends `AbsListViewBaseFragment`; implements `anarep_finwiseabstrct_adapter.financelistadapterlistner` |
| **Layout(s)** | `fr_image_list.xml` + row `anarep_finacewiseabs_row.xml` |
| **Opened from** | `loananddepositanalyisreport` page 0 |
| **Opens** | PDF viewer / share chooser (print hidden) |
| **Data** | Remote selected-branch DB: `exec Rep_FinWiseSTLoans @RDate='<todaydate>'` or `exec Rep_FinWiseOFLoans …`; columns used `FinId`, `Finance`, `Total` |

**Purpose.** The total ST/OF loan amount per finance (line) as on the working date. The `Total` value is computed by the procedure.

**UI.**
- Heading = `getFinWiseAbsHeadStr()`.
- Rows: finance name | amount (`##,##,##,##0`). Rows whose name contains "Total" are blue (0xFF0000FF); others are dark grey (0xFF444444).

**Menu / actions.** Show as PDF, Share.

**How it works.**
1. `onCreateView` sets the heading, resets the StrictMode VM policy to an empty one (this disables file-URI checks process-wide) and starts the query.
2. Rows map to `anarep_finwiseabstract_data` (finid, finname, amount).
3. Output is a PDF with columns Finance | Amount, the amount formatted `##,##,##,##,##,##0`.

**Functions.**
- `onCreateView()`.
- `onOptionsItemSelected()`: pages = ⌈rows/23⌉, then print, PDF or share.
- `ASYNCGetFinancelist.*`.
- `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.onLayout/onWrite/pageInRange`, `drawPage()`, `drawCenterwithgivenwidth()`. `drawCenter()` and `drawCenterwithbox()` are unused.
- `onContactSelected()`: empty.

**Rules & edge cases.** The footer time is null (see host).

## Partner-wise abstract (analysis page 1) — `analysisrep_partnerwiseabstract_fragment`
| | |
|---|---|
| **Type** | Fragment, extends `AbsListViewBaseFragment`; reuses `anarep_finwiseabstrct_adapter` |
| **Layout(s)** | `fr_image_list.xml` + row `anarep_finacewiseabs_row.xml` |
| **Opened from** | `loananddepositanalyisreport` page 1 |
| **Opens** | PDF viewer / share chooser |
| **Data** | `exec Rep_ShowPartnerWiseSTLoanBalance @RDate='<todaydate>'` or `exec Rep_ShowPartnerWiseOFLoanBalance …`; columns `RecPartnerId`, `Partner`, `Total` |

**Purpose.** The ST/OF loan amount grouped by recommending partner (`RecPartnerId`). The procedure names call it the partner-wise outstanding balance as on the date.

**UI.** Heading = `getPartnerwiseAbsHeadStr()`. Rows: partner | amount, with "Total" rows in blue.

**How it works.**
1. Like page 0, but each row stores the partner in `anarep_finwiseabstract_data` (finid = RecPartnerId, finname = Partner).
2. **Bug:** `ReportHeader` (the PDF title) is taken from `getFinandPartnerWiseAbsHeadStr()`, so the PDF is titled "Finance and Partner Wise … Analysis". Its first column header also reads "Finance".

**Functions.**
- `onCreateView()` (also resets StrictMode).
- `onOptionsItemSelected()`, `ASYNCGetFinancelist.*`, `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.*`, `drawPage()` (Finance | Amount), `drawCenterwithgivenwidth()`.
- `onContactSelected()`, `onDestroy()` (super only).

**Rules & edge cases.** The footer time is null.

## Finance and partner-wise (analysis page 2) — `analysisrepfin_partnerwiseabstract_fragment`
| | |
|---|---|
| **Type** | Fragment, extends `AbsListViewBaseFragment`; implements `anarep_fin_partwiseabstrat_adapter.financelistadapterlistner`. JADX partial decompile of `drawPage`. |
| **Layout(s)** | `fr_image_list.xml` + row `anarep_finace_partnerwiseabs_row.xml` |
| **Opened from** | `loananddepositanalyisreport` page 2 |
| **Opens** | PDF viewer / share chooser |
| **Data** | `exec Rep_ShowPartnerFinanceWiseSTLoanBalance @RDate='<todaydate>'` or `exec Rep_ShowPartnerFinanceWiseOFLoanBalance …`; columns `FinId`, `Finance`, `Partner`, `RecPartnerId`, `Total` |

**Purpose.** The loan amount for each finance × partner combination.

**UI.** Heading = `getFinandPartnerWiseAbsHeadStr()`. Rows show partner, finance and amount. Rows whose finance name contains "Total" are blue.

**How it works.**
1. Rows map to `anarep_fin_partnerwiseabstract_data`.
2. The PDF has columns headed "Finance" (it holds the partner name, blank when null) | "FIN" (finance) | "Amount" (`##,##,##,##0`).

**Functions.** `onCreateView()`, `onOptionsItemSelected()`, `ASYNCGetFinancelist.*`, `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.*`, `drawPage()`, `drawCenterwithgivenwidth()`, `onContactSelected()`, `onDestroy()`.

**Rules & edge cases.** The column header is mislabelled; the footer time is null.

## Party-wise (analysis page 3) — `analysisrepfin_partyrwiseabstract_fragment`
| | |
|---|---|
| **Type** | Fragment, extends `AbsListViewBaseFragment`; implements `anarep_partywiseabstrat_adapter.financelistadapterlistner` |
| **Layout(s)** | `fr_image_list.xml` + row `anarep_partywiseabs_row.xml` |
| **Opened from** | `loananddepositanalyisreport` page 3 |
| **Opens** | Nothing in practice (see Menu) |
| **Data** | `exec RPT_PartywiseSTLoans @RDate='<todaydate>'` or `exec RPT_PartywiseOFLoans …`; columns `FinId`, `Finance`, `Partner`, `RecPartnerId`, `Total`, `Party`, `IntRate` |

**Purpose.** A party-by-party (borrower) list of ST/OF loan amounts, with the recommending partner, the finance and the interest rate.

**UI.**
- Heading = `getPartyWiseAbsHeadStr()`.
- Each row shows the party name on top, then partner, interest rate (the raw double), finance short name and amount (`##,##,##,##0`).

**Menu / actions.** Show as PDF and Share appear, but `onOptionsItemSelected` only recomputes the page count (33 rows per page) and calls super (confirmed in smali). Neither does anything.

**How it works.**
1. The load works like the other pages and fills `anarep_partywiseabs_data`.
2. The unreachable output code has columns Party | Partner | FIN | IR (`0.00`) | Amount and sets the print timestamp at each page break.

**Functions.**
- `onCreateView()`, `onOptionsItemSelected()` (page count only), `ASYNCGetFinancelist.*`.
- `printDocument()`, `createPdf(name)` and `sharePdf(name)` strip spaces and colons from the file name. Together with `MyPrintDocumentAdapter.*`, `drawPage()` and `drawCenterwithgivenwidth()` they are all unreachable.
- `onContactSelected()`, `onDestroy()`.

**Rules & edge cases.** This is the only analysis page with no working PDF or share.

## Interest-rate distribution (analysis page 4) — `analysisrepfin_interestratedist_fragment`
| | |
|---|---|
| **Type** | Fragment, extends `AbsListViewBaseFragment`; implements `anarep_interestratedist_adapter.financelistadapterlistner` |
| **Layout(s)** | `fr_image_list.xml` + row `anarep_interestratedist_row.xml` |
| **Opened from** | `loananddepositanalyisreport` page 4 |
| **Opens** | PDF viewer / share chooser |
| **Data** | `exec Rep_ShowSTLoanIntDistribution @RDate='<todaydate>'` or `exec Rep_ShowOFLoanIntDistribution …`; columns `count`, `Finance`, `loan`, `minimum`, `maximum`, `average`, `IntRate` |

**Purpose.** For each finance and each interest rate: how many loans there are (`count`), their total amount (`loan`), and the smallest, largest and average loan. All values are computed by the procedure.

**UI.**
- Heading = `getIntRateDisAbsHeadStr()`.
- Rows show finance, count, IR (`##,##,##,##0.00`) and total, then "MAX: <maximum>", "MIN : <minimum>" and "AVG : <average>". The adapter puts `getMaximum()` into the view labelled MAX, so the labels are correct here.

**How it works.**
1. Rows map to `anarep_interestratedist_data`.
2. The PDF has columns Finance (only 50 pt wide) | IR | Count | Total | MIN | MAX. The average is not printed.

**Functions.** `onCreateView()`, `onOptionsItemSelected()`, `ASYNCGetFinancelist.*`, `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.*`, `drawPage()`, `drawCenterwithgivenwidth()`, `onContactSelected()`, `onDestroy()`.

**Rules & edge cases.** The footer time is null.

## Overall interest-rate distribution (analysis page 5) — `analysisrepfin_overallinterestratedist_fragment`
| | |
|---|---|
| **Type** | Fragment, extends `AbsListViewBaseFragment`; implements `anarep_overallinterestratedist_adapter.financelistadapterlistner` |
| **Layout(s)** | `fr_image_list.xml` + row `anarep_overallinterestratedist_row.xml` |
| **Opened from** | `loananddepositanalyisreport` page 5 |
| **Opens** | PDF viewer / share chooser |
| **Data** | `exec [Rep_ShowSTLoanIntDistributionOverAll] @RDate='<todaydate>'` or `exec [Rep_ShowOFLoanIntDistributionOverAll] …`; columns `count`, `loan`, `minimum`, `maximum`, `average`, `IntRate` (no finance) |

**Purpose.** The same distribution as page 4, by interest rate across all finances combined.

**UI.**
- Heading = `getOverallIntRateDisAbsHeadStr()`.
- Rows show count, IR and total, then "MIN:", "MAX :" and "AVG :" values.

**How it works.**
1. The load works like page 4.
2. The PDF has columns IR | Count | Total | MIN | MAX. Both "Show as PDF" and "Share" write the same fixed file `Documents/venusmfc/OverallROIAnalysis.pdf`, overwriting it each time.

**Functions.** `onCreateView()`, `onOptionsItemSelected()`, `ASYNCGetFinancelist.*`, `printDocument()`, `createPdf()`, `sharePdf()` (no arguments, fixed file name), `MyPrintDocumentAdapter.*`, `drawPage()`, `drawCenterwithgivenwidth()`, `onContactSelected()`, `onDestroy()`.

**Rules & edge cases.**
- **Display bug:** the adapter puts `getMaximum()` into `…_minimumamountid` and `getMinimum()` into `…_maximumamountid`. In this row layout the labels sit in the order "MIN:" then "MAX :", so the screen shows the minimum and maximum swapped. The PDF columns are correct.
- The footer time is null.

## Partner-wise loans & performance (home) — `partnerwiseloansandperformancehome`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity). **Not declared in AndroidManifest.xml**, so starting it throws ActivityNotFoundException. |
| **Layout(s)** | `partnerwiseloanandperformancehome.xml` (ViewPager `pager`; PagerTitleStrip `gone`) |
| **Opened from** | Only `partyledgers_parameters` rc 7015 (`partnerledgerid`, `todate`, `ttid`, `selectecdfinanceids`, `selectedfinancenames`, `requestcode`). No screen sends rc 7015, so it is unreachable twice over. |
| **Opens** | `partnerwiseloansandperformance_fragment` pages |
| **Data** | None. It passes Bundle arguments to its fragments. |

**Purpose (intended).** For one partner, show the pending interest on the loans that partner recommended, in two pages: ST loans and OF loans.

**How it works.**
1. `onCreate` reads the extras. `ImagePagerAdapter` reports `getCount()`=3:
   - page 0 → fragment with `ttid` 3, `fragmenttitle` "STLOANS";
   - page 1 → fragment with `ttid` 5, `fragmenttitle` "OF LOANS";
   - page 2 → `null`.
   Each page also gets `partnerledgerid`, `todate` (yyyy-MM-dd string) and `selectecdfinanceids`.
2. Page titles would be "STLoans" / "Deposits" / "OF Loans", but they are hidden.

**Functions.** `onCreate()`, `onSaveInstanceState()`, `ImagePagerAdapter.getCount/getItem/getPageTitle`.

**Rules & edge cases.**
- `getItem(2)` returns null, so swiping to page 1 (which makes the pager build page 2) would crash.
- A third `depositsfragment` instance is built but never used.
- The title-tint code is the same as in the analysis host.

## Partner-wise interest pending (loans & performance page) — `partnerwiseloansandperformance_fragment`
| | |
|---|---|
| **Type** | Fragment, extends `partnerwiseperformanceandloans_BaseFragment` → `BaseFragment` (menu `partnerwiseperformance_menu`). JADX partial decompile of `drawPage`. |
| **Layout(s)** | `fragment_partnerwiseloansandperfomance.xml`; groups `mettingreport_partylevel_row.xml` and children `meetingreport_loanlevel_row.xml` through `meetingreports_expandablelist_adapter` |
| **Opened from** | `partnerwiseloansandperformancehome` pages 0 and 1 (unreachable, so this fragment is dead code in this build) |
| **Opens** | PDF viewer, share chooser and print (print is hidden). `voicecallandsmsoptions` (dialer, SMS, WhatsApp Business) exists but is never called. |
| **Data** | **Remote ST:** `exec STLIntColPending @IUPDate = '<todate>', @IntPaidDate ='<todate>', @SelectedFinanceIds='<ids>'`, reading `DOWNLOADDatetime` into `globalvars.STLoans_meetingreport_recentdownloaddateandtime`. Then `delete from tempIntColPending where PaidInt>0`; `exec STLIntColPendingList`; a SELECT from `tempstlIntColPenidnglistnew where RecPartnerId=<partner> order by LedgerName`; and a party aggregate.<br>**Remote OF:** `exec ofIntColPending …` (same parameters, no delete step); `exec ofIntColPendingList`; `tempoflIntColPenidnglistnew … order by PartyLedgerId`.<br>**Local:** `qatledgers` supplies the partner name. |

**Purpose (intended).** For one partner, list each party whose loans that partner recommended, with the outstanding balance and the interest calculated but not yet paid up to the chosen date. Each party expands to its individual loans.

**UI.**
- The info TextView `meetingreports_includedfinanceinfoid` shows "STLOANS" or "OF LOANS".
- `ExpandableListView` `lvExp`:
  - group row: party name (bold), total balance, total interest;
  - child row: finance short name, balance, interest-from and interest-upto (`dd-MM-yy`), days, interest = `CInt`.
- A progress bar and a progress text show the steps "Connecting", "Preparing List 1/2" and "Getting List 2/1".

**Menu / actions.** Show as PDF and Share. Group and child click listeners do nothing.

**How it works.**
1. `ASYNC_GetInterestPendingList` sets `ReportHeader` = "`Partner Name:<name>- STL Interest Pending upto:dd-MM-yyyy`" (or "…- OF Loan Interest Pending upto:…"). It runs the procedure chain above, then the loan-level SELECT. Columns `typeid`, `FinId`, `STLoanId`/`ofloanId`, `RecPartnerId`, `PartyLedgerId`, `IntRate`, `LoanDate`, `…AmountPaid`, `LedgerName`, `MobileNo1`, `ShortName`, `LoanNo`, `…Amount`, `Balance`, `IntFrom`, `Intupto`, `Days`, `CInt`, `PaidInt`, `Paidon` and `Receno` become `meetingreport_data` (for print) and `meetingreport_loanlevel_data` (for the list).
2. The groups query: `Select 1 as ShowId, PartyLedgerId, Ledgername, sum(Balance) as Balance, sum(CInt) as Interest from <temp> where RecPartnerId=<id> and typeid=1 group by PartyLedgerId, Ledgername union all select 2 as showId, Null,'Total',sum(Balance),sum(CInt) … order by showId,LedgerName`. Each party's children are the loan rows with the same PartyLedgerId.
3. PDF output is named `anarep<millis>.pdf`. The `pdffilename` "`<partner>_STLInterestPendingupto<ddMMyyyy>`" is built but never used. "Show as PDF" uses a 792×576 page; "Share" uses 842×595 with MIME "text/csv". Each page has 23 rows and the columns Party | FIN | No | Date | Loan | Paid | Balance | Int From | Int upto | Days | CalInt | Paid | Date | Recpt. Row kinds:
   - `typeid` 1: a loan line; the party name prints only on its first line;
   - `typeid` 2: a centred heading;
   - `typeid` 3: a sub-total (name, short name, balance, CInt);
   - `typeid` 4: the grand total.

**Functions.**
- `onCreateView()`: reads the arguments, builds the list, starts the task.
- `ASYNC_GetInterestPendingList.*`.
- `onOptionsItemSelected()`.
- `printDocument()`, `createPdf()`, `sharePdf()`, `MyPrintDocumentAdapter.onStart/onLayout/onWrite/pageInRange`. `onWrite` sets the start row to `page×23`, using -1 for page 0.
- `drawPage()`, `drawCenter()` (used for the typeid 2 heading), `drawCenterwithgivenwidth()`.
- `showprintandshareoptions()`: never called.
- `voicecallandsmsoptions(ctx, mobile, msg)`: never called. It offers:
  - "Call": ACTION_CALL `tel:` with flag 0x04000000 = FLAG_ACTIVITY_CLEAR_TOP (JADX shows `AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL`); toast "Call Failed, please try again later.";
  - "Send SMS" / "Compose SMS": `sms:` with `sms_body`;
  - "Whatsapp": `com.whatsapp.w4b` with `https://api.whatsapp.com/send?phone=91<mobile>&text=<msg>`, started twice; toast "Sharing tools have not been installed.".
- `onDestroy()`.

**Rules & edge cases.**
- The temp tables have no `#` prefix, so they are ordinary server tables shared by all users; concurrent runs can overwrite each other's data.
- The loan-level objects never get `PaidInt`, so the child view always shows `CInt`.
- Errors are swallowed.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `fintranslist_adapter` | RecyclerView adapter, Filterable | `fintrans_list` | Row `stloanslist_row`: party, AddLine1, "`<finance>-<FWUD no>`" (`defaulttables.fetchfinancename`), status, date `dd-MM-yyyy`, amount `##,##,##,##0`, IR = daily rate × 30 (`##0.00`). ST rows are tinted goldenrod, OF rows cyan. Click → `ContactsAdapterListener.onTransactionselected`; `onthumnailclicked` is never fired. Filter: name or AddLine1 `contains`, case-sensitive. |
| `partieslist_adapter` | RecyclerView adapter, Filterable | `parties_list` | Row `partieslistrow`: LedgerName, AddLine1 (`partylist_tagid`), AddLine2, City, MobileNo1. Click → `onPartyselected(party, pos)`. Filter: upper-cased name, or `AddLine2.contains(raw query)`. |
| `financewisepartners_adabpter` | RecyclerView adapter, Filterable | `financewisepartners_list` | Row `financewisepartners_row`: a "selected" CheckBox (text = partner name, disabled when `RecTransCount` > 0) and an "Active" CheckBox. Callbacks `onfinancewisepartnerselected` and `onactivechecked`; binding with `setChecked` also fires them. Filter by name is broken (upper-cased query vs lower-cased text). |
| `partyledgers_adapter` | RecyclerView adapter, Filterable | `partyledgers_list` | Row `partyledgers_row`: date, Particulars (TransType), Interest, Loan, Return; each amount blank when 0. Click → `onContactSelected`, which is empty. The TransType filter is broken the same way. |
| `loanledgers_adapter` | RecyclerView adapter, Filterable | `loanledgers_list` | Row `loanledgers_row`: SNO (only when 0 < SNO < 1000), date, Party, LoanNo, IR `0.000`, Return, and Loan (shown under the Balance id). Click → `onContactSelected`, which is empty. The Party filter is broken the same way. |
| `anarep_finwiseabstrct_adapter` | RecyclerView adapter | analysis pages 0 and 1 | Row `anarep_finacewiseabs_row`: name and amount; rows containing "Total" are blue, others dark grey. Click → `onContactSelected`. The name filter is never used. |
| `anarep_fin_partwiseabstrat_adapter` | RecyclerView adapter | analysis page 2 | Row `anarep_finace_partnerwiseabs_row`: partner, finance and amount; "Total" rows (by finance name) are blue. Click → `onContactSelected`. |
| `anarep_partywiseabstrat_adapter` | RecyclerView adapter | analysis page 3 | Row `anarep_partywiseabs_row`: party, partner, IntRate (raw), finance and amount. Click → `onContactSelected`. Filter by party is unused. |
| `anarep_interestratedist_adapter` | RecyclerView adapter | analysis page 4 | Row `anarep_interestratedist_row`: finance, count, IR `0.00`, total; the "MAX:" label shows `maximum`, "MIN :" shows `minimum`, then average. Click → `onContactSelected`. |
| `anarep_overallinterestratedist_adapter` | RecyclerView adapter | analysis page 5 | Row `anarep_overallinterestratedist_row`: count, IR, total, then min/max/avg with min and max swapped against their labels (see page 5). Its filter calls `getFinance()`, which is always null for this report, so it would NPE if ever used. |
| `meetingreports_expandablelist_adapter` | BaseExpandableListAdapter | `partnerwiseloansandperformance_fragment`, `meetingreport` (other module) | Groups `meetingreport_partylevel_data` → `mettingreport_partylevel_row` (bold name, Balance and Interest in `##,##,##,##0`). Children: a HashMap of party to a list of `meetingreport_loanlevel_data` → `meetingreport_loanlevel_row` (ShortName, Balance, IntFrom/Intupto `dd-MM-yy`, Days, interest = CInt when PaidInt is 0, else 0). Ids are not stable; children are selectable. |
| `fintrans_data` | Serializable model | `fintrans_list`, `fintranslist_adapter`, passed as `transdata` to `stloans`/`ofloans` | FinId, LedgerId, Status, TGFWTId, TGWTId, addline1/2, city, mobileno1/2, ledgername, loanordepositid (ST/OF loan id), loanordepositudid (FWUD number), loanordepositno, transDate (`java.sql.Date`), transamount (int), ttid, daily/monthly/yearly interest rate (float) |
| `partyledgers_data` | model | `partyledgers_list` | Typeid, TransId, TransDate, TransType (particulars), Interest (int), Loan (Double), Repayment (int) |
| `loanledgers_data` | model | `loanledgers_list`, `partyledgers_parameters` | SNO, Party, LoanNo, IR (Double), Return, Loan, LDate |
| `financewisepartners_data` | model | `financewisepartners_list` | UID (`tblFinwisePartners` link id; 0 = not linked), PartnerLedgerId, LedgerName, IsSelected, Active, RecTransCount |
| `anarep_finwiseabstract_data` | model | analysis pages 0 and 1 | finid, finname, amount. Page 1 stores RecPartnerId and Partner in these fields. |
| `anarep_fin_partnerwiseabstract_data` | model | analysis page 2 | finid, finname, partnerledgerid, partnername, amount |
| `anarep_partywiseabs_data` | model | analysis page 3 | FinId, Finance, RecPartnerId, Partner, Party, IntRate (Double), Total (Double) |
| `anarep_interestratedist_data` | model | analysis pages 4 and 5 | Finance, intrate (Double), count, loan (total), minimum, maximum, average (all int) |
| `meetingreport_data` | model | `partnerwiseloansandperformance_fragment` (PDF rows), `meetingreport` | typeid, FinId, STLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, STLoanAmount, STLoanAmountPaid, Balance, IntFrom, Intupto, Days, CInt (calculated interest), PaidInt, Paidon, Receno, LedgerName, MobileNo1, ShortName, LoanNo (int). OF rows reuse the ST-named fields. |
| `meetingreport_loanlevel_data` | model | `meetingreports_expandablelist_adapter` children; the fragment and `meetingreport` | Same fields as `meetingreport_data`, except LoanNo is a Double. Only ShortName, Balance, IntFrom, Intupto, CInt, Days and PartyLedgerId are filled. |
| `meetingreport_partylevel_data` | model | adapter groups; the fragment and `meetingreport` | PartyLedgerId, Ledgername, Balance, Interest, plus intfrom, intupto and mobileno, which are unused here. It is also the HashMap key, without equals or hashCode, so lookups are by identity. |
| `singlevalueselection_data` | model | `partyledgers_parameters` (declared only) | `finid`. Never instantiated. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `ac_complex.xml` | `loananddepositanalyisreport` | ViewPager `pager` with a PagerTitleStrip (`gone`, design text "01-06-2001") |
| `partnerwiseloanandperformancehome.xml` | `partnerwiseloansandperformancehome` | ViewPager `pager` + PagerTitleStrip (`gone`) |
| `activity_partieslist.xml` | `fintrans_list`, `parties_list` (also `customers_list`, other module) | CoordinatorLayout `main`: Toolbar `toolbar`, RecyclerView `partieslist_recycler_view`, ProgressBar `partieslist_pbar` (gone), FAB `partieslist_addnewpartyfabid` (`ic_input_add`, bottom-end) |
| `partieslistrow.xml` | `partieslist_adapter` | `partylist_partynameid`, `partylist_tagid` (AddLine1), `partylist_addline2`, `partylist_city`, `partylist_mobileno` |
| `stloanslist_row.xml` | `fintranslist_adapter` | `stloanslist_ledgernameid`, `stloanslist_addressid`, `stloanslist_finnameid`, `stloanslist_loanstatusid`, `stloanslist_loandateid`, `stloanslist_loanamountid`, `stloanslist_intrateid` |
| `activity_financewisepartners.xml` | `financewisepartners_list` | Toolbar with "Finance" label + EditText `financewisepartners_financenameid`; RecyclerView `partieslist_recycler_view`; ProgressBar `financewisepartners_pbarid` (gone); Button `financewisepartners_savebuttonid` "UPDATE" |
| `financewisepartners_row.xml` | `financewisepartners_adabpter` | CheckBox `financewisepartnersrow_partnerselectedornotid` (partner name), CheckBox `financewisepartnersrow_isactiveid` "Active" |
| `activity_loanpartyledgers_parameters.xml` | `partyledgers_parameters` | ConstraintLayout `main`: "Select Finances" label, RecyclerView `partyledgersparameters_finanelistrecyclerviewid`, "Select Period" label; from-date row ("ஆரம்ப தேதி" = Start date, EditText `partyledgersparameters_fromdateid`); to-date row ("நிறைவு தேதி" = End date, `partyledgersparameters_todateid`); party row `partyledgerparameters_partyselectionid` ("Select Party" + EditText `partyledgersparameters_LedgerNameid`); Button `partyledgersparameters_showbuttonid` "SHOW"; ProgressBar (gone) |
| `activity_partyledgerslist.xml` | `partyledgers_list` | Toolbar block: "PARTY : name", "FINANCE : names", "PERIOD : …" (`periodorloannoid`/`partyledgerslist_periodid`); 25 dp delete Button `partyledger_deleteloanpartybuttonid` (`ic_action_deletechitparty`, kept GONE); header strip `partyledgerslist_columnheaderid` "Particulars / Interest / Loan / Return"; RecyclerView `acctranslist_recycler_view`; ProgressBar `acctranslist_pbar` |
| `partyledgers_row.xml` | `partyledgers_adapter` | `partyledgersrow_transdateid`, `partyledgersrow_particularsid`, `partyledgersrow_interestid`, `partyledgersrow_loanid`, `partyledgersrow_repaymentid` |
| `activity_loanledgerslist.xml` | `loanledgers_list` | Toolbar block "FINANCE : names" and "PERIOD : …" (string resources); header strip "S.No / Date (ta தேதி) / Party/No / IR / Return / Balance" (code sets the last to "Loan"); RecyclerView `acctranslist_recycler_view`; ProgressBar |
| `loanledgers_row.xml` | `loanledgers_adapter` | `loanledgersrow_snoid`, `_dateid`, `_partyid`, `_noid`, `_irid`, `_returnid`, `_balanceid` |
| `fr_image_list.xml` | all six analysis fragments | TextView `fragmenttitleid` (page heading), RecyclerView `@android:id/list`, ProgressBar `fragment_progressbarid` (gone) |
| `anarep_finacewiseabs_row.xml` | `anarep_finwiseabstrct_adapter` | `anarep_finwiseabsrow_financenameid` (name), `anarep_finwiseabsrow_amountid` (amount) |
| `anarep_finace_partnerwiseabs_row.xml` | `anarep_fin_partwiseabstrat_adapter` | `…_partnernameid`, `…_financenameid`, `…_amountid` |
| `anarep_partywiseabs_row.xml` | `anarep_partywiseabstrat_adapter` | `anarep_fin_partywiseabsrow_partynameid`, then a line with `…_partnernameid`, `…_intrateid`, `…_financenameid`, `…_amountid` |
| `anarep_interestratedist_row.xml` | `anarep_interestratedist_adapter` | finance, count, interest rate, total; "MAX:" + `…_minimumamountid`, "MIN :" + `…_maximumamountid`, "AVG :" + `…_averageamountid` |
| `anarep_overallinterestratedist_row.xml` | `anarep_overallinterestratedist_adapter` | count, interest rate, total; "MIN:" + `…_minimumamountid`, "MAX :" + `…_maximumamountid`, "AVG :" + `…_averageamountid` (label order is reversed vs the per-finance row) |
| `fragment_partnerwiseloansandperfomance.xml` | `partnerwiseloansandperformance_fragment` | TextView `meetingreports_includedfinanceinfoid`, ExpandableListView `lvExp`, ProgressBar `meetingreport_pbarid` (gone), progress TextView `meetingreport_progresstextid` (gone) |
| `mettingreport_partylevel_row.xml` | `meetingreports_expandablelist_adapter` (group) | `mettingreportpartyrow_partyname`, `mettingreportpartyrow_totalloanid` (balance), `mettingreportpartyrow_totalinterestid` |
| `meetingreport_loanlevel_row.xml` | `meetingreports_expandablelist_adapter` (child) | `meetingreportloanlevelrow_finnameid`, `_loanamountid` (balance), `_fromdateid`, `_todateid`, `_daysid`, `_interestamountid` |

---
[← brain.md](../../brain.md) · Module 8.15 of 20 · Previous: [8.14 ST Loans and OF Loans (interest-bearing loan books)](14-st-loans-of-loans-interest-bearing.md) · Next: [8.16 Admin masters: employees, branch employees, access control, loan methods and finance lines](16-admin-masters-employees-branch-employees-access.md)
