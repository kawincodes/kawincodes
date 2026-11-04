# SQL — 8.18 Printing: collection list prints, receipts and Bluetooth printers

[← work.md](../../work.md) · [Module spec](../../brain/modules/18-printing-collection-list-prints-receipts-bluetooth.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **6 statements** (0 local SQLite, 6 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`collectionlistprints`](#collectionlistprints) (6)

## collectionlistprints

Screen: **Collection List Print** · [spec](../../brain/modules/18-printing-collection-list-prints-receipts-bluetooth.md#collection-list-print--collectionlistprints) · 6 statements

#### `AsyncDownloaddata.doInBackground` · line 6411
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · EXEC · procedure: `app_mcloancollectiondatawithloanclosouremonitering_withweekdayssupport`
```sql
exec APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport ?,?,?
```

#### `AsyncDownloaddata.doInBackground` · line 6413
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · EXEC · procedure: `app_mcloancollectiondataautoloanclosoure_withweekdayssupport`
```sql
exec APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport ?,?,?
```

#### `AsyncDownloaddata.doInBackground` · line 6415
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · EXEC · procedure: `app_mcloancollectiondatawithloanclosouremonitering_withoutweekdayssupport`
```sql
exec APP_MCLoancollectionDatawithLoanClosoureMonitering_withoutWeekdayssupport ?,?,?
```

#### `AsyncDownloaddata.doInBackground` · line 6417
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · EXEC · procedure: `app_mcloancollectiondataautoloanclosoure_withoutweekdayssupport`
```sql
exec APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithOUTWeekdaysSupport ?,?,?
```

- Params: 1 → `Date.valueOf(collectionperiod.get(collectionperiod.size() - 1).toString())` (setDate) · 3 → `Date.valueOf(collectionperiod.get(0).minusDays(1L).toString())` (setDate) · 1 → `Date.valueOf(reportdate.toString())` (setDate) · 3 → `Date.valueOf(reportdate.toString())` (setDate) · 2 → `globalvars.curfin.getFinId().intValue()` (setInt)
- Reads: `UDLedgerId`, `ShowId`, `SubAreaName`, `CWLedgerId`, `PartyName`, `Tag`, `MCLoanAmountCollectable`, `MobileNo1`, `MCLoanId`, `MCLoanDate`, `InsAmount`, `DuesCount`, `PrevCollected`, `CollectionBreakString`

#### `AsyncDownloaddata.doInBackground` · line 6456
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · SELECT · tables: `tblMCLoanDueCol`
```sql
select MCLoanId,MCLDueColDate ,FinId,Paid
From tblMCLoanDueCol
where FinId=? and MCLDueColDate>=? and MCLDueColDate <=?
```

- Params: 1 → `globalvars.curfin.getFinId().intValue()` (setInt) · 2 → `Date.valueOf(reportdate.withDayOfMonth(1).toString())` (setDate) · 3 → `Date.valueOf(reportdatemonth_lastdate.toString())` (setDate)

#### `AsyncDownloaddata.doInBackground` · line 6464
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · EXEC · procedure: `getlastncollectionforcollreportprint`
```sql
exec GetLastNCollectionforCollReportPrint ?,?,?,?
```

- Params: 1 → `Date.valueOf(reportdate.toString())` (setDate) · 2 → `globalvars.curfin.getFinId().intValue()` (setInt) · 3 → `curfinancedefaultcollectionbreakstring` (setString) · 4 → `curmethod.getNoofbreaksforprevcollection()` (setInt)
- Reads: `MCLoanId`, `MCLDueColDate`, `Paid`

