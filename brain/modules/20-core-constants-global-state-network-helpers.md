# 8.20 Core constants, global state and network helpers

[← brain.md](../../brain.md) · Module 8.20 of 20 · Previous: [8.19 Shared data-access layer, server-driven settings and utilities](19-shared-data-access-layer-server-driven.md)

These twelve small classes are the app's "kernel". They hold no UI. Every screen reads its connection strings and session context from `globalvars`, its request codes and transaction-type ids from `constantvalues` and `requestcodes`, and its permission ids from `specialaccessids` and `smstemplateids`.

## Application object — `App`
| | |
|---|---|
| **Type** | `android.app.Application` subclass (singleton `App.getInstance()`) |
| **Data** | SharedPreferences **`MyPref`**: `PRINTER_SELECTION` (int, default 2), `PRINTER_TYPE` (int, default 1) |

**Functions.** `onCreate()` opens `MyPref` and stores the instance · `getPrinerSize()` / `setPrinterSize(int)` read and write the printer paper-size selection · `getPrinterType()` / `setPrinterType(int)` read and write the printer type.

> **Dead code:** the manifest's `<application>` element has no `android:name`, so Android never instantiates `App`, and no class calls `App.getInstance()`. Printer settings are handled elsewhere (see the printing chapter).

## Build constants — `BuildConfig`
`APPLICATION_ID = "net.venussolutions.venusmicrofindailyweeklycollection.clone"`, `BUILD_TYPE = "debug"`, `DEBUG = true`, `FLAVOR = "clone"`, `VERSION_CODE = 20`, `VERSION_NAME = "16.0.220125"`.

## Session state — `globalvars`
Static fields shared by all screens. They are lost when Android kills the process, because nothing re-hydrates them except `splash` and the line/branch selection screens.

| Group | Fields |
|---|---|
| Connection contexts | `MasterDBconnString/username/password`, `branchconnString/branchdbusername/branchdbpassword`, `selbranchconnString/selbranchdbusername/selbranchdbpassword`, `branchlevelReport_branchconnString/…`, `ImagesuploadconnString/…`, `driver = "net.sourceforge.jtds.jdbc.Driver"`, `ismasterdb`, `dbidentificationno`, `db` (SQLiteDatabase) |
| Constants | `SQLDBName = "venusmicrofinv005"` (local SQLite file), `projectprefrencesname = "venusmicrofinpref011"` (prefs file), `SDCorderbymethod = 2`, `PURCHASE_LEDGERID = 151`, `hideprice = false`, `issmsoff = false` |
| Identity and session | `device_id` (ANDROID_ID), `APPUserName`, `MobileUserLedgerId`, `app_userroleid`, `AdminMobileNos`, `emailidentifier`, `smsidentifier`, `expirydate`, `renewalnoticedate`, `isFullAccessAllowed`, `specialaccessrules` (`specialaccessrulesobj`), `isbranchusercontrolallowed`, `iscurrentuserhavemultibranchaccess`, `isrequestraisedformultibranchuser`, `isinventoryenabled` |
| Working context | `BranchId`, `BranchName`, `curfin` (`financemaster_data`), `finid`, `financename`, `isselectedlineisweeklyline`, `currentweekdayid`, `DefaultLoanMethodId`, `FinanceCashBookLedgerId`, `cashbookledgerid`, `bankaccountledgerid`, `collectiondate`, `todaydate`, `accyearfromdate/accyeartodate`, `cutoffdateforloanupdateblock`, `LastDownloadDatetime`, `isLastUpdateFound`, `openingbalancedownloaded`, `currentcollectionexecutivename`, `currentcollectionexecutivelederid1/2`, `selectedemployeelegerid`, `isoverallcollectionenabled`, `iscollectionamountisbeingdisplayed`, `calledfromuploadordownload` |
| Caches between screens | `SevenDaysCollectionJSON` + `sevendayscollectiondetails_list`, `allfinancedailypattiJSON`, `uploadallowedperiodJSON` + `uploadperioddateslist`, `uploadinglimitationsJSON`, `tabbedcollectionlistdataList`, `areawisecollectionlistdataList`, `acctransactionslit_datalist`, `salesdetailsforandroid_datalist`, `curanalysisreport`, `STLoans_/OFLoans_meetingreport_*` (recent download time, interest-up-to date, selected finance names) |
| Image hand-off | `imgtobepassed`, `idproofimgtobepassed`, `idproofimgiitobepassed` (Uris), `isPartyImagefromDB`, `isIdproofImagefromDB`, `isIdproofImageIIfromDB`, `istransactionimagefromdb`, `groupphotospath`, `noimagepath` |
| New-record ids | `newmcloanid`, `newpartyledgerid` |

