# SQL — 8.13 Accounts transactions, cash/bank book, expenses & incomes, hand loans and ledger statements

[← work.md](../../work.md) · [Module spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **67 statements** (30 local SQLite, 37 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`accledgers_searchlist`](#accledgerssearchlist) (1) · [`accountledgerdetails`](#accountledgerdetails) (12) · [`accounttrans_list`](#accounttranslist) (4) · [`accounttransactions_act`](#accounttransactionsact) (11) · [`cashbook`](#cashbook) (26) · [`cashbookabstract`](#cashbookabstract) (1) · [`cashreceiveandreturns`](#cashreceiveandreturns) (2) · [`expensesandreceipt_offlineentry`](#expensesandreceiptofflineentry) (5) · [`expensesentry_searchlist`](#expensesentrysearchlist) (1) · [`handloans_list`](#handloanslist) (2) · [`ledgerstatement_act`](#ledgerstatementact) (1) · [`ledgerstatmentsdateselection`](#ledgerstatmentsdateselection) (1)

## accledgers_searchlist

Screen: **Account ledgers list** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#account-ledgers-list--accledgerssearchlist) · 1 statements

#### `ASYNCAccountsLedgers.doInBackground` · line 255
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `PattiExpLedgers`, `tblLedgers`
```sql
select a.LedgerId,LedgerName,a.GroupId,ScreenType,FinId,IsDefault,isReserved,case when c.LedgerId is null then 0 else 1 end as isPattiExpenses
from tblLedgers a
left
join PattiExpLedgers c on a.LedgerId =c.LedgerId
where a.LedgerId>200 and Screentype=5
order by LedgerName
```

- Reads: `LedgerName`, `LedgerId`, `GroupId`, `FinId`, `isPattiExpenses`

## accountledgerdetails

Screen: **Account ledger create / edit / delete** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#account-ledger-create--edit--delete--accountledgerdetails) · 12 statements

#### `ASYNCupdateemployee.doInBackground` · line 245
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertaccledgersapp`
```sql
{call dbo.InsertAccLedgersAPP(?,?,?,?,?,?,?)}
```

- Params: "LedgerName" → `accledgerselected.getLedgerName()` (setString) · "GroupId" → `accledgerselected.getGroupId()` (setDouble) · "ScreenType" → `5` (setInt) · "IsDefault" → `0.0d` (setDouble) · "isReserved" → `0.0d` (setDouble) · "FinId" → `accledgerselected.getFinid()` (setInt) · "FinId" → `8` (setNull)
- OUT: "LedgerId" (8)
- Reads: `LedgerId`

#### `ASYNCupdateemployee.doInBackground` · line 263, 308
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `PattiExpLedgers`
```sql
insert into PattiExpLedgers values(?,?)
```

- Params: 1 → `accledgerselected.getLedgerId()` (setDouble) · 2 → `1` (setInt)

#### `ASYNCupdateemployee.doInBackground` · line 275, 289
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ExpHeaders`
```sql
INSERT INTO ExpHeaders (LedgerId, LedgerName, TTId, TTId)
VALUES (${Integer.valueOf(accledgerselected.getLedgerId())}, ${accledgerselected.getLedgerName()}, ${(Integer) 7}, ${(Integer) 8})
```

#### `ASYNCupdateemployee.doInBackground` · line 277
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `PattiIncomeLedgers`
```sql
insert into PattiIncomeLedgers values(?,?)
```

- Params: 1 → `accledgerselected.getLedgerId()` (setDouble) · 2 → `1` (setInt)

#### `ASYNCupdateemployee.doInBackground` · line 294
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblLedgers`
```sql
update tblLedgers set LedgerName=?,GroupId=?,Finid=? where ledgerid=?
```

- Params: 1 → `accledgerselected.getLedgerName()` (setString) · 2 → `accledgerselected.getGroupId()` (setDouble) · 3 → `accledgerselected.getFinid()` (setDouble) · 3 → `8` (setNull) · 4 → `accledgerselected.getLedgerId()` (setInt)

#### `ASYNCupdateemployee.doInBackground` · line 306, 332
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · DELETE · tables: `PattiExpLedgers`
```sql
delete from  PattiExpLedgers where LedgerId=${accledgerselected.getLedgerId()}
```

#### `ASYNCupdateemployee.doInBackground` · line 321
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `ExpHeaders`
```sql
UPDATE ExpHeaders SET LedgerId=${Integer.valueOf(accledgerselected.getLedgerId())}, LedgerName=${accledgerselected.getLedgerName()}, TTId=${(Integer) 7}, TTId=${(Integer) 8}
WHERE LedgerId=${accledgerselected.getLedgerId()}
```

#### `ASYNCupdateemployee.doInBackground` · line 325
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers where LedgerId=?
```

- Params: 1 → `accledgerselected.getLedgerId()` (setDouble)

#### `ASYNCupdateemployee.doInBackground` · line 330
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `ExpHeaders`
```sql
DELETE FROM ExpHeaders WHERE LedgerId=${accledgerselected.getLedgerId()}
```

## accounttrans_list

Screen: **Accounts transaction list** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#accounts-transaction-list--accounttranslist) · 4 statements

#### `ASYNCGetAccountstransactionslist.doInBackground` · line 389
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslistforselectedline`
```sql
exec APP_TransactionslistforSelectedLine @FromDate='${curdaterange.getFromdate()}', @ToDate='${curdaterange.getTodate()}', @TTId=${ttid} , @FinId=${globalvars.curfin.FinId}
```

#### `ASYNCGetAccountstransactionslist.doInBackground` · line 393
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslistforselectedline`
```sql
exec APP_TransactionslistforSelectedLine @FromDate='${new Date(cal.getTimeInMillis())}', @ToDate='${new Date(cal.getTimeInMillis())}', @TTId=${ttid} , @FinId=${globalvars.curfin.FinId}
```

#### `ASYNCGetAccountstransactionslist.doInBackground` · line 396
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslistnew`
```sql
exec APP_TransactionslistNew @FromDate='${curdaterange.getFromdate()}', @ToDate='${curdaterange.getTodate()}', @TTId=${ttid}
```

#### `ASYNCGetAccountstransactionslist.doInBackground` · line 400
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslistnew`
```sql
exec APP_TransactionslistNew @FromDate='${new Date(cal2.getTimeInMillis())}', @ToDate='${new Date(cal2.getTimeInMillis())}', @TTId=${ttid}
```

- Reads: `TransId`, `FinId`, `TransDate`, `TransType`, `Narration`, `Particulars`, `TransAmount`, `SourceLedgerId`, `TransLedgerId`, `FWVWUDTransId`

## accounttransactions_act

Screen: **Accounts voucher entry (Payment / Receipt / Contra / Journal / Capital / Hand loan / Salary advance)** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#accounts-voucher-entry-payment--receipt--contra--journal--capital--hand-loan--salary-advance--accounttransactionsact) · 11 statements

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 636
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `qatledgers`
```sql
Create table IF NOT EXISTS qatledgers(FinId int, TTId int,AType int,LedgerId Double,LedgerName String,isExecutiveLevelExpensesLedger bit,isExecutiveLevelReceiptsLedger bit)
```

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 637
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `qatledgers`
```sql
delete from qatledgers
```

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 638
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `get_ledgersforquicktransactions`
```sql
exec Get_LedgersforQuickTransactions
```

- Reads: `TTId`, `FinId`, `AType`, `LedgerId`, `LedgerName`, `isExecutiveLevelExpensesLedger`, `isExecutiveLevelReceiptsLedger`

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 648
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `qatledgers`
```sql
INSERT INTO qatledgers (TTId, FinId, AType, LedgerId, LedgerName, isExecutiveLevelExpensesLedger, isExecutiveLevelReceiptsLedger)
VALUES (${Integer.valueOf(reset.getInt("TTId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt("AType"))}, ${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Boolean.valueOf(reset.getBoolean("isExecutiveLevelExpensesLedger"))}, ${Boolean.valueOf(reset.getBoolean("isExecutiveLevelReceiptsLedger"))})
```

#### `ASYNCTransactions.doInBackground` · line 720
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · DELETE · tables: `tbltransactionsdetails`, `tbltransactionsmaster`
```sql
delete
from tbltransactionsdetails
where transid=${curtransaction.getTransId()}; delete
from tbltransactionsmaster
where transid=${curtransaction.getTransId()}
```

#### `ASYNCTransactions.doInBackground` · line 724
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertimageuploads`
```sql
{call dbo.InsertImageUploads(?,?)}
```

- Params: "Dummy" → `"Dummy"` (setString)
- OUT: "IUId" (4)
- Reads: `IUId`

#### `ASYNCTransactions.doInBackground` · line 737
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertappacctransactionsmaster`
```sql
{call dbo.InsertAPPAccTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `curtransaction.getTransDate()` (setDate) · "TransType" → `ttid` (setInt) · "Narration" → `curtransaction.getNarration()` (setString) · "FinId" → `curtransaction.getFinid()` (setDouble) · "TransAmount" → `curtransaction.getTransAmount()` (setDouble) · "TransLedgerId" → `curtransaction.getLedgerIdTwo()` (setDouble) · "TransLedgerId" → `curtransaction.getLedgerIdOne()` (setDouble) · "SourceLedgerId" → `curtransaction.getLedgerIdTwo()` (setDouble) · "SourceLedgerId" → `curtransaction.getLedgerIdOne()` (setDouble) · "ImageFileName" → `4` (setNull)
- OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
- Reads: `TransId`, `FWVWUDTransId`

#### `ASYNCTransactions.doInBackground` · line 761
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tbltransactionsmaster`
```sql
Update tbltransactionsmaster set  TransDate=?,TransType=?,Narration=?,FinId=?,TransAmount=?,TransLedgerId=?,SourceLedgerId=?,ImageFileName=?
Where transid=?
```

- Params: 1 → `curtransaction.getTransDate()` (setDate) · 2 → `ttid` (setInt) · 3 → `curtransaction.getNarration()` (setString) · 4 → `curtransaction.getFinid()` (setDouble) · 5 → `curtransaction.getTransAmount()` (setDouble) · 6 → `accounttransactions_actVar2.curtransaction.getLedgerIdOne()` (setDouble) · 6 → `accounttransactions_actVar2.curtransaction.getLedgerIdTwo()` (setDouble) · 7 → `accounttransactions_actVar3.curtransaction.getLedgerIdTwo()` (setDouble) · 7 → `accounttransactions_actVar3.curtransaction.getLedgerIdOne()` (setDouble) · 8 → `transactionimagefilename` (setString) · 9 → `curtransaction.getTransId()` (setDouble)

#### `ASYNCTransactions.doInBackground` · line 785
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · DELETE · tables: `tbltransactionsdetails`
```sql
delete from tbltransactionsdetails where transid=${curtransaction.getTransId()}
```

#### `ASYNCTransactions.doInBackground` · line 787, 803
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `tbltransactionsdetails`
```sql
insert into tbltransactionsdetails values(?,?,?,?,?,?,?)
```

- Params: 1 → `curtransaction.getTransId()` (setDouble) · 2 → `1.0d` (setDouble) · 3 → `accounttransactions_actVar4.curtransaction.getLedgerIdOne()` (setDouble) · 4 → `"Source"` (setString) · 3 → `accounttransactions_actVar4.curtransaction.getLedgerIdTwo()` (setDouble) · 4 → `"Source"` (setString) · 5 → `curtransaction.getTransAmount()` (setDouble) · 6 → `"Cr"` (setString) · 7 → `2.0d` (setDouble)

## cashbook

Screen: **Day cash / bank book with verification** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#day-cash--bank-book-with-verification--cashbook) · 26 statements

#### `DisplayAbstractCashBook` · line 315
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CashbookAbstract`
```sql
select SNo,Particulars,debit,credit from CashbookAbstract
```

- Reads: `Particulars`, `debit`, `credit`

#### `DisplayDetailedCashBook` · line 330
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Cashbook`
```sql
select SNo,Particulars,debit,credit from Cashbook
```

- Reads: `Particulars`, `debit`, `credit`

#### `createPdf` · line 367
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Cashbook`
```sql
select SNo,Particulars,debit,credit from Cashbook
```

#### `printDocument` · line 504
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Cashbook`
```sql
select SNo,Particulars,Debit,Credit from Cashbook
```

#### `AsyncCallCashBook.doInBackground` · line 623
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CashBook`
```sql
Create table IF NOT EXISTS CashBook (SNo int,Particulars varchar,debit int,credit int)
```

#### `AsyncCallCashBook.doInBackground` · line 624
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CashBook`
```sql
delete from CashBook
```

#### `AsyncCallCashBook.doInBackground` · line 629
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `curdaycashbook`
```sql
exec CurDayCashBook @IMEI='${globalvars.device_id}', @CashLedgerId=${selectedledgerid} ,@FinId =${globalvars.finid},  @CurDate='${cashbookdate}'
```

#### `AsyncCallCashBook.doInBackground` · line 631
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `curdaycompainedcashbook`
```sql
exec CurDayCompainedCashBook @IMEI='${globalvars.device_id}', @FinId =${globalvars.finid},  @CurDate='${cashbookdate}'
```

#### `AsyncCallCashBook.doInBackground` · line 639
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `CashBook`
```sql
INSERT INTO CashBook (SNo, Particulars, Debit, Credit)
VALUES (${Integer.valueOf(reset.getInt("SNo"))}, ${reset.getString("Particulars")}, ${Integer.valueOf(reset.getInt("Debit"))}, ${Integer.valueOf(reset.getInt("Credit"))})
```

#### `AsyncCallCashBook.doInBackground` · line 641
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · UPDATE · tables: `TempDailyCashBook`
```sql
update TempDailyCashBook set Particulars =N'${getString(R.string.newloans)}'
where TTId=21 and LedSNo=2; Update TempDailyCashBook set Debit=Null
where Debit=0;Update TempDailyCashBook set Credit=Null
where Credit=0
```

#### `AsyncCallCashBook.doInBackground` · line 643
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CashBookAbstract`
```sql
Create table IF NOT EXISTS CashBookAbstract (SNo int,Particulars varchar,debit int,credit int)
```

#### `AsyncCallCashBook.doInBackground` · line 644
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CashBookAbstract`
```sql
delete from CashBookAbstract
```

#### `AsyncCallCashBook.doInBackground` · line 645
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `TempDailyCashBook`
```sql
select  Row_Number() over (order by max(rowid)) as SNo, Particulars ,SUM(Debit) as Debit ,SUM(Credit)as Credit
from TempDailyCashBook
where Particulars is not null
group by Particulars
order by MAX(rowId)
```

- Reads: `SNo`, `Particulars`, `Debit`, `Credit`

#### `AsyncCallCashBook.doInBackground` · line 652
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `CashBookAbstract`
```sql
INSERT INTO CashBookAbstract (SNo, Particulars, Debit, Credit)
VALUES (${Integer.valueOf(reset.getInt("SNo"))}, ${reset.getString("Particulars")}, ${Integer.valueOf(reset.getInt("Debit"))}, ${Integer.valueOf(reset.getInt("Credit"))})
```

#### `AsyncCallCashBook.doInBackground` · line 655
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `CashBookVerification`
```sql
select convert(bit,count(*))  as isverifybuttondisabled
from CashBookVerification
where FinId=${globalvars.finid} and  Ledgerid=${selectedledgerid} and VerfiedDate='${cashbookdate}'
```

- Reads: `isverifybuttondisabled`

#### `MyPrintDocumentAdapter.onStart` · line 535
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Cashbook`
```sql
select SNo,Particulars,Debit,Credit from Cashbook
```

#### `MyPrintDocumentAdapter.onWrite` · line 566
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Cashbook`
```sql
select SNo,Particulars,Debit,Credit from Cashbook
```

#### `asyncCANCELcashbookverification.doInBackground` · line 808
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `CASHBookVerification`, `DownloadControling`, `UploadControling`
```sql
${"delete from CASHBookVerification where FinId= " + globalvars.finid + " and VerfiedDate='…}${"'; "}update DownloadControling set isAllowed='True'
where FinId= ${globalvars.finid} and TransDate='${cashbookdate}'; update UploadControling set isAllowed='True'
where FinId= ${globalvars.finid} and TransDate='${cashbookdate}';
```

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 730
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · UPDATE · tables: `DownloadControling`, `UploadControling`
```sql
update DownloadControling set isAllowed='False'
where FinId= ${globalvars.finid} and TransDate='${cashbookdate}'; update UploadControling set isAllowed='False'
where FinId= ${globalvars.finid} and TransDate='${cashbookdate}';
```

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 731
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertcashbookverification`
```sql
{call dbo.InsertCashBookVerification(?,?,?,?,?,?)}
```

- Params: "FinId" → `globalvars.finid.intValue()` (setInt) · "VerfiedDate" → `cashbookdate.toString()` (setString) · "verifiedby" → `globalvars.APPUserName` (setString) · "ledgerid" → `selectedledgerid` (setInt)
- OUT: "CBVId" (8) · "VerifiedDateTime" (91)

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 741
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `checkisallcashandbankbooksverified`
```sql
exec checkisallcashandbankbooksverified @FinId=${globalvars.finid} , @VerfiedDate='${cashbookdate}'
```

- Reads: `UnverifiedCount`

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 746
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 747
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 748
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 749
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

#### `asynccashbookverificationand_blockdownloauploadaccess.doInBackground` · line 750
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

## cashbookabstract

Screen: **Cash / bank balance abstract** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#cash--bank-balance-abstract--cashbookabstract) · 1 statements

#### `ASYNCGetCashAbstract.doInBackground` · line 160
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `preparedashboardcash`
```sql
exec PrepareDashBoardCash
```

- Reads: `GroupId`, `Grouping`, `LedgerId`, `FinId`, `Ledger`, `Balance`

## cashreceiveandreturns

Screen: **Executive cash received from / returned to office** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#executive-cash-received-from--returned-to-office--cashreceiveandreturns) · 2 statements

#### `ASYNCReceiptsandReturns.doInBackground` · line 108
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertcontratbltransactionsmaster`
```sql
{call dbo.InsertContratblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransType" → `9.0d` (setDouble) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setInt) · "TransAmount" → `receivedamount` (setDouble) · "FWTransId" → `8` (setNull) · "FWVWUDTransId" → `8` (setNull) · "GColId" → `0.0d` (setDouble) · "TransLedgerId" → `targetledgerid.intValue()` (setDouble) · "SourceLedgerId" → `sourceledgerid.intValue()` (setDouble) · "RouteId" → `globalvars.finid.intValue()` (setDouble)
- OUT: "TransId" (8) · "FWVWTransId" (8)
- Reads: `TransId`

#### `ASYNCReceiptsandReturns.doInBackground` · line 123
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · INSERT · tables: `tblTransactionsDetails`
```sql
insert into tblTransactionsDetails  Select ${cstmt.getDouble("TransId")} ,1 as SNo,${targetledgerid},'Target',${receivedamount},'Dr',2
union all  Select ${cstmt.getDouble("TransId")},2 as SNo,${sourceledgerid},'Source',${receivedamount} ,'Cr',1
```

## expensesandreceipt_offlineentry

Screen: **Offline expense / receipt entry** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#offline-expense--receipt-entry--expensesandreceiptofflineentry) · 5 statements

#### `deleteexpenses` · line 212
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpensesEntries`
```sql
Create table IF NOT EXISTS ExpensesEntries(ID INTEGER PRIMARY KEY   AUTOINCREMENT,FinId Double,LedgerId Double,Amount Double,ServerGenTransId Double,TTId int,SourceLedgerId int,Narration varchar )
```

#### `deleteexpenses` · line 213
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
Delete from  ExpensesEntries where ID=${receivedexpensesentry.getID()}
```

#### `saveexpenses` · line 170
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpensesEntries`
```sql
Create table IF NOT EXISTS ExpensesEntries(ID INTEGER PRIMARY KEY   AUTOINCREMENT,FinId Double,LedgerId Double,Amount Double,ServerGenTransId Double,TTId int,SourceLedgerId int,Narration varchar )
```

#### `saveexpenses` · line 180
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ExpensesEntries`
```sql
INSERT INTO ExpensesEntries (FinId, LedgerId, Amount, ServerGenTransId, TTId, Narration, SourceLedgerId)
VALUES (${Integer.valueOf(receivedexpensesentry.getFinId())}, ${Integer.valueOf(receivedexpensesentry.getLedgerId())}, ${Double.valueOf(receivedexpensesentry.getAmount())}, ${(Integer) 0}, ${Integer.valueOf(receivedexpensesentry.getTTId())}, ${receivedexpensesentry.getNarration()}, ${Integer.valueOf(receivedexpensesentry.getSourceledgerid())})
```

#### `saveexpenses` · line 195
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `ExpensesEntries`
```sql
UPDATE ExpensesEntries SET Amount=${Double.valueOf(receivedexpensesentry.getAmount())}, Narration=${receivedexpensesentry.getNarration()}, LedgerId=${Integer.valueOf(receivedexpensesentry.getLedgerId())}
WHERE ID=${receivedexpensesentry.getID()}
```

## expensesentry_searchlist

Screen: **Offline expenses / incomes list** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#offline-expenses--incomes-list--expensesentrysearchlist) · 1 statements

#### `fetchContacts` · line 113
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesEntries`
```sql
Select ID,FinId,LedgerId,Amount,ServerGenTransId,TTId,Narration,SourceLedgerId
from ExpensesEntries
where TTId=${ttid}
order by ID
```

- Reads: `ID`, `LedgerId`, `ServerGenTransId`, `TTId`, `Amount`, `Narration`, `SourceLedgerId`

## handloans_list

Screen: **Hand loans (kaimaathu) report** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#hand-loans-kaimaathu-report--handloanslist) · 2 statements

#### `ASYNCGetLedgerStatement.doInBackground` · line 786
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslistforhandloanshistory`
```sql
exec APP_Transactionslistforhandloanshistory @LedId=${passedhandloanforentry.getLedgerId()}
```

- Reads: `RowId`, `TransId`, `FWVWUDTransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `Narration`, `TransDate`, `TTId`, `LedgerIdTwo`, `Particulars`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`, `FinId`

#### `ASYNC_GetHandloandetails.doInBackground` · line 235
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `gethandloansdetails`
```sql
exec GetHandLoansDetails
```

- Reads: `FinId`, `LedgerId`, `LedgerName`, `Receivable`, `Payable`

## ledgerstatement_act

Screen: **Ledger statement** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#ledger-statement--ledgerstatementact) · 1 statements

#### `ASYNCGetLedgerStatement.doInBackground` · line 335
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslist`
```sql
exec APP_Transactionslist @FinId=${reportfinid},@LedId=${ledgeridpassed},@FromDate='${curdaterange.getFromdate()}',@ToDate='${curdaterange.getTodate()}'
```

- Reads: `RowId`, `TransId`, `LedgerIdOne`, `LedgerStatusOne`, `TransDate`, `Narration`, `TTId`, `LedgerIdTwo`, `Particulars`, `ImageFileName`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`

## ledgerstatmentsdateselection

Screen: **Ledger and period selection** · [spec](../../brain/modules/13-accounts-transactions-cash-bank-book-expenses.md#ledger-and-period-selection--ledgerstatmentsdateselection) · 1 statements

#### `ASYNCGetLedgerslist.doInBackground` · line 194
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblledgers`, `tblTransactionsMaster`
```sql
select LedgerId,LedgerName
From tblledgers
where (GroupId not  in (1) or LedgerId=114) and  LedgerId in (select LedgerId
from tblTransactionsMaster x,tbltransactionsDetails y
where x.TransId=y.TransId and FinId=${globalvars.finid} )
order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

