# 8.1 App start, registration, device identity, settings and access control

[← brain.md](../../brain.md) · Module 8.1 of 20 · Next: [8.2 Navigation hub, routers and line/branch selection](02-navigation-hub-routers-line-branch-selection.md)

This module covers everything between the launcher icon and a working, authorised device: the vendor web-service registration (a 6-character Registration Key is exchanged for SQL Server connection details), the three database-distribution paths (1 = vendor-hosted customer DB, 2 = customer's on-premises SQL Server typed in by hand, 3 = shared Play-Store demo DB with self-registration and e-mail/SMS OTP), and "Sync Resources", which pulls menus, roles, rights, loan methods and server feature flags into SQLite, static fields and prefs. It also holds the administrator screens that decide who may use the app: device approval and line access (`MobileDevlicesAllowed`/`FinanceAccessAllowed`), branch-level device access on the master DB (`BranchMobileDevlicesAllowed`/`BranchAccessAllowed`), per-role menu rights (`AndroidMenus`/`tblSalesDetailsforAndroid`) and the 20 special access rules (`SpecialAccessRules`/`SpecialAccessAllowed`). The server-side Settings screen (`SoftSettings`), the device-info/language screen and the vendor-website viewer are here too. All registration state lives in SharedPreferences `venusmicrofinpref011`. DB credentials arrive from the vendor web service (or manual entry) and are stored there in plain text.

## Splash / boot router — `splash`
| | |
|---|---|
| **Type** | Activity (`android.app.Activity`); launcher (`MAIN`/`LAUNCHER`), `exported=true`, theme `@android:style/Theme.NoTitleBar`; window flag 1024 (`FLAG_FULLSCREEN`) |
| **Layout(s)** | `activity_splash.xml` |
| **Opened from** | Launcher icon. This is the only entry point, so it is also how the user gets back in after "Clear My Data" or after saving local-server settings (both close the app). |
| **Opens** | `navigationdrawactivity` (device registered) or `collectdbinfofromweb` (not registered); always `finish()`es |
| **Data** | Opens/creates SQLite `venusmicrofinv005` (no table access) · prefs read: `applangid`, `dbidentificationno`, `isdbregistered`, `ismasterdb`, `MasterDBconnString/MasterDBusername/MasterDBpassword`, `selbranchconnString/selbranchdbusername/selbranchdbpassword`, `branchconnString/branchdbusername/branchdbpassword` · no SQL Server or web access |

**Purpose.** An invisible router. It applies the saved language, captures the device id, asks for runtime permissions, then sends the user either into the app or into first-run registration.

**UI.** "Venus Solutions" / "WELCOMES YOU" (same text in Tamil), logo `imageView2`, hidden status text `pstatusid`, hidden ProgressBar. A `visibility=gone` panel "Kindly Select Your APP Type" holds the buttons "Login with Registration Key", "Local Server Registration" and "Demo Database". They have no ids or handlers, so this is dead UI; the path is chosen by the server's distribution type instead.

**Menu / actions.** None.

**How it works.**
1. `onCreate`: opens SQLite, goes full screen, reads `applangid` (default 1) and maps it with `defaulttables.getlanguageshortname` (1 → `ta` "தமிழ்", 2 → `en` "English"). It always calls `setLocale(ta, IN)` first, then calls `setLocale(ta)` again or `setLocaleforeng(en)`.
2. Sets `globalvars.device_id = Settings.Secure.ANDROID_ID`. This is the "IMEI" / "Android Device Id" stored in `MobileDevlicesAllowed.IMEI`, `FinanceAccessAllowed.IMEI`, etc. Also sets `globalvars.dbidentificationno` from the pref.
3. `requestPermissions()`: CAMERA, CALL_PHONE, INTERNET, READ_PHONE_STATE, SEND_SMS, BLUETOOTH_CONNECT, BLUETOOTH_SCAN (+ READ_MEDIA_IMAGES on API ≥ 33). It requests the missing ones with request code 100; if none are missing it calls `proceedwithapp()` directly. `onRequestPermissionsResult(100)` calls `proceedwithapp()` whether or not the permissions were granted.
4. `proceedwithapp()`:
   - `isdbregistered && !ismasterdb` → the `branch*` and `selbranch*` globals are loaded from the `branchconnString/branchdbusername/branchdbpassword` prefs; `Imagesupload*` and `branchlevelReport_*` are set to the branch connection → `navigationdrawactivity`.
   - `isdbregistered && ismasterdb` → `MasterDB*` and `selbranch*` are loaded from prefs; `Imagesupload*` is set to the master connection; `branch*` and `branchlevelReport_*` come from the `branch*` prefs → `navigationdrawactivity`.
   - otherwise → `collectdbinfofromweb`.

**First-run state machine (end to end).**
1. Launch → `splash`. If the device is not registered → `collectdbinfofromweb`.
2. The key is sent to `installpendingcustomer.aspx`; the result branches on `dbdistributiontypeid`:
   - **1** (online customer DB): the master connection is saved with `isdbregistered=true` and the app goes straight to the drawer. There is no OTP. The drawer shows almost no menus until "Sync Resources" is run, and the sync answers "Permission Denied.." unless an admin has already added this device id in Access Control.
   - **2** (on-premises): → `sqlserverconnectionproperties`.
   - **3** (demo): the demo-DB connection is saved but `isdbregistered=false` → `playstoredemouserregistrationact`.
3. Type 2: Test + Save write the `branchconnString…` prefs, `isdbregistered=true` and `dbidentificationno="LocalServer"`, then the screen closes and the app exits. The next launch goes to the drawer.
4. Type 3: the user enters name, mobile, finance, city and e-mail → `microfinappinstalations.aspx`. The server returns `Appinsid` plus an OTP pair and sends the OTP by e-mail/mobile → `otpverificationact`.
5. OTP accepted → `isdbregistered=true` → `getdemodbdetailsandmakeadmindevice.aspx` (the server creates this device as an admin device in the demo DB) → `syncresources` with `issyncneeded=true`.
6. Sync succeeds → `navigationdrawactivity` with `issyncneeded=true`. Its `ASYNCfirstautosyncprocess` runs `GetAllowedFinances @IMEI` into `FinanceDetails`, makes the first line current and clears the transactional tables. It then runs `syncresources` again (request code 9026) and sets pref `isfirstsyncdone=true`.
7. Later launches go `splash` → drawer.
8. Reset: drawer "Clear My Data" calls `removemfinstalationdetails.aspx?deviceid=` (JSON `isdeviceremoved`). It then wipes internal storage, cache, all prefs and the SQLite DB, even if that call failed, and exits the app ("Yes, Keep the APP" / "Want to Uninstall the APP"). The next launch starts again at step 1.

The prefs `dbdistributiontypeid` and `isfirstsyncdone` are written but never read anywhere.

**Functions.**
- `onCreate()` — locale, device id, permission request.
- `requestPermissions()` — builds the list of missing permissions, then requests them (code 100) or proceeds.
- `onRequestPermissionsResult()` — proceeds regardless of the result.
- `proceedwithapp()` — routing; loads the connection globals from prefs.
- `setLocale(ctx, lang, country)` — `Locale.setDefault` + `resources.updateConfiguration`.
- `setLocaleforeng(ctx, lang)` — sets the default locale and mutates the configuration; calls `createConfigurationContext` but discards the result.
- `isNetworkConnected()` — never called.

**Rules & edge cases.**
- Denying permissions never blocks the app; SMS, Bluetooth printing and the camera simply fail later.
- English: Tamil is applied first, and `setLocaleforeng` never calls `updateConfiguration`. English works only because the shared Configuration object is mutated in place, which is fragile.
- In the non-master path, `Imagesuploaddbpassword` is set from `globalvars.MasterDBpassword`, which is not loaded in that path (likely null or stale).
- There is no network check before routing.

## Registration key entry — `collectdbinfofromweb`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); `exported=false`, theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_collectdbinfofromweb.xml` |
| **Opened from** | `splash` when `isdbregistered` is false |
| **Opens** | `navigationdrawactivity` (type 1), `sqlserverconnectionproperties` (type 2), `playstoredemouserregistrationact` (type 3); the YouTube app or browser for the demo video |
| **Data** | Web GET `installpendingcustomer.aspx` (via helper `jsondataretriver.retrivedatabaseinfofromweb`) → model `dbdetailsobj` · prefs written: `isdbregistered`, `ismasterdb`, `dbidentificationno`, `dbdistributiontypeid`, `MasterDBconnString/MasterDBusername/MasterDBpassword`, `branchconnString/branchdbusername/branchdbpassword`, `selbranchconnString/selbranchdbusername/selbranchdbpassword`, `branchlevelReport_branchconnString/…username/…password` · globals: the same, plus `finid=0` |

**Purpose.** First-run registration. The customer types the 6-character Registration Key issued by Venus Solutions, and the vendor web service returns the SQL Server database the app should use.

**UI.** Logo, "Venus Solutions", "Contact Nos 📱" with the vendor numbers +919655842840, +919790070227, +919487792840 (Tamil variant prefixes "Contact Number:"), "Enter Your Registration Key", and a hint "DEMO Registration Key" "M M M M M M". Below: six one-character boxes `et_key_1…6` (maxLength 1, capitals), "📨 Submit" (`btn_submit_key`), progress `collectdbpbarid`, and "▶️ Watch Video" (`btn_watch_video`).

**Menu / actions.**
- Typing a character moves focus to the next box. Backspace (key code 67) in an empty box moves to the previous box and clears it.
- Submit → `getOtp()` (misnamed) joins the trimmed boxes into the key → `asycgetdbdetails`.
- Watch Video → `ACTION_VIEW https://youtu.be/tmIKAI2uftM`, in the YouTube app if installed, otherwise the browser.

**How it works.**
1. `doInBackground`: GET `http://microfinapp.venussolutions.co.in/installpendingcustomer.aspx?databaseverificationcode=<KEY>` through a plain `URLConnection` (cleartext, no timeouts). The body must be a JSON **array** (`new JSONArray(...)`); any exception (no network, non-array body) sets the error flag.
2. `onPostExecute` (success): Gson maps the array to `List<dbdetailsobj>`; fields `CId`, `ServerName`, `DatabaseName`, `DBUserId`, `DBPassword`, `ismasterdb`, `dbdistributiontypeid`, `dbidentificationno`. Element 0 is used. If Gson fails, the method returns silently and the spinner keeps turning.
3. `globalvars.ismasterdb` is set from the response. Then:
   - **type 2**: `putBoolean(isdbregistered,true)` is called without `apply()`, so nothing is persisted. Toast "Verified" → `sqlserverconnectionproperties`.
   - **other types**: builds `MasterDBconnString = jdbc:jtds:sqlserver://<ServerName>:1433/<DatabaseName>;encrypt=false;user=<DBUserId>;password=[REDACTED];` (the password comes from the response) and copies it into `selbranch*`, `branch*` and `branchlevelReport_*`. Writes all of these to prefs plus `dbidentificationno`, `ismasterdb`, `isdbregistered=true`, `dbdistributiontypeid`, then shows toast "Verified".
   - **type 1** → `navigationdrawactivity`.
   - **type 3** → sets `isdbregistered=false` again → `playstoredemouserregistrationact`.
   - **any other type**: stays on the screen, already marked registered.
4. Error path: dialog "Connection Error" — "Could not verify the Instalation\nDo you want DEMO APP, pls enter MMMMMM as Registration Key". OK hides the spinner.

**Functions.**
- `onCreate()` — sets up the key boxes, Submit and Video buttons.
- `lambda$onCreate$0` — backspace navigation between boxes.
- `lambda$onCreate$1` — Submit.
- `lambda$onCreate$2` — opens the YouTube link.
- `getOtp()` — concatenates the six boxes.
- `asycgetdbdetails.doInBackground/onPostExecute/onPreExecute` — web call, parsing, prefs and routing.

**Rules & edge cases.**
- The key length is not validated.
- An empty array `[]` from the server crashes the app (`items.get(0)` on the UI thread).
- **Security:** anyone holding a 6-character key receives the customer's SQL Server login and password over plain HTTP. They end up in plain SharedPreferences, and the app is `debuggable=true` / `allowBackup=true`. The demo key MMMMMM is printed on this screen.
- The `gb2312` encoding argument of `jsondataretriver` is ignored; the default charset is used.
- `publishProgress` messages are discarded. JADX shows the constant `ExifInterface.GPS_MEASUREMENT_2D`; it is just the string "2".

## Local SQL Server connection setup — `sqlserverconnectionproperties`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), edge-to-edge; title "Server Connection Properties" |
| **Layout(s)** | `activity_sqlserverconnectionproperties.xml` |
| **Opened from** | `collectdbinfofromweb` when `dbdistributiontypeid` = 2 (DB on the customer's premises) |
| **Opens** | Nothing. Save → `onBackPressed()`; the back stack is empty, so the app closes. |
| **Data** | SQL Server: connection test only, no query · prefs read: `servername`, `sqlinstancename` (default "SQLExpress"), `dbusername` (default "sa"), `databasename`, `dbpassword` · prefs written: `isdbregistered=true`, `branchconnString`, `branchdbusername`, `branchdbpassword`, `dbidentificationno` · globals `branchconnString/branchdbusername/branchdbpassword`, `dbidentificationno="LocalServer"`, `globalvars.db` |

**Purpose.** Manual entry of the on-premises MS SQL Server (LAN IP, named instance, login, database) for customers who host the FinSoft DB themselves.

**UI.** Fields (labels identical in Tamil): "MS SQL Server IP Address" (`tvservernameid`, layout default 192.168.0.100), "SQL Instance" (`tvinstancenameid`, "SQLExpress"), "User Name" (`etusernameid`, "sa"), "Password" (`etpasswordid`, textPassword; the layout carries a default literal [REDACTED]), "Database Name" (`etdbnameid`, "mfdbonpremises"). Buttons "CONNECTION TEST" and "SAVE", plus a progress bar. At start-up all five fields are overwritten from prefs whose keys are never written anywhere, so the user always sees an empty server/DB/password, "SQLExpress" and "sa".

**Menu / actions.**
- **CONNECTION TEST**:
  1. Builds `jdbc:jtds:sqlserver://<ip>;databaseName=<db>;instance=<instance>;user=<user>; password=[REDACTED]`.
  2. Immediately saves it to prefs, together with `isdbregistered=true`.
  3. Runs `async_checkconnectionstatus` (`DriverManager.getConnection` + `createStatement`).
  4. Shows a dialog: "Connect..." / "Connection succeed", or "Unable to Connect..." with the driver message.
- **SAVE**: if there was no successful test in this session → dialog "Test is not completed..." with the last status (null if never tested). Otherwise it rebuilds the string from the current fields, writes the same prefs, shows toast "Settings Updated" and calls `onBackPressed()`.

**How it works.** As above. `ismasterdb` is never written, so the next launch treats the install as a single-DB (non-master) install: `splash` loads `branchconnString` into `branch*` and `selbranch*`.

**Functions.**
- `onCreate()` — pre-fills the fields from prefs.
- `lambda$onCreate$3` — Test.
- `lambda$onCreate$2` — Save.
- `async_checkconnectionstatus.doInBackground/onPostExecute` — opens the JDBC connection and reports.

**Rules & edge cases.**
- The device is marked registered even when the test fails. Leaving after a failed test makes the next launch open the drawer with a broken connection; the only way out is Clear My Data or a reinstall.
- Save uses whatever is in the fields at save time, which may differ from what was tested.
- The password is stored in plain prefs.
- The URL has a space before `password=`.

## Play-Store demo registration — `playstoredemouserregistrationact`
| | |
|---|---|
| **Type** | Activity (`android.app.Activity`), default theme |
| **Layout(s)** | `activity_playstoredemouserregistration.xml` |
| **Opened from** | `collectdbinfofromweb` when `dbdistributiontypeid` = 3 (e.g. key MMMMMM) |
| **Opens** | `otpverificationact` (extras `otpValueactual`, `otpValueopen`, `curinsobj` = `appinsobj`); finishes itself |
| **Data** | Web GET `microfinappinstalations.aspx` · no DB or prefs (uses `globalvars.device_id`) |

**Purpose.** A demo user introduces themselves; the vendor server records the installation and sends an OTP.

**UI.** Logo `reglogo`, "VENUS SOLUTIONS" / "WELCOMES YOU" / "Kindly Introduce Yourself". Fields: Name (`login_contactperson`), Mobile No (`login_contactno`, number), Finance Name (`login_financename`), City (`login_cityname`), EMail (`login_emailid`). Button "Register and SEND OTP" (`login_sendotp`), ProgressBar `loading`. A "Connectig....." text exists but is never shown.

**Menu / actions.** Register:
- Checks `Patterns.EMAIL_ADDRESS` and `Patterns.PHONE`. On failure: "Warning..." — "Kindly ensure your Email and Mobile No are correct."
- On success: disables the button (text "Working"), copies the fields into `appinsobj` and runs `Async_updateuserandsendotp`.

**How it works.**
1. GET `http://microfinapp.venussolutions.co.in/microfinappinstalations.aspx?FinanceName=&ContactNo=&ContactPerson=&City=&deviceid=<ANDROID_ID>&Email=`. Values are URL-encoded only on API ≥ 33; on API 28–32 they are concatenated raw. Method "GET" (JADX shows the constant `HttpGet.METHOD_NAME`), 30 s connect and read timeouts.
2. On HTTP 200 the JSON object is read: `OtpValueactual` (string), `OtpValueopen` (string), `Emailstatus` (bool), `Appinsid` (int → `curinsobj.appinsid`). An `ApiResponse` holder is also built but not used.
3. `onPostExecute`:
   - `Emailstatus` true → "Info..." — "An OTP has been sent to your email and mobile no. Please check your inbox and spam/junk folder." OK opens the OTP screen.
   - false (also on network error or non-200) → "Unable to send OTP to your Email, Please try again later.... ". OK re-enables the button with the text "Submit".

**Functions.**
- `onCreate()` — wires the button.
- `lambda$onCreate$0` — validates and submits.
- `showalert()` — the invalid e-mail/mobile dialog.
- `isValidMail()`, `isValidMobile()` — Android `Patterns` checks.
- `Async_updateuserandsendotp.doInBackground/onPostExecute` + dialog lambdas — web call and dialogs.
- `ApiResponse` — immutable holder: `otpValueactual()`, `otpValueopen()`, `getemailstatus()`, `appinsid()`.

**Rules & edge cases.**
- Name, Finance Name and City are not validated and may be blank.
- On API < 33, unencoded spaces, `&` or `#` in names corrupt the query string.
- The OTP pair travels back to the phone in clear text (see the next screen).

## OTP verification and demo admin-device registration — `otpverificationact`
| | |
|---|---|
| **Type** | Activity (`android.app.Activity`), default theme. A 118 KB class; most of it is unreachable copy-pasted code (see step 4). |
| **Layout(s)** | `activity_otpverification.xml` |
| **Opened from** | `playstoredemouserregistrationact` (extras `otpValueactual`, `otpValueopen`, `curinsobj`). It also reads an extra `appinsid` that no caller supplies (always 0, unused). |
| **Opens** | `syncresources` (extra `issyncneeded=true`) once the device has been made an admin device. The dead code would open `navigationdrawactivity`. |
| **Data** | Web GET `microfinresendotpandemail.aspx` and `getdemodbdetailsandmakeadmindevice.aspx` · prefs written by live code: `isdbregistered=true` · SQL in dead code only (see step 4) |

**Purpose.** Checks the 4-digit OTP sent to the demo user's e-mail/mobile, then asks the vendor server to create this device as an admin device of the shared demo DB and starts the first sync.

**UI.** "Customer ID : <Appinsid>" (`tv_otp_appinsid`), "Enter the OTP sent to your", "Mobile : <mobile>" (`tv_otp_mobile`; the layout placeholder "Mobile: 9790070226" is replaced at runtime), "EMail : <email>" (`tv_otp_email`). Four numeric boxes `et_otp_1..4`, "Submit" (`btn_submit_otp`), a "Resend OTP" link (`tv_resend_otp`) and ProgressBar `loading`. Strings are English only.

**Menu / actions.**
- Typing a digit moves to the next box; Backspace in an empty box clears the previous one.
- Submit → `getOtp()`:
  - length ≠ 4 → toast "Please enter a valid 4-digit OTP";
  - equal to `otpValueactual` **or** `otpValueopen` → pref `isdbregistered=true` → `asyncaddasadmindeviceandgetdbdetails`;
  - otherwise → dialog "OTP Error.........." / "INVALID OTP".
- Resend OTP → `Async_updateuserandsendotp`.

**How it works.**
1. `asyncaddasadmindeviceandgetdbdetails`:
   - GET `getdemodbdetailsandmakeadmindevice.aspx?ContactPerson=<name>&deviceid=<ANDROID_ID>&AppInsId=<Appinsid>` (encoded on API ≥ 33; 5 s timeouts) → JSON `Isdeviceadded`.
   - true → start `syncresources` with `issyncneeded=true` and finish.
   - false or error → "Info..." — "Unable to Register your device, Please try again later.... ".
   - The rows written in the demo DB (`MobileDevlicesAllowed`/`FinanceAccessAllowed`, admin role) are created server-side and are not visible in the app.
2. `Async_updateuserandsendotp` (resend):
   - GET `microfinresendotpandemail.aspx?FinanceName=&ContactNo=&ContactPerson=&City=&deviceid=&otpsent=<otpValueactual>&Email=` (30 s) → JSON `Emailstatus`.
   - true → "An OTP has been sent to your email and mobile no. …"; false → "Unable to send OTP to your Email, Please try again later.... ".
   - The original OTP is sent back to the server as `otpsent`, presumably so the server re-sends the same code. The screen keeps checking against the original values; no new OTP is read.
3. The OTP is generated on the server. Checking it is a local string comparison against the two values received in the registration response.
4. **Dead code.** `ASYNCSettings` is never constructed anywhere in the DEX, and `AsyncDownloaddata` is started only by it. Both are copies of the old first-sync and download logic:
   - `ASYNCSettings`, on `branchconnString`, pulls:
     - `SMSTemplates`;
     - menu rights (`AndroidMenus` ⟕ `tblSalesDetailsforAndroid`);
     - `SpecialAccessAllowed` (inserted twice);
     - `LoanMethods`, `tblGroups`, `LoansAllowed`;
     - `SoftSettings`, including SSIds not read by `syncresources`: 55 `enablelinewisestaff`, 56 `disabletodayCollectionforIndividualcollectionEntry`, 57 `showopeningBalASZeroforexecutive`, 59 `STLIntRate`, 60 `OFIntRate`;
     - `venussoftsettings` (VSSId 22–25);
     - `CollectionExecutives`;
     - `getCashandBankLedgers` → `CashBankLedgers`;
     - `tblSTLoans` + `GetSTLoansStatus` → `STLoanStatusforAPP`;
     - `tblOFLoans` + `GetOFLoansStatus` → `OFLoanStatusforAPP`;
     - the full `tblLedgers` and `tblFinwisePartners`;
     - `GetAllowedFinances @IMEI` → `FinanceDetails`.
   - Its `onPostExecute` would:
     - set the current-line globals and prefs `FinId`, `CollectionDate`, `MobileUserLedgerId`, `FinanceName`, `AdminMobileNos`, `CashBookLedgerId`, `appusername`, `FinanceCashBookLedgerId`, `smsidentifier`, `BankLedgerid` and `isselectedlineisweeklyline` (weekly when `CollectionBreakString` is "W"/"ww"; JADX shows "W" as `ExifInterface.LONGITUDE_WEST`);
     - clear `MobileAppCollData`, `LastUpload`, `NewParties`, `NewMCLoans`, `SubAreaDetails`, `ExpensesEntries`;
     - start `AsyncDownloaddata`.
   - `AsyncDownloaddata` (a copy of `downloaddata`):
     - Device check: `FinanceAccessAllowed` ⨝ `MobileDevlicesAllowed`, plus `DownloadControling` gated by SoftSettings SSId 32.
     - Role, then `subareadetails` (filtered by weekday for weekly lines).
     - Collection list from one of `APP_MCLoancollectionDatawithLoanClosoureMonitering_withWeekdayssupport` / `…_withoutWeekdayssupport` / `APP_MCLoancollectionDataAUTOLOANCLOSOURE_WithWeekdaysSupport` / `…_WithOUTWeekdaysSupport` (`@ColDate`, `@FinId`) → `MobileAppCollData`.
     - `dbo.showLedgerBalance(CashLedgerId,FinId)` → pref `openingbalancedownloaded`.
     - Parties (`tblLedgers` ScreenType 3) → `Parties` (column "Age"; JADX shows the constant `HttpHeaders.AGE`).
     - `ExpHeaders`: TTId 7 from `PattiExpLedgers`, TTId 8 from GroupId 2/3/4/16/19.
     - `PrepareLast5CollectionsforPrev5dates` / `PrepareLast5Collections` → `PrevCollections`.
     - Subscription dialogs "Subscription will be expired on dd-MM-yyyy\nKindly pay the renewal charges" / " Subscription expired \n Kindly pay the renewal charges" (title "தங்கள் கவனத்திற்கு..." = "For your attention…"), whose flags are never set in this class.

**Functions.**
- `onCreate()` — reads the extras, fills the labels, wires the OTP boxes and buttons.
- `lambda$onCreate$0` — backspace handling.
- `lambda$onCreate$2` — Submit and OTP check.
- `lambda$onCreate$3` — Resend.
- `getOtp()` — joins the 4 boxes.
- `asyncaddasadmindeviceandgetdbdetails.doInBackground/onPostExecute/lambda$onPostExecute$0` — make-admin call and routing.
- `Async_updateuserandsendotp.doInBackground/onPostExecute/lambda$onPostExecute$1` — resend.
- `ASYNCSettings.doInBackground/onPostExecute/onProgressUpdate` — dead full master sync.
- `AsyncDownloaddata.onPreExecute/doInBackground/onPostExecute` + 4 dialog lambdas — dead collection download.

**Rules & edge cases.**
- **Security:**
  - Both OTP values reach the device in the plain-HTTP registration response and are compared locally, so the OTP can be read off the network or bypassed by patching the app.
  - `OtpValueopen` is a second value that is always accepted.
  - The make-admin call carries no proof of the OTP, only name, device id and `AppInsId`.
- `isdbregistered` is set **before** the admin-device call. If that call fails, the next launch still enters the drawer on the demo DB, where sync answers "Permission Denied..".
- **Crash:** the OK buttons of the "Unable to Register…" and "Unable to send OTP…" dialogs call `findViewById(R.id.login_sendotp)`. That button exists only in the previous screen's layout, so the app throws a NullPointerException.
- The live path performs no licence or expiry check; expiry is evaluated only in `downloaddata`.
- JADX failed type inference in `ASYNCSettings.doInBackground` and rendered the catch blocks inside the loops. This has no effect at runtime because the code never runs.

## Sync Resources — `syncresources`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), edge-to-edge; title "SYNC RESOURCES"; horizontal `ProgressDialog` |
| **Layout(s)** | `activity_syncresources.xml` (logo only) |
| **Opened from** | Drawer "Sync Resources" (`nav_sync`, `startActivityForResult` request code 9026 `requestcodes.syncresourcescalled`); `navigationdrawactivity.ASYNCfirstautosyncprocess` (9026); `otpverificationact` (extra `issyncneeded=true`) |
| **Opens** | `navigationdrawactivity` (extra `issyncneeded=true`), but only when it was started with `issyncneeded`; otherwise it just finishes. The drawer runs `syncsettings()` on request code 9026 whatever the result. |
| **Data** | SQL Server: the selected branch DB (`selbranchconnString`/`selbranchdbusername`/`selbranchdbpassword`) for everything, plus the master DB (`MasterDBconnString`) for one query; proc `getCashandBankLedgers`; tables listed below · SQLite tables listed below · prefs: `userroleid`, `appusername`, `isFullAccessAllowed`, `isbranchusercontrolallowed` and about 65 soft-setting keys |

**Purpose.** Refreshes all master data, permissions and server-controlled feature flags for this device and branch into SQLite, static fields and prefs. It runs twice after demo registration (OTP screen, then the drawer's first auto-sync) and whenever the user taps "Sync Resources". A branch change does not trigger it automatically.

**UI.** Logo, and a progress dialog "Sync Begins ..." with the steps "Clearing temp data", "Connecting", "Sync User rights", "Sync User Roles", "Sync Account Ledgers", "Special Access Rules", "Sync Loan Methods", "Account Groups...", "Sync Soft Settings", "Sync Collection Executives", "Sync Completed".

**Menu / actions.** None; it starts automatically.

**How it works.**
1. Creates (if needed) and **empties** the local tables `CollectionExecutives`, `LoanMethods`, `tblGroups`, `LoansAllowed`, `tblLedgers`, `tblFinwisePartners`, `RelatedDocCollection`, `tblSalesDetailsforAndroid`, `SMSTemplates`, `userroles`, `CashBankLedgers`, `SpecialAccessRules` and `SpecialAccessAllowed`; makes sure `subareadetails` exists.
2. Connects through jTDS to the selected branch DB.
3. Device authorisation check:

   `select top 1 x.IMEI,userroleid,c.isFullAccessAllowed,d.LedgerName from FinanceAccessAllowed x, MobileDevlicesAllowed y, UserRoles c, tblLedgers d where d.LedgerId=y.LId and y.userroleid=c.URId and x.IMEI=y.IMEI and y.isActive=1 and x.IMEI='<device_id>'`

   It sets `isactiveuser`, `globalvars.APPUserName`, `app_userroleid` and `isFullAccessAllowed`, plus the prefs `userroleid`, `appusername` and `isFullAccessAllowed`. If no row comes back, nothing else is pulled.
4. Pulls from the server into SQLite:

| # | Server source | Local target / effect |
|---|---|---|
| 1 | `SMSTemplates` (STId, SMSTitle, SMSDesc) | `SMSTemplates` |
| 2 | `AndroidMenus a left join tblSalesDetailsforAndroid b on a.mnuid=b.mnuid and b.UserLevelId=<role> where a.isAllowed=1 and (isonlyformasterdb='FALSE' or isonlyformasterdb='<ismasterdb>')` | `tblSalesDetailsforAndroid` (UserLevelId, mnuId, mnustyle, Show/View/Update/Add/DeleteState, mnuname, mnudisplayname): the role's menu rights |
| 3 | `userroles` (URId, URName, isvisible, DisplayIndex, isFullAccessAllowed) | `userroles` |
| 4 | `exec getCashandBankLedgers @URId=<role>` | `CashBankLedgers` (LedgerId, LedgerName, Finid, Alloted, LedgerType, istobeVerified, GroupId) |
| 5 | `SpecialAccessRules` (SARId, SAName, ValueType, isAllowed) | `SpecialAccessRules` |
| 6 | `SpecialAccessAllowed where UserLevelId=<role>` | `SpecialAccessAllowed` → `globalvars.specialaccessrules`. Here only SARId 1–13 are mapped; 14–20 are mapped when the drawer's `syncsettings()` re-reads the table. |
| 7 | `LoanMethods` (LoanMethodId … WarningDays, DueSupportAmount; 20 columns) | `LoanMethods` |
| 8 | `tblGroups` (GroupId, GroupName, GroupUnder, ShowId, isShowDetails, isVisible, ANId) | `tblGroups` |
| 9 | `LoansAllowed` (LId, LoanIndex) | `LoansAllowed` |
| 10 | `SoftSettings` (SSId, SSDesc, SSValue, Datevalue, bitvalue, NumericValue) | `softsettingsvalue.*` statics + prefs (no local table). The SSId map for the Settings screen is in the `settings_act` table below. |
| 11 | `venussoftsettings` (VSSId, VSSName, CharValue, NumericValue, BitValue, DateValue) | VSSId 1 `vss_branchname`, 22 `vss_executivehandlingmethod` (1 offline / 2 online), 23 `vss_enablelivelocationduringcollectionentry`, 24 `vss_enablestloansincollection`, 25 `vss_enableofloansincollection`, 26 `vss_enablefinancecashbook`, 27 `vss_IsthisDBunderBranchManagement` (+ prefs with the same names). This is the vendor's own table; no app screen edits it. |
| 12 | `stockgrp`; only if it has rows: `stockcat`, `stockUOM`, `ItemMaster`, `StockUOM` | Inventory tables with the same names |
| 13 | `RelatedDocmenttypes` (DUID, DocDesc, isvisible, showid, isImageAllowed); only if SSId 72 `EnableUploadLoanRelatedDocuments` is on | `RelatedDocmenttypes` |
| 14 | `tblLedgers where ScreenType=8 and designationid=1 and desigstatusid=1` | `CollectionExecutives` (LedgerId, LedgerName, FinId) |
| 15 | Master DB: `select isbranchusercontrolallowed from BranchMobileDevlicesAllowed where IMEI=…`; only if `ismasterdb && iscurrentuserhavemultibranchaccess`, otherwise false | `globalvars.isbranchusercontrolallowed` + pref |

5. SoftSettings read here that are not on the Settings screen (the pref key is the field name unless noted):
   - 4 `businessnameinapp` → pref `FinanceGroupName`
   - 15 `reportingemailaddress`
   - 18 `partiesimagepath`; 19 `idproofimagepath`
   - 21 `enablepartyimageloading`; 22 `enableidproofimageloading`; 48 `enableIIidproofimageloading`
   - 24 `idproof_file_upload_url`; 25 `party_image_file_upload_url`; 26 `daily_report_file_upload_url`; 27 `email_sending_url`
   - 49 `allowdecimalinamounts`; 50 `smsmethod`
   - 72 `EnableUploadLoanRelatedDocuments`; 73 `LoanRelatedDocumentsUploadURL`; 74 `LoanRelatedDocumentsUploadPATH`
   - 101 `enablecashbookverification`; 102 `AreawisePartiesDisplayOrder`
   - 201 `LimitAccountTransactionstoCurrentLine_201` → pref `LimitAccountTransactionstoCurrentLine`
   - 401 `GroupnameinsteadofAreaname_401`
6. `onPostExecute`:
   - error → dialog "Error Info" (line number + exception text) → finish;
   - device not authorised → "Info" / "Permission Denied.." → finish;
   - success → rebuilds `globalvars.salesdetailsforandroid_datalist` from the local `tblSalesDetailsforAndroid` and shows "Info" / "Sync Completed.." (icon `download_done_48px`). OK opens the drawer if `issyncneeded`, then finishes.

**Functions.**
- `onCreate()` — reads the `issyncneeded` extra and starts the task.
- `asyncdatabasecontents.onPreExecute()` — shows the progress dialog.
- `doInBackground()` — clear, authorise, pull (above).
- `onProgressUpdate()` — updates the message and percentage.
- `onPostExecute()` + `lambda$onPostExecute$0/1/2` — the result dialogs and routing.

**Rules & edge cases.**
- Local master tables are wiped **before** the connection is attempted. A failed sync leaves the device without menus or rights until the next successful sync.
- `tblLedgers` and `tblFinwisePartners` are emptied here but not re-filled.
- To sync, a device needs an **active** `MobileDevlicesAllowed` row **and** at least one `FinanceAccessAllowed` row (line access), linked to an employee ledger and a role.
- `stockUOM` and `StockUOM` are the same SQLite table (table names are case-insensitive), so it is filled twice.
- `HSNSACCode` is read with `getDouble`; a non-numeric code aborts the inventory block.
- A duplicate `SSId == 39` branch makes `selectedfinance_showlast5dayscollectioninareawisecollectionlist` unreachable.
- SoftSettings gaps:
  - SSIds 55, 56, 59 and 60 (`enablelinewisestaff`, `disabletodayCollectionforIndividualcollectionEntry`, `STLIntRate`, `OFIntRate`) are not read, yet their prefs are rewritten from whatever the statics currently hold.
  - 63, 64, 68 and 69 are read but not saved to prefs.
  - 70 is not read.
- Several pref keys differ from the ones `settings_act` writes; see the Settings table.
- JADX type inference failed in `doInBackground`, so the catch blocks appear inside the loops. The logic described above is the evident original: one try/catch around the whole pull and a separate try for inventory.

## Device and APP Info — `deviceidentity`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` with a Toolbar; title "Device and APP Info" |
| **Layout(s)** | `activity_deviceidentity.xml` |
| **Opened from** | Drawer About → "Device Identity" (`nav_deviceid`) |
| **Opens** | `spinnersearch_list` (language picker, request code 30) |
| **Data** | Master DB (`MasterDBconnString`): proc `dbo.InsertBranchNewIMEIRequests`, `BranchMobileDevlicesAllowed` (count) · prefs read/written: `applangid`, `isrequestraisedformultibranchuser`, `branchrequestNIRId`, `branchrequestRequestRaisedAt`, `iscurrentuserhavemultibranchaccess` |

**Purpose.** Shows the identifiers an administrator needs to authorise this phone, the licence validity and the app version. It is also where the user changes the app language and, on multi-branch master databases, requests branch-level access.

**UI.**
- Launcher icon, OS name (`onnametvid`).
- Android Device ID (`deviceidtvid` = ANDROID_ID, the value an admin enters in Access Control).
- "VENUS SOLUTIONS" and the vendor phones 9790070227, 9655842840, 9487792840.
- "Choose Language" field `applanguage_id`, showing "தமிழ்" or "English".
- Button "Raise Request for Branch Level Access" (`btnraisebranchrequest`).
- "Licence Valid up to:<dd-MM-yyyy>" (`deviceinfo_licenseholder`; Tamil string "Licence Valid upto:").
- "Version Code: <versionName>" (`deviceinfo_appversion`; replaces the layout text "APP Version 9.1 released on 10-10-2024").
- "URNo:<dbidentificationno>" (`deviceinfo_dbidentification`, the customer reference number; "LocalServer" for type 2).
- A progress bar.

**Menu / actions.**
- Language field → `spinnersearch_list` "App Language" (1 தமிழ் / 2 English). On result 30: saves `applangid`, applies the locale and shows "Restart Required" — "The app needs to restart to apply the new language. Kindly close and reopen this app".
- Branch-request button, visible only when `globalvars.ismasterdb && !iscurrentuserhavemultibranchaccess`. The first tap runs `asyncraisebranchaccessrequest`. If a request had already been raised when the screen opened, the tap runs `asyncconfirmbranchaccessrequest` instead.

**How it works.**
1. The OS name is the `Build.VERSION_CODES` field at index `SDK_INT`, found by reflection.
2. The licence date comes from `globalvars.expirydate`, which `downloaddata` sets from `FinanceMaster.ExpiryDate`. It is blank if no download has happened yet; the exception is caught.
3. `asyncraisebranchaccessrequest`:
   - Calls `{call dbo.InsertBranchNewIMEIRequests(?,?,?)}` on the master DB: `IMEI` in; `NIRId` out (SQL type 8 = `Types.DOUBLE`); `RequestRaisedAt` out (type 91 = `Types.DATE`).
   - Saves prefs `isrequestraisedformultibranchuser=true`, `branchrequestNIRId` and `branchrequestRequestRaisedAt`.
   - The button text becomes "<NIRId>/<date>".
4. `asyncconfirmbranchaccessrequest`:
   - Runs `select count(IMEI) as imeicount from BranchMobileDevlicesAllowed where IMEI='<id>'`.
   - A count above 0 sets pref `iscurrentuserhavemultibranchaccess=true` and shows the toast "You have access to multiple branches"; otherwise the toast is "ACCESS NOT YET ALLOWED".
   - The drawer picks up the pref on its next refresh.

**Functions.**
- `onCreate()` — fills all labels and wires the language field and request button.
- `lambda$onCreate$1` — raise or confirm the branch request.
- `lambda$onCreate$2` — opens the language picker.
- `onActivityResult()` — saves the language, applies the locale and shows the restart dialog.
- `setLocale()`, `setLocaleforeng()` — same as in `splash`.
- `showRestartConfirmationDialog()` — the "Restart Required" dialog.
- `asyncraisebranchaccessrequest` — calls the stored procedure and saves prefs.
- `asyncconfirmbranchaccessrequest` — counts the approval rows and shows the toast.

**Rules & edge cases.**
- The index-based `VERSION_CODES` lookup can show the wrong name or throw; it is not guarded.
- Raise-versus-confirm is decided from the value captured at `onCreate`, so a second tap during the same visit raises another request.
- Errors in both tasks are silent.
- Approval happens in `branchmobiledeviceaccescontrol_act`; its "GI" button picks up the newest `BranchNewIMEIRequests` row.

## Settings — `settings_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, implements `View.OnClickListener`), edge-to-edge; title "Settings" |
| **Layout(s)** | `activity_settings_appearance.xml` |
| **Opened from** | Drawer Settings → "Settings" (`nav_appearancesettings`) |
| **Opens** | `spinnersearch_list` (request codes 7016, 7022, 9027, 12002); `collectionreportprintingmethods_searchlist` (7021) |
| **Data** | SQL Server (selected branch, `selbranchconnString`): `select SSId,SSDesc,SSValue,Datevalue,bitvalue,NumericValue from SoftSettings`; 39 statements `Update SoftSettings set bitvalue/NumericValue=? where SSId=?` · SQLite `LoansAllowed` (picker for item 25) · prefs written: see the table · statics `softsettingsvalue.*` |

**Purpose.** An administrator edits the company-wide behaviour switches stored in the selected branch DB's `SoftSettings` table: download controls, collection-list display, report parameters, loan-screen limits, party-creation rules, and SMS / data-clearing options. Other devices pick the changes up at their next Sync Resources.

**UI.** Six titled groups of numbered rows. Labels are English only; the button reads "Update", Tamil "சேமி". The full list:

| # | Label (group) | Control | SSId | Field `softsettingsvalue.` | Pref key written on Update (key used by Sync if different) |
|---|---|---|---|---|---|
| 1 | Monitor on Download and Upload (APP-Download and Upload Data) | CheckBox | 32 bit | `enablemoniterondownloadandupload_1` | `enablemoniterondownloadandupload` |
| 2 | BackDays Allowed for Download Data | number | 58 num | `noofbackdaysallowedfordownloaddata_2` | `noofbackdaysallowedfordownloaddata` |
| 3 | Strict on Two Collection Executives | CheckBox | 31 | `strictontwocollectionexecutives_3` | `strictontwocollectionexecutives` (sync `StrictonTwoCollectionExecutives`) |
| 4 | Allow Overall List Download | CheckBox | 30 | `allowoveralllistdownload_4` | `allowoveralllistdownload` (sync `AllowOverallListDownload`) |
| 5 | Monitoring on Loan Closure | CheckBox | 14 | `enablemoniteringonloanclosoure_5` | `enablemoniteringonloanclosoure` (sync `isEnableMoniteringonLoanclosoure`) |
| 6 | Show Party No in Collection List (APP-Collection List) | CheckBox | 20 | `showpartynoincollectionlist_6` | same name |
| 6a | Visit Checkup | CheckBox | 23 | `enablevisitcheckup_6a` | `enablevisitcheckup` |
| 7 | Show Last 5 Days Collection in Area Wise Collection List | CheckBox | 39 | `showlast5dayscollectioninareawisecollectionlist_7` | same name |
| 8 | Prev Collections Method | picker `settings_fivedayscollectionsmethodet` (7016, "Prev Collections Mode") | 47 num | `fivedayscollectiondetailsmethod_8`: 1 "LAST 5 DAYS COLLECTIONS" (downloads use `PrepareLast5CollectionsforPrev5dates`, `…Prev5weeks` for weekly lines), 2 "LAST COLLECTED 5 DAYS COLLECTION" (`PrepareLast5Collections`) | `fivedayscollectiondetailsmethod` |
| 9 | Show Loan Method in APP Collection List | CheckBox | 41 | `showloanmethodinappcollectionlist_9` | same name |
| 10 | Loan Status Method | picker `settings_loanstatusmethodid` (9027; title also "Prev Collections Mode") | 34 num | `loanstatusmethod_10`: 1 "வசூலான தவணைகள்/ வசூலாக வேண்டிய தவணைகள். குறைவான தொகை வரவு" (collected ÷ due instalments, plus short amount), 2 "வசூலான தவணைகள்/மொத்த தவணைகள்." (collected ÷ total instalments) | `loanstatusmethod` |
| 11 | Enable Full Options in Edit Collection List | CheckBox | 61 | `Enablefulloptioonsincollectionlist_11` | same name |
| 12 | Allow Penalty collection in Collection Screen | CheckBox | 45 | `ispenaltyallowedincollectionscreen_12` | same name |
| 13 | Allow Collection Savings in Collection Screen | CheckBox | 46 | `issavingscollectionallowedincollectionscreen_13` (when off, the drawer also hides "Savings Closures") | same name |
| 14 | NIP Break for Weekly Thittam (REPORTS) | number | 1 num | `nipbreakforweeklythittam_14` | same name |
| 15 | NIP Break for Monthly Thittam | number | 2 num | `nipbreakformonthlythittam_15` | same name |
| 16 | Divisible Value for Thittam | number | 3 num | `divisiblevalueforthittam_16` | same name |
| 17 | No Of Days Allowed in Staff CashBook | number | 53 num | `noofdaysallowedinstaffcashbook_17` | same name |
| 18 | Show Zero Parties subareas in 7 Days Collection Report | CheckBox | 42 | `showzeropartiessubareasin7dayscollectionreport_18` | same name |
| 19 | Show Dashboard Cash Abstract | CheckBox | 7 | `showdashboardcashabstract_19` | same name |
| 20 | Current Financial Year | picker (7022, "Financial Year") | 8 num | `currentfinancialyearid_20`: hard-coded FYId 1 = 2011-2012 … 18 = 2028-2029; "2029-2030" has FYId 29 | `currentfinancialyearid` |
| 21 | Collection Report Printing Method | → `collectionreportprintingmethods_searchlist` (7021) | 11 num | `collectionreportprintingmethod_21` (methods 1–10) | `collectionreportprintingmethod` |
| 22 | Restriction on Maximum Amount Allowed (APP- Loan Screen) | CheckBox; relabels item 23 "First Loan Amount" / "Maximum Loan Amount(Applicable for All Parties)" | 64 | `enableMaximumAmountLoanAllowed_22` | `EnableMaximumAmountLoanAllowed` (not stored by sync) |
| 23 | Maximum Loan Amount Allowed / First Loan Amount | number | 62 num | `MaximumLoanAmountAllowed_23` | `MaximumAmountLoanAllowed` (sync `MaximumLoanAmountAllowed`) |
| 24 | Individual Restriction on No of Loans Allowed | CheckBox; relabels item 25 "No of Loans Allowed, When New Party is created" / "No of Loans Allowed for All Parties" | 65 | `IsRestrictinEnabledonNoofLoansAllowed_IndividualRestriction_24` | same name |
| 25 | No of Loans Allowed | picker "NO OF LOANS" (12002, from local `LoansAllowed`; extra `calledfrom`=12002) | 63 num | `MaximumnoofloansallowedwhilenewpartycreationORnoofloansallowedforallparties_25` | `MaximumNoofLoansallowedforAllPartieswhenIndividualRestrictionisoff` (not stored by sync) |
| 26 | Zero interest Loans Allowed | CheckBox | 51 | `iszerointerestallowed_26` | same name |
| 27 | Enable OnDay Collection | CheckBox | 36 | `enableondaycollection_27` | same name |
| 28 | Enable MobileNo in New Loans | CheckBox | 37 | `enablemobilenoinnewloans_28` | same name |
| 29 | Enable Remarks in New Loans | CheckBox | 38 | `enableremarksinnewloans_29` | same name |
| 30 | Enable User Defined CustomerNo in New Customer Creation Screen (APP- Party Creation Screen) | CheckBox | 40 | `enableuserdefinedcustomernumberinnewcustomercreationscreen_30` | same name |
| 31 | Make all values necessary in New Party Creation Screen | CheckBox | 43 | `makeallvaluesnecessaryinnewpartycreationscreen_31` | same name |
| 32 | SMS on Download Data (APP- SMS and Data Clearing) | CheckBox | 5 | `isSendSMSonDataDownload_32` | `isSendSMSonDataDownload_32` (sync `isSendSMSonDataDownload`) |
| 33 | SMS on upload Data | CheckBox | 6 | `isSendSMSonDataUpload_33` | `isSendSMSonDataUpload_33` (sync `isSendSMSonDataUpload`) |
| 34 | SMS on Cash Receipts | CheckBox | 68 | `sendsmsoncashreceipts_34` | same name (not stored by sync) |
| 35 | SMS on Today Collection Verification | CheckBox | 69 | `sendsmsontodaycollectionverification_35` | same name (not stored by sync) |
| – | Enable Cash/Bank Book Verification (unnumbered) | CheckBox; enables/disables items 36–38 | 101 | `enablecashbookverification` | **not saved** (sync stores `enablecashbookverification`) |
| 36 | SMS on Cash/Bank Book Verification | CheckBox | 70 | `sendsmsoncashandbankbookverification_36` | same name (not read by sync) |
| 37 | Clear Data after C&B Verification | CheckBox | 12 | `deletedataaftercashbookverification_37` | `deletedataaftercashbookverification_37` (sync `deletedataaftercashbookverification`) |
| 38 | Enable to Send SMS on Collection | CheckBox | 71 | `enabletsendSMSonCollection_38` | same name |

**Menu / actions.**
- The picker fields open the lists described in the table.
- Toggling the master C&B checkbox enables items 36–38; unticking it also unticks them.
- Toggling item 22 or 24 changes the label of item 23 or 25.
- Update (`settings_updatebuttonid`) is hidden until the load finishes, and is shown only if the user has full access or the **Update** right on menu `nav_appearancesettings`.

**How it works.**
1. `onCreate` looks up `globalvars.salesdetailsforandroid_datalist` for `nav_appearancesettings` (full access grants every right), then runs `ASYNCSettings`.
2. `ASYNCSettings` reads every `SoftSettings` row into the statics. Besides the screen items it also reads 4, 15, 18, 19, 21, 22, 24–27, 48–50, 101 and 102. `onPostExecute` binds all the controls. Picker text comes from `fivecollectionsmethodname`, `loanstatusmethodname`, `fetchfydesc` and `collectionreportprintingmethodname`.
3. Update copies every control into the statics; the numeric fields go through `Integer.parseInt`. Then `ASYNCSaveSettings`:
   - connects;
   - writes the prefs in the table;
   - runs one `UPDATE` per setting: 39 statements, `bitvalue` for checkboxes and `NumericValue` for numbers and pickers;
   - finishes. Errors are only printed.
4. `onActivityResult` (result 1, extra `spinnerserchobjectname`) sets: 7022 → item 20, 7016 → item 8, 9027 → item 10, 7021 → item 21, 12002 → item 25 (stores the `LoansAllowed.LId`, displays the `LoanIndex`).

**Functions.**
- `onCreate()` — finds the controls, resolves menu rights, wires the listeners, starts the load.
- `lambda$onCreate$1` — master C&B checkbox toggles items 36–38.
- `lambda$onCreate$2` — relabels item 23.
- `lambda$onCreate$3` — relabels item 25.
- `lambda$onCreate$4` — item 8 picker.
- `lambda$onCreate$5` — item 20 picker.
- `lambda$onCreate$6` — item 21 picker.
- `lambda$onCreate$7` — Update: collects the values and starts the save.
- `lambda$onCreate$8` — item 25 picker.
- `lambda$onCreate$9` — item 10 picker.
- `onActivityResult()` — applies the picker results.
- `showAlert()`, `onClick()` — dead. They would show `getsettingdescription(14)`, but `settings_moniteringonloanclosurelabeltv` is never assigned and no listener is registered.
- `ASYNCSettings.doInBackground/onPostExecute` — load and bind.
- `ASYNCSaveSettings.doInBackground/onPostExecute` — prefs + server updates, then finish.

**Rules & edge cases.**
- **Crash:** picking item 21 crashes the app. `collectionreportprintingmethods_searchlist` returns extra `method` (a `collectionreportprintingmethodsobj`), but this screen reads `spinnerserchobjectname`, gets null and throws a NullPointerException (verified in smali).
- **Crash:** a blank or decimal value in item 2, 14, 15, 16, 17 or 23 makes `Integer.parseInt` throw on the UI thread. The fields' input type `numberDecimal` allows a ".".
- **Crash:** selecting "2029-2030" stores FYId 29. On the next visit `fetchfydesc(29)` indexes `list.get(28)` and throws IndexOutOfBoundsException.
- The master "Enable Cash/Bank Book Verification" checkbox is never bound: it always opens unticked and is never saved. Item 36's checkbox is bound to SSId 101 instead of its own SSId 70.
- Saving only runs `UPDATE`s, so a missing `SoftSettings` row is silently ignored. If the connection fails, nothing is saved but the in-memory statics are already changed for this session.
- The pref keys written here often differ from those written by `syncresources` (see the table), so screens reading the sync keys see a change only after the next sync.
- Settings that are **not** on this screen: printer size and type (`App` prefs `MyPref` `PRINTER_SELECTION`/`PRINTER_TYPE`), the app language (`deviceidentity`), and the executive handling method offline/online (`venussoftsettings` VSSId 22, vendor-controlled; `defaulttables.executivehandlingmethods()` lists 1 Offline / 2 Online but is never used).

## Collection report printing method picker — `collectionreportprintingmethods_searchlist`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity); manifest label "SELECT COLLECTION REPORT PRINTING METHOD", theme `AppTheme.NoActionBar`; toolbar title "Select Method" |
| **Layout(s)** | `activity_employeesearch.xml` (shared list frame) + row `accledgers_row.xml`; menu `menuonlywithsearchoption` |
| **Opened from** | `settings_act` item 21 and `collectionlistprints`, both with `startActivityForResult` request code 7021 (`collectionreportprintingmethodrequestfrom_settingsscreen`) |
| **Opens** | Nothing; returns result 1 with extra `method` (`collectionreportprintingmethodsobj`) |
| **Data** | In-memory list `defaulttables.collectionreportprintingmethods()`; no DB access (the unused fields `conn`/`stmt`/`reset` are left over) |

**Purpose.** A searchable list of the 10 collection-list print layouts used by `collectionlistprints` and Bluetooth/PDF printing.

**UI.** Toolbar with Up and search; the FAB `addnewemployeeid` is hidden; a RecyclerView of method names:

| Id | Method name | Columns | Collection entry can be included | Prev-collection breaks |
|---|---|---|---|---|
| 1 | Collection List for Next Seven Days | 1 | no | 7 |
| 2 | Collection List for Single Day | 2 | yes | -1 |
| 3 | For today with Prev two days collection | 2 | yes | -2 |
| 4 | LoanAmount,Balance,Status,PrevDayCollection,Today Collection Entry (Two Columns) | 2 | yes | -1 |
| 5 | Two columns with Loan Amount and Three Days Prev Collection | 2 | yes | -3 |
| 6 | Two columns with Loan Amount,Status and Prev Two Days Collection and Today Collection Entry | 2 | yes | -2 |
| 7 | Monthly Collection Report with LoanDate Prev Balance and next Month Balance (A4 Size) | 1 | yes | 0 |
| 8 | Monthly Collection Report with LoanDate,Status,LoanMethod, Prev Balance and next Month Balance (Legal Size) | 1 | yes | 0 |
| 9 | Status and Balance with Two Days Entry | 2 | no | 2 |
| 10 | Status and Loan Amount with Four Days Entry | 2 | no | 4 |

**Menu / actions.**
- Search filters by substring, case-insensitive.
- Tapping a row → `setResult(1, method)` → finish.
- Back first collapses an open search view. Up (`android.R.id.home`, shown by JADX as the number 16908332) → back.

**How it works.** It builds the list, wires `collectionreportprintingmethods_adapter` and returns the chosen object.

**Functions.**
- `onCreate()` — toolbar, back-press callback, list.
- `onCreateOptionsMenu()` — SearchView that filters the adapter.
- `onOptionsItemSelected()` — Up / back.
- `whiteNotificationBar()` — light status bar.
- `onContactSelected()` — returns the chosen method.

**Rules & edge cases.** The result key `method` is correct for `collectionlistprints` but not for `settings_act`, which crashes (see above).

## Special Access Settings — `specialaccesscontrolact`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), edge-to-edge; title "Special Access Settings" |
| **Layout(s)** | `activity_specialaccesscontrol.xml` |
| **Opened from** | Drawer Settings → "Special Access Control" (`nav_specialaccesssettings`; `visible=false` in the menu XML, shown when the role's `tblSalesDetailsforAndroid.ShowState` for that `mnuname` is true) |
| **Opens** | `spinnersearch_list` "User Roles" (request code 7009) |
| **Data** | SQL Server (selected branch): `SpecialAccessRules`, `SpecialAccessAllowed` · SQLite `UserRoles` (picker) · no prefs |

**Purpose.** For a chosen user role, switch on or off the 20 special rights that individual screens check through `globalvars.specialaccessrules`.

**UI.** "User Level" with a role field `specialuserlevels_id` (tap to pick). Panel `lytspac` with 20 numbered checkboxes; it stays hidden until a role is loaded. Update button (`updatebutton`, "Update"/"சேமி", gone by default). Progress `settings_pbarid`.

| SARId | Label | CheckBox id | Shown value after load |
|---|---|---|---|
| 1 | Allow to change Maximum Amount | `special_allowedchangemaximumamount` | correct |
| 2 | Allow to Change No of Loans | `special_allowedchangenoofloans` | correct |
| 3 | Show collected amount in Collection List | `special_showloancollectedamountincolllist` | correct |
| 4 | Show Loan Collection Status in Collection List | `special_ShowLoanCollectionStatusinCollectionList` | correct |
| 5 | Show Balance in Collection List | `special_showbalanceincolllist` | correct |
| 6 | Allow Unconditional Download | `special_allowunconditionaldownload` | correct |
| 7 | Allow change user role | `special_allowchangeuserroleinmobaccess` | correct |
| 8 | Allow to change weekday in loan screen | `special_allowchangeweekdayinloanscreen` | correct |
| 9 | Allow to see Prev Loans | `special_allowtoseeprevloans` | correct |
| 10 | Allow change loan closing date | `special_allowchangeloanclosingdateinloanclosure` | correct |
| 11 | Allow to change Exisiting Parties Details | `special_allowtochangeexisitingpartiesdetails` | correct |
| 12 | Allow to Add New Parties | `special_allowtoaddNewParties` | correct |
| 13 | Allow to Add New Loans | `special_allowtoaddNewLoans` | correct |
| 14 | Allow to Change Values in Loan Screen | `special_allowtochangenewloandetails` | correct |
| 15 | Allow to Check Complete Collection Details | `special_allowtoCheckCompleteCollectionDetails` | **wrong**: overwritten six times, ends with rule 20's value |
| 16 | Allow to Clear Cashbook Verification | `special_allowtoclearcashbookverification` | correct |
| 17 | Allow to View Compained Cash Book | `special_allowtoviewcompainedcashbook` | **never loaded** (unticked) |
| 18 | Allow to Create New Loans against onday closed Loans_18 | `special_AllowtoCreateNewLoansagainstondayclosedLoans_18` | **never loaded** |
| 19 | Allow Unconditional access in Acc Trans | `special_AllowUnconditionalaccessinAccountTransactions_19` | **never loaded** |
| 20 | Allow to Rearrange Areas and Parties | `special_allowtorearranfeareasandparties` | **never loaded** |

**Menu / actions.**
- Role field → picker list from `defaulttables.fetchuserrolesforspecialaccessrules`: roles with `isFullAccessAllowed=0` and `DisplayIndex ≥` the current user's role.
- Update → `ASYNCSaveSettings`.

**How it works.**
1. On a role result (7009): `curmaobj.userroleid` = the picked role → `ASYNCSettings`. It runs `select a.SARId,SAName,bitvalue from SpecialAccessRules a left join SpecialAccessAllowed b on a.SARId=b.SARId and b.UserLevelId=<role> where a.isAllowed=1 ORDER BY SARId` and maps the rows into `specialaccessrulesobj spaOBJ`.
2. `onPostExecute` shows the panel and binds the checkboxes (with the defects in the table). Update becomes visible with full access or the Update right on `nav_specialaccesssettings`.
3. Update copies every checkbox into `spaOBJ`; rule 20 is first set from the clear-cash-book box, then from its own box. For SARId 1…20 it runs `Update SpecialAccessAllowed set bitvalue=? where SARId=? and UserLevelId=?`, and when 0 rows change, `Insert into SpecialAccessAllowed (bitvalue,SARId,UserLevelId) values(?,?,?)`. Then it finishes.
4. Nothing is written locally. The rights reach devices of that role at their next Sync Resources.

**Functions.**
- `onCreate()` — finds the 20 checkboxes, resolves menu rights, wires the role field and Update.
- `lambda$onCreate$1` — role picker.
- `lambda$onCreate$2` — Update.
- `onActivityResult()` — sets the role and reloads.
- `ASYNCSettings.doInBackground/onPostExecute` — load and bind.
- `ASYNCSaveSettings.doInBackground/onPostExecute` — update-or-insert all 20 rules, then finish.

**Rules & edge cases.**
- **Data-loss bug:** because of the load defects, a save made without re-ticking resets rules 17–20 to false and writes rule 20's value into rule 15.
- Rules with `isAllowed=0` are still displayed and saved.
- An admin who is not full-access can edit their **own** role (the picker uses `>=`).
- Errors are swallowed and the screen closes anyway.

## Menu Access Control (User Privileges) — `userroleprevilages_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, implements `userroleprevilages_adapter.ContactsAdapterListener`), theme `AppTheme.NoActionBar`; toolbar title "User Previlages" |
| **Layout(s)** | `activity_userprevillages.xml` + row `userroleprevilages_row.xml` |
| **Opened from** | Drawer Settings → "Menu Access Control" (`nav_Andriodmenusettings`; `visible=false` in the XML, shown per role rights) |
| **Opens** | `spinnersearch_list` "User Role" (request code 40) |
| **Data** | SQL Server (selected branch): `AndroidMenus` (mnuid, mnulevel, mnuName, mnudisplayname, mnustyle, isallowed, isonlyformasterdb), `tblSalesDetailsforAndroid` (UserLevelId, mnuId, ShowState, ViewState, UpdateState, AddState, DeleteState) · SQLite `UserRoles` (picker) |

**Purpose.** Defines what each role sees and may do in the Android app.
- **Show**: the drawer item is visible. `navigationdrawactivity` maps every `mnuname` to the drawer item id with the same name and calls `setVisible(ShowState)`.
- **View / Add / Up(date) / Del(ete)**: screen-level rights. Screens read them through `curitemaccesslimits`, e.g. the Settings Update button and the Access Control Save/Delete buttons.

**UI.**
- "User Level : <role>" (`userlevels_id`, tap to pick).
- A RecyclerView with one row per allowed menu. Level-1 items are indented and black; others are blue.
- The checkboxes shown depend on `mnustyle`:
  - 0, or a level-0 group header → Show only;
  - 1 → Show + Up;
  - 3 → Show + Up + Del;
  - anything else → Show + Add + View + Up + Del.
- SAVE (`savebuttonid`) is visible only with full access or the Update right on `nav_Andriodmenusettings`.

**Menu / actions.** The role picker loads the grid. Checkbox changes update the in-memory list (`on…statechanged`). SAVE stores the grid. Up → back.

**How it works.**
1. `asyncuserroles`: `select a.mnuid,a.mnulevel,a.mnuName,b.ShowState as 'Select',a.mnudisplayname,b.ViewState as 'View',b.UpdateState as 'Update',b.AddState as 'Add',b.DeleteState as 'Delete',mnustyle from AndroidMenus a left join tblSalesDetailsforAndroid b on a.mnuId=b.mnuid and b.UserLevelId=<role> where isallowed=1 and (isonlyformasterdb=0 or isonlyformasterdb='<ismasterdb>') ORDER BY MNUID` → `userroleprevilages_data` list.
2. `asyncsaveprevillaged`: a single batch, `delete from tblSalesDetailsforAndroid where UserLevelId=<role>`, followed by one `;insert into tblSalesDetailsforAndroid (UserLevelId,mnuId,ShowState,ViewState,UpdateState,AddState,DeleteState) values(<role>,<mnuid>,'true'/'false',…)` per row, run with `executeUpdate`. Then it finishes.
3. Devices of that role get the change at their next Sync Resources.

**Functions.**
- `onCreate()` — toolbar, list, rights, buttons.
- `lambda$onCreate$1` — save.
- `lambda$onCreate$2` — role picker.
- `onActivityResult()` — role chosen → load.
- `onshowstatechanged/onaddstatechanged/onupdatestatechanged/ondeletestatechanged/onviewstatechanged()` — update the list.
- `onOptionsItemSelected()` — Up / back.
- `whiteNotificationBar()` — light status bar.
- `asyncuserroles` — load.
- `asyncsaveprevillaged` — replace the role's rights.

**Rules & edge cases.**
- The role list includes the current user's own role (`DisplayIndex >=`), so a non-full-access admin can raise their own menu rights.
- Tapping SAVE before choosing a role throws an NPE, which is caught; the screen then just closes.
- The list is cleared and refilled on the background thread while it is attached to the RecyclerView (race).
- The progress dialog shows only "downloading, please wait ..." / "Connecting". The error-text update has a single argument and is lost when `values[1]` is read, so errors are never displayed.

## Mobile device access setup — `mobiledeviceaccescontrol_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, implements `selectablelist_adapter.ContactsAdapterListener`), theme `AppTheme.NoActionBar`, `windowSoftInputMode=stateAlwaysHidden` |
| **Layout(s)** | `activity_mobiledeviceaccesscontrol.xml` + row `clubsandgroupsmarked_row.xml`; menu `simplemenu` |
| **Opened from** | `employeesaccessearchlist` (drawer Admin → "Access Control", `nav_accesscontrol`). FAB add → extra `calltype`=7011 (`accessobj_addrequest`) with an empty `mobiledeviceaccescontrolobj`, request code 7011. Tapping a device row → `calltype`=7012 (`accessobj_editrequest`), request code 7012, allowed only with the View right and (full access, or own role `DisplayIndex` < the row's role); otherwise toast "ACCESS DENIED". |
| **Opens** | `spinnersearch_list` for user roles (7009) and employees (7010, also titled "User Roles") |
| **Data** | SQL Server (selected branch): proc `dbo.InsertMobileDevlicesAllowed`; tables `MobileDevlicesAllowed` (MDAId, IMEI, LId, isActive, userroleid), `FinanceAccessAllowed` (MDAId, IMEI, FinId), `NewIMEIRequests` (NIRId, IMEI, RequestRaisedAt) · SQLite `FinanceDetails`, `FinanceAccessAllowed`, `MobileDevlicesAllowed`, `EMPLedgers`, `userroles` · model `mobiledeviceaccescontrolobj` (MDAId, IMEI, LId, LedgerName, isActive, userroleid) |

**Purpose.** Approves a phone for an employee on the current branch DB: links the device id to an employee ledger and a user role, marks it active, and chooses which lines (finances) it may work on.

**UI.**
- Title: "Access Setup :<employee>", "Access Setup : New", or "<BranchName>- Mobile Access :" (master DB with a branch selected).
- Fields: "Android Device Id" (`mdac_imeiid`) with a "GI" button (get the latest request); "Employee" (`mdac_employeeetid`, picker); "User Role" (`mdac_userroleetid`, picker, enabled only if special rule 7 "Allow change user role" is on); "Active" checkbox.
- "Allowed Lines" (Tamil "அனுமதிக்கப்பட்ட பைனான்ஸ்கள்") checklist.
- SAVE and DELETE buttons, and a "Please wait..." overlay.

**Menu / actions.**
- **GI**:
  1. Cleans up orphans on the server: `delete from FinanceAccessAllowed where IMEI not in (select IMEI from MobileDevlicesAllowed)`.
  2. Fetches `select top 1 IMEI,RequestRaisedAt from NewIMEIRequests where IMEI not in (select IMEI from FinanceAccessAllowed) order by NIRId desc`.
  3. Fills the device id and shows "Recent Request.... ": "Last Request found on \n <date>\n <time>", or "No Request found".
- **Employee picker**: local `EMPLedgers` with `desigstatusid=1`.
- **Role picker**: `fetchuserroles`; full access sees every role, others only roles with a higher `DisplayIndex` than their own.
- **Line checkbox**: updates `markeditems_data.ismarked`.
- **Visibility**: in edit mode, SAVE needs the Update right and DELETE needs the Delete right on `nav_accesscontrol`. In add mode only SAVE is shown.
- The toolbar search icon does nothing; Up → back.

**How it works.**
1. `fetchdata()` builds the checklist: `select a.FinId, case when b.FinId is null then 0 else 1 end as isSelected, FinMailingName from FinanceDetails a left join FinanceAccessAllowed b on (a.FinId=b.FinId and b.IMEI='<IMEI>')` on local SQLite, i.e. the lines this admin device knows.
2. **Add (7011)**:
   - `{call dbo.InsertMobileDevlicesAllowed(?,?,?,?,?)}` with `MDAId` out (type 8 = `Types.DOUBLE`), `IMEI`, `LId`, `isActive`, `userroleid`.
   - Local `delete from FinanceAccessAllowed where IMEI=…`, then per ticked line a local `insert into FinanceAccessAllowed(MDAId,IMEI,FinId)`. The same statements, joined with `;`, are run as one server batch.
   - Local insert into `MobileDevlicesAllowed`.
3. **Edit (7012)**:
   - Server `update MobileDevlicesAllowed set IMEI=?,LId=?,isActive=?,userroleid=? where MDAId=?`.
   - Local and server delete, then re-insert of `FinanceAccessAllowed(IMEI,FinId)` for the ticked lines.
   - Local update of `MobileDevlicesAllowed`.
4. **Delete (7013, no confirmation)**: server `delete from MobileDevlicesAllowed where IMEI=?` and `delete from FinanceAccessAllowed where IMEI=?`; local delete from `MobileDevlicesAllowed`.
5. `setResult(actiontype, curmaobj)` → finish. The caller list inserts (7011), replaces (7012) or removes (7013) the row.

**Functions.**
- `onCreate()` — mode setup, rights, list, listeners.
- `lambda$onCreate$1` — GI.
- `lambda$onCreate$2` — role picker.
- `lambda$onCreate$3` — employee picker.
- `lambda$onCreate$4` — Save.
- `lambda$onCreate$5` — Delete.
- `fetchdata()` — line checklist.
- `onActivityResult()` — role or employee chosen.
- `onItemselected()` — tick or untick a line.
- `onCreateOptionsMenu()`, `onOptionsItemSelected()` — menu and Up.
- `whiteNotificationBar()` — light status bar.
- `ASYNCgetrecentid` — latest request plus orphan cleanup.
- `ASYNCSSaveNewMarks` — add, edit or delete.

**Rules & edge cases.**
- **Probable bug:** every local `FinanceAccessAllowed` definition found in the app is `(IMEI String, FinId int)`, with no `MDAId` column. The add-mode local insert therefore fails at the first ticked line, *before* the server batch runs. The device row is created but gets no line access until it is edited and saved again.
- Errors are swallowed and a result is returned even on failure.
- Delete has no confirmation.
- SQL is built by concatenating the typed device id.
- If the device id is edited, the old id's `FinanceAccessAllowed` rows stay behind until the next GI cleanup.
- No client code ever inserts into `NewIMEIRequests`; the rows are created outside the app.

## Branch-level device access — `branchmobiledeviceaccescontrol_act`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity, implements `selectablelist_adapter.ContactsAdapterListener`), theme `AppTheme.NoActionBar`, `stateAlwaysHidden`; title "Branch Access " |
| **Layout(s)** | `activity_branchmobiledeviceaccesscontrol.xml` + row `clubsandgroupsmarked_row.xml` |
| **Opened from** | `branchemployeesaccessearchlist` (drawer Admin → "Branch Access Control", `nav_branchaccesscontrol`): add → `calltype` 7011 (request code 7011); row → `calltype` 7012 + extra `branchaccess_obj` (7012) |
| **Opens** | `spinnersearch_list` "Employees" (7010) |
| **Data** | **Master DB** (`MasterDBconnString`): proc `dbo.InsertBranchMobileDevlicesAllowed`; tables `BranchMobileDevlicesAllowed` (MDAId, IMEI, LId, isActive, isbranchusercontrolallowed), `BranchAccessAllowed` (IMEI, BId, MDAId), `BranchNewIMEIRequests` · SQLite `BranchConnStrs`, `BranchAccessAllowed`, `BranchMobileDevlicesAllowed`, `BranchEMPLedgers` · model `branchaccess_obj` (IMEI, LId, MDAId, isActive, isbranchusercontrolallowed, ledgername) |

**Purpose.** In multi-branch (master DB) installations, decides which branches a phone may switch to, and whether that user may manage branch users ("Is Branch User Control Allowed").

**UI.** "Android Device ID" (`mdac_imeiid`, hint "android device id") with "GI"; "Employee" picker; checkboxes "Is Branch User Control Allowed" and "Active"; "Branches Allowed" (Tamil "அனுமதிக்கப்பட்ட கிளைகள்") checklist; SAVE and DELETE (in edit mode they need the Update and Delete rights on `nav_branchaccesscontrol`); ProgressBar.

**Menu / actions.**
- **GI**: master `select top 1 IMEI,RequestRaisedAt from BranchNewIMEIRequests where IMEI not in (select IMEI from BranchMobileDevlicesAllowed) order by NIRId desc`. Fills the device id and always shows "Last Request found on …" ("null" when there is none).
- **Employee picker**: local `BranchEMPLedgers`.
- SAVE and DELETE are described below.

**How it works.**
1. `fetchbranchaccessallowed()` (local): `select a.BId, case when b.BId is null then 0 else 1 end as isSelected, BranchName from BranchConnStrs a left join BranchAccessAllowed b on (a.BId=b.BId and b.IMEI='<IMEI>')`.
2. **Add (7011)**:
   - `{call dbo.InsertBranchMobileDevlicesAllowed(?,?,?,?,?)}` (`MDAId` out as `Types.DOUBLE`, IMEI, LId, isActive, isbranchusercontrolallowed).
   - Local insert into `BranchMobileDevlicesAllowed`; its LId comes from `globalvars.selectedemployeelegerid`, not from the picked employee.
   - Per ticked branch: `Insert into BranchAccessAllowed (IMEI,BId,MDAId) values(?,?,?)`.
3. **Edit (7012)**: `update BranchMobileDevlicesAllowed set IMEI=?,LId=?,isActive=?,isbranchusercontrolallowed=? where MDAId=?`, then `delete from BranchAccessAllowed where MDAId=?`, then the inserts again.
4. **Delete (7013, no confirmation)**: `delete from BranchAccessAllowed where MDAId=?` and `delete from BranchMobileDevlicesAllowed where MDAId=?`.
5. Then `onBackPressed()`; there is no `setResult`.

**Functions.**
- `onCreate()` — mode setup, rights, list, listeners.
- `lambda$onCreate$1` — GI.
- `lambda$onCreate$2` — employee picker.
- `lambda$onCreate$3` — Save.
- `lambda$onCreate$4` — Delete.
- `fetchbranchaccessallowed()` — branch checklist.
- `onActivityResult()` — employee chosen.
- `onItemselected()` — tick or untick a branch.
- `whiteNotificationBar()` — light status bar.
- `ASYNCgetrecentid` — latest branch request.
- `async_accessrights` — add, edit or delete on the master DB.

**Rules & edge cases.**
- **Probable bug:** every `BranchAccessAllowed` INSERT/DELETE and the final DELETE run through `executeQuery()`. jTDS throws "The executeQuery method must return a result set" *after* running the statement, which aborts the rest of the sequence:
  - add stores only the first ticked branch;
  - edit deletes the branch rows and re-inserts at most one;
  - delete leaves the `BranchMobileDevlicesAllowed` row in place.
- Errors are swallowed.
- Local `BranchAccessAllowed` is never refreshed here.

## Vendor website — `financesoftwaredontinwebview`
| | |
|---|---|
| **Type** | Activity (AppCompatActivity), theme `AppTheme.NoActionBar` |
| **Layout(s)** | `activity_webview.xml` |
| **Opened from** | Drawer About → "Venus Solutions" (`nav_venuswebsite`) |
| **Opens** | Nothing (all links stay inside the WebView) |
| **Data** | `http://financesoftware.in/` (cleartext; allowed by `usesCleartextTraffic=true`) |

**Purpose.** Shows the vendor's product website inside the app.

**UI.** A full-screen WebView `webView`; JavaScript is enabled, images load automatically, and scrollbar style 0 (`SCROLLBARS_INSIDE_OVERLAY`) is used.

**Menu / actions.** None. Back closes the screen; there is no in-page back navigation.

**How it works.** `onCreate` configures the WebView and loads the URL. `MyBrowser.shouldOverrideUrlLoading` loads every clicked URL in the same WebView.

**Functions.**
- `onCreate()` — WebView setup and URL.
- `MyBrowser.shouldOverrideUrlLoading()` — keeps navigation inside the app.

**Rules & edge cases.** JavaScript runs on a page fetched over plain HTTP.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `userroleprevilages_adapter` | RecyclerView adapter (Filterable) | `userroleprevilages_act` | **Row:** `userroleprevilages_row` — menu display name plus Show/Add/View/Up/Del checkboxes bound to `userroleprevilages_data`. Level-1 items are indented with a 15 px margin and black text; others are blue. **Visible boxes:** by `mnustyle` — 0 or level 0 → Show only; 1 → Show+Up; 3 → Show+Up+Del; other → all five. **Callbacks:** each change → `ContactsAdapterListener.onshowstatechanged / onaddstatechanged / onupdatestatechanged / ondeletestatechanged / onviewstatechanged(item, pos, checked)`. **Filter:** broken (any non-empty query gives an empty list); unused. |
| `collectionreportprintingmethods_adapter` | RecyclerView adapter (Filterable) | `collectionreportprintingmethods_searchlist` | **Row:** `accledgers_row` — method name in `accledgers_ledgername` at 15 sp; `accledgers_groupname` hidden. **Callback:** row tap → `onContactSelected(collectionreportprintingmethodsobj, pos)`. **Filter:** case-insensitive substring of the name. The list items (`collectionreportprintingmethodsobj`) hold `methodid`, `methodname`, `columns`, `iscollectioncanbeincluded`, `noofbreaksforprevcollection`. |
| `selectablelist_adapter` | RecyclerView adapter (Filterable) | `mobiledeviceaccescontrol_act` (lines), `branchmobiledeviceaccescontrol_act` (branches) | **Row:** `clubsandgroupsmarked_row` — one CheckBox whose text is `markeditems_data.CGName`, ticked when `ismarked`. **Callback:** checkbox click → `onItemselected(item, pos, isChecked)`. **Filter:** upper-cases the query but compares it with lower-cased names, so it never matches letters; unused. |
| `specialaccessrulesobj` | Serializable model | `globalvars.specialaccessrules` (filled by `syncresources`, the drawer's `syncsettings()` and `downloaddata`), `specialaccesscontrolact` | 20 booleans, one per SARId: 1 change max amount, 2 change no. of loans, 3 show collected amount, 4 show loan status, 5 show balance, 6 unconditional download, 7 change user roles, 8 change weekday in loan screen, 9 see previous loans, 10 change loan closing date, 11 change existing party details, 12 add new parties, 13 add new loans, 14 change values in loan screen, 15 check complete collection details, 16 clear cash-book verification, 17 view combined cash book, 18 new loans against on-day-closed loans, 19 unconditional access in account transactions, 20 rearrange collection areas and parties. `is…`/`get…` plus setters; each maps to its own field. |
| `userroleprevilages_data` | Serializable model | `userroleprevilages_act` and its adapter | `mnuid`, `mnulevel`, `mnuName` (= drawer item id name), `mnudisplayname`, `mnustyle`, `ShowState`, `ViewState`, `UpDateState`, `AddState`, `DeleteState`; `userlevelid` and `activitytypeid` are unused |
| `appinsobj` | Serializable model | `playstoredemouserregistrationact` → `otpverificationact` (extra `curinsobj`) | Demo registration: `appinsid` (server `Appinsid`, shown as "Customer ID"), `contactname`, `mobileno`, `email`, `financename`, `city` |
| `userroles_data` | Serializable model | `userroleprevilages_act` | The selected role: `ULId`, `ULName`, `isFullAccessAllowed`; only `ULId` is ever set |
| `markeditems_data` | Plain model | `selectablelist_adapter` and both device-access screens | Checklist item: `cgid` (FinId or BId), `CGName` (line or branch name), `ismarked` |

## Layouts in this module
| Layout | Used by | Contents |
|---|---|---|
| `accledgers_row.xml` | `collectionreportprintingmethods_adapter` (also `accledgerssearch_adapter`) | TextViews `accledgers_ledgername` ("Acc Ledger Name") and `accledgers_groupname` ("Group Name"; hidden in the method picker) |
| `activity_branchmobiledeviceaccesscontrol.xml` | `branchmobiledeviceaccescontrol_act` | CoordinatorLayout + Toolbar. "Android Device ID" EditText `mdac_imeiid` with a "GI" button; "Employee" `mdac_employeeetid`; CheckBoxes "Is Branch User Control Allowed" `mdac_isbranchusercontrolallowedid` and "Active" `mdac_activeid`; "Branches Allowed"/"அனுமதிக்கப்பட்ட கிளைகள்" RecyclerView `recycler_view`; SAVE `btnsavelinemobileaccess`, DELETE `btndeletelinemobileaccess`; ProgressBar `progressBar` |
| `activity_collectdbinfofromweb.xml` | `collectdbinfofromweb` | Logo, "Venus Solutions", "Contact Nos 📱" with the vendor numbers, "Enter Your Registration Key", "DEMO Registration Key" / "M M M M M M", six 1-character capital boxes `et_key_1..6`, "📨 Submit" `btn_submit_key`, ProgressBar `collectdbpbarid`, "▶️ Watch Video" `btn_watch_video` |
| `activity_deviceidentity.xml` | `deviceidentity` | Toolbar, launcher icon, OS name `onnametvid`, device id `deviceidtvid`, "VENUS SOLUTIONS" and phone numbers, "Choose Language" `applanguage_id`, "Raise Request for Branch Level Access" `btnraisebranchrequest`, licence `deviceinfo_licenseholder`, app version `deviceinfo_appversion`, user reference no. `deviceinfo_dbidentification`, ProgressBar |
| `activity_employeesearch.xml` | `collectionreportprintingmethods_searchlist` (shared by 16 other list screens) | CoordinatorLayout, Toolbar, included `customersearchlist` (RecyclerView `recycler_view`), FAB `addnewemployeeid` (hidden here), progress overlay `progress_Overlay` with `capbarid` |
| `activity_mobiledeviceaccesscontrol.xml` | `mobiledeviceaccescontrol_act` | Toolbar. "Android Device Id" `mdac_imeiid` with "GI"; "Employee" `mdac_employeeetid`; "User Role" `mdac_userroleetid`; "Active" `mdac_activeid`; "Allowed Lines"/"அனுமதிக்கப்பட்ட பைனான்ஸ்கள்" RecyclerView; SAVE / DELETE; overlay `progress_overlay` "Please wait..." |
| `activity_otpverification.xml` | `otpverificationact` | "Customer Id" `tv_otp_appinsid`, "Enter the OTP sent to your", `tv_otp_mobile`, `tv_otp_email`, four numeric boxes `et_otp_1..4`, "Submit" `btn_submit_otp`, "Resend OTP" `tv_resend_otp`, ProgressBar `loading` |
| `activity_playstoredemouserregistration.xml` | `playstoredemouserregistrationact` | Logo `reglogo`, "VENUS SOLUTIONS" / "WELCOMES YOU" / "Kindly Introduce Yourself"; inputs Name `login_contactperson`, Mobile No `login_contactno`, Finance Name `login_financename`, City `login_cityname`, EMail `login_emailid`; "Register and SEND OTP" `login_sendotp`; ProgressBar `loading`; hidden "Connectig....." `loadingmsgid` |
| `activity_settings_appearance.xml` | `settings_act` | Six titled groups: "APP-Download and Upload Data", "APP-Collection List", "REPORTS", "APP- Loan Screen", "APP- Party Creation Screen", "APP- SMS and Data Clearing". 39 numbered rows (1–38 plus 6a), each a CheckBox or EditText, plus the unnumbered "Enable Cash/Bank Book Verification" CheckBox. Labels `lblmaxloanamountid` and `lblnoofloansid` change at runtime. Update button `settings_updatebuttonid` ("Update"/"சேமி", gone until allowed); ProgressBar `settings_pbarid` |
| `activity_specialaccesscontrol.xml` | `specialaccesscontrolact` | "User Level" with role field `specialuserlevels_id`; panel `lytspac` with 20 numbered rule CheckBoxes (`special_*`); Update `updatebutton` ("Update"/"சேமி", gone); ProgressBar `settings_pbarid` |
| `activity_splash.xml` | `splash` | "Venus Solutions", "WELCOMES YOU", logo `imageView2`; a hidden "Kindly Select Your APP Type" panel with 3 unused buttons ("Login with Registration Key", "Local Server Registration", "Demo Database"); hidden ProgressBar; status text `pstatusid` |
| `activity_sqlserverconnectionproperties.xml` | `sqlserverconnectionproperties` | "MS SQL Server IP Address" `tvservernameid` (default 192.168.0.100), "SQL Instance" `tvinstancenameid` (SQLExpress), "User Name" `etusernameid` (sa), "Password" `etpasswordid` (default literal [REDACTED]), "Database Name" `etdbnameid` (mfdbonpremises); "CONNECTION TEST" `btntestconnectionid`, "SAVE" `btnnsaveproid`; ProgressBar |
| `activity_syncresources.xml` | `syncresources` | ConstraintLayout `main` holding only the app logo; progress is a ProgressDialog |
| `activity_userprevillages.xml` | `userroleprevilages_act` | CoordinatorLayout + Toolbar; "User Level" ":" role field `userlevels_id`; RecyclerView `recycler_view`; SAVE `savebuttonid` (gone until allowed) |
| `activity_webview.xml` | `financesoftwaredontinwebview` | A single WebView `webView` |
| `clubsandgroupsmarked_row.xml` | `selectablelist_adapter` | CheckBox `cgm_ismarkedid` (its text is the item name); hidden TextView `cgm_clubandgroupnameid` |
| `userroleprevilages_row.xml` | `userroleprevilages_adapter` | TextView `userroleprevilagesrow_activitynametv` (menu name) and CheckBoxes Show `…showstatechk`, Add `…addtatechk`, View `…viewstatechk`, Up `…updatestatechk`, Del `…deletestatechk` |

---
[← brain.md](../../brain.md) · Module 8.1 of 20 · Next: [8.2 Navigation hub, routers and line/branch selection](02-navigation-hub-routers-line-branch-selection.md)
