# SQL — 8.16 Admin masters: employees, branch employees, access control, loan methods and finance lines

[← work.md](../../work.md) · [Module spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **153 statements** (103 local SQLite, 50 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`branch_employee`](#branchemployee) (6) · [`branchemployeelist`](#branchemployeelist) (22) · [`branchemployeesaccessearchlist`](#branchemployeesaccessearchlist) (28) · [`branchemployeesearchlist`](#branchemployeesearchlist) (33) · [`employeedetails_act`](#employeedetailsact) (6) · [`employeesaccessearchlist`](#employeesaccessearchlist) (13) · [`employeesearchlist`](#employeesearchlist) (33) · [`financelinesaddandedit`](#financelinesaddandedit) (2) · [`loanmethods`](#loanmethods) (3) · [`loanmethods_searchlist`](#loanmethodssearchlist) (7)

## branch_employee

Screen: **Branch employee add / edit** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#branch-employee-add--edit--branchemployee) · 6 statements

#### `ASYNCupdateemployee.doInBackground` · line 157
**SQL Server** · `jdbc.prepareCall` · master DB · CALL · procedure: `insertbranchempledgers`
```sql
{call dbo.InsertBranchEmpLedgers(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "LedgerName" → `receivedemployee.getLedgerName()` (setString) · "GroupId" → `29.0d` (setDouble) · "FinId" → `4` (setNull) · "Tag" → `""` (setString) · "AddLine1" → `receivedemployee.getAddLine1()` (setString) · "AddLine2" → `receivedemployee.getAddLine2()` (setString) · "City" → `receivedemployee.getCity()` (setString) · "PinCode" → `receivedemployee.getPinCode()` (setString) · "MobileNo1" → `receivedemployee.getMobileNo1()` (setString) · "MobileNo2" → `receivedemployee.getMobileNo2()` (setString) · "EMail" → `receivedemployee.getEMail()` (setString) · "designationid" → `1` (setInt) · "desigstatusid" → `receivedemployee.getDesigstatusid()` (setInt) · "ScreenType" → `8` (setInt)
- OUT: "LedgerId" (8)
- Reads: `LedgerId`

#### `ASYNCupdateemployee.doInBackground` · line 189
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchEMPLedgers`
```sql
INSERT INTO BranchEMPLedgers (LedgerName, GroupId, LedgerId, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType)
VALUES (${receivedemployee.getLedgerName()}, ${receivedemployee.getGroupId()}, ${receivedemployee.getLedgerId()}, ${receivedemployee.getAddLine1()}, ${receivedemployee.getAddLine2()}, ${receivedemployee.getCity()}, ${receivedemployee.getPinCode()}, ${receivedemployee.getMobileNo1()}, ${receivedemployee.getMobileNo2()}, ${receivedemployee.getEMail()}, ${Integer.valueOf(receivedemployee.getDesignationid())}, ${Integer.valueOf(receivedemployee.getDesigstatusid())}, ${Integer.valueOf(receivedemployee.getScreenType())})
```

#### `ASYNCupdateemployee.doInBackground` · line 195
**SQL Server** · `jdbc.prepareStatement` · master DB · UPDATE · tables: `BranchEMPLedgers`
```sql
update BranchEMPLedgers set LedgerName=?,GroupId=?,FinId=?,Tag=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,MobileNo1=?,MobileNo2=?,EMail=?,designationid=?,desigstatusid=?,ScreenType=?
where LedgerId=?
```

- Params: 1 → `receivedemployee.getLedgerName()` (setString) · 2 → `receivedemployee.getGroupId().doubleValue()` (setDouble) · 3 → `4` (setNull) · 4 → `""` (setString) · 5 → `receivedemployee.getAddLine1()` (setString) · 6 → `receivedemployee.getAddLine2()` (setString) · 7 → `receivedemployee.getCity()` (setString) · 8 → `receivedemployee.getPinCode()` (setString) · 9 → `receivedemployee.getMobileNo1()` (setString) · 10 → `receivedemployee.getMobileNo2()` (setString) · 11 → `receivedemployee.getEMail()` (setString) · 12 → `receivedemployee.getDesignationid()` (setInt) · 13 → `receivedemployee.getDesigstatusid()` (setInt) · 14 → `receivedemployee.getScreenType()` (setInt) · 15 → `receivedemployee.getLedgerId().intValue()` (setInt)

#### `ASYNCupdateemployee.doInBackground` · line 224
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `BranchEMPLedgers`
```sql
UPDATE BranchEMPLedgers SET LedgerName=${receivedemployee.getLedgerName()}, GroupId=${receivedemployee.getGroupId()}, AddLine1=${receivedemployee.getAddLine1()}, AddLine2=${receivedemployee.getAddLine2()}, City=${receivedemployee.getCity()}, PinCode=${receivedemployee.getPinCode()}, MobileNo1=${receivedemployee.getMobileNo1()}, MobileNo2=${receivedemployee.getMobileNo2()}, EMail=${receivedemployee.getEMail()}, desigstatusid=${Integer.valueOf(receivedemployee.getDesigstatusid())}
WHERE LedgerId=${receivedemployee.getLedgerId().intValue()}
```

#### `ASYNCupdateemployee.doInBackground` · line 226
**SQL Server** · `jdbc.prepareStatement` · master DB · DELETE · tables: `BranchEMPLedgers`
```sql
delete from BranchEMPLedgers where LedgerId=?
```

- Params: 1 → `receivedemployee.getLedgerId().intValue()` (setInt)

#### `ASYNCupdateemployee.doInBackground` · line 231
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `BranchEMPLedgers`
```sql
DELETE FROM BranchEMPLedgers WHERE LedgerId=${receivedemployee.getLedgerId().intValue()}
```

## branchemployeelist

Screen: **Branch employees (master DB)** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#branch-employees-master-db--branchemployeelist) · 22 statements

#### `fetchemployeesandotherdetails` · line 172
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchEMPLedgers`
```sql
Create table IF NOT EXISTS BranchEMPLedgers(LedgerId Double,LedgerName String,GroupId Double,FinId int,Tag String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,designationid int,desigstatusid int,ScreenType int)
```

#### `fetchemployeesandotherdetails` · line 173
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BranchEMPLedgers`
```sql
Select LedgerId,LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType
from BranchEMPLedgers
where desigstatusid=1
order by LedgerName
```

#### `fetchemployeesandotherdetails` · line 175
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BranchEMPLedgers`
```sql
Select LedgerId,LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType
from BranchEMPLedgers
where desigstatusid=1
order by LedgerName
```

#### `fetchemployeesandotherdetails` · line 177
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `BranchEMPLedgers`
```sql
Select LedgerId,LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType
from BranchEMPLedgers
order by LedgerName
```

- Reads: `Tag`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `designationid`, `desigstatusid`, `ScreenType`, `LedgerName`, `LedgerId`, `GroupId`, `FinId`

#### `ASYNCEmployees.doInBackground` · line 326, 337
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
Select MDAId, IMEI, LId, isActive, isbranchusercontrolallowed
from BranchMobileDevlicesAllowed
where IMEI='${globalvars.device_id}'
```

#### `ASYNCEmployees.doInBackground` · line 349
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchEMPLedgers`
```sql
Create table IF NOT EXISTS BranchEMPLedgers(LedgerId Double,LedgerName String,GroupId Double,FinId int,Tag String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,designationid int,desigstatusid int,ScreenType int)
```

#### `ASYNCEmployees.doInBackground` · line 350
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchEMPLedgers`
```sql
delete from BranchEMPLedgers
```

#### `ASYNCEmployees.doInBackground` · line 351
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchEMPLedgers`
```sql
Select LedgerId, LedgerName, GroupId,  FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid,desigstatusid,ScreenType
from BranchEMPLedgers
```

- Reads: `LedgerName`, `GroupId`, `LedgerId`, `FinId`, `Tag`, `AddLine1`, `AddLine2`, `City`, `desigstatusid`, `designationid`, `ScreenType`

#### `ASYNCEmployees.doInBackground` · line 388
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchEMPLedgers`
```sql
INSERT INTO BranchEMPLedgers (LedgerName, GroupId, LedgerId, desigstatusid, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, ScreenType)
VALUES (${LedgerName}, ${GroupId}, ${LedgerId}, ${Integer.valueOf(desigstatusid)}, ${Integer.valueOf(FinId)}, ${Tag}, ${AddLine1}, ${AddLine2}, ${City}, ${PinCode}, ${MobileNo1}, ${MobileNo22}, ${EMail2}, ${Integer.valueOf(designationid)}, ${Integer.valueOf(ScreenType)})
```

#### `ASYNCEmployees.doInBackground` · line 398
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchMobileDevlicesAllowed`
```sql
Create table IF NOT EXISTS BranchMobileDevlicesAllowed(MDAId int,IMEI String,LId Double,isActive Boolean, isbranchusercontrolallowed Boolean)
```

#### `ASYNCEmployees.doInBackground` · line 399
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchMobileDevlicesAllowed`
```sql
delete from BranchMobileDevlicesAllowed
```

#### `ASYNCEmployees.doInBackground` · line 400
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
Select MDAId, IMEI, LId, isActive, isbranchusercontrolallowed from BranchMobileDevlicesAllowed order by MDAId
```

- Reads: `IMEI`

#### `ASYNCEmployees.doInBackground` · line 417
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchMobileDevlicesAllowed`
```sql
INSERT INTO BranchMobileDevlicesAllowed (MDAId, IMEI, LId, isActive, isbranchusercontrolallowed)
VALUES (${Integer.valueOf(MDAId)}, ${IMEI}, ${LId}, ${Boolean.valueOf(isActive)}, ${Boolean.valueOf(isbranchusercontrolallowed)})
```

#### `ASYNCEmployees.doInBackground` · line 427
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchAccessAllowed`
```sql
Create table IF NOT EXISTS BranchAccessAllowed(IMEI String,BId int)
```

#### `ASYNCEmployees.doInBackground` · line 428
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchAccessAllowed`
```sql
delete from BranchAccessAllowed
```

#### `ASYNCEmployees.doInBackground` · line 429
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchAccessAllowed`
```sql
Select distinct(IMEI), BId from BranchAccessAllowed order by BId
```

- Reads: `IMEI`, `BId`

#### `ASYNCEmployees.doInBackground` · line 436
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchAccessAllowed`
```sql
INSERT INTO BranchAccessAllowed (IMEI, BId) VALUES (${IMEI2}, ${Integer.valueOf(BId)})
```

#### `ASYNCEmployees.doInBackground` · line 439
**local SQLite** · `sqlite.execSQL` · DROP · tables: `BranchConnStrs`
```sql
drop table BranchConnStrs
```

#### `ASYNCEmployees.doInBackground` · line 441
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchConnStrs`
```sql
delete from BranchConnStrs
```

#### `ASYNCEmployees.doInBackground` · line 442
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchConnStrs`
```sql
Select BId, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr,ShowId
from BranchConnStrs
order by BId
```

- Reads: `BId`, `ShowId`, `BranchName`, `ConStr`, `FinCashLedgerId`, `CollectionTeamCashLedgerId`, `ServerName`, `DatabaseName`, `DBUserId`, `DBPassword`, `appconstr`

#### `ASYNCEmployees.doInBackground` · line 467
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchConnStrs`
```sql
INSERT INTO BranchConnStrs (BId, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr, ShowId)
VALUES (${Integer.valueOf(BId2)}, ${BranchName}, ${ConStr}, ${Integer.valueOf(FinCashLedgerId)}, ${Integer.valueOf(CollectionTeamCashLedgerId)}, ${ServerName}, ${DatabaseName}, ${DBUserId}, ${DBPassword}, ${appconstr}, ${Integer.valueOf(ShowId)})
```

## branchemployeesaccessearchlist

Screen: **Branch mobile device access control** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#branch-mobile-device-access-control--branchemployeesaccessearchlist) · 28 statements

#### `ASYNCEmployees.doInBackground` · line 223, 277
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchEMPLedgers`
```sql
Create table IF NOT EXISTS BranchEMPLedgers(LedgerId Double,LedgerName String,GroupId Double,FinId int,Tag String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,designationid int,desigstatusid int,ScreenType int)
```

#### `ASYNCEmployees.doInBackground` · line 224, 278
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchEMPLedgers`
```sql
delete from BranchEMPLedgers
```

#### `ASYNCEmployees.doInBackground` · line 225, 279
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchEMPLedgers`
```sql
Select LedgerId, LedgerName, GroupId,  FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid,desigstatusid,ScreenType
from BranchEMPLedgers
```

#### `ASYNCEmployees.doInBackground` · line 232, 328
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchMobileDevlicesAllowed`
```sql
Create table IF NOT EXISTS BranchMobileDevlicesAllowed(MDAId int,IMEI String,LId Double,isActive Boolean, isbranchusercontrolallowed Boolean)
```

#### `ASYNCEmployees.doInBackground` · line 233, 329
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchMobileDevlicesAllowed`
```sql
delete from BranchMobileDevlicesAllowed
```

#### `ASYNCEmployees.doInBackground` · line 234, 330
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
Select MDAId, IMEI, LId, isActive, isbranchusercontrolallowed from BranchMobileDevlicesAllowed order by MDAId
```

#### `ASYNCEmployees.doInBackground` · line 241, 346
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchAccessAllowed`
```sql
Create table IF NOT EXISTS BranchAccessAllowed(IMEI String,BId int)
```

#### `ASYNCEmployees.doInBackground` · line 242, 347
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchAccessAllowed`
```sql
delete from BranchAccessAllowed
```

#### `ASYNCEmployees.doInBackground` · line 243, 348
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchAccessAllowed`
```sql
Select distinct(IMEI), BId from BranchAccessAllowed order by BId
```

#### `ASYNCEmployees.doInBackground` · line 247, 356
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchConnStrs`
```sql
delete from BranchConnStrs
```

#### `ASYNCEmployees.doInBackground` · line 248, 357
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchConnStrs`
```sql
Select BId, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr,ShowId
from BranchConnStrs
order by BId
```

#### `ASYNCEmployees.doInBackground` · line 251, 374
**SQL Server** · `jdbc.Statement.executeQuery` · master DB · SELECT · tables: `BranchMobileDevlicesAllowed`
```sql
select Lid, IMEI ,a.isActive,Ledgername,MDAId
from BranchMobileDevlicesAllowed a, BranchEMPLedgers b
where lid=ledgerid
order by Lid
```

#### `ASYNCEmployees.doInBackground` · line 317
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchEMPLedgers`
```sql
INSERT INTO BranchEMPLedgers (LedgerName, GroupId, LedgerId, desigstatusid, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, ScreenType)
VALUES (${LedgerName}, ${GroupId}, ${LedgerId}, ${Integer.valueOf(desigstatusid)}, ${Integer.valueOf(FinId)}, ${Tag}, ${AddLine1}, ${AddLine2}, ${City}, ${PinCode}, ${MobileNo1}, ${MobileNo2}, ${EMail}, ${Integer.valueOf(designationid)}, ${Integer.valueOf(ScreenType)})
```

#### `ASYNCEmployees.doInBackground` · line 339
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchMobileDevlicesAllowed`
```sql
INSERT INTO BranchMobileDevlicesAllowed (MDAId, IMEI, LId, isActive, isbranchusercontrolallowed)
VALUES (${Integer.valueOf(reset.getInt("MDAId"))}, ${reset.getString("IMEI")}, ${Double.valueOf(reset.getDouble(str10))}, ${Boolean.valueOf(reset.getBoolean("isActive"))}, ${Boolean.valueOf(reset.getBoolean("isbranchusercontrolallowed"))})
```

#### `ASYNCEmployees.doInBackground` · line 353
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchAccessAllowed`
```sql
INSERT INTO BranchAccessAllowed (IMEI, BId)
VALUES (${reset.getString("IMEI")}, ${Integer.valueOf(reset.getInt("BId"))})
```

#### `ASYNCEmployees.doInBackground` · line 372
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `BranchConnStrs`
```sql
INSERT INTO BranchConnStrs (BId, BranchName, ConStr, FinCashLedgerId, CollectionTeamCashLedgerId, ServerName, DatabaseName, DBUserId, DBPassword, appconstr, ShowId, dbidentificationno)
VALUES (${Integer.valueOf(reset.getInt("BId"))}, ${reset.getString("BranchName")}, ${reset.getString("ConStr")}, ${Integer.valueOf(reset.getInt("FinCashLedgerId"))}, ${Integer.valueOf(reset.getInt("CollectionTeamCashLedgerId"))}, ${reset.getString("ServerName")}, ${reset.getString("DatabaseName")}, ${reset.getString("DBUserId")}, ${reset.getString("DBPassword")}, ${reset.getString("appconstr")}, ${Integer.valueOf(reset.getInt("ShowId"))}, ${reset.getString("dbidentificationno")})
```

## branchemployeesearchlist

Screen: **Branch employee list clone (unreachable)** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#branch-employee-list-clone-unreachable--branchemployeesearchlist) · 33 statements

#### `fetchemployeesandotherdetails` · line 158, 160
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `EMPLedgers`
```sql
Select LedgerId,LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType
from EMPLedgers
where desigstatusid=1
order by LedgerName
```

#### `fetchemployeesandotherdetails` · line 162
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `EMPLedgers`
```sql
Select LedgerId,LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType
from EMPLedgers
order by LedgerName
```

- Reads: `LedgerName`, `LedgerId`, `GroupId`, `FinId`, `Tag`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `designationid`, `desigstatusid`, `ScreenType`

#### `ASYNCEmployees.doInBackground` · line 296, 340, 374
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `EMPLedgers`
```sql
Create table IF NOT EXISTS EMPLedgers(LedgerId Double,LedgerName String,GroupId Double,FinId int,Tag String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,designationid int,desigstatusid int,ScreenType int)
```

#### `ASYNCEmployees.doInBackground` · line 297, 341, 375
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `EMPLedgers`
```sql
delete from EMPLedgers
```

#### `ASYNCEmployees.doInBackground` · line 298, 342, 376
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId,  FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid,ScreenType
from tblLedgers
where ScreenType=8
```

#### `ASYNCEmployees.doInBackground` · line 305, 349, 402
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `MobileDevlicesAllowed`
```sql
Create table IF NOT EXISTS MobileDevlicesAllowed(MDAId int,IMEI String,LId Double,isActive Boolean,Userroleid int )
```

#### `ASYNCEmployees.doInBackground` · line 306, 350, 403
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileDevlicesAllowed`
```sql
delete from MobileDevlicesAllowed
```

#### `ASYNCEmployees.doInBackground` · line 307, 351, 404
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
Select MDAId, IMEI, LId, isActive, userroleid from MobileDevlicesAllowed order by MDAId
```

#### `ASYNCEmployees.doInBackground` · line 314, 358, 426
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `FinanceAccessAllowed`
```sql
Create table IF NOT EXISTS FinanceAccessAllowed(IMEI String,FinId int)
```

#### `ASYNCEmployees.doInBackground` · line 315, 359, 427
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceAccessAllowed`
```sql
delete from FinanceAccessAllowed
```

#### `ASYNCEmployees.doInBackground` · line 316, 360, 428
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceAccessAllowed`
```sql
Select distinct(IMEI), FinId from FinanceAccessAllowed order by FinId
```

#### `ASYNCEmployees.doInBackground` · line 395
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `EMPLedgers`
```sql
INSERT INTO EMPLedgers (LedgerName, GroupId, LedgerId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType)
VALUES (${reset.getString("LedgerName")}, ${Double.valueOf(reset.getDouble("GroupId"))}, ${Double.valueOf(reset.getDouble("LedgerId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${reset.getString("Tag")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("Pincode")}, ${reset.getString("MobileNo1")}, ${reset.getString("MobileNo2")}, ${reset.getString("Email")}, ${Integer.valueOf(reset.getInt("designationid"))}, ${Integer.valueOf(reset.getInt(str6))}, ${Integer.valueOf(reset.getInt("Screentype"))})
```

#### `ASYNCEmployees.doInBackground` · line 416
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `MobileDevlicesAllowed`
```sql
INSERT INTO MobileDevlicesAllowed (MDAId, IMEI, LId, isActive, userroleid)
VALUES (${Integer.valueOf(reset.getInt(str7))}, ${reset.getString("IMEI")}, ${Double.valueOf(reset.getDouble(str8))}, ${Boolean.valueOf(reset.getBoolean(str9))}, ${Boolean.valueOf(reset.getBoolean(str10))})
```

#### `ASYNCEmployees.doInBackground` · line 433
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `FinanceAccessAllowed`
```sql
INSERT INTO FinanceAccessAllowed (IMEI, FinId)
VALUES (${reset.getString("IMEI")}, ${Integer.valueOf(reset.getInt("Finid"))})
```

## employeedetails_act

Screen: **Employee add / edit** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#employee-add--edit--employeedetailsact) · 6 statements

#### `ASYNCupdateemployee.doInBackground` · line 224
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertempledgers`
```sql
{call dbo.InsertEmpLedgers(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "LedgerName" → `receivedemployee.getLedgerName()` (setString) · "GroupId" → `29.0d` (setDouble) · "FinId" → `receivedemployee.getFinId()` (setInt) · "FinId" → `4` (setNull) · "Tag" → `""` (setString) · "AddLine1" → `receivedemployee.getAddLine1()` (setString) · "AddLine2" → `receivedemployee.getAddLine2()` (setString) · "City" → `receivedemployee.getCity()` (setString) · "PinCode" → `receivedemployee.getPinCode()` (setString) · "MobileNo1" → `receivedemployee.getMobileNo1()` (setString) · "MobileNo2" → `receivedemployee.getMobileNo2()` (setString) · "EMail" → `receivedemployee.getEMail()` (setString) · "designationid" → `1` (setInt) · "desigstatusid" → `receivedemployee.getDesigstatusid()` (setInt) · "ScreenType" → `8` (setInt)
- OUT: "LedgerId" (8)
- Reads: `LedgerId`

#### `ASYNCupdateemployee.doInBackground` · line 260
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `EMPLedgers`
```sql
INSERT INTO EMPLedgers (LedgerName, GroupId, LedgerId, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType)
VALUES (${receivedemployee.getLedgerName()}, ${receivedemployee.getGroupId()}, ${receivedemployee.getLedgerId()}, ${receivedemployee.getAddLine1()}, ${receivedemployee.getAddLine2()}, ${receivedemployee.getCity()}, ${receivedemployee.getPinCode()}, ${receivedemployee.getMobileNo1()}, ${receivedemployee.getMobileNo2()}, ${receivedemployee.getEMail()}, ${Integer.valueOf(receivedemployee.getDesignationid())}, ${Integer.valueOf(receivedemployee.getDesigstatusid())}, ${Integer.valueOf(receivedemployee.getScreenType())})
```

#### `ASYNCupdateemployee.doInBackground` · line 268
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers where LedgerId=?
```

- Params: 1 → `receivedemployee.getLedgerId().intValue()` (setInt)

#### `ASYNCupdateemployee.doInBackground` · line 273
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `EMPLedgers`
```sql
DELETE FROM EMPLedgers WHERE LedgerId=${receivedemployee.getLedgerId().intValue()}
```

#### `ASYNCupdateemployee.doInBackground` · line 280
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblLedgers`
```sql
update tblLedgers set LedgerName=?,GroupId=?,FinId=?,Tag=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,MobileNo1=?,MobileNo2=?,EMail=?,designationid=?,desigstatusid=?,ScreenType=?
where LedgerId=?
```

- Params: 1 → `receivedemployee.getLedgerName()` (setString) · 2 → `receivedemployee.getGroupId().doubleValue()` (setDouble) · 3 → `receivedemployee.getFinId()` (setInt) · 3 → `4` (setNull) · i → `""` (setString) · 5 → `receivedemployee.getAddLine1()` (setString) · 6 → `receivedemployee.getAddLine2()` (setString) · 7 → `receivedemployee.getCity()` (setString) · 8 → `receivedemployee.getPinCode()` (setString) · 9 → `receivedemployee.getMobileNo1()` (setString) · 10 → `receivedemployee.getMobileNo2()` (setString) · 11 → `receivedemployee.getEMail()` (setString) · 12 → `receivedemployee.getDesignationid()` (setInt) · 13 → `receivedemployee.getDesigstatusid()` (setInt) · 14 → `receivedemployee.getScreenType()` (setInt) · 15 → `receivedemployee.getLedgerId().intValue()` (setInt)

#### `ASYNCupdateemployee.doInBackground` · line 315
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `EMPLedgers`
```sql
UPDATE EMPLedgers SET LedgerName=${receivedemployee.getLedgerName()}, GroupId=${receivedemployee.getGroupId()}, AddLine1=${receivedemployee.getAddLine1()}, AddLine2=${receivedemployee.getAddLine2()}, City=${receivedemployee.getCity()}, PinCode=${receivedemployee.getPinCode()}, MobileNo1=${receivedemployee.getMobileNo1()}, MobileNo2=${receivedemployee.getMobileNo2()}, EMail=${receivedemployee.getEMail()}, desigstatusid=${Integer.valueOf(receivedemployee.getDesigstatusid())}
WHERE LedgerId=${receivedemployee.getLedgerId().intValue()}
```

## employeesaccessearchlist

Screen: **Mobile device access control** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#mobile-device-access-control--employeesaccessearchlist) · 13 statements

#### `ASYNCEmployees.doInBackground` · line 244
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
select Lid, IMEI ,a.isActive,userroleid ,Ledgername,MDAId
from MobileDevlicesAllowed a, tblLedgers b
where lid=ledgerid
order by Lid
```

- Reads: `Lid`, `IMEI`, `Ledgername`, `userroleid`, `isActive`, `MDAId`

#### `ASYNCEmployees.doInBackground` · line 259
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `EMPLedgers`
```sql
Create table IF NOT EXISTS EMPLedgers(LedgerId Double,LedgerName String,GroupId Double,FinId int,Tag String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,designationid int,desigstatusid int,ScreenType int)
```

#### `ASYNCEmployees.doInBackground` · line 260
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `EMPLedgers`
```sql
delete from EMPLedgers
```

#### `ASYNCEmployees.doInBackground` · line 261
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId,  FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid,ScreenType
from tblLedgers
where ScreenType=8
```

- Reads: `LedgerName`, `GroupId`, `LedgerId`, `FinId`, `Tag`, `AddLine1`, `AddLine2`, `Pincode`, `Email`, `Screentype`

#### `ASYNCEmployees.doInBackground` · line 285
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `EMPLedgers`
```sql
INSERT INTO EMPLedgers (LedgerName, GroupId, LedgerId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType)
VALUES (${reset.getString("LedgerName")}, ${Double.valueOf(reset.getDouble("GroupId"))}, ${Double.valueOf(reset.getDouble("LedgerId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${reset.getString("Tag")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString(str5)}, ${reset.getString("Pincode")}, ${reset.getString(str4)}, ${reset.getString(str10)}, ${reset.getString("Email")}, ${Integer.valueOf(reset.getInt(str11))}, ${Integer.valueOf(reset.getInt(str12))}, ${Integer.valueOf(reset.getInt("Screentype"))})
```

#### `ASYNCEmployees.doInBackground` · line 294
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `MobileDevlicesAllowed`
```sql
Create table IF NOT EXISTS MobileDevlicesAllowed(MDAId int,IMEI String,LId Double,isActive Boolean,Userroleid int )
```

#### `ASYNCEmployees.doInBackground` · line 295
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileDevlicesAllowed`
```sql
delete from MobileDevlicesAllowed
```

#### `ASYNCEmployees.doInBackground` · line 296
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
Select MDAId, IMEI, LId, isActive, userroleid from MobileDevlicesAllowed order by MDAId
```

- Reads: `MDAId`, `IMEI`, `isActive`, `userroleid`

#### `ASYNCEmployees.doInBackground` · line 305
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `MobileDevlicesAllowed`
```sql
INSERT INTO MobileDevlicesAllowed (MDAId, IMEI, LId, isActive, userroleid)
VALUES (${Integer.valueOf(reset.getInt("MDAId"))}, ${reset.getString("IMEI")}, ${Double.valueOf(reset.getDouble(str13))}, ${Boolean.valueOf(reset.getBoolean("isActive"))}, ${Boolean.valueOf(reset.getBoolean("userroleid"))})
```

#### `ASYNCEmployees.doInBackground` · line 312
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `FinanceAccessAllowed`
```sql
Create table IF NOT EXISTS FinanceAccessAllowed(IMEI String,FinId int)
```

#### `ASYNCEmployees.doInBackground` · line 313
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceAccessAllowed`
```sql
delete from FinanceAccessAllowed
```

#### `ASYNCEmployees.doInBackground` · line 314
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceAccessAllowed`
```sql
Select distinct(IMEI), FinId from FinanceAccessAllowed order by FinId
```

- Reads: `IMEI`, `Finid`

#### `ASYNCEmployees.doInBackground` · line 319
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `FinanceAccessAllowed`
```sql
INSERT INTO FinanceAccessAllowed (IMEI, FinId)
VALUES (${reset.getString("IMEI")}, ${Integer.valueOf(reset.getInt("Finid"))})
```

## employeesearchlist

Screen: **Employees list** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#employees-list--employeesearchlist) · 33 statements

#### `fetchemployeesandotherdetails` · line 174, 176
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `EMPLedgers`
```sql
Select LedgerId,LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType
from EMPLedgers
where desigstatusid=1
order by LedgerName
```

#### `fetchemployeesandotherdetails` · line 178
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `EMPLedgers`
```sql
Select LedgerId,LedgerName, GroupId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType
from EMPLedgers
order by LedgerName
```

- Reads: `LedgerName`, `LedgerId`, `GroupId`, `FinId`, `Tag`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `designationid`, `desigstatusid`, `ScreenType`

#### `ASYNCEmployees.doInBackground` · line 316, 360, 394
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `EMPLedgers`
```sql
Create table IF NOT EXISTS EMPLedgers(LedgerId Double,LedgerName String,GroupId Double,FinId int,Tag String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,designationid int,desigstatusid int,ScreenType int)
```

#### `ASYNCEmployees.doInBackground` · line 317, 361, 395
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `EMPLedgers`
```sql
delete from EMPLedgers
```

#### `ASYNCEmployees.doInBackground` · line 318, 362, 396
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId,  FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid,ScreenType
from tblLedgers
where ScreenType=8
```

#### `ASYNCEmployees.doInBackground` · line 325, 369, 422
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `MobileDevlicesAllowed`
```sql
Create table IF NOT EXISTS MobileDevlicesAllowed(MDAId int,IMEI String,LId Double,isActive Boolean,Userroleid int )
```

#### `ASYNCEmployees.doInBackground` · line 326, 370, 423
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileDevlicesAllowed`
```sql
delete from MobileDevlicesAllowed
```

#### `ASYNCEmployees.doInBackground` · line 327, 371, 424
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
Select MDAId, IMEI, LId, isActive, userroleid from MobileDevlicesAllowed order by MDAId
```

#### `ASYNCEmployees.doInBackground` · line 334, 378, 446
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `FinanceAccessAllowed`
```sql
Create table IF NOT EXISTS FinanceAccessAllowed(IMEI String,FinId int)
```

#### `ASYNCEmployees.doInBackground` · line 335, 379, 447
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `FinanceAccessAllowed`
```sql
delete from FinanceAccessAllowed
```

#### `ASYNCEmployees.doInBackground` · line 336, 380, 448
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceAccessAllowed`
```sql
Select distinct(IMEI), FinId from FinanceAccessAllowed order by FinId
```

#### `ASYNCEmployees.doInBackground` · line 415
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `EMPLedgers`
```sql
INSERT INTO EMPLedgers (LedgerName, GroupId, LedgerId, FinId, Tag, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, designationid, desigstatusid, ScreenType)
VALUES (${reset.getString("LedgerName")}, ${Double.valueOf(reset.getDouble("GroupId"))}, ${Double.valueOf(reset.getDouble("LedgerId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${reset.getString("Tag")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("Pincode")}, ${reset.getString("MobileNo1")}, ${reset.getString("MobileNo2")}, ${reset.getString("Email")}, ${Integer.valueOf(reset.getInt("designationid"))}, ${Integer.valueOf(reset.getInt(str6))}, ${Integer.valueOf(reset.getInt("Screentype"))})
```

#### `ASYNCEmployees.doInBackground` · line 436
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `MobileDevlicesAllowed`
```sql
INSERT INTO MobileDevlicesAllowed (MDAId, IMEI, LId, isActive, userroleid)
VALUES (${Integer.valueOf(reset.getInt(str7))}, ${reset.getString("IMEI")}, ${Double.valueOf(reset.getDouble(str8))}, ${Boolean.valueOf(reset.getBoolean(str9))}, ${Boolean.valueOf(reset.getBoolean(str10))})
```

#### `ASYNCEmployees.doInBackground` · line 453
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `FinanceAccessAllowed`
```sql
INSERT INTO FinanceAccessAllowed (IMEI, FinId)
VALUES (${reset.getString("IMEI")}, ${Integer.valueOf(reset.getInt("Finid"))})
```

## financelinesaddandedit

Screen: **Edit line (finance) details** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#edit-line-finance-details--financelinesaddandedit) · 2 statements

#### `ASYNCupdatefinance.doInBackground` · line 206
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `FinanceMaster`
```sql
update FinanceMaster set FinName=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,RegistrationNo=?,ShortName=?,EmailIdentifier=?,SMSIdentifier=?,FinMailingName=?,AdminMobileNos=?,DefaultLoanMethodId=?,BankLedgerid=?
where FinId=?
```

- Params: 1 → `receivedfinance.getFinName()` (setString) · 2 → `receivedfinance.getAddLine1()` (setString) · 3 → `receivedfinance.getAddLine2()` (setString) · 4 → `receivedfinance.getCity()` (setString) · 5 → `receivedfinance.getPinCode()` (setString) · 6 → `receivedfinance.getRegistrationNo()` (setString) · 7 → `receivedfinance.getShortName()` (setString) · 8 → `receivedfinance.getEmailIdentifier()` (setString) · 9 → `receivedfinance.getSMSIdentifier()` (setString) · 10 → `receivedfinance.getFinMailingName()` (setString) · 11 → `receivedfinance.getAdminMobileNos()` (setString) · 12 → `receivedfinance.getDefaultLoanMethodId().intValue()` (setInt) · 13 → `receivedfinance.getDefaultBankledgersid()` (setInt) · 14 → `receivedfinance.getFinId().intValue()` (setInt)

#### `ASYNCupdatefinance.doInBackground` · line 222
**local SQLite** · `sqlite.compileStatement` · UPDATE · tables: `FinanceDetails`
```sql
Update  FinanceDetails set  FinName=?, AddLine1=?, AddLine2=?, City=?, PinCode=?, RegistrationNo=?,  ShortName=?, EmailIdentifier=?, SMSIdentifier=?, FinMailingName=?,  AdminMobileNos=?, DefaultLoanMethodId=?,BankLedgerid=?
where FinId=?
```

- Params: 1 → `receivedfinance.getFinName()` (bindString) · 2 → `receivedfinance.getAddLine1()` (bindString) · 3 → `receivedfinance.getAddLine2()` (bindString) · 4 → `receivedfinance.getCity()` (bindString) · 5 → `receivedfinance.getPinCode()` (bindString) · 6 → `receivedfinance.getRegistrationNo()` (bindString) · 7 → `receivedfinance.getShortName()` (bindString) · 8 → `receivedfinance.getEmailIdentifier()` (bindString) · 9 → `receivedfinance.getSMSIdentifier()` (bindString) · 10 → `receivedfinance.getFinMailingName()` (bindString) · 11 → `receivedfinance.getAdminMobileNos()` (bindString) · 12 → `receivedfinance.getDefaultLoanMethodId().intValue()` (bindLong) · 13 → `receivedfinance.getDefaultBankledgersid()` (bindLong) · 14 → `receivedfinance.getFinId().intValue()` (bindDouble)

## loanmethods

Screen: **Loan method add / edit** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#loan-method-add--edit--loanmethods) · 3 statements

#### `ASYNCUpdateLoanmethods.doInBackground` · line 339
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertloanmethods`
```sql
{call dbo.InsertLoanMethods(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "LoanMethodDesc" → `receiveddata.getLoanMethodDesc()` (setString) · "LoanAmount" → `receiveddata.getLoanAmount()` (setDouble) · "LoanAmountGiven" → `receiveddata.getLoanAmountGiven()` (setDouble) · "IntDeduction" → `receiveddata.getIntDeduction()` (setDouble) · "IntAddition" → `receiveddata.getIntAddition()` (setDouble) · "TotAmountCollectable" → `receiveddata.getTotAmountCollectable()` (setDouble) · "CollectionBreak" → `receiveddata.getCollectionBreak().intValue()` (setInt) · "CollectionBreakString" → `receiveddata.getCollectionBreakString()` (setString) · "NoOfIns" → `receiveddata.getNoOfIns().intValue()` (setDouble) · "InsAmount" → `receiveddata.getInsAmount()` (setDouble) · "GracePeriod" → `receiveddata.getGracePeriod().intValue()` (setDouble) · "DocChargesMethod" → `receiveddata.getDocChargesMethod().intValue()` (setDouble) · "DocCharges" → `receiveddata.getDocCharges().intValue()` (setDouble) · "ReportIdentification" → `receiveddata.getReportIdentification()` (setString) · "CBCount" → `receiveddata.getCBCount()` (setInt) · "IsVisible" → `receiveddata.getVisible()` (setBoolean) · "ShowId" → `receiveddata.getShowId().intValue()` (setDouble) · "WarningDays" → `receiveddata.getWarningDays()` (setInt) · "DueSupportAmount" → `receiveddata.getDueSupportAmount()` (setDouble)
- OUT: "LoanMethodId" (8)
- Reads: `LoanMethodId`

#### `ASYNCUpdateLoanmethods.doInBackground` · line 363
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `LoanMethods`
```sql
update LoanMethods set LoanMethodDesc=?,LoanAmount=?,LoanAmountGiven=?,IntDeduction=?,IntAddition=?,TotAmountCollectable=?,CollectionBreak=?,CollectionBreakString=?,NoOfIns=?,InsAmount=?,GracePeriod=?,DocChargesMethod=?,DocCharges=?,ReportIdentification=?,CBCount=?,IsVisible=?,ShowId=?,WarningDays=?,DueSupportAmount=?
where LoanMethodId=?
```

- Params: 1 → `receiveddata.getLoanMethodDesc()` (setString) · 2 → `receiveddata.getLoanAmount()` (setDouble) · 3 → `receiveddata.getLoanAmountGiven()` (setDouble) · 4 → `receiveddata.getIntDeduction()` (setDouble) · 5 → `receiveddata.getIntAddition()` (setDouble) · 6 → `receiveddata.getTotAmountCollectable()` (setDouble) · 7 → `receiveddata.getCollectionBreak().intValue()` (setInt) · 8 → `receiveddata.getCollectionBreakString()` (setString) · 9 → `receiveddata.getNoOfIns().intValue()` (setDouble) · 10 → `receiveddata.getInsAmount()` (setDouble) · 11 → `receiveddata.getGracePeriod().intValue()` (setDouble) · 12 → `receiveddata.getDocChargesMethod().intValue()` (setDouble) · 13 → `receiveddata.getDocCharges().intValue()` (setDouble) · 14 → `receiveddata.getReportIdentification()` (setString) · 15 → `receiveddata.getCBCount()` (setInt) · 16 → `receiveddata.getVisible()` (setBoolean) · 17 → `receiveddata.getShowId().intValue()` (setDouble) · 18 → `receiveddata.getWarningDays()` (setInt) · 19 → `receiveddata.getDueSupportAmount()` (setDouble) · 20 → `receiveddata.getLoanMethodId().intValue()` (setInt)

#### `ASYNCUpdateLoanmethods.doInBackground` · line 386
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `LoanMethods`
```sql
delete from LoanMethods where LoanMethodId=?
```

- Params: 1 → `receiveddata.getLoanMethodId().intValue()` (setInt)

## loanmethods_searchlist

Screen: **Loan methods list** · [spec](../../brain/modules/16-admin-masters-employees-branch-employees-access.md#loan-methods-list--loanmethodssearchlist) · 7 statements

#### `ASYNCAccountsLedgers.doInBackground` · line 305, 328
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `LoanMethods`
```sql
Create table IF NOT EXISTS LoanMethods(LoanMethodId Double,LoanMethodDesc String,LoanAmount Double,LoanAmountGiven Double,IntDeduction Double,IntAddition Double,TotAmountCollectable Double,CollectionBreak String,CollectionBreakString String,NoOfIns Double,InsAmount Double,GracePeriod Double,DocChargesMethod Double,DocCharges Double,ReportIdentification String,CBCount int,IsVisible Boolean,ShowId Double,WarningDays int,DueSupportAmount int)
```

#### `ASYNCAccountsLedgers.doInBackground` · line 306, 329
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LoanMethods`
```sql
delete from LoanMethods
```

#### `ASYNCAccountsLedgers.doInBackground` · line 307, 330
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `LoanMethods`
```sql
Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount
from LoanMethods
order by LoanMethodId
```

#### `ASYNCAccountsLedgers.doInBackground` · line 397
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoanMethods`
```sql
INSERT INTO LoanMethods (LoanMethodId, LoanMethodDesc, LoanAmount, DueSupportAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays)
VALUES (${LoanMethodId}, ${LoanMethodDesc}, ${LoanAmount}, ${Integer.valueOf(DueSupportAmount)}, ${LoanAmountGiven}, ${IntDeduction}, ${IntAddition}, ${TotAmountCollectable}, ${CollectionBreak}, ${CollectionBreakString3}, ${NoOfIns}, ${InsAmount}, ${GracePeriod}, ${DocChargesMethod}, ${DocCharges}, ${ReportIdentification3}, ${Integer.valueOf(CBCount)}, ${Boolean.valueOf(IsVisible)}, ${ShowId}, ${Integer.valueOf(WarningDays)})
```

