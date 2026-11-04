# SQL Server stored procedures — every call site

[← work.md](../../work.md)

**123 procedures** called from the app. For each call: the call text with its parameters, the bound values, OUT parameters and columns read back. The procedure bodies live only on the server and are not in the APK.

## `app_get7dayscollectionfordailyloans`

- [`selectbranchandfinance.ASYNCGet7DaysCollection.doInBackground:804`](02-navigation-hub-routers-line-branch-selection.md#selectbranchandfinance) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec [APP_Get7daysCollectionfordailyloans] @EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
  ```
  Reads: `UdLedgerId`, `CWLedgerId`, `MCLoanId`, `FWUDMCLoanId`, `FinId`, `PartyLedgerId`, `SubAreaNo`, `LedgerName`, `MCLoanDate`, `LoanStatus`, `ReportIdentification`, `Tag`, `IsAttentionRequired`

## `app_get7dayscollectionformonthlyloans`

- [`selectbranchandfinance.ASYNCGet7DaysCollection.doInBackground:904`](02-navigation-hub-routers-line-branch-selection.md#selectbranchandfinance) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec [APP_Get7daysCollectionforMonthlyloans] @EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
  ```
  Reads: `UdLedgerId`, `MCLoanId`, `FWUDMCLoanId`, `PartyLedgerId`, `LedgerName`, `MCLoanDate`, `LoanStatus`, `Tag`, `ReportIdentification`, `IsAttentionRequired`

## `app_get7dayscollectionforweeklyloans`

- [`selectbranchandfinance.ASYNCGet7DaysCollection.doInBackground:854`](02-navigation-hub-routers-line-branch-selection.md#selectbranchandfinance) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec [APP_Get7daysCollectionforweeklyloans] @EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
  ```

## `app_get7dayscollectionforweeklyloanswithweekdayid`

- [`selectbranchandfinance.ASYNCGet7DaysCollection.doInBackground:856`](02-navigation-hub-routers-line-branch-selection.md#selectbranchandfinance) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec [APP_Get7daysCollectionforweeklyloanswithWeekdayid] @WeekdayId=${WeekDayId} ,@EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
  ```

## `app_gettodaycollections`

- [`todaycollectionlist.ASYNCTodayCollection.doInBackground:595`](06-collection-reports-collection-day-abstract-7.md#todaycollectionlist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_GetTodayCollections @IMEI='${globalvars.device_id}',@FinId=${globalvars.finid}
  ```
  Reads: `MCLoanId`, `FWUDMCLoanid`, `CWLedgerId`, `PartyLedgerId`, `LDate`, `PartyName`, `Tag`, `SubAreaName`, `SubAreaNo`, `LAmount`, `Collected`, `cashCollected`, `BankCollected`, `ShowId`, `UdLedgerId`

## `app_mcloancollectiondataautoloanclosoure_withoutweekdayssupport`

- [`collectionlistprints.AsyncDownloaddata.doInBackground:6417`](18-printing-collection-list-prints-receipts-bluetooth.md#collectionlistprints) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport ?,?,?
  ```
  Params: 1 → `Date.valueOf(collectionperiod.get(collectionperiod.size() - 1).toString())` · 3 → `Date.valueOf(collectionperiod.get(0).minusDays(1L).toString())` · 1 → `Date.valueOf(reportdate.toString())` · 3 → `Date.valueOf(reportdate.toString())` · 2 → `globalvars.curfin.getFinId().intValue()`
  Reads: `UDLedgerId`, `ShowId`, `SubAreaName`, `CWLedgerId`, `PartyName`, `Tag`, `MCLoanAmountCollectable`, `MobileNo1`, `MCLoanId`, `MCLoanDate`, `InsAmount`, `DuesCount`, `PrevCollected`, `CollectionBreakString`

## `app_mcloancollectiondataautoloanclosoure_withweekdayssupport`

- [`collectionlistprints.AsyncDownloaddata.doInBackground:6413`](18-printing-collection-list-prints-receipts-bluetooth.md#collectionlistprints) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport ?,?,?
  ```

## `app_mcloancollectiondatawithloanclosouremonitering_withoutweekdayssupport`

- [`collectionlistprints.AsyncDownloaddata.doInBackground:6415`](18-printing-collection-list-prints-receipts-bluetooth.md#collectionlistprints) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport ?,?,?
  ```

## `app_mcloancollectiondatawithloanclosouremonitering_withweekdayssupport`

- [`collectionlistprints.AsyncDownloaddata.doInBackground:6411`](18-printing-collection-list-prints-receipts-bluetooth.md#collectionlistprints) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport ?,?,?
  ```

## `app_pendingreportautoloanclosoure_withoutweekdayssupport`

- [`daywisependingreport_act.ASYNCGetNewpartiesNewLoans.doInBackground:189`](06-collection-reports-collection-day-abstract-7.md#daywisependingreportact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec APP_PendingReportAUTOLOANCLOSOURE_WithOUTWeekdaysSupport   @ColDate='${todate}',  @FinId=${financeid}
  ```
  Reads: `MCLoanId`, `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `ShowId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `PartyName`, `MobileNo1`, `MobileNo2`, `Imagefilename`, `SubAreaNo`, `SubAreaName`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PrevCollected`, `Balance`, `NoofCollections`, `LastCollDate`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `ReportIdentification`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`, `LoanStatusType2`, `InterestAmount`

## `app_transactionslist`

- [`ledgerstatement_act.ASYNCGetLedgerStatement.doInBackground:335`](13-accounts-transactions-cash-bank-book-expenses.md#ledgerstatementact) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_Transactionslist @FinId=${reportfinid},@LedId=${ledgeridpassed},@FromDate='${curdaterange.getFromdate()}',@ToDate='${curdaterange.getTodate()}'
  ```
  Reads: `RowId`, `TransId`, `LedgerIdOne`, `LedgerStatusOne`, `TransDate`, `Narration`, `TTId`, `LedgerIdTwo`, `Particulars`, `ImageFileName`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`
- [`monthlypandl_fragment.ASYNCGetLedgerStatement.doInBackground:661`](10-accounts-reports-trial-balance-balance-sheet.md#monthlypandlfragment) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_Transactionslist @FinId=${params[4]},@LedId=${params[0]},@FromDate='${params[1]}',@ToDate='${params[2]}'
  ```
  Reads: `RowId`, `TransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `TransDate`, `TTId`, `LedgerIdTwo`, `Particulars`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`
- [`tbbspl_list.ASYNCGetLedgerStatement.doInBackground:754`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_Transactionslist @FinId=${globalvars.finid},@LedId=${params[0]},@FromDate='${params[1]}',@ToDate='${params[2]}'
  ```
  Reads: `RowId`, `TransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `TransDate`, `TTId`, `LedgerIdTwo`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`
- [`tbbspl_particulars.ASYNCGetLedgerStatement.doInBackground:177`](10-accounts-reports-trial-balance-balance-sheet.md#tbbsplparticulars) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_Transactionslist @FinId=${globalvars.finid},@LedId=${params[0]},@FromDate='${params[1]}',@ToDate='${params[2]}'
  ```
  Reads: `RowId`, `TransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `TransDate`, `TTId`, `LedgerIdTwo`, `Particulars`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`

## `app_transactionslistforhandloanshistory`

- [`handloans_list.ASYNCGetLedgerStatement.doInBackground:786`](13-accounts-transactions-cash-bank-book-expenses.md#handloanslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_Transactionslistforhandloanshistory @LedId=${passedhandloanforentry.getLedgerId()}
  ```
  Reads: `RowId`, `TransId`, `FWVWUDTransId`, `LedgerIdOne`, `ImageFileName`, `LedgerStatusOne`, `Narration`, `TransDate`, `TTId`, `LedgerIdTwo`, `Particulars`, `LedgerStatusTwo`, `TransAmount`, `Debit`, `Credit`, `FinId`

## `app_transactionslistforselectedline`

- [`accounttrans_list.ASYNCGetAccountstransactionslist.doInBackground:389`](13-accounts-transactions-cash-bank-book-expenses.md#accounttranslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_TransactionslistforSelectedLine @FromDate='${curdaterange.getFromdate()}', @ToDate='${curdaterange.getTodate()}', @TTId=${ttid} , @FinId=${globalvars.curfin.FinId}
  ```
- [`accounttrans_list.ASYNCGetAccountstransactionslist.doInBackground:393`](13-accounts-transactions-cash-bank-book-expenses.md#accounttranslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_TransactionslistforSelectedLine @FromDate='${new Date(cal.getTimeInMillis())}', @ToDate='${new Date(cal.getTimeInMillis())}', @TTId=${ttid} , @FinId=${globalvars.curfin.FinId}
  ```

## `app_transactionslistnew`

- [`accounttrans_list.ASYNCGetAccountstransactionslist.doInBackground:396`](13-accounts-transactions-cash-bank-book-expenses.md#accounttranslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_TransactionslistNew @FromDate='${curdaterange.getFromdate()}', @ToDate='${curdaterange.getTodate()}', @TTId=${ttid}
  ```
- [`accounttrans_list.ASYNCGetAccountstransactionslist.doInBackground:400`](13-accounts-transactions-cash-bank-book-expenses.md#accounttranslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_TransactionslistNew @FromDate='${new Date(cal2.getTimeInMillis())}', @ToDate='${new Date(cal2.getTimeInMillis())}', @TTId=${ttid}
  ```
  Reads: `TransId`, `FinId`, `TransDate`, `TransType`, `Narration`, `Particulars`, `TransAmount`, `SourceLedgerId`, `TransLedgerId`, `FWVWUDTransId`

## `app_unpaidpartiesforselecteddate`

- [`selectdateforreport.ASYNCcollectiondetailsr.doInBackground:151`](06-collection-reports-collection-day-abstract-7.md#selectdateforreport) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APP_unpaidpartiesforselecteddate @ColDate='${TransDate}',@FinId=${globalvars.finid}
  ```
  Reads: `MCLoanId`, `FWUDMCLoanId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `PartyName`, `SubAreaNo`, `SubAreaName`, `PassedDues`, `LoanMethodDesc`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`

## `appbs_getbalancefortheledger`

- [`tbbspl_list.ASYNCGetselectedLedgerAmount.doInBackground:845`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APPBS_GetBalancefortheLedger @LedgerId=${params[0]}, @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
  ```
  Reads: `Value`

## `appbs_getbankbalance`

- [`tbbspl_list.ASYNCGetAccountReportDetails.doInBackground:517`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APPBS_GetBankBalance @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
  ```
  Reads: `LedgerName`, `Value`

## `appbs_getcashbalance`

- [`tbbspl_list.ASYNCGetAccountReportDetails.doInBackground:483`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APPBS_GetCashBalance @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
  ```
  Reads: `LedgerName`, `LedgerId`, `Value`

## `applistplincomes`

- [`tbbspl_list.ASYNCGetAccountReportDetails.doInBackground:555`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec APPListPLIncomes @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
  ```

## `apppattiproc_forbalancesheet`

- [`tbbspl_list.ASYNCGetAccountReportDetails.doInBackground:365`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APPPattiProc_forBalancesheet    @SelectedFinanceIds='${selectecdfinanceids}', @ReportDate ='${reportdate}'
  ```
  Reads: `LedgerName`, `LedgerId`, `Balance`, `MCLoanId`, `IsNip`

## `apppattiproc_getextracollection`

- [`tbbspl_list.ASYNCGetAccountReportDetails.doInBackground:577`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec APPPattiProc_GetExtraCollection    @SelectedFinanceIds='${selectecdfinanceids}', @FromColDate ='${fromdate}', @ToColDate ='${reportdate}'
  ```
  Reads: `LedgerName`, `ExtraCollection`, `MCLoanId`

## `bsapp_getbalancesheetcomponentparticulars`

- [`tbbspl_list.ASYNCGetAccountReportDetails.doInBackground:321`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec BSAPP_GetBalanceSheetComponentParticulars @ComponentId=2, @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
  ```
  Reads: `LedgerName`, `LedgerId`, `Cr`, `dr`
- [`tbbspl_list.ASYNCGetAccountReportDetails.doInBackground:440`](10-accounts-reports-trial-balance-balance-sheet.md#tbbspllist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec BSAPP_GetBalanceSheetComponentParticulars @ComponentId=${bscompslist.get(i).getValuemember()}, @SelectedFinanceIds='${selectecdfinanceids}',  @RDate ='${reportdate}'
  ```
  Reads: `LedgerName`, `LedgerId`, `Cr`, `dr`

## `checkisallcashandbankbooksverified`

- [`cashbook.asynccashbookverificationand_blockdownloauploadaccess.doInBackground:741`](13-accounts-transactions-cash-bank-book-expenses.md#cashbook) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec checkisallcashandbankbooksverified @FinId=${globalvars.finid} , @VerfiedDate='${cashbookdate}'
  ```
  Reads: `UnverifiedCount`

## `checkisanyoneofcashandorbooksverified`

- [`uploadcollection.ASYNCUpload.doInBackground:766`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec checkisanyoneofcashandorbooksverified @FinId=${globalvars.finid} ,  @VerfiedDate='${globalvars.collectiondate}'
  ```
  Reads: `verifiedCount`

## `collectiondayandcollectionexecutiveforapp`

- [`selectbranchandfinance.ASYNCDataforAttendance.doInBackground:1386`](02-navigation-hub-routers-line-branch-selection.md#selectbranchandfinance) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec  CollectionDayandCollectionExecutiveforapp   @FromDate ='${fromdate}', @ToDate ='${todate}'
  ```
  Reads: `attdate`, `finname`, `ledgerid`

## `curdaycashbook`

- [`cashbook.AsyncCallCashBook.doInBackground:629`](13-accounts-transactions-cash-bank-book-expenses.md#cashbook) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec CurDayCashBook @IMEI='${globalvars.device_id}', @CashLedgerId=${selectedledgerid} ,@FinId =${globalvars.finid},  @CurDate='${cashbookdate}'
  ```

## `curdaycompainedcashbook`

- [`cashbook.AsyncCallCashBook.doInBackground:631`](13-accounts-transactions-cash-bank-book-expenses.md#cashbook) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec CurDayCompainedCashBook @IMEI='${globalvars.device_id}', @FinId =${globalvars.finid},  @CurDate='${cashbookdate}'
  ```

## `dailycollectionanalysis`

- [`todaycollectionabstractlist.ASYNCGetAbstractlist.doInBackground:274`](06-collection-reports-collection-day-abstract-7.md#todaycollectionabstractlist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec DailyCollectionAnalysis @FinId =${financeid},@collDate='${sqlDate}'
  ```
  Reads: `ct`, `Particulars`, `PartiesCount`, `Collected`

## `dailycollectiondetails`

- [`todaycollectionlist.ASYNCGetCollectionDetails.doInBackground:464`](06-collection-reports-collection-day-abstract-7.md#todaycollectionlist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec DailyCollectionDetails @ctid=${ctid}, @FinId =${financeid},@collDate='${sqlDate}'
  ```

## `dailycollectiondetailsforweeklylines`

- [`todaycollectionlist.ASYNCGetCollectionDetails.doInBackground:462`](06-collection-reports-collection-day-abstract-7.md#todaycollectionlist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec DailyCollectionDetailsforweeklyLines @ctid=${ctid}, @FinId =${financeid},@collDate='${sqlDate}'
  ```

## `get_downloadlimitations`

- [`navigationdrawactivity.ASYNCGetDownloadUploadAllowedDetails.doInBackground:1592`](02-navigation-hub-routers-line-branch-selection.md#navigationdrawactivity) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec Get_DownloadLimitations @IMEI='${globalvars.device_id}'
  ```

## `get_ledgersforquicktransactions`

- [`accounttransactions_act.ASYNCRefreshaccountsledgers.doInBackground:638`](13-accounts-transactions-cash-bank-book-expenses.md#accounttransactionsact) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec Get_LedgersforQuickTransactions
  ```
  Reads: `TTId`, `FinId`, `AType`, `LedgerId`, `LedgerName`, `isExecutiveLevelExpensesLedger`, `isExecutiveLevelReceiptsLedger`
- [`refreshorreloadbrancheslist.ASYNCSettings.doInBackground:468`](02-navigation-hub-routers-line-branch-selection.md#refreshorreloadbrancheslist) · `jdbc.Statement.executeQuery` · master DB
  ```sql
  exec Get_LedgersforQuickTransactions
  ```
  Reads: `TTId`, `FinId`, `AType`, `LedgerId`, `LedgerName`, `isExecutiveLevelExpensesLedger`, `isExecutiveLevelReceiptsLedger`

## `get_uploadlimitations`

- [`navigationdrawactivity.ASYNCGetDownloadUploadAllowedDetails.doInBackground:1590`](02-navigation-hub-routers-line-branch-selection.md#navigationdrawactivity) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec Get_UploadLimitations @IMEI='${globalvars.device_id}'
  ```

## `get_uploadlimitationsdaterange`

- [`navigationdrawactivity.ASYNCGetDownloadUploadAllowedDetails.doInBackground:1582`](02-navigation-hub-routers-line-branch-selection.md#navigationdrawactivity) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec Get_UploadLimitationsDateRange @IMEI='${globalvars.device_id}'
  ```

## `getcashandbankledgers`

- [`refreshorreloadbrancheslist.ASYNCSettings.doInBackground:455`](02-navigation-hub-routers-line-branch-selection.md#refreshorreloadbrancheslist) · `jdbc.Statement.executeQuery` · master DB
  ```sql
  exec getCashandBankLedgers @URId=${globalvars.app_userroleid}
  ```
  Reads: `LedgerId`, `LedgerName`, `Finid`, `Alloted`, `LedgerType`, `istobeVerified`
- [`syncresources.asyncdatabasecontents.doInBackground:226`](01-app-start-registration-device-identity-settings.md#syncresources) · `jdbc.Statement.executeQuery` · master DB
  ```sql
  exec getCashandBankLedgers @URId=${globalvars.app_userroleid}
  ```
  Reads: `LedgerId`, `LedgerName`, `Finid`, `Alloted`, `LedgerType`, `istobeVerified`, `GroupId`

## `getfinwisepartners`

- [`financewisepartners_list.ASYNCGetFinancelist.doInBackground:249`](15-finance-transactions-lists-party-loan-ledgers.md#financewisepartnerslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec getfinwisepartners @FinId=${selectedfinanceid}
  ```
  Reads: `IsSelected`, `PartnerLedgerId`, `UID`, `LedgerName`, `Active`, `RecTransCount`

## `gethandloansdetails`

- [`handloans_list.ASYNC_GetHandloandetails.doInBackground:235`](13-accounts-transactions-cash-bank-book-expenses.md#handloanslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec GetHandLoansDetails
  ```
  Reads: `FinId`, `LedgerId`, `LedgerName`, `Receivable`, `Payable`

## `getlastncollectionforcollreportprint`

- [`collectionlistprints.AsyncDownloaddata.doInBackground:6464`](18-printing-collection-list-prints-receipts-bluetooth.md#collectionlistprints) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec GetLastNCollectionforCollReportPrint ?,?,?,?
  ```
  Params: 1 → `Date.valueOf(reportdate.toString())` · 2 → `globalvars.curfin.getFinId().intValue()` · 3 → `curfinancedefaultcollectionbreakstring` · 4 → `curmethod.getNoofbreaksforprevcollection()`
  Reads: `MCLoanId`, `MCLDueColDate`, `Paid`

## `getloandetailsforperiod`

- [`periodicloareportlist_act.ASYNCGetAbstractlist.doInBackground:201`](06-collection-reports-collection-day-abstract-7.md#periodicloareportlistact) · `jdbc.Statement.executeQuery` · branch DB
  ```sql
  exec GetLoanDetailsforPeriod @FinId =${financeid},@FromDate='${fromdate}' ,@ToDate='${todate}'
  ```
  Reads: `TypeId`, `MCLoanId`, `SNo`, `SubAreaName`, `Date`, `Party`, `LT`, `Loan`, `Dues`, `IntRate`, `Interest`, `PartyLedgerId`, `Mahimai`, `SubAreaNo`, `showid`, `Balance`

## `getmcloanslistforappforselectedperiod`

- [`loanslist_act.ASYNCLoansList.doInBackground:457`](08-loan-lists-loan-savings-closures-party.md#loanslistact) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec GetMCLoansListforAPPforselectedperiod ?,?,?
  ```
  Params: 1 → `receviedfinanceid` · 2 → `globalvars.collectiondate` · 3 → `globalvars.collectiondate`
- [`loanslist_act.ASYNCLoansList.doInBackground:465`](08-loan-lists-loan-savings-closures-party.md#loanslistact) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec GetMCLoansListforAPPforselectedperiod ?,?,?
  ```
  Params: 1 → `receviedfinanceid` · 2 → `curdaterange.getFromdate()` · 3 → `curdaterange.getTodate()`

## `getmcloanslistforappforselectedperiodwithweekdayid`

- [`loanslist_act.ASYNCLoansList.doInBackground:475`](08-loan-lists-loan-savings-closures-party.md#loanslistact) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec GetMCLoansListforAPPforselectedperiodwithweekdayid ?,?,?,?
  ```
  Params: 1 → `receviedfinanceid` · 2 → `globalvars.collectiondate` · 3 → `globalvars.collectiondate` · 4 → `weekdayid`
- [`loanslist_act.ASYNCLoansList.doInBackground:484`](08-loan-lists-loan-savings-closures-party.md#loanslistact) · `jdbc.prepareStatement` · selected-branch DB
  ```sql
  exec GetMCLoansListforAPPforselectedperiodwithweekdayid ?,?,?,?
  ```
  Params: 1 → `receviedfinanceid` · 2 → `curdaterange.getFromdate()` · 3 → `curdaterange.getTodate()` · 4 → `weekdayid`
  Reads: `MCLoanId`, `CWLedgerId`, `FinId`, `MCLoanDate`, `PartyLedgerId`, `MCLoanAmount`, `TotalMonths`, `DocCharges`, `LoanMethodId`, `InterestAmount`, `MCLoanAmountCollectable`, `InsAmount`, `ondaycollection`, `MCLoanAmountEstimated`, `SubAreaNo`, `ServerGenMCLoanId`, `ServerGenLedgerId`, `Remarks`, `WeekDayId`, `MonthDayid`, `PartyName`, `AreaName`, `ServerGenTransId`, `Collected`, `Sourceledgerid`, `PartyReferenced`, `Loanindex`, `MobileNo`, `AdvanceAmount`, `Balanceamount`, `Productvalue`, `Intrate`

## `getoflabstract`

- [`meetingreport.ASYNC_GetInterestPendingList.doInBackground:668`](11-daily-patti-outstanding-reports-meeting-report.md#meetingreport) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec GetOFLAbstract
  ```

## `getstlabstract`

- [`meetingreport.ASYNC_GetInterestPendingList.doInBackground:547`](11-daily-patti-outstanding-reports-meeting-report.md#meetingreport) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec GetSTLAbstract
  ```

## `getthittamitems`

- [`varathittam.AsyncWeeklyThittam.doInBackground:759`](12-thittam-collection-plan-weekly-analysis-reports.md#varathittam) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec GetThittamItems @CurFinId=${financeid}
  ```
  Reads: `முன்கையிருப்பு`, `கடன்வரவு`, `நிப்புவரவு`, `ஆதாயவரவு`, `மகிமைவரவு`, `எழுத்துக்கூலி`, `புதியகடன்`, `முன்நடப்பு`, `முன்நிப்பு`

## `insertaccledgersapp`

- [`accountledgerdetails.ASYNCupdateemployee.doInBackground:245`](13-accounts-transactions-cash-bank-book-expenses.md#accountledgerdetails) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAccLedgersAPP(?,?,?,?,?,?,?)}
  ```
  Params: "LedgerName" → `accledgerselected.getLedgerName()` · "GroupId" → `accledgerselected.getGroupId()` · "ScreenType" → `5` · "IsDefault" → `0.0d` · "isReserved" → `0.0d` · "FinId" → `accledgerselected.getFinid()` · "FinId" → `8`
  OUT: "LedgerId" (8)
  Reads: `LedgerId`

## `insertacctransactionsmaster`

- [`closingsavingsaccount.ASYNCSave_savingsclosure.doInBackground:171`](08-loan-lists-loan-savings-closures-party.md#closingsavingsaccount) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAccTransactionsMaster(?,?,?,?,?,?,?,?,?,? )}
  ```
  Params: "TransDate" → `loanclosuredate` · "TransType" → `28` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `paidamout` · "TransLedgerId" → `112.0d` · "ImageFileName" → `""`
  OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
  Reads: `TransId`
- [`loanclosoureentry.ASYNCSaveLoanClosure.doInBackground:361`](08-loan-lists-loan-savings-closures-party.md#loanclosoureentry) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAccTransactionsMaster(?,?,?,?,?,?,?,?,?,? )}
  ```
  Params: "TransDate" → `closureloanobj.getClosedDate()` · "TransType" → `23` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `closureloanobj.getPending()` · "TransLedgerId" → `125.0d` · "ImageFileName" → `""`
  OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
  Reads: `TransId`

## `insertappacctransactionsmaster`

- [`accounttransactions_act.ASYNCTransactions.doInBackground:737`](13-accounts-transactions-cash-bank-book-expenses.md#accounttransactionsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAPPAccTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `curtransaction.getTransDate()` · "TransType" → `ttid` · "Narration" → `curtransaction.getNarration()` · "FinId" → `curtransaction.getFinid()` · "TransAmount" → `curtransaction.getTransAmount()` · "TransLedgerId" → `curtransaction.getLedgerIdTwo()` · "TransLedgerId" → `curtransaction.getLedgerIdOne()` · "SourceLedgerId" → `curtransaction.getLedgerIdTwo()` · "SourceLedgerId" → `curtransaction.getLedgerIdOne()` · "ImageFileName" → `4`
  OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
  Reads: `TransId`, `FWVWUDTransId`
- [`invtransactnew.ASYNC_SaveSalesentry.doInBackground:1133`](17-inventory-module-stock-masters-items-customers.md#invtransactnew) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAPPAccTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "FWVWUDTransId" → `curtransaction.getFWVWUDTransId()` · "TransDate" → `curtransaction.getTransDate()` · "TransType" → `ttid` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `curtransaction.getTransAmount()` · "TransLedgerId" → `globalvars.PURCHASE_LEDGERID.intValue()` · "SourceLedgerId" → `curtransaction.getTransLedgerId()` · "ImageFileName" → `4`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`

## `insertappmcloanclosuretransdetailsp1`

- [`closingsavingsaccount.ASYNCSave_savingsclosure.doInBackground:184`](08-loan-lists-loan-savings-closures-party.md#closingsavingsaccount) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAPPMCLoanClosureTransDetailsP1(?,?,?,?,?,?,?)}
  ```
  Params: 1 → `AccTransId` · 2 → `retriver.fetchstaffcashbookledgerid(db, globalvars.finid.intValue())` · 3 → `paidamout` · 4 → `"Source"` · 5 → `"Cr"` · 6 → `1.0d` · 7 → `2.0d`
- [`loanclosoureentry.ASYNCSaveLoanClosure.doInBackground:374`](08-loan-lists-loan-savings-closures-party.md#loanclosoureentry) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAPPMCLoanClosureTransDetailsP1(?,?,?,?,?,?,?)}
  ```
  Params: 1 → `closureloanobj.getTransId()` · 2 → `closureloanobj.getPartyLedgerId()` · 3 → `closureloanobj.getPending()` · 4 → `"Source"` · 5 → `"Cr"` · 6 → `1.0d` · 7 → `2.0d`
- [`loanclosoureentry.ASYNCSaveLoanClosure.doInBackground:416`](08-loan-lists-loan-savings-closures-party.md#loanclosoureentry) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertAPPMCLoanClosureTransDetailsP1(?,?,?,?,?,?,?)}
  ```
  Params: 1 → `closureloanobj.getTransId()` · 2 → `closureloanobj.getPartyLedgerId()` · 3 → `closureloanobj.getPending()` · 4 → `"Source"` · 5 → `"Cr"` · 6 → `1.0d` · 7 → `2.0d`

## `insertbranchempledgers`

- [`branch_employee.ASYNCupdateemployee.doInBackground:157`](16-admin-masters-employees-branch-employees-access.md#branchemployee) · `jdbc.prepareCall` · master DB
  ```sql
  {call dbo.InsertBranchEmpLedgers(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "LedgerName" → `receivedemployee.getLedgerName()` · "GroupId" → `29.0d` · "FinId" → `4` · "Tag" → `""` · "AddLine1" → `receivedemployee.getAddLine1()` · "AddLine2" → `receivedemployee.getAddLine2()` · "City" → `receivedemployee.getCity()` · "PinCode" → `receivedemployee.getPinCode()` · "MobileNo1" → `receivedemployee.getMobileNo1()` · "MobileNo2" → `receivedemployee.getMobileNo2()` · "EMail" → `receivedemployee.getEMail()` · "designationid" → `1` · "desigstatusid" → `receivedemployee.getDesigstatusid()` · "ScreenType" → `8`
  OUT: "LedgerId" (8)
  Reads: `LedgerId`

## `insertbranchmobiledevlicesallowed`

- [`branchmobiledeviceaccescontrol_act.async_accessrights.doInBackground:315`](01-app-start-registration-device-identity-settings.md#branchmobiledeviceaccescontrolact) · `jdbc.prepareCall` · master DB
  ```sql
  {call dbo.InsertBranchMobileDevlicesAllowed(?,?,?,?,? )}
  ```
  Params: "IMEI" → `curmaobj.getIMEI()` · "LId" → `curmaobj.getLId()` · "isActive" → `curmaobj.isActive()` · "isbranchusercontrolallowed" → `curmaobj.isIsbranchusercontrolallowed()`
  OUT: "MDAId" (8)
  Reads: `MDAId`

## `insertbranchnewimeirequests`

- [`deviceidentity.asyncraisebranchaccessrequest.doInBackground:193`](01-app-start-registration-device-identity-settings.md#deviceidentity) · `jdbc.prepareCall` · master DB
  ```sql
  {call dbo.InsertBranchNewIMEIRequests(?,?,?)}
  ```
  Params: "IMEI" → `globalvars.device_id`
  OUT: "NIRId" (8) · "RequestRaisedAt" (91)
  Reads: `NIRId`, `RequestRaisedAt`

## `insertcashbookverification`

- [`cashbook.asynccashbookverificationand_blockdownloauploadaccess.doInBackground:731`](13-accounts-transactions-cash-bank-book-expenses.md#cashbook) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertCashBookVerification(?,?,?,?,?,?)}
  ```
  Params: "FinId" → `globalvars.finid.intValue()` · "VerfiedDate" → `cashbookdate.toString()` · "verifiedby" → `globalvars.APPUserName` · "ledgerid" → `selectedledgerid`
  OUT: "CBVId" (8) · "VerifiedDateTime" (91)

## `insertcontratbltransactionsmaster`

- [`cashreceiveandreturns.ASYNCReceiptsandReturns.doInBackground:108`](13-accounts-transactions-cash-bank-book-expenses.md#cashreceiveandreturns) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertContratblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransType" → `9.0d` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `receivedamount` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · "GColId" → `0.0d` · "TransLedgerId" → `targetledgerid.intValue()` · "SourceLedgerId" → `sourceledgerid.intValue()` · "RouteId" → `globalvars.finid.intValue()`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`

## `insertdepositinttbltransactionsmaster`

- [`ofloans_interestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:753`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertDepositInttblTransactionsMaster( ?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransType" → `CurTTId` · str3 → `str3` · "FinId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TransAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TGWTID" → `TGWTId` · "TGFWTId" → `TGFWTId` · "TransDate" → `defaulttransdate`
  OUT: "FWVWTransId" (4) · "FWVWUDTransId" (4) · "TransId" (4)
  Reads: `TransId`
- [`stloaninterestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:741`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertDepositInttblTransactionsMaster( ?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransType" → `CurTTId` · "Narration" → `"Narration"` · "FinId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getFinId()` · "TransAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` · "TGWTID" → `TGWTId` · "TGFWTId" → `TGFWTId` · "TransDate" → `defaulttransdate`
  OUT: "FWVWTransId" (4) · "FWVWUDTransId" (4) · "TransId" (4)
  Reads: `TransId`

## `insertempledgers`

- [`employeedetails_act.ASYNCupdateemployee.doInBackground:224`](16-admin-masters-employees-branch-employees-access.md#employeedetailsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertEmpLedgers(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "LedgerName" → `receivedemployee.getLedgerName()` · "GroupId" → `29.0d` · "FinId" → `receivedemployee.getFinId()` · "FinId" → `4` · "Tag" → `""` · "AddLine1" → `receivedemployee.getAddLine1()` · "AddLine2" → `receivedemployee.getAddLine2()` · "City" → `receivedemployee.getCity()` · "PinCode" → `receivedemployee.getPinCode()` · "MobileNo1" → `receivedemployee.getMobileNo1()` · "MobileNo2" → `receivedemployee.getMobileNo2()` · "EMail" → `receivedemployee.getEMail()` · "designationid" → `1` · "desigstatusid" → `receivedemployee.getDesigstatusid()` · "ScreenType" → `8`
  OUT: "LedgerId" (8)
  Reads: `LedgerId`

## `insertimageuploads`

- [`accounttransactions_act.ASYNCTransactions.doInBackground:724`](13-accounts-transactions-cash-bank-book-expenses.md#accounttransactionsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertImageUploads(?,?)}
  ```
  Params: "Dummy" → `"Dummy"`
  OUT: "IUId" (4)
  Reads: `IUId`
- [`documentsupload.ASYNCUploaddocumentinfo.doInBackground:248`](07-parties-borrowers-new-loan-creation-kyc.md#documentsupload) · `jdbc.prepareCall` · branch DB
  ```sql
  {call dbo.InsertImageUploads(?,?)}
  ```
  Params: "Dummy" → `"Dummy"`
  OUT: "IUId" (4)
  Reads: `IUId`
- [`partyandpartnerdetails.updatepartiesinremoteandlocal.doInBackground:431`](07-parties-borrowers-new-loan-creation-kyc.md#partyandpartnerdetails) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertImageUploads(?,?)}
  ```
  Params: "Dummy" → `"Dummy"`
  OUT: "IUId" (4)
  Reads: `IUId`
- [`partydetails_act.updatepartiesinremoteandlocal.doInBackground:1549`](07-parties-borrowers-new-loan-creation-kyc.md#partydetailsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertImageUploads(?,?)}
  ```
  Params: "Dummy" → `"Dummy"`
  OUT: "IUId" (4)
  Reads: `IUId`
- [`partydetails_act.updatepartiesinremoteandlocal.doInBackground:1559`](07-parties-borrowers-new-loan-creation-kyc.md#partydetailsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertImageUploads(?,?)}
  ```
  Params: "Dummy" → `"Dummy"`
  OUT: "IUId" (4)
  Reads: `IUId`
- [`partydetails_act.updatepartiesinremoteandlocal.doInBackground:1569`](07-parties-borrowers-new-loan-creation-kyc.md#partydetailsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertImageUploads(?,?)}
  ```
  Params: "Dummy" → `"Dummy"`
  OUT: "IUId" (4)
  Reads: `IUId`

## `insertitemmaster`

- [`itemmaster_activity.ASYNCupdatestockcat.doInBackground:219`](17-inventory-module-stock-masters-items-customers.md#itemmasteractivity) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Insertitemmaster(?,?,?,?,?,?,?)}
  ```
  Params: "ItemName" → `curitemmasterdata.getItemName()` · "PriUOMid" → `curitemmasterdata.getPriUOMid()` · "Catid" → `curitemmasterdata.getCatid()` · "Grpid" → `curitemmasterdata.getGrpid()` · "MinimumOrderIdQty" → `curitemmasterdata.getMinimumOrderIdQty()` · "ReOrderLevel" → `curitemmasterdata.getReOrderLevel()`
  OUT: "itemcode" (8)
  Reads: `itemcode`

## `insertloanmethods`

- [`loanmethods.ASYNCUpdateLoanmethods.doInBackground:339`](16-admin-masters-employees-branch-employees-access.md#loanmethods) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertLoanMethods(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "LoanMethodDesc" → `receiveddata.getLoanMethodDesc()` · "LoanAmount" → `receiveddata.getLoanAmount()` · "LoanAmountGiven" → `receiveddata.getLoanAmountGiven()` · "IntDeduction" → `receiveddata.getIntDeduction()` · "IntAddition" → `receiveddata.getIntAddition()` · "TotAmountCollectable" → `receiveddata.getTotAmountCollectable()` · "CollectionBreak" → `receiveddata.getCollectionBreak().intValue()` · "CollectionBreakString" → `receiveddata.getCollectionBreakString()` · "NoOfIns" → `receiveddata.getNoOfIns().intValue()` · "InsAmount" → `receiveddata.getInsAmount()` · "GracePeriod" → `receiveddata.getGracePeriod().intValue()` · "DocChargesMethod" → `receiveddata.getDocChargesMethod().intValue()` · "DocCharges" → `receiveddata.getDocCharges().intValue()` · "ReportIdentification" → `receiveddata.getReportIdentification()` · "CBCount" → `receiveddata.getCBCount()` · "IsVisible" → `receiveddata.getVisible()` · "ShowId" → `receiveddata.getShowId().intValue()` · "WarningDays" → `receiveddata.getWarningDays()` · "DueSupportAmount" → `receiveddata.getDueSupportAmount()`
  OUT: "LoanMethodId" (8)
  Reads: `LoanMethodId`

## `insertmcloansclosoure`

- [`loanclosoureentry.ASYNCSaveLoanClosure.doInBackground:393`](08-loan-lists-loan-savings-closures-party.md#loanclosoureentry) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertMCLoansClosoure(?,?,?,?,?,?,?,?)}
  ```
  Params: "MCLoanId" → `closureloanobj.getMCLoanId()` · "ClosedDate" → `closureloanobj.getClosedDate()` · "Collectable" → `closureloanobj.getMCLoanAmountCollectable()` · "Collected" → `closureloanobj.getCollected()` · "Pending" → `closureloanobj.getPending()` · "extracollection" → `closureloanobj.getExtraCollection()` · "TransId" → `closureloanobj.getTransId()`
  OUT: "LCId" (8)

## `insertmobiledevlicesallowed`

- [`mobiledeviceaccescontrol_act.ASYNCSSaveNewMarks.doInBackground:378`](01-app-start-registration-device-identity-settings.md#mobiledeviceaccescontrolact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertMobileDevlicesAllowed(?,?,?,?,?)}
  ```
  Params: "IMEI" → `curmaobj.getIMEI()` · "LId" → `curmaobj.getLId()` · "isActive" → `curmaobj.getActive().booleanValue()` · "userroleid" → `curmaobj.getUserroleid()`
  OUT: "MDAId" (8)
  Reads: `MDAId`

## `insertnewparties`

- [`partydetails_act.updatepartiesinremoteandlocal.doInBackground:1689`](07-parties-borrowers-new-loan-creation-kyc.md#partydetailsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertNewParties(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "CWLedgerId" → `receivedparty.getCWLedgerId().intValue()` · "LedgerName" → `receivedparty.getLedgerName()` · "Tag" → `receivedparty.getTag()` · "AreaNo" → `globalvars.finid.intValue()` · "SubAreaNo" → `SubAreaNo` · "UDLedgerId" → `4` · "MobileNo1" → `receivedparty.getMobileNo1()` · "MobileNo2" → `receivedparty.getMobileNo2()` · "AddLine1" → `receivedparty.getAddLine1()` · "SpouseName" → `receivedparty.getSpouseName()` · "FamilyName" → `receivedparty.getFamilyName()` · "ImagefileName" → `receivedparty.getImagefilename()` · "IdProoffileName" → `receivedparty.getIdProofFileName()` · "lat" → `receivedparty.getLat()` · "lng" → `receivedparty.getLng()` · "IdProofFileNameII" → `receivedparty.getIdProofImageIIfileName()` · "NoOfLoansAllowed" → `receivedparty.getNoOfLoansAllowed()` · "MaximumAmountAllowed" → `receivedparty.getMaximumAmountAllowed()`
  OUT: "LedgerId" (8)
  Reads: `LedgerId`
- [`uploadcollection.ASYNCUpload.doInBackground:811`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertNewParties(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```

## `insertnewpartieswithautocwledgerid`

- [`uploadcollection.ASYNCUpload.doInBackground:811`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertNewPartieswithautocwledgerid(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```

## `insertpartiesfromapp`

- [`partyandpartnerdetails.updatepartiesinremoteandlocal.doInBackground:441`](07-parties-borrowers-new-loan-creation-kyc.md#partyandpartnerdetails) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertPartiesfromAPP(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "Ledgername" → `receivedparty.getLedgerName()` · "tag" → `receivedparty.getTag()` · "groupid" → `receivedparty.getGroupId()` · "GType" → `receivedparty.getGType()` · "GuardianName" → `receivedparty.getGuardianName()` · "AddLine1" → `receivedparty.getAddLine1()` · "AddLine2" → `receivedparty.getAddLine2()` · "City" → `receivedparty.getCity()` · "Pincode" → `receivedparty.getPinCode()` · "MobileNo1" → `receivedparty.getMobileNo1()` · "MobileNo2" → `receivedparty.getMobileNo2()` · 12 → `receivedparty.getMasterPartyLedgerId()` · "MasterPartyLedgerId" → `receivedparty.getMasterPartyLedgerId()` · "MasterPartyLedgerId" → `4` · "PanNo" → `receivedparty.getPanNo()` · "AadhaarNo" → `receivedparty.getAadhaarNo()` · "Imagefilename" → `receivedparty.getImagefilename()` · "MartialStatus" → `receivedparty.getMartialStatus()` · "Qualification" → `receivedparty.getQualification()` · "Profession" → `receivedparty.getProfession()` · "MonthlyIncome" → `receivedparty.getMonthlyIncome()` · "GuardianMonthlyIncome" → `receivedparty.getGuardianMonthlyIncome()` · "GuardianMobileNo1" → `receivedparty.getGuardianMobileNo1()` · "GuardianMobileNo2" → `receivedparty.getGuardianMobileNo2()` · "GuardianProfession" → `receivedparty.getGuardianProfession()` · "LandMark" → `receivedparty.getLandMark()` · "ScreenType" → `receivedparty.getScreenType()`
  OUT: "ledgerid" (8)
  Reads: `ledgerid`

## `insertrelateddoccollection`

- [`documentsupload.ASYNCUploaddocumentinfo.doInBackground:258`](07-parties-borrowers-new-loan-creation-kyc.md#documentsupload) · `jdbc.prepareCall` · branch DB
  ```sql
  {call dbo.InsertRelatedDocCollection(?,?,?,?,?)}
  ```
  Params: "MCLoanId" → `passeddocument.getMcloanid()` · "DUID" → `passeddocument.getDuid()` · "Remarks" → `passeddocument.getRemarks()` · "ImagePath" → `passeddocument.getImagepath()`
  OUT: "DCUId" (4)
  Reads: `DCUId`

## `insertstockcat`

- [`stockcategories.ASYNCupdatestockcat.doInBackground:168`](17-inventory-module-stock-masters-items-customers.md#stockcategories) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Insertstockcat(?,?,?,?)}
  ```
  Params: "CatDesc" → `curstockcat.getCatDesc()` · "DisplayIndex" → `curstockcat.getDisplayIndex()` · "IsActive" → `curstockcat.getActive().booleanValue()`
  OUT: "catid" (8)
  Reads: `catid`

## `insertstockgrp`

- [`stockgroup_act.ASYNCupdatestockgrp.doInBackground:171`](17-inventory-module-stock-masters-items-customers.md#stockgroupact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertStockGrp(?,?,?,?,?)}
  ```
  Params: "GrpDesc" → `curstockgrp.getGrpDesc()` · "HSNSACCode" → `curstockgrp.getHSNSACCode()` · "DisplayIndex" → `curstockgrp.getDisplayIndex()` · "IsActive" → `curstockgrp.getActive().booleanValue()`
  OUT: "GrpId" (8)
  Reads: `GrpId`

## `insertstockuom`

- [`stockUOM_act.ASYNCupdatestockuom.doInBackground:158`](17-inventory-module-stock-masters-items-customers.md#stockuomact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertStockUOM(?,?,?,?)}
  ```
  Params: "UOMDesc" → `curstockuom.getUOMDesc()` · "DisplayIndex" → `curstockuom.getDisplayIndex()` · "IsActive" → `curstockuom.getActive().booleanValue()`
  OUT: "UOMId" (8)
  Reads: `UOMId`

## `insertsubareadetails`

- [`collectionareadetails_act.AYNCUploadCollectionArea.doInBackground:225`](09-collection-areas-maps-gps-staff-tracking.md#collectionareadetailsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertSubAreaDetails(?,?,?,?,?,?,?,?)}
  ```
  Params: "SubAreaName" → `receivedarea.getSubAreaName()` · "IsVisible" → `receivedarea.getVisible()` · "ShowId" → `receivedarea.getShowId()` · "FinId" → `globalvars.finid.intValue()` · "Latitude" → `receivedarea.getLatitude()` · "Longitude" → `receivedarea.getLongitude()` · "WeekdayId" → `receivedarea.getWeekdayId()` · "WeekdayId" → `4`
  OUT: "SubAreaNo" (8)
  Reads: `SubAreaNo`

## `insertsuppliers`

- [`customerdetails_act.ASYNCupdateAccLedgers.doInBackground:256`](17-inventory-module-stock-masters-items-customers.md#customerdetailsact) · `jdbc.prepareCall` · branch DB
  ```sql
  {call dbo.InsertSuppliers(?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "ledgername" → `receievedcustomer.getLedgerName()` · "AddLine1" → `receievedcustomer.getAddLine1()` · "AddLine2" → `receievedcustomer.getAddLine2()` · "City" → `receievedcustomer.getCity()` · "PinCode" → `receievedcustomer.getPinCode()` · "MobileNo1" → `receievedcustomer.getMobileNo1()` · "MobileNo2" → `receievedcustomer.getMobileNo2()` · "EMail" → `receievedcustomer.getEMail()` · "GroupId" → `receievedcustomer.getGroupId()` · "IsDefault" → `receievedcustomer.getIsDefault()` · "IsReserved" → `receievedcustomer.getIsReserved()` · "ScreenType" → `receievedcustomer.getScreenType()`
  OUT: "ledgerid" (8)
  Reads: `ledgerid`

## `inserttblmcloanduecol`

- [`MyIntentService.ASYNCSavingCollections.doInBackground:151`](03-offline-mode-download-upload-sync-services.md#myintentservice) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "MCLDueColDate" → `selectedcollection.getCollDate()` · "MCLoanId" → `tabbedcollectionlistdataList.get(i).getMCLoanId().intValue()` · "TTId" → `22.0d` · "SourceLedgerId" → `tabbedcollectionlistdataList.get(i).getPartyLedgerId().intValue()` · "TargetLedgerId" → `1.0d` · "FinId" → `selectedcollection.getFinId()` · "Paid" → `tabbedcollectionlistdataList.get(i).getCollected().intValue()` · "GColId" → `selectedcollection.getMCLGCollId()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` · "SavingsCollected" → `tabbedcollectionlistdataList.get(i).getSavingsCollected().intValue()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` · "CashCollection" → `tabbedcollectionlistdataList.get(i).getCashCollection()` · "BankCollection" → `tabbedcollectionlistdataList.get(i).getBankCollection()` · "lng" → `tabbedcollectionlistdataList.get(i).getCollectionlng()` · "lat" → `tabbedcollectionlistdataList.get(i).getCollectionlat()`
  OUT: "MCLDueCollId" (8)
  Reads: `MCLDueCollId`
- [`MyService_updatecollection.ASYNCSavingCollections.doInBackground:153`](03-offline-mode-download-upload-sync-services.md#myserviceupdatecollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "MCLDueColDate" → `selectedcollection.getCollDate()` · str2 → `tabbedcollectionlistdataList.get(i).getMCLoanId().intValue()` · "TTId" → `22.0d` · "SourceLedgerId" → `tabbedcollectionlistdataList.get(i).getPartyLedgerId().intValue()` · "TargetLedgerId" → `1.0d` · "FinId" → `selectedcollection.getFinId()` · "Paid" → `tabbedcollectionlistdataList.get(i).getCollected().intValue()` · "GColId" → `selectedcollection.getMCLGCollId()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` · "SavingsCollected" → `tabbedcollectionlistdataList.get(i).getSavingsCollected().intValue()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` · "CashCollection" → `tabbedcollectionlistdataList.get(i).getCashCollection()` · "BankCollection" → `tabbedcollectionlistdataList.get(i).getBankCollection()` · "lng" → `tabbedcollectionlistdataList.get(i).getCollectionlng()` · "lat" → `tabbedcollectionlistdataList.get(i).getCollectionlat()`
  OUT: "MCLDueCollId" (8)
  Reads: `MCLDueCollId`
- [`editing_collectionswithfulloptions_act.ASYNCSavingCollections.doInBackground:1866`](05-online-collection-entry-editing.md#editingcollectionswithfulloptionsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "MCLDueColDate" → `selectedcollection.getCollDate()` · "MCLoanId" → `tabbedcollectionlistdataList.get(i5).getMCLoanId().intValue()` · "TTId" → `22.0d` · "SourceLedgerId" → `tabbedcollectionlistdataList.get(i5).getPartyLedgerId().intValue()` · "TargetLedgerId" → `1.0d` · "FinId" → `selectedcollection.getFinId()` · "Paid" → `tabbedcollectionlistdataList.get(i5).getCollected().intValue()` · "GColId" → `selectedcollection.getMCLGCollId()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i5).getPenaltyCollected().intValue()` · "SavingsCollected" → `tabbedcollectionlistdataList.get(i5).getSavingsCollected().intValue()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i5).getPenaltyCollected().intValue()` · "CashCollection" → `tabbedcollectionlistdataList.get(i5).getCashCollection()` · "BankCollection" → `tabbedcollectionlistdataList.get(i5).getBankCollection()` · "lng" → `tabbedcollectionlistdataList.get(i5).getCollectionlng()` · "lat" → `tabbedcollectionlistdataList.get(i5).getCollectionlat()`
  OUT: str2 (8)
  Reads: `OUT#str2`
- [`editing_collectionswithfulloptions_act.ASYNCSavingCollections.doInBackground:1931`](05-online-collection-entry-editing.md#editingcollectionswithfulloptionsact) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "MCLDueColDate" → `selectedcollection.getCollDate()` · "MCLoanId" → `tabbedcollectionlistdataList.get(i6).getMCLoanId().intValue()` · "TTId" → `22.0d` · "SourceLedgerId" → `tabbedcollectionlistdataList.get(i6).getPartyLedgerId().intValue()` · "TargetLedgerId" → `1.0d` · "FinId" → `selectedcollection.getFinId()` · "FinId" → `tabbedcollectionlistdataList.get(i6).getFinid()` · "Paid" → `tabbedcollectionlistdataList.get(i6).getCollected().intValue()` · "GColId" → `selectedcollection.getMCLGCollId()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i6).getPenaltyCollected().intValue()` · "SavingsCollected" → `tabbedcollectionlistdataList.get(i6).getSavingsCollected().intValue()` · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i6).getPenaltyCollected().intValue()` · "CashCollection" → `tabbedcollectionlistdataList.get(i6).getCashCollection()` · "BankCollection" → `tabbedcollectionlistdataList.get(i6).getBankCollection()` · "lng" → `tabbedcollectionlistdataList.get(i6).getCollectionlng()` · "lat" → `tabbedcollectionlistdataList.get(i6).getCollectionlat()`
  OUT: str3 (8)
  Reads: `OUT#str3`

## `inserttblmcloangroupcollectionmasterwithtwostaffs`

- [`directcollectionentry.asyncsavecollection.doInBackground:848`](04-offline-collection-entry-area-wise-collection.md#directcollectionentry) · `jdbc.prepareCall` · branch DB
  ```sql
  {call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "CollDate" → `collectiondate` · "CollAmount" → `duecollection` · "PenaltyCollected" → `penaltycollection` · "SavingsCollected" → `savingscollection` · "CollStaffLedgerId" → `globalvars.MobileUserLedgerId.intValue()` · "CollStaff2LedgerId" → `globalvars.MobileUserLedgerId.intValue()` · "TransId" → `CollTransId` · "NIPAmountCollected" → `0.0d` · "FinId" → `globalvars.finid.intValue()` · "BankCollection" → `bankcollection` · "CashCollection" → `cashcollecton`
  OUT: "MCLGCollId" (3) · "FWGColId" (3)
  Reads: `MCLGCollId`
- [`editing_collectionswithfulloptions_act.AsyncDownloaddata.doInBackground:1559`](05-online-collection-entry-editing.md#editingcollectionswithfulloptionsact) · `jdbc.prepareCall` · branch-report DB
  ```sql
  {call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "CollDate" → `globalvars.collectiondate` · "CollAmount" → `0.0d` · "PenaltyCollected" → `0.0d` · "SavingsCollected" → `0.0d` · "CollStaffLedgerId" → `globalvars.currentcollectionexecutivelederid1.intValue()` · "CollStaff2LedgerId" → `globalvars.currentcollectionexecutivelederid2.intValue()` · "TransId" → `selectedcollection.getTransId()` · "NIPAmountCollected" → `0.0d` · "FinId" → `globalvars.finid.intValue()` · str21 → `0` · str20 → `0`
  OUT: "MCLGCollId" (3) · "FWGColId" (3)
  Reads: `MCLGCollId`, `FWGColId`
- [`uploadcollection.ASYNCUpload.doInBackground:874`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "CollDate" → `globalvars.collectiondate` · "CollAmount" → `DueCollectionTotal` · "PenaltyCollected" → `PenaltyCollectedTotal` · "SavingsCollected" → `SavingsCollectedTotal` · "CollStaffLedgerId" → `globalvars.currentcollectionexecutivelederid1.intValue()` · "CollStaff2LedgerId" → `globalvars.currentcollectionexecutivelederid2.intValue()` · "TransId" → `CollTransId.doubleValue()` · "NIPAmountCollected" → `0.0d` · "FinId" → `globalvars.finid.intValue()` · str12 → `BankCollectionTotal` · str13 → `CashCollectionTotal`
  OUT: "MCLGCollId" (3) · "FWGColId" (3)
  Reads: `MCLGCollId`
- [`uploadcollection.ASYNCUpload.doInBackground:1492`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "CollDate" → `globalvars.collectiondate` · "CollAmount" → `DueCollectionTotal` · "PenaltyCollected" → `PenaltyCollectedTotal` · "SavingsCollected" → `SavingsCollectedTotal` · "CollStaffLedgerId" → `globalvars.currentcollectionexecutivelederid1.intValue()` · "CollStaff2LedgerId" → `globalvars.currentcollectionexecutivelederid2.intValue()` · "TransId" → `CollTransId.doubleValue()` · "NIPAmountCollected" → `0.0d` · "FinId" → `globalvars.finid.intValue()` · str12 → `BankCollectionTotal` · str13 → `CashCollectionTotal`
  OUT: "MCLGCollId" (3) · "FWGColId" (3)
  Reads: `MCLGCollId`

## `inserttblmcloanswithweekdayid`

- [`newloancreation.ASYNCUpdateLoaninSQLDB.doInBackground:1508`](07-parties-borrowers-new-loan-creation-kyc.md#newloancreation) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanswithWeekdayId(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ,?,?,?,?,?,?)}
  ```
  Params: "MCLoanDate" → `receivedloandata.getMCLoanDate()` · "FWMCLoanId" → `4` · "FWUDMCLoanId" → `4` · "FinId" → `globalvars.finid.intValue()` · "PartyLedgerId" → `receivedloandata.getServerGenLedgerId()` · "AgentLedgerId" → `4` · "GuaranderLedgerID" → `4` · "RecPartnerLedgerID" → `4` · "MCLoanAmount" → `receivedloandata.getMCLoanAmount()` · "TotalMonths" → `receivedloandata.getTotalMonths()` · "IntRate" → `receivedloandata.getIntrate()` · "LoanType" → `0.0d` · "CollectionLineId" → `0.0d` · "TransId" → `receivedloandata.getServerGenTransId()` · "DCPercent" → `0.0d` · "DocCharges" → `receivedloandata.getDocCharges()` · "LoanMethodId" → `receivedloandata.getLoanMethodId()` · "InterestAmount" → `receivedloandata.getInterestAmount()` · "MCLoanAmountCollectable" → `receivedloandata.getMCLoanAmountCollectable()` · "InsAmount" → `receivedloandata.getInsAmount()` · "MCLoanAmountEstimated" → `receivedloandata.getMCLoanAmountCollectable()` · "RecPartyLedgerId" → `4` · "Remarks" → `receivedloandata.getRemarks()` · "WeekDayId" → `receivedloandata.getWeekDayId()` · "MonthDayId" → `receivedloandata.getMonthDayid()` · "mobileno" → `receivedloandata.getMobileno()` · "Sourceledgerid" → `receivedloandata.getSourceledgerid()` · "PartyReferenced" → `receivedloandata.getPartyreference()` · "loanindex" → `receivedloandata.getLoanindex()` · "Productvalue" → `receivedloandata.getProductsvalue()` · "AdvanceAmount" → `receivedloandata.getAdvanceAmount()`
  OUT: "MCLoanId" (8)
  Reads: `MCLoanId`
- [`uploadcollection.ASYNCUpload.doInBackground:1115`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblMCLoanswithWeekdayId(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ,?,?,?,?,?,?)}
  ```
  Params: "MCLoanDate" → `newloan2.getMCLoanDate()` · "FWMCLoanId" → `4` · "FWUDMCLoanId" → `4` · "FinId" → `globalvars.finid.intValue()` · "PartyLedgerId" → `newloan2.getServerGenLedgerId()` · "AgentLedgerId" → `4` · "GuaranderLedgerID" → `4` · "RecPartnerLedgerID" → `4` · "MCLoanAmount" → `newloan2.getMCLoanAmount()` · "TotalMonths" → `newloan2.getTotalMonths()` · "IntRate" → `newloan2.getIntrate()` · "LoanType" → `0.0d` · "CollectionLineId" → `0.0d` · "TransId" → `newloan2.getServerGenTransId()` · "DCPercent" → `0.0d` · "DocCharges" → `newloan2.getDocCharges()` · "LoanMethodId" → `newloan2.getLoanMethodId()` · "InterestAmount" → `newloan2.getInterestAmount()` · "MCLoanAmountCollectable" → `newloan2.getMCLoanAmountCollectable()` · "InsAmount" → `newloan2.getInsAmount()` · "MCLoanAmountEstimated" → `newloan2.getMCLoanAmountCollectable()` · "RecPartyLedgerId" → `4` · "Remarks" → `newloan2.getRemarks()` · "WeekDayId" → `newloan2.getWeekDayId()` · "MonthDayId" → `newloan2.getMonthDayid()` · "mobileno" → `newloan2.getMobileno()` · "Sourceledgerid" → `newloan2.getSourceledgerid()` · "PartyReferenced" → `newloan2.getPartyreference()` · "loanindex" → `newloan2.getLoanindex()` · "Productvalue" → `newloan2.getProductsvalue()` · "AdvanceAmount" → `newloan2.getAdvanceAmount()`
  OUT: "MCLoanId" (8)
  Reads: `MCLoanId`

## `inserttblofloanint`

- [`ofloans_interestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:777`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblOFLoanInt(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TransDate" → `defaulttransdate` · "OFLoanId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "PartyLedgerId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "SNo" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "CurOFLoanAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "PrincipalAmount" → `4` · "OFLoanAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "CalcInt" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "PaidInt" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "Days" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "IntFrom" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "IntUpTo" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "PrincipalAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "OFLoanAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "CalcInt" → `4` · "PaidInt" → `4` · "Days" → `4` · "IntFrom" → `91` · "IntUpTo" → `91` · "FinId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "FinSCashLedgerId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TargetLedgerId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "IntRate" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · str3 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "SetId" → `selectedSETId`
  OUT: "OFLIntPayid" (8)
  Reads: `OFLIntPayid`

## `inserttblofloans`

- [`ofloans.ASYNCSaveLoan.doInBackground:1033`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblOFLoans(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblOFLoans_newdata.getTransId()` · "TransDate" → `tblOFLoans_newdata.getTransDate()` · "FWUDOFLoanId" → `tblOFLoans_newdata.getFWUDOFLoanId()` · "PartyLedgerId" → `tblOFLoans_newdata.getPartyLedgerId()` · "OFLoanAmount" → `tblOFLoans_newdata.getOFLoanAmount()` · "OFLIntRateinDM" → `tblOFLoans_newdata.getOFLIntRateinDM().doubleValue()` · "OFLIntRateinMM" → `tblOFLoans_newdata.getOFLIntRateinMM().doubleValue()` · "RecPartnerId" → `tblOFLoans_newdata.getRecPartnerId()` · "FinId" → `tblOFLoans_newdata.getFinId()` · "IntCalcStartOn" → `tblOFLoans_newdata.getIntCalcStartOn()` · "IntCalcStartOnSYSSET" → `tblOFLoans_newdata.getIntCalcStartOn()` · "CurStat" → `tblOFLoans_newdata.getCurStat().booleanValue()` · "Remarks" → `tblOFLoans_newdata.getRemarks()` · "BankRefId" → `tblOFLoans_newdata.getBankRefId()` · "BankRefNo" → `tblOFLoans_newdata.getBankRefNo()` · "BankRefId" → `4` · "BankRefNo" → `4` · "DocCharges" → `tblOFLoans_newdata.getDocCharges()` · "SourceLedgerId" → `tblOFLoans_newdata.getCashorbankledgerid()`
  OUT: "OFLoanId" (8) · "FWUDOFLoanId" (8)
  Reads: `OFLoanId`, `FWUDOFLoanId`

## `inserttblofloanswithuserdefinedloanno`

- [`ofloans.ASYNCSaveLoan.doInBackground:1030`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblOFLoanswithUserDefinedLoanNo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```

## `inserttblstloanint`

- [`stloaninterestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:765`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblSTLoanInt(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` · "TransDate" → `defaulttransdate` · "STLoanId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getSTLoanId()` · "PartyLedgerId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getPartyLedgerI…` · "SNo" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getSNo()` · "CurSTLoanAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCurrentloana…` · "PrincipalAmount" → `4` · "STLoanAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getInterestcalc…` · "CalcInt" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCalcInt().in…` · "PaidInt" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` · "Days" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getDays()` · "IntFrom" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntFrom()` · "IntUpTo" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntUpto()` · "PrincipalAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` · "STLoanAmount" → `4` · "CalcInt" → `4` · "PaidInt" → `4` · "Days" → `4` · "IntFrom" → `91` · "IntUpTo" → `91` · "FinId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getFinId()` · "FinSCashLedgerId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCashLedgerId…` · "TargetLedgerId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTargetLedger…` · "IntRate" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntRate().do…` · "Narration" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getNarration()` · "SetId" → `selectedSETId`
  OUT: "STLIntPayid" (8)
  Reads: `STLIntPayid`

## `inserttblstloans`

- [`stloans.ASYNCSaveLoan.doInBackground:1530`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblSTLoans(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblSTLoans_newdata.getTransId()` · "TransDate" → `tblSTLoans_newdata.getTransDate()` · "FWSTLoanId" → `tblSTLoans_newdata.getFWSTLoanId()` · "FWUDSTLoanId" → `tblSTLoans_newdata.getFWUDSTLoanId()` · "PartyLedgerId" → `tblSTLoans_newdata.getPartyLedgerId()` · "STLoanAmount" → `tblSTLoans_newdata.getSTLoanAmount()` · "STLIntRateinDM" → `tblSTLoans_newdata.getSTLIntRateinDM().doubleValue()` · "STLIntRateinMM" → `tblSTLoans_newdata.getSTLIntRateinMM().doubleValue()` · "STLPriIntRateinDM" → `8` · "STLSecIntRateinDM" → `8` · "STLTriIntRateinDM" → `8` · "RecPartnerId" → `tblSTLoans_newdata.getRecPartnerId()` · "RecPartyId" → `tblSTLoans_newdata.getRecPartyId()` · "RecPartyId" → `8` · "FinId" → `tblSTLoans_newdata.getFinId()` · "DocCharges" → `tblSTLoans_newdata.getDocCharges()` · "Remarks" → `tblSTLoans_newdata.getRemarks()` · "IntCalcStartOn" → `tblSTLoans_newdata.getIntCalcStartOn()` · "IntCalcStartOnSYSSET" → `tblSTLoans_newdata.getIntCalcStartOn()` · "CurStat" → `tblSTLoans_newdata.getCurStat().booleanValue()` · "BankRefId" → `tblSTLoans_newdata.getBankRefId()` · "BankRefNo" → `tblSTLoans_newdata.getBankRefNo()` · "BankRefId" → `4` · "BankRefNo" → `4` · "SourceLedgerId" → `tblSTLoans_newdata.getSourceLedgerId()` · "DCTargetLedgerId" → `tblSTLoans_newdata.getDCTargetLedgerId()`
  OUT: "STLoanId" (8) · "FWUDSTLoanId" (8)
  Reads: `STLoanId`, `FWUDSTLoanId`

## `inserttblstloanswithuserdefinedloanno`

- [`stloans.ASYNCSaveLoan.doInBackground:1527`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblSTLoanswithUserDefinedLoanNo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```

## `inserttbltransactionsdetails`

- [`ofloans.ASYNCSaveLoan.doInBackground:1201`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblOFLoans_newdata.getTransId()` · "Sno" → `1.0d` · str6 → `tblOFLoans_newdata.getPartyLedgerId()` · str7 → `"Source"` · str8 → `tblOFLoans_newdata.getOFLoanAmount()` · str9 → `"Cr"` · str10 → `2.0d`
- [`ofloans.ASYNCSaveLoan.doInBackground:1215`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblOFLoans_newdata.getTransId()` · "Sno" → `2.0d` · str6 → `tblOFLoans_newdata.getCashorbankledgerid()` · str7 → `"Target"` · str8 → `tblOFLoans_newdata.getOFLoanAmount()` · str9 → `"Dr"` · str10 → `1.0d`
- [`ofloans.ASYNCSaveLoan.doInBackground:1225`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblOFLoans_newdata.getTransId()` · "Sno" → `3.0d` · str6 → `tblOFLoans_newdata.getCashorbankledgerid()` · str7 → `"Source"` · str8 → `tblOFLoans_newdata.getDocCharges()` · str9 → `"Cr"` · str10 → `4.0d`
- [`ofloans.ASYNCSaveLoan.doInBackground:1234`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblOFLoans_newdata.getTransId()` · "Sno" → `4.0d` · str6 → `104.0d` · str7 → `"Target"` · str8 → `tblOFLoans_newdata.getDocCharges()` · str9 → `"Dr"` · str10 → `3.0d`
- [`ofloans_interestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:811`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TransId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "Sno" → `1.0d` · str4 → `"Target"` · "LedgerAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "AmtType" → `"Dr"` · "AgnstLedgerSNo" → `2.0d`
- [`ofloans_interestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:821`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TransId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "Sno" → `2.0d` · str4 → `"Source"` · "LedgerAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "AmtType" → `"Cr"` · "AgnstLedgerSNo" → `1.0d`
- [`ofloans_interestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:872`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TransId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "Sno" → `1.0d` · "LedgerStatus" → `"Target"` · "LedgerAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "AmtType" → `"Dr"` · "AgnstLedgerSNo" → `2.0d`
- [`ofloans_interestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:881`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TransId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "Sno" → `2.0d` · "LedgerStatus" → `"Source"` · "LedgerAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "AmtType" → `"Cr"` · "AgnstLedgerSNo" → `1.0d`
- [`stloaninterestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:799`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCashLedgerId…` · "TransId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` · "Sno" → `1.0d` · "LedgerStatus" → `"Target"` · str2 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` · "AmtType" → `"Dr"` · "AgnstLedgerSNo" → `2.0d`
- [`stloaninterestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:809`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTargetLedger…` · "TransId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` · "Sno" → `2.0d` · "LedgerStatus" → `"Source"` · str2 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` · "AmtType" → `"Cr"` · "AgnstLedgerSNo" → `1.0d`
- [`stloaninterestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:855`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCashLedgerId…` · "TransId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` · "Sno" → `1.0d` · "LedgerStatus" → `"Target"` · "LedgerAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` · "AmtType" → `"Dr"` · "AgnstLedgerSNo" → `2.0d`
- [`stloaninterestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:864`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
  ```
  Params: "ledgerid" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTargetLedger…` · "TransId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` · "Sno" → `2.0d` · "LedgerStatus" → `"Source"` · "LedgerAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` · "AmtType" → `"Cr"` · "AgnstLedgerSNo" → `1.0d`
- [`stloans.ASYNCSaveLoan.doInBackground:1638`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblSTLoans_newdata.getTransId()` · "Sno" → `1.0d` · "LedgerId" → `tblSTLoans_newdata.getSourceLedgerId()` · "LedgerStatus" → `"Source"` · str4 → `tblSTLoans_newdata.getSTLoanAmount()` · str5 → `"Cr"` · str → `2.0d`
- [`stloans.ASYNCSaveLoan.doInBackground:1649`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblSTLoans_newdata.getTransId()` · "Sno" → `2.0d` · "LedgerId" → `tblSTLoans_newdata.getPartyLedgerId()` · "LedgerStatus" → `"Target"` · str4 → `tblSTLoans_newdata.getSTLoanAmount()` · str5 → `"Dr"` · str → `1.0d`
- [`stloans.ASYNCSaveLoan.doInBackground:1750`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblSTLoans_newdata.getTransId()` · "Sno" → `1.0d` · "LedgerId" → `tblSTLoans_newdata.getSourceLedgerId()` · "LedgerStatus" → `"Source"` · str42 → `tblSTLoans_newdata.getSTLoanAmount()` · str52 → `"Cr"` · str → `2.0d`
- [`stloans.ASYNCSaveLoan.doInBackground:1761`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblSTLoans_newdata.getTransId()` · "Sno" → `2.0d` · "LedgerId" → `tblSTLoans_newdata.getPartyLedgerId()` · "LedgerStatus" → `"Target"` · str42 → `tblSTLoans_newdata.getSTLoanAmount()` · str52 → `"Dr"` · str → `1.0d`
- [`stloans.ASYNCSaveLoan.doInBackground:1771`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblSTLoans_newdata.getTransId()` · "Sno" → `3.0d` · "LedgerId" → `104.0d` · "LedgerStatus" → `"Source"` · str42 → `tblSTLoans_newdata.getDocCharges()` · str52 → `"Cr"` · str → `4.0d`
- [`stloans.ASYNCSaveLoan.doInBackground:1780`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
  ```
  Params: "TransId" → `tblSTLoans_newdata.getTransId()` · "Sno" → `4.0d` · "LedgerId" → `tblSTLoans_newdata.getDCTargetLedgerId()` · "LedgerStatus" → `"Target"` · str42 → `tblSTLoans_newdata.getDocCharges()` · str52 → `"Dr"` · str → `3.0d`

## `inserttbltransactionsmaster`

- [`directcollectionentry.asyncsavecollection.doInBackground:833`](04-offline-collection-entry-area-wise-collection.md#directcollectionentry) · `jdbc.prepareCall` · branch DB
  ```sql
  {call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `collectiondate` · "TransType" → `constantvalues.MCLoanCollectionTTId.intValue()` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `penaltycollection + savingscollection + duecollection` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · "GColId" → `8` · "TransLedgerId" → `8` · "RouteId" → `globalvars.finid.intValue()`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`
- [`editing_collectionswithfulloptions_act.AsyncDownloaddata.doInBackground:1541`](05-online-collection-entry-editing.md#editingcollectionswithfulloptionsact) · `jdbc.prepareCall` · branch-report DB
  ```sql
  {call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `selectedcollectiondate` · "TransType" → `constantvalues.MCLoanCollectionTTId.intValue()` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `0.0d` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · "GColId" → `8` · "TransLedgerId" → `8` · "RouteId" → `globalvars.finid.intValue()`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`
- [`newloancreation.ASYNCUpdateLoaninSQLDB.doInBackground:1493`](07-parties-borrowers-new-loan-creation-kyc.md#newloancreation) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `receivedloandata.getMCLoanDate()` · "TransType" → `21.0d` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `receivedloandata.getMCLoanAmountCollectable()` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · "GColId" → `8` · "TransLedgerId" → `receivedloandata.getServerGenMCLoanId()` · "RouteId" → `globalvars.finid.intValue()`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`
- [`uploadcollection.ASYNCUpload.doInBackground:853`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `String.valueOf(globalvars.collectiondate)` · "TransType" → `constantvalues.MCLoanCollectionTTId.intValue()` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `PenaltyCollectedTotal + SavingsCollectedTotal + DueCollectionTotal` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · str14 → `8` · "TransLedgerId" → `8` · "RouteId" → `globalvars.finid.intValue()`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`
- [`uploadcollection.ASYNCUpload.doInBackground:1095`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `newloan2.getMCLoanDate()` · "TransType" → `21.0d` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `newloan2.getMCLoanAmount()` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · "GColId" → `8` · "TransLedgerId" → `newloan2.getServerGenLedgerId()` · "RouteId" → `globalvars.finid.intValue()`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`
- [`uploadcollection.ASYNCUpload.doInBackground:1476`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `String.valueOf(globalvars.collectiondate)` · "TransType" → `constantvalues.MCLoanCollectionTTId.intValue()` · "Narration" → `""` · "FinId" → `globalvars.finid.intValue()` · "TransAmount" → `PenaltyCollectedTotal + SavingsCollectedTotal + DueCollectionTotal` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · str14 → `8` · "TransLedgerId" → `8` · "RouteId" → `globalvars.finid.intValue()`
  OUT: "TransId" (8) · "FWVWTransId" (8)
  Reads: `TransId`
- [`uploadcollection.ASYNCUpload.doInBackground:1669`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `String.valueOf(globalvars.collectiondate)` · "TransType" → `TTId.intValue()` · "Narration" → `Narration` · str6 → `globalvars.finid.intValue()` · "TransAmount" → `Amount.doubleValue()` · "FWTransId" → `8` · "FWVWUDTransId" → `8` · str9 → `MCLGCollId` · str9 → `8` · "TransLedgerId" → `LedgerId.doubleValue()` · "RouteId" → `globalvars.finid.intValue()` · "Sourceledgerid" → `globalvars.cashbookledgerid.intValue()`
  OUT: str24 (8) · "FWVWTransId" (8)
  Reads: `OUT#str7`

## `inserttbltransactionsmasterformonthly`

- [`ofloans.ASYNCSaveLoan.doInBackground:1006`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsMasterForMonthly( ?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `tblOFLoans_newdata.getTransDate()` · "TransType" → `5.0d` · "Narration" → `12` · "FinId" → `tblOFLoans_newdata.getFinId()` · "TransAmount" → `tblOFLoans_newdata.getOFLoanAmount()` · "TGWTID" → `TGWTId` · "TGFWTId" → `TGFWTId`
  OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
  Reads: `TransId`
- [`stloans.ASYNCSaveLoan.doInBackground:1501`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InserttblTransactionsMasterForMonthly(?,?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `tblSTLoans_newdata.getTransDate()` · "TransType" → `3.0d` · "Narration" → `12` · "FinId" → `tblSTLoans_newdata.getFinId()` · "TransAmount" → `tblSTLoans_newdata.getSTLoanAmount()` · "TGWTID" → `TGWTId` · "TGFWTId" → `TGFWTId`
  OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
  Reads: `TransId`

## `inserttransserial`

- [`ofloans.ASYNCSaveLoan.doInBackground:995`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertTransSerial(?,?,?,?,?,?)}
  ```
  Params: "FinId" → `tblOFLoans_newdata.getFinId()` · "TTypeId" → `1` · "TransDate" → `tblOFLoans_newdata.getTransDate()`
  OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
  Reads: `TSId`, `TGWTId`, `TGFWTId`
- [`ofloans_interestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:728`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertTransSerial(?,?,?,?,?,?)}
  ```
  Params: "FinId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` · "TTypeId" → `1` · "TransDate" → `defaulttransdate`
  OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
  Reads: `TSId`, `TGWTId`, `TGFWTId`
- [`stloaninterestcalculationandpayments.ASYNCSAVECollectedIntandPriDetails.doInBackground:714`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertTransSerial(?,?,?,?,?,?)}
  ```
  Params: "FinId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getFinId()` · "TTypeId" → `1` · "TransDate" → `defaulttransdate`
  OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
  Reads: `TSId`, `TGWTId`, `TGFWTId`
- [`stloans.ASYNCSaveLoan.doInBackground:1490`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.InsertTransSerial(?,?,?,?,?,?)}
  ```
  Params: "FinId" → `tblSTLoans_newdata.getFinId()` · "TTypeId" → `1` · "TransDate" → `tblSTLoans_newdata.getTransDate()`
  OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
  Reads: `TSId`, `TGWTId`, `TGFWTId`

## `ledgerwisebalance`

- [`simplethittamlist_act.ASYNCGetAbstractlist.doInBackground:300`](12-thittam-collection-plan-weekly-analysis-reports.md#simplethittamlistact) · `jdbc.Statement.execute` · branch-report DB
  ```sql
  exec LedgerwiseBalance @CCom =${financeid},@LedId=${((spinnersearch_data) arrayList.get(i)).getValuemember()} ,@FDate='${todate_patti}' ,@TDate='${todate_patti}'
  ```
- [`simplethittamlist_act.ASYNCGetAbstractlist.doInBackground:322`](12-thittam-collection-plan-weekly-analysis-reports.md#simplethittamlistact) · `jdbc.Statement.execute` · branch-report DB
  ```sql
  exec LedgerwiseBalance @CCom =${financeid},@LedId=${((spinnersearch_data) arrayList.get(i2)).getValuemember()} ,@FDate='${fromdate_patti}' ,@TDate='${todate_patti}'
  ```

## `loanclosedandrenewedforwindowsapp`

- [`closed_renewedloanslist_act.ASYNCGetClosedandRenewedloans.doInBackground:450`](08-loan-lists-loan-savings-closures-party.md#closedrenewedloanslistact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec LoanClosedandRenewedforwindowsAPP @fromDate='${fromdate}',  @ToDate='${todate}', @FinId=${selectedfinanceid}
  ```

## `loanclosedandrenewedwithweekdayid`

- [`closed_renewedloanslist_act.ASYNCGetClosedandRenewedloans.doInBackground:452`](08-loan-lists-loan-savings-closures-party.md#closedrenewedloanslistact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec LoanClosedandRenewedwithweekdayid  @weekdayid= ${weekdayid}, @fromDate='${fromdate}',  @ToDate='${todate}', @FinId=${selectedfinanceid}
  ```
  Reads: `PartyLedgerId`, `MCLoanId`, `SNo`, `SubAreaName`, `LedgerName`, `LoanIndex`, `CWLedgerId`, `MCLoanDate`, `MCLoanAmountCollectable`, `Collected`, `LastCollDate`, `LastCollection`, `NextLoanDate`, `NextLoanAmount`

## `monthlyplincomes`

- [`monthlypandl_fragment.ASYNCGetMonthlypandl.doInBackground:253`](10-accounts-reports-trial-balance-balance-sheet.md#monthlypandlfragment) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec [MonthlyPLIncomes] @SelectedFinanceIds='${selectecdfinanceids}',  @FDate ='${fromdate}',  @RDate ='${todate}'
  ```

## `newpartiesnewloans`

- [`newpartiesnewloanslist_act.ASYNCGetNewpartiesNewLoans.doInBackground:291`](08-loan-lists-loan-savings-closures-party.md#newpartiesnewloanslistact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec NewPartiesNewLoans @fromDate='${curdaterange.getFromdate()}',  @ReportDate='${curdaterange.getTodate()}', @IMEI='${globalvars.device_id}', @FinId=${financeid}
  ```

## `newpartiesnewloanswithweekdayid`

- [`newpartiesnewloanslist_act.ASYNCGetNewpartiesNewLoans.doInBackground:293`](08-loan-lists-loan-savings-closures-party.md#newpartiesnewloanslistact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec NewPartiesNewLoanswithweekdayid  @weekdayid= ${weekdayid}, @fromDate='${curdaterange.getFromdate()}',  @ReportDate='${curdaterange.getTodate()}', @IMEI='${globalvars.device_id}', @FinId=${financeid}
  ```
  Reads: `SubAreaName`, `LedgerName`, `CWLedgerId`, `PartyLedgerId`, `MCLoanId`, `MCLoanDate`, `InterestAmount`, `MCLoanAmountCollectable`, `InsAmount`, `TotalMonths`, `MobileNo`

## `niploansclosed`

- [`closed_renewedloanslist_act.ASYNCGetClosedandRenewedloans.doInBackground:444`](08-loan-lists-loan-savings-closures-party.md#closedrenewedloanslistact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec NIPLoansClosed @IMEI='${globalvars.device_id}',  @FinId=${selectedfinanceid}
  ```

## `ofintcolpending`

- [`meetingreport.ASYNC_GetInterestPendingList.doInBackground:661`](11-daily-patti-outstanding-reports-meeting-report.md#meetingreport) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec OFIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
  ```
- [`partnerwiseloansandperformance_fragment.ASYNC_GetInterestPendingList.doInBackground:919`](15-finance-transactions-lists-party-loan-ledgers.md#partnerwiseloansandperformancefragment) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec ofIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
  ```

## `ofintcolpendinglist`

- [`meetingreport.ASYNC_GetInterestPendingList.doInBackground:666`](11-daily-patti-outstanding-reports-meeting-report.md#meetingreport) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec OFIntColPendingList
  ```
- [`partnerwiseloansandperformance_fragment.ASYNC_GetInterestPendingList.doInBackground:923`](15-finance-transactions-lists-party-loan-ledgers.md#partnerwiseloansandperformancefragment) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec ofIntColPendingList
  ```

## `ofl_prepareintpricolllist`

- [`ofloans_interestcalculationandpayments.ASYNCindividualloandetails.doInBackground:515`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec OFL_PrepareIntPriCollList @isAutoIncEnabled=false, @IntRecptDate='${interestcalculation_uptodate}',@PartyLedId=${receivedparty.getLedgerId()}
  ```

## `ofl_prepareintpricolllistoverallpenalty`

- [`ofloans_interestcalculationandpayments.ASYNCindividualloandetails.doInBackground:517`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec OFL_PrepareIntPriCollListOverAllPenalty @isAutoIncEnabled=false, @IntRecptDate='${interestcalculation_uptodate}',@PartyLedId=${receivedparty.getLedgerId()}
  ```
  Reads: `PartyLedgerId`, `OFLoanId`, `FinId`, `CashLedgerId`, `IntRate`, `PenaltyRate`, `PType`, `SNo`, `ShortName`, `Loan`, `LedgerName`, `FWUDOFLoanId`, `IntFrom`, `IntUpto`, `Days`, `IsAutoIncEnabled`, `CalcInt`, `CutOffDate`

## `ofl_showindoflstatus`

- [`ofloans.ASYNCindividualloandetails.doInBackground:764`](14-st-loans-of-loans-interest-bearing.md#ofloans) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec OFL_ShowIndOFLStatus @PartyLedgerId= ${transdata.getLedgerId()}
  ```
  Reads: `PartyLedgerId`, `TransId`, `OFLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`, `TGFWTId`, `TGWTId`
- [`ofloans_interestcalculationandpayments.ASYNCindividualloandetails.doInBackground:492`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec OFL_ShowIndOFLStatus @PartyLedgerId= ${receivedparty.getLedgerId()}
  ```
  Reads: `PartyLedgerId`, `TransId`, `OFLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`

## `oflint_dispintpaydetails`

- [`ofloans_interestcalculationandpayments.ASYNCGetCollectedIntandPriDetails.doInBackground:620`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  execute OFLInt_DispIntpayDetails @PartyLedId= ${receivedparty.getLedgerId()},@SetId=${selectedSETId}
  ```
  Reads: `PartyLedgerId`, `OFLoanId`, `FinId`, `TransDate`, `IntRate`, `CashLedgerId`, `PType`, `SNo`, `ShortName`, `LedgerName`, `FWUDOFLoanId`, `Loan`, `IntFrom`, `IntUpTo`, `Days`, `CalcInt`, `Paid`, `TransId`, `OFLIntPayId`, `PrincipalAmount`, `TargetLedgerId`, `FWTransId`, `TGWTId`, `TGFWTId`, `Narration`

## `ofloanledgers`

- [`loanledgers_list.ASYNC_GetLoanLedgers.doInBackground:247`](15-finance-transactions-lists-party-loan-ledgers.md#loanledgerslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec OFLoanLedgers @FromDate = '${fromdate}', @ToDate = '${todate}' , @SelectedFinanceIds='${selectecdfinanceids}'
  ```
  Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`
- [`partyledgers_parameters.ASYNC_GetInterestPendingList.doInBackground:918`](15-finance-transactions-lists-party-loan-ledgers.md#partyledgersparameters) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec OFLoanLedgers @CFD = '${globalvars.accyearfromdate}' , @SelectedFinanceIds='${selectecdfinanceids}'
  ```
  Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`

## `pattiproc_01nadappuandnippuphakki`

- [`dailypattinew_act.ASYNCGetAbstractlist.doInBackground:384`](11-daily-patti-outstanding-reports-meeting-report.md#dailypattinewact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec PattiProc_01NadappuandNippuPhakki @FinId =${financeid},@ReportDate='${fromdate_patti}'
  ```
  Reads: `MCLoanDate`, `ReportIdentification`, `LedgerName`, `MCLoanAmountCollectable`, `Collected`, `Balance`, `ClosingDate`, `IsNIP`, `SubAreaNo`, `SubAreaName`, `PrevCollection`, `Collection`, `ExtraCollection`, `CollCount`, `ClosedDate`, `collected`, `Pending`, `Party`, `LT`, `Loan`, `Dues`, `IntRate`, `Interest`, `Mahimai`

## `pattiproc_02nadapunippucollection`

- [`dailypattinew_act.ASYNCGetAbstractlist.doInBackground:425`](11-daily-patti-outstanding-reports-meeting-report.md#dailypattinewact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec PattiProc_02NadapuNippuCollection @FinId =${financeid},@FromColDate='${fromdate_patti}',@ToColDate='${todate_patti}'
  ```

## `pattiproc_03varakadanillappu`

- [`dailypattinew_act.ASYNCGetAbstractlist.doInBackground:492`](11-daily-patti-outstanding-reports-meeting-report.md#dailypattinewact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec PattiProc_03VarakadanIllappu @FinId =${financeid},@FromColDate='${fromdate_patti}',@ToColDate='${todate_patti}'
  ```

## `pattiproc_04kadanpatru`

- [`dailypattinew_act.ASYNCGetAbstractlist.doInBackground:547`](11-daily-patti-outstanding-reports-meeting-report.md#dailypattinewact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec PattiProc_04KadanPatru @FinId =${financeid},@FromDate='${fromdate_patti}',@ToDate='${todate_patti}'
  ```

## `preparedashboardcash`

- [`cashbookabstract.ASYNCGetCashAbstract.doInBackground:160`](13-accounts-transactions-cash-bank-book-expenses.md#cashbookabstract) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec PrepareDashBoardCash
  ```
  Reads: `GroupId`, `Grouping`, `LedgerId`, `FinId`, `Ledger`, `Balance`

## `preparepattiiiweekly`

- [`weeklycollectionpatti_dashboard.asyncthittamstage1.doInBackground:94`](12-thittam-collection-plan-weekly-analysis-reports.md#weeklycollectionpattidashboard) · `jdbc.prepareStatement` · branch-report DB
  ```sql
  EXEC PreparePattiIIWeekly
  ```
  Reads: `PartyName`, `Subareaname`, `Loan`, `MCLoanId`, `Balance`, `Collected`

## `rep24weekcollection`

- [`varathittam.AsyncWeeklyThittam.doInBackground:739`](12-thittam-collection-plan-weekly-analysis-reports.md#varathittam) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec Rep24WeekCollection @NipLimit=${softsettingsvalue.nipbreakforweeklythittam_14},@FinIdN=${financeid}, @AreaNo=0, @RDate='${thittamdate}',@ColWeekToDate='${thittamdate}',@ColWeekFromDate='${thittamdate}'
  ```
- [`weeklycollectionanalysis_act.AsyncWeeklyThittam.doInBackground:172`](12-thittam-collection-plan-weekly-analysis-reports.md#weeklycollectionanalysisact) · `jdbc.Statement.execute` · branch DB
  ```sql
  exec Rep24WeekCollection @NipLimit=${softsettingsvalue.nipbreakforweeklythittam_14},@FinIdN=${financeid}, @AreaNo=0, @RDate='${thittamdate}',@ColWeekToDate='${thittamdate}',@ColWeekFromDate='${thittamdate}'
  ```

## `setattentionrequired`

- [`sevendayscollectionforselline.ASYNCupdateemployee.doInBackground:260`](06-collection-reports-collection-day-abstract-7.md#sevendayscollectionforselline) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.SetAttentionRequired(?)}
  ```
  Params: "MCLoanIdArray" → `MCLoanIdArray.toString()`

## `showexpensesoftheweek`

- [`varathittam.AsyncWeeklyThittam.doInBackground:782`](12-thittam-collection-plan-weekly-analysis-reports.md#varathittam) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec ShowExpensesoftheWeek @FinId=${financeid}
  ```
  Reads: `LedgerName`, `Amount`, `LedgerAmount`

## `showincomesoftheweek`

- [`varathittam.AsyncWeeklyThittam.doInBackground:807`](12-thittam-collection-plan-weekly-analysis-reports.md#varathittam) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec ShowIncomesoftheWeek @FinId=${financeid}
  ```
  Reads: `LedgerName`, `Amount`

## `showoflintlist`

- [`ofloans_interestcalculationandpayments.ASYNCindividualloandetails.doInBackground:556`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec ShowOFLIntList @PartyLedId= ${receivedparty.getLedgerId()},@CurPF='${globalvars.accyearfromdate}' ,@CurPT='${globalvars.accyeartodate}'
  ```
  Reads: `SetId`, `SNo`, `TransDate`, `Principal`, `int`

## `showstlintlist`

- [`stloaninterestcalculationandpayments.ASYNCindividualloandetails.doInBackground:542`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec ShowSTLIntList @PartyLedId= ${receivedparty.getLedgerId()},@CurPF='${globalvars.accyearfromdate}' ,@CurPT='${globalvars.accyeartodate}'
  ```
  Reads: `SetId`, `SNo`, `TransDate`, `Principal`, `Int`

## `simplethittam`

- [`simplethittamlist_act.ASYNCGetAbstractlist.doInBackground:361`](12-thittam-collection-plan-weekly-analysis-reports.md#simplethittamlistact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec SimpleThittam @FromDate='${fromdate_patti}',@ToDate='${todate_patti}',@FinId=${financeid}
  ```
  Reads: `ShowType`, `TypeId`, `TTId`, `Particulars`, `debit`, `credit`, `ledgerid`, `Ledgername`

## `singlefinancecashbook`

- [`ASYNCGetsingleFinanceCashBook.doInBackground:24`](12-thittam-collection-plan-weekly-analysis-reports.md#asyncgetsinglefinancecashbook) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec singleFinanceCashBook @IMEI='${globalvars.device_id}',@ReportDate  ='${financeids[1]}', @FinId =${financeids[0]}
  ```
  Reads: `SNo`, `ShowId`, `TransId`, `FinId`, `LedgerId`, `Ledgername`, `Particulars`, `IsNewLoan`, `TransDate`, `TransType`, `Debit`, `Credit`, `Narration`
- [`allfinancedailythittam.PlaceholderFragment.ASYNCGetsingleFinanceCashBook.doInBackground:170`](12-thittam-collection-plan-weekly-analysis-reports.md#allfinancedailythittam) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec singleFinanceCashBook @IMEI='${globalvars.device_id}',@ReportDate  ='${allfinancedailythittam.fromdate_patti}', @FinId =${allfinancedailythittam.newdata.getFinId()}
  ```
  Reads: `SNo`, `ShowId`, `TransId`, `FinId`, `LedgerId`, `Ledgername`, `Particulars`, `IsNewLoan`, `TransDate`, `TransType`, `Debit`, `Credit`, `Narration`
- [`singlefinancedailythittam_act.ASYNCGetsingleFinanceCashBook.doInBackground:167`](12-thittam-collection-plan-weekly-analysis-reports.md#singlefinancedailythittamact) · `jdbc.Statement.executeQuery` · branch-report DB
  ```sql
  exec singleFinanceCashBook @IMEI='${globalvars.device_id}',@ReportDate  ='${fromdate_patti}', @FinId =${financeid}
  ```
  Reads: `SNo`, `ShowId`, `TransId`, `FinId`, `LedgerId`, `Ledgername`, `Particulars`, `IsNewLoan`, `TransDate`, `TransType`, `Debit`, `Credit`, `Narration`

## `stl_prepareintpricolllist`

- [`stloaninterestcalculationandpayments.ASYNCindividualloandetails.doInBackground:496`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec STL_PrepareIntPriCollList @isAutoIncEnabled=false, @IntRecptDate='${interestcalculation_uptodate}',@PartyLedId=${receivedparty.getLedgerId()}
  ```
  Reads: `PartyLedgerId`, `STLoanId`, `FinId`, `CashLedgerId`, `IntRate`, `PenaltyRate`, `PType`, `SNo`, `ShortName`, `Loan`, `LedgerName`, `FWUDSTLoanId`, `IntFrom`, `IntUpto`, `Days`, `IsAutoIncEnabled`, `CalcInt`, `CutOffDate`

## `stl_showindstlstatus`

- [`stloaninterestcalculationandpayments.ASYNCindividualloandetails.doInBackground:477`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec STL_ShowIndSTLStatus @PartyLedgerId= ${receivedparty.getLedgerId()}
  ```
  Reads: `PartyLedgerId`, `TransId`, `STLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`
- [`stloans.ASYNCindividualloandetails.doInBackground:1256`](14-st-loans-of-loans-interest-bearing.md#stloans) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec STL_ShowIndSTLStatus @PartyLedgerId= ${curpartyledgerid}
  ```
  Reads: `PartyLedgerId`, `TransId`, `STLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`, `TGFWTId`, `TGWTId`

## `stlint_dispintpaydetails`

- [`stloaninterestcalculationandpayments.ASYNCGetCollectedIntandPriDetails.doInBackground:606`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  execute STLInt_DispIntpayDetails @PartyLedId= ${receivedparty.getLedgerId()},@SetId=${selectedSETId}
  ```
  Reads: `PartyLedgerId`, `STLoanId`, `FinId`, `CurSTLoanAmount`, `TransDate`, `IntRate`, `CashLedgerId`, `PType`, `SNo`, `ShortName`, `LedgerName`, `FWUDSTLoanId`, `Loan`, `IntFrom`, `IntUpTo`, `Days`, `CalcInt`, `Received`, `TransId`, `STLIntPayId`, `FWSTLIntId`, `PrincipalAmount`, `TargetLedgerId`, `FWTransId`, `TGWTId`, `TGFWTId`, `Narration`

## `stlintcolpending`

- [`meetingreport.ASYNC_GetInterestPendingList.doInBackground:540`](11-daily-patti-outstanding-reports-meeting-report.md#meetingreport) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec STLIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
  ```
- [`partnerwiseloansandperformance_fragment.ASYNC_GetInterestPendingList.doInBackground:772`](15-finance-transactions-lists-party-loan-ledgers.md#partnerwiseloansandperformancefragment) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec STLIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
  ```
  Reads: `DOWNLOADDatetime`

## `stlintcolpendinglist`

- [`meetingreport.ASYNC_GetInterestPendingList.doInBackground:545`](11-daily-patti-outstanding-reports-meeting-report.md#meetingreport) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec STLIntColPendingList
  ```
- [`partnerwiseloansandperformance_fragment.ASYNC_GetInterestPendingList.doInBackground:778`](15-finance-transactions-lists-party-loan-ledgers.md#partnerwiseloansandperformancefragment) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec STLIntColPendingList
  ```

## `stloanledgers`

- [`loanledgers_list.ASYNC_GetLoanLedgers.doInBackground:230`](15-finance-transactions-lists-party-loan-ledgers.md#loanledgerslist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec STLoanLedgers @FromDate = '${fromdate}', @ToDate = '${todate}' , @SelectedFinanceIds='${selectecdfinanceids}'
  ```
  Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`
- [`partyledgers_parameters.ASYNC_GetInterestPendingList.doInBackground:902`](15-finance-transactions-lists-party-loan-ledgers.md#partyledgersparameters) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec STLoanLedgers @FromDate = '${fromdate}', @ToDate = '${todate}' , @SelectedFinanceIds='${selectecdfinanceids}'
  ```
  Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`

## `stockinhand`

- [`stockinhand_list.ASYNCAccountsLedgers.doInBackground:153`](17-inventory-module-stock-masters-items-customers.md#stockinhandlist) · `jdbc.Statement.executeQuery` · selected-branch DB
  ```sql
  exec StockinHand
  ```
  Reads: `Itemcode`, `ItemName`, `Purchase`, `Issued`, `Balance`

## `updatecollectedamount`

- [`varathittam.AsyncWeeklyThittam.doInBackground:741`](12-thittam-collection-plan-weekly-analysis-reports.md#varathittam) · `jdbc.Statement.execute` · selected-branch DB
  ```sql
  exec UpdateCollectedAmount @FinId=${financeid}
  ```
- [`weeklycollectionanalysis_act.AsyncWeeklyThittam.doInBackground:173`](12-thittam-collection-plan-weekly-analysis-reports.md#weeklycollectionanalysisact) · `jdbc.Statement.execute` · branch DB
  ```sql
  exec UpdateCollectedAmount @FinId=${financeid}
  ```

## `updatetbltransactionsmaster`

- [`uploadcollection.ASYNCUpload.doInBackground:1709`](03-offline-mode-download-upload-sync-services.md#uploadcollection) · `jdbc.prepareCall` · selected-branch DB
  ```sql
  {call dbo.UpdatetblTransactionsMaster(?,?,?,?,?,?,?,?,?)}
  ```
  Params: "TransDate" → `String.valueOf(globalvars.collectiondate)` · "TransType" → `TTId.intValue()` · "Narration" → `Narration` · str6 → `globalvars.finid.intValue()` · "TransAmount" → `Amount.doubleValue()` · str9 → `MCLGCollId` · str9 → `8` · "TransLedgerId" → `LedgerId.doubleValue()` · "RouteId" → `globalvars.finid.intValue()` · str7 → `ServerGenExpensesTransId.doubleValue()`

## `vk_rawoflintselcustomer`

- [`ofloans_interestcalculationandpayments.ASYNCindividualloandetails.doInBackground:514`](14-st-loans-of-loans-interest-bearing.md#ofloansinterestcalculationandpayments) · `jdbc.Statement.executeUpdate` · selected-branch DB
  ```sql
  execute VK_RAWOFLIntSelCustomer @GraceDays= 10,@CustId=${receivedparty.getLedgerId()},@IUPDate = '${interestcalculation_uptodate}'
  ```

## `vk_rawstlintselcustomer`

- [`stloaninterestcalculationandpayments.ASYNCindividualloandetails.doInBackground:495`](14-st-loans-of-loans-interest-bearing.md#stloaninterestcalculationandpayments) · `jdbc.Statement.executeUpdate` · selected-branch DB
  ```sql
  execute VK_RAWSTLIntSelCustomer  @GraceDays= 10,@CustId=${receivedparty.getLedgerId()},@IUPDate = '${interestcalculation_uptodate}'
  ```

## `weeklyanalysisstage1`

- [`weeklycollectionpatti_dashboard.asyncthittamstage1.doInBackground:68`](12-thittam-collection-plan-weekly-analysis-reports.md#weeklycollectionpattidashboard) · `jdbc.prepareStatement` · branch-report DB
  ```sql
  EXEC WeeklyAnalysisstage1 ?,?,?,?,?
  ```
  Params: 1 → `receivedweek.getRSDate()` · 2 → `receivedweek.getREDate()` · 3 → `receivedweek.getRSDate()` · 4 → `receivedweek.getColWeekToDate()` · 5 → `financeid`
  Reads: `ColDateH`, `ColDate`

## `weeklypattistageii`

- [`weeklycollectionpatti_dashboard.asyncthittamstage1.doInBackground:109`](12-thittam-collection-plan-weekly-analysis-reports.md#weeklycollectionpattidashboard) · `jdbc.prepareStatement` · branch-report DB
  ```sql
  EXEC WeeklypattiStageII
  ```
  Reads: `MCLoanId`, `ColDate`, `Paid`

