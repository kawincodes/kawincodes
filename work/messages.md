# User-facing messages

[← work.md](../work.md)

Every literal toast, snackbar, dialog title/message/button, field error and hint set in code: **782**, grouped by module and class. `${…}` marks values computed at runtime, and `@string/…` marks localized resources (see brain Appendix D for the English and Tamil text).

## 8.1 App start, registration, device identity, settings and access control

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `branchmobiledeviceaccescontrol_act` | Dialog/screen title | Branch Access  | `onCreate:88` |
| `branchmobiledeviceaccescontrol_act` | Dialog button | OK | `onPostExecute:274` |
| `branchmobiledeviceaccescontrol_act` | Dialog/screen title | Recent Request....  | `onPostExecute:281` |
| `collectdbinfofromweb` | Toast | Verified | `onPostExecute:179` |
| `collectdbinfofromweb` | Toast | Verified | `onPostExecute:215` |
| `collectdbinfofromweb` | Dialog button | OK | `onPostExecute:238` |
| `collectdbinfofromweb` | Dialog/screen title | Connection Error | `onPostExecute:238` |
| `collectionreportprintingmethods_searchlist` | Dialog/screen title | Select Method | `onCreate:64` |
| `deviceidentity` | Dialog/screen title | Device and APP Info | `onCreate:105` |
| `deviceidentity` | Toast | You have access to multiple branches | `onPostExecute:275` |
| `deviceidentity` | Toast | ACCESS NOT YET ALLOWED | `onPostExecute:277` |
| `deviceidentity` | Dialog button | OK | `showRestartConfirmationDialog:294` |
| `deviceidentity` | Dialog message | The app needs to restart to apply the new language. Kindly close and reopen this app | `showRestartConfirmationDialog:294` |
| `deviceidentity` | Dialog/screen title | Restart Required | `showRestartConfirmationDialog:294` |
| `mobiledeviceaccescontrol_act` | Dialog/screen title | ${globalvars.BranchName}- Mobile Access : | `onCreate:104` |
| `mobiledeviceaccescontrol_act` | Dialog/screen title | Access Setup :${curmaobj.getLedgerName()} | `onCreate:106` |
| `mobiledeviceaccescontrol_act` | Dialog/screen title | Access Setup : New | `onCreate:123` |
| `mobiledeviceaccescontrol_act` | Dialog button | OK | `onPostExecute:315` |
| `mobiledeviceaccescontrol_act` | Dialog/screen title | Recent Request....  | `onPostExecute:322` |
| `mobiledeviceaccescontrol_act` | Dialog button | OK | `onPostExecute:326` |
| `mobiledeviceaccescontrol_act` | Dialog message | No Request found | `onPostExecute:326` |
| `mobiledeviceaccescontrol_act` | Dialog/screen title | Recent Request....  | `onPostExecute:333` |
| `mobiledeviceaccescontrol_act` | Dialog message | Working, Please wait ... | `onPreExecute:346` |
| `mobiledeviceaccescontrol_act` | Dialog message | Working, Please wait ... | `onPreExecute:489` |
| `otpverificationact` | Dialog button | OK | `lambda$onCreate$2:163` |
| `otpverificationact` | Dialog message | INVALID OTP | `lambda$onCreate$2:163` |
| `otpverificationact` | Dialog/screen title | OTP Error.......... | `lambda$onCreate$2:170` |
| `otpverificationact` | Toast | Please enter a valid 4-digit OTP | `lambda$onCreate$2:174` |
| `otpverificationact` | Dialog/screen title | Info... | `onPostExecute:262` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:264` |
| `otpverificationact` | Dialog message | Unable to Register your device, Please try again later....  | `onPostExecute:264` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:918` |
| `otpverificationact` | Dialog/screen title | Error Info | `onPostExecute:918` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:1267` |
| `otpverificationact` | Dialog/screen title | Error Info... | `onPostExecute:1267` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:1279` |
| `otpverificationact` | Dialog/screen title | Error Info... | `onPostExecute:1279` |
| `otpverificationact` | Dialog message | ACCESS Denied | `onPostExecute:1279` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:1292` |
| `otpverificationact` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onPostExecute:1292` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:1304` |
| `otpverificationact` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onPostExecute:1304` |
| `otpverificationact` | Dialog message |  Subscription expired   Kindly pay the renewal charges | `onPostExecute:1304` |
| `otpverificationact` | Toast | Download Completed | `onPostExecute:1328` |
| `otpverificationact` | Toast | Access Denied | `onPostExecute:1331` |
| `otpverificationact` | Toast | Access Denied | `lambda$onPostExecute$2:1355` |
| `otpverificationact` | Toast | Download Completed | `lambda$onPostExecute$2:1358` |
| `otpverificationact` | Dialog message | downloading, please wait ... | `onPreExecute:1391` |
| `otpverificationact` | Dialog/screen title | Info... | `onPostExecute:1487` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:1489` |
| `otpverificationact` | Dialog message | An OTP has been sent to your email and mobile no. Please check your inbox and spam/junk folder.  | `onPostExecute:1489` |
| `otpverificationact` | Dialog/screen title | Info... | `onPostExecute:1500` |
| `otpverificationact` | Dialog button | OK | `onPostExecute:1502` |
| `otpverificationact` | Dialog message | Unable to send OTP to your Email, Please try again later....  | `onPostExecute:1502` |
| `playstoredemouserregistrationact` | Dialog/screen title | Warning... | `showalert:71` |
| `playstoredemouserregistrationact` | Dialog button | OK | `showalert:72` |
| `playstoredemouserregistrationact` | Dialog message | Kindly ensure your Email and Mobile No are correct. | `showalert:72` |
| `playstoredemouserregistrationact` | Dialog/screen title | Info... | `onPostExecute:175` |
| `playstoredemouserregistrationact` | Dialog button | OK | `onPostExecute:177` |
| `playstoredemouserregistrationact` | Dialog message | An OTP has been sent to your email and mobile no. Please check your inbox and spam/junk folder.  | `onPostExecute:177` |
| `playstoredemouserregistrationact` | Dialog/screen title | Info... | `onPostExecute:188` |
| `playstoredemouserregistrationact` | Dialog button | OK | `onPostExecute:190` |
| `playstoredemouserregistrationact` | Dialog message | Unable to send OTP to your Email, Please try again later....  | `onPostExecute:190` |
| `settings_act` | Dialog/screen title | Settings | `onCreate:97` |
| `settings_act` | Dialog button | OK | `showAlert:352` |
| `settings_act` | Dialog/screen title | Description | `showAlert:352` |
| `specialaccesscontrolact` | Dialog/screen title | Special Access Settings | `onCreate:76` |
| `sqlserverconnectionproperties` | Dialog/screen title | Server Connection Properties | `onCreate:67` |
| `sqlserverconnectionproperties` | Dialog/screen title | Test is not completed... | `lambda$onCreate$2:92` |
| `sqlserverconnectionproperties` | Dialog button | OK | `lambda$onCreate$2:94` |
| `sqlserverconnectionproperties` | Toast | Settings Updated | `lambda$onCreate$2:118` |
| `sqlserverconnectionproperties` | Dialog/screen title | Unable to Connect... | `onPostExecute:171` |
| `sqlserverconnectionproperties` | Dialog button | OK | `onPostExecute:173` |
| `sqlserverconnectionproperties` | Dialog/screen title | Connect... | `onPostExecute:185` |
| `sqlserverconnectionproperties` | Dialog button | OK | `onPostExecute:187` |
| `syncresources` | Dialog/screen title | SYNC RESOURCES | `onCreate:50` |
| `syncresources` | Dialog button | OK | `onPostExecute:711` |
| `syncresources` | Dialog/screen title | Error Info | `onPostExecute:711` |
| `syncresources` | Dialog/screen title | Info | `onPostExecute:723` |
| `syncresources` | Dialog message | Permission Denied.. | `onPostExecute:724` |
| `syncresources` | Dialog button | OK | `onPostExecute:725` |
| `syncresources` | Dialog/screen title | Info | `onPostExecute:755` |
| `syncresources` | Dialog message | Sync Completed.. | `onPostExecute:758` |
| `syncresources` | Dialog button | OK | `onPostExecute:759` |
| `syncresources` | Dialog message | Sync Begins ... | `onPreExecute:795` |
| `userroleprevilages_act` | Dialog/screen title | User Previlages | `onCreate:66` |
| `userroleprevilages_act` | Dialog message | downloading, please wait ... | `onPreExecute:240` |
| `userroleprevilages_act` | Dialog message | downloading, please wait ... | `onPreExecute:294` |

## 8.2 Navigation hub, routers and line/branch selection

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `navigationdrawactivity` | Toast | Please click BACK again to exit | `handleOnBackPressed:175` |
| `navigationdrawactivity` | Dialog/screen title | Info... | `onPostExecute:617` |
| `navigationdrawactivity` | Dialog message | Your Data Removed from our Records. You may hold it for future use or uninstall this app....  | `onPostExecute:619` |
| `navigationdrawactivity` | Dialog button | Yes, Keep the APP | `onPostExecute:621` |
| `navigationdrawactivity` | Dialog button | Want to Uninstall the APP | `onPostExecute:627` |
| `navigationdrawactivity` | Dialog/screen title | Info... | `onPostExecute:639` |
| `navigationdrawactivity` | Dialog button | OK | `onPostExecute:641` |
| `navigationdrawactivity` | Dialog message | Failed to remove your data, Please try again later....  | `onPostExecute:641` |
| `navigationdrawactivity` | Dialog message | Removing your information ... | `onPreExecute:672` |
| `navigationdrawactivity` | Dialog/screen title | ${globalvars.financename} [${(Object) DateFormat.format("dd-MM-yyyy", globalvars.collectiondate)}] | `DisplayIndexData:740` |
| `navigationdrawactivity` | Toast | Please Select Line First | `onNavigationItemSelected:897` |
| `navigationdrawactivity` | Dialog/screen title | Info... | `onNavigationItemSelected:907` |
| `navigationdrawactivity` | Dialog message | Sure to Remove Data.. | `onNavigationItemSelected:909` |
| `navigationdrawactivity` | Dialog button | No | `onNavigationItemSelected:912` |
| `navigationdrawactivity` | Dialog button | Yes | `onNavigationItemSelected:918` |
| `navigationdrawactivity` | Toast | Access Denied | `onPostExecute:1625` |
| `navigationdrawactivity` | Dialog message | Working | `onPreExecute:1632` |
| `navigationdrawactivity` | Dialog message | Working | `onPreExecute:1867` |
| `refreshorreloadbrancheslist` | Dialog/screen title | Branch:${AppSettings.getString("BranchName", "Branch not selected")} | `onCreate:68` |
| `refreshorreloadbrancheslist` | Dialog/screen title | Error... | `onPostExecute:263` |
| `refreshorreloadbrancheslist` | Dialog button | OK | `onPostExecute:264` |
| `refreshorreloadbrancheslist` | Dialog button | Cancel | `onPostExecute:264` |
| `refreshorreloadbrancheslist` | Dialog button | OK | `onPostExecute:286` |
| `refreshorreloadbrancheslist` | Dialog message | No Branches Allowed | `onPostExecute:286` |
| `refreshorreloadbrancheslist` | Dialog message | Loading Branches | `onPreExecute:315` |
| `refreshorreloadbrancheslist` | Dialog/screen title | Info | `onPostExecute:820` |
| `refreshorreloadbrancheslist` | Dialog message | Permission Denied.. This device is not added in selected branch Kindly try with Select Line and then do Sync | `onPostExecute:821` |
| `refreshorreloadbrancheslist` | Dialog button | OK | `onPostExecute:822` |
| `refreshorreloadbrancheslist` | Dialog/screen title | Info | `onPostExecute:833` |
| `refreshorreloadbrancheslist` | Dialog button | OK | `onPostExecute:835` |
| `refreshorreloadbrancheslist` | Dialog/screen title | Info | `onPostExecute:845` |
| `refreshorreloadbrancheslist` | Dialog message | Sync Completed.. Kindly Select Your Line.... | `onPostExecute:846` |
| `refreshorreloadbrancheslist` | Dialog button | OK | `onPostExecute:848` |
| `refreshorreloadbrancheslist` | Dialog message | Sync Begins ... | `onPreExecute:884` |
| `refreshorreloadbrancheslist` | Dialog/screen title | Venus Solutions Welcomes You | `onPostExecute:938` |
| `refreshorreloadbrancheslist` | Dialog button | OK | `onPostExecute:939` |
| `refreshorreloadbrancheslist` | Dialog button | Cancel | `onPostExecute:939` |
| `refreshorreloadbrancheslist` | Dialog message | Do you want to save this device as branch level admin device | `onPostExecute:939` |
| `refreshorreloadbrancheslist` | Dialog/screen title | Venus Solutions Welcomes You | `onPostExecute:955` |
| `refreshorreloadbrancheslist` | Dialog button | OK | `onPostExecute:956` |
| `refreshorreloadbrancheslist` | Dialog message | This device has no permission to access  Kindly contact Admin | `onPostExecute:956` |
| `refreshorreloadbrancheslist` | Dialog message | Verifying User.. | `onPreExecute:978` |
| `refreshorreloadbrancheslist` | Toast | Device Added Successfuly Close and reopen the app | `onPostExecute:1029` |
| `refreshorreloadbrancheslist` | Toast | Unable to add thils Device  contact VENUS SOLUTIONS, 9790070227 9655842840 9790070226 | `onPostExecute:1031` |
| `refreshorreloadbrancheslist` | Dialog message | Verifying User.. | `onPreExecute:1038` |
| `refreshorreloadfinancelist` | Dialog/screen title | Select Line | `onCreate:98` |
| `refreshorreloadfinancelist` | Dialog/screen title | Daily Thittam | `onCreate:102` |
| `refreshorreloadfinancelist` | Dialog/screen title | Select Line | `onCreate:108` |
| `refreshorreloadfinancelist` | Toast | Cur Finance Changed | `onContactSelected:236` |
| `refreshorreloadfinancelist` | Dialog button | OK | `onPostExecute:430` |
| `refreshorreloadfinancelist` | Dialog/screen title | Error Info | `onPostExecute:430` |
| `refreshorreloadfinancelist` | Toast | Done | `onPostExecute:444` |
| `refreshorreloadfinancelist` | Dialog/screen title | Venus Solutions Welcomes You | `onPostExecute:497` |
| `refreshorreloadfinancelist` | Dialog button | OK | `onPostExecute:498` |
| `refreshorreloadfinancelist` | Dialog button | Cancel | `onPostExecute:498` |
| `refreshorreloadfinancelist` | Dialog message | Do you want to save this device as admin device | `onPostExecute:498` |
| `refreshorreloadfinancelist` | Dialog/screen title | Venus Solutions Welcomes You | `onPostExecute:514` |
| `refreshorreloadfinancelist` | Dialog button | OK | `onPostExecute:515` |
| `refreshorreloadfinancelist` | Dialog message | This device has no permission to access  Kindly contact Admin | `onPostExecute:515` |
| `refreshorreloadfinancelist` | Toast | Device Added Successfuly Close and reopen the app | `onPostExecute:614` |
| `refreshorreloadfinancelist` | Toast | Unable to add thils Device  contact VENUS SOLUTIONS, 9790070227 9655842840 9790070226 | `onPostExecute:616` |
| `selectbranchandfinance` | Dialog/screen title | Select Branch and Finance | `onCreate:328` |
| `selectbranchandfinance` | Dialog/screen title | Select Branch and Finance | `onCreate:330` |
| `selectbranchandfinance` | Dialog/screen title | Select Finance | `onCreate:332` |
| `selectbranchandfinance` | Dialog button | OK | `onPostExecute:970` |
| `selectbranchandfinance` | Dialog/screen title | Error Info | `onPostExecute:970` |
| `selectbranchandfinance` | Dialog button | OK | `onPostExecute:997` |
| `selectbranchandfinance` | Dialog/screen title | Error Info | `onPostExecute:997` |
| `selectbranchandfinance` | Dialog message | Working | `onPreExecute:1032` |
| `selectbranchandfinance` | Toast | Authentication Failed | `onPostExecute:1175` |

## 8.3 Offline mode: download, upload and sync services

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `archievedloandisplay` | Dialog/screen title | Archieved Loans | `onCreate:50` |
| `downloaddata` | Dialog button | OK | `SyncWithServer:294` |
| `downloaddata` | Dialog button | OK | `onPostExecute:848` |
| `downloaddata` | Dialog/screen title | Error Info... | `onPostExecute:848` |
| `downloaddata` | Dialog button | OK | `onPostExecute:860` |
| `downloaddata` | Dialog/screen title | Error Info... | `onPostExecute:860` |
| `downloaddata` | Dialog message | ACCESS Denied | `onPostExecute:860` |
| `downloaddata` | Dialog button | OK | `onPostExecute:873` |
| `downloaddata` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onPostExecute:873` |
| `downloaddata` | Dialog button | OK | `onPostExecute:885` |
| `downloaddata` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onPostExecute:885` |
| `downloaddata` | Dialog message |  Subscription expired   Kindly pay the renewal charges | `onPostExecute:885` |
| `downloaddata` | Toast | Download Completed | `onPostExecute:910` |
| `downloaddata` | Toast | Access Denied | `onPostExecute:913` |
| `downloaddata` | Toast | Access Denied | `lambda$onPostExecute$2:937` |
| `downloaddata` | Toast | Download Completed | `lambda$onPostExecute$2:945` |
| `downloaddata` | Dialog message | downloading, please wait ... | `onPreExecute:984` |
| `downloaddata` | Toast | SMS Sent | `onPostExecute:1031` |
| `uploadcollection` | Dialog button | Go to Today Collection | `onPostExecute:1755` |
| `uploadcollection` | Dialog button | OK | `onPostExecute:1755` |
| `uploadcollection` | Dialog/screen title | Upload Status | `onPostExecute:1755` |
| `uploadcollection` | Dialog message | ${message2}  All Components Uploaded Successfully You have to confirm the collection details in Today Collection  | `onPostExecute:1755` |
| `uploadcollection` | Dialog button | OK | `onPostExecute:1770` |
| `uploadcollection` | Dialog/screen title | Upload Status | `onPostExecute:1770` |
| `uploadcollection` | Dialog message | ${message2} One or more Components failed to upload try again  ${ErrorTest} | `onPostExecute:1770` |
| `uploadcollection` | Dialog button | OK | `onPostExecute:1786` |
| `uploadcollection` | Dialog/screen title | Upload Status | `onPostExecute:1786` |
| `uploadcollection` | Dialog message | Connecting, please wait ... | `onPreExecute:1835` |
| `uploadcollection` | Toast | SMS Sent | `onPostExecute:1883` |
| `uploadcollection` | Dialog button | OK | `showAlert:1922` |
| `uploadcollection` | Dialog/screen title | Error Info... | `createPdf:2526` |
| `uploadcollection` | Dialog button | OK | `createPdf:2528` |
| `uploaddownloadcontroling` | Dialog/screen title | Upload Controling | `onCreate:70` |
| `uploaddownloadcontroling` | Dialog/screen title | Download Controling | `onCreate:73` |
| `uploaddownloadcontroling` | Dialog message | uploading ... | `onPreExecute:229` |

## 8.4 Offline collection entry (area-wise collection list & individual collection)

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `directcollectionentry` | Dialog/screen title | Direct Collection | `onCreate:115` |
| `directcollectionentry` | Field error | Bank Collection cannot be greater then Due Collection | `onTextChanged:362` |
| `directcollectionentry` | Dialog button | OK | `lambda$onCreate$3:505` |
| `directcollectionentry` | Dialog message | Future Date cannot be used | `lambda$onCreate$3:505` |
| `directcollectionentry` | Dialog/screen title | Info....  | `lambda$onCreate$3:512` |
| `directcollectionentry` | Dialog button | OK | `onPostExecute:892` |
| `directcollectionentry` | Dialog message | Collection Entry Updated | `onPostExecute:892` |
| `directcollectionentry` | Dialog/screen title | Info  | `onPostExecute:899` |
| `directcollectionentry` | Dialog button | OK | `onPostExecute:905` |
| `directcollectionentry` | Dialog/screen title | Unable to Complete..  | `onPostExecute:912` |
| `selectedloancollectiondetails` | Dialog/screen title | Print and Share Options | `onOptionsItemSelected:182` |
| `selectedloancollectiondetails` | Dialog/screen title | Working | `onPreExecute:956` |
| `selectedloancollectiondetails` | Dialog button | OK | `onPostExecute:1022` |
| `selectedloancollectiondetails` | Dialog/screen title | Error Info | `onPostExecute:1022` |
| `selectedloancollectiondetails` | Dialog message | downloading, please wait ... | `onPreExecute:1052` |
| `tabbedcollectionlist` | Dialog/screen title | Collection List | `onCreate:67` |
| `tabbedcollectionlist` | Dialog/screen title | Parties | `onCreate:69` |
| `tabbedcollectionlistmviewpager` | Dialog/screen title | Collection List | `onCreate:55` |
| `tabbedcollectionlistmviewpager` | Dialog/screen title | Parties | `onCreate:57` |
| `tabbedindividualcollectionentry` | Toast | Not connected | `handleMessage:101` |
| `tabbedindividualcollectionentry` | Toast | Connecting... | `handleMessage:104` |
| `tabbedindividualcollectionentry` | Toast | Connected: ${mConnectedDeviceName} | `handleMessage:107` |
| `tabbedindividualcollectionentry` | Dialog/screen title | Collection Entry | `onCreate:167` |
| `tabbedindividualcollectionentry` | Field error | Bank Collection cannot be greater then Due Collection | `onTextChanged:669` |
| `tabbedindividualcollectionentry` | Field error | Bank Collection cannot be greater then Due Collection | `onTextChanged:1173` |
| `tabbedindividualcollectionentry` | Dialog/screen title | Choose Action | `lambda$onCreateView$4:1365` |
| `tabbedindividualcollectionentry` | Dialog message | What do you want to do? | `lambda$onCreateView$4:1366` |
| `tabbedindividualcollectionentry` | Dialog button | Connect Printer | `lambda$onCreateView$4:1367` |
| `tabbedindividualcollectionentry` | Dialog button | Get Location | `lambda$onCreateView$4:1373` |
| `tabbedindividualcollectionentry` | Dialog button | Cancel | `lambda$onCreateView$4:1379` |
| `tabbedindividualcollectionentry` | Toast | Printer is not connected | `lambda$onCreateView$6:1415` |
| `tabbedindividualcollectionentry` | Toast | This feature is not enabled | `lambda$onCreateView$11:1467` |
| `tabbedindividualcollectionentry` | Toast | This feature is not enabled | `lambda$onCreateView$12:1476` |
| `tabbedindividualcollectionentry` | Snackbar | Access Denied | `lambda$onCreateView$14:1500` |
| `tabbedindividualcollectionentry` | Toast | Unable to open phone app | `makeCall:1594` |
| `tabbedindividualcollectionentry` | Toast | SMS Sent | `sendsms:1607` |

## 8.5 Online collection entry & editing

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `editing_collectionlist_act` | Dialog/screen title | ${selectedfinanceobj.getFinMailingName()}-Coll List | `onCreate:97` |
| `editing_collectionlist_act` | Dialog button | OK | `onPostExecute:348` |
| `editing_collectionlist_act` | Dialog/screen title | Error Info | `onPostExecute:348` |
| `editing_collectionlist_act` | Dialog message | downloading, please wait ... | `onPreExecute:369` |
| `editing_collections_act` | Dialog/screen title | ${(Object) DateFormat.format("dd-MM-yyyy", selectedcollection.getCollDa…}-Coll List | `handleExtras:170` |
| `editing_collections_act` | Dialog/screen title | Warning... | `onOptionsItemSelected:337` |
| `editing_collections_act` | Dialog button | OK | `onOptionsItemSelected:339` |
| `editing_collections_act` | Dialog button | Cancel | `onOptionsItemSelected:339` |
| `editing_collections_act` | Dialog message | you about to delete entire collection of the day, if it is deleted then  it can not be get back  | `onOptionsItemSelected:339` |
| `editing_collections_act` | Dialog/screen title | Print and Share Options | `onOptionsItemSelected:356` |
| `editing_collections_act` | Dialog button | OK | `onPostExecute:1186` |
| `editing_collections_act` | Dialog/screen title | Error Info | `onPostExecute:1186` |
| `editing_collections_act` | Dialog button | OK | `onPostExecute:1206` |
| `editing_collections_act` | Dialog/screen title | Info | `onPostExecute:1206` |
| `editing_collections_act` | Toast | SMS Sent | `onPostExecute:1307` |
| `editing_collections_act` | Toast | SMS Sent | `onPostExecute:1386` |
| `editing_collections_act` | Dialog/screen title | ${(Object) DateFormat.format("dd-MM-yyyy", selectedcollectiondate)}-Coll List | `onActivityResult:1405` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | ${(Object) DateFormat.format("dd-MM-yyyy", selectedcollection.getCollDa…}-Coll List | `onCreate:233` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | Warning... | `onOptionsItemSelected:476` |
| `editing_collectionswithfulloptions_act` | Dialog button | OK | `onOptionsItemSelected:478` |
| `editing_collectionswithfulloptions_act` | Dialog button | Cancel | `onOptionsItemSelected:478` |
| `editing_collectionswithfulloptions_act` | Dialog message | you about to delete entire collection of the day, if it is deleted then  it can not be get back  | `onOptionsItemSelected:478` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | Print and Share Options | `onOptionsItemSelected:495` |
| `editing_collectionswithfulloptions_act` | Dialog button | OK | `onPostExecute:1627` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | Error Info... | `onPostExecute:1627` |
| `editing_collectionswithfulloptions_act` | Dialog button | OK | `onPostExecute:1637` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onPostExecute:1637` |
| `editing_collectionswithfulloptions_act` | Dialog message |  Access not Allowed | `onPostExecute:1637` |
| `editing_collectionswithfulloptions_act` | Dialog button | OK | `onPostExecute:2005` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | Error Info | `onPostExecute:2005` |
| `editing_collectionswithfulloptions_act` | Dialog button | OK | `onPostExecute:2030` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | Info | `onPostExecute:2030` |
| `editing_collectionswithfulloptions_act` | Toast | SMS Sent | `onPostExecute:2155` |
| `editing_collectionswithfulloptions_act` | Toast | SMS Sent | `onPostExecute:2244` |
| `editing_collectionswithfulloptions_act` | Toast | SMS Sent | `onPostExecute:2333` |
| `editing_collectionswithfulloptions_act` | Dialog/screen title | ${(Object) DateFormat.format("dd-MM-yyyy", selectedcollectiondate)}-Coll List | `onActivityResult:2352` |

