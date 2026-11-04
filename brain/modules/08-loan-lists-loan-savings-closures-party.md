# 8.8 Loan lists, loan & savings closures, party transfer and rearrangement

[← brain.md](../../brain.md) · Module 8.8 of 20 · Previous: [8.7 Parties (borrowers), new loan creation and KYC documents](07-parties-borrowers-new-loan-creation-kyc.md) · Next: [8.9 Collection areas, maps, GPS staff tracking and attendance](09-collection-areas-maps-gps-staff-tracking.md)

This module holds the MC-loan list and review screens, the loan-closure and savings-account-closure workflows, and two party-maintenance tools. The offline lists (`newloanslist_act`, `newpartieslist_act`) read only the device's SQLite tables `NewMCLoans`, `NewParties`, `Parties` and `SubAreaDetails`. All other lists call SQL Server stored procedures over jTDS: `GetMCLoansListforAPPforselectedperiod[withweekdayid]`, `LoanClosedandRenewedforwindowsAPP` / `LoanClosedandRenewedwithweekdayid` / `NIPLoansClosed`, `NewPartiesNewLoans[withweekdayid]`, `APP_ShowMCLStatus`, `Get_LoanindexwiseLoansforselectedparty`, `APP_ListofLiveLoans[WithWeekdayid]`, `MCLoanClosureList`, `GetClearableSavingAccounts` and `Savingaccountclosure`. A loan closure always writes an `MCLoansClosoure` row. When the collection fell short, it also posts a TTID 23 write-off voucher: Dr hard-coded ledger 125, Cr the party. A savings closure (TTID 28) pays out balance + interest from the line's cash ledger and inserts a `SavingsClosure` row. Party transfer moves one party to another collection area: on the server it changes `tblLedgers.SubAreaNo`, and on the device it changes the local copies. Party rearrange lets the user drag parties into a new route order, then saves the order as `UDLedgerId` on the device and the server. Connection strings: lists opened through the branch/line picker `selectbranchandfinance` read from `globalvars.branchlevelReport_branchconnString`, the branch chosen in the picker. All writes, and the other reads, use `globalvars.selbranchconnString`, the app's working branch. In a multi-branch setup these two can point to different databases.

## Loans list for a line / for today — `loanslist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, theme `AppTheme.NoActionBar`, own Toolbar); implements `newloanslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_newloanslist.xml` + row `newloanslistrow.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | (a) Drawer → Executive Entries (online) → "Loans / கடன்கள் விவரம்" (`nav_loansfortoday`). Extras: `passedfinanceid`=`globalvars.finid`, `LineName`=`globalvars.financename`, `calledfrom`=1022 (`requestcodes.loanlistcalledfrom_executives`). (b) Drawer → Admin → "Loans" (`nav_loans`) → `selectbranchandfinance` rc 114. Daily line: `passedfinanceid`, `LineName`, `isweeklyfinance`=false, `weekdayid`=0, `calledfrom`=2. Weekly line: the weekday picker (`spinnersearch_list` "Collection Day", rc 2001) opens first, then the same extras with `isweeklyfinance`=true and `weekdayid` 1–7 (Sunday = 1). |
| **Opens** | `newloancreation`: CallType 301, `passednewloan`=mcloanobj, `passedparty`=partydetails_data, forResult 301. Used to view, edit or delete the loan. `directcollectionentry`: same extras, rc 301; posts a collection for the loan. `selectedloancollectiondetails`: `MCLoanId`, `iscollectioneditallowed`="0", `PartyName`, `Remarks`, `Tag`. FAB → `collectionareassearchlist`: `CallType`=7107 (`constantvalues.getLoan_newloanrequest`), `receviedfinanceid`, `calledfromloanmethod`=1; starts the new-loan flow. |
| **Data** | Remote (`selbranchconnString`): `exec GetMCLoansListforAPPforselectedperiod ?,?,?` (FinId, from, to), or `exec GetMCLoansListforAPPforselectedperiodwithweekdayid ?,?,?,?` (+ weekdayid); `tblLedgers` (ScreenType=3) with `subareadetails` (FinId). Local: `Parties` is wiped and reloaded, and read for Tag; `defaulttables.partyobject()` reads `Parties` ∪ `NewParties` ⟕ `MobileAppCollData`. |

**Purpose.** Lists the MC loans given on one line.
- Admin route: loans for a date range (default: the last month), with collection progress. Each loan can be opened, or a collection can be posted for it.
- Executive (online) route: only the loans created on the current collection date. A tap goes straight to the loan screen to correct or delete the loan.
- A footer totals loan, interest, document charges (DC), collection and balance.

**UI.**
- Toolbar title:
  - Admin: "`<LineName>`-Loans".
  - Executive: "Loans-`<dd-MM-yyyy>`". The date shown is wrong (see Rules).
- Date bar `lytdaterangeid`, admin only:
  - "From : `loanlist_fromdateid`" and "To : `loanlist_todateid`" (chocolate text). Tap either to open a DatePicker.
  - Refresh ImageButton `loanslistrefreshid` ("Refresh List").
  - Hidden for calledfrom 1022. `lytselectlineid` ("Line Name") is always hidden.
- List `loansslist_recycler_view`, rows `newloanslistrow`:
  - "`CWLedgerId`. PartyName" (the number is left out when it is 0), then the area name (goldenrod).
  - "Loan: / கடன்:" = MCLoanAmountCollectable.
  - "Ins: / தவணை:" = TotalMonths (number of instalments).
  - "Int: / வட்டி:" = InterestAmount.
  - "Date: / தேதி:" = MCLoanDate.
  - "DC: / மகிமை :" = DocCharges.
  - Admin mode (adapter calltype 2) also shows "Coll:" = Collected and the collection icon `newloanlist_collectiondetailsid`. Executive mode hides both.
- Footers. Both are forced visible in both modes:
  - "Total Loan : Σ MCLoanAmountCollectable", "Interest : Σ InterestAmount", "DC : Σ DocCharges".
  - "Count: n", "Collection : Σ Collected", "Balance : Total Loan − Collection".
- FAB `newloan_fab` "+" ("Add New").

**Menu / actions.**
- **Search** (SearchView):
  - Digits only → exact party number (CWLedgerId).
  - Other text → area name (lower-cased) contains the query, or party name contains it (as typed or lower-cased).
  - Back closes the search box first.
- **Row tap:**
  - Executive (1022) → `newloancreation` (CallType 301) directly.
  - Admin → dialog "Choose an Option":
    - "See Loan Details" → `newloancreation`.
    - "New Collection Entry" → `directcollectionentry`.
- **Collection icon** (admin only) → read-only collection history. The Tag comes from local `Parties`.
- **FAB** → new-loan flow for this line. This screen applies no access check to it.
- **Date pickers and Refresh** → change the range and re-run the query.

**How it works.**
1. `onCreate` reads `passedfinanceid`, `LineName`, `calledfrom`, `isweeklyfinance` and `weekdayid`. It then:
   - sets the range from today − 1 month to today;
   - makes both footers visible;
   - for 1022, hides the date bar and sets the title;
   - creates the adapter with calltype = calledfrom;
   - runs `ASYNCLoansList`.
2. `ASYNCLoansList.doInBackground` shows a ProgressDialog "downloading, please wait ..." with steps "Connecting" and "Loading Loans", then connects to `selbranchconnString`. Query timeout is 3000 s.
   - Daily line: `GetMCLoansListforAPPforselectedperiod(FinId, from, to)`. For 1022, from = to = `globalvars.collectiondate`.
   - Weekly line (reachable only from the admin route): `…withweekdayid(FinId, from, to, weekdayid)`.
   - Each result row becomes an `mcloanobj` with these columns: MCLoanId, CWLedgerId, FinId, MCLoanDate, PartyLedgerId, MCLoanAmount, TotalMonths, DocCharges, LoanMethodId, InterestAmount, MCLoanAmountCollectable, InsAmount, ondaycollection, MCLoanAmountEstimated, SubAreaNo, ServerGenMCLoanId, ServerGenLedgerId, Remarks, WeekDayId, MonthDayid, PartyName, AreaName, ServerGenTransId, Collected, Sourceledgerid, PartyReferenced, Loanindex, MobileNo, AdvanceAmount, Balanceamount, Productvalue, Intrate.
3. The same task then rebuilds the device's party cache:
   - `db.delete("Parties","LedgerId>0")`.
   - `select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,…,NoOfLoansAllowed,MaximumAmountAllowed,isactive from tblLedgers where [isactive=1 and] ScreenType=3 and SubAreaNo in (select SubAreaNo from subareadetails Where FinId=<fin>) order by SubAreaNo, UDLedgerId`.
   - The `isactive=1` filter is dropped when `globalvars.isFullAccessAllowed` is set or the user has special access 11.
   - Every row is inserted into local `Parties`, including mobiles, spouse/family name, lat/lng, image and ID-proof file names, AddLine1, NoOfLoansAllowed, MaximumAmountAllowed and IsActive.
4. `onPostExecute` refreshes the list and computes the footer totals.
5. `onActivityResult` (rc 301):
   - resultCode 301 (saved) → replaces the row with the same MCLoanId by the returned `receivedloandata`, then scrolls to it.
   - resultCode 303 (delete) → `nooftransfound` holds the number of rows `newloancreation` deleted:
     - more than 0 → the row is removed and dialog "Info..." shows "Loan Deleted.";
     - 0 → warning "Related entries found\nLoan cannot be deleted".
6. `oncollectiondetailslinkclicked` copies the login branch (`branchconnString`, user, password) into `globalvars.selbranch*`, then opens the details screen.

**Functions.**
- `onCreate()` — read extras, set default range, footers, adapter, first load.
- `lambda$onCreate$1()` — Refresh button → `ASYNCLoansList`.
- `lambda$onCreate$2()` — FAB → `collectionareassearchlist` new-loan request.
- `lambda$onCreate$3/4()`, `lambda$onCreate$5/6()` — From / To DatePickers.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — SearchView filter; home → back.
- `onContactSelected()` / `lambda$onContactSelected$9()` — row tap; executive goes direct, admin gets the two-option dialog.
- `oncollectiondetailslinkclicked()` — opens collection history and resets the working branch.
- `onActivityResult()` — applies edit and delete results.
- `ASYNCLoansList.doInBackground()` / `onPostExecute()` / `onPreExecute()` / `onProgressUpdate()` — load loans and rebuild `Parties`; totals; progress dialog.

