# SQL — 8.1 App start, registration, device identity, settings and access control

[← work.md](../../work.md) · [Module spec](../../brain/modules/01-app-start-registration-device-identity-settings.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **291 statements** (137 local SQLite, 154 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`branchmobiledeviceaccescontrol_act`](#branchmobiledeviceaccescontrolact) (10) · [`deviceidentity`](#deviceidentity) (2) · [`mobiledeviceaccescontrol_act`](#mobiledeviceaccescontrolact) (16) · [`otpverificationact`](#otpverificationact) (105) · [`settings_act`](#settingsact) (40) · [`specialaccesscontrolact`](#specialaccesscontrolact) (41) · [`syncresources`](#syncresources) (75) · [`userroleprevilages_act`](#userroleprevilagesact) (2)

## branchmobiledeviceaccescontrol_act

Screen: **Branch-level device access** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#branch-level-device-access--branchmobiledeviceaccescontrolact) · 10 statements

#### `fetchbranchaccessallowed` · line 199
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BranchAccessAllowed`, `BranchConnStrs`
```sql
select a.BId,case when b.BId is null then 0 else 1 end as isSelected,BranchName
from BranchConnStrs a
left
join BranchAccessAllowed b on (a.BId=b.BId and b.IMEI='${curmaobj.getIMEI()}')
order by a.BId
```

- Reads: `BId`, `BranchName`, `isSelected`

#### `ASYNCgetrecentid.doInBackground` · line 254
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`, `BranchNewIMEIRequests`
```sql
select top 1 IMEI,RequestRaisedAt
from  BranchNewIMEIRequests
where IMEI  not in (select IMEI
from BranchMobileDevlicesAllowed)
order by NIRId desc
```

- Reads: `IMEI`, `RequestRaisedAt`

#### `async_accessrights.doInBackground` · line 315
**SQL Server** · `jdbc.prepareCall` · master DB · CALL · procedure: `insertbranchmobiledevlicesallowed`
```sql
{call dbo.InsertBranchMobileDevlicesAllowed(?,?,?,?,? )}
```

- Params: "IMEI" → `curmaobj.getIMEI()` (setString) · "LId" → `curmaobj.getLId()` (setInt) · "isActive" → `curmaobj.isActive()` (setBoolean) · "isbranchusercontrolallowed" → `curmaobj.isIsbranchusercontrolallowed()` (setBoolean)
- OUT: "MDAId" (8)
- Reads: `MDAId`

#### `async_accessrights.doInBackground` · line 329
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchMobileDevlicesAllowed`
```sql
INSERT INTO BranchMobileDevlicesAllowed (MDAId, IMEI, LId, isActive,  isbranchusercontrolallowed)
VALUES (${Integer.valueOf(curmaobj.getMDAId())}, ${curmaobj.getIMEI()}, ${globalvars.selectedemployeelegerid}, ${Boolean.valueOf(curmaobj.isActive())}, ${Boolean.valueOf(curmaobj.isIsbranchusercontrolallowed())})
```

#### `async_accessrights.doInBackground` · line 332, 355
**SQL Server** · `jdbc.prepareStatement` · master DB · INSERT · tables: `BranchAccessAllowed`
```sql
Insert into  BranchAccessAllowed (IMEI ,BId, MDAId) values(?,?,?)
```

- Params: 1 → `curmaobj.getIMEI()` (setString) · 2 → `((markeditems_data) markeditems_dataList.get(i2)).getCgid()` (setInt) · 3 → `curmaobj.getMDAId()` (setInt)

#### `async_accessrights.doInBackground` · line 343
**SQL Server** · `jdbc.prepareStatement` · master DB · UPDATE · tables: `BranchMobileDevlicesAllowed`
```sql
update BranchMobileDevlicesAllowed set IMEI=?,LId=?,isActive=?,isbranchusercontrolallowed=? where MDAId=?
```

- Params: 1 → `curmaobj.getIMEI()` (setString) · 2 → `curmaobj.getLId()` (setInt) · 3 → `curmaobj.isActive()` (setBoolean) · 4 → `curmaobj.isIsbranchusercontrolallowed()` (setBoolean) · 5 → `curmaobj.getMDAId()` (setInt)

#### `async_accessrights.doInBackground` · line 350, 363
**SQL Server** · `jdbc.prepareStatement` · master DB · DELETE · tables: `BranchAccessAllowed`
```sql
delete  from  BranchAccessAllowed where  MDAId=?
```

- Params: 1 → `curmaobj.getMDAId()` (setInt)

#### `async_accessrights.doInBackground` · line 366
**SQL Server** · `jdbc.prepareStatement` · master DB · DELETE · tables: `BranchMobileDevlicesAllowed`
```sql
delete from BranchMobileDevlicesAllowed where  MDAId=?
```

- Params: 1 → `curmaobj.getMDAId()` (setInt)

## deviceidentity

Screen: **Device and APP Info** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#device-and-app-info--deviceidentity) · 2 statements

#### `asyncconfirmbranchaccessrequest.doInBackground` · line 249
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
select count(IMEI) as imeicount from BranchMobileDevlicesAllowed Where IMEI='${globalvars.device_id}'
```

- Reads: `imeicount`

#### `asyncraisebranchaccessrequest.doInBackground` · line 193
**SQL Server** · `jdbc.prepareCall` · master DB · CALL · procedure: `insertbranchnewimeirequests`
```sql
{call dbo.InsertBranchNewIMEIRequests(?,?,?)}
```

- Params: "IMEI" → `globalvars.device_id` (setString)
- OUT: "NIRId" (8) · "RequestRaisedAt" (91)
- Reads: `NIRId`, `RequestRaisedAt`

## mobiledeviceaccescontrol_act

Screen: **Mobile device access setup** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#mobile-device-access-setup--mobiledeviceaccescontrolact) · 16 statements

#### `fetchdata` · line 215
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceAccessAllowed`, `FinanceDetails`
```sql
select a.FinId,case when b.FinId is null then 0 else 1 end as isSelected,FinMailingName
from FinanceDetails a
left
join FinanceAccessAllowed b on (a.FInId=b.FinId and b.IMEI='${curmaobj.getIMEI()}')
order by a.FinId
```

- Reads: `FinId`, `FinMailingName`, `isSelected`

#### `ASYNCSSaveNewMarks.doInBackground` · line 378
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertmobiledevlicesallowed`
```sql
{call dbo.InsertMobileDevlicesAllowed(?,?,?,?,?)}
```

- Params: "IMEI" → `curmaobj.getIMEI()` (setString) · "LId" → `curmaobj.getLId()` (setDouble) · "isActive" → `curmaobj.getActive().booleanValue()` (setBoolean) · "userroleid" → `curmaobj.getUserroleid()` (setInt)
- OUT: "MDAId" (8)
- Reads: `MDAId`

#### `ASYNCSSaveNewMarks.doInBackground` · line 388, 441
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceAccessAllowed`
```sql
delete from FinanceAccessAllowed where IMEI='${curmaobj.getIMEI()}'
```

#### `ASYNCSSaveNewMarks.doInBackground` · line 398
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `FinanceAccessAllowed`
```sql
insert into FinanceAccessAllowed(MDAId,IMEI,FinId)
values(${curmaobj.getMDAId()},'${curmaobj.getIMEI()}',${((markeditems_data) markeditems_dataList.get(i2)).getCgid()});
```

#### `ASYNCSSaveNewMarks.doInBackground` · line 405
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · DELETE · tables: `FinanceAccessAllowed`
```sql
delete
from FinanceAccessAllowed
where IMEI='${curmaobj.getIMEI()}'insert into FinanceAccessAllowed(MDAId,IMEI,FinId)
values(${curmaobj.getMDAId()},'${curmaobj.getIMEI()}',${((markeditems_data) markeditems_dataList.get(i2)).getCgid()});;
```

#### `ASYNCSSaveNewMarks.doInBackground` · line 412
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `MobileDevlicesAllowed`
```sql
INSERT INTO MobileDevlicesAllowed (MDAId, IMEI, LId, isActive, userroleid)
VALUES (${Integer.valueOf(curmaobj.getMDAId())}, ${curmaobj.getIMEI()}, ${Integer.valueOf(curmaobj.getLId())}, ${curmaobj.getActive()}, ${Integer.valueOf(curmaobj.getUserroleid())})
```

#### `ASYNCSSaveNewMarks.doInBackground` · line 424
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `MobileDevlicesAllowed`
```sql
delete from  MobileDevlicesAllowed where IMEI=?
```

- Params: 1 → `curmaobj.getIMEI()` (setString)

#### `ASYNCSSaveNewMarks.doInBackground` · line 427
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `FinanceAccessAllowed`
```sql
delete from  FinanceAccessAllowed where IMEI=?
```

- Params: 1 → `curmaobj.getIMEI()` (setString)

#### `ASYNCSSaveNewMarks.doInBackground` · line 430
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `MobileDevlicesAllowed`
```sql
DELETE FROM MobileDevlicesAllowed WHERE IMEI=?
```

- Selection args: `new String[]{String.valueOf(curmaobj.getIMEI())}`

#### `ASYNCSSaveNewMarks.doInBackground` · line 433
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `MobileDevlicesAllowed`
```sql
update MobileDevlicesAllowed set IMEI=?,LId=?,isActive=?,userroleid=?  where MDAId=?
```

- Params: 1 → `curmaobj.getIMEI()` (setString) · 2 → `curmaobj.getLId()` (setDouble) · 3 → `curmaobj.getActive().booleanValue()` (setBoolean) · 4 → `curmaobj.getUserroleid()` (setInt) · 5 → `curmaobj.getMDAId()` (setInt)

#### `ASYNCSSaveNewMarks.doInBackground` · line 450
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `FinanceAccessAllowed`
```sql
insert into FinanceAccessAllowed(IMEI,FinId)
values('${curmaobj.getIMEI()}',${((markeditems_data) markeditems_dataList.get(i4)).getCgid()});
```

#### `ASYNCSSaveNewMarks.doInBackground` · line 454
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · DELETE · tables: `FinanceAccessAllowed`
```sql
delete
from FinanceAccessAllowed
where IMEI='${curmaobj.getIMEI()}'insert into FinanceAccessAllowed(IMEI,FinId)
values('${curmaobj.getIMEI()}',${((markeditems_data) markeditems_dataList.get(i4)).getCgid()});;
```

#### `ASYNCSSaveNewMarks.doInBackground` · line 461
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `MobileDevlicesAllowed`
```sql
UPDATE MobileDevlicesAllowed SET MDAId=${Integer.valueOf(curmaobj.getMDAId())}, IMEI=${curmaobj.getIMEI()}, LId=${Integer.valueOf(curmaobj.getLId())}, isActive=${curmaobj.getActive()}, userroleid=${Integer.valueOf(curmaobj.getUserroleid())}
WHERE MDAId=?
```

- Selection args: `new String[]{String.valueOf(curmaobj.getMDAId())}`

#### `ASYNCgetrecentid.doInBackground` · line 290
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `FinanceAccessAllowed`, `MobileDevlicesAllowed`
```sql
delete from FinanceAccessAllowed where IMEI not in (select IMEI from MobileDevlicesAllowed )
```

#### `ASYNCgetrecentid.doInBackground` · line 292
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceAccessAllowed`, `NewIMEIRequests`
```sql
select top 1 IMEI,RequestRaisedAt
from  NewIMEIRequests
where IMEI  not in (select IMEI
from FinanceAccessAllowed)
order by NIRId desc
```

- Reads: `IMEI`, `RequestRaisedAt`

## otpverificationact

Screen: **OTP verification and demo admin-device registration** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#otp-verification-and-demo-admin-device-registration--otpverificationact) · 105 statements

#### `ASYNCSettings.doInBackground` · line 323, 325
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CollectionExecutives`
```sql
Create table IF NOT EXISTS CollectionExecutives (LedgerId Double,LedgerName VarChar,FinId int)
```

#### `ASYNCSettings.doInBackground` · line 324
**local SQLite** · `sqlite.execSQL` · DROP · tables: `CollectionExecutives`
```sql
drop table  CollectionExecutives
```

#### `ASYNCSettings.doInBackground` · line 326
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CollectionExecutives`
```sql
delete from CollectionExecutives
```

#### `ASYNCSettings.doInBackground` · line 327
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoanMethods`
```sql
Create table IF NOT EXISTS LoanMethods(LoanMethodId Double,LoanMethodDesc String,LoanAmount Double,LoanAmountGiven Double,IntDeduction Double,IntAddition Double,TotAmountCollectable Double,CollectionBreak String,CollectionBreakString String,NoOfIns Double,InsAmount Double,GracePeriod Double,DocChargesMethod Double,DocCharges Double,ReportIdentification String,CBCount int,IsVisible Boolean,ShowId Double,WarningDays int,DueSupportAmount int)
```

#### `ASYNCSettings.doInBackground` · line 328
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoanMethods`
```sql
delete from LoanMethods
```

#### `ASYNCSettings.doInBackground` · line 329
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblGroups`
```sql
Create table IF NOT EXISTS tblGroups(GroupId Double,GroupName String,GroupUnder String,ShowId Decimal,isShowDetails String,isVisible String,ANId int)
```

#### `ASYNCSettings.doInBackground` · line 330
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblGroups`
```sql
delete from tblGroups
```

#### `ASYNCSettings.doInBackground` · line 331
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoansAllowed`
```sql
Create table IF NOT EXISTS LoansAllowed(LId Int,LoanIndex Int)
```

#### `ASYNCSettings.doInBackground` · line 332
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoansAllowed`
```sql
delete from LoansAllowed
```

#### `ASYNCSettings.doInBackground` · line 333
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblSTLoans`
```sql
Create table IF NOT EXISTS tblSTLoans(TransId Double,TransDate date,STLoanId Double,FWSTLoanId Double,FWUDSTLoanId Double,PartyLedgerId Double,STLoanAmount Double,STLIntRateinDM Double,STLIntRateinMM Double,STLPriIntRateinDM Double,STLSecIntRateinDM Double,STLTriIntRateinDM Double,RecPartnerId Double,RecPartyId Double,FinId Double,DocCharges Double,Remarks String,IntCalcStartOn date,IntCalcStartOnSYSSET date,CurStat Boolean,BankRefId int,BankRefNo String,SourceLedgerId Double,DCTargetLedgerId int)
```

#### `ASYNCSettings.doInBackground` · line 334
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblSTLoans`
```sql
delete from tblSTLoans
```

#### `ASYNCSettings.doInBackground` · line 335
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblOFLoans`
```sql
Create table IF NOT EXISTS tblOFLoans(TransId Double,TransDate date,OFLoanId Double,FWOFLoanId Double,FWUDOFLoanId Double,PartyLedgerId Double,OFLoanAmount Double,OFLIntRateinDM Double,OFLIntRateinMM Double,OFLIntRateinYM Double,OFLPriIntRateinDM Double,OFLSecRateinDM Double,OFLTriRateinDM Double,RecPartnerId Double,FinId Double,IntCalcStartOn date,IntCalcStartOnSYSSET date,CurStat Boolean,Remarks String,BankRefId int,BankRefNo String,DocCharges Double,SourceLedgerId Double)
```

#### `ASYNCSettings.doInBackground` · line 336
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblOFLoans`
```sql
delete from tblOFLoans
```

#### `ASYNCSettings.doInBackground` · line 337
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblLedgers`
```sql
Create table IF NOT EXISTS tblLedgers(LedgerId Double,LedgerName String,GroupId Double,OpeningBalance Double,OpBalType String,FinId Double,Title String,Tag String,GType String,GuardianName String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,MasterPartyLedgerId Double,ScreenType Double,PartyGroupId Double,IsDefault Double,IsReserved Double,ListSNo Double,PanNo String,isActive Boolean,DesignationId int,StatusId int,AndroidDeviceId String,AadhaarNo String,Imagefilename String,lat String,lng String,Age int,MartialStatus Boolean,Qualification String,Profession String,MonthlyIncome int,GuardianMonthlyIncome int,GuardianMobileNo1 String,GuardianMobileNo2 String,GuardianProfession String,LandMark String,SpouseName String,FamilyName String,desigstatusid int)
```

#### `ASYNCSettings.doInBackground` · line 338
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers
```

#### `ASYNCSettings.doInBackground` · line 339
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblFinwisePartners`
```sql
Create table IF NOT EXISTS tblFinwisePartners(UID Double,PartnerLedgerId Double,FinId Double,CapitalReceiptTransId Double,ListSNo Double, isActive Boolean)
```

#### `ASYNCSettings.doInBackground` · line 340
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblFinwisePartners`
```sql
delete from tblFinwisePartners
```

#### `ASYNCSettings.doInBackground` · line 353
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle String,SMSDesc String)
```

#### `ASYNCSettings.doInBackground` · line 354
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SMSTemplates`
```sql
delete from SMSTemplates
```

#### `ASYNCSettings.doInBackground` · line 355
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `SMSTemplates`
```sql
Select STId, SMSTitle, SMSDesc from SMSTemplates order by STId
```

- Reads: `STId`, `SMSTitle`, `SMSDesc`

#### `ASYNCSettings.doInBackground` · line 361
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SMSTemplates`
```sql
INSERT INTO SMSTemplates (STId, SMSTitle, SMSDesc)
VALUES (${Integer.valueOf(reset.getInt("STId"))}, ${reset.getString("SMSTitle")}, ${reset.getString("SMSDesc")})
```

#### `ASYNCSettings.doInBackground` · line 368
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblSalesDetailsforAndroid`
```sql
delete from tblSalesDetailsforAndroid
```

#### `ASYNCSettings.doInBackground` · line 370
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `AndroidMenus`, `tblSalesDetailsforAndroid`
```sql
select a.mnuid,a.mnulevel,a.mnuName,b.ShowState as 'Select',a.mnudisplayname,b.ViewState as 'View' ,b.UpdateState as 'Update' ,b.AddState as 'Add' ,b.DeleteState  as 'Delete',mnustyle
from AndroidMenus a
left
join tblSalesDetailsforAndroid b on a.mnuId=b.mnuid and b.UserLevelId=${globalvars.app_userroleid}
where isallowed=1 and (isonlyformasterdb=0 or isonlyformasterdb='${globalvars.ismasterdb}')
ORDER BY MNUID
```

- Reads: `UserLevelId`, `mnuId`, `ShowState`, `ViewState`, `UpdateState`, `AddState`, `DeleteState`, `mnuname`, `mnudisplayname`

#### `ASYNCSettings.doInBackground` · line 382
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblSalesDetailsforAndroid`
```sql
INSERT INTO tblSalesDetailsforAndroid (UserLevelId, mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState, mnuname, mnudisplayname)
VALUES (${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("mnuId"))}, ${Boolean.valueOf(reset.getBoolean("ShowState"))}, ${Boolean.valueOf(reset.getBoolean("ViewState"))}, ${Boolean.valueOf(reset.getBoolean("UpdateState"))}, ${Boolean.valueOf(reset.getBoolean("AddState"))}, ${Boolean.valueOf(reset.getBoolean("DeleteState"))}, ${reset.getString("mnuname")}, ${reset.getString("mnudisplayname")})
```

#### `ASYNCSettings.doInBackground` · line 384, 394
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SpecialAccessAllowed`
```sql
Create table IF NOT EXISTS SpecialAccessAllowed(SAAId Int,UserLevelId Int,SARId Int,bitvalue Boolean)
```

