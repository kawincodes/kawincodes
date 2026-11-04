# SQL — 8.4 Offline collection entry (area-wise collection list & individual collection)

[← work.md](../../work.md) · [Module spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **70 statements** (59 local SQLite, 11 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`directcollectionentry`](#directcollectionentry) (16) · [`selectedloancollectiondetails`](#selectedloancollectiondetails) (4) · [`tabbedcollectionlist`](#tabbedcollectionlist) (3) · [`tabbedcollectionlistmviewpager`](#tabbedcollectionlistmviewpager) (1) · [`tabbedindividualcollectionentry`](#tabbedindividualcollectionentry) (33) · [`tabbedcollectionlistadapter`](#tabbedcollectionlistadapter) (6) · [`tabbedcollectionlistadaptermviewpager`](#tabbedcollectionlistadaptermviewpager) (7)

## directcollectionentry

Screen: **Direct collection entry (online, single loan)** · [spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md#direct-collection-entry-online-single-loan--directcollectionentry) · 16 statements

#### `asyncgetloandetails.doInBackground` · line 577
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `InvTransDetails`
```sql
Select SNo ,ItemCode ,Rate ,BilledQty ,Total,  TransId
from InvTransDetails
where  Transid=${curloanobj.getServerGenTransId()}
order by SNo
```

- Reads: `SNo`, `Itemcode`, `Rate`, `BilledQty`, `Total`

#### `asyncgetloandetails.doInBackground` · line 589
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GETDETAILSFORDIRECTCOLLECTION
```sql
GetDetailsforDirectCollection @ColDate ='${collectiondate}' ,@MCLoanId=${curloanobj.getMCLoanId()}
```

- Reads: `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `MCLoanDate`, `ClosingDate`, `UDLedgerId`, `PartyName`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`, `PrevCollected`, `interestamount`, `LoanIndex`, `Noofloansallowed`, `Doccharges`, `IsAttentionRequired`

#### `asyncgetloandetails.doInBackground` · line 636
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblMCLoanDueCol`, `tblMCLoans`
```sql
select GColId,Row_Number() over(
Order by MCLDueCollId) as 'SNo' , MCLDueColDate,isnull(Paid,0) as 'Collected' ,MCLoanAmount-isnull((select sum(Paid)
from tblMCLoanDueCol
where MCLoanId=x.MCLoanId and MCLDueCollId<=x.MCLDueCollId),0) as 'Balance',PenaltyCollected,SavingsCollected
from tblMCLoanDueCol x
right
join tblMCLoans y on  x.MCLoanId=y.McLoanId
where   y.MCLoanId=${curloanobj.getMCLoanId()}
order by sno asc
```

- Reads: `SNo`, `MCLDueColDate`, `Collected`, `Balance`, `GColId`, `SavingsCollected`, `PenaltyCollected`

#### `asyncsavecollection.doInBackground` · line 833
**SQL Server** · `jdbc.prepareCall` · branch DB · CALL · procedure: `inserttbltransactionsmaster`
```sql
{call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `collectiondate` (setDate) · "TransType" → `constantvalues.MCLoanCollectionTTId.intValue()` (setDouble) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setInt) · "TransAmount" → `penaltycollection + savingscollection + duecollection` (setDouble) · "FWTransId" → `8` (setNull) · "FWVWUDTransId" → `8` (setNull) · "GColId" → `8` (setNull) · "TransLedgerId" → `8` (setNull) · "RouteId" → `globalvars.finid.intValue()` (setDouble)
- OUT: "TransId" (8) · "FWVWTransId" (8)
- Reads: `TransId`

#### `asyncsavecollection.doInBackground` · line 848
**SQL Server** · `jdbc.prepareCall` · branch DB · CALL · procedure: `inserttblmcloangroupcollectionmasterwithtwostaffs`
```sql
{call dbo.InserttblMCLoanGroupCollectionMasterWithTWOStaffs(?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "CollDate" → `collectiondate` (setDate) · "CollAmount" → `duecollection` (setDouble) · "PenaltyCollected" → `penaltycollection` (setDouble) · "SavingsCollected" → `savingscollection` (setDouble) · "CollStaffLedgerId" → `globalvars.MobileUserLedgerId.intValue()` (setInt) · "CollStaff2LedgerId" → `globalvars.MobileUserLedgerId.intValue()` (setInt) · "TransId" → `CollTransId` (setDouble) · "NIPAmountCollected" → `0.0d` (setDouble) · "FinId" → `globalvars.finid.intValue()` (setDouble) · "BankCollection" → `bankcollection` (setInt) · "CashCollection" → `cashcollecton` (setInt)
- OUT: "MCLGCollId" (3) · "FWGColId" (3)
- Reads: `MCLGCollId`

#### `asyncsavecollection.doInBackground` · line 865
**SQL Server** · `jdbc.Statement.execute` · branch DB · ? · tables: `tblMCLoanDueCol`
```sql
${"insert into tblMCLoanDueCol (GColId,MCLDueColDate,MCLoanId,TTId,FinId,Paid,PenaltyCollec…}${globalvars.finid},${duecollection} ,${penaltycollection},${savingscollection},${bankcollection},${cashcollecton});
```

#### `asyncsavecollection.doInBackground` · line 873
**SQL Server** · `jdbc.Statement.execute` · branch DB · ? · tables: `tblTransactionsDetails`
```sql
${CollectionSQLStr + " Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,Led…}${CollTransId},114,${bankcollection},'Source','Cr',3,4); Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo)
values(${cstmt.getInt("TransId")},${globalvars.bankaccountledgerid},${bankcollection},'Target','Dr',4,3);
```

#### `new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 435
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set Collected=${Integer.parseInt(etduecollection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 436
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  CashCollection=${((penaltycollection + savingscollection) + duecollection) - bankcollection}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `new TextWatcher(){}.onTextChanged` · line 272
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  PenaltyCollected=${Integer.parseInt(etpenaltycollection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `new TextWatcher(){}.onTextChanged` · line 273
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set CashCollection=${((penaltycollection + savingscollection) + duecollection) - bankcollection}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `new TextWatcher(){}.onTextChanged` · line 319
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  SavingsCollected=${Integer.parseInt(etsavingscollection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `new TextWatcher(){}.onTextChanged` · line 320
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  BankCollection=${Integer.parseInt(etbankcolection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `new TextWatcher(){}.onTextChanged` · line 321, 366
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  CashCollection=${((penaltycollection + savingscollection) + duecollection) - bankcollection}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `new TextWatcher(){}.onTextChanged` · line 365
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  BankCollection=${TextUtils.isEmpty(etbankcolection.getText().toString()) ? 0 : Integer.parseInt(etbankcole…}
Where MCLoanId=${selloandata.getMCLoanId()}
```

## selectedloancollectiondetails

Screen: **Individual loan collection details (history, print & share)** · [spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md#individual-loan-collection-details-history-print--share--selectedloancollectiondetails) · 4 statements

#### `ASYNCCollections.doInBackground` · line 992
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblMCLoanGroupCollectionMaster`
```sql
Select MCLGCollId, FWGColId, CollDate, CollAmount,  CollStaffLedgerId, TransId, FinId,  NIPAmountCollected, CollStaff2LedgerId ,PenaltyCollected,SavingsCollected
from tblMCLoanGroupCollectionMaster
where MCLGCollId=${selectedcollectiongcolid}
order by CollDate desc
```

- Reads: `MCLGCollId`, `FWGColId`, `CollDate`, `CollAmount`, `CollStaffLedgerId`, `TransId`, `FinId`, `NIPAmountCollected`, `CollStaff2LedgerId`, `PenaltyCollected`, `SavingsCollected`

#### `ASYNCGetCollectionDetails.doInBackground` · line 852
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblMCLoanDueCol`, `tblMCLoans`
```sql
select GColId,Row_Number() over(
Order by MCLDueCollId) as 'SNo' , MCLDueColDate,isnull(Paid,0) as 'Collected' ,MCLoanAmount-isnull((select sum(Paid)
from tblMCLoanDueCol
where MCLoanId=x.MCLoanId and MCLDueCollId<=x.MCLDueCollId),0) as 'Balance',PenaltyCollected,SavingsCollected
from tblMCLoanDueCol x
right
join tblMCLoans y on  x.MCLoanId=y.McLoanId
where   y.MCLoanId=${PassedMCLoanId}
order by sno asc
```

- Reads: `SNo`, `MCLDueColDate`, `Collected`, `Balance`, `GColId`, `SavingsCollected`, `PenaltyCollected`

#### `ASYNCGetCollectionDetails.doInBackground` · line 881
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · APP_GETLOANDETAILSFORALL
```sql
APP_GetLoanDetailsforALL  @MCLoanId =${PassedMCLoanId}
```

- Reads: `MCLoanId`, `FWUDMCLoanId`, `PartyLedgerId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `SubAreaNo`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PrevCollected`, `PaidDues`, `PassedDues`, `interestamount`, `LoanMethodDesc`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`, `PartyName`, `DocCharges`

#### `ASYNCGetCollectionDetails.onPostExecute` · line 921
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SelectedLoanCollectionDetails`
```sql
Create table IF NOT EXISTS SelectedLoanCollectionDetails(SNo int,ColDate varchar,Amount int,Balance int)
```

## tabbedcollectionlist

Screen: **Legacy area-wise collection list (ViewPager2)** · [spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md#legacy-area-wise-collection-list-viewpager2--tabbedcollectionlist) · 3 statements

#### `PlaceholderFragment.oncollectionamountchanged` · line 357
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  BankCollection=${globalvars.tabbedcollectionlistdataList.get(i2).getBankCollection()}
Where MCLoanId=${globalvars.tabbedcollectionlistdataList.get(i2).getMCLoanId()}
```

#### `PlaceholderFragment.oncollectionamountchanged` · line 358
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  cashcollection=${globalvars.tabbedcollectionlistdataList.get(i2).getCashCollection()}
Where MCLoanId=${globalvars.tabbedcollectionlistdataList.get(i2).getMCLoanId()}
```

#### `PlaceholderFragment.onmarkchanged` · line 374
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  ismarked=${ismarked}
Where MCLoanId=${globalvars.tabbedcollectionlistdataList.get(i).getMCLoanId()}
```

## tabbedcollectionlistmviewpager

Screen: **Area-wise collection list (party pages per collection area)** · [spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md#area-wise-collection-list-party-pages-per-collection-area--tabbedcollectionlistmviewpager) · 1 statements

#### `PlaceholderFragment.onmarkchanged` · line 282
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  isMarked=${ismarked}
Where MCLoanId=${globalvars.tabbedcollectionlistdataList.get(i).getMCLoanId()}
```

## tabbedindividualcollectionentry

Screen: **Individual collection entry (per-party swipe form)** · [spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md#individual-collection-entry-per-party-swipe-form--tabbedindividualcollectionentry) · 33 statements

#### `PlaceholderFragment.onActivityResult` · line 1578
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set TargetLedgerId=${Integer.parseInt(data.getExtras().get("valuemember").toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.onCreateView` · line 816, 1313
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select Tag,MobileNo1,MobileNo2 from Parties where LedgerId=${selloandata.getPartyLedgerId()}
```

- Reads: `Tag`, `MobileNo1`, `MobileNo2`

#### `PlaceholderFragment.onCreateView` · line 841, 1323
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `PrevCollections`
```sql
select CollDate,Paid from PrevCollections where MCLoanid =${selloandata.getMCLoanId()} order by RowNo
```

#### `PlaceholderFragment.onCreateView` · line 919, 1334
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `PrevLocations`
```sql
Create table IF NOT EXISTS PrevLocations (MCLoanId int, MCLDueColDate date,lat nvarchar,lng nvarchar)
```

#### `PlaceholderFragment.onCreateView` · line 920, 1335
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `PrevLocations`
```sql
select MCLoanId,MCLDueColDate,lat,lng from PrevLocations where MCLoanid =${selloandata.getMCLoanId()}
```

- Reads: `MCLDueColDate`, `lat`, `lng`

#### `PlaceholderFragment.ASYNCGetInvTransDetails.doInBackground` · line 1632
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GETITEMDETAILSFORLOANS
```sql
getItemDetailsforloans @MCLoanid=${params[0]}
```

- Reads: `SNo`, `ItemCode`, `ItemName`, `BilledQty`, `Rate`, `Itotal`

#### `PlaceholderFragment.new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 763, 1260
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set Collected=${Integer.parseInt(etduecollection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 764, 1261
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  CashCollection=${((penaltycollection + savingscollection) + duecollection) - bankcollection}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new TextWatcher(){}.onTextChanged` · line 529, 1033
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set Collected=${Integer.parseInt(etduecollection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new TextWatcher(){}.onTextChanged` · line 530, 628, 673, 1034, 1132, 1177
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  CashCollection=${((penaltycollection + savingscollection) + duecollection) - bankcollection}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new TextWatcher(){}.onTextChanged` · line 579, 1083
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  PenaltyCollected=${Integer.parseInt(etpenaltycollection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new TextWatcher(){}.onTextChanged` · line 580, 1084
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set CashCollection=${((penaltycollection + savingscollection) + duecollection) - bankcollection}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new TextWatcher(){}.onTextChanged` · line 626, 1130
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  SavingsCollected=${Integer.parseInt(etsavingscollection.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new TextWatcher(){}.onTextChanged` · line 627, 1131
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  BankCollection=${Integer.parseInt(etbankcolection2.getText().toString())}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `PlaceholderFragment.new TextWatcher(){}.onTextChanged` · line 672, 1176
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
Update MobileAppCollData set  BankCollection=${TextUtils.isEmpty(etbankcolection2.getText().toString()) ? 0 : Integer.parseInt(etbankcol…}
Where MCLoanId=${selloandata.getMCLoanId()}
```

#### `SectionsPagerAdapter.getCount` · line 1694
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select MCLoanId  from MobileAppCollData   where SubAreaNo=${tabbedindividualcollectionentry.SubAreaNo}
```

## tabbedcollectionlistadapter

[spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md) · 6 statements

#### `onBindViewHolder` · line 151
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `PrevCollections`
```sql
select CollDate,Paid
from PrevCollections
where MCLoanid =${tabbedcollectionlistdata.getMCLoanId()}
order by RowNo desc
```

#### `MyViewHolder.lambda$new$0` · line 356
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set IsVisited =${isChecked}
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
```

#### `MyViewHolder.new TextWatcher(){}.onTextChanged` · line 338
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set Collected = ${Double.parseDouble(s.toString())}
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getPosition())).getM…}
```

#### `MyViewHolder.new TextWatcher(){}.onTextChanged` · line 339
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set CashCollection = ifnull(Collected,0) + ifnull(PenaltyCollected,0)+ ifnull(SavingsCollected,0)- ifnull(BankCollection,0)
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getPosition())).getM…}
```

#### `MyViewHolder.new View.OnTouchListener(){}.new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 305
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set Collected = ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getPosition())).getI…}
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getPosition())).getM…}
```

#### `MyViewHolder.new View.OnTouchListener(){}.new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 306
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set CashCollection = ifnull(Collected,0) + ifnull(PenaltyCollected,0)+ ifnull(SavingsCollected,0)- ifnull(BankCollection,0)
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getPosition())).getM…}
```

## tabbedcollectionlistadaptermviewpager

[spec](../../brain/modules/04-offline-collection-entry-area-wise-collection.md) · 7 statements

#### `onBindViewHolder` · line 148
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `PrevCollections`
```sql
select CollDate,Paid
from PrevCollections
where MCLoanid =${tabbedcollectionlistdata.getMCLoanId()}
order by RowNo desc
```

#### `MyViewHolder.lambda$new$0` · line 350
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set IsVisited =${isChecked}
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
```

#### `MyViewHolder.lambda$new$1` · line 359
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set isMarked ='false'
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
```

#### `MyViewHolder.lambda$new$1` · line 364
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set isMarked ='true'
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
```

#### `MyViewHolder.new TextWatcher(){}.onTextChanged` · line 334
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set Collected = ${Double.parseDouble(s.toString())}
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
```

#### `MyViewHolder.new View.OnTouchListener(){}.new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 302
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set Collected = ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
```

#### `MyViewHolder.new View.OnTouchListener(){}.new GestureDetector.SimpleOnGestureListener(){}.onDoubleTap` · line 303
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set CashCollection = ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
where  MCLoanId= ${((tabbedcollectionlistdata) tabbedcollectionlistdataListFiltered.get(getAdapterPosition()…}
```

