# 8.14 ST Loans and OF Loans (interest-bearing loan books)

[← brain.md](../../brain.md) · Module 8.14 of 20 · Previous: [8.13 Accounts transactions, cash/bank book, expenses & incomes, hand loans and ledger statements](13-accounts-transactions-cash-bank-book-expenses.md) · Next: [8.15 Finance transactions lists, party/loan ledgers and loan & deposit analysis reports](15-finance-transactions-lists-party-loan-ledgers.md)

This module runs two mirror-image, day-rate loan books **online only** — every read/write goes by JDBC to the selected branch SQL Server (`globalvars.selbranchconnString`) — while a local SQLite mirror (`tblSTLoans`/`tblOFLoans`, `STLoanStatusforAPP`/`OFLoanStatusforAPP` — filled only by `otpverificationact` at OTP login, status via procs `GetSTLoansStatus`/`GetOFLoansStatus`; `refreshorreloadbrancheslist` empties the two loan tables without refilling them) is used for lists and for loading a loan into the form. **ST Loans** (TTID 3 "ST லோன்", interest TTID 4 "STL Int", principal repayment TTID 15 "STL RP"; tables `tblSTLoans`/`tblSTLoanInt`) are short-term loans the finance **gives** to a party: `defaulttables.fetchtranstype_inorout` returns −1 (money out) for TTID 3 and +1 (money in) for 4 and 15, the loan voucher debits the party and credits cash/bank, the printed voucher is signed by "கடன் பெறுபவர்" (borrower), and `meetingreport` labels the party action "Collect Interest". **OF Loans** (TTID 5 "OF லோன்", interest TTID 6 "OFL Int", repayment TTID 20 "OFL RP"; tables `tblOFLoans`/`tblOFLoanInt`) are the reverse — money the finance **receives/borrows** from a party or partner (TTID 5 = +1, 6 and 20 = −1; the party ledger is credited and cash/bank debited, the loan date is called "Deposit Date" in validation, and `meetingreport` says "Pay Interest"); the abbreviation "OF" is never expanded anywhere in the APK. Every loan carries a finance-wise partner recommender, a daily rate in ₹ per ₹100 per day (monthly = daily × 30), optional document charges (hard-coded ledger 104) and an interest-start date; the interest due is computed by server stored procedures and settled in the two "Interest" screens, which post the receipts/payments and print Tamil interest receipts.

