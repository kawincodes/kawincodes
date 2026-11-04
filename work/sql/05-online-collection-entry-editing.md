# SQL — 8.5 Online collection entry & editing

[← work.md](../../work.md) · [Module spec](../../brain/modules/05-online-collection-entry-editing.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **118 statements** (76 local SQLite, 42 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`editing_collectionlist_act`](#editingcollectionlistact) (1) · [`editing_collections_act`](#editingcollectionsact) (21) · [`editing_collectionswithfulloptions_act`](#editingcollectionswithfulloptionsact) (95) · [`editing_collectionsadapter`](#editingcollectionsadapter) (1)

## editing_collectionlist_act

Screen: **Loans Collection register** · [spec](../../brain/modules/05-online-collection-entry-editing.md#loans-collection-register--editingcollectionlistact) · 1 statements

#### `ASYNCCollections.doInBackground` · line 318
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblMCLoanGroupCollectionMaster`
```sql
Select MCLGCollId, FWGColId, CollDate, CollAmount,  CollStaffLedgerId, TransId, FinId,  NIPAmountCollected, CollStaff2LedgerId ,PenaltyCollected,SavingsCollected
from tblMCLoanGroupCollectionMaster
where CollDate>='${curdaterange.getFromdate()}' and CollDate<='${curdaterange.getTodate()}' and finId=${selectedfinanceobj.getFinId()}
order by CollDate desc
```

- Reads: `MCLGCollId`, `FWGColId`, `CollDate`, `CollAmount`, `CollStaffLedgerId`, `TransId`, `FinId`, `NIPAmountCollected`, `CollStaff2LedgerId`, `PenaltyCollected`, `SavingsCollected`

## editing_collections_act

Screen: **Collection editor (basic)** · [spec](../../brain/modules/05-online-collection-entry-editing.md#collection-editor-basic--editingcollectionsact) · 21 statements

#### `DisplayIndexData` · line 244
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select x.SubAreaName,PaidDues,PassedDues,x.MCLoanId,x.FWUDMCLoanId,x.Remarks,x.PartyLedgerId,x.MCLoanDate, interestamount,x.ClosingDate,y.CWLedgerId,x.UDLedgerId,x.PartyName,y.Tag,x.SubAreaNo,x.LAmount,x.DuesCount,x.InsAmount,x.MCLoanAmountCollectable,x.Collected,x.PaidDues,x.PassedDues,x.LoanMethodDesc,x.CollectionBreakString,x.NoOfIns,x.CBCount,x.istobeWarned,x.IsNIP,x.LoanStatus,x.IsMarked,x.PrevCollected ,ImagefileName,IdProofFileName,y.lat,y.lng,x.lat as collectionlat,x.lng as collectionlng,IsVisited,PenaltyCollected , SavingsCollected,MCLDueCollId,islocallyfixed
from MobileAppCollData  x, Parties y, SubAreaDetails z
where x.PartyLedgerId=y.LedgerId and x.SubAreaNo=z.SubAreaNo
order by z.ShowId,x.UDLedgerId
```

- Reads: `MCLoanAmountCollectable`, `PrevCollected`, `collectionlng`, `collectionlat`, `InterestAmount`, `MCLoanDate`, `CWLedgerId`, `ClosingDate`, `LoanStatus`, `IsMarked`, `UDLedgerId`, `LoanMethodDesc`, `PartyName`, `Tag`, `IsVisited`, `Remarks`, `MCLoanId`, `Collected`, `SubAreaNo`, `InsAmount`, `PaidDues`, `PassedDues`, `lat`, `islocallyfixed`, `lng`, `LAmount`, `IsNIP`, `PartyLedgerId`, `ImagefileName`, `IdProofFileName`, `PenaltyCollected`, `SavingsCollected`, `MCLDueCollId`, `SubAreaName`

#### `onpenaltyamountchange` · line 874
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set PenaltyCollected=${penaltyamountcollected}
where  MCLoanId= ${datareturnedfromadapter.getMCLoanId()}
```

#### `onsavingsamountchange` · line 886
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set SavingsCollected=${savingsamountcollected}
where  MCLoanId= ${datareturnedfromadapter.getMCLoanId()}
```

#### `ASYNCSavingCollections.doInBackground` · line 1091
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoanGroupCollectionMaster`
```sql
update tblMCLoanGroupCollectionMaster set  CollDate=?,CollAmount=?,CollStaffLedgerId=?,TransId=?,FinId=?,NIPAmountCollected=?,CollStaff2LedgerId=? ,PenaltyCollected=?,SavingsCollected=?,BankCollection=?,CashCollection=?
where MCLGCollId=?
```

- Params: 1 → `selectedcollection.getCollDate()` (setDate) · 2 → `selectedcollection.getCollAmount()` (setDouble) · 3 → `selectedcollection.getCollStaffLedgerId()` (setDouble) · 4 → `selectedcollection.getTransId()` (setDouble) · 5 → `selectedcollection.getFinId()` (setDouble) · 6 → `selectedcollection.getNIPAmountCollected()` (setDouble) · 7 → `selectedcollection.getCollStaff2LedgerId()` (setDouble) · 8 → `selectedcollection.getPenaltyCollected()` (setDouble) · 9 → `selectedcollection.getSavingsCollected()` (setDouble) · 10 → `selectedcollection.getCashCollection()` (setDouble) · 11 → `selectedcollection.getBankCollection()` (setDouble) · 12 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1105
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set  TransAmount=? where TransId=?
```

- Params: 1 → `selectedcollection.getCollAmount() + selectedcollection.getCashCollection() + selectedcol…` (setDouble) · 2 → `selectedcollection.getTransId()` (setDouble)

#### `ASYNCSavingCollections.doInBackground` · line 1126
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `tblTransactionsDetails`
```sql
${SQLStr + " Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus,…}${selectedcollection.getTransId()},114,0${selectedcollectiond_detailedlist.get(CollectedAmountTotal).getBankCollection()},'Source','Cr',3,4);Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(${selectedcollection.getTransId()},${selectedfinanceobj.getDefaultBankledgersid()},0${selectedcollectiond_detailedlist.get(CollectedAmountTotal).getBankCollection()},'Target','Dr',4,3);
```

#### `ASYNCSavingCollections.doInBackground` · line 1130
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoanDueCol`
```sql
update tblMCLoanDueCol set  Paid=?,  SavingsCollected=? ,  PenaltyCollected=? ,CashCollection=?,BankCollection=?
where MCLoanId=? and GColId=?
```

- Params: 1 → `selectedcollectiond_detailedlist.get(i2).getCollected().intValue()` (setInt) · 2 → `selectedcollectiond_detailedlist.get(i2).getSavingsCollected().intValue()` (setInt) · 3 → `selectedcollectiond_detailedlist.get(i2).getPenaltyCollected().intValue()` (setInt) · 4 → `selectedcollectiond_detailedlist.get(i2).getCashCollection()` (setInt) · 5 → `selectedcollectiond_detailedlist.get(i2).getBankCollection()` (setInt) · 6 → `selectedcollectiond_detailedlist.get(i2).getMCLoanId().intValue()` (setInt) · 7 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1141
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoanDueCol`
```sql
delete from tblMCLoanDueCol where MCLoanId=? and GColId=?
```

- Params: 1 → `selectedcollectiond_detailedlist.get(i2).getMCLoanId().intValue()` (setInt) · 2 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1156
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoanDueCol`
```sql
delete from tblMCLoanDueCol  where GColId=?
```

- Params: 1 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1159
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoanGroupCollectionMaster`
```sql
delete from tblMCLoanGroupCollectionMaster  where MCLGCollId=?
```

- Params: 1 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1162
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete from tblTransactionsDetails   where TransId=?
```

- Params: 1 → `selectedcollection.getTransId()` (setDouble)

#### `ASYNCSavingCollections.doInBackground` · line 1165
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsMaster`
```sql
delete from tblTransactionsMaster   where TransId=?
```

- Params: 1 → `selectedcollection.getTransId()` (setDouble)

#### `AsyncDownloaddata.doInBackground` · line 973
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · MCLOANCOLLECTIONDISPLAY
```sql
MCLoanCollectionDisplay @GColId=${selectedcollection.getMCLGCollId()}
```

- Reads: `MCLoanId`, `FWUDMCLoanid`, `PartyLedgerId`, `LDate`, `CWLedgerId`, `PartyName`, `SubAreaName`, `SubAreaNo`, `LAmount`, `DuesCount`, `LoanStatus`, `DueCollection`, `Balance`, `CurDue`, `MCLDueCollId`, `SavingsCollected`, `PenaltyCollected`, `Completed`, `Duescount`, `CashCollection`, `BankCollection`

#### `Update_sendsms.doInBackground` · line 1255
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
select AdminMobileNos from SelectedBranchFinanceDetails where finid=${selectedcollection.getFinId()}
```

- Reads: `AdminMobileNos`

#### `Update_sendsms.doInBackground` · line 1261
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle varchar,SMSDesc varchar)
```

#### `Update_sendsms.doInBackground` · line 1262
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSGateWay`
```sql
Create table IF NOT EXISTS SMSGateWay(SMSGId int,SMSAPI varchar,APIKey varchar,SMSSenderId varchar,SMSRoute int,SMSType varchar)
```

#### `Update_sendsms.doInBackground` · line 1267
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=11
```

- Reads: `SMSDesc`

#### `delete_sendsms.doInBackground` · line 1334
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
select AdminMobileNos from SelectedBranchFinanceDetails where finid=${selectedcollection.getFinId()}
```

- Reads: `AdminMobileNos`

#### `delete_sendsms.doInBackground` · line 1340
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle varchar,SMSDesc varchar)
```

#### `delete_sendsms.doInBackground` · line 1341
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSGateWay`
```sql
Create table IF NOT EXISTS SMSGateWay(SMSGId int,SMSAPI varchar,APIKey varchar,SMSSenderId varchar,SMSRoute int,SMSType varchar)
```

#### `delete_sendsms.doInBackground` · line 1346
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=10
```

- Reads: `SMSDesc`

## editing_collectionswithfulloptions_act

Screen: **Collection Entry (online) / full-options collection editor** · [spec](../../brain/modules/05-online-collection-entry-editing.md#collection-entry-online--full-options-collection-editor--editingcollectionswithfulloptionsact) · 95 statements

#### `DisplayIndexData` · line 324
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select z.FinId, x.SubAreaName,PaidDues,PassedDues,x.MCLoanId,x.FWUDMCLoanId,x.Remarks,x.PartyLedgerId,x.MCLoanDate, interestamount,x.ClosingDate,y.CWLedgerId,x.UDLedgerId,x.PartyName,y.Tag,x.SubAreaNo,x.LAmount,x.DuesCount,x.InsAmount,x.MCLoanAmountCollectable,x.Collected,x.PaidDues,x.PassedDues,x.LoanMethodDesc,x.CollectionBreakString,x.NoOfIns,x.CBCount,x.istobeWarned,x.IsNIP,x.LoanStatus,x.IsMarked,x.PrevCollected ,ImagefileName,IdProofFileName,y.lat,y.lng,x.lat as collectionlat,x.lng as collectionlng,IsVisited,PenaltyCollected ,z.ShowId, SavingsCollected,MCLDueCollId,islocallyfixed,CashCollection,BankCollection
from MobileAppCollData  x, Parties y, SubAreaDetails z
where x.PartyLedgerId=y.LedgerId and x.SubAreaNo=z.SubAreaNo
order by z.ShowId,x.UDLedgerId
```

- Reads: `ShowId`, `MCLoanAmountCollectable`, `PrevCollected`, `collectionlng`, `collectionlat`, `InterestAmount`, `MCLoanDate`, `CWLedgerId`, `ClosingDate`, `IsMarked`, `UDLedgerId`, `LoanMethodDesc`, `PartyName`, `Tag`, `IsVisited`, `Remarks`, `MCLoanId`, `Collected`, `SubAreaNo`, `InsAmount`, `LoanStatus`, `PaidDues`, `PassedDues`, `lat`, `islocallyfixed`, `lng`, `LAmount`, `IsNIP`, `PartyLedgerId`, `ImagefileName`, `IdProofFileName`, `PenaltyCollected`, `SavingsCollected`, `MCLDueCollId`, `SubAreaName`, `CashCollection`, `BankCollection`, `FinId`

#### `lambda$onCreate$1` · line 299
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set islocallyfixed ='true'
where  MCLoanId= ${tabbedcollectionlistdataList.get(i).getMCLoanId()}
```

#### `onCreate` · line 242
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LastUpload`
```sql
select SubmitAmount,SubmitedAt,MCLGCollId,FWGColId,TransId,CollStaffLedgerId,CollStaff2LedgerId
from LastUpload
```

- Reads: `CollStaffLedgerId`, `CollStaff2LedgerId`, `MCLGCollId`, `FWGColId`, `TransId`, `SubmitAmount`, `SubmitedAt`

#### `onOptionsItemSelected` · line 448
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `LastUpload`
```sql
select SubmitAmount,SubmitedAt,MCLGCollId,FWGColId,TransId,CollStaffLedgerId,CollStaff2LedgerId
from LastUpload
```

- Reads: `CollStaffLedgerId`, `CollStaff2LedgerId`, `MCLGCollId`, `TransId`, `SubmitAmount`, `SubmitedAt`

#### `oncollectionamountchanged` · line 936
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `MobileAppCollData`
```sql
UPDATE MobileAppCollData SET lng=${Double.valueOf(currentLng)}, lat=${Double.valueOf(currentLat)}, Collected=${Integer.valueOf(collectedamount)}, CashCollection=${Integer.valueOf(datareturnedfromadapter.getCashCollection())}, BankCollection=${Integer.valueOf(datareturnedfromadapter.getBankCollection())}
WHERE MCLoanId=${datareturnedfromadapter.getMCLoanId()}
```

#### `ondueamounttabbed` · line 970
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `MobileAppCollData`
```sql
UPDATE MobileAppCollData SET lng=${Double.valueOf(latitude)}, lat=${Double.valueOf(longitude)}, Collected=${Integer.valueOf(collectedamount)}, CashCollection=${Integer.valueOf(tabbedcollectionlistdataList.get(i).getCashCollection())}, BankCollection=${Integer.valueOf(tabbedcollectionlistdataList.get(i).getBankCollection())}
WHERE MCLoanId=${datareturnedfromadapter.getMCLoanId()}
```

#### `onpenaltyamountchange` · line 978
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set PenaltyCollected=${penaltyamountcollected}
where  MCLoanId= ${datareturnedfromadapter.getMCLoanId()}
```

#### `onsavingsamountchange` · line 990
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set SavingsCollected=${savingsamountcollected}
where  MCLoanId= ${datareturnedfromadapter.getMCLoanId()}
```

#### `onvisitstatuschanged` · line 946
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set Visited=${visitstatus} where  MCLoanId= ${datareturnedfromadapter.getMCLoanId()}
```

#### `ASYNCSavingCollections.doInBackground` · line 1772
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select MCLoanId,MCLDueCollId
from MobileAppCollData
where MCLoanId=${tabbedcollectionlistdataList.get(i4).getMCLoanId()}
```

- Reads: `MCLDueCollId`

#### `ASYNCSavingCollections.doInBackground` · line 1781
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoanGroupCollectionMaster`
```sql
update tblMCLoanGroupCollectionMaster set CollDate=?,CollAmount=?,CollStaffLedgerId=?,NIPAmountCollected=?,CollStaff2LedgerId=?,PenaltyCollected=?,SavingsCollected=?,CashCollection=?,BankCollection=?
where MCLGCollId=?
```

- Params: 1 → `selectedcollection.getCollDate()` (setDate) · 2 → `selectedcollection.getCollAmount()` (setDouble) · 3 → `selectedcollection.getCollStaffLedgerId()` (setDouble) · 4 → `0.0d` (setDouble) · 5 → `selectedcollection.getCollStaff2LedgerId()` (setDouble) · 6 → `selectedcollection.getPenaltyCollected()` (setDouble) · 7 → `selectedcollection.getSavingsCollected()` (setDouble) · 8 → `selectedcollection.getCashCollection()` (setInt) · 9 → `selectedcollection.getBankCollection()` (setInt) · 10 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1793
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set  TransAmount=? where TransId=?
```

- Params: 1 → `selectedcollection.getCollAmount()` (setDouble) · 2 → `selectedcollection.getTransId()` (setDouble)

#### `ASYNCSavingCollections.doInBackground` · line 1821
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `tblTransactionsDetails`
```sql
${CollectionSQLStr2 + " Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,Le…}${selectedcollection.getTransId()},114,${selectedcollection.getBankCollection()},'Source','Cr',3,4); Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(${selectedcollection.getTransId()},${globalvars.bankaccountledgerid},${selectedcollection.getBankCollection()},'Target','Dr',4,3);
```

#### `ASYNCSavingCollections.doInBackground` · line 1833
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoanDueCol`
```sql
update tblMCLoanDueCol set Paid=?,SavingsCollected=?,  PenaltyCollected=? ,  CashCollection=? ,  BankCollection=?
where MCLoanId=? and MCLDueCollId=?
```

- Params: 1 → `tabbedcollectionlistdataList.get(i5).getCollected().intValue()` (setInt) · 2 → `tabbedcollectionlistdataList.get(i5).getSavingsCollected().intValue()` (setInt) · 3 → `tabbedcollectionlistdataList.get(i5).getPenaltyCollected().intValue()` (setInt) · 4 → `tabbedcollectionlistdataList.get(i5).getCashCollection()` (setInt) · 5 → `tabbedcollectionlistdataList.get(i5).getBankCollection()` (setInt) · 6 → `tabbedcollectionlistdataList.get(i5).getMCLoanId().intValue()` (setInt) · 7 → `tabbedcollectionlistdataList.get(i5).getMCLDueCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1850
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoanDueCol`
```sql
delete from tblMCLoanDueCol where MCLoanId=? and GColId=?
```

- Params: 1 → `tabbedcollectionlistdataList.get(i5).getMCLoanId().intValue()` (setInt) · 2 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1866, 1931
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblmcloanduecol`
```sql
{call dbo.InserttblMCLoanDueCol(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "MCLDueColDate" → `selectedcollection.getCollDate()` (setDate) · "MCLoanId" → `tabbedcollectionlistdataList.get(i5).getMCLoanId().intValue()` (setDouble) · "TTId" → `22.0d` (setDouble) · "SourceLedgerId" → `tabbedcollectionlistdataList.get(i5).getPartyLedgerId().intValue()` (setDouble) · "TargetLedgerId" → `1.0d` (setDouble) · "FinId" → `selectedcollection.getFinId()` (setDouble) · "Paid" → `tabbedcollectionlistdataList.get(i5).getCollected().intValue()` (setDouble) · "GColId" → `selectedcollection.getMCLGCollId()` (setDouble) · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i5).getPenaltyCollected().intValue()` (setDouble) · "SavingsCollected" → `tabbedcollectionlistdataList.get(i5).getSavingsCollected().intValue()` (setDouble) · "PenaltyCollected" → `tabbedcollectionlistdataList.get(i5).getPenaltyCollected().intValue()` (setDouble) · "CashCollection" → `tabbedcollectionlistdataList.get(i5).getCashCollection()` (setDouble) · "BankCollection" → `tabbedcollectionlistdataList.get(i5).getBankCollection()` (setDouble) · "lng" → `tabbedcollectionlistdataList.get(i5).getCollectionlng()` (setString) · "lat" → `tabbedcollectionlistdataList.get(i5).getCollectionlat()` (setString)
- OUT: str2 (8)
- Reads: `OUT#str2`

#### `ASYNCSavingCollections.doInBackground` · line 1889
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set  MCLDueCollId=${tabbedcollectionlistdataList.get(i5).getMCLDueCollId()}
where  MCLoanId= ${tabbedcollectionlistdataList.get(i5).getMCLoanId()}
```

#### `ASYNCSavingCollections.doInBackground` · line 1957
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set MCLDueCollId=${tabbedcollectionlistdataList.get(i6).getMCLDueCollId()}
where  MCLoanId= ${tabbedcollectionlistdataList.get(i6).getMCLoanId()}
```

#### `ASYNCSavingCollections.doInBackground` · line 1977
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoanDueCol`
```sql
delete from tblMCLoanDueCol  where GColId=?
```

- Params: 1 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1980
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoanGroupCollectionMaster`
```sql
delete from tblMCLoanGroupCollectionMaster  where MCLGCollId=?
```

- Params: 1 → `selectedcollection.getMCLGCollId()` (setInt)

#### `ASYNCSavingCollections.doInBackground` · line 1983
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete from tblTransactionsDetails   where TransId=?
```

- Params: 1 → `selectedcollection.getTransId()` (setDouble)

#### `ASYNCSavingCollections.doInBackground` · line 1986
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsMaster`
```sql
delete from tblTransactionsMaster   where TransId=?
```

- Params: 1 → `selectedcollection.getTransId()` (setDouble)

#### `ASYNCSavingCollections.onPostExecute` · line 2025
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `ASYNCSavingCollections.onPostExecute` · line 2026
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `AsyncDownloaddata.doInBackground` · line 1144
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `DownloadControling`, `FinanceAccessAllowed`, `softsettings`
```sql
select top 1 x.IMEI , left( SWITCHOFFSET(SYSDATETIMEOFFSET(),'+05:30') ,10 ) as downloaddate,convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(), '+05:30'),0 ) as DownloadTime ,case when (select  bitvalue
from softsettings
where SSId=32)='True' and isAllowed is not null and isAllowed='True' then 'True' when (select bitvalue
from softsettings
where SSId=32)='False' then 'True' else 'False' end  as 'IsAllowed'
from FinanceAccessAllowed x
left
join DownloadControling z on x.FinId=z.FinId and TransDate='${selectedcollectiondate}',MobileDevlicesAllowed y
where x.IMEI=y.IMEI and x.FinId=${receviedfinanceid} and y.isActive =1 and x.IMEI='${globalvars.device_id}'
```

- Reads: `isAllowed`, `DownloadTime`

#### `AsyncDownloaddata.doInBackground` · line 1166
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `MobileDevlicesAllowed`
```sql
select userroleid from MobileDevlicesAllowed where IMEI='${globalvars.device_id}'
```

- Reads: `userroleid`

#### `AsyncDownloaddata.doInBackground` · line 1182
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `loanindex`
```sql
Create table IF NOT EXISTS loanindex(LedgerId Int,LoanIndex Int,MCLoanId Int,MCLoanAmountCollectable Int,collected Int,Balance Int)
```

#### `AsyncDownloaddata.doInBackground` · line 1183
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `loanindex`
```sql
delete from loanindex
```

#### `AsyncDownloaddata.doInBackground` · line 1185
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · GET_LOANINDEXWISELOANS
```sql
Get_LoanindexwiseLoans @FinId=${receviedfinanceid}
```

#### `AsyncDownloaddata.doInBackground` · line 1187
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · GET_LOANINDEXWISELOANSFORWEEKLYLINES
```sql
Get_LoanindexwiseLoansforweeklylines @ColDate ='${selectedcollectiondate}' ,@FinId=${receviedfinanceid}
```

- Reads: `LedgerId`, `LoanIndex`, `MCLoanId`, `MCLoanAmountCollectable`, `collected`, `Balance`

#### `AsyncDownloaddata.doInBackground` · line 1199
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `loanindex`
```sql
INSERT INTO loanindex (LedgerId, LoanIndex, MCLoanId, MCLoanAmountCollectable, collected, Balance)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${Integer.valueOf(reset.getInt("LoanIndex"))}, ${Integer.valueOf(reset.getInt("MCLoanId"))}, ${Integer.valueOf(reset.getInt("MCLoanAmountCollectable"))}, ${Integer.valueOf(reset.getInt("collected"))}, ${Integer.valueOf(reset.getInt("Balance"))})
```

#### `AsyncDownloaddata.doInBackground` · line 1223
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `softsettings`
```sql
select bitvalue from softsettings where ssid=14
```

- Reads: `bitvalue`

#### `AsyncDownloaddata.doInBackground` · line 1227
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `financemaster`
```sql
select CollectionBreakString,cashledgerid
From financemaster a, LoanMethods b
where a.defaultloanmethodid=b.LoanMethodId and a.finid=${receviedfinanceid}
```

- Reads: `cashledgerid`, `CollectionBreakString`

#### `AsyncDownloaddata.doInBackground` · line 1241
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `selectedcollectiondetails`
```sql
Create table IF NOT EXISTS selectedcollectiondetails(MCLDueCollId Double,MCLoanId Double,GColId Double,Paid Double,PenaltyCollected Double,SavingsCollected Double,CashCollection Double,BankCollection Double)
```

#### `AsyncDownloaddata.doInBackground` · line 1242
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `selectedcollectiondetails`
```sql
delete from selectedcollectiondetails
```

#### `AsyncDownloaddata.doInBackground` · line 1245, 1275
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `AsyncDownloaddata.doInBackground` · line 1247
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblMCLoanDueCol`
```sql
select MCLDueCollId,MCLoanId,GColId,Paid,PenaltyCollected,SavingsCollected ,CashCollection,BankCollection
From tblMCLoanDueCol
where GColId=${selectedcollection.getMCLGCollId()}
order by MCLoanId
```

- Reads: `MCLDueCollId`, `MCLoanId`, `GColId`, `Paid`, `PenaltyCollected`, `SavingsCollected`, `CashCollection`, `BankCollection`

#### `AsyncDownloaddata.doInBackground` · line 1262
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `selectedcollectiondetails`
```sql
INSERT INTO selectedcollectiondetails (MCLDueCollId, MCLoanId, GColId, Paid, PenaltyCollected, SavingsCollected, CashCollection, BankCollection)
VALUES (${Integer.valueOf(reset.getInt("MCLDueCollId"))}, ${Integer.valueOf(reset.getInt("MCLoanId"))}, ${Integer.valueOf(reset.getInt("GColId"))}, ${Integer.valueOf(reset.getInt("Paid"))}, ${Integer.valueOf(reset.getInt("PenaltyCollected"))}, ${Integer.valueOf(reset.getInt("SavingsCollected"))}, ${Integer.valueOf(reset.getInt("CashCollection"))}, ${Integer.valueOf(reset.getInt("BankCollection"))})
```

#### `AsyncDownloaddata.doInBackground` · line 1273
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpHeaders`
```sql
CREATE TABLE IF NOT EXISTS ExpHeaders(LedgerId Double,LedgerName nvarchar,TTId int)
```

#### `AsyncDownloaddata.doInBackground` · line 1276
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpHeaders`
```sql
delete from ExpHeaders
```

#### `AsyncDownloaddata.doInBackground` · line 1277
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `PrevCollections`
```sql
Create table IF NOT EXISTS PrevCollections (RowNo int,MCLoanId Double, CollDate varchar,Paid Double)
```

#### `AsyncDownloaddata.doInBackground` · line 1278
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `PrevCollections`
```sql
delete from PrevCollections
```

#### `AsyncDownloaddata.doInBackground` · line 1279
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `PrevLocations`
```sql
Create table IF NOT EXISTS PrevLocations (MCLoanId int, MCLDueColDate date,lat nvarchar,lng nvarchar)
```

#### `AsyncDownloaddata.doInBackground` · line 1280
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `PrevLocations`
```sql
delete from PrevLocations
```

#### `AsyncDownloaddata.doInBackground` · line 1282
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

#### `AsyncDownloaddata.doInBackground` · line 1284
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `financemaster`
```sql
select CollectionBreakString
From financemaster a, LoanMethods b
where a.defaultloanmethodid=b.LoanMethodId and a.finid=${globalvars.finid}
```

- Reads: `CollectionBreakString`

#### `AsyncDownloaddata.doInBackground` · line 1291
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId
from subareadetails
where FinId=${globalvars.finid}
order by ShowId
```

#### `AsyncDownloaddata.doInBackground` · line 1293
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId
from subareadetails
where FinId=${globalvars.finid} and WeekDayId=${globalvars.currentweekdayid}
order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`, `IsVisible`, `ShowId`, `FinId`, `WeekDayId`

#### `AsyncDownloaddata.doInBackground` · line 1309
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SubAreaDetails`
```sql
INSERT INTO SubAreaDetails (SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId)
VALUES (${Double.valueOf(reset.getDouble("SubAreaNo"))}, ${reset.getString("SubAreaName")}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Integer.valueOf(reset.getInt("ShowId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt("WeekDayId"))})
```

#### `AsyncDownloaddata.doInBackground` · line 1315
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `subareadetails`, `tblLedgers`
```sql
select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,isnull(MobileNo1,'') as MobileNo1,isnull(MobileNo2,'') as MobileNo2,SpouseName,FamilyName,lat,lng,ImageFileName,IdProofFileName,AddLine1,IdProofFileNameII ,NoOfLoansAllowed,MaximumAmountAllowed,isactive
from tblLedgers
where ScreenType=3 and isactive=1 and SubAreaNo in (select SubAreaNo
from subareadetails
Where FinId=${globalvars.finid})
order by SubAreaNo, UDLedgerId
```

#### `AsyncDownloaddata.doInBackground` · line 1317
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `subareadetails`, `tblLedgers`
```sql
select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,isnull(MobileNo1,'') as MobileNo1,isnull(MobileNo2,'') as MobileNo2,SpouseName,FamilyName,lat,lng,ImageFileName,IdProofFileName,AddLine1,IdProofFileNameII ,NoOfLoansAllowed,MaximumAmountAllowed,isactive
from tblLedgers
where  isactive=1 and ScreenType=3 and  SubAreaNo in (select SubAreaNo
from subareadetails
Where FinId=${globalvars.finid})
order by SubAreaNo, UDLedgerId
```

- Reads: `CWLedgerId`, `LedgerName`, `Tag`, `SubAreaNo`, `UDLedgerId`, `MobileNo1`, `MobileNo2`, `SpouseName`, `FamilyName`, `lat`, `lng`, `ImageFileName`, `IdProofFileName`, `AddLine1`, `IdProofFileNameII`, `isactive`, `MaximumAmountAllowed`, `IsActive`

#### `AsyncDownloaddata.doInBackground` · line 1345
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Parties`
```sql
INSERT INTO Parties (LedgerId, CWLedgerId, LedgerName, Tag, SubAreaNo, UDLedgerId, MobileNo1, MobileNo2, SpouseName, FamilyName, lat, lng, ImageFileName, IdProofFileName, AddLine1, IdProofFileNameII, IdProofFileNameII, MaximumAmountAllowed, IsActive)
VALUES (${Double.valueOf(reset.getDouble(str10))}, ${Double.valueOf(reset.getDouble("CWLedgerId"))}, ${reset.getString("LedgerName")}, ${reset.getString("Tag")}, ${reset.getString("SubAreaNo")}, ${reset.getString("UDLedgerId")}, ${reset.getString("MobileNo1")}, ${reset.getString("MobileNo2")}, ${reset.getString("SpouseName")}, ${reset.getString("FamilyName")}, ${reset.getString("lat")}, ${reset.getString("lng")}, ${reset.getString("ImageFileName")}, ${reset.getString("IdProofFileName")}, ${reset.getString("AddLine1")}, ${reset.getString("IdProofFileNameII")}, ${reset.getString("isactive")}, ${Integer.valueOf(reset.getInt("MaximumAmountAllowed"))}, ${Integer.valueOf(reset.getInt("IsActive"))})
```

#### `AsyncDownloaddata.doInBackground` · line 1352
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · APP_MCLOANCOLLECTIONDATAWITHLOANCLOSOUREMONITERING_WITHWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport @ColDate ='${selectedcollectiondate}' ,@FinId=${receviedfinanceid}
```

#### `AsyncDownloaddata.doInBackground` · line 1354
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · APP_MCLOANCOLLECTIONDATAWITHLOANCLOSOUREMONITERING_WITHOUTWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport @ColDate ='${selectedcollectiondate}' ,@CollectionListfromDate ='${selectedcollectiondate}' ,@FinId=${receviedfinanceid}
```

#### `AsyncDownloaddata.doInBackground` · line 1356
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · APP_MCLOANCOLLECTIONDATAAUTOLOANCLOSOURE_WITHWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport @ColDate ='${selectedcollectiondate}' ,@FinId=${receviedfinanceid}
```

#### `AsyncDownloaddata.doInBackground` · line 1358
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · APP_MCLOANCOLLECTIONDATAAUTOLOANCLOSOURE_WITHOUTWEEKDAYSSUPPORT
```sql
APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport @ColDate ='${selectedcollectiondate}' ,  @CollectionListfromDate ='${selectedcollectiondate}' , @FinId=${receviedfinanceid}
```

- Reads: `ShowId`, `MCLoanId`, `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `PartyName`, `SubAreaNo`, `SubAreaName`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PrevCollected`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`, `InterestAmount`, `Imagefilename`, `interestamount`, `Noofloansallowed`, `Doccharges`, `IsAttentionRequired`, `RowNo`, `MCLoanID`, `Paid`, `CollDate`, `MCLDueColDate`, `lng`, `lat`

#### `AsyncDownloaddata.doInBackground` · line 1440
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `MobileAppCollData`
```sql
INSERT INTO MobileAppCollData (MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, LAmount, DuesCount, InsAmount, ShowId, MCLoanAmountCollectable, Collected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, IsMarked, PrevCollected, interestamount, PartyName, SubAreaName, PenaltyCollected, SavingsCollected, IsVisited, LoanIndex, Noofloansallowed, Doccharges, IsAttentionRequired)
VALUES (${Integer.valueOf(reset.getInt("MCLoanId"))}, ${Integer.valueOf(reset.getInt("FWUDMCLoanId"))}, ${reset.getString("Remarks")}, ${Integer.valueOf(reset.getInt("PartyLedgerId"))}, ${reset.getString("MCLoanDate")}, ${reset.getString("ClosingDate")}, ${Integer.valueOf(reset.getInt("CWLedgerId"))}, ${Integer.valueOf(reset.getInt(str3))}, ${reset.getString("PartyName")}, ${Integer.valueOf(reset.getInt("SubAreaNo"))}, ${Integer.valueOf(reset.getInt("LAmount"))}, ${Integer.valueOf(reset.getInt("DuesCount"))}, ${Integer.valueOf(reset.getInt("InsAmount"))}, ${Integer.valueOf(reset.getInt("ShowId"))}, ${Integer.valueOf(reset.getInt("MCLoanAmountCollectable"))}, ${num}, ${Integer.valueOf(reset.getInt("PaidDues"))}, ${Integer.valueOf(reset.getInt("PassedDues"))}, ${reset.getString("LoanMethodDesc")}, ${reset.getString(str)}, ${Integer.valueOf(reset.getInt("NoOfIns"))}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Boolean.valueOf(reset.getBoolean("istobeWarned"))}, ${Boolean.valueOf(reset.getBoolean("IsNIP"))}, ${reset.getString("LoanStatus")}, ${bool}, ${Integer.valueOf(reset.getInt("PrevCollected"))}, ${Integer.valueOf(reset.getInt("interestamount"))}, ${tabbedcollectionlistdataVar.getPartyName()}, ${tabbedcollectionlistdataVar.getSubAreaName()}, ${num}, ${num}, ${bool}, ${Integer.valueOf(reset.getInt(str16))}, ${Integer.valueOf(reset.getInt("Noofloansallowed"))}, ${Integer.valueOf(reset.getInt("Doccharges"))}, ${Boolean.valueOf(reset.getBoolean("IsAttentionRequired"))})
```

#### `AsyncDownloaddata.doInBackground` · line 1450
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `selectedcollectiondetails`
```sql
Select MCLDueCollId, MCLoanId, GColId, Paid, PenaltyCollected, SavingsCollected,CashCollection,BankCollection
from selectedcollectiondetails
where MCLoanId=${reset.getInt("MCLoanId")}
```

- Reads: `Paid`, `PenaltyCollected`, `SavingsCollected`, `MCLDueCollId`

#### `AsyncDownloaddata.doInBackground` · line 1521
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `PrevCollections`
```sql
INSERT INTO PrevCollections (RowNo, MCLoanID, Paid, CollDate)
VALUES (${Integer.valueOf(reset.getInt("RowNo"))}, ${Integer.valueOf(reset.getInt("MCLoanID"))}, ${Double.valueOf(reset.getDouble("Paid"))}, ${String.valueOf(reset.getDate("CollDate"))})
```

#### `AsyncDownloaddata.doInBackground` · line 1532
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `PrevLocations`
```sql
INSERT INTO PrevLocations (MCLoanID, MCLDueColDate, lng, lat)
VALUES (${Integer.valueOf(reset.getInt("MCLoanID"))}, ${String.valueOf(reset.getDate("MCLDueColDate"))}, ${reset.getString("lng")}, ${reset.getString("lat")})
```

#### `AsyncDownloaddata.doInBackground` · line 1541
**SQL Server** · `jdbc.prepareCall` · branch-report DB · CALL · procedure: `inserttbltransactionsmaster`
```sql
{call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `selectedcollectiondate` (setDate) · "TransType" → `constantvalues.MCLoanCollectionTTId.intValue()` (setDouble) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setInt) · "TransAmount" → `0.0d` (setDouble) · "FWTransId" → `8` (setNull) · "FWVWUDTransId" → `8` (setNull) · "GColId" → `8` (setNull) · "TransLedgerId" → `8` (setNull) · "RouteId" → `globalvars.finid.intValue()` (setDouble)
- OUT: "TransId" (8) · "FWVWTransId" (8)
- Reads: `TransId`

#### `AsyncDownloaddata.doInBackground` · line 1559
**SQL Server** · `jdbc.prepareCall` · branch-report DB · CALL · procedure: `inserttblmcloangroupcollectionmasterwithtwostaffs`
```sql
{call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "CollDate" → `globalvars.collectiondate` (setDate) · "CollAmount" → `0.0d` (setDouble) · "PenaltyCollected" → `0.0d` (setDouble) · "SavingsCollected" → `0.0d` (setDouble) · "CollStaffLedgerId" → `globalvars.currentcollectionexecutivelederid1.intValue()` (setInt) · "CollStaff2LedgerId" → `globalvars.currentcollectionexecutivelederid2.intValue()` (setInt) · "TransId" → `selectedcollection.getTransId()` (setDouble) · "NIPAmountCollected" → `0.0d` (setDouble) · "FinId" → `globalvars.finid.intValue()` (setDouble) · str21 → `0` (setInt) · str20 → `0` (setInt)
- OUT: "MCLGCollId" (3) · "FWGColId" (3)
- Reads: `MCLGCollId`, `FWGColId`

#### `AsyncDownloaddata.doInBackground` · line 1578
**SQL Server** · `jdbc.Statement.execute` · branch-report DB · INSERT · tables: `tblTransactionsDetails`
```sql
Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(${selectedcollection.getTransId()},114,0,'Source','Cr',1,2);Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(${selectedcollection.getTransId()},${receviedfinancecashledgerid},0,'Target','Dr',2,1);
```

#### `AsyncDownloaddata.doInBackground` · line 1582
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT
```sql
select  convert(varchar,SWITCHOFFSET(SYSDATETIMEOFFSET(), '+05:30'),0 ) as DownloadTime
```

- Reads: `DownloadTime`

#### `AsyncDownloaddata.doInBackground` · line 1589
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `AsyncDownloaddata.doInBackground` · line 1598
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `LastUpload`
```sql
INSERT INTO LastUpload (MCLGCollId, FWGColId, TransId, SubmitAmount, SubmitedAt, CollStaffLedgerId, CollStaff2LedgerId)
VALUES (${Integer.valueOf(selectedcollection.getMCLGCollId())}, ${Integer.valueOf(selectedcollection.getFWGColId())}, ${Integer.valueOf(selectedcollection.getTransId())}, ${num4}, ${LastSubmittedDateandTime}, ${Integer.valueOf(selectedcollection.getCollStaffLedgerId())}, ${Integer.valueOf(selectedcollection.getCollStaff2LedgerId())})
```

#### `AsyncDownloaddata.onPreExecute` · line 1681
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `NewItems`
```sql
CREATE TABLE IF NOT EXISTS NewItems(SNo int,ItemCode int,Qty int,MCLoanid Double,PRate int,TotalAmt int)
```

#### `AsyncDownloaddata.onPreExecute` · line 1682
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpHeaders`
```sql
CREATE TABLE IF NOT EXISTS ExpHeaders(LedgerId Double,LedgerName nvarchar,TTId int)
```

#### `AsyncDownloaddata.onPreExecute` · line 1683
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewItems`
```sql
delete from NewItems
```

#### `AsyncDownloaddata.onPreExecute` · line 1686
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `MobileAppCollData`
```sql
delete from MobileAppCollData
```

#### `AsyncDownloaddata.onPreExecute` · line 1687
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `LastUpload`
```sql
delete from LastUpload
```

#### `AsyncDownloaddata.onPreExecute` · line 1688
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties
```

#### `AsyncDownloaddata.onPreExecute` · line 1689
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans
```

#### `AsyncDownloaddata.onPreExecute` · line 1690
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `ExpensesEntries`
```sql
Create table IF NOT EXISTS ExpensesEntries(ID INTEGER PRIMARY KEY   AUTOINCREMENT,FinId Double,LedgerId Double,Amount Double,ServerGenTransId Double,TTId int,SourceLedgerId int,Narration varchar )
```

#### `AsyncDownloaddata.onPreExecute` · line 1691
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpensesEntries`
```sql
delete from ExpensesEntries
```

#### `AsyncDownloaddata.onPreExecute` · line 1692
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `Parties`
```sql
delete from Parties
```

#### `AsyncDownloaddata.onPreExecute` · line 1693
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `ExpHeaders`
```sql
delete from ExpHeaders
```

#### `AsyncDownloaddata.onPreExecute` · line 1694
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `PrevCollections`
```sql
Create table IF NOT EXISTS PrevCollections (RowNo int,MCLoanId Double, CollDate varchar,Paid Double)
```

#### `AsyncDownloaddata.onPreExecute` · line 1695
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `PrevCollections`
```sql
delete from PrevCollections
```

#### `AsyncDownloaddata.onPreExecute` · line 1697
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails
```

#### `AsyncDownloaddata.onPreExecute` · line 1698
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `RelatedDocCollection`
```sql
delete from RelatedDocCollection
```

#### `Update_sendsms.doInBackground` · line 2185
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
select AdminMobileNos from SelectedBranchFinanceDetails where finid=${selectedcollection.getFinId()}
```

- Reads: `AdminMobileNos`

#### `Update_sendsms.doInBackground` · line 2192
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle varchar,SMSDesc varchar)
```

#### `Update_sendsms.doInBackground` · line 2193
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSGateWay`
```sql
Create table IF NOT EXISTS SMSGateWay(SMSGId int,SMSAPI varchar,APIKey varchar,SMSSenderId varchar,SMSRoute int,SMSType varchar)
```

#### `Update_sendsms.doInBackground` · line 2198
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=11
```

- Reads: `SMSDesc`

#### `delete_sendsms.doInBackground` · line 2274
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
select AdminMobileNos from SelectedBranchFinanceDetails where finid=${selectedcollection.getFinId()}
```

- Reads: `AdminMobileNos`

#### `delete_sendsms.doInBackground` · line 2281
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle varchar,SMSDesc varchar)
```

#### `delete_sendsms.doInBackground` · line 2282
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSGateWay`
```sql
Create table IF NOT EXISTS SMSGateWay(SMSGId int,SMSAPI varchar,APIKey varchar,SMSSenderId varchar,SMSRoute int,SMSType varchar)
```

#### `delete_sendsms.doInBackground` · line 2287
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=10
```

- Reads: `SMSDesc`

#### `new BroadcastReceiver(){}.onReceive` · line 143
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select MCLoanId,MCLDueCollId
from MobileAppCollData
where MCLDueCollId is not null and MCLoanId=  ${tabbedcollectionlistdataList.get(i).getMCLoanId()}
```

- Reads: `MCLDueCollId`

#### `onlinefinishingcollectionSendSMS.doInBackground` · line 2082
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where LedgerId=${globalvars.currentcollectionexecutivelederid1}
```

#### `onlinefinishingcollectionSendSMS.doInBackground` · line 2092
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `CollectionExecutives`
```sql
select LedgerId,LedgerName
from CollectionExecutives
where LedgerId=${globalvars.currentcollectionexecutivelederid2}
```

#### `onlinefinishingcollectionSendSMS.doInBackground` · line 2103
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=2
```

- Reads: `SMSDesc`

## editing_collectionsadapter

[spec](../../brain/modules/05-online-collection-entry-editing.md) · 1 statements

#### `onBindViewHolder` · line 143
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `PrevCollections`
```sql
select CollDate,Paid
from PrevCollections
where MCLoanid =${tabbedcollectionlistdata.getMCLoanId()}
order by RowNo desc
```

