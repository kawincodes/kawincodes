# SQL — 8.11 Daily Patti (outstanding) reports and meeting report

[← work.md](../../work.md) · [Module spec](../../brain/modules/11-daily-patti-outstanding-reports-meeting-report.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **47 statements** (34 local SQLite, 13 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`allfinancedailypatti`](#allfinancedailypatti) (2) · [`dailypatti_kadanpatrulist_act`](#dailypattikadanpatrulistact) (2) · [`dailypatti_varakadanillapulist_act`](#dailypattivarakadanillapulistact) (2) · [`dailypattinew_act`](#dailypattinewact) (4) · [`meetingreport`](#meetingreport) (36) · [`pattidetails_loans_act`](#pattidetailsloansact) (1)

## allfinancedailypatti

Screen: **All-finance daily patti pager (legacy)** · [spec](../../brain/modules/11-daily-patti-outstanding-reports-meeting-report.md#all-finance-daily-patti-pager-legacy--allfinancedailypatti) · 2 statements

#### `onCreate` · line 61
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select FinId,FinMailingName,CashLedgerId,AdminMobileNos,LedgerName,LId ,DefaultLoanMethodId
from FinanceDetails
```

- Reads: `FinMailingName`, `LedgerName`, `AdminMobileNos`, `FinId`, `CashLedgerId`, `LId`, `DefaultLoanMethodId`

#### `SectionsPagerAdapter.getCount` · line 173
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select count(FinId) from FinanceDetails
```

## dailypatti_kadanpatrulist_act

Screen: **New loans (kadan patru) list** · [spec](../../brain/modules/11-daily-patti-outstanding-reports-meeting-report.md#new-loans-kadan-patru-list--dailypattikadanpatrulistact) · 2 statements

#### `onContactSelected` · line 143
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2 from Parties where LedgerId=${passetpartyledgerid}
```

- Reads: `LedgerName`, `Tag`

#### `onContactSelected` · line 152
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select Remarks  from MobileAppCollData where MCLoanId=${dailypatti_kadanpatru_data.getMCLoanId()}
```

- Reads: `Remarks`

## dailypatti_varakadanillapulist_act

Screen: **Bad-debt loss list** · [spec](../../brain/modules/11-daily-patti-outstanding-reports-meeting-report.md#bad-debt-loss-list--dailypattivarakadanillapulistact) · 2 statements

#### `onContactSelected` · line 143
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2 from Parties where LedgerId=${passetpartyledgerid}
```

- Reads: `LedgerName`, `Tag`

#### `onContactSelected` · line 152
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select Remarks  from MobileAppCollData where MCLoanId=${dailypatti_varakadanillapu_data.getMCLoanId()}
```

- Reads: `Remarks`

## dailypattinew_act

Screen: **Daily Patti statement** · [spec](../../brain/modules/11-daily-patti-outstanding-reports-meeting-report.md#daily-patti-statement--dailypattinewact) · 4 statements

#### `ASYNCGetAbstractlist.doInBackground` · line 384
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `pattiproc_01nadappuandnippuphakki`
```sql
exec PattiProc_01NadappuandNippuPhakki @FinId =${financeid},@ReportDate='${fromdate_patti}'
```

- Reads: `MCLoanDate`, `ReportIdentification`, `LedgerName`, `MCLoanAmountCollectable`, `Collected`, `Balance`, `ClosingDate`, `IsNIP`, `SubAreaNo`, `SubAreaName`, `PrevCollection`, `Collection`, `ExtraCollection`, `CollCount`, `ClosedDate`, `collected`, `Pending`, `Party`, `LT`, `Loan`, `Dues`, `IntRate`, `Interest`, `Mahimai`

#### `ASYNCGetAbstractlist.doInBackground` · line 425
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `pattiproc_02nadapunippucollection`
```sql
exec PattiProc_02NadapuNippuCollection @FinId =${financeid},@FromColDate='${fromdate_patti}',@ToColDate='${todate_patti}'
```

#### `ASYNCGetAbstractlist.doInBackground` · line 492
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `pattiproc_03varakadanillappu`
```sql
exec PattiProc_03VarakadanIllappu @FinId =${financeid},@FromColDate='${fromdate_patti}',@ToColDate='${todate_patti}'
```

#### `ASYNCGetAbstractlist.doInBackground` · line 547
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `pattiproc_04kadanpatru`
```sql
exec PattiProc_04KadanPatru @FinId =${financeid},@FromDate='${fromdate_patti}',@ToDate='${todate_patti}'
```

## meetingreport

Screen: **ST/OF loan interest-pending meeting report** · [spec](../../brain/modules/11-daily-patti-outstanding-reports-meeting-report.md#stof-loan-interest-pending-meeting-report--meetingreport) · 36 statements

#### `fetchdata` · line 1378
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where LedgerId=${partyledgerid}
```

- Reads: `LedgerId`, `LedgerName`, `GroupId`, `OpeningBalance`, `OpBalType`, `FinId`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `MasterPartyLedgerId`, `ScreenType`, `PartyGroupId`, `IsDefault`, `IsReserved`, `ListSNo`, `PanNo`, `isActive`, `DesignationId`, `StatusId`, `AndroidDeviceId`, `AadhaarNo`, `Imagefilename`, `lat`, `lng`, `MartialStatus`, `Qualification`, `Profession`, `MonthlyIncome`, `GuardianMonthlyIncome`, `GuardianMobileNo1`, `GuardianMobileNo2`, `GuardianProfession`, `LandMark`, `SpouseName`, `FamilyName`

#### `lambda$onCreate$3` · line 204
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
Select STId, SMSTitle, SMSDesc from SMSTemplates Where STId=1
```

- Reads: `SMSDesc`

#### `ofloans_fillprintablerows` · line 290
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tempoflIntColPenidnglistnew`
```sql
Select typeid, FinId, ofloanId, RecPartnerId, PartyLedgerId, IntRate, IFNULL(LoanDate,'') as LoanDate, ofloanAmountPaid, LedgerName, MobileNo1, IFNULL(ShortName,'') as ShortName, LoanNo, ofloanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno
from tempoflIntColPenidnglistnew
order by RecPartnerId,typeId,LedgerName
```

- Reads: `typeid`, `FinId`, `ofloanId`, `RecPartnerId`, `PartyLedgerId`, `IntRate`, `LoanDate`, `ofloanAmountPaid`, `LedgerName`, `MobileNo1`, `ShortName`, `LoanNo`, `ofloanAmount`, `Balance`, `IntFrom`, `Intupto`, `Paidon`, `Days`, `CInt`, `PaidInt`, `Receno`

#### `prepareListData` · line 369
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempstlIntColPenidnglistnew`
```sql
Create table IF NOT EXISTS tempstlIntColPenidnglistnew(typeid int,FinId Double,STLoanId Double,RecPartnerId Double,PartyLedgerId Double,IntRate Double,LoanDate date,STLoanAmountPaid Double,LedgerName String,MobileNo1 String,ShortName String,LoanNo Double,STLoanAmount Double,Balance Double,IntFrom date,Intupto date,Days int,CInt Double,PaidInt Double,Paidon date,Receno Double)
```

#### `prepareListData` · line 370
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempPartyLevelPending`
```sql
Create table IF NOT EXISTS tempPartyLevelPending(PartyLedgerId Double,Ledgername String,Balance Double,Interest Double)
```

#### `prepareListData` · line 371
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempdepIntColPenidnglistnew`
```sql
Create table IF NOT EXISTS tempdepIntColPenidnglistnew(typeid int,FinId Double,DepositId Double,RecPartnerId Double,PartyLedgerId Double,IntRate Double,LoanDate date,DepositAmountPaid Double,LedgerName String,MobileNo1 String,ShortName String,LoanNo Double,DepositAmount Double,Balance Double,IntFrom date,Intupto date,Days int,CInt Double,PaidInt Double,Paidon date,Receno Double)
```

#### `prepareListData` · line 372
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `deptempPartyLevelPending`
```sql
Create table IF NOT EXISTS deptempPartyLevelPending(PartyLedgerId Double,Ledgername String,Balance Double,Interest Double)
```

#### `prepareListData` · line 373
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempoflIntColPenidnglistnew`
```sql
Create table IF NOT EXISTS tempoflIntColPenidnglistnew(typeid int,FinId Double,ofloanId Double,RecPartnerId Double,PartyLedgerId Double,IntRate Double,LoanDate date,ofloanAmountPaid Double,LedgerName String,MobileNo1 String,ShortName String,LoanNo Double,ofloanAmount Double,Balance Double,IntFrom date,Intupto date,Days int,CInt Double,PaidInt Double,Paidon date,Receno Double)
```

#### `prepareListData` · line 374
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `OFLtempPartyLevelPending`
```sql
Create table IF NOT EXISTS OFLtempPartyLevelPending(PartyLedgerId Double,Ledgername String,Balance Double,Interest Double)
```

#### `prepareListData` · line 391
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tempPartyLevelPending`
```sql
Select PartyLedgerId, Ledgername, Balance, Interest,MobileNo1,intupto,intFrom
from tempPartyLevelPending
where Interest>0
```

- Reads: `PartyLedgerId`, `Balance`, `Ledgername`, `Interest`, `MobileNo1`, `intupto`, `intFrom`

#### `prepareListData` · line 404
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tempstlIntColPenidnglistnew`
```sql
select ShortName,Balance,IntFrom,Intupto,CInt,Days,PaidInt
from tempstlIntColPenidnglistnew
where TypeId=1 and   PaidInt=0 and  PartyLedgerId=${c.getInt(c.getColumnIndexOrThrow("PartyLedgerId"))}
```

- Reads: `ShortName`, `Balance`, `IntFrom`, `Intupto`, `CInt`

#### `prepareListData` · line 443
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ofltempPartyLevelPending`
```sql
Select PartyLedgerId, Ledgername, Balance, Interest  from ofltempPartyLevelPending  where Interest>0
```

- Reads: `Ledgername`, `Balance`

#### `prepareListData` · line 458
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tempoflIntColPenidnglistnew`
```sql
select ShortName,Balance,IntFrom,Intupto,CInt,Days,PaidInt
from tempoflIntColPenidnglistnew
where TypeId=1  and    PaidInt=0 and  PartyLedgerId=${c3.getInt(c3.getColumnIndexOrThrow(str4))}
```

- Reads: `ShortName`, `Balance`, `IntFrom`

#### `stloans_fillprintablerows` · line 243
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tempstlIntColPenidnglistnew`
```sql
Select typeid, FinId, STLoanId, RecPartnerId, PartyLedgerId, IntRate, IFNULL(LoanDate,'') as LoanDate, STLoanAmountPaid, LedgerName, MobileNo1, IFNULL(ShortName,'') as ShortName, LoanNo, STLoanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno
from tempstlIntColPenidnglistnew
where Paidint=0
order by RecPartnerId,typeId,LedgerName
```

- Reads: `typeid`, `FinId`, `STLoanId`, `RecPartnerId`, `PartyLedgerId`, `IntRate`, `LoanDate`, `STLoanAmountPaid`, `LedgerName`, `MobileNo1`, `ShortName`, `LoanNo`, `STLoanAmount`, `Balance`, `IntFrom`, `Intupto`, `Paidon`, `Days`, `CInt`, `PaidInt`, `Receno`

#### `ASYNC_GetInterestPendingList.doInBackground` · line 534
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempstlIntColPenidnglistnew`
```sql
Create table IF NOT EXISTS tempstlIntColPenidnglistnew(typeid int,FinId Double,STLoanId Double,RecPartnerId Double,PartyLedgerId Double,IntRate Double,LoanDate date,STLoanAmountPaid Double,LedgerName String,MobileNo1 String,ShortName String,LoanNo Double,STLoanAmount Double,Balance Double,IntFrom date,Intupto date,Days int,CInt Double,PaidInt Double,Paidon date,Receno Double)
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 535
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tempstlIntColPenidnglistnew`
```sql
delete from tempstlIntColPenidnglistnew
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 536
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempPartyLevelPending`
```sql
Create table IF NOT EXISTS tempPartyLevelPending(PartyLedgerId Double,Ledgername String,Balance Double,Interest Double,MobileNo1 String,intupto Date ,intFrom Date )
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 537
**local SQLite** · `sqlite.execSQL` · DROP · tables: `tempPartyLevelPending`
```sql
drop  table   tempPartyLevelPending
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 538
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempPartyLevelPending`
```sql
Create table IF NOT EXISTS tempPartyLevelPending(PartyLedgerId Double,Ledgername String,Balance Double,Interest Double,MobileNo1 String,intupto Date  ,intFrom Date)
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 539
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tempPartyLevelPending`
```sql
delete from tempPartyLevelPending
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 540
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stlintcolpending`
```sql
exec STLIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 545
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `stlintcolpendinglist`
```sql
exec STLIntColPendingList
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 547
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `getstlabstract`
```sql
exec GetSTLAbstract
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 566
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `tempPartyLevelPending`
```sql
INSERT INTO tempPartyLevelPending (PartyLedgerId, Ledgername, Balance, Interest, MobileNo1, Intupto, IntFrom)
VALUES (${PartyLedgerId}, ${Ledgername}, ${Balance}, ${Interest}, ${MobileNo1}, ${String.valueOf(Intupto)}, ${String.valueOf(IntFrom)})
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 575
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempstlIntColPenidnglistnew`
```sql
Select typeid, FinId, STLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, STLoanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, STLoanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno
from tempstlIntColPenidnglistnew
order by PartyLedgerId
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 630
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tempstlIntColPenidnglistnew`
```sql
INSERT INTO tempstlIntColPenidnglistnew (typeid, FinId, STLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, STLoanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, STLoanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno)
VALUES (${Integer.valueOf(typeid)}, ${FinId}, ${STLoanId}, ${RecPartnerId}, ${PartyLedgerId2}, ${IntRate}, ${String.valueOf(LoanDate)}, ${STLoanAmountPaid}, ${LedgerName2}, ${MobileNo12}, ${ShortName2}, ${LoanNo}, ${STLoanAmount}, ${Balance2}, ${String.valueOf(IntFrom2)}, ${String.valueOf(Intupto2)}, ${Integer.valueOf(Days)}, ${CInt}, ${PaidInt}, ${String.valueOf(Paidon)}, ${Receno})
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 655
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempofllIntColPenidnglistnew`
```sql
Create table IF NOT EXISTS tempofllIntColPenidnglistnew(typeid int,FinId Double,ofloanId Double,RecPartnerId Double,PartyLedgerId Double,IntRate Double,LoanDate date,ofloanAmountPaid Double,LedgerName String,MobileNo1 String,ShortName String,LoanNo Double,ofloanAmount Double,Balance Double,IntFrom date,Intupto date,Days int,CInt Double,PaidInt Double,Paidon date,Receno Double)
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 656
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `OFLtempPartyLevelPending`
```sql
Create table IF NOT EXISTS OFLtempPartyLevelPending(PartyLedgerId Double,Ledgername String,Balance Double,Interest Double)
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 657
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tempoflIntColPenidnglistnew`
```sql
delete from tempoflIntColPenidnglistnew
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 658
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ofltempPartyLevelPending`
```sql
delete from ofltempPartyLevelPending
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 661
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofintcolpending`
```sql
exec OFIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 666
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `ofintcolpendinglist`
```sql
exec OFIntColPendingList
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 668
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `getoflabstract`
```sql
exec GetOFLAbstract
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 680
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `OFLtempPartyLevelPending`
```sql
INSERT INTO OFLtempPartyLevelPending (PartyLedgerId, Ledgername, Balance, Interest)
VALUES (${PartyLedgerId3}, ${Ledgername2}, ${Balance3}, ${Interest2})
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 689
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempOFLIntColPenidnglistnew`
```sql
Select typeid, FinId, OFLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, OFLoanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, OFLoanAmount, Balance, min(IntFrom) as IntFrom, max(Intupto) as IntUpto, max(Days) as Days,sum( CInt) as CInt, sum(PaidInt) as PaidInt, Paidon, Receno
from tempOFLIntColPenidnglistnew
GROUP BY OFLOANID,typeid, FinId, OFLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, OFLoanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, OFLoanAmount, Balance , Paidon, Receno
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 746
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tempOFLIntColPenidnglistnew`
```sql
INSERT INTO tempOFLIntColPenidnglistnew (typeid, FinId, OFLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, OFLoanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, OFLoanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno)
VALUES (${Integer.valueOf(typeid2)}, ${FinId2}, ${OFLoanId}, ${RecPartnerId2}, ${PartyLedgerId4}, ${IntRate2}, ${String.valueOf(LoanDate2)}, ${OFLoanAmountPaid}, ${LedgerName4}, ${MobileNo14}, ${ShortName3}, ${LoanNo2}, ${OFLoanAmount}, ${Balance4}, ${String.valueOf(IntFrom3)}, ${String.valueOf(Intupto3)}, ${Integer.valueOf(Days2)}, ${CInt2}, ${PaidInt2}, ${String.valueOf(Paidon2)}, ${Receno2})
```

## pattidetails_loans_act

Screen: **Patti line loan details (legacy)** · [spec](../../brain/modules/11-daily-patti-outstanding-reports-meeting-report.md#patti-line-loan-details-legacy--pattidetailsloansact) · 1 statements

#### `ASYNCGetPrevLoanList.doInBackground` · line 171
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
[PattiDetails_LoanDetails] @PCNo=${passedpatticompno}
```

- Reads: `MCLoanId`, `LedgerId`, `SubAreaNo`, `SubAreaName`, `LedgerName`, `Imagefilename`, `CWLedgerId`, `MCLoanAmountCollectable`, `Balance`, `MCLoanDate`, `ClosingDate`, `Paid`, `InsAmount`