## ST Loan entry / edit — `stloans`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`, implements `individualloansanddepositsstatuslist_adapter.ContactsAdapterListener`); manifest label "ST Loans", `windowSoftInputMode=stateAlwaysHidden`; action bar colour `stloandarkbackground` (DarkGoldenrod). `ASYNCSaveLoan.doInBackground` is a JADX "decompiled incorrectly" method — the control flow below was checked in smali. |
| **Layout(s)** | `activity_stloan.xml` (+ list rows `individualloanordepositdetails_row.xml`) |
| **Opened from** | `fintrans_list` (drawer **ST Loans** `nav_stloans` → `fintrans_list` ttid 3): FAB → extras `ttid`=3, `actiontype`=7010 (`loan_newloanrequestfor_newparty`), empty `transdata`, forResult 7010; list row → `transdata` (`fintrans_data`: party ledger/address + loan id), `calltype`=2, `actiontype`=7011 (`loan_exsistingpartyloans`), forResult 100 (the list reloads on return). |
| **Opens** | `parties_list`: party picker (title "Select Party", `screentype` 21, `requestcode` 7004 `loan_getpartyname`, `finid` 1; rc 7010; lists parties and partners, ScreenType 21/22) — only in new-party mode; partner picker (`screentype` 22, `requestcode` 7001 `loan_getrecommendedpartnername`, `finid`; rc 7001 → active partners of that finance from `tblFinwisePartners`); party-recommendation picker (`screentype` 21, `requestcode` 7002; rc 7002). `spinnersearch_list` (`title`, `searchlist`): "Finance" (rc 7005), "Received in/as" for Paid From (rc 7003) and for DC Received As (rc 7031), "Transaction Method" (rc 7009). Android print UI, PDF viewer (ACTION_VIEW), share chooser. |
| **Data** | Remote: procs `STL_ShowIndSTLStatus`, `InsertTransSerial`, `InserttblTransactionsMasterForMonthly`, `InserttblSTLoans` / `InserttblSTLoanswithUserDefinedLoanNo`, `Inserttbltransactionsdetails`; tables `tblSTLoans`, `tblTransactionsMaster`, `tbltransactionsdetails`, `tblstloanint` (count only). Local SQLite: `tblSTLoans` (read/insert/update/delete), `STLoanStatusforAPP` (insert), `FinanceDetails`, `tblLedgers`, `qatledgers` (names via `defaulttables.fetchledgername`). Globals `isFullAccessAllowed`, `accyearfromdate`/`accyeartodate`. No SharedPreferences (the dossier's "prefs read" names are JDBC result-set columns). |

**Purpose.** Lend money to a party: create a new ST loan, review all of the party's ST loans with repaid/balance/status, edit or delete a loan, and print or share the Tamil loan voucher.

**UI.** All labels are hard-coded English in the layout (no Tamil resource); only the printed voucher is Tamil.
- Header "Party Name": party name, address line 1, line 2 + city (`stloan_LedgerNameid`, `stloan_partyaddline1id/2id`); tapping any of them opens the party picker, but only while actiontype is 7010.
- "LOANS LIST": columns FIN · No · Date · Amount · Repaid · Balance · Status (`recycler_view`), add-new-loan icon (`stloans_addnewloanid`), "Total Loan : n" (sum of Balance).
- "LOANS DETAILS" form: Finance (picker), Partner Recommendation (picker), ST Loan No (optional user-defined number; blank = server-assigned), Date (date picker, default today), Amount (whole rupees) with the amount in Tamil words underneath (`nototamilwords.tamilcharstringvalue`), Daily Interest Rate (default 0.070), Monthly Interest Rate, Party Recommendation (optional; remove icon `interestcalculationandpayments_removepartyrecid` clears it), Document Charges, DC Received As (cash/bank picker), Interest From (date picker, default today), Remarks, Paid From (cash/bank picker), Bank Transaction Type + Bank Ref No (shown only when Paid From is a bank ledger), buttons SUBMIT / PRINT / DELETE; hidden `imgfacebook` ImageView; ProgressBar.

**Menu / actions.** No options menu. Add icon → actiontype 7006 (`loan_newloanrequest`), blank form for the same party. Loan row tap → loads that loan into the form (actiontype 7007 = edit). SUBMIT → validate and save. DELETE (enabled only when `globalvars.isFullAccessAllowed`) → dialog "Alert" / "Deleted Loan cannot be Recovered" (OK / Cancel) → actiontype 7008. PRINT → "Print Options": Direct Print · Show as PDF · Share.

**How it works.**
1. `onCreate`: opens SQLite `venusmicrofinv005`, resets the StrictMode VM policy (permits file URIs), binds views and pickers. Rate-sync TextWatchers: while Daily has focus, Monthly := Daily × 30; while Monthly has focus, Daily := Monthly ÷ 30 (format `0.000`); both are suppressed while a loan is being displayed. Extras: 7011 → shows the `transdata` party and runs `ASYNCindividualloandetails`; 7010 → `preparescreenfornewdeposit()`.
2. `ASYNCindividualloandetails`: `exec STL_ShowIndSTLStatus @PartyLedgerId= <party>` → PartyLedgerId, TransId, STLoanId, No, Date, Fin, Amount, Repaid, Balance, Status, TGFWTId, TGWTId; sums Balance into "Total Loan :". `onPostExecute` auto-opens the just-saved loan (`recentlysavedstloanid`) or else the loan tapped in `fintrans_list` (`transdata.getLoanordepositid()`).
3. `onloanselected`: actiontype := 7007; reads the loan from the **local** `tblSTLoans where STLoanId=` (24 columns) into `stloans_data` and fills the form; partner / party-recommendation / paid-from / DC ledger names come from `qatledgers`, the finance name from `FinanceDetails.FinMailingName`; `fetchgroupid(SourceLedgerId)` = 5 (bank) shows the bank fields with the mode name (`fetchmoneygivenmode`: 1 Cheque, 2 RTGS, 3 NEFT, 4 Others) and ref no., 6 (cash) hides them; keeps the row's TGWTId/TGFWTId for printing; `displayloanmode()`.
4. Picker chains: in new-party mode, choosing the party (`receivedparty`) resets the form and opens the Finance picker automatically; choosing the finance opens the Partner picker; then focus moves to Amount. The Paid From / DC Received As list is `tblLedgers where isreserved=0 and finid=<loan finance> and GroupId in (5,6)` (cash and bank). Picking a bank ledger for Paid From auto-opens "Transaction Method" (Cheque/RTGS/NEFT/Others → `BankRefId`) and then focuses Bank Ref No.
5. SUBMIT validation (alert title "Incomplete data", checked in this order): "Party not selected..."; Interest From earlier than Date → "Interest cannot be calculated from prev date..."; amount blank or 0 → "Loan Amount cannot be Zero.."; Document Charges > 0 with no DC ledger → "Target Ledger for Document Charges is not selected.."; "Daily Interest Rate cannot be Zero.."; "Monthly Interest Rate cannot be Zero.."; "Finance not selected.."; "Partner not selected.."; "Cash or Bank Account is not selected.."; bank ledger without type → "Bank Transaction type not selected.."; bank ledger with an empty *type* text → "Bank Transaction No cannot be empty.."; Date after `accyeartodate` → "Date Selection Error..." / "Date cannot be after current accounting peroid <from> to <to>". Then SUBMIT is disabled and `ASYNCSaveLoan` runs (window flag 16 = not touchable until it finishes).
6. `ASYNCSaveLoan`:
   - **New (7006/7010):** `{call dbo.InsertTransSerial(FinId, TTypeId=1, TransDate)}` → OUT `TSId`, `TGWTId` (voucher no.), `TGFWTId`; `{call dbo.InserttblTransactionsMasterForMonthly(TransDate, TransType=3, Narration=NULL, FinId, TransAmount=loan amount, TGWTID, TGFWTId)}` → OUT `TransId` (+ `FWVWTransId`, `FWVWUDTransId`); then `InserttblSTLoanswithUserDefinedLoanNo` when a loan no. was typed, else `InserttblSTLoans` (FWUDSTLoanId becomes an OUT param), 24 params: TransId, TransDate, FWSTLoanId, FWUDSTLoanId, PartyLedgerId, STLoanAmount, STLIntRateinDM, STLIntRateinMM, STLPri/Sec/TriIntRateinDM = NULL, RecPartnerId, RecPartyId (NULL if none), FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET (= IntCalcStartOn), CurStat = true, BankRefId/BankRefNo (NULL for cash), SourceLedgerId, DCTargetLedgerId → OUT `STLoanId`. Mirrors into SQLite `tblSTLoans` and `STLoanStatusforAPP` (Repaid 0, Balance = amount, Status "Live") and the on-screen list.
   - **Edit (7007):** `update tblSTLoans set TransId=?,TransDate=?,FWSTLoanId=?,FWUDSTLoanId=?,…,SourceLedgerId=? ,DCTargetLedgerId=? where STLoanId=?`; `update tblTransactionsMaster set TransDate=?, FinId=?,TransAmount=? where TransId=?`; the same update on SQLite `tblSTLoans`; the list row is refreshed (Balance = Amount − Repaid).
   - **Delete (7008):** `select count(transid) transidcount from tblstloanint where stloanid= <id>`; if 0 → `delete from tbltransactionsdetails where transid=T; delete from tblSTLoans where STLoanId=L; delete from tbltransactionsmaster where transid=T`, SQLite delete, row removed; otherwise nothing is deleted.
   - **Every path then** (smali `goto :goto_e`) rebuilds the voucher lines: `delete from tbltransactionsdetails where transid=T`, then `{call dbo.Inserttbltransactionsdetails(TransId, Sno, LedgerId, LedgerStatus, LedgerAmount, AmtType, AgnstLedgerSNo)}`:

     | Sno | Ledger | Status | Dr/Cr | Amount | Against Sno |
     |---|---|---|---|---|---|
     | 1 | SourceLedgerId (Paid From cash/bank) | Source | Cr | loan amount | 2 |
     | 2 | PartyLedgerId | Target | Dr | loan amount | 1 |
     | 3 (DocCharges > 0) | 104 (hard-coded document-charges ledger, also used by MC loans) | Source | Cr | DocCharges | 4 |
     | 4 (DocCharges > 0) | DCTargetLedgerId (DC Received As) | Target | Dr | DocCharges | 3 |
   - `onPostExecute`: recomputes "Total Loan", scrolls to top (`fullScroll(33)` = FOCUS_UP), toast "Updated" (7006/7007/7010) / "Deleted" / "Interest Entries found, Could not delete", clears the form, re-selects the last list row and reruns `ASYNCindividualloandetails` (which re-selects the saved loan).
7. **Loan voucher** (`drawPage`; two identical copies side by side, left x 15–285 and right 300–565): "உ"; finance FinMailingName and "AddLine1, City" (SQLite `FinanceDetails`); title "கடன் வவுச்சர்" (Loan voucher); "எண் :" TGWTId; "தேதி :" loan date; "To :" party name, address lines, "City, Mobile:<MobileNo1>"; "பரிந்துரை :" (recommended by) partner; "கடன் எண் :" FWUDSTLoanId; "கடன் தொகை" (loan amount) ₹; "வட்டி வீதம்" (interest rate) = daily rate (0.000); "ஆவணக் கட்டணம்" (document charges) if > 0; declaration "ரூபாய் <amount in Tamil words> / நாள் ஒன்றுக்கு ரூ 100க்கு <rate×100> பைசா / வட்டி வீதத்தில் கடனாகப் பெற்றுக் கொண்டேன்." ("I received this as a loan at N paise per ₹100 per day"); Paid-From ledger name (+ "<mode>:<ref no>" for bank); captions "கடன் பெறுபவர்" (borrower) · "அலுவலர்" (officer) · "மேனேஜிங் பார்ட்னர்" (managing partner); "For <finance>".
   - Direct Print → `PrintManager` job "<app_name> Document", ISO A5 landscape, document "STLoanReceipt<party>.pdf". Show as PDF → one 576×400-pt page (JADX prints 400 as `HttpStatus.SC_BAD_REQUEST`) saved as `Documents/venusmfc/stlvoc<TGWTId>.pdf` and opened through FileProvider `<package>.provider` (else toast "Error while opening the document"). Share → 290×400-pt page (only the left copy fits), same file name, ACTION_SEND with MIME "text/csv", chooser "Share".

**Functions.**
- `onCreate()` — view binding, pickers, rate-sync watchers, actiontype routing.
- `lambda$onCreate$0/1/2/3/5/6/7/8/10` — open party-recommendation, partner, Paid-From, DC-target, finance, party (×3 header views) and transaction-method pickers; `$4` clears the party recommendation.
- `lambda$onCreate$11/12` — rate-field focus flags; `$13`–`$16` date pickers (Date, Interest From); `$17` add new loan; `$18` SUBMIT validation; `$19`–`$21` delete confirmation.
- `showincompletedataalert(title,msg)` — OK-only alert.
- `preparescreenfornewdeposit()` — clears the form, new `stloans_data` (dates = now, party = current), hides bank fields/PRINT/DELETE, shows SUBMIT, default rates.
- `displayloanmode()` — PRINT/DELETE/add enabled; full access → SUBMIT, PRINT, DELETE visible and enabled; otherwise DELETE hidden.
- `showprintandshareoptions(Context)` (JADX `lambda$onCreate$9`) and `lambda$showprintandshareoptions$23` — print/PDF/share options.
- `clearboxes()` — blanks the inputs and disables SUBMIT/DELETE/PRINT. `whiteNotificationBar(View)` — light status bar.
- `onloanselected()` — loads a loan from local `tblSTLoans` into the form; `onthumnailclicked()` — no-op.
- `ASYNCindividualloandetails` — party's loan list (`STL_ShowIndSTLStatus`).
- `onActivityResult()` — handles rc 7002, 7010, 7001, 7031, 7003, 7005, 7009 (resultCode 1).
- `ASYNCSaveLoan` — create / edit / delete, voucher posting, local mirror.
- `drawPage(Page,int)` — voucher drawing; `drawCenter()` / `drawCenterwithbox()` — unused helpers.
- `printDocument()`, `createPdf(name)`, `sharePdf(name)`, inner `MyPrintDocumentAdapter` (`onLayout` builds the document info, `onWrite` draws the pages, `pageInRange`). JADX shows `onWrite`'s `finally` around each loop pass; smali shows it only wraps `writeTo` (no bug).

**Rules & edge cases.**
- Delete is gated only by `isFullAccessAllowed`; editing and printing are open to anyone who reaches the screen. Delete is refused if any `tblstloanint` row exists (interest **or** principal receipts are both stored there).
- Bug (smali-verified): the delete path falls through into the posting block, which re-inserts the 2–4 `tbltransactionsdetails` lines for the TransId it just deleted (orphan detail rows unless the server rejects them); it also re-posts when the deletion was refused.
- Bug: the new-loan default Monthly rate is `String.format("%.2f", 0.07)` = "0.07" (should be 2.10), so `STLIntRateinMM` is saved as 0.07 unless the user edits a rate field (OF correctly uses 2.10).
- Bug: "Bank Transaction No cannot be empty.." tests the transaction-*type* field, so an empty Bank Ref No is accepted.
- Same-day quirk: an Interest From picked through the date picker is midnight, while the default Date carries the current time, so picking today for Interest From (without re-picking Date) is rejected as "prev date".
- Only the upper accounting-year bound is checked for the loan date. Amount and charges use `Integer.parseInt` (no paise; `numberSigned` lets a negative amount through).
- Non-full-access users: `displayloanmode()` never re-enables SUBMIT, so after one save it stays disabled until the add-new-loan icon is used.
- Loans load from the local `tblSTLoans` copy, which only `otpverificationact` fills (a branch reload in `refreshorreloadbrancheslist` runs `delete from tblSTLoans`/`tblOFLoans` and never refills them, leaving `fintrans_list` and the "ST/OF Loan Parties" pickers empty until the next OTP login); a loan that exists on the server but not locally is listed (server proc) but tapping it loads nothing.
- No DB transaction and all SQL errors are swallowed (`printStackTrace`); "Updated" is shown even if the server calls failed. The local mirror stores `FWSTLoanId` = FWUDSTLoanId and an edit writes that value back to the server's `FWSTLoanId`.
- `clearboxes()` does not clear the "DC Received As" text (its value is reset to 0). After a party pick, address line 2 is overwritten by City alone when City is present.
- Dead code: a 4th print option (share as image — 300×370 bitmap drawn into the hidden `imgfacebook`, saved as `share_image_<millis>.jpg`, ACTION_SEND "image/jpg", toast "Sharing tools have not been installed.") is unreachable because the dialog lists only 3 options; `isinterestfromdateischangedbyuser` is set but never read. Soft setting `softsettingsvalue.STLIntRate` (id 59) is downloaded elsewhere but not used here — the default rate is hard-coded 0.07 per day.

## OF Loan entry / edit — `ofloans`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`, implements `individualloansanddepositsstatuslist_adapter.ContactsAdapterListener`); manifest label " OF Loans", `windowSoftInputMode=stateAlwaysHidden`; title "OF Loans", colour `ofloandarkbackground` (DarkCyan). `ASYNCSaveLoan` is partially decompiled (flow checked in smali). |
| **Layout(s)** | `activity_ofloan.xml` (+ `individualloanordepositdetails_row.xml`) |
| **Opened from** | `fintrans_list` (drawer **OF Loans** `nav_ofloans` → ttid 5): FAB → `ttid` 5, `actiontype` 7010, empty `transdata` (rc 7010); row → `transdata`, `calltype` 2, `actiontype` 7011 (rc 100). |
| **Opens** | `parties_list` party picker ("Select Party", screentype 21, requestcode 7004; rc 7010, new-party mode only) and partner picker (screentype 22, requestcode 7001, `finid`; rc 7001); `spinnersearch_list` "Finance" (7005), "Received in/as" (7003), "Transaction Method" (7009); Android print UI. |
| **Data** | Remote: `OFL_ShowIndOFLStatus`, `InsertTransSerial`, `InserttblTransactionsMasterForMonthly`, `InserttblOFLoans` / `InserttblOFLoanswithUserDefinedLoanNo`, `Inserttbltransactionsdetails`; tables `tblOFLoans`, `tblTransactionsMaster`, `tbltransactionsdetails`, `tblofloanint` (count). Local SQLite: `tblOFLoans`, `OFLoanStatusforAPP`, `tblLedgers`, `qatledgers`, `FinanceDetails` (finance name), `FinanceMaster` (voucher header — see edge cases). |

**Purpose.** Record money the finance takes in from a party or partner as an OF loan/deposit: create one, review the party's OF loans, edit, delete and print the voucher.

**UI.** Same structure as `stloans` with `ofloan_*` ids: Party Name block; "LOANS LIST" (FIN/No/Date/Amount/Repaid/Balance/Status, add icon, "Total Loan :"); form Finance, Partner Recommendation, **OF Loan No**, Date, Amount (+ Tamil words), Daily Interest Rate (default 0.070), Monthly Interest Rate (default 2.10), Document Charges, Interest From, Remarks, **Received as/in** (the cash/bank account the money comes into), Bank Transaction Type, Bank Ref No; SUBMIT / PRINT (`gone` in the layout) / DELETE. There is no Party Recommendation and no DC Received As field.

**Menu / actions.** Add icon (hidden in new-party mode) → actiontype 7006; loan row → edit (7007); SUBMIT; DELETE → "Alert" / "Deleted Loan cannot be Recovered" → 7008; PRINT → prints immediately (no options dialog).

**How it works.**
1. `onCreate` mirrors `stloans` (without the StrictMode reset): 7011 → `ASYNCindividualloandetails` (`exec OFL_ShowIndOFLStatus @PartyLedgerId= <transdata ledger>`; OFLoanId is stored in the shared model's STLoanId field) and auto-opens the tapped loan; 7010 → blank form, party fields emptied, add icon hidden.
2. Party picked (rc 7010) → actiontype := 7006, then the Finance → Partner picker chain. "Received in/as" uses the same cash/bank list; a bank ledger opens the Transaction Method picker.
3. `onloanselected` reads local `tblOFLoans where OFLoanId=` (19 columns); the partner name comes from `fetchstledgername` (`tblLedgers`).
4. SUBMIT validation (title "Incomplete Data"): Party → Amount → Daily rate → Monthly rate → Finance → Partner → Cash/Bank → bank type → bank "No" (same type-field bug as ST) → Interest From before Date → "Interest cannot be calculated before Deposit Date" → accounting-year end check ("Date Selection Error...").
5. `ASYNCSaveLoan`:
   - New: `InsertTransSerial(FinId, TTypeId 1, TransDate)`; `InserttblTransactionsMasterForMonthly(…, TransType=5, …)`; `InserttblOFLoans[withUserDefinedLoanNo]` (18 params: TransId, TransDate, FWUDOFLoanId (IN or OUT), PartyLedgerId, OFLoanAmount, OFLIntRateinDM, OFLIntRateinMM, RecPartnerId, FinId, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, Remarks, BankRefId/BankRefNo (NULL for cash), DocCharges, SourceLedgerId = Received-as ledger) → OUT `OFLoanId`; SQLite `tblOFLoans` + `OFLoanStatusforAPP` ("Live").
   - Edit: `update tblOFLoans set TransId=?,TransDate=?,FWUDOFLoanId=?,PartyLedgerId=?,OFLoanAmount=?,OFLIntRateinDM=?,OFLIntRateinMM=?,RecPartnerId=?,FinId=?,DocCharges=?,Remarks=?,IntCalcStartOn=?,IntCalcStartOnSYSSET=?,CurStat=?,BankRefId=?,BankRefNo=?,SourceLedgerId=? where OFLoanId=?`, `update tblTransactionsMaster set TransDate=?, FinId=?,TransAmount=? where TransId=?`, SQLite update.
   - Delete: `select count(transid) transidcount from tblofloanint where ofloanid= <id>` → if 0, delete details / `tblOFLoans` / master and the SQLite row.
   - Then (all paths, smali-verified) the voucher lines are re-posted:

     | Sno | Ledger | Status | Dr/Cr | Amount | Against Sno |
     |---|---|---|---|---|---|
     | 1 | PartyLedgerId | Source | Cr | loan amount | 2 |
     | 2 | cash/bank (Received as/in) | Target | Dr | loan amount | 1 |
     | 3 (DocCharges > 0) | cash/bank | Source | Cr | DocCharges | 4 |
     | 4 (DocCharges > 0) | 104 (document-charges ledger) | Target | Dr | DocCharges | 3 |

     i.e. cash/bank rises by the loan, and document charges are paid **out** by the finance.
   - `onPostExecute`: the same toasts as ST, clears the form, re-selects the last list row (no reload from the server).
6. PRINT (visible only for full-access users once a loan is displayed) → ISO A4 portrait job "<app_name> Document", document "ofloanvoucher<party>.pdf". `drawPage` is a copy of the ST voucher (same "கடன் வவுச்சர்" layout, "கடன் பெறுபவர்" caption and "கடனாகப் பெற்றுக் கொண்டேன்" wording, although here the finance is the borrower); header read from local `FinanceMaster`, partner name from `qatledgers`.

**Functions.**
- `onCreate()`; `lambda$onCreate$0`–`$7` (partner, received-as, finance, party ×3, print, transaction-method handlers); `$8/$9` rate focus flags; `$10`–`$13` date pickers (mutate shared `Date` objects with deprecated setters); `$14` add loan; `$15` SUBMIT validation; `$16`–`$18` delete confirmation.
- `showincompletedataalert()`, `preparescreenfornewdeposit()`, `displayloanmode()` (full access → SUBMIT/PRINT/DELETE visible; otherwise SUBMIT visible, DELETE hidden), `clearboxes()`, `whiteNotificationBar()`.
- `onloanselected()`, `onthumnailclicked()` (no-op), `onActivityResult()` (rc 7010, 7001, 7003, 7005, 7009).
- `ASYNCindividualloandetails`, `ASYNCSaveLoan`.
- `drawPage()`, unused `drawCenter()`/`drawCenterwithbox()`, `printDocument()`, `MyPrintDocumentAdapter` (`onLayout`, `onWrite`, `pageInRange`).

**Rules & edge cases.**
- DELETE is hidden for non-full-access users; the same delete fall-through re-posting bug as ST exists here; SUBMIT is likewise never re-enabled for non-full users after a save.
- Bug: the local status row is inserted with `OFLoanId` = FWUDOFLoanId (the user loan no.), so `fintrans_list`'s join `tblOFLoans a left join OFLoanStatusforAPP c on a.OFLoanId=c.OFLoanId` misses the status of OF loans created on the device until the next download.
- No Show-as-PDF/Share for the OF voucher. The local SQLite DB never creates a `FinanceMaster` table (no code in the APK does; only `FinanceDetails` exists), so the print path's `rawQuery("… from FinanceMaster …")` most likely throws "no such table" — unclear whether it exists at runtime.
- Same caveats as ST: loans load from the local `tblOFLoans` mirror (emptied by a branch reload, refilled only at OTP login), only the upper accounting bound is checked, integer amounts, no transaction, errors swallowed, "Updated" shown regardless.

## ST Loan Interest collection — `stloaninterestcalculationandpayments`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`, implements `individualloansanddepositsstatuslist_adapter.ContactsAdapterListener`, `stloansinterestcalculationlist_adapter.insterestcollectiondetailsrow`, `prevreceiptslist_adapter.PreveReceipts_AdapterListener`); theme `AppTheme.NoActionBar` with its own Toolbar titled "ST Loan Interest", colour `stloandarkbackground`. |
| **Layout(s)** | `activity_interestcalculationandpayments.xml` (+ `individualloanordepositdetails_row.xml`, `interestcalculation_row.xml`, `prevreceipts_row.xml`); menu `menuemployeesearchlist` |
| **Opened from** | Drawer **ST Loans Int** (`nav_stloanint`) → `parties_list` (screentype 21, ttid 4, requestcode 9002 `Interestdetails_AccessRequest`; titled "ST Loan Parties", list = `tblLedgers where LedgerId in (select PartyLedgerId from tblSTLoans)`) → party tap → extras `ledgerdata` (`partydetails_data`), `ttid` 4. Also `meetingreport`'s party dialog option "Collect Interest" (its ttid 3 → passes ttid 4). |
| **Opens** | `partyledgers_list` (loan row tap: `loanordepositid`, `partyledgerid`, `requesttype` 7029 `individualloandetailsrequestfrominterestcollectionscreen`, `selectedfinancenames` = Fin, `partyname`, `loanno`, `ttid`) — the loan statement; `stloan_interestcalculationdetails` (grid row tap, extra `intrestcalculationrow`, rc 8002 `interestcalcualation_editrow`); print UI, PDF viewer, share chooser, WhatsApp / WhatsApp Business. |
| **Data** | Remote procs `STL_ShowIndSTLStatus`, `VK_RAWSTLIntSelCustomer`, `STL_PrepareIntPriCollList`, `ShowSTLIntList`, `STLInt_DispIntpayDetails`, `InsertTransSerial`, `InsertDepositInttblTransactionsMaster`, `InserttblSTLoanInt`, `InserttblTransactionsDetails`; tables `tblTransactionsMaster`, `tblSTLoanInt`, `tblTransactionsDetails`. Local SQLite `FinanceMaster` (receipt header — see edge cases), `tblledgers`. Globals `accyearfromdate`/`accyeartodate`, `isFullAccessAllowed`. |

**Purpose.** For one borrower: see their ST loans, compute the interest due on all live loans up to a chosen date, collect interest and/or principal in one go (one voucher set per loan), reopen/edit/delete earlier receipts of the accounting year, and print or WhatsApp the Tamil interest receipt.

**UI.**
- Toolbar: party name and two address lines; search icon.
- "LOANS DETAILS" panel (hidden by default; opened with the "LOAN DETAILS" link): FIN · No · Date · Amount · Repaid · Balance · Status list, "PREV RECEIPTS" link, "Total Loan : n".
- "PREVIOUS RECEIPTS" panel (shown by default): SNo · Date · Principal · Interest of the current accounting year; tapping a row opens that receipt.
- "INTEREST COLLECTION": "Date :" receipt date (default today 00:00; the picker refuses dates outside the accounting year), "Interest upto :" (default = last day of the previous month; picker), refresh icon (recompute); grid header act · PI · Loan · from · upto · days · int · Received; rows (`interestcalculation_row`); "Collectable : n.nn"; UPDATE and PRINT (PRINT disabled until a receipt is saved or reopened); ProgressBar.

**Menu / actions.** Search (SearchView) filters the loan-status list by its Status text; the reused menu also shows "Show all Employees" / "Show Active Employees", which have no handler here; Up/back closes the search first. Grid: row tap → editor; remove icon (only on unsaved rows, TransId 0) → drops the row and recomputes Collectable; tapping the row's type text toggles "selected for deletion" (full-access users only; saved rows turn yellow) — deleted on UPDATE. Previous-receipt tap → reloads that receipt set. UPDATE → save; PRINT → "Print and Share Options".

**How it works.**
1. `onCreate`: reads `ttid` and `ledgerdata`; sets the default dates; loads all three parts (`isloandetailstobeloaded`, `iscollectabledetailsttobeloaded`, `isprevreceiptstobeloaded` = true) via `ASYNCindividualloandetails`.
2. `ASYNCindividualloandetails` (by flag):
   - Loans: `exec STL_ShowIndSTLStatus @PartyLedgerId= <party>`.
   - Interest due: `execute VK_RAWSTLIntSelCustomer  @GraceDays= 10,@CustId=<party>,@IUPDate = '<upto>'` (executeUpdate — prepares the server's raw interest working set), then ` exec STL_PrepareIntPriCollList @isAutoIncEnabled=false, @IntRecptDate='<upto>',@PartyLedId=<party>` → rows PartyLedgerId, STLoanId, FinId, CashLedgerId, IntRate, PenaltyRate, PType (`INT` = interest line, `Pri` = principal line), SNo, ShortName, Loan, LedgerName, FWUDSTLoanId, IntFrom, IntUpto, Days, IsAutoIncEnabled, CalcInt, CutOffDate. INT rows pre-fill Received = (int) CalcInt (truncated); Pri rows start at 0 — the user types the principal being repaid in the editor. Collectable = Σ CalcInt.
   - Previous receipts: `exec ShowSTLIntList @PartyLedId= <party>,@CurPF='<acc-year from>' ,@CurPT='<acc-year to>'` → SetId, SNo, TransDate, Principal, Int.
   - The refresh icon reloads only the interest-due part (and disables PRINT).
3. **Interest formula.** The due amounts come from the server procedures, whose SQL is not in the APK; the grace period (10 days), `PenaltyRate`, `CutOffDate` and `IsAutoIncEnabled` are applied server-side — the device loads them but never uses them. The device-side recalculation (in the editor) is `CalcInt = Principal × DailyRate × Days ÷ 100`, `Days = Upto − From + 1` (inclusive), rounded **up** to the next ₹5 (`ceil(|x|/5)×5`); the rate is ₹ per ₹100 per day (0.07 = 7 paise per ₹100 per day ≈ 2.1 % per month). Interest and principal are separate rows (INT vs Pri) and post to different ledgers (below).
4. Reopen a receipt: `execute STLInt_DispIntpayDetails @PartyLedId= <party>,@SetId=<SetId>` → the saved rows (adds TransId, STLIntPayId, FWSTLIntId, CurSTLoanAmount, TransDate, Received, PrincipalAmount, TargetLedgerId, FWTransId, TGWTId, TGFWTId, Narration) replace the grid; Date := first row's TransDate; PRINT enabled.
5. UPDATE: Date must lie within [`accyearfromdate`, `accyeartodate`] ("Date Selection Error..." / "Date cannot be before|after current accounting peroid <from> to <to>"). `ASYNCSAVECollectedIntandPriDetails` walks the grid:
   - INT row → TTID 4, `TargetLedgerId` := 102 (hard-coded interest-income ledger); Pri row → TTID 15 (STL RP), `TargetLedgerId` := the party ledger.
   - Unmarked **new** row (TransId 0) with Received > 0: on the first row of each loan (when `LastSTLoanId` changes) `InsertTransSerial(FinId, TTypeId 1, Date)` → `SetId` (TSId), receipt no. `TGWTId`, `TGFWTId` (rows of the same loan share the set); `{call dbo.InsertDepositInttblTransactionsMaster(TransType, Narration='Narration', FinId, TransAmount=Received, TGWTID, TGFWTId, TransDate)}` → OUT `TransId`; `update tblTransactionsMaster set TransDate=?, Narration=?, TransAmount=?  where TransId=?` (real narration); `{call dbo.InserttblSTLoanInt(…20…)}` (TransId, TransDate, STLoanId, PartyLedgerId, SNo, CurSTLoanAmount; INT: PrincipalAmount NULL, STLoanAmount = interest basis, CalcInt, PaidInt = Received, Days, IntFrom, IntUpTo; Pri: PrincipalAmount = Received and the rest NULL; FinId, FinSCashLedgerId = CashLedgerId, TargetLedgerId, IntRate, Narration, SetId) → OUT `STLIntPayid`; then the detail lines: Sno 1 CashLedgerId Target **Dr** Received (against 2), Sno 2 TargetLedgerId Source **Cr** Received (against 1) — cash in, interest income or party credited.
   - Unmarked **saved** row (TransId ≠ 0): `update tblSTLoanInt set TransId=?,TransDate=?,STLoanId=?,PartyLedgerId=?,SNo=?,IntFrom=?,IntUpTo=?,Days=?,PrincipalAmount=?,STLoanAmount=?,CalcInt=?,PaidInt=?,FinId=?,FinSCashLedgerId=?,TargetLedgerId=?,IntRate=?,Narration=? where STLIntPayId=?` and re-post the two detail lines.
   - New row with Received 0 → skipped. **Marked** row → `delete from tblTransactionsDetails where TransId=T; delete from tblSTLoanInt where TransId=T; delete from tblTransactionsMaster where TransId=T`.
   - `onPostExecute`: removes deleted and zero rows, recomputes Collectable, reloads only the previous receipts (the loan list's Repaid/Balance is not refreshed), enables PRINT.
6. **Interest receipt** (`drawPage`, one page per loan in the grid; `printpage_data` = finance, loan, party, receipt no. = TGWTId of the loan's first row, current loan amount, user loan no.): two copies side by side; "உ"; finance name and "AddLine1, City"; title "வட்டி ரசீது" (Interest receipt); "எண் :"; "தேதி :" receipt date; "To :" party block; "கடன் எண் :"; "கடன் தொகை : ரூ"; per INT line "<from> முதல் <upto> வரை" (from … to) + "ரூ <basis>க்கு <days> நாட்களுக்கான வட்டி" (interest for N days on ₹X) + amount; per PRI line "அசல் வரவு" (principal received — when Received equals the basis) or "பகுதி அசல் வரவு" (part principal) + amount; "மொத்தம்" (total) = Σ Received of that loan; "ரூபாய் <Tamil words>"; captions "செலுத்துபவர்" (payer) · "வசூலிப்பவர்" (collector) · "மேனேஜிங் பார்ட்னர்"; "For <finance>".
   - Options: Direct Print (ISO A4 portrait, job "<app_name> Document", document "stlinterestreceipt.pdf"); Show as PDF (`Documents/venusmfc/stlinterestreceipt_<ddMMyyyy>.pdf`, 576×400 pages, FileProvider VIEW, else toast "Error while opening the document"); Share (`Documents/venusmfc/stlrec_.pdf`, 290×400 pages, ACTION_SEND "text/csv", chooser "Share"); Share with Whatsapp / Share with Whatsapp Business (`stlinterestreceipt_<dd-MM-yyyy>.pdf`; ACTION_SEND to package `com.whatsapp` / `com.whatsapp.w4b` with extra `jid` = "91" + party MobileNo1 + "@s.whatsapp.net", type `*/*`; failure → alert "Error..").

**Functions.**
- `onCreate()`; `lambda$onCreate$1` (UPDATE + date checks), `$2`/`$3` (toggle loan / receipts panels), `$4`–`$7` (date pickers), `$8` (refresh).
- `onCreateOptionsMenu()` (SearchView → loan-list filter), `onOptionsItemSelected()` (search, home), back-press callback, `whiteNotificationBar()`.
- `onloanselected()` → `partyledgers_list`; `onthumnailclicked()` no-op; `onInterestCalculationRowSelected()` → editor; `onclickedformark()` → toggles `Sel2Del` (full access only); `onremovebuttonclicked()` → removes an unsaved row; `onprevreciptclicked()` → loads a receipt set.
- `ASYNCindividualloandetails`, `ASYNCGetCollectedIntandPriDetails`, `ASYNCSAVECollectedIntandPriDetails` (see above).
- `onActivityResult()` — rc 8002: replaces the row with the same SNo + PType, recomputes Collectable.
- `showprintandshareoptions(Context)` (JADX `lambda$onCreate$0`) builds `printpagelist` and the 5-option dialog; `lambda$showprintandshareoptions$9` dispatches.
- `drawPage(Page,int,printpage_data)`, unused `drawCenter()`/`drawCenterwithbox()`, `printDocument()`, `createPdf()`, `sharePdf()`, `sharePdfwithwhatsapp()`, `sharePdfwithwhatsappbusiness()`, `showincompletedataalert()`, `MyPrintDocumentAdapter`.

**Rules & edge cases.**
- Delete-marking requires `isFullAccessAllowed` (checked in the activity and the adapter); there is no other role check on this screen. The drawer route depends on the local `tblSTLoans` mirror ("ST Loan Parties" = its party ids), so it is empty after a branch reload until the next OTP login.
- Bug: editing a saved row updates `tblSTLoanInt` and the detail lines but not `tblTransactionsMaster` (the voucher master keeps the old amount and date).
- PType case mismatch: the insert path tests `"Pri"`, the update path `"PRI"`, printing uses `equalsIgnoreCase("PRI")`; if the procs return "Pri", an edited saved principal row leaves update parameters 6–12 unset and the update fails silently (depends on the proc output — unclear from client code).
- No DB transaction; errors are swallowed and never shown; the save does not block input (OF does). The narration is first stored as the literal "Narration" and then overwritten.
- `ASYNCGetCollectedIntandPriDetails.onPostExecute` reads `list.get(0)` → crash if the chosen set returns no rows.
- The receipt header queries a local SQLite `FinanceMaster` table that no code in the APK creates (only `FinanceDetails` exists), so printing most likely fails with "no such table" — unclear at runtime.
- JADX artifacts: `setTextSize(1090519040)` is the float bit pattern of 8.0f; page height `HttpStatus.SC_BAD_REQUEST` = 400 pt.
- PDFs containing the party's name, address and mobile number are written to shared external storage (`Documents/venusmfc`).

## OF Loan Interest payment — `ofloans_interestcalculationandpayments`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`, implements `individualloansanddepositsstatuslist_adapter.ContactsAdapterListener`, `ofloans_interestcalculationlist_adapter.insterestcollectiondetailsrow`, `prevreceiptslist_adapter.PreveReceipts_AdapterListener`); theme `AppTheme.NoActionBar`, Toolbar "OF Loan Interest", colour `ofloandarkbackground`. |
| **Layout(s)** | `activity_ofloans_interestcalculationandpayments.xml` (+ `individualloanordepositdetails_row.xml`, `interestcalculation_row.xml`, `prevreceipts_row.xml`); menu `menuemployeesearchlist` |
| **Opened from** | Drawer **OF Loans Int** (`nav_ofloanint`) → `parties_list` (screentype 21, ttid 6, requestcode 9002; titled "OF Loan Parties" = ledgers that appear in `tblOFLoans`) → `ledgerdata`, `ttid` 6; `meetingreport` option "Pay Interest" (its ttid 5 → passes 6). |
| **Opens** | `partyledgers_list` (same extras, requesttype 7029); `ofloan_interestcalculationdetails` (rc 8002, extra `intrestcalculationrow`); print UI, PDF viewer, share chooser. |
| **Data** | Remote procs `OFL_ShowIndOFLStatus`, `VK_RAWOFLIntSelCustomer`, `OFL_PrepareIntPriCollList`, `OFL_PrepareIntPriCollListOverAllPenalty`, `ShowOFLIntList`, `OFLInt_DispIntpayDetails`, `InsertTransSerial`, `InsertDepositInttblTransactionsMaster`, `InserttblOFLoanInt`, `InserttblTransactionsDetails`; tables `tblTransactionsMaster`, `tblOFLoanInt`, `tblTransactionsDetails`. Local SQLite `FinanceMaster` (receipt header), `tblledgers`. |

**Purpose.** For one OF lender: compute the interest the finance owes up to a date (using the "overall penalty" procedure), pay interest and/or repay principal, reopen earlier payments, and print or share the voucher.

**UI.** Identical to the ST interest screen, except the loans header order is No/Date/FIN (the rows stay FIN/No/Date), the grid's last column is "Paid", and the receipt-date default keeps the current time of day.

**Menu / actions.** As in the ST screen; the type-text tap calls `onclickedformark` only for full-access users (checked in the adapter), but marking has no effect on save (see below). PRINT → "Print Options": Direct Print · Show as PDF · Share (no WhatsApp options).

**How it works.**
1. Loading as in ST with `exec OFL_ShowIndOFLStatus @PartyLedgerId= …`, `execute VK_RAWOFLIntSelCustomer @GraceDays= 10,@CustId=…,@IUPDate = '…'`, then ` exec OFL_PrepareIntPriCollList …` immediately followed, inside `if (1 != 0)`, by ` exec OFL_PrepareIntPriCollListOverAllPenalty @isAutoIncEnabled=false, @IntRecptDate='<upto>',@PartyLedId=<party>` — the OverAllPenalty result always replaces the first one. Previous payments: `exec ShowOFLIntList @PartyLedId= …,@CurPF='…' ,@CurPT='…'` (column `int`); reopening: `execute OFLInt_DispIntpayDetails @PartyLedId= …,@SetId=…` (column `Paid`); tapping a previous payment sets Date to its date first.
2. Save (`ASYNCSAVECollectedIntandPriDetails`, input blocked while running): INT → TTID 6, `TargetLedgerId` := 103 (hard-coded interest-expense ledger); Pri → TTID 20 (OFL RP), target = party. New rows: `InsertTransSerial` per loan, `InsertDepositInttblTransactionsMaster`, the master narration update, `{call dbo.InserttblOFLoanInt(…)}` (CurOFLoanAmount; INT as in ST; Pri: PrincipalAmount = Paid **and** OFLoanAmount = basis), then detail lines Sno 1 TargetLedgerId Target **Dr** Paid (against 2), Sno 2 CashLedgerId Source **Cr** Paid (against 1) — cash goes out. Saved rows: `update tblOFLoanInt set TransId=?,…,Narration=? where OFLIntPayId=?` + re-post the detail lines.
3. Print: the same layout and Tamil text as the ST receipt ("வட்டி ரசீது", payer/collector captions; "அசல் வரவு" when CurOFLoanAmount = Paid). Direct Print → A4 portrait, document "ofloaninterestvoucher.pdf"; Show as PDF → `<external storage root>/venusmfc/oflinterestvoucher<dd-MM-yyyy>_<receipt nos>.pdf`, opened with a `file://` URI (`Uri.fromFile`) and flags replaced by 0x40000000 = FLAG_ACTIVITY_NO_HISTORY (JADX shows `BasicMeasure.EXACTLY`); Share → the same file via `file://`, MIME `application/pdf`, chooser "Share Image", toast "Sharing tools have not been installed." if no app.

**Functions.** Same set as the ST screen: `onCreate()`; date / refresh / panel lambdas; `onCreateOptionsMenu()`, `onOptionsItemSelected()`, back-press callback; `onloanselected()`, `onthumnailclicked()`, `onInterestCalculationRowSelected()`, `onclickedformark()`, `onremovebuttonclicked()`, `onprevreciptclicked()`; `ASYNCindividualloandetails`, `ASYNCGetCollectedIntandPriDetails`, `ASYNCSAVECollectedIntandPriDetails`; `onActivityResult()` (rc 8002); `showprintandshareoptions(Context)` (`lambda$onCreate$0`, builds `printpagelist`) + `lambda$showprintandshareoptions$10`; `drawPage()`, `printDocument()` (relies on `printpagelist` built by the dialog), `createPdf()`, `sharePdf()`, `showincompletedataalert()`, `whiteNotificationBar()`, `MyPrintDocumentAdapter`, unused `drawCenter*()`.

**Rules & edge cases.**
- Bug (smali-verified): the save loop's guard is `getSel2Del() || !getSel2Del()` (always true), so the delete branch — which would also run its three-statement delete twice — is unreachable; marked rows are saved/updated like the others and payments cannot be deleted from this screen.
- `onclickedformark` / `onremovebuttonclicked` compare PType with `==` (reference equality), which only matches the tapped row object itself.
- Same issues as ST: saved-row edits do not update `tblTransactionsMaster`; "Pri" vs "PRI" case mismatch; the `FinanceMaster` print dependency; swallowed errors; `get(0)` crash risk when a set is empty; the drawer's "OF Loan Parties" list depends on the local `tblOFLoans` mirror.
- `file://` sharing depends on the StrictMode VM-policy reset in `onCreate`; files are written to world-readable external storage.

## ST interest row editor — `stloan_interestcalculationdetails`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`), title "STLoan Interest Details " |
| **Layout(s)** | `activity_interestcalculationdetails.xml` |
| **Opened from** | `stloaninterestcalculationandpayments` grid row tap — extra `intrestcalculationrow` (`interestcalculationlist_data`), forResult 8002. |
| **Opens** | `spinnersearch_list` "Received in/as" (rc 8001 `interestcalcualationdetails_gettargetledger`) — cash/bank ledgers of the row's finance. |
| **Data** | Local SQLite only: `tblLedgers` (cash/bank list via `fetchsubcollectiontargetledgers`), `qatledgers` (ledger name). Returns resultCode 1 with extra `receivedinterestrow`. |

**Purpose.** Adjust one INT or Pri line before UPDATE: the interest period, the principal basis, the amount actually received, the cash/bank account it goes into, and the narration.

**UI.** Row Type, Finance (short name), Party, Loan No, Loan Amount (current), Interest Paid for (principal basis), Int Rate, S.No (hidden row), Int from, Int upto (date picker), Days, Calc Int, Paid + "Reverse Calculation", Paid as /in (picker), Narration, SAVE. Pri rows hide Interest Paid for, Int Rate, Int from/upto, Days and Calc Int.

**Menu / actions.** Int upto → DatePicker; Paid as /in → ledger picker; Reverse Calculation; SAVE.

**How it works.**
1. Fills the fields from the row (Loan Amount = currentloanamount; Interest Paid for = interestcalculatedloanamount; Paid as /in = name of `CashLedgerId`).
2. Int upto picked → `Days = days(Upto − From) + 1` → `calcint()`.
3. Interest Paid for edited → `PrincipalAmountCollected` := value → `calcint()`.
4. `calcint()`: `CalcInt = PrincipalAmountCollected × IntRate × Days / 100`, then `ceil(|CalcInt| / 5) × 5`; writes Calc Int and Paid.
5. Paid edited: a Pri row with an amount above the current loan → alert "Collected amount cannot be greater then Loan Amount"; otherwise `PrincipalAmountCollected` := amount.
6. Reverse Calculation: `Days = int((Paid + adjustableamount) × 100 / (basis × IntRate))`, `CalcInt = basis × IntRate × Days / 100 − adjustableamount`, `IntUpto = IntFrom + Days − 1`; updates Calc Int, Days and Int upto (Paid unchanged, no ₹5 rounding; `adjustableamount` is never set, so it is 0) — i.e. "how many days of interest does this payment cover".
7. Paid as /in result (rc 8001) → `CashLedgerId` (the cash/bank ledger debited when the caller posts; the credit side is fixed there).
8. SAVE: Received := Paid, Narration; a Pri row with Received above the current loan → "Collected Amount cannot be greater than Loan Amount"; interestcalculatedloanamount := Interest Paid for; returns the row and finishes. Alerts are titled "Error..."; OK refocuses Paid with select-all.

**Functions.** `onCreate()`; `lambda$onCreate$0/1` (Int upto picker), `$2` (ledger picker), `$3` (SAVE), `$4` (reverse calculation); `onActivityResult()` (rc 8001); `calcint()`; `showincompletedataalert()`; `addDays()`; `subtractDays()` (unused).

**Rules & edge cases.**
- Bug: the TextWatcher meant for Int Rate is attached to **Int from** — editing Int from feeds the "dd-MM-yyyy" text to `Double.parseDouble` (NumberFormatException → crash), while editing Int Rate has no effect at all.
- Bug: because `calcint()` writes Paid, the Paid watcher then overwrites `PrincipalAmountCollected` with the interest amount, so a second recalculation in the same visit uses the previous interest as the principal; rows reopened from a saved receipt carry PrincipalAmountCollected = PrincipalAmount (0 for INT lines), so recalculating them yields 0.
- An empty Paid crashes SAVE (`Integer.parseInt("")`); Reverse Calculation divides by zero when the basis or rate is 0.

## OF interest row editor — `ofloan_interestcalculationdetails`
| | |
|---|---|
| **Type** | Activity (`AppCompatActivity`), title "OFLoan Interest Details " |
| **Layout(s)** | `activity_interestcalculationdetails.xml` (shared with the ST editor) |
| **Opened from** | `ofloans_interestcalculationandpayments` grid row tap — extra `intrestcalculationrow` (`ofloans_interestcalculationlist_data`), forResult 8002. |
| **Opens** | `spinnersearch_list` "Paid in/as" (rc 8001) — cash/bank ledgers of the row's finance. |
| **Data** | Local SQLite `tblLedgers`, `qatledgers`; returns resultCode 1 with `receivedinterestrow`. |

**Purpose.** Same as the ST editor, for an OF interest-payment or principal-repayment line.

**UI.** Same layout; Loan Amount = CurOFLoanAmount, Interest Paid for = interestcalculatedfor_ofloanamount; the "Reverse Calculation" button is not wired up (does nothing).

**How it works.** Int upto picker → Days = difference + 1 → `calcint()` (`PrincipalAmount × IntRate × Days / 100`, rounded up to ₹5, written to Calc Int and Paid). The Interest Paid for and Paid watchers: value above CurOFLoanAmount → alert "Paid amount cannot be greater then Loan Amount", else `PrincipalAmount` := value (Interest Paid for also recalculates). Paid in/as → `CashLedgerId` (the cash/bank ledger credited when the caller posts). SAVE: Received := Paid, Narration; Received above CurOFLoanAmount (any row type) → "Collected Amount cannot be greater than Loan Amount"; Interest Paid for (if not blank) → interestcalculatedfor_ofloanamount; setResult(1) and finish.

**Functions.** `onCreate()`; `lambda$onCreate$0/1` (Int upto picker), `$2` (ledger picker), `$3` (SAVE); `onActivityResult()` (rc 8001); `calcint()`; `showincompletedataalert()`.

**Rules & edge cases.** Same Int-from/Int-rate watcher bug and Paid-overwrites-principal effect as the ST editor; an empty Paid crashes SAVE; the loan-amount ceiling is also applied to interest lines.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `individualloansanddepositsstatuslist_adapter` | RecyclerView adapter (Filterable) | `stloans`, `ofloans`, both interest screens | Row `individualloanordepositdetails_row`: No, Fin, Amount, Repaid, Balance, Status, Date (dd-MM-yyyy). Row tap → `ContactsAdapterListener.onloanselected(data,pos)` (`onthumnailclicked` is declared but never fired). Filter keeps rows whose Status contains the query (as typed or upper-cased). |
| `stloansinterestcalculationlist_adapter` | RecyclerView adapter (Filterable) | `stloaninterestcalculationandpayments` | Row `interestcalculation_row`: PType, Loan (CurrentLoanAmount; INT rows show the interest basis), From/Upto (dd-MM-yy), Days and CalcInt (int) for INT rows, Received; remove icon visible only when TransId = 0; yellow (-256) background when `Sel2Del`. Interface `insterestcollectiondetailsrow`: row tap → `onInterestCalculationRowSelected`, remove icon → `onremovebuttonclicked`, type text → `onclickedformark` (+ yellow/white toggle for saved rows when `isFullAccessAllowed`). The filter is a stub (any text → empty list; not wired to the search box). |
| `ofloans_interestcalculationlist_adapter` | RecyclerView adapter (Filterable) | `ofloans_interestcalculationandpayments` | Same row and callbacks for `ofloans_interestcalculationlist_data` (dates dd-MM-yyyy, CalcInt shown as a double, Loan = CurOFLoanAmount or the basis); `onclickedformark` is only called when `isFullAccessAllowed`. Stub filter. |
| `prevreceiptslist_adapter` | RecyclerView adapter (Filterable) | both interest screens | Row `prevreceipts_row`: SNo, Date, Principal, Interest; tap → `PreveReceipts_AdapterListener.onprevreciptclicked`. Stub filter. |
| `interestcalculationlist_data` | Serializable model | ST interest screen, ST editor, ST adapter | PartyLedgerId, STLoanId, FinId, CashLedgerId (cash/bank of the receipt), IntRate, PenaltyRate, PType, SNo, ShortName, LedgerName, FWUDSTLoanId, currentloanamount, interestcalculatedloanamount (interest basis), PrincipalAmountCollected, IntFrom, IntUpto, Days, CalcInt, Received, IsAutoIncEnabled, CutOffDate, Sel2Del (Boolean), TransId, TransDate, STLIntPayId, FWSTLIntId, FWTransId, TargetLedgerId, TGWTId, TGFWTId, SetId, TTypeId, Narration, adjustableamount (never set). PenaltyRate, CutOffDate, IsAutoIncEnabled, TTypeId, FWSTLIntId and FWTransId are never read. |
| `ofloans_interestcalculationlist_data` | Serializable model | OF interest screen, OF editor, OF adapter | Same shape with OFLoanId, FWUDOFLoanId, CurOFLoanAmount, interestcalculatedfor_ofloanamount, PrincipalAmount, OFLIntPayId, FWOFLIntId; `Sel2Del` and `IsAutoIncEnabled` are primitive booleans; no adjustableamount. |
| `stloans_data` | Model | `stloans` | One `tblSTLoans` row: TransId, TransDate, STLoanId, FWSTLoanId, FWUDSTLoanId, PartyLedgerId, STLoanAmount, STLIntRateinDM/MM, STLPri/Sec/TriIntRateinDM, RecPartnerId, RecPartyId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET (String), CurStat, BankRefId, BankRefNo, SourceLedgerId, DCTargetLedgerId. |
| `ofloans_data` | Model | `ofloans` | One `tblOFLoans` row: TransId, TransDate, OFLoanId, FWOFLoanId, FWUDOFLoanId, PartyLedgerId, OFLoanAmount, OFLIntRateinDM/MM/YM, OFLPriIntRateinDM, OFLSecRateinDM, OFLTriRateinDM, RecPartnerId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET (Date), CurStat, BankRefId, BankRefNo, cashorbankledgerid (= SourceLedgerId). The YM/Pri/Sec/Tri rates are unused. |
| `indvidualloanordepositstatus_data` | Model | loan-status lists (all four screens) | PartyLedgerId, TransId, STLoanId (also holds OFLoanId), No (user loan no.), date, Fin, Amount, Repaid, Balance, Status, TGWTId (voucher no.), TGFWTId, selectedSETId (unused). |
| `printpage_data` | Model | interest-receipt printing | pageno, finid, stloanid (ST or OF loan id), partyledgerid, receiptno (TGWTId), loanamount (current loan amount), FWUDId (user loan no.). |
| `prevreceipts_data` | Model | previous-receipts list | SetId, SNo, TransDate, Principal, interest. |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `activity_stloan.xml` | `stloans` | ScrollView: party name and address; "LOANS LIST" header (FIN/No/Date/Amount/Repaid/Balance/Status) + `recycler_view` + add-loan icon + TOTAL; "LOANS DETAILS" form (Finance, Partner Recommendation, ST Loan No, Date, Amount + words, Daily/Monthly Interest Rate, Party Recommendation + remove icon, Document Charges, DC Received As, Interest From, Remarks, Paid From, Bank Transaction Type, Bank Ref No); SUBMIT/PRINT/DELETE; hidden `imgfacebook`; ProgressBar. |
| `activity_ofloan.xml` | `ofloans` | Same as `activity_stloan` minus Party Recommendation and DC Received As; "OF Loan No"; "Received as/in"; PRINT `gone`. |
| `activity_interestcalculationandpayments.xml` | `stloaninterestcalculationandpayments` | Toolbar (party name, two address lines); LOANS DETAILS panel (header + `recycler_view` + PREV RECEIPTS link + TOTAL); PREVIOUS RECEIPTS panel (SNo/Date/Principal/Interest + `previousreceipts_recycler_view` + LOAN DETAILS link); INTEREST COLLECTION (Date, Interest upto, refresh icon, header act/PI/Loan/from/upto/days/int/Received, `interestcalculationandpayments_intcolid`, TOTAL); UPDATE/PRINT; ProgressBar. |
| `activity_ofloans_interestcalculationandpayments.xml` | `ofloans_interestcalculationandpayments` | Same as above; loans header ordered No/Date/FIN/…; grid's last column "Paid". |
| `activity_interestcalculationdetails.xml` | `stloan_interestcalculationdetails`, `ofloan_interestcalculationdetails` | Labelled EditTexts: Row Type, Finance, Party, Loan No, Loan Amount, Interest Paid for, Int Rate, S.No (gone), Int from, Int upto, Days, Calc Int, Paid + "Reverse Calculation", Paid as /in, Narration; SAVE. |
| `individualloanordepositdetails_row.xml` | `individualloansanddepositsstatuslist_adapter` | FIN, No, Date, Amount, Repaid, Balance, Status. |
| `interestcalculation_row.xml` | `stloansinterestcalculationlist_adapter`, `ofloans_interestcalculationlist_adapter` | Remove icon, Type, Loan, From, upto, days, calcint, Received, inside `interestcalculationrow_layoutid`, whose background shows the mark-for-delete state. |
| `prevreceipts_row.xml` | `prevreceiptslist_adapter` | SNo, Date, Pri, Interest. |

---
[← brain.md](../../brain.md) · Module 8.14 of 20 · Previous: [8.13 Accounts transactions, cash/bank book, expenses & incomes, hand loans and ledger statements](13-accounts-transactions-cash-bank-book-expenses.md) · Next: [8.15 Finance transactions lists, party/loan ledgers and loan & deposit analysis reports](15-finance-transactions-lists-party-loan-ledgers.md)
