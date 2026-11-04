# 8.13 Accounts transactions, cash/bank book, expenses & incomes, hand loans and ledger statements

[← brain.md](../../brain.md) · Module 8.13 of 20 · Previous: [8.12 Thittam (collection plan) and weekly analysis reports](12-thittam-collection-plan-weekly-analysis-reports.md) · Next: [8.14 ST Loans and OF Loans (interest-bearing loan books)](14-st-loans-of-loans-interest-bearing.md)

This module is the app's small accounting front-end. It works directly on the selected branch's SQL Server books (`globalvars.selbranchconnString`): double-entry vouchers in `tblTransactionsMaster` / `tblTransactionsDetails`, and account ledgers in `tblLedgers`. One generic voucher screen (`accounttransactions_act`) posts payment, receipt, contra, journal, capital, hand-loan and salary-advance vouchers online. `cashreceiveandreturns` posts the cash an executive takes from the office, or hands back, as a contra voucher. Offline executives record their day's expenses and incomes in the local SQLite table `ExpensesEntries` instead; `uploadcollection` uploads them later. The reports are: the day cash/bank book (staff or combined) with its verification workflow (PDF → upload → e-mail → lock download/upload → SMS), the company cash/bank balance abstract, hand-loan receivables and payables, and per-ledger statements. The module also holds account-ledger maintenance, the app-wide searchable picker `spinnersearch_list`, and the loan due-schedule viewer.

## Accounts voucher entry (Payment / Receipt / Contra / Journal / Capital / Hand loan / Salary advance) — `accounttransactions_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity). Manifest: default theme with an ActionBar and no label. The title is set to the voucher type ("PAYMENT", "RECEIPT", "CONTRA", "JOURNAL", "CAPITAL RECEIPT", "CAPITAL RETURN", "HAND LOANS PAYMENT", "HAND LOANS RECEIPT", "SALARY ADVANCE PAYMENT", "SALARY ADVANCE RECEIPT"). |
| **Layout(s)** | `activity_quickacccounttransactions.xml`; menu `menu_expensesheaders.xml` |
| **Opened from** | **Drawer "Accounts Trans"** (`startActivity`, extras `ttid`, `actiontype`=501, empty `transdata`): nav_quickpayments 7, nav_quickreceipts 8, nav_quickcontraentries 9, nav_quickjournalentries 10, nav_capitalreceipts 11, nav_capitalreturns 12, nav_handloanspayment 24, nav_handloansreceipt 25, nav_quicksalaryadvacepaymententries 26, nav_quicksalaryadvancereceiptentries 27. **`handloans_list`** "Make Payment/Receipt Entry" (forResult 501; `ttid` 24/25, `actiontype` 501, `requestfrom` 7034 = `handloan_directentryfromhandloansreport`, `handloandata`). **`ledgerstatement_act`** row tap (forResult 502; `actiontype` 502, `transdata`, `ttid`, `finid` [never read]). |
| **Opens** | `spinnersearch_list` (rc 504 source ledger, 505 target ledger, 7005 finance); `accounttrans_list` (rc 1000, menu LIST; extras `ttid` and `caltype`="app", which is unused); `ImagePickerActivity` (rc 100) and `fullimageview` (`passedimage`), both reachable only from views that stay hidden. |
| **Data** | Local SQLite: `qatledgers` (picker source, rebuilt by "Refresh Headers"), `FinanceDetails` (finance list and names). Remote: `exec Get_LedgersforQuickTransactions`, `{call dbo.InsertAPPAccTransactionsMaster(11 params)}`, `Update tbltransactionsmaster …`, `delete/insert tbltransactionsdetails`, `{call dbo.InsertImageUploads(?,?)}`. Globals and settings: `isFullAccessAllowed`, `salesdetailsforandroid_datalist` (per-role menu rights loaded from local `tblSalesDetailsforAndroid`), special access 19, `curfin.FinId`, soft settings `LimitAccountTransactionstoCurrentLine_201` (id 201) and `currentfinancialyearid_20` (id 8), `globalvars.imgtobepassed`/`istransactionimagefromdb`. |

**Purpose.** Create, view, edit or delete one two-legged voucher of a given transaction type (TTID). The user picks the line (finance), the date, the account money comes out of (Source, credited) and the account it goes into (Target, debited), then enters the amount and notes.

**UI.** `qat_ttypeid` is hidden by code; the type shows in the title.
- Date `qatl_transdateid` ("Date"/"நாள்"): tap for a DatePicker. Enabled only with full access or the menu's UpdateState.
- Finance `qat_financeid` ("Finance"): picker over local `FinanceDetails.FinMailingName`.
- SNo `qat_CWVWUDTransIdDispid` ("SNo"/"எண்"): the server voucher number `FWVWUDTransId`.
- Source Account `qat_sourceaccountetid` ("Source Account"/"பணம் பெறப்படும் கணக்கு", the account money is taken from).
- Target Account `qat_targetaccountetid` ("Target Account"/"பணம் அளிக்கப்படும் கணக்கு", the account money is given to).
- Amount `qat_amountid` ("Amount"/"தொகை", numberDecimal) and Notes `qat_narrationid` ("Notes"/"குறிப்பு").
- Buttons Update/"சேமி", DELETE/"நீக்கு" and NEW.
- Hidden widgets: `groupphotoimageviewid` (voucher image), `shg_img_plus` (add-image icon), and progress `qat_progressBar` + `qat_txtPercentage` ("Updating Transaction").

**Menu / actions.**
- LIST (`translistid`, icon): only with the menu's ViewState; opens `accounttrans_list` (rc 1000).
- Overflow "Refresh Headers" (`action_settings`): runs `ASYNCRefreshaccountsledgers`.
- Up: back.
- Taps on Finance, Source, Target and Date open their pickers.
- Update saves, DELETE deletes after a confirmation, NEW starts a blank voucher.

**How it works.**
1. **onCreate.**
   - Sets the accounting period `globalvars.accyearfromdate/accyeartodate` from soft setting 20. The financial-year table is hard-coded in `defaulttables.fetchfinancialyears`: FYId 1 = 2011-04-01…2012-03-31, then +1 year per id.
   - Reads `ttid`, `actiontype` and `requestfrom`, and maps the TTID to a title and a menu name: 7 nav_quickpayments, 8 nav_quickreceipts, 9 nav_quickcontraentries, 10 nav_quickjournalentries, 11 nav_capitalreceipts, 12 nav_capitalreturns, 24 nav_handloanspayment, 25 nav_handloansreceipt, 26 nav_quicksalaryadvacepaymententries, 27 nav_quicksalaryadvancereceiptentries.
   - Rights: full access gets every right. Otherwise the user gets the `salesdetailsforandroid_datalist` row with that `mnuname`; with no row, every right is false.
   - Update is visible only with **UpdateState**, for new vouchers too. Delete needs DeleteState.
   - `actiontype` 501 runs `prepareactfornewtransaction()`; 502 runs `diplaytransaction()` on the passed `transdata`.
   - If flag 201 is on, the finance field is disabled and shows the current line's name.
2. **New voucher** (`prepareactfornewtransaction`).
   - Starts a fresh `acctransactionslit_data` dated today. FinId is the current line (field locked) if flag 201 is on, otherwise 0.
   - Hand-loan prefill (`requestfrom`==7034): FinId comes from `handloandata` and the finance field is locked.
   - TTID 25: Amount = Receivable; Source = the party ledger, locked, status "Source".
   - TTID 24: Amount = Payable; Target = the party ledger, locked. The status, however, is stored in `LedgerStatusOne`="Target" (see bugs).
   - Delete and New are hidden.
3. **Ledger pickers** call `defaulttables.fetchsourceledgersforaccountstransactions(db, AType, TTId, FinId, excludeLedgerId)`. It reads `qatledgers` with `AType` 1 = source and 2 = target, `TTId` = the voucher type, `(FinId=0 or FinId=<chosen>)`, excluding the ledger already picked, ordered by name. Which rows a user sees:

   | Who / which list | Rows shown |
   |---|---|
   | Full access, special access 19, Payment source (7/AType 1), Receipt target (8/AType 2), or any Contra (9) | All matching rows |
   | Payment target (7/AType 2) | Only `isExecutiveLevelExpensesLedger=1` |
   | Receipt source (8/AType 1) | Only `isExecutiveLevelExpensesLedger=1`. The `isExecutiveLevelReceiptsLedger` column is downloaded but never used. |
   | Any other TTID (10, 11, 12, 24–27) for a restricted user | The cursor stays `null`, so a NullPointerException crashes the app on the UI thread (confirmed in smali) |

   Which ledgers exist for each TTId/AType is defined by the server procedure.
4. **Picker results.**
   - Finance (7005) clears both ledgers and opens the source picker.
   - Source (504) sets `LedgerIdOne` with status "Source" and opens the target picker if the target is still empty.
   - Target (505) sets `LedgerIdTwo` with status "Target" and focuses Amount.
   - `Particulars` is kept in memory only, for lists: the source name for "inward" types (`fetchtranstype_inorout`=+1: 8, 9, 10, 11, 25, 27) or the target name for "outward" types (−1: 7, 12, 24, 26).
