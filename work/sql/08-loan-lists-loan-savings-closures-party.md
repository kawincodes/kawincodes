# SQL — 8.8 Loan lists, loan & savings closures, party transfer and rearrangement

[← work.md](../../work.md) · [Module spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **75 statements** (34 local SQLite, 41 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`closableloanssearchlistact`](#closableloanssearchlistact) (8) · [`closablesavings_list`](#closablesavingslist) (1) · [`closed_renewedloanslist_act`](#closedrenewedloanslistact) (3) · [`closingsavingsaccount`](#closingsavingsaccount) (5) · [`loanclosoureentry`](#loanclosoureentry) (14) · [`loanindexloanswiselist_act`](#loanindexloanswiselistact) (4) · [`loanslist_act`](#loanslistact) (9) · [`newloanslist_act`](#newloanslistact) (2) · [`newpartieslist_act`](#newpartieslistact) (1) · [`newpartiesnewloanslist_act`](#newpartiesnewloanslistact) (3) · [`partyrearrange`](#partyrearrange) (6) · [`partytransfer`](#partytransfer) (4) · [`prevloanclosure_list_act`](#prevloanclosurelistact) (1) · [`prevloanslist_act`](#prevloanslistact) (10) · [`prevsavingclosure_list_act`](#prevsavingclosurelistact) (4)

## closableloanssearchlistact

Screen: **Live loans picker for closure** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#live-loans-picker-for-closure--closableloanssearchlistact) · 8 statements

#### `getclosableloans` · line 210
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `closableliveloans`
```sql
Create table IF NOT EXISTS closableliveloans(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate date,ClosingDate String,CWLedgerId Double,UDLedgerId Int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount Int,MCLoanAmountCollectable Double,PrevCollected Int,PaidDues Int,PassedDues Int,LoanMethodDesc String,ReportIdentification String,CollectionBreakString String,NoOfIns Double,CBCount Int,istobeWarned Int,IsNIP Int,LoanStatus Double,SubAreaName String)
```

#### `getclosableloans` · line 211
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `closableliveloans`
```sql
Select MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, SubAreaName, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, PaidDues, PassedDues, LoanMethodDesc, ReportIdentification, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus
from closableliveloans
order by MCLoanId
```

- Reads: `MCLoanId`, `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `ClosingDate`, `MCLoanDate`, `CWLedgerId`, `UDLedgerId`, `PartyName`, `SubAreaNo`, `SubAreaName`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PrevCollected`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `ReportIdentification`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`

#### `onCreate` · line 88
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `tempLoanClosure`
```sql
Create table IF NOT EXISTS tempLoanClosure(MCLoanId int,PartyLedgerId int,LedgerName String,SubAreaName String,MCLoanAmountCollectable int,MCLoanDate string,ReportIdentification String)
```

#### `ASYNCLoanClosures.doInBackground` · line 279
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `closableliveloans`
```sql
Create table IF NOT EXISTS closableliveloans(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate date,ClosingDate String,CWLedgerId Double,UDLedgerId Int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount Int,MCLoanAmountCollectable Double,PrevCollected Int,PaidDues Int,PassedDues Int,LoanMethodDesc String,ReportIdentification String,CollectionBreakString String,NoOfIns Double,CBCount Int,istobeWarned Int,IsNIP Int,LoanStatus Double,SubAreaName String)
```

#### `ASYNCLoanClosures.doInBackground` · line 280
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `closableliveloans`
```sql
delete from closableliveloans
```

#### `ASYNCLoanClosures.doInBackground` · line 284
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · ?
```sql
[APP_ListofLiveLoanswithWeekdayid]  @ColDate='31-Dec-2050',@weekdayid =${weekdayid}, @FinId=${loanclosurefinId}
```

#### `ASYNCLoanClosures.doInBackground` · line 286
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · ?
```sql
[APP_ListofLiveLoans]  @ColDate='31-Dec-2050', @FinId=${loanclosurefinId}
```

#### `ASYNCLoanClosures.doInBackground` · line 337
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `closableliveloans`
```sql
INSERT INTO closableliveloans (MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, PaidDues, PassedDues, LoanMethodDesc, ReportIdentification, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus, SubAreaName)
VALUES (${Double.valueOf(reset.getDouble("MCLoanId"))}, ${Double.valueOf(reset.getDouble("FWUDMCLoanId"))}, ${reset.getString("Remarks")}, ${Double.valueOf(reset.getDouble("PartyLedgerId"))}, ${String.valueOf(reset.getDate(str4))}, ${String.valueOf(reset.getDate(str2))}, ${Double.valueOf(reset.getDouble("CWLedgerId"))}, ${Integer.valueOf(reset.getInt("UDLedgerId"))}, ${reset.getString("PartyName")}, ${Double.valueOf(reset.getDouble("SubAreaNo"))}, ${Double.valueOf(reset.getDouble("LAmount"))}, ${Double.valueOf(reset.getDouble(str13))}, ${Integer.valueOf(reset.getInt(str14))}, ${Double.valueOf(reset.getDouble(str16))}, ${Integer.valueOf(reset.getInt(str17))}, ${Integer.valueOf(reset.getInt(str18))}, ${Integer.valueOf(reset.getInt(str19))}, ${reset.getString(str20)}, ${reset.getString(str21)}, ${reset.getString(str22)}, ${Double.valueOf(reset.getDouble(str23))}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Integer.valueOf(reset.getInt("istobeWarned"))}, ${Integer.valueOf(reset.getInt("IsNIP"))}, ${Double.valueOf(reset.getDouble("LoanStatus"))}, ${reset.getString("SubAreaName")})
```

## closablesavings_list

Screen: **Live saving accounts** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#live-saving-accounts--closablesavingslist) · 1 statements

#### `ASYNCLoanClosures.doInBackground` · line 178
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GETCLEARABLESAVINGACCOUNTS
```sql
GetClearableSavingAccounts @FinId=${loanclosurefinId}
```

- Reads: `LedgerId`, `SubAreaNo`, `LedgerName`, `SubAreaName`, `CWLedgerId`, `Tag`, `TargetLedgerId`, `SavingsAmount`

## closed_renewedloanslist_act

Screen: **Parties to be Reviewed (closed & renewed loans)** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#parties-to-be-reviewed-closed--renewed-loans--closedrenewedloanslistact) · 3 statements

#### `ASYNCGetClosedandRenewedloans.doInBackground` · line 444
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `niploansclosed`
```sql
exec NIPLoansClosed @IMEI='${globalvars.device_id}',  @FinId=${selectedfinanceid}
```

#### `ASYNCGetClosedandRenewedloans.doInBackground` · line 450
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `loanclosedandrenewedforwindowsapp`
```sql
exec LoanClosedandRenewedforwindowsAPP @fromDate='${fromdate}',  @ToDate='${todate}', @FinId=${selectedfinanceid}
```

#### `ASYNCGetClosedandRenewedloans.doInBackground` · line 452
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `loanclosedandrenewedwithweekdayid`
```sql
exec LoanClosedandRenewedwithweekdayid  @weekdayid= ${weekdayid}, @fromDate='${fromdate}',  @ToDate='${todate}', @FinId=${selectedfinanceid}
```

- Reads: `PartyLedgerId`, `MCLoanId`, `SNo`, `SubAreaName`, `LedgerName`, `LoanIndex`, `CWLedgerId`, `MCLoanDate`, `MCLoanAmountCollectable`, `Collected`, `LastCollDate`, `LastCollection`, `NextLoanDate`, `NextLoanAmount`

## closingsavingsaccount

Screen: **Savings account closure** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#savings-account-closure--closingsavingsaccount) · 5 statements

#### `ASYNCSave_savingsclosure.doInBackground` · line 171
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertacctransactionsmaster`
```sql
{call dbo.InsertAccTransactionsMaster(?,?,?,?,?,?,?,?,?,? )}
```

- Params: "TransDate" → `loanclosuredate` (setDate) · "TransType" → `28` (setInt) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setDouble) · "TransAmount" → `paidamout` (setDouble) · "TransLedgerId" → `112.0d` (setDouble) · "ImageFileName" → `""` (setString)
- OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
- Reads: `TransId`