**Rules & edge cases.**
- calledfrom values:
  - 1022 = `requestcodes.loanlistcalledfrom_executives`.
  - The admin route passes 2 (= `constantvalues.loanlistcalledfrom.loansfortheperiod`).
  - `requestcodes.loanlistcalledfrom_admin` (1023) is defined but never passed.
- The calledfrom value becomes the adapter's calltype:
  - 2 → "Coll:" column and collection icon.
  - 1 (`loanlistcalledfrom.newloans`, used by `newloanslist_act`) → "Onday Coll" column, only when soft setting `enableondaycollection_27` (id 36) is on.
  - 1022 → neither.
- Executive title bug: the title is built from `curdaterange.getFromdate()`, which is today − 1 month. The data itself is for `globalvars.collectiondate`.
- The executive route never passes `isweeklyfinance`, so it always uses the procedure without weekday.
- Every load (and every Refresh) deletes and rebuilds the device's `Parties` table with this line's parties. This replaces the offline party cache. If an admin opens loans for a line other than the one downloaded, the offline cache now holds the wrong line's parties.
- Footer totals cover the whole list, not the search result. They are not recomputed after an in-place edit or delete.
- Opening collection details switches `selbranchconnString` to the login branch, so a later Refresh queries the login branch.
- Errors are only printed. A connection failure shows an empty list.

## Today New Loans (offline) — `newloanslist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `newloanslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_newloanslist.xml` (date bar and FAB hidden in code; footers stay hidden) + `newloanslistrow.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer → Executive Entries (offline) → "New Loans / புதிய கடன்கள்" (`nav_newloans`), extra `passedmenuname`="nav_newloans" |
| **Opens** | `newloancreation`: CallType 9008 (`requestcodes.editloanrequestcodefromOFFLINEdata`), `passedparty`, `passedloandata`; forResult 9008 |
| **Data** | Local only. `defaulttables.getnewloanlist()` creates the tables if missing, then reads `NewMCLoans` ⋈ `NewParties` ⋈ `SubAreaDetails` and `NewMCLoans` ⋈ `Parties` ⋈ `SubAreaDetails`. Menu rights come from `globalvars.salesdetailsforandroid_datalist` (local `tblSalesDetailsforAndroid` for the user role), entry "nav_newloans". |

**Purpose.** Lists the loans the offline executive created on this device since the last upload, for both new and existing parties, so they can be opened and corrected before upload.

**UI.**
- Toolbar "Today New Loans" (ta "புதிய கடன்கள்").
- Rows as in `loanslist_act`, with adapter calltype 1:
  - "Onday Coll : / க.நாள் வசூல் :" shows `ondaycollection` only when soft setting `enableondaycollection_27` is on.
  - The collection icon is hidden.

**Menu / actions.**
- Search: same matching as `loanslist_act`.
- Row tap: opens the offline loan for editing, if the user has the view right.

**How it works.**
1. `onCreate`:
   - hides the date bar and FAB;
   - loads the list with two SELECTs: `select MCLoanDate, SubAreaName as AreaName, CWLedgerId, ondaycollection, Ledgername as PartyName, Tag, MCLoanId, a.FinId, PartyLedgerId, MCLoanAmount, TotalMonths, DocCharges, LoanMethodId, InterestAmount, MCLoanAmountCollectable, InsAmount, MCLoanAmountEstimated, a.SubAreaNo, a.ServerGenMCLoanId, a.ServerGenLedgerId, Remarks, a.ServerGenTransId, a.WeekDayId, MonthDayId, mobileno, Sourceledgerid, LoanIndex, PartyReferenced, ProductValue, AdvanceAmount, IntRate From NewMCLoans a, NewParties b, SubAreaDetails c where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo`, then the same query against `Parties b`.
2. Access rights: full access grants all rights. Otherwise the role's "nav_newloans" rights apply.
3. Empty list → AlertDialog "Info": "No Loans Created today..". OK closes the screen.
4. Row tap: if the user has the view right, `partyobject(db, PartyLedgerId)` loads the party, then `newloancreation` opens with CallType 9008.
5. `onActivityResult` (any code) clears the list and re-runs both queries inline.

**Functions.**
- `onCreate()` — build the list, apply rights, handle the empty case.
- `lambda$onCreate$1()` — empty-dialog OK → `finish()`.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — search; home.
- `onContactSelected()` — opens the offline loan for edit.
- `onActivityResult()` — reload from SQLite.
- `oncollectiondetailslinkclicked()` — empty (icon hidden).

**Rules & edge cases.**
- Without the view right, a tap does nothing and shows no message.
- "Today" is not a date filter: the screen shows everything in `NewMCLoans`.
- Rows whose party or area is missing from the joined tables are dropped (inner joins).

## Today New Parties (offline) — `newpartieslist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `newpartieslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_newloanslist.xml` (date bar hidden) + `newpartieslistrow.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer → Executive Entries (offline) → "New Parties / புதிய பார்ட்டி" (`nav_newparties`) |
| **Opens** | `partydetails_act`: `passedparty`, CallType 9902 (`partyscreen_partyshowandupdaterequestfrom_newpartyscreen_offlinepartyupdate`), `SubAreaNo`, `AreaName`; forResult 100 |
| **Data** | Local: `Select LedgerId, CWLedgerId, LedgerName, Tag, a.AreaNo, a.SubAreaNo, b.SubAreaName, UDLedgerId, MobileNo1, MobileNo2, ServerGenLedgerId, AddLine1, SpouseName, ImagefileName, IdProoffileName, lat, lng, IdProofFileNameII from NewParties a, SubAreaDetails b where a.SubAreaNo=b.SubAreaNo` |

**Purpose.** Lists the parties (borrowers) created offline on this device, and opens each one for viewing or correcting before upload.

**UI.**
- Toolbar "Today New Parties" (ta "புதிய கடன்தாரர்கள்").
- Rows: "LedgerName, Tag" and sub-area name.
- The "+" FAB stays visible but has no handler.

**Menu / actions.**
- Search on party name or sub-area.
- Row tap → party detail screen in offline-update mode.

**How it works.**
1. `fetchdata_fromnewpartiestable()` runs the query above and maps each row to `partydetails_data`.
2. Empty list → "Info": "No Parties Created today..". OK → `finish()`.
3. Any activity result → reload.

**Functions.**
- `onCreate()` — setup, first load, empty-list dialog.
- `fetchdata_fromnewpartiestable()` — SQLite read.
- `onContactSelected()` — open `partydetails_act`.
- `onActivityResult()` — reload.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — search; home.

**Rules & edge cases.**
- The FAB is a dead button.
- Parties whose `SubAreaNo` is missing from `SubAreaDetails` are not listed (inner join).
- Name search is effectively case-sensitive (see adapter).

## New Party Loans report — `newpartiesnewloanslist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `newloanslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_newloanslist.xml` (date bar shown, footers hidden) + `newloanslistrow.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer → Reports → "New Party Loans / புதிய கடன்தாரர்கள்" (`nav_newpartyloans`) → `selectbranchandfinance` rc 9022 (line and From/To dates). A weekly line goes through the weekday picker (rc 500) first. Extras: `financeid`, `weekdayid` (−1 for a daily line), `fromdate`, `todate` (java.sql.Date). |
| **Opens** | `selectedloancollectiondetails` (collection icon; `MCLoanId`, `iscollectioneditallowed`="0", `PartyName`, `Remarks`, `Tag`) |
| **Data** | Remote (`branchlevelReport_branchconnString`): `exec NewPartiesNewLoans @fromDate='<from>', @ReportDate='<to>', @IMEI='<globalvars.device_id>', @FinId=<fin>`, or `exec NewPartiesNewLoanswithweekdayid @weekdayid=<w>, …` (same parameters). Columns used: SubAreaName, LedgerName, CWLedgerId, PartyLedgerId, MCLoanId, MCLoanDate, InterestAmount, MCLoanAmountCollectable, InsAmount, TotalMonths, MobileNo. Local `Parties` supplies the Tag. |

**Purpose.** Report of loans given in the period to new parties. The server procedures decide which loans count as "new party" loans.

**UI.**
- Toolbar "New Loan Parties" (ta "புதிய கடன்தாரர்கள்").
- From / To date fields (tap → DatePicker) and a refresh icon that reloads.
- Rows use adapter calltype 2, so they show "Coll:" and the collection icon. "Coll:" and "DC:" are always 0 because Collected and DocCharges are not read from the result set.
- The FAB is visible but does nothing.

**Menu / actions.**
- Search as in `loanslist_act`.
- Collection icon → read-only collection history.
- Row tap builds an Intent for `newloancreation` (CallType 2) but never starts it, so it is a no-op.

**How it works.**
1. `ASYNCGetNewpartiesNewLoans` (ProgressDialog "Working") picks the procedure:
   - `weekdayid < 0` → `NewPartiesNewLoans`;
   - otherwise → `NewPartiesNewLoanswithweekdayid`.
2. Each row is mapped into an `mcloanobj`.
3. `onPostExecute` dismisses the progress dialog and refreshes the list.

**Functions.**
- `onCreate()` — extras, adapter, listeners, first load.
- `lambda$onCreate$1/2()`, `lambda$onCreate$4/5()` — From / To pickers.
- `lambda$onCreate$3()` — refresh.
- `onContactSelected()` — no-op (Intent never started).
- `oncollectiondetailslinkclicked()` — opens collection details; Tag from local `Parties`.
- `ASYNCGetNewpartiesNewLoans.doInBackground()` / `onPostExecute()` / `onPreExecute()` — query, then show.
- `lambda$onPostExecute$1()` — error-dialog OK → `finish()`.

**Rules & edge cases.**
- The error dialog can never appear: the catch block sets `iserroroccured=false`, and `ErrorText` is never set. A failure shows an empty list.
- Footers stay hidden.