#### `ASYNCSettings.doInBackground` · line 385, 395
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue
from SpecialAccessAllowed
where UserLevelId= ${globalvars.app_userroleid}
```

- Reads: `SAAId`, `UserLevelId`, `SARId`, `bitvalue`

#### `ASYNCSettings.doInBackground` · line 392, 402
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SpecialAccessAllowed`
```sql
INSERT INTO SpecialAccessAllowed (SAAId, UserLevelId, SARId, bitvalue)
VALUES (${Integer.valueOf(reset.getInt("SAAId"))}, ${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("SARId"))}, ${Boolean.valueOf(reset.getBoolean("bitvalue"))})
```

#### `ASYNCSettings.doInBackground` · line 405
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue from SpecialAccessAllowed order by SAAId
```

- Reads: `SARId`, `bitvalue`

#### `ASYNCSettings.doInBackground` · line 434
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `LoanMethods`
```sql
Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount
from LoanMethods
order by LoanMethodId
```

- Reads: `LoanMethodId`, `LoanMethodDesc`, `LoanAmount`, `DueSupportAmount`, `LoanAmountGiven`, `IntDeduction`, `IntAddition`, `TotAmountCollectable`, `CollectionBreak`, `CollectionBreakString`, `NoOfIns`, `InsAmount`, `GracePeriod`, `DocChargesMethod`, `DocCharges`, `ReportIdentification`, `CBCount`, `IsVisible`, `ShowId`, `WarningDays`

#### `ASYNCSettings.doInBackground` · line 457
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoanMethods`
```sql
INSERT INTO LoanMethods (LoanMethodId, LoanMethodDesc, LoanAmount, DueSupportAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays)
VALUES (${Double.valueOf(reset.getDouble("LoanMethodId"))}, ${reset.getString("LoanMethodDesc")}, ${Double.valueOf(reset.getDouble("LoanAmount"))}, ${Double.valueOf(reset.getDouble("DueSupportAmount"))}, ${Double.valueOf(reset.getDouble("LoanAmountGiven"))}, ${Double.valueOf(reset.getDouble("IntDeduction"))}, ${Double.valueOf(reset.getDouble("IntAddition"))}, ${Double.valueOf(reset.getDouble("TotAmountCollectable"))}, ${reset.getString("CollectionBreak")}, ${reset.getString("CollectionBreakString")}, ${Double.valueOf(reset.getDouble("NoOfIns"))}, ${Double.valueOf(reset.getDouble("InsAmount"))}, ${Double.valueOf(reset.getDouble("GracePeriod"))}, ${Double.valueOf(reset.getDouble("DocChargesMethod"))}, ${Double.valueOf(reset.getDouble("DocCharges"))}, ${reset.getString("ReportIdentification")}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${Integer.valueOf(reset.getInt("WarningDays"))})
```

