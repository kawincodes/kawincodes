# SQL — 8.15 Finance transactions lists, party/loan ledgers and loan & deposit analysis reports

[← work.md](../../work.md) · [Module spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **42 statements** (15 local SQLite, 27 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`analysisrep_financewiseabstract_fragment`](#analysisrepfinancewiseabstractfragment) (1) · [`analysisrep_partnerwiseabstract_fragment`](#analysisreppartnerwiseabstractfragment) (1) · [`analysisrepfin_interestratedist_fragment`](#analysisrepfininterestratedistfragment) (1) · [`analysisrepfin_overallinterestratedist_fragment`](#analysisrepfinoverallinterestratedistfragment) (1) · [`analysisrepfin_partnerwiseabstract_fragment`](#analysisrepfinpartnerwiseabstractfragment) (1) · [`analysisrepfin_partyrwiseabstract_fragment`](#analysisrepfinpartyrwiseabstractfragment) (1) · [`financewisepartners_list`](#financewisepartnerslist) (7) · [`fintrans_list`](#fintranslist) (2) · [`loanledgers_list`](#loanledgerslist) (2) · [`parties_list`](#partieslist) (9) · [`partnerwiseloansandperformance_fragment`](#partnerwiseloansandperformancefragment) (9) · [`partyledgers_list`](#partyledgerslist) (4) · [`partyledgers_parameters`](#partyledgersparameters) (3)

## analysisrep_financewiseabstract_fragment

Screen: **Finance-wise abstract (analysis page 0)** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#finance-wise-abstract-analysis-page-0--analysisrepfinancewiseabstractfragment) · 1 statements

#### `ASYNCGetFinancelist.doInBackground` · line 120
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
${globalvars.curanalysisreport.getFinWiseAbsSQLStr()}'${globalvars.todaydate}'
```

- Reads: `FinId`, `Finance`, `Total`

## analysisrep_partnerwiseabstract_fragment

Screen: **Partner-wise abstract (analysis page 1)** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#partner-wise-abstract-analysis-page-1--analysisreppartnerwiseabstractfragment) · 1 statements

#### `ASYNCGetFinancelist.doInBackground` · line 125
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
${globalvars.curanalysisreport.getPartnerwiseAbsSQLStr()}'${globalvars.todaydate}'
```

- Reads: `RecPartnerId`, `Partner`, `Total`

## analysisrepfin_interestratedist_fragment

Screen: **Interest-rate distribution (analysis page 4)** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#interest-rate-distribution-analysis-page-4--analysisrepfininterestratedistfragment) · 1 statements

#### `ASYNCGetFinancelist.doInBackground` · line 103
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
${globalvars.curanalysisreport.getIntRateDisAbsSQLStr()}'${globalvars.todaydate}'
```

- Reads: `count`, `Finance`, `loan`, `minimum`, `maximum`, `average`, `IntRate`

## analysisrepfin_overallinterestratedist_fragment

Screen: **Overall interest-rate distribution (analysis page 5)** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#overall-interest-rate-distribution-analysis-page-5--analysisrepfinoverallinterestratedistfragment) · 1 statements

#### `ASYNCGetFinancelist.doInBackground` · line 104
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
${globalvars.curanalysisreport.getOverallIntRateDisAbsSQLStr()}'${globalvars.todaydate}'
```

- Reads: `count`, `loan`, `minimum`, `maximum`, `average`, `IntRate`

## analysisrepfin_partnerwiseabstract_fragment

Screen: **Finance and partner-wise (analysis page 2)** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#finance-and-partner-wise-analysis-page-2--analysisrepfinpartnerwiseabstractfragment) · 1 statements

#### `ASYNCGetFinancelist.doInBackground` · line 104
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
${globalvars.curanalysisreport.getFinandPartnerWiseAbsSQLStr()}'${globalvars.todaydate}'
```

- Reads: `FinId`, `Finance`, `Partner`, `RecPartnerId`, `Total`

## analysisrepfin_partyrwiseabstract_fragment

Screen: **Party-wise (analysis page 3)** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#party-wise-analysis-page-3--analysisrepfinpartyrwiseabstractfragment) · 1 statements

#### `ASYNCGetFinancelist.doInBackground` · line 108
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
${globalvars.curanalysisreport.getPartyWiseAbsSQLStr()}'${globalvars.todaydate}'
```

- Reads: `FinId`, `Finance`, `Partner`, `RecPartnerId`, `Total`, `Party`, `IntRate`

## financewisepartners_list

Screen: **Finance-wise Partners** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#finance-wise-partners--financewisepartnerslist) · 7 statements

#### `ASYNCGetFinancelist.doInBackground` · line 249
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `getfinwisepartners`
```sql
exec getfinwisepartners @FinId=${selectedfinanceid}
```

- Reads: `IsSelected`, `PartnerLedgerId`, `UID`, `LedgerName`, `Active`, `RecTransCount`

#### `ASYNCSaveFinancewisepartners.doInBackground` · line 334
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `tblFinwisePartners`
```sql
INSERT INTO tblFinwisePartners (PartnerLedgerId,FinId,isActive) output inserted.[UID] values(?,?,?)
```

- Params: 1 → `financewisepartners_dataVar.getPartnerLedgerId()` (setDouble) · 2 → `selectedfinanceid` (setDouble) · 3 → `financewisepartners_dataVar.getActive().booleanValue()` (setBoolean)

#### `ASYNCSaveFinancewisepartners.doInBackground` · line 344
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `tblFinwisePartners`
```sql
INSERT INTO tblFinwisePartners (UID, PartnerLedgerId, FinId, isActive)
VALUES (${Integer.valueOf(((financewisepartners_data) financewisepartners_dataList.get(i)).getUID())}, ${Integer.valueOf(financewisepartners_dataVar.getPartnerLedgerId())}, ${Integer.valueOf(selectedfinanceid)}, ${financewisepartners_dataVar.getActive()})
```

#### `ASYNCSaveFinancewisepartners.doInBackground` · line 349
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblFinwisePartners`
```sql
delete from tblFinwisePartners  where UID=?
```

- Params: 1 → `financewisepartners_dataVar.getUID()` (setInt)

#### `ASYNCSaveFinancewisepartners.doInBackground` · line 352
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `tblFinwisePartners`
```sql
DELETE FROM tblFinwisePartners WHERE UID=?
```

- Selection args: `new String[]{String.valueOf(((financewisepartners_data) financewisepartners_dataList.get(…`

#### `ASYNCSaveFinancewisepartners.doInBackground` · line 355
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblFinwisePartners`
```sql
update tblFinwisePartners set  PartnerLedgerId=?,FinId=?, isActive=? where UID=?
```

- Params: 1 → `financewisepartners_dataVar.getPartnerLedgerId()` (setDouble) · 2 → `selectedfinanceid` (setDouble) · 3 → `financewisepartners_dataVar.getActive().booleanValue()` (setBoolean) · 4 → `financewisepartners_dataVar.getUID()` (setInt)

#### `ASYNCSaveFinancewisepartners.doInBackground` · line 368
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `tblFinwisePartners`
```sql
UPDATE tblFinwisePartners SET UID=${Integer.valueOf(((financewisepartners_data) financewisepartners_dataList.get(i)).getUID())}, PartnerLedgerId=${Integer.valueOf(financewisepartners_dataVar.getPartnerLedgerId())}, FinId=${Integer.valueOf(selectedfinanceid)}, isActive=${financewisepartners_dataVar.getActive()}
WHERE UID=?
```

- Selection args: `new String[]{String.valueOf(((financewisepartners_data) financewisepartners_dataList.get(…`

## fintrans_list

Screen: **ST / OF Loans list** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#st--of-loans-list--fintranslist) · 2 statements

#### `fetchdata_OFLoans` · line 240
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `OFLoanStatusforAPP`, `tblOFLoans`
```sql
select  b.LedgerId,b.LedgerName,b.AddLine1,b.AddLine2,b.City,a.OFLoanId,a.FWUDOFLoanId,a.PartyLedgerId,a.OFLoanAmount,a.FinId,a.OFLIntRateinDM,a.TransDate,c.Status
from tblOFLoans a
left
join  OFLoanStatusforAPP c  on a.OFLoanId=c.OFLoanId, tblLedgers b
where   a.PartyLedgerId=b.LedgerId
order by a.OFLoanId desc
```

- Reads: `LedgerName`, `LedgerId`, `Status`, `AddLine1`, `AddLine2`, `City`, `FinId`, `TransDate`, `FWUDOFLoanId`, `OFLoanId`, `OFLoanAmount`, `OFLIntRateinDM`

#### `fetchdata_stloans` · line 212
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `STLoanStatusforAPP`, `tblSTLoans`
```sql
select  b.LedgerId,b.LedgerName,b.AddLine1,b.AddLine2,b.City,a.STLoanId,a.FWUDSTLoanId,a.FinId,a.STLoanAmount,a.STLIntRateinDM,a.TransDate,c.Status
from tblSTLoans a
left
join  STLoanStatusforAPP c on a.STLoanId=c.STLoanId , tblLedgers b
where   a.PartyLedgerId=b.LedgerId
order by a.STLoanId desc
```

- Reads: `LedgerName`, `AddLine1`, `Status`, `LedgerId`, `FinId`, `AddLine2`, `City`, `TransDate`, `FWUDSTLoanId`, `STLoanId`, `STLoanAmount`, `STLIntRateinDM`

## loanledgers_list

Screen: **ST/OF loan ledgers register** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#stof-loan-ledgers-register--loanledgerslist) · 2 statements

#### `ASYNC_GetLoanLedgers.doInBackground` · line 230
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stloanledgers`
```sql
exec STLoanLedgers @FromDate = '${fromdate}', @ToDate = '${todate}' , @SelectedFinanceIds='${selectecdfinanceids}'
```

- Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`

#### `ASYNC_GetLoanLedgers.doInBackground` · line 247
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofloanledgers`
```sql
exec OFLoanLedgers @FromDate = '${fromdate}', @ToDate = '${todate}' , @SelectedFinanceIds='${selectecdfinanceids}'
```

- Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`

## parties_list

Screen: **Parties / Partners list and party picker** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#parties--partners-list-and-party-picker--partieslist) · 9 statements

#### `fetchdata` · line 330, 335
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where ScreenType=21 or ScreenType=22
order by LedgerName
```

#### `fetchdata` · line 332
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where ScreenType=${screentype}
order by LedgerName
```

#### `fetchdata` · line 339
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`, `tblSTLoans`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where LedgerId in (select PartyLedgerId
from tblSTLoans)
order by LedgerName
```

#### `fetchdata` · line 342
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`, `tblOFLoans`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where LedgerId in (select PartyLedgerId
from tblOFLoans)
order by LedgerName
```

#### `fetchdata` · line 346
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblFinwisePartners`, `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where  Ledgerid in (select PartnerLedgerId
from tblFinwisePartners
where isActive=1 and  FinId=${finid})order by LedgerName
```

#### `fetchdata` · line 348
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where ScreenType=21
order by LedgerName
```

#### `fetchdata` · line 350
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName
from tblLedgers
where ScreenType=21  or  ScreenType=22
order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`, `GroupId`, `OpeningBalance`, `OpBalType`, `FinId`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `MasterPartyLedgerId`, `ScreenType`, `PartyGroupId`, `IsDefault`, `IsReserved`, `ListSNo`, `PanNo`, `isActive`, `DesignationId`, `StatusId`, `AndroidDeviceId`, `AadhaarNo`, `Imagefilename`, `lat`, `lng`, `MartialStatus`, `Qualification`, `Profession`, `MonthlyIncome`, `GuardianMonthlyIncome`, `GuardianMobileNo1`, `GuardianMobileNo2`, `GuardianProfession`, `LandMark`, `SpouseName`, `FamilyName`

#### `ASYNCRefreshpartnerslist.doInBackground` · line 466
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, CWLedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName, Photo, AddLine1, AddLine2, City, PinCode, LandLinePhoneNo, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, AccountNo, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, CAId, CSAId, SubAreaNo, UDLedgerId, designationid, desigstatusid, statusid, Imagefilename, SpouseName, FamilyName, lat, lng, IdProofFileName, IdProofFileNameII, NoOfLoansAllowed, PanNo, isActive, LandMark, GuardianProfession, GuardianMobileNo2, GuardianMonthlyIncome, MonthlyIncome, Age, MartialStatus, Qualification, AndroidDeviceId, AadhaarNo, Profession, GuardianMobileNo1, MaximumAmountAllowed,isactive
from tblLedgers
where Screentype=22
order by LedgerId
```

- Reads: `LedgerId`, `CWLedgerId`, `LedgerName`, `GroupId`, `OpeningBalance`, `OpBalType`, `FinId`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `MasterPartyLedgerId`, `ScreenType`, `PartyGroupId`, `IsDefault`, `IsReserved`, `ListSNo`, `Imagefilename`, `SpouseName`, `FamilyName`, `LandMark`, `GuardianProfession`, `GuardianMobileNo2`, `GuardianMonthlyIncome`, `MonthlyIncome`, `Qualification`, `AndroidDeviceId`, `AadhaarNo`, `Profession`, `GuardianMobileNo1`, `MaximumAmountAllowed`, `isactive`

## partnerwiseloansandperformance_fragment

Screen: **Partner-wise interest pending (loans & performance page)** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#partner-wise-interest-pending-loans--performance-page--partnerwiseloansandperformancefragment) · 9 statements

#### `ASYNC_GetInterestPendingList.doInBackground` · line 772
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stlintcolpending`
```sql
exec STLIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
```

- Reads: `DOWNLOADDatetime`

#### `ASYNC_GetInterestPendingList.doInBackground` · line 777
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · DELETE · tables: `tempIntColPending`
```sql
delete from tempIntColPending where PaidInt>0
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 778
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `stlintcolpendinglist`
```sql
exec STLIntColPendingList
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 782
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempstlIntColPenidnglistnew`
```sql
Select typeid, FinId, STLoanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, STLoanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, STLoanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno
from tempstlIntColPenidnglistnew
where RecPartnerId=${partnerledgerid}
order by LedgerName
```

- Reads: `typeid`, `FinId`, `STLoanId`, `RecPartnerId`, `PartyLedgerId`, `STLoanAmountPaid`, `STLoanAmount`, `Receno`

#### `ASYNC_GetInterestPendingList.doInBackground` · line 880
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempstlIntColPenidnglistnew`
```sql
Select 1 as ShowId, PartyLedgerId, Ledgername, sum(Balance) as Balance, sum(CInt) as Interest
from tempstlIntColPenidnglistnew
where    RecPartnerId=${partnerledgerid} and typeid=1
group by PartyLedgerId, Ledgername
union all select 2 as showId, Null,'Total',sum(Balance),sum(CInt)
from tempstlIntColPenidnglistnew
where    RecPartnerId=${partnerledgerid} and typeid=1
order by showId,LedgerName
```

- Reads: `PartyLedgerId`, `Ledgername`, `Interest`

#### `ASYNC_GetInterestPendingList.doInBackground` · line 919
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofintcolpending`
```sql
exec ofIntColPending @IUPDate = '${todate}', @IntPaidDate ='${todate}', @SelectedFinanceIds='${selectecdfinanceids}'
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 923
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · EXEC · procedure: `ofintcolpendinglist`
```sql
exec ofIntColPendingList
```

#### `ASYNC_GetInterestPendingList.doInBackground` · line 926
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempoflIntColPenidnglistnew`
```sql
Select typeid, FinId, ofloanId, RecPartnerId, PartyLedgerId, IntRate, LoanDate, ofloanAmountPaid, LedgerName, MobileNo1, ShortName, LoanNo, ofloanAmount, Balance, IntFrom, Intupto, Days, CInt, PaidInt, Paidon, Receno
from tempoflIntColPenidnglistnew
where RecPartnerId=${partnerledgerid}
order by PartyLedgerId
```

- Reads: `FinId`, `ofloanId`, `RecPartnerId`, `PartyLedgerId`, `ofloanAmountPaid`, `ofloanAmount`, `Receno`

#### `ASYNC_GetInterestPendingList.doInBackground` · line 1026
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tempoflIntColPenidnglistnew`
```sql
Select  1 as ShowId, PartyLedgerId, Ledgername, sum(Balance) as Balance, sum(CInt) as Interest
from tempoflIntColPenidnglistnew
where    RecPartnerId=${partnerledgerid} and typeid=1
group by PartyLedgerId, Ledgername
union all select 2 as showId, Null,'Total',sum(Balance),sum(CInt)
from tempoflIntColPenidnglistnew
where    RecPartnerId=${partnerledgerid} and typeid=1
order by showId,LedgerName
```

- Reads: `PartyLedgerId`, `Ledgername`, `Interest`

## partyledgers_list

Screen: **Party loan-account statement** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#party-loan-account-statement--partyledgerslist) · 4 statements

#### `ASYNCDeleteselectedstloan.doInBackground` · line 337
**SQL Server** · `jdbc.prepareStatement` · branch DB · DELETESELECTEDLOANANDRELATEDENTRIES
```sql
DeleteSelectedLoanandRelatedEntries  @STLoanId=?
```

- Params: 1 → `loanordepositid` (setInt)

#### `ASYNCDeleteselectedstloan.doInBackground` · line 341
**local SQLite** · `sqlite.compileStatement` · DELETE · tables: `tblSTLoans`
```sql
delete from  tblSTLoans where STLoanId=?
```

- Params: 1 → `loanordepositid` (bindDouble)

#### `ASYNCGetAccountstransactionslist.doInBackground` · line 283
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC
```sql
exec ${exeprocname}
```

#### `ASYNCGetAccountstransactionslist.doInBackground` · line 283
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC
```sql
exec ${exeprocname} @FromDate='${fromdate}', @ToDate ='${todate}', @PartyLedgerId =${partyledgerid} , @SelectedFinanceIds='${selectecdfinanceids}'
```

## partyledgers_parameters

Screen: **ST/OF loan accounts & ledgers — parameters** · [spec](../../brain/modules/15-finance-transactions-lists-party-loan-ledgers.md#stof-loan-accounts--ledgers--parameters--partyledgersparameters) · 3 statements

#### `fetchemployeesandotherdetails` · line 496
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select  FinId,   FinMailingName  from FinanceDetails
```

- Reads: `FinId`, `FinMailingName`

#### `ASYNC_GetInterestPendingList.doInBackground` · line 902
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stloanledgers`
```sql
exec STLoanLedgers @FromDate = '${fromdate}', @ToDate = '${todate}' , @SelectedFinanceIds='${selectecdfinanceids}'
```

- Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`

#### `ASYNC_GetInterestPendingList.doInBackground` · line 918
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofloanledgers`
```sql
exec OFLoanLedgers @CFD = '${globalvars.accyearfromdate}' , @SelectedFinanceIds='${selectecdfinanceids}'
```

- Reads: `SNO`, `Party`, `LoanNo`, `IR`, `Return`, `Loan`, `Date`