## 8.6 Collection reports (collection for a day, abstract, 7-days, unpaid parties, day-wise pending, periodic loans)

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `collectionarealistforloanpartyandloans` | Dialog/screen title | Collection Areas in ${LineName} | `DisplayIndexData:71` |
| `collectionarealistforsevendayscollection` | Dialog/screen title | ${LineName} [7DC] | `DisplayIndexData:71` |
| `daywisependingreport_act` | Dialog/screen title | Daywise Pending | `onCreate:78` |
| `daywisependingreport_act` | Dialog/screen title | Error... | `onPostExecute:244` |
| `daywisependingreport_act` | Dialog button | OK | `onPostExecute:245` |
| `daywisependingreport_act` | Dialog button | Cancel | `onPostExecute:245` |
| `daywisependingreport_act` | Dialog message | Working | `onPreExecute:275` |
| `newsevendayscollection` | Snackbar | Replace with your own action | `onClick:46` |
| `periodicloareportlist_act` | Dialog/screen title | Periodic Loans | `onCreate:80` |
| `periodicloareportlist_act` | Dialog/screen title | Error Info... | `onPostExecute:249` |
| `periodicloareportlist_act` | Dialog button | OK | `onPostExecute:251` |
| `periodicloareportlist_act` | Dialog message | Loading Details | `onPreExecute:274` |
| `selectdateforreport` | Dialog/screen title | Unpaid Parties for the date | `onCreate:52` |
| `sevendayscollectionforselline` | Dialog/screen title | Collection List | `onCreate:76` |
| `sevendayscollectionforselline` | Dialog/screen title | Parties | `onCreate:78` |
| `sevendayscollectionforselline` | Dialog/screen title | 7 நாள் வசூல் | `onCreate:82` |
| `sevendayscollectionforselline` | Toast | Access Denied | `onContactSelected:203` |
| `unpaidpartiesonselectedday` | Dialog/screen title | DETAILS${k} | `initDate:97` |