#### `ASYNCSave_savingsclosure.doInBackground` · line 184
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertappmcloanclosuretransdetailsp1`
```sql
{call dbo.InsertAPPMCLoanClosureTransDetailsP1(?,?,?,?,?,?,?)}
```

- Params: 1 → `AccTransId` (setDouble) · 2 → `retriver.fetchstaffcashbookledgerid(db, globalvars.finid.intValue())` (setDouble) · 3 → `paidamout` (setDouble) · 4 → `"Source"` (setString) · 5 → `"Cr"` (setString) · 6 → `1.0d` (setDouble) · 7 → `2.0d` (setDouble)

#### `ASYNCSave_savingsclosure.doInBackground` · line 193, 202
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL
```sql
call dbo.InsertAPPMCLoanClosureTransDetailsP2 (?,?,?,?,?,?,?)}
```

- Params: 1 → `AccTransId` (setDouble) · 2 → `receivedaccount.getTargetLedgerId()` (setDouble) · 3 → `paidamout` (setDouble) · 4 → `"Target"` (setString) · 5 → `"Dr"` (setString) · 6 → `2.0d` (setDouble) · 7 → `1.0d` (setDouble)

#### `ASYNCSave_savingsclosure.doInBackground` · line 211
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `SavingsClosure`
```sql
insert into SavingsClosure (TransDate,PartyLedgerId,Amount,AddInterest,Transid)values(?,?,?,?,?)
```

- Params: 1 → `loanclosuredate` (setDate) · 2 → `receivedaccount.getTargetLedgerId()` (setInt) · 3 → `receivedaccount.getSavingsAmount()` (setDouble) · 4 → `interestamount` (setDouble) · 5 → `AccTransId` (setDouble)

## loanclosoureentry

Screen: **Loan closure entry** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#loan-closure-entry--loanclosoureentry) · 14 statements

#### `onActivityResult` · line 269
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `closableliveloans`
```sql
delete from closableliveloans
```

#### `ASYNCSaveLoanClosure.doInBackground` · line 361
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertacctransactionsmaster`
```sql
{call dbo.InsertAccTransactionsMaster(?,?,?,?,?,?,?,?,?,? )}
```

