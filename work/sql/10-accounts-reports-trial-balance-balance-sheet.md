# SQL — 8.10 Accounts reports: trial balance, balance sheet, P&L, month-wise P&L

[← work.md](../../work.md) · [Module spec](../../brain/modules/10-accounts-reports-trial-balance-balance-sheet.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **16 statements** (1 local SQLite, 15 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`accountsreports_parameters`](#accountsreportsparameters) (1) · [`monthlypandl_fragment`](#monthlypandlfragment) (3) · [`tbbspl_list`](#tbbspllist) (11) · [`tbbspl_particulars`](#tbbsplparticulars) (1)

## accountsreports_parameters

Screen: **Accounts report parameters** · [spec](../../brain/modules/10-accounts-reports-trial-balance-balance-sheet.md#accounts-report-parameters--accountsreportsparameters) · 1 statements

#### `fetchfinancelist` · line 277
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select  FinId,FinMailingName  from FinanceDetails
```

- Reads: `FinId`, `FinMailingName`

## monthlypandl_fragment

Screen: **Monthly P&L page** · [spec](../../brain/modules/10-accounts-reports-trial-balance-balance-sheet.md#monthly-pl-page--monthlypandlfragment) · 3 statements

#### `ASYNCGetLedgerStatement.doInBackground` · line 661
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslist`
```sql
exec APP_Transactionslist @FinId=${params[4]},@LedId=${params[0]},@FromDate='${params[1]}',@ToDate='${params[2]}'
```

- Reads: `RowId`, `TransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `TransDate`, `TTId`, `LedgerIdTwo`, `Particulars`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`

#### `ASYNCGetMonthlypandl.doInBackground` · line 253
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `monthlyplincomes`
```sql
exec [MonthlyPLIncomes] @SelectedFinanceIds='${selectecdfinanceids}',  @FDate ='${fromdate}',  @RDate ='${todate}'
```

#### `ASYNCGetMonthlypandl.doInBackground` · line 254
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `ComparedPL`
```sql
select 0 as  SNo,LedgerId as ComponentId,LedgerName as ComponentDesc, case when isnull(sum(CAmount),0) > isnull(sum(DAmount),0)  then isnull(sum(CAmount),0) - isnull(sum(DAmount),0) else 0 end as Cr,case when isnull(sum(DAmount),0) >= isnull(sum(CAmount),0)  then isnull(sum(DAmount),0) - isnull(sum(CAmount),0) else 0 end  as Dr
from ComparedPL
group by LedgerId,LedgerName
```

- Reads: `SNo`, `ComponentId`, `ComponentDesc`, `Cr`, `dr`

## tbbspl_list

Screen: **Balance Sheet / Trial Balance / P&L Statement** · [spec](../../brain/modules/10-accounts-reports-trial-balance-balance-sheet.md#balance-sheet--trial-balance--pl-statement--tbbspllist) · 11 statements

#### `ASYNCGetAccountReportDetails.doInBackground` · line 321
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `bsapp_getbalancesheetcomponentparticulars`
```sql
exec BSAPP_GetBalanceSheetComponentParticulars @ComponentId=2, @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
```

- Reads: `LedgerName`, `LedgerId`, `Cr`, `dr`

#### `ASYNCGetAccountReportDetails.doInBackground` · line 365
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `apppattiproc_forbalancesheet`
```sql
exec APPPattiProc_forBalancesheet    @SelectedFinanceIds='${selectecdfinanceids}', @ReportDate ='${reportdate}'
```

- Reads: `LedgerName`, `LedgerId`, `Balance`, `MCLoanId`, `IsNip`

#### `ASYNCGetAccountReportDetails.doInBackground` · line 419
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `BSComps`
```sql
select ComponentId,ComponentDesc from BSComps where ComponentId in (12,13,20,15,18,19,20)
```

- Reads: `ComponentId`, `ComponentDesc`

#### `ASYNCGetAccountReportDetails.doInBackground` · line 440
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `bsapp_getbalancesheetcomponentparticulars`
```sql
exec BSAPP_GetBalanceSheetComponentParticulars @ComponentId=${bscompslist.get(i).getValuemember()}, @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
```

- Reads: `LedgerName`, `LedgerId`, `Cr`, `dr`

#### `ASYNCGetAccountReportDetails.doInBackground` · line 483
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `appbs_getcashbalance`
```sql
exec APPBS_GetCashBalance @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
```

- Reads: `LedgerName`, `LedgerId`, `Value`

#### `ASYNCGetAccountReportDetails.doInBackground` · line 517
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `appbs_getbankbalance`
```sql
exec APPBS_GetBankBalance @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
```

- Reads: `LedgerName`, `Value`

#### `ASYNCGetAccountReportDetails.doInBackground` · line 555
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `applistplincomes`
```sql
exec APPListPLIncomes @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
```

#### `ASYNCGetAccountReportDetails.doInBackground` · line 556
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `ComparedPL`
```sql
select 0 as  SNo,LedgerId as ComponentId,LedgerName as ComponentDesc, case when isnull(sum(CAmount),0) > isnull(sum(DAmount),0)  then isnull(sum(CAmount),0) - isnull(sum(DAmount),0) else 0 end as Cr,case when isnull(sum(DAmount),0) >= isnull(sum(CAmount),0)  then isnull(sum(DAmount),0) - isnull(sum(CAmount),0) else 0 end  as Dr
from ComparedPL
where CAmount<>0 or DAmount<>0
group by LedgerId,LedgerName
```

- Reads: `SNo`, `ComponentId`, `ComponentDesc`, `Cr`, `dr`

#### `ASYNCGetAccountReportDetails.doInBackground` · line 577
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `apppattiproc_getextracollection`
```sql
exec APPPattiProc_GetExtraCollection    @SelectedFinanceIds='${selectecdfinanceids}', @FromColDate ='${fromdate}', @ToColDate ='${reportdate}'
```

- Reads: `LedgerName`, `ExtraCollection`, `MCLoanId`

#### `ASYNCGetLedgerStatement.doInBackground` · line 754
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslist`
```sql
exec APP_Transactionslist @FinId=${globalvars.finid},@LedId=${params[0]},@FromDate='${params[1]}',@ToDate='${params[2]}'
```

- Reads: `RowId`, `TransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `TransDate`, `TTId`, `LedgerIdTwo`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`

#### `ASYNCGetselectedLedgerAmount.doInBackground` · line 845
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `appbs_getbalancefortheledger`
```sql
exec APPBS_GetBalancefortheLedger @LedgerId=${params[0]}, @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
```

- Reads: `Value`

## tbbspl_particulars

Screen: **Component particulars (drill-down)** · [spec](../../brain/modules/10-accounts-reports-trial-balance-balance-sheet.md#component-particulars-drill-down--tbbsplparticulars) · 1 statements

#### `ASYNCGetLedgerStatement.doInBackground` · line 177
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_transactionslist`
```sql
exec APP_Transactionslist @FinId=${globalvars.finid},@LedId=${params[0]},@FromDate='${params[1]}',@ToDate='${params[2]}'
```

- Reads: `RowId`, `TransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `TransDate`, `TTId`, `LedgerIdTwo`, `Particulars`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`

