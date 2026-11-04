# SQL — 8.17 Inventory module: stock masters, items, customers/suppliers, purchase and stock in hand

[← work.md](../../work.md) · [Module spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **70 statements** (29 local SQLite, 41 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`customerdetails_act`](#customerdetailsact) (6) · [`customers_list`](#customerslist) (4) · [`inventorytransactionslist_act`](#inventorytransactionslistact) (1) · [`invtransactnew`](#invtransactnew) (22) · [`itemmaster_activity`](#itemmasteractivity) (5) · [`itemmaster_list`](#itemmasterlist) (4) · [`stockcat_list`](#stockcatlist) (4) · [`stockcategories`](#stockcategories) (5) · [`stockgroup_act`](#stockgroupact) (5) · [`stockgrp_list`](#stockgrplist) (4) · [`stockinhand_list`](#stockinhandlist) (1) · [`stockUOM_act`](#stockuomact) (5) · [`stockUOM_list`](#stockuomlist) (4)

## customerdetails_act

Screen: **Supplier / customer details** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#supplier--customer-details--customerdetailsact) · 6 statements

#### `ASYNCupdateAccLedgers.doInBackground` · line 256
**SQL Server** · `jdbc.prepareCall` · branch DB · CALL · procedure: `insertsuppliers`
```sql
{call dbo.InsertSuppliers(?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "ledgername" → `receievedcustomer.getLedgerName()` (setString) · "AddLine1" → `receievedcustomer.getAddLine1()` (setString) · "AddLine2" → `receievedcustomer.getAddLine2()` (setString) · "City" → `receievedcustomer.getCity()` (setString) · "PinCode" → `receievedcustomer.getPinCode()` (setString) · "MobileNo1" → `receievedcustomer.getMobileNo1()` (setString) · "MobileNo2" → `receievedcustomer.getMobileNo2()` (setString) · "EMail" → `receievedcustomer.getEMail()` (setString) · "GroupId" → `receievedcustomer.getGroupId()` (setDouble) · "IsDefault" → `receievedcustomer.getIsDefault()` (setDouble) · "IsReserved" → `receievedcustomer.getIsReserved()` (setDouble) · "ScreenType" → `receievedcustomer.getScreenType()` (setDouble)
- OUT: "ledgerid" (8)
- Reads: `ledgerid`

#### `ASYNCupdateAccLedgers.doInBackground` · line 286
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Suppliers`
```sql
INSERT INTO Suppliers (ledgerid, ledgername, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, GroupId, IsDefault, IsReserved, ScreenType)
VALUES (${Integer.valueOf(receievedcustomer.getLedgerId())}, ${receievedcustomer.getLedgerName()}, ${receievedcustomer.getAddLine1()}, ${receievedcustomer.getAddLine2()}, ${receievedcustomer.getCity()}, ${receievedcustomer.getPinCode()}, ${receievedcustomer.getMobileNo1()}, ${receievedcustomer.getMobileNo2()}, ${receievedcustomer.getEMail()}, ${Integer.valueOf(receievedcustomer.getGroupId())}, ${Integer.valueOf(receievedcustomer.getIsDefault())}, ${Integer.valueOf(receievedcustomer.getIsReserved())}, ${Integer.valueOf(receievedcustomer.getScreenType())})
```

#### `ASYNCupdateAccLedgers.doInBackground` · line 291
**SQL Server** · `jdbc.prepareStatement` · branch DB · UPDATE · tables: `tblledgers`
```sql
update tblledgers set ledgername=?,AddLine1=?,AddLine2=?,City=?,PinCode=?,MobileNo1=?,MobileNo2=?,EMail=?
where Ledgerid=?
```

- Params: 1 → `receievedcustomer.getLedgerName()` (setString) · 2 → `receievedcustomer.getAddLine1()` (setString) · 3 → `receievedcustomer.getAddLine2()` (setString) · 4 → `receievedcustomer.getCity()` (setString) · 5 → `receievedcustomer.getPinCode()` (setString) · 6 → `receievedcustomer.getMobileNo1()` (setString) · 7 → `receievedcustomer.getMobileNo2()` (setString) · 8 → `receievedcustomer.getEMail()` (setString) · 9 → `receievedcustomer.getLedgerId()` (setInt)

#### `ASYNCupdateAccLedgers.doInBackground` · line 312
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `Suppliers`
```sql
UPDATE Suppliers SET ledgerid=${Integer.valueOf(receievedcustomer.getLedgerId())}, ledgername=${receievedcustomer.getLedgerName()}, AddLine1=${receievedcustomer.getAddLine1()}, AddLine2=${receievedcustomer.getAddLine2()}, City=${receievedcustomer.getCity()}, PinCode=${receievedcustomer.getPinCode()}, MobileNo1=${receievedcustomer.getMobileNo1()}, MobileNo2=${receievedcustomer.getMobileNo2()}, EMail=${receievedcustomer.getEMail()}
WHERE ledgerid=?
```

- Selection args: `new String[]{String.valueOf(receievedcustomer.getLedgerId())}`

#### `ASYNCupdateAccLedgers.doInBackground` · line 314
**SQL Server** · `jdbc.prepareStatement` · branch DB · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers where ledgerid=?
```

- Params: 1 → `receievedcustomer.getLedgerId()` (setInt)

#### `ASYNCupdateAccLedgers.doInBackground` · line 318
**local SQLite** · `sqlite.compileStatement` · DELETE · tables: `Suppliers`
```sql
delete from  Suppliers where LedgerId=?
```

- Params: 1 → `receievedcustomer.getLedgerId()` (bindDouble)

## customers_list

Screen: **Suppliers list** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#suppliers-list--customerslist) · 4 statements

#### `ASYNCAccountsLedgers.doInBackground` · line 269
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `Suppliers`
```sql
Create table IF NOT EXISTS Suppliers(ledgerid Double,ledgername String,AddLine1 String,AddLine2 String,City String,PinCode String,MobileNo1 String,MobileNo2 String,EMail String,GroupId Double,IsDefault Double,IsReserved Double,ScreenType Double)
```

#### `ASYNCAccountsLedgers.doInBackground` · line 270
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `Suppliers`
```sql
delete from Suppliers
```

#### `ASYNCAccountsLedgers.doInBackground` · line 271
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblledgers`
```sql
Select ledgerid, ledgername, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, GroupId, IsDefault, IsReserved, ScreenType
from tblledgers
where screentype=${screentype}
order by LedgerId
```

- Reads: `ledgerid`, `ledgername`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`, `EMail`, `GroupId`, `IsDefault`

#### `ASYNCAccountsLedgers.doInBackground` · line 289
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Suppliers`
```sql
INSERT INTO Suppliers (ledgerid, ledgername, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, GroupId, IsDefault, IsReserved, ScreenType)
VALUES (${Double.valueOf(reset.getDouble("ledgerid"))}, ${reset.getString("ledgername")}, ${reset.getString("AddLine1")}, ${reset.getString("AddLine2")}, ${reset.getString("City")}, ${reset.getString("PinCode")}, ${reset.getString("MobileNo1")}, ${reset.getString("MobileNo2")}, ${reset.getString("EMail")}, ${Double.valueOf(reset.getDouble("GroupId"))}, ${Double.valueOf(reset.getDouble("IsDefault"))}, ${Double.valueOf(reset.getDouble(str3))}, ${Double.valueOf(reset.getDouble(str4))})
```

## inventorytransactionslist_act

Screen: **Purchase bills list** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#purchase-bills-list--inventorytransactionslistact) · 1 statements

#### `ASYNCGetTranslist.doInBackground` · line 391
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `tblTransactionsmaster`
```sql
select a.Transid, PartyLedgerId as TransLedgerId,FWVWUDTransId ,TransDate ,LedgerName as Particulars  ,TransAmount
From tblTransactionsmaster a,InvTransMaster c, tblLedgers b
where a.TransId =c.TransId  and  c.PartyLedgerId =b.LedgerId and TransDate>='${curdaterange.getFromdate()}' and  TransDate<='${curdaterange.getTodate()}'   and  Transtype=${ttid}
order by TransId desc
```

- Reads: `Transid`, `FWVWUDTransId`, `Particulars`, `TransDate`, `TransAmount`, `TransLedgerId`

## invtransactnew

Screen: **Purchase note entry** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#purchase-note-entry--invtransactnew) · 22 statements

#### `ASYNCGetInvTransDetails.doInBackground` · line 1326
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `InvTransDetails`
```sql
Select SNo ,ItemCode ,Rate ,BilledQty ,Total,  TransId
from InvTransDetails
where  Transid=${curtransaction.getTransId()}
order by SNo
```

- Reads: `SNo`, `Itemcode`, `Rate`, `BilledQty`, `Total`

#### `ASYNCGetNewBillNo.doInBackground` · line 1388
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `transactionsmaster`
```sql
select isnull( max(CWVWUDTransIdforDisp),0)+1 as NewBillNo From transactionsmaster where TTId=11
```

- Reads: `NewBillNo`

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 1512
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `RecentPartieswithContractVehicles`
```sql
Create table IF NOT EXISTS RecentPartieswithContractVehicles(Transid Double,VId Double,PartyLedgerId Double)
```

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 1513
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `RecentPartieswithContractVehicles`
```sql
delete from RecentPartieswithContractVehicles
```

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 1514
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · GETRECENTPARTIESWITHCONTRACTVEHICLES
```sql
GetRecentPartieswithContractVehicles
```

- Reads: `Transid`, `VId`, `PartyLedgerId`

#### `ASYNCRefreshaccountsledgers.doInBackground` · line 1520
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `RecentPartieswithContractVehicles`
```sql
INSERT INTO RecentPartieswithContractVehicles (Transid, VId, PartyLedgerId)
VALUES (${Double.valueOf(reset.getDouble("Transid"))}, ${Double.valueOf(reset.getDouble("VId"))}, ${Double.valueOf(reset.getDouble("PartyLedgerId"))})
```

#### `ASYNC_SaveSalesentry.doInBackground` · line 1133
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertappacctransactionsmaster`
```sql
{call dbo.InsertAPPAccTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "FWVWUDTransId" → `curtransaction.getFWVWUDTransId()` (setInt) · "TransDate" → `curtransaction.getTransDate()` (setDate) · "TransType" → `ttid` (setInt) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setDouble) · "TransAmount" → `curtransaction.getTransAmount()` (setDouble) · "TransLedgerId" → `globalvars.PURCHASE_LEDGERID.intValue()` (setDouble) · "SourceLedgerId" → `curtransaction.getTransLedgerId()` (setDouble) · "ImageFileName" → `4` (setNull)
- OUT: "TransId" (8) · "FWVWTransId" (8)
- Reads: `TransId`

#### `ASYNC_SaveSalesentry.doInBackground` · line 1148, 1226
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `tblTransactionsDetails`
```sql
INSERT INTO tblTransactionsDetails(Sno,LedgerId,LedgerStatus,LedgerAmount,AmtType,AgnstLedgerSNo, TransId)values(?,?,?,?,?,?,? )
```

- Params: 1 → `1` (setInt) · 2 → `curtransaction.getTransLedgerId()` (setInt) · 3 → `"Target"` (setString) · 4 → `curtransaction.getTransAmount()` (setDouble) · 5 → `"Dr"` (setString) · 6 → `2` (setInt) · 7 → `curtransaction.getTransId()` (setInt)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1157, 1235
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `tblTransactionsDetails`
```sql
INSERT INTO tblTransactionsDetails(Sno,LedgerId,LedgerStatus,LedgerAmount,AmtType,AgnstLedgerSNo,TransId)values(?,?,?,?,?,?,?)
```

- Params: 1 → `2` (setInt) · 2 → `globalvars.PURCHASE_LEDGERID.intValue()` (setInt) · 3 → `"Source"` (setString) · 4 → `curtransaction.getTransAmount()` (setDouble) · 5 → `"Cr"` (setString) · 6 → `1` (setInt) · 7 → `curtransaction.getTransId()` (setInt)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1166
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `InvTransMaster`
```sql
INSERT INTO InvTransMaster(NetTotal,TotQty,PartyLedgerId,roundoff,TransId)values( ?,?,?,?,? )
```

- Params: 1 → `curtransaction.getTransAmount()` (setDouble) · 2 → `curtransaction.getTotqty()` (setDouble) · 3 → `curtransaction.getTransLedgerId()` (setDouble) · 4 → `0.0d` (setDouble) · 5 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1176, 1246
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `InvTransDetails`
```sql
INSERT INTO InvTransDetails(SNo ,ItemCode ,Rate ,BilledQty ,Total,  TransId)values( ?,?,?,?,?,? )
```

- Params: 1 → `c2 + 1` (setDouble) · 2 → `((invtransdetails_data) invtransdetails_dataList.get(c2)).getItemcode()` (setDouble) · 3 → `((invtransdetails_data) invtransdetails_dataList.get(c2)).getRate()` (setDouble) · 4 → `((invtransdetails_data) invtransdetails_dataList.get(c2)).getBilledQty()` (setDouble) · 5 → `((invtransdetails_data) invtransdetails_dataList.get(c2)).getTotalAmt()` (setDouble) · 6 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1192
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `InvTransDetails`
```sql
delete from InvTransDetails where TransId=?
```

- Params: 1 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1195
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete from tblTransactionsDetails where TransId=?
```

- Params: 1 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1198
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `InvTransMaster`
```sql
delete from  InvTransMaster where TransId=?
```

- Params: 1 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1201
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE
```sql
delete  tblTransactionsMaster where TransId=?
```

- Params: 1 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1207
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tbltransactionsmaster`
```sql
update tbltransactionsmaster set  FWVWTransId=?,FWVWUDTransId=?,TransDate=?,TransAmount=?  where TransId=?
```

- Params: 1 → `curtransaction.getFWVWUDTransId()` (setDouble) · 2 → `curtransaction.getFWVWUDTransId()` (setInt) · 3 → `curtransaction.getTransDate()` (setDate) · 4 → `curtransaction.getTransAmount()` (setDouble) · 5 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1217
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `InvTransMaster`
```sql
update InvTransMaster set NetTotal=?,TotQty=?,PartyLedgerId=?,roundoff =? where TransId=?
```

- Params: 1 → `curtransaction.getTransAmount()` (setDouble) · 2 → `curtransaction.getTotqty()` (setDouble) · 3 → `curtransaction.getTransLedgerId()` (setDouble) · 4 → `0.0d` (setDouble) · 5 → `curtransaction.getTransId()` (setDouble)

#### `ASYNC_SaveSalesentry.doInBackground` · line 1224
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `InvTransDetails`
```sql
delete  from InvTransDetails  where  TransId=${curtransaction.getTransId()}
```

#### `ASYNC_SaveSalesentry.doInBackground` · line 1225
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete  from tblTransactionsDetails  where  TransId=${curtransaction.getTransId()}
```

## itemmaster_activity

Screen: **Item editor** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#item-editor--itemmasteractivity) · 5 statements

#### `ASYNCupdatestockcat.doInBackground` · line 219
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertitemmaster`
```sql
{call dbo.Insertitemmaster(?,?,?,?,?,?,?)}
```

- Params: "ItemName" → `curitemmasterdata.getItemName()` (setString) · "PriUOMid" → `curitemmasterdata.getPriUOMid()` (setInt) · "Catid" → `curitemmasterdata.getCatid()` (setInt) · "Grpid" → `curitemmasterdata.getGrpid()` (setInt) · "MinimumOrderIdQty" → `curitemmasterdata.getMinimumOrderIdQty()` (setInt) · "ReOrderLevel" → `curitemmasterdata.getReOrderLevel()` (setInt)
- OUT: "itemcode" (8)
- Reads: `itemcode`

#### `ASYNCupdatestockcat.doInBackground` · line 237
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Itemmaster`
```sql
INSERT INTO Itemmaster (ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel)
VALUES (${Integer.valueOf(curitemmasterdata.getItemCode())}, ${curitemmasterdata.getItemName()}, ${Integer.valueOf(curitemmasterdata.getPriUOMid())}, ${Integer.valueOf(curitemmasterdata.getCatid())}, ${Integer.valueOf(curitemmasterdata.getGrpid())}, ${Integer.valueOf(curitemmasterdata.getMinimumOrderIdQty())}, ${Integer.valueOf(curitemmasterdata.getReOrderLevel())})
```

#### `ASYNCupdatestockcat.doInBackground` · line 242
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `Itemmaster`
```sql
update Itemmaster set ItemName=?,PriUOMid=?,Catid=?,Grpid=?,MinimumOrderIdQty=?,ReOrderLevel=?
where itemcode=?
```

- Params: 1 → `curitemmasterdata.getItemName()` (setString) · 2 → `curitemmasterdata.getPriUOMid()` (setInt) · 3 → `curitemmasterdata.getCatid()` (setInt) · 4 → `curitemmasterdata.getGrpid()` (setInt) · 5 → `curitemmasterdata.getMinimumOrderIdQty()` (setInt) · 6 → `curitemmasterdata.getReOrderLevel()` (setInt) · 7 → `curitemmasterdata.getItemCode()` (setInt)

#### `ASYNCupdatestockcat.doInBackground` · line 259
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `Itemmaster`
```sql
UPDATE Itemmaster SET ItemCode=${Integer.valueOf(curitemmasterdata.getItemCode())}, ItemName=${curitemmasterdata.getItemName()}, PriUOMid=${Integer.valueOf(curitemmasterdata.getPriUOMid())}, Catid=${Integer.valueOf(curitemmasterdata.getCatid())}, Grpid=${Integer.valueOf(curitemmasterdata.getGrpid())}, MinimumOrderIdQty=${Integer.valueOf(curitemmasterdata.getMinimumOrderIdQty())}, ReOrderLevel=${Integer.valueOf(curitemmasterdata.getReOrderLevel())}
WHERE itemcode=${curitemmasterdata.getItemCode()}
```

#### `ASYNCupdatestockcat.doInBackground` · line 261
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `Itemmaster`
```sql
DELETE From Itemmaster where itemcode=?
```

- Params: 1 → `curitemmasterdata.getItemCode()` (setInt)

## itemmaster_list

Screen: **Item master list** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#item-master-list--itemmasterlist) · 4 statements

#### `ASYNCAccountsLedgers.doInBackground` · line 193
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `Itemmaster`
```sql
Create table IF NOT EXISTS Itemmaster(ItemCode Int,ItemName String,PriUOMid Int,Catid Int,Grpid Int,MinimumOrderIdQty Int,ReOrderLevel Int)
```

#### `ASYNCAccountsLedgers.doInBackground` · line 194
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `Itemmaster`
```sql
delete from Itemmaster
```

#### `ASYNCAccountsLedgers.doInBackground` · line 195
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `Itemmaster`
```sql
Select ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel
from Itemmaster
order by itemcode
```

- Reads: `ItemCode`, `ItemName`, `PriUOMid`, `Catid`, `Grpid`, `MinimumOrderIdQty`, `ReOrderLevel`

#### `ASYNCAccountsLedgers.doInBackground` · line 205
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Itemmaster`
```sql
INSERT INTO Itemmaster (ItemCode, ItemName, PriUOMid, Catid, Grpid, MinimumOrderIdQty, ReOrderLevel)
VALUES (${Integer.valueOf(reset.getInt("ItemCode"))}, ${reset.getString("ItemName")}, ${Integer.valueOf(reset.getInt("PriUOMid"))}, ${Integer.valueOf(reset.getInt("Catid"))}, ${Integer.valueOf(reset.getInt("Grpid"))}, ${Integer.valueOf(reset.getInt("MinimumOrderIdQty"))}, ${Integer.valueOf(reset.getInt("ReOrderLevel"))})
```

## stockcat_list

Screen: **Stock Categories list** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#stock-categories-list--stockcatlist) · 4 statements

#### `ASYNCAccountsLedgers.doInBackground` · line 194
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `Stockcat`
```sql
Create table IF NOT EXISTS Stockcat(CatId Double,CatDesc String,DisplayIndex Double,IsActive Boolean)
```

#### `ASYNCAccountsLedgers.doInBackground` · line 195
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `Stockcat`
```sql
delete from Stockcat
```

#### `ASYNCAccountsLedgers.doInBackground` · line 196
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `Stockcat`
```sql
Select CatId, CatDesc, DisplayIndex, IsActive from Stockcat order by Catid
```

- Reads: `CatId`, `CatDesc`, `DisplayIndex`, `IsActive`

#### `ASYNCAccountsLedgers.doInBackground` · line 203
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Stockcat`
```sql
INSERT INTO Stockcat (CatId, CatDesc, DisplayIndex, IsActive)
VALUES (${Double.valueOf(reset.getDouble("CatId"))}, ${reset.getString("CatDesc")}, ${Double.valueOf(reset.getDouble("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

## stockcategories

Screen: **Stock Category editor** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#stock-category-editor--stockcategories) · 5 statements

#### `ASYNCupdatestockcat.doInBackground` · line 168
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertstockcat`
```sql
{call dbo.Insertstockcat(?,?,?,?)}
```

- Params: "CatDesc" → `curstockcat.getCatDesc()` (setString) · "DisplayIndex" → `curstockcat.getDisplayIndex()` (setDouble) · "IsActive" → `curstockcat.getActive().booleanValue()` (setBoolean)
- OUT: "catid" (8)
- Reads: `catid`

#### `ASYNCupdatestockcat.doInBackground` · line 180
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockcat`
```sql
INSERT INTO stockcat (CatId, CatDesc, DisplayIndex, IsActive)
VALUES (${Integer.valueOf(curstockcat.getCatId())}, ${curstockcat.getCatDesc()}, ${Integer.valueOf(curstockcat.getDisplayIndex())}, ${curstockcat.getActive()})
```

#### `ASYNCupdatestockcat.doInBackground` · line 185
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `stockcat`
```sql
update stockcat set CatDesc=?,DisplayIndex=?,IsActive=? where Catid=?
```

- Params: 1 → `curstockcat.getCatDesc()` (setString) · 2 → `curstockcat.getDisplayIndex()` (setDouble) · 3 → `curstockcat.getActive().booleanValue()` (setBoolean) · 4 → `curstockcat.getCatId()` (setInt)

#### `ASYNCupdatestockcat.doInBackground` · line 196
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `StockCat`
```sql
UPDATE StockCat SET CatId=${Integer.valueOf(curstockcat.getCatId())}, CatDesc=${curstockcat.getCatDesc()}, DisplayIndex=${Integer.valueOf(curstockcat.getDisplayIndex())}, IsActive=${curstockcat.getActive()}
WHERE Catid=${curstockcat.getCatId()}
```

#### `ASYNCupdatestockcat.doInBackground` · line 198
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `stockcat`
```sql
DELETE From stockcat where Catid=?
```

- Params: 1 → `curstockcat.getCatId()` (setInt)

## stockgroup_act

Screen: **Stock Group editor** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#stock-group-editor--stockgroupact) · 5 statements

#### `ASYNCupdatestockgrp.doInBackground` · line 171
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertstockgrp`
```sql
{call dbo.InsertStockGrp(?,?,?,?,?)}
```

- Params: "GrpDesc" → `curstockgrp.getGrpDesc()` (setString) · "HSNSACCode" → `curstockgrp.getHSNSACCode()` (setDouble) · "DisplayIndex" → `curstockgrp.getDisplayIndex()` (setDouble) · "IsActive" → `curstockgrp.getActive().booleanValue()` (setBoolean)
- OUT: "GrpId" (8)
- Reads: `GrpId`

#### `ASYNCupdatestockgrp.doInBackground` · line 185
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `stockgrp`
```sql
INSERT INTO stockgrp (GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex)
VALUES (${Integer.valueOf(curstockgrp.getGrpId())}, ${curstockgrp.getGrpDesc()}, ${Integer.valueOf(curstockgrp.getHSNSACCode())}, ${curstockgrp.getActive()}, ${Integer.valueOf(curstockgrp.getDisplayIndex())})
```

#### `ASYNCupdatestockgrp.doInBackground` · line 190
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `stockgrp`
```sql
update stockgrp set GrpDesc=?,HSNSACCode=?,IsActive=?,DisplayIndex=? where GrpId=?
```

- Params: 1 → `curstockgrp.getGrpDesc()` (setString) · 2 → `curstockgrp.getHSNSACCode()` (setInt) · 3 → `curstockgrp.getActive().booleanValue()` (setBoolean) · 4 → `curstockgrp.getDisplayIndex()` (setInt) · 5 → `curstockgrp.getGrpId()` (setInt)

#### `ASYNCupdatestockgrp.doInBackground` · line 203
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `stockgrp`
```sql
UPDATE stockgrp SET GrpId=${Integer.valueOf(curstockgrp.getGrpId())}, GrpDesc=${curstockgrp.getGrpDesc()}, HSNSACCode=${Integer.valueOf(curstockgrp.getHSNSACCode())}, IsActive=${curstockgrp.getActive()}, DisplayIndex=${Integer.valueOf(curstockgrp.getDisplayIndex())}
WHERE GrpId=${curstockgrp.getGrpId()}
```

#### `ASYNCupdatestockgrp.doInBackground` · line 205
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `stockgrp`
```sql
DELETE From stockgrp where GrpId=?
```

- Params: 1 → `curstockgrp.getGrpId()` (setInt)

## stockgrp_list

Screen: **Stock Groups list** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#stock-groups-list--stockgrplist) · 4 statements

#### `asyncstockgroupsfromserver.doInBackground` · line 229
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `StockGrp`
```sql
Create table IF NOT EXISTS StockGrp(GrpId int,GrpDesc String,HSNSACCode int,IsActive Boolean,DisplayIndex int)
```

#### `asyncstockgroupsfromserver.doInBackground` · line 230
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `StockGrp`
```sql
delete from StockGrp
```

#### `asyncstockgroupsfromserver.doInBackground` · line 231
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `StockGrp`
```sql
Select GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex from StockGrp order by Grpid
```

- Reads: `GrpId`, `GrpDesc`, `HSNSACCode`, `IsActive`, `DisplayIndex`

#### `asyncstockgroupsfromserver.doInBackground` · line 239
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `StockGrp`
```sql
INSERT INTO StockGrp (GrpId, GrpDesc, HSNSACCode, IsActive, DisplayIndex)
VALUES (${Integer.valueOf(reset.getInt("GrpId"))}, ${reset.getString("GrpDesc")}, ${Double.valueOf(reset.getDouble("HSNSACCode"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))}, ${Integer.valueOf(reset.getInt("DisplayIndex"))})
```

## stockinhand_list

Screen: **Stock in hand** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#stock-in-hand--stockinhandlist) · 1 statements

#### `ASYNCAccountsLedgers.doInBackground` · line 153
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stockinhand`
```sql
exec StockinHand
```

- Reads: `Itemcode`, `ItemName`, `Purchase`, `Issued`, `Balance`

## stockUOM_act

Screen: **Stock UOM editor** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#stock-uom-editor--stockuomact) · 5 statements

#### `ASYNCupdatestockuom.doInBackground` · line 158
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertstockuom`
```sql
{call dbo.InsertStockUOM(?,?,?,?)}
```

- Params: "UOMDesc" → `curstockuom.getUOMDesc()` (setString) · "DisplayIndex" → `curstockuom.getDisplayIndex()` (setDouble) · "IsActive" → `curstockuom.getActive().booleanValue()` (setBoolean)
- OUT: "UOMId" (8)
- Reads: `UOMId`

#### `ASYNCupdatestockuom.doInBackground` · line 170
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `StockUOM`
```sql
INSERT INTO StockUOM (UOMId, UOMDesc, DisplayIndex, IsActive)
VALUES (${Integer.valueOf(curstockuom.getUOMId())}, ${curstockuom.getUOMDesc()}, ${Integer.valueOf(curstockuom.getDisplayIndex())}, ${curstockuom.getActive()})
```

#### `ASYNCupdatestockuom.doInBackground` · line 175
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `StockUOM`
```sql
update StockUOM set UOMDesc=?,DisplayIndex=?,IsActive=? where UOMId=?
```

- Params: 1 → `curstockuom.getUOMDesc()` (setString) · 2 → `curstockuom.getDisplayIndex()` (setDouble) · 3 → `curstockuom.getActive().booleanValue()` (setBoolean) · 4 → `curstockuom.getUOMId()` (setInt)

#### `ASYNCupdatestockuom.doInBackground` · line 186
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `StockUOM`
```sql
UPDATE StockUOM SET UOMId=${Integer.valueOf(curstockuom.getUOMId())}, UOMDesc=${curstockuom.getUOMDesc()}, DisplayIndex=${Integer.valueOf(curstockuom.getDisplayIndex())}, IsActive=${curstockuom.getActive()}
WHERE UOMId=${curstockuom.getUOMId()}
```

#### `ASYNCupdatestockuom.doInBackground` · line 188
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `StockUOM`
```sql
DELETE From StockUOM where UOMId=?
```

- Params: 1 → `curstockuom.getUOMId()` (setInt)

## stockUOM_list

Screen: **Stock UOM list** · [spec](../../brain/modules/17-inventory-module-stock-masters-items-customers.md#stock-uom-list--stockuomlist) · 4 statements

#### `ASYNCAccountsLedgers.doInBackground` · line 185
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `StockUOM`
```sql
Create table IF NOT EXISTS StockUOM(UOMId Double,UOMDesc String,DisplayIndex Double,IsActive Boolean)
```

#### `ASYNCAccountsLedgers.doInBackground` · line 186
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `StockUOM`
```sql
delete from StockUOM
```

#### `ASYNCAccountsLedgers.doInBackground` · line 187
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `StockUOM`
```sql
Select UOMId, UOMDesc, DisplayIndex, IsActive from StockUOM order by UOMid
```

- Reads: `UOMId`, `UOMDesc`, `DisplayIndex`, `IsActive`

#### `ASYNCAccountsLedgers.doInBackground` · line 194
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `StockUOM`
```sql
INSERT INTO StockUOM (UOMId, UOMDesc, DisplayIndex, IsActive)
VALUES (${Double.valueOf(reset.getDouble("UOMId"))}, ${reset.getString("UOMDesc")}, ${Double.valueOf(reset.getDouble("DisplayIndex"))}, ${Boolean.valueOf(reset.getBoolean("IsActive"))})
```