- Params: "TransDate" → `closureloanobj.getClosedDate()` (setDate) · "TransType" → `23` (setInt) · "Narration" → `""` (setString) · "FinId" → `globalvars.finid.intValue()` (setDouble) · "TransAmount" → `closureloanobj.getPending()` (setDouble) · "TransLedgerId" → `125.0d` (setDouble) · "ImageFileName" → `""` (setString)
- OUT: "TransId" (8) · "FWVWTransId" (8) · "FWVWUDTransId" (8)
- Reads: `TransId`

#### `ASYNCSaveLoanClosure.doInBackground` · line 374, 416
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertappmcloanclosuretransdetailsp1`
```sql
{call dbo.InsertAPPMCLoanClosureTransDetailsP1(?,?,?,?,?,?,?)}
```

- Params: 1 → `closureloanobj.getTransId()` (setDouble) · 2 → `closureloanobj.getPartyLedgerId()` (setDouble) · 3 → `closureloanobj.getPending()` (setDouble) · 4 → `"Source"` (setString) · 5 → `"Cr"` (setString) · 6 → `1.0d` (setDouble) · 7 → `2.0d` (setDouble)

#### `ASYNCSaveLoanClosure.doInBackground` · line 383, 425
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL
```sql
call dbo.InsertAPPMCLoanClosureTransDetailsP2 (?,?,?,?,?,?,?)}
```

- Params: 1 → `closureloanobj.getTransId()` (setDouble) · 2 → `125.0d` (setDouble) · 3 → `closureloanobj.getPending()` (setDouble) · 4 → `"Target"` (setString) · 5 → `"Dr"` (setString) · 6 → `2.0d` (setDouble) · 7 → `1.0d` (setDouble)

#### `ASYNCSaveLoanClosure.doInBackground` · line 393
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertmcloansclosoure`
```sql
{call dbo.InsertMCLoansClosoure(?,?,?,?,?,?,?,?)}
```

- Params: "MCLoanId" → `closureloanobj.getMCLoanId()` (setDouble) · "ClosedDate" → `closureloanobj.getClosedDate()` (setDate) · "Collectable" → `closureloanobj.getMCLoanAmountCollectable()` (setDouble) · "Collected" → `closureloanobj.getCollected()` (setDouble) · "Pending" → `closureloanobj.getPending()` (setDouble) · "extracollection" → `closureloanobj.getExtraCollection()` (setDouble) · "TransId" → `closureloanobj.getTransId()` (setDouble)
- OUT: "LCId" (8)

