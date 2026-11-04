# SQL — 8.14 ST Loans and OF Loans (interest-bearing loan books)

[← work.md](../../work.md) · [Module spec](../../brain/modules/14-st-loans-of-loans-interest-bearing.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **87 statements** (20 local SQLite, 67 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`ofloans`](#ofloans) (21) · [`ofloans_interestcalculationandpayments`](#ofloansinterestcalculationandpayments) (21) · [`stloaninterestcalculationandpayments`](#stloaninterestcalculationandpayments) (19) · [`stloans`](#stloans) (26)

## ofloans

Screen: **OF Loan entry / edit** · [spec](../../brain/modules/14-st-loans-of-loans-interest-bearing.md#of-loan-entry--edit--ofloans) · 21 statements

#### `drawPage` · line 1329
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceMaster`
```sql
Select FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, YearFrom, BooksFrom, CashLedgerId, PhoneNo, ShortName, MobileNo, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage, IsUseforHandLoans, IsHideinDashBoard, SMSFinName, EmailIdentifier
from FinanceMaster
where FInId=${tblOFLoans_newdata.getFinId()}
order by FinId
```

- Reads: `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `YearFrom`, `BooksFrom`, `CashLedgerId`, `PhoneNo`, `ShortName`, `MobileNo`, `HLinecolor`, `LoanNoSuffix`, `SCopyImage`, `FCopyImage`, `SMSFinName`, `EmailIdentifier`

#### `drawPage` · line 1410
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2
from tblLedgers
where LedgerId=${tblOFLoans_newdata.getPartyLedgerId()}
order by LedgerId
```

- Reads: `LedgerName`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`

#### `onloanselected` · line 673
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblOFLoans`
```sql
Select TransId, TransDate, OFLoanId, FWOFLoanId, FWUDOFLoanId, PartyLedgerId, OFLoanAmount, OFLIntRateinDM, OFLIntRateinMM, RecPartnerId,  FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, BankRefId, BankRefNo, SourceLedgerId
from tblOFLoans
where OFLoanId=${indvidualloanordepositstatus_data.getSTLoanId()}
order by OFLoanId
```

- Reads: `TransId`, `TransDate`, `OFLoanId`, `FWOFLoanId`, `FWUDOFLoanId`, `PartyLedgerId`, `OFLoanAmount`, `OFLIntRateinDM`, `OFLIntRateinMM`, `RecPartnerId`, `FinId`, `DocCharges`, `Remarks`, `IntCalcStartOn`, `IntCalcStartOnSYSSET`, `BankRefId`, `BankRefNo`, `SourceLedgerId`

#### `ASYNCSaveLoan.doInBackground` · line 967
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblofloanint`
```sql
select count(transid) transidcount from tblofloanint where ofloanid= ${tblOFLoans_newdata.getOFLoanId()}
```

- Reads: `transidcount`

#### `ASYNCSaveLoan.doInBackground` · line 972
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tblOFLoans`, `tbltransactionsdetails`, `tbltransactionsmaster`
```sql
delete
from tbltransactionsdetails
where transid=${tblOFLoans_newdata.getTransId()}; delete
from tblOFLoans
where OFLoanId=${tblOFLoans_newdata.getOFLoanId()}; delete
from tbltransactionsmaster
where transid=${tblOFLoans_newdata.getTransId()}
```

#### `ASYNCSaveLoan.doInBackground` · line 977
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblOFLoans`
```sql
delete from tblOFLoans where OFLoanId=${tblOFLoans_newdata.getOFLoanId()}
```

#### `ASYNCSaveLoan.doInBackground` · line 995
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttransserial`
```sql
{call dbo.InsertTransSerial(?,?,?,?,?,?)}
```

- Params: "FinId" → `tblOFLoans_newdata.getFinId()` (setInt) · "TTypeId" → `1` (setInt) · "TransDate" → `tblOFLoans_newdata.getTransDate()` (setDate)
- OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
- Reads: `TSId`, `TGWTId`, `TGFWTId`

#### `ASYNCSaveLoan.doInBackground` · line 1006
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsmasterformonthly`
```sql
{call dbo.InserttblTransactionsMasterForMonthly( ?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `tblOFLoans_newdata.getTransDate()` (setDate) · "TransType" → `5.0d` (setDouble) · "Narration" → `12` (setNull) · "FinId" → `tblOFLoans_newdata.getFinId()` (setDouble) · "TransAmount" → `tblOFLoans_newdata.getOFLoanAmount()` (setDouble) · "TGWTID" → `TGWTId` (setDouble) · "TGFWTId" → `TGFWTId` (setDouble)
- OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
- Reads: `TransId`

#### `ASYNCSaveLoan.doInBackground` · line 1030
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblofloanswithuserdefinedloanno`
```sql
{call dbo.InserttblOFLoanswithUserDefinedLoanNo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

#### `ASYNCSaveLoan.doInBackground` · line 1033
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblofloans`
```sql
{call dbo.InserttblOFLoans(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransId" → `tblOFLoans_newdata.getTransId()` (setDouble) · "TransDate" → `tblOFLoans_newdata.getTransDate()` (setDate) · "FWUDOFLoanId" → `tblOFLoans_newdata.getFWUDOFLoanId()` (setInt) · "PartyLedgerId" → `tblOFLoans_newdata.getPartyLedgerId()` (setInt) · "OFLoanAmount" → `tblOFLoans_newdata.getOFLoanAmount()` (setInt) · "OFLIntRateinDM" → `tblOFLoans_newdata.getOFLIntRateinDM().doubleValue()` (setDouble) · "OFLIntRateinMM" → `tblOFLoans_newdata.getOFLIntRateinMM().doubleValue()` (setDouble) · "RecPartnerId" → `tblOFLoans_newdata.getRecPartnerId()` (setInt) · "FinId" → `tblOFLoans_newdata.getFinId()` (setInt) · "IntCalcStartOn" → `tblOFLoans_newdata.getIntCalcStartOn()` (setDate) · "IntCalcStartOnSYSSET" → `tblOFLoans_newdata.getIntCalcStartOn()` (setDate) · "CurStat" → `tblOFLoans_newdata.getCurStat().booleanValue()` (setBoolean) · "Remarks" → `tblOFLoans_newdata.getRemarks()` (setString) · "BankRefId" → `tblOFLoans_newdata.getBankRefId()` (setInt) · "BankRefNo" → `tblOFLoans_newdata.getBankRefNo()` (setString) · "BankRefId" → `4` (setNull) · "BankRefNo" → `4` (setNull) · "DocCharges" → `tblOFLoans_newdata.getDocCharges()` (setInt) · "SourceLedgerId" → `tblOFLoans_newdata.getCashorbankledgerid()` (setDouble)
- OUT: "OFLoanId" (8) · "FWUDOFLoanId" (8)
- Reads: `OFLoanId`, `FWUDOFLoanId`

#### `ASYNCSaveLoan.doInBackground` · line 1091
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblOFLoans`
```sql
INSERT INTO tblOFLoans (TransId, TransDate, OFLoanId, FWUDOFLoanId, PartyLedgerId, OFLoanAmount, OFLIntRateinDM, OFLIntRateinMM, RecPartnerId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, BankRefId, BankRefNo, SourceLedgerId)
VALUES (${Integer.valueOf(tblOFLoans_newdata.getTransId())}, ${tblOFLoans_newdata.getTransDate().toString()}, ${Integer.valueOf(tblOFLoans_newdata.getOFLoanId())}, ${Integer.valueOf(tblOFLoans_newdata.getFWUDOFLoanId())}, ${Integer.valueOf(tblOFLoans_newdata.getPartyLedgerId())}, ${Integer.valueOf(tblOFLoans_newdata.getOFLoanAmount())}, ${tblOFLoans_newdata.getOFLIntRateinDM()}, ${tblOFLoans_newdata.getOFLIntRateinMM()}, ${Integer.valueOf(tblOFLoans_newdata.getRecPartnerId())}, ${Integer.valueOf(tblOFLoans_newdata.getFinId())}, ${Integer.valueOf(tblOFLoans_newdata.getDocCharges())}, ${tblOFLoans_newdata.getRemarks()}, ${tblOFLoans_newdata.getIntCalcStartOn().toString()}, ${tblOFLoans_newdata.getIntCalcStartOn().toString()}, ${tblOFLoans_newdata.getCurStat()}, ${Integer.valueOf(tblOFLoans_newdata.getBankRefId())}, ${tblOFLoans_newdata.getBankRefNo()}, ${Integer.valueOf(tblOFLoans_newdata.getCashorbankledgerid())})
```

#### `ASYNCSaveLoan.doInBackground` · line 1097
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `OFLoanStatusforAPP`
```sql
INSERT INTO OFLoanStatusforAPP (OFLoanId, Repaid, Balance, Status)
VALUES (${Integer.valueOf(tblOFLoans_newdata.getFWUDOFLoanId())}, ${Integer.valueOf(i)}, ${Integer.valueOf(tblOFLoans_newdata.getOFLoanAmount())}, Live)
```

#### `ASYNCSaveLoan.doInBackground` · line 1127
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblOFLoans`
```sql
update tblOFLoans set TransId=?,TransDate=?,FWUDOFLoanId=?,PartyLedgerId=?,OFLoanAmount=?,OFLIntRateinDM=?,OFLIntRateinMM=?,RecPartnerId=?,FinId=?,DocCharges=?,Remarks=?,IntCalcStartOn=?,IntCalcStartOnSYSSET=?,CurStat=?,BankRefId=?,BankRefNo=?,SourceLedgerId=?
where OFLoanId=?
```

- Params: 1 → `tblOFLoans_newdata.getTransId()` (setInt) · 2 → `tblOFLoans_newdata.getTransDate()` (setDate) · 3 → `tblOFLoans_newdata.getFWUDOFLoanId()` (setDouble) · 4 → `tblOFLoans_newdata.getPartyLedgerId()` (setDouble) · 5 → `tblOFLoans_newdata.getOFLoanAmount()` (setDouble) · 6 → `tblOFLoans_newdata.getOFLIntRateinDM().doubleValue()` (setDouble) · 7 → `tblOFLoans_newdata.getOFLIntRateinMM().doubleValue()` (setDouble) · 8 → `tblOFLoans_newdata.getRecPartnerId()` (setDouble) · 9 → `tblOFLoans_newdata.getFinId()` (setDouble) · 10 → `tblOFLoans_newdata.getDocCharges()` (setDouble) · 11 → `tblOFLoans_newdata.getRemarks()` (setString) · 12 → `tblOFLoans_newdata.getIntCalcStartOn()` (setDate) · 13 → `tblOFLoans_newdata.getIntCalcStartOn()` (setDate) · 14 → `tblOFLoans_newdata.getCurStat().booleanValue()` (setBoolean) · 15 → `tblOFLoans_newdata.getBankRefId()` (setInt) · 16 → `tblOFLoans_newdata.getBankRefNo()` (setString) · 17 → `tblOFLoans_newdata.getCashorbankledgerid()` (setDouble) · 18 → `tblOFLoans_newdata.getOFLoanId()` (setInt)

#### `ASYNCSaveLoan.doInBackground` · line 1147
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set TransDate=?, FinId=?,TransAmount=? where TransId=?
```

- Params: 1 → `tblOFLoans_newdata.getTransDate()` (setDate) · 2 → `tblOFLoans_newdata.getFinId()` (setDouble) · 3 → `tblOFLoans_newdata.getOFLoanAmount()` (setDouble) · 4 → `tblOFLoans_newdata.getTransId()` (setInt)

#### `ASYNCSaveLoan.doInBackground` · line 1180
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `tblOFLoans`
```sql
UPDATE tblOFLoans SET TransId=${Integer.valueOf(tblOFLoans_newdata.getTransId())}, TransDate=${tblOFLoans_newdata.getTransDate().toString()}, FWUDOFLoanId=${Integer.valueOf(tblOFLoans_newdata.getFWUDOFLoanId())}, PartyLedgerId=${Integer.valueOf(tblOFLoans_newdata.getPartyLedgerId())}, OFLoanAmount=${Integer.valueOf(tblOFLoans_newdata.getOFLoanAmount())}, OFLIntRateinDM=${tblOFLoans_newdata.getOFLIntRateinDM()}, OFLIntRateinMM=${tblOFLoans_newdata.getOFLIntRateinMM()}, RecPartnerId=${Integer.valueOf(tblOFLoans_newdata.getRecPartnerId())}, FinId=${Integer.valueOf(tblOFLoans_newdata.getFinId())}, DocCharges=${Integer.valueOf(tblOFLoans_newdata.getDocCharges())}, Remarks=${tblOFLoans_newdata.getRemarks()}, IntCalcStartOn=${tblOFLoans_newdata.getIntCalcStartOn().toString()}, IntCalcStartOnSYSSET=${tblOFLoans_newdata.getIntCalcStartOn().toString()}, CurStat=${tblOFLoans_newdata.getCurStat()}, BankRefId=${Integer.valueOf(tblOFLoans_newdata.getBankRefId())}, BankRefNo=${tblOFLoans_newdata.getBankRefNo()}, SourceLedgerId=${Integer.valueOf(tblOFLoans_newdata.getCashorbankledgerid())}
WHERE OFLoanId=?
```

- Selection args: `strArr`

#### `ASYNCSaveLoan.doInBackground` · line 1200
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tbltransactionsdetails`
```sql
delete from tbltransactionsdetails where transid=${tblOFLoans_newdata.getTransId()}
```

#### `ASYNCSaveLoan.doInBackground` · line 1201, 1215, 1225, 1234
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsdetails`
```sql
{call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
```

- Params: "TransId" → `tblOFLoans_newdata.getTransId()` (setDouble) · "Sno" → `1.0d` (setDouble) · str6 → `tblOFLoans_newdata.getPartyLedgerId()` (setDouble) · str7 → `"Source"` (setString) · str8 → `tblOFLoans_newdata.getOFLoanAmount()` (setInt) · str9 → `"Cr"` (setString) · str10 → `2.0d` (setDouble)

#### `ASYNCindividualloandetails.doInBackground` · line 764
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofl_showindoflstatus`
```sql
exec OFL_ShowIndOFLStatus @PartyLedgerId= ${transdata.getLedgerId()}
```

- Reads: `PartyLedgerId`, `TransId`, `OFLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`, `TGFWTId`, `TGWTId`

## ofloans_interestcalculationandpayments

Screen: **OF Loan Interest payment** · [spec](../../brain/modules/14-st-loans-of-loans-interest-bearing.md#of-loan-interest-payment--ofloansinterestcalculationandpayments) · 21 statements

#### `drawPage` · line 1006
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceMaster`
```sql
Select FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, YearFrom, BooksFrom, CashLedgerId, PhoneNo, ShortName, MobileNo, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage, IsUseforHandLoans, IsHideinDashBoard, SMSFinName, EmailIdentifier
from FinanceMaster
where FInId=${pagedata.getFinid()}
order by FinId
```

- Reads: `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `YearFrom`, `BooksFrom`, `CashLedgerId`, `PhoneNo`, `ShortName`, `MobileNo`, `HLinecolor`, `LoanNoSuffix`, `SCopyImage`, `FCopyImage`, `SMSFinName`, `EmailIdentifier`

#### `drawPage` · line 1088
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblledgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2
from tblledgers
where LedgerId=${pagedata.getPartyledgerid()}
order by LedgerId
```

- Reads: `LedgerName`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`

#### `ASYNCGetCollectedIntandPriDetails.doInBackground` · line 620
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `oflint_dispintpaydetails`
```sql
execute OFLInt_DispIntpayDetails @PartyLedId= ${receivedparty.getLedgerId()},@SetId=${selectedSETId}
```

- Reads: `PartyLedgerId`, `OFLoanId`, `FinId`, `TransDate`, `IntRate`, `CashLedgerId`, `PType`, `SNo`, `ShortName`, `LedgerName`, `FWUDOFLoanId`, `Loan`, `IntFrom`, `IntUpTo`, `Days`, `CalcInt`, `Paid`, `TransId`, `OFLIntPayId`, `PrincipalAmount`, `TargetLedgerId`, `FWTransId`, `TGWTId`, `TGFWTId`, `Narration`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 728
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttransserial`
```sql
{call dbo.InsertTransSerial(?,?,?,?,?,?)}
```

- Params: "FinId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "TTypeId" → `1` (setInt) · "TransDate" → `defaulttransdate` (setDate)
- OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
- Reads: `TSId`, `TGWTId`, `TGFWTId`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 753
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertdepositinttbltransactionsmaster`
```sql
{call dbo.InsertDepositInttblTransactionsMaster( ?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransType" → `CurTTId` (setInt) · str3 → `str3` (setString) · "FinId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "TransAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "TGWTID" → `TGWTId` (setInt) · "TGFWTId" → `TGFWTId` (setInt) · "TransDate" → `defaulttransdate` (setDate)
- OUT: "FWVWTransId" (4) · "FWVWUDTransId" (4) · "TransId" (4)
- Reads: `TransId`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 771
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set TransDate=?, Narration=?, TransAmount=?  where TransId=?
```

- Params: 1 → `defaulttransdate` (setDate) · 2 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setString) · 3 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 4 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt)

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 777
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblofloanint`
```sql
{call dbo.InserttblOFLoanInt(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "TransDate" → `defaulttransdate` (setDate) · "OFLoanId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "PartyLedgerId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "SNo" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "CurOFLoanAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "PrincipalAmount" → `4` (setNull) · "OFLoanAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "CalcInt" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "PaidInt" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "Days" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "IntFrom" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDate) · "IntUpTo" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDate) · "PrincipalAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "OFLoanAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · "CalcInt" → `4` (setNull) · "PaidInt" → `4` (setNull) · "Days" → `4` (setNull) · "IntFrom" → `91` (setNull) · "IntUpTo" → `91` (setNull) · "FinId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "FinSCashLedgerId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "TargetLedgerId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "IntRate" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · str3 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setString) · "SetId" → `selectedSETId` (setDouble)
- OUT: "OFLIntPayid" (8)
- Reads: `OFLIntPayid`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 810, 871
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete
from tblTransactionsDetails
where TransId=${((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…}
```

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 811, 821, 872, 881
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsdetails`
```sql
{call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
```

- Params: "ledgerid" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "TransId" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "Sno" → `1.0d` (setDouble) · str4 → `"Target"` (setString) · "LedgerAmount" → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · "AmtType" → `"Dr"` (setString) · "AgnstLedgerSNo" → `2.0d` (setDouble)

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 839
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblOFLoanInt`
```sql
update tblOFLoanInt set TransId=?,TransDate=?,OFLoanId=?,PartyLedgerId=?,SNo=?,IntFrom=?,IntUpTo=?,Days=?,PrincipalAmount=?,OFLoanAmount=?,CalcInt=?,PaidInt=?,FinId=?,FinSCashLedgerId=?,TargetLedgerId=?,IntRate=?,Narration=?
where OFLIntPayId=?
```

- Params: 1 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · 2 → `defaulttransdate` (setDate) · 3 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt) · 4 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 5 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 9 → `4` (setNull) · 10 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 11 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 12 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 6 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDate) · 7 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDate) · 8 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 9 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 10 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 11 → `4` (setNull) · 12 → `4` (setNull) · 6 → `4` (setNull) · 7 → `4` (setNull) · 8 → `4` (setNull) · 13 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 14 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 15 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 16 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setDouble) · 17 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setString) · 18 → `((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…` (setInt)

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 902, 903
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tblOFLoanInt`, `tblTransactionsDetails`, `tblTransactionsMaster`
```sql
delete
from tblTransactionsDetails
where TransId=${((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…}; delete
from tblOFLoanInt
where TransId=${((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…}; delete
from tblTransactionsMaster
where TransId=${((ofloans_interestcalculationlist_data) ofloans_interestcalculationlist_dataList.get(pi))…}
```

#### `ASYNCindividualloandetails.doInBackground` · line 492
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofl_showindoflstatus`
```sql
exec OFL_ShowIndOFLStatus @PartyLedgerId= ${receivedparty.getLedgerId()}
```

- Reads: `PartyLedgerId`, `TransId`, `OFLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`

#### `ASYNCindividualloandetails.doInBackground` · line 514
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · EXEC · procedure: `vk_rawoflintselcustomer`
```sql
execute VK_RAWOFLIntSelCustomer @GraceDays= 10,@CustId=${receivedparty.getLedgerId()},@IUPDate = '${interestcalculation_uptodate}'
```

#### `ASYNCindividualloandetails.doInBackground` · line 515
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofl_prepareintpricolllist`
```sql
exec OFL_PrepareIntPriCollList @isAutoIncEnabled=false, @IntRecptDate='${interestcalculation_uptodate}',@PartyLedId=${receivedparty.getLedgerId()}
```

#### `ASYNCindividualloandetails.doInBackground` · line 517
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `ofl_prepareintpricolllistoverallpenalty`
```sql
exec OFL_PrepareIntPriCollListOverAllPenalty @isAutoIncEnabled=false, @IntRecptDate='${interestcalculation_uptodate}',@PartyLedId=${receivedparty.getLedgerId()}
```

- Reads: `PartyLedgerId`, `OFLoanId`, `FinId`, `CashLedgerId`, `IntRate`, `PenaltyRate`, `PType`, `SNo`, `ShortName`, `Loan`, `LedgerName`, `FWUDOFLoanId`, `IntFrom`, `IntUpto`, `Days`, `IsAutoIncEnabled`, `CalcInt`, `CutOffDate`

#### `ASYNCindividualloandetails.doInBackground` · line 556
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `showoflintlist`
```sql
exec ShowOFLIntList @PartyLedId= ${receivedparty.getLedgerId()},@CurPF='${globalvars.accyearfromdate}' ,@CurPT='${globalvars.accyeartodate}'
```

- Reads: `SetId`, `SNo`, `TransDate`, `Principal`, `int`

## stloaninterestcalculationandpayments

Screen: **ST Loan Interest collection** · [spec](../../brain/modules/14-st-loans-of-loans-interest-bearing.md#st-loan-interest-collection--stloaninterestcalculationandpayments) · 19 statements

#### `drawPage` · line 978
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceMaster`
```sql
Select FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, YearFrom, BooksFrom, CashLedgerId, PhoneNo, ShortName, MobileNo, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage, IsUseforHandLoans, IsHideinDashBoard, SMSFinName, EmailIdentifier
from FinanceMaster
where FInId=${pagedata.getFinid()}
order by FinId
```

- Reads: `FinMailingName`, `AddLine1`, `City`

#### `drawPage` · line 1043
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblledgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2
from tblledgers
where LedgerId=${pagedata.getPartyledgerid()}
order by LedgerId
```

- Reads: `LedgerName`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`

#### `ASYNCGetCollectedIntandPriDetails.doInBackground` · line 606
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stlint_dispintpaydetails`
```sql
execute STLInt_DispIntpayDetails @PartyLedId= ${receivedparty.getLedgerId()},@SetId=${selectedSETId}
```

- Reads: `PartyLedgerId`, `STLoanId`, `FinId`, `CurSTLoanAmount`, `TransDate`, `IntRate`, `CashLedgerId`, `PType`, `SNo`, `ShortName`, `LedgerName`, `FWUDSTLoanId`, `Loan`, `IntFrom`, `IntUpTo`, `Days`, `CalcInt`, `Received`, `TransId`, `STLIntPayId`, `FWSTLIntId`, `PrincipalAmount`, `TargetLedgerId`, `FWTransId`, `TGWTId`, `TGFWTId`, `Narration`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 714
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttransserial`
```sql
{call dbo.InsertTransSerial(?,?,?,?,?,?)}
```

- Params: "FinId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getFinId()` (setInt) · "TTypeId" → `1` (setInt) · "TransDate" → `defaulttransdate` (setDate)
- OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
- Reads: `TSId`, `TGWTId`, `TGFWTId`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 741
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertdepositinttbltransactionsmaster`
```sql
{call dbo.InsertDepositInttblTransactionsMaster( ?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransType" → `CurTTId` (setInt) · "Narration" → `"Narration"` (setString) · "FinId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getFinId()` (setInt) · "TransAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` (setInt) · "TGWTID" → `TGWTId` (setInt) · "TGFWTId" → `TGFWTId` (setInt) · "TransDate" → `defaulttransdate` (setDate)
- OUT: "FWVWTransId" (4) · "FWVWUDTransId" (4) · "TransId" (4)
- Reads: `TransId`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 759
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set TransDate=?, Narration=?, TransAmount=?  where TransId=?
```

- Params: 1 → `defaulttransdate` (setDate) · 2 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getNarration()` (setString) · 3 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` (setDouble) · 4 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` (setInt)

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 765
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblstloanint`
```sql
{call dbo.InserttblSTLoanInt(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` (setDouble) · "TransDate" → `defaulttransdate` (setDate) · "STLoanId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getSTLoanId()` (setDouble) · "PartyLedgerId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getPartyLedgerI…` (setDouble) · "SNo" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getSNo()` (setDouble) · "CurSTLoanAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCurrentloana…` (setDouble) · "PrincipalAmount" → `4` (setNull) · "STLoanAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getInterestcalc…` (setInt) · "CalcInt" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCalcInt().in…` (setInt) · "PaidInt" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` (setInt) · "Days" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getDays()` (setInt) · "IntFrom" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntFrom()` (setDate) · "IntUpTo" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntUpto()` (setDate) · "PrincipalAmount" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` (setInt) · "STLoanAmount" → `4` (setNull) · "CalcInt" → `4` (setNull) · "PaidInt" → `4` (setNull) · "Days" → `4` (setNull) · "IntFrom" → `91` (setNull) · "IntUpTo" → `91` (setNull) · "FinId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getFinId()` (setDouble) · "FinSCashLedgerId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCashLedgerId…` (setDouble) · "TargetLedgerId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTargetLedger…` (setDouble) · "IntRate" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntRate().do…` (setDouble) · "Narration" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getNarration()` (setString) · "SetId" → `selectedSETId` (setDouble)
- OUT: "STLIntPayid" (8)
- Reads: `STLIntPayid`

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 798, 854
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tblTransactionsDetails`
```sql
delete
from tblTransactionsDetails
where TransId=${((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()}
```

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 799, 809, 855, 864
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsdetails`
```sql
{call dbo.InserttblTransactionsDetails(?,?,?,?,?,?,?)}
```

- Params: "ledgerid" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCashLedgerId…` (setDouble) · "TransId" → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` (setDouble) · "Sno" → `1.0d` (setDouble) · "LedgerStatus" → `"Target"` (setString) · str2 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` (setDouble) · "AmtType" → `"Dr"` (setString) · "AgnstLedgerSNo" → `2.0d` (setDouble)

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 824
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblSTLoanInt`
```sql
update tblSTLoanInt set TransId=?,TransDate=?,STLoanId=?,PartyLedgerId=?,SNo=?,IntFrom=?,IntUpTo=?,Days=?,PrincipalAmount=?,STLoanAmount=?,CalcInt=?,PaidInt=?,FinId=?,FinSCashLedgerId=?,TargetLedgerId=?,IntRate=?,Narration=?
where STLIntPayId=?
```

- Params: 1 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()` (setInt) · 2 → `defaulttransdate` (setDate) · 3 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getSTLoanId()` (setInt) · 4 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getPartyLedgerI…` (setDouble) · 5 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getSNo()` (setDouble) · 9 → `4` (setNull) · 10 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getInterestcalc…` (setDouble) · 11 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCalcInt().do…` (setDouble) · 12 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` (setDouble) · 6 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntFrom()` (setDate) · 7 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntUpto()` (setDate) · 8 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getDays()` (setDouble) · 9 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getReceived()` (setDouble) · 10 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getInterestcalc…` (setDouble) · 11 → `4` (setNull) · 12 → `4` (setNull) · 6 → `4` (setNull) · 7 → `4` (setNull) · 8 → `4` (setNull) · 13 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getFinId()` (setDouble) · 14 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getCashLedgerId…` (setDouble) · 15 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTargetLedger…` (setDouble) · 16 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getIntRate().do…` (setDouble) · 17 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getNarration()` (setString) · 18 → `((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getSTLIntPayId()` (setInt)

#### `ASYNCSAVECollectedIntandPriDetails.doInBackground` · line 880
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tblSTLoanInt`, `tblTransactionsDetails`, `tblTransactionsMaster`
```sql
delete
from tblTransactionsDetails
where TransId=${((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()}; delete
from tblSTLoanInt
where TransId=${((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()}; delete
from tblTransactionsMaster
where TransId=${((interestcalculationlist_data) interestcalculationlist_dataList.get(pi)).getTransId()}
```

#### `ASYNCindividualloandetails.doInBackground` · line 477
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stl_showindstlstatus`
```sql
exec STL_ShowIndSTLStatus @PartyLedgerId= ${receivedparty.getLedgerId()}
```

- Reads: `PartyLedgerId`, `TransId`, `STLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`

#### `ASYNCindividualloandetails.doInBackground` · line 495
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · EXEC · procedure: `vk_rawstlintselcustomer`
```sql
execute VK_RAWSTLIntSelCustomer  @GraceDays= 10,@CustId=${receivedparty.getLedgerId()},@IUPDate = '${interestcalculation_uptodate}'
```

#### `ASYNCindividualloandetails.doInBackground` · line 496
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stl_prepareintpricolllist`
```sql
exec STL_PrepareIntPriCollList @isAutoIncEnabled=false, @IntRecptDate='${interestcalculation_uptodate}',@PartyLedId=${receivedparty.getLedgerId()}
```

- Reads: `PartyLedgerId`, `STLoanId`, `FinId`, `CashLedgerId`, `IntRate`, `PenaltyRate`, `PType`, `SNo`, `ShortName`, `Loan`, `LedgerName`, `FWUDSTLoanId`, `IntFrom`, `IntUpto`, `Days`, `IsAutoIncEnabled`, `CalcInt`, `CutOffDate`

#### `ASYNCindividualloandetails.doInBackground` · line 542
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `showstlintlist`
```sql
exec ShowSTLIntList @PartyLedId= ${receivedparty.getLedgerId()},@CurPF='${globalvars.accyearfromdate}' ,@CurPT='${globalvars.accyeartodate}'
```

- Reads: `SetId`, `SNo`, `TransDate`, `Principal`, `Int`

## stloans

Screen: **ST Loan entry / edit** · [spec](../../brain/modules/14-st-loans-of-loans-interest-bearing.md#st-loan-entry--edit--stloans) · 26 statements

#### `drawPage` · line 1875
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, YearFrom, BooksFrom, CashLedgerId, PhoneNo, ShortName, MobileNo, HLinecolor, LoanNoSuffix, SCopyImage, FCopyImage , EmailIdentifier
from FinanceDetails
where FInId=${tblSTLoans_newdata.getFinId()}
order by FinId
```

- Reads: `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `YearFrom`, `BooksFrom`, `CashLedgerId`, `PhoneNo`, `ShortName`, `MobileNo`, `HLinecolor`, `LoanNoSuffix`, `SCopyImage`, `FCopyImage`, `EmailIdentifier`

#### `drawPage` · line 1956
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2
from tblLedgers
where LedgerId=${tblSTLoans_newdata.getPartyLedgerId()}
order by LedgerId
```

- Reads: `LedgerName`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `City`, `PinCode`, `MobileNo1`, `MobileNo2`

#### `lambda$showprintandshareoptions$23` · line 788
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `FinanceDetails`
```sql
Select FinId, FinName, FinMailingName, AddLine1, AddLine2, City, PINcode, RegistrationNo, YearFrom, BooksFrom, CashLedgerId, PhoneNo, ShortName, MobileNo, Showid, IsVisible,   AdminMobileNos, DefaultLoanMethodId, FinanceCashBookLedgerId  ,EmailIdentifier
from FinanceDetails
where FInId=${tblSTLoans_newdata.getFinId()}
order by FinId
```

- Reads: `FinName`, `FinMailingName`, `AddLine1`, `AddLine2`, `City`, `PINcode`, `RegistrationNo`, `YearFrom`, `BooksFrom`, `CashLedgerId`, `PhoneNo`, `ShortName`, `MobileNo`, `HLinecolor`, `LoanNoSuffix`, `SCopyImage`, `FCopyImage`, `EmailIdentifier`

#### `lambda$showprintandshareoptions$23` · line 862
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblLedgers`
```sql
Select LedgerId, LedgerName, GroupId, OpeningBalance, OpBalType, FinId, Title, Tag, GType, GuardianName,  AddLine1, AddLine2, City, PinCode, MobileNo1, MobileNo2
from tblLedgers
where LedgerId=${tblSTLoans_newdata.getPartyLedgerId()}
order by LedgerId
```

- Reads: `LedgerName`, `Title`, `Tag`, `GType`, `GuardianName`, `AddLine1`, `AddLine2`, `PinCode`, `MobileNo1`, `MobileNo2`

#### `onloanselected` · line 1155
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `tblSTLoans`
```sql
Select TransId, TransDate, STLoanId, FWSTLoanId, FWUDSTLoanId, PartyLedgerId, STLoanAmount, STLIntRateinDM, STLIntRateinMM, STLPriIntRateinDM, STLSecIntRateinDM, STLTriIntRateinDM, RecPartnerId, RecPartyId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, BankRefId, BankRefNo, SourceLedgerId,DCTargetLedgerId
from tblSTLoans
where STLoanId=${indvidualloanordepositstatus_data.getSTLoanId()}
order by STLoanId
```

- Reads: `TransId`, `TransDate`, `STLoanId`, `FWSTLoanId`, `FWUDSTLoanId`, `PartyLedgerId`, `STLoanAmount`, `STLIntRateinDM`, `STLIntRateinMM`, `STLPriIntRateinDM`, `STLSecIntRateinDM`, `STLTriIntRateinDM`, `RecPartnerId`, `RecPartyId`, `FinId`, `DocCharges`, `Remarks`, `IntCalcStartOn`, `IntCalcStartOnSYSSET`, `BankRefId`, `BankRefNo`, `SourceLedgerId`, `DCTargetLedgerId`

#### `ASYNCSaveLoan.doInBackground` · line 1469
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `tblstloanint`
```sql
select count(transid) transidcount from tblstloanint where stloanid= ${tblSTLoans_newdata.getSTLoanId()}
```

- Reads: `transidcount`

#### `ASYNCSaveLoan.doInBackground` · line 1474
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tblSTLoans`, `tbltransactionsdetails`, `tbltransactionsmaster`
```sql
delete
from tbltransactionsdetails
where transid=${tblSTLoans_newdata.getTransId()}; delete
from tblSTLoans
where STLoanId=${tblSTLoans_newdata.getSTLoanId()}; delete
from tbltransactionsmaster
where transid=${tblSTLoans_newdata.getTransId()}
```

#### `ASYNCSaveLoan.doInBackground` · line 1479
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `tblSTLoans`
```sql
delete from tblSTLoans where STLoanId=${tblSTLoans_newdata.getSTLoanId()}
```

#### `ASYNCSaveLoan.doInBackground` · line 1490
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttransserial`
```sql
{call dbo.InsertTransSerial(?,?,?,?,?,?)}
```

- Params: "FinId" → `tblSTLoans_newdata.getFinId()` (setInt) · "TTypeId" → `1` (setInt) · "TransDate" → `tblSTLoans_newdata.getTransDate()` (setDate)
- OUT: "TSId" (4) · "TGWTId" (4) · "TGFWTId" (4)
- Reads: `TSId`, `TGWTId`, `TGFWTId`

#### `ASYNCSaveLoan.doInBackground` · line 1501
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsmasterformonthly`
```sql
{call dbo.InserttblTransactionsMasterForMonthly(?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransDate" → `tblSTLoans_newdata.getTransDate()` (setDate) · "TransType" → `3.0d` (setDouble) · "Narration" → `12` (setNull) · "FinId" → `tblSTLoans_newdata.getFinId()` (setDouble) · "TransAmount" → `tblSTLoans_newdata.getSTLoanAmount()` (setDouble) · "TGWTID" → `TGWTId` (setDouble) · "TGFWTId" → `TGFWTId` (setDouble)
- OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
- Reads: `TransId`

#### `ASYNCSaveLoan.doInBackground` · line 1527
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblstloanswithuserdefinedloanno`
```sql
{call dbo.InserttblSTLoanswithUserDefinedLoanNo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

#### `ASYNCSaveLoan.doInBackground` · line 1530
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttblstloans`
```sql
{call dbo.InserttblSTLoans(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}
```

- Params: "TransId" → `tblSTLoans_newdata.getTransId()` (setDouble) · "TransDate" → `tblSTLoans_newdata.getTransDate()` (setDate) · "FWSTLoanId" → `tblSTLoans_newdata.getFWSTLoanId()` (setInt) · "FWUDSTLoanId" → `tblSTLoans_newdata.getFWUDSTLoanId()` (setInt) · "PartyLedgerId" → `tblSTLoans_newdata.getPartyLedgerId()` (setInt) · "STLoanAmount" → `tblSTLoans_newdata.getSTLoanAmount()` (setInt) · "STLIntRateinDM" → `tblSTLoans_newdata.getSTLIntRateinDM().doubleValue()` (setDouble) · "STLIntRateinMM" → `tblSTLoans_newdata.getSTLIntRateinMM().doubleValue()` (setDouble) · "STLPriIntRateinDM" → `8` (setNull) · "STLSecIntRateinDM" → `8` (setNull) · "STLTriIntRateinDM" → `8` (setNull) · "RecPartnerId" → `tblSTLoans_newdata.getRecPartnerId()` (setInt) · "RecPartyId" → `tblSTLoans_newdata.getRecPartyId()` (setInt) · "RecPartyId" → `8` (setNull) · "FinId" → `tblSTLoans_newdata.getFinId()` (setInt) · "DocCharges" → `tblSTLoans_newdata.getDocCharges()` (setInt) · "Remarks" → `tblSTLoans_newdata.getRemarks()` (setString) · "IntCalcStartOn" → `tblSTLoans_newdata.getIntCalcStartOn()` (setDate) · "IntCalcStartOnSYSSET" → `tblSTLoans_newdata.getIntCalcStartOn()` (setDate) · "CurStat" → `tblSTLoans_newdata.getCurStat().booleanValue()` (setBoolean) · "BankRefId" → `tblSTLoans_newdata.getBankRefId()` (setInt) · "BankRefNo" → `tblSTLoans_newdata.getBankRefNo()` (setString) · "BankRefId" → `4` (setNull) · "BankRefNo" → `4` (setNull) · "SourceLedgerId" → `tblSTLoans_newdata.getSourceLedgerId()` (setDouble) · "DCTargetLedgerId" → `tblSTLoans_newdata.getDCTargetLedgerId()` (setDouble)
- OUT: "STLoanId" (8) · "FWUDSTLoanId" (8)
- Reads: `STLoanId`, `FWUDSTLoanId`

#### `ASYNCSaveLoan.doInBackground` · line 1603
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `tblSTLoans`
```sql
INSERT INTO tblSTLoans (TransId, TransDate, STLoanId, FWSTLoanId, FWUDSTLoanId, PartyLedgerId, STLoanAmount, STLIntRateinDM, STLIntRateinMM, RecPartnerId, RecPartyId, FinId, DocCharges, Remarks, IntCalcStartOn, IntCalcStartOnSYSSET, CurStat, BankRefId, BankRefNo, SourceLedgerId, DCTargetLedgerId)
VALUES (${Integer.valueOf(tblSTLoans_newdata.getTransId())}, ${tblSTLoans_newdata.getTransDate().toString()}, ${Integer.valueOf(tblSTLoans_newdata.getSTLoanId())}, ${Integer.valueOf(tblSTLoans_newdata.getFWUDSTLoanId())}, ${Integer.valueOf(tblSTLoans_newdata.getFWUDSTLoanId())}, ${Integer.valueOf(tblSTLoans_newdata.getPartyLedgerId())}, ${Integer.valueOf(tblSTLoans_newdata.getSTLoanAmount())}, ${tblSTLoans_newdata.getSTLIntRateinDM()}, ${tblSTLoans_newdata.getSTLIntRateinMM()}, ${Integer.valueOf(tblSTLoans_newdata.getRecPartnerId())}, ${Integer.valueOf(tblSTLoans_newdata.getRecPartyId())}, ${Integer.valueOf(tblSTLoans_newdata.getFinId())}, ${Integer.valueOf(tblSTLoans_newdata.getDocCharges())}, ${tblSTLoans_newdata.getRemarks()}, ${tblSTLoans_newdata.getIntCalcStartOn().toString()}, ${tblSTLoans_newdata.getIntCalcStartOnSYSSET()}, ${tblSTLoans_newdata.getCurStat()}, ${Integer.valueOf(tblSTLoans_newdata.getBankRefId())}, ${tblSTLoans_newdata.getBankRefNo()}, ${Integer.valueOf(tblSTLoans_newdata.getSourceLedgerId())}, ${Integer.valueOf(tblSTLoans_newdata.getDCTargetLedgerId())})
```

#### `ASYNCSaveLoan.doInBackground` · line 1609
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `STLoanStatusforAPP`
```sql
INSERT INTO STLoanStatusforAPP (STLoanId, Repaid, Balance, Status)
VALUES (${Integer.valueOf(tblSTLoans_newdata.getSTLoanId())}, ${(Integer) 0}, ${Integer.valueOf(tblSTLoans_newdata.getSTLoanAmount())}, Live)
```

#### `ASYNCSaveLoan.doInBackground` · line 1637, 1749
**SQL Server** · `jdbc.Statement.executeUpdate` · selected-branch DB · DELETE · tables: `tbltransactionsdetails`
```sql
delete from tbltransactionsdetails where transid=${tblSTLoans_newdata.getTransId()}
```

#### `ASYNCSaveLoan.doInBackground` · line 1638, 1649, 1750, 1761, 1771, 1780
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `inserttbltransactionsdetails`
```sql
{call dbo.Inserttbltransactionsdetails( ?,?,?,?,?,?,?)}
```

- Params: "TransId" → `tblSTLoans_newdata.getTransId()` (setDouble) · "Sno" → `1.0d` (setDouble) · "LedgerId" → `tblSTLoans_newdata.getSourceLedgerId()` (setDouble) · "LedgerStatus" → `"Source"` (setString) · str4 → `tblSTLoans_newdata.getSTLoanAmount()` (setInt) · str5 → `"Cr"` (setString) · str → `2.0d` (setDouble)

#### `ASYNCSaveLoan.doInBackground` · line 1669
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblSTLoans`
```sql
update tblSTLoans set TransId=?,TransDate=?,FWSTLoanId=?,FWUDSTLoanId=?,PartyLedgerId=?,STLoanAmount=?,STLIntRateinDM=?,STLIntRateinMM=?,STLPriIntRateinDM=?,STLSecIntRateinDM=?,STLTriIntRateinDM=?,RecPartnerId=?,RecPartyId=?,FinId=?,DocCharges=?,Remarks=?,IntCalcStartOn=?,IntCalcStartOnSYSSET=?,CurStat=?,BankRefId=?,BankRefNo=?,SourceLedgerId=? ,DCTargetLedgerId=?
where STLoanId=?
```

- Params: 1 → `tblSTLoans_newdata.getTransId()` (setInt) · 2 → `tblSTLoans_newdata.getTransDate()` (setDate) · 3 → `tblSTLoans_newdata.getFWSTLoanId()` (setDouble) · 4 → `tblSTLoans_newdata.getFWUDSTLoanId()` (setDouble) · 5 → `tblSTLoans_newdata.getPartyLedgerId()` (setDouble) · 6 → `tblSTLoans_newdata.getSTLoanAmount()` (setDouble) · 7 → `tblSTLoans_newdata.getSTLIntRateinDM().doubleValue()` (setDouble) · 8 → `tblSTLoans_newdata.getSTLIntRateinMM().doubleValue()` (setDouble) · 9 → `tblSTLoans_newdata.getSTLPriIntRateinDM().doubleValue()` (setDouble) · 10 → `tblSTLoans_newdata.getSTLSecIntRateinDM().doubleValue()` (setDouble) · 11 → `tblSTLoans_newdata.getSTLTriIntRateinDM().doubleValue()` (setDouble) · 12 → `tblSTLoans_newdata.getRecPartnerId()` (setDouble) · 13 → `tblSTLoans_newdata.getRecPartyId()` (setDouble) · 14 → `tblSTLoans_newdata.getFinId()` (setDouble) · 15 → `tblSTLoans_newdata.getDocCharges()` (setDouble) · 16 → `tblSTLoans_newdata.getRemarks()` (setString) · 17 → `tblSTLoans_newdata.getIntCalcStartOn()` (setDate) · 18 → `tblSTLoans_newdata.getIntCalcStartOn()` (setDate) · 19 → `tblSTLoans_newdata.getCurStat().booleanValue()` (setBoolean) · 20 → `tblSTLoans_newdata.getBankRefId()` (setInt) · 21 → `tblSTLoans_newdata.getBankRefNo()` (setString) · 22 → `tblSTLoans_newdata.getSourceLedgerId()` (setDouble) · 23 → `tblSTLoans_newdata.getDCTargetLedgerId()` (setInt) · 24 → `tblSTLoans_newdata.getSTLoanId()` (setInt)

#### `ASYNCSaveLoan.doInBackground` · line 1695
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tblTransactionsMaster`
```sql
update tblTransactionsMaster set TransDate=?, FinId=?,TransAmount=? where TransId=?
```

- Params: 1 → `tblSTLoans_newdata.getTransDate()` (setDate) · 2 → `tblSTLoans_newdata.getFinId()` (setDouble) · 3 → `tblSTLoans_newdata.getSTLoanAmount()` (setDouble) · 4 → `tblSTLoans_newdata.getTransId()` (setInt)

#### `ASYNCSaveLoan.doInBackground` · line 1701
**local SQLite** · `sqlite.compileStatement` · UPDATE · tables: `tblSTLoans`
```sql
Update  tblSTLoans set TransId=?, TransDate=?, STLoanId=?, FWSTLoanId=?, FWUDSTLoanId=?, PartyLedgerId=?, STLoanAmount=?, STLIntRateinDM=?, STLIntRateinMM=?, STLPriIntRateinDM=?, STLSecIntRateinDM=?, STLTriIntRateinDM=?, RecPartnerId=?, RecPartyId=?, FinId=?, DocCharges=?, Remarks=?, IntCalcStartOn=?, IntCalcStartOnSYSSET=?, CurStat=?, BankRefId=?, BankRefNo=?, SourceLedgerId=?,DCTargetLedgerId=?
where STLoanId=?
```

- Params: 1 → `tblSTLoans_newdata.getTransId()` (bindDouble) · 2 → `String.valueOf(tblSTLoans_newdata.getTransDate())` (bindString) · 3 → `tblSTLoans_newdata.getSTLoanId()` (bindDouble) · 4 → `tblSTLoans_newdata.getFWUDSTLoanId()` (bindDouble) · 5 → `tblSTLoans_newdata.getFWUDSTLoanId()` (bindDouble) · 6 → `tblSTLoans_newdata.getPartyLedgerId()` (bindDouble) · 7 → `tblSTLoans_newdata.getSTLoanAmount()` (bindDouble) · 8 → `tblSTLoans_newdata.getSTLIntRateinDM().doubleValue()` (bindDouble) · 9 → `tblSTLoans_newdata.getSTLIntRateinMM().doubleValue()` (bindDouble) · 10 → `tblSTLoans_newdata.getSTLPriIntRateinDM().doubleValue()` (bindDouble) · 11 → `tblSTLoans_newdata.getSTLSecIntRateinDM().doubleValue()` (bindDouble) · 12 → `tblSTLoans_newdata.getSTLTriIntRateinDM().doubleValue()` (bindDouble) · 13 → `tblSTLoans_newdata.getRecPartnerId()` (bindDouble) · 14 → `tblSTLoans_newdata.getRecPartyId()` (bindDouble) · 15 → `tblSTLoans_newdata.getFinId()` (bindDouble) · 16 → `tblSTLoans_newdata.getDocCharges()` (bindDouble) · 17 → `tblSTLoans_newdata.getRemarks()` (bindString) · 18 → `String.valueOf(tblSTLoans_newdata.getIntCalcStartOn())` (bindString) · 19 → `String.valueOf(tblSTLoans_newdata.getIntCalcStartOn())` (bindString) · 20 → `String.valueOf(tblSTLoans_newdata.getCurStat())` (bindString) · 21 → `tblSTLoans_newdata.getBankRefId()` (bindLong) · 22 → `tblSTLoans_newdata.getBankRefNo()` (bindString) · 23 → `tblSTLoans_newdata.getSourceLedgerId()` (bindDouble) · 24 → `tblSTLoans_newdata.getDCTargetLedgerId()` (bindDouble) · 25 → `tblSTLoans_newdata.getSTLoanId()` (bindDouble)

#### `ASYNCindividualloandetails.doInBackground` · line 1256
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `stl_showindstlstatus`
```sql
exec STL_ShowIndSTLStatus @PartyLedgerId= ${curpartyledgerid}
```

- Reads: `PartyLedgerId`, `TransId`, `STLoanId`, `No`, `Date`, `Fin`, `Amount`, `Repaid`, `Balance`, `Status`, `TGFWTId`, `TGWTId`