5. **Date check.** A date before the period start shows "Date Selection Error..." / "Date cannot be before current accounting peroid dd-MM-yyyy to dd-MM-yyyy". A date one or more days after the period end shows "...cannot be after...". A rejected date leaves the previous date in place.
6. **Save validations,** in order:
   - The same two date checks.
   - Empty amount: dialog "தங்கள் கவனத்திற்கு.... " (for your attention) / "தொகை பதிவிடப்படவில்லை..." (amount not entered).
   - FinId 0: "பைனான்ஸ் தேர்வு செய்யவில்லை..." (finance not selected).
   - No source: "பணம் பெறப்படும் கணக்கு தேர்வு செய்யவில்லை...".
   - No target: "பணம் அளிக்கப்படும் கணக்கு தேர்வு செய்யவில்லை...".

   Notes are copied only if non-empty. The amount is read with `Integer.parseInt`. In-memory Credit or Debit is set to the amount.
7. **`ASYNCTransactions`** (touches blocked with window flag `FLAG_NOT_TOUCHABLE`):
   - **Delete (503):** `delete from tbltransactionsdetails where transid=N; delete from tbltransactionsmaster where transid=N`, with no other checks.
   - **Image name:** if `!istransactionimagefromdb && imgtobepassed != null`, it calls `InsertImageUploads(IUId OUT, Dummy='Dummy')` and names the image `trans<IUId>.jpg`. No file is ever uploaded.
   - **New (501):** `InsertAPPAccTransactionsMaster(TransId OUT, FWVWTransId OUT, FWVWUDTransId OUT, TransDate, TransType=ttid, Narration, FinId, TransAmount, TransLedgerId=target, SourceLedgerId=source, ImageFileName=NULL)`. The procedure generates the voucher numbers; the names suggest finance-wise / voucher-type-wise serials, but the numbering logic is server-side and not visible. The client keeps `TransId` and `FWVWUDTransId`.
   - **Update (502):** `Update tbltransactionsmaster set TransDate=?,TransType=?,Narration=?,FinId=?,TransAmount=?,TransLedgerId=?,SourceLedgerId=?,ImageFileName=? Where transid=?`, then `delete from tbltransactionsdetails where transid=N`.
   - **Detail rows,** two positional inserts `insert into tbltransactionsdetails values(?,?,?,?,?,?,?)`. The column order, from named inserts elsewhere, is TransId, SNo, LedgerId, LedgerStatus, LedgerAmount, AmtType, AgnstLedgerSNo. The rows are `(TransId,1,source,'Source',amt,'Cr',2)` and `(TransId,2,target,'Target',amt,'Dr',1)`.
   - **Errors:** an "Error Info" dialog `<line>.<message>`; OK closes the screen.
   - **Success:** a new voucher switches to `actiontype` 502 and shows its SNo. A dialog "Info" / "Transaction Updated" appears (after a delete too). OK re-shows Save, New and Delete according to the user's rights.
8. **NEW** (`prepareactivityfornewtransaction`, the listener that wins): `actiontype` 501, today's date, cleared source/target/amount/notes/SNo, Save visible. FinId is reset to 0 and the hand-loan prefill texts are wiped.
9. **DELETE:** confirmation "Alert" / "Deleted Transactions cannot be Recovered" (OK/Cancel), then `actiontype` 503 and `ASYNCTransactions`.
10. **Refresh Headers** (`ASYNCRefreshaccountsledgers`): `exec Get_LedgersforQuickTransactions` rebuilds local `qatledgers(FinId, TTId, AType, LedgerId, LedgerName, isExecutiveLevelExpensesLedger, isExecutiveLevelReceiptsLedger)`. `refreshorreloadbrancheslist` also loads this table. On error: "Error Info".
11. **LIST result** (rc 1000, result 1): `actiontype` 502 shows the chosen voucher; 501 starts a blank one. The screen never calls `setResult()`, so the result handlers in `handloans_list`, `accounttrans_list` and `ledgerstatement_act` never fire. There is no SMS and no printing.

**Functions.**
- `onCreate()`: TTID-to-title/menu mapping, rights, button visibility, listeners, flag-201 lock.
- `lambda$onCreate$2/$4`: open the source / target picker (`fetchsourceledgersforaccountstransactions`, AType 1/2).
- `lambda$onCreate$3`: finance picker (`fetchfinancelist`, rc 7005).
- `lambda$onCreate$6/$5`: date picker and accounting-period validation.
- `lambda$onCreate$7`: open the voucher image in `fullimageview` (`groupphotospath + name`).
- `lambda$onCreate$8`: add an image (`isPartyImagefromDB=false`, `showImagePickerOptions`).
- `lambda$onCreate$9`: save validations, then `ASYNCTransactions`.
- `lambda$onCreate$12/$10`: delete confirmation.
- `lambda$onCreate$1/$13`: NEW button; the second registration, `$13`, overrides the first.
- `prepareactfornewtransaction()`: initial new voucher plus hand-loan prefill.
- `prepareactivityfornewtransaction()`: reset for the next voucher.
- `diplaytransaction()`: fill the form from a voucher. Missing statuses default to Source/Target; a "Target" first leg is handled by swapping the display.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: LIST and Refresh Headers.
- `showImagePickerOptions()`, `launchCameraIntent()` (camera, 16:9, max 1000×1000), `launchGalleryIntent()` (gallery, 16:9): all rc 100.
- `ASYNCRefreshaccountsledgers`: reload `qatledgers`.
- `ASYNCTransactions`: insert, update or delete plus the detail rows.
- `onActivityResult()`: handles rc 504, 505, 7005 and 1000. There is no handler for rc 100.
- `showincompletedataalert()`: warning dialog with an OK button.

**Rules & edge cases.**
- **Restricted users cannot post TTID 10/11/12/24–27.** Their source and target pickers crash with a NullPointerException.
- **Hand-loan payment from `handloans_list` fails.** The prefill sets `LedgerStatusOne`="Target", picking the source then overwrites it with "Source", and `LedgerStatusTwo` stays null. The insert path then calls `getLedgerStatusTwo().equals(...)`, which throws a NullPointerException; the user sees "Error Info" and the screen closes.
- **Decimal amounts crash.** The field accepts decimals but `Integer.parseInt` throws on "12.5", uncaught on the UI thread. An amount of 0 is accepted.
- **NEW after a save loses the finance.** It resets FinId to 0 while the finance field keeps its old text. With flag 201 the field is disabled, so the next save always fails with "finance not selected".
- **Flag 201 hides the voucher's own line on edit.** In 502 mode the finance text is overwritten with the current line's name, although the voucher keeps its own FinId.
- **Voucher images never work.** The image views are always `gone`, rc 100 is never handled and no file is uploaded. A stale `globalvars.imgtobepassed` left by party or document screens still makes a save call `InsertImageUploads`; on update it writes `ImageFileName`=`trans<IUId>.jpg`, pointing to a file that does not exist.
- **Other TTIDs opened from a ledger statement** (e.g. ST/OF loan 3–6 or 28) get a blank title and no matching menu, so all rights are false. Only full-access users see Save/Delete, which would rewrite the voucher generically.
- **The date-after check reads `tempselecteddate`.** A rejected later date keeps blocking Save until a valid date is picked.
- **After a delete the voucher stays on screen.** Pressing Save just runs the delete again.

## Accounts transaction list — `accounttrans_list`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` with its own Toolbar; title = voucher description for the TTID |
| **Layout(s)** | `activity_acctranslist.xml` (+ row `accountstranslist_row.xml`); menu `menuonlywithsearchoption.xml` |
| **Opened from** | `accounttransactions_act` menu LIST (forResult 1000; `ttid`, `caltype`); drawer nav_executivepayments (`ttid` 7) and nav_executivereceipts (`ttid` 8), both with `calledfrom`=1022 (`requestcodes.loanlistcalledfrom_executives`, unused) via plain `startActivity` |
| **Opens** | Nothing. It returns the selection to its caller. |
| **Data** | Remote: `exec APP_TransactionslistforSelectedLine @FromDate,@ToDate,@TTId,@FinId=<curfin>` when flag 201 is on, else `exec APP_TransactionslistNew @FromDate,@ToDate,@TTId`. Columns read: TransId, FinId, TransDate, TransType, Narration, Particulars, TransAmount, SourceLedgerId, TransLedgerId, FWVWUDTransId. Local `FinanceDetails` supplies the finance names in rows. Resets `globalvars.acctransactionslit_datalist` without using it. |

**Purpose.** Browse the vouchers of one type for a date range, then pick one to edit or start a new one.

**UI.** Date range "From : `ds_fromdateid`" / "To : `ds_todateid`" (tap for a DatePicker) and a refresh `ImageButton` `acctranslistrefreshid`. RecyclerView `acctranslist_recycler_view` rows show date, finance short name, particulars and amount. FAB `acctranslist_addaccounttransactionfabid` ("add new transaction"/"புதிய") is shown only with AddState. Progress bar `acctranslist_pbar`.