## 8.7 Parties (borrowers), new loan creation and KYC documents

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `ImagePickerActivity` | Toast | @string/toast_image_intent_null | `onCreate:63` |
| `documentsphotossearchlist` | Dialog/screen title | Photos | `onCreate:80` |
| `documentsupload` | Dialog button | OK | `showAlert:223` |
| `documentsupload` | Dialog/screen title | Response from Servers | `showAlert:223` |
| `newloancreation` | Dialog/screen title | New Loan-OFFLine | `onCreate:337` |
| `newloancreation` | Dialog/screen title | New Loan-online | `onCreate:344` |
| `newloancreation` | Dialog/screen title | New Loan-OFFLine | `onCreate:350` |
| `newloancreation` | Dialog/screen title | Edit Loan-OFFLine | `onCreate:410` |
| `newloancreation` | Dialog/screen title | Edit Loan-Online | `onCreate:491` |
| `newloancreation` | Toast | Loan Amount Exceed | `afterTextChanged:577` |
| `newloancreation` | Dialog button | OK | `lambda$onCreate$10:806` |
| `newloancreation` | Dialog message | Future Date cannot be used | `lambda$onCreate$10:806` |
| `newloancreation` | Dialog/screen title | Info....  | `lambda$onCreate$10:813` |
| `newloancreation` | Dialog/screen title | Info... | `lambda$onCreate$14:826` |
| `newloancreation` | Dialog button | OK | `SaveLoan:1143` |
| `newloancreation` | Dialog message | Invalid phone number | `SaveLoan:1143` |
| `newloancreation` | Dialog/screen title | Info....  | `SaveLoan:1150` |
| `newloancreation` | Dialog button | OK | `SaveLoan:1161` |
| `newloancreation` | Dialog/screen title | Info....  | `SaveLoan:1168` |
| `newloancreation` | Dialog button | OK | `SaveLoan:1178` |
| `newloancreation` | Dialog/screen title | Info....  | `SaveLoan:1185` |
| `newloancreation` | Dialog button | OK | `SaveLoan:1190` |
| `newloancreation` | Dialog message | Chec Installments and Installment amount  | `SaveLoan:1190` |
| `newloancreation` | Dialog/screen title | Invalid manual Calculation | `SaveLoan:1190` |
| `newloancreation` | Dialog button | OK | `SaveLoan:1194` |
| `newloancreation` | Dialog message | No of Installments and Installment amount cannot be zero. | `SaveLoan:1194` |
| `newloancreation` | Dialog/screen title | Invalid Input | `SaveLoan:1194` |
| `newloancreation` | Toast | New Loan Added | `SaveLoan:1319` |
| `newloancreation` | Toast | ${e.getMessage()} | `SaveLoan:1348` |
| `newloancreation` | Toast | Loan is saved | `onPostExecute:1639` |
| `newloancreation` | Dialog button | OK | `onPreExecute:1658` |
| `newloancreation` | Dialog message | No of Installments and Installment amount cannot be zero. | `onPreExecute:1658` |
| `newloancreation` | Dialog/screen title | Invalid Input | `onPreExecute:1658` |
| `newloancreation` | Dialog button | OK | `onPreExecute:1661` |
| `newloancreation` | Dialog message | Chec Installments and Installment amount  | `onPreExecute:1661` |
| `newloancreation` | Dialog/screen title | Invalid manual Calculation | `onPreExecute:1661` |
| `newloancreation` | Toast | SMS Sent | `onPostExecute:1799` |
| `newloancreation` | Toast | SMS Sent | `onPostExecute:1870` |
| `partyandpartnerdetails` | Dialog/screen title | Partner Details | `onCreate:134` |
| `partyandpartnerdetails` | Dialog/screen title | Party Details | `onCreate:145` |
| `partyandpartnerdetails` | Dialog button | OK | `lambda$onCreate$5:279` |
| `partyandpartnerdetails` | Dialog message | NAME CANNOT BE EMPTY... | `lambda$onCreate$5:279` |
| `partyandpartnerdetails` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `lambda$onCreate$5:286` |
| `partyandpartnerdetails` | Dialog button | அழிக்க வேண்டாம் | `lambda$onCreate$8:315` |
| `partyandpartnerdetails` | Dialog button | அழிக்கலாம் | `lambda$onCreate$8:315` |
| `partyandpartnerdetails` | Dialog message | அழித்தப் பின் திரும்ப பெற இயலாது என்பதை கவனத்தில் கொள்ளவும்... | `lambda$onCreate$8:315` |
| `partyandpartnerdetails` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `lambda$onCreate$8:327` |
| `partyandpartnerdetails` | Dialog button | OK | `showAlert:404` |
| `partyandpartnerdetails` | Dialog/screen title | Response from Servers | `showAlert:404` |
| `partyandpartnerdetails` | Dialog button | சரி | `onPostExecute:573` |
| `partyandpartnerdetails` | Dialog message | தொடர்பான பதிவுகள் இருப்பதால் நீக்க இயலாது... | `onPostExecute:573` |
| `partyandpartnerdetails` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `onPostExecute:580` |
| `partydetails_act` | Dialog/screen title | Create Party (Offline) | `onCreate:265` |
| `partydetails_act` | Dialog/screen title | Create Party (Online) | `onCreate:289` |
| `partydetails_act` | Dialog/screen title | Edit Party (Online) | `onCreate:295` |
| `partydetails_act` | Dialog/screen title | Edit Party (Online-Admin) | `onCreate:331` |
| `partydetails_act` | Dialog/screen title | Edit Party (Offline) | `onCreate:345` |
| `partydetails_act` | Dialog button | OK | `onCreate:357` |
| `partydetails_act` | Dialog/screen title | Error Info | `onCreate:357` |
| `partydetails_act` | Field error | பார்ட்டி பெயர் நிரப்பப்படவில்லை | `lambda$onCreate$8:472` |
| `partydetails_act` | Field error | Tag Cannot be Empty | `lambda$onCreate$8:477` |
| `partydetails_act` | Field error | Mobile No Cannot be Empty | `lambda$onCreate$8:481` |
| `partydetails_act` | Field error | Spouse Name No Cannot be Empty | `lambda$onCreate$8:485` |
| `partydetails_act` | Field error | Location No Cannot be Empty | `lambda$onCreate$8:489` |
| `partydetails_act` | Toast | Image cannot be Empty | `lambda$onCreate$8:493` |
| `partydetails_act` | Toast | IdProof Image cannot be Empty | `lambda$onCreate$8:497` |
| `partydetails_act` | Toast | IdProof II Image cannot be Empty | `lambda$onCreate$8:501` |
| `partydetails_act` | Dialog button | OK | `lambda$onCreate$8:545` |
| `partydetails_act` | Dialog/screen title | Incomplete Input... | `lambda$onCreate$8:545` |
| `partydetails_act` | Dialog message | Kinldy Fill necessary values | `lambda$onCreate$8:545` |
| `partydetails_act` | Toast | Deleted | `lambda$onCreate$9:575` |
| `partydetails_act` | Toast | Location permission denied | `onRequestPermissionsResult:640` |
| `partydetails_act` | Dialog button | OK | `deleteparty:650` |
| `partydetails_act` | Dialog/screen title | Error Info | `deleteparty:650` |
| `partydetails_act` | Dialog message | Loan Exisits  Party cannot be deleted | `deleteparty:650` |
| `partydetails_act` | Toast | Deleted | `deleteparty:662` |
| `partydetails_act` | Dialog button | OK | `onPostExecute:1012` |
| `partydetails_act` | Dialog/screen title | Error Info | `onPostExecute:1012` |
| `partydetails_act` | Dialog button | OK | `onPostExecute:1264` |
| `partydetails_act` | Dialog/screen title | Id Proof II Image Uploade error.. | `onPostExecute:1264` |
| `partydetails_act` | Dialog button | OK | `onPostExecute:1508` |
| `partydetails_act` | Dialog/screen title | Id Proof I Image Upload error.. | `onPostExecute:1508` |
| `partydetails_act` | Toast | Party Information Updated | `returntobackornextscreen:1921` |
| `partydetails_act` | Toast | Unable to resolve location settings. | `lambda$checkLocationSettingsAndFetchLocation$16:1959` |
| `partydetails_act` | Toast | Unable to get location. Try again later. | `lambda$fetchLocationnew$17:1989` |
| `partydetails_act` | Toast | Failed to retrieve location: ${e.getMessage()} | `lambda$fetchLocationnew$18:1994` |
| `partydetails_searchlist` | Dialog/screen title | Party Details | `onCreate:78` |
| `partydetails_searchlist` | Dialog button | OK | `onActivityResult:143` |
| `partydetails_searchlist` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onActivityResult:143` |
| `partydetails_searchlist` | Dialog message | பார்ட்டி விவரம் நீக்கப்பட்டது. | `onActivityResult:143` |
| `partydetails_searchlist` | Dialog button | OK | `onActivityResult:155` |
| `partydetails_searchlist` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onActivityResult:155` |
| `partydetails_searchlist` | Dialog message | பதிவுகள் இருப்பதால் இந்த பார்ட்டியை நீக்க இயலாது | `onActivityResult:155` |
| `partydetails_searchlist` | Dialog button | OK | `onActivityResult:178` |
| `partydetails_searchlist` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onActivityResult:178` |
| `partydetails_searchlist` | Dialog message | பார்ட்டி விவரம் நீக்கப்பட்டது. | `onActivityResult:178` |
| `partydetails_searchlist` | Dialog button | OK | `onActivityResult:190` |
| `partydetails_searchlist` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onActivityResult:190` |
| `partydetails_searchlist` | Dialog message | பதிவுகள் இருப்பதால் இந்த பார்ட்டியை நீக்க இயலாது | `onActivityResult:190` |
| `partylistwithprevloansfornewloanselection` | Toast | New Loans cannot be added.. | `onContactSelected:259` |
| `partylistwithprevloansfornewloanselection` | Toast | ACCES DENIED | `onprevloansbuttonclicked:271` |