## Constants — `constantvalues`, `requestcodes`, `specialaccessids`, `smstemplateids`
`specialaccessids` (ids 1–20) and `smstemplateids` are listed in §5.5 and §5.6. The URL constants are listed in §4.4. The full numeric tables are below. JADX wrote some values as references to unrelated constants with the same value, so they have been resolved to numbers.

### `constantvalues` — request codes, ids and transaction types

| Constant | Value |
|---|---|
| `ADDNEW_expenses_REQUESTCODE` | `1` |
| `CAPITALCOLLECTION_TRANSACTION_DESC` | `"CAPITAL RECEIPT"` |
| `CAPITALCOLLECTION_TRANSACTION_TTID` | `11` |
| `CAPITALReturn_TRANSACTION_DESC` | `"CAPITAL RETURN"` |
| `CAPITALReturn_TRANSACTION_TTID` | `12` |
| `CONTRA_TRANSACTION_DESC` | `"CONTRA"` |
| `CONTRA_TRANSACTION_TTID` | `9` |
| `DELIVERYNOTE_TRANSACTION_TTID` | `75` |
| `HANDLOANSPAYMENTTRANSACTION_DESC` | `"HAND LOANS PAYMENT"` |
| `HANDLOANSPAYMENT_TRANSACTION_TTID` | `24` |
| `HANDLOANSRECEIPTTRANSACTION_DESC` | `"HAND LOANS RECEIPT"` |
| `HANDLOANSRECEIPT_TRANSACTION_TTID` | `25` |
| `Interestdetails_AccessRequest` | `9002` |
| `JOURNAL_TRANSACTION_DESC` | `"JOURNAL"` |
| `JOURNAL_TRANSACTION_TTID` | `10` |
| `MicroLoanClosoure_TRANSACTION_TTID` | `23` |
| `MicroLoanCollection_TRANSACTION_TTID` | `22` |
| `MicroLoans_TRANSACTION_TTID` | `21` |
| `OFLoanInterest_TRANSACTION_TTID` | `6` |
| `OFLoan_TRANSACTION_TTID` | `5` |
| `PAYMENT_TRANSACTION_DESC` | `"PAYMENT"` |
| `PAYMENT_TRANSACTION_TTID` | `7` |
| `PURCHASE_TRANSACTION_DESC` | `"PURCHASE"` |
| `PURCHASE_TRANSACTION_TTID` | `116` |
| `Parties_ScreenType` | `21` |
| `Partnerdetails_AccessRequest` | `3001` |
| `Partners_ScreenType` | `22` |
| `Partydetails_AccessRequest` | `9001` |
| `RECEIPTNOTE_TRANSACTION_TTID` | `16` |
| `RECEIPT_TRANSACTION_DESC` | `"RECEIPT"` |
| `RECEIPT_TRANSACTION_TTID` | `8` |
| `SALARYADVANCEPAYMENT_TRANSACTION_DESC` | `"SALARY ADVANCE PAYMENT"` |
| `SALARYADVANCEPAYMENT_TRANSACTION_TTID` | `26` |
| `SALARYADVANCERECEIPT_TRANSACTION_DESC` | `"SALARY ADVANCE RECEIPT"` |
| `SALARYADVANCERECEIPT_TRANSACTION_TTID` | `27` |
| `SALES_TRANSACTION_TTID` | `11` |
| `STLoanInterest_TRANSACTION_TTID` | `4` |
| `STLoan_TRANSACTION_TTID` | `3` |
| `SevenDCorderbymethod_MCLoanDate` | `2` |
| `UPDATE_expenses_REQUESTCODE` | `2` |
| `accessobj_addrequest` | `7011` |
| `accessobj_deleterequest` | `7013` |
| `accessobj_editrequest` | `7012` |
| `accountgroups_bankaccountsgroupid` | `5` |
| `accountgroups_cashinhandgroupid` | `6` |
| `accountgroups_indirectexpenses` | `18` |
| `accountgroups_indirectincomes` | `19` |
| `accountsledgers_deleteledgerrequest` | `9008` |
| `accountsledgers_newledgerrequest` | `9006` |
| `accountsledgers_passedobjectname` | `"accountledger"` |
| `accountsledgers_updateledgerrequest` | `9007` |
| `accountsreports_balancesheet` | `6018` |
| `accountsreports_monthwiseprofitandlossstatement` | `6021` |
| `accountsreports_profitandlossstatement` | `6020` |
| `accountsreports_trailbalance` | `6019` |
| `accounttransactions_sourceledgerrequestcode` | `504` |
| `accounttransactions_targetledgerrequestcode` | `505` |
| `appexternaldirectoryname` | `"venusmfc"` |
| `bimonthlystyle` | `6` |
| `branchnamerequest_fromselectfinanceandbranchactivity` | `7013` |
| `branchnamerequestcode` | `9905` |
| `calendermonthstyle` | `4` |
| `calledfrom` | `"calledfrom"` |
| `collectionlistrequest_fromtodaycollectionentry` | `9907` |
| `collectionlistrequest_fromtodaycollectionlist` | `9908` |
| `collectionreportprintingmethodrequestfrom_settingsscreen` | `7021` |
| `customerobject` | `"customerobject"` |
| `customers_ScreenType` | `6` |
| `deleteaccountransaction_requestcode` | `503` |
| `documentslistcalledfrom_indcollectionentry` | `100` |
| `downloaddatacalledfrom_collectionentry` | `101` |
| `downloaddatacalledfrom_navmenu` | `102` |
| `empnamerequest` | `7010` |
| `executivehandlingmethod_offline` | `1` |
| `executivehandlingmethod_online` | `2` |
| `expensesledgerrequestcode` | `3` |
| `fetch_guardiantyperequestcode` | `9010` |
| `fetch_partylistforpartyledgersrequestcode` | `9013` |
| `fetchingaccountgroup_requestcode` | `9009` |
| `fetchingfinancelist_requestcode` | `9012` |
| `fetchingopeningbalancetype_requestcode` | `9011` |
| `fetchingstockcatlist_requestcode` | `2021` |
| `fetchingstockgrplist_requestcode` | `2023` |
| `fetchingstockuomlist_requestcode` | `2022` |
| `financelistcalledfor_collectionforaday` | `5` |
| `financelistcalledfor_dailythittam` | `3` |
| `financelistcalledfor_financeedit` | `4` |
| `financelistcalledfor_lineselection` | `1` |
| `financelistcalledfor_loanclosureentry` | `2` |
| `financenamerequest_fromselectfinanceandbranchactivity` | `7014` |
| `financialyear_requestcode` | `7022` |
| `fivedayscollectionmethodrequestfromreportsettings` | `7016` |
| `getLoan_newloanrequest` | `7107` |
| `getcashandbankledgersforiew` | `2024` |
| `getloanindex_requestcode` | `12003` |
| `getnoofloans_requestcode` | `12002` |
| `gettargetledgersforloancollection` | `11002` |
| `handloan_directentryfromhandloansreport` | `7034` |
| `handloan_ledgerstatementcalledfromhandloans` | `7035` |
| `individualloandetailsrequestfrominterestcollectionscreen` | `7029` |
| `individualloandetailsrequestfromloanaccountsreportscreen` | `7030` |
| `interestcalcualation_editrow` | `8002` |
| `interestcalcualationdetails_gettargetledger` | `8001` |
| `interestpaidforsavingsledgerid` | `112` |
| `invrowdetails_deleterequestcode` | `1004` |
| `invrowdetails_editrequestcode` | `1003` |
| `invtransactions_addnewrequestcode` | `1006` |
| `invtransactions_deleterequestcode` | `1008` |
| `invtransactions_editrequestcode` | `1007` |
| `itemmaster_addnewrequestid` | `2018` |
| `itemmaster_deleterequestid` | `2020` |
| `itemmaster_editrequestid` | `2019` |
| `itemmaster_requestcode` | `2017` |
| `language_english` | `2` |
| `language_tamil` | `1` |
| `linenamerequestcode` | `9906` |
| `loan_dctargetledgeridselectionrequest` | `7031` |
| `loan_deleteloanrequest` | `7008` |
| `loan_editloanrequest` | `7007` |
| `loan_exsistingpartyloans` | `7011` |
| `loan_getbankpaymentmethodrequest` | `7009` |
| `loan_getfinancename` | `7005` |
| `loan_getpartyname` | `7004` |
| `loan_getrecommendedpaidas` | `7003` |
| `loan_getrecommendedpartnername` | `7001` |
| `loan_getrecommendedpartyname` | `7002` |
| `loan_newloanrequest` | `7006` |
| `loan_newloanrequestfor_newparty` | `7010` |
| `loancollectionsforadmin` | `115` |
| `loanpartiesforadmin` | `113` |
| `loansforadmin` | `114` |
| `loanstatusmethodrequestfrom_settingsscreen` | `7020` |
| `masterpartyselectionrequest` | `7032` |
| `newaccountransaction_requestcode` | `501` |
| `orderbymethod_cwledgerid_asc` | `3` |
| `orderbymethod_cwledgerid_desc` | `4` |
| `orderbymethod_loandate` | `2` |
| `orderbymethod_udledgerId` | `1` |
| `parametersselection_requestcodeforloanledgers` | `7027` |
| `parametersselection_requestcodeformeetingreports` | `7013` |
| `parametersselection_requestcodeforpartnerwiseloans` | `7015` |
| `parametersselection_requestcodeforpartyledgers` | `7014` |
| `parties_deletrepartyrequest` | `9005` |
| `parties_newpartyrequest` | `9003` |
| `parties_updatepartyrequest` | `9004` |
| `partynamerequest_forpartyabstractcode` | `7037` |
| `partynamerequestfrompartytransferscreen` | `7019` |
| `partyscreen_newpartyrequestfrom_partyselectionscreen_offlinepartycreation` | `9901` |
| `partyscreen_onlineupdate` | `9903` |
| `partyscreen_partyobjectname` | `"passedparty"` |
| `partyscreen_partyshowandupdaterequestfrom_newpartyscreen_offlinepartyupdate` | `9902` |
| `partyscreen_partyshowandupdaterequestfrom_tabbedcollectionentry_offlineandonlinepartyupdate` | `9904` |
| `partyselectionfromphonecontactsrequest` | `7033` |
| `passedmenuname` | `"passedmenuname"` |
| `passedobjectname` | `"expensesentry"` |
| `product_requestcode` | `1002` |
| `savingsaccountclosure_ttid` | `28` |
| `screentype_intentpassname` | `"screentype"` |
| `sourcesubarearequestfrompartytransferscreen` | `7017` |
| `spinnerserchobjectname` | `"spinnerserchobjectname"` |
| `stockcat_addnewrequestid` | `9014` |
| `stockcat_deleterequestid` | `9016` |
| `stockcat_editrequestid` | `9015` |
| `stockcat_requestcode` | `2013` |
| `stockgrorup_addnewrequestid` | `9018` |
| `stockgrorup_deleterequestid` | `9020` |
| `stockgrorup_editrequestid` | `9019` |
| `stockgrorup_requestcode` | `9017` |
| `stockuom_addnewrequestid` | `9022` |
| `stockuom_deleterequestid` | `9024` |
| `stockuom_editrequestid` | `9023` |
| `supplier_groupid` | `11` |
| `supplierdetails_AccessRequest` | `3003` |
| `suppliername_requestcode` | `1001` |
| `suppliers_screentype` | `17` |
| `targetsubarearequestfrompartytransferscreen` | `7018` |
| `thirtydaysmonthstyle` | `5` |
| `transtypedesc_intentpassname` | `"transtypedesc"` |
| `ttid_intentpassname` | `"ttid"` |
| `updateaccountransaction_requestcode` | `502` |
| `userlevelnamerequest` | `7009` |
| `weekdayrequest_fromselectfinanceandbranchactivity` | `7015` |
| `weeklystyle` | `2` |
| `MCLoanCollectionTTId` | `22` |
| `SevenDCorderbymethod_udledgerId` | `1` |
| `documentslistcalledfrom_onlinenewloanentry` | `200` |
| `documentslistcalledfrom_offlinenewloanentry` | `300` |
| `biweekly` | `3` |
| `daily` | `1` |
| `monthly` | `4` |
| `weekly` | `2` |
| `method01` | `1` |
| `method02` | `2` |
| `method03` | `3` |
| `method04` | `4` |
| `method05` | `5` |
| `method06` | `6` |
| `method07` | `7` |
| `method08` | `8` |
| `method09` | `9` |
| `method10` | `10` |
| `localdbatcustomerplace` | `2` |
| `onlineforaspecificcustomer` | `1` |
| `playstoredemodb` | `3` |
| `loansfortheperiod` | `2` |
| `newloans` | `1` |
| `offline` | `2` |
| `online` | `1` |
| `loandate` | `2` |
| `loanno` | `1` |
| `partyno` | `3` |
| `regular` | `0` |

