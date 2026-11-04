# SQL — 8.2 Navigation hub, routers and line/branch selection

[← work.md](../../work.md) · [Module spec](../../brain/modules/02-navigation-hub-routers-line-branch-selection.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **162 statements** (123 local SQLite, 39 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`navigationdrawactivity`](#navigationdrawactivity) (34) · [`refreshorreloadbrancheslist`](#refreshorreloadbrancheslist) (76) · [`refreshorreloadfinancelist`](#refreshorreloadfinancelist) (23) · [`selectbranchandfinance`](#selectbranchandfinance) (29)

## navigationdrawactivity

Screen: **Home dashboard & navigation drawer** · [spec](../../brain/modules/02-navigation-hub-routers-line-branch-selection.md#home-dashboard--navigation-drawer--navigationdrawactivity) · 34 statements

#### `DisplayIndexData` · line 750
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select a.SubAreaNo, count(MCLoanId) as 'AreaNoCount' ,b.SubAreaName ,sum(Collected) as Collected,count(IsMarked) as MarkedCount
from MobileAppCollData a,SubAreaDetails b
where a.SubAreaNo=b.SubAreaNo
group by a.SubAreaNo,b.SubAreaName,b.Showid
order by b.ShowId
```

- Reads: `SubAreaName`, `SubAreaNo`, `AreaNoCount`, `Collected`

#### `DisplayIndexData` · line 770
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select count(IsMarked) as MarkedCount
from MobileAppCollData
where IsMarked=1 and SubAreaNo=${c.getInt(c.getColumnIndexOrThrow("SubAreaNo"))}
```

- Reads: `MarkedCount`

#### `DisplayIndexData` · line 775
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select count(IsNIP) as MarkedCount
from MobileAppCollData
where IsNIP=1 and SubAreaNo=${c.getInt(c.getColumnIndexOrThrow("SubAreaNo"))}
```

- Reads: `MarkedCount`

#### `DisplayIndexData` · line 780
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select count(istobeWarned) as MarkedCount
from MobileAppCollData
where  IsNIP=0 and istobeWarned=1 and SubAreaNo=${c.getInt(c.getColumnIndexOrThrow("SubAreaNo"))}
```

- Reads: `MarkedCount`

#### `DisplayIndexData` · line 791
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select  sum(InsAmount) as ColA,sum(Collected) ColH ,count(MCLoanId)  as ColCount
from MobileAppCollData
where ISNIP=0 and subareano>0
```

- Reads: `ColCount`, `ColH`, `ColA`

#### `DisplayIndexData` · line 800
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select  count(Collected) ColCount from MobileAppCollData where Collected>0 and ISNIP=0 and subareano>0
```

- Reads: `ColCount`

#### `DisplayIndexData` · line 833
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select  sum(InsAmount) as ColA,sum(Collected)ColH ,count(MCLoanId)  as ColCount
from MobileAppCollData
where ISNIP=1 and subareano>0
```

- Reads: `ColCount`, `ColH`, `ColA`

#### `DisplayIndexData` · line 841
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select  count(Collected) ColCount from MobileAppCollData where Collected>0 and ISNIP=1 and subareano>0
```

- Selection args: `strArr`
- Reads: `ColCount`

#### `onContactSelected` · line 1507
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select PaidDues,PassedDues,x.MCLoanId,x.FWUDMCLoanId,x.Remarks,x.PartyLedgerId,x.MCLoanDate, interestamount,x.ClosingDate,y.CWLedgerId,x.UDLedgerId,x.PartyName,y.Tag,x.SubAreaNo,x.LAmount,x.DuesCount,x.InsAmount,x.MCLoanAmountCollectable,x.Collected,x.PaidDues,x.PassedDues,x.LoanMethodDesc,x.CollectionBreakString,x.NoOfIns,x.CBCount,x.istobeWarned,x.IsNIP,x.LoanStatus,x.IsMarked,x.PrevCollected ,ImagefileName,IdProofFileName,y.lat,y.lng,IsVisited,PenaltyCollected , SavingsCollected ,CashCollection  ,BankCollection ,x.DocCharges ,x.IsAttentionRequired
from MobileAppCollData  x, Parties y
where x.PartyLedgerId=y.LedgerId
order by x.SubAreaNo,x.MCLoanDate desc
```

- Reads: `NoOfIns`, `DuesCount`, `MCLoanAmountCollectable`, `PrevCollected`, `InterestAmount`, `MCLoanDate`, `CWLedgerId`, `ClosingDate`, `LoanStatus`, `UDLedgerId`, `LoanMethodDesc`, `PartyName`, `Tag`, `IsVisited`, `Remarks`, `MCLoanId`, `FWUDMCLoanId`, `Collected`, `SubAreaNo`, `InsAmount`, `PaidDues`, `PassedDues`, `lat`, `lng`, `LAmount`, `IsNIP`, `IsMarked`, `PartyLedgerId`, `ImagefileName`, `IdProofFileName`, `PenaltyCollected`, `SavingsCollected`, `BankCollection`, `CashCollection`, `DocCharges`, `IsAttentionRequired`

#### `onNavigationItemSelected` · line 1242
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CashBook`
```sql
Create table IF NOT EXISTS CashBook (SNo int,Particulars varchar,debit int,credit int)
```

#### `onNavigationItemSelected` · line 1243
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CashBook`
```sql
delete from CashBook
```

#### `onNavigationItemSelected` · line 1292
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `closableliveloans`
```sql
Create table IF NOT EXISTS closableliveloans(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate date,ClosingDate String,CWLedgerId Double,UDLedgerId Int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount Int,MCLoanAmountCollectable Double,PrevCollected Int,PaidDues Int,PassedDues Int,LoanMethodDesc String,ReportIdentification String,CollectionBreakString String,NoOfIns Double,CBCount Int,istobeWarned Int,IsNIP Int,LoanStatus Double,SubAreaName String)
```

#### `onNavigationItemSelected` · line 1293
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `closableliveloans`
```sql
delete from closableliveloans
```

#### `syncsettings` · line 335
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblSalesDetailsforAndroid`
```sql
Select UserLevelId, mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState,mnuname,mnudisplayname,showid
from tblSalesDetailsforAndroid
where UserLevelId=${globalvars.app_userroleid}
order by UserLevelId
```

- Reads: `UserLevelId`, `mnuId`, `ShowState`, `ViewState`, `UpdateState`, `AddState`, `DeleteState`, `mnuname`, `mnudisplayname`, `showid`

#### `syncsettings` · line 356
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SpecialAccessAllowed`
```sql
Create table IF NOT EXISTS SpecialAccessAllowed(SAAId Int,UserLevelId Int,SARId Int,bitvalue Boolean)
```

#### `syncsettings` · line 357
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue from SpecialAccessAllowed order by SAAId
```

- Reads: `SARId`, `bitvalue`

#### `ASYNCGetDownloadUploadAllowedDetails.doInBackground` · line 1582
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `get_uploadlimitationsdaterange`
```sql
exec Get_UploadLimitationsDateRange @IMEI='${globalvars.device_id}'
```

#### `ASYNCGetDownloadUploadAllowedDetails.doInBackground` · line 1590
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `get_uploadlimitations`
```sql
exec Get_UploadLimitations @IMEI='${globalvars.device_id}'
```

#### `ASYNCGetDownloadUploadAllowedDetails.doInBackground` · line 1592
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `get_downloadlimitations`
```sql
exec Get_DownloadLimitations @IMEI='${globalvars.device_id}'
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1663
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GETALLOWEDFINANCES
```sql
GetAllowedFinances @IMEI='${globalvars.device_id}'
```

- Reads: `FinId`, `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `CashLedgerId`, `YearFrom`, `BooksFrom`, `ShortName`, `PhoneNo`, `MobileNo`, `AdminMobileNos`, `enableinventory`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `CollectionBreakString`, `EmailIdentifier`, `SMSIdentifier`, `BankLedgerid`, `Showid`, `HLinecolor`, `LoanNoSuffix`, `SCopyImage`, `FCopyImage`

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1708
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `FinanceDetails`
```sql
INSERT INTO FinanceDetails (FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, CashLedgerId, YearFrom, BooksFrom, ShortName, PhoneNo, MobileNo, AdminMobileNos, enableinventory, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString, EmailIdentifier, SMSIdentifier, BankLedgerid, Showid, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage, enableinventory)
VALUES (${Integer.valueOf(reset.getInt("FinId"))}, ${reset.getString("FinName")}, ${reset.getString("FinMailingName")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("PINcode")}, ${reset.getString("RegistrationNo")}, ${Integer.valueOf(reset.getInt("CashLedgerId"))}, ${reset.getString("YearFrom")}, ${reset.getString("BooksFrom")}, ${reset.getString("ShortName")}, ${reset.getString("PhoneNo")}, ${reset.getString("MobileNo")}, ${reset.getString("AdminMobileNos")}, ${Boolean.valueOf(reset.getBoolean("enableinventory"))}, ${Integer.valueOf(reset.getInt("DefaultLoanMethodId"))}, ${Integer.valueOf(reset.getInt("FinanceCashBookLedgerId"))}, ${reset.getString("CollectionBreakString")}, ${reset.getString("EmailIdentifier")}, ${reset.getString("SMSIdentifier")}, ${Integer.valueOf(reset.getInt("BankLedgerid"))}, ${Integer.valueOf(reset.getInt("Showid"))}, ${reset.getString("HLinecolor")}, ${reset.getString("LoanNoSuffix")}, ${reset.getString("SCopyImage")}, ${reset.getString("FCopyImage")}, ${Boolean.valueOf(reset.getBoolean("enableinventory"))})
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1732, 1734
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BankLedgers`
```sql
Create table IF NOT EXISTS BankLedgers(LedgerId int,Ledgername String ,FinId int)
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1733
**local SQLite** · `sqlite.execSQL` · DROP · tables: `BankLedgers`
```sql
drop table BankLedgers
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1735
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BankLedgers`
```sql
delete from BankLedgers
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1737
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, Ledgername,FinId from tblLedgers where Groupid=5 order by LedgerId
```

- Reads: `FinId`, `LedgerId`, `Ledgername`

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1743
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `BankLedgers`
```sql
INSERT INTO BankLedgers (FinId, LedgerId, Ledgername)
VALUES (${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("Ledgername")})
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1750
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinId,FinMailingName, AddLine1,AddLine2,SMSIdentifier,City,PINcode,RegistrationNo,FinName,BooksFrom,ShortName, AddLine1, AddLine2, City, PinCode, RegistrationNo, BooksFrom, ShortName, EmailIdentifier, SMSIdentifier, FinMailingName, CashLedgerId, AdminMobileNos, DefaultLoanMethodId, FinanceCashBookLedgerId,CollectionBreakString,BankLedgerid,HLinecolor,LoanNoSuffix,SCopyImage,FCopyImage,ShowId,enableinventory
from FinanceDetails
order by FinId
```

- Reads: `FinId`, `CollectionBreakString`, `AdminMobileNos`, `CashLedgerId`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `AddLine2`, `City`, `LoanNoSuffix`, `HLinecolor`, `SCopyImage`, `FCopyImage`

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1839
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1840
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1841
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1842
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1843
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

#### `ASYNCfirstautosyncprocess.doInBackground` · line 1844
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

## refreshorreloadbrancheslist

Screen: **Branch selection & branch re-sync** · [spec](../../brain/modules/02-navigation-hub-routers-line-branch-selection.md#branch-selection--branch-re-sync--refreshorreloadbrancheslist) · 76 statements

#### `fetchandloadbranchlist` · line 154
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BranchConnStrs`
```sql
select BID, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr,ShowId,dbidentificationno
from BranchConnStrs
order by showid
```

- Reads: `BId`, `BranchName`, `ConStr`, `FinCashLedgerId`, `CollectionTeamCashLedgerId`, `ServerName`, `DatabaseName`, `DBUserId`, `DBPassword`, `appconstr`, `dbidentificationno`, `ShowId`

#### `onContactSelected` · line 120
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `onContactSelected` · line 121
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `onContactSelected` · line 122
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `onContactSelected` · line 123
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `onContactSelected` · line 124
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

#### `onContactSelected` · line 125
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

#### `onContactSelected` · line 126
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceDetails`
```sql
delete from FinanceDetails
```

#### `onContactSelected` · line 127
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CollectionExecutives`
```sql
Create table IF NOT EXISTS CollectionExecutives (LedgerId Double,LedgerName VarChar)
```

#### `onContactSelected` · line 128
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CollectionExecutives`
```sql
delete from CollectionExecutives
```

#### `ASYNCGetbranchlist.doInBackground` · line 200
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceDetails`
```sql
delete from FinanceDetails
```

#### `ASYNCGetbranchlist.doInBackground` · line 201
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchAccessAllowed`
```sql
Create table IF NOT EXISTS BranchAccessAllowed(IMEI String,BId int)
```

#### `ASYNCGetbranchlist.doInBackground` · line 202
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BRANCHACCESSALLOWED`
```sql
delete from BRANCHACCESSALLOWED
```

#### `ASYNCGetbranchlist.doInBackground` · line 203
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchConnStrs`
```sql
delete from BranchConnStrs
```

#### `ASYNCGetbranchlist.doInBackground` · line 211
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
select count(IMEI) as imeicount from BranchMobileDevlicesAllowed Where IMEI='${globalvars.device_id}'
```

- Reads: `BranchName`, `ConStr`, `FinCashLedgerId`, `CollectionTeamCashLedgerId`, `BId`, `ServerName`, `DatabaseName`, `DBUserId`, `DBPassword`

#### `ASYNCGetbranchlist.doInBackground` · line 223
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · GETALLOWEDBRANCHSDETAILS
```sql
GetAllowedBranchsDetails @IMEI='${globalvars.device_id}'
```

#### `ASYNCGetbranchlist.doInBackground` · line 242
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `BranchConnStrs`
```sql
INSERT INTO BranchConnStrs (BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, BId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr, dbidentificationno, ShowId)
VALUES (${reset.getString("BranchName")}, ${reset.getString("ConStr")}, ${Integer.valueOf(reset.getInt("FinCashLedgerId"))}, ${Integer.valueOf(reset.getInt("CollectionTeamCashLedgerId"))}, ${Integer.valueOf(reset.getInt("BId"))}, ${reset.getString("ServerName")}, ${reset.getString("DatabaseName")}, ${reset.getString("DBUserId")}, ${reset.getString("DBPassword")}, ${reset.getString(str)}, ${reset.getString(str4)}, ${Integer.valueOf(reset.getInt(str5))})
```

#### `ASYNCSaveAdminDevice.doInBackground` · line 1013
**SQL Server** · `jdbc.Statement.executeUpdate` · master DB · BRANCH_SETRECEIVEDDEVICEASPRIMEDEVICE
```sql
BRANCH_SetReceivedDeviceasPrimeDevice  @AndroidDeviceId  ='${globalvars.device_id}'
```

#### `ASYNCSettings.doInBackground` · line 355
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CollectionExecutives`
```sql
Create table IF NOT EXISTS CollectionExecutives (LedgerId Double,LedgerName VarChar,FinId int)
```

#### `ASYNCSettings.doInBackground` · line 356
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CollectionExecutives`
```sql
delete from CollectionExecutives
```

#### `ASYNCSettings.doInBackground` · line 357
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoanMethods`
```sql
Create table IF NOT EXISTS LoanMethods(LoanMethodId Double,LoanMethodDesc String,LoanAmount Double,LoanAmountGiven Double,IntDeduction Double,IntAddition Double,TotAmountCollectable Double,CollectionBreak String,CollectionBreakString String,NoOfIns Double,InsAmount Double,GracePeriod Double,DocChargesMethod Double,DocCharges Double,ReportIdentification String,CBCount int,IsVisible Boolean,ShowId Double,WarningDays int,DueSupportAmount int)
```

#### `ASYNCSettings.doInBackground` · line 358
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoanMethods`
```sql
delete from LoanMethods
```

#### `ASYNCSettings.doInBackground` · line 359
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblGroups`
```sql
Create table IF NOT EXISTS tblGroups(GroupId Double,GroupName String,GroupUnder String,ShowId Decimal,isShowDetails String,isVisible String,ANId int)
```

#### `ASYNCSettings.doInBackground` · line 360
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblGroups`
```sql
delete from tblGroups
```

#### `ASYNCSettings.doInBackground` · line 361
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoansAllowed`
```sql
Create table IF NOT EXISTS LoansAllowed(LId Int,LoanIndex Int)
```

#### `ASYNCSettings.doInBackground` · line 362
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoansAllowed`
```sql
delete from LoansAllowed
```

#### `ASYNCSettings.doInBackground` · line 363
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblSTLoans`
```sql
Create table IF NOT EXISTS tblSTLoans(TransId Double,TransDate date,STLoanId Double,FWSTLoanId Double,FWUDSTLoanId Double,PartyLedgerId Double,STLoanAmount Double,STLIntRateinDM Double,STLIntRateinMM Double,STLPriIntRateinDM Double,STLSecIntRateinDM Double,STLTriIntRateinDM Double,RecPartnerId Double,RecPartyId Double,FinId Double,DocCharges Double,Remarks String,IntCalcStartOn date,IntCalcStartOnSYSSET date,CurStat Boolean,BankRefId int,BankRefNo String,SourceLedgerId Double,DCTargetLedgerId int)
```

#### `ASYNCSettings.doInBackground` · line 364
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblSTLoans`
```sql
delete from tblSTLoans
```

#### `ASYNCSettings.doInBackground` · line 365
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblOFLoans`
```sql
Create table IF NOT EXISTS tblOFLoans(TransId Double,TransDate date,OFLoanId Double,FWOFLoanId Double,FWUDOFLoanId Double,PartyLedgerId Double,OFLoanAmount Double,OFLIntRateinDM Double,OFLIntRateinMM Double,OFLIntRateinYM Double,OFLPriIntRateinDM Double,OFLSecRateinDM Double,OFLTriRateinDM Double,RecPartnerId Double,FinId Double,IntCalcStartOn date,IntCalcStartOnSYSSET date,CurStat Boolean,Remarks String,BankRefId int,BankRefNo String,DocCharges Double,SourceLedgerId Double)
```

#### `ASYNCSettings.doInBackground` · line 366
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblOFLoans`
```sql
delete from tblOFLoans
```

#### `ASYNCSettings.doInBackground` · line 367
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblLedgers`
```sql
Create table IF NOT EXISTS tblLedgers(LedgerId Double,LedgerName String,GroupId Double,OpeningBalance Double,OpBalType String,FinId Double,Title String,Tag String,GType String,GuardianName String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,MasterPartyLedgerId Double,ScreenType Double,PartyGroupId Double,IsDefault Double,IsReserved Double,ListSNo Double,PanNo String,isActive Boolean,DesignationId int,StatusId int,AndroidDeviceId String,AadhaarNo String,Imagefilename String,lat String,lng String,Age int,MartialStatus Boolean,Qualification String,Profession String,MonthlyIncome int,GuardianMonthlyIncome int,GuardianMobileNo1 String,GuardianMobileNo2 String,GuardianProfession String,LandMark String,SpouseName String,FamilyName String,desigstatusid int)
```

#### `ASYNCSettings.doInBackground` · line 368
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers
```

#### `ASYNCSettings.doInBackground` · line 369
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblFinwisePartners`
```sql
Create table IF NOT EXISTS tblFinwisePartners(UID Double,PartnerLedgerId Double,FinId Double,CapitalReceiptTransId Double,ListSNo Double, isActive Boolean)
```

#### `ASYNCSettings.doInBackground` · line 370
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblFinwisePartners`
```sql
delete from tblFinwisePartners
```

#### `ASYNCSettings.doInBackground` · line 372
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblSalesDetailsforAndroid`
```sql
delete from tblSalesDetailsforAndroid
```

#### `ASYNCSettings.doInBackground` · line 373
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle String,SMSDesc String)
```

#### `ASYNCSettings.doInBackground` · line 374
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SMSTemplates`
```sql
delete from SMSTemplates
```

#### `ASYNCSettings.doInBackground` · line 375
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `userroles`
```sql
Create table IF NOT EXISTS userroles(URId Int,URName String,isFullAccessAllowed Boolean,isvisible Boolean,DisplayIndex Int)
```

#### `ASYNCSettings.doInBackground` · line 376
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `userroles`
```sql
delete from userroles
```

#### `ASYNCSettings.doInBackground` · line 377
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CashBankLedgers`
```sql
Create table IF NOT EXISTS CashBankLedgers(LedgerId int,GroupId int,LedgerName String,Finid int,Alloted boolean,LedgerType Int,istobeVerified boolean)
```

#### `ASYNCSettings.doInBackground` · line 378
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CashBankLedgers`
```sql
delete from CashBankLedgers
```

#### `ASYNCSettings.doInBackground` · line 379
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SpecialAccessRules`
```sql
Create table IF NOT EXISTS SpecialAccessRules(SARId Int,SAName String,ValueType Int,isAllowed Boolean)
```

#### `ASYNCSettings.doInBackground` · line 380
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SpecialAccessRules`
```sql
delete from SpecialAccessRules
```

#### `ASYNCSettings.doInBackground` · line 381
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SpecialAccessAllowed`
```sql
Create table IF NOT EXISTS SpecialAccessAllowed(SAAId Int,UserLevelId Int,SARId Int,bitvalue Boolean)
```

#### `ASYNCSettings.doInBackground` · line 382
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SpecialAccessAllowed`
```sql
delete from SpecialAccessAllowed
```

#### `ASYNCSettings.doInBackground` · line 394
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `FinanceAccessAllowed`
```sql
select top 1 x.IMEI,userroleid,c.isFullAccessAllowed ,d.LedgerName
from FinanceAccessAllowed x , MobileDevlicesAllowed y,UserRoles c, tblLedgers d
where d.LedgerId=y.LId and y.userroleid=c.URId and x.IMEI=y.IMEI  and y.isActive =1 and x.IMEI='${globalvars.device_id}'
```

- Reads: `LedgerName`, `userroleid`, `isFullAccessAllowed`

#### `ASYNCSettings.doInBackground` · line 416
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SMSTemplates`
```sql
Select STId, SMSTitle, SMSDesc from SMSTemplates order by STId
```

- Reads: `STId`, `SMSTitle`, `SMSDesc`

#### `ASYNCSettings.doInBackground` · line 422
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SMSTemplates`
```sql
INSERT INTO SMSTemplates (STId, SMSTitle, SMSDesc)
VALUES (${Integer.valueOf(reset.getInt("STId"))}, ${reset.getString("SMSTitle")}, ${reset.getString("SMSDesc")})
```

#### `ASYNCSettings.doInBackground` · line 431
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `AndroidMenus`, `tblSalesDetailsforAndroid`
```sql
Select mnuname,mnudisplayname,  UserLevelId, a.mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState,mnustyle
from AndroidMenus a
left
join tblSalesDetailsforAndroid b on a.mnuid=b.mnuid and b.UserLevelId=${globalvars.app_userroleid}
where a.isAllowed =1 and isonlyformasterdb='${globalvars.ismasterdb}'
order by mnuId
```

- Reads: `UserLevelId`, `mnuId`, `ShowState`, `ViewState`, `UpdateState`, `AddState`, `DeleteState`, `mnuname`, `mnudisplayname`

#### `ASYNCSettings.doInBackground` · line 443
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblSalesDetailsforAndroid`
```sql
INSERT INTO tblSalesDetailsforAndroid (UserLevelId, mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState, mnuname, mnudisplayname)
VALUES (${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("mnuId"))}, ${Boolean.valueOf(reset.getBoolean("ShowState"))}, ${Boolean.valueOf(reset.getBoolean("ViewState"))}, ${Boolean.valueOf(reset.getBoolean("UpdateState"))}, ${Boolean.valueOf(reset.getBoolean("AddState"))}, ${Boolean.valueOf(reset.getBoolean("DeleteState"))}, ${reset.getString("mnuname")}, ${reset.getString("mnudisplayname")})
```

#### `ASYNCSettings.doInBackground` · line 445
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `userroles`
```sql
Select URId, URName, isvisible, DisplayIndex,isFullAccessAllowed from userroles order by URId
```

- Reads: `URId`, `URName`, `isvisible`, `DisplayIndex`, `isFullAccessAllowed`

#### `ASYNCSettings.doInBackground` · line 453
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `userroles`
```sql
INSERT INTO userroles (URId, URName, isvisible, DisplayIndex, isFullAccessAllowed)
VALUES (${Integer.valueOf(reset.getInt("URId"))}, ${reset.getString("URName")}, ${Boolean.valueOf(reset.getBoolean("isvisible"))}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Integer.valueOf(reset.getInt("isFullAccessAllowed"))})
```

#### `ASYNCSettings.doInBackground` · line 455
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · EXEC · procedure: `getcashandbankledgers`
```sql
exec getCashandBankLedgers @URId=${globalvars.app_userroleid}
```

- Reads: `LedgerId`, `LedgerName`, `Finid`, `Alloted`, `LedgerType`, `istobeVerified`

#### `ASYNCSettings.doInBackground` · line 464
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `CashBankLedgers`
```sql
INSERT INTO CashBankLedgers (LedgerId, LedgerName, Finid, Alloted, LedgerType, istobeVerified)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Integer.valueOf(reset.getInt("Finid"))}, ${Boolean.valueOf(reset.getBoolean("Alloted"))}, ${Integer.valueOf(reset.getInt("LedgerType"))}, ${Boolean.valueOf(reset.getBoolean("istobeVerified"))})
```

#### `ASYNCSettings.doInBackground` · line 466
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `qatledgers`
```sql
Create table IF NOT EXISTS qatledgers(FinId int, TTId int,AType int,LedgerId Double,LedgerName String,isExecutiveLevelExpensesLedger bit,isExecutiveLevelReceiptsLedger bit)
```

#### `ASYNCSettings.doInBackground` · line 467
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `qatledgers`
```sql
delete from qatledgers
```

#### `ASYNCSettings.doInBackground` · line 468
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · EXEC · procedure: `get_ledgersforquicktransactions`
```sql
exec Get_LedgersforQuickTransactions
```

- Reads: `TTId`, `FinId`, `AType`, `LedgerId`, `LedgerName`, `isExecutiveLevelExpensesLedger`, `isExecutiveLevelReceiptsLedger`

#### `ASYNCSettings.doInBackground` · line 478
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `qatledgers`
```sql
INSERT INTO qatledgers (TTId, FinId, AType, LedgerId, LedgerName, isExecutiveLevelExpensesLedger, isExecutiveLevelReceiptsLedger)
VALUES (${Integer.valueOf(reset.getInt("TTId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt("AType"))}, ${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Boolean.valueOf(reset.getBoolean("isExecutiveLevelExpensesLedger"))}, ${Boolean.valueOf(reset.getBoolean("isExecutiveLevelReceiptsLedger"))})
```

#### `ASYNCSettings.doInBackground` · line 480
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SpecialAccessRules`
```sql
Select SARId, SAName, ValueType, isAllowed from SpecialAccessRules order by SARId
```

- Reads: `SARId`, `SAName`, `ValueType`, `isAllowed`

#### `ASYNCSettings.doInBackground` · line 487
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SpecialAccessRules`
```sql
INSERT INTO SpecialAccessRules (SARId, SAName, ValueType, isAllowed)
VALUES (${Integer.valueOf(reset.getInt("SARId"))}, ${reset.getString("SAName")}, ${Integer.valueOf(reset.getInt("ValueType"))}, ${Boolean.valueOf(reset.getBoolean("isAllowed"))})
```

#### `ASYNCSettings.doInBackground` · line 490
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue
from SpecialAccessAllowed
where UserLevelId=${globalvars.app_userroleid}
order by SAAId
```

- Reads: `SAAId`, `UserLevelId`, `SARId`, `bitvalue`

#### `ASYNCSettings.doInBackground` · line 497
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SpecialAccessAllowed`
```sql
INSERT INTO SpecialAccessAllowed (SAAId, UserLevelId, SARId, bitvalue)
VALUES (${Integer.valueOf(reset.getInt("SAAId"))}, ${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("SARId"))}, ${Boolean.valueOf(reset.getBoolean("bitvalue"))})
```

#### `ASYNCSettings.doInBackground` · line 499
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue
from SpecialAccessAllowed
where UserLevelId=${globalvars.app_userroleid}
order by SAAId
```

- Reads: `SARId`, `bitvalue`

#### `ASYNCSettings.doInBackground` · line 540
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `LoanMethods`
```sql
DELETE FROM LoanMethods WHERE LoanMethodId > ?
```

- Selection args: `strArr4`

#### `ASYNCSettings.doInBackground` · line 541
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `LoanMethods`
```sql
Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount
from LoanMethods
order by LoanMethodId
```

- Reads: `LoanMethodId`, `LoanMethodDesc`, `LoanAmount`, `DueSupportAmount`, `LoanAmountGiven`, `IntDeduction`, `IntAddition`, `TotAmountCollectable`, `CollectionBreak`, `CollectionBreakString`, `NoOfIns`, `InsAmount`, `GracePeriod`, `DocChargesMethod`, `DocCharges`, `ReportIdentification`, `CBCount`, `IsVisible`, `ShowId`, `WarningDays`

#### `ASYNCSettings.doInBackground` · line 564
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoanMethods`
```sql
INSERT INTO LoanMethods (LoanMethodId, LoanMethodDesc, LoanAmount, DueSupportAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays)
VALUES (${Double.valueOf(reset.getDouble("LoanMethodId"))}, ${reset.getString("LoanMethodDesc")}, ${Double.valueOf(reset.getDouble("LoanAmount"))}, ${Double.valueOf(reset.getDouble("DueSupportAmount"))}, ${Double.valueOf(reset.getDouble("LoanAmountGiven"))}, ${Double.valueOf(reset.getDouble("IntDeduction"))}, ${Double.valueOf(reset.getDouble("IntAddition"))}, ${Double.valueOf(reset.getDouble("TotAmountCollectable"))}, ${reset.getString("CollectionBreak")}, ${reset.getString("CollectionBreakString")}, ${Double.valueOf(reset.getDouble("NoOfIns"))}, ${Double.valueOf(reset.getDouble("InsAmount"))}, ${Double.valueOf(reset.getDouble("GracePeriod"))}, ${Double.valueOf(reset.getDouble("DocChargesMethod"))}, ${Double.valueOf(reset.getDouble("DocCharges"))}, ${reset.getString("ReportIdentification")}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${Integer.valueOf(reset.getInt("WarningDays"))})
```

#### `ASYNCSettings.doInBackground` · line 571
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `tblGroups`
```sql
Select GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId
from tblGroups
order by GroupName
```

- Reads: `GroupId`, `GroupName`, `GroupUnder`, `ShowId`, `isShowDetails`, `isVisible`, `ANId`

#### `ASYNCSettings.doInBackground` · line 581
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `tblGroups`
```sql
INSERT INTO tblGroups (GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId)
VALUES (${Double.valueOf(reset.getDouble("GroupId"))}, ${reset.getString("GroupName")}, ${reset.getString("GroupUnder")}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${reset.getString("isShowDetails")}, ${reset.getString("isVisible")}, ${Integer.valueOf(reset.getInt("ANId"))})
```

#### `ASYNCSettings.doInBackground` · line 583
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `LoansAllowed`
```sql
Select LId, LoanIndex from LoansAllowed order by LId
```

- Reads: `LId`, `LoanIndex`

#### `ASYNCSettings.doInBackground` · line 588
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoansAllowed`
```sql
INSERT INTO LoansAllowed (LId, LoanIndex)
VALUES (${Integer.valueOf(reset.getInt("LId"))}, ${Integer.valueOf(reset.getInt("LoanIndex"))})
```

#### `ASYNCSettings.doInBackground` · line 595
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SoftSettings`
```sql
Select SSId,SSDesc,SSValue,Datevalue,bitvalue,NumericValue from SoftSettings
```

- Reads: `bitValue`, `NumericValue`, `SSValue`, `VSSId`, `BitValue`, `CharValue`

#### `ASYNCSettings.doInBackground` · line 707
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `venussoftsettings`
```sql
Select  VSSId,VSSName,CharValue,NumericValue,BitValue,DateValue from venussoftsettings
```

#### `ASYNCSettings.doInBackground` · line 782
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `tblLedgers`
```sql
select LedgerId,LedgerName,FinId from tblLedgers where  ScreenType=8  and designationid=1 and desigstatusid=1
```

- Reads: `LedgerId`, `LedgerName`, `FinId`

#### `ASYNCSettings.doInBackground` · line 788
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `CollectionExecutives`
```sql
INSERT INTO CollectionExecutives (LedgerId, LedgerName, FinId)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Integer.valueOf(reset.getInt("FinId"))})
```

#### `ASYNCSettings.doInBackground` · line 796
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
select isbranchusercontrolallowed from BranchMobileDevlicesAllowed where IMEI='${globalvars.device_id}'
```

- Reads: `isbranchusercontrolallowed`

#### `ASYNC_checkisdbhasadminuser.doInBackground` · line 915
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
select isnull(IMEI,0) as AndroidDeviceId  from BranchMobileDevlicesAllowed Where isbranchusercontrolallowed=1
```

## refreshorreloadfinancelist

Screen: **Line (finance) list & line chooser** · [spec](../../brain/modules/02-navigation-hub-routers-line-branch-selection.md#line-finance-list--line-chooser--refreshorreloadfinancelist) · 23 statements

#### `fetchandloadfinancelist` · line 268
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinId,FinMailingName, AddLine1,AddLine2,SMSIdentifier,City,PINcode,RegistrationNo,FinName,BooksFrom,ShortName, AddLine1, AddLine2, City, PinCode, RegistrationNo, BooksFrom, ShortName, EmailIdentifier, SMSIdentifier, FinMailingName, CashLedgerId, AdminMobileNos, DefaultLoanMethodId, FinanceCashBookLedgerId,CollectionBreakString,BankLedgerid,HLinecolor,LoanNoSuffix,SCopyImage,FCopyImage,ShowId,enableinventory
from FinanceDetails
order by ShowId
```

- Reads: `FinId`, `FinMailingName`, `CollectionBreakString`, `AdminMobileNos`, `CashLedgerId`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `FinName`, `BooksFrom`, `ShortName`, `EmailIdentifier`, `SMSIdentifier`, `BankLedgerid`, `LoanNoSuffix`, `HLinecolor`, `SCopyImage`, `FCopyImage`, `enableinventory`

#### `onActivityResult` · line 551
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `closableliveloans`
```sql
Create table IF NOT EXISTS closableliveloans(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate date,ClosingDate String,CWLedgerId Double,UDLedgerId Int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount Int,MCLoanAmountCollectable Double,PrevCollected Int,PaidDues Int,PassedDues Int,LoanMethodDesc String,ReportIdentification String,CollectionBreakString String,NoOfIns Double,CBCount Int,istobeWarned Int,IsNIP Int,LoanStatus Double,SubAreaName String)
```

#### `onActivityResult` · line 552
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `closableliveloans`
```sql
delete from closableliveloans
```

#### `onContactSelected` · line 159
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `closableliveloans`
```sql
Create table IF NOT EXISTS closableliveloans(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate date,ClosingDate String,CWLedgerId Double,UDLedgerId Int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount Int,MCLoanAmountCollectable Double,PrevCollected Int,PaidDues Int,PassedDues Int,LoanMethodDesc String,ReportIdentification String,CollectionBreakString String,NoOfIns Double,CBCount Int,istobeWarned Int,IsNIP Int,LoanStatus Double,SubAreaName String)
```

#### `onContactSelected` · line 160
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `closableliveloans`
```sql
delete from closableliveloans
```

#### `onContactSelected` · line 227
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `onContactSelected` · line 228
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `onContactSelected` · line 229
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `onContactSelected` · line 230
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `onContactSelected` · line 231
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

#### `onContactSelected` · line 232
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

#### `ASYNCGetFinancelist.doInBackground` · line 330
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceDetails`
```sql
delete from FinanceDetails
```

#### `ASYNCGetFinancelist.doInBackground` · line 333
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SelectedBranchFinanceDetails`
```sql
delete from SelectedBranchFinanceDetails
```

#### `ASYNCGetFinancelist.doInBackground` · line 341
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GETALLOWEDFINANCES
```sql
GetAllowedFinances @IMEI='${globalvars.device_id}'
```

- Reads: `FinId`, `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `CashLedgerId`, `YearFrom`, `BooksFrom`, `FinanceCashBookLedgerId`, `CollectionBreakString`, `EmailIdentifier`, `SMSIdentifier`, `BankLedgerid`, `Showid`, `HLinecolor`, `LoanNoSuffix`, `SCopyImage`, `FCopyImage`

#### `ASYNCGetFinancelist.doInBackground` · line 390
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `FinanceDetails`
```sql
INSERT INTO FinanceDetails (FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, CashLedgerId, YearFrom, BooksFrom, ShortName, PhoneNo, MobileNo, AdminMobileNos, enableinventory, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString, EmailIdentifier, SMSIdentifier, BankLedgerid, Showid, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage, enableinventory)
VALUES (${Integer.valueOf(reset.getInt("FinId"))}, ${reset.getString("FinName")}, ${reset.getString("FinMailingName")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("PINcode")}, ${reset.getString("RegistrationNo")}, ${Integer.valueOf(reset.getInt("CashLedgerId"))}, ${reset.getString("YearFrom")}, ${reset.getString("BooksFrom")}, ${reset.getString(str9)}, ${reset.getString(str10)}, ${reset.getString(str11)}, ${reset.getString(str12)}, ${Boolean.valueOf(reset.getBoolean(str13))}, ${Integer.valueOf(reset.getInt(str14))}, ${Integer.valueOf(reset.getInt("FinanceCashBookLedgerId"))}, ${reset.getString("CollectionBreakString")}, ${reset.getString("EmailIdentifier")}, ${reset.getString("SMSIdentifier")}, ${Integer.valueOf(reset.getInt("BankLedgerid"))}, ${Integer.valueOf(reset.getInt("Showid"))}, ${reset.getString("HLinecolor")}, ${reset.getString("LoanNoSuffix")}, ${reset.getString("SCopyImage")}, ${reset.getString("FCopyImage")}, ${Boolean.valueOf(reset.getBoolean(str13))})
```

#### `ASYNCGetFinancelist.doInBackground` · line 396, 398
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BankLedgers`
```sql
Create table IF NOT EXISTS BankLedgers(LedgerId int,Ledgername String ,FinId int)
```

#### `ASYNCGetFinancelist.doInBackground` · line 397
**local SQLite** · `sqlite.execSQL` · DROP · tables: `BankLedgers`
```sql
drop table BankLedgers
```

#### `ASYNCGetFinancelist.doInBackground` · line 399
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BankLedgers`
```sql
delete from BankLedgers
```

#### `ASYNCGetFinancelist.doInBackground` · line 400
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, Ledgername,FinId from tblLedgers where Groupid=5 order by LedgerId
```

- Reads: `FinId`

#### `ASYNCGetFinancelist.doInBackground` · line 408
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `BankLedgers`
```sql
INSERT INTO BankLedgers (FinId, LedgerId, Ledgername)
VALUES (${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt(str15))}, ${reset.getString(str16)})
```

#### `ASYNCSaveAdminDevice.doInBackground` · line 595
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · SETRECEIVEDDEVICEASPRIMEDEVICE
```sql
SetReceivedDeviceasPrimeDevice  @AndroidDeviceId  ='${globalvars.device_id}'
```

#### `ASYNC_checkisdbhasadminuser.doInBackground` · line 480
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
select count(*)  as AdminCount
From MobileDevlicesAllowed a, UserRoles b
where a.UserRoleId=b.URId and isFullAccessAllowed=1
```

## selectbranchandfinance

Screen: **Branch / line / period picker** · [spec](../../brain/modules/02-navigation-hub-routers-line-branch-selection.md#branch--line--period-picker--selectbranchandfinance) · 29 statements

#### `fetchloadfinancelist` · line 721
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
select FinId, FinMailingName,CollectionBreakString,IsWeeklyFinance from SelectedBranchFinanceDetails
```

- Reads: `FinId`, `FinMailingName`, `CollectionBreakString`, `IsWeeklyFinance`

#### `getbranchnames` · line 1210
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BranchConnStrs`
```sql
select BID, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr
from BranchConnStrs
order by ShowId
```

- Reads: `BId`, `BranchName`, `ConStr`, `FinCashLedgerId`, `CollectionTeamCashLedgerId`, `ServerName`, `DatabaseName`, `DBUserId`, `DBPassword`, `appconstr`

#### `lambda$onCreate$13` · line 597
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `closableliveloans`
```sql
Create table IF NOT EXISTS closableliveloans(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate date,ClosingDate String,CWLedgerId Double,UDLedgerId Int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount Int,MCLoanAmountCollectable Double,PrevCollected Int,PaidDues Int,PassedDues Int,LoanMethodDesc String,ReportIdentification String,CollectionBreakString String,NoOfIns Double,CBCount Int,istobeWarned Int,IsNIP Int,LoanStatus Double,SubAreaName String)
```

#### `lambda$onCreate$13` · line 598
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `closableliveloans`
```sql
delete from closableliveloans
```

#### `onActivityResult` · line 1294
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `closableliveloans`
```sql
Create table IF NOT EXISTS closableliveloans(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate date,ClosingDate String,CWLedgerId Double,UDLedgerId Int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount Int,MCLoanAmountCollectable Double,PrevCollected Int,PaidDues Int,PassedDues Int,LoanMethodDesc String,ReportIdentification String,CollectionBreakString String,NoOfIns Double,CBCount Int,istobeWarned Int,IsNIP Int,LoanStatus Double,SubAreaName String)
```

#### `onActivityResult` · line 1295
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `closableliveloans`
```sql
delete from closableliveloans
```

#### `ASYNCDataforAttendance.doInBackground` · line 1369
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblledgers`
```sql
select LedgerId,LedgerName,designationid
from tblledgers
where ScreenType =8 and designationid in (1,2)  and desigstatusid =1
order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

#### `ASYNCDataforAttendance.doInBackground` · line 1378
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `TwenDuration`
```sql
select IntCalDate
from TwenDuration
where IntCalDate >='${fromdate}' and IntCalDate <='${todate}'order by IntCalDate
```

- Reads: `IntCalDate`

#### `ASYNCDataforAttendance.doInBackground` · line 1386
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `collectiondayandcollectionexecutiveforapp`
```sql
exec  CollectionDayandCollectionExecutiveforapp   @FromDate ='${fromdate}', @ToDate ='${todate}'
```

- Reads: `attdate`, `finname`, `ledgerid`

#### `ASYNCGet7DaysCollection.doInBackground` · line 781
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SelectedFinanceSubAreaDetailsALL`
```sql
Create table IF NOT EXISTS SelectedFinanceSubAreaDetailsALL (FinId int,SubAreaId int ,SubAreaName Varchar)
```

#### `ASYNCGet7DaysCollection.doInBackground` · line 782
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SelectedFinanceSubAreaDetailsALL`
```sql
delete from SelectedFinanceSubAreaDetailsALL
```

#### `ASYNCGet7DaysCollection.doInBackground` · line 783
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SelectedFinanceSubAreaDetails`
```sql
Create table IF NOT EXISTS SelectedFinanceSubAreaDetails (FinId int,SubAreaId int ,SubAreaName Varchar)
```

#### `ASYNCGet7DaysCollection.doInBackground` · line 784
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SelectedFinanceSubAreaDetails`
```sql
delete from SelectedFinanceSubAreaDetails
```

#### `ASYNCGet7DaysCollection.doInBackground` · line 785
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · GETALLOWEDFINANCESUBAREAS
```sql
GetAllowedFinanceSubAreas @IMEI='${globalvars.device_id}'
```

- Reads: `FinId`, `SubAreaNo`, `SubAreaName`

#### `ASYNCGet7DaysCollection.doInBackground` · line 792
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `SelectedFinanceSubAreaDetailsALL`
```sql
INSERT INTO SelectedFinanceSubAreaDetailsALL ( FinId,SubAreaId,SubAreaName  )
VALUES (${reset.getInt("FinId")},${reset.getInt("SubAreaNo")},'${reset.getString("SubAreaName")}')
```

#### `ASYNCGet7DaysCollection.doInBackground` · line 804
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `app_get7dayscollectionfordailyloans`
```sql
exec [APP_Get7daysCollectionfordailyloans] @EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
```

- Reads: `UdLedgerId`, `CWLedgerId`, `MCLoanId`, `FWUDMCLoanId`, `FinId`, `PartyLedgerId`, `SubAreaNo`, `LedgerName`, `MCLoanDate`, `LoanStatus`, `ReportIdentification`, `Tag`, `IsAttentionRequired`

#### `ASYNCGet7DaysCollection.doInBackground` · line 854
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `app_get7dayscollectionforweeklyloans`
```sql
exec [APP_Get7daysCollectionforweeklyloans] @EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
```

#### `ASYNCGet7DaysCollection.doInBackground` · line 856
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `app_get7dayscollectionforweeklyloanswithweekdayid`
```sql
exec [APP_Get7daysCollectionforweeklyloanswithWeekdayid] @WeekdayId=${WeekDayId} ,@EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
```

#### `ASYNCGet7DaysCollection.doInBackground` · line 904
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `app_get7dayscollectionformonthlyloans`
```sql
exec [APP_Get7daysCollectionforMonthlyloans] @EndDate='${fromdate}', @Daystoadd=0, @FinId=${financeid}
```

- Reads: `UdLedgerId`, `MCLoanId`, `FWUDMCLoanId`, `PartyLedgerId`, `LedgerName`, `MCLoanDate`, `LoanStatus`, `Tag`, `ReportIdentification`, `IsAttentionRequired`

#### `ASYNCGet7DaysCollection.onPostExecute` · line 980
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedFinanceSubAreaDetailsALL`
```sql
select SubAreaId,SubAreaName  from SelectedFinanceSubAreaDetailsALL where finid=${financeid}
```

- Reads: `SubAreaId`, `SubAreaName`

#### `ASYNCGet7DaysCollection.onPostExecute` · line 1007
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `SelectedFinanceSubAreaDetails`
```sql
INSERT INTO SelectedFinanceSubAreaDetails ( FinId,SubAreaId,SubAreaName  )
VALUES (${financeid},${data.getSubAreaNo()},'${data.getSubAreaName()}')
```

#### `ASYNCGetFinancelist.doInBackground` · line 1057
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SelectedBranchFinanceDetails`
```sql
delete from SelectedBranchFinanceDetails
```

#### `ASYNCGetFinancelist.doInBackground` · line 1066
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · GETALLOWEDFINANCES
```sql
GetAllowedFinances @IMEI='${globalvars.device_id}'
```

- Reads: `FinId`, `FinName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `RegistrationNo`, `YearFrom`, `ShortName`, `MobileNo`, `PhoneNo`, `EmailIdentifier`, `FinMailingName`, `CashLedgerId`, `AdminMobileNos`, `smsidentifier`, `BankLedgerid`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `CollectionBreakString`, `IsWeeklyFinance`

#### `ASYNCGetFinancelist.doInBackground` · line 1092
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SelectedBranchFinanceDetails`
```sql
INSERT INTO SelectedBranchFinanceDetails (FinId, FinName, AddLine1, AddLine2, City, PinCode, RegistrationNo, YearFrom, BooksFrom, ShortName, MobileNo, PhoneNo, EmailIdentifier, FinMailingName, CashLedgerId, AdminMobileNos, smsidentifier, BankLedgerid, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString, IsWeeklyFinance)
VALUES (${Integer.valueOf(reset.getInt("FinId"))}, ${reset.getString("FinName")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("PinCode")}, ${reset.getString("RegistrationNo")}, ${reset.getString("YearFrom")}, ${reset.getString(str)}, ${reset.getString("ShortName")}, ${reset.getString("MobileNo")}, ${reset.getString("PhoneNo")}, ${reset.getString("EmailIdentifier")}, ${reset.getString("FinMailingName")}, ${Double.valueOf(reset.getDouble("CashLedgerId"))}, ${reset.getString("AdminMobileNos")}, ${reset.getString("smsidentifier")}, ${Integer.valueOf(reset.getInt("BankLedgerid"))}, ${Integer.valueOf(reset.getInt("DefaultLoanMethodId"))}, ${Double.valueOf(reset.getDouble("FinanceCashBookLedgerId"))}, ${reset.getString("CollectionBreakString")}, ${Integer.valueOf(reset.getInt("IsWeeklyFinance"))})
```

#### `ASYNCGetFinancelist.doInBackground` · line 1097
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoanMethods`
```sql
Create table IF NOT EXISTS LoanMethods(LoanMethodId Double,LoanMethodDesc String,LoanAmount Double,LoanAmountGiven Double,IntDeduction Double,IntAddition Double,TotAmountCollectable Double,CollectionBreak String,CollectionBreakString String,NoOfIns Double,InsAmount Double,GracePeriod Double,DocChargesMethod Double,DocCharges Double,ReportIdentification String,CBCount int,IsVisible Boolean,ShowId Double,WarningDays int,DueSupportAmount int)
```

#### `ASYNCGetFinancelist.doInBackground` · line 1098
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoanMethods`
```sql
delete from LoanMethods
```

#### `ASYNCGetFinancelist.doInBackground` · line 1099
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `LoanMethods`
```sql
Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount
from LoanMethods
order by LoanMethodId
```

- Reads: `LoanMethodId`, `LoanMethodDesc`, `IntAddition`, `TotAmountCollectable`, `CollectionBreak`, `CollectionBreakString`, `NoOfIns`, `InsAmount`, `GracePeriod`, `DocChargesMethod`, `DocCharges`, `ReportIdentification`, `CBCount`, `IsVisible`, `ShowId`, `WarningDays`

#### `ASYNCGetFinancelist.doInBackground` · line 1126
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoanMethods`
```sql
INSERT INTO LoanMethods (LoanMethodId, LoanMethodDesc, LoanAmount, DueSupportAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays)
VALUES (${Double.valueOf(reset.getDouble("LoanMethodId"))}, ${reset.getString("LoanMethodDesc")}, ${Double.valueOf(reset.getDouble(str6))}, ${Double.valueOf(reset.getDouble(str7))}, ${Double.valueOf(reset.getDouble(str8))}, ${Double.valueOf(reset.getDouble(str9))}, ${Double.valueOf(reset.getDouble("IntAddition"))}, ${Double.valueOf(reset.getDouble("TotAmountCollectable"))}, ${reset.getString("CollectionBreak")}, ${reset.getString("CollectionBreakString")}, ${Double.valueOf(reset.getDouble("NoOfIns"))}, ${Double.valueOf(reset.getDouble("InsAmount"))}, ${Double.valueOf(reset.getDouble("GracePeriod"))}, ${Double.valueOf(reset.getDouble("DocChargesMethod"))}, ${Double.valueOf(reset.getDouble("DocCharges"))}, ${reset.getString("ReportIdentification")}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${Integer.valueOf(reset.getInt("WarningDays"))})
```

#### `ASYNCGetFinancelist.doInBackground` · line 1135
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `SoftSettings`
```sql
Select SSId,SSDesc,SSValue,Datevalue,BitValue from SoftSettings
```

- Reads: `SSId`, `BitValue`, `NumericValue`