## 8.8 Loan lists, loan & savings closures, party transfer and rearrangement

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `closableloanssearchlistact` | Dialog/screen title | Live Loans | `onCreate:82` |
| `closablesavings_list` | Dialog/screen title | Live Saving Accounts | `onCreate:72` |
| `closablesavings_list` | Dialog button | OK | `onPostExecute:208` |
| `closablesavings_list` | Dialog/screen title | Error Info | `onPostExecute:208` |
| `closablesavings_list` | Dialog message | downloading, please wait ... | `onPreExecute:226` |
| `closed_renewedloanslist_act` | Dialog message | Working | `onPreExecute:527` |
| `closingsavingsaccount` | Dialog/screen title | Loan Closure Entry | `onCreate:73` |
| `closingsavingsaccount` | Dialog button | OK | `onPostExecute:235` |
| `closingsavingsaccount` | Dialog/screen title | Error Info | `onPostExecute:235` |
| `closingsavingsaccount` | Dialog message | Updating | `onPreExecute:264` |
| `loanclosoureentry` | Dialog/screen title | Loan Closure New Entry | `onCreate:84` |
| `loanclosoureentry` | Toast | ${e.getMessage()} | `onCreate:91` |
| `loanclosoureentry` | Dialog/screen title | Loan Closure New Entry | `onActivityResult:289` |
| `loanclosoureentry` | Dialog/screen title | Loan Closure Edit Entry | `onActivityResult:319` |
| `loanclosoureentry` | Dialog message | Updating | `onPreExecute:472` |
| `loanindexloanswiselist_act` | Toast | ACCESS DENIED | `onLoanselected:189` |
| `loanindexloanswiselist_act` | Toast | ACCESS DENIED | `onLoanselected:202` |
| `loanindexloanswiselist_act` | Toast | ACCESS DENIED | `onLoanselected:205` |
| `loanindexloanswiselist_act` | Toast | ACCES DENIED | `onprevloansbuttonclicked:220` |
| `loanslist_act` | Dialog/screen title | List of Loans | `onCreate:108` |
| `loanslist_act` | Dialog/screen title | ${extras.getString("LineName")}-Loans | `onCreate:112` |
| `loanslist_act` | Dialog/screen title | Loans-${(Object) DateFormat.format("dd-MM-yyyy", curdaterange.getFromdate())} | `onCreate:140` |
| `loanslist_act` | Dialog button | OK | `onActivityResult:324` |
| `loanslist_act` | Dialog/screen title | Info... | `onActivityResult:324` |
| `loanslist_act` | Dialog message | Loan Deleted. | `onActivityResult:324` |
| `loanslist_act` | Dialog button | OK | `onActivityResult:336` |
| `loanslist_act` | Dialog/screen title | Info... | `onActivityResult:336` |
| `loanslist_act` | Dialog message | Related entries found Loan cannot be deleted | `onActivityResult:336` |
| `loanslist_act` | Dialog/screen title | Choose an Option | `onContactSelected:368` |
| `loanslist_act` | Dialog message | downloading, please wait ... | `onPreExecute:602` |
| `newloanslist_act` | Dialog/screen title | Info | `onCreate:103` |
| `newloanslist_act` | Dialog message | No Loans Created today.. | `onCreate:105` |
| `newloanslist_act` | Dialog button | OK | `onCreate:106` |
| `newpartieslist_act` | Dialog/screen title | Info | `onCreate:78` |
| `newpartieslist_act` | Dialog message | No Parties Created today.. | `onCreate:80` |
| `newpartieslist_act` | Dialog button | OK | `onCreate:81` |
| `newpartiesnewloanslist_act` | Dialog/screen title | Error... | `onPostExecute:325` |
| `newpartiesnewloanslist_act` | Dialog button | OK | `onPostExecute:326` |
| `newpartiesnewloanslist_act` | Dialog button | Cancel | `onPostExecute:326` |
| `newpartiesnewloanslist_act` | Dialog message | Working | `onPreExecute:356` |
| `partyrearrange` | Dialog/screen title | Rearrange-${extras.getString("areaname")} | `onCreate:62` |
| `partyrearrange` | Dialog message | uploading ... | `onPreExecute:204` |
| `partytransfer` | Dialog/screen title | PARTY TRANSFER | `onCreate:57` |
| `partytransfer` | Toast | Transfer Completed | `onPostExecute:156` |
| `partytransfer` | Dialog message | Transfering ... | `onPreExecute:162` |
| `partytransfer` | Dialog/screen title | Incomplete Data | `showincompletedataalert:202` |
| `partytransfer` | Dialog button | OK | `showincompletedataalert:203` |
| `partytransfer` | Dialog message | Kindly ensure whether Party and New Area Selected | `showincompletedataalert:203` |
| `prevloanclosure_list_act` | Dialog/screen title | Closed Loans | `onCreate:82` |
| `prevloanclosure_list_act` | Dialog/screen title | Prev Loan Closures | `onCreate:107` |
| `prevloanclosure_list_act` | Dialog message | Connecting | `onPreExecute:338` |
| `prevloanslist_act` | Dialog message | Connecting | `onPreExecute:256` |
| `prevloanslist_act` | Dialog message | Connecting | `onPreExecute:336` |
| `prevsavingclosure_list_act` | Dialog/screen title | Saving Account Closure List | `onCreate:71` |
| `prevsavingclosure_list_act` | Dialog button | NO | `onContactSelected:145` |
| `prevsavingclosure_list_act` | Dialog button | YES | `onContactSelected:145` |
| `prevsavingclosure_list_act` | Dialog/screen title | Confirm Delete | `onContactSelected:145` |
| `prevsavingclosure_list_act` | Dialog message | Sure to delete?... | `onContactSelected:145` |
| `prevsavingclosure_list_act` | Dialog message | Connecting | `onPreExecute:210` |
| `prevsavingclosure_list_act` | Dialog button | OK | `onPostExecute:263` |
| `prevsavingclosure_list_act` | Dialog/screen title | Confirmation | `onPostExecute:263` |
| `prevsavingclosure_list_act` | Dialog message | Deleted... | `onPostExecute:263` |
| `prevsavingclosure_list_act` | Dialog message | Processing.... | `onPreExecute:280` |

