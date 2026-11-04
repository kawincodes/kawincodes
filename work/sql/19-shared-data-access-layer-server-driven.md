# SQL — 8.19 Shared data-access layer, server-driven settings and utilities

[← work.md](../../work.md) · [Module spec](../../brain/modules/19-shared-data-access-layer-server-driven.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **80 statements** (80 local SQLite, 0 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`defaulttables`](#defaulttables) (80)

## defaulttables

[spec](../../brain/modules/19-shared-data-access-layer-server-driven.md) · 80 statements

#### `createRelatedDocCollection` · line 1637
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `RelatedDocCollection`
```sql
Create table IF NOT EXISTS RelatedDocCollection(DCUId int,ServerGenMCLoanId int,MCLoanId int,DUID int,Remarks String,ImagePath String)
```

#### `createlastuploadtable` · line 1645
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LastUpload`
```sql
Create table IF NOT EXISTS LastUpload(MCLGCollId Double,FWGColId Double,TransId Double,SubmitAmount Double,SubmitedAt DATETIME DEFAULT CURRENT_TIMESTAMP,CollStaff2LedgerId double,CollStaffLedgerId double)
```

#### `createmobilappcoldata` · line 1633
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `MobileAppCollData`
```sql
Create table IF NOT EXISTS MobileAppCollData( MCLoanId int,FWUDMCLoanId int,Remarks varchar,PartyLedgerId int,MCLoanDate  Date, ClosingDate	Date, CWLedgerId int,	UDLedgerId int  ,PartyName Varchar,	SubAreaNo int	,LAmount	 int, DuesCount  int,	InsAmount	 int, MCLoanAmountCollectable int	,Collected int	,PaidDues int,	PassedDues 	int ,LoanMethodDesc Varchar,	CollectionBreakString Varchar,	NoOfIns int,	CBCount int, 	istobeWarned boolean,	IsNIP boolean,	LoanStatus  Varchar,IsMarked boolean,PrevCollected Int,IsVisited boolean,InterestAmount int, PenaltyCollected int, SavingsCollected int ,SubAreaName string,MCLDueCollId int  ,lng varchar,lat varchar,islocallyfixed boolean,TargetLedgerId int,LoanIndex int,NoofLoansAllowed int,CashCollection int,BankCollection int,DocCharges int,MaximumAmountAllowed int,IsAttentionRequired boolean,ShowId int)
```

#### `createnewmcloanstable` · line 1641
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `NewMCLoans`
```sql
CREATE TABLE IF NOT EXISTS NewMCLoans(MCLoanId int,MCLoanDate date,FinId int,PartyLedgerId int,MCLoanAmount int,TotalMonths int ,DocCharges int,LoanMethodId int,InterestAmount int,MCLoanAmountCollectable Double,InsAmount Double,MCLoanAmountEstimated int,SubAreaNo int,ServerGenMCLoanId int,ServerGenLedgerId int,Remarks nvarchar,ServerGenTransId int,WeekdayId int,MonthdayId int, ondaycollection int,mobileno string,Sourceledgerid int,PartyReferenced string,LoanIndex int,ProductValue int,AdvanceAmount int,IntRate double)
```

#### `createnewpartiestable` · line 1649
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `NewParties`
```sql
Create table IF NOT EXISTS NewParties (LedgerId int,CWLedgerId int,LedgerName Varchar,Tag VarChar,AreaNo int,SubAreaNo int,UDLedgerId int,MobileNo1 varchar,MobileNo2  Varchar,ServerGenLedgerId Double,AddLine1 varchar,SpouseName varchar, FamilyName varchar,ImagefileName varchar,IdProoffileName varchar,lat varchar,lng varchar,IdProofFileNameII varchar, NoOfLoansAllowed int,MaximumAmountAllowed int,IsActive boolean )
```

#### `createpartiestable` · line 1653
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `Parties`
```sql
Create table IF NOT EXISTS Parties (LedgerId int,CWLedgerId int,LedgerName Varchar,Tag VarChar,SubAreaNo int,UDLedgerId int,MobileNo1 varchar,MobileNo2  Varchar,AddLine1 varchar,SpouseName varchar, FamilyName varchar,ImagefileName varchar,IdProofFileName varchar,lat varchar,lng varchar,IdProofFileNameII varchar, NoOfLoansAllowed int,MaximumAmountAllowed int,IsActive boolean )
```

#### `createsubareadetails` · line 1629
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SubAreaDetails`
```sql
Create table IF NOT EXISTS SubAreaDetails(SubAreaNo Double,SubAreaName varchar,FinId int,WeekdayId int, IsVisible Boolean,ShowId Double,Latitude REAL, Longitude REAL )
```

#### `createtableBranchConnStrs` · line 1621
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchConnStrs`
```sql
Create table IF NOT EXISTS BranchConnStrs(BId int,BranchName String,ConStr String,FinCashLedgerId int,CollectionTeamCashLedgerId int,ServerName String,DatabaseName String,DBUserId String,DBPassword String,appconstr String,dbidentificationno String,ShowId int)
```

#### `createtablefinancedetails` · line 1613
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `FinanceDetails`
```sql
Create table IF NOT EXISTS FinanceDetails(FinId int,CollectionBreakString String ,FinName String,FinMailingName String,AddLine1 String,AddLine2 String,City String,PINcode String,RegistrationNo String,CashLedgerId Double,PhoneNo String,ShortName String,MobileNo String,Showid Double,IsVisible Boolean,AdminMobileNos String,DefaultLoanMethodId int,FinanceCashBookLedgerId Double,SMSIdentifier String,EmailIdentifier String,YearFrom date,BooksFrom date,BankLedgerid int,HLinecolor String,LoanNoSuffix String,SCopyImage String,FCopyImage String,LicenseDate date,RecentRenewal date,ExpiryDate date,RenewalAmount int,RenewalNoticeDate date,IsWeeklyFinance boolean,enableinventory boolean)
```

#### `createtableseFinancesinSelectedBranch` · line 1617
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SelectedBranchFinanceDetails`
```sql
Create table IF NOT EXISTS SelectedBranchFinanceDetails(FinId int,CollectionBreakString String ,FinName String,FinMailingName String,AddLine1 String,AddLine2 String,City String,PINcode String,RegistrationNo String,CashLedgerId Double,PhoneNo String,ShortName String,MobileNo String,Showid Double,IsVisible Boolean,AdminMobileNos String,DefaultLoanMethodId int,FinanceCashBookLedgerId Double,SMSIdentifier String,EmailIdentifier String,YearFrom date,BooksFrom date,BankLedgerid int,HLinecolor String,LoanNoSuffix String,SCopyImage String,FCopyImage String,LicenseDate date,RecentRenewal date,ExpiryDate date,RenewalAmount int,RenewalNoticeDate date,IsWeeklyFinance boolean,enableinventory boolean)
```

#### `createtabletblSalesDetailsforAndroid` · line 1625
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblSalesDetailsforAndroid`
```sql
Create table IF NOT EXISTS tblSalesDetailsforAndroid( mnuname string,mnudisplayname string,UserLevelId Int,mnuId Int,showid int, ShowState Boolean,ViewState Boolean,UpdateState Boolean,AddState Boolean,DeleteState Boolean,mnustyle int)
```

#### `emailidentifierforselectedfinance` · line 1738
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select EmailIdentifier from FinanceDetails  where FinId=${finid}
```

- Reads: `EmailIdentifier`

#### `fetchCollectionBreakStringforselectedfinance` · line 1698
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select CollectionBreakString from FinanceDetails  where FinId=${finid}
```

- Reads: `CollectionBreakString`

#### `fetchCollectionBreakStringforselectedfinance` · line 1707
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinanceCashBookLedgerId from FinanceDetails  where FinId=${finid}
```

- Reads: `CollectionBreakString`

#### `fetch_stockUOM` · line 101
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `StockUOM`
```sql
Select UOMId, UOMDesc   from StockUOM where IsActive=1  order by DisplayIndex
```

- Reads: `UOMDesc`, `UOMId`

#### `fetch_stockcat` · line 114
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Stockcat`
```sql
Select CatId, CatDesc from Stockcat where IsActive=1 order by DisplayIndex
```

- Reads: `CatDesc`, `CatId`

#### `fetch_stockgrp` · line 127
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Stockgrp`
```sql
Select GrpId, GrpDesc from Stockgrp where IsActive=1 order by DisplayIndex
```

- Reads: `GrpDesc`, `GrpId`

#### `fetchaccountgroupname` · line 1229
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblGroups`
```sql
Select GroupName from tblGroups  where GroupId=${groupid}
```

- Reads: `GroupName`

#### `fetchaccountgroups` · line 1289
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblGroups`
```sql
Select GroupId, GroupName  from tblGroups order by GroupName
```

- Reads: `GroupId`, `GroupName`

#### `fetchallloanmethods` · line 1961
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LoanMethods`
```sql
select  LoanMethodId,LoanMethodDesc from LoanMethods order by ShowId
```

- Reads: `LoanMethodId`, `LoanMethodDesc`

#### `fetchallpartiesinsubarea` · line 2062
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `Parties`
```sql
select PartyLedgerId,PartyName  || ', ' ||  tag  as PartyName
from MobileAppCollData a
left
join Parties  b on a.PartyLedgerId =b.LedgerId
where a.SubAreaNo= ${exceptsubareano}
order by PartyName
```

- Reads: `PartyLedgerId`, `PartyName`

#### `fetchallsubareas` · line 1948
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SubAreaDetails`
```sql
select SubAreaNo,SubAreaName from SubAreaDetails  where finid=${finid} order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`

#### `fetchallsubareaswithexceptsubarea` · line 2049
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SubAreaDetails`
```sql
select SubAreaNo,SubAreaName
from SubAreaDetails
where FinId=${finid} and SubAreaNo !=${exceptsubareano}
order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`

#### `fetchbankledgerlistforselectedfinance` · line 1974
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BankLedgers`
```sql
select  LedgerId,Ledgername from BankLedgers where FinId=${finid}  order by Ledgername
```

- Reads: `LedgerId`, `Ledgername`

#### `fetchcashbankledgergroupid` · line 763
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CashBankLedgers`
```sql
select LedgerId,LedgerName,GroupId from CashBankLedgers where  LedgerId=${ledgerid}
```

- Reads: `GroupId`

#### `fetchcashbankledgername` · line 749
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CashBankLedgers`
```sql
select LedgerId,LedgerName,GroupId from CashBankLedgers where  LedgerId=${ledgerid}
```

#### `fetchexpledgers` · line 1304
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpHeaders`
```sql
CREATE TABLE IF NOT EXISTS ExpHeaders(LedgerId Double,LedgerName nvarchar,TTId int)
```

#### `fetchexpledgers` · line 1305
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpHeaders`
```sql
Select LedgerId, LedgerName  from ExpHeaders where TTId=${ttid} order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

#### `fetchfinanceName` · line 1196
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select FinName From FinanceDetails where FinId=${finid}
```

#### `fetchfinancecashbookledgerid` · line 1721
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
Select FinanceCashBookLedgerId from SelectedBranchFinanceDetails  where FinId=${finid}
```

- Reads: `FinanceCashBookLedgerId`

#### `fetchfinancecashbookledgerid` · line 1727
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinanceCashBookLedgerId from FinanceDetails  where FinId=${finid}
```

- Reads: `FinanceCashBookLedgerId`

#### `fetchfinancelist` · line 419
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select FinId, FinMailingName From FinanceDetails
```

#### `fetchfinancename` · line 980
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select FinMailingName From FinanceDetails where FinId=${finid}
```

#### `fetchgroupid` · line 198
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
select GroupId from tblLedgers where  LedgerId=${ledgerid}
```

#### `fetchitemslist` · line 35
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `stockitems`
```sql
select ItemCode,ItemName,PRate  as Price  from  stockitems a order by ItemName
```

- Reads: `ItemName`, `ItemCode`, `Price`

#### `fetchitemslistforpurchase` · line 49
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ItemMaster`
```sql
select ItemCode,ItemName,0  as Price  from  ItemMaster   order by ItemName
```

- Reads: `ItemName`, `ItemCode`, `Price`

#### `fetchledgername` · line 721
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `qatledgers`
```sql
select LedgerId,LedgerName from qatledgers where  LedgerId=${ledgerid}
```

#### `fetchlistfornoofloansallowedfortheparty` · line 888
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LoansAllowed`, `Parties`
```sql
select  LId ,LoanIndex
from LoansAllowed
where LoanIndex<=(select NoOfLoansAllowed
from Parties
where LedgerId=${partyledgerid})
```

- Reads: `LoanIndex`, `LId`

#### `fetchmobileno2` · line 610
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
select LedgerId,MobileNo2 from tblLedgers where  LedgerId=${ledgerid}
```

#### `fetchnoofloansallowed` · line 875
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LoansAllowed`
```sql
select  LId ,LoanIndex from LoansAllowed
```

- Reads: `LoanIndex`, `LId`

#### `fetchpartybalance` · line 967
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select   MCLoanAmountCollectable -ifnull( PrevCollected,0)-ifnull( Collected,0) as Balance
from MobileAppCollData
where PartyLedgerId=${ledgerid}
```

- Reads: `Balance`

#### `fetchsmstemplatetext` · line 990
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=${stid}
```

- Reads: `SMSDesc`

#### `fetchsourceledgersforaccountstransactions` · line 1205
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `qatledgers`
```sql
Create table IF NOT EXISTS qatledgers(FinId int, TTId int,AType int,LedgerId Double,LedgerName String,isExecutiveLevelExpensesLedger bit,isExecutiveLevelReceiptsLedger bit)
```

#### `fetchsourceledgersforaccountstransactions` · line 1208
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `qatledgers`
```sql
Select TTId, AType, LedgerId , LedgerName
from qatledgers
where  LedgerId!= ${excludeledgerid} and   (finid=0 or finId=${finid}) and AType=${atype} and TTId= ${curttid}
order by ledgername
```

#### `fetchsourceledgersforaccountstransactions` · line 1210
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `qatledgers`
```sql
Select TTId, AType, LedgerId , LedgerName
from qatledgers
where LedgerId!= ${excludeledgerid} and  (finid=0 or finId=${finid}) and isExecutiveLevelExpensesLedger=1 and  AType=${atype} and TTId= ${curttid}
order by ledgername
```

#### `fetchsourceledgersforaccountstransactions` · line 1212
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `qatledgers`
```sql
Select TTId, AType, LedgerId , LedgerName
from qatledgers
where LedgerId!= ${excludeledgerid} and    (finid=0 or finId=${finid}) and isExecutiveLevelExpensesLedger=1 and  AType=${atype} and TTId= ${curttid}
order by ledgername
```

- Reads: `LedgerId`, `LedgerName`

#### `fetchstaffcashbookledgerid` · line 1597
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
Select CashLedgerId from SelectedBranchFinanceDetails  where FinId=${finid}
```

- Reads: `CashLedgerId`

#### `fetchstaffcashbookledgerid` · line 1603
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select CashLedgerId from  FinanceDetails  where FinId=${finid}
```

- Reads: `CashLedgerId`

#### `fetchstledgername` · line 707
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
select LedgerId,LedgerName from tblLedgers where  LedgerId=${ledgerid}
```

#### `fetchsubcollectiontargetledgers` · line 184
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
select LedgerId,LedgerName,GroupId
from tblLedgers
where isreserved=0 and finid=${finid} and  GroupId in (5,6)
```

#### `fetchsupplierledgername` · line 735
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Suppliers`
```sql
select ledgername from Suppliers where ledgerid=${ledgerid}
```

- Reads: `ledgername`

#### `fetchsuppliersandcustomers` · line 83
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Suppliers`
```sql
select ledgerid,ledgername,City,GroupId from Suppliers
```

- Reads: `ledgername`, `City`, `ledgerid`, `GroupId`

#### `fetchtargetledgers` · line 782
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CashBankLedgers`
```sql
select  LedgerId,Ledgername,GroupId from CashBankLedgers where FinId=  ${finid}
```

- Reads: `LedgerName`, `LedgerId`, `GroupId`

#### `fetchtargetledgersforcashandbank` · line 835
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CashBankLedgers`
```sql
select  LedgerId, Ledgername,GroupId from CashBankLedgers   where  Finid=${finid}
```

#### `fetchtargetledgersforcashandbank` · line 837
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CashBankLedgers`
```sql
select  LedgerId, Ledgername,GroupId from CashBankLedgers   where Alloted=1 and Finid=${finid}
```

- Reads: `LedgerName`, `LedgerId`, `GroupId`

#### `fetchtargetledgersforcashandbanktobeverified` · line 852
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CashBankLedgers`
```sql
select  LedgerId, Ledgername,istobeVerified,GroupId from CashBankLedgers  where Alloted=1 and  Finid=${finid}
```

- Reads: `LedgerName`, `LedgerId`, `istobeVerified`, `GroupId`

#### `fetchuserroledispindex` · line 638
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `userroles`
```sql
select DisplayIndex from userroles where  URId=${urid}
```

#### `fetchuserrolename` · line 624
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `userroles`
```sql
select URName from userroles where  URId=${urid}
```

#### `fetchuserroles` · line 546
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `UserRoles`
```sql
Select a.URId, a.URName, a.isvisible, a.DisplayIndex,a.isFullAccessAllowed
from UserRoles  a
order by a.DisplayIndex
```

- Reads: `URId`, `URName`

#### `fetchuserroles` · line 556
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `UserRoles`
```sql
Select a.URId, a.URName, a.isvisible, a.DisplayIndex,a.isFullAccessAllowed
from UserRoles  a ,UserRoles b
where b.URId=${globalvars.app_userroleid} and a.DisplayIndex>b.DisplayIndex
order by a.URId
```

- Reads: `URId`, `URName`

#### `fetchuserrolesforspecialaccessrules` · line 570
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `UserRoles`
```sql
Select a.URId, a.URName, a.isvisible, a.DisplayIndex,a.isFullAccessAllowed
from UserRoles  a ,UserRoles b
where a.isFullAccessAllowed=0 and b.URId=${globalvars.app_userroleid} and a.DisplayIndex>=b.DisplayIndex
order by a.URId
```

- Reads: `URId`, `URName`

#### `getBranchEMPLedgers` · line 596
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BranchEMPLedgers`
```sql
select LedgerId,LedgerName from BranchEMPLedgers order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

#### `getbankledgername` · line 2029
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BankLedgers`
```sql
select  Ledgername from BankLedgers where   LedgerId=${ledgerid}
```

- Reads: `Ledgername`

#### `getbranchfinanceobj` · line 933
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
Select FinId, FinName, AddLine1, AddLine2, City, PinCode, RegistrationNo, YearFrom, BooksFrom, ShortName, MobileNo, PhoneNo, EmailIdentifier, FinMailingName, CashLedgerId, AdminMobileNos, smsidentifier , BankLedgerid, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString, IsWeeklyFinance,enableinventory
from SelectedBranchFinanceDetails
where finid=${finid}
```

- Reads: `FinId`, `FinName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `BooksFrom`, `ShortName`, `EmailIdentifier`, `FinMailingName`, `CashLedgerId`, `AdminMobileNos`, `SMSIdentifier`, `BankLedgerid`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `CollectionBreakString`, `IsWeeklyFinance`, `enableinventory`

#### `getdefaultloanmthodidforfinance` · line 2039
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
select  DefaultLoanMethodId from FinanceDetails  where FinId=${finid}
```

- Reads: `DefaultLoanMethodId`

#### `getemps` · line 583
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `EMPLedgers`
```sql
select LedgerId,LedgerName from EMPLedgers where desigstatusid=1 order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

#### `getexpensesheadername` · line 1279
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpHeaders`
```sql
Select LedgerName from ExpHeaders  where LedgerId=${ledgerid}
```

- Reads: `LedgerName`

#### `getfinanceobj` · line 902
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, CashLedgerId, PhoneNo, ShortName, MobileNo, Showid, IsVisible, AdminMobileNos, DefaultLoanMethodId, FinanceCashBookLedgerId, SMSIdentifier, EmailIdentifier, YearFrom, BooksFrom, BankLedgerid,enableinventory
from FinanceDetails
where FinId=${finid}
```

- Reads: `FinId`, `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `CashLedgerId`, `ShortName`, `AdminMobileNos`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `SMSIdentifier`, `EmailIdentifier`, `BooksFrom`, `BankLedgerid`, `enableinventory`

#### `getitemnameforitemcode` · line 63
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Itemmaster`
```sql
select  ItemName from Itemmaster where  ItemCode=${ItemCode}
```

- Reads: `ItemName`

#### `getitemnameforitemcodefromstok` · line 73
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ItemMaster`
```sql
select  ItemName from ItemMaster where  ItemCode=${ItemCode}
```

- Reads: `ItemName`

#### `getloanmethodname` · line 2019
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LoanMethods`
```sql
select  LoanMethodDesc from LoanMethods where LoanMethodId=${loanmethodid}
```

- Reads: `LoanMethodDesc`

#### `getloanmthodparticularsforselectedloanmethod` · line 797
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LoanMethods`
```sql
Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount
from LoanMethods
where LoanMethodId=${LoanMethodId}
order by LoanMethodId
```

- Reads: `LoanMethodId`, `LoanMethodDesc`, `LoanAmount`, `LoanAmountGiven`, `IntDeduction`, `IntAddition`, `TotAmountCollectable`, `CollectionBreak`, `CollectionBreakString`, `NoOfIns`, `InsAmount`, `GracePeriod`, `DocChargesMethod`, `DocCharges`, `ReportIdentification`, `CBCount`, `IsVisible`, `ShowId`, `WarningDays`, `DueSupportAmount`

#### `getnewloanlist` · line 1015
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select MCLoanDate, SubAreaName as AreaName,CWLedgerId,ondaycollection,Ledgername as PartyName,Tag,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,  a.ServerGenMCLoanId, a.ServerGenLedgerId,Remarks, a.ServerGenTransId,a.WeekDayId,MonthDayId,mobileno,Sourceledgerid ,LoanIndex ,PartyReferenced,ProductValue,AdvanceAmount,IntRate
From NewMCLoans a,NewParties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
```

- Reads: `FinId`, `MCLoanId`, `CWLedgerId`, `Tag`, `MCLoanDate`, `PartyLedgerId`, `MCLoanAmount`, `ondaycollection`, `PartyName`, `AreaName`, `TotalMonths`, `DocCharges`, `LoanMethodId`, `InterestAmount`, `MCLoanAmountCollectable`, `InsAmount`, `MCLoanAmountEstimated`, `SubAreaNo`, `ServerGenMCLoanId`, `ServerGenLedgerId`, `Remarks`, `ServerGenTransId`, `WeekdayId`, `MonthdayId`, `mobileno`, `Sourceledgerid`, `PartyReferenced`, `LoanIndex`, `ProductValue`, `AdvanceAmount`, `IntRate`

#### `getnewloanlist` · line 1079
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select MCLoanDate, SubAreaName as AreaName,CWLedgerId,ondaycollection,Ledgername as PartyName,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,ServerGenMCLoanId,ServerGenLedgerId,Remarks,ServerGenTransId,a.WeekDayId,MonthDayId,mobileno,Sourceledgerid,PartyReferenced,LoanIndex ,PartyReferenced,ProductValue,AdvanceAmount,IntRate
From NewMCLoans a,Parties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
```

- Reads: `WeekdayId`, `MonthdayId`, `mobileno`, `ServerGenTransId`, `LoanIndex`, `ProductValue`, `AdvanceAmount`, `IntRate`, `PartyReferenced`

#### `getselectedpartiesloanindexwiseloans` · line 1990
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `loanindex`, `MobileAppCollData`, `NewMCLoans`
```sql
Select a.LedgerId, a.LoanIndex, a.MCLoanId, a.MCLoanAmountCollectable,a. collected, ifnull(c.MCLoanId,0) as CurLoanId, a.Balance+ifnull(b.MCLoanAmount,0) as Balance ,ifnull(b.MCLoanAmount,0) as NewLoanAmount,ifnull(c.Collected,0) as TodayCollection
from loanindex a
left
join NewMCLoans b  on a.ledgerid=b.PartyLedgerid   and a.LoanIndex=b.LoanIndex
Left
join MobileAppCollData c on a.ledgerid=c.PartyLedgerid   and a.LoanIndex=c.LoanIndex
where a.LedgerId=${partyledgerid}
order by a.LoanIndex
```

- Reads: `LedgerId`, `LoanIndex`, `MCLoanId`, `CurLoanId`, `MCLoanAmountCollectable`, `NewLoanAmount`, `collected`, `Balance`, `TodayCollection`

#### `getstockcatname` · line 150
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Stockcat`
```sql
Select CatDesc from Stockcat where CatId= ${catid}
```

- Reads: `CatDesc`

#### `getstockgrpname` · line 140
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Stockgrp`
```sql
Select GrpDesc from Stockgrp where GrpId= ${grpid}
```

- Reads: `GrpDesc`

#### `getstockuomname` · line 170
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `StockUOM`
```sql
Select UOMDesc from StockUOM where UOMId= ${uomid}
```

- Reads: `UOMDesc`

#### `getsubareaname` · line 160
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SubAreaDetails`
```sql
select SubAreaName from SubAreaDetails where SubAreaNo = ${SubAreaNo}
```

- Reads: `SubAreaName`

#### `partyobject` · line 396
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `NewParties`, `Parties`
```sql
select distinct(LedgerId) as LedgerId, LedgerName,Tag,UDLedgerId,SubAreaNo,CWLedgerId,MobileNo1,MobileNo2 ,PartyLedgerId,MaximumAmountAllowed
from (select a.*,ifnull(b.PartyLedgerId,0) as PartyLedgerId,MobileNo1,MobileNo2,a.CWLedgerId
from(select LedgerId,LedgerName,Tag,UDLedgerId,SubAreaNo,CWLedgerId,MobileNo1,MobileNo2 ,MaximumAmountAllowed
from Parties
union all select LedgerId,LedgerName,Tag,1000 as UDLedgerId,SubAreaNo, CWLedgerId,MobileNo1,MobileNo2,MaximumAmountAllowed
from NewParties  ) a
left
Join MobileAppCollData b on a.LedgerId=b.PartyLedgerId)x
where Ledgerid= ${ledgerid}
```

- Reads: `PartyLedgerId`, `LedgerId`, `CWLedgerId`, `LedgerName`, `MobileNo1`, `MobileNo2`, `Tag`, `SubAreaNo`, `MaximumAmountAllowed`

