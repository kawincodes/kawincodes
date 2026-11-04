# Appendix B — Remote Microsoft SQL Server: tables and stored procedures

All remote access is direct JDBC (jTDS) from the phone to SQL Server — there is no REST API layer for business data. Tables below are those named in SQL text executed through `PreparedStatement`/`CallableStatement`/`Statement` (classification by call context; a few literals whose context could not be determined are listed separately).

## B.1 Tables (64)

| Table | Used by (classes) |
|---|---|
| `androidmenus` | `downloaddata`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`, `userroleprevilages_act` |
| `archivedmcloans` | `prevloanslist_act` |
| `branchaccessallowed` | `branchemployeelist`, `branchemployeesaccessearchlist`, `branchmobiledeviceaccescontrol_act` |
| `branchconnstrs` | `branchemployeelist`, `branchemployeesaccessearchlist` |
| `branchempledgers` | `branch_employee`, `branchemployeelist`, `branchemployeesaccessearchlist` |
| `branchmobiledevlicesallowed` | `branchemployeelist`, `branchemployeesaccessearchlist`, `branchmobiledeviceaccescontrol_act`, `deviceidentity`, `refreshorreloadbrancheslist`, `syncresources` |
| `branchnewimeirequests` | `branchmobiledeviceaccescontrol_act` |
| `bscomps` | `tbbspl_list` |
| `cashbookverification` | `cashbook` |
| `collected` | `varathittam` |
| `comparedpl` | `monthlypandl_fragment`, `tbbspl_list` |
| `details` | `specialaccesscontrolact` |
| `downloadcontroling` | `cashbook`, `downloaddata`, `editing_collectionswithfulloptions_act`, `otpverificationact` |
| `financeaccessallowed` | `branchemployeesearchlist`, `downloaddata`, `editing_collectionswithfulloptions_act`, `employeesaccessearchlist`, `employeesearchlist`, `mobiledeviceaccescontrol_act`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`, `uploadcollection` |
| `financemaster` | `downloaddata`, `editing_collectionswithfulloptions_act`, `financelinesaddandedit`, `otpverificationact` |
| `invtransdetails` | `directcollectionentry`, `invtransactnew`, `newloancreation`, `uploadcollection` |
| `invtransmaster` | `invtransactnew` |
| `itemmaster` | `downloaddata`, `itemmaster_activity`, `itemmaster_list`, `syncresources` |
| `loanmethods` | `downloaddata`, `loanmethods`, `loanmethods_searchlist`, `otpverificationact`, `refreshorreloadbrancheslist`, `selectbranchandfinance`, `syncresources` |
| `loans` | `editing_collections_act`, `editing_collectionswithfulloptions_act` |
| `loansallowed` | `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources` |
| `mcloansclosoure` | `loanclosoureentry` |
| `mobiledevlicesallowed` | `branchemployeesearchlist`, `downloaddata`, `editing_collectionswithfulloptions_act`, `employeesaccessearchlist`, `employeesearchlist`, `mobiledeviceaccescontrol_act`, `otpverificationact`, `refreshorreloadfinancelist` |
| `newimeirequests` | `mobiledeviceaccescontrol_act` |
| `partyvisitstatus` | `uploadcollection` |
| `pattiexpledgers` | `accledgers_searchlist`, `accountledgerdetails`, `downloaddata`, `otpverificationact` |
| `pattiincomeledgers` | `accountledgerdetails`, `downloaddata` |
| `relateddoccollection` | `documentsphotossearchlist`, `documentsupload`, `newloancreation`, `uploadcollection` |
| `relateddocmenttypes` | `documentsphotossearchlist`, `newloancreation`, `syncresources` |
| `savingsclosure` | `closingsavingsaccount`, `prevsavingclosure_list_act` |
| `smstemplates` | `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources` |
| `softsettings` | `downloaddata`, `editing_collectionswithfulloptions_act`, `otpverificationact`, `refreshorreloadbrancheslist`, `selectbranchandfinance`, `settings_act`, `syncresources`, `uploadcollection` |
| `specialaccessallowed` | `downloaddata`, `otpverificationact`, `refreshorreloadbrancheslist`, `specialaccesscontrolact`, `syncresources` |
| `specialaccessrules` | `refreshorreloadbrancheslist`, `specialaccesscontrolact`, `syncresources` |
| `stockcat` | `downloaddata`, `stockcat_list`, `stockcategories`, `syncresources` |
| `stockgrp` | `downloaddata`, `stockgroup_act`, `stockgrp_list`, `syncresources` |
| `stockuom` | `downloaddata`, `stockUOM_act`, `stockUOM_list`, `syncresources` |
| `subareadetails` | `collectionareadetails_act`, `collectionareassearchlist`, `downloaddata`, `editing_collectionswithfulloptions_act`, `loanslist_act`, `otpverificationact`, `partydetails_searchlist` |
| `tblfinwisepartners` | `financewisepartners_list`, `otpverificationact` |
| `tblgroups` | `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources` |
| `tblledgerdet` | `simplethittamlist_act` |
| `tblledgerdetforsimplethittam` | `simplethittamlist_act` |
| `tblledgers` | `accledgers_searchlist`, `accountledgerdetails`, `branchemployeesearchlist`, `customerdetails_act`, `customers_list`, `downloaddata`, `editing_collectionswithfulloptions_act`, `employeedetails_act`, `employeesaccessearchlist`, `employeesearchlist`, `ledgerstatmentsdateselection`, `loanslist_act`, `navigationdrawactivity`, `otpverificationact`, `parties_list`, `partyandpartnerdetails`, `partydetails_act`, `partydetails_searchlist`, `partytransfer`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`, `selectbranchandfinance`, `simplethittamlist_act`, `syncresources`, `uploadcollection` |
| `tblmcloanduecol` | `collectionlistprints`, `directcollectionentry`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `selectedloancollectiondetails`, `uploadcollection` |
| `tblmcloangroupcollectionmaster` | `MyIntentService`, `MyService_updatecollection`, `editing_collectionlist_act`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `selectedloancollectiondetails`, `uploadcollection` |
| `tblmcloans` | `directcollectionentry`, `newloancreation`, `selectedloancollectiondetails`, `uploadcollection` |
| `tblofloanint` | `ofloans`, `ofloans_interestcalculationandpayments` |
| `tblofloans` | `ofloans`, `otpverificationact` |
| `tblsalesdetailsforandroid` | `downloaddata`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`, `userroleprevilages_act` |
| `tblstafflocationtrack` | `staffgpstracking_service` |
| `tblstloanint` | `stloaninterestcalculationandpayments`, `stloans` |
| `tblstloans` | `otpverificationact`, `stloans` |
| `tbltransactionsdetails` | `accounttransactions_act`, `cashreceiveandreturns`, `directcollectionentry`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `invtransactnew`, `loanclosoureentry`, `newloancreation`, `ofloans`, `ofloans_interestcalculationandpayments`, `prevsavingclosure_list_act`, `stloaninterestcalculationandpayments`, `stloans`, `uploadcollection` |
| `tbltransactionsmaster` | `accounttransactions_act`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `inventorytransactionslist_act`, `invtransactnew`, `ledgerstatmentsdateselection`, `loanclosoureentry`, `newloancreation`, `ofloans`, `ofloans_interestcalculationandpayments`, `prevsavingclosure_list_act`, `stloaninterestcalculationandpayments`, `stloans`, `uploadcollection` |
| `tempcollweeks` | `varathittam`, `weeklycollectionanalysis_act` |
| `tempdailycashbook` | `cashbook` |
| `tempintcolpending` | `partnerwiseloansandperformance_fragment` |
| `tempoflintcolpenidnglistnew` | `meetingreport`, `partnerwiseloansandperformance_fragment` |
| `tempstlintcolpenidnglistnew` | `meetingreport`, `partnerwiseloansandperformance_fragment` |
| `transactionsmaster` | `invtransactnew` |
| `twenduration` | `selectbranchandfinance` |
| `uploadcontroling` | `cashbook`, `uploadcollection` |
| `userroles` | `refreshorreloadbrancheslist`, `syncresources` |
| `venussoftsettings` | `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources` |