## 8.9 Collection areas, maps, GPS staff tracking and attendance

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `collectionareadetails_act` | Dialog/screen title | Edit Area | `onCreate:119` |
| `collectionareadetails_act` | Toast | Unable to resolve location settings. | `lambda$checkLocationSettingsAndFetchLocation$6:361` |
| `collectionareadetails_act` | Toast | Unable to get location. Try again later. | `lambda$fetchLocationnew$7:393` |
| `collectionareadetails_act` | Toast | Failed to retrieve location: ${e.getMessage()} | `lambda$fetchLocationnew$8:398` |
| `collectionarearearrangeact` | Dialog message | uploading ... | `onPreExecute:198` |
| `collectionareassearchlist` | Dialog/screen title | Warning... | `showalert:266` |
| `collectionareassearchlist` | Dialog button | OK | `showalert:267` |
| `collectionareassearchlist` | Dialog message | தொடர்பான பதிவுகள் இருப்பதால் நீக்க இயலாது.. | `showalert:267` |
| `collectionareassearchlist` | Dialog/screen title | Select a Weekday | `showWeekdaySelectionDialog:478` |
| `collectionareassearchlist` | Dialog/screen title | Select a Weekday | `showmonthdaySelectionDialog:503` |
| `maps_partydisplay` | Dialog/screen title | Rearrange-${extras.getString("areaname")} | `onMapReady:62` |
| `staffgpstrackmap_act` | Dialog/screen title | Staff Route | `setupToolbar:93` |
| `staffgpstrackmap_act` | Dialog button | OK | `onPostExecute:419` |
| `staffgpstrackmap_act` | Dialog/screen title | Error Info | `onPostExecute:419` |

## 8.10 Accounts reports: trial balance, balance sheet, P&L, month-wise P&L

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `accountsreports_parameters` | Dialog/screen title | Balancesheet | `onCreate:110` |
| `accountsreports_parameters` | Dialog/screen title | Trailbalance | `onCreate:113` |
| `accountsreports_parameters` | Dialog/screen title | Profit and Loss Statement | `onCreate:116` |
| `accountsreports_parameters` | Dialog/screen title | Monthly P&L | `onCreate:121` |
| `accountsreports_parameters` | Dialog/screen title | Monthwise P&L | `onCreate:128` |
| `accountsreports_parameters` | Dialog/screen title | Incomplete data... | `showincompletedataalert:255` |
| `accountsreports_parameters` | Dialog button | OK | `showincompletedataalert:256` |
| `accountsreports_parameters` | Dialog message | Finance not Selected | `showincompletedataalert:256` |
| `monthlypandl_fragment` | Toast | Multiple Finance Report, cannot show details | `onTransactionselected:225` |
| `monthlypandl_fragment` | Toast | Sharing tools have not been installed. | `sharePdf:438` |
| `tbbspl_list` | Dialog/screen title | BALANCE SHEET | `onCreate:157` |
| `tbbspl_list` | Dialog/screen title | TRAILBALANCE | `onCreate:160` |
| `tbbspl_list` | Dialog/screen title | P & L STATEMENT | `onCreate:163` |
| `tbbspl_list` | Dialog button | OK | `onPostExecute:710` |
| `tbbspl_list` | Dialog/screen title | Error Info... | `onPostExecute:710` |
| `tbbspl_list` | Dialog button | OK | `onPostExecute:958` |
| `tbbspl_list` | Dialog/screen title | Error Info... | `onPostExecute:958` |
| `tbbspl_list` | Toast | Sharing tools have not been installed. | `sharePdf:1074` |
| `tbbspl_particulars` | Dialog/screen title |  | `onCreate:81` |

## 8.11 Daily Patti (outstanding) reports and meeting report

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `dailypatti_kadanpatrulist_act` | Dialog/screen title | Parties | `onCreate:62` |
| `dailypatti_nadapunippucollectionlist_act` | Dialog/screen title | Parties | `onCreate:61` |
| `dailypatti_varakadanillapulist_act` | Dialog/screen title | Parties TEST | `onCreate:62` |
| `dailypattinew_act` | Dialog/screen title | @string/Patti${(Object) DateFormat.format("dd-MM-yy", fromdate_patti)} to ${(Object) DateFormat.format("dd-MM-yy", todate_patti)} | `onCreate:104` |
| `dailypattinew_act` | Dialog message | Loading.... | `onPreExecute:1088` |
| `meetingreport` | Toast | ${partyleveldataList.get(position).getLedgername()} | `lambda$onCreate$3:218` |
| `meetingreport` | Dialog/screen title | ST Loan Interest Pending | `prepareListData:384` |
| `meetingreport` | Dialog/screen title | OF Loan Interest Pending | `prepareListData:436` |
| `meetingreport` | Toast | Error while opening the document | `createPdf:841` |
| `meetingreport` | Toast | Call Failed, please try again later. | `lambda$voicecallandsmsoptions$7:1293` |
| `meetingreport` | Toast | Call Failed, please try again later. | `lambda$voicecallandsmsoptions$7:1303` |
| `meetingreport` | Toast | Message Sent | `lambda$voicecallandsmsoptions$7:1319` |
| `meetingreport` | Toast | Sharing tools have not been installed. | `lambda$voicecallandsmsoptions$7:1339` |
| `meetingreport` | Toast | Sharing tools have not been installed. | `lambda$voicecallandsmsoptions$7:1356` |
| `pattidetails_loans_act` | Dialog message | Connecting | `onPreExecute:221` |

