# SQL — 8.9 Collection areas, maps, GPS staff tracking and attendance

[← work.md](../../work.md) · [Module spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md) · [By table](by-table.md) · [By procedure](by-procedure.md)

Every SQL statement executed by the classes of this module: **25 statements** (16 local SQLite, 9 SQL Server). Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.

**Classes:** [`collectionareadetails_act`](#collectionareadetailsact) (6) · [`collectionarearearrangeact`](#collectionarearearrangeact) (4) · [`collectionareassearchlist`](#collectionareassearchlist) (6) · [`maps_collectionareadisplay`](#mapscollectionareadisplay) (1) · [`maps_partydisplay`](#mapspartydisplay) (2) · [`staffgpstracking_service`](#staffgpstrackingservice) (5) · [`staffgpstrackmap_act`](#staffgpstrackmapact) (1)

## collectionareadetails_act

Screen: **Add / Edit Collection Area** · [spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md#add--edit-collection-area--collectionareadetailsact) · 6 statements

#### `AYNCUploadCollectionArea.doInBackground` · line 225
**SQL Server** · `jdbc.prepareCall` · selected-branch DB · CALL · procedure: `insertsubareadetails`
```sql
{call dbo.InsertSubAreaDetails(?,?,?,?,?,?,?,?)}
```

- Params: "SubAreaName" → `receivedarea.getSubAreaName()` (setString) · "IsVisible" → `receivedarea.getVisible()` (setBoolean) · "ShowId" → `receivedarea.getShowId()` (setInt) · "FinId" → `globalvars.finid.intValue()` (setInt) · "Latitude" → `receivedarea.getLatitude()` (setDouble) · "Longitude" → `receivedarea.getLongitude()` (setDouble) · "WeekdayId" → `receivedarea.getWeekdayId()` (setInt) · "WeekdayId" → `4` (setNull)
- OUT: "SubAreaNo" (8)
- Reads: `SubAreaNo`

#### `AYNCUploadCollectionArea.doInBackground` · line 253
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `SubAreaDetails`
```sql
INSERT INTO SubAreaDetails (SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekdayId, Latitude, Longitude)
VALUES (${Integer.valueOf(receivedarea.getSubAreaNo())}, ${receivedarea.getSubAreaName()}, ${Boolean.valueOf(receivedarea.getVisible())}, ${Integer.valueOf(receivedarea.getShowId())}, ${Integer.valueOf(receivedarea.getFinId())}, ${globalvars.currentweekdayid}, ${Double.valueOf(receivedarea.getLatitude())}, ${Double.valueOf(receivedarea.getLongitude())})
```

#### `AYNCUploadCollectionArea.doInBackground` · line 257
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · DELETE · tables: `SubAreaDetails`
```sql
delete from SubAreaDetails where SubAreaNo=?
```

- Params: 1 → `receivedarea.getSubAreaNo()` (setInt)

#### `AYNCUploadCollectionArea.doInBackground` · line 260
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `SubAreaDetails`
```sql
DELETE FROM SubAreaDetails WHERE SubAreaNo=${receivedarea.getSubAreaNo()}
```

#### `AYNCUploadCollectionArea.doInBackground` · line 280
**local SQLite** · `sqlite.update(ContentValues)` · UPDATE · tables: `SubAreaDetails`
```sql
UPDATE SubAreaDetails SET SubAreaNo=${Integer.valueOf(receivedarea.getSubAreaNo())}, SubAreaName=${receivedarea.getSubAreaName()}, IsVisible=${Boolean.valueOf(receivedarea.getVisible())}, ShowId=${Integer.valueOf(receivedarea.getShowId())}, FinId=${Integer.valueOf(receivedarea.getFinId())}, Latitude=${Double.valueOf(receivedarea.getLatitude())}, Longitude=${Double.valueOf(receivedarea.getLongitude())}, WeekdayId=${globalvars.currentweekdayid}
WHERE SubAreaNo=${receivedarea.getSubAreaNo()}
```

#### `AYNCUploadCollectionArea.doInBackground` · line 281
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · UPDATE · tables: `SubAreaDetails`
```sql
update SubAreaDetails set SubAreaName=?,IsVisible=?,ShowId=?,FinId=?,WeekdayId=?,Latitude =?,Longitude =?
where SubAreaNo=?
```

- Params: 1 → `receivedarea.getSubAreaName()` (setString) · 2 → `receivedarea.getVisible()` (setBoolean) · 3 → `receivedarea.getShowId()` (setInt) · 4 → `receivedarea.getFinId()` (setInt) · 5 → `receivedarea.getWeekdayId()` (setInt) · 5 → `4` (setNull) · 6 → `receivedarea.getLatitude()` (setDouble) · 7 → `receivedarea.getLongitude()` (setDouble) · 8 → `receivedarea.getSubAreaNo()` (setInt)

## collectionarearearrangeact

Screen: **Rearrange Areas** · [spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md#rearrange-areas--collectionarearearrangeact) · 4 statements

#### `onCreate` · line 55
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SubAreaDetails`
```sql
select  SubAreaNo, SubAreaName,  IsVisible, ShowId from SubAreaDetails order by showid
```

#### `onOptionsItemSelected` · line 151
**local SQLite** · `sqlite.execSQL` · UPDATE · tables: `SubAreaDetails`
```sql
update SubAreaDetails set ShowId=01 where SubAreaNo=${Integer.parseInt(textView.getTag().toString())}
```

#### `AsyncUpdateShowindexvalues.doInBackground` · line 170
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SubAreaDetails`
```sql
Select SubAreaNo,ShowId from SubAreaDetails
```

#### `AsyncUpdateShowindexvalues.doInBackground` · line 178
**SQL Server** · `jdbc.Statement.execute` · selected-branch DB · ? · tables: `SubAreaDetails`
```sql
;${Double.parseDouble(c.getString(0))}
where SubAreaNo=${Double.parseDouble(c.getString(1))}update SubAreaDetails set ShowId=
```

## collectionareassearchlist

Screen: **Collection Areas list** · [spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md#collection-areas-list--collectionareassearchlist) · 6 statements

#### `fetchcollectionareadetails` · line 283
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SubAreaDetails`
```sql
select  SubAreaNo, SubAreaName,WeekdayId, FinId, IsVisible, ShowId,Latitude,Longitude
from SubAreaDetails
where FinId = ${globalvars.finid}
order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`, `IsVisible`, `ShowId`, `FinId`, `WeekdayId`, `Latitude`, `Longitude`

#### `AYNCGetCollectionArea.doInBackground` · line 411
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId,Latitude,Longitude
from subareadetails
where FinId=${getIntent().getIntExtra("receviedfinanceid", 0)}
order by ShowId
```

#### `AYNCGetCollectionArea.doInBackground` · line 413
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId,Latitude,Longitude
from subareadetails
where FinId=${globalvars.finid}
order by ShowId
```

#### `AYNCGetCollectionArea.doInBackground` · line 415
**SQL Server** · `jdbc.Statement.executeQuery` · selected-branch DB · SELECT · tables: `subareadetails`
```sql
Select SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId,Latitude,Longitude
from subareadetails
where FinId=${globalvars.finid} and WeekDayId=${globalvars.currentweekdayid}
order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`, `IsVisible`, `ShowId`, `FinId`, `WeekDayId`, `Latitude`, `Longitude`

#### `AYNCGetCollectionArea.doInBackground` · line 417
**local SQLite** · `sqlite.delete(ContentValues)` · DELETE · tables: `subareadetails`
```sql
DELETE FROM subareadetails WHERE SubAreaNo>?
```

- Selection args: `new String[]{String.valueOf(0)}`

#### `AYNCGetCollectionArea.doInBackground` · line 429
**local SQLite** · `sqlite.insertOrThrow(ContentValues)` · INSERT · tables: `subareadetails`
```sql
INSERT INTO subareadetails (SubAreaNo, SubAreaName, IsVisible, ShowId, FinId, WeekDayId, Latitude, Longitude)
VALUES (${Double.valueOf(reset.getDouble("SubAreaNo"))}, ${reset.getString("SubAreaName")}, ${Boolean.valueOf(reset.getBoolean("IsVisible"))}, ${Integer.valueOf(reset.getInt("ShowId"))}, ${Integer.valueOf(reset.getInt("FinId"))}, ${Integer.valueOf(reset.getInt("WeekDayId"))}, ${Double.valueOf(reset.getDouble("Latitude"))}, ${Double.valueOf(reset.getDouble("Longitude"))})
```

## maps_collectionareadisplay

Screen: **Collection areas on map** · [spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md#collection-areas-on-map--mapscollectionareadisplay) · 1 statements

#### `onMapReady` · line 59
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `SubAreaDetails`
```sql
select  SubAreaNo, SubAreaName,WeekdayId, FinId, IsVisible, ShowId,Latitude  ,   Longitude
from SubAreaDetails
where FinId = ${globalvars.finid}
order by ShowId
```

- Reads: `SubAreaNo`, `SubAreaName`, `IsVisible`, `ShowId`, `FinId`, `Latitude`, `Longitude`, `WeekdayId`

## maps_partydisplay

Screen: **Parties on map** · [spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md#parties-on-map--mapspartydisplay) · 2 statements

#### `onMapReady` · line 64
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `Parties`
```sql
select  distinct( LedgerId),  LedgerName,a.lng,a.lat
from Parties  a
left
join MobileAppCollData b on a.LedgerId=b.PartyLedgerId
order by a.UDLedgerId
```

#### `onMapReady` · line 66
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `MobileAppCollData`, `Parties`
```sql
select  distinct( LedgerId),  LedgerName, a.lng,a.lat
from Parties  a
left
join MobileAppCollData b on a.LedgerId=b.PartyLedgerId
where a.SubAreaNo=${extras.getInt("PLID", 0)}
order by a.UDLedgerId
```

- Reads: `lat`, `lng`, `LedgerName`

## staffgpstracking_service

Screen: **GPS staff-tracking service** · [spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md#gps-staff-tracking-service--staffgpstrackingservice) · 5 statements

#### `getQueuedCount` · line 179
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `StaffLocationQueue`
```sql
select count(*) from StaffLocationQueue
```

#### `savePingLocally` · line 160
**local SQLite** · `sqlite.execSQL` · INSERT · tables: `StaffLocationQueue`
```sql
insert into StaffLocationQueue(FinId,StaffLedgerId,TrackDate,Lat,Lng,Accuracy,Speed,BatteryLevel,IsMockLocation,DeviceTime)
values(?,?,?,?,?,?,?,?,?,?)
```

- Selection args: `globalvars.finid`, `globalvars.currentcollectionexecutivelederid1`, `trackdate`, `String.valueOf(loc.getLatitude())`, `String.valueOf(loc.getLongitude())`, `Integer.valueOf((int) loc.getAccuracy())`, `Integer.valueOf((int) loc.getSpeed())`, `Integer.valueOf(getBatteryLevel())`, `Integer.valueOf(ismock ? 1 : 0)`, `devicetime`

#### `AsyncUploadLocationQueue.doInBackground` · line 247
**local SQLite** · `sqlite.rawQuery` · SELECT · tables: `StaffLocationQueue`
```sql
select * from StaffLocationQueue order by QId limit 200
```

- Reads: `FinId`, `StaffLedgerId`, `TrackDate`, `Lat`, `Lng`, `Accuracy`, `Speed`, `BatteryLevel`, `IsMockLocation`, `DeviceTime`, `QId`

#### `AsyncUploadLocationQueue.doInBackground` · line 270
**SQL Server** · `jdbc.prepareStatement` · selected-branch DB · INSERT · tables: `tblStaffLocationTrack`
```sql
insert into tblStaffLocationTrack(FinId,StaffLedgerId,TrackDate,Lat,Lng,Accuracy,Speed,BatteryLevel,IsMockLocation,DeviceTime)
values(?,?,?,?,?,?,?,?,?,?)
```

- Params: 1 → `c.getInt(c.getColumnIndexOrThrow("FinId"))` (setInt) · 2 → `c.getInt(c.getColumnIndexOrThrow("StaffLedgerId"))` (setInt) · 3 → `c.getString(c.getColumnIndexOrThrow("TrackDate"))` (setString) · 4 → `c.getString(c.getColumnIndexOrThrow("Lat"))` (setString) · 5 → `c.getString(c.getColumnIndexOrThrow("Lng"))` (setString) · 6 → `c.getInt(c.getColumnIndexOrThrow("Accuracy"))` (setInt) · 7 → `c.getInt(c.getColumnIndexOrThrow("Speed"))` (setInt) · 8 → `c.getInt(c.getColumnIndexOrThrow("BatteryLevel"))` (setInt) · 9 → `c.getInt(c.getColumnIndexOrThrow("IsMockLocation"))` (setInt) · 10 → `c.getString(c.getColumnIndexOrThrow("DeviceTime"))` (setString)

#### `AsyncUploadLocationQueue.doInBackground` · line 288
**local SQLite** · `sqlite.execSQL` · DELETE · tables: `StaffLocationQueue`
```sql
delete from StaffLocationQueue where QId=${qid}
```

## staffgpstrackmap_act

Screen: **Staff Route (GPS track replay)** · [spec](../../brain/modules/09-collection-areas-maps-gps-staff-tracking.md#staff-route-gps-track-replay--staffgpstrackmapact) · 1 statements

#### `AsyncLoadTrack.doInBackground` · line 371
**SQL Server** · `jdbc.Statement.executeQuery` · branch-report DB · STAFFLOCATIONTRACKDISPLAY
```sql
StaffLocationTrackDisplay @FinId=${globalvars.finid}, @StaffLedgerId=${staffledgerid}, @TrackDate='${new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedcal.getTime())}'
```

- Reads: `Lat`, `Lng`, `Accuracy`, `Speed`, `BatteryLevel`, `IsMockLocation`, `DeviceTime`