#### `ASYNCSaveLoanClosure.doInBackground` · line 403
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `closableliveloans`
```sql
DELETE FROM closableliveloans WHERE MCLoanId=${closureloanobj.getMCLoanId()}
```

#### `ASYNCSaveLoanClosure.doInBackground` · line 409
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `tbltransactionsmaster`
```sql
update tbltransactionsmaster set TransDate='${closureloanobj.getClosedDate()}' where transid=?
```

- Params: 1 → `closureloanobj.getTransId()` (setDouble)

#### `ASYNCSaveLoanClosure.doInBackground` · line 413, 442
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tbltransactionsdetails`
```sql
delete from tbltransactionsdetails where transid=?
```

- Params: 1 → `closureloanobj.getTransId()` (setDouble)

#### `ASYNCSaveLoanClosure.doInBackground` · line 435
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `MCLoansClosoure`
```sql
update MCLoansClosoure set ClosedDate='${closureloanobj.getClosedDate()}' where MCLoanId=?
```

- Params: 1 → `closureloanobj.getMCLoanId()` (setDouble)

#### `ASYNCSaveLoanClosure.doInBackground` · line 445
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tbltransactionsmaster`
```sql
delete from tbltransactionsmaster where transid=?
```

#### `ASYNCSaveLoanClosure.doInBackground` · line 447
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `MCLoansClosoure`
```sql
delete from MCLoansClosoure where MCLoanId=?
```

- Params: 1 → `closureloanobj.getMCLoanId()` (setDouble)

## loanindexloanswiselist_act

Screen: **Loans of a party by loan index** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#loans-of-a-party-by-loan-index--loanindexloanswiselistact) · 4 statements

#### `ASYNCRefresh.doInBackground` · line 243
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `loanindex`
```sql
DELETE FROM loanindex WHERE LedgerId=${curpartyobj.getLedgerId()}
```

#### `ASYNCRefresh.doInBackground` · line 244
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · GET_LOANINDEXWISELOANSFORSELECTEDPARTY
```sql
Get_LoanindexwiseLoansforselectedparty @PartyLedgerId=${curpartyobj.getLedgerId()}
```

- Reads: `LedgerId`, `LoanIndex`, `MCLoanId`, `MaximumAmountAllowed`

#### `ASYNCRefresh.doInBackground` · line 253
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `loanindex`
```sql
INSERT INTO loanindex (LedgerId, LoanIndex, MCLoanId, MCLoanAmountCollectable, collected, Balance)
VALUES (${Integer.valueOf(reset.getInt("LedgerId"))}, ${Integer.valueOf(reset.getInt("LoanIndex"))}, ${Integer.valueOf(reset.getInt("MCLoanId"))}, ${Integer.valueOf(reset.getInt(str3))}, ${Integer.valueOf(reset.getInt(str2))}, ${Integer.valueOf(reset.getInt(str))})
```

#### `ASYNCRefresh.doInBackground` · line 259
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `Parties`
```sql
UPDATE Parties SET MaximumAmountAllowed=${Integer.valueOf(reset.getInt("MaximumAmountAllowed"))}
WHERE LedgerId=${curpartyobj.getLedgerId()}
```

## loanslist_act

Screen: **Loans list for a line / for today** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#loans-list-for-a-line--for-today--loanslistact) · 9 statements