## Parties to be Reviewed (closed & renewed loans) — `closed_renewedloanslist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `closed_renewedloanslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_closed_remevedloanslist.xml` (includes `customersearchlist` → `recycler_view`) + row `closed_renewedloansrow.xml`; menu `menucollectionareasearchlist` |
| **Opened from** | Drawer → Reports → "Parties to be Reviewed / முடிந்த கடன்கள்" (`nav_closedandrenewedloans`) → `selectbranchandfinance` rc 9021 (line and From/To dates). A weekly line goes through the weekday picker (rc 400; JADX shows the literal as `HttpStatus.SC_BAD_REQUEST`) first. Extras: `selectedfinanceid`, `weekdayid` (−1 or 1–7), `fromdate`, `todate`, `calledfrom`="closedloans". |
| **Opens** | `selectedloancollectiondetails` on row tap: `MCLoanId`, `iscollectioneditallowed`="0", `PartyName`, `Remarks`="", `Tag`=LedgerName. `prevloanslist_act` on a tap of the previous-loan cell: `partyledgerid`, `partyname`. `collectionarearearrangeact` only in unreachable code. |
| **Data** | Remote (`branchlevelReport_branchconnString`): `exec LoanClosedandRenewedforwindowsAPP @fromDate='<from>', @ToDate='<to>', @FinId=<fin>` (weekdayid < 0), or `exec LoanClosedandRenewedwithweekdayid @weekdayid=<w>, @fromDate=…, @ToDate=…, @FinId=…`. `exec NIPLoansClosed @IMEI='<device_id>', @FinId=<fin>` runs only when calledfrom="niploans", which no caller passes. Columns: PartyLedgerId, MCLoanId, SNo, SubAreaName, LedgerName, LoanIndex, CWLedgerId, MCLoanDate, MCLoanAmountCollectable, Collected, LastCollDate, LastCollection, NextLoanDate, NextLoanAmount. |

**Purpose.** Lists the loans that closed in a period and shows whether each party took a new (renewal) loan, and whether it was larger, smaller or the same. Management uses it to follow up parties who did not renew.

**UI.**
- Title "Closed Loans" (ta "முடிந்த கடன்கள்"). In the niploans mode: "Nippu Closed Loans" (ta "நிப்பிலிருந்து முடிந்த கடன்கள்").
- Date bar: "From : dd-MM-yyyy" and "To : …" (tap → DatePicker), plus refresh `acctranslistrefreshid`.
- Six summary tiles. Each is a clickable filter:

  | Tile | Tamil label | Rows shown |
  |---|---|---|
  | No of Closed Loans | முடிந்த கடன்கள் | all rows |
  | No of Parties get New Loans | மறு கடன் பெற்றவர்கள் | NextLoanAmount > 0 |
  | No of Parties not get New Loans | மறு கடன் பெறாதவர்கள் | NextLoanAmount = 0 (count = closed − renewed) |
  | No of Extra Amount Loans | கூடுதல் தொகை அளிக்கப்பட்ட மறு கடன்கள் | new loan > previous MCLoanAmountCollectable |
  | No of Less Amount Loans | குறைவான தொகை மறுகடன் பெற்றவர்கள் | 0 < new loan < previous |
  | No of Same Amount Loans | அதே தொகை மறு கடன்கள் | new loan = previous |

- `titletvid` shows the label of the active tile.
- Rows (`closed_renewedloansrow`). Column captions are hard-coded in Tamil: முந்தைய கடன் "previous loan", கடைசி வசூல் "last collection", புதிய கடன் "new loan".
  - "CW. LedgerName", in blue (0xFF0000FF) when there is no new loan; sub-area below.
  - Previous loan: "`<MCLoanAmountCollectable>/<LastCollDate dd-MM-yy>`".
  - Last collection: "`<LastCollection>/<LastCollDate>`".
  - New loan: "`<NextLoanAmount>/<NextLoanDate>`", in red (0xFFFF0000, shown by JADX as `SupportMenu.CATEGORY_MASK`) when larger than the previous loan; blank when there is none.

**Menu / actions.**
- Search: party name, sub-area or party number.
- The shared menu also adds "Rearrange Areas" and "Show in Map". Neither is handled, so both do nothing.
- Tile tap → filter the list.
- Row tap → collection history.
- Previous-loan cell tap → the party's loan history.

**How it works.**
1. `onCreate` reads the extras, shows the dates, wires the tiles and date fields, and runs `ASYNCGetClosedandRenewedloans` (ProgressDialog "Working").
2. `doInBackground` picks the procedure from calledfrom and weekdayid, then fills both the full list and the displayed (filtered) list.
3. `onPostExecute` computes the tile counts:
   - closed = row count;
   - renewed = rows with NextLoanAmount > 0;
   - same / less / extra = compare previous MCLoanAmountCollectable with NextLoanAmount;
   - not renewed = closed − renewed.
   It then resets the title line.
4. Each tile handler rebuilds the displayed list from the full list. The search box filters within the tile's subset.

**Functions.**
- `onCreate()` — setup and first load.
- `lambda$onCreate$1()` — refresh.
- `lambda$onCreate$2..7()` — the six tile filters.
- `lambda$onCreate$8..11()` — From / To DatePickers.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — search, and an unreachable `id == 0` branch.
- `onContactSelected()` — open collection details.
- `onthumnailclicked()` — open `prevloanslist_act`.
- `onActivityResult()` — unreachable handler (rc 100, nothing is started for it).
- `ASYNCGetClosedandRenewedloans.*` — load rows and tile counts.

**Rules & edge cases.**
- The previous-loan cell shows the last-collection date, not the loan date. `MCLoanDate` is fetched but never shown. Probably a bug.
- The `Tag` extra sent to collection details is the party name.
- The `id == 0` branch would start `collectionarearearrangeact` twice (forResult and plain); no menu item has id 0.
- The niploans mode is unreachable. If used, the date pickers would crash because the dates are null.
- Errors are swallowed and the list shows empty.

## Party loan history — `prevloanslist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `prevloanslist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_customersearch.xml` (FAB hidden) + row `prevloanlistrow.xml`; menu `menucustomersearchlist` |
| **Opened from** | `closed_renewedloanslist_act`: tap on the previous-loan cell (`partyledgerid`, `partyname`). `loanindexloanswiselist_act`: "Prev Loans" button (adds `LoanIndex`; needs special access 9). `partylistwithprevloansfornewloanselection`: prev-loans button (special access 9). |
| **Opens** | `archievedloandisplay` (`prevloan`=archievedloandisplay_data, `partyname`) for archived loans. `selectedloancollectiondetails` (`MCLoanId`, `PartyName`, `Remarks`, `Tag`) for all others. |
| **Data** | Remote (`selbranchconnString`): `[APP_ShowMCLStatus] @PLId=<party>,@Loanindex=<idx>`. For an archived loan: `Select AMCLoanId, MCLoanId, MCLoanDate, FWMCLoanId, FWUDMCLoanId, FinId, PartyLedgerId, GuaranderLedgerID, MCLoanAmount, TotalMonths, DCPercent, DocCharges, InterestAmount, MCLoanAmountCollectable, InsAmount, Remarks, WeekDayId, MonthDayId, mobileno, Sourceledgerid, LoanIndex, Closingdate, LastPaymentDate, MinCollection, MaxCollection, AvgCollection, MinGap, AvgGap, MaxGap, TotalCollection, ReportIdentification from ArchivedMCLoans where Mcloanid=<id>`. Local: `PrevLoansListTable` (dropped, re-created, filled, read), `Parties` (LedgerName, Tag), `MobileAppCollData` (Remarks). |

**Purpose.** Shows every loan a party has had, optionally limited to one loan-index slot, with amount, amount collected, days and status. Each loan opens either its collection history or the archived-loan summary.

**UI.**
- Title = party name, subtitle "LOANS".
- Rows: "Date:dd-MM-yyyy", "Loan No:<n>", "Loan:<amount>", "Collected:<paid>", "Days:<n>", and the status text (for example "A-Closed").

**Menu / actions.**
- Search matches the loan date as `yyyy-MM-dd` text.
- The menu's "Rearrange Parties" and "Show Parties in MAP" items are not handled.
- Row tap: see below.
- The swipe-to-refresh layout has no listener.

**How it works.**
1. `onCreate` reads `partyname`, `partyledgerid` and `LoanIndex` (0 when absent), then runs `ASYNCGetPrevLoanList` (ProgressDialog "Connecting").
2. `doInBackground`:
   - drops and re-creates `PrevLoansListTable(MCLoanId int, LoanNo int, LoanDate Date, LoanAmount int, Paid int, Status String, Days int)`;
   - runs `APP_ShowMCLStatus`;
   - inserts one row per loan.
3. `onPostExecute` → `fetchContacts()`: `Select … from PrevLoansListTable order by MCLoanId`.
4. Row tap:
   - Status "A-Closed" → `ASYNCGetSelectedArchievedloandetails` reads the `ArchivedMCLoans` row into `archievedloandisplay_data` and opens `archievedloandisplay`.
   - Otherwise it reads name and tag from `Parties` and remarks from `MobileAppCollData`, copies `globalvars.selbranch*` into `globalvars.branchlevelReport_*`, then opens collection details.

**Functions.**
- `onCreate()` — extras, adapter, load.
- `fetchContacts()` — read the local cache.
- `onContactSelected()` — archived or live drill-down.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — search; home.
- `ASYNCGetPrevLoanList.*` — server → cache.
- `ASYNCGetSelectedArchievedloandetails.*` — archived loan → display screen.
- `onActivityResult()` — empty.

**Rules & edge cases.**
- LoanIndex 0 is sent when no index is passed. What 0 means is decided inside the procedure, which is not in the APK.
- `iscollectioneditallowed` is not passed. `selectedloancollectiondetails` reads it with `getBoolean`, so editing is off by default. Other callers pass the String "0", which `getBoolean` also reads as false.
- Opening collection details overwrites the report connection with the working branch (side effect on other report screens).
- Errors are only printed.