#### `ASYNCSettings.doInBackground` · line 461
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblGroups`
```sql
Select GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId
from tblGroups
order by GroupName
```

- Reads: `GroupId`, `GroupName`, `GroupUnder`, `ShowId`, `isShowDetails`, `isVisible`, `ANId`

#### `ASYNCSettings.doInBackground` · line 471
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `tblGroups`
```sql
INSERT INTO tblGroups (GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId)
VALUES (${Double.valueOf(reset.getDouble("GroupId"))}, ${reset.getString("GroupName")}, ${reset.getString("GroupUnder")}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${reset.getString("isShowDetails")}, ${reset.getString("isVisible")}, ${Integer.valueOf(reset.getInt("ANId"))})
```

#### `ASYNCSettings.doInBackground` · line 473
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `LoansAllowed`
```sql
Select LId, LoanIndex from LoansAllowed order by LId
```

- Reads: `LId`, `LoanIndex`

#### `ASYNCSettings.doInBackground` · line 478
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoansAllowed`
```sql
INSERT INTO LoansAllowed (LId, LoanIndex)
VALUES (${Integer.valueOf(reset.getInt("LId"))}, ${Integer.valueOf(reset.getInt("LoanIndex"))})
```

#### `ASYNCSettings.doInBackground` · line 482
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `SoftSettings`
```sql
Select SSId,SSDesc,SSValue,Datevalue,bitvalue,NumericValue from SoftSettings
```

- Reads: `NumericValue`, `SSValue`, `bitValue`, `Bitvalue`, `BitValue`

#### `ASYNCSettings.doInBackground` · line 608
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `venussoftsettings`
```sql
Select  VSSId,VSSName,CharValue,NumericValue,BitValue,DateValue from venussoftsettings
```

#### `ASYNCSettings.doInBackground` · line 690
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblLedgers`
```sql
select LedgerId,LedgerName,FinId from tblLedgers where  ScreenType=8  and designationid=1 and desigstatusid=1
```

- Reads: `LedgerId`, `LedgerName`, `FinId`

#### `ASYNCSettings.doInBackground` · line 698
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `CollectionExecutives`
```sql
INSERT INTO CollectionExecutives (LedgerId, LedgerName, FinId)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Integer.valueOf(reset.getInt("FinId"))})
```

#### `ASYNCSettings.doInBackground` · line 706
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · GETCASHANDBANKLEDGERS
```sql
getCashandBankLedgers
```

- Reads: `LedgerId`, `LedgerName`, `Finid`, `Alloted`, `LedgerType`, `istobeVerified`

#### `ASYNCSettings.doInBackground` · line 717
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `CashBankLedgers`
```sql
INSERT INTO CashBankLedgers (LedgerId, LedgerName, Finid, Alloted, LedgerType, istobeVerified)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Integer.valueOf(reset.getInt("Finid"))}, ${Boolean.valueOf(reset.getBoolean("Alloted"))}, ${Integer.valueOf(reset.getInt("LedgerType"))}, ${Boolean.valueOf(reset.getBoolean("istobeVerified"))})
```

#### `ASYNCSettings.doInBackground` · line 727
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblSTLoans`
```sql
Select TransId, TransDate, STLoanId, FWSTLoanId, FWUDSTLoanId, PartyLedgerId, STLoanAmount, STLIntRateinDM, STLIntRateinMM, STLPriIntRateinDM, STLSecIntRateinDM, STLTriIntRateinDM, RecPartnerId, RecPartyId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, BankRefId, BankRefNo, SourceLedgerId,DCTargetLedgerId
from tblSTLoans
order by finid
```

- Reads: `TransId`, `TransDate`, `STLoanId`, `FWSTLoanId`, `FWUDSTLoanId`, `PartyLedgerId`, `STLoanAmount`, `STLIntRateinDM`, `STLIntRateinMM`, `STLPriIntRateinDM`, `STLSecIntRateinDM`, `STLTriIntRateinDM`, `RecPartnerId`, `RecPartyId`, `FinId`, `DocCharges`, `Remarks`, `IntCalcStartOn`, `IntCalcStartOnSYSSET`, `CurStat`, `BankRefId`, `BankRefNo`, `SourceLedgerId`, `DCTargetLedgerId`

#### `ASYNCSettings.doInBackground` · line 754
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblSTLoans`
```sql
INSERT INTO tblSTLoans (TransId, TransDate, STLoanId, FWSTLoanId, FWUDSTLoanId, PartyLedgerId, STLoanAmount, STLIntRateinDM, STLIntRateinMM, STLPriIntRateinDM, STLSecIntRateinDM, STLTriIntRateinDM, RecPartnerId, RecPartyId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, BankRefId, BankRefNo, SourceLedgerId, DCTargetLedgerId)
VALUES (${Double.valueOf(reset.getDouble("TransId"))}, ${reset.getString("TransDate")}, ${Double.valueOf(reset.getDouble("STLoanId"))}, ${Double.valueOf(reset.getDouble("FWSTLoanId"))}, ${Double.valueOf(reset.getDouble("FWUDSTLoanId"))}, ${Double.valueOf(reset.getDouble("PartyLedgerId"))}, ${Double.valueOf(reset.getDouble("STLoanAmount"))}, ${Double.valueOf(reset.getDouble("STLIntRateinDM"))}, ${Double.valueOf(reset.getDouble("STLIntRateinMM"))}, ${Double.valueOf(reset.getDouble("STLPriIntRateinDM"))}, ${Double.valueOf(reset.getDouble("STLSecIntRateinDM"))}, ${Double.valueOf(reset.getDouble("STLTriIntRateinDM"))}, ${Double.valueOf(reset.getDouble("RecPartnerId"))}, ${Double.valueOf(reset.getDouble("RecPartyId"))}, ${Double.valueOf(reset.getDouble("FinId"))}, ${Double.valueOf(reset.getDouble("DocCharges"))}, ${reset.getString("Remarks")}, ${reset.getString("IntCalcStartOn")}, ${reset.getString("IntCalcStartOnSYSSET")}, ${Boolean.valueOf(reset.getBoolean("CurStat"))}, ${Integer.valueOf(reset.getInt("BankRefId"))}, ${reset.getString("BankRefNo")}, ${Double.valueOf(reset.getDouble("SourceLedgerId"))}, ${Integer.valueOf(reset.getInt("DCTargetLedgerId"))})
```

#### `ASYNCSettings.doInBackground` · line 756
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `STLoanStatusforAPP`
```sql
Create table IF NOT EXISTS STLoanStatusforAPP(STLoanId Double,Repaid Double,Balance Double,Status String)
```

#### `ASYNCSettings.doInBackground` · line 757
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `STLoanStatusforAPP`
```sql
delete from STLoanStatusforAPP
```

#### `ASYNCSettings.doInBackground` · line 758
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · GETSTLOANSSTATUS
```sql
GetSTLoansStatus
```

- Reads: `STLoanId`, `Repaid`, `Balance`, `Status`

#### `ASYNCSettings.doInBackground` · line 765
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `STLoanStatusforAPP`
```sql
INSERT INTO STLoanStatusforAPP (STLoanId, Repaid, Balance, Status)
VALUES (${Double.valueOf(reset.getDouble("STLoanId"))}, ${Double.valueOf(reset.getDouble("Repaid"))}, ${Double.valueOf(reset.getDouble("Balance"))}, ${reset.getString("Status")})
```

#### `ASYNCSettings.doInBackground` · line 768
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblOFLoans`
```sql
Select TransId, TransDate, OFLoanId, FWOFLoanId, FWUDOFLoanId, PartyLedgerId, OFLoanAmount, OFLIntRateinDM, OFLIntRateinMM, OFLIntRateinYM, OFLPriIntRateinDM, OFLSecRateinDM,   RecPartnerId, FinId, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, Remarks, BankRefId, BankRefNo, DocCharges, SourceLedgerId
from tblOFLoans
order by finid
```