#### `oncollectiondetailslinkclicked` · line 406
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2 from Parties where LedgerId=${newloanslist_data.getPartyLedgerId()}
```

- Reads: `Tag`

#### `ASYNCLoansList.doInBackground` · line 457, 465
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · EXEC · procedure: `getmcloanslistforappforselectedperiod`
```sql
exec GetMCLoansListforAPPforselectedperiod ?,?,?
```

- Params: 1 → `receviedfinanceid` (setInt) · 2 → `globalvars.collectiondate` (setDate) · 3 → `globalvars.collectiondate` (setDate)

#### `ASYNCLoansList.doInBackground` · line 475, 484
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · EXEC · procedure: `getmcloanslistforappforselectedperiodwithweekdayid`
```sql
exec GetMCLoansListforAPPforselectedperiodwithweekdayid ?,?,?,?
```

- Params: 1 → `receviedfinanceid` (setInt) · 2 → `globalvars.collectiondate` (setDate) · 3 → `globalvars.collectiondate` (setDate) · 4 → `weekdayid` (setInt)

#### `ASYNCLoansList.doInBackground` · line 531
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `Parties`
```sql
DELETE FROM Parties WHERE LedgerId>0
```

#### `ASYNCLoansList.doInBackground` · line 532
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`, `tblLedgers`
```sql
select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,isnull(MobileNo1,'') as MobileNo1,isnull(MobileNo2,'') as MobileNo2,SpouseName,FamilyName,lat,lng,ImageFileName,IdProofFileName,AddLine1,IdProofFileNameII ,NoOfLoansAllowed,MaximumAmountAllowed,isactive
from tblLedgers
where ScreenType=3 and  SubAreaNo in (select SubAreaNo
from subareadetails
Where FinId=${receviedfinanceid})
order by SubAreaNo, UDLedgerId
```

#### `ASYNCLoansList.doInBackground` · line 532
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`, `tblLedgers`
```sql
select LedgerId,CWLedgerId,LedgerName,isnull(Tag,'') as Tag,SubAreaNo,UDLedgerId,isnull(MobileNo1,'') as MobileNo1,isnull(MobileNo2,'') as MobileNo2,SpouseName,FamilyName,lat,lng,ImageFileName,IdProofFileName,AddLine1,IdProofFileNameII ,NoOfLoansAllowed,MaximumAmountAllowed,isactive
from tblLedgers
where  isactive=1 and ScreenType=3 and  SubAreaNo in (select SubAreaNo
from subareadetails
Where FinId=${receviedfinanceid})
order by SubAreaNo, UDLedgerId
```

#### `ASYNCLoansList.doInBackground` · line 556
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `Parties`
```sql
INSERT INTO Parties (LedgerId, CWLedgerId, LedgerName, Tag, SubAreaNo, UDLedgerId, MobileNo1, MobileNo2, SpouseName, FamilyName, lat, lng, ImageFileName, IdProofFileName, AddLine1, IdProofFileNameII, NoOfLoansAllowed, MaximumAmountAllowed, IsActive)
VALUES (${Double.valueOf(reset2.getDouble("LedgerId"))}, ${Double.valueOf(reset2.getDouble("CWLedgerId"))}, ${reset2.getString("LedgerName")}, ${reset2.getString("Tag")}, ${reset2.getString("SubAreaNo")}, ${reset2.getString("UDLedgerId")}, ${reset2.getString("MobileNo1")}, ${reset2.getString("MobileNo2")}, ${reset2.getString(str)}, ${reset2.getString(str3)}, ${reset2.getString("lat")}, ${reset2.getString("lng")}, ${reset2.getString("ImageFileName")}, ${reset2.getString("IdProofFileName")}, ${reset2.getString("AddLine1")}, ${reset2.getString("IdProofFileNameII")}, ${Integer.valueOf(reset2.getInt("NoOfLoansAllowed"))}, ${Integer.valueOf(reset2.getInt("MaximumAmountAllowed"))}, ${Integer.valueOf(reset2.getInt("IsActive"))})
```

## newloanslist_act

Screen: **Today New Loans (offline)** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#today-new-loans-offline--newloanslistact) · 2 statements

#### `onActivityResult` · line 179
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select MCLoanDate, SubAreaName as AreaName,CWLedgerId,ondaycollection,Ledgername as PartyName,Tag,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,  a.ServerGenMCLoanId,   a.ServerGenLedgerId,Remarks, a.ServerGenTransId,a.WeekDayId,MonthDayId,mobileno,Sourceledgerid  ,PartyReferenced ,LoanIndex ,ProductValue,AdvanceAmount,IntRate
From NewMCLoans a,NewParties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
```

- Reads: `FinId`, `MCLoanId`, `CWLedgerId`, `Tag`, `MCLoanDate`, `PartyLedgerId`, `MCLoanAmount`, `ondaycollection`, `PartyName`, `AreaName`, `TotalMonths`, `DocCharges`, `LoanMethodId`, `InterestAmount`, `MCLoanAmountCollectable`, `InsAmount`, `MCLoanAmountEstimated`, `SubAreaNo`, `ServerGenMCLoanId`, `ServerGenLedgerId`, `Remarks`, `WeekdayId`, `MonthdayId`, `mobileno`, `PartyReferenced`, `LoanIndex`, `Sourceledgerid`, `ProductValue`, `AdvanceAmount`, `IntRate`