## 8.12 Thittam (collection plan) and weekly analysis reports

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `allfinancedailythittam` | Dialog/screen title | Daily Thittam  (${(Object) DateFormat.format("dd-MM-yy", fromdate_patti)}) | `onCreate:69` |
| `dailythittamforallowedlines` | Dialog/screen title | திட்டம்-${(Object) DateFormat.format("dd-MM-yyyy", receivedreportdate)} | `onCreate:98` |
| `dailythittamforallowedlines` | Toast | பட்டியல் காலியாக உள்ளது | `preparePrintData:185` |
| `dailythittamforallowedlines` | Toast | PDF உருவாக்க முடியவில்லை | `writePdfToFile:239` |
| `dailythittamforallowedlines` | Toast | ${R.string.noapptoopenpdf} | `createPdf:262` |
| `simplethittamlist_act` | Dialog/screen title | Simple Thittam | `onCreate:119` |
| `simplethittamlist_act` | Dialog/screen title | ST:${(Object) DateFormat.format("dd-MM-yy", fromdate_patti)} to ${(Object) DateFormat.format("dd-MM-yy", todate_patti)} | `onCreate:129` |
| `simplethittamlist_act` | Toast | ${ErrorText} | `onPostExecute:388` |
| `simplethittamlist_act` | Dialog message | Loading Details | `onPreExecute:395` |
| `singlefinancedailythittam_act` | Dialog/screen title | Daily Thittam  (${(Object) DateFormat.format("dd-MM-yy", fromdate_patti)}) | `onCreate:83` |
| `singlefinancedailythittam_act` | Dialog message | Loading Details | `onPreExecute:203` |
| `varathittam` | Dialog/screen title | வாரத்திட்டம் | `onCreate:130` |
| `varathittam` | Dialog/screen title | Print Options | `onPostExecute:867` |
| `varathittam` | Dialog message | Working, please wait ... | `onPreExecute:897` |
| `weeklycollectionanalysis_act` | Dialog/screen title | Weekly Collection Analysis | `onCreate:76` |
| `weeklycollectionanalysis_act` | Dialog/screen title | Error Info... | `onPostExecute:201` |
| `weeklycollectionanalysis_act` | Dialog button | OK | `onPostExecute:203` |
| `weeklycollectionanalysis_act` | Dialog message | Loading Details | `onPreExecute:226` |
| `weeklycollectionpatti_dashboard` | Dialog message | Preparing... | `onProgressUpdate:146` |

## 8.13 Accounts transactions, cash/bank book, expenses & incomes, hand loans and ledger statements

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `accledgers_searchlist` | Dialog/screen title | Accounts Ledgers | `onCreate:60` |
| `accledgers_searchlist` | Toast | Deleted | `onActivityResult:164` |
| `accledgers_searchlist` | Toast | Transactions found cannot delete | `onActivityResult:168` |
| `accledgers_searchlist` | Toast | ${error} | `onPostExecute:277` |
| `accounttransactions_act` | Dialog/screen title | Accounts Transaction | `onCreate:92` |
| `accounttransactions_act` | Dialog/screen title | Alert | `lambda$onCreate$12:394` |
| `accounttransactions_act` | Dialog button | Cancel | `lambda$onCreate$12:395` |
| `accounttransactions_act` | Dialog button | OK | `lambda$onCreate$12:395` |
| `accounttransactions_act` | Dialog message | Deleted Transactions cannot be Recovered | `lambda$onCreate$12:395` |
| `accounttransactions_act` | Dialog button | OK | `onPostExecute:666` |
| `accounttransactions_act` | Dialog/screen title | Error Info | `onPostExecute:666` |
| `accounttransactions_act` | Dialog button | OK | `onPostExecute:843` |
| `accounttransactions_act` | Dialog/screen title | Error Info | `onPostExecute:843` |
| `accounttransactions_act` | Dialog button | OK | `onPostExecute:861` |
| `accounttransactions_act` | Dialog/screen title | Info | `onPostExecute:861` |
| `accounttransactions_act` | Dialog message | Transaction Updated | `onPostExecute:861` |
| `accounttransactions_act` | Dialog button | OK | `showincompletedataalert:977` |
| `cashbook` | Dialog button | BACK | `lambda$setupListeners$7:261` |
| `cashbook` | Dialog button | CONFIRM | `lambda$setupListeners$7:261` |
| `cashbook` | Dialog message | This action will finalize the Cash Book. Do you want to continue? | `lambda$setupListeners$7:261` |
| `cashbook` | Dialog/screen title | Cash Book Verification | `lambda$setupListeners$7:261` |
| `cashbook` | Toast | No cash book entries found for verification | `lambda$setupListeners$7:273` |
| `cashbook` | Dialog/screen title | Abstract Cash Book | `DisplayAbstractCashBook:313` |
| `cashbook` | Dialog/screen title | Detailed Cash Book | `DisplayDetailedCashBook:329` |
| `cashbook` | Toast | PDF Created Successfully | `createPdf:392` |
| `cashbook` | Toast | Something went wrong: ${e2} | `createPdf:395` |
| `cashbook` | Dialog button | OK | `onPostExecute:672` |
| `cashbook` | Dialog/screen title | Error Info | `onPostExecute:672` |
| `cashbook` | Dialog button | OK | `onPostExecute:767` |
| `cashbook` | Dialog/screen title | Error Info | `onPostExecute:767` |
| `cashbook` | Dialog button | OK | `onPostExecute:823` |
| `cashbook` | Dialog/screen title | Error Info | `onPostExecute:823` |
| `cashbook` | Dialog button | OK | `onPostExecute:830` |
| `cashbook` | Dialog/screen title | Info | `onPostExecute:830` |
| `cashbook` | Dialog message | All Verification on this day are Cleared | `onPostExecute:830` |
| `cashbook` | Toast | SMS Sent | `onPostExecute:877` |
| `cashbookabstract` | Dialog/screen title | Cash Book Abstract | `onCreate:63` |
| `cashbookabstract` | Dialog button | OK | `onPostExecute:193` |
| `cashbookabstract` | Dialog/screen title | Error Info... | `onPostExecute:193` |
| `cashreceiveandreturns` | Dialog button | No | `lambda$onCreate$2:82` |
| `cashreceiveandreturns` | Dialog button | Yes | `lambda$onCreate$2:82` |
| `cashreceiveandreturns` | Dialog/screen title | Alert | `lambda$onCreate$2:82` |
| `cashreceiveandreturns` | Toast | Transaction added | `onPostExecute:139` |
| `expensesandreceipt_offlineentry` | Dialog/screen title | New Receipt Entry | `onCreate:66` |
| `expensesandreceipt_offlineentry` | Dialog/screen title | New Expenses Entry | `onCreate:70` |
| `expensesandreceipt_offlineentry` | Dialog/screen title | Edit Receipt Entry | `onCreate:80` |
| `expensesandreceipt_offlineentry` | Dialog/screen title | Edit Expenses Entry | `onCreate:84` |
| `expensesandreceipt_offlineentry` | Toast | New Expenses Added | `saveexpenses:182` |
| `expensesandreceipt_offlineentry` | Toast | New Receipt Added | `saveexpenses:184` |
| `expensesandreceipt_offlineentry` | Toast | Expenses updated | `saveexpenses:197` |
| `expensesandreceipt_offlineentry` | Toast | Receipt updated | `saveexpenses:199` |
| `expensesandreceipt_offlineentry` | Toast | ${e.getMessage()} | `saveexpenses:205` |
| `expensesandreceipt_offlineentry` | Toast | Expenses Deleted | `deleteexpenses:214` |
| `expensesandreceipt_offlineentry` | Toast | ${e.getMessage()} | `deleteexpenses:218` |
| `handloans_list` | Dialog/screen title | Hand loans | `onCreate:113` |
| `handloans_list` | Dialog/screen title | Options | `onContactSelected:179` |
| `handloans_list` | Dialog button | OK | `onPostExecute:268` |
| `handloans_list` | Dialog/screen title | Error Info | `onPostExecute:268` |
| `handloans_list` | Dialog/screen title | Options | `showprintandshareoptions:331` |
| `handloans_list` | Toast | Sharing tools have not been installed. | `sharePdf:457` |
| `ledgerstatement_act` | Dialog/screen title | ${selectedledgername}${'('}${(Object) DateFormat.format("dd-MM-yy", curdaterange.getFromdate())} to ${(Object) DateFormat.format("dd-MM-yy", curdaterange.getTodate())}) | `onCreate:130` |
| `ledgerstatement_act` | Dialog/screen title | ${selectedledgername}${'('}${(Object) DateFormat.format("dd-MM-yy", curdaterange.getFromdate())} to ${(Object) DateFormat.format("dd-MM-yy", curdaterange.getTodate())}) | `onPostExecute:366` |
| `ledgerstatement_act` | Dialog message | Working | `onPreExecute:374` |
| `ledgerstatmentsdateselection` | Dialog/screen title | Ledger and Period Selection | `onCreate:71` |
| `ledgerstatmentsdateselection` | Dialog message | Loading Ledgers List | `onPreExecute:218` |