- Reads: `TransId`, `TransDate`, `OFLoanId`, `FWOFLoanId`, `FWUDOFLoanId`, `PartyLedgerId`, `OFLoanAmount`, `OFLIntRateinDM`, `OFLIntRateinMM`, `RecPartnerId`, `FinId`, `DocCharges`, `Remarks`, `IntCalcStartOn`, `IntCalcStartOnSYSSET`, `CurStat`, `BankRefId`, `BankRefNo`, `SourceLedgerId`

#### `ASYNCSettings.doInBackground` · line 790
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblOFLoans`
```sql
INSERT INTO tblOFLoans (TransId, TransDate, OFLoanId, FWOFLoanId, FWUDOFLoanId, PartyLedgerId, OFLoanAmount, OFLIntRateinDM, OFLIntRateinMM, RecPartnerId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, BankRefId, BankRefNo, SourceLedgerId)
VALUES (${Double.valueOf(reset.getDouble("TransId"))}, ${reset.getString("TransDate")}, ${Double.valueOf(reset.getDouble("OFLoanId"))}, ${Double.valueOf(reset.getDouble("FWOFLoanId"))}, ${Double.valueOf(reset.getDouble("FWUDOFLoanId"))}, ${Double.valueOf(reset.getDouble("PartyLedgerId"))}, ${Double.valueOf(reset.getDouble("OFLoanAmount"))}, ${Double.valueOf(reset.getDouble("OFLIntRateinDM"))}, ${Double.valueOf(reset.getDouble("OFLIntRateinMM"))}, ${Double.valueOf(reset.getDouble("RecPartnerId"))}, ${Double.valueOf(reset.getDouble("FinId"))}, ${Double.valueOf(reset.getDouble("DocCharges"))}, ${reset.getString("Remarks")}, ${reset.getString("IntCalcStartOn")}, ${reset.getString("IntCalcStartOnSYSSET")}, ${Boolean.valueOf(reset.getBoolean("CurStat"))}, ${Integer.valueOf(reset.getInt("BankRefId"))}, ${reset.getString("BankRefNo")}, ${Double.valueOf(reset.getDouble("SourceLedgerId"))})
```

#### `ASYNCSettings.doInBackground` · line 792
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, lat, lng, Age, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName,  desigstatusid
from tblLedgers
order by LedgerId
```

- Reads: `LedgerId`, `LedgerName`, `GroupId`, `OpeningBalance`, `OpBalType`, `FinId`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `MasterPartyLedgerId`, `ScreenType`, `PartyGroupId`, `IsDefault`, `IsReserved`, `ListSNo`, `PanNo`, `isActive`, `DesignationId`, `StatusId`, `AndroidDeviceId`, `AadhaarNo`, `Imagefilename`, `desigstatusid`, `lat`, `lng`, `MartialStatus`, `Qualification`, `Profession`, `MonthlyIncome`, `GuardianMonthlyIncome`, `GuardianMobileNo1`, `GuardianMobileNo2`, `GuardianProfession`, `LandMark`, `SpouseName`, `FamilyName`

#### `ASYNCSettings.doInBackground` · line 841
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblLedgers`
```sql
INSERT INTO tblLedgers (LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, PartyGroupId, IsDefault, IsReserved, ListSNo, PanNo, isActive, DesignationId, StatusId, AndroidDeviceId, AadhaarNo, Imagefilename, desigstatusid, lat, lng, ${HttpHeaders.AGE}, MartialStatus, Qualification, Profession, MonthlyIncome, GuardianMonthlyIncome, GuardianMobileNo1, GuardianMobileNo2, GuardianProfession, LandMark, SpouseName, FamilyName)
VALUES (${Double.valueOf(reset.getDouble("LedgerId"))}, ${reset.getString("LedgerName")}, ${Double.valueOf(reset.getDouble("GroupId"))}, ${Double.valueOf(reset.getDouble("OpeningBalance"))}, ${reset.getString("OpBalType")}, ${Double.valueOf(reset.getDouble("FinId"))}, ${reset.getString("Title")}, ${reset.getString("Tag")}, ${reset.getString("GType")}, ${reset.getString("GuardianName")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("PinCode")}, ${reset.getString("MobileNo1")}, ${reset.getString("MobileNo2")}, ${reset.getString("EMail")}, ${Double.valueOf(reset.getDouble("MasterPartyLedgerId"))}, ${Double.valueOf(reset.getDouble("ScreenType"))}, ${Double.valueOf(reset.getDouble("PartyGroupId"))}, ${Double.valueOf(reset.getDouble("IsDefault"))}, ${Double.valueOf(reset.getDouble("IsReserved"))}, ${Double.valueOf(reset.getDouble("ListSNo"))}, ${reset.getString("PanNo")}, ${Boolean.valueOf(reset.getBoolean("isActive"))}, ${Integer.valueOf(reset.getInt("DesignationId"))}, ${Integer.valueOf(reset.getInt("StatusId"))}, ${reset.getString("AndroidDeviceId")}, ${reset.getString("AadhaarNo")}, ${reset.getString("Imagefilename")}, ${Integer.valueOf(reset.getInt("desigstatusid"))}, ${reset.getString("lat")}, ${reset.getString("lng")}, ${Integer.valueOf(reset.getInt(HttpHeaders.AGE))}, ${Boolean.valueOf(reset.getBoolean("MartialStatus"))}, ${reset.getString("Qualification")}, ${reset.getString("Profession")}, ${Integer.valueOf(reset.getInt("MonthlyIncome"))}, ${Integer.valueOf(reset.getInt("GuardianMonthlyIncome"))}, ${reset.getString("GuardianMobileNo1")}, ${reset.getString("GuardianMobileNo2")}, ${reset.getString("GuardianProfession")}, ${reset.getString("LandMark")}, ${reset.getString("SpouseName")}, ${reset.getString("FamilyName")})
```

#### `ASYNCSettings.doInBackground` · line 846
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `OFLoanStatusforAPP`
```sql
Create table IF NOT EXISTS OFLoanStatusforAPP(OFLoanId Double,Repaid Double,Balance Double,Status String)
```

#### `ASYNCSettings.doInBackground` · line 847
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `OFLoanStatusforAPP`
```sql
delete from OFLoanStatusforAPP
```

#### `ASYNCSettings.doInBackground` · line 848
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · GETOFLOANSSTATUS
```sql
GetOFLoansStatus
```

- Reads: `OFLoanId`, `Repaid`, `Balance`, `Status`

#### `ASYNCSettings.doInBackground` · line 855
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `OFLoanStatusforAPP`
```sql
INSERT INTO OFLoanStatusforAPP (OFLoanId, Repaid, Balance, Status)
VALUES (${Double.valueOf(reset.getDouble("OFLoanId"))}, ${Double.valueOf(reset.getDouble("Repaid"))}, ${Double.valueOf(reset.getDouble("Balance"))}, ${reset.getString("Status")})
```

#### `ASYNCSettings.doInBackground` · line 857
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblFinwisePartners`
```sql
Select UID, PartnerLedgerId, FinId, CapitalReceiptTransId, ListSNo, isActive
from tblFinwisePartners
order by UID
```

- Reads: `UID`, `PartnerLedgerId`, `FinId`, `CapitalReceiptTransId`, `ListSNo`, `isActive`

#### `ASYNCSettings.doInBackground` · line 866
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `tblFinwisePartners`
```sql
INSERT INTO tblFinwisePartners (UID, PartnerLedgerId, FinId, CapitalReceiptTransId, ListSNo, isActive)
VALUES (${Double.valueOf(reset.getDouble("UID"))}, ${Double.valueOf(reset.getDouble("PartnerLedgerId"))}, ${Double.valueOf(reset.getDouble("FinId"))}, ${Double.valueOf(reset.getDouble("CapitalReceiptTransId"))}, ${Double.valueOf(reset.getDouble("ListSNo"))}, ${Boolean.valueOf(reset.getBoolean("isActive"))})
```

#### `ASYNCSettings.doInBackground` · line 871
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceDetails`
```sql
delete from FinanceDetails
```

#### `ASYNCSettings.doInBackground` · line 873
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SelectedBranchFinanceDetails`
```sql
delete from SelectedBranchFinanceDetails
```

#### `ASYNCSettings.doInBackground` · line 874
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · GETALLOWEDFINANCES
```sql
GetAllowedFinances @IMEI='${globalvars.device_id}'
```

- Reads: `FinId`, `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `CashLedgerId`, `YearFrom`, `BooksFrom`, `ShortName`, `PhoneNo`, `MobileNo`, `AdminMobileNos`, `LedgerName`, `LId`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `CollectionBreakString`, `EmailIdentifier`, `SMSIdentifier`

#### `ASYNCSettings.doInBackground` · line 900
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `FinanceDetails`
```sql
INSERT INTO FinanceDetails (FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, CashLedgerId, YearFrom, BooksFrom, ShortName, PhoneNo, MobileNo, AdminMobileNos, LedgerName, LId, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString, EmailIdentifier, SMSIdentifier)
VALUES (${Integer.valueOf(reset.getInt("FinId"))}, ${reset.getString("FinName")}, ${reset.getString("FinMailingName")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("PINcode")}, ${reset.getString("RegistrationNo")}, ${Integer.valueOf(reset.getInt("CashLedgerId"))}, ${reset.getString("YearFrom")}, ${reset.getString("BooksFrom")}, ${reset.getString("ShortName")}, ${reset.getString("PhoneNo")}, ${reset.getString("MobileNo")}, ${reset.getString("AdminMobileNos")}, ${reset.getString("LedgerName")}, ${Integer.valueOf(reset.getInt("LId"))}, ${Integer.valueOf(reset.getInt("DefaultLoanMethodId"))}, ${Integer.valueOf(reset.getInt("FinanceCashBookLedgerId"))}, ${reset.getString("CollectionBreakString")}, ${reset.getString("EmailIdentifier")}, ${reset.getString("SMSIdentifier")})
```

#### `ASYNCSettings.onPostExecute` · line 929
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinId, FinName, AddLine1, AddLine2, City, PinCode, RegistrationNo, YearFrom, BooksFrom, ShortName, MobileNo, PhoneNo, EmailIdentifier, FinMailingName, CashLedgerId, AdminMobileNos, SMSIdentifier, LedgerName, LId, BankLedgerid, DefaultLoanMethodId, FinanceCashBookLedgerId, CollectionBreakString
from FinanceDetails
```