#### `onActivityResult` · line 223
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewMCLoans`
```sql
select MCLoanDate, SubAreaName as AreaName,CWLedgerId,ondaycollection,Ledgername as PartyName,MCLoanId,a.FinId,PartyLedgerId,MCLoanAmount,TotalMonths,DocCharges,LoanMethodId,InterestAmount,MCLoanAmountCollectable,InsAmount,MCLoanAmountEstimated,a.SubAreaNo,ServerGenMCLoanId,ServerGenLedgerId,Remarks,ServerGenTransId,a.WeekDayId,MonthDayId,mobileno,PartyReferenced,Sourceledgerid,LoanIndex,ProductValue,AdvanceAmount,IntRate
From NewMCLoans a,Parties b ,SubAreaDetails c
where a.PartyLedgerId=b.LedgerId and c.SubAreaNo=a.SubAreaNo
```

- Reads: `FinId`, `MCLoanId`, `CWLedgerId`, `PartyLedgerId`, `mobileno`, `MCLoanAmount`, `AreaName`, `MCLoanDate`, `TotalMonths`, `DocCharges`, `LoanMethodId`, `InterestAmount`, `InsAmount`, `MCLoanAmountEstimated`, `SubAreaNo`, `ServerGenMCLoanId`, `ServerGenLedgerId`, `Remarks`, `WeekdayId`, `MonthdayId`, `PartyReferenced`, `Sourceledgerid`, `LoanIndex`, `ProductValue`, `AdvanceAmount`, `IntRate`

## newpartieslist_act

Screen: **Today New Parties (offline)** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#today-new-parties-offline--newpartieslistact) · 1 statements

#### `fetchdata_fromnewpartiestable` · line 106
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `NewParties`
```sql
Select LedgerId, CWLedgerId, LedgerName, Tag, a.AreaNo, a.SubAreaNo,b.SubAreaName, UDLedgerId, MobileNo1, MobileNo2, ServerGenLedgerId,AddLine1,SpouseName,ImagefileName,IdProoffileName,lat,lng,IdProofFileNameII
from NewParties a, SubAreaDetails b
where a.SubAreaNo=b.SubAreaNo
```

- Reads: `LedgerId`, `CWLedgerId`, `LedgerName`, `Tag`, `SubAreaName`, `SubAreaNo`, `UDLedgerId`, `MobileNo1`, `MobileNo2`, `ServerGenLedgerId`, `SpouseName`, `AddLine1`, `ImagefileName`, `IdProoffileName`, `IdProofFileNameII`, `lng`, `lat`

## newpartiesnewloanslist_act

Screen: **New Party Loans report** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#new-party-loans-report--newpartiesnewloanslistact) · 3 statements

#### `oncollectiondetailslinkclicked` · line 259
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2 from Parties where LedgerId=${newloanslist_data.getPartyLedgerId()}
```

- Reads: `Tag`

#### `ASYNCGetNewpartiesNewLoans.doInBackground` · line 291
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `newpartiesnewloans`
```sql
exec NewPartiesNewLoans @fromDate='${curdaterange.getFromdate()}',  @ReportDate='${curdaterange.getTodate()}', @IMEI='${globalvars.device_id}', @FinId=${financeid}
```

#### `ASYNCGetNewpartiesNewLoans.doInBackground` · line 293
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `newpartiesnewloanswithweekdayid`
```sql
exec NewPartiesNewLoanswithweekdayid  @weekdayid= ${weekdayid}, @fromDate='${curdaterange.getFromdate()}',  @ReportDate='${curdaterange.getTodate()}', @IMEI='${globalvars.device_id}', @FinId=${financeid}
```

- Reads: `SubAreaName`, `LedgerName`, `CWLedgerId`, `PartyLedgerId`, `MCLoanId`, `MCLoanDate`, `InterestAmount`, `MCLoanAmountCollectable`, `InsAmount`, `TotalMonths`, `MobileNo`

## partyrearrange

Screen: **Rearrange parties** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#rearrange-parties--partyrearrange) · 6 statements