## 8.14 ST Loans and OF Loans (interest-bearing loan books)

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `ofloan_interestcalculationdetails` | Dialog/screen title | OFLoan Interest Details  | `onCreate:56` |
| `ofloan_interestcalculationdetails` | Dialog/screen title | Error... | `showincompletedataalert:284` |
| `ofloan_interestcalculationdetails` | Dialog button | OK | `showincompletedataalert:285` |
| `ofloans` | Dialog/screen title | OF Loans | `onCreate:300` |
| `ofloans` | Dialog/screen title | Alert | `lambda$onCreate$18:555` |
| `ofloans` | Dialog button | Cancel | `lambda$onCreate$18:556` |
| `ofloans` | Dialog button | OK | `lambda$onCreate$18:556` |
| `ofloans` | Dialog message | Deleted Loan cannot be Recovered | `lambda$onCreate$18:556` |
| `ofloans` | Dialog button | OK | `showincompletedataalert:583` |
| `ofloans` | Toast | Updated | `onPostExecute:1262` |
| `ofloans` | Toast | Deleted | `onPostExecute:1265` |
| `ofloans` | Toast | Interest Entries found, Could not delete | `onPostExecute:1268` |
| `ofloans_interestcalculationandpayments` | Dialog/screen title | OF Loan Interest | `onCreate:148` |
| `ofloans_interestcalculationandpayments` | Dialog button | OK | `showincompletedataalert:357` |
| `ofloans_interestcalculationandpayments` | Dialog/screen title | Print Options | `lambda$onCreate$0:1436` |
| `ofloans_interestcalculationandpayments` | Toast | Sharing tools have not been installed. | `sharePdf:1547` |
| `stloan_interestcalculationdetails` | Dialog/screen title | STLoan Interest Details  | `onCreate:58` |
| `stloan_interestcalculationdetails` | Dialog/screen title | Error... | `showincompletedataalert:291` |
| `stloan_interestcalculationdetails` | Dialog button | OK | `showincompletedataalert:292` |
| `stloaninterestcalculationandpayments` | Dialog/screen title | ST Loan Interest | `onCreate:150` |
| `stloaninterestcalculationandpayments` | Dialog/screen title | Print and Share Options | `lambda$onCreate$0:1414` |
| `stloaninterestcalculationandpayments` | Toast | Error while opening the document | `createPdf:1485` |
| `stloaninterestcalculationandpayments` | Dialog button | OK | `showincompletedataalert:1636` |
| `stloans` | Dialog/screen title | Alert | `lambda$onCreate$21:625` |
| `stloans` | Dialog button | Cancel | `lambda$onCreate$21:626` |
| `stloans` | Dialog button | OK | `lambda$onCreate$21:626` |
| `stloans` | Dialog message | Deleted Loan cannot be Recovered | `lambda$onCreate$21:626` |
| `stloans` | Dialog button | OK | `showincompletedataalert:653` |
| `stloans` | Dialog/screen title | Print Options | `lambda$onCreate$9:721` |
| `stloans` | Toast | Sharing tools have not been installed. | `lambda$showprintandshareoptions$23:1114` |
| `stloans` | Toast | Updated | `onPostExecute:1808` |
| `stloans` | Toast | Deleted | `onPostExecute:1811` |
| `stloans` | Toast | Interest Entries found, Could not delete | `onPostExecute:1814` |
| `stloans` | Toast | Error while opening the document | `createPdf:2242` |

## 8.15 Finance transactions lists, party/loan ledgers and loan & deposit analysis reports

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `financewisepartners_list` | Dialog/screen title | Incompeted Data... | `showincompletedataalert:163` |
| `financewisepartners_list` | Dialog button | OK | `showincompletedataalert:164` |
| `financewisepartners_list` | Dialog message | Minimum one Partner should be selected and active. | `showincompletedataalert:164` |
| `fintrans_list` | Dialog/screen title | Parties | `onCreate:71` |
| `fintrans_list` | Dialog/screen title | ST Loans | `onCreate:76` |
| `fintrans_list` | Dialog/screen title | OF Loans | `onCreate:80` |
| `loananddepositanalyisreport` | Dialog/screen title | Analysis Reports | `onCreate:24` |
| `loanledgers_list` | Dialog/screen title | ST Loan Accounts | `onCreate:142` |
| `loanledgers_list` | Dialog/screen title | OF Loan Accounts | `onCreate:147` |
| `parties_list` | Dialog/screen title | Parties | `onCreate:69` |
| `parties_list` | Dialog/screen title | Parties | `onCreate:76` |
| `parties_list` | Dialog/screen title | Partners | `onCreate:78` |
| `parties_list` | Dialog/screen title | ST Loan Parties | `onCreate:83` |
| `parties_list` | Dialog/screen title | OF Loan Parties | `onCreate:87` |
| `parties_list` | Dialog/screen title | Party Recomendation | `onCreate:92` |
| `parties_list` | Dialog/screen title | Select Master Party | `onCreate:94` |
| `parties_list` | Dialog/screen title | Partner Recomendation | `onCreate:96` |
| `parties_list` | Dialog/screen title | ST Loan Parties | `fetchdata:340` |
| `parties_list` | Dialog/screen title | OF Loan Parties | `fetchdata:343` |
| `parties_list` | Dialog button | OK | `onPostExecute:525` |
| `parties_list` | Dialog/screen title | Error Info | `onPostExecute:525` |
| `partnerwiseloansandperformance_fragment` | Dialog/screen title | Print Options | `onOptionsItemSelected:166` |
| `partnerwiseloansandperformance_fragment` | Dialog/screen title | Communication Options | `voicecallandsmsoptions:636` |
| `partnerwiseloansandperformance_fragment` | Toast | Call Failed, please try again later. | `lambda$voicecallandsmsoptions$4:658` |
| `partnerwiseloansandperformance_fragment` | Toast | Sharing tools have not been installed. | `lambda$voicecallandsmsoptions$4:685` |
| `partnerwiseloansandperformance_fragment` | Dialog/screen title | Print Options | `showprintandshareoptions:705` |
| `partyledgers_list` | Dialog/screen title | ST Loan Accounts | `onCreate:150` |
| `partyledgers_list` | Dialog/screen title | OF Loan Accounts | `onCreate:156` |
| `partyledgers_list` | Dialog/screen title | ST Loan Accounts | `onCreate:169` |
| `partyledgers_list` | Dialog/screen title | OF Loan Accounts | `onCreate:174` |
| `partyledgers_list` | Dialog button | அழிக்கவும் | `lambda$onCreate$3:212` |
| `partyledgers_list` | Dialog button | வேண்டாம் | `lambda$onCreate$3:212` |
| `partyledgers_list` | Dialog message | இந்த கடன் மற்றும் கடன் தொடர்பான பதிவுகளை நீக்குவது ரொக்க இருப்பு மற்றும் வங்கி இருப்பு போன்றவற்றில் மாற்றங்களை ஏற்படுத்தும் என்பதை கவனத்தில் கொள்ளவும்.... அழித்த பின் திரும்ப பெற இயலாது என்பதையும் கவனத்தில் கொள்ளவும்... | `lambda$onCreate$3:212` |
| `partyledgers_list` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `lambda$onCreate$3:224` |
| `partyledgers_list` | Dialog button | OK | `onPostExecute:364` |
| `partyledgers_list` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `onPostExecute:371` |
| `partyledgers_list` | Toast | Error while opening the document | `createPdf:495` |
| `partyledgers_parameters` | Dialog/screen title | ST Loan Accounts | `onCreate:157` |
| `partyledgers_parameters` | Dialog/screen title | OF Loan Accounts | `onCreate:159` |
| `partyledgers_parameters` | Dialog/screen title | Balancesheet | `onCreate:163` |
| `partyledgers_parameters` | Dialog/screen title | Trailbalance | `onCreate:174` |
| `partyledgers_parameters` | Dialog/screen title | Profit and Loss Statement | `onCreate:177` |
| `partyledgers_parameters` | Dialog/screen title | Monthly P&L | `onCreate:190` |
| `partyledgers_parameters` | Dialog/screen title | STL Interest Pending | `onCreate:207` |
| `partyledgers_parameters` | Dialog/screen title | OFL Interest Pending | `onCreate:209` |
| `partyledgers_parameters` | Dialog/screen title | ST Loans | `onCreate:218` |
| `partyledgers_parameters` | Dialog/screen title | OF Loans | `onCreate:220` |
| `partyledgers_parameters` | Dialog/screen title | Monthwise P&L | `onCreate:227` |
| `partyledgers_parameters` | Dialog/screen title | Partnerwise Loans | `onCreate:242` |
| `partyledgers_parameters` | Dialog/screen title | OFL Interest Pending | `onCreate:244` |
| `partyledgers_parameters` | Dialog/screen title | Incomplete data... | `showincompletedataalert:473` |
| `partyledgers_parameters` | Dialog button | OK | `showincompletedataalert:474` |
| `partyledgers_parameters` | Toast | Sharing tools have not been installed. | `sharePdf:637` |
| `partyledgers_parameters` | Dialog/screen title | Print Options | `showprintandshareoptions:962` |