- Reads: `FinId`, `FinName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `BooksFrom`, `ShortName`, `EmailIdentifier`, `FinMailingName`, `CashLedgerId`, `AdminMobileNos`, `SMSIdentifier`, `LedgerName`, `LId`, `BankLedgerid`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `CollectionBreakString`

#### `ASYNCSettings.onPostExecute` · line 987
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `ASYNCSettings.onPostExecute` · line 988
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `ASYNCSettings.onPostExecute` · line 989
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `ASYNCSettings.onPostExecute` · line 990
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `ASYNCSettings.onPostExecute` · line 991
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

#### `ASYNCSettings.onPostExecute` · line 992
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

#### `ASYNCSettings.onPostExecute` · line 996
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblSalesDetailsforAndroid`
```sql
Select UserLevelId, mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState,mnuname,mnudisplayname
from tblSalesDetailsforAndroid
order by UserLevelId
```

- Reads: `UserLevelId`, `mnuId`, `ShowState`, `ViewState`, `UpdateState`, `AddState`, `DeleteState`, `mnuname`, `mnudisplayname`

#### `AsyncDownloaddata.doInBackground` · line 1066
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `DownloadControling`, `FinanceAccessAllowed`, `softsettings`
```sql
select top 1 x.IMEI,convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(), '+05:30'),0 ) as DownloadTime ,case when (select  bitvalue
from softsettings
where SSId=32)='True' and isAllowed is not null and isAllowed='True' then 'True' when (select bitvalue
from softsettings
where SSId=32)='False' then 'True' else 'False' end  as 'IsAllowed'
from FinanceAccessAllowed x
left
join DownloadControling z on x.FinId=z.FinId and TransDate='${CollectionDate}',MobileDevlicesAllowed y
where x.IMEI=y.IMEI and x.FinId=${globalvars.finid} and y.isActive =1 and x.IMEI='${globalvars.device_id}'
```

- Reads: `DownloadTime`

#### `AsyncDownloaddata.doInBackground` · line 1076
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
select userroleid from MobileDevlicesAllowed where IMEI='${globalvars.device_id}'
```

#### `AsyncDownloaddata.doInBackground` · line 1091
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `financemaster`
```sql
select CollectionBreakString
From financemaster a, LoanMethods b
where a.defaultloanmethodid=b.LoanMethodId and a.finid=${globalvars.finid}
```

- Reads: `CollectionBreakString`, `SubAreaNo`, `SubAreaName`, `IsVisible`, `ShowId`, `FinId`, `WeekDayId`

#### `AsyncDownloaddata.doInBackground` · line 1108
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId
from subareadetails
where FinId=${globalvars.finid}
order by SubAreaNo
```