URL constants: `erpurlpath` = `"http://microfinapp.venussolutions.co.in/"`, `getdemodbdetailsandmakeadmindeviceurl` = `"http://microfinapp.venussolutions.co.in/getdemodbdetailsandmakeadmindevice.aspx?"`, `installpendingcustomerurl` = `"http://microfinapp.venussolutions.co.in/installpendingcustomer.aspx?"`, `microfinappinstalationsurlString` = `"http://microfinapp.venussolutions.co.in/microfinappinstalations.aspx?"`, `otpresendurl` = `"http://microfinapp.venussolutions.co.in/microfinresendotpandemail.aspx?"`, `removemfinstalationdetailsurl` = `"http://microfinapp.venussolutions.co.in/removemfinstalationdetails.aspx?"`

### `requestcodes`

| Constant | Value |
|---|---|
| `DELETELOANMETHOD_DirectREQUEST` | `303` |
| `EDITLOANMETHOD_DirectREQUEST` | `301` |
| `LIST_PARTIES_REQUESTCODE` | `24` |
| `REARRANGE_REQUESTCODE` | `23` |
| `deleteaccountledgerrequestcode` | `3` |
| `deletearearequestcode` | `3` |
| `deletecollectionrequestcode` | `102` |
| `loaclosure_loanselection` | `305` |
| `loanclosure_addrequest` | `1021` |
| `loanclosure_deleterequest` | `1024` |
| `loanclosure_editrequest` | `1011` |
| `loanclousreobjectname` | `"prevloanclosure"` |
| `loanlistcalledfrom_admin` | `1023` |
| `loanlistcalledfrom_executives` | `1022` |
| `newaccountledgerrequestcode` | `1` |
| `newarearequestcode` | `1` |
| `newloanrequestcode` | `9007` |
| `newloanrequestcode_online` | `9017` |
| `passedemployeeobject` | `"passedemployee"` |
| `passedloanobject` | `"passedloandata"` |
| `updateaccountledgerrequestcode` | `2` |
| `updatearearequestcode` | `2` |
| `updatecollectionrequestcode` | `101` |
| `weeklythittam_requestcode` | `304` |
| `sevendayscollection_requestcode_frommenu` | `9001` (written as `Integer.valueOf(constantvalues.Partydetails_AccessRequest)` by JADX) |
| `todaycollectionabstract_requestcode_frommenu` | `9002` (written as `Integer.valueOf(constantvalues.Interestdetails_AccessRequest)` by JADX) |
| `todaycollectiondetails_requestcode_fromabstract` | `9003` (written as `Integer.valueOf(constantvalues.parties_newpartyrequest)` by JADX) |
| `loanclosurerequestfrom_menu` | `9010` (written as `Integer.valueOf(constantvalues.fetch_guardiantyperequestcode)` by JADX) |
| `prevclosedloans_requestfrom_menu` | `9011` (written as `Integer.valueOf(constantvalues.fetchingopeningbalancetype_requestcode)` by JADX) |
| `prevsavingclosures_requestfrom_menu` | `9012` (written as `Integer.valueOf(constantvalues.fetchingfinancelist_requestcode)` by JADX) |
| `savingaccountclosure_requestfrom_menu` | `9013` (written as `Integer.valueOf(constantvalues.fetch_partylistforpartyledgersrequestcode)` by JADX) |
| `dailypatti_requestcode` | `9004` (written as `Integer.valueOf(constantvalues.parties_updatepartyrequest)` by JADX) |
| `periodicloans_requestcode` | `9005` (written as `Integer.valueOf(constantvalues.parties_deletrepartyrequest)` by JADX) |
| `simplethittam_requestcode` | `9006` (written as `Integer.valueOf(constantvalues.accountsledgers_newledgerrequest)` by JADX) |
| `partiestobereviewed_requestcode` | `9021` |
| `newpartyloans_requestcode` | `9022` (written as `Integer.valueOf(constantvalues.stockuom_addnewrequestid)` by JADX) |
| `dailythittam_requestcode` | `9023` (written as `Integer.valueOf(constantvalues.stockuom_editrequestid)` by JADX) |
| `attendance_requestcode` | `9024` (written as `Integer.valueOf(constantvalues.stockuom_deleterequestid)` by JADX) |
| `daywisepending_requestcode` | `9025` |
| `branchselectionraised` | `9025` |
| `syncresourcescalled` | `9026` |
| `loanstatusmethodrequest` | `9027` |
| `lineselectionraised` | `9028` |
| `todaycollectioncalledfromadmin` | `9029` |
| `editloanrequestcodefromOFFLINEdata` | `9008` (written as `Integer.valueOf(constantvalues.accountsledgers_deleteledgerrequest)` by JADX) |
| `editloanrequestcodefromONLINEdata` | `9009` (written as `Integer.valueOf(constantvalues.fetchingaccountgroup_requestcode)` by JADX) |
| `newpartyrequestcodefrom_selectionscreen` | `1001` |
| `PASSEDADDNEWEMPLOYEEREQUESTCODE` | `1` |
| `Edit_EMPLOYEEREQUESTCODE` | `2` |
| `Delete_EMPLOYEEREQUESTCODE` | `3` |

