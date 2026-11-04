# SQL — 8.7 Parties (borrowers), new loan creation and KYC documents

[← work.md](../../work.md) · [Module spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **74 statements** (41 local SQLite, 33 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`documentsphotossearchlist`](#documentsphotossearchlist) (2) · [`documentsupload`](#documentsupload) (4) · [`newloancreation`](#newloancreation) (34) · [`partyandpartnerdetails`](#partyandpartnerdetails) (7) · [`partydetails_act`](#partydetailsact) (19) · [`partydetails_searchlist`](#partydetailssearchlist) (5) · [`partylistwithprevloansfornewloanselection`](#partylistwithprevloansfornewloanselection) (3)

## documentsphotossearchlist

Screen: **Loan documents (photo list)** · [spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md#loan-documents-photo-list--documentsphotossearchlist) · 2 statements

#### `ASYNCRelatedDocslist.doInBackground` · line 264
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `RelatedDocmenttypes`
```sql
select MCLoanId,DCUId, a.DUId,DocDesc,Remarks ,ImagePath
from RelatedDocmenttypes a ,  RelatedDocCollection b
where  (a.DUId=b.DUId and MCLoanId=${receivedloan.getMCLoanId()} and a.isImageAllowed =1)
order by showid
```

- Reads: `DUId`, `DCUId`, `DocDesc`, `Remarks`, `ImagePath`

#### `ASYNCUploaddocumentinfo.doInBackground` · line 218
**SQL Server** · `jdbc.prepareStatement` · branch DB · UPDATE · tables: `RelatedDocCollection`
```sql
update RelatedDocCollection set MCLoanId=?,DUID=?,Remarks=?,ImagePath=? where DCUId=?
```

- Params: 1 → `currentdocument.getMcloanid()` (setInt) · 2 → `currentdocument.getDuid()` (setInt) · 3 → `currentdocument.getRemarks()` (setString) · 4 → `currentdocument.getImagepath()` (setString) · 5 → `currentdocument.getDCUId()` (setInt)

## documentsupload

Screen: **Upload one loan document** · [spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md#upload-one-loan-document--documentsupload) · 4 statements

#### `ASYNCUploaddocumentinfo.doInBackground` · line 248
**SQL Server** · `jdbc.prepareCall` · branch DB · CALL · procedure: `insertimageuploads`
```sql
{call dbo.InsertImageUploads(?,?)}
```

- Params: "Dummy" → `"Dummy"` (setString)
- OUT: "IUId" (4)
- Reads: `IUId`

#### `ASYNCUploaddocumentinfo.doInBackground` · line 258
**SQL Server** · `jdbc.prepareCall` · branch DB · CALL · procedure: `insertrelateddoccollection`
```sql
{call dbo.InsertRelatedDocCollection(?,?,?,?,?)}
```

- Params: "MCLoanId" → `passeddocument.getMcloanid()` (setInt) · "DUID" → `passeddocument.getDuid()` (setInt) · "Remarks" → `passeddocument.getRemarks()` (setString) · "ImagePath" → `passeddocument.getImagepath()` (setString)
- OUT: "DCUId" (4)
- Reads: `DCUId`

#### `ASYNCUploaddocumentinfo.doInBackground` · line 267
**SQL Server** · `jdbc.prepareStatement` · branch DB · UPDATE · tables: `RelatedDocCollection`
```sql
update RelatedDocCollection set MCLoanId=?,DUID=?,Remarks=?,ImagePath=? where DCUId=?
```

- Params: 1 → `passeddocument.getMcloanid()` (setInt) · 2 → `passeddocument.getDuid()` (setInt) · 3 → `passeddocument.getRemarks()` (setString) · 4 → `passeddocument.getImagepath()` (setString) · 5 → `passeddocument.getDCUId()` (setInt)

#### `ASYNCUploaddocumentinfo.doInBackground` · line 282
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `RelatedDocCollection`
```sql
INSERT INTO RelatedDocCollection (DCUId, MCLoanId, DUID, Remarks, ImagePath)
VALUES (${Integer.valueOf(passeddocument.getDCUId())}, ${Integer.valueOf(passeddocument.getMcloanid())}, ${Integer.valueOf(passeddocument.getDuid())}, ${passeddocument.getRemarks()}, ${passeddocument.getImagepath()})
```

## newloancreation

Screen: **New / edit MC loan** · [spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md#new--edit-mc-loan--newloancreation) · 34 statements

#### `SaveLoan` · line 1240
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `NewMCLoans`
```sql
UPDATE NewMCLoans SET MCLoanAmount=${Integer.valueOf(receivedloandata.getMCLoanAmount())}, TotalMonths=${Integer.valueOf(receivedloandata.getTotalMonths())}, DocCharges=${Integer.valueOf(receivedloandata.getDocCharges())}, LoanMethodId=${Integer.valueOf(receivedloandata.getLoanMethodId())}, InterestAmount=${Integer.valueOf(receivedloandata.getInterestAmount())}, MCLoanAmountCollectable=${Integer.valueOf(receivedloandata.getMCLoanAmountCollectable())}, InsAmount=${Integer.valueOf(receivedloandata.getInsAmount())}, ondaycollection=${Integer.valueOf(receivedloandata.getOndaycollection())}, MCLoanAmountEstimated=${Integer.valueOf(receivedloandata.getMCLoanAmountEstimated())}, SubAreaNo=${Integer.valueOf(receivedloandata.getSubAreaNo())}, Remarks=${txtRemarks.getText().toString()}, mobileno=${txtmobileno.getText().toString()}, MCLoanDate=${receivedloandata.getMCLoanDate().toString()}, WeekdayId=${Integer.valueOf(receivedloandata.getWeekDayId())}, Sourceledgerid=${Integer.valueOf(receivedloandata.getSourceledgerid())}, PartyReferenced=${receivedloandata.getPartyreference()}, MonthdayId=${Integer.valueOf(receivedloandata.getMonthDayid())}, IntRate=${Double.valueOf(receivedloandata.getIntrate())}, AdvanceAmount=${Integer.valueOf(receivedloandata.getAdvanceAmount())}, ProductValue=${Integer.valueOf(receivedloandata.getProductsvalue())}, LoanIndex=${Integer.valueOf(receivedloandata.getLoanindex())}
WHERE MCLoanId=${MCLoanIdPassed}
```

#### `SaveLoan` · line 1243
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `RelatedDocCollection`
```sql
UPDATE RelatedDocCollection SET MCLoanId=${Integer.valueOf(receivedloandata.getMCLoanId())}
WHERE MCLoanId = ?
```

- Selection args: `new String[]{"0"}`

#### `SaveLoan` · line 1315
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `NewMCLoans`
```sql
INSERT INTO NewMCLoans (MCLoanId, FinId, PartyLedgerId, MCLoanAmount, TotalMonths, DocCharges, LoanMethodId, InterestAmount, MCLoanAmountCollectable, InsAmount, ondaycollection, MCLoanAmountEstimated, SubAreaNo, ServerGenMCLoanId, ServerGenLedgerId, Remarks, mobileno, ServerGenTransId, MCLoanDate, WeekdayId, Sourceledgerid, PartyReferenced, MonthdayId, ProductValue, LoanIndex, IntRate, AdvanceAmount, ProductValue)
VALUES (${MCLoanIdPassed}, ${globalvars.finid}, ${Integer.valueOf(receivedparty.getLedgerId())}, ${Integer.valueOf(receivedloandata.getMCLoanAmount())}, ${Integer.valueOf(receivedloandata.getTotalMonths())}, ${Integer.valueOf(receivedloandata.getDocCharges())}, ${Integer.valueOf(receivedloandata.getLoanMethodId())}, ${Integer.valueOf(receivedloandata.getInterestAmount())}, ${Integer.valueOf(receivedloandata.getMCLoanAmountCollectable())}, ${Integer.valueOf(receivedloandata.getInsAmount())}, ${Integer.valueOf(receivedloandata.getOndaycollection())}, ${Integer.valueOf(receivedloandata.getMCLoanAmountEstimated())}, ${Integer.valueOf(receivedloandata.getSubAreaNo())}, ${(Integer) 0}, ${Integer.valueOf(receivedparty.getServerGenLedgerId())}, ${txtRemarks.getText().toString()}, ${txtmobileno.getText().toString()}, ${(Integer) 0}, ${receivedloandata.getMCLoanDate().toString()}, ${Integer.valueOf(receivedloandata.getWeekDayId())}, ${Integer.valueOf(receivedloandata.getSourceledgerid())}, ${receivedloandata.getPartyreference()}, ${Integer.valueOf(receivedloandata.getMonthDayid())}, ${Integer.valueOf(receivedloandata.getProductsvalue())}, ${Integer.valueOf(receivedloandata.getLoanindex())}, ${Double.valueOf(receivedloandata.getIntrate())}, ${Integer.valueOf(receivedloandata.getAdvanceAmount())}, ${Integer.valueOf(receivedloandata.getProductsvalue())})
```

#### `SaveLoan` · line 1318
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `RelatedDocCollection`
```sql
UPDATE RelatedDocCollection SET MCLoanId=${MCLoanIdPassed} WHERE MCLoanId = ?
```

- Selection args: `new String[]{"0"}`

#### `deleteloan` · line 1362
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewMCLoans`
```sql
delete from NewMCLoans where MCLoanId=${MCLoanIdPassed}
```

#### `onActivityResult` · line 1111
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `RelatedDocCollection`
```sql
DELETE FROM RelatedDocCollection WHERE MCLoanId = ?
```

- Selection args: `new String[]{String.valueOf(receivedloandata.getMCLoanId())}`

#### `onActivityResult` · line 1121
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `RelatedDocCollection`
```sql
INSERT INTO RelatedDocCollection (DCUId, ServerGenMCLoanId, MCLoanId, DUId, Remarks, ImagePath)
VALUES (${Integer.valueOf(receivedarea.getDCUId())}, ${Integer.valueOf(receivedarea.getServergenmcloanid())}, ${Integer.valueOf(receivedarea.getMcloanid())}, ${Integer.valueOf(receivedarea.getDuid())}, ${receivedarea.getRemarks()}, ${receivedarea.getImagepath()})
```

#### `onCreate` · line 188
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `RelatedDocCollection`
```sql
delete from RelatedDocCollection Where MCLoanId=0 and ServerGenMCLoanId=0
```

#### `onCreate` · line 473
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewItems`
```sql
Select SNo, MCLoanId, Qty, ItemCode,PRate,TotalAmt
from NewItems
where  MCLoanId=${receivedloandata.getMCLoanId()}
order by ItemCode
```

- Reads: `SNo`, `Qty`, `ItemCode`, `PRate`, `TotalAmt`

#### `ASYNCGetInvTransDetails.doInBackground` · line 1694
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `invtransdetails`
```sql
Select TransId, SNo,  ItemCode,  abs(BilledQty) as  BilledQty ,Rate,Total
from invtransdetails c
where Transid=${receivedloandata.getServerGenTransId()}
order by SNo
```

- Reads: `TransId`, `SNo`, `ItemCode`, `BilledQty`, `Rate`, `Total`

#### `ASYNCRelatedDocslist.doInBackground` · line 1395
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · SELECT · tables: `RelatedDocCollection`, `RelatedDocmenttypes`
```sql
select MCLoanId,DCUId, a.DUId, convert(bit, case when b.DCUId is null then 0 else 1 end) as 'Collected',DocDesc,Remarks ,ImagePath
from RelatedDocmenttypes a
Left
Join  RelatedDocCollection b   on  (a.DUId=b.DUId and MCLoanId=${receivedloandata.getServerGenMCLoanId()})  and a.isImageAllowed =1
order by showid
```

- Reads: `DUId`, `DCUId`, `Collected`, `DocDesc`, `Remarks`, `ImagePath`

#### `ASYNCRelatedDocslist.doInBackground` · line 1413
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `RelatedDocCollection`, `RelatedDocmenttypes`
```sql
SELECT MCLoanId, DCUId, a.DUID, CASE WHEN b.DCUId IS NULL THEN 0 ELSE 1 END AS Collected, DocDesc, Remarks, ImagePath
FROM RelatedDocmenttypes a
LEFT
JOIN RelatedDocCollection b ON (a.DUId = b.DUId AND b.ServerGenMCLoanId = ?)
WHERE a.isImageAllowed = 1
ORDER BY showid
```

- Selection args: `String.valueOf(receivedloandata.getServerGenMCLoanId())`
- Reads: `DUID`, `DCUId`, `Collected`, `DocDesc`, `Remarks`, `ImagePath`

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1480
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblMCLoans`
```sql
delete from tblMCLoans where MCloanId=?
```

- Params: 1 → `receivedloandata.getMCLoanId()` (setDouble)

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1484
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete from tblTransactionsDetails where TransId=?
```

- Params: 1 → `receivedloandata.getServerGenTransId()` (setDouble)

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1487
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblTransactionsMaster`
```sql
delete from tblTransactionsMaster where TransId=?
```

- Params: 1 → `receivedloandata.getServerGenTransId()` (setDouble)

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1493
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsmaster`
```sql
{call dbo.InserttblTransactionsMaster(?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `receivedloandata.getMCLoanDate()` (setDate) · "TransType" → `21.0d` (setDouble) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setInt) · "TransAmount" → `receivedloandata.getMCLoanAmountCollectable()` (setDouble) · "FWTransId" → `8` (setNull) · "FWVWUDTransId" → `8` (setNull) · "GColId" → `8` (setNull) · "TransLedgerId" → `receivedloandata.getServerGenMCLoanId()` (setDouble) · "RouteId" → `globalvars.finid.intValue()` (setDouble)
- OUT: "TransId" (8) · "FWVWTransId" (8)
- Reads: `TransId`

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1508
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblmcloanswithweekdayid`
```sql
{call dbo.InserttblMCLoanswithWeekdayId(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ,?,?,?,?,?,?)}
```

- Params: "MCLoanDate" → `receivedloandata.getMCLoanDate()` (setDate) · "FWMCLoanId" → `4` (setNull) · "FWUDMCLoanId" → `4` (setNull) · "FinId" → `globalvars.finid.intValue()` (setDouble) · "PartyLedgerId" → `receivedloandata.getServerGenLedgerId()` (setDouble) · "AgentLedgerId" → `4` (setNull) · "GuaranderLedgerID" → `4` (setNull) · "RecPartnerLedgerID" → `4` (setNull) · "MCLoanAmount" → `receivedloandata.getMCLoanAmount()` (setDouble) · "TotalMonths" → `receivedloandata.getTotalMonths()` (setDouble) · "IntRate" → `receivedloandata.getIntrate()` (setDouble) · "LoanType" → `0.0d` (setDouble) · "CollectionLineId" → `0.0d` (setDouble) · "TransId" → `receivedloandata.getServerGenTransId()` (setDouble) · "DCPercent" → `0.0d` (setDouble) · "DocCharges" → `receivedloandata.getDocCharges()` (setDouble) · "LoanMethodId" → `receivedloandata.getLoanMethodId()` (setDouble) · "InterestAmount" → `receivedloandata.getInterestAmount()` (setDouble) · "MCLoanAmountCollectable" → `receivedloandata.getMCLoanAmountCollectable()` (setDouble) · "InsAmount" → `receivedloandata.getInsAmount()` (setDouble) · "MCLoanAmountEstimated" → `receivedloandata.getMCLoanAmountCollectable()` (setDouble) · "RecPartyLedgerId" → `4` (setNull) · "Remarks" → `receivedloandata.getRemarks()` (setString) · "WeekDayId" → `receivedloandata.getWeekDayId()` (setInt) · "MonthDayId" → `receivedloandata.getMonthDayid()` (setInt) · "mobileno" → `receivedloandata.getMobileno()` (setString) · "Sourceledgerid" → `receivedloandata.getSourceledgerid()` (setInt) · "PartyReferenced" → `receivedloandata.getPartyreference()` (setString) · "loanindex" → `receivedloandata.getLoanindex()` (setInt) · "Productvalue" → `receivedloandata.getProductsvalue()` (setInt) · "AdvanceAmount" → `receivedloandata.getAdvanceAmount()` (setInt)
- OUT: "MCLoanId" (8)
- Reads: `MCLoanId`

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1548
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ?
```sql
${(SQLString + " union all") + " Select " + receivedloandata.getServerGenTransId() + ",5 as…}${receivedloandata.getSourceledgerid()},'Target',${receivedloandata.getDocCharges()},'Dr',6
union all  Select ${receivedloandata.getServerGenTransId()},6 as SNo,104,'Source',${receivedloandata.getDocCharges()},'Cr',5
```

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1557, 1612
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `InvTransDetails`
```sql
INSERT INTO InvTransDetails(SNo ,ItemCode , BilledQty , TransId,Rate,Total)values( ?,?,?,?,?,? )
```

- Params: 1 → `newitem.getSNo()` (setDouble) · 2 → `newitem.getItemcode()` (setDouble) · 3 → `newitem.getBilledQty() * (-1)` (setDouble) · 4 → `receivedloandata.getServerGenTransId()` (setDouble) · 5 → `newitem.getRate()` (setDouble) · 6 → `newitem.getTotalAmt()` (setDouble)

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1567
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `RelatedDocCollection`
```sql
select  DCUId,ServerGenMCLoanId,MCLoanId,DUID,Remarks,ImagePath
From RelatedDocCollection
WHERE ServerGenMCLoanId =0 and MCLoanId = ?
```

- Selection args: `String.valueOf(0)`
- Reads: `DCUId`

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1569
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `RelatedDocCollection`
```sql
Update RelatedDocCollection set MCLoanId=? where DCUId=?
```

- Params: 1 → `receivedloandata.getServerGenMCLoanId()` (setDouble) · 2 → `DOCc.getInt(DOCc.getColumnIndexOrThrow("DCUId"))` (setDouble)

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1575
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `RelatedDocCollection`
```sql
UPDATE RelatedDocCollection SET ServerGenMCLoanId=${Integer.valueOf(receivedloandata.getServerGenMCLoanId())}
WHERE MCLoanId = ?
```

- Selection args: `new String[]{"0"}`

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1580
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set  TransAmount=?,TransDate=?  where TransId=?
```

- Params: 1 → `receivedloandata.getMCLoanAmountCollectable()` (setDouble) · 2 → `receivedloandata.getMCLoanDate()` (setDate) · 3 → `receivedloandata.getServerGenTransId()` (setInt)

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1585
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblMCLoans`
```sql
update tblMCLoans set   MCLoanAmount=?,TotalMonths=?, DocCharges=?,LoanMethodId=?,InterestAmount=?,MCLoanAmountCollectable=?,InsAmount=?,MCLoanAmountEstimated=?,Remarks=?,WeekDayId=?,MonthDayId=?,MCLoanDate=?, Sourceledgerid=?,PartyReferenced=?,Loanindex=?
where MCLoanId=?
```

- Params: 1 → `receivedloandata.getMCLoanAmount()` (setDouble) · 2 → `receivedloandata.getTotalMonths()` (setDouble) · 3 → `receivedloandata.getDocCharges()` (setDouble) · 4 → `receivedloandata.getLoanMethodId()` (setDouble) · 5 → `receivedloandata.getInterestAmount()` (setDouble) · 6 → `receivedloandata.getMCLoanAmountCollectable()` (setDouble) · 7 → `receivedloandata.getInsAmount()` (setInt) · 8 → `receivedloandata.getMCLoanAmountEstimated()` (setDouble) · 9 → `receivedloandata.getRemarks()` (setString) · 10 → `receivedloandata.getWeekDayId()` (setInt) · 11 → `receivedloandata.getMonthDayid()` (setInt) · 12 → `receivedloandata.getMCLoanDate()` (setDate) · 13 → `receivedloandata.getSourceledgerid()` (setInt) · 14 → `receivedloandata.getPartyreference()` (setString) · 15 → `receivedloandata.getLoanindex()` (setInt) · 16 → `receivedloandata.getMCLoanId()` (setInt)

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1607
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ?
```sql
${(SQLString2 + " union all") + " Select " + receivedloandata.getServerGenTransId() + ",5 a…}${receivedloandata.getSourceledgerid()},'Target',${receivedloandata.getDocCharges()},'Dr',6
union all  Select ${receivedloandata.getServerGenTransId()},6 as SNo,104,'Source',${receivedloandata.getDocCharges()},'Cr',5
```

#### `ASYNCUpdateLoaninSQLDB.doInBackground` · line 1608
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `InvTransDetails`
```sql
delete from InvTransDetails where TransId=?
```

- Params: 1 → `receivedloandata.getServerGenTransId()` (setDouble)

#### `Update_sendsms.doInBackground` · line 1744
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedBranchFinanceDetails`
```sql
select AdminMobileNos,smsidentifier
from SelectedBranchFinanceDetails
where finid=${receivedloandata.getFinId()}
```

- Reads: `AdminMobileNos`, `SMSIdentifier`

#### `Update_sendsms.doInBackground` · line 1751
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle varchar,SMSDesc varchar)
```

#### `Update_sendsms.doInBackground` · line 1752
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSGateWay`
```sql
Create table IF NOT EXISTS SMSGateWay(SMSGId int,SMSAPI varchar,APIKey varchar,SMSSenderId varchar,SMSRoute int,SMSType varchar)
```

#### `Update_sendsms.doInBackground` · line 1759
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=8
```

- Reads: `SMSDesc`

#### `delete_sendsms.doInBackground` · line 1825
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSTemplates`
```sql
Create table IF NOT EXISTS SMSTemplates(STId int,SMSTitle varchar,SMSDesc varchar)
```

#### `delete_sendsms.doInBackground` · line 1826
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `SMSGateWay`
```sql
Create table IF NOT EXISTS SMSGateWay(SMSGId int,SMSAPI varchar,APIKey varchar,SMSSenderId varchar,SMSRoute int,SMSType varchar)
```

#### `delete_sendsms.doInBackground` · line 1833
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SMSTemplates`
```sql
select STId,SMSTitle,SMSDesc  from SMSTemplates where STId=9
```

- Reads: `SMSDesc`

## partyandpartnerdetails

Screen: **Party / partner ledger master** · [spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md#party--partner-ledger-master--partyandpartnerdetails) · 7 statements

#### `updatepartiesinremoteandlocal.doInBackground` · line 431
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertimageuploads`
```sql
{call dbo.InsertImageUploads(?,?)}
```

- Params: "Dummy" → `"Dummy"` (setString)
- OUT: "IUId" (4)
- Reads: `IUId`

#### `updatepartiesinremoteandlocal.doInBackground` · line 441
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertpartiesfromapp`
```sql
{call dbo.InsertPartiesfromAPP(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "Ledgername" → `receivedparty.getLedgerName()` (setString) · "tag" → `receivedparty.getTag()` (setString) · "groupid" → `receivedparty.getGroupId()` (setDouble) · "GType" → `receivedparty.getGType()` (setString) · "GuardianName" → `receivedparty.getGuardianName()` (setString) · "AddLine1" → `receivedparty.getAddLine1()` (setString) · "AddLine2" → `receivedparty.getAddLine2()` (setString) · "City" → `receivedparty.getCity()` (setString) · "Pincode" → `receivedparty.getPinCode()` (setString) · "MobileNo1" → `receivedparty.getMobileNo1()` (setString) · "MobileNo2" → `receivedparty.getMobileNo2()` (setString) · 12 → `receivedparty.getMasterPartyLedgerId()` (setDouble) · "MasterPartyLedgerId" → `receivedparty.getMasterPartyLedgerId()` (setDouble) · "MasterPartyLedgerId" → `4` (setNull) · "PanNo" → `receivedparty.getPanNo()` (setString) · "AadhaarNo" → `receivedparty.getAadhaarNo()` (setString) · "Imagefilename" → `receivedparty.getImagefilename()` (setString) · "MartialStatus" → `receivedparty.getMartialStatus()` (setInt) · "Qualification" → `receivedparty.getQualification()` (setString) · "Profession" → `receivedparty.getProfession()` (setString) · "MonthlyIncome" → `receivedparty.getMonthlyIncome()` (setInt) · "GuardianMonthlyIncome" → `receivedparty.getGuardianMonthlyIncome()` (setInt) · "GuardianMobileNo1" → `receivedparty.getGuardianMobileNo1()` (setString) · "GuardianMobileNo2" → `receivedparty.getGuardianMobileNo2()` (setString) · "GuardianProfession" → `receivedparty.getGuardianProfession()` (setString) · "LandMark" → `receivedparty.getLandMark()` (setString) · "ScreenType" → `receivedparty.getScreenType()` (setInt)
- OUT: "ledgerid" (8)
- Reads: `ledgerid`

#### `updatepartiesinremoteandlocal.doInBackground` · line 493
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblLedgers`
```sql
INSERT INTO tblLedgers (LedgerId, LedgerName, GroupId, Title, Tag, GType, GuardianName, AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2, EMail, MasterPartyLedgerId, ScreenType, AadhaarNo)
VALUES (${Integer.valueOf(receivedparty.getLedgerId())}, ${receivedparty.getLedgerName()}, ${(Integer) 1}, ${receivedparty.getTitle()}, ${receivedparty.getTag()}, ${receivedparty.getGType()}, ${receivedparty.getGuardianName()}, ${receivedparty.getAddLine1()}, ${receivedparty.getAddLine2()}, ${receivedparty.getCity()}, ${receivedparty.getPinCode()}, ${receivedparty.getMobileNo1()}, ${receivedparty.getMobileNo2()}, ${receivedparty.getEMail()}, ${Integer.valueOf(receivedparty.getMasterPartyLedgerId())}, ${Integer.valueOf(receivedparty.getScreenType())}, ${receivedparty.getAadhaarNo()})
```

#### `updatepartiesinremoteandlocal.doInBackground` · line 501
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblLedgers`
```sql
update tblLedgers set Ledgername=?,tag=?,groupid=?,GType=?,GuardianName=?,AddLine1=?,AddLine2=?,City=?,Pincode=?,MobileNo1=?,MobileNo2=?,MasterPartyLedgerId=?,PanNo=?,AadhaarNo=?,Imagefilename=?,MartialStatus=?,Qualification=?,Profession=?,MonthlyIncome=?,GuardianMonthlyIncome=?,GuardianMobileNo1=?,GuardianMobileNo2=?,GuardianProfession=?,LandMark=?
where ledgerid=?
```

- Params: 1 → `receivedparty.getLedgerName()` (setString) · 2 → `receivedparty.getTag()` (setString) · 3 → `1.0d` (setDouble) · 4 → `receivedparty.getGType()` (setString) · 5 → `receivedparty.getGuardianName()` (setString) · 6 → `receivedparty.getAddLine1()` (setString) · 7 → `receivedparty.getAddLine2()` (setString) · 8 → `receivedparty.getCity()` (setString) · 9 → `receivedparty.getPinCode()` (setString) · 10 → `receivedparty.getMobileNo1()` (setString) · 11 → `receivedparty.getMobileNo2()` (setString) · 12 → `receivedparty.getMasterPartyLedgerId()` (setDouble) · 12 → `4` (setNull) · 13 → `receivedparty.getPanNo()` (setString) · 14 → `receivedparty.getAadhaarNo()` (setString) · 15 → `receivedparty.getImagefilename()` (setString) · 16 → `receivedparty.getMartialStatus()` (setInt) · 17 → `receivedparty.getQualification()` (setString) · 18 → `receivedparty.getProfession()` (setString) · 19 → `receivedparty.getMonthlyIncome()` (setInt) · 20 → `receivedparty.getGuardianMonthlyIncome()` (setInt) · 21 → `receivedparty.getGuardianMobileNo1()` (setString) · 22 → `receivedparty.getGuardianMobileNo2()` (setString) · 23 → `receivedparty.getGuardianProfession()` (setString) · 24 → `receivedparty.getLandMark()` (setString) · 25 → `receivedparty.getLedgerId()` (setInt)

#### `updatepartiesinremoteandlocal.doInBackground` · line 532
**local SQLite** · `sqlite.compileStatement` · UPDATE · tables: `tblLedgers`
```sql
Update  tblLedgers set ledgerid=?, Ledgername=?, tag=?, groupid=?, GType=?, GuardianName=?, AddLine1=?, AddLine2=?, City=?, Pincode=?, MobileNo1=?, MobileNo2=?, MasterPartyLedgerId=?, PanNo=?, AadhaarNo=?
where LedgerId=?
```

- Params: 1 → `receivedparty.getLedgerId()` (bindDouble) · 2 → `receivedparty.getLedgerName()` (bindString) · 3 → `receivedparty.getTag()` (bindString) · 4 → `1.0d` (bindDouble) · 5 → `receivedparty.getGType()` (bindString) · 6 → `receivedparty.getGuardianName()` (bindString) · 7 → `receivedparty.getAddLine1()` (bindString) · 8 → `receivedparty.getAddLine2()` (bindString) · 9 → `receivedparty.getCity()` (bindString) · 10 → `receivedparty.getPinCode()` (bindString) · 11 → `receivedparty.getMobileNo1()` (bindString) · 12 → `receivedparty.getMobileNo2()` (bindString) · 13 → `receivedparty.getMasterPartyLedgerId()` (bindDouble) · 14 → `receivedparty.getPanNo()` (bindString) · 15 → `receivedparty.getAadhaarNo()` (bindString) · 16 → `receivedparty.getLedgerId()` (bindDouble)

#### `updatepartiesinremoteandlocal.doInBackground` · line 553
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers where ledgerid=?
```

- Params: 1 → `receivedparty.getLedgerId()` (setInt)

#### `updatepartiesinremoteandlocal.doInBackground` · line 557
**local SQLite** · `sqlite.compileStatement` · DELETE · tables: `tblLedgers`
```sql
delete from  tblLedgers where LedgerId=?
```

- Params: 1 → `receivedparty.getLedgerId()` (bindDouble)

## partydetails_act

Screen: **Party (borrower) details & KYC** · [spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md#party-borrower-details--kyc--partydetailsact) · 19 statements

#### `deleteparty` · line 646
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
Select  * from NewMCLoans a where PartyLedgerId=${PartyLedgerIdPassed}
```

#### `deleteparty` · line 661
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties where LedgerId=${PartyLedgerIdPassed}
```

#### `lambda$onCreate$9` · line 569
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `NewParties`
```sql
delete from NewParties where LedgerId=${PartyLedgerIdPassed}
```

#### `lambda$onCreate$9` · line 570
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
Select  * from NewMCLoans a  PartyLedgerId=${PartyLedgerIdPassed}
```

#### `lambda$onCreate$9` · line 572
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `NewParties`
```sql
DELETE FROM NewParties WHERE PartyLedgerId=${PartyLedgerIdPassed}
```

#### `onCreate` · line 297
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
Select  LedgerId, CWLedgerId, LedgerName,IsActive, Tag, a.SubAreaNo, SubAreaName,UDLedgerId, MobileNo1, MobileNo2, AddLine1, SpouseName, FamilyName, ImagefileName, IdProoffileName, lat, lng ,IdProofFileNameII,NoOfLoansAllowed
from Parties a,SubAreaDetails b
where a.SubAreaNo=b.SubAreaNo and LedgerId=${extras.getInt("ledgerid")}
```

- Reads: `LedgerName`, `Tag`, `SubAreaName`, `CWLedgerId`, `MobileNo1`, `MobileNo2`, `AddLine1`, `SpouseName`, `FamilyName`, `lat`, `lng`, `ImagefileName`, `IdProofFileName`, `IdProofFileNameII`, `NoOfLoansAllowed`, `UDLedgerId`, `IsActive`

#### `deletepartiesinremoteandlocal.doInBackground` · line 1859
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tblLedgers`
```sql
delete from tblLedgers where LedgerId=?
```

- Params: 1 → `PartyLedgerIdPassed` (setDouble)

#### `deletepartiesinremoteandlocal.doInBackground` · line 1863
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `Parties`
```sql
DELETE FROM Parties WHERE LedgerId=${receivedparty.getLedgerId()}
```

#### `updatepartiesinremoteandlocal.doInBackground` · line 1549, 1559, 1569
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertimageuploads`
```sql
{call dbo.InsertImageUploads(?,?)}
```

- Params: "Dummy" → `"Dummy"` (setString)
- OUT: "IUId" (4)
- Reads: `IUId`

#### `updatepartiesinremoteandlocal.doInBackground` · line 1585
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblledgers`
```sql
update tblledgers set CWLedgerId=?,LedgerName=?,Tag=?, SubAreaNo=?,UDLedgerId=?,MobileNo1=?,MobileNo2=?,AddLine1=?,SpouseName=?,FamilyName=?,ImagefileName=?,IdProoffileName=?,IdProofFileNameII=?,lat=?,lng=?,NoOfLoansAllowed=?,MaximumAmountAllowed=? , IsActive=?
where ledgerid=?
```

- Params: 1 → `receivedparty.getCWLedgerId().intValue()` (setDouble) · 2 → `receivedparty.getLedgerName()` (setString) · 3 → `receivedparty.getTag()` (setString) · 4 → `receivedparty.getSubAreaNo().intValue()` (setDouble) · 5 → `receivedparty.getUDLedgerId()` (setInt) · 6 → `receivedparty.getMobileNo1()` (setString) · 7 → `receivedparty.getMobileNo2()` (setString) · 8 → `receivedparty.getAddLine1()` (setString) · 9 → `receivedparty.getSpouseName()` (setString) · 10 → `receivedparty.getFamilyName()` (setString) · 11 → `receivedparty.getImagefilename()` (setString) · 12 → `receivedparty.getIdProofFileName()` (setString) · 13 → `receivedparty.getIdProofImageIIfileName()` (setString) · 14 → `receivedparty.getLat()` (setString) · 15 → `receivedparty.getLng()` (setString) · 16 → `receivedparty.getNoOfLoansAllowed()` (setInt) · 17 → `receivedparty.getMaximumAmountAllowed()` (setInt) · 18 → `receivedparty.getIsactive()` (setBoolean) · 19 → `receivedparty.getLedgerId()` (setInt)

#### `updatepartiesinremoteandlocal.doInBackground` · line 1643
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `NewParties`
```sql
UPDATE NewParties SET LedgerId=${Integer.valueOf(receivedparty.getLedgerId())}, CWLedgerId=${receivedparty.getCWLedgerId()}, LedgerName=${receivedparty.getLedgerName()}, Tag=${receivedparty.getTag()}, SubAreaNo=${receivedparty.getSubAreaNo()}, CWLedgerId=${receivedparty.getCWLedgerId()}, UDLedgerId=${Integer.valueOf(receivedparty.getUDLedgerId())}, MobileNo1=${receivedparty.getMobileNo1()}, MobileNo2=${receivedparty.getMobileNo2()}, AddLine1=${receivedparty.getAddLine1()}, SpouseName=${receivedparty.getSpouseName()}, FamilyName=${receivedparty.getFamilyName()}, ImagefileName=${receivedparty.getImagefilename()}, IdProoffileName=${receivedparty.getIdProofFileName()}, IdProofFileNameII=${receivedparty.getIdProofImageIIfileName()}, lat=${receivedparty.getLat()}, lng=${receivedparty.getLng()}, NoOfLoansAllowed=${Integer.valueOf(receivedparty.getNoOfLoansAllowed())}, MaximumAmountAllowed=${Integer.valueOf(receivedparty.getMaximumAmountAllowed())}, ServerGenLedgerId=${(Integer) 0}
WHERE LedgerId=${receivedparty.getLedgerId()}
```

#### `updatepartiesinremoteandlocal.doInBackground` · line 1680
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `NewParties`
```sql
INSERT INTO NewParties (LedgerId, CWLedgerId, LedgerName, Tag, AreaNo, SubAreaNo, CWLedgerId, UDLedgerId, MobileNo1, MobileNo2, AddLine1, SpouseName, FamilyName, ImagefileName, IdProoffileName, IdProofFileNameII, lat, lng, NoOfLoansAllowed, MaximumAmountAllowed, ServerGenLedgerId)
VALUES (${Integer.valueOf(receivedparty.getLedgerId())}, ${receivedparty.getCWLedgerId()}, ${receivedparty.getLedgerName()}, ${receivedparty.getTag()}, ${globalvars.finid}, ${Integer.valueOf(SubAreaNo)}, ${receivedparty.getCWLedgerId()}, ${Integer.valueOf(receivedparty.getUDLedgerId())}, ${receivedparty.getMobileNo1()}, ${receivedparty.getMobileNo2()}, ${receivedparty.getAddLine1()}, ${receivedparty.getSpouseName()}, ${receivedparty.getFamilyName()}, ${receivedparty.getImagefilename()}, ${receivedparty.getIdProofFileName()}, ${receivedparty.getIdProofImageIIfileName()}, ${receivedparty.getLat()}, ${receivedparty.getLng()}, ${Integer.valueOf(receivedparty.getNoOfLoansAllowed())}, ${Integer.valueOf(receivedparty.getMaximumAmountAllowed())}, ${(Integer) 0})
```

#### `updatepartiesinremoteandlocal.doInBackground` · line 1689
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertnewparties`
```sql
{call dbo.InsertNewParties(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "CWLedgerId" → `receivedparty.getCWLedgerId().intValue()` (setDouble) · "LedgerName" → `receivedparty.getLedgerName()` (setString) · "Tag" → `receivedparty.getTag()` (setString) · "AreaNo" → `globalvars.finid.intValue()` (setDouble) · "SubAreaNo" → `SubAreaNo` (setDouble) · "UDLedgerId" → `4` (setNull) · "MobileNo1" → `receivedparty.getMobileNo1()` (setString) · "MobileNo2" → `receivedparty.getMobileNo2()` (setString) · "AddLine1" → `receivedparty.getAddLine1()` (setString) · "SpouseName" → `receivedparty.getSpouseName()` (setString) · "FamilyName" → `receivedparty.getFamilyName()` (setString) · "ImagefileName" → `receivedparty.getImagefilename()` (setString) · "IdProoffileName" → `receivedparty.getIdProofFileName()` (setString) · "lat" → `receivedparty.getLat()` (setString) · "lng" → `receivedparty.getLng()` (setString) · "IdProofFileNameII" → `receivedparty.getIdProofImageIIfileName()` (setString) · "NoOfLoansAllowed" → `receivedparty.getNoOfLoansAllowed()` (setInt) · "MaximumAmountAllowed" → `receivedparty.getMaximumAmountAllowed()` (setInt)
- OUT: "LedgerId" (8)
- Reads: `LedgerId`

#### `updatepartiesinremoteandlocal.doInBackground` · line 1733
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Parties`
```sql
INSERT INTO Parties (LedgerId, CWLedgerId, LedgerName, Tag, SubAreaNo, CWLedgerId, UDLedgerId, MobileNo1, MobileNo2, AddLine1, SpouseName, FamilyName, ImagefileName, IdProoffileName, IdProofFileNameII, lat, lng, NoOfLoansAllowed, MaximumAmountAllowed)
VALUES (${Integer.valueOf(ServerGenLedgerId)}, ${receivedparty.getCWLedgerId()}, ${receivedparty.getLedgerName()}, ${receivedparty.getTag()}, ${Integer.valueOf(SubAreaNo)}, ${receivedparty.getCWLedgerId()}, ${Integer.valueOf(receivedparty.getUDLedgerId())}, ${receivedparty.getMobileNo1()}, ${receivedparty.getMobileNo2()}, ${receivedparty.getAddLine1()}, ${receivedparty.getSpouseName()}, ${receivedparty.getFamilyName()}, ${receivedparty.getImagefilename()}, ${receivedparty.getIdProofFileName()}, ${receivedparty.getIdProofImageIIfileName()}, ${receivedparty.getLat()}, ${receivedparty.getLng()}, ${Integer.valueOf(receivedparty.getNoOfLoansAllowed())}, ${Integer.valueOf(receivedparty.getMaximumAmountAllowed())})
```

#### `updatepartiesinremoteandlocal.doInBackground` · line 1752
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblledgers`
```sql
update tblledgers set CWLedgerId=?,LedgerName=?,Tag=?, SubAreaNo=? ,MobileNo1=?,MobileNo2=?,AddLine1=?,SpouseName=?,FamilyName=?,ImagefileName=?,IdProoffileName=?,IdProofFileNameII=?,lat=?,lng=?,NoOfLoansAllowed=?,MaximumAmountAllowed=?,IsActive=?
where ledgerid=?
```

- Params: 1 → `receivedparty.getCWLedgerId().intValue()` (setDouble) · 2 → `receivedparty.getLedgerName()` (setString) · 3 → `receivedparty.getTag()` (setString) · 4 → `receivedparty.getSubAreaNo().intValue()` (setDouble) · 5 → `receivedparty.getMobileNo1()` (setString) · 6 → `receivedparty.getMobileNo2()` (setString) · 7 → `receivedparty.getAddLine1()` (setString) · 8 → `receivedparty.getSpouseName()` (setString) · 9 → `receivedparty.getFamilyName()` (setString) · 10 → `receivedparty.getImagefilename()` (setString) · 11 → `receivedparty.getIdProofFileName()` (setString) · 12 → `receivedparty.getIdProofImageIIfileName()` (setString) · 13 → `receivedparty.getLat()` (setString) · 14 → `receivedparty.getLng()` (setString) · 15 → `receivedparty.getNoOfLoansAllowed()` (setInt) · 16 → `receivedparty.getMaximumAmountAllowed()` (setInt) · 17 → `receivedparty.getIsactive()` (setBoolean) · 18 → `receivedparty.getLedgerId()` (setInt)

#### `updatepartiesinremoteandlocal.doInBackground` · line 1798
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `Parties`
```sql
UPDATE Parties SET LedgerId=${Integer.valueOf(receivedparty.getLedgerId())}, CWLedgerId=${receivedparty.getCWLedgerId()}, LedgerName=${receivedparty.getLedgerName()}, Tag=${receivedparty.getTag()}, SubAreaNo=${receivedparty.getSubAreaNo()}, CWLedgerId=${receivedparty.getCWLedgerId()}, UDLedgerId=${Integer.valueOf(receivedparty.getUDLedgerId())}, MobileNo1=${receivedparty.getMobileNo1()}, MobileNo2=${receivedparty.getMobileNo2()}, AddLine1=${receivedparty.getAddLine1()}, SpouseName=${receivedparty.getSpouseName()}, FamilyName=${receivedparty.getFamilyName()}, ImagefileName=${receivedparty.getImagefilename()}, IdProoffileName=${receivedparty.getIdProofFileName()}, IdProofFileNameII=${receivedparty.getIdProofImageIIfileName()}, lat=${receivedparty.getLat()}, lng=${receivedparty.getLng()}, NoOfLoansAllowed=${Integer.valueOf(receivedparty.getNoOfLoansAllowed())}, MaximumAmountAllowed=${Integer.valueOf(receivedparty.getMaximumAmountAllowed())}, IsActive=${Boolean.valueOf(receivedparty.getIsactive())}
WHERE LedgerId=${receivedparty.getLedgerId()}
```

#### `updatepartiesinremoteandlocal.doInBackground` · line 1803
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `MobileAppCollData`
```sql
UPDATE MobileAppCollData SET CWLedgerId=${receivedparty.getCWLedgerId()}, PartyName=${receivedparty.getLedgerName()}, UDLedgerId=${Integer.valueOf(receivedparty.getUDLedgerId())}
WHERE PartyLedgerId=${receivedparty.getLedgerId()}
```

## partydetails_searchlist

Screen: **Loan parties admin list** · [spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md#loan-parties-admin-list--partydetailssearchlist) · 5 statements

#### `ASYNCParties.doInBackground` · line 288
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `BranchSubAreaDetails`
```sql
Create table IF NOT EXISTS BranchSubAreaDetails(SubAreaNo Double,SubAreaName varchar,FinId int,IsVisible Boolean,ShowId Double )
```

#### `ASYNCParties.doInBackground` · line 289
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `BranchSubAreaDetails`
```sql
delete from BranchSubAreaDetails
```

#### `ASYNCParties.doInBackground` · line 290
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `SubAreaDetails`
```sql
select   SubAreaNo,SubAreaName,isVisible,ShowId ,FinId
from SubAreaDetails
where finid=${globalvars.finid} and IsVisible='True'
order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`, `FinId`, `IsVisible`, `ShowId`

#### `ASYNCParties.doInBackground` · line 292
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `BranchSubAreaDetails`
```sql
Insert into BranchSubAreaDetails
values (${reset.getDouble("SubAreaNo")},'${reset.getString("SubAreaName")}',${reset.getDouble("FinId")},'${reset.getBoolean("IsVisible")}',${reset.getDouble("ShowId")})
```

#### `ASYNCParties.doInBackground` · line 295
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SELECT · tables: `tblLedgers`
```sql
select LedgerId,CWLedgerId,SubAreaName,LedgerName,Tag,AddLine1,AddLine2,MobileNo1,MobileNo2,Imagefilename,a.SubAreaNo,SpouseName,FamilyName,IdProoffileName,IdProofFileNameII,lat,lng ,NoOfLoansAllowed ,MaximumAmountAllowed,IsActive
from tblLedgers a,SubAreaDetails b
where a.SubAreaNo=b.SubAreaNo and Screentype=3 and b.FinId=${financeid}
order by CWLedgerId
```

- Reads: `AddLine1`, `lat`, `lng`, `IdProofFileNameII`, `CWLedgerId`, `SubAreaNo`, `AddLine2`, `MobileNo1`, `MobileNo2`, `LedgerName`, `SubAreaName`, `Tag`, `SpouseName`, `FamilyName`, `Imagefilename`, `IdProoffileName`, `LedgerId`, `NoOfLoansAllowed`, `MaximumAmountAllowed`, `IsActive`

## partylistwithprevloansfornewloanselection

Screen: **Party picker for a new loan (per collection area)** · [spec](../../brain/modules/07-parties-borrowers-new-loan-creation-kyc.md#party-picker-for-a-new-loan-per-collection-area--partylistwithprevloansfornewloanselection) · 3 statements

#### `fetchContacts` · line 129
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `Parties`
```sql
select distinct(LedgerId) as LedgerId, LedgerName,Tag,UDLedgerId,SubAreaNo,CWLedgerId,MobileNo1,MobileNo2 ,PartyLedgerId,MaximumAmountAllowed
from (select a.*,ifnull(b.PartyLedgerId,0) as PartyLedgerId,MobileNo1,MobileNo2,a.CWLedgerId
from(select LedgerId,LedgerName,Tag,UDLedgerId,SubAreaNo,CWLedgerId,MobileNo1,MobileNo2,MaximumAmountAllowed
from Parties
where isactive=1 and SubAreaNo=${SubAreaNo}  ) a
left
Join MobileAppCollData b on a.LedgerId=b.PartyLedgerId)x
```

- Reads: `PartyLedgerId`, `LedgerId`, `CWLedgerId`, `LedgerName`, `MobileNo1`, `MobileNo2`, `Tag`, `SubAreaNo`, `MaximumAmountAllowed`

#### `fetchContacts` · line 149
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `NewParties`
```sql
select distinct(LedgerId) as LedgerId, LedgerName,Tag,UDLedgerId,SubAreaNo,CWLedgerId,MobileNo1,MobileNo2 ,PartyLedgerId,MaximumAmountAllowed ,NoOfLoansAllowed
from (select a.*,ifnull(b.PartyLedgerId,0) as PartyLedgerId,MobileNo1,MobileNo2,a.CWLedgerId
from ( select LedgerId,LedgerName,Tag,1000 as UDLedgerId,SubAreaNo, CWLedgerId,MobileNo1,MobileNo2 ,MaximumAmountAllowed,NoOfLoansAllowed
from NewParties
where SubAreaNo=${SubAreaNo} ) a
left
Join MobileAppCollData b on a.LedgerId=b.PartyLedgerId)x
```

- Reads: `PartyLedgerId`, `LedgerId`, `CWLedgerId`, `LedgerName`, `MobileNo1`, `MobileNo2`, `Tag`, `SubAreaNo`, `NoOfLoansAllowed`, `MaximumAmountAllowed`

#### `onContactSelected` · line 246
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
Select count(MCLoanId) as loanscount  from NewMCLoans  where PartyLedgerId=${partydetails_data.getLedgerId()}
```

- Reads: `loanscount`