#### `AsyncDownloaddata.doInBackground` · line 1111
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId
from subareadetails
where FinId=${globalvars.finid} and WeekDayId=${globalvars.currentweekdayid}
order by SubAreaNo
```

#### `AsyncDownloaddata.doInBackground` · line 1125
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `subareadetails`
```sql
INSERT INTO subareadetails (SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId)
VALUES (${Double.valueOf(reset.getDouble("SubAreaNo"))}, ${reset.getString("SubAreaName")}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Integer.valueOf(reset.getInt("ShowId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt("WeekDayId"))})
```

#### `AsyncDownloaddata.doInBackground` · line 1128
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · APP_MCLOANCOLLECTIONDATAWITHLOANCLOSOUREMONITERING_WITHWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport @ColDate ='${CollectionDate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 1130
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · APP_MCLOANCOLLECTIONDATAWITHLOANCLOSOUREMONITERING_WITHOUTWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport @ColDate ='${CollectionDate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 1132
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · APP_MCLOANCOLLECTIONDATAAUTOLOANCLOSOURE_WITHWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport @ColDate ='${CollectionDate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 1134
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · APP_MCLOANCOLLECTIONDATAAUTOLOANCLOSOURE_WITHOUTWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport @ColDate ='${CollectionDate}' ,@FinId=${globalvars.finid}
```

- Reads: `MCLoanId`, `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `PartyName`, `SubAreaNo`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`, `PrevCollected`, `interestamount`

#### `AsyncDownloaddata.doInBackground` · line 1172
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `MobileAppCollData`
```sql
INSERT INTO MobileAppCollData (MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, Collected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, IsMarked, PrevCollected, interestamount, PenaltyCollected, SavingsCollected, IsVisited)
VALUES (${Integer.valueOf(reset.getInt("MCLoanId"))}, ${Integer.valueOf(reset.getInt("FWUDMCLoanId"))}, ${reset.getString("Remarks")}, ${Integer.valueOf(reset.getInt("PartyLedgerId"))}, ${reset.getString("MCLoanDate")}, ${reset.getString("ClosingDate")}, ${Integer.valueOf(reset.getInt("CWLedgerId"))}, ${Integer.valueOf(reset.getInt("UDLedgerId"))}, ${reset.getString("PartyName")}, ${Integer.valueOf(reset.getInt("SubAreaNo"))}, ${Integer.valueOf(FirstSubAreaNo2)}, ${Integer.valueOf(reset.getInt("LAmount"))}, ${Integer.valueOf(reset.getInt("DuesCount"))}, ${Integer.valueOf(reset.getInt("InsAmount"))}, ${Integer.valueOf(reset.getInt("MCLoanAmountCollectable"))}, ${(Integer) 0}, ${Integer.valueOf(reset.getInt("PaidDues"))}, ${Integer.valueOf(reset.getInt("PassedDues"))}, ${reset.getString("LoanMethodDesc")}, ${reset.getString("CollectionBreakString")}, ${Integer.valueOf(reset.getInt("NoOfIns"))}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Boolean.valueOf(reset.getBoolean("istobeWarned"))}, ${Boolean.valueOf(reset.getBoolean("IsNIP"))}, ${reset.getString("LoanStatus")}, ${(Boolean) false}, ${Integer.valueOf(reset.getInt("PrevCollected"))}, ${Integer.valueOf(reset.getInt("interestamount"))}, ${(Integer) 0}, ${(Integer) 0}, ${(Boolean) false})
```

#### `AsyncDownloaddata.doInBackground` · line 1175
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `FinanceMaster`
```sql
select dbo.showLedgerBalance(CashLedgerId,FinId) as  openingbalance
from FinanceMaster
where  finid=${globalvars.finid} and IsVisible='True'
order by ShowId
```

#### `AsyncDownloaddata.doInBackground` · line 1190
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `subareadetails`, `tblLedgers`
```sql
select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,isnull(MobileNo1,'') as MobileNo1,isnull(MobileNo2,'') as MobileNo2,SpouseName,FamilyName,lat,lng,ImageFileName,IdProofFileName,AddLine1,IdProofFileNameII
from tblLedgers
where ScreenType=3 and  SubAreaNo in (select SubAreaNo
from subareadetails
Where FinId=${globalvars.finid})
order by SubAreaNo, UDLedgerId
```

- Reads: `LedgerId`, `CWLedgerId`, `LedgerName`, `Tag`, `SubAreaNo`, `UDLedgerId`, `MobileNo1`, `MobileNo2`, `SpouseName`, `FamilyName`, `lat`, `lng`, `ImageFileName`, `IdProofFileName`, `AddLine1`, `IdProofFileNameII`

#### `AsyncDownloaddata.doInBackground` · line 1209
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Parties`
```sql
INSERT INTO Parties (LedgerId, CWLedgerId, LedgerName, Tag, SubAreaNo, UDLedgerId, MobileNo1, MobileNo2, SpouseName, FamilyName, lat, lng, ImageFileName, IdProofFileName, AddLine1, IdProofFileNameII)
VALUES (${Double.valueOf(reset.getDouble("LedgerId"))}, ${Double.valueOf(reset.getDouble("CWLedgerId"))}, ${reset.getString("LedgerName")}, ${reset.getString("Tag")}, ${reset.getString("SubAreaNo")}, ${reset.getString("UDLedgerId")}, ${reset.getString("MobileNo1")}, ${reset.getString("MobileNo2")}, ${reset.getString("SpouseName")}, ${reset.getString("FamilyName")}, ${reset.getString("lat")}, ${reset.getString("lng")}, ${reset.getString("ImageFileName")}, ${reset.getString("IdProofFileName")}, ${reset.getString("AddLine1")}, ${reset.getString("IdProofFileNameII")})
```

#### `AsyncDownloaddata.doInBackground` · line 1212
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `PattiExpLedgers`, `tblLedgers`
```sql
select LedgerId,LedgerName
from tblLedgers
where LedgerId in (select LedgerId
from PattiExpLedgers)
order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

#### `AsyncDownloaddata.doInBackground` · line 1218
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ExpHeaders`
```sql
INSERT INTO ExpHeaders (LedgerId, LedgerName, TTId)
VALUES (${Double.valueOf(reset.getDouble("LedgerId"))}, ${reset.getString("LedgerName")}, ${(Integer) 7})
```

#### `AsyncDownloaddata.doInBackground` · line 1221
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblLedgers`
```sql
select LedgerId,LedgerName
from tblLedgers
where GroupId in (3,4,2,16,19) and isreserved=0 and LedgerId>200
order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

#### `AsyncDownloaddata.doInBackground` · line 1227
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ExpHeaders`
```sql
INSERT INTO ExpHeaders (LedgerId, LedgerName, TTId)
VALUES (${Double.valueOf(reset.getDouble("LedgerId"))}, ${reset.getString("LedgerName")}, ${(Integer) 8})
```

#### `AsyncDownloaddata.doInBackground` · line 1231
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · PREPARELAST5COLLECTIONSFORPREV5DATES
```sql
PrepareLast5CollectionsforPrev5dates @FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 1233
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · PREPARELAST5COLLECTIONS
```sql
PrepareLast5Collections @FinId=${globalvars.finid}
```

- Reads: `RowNo`, `MCLoanID`, `Paid`, `CollDate`

#### `AsyncDownloaddata.doInBackground` · line 1242
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `PrevCollections`
```sql
INSERT INTO PrevCollections (RowNo, MCLoanID, Paid, CollDate)
VALUES (${Integer.valueOf(reset.getInt("RowNo"))}, ${Integer.valueOf(reset.getInt("MCLoanID"))}, ${Double.valueOf(reset.getDouble("Paid"))}, ${String.valueOf(reset.getDate("CollDate"))})
```

#### `AsyncDownloaddata.onPreExecute` · line 1376
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpHeaders`
```sql
CREATE TABLE IF NOT EXISTS ExpHeaders(LedgerId Double,LedgerName nvarchar,TTId int)
```

#### `AsyncDownloaddata.onPreExecute` · line 1378
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `AsyncDownloaddata.onPreExecute` · line 1379
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `AsyncDownloaddata.onPreExecute` · line 1380
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `AsyncDownloaddata.onPreExecute` · line 1381
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `AsyncDownloaddata.onPreExecute` · line 1382
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpensesEntries`
```sql
Create table IF NOT EXISTS ExpensesEntries(ID INTEGER PRIMARY KEY   AUTOINCREMENT,FinId Double,LedgerId Double,Amount Double,ServerGenTransId Double,TTId int,SourceLedgerId int,Narration varchar )
```

#### `AsyncDownloaddata.onPreExecute` · line 1383
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

#### `AsyncDownloaddata.onPreExecute` · line 1384
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `Parties`
```sql
delete from Parties
```

#### `AsyncDownloaddata.onPreExecute` · line 1385
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpHeaders`
```sql
delete from ExpHeaders
```

#### `AsyncDownloaddata.onPreExecute` · line 1386
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `PrevCollections`
```sql
Create table IF NOT EXISTS PrevCollections (RowNo int,MCLoanId Double, CollDate varchar,Paid Double)
```

#### `AsyncDownloaddata.onPreExecute` · line 1387
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `PrevCollections`
```sql
delete from PrevCollections
```

#### `AsyncDownloaddata.onPreExecute` · line 1389
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

## settings_act

Screen: **Settings** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#settings--settingsact) · 40 statements

#### `ASYNCSaveSettings.doInBackground` · line 667, 675, 679, 683, 687, 691, 695, 703, 711, 715, 719, 739, 743, 755, 763, 771, 775, 779, 783, 787, 791, 795, 799, 803, 807, 811, 815, 819
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `SoftSettings`
```sql
Update SoftSettings set bitvalue =? where SSId=?
```

- Params: 2 → `32` (setInt) · 1 → `softsettingsvalue.enablemoniterondownloadandupload_1` (setBoolean)

#### `ASYNCSaveSettings.doInBackground` · line 671, 707, 759, 767
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `SoftSettings`
```sql
Update SoftSettings set NumericValue =? where SSId=?
```

- Params: 2 → `58` (setInt) · 1 → `softsettingsvalue.noofbackdaysallowedfordownloaddata_2` (setInt)

#### `ASYNCSaveSettings.doInBackground` · line 699, 723, 727, 731, 735, 747, 751
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `SoftSettings`
```sql
Update SoftSettings set Numericvalue =? where SSId=?
```

- Params: 2 → `47` (setInt) · 1 → `softsettingsvalue.fivedayscollectiondetailsmethod_8` (setInt)

#### `ASYNCSettings.doInBackground` · line 419
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `SoftSettings`
```sql
Select SSId,SSDesc,SSValue,Datevalue,bitvalue,NumericValue from SoftSettings
```

- Reads: `SSId`, `bitValue`, `NumericValue`, `SSValue`

## specialaccesscontrolact

Screen: **Special Access Settings** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#special-access-settings--specialaccesscontrolact) · 41 statements

#### `ASYNCSaveSettings.doInBackground` · line 306, 318, 330, 342, 354, 366, 378, 390, 402, 414, 426, 438, 450, 462, 474, 486, 498, 510, 522, 534
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `SpecialAccessAllowed`
```sql
Update SpecialAccessAllowed set bitvalue =? where SARId=? and UserLevelId=?
```

- Params: 1 → `spaOBJ.isAllowtoChangeMaximumAmount_1()` (setBoolean) · 2 → `1` (setInt) · 3 → `curmaobj.getUserroleid()` (setInt)

#### `ASYNCSaveSettings.doInBackground` · line 312, 324, 336, 348, 360, 372, 384, 396, 408, 420, 432, 444, 456, 468, 480, 492, 504
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `SpecialAccessAllowed`
```sql
Insert into  SpecialAccessAllowed (bitvalue,SARId, UserLevelId)values(?,?,?)
```

- Params: 1 → `spaOBJ.isAllowtoChangeMaximumAmount_1()` (setBoolean) · 2 → `1` (setInt) · 3 → `curmaobj.getUserroleid()` (setInt)

#### `ASYNCSaveSettings.doInBackground` · line 516, 528, 540
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `SpecialAccessAllowed`
```sql
Insert into SpecialAccessAllowed (bitvalue,SARId, UserLevelId)values(?,?,?)
```

- Params: 1 → `spaOBJ.getAllowtoCreateNewLoansagainstondayclosedLoans_18()` (setBoolean) · 2 → `18` (setInt) · 3 → `curmaobj.getUserroleid()` (setInt)

#### `ASYNCSettings.doInBackground` · line 197
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `SpecialAccessAllowed`, `SpecialAccessRules`
```sql
select a.SARId,SAName,bitvalue
from SpecialAccessRules a
left
join SpecialAccessAllowed b on a.SARId=b.SARId and b.UserLevelId=${curmaobj.getUserroleid()}
where a.isAllowed=1
ORDER BY SARId
```

- Reads: `SARId`, `bitvalue`

## syncresources

Screen: **Sync Resources** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#sync-resources--syncresources) · 75 statements

#### `asyncdatabasecontents.doInBackground` · line 110
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CollectionExecutives`
```sql
Create table IF NOT EXISTS CollectionExecutives (LedgerId Double,LedgerName VarChar,FinId int)
```

#### `asyncdatabasecontents.doInBackground` · line 111
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CollectionExecutives`
```sql
delete from CollectionExecutives
```

#### `asyncdatabasecontents.doInBackground` · line 112
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoanMethods`
```sql
Create table IF NOT EXISTS LoanMethods(LoanMethodId Double,LoanMethodDesc String,LoanAmount Double,LoanAmountGiven Double,IntDeduction Double,IntAddition Double,TotAmountCollectable Double,CollectionBreak String,CollectionBreakString String,NoOfIns Double,InsAmount Double,GracePeriod Double,DocChargesMethod Double,DocCharges Double,ReportIdentification String,CBCount int,IsVisible Boolean,ShowId Double,WarningDays int,DueSupportAmount int)
```

#### `asyncdatabasecontents.doInBackground` · line 113
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoanMethods`
```sql
delete from LoanMethods
```

#### `asyncdatabasecontents.doInBackground` · line 114
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblGroups`
```sql
Create table IF NOT EXISTS tblGroups(GroupId Double,GroupName String,GroupUnder String,ShowId Decimal,isShowDetails String,isVisible String,ANId int)
```

#### `asyncdatabasecontents.doInBackground` · line 115
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblGroups`
```sql
delete from tblGroups
```

#### `asyncdatabasecontents.doInBackground` · line 116
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoansAllowed`
```sql
Create table IF NOT EXISTS LoansAllowed(LId Int,LoanIndex Int)
```

#### `asyncdatabasecontents.doInBackground` · line 117
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoansAllowed`
```sql
delete from LoansAllowed
```

#### `asyncdatabasecontents.doInBackground` · line 118
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblLedgers`
```sql
Create table IF NOT EXISTS tblLedgers(LedgerId Double,LedgerName String,GroupId Double,OpeningBalance Double,OpBalType String,FinId Double,Title String,Tag String,GType String,GuardianName String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,MasterPartyLedgerId Double,ScreenType Double,PartyGroupId Double,IsDefault Double,IsReserved Double,ListSNo Double,PanNo String,isActive Boolean,DesignationId int,StatusId int,AndroidDeviceId String,AadhaarNo String,Imagefilename String,lat String,lng String,Age int,MartialStatus Boolean,Qualification String,Profession String,MonthlyIncome int,GuardianMonthlyIncome int,GuardianMobileNo1 String,GuardianMobileNo2 String,GuardianProfession String,LandMark String,SpouseName String,FamilyName String,desigstatusid int)
```

#### `asyncdatabasecontents.doInBackground` · line 119
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers
```

#### `asyncdatabasecontents.doInBackground` · line 120
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tblFinwisePartners`
```sql
Create table IF NOT EXISTS tblFinwisePartners(UID Double,PartnerLedgerId Double,FinId Double,CapitalReceiptTransId Double,ListSNo Double, isActive Boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 121
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblFinwisePartners`
```sql
delete from tblFinwisePartners
```

#### `asyncdatabasecontents.doInBackground` · line 125
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `RelatedDocCollection`
```sql
delete from RelatedDocCollection
```

#### `asyncdatabasecontents.doInBackground` · line 126
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblSalesDetailsforAndroid`
```sql
delete from tblSalesDetailsforAndroid
```

#### `asyncdatabasecontents.doInBackground` · line 127
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle String,SMSDesc String)
```

#### `asyncdatabasecontents.doInBackground` · line 128
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SMSTemplates`
```sql
delete from SMSTemplates
```

#### `asyncdatabasecontents.doInBackground` · line 129
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `userroles`
```sql
Create table IF NOT EXISTS userroles(URId Int,URName String,isFullAccessAllowed Boolean,isvisible Boolean,DisplayIndex Int)
```

#### `asyncdatabasecontents.doInBackground` · line 130
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `userroles`
```sql
delete from userroles
```

#### `asyncdatabasecontents.doInBackground` · line 131
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `CashBankLedgers`
```sql
Create table IF NOT EXISTS CashBankLedgers(LedgerId int,GroupId int,LedgerName String,Finid int,Alloted boolean,LedgerType Int,istobeVerified boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 132
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `CashBankLedgers`
```sql
delete from CashBankLedgers
```

#### `asyncdatabasecontents.doInBackground` · line 133
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SpecialAccessRules`
```sql
Create table IF NOT EXISTS SpecialAccessRules(SARId Int,SAName String,ValueType Int,isAllowed Boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 134
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SpecialAccessRules`
```sql
delete from SpecialAccessRules
```

#### `asyncdatabasecontents.doInBackground` · line 135
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SpecialAccessAllowed`
```sql
Create table IF NOT EXISTS SpecialAccessAllowed(SAAId Int,UserLevelId Int,SARId Int,bitvalue Boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 136
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SpecialAccessAllowed`
```sql
delete from SpecialAccessAllowed
```

#### `asyncdatabasecontents.doInBackground` · line 149
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `FinanceAccessAllowed`
```sql
select top 1 x.IMEI,userroleid,c.isFullAccessAllowed ,d.LedgerName
from FinanceAccessAllowed x , MobileDevlicesAllowed y,UserRoles c, tblLedgers d
where d.LedgerId=y.LId and y.userroleid=c.URId and x.IMEI=y.IMEI and   y.isActive =1 and x.IMEI='${globalvars.device_id}'
```

- Reads: `LedgerName`, `userroleid`, `isFullAccessAllowed`

#### `asyncdatabasecontents.doInBackground` · line 176
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SMSTemplates`
```sql
Select STId, SMSTitle, SMSDesc from SMSTemplates order by STId
```

- Reads: `STId`, `SMSTitle`, `SMSDesc`

#### `asyncdatabasecontents.doInBackground` · line 182
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SMSTemplates`
```sql
INSERT INTO SMSTemplates (STId, SMSTitle, SMSDesc)
VALUES (${Integer.valueOf(reset.getInt("STId"))}, ${reset.getString("SMSTitle")}, ${reset.getString("SMSDesc")})
```

#### `asyncdatabasecontents.doInBackground` · line 186
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `AndroidMenus`, `tblSalesDetailsforAndroid`
```sql
Select  mnuname,mnudisplayname,  UserLevelId, a.mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState,mnustyle
from AndroidMenus a
left
join tblSalesDetailsforAndroid b on a.mnuid=b.mnuid and b.UserLevelId=${globalvars.app_userroleid}
where a.isAllowed =1 and(isonlyformasterdb='FALSE' OR  isonlyformasterdb='${globalvars.ismasterdb}')
order by mnuId
```

- Reads: `UserLevelId`, `mnuId`, `mnustyle`, `ShowState`, `ViewState`, `UpdateState`, `AddState`, `DeleteState`, `mnuname`, `mnudisplayname`

#### `asyncdatabasecontents.doInBackground` · line 199
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblSalesDetailsforAndroid`
```sql
INSERT INTO tblSalesDetailsforAndroid (UserLevelId, mnuId, mnustyle, ShowState, ViewState, UpdateState, AddState, DeleteState, mnuname, mnudisplayname)
VALUES (${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("mnuId"))}, ${Integer.valueOf(reset.getInt("mnustyle"))}, ${Boolean.valueOf(reset.getBoolean("ShowState"))}, ${Boolean.valueOf(reset.getBoolean("ViewState"))}, ${Boolean.valueOf(reset.getBoolean("UpdateState"))}, ${Boolean.valueOf(reset.getBoolean("AddState"))}, ${Boolean.valueOf(reset.getBoolean("DeleteState"))}, ${reset.getString("mnuname")}, ${reset.getString("mnudisplayname")})
```

#### `asyncdatabasecontents.doInBackground` · line 203
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `userroles`
```sql
Select URId, URName, isvisible, DisplayIndex,isFullAccessAllowed from userroles order by URId
```

- Reads: `URName`, `isvisible`, `DisplayIndex`, `isFullAccessAllowed`

#### `asyncdatabasecontents.doInBackground` · line 214
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `userroles`
```sql
INSERT INTO userroles (URId, URName, isvisible, DisplayIndex, isFullAccessAllowed)
VALUES (${Integer.valueOf(resultSet.getInt("URId"))}, ${reset.getString("URName")}, ${Boolean.valueOf(reset.getBoolean("isvisible"))}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Integer.valueOf(reset.getInt("isFullAccessAllowed"))})
```

#### `asyncdatabasecontents.doInBackground` · line 226
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · EXEC · procedure: `getcashandbankledgers`
```sql
exec getCashandBankLedgers @URId=${globalvars.app_userroleid}
```

- Reads: `LedgerId`, `LedgerName`, `Finid`, `Alloted`, `LedgerType`, `istobeVerified`, `GroupId`

#### `asyncdatabasecontents.doInBackground` · line 236
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `CashBankLedgers`
```sql
INSERT INTO CashBankLedgers (LedgerId, LedgerName, Finid, Alloted, LedgerType, istobeVerified, GroupId)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Integer.valueOf(reset.getInt("Finid"))}, ${Boolean.valueOf(reset.getBoolean("Alloted"))}, ${Integer.valueOf(reset.getInt("LedgerType"))}, ${Boolean.valueOf(reset.getBoolean("istobeVerified"))}, ${Integer.valueOf(reset.getInt("GroupId"))})
```

#### `asyncdatabasecontents.doInBackground` · line 243
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SpecialAccessRules`
```sql
Select SARId, SAName, ValueType, isAllowed from SpecialAccessRules order by SARId
```

- Reads: `SARId`, `SAName`, `ValueType`, `isAllowed`

#### `asyncdatabasecontents.doInBackground` · line 250
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SpecialAccessRules`
```sql
INSERT INTO SpecialAccessRules (SARId, SAName, ValueType, isAllowed)
VALUES (${Integer.valueOf(reset.getInt("SARId"))}, ${reset.getString("SAName")}, ${Integer.valueOf(reset.getInt("ValueType"))}, ${Boolean.valueOf(reset.getBoolean("isAllowed"))})
```

#### `asyncdatabasecontents.doInBackground` · line 252
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue
from SpecialAccessAllowed
where UserLevelId=${globalvars.app_userroleid}
order by SAAId
```

- Reads: `SAAId`, `UserLevelId`, `SARId`, `bitvalue`

#### `asyncdatabasecontents.doInBackground` · line 259
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SpecialAccessAllowed`
```sql
INSERT INTO SpecialAccessAllowed (SAAId, UserLevelId, SARId, bitvalue)
VALUES (${Integer.valueOf(reset.getInt("SAAId"))}, ${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("SARId"))}, ${Boolean.valueOf(reset.getBoolean("bitvalue"))})
```

#### `asyncdatabasecontents.doInBackground` · line 262
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue
from SpecialAccessAllowed
where UserLevelId=${globalvars.app_userroleid}
order by SAAId
```

- Reads: `SARId`, `bitvalue`

#### `asyncdatabasecontents.doInBackground` · line 303
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `LoanMethods`
```sql
DELETE FROM LoanMethods WHERE LoanMethodId > ?
```

- Selection args: `strArr5`

#### `asyncdatabasecontents.doInBackground` · line 304
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `LoanMethods`
```sql
Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount
from LoanMethods
order by LoanMethodId
```

- Reads: `LoanMethodId`, `LoanMethodDesc`, `LoanAmount`, `DueSupportAmount`, `LoanAmountGiven`, `IntDeduction`, `IntAddition`, `TotAmountCollectable`, `CollectionBreak`, `CollectionBreakString`, `NoOfIns`, `InsAmount`, `GracePeriod`, `DocChargesMethod`, `DocCharges`, `ReportIdentification`, `CBCount`, `IsVisible`, `ShowId`, `WarningDays`

#### `asyncdatabasecontents.doInBackground` · line 327
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoanMethods`
```sql
INSERT INTO LoanMethods (LoanMethodId, LoanMethodDesc, LoanAmount, DueSupportAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays)
VALUES (${Double.valueOf(reset.getDouble("LoanMethodId"))}, ${reset.getString("LoanMethodDesc")}, ${Double.valueOf(reset.getDouble("LoanAmount"))}, ${Double.valueOf(reset.getDouble("DueSupportAmount"))}, ${Double.valueOf(reset.getDouble("LoanAmountGiven"))}, ${Double.valueOf(reset.getDouble("IntDeduction"))}, ${Double.valueOf(reset.getDouble("IntAddition"))}, ${Double.valueOf(reset.getDouble("TotAmountCollectable"))}, ${reset.getString("CollectionBreak")}, ${reset.getString("CollectionBreakString")}, ${Double.valueOf(reset.getDouble("NoOfIns"))}, ${Double.valueOf(reset.getDouble("InsAmount"))}, ${Double.valueOf(reset.getDouble("GracePeriod"))}, ${Double.valueOf(reset.getDouble("DocChargesMethod"))}, ${Double.valueOf(reset.getDouble("DocCharges"))}, ${reset.getString("ReportIdentification")}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${Integer.valueOf(reset.getInt("WarningDays"))})
```

#### `asyncdatabasecontents.doInBackground` · line 334
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `tblGroups`
```sql
Select GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId
from tblGroups
order by GroupName
```

- Reads: `GroupId`, `GroupName`, `GroupUnder`, `ShowId`, `isShowDetails`, `isVisible`, `ANId`

#### `asyncdatabasecontents.doInBackground` · line 344
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `tblGroups`
```sql
INSERT INTO tblGroups (GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId)
VALUES (${Double.valueOf(reset.getDouble("GroupId"))}, ${reset.getString("GroupName")}, ${reset.getString("GroupUnder")}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${reset.getString("isShowDetails")}, ${reset.getString("isVisible")}, ${Integer.valueOf(reset.getInt("ANId"))})
```

#### `asyncdatabasecontents.doInBackground` · line 346
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `LoansAllowed`
```sql
Select LId, LoanIndex from LoansAllowed order by LId
```

- Reads: `LId`, `LoanIndex`

#### `asyncdatabasecontents.doInBackground` · line 351
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoansAllowed`
```sql
INSERT INTO LoansAllowed (LId, LoanIndex)
VALUES (${Integer.valueOf(reset.getInt("LId"))}, ${Integer.valueOf(reset.getInt("LoanIndex"))})
```

#### `asyncdatabasecontents.doInBackground` · line 358
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `SoftSettings`
```sql
Select SSId,SSDesc,SSValue,Datevalue,bitvalue,NumericValue from SoftSettings
```

- Reads: `bitValue`, `NumericValue`, `BitValue`, `SSValue`, `CharValue`

#### `asyncdatabasecontents.doInBackground` · line 487
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `venussoftsettings`
```sql
Select  VSSId,VSSName,CharValue,NumericValue,BitValue,DateValue from venussoftsettings
```

#### `asyncdatabasecontents.doInBackground` · line 512
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `stockgrp`
```sql
Create table IF NOT EXISTS stockgrp(GrpId int,GrpDesc String,HSNSACCode String,IsActive Boolean,DisplayIndex int)
```

#### `asyncdatabasecontents.doInBackground` · line 513
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `stockgrp`
```sql
delete from stockgrp
```

#### `asyncdatabasecontents.doInBackground` · line 514
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `stockgrp`
```sql
Select GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex from stockgrp order by DisplayIndex
```

- Reads: `GrpId`, `GrpDesc`, `HSNSACCode`, `IsActive`, `DisplayIndex`

#### `asyncdatabasecontents.doInBackground` · line 523
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockgrp`
```sql
INSERT INTO stockgrp (GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex)
VALUES (${Double.valueOf(reset.getDouble("GrpId"))}, ${reset.getString("GrpDesc")}, ${Double.valueOf(reset.getDouble("HSNSACCode"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))}, ${Double.valueOf(reset.getDouble("DisplayIndex"))})
```

#### `asyncdatabasecontents.doInBackground` · line 528
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `stockcat`
```sql
Create table IF NOT EXISTS stockcat(CatId Double,CatDesc String,DisplayIndex Int,IsActive Boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 529
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `stockcat`
```sql
delete from stockcat
```

#### `asyncdatabasecontents.doInBackground` · line 530
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `stockcat`
```sql
Select CatId, CatDesc, DisplayIndex, IsActive from stockcat order by DisplayIndex
```

- Reads: `CatId`, `CatDesc`, `DisplayIndex`, `IsActive`

#### `asyncdatabasecontents.doInBackground` · line 537
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockcat`
```sql
INSERT INTO stockcat (CatId, CatDesc, DisplayIndex, IsActive)
VALUES (${Double.valueOf(reset.getDouble("CatId"))}, ${reset.getString("CatDesc")}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

#### `asyncdatabasecontents.doInBackground` · line 539
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `stockUOM`
```sql
Create table IF NOT EXISTS stockUOM(UOMId Int,UOMDesc String,DisplayIndex Int,IsActive Boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 540
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `stockUOM`
```sql
delete from stockUOM
```

#### `asyncdatabasecontents.doInBackground` · line 541
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `stockUOM`
```sql
Select UOMId, UOMDesc, DisplayIndex, IsActive from stockUOM order by DisplayIndex
```

- Reads: `UOMId`, `UOMDesc`, `DisplayIndex`, `IsActive`

#### `asyncdatabasecontents.doInBackground` · line 548
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockUOM`
```sql
INSERT INTO stockUOM (UOMId, UOMDesc, DisplayIndex, IsActive)
VALUES (${Integer.valueOf(reset.getInt("UOMId"))}, ${reset.getString("UOMDesc")}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

#### `asyncdatabasecontents.doInBackground` · line 550
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ItemMaster`
```sql
Create table IF NOT EXISTS ItemMaster(ItemCode Int,ItemName String,PriUOMid Int,Catid Int,Grpid Int,MinimumOrderIdQty Int,ReOrderLevel Int )
```

#### `asyncdatabasecontents.doInBackground` · line 551
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ItemMaster`
```sql
delete from ItemMaster
```

#### `asyncdatabasecontents.doInBackground` · line 554
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `ItemMaster`
```sql
Select ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel
from ItemMaster
order by ItemName
```

- Reads: `ItemCode`, `ItemName`, `PriUOMid`, `Catid`, `Grpid`, `MinimumOrderIdQty`, `ReOrderLevel`

#### `asyncdatabasecontents.doInBackground` · line 564
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ItemMaster`
```sql
INSERT INTO ItemMaster (ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel)
VALUES (${Integer.valueOf(reset.getInt("ItemCode"))}, ${reset.getString("ItemName")}, ${Integer.valueOf(reset.getInt("PriUOMid"))}, ${Integer.valueOf(reset.getInt("Catid"))}, ${Integer.valueOf(reset.getInt("Grpid"))}, ${Integer.valueOf(reset.getInt("MinimumOrderIdQty"))}, ${Integer.valueOf(reset.getInt("ReOrderLevel"))})
```

#### `asyncdatabasecontents.doInBackground` · line 566
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `StockUOM`
```sql
Create table IF NOT EXISTS StockUOM(UOMId Int,UOMDesc String,DisplayIndex Int,IsActive Boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 567
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `StockUOM`
```sql
delete from StockUOM
```

#### `asyncdatabasecontents.doInBackground` · line 568
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `StockUOM`
```sql
Select UOMId, UOMDesc, DisplayIndex, IsActive from StockUOM order by DisplayIndex
```

- Reads: `UOMId`, `UOMDesc`, `DisplayIndex`, `IsActive`

#### `asyncdatabasecontents.doInBackground` · line 575
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `StockUOM`
```sql
INSERT INTO StockUOM (UOMId, UOMDesc, DisplayIndex, IsActive)
VALUES (${Integer.valueOf(reset.getInt("UOMId"))}, ${reset.getString("UOMDesc")}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

#### `asyncdatabasecontents.doInBackground` · line 585
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `RelatedDocmenttypes`
```sql
Create table IF NOT EXISTS RelatedDocmenttypes(DUID Int,DocDesc String,isvisible Boolean,showid Int,isImageAllowed Boolean)
```

#### `asyncdatabasecontents.doInBackground` · line 586
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `RelatedDocmenttypes`
```sql
delete from RelatedDocmenttypes
```

#### `asyncdatabasecontents.doInBackground` · line 587
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `RelatedDocmenttypes`
```sql
Select DUID, DocDesc, isvisible, showid, isImageAllowed from RelatedDocmenttypes order by DUId
```

- Reads: `DUID`, `DocDesc`, `isvisible`, `showid`, `isImageAllowed`

#### `asyncdatabasecontents.doInBackground` · line 595
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `RelatedDocmenttypes`
```sql
INSERT INTO RelatedDocmenttypes (DUID, DocDesc, isvisible, showid, isImageAllowed)
VALUES (${Integer.valueOf(reset.getInt("DUID"))}, ${reset.getString("DocDesc")}, ${Boolean.valueOf(reset.getBoolean("isvisible"))}, ${Integer.valueOf(reset.getInt("showid"))}, ${Boolean.valueOf(reset.getBoolean("isImageAllowed"))})
```

#### `asyncdatabasecontents.doInBackground` · line 669
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `tblLedgers`
```sql
select LedgerId,LedgerName,FinId from tblLedgers where  ScreenType=8  and designationid=1 and desigstatusid=1
```

- Reads: `LedgerId`, `LedgerName`, `FinId`

#### `asyncdatabasecontents.doInBackground` · line 675
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `CollectionExecutives`
```sql
INSERT INTO CollectionExecutives (LedgerId, LedgerName, FinId)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${reset.getString("LedgerName")}, ${Integer.valueOf(reset.getInt("FinId"))})
```

#### `asyncdatabasecontents.doInBackground` · line 683
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
select isbranchusercontrolallowed from BranchMobileDevlicesAllowed where IMEI='${globalvars.device_id}'
```

- Reads: `isbranchusercontrolallowed`

#### `asyncdatabasecontents.onPostExecute` · line 734
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblSalesDetailsforAndroid`
```sql
Select UserLevelId, mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState,mnuname,mnudisplayname
from tblSalesDetailsforAndroid
order by UserLevelId
```

- Reads: `UserLevelId`, `mnuId`, `ShowState`, `ViewState`, `UpdateState`, `AddState`, `DeleteState`, `mnuname`, `mnudisplayname`

## userroleprevilages_act

Screen: **Menu Access Control (User Privileges)** · [spec](../../brain/modules/01-app-start-registration-device-identity-settings.md#menu-access-control-user-privileges--userroleprevilagesact) · 2 statements

#### `asyncsaveprevillaged.doInBackground` · line 274
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblSalesDetailsforAndroid`
```sql
delete
from tblSalesDetailsforAndroid
where UserLevelId= ${userroleobj.getULId()}')${row.getDeleteState()}','${row.getAddState()}','${row.getUpDateState()}','${row.getViewState()}','${row.getShowState()},'${row.getMnuid()},${userroleobj.getULId()};insert into tblSalesDetailsforAndroid (UserLevelId,mnuId,ShowState,ViewState,UpdateState,AddState,DeleteState)values(
```

#### `asyncuserroles.doInBackground` · line 207
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `AndroidMenus`, `tblSalesDetailsforAndroid`
```sql
select a.mnuid,a.mnulevel,a.mnuName,b.ShowState as 'Select',a.mnudisplayname,b.ViewState as 'View' ,b.UpdateState as 'Update' ,b.AddState as 'Add' ,b.DeleteState  as 'Delete',mnustyle
from AndroidMenus a
left
join tblSalesDetailsforAndroid b on a.mnuId=b.mnuid and b.UserLevelId=${userroleobj.getULId()}
where isallowed=1 and (isonlyformasterdb=0 or isonlyformasterdb='${globalvars.ismasterdb}')
ORDER BY MNUID
```

- Reads: `mnuid`, `mnulevel`, `mnuName`, `Select`, `mnudisplayname`, `View`, `Update`, `Add`, `Delete`, `mnustyle`