- `constantvalues.collectionbreakids`: biweekly = 3, daily = 1, monthly = 4, weekly = 2
- `constantvalues.collectionlistprintingmethods`: method01 = 1, method02 = 2, method03 = 3, method04 = 4, method05 = 5, method06 = 6, method07 = 7, method08 = 8, method09 = 9, method10 = 10
- `constantvalues.dbdistributiontypes`: localdbatcustomerplace = 2, onlineforaspecificcustomer = 1, playstoredemodb = 3
- `constantvalues.loanlistcalledfrom`: loansfortheperiod = 2, newloans = 1
- `constantvalues.newloanentrymethods`: offline = 2, online = 1
- `constantvalues.todaycollectionorderbymethods`: loandate = 2, loanno = 1, partyno = 3, regular = 0

## Network helpers — `jsondataretriver`, `RetrofitClient`, `ApiService`, `SyncCallback`, `dbdetailsobj`
* **`jsondataretriver`** is a singleton (`getInstance()`). `retrivedatabaseinfofromweb(String params)` does an HTTP GET of `constantvalues.installpendingcustomerurl + params` through `URLConnection`, reads the body, wraps it as a `JSONArray` and returns the JSON text. `preProcess()` trims to the outer `{…}`. `parseJson()` is a no-op stub. The constant `ENCODING_GB2312 = "gb2312"` is passed but ignored (the reader uses the platform charset), and the log tag `"CallerlocRetriever"` suggests the code was copied from a caller-ID project. Errors return `null`. `putSpecialNum` / `getSpecial` form an unused hashtable.
* **`dbdetailsobj`** is the DB descriptor returned by the licensing server: `CId`, `ServerName`, `DatabaseName`, `DBUserId`, `DBPassword`, `dbdistributiontypeid` (1/2/3), `dbidentificationno`, `ismasterdb`.
* **`RetrofitClient.getClient(baseUrl)`** returns a lazily built singleton `Retrofit` with a Gson converter. The base URL is fixed by the first caller.
* **`ApiService`** is a Retrofit interface: `@Multipart @POST("/fileupload.php") Call<Void> uploadDatabase(@Part MultipartBody.Part part)`.
* **`SyncCallback`** is an interface: `onSyncCompleted(boolean, boolean, String)`, `onSyncStatusUpdate(String)`. It is used by the sync and upload flows to report progress.

## Adapters, models and helpers in this module
| Class | Kind | Used by | What it holds / does |
|---|---|---|---|
| `App` | Application (unregistered, dead code) | — | Printer size and type prefs (never used) |
| `BuildConfig` | constants | — | Build metadata |
| `globalvars` | static state | all screens | Session, connection and cache fields (above) |
| `constantvalues` | constants | all screens | Request codes, TTIDs, account groups, URLs, method ids |
| `requestcodes` | constants | routers, lists | `startActivityForResult` codes, passed-object names |
| `specialaccessids` | constants | access checks | Special access ids 1–20 |
| `smstemplateids` | constants | SMS senders | SMS template ids |
| `jsondataretriver` | helper | `collectdbinfofromweb` | Licensing-server GET |
| `dbdetailsobj` | model | `collectdbinfofromweb` | DB descriptor |
| `RetrofitClient` | helper | upload flows | Retrofit singleton |
| `ApiService` | interface | upload flows | `POST /fileupload.php` |
| `SyncCallback` | interface | sync and upload flows | Progress callbacks |

---
[← brain.md](../../brain.md) · Module 8.20 of 20 · Previous: [8.19 Shared data-access layer, server-driven settings and utilities](19-shared-data-access-layer-server-driven.md)