## Loans of a party by loan index — `loanindexloanswiselist_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `loanindexloanswiselist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_customersearch.xml` (SwipeRefreshLayout + RecyclerView, FAB hidden, ProgressBar `cs_pbarid`) + row `loanindexloanwielistrow.xml`; menu `mnu_loanindexwiseloans` |
| **Opened from** | `partylistwithprevloansfornewloanselection` (the party picker of the new-loan flow), when the tapped party's LedgerId > 200 (an existing server party). Extras: `curpartyobj` (partydetails_data), `CallType` (the picker's own `calledfrom`, passed through). |
| **Opens** | `newloancreation`: `passedparty` (ServerGenLedgerId set to LedgerId), `loanindex`, `CallType`; forResult 8001 (JADX shows the literal as `constantvalues.interestcalcualationdetails_gettargetledger`). `prevloanslist_act`: `partyledgerid`, `partyname`, `LoanIndex`. |
| **Data** | Local, via `defaulttables.getselectedpartiesloanindexwiseloans()`: `loanindex` (LedgerId, LoanIndex, MCLoanId, MCLoanAmountCollectable, collected, Balance; filled by `downloaddata`) ⟕ `NewMCLoans` ⟕ `MobileAppCollData`. Refresh also updates `Parties.MaximumAmountAllowed`. Remote on Refresh (`selbranchconnString`): `Get_LoanindexwiseLoansforselectedparty @PartyLedgerId=<id>`. |

**Purpose.** A party can hold several loans at once, each in a numbered "loan index" slot. This screen lists the party's slots with the current loan amount and balance, and decides whether a new loan may be created in a slot.

**UI.**
- Title = party name, subtitle "LOANS".
- Rows:
  - "Loan Index" value.
  - "Loan Amount": MCLoanAmountCollectable, or the new loan's amount when the slot's collectable is 0.
  - "Balance" (shown only under the rule below).
  - Button "Prev\nLoans".
  - Label "New Loan" when an offline new loan already exists in this slot.
  - Label "Today Closed" when today's collection is > 0 and ≥ balance, and the user has full access or special access 18.

**Menu / actions.**
- Search (broken: any text empties the list).
- "Refresh" (`mnurefreshparties`) or swipe-down → reload from the server.
- Row tap → new-loan rules below.
- "Prev Loans" → loan history. Needs special access 9; otherwise toast "ACCES DENIED".

**How it works.**
1. Initial load from SQLite: `Select a.LedgerId, a.LoanIndex, a.MCLoanId, a.MCLoanAmountCollectable, a.collected, ifnull(c.MCLoanId,0) as CurLoanId, a.Balance+ifnull(b.MCLoanAmount,0) as Balance, ifnull(b.MCLoanAmount,0) as NewLoanAmount, ifnull(c.Collected,0) as TodayCollection from loanindex a left join NewMCLoans b on a.ledgerid=b.PartyLedgerid and a.LoanIndex=b.LoanIndex left join MobileAppCollData c on … where a.LedgerId=<party> order by a.LoanIndex`.
   - NewLoan = NewLoanAmount > 0.
   - IsClosed = TodayCollection > 0 and NewLoanAmount = Balance. Computed but never used.
2. `onLoanselected` decides whether a new loan may be opened. The checks run in this order:
   1. Balance ≤ 0, or TodayCollection ≥ Balance with full access or special access 18 ("create new loans against on-day-closed loans") → open `newloancreation` for that slot.
   2. Otherwise, if soft setting `enablemoniteringonloanclosoure_5` (id 14) is on:
      - the slot's loan is in today's collection list (CurLoanId > 0) → toast "ACCESS DENIED";
      - it is not → open `newloancreation`.
   3. Otherwise, no special access 18 and Balance > 0 → "ACCESS DENIED".
   4. Otherwise (TodayCollection < Balance) → "ACCESS DENIED", except with full access, where nothing happens.
3. `ASYNCRefresh`:
   - deletes the party's `loanindex` rows;
   - runs `Get_LoanindexwiseLoansforselectedparty` and inserts the returned rows;
   - writes MaximumAmountAllowed into local `Parties` and into `curpartyobj`;
   - reloads the list and stops the spinner.
4. Result 8001 (extras `ledgerid`, `isnewloan`, `loanindex`, `loanamount`) → marks the slot "New Loan" and sets its Balance and amount to `loanamount`.

**Functions.**
- `onCreate()` — party object, local load, swipe listener.
- `lambda$onCreate$1()` — swipe → refresh.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — search; Refresh; home.
- `onLoanselected()` — new-loan permission logic.
- `onprevloansbuttonclicked()` — history, gated by special access 9.
- `onActivityResult()` — apply the new-loan result.
- `ASYNCRefresh.*` — server refresh of the slot table.
- `onPointerCaptureChanged()` — trivial.

**Rules & edge cases.**
- The Balance column is visible with full access or special access 5 ("show balance in list"). Otherwise it shows only when the balance is 0 and soft setting 14 is off.
- The search filter's loop body is empty (confirmed in smali), so any query clears the list.
- "Prev Loans" uses the unfiltered list position.
- The two toasts are spelled differently: "ACCES DENIED" and "ACCESS DENIED".
- Before the first offline download the `loanindex` table does not exist, so the list stays empty until Refresh.

## Loan closure entry — `loanclosoureentry`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, default app theme with action bar; no Up arrow); implements `View.OnClickListener` |
| **Layout(s)** | `activity_loanclosoureentry.xml`; menu `menu_listingicononly` |
| **Opened from** | Drawer → Admin → "Loan Closures / கணக்கு முடித்தல்" (`nav_LoanClosures`). The drawer creates or clears local `closableliveloans`, then opens `selectbranchandfinance` rc 9010 (JADX: `fetch_guardiantyperequestcode`). That screen clears the cache again and passes `finidforloanclose` and `IsWeeklyFinance`. A legacy path in `refreshorreloadfinancelist` (calledfrom 2) passes only `findata` and has no caller. |
| **Opens** | `spinnersearch_list` (weekday "Collection Day"; rc 101 for a new entry, rc 201 for the list). `closableloanssearchlistact` (`finidforloanclose`, `weekdayid`; rc 305 = `requestcodes.loaclosure_loanselection`). `prevloanclosure_list_act` (`finidforloanclose`, `caltype`="app", `weekdayid`; rc 1011). |
| **Data** | Local: `closableliveloans` is cleared when the weekday changes, and the closed loan's row is deleted after saving. Remote (`selbranchconnString`): `{call dbo.InsertAccTransactionsMaster(?,?,?,?,?,?,?,?,?,?)}`, `{call dbo.InsertAPPMCLoanClosureTransDetailsP1(?,?,?,?,?,?,?)}`, `call dbo.InsertAPPMCLoanClosureTransDetailsP2 (?,?,?,?,?,?,?)}`, `{call dbo.InsertMCLoansClosoure(?,?,?,?,?,?,?,?)}`; tables `tbltransactionsmaster`, `tbltransactionsdetails`, `MCLoansClosoure`. Rights: role entry "nav_LoanClosures" in `globalvars.salesdetailsforandroid_datalist`; special access 10. |

**Purpose.** Closes ("settles") a live MC loan:
- records the amount collected against the amount collectable;
- books any shortfall (collection loss) to a loss ledger;
- records any excess collection;
- sets the closing date.
A list icon shows previous closures, which can be deleted. Re-dating is coded but broken (see Rules).

**UI.**
- Card "PARTY DETAILS": party name `loanclose_partynameid` and area `loanclose_collectionareanameid`. Tapping the card picks a loan. It is clickable only with the Add right.
- Card "LOAN DETAILS":
  - "Loan Date" `loanclose_loandateid`.
  - "Loan Amount / கடன் தொகை" `loanclose_collectableid` = MCLoanAmountCollectable.
- Amount fields. All are disabled and non-editable in the XML, so they display computed values only:
  - "Collected Amount / வசூலான தொகை" `loanclose_collectedid`.
  - "Extra Collection / கூடுதல் வசூல்" `loanclose_extracollectedid`.
  - "Collection Loss / வசூல் குறைவு" `loanclose_vasoolkuraivuid`.
- "Closed Date / முடிவு தேதி" `loanclose_closedondateid`. Enabled in code only when the user has special access 10.
- Buttons:
  - "NEW" `loanclose_newbuttonid` (hidden initially).
  - "Update / சேமி" `loanclose_savebuttonid`.
  - "DELETE / நீக்கு" `loanclose_deletebuttonid` (red).
- Window titles: "Loan Closure New Entry" or "Loan Closure Edit Entry".

**Menu / actions.**
- Toolbar "LIST" icon (`translistid`, needs the View right) → previous closures. A weekly line asks for the weekday first (rc 201).
- PARTY DETAILS card → pick a loan. A weekly line asks for the weekday first (rc 101).
- Closed-date field → DatePicker (special access 10).
- Update → runs the save task in the current mode. After a loan is picked the mode is 1021 (new entry). Edit mode never sets its own mode (see step 5).
- DELETE → delete mode (1024).
- NEW → clears the form and immediately reopens the loan picker.

**How it works.**
1. `onCreate`:
   - sets the title "Loan Closure New Entry";
   - enables the date field only with special access 10;
   - reads `finidforloanclose`, `IsWeeklyFinance`, `findata` and `requestcode`;
   - copies the role's "nav_LoanClosures" rights; full access sets Add, Delete, View, Update and Show to true;
   - hides Update and Delete.
2. **Pick a loan** (card tap):
   - Weekly line: weekday picker (rc 101). If the chosen weekday differs from the previous one, `delete from closableliveloans`.
   - Then `closableloanssearchlistact` (rc 305). A daily line passes weekdayid −1.
3. **rc 305 result** (`loancloseobj`, a `loanclosureloanlist_data`). Mode becomes 1021. The screen fills party, area, loan date and amount, then computes:
   - Collected = Collectable − Loanbalance, which equals the server's PrevCollected.
   - If Collected ≥ Collectable: ExtraCollection = Collected − Collectable, and Loss shows 0.
   - Otherwise: Pending (Collection Loss) = Collectable − Collected.
   - ClosedDate = today (device clock). Update becomes visible.
