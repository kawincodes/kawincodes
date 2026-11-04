# SQL — 8.12 Thittam (collection plan) and weekly analysis reports

[← work.md](../../work.md) · [Module spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **41 statements** (15 local SQLite, 26 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`allfinancedailythittam`](#allfinancedailythittam) (3) · [`simplethittamlist_act`](#simplethittamlistact) (9) · [`singlefinancedailythittam_act`](#singlefinancedailythittamact) (1) · [`varathittam`](#varathittam) (21) · [`weeklycollectionanalysis_act`](#weeklycollectionanalysisact) (3) · [`weeklycollectionpatti_dashboard`](#weeklycollectionpattidashboard) (3) · [`ASYNCGetsingleFinanceCashBook`](#asyncgetsinglefinancecashbook) (1)

## allfinancedailythittam

Screen: **Daily Thittam all finances (orphan)** · [spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md#daily-thittam-all-finances-orphan--allfinancedailythittam) · 3 statements

#### `onCreate` · line 72
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select FinId,FinMailingName,CashLedgerId,AdminMobileNos,LedgerName,LId ,DefaultLoanMethodId
from FinanceDetails
```

- Reads: `FinMailingName`, `LedgerName`, `AdminMobileNos`, `FinId`, `CashLedgerId`, `LId`, `DefaultLoanMethodId`

#### `PlaceholderFragment.ASYNCGetsingleFinanceCashBook.doInBackground` · line 170
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `singlefinancecashbook`
```sql
exec singleFinanceCashBook @IMEI='${globalvars.device_id}',@ReportDate  ='${allfinancedailythittam.fromdate_patti}', @FinId =${allfinancedailythittam.newdata.getFinId()}
```

- Reads: `SNo`, `ShowId`, `TransId`, `FinId`, `LedgerId`, `Ledgername`, `Particulars`, `IsNewLoan`, `TransDate`, `TransType`, `Debit`, `Credit`, `Narration`

#### `SectionsPagerAdapter.getCount` · line 233
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select count(FinId) from FinanceDetails
```

## simplethittamlist_act

Screen: **Simple Thittam (period cash/bank flow)** · [spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md#simple-thittam-period-cashbank-flow--simplethittamlistact) · 9 statements

#### `ASYNCGetAbstractlist.doInBackground` · line 284
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblLedgers`
```sql
select LedgerId,Ledgername,GroupId from tblLedgers where  FinId=${financeid} and GroupId in (5,6)
```

- Reads: `Ledgername`, `LedgerId`, `GroupId`

#### `ASYNCGetAbstractlist.doInBackground` · line 296
**SQL Server** · `jdbc.Statement.execute` · branch-report DB · DELETE · tables: `tblLedgerDetforSimpleThittam`
```sql
delete from tblLedgerDetforSimpleThittam
```

#### `ASYNCGetAbstractlist.doInBackground` · line 300
**SQL Server** · `jdbc.Statement.execute` · branch-report DB · EXEC · procedure: `ledgerwisebalance`
```sql
exec LedgerwiseBalance @CCom =${financeid},@LedId=${((spinnersearch_data) arrayList.get(i)).getValuemember()} ,@FDate='${todate_patti}' ,@TDate='${todate_patti}'
```

#### `ASYNCGetAbstractlist.doInBackground` · line 301
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblledgerDet`
```sql
select sum(debit) as debit ,sum(Credit) as credit from tblledgerDet
```

- Reads: `debit`, `Credit`

#### `ASYNCGetAbstractlist.doInBackground` · line 322
**SQL Server** · `jdbc.Statement.execute` · branch-report DB · EXEC · procedure: `ledgerwisebalance`
```sql
exec LedgerwiseBalance @CCom =${financeid},@LedId=${((spinnersearch_data) arrayList.get(i2)).getValuemember()} ,@FDate='${fromdate_patti}' ,@TDate='${todate_patti}'
```

#### `ASYNCGetAbstractlist.doInBackground` · line 323
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblledgerDet`
```sql
select debit,Credit from tblledgerDet where RowId=1
```

- Reads: `debit`, `Credit`

#### `ASYNCGetAbstractlist.doInBackground` · line 342
**SQL Server** · `jdbc.Statement.execute` · branch-report DB · INSERT · tables: `tblLedgerDet`, `tblLedgerDetforSimpleThittam`
```sql
Insert into tblLedgerDetforSimpleThittam (SourceLedgerId,TransId ,TransDate ,TTId ,Particulars ,Debit ,Credit ,Narration ,LedSNo,LedgerId ) select ${((spinnersearch_data) arrayList.get(i2)).getValuemember()},TransId ,TransDate ,TTId ,Particulars ,Debit ,Credit ,Narration ,LedSNo ,LedgerId
from tblLedgerDet
where RowId>1
```

#### `ASYNCGetAbstractlist.doInBackground` · line 357
**SQL Server** · `jdbc.Statement.execute` · branch-report DB · INSERT · tables: `tblLedgerDetforSimpleThittam`
```sql
Insert into tblLedgerDetforSimpleThittam (SourceLedgerId,TransId ,TransDate ,TTId ,Particulars ,Debit ,Credit ,Narration ,LedSNo,LedgerId ) select  Null,Null ,Null ,Null ,N'முன் இருப்பு' ,0.0d${openingbaldetailslist.get(i3).getCredit()} , 0.0d${simplethittamlist_actVar.openingbaldetailslist.get(i3).getDebit()}   ,Null ,Null ,Null
```

#### `ASYNCGetAbstractlist.doInBackground` · line 361
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `simplethittam`
```sql
exec SimpleThittam @FromDate='${fromdate_patti}',@ToDate='${todate_patti}',@FinId=${financeid}
```

- Reads: `ShowType`, `TypeId`, `TTId`, `Particulars`, `debit`, `credit`, `ledgerid`, `Ledgername`

## singlefinancedailythittam_act

Screen: **Daily Thittam for one line (legacy)** · [spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md#daily-thittam-for-one-line-legacy--singlefinancedailythittamact) · 1 statements

#### `ASYNCGetsingleFinanceCashBook.doInBackground` · line 167
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `singlefinancecashbook`
```sql
exec singleFinanceCashBook @IMEI='${globalvars.device_id}',@ReportDate  ='${fromdate_patti}', @FinId =${financeid}
```

- Reads: `SNo`, `ShowId`, `TransId`, `FinId`, `LedgerId`, `Ledgername`, `Particulars`, `IsNewLoan`, `TransDate`, `TransType`, `Debit`, `Credit`, `Narration`

## varathittam

Screen: **Weekly Thittam sheet (வாரத்திட்டம்)** · [spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md#weekly-thittam-sheet-வரததடடம--varathittam) · 21 statements

#### `drawPage` · line 274
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tempCollWeeks`
```sql
select WeekNo,WDate ,CollAmount,PendingAmount,ColWeekFromDate,ColWeekToDate,RSDate,REDate from tempCollWeeks
```

- Reads: `WDate`, `CollAmount`, `PendingAmount`, `WeekNo`

#### `drawPage` · line 482
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `IncomesoftheWeek`
```sql
select LedgerName,Amount ,LedgerAmount from IncomesoftheWeek
```

- Reads: `LedgerName`, `LedgerAmount`

#### `drawPage` · line 504
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesoftheWeek`
```sql
select LedgerName,Amount ,LedgerAmount from ExpensesoftheWeek
```

#### `printDocument` · line 703
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tempCollWeeks`
```sql
select WeekNo,WDate ,CollAmount,PendingAmount,ColWeekFromDate,ColWeekToDate,RSDate,REDate from tempCollWeeks
```

#### `AsyncWeeklyThittam.doInBackground` · line 739
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `rep24weekcollection`
```sql
exec Rep24WeekCollection @NipLimit=${softsettingsvalue.nipbreakforweeklythittam_14},@FinIdN=${financeid}, @AreaNo=0, @RDate='${thittamdate}',@ColWeekToDate='${thittamdate}',@ColWeekFromDate='${thittamdate}'
```

#### `AsyncWeeklyThittam.doInBackground` · line 741
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `updatecollectedamount`
```sql
exec UpdateCollectedAmount @FinId=${financeid}
```

#### `AsyncWeeklyThittam.doInBackground` · line 743
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempcollweeks`
```sql
select top 1 * from tempcollweeks order by WeekNo desc
```

- Reads: `RSDate`, `REDate`

#### `AsyncWeeklyThittam.doInBackground` · line 759
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `getthittamitems`
```sql
exec GetThittamItems @CurFinId=${financeid}
```

- Reads: `முன்கையிருப்பு`, `கடன்வரவு`, `நிப்புவரவு`, `ஆதாயவரவு`, `மகிமைவரவு`, `எழுத்துக்கூலி`, `புதியகடன்`, `முன்நடப்பு`, `முன்நிப்பு`

#### `AsyncWeeklyThittam.doInBackground` · line 779
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpensesoftheWeek`
```sql
Create table IF NOT EXISTS ExpensesoftheWeek (LedgerName varchar,Amount varchar,LedgerAmount double)
```

#### `AsyncWeeklyThittam.doInBackground` · line 780
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesoftheWeek`
```sql
delete from ExpensesoftheWeek
```

#### `AsyncWeeklyThittam.doInBackground` · line 782
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `showexpensesoftheweek`
```sql
exec ShowExpensesoftheWeek @FinId=${financeid}
```

- Reads: `LedgerName`, `Amount`, `LedgerAmount`

#### `AsyncWeeklyThittam.doInBackground` · line 790
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `ExpensesoftheWeek`
```sql
Insert into ExpensesoftheWeek
values ('${reset.getString("LedgerName")}','${reset.getString("Amount")}',${reset.getDouble("LedgerAmount")})
```

#### `AsyncWeeklyThittam.doInBackground` · line 805
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `IncomesoftheWeek`
```sql
Create table IF NOT EXISTS IncomesoftheWeek (LedgerName varchar,Amount varchar,LedgerAmount double)
```

#### `AsyncWeeklyThittam.doInBackground` · line 806
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `IncomesoftheWeek`
```sql
delete from IncomesoftheWeek
```

#### `AsyncWeeklyThittam.doInBackground` · line 807
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `showincomesoftheweek`
```sql
exec ShowIncomesoftheWeek @FinId=${financeid}
```

- Reads: `LedgerName`, `Amount`

#### `AsyncWeeklyThittam.doInBackground` · line 809
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `IncomesoftheWeek`
```sql
Insert into IncomesoftheWeek
values ('${reset.getString("LedgerName")}','${reset.getString("Amount")}',${reset.getDouble(str3)})
```

#### `AsyncWeeklyThittam.doInBackground` · line 818
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · PREPARENIPLIVESTATUS
```sql
PrepareNIPLiveStatus @NIPCutOffDate='${reset.getDate("REDate")}', @FinId=${financeid}
```

- Reads: `NoOfNIPLoans`, `NoOfLiveLoans`

#### `AsyncWeeklyThittam.doInBackground` · line 827
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempCollWeeks`
```sql
Create table IF NOT EXISTS tempCollWeeks (WeekNo Integer, WDate varchar  ,CollAmount double,PendingAmount double,ColWeekFromDate date,ColWeekToDate date,RSDate date,REDate date )
```

#### `AsyncWeeklyThittam.doInBackground` · line 828
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tempCollWeeks`
```sql
delete from tempCollWeeks
```

#### `AsyncWeeklyThittam.doInBackground` · line 829
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempCollWeeks`
```sql
select WeekNo,convert(varchar,WDate,105) as 'Date' ,CollAmount,PendingAmount,ColWeekFromDate,ColWeekToDate,RSDate,REDate
from tempCollWeeks
```

- Reads: `WeekNo`, `Date`, `ColWeekFromDate`, `ColWeekToDate`, `RSDate`, `CollAmount`, `PendingAmount`

#### `AsyncWeeklyThittam.doInBackground` · line 848
**local SQLite** · `sqlite.execSQL` · ? · tables: `tempCollWeeks`
```sql
${"Insert into tempCollWeeks values (" + i2 + str4 + string}${"',"}${reset.getDouble("CollAmount")},${reset.getDouble("PendingAmount")},'${reset.getString("ColWeekFromDate")}','${reset.getString("ColWeekToDate")}','${reset.getString("RSDate")}','${reset.getString(str2)}')
```

## weeklycollectionanalysis_act

Screen: **Weekly Collection Analysis** · [spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md#weekly-collection-analysis--weeklycollectionanalysisact) · 3 statements

#### `AsyncWeeklyThittam.doInBackground` · line 172
**SQL Server** · `jdbc.Statement.execute` · branch DB · EXEC · procedure: `rep24weekcollection`
```sql
exec Rep24WeekCollection @NipLimit=${softsettingsvalue.nipbreakforweeklythittam_14},@FinIdN=${financeid}, @AreaNo=0, @RDate='${thittamdate}',@ColWeekToDate='${thittamdate}',@ColWeekFromDate='${thittamdate}'
```

#### `AsyncWeeklyThittam.doInBackground` · line 173
**SQL Server** · `jdbc.Statement.execute` · branch DB · EXEC · procedure: `updatecollectedamount`
```sql
exec UpdateCollectedAmount @FinId=${financeid}
```

#### `AsyncWeeklyThittam.doInBackground` · line 174
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tempCollWeeks`
```sql
select * from tempCollWeeks
```

- Reads: `weekno`, `WDate`, `CollAmount`, `PendingAmount`, `REDate`, `RSDate`, `ColWeekFromDate`, `ColWeekToDate`

## weeklycollectionpatti_dashboard

Screen: **Weekly patti dashboard (loader)** · [spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md#weekly-patti-dashboard-loader--weeklycollectionpattidashboard) · 3 statements

#### `asyncthittamstage1.doInBackground` · line 68
**SQL Server** · `jdbc.prepareStatement` · branch-report DB · EXEC · procedure: `weeklyanalysisstage1`
```sql
EXEC WeeklyAnalysisstage1 ?,?,?,?,?
```

- Params: 1 → `receivedweek.getRSDate()` (setDate) · 2 → `receivedweek.getREDate()` (setDate) · 3 → `receivedweek.getRSDate()` (setDate) · 4 → `receivedweek.getColWeekToDate()` (setDate) · 5 → `financeid` (setInt)
- Reads: `ColDateH`, `ColDate`

#### `asyncthittamstage1.doInBackground` · line 94
**SQL Server** · `jdbc.prepareStatement` · branch-report DB · EXEC · procedure: `preparepattiiiweekly`
```sql
EXEC PreparePattiIIWeekly
```

- Reads: `PartyName`, `Subareaname`, `Loan`, `MCLoanId`, `Balance`, `Collected`

#### `asyncthittamstage1.doInBackground` · line 109
**SQL Server** · `jdbc.prepareStatement` · branch-report DB · EXEC · procedure: `weeklypattistageii`
```sql
EXEC WeeklypattiStageII
```

- Reads: `MCLoanId`, `ColDate`, `Paid`

## ASYNCGetsingleFinanceCashBook

[spec](../../brain/modules/12-thittam-collection-plan-weekly-analysis-reports.md) · 1 statements

#### `doInBackground` · line 24
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `singlefinancecashbook`
```sql
exec singleFinanceCashBook @IMEI='${globalvars.device_id}',@ReportDate  ='${financeids[1]}', @FinId =${financeids[0]}
```

- Reads: `SNo`, `ShowId`, `TransId`, `FinId`, `LedgerId`, `Ledgername`, `Particulars`, `IsNewLoan`, `TransDate`, `TransType`, `Debit`, `Credit`, `Narration`