## 8.16 Admin masters: employees, branch employees, access control, loan methods and finance lines

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `branch_employee` | Dialog/screen title | New Employee(Branch) | `onCreate:58` |
| `branchemployeelist` | Dialog/screen title | Branch Emp-${globalvars.BranchName} | `onCreate:82` |
| `branchemployeelist` | Dialog/screen title | Info | `onPostExecute:486` |
| `branchemployeelist` | Dialog message | Permission Denied.. | `onPostExecute:487` |
| `branchemployeelist` | Dialog button | OK | `onPostExecute:488` |
| `branchemployeelist` | Dialog message | downloading, please wait ... | `onPreExecute:509` |
| `branchemployeesaccessearchlist` | Dialog/screen title | Mobile Device Access Control | `onCreate:62` |
| `branchemployeesearchlist` | Dialog/screen title | Employees | `onCreate:78` |
| `branchemployeesearchlist` | Dialog message | downloading, please wait ... | `onPreExecute:451` |
| `employeedetails_act` | Dialog/screen title | New Employee | `onCreate:65` |
| `employeedetails_act` | Dialog/screen title | Edit Employee | `onCreate:105` |
| `employeedetails_act` | Toast | Unable to delete, | `onPostExecute:328` |
| `employeesaccessearchlist` | Dialog/screen title | Mobile Device Access Control | `onCreate:63` |
| `employeesaccessearchlist` | Toast | ACCESS DENIED | `onContactSelected:201` |
| `employeesearchlist` | Dialog/screen title | Employees | `onCreate:78` |
| `employeesearchlist` | Toast | No View Permission | `onContactSelected:266` |
| `employeesearchlist` | Dialog message | downloading, please wait ... | `onPreExecute:471` |
| `financelinesaddandedit` | Dialog/screen title | Edit Line Details | `onCreate:93` |
| `loanmethods` | Dialog/screen title | Loan Methods | `onCreate:72` |
| `loanmethods` | Field error | Please enter loan method description | `lambda$onCreate$3:221` |
| `loanmethods` | Field error | Please enter loan amount | `lambda$onCreate$3:225` |
| `loanmethods` | Field error | Please enter loan amount given in hand | `lambda$onCreate$3:229` |
| `loanmethods` | Field error | Please enter Interest Deduction | `lambda$onCreate$3:233` |
| `loanmethods` | Field error | Please enter Total Amount collectable | `lambda$onCreate$3:237` |
| `loanmethods` | Field error | Please enter no of instalments | `lambda$onCreate$3:241` |
| `loanmethods` | Field error | Please enter no of instalment amount | `lambda$onCreate$3:245` |
| `loanmethods` | Field error | Please enter grace period in days | `lambda$onCreate$3:249` |
| `loanmethods` | Field error | Please enter document charges | `lambda$onCreate$3:253` |
| `loanmethods` | Field error | Please enter report identification | `lambda$onCreate$3:257` |
| `loanmethods` | Field error | Please enter collection break count | `lambda$onCreate$3:261` |
| `loanmethods` | Field error | Please enter SNo | `lambda$onCreate$3:265` |
| `loanmethods` | Field error | Please enter warning days | `lambda$onCreate$3:269` |
| `loanmethods` | Field error | Please enter Due Support Amount | `lambda$onCreate$3:273` |
| `loanmethods` | Toast | Please enter collection break count | `lambda$onCreate$3:295` |
| `loanmethods_searchlist` | Dialog/screen title | Loan Methods | `onCreate:78` |
| `loanmethods_searchlist` | Toast | Deleted | `onActivityResult:164` |
| `loanmethods_searchlist` | Dialog button | OK | `onActivityResult:173` |
| `loanmethods_searchlist` | Dialog/screen title | தங்கள் கவனத்திற்கு... | `onActivityResult:173` |
| `loanmethods_searchlist` | Dialog message | Transactions found cannot be deleted | `onActivityResult:173` |
| `loanmethods_searchlist` | Dialog button | OK | `onPostExecute:469` |
| `loanmethods_searchlist` | Dialog/screen title | Error Info | `onPostExecute:469` |
| `loanmethods_searchlist` | Dialog message | downloading, please wait ... | `onPreExecute:491` |

## 8.17 Inventory module: stock masters, items, customers/suppliers, purchase and stock in hand

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `customerdetails_act` | Dialog/screen title | Supplier | `onCreate:78` |
| `customerdetails_act` | Dialog/screen title | New Customer Details | `onCreate:122` |
| `customerdetails_act` | Dialog/screen title | New Supplier Details | `onCreate:124` |
| `customerdetails_act` | Dialog/screen title | Edit Customer Details | `onCreate:128` |
| `customerdetails_act` | Dialog/screen title | Edit Supplier Details | `onCreate:130` |
| `customerdetails_act` | Dialog button | OK | `lambda$onCreate$2:199` |
| `customerdetails_act` | Dialog/screen title | Error Info | `lambda$onCreate$2:199` |
| `customerdetails_act` | Dialog button | OK | `onPostExecute:342` |
| `customerdetails_act` | Dialog/screen title | Error Info | `onPostExecute:342` |
| `customerdetails_act` | Dialog button | சரி | `onPostExecute:353` |
| `customerdetails_act` | Dialog message | தொடர்பான பதிவுகள் இருப்பதால் நீக்க இயலாது... | `onPostExecute:353` |
| `customerdetails_act` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `onPostExecute:360` |
| `customers_list` | Dialog/screen title | Parties | `onCreate:81` |
| `customers_list` | Dialog/screen title | Customers | `onCreate:104` |
| `customers_list` | Dialog/screen title | Suppliers | `onCreate:106` |
| `inventorytransactionslist_act` | Dialog/screen title | Purchase Bills | `onCreate:110` |
| `inventorytransactionslist_act` | Dialog message | downloading, please wait ... | `onPreExecute:424` |
| `invtrans_rowdetails` | Dialog/screen title | Item Details | `onCreate:64` |
| `invtrans_rowdetails` | Dialog/screen title | Error... | `showincompletedataalert:276` |
| `invtrans_rowdetails` | Dialog button | OK | `showincompletedataalert:277` |
| `invtransactnew` | Dialog/screen title | New Sales | `onCreate:192` |
| `invtransactnew` | Dialog/screen title | New Purchase | `onCreate:194` |
| `invtransactnew` | Dialog/screen title | Edit Sales | `onCreate:205` |
| `invtransactnew` | Dialog/screen title | Edit Purchase | `onCreate:207` |
| `invtransactnew` | Dialog button | அழிக்க வேண்டாம் | `lambda$onCreate$7:347` |
| `invtransactnew` | Dialog button | அழிக்கலாம் | `lambda$onCreate$7:347` |
| `invtransactnew` | Dialog message | அழித்தப் பின் திரும்ப பெற இயலாது என்பதை கவனத்தில் கொள்ளவும்... | `lambda$onCreate$7:347` |
| `invtransactnew` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `lambda$onCreate$7:359` |
| `invtransactnew` | Dialog/screen title | Error... | `showincompletedataalert:500` |
| `invtransactnew` | Dialog button | OK | `showincompletedataalert:501` |
| `invtransactnew` | Toast | Sharing tools have not been installed. | `sharePdf:622` |
| `invtransactnew` | Dialog button | OK | `onPostExecute:1275` |
| `invtransactnew` | Dialog/screen title | Error Info | `onPostExecute:1275` |
| `itemmaster_activity` | Dialog/screen title | Item Details | `onCreate:61` |
| `itemmaster_activity` | Toast | Incomplete Data | `onClick:85` |
| `itemmaster_activity` | Dialog button | சரி | `onPostExecute:279` |
| `itemmaster_activity` | Dialog message |   பதிவுகள் இருப்பதால் நீக்க இயலாது... | `onPostExecute:279` |
| `itemmaster_activity` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `onPostExecute:285` |
| `itemmaster_list` | Dialog/screen title | Items | `onCreate:73` |
| `itemmaster_list` | Dialog message | downloading, please wait ... | `onPreExecute:233` |
| `stockUOM_act` | Dialog/screen title | Stock UOM | `onCreate:51` |
| `stockUOM_act` | Dialog button | சரி | `onPostExecute:206` |
| `stockUOM_act` | Dialog message |   பதிவுகள் இருப்பதால் நீக்க இயலாது... | `onPostExecute:206` |
| `stockUOM_act` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `onPostExecute:213` |
| `stockUOM_list` | Dialog/screen title | Stock UOM | `onCreate:62` |
| `stockUOM_list` | Dialog message | downloading, please wait ... | `onPreExecute:219` |
| `stockcat_list` | Dialog/screen title | Stock Categories | `onCreate:74` |
| `stockcat_list` | Dialog message | downloading, please wait ... | `onPreExecute:228` |
| `stockcategories` | Dialog/screen title | Stock Cat | `onCreate:57` |
| `stockcategories` | Dialog button | சரி | `onPostExecute:216` |
| `stockcategories` | Dialog message |   பதிவுகள் இருப்பதால் நீக்க இயலாது... | `onPostExecute:216` |
| `stockcategories` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `onPostExecute:223` |
| `stockgroup_act` | Dialog/screen title | Edit Stock Group | `onCreate:92` |
| `stockgroup_act` | Dialog/screen title | New Stock group | `onCreate:105` |
| `stockgroup_act` | Dialog button | சரி | `onPostExecute:223` |
| `stockgroup_act` | Dialog message |   பதிவுகள் இருப்பதால் நீக்க இயலாது... | `onPostExecute:223` |
| `stockgroup_act` | Dialog/screen title | தங்கள் கவனத்திற்கு....  | `onPostExecute:230` |
| `stockgrp_list` | Dialog/screen title | Stock Groups | `onCreate:73` |
| `stockgrp_list` | Dialog message | downloading, please wait ... | `onPreExecute:265` |
| `stockinhand_list` | Dialog/screen title | Stock in Hand | `onCreate:70` |
| `stockinhand_list` | Dialog message | downloading, please wait ... | `onPreExecute:180` |

## 8.18 Printing: collection list prints, receipts and Bluetooth printers

| Class | Kind | Text | Method:line |
|---|---|---|---|
| `BluetoothPrinterMain` | Toast | Not connected | `handleMessage:39` |
| `BluetoothPrinterMain` | Toast | Device: ${mConnectedDeviceName} Mac ID: ${BluetoothPrinterMain.mConnectedDeviceMacID} | `handleMessage:57` |
| `BluetoothPrinterMain` | Toast | ${strr} | `handleMessage:65` |
| `BluetoothPrinterMain` | Toast | ${printStatus} | `handleMessage:69` |
| `BluetoothPrinterMain` | Dialog/screen title | NGX Bluetooth Printer | `onOptionsItemSelected:178` |
| `BluetoothPrinterMain` | Dialog message | Are you sure you want to delete your preferred Bluetooth printer ? | `onOptionsItemSelected:179` |
| `BluetoothPrinterMain` | Toast | Preferred Bluetooth printer cleared | `onClick:184` |
| `BluetoothPrinterMain` | Dialog/screen title | Bluetooth Printer unpair | `onOptionsItemSelected:204` |
| `BluetoothPrinterMain` | Dialog message | Are you sure you want to unpair all Bluetooth printers ? | `onOptionsItemSelected:205` |
| `BluetoothPrinterMain` | Toast | All NGX Bluetooth printer(s) unpaired | `onClick:210` |
| `collectionlistprints` | Dialog/screen title | Print Options | `onPostExecute:6502` |
| `collectionlistprints` | Toast | Error Occured | `onPostExecute:6514` |