**Menu / actions.**
- Search filters rows by Particulars, case-insensitive.
- Row tap, only with ViewState: `setResult(1, {transdata, ttid, actiontype=502})` and finish.
- FAB: `setResult(1, {new transdata, ttid, actiontype=501})` and finish.
- Refresh reloads the list.
- Back first collapses the search view.

**How it works.**
1. **Default range.** With full access or special access 19, the range is today − 7 days to today and the date fields are editable. Otherwise the date fields are disabled and never filled (they keep the layout placeholders "06-06-2020"/"06-Jun-2020"), and the query uses today only.
2. **Rights** come from `salesdetailsforandroid_datalist`, using the same TTID-to-menu-name mapping as `accounttransactions_act`.
3. **`ASYNCGetAccountstransactionslist`** runs the procedure chosen by flag 201 and special access 19. It maps `SourceLedgerId` to `LedgerIdOne` and `TransLedgerId` to `LedgerIdTwo`, leaving the statuses null; `diplaytransaction` defaults them to Source/Target.

**Functions.**
- `onCreate()`: date range, title and menu name, rights, FAB, initial load.
- `lambda$onCreate$1`: FAB returns a new-voucher result.
- `lambda$onCreate$2`: refresh.
- `lambda$onCreate$4/$3` and `$6/$5`: from/to date pickers.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: search and Up.
- `onTransactionselected()`: return the chosen voucher.
- `ASYNCGetAccountstransactionslist`: load the vouchers.
- `onActivityResult()`: dead code. It handles rc 501/502 results with key `accountledger`, but this screen never starts an activity. Its 501 branch would also add the row twice.
- `whiteNotificationBar()`: white, light status bar.

**Rules & edge cases.**
- From the drawer (plain `startActivity`), tapping a row or the FAB just closes the list, because nothing receives the result. The drawer entries are effectively view-only.
- The date range is not validated.
- Errors are swallowed and produce an empty list.

## Executive cash received from / returned to office — `cashreceiveandreturns`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default ActionBar; title = extra `activityheader` |
| **Layout(s)** | `activity_cashreceiveandreturns.xml` |
| **Opened from** | **Receive money** (nav_receivemoney): `sourceledgerid`=`globalvars.FinanceCashBookLedgerId`, `targetledgerid`=`globalvars.cashbookledgerid`, `transtype`=R.string.cashreceived ("Amount Received"/"பெறப்பட்ட தொகை"), `activityheader`=amountreceived ("Amount Received"/"பணம் பெறுதல்"), `notes`=amountreceiptsnotes, `smstemplateid` 14 if `vss_IsthisDBunderBranchManagement` else 12. **Return money** (nav_returnmoney): source and target swapped, CashPaid ("Cash Paid"/"செலுத்தப்பட்ட தொகை"), AmountPaid ("Amount paid"/"பணம் செலுத்துதல்"), amountreturnrnotes, `smstemplateid` 15/13. |
| **Opens** | Nothing (device SMS via `SmsSender`) |
| **Data** | Remote: `{call dbo.InsertContratblTransactionsMaster(12 params)}` and `insert into tblTransactionsDetails Select …`. Local: `SMSTemplates` (`fetchsmstemplatetext`). Prefs `venusmicrofinpref011`: `currentcollectionexecutivelederid1/2` are loaded into globals but not used here. |

**Purpose.** At the start of the day the executive records the cash received from the manager or office. At the end of the day they record the cash handed back. The notes read "Enter the amount received from the manager by the person going for collection." / "...amount returned to the manager by the person after completing the collection." (Tamil variants exist).

**UI.** Notes text `notesid`, header `tvpaymentreceiptsheaderid`, amount `etreceptandpaymentamount` (numberSigned) and a SAVE button `paymentreceiptssavebuttonid`. While posting, the button is disabled and reads "Please Wait.....".

**How it works.**
1. An empty amount shows "Alert" / "Please enter amount" (Yes/No).
2. Otherwise `Integer.parseInt(amount)` and `ASYNCReceiptsandReturns`:
   - `InsertContratblTransactionsMaster(TransId OUT, FWVWTransId OUT, TransType=9 (contra), Narration='', FinId=globalvars.finid, TransAmount, FWTransId=NULL, FWVWUDTransId=NULL, GColId=0, TransLedgerId=target, SourceLedgerId=source, RouteId=globalvars.finid)`.
   - Then `insert into tblTransactionsDetails Select <TransId>,1 as SNo,<target>,'Target',<amt>,'Dr',2 union all Select <TransId>,2 as SNo,<source>,'Source',<amt>,'Cr',1`.
3. **SMS.** The template from `SMSTemplates.SMSDesc` has these placeholders replaced: `{BranchName}`→`vss_branchname`, `{Finance}`→`financename`+newline, `{Amount}`→"Rs. <amt>"+newline, `{executive1}`→the literal " Collection Executives". It is sent only when `smsmethod`==1, via `SmsSender.sendMultipartSMS(globalvars.AdminMobileNos)`: comma-separated numbers through `SmsManager`, or the SMS app opens pre-filled if SEND_SMS is denied.
4. **Always** a toast "Transaction added" and back, because all exceptions are swallowed.

**Functions.**
- `onCreate()`: read extras, set header, notes and title.
- `lambda$onCreate$2`: SAVE validation.
- `lambda$onCreate$1`: dialog Yes.
- `ASYNCReceiptsandReturns`: post the voucher and send the SMS.

**Rules & edge cases.**
- There is no date field: `TransDate` is not passed, so the procedure decides it (unclear from client code).
- Negative amounts are accepted (numberSigned), which reverses the entry.
- A bad value such as "-" crashes `parseInt`.
- Success is reported even when the insert fails.
- The screen does no rights check.

## Offline expenses / incomes list — `expensesentry_searchlist`
| | |
|---|---|
| **Type** | Activity, theme NoActionBar with Toolbar. Manifest label "Account Ledgers"; the title is R.string.todayexpenses "Today Expenses"/"இன்றைய செலவுகள்" (`ttid` 7) or todayincomes "Today Incomes"/"இன்றைய வரவுகள்" (`ttid` 8). |
| **Layout(s)** | `activity_employeesearch.xml` (+ row `expenseslist01_row.xml`); menu `menuonlywithsearchoption.xml` |
| **Opened from** | Drawer offline "Executive Entries": nav_expensesentrlistnew (`ttid` 7), nav_incomes (`ttid` 8) |
| **Opens** | `expensesandreceipt_offlineentry` (FAB: `actiontype` 1 + `ttid`, rc 1; row: `actiontype` 2 + `expensesentry` object + `ttid`, rc 2) |
| **Data** | Local SQLite only: `Select ID,FinId,LedgerId,Amount,ServerGenTransId,TTId,Narration,SourceLedgerId from ExpensesEntries where TTId=<ttid> order by ID`; the ledger name comes from `ExpHeaders` (`getexpensesheadername`) |

**Purpose.** Shows today's locally recorded expense or income entries for the offline executive.

**UI / actions.**
- Rows show ledger name and amount ("%.2f").
- The FAB `addnewemployeeid` is always visible (no rights check).
- Search filters by ledger name (case-sensitive contains).
- Back first collapses the search view.
- Any return from the entry screen reloads the list (`fetchContacts`).

**Functions.**
- `onCreate()`: read `ttid`, set title and recycler.
- `lambda$onCreate$1`: FAB opens a new entry.
- `fetchContacts()`: load from `ExpensesEntries`.
- `onActivityResult()`: reload.
- `onContactSelected()`: edit the tapped entry.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: search and Up.
- `whiteNotificationBar()`.

**Rules & edge cases.**
- Entries are not filtered by FinId, so every line's rows appear.
- If the table does not exist yet, the exception is swallowed and the list is empty.
- `uploadcollection` uploads these rows, stores `ServerGenTransId`, and prints them in its daily report. `cashbook` verification may wipe the table.

## Offline expense / receipt entry — `expensesandreceipt_offlineentry`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default ActionBar. Manifest label "Expenses Entry"; the code sets the title to "New Expenses Entry", "New Receipt Entry", "Edit Expenses Entry" or "Edit Receipt Entry". |
| **Layout(s)** | `activity_expensesentry.xml` |
| **Opened from** | `expensesentry_searchlist` (`actiontype` 1 new / 2 edit, `ttid` 7/8, `expensesentry` = `expenses_data`) |
| **Opens** | `spinnersearch_list`: rc 3, ledger list from local `ExpHeaders where TTId=<ttid>`, titled "Expense Ledger"/"செலவு கணக்கு" or "Income Ledger"/"வரவுக் கணக்கு"; rc 2024, "Choose Ledger" from local `CashBankLedgers` (Alloted=1, current FinId, plus a "Combained Cash Book" row with special access 17) |
| **Data** | Local `ExpensesEntries(ID INTEGER PK AUTOINCREMENT, FinId, LedgerId, Amount, ServerGenTransId, TTId, SourceLedgerId, Narration)`, `ExpHeaders`, `CashBankLedgers` |

**Purpose.** Record an expense paid (TTID 7) or an income received (TTID 8) by the executive while offline.

