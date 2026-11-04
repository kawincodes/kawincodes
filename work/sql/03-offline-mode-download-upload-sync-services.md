# SQL — 8.3 Offline mode: download, upload and sync services

[← work.md](../../work.md) · [Module spec](../../brain/modules/03-offline-mode-download-upload-sync-services.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **216 statements** (141 local SQLite, 75 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`downloaddata`](#downloaddata) (92) · [`MyIntentService`](#myintentservice) (5) · [`MyService_updatecollection`](#myserviceupdatecollection) (7) · [`newtodaycollectionofflinedata_list`](#newtodaycollectionofflinedatalist) (1) · [`uploadcollection`](#uploadcollection) (110) · [`uploaddownloadcontroling`](#uploaddownloadcontroling) (1)

## downloaddata

Screen: **Download Data** · [spec](../../brain/modules/03-offline-mode-download-upload-sync-services.md#download-data--downloaddata) · 92 statements

#### `lambda$onCreate$1` · line 141
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where LedgerId not in(${exectiveledgerid_2})
order by LedgerName
```

#### `lambda$onCreate$1` · line 143
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where FinId=${globalvars.finid} and  LedgerId not in(${exectiveledgerid_2})
order by LedgerName
```

#### `lambda$onCreate$1` · line 146
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName from CollectionExecutives order by LedgerName
```

#### `lambda$onCreate$1` · line 148
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName from CollectionExecutives where FinId=${globalvars.finid}   order by LedgerName
```

- Reads: `LedgerName`, `LedgerId`

#### `lambda$onCreate$2` · line 175
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where LedgerId not in(${exectiveledgerid_1})
order by LedgerName
```

#### `lambda$onCreate$2` · line 177
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where FinId=${globalvars.finid} and  LedgerId not in(${exectiveledgerid_1})
order by LedgerName
```

#### `lambda$onCreate$2` · line 180
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName from CollectionExecutives order by LedgerName
```

#### `lambda$onCreate$2` · line 182
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName from CollectionExecutives where FinId=${globalvars.finid}   order by LedgerName
```

- Reads: `LedgerName`, `LedgerId`

#### `onActivityResult` · line 215
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where LedgerId not in(${((Bundle) Objects.requireNonNull(data.getExtras())).getInt("valuemember")})
order by LedgerName
```

#### `onActivityResult` · line 217
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where FinId=${globalvars.finid} and  LedgerId not in(${((Bundle) Objects.requireNonNull(data.getExtras())).getInt("valuemember")})
order by LedgerName
```

#### `onActivityResult` · line 220
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName from CollectionExecutives order by LedgerName
```

#### `onActivityResult` · line 222
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName from CollectionExecutives where FinId=${globalvars.finid}   order by LedgerName
```

- Reads: `LedgerName`, `LedgerId`

#### `AsyncDownloaddata.doInBackground` · line 381
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `DownloadControling`, `FinanceAccessAllowed`, `softsettings`
```sql
select top 1 x.IMEI,convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(), '+05:30'),0 ) as DownloadTime ,case when (select  bitvalue
from softsettings
where SSId=32)='True' and isAllowed is not null and isAllowed='True' then 'True' when (select bitvalue
from softsettings
where SSId=32)='False' then 'True' else 'False' end  as 'IsAllowed'
from FinanceAccessAllowed x
left
join DownloadControling z on x.FinId=z.FinId and TransDate='${globalvars.collectiondate}',MobileDevlicesAllowed y
where x.IMEI=y.IMEI and x.FinId=${globalvars.finid} and y.isActive =1 and x.IMEI='${globalvars.device_id}'
```

- Reads: `isAllowed`, `DownloadTime`

#### `AsyncDownloaddata.doInBackground` · line 390
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceMaster`
```sql
select RecentRenewal,ExpiryDate,LicenseDate,RenewalAmount,RenewalNoticeDate
From FinanceMaster
where FinId=${globalvars.finid}
```

- Reads: `ExpiryDate`, `RenewalNoticeDate`

#### `AsyncDownloaddata.doInBackground` · line 406
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
select userroleid from MobileDevlicesAllowed where IMEI='${globalvars.device_id}'
```

#### `AsyncDownloaddata.doInBackground` · line 419
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue
from SpecialAccessAllowed
where UserLevelId=${globalvars.app_userroleid}
order by SAAId
```

- Reads: `SAAId`, `UserLevelId`, `SARId`, `bitvalue`

#### `AsyncDownloaddata.doInBackground` · line 426
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SpecialAccessAllowed`
```sql
INSERT INTO SpecialAccessAllowed (SAAId, UserLevelId, SARId, bitvalue)
VALUES (${Integer.valueOf(reset.getInt("SAAId"))}, ${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("SARId"))}, ${Boolean.valueOf(reset.getBoolean("bitvalue"))})
```

#### `AsyncDownloaddata.doInBackground` · line 430
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `AndroidMenus`, `tblSalesDetailsforAndroid`
```sql
Select  mnuname,mnudisplayname,  UserLevelId, a.mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState,mnustyle
from AndroidMenus a
left
join tblSalesDetailsforAndroid b on a.mnuid=b.mnuid and b.UserLevelId=${globalvars.app_userroleid}
where a.isAllowed =1 and(isonlyformasterdb='FALSE' OR  isonlyformasterdb='${globalvars.ismasterdb}')
order by mnuId
```

- Reads: `UserLevelId`, `mnuId`, `mnustyle`, `ShowState`, `ViewState`, `UpdateState`, `AddState`, `DeleteState`, `mnuname`, `mnudisplayname`

#### `AsyncDownloaddata.doInBackground` · line 443
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblSalesDetailsforAndroid`
```sql
INSERT INTO tblSalesDetailsforAndroid (UserLevelId, mnuId, mnustyle, ShowState, ViewState, UpdateState, AddState, DeleteState, mnuname, mnudisplayname)
VALUES (${Integer.valueOf(reset.getInt("UserLevelId"))}, ${Integer.valueOf(reset.getInt("mnuId"))}, ${Integer.valueOf(reset.getInt("mnustyle"))}, ${Boolean.valueOf(reset.getBoolean("ShowState"))}, ${Boolean.valueOf(reset.getBoolean("ViewState"))}, ${Boolean.valueOf(reset.getBoolean("UpdateState"))}, ${Boolean.valueOf(reset.getBoolean("AddState"))}, ${Boolean.valueOf(reset.getBoolean("DeleteState"))}, ${reset.getString("mnuname")}, ${reset.getString("mnudisplayname")})
```

#### `AsyncDownloaddata.doInBackground` · line 445
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SpecialAccessAllowed`
```sql
Select SAAId, UserLevelId, SARId, bitvalue
from SpecialAccessAllowed
where UserLevelId=${globalvars.app_userroleid}
order by SAAId
```

- Reads: `SARId`, `bitvalue`

#### `AsyncDownloaddata.doInBackground` · line 482
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `LoanMethods`
```sql
DELETE FROM LoanMethods WHERE LoanMethodId > ?
```

- Selection args: `new String[]{"0"}`

#### `AsyncDownloaddata.doInBackground` · line 483
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `LoanMethods`
```sql
Select LoanMethodId, LoanMethodDesc, LoanAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays,DueSupportAmount
from LoanMethods
order by LoanMethodId
```

- Reads: `LoanMethodId`, `LoanMethodDesc`, `LoanAmount`, `DueSupportAmount`, `LoanAmountGiven`, `IntDeduction`, `IntAddition`, `TotAmountCollectable`, `CollectionBreak`, `CollectionBreakString`, `NoOfIns`, `InsAmount`, `GracePeriod`, `DocChargesMethod`, `DocCharges`, `ReportIdentification`, `CBCount`, `IsVisible`, `ShowId`, `WarningDays`

#### `AsyncDownloaddata.doInBackground` · line 512
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `LoanMethods`
```sql
INSERT INTO LoanMethods (LoanMethodId, LoanMethodDesc, LoanAmount, DueSupportAmount, LoanAmountGiven, IntDeduction, IntAddition, TotAmountCollectable, CollectionBreak, CollectionBreakString, NoOfIns, InsAmount, GracePeriod, DocChargesMethod, DocCharges, ReportIdentification, CBCount, IsVisible, ShowId, WarningDays)
VALUES (${Integer.valueOf(reset.getInt("LoanMethodId"))}, ${reset.getString("LoanMethodDesc")}, ${Double.valueOf(reset.getDouble("LoanAmount"))}, ${Double.valueOf(reset.getDouble("DueSupportAmount"))}, ${Double.valueOf(reset.getDouble("LoanAmountGiven"))}, ${Double.valueOf(reset.getDouble("IntDeduction"))}, ${Double.valueOf(reset.getDouble("IntAddition"))}, ${Double.valueOf(reset.getDouble("TotAmountCollectable"))}, ${reset.getString("CollectionBreak")}, ${reset.getString("CollectionBreakString")}, ${Double.valueOf(reset.getDouble("NoOfIns"))}, ${Double.valueOf(reset.getDouble("InsAmount"))}, ${Double.valueOf(reset.getDouble("GracePeriod"))}, ${Double.valueOf(reset.getDouble("DocChargesMethod"))}, ${Double.valueOf(reset.getDouble("DocCharges"))}, ${reset.getString("ReportIdentification")}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Double.valueOf(reset.getDouble("ShowId"))}, ${Integer.valueOf(reset.getInt("WarningDays"))})
```

#### `AsyncDownloaddata.doInBackground` · line 522
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `financemaster`
```sql
select CollectionBreakString
From financemaster a, LoanMethods b
where a.defaultloanmethodid=b.LoanMethodId and a.finid=${globalvars.finid}
```

- Reads: `CollectionBreakString`, `SubAreaNo`, `SubAreaName`, `IsVisible`, `ShowId`, `FinId`, `WeekDayId`, `Latitude`, `Longitude`

#### `AsyncDownloaddata.doInBackground` · line 539
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId,Latitude,Longitude
from subareadetails
where IsVisible=1 and FinId=${globalvars.finid}
order by SubAreaNo
```

#### `AsyncDownloaddata.doInBackground` · line 542
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId ,Latitude,Longitude
from subareadetails
where IsVisible=1 and FinId=${globalvars.finid} and WeekDayId=${globalvars.currentweekdayid}
order by SubAreaNo
```

#### `AsyncDownloaddata.doInBackground` · line 558
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `subareadetails`
```sql
INSERT INTO subareadetails (SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId, Latitude, Longitude)
VALUES (${Double.valueOf(reset.getDouble("SubAreaNo"))}, ${reset.getString("SubAreaName")}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Integer.valueOf(reset.getInt("ShowId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt("WeekDayId"))}, ${Double.valueOf(reset.getDouble("Latitude"))}, ${Double.valueOf(reset.getDouble("Longitude"))})
```

#### `AsyncDownloaddata.doInBackground` · line 568
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · APP_MCLOANCOLLECTIONDATAWITHLOANCLOSOUREMONITERING_WITHWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 572
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · APP_MCLOANCOLLECTIONDATAAUTOLOANCLOSOURE_WITHWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

- Reads: `PartyName`, `MCLoanId`, `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `SubAreaNo`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `CollectionBreakString`, `NoOfIns`, `istobeWarned`, `IsNIP`, `LoanStatus`, `PrevCollected`, `interestamount`, `LoanIndex`, `Noofloansallowed`, `Doccharges`, `IsAttentionRequired`

#### `AsyncDownloaddata.doInBackground` · line 577
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · APP_MCLOANCOLLECTIONDATAWITHLOANCLOSOUREMONITERING_WITHOUTWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 579
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · APP_MCLOANCOLLECTIONDATAAUTOLOANCLOSOURE_WITHOUTWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 631
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `MobileAppCollData`
```sql
INSERT INTO MobileAppCollData (MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, Collected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, IsMarked, PrevCollected, interestamount, PenaltyCollected, SavingsCollected, IsVisited, TargetLedgerId, LoanIndex, Noofloansallowed, Doccharges, IsAttentionRequired)
VALUES (${Integer.valueOf(reset.getInt("MCLoanId"))}, ${Integer.valueOf(reset.getInt("FWUDMCLoanId"))}, ${reset.getString("Remarks")}, ${Integer.valueOf(reset.getInt("PartyLedgerId"))}, ${reset.getString("MCLoanDate")}, ${reset.getString("ClosingDate")}, ${Integer.valueOf(reset.getInt("CWLedgerId"))}, ${Integer.valueOf(reset.getInt("UDLedgerId"))}, ${reset.getString("PartyName")}, ${Integer.valueOf(reset.getInt("SubAreaNo"))}, ${Integer.valueOf(i3)}, ${Integer.valueOf(reset.getInt("LAmount"))}, ${Integer.valueOf(reset.getInt("DuesCount"))}, ${Integer.valueOf(reset.getInt("InsAmount"))}, ${Integer.valueOf(reset.getInt("MCLoanAmountCollectable"))}, ${(Integer) 0}, ${Integer.valueOf(reset.getInt("PaidDues"))}, ${Integer.valueOf(reset.getInt("PassedDues"))}, ${reset.getString("LoanMethodDesc")}, ${reset.getString("CollectionBreakString")}, ${Integer.valueOf(reset.getInt("NoOfIns"))}, ${Integer.valueOf(reset.getInt(str7))}, ${Boolean.valueOf(reset.getBoolean("istobeWarned"))}, ${Boolean.valueOf(reset.getBoolean("IsNIP"))}, ${reset.getString("LoanStatus")}, ${bool2}, ${Integer.valueOf(reset.getInt("PrevCollected"))}, ${Integer.valueOf(reset.getInt("interestamount"))}, ${(Integer) 0}, ${(Integer) 0}, ${bool2}, ${globalvars.cashbookledgerid}, ${Integer.valueOf(reset.getInt("LoanIndex"))}, ${Integer.valueOf(reset.getInt("Noofloansallowed"))}, ${Integer.valueOf(reset.getInt("Doccharges"))}, ${Boolean.valueOf(reset.getBoolean("IsAttentionRequired"))})
```

#### `AsyncDownloaddata.doInBackground` · line 640
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `SubAreaDetails`
```sql
Update SubAreaDetails set SubAreaName ='Over all List' Where SubAreaNo=${reset.getInt("SubAreaNo")}
```

#### `AsyncDownloaddata.doInBackground` · line 647
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceMaster`
```sql
select dbo.showLedgerBalance(CashLedgerId,FinId) as  openingbalance
from FinanceMaster
where  finid=${globalvars.finid} and IsVisible='True'
order by ShowId
```

- Reads: `openingbalance`

#### `AsyncDownloaddata.doInBackground` · line 656
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `Parties`
```sql
DELETE FROM Parties WHERE LedgerId>0
```

#### `AsyncDownloaddata.doInBackground` · line 659
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`, `tblLedgers`
```sql
select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,isnull(MobileNo1,'') as MobileNo1,isnull(MobileNo2,'') as MobileNo2,SpouseName,FamilyName,lat,lng,ImageFileName,IdProofFileName,AddLine1,IdProofFileNameII ,NoOfLoansAllowed,MaximumAmountAllowed,isactive
from tblLedgers
where ScreenType=3 and  SubAreaNo in (select SubAreaNo
from subareadetails
Where FinId=${globalvars.finid})
order by SubAreaNo, UDLedgerId
```

#### `AsyncDownloaddata.doInBackground` · line 662
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`, `tblLedgers`
```sql
select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,isnull(MobileNo1,'') as MobileNo1,isnull(MobileNo2,'') as MobileNo2,SpouseName,FamilyName,lat,lng,ImageFileName,IdProofFileName,AddLine1,IdProofFileNameII ,NoOfLoansAllowed,MaximumAmountAllowed,isactive
from tblLedgers
where  isactive=1 and ScreenType=3 and  SubAreaNo in (select SubAreaNo
from subareadetails
Where FinId=${globalvars.finid})
order by SubAreaNo, UDLedgerId
```

#### `AsyncDownloaddata.doInBackground` · line 667
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GET_PARTIESLISTFORWEEKLYLINE
```sql
Get_PartiesListforweeklyline @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 670
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GET_PARTIESLISTFORWEEKLYLINEONLYACTIVEPARTIES
```sql
Get_PartiesListforweeklylineonlyactiveparties @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

- Reads: `LedgerId`, `CWLedgerId`, `LedgerName`, `Tag`, `SubAreaNo`, `UDLedgerId`, `MobileNo1`, `MobileNo2`, `SpouseName`, `FamilyName`, `lat`, `lng`, `ImageFileName`, `IdProofFileName`, `AddLine1`, `IdProofFileNameII`, `NoOfLoansAllowed`, `MaximumAmountAllowed`, `IsActive`

#### `AsyncDownloaddata.doInBackground` · line 693
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Parties`
```sql
INSERT INTO Parties (LedgerId, CWLedgerId, LedgerName, Tag, SubAreaNo, UDLedgerId, MobileNo1, MobileNo2, SpouseName, FamilyName, lat, lng, ImageFileName, IdProofFileName, AddLine1, IdProofFileNameII, NoOfLoansAllowed, MaximumAmountAllowed, IsActive)
VALUES (${Double.valueOf(reset.getDouble("LedgerId"))}, ${Double.valueOf(reset.getDouble("CWLedgerId"))}, ${reset.getString("LedgerName")}, ${reset.getString("Tag")}, ${reset.getString("SubAreaNo")}, ${reset.getString("UDLedgerId")}, ${reset.getString("MobileNo1")}, ${reset.getString("MobileNo2")}, ${reset.getString("SpouseName")}, ${reset.getString("FamilyName")}, ${reset.getString("lat")}, ${reset.getString("lng")}, ${reset.getString("ImageFileName")}, ${reset.getString("IdProofFileName")}, ${reset.getString("AddLine1")}, ${reset.getString("IdProofFileNameII")}, ${Integer.valueOf(reset.getInt("NoOfLoansAllowed"))}, ${Integer.valueOf(reset.getInt("MaximumAmountAllowed"))}, ${Integer.valueOf(reset.getInt("IsActive"))})
```

#### `AsyncDownloaddata.doInBackground` · line 696
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `PattiExpLedgers`, `tblLedgers`
```sql
select LedgerId,LedgerName
from tblLedgers
where LedgerId in (select LedgerId
from PattiExpLedgers)
order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`

#### `AsyncDownloaddata.doInBackground` · line 702
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ExpHeaders`
```sql
INSERT INTO ExpHeaders (LedgerId, LedgerName, TTId)
VALUES (${Double.valueOf(reset.getDouble("LedgerId"))}, ${reset.getString("LedgerName")}, ${(Integer) 7})
```

#### `AsyncDownloaddata.doInBackground` · line 705
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `PattiIncomeLedgers`, `tblLedgers`
```sql
select LedgerId,LedgerName
from tblLedgers
where LedgerId in (select LedgerId
from PattiIncomeLedgers)
order by LedgerName
```

- Reads: `LedgerId`, `LedgerName`, `RowNo`, `MCLoanID`, `Paid`, `CollDate`, `LoanIndex`, `MCLoanId`, `MCLoanAmountCollectable`, `collected`, `Balance`

#### `AsyncDownloaddata.doInBackground` · line 711
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ExpHeaders`
```sql
INSERT INTO ExpHeaders (LedgerId, LedgerName, TTId)
VALUES (${Double.valueOf(reset.getDouble("LedgerId"))}, ${reset.getString("LedgerName")}, ${(Integer) 8})
```

#### `AsyncDownloaddata.doInBackground` · line 718
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · PREPARELAST5COLLECTIONSFORPREV5DATES
```sql
PrepareLast5CollectionsforPrev5dates @ColDate ='${globalvars.collectiondate}' , @FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 720
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · PREPARELAST5COLLECTIONSFORPREV5WEEKS
```sql
PrepareLast5CollectionsforPrev5weeks @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 726
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · PREPARELAST5COLLECTIONS
```sql
PrepareLast5Collections @ColDate ='${globalvars.collectiondate}' , @FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 728
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · PREPARELAST5COLLECTIONSFORWEEKLYLINE_WITHAUTOLOANCLOSURE
```sql
PrepareLast5Collectionsforweeklyline_withautoloanclosure @ColDate ='${globalvars.collectiondate}' , @FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 740
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `PrevCollections`
```sql
INSERT INTO PrevCollections (RowNo, MCLoanID, Paid, CollDate)
VALUES (${Integer.valueOf(reset.getInt("RowNo"))}, ${Integer.valueOf(reset.getInt("MCLoanID"))}, ${Double.valueOf(reset.getDouble("Paid"))}, ${String.valueOf(reset.getDate("CollDate"))})
```

#### `AsyncDownloaddata.doInBackground` · line 744
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `loanindex`
```sql
Create table IF NOT EXISTS loanindex(LedgerId Int,LoanIndex Int,MCLoanId Int,MCLoanAmountCollectable Int,collected Int,Balance Int)
```

#### `AsyncDownloaddata.doInBackground` · line 745
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `loanindex`
```sql
delete from loanindex
```

#### `AsyncDownloaddata.doInBackground` · line 749
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GET_LOANINDEXWISELOANS
```sql
Get_LoanindexwiseLoans @Finid=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 751
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GET_LOANINDEXWISELOANSFORWEEKLYLINE
```sql
Get_LoanindexwiseLoansforweeklyline @ColDate ='${globalvars.collectiondate}' ,@FinId=${globalvars.finid}
```

#### `AsyncDownloaddata.doInBackground` · line 763
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `loanindex`
```sql
INSERT INTO loanindex (LedgerId, LoanIndex, MCLoanId, MCLoanAmountCollectable, collected, Balance)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${Integer.valueOf(reset.getInt("LoanIndex"))}, ${Integer.valueOf(reset.getInt("MCLoanId"))}, ${Integer.valueOf(reset.getInt("MCLoanAmountCollectable"))}, ${Integer.valueOf(reset.getInt("collected"))}, ${Integer.valueOf(reset.getInt("Balance"))})
```

#### `AsyncDownloaddata.doInBackground` · line 767
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `stockgrp`
```sql
Create table IF NOT EXISTS stockgrp(GrpId int,GrpDesc String,HSNSACCode String,IsActive Boolean,DisplayIndex int)
```

#### `AsyncDownloaddata.doInBackground` · line 768
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `stockgrp`
```sql
delete from stockgrp
```

#### `AsyncDownloaddata.doInBackground` · line 769
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `stockgrp`
```sql
Select GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex from stockgrp order by DisplayIndex
```

- Reads: `GrpId`, `GrpDesc`, `HSNSACCode`, `IsActive`, `DisplayIndex`

#### `AsyncDownloaddata.doInBackground` · line 777
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockgrp`
```sql
INSERT INTO stockgrp (GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex)
VALUES (${Double.valueOf(reset.getDouble("GrpId"))}, ${reset.getString("GrpDesc")}, ${Double.valueOf(reset.getDouble("HSNSACCode"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))}, ${Double.valueOf(reset.getDouble("DisplayIndex"))})
```

#### `AsyncDownloaddata.doInBackground` · line 779
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `stockcat`
```sql
Create table IF NOT EXISTS stockcat(CatId Double,CatDesc String,DisplayIndex Int,IsActive Boolean)
```

#### `AsyncDownloaddata.doInBackground` · line 780
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `stockcat`
```sql
delete from stockcat
```

#### `AsyncDownloaddata.doInBackground` · line 781
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `stockcat`
```sql
Select CatId, CatDesc, DisplayIndex, IsActive from stockcat order by DisplayIndex
```

- Reads: `CatId`, `CatDesc`, `DisplayIndex`, `IsActive`

#### `AsyncDownloaddata.doInBackground` · line 788
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockcat`
```sql
INSERT INTO stockcat (CatId, CatDesc, DisplayIndex, IsActive)
VALUES (${Double.valueOf(reset.getDouble("CatId"))}, ${reset.getString("CatDesc")}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

#### `AsyncDownloaddata.doInBackground` · line 790
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `stockUOM`
```sql
Create table IF NOT EXISTS stockUOM(UOMId Int,UOMDesc String,DisplayIndex Int,IsActive Boolean)
```

#### `AsyncDownloaddata.doInBackground` · line 791
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `stockUOM`
```sql
delete from stockUOM
```

#### `AsyncDownloaddata.doInBackground` · line 792
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `stockUOM`
```sql
Select UOMId, UOMDesc, DisplayIndex, IsActive from stockUOM order by DisplayIndex
```

- Reads: `UOMId`, `UOMDesc`, `DisplayIndex`, `IsActive`

#### `AsyncDownloaddata.doInBackground` · line 799
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockUOM`
```sql
INSERT INTO stockUOM (UOMId, UOMDesc, DisplayIndex, IsActive)
VALUES (${Integer.valueOf(reset.getInt("UOMId"))}, ${reset.getString("UOMDesc")}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

#### `AsyncDownloaddata.doInBackground` · line 801
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ItemMaster`
```sql
Create table IF NOT EXISTS ItemMaster(ItemCode Int,ItemName String,PriUOMid Int,Catid Int,Grpid Int,MinimumOrderIdQty Int,ReOrderLevel Int )
```

#### `AsyncDownloaddata.doInBackground` · line 802
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ItemMaster`
```sql
delete from ItemMaster
```

#### `AsyncDownloaddata.doInBackground` · line 803
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `ItemMaster`
```sql
Select ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel
from ItemMaster
order by ItemName
```

- Reads: `ItemCode`, `ItemName`, `PriUOMid`, `Catid`, `Grpid`, `MinimumOrderIdQty`, `ReOrderLevel`

#### `AsyncDownloaddata.doInBackground` · line 813
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `ItemMaster`
```sql
INSERT INTO ItemMaster (ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel)
VALUES (${Integer.valueOf(reset.getInt("ItemCode"))}, ${reset.getString("ItemName")}, ${Integer.valueOf(reset.getInt("PriUOMid"))}, ${Integer.valueOf(reset.getInt("Catid"))}, ${Integer.valueOf(reset.getInt("Grpid"))}, ${Integer.valueOf(reset.getInt("MinimumOrderIdQty"))}, ${Integer.valueOf(reset.getInt("ReOrderLevel"))})
```

#### `AsyncDownloaddata.doInBackground` · line 815
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `StockUOM`
```sql
Create table IF NOT EXISTS StockUOM(UOMId Int,UOMDesc String,DisplayIndex Int,IsActive Boolean)
```

#### `AsyncDownloaddata.doInBackground` · line 816
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `StockUOM`
```sql
delete from StockUOM
```

#### `AsyncDownloaddata.doInBackground` · line 817
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `StockUOM`
```sql
Select UOMId, UOMDesc, DisplayIndex, IsActive from StockUOM order by DisplayIndex
```

- Reads: `UOMId`, `UOMDesc`, `DisplayIndex`, `IsActive`

#### `AsyncDownloaddata.doInBackground` · line 824
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `StockUOM`
```sql
INSERT INTO StockUOM (UOMId, UOMDesc, DisplayIndex, IsActive)
VALUES (${Integer.valueOf(reset.getInt("UOMId"))}, ${reset.getString("UOMDesc")}, ${Integer.valueOf(reset.getInt("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

#### `AsyncDownloaddata.onPreExecute` · line 962
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `NewItems`
```sql
CREATE TABLE IF NOT EXISTS NewItems(SNo int,ItemCode int,Qty int,MCLoanid Double,PRate int,TotalAmt int)
```

#### `AsyncDownloaddata.onPreExecute` · line 963
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpHeaders`
```sql
CREATE TABLE IF NOT EXISTS ExpHeaders(LedgerId Double,LedgerName nvarchar,TTId int)
```

#### `AsyncDownloaddata.onPreExecute` · line 964
**local SQLite** · `sqlite.execSQL` · DROP · tables: `MobileAppCollData`
```sql
drop table MobileAppCollData
```

#### `AsyncDownloaddata.onPreExecute` · line 965
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewItems`
```sql
delete from NewItems
```

#### `AsyncDownloaddata.onPreExecute` · line 968
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `AsyncDownloaddata.onPreExecute` · line 969
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `AsyncDownloaddata.onPreExecute` · line 970
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `AsyncDownloaddata.onPreExecute` · line 971
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpensesEntries`
```sql
Create table IF NOT EXISTS ExpensesEntries(ID INTEGER PRIMARY KEY   AUTOINCREMENT,FinId Double,LedgerId Double,Amount Double,ServerGenTransId Double,TTId int,Narration varchar  )
```

#### `AsyncDownloaddata.onPreExecute` · line 972
**local SQLite** · `sqlite.execSQL` · DROP · tables: `ExpensesEntries`
```sql
drop table ExpensesEntries
```

#### `AsyncDownloaddata.onPreExecute` · line 973
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpensesEntries`
```sql
Create table IF NOT EXISTS ExpensesEntries(ID INTEGER PRIMARY KEY   AUTOINCREMENT,FinId Double,LedgerId Double,Amount Double,ServerGenTransId Double,TTId int,SourceLedgerId int,Narration varchar )
```

#### `AsyncDownloaddata.onPreExecute` · line 974
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `StaffLocationQueue`
```sql
Create table IF NOT EXISTS StaffLocationQueue(QId INTEGER PRIMARY KEY AUTOINCREMENT, FinId int, StaffLedgerId int, TrackDate varchar, Lat varchar, Lng varchar, Accuracy int, Speed int, BatteryLevel int, IsMockLocation int, DeviceTime varchar)
```

#### `AsyncDownloaddata.onPreExecute` · line 975
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

#### `AsyncDownloaddata.onPreExecute` · line 976
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `Parties`
```sql
delete from Parties
```

#### `AsyncDownloaddata.onPreExecute` · line 977
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpHeaders`
```sql
delete from ExpHeaders
```

#### `AsyncDownloaddata.onPreExecute` · line 978
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `PrevCollections`
```sql
Create table IF NOT EXISTS PrevCollections (RowNo int,MCLoanId Double, CollDate varchar,Paid Double)
```

#### `AsyncDownloaddata.onPreExecute` · line 979
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `PrevCollections`
```sql
delete from PrevCollections
```

#### `AsyncDownloaddata.onPreExecute` · line 981
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

#### `AsyncDownloaddata.onPreExecute` · line 982
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `RelatedDocCollection`
```sql
delete from RelatedDocCollection
```

## MyIntentService

Screen: **Intent service template** · [spec](../../brain/modules/03-offline-mode-download-upload-sync-services.md#intent-service-template--myintentservice) · 5 statements

#### `ASYNCSavingCollections.doInBackground` · line 94
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LastUpload`
```sql
select SubmitAmount,SubmitedAt,MCLGCollId,TransId from LastUpload
```

- Reads: `MCLGCollId`, `TransId`

#### `ASYNCSavingCollections.doInBackground` · line 104
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select x.SubAreaName,PaidDues,PassedDues,x.MCLoanId,x.FWUDMCLoanId,x.Remarks,x.PartyLedgerId,x.MCLoanDate, interestamount,x.ClosingDate,y.CWLedgerId,x.UDLedgerId,x.PartyName,y.Tag,x.SubAreaNo,x.LAmount,x.DuesCount,x.InsAmount,x.MCLoanAmountCollectable,x.Collected,x.PaidDues,x.PassedDues,x.LoanMethodDesc,x.CollectionBreakString,x.NoOfIns,x.CBCount,x.istobeWarned,x.IsNIP,x.LoanStatus,x.IsMarked,x.PrevCollected ,ImagefileName,IdProofFileName,y.lat,y.lng,x.lat as collectionlat,x.lng as collectionlng,IsVisited,PenaltyCollected , SavingsCollected,MCLDueCollId
from MobileAppCollData  x, Parties y, SubAreaDetails z
where x.PartyLedgerId=y.LedgerId and x.SubAreaNo=z.SubAreaNo and islocallyfixed='true'
order by z.ShowId,x.UDLedgerId
```

- Reads: `MCLoanId`, `Collected`, `SubAreaNo`, `InsAmount`, `PaidDues`, `PassedDues`, `collectionlat`, `collectionlng`, `LAmount`, `PartyLedgerId`, `SubAreaName`, `SavingsCollected`, `PenaltyCollected`

#### `ASYNCSavingCollections.doInBackground` · line 151
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblmcloanduecol`
```sql
{call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "MCLDueColDate" → `selectedcollection.getCollDate()` (setDate) · "MCLoanId" → `tabbedcollectionlistdataList.get(i).getMCLoanId().intValue()` (setDouble) · "TTId" → `22.0d` (setDouble) · "SourceLedgerId" → `tabbedcollectionlistdataList.get(i).getPartyLedgerId().intValue()` (setDouble) · "TargetLedgerId" → `1.0d` (setDouble) · "FinId" → `selectedcollection.getFinId()` (setDouble) · "Paid" → `tabbedcollectionlistdataList.get(i).getCollected().intValue()` (setDouble) · "GColId" → `selectedcollection.getMCLGCollId()` (setDouble) · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` (setDouble) · "SavingsCollected" → `tabbedcollectionlistdataList.get(i).getSavingsCollected().intValue()` (setDouble) · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` (setDouble) · "CashCollection" → `tabbedcollectionlistdataList.get(i).getCashCollection()` (setDouble) · "BankCollection" → `tabbedcollectionlistdataList.get(i).getBankCollection()` (setDouble) · "lng" → `tabbedcollectionlistdataList.get(i).getCollectionlng()` (setString) · "lat" → `tabbedcollectionlistdataList.get(i).getCollectionlat()` (setString)
- OUT: "MCLDueCollId" (8)
- Reads: `MCLDueCollId`

#### `ASYNCSavingCollections.doInBackground` · line 172
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set MCLDueCollId=${tabbedcollectionlistdataList.get(i).getMCLDueCollId()}
where  MCLoanId= ${tabbedcollectionlistdataList.get(i).getMCLoanId()}
```

#### `ASYNCSavingCollections.doInBackground` · line 178
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoanGroupCollectionMaster`
```sql
Update tblMCLoanGroupCollectionMaster set CollAmount=?,NIPAmountCollected=?, PenaltyCollected=?,SavingsCollected=?
where MCLGCollId=? and TransId =?
```

- Params: 1 → `totalcollection` (setDouble) · 2 → `0.0d` (setDouble) · 3 → `totalpenalty` (setDouble) · 4 → `totalsavings` (setDouble) · 5 → `selectedcollection.getMCLGCollId()` (setInt) · 6 → `selectedcollection.getTransId()` (setInt)

## MyService_updatecollection

Screen: **Background collection sync** · [spec](../../brain/modules/03-offline-mode-download-upload-sync-services.md#background-collection-sync--myserviceupdatecollection) · 7 statements

#### `ASYNCSavingCollections.doInBackground` · line 107
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LastUpload`
```sql
select SubmitAmount,SubmitedAt,MCLGCollId,TransId from LastUpload
```

- Reads: `MCLGCollId`, `TransId`

#### `ASYNCSavingCollections.doInBackground` · line 117
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select x.SubAreaName,PaidDues,PassedDues,x.MCLoanId,x.FWUDMCLoanId,x.Remarks,x.PartyLedgerId,x.MCLoanDate, interestamount,x.ClosingDate,y.CWLedgerId,x.UDLedgerId,x.PartyName,y.Tag,x.SubAreaNo,x.LAmount,x.DuesCount,x.InsAmount,x.MCLoanAmountCollectable,x.Collected,x.PaidDues,x.PassedDues,x.LoanMethodDesc,x.CollectionBreakString,x.NoOfIns,x.CBCount,x.istobeWarned,x.IsNIP,x.LoanStatus,x.IsMarked,x.PrevCollected ,ImagefileName,IdProofFileName,y.lat,y.lng,x.lat as collectionlat,x.lng as collectionlng,IsVisited,PenaltyCollected , SavingsCollected,MCLDueCollId,CashCollection,BankCollection
from MobileAppCollData  x, Parties y, SubAreaDetails z
where x.PartyLedgerId=y.LedgerId and x.SubAreaNo=z.SubAreaNo and islocallyfixed='true'  and MCLDueCollId  is Null
order by z.ShowId,x.UDLedgerId
```

- Reads: `MCLoanId`, `Collected`, `SubAreaNo`, `InsAmount`, `PaidDues`, `PassedDues`, `collectionlat`, `collectionlng`, `LAmount`, `PartyLedgerId`, `SubAreaName`, `CashCollection`, `BankCollection`

#### `ASYNCSavingCollections.doInBackground` · line 153
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblmcloanduecol`
```sql
{call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "MCLDueColDate" → `selectedcollection.getCollDate()` (setDate) · str2 → `tabbedcollectionlistdataList.get(i).getMCLoanId().intValue()` (setDouble) · "TTId" → `22.0d` (setDouble) · "SourceLedgerId" → `tabbedcollectionlistdataList.get(i).getPartyLedgerId().intValue()` (setDouble) · "TargetLedgerId" → `1.0d` (setDouble) · "FinId" → `selectedcollection.getFinId()` (setDouble) · "Paid" → `tabbedcollectionlistdataList.get(i).getCollected().intValue()` (setDouble) · "GColId" → `selectedcollection.getMCLGCollId()` (setDouble) · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` (setDouble) · "SavingsCollected" → `tabbedcollectionlistdataList.get(i).getSavingsCollected().intValue()` (setDouble) · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i).getPenaltyCollected().intValue()` (setDouble) · "CashCollection" → `tabbedcollectionlistdataList.get(i).getCashCollection()` (setDouble) · "BankCollection" → `tabbedcollectionlistdataList.get(i).getBankCollection()` (setDouble) · "lng" → `tabbedcollectionlistdataList.get(i).getCollectionlng()` (setString) · "lat" → `tabbedcollectionlistdataList.get(i).getCollectionlat()` (setString)
- OUT: "MCLDueCollId" (8)
- Reads: `MCLDueCollId`

#### `ASYNCSavingCollections.doInBackground` · line 175
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set MCLDueCollId=${tabbedcollectionlistdataList.get(i).getMCLDueCollId()}
where  MCLoanId= ${tabbedcollectionlistdataList.get(i).getMCLoanId()}
```

#### `ASYNCSavingCollections.doInBackground` · line 184
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select sum(Collected) as Collected, sum(PenaltyCollected) as PenaltyCollected , sum(SavingsCollected) as SavingsCollected
from MobileAppCollData  x
where   MCLDueCollId>0
```

- Reads: `SavingsCollected`, `PenaltyCollected`

#### `ASYNCSavingCollections.doInBackground` · line 196
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoanGroupCollectionMaster`
```sql
Update tblMCLoanGroupCollectionMaster set CollAmount=?,NIPAmountCollected=?, PenaltyCollected=?,SavingsCollected=?
where MCLGCollId=? and TransId =?
```

- Params: 1 → `totalcollection` (setDouble) · 2 → `0.0d` (setDouble) · 3 → `totalpenalty` (setDouble) · 4 → `totalsavings` (setDouble) · 5 → `selectedcollection.getMCLGCollId()` (setInt) · 6 → `selectedcollection.getTransId()` (setInt)

#### `TimeDisplayTimerTask.lambda$run$0` · line 64
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select x.SubAreaName,PaidDues,PassedDues,x.MCLoanId,x.FWUDMCLoanId,x.Remarks,x.PartyLedgerId,x.MCLoanDate, interestamount,x.ClosingDate,y.CWLedgerId,x.UDLedgerId,x.PartyName,y.Tag,x.SubAreaNo,x.LAmount,x.DuesCount,x.InsAmount,x.MCLoanAmountCollectable,x.Collected,x.PaidDues,x.PassedDues,x.LoanMethodDesc,x.CollectionBreakString,x.NoOfIns,x.CBCount,x.istobeWarned,x.IsNIP,x.LoanStatus,x.IsMarked,x.PrevCollected ,ImagefileName,IdProofFileName,y.lat,y.lng,x.lat as collectionlat,x.lng as collectionlng,IsVisited,PenaltyCollected , SavingsCollected,MCLDueCollId
from MobileAppCollData  x, Parties y, SubAreaDetails z
where x.PartyLedgerId=y.LedgerId and x.SubAreaNo=z.SubAreaNo and islocallyfixed='true'  and MCLDueCollId  is Null
order by z.ShowId,x.UDLedgerId
```

## newtodaycollectionofflinedata_list

Screen: **Today Collection (offline list)** · [spec](../../brain/modules/03-offline-mode-download-upload-sync-services.md#today-collection-offline-list--newtodaycollectionofflinedatalist) · 1 statements

#### `fetchdata_fromnewpartiestable` · line 91
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
Select MCLoanId,FWUDMCLoanId,  CWLedgerId, PartyLedgerId,MCLoanDate, PartyName,  b.SubAreaName,LAmount,Collected ,CashCollection ,BankCollection
from MobileAppCollData a, SubAreaDetails b
where a.SubAreaNo=b.SubAreaNo and Collected>0
```

- Reads: `MCLoanId`, `FWUDMCLoanId`, `CWLedgerId`, `PartyLedgerId`, `MCLoanDate`, `PartyName`, `SubAreaName`, `LAmount`, `Collected`, `CashCollection`, `BankCollection`

## uploadcollection

Screen: **Upload Data** · [spec](../../brain/modules/03-offline-mode-download-upload-sync-services.md#upload-data--uploadcollection) · 110 statements

#### `ShowLastUpdate` · line 643
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LastUpload`
```sql
select SubmitAmount,SubmitedAt,MCLGCollId,TransId from LastUpload
```

- Reads: `MCLGCollId`, `TransId`

#### `createPdf` · line 2470
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select SubAreaName ,Ledgername as PartyName,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,ServerGenMCLoanId,a.ServerGenLedgerId,Remarks,a.ServerGenTransId
From NewMCLoans a,NewParties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
union all select SubAreaName ,Ledgername as PartyName,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,ServerGenMCLoanId,a.ServerGenLedgerId,Remarks,a.ServerGenTransId
From NewMCLoans a, Parties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
```

#### `createPdf` · line 2479
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select b.SubAreaName,PartyName,LAmount,Collected,PrevCollected,MCLoanDate, CWLedgerId
From MobileAppCollData a,SubAreaDetails b
where a.subareano=b.subareano
order by b.ShowId,a.UDLedgerId
```

#### `createPdf` · line 2492
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesEntries`
```sql
select LedgerName,narration,case when a.ttid=8 then Amount Else Null end as AmountIn, case when a.ttid=7 then Amount else Null end as AmountOut
from ExpensesEntries a,ExpHeaders b
where a.LedgerId= b.LedgerId
order by ID
```

#### `lambda$onCreate$1` · line 600
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select PartyName,a.LoanIndex
From  NewMCLoans a , MobileAppCollData b,Parties c
where a.PartyLedgerId=b.PartyLedgerId and a.PartyLedgerId =c.LedgerId and  a.LoanIndex=b.LoanIndex  and b.MCLoanAmountCollectable-PrevCollected-Collected>0
```

- Reads: `LoanIndex`, `PartyName`

#### `onCreate` · line 255
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where LedgerId=${globalvars.currentcollectionexecutivelederid1}
```

- Reads: `LedgerName`

#### `onCreate` · line 265
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where LedgerId=${globalvars.currentcollectionexecutivelederid2}
```

- Reads: `LedgerName`

#### `onCreate` · line 293
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `NewMCLoans`
```sql
select  sum(Collected)  as Received
from MobileAppCollData
where MCLoanId not in (select ServerGenMCLoanId
from NewMCLoans)
```

- Reads: `Received`

#### `onCreate` · line 305
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select  sum(BankCollection)  as BankCollectionTotal,  sum(CashCollection)  as CashCollectionTotal
from MobileAppCollData
```

- Reads: `CashCollectionTotal`, `BankCollectionTotal`

#### `onCreate` · line 313
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select sum(ondaycollection) as ondaycollectiontotal from NewMCLoans
```

#### `onCreate` · line 320
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select sum(SavingsCollected) as Received from MobileAppCollData
```

#### `onCreate` · line 338
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select sum(PenaltyCollected) as Received from MobileAppCollData
```

#### `onCreate` · line 354
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesEntries`
```sql
select case when Narration is null or length(Narration)=0 then LedgerName else Narration End as Particulars,Null as Debit ,Amount as Credit
from ExpensesEntries a ,  ExpHeaders b
where a.LedgerId=b.LedgerId  and a.ttid=7
```

- Reads: `Particulars`, `Debit`, `Credit`

#### `onCreate` · line 376
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesEntries`
```sql
select case when Narration is null or length(Narration)=0 then LedgerName else Narration End as Particulars,Amount as Debit ,Null as Credit
from ExpensesEntries a ,  ExpHeaders b
where a.LedgerId=b.LedgerId  and a.ttid=8 and b.ttid=8
```

- Reads: `Particulars`, `Debit`, `Credit`

#### `onCreate` · line 389
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select c.SubAreaName as AreaName,Ledgername as PartyName,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,ServerGenMCLoanId,b.ServerGenLedgerId,Remarks,ServerGenTransId,ondaycollection
From NewMCLoans a,NewParties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
```

- Reads: `PartyName`, `AreaName`, `MCLoanAmountCollectable`, `InterestAmount`, `DocCharges`, `ondaycollection`

#### `onCreate` · line 467, 562
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select sum(MCLoanAmountCollectable) as Received from NewMCLoans
```

#### `onCreate` · line 477, 569
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesEntries`
```sql
select sum(Amount) as Received from ExpensesEntries where ttid=7
```

#### `onCreate` · line 486, 575
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesEntries`
```sql
select sum(Amount) as Received from ExpensesEntries where ttid=8
```

#### `onCreate` · line 504
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select c.SubAreaName as AreaName,Ledgername as PartyName,ondaycollection,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,0 as ServerGenMCLoanId, 0 as ServerGenLedgerId,Remarks,0 as ServerGenTransId
From NewMCLoans a,Parties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
```

- Reads: `MCLoanAmountCollectable`, `InterestAmount`, `DocCharges`, `ondaycollection`

#### `printDocument` · line 2371
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select SubAreaName,PartyName,LAmount,Collected,PrevCollected,MCLoanDate
From MobileAppCollData a,SubAreaDetails b
where a.subareano=b.subareano
order by b.ShowId,a.UDLedgerId
```

#### `ASYNCUpload.doInBackground` · line 749
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceAccessAllowed`
```sql
select top 1 x.IMEI,convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(), '+05:30'),0 ) as DownloadTime ,'True' as 'IsAllowed'
from FinanceAccessAllowed x , MobileDevlicesAllowed y
where x.IMEI=y.IMEI and x.FinId=${globalvars.finid} and y.isActive =1 and x.IMEI='${globalvars.device_id}'
```

- Reads: `isAllowed`

#### `ASYNCUpload.doInBackground` · line 755
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `FinanceAccessAllowed`, `softsettings`, `UploadControling`
```sql
select top 1 x.IMEI,convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(), '+05:30'),0 ) as UploadTime ,case when (select bitvalue
from softsettings
where SSId=32)='True' and isAllowed is not null and isAllowed='True' then 'True' when (select bitvalue
from softsettings
where SSId=32)='False' then 'True' else 'False' end  as 'IsAllowed'
from FinanceAccessAllowed x
left
join UploadControling z on x.FinId=z.FinId and TransDate='${globalvars.collectiondate}',MobileDevlicesAllowed y
where x.IMEI=y.IMEI and x.FinId=${globalvars.finid} and y.isActive =1 and x.IMEI='${globalvars.device_id}'
```

- Reads: `isAllowed`

#### `ASYNCUpload.doInBackground` · line 766
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `checkisanyoneofcashandorbooksverified`
```sql
exec checkisanyoneofcashandorbooksverified @FinId=${globalvars.finid} ,  @VerfiedDate='${globalvars.collectiondate}'
```

- Reads: `verifiedCount`

#### `ASYNCUpload.doInBackground` · line 786
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewParties`
```sql
select  LedgerId ,CWLedgerId ,LedgerName,Tag ,SubAreaNo ,UDLedgerId,MobileNo1 ,MobileNo2 ,ServerGenLedgerId,AddLine1,SpouseName,FamilyName,ImagefileName,IdProoffileName,IdProofFileNameII ,lat,lng,NoOfLoansAllowed,MaximumAmountAllowed
from NewParties
order by LedgerId
```

- Reads: `LedgerId`, `CWLedgerId`, `LedgerName`, `Tag`, `SubAreaNo`, `MobileNo1`, `MobileNo2`, `AddLine1`, `SpouseName`, `FamilyName`, `ImagefileName`, `IdProoffileName`, `IdProofFileNameII`, `lat`, `lng`, `NoOfLoansAllowed`, `MaximumAmountAllowed`, `ServerGenLedgerId`

#### `ASYNCUpload.doInBackground` · line 811
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertnewparties`
```sql
{call dbo.InsertNewParties(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

#### `ASYNCUpload.doInBackground` · line 811
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertnewpartieswithautocwledgerid`
```sql
{call dbo.InsertNewPartieswithautocwledgerid(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

#### `ASYNCUpload.doInBackground` · line 825, 989, 1057
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select  MCLoanId  ,FinId  ,PartyLedgerId  ,MCLoanAmount  ,TotalMonths   ,DocCharges  ,LoanMethodId  ,InterestAmount  ,MCLoanAmountCollectable  ,InsAmount  ,MCLoanAmountEstimated,SubAreaNo,ServerGenMCLoanId,ServerGenLedgerId,ServerGenTransId,Remarks ,WeekdayId,MonthdayId,mobileno ,MCLoanDate,Sourceledgerid,PartyReferenced,LoanIndex,ProductValue,AdvanceAmount,IntRate
from NewMCLoans
order by MCLoanId
```

#### `ASYNCUpload.doInBackground` · line 830, 994, 1232
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select  MCLoanId  ,FinId  ,PartyLedgerId  ,MCLoanAmount  ,TotalMonths   ,DocCharges,Sourceledgerid  ,LoanMethodId  ,InterestAmount  ,MCLoanAmountCollectable  ,InsAmount  ,MCLoanAmountEstimated,SubAreaNo,ServerGenMCLoanId,Remarks ,ServerGenLedgerId,ServerGenTransId,WeekDayId,MonthDayId,OndayCollection
from NewMCLoans
order by MCLoanId
```

#### `ASYNCUpload.doInBackground` · line 846, 1010, 1316, 1356, 1391, 1417, 1465
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select sum(Collected)  as Collected,sum(CashCollection)  as CashCollection,sum(BankCollection)  as BankCollection,  sum(PenaltyCollected) as PenaltyCollected,sum(SavingsCollected) as  SavingsCollected
from MobileAppCollData
```

#### `ASYNCUpload.doInBackground` · line 853, 1095, 1476
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsmaster`
```sql
{call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `String.valueOf(globalvars.collectiondate)` (setString) · "TransType" → `constantvalues.MCLoanCollectionTTId.intValue()` (setDouble) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setInt) · "TransAmount" → `PenaltyCollectedTotal + SavingsCollectedTotal + DueCollectionTotal` (setDouble) · "FWTransId" → `8` (setNull) · "FWVWUDTransId" → `8` (setNull) · str14 → `8` (setNull) · "TransLedgerId" → `8` (setNull) · "RouteId" → `globalvars.finid.intValue()` (setDouble)
- OUT: "TransId" (8) · "FWVWTransId" (8)
- Reads: `TransId`

#### `ASYNCUpload.doInBackground` · line 874, 1492
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblmcloangroupcollectionmasterwithtwostaffs`
```sql
{call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "CollDate" → `globalvars.collectiondate` (setDate) · "CollAmount" → `DueCollectionTotal` (setDouble) · "PenaltyCollected" → `PenaltyCollectedTotal` (setDouble) · "SavingsCollected" → `SavingsCollectedTotal` (setDouble) · "CollStaffLedgerId" → `globalvars.currentcollectionexecutivelederid1.intValue()` (setInt) · "CollStaff2LedgerId" → `globalvars.currentcollectionexecutivelederid2.intValue()` (setInt) · "TransId" → `CollTransId.doubleValue()` (setDouble) · "NIPAmountCollected" → `0.0d` (setDouble) · "FinId" → `globalvars.finid.intValue()` (setDouble) · str12 → `BankCollectionTotal` (setInt) · str13 → `CashCollectionTotal` (setInt)
- OUT: "MCLGCollId" (3) · "FWGColId" (3)
- Reads: `MCLGCollId`

#### `ASYNCUpload.doInBackground` · line 901, 950, 1017, 1328, 1336, 1363, 1398, 1424, 1583, 1602, 1657
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `ExpensesEntries`
```sql
select ID, LedgerId,FinId ,Amount ,ServerGenTransId,TTId,Narration,SourceLedgerId
from ExpensesEntries
order by ID
```

#### `ASYNCUpload.doInBackground` · line 936, 1637
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT
```sql
select  convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(), '+05:30'),0 ) as DownloadTime
```

- Reads: `DownloadTime`

#### `ASYNCUpload.doInBackground` · line 944, 1646
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `ASYNCUpload.doInBackground` · line 945
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `LastUpload`
```sql
INSERT INTO LastUpload(MCLGCollId ,FWGColId,TransId,SubmitAmount,SubmitedAt)
VALUES(${cstmt3.getInt("MCLGCollId")},${FWGColId},${cstmt2.getDouble("TransId")},00,'${LastSubmittedDateandTime}' )
```

#### `ASYNCUpload.doInBackground` · line 980
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `NewParties`
```sql
update NewParties set ServerGenLedgerId=${curparty.getServerGenLedgerId()}
where LedgerId=${curparty.getLedgerId()}
```

#### `ASYNCUpload.doInBackground` · line 981
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `NewMCLoans`
```sql
update NewMCLoans set ServerGenLedgerId=${curparty.getServerGenLedgerId()}
where PartyLedgerId=${curparty.getLedgerId()}
```

#### `ASYNCUpload.doInBackground` · line 1027
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblLedgers`
```sql
update tblLedgers set CWLedgerId=?,LedgerName=?,Tag=?,MobileNo1=?,MobileNo2=?,AddLine1=?,SpouseName=?, FamilyName=?,ImagefileName=?,IdProoffileName=?, lat=?,  lng=?,  IdProofFileNameII=?,NoOfLoansAllowed=?,MaximumAmountAllowed=?
where LedgerId=?
```

- Params: 1 → `curparty.getCWLedgerId().intValue()` (setDouble) · 2 → `curparty.getLedgerName()` (setString) · 3 → `curparty.getTag()` (setString) · 4 → `curparty.getMobileNo1()` (setString) · 5 → `curparty.getMobileNo2()` (setString) · 6 → `curparty.getAddLine1()` (setString) · 7 → `curparty.getSpouseName()` (setString) · 8 → `curparty.getFamilyName()` (setString) · 9 → `curparty.getImagefilename()` (setString) · 10 → `curparty.getIdProofFileName()` (setString) · 11 → `curparty.getLat()` (setString) · 12 → `curparty.getLng()` (setString) · 13 → `curparty.getIdProofImageIIfileName()` (setString) · 14 → `curparty.getNoOfLoansAllowed()` (setInt) · 15 → `curparty.getMaximumAmountAllowed()` (setInt) · 16 → `curparty.getServerGenLedgerId()` (setInt)

#### `ASYNCUpload.doInBackground` · line 1086
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoans`
```sql
delete from tblMCLoans where TransId=?
```

- Params: 1 → `newloan2.getServerGenTransId()` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1092
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · UPDATE · tables: `tblledgers`
```sql
update tblledgers set MobileNo1='${newloan2.getMobileno()}'
where ledgerid= ${newloan2.getServerGenLedgerId()}
```

#### `ASYNCUpload.doInBackground` · line 1110
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `NewMCLoans`
```sql
update NewMCLoans set ServerGenTransId =${newloan2.getServerGenTransId()}
where MCLoanId=${newloan2.getMCLoanId()}
```

#### `ASYNCUpload.doInBackground` · line 1115
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblmcloanswithweekdayid`
```sql
{call dbo.InserttblMCLoanswithWeekdayId(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ,?,?,?,?,?,?)}
```

- Params: "MCLoanDate" → `newloan2.getMCLoanDate()` (setDate) · "FWMCLoanId" → `4` (setNull) · "FWUDMCLoanId" → `4` (setNull) · "FinId" → `globalvars.finid.intValue()` (setDouble) · "PartyLedgerId" → `newloan2.getServerGenLedgerId()` (setDouble) · "AgentLedgerId" → `4` (setNull) · "GuaranderLedgerID" → `4` (setNull) · "RecPartnerLedgerID" → `4` (setNull) · "MCLoanAmount" → `newloan2.getMCLoanAmount()` (setDouble) · "TotalMonths" → `newloan2.getTotalMonths()` (setDouble) · "IntRate" → `newloan2.getIntrate()` (setDouble) · "LoanType" → `0.0d` (setDouble) · "CollectionLineId" → `0.0d` (setDouble) · "TransId" → `newloan2.getServerGenTransId()` (setDouble) · "DCPercent" → `0.0d` (setDouble) · "DocCharges" → `newloan2.getDocCharges()` (setDouble) · "LoanMethodId" → `newloan2.getLoanMethodId()` (setDouble) · "InterestAmount" → `newloan2.getInterestAmount()` (setDouble) · "MCLoanAmountCollectable" → `newloan2.getMCLoanAmountCollectable()` (setDouble) · "InsAmount" → `newloan2.getInsAmount()` (setDouble) · "MCLoanAmountEstimated" → `newloan2.getMCLoanAmountCollectable()` (setDouble) · "RecPartyLedgerId" → `4` (setNull) · "Remarks" → `newloan2.getRemarks()` (setString) · "WeekDayId" → `newloan2.getWeekDayId()` (setInt) · "MonthDayId" → `newloan2.getMonthDayid()` (setInt) · "mobileno" → `newloan2.getMobileno()` (setString) · "Sourceledgerid" → `newloan2.getSourceledgerid()` (setInt) · "PartyReferenced" → `newloan2.getPartyreference()` (setString) · "loanindex" → `newloan2.getLoanindex()` (setInt) · "Productvalue" → `newloan2.getProductsvalue()` (setInt) · "AdvanceAmount" → `newloan2.getAdvanceAmount()` (setInt)
- OUT: "MCLoanId" (8)
- Reads: `MCLoanId`

#### `ASYNCUpload.doInBackground` · line 1150
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `NewMCLoans`
```sql
update NewMCLoans set ServerGenMCLoanId=${newloan2.getServerGenMCLoanId()}
where MCLoanId=${newloan2.getMCLoanId()}
```

#### `ASYNCUpload.doInBackground` · line 1151
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewItems`
```sql
Select SNo, MCLoanId, Qty, ItemCode,PRate,TotalAmt
from NewItems
where  MCLoanId=${newloan2.getMCLoanId()}
order by SNo
```

- Reads: `SNo`, `ItemCode`, `Qty`, `PRate`, `TotalAmt`

#### `ASYNCUpload.doInBackground` · line 1153, 1205
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `InvTransDetails`
```sql
INSERT INTO InvTransDetails(SNo ,ItemCode , BilledQty , TransId,Rate,Total)values( ?,?,?,?,?,? )
```

- Params: 1 → `productC.getInt(productC.getColumnIndexOrThrow("SNo"))` (setDouble) · 2 → `productC.getDouble(productC.getColumnIndexOrThrow("ItemCode"))` (setDouble) · 3 → `productC.getInt(productC.getColumnIndexOrThrow("Qty")) * (-1)` (setDouble) · 4 → `r28.getServerGenTransId()` (setDouble) · 5 → `productC.getInt(productC.getColumnIndexOrThrow("PRate"))` (setDouble) · 6 → `productC.getInt(productC.getColumnIndexOrThrow("TotalAmt"))` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1166, 1215
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `RelatedDocCollection`
```sql
select  DCUId,ServerGenMCLoanId,MCLoanId,DUID,Remarks,ImagePath  From RelatedDocCollection WHERE MCLoanId = ?
```

- Selection args: `String.valueOf(newloan.getMCLoanId())`
- Reads: `DCUId`

#### `ASYNCUpload.doInBackground` · line 1168, 1217
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `RelatedDocCollection`
```sql
Update RelatedDocCollection set MCLoanId=? where DCUId=?
```

- Params: 1 → `newloan.getServerGenMCLoanId()` (setDouble) · 2 → `DOCc.getInt(DOCc.getColumnIndexOrThrow("DCUId"))` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1177
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set TransAmount=${newloan.getMCLoanAmount()},Transdate= '${newloan.getMCLoanDate()}'
where TransId=${newloan.getServerGenTransId()}
```

#### `ASYNCUpload.doInBackground` · line 1179
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · UPDATE · tables: `tblledgers`
```sql
update tblledgers set MobileNo1='${newloan.getMobileno()}' where ledgerid=${newloan.getServerGenLedgerId()}
```

#### `ASYNCUpload.doInBackground` · line 1181
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoans`
```sql
update tblMCLoans set MCLoanAmount=?,TotalMonths=?,DocCharges=?,LoanMethodId=?,InterestAmount=?,MCLoanAmountCollectable=?,InsAmount=?, MCLoanAmountEstimated=?,Remarks=?,Sourceledgerid=?,PartyReferenced=?,loanindex=?,MCloandate=?,AdvanceAmount=?,Productvalue=?,IntRate=?
where TransId=?
```

- Params: 1 → `newloan.getMCLoanAmount()` (setDouble) · 2 → `newloan.getTotalMonths()` (setDouble) · 3 → `newloan.getDocCharges()` (setDouble) · 4 → `newloan.getLoanMethodId()` (setDouble) · 5 → `newloan.getInterestAmount()` (setDouble) · 6 → `newloan.getMCLoanAmountCollectable()` (setDouble) · 7 → `newloan.getInsAmount()` (setDouble) · 8 → `newloan.getMCLoanAmountEstimated()` (setDouble) · 9 → `newloan.getRemarks()` (setString) · 10 → `newloan.getSourceledgerid()` (setInt) · 11 → `newloan.getPartyreference()` (setString) · 12 → `newloan.getLoanindex()` (setInt) · 13 → `newloan.getMCLoanDate()` (setDate) · 14 → `newloan.getAdvanceAmount()` (setInt) · 15 → `newloan.getProductsvalue()` (setInt) · 16 → `newloan.getIntrate()` (setDouble) · 17 → `newloan.getServerGenTransId()` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1200
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `InvTransDetails`
```sql
delete from InvTransDetails where TransId=?
```

- Params: 1 → `newloan.getServerGenTransId()` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1203
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewItems`
```sql
Select SNo, MCLoanId, Qty, ItemCode,PRate,TotalAmt
from NewItems
where  MCLoanId=${newloan.getMCLoanId()}
order by SNo
```

- Reads: `SNo`, `ItemCode`, `Qty`, `PRate`, `TotalAmt`

#### `ASYNCUpload.doInBackground` · line 1228
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ?
```sql
${(SQLString + " union all") + " Select " + newloan.getServerGenTransId() + ",5 as SNo,"}${newloan.getSourceledgerid()},'Target',${newloan.getDocCharges()},'Dr',6
union all  Select ${newloan.getServerGenTransId()},6 as SNo,104,'Source',${newloan.getDocCharges()},'Cr',5
```

#### `ASYNCUpload.doInBackground` · line 1241
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select MCLoanId ,Collected,PartyLedgerId
from MobileAppCollData
where MCLoanId= ${c5.getDouble(c5.getColumnIndexOrThrow("ServerGenMCLoanId"))}
```

#### `ASYNCUpload.doInBackground` · line 1256
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set Collected=${c5.getDouble(c5.getColumnIndexOrThrow("ondaycollection"))},BankCollection=0,   CashCollection=${c5.getDouble(c5.getColumnIndexOrThrow("ondaycollection"))}  , PartyLedgerId=${c5.getDouble(c5.getColumnIndexOrThrow("ServerGenLedgerId"))}
where MCLoanId=${collection.getDouble(collection.getColumnIndexOrThrow(str18))}
```

#### `ASYNCUpload.doInBackground` · line 1258
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set Collected=${c5.getDouble(c5.getColumnIndexOrThrow("ondaycollection"))},CashCollection=0,   BankCollection=${c5.getDouble(c5.getColumnIndexOrThrow("ondaycollection"))}  , PartyLedgerId=${c5.getDouble(c5.getColumnIndexOrThrow("ServerGenLedgerId"))}
where MCLoanId=${collection.getDouble(collection.getColumnIndexOrThrow(str18))}
```

#### `ASYNCUpload.doInBackground` · line 1381
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `MobileAppCollData`
```sql
INSERT INTO MobileAppCollData (MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, Collected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, IsMarked, PrevCollected, IsVisited, CashCollection, BankCollection, CashCollection, BankCollection)
VALUES (${ServerGenMCLoanId}, ${ServerGenMCLoanId}, , ${Double.valueOf(PartyLedgerId)}, ${String.valueOf(globalvars.collectiondate)}, ${String.valueOf(globalvars.collectiondate)}, ${Double.valueOf(PartyLedgerId)}, ${Double.valueOf(PartyLedgerId)}, PartyName, ${num3}, ${num4}, ${num4}, ${num4}, ${num3}, ${Double.valueOf(ondaycollection)}, ${num3}, ${num3}, LoanMethodDesc, CollectionBreakString, ${num3}, ${num3}, ${bool3}, ${bool3}, ${num3}, ${bool3}, ${num3}, ${bool3}, ${Double.valueOf(ondaycollection)}, ${num3}, ${num3}, ${Double.valueOf(ondaycollection)})
```

#### `ASYNCUpload.doInBackground` · line 1513
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoanGroupCollectionMaster`
```sql
update tblMCLoanGroupCollectionMaster set CollDate=?,CollAmount=?,CollStaffLedgerId=?,NIPAmountCollected=?,CollStaff2LedgerId=?,PenaltyCollected=?,SavingsCollected=?,CashCollection=?,BankCollection=?
where MCLGCollId=?
```

- Params: 1 → `globalvars.collectiondate` (setDate) · 2 → `DueCollectionTotal` (setDouble) · 3 → `globalvars.currentcollectionexecutivelederid1.intValue()` (setDouble) · 4 → `0.0d` (setDouble) · 5 → `globalvars.currentcollectionexecutivelederid2.intValue()` (setDouble) · 6 → `PenaltyCollectedTotal` (setDouble) · 7 → `SavingsCollectedTotal` (setDouble) · 8 → `CashCollectionTotal` (setInt) · 9 → `BankCollectionTotal` (setInt) · 10 → `MCLGCollId` (setInt)

#### `ASYNCUpload.doInBackground` · line 1525
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsmaster`
```sql
Update tblTransactionsmaster set TransAmount=? where TransId=?
```

- Params: 1 → `BankCollectionTotal + CashCollectionTotal` (setDouble) · 2 → `CollTransId.doubleValue()` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1529
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoanDueCol`
```sql
delete from tblMCLoanDueCol where GColId=?
```

- Params: 1 → `MCLGCollId` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1532
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete from tblTransactionsDetails where TransId=?
```

- Params: 1 → `CollTransId.doubleValue()` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1536
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `PartyVisitStatus`
```sql
delete from PartyVisitStatus where MCLGCollId=?
```

- Params: 1 → `MCLGCollId` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1552
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select MCLoanId ,Collected,PartyLedgerId,isVisited,PenaltyCollected , SavingsCollected ,CashCollection,BankCollection
from MobileAppCollData
order by SubAreaNo,UDLedgerId
```

#### `ASYNCUpload.doInBackground` · line 1552
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select MCLoanId ,Collected,PartyLedgerId,isVisited ,PenaltyCollected , SavingsCollected,CashCollection,BankCollection
from MobileAppCollData
where (Collected>0 or SavingsCollected>0 or PenaltyCollected>0 )
order by SubAreaNo,UDLedgerId
```

#### `ASYNCUpload.doInBackground` · line 1615
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `tblMCLoanDueCol`
```sql
);${c3.getInt(c3.getColumnIndexOrThrow(str13))},${c3.getInt(c3.getColumnIndexOrThrow(str12))},${c3.getInt(c3.getColumnIndexOrThrow("SavingsCollected"))},${c3.getInt(c3.getColumnIndexOrThrow("PenaltyCollected"))} ,${c3.getInt(c3.getColumnIndexOrThrow("Collected"))},${globalvars.finid},22 ,${c3.getDouble(c3.getColumnIndexOrThrow(str21))}',${globalvars.collectiondate},'${cstmt32.getInt("MCLGCollId")}insert into tblMCLoanDueCol (GColId,MCLDueColDate,MCLoanId,TTId,FinId,Paid,PenaltyCollected,SavingsCollected,BankCollection,CashCollection)
values (
```

#### `ASYNCUpload.doInBackground` · line 1619
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `PartyVisitStatus`
```sql
');${c3.getString(c3.getColumnIndexOrThrow("IsVisited")).equals("true")},'${c3.getDouble(c3.getColumnIndexOrThrow(str21))},${c3.getDouble(c3.getColumnIndexOrThrow("PartyLedgerId"))},${cstmt32.getInt("MCLGCollId")}insert into PartyVisitStatus(MCLGCollId,PartyLedgerId,MCLoanId,IsVisited)
values (
```

#### `ASYNCUpload.doInBackground` · line 1635
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete
from tblTransactionsDetails
where TransId=${cstmt22.getDouble("TransId")};,'Source','Cr',1,2);${c2.getInt(c2.getColumnIndexOrThrow(str13))},114,${cstmt22.getDouble("TransId")} Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(,'Target','Dr',2,1);${c2.getInt(c2.getColumnIndexOrThrow(str13))},${globalvars.cashbookledgerid},${cstmt22.getDouble("TransId")}Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(,'Source','Cr',3,4);${c2.getInt(c2.getColumnIndexOrThrow(str12))},114,${cstmt22.getDouble("TransId")} Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(,'Target','Dr',4,3);${c2.getInt(c2.getColumnIndexOrThrow(str12))},${globalvars.bankaccountledgerid},${cstmt22.getDouble("TransId")} Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(
```

#### `ASYNCUpload.doInBackground` · line 1647
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `LastUpload`
```sql
INSERT INTO LastUpload(MCLGCollId ,FWGColId,TransId,SubmitAmount,SubmitedAt)
VALUES(${cstmt32.getInt("MCLGCollId")},${FWGColId},${cstmt22.getDouble("TransId")},${c2.getInt(c2.getColumnIndexOrThrow(str13))}${c2.getInt(c2.getColumnIndexOrThrow(str12))},'${LastSubmittedDateandTime}' )
```

#### `ASYNCUpload.doInBackground` · line 1669
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsmaster`
```sql
{call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `String.valueOf(globalvars.collectiondate)` (setString) · "TransType" → `TTId.intValue()` (setDouble) · "Narration" → `Narration` (setString) · str6 → `globalvars.finid.intValue()` (setInt) · "TransAmount" → `Amount.doubleValue()` (setDouble) · "FWTransId" → `8` (setNull) · "FWVWUDTransId" → `8` (setNull) · str9 → `MCLGCollId` (setDouble) · str9 → `8` (setNull) · "TransLedgerId" → `LedgerId.doubleValue()` (setDouble) · "RouteId" → `globalvars.finid.intValue()` (setDouble) · "Sourceledgerid" → `globalvars.cashbookledgerid.intValue()` (setDouble)
- OUT: str24 (8) · "FWVWTransId" (8)
- Reads: `OUT#str7`

#### `ASYNCUpload.doInBackground` · line 1694
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `ExpensesEntries`
```sql
update ExpensesEntries set ServerGenTransId=${cstmt6.getDouble(str7)}
where Id=${Double.parseDouble(c.getString(0))}
```

#### `ASYNCUpload.doInBackground` · line 1697
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · INSERT · tables: `tblTransactionsDetails`
```sql
insert into tblTransactionsDetails  Select ${Double.valueOf(cstmt6.getDouble(str7))} ,1 as SNo,${Double.parseDouble(c.getString(1))},'Target',${Double.parseDouble(c.getString(3))},'Dr',2
union all  Select ${cstmt6.getDouble(str7)},2 as SNo,${c.getInt(c.getColumnIndexOrThrow("SourceLedgerId"))},'Source',${Double.parseDouble(c.getString(3))} ,'Cr',1
```

#### `ASYNCUpload.doInBackground` · line 1701
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · INSERT · tables: `tblTransactionsDetails`
```sql
insert into tblTransactionsDetails  Select ${Double.valueOf(cstmt6.getDouble(str7))} ,1 as SNo,${Double.parseDouble(c.getString(1))},'Source',${Double.parseDouble(c.getString(3))},'Cr',2
union all  Select ${cstmt6.getDouble(str7)},2 as SNo,${c.getInt(c.getColumnIndexOrThrow("SourceLedgerId"))},'Target',${Double.parseDouble(c.getString(3))} ,'Dr',1
```

#### `ASYNCUpload.doInBackground` · line 1709
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `updatetbltransactionsmaster`
```sql
{call dbo.UpdatetblTransactionsMaster(?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `String.valueOf(globalvars.collectiondate)` (setString) · "TransType" → `TTId.intValue()` (setDouble) · "Narration" → `Narration` (setString) · str6 → `globalvars.finid.intValue()` (setInt) · "TransAmount" → `Amount.doubleValue()` (setDouble) · str9 → `MCLGCollId` (setDouble) · str9 → `8` (setNull) · "TransLedgerId" → `LedgerId.doubleValue()` (setDouble) · "RouteId" → `globalvars.finid.intValue()` (setInt) · str7 → `ServerGenExpensesTransId.doubleValue()` (setDouble)

#### `ASYNCUpload.doInBackground` · line 1725
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `tblTransactionsDetails`, `tbltransactionsdetails`
```sql
${"delete from tbltransactionsdetails where transid=" + ServerGenExpensesTransId}${";insert into tblTransactionsDetails Select "}${Double.valueOf(Double.parseDouble(c.getString(4)))} ,1 as SNo,${Double.parseDouble(c.getString(1))},'Target',${Double.parseDouble(c.getString(3))},'Dr',2
union all  Select ${Double.parseDouble(c.getString(4))},2 as SNo,${c.getInt(c.getColumnIndexOrThrow("SourceLedgerId"))},'Source',${Double.parseDouble(c.getString(3))} ,'Cr',1
```

#### `ASYNCUpload.doInBackground` · line 1727
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `tblTransactionsDetails`, `tbltransactionsdetails`
```sql
${"delete from tbltransactionsdetails where transid=" + ServerGenExpensesTransId}${";insert into tblTransactionsDetails Select "}${Double.valueOf(Double.parseDouble(c.getString(4)))} ,1 as SNo,${Double.parseDouble(c.getString(1))},'Source',${Double.parseDouble(c.getString(3))},'Cr',2
union all  Select ${Double.parseDouble(c.getString(4))},2 as SNo,${c.getInt(c.getColumnIndexOrThrow("SourceLedgerId"))},'Target',${Double.parseDouble(c.getString(3))} ,'Dr',1
```

#### `MyPrintDocumentAdapter.onStart` · line 2393
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select SubAreaName,PartyName,LAmount,Collected
From MobileAppCollData a,SubAreaDetails b
where a.subareano=b.subareano
order by b.ShowId,a.UDLedgerId
```

#### `MyPrintDocumentAdapter.onStart` · line 2394
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select SubAreaName,PartyName,LAmount,Collected,PrevCollected,MCLoanDate
From MobileAppCollData a,SubAreaDetails b
where a.subareano=b.subareano
order by b.ShowId,a.UDLedgerId
```

#### `MyPrintDocumentAdapter.onWrite` · line 2428
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select SubAreaName,PartyName,LAmount,Collected
From MobileAppCollData a,SubAreaDetails b
where a.subareano=b.subareano
order by b.ShowId,a.UDLedgerId
```

#### `MyPrintDocumentAdapter.onWrite` · line 2429
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select SubAreaName,PartyName,LAmount,Collected,PrevCollected,MCLoanDate
From MobileAppCollData a,SubAreaDetails b
where a.subareano=b.subareano
order by b.ShowId,a.UDLedgerId
```

## uploaddownloadcontroling

Screen: **Upload Control / Download Control** · [spec](../../brain/modules/03-offline-mode-download-upload-sync-services.md#upload-control--download-control--uploaddownloadcontroling) · 1 statements

#### `ASYNCDetails.doInBackground` · line 211
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ?
```sql
${UpdateSQLString}
```