4. **Save, new entry (1021)**, task `ASYNCSaveLoanClosure` (ProgressDialog "Updating"):
   1. Only if Pending ≠ 0, post the write-off voucher:
      - `InsertAccTransactionsMaster`: TransId (out), TransDate = ClosedDate, TransType = 23, Narration '', FinId = `globalvars.finid`, TransAmount = Pending, TransLedgerId = 125, FWVWTransId / FWVWUDTransId (out), ImageFileName ''.
      - `InsertAPPMCLoanClosureTransDetailsP1(TransId, PartyLedgerId, Pending, 'Source', 'Cr', 1, 2)`.
      - `InsertAPPMCLoanClosureTransDetailsP2(TransId, 125, Pending, 'Target', 'Dr', 2, 1)`.
      - Effect: Dr ledger 125 (loss / write-off), Cr the party's loan account.
   2. Always: `InsertMCLoansClosoure` with LCId (out), MCLoanId, ClosedDate, Collectable, Collected, Pending, extracollection, TransId. TransId stays 0 when no voucher was posted.
   3. Delete the loan from local `closableliveloans`.
5. **Edit**: LIST → `prevloanclosure_list_act` → rc 1011 with `prevloanclosure`.
   - Title "Loan Closure Edit Entry". Delete and Update are shown, enabled by the Delete and Update rights.
   - The fields are filled from the closure, and Extra / Pending are recomputed.
   - The save task has an edit branch (mode 1011) that would change only the date:
     - `update tbltransactionsmaster set TransDate='<ClosedDate>' where transid=?`;
     - if TransId ≠ 0: `delete from tbltransactionsdetails where transid=?`, then P1 and P2 are inserted again with the same Pending;
     - `update MCLoansClosoure set ClosedDate='<date>' where MCLoanId=?`.
   - **That branch is unreachable.** The mode field `request_resultcode` is only ever set to 1021, 1024, or the `requestcode` extra, which no caller passes (so 0). This is confirmed in smali. Pressing Update in edit mode therefore runs whatever mode was used last in this session:
     - 0 (fresh screen) → nothing happens;
     - 1021 (after a new closure or NEW) → the new-entry branch runs again and inserts a second `MCLoansClosoure` row for the same loan, plus a second TTID 23 voucher if Pending ≠ 0;
     - 1024 (after a delete) → the selected closure is deleted.
6. **Delete (1024)**:
   - if TransId ≠ 0: `delete from tbltransactionsdetails where transid=?`, then `delete from tbltransactionsmaster where transid=?` is prepared but **never executed**;
   - `delete from MCLoansClosoure where MCLoanId=?`.
7. `onPostExecute` (every mode) dismisses the progress dialog, shows NEW and hides Update and Delete. No message is shown and no result is returned to the caller.

**Functions.**
- `onCreate()` — rights, special access 10, listeners.
- `lambda$onCreate$1()` — card → pick a loan (weekday picker first on a weekly line).
- `lambda$onCreate$2()` — DELETE → mode 1024, save task.
- `lambda$onCreate$3()` — NEW → clear the form and pick a loan.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — LIST → previous closures (weekday picker first on a weekly line).
- `onClick()` — closed-date picker; Update → save task.
- `lambda$onClick$4()` — DatePicker result → ClosedDate.
- `onActivityResult()` — rc 101 / 201 weekday results; rc 305 loan picked; rc 1011 closure to edit.
- `ASYNCSaveLoanClosure.doInBackground()` / `onPostExecute()` / `onPreExecute()` — server writes for modes 1021 and 1024 (the 1011 branch is unreachable).