#### `onCreate` · line 66
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `Parties`
```sql
select  distinct( LedgerId),  LedgerName, Tag,b.PartyLedgerId ,a.CWLedgerId
from Parties  a
left
join MobileAppCollData b on a.LedgerId=b.PartyLedgerId
order by a.UDLedgerId
```

#### `onCreate` · line 68
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `Parties`
```sql
select  distinct( LedgerId),  LedgerName, Tag,b.PartyLedgerId ,a.CWLedgerId
from Parties  a
left
join MobileAppCollData b on a.LedgerId=b.PartyLedgerId
where a.SubAreaNo=${extras.getInt("PLID", 0)}
order by a.UDLedgerId
```

#### `onOptionsItemSelected` · line 149
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `Parties`
```sql
update Parties set UDLedgerId=01 where LedgerId=${Integer.parseInt(textView.getTag().toString())}
```

#### `onOptionsItemSelected` · line 150
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set UDLedgerId=01
where PartyLedgerId=${Integer.parseInt(textView.getTag().toString())}
```

#### `Asyncupdateudledgerid.doInBackground` · line 176
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
Select LedgerId,UDLedgerId from Parties where SubAreaNo=${passedsubareano}
```

#### `Asyncupdateudledgerid.doInBackground` · line 184
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `tblLedgers`
```sql
;${c.getInt(0)} where LedgerId=${c.getInt(1)}update tblLedgers set UDLedgerId=
```

## partytransfer

Screen: **Party transfer** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#party-transfer--partytransfer) · 4 statements

#### `AsyncPartyTransfer.doInBackground` · line 140
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · UPDATE · tables: `tblLedgers`
```sql
update tblLedgers set SubAreaNo =${TargetAreaId} where LedgerId=${SelectedPartyLedgerId}
```

#### `AsyncPartyTransfer.doInBackground` · line 142
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set SubAreaNo =${TargetAreaId} where PartyLedgerId=${SelectedPartyLedgerId}
```

#### `AsyncPartyTransfer.doInBackground` · line 143
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `MobileAppCollData`
```sql
update MobileAppCollData set SubAreaName ='${NewAreaName}' where PartyLedgerId=${SelectedPartyLedgerId}
```

#### `AsyncPartyTransfer.doInBackground` · line 144
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `Parties`
```sql
update Parties set SubAreaNo =${TargetAreaId} where LedgerId=${SelectedPartyLedgerId}
```

## prevloanclosure_list_act

Screen: **Previous loan closures** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#previous-loan-closures--prevloanclosurelistact) · 1 statements

#### `ASYNCGetPrevLoanList.doInBackground` · line 303
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · MCLOANCLOSURELIST
```sql
MCLoanClosureList @Weekdayid=${weekdayid} , @finid=${finidforloanclose}, @fromdate='${curdaterange.getFromdate()}', @todate='${curdaterange.getTodate()}'
```

- Reads: `TransId`, `MCLoanId`, `CWLedgerId`, `PartyLedgerId`, `SubAreaNo`, `MCLoanDate`, `SubAreaName`, `LedgerName`, `Collected`, `Pending`, `ExtraCollection`, `ClosedDate`, `MCLoanAmountCollectable`

## prevloanslist_act

Screen: **Party loan history** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#party-loan-history--prevloanslistact) · 10 statements

#### `fetchContacts` · line 112
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `PrevLoansListTable`
```sql
Select MCLoanId, LoanNo, LoanDate, LoanAmount, Paid, Status, Days from PrevLoansListTable order by MCLoanId
```

- Reads: `MCLoanId`, `LoanNo`, `LoanDate`, `LoanAmount`, `Paid`, `Status`, `Days`

#### `onContactSelected` · line 189
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2 from Parties where LedgerId=${passetpartyledgerid}
```

- Reads: `LedgerName`, `Tag`