**UI.**
- Ledger label/value (`tvledgernamelabelid`/`tvledgernamedispid`, tap to pick): "Expenses Account"/"செலவு கணக்கு" in the layout. Code relabels it "Expenses Ledger" or "Received from".
- Amount (`etamountid`, number): "Exepenses Amount"/"செலவு தொகை", or "Received Amount".
- Notes `etnarratiobid` ("Notes"/"குறிப்பு").
- "Paid as / in" `etsourceledgerid`: tap to pick a cash/bank ledger. It defaults to the executive's cash ledger `globalvars.cashbookledgerid`.
- Update/"சேமி" and DELETE/"நீக்கு".

**How it works.**
1. **New:** Delete is hidden and `SourceLedgerId` = the executive's cash ledger.
2. **Edit:** the fields are filled from the object. Delete is hidden once `ServerGenTransId`>0, i.e. after upload.
3. **Save** (`saveexpenses`): `Amount=Double.parseDouble(...)`, `FinId=globalvars.finid`.
   - New: insert with `ServerGenTransId=0`; toast "New Expenses Added" / "New Receipt Added".
   - Edit: `update ExpensesEntries set Amount, Narration, LedgerId where ID=…`; toast "Expenses updated" / "Receipt updated".
   - Then back. An exception shows a toast with its message.
4. **Delete** (`deleteexpenses`): `Delete from ExpensesEntries where ID=…`, toast "Expenses Deleted" (also for receipts), back. There is no confirmation.

**Functions.**
- `onCreate()`: mode setup, labels and listeners.
- `lambda$onCreate$1`: save.
- `lambda$onCreate$2`: delete.
- `lambda$onCreate$3`: ledger picker.
- `lambda$onCreate$4`: paid-as picker.
- `saveexpenses()`, `deleteexpenses()`.
- `onActivityResult()`: rc 3 sets `LedgerId`; rc 2024 sets `SourceLedgerId`.

**Rules & edge cases.**
- No validation: the ledger can be left unset (`LedgerId` 0), and an empty amount only shows the exception toast.
- Editing does not save a changed "Paid as / in" (`SourceLedgerId` is missing from the UPDATE) or FinId.
- The combined pseudo-ledger (id 0) can be picked as "Paid as".
- Uploaded entries can still be edited locally.

## Day cash / bank book with verification — `cashbook`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default ActionBar; title "Detailed Cash Book" / "Abstract Cash Book" |
| **Layout(s)** | `activity_cashbook.xml` (+ row `cashbookverifyrow.xml`); menu `menu_cashbook.xml` |
| **Opened from** | Drawer Reports nav_cashbook. The drawer first runs `delete from CashBook` locally and passes `cashbooktype`="staffcashbook", which this class never reads. |
| **Opens** | `spinnersearch_list` (rc 2024 = `getcashandbankledgersforiew`, "Choose Ledger"); Android print dialog; no other screens |
| **Data** | Local `CashBook(SNo,Particulars,debit,credit)`, `CashBookAbstract(same)`, `CashBankLedgers`, `SMSTemplates`, `FinanceDetails.EmailIdentifier`. Wiped after verification: `LastUpload`, `NewParties`, `NewMCLoans`, `ExpensesEntries`, `MobileAppCollData`. Remote: `exec CurDayCashBook`, `exec CurDayCompainedCashBook`, `TempDailyCashBook`, `CashBookVerification`, `{call dbo.InsertCashBookVerification(?,?,?,?,?,?)}`, `exec checkisallcashandbankbooksverified`, `DownloadControling`, `UploadControling`. HTTP: `daily_report_file_upload_url` (setting 26), `email_sending_url` (27). Settings: `enablecashbookverification` (101), `noofdaysallowedinstaffcashbook_17` (53), `deletedataaftercashbookverification_37` (12), `reportingemailaddress` (15), `smsmethod` (50), `vss_IsthisDBunderBranchManagement`. Special access 6, 16, 17. |

**Purpose.** Shows the day's cash book of one cash or bank ledger of the current line (the staff cash book), or the combined cash book of all its cash and bank ledgers. For ledgers flagged for verification, the manager can **verify** the day's book. Verifying produces a PDF that is uploaded and e-mailed, locks the day against further download and upload, optionally wipes the device's offline data, and SMSes the admins. The verification can be cleared again.

**UI.**
- "Date"/"தேதி" `reportdatetvid`: tap for a DatePicker.
- "Ledger" `bankledgers_id` (read-only style `readonlybox`): tap for the picker.
- SHOW `btnPrepareandPrintcashbook`: disabled until a ledger is picked.
- RecyclerView `recycler_view`: particulars, debit and credit. "முன் இருப்பு" (opening balance) shows dark green, "நிகர இருப்பு" (net balance) `nipcolor`, "ஆக மொத்தம்" (grand total) red.
- E-mail status `cashbook_emailstatusid`.
- Verify button `btncashbookverfiedid`: "VERIFY CASH BOOK" in white, or "CASH BOOK VERIFIED" in red.
- CLEAR VERIFICATION `btncashbookclearverificationnid`.
- Progress `cashbook_pbar` + `cashbook_progresstexttvid`.

**Menu / actions.** Overflow menu:
- "Detailed": shows local `Cashbook`.
- "Abstract": shows local `CashbookAbstract`.
- "Print Cash Book": `printDocument()`, an Android print job "<app_name> Cashbook Document", A4 portrait.