**Rules & edge cases.**
- Editing a previous closure is broken: Update runs the last mode used (nothing, a duplicate insert, or a delete), never the date-only update. See step 5.
- Amounts cannot be adjusted by hand. Even when the user types, saving uses the computed `closureloanobj` values.
- Extra collection gets no accounting entry; it is stored only in `MCLoansClosoure.extracollection`.
- Only special access 10 unlocks the closing date. Full access is not checked here.
- Delete leaves the TTID 23 master row in `tbltransactionsmaster` (statement never executed, confirmed in smali). The orphan row keeps the loss amount.
- Delete does not put the loan back into the local `closableliveloans` cache.
- The voucher's FinId is `globalvars.finid` (the app's current line), not the line chosen in the picker (`finidforloanclose`).
- Live loans are read from `branchlevelReport_branchconnString`, but writes go to `selbranchconnString`. Wrong branch if the two differ.
- There is no validation and no success or failure feedback. Exceptions are only printed.
- In edit mode the area label is overwritten with the raw MCLoanDate (`yyyy-mm-dd`). Bug.
- With special access 10, tapping the date before picking a loan dereferences a null `closureloanobj` → crash.
- On a daily line, LIST passes `weekdayid` 0 (the field's default) instead of −1.
- The P2 call strings are missing the opening `{`. The bundled jTDS (`JtdsPreparedStatement.normalizeCall`) rewrites them to `{ call …}}` and runs them as an RPC by procedure name, so they most likely still work. Fragile, not a functional bug.
- Ledger 125 is hard-coded; its name is not in the app.
- The full-access override mutates the shared global rights object.

## Live loans picker for closure — `closableloanssearchlistact`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `loanclosureloanlist_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_employeesearch.xml` (FAB `addnewemployeeid` hidden, `progress_Overlay` used) + row `loanclosurelistrow.xml`; menu `menuwithsearchandorderbyoptions` |
| **Opened from** | `loanclosoureentry` (rc 305; `finidforloanclose`, `weekdayid`). The rc 100 handlers in `selectbranchandfinance` and `refreshorreloadfinancelist` would also open it, but nothing starts rc 100 (unreachable). |
| **Opens** | — Returns resultCode 1 with `loancloseobj` (loanclosureloanlist_data) and `requestcode`=1021. |
| **Data** | Local: `closableliveloans` (cache; columns MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, PaidDues, PassedDues, LoanMethodDesc, ReportIdentification, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, SubAreaName). `tempLoanClosure` is created but never used. Remote (`branchlevelReport_branchconnString`): `[APP_ListofLiveLoans] @ColDate='31-Dec-2050', @FinId=<fin>` or `[APP_ListofLiveLoanswithWeekdayid] @ColDate='31-Dec-2050', @weekdayid=<w>, @FinId=<fin>`. |

**Purpose.** Pick the live loan to close.

**UI.**
- Title "Live Loans". A full-screen progress overlay shows while downloading.
- Rows:
  - "CW. PartyName";
  - "SubAreaName / ReportIdentification";
  - loan date `dd-MM-yyyy`;
  - "Collectable/Balance".

**Menu / actions.**
- Search.
- "Order by Party No", "Order by Loan Date", "Order by Balance".
- Row tap → return the loan to the caller.

**How it works.**
1. `onCreate` creates `tempLoanClosure`, then calls `getclosableloans()`:
   - reads the cache;
   - Loanbalance = Collectable − PrevCollected;
   - sorts by balance, ascending.
2. If the cache is empty, `ASYNCLoanClosures`:
   - clears the cache;
   - runs the procedure (the weekday variant when weekdayid ≥ 0);
   - inserts every row;
   - `onPostExecute` reloads from the cache.
3. How "closable" loans are chosen: the app does not filter. Every loan the procedure reports as live on 31-Dec-2050 (in effect, every open loan) can be closed. Sorting by balance puts fully or nearly paid loans first.

**Functions.**
- `onCreate()` — tables, adapter, cache or download.
- `getclosableloans()` — cache → list, then sort.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — search and the three sorts.
- `onContactSelected()` — `setResult(1)` and finish.
- `onActivityResult()` — unreachable row-removal handler.
- `ASYNCLoanClosures.*` — server → cache.

**Rules & edge cases.**
- The cache is reused for the whole session. There is no refresh action; the cache is reset only from the drawer and picker or when the weekday changes.
- Errors are stored in `ErrorText` and never shown.
- Rows that fail to parse are skipped silently.
- Search quirks are described under the adapter.

## Previous loan closures — `prevloanclosure_list_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `prevloanclosure_adapter.partyledgerlistlistner` |
| **Layout(s)** | `activity_prevloanclosure.xml` + row `prevloanclosure_row.xml`; menu `menucustomersearchlist` |
| **Opened from** | `loanclosoureentry` LIST icon (rc 1011; a weekly line goes through the weekday picker rc 201 first). The `selectbranchandfinance` rc 9011 route exists, but no drawer item uses it. |
| **Opens** | — Returns resultCode 1 with `prevloanclosure` (prevloanclosure_data) and `requestcode`=1011. |
| **Data** | Remote (`branchlevelReport_branchconnString`): `MCLoanClosureList @Weekdayid=<w> , @finid=<fin>, @fromdate='<from>', @todate='<to>'`. Columns: TransId, MCLoanId, CWLedgerId, PartyLedgerId, SubAreaNo, MCLoanDate, SubAreaName, LedgerName, Collected, Pending, ExtraCollection, ClosedDate, MCLoanAmountCollectable. |

**Purpose.** Lists loan closures in a date range. The user picks one to open it in `loanclosoureentry`. There only Delete works reliably; Update in edit mode is broken (see that screen).

**UI.**
- Title "Prev Loan Closures" ("Closed Loans" when there are no extras).
- Date bar: From (default today − 7 days) / To (today), plus refresh.
- Header columns "Party/SubArea", "L.Date/Extra Coll", "C.Date/Pending".
- Rows: party number, name, loan date, closed date, area, extra (shown only if ≠ 0), pending (shown only if ≠ 0).

**Menu / actions.**
- Search.
- From / To date pickers and refresh.
- Row tap → return the closure to the caller.
- The menu's "Rearrange Parties" and "Show Parties in MAP" items are not handled.

**How it works.**
1. `ASYNCGetPrevLoanList` (ProgressDialog "Connecting") runs `MCLoanClosureList` and maps each row to `prevloanclosure_data`.
2. A row tap returns it to the caller and finishes.

**Functions.**
- `onCreate()` — date defaults, extras, load.
- `lambda$onCreate$1()` — refresh.
- `lambda$onCreate$2..5()` — From / To pickers.
- `onContactSelected()` — return the selection.
- `onActivityResult()` — unreachable (rc 100).
- `ASYNCGetPrevLoanList.*` — query.

**Rules & edge cases.**
- Side effect: sets `globalvars.accyearfromdate` / `accyeartodate` from soft setting `currentfinancialyearid_20`, then ignores them for the range.
- The search filter calls `Integer.parseInt(query)` when name and area do not match. Text that matches neither throws; Android's Filter then publishes null and the adapter likely crashes.
- When no row matches, the filter shows the full list.

## Live saving accounts — `closablesavings_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `closablesavings_adapter.ContactsAdapterListener` |
| **Layout(s)** | `activity_employeesearch.xml` (FAB hidden) + row `closablesavings_row.xml`; menu `menucustomersearchlist` |
| **Opened from** | Drawer → Executive Entries (offline) → "Savings Account Closures / சேமிப்பு திட்ட முடிப்பு" (`nav_SavingsClosures`) → `selectbranchandfinance` rc 9013 → `finidforloanclose` (the chosen line), `weekdayid`=−1 (ignored) |
| **Opens** | `closingsavingsaccount` (`passedaccount`, `CallType`=1; forResult 100) |
| **Data** | Remote (`selbranchconnString`): `GetClearableSavingAccounts @FinId=<fin>`. Columns: LedgerId, SubAreaNo, LedgerName, SubAreaName, CWLedgerId, Tag, TargetLedgerId, SavingsAmount. |

**Purpose.** Lists the savings accounts that can be closed on a line, and opens the closure form.

**UI.**
- Title "Live Saving Accounts".
- Rows: party name ("Party Name / பார்ட்டி பெயர்"), business tag ("Business / தொழில்"), sub-area, savings amount.

**Menu / actions.**
- Search: digits → exact party number; text → name.
- Row tap → closure form.
- The menu's "Rearrange Parties" and "Show Parties in MAP" items are not handled.

**How it works.**
1. `ASYNCLoanClosures` (ProgressDialog "downloading, please wait ...") loads the list.
2. On error: dialog "Error Info" showing "`<line>.<message>`". OK finishes the screen.
3. Result 1 from the closure form → the row with the same LedgerId is removed.

**Functions.**
- `onCreate()` — setup and load.
- `onContactSelected()` — open `closingsavingsaccount`.
- `onActivityResult()` — remove the closed account.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — search; home.
- `ASYNCLoanClosures.*` — query and error dialog.

**Rules & edge cases.**
- There is no access-rights check in this screen.
- The class name `ASYNCLoanClosures` is a copy-paste from the loan screens.

## Savings account closure — `closingsavingsaccount`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, default theme with action bar); implements `View.OnClickListener` |
| **Layout(s)** | `activity_closingsavingsaccount.xml` |
| **Opened from** | `closablesavings_list` row tap (`passedaccount`=closablesavings_data, `CallType`=1; rc 100) |
| **Opens** | — Returns resultCode 1 with `passedaccount`. |
| **Data** | Remote (`selbranchconnString`): `{call dbo.InsertAccTransactionsMaster(...)}` (TTID 28), `{call dbo.InsertAPPMCLoanClosureTransDetailsP1(...)}`, `call dbo.InsertAPPMCLoanClosureTransDetailsP2 (...)}` ×2, `insert into SavingsClosure (TransDate,PartyLedgerId,Amount,AddInterest,Transid)values(?,?,?,?,?)`. Local: the line's `CashLedgerId` from `SelectedBranchFinanceDetails`, falling back to `FinanceDetails` (`defaulttables.fetchstaffcashbookledgerid`). |

**Purpose.** Pays out and closes a party's savings account. The user can add interest. The payout is booked from the line's cash ledger.

**UI.**
- Window title "Loan Closure Entry" (copy-paste; this is the savings closure).
- Party name.
- "Balance Available / இருப்புத் தொகை" = SavingsAmount.
- "Interest / வட்டி" EditText (decimal).
- "Total / மொத்தம்" = balance + interest, updated live by a TextWatcher.
- "Date / நாள்" (default today). Tappable for a DatePicker only when `globalvars.isFullAccessAllowed`.
- Button "SAVE".

**Menu / actions.**
- SAVE → posts the closure.
- Date field → DatePicker (full access only).

**How it works.**
1. SAVE reads Total into `paidamout` and Interest into `interestamount`, then runs `ASYNCSave_savingsclosure`. The window is made untouchable (`FLAG_NOT_TOUCHABLE`, 16) while it runs.
2. Master voucher: `InsertAccTransactionsMaster` with TransDate = date, TransType = 28, FinId = `globalvars.finid`, TransAmount = Total, TransLedgerId = 112 (interest-paid-on-savings ledger). Returns TransId.
3. Detail lines:

   | Call | Ledger | Amount | Side | Sequence |
   |---|---|---|---|---|
   | P1 | line cash ledger | Total | 'Source', 'Cr' | 1, 2 |
   | P2 | `TargetLedgerId` (savings ledger) | Total | 'Target', 'Dr' | 2, 1 |
   | P2 | 112 | Interest | 'Target', 'Dr' | 3, 1 |

4. `insert into SavingsClosure`: TransDate, PartyLedgerId = TargetLedgerId, Amount = SavingsAmount, AddInterest = Interest, Transid.
5. Success → `setResult(1, passedaccount)` and finish. Error → "Error Info" dialog with "`<line>.<message>`"; OK finishes.

**Functions.**
- `onCreate()` — fill fields, TextWatcher, date default.
- `onTextChanged()` — Total = balance + interest.
- `onClick()` — date picker; SAVE.
- `lambda$onClick$1()` — date set.
- `ASYNCSave_savingsclosure.doInBackground()` / `onPostExecute()` / `onPreExecute()` — voucher and `SavingsClosure` insert; result.

**Rules & edge cases.**
- As coded, the entry is unbalanced whenever interest > 0. The savings ledger is debited the full Total (balance + interest), ledger 112 is debited the interest again, and cash is credited only the Total.
- A zero-amount interest line is inserted even when interest is 0.
- There is no validation. Typing a lone "." crashes `Double.parseDouble` in the TextWatcher.
- The date is editable only with full access; no special access applies.
- The voucher uses `globalvars.finid`, not the line chosen in the picker.
- The P2 string is missing its `{` (see loan closure; jTDS normalizes it).

## Previous saving closures — `prevsavingclosure_list_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, `AppTheme.NoActionBar`); implements `prevsavingclosure_adapter.partyledgerlistlistner` |
| **Layout(s)** | `activity_prevsavingclosure.xml` + row `prevsavingclosure_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | Drawer → Admin → "Prev Saving Closure / முடிக்கப்பட்ட சேமிப்பு க/கு" (`nav_prevsavingclosure`) → `selectbranchandfinance` rc 9012. A weekly line goes through the weekday picker (rc 300) first. Extras `finidforloanclose` and `weekdayid` are both ignored. |
| **Opens** | — |
| **Data** | Remote list (`branchlevelReport_branchconnString`): `Savingaccountclosure` with no parameters. Columns: SCId, TransId, TransDate, PartyLedgerId, LedgerName, SubAreaName, Amount, AddInterest. Delete (`selbranchconnString`): `delete from SavingsClosure where Transid=?`, `delete from tbltransactionsdetails where Transid=?`, `delete from tbltransactionsmaster where Transid=?`. |

**Purpose.** Lists past savings-account closures. A long-press deletes one, reversing its voucher in full.

**UI.**
- Title "Saving Account Closure List".
- Header "Date / Party / Amount / Interest".
- Rows: date, name, amount, interest, sub-area.

**Menu / actions.**
- Search on party name (case-insensitive).
- Long-press a row → dialog "Confirm Delete": "Sure to delete?..." with YES / NO.

**How it works.**
1. `ASYNCGetPrevLoanList` loads the list. The window is untouchable while it loads.
2. YES → `ASYNCDeletesavingclosure` (ProgressDialog "Processing....") runs the three deletes, then removes the row from the list on the background thread.
3. `onPostExecute` shows dialog "Confirmation": "Deleted...".

**Functions.**
- `onCreate()` — setup and load.
- `onContactSelected()` — delete confirmation.
- `lambda$onContactSelected$1()` — YES → delete task.
- `ASYNCGetPrevLoanList.*` — list.
- `ASYNCDeletesavingclosure.*` — delete and confirmation.
- `onActivityResult()` — empty.

**Rules & edge cases.**
- Delete has no permission check.
- "Deleted..." is shown even when the delete failed.
- The list holds every closure in the report-branch database, whatever line or weekday was picked, with no date filter.
- The list is modified off the UI thread.

## Party transfer — `partytransfer`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, default theme, title "PARTY TRANSFER") |
| **Layout(s)** | `activity_partytransfer.xml` |
| **Opened from** | Drawer → Executive Entries offline → "Party Transfer / ஊர் மாற்றம் செய்தல்" (`nav_partytransfer`), and the online group's "Party Transfer" (`nav_onlinepartytransfer`). No extras. Both open the same screen with identical behaviour. |
| **Opens** | `spinnersearch_list` (`title`, `searchlist`): "Currently In Collection Area" rc 7017, "Party" rc 7019, "New Collection Area" rc 7018. Each returns `displaymember` / `valuemember`. |
| **Data** | Local pickers: `select SubAreaNo,SubAreaName from SubAreaDetails where finid=<globalvars.finid> order by ShowId`. Target areas: the same query with `and SubAreaNo != <source>`. Parties: `MobileAppCollData` ⟕ `Parties`, shown as "PartyName, tag", where `a.SubAreaNo=<source>`. Remote (`selbranchconnString`): `update tblLedgers set SubAreaNo=<target> where LedgerId=<party>`. Local: `update MobileAppCollData set SubAreaNo=…`, `update MobileAppCollData set SubAreaName='<NewAreaName>'`, `update Parties set SubAreaNo=…`. |

**Purpose.** Moves a party (borrower) from one collection area (sub-area) of the current line to another, on the server and on the device.

**UI.** Three read-only pick boxes and one button:
- "Current Collection Area / தற்போது இருக்கும் பகுதி";
- "Party / பார்ட்டி";
- "New Area / மாற்ற வேண்டிய பகுதி";
- button "Update / சேமி".

**Menu / actions.**
- Tap each box → searchable picker.
- Update → validate, then transfer.

**How it works.**
1. The user picks the source area first. The party list then shows only parties of that area that appear in the downloaded `MobileAppCollData`. The target list excludes the source area.
2. Update with no target area or no party → dialog "Incomplete Data": "Kindly ensure whether Party and New Area Selected".
3. `AsyncPartyTransfer` (ProgressDialog "Transfering ...") runs the server update, then the three local updates.
4. Toast "Transfer Completed". The fields are not cleared.

**Functions.**
- `onCreate()` — wire the pickers and the button.
- `lambda$onCreate$1/2/3()` — open the source-area / target-area / party pickers.
- `lambda$onCreate$4()` — validation and run.
- `onActivityResult()` — store the picked ids and names.
- `showincompletedataalert()` — validation dialog.
- `AsyncPartyTransfer.*` — updates and toast.

**Rules & edge cases.**
- `NewAreaName` is never assigned, so the local `MobileAppCollData.SubAreaName` becomes the literal string 'null'.
- "Transfer Completed" is shown even on failure. `ErrorText` ("Connection Error,Please try again...") is set but never displayed.
- Tapping Party or New Area before Current Area dereferences a null `SourceAreaId` → crash.
- Only parties in today's downloaded collection list can be moved. Online users without a download see no parties.
- The party picker label is built with SQLite `PartyName || ', ' || tag`. A party with a NULL tag, or with no `Parties` row, gets a NULL label.
- The server change touches only `tblLedgers`.
- The screen itself has no role or special-access check.

## Rearrange parties — `partyrearrange`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity; manifest label "Rearrange Parties"; default theme, action bar tinted Green) |
| **Layout(s)** | `activity_partyrearrange.xml` (`CustomListView` drag list; rows `collectionarearearangelist_row` via `collectionarearearrangeAdapter`, documented elsewhere); menu `menu_partyrearrange` |
| **Opened from** | Each caller passes `PLID` (sub-area number) and `areaname`, and starts the screen forResult rc 1: <br>• `tabbedcollectionlistmviewpager` menu "Rearrange Parties", visible only with special access 20.<br>• `partylistwithprevloansfornewloanselection` menu "Rearrange Parties", visible only with special access 20; areaname "".<br>• `tabbedcollectionlist`: tap on the area-name header, with no access check. Its menu id 0 path passes the key "AreaName" instead of `areaname`. |
| **Opens** | — Returns resultCode 1 after the sync. |
| **Data** | Local list: `select distinct(LedgerId), LedgerName, Tag, b.PartyLedgerId, a.CWLedgerId from Parties a left join MobileAppCollData b on a.LedgerId=b.PartyLedgerId [where a.SubAreaNo=<PLID>] order by a.UDLedgerId`. The area filter is dropped when `globalvars.isoverallcollectionenabled`. Writes: local `Parties.UDLedgerId`, `MobileAppCollData.UDLedgerId`, and the in-memory `globalvars.tabbedcollectionlistdataList`. Remote (`selbranchconnString`): batch `update tblLedgers set UDLedgerId=<n> where LedgerId=<id>;…` for every local party of the area. |

**Purpose.** Drag-and-drop reordering of an area's parties into the collection route order. "Save and Sync" saves the order (`UDLedgerId` = 1..n) locally and on the server.

**UI.**
- Title "Rearrange-`<areaname>`".
- Rows show "`CWLedgerId`.LedgerName" and the Tag, with a colour bar: green 0xFF78CE55 when the party has a loan in `MobileAppCollData`, blue-violet 0xFF5C55CE otherwise.
- Touching the grab icon or either text starts the drag. `CustomListView.onGrab`, then `swapElements` swaps the list items.

**Menu / actions.** Overflow "Save and Sync" (`action_saveandsync`).

**How it works.**
1. For each list position i, the party's LedgerId is read from the tag of `textSecondaryDisplayMemberId`, using `getViewByPosition` for rows off screen. Then:
   - `update Parties set UDLedgerId=i+1 where LedgerId=…`;
   - `update MobileAppCollData set UDLedgerId=i+1 where PartyLedgerId=…`;
   - the in-memory collection list is patched;
   - `SyncWithServer()` is called.
2. `Asyncupdateudledgerid` (ProgressDialog "uploading ..."):
   - reads `Select LedgerId,UDLedgerId from Parties where SubAreaNo=<PLID>`;
   - builds one multi-statement UPDATE batch and runs it on the server;
   - `onPostExecute` → `setResult(1)` and `finish()`.

**Functions.**
- `onCreate()` — query, adapter, drag listener.
- `lambda$onCreate$1()` — grab → `listView.onGrab()`.
- `lambda$onCreate$2()` — swap two items.
- `getViewByPosition()` — get the row view for any position.
- `SyncWithServer()` — start the upload task.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()` — "Save and Sync".
- `Asyncupdateudledgerid.*` — server batch update and finish.

**Rules & edge cases.**
- `SyncWithServer()` is called inside the per-row loop. This queues one identical full-area upload per party; the screen finishes when the first one completes.
- In overall-collection mode, all areas' parties are numbered together, but only the passed area is synced to the server.
- The screen itself does not check special access 20.
- Server errors are stored in `ErrorText` and never shown.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `newloanslist_adapter` | RecyclerView adapter, Filterable | `loanslist_act`, `newloanslist_act`, `newpartiesnewloanslist_act` | Row `newloanslistrow`: "CW. PartyName" (number left out when CWLedgerId = 0), area, Loan = MCLoanAmountCollectable, Ins = TotalMonths, Int = InterestAmount, Date = MCLoanDate, DC = DocCharges. Constructor arg `calltype`: 2 → "Coll:" + Collected and the collection icon; 1 with `enableondaycollection_27` → "Onday Coll :" + ondaycollection; otherwise hidden. Callbacks `onContactSelected(mcloanobj,pos)` (row) and `oncollectiondetailslinkclicked(mcloanobj,pos)` (icon). Filter: digits → CWLedgerId equal; text → area name (lower-cased) contains the raw query, or party name contains it. |
| `closed_renewedloanslist_adapter` | RecyclerView adapter, Filterable | `closed_renewedloanslist_act` | Row `closed_renewedloansrow`: "CW. LedgerName" (blue if not renewed), sub-area, previous loan "collectable/LastCollDate", last collection "amount/LastCollDate", new loan "amount/NextLoanDate" (red when larger). Callbacks `onContactSelected` (row) and `onthumnailclicked` (previous-loan cell). Filter: the query is upper-cased but compared with lower-cased names, so letters never match by name; sub-area match is case-sensitive; CWLedgerId by substring. |
| `loanindexloanswiselist_adapter` | RecyclerView adapter, Filterable | `loanindexloanswiselist_act` | Row `loanindexloanwielistrow`: loan index, loan amount, balance. Balance is visible with full access or special access 5, or when the balance is 0 and soft setting 14 is off. Labels "New Loan" and "Today Closed" (needs full access or special access 18). Callbacks `onLoanselected` (row, filtered list) and `onprevloansbuttonclicked` ("Prev Loans" button, unfiltered index). The filter loop is empty, so any query empties the list. |
| `prevloanclosure_adapter` | RecyclerView adapter, Filterable | `prevloanclosure_list_act` | Row `prevloanclosure_row`: CWLedgerId, LedgerName, MCLoanDate, ClosedDate, SubAreaName; ExtraCollection and Pending (blank when 0). Interface `partyledgerlistlistner.onContactSelected`. Filter: name (case-insensitive), sub-area, or `CWLedgerId == Integer.parseInt(query)`, which throws on text. An empty result falls back to the full list. |
| `loanclosureloanlist_adapter` | RecyclerView adapter, Filterable | `closableloanssearchlistact` | Row `loanclosurelistrow`: "CW. PartyName", "SubAreaName / ReportIdentification", loan date, "Collectable/(Collectable−PrevCollected)". Callback `onContactSelected(data)`. Filter: digits → CWLedgerId equal; text → raw name, CWLedgerId, ReportIdentification or collectable contains the query (the lower-case comparison never matches letters). |
| `prevloanslist_adapter` | RecyclerView adapter, Filterable | `prevloanslist_act` | Row `prevloanlistrow`: "Date:", "Loan No:", "Loan:", "Collected:", "Days:", Status. Callback `onContactSelected`. Filter: `LoanDate.toString()` (yyyy-MM-dd) contains the query. |
| `closablesavings_adapter` | RecyclerView adapter, Filterable | `closablesavings_list` | Row `closablesavings_row`: LedgerName, Tag, SubAreaName, SavingsAmount. Callback `onContactSelected`. Filter: digits → CWLedgerId equal; text → name (same upper/lower-case quirk). |
| `prevsavingclosure_adapter` | RecyclerView adapter, Filterable | `prevsavingclosure_list_act` | Row `prevsavingclosure_row`: TransDate, LedgerName, Amount, AddInterest, SubAreaName. Long-press → `partyledgerlistlistner.onContactSelected` (delete prompt). An empty `ContactsAdapterListener` interface is declared but unused. Filter: name contains the query (case-insensitive). |
| `newpartieslist_adapter` | RecyclerView adapter, Filterable | `newpartieslist_act` | Row `newpartieslistrow`: "LedgerName, Tag" and SubAreaName. Callback `onContactSelected(partydetails_data,pos)`. Filter: name (upper/lower-case quirk) or sub-area (case-sensitive). |
| `mcloanobj` | Serializable model | This module plus `newloancreation`, `directcollectionentry`, `tabbedcollectionlist`, `tabbedcollectionlistmviewpager`, `tabbedindividualcollectionentry`, `uploadcollection`, `defaulttables`, `documentsphotossearchlist`, `navigationdrawactivity` | One MC loan: MCLoanId, MCLoanDate (sql Date), FinId, PartyLedgerId, PartyName, CWLedgerId, Tag, AreaName, SubAreaNo, MCLoanAmount, MCLoanAmountCollectable, MCLoanAmountEstimated, TotalMonths, InsAmount, InterestAmount, intrate (double), DocCharges, LoanMethodId, ondaycollection, Collected, balanceamount, AdvanceAmount, Productsvalue, Loanindex, WeekDayId, MonthDayid, mobileno, Partyreference, sourceledgerid, Remarks, ServerGenMCLoanId, ServerGenLedgerId, ServerGenTransId. Getters and setters only. |
| `loanclosureloanlist_data` | Serializable model | `closableloanssearchlistact`, `loanclosoureentry`, `loanclosureloanlist_adapter` | One live loan from `closableliveloans`: MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, PartyName, CWLedgerId, UDLedgerId, SubAreaNo, SubAreaName, MCLoanDate, ClosingDate, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, Loanbalance (computed), PaidDues, PassedDues, NoOfIns, LoanMethodDesc, ReportIdentification, CollectionBreakString, CBCount, istobeWarned, IsNIP, LoanStatus (Double). |
| `prevloanclosure_data` | Serializable model | `loanclosoureentry`, `prevloanclosure_list_act`, `prevloanclosure_adapter` | One closure, also the closure form's working object: TransId, MCLoanId, PartyLedgerId, LedgerName, CWLedgerId, SubAreaNo, SubAreaName, MCLoanDate, ClosedDate, MCLoanAmountCollectable, Collected, Pending, ExtraCollection. |
| `closed_renewedloans_data` | Model (not Serializable) | `closed_renewedloanslist_act`, `closed_renewedloanslist_adapter` | SNo, PartyLedgerId, LedgerName, CWLedgerId, SubAreaName, MCLoanId, LoanIndex, MCLoanDate, MCLoanAmountCollectable, Collected, LastCollDate, LastCollection, NextLoanDate, NextLoanAmount, MobileNo1/2 (never filled here). |
| `loanindexwiseloans_data` | Serializable model | `loanindexloanswiselist_act`, its adapter, `defaulttables` | LedgerId, LoanIndex, MCLoanId, CurLoanId (loan in today's collection list), MCLoanAmountCollectable, collected, Balance, todaycollection, IsNewLoan, IsClosed (computed, unused). |
| `closablesavings_data` | Serializable model | `closablesavings_list`, `closingsavingsaccount`, `closablesavings_adapter` | LedgerId, CWLedgerId, LedgerName, tag, SubAreaNo, SubAreaName, TargetLedgerId (ledger debited on payout), SavingsAmount (double). |
| `prevsavingclosure_data` | Model | `prevsavingclosure_list_act`, `prevsavingclosure_adapter` | SCId, TransId, TransDate, PartyLedgerId, LedgerName, SubAreaName, Amount, AddInterest. |
| `prevloanslist_data` | Model | `prevloanslist_act`, `prevloanslist_adapter` | MCLoanId, LoanNo, LoanDate, LoanAmount, Paid, Days, Status ("A-Closed" = archived loan → `archievedloandisplay`). |
| `dataforrearrangelist` | Immutable row model | `partyrearrange`, `collectionarearearrangeact`, `collectionarearearrangeAdapter` | `primarydisplayvalue`, `secondarydisplayvalue`, `color` (bar colour), `valuemember` (id stored as the row's tag, LedgerId here). Constructor only. |
| `daterange` | Model | `loanslist_act`, `newpartiesnewloanslist_act`, `prevloanclosure_list_act` (also `accounttrans_list`, `editing_collectionlist_act`, `inventorytransactionslist_act`, `ledgerstatement_act`) | `fromdate` and `todate` (java.sql.Date) with getters and setters. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_newloanslist.xml` | `loanslist_act`, `newloanslist_act`, `newpartieslist_act`, `newpartiesnewloanslist_act` | CoordinatorLayout `main`, Toolbar. Hidden `lytselectlineid` ("Line Name :"). Date bar `lytdaterangeid`: From `loanlist_fromdateid`, To `loanlist_todateid`, ImageButton `loanslistrefreshid` ("Refresh List"). RecyclerView `loansslist_recycler_view`. Hidden footers `lyttotallytid` (Total Loan / Interest / DC) and `lyttotalcollectionlytid` (Count: / Collection / Balance). FAB `newloan_fab` ("Add New"). |
| `newloanslistrow.xml` | `newloanslist_adapter` | Party name, area (goldenrod); "Loan:/கடன்:", "Ins:/தவணை:", "Int:/வட்டி:"; "Date:/தேதி:", "DC:/மகிமை :"; `newloanlist_ondaycollectionlayoutid` ("Onday Coll : / க.நாள் வசூல் :" + amount); collection icon `newloanlist_collectiondetailsid`. |
| `newpartieslistrow.xml` | `newpartieslist_adapter` | `newpartylist_partynameid`, `newpartylist_areanameid`. |
| `activity_closed_remevedloanslist.xml` | `closed_renewedloanslist_act` | Toolbar. Date bar (From `ds_fromdateid`, To `ds_todateid`, refresh `acctranslistrefreshid`). Six clickable tiles with label and value: closed / renewed / not renewed / extra / less / same amount, with the Tamil labels listed under the screen. Title `titletvid`. Includes `customersearchlist` (`recycler_view`). |
| `closed_renewedloansrow.xml` | `closed_renewedloanslist_adapter` | `crloanlistpartynameid`, `crloanlistareanameid` (placeholders "ACCESS DENIED" / "DENIED"). Hard-coded Tamil captions முந்தைய கடன் / கடைசி வசூல் / புதிய கடன். Values `prevloandateandamounttvid`, `prevloanlastcollectiondateandamounttvid`, `newloandateandamounttvid`. |
| `activity_customersearch.xml` | `prevloanslist_act`, `loanindexloanswiselist_act` | Toolbar, SwipeRefreshLayout `refreshLayout` → RecyclerView `recycler_view`, FAB `fab` (hidden in code), ProgressBar `cs_pbarid` (gone). |
| `prevloanlistrow.xml` | `prevloanslist_adapter` | `ploandateid`, `ploannoid`, `ploanamountid`, `ploanpaidid`, `ploandaysid`, `ploanstatusid`. |
| `loanindexloanwielistrow.xml` | `loanindexloanswiselist_adapter` | Captions "Loan Index", "Loan Amount", "Balance". Values `loanindex_id`, `prevloanamount_id`, `loanbal_id`. Button `showprevloansbutton` "Prev\nLoans". Labels `newloanlabel` "New Loan" and `todayclosedlabel` "Today Closed" (both gone by default). |
| `activity_loanclosoureentry.xml` | `loanclosoureentry` | ScrollView with cards: "PARTY DETAILS" (`partyandloandetailsid` clickable: name and area); "LOAN DETAILS" (Loan Date, Loan Amount / கடன் தொகை); amounts (disabled TextInputEditTexts: Collected / வசூலான தொகை, Extra Collection / கூடுதல் வசூல், Collection Loss / வசூல் குறைவு, Closed Date / முடிவு தேதி). Buttons NEW (gone), "Update / சேமி", "DELETE / நீக்கு". |
| `activity_employeesearch.xml` | `closableloanssearchlistact`, `closablesavings_list` | Toolbar, include `customersearchlist` (`recycler_view`), FAB `addnewemployeeid` (hidden in code), clickable `progress_Overlay` with ProgressBar `capbarid`. |
| `loanclosurelistrow.xml` | `loanclosureloanlist_adapter` | `loanclosurerow_partynameid` (Party Name / பார்ட்டி பெயர்), `loanclosurerow_tagid` (shows "area / ReportIdentification"), `loanclosurerow_loandate`, `loanclosurerow_loanamount`. |
| `activity_prevloanclosure.xml` | `prevloanclosure_list_act` | Toolbar. Date bar (From `ds_fromdateid`, To `ds_todateid`, refresh `acctranslistrefreshid`). Header "Party/SubArea", "L.Date/Extra Coll", "C.Date/Pending". RecyclerView `prevsavingsclosure_recycler_view` (id reused). |
| `prevloanclosure_row.xml` | `prevloanclosure_adapter` | No, Party Name, Loan Date, Closed Date, Area, Extra Coll, Pending; two hidden captions ("Loan Date", "days"). |
| `closablesavings_row.xml` | `closablesavings_adapter` | `loanclosurelistrow_partynameid` (Party Name / பார்ட்டி பெயர்), `loanclosurelistrow_tagid` (Business / தொழில்), `loanclosurelistrow_subareanameid`, `loanclosurelistrow_savingsamount`. |
| `activity_closingsavingsaccount.xml` | `closingsavingsaccount` | Party name `loanclose_partynameid`; Balance Available / இருப்புத் தொகை; Interest / வட்டி (EditText, decimal); Total / மொத்தம்; Date / நாள் (EditText `closingsavingsaccount_closingdateid`); button SAVE. |
| `activity_prevsavingclosure.xml` | `prevsavingclosure_list_act` | Toolbar, header Date / Party / Amount / Interest, RecyclerView `prevsavingsclosure_recycler_view`. |
| `prevsavingclosure_row.xml` | `prevsavingclosure_adapter` | `prevsavingclosure_transdateid`, `_partynameid`, `_amountid`, `_addinterestid`, `_subareanameid`. |
| `activity_partytransfer.xml` | `partytransfer` | Read-only boxes (style `readonlybox`): Current Collection Area / தற்போது இருக்கும் பகுதி (`partytransfer_sourcesubareaid`), Party / பார்ட்டி (`partytransfer_selectespartyid`), New Area / மாற்ற வேண்டிய பகுதி (`partytransfer_targetsubareaid`); button "Update / சேமி" (`btnsavepartytranseferid`). |
| `activity_partyrearrange.xml` | `partyrearrange` | A single `CustomListView` `listView1` (drag-and-drop list, light-grey background). Rows come from `collectionarearearangelist_row` (another module). |

---
[← brain.md](../../brain.md) · Module 8.8 of 20 · Previous: [8.7 Parties (borrowers), new loan creation and KYC documents](07-parties-borrowers-new-loan-creation-kyc.md) · Next: [8.9 Collection areas, maps, GPS staff tracking and attendance](09-collection-areas-maps-gps-staff-tracking.md)