#### `onContactSelected` · line 198
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select Remarks  from MobileAppCollData where MCLoanId=${prevloanslist_data.getMCLoanId()}
```

- Reads: `Remarks`

#### `ASYNCGetPrevLoanList.doInBackground` · line 224, 226
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `PrevLoansListTable`
```sql
CREATE TABLE IF NOT EXISTS PrevLoansListTable(MCLoanId int,LoanNo int,LoanDate Date,LoanAmount int,Paid int,Status String,Days int)
```

#### `ASYNCGetPrevLoanList.doInBackground` · line 225
**local SQLite** · `sqlite.execSQL` · DROP · tables: `PrevLoansListTable`
```sql
drop table PrevLoansListTable
```

#### `ASYNCGetPrevLoanList.doInBackground` · line 227
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `PrevLoansListTable`
```sql
delete from PrevLoansListTable
```

#### `ASYNCGetPrevLoanList.doInBackground` · line 228
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · ?
```sql
[APP_ShowMCLStatus] @PLId=${passetpartyledgerid},@Loanindex=${LoanIndex}
```

- Reads: `MCLoanId`, `LoanNo`, `LoanDate`, `LoanAmount`, `Paid`, `Status`, `Days`

#### `ASYNCGetPrevLoanList.doInBackground` · line 238
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `PrevLoansListTable`
```sql
INSERT INTO PrevLoansListTable (MCLoanId, LoanNo, LoanDate, LoanAmount, Paid, Status, Days)
VALUES (${Double.valueOf(reset.getDouble("MCLoanId"))}, ${Integer.valueOf(reset.getInt("LoanNo"))}, ${String.valueOf(reset.getDate("LoanDate"))}, ${Integer.valueOf(reset.getInt("LoanAmount"))}, ${Integer.valueOf(reset.getInt("Paid"))}, ${reset.getString("Status")}, ${Integer.valueOf(reset.getInt("Days"))})
```

#### `ASYNCGetSelectedArchievedloandetails.doInBackground` · line 282
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `ArchivedMCLoans`
```sql
Select AMCLoanId,MCLoanId, MCLoanDate, FWMCLoanId, FWUDMCLoanId, FinId, PartyLedgerId, GuaranderLedgerID, MCLoanAmount, TotalMonths, DCPercent, DocCharges, InterestAmount, MCLoanAmountCollectable, InsAmount, Remarks, WeekDayId, MonthDayId, mobileno, Sourceledgerid, LoanIndex, Closingdate, LastPaymentDate, MinCollection, MaxCollection, AvgCollection, MinGap, AvgGap, MaxGap, TotalCollection, ReportIdentification
from ArchivedMCLoans
where Mcloanid=${selectedloan.getMCLoanId()}
```

- Reads: `AMCLoanId`, `MCLoanId`, `MCLoanDate`, `FWMCLoanId`, `FWUDMCLoanId`, `FinId`, `PartyLedgerId`, `GuaranderLedgerID`, `MCLoanAmount`, `TotalMonths`, `DCPercent`, `DocCharges`, `InterestAmount`, `MCLoanAmountCollectable`, `InsAmount`, `Remarks`, `WeekDayId`, `MonthDayId`, `mobileno`, `Sourceledgerid`, `LoanIndex`, `ClosingDate`, `LastPaymentDate`, `MinCollection`, `MaxCollection`, `AvgCollection`, `MinGap`, `AvgGap`, `MaxGap`, `TotalCollection`, `ReportIdentification`

## prevsavingclosure_list_act

Screen: **Previous saving closures** · [spec](../../brain/modules/08-loan-lists-loan-savings-closures-party.md#previous-saving-closures--prevsavingclosurelistact) · 4 statements

#### `ASYNCDeletesavingclosure.doInBackground` · line 235
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `SavingsClosure`
```sql
delete from SavingsClosure where Transid=?
```

- Params: 1 → `selectedsavingsclosure.getTransId()` (setDouble)

#### `ASYNCDeletesavingclosure.doInBackground` · line 238
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tbltransactionsdetails`
```sql
delete from tbltransactionsdetails where Transid=?
```

- Params: 1 → `selectedsavingsclosure.getTransId()` (setDouble)

#### `ASYNCDeletesavingclosure.doInBackground` · line 241
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `tbltransactionsmaster`
```sql
delete from tbltransactionsmaster where Transid=?
```

- Params: 1 → `selectedsavingsclosure.getTransId()` (setDouble)

#### `ASYNCGetPrevLoanList.doInBackground` · line 179
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · SAVINGACCOUNTCLOSURE
```sql
Savingaccountclosure
```

- Reads: `SCId`, `TransId`, `TransDate`, `PartyLedgerId`, `LedgerName`, `SubAreaName`, `Amount`, `AddInterest`