Tables seen in SQL literals whose execution context (local vs remote) could not be resolved mechanically: `collection` (uploadcollection), `financeaccessallowed` (mobiledeviceaccescontrol_act), `loans` (editing_collectionswithfulloptions_act), `subareadetails` (collectionarearearrangeact), `tblledgers` (partyrearrange), `tbltransactionsdetails` (uploadcollection)

## B.2 Stored procedures (135)

Parameter count = number of `?` placeholders in the JDBC call text (blank when the call is built dynamically).

| Procedure | Params | Called from | Example call text |
|---|---|---|---|
| `app_get7dayscollectionfordailyloans` | 3 | `selectbranchandfinance` | `exec [APP_Get7daysCollectionfordailyloans] @EndDate=' … ', @Daystoadd=0, @FinId=` |
| `app_get7dayscollectionformonthlyloans` | 3 | `selectbranchandfinance` | `exec [APP_Get7daysCollectionforMonthlyloans] @EndDate=' … ', @Daystoadd=0, @FinId=` |
| `app_get7dayscollectionforweeklyloans` | 3 | `selectbranchandfinance` | `exec [APP_Get7daysCollectionforweeklyloans] @EndDate=' … ', @Daystoadd=0, @FinId=` |
| `app_get7dayscollectionforweeklyloanswithweekdayid` | 2 | `selectbranchandfinance` | `exec [APP_Get7daysCollectionforweeklyloanswithWeekdayid] @WeekdayId= …  ,@EndDate='` |
| `app_gettodaycollections` | 2 | `todaycollectionlist` | `exec APP_GetTodayCollections @IMEI=' … ',@FinId=` |
| `app_mcloancollectiondataautoloanclosoure_withoutweekdayssupport` | 3 | `collectionlistprints` | `exec APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport ?,?,?` |
| `app_mcloancollectiondataautoloanclosoure_withweekdayssupport` | 3 | `collectionlistprints` | `exec APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport ?,?,?` |
| `app_mcloancollectiondatawithloanclosouremonitering_withoutweekdayssupport` | 3 | `collectionlistprints` | `exec APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport ?,?,?` |
| `app_mcloancollectiondatawithloanclosouremonitering_withweekdayssupport` | 3 | `collectionlistprints` | `exec APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport ?,?,?` |
| `app_pendingreportautoloanclosoure_withoutweekdayssupport` | 2 | `daywisependingreport_act` | `exec APP_PendingReportAUTOLOANCLOSOURE_WithOUTWeekdaysSupport   @ColDate=' … ',  @FinId=` |
| `app_transactionslist` | 2 | `ledgerstatement_act`, `monthlypandl_fragment`, `tbbspl_list`, `tbbspl_particulars` | `exec APP_Transactionslist @FinId= … ,@LedId=` |
| `app_transactionslistforhandloanshistory` | 1 | `handloans_list` | `exec APP_Transactionslistforhandloanshistory @LedId=` |
| `app_transactionslistforselectedline` | 2 | `accounttrans_list` | `exec APP_TransactionslistforSelectedLine @FromDate=' … ', @ToDate='` |
| `app_transactionslistnew` | 2 | `accounttrans_list` | `exec APP_TransactionslistNew @FromDate=' … ', @ToDate='` |
| `app_unpaidpartiesforselecteddate` | 2 | `selectdateforreport` | `exec APP_unpaidpartiesforselecteddate @ColDate=' … ',@FinId=` |
| `appbs_getbalancefortheledger` | 2 | `tbbspl_list` | `exec APPBS_GetBalancefortheLedger @LedgerId= … , @SelectedFinanceIds='` |
| `appbs_getbankbalance` | 2 | `tbbspl_list` | `exec APPBS_GetBankBalance @SelectedFinanceIds=' … ',  @RDate ='` |
| `appbs_getcashbalance` | 2 | `tbbspl_list` | `exec APPBS_GetCashBalance @SelectedFinanceIds=' … ',  @RDate ='` |
| `applistplincomes` | 2 | `tbbspl_list` | ` exec APPListPLIncomes @SelectedFinanceIds=' … ',  @RDate ='` |
| `apppattiproc_forbalancesheet` | 2 | `tbbspl_list` | `exec APPPattiProc_forBalancesheet    @SelectedFinanceIds=' … ', @ReportDate ='` |
| `apppattiproc_getextracollection` | 2 | `tbbspl_list` | `exec APPPattiProc_GetExtraCollection    @SelectedFinanceIds=' … ', @FromColDate ='` |
| `bsapp_getbalancesheetcomponentparticulars` | 2, 3 | `tbbspl_list` | `exec BSAPP_GetBalanceSheetComponentParticulars @ComponentId= … , @SelectedFinanceIds='` |
| `checkisallcashandbankbooksverified` | 2 | `cashbook` | `exec checkisallcashandbankbooksverified @FinId= …  , @VerfiedDate='` |
| `checkisanyoneofcashandorbooksverified` | 2 | `uploadcollection` | `exec checkisanyoneofcashandorbooksverified @FinId= …  ,  @VerfiedDate='` |
| `collectiondayandcollectionexecutiveforapp` | 2 | `selectbranchandfinance` | `exec  CollectionDayandCollectionExecutiveforapp   @FromDate =' … ', @ToDate ='` |
| `curdaycashbook` | 2 | `cashbook` | `exec CurDayCashBook @IMEI=' … ', @CashLedgerId=` |
| `curdaycompainedcashbook` | 2 | `cashbook` | `exec CurDayCompainedCashBook @IMEI=' … ', @FinId =` |
| `dailycollectionanalysis` | 2 | `todaycollectionabstractlist` | `exec DailyCollectionAnalysis @FinId = … ,@collDate='` |
| `dailycollectiondetails` | 2 | `todaycollectionlist` | `exec DailyCollectionDetails @ctid= … , @FinId =` |
| `dailycollectiondetailsforweeklylines` | 2 | `todaycollectionlist` | `exec DailyCollectionDetailsforweeklyLines @ctid= … , @FinId =` |
| `get_downloadlimitations` | 1 | `navigationdrawactivity` | `exec Get_DownloadLimitations @IMEI=' … '` |
| `get_ledgersforquicktransactions` |  | `accounttransactions_act`, `refreshorreloadbrancheslist` | `exec Get_LedgersforQuickTransactions` |
| `get_uploadlimitations` | 1 | `navigationdrawactivity` | `exec Get_UploadLimitations @IMEI=' … '` |
| `get_uploadlimitationsdaterange` | 1 | `navigationdrawactivity` | `exec Get_UploadLimitationsDateRange @IMEI=' … '` |
| `getcashandbankledgers` | 1 | `refreshorreloadbrancheslist`, `syncresources` | `exec getCashandBankLedgers @URId=` |
| `getfinwisepartners` | 1 | `financewisepartners_list` | `exec getfinwisepartners @FinId=` |
| `gethandloansdetails` |  | `handloans_list` | `exec GetHandLoansDetails` |
| `getlastncollectionforcollreportprint` | 4 | `collectionlistprints` | `exec GetLastNCollectionforCollReportPrint ?,?,?,?` |
| `getloandetailsforperiod` | 2 | `periodicloareportlist_act` | `exec GetLoanDetailsforPeriod @FinId = … ,@FromDate='` |
| `getmcloanslistforappforselectedperiod` | 3 | `loanslist_act` | `exec GetMCLoansListforAPPforselectedperiod ?,?,?` |
| `getmcloanslistforappforselectedperiodwithweekdayid` | 4 | `loanslist_act` | `exec GetMCLoansListforAPPforselectedperiodwithweekdayid ?,?,?,?` |
| `getoflabstract` |  | `meetingreport` | `exec GetOFLAbstract` |
| `getstlabstract` |  | `meetingreport` | `exec GetSTLAbstract` |
| `getthittamitems` | 1 | `varathittam` | `exec GetThittamItems @CurFinId=` |
| `insertaccledgersapp` | 7 | `accountledgerdetails` | `{call dbo.InsertAccLedgersAPP(?,?,?,?,?,?,?)}` |
| `insertacctransactionsmaster` | 10 | `closingsavingsaccount`, `loanclosoureentry` | `{call dbo.InsertAccTransactionsMaster(?,?,?,?,?,?,?,?,?,? )}` |
| `insertappacctransactionsmaster` | 11 | `accounttransactions_act`, `invtransactnew` | `{call dbo.InsertAPPAccTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertappmcloanclosuretransdetailsp1` | 7 | `closingsavingsaccount`, `loanclosoureentry` | `{call dbo.InsertAPPMCLoanClosureTransDetailsP1(?,?,?,?,?,?,?)}` |
| `insertbranchempledgers` | 15 | `branch_employee` | `{call dbo.InsertBranchEmpLedgers(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertbranchmobiledevlicesallowed` | 5 | `branchmobiledeviceaccescontrol_act` | `{call dbo.InsertBranchMobileDevlicesAllowed(?,?,?,?,? )}` |
| `insertbranchnewimeirequests` | 3 | `deviceidentity` | `{call dbo.InsertBranchNewIMEIRequests(?,?,?)}` |
| `insertcashbookverification` | 6 | `cashbook` | `{call dbo.InsertCashBookVerification(?,?,?,?,?,?)}` |
| `insertcontratbltransactionsmaster` | 12 | `cashreceiveandreturns` | `{call dbo.InsertContratblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertdepositinttbltransactionsmaster` | 10 | `ofloans_interestcalculationandpayments`, `stloaninterestcalculationandpayments` | `{call dbo.InsertDepositInttblTransactionsMaster( ?,?,?,?,?,?,?,?,?,?)}` |
| `insertempledgers` | 15 | `employeedetails_act` | `{call dbo.InsertEmpLedgers(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertimageuploads` | 2 | `accounttransactions_act`, `documentsupload`, `partyandpartnerdetails`, `partydetails_act` | `{call dbo.InsertImageUploads(?,?)}` |
| `insertitemmaster` | 7 | `itemmaster_activity` | `{call dbo.Insertitemmaster(?,?,?,?,?,?,?)}` |
| `insertloanmethods` | 20 | `loanmethods` | `{call dbo.InsertLoanMethods(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertmcloansclosoure` | 8 | `loanclosoureentry` | `{call dbo.InsertMCLoansClosoure(?,?,?,?,?,?,?,?)}` |
| `insertmobiledevlicesallowed` | 5 | `mobiledeviceaccescontrol_act` | `{call dbo.InsertMobileDevlicesAllowed(?,?,?,?,?)}` |
| `insertnewparties` | 19 | `partydetails_act`, `uploadcollection` | `{call dbo.InsertNewParties(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertnewpartieswithautocwledgerid` | 19 | `uploadcollection` | `{call dbo.InsertNewPartieswithautocwledgerid(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertpartiesfromapp` | 26 | `partyandpartnerdetails` | `{call dbo.InsertPartiesfromAPP(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `insertrelateddoccollection` | 5 | `documentsupload` | `{call dbo.InsertRelatedDocCollection(?,?,?,?,?)}` |
| `insertstockcat` | 4 | `stockcategories` | `{call dbo.Insertstockcat(?,?,?,?)}` |
| `insertstockgrp` | 5 | `stockgroup_act` | `{call dbo.InsertStockGrp(?,?,?,?,?)}` |
| `insertstockuom` | 4 | `stockUOM_act` | `{call dbo.InsertStockUOM(?,?,?,?)}` |
| `insertsubareadetails` | 8 | `collectionareadetails_act` | `{call dbo.InsertSubAreaDetails(?,?,?,?,?,?,?,?)}` |
| `insertsuppliers` | 13 | `customerdetails_act` | `{call dbo.InsertSuppliers(?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblmcloanduecol` | 15 | `MyIntentService`, `MyService_updatecollection`, `editing_collectionswithfulloptions_act` | `{call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblmcloangroupcollectionmasterwithtwostaffs` | 13 | `directcollectionentry`, `editing_collectionswithfulloptions_act`, `uploadcollection` | `{call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblmcloanswithweekdayid` | 32 | `newloancreation`, `uploadcollection` | `{call dbo.InserttblMCLoanswithWeekdayId(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ,?,?,?,?,?,?)}` |
| `inserttblofloanint` | 20 | `ofloans_interestcalculationandpayments` | `{call dbo.InserttblOFLoanInt(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblofloans` | 18 | `ofloans` | `{call dbo.InserttblOFLoans(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblofloanswithuserdefinedloanno` | 18 | `ofloans` | `{call dbo.InserttblOFLoanswithUserDefinedLoanNo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblstloanint` | 20 | `stloaninterestcalculationandpayments` | `{call dbo.InserttblSTLoanInt(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblstloans` | 24 | `stloans` | `{call dbo.InserttblSTLoans(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttblstloanswithuserdefinedloanno` | 24 | `stloans` | `{call dbo.InserttblSTLoanswithUserDefinedLoanNo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttbltransactionsdetails` | 7 | `ofloans`, `ofloans_interestcalculationandpayments`, `stloaninterestcalculationandpayments`, `stloans` | `{call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}` |
| `inserttbltransactionsmaster` | 12, 13 | `directcollectionentry`, `editing_collectionswithfulloptions_act`, `newloancreation`, `uploadcollection` | `{call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}` |
| `inserttbltransactionsmasterformonthly` | 10 | `ofloans`, `stloans` | `{call dbo.InserttblTransactionsMasterForMonthly( ?,?,?,?,?,?,?,?,?,?)}` |
| `inserttransserial` | 6 | `ofloans`, `ofloans_interestcalculationandpayments`, `stloaninterestcalculationandpayments`, `stloans` | `{call dbo.InsertTransSerial(?,?,?,?,?,?)}` |
| `ledgerwisebalance` | 2 | `simplethittamlist_act` | `exec LedgerwiseBalance @CCom = … ,@LedId=` |
| `loanclosedandrenewedforwindowsapp` | 2 | `closed_renewedloanslist_act` | `exec LoanClosedandRenewedforwindowsAPP @fromDate=' … ',  @ToDate='` |
| `loanclosedandrenewedwithweekdayid` | 2 | `closed_renewedloanslist_act` | `exec LoanClosedandRenewedwithweekdayid  @weekdayid=  … , @fromDate='` |
| `monthlyplincomes` | 2 | `monthlypandl_fragment` | `exec [MonthlyPLIncomes] @SelectedFinanceIds=' … ',  @FDate ='` |
| `newpartiesnewloans` | 2 | `newpartiesnewloanslist_act` | `exec NewPartiesNewLoans @fromDate=' … ',  @ReportDate='` |
| `newpartiesnewloanswithweekdayid` | 2 | `newpartiesnewloanslist_act` | `exec NewPartiesNewLoanswithweekdayid  @weekdayid=  … , @fromDate='` |
| `niploansclosed` | 2 | `closed_renewedloanslist_act` | `exec NIPLoansClosed @IMEI=' … ',  @FinId=` |
| `ofintcolpending` | 2 | `meetingreport`, `partnerwiseloansandperformance_fragment` | `exec OFIntColPending @IUPDate = ' … ', @IntPaidDate ='` |
| `ofintcolpendinglist` |  | `meetingreport`, `partnerwiseloansandperformance_fragment` | `exec OFIntColPendingList` |
| `ofl_prepareintpricolllist` | 3 | `ofloans_interestcalculationandpayments` | ` exec OFL_PrepareIntPriCollList @isAutoIncEnabled=false, @IntRecptDate=' … ',@PartyLedId=` |
| `ofl_prepareintpricolllistoverallpenalty` | 3 | `ofloans_interestcalculationandpayments` | ` exec OFL_PrepareIntPriCollListOverAllPenalty @isAutoIncEnabled=false, @IntRecptDate=' … ',@PartyLedId=` |
| `ofl_showindoflstatus` | 1 | `ofloans`, `ofloans_interestcalculationandpayments` | `exec OFL_ShowIndOFLStatus @PartyLedgerId= ` |
| `oflint_dispintpaydetails` | 2 | `ofloans_interestcalculationandpayments` | `execute OFLInt_DispIntpayDetails @PartyLedId=  … ,@SetId=` |
| `ofloanledgers` | 2 | `loanledgers_list`, `partyledgers_parameters` | `exec OFLoanLedgers @CFD = ' … ' , @SelectedFinanceIds='` |
| `pattiproc_01nadappuandnippuphakki` | 2 | `dailypattinew_act` | `exec PattiProc_01NadappuandNippuPhakki @FinId = … ,@ReportDate='` |
| `pattiproc_02nadapunippucollection` | 2 | `dailypattinew_act` | `exec PattiProc_02NadapuNippuCollection @FinId = … ,@FromColDate='` |
| `pattiproc_03varakadanillappu` | 2 | `dailypattinew_act` | `exec PattiProc_03VarakadanIllappu @FinId = … ',@ToColDate='` |
| `pattiproc_04kadanpatru` | 2 | `dailypattinew_act` | `exec PattiProc_04KadanPatru @FinId = … ,@FromDate='` |
| `preparedashboardcash` |  | `cashbookabstract` | `exec PrepareDashBoardCash` |
| `preparepattiiiweekly` |  | `weeklycollectionpatti_dashboard` | `EXEC PreparePattiIIWeekly` |
| `rep24weekcollection` | 2 | `varathittam`, `weeklycollectionanalysis_act` | `exec Rep24WeekCollection @NipLimit= … ,@FinIdN=` |
| `rep_finwiseofloans` | 1 | `defaulttables` | `exec Rep_FinWiseOFLoans @RDate=` |
| `rep_finwisestloans` | 1 | `defaulttables` | `exec Rep_FinWiseSTLoans @RDate=` |
| `rep_showofloanintdistribution` | 1 | `defaulttables` | `exec Rep_ShowOFLoanIntDistribution @RDate=` |
| `rep_showofloanintdistributionoverall` | 1 | `defaulttables` | `exec [Rep_ShowOFLoanIntDistributionOverAll] @RDate=` |
| `rep_showpartnerfinancewiseofloanbalance` | 1 | `defaulttables` | `exec Rep_ShowPartnerFinanceWiseOFLoanBalance @RDate=` |
| `rep_showpartnerfinancewisestloanbalance` | 1 | `defaulttables` | `exec Rep_ShowPartnerFinanceWiseSTLoanBalance @RDate=` |
| `rep_showpartnerwiseofloanbalance` | 1 | `defaulttables` | `exec Rep_ShowPartnerWiseOFLoanBalance @RDate=` |
| `rep_showpartnerwisestloanbalance` | 1 | `defaulttables` | `exec Rep_ShowPartnerWiseSTLoanBalance @RDate=` |
| `rep_showstloanintdistribution` | 1 | `defaulttables` | `exec Rep_ShowSTLoanIntDistribution @RDate=` |
| `rep_showstloanintdistributionoverall` | 1 | `defaulttables` | `exec [Rep_ShowSTLoanIntDistributionOverAll] @RDate=` |
| `rpt_partywiseofloans` | 1 | `defaulttables` | `exec RPT_PartywiseOFLoans @RDate=` |
| `rpt_partywisestloans` | 1 | `defaulttables` | `exec RPT_PartywiseSTLoans @RDate=` |
| `setattentionrequired` | 1 | `sevendayscollectionforselline` | `{call dbo.SetAttentionRequired(?)}` |
| `showexpensesoftheweek` | 1 | `varathittam` | `exec ShowExpensesoftheWeek @FinId=` |
| `showincomesoftheweek` | 1 | `varathittam` | `exec ShowIncomesoftheWeek @FinId=` |
| `showoflintlist` | 2 | `ofloans_interestcalculationandpayments` | `exec ShowOFLIntList @PartyLedId=  … ,@CurPF='` |
| `showstlintlist` | 2 | `stloaninterestcalculationandpayments` | `exec ShowSTLIntList @PartyLedId=  … ,@CurPF='` |
| `simplethittam` | 2 | `simplethittamlist_act` | `exec SimpleThittam @FromDate=' … ',@ToDate='` |
| `singlefinancecashbook` | 2 | `ASYNCGetsingleFinanceCashBook`, `allfinancedailythittam`, `singlefinancedailythittam_act` | `exec singleFinanceCashBook @IMEI=' … ',@ReportDate  ='` |
| `stl_prepareintpricolllist` | 3 | `stloaninterestcalculationandpayments` | ` exec STL_PrepareIntPriCollList @isAutoIncEnabled=false, @IntRecptDate=' … ',@PartyLedId=` |
| `stl_showindstlstatus` | 1 | `stloaninterestcalculationandpayments`, `stloans` | `exec STL_ShowIndSTLStatus @PartyLedgerId= ` |
| `stlint_dispintpaydetails` | 2 | `stloaninterestcalculationandpayments` | `execute STLInt_DispIntpayDetails @PartyLedId=  … ,@SetId=` |
| `stlintcolpending` | 2 | `meetingreport`, `partnerwiseloansandperformance_fragment` | `exec STLIntColPending @IUPDate = ' … ', @IntPaidDate ='` |
| `stlintcolpendinglist` |  | `meetingreport`, `partnerwiseloansandperformance_fragment` | `exec STLIntColPendingList` |
| `stloanledgers` | 2 | `loanledgers_list`, `partyledgers_parameters` | `exec STLoanLedgers @FromDate = ' … ', @ToDate = '` |
| `stockinhand` |  | `stockinhand_list` | `exec StockinHand` |
| `updatecollectedamount` | 1 | `varathittam`, `weeklycollectionanalysis_act` | `exec UpdateCollectedAmount @FinId=` |
| `updatetbltransactionsmaster` | 9 | `uploadcollection` | `{call dbo.UpdatetblTransactionsMaster(?,?,?,?,?,?,?,?,?)}` |
| `vk_rawoflintselcustomer` | 3 | `ofloans_interestcalculationandpayments` | `execute VK_RAWOFLIntSelCustomer @GraceDays= 10,@CustId= … ,@IUPDate = '` |
| `vk_rawstlintselcustomer` | 3 | `stloaninterestcalculationandpayments` | `execute VK_RAWSTLIntSelCustomer  @GraceDays= 10,@CustId= … ,@IUPDate = '` |
| `weeklyanalysisstage1` | 5 | `weeklycollectionpatti_dashboard` | `EXEC WeeklyAnalysisstage1 ?,?,?,?,? ` |
| `weeklypattistageii` |  | `weeklycollectionpatti_dashboard` | `EXEC WeeklypattiStageII` |
