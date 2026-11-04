# SQL — 8.6 Collection reports (collection for a day, abstract, 7-days, unpaid parties, day-wise pending, periodic loans)

[← work.md](../../work.md) · [Module spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **24 statements** (16 local SQLite, 8 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`collectionarealistforloanpartyandloans`](#collectionarealistforloanpartyandloans) (1) · [`collectionarealistforsevendayscollection`](#collectionarealistforsevendayscollection) (1) · [`daywisependingreport_act`](#daywisependingreportact) (2) · [`periodicloareportlist_act`](#periodicloareportlistact) (3) · [`selectdateforreport`](#selectdateforreport) (6) · [`sevendayscollectionforselline`](#sevendayscollectionforselline) (3) · [`todaycollectionabstractlist`](#todaycollectionabstractlist) (1) · [`todaycollectionlist`](#todaycollectionlist) (5) · [`unpaidpartiesonselectedday`](#unpaidpartiesonselectedday) (2)

## collectionarealistforloanpartyandloans

Screen: **Collection areas for Loan Parties/Loans** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#collection-areas-for-loan-partiesloans--collectionarealistforloanpartyandloans) · 1 statements

#### `DisplayIndexData` · line 74
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedFinanceSubAreaDetails`
```sql
select SubAreaId,SubAreaName  from SelectedFinanceSubAreaDetails where finid=${passedfinid}
```

- Reads: `SubAreaName`, `SubAreaId`

## collectionarealistforsevendayscollection

Screen: **7-Days Collection — area list** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#7-days-collection--area-list--collectionarealistforsevendayscollection) · 1 statements

#### `DisplayIndexData` · line 74
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedFinanceSubAreaDetails`
```sql
select SubAreaId,SubAreaName  from SelectedFinanceSubAreaDetails where finid=${passedfinid}
```

- Reads: `SubAreaId`, `SubAreaName`

## daywisependingreport_act

Screen: **Day-wise Pending** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#day-wise-pending--daywisependingreportact) · 2 statements

#### `oncollectiondetailslinkclicked` · line 161
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2
from Parties
where LedgerId=${daywisependingreport_data.getPartyLedgerId()}
```

- Reads: `Tag`

#### `ASYNCGetNewpartiesNewLoans.doInBackground` · line 189
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · EXEC · procedure: `app_pendingreportautoloanclosoure_withoutweekdayssupport`
```sql
exec APP_PendingReportAUTOLOANCLOSOURE_WithOUTWeekdaysSupport   @ColDate='${todate}',  @FinId=${financeid}
```

- Reads: `MCLoanId`, `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `ShowId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `PartyName`, `MobileNo1`, `MobileNo2`, `Imagefilename`, `SubAreaNo`, `SubAreaName`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PrevCollected`, `Balance`, `NoofCollections`, `LastCollDate`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `ReportIdentification`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`, `LoanStatusType2`, `InterestAmount`

## periodicloareportlist_act

Screen: **Periodic Loans** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#periodic-loans--periodicloareportlistact) · 3 statements

#### `onContactSelected` · line 157
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2
from Parties
where LedgerId=${periodicloanreport_data.getPartyLedgerId()}
```

- Reads: `Tag`

#### `onContactSelected` · line 164
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select Remarks  from MobileAppCollData where MCLoanId=${periodicloanreport_data.getMCLoanId()}
```

- Reads: `Remarks`

#### `ASYNCGetAbstractlist.doInBackground` · line 201
**SQL Server** · `jdbc.Statement.executeQuery` · branch DB · EXEC · procedure: `getloandetailsforperiod`
```sql
exec GetLoanDetailsforPeriod @FinId =${financeid},@FromDate='${fromdate}' ,@ToDate='${todate}'
```

- Reads: `TypeId`, `MCLoanId`, `SNo`, `SubAreaName`, `Date`, `Party`, `LT`, `Loan`, `Dues`, `IntRate`, `Interest`, `PartyLedgerId`, `Mahimai`, `SubAreaNo`, `showid`, `Balance`

## selectdateforreport

Screen: **Unpaid Parties — date selection** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#unpaid-parties--date-selection--selectdateforreport) · 6 statements

#### `ASYNCcollectiondetailsr.doInBackground` · line 146
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `Unpaidparties`
```sql
Create table IF NOT EXISTS Unpaidparties(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate String,ClosingDate String,CWLedgerId Double,UDLedgerId int,PartyName String,SubAreaNo Double,LAmount Double,DuesCount Double,InsAmount int,MCLoanAmountCollectable Double,PrevCollected Double,PaidDues int,PassedDues int,LoanMethodDesc String,CollectionBreakString String,NoOfIns Double,CBCount int,istobeWarned int,IsNIP int,LoanStatus Double)
```

#### `ASYNCcollectiondetailsr.doInBackground` · line 147
**local SQLite** · `sqlite.execSQL` · DROP · tables: `Unpaidparties`
```sql
drop table Unpaidparties
```

#### `ASYNCcollectiondetailsr.doInBackground` · line 148
**local SQLite** · `sqlite.execSQL` · CREATE · tables: `UnpaidParties`
```sql
Create table IF NOT EXISTS UnpaidParties(MCLoanId Double,FWUDMCLoanId Double,Remarks String,PartyLedgerId Double,MCLoanDate String,ClosingDate String,CWLedgerId Double,UDLedgerId int,PartyName String,SubAreaNo Double,SubAreaName String,ShowId int,LAmount Double,DuesCount Double,InsAmount int,MCLoanAmountCollectable Double,PrevCollected Double,PaidDues int,PassedDues int,LoanMethodDesc String,CollectionBreakString String,NoOfIns Double,CBCount int,istobeWarned int,IsNIP int,LoanStatus Double)
```

#### `ASYNCcollectiondetailsr.doInBackground` · line 149
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `UnpaidParties`
```sql
delete from UnpaidParties
```

#### `ASYNCcollectiondetailsr.doInBackground` · line 151
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_unpaidpartiesforselecteddate`
```sql
exec APP_unpaidpartiesforselecteddate @ColDate='${TransDate}',@FinId=${globalvars.finid}
```

- Reads: `MCLoanId`, `FWUDMCLoanId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `PartyName`, `SubAreaNo`, `SubAreaName`, `PassedDues`, `LoanMethodDesc`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`

#### `ASYNCcollectiondetailsr.doInBackground` · line 195
**local SQLite** · `sqlite.insert(ContentValues)` · INSERT · tables: `Unpaidparties`
```sql
INSERT INTO Unpaidparties (MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, SubAreaName, ShowId, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus)
VALUES (${Double.valueOf(reset.getDouble("MCLoanId"))}, ${Double.valueOf(reset.getDouble("FWUDMCLoanId"))}, ${reset.getString(str6)}, ${Double.valueOf(reset.getDouble(str4))}, ${String.valueOf(reset.getDate("MCLoanDate"))}, ${String.valueOf(reset.getDate("ClosingDate"))}, ${Double.valueOf(reset.getDouble("CWLedgerId"))}, ${Integer.valueOf(reset.getInt("UDLedgerId"))}, ${reset.getString("PartyName")}, ${Double.valueOf(reset.getDouble("SubAreaNo"))}, ${reset.getString("SubAreaName")}, ${Integer.valueOf(reset.getInt(str10))}, ${Double.valueOf(reset.getDouble(str11))}, ${Double.valueOf(reset.getDouble(str13))}, ${Integer.valueOf(reset.getInt(str14))}, ${Double.valueOf(reset.getDouble(str15))}, ${Double.valueOf(reset.getDouble(str16))}, ${Integer.valueOf(reset.getInt(str17))}, ${Integer.valueOf(reset.getInt("PassedDues"))}, ${reset.getString("LoanMethodDesc")}, ${reset.getString("CollectionBreakString")}, ${Double.valueOf(reset.getDouble("NoOfIns"))}, ${Integer.valueOf(reset.getInt("CBCount"))}, ${Integer.valueOf(reset.getInt("istobeWarned"))}, ${Integer.valueOf(reset.getInt("IsNIP"))}, ${Double.valueOf(reset.getDouble("LoanStatus"))})
```

## sevendayscollectionforselline

Screen: **7-Days Collection — area pages** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#7-days-collection--area-pages--sevendayscollectionforselline) · 3 statements

#### `onCreate` · line 88
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedFinanceSubAreaDetails`
```sql
select SubAreaId,SubAreaName from SelectedFinanceSubAreaDetails where finid=${extras1.getInt("passedfinid")}
```

- Reads: `SubAreaName`, `SubAreaId`

#### `ASYNCupdateemployee.doInBackground` · line 260
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `setattentionrequired`
```sql
{call dbo.SetAttentionRequired(?)}
```

- Params: "MCLoanIdArray" → `MCLoanIdArray.toString()` (setString)

#### `SectionsPagerAdapter.getCount` · line 232
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SelectedFinanceSubAreaDetails`
```sql
select SubAreaId,SubAreaName from SelectedFinanceSubAreaDetails where finid=${extras.getInt("passedfinid")}
```

## todaycollectionabstractlist

Screen: **Collection Abstract for a Day** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#collection-abstract-for-a-day--todaycollectionabstractlist) · 1 statements

#### `ASYNCGetAbstractlist.doInBackground` · line 274
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `dailycollectionanalysis`
```sql
exec DailyCollectionAnalysis @FinId =${financeid},@collDate='${sqlDate}'
```

- Reads: `ct`, `Particulars`, `PartiesCount`, `Collected`

## todaycollectionlist

Screen: **Collection for a Day** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#collection-for-a-day--todaycollectionlist) · 5 statements

#### `onContactSelected` · line 425
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `Parties`
```sql
select LedgerName,Tag,MobileNo1,MobileNo2
from Parties
where LedgerId=${todaycollectionsdata.getPartyLedgerId()}
```

- Reads: `LedgerName`, `Tag`

#### `onContactSelected` · line 434
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`
```sql
select Remarks  from MobileAppCollData where MCLoanId=${todaycollectionsdata.getMCLoanId()}
```

- Reads: `Remarks`

#### `ASYNCGetCollectionDetails.doInBackground` · line 462
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `dailycollectiondetailsforweeklylines`
```sql
exec DailyCollectionDetailsforweeklyLines @ctid=${ctid}, @FinId =${financeid},@collDate='${sqlDate}'
```

#### `ASYNCGetCollectionDetails.doInBackground` · line 464
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `dailycollectiondetails`
```sql
exec DailyCollectionDetails @ctid=${ctid}, @FinId =${financeid},@collDate='${sqlDate}'
```

#### `ASYNCTodayCollection.doInBackground` · line 595
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · EXEC · procedure: `app_gettodaycollections`
```sql
exec APP_GetTodayCollections @IMEI='${globalvars.device_id}',@FinId=${globalvars.finid}
```

- Reads: `MCLoanId`, `FWUDMCLoanid`, `CWLedgerId`, `PartyLedgerId`, `LDate`, `PartyName`, `Tag`, `SubAreaName`, `SubAreaNo`, `LAmount`, `Collected`, `cashCollected`, `BankCollected`, `ShowId`, `UdLedgerId`

## unpaidpartiesonselectedday

Screen: **Unpaid Parties (result)** · [spec](../../brain/modules/06-collection-reports-collection-day-abstract-7.md#unpaid-parties-result--unpaidpartiesonselectedday) · 2 statements

#### `initDate` · line 49
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `UnPaidParties`
```sql
select SubAreaNo,SubAreaName,count(MCLoanId) as UnpaidPartiesCount
from UnPaidParties
group by SubAreaNo,SubAreaName,ShowId
order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`, `UnpaidPartiesCount`

#### `initDate` · line 61
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `UnpaidParties`
```sql
Select MCLoanId, FWUDMCLoanId, Remarks, PartyLedgerId, MCLoanDate, ClosingDate, CWLedgerId, UDLedgerId, PartyName, SubAreaNo, SubAreaName, ShowId, LAmount, DuesCount, InsAmount, MCLoanAmountCollectable, PrevCollected, PaidDues, PassedDues, LoanMethodDesc, CollectionBreakString, NoOfIns, CBCount, istobeWarned, IsNIP, LoanStatus
from UnpaidParties
Where SubAreaNo=${c2.getDouble(c2.getColumnIndexOrThrow("SubAreaNo"))}
order by UDLedgerId
```

- Selection args: `strArr`
- Reads: `MCLoanId`, `FWUDMCLoanId`, `Remarks`, `PartyLedgerId`, `MCLoanDate`, `ClosingDate`, `CWLedgerId`, `UDLedgerId`, `PartyName`, `ShowId`, `LAmount`, `DuesCount`, `InsAmount`, `MCLoanAmountCollectable`, `PrevCollected`, `PaidDues`, `PassedDues`, `LoanMethodDesc`, `CollectionBreakString`, `NoOfIns`, `CBCount`, `istobeWarned`, `IsNIP`, `LoanStatus`