**How it works.**
1. **onCreate.**
   - Today is the default date and the screen shows whatever is in local `Cashbook` (empty after the drawer's delete). `onResume` re-reads it too.
   - Date picker: minimum today − `noofdaysallowedinstaffcashbook_17` days, maximum today, unless the user has full access or special access 6 ("unconditional download").
   - The verify button is enabled only if `enablecashbookverification`.
2. **Ledger picker.** `fetchtargetledgersforcashandbanktobeverified`: `select LedgerId, Ledgername, istobeVerified, GroupId from CashBankLedgers where Alloted=1 and Finid=<finid>`. With special access 17 it adds a pseudo-row "Combained Cash Book" (value 0). Picking enables SHOW, sets `istobeverified = istobeVerified==1` and loads immediately.
3. **`AsyncCallCashBook`** (also run by SHOW):
   - Clears local `CashBook`.
   - A ledger id > 0 runs `exec CurDayCashBook @IMEI='<device_id>', @CashLedgerId=<id>, @FinId=<finid>, @CurDate='yyyy-MM-dd'`. Id 0 runs `exec CurDayCompainedCashBook @IMEI, @FinId, @CurDate`. Rows (SNo, Particulars, Debit, Credit) go into local `CashBook`.
   - Then on the server: `update TempDailyCashBook set Particulars=N'<New Loans/புதிய கடன்கள்>' where TTId=21 and LedSNo=2`, followed by updates that turn zero Debit/Credit into NULL.
   - Abstract: `select Row_Number() over (order by max(rowid)) as SNo, Particulars, SUM(Debit), SUM(Credit) from TempDailyCashBook where Particulars is not null group by Particulars order by MAX(rowId)` goes into local `CashBookAbstract`. All new loans collapse into one line.
   - If the ledger is to be verified: `select convert(bit,count(*)) as isverifybuttondisabled from CashBookVerification where FinId=… and Ledgerid=… and VerfiedDate='…'`.
   - After the load the verify button is hidden unless `enablecashbookverification` and `istobeverified`. An already verified day shows red "CASH BOOK VERIFIED", disabled, plus CLEAR VERIFICATION if the user has full access or special access 16. Otherwise "VERIFY CASH BOOK". On error: "Error Info", and OK closes the screen.
4. **Verify.**
   - With no rows the toast is "No cash book entries found for verification". Otherwise a dialog "Cash Book Verification" / "This action will finalize the Cash Book.\nDo you want to continue?" (CONFIRM/BACK).
   - **`createPdf()`:** A4 595×842, 38 rows per page. Each page has "Page n", the finance name, "Cash Book - dd-MM-yyyy" and a grid S.No/Particulars/Debit/Credit.
   - The file is `<dbidentificationno>CV_<ledger>_<yyyy-MM-dd>-<emailidentifier>_<yyyyMMdd_HHmmss>_<device_id>.pdf` in the app's external Documents folder. Toast "PDF Created Successfully" (or "Something went wrong: …").
   - **`ASYNCUploadDailyReportFile`:** OkHttp multipart POST to `daily_report_file_upload_url` with fields `image`=file, `website`="www.venussolutions.net" and `email`=a hard-coded vendor e-mail address. Progress shows as "Report file Uploading..N%", and the local PDF is deleted afterwards.
   - **`ASYNCsendmailtoadmins`:** form POST to `email_sending_url` with `receipantadrees`=`reportingemailaddress`, `emailbody`="REFER ATTACHMENT", `filenametobesent`=the file name, and `emailsubject`="<finance EmailIdentifier>-<date>-<ledger>- Sent by:<APPUserName>". The response's first line, or "EMail Failed", is shown with the file name.
   - **`asynccashbookverificationand_blockdownloauploadaccess`**, which runs **whatever the upload or e-mail outcome**:
     - `update DownloadControling set isAllowed='False'` and the same on `UploadControling`, `where FinId=… and TransDate='…'`.
     - `InsertCashBookVerification(CBVId OUT, VerifiedDateTime OUT, FinId, VerfiedDate, verifiedby=APPUserName, ledgerid)`.
     - If `deletedataaftercashbookverification_37`: `exec checkisallcashandbankbooksverified @FinId, @VerfiedDate` returns `UnverifiedCount`. If 0, local `LastUpload`, `NewParties`, `NewMCLoans`, `ExpensesEntries` and `MobileAppCollData` are deleted.
     - The button turns red "Cash Book Verified", disabled.
   - **`ASYNCSendSMS`:** template 7 (branch-managed DB) or 3. Placeholders: `{BranchName}`, `{LineName}`→`smsidentifier`+"\nDt: dd-MM-yyyy", `{executive}`→`APPUserName`, `{BookName}`→the ledger name; "\n@  dd-MM-yy hh:mm aa" is appended. Sent to `AdminMobileNos` only if `smsmethod`==1. The toast "SMS Sent" shows regardless.
5. **CLEAR VERIFICATION** (`asyncCANCELcashbookverification`): `delete from CASHBookVerification where FinId=… and VerfiedDate=…`, which removes **all** ledgers of that day, and sets both control tables back to `isAllowed='True'`. Info "All Verification on this day are Cleared", and OK closes the screen.

**Functions.**
- `onCreate()`, `onResume()`, `initUI()`, `setupRecyclerView()`, `whiteNotificationBar()`.
- `setupListeners()` and its lambdas: date picker with limits, clear, SHOW, verify confirmation, ledger picker.
- `onActivityResult()`: any non-null result is read as the `spinnerserchobjectname` ledger.
- `DisplayDetailedCashBook()` / `DisplayAbstractCashBook()`.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`.
- `createPdf()`, `drawPage()` (header plus up to 38 rows, Particulars ellipsized to 242 pt), `drawVerticals()`, `drawCenter()`, `getfilepath()`, `printDocument()`.
- `MyPrintDocumentAdapter`: `onStart()`, `onLayout()` (page count = ceil(rows/38)), `onWrite()` (standard loop then write; the odd JADX shape is a decompile artifact, checked in smali), `pageInRange()`.
- AsyncTasks: `AsyncCallCashBook`, `asynccashbookverificationand_blockdownloauploadaccess`, `asyncCANCELcashbookverification`, `ASYNCSendSMS`, `ASYNCUploadDailyReportFile`, `ASYNCsendmailtoadmins`.

**Rules & edge cases.**
- The combined book (id 0) can never be verified.
- The PDF and the print use the list currently on screen, so verifying while the Abstract view is shown uploads the abstract.
- `createPdf` does not reset `curprintingrowcount`, so after a "Print Cash Book" in the same session the verification PDF has empty tables.
- Follow-up statements on `TempDailyCashBook` are not filtered by IMEI (concurrency risk, per the client SQL).
- `cashbookrowslist` is cleared on the background thread.
- The public `Documents/venusmfc` folder is created but never used.

## Cash / bank balance abstract — `cashbookabstract`
| | |
|---|---|
| **Type** | Activity, NoActionBar with Toolbar; title "Cash Book Abstract" |
| **Layout(s)** | `activity_cashbookabstract.xml` (+ row `cashbookabstract_row.xml`); menu `menuonlywithsearchoption.xml` |
| **Opened from** | Drawer Reports nav_cashbookabstract |
| **Opens** | Nothing |
| **Data** | Remote `exec PrepareDashBoardCash`, no parameters (company-wide). Columns: GroupId, Grouping, LedgerId, FinId, Ledger, Balance. |

**Purpose.** A read-only list of cash and bank ledger balances, with group or total lines as the procedure returns them.

**UI / actions.**
- RecyclerView `cashbookabstract_recycler_view`: ledger name and balance ("##,##,##,##0").
- `Grouping` 7 rows show blue, 15 dark magenta, others dark grey.
- Search filters by ledger name. Tapping a row does nothing.
- Progress bar `cashbookabstract_pbar`.

**How it works.** `ASYNCGetCashAbstract` connects, executes the procedure and fills `cashabatract_datalist`. Any error shows "Error Info..." and OK closes the screen.

**Functions.**
- `onCreate()`, `onResume()` (does nothing), `whiteNotificationBar()`.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: search and Up.
- `onContactSelected()`: no-op.
- `ASYNCGetCashAbstract`.

**Rules & edge cases.**
- No filter by FinId or date on the client.
- If the connection fails, a NullPointerException message replaces the real error.

## Hand loans (kaimaathu) report — `handloans_list`
| | |
|---|---|
| **Type** | Activity, NoActionBar with Toolbar; title "Hand loans" |
| **Layout(s)** | `activity_handloanslist.xml` (+ row `handloans_row.xml`); menu `menuwithsearchandprintoptions.xml` |
| **Opened from** | Drawer Reports nav_handloansreport |
| **Opens** | `accounttransactions_act` (rc 501, extras as above); `ledgerstatement_act` (extras `selectedledgername`, `fromdate`/`todate` = the accounting year, `requestfrom`=7035 [unused there], `reportfinid`=`finid`, `ledgeridpassed`=-1, `list`); PDF viewer (`ACTION_VIEW`); share chooser "Share Image" (`ACTION_SEND`); print dialog |
| **Data** | Remote `exec GetHandLoansDetails` (FinId, LedgerId, LedgerName, Receivable, Payable) and `exec APP_Transactionslistforhandloanshistory @LedId=<ledger>`. Local `FinanceDetails` supplies the finance short name. File `<external storage>/mlfinsoftversion/Handloans.pdf`. |

**Purpose.** Lists every hand-loan party with the amount receivable from them or payable to them, across all lines. From a row the user can post the settling voucher or view the party's transaction history.

**UI.**
- Column header "Party | FIN | Receivable | Payable".
- Rows show party name, line short name, receivable and payable ("##,##,##,##0", blank when 0).
- Progress bar `acctranslist_pbar`.

**Menu / actions.**
- Search filters by party name.
- "Direct Print": Android print job "<app_name> Document".
- "Show as PDF": writes the PDF and opens it.
- "Share": writes the PDF and sends it.
- Row tap opens an "Options" dialog:
  - "Make Payment Entry" (if Payable≠0): `accounttransactions_act` with TTID 24, the Payable amount and the party as Target.
  - "Make Receipt Entry": TTID 25, the Receivable amount and the party as Source.
  - "Details": loads the history.

**How it works.**
1. onCreate resets the StrictMode VM policy so that `file://` URIs are allowed, sets the accounting year, and runs `ASYNC_GetHandloandetails`. That task loads the rows and sets the print header "HAND LOANS REPORT", the page count ceil(n/23) and the load date and time.
2. "Details" runs `ASYNCGetLedgerStatement`: it fills `globalvars.acctransactionslit_datalist` (RowId, TransId, FWVWUDTransId, LedgerIdOne/Two, statuses, ImageFileName, Narration, TransDate, TTId, Particulars, TransAmount, Debit, Credit, FinId), then opens the ledger statement with `ledgeridpassed`=-1 so that it shows this list.
3. **PDF and print** (`drawPage`, checked against smali):
   - A4 595×842, 23 rows per page.
   - "Page n" at top right and the report header "Handloans" at top left. A menu action resets the header to this text.
   - Grid columns Party, Receivables and Payables. Amounts are drawn only when > 0, using "##,##,##,##,###".
   - When a page fills, the footer "Printed @ dd-MM-yyyy, HH:mm:ss" (the load time) is drawn.

**Functions.**
- `onCreate()`, `handleOnBackPressed()` (collapses search first), `onCreateOptionsMenu()`, `whiteNotificationBar()`.
- `onContactSelected()` and `lambda$onContactSelected$1`: the options dialog.
- `ASYNC_GetHandloandetails`.
- `onOptionsItemSelected()`: print, PDF, share.
- `printDocument()`, `createPdf()`, `sharePdf()`.
- `MyPrintDocumentAdapter`: `onLayout()`, `onWrite()`, `pageInRange()`.
- `drawPage()`, `drawCenterwithgivenwidth()`.
- `drawCenter()` and `drawCenterwithbox()`: never called.
- `showprintandshareoptions()`: dead; its dialog has an empty switch.
- `onActivityResult()`: dead. It expects result 501 with key `accountledger` to reduce or remove the row, but `accounttransactions_act` never sets a result.
- `ASYNCGetLedgerStatement`: history load. Its `onPreExecute` hides the progress bar, so no spinner shows.

**Rules & edge cases.**
- The list is not refreshed after an entry.
- "Make Payment Entry" fails on save (the NullPointerException described under `accounttransactions_act`). For restricted users, TTID 24/25 ledger pickers crash.
- The PDF goes to the external-storage root without any storage permission (targetSdk 37), so writing probably fails on Android 11+ and the viewer or share then gets a missing file.
- `setFlags(0x40000000)` (FLAG_ACTIVITY_NO_HISTORY) overwrites the earlier read-grant flag on the view intent.

## Ledger and period selection — `ledgerstatmentsdateselection`
| | |
|---|---|
| **Type** | Activity, NoActionBar with Toolbar, `windowSoftInputMode=stateAlwaysHidden`; title "Ledger and Period Selection" |
| **Layout(s)** | `activity_ledgerstatmentsdateselection.xml` (+ row `ledgersstatementlist_row.xml`) |
| **Opened from** | Drawer Reports nav_ledgerstatements |
| **Opens** | `ledgerstatement_act` (`selectedledgername`, `fromdate`, `todate`, `ledgeridpassed`, `reportfinid`=`globalvars.finid`) via `startActivity` |
| **Data** | Remote `select LedgerId,LedgerName From tblledgers where (GroupId not in (1) or LedgerId=114) and LedgerId in (select LedgerId from tblTransactionsMaster x, tbltransactionsDetails y where x.TransId=y.TransId and FinId=<finid>) order by LedgerName` |

**Purpose.** The user picks a period ("From Date"/"ஆரம்ப தேதி", "To Date"/"நிறைவு தேதி"; both default to today) and then an account ledger ("Accounts Ledgers"/"வரவு செலவு கணக்குகள்") with transactions in the current line. Group 1 ledgers are excluded, except ledger 114.

**How it works.** `ASYNCGetLedgerslist` loads the list once, showing the ProgressDialog "Loading Ledgers List". The date pickers only change the dates. Tapping a ledger opens its statement.

**Functions.**
- `onCreate()`.
- `lambda$onCreate$2/$1`: from-date picker.
- `lambda$onCreate$4/$3`: to-date picker.
- `onOptionsItemSelected()`: Up.
- `onContactSelected()`: open the statement.
- `ASYNCGetLedgerslist`.
- `whiteNotificationBar()`.

**Rules & edge cases.**
- No check that From ≤ To.
- No search menu, although the adapter is filterable.
- Errors are swallowed and the list stays empty.

## Ledger statement — `ledgerstatement_act`
| | |
|---|---|
| **Type** | Activity, NoActionBar with Toolbar; title "<ledger>(dd-MM-yy to dd-MM-yy)" |
| **Layout(s)** | `activity_newloanslist.xml` (shared; + row `ledgerstatment_row.xml`) |
| **Opened from** | `ledgerstatmentsdateselection` (normal case). `handloans_list` "Details" and `simplethittamlist_act` (`ledgeridpassed`=-1 + a pre-loaded `list`; or a ledger id). `tbbspl_list`, `tbbspl_particulars` and `monthlypandl_fragment` (forResult 100), but these pass `ledgername`/`receivedledgerid`, keys this screen never reads. |
| **Opens** | `accounttransactions_act` (rc 502, `actiontype` 502, `transdata`, `ttid`, `finid`) |
| **Data** | Remote `exec APP_Transactionslist @FinId=<reportfinid>,@LedId=<ledger>,@FromDate='…',@ToDate='…'`. Columns: RowId, TransId, LedgerIdOne, LedgerStatusOne, TransDate, Narration, TTId, LedgerIdTwo, Particulars, ImageFileName, LedgerStatusTwo, TransAmount, Debit, Credit. |

**Purpose.** A debit/credit statement of one ledger for a period, with totals and the closing balance as computed by the server. The user can drill into a voucher to edit it.

**UI.**
- Date range `loanlist_fromdateid`/`loanlist_todateid` (tap for pickers) and refresh `loanslistrefreshid`.
- RecyclerView `loansslist_recycler_view`: date, particulars (the narration if present), debit and credit.
- The FAB `newloan_fab`, the line selector and the totals bars are hidden.

**How it works.**
1. `reportfinid` defaults to `globalvars.finid`, then the extra overrides it.
2. If `ledgeridpassed`==-1 the passed `list` is shown as is. Otherwise `ASYNCGetLedgerStatement` runs, showing the ProgressDialog "Working". Refresh runs it again with the current dates.
3. Rows with TransId ≤ 0 are summary rows supplied by the server. On return from the edit screen, and on back, the client recomputes them: row −1 gets the sums of Debit and Credit over rows with TransId > 0, and row −2 gets the difference on the larger side (`resultamountCr` or `resultamountDr`). No per-row running balance is computed.
4. Row tap: TTIDs 21/22/23 (MC loan, collection, closure) and 0 are not editable. Any other row opens `accounttransactions_act` in edit mode.
5. Back returns `setResult(1)` with `resultamountCr`, `resultamountDr`, `receivedledgerid`(=`ledgeridpassed`), `related_ledgerid`, `related_ledgeramount` and `related_ledgerstatus` (default "Dr"), then finishes.

**Functions.**
- `onCreate()`.
- `handleOnBackPressed()`: totals plus the result.
- `lambda$onCreate$1`: refresh.
- `lambda$onCreate$3/$2` and `$5/$4`: date pickers.
- `onOptionsItemSelected()`: Up.
- `onActivityResult()`: updates or removes rows for results 502/503 with key `accountledger` (never delivered), then recomputes the totals.
- `editbuttonclicked()`.
- `ASYNCGetLedgerStatement`.
- `whiteNotificationBar()`.

**Rules & edge cases.**
- **Drill-down from trial balance, balance sheet and P&L is broken.** Those callers pass `ledgername`/`receivedledgerid` instead of `selectedledgername`/`ledgeridpassed`. The title becomes "null(…)" and the query runs with `@LedId=0`; the list they pre-loaded into `globalvars.acctransactionslit_datalist` is ignored.
- With `ledgeridpassed`=-1, Refresh queries `@LedId=-1` and replaces the passed list.
- After an edit the rows are not reloaded unless the user taps Refresh.
- `resultamountCr` and `resultamountDr` are never reset, so both can be non-zero.

## Account ledgers list — `accledgers_searchlist`
| | |
|---|---|
| **Type** | Activity, NoActionBar with Toolbar; title "Accounts Ledgers" (manifest label "Account Ledgers") |
| **Layout(s)** | `activity_employeesearch.xml` (shared; + row `accledgers_row.xml`); menu `menuonlywithsearchoption.xml` |
| **Opened from** | Drawer Admin nav_accledgers |
| **Opens** | `accountledgerdetails` (FAB: `actiontype` 1, rc 1, only with AddState; row tap: `actiontype` 2 + `accledgerselected`, rc 2, only with ViewState) |
| **Data** | Remote `select a.LedgerId,LedgerName,a.GroupId,ScreenType,FinId,IsDefault,isReserved, case when c.LedgerId is null then 0 else 1 end as isPattiExpenses from tblLedgers a left join PattiExpLedgers c on a.LedgerId=c.LedgerId where a.LedgerId>200 and Screentype=5 order by LedgerName`. Local `tblGroups` supplies group names. Rights come from menu `nav_accledgers`. |

**Purpose.** Lists the user-created account ledgers: ScreenType 5, ids above 200, so built-in ledgers are hidden. From here the user adds, edits or deletes them.

**UI / actions.**
- Rows show ledger name and group name. Search filters by name.
- While loading, the overlay `progress_Overlay` shows and Back is blocked.
- Load errors show as a long toast with the message.
- Results from the detail screen:
  - 1/1 appends the new ledger.
  - 2/2 replaces the edited row.
  - 2/3 (delete): with `nooftransactionsfound`>0, removes the row and toasts "Deleted"; with 0, toasts "Transactions found cannot delete".

**Functions.**
- `onCreate()`, `handleOnBackPressed()`.
- `lambda$onCreate$1`: FAB.
- `onActivityResult()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`.
- `onContactSelected()`.
- `showLoading()` / `hideLoading()`.
- `ASYNCAccountsLedgers`.
- `whiteNotificationBar()`.

**Rules & edge cases.** "Treat as Executive level Ledger" is read only from `PattiExpLedgers`, so income ledgers flagged in `PattiIncomeLedgers` always show unchecked.

## Account ledger create / edit / delete — `accountledgerdetails`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), default ActionBar; title R.string.accounts_ledger_create_edit "Accounts Ledger Create/Edit" |
| **Layout(s)** | `activity_newacccountsledgers.xml` |
| **Opened from** | `accledgers_searchlist` (`actiontype` 1 new / 2 edit + `accledgerselected`) |
| **Opens** | `spinnersearch_list`: rc 9009 "Ledger Group" (local `tblGroups`), rc 9012 "Finance Name" (local `FinanceDetails`) |
| **Data** | Remote `{call dbo.InsertAccLedgersAPP(?,?,?,?,?,?,?)}`, `update tblLedgers set LedgerName=?,GroupId=?,Finid=? where ledgerid=?`, `delete from tblLedgers where LedgerId=?`, `PattiExpLedgers`, `PattiIncomeLedgers`. Local `ExpHeaders`. |

**Purpose.** Maintain one account ledger: its name, its accounts group, the owning finance or line (bank accounts only), and the "Treat as Executive level Ledger" flag. The flag puts an expense or income ledger into the executives' expense/income heads (`PattiExpLedgers`/`PattiIncomeLedgers` on the server, `ExpHeaders` on the device). **This screen has no opening-balance, opening-balance-type or bank-detail fields.**

**UI.**
- "Ledger Name" `newaccled_ledgernameid`.
- "Accounts Group" `newaccled_undergroupnameid` (tap to pick).
- "Ledger of the Finance" `newaccled_financeledgerid`, shown only for group 5 (bank accounts).
- Checkbox `newaccountledgers_chktreatasdefaultexpebses`: shown for groups 18 (indirect expenses) and 19 (indirect incomes) in edit mode. In new mode it is always visible.
- Update/"சேமி" and DELETE/"நீக்கு": in edit mode they need UpdateState / DeleteState. New mode always shows Update and hides Delete.
- Progress `newaccled_pbarid`.

**How it works.**
1. **Update validation.** Group 5 without a finance shows "Attention"/"தங்கள் கவனத்திற்கு" with msg_bank_finance_not_selected ("Please specify to which finance or line this bank account belongs."). No group shows msg_account_group_not_selected ("Accounts group is not selected.").
2. **`ASYNCupdateemployee`, case 1 (new):**
   - `InsertAccLedgersAPP(LedgerId OUT, LedgerName, GroupId, ScreenType=5, IsDefault=0, isReserved=0, FinId)`. FinId is sent only for group 5, otherwise NULL.
   - If the flag is checked: group 18 runs `insert into PattiExpLedgers values(LedgerId,1)` plus local `ExpHeaders(LedgerId, LedgerName, TTId=7)`. Group 19 inserts into `PattiIncomeLedgers` plus `ExpHeaders` with TTId=8.
3. **Case 2 (edit):**
   - The `tblLedgers` update (Finid only for group 5, otherwise NULL).
   - `delete from PattiExpLedgers where LedgerId=…`.
   - If the flag is checked: insert into `PattiExpLedgers` (for income ledgers too) and update the existing local `ExpHeaders` row (TTId 7 for groups 18/15, 8 for 19/16).
4. **Case 3 (delete), with no confirmation:**
   - `delete from tblLedgers where LedgerId=?` and keep the row count.
   - If rows > 0, delete the local `ExpHeaders` row. The decompiled code then runs `delete from PattiExpLedgers…` on a **null `Statement`** (confirmed in smali): the NullPointerException is swallowed and the server row is orphaned.
5. **Result** (always, even after an error): `setResult(1|2|3)` with `passedledger`; delete also returns `deletededledgerid` and `nooftransactionsfound` (= rows deleted). Any SQL error on delete, e.g. a server constraint when transactions exist, therefore shows as "Transactions found cannot delete".

**Functions.**
- `onCreate()`: mode setup and rights.
- `lambda$onCreate$3`: validate and save.
- `lambda$onCreate$1/$2`: dialog OK focuses the finance field.
- `lambda$onCreate$4`: group picker.
- `lambda$onCreate$5`: delete.
- `lambda$onCreate$6`: finance picker.
- `onActivityResult()`: rc 9009 sets the group and shows the finance row or the checkbox; rc 9012 sets FinId.
- `ASYNCupdateemployee` (insert/update/delete; misnamed).

**Rules & edge cases.**
- An empty ledger name is not rejected.
- A failed insert still returns the ledger, with LedgerId 0, to the list.
- Unchecking the flag removes the server row but leaves the local `ExpHeaders` row.
- `PattiIncomeLedgers` is never cleared on edit.
- Changing the group away from 5 does not hide the finance row.

## Generic searchable picker — `spinnersearch_list`
| | |
|---|---|
| **Type** | Activity, NoActionBar with Toolbar. Manifest label "Account Ledgers", replaced by the `title` extra. |
| **Layout(s)** | `activity_spinnersearchlist.xml` (+ row `spinnersearch_row.xml`); menu `menuonlywithsearchoption.xml` |
| **Opened from** | 32 classes via `startActivityForResult`. **In this module:** `accounttransactions_act` (504/505/7005), `cashbook` (2024), `accountledgerdetails` (9009/9012), `expensesandreceipt_offlineentry` (3/2024). **Elsewhere:** accountsreports_parameters, branchmobiledeviceaccescontrol_act, collectionareadetails_act, deviceidentity, directcollectionentry, downloaddata, employeedetails_act, financelinesaddandedit, financewisepartners_list, invtrans_rowdetails, invtransactnew, itemmaster_activity, loanclosoureentry, loanmethods, mobiledeviceaccescontrol_act, newloancreation, ofloan_interestcalculationdetails, ofloans, partyandpartnerdetails, partydetails_act, partytransfer, selectbranchandfinance, settings_act, specialaccesscontrolact, stloan_interestcalculationdetails, stloans, tabbedindividualcollectionentry, userroleprevilages_act. |
| **Opens** | Nothing |
| **Data** | None. It works purely on the intent data. |

**Purpose.** A reusable "spinner with search": it shows a list of `spinnersearch_data` items and returns the tapped one.

**Contract.**
- **Extras in:**
  - `title` (String, toolbar title).
  - `searchlist` (Serializable `ArrayList<spinnersearch_data>`; each item has `displaymember`, `valuemember` int, `secondvaluemember_inttype`, `thirdvaluemember`, `fourthvaluemember`, `fithvaluemember`, `seconddisplaymember`, `thirddisplaymember`).
  - Optional `fromdate`/`todate` (`java.sql.Date`).
  - Optional `calledfrom` (int). Only when it equals 11002 (`gettargetledgersforloancollection`) is `selloandata` (`tabbedcollectionlistdata`) also read.
- **Result on tap:** `setResult(1, …)` with `spinnerserchobjectname` (the whole `spinnersearch_data`), `displaymember` (String), `valuemember` (int), `fromdate`/`todate` echoed (null if they were not passed), and `selloandata` echoed when `calledfrom`==11002.
- **Back:** RESULT_CANCELED (0) with no data. Back first collapses an open search view.

**UI.** RecyclerView rows show only `displaymember`; `valuemember` sits in a hidden TextView. Search filters by `displaymember`, case-insensitive contains.

**Functions.**
- `onCreate()`: read extras and set up the list.
- `handleOnBackPressed()`.
- `onCreateOptionsMenu()` / `onOptionsItemSelected()`: search and Up.
- `onContactSelected()`: build the result.
- `whiteNotificationBar()`.

**Rules & edge cases.** Back before the menu exists dereferences the null `searchView`.

## Loan due schedule viewer — `dueschdule`
| | |
|---|---|
| **Type** | Activity, NoActionBar theme without a Toolbar, so no title bar. Manifest label "Account Ledgers" (a copy-paste). |
| **Layout(s)** | `activity_dueschdule.xml` (+ row `duelist_row.xml`) |
| **Opened from** | `newloancreation` "Show Schedule" button `showschbtnid`, visible only when `globalvars.curfin.isEnableinventory()`, with extra `dueslist_dataList` |
| **Opens** | Nothing |
| **Data** | Only the intent list. It is built by `defaulttables.prepareandreturnduelist(loanDate, Balanceamount, TotalMonths, Intrate, 5, loanDate, loanMethod.CollectionBreak, manual=true, InsAmount, PDue=InsAmount, IDue=0)`. |

**Purpose.** Shows the planned dues of the loan being created: "Due No", "Due Date" and "Due Amount".

**How it works.** The schedule is computed in `defaulttables`. With the manual flag, principal per due = the installment amount and interest = 0. Schedule type 4 (calendar month): first due = loan date + 1 month, then +1 month each. Types 5 and 6: first due = loan date, then +30 days each. Type 2 (weekly): first due = loan date, then +1 week each.

**Functions.**
- `onCreate()`: bind the list to `duelist_adapter`.
- `onContactSelected()`: no-op.
- `whiteNotificationBar()`.

**Rules & edge cases.**
- `newloancreation` passes the loan method's collection break (1 daily, 2 weekly, 3 bi-weekly, 4 monthly) as the schedule type. Daily (1) and bi-weekly (3) loans therefore get an **empty** schedule.
- Weekly due 1 uses interest/4 but later dues use the full interest in their totals. This is moot here because IDue is 0.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `cashbook_adapter` | RecyclerView adapter (Filterable) | `cashbook` | Row `cashbookverifyrow`; rows are `allfinancedailythittamdata`. Shows Particulars plus Debit/Credit (plain integers, only when > 0). Colours verified in smali: "முன் இருப்பு" DarkGreen, "நிகர இருப்பு" `nipcolor`, "ஆக மொத்தம்" Red, others Black. Click does nothing; `ContactsAdapterListener` is an empty interface. The filter on LedgerName/Particulars is unused. |
| `ledgerstatement_adapter` | RecyclerView adapter | `ledgerstatement_act` | Row `ledgerstatment_row`: date dd-MM-yy (blank if null); Narration if non-empty, else Particulars; Debit/Credit as "##,##,##,##0", or "##,##,##,##0.00" when `allowdecimalinamounts` (setting 49). Click calls `editbuttonclicked(item,pos)`. Its filter compares an upper-cased query with lower-cased text, so letters never match; it is unused. |
| `accounttranslist_adapter` | RecyclerView adapter | `accounttrans_list` | Row `accountstranslist_row`: date dd-MM-yyyy, finance short name (`FinanceDetails.FinMailingName`), Particulars, TransAmount. Click calls `onTransactionselected`. Filters by Particulars, case-insensitive. |
| `cashbookabstract_adapter` | RecyclerView adapter | `cashbookabstract` | Row `cashbookabstract_row`: Ledger and Balance ("##,##,##,##0"). Grouping 7 shows blue (0xFF0000FF), 15 DarkMagenta, others 0xFF444444. Click calls `onContactSelected(item, Integer pos)`. Filters by Ledger. |
| `handloans_adapter` | RecyclerView adapter | `handloans_list` | Row `handloans_row`: LedgerName, finance short name, Receivable/Payable ("##,##,##,##0", blank when 0). Click calls `partyledgerlistlistner.onContactSelected(handloans_data)`. Filters by LedgerName. |
| `ledgerstatementselecteledger_adapter` | RecyclerView adapter | `ledgerstatmentsdateselection` | Row `ledgersstatementlist_row`: ledger name. Click calls `onContactSelected`. The filter has the same upper/lower-case bug as `ledgerstatement_adapter` and is unused. |
| `accledgerssearch_adapter` | RecyclerView adapter | `accledgers_searchlist` | Row `accledgers_row`: ledger name and group name (local `tblGroups`). Click calls `onContactSelected`. Filters by name, case-insensitive. |
| `spinnersearch_adapter` | RecyclerView adapter | `spinnersearch_list` | Row `spinnersearch_row`: `displaymember` (`valuemember` hidden). Click calls `onContactSelected(item,pos)`. Filters by `displaymember`, case-insensitive. |
| `duelist_adapter` | RecyclerView adapter | `dueschdule` | Row `duelist_row`: Sno, Duedate (dd-MM-yyyy via `DateTimeFormatter`), Totaldue. Click calls `onContactSelected`. Its filter returns every row. |
| `expensesentrylist_adapter` | RecyclerView adapter | `expensesentry_searchlist` | Row `expenseslist01_row`: LedgerName and Amount ("%.2f"). Click calls `onContactSelected`. Filters by LedgerName, case-sensitive contains. |
| `acctransactionslit_data` | Model (Serializable) | vouchers, lists, ledger statement, hand loans; also `tbbspl_*` and `simplethittamlist_act` via `globalvars.acctransactionslit_datalist` | TransId, RowId, FWVWUDTransId, Finid, TTId, TransDate (`java.sql.Date`), TransAmount (int), Debit, Credit, LedgerIdOne/LedgerStatusOne, LedgerIdTwo/LedgerStatusTwo ("Source"/"Target"), Narration, Particulars, ImageFileName |
| `expenses_data` | Model (Serializable) | offline expenses screens | ID, FinId, LedgerId, LedgerName, Amount (double), Narration, ServerGenTransId, TTId, sourceledgerid |
| `accledgers_data` | Model (Serializable) | ledger list and detail screens | LedgerId, LedgerName, GroupId, Finid, Screentype, isDefault, isReserved, isdefaultexpense (true when the ledger is in `PattiExpLedgers`) |
| `dueslist_data` | Model (Serializable) | `dueschdule`, `duelist_adapter`, `defaulttables.prepareandreturnduelist`, `newloancreation` | sno, duedate, dueperiodfromdate, dueperiodtodate (`LocalDate`), principledue, interestdue, totaldue; a no-op static `getExtras()` |
| `cashbankledgersobj` | Model | **unused** (no references in Java or smali) | LedgerId, LedgerName, Finid (Double), Alloted, LedgerType, istobeVerified. Mirrors the local `CashBankLedgers` table. |
| `cashabstract_data` | Model | `cashbookabstract` and its adapter | GroupId, Grouping, LedgerId, FinId, Ledger, Balance |
| `handloans_data` | Model (Serializable) | `handloans_list`, `accounttransactions_act` (`handloandata`) | FinId, LedgerId, LedgerName, Receivable, Payable |
| `translist_data` | Model (Serializable) | **unused** | No (String), Particulars, TransDate (`java.sql.Date`), Transid (Double) |
| `qatledgers_data` | Model | **unused** (the code reads the `qatledgers` table directly) | TTId, AType, LedgerId (Double), LedgerName |
| `ledgersstatementledgerlist_data` | Model | `ledgerstatmentsdateselection` and its adapter | LedgerId, LedgerName |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_quickacccounttransactions.xml` | `accounttransactions_act` | Type header `qat_ttypeid` (hidden by code); Date `qatl_transdateid`; Finance `qat_financeid`; SNo `qat_CWVWUDTransIdDispid`; Source/Target account EditTexts; Amount (numberDecimal); Notes; hidden image views `groupphotoimageviewid` and `shg_img_plus`; buttons Update/DELETE/NEW; progress bar and percentage text |
| `activity_acctranslist.xml` | `accounttrans_list` | Toolbar; From/To date TextViews and refresh ImageButton; RecyclerView `acctranslist_recycler_view`; progress bar; FAB `acctranslist_addaccounttransactionfabid` |
| `accountstranslist_row.xml` | `accounttranslist_adapter` | Date, finance short name, particulars, amount |
| `activity_cashreceiveandreturns.xml` | `cashreceiveandreturns` | Notes TextView, header "Amount Received"/"பெற்றுக் கொள்ளப்பட்ட தொகை", amount EditText (numberSigned), SAVE button |
| `activity_employeesearch.xml` | `accledgers_searchlist`, `expensesentry_searchlist` (shared with employee, stock and loan-method lists in other groups) | Toolbar; include `customersearchlist` (RecyclerView `recycler_view`); FAB `addnewemployeeid`; overlay `progress_Overlay` with ProgressBar |
| `expenseslist01_row.xml` | `expensesentrylist_adapter` | Ledger name, amount |
| `activity_expensesentry.xml` | `expensesandreceipt_offlineentry` | "Expenses Account"/"செலவு கணக்கு" label and value; "Exepenses Amount"/"செலவு தொகை" (number); Notes/"குறிப்பு"; "Paid as / in" EditText; Update/"சேமி" and DELETE/"நீக்கு" |
| `activity_cashbook.xml` | `cashbook` | Date "Date"/"தேதி"; "Ledger" EditText `bankledgers_id`; SHOW (disabled until a ledger is picked); RecyclerView; e-mail status TextView; buttons "Cash Book Verified" and CLEAR VERIFICATION (both gone/disabled initially); progress bar and progress text |
| `cashbookverifyrow.xml` | `cashbook_adapter` | Hidden Ledger column; Particulars, Debit, Credit |
| `activity_cashbookabstract.xml` | `cashbookabstract` | Toolbar, RecyclerView `cashbookabstract_recycler_view`, progress bar |
| `cashbookabstract_row.xml` | `cashbookabstract_adapter` | Ledger name, amount |
| `activity_handloanslist.xml` | `handloans_list` | Toolbar with column header "Party / FIN / Receivable / Payable"; RecyclerView `acctranslist_recycler_view`; progress bar |
| `handloans_row.xml` | `handloans_adapter` | Party ledger name, finance short name, receivables, payables |
| `activity_ledgerstatmentsdateselection.xml` | `ledgerstatmentsdateselection` | AppBar/Toolbar; "From Date"/"ஆரம்ப தேதி" and "To Date"/"நிறைவு தேதி" TextViews; label "Accounts Ledgers"/"வரவு செலவு கணக்குகள்"; include `customersearchlist` |
| `ledgersstatementlist_row.xml` | `ledgerstatementselecteledger_adapter` | Ledger name |
| `activity_newloanslist.xml` | `ledgerstatement_act` (shared with loanslist_act, newloanslist_act, newpartieslist_act, newpartiesnewloanslist_act, newtodaycollectionofflinedata_list) | Toolbar; hidden "Line Name" bar; From/To dates and refresh; RecyclerView `loansslist_recycler_view`; hidden totals bars (Total Loan/Interest/DC, Count/Collection/Balance); FAB `newloan_fab` (hidden by this screen) |
| `ledgerstatment_row.xml` | `ledgerstatement_adapter` | Date, particulars, debit, credit |
| `activity_newacccountsledgers.xml` | `accountledgerdetails` | Ledger Name, Accounts Group, "Ledger of the Finance" row (`newaccled_financenamelyt`), checkbox "Treat as Executive level Ledger", Update/"சேமி" and DELETE/"நீக்கு", progress bar |
| `accledgers_row.xml` | `accledgerssearch_adapter` (also collectionreportprintingmethods_adapter) | Ledger name ("Acc Ledger Name"), group name |
| `activity_spinnersearchlist.xml` | `spinnersearch_list` | Toolbar and include `customersearchlist` |
| `spinnersearch_row.xml` | `spinnersearch_adapter` | Display member (placeholder "Party Name"/"பார்ட்டி பெயர்"), hidden value member |
| `activity_dueschdule.xml` | `dueschdule` | Header "Due No / Due Date / Due Amount"; RecyclerView `duelistrecyclerviewid` |
| `duelist_row.xml` | `duelist_adapter` | Due number, due date, due amount |

---
[← brain.md](../../brain.md) · Module 8.13 of 20 · Previous: [8.12 Thittam (collection plan) and weekly analysis reports](12-thittam-collection-plan-weekly-analysis-reports.md) · Next: [8.14 ST Loans and OF Loans (interest-bearing loan books)](14-st-loans-of-loans-interest-bearing.md)
