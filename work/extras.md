# Intent extras (screen contracts)

[← work.md](../work.md)

For every class: the extras it **receives** (`get*Extra` / `getExtras().get*`) and the extras it **sends** with each `putExtra`, with the target screen when the Intent is created in the same class. 963 put sites, 359 get sites, 163 distinct keys.

## accledgers_searchlist

Receives: `nooftransactionsfound` (Int), `passedledger` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `1` | `accountledgerdetails` | `lambda$onCreate$1:122` |
| `accledgerselected` | `accledgerselected` | `accountledgerdetails` | `onContactSelected:219` |
| `actiontype` | `2` | `accountledgerdetails` | `onContactSelected:220` |

## accountledgerdetails

Receives: `actiontype` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Ledger Group"` | `spinnersearch_list` | `lambda$onCreate$4:188` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$4:189` |
| `title` | `"Finance Name"` | `spinnersearch_list` | `lambda$onCreate$6:204` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$6:205` |
| `passedledger` | `accledgerselected` | — | `onPostExecute:348` |
| `deletededledgerid` | `accledgerselected.getLedgerId()` | — | `onPostExecute:359` |
| `nooftransactionsfound` | `noofrowsdeleted` | — | `onPostExecute:360` |

## accountsreports_parameters

Receives: `requestcode` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Financial Year"` | `tbbspl_list` | `lambda$onCreate$1:162` |
| `searchlist` | `ledgerslist` | `tbbspl_list` | `lambda$onCreate$1:163` |
| `requestcode` | `requestcode` | `tbbspl_list` | `lambda$onCreate$4:220` |
| `fromdate` | `fromdate` | `tbbspl_list` | `lambda$onCreate$4:221` |
| `todate` | `todate` | `tbbspl_list` | `lambda$onCreate$4:222` |
| `reportdate` | `reportdate` | `tbbspl_list` | `lambda$onCreate$4:223` |
| `selectecdfinanceids` | `selectecdfinanceids` | `tbbspl_list` | `lambda$onCreate$4:225` |
| `selectedfinancenames` | `selectedfinancenames` | `tbbspl_list` | `lambda$onCreate$4:226` |
| `fromdate` | `fromdate` | `monthwisepandl` | `lambda$onCreate$4:232` |
| `requestcode` | `requestcode` | `monthwisepandl` | `lambda$onCreate$4:233` |
| `todate` | `todate` | `monthwisepandl` | `lambda$onCreate$4:234` |
| `selectecdfinanceids` | `selectecdfinanceids` | `monthwisepandl` | `lambda$onCreate$4:236` |
| `selectedfinancenames` | `selectedfinancenames` | `monthwisepandl` | `lambda$onCreate$4:237` |

## accounttrans_list


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `transdata` | `transdata` | — | `lambda$onCreate$1:215` |
| `actiontype` | `501` | — | `lambda$onCreate$1:217` |
| `transdata` | `transdata` | — | `onTransactionselected:365` |
| `actiontype` | `502` | — | `onTransactionselected:367` |

## accounttransactions_act

Receives: `actiontype` (Int), `displaymember` (String), `requestfrom` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"பணம் பெறப்படும் கணக்கு"` | `accounttrans_list` | `lambda$onCreate$2:281` |
| `searchlist` | `ledgerslist` | `accounttrans_list` | `lambda$onCreate$2:282` |
| `title` | `"Finance"` | `accounttrans_list` | `lambda$onCreate$3:290` |
| `searchlist` | `ledgerslist` | `accounttrans_list` | `lambda$onCreate$3:291` |
| `title` | `"பணம் அளிக்கப்படும் கணக்கு"` | `accounttrans_list` | `lambda$onCreate$4:299` |
| `searchlist` | `ledgerslist` | `accounttrans_list` | `lambda$onCreate$4:300` |
| `passedimage` | `globalvars.groupphotospath + transactionimagefilename` | `accounttrans_list` | `lambda$onCreate$7:340` |
| `caltype` | `"app"` | `accounttrans_list` | `onOptionsItemSelected:570` |
| `title` | `"பணம் அளிக்கப்படும் கணக்கு"` | `spinnersearch_list` | `onActivityResult:924` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `onActivityResult:925` |
| `title` | `"பணம் பெறப்படும் கணக்கு"` | `spinnersearch_list` | `onActivityResult:954` |
| `searchlist` | `ledgerslist2` | `spinnersearch_list` | `onActivityResult:955` |

## allfinancedailypatti


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `pcno` | `1` | `pattidetails_loans_act` | `onContactSelected:145` |
| `pcno` | `allfinancedailypattidata.getSNo()` | `pattidetails_loans_act` | `onContactSelected:152` |
| `particulars` | `allfinancedailypattidata.getParticulars()` | `pattidetails_loans_act` | `onContactSelected:153` |

## analysisrep_financewiseabstract_fragment


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:253` |

## analysisrep_partnerwiseabstract_fragment


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:258` |

## analysisrepfin_interestratedist_fragment


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:259` |

## analysisrepfin_overallinterestratedist_fragment


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:262` |

## analysisrepfin_partnerwiseabstract_fragment


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:259` |

## analysisrepfin_partyrwiseabstract_fragment


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:252` |

## archievedloandisplay

Receives: `partyname` (String)

## branch_employee

Receives: `CallType` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `noofrowsdeleted` | `noofrowsdeleted` | — | `onPostExecute:248` |

## branchemployeelist

Receives: `noofrowsdeleted` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `requestcodes.PASSEDADDNEWEMPLOYEEREQUESTCODE` | `branch_employee` | `lambda$onCreate$1:129` |
| `CallType` | `requestcodes.Edit_EMPLOYEEREQUESTCODE` | `branch_employee` | `onContactSelected:276` |

## branchemployeesaccessearchlist

Receives: `curmaobj` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `calltype` | `7011` | `branchmobiledeviceaccescontrol_act` | `lambda$onCreate$1:110` |
| `branchaccess_obj` | `curmobj` | `branchmobiledeviceaccescontrol_act` | `lambda$onCreate$1:112` |
| `branchaccess_obj` | `branchaccess_obj` | `branchmobiledeviceaccescontrol_act` | `onContactSelected:176` |
| `calltype` | `constantvalues.accessobj_editrequest` | `branchmobiledeviceaccescontrol_act` | `onContactSelected:177` |

## branchemployeesearchlist


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `requestcodes.PASSEDADDNEWEMPLOYEEREQUESTCODE` | `employeedetails_act` | `lambda$onCreate$1:120` |
| `CallType` | `requestcodes.Edit_EMPLOYEEREQUESTCODE` | `employeedetails_act` | `onContactSelected:245` |

## branchmobiledeviceaccescontrol_act

Receives: `calltype` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Employees"` | `spinnersearch_list` | `lambda$onCreate$2:177` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$2:178` |

## cashbook


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Choose Ledger"` | `spinnersearch_list` | `lambda$setupListeners$8:288` |
| `searchlist` | `cashandbankledgerslist` | `spinnersearch_list` | `lambda$setupListeners$8:289` |

## cashreceiveandreturns

Receives: `activityheader` (String), `notes` (String), `smstemplateid` (Int), `sourceledgerid` (Int), `targetledgerid` (Int), `transtype` (String)

## closableloanssearchlistact

Receives: `finidforloanclose` (Int), `mcloanid` (Int), `weekdayid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `loancloseobj` | `loanclosureloanlist_data` | — | `onContactSelected:203` |
| `requestcode` | `1021` | — | `onContactSelected:204` |

## closablesavings_list

Receives: `finidforloanclose` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `passedaccount` | `closablesavings_data` | `closingsavingsaccount` | `onContactSelected:159` |
| `CallType` | `1` | `closingsavingsaccount` | `onContactSelected:160` |

## closed_renewedloanslist_act

Receives: `position` (Int), `selectedfinanceid` (Int), `weekdayid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `closed_renewedloans_data.getMCLoanId()` | `prevloanslist_act` | `onContactSelected:406` |
| `iscollectioneditallowed` | `"0"` | `prevloanslist_act` | `onContactSelected:407` |
| `PartyName` | `PartyName` | `prevloanslist_act` | `onContactSelected:411` |
| `Remarks` | `""` | `prevloanslist_act` | `onContactSelected:412` |
| `Tag` | `Tag` | `prevloanslist_act` | `onContactSelected:413` |
| `partyledgerid` | `closed_renewedloans_data.getPartyLedgerId()` | `prevloanslist_act` | `onthumnailclicked:423` |
| `partyname` | `closed_renewedloans_data.getLedgerName()` | `prevloanslist_act` | `onthumnailclicked:424` |

## closingsavingsaccount


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `passedaccount` | `receivedaccount` | — | `onPostExecute:246` |

## collectionareadetails_act

Receives: `displaymember` (String), `requestcode` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Collection Day"` | `spinnersearch_list` | `lambda$onCreate$4:194` |
| `searchlist` | `weekdayslist` | `spinnersearch_list` | `lambda$onCreate$4:195` |
| `passedcollectionarea` | `receivedarea` | — | `onPostExecute:307` |
| `deletedrowscount` | `deletedrowscount` | — | `onPostExecute:308` |

## collectionarealistforloanpartyandloans

Receives: `LineName` (String), `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `passedfinid` | `passedfinid` | `sevendayscollectionforselline` | `onContactSelected:96` |
| `listindex` | `adapterpos` | `sevendayscollectionforselline` | `onContactSelected:97` |

## collectionarealistforsevendayscollection

Receives: `LineName` (String), `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `passedfinid` | `passedfinid` | `sevendayscollectionforselline` | `onContactSelected:103` |
| `listindex` | `adapterpos` | `sevendayscollectionforselline` | `onContactSelected:104` |

## collectionareassearchlist

Receives: `calledfromloanmethod` (Int), `deletedrowscount` (Int), `passedcollectionarea` (Serializable), `receviedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `requestcode` | `1` | `collectionareadetails_act` | `lambda$onCreate$2:198` |
| `SubAreaNo` | `collectionareas_data.getSubAreaNo()` | `collectionareadetails_act` | `onContactSelected:373` |
| `AreaName` | `collectionareas_data.getSubAreaName()` | `collectionareadetails_act` | `onContactSelected:379` |
| `passedarea` | `collectionareas_data` | `collectionareadetails_act` | `oncollectionareaedit:386` |
| `requestcode` | `2` | `collectionareadetails_act` | `oncollectionareaedit:387` |

## collectionedit_individualcollection


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `selectedcollection` | `selectedpartcollection` | — | `handleOnBackPressed:50` |

## collectionlistprints

Receives: `method` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:578` |
| `android.intent.extra.STREAM` | `uri2` | — | `sharePdf:593` |

## collectionreportprintingmethods_searchlist


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `method` | `collectionreportprintingmethodsobj` | — | `onContactSelected:132` |

## customerdetails_act

Receives: `actiontype` (Int), `displaymember` (String), `groupid` (Int), `requestcode` (Int)

## customers_list

Receives: `groupid` (Int), `requestcode` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `ledgerdata` | `ledgerdata` | `customerdetails_act` | `lambda$onCreate$1:157` |
| `groupid` | `36` | `customerdetails_act` | `lambda$onCreate$1:159` |
| `actiontype` | `constantvalues.parties_newpartyrequest` | `customerdetails_act` | `lambda$onCreate$1:160` |
| `actiontype` | `constantvalues.parties_updatepartyrequest` | `customerdetails_act` | `onPartyselected:249` |

## dailypatti_kadanpatrulist_act


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `dailypatti_kadanpatru_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:141` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `onContactSelected:142` |
| `PartyName` | `PartyName` | `selectedloancollectiondetails` | `onContactSelected:157` |
| `Remarks` | `Remarks` | `selectedloancollectiondetails` | `onContactSelected:158` |
| `Tag` | `Tag` | `selectedloancollectiondetails` | `onContactSelected:159` |

## dailypatti_nadapunippucollectionlist_act


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `dailypatti_nadapuandnippucollection_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:140` |
| `PartyName` | `dailypatti_nadapuandnippucollection_data.getLedgerName()` | `selectedloancollectiondetails` | `onContactSelected:141` |
| `Remarks` | `""` | `selectedloancollectiondetails` | `onContactSelected:142` |
| `Tag` | `""` | `selectedloancollectiondetails` | `onContactSelected:143` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `onContactSelected:144` |

## dailypatti_nadapunippuphakkilist_act

Receives: `fromdate_patti` (Serializable), `title` (String), `todate_patti` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `dailypatti_nadapuandnippuphakki_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:191` |
| `PartyName` | `dailypatti_nadapuandnippuphakki_data.getLedgerName()` | `selectedloancollectiondetails` | `onContactSelected:192` |
| `Remarks` | `""` | `selectedloancollectiondetails` | `onContactSelected:193` |
| `Tag` | `""` | `selectedloancollectiondetails` | `onContactSelected:194` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `onContactSelected:195` |
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:622` |

## dailypatti_varakadanillapulist_act


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `dailypatti_varakadanillapu_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:142` |
| `PartyName` | `PartyName` | `selectedloancollectiondetails` | `onContactSelected:157` |
| `Remarks` | `Remarks` | `selectedloancollectiondetails` | `onContactSelected:158` |
| `Tag` | `Tag` | `selectedloancollectiondetails` | `onContactSelected:159` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `onContactSelected:160` |

## dailypattinew_act

Receives: `LineName` (String), `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `list` | `tempnadappupartieslist` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:185` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:186` |
| `fromdate_patti` | `fromdate_patti` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:187` |
| `todate_patti` | `todate_patti` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:188` |
| `list` | `tempnadappupartieslist2` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:200` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:201` |
| `fromdate_patti` | `fromdate_patti` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:202` |
| `todate_patti` | `todate_patti` | `dailypatti_nadapunippuphakkilist_act` | `onpattirow_selected:203` |
| `list` | `tempnadappupartieslist3` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:215` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:216` |
| `list` | `tempnadappupartieslist4` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:228` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:229` |
| `list` | `tempnadappupartieslist5` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:241` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:242` |
| `list` | `tempnadappupartieslist6` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:254` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:255` |
| `list` | `tempnadappupartieslist7` | `dailypatti_varakadanillapulist_act` | `onpattirow_selected:267` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_varakadanillapulist_act` | `onpattirow_selected:268` |
| `list` | `tempnadappupartieslist8` | `dailypatti_varakadanillapulist_act` | `onpattirow_selected:280` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_varakadanillapulist_act` | `onpattirow_selected:281` |
| `list` | `dailypatti_kadanpatru_dataList` | `dailypatti_kadanpatrulist_act` | `onpattirow_selected:287` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_kadanpatrulist_act` | `onpattirow_selected:288` |
| `list` | `tempnadappupartieslist9` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:300` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:301` |
| `list` | `tempnadappupartieslist10` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:313` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:314` |
| `list` | `tempnadappupartieslist11` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:326` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:327` |
| `list` | `tempnadappupartieslist12` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:339` |
| `title` | `dailypattinew_data.getParticulars()` | `dailypatti_nadapunippucollectionlist_act` | `onpattirow_selected:340` |
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:1433` |

## dailythittamforallowedlines

Receives: `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `getFileUri(f)` | — | `sharePdf:273` |
| `android.intent.extra.SUBJECT` | `"தினத்திட்டம் - " + printheadername` | — | `sharePdf:274` |

## daywisependingreport_act

Receives: `selectedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `2` | `selectedloancollectiondetails` | `onContactSelected:151` |
| `MCLoanId` | `daywisependingreport_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:152` |
| `position` | `position` | `selectedloancollectiondetails` | `onContactSelected:153` |
| `PartyName` | `daywisependingreport_data.getPartyName()` | `selectedloancollectiondetails` | `onContactSelected:154` |
| `MCLoanId` | `daywisependingreport_data.getMCLoanId()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:160` |
| `MCLoanId` | `daywisependingreport_data.getMCLoanId()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:167` |
| `PartyName` | `daywisependingreport_data.getPartyName()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:168` |
| `Remarks` | `daywisependingreport_data.getRemarks()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:169` |
| `Tag` | `Tag` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:170` |

## deviceidentity

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"App Language"` | `spinnersearch_list` | `lambda$onCreate$2:138` |
| `searchlist` | `languagelist` | `spinnersearch_list` | `lambda$onCreate$2:139` |

## directcollectionentry


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"வரவு வைக்கப்படும் கணக்கு"` | `partydetails_act` | `lambda$onCreate$5:523` |
| `searchlist` | `billprintingmethods` | `partydetails_act` | `lambda$onCreate$5:524` |
| `selloandata` | `selloandata` | `partydetails_act` | `lambda$onCreate$5:525` |
| `ledgerid` | `selloandata.getPartyLedgerId()` | `partydetails_act` | `lambda$onCreate$6:534` |
| `SubAreaNo` | `selloandata.getSubAreaNo()` | `partydetails_act` | `lambda$onCreate$6:535` |
| `CallType` | `constantvalues.partyscreen_partyshowandupdaterequestfrom_tabbedcollec…` | `partydetails_act` | `lambda$onCreate$6:536` |

## documentsphotossearchlist

Receives: `calledfor` (Int), `passeddocument` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `docslist` | `(Serializable) documentscollected_datalist` | `documentsupload` | `handleOnBackPressed:71` |
| `passeddocument` | `markedclubandgroups_data` | `documentsupload` | `onContactSelected:183` |
| `calltype` | `"edit"` | `documentsupload` | `onContactSelected:184` |

## documentsupload

Receives: `passeddocument` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `passedimage` | `softsettingsvalue.LoanRelatedDocumentsUploadPATH + passeddocument.get…` | `fullimageview` | `lambda$onCreate$2:130` |
| `passeddocument` | `passeddocument` | — | `onPostExecute:300` |
| `passeddocument` | `passeddocument` | — | `onPostExecute:400` |

## downloaddata

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"வசூலுக்கு செல்பவர்-1"` | `spinnersearch_list` | `lambda$onCreate$1:162` |
| `searchlist` | `executiveslist` | `spinnersearch_list` | `lambda$onCreate$1:163` |
| `title` | `"வசூலுக்கு செல்பவர்-2"` | `spinnersearch_list` | `lambda$onCreate$2:196` |
| `searchlist` | `executiveslist` | `spinnersearch_list` | `lambda$onCreate$2:197` |
| `title` | `"வசூலுக்கு செல்பவர்-2"` | `spinnersearch_list` | `onActivityResult:236` |
| `searchlist` | `executiveslist` | `spinnersearch_list` | `onActivityResult:237` |
| `CollectionDate` | `globalvars.collectiondate` | — | `SyncWithServer:335` |
| `currentcollectionexecutivelederid1` | `globalvars.currentcollectionexecutivelederid1` | — | `SyncWithServer:336` |
| `currentcollectionexecutivelederid2` | `globalvars.currentcollectionexecutivelederid2` | — | `SyncWithServer:337` |

## editing_collectionlist_act

Receives: `selectedcollection` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `selectedcollection` | `selectedcollection` | `editing_collectionswithfulloptions_act` | `onContactSelected:276` |
| `selectedcollectiondate` | `collectionslist_data.getCollDate()` | `editing_collectionswithfulloptions_act` | `onContactSelected:277` |
| `receviedfinanceid` | `selectedfinanceobj.getFinId()` | `editing_collectionswithfulloptions_act` | `onContactSelected:278` |
| `receivedfinancename` | `selectedfinanceobj.getFinMailingName()` | `editing_collectionswithfulloptions_act` | `onContactSelected:279` |
| `selectedcollection` | `collectionslist_data` | `editing_collections_act` | `onContactSelected:285` |
| `selectedfinanceobj` | `selectedfinanceobj` | `editing_collections_act` | `onContactSelected:286` |

## editing_collections_act

Receives: `mcloanidcalledforcollection` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `selectedcollection` | `selectedcollection` | — | `handleOnBackPressed:220` |
| `android.intent.extra.STREAM` | `pdfUri` | — | `sharePdf:549` |
| `android.intent.extra.SUBJECT` | `"Collection Details"` | — | `sharePdf:550` |
| `android.intent.extra.TEXT` | `"Collection Details"` | — | `sharePdf:551` |
| `AreaName` | `area.getSubAreaName()` | `collectionedit_individualcollection` | `onsubareanameclicked:899` |
| `SubAreaNo` | `area.getSubAreaNo()` | `collectionedit_individualcollection` | `onsubareanameclicked:900` |
| `selectedcollection` | `tabbedcollectionlistdata` | `collectionedit_individualcollection` | `onrowselected:908` |
| `selectedcollection` | `selectedcollection` | — | `lambda$onPostExecute$1:1226` |

## editing_collectionswithfulloptions_act

Receives: `DATAPASSED` (String), `receivedfinancename` (String), `receviedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `selectedcollection` | `selectedcollection` | `editing_collectionswithfulloptions_act` | `handleOnBackPressed:188` |
| `android.intent.extra.STREAM` | `pdfUri` | — | `sharePdf:657` |
| `android.intent.extra.SUBJECT` | `"Collection Details"` | — | `sharePdf:658` |
| `android.intent.extra.TEXT` | `"Collection Details"` | — | `sharePdf:659` |
| `AreaName` | `area.getSubAreaName()` | `tabbedindividualcollectionentry` | `onsubareanameclicked:1004` |
| `SubAreaNo` | `area.getSubAreaNo()` | `tabbedindividualcollectionentry` | `onsubareanameclicked:1005` |
| `SubAreaName` | `tabbedcollectionlistdata.getPartyName()` | `tabbedindividualcollectionentry` | `onrowselected:1017` |
| `SubAreaNo` | `tabbedcollectionlistdata.getSubAreaNo()` | `tabbedindividualcollectionentry` | `onrowselected:1018` |
| `MCLoanId` | `tabbedcollectionlistdata.getMCLoanId()` | `tabbedindividualcollectionentry` | `onrowselected:1019` |
| `collectionareaname` | `tabbedcollectionlistdata.getTag()` | `tabbedindividualcollectionentry` | `onrowselected:1020` |
| `Islocallyfixed` | `tabbedcollectionlistdata.getisIslocallyfixed()` | `tabbedindividualcollectionentry` | `onrowselected:1021` |
| `listindex` | `listpos` | `tabbedindividualcollectionentry` | `onrowselected:1037` |
| `selectedcollection` | `tabbedcollectionlistdata` | `collectionedit_individualcollection` | `onrowselected:1042` |
| `selectedcollection` | `selectedcollection` | `editing_collectionswithfulloptions_act` | `lambda$onPostExecute$1:2050` |

## employeedetails_act

Receives: `CallType` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Finance Name"` | `spinnersearch_list` | `lambda$onCreate$3:190` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$3:191` |
| `deletedrowscount` | `deletedrowscount` | — | `onPostExecute:333` |

## employeesaccessearchlist

Receives: `curmaobj` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `calltype` | `7011` | `mobiledeviceaccescontrol_act` | `lambda$onCreate$1:122` |
| `mobiledeviceaccescontrolobj` | `curmobj` | `mobiledeviceaccescontrol_act` | `lambda$onCreate$1:124` |
| `mobiledeviceaccescontrolobj` | `mobiledeviceaccescontrolobj` | `mobiledeviceaccescontrol_act` | `onContactSelected:196` |
| `calltype` | `constantvalues.accessobj_editrequest` | `mobiledeviceaccescontrol_act` | `onContactSelected:197` |

## employeesearchlist

Receives: `menuname` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `requestcodes.PASSEDADDNEWEMPLOYEEREQUESTCODE` | `employeedetails_act` | `lambda$onCreate$1:126` |
| `CallType` | `requestcodes.Edit_EMPLOYEEREQUESTCODE` | `employeedetails_act` | `onContactSelected:262` |

## expensesandreceipt_offlineentry

Receives: `actiontype` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `getString(R.string.ExpenseLedger)` | `spinnersearch_list` | `lambda$onCreate$3:146` |
| `title` | `getString(R.string.IncomeLedger)` | `spinnersearch_list` | `lambda$onCreate$3:148` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$3:150` |
| `title` | `"Choose Ledger"` | `spinnersearch_list` | `lambda$onCreate$4:158` |
| `searchlist` | `cashandbankledgerslist` | `spinnersearch_list` | `lambda$onCreate$4:159` |

## expensesentry_searchlist


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `1` | `expensesandreceipt_offlineentry` | `lambda$onCreate$1:104` |
| `actiontype` | `2` | `expensesandreceipt_offlineentry` | `onContactSelected:187` |

## financelinesaddandedit

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Loan Method"` | `spinnersearch_list` | `lambda$onCreate$1:146` |
| `searchlist` | `loanmethodslist` | `spinnersearch_list` | `lambda$onCreate$1:147` |
| `title` | `"Bank Ledgers"` | `spinnersearch_list` | `lambda$onCreate$2:155` |
| `searchlist` | `BankLedgerList` | `spinnersearch_list` | `lambda$onCreate$2:156` |
| `passedfinance` | `receivedfinance` | — | `onPostExecute:250` |

## financewisepartners_list

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Finance"` | `spinnersearch_list` | `lambda$onCreate$1:140` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$1:141` |

## fintrans_list


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `7010` | `stloans` | `lambda$onCreate$1:121` |
| `transdata` | `transdata` | `stloans` | `lambda$onCreate$1:122` |
| `actiontype` | `7010` | `ofloans` | `lambda$onCreate$1:129` |
| `transdata` | `transdata` | `ofloans` | `lambda$onCreate$1:130` |
| `transdata` | `transdata` | `stloans` | `onTransactionselected:191` |
| `calltype` | `2` | `stloans` | `onTransactionselected:192` |
| `actiontype` | `7011` | `stloans` | `onTransactionselected:193` |
| `transdata` | `transdata` | `ofloans` | `onTransactionselected:199` |
| `calltype` | `2` | `ofloans` | `onTransactionselected:200` |
| `actiontype` | `7011` | `ofloans` | `onTransactionselected:201` |

## fullimageview

Receives: `passedimage` (String)

## handloans_list


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `501` | `ledgerstatement_act` | `lambda$onContactSelected$1:202` |
| `transdata` | `transdata` | `ledgerstatement_act` | `lambda$onContactSelected$1:203` |
| `requestfrom` | `constantvalues.handloan_directentryfromhandloansreport` | `ledgerstatement_act` | `lambda$onContactSelected$1:204` |
| `handloandata` | `curhandloan` | `ledgerstatement_act` | `lambda$onContactSelected$1:205` |
| `android.intent.extra.STREAM` | `Uri.fromFile(file1)` | — | `sharePdf:451` |
| `selectedledgername` | `passedhandloanforentry.getLedgerName()` | `ledgerstatement_act` | `onPostExecute:841` |
| `fromdate` | `globalvars.accyearfromdate` | `ledgerstatement_act` | `onPostExecute:842` |
| `todate` | `globalvars.accyeartodate` | `ledgerstatement_act` | `onPostExecute:843` |
| `requestfrom` | `constantvalues.handloan_ledgerstatementcalledfromhandloans` | `ledgerstatement_act` | `onPostExecute:844` |
| `reportfinid` | `globalvars.finid` | `ledgerstatement_act` | `onPostExecute:845` |
| `ledgeridpassed` | `-1` | `ledgerstatement_act` | `onPostExecute:846` |
| `list` | `(Serializable) globalvars.acctransactionslit_datalist` | `ledgerstatement_act` | `onPostExecute:847` |

## ImagePickerActivity


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `output` | `getCacheImagePath(ImagePickerActivity.fileName)` | — | `onPermissionsChecked:114` |
| `output` | `getCacheImagePath(ImagePickerActivity.fileName)` | — | `onPermissionsChecked:133` |

## inventorytransactionslist_act

Receives: `transdata` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `1006` | `invtransactnew` | `lambda$onCreate$6:242` |
| `qty` | `-1` | `invtransactnew` | `lambda$onCreate$6:243` |
| `actiontype` | `1006` | `invtransactnew` | `lambda$onCreate$6:251` |
| `qty` | `1` | `invtransactnew` | `lambda$onCreate$6:252` |
| `actiontype` | `1006` | `invtransactnew` | `lambda$onCreate$6:260` |
| `qty` | `1` | `invtransactnew` | `lambda$onCreate$6:261` |
| `actiontype` | `1007` | `invtransactnew` | `onContactSelected:359` |
| `qty` | `-1` | `invtransactnew` | `onContactSelected:360` |
| `transdata` | `transdata` | `invtransactnew` | `onContactSelected:362` |
| `actiontype` | `1007` | `invtransactnew` | `onContactSelected:369` |
| `qty` | `1` | `invtransactnew` | `onContactSelected:370` |
| `transdata` | `transdata` | `invtransactnew` | `onContactSelected:372` |

## invtrans_rowdetails

Receives: `actiontype` (Int), `hideprice` (Boolean)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Select Item"` | `spinnersearch_list` | `lambda$onCreate$1:213` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$1:214` |
| `receiveddata` | `curinventoryrow` | — | `lambda$onCreate$5:242` |
| `receiveddata` | `curinventoryrow` | — | `lambda$onCreate$6:251` |

## invtransactnew

Receives: `actiontype` (Int), `receiveddata` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `nametxt.getText().toString()` | `invtrans_rowdetails` | `lambda$onCreate$2:312` |
| `searchlist` | `ledgerslist` | `invtrans_rowdetails` | `lambda$onCreate$2:313` |
| `caltype` | `"app"` | `invtrans_rowdetails` | `onOptionsItemSelected:394` |
| `actiontype` | `1003` | `invtrans_rowdetails` | `onContactSelected:490` |
| `receiveddata` | `invtransdetails_data` | `invtrans_rowdetails` | `onContactSelected:492` |
| `Currentlotno` | `0` | `invtrans_rowdetails` | `onContactSelected:493` |
| `android.intent.extra.STREAM` | `Uri.fromFile(file1)` | — | `sharePdf:616` |
| `transdata` | `curtransaction` | — | `onPostExecute:1286` |

## itemmaster_activity

Receives: `actiontype` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Category/Brand"` | `spinnersearch_list` | `onClick:97` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `onClick:98` |
| `title` | `"UOM"` | `spinnersearch_list` | `onClick:107` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `onClick:108` |
| `title` | `"Stock Group"` | `spinnersearch_list` | `onClick:117` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `onClick:118` |
| `actiontype` | `actiontype` | — | `onPostExecute:290` |
| `itemmasterobj` | `curitemmasterdata` | — | `onPostExecute:291` |
| `noofrowsdeleted` | `noofrowsdeleted` | — | `onPostExecute:292` |

## itemmaster_list

Receives: `noofrowsdeleted` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `constantvalues.itemmaster_addnewrequestid` | `itemmaster_activity` | `lambda$onCreate$1:122` |
| `itemmasterobj` | `itemmasterobj` | `itemmaster_activity` | `onContactSelected:174` |
| `actiontype` | `constantvalues.itemmaster_editrequestid` | `itemmaster_activity` | `onContactSelected:175` |

## ledgerstatement_act

Receives: `ledgeridpassed` (Int), `list` (Serializable), `reportfinid` (Int), `selectedledgername` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `resultamountCr` | `resultamountCr` | — | `handleOnBackPressed:97` |
| `resultamountDr` | `resultamountDr` | — | `handleOnBackPressed:98` |
| `receivedledgerid` | `ledgeridpassed` | — | `handleOnBackPressed:99` |
| `related_ledgerid` | `related_ledgerid` | — | `handleOnBackPressed:100` |
| `related_ledgeramount` | `related_ledgeramount` | — | `handleOnBackPressed:101` |
| `related_ledgerstatus` | `related_ledgerstatus` | — | `handleOnBackPressed:102` |
| `transdata` | `passedtransaction` | `accounttransactions_act` | `editbuttonclicked:315` |
| `finid` | `passedtransaction.getFinid()` | `accounttransactions_act` | `editbuttonclicked:317` |
| `actiontype` | `502` | `accounttransactions_act` | `editbuttonclicked:318` |

## ledgerstatmentsdateselection


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `selectedledgername` | `passeddata.getLedgerName()` | `ledgerstatement_act` | `onContactSelected:175` |
| `fromdate` | `fromdate` | `ledgerstatement_act` | `onContactSelected:176` |
| `todate` | `todate` | `ledgerstatement_act` | `onContactSelected:177` |
| `ledgeridpassed` | `passeddata.getLedgerId()` | `ledgerstatement_act` | `onContactSelected:178` |
| `reportfinid` | `globalvars.finid` | `ledgerstatement_act` | `onContactSelected:179` |

## loanclosoureentry

Receives: `IsWeeklyFinance` (Boolean), `finidforloanclose` (Int), `requestcode` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Collection Day"` | `prevloanclosure_list_act` | `lambda$onCreate$1:149` |
| `searchlist` | `weekdayslist` | `prevloanclosure_list_act` | `lambda$onCreate$1:150` |
| `finidforloanclose` | `financeid` | `closableloanssearchlistact` | `lambda$onCreate$1:155` |
| `weekdayid` | `-1` | `closableloanssearchlistact` | `lambda$onCreate$1:156` |
| `title` | `"Collection Day"` | `prevloanclosure_list_act` | `lambda$onCreate$3:183` |
| `searchlist` | `weekdayslist` | `prevloanclosure_list_act` | `lambda$onCreate$3:184` |
| `finidforloanclose` | `financeid` | `closableloanssearchlistact` | `lambda$onCreate$3:189` |
| `weekdayid` | `-1` | `closableloanssearchlistact` | `lambda$onCreate$3:190` |
| `title` | `"Collection Day"` | `prevloanclosure_list_act` | `onOptionsItemSelected:211` |
| `searchlist` | `weekdayslist` | `prevloanclosure_list_act` | `onOptionsItemSelected:212` |
| `finidforloanclose` | `financeid` | `closableloanssearchlistact` | `onOptionsItemSelected:217` |
| `caltype` | `"app"` | `closableloanssearchlistact` | `onOptionsItemSelected:218` |
| `weekdayid` | `weekdayid` | `closableloanssearchlistact` | `onOptionsItemSelected:219` |
| `finidforloanclose` | `financeid` | `closableloanssearchlistact` | `onActivityResult:266` |
| `weekdayid` | `valuemember` | `closableloanssearchlistact` | `onActivityResult:272` |
| `finidforloanclose` | `financeid` | `prevloanclosure_list_act` | `onActivityResult:279` |
| `caltype` | `"app"` | `prevloanclosure_list_act` | `onActivityResult:280` |
| `weekdayid` | `weekdayid` | `prevloanclosure_list_act` | `onActivityResult:281` |

## loanindexloanswiselist_act

Receives: `CallType` (Int), `isnewloan` (Boolean), `ledgerid` (Int), `loanamount` (Int), `loanindex` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `loanindex` | `loanindexwiseloans_data.getLoanIndex()` | `prevloanslist_act` | `onLoanselected:183` |
| `CallType` | `calledfrom` | `prevloanslist_act` | `onLoanselected:184` |
| `loanindex` | `loanindexwiseloans_data.getLoanIndex()` | `newloancreation` | `onLoanselected:196` |
| `CallType` | `calledfrom` | `newloancreation` | `onLoanselected:197` |
| `partyledgerid` | `loanindexwiseloans_data.getLedgerId()` | `prevloanslist_act` | `onprevloansbuttonclicked:214` |
| `partyname` | `curpartyobj.getLedgerName()` | `prevloanslist_act` | `onprevloansbuttonclicked:215` |
| `LoanIndex` | `loanindexwiseloans_data.getLoanIndex()` | `prevloanslist_act` | `onprevloansbuttonclicked:216` |

## loanledgers_list

Receives: `loanno` (Int), `loanordepositid` (Int), `requesttype` (Int), `selectecdfinanceids` (String), `selectedfinancenames` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:469` |

## loanmethods

Receives: `curcaltypeid` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"எழுத்துக்கூலி கணக்கிடும் முறை"` | `spinnersearch_list` | `lambda$onCreate$1:204` |
| `searchlist` | `passeddocumentchargesmethodlist` | `spinnersearch_list` | `lambda$onCreate$1:205` |
| `title` | `"வசூல் இடைவெளி"` | `spinnersearch_list` | `lambda$onCreate$2:212` |
| `searchlist` | `passedcollectionbreaklist` | `spinnersearch_list` | `lambda$onCreate$2:213` |
| `receiveddata` | `receiveddata` | — | `onPostExecute:402` |
| `norowsdeleted` | `norowsdeleted` | — | `onPostExecute:403` |

## loanmethods_searchlist

Receives: `norowsdeleted` (Int), `receiveddata` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `"new"` | `loanmethods` | `lambda$onCreate$1:126` |
| `passedmethod` | `accledgerselected` | `loanmethods` | `onContactSelected:236` |
| `curcaltypeid` | `2` | `loanmethods` | `onContactSelected:237` |

## loanslist_act

Receives: `LineName` (String), `isweeklyfinance` (Boolean), `nooftransfound` (Int), `passedfinanceid` (Int), `receivedloandata` (Serializable), `weekdayid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `constantvalues.getLoan_newloanrequest` | `selectedloancollectiondetails` | `lambda$onCreate$2:192` |
| `receviedfinanceid` | `receviedfinanceid` | `selectedloancollectiondetails` | `lambda$onCreate$2:193` |
| `calledfromloanmethod` | `1` | `selectedloancollectiondetails` | `lambda$onCreate$2:194` |
| `CallType` | `301` | `selectedloancollectiondetails` | `onContactSelected:360` |
| `passednewloan` | `newloanslist_data` | `selectedloancollectiondetails` | `onContactSelected:361` |
| `CallType` | `301` | `selectedloancollectiondetails` | `lambda$onContactSelected$9:383` |
| `passednewloan` | `newloanslist_data` | `selectedloancollectiondetails` | `lambda$onContactSelected$9:384` |
| `CallType` | `301` | `directcollectionentry` | `lambda$onContactSelected$9:390` |
| `passednewloan` | `newloanslist_data` | `directcollectionentry` | `lambda$onContactSelected$9:391` |
| `MCLoanId` | `newloanslist_data.getMCLoanId()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:404` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:405` |
| `MCLoanId` | `newloanslist_data.getMCLoanId()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:412` |
| `PartyName` | `newloanslist_data.getPartyName()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:413` |
| `Remarks` | `newloanslist_data.getRemarks()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:414` |
| `Tag` | `Tag` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:415` |

## maps_partydisplay

Receives: `PLID` (Int), `areaname` (String)

## meetingreport

Receives: `requestcode` (Int), `selectecdfinanceids` (String), `selectedfinancenames` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `requestcode` | `7013` | — | `onOptionsItemSelected:341` |
| `sms_body` | `message` | — | `lambda$voicecallandsmsoptions$7:1323` |
| `ledgerdata` | `tblLedgers_newdata` | `stloaninterestcalculationandpayments` | `lambda$voicecallandsmsoptions$7:1363` |
| `ledgerdata` | `tblLedgers_newdata` | `ofloans_interestcalculationandpayments` | `lambda$voicecallandsmsoptions$7:1368` |

## mobiledeviceaccescontrol_act

Receives: `calltype` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"User Roles"` | `spinnersearch_list` | `lambda$onCreate$2:186` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$2:187` |
| `title` | `"User Roles"` | `spinnersearch_list` | `lambda$onCreate$3:195` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$3:196` |
| `curmaobj` | `curmaobj` | — | `onPostExecute:481` |

## monthlypandl_fragment

Receives: `displaymember` (String), `receivedledgerid` (Int), `resultamountCr` (Int), `resultamountDr` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `Uri.fromFile(file1)` | — | `sharePdf:432` |
| `ledgername` | `SelectedLedgerName` | `ledgerstatement_act` | `onPostExecute:709` |
| `fromdate` | `fromdate` | `ledgerstatement_act` | `onPostExecute:710` |
| `todate` | `todate` | `ledgerstatement_act` | `onPostExecute:711` |
| `receivedledgerid` | `SelectedLedgerId` | `ledgerstatement_act` | `onPostExecute:712` |
| `reportfinid` | `globalvars.finid` | `ledgerstatement_act` | `onPostExecute:713` |

## monthwisepandl

Receives: `selectecdfinanceids` (String), `selectedfinancenames` (String)

## MyService_updatecollection


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `DATAPASSED` | `DateFormat.format("h:mm:ss a", curdatetime)` | — | `onPostExecute:223` |
| `DATAPASSED` | `ErrorText` | — | `onPostExecute:232` |

## navigationdrawactivity

Receives: `issyncneeded` (Boolean)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `requestcode` | `constantvalues.accountsreports_trailbalance` | `accountsreports_parameters` | `onNavigationItemSelected:946` |
| `requestcode` | `constantvalues.accountsreports_balancesheet` | `accountsreports_parameters` | `onNavigationItemSelected:952` |
| `requestcode` | `constantvalues.accountsreports_profitandlossstatement` | `accountsreports_parameters` | `onNavigationItemSelected:958` |
| `requestcode` | `114` | `selectbranchandfinance` | `onNavigationItemSelected:974` |
| `requestcode` | `115` | `selectbranchandfinance` | `onNavigationItemSelected:979` |
| `groupid` | `11` | `customers_list` | `onNavigationItemSelected:1020` |
| `requestcode` | `constantvalues.supplierdetails_AccessRequest` | `customers_list` | `onNavigationItemSelected:1021` |
| `actiontype` | `1006` | `invtransactnew` | `onNavigationItemSelected:1030` |
| `listheading` | `"Purchase Notes"` | `invtransactnew` | `onNavigationItemSelected:1031` |
| `requestcode` | `requestcodes.todaycollectioncalledfromadmin` | `todaycollectionlist` | `onNavigationItemSelected:1045` |
| `ctid` | `7` | `todaycollectionlist` | `onNavigationItemSelected:1046` |
| `financeid` | `globalvars.curfin.getFinId()` | `todaycollectionlist` | `onNavigationItemSelected:1047` |
| `requestcode` | `requestcodes.todaycollectiondetails_requestcode_fromabstract` | `todaycollectionlist` | `onNavigationItemSelected:1048` |
| `collectiontype` | `"All Parties"` | `todaycollectionlist` | `onNavigationItemSelected:1049` |
| `collectiondate` | `Date.valueOf(AppSettings.getString("CollectionDate", String.valueOf(L…` | `todaycollectionlist` | `onNavigationItemSelected:1050` |
| `requestcode` | `requestcodes.dailypatti_requestcode` | `selectbranchandfinance` | `onNavigationItemSelected:1056` |
| `requestcode` | `requestcodes.daywisepending_requestcode` | `varathittam` | `onNavigationItemSelected:1062` |
| `requestcode` | `304` | `selectbranchandfinance` | `onNavigationItemSelected:1068` |
| `requestcode` | `requestcodes.partiestobereviewed_requestcode` | `selectbranchandfinance` | `onNavigationItemSelected:1074` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1082` |
| `transdata` | `transdata` | `accounttransactions_act` | `onNavigationItemSelected:1083` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1091` |
| `transdata` | `transdata2` | `accounttransactions_act` | `onNavigationItemSelected:1092` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1100` |
| `transdata` | `transdata3` | `accounttransactions_act` | `onNavigationItemSelected:1101` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1109` |
| `transdata` | `transdata4` | `accounttransactions_act` | `onNavigationItemSelected:1110` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1118` |
| `transdata` | `transdata5` | `accounttransactions_act` | `onNavigationItemSelected:1119` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1127` |
| `transdata` | `transdata6` | `accounttransactions_act` | `onNavigationItemSelected:1128` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1136` |
| `transdata` | `transdata7` | `accounttransactions_act` | `onNavigationItemSelected:1137` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1145` |
| `transdata` | `transdata8` | `accounttransactions_act` | `onNavigationItemSelected:1146` |
| `requestcode` | `requestcodes.newpartyloans_requestcode` | `selectbranchandfinance` | `onNavigationItemSelected:1160` |
| `requestcode` | `requestcodes.daywisepending_requestcode` | `selectbranchandfinance` | `onNavigationItemSelected:1166` |
| `passedfinanceid` | `globalvars.finid` | `loanslist_act` | `onNavigationItemSelected:1179` |
| `LineName` | `globalvars.financename` | `loanslist_act` | `onNavigationItemSelected:1180` |
| `requestcode` | `requestcodes.simplethittam_requestcode` | `selectbranchandfinance` | `onNavigationItemSelected:1230` |
| `requestcode` | `requestcodes.attendance_requestcode` | `selectbranchandfinance` | `onNavigationItemSelected:1236` |
| `cashbooktype` | `"staffcashbook"` | `cashbook` | `onNavigationItemSelected:1245` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1253` |
| `transdata` | `transdata9` | `accounttransactions_act` | `onNavigationItemSelected:1254` |
| `actiontype` | `501` | `accounttransactions_act` | `onNavigationItemSelected:1262` |
| `transdata` | `transdata10` | `accounttransactions_act` | `onNavigationItemSelected:1263` |
| `requestcode` | `requestcodes.loanclosurerequestfrom_menu` | `selectbranchandfinance` | `onNavigationItemSelected:1295` |
| `requestcode` | `requestcodes.prevsavingclosures_requestfrom_menu` | `selectbranchandfinance` | `onNavigationItemSelected:1299` |
| `requestcode` | `requestcodes.savingaccountclosure_requestfrom_menu` | `selectbranchandfinance` | `onNavigationItemSelected:1303` |
| `finidforloanclose` | `globalvars.finid` | `selectbranchandfinance` | `onNavigationItemSelected:1304` |
| `weekdayid` | `-1` | `selectbranchandfinance` | `onNavigationItemSelected:1305` |
| `passedfinanceid` | `globalvars.finid` | `todaycollectionabstractlist` | `onNavigationItemSelected:1312` |
| `LineName` | `globalvars.curfin.getFinName()` | `todaycollectionabstractlist` | `onNavigationItemSelected:1313` |
| `sqlDate` | `globalvars.collectiondate` | `todaycollectionabstractlist` | `onNavigationItemSelected:1316` |
| `calledfor` | `"sevendayscollection"` | `selectbranchandfinance` | `onNavigationItemSelected:1322` |
| `requestcode` | `requestcodes.sevendayscollection_requestcode_frommenu` | `selectbranchandfinance` | `onNavigationItemSelected:1323` |
| `requestcode` | `113` | `selectbranchandfinance` | `onNavigationItemSelected:1329` |
| `menuname` | `"nav_loanparties"` | `selectbranchandfinance` | `onNavigationItemSelected:1330` |
| `menuname` | `"nav_employee"` | `employeesearchlist` | `onNavigationItemSelected:1334` |
| `requestcode` | `constantvalues.accountsreports_monthwiseprofitandlossstatement` | `accountsreports_parameters` | `onNavigationItemSelected:1351` |
| `sourceledgerid` | `globalvars.FinanceCashBookLedgerId` | `cashreceiveandreturns` | `onNavigationItemSelected:1357` |
| `targetledgerid` | `globalvars.cashbookledgerid` | `cashreceiveandreturns` | `onNavigationItemSelected:1358` |
| `transtype` | `getString(R.string.cashreceived)` | `cashreceiveandreturns` | `onNavigationItemSelected:1359` |
| `activityheader` | `getString(R.string.amountreceived)` | `cashreceiveandreturns` | `onNavigationItemSelected:1360` |
| `notes` | `getResources().getString(R.string.amountreceiptsnotes)` | `cashreceiveandreturns` | `onNavigationItemSelected:1361` |
| `smstemplateid` | `14` | `cashreceiveandreturns` | `onNavigationItemSelected:1363` |
| `smstemplateid` | `12` | `cashreceiveandreturns` | `onNavigationItemSelected:1365` |
| `sourceledgerid` | `globalvars.cashbookledgerid` | `cashreceiveandreturns` | `onNavigationItemSelected:1372` |
| `targetledgerid` | `globalvars.FinanceCashBookLedgerId` | `cashreceiveandreturns` | `onNavigationItemSelected:1373` |
| `transtype` | `getString(R.string.CashPaid)` | `cashreceiveandreturns` | `onNavigationItemSelected:1374` |
| `activityheader` | `getString(R.string.AmountPaid)` | `cashreceiveandreturns` | `onNavigationItemSelected:1375` |
| `notes` | `getResources().getString(R.string.amountreturnrnotes)` | `cashreceiveandreturns` | `onNavigationItemSelected:1376` |
| `smstemplateid` | `15` | `cashreceiveandreturns` | `onNavigationItemSelected:1378` |
| `smstemplateid` | `13` | `cashreceiveandreturns` | `onNavigationItemSelected:1380` |
| `requestcode` | `constantvalues.Interestdetails_AccessRequest` | `parties_list` | `onNavigationItemSelected:1411` |
| `requestcode` | `constantvalues.Interestdetails_AccessRequest` | `parties_list` | `onNavigationItemSelected:1419` |
| `requestcode` | `7014` | `partyledgers_parameters` | `onNavigationItemSelected:1426` |
| `requestcode` | `7014` | `partyledgers_parameters` | `onNavigationItemSelected:1433` |
| `requestcode` | `constantvalues.Partnerdetails_AccessRequest` | `parties_list` | `onNavigationItemSelected:1440` |
| `menuname` | `"nav_partnerdetails"` | `parties_list` | `onNavigationItemSelected:1441` |
| `requestcode` | `constantvalues.parametersselection_requestcodeforloanledgers` | `partyledgers_parameters` | `onNavigationItemSelected:1474` |
| `requestcode` | `constantvalues.parametersselection_requestcodeforloanledgers` | `partyledgers_parameters` | `onNavigationItemSelected:1481` |
| `listindex` | `listpos` | `syncresources` | `onContactSelected:1556` |

## newloancreation

Receives: `CallType` (Int), `displaymember` (String), `docslist` (Serializable), `loanindex` (Int), `passednewloan` (Serializable), `receiveddata` (Serializable), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `dueslist_dataList` | `dueslist_dataList` | `documentsphotossearchlist` | `lambda$onCreate$1:692` |
| `actiontype` | `1003` | `documentsphotossearchlist` | `lambda$onCreate$3:713` |
| `receiveddata` | `emptyrow` | `documentsphotossearchlist` | `lambda$onCreate$3:715` |
| `Currentlotno` | `0` | `documentsphotossearchlist` | `lambda$onCreate$3:716` |
| `hideprice` | `false` | `documentsphotossearchlist` | `lambda$onCreate$3:717` |
| `title` | `"Loan Method"` | `documentsphotossearchlist` | `lambda$onCreate$4:725` |
| `searchlist` | `loanmethodslist` | `documentsphotossearchlist` | `lambda$onCreate$4:726` |
| `title` | `"Choose Ledger"` | `documentsphotossearchlist` | `lambda$onCreate$5:734` |
| `searchlist` | `billprintingmethods` | `documentsphotossearchlist` | `lambda$onCreate$5:735` |
| `title` | `"Loan Index"` | `documentsphotossearchlist` | `lambda$onCreate$6:744` |
| `searchlist` | `billprintingmethods` | `documentsphotossearchlist` | `lambda$onCreate$6:745` |
| `title` | `"Collection Day"` | `documentsphotossearchlist` | `lambda$onCreate$7:755` |
| `searchlist` | `weekdayslist` | `documentsphotossearchlist` | `lambda$onCreate$7:756` |
| `title` | `R.string.duedatelabel` | `spinnersearch_list` | `lambda$onCreate$7:763` |
| `searchlist` | `weekdayslist2` | `spinnersearch_list` | `lambda$onCreate$7:764` |
| `isdeleted` | `false` | — | `SaveLoan:1245` |
| `receivedloandata` | `receivedloandata` | — | `SaveLoan:1246` |
| `isdeleted` | `false` | — | `SaveLoan:1321` |
| `isnewloan` | `true` | — | `SaveLoan:1322` |
| `loanindex` | `receivedloandata.getLoanindex()` | — | `SaveLoan:1323` |
| `loanamount` | `receivedloandata.getMCLoanAmountCollectable()` | — | `SaveLoan:1324` |
| `isdeleted` | `true` | — | `deleteloan:1365` |
| `actiontype` | `1003` | `documentsphotossearchlist` | `onContactSelected:1373` |
| `receiveddata` | `invtransdetails_data` | `documentsphotossearchlist` | `onContactSelected:1375` |
| `Currentlotno` | `0` | `documentsphotossearchlist` | `onContactSelected:1376` |
| `hideprice` | `false` | `documentsphotossearchlist` | `onContactSelected:1377` |
| `docslist` | `(Serializable) documentscollected_datalist` | `documentsphotossearchlist` | `onPostExecute:1435` |
| `receivedloandata` | `receivedloandata` | — | `onPostExecute:1643` |
| `nooftransfound` | `nofrowsdeleted` | — | `onPostExecute:1644` |

## newloanslist_act


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `requestcodes.editloanrequestcodefromOFFLINEdata` | `newloancreation` | `onContactSelected:276` |

## newpartieslist_act


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `constantvalues.partyscreen_partyshowandupdaterequestfrom_newpartyscre…` | `partydetails_act` | `onContactSelected:186` |
| `SubAreaNo` | `newpartieslist_data.getSubAreaNo()` | `partydetails_act` | `onContactSelected:187` |
| `AreaName` | `newpartieslist_data.getSubAreaName()` | `partydetails_act` | `onContactSelected:188` |

## newpartiesnewloanslist_act

Receives: `financeid` (Int), `weekdayid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `2` | `selectedloancollectiondetails` | `onContactSelected:248` |
| `MCLoanId` | `newloanslist_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:249` |
| `position` | `position` | `selectedloancollectiondetails` | `onContactSelected:250` |
| `PartyName` | `newloanslist_data.getPartyName()` | `selectedloancollectiondetails` | `onContactSelected:251` |
| `MCLoanId` | `newloanslist_data.getMCLoanId()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:257` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:258` |
| `MCLoanId` | `newloanslist_data.getMCLoanId()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:265` |
| `PartyName` | `newloanslist_data.getPartyName()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:266` |
| `Remarks` | `newloanslist_data.getRemarks()` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:267` |
| `Tag` | `Tag` | `selectedloancollectiondetails` | `oncollectiondetailslinkclicked:268` |

## ofloan_interestcalculationdetails

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Paid in/as"` | `spinnersearch_list` | `lambda$onCreate$2:240` |
| `searchlist` | `ledgerslist2` | `spinnersearch_list` | `lambda$onCreate$2:241` |
| `receivedinterestrow` | `receivedinterestrow` | — | `lambda$onCreate$3:257` |

## ofloans

Receives: `actiontype` (Int), `displaymember` (String), `secondvaluemember` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Party Recomendation"` | `spinnersearch_list` | `lambda$onCreate$0:334` |
| `requestcode` | `constantvalues.loan_getrecommendedpartnername` | `spinnersearch_list` | `lambda$onCreate$0:336` |
| `finid` | `tblOFLoans_newdata.getFinId()` | `spinnersearch_list` | `lambda$onCreate$0:337` |
| `title` | `"Received in/as"` | `spinnersearch_list` | `lambda$onCreate$1:345` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$1:346` |
| `title` | `"Finance"` | `spinnersearch_list` | `lambda$onCreate$2:355` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$2:356` |
| `title` | `"Select Party"` | `spinnersearch_list` | `lambda$onCreate$3:364` |
| `requestcode` | `constantvalues.loan_getpartyname` | `spinnersearch_list` | `lambda$onCreate$3:366` |
| `finid` | `1` | `spinnersearch_list` | `lambda$onCreate$3:367` |
| `title` | `"Select Party"` | `spinnersearch_list` | `lambda$onCreate$4:376` |
| `requestcode` | `constantvalues.loan_getpartyname` | `spinnersearch_list` | `lambda$onCreate$4:378` |
| `finid` | `1` | `spinnersearch_list` | `lambda$onCreate$4:379` |
| `title` | `"Select Party"` | `spinnersearch_list` | `lambda$onCreate$5:388` |
| `requestcode` | `constantvalues.loan_getpartyname` | `spinnersearch_list` | `lambda$onCreate$5:390` |
| `finid` | `1` | `spinnersearch_list` | `lambda$onCreate$5:391` |
| `title` | `"Transaction Method"` | `spinnersearch_list` | `lambda$onCreate$7:406` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$7:407` |
| `title` | `"Finance"` | `spinnersearch_list` | `onActivityResult:844` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `onActivityResult:845` |
| `title` | `"Party Recomendation"` | `parties_list` | `onActivityResult:864` |
| `requestcode` | `constantvalues.loan_getrecommendedpartnername` | `parties_list` | `onActivityResult:866` |
| `finid` | `tblOFLoans_newdata.getFinId()` | `parties_list` | `onActivityResult:867` |
| `title` | `"Transaction Method"` | `spinnersearch_list` | `onActivityResult:899` |
| `searchlist` | `ledgerslist1` | `spinnersearch_list` | `onActivityResult:900` |

## ofloans_interestcalculationandpayments

Receives: `receivedinterestrow` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `loanordepositid` | `indvidualloanordepositstatus_data.getSTLoanId()` | `ofloan_interestcalculationdetails` | `onloanselected:414` |
| `partyledgerid` | `indvidualloanordepositstatus_data.getPartyLedgerId()` | `ofloan_interestcalculationdetails` | `onloanselected:415` |
| `requesttype` | `constantvalues.individualloandetailsrequestfrominterestcollectionscre…` | `ofloan_interestcalculationdetails` | `onloanselected:416` |
| `selectedfinancenames` | `indvidualloanordepositstatus_data.getFin()` | `ofloan_interestcalculationdetails` | `onloanselected:417` |
| `partyname` | `receivedparty.getLedgerName()` | `ofloan_interestcalculationdetails` | `onloanselected:418` |
| `loanno` | `indvidualloanordepositstatus_data.getNo()` | `ofloan_interestcalculationdetails` | `onloanselected:419` |
| `intrestcalculationrow` | `ofloans_interestcalculationlist_data` | `ofloan_interestcalculationdetails` | `onInterestCalculationRowSelected:431` |
| `android.intent.extra.STREAM` | `Uri.fromFile(file1)` | — | `sharePdf:1541` |

## otpverificationact

Receives: `appinsid` (Int), `otpValueactual` (String), `otpValueopen` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `issyncneeded` | `true` | `syncresources` | `onPostExecute:256` |

## parties_list

Receives: `finid` (Int), `menuname` (String), `requestcode` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `ledgerdata` | `ledgerdata` | `partyandpartnerdetails` | `lambda$onCreate$0:145` |
| `actiontype` | `constantvalues.parties_newpartyrequest` | `partyandpartnerdetails` | `lambda$onCreate$0:147` |
| `ledgerdata` | `ledgerdata` | `partyandpartnerdetails` | `onPartyselected:229` |
| `actiontype` | `constantvalues.parties_updatepartyrequest` | `partyandpartnerdetails` | `onPartyselected:231` |
| `ledgerdata` | `ledgerdata` | `partyandpartnerdetails` | `onPartyselected:238` |
| `actiontype` | `constantvalues.parties_updatepartyrequest` | `partyandpartnerdetails` | `onPartyselected:239` |
| `menuname` | `menuname` | `partyandpartnerdetails` | `onPartyselected:241` |
| `ledgerdata` | `ledgerdata` | `stloaninterestcalculationandpayments` | `onPartyselected:249` |
| `ledgerdata` | `ledgerdata` | `ofloans_interestcalculationandpayments` | `onPartyselected:256` |
| `displaymember` | `ledgerdata.getLedgerName()` | — | `onPartyselected:263` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:264` |
| `displaymember` | `ledgerdata.getLedgerName()` | — | `onPartyselected:271` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:272` |
| `ledgerdata` | `ledgerdata` | — | `onPartyselected:273` |
| `displaymember` | `ledgerdata.getLedgerName()` | — | `onPartyselected:280` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:281` |
| `displaymember` | `ledgerdata.getLedgerName()` | — | `onPartyselected:288` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:289` |
| `displaymember` | `ledgerdata.getLedgerName()` | — | `onPartyselected:296` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:297` |
| `receivedparty` | `ledgerdata` | — | `onPartyselected:298` |
| `displaymember` | `ledgerdata.getLedgerName()` | — | `onPartyselected:305` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:306` |
| `displaymember` | `ledgerdata.getLedgerName() + ", " + ledgerdata.getAddLine1()` | — | `onPartyselected:313` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:314` |
| `displaymember` | `ledgerdata.getLedgerName() + ", " + ledgerdata.getAddLine1()` | — | `onPartyselected:321` |
| `valuemember` | `ledgerdata.getLedgerId()` | — | `onPartyselected:322` |

## partnerwiseloansandperformance_fragment


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:220` |
| `sms_body` | `message` | — | `lambda$voicecallandsmsoptions$4:664` |

## partnerwiseloansandperformancehome

Receives: `partnerledgerid` (Int), `selectecdfinanceids` (String), `selectedfinancenames` (String)

## partyandpartnerdetails

Receives: `actiontype` (Int), `displaymember` (String), `menuname` (String), `seconddisplaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Guardian Type"` | `parties_list` | `lambda$onCreate$2:260` |
| `searchlist` | `ledgerslist` | `parties_list` | `lambda$onCreate$2:261` |
| `title` | `"Select Party"` | `parties_list` | `lambda$onCreate$3:268` |
| `requestcode` | `constantvalues.masterpartyselectionrequest` | `parties_list` | `lambda$onCreate$3:270` |
| `finid` | `1` | `parties_list` | `lambda$onCreate$3:271` |
| `receivedparty` | `receivedparty` | — | `onPostExecute:584` |

## partydetails_act

Receives: `AreaName` (String), `CallType` (Int), `SubAreaNo` (Int), `ledgerid` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"No of Loans Allowed"` | `newloancreation` | `lambda$onCreate$1:427` |
| `searchlist` | `billprintingmethods` | `newloancreation` | `lambda$onCreate$1:428` |
| `passedimage` | `softsettingsvalue.idproofimagepath + receivedparty.getIdProofFileName…` | `newloancreation` | `lambda$onCreate$4:450` |
| `passedimage` | `softsettingsvalue.idproofimagepath + receivedparty.getIdProofImageIIf…` | `newloancreation` | `lambda$onCreate$5:457` |
| `passedimage` | `softsettingsvalue.partiesimagepath + receivedparty.getImagefilename()` | `newloancreation` | `lambda$onCreate$6:464` |
| `rowsdeleted` | `rowsdeleted` | `newloancreation` | `onPostExecute:1877` |
| `CallType` | `CurPartyMode` | `newloancreation` | `returntobackornextscreen:1900` |
| `loanindex` | `1` | `newloancreation` | `returntobackornextscreen:1901` |

## partydetails_searchlist

Receives: `LineName` (String), `nooftransfound` (Int), `passedfinanceid` (Int), `rowsdeleted` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CallType` | `constantvalues.partyscreen_onlineupdate` | `partydetails_act` | `onContactSelected:263` |
| `SubAreaNo` | `partyselected.getSubAreaNo()` | `partydetails_act` | `onContactSelected:264` |
| `AreaName` | `partyselected.getSubAreaName()` | `partydetails_act` | `onContactSelected:265` |

## partyledgers_list

Receives: `loanno` (Int), `loanordepositid` (Int), `partyledgerid` (Int), `requesttype` (Int), `selectecdfinanceids` (String), `selectedfinancenames` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:531` |

## partyledgers_parameters

Receives: `displaymember` (String), `requestcode` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Select Partner"` | `partyledgers_list` | `lambda$onCreate$1:284` |
| `title` | `"Select Party"` | `partyledgers_list` | `lambda$onCreate$1:287` |
| `requestcode` | `requestcode` | `partyledgers_list` | `lambda$onCreate$1:290` |
| `finid` | `1` | `partyledgers_list` | `lambda$onCreate$1:291` |
| `partyledgerid` | `partyledgerid` | `partyledgers_list` | `lambda$onCreate$6:382` |
| `requesttype` | `constantvalues.individualloandetailsrequestfromloanaccountsreportscre…` | `partyledgers_list` | `lambda$onCreate$6:383` |
| `fromdate` | `fromdate` | `partyledgers_list` | `lambda$onCreate$6:384` |
| `todate` | `todate` | `partyledgers_list` | `lambda$onCreate$6:385` |
| `requestcode` | `constantvalues.fetch_partylistforpartyledgersrequestcode` | `partyledgers_list` | `lambda$onCreate$6:387` |
| `selectecdfinanceids` | `selectecdfinanceids` | `partyledgers_list` | `lambda$onCreate$6:388` |
| `selectedfinancenames` | `selectedfinancenames` | `partyledgers_list` | `lambda$onCreate$6:389` |
| `fromdate` | `fromdate` | `meetingreport` | `lambda$onCreate$6:395` |
| `requestcode` | `requestcode` | `meetingreport` | `lambda$onCreate$6:396` |
| `todate` | `todate` | `meetingreport` | `lambda$onCreate$6:397` |
| `selectecdfinanceids` | `selectecdfinanceids` | `meetingreport` | `lambda$onCreate$6:399` |
| `selectedfinancenames` | `selectedfinancenames` | `meetingreport` | `lambda$onCreate$6:400` |
| `partnerledgerid` | `partyledgerid` | `partnerwiseloansandperformancehome` | `lambda$onCreate$6:415` |
| `todate` | `todate` | `partnerwiseloansandperformancehome` | `lambda$onCreate$6:416` |
| `selectecdfinanceids` | `selectecdfinanceids` | `partnerwiseloansandperformancehome` | `lambda$onCreate$6:418` |
| `selectedfinancenames` | `selectedfinancenames` | `partnerwiseloansandperformancehome` | `lambda$onCreate$6:419` |
| `requestcode` | `requestcode` | `partnerwiseloansandperformancehome` | `lambda$onCreate$6:420` |
| `fromdate` | `fromdate` | `loanledgers_list` | `lambda$onCreate$6:428` |
| `requestcode` | `requestcode` | `loanledgers_list` | `lambda$onCreate$6:429` |
| `todate` | `todate` | `loanledgers_list` | `lambda$onCreate$6:430` |
| `selectecdfinanceids` | `selectecdfinanceids` | `loanledgers_list` | `lambda$onCreate$6:432` |
| `selectedfinancenames` | `selectedfinancenames` | `loanledgers_list` | `lambda$onCreate$6:433` |
| `fromdate` | `fromdate` | `tbbspl_list` | `lambda$onCreate$6:439` |
| `requestcode` | `requestcode` | `tbbspl_list` | `lambda$onCreate$6:440` |
| `todate` | `interestcalculateuptodate` | `tbbspl_list` | `lambda$onCreate$6:441` |
| `selectecdfinanceids` | `selectecdfinanceids` | `tbbspl_list` | `lambda$onCreate$6:443` |
| `selectedfinancenames` | `selectedfinancenames` | `tbbspl_list` | `lambda$onCreate$6:444` |
| `fromdate` | `fromdate` | `tbbspl_list` | `lambda$onCreate$6:450` |
| `requestcode` | `requestcode` | `tbbspl_list` | `lambda$onCreate$6:451` |
| `todate` | `todate` | `tbbspl_list` | `lambda$onCreate$6:452` |
| `selectecdfinanceids` | `selectecdfinanceids` | `tbbspl_list` | `lambda$onCreate$6:454` |
| `selectedfinancenames` | `selectedfinancenames` | `tbbspl_list` | `lambda$onCreate$6:455` |
| `fromdate` | `fromdate` | `monthwisepandl` | `lambda$onCreate$6:461` |
| `requestcode` | `requestcode` | `monthwisepandl` | `lambda$onCreate$6:462` |
| `todate` | `todate` | `monthwisepandl` | `lambda$onCreate$6:463` |
| `selectecdfinanceids` | `selectecdfinanceids` | `monthwisepandl` | `lambda$onCreate$6:465` |
| `selectedfinancenames` | `selectedfinancenames` | `monthwisepandl` | `lambda$onCreate$6:466` |
| `android.intent.extra.STREAM` | `Uri.fromFile(file1)` | — | `sharePdf:631` |

## partylistwithprevloansfornewloanselection

Receives: `AreaName` (String), `SubAreaNo` (Int), `ledgerid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `AreaName` | `AreaName` | `prevloanslist_act` | `lambda$onCreate$1:116` |
| `CallType` | `calledfrom` | `prevloanslist_act` | `lambda$onCreate$1:117` |
| `SubAreaNo` | `SubAreaNo` | `prevloanslist_act` | `lambda$onCreate$1:118` |
| `receivedparty` | `receivedparty` | `prevloanslist_act` | `lambda$onCreate$1:119` |
| `PLID` | `SubAreaNo` | `prevloanslist_act` | `onOptionsItemSelected:213` |
| `areaname` | `""` | `prevloanslist_act` | `onOptionsItemSelected:214` |
| `PLID` | `SubAreaNo` | `newloancreation` | `onOptionsItemSelected:220` |
| `areaname` | `""` | `newloancreation` | `onOptionsItemSelected:221` |
| `curpartyobj` | `partydetails_data` | `prevloanslist_act` | `onContactSelected:240` |
| `CallType` | `calledfrom` | `prevloanslist_act` | `onContactSelected:241` |
| `loanindex` | `1` | `newloancreation` | `onContactSelected:254` |
| `CallType` | `calledfrom` | `newloancreation` | `onContactSelected:255` |
| `partyledgerid` | `customersearchdata.getLedgerId()` | `prevloanslist_act` | `onprevloansbuttonclicked:266` |
| `partyname` | `customersearchdata.getLedgerName()` | `prevloanslist_act` | `onprevloansbuttonclicked:267` |

## partyrearrange

Receives: `PLID` (Int), `areaname` (String)

## partytransfer

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `getString(R.string.currentcollectionarea)` | `spinnersearch_list` | `lambda$onCreate$1:96` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$1:97` |
| `title` | `getString(R.string.newcollectionarea)` | `spinnersearch_list` | `lambda$onCreate$2:105` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$2:106` |
| `title` | `getString(R.string.partyselet)` | `spinnersearch_list` | `lambda$onCreate$3:114` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$3:115` |

## pattidetails_loans_act

Receives: `pcno` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `pattidetails_loansdata.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:148` |
| `PartyName` | `pattidetails_loansdata.getLedgerName()` | `selectedloancollectiondetails` | `onContactSelected:149` |
| `Remarks` | `""` | `selectedloancollectiondetails` | `onContactSelected:150` |
| `Tag` | `""` | `selectedloancollectiondetails` | `onContactSelected:151` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `onContactSelected:152` |

## periodicloareportlist_act

Receives: `LineName` (String), `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `periodicloanreport_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:156` |
| `MCLoanId` | `periodicloanreport_data.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:169` |
| `PartyName` | `periodicloanreport_data.getParty()` | `selectedloancollectiondetails` | `onContactSelected:170` |
| `Remarks` | `Remarks` | `selectedloancollectiondetails` | `onContactSelected:171` |
| `Tag` | `Tag` | `selectedloancollectiondetails` | `onContactSelected:172` |
| `iscollectioneditallowed` | `"0"` | `selectedloancollectiondetails` | `onContactSelected:173` |

## playstoredemouserregistrationact


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `otpValueactual` | `_otpValueactual` | `otpverificationact` | `lambda$onPostExecute$0:203` |
| `otpValueopen` | `_otpValueopen` | `otpverificationact` | `lambda$onPostExecute$0:204` |
| `curinsobj` | `curinsobj` | `otpverificationact` | `lambda$onPostExecute$0:205` |

## prevloanclosure_list_act

Receives: `finidforloanclose` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `requestcode` | `1011` | — | `onContactSelected:286` |

## prevloanslist_act

Receives: `LoanIndex` (Int), `partyledgerid` (Int), `partyname` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `prevloanslist_data.getMCLoanId()` | `archievedloandisplay` | `onContactSelected:188` |
| `PartyName` | `PartyName` | `archievedloandisplay` | `onContactSelected:203` |
| `Remarks` | `Remarks` | `archievedloandisplay` | `onContactSelected:204` |
| `Tag` | `Tag` | `archievedloandisplay` | `onContactSelected:205` |
| `prevloan` | `archivedmcloans_newdata` | `archievedloandisplay` | `onPostExecute:328` |
| `partyname` | `partyname` | `archievedloandisplay` | `onPostExecute:329` |

## refreshorreloadfinancelist

Receives: `passedfinance` (Serializable), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `findata` | `findata` | `financelinesaddandedit` | `onContactSelected:162` |
| `passedfinid` | `findata.getFinId()` | `closableloanssearchlistact` | `onContactSelected:168` |
| `listindex` | `adapterpos` | `closableloanssearchlistact` | `onContactSelected:169` |
| `financelist` | `(Serializable) financelist` | `closableloanssearchlistact` | `onContactSelected:170` |
| `reportdate` | `reportdate` | `closableloanssearchlistact` | `onContactSelected:171` |
| `passedfinance` | `findata` | `financelinesaddandedit` | `onContactSelected:177` |
| `passedfinance` | `findata` | — | `onContactSelected:183` |
| `passedfinance` | `findata` | `financelinesaddandedit` | `onEditSelected:244` |
| `finidforloanclose` | `curselectedfinance.getFinId()` | `closableloanssearchlistact` | `onActivityResult:555` |
| `weekdayid` | `valuemember` | `closableloanssearchlistact` | `onActivityResult:556` |

## selectbranchandfinance

Receives: `calledfor` (String), `displaymember` (String), `menuname` (String), `requestcode` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `thittamdate` | `thittamdate` | `closableloanssearchlistact` | `lambda$onCreate$1:367` |
| `financeid` | `financeid` | `closableloanssearchlistact` | `lambda$onCreate$1:368` |
| `passedfinanceid` | `financeid` | `executiveattendance_displayboard` | `lambda$onCreate$2:375` |
| `LineName` | `LineName` | `executiveattendance_displayboard` | `lambda$onCreate$2:376` |
| `fromdate` | `fromdate` | `executiveattendance_displayboard` | `lambda$onCreate$2:377` |
| `todate` | `todate` | `executiveattendance_displayboard` | `lambda$onCreate$2:378` |
| `passedfinanceid` | `financeid` | `executiveattendance_displayboard` | `lambda$onCreate$3:385` |
| `LineName` | `LineName` | `executiveattendance_displayboard` | `lambda$onCreate$3:386` |
| `fromdate` | `fromdate` | `executiveattendance_displayboard` | `lambda$onCreate$3:387` |
| `todate` | `todate` | `executiveattendance_displayboard` | `lambda$onCreate$3:388` |
| `passedfinanceid` | `financeid` | `executiveattendance_displayboard` | `lambda$onCreate$4:395` |
| `LineName` | `LineName` | `executiveattendance_displayboard` | `lambda$onCreate$4:396` |
| `fromdate` | `fromdate` | `executiveattendance_displayboard` | `lambda$onCreate$4:397` |
| `todate` | `todate` | `executiveattendance_displayboard` | `lambda$onCreate$4:398` |
| `title` | `"Select Branch Name"` | `closableloanssearchlistact` | `lambda$onCreate$5:412` |
| `searchlist` | `branchestobeshown` | `closableloanssearchlistact` | `lambda$onCreate$5:413` |
| `title` | `"Select Line Name"` | `dailypattinew_act` | `lambda$onCreate$6:429` |
| `searchlist` | `branchestobeshown` | `dailypattinew_act` | `lambda$onCreate$6:430` |
| `passedfinanceid` | `financeid` | `executiveattendance_displayboard` | `lambda$onCreate$12:503` |
| `LineName` | `LineName` | `executiveattendance_displayboard` | `lambda$onCreate$12:504` |
| `WeekDayId` | `WeekDayId` | `executiveattendance_displayboard` | `lambda$onCreate$12:505` |
| `passedfinanceid` | `financeid` | `executiveattendance_displayboard` | `lambda$onCreate$13:518` |
| `LineName` | `LineName` | `executiveattendance_displayboard` | `lambda$onCreate$13:519` |
| `sqlDate` | `fromdate` | `executiveattendance_displayboard` | `lambda$onCreate$13:520` |
| `passedfinanceid` | `financeid` | `closableloanssearchlistact` | `lambda$onCreate$13:526` |
| `LineName` | `LineName` | `closableloanssearchlistact` | `lambda$onCreate$13:527` |
| `fromdate` | `fromdate` | `closableloanssearchlistact` | `lambda$onCreate$13:528` |
| `passedfinanceid` | `financeid` | `dailypattinew_act` | `lambda$onCreate$13:534` |
| `LineName` | `LineName` | `dailypattinew_act` | `lambda$onCreate$13:535` |
| `fromdate` | `fromdate` | `dailypattinew_act` | `lambda$onCreate$13:536` |
| `todate` | `todate` | `dailypattinew_act` | `lambda$onCreate$13:537` |
| `passedfinanceid` | `financeid` | `periodicloareportlist_act` | `lambda$onCreate$13:543` |
| `LineName` | `LineName` | `periodicloareportlist_act` | `lambda$onCreate$13:544` |
| `fromdate` | `fromdate` | `periodicloareportlist_act` | `lambda$onCreate$13:545` |
| `todate` | `todate` | `periodicloareportlist_act` | `lambda$onCreate$13:546` |
| `passedfinanceid` | `financeid` | `simplethittamlist_act` | `lambda$onCreate$13:552` |
| `LineName` | `LineName` | `simplethittamlist_act` | `lambda$onCreate$13:553` |
| `fromdate` | `fromdate` | `simplethittamlist_act` | `lambda$onCreate$13:554` |
| `todate` | `todate` | `simplethittamlist_act` | `lambda$onCreate$13:555` |
| `passedfinanceid` | `financeid` | `partydetails_searchlist` | `lambda$onCreate$13:561` |
| `LineName` | `LineName` | `partydetails_searchlist` | `lambda$onCreate$13:562` |
| `menuname` | `menuname` | `partydetails_searchlist` | `lambda$onCreate$13:563` |
| `passedfinanceid` | `financeid` | `loanslist_act` | `lambda$onCreate$13:570` |
| `LineName` | `LineName` | `loanslist_act` | `lambda$onCreate$13:571` |
| `isweeklyfinance` | `false` | `loanslist_act` | `lambda$onCreate$13:572` |
| `weekdayid` | `0` | `loanslist_act` | `lambda$onCreate$13:573` |
| `title` | `"Collection Day"` | `spinnersearch_list` | `lambda$onCreate$13:582` |
| `searchlist` | `weekdayslist` | `spinnersearch_list` | `lambda$onCreate$13:583` |
| `selectedfinanceobj` | `selectedfinanceobj` | `editing_collectionlist_act` | `lambda$onCreate$13:591` |
| `finidforloanclose` | `financeid` | `prevloanclosure_list_act` | `lambda$onCreate$13:600` |
| `IsWeeklyFinance` | `IsWeeklyFinance` | `prevloanclosure_list_act` | `lambda$onCreate$13:601` |
| `title` | `"Collection Day"` | `spinnersearch_list` | `lambda$onCreate$13:614` |
| `searchlist` | `weekdayslist2` | `spinnersearch_list` | `lambda$onCreate$13:615` |
| `finidforloanclose` | `financeid` | `loanslist_act` | `lambda$onCreate$13:620` |
| `weekdayid` | `-1` | `loanslist_act` | `lambda$onCreate$13:621` |
| `title` | `"Collection Day"` | `spinnersearch_list` | `lambda$onCreate$13:630` |
| `searchlist` | `weekdayslist3` | `spinnersearch_list` | `lambda$onCreate$13:631` |
| `finidforloanclose` | `financeid` | `prevsavingclosure_list_act` | `lambda$onCreate$13:636` |
| `weekdayid` | `-1` | `prevsavingclosure_list_act` | `lambda$onCreate$13:637` |
| `finidforloanclose` | `financeid` | `closed_renewedloanslist_act` | `lambda$onCreate$13:643` |
| `weekdayid` | `-1` | `closed_renewedloanslist_act` | `lambda$onCreate$13:644` |
| `title` | `"Collection Day"` | `spinnersearch_list` | `lambda$onCreate$13:653` |
| `searchlist` | `weekdayslist4` | `spinnersearch_list` | `lambda$onCreate$13:654` |
| `fromdate` | `fromdate` | `spinnersearch_list` | `lambda$onCreate$13:655` |
| `todate` | `todate` | `spinnersearch_list` | `lambda$onCreate$13:656` |
| `selectedfinanceid` | `financeid` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:661` |
| `weekdayid` | `-1` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:662` |
| `fromdate` | `fromdate` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:663` |
| `todate` | `todate` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:664` |
| `selectedfinanceid` | `financeid` | `daywisependingreport_act` | `lambda$onCreate$13:671` |
| `weekdayid` | `-1` | `daywisependingreport_act` | `lambda$onCreate$13:672` |
| `fromdate` | `fromdate` | `daywisependingreport_act` | `lambda$onCreate$13:673` |
| `todate` | `todate` | `daywisependingreport_act` | `lambda$onCreate$13:674` |
| `title` | `"Collection Day"` | `spinnersearch_list` | `lambda$onCreate$13:683` |
| `searchlist` | `weekdayslist5` | `spinnersearch_list` | `lambda$onCreate$13:684` |
| `financeid` | `financeid` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:689` |
| `weekdayid` | `-1` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:690` |
| `fromdate` | `fromdate` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:691` |
| `todate` | `todate` | `newpartiesnewloanslist_act` | `lambda$onCreate$13:692` |
| `passedfinanceid` | `financeid` | `executiveattendance_displayboard` | `onPostExecute:1013` |
| `LineName` | `LineName` | `executiveattendance_displayboard` | `onPostExecute:1014` |
| `finidforloanclose` | `financeid` | `closableloanssearchlistact` | `onActivityResult:1298` |
| `weekdayid` | `valuemember2` | `closableloanssearchlistact` | `onActivityResult:1299` |
| `finidforloanclose` | `financeid` | `prevloanclosure_list_act` | `onActivityResult:1306` |
| `weekdayid` | `valuemember3` | `prevloanclosure_list_act` | `onActivityResult:1307` |
| `passedfinanceid` | `financeid` | `loanslist_act` | `onActivityResult:1314` |
| `LineName` | `LineName` | `loanslist_act` | `onActivityResult:1315` |
| `isweeklyfinance` | `true` | `loanslist_act` | `onActivityResult:1317` |
| `weekdayid` | `valuemember4` | `loanslist_act` | `onActivityResult:1318` |
| `finidforloanclose` | `financeid` | `prevsavingclosure_list_act` | `onActivityResult:1325` |
| `weekdayid` | `valuemember5` | `prevsavingclosure_list_act` | `onActivityResult:1326` |
| `selectedfinanceid` | `financeid` | `closed_renewedloanslist_act` | `onActivityResult:1333` |
| `weekdayid` | `valuemember6` | `closed_renewedloanslist_act` | `onActivityResult:1334` |
| `fromdate` | `fromdate` | `closed_renewedloanslist_act` | `onActivityResult:1335` |
| `todate` | `todate` | `closed_renewedloanslist_act` | `onActivityResult:1336` |
| `financeid` | `financeid` | `newpartiesnewloanslist_act` | `onActivityResult:1344` |
| `weekdayid` | `valuemember7` | `newpartiesnewloanslist_act` | `onActivityResult:1345` |
| `fromdate` | `fromdate` | `newpartiesnewloanslist_act` | `onActivityResult:1346` |
| `todate` | `todate` | `newpartiesnewloanslist_act` | `onActivityResult:1347` |
| `actiontype` | `9007` | `executiveattendance_displayboard` | `onPostExecute:1412` |

## selectdateforreport


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `CALLEDFROM` | `2` | `unpaidpartiesonselectedday` | `onPostExecute:213` |

## selectedloancollectiondetails

Receives: `iscollectioneditallowed` (Boolean)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `pdfUri` | — | `sharePdf:385` |
| `android.intent.extra.SUBJECT` | `"Collection Details"` | — | `sharePdf:386` |
| `android.intent.extra.TEXT` | `"Collection Details"` | — | `sharePdf:387` |
| `selectedcollection` | `tblmcloangroupcollectionmaster_newdata` | `editing_collections_act` | `onPostExecute:1035` |
| `selectedcollectiondate` | `tblmcloangroupcollectionmaster_newdata.getCollDate()` | `editing_collections_act` | `onPostExecute:1036` |
| `receviedfinanceid` | `tblmcloangroupcollectionmaster_newdata.getFinId()` | `editing_collections_act` | `onPostExecute:1037` |
| `mcloanidcalledforcollection` | `PassedMCLoanId.intValue()` | `editing_collections_act` | `onPostExecute:1038` |
| `selectedfinanceobj` | `selectedfinanceobj` | `editing_collections_act` | `onPostExecute:1039` |

## settings_act


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Prev Collections Mode"` | `spinnersearch_list` | `lambda$onCreate$4:272` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$4:273` |
| `title` | `"Financial Year"` | `spinnersearch_list` | `lambda$onCreate$5:281` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$5:282` |
| `title` | `"NO OF LOANS"` | `spinnersearch_list` | `lambda$onCreate$8:335` |
| `searchlist` | `billprintingmethods` | `spinnersearch_list` | `lambda$onCreate$8:336` |
| `title` | `"Prev Collections Mode"` | `spinnersearch_list` | `lambda$onCreate$9:345` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$9:346` |

## sevendayscollectionforselline

Receives: `passedfinanceid` (Int), `passedfinid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `sevendayscollectiondata.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:197` |
| `PartyName` | `sevendayscollectiondata.getLedgerName()` | `selectedloancollectiondetails` | `onContactSelected:198` |
| `Tag` | `sevendayscollectiondata.getTag()` | `selectedloancollectiondetails` | `onContactSelected:199` |

## simplethittamlist_act

Receives: `LineName` (String), `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `selectedledgername` | `getString(R.string.openingbalance)` | `ledgerstatement_act` | `onContactSelected:227` |
| `ledgeridpassed` | `-1` | `ledgerstatement_act` | `onContactSelected:228` |
| `fromdate` | `fromdate_patti` | `ledgerstatement_act` | `onContactSelected:229` |
| `todate` | `todate_patti` | `ledgerstatement_act` | `onContactSelected:230` |
| `reportfinid` | `financeid` | `ledgerstatement_act` | `onContactSelected:231` |
| `list` | `(Serializable) globalvars.acctransactionslit_datalist` | `ledgerstatement_act` | `onContactSelected:232` |
| `ledgername` | `getString(R.string.Closingamount)` | `ledgerstatement_act` | `onContactSelected:248` |
| `fromdate` | `fromdate_patti` | `ledgerstatement_act` | `onContactSelected:249` |
| `todate` | `todate_patti` | `ledgerstatement_act` | `onContactSelected:250` |
| `ledgeridpassed` | `-1` | `ledgerstatement_act` | `onContactSelected:251` |
| `list` | `(Serializable) globalvars.acctransactionslit_datalist` | `ledgerstatement_act` | `onContactSelected:252` |
| `reportfinid` | `financeid` | `ledgerstatement_act` | `onContactSelected:253` |
| `selectedledgername` | `selectedledgername` | `ledgerstatement_act` | `onContactSelected:263` |
| `ledgeridpassed` | `ledgerid` | `ledgerstatement_act` | `onContactSelected:264` |
| `fromdate` | `fromdate_patti` | `ledgerstatement_act` | `onContactSelected:265` |
| `todate` | `todate_patti` | `ledgerstatement_act` | `onContactSelected:266` |
| `reportfinid` | `financeid` | `ledgerstatement_act` | `onContactSelected:267` |
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:529` |

## singlefinancedailythittam_act

Receives: `LineName` (String), `passedfinanceid` (Int)

## SmsSender


| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `sms_body` | `messageText` | — | `openDefaultSmsApp:50` |

## specialaccesscontrolact

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"User Roles"` | `spinnersearch_list` | `lambda$onCreate$1:148` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$1:149` |

## spinnersearch_list

Receives: `searchlist` (Serializable), `title` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `displaymember` | `selectedobj.getDisplaymember()` | — | `onContactSelected:148` |
| `valuemember` | `selectedobj.getValuemember()` | — | `onContactSelected:149` |
| `selloandata` | `selloandata` | — | `onContactSelected:151` |
| `fromdate` | `fromdate` | — | `onContactSelected:154` |
| `todate` | `todate` | — | `onContactSelected:155` |

## stloan_interestcalculationdetails

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Received in/as"` | `spinnersearch_list` | `lambda$onCreate$2:236` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$2:237` |
| `receivedinterestrow` | `receivedinterestrow` | — | `lambda$onCreate$3:253` |

## stloaninterestcalculationandpayments

Receives: `receivedinterestrow` (Serializable)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `loanordepositid` | `indvidualloanordepositstatus_data.getSTLoanId()` | `stloan_interestcalculationdetails` | `onloanselected:397` |
| `partyledgerid` | `indvidualloanordepositstatus_data.getPartyLedgerId()` | `stloan_interestcalculationdetails` | `onloanselected:398` |
| `requesttype` | `constantvalues.individualloandetailsrequestfrominterestcollectionscre…` | `stloan_interestcalculationdetails` | `onloanselected:399` |
| `selectedfinancenames` | `indvidualloanordepositstatus_data.getFin()` | `stloan_interestcalculationdetails` | `onloanselected:400` |
| `partyname` | `receivedparty.getLedgerName()` | `stloan_interestcalculationdetails` | `onloanselected:401` |
| `loanno` | `indvidualloanordepositstatus_data.getNo()` | `stloan_interestcalculationdetails` | `onloanselected:402` |
| `intrestcalculationrow` | `interestcalculationlist_data` | `stloan_interestcalculationdetails` | `onInterestCalculationRowSelected:414` |
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:1526` |
| `android.intent.extra.STREAM` | `apkURI` | — | `sharePdfwithwhatsapp:1566` |
| `jid` | `toNumber3 + "@s.whatsapp.net"` | — | `sharePdfwithwhatsapp:1567` |
| `android.intent.extra.TEXT` | `""` | — | `sharePdfwithwhatsapp:1568` |
| `android.intent.extra.STREAM` | `apkURI` | — | `sharePdfwithwhatsappbusiness:1619` |
| `jid` | `toNumber3 + "@s.whatsapp.net"` | — | `sharePdfwithwhatsappbusiness:1620` |
| `android.intent.extra.TEXT` | `""` | — | `sharePdfwithwhatsappbusiness:1621` |

## stloans

Receives: `actiontype` (Int), `displaymember` (String), `secondvaluemember` (Int), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"Party Recomendation"` | `spinnersearch_list` | `lambda$onCreate$0:383` |
| `requestcode` | `constantvalues.loan_getrecommendedpartyname` | `spinnersearch_list` | `lambda$onCreate$0:385` |
| `finid` | `tblSTLoans_newdata.getFinId()` | `spinnersearch_list` | `lambda$onCreate$0:386` |
| `title` | `"Party Recomendation"` | `spinnersearch_list` | `lambda$onCreate$1:393` |
| `requestcode` | `constantvalues.loan_getrecommendedpartnername` | `spinnersearch_list` | `lambda$onCreate$1:395` |
| `finid` | `tblSTLoans_newdata.getFinId()` | `spinnersearch_list` | `lambda$onCreate$1:396` |
| `title` | `"Received in/as"` | `spinnersearch_list` | `lambda$onCreate$2:404` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$2:405` |
| `title` | `"Received in/as"` | `spinnersearch_list` | `lambda$onCreate$3:413` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$3:414` |
| `title` | `"Finance"` | `spinnersearch_list` | `lambda$onCreate$5:428` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$5:429` |
| `title` | `"Select Party"` | `spinnersearch_list` | `lambda$onCreate$6:437` |
| `requestcode` | `constantvalues.loan_getpartyname` | `spinnersearch_list` | `lambda$onCreate$6:439` |
| `finid` | `1` | `spinnersearch_list` | `lambda$onCreate$6:440` |
| `title` | `"Select Party"` | `spinnersearch_list` | `lambda$onCreate$7:449` |
| `requestcode` | `constantvalues.loan_getpartyname` | `spinnersearch_list` | `lambda$onCreate$7:451` |
| `finid` | `1` | `spinnersearch_list` | `lambda$onCreate$7:452` |
| `title` | `"Select Party"` | `spinnersearch_list` | `lambda$onCreate$8:461` |
| `requestcode` | `constantvalues.loan_getpartyname` | `spinnersearch_list` | `lambda$onCreate$8:463` |
| `finid` | `1` | `spinnersearch_list` | `lambda$onCreate$8:464` |
| `title` | `"Transaction Method"` | `spinnersearch_list` | `lambda$onCreate$10:473` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `lambda$onCreate$10:474` |
| `android.intent.extra.TEXT` | `" "` | — | `lambda$showprintandshareoptions$23:1107` |
| `android.intent.extra.STREAM` | `bmpUri2` | — | `lambda$showprintandshareoptions$23:1108` |
| `title` | `"Finance"` | `spinnersearch_list` | `onActivityResult:1347` |
| `searchlist` | `ledgerslist` | `spinnersearch_list` | `onActivityResult:1348` |
| `title` | `"Party Recomendation"` | `parties_list` | `onActivityResult:1375` |
| `requestcode` | `constantvalues.loan_getrecommendedpartnername` | `parties_list` | `onActivityResult:1377` |
| `finid` | `tblSTLoans_newdata.getFinId()` | `parties_list` | `onActivityResult:1378` |
| `title` | `"Transaction Method"` | `spinnersearch_list` | `onActivityResult:1409` |
| `searchlist` | `ledgerslist2` | `spinnersearch_list` | `onActivityResult:1410` |
| `android.intent.extra.STREAM` | `uri` | — | `sharePdf:2279` |

## stockcat_list

Receives: `noofrowsdeleted` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `constantvalues.stockcat_addnewrequestid` | `stockcategories` | `lambda$onCreate$1:122` |
| `displayindex` | `stockcat_dataList.size() + 1` | `stockcategories` | `lambda$onCreate$1:123` |
| `stockcatobj` | `stockcatobj` | `stockcategories` | `onContactSelected:175` |
| `actiontype` | `constantvalues.stockcat_editrequestid` | `stockcategories` | `onContactSelected:176` |

## stockcategories

Receives: `actiontype` (Int), `displayindex` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `actiontype` | — | `onPostExecute:228` |
| `stockobj` | `curstockcat` | — | `onPostExecute:229` |
| `noofrowsdeleted` | `noofrowsdeleted` | — | `onPostExecute:230` |

## stockgroup_act

Receives: `actiontype` (Int), `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `stockgrpobj` | `curstockgrp` | — | `onPostExecute:235` |
| `deletedrowscount` | `deletedrowscount` | — | `onPostExecute:236` |

## stockgrp_list

Receives: `deletedrowscount` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `constantvalues.stockgrorup_addnewrequestid` | `stockgroup_act` | `lambda$onCreate$1:122` |
| `stockgrpobj` | `stockgrpData` | `stockgroup_act` | `onstockgroupselected:210` |
| `actiontype` | `constantvalues.stockgrorup_editrequestid` | `stockgroup_act` | `onstockgroupselected:211` |

## stockUOM_act

Receives: `actiontype` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `stockuomobj` | `curstockuom` | — | `onPostExecute:218` |
| `actiontype` | `actiontype` | — | `onPostExecute:219` |
| `noofrowsdeleted` | `noofrowsdeleted` | — | `onPostExecute:220` |

## stockUOM_list

Receives: `noofrowsdeleted` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `actiontype` | `constantvalues.stockuom_addnewrequestid` | `stockUOM_act` | `onClick:69` |
| `stockuomobj` | `curstockcat` | `stockUOM_act` | `onContactSelected:166` |
| `actiontype` | `constantvalues.stockuom_editrequestid` | `stockUOM_act` | `onContactSelected:167` |

## syncresources

Receives: `issyncneeded` (Boolean)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `issyncneeded` | `issyncneeded` | `navigationdrawactivity` | `lambda$onPostExecute$2:786` |

## tabbedcollectionlist

Receives: `listindex` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `PLID` | `curscreensubareano` | `newloancreation` | `onOptionsItemSelected:133` |
| `AreaName` | `"AreaName"` | `newloancreation` | `onOptionsItemSelected:134` |
| `PLID` | `Integer.parseInt(textView.getTag().toString())` | `newloancreation` | `lambda$onCreateView$3:303` |
| `areaname` | `textView.getText().toString()` | `newloancreation` | `lambda$onCreateView$3:304` |
| `AreaName` | `globalvars.areawisecollectionlistdataList.get(getArguments().getInt(A…` | `newloancreation` | `lambda$onCreateView$4:311` |
| `SubAreaNo` | `globalvars.areawisecollectionlistdataList.get(getArguments().getInt(A…` | `newloancreation` | `lambda$onCreateView$4:312` |
| `CallType` | `requestcodes.editloanrequestcodefromOFFLINEdata` | `newloancreation` | `onContactSelected:388` |
| `SubAreaName` | `tabbedcollectionlistdata.getPartyName()` | `tabbedindividualcollectionentry` | `onContactSelected:401` |
| `SubAreaNo` | `tabbedcollectionlistdata.getSubAreaNo()` | `tabbedindividualcollectionentry` | `onContactSelected:402` |
| `MCLoanId` | `tabbedcollectionlistdata.getMCLoanId()` | `tabbedindividualcollectionentry` | `onContactSelected:403` |
| `collectionareaname` | `tabbedcollectionlistdata.getTag()` | `tabbedindividualcollectionentry` | `onContactSelected:404` |
| `listindex` | `listpos` | `tabbedindividualcollectionentry` | `onContactSelected:417` |

## tabbedcollectionlistmviewpager

Receives: `listindex` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `PLID` | `tabbedcollectionlistmviewpager.curscreensubareano` | `tabbedindividualcollectionentry` | `onOptionsItemSelected:117` |
| `areaname` | `tabbedcollectionlistmviewpager.curscreensubareaname` | `tabbedindividualcollectionentry` | `onOptionsItemSelected:118` |
| `PLID` | `tabbedcollectionlistmviewpager.curscreensubareano` | `maps_partydisplay` | `onOptionsItemSelected:124` |
| `areaname` | `tabbedcollectionlistmviewpager.curscreensubareaname` | `maps_partydisplay` | `onOptionsItemSelected:125` |
| `AreaName` | `globalvars.areawisecollectionlistdataList.get(getArguments().getInt(A…` | `tabbedindividualcollectionentry` | `lambda$onCreateView$0:251` |
| `SubAreaNo` | `globalvars.areawisecollectionlistdataList.get(getArguments().getInt(A…` | `tabbedindividualcollectionentry` | `lambda$onCreateView$0:252` |
| `SubAreaName` | `tabbedcollectionlistdata.getPartyName()` | `tabbedindividualcollectionentry` | `onContactSelected:301` |
| `SubAreaNo` | `tabbedcollectionlistdata.getSubAreaNo()` | `tabbedindividualcollectionentry` | `onContactSelected:302` |
| `MCLoanId` | `tabbedcollectionlistdata.getMCLoanId()` | `tabbedindividualcollectionentry` | `onContactSelected:303` |
| `collectionareaname` | `tabbedcollectionlistdata.getTag()` | `tabbedindividualcollectionentry` | `onContactSelected:304` |
| `listindex` | `listpos` | `tabbedindividualcollectionentry` | `onContactSelected:317` |

## tabbedindividualcollectionentry

Receives: `Islocallyfixed` (Boolean), `SubAreaNo` (Int), `listindex` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `receivedloan` | `receivedloan` | — | `lambda$onCreateView$5:1407` |
| `calledfor` | `100` | — | `lambda$onCreateView$5:1408` |
| `title` | `"வரவு வைக்கப்படும் கணக்கு"` | — | `lambda$onCreateView$7:1436` |
| `searchlist` | `billprintingmethods` | — | `lambda$onCreateView$7:1437` |
| `selloandata` | `selloandata` | — | `lambda$onCreateView$7:1438` |
| `ledgerid` | `selloandata.getPartyLedgerId()` | — | `lambda$onCreateView$8:1447` |
| `SubAreaNo` | `selloandata.getSubAreaNo()` | — | `lambda$onCreateView$8:1448` |
| `CallType` | `constantvalues.partyscreen_partyshowandupdaterequestfrom_tabbedcollec…` | — | `lambda$onCreateView$8:1449` |
| `MCLoanId` | `selloandata.getMCLoanId()` | — | `lambda$onCreateView$14:1492` |
| `PartyName` | `selloandata.getPartyName()` | — | `lambda$onCreateView$14:1493` |
| `Remarks` | `selloandata.getRemarks()` | — | `lambda$onCreateView$14:1494` |
| `Tag` | `tvtag.getText().toString()` | — | `lambda$onCreateView$14:1495` |
| `iscollectioneditallowed` | `true` | — | `lambda$onCreateView$14:1496` |

## tbbspl_list

Receives: `requestcode` (Int), `selectecdfinanceids` (String), `selectedfinancenames` (String)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `details` | `listtopassed` | `ledgerstatement_act` | `onTransactionselected:286` |
| `cdesc` | `transdata.getComponentDesc()` | `ledgerstatement_act` | `onTransactionselected:287` |
| `fromdate` | `fromdate` | `ledgerstatement_act` | `onTransactionselected:288` |
| `todate` | `todate` | `ledgerstatement_act` | `onTransactionselected:289` |
| `reportdate` | `reportdate` | `ledgerstatement_act` | `onTransactionselected:290` |
| `ledgername` | `SelectedLedgerName` | `ledgerstatement_act` | `onPostExecute:806` |
| `fromdate` | `fromdate` | `ledgerstatement_act` | `onPostExecute:807` |
| `todate` | `reportdate` | `ledgerstatement_act` | `onPostExecute:808` |
| `reportfinid` | `globalvars.finid` | `ledgerstatement_act` | `onPostExecute:809` |
| `receivedledgerid` | `SelectedLedgerId` | `ledgerstatement_act` | `onPostExecute:810` |
| `android.intent.extra.STREAM` | `Uri.fromFile(file1)` | — | `sharePdf:1068` |

## tbbspl_particulars

Receives: `cdesc` (String), `displaymember` (String), `receivedledgerid` (Int), `related_ledgeramount` (Int), `related_ledgerid` (Int), `related_ledgerstatus` (String), `resultamountCr` (Int), `resultamountDr` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `accounttrans_data.getMCLoanId()` | `ledgerstatement_act` | `onTransactionselected:152` |
| `PartyName` | `accounttrans_data.getLedgerName()` | `ledgerstatement_act` | `onTransactionselected:153` |
| `Remarks` | `""` | `ledgerstatement_act` | `onTransactionselected:154` |
| `Tag` | `""` | `ledgerstatement_act` | `onTransactionselected:155` |
| `ledgername` | `SelectedLedgerName` | `ledgerstatement_act` | `onPostExecute:226` |
| `fromdate` | `globalvars.accyearfromdate` | `ledgerstatement_act` | `onPostExecute:227` |
| `todate` | `globalvars.accyeartodate` | `ledgerstatement_act` | `onPostExecute:228` |
| `receivedledgerid` | `SelectedLedgerId` | `ledgerstatement_act` | `onPostExecute:229` |
| `reportfinid` | `globalvars.finid` | `ledgerstatement_act` | `onPostExecute:230` |

## todaycollectionabstractlist

Receives: `LineName` (String), `passedfinance` (Serializable), `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `financeid` | `financeid` | `todaycollectionlist` | `onContactSelected:226` |
| `requestcode` | `requestcodes.todaycollectiondetails_requestcode_fromabstract` | `todaycollectionlist` | `onContactSelected:227` |
| `collectiondate` | `sqlDate` | `todaycollectionlist` | `onContactSelected:228` |
| `ctid` | `todaycollectionabstract_data.getCtid()` | `todaycollectionlist` | `onContactSelected:229` |
| `collectiontype` | `todaycollectionabstract_data.getCollectiontype()` | `todaycollectionlist` | `onContactSelected:230` |
| `Totalamount` | `todaycollectionabstract_data.getTotalamount()` | `todaycollectionlist` | `onContactSelected:231` |
| `Partycount` | `todaycollectionabstract_data.getPartycount()` | `todaycollectionlist` | `onContactSelected:232` |

## todaycollectionlist

Receives: `collectiontype` (String), `ctid` (Int), `financeid` (Int), `passedfinance` (Serializable), `requestcode` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `MCLoanId` | `todaycollectionsdata.getMCLoanId()` | `selectedloancollectiondetails` | `onContactSelected:424` |
| `PartyName` | `PartyName` | `selectedloancollectiondetails` | `onContactSelected:439` |
| `Remarks` | `Remarks` | `selectedloancollectiondetails` | `onContactSelected:440` |
| `Tag` | `Tag` | `selectedloancollectiondetails` | `onContactSelected:441` |

## userroleprevilages_act

Receives: `displaymember` (String), `valuemember` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `title` | `"User Role"` | `spinnersearch_list` | `lambda$onCreate$2:131` |
| `searchlist` | `userlevelsmethods` | `spinnersearch_list` | `lambda$onCreate$2:132` |

## varathittam

Receives: `passedfinanceid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `android.intent.extra.STREAM` | `uri` | — | `selectedweekthittamandpattisharePdf:952` |

## weeklycollectionanalysis_act

Receives: `financeid` (Int)

| Sends key | Value | Target | Method:line |
|---|---|---|---|
| `loanweek` | `weeklycollectionanalysis_data` | `weeklycollectionpatti_dashboard` | `onContactSelected:148` |
| `financeid` | `financeid` | `weeklycollectionpatti_dashboard` | `onContactSelected:149` |
| `weekno` | `weeklycollectionanalysis_data.getWeekNo()` | `weeklycollectionpatti_dashboard` | `onContactSelected:150` |

## weeklycollectionpatti_dashboard

Receives: `financeid` (Int), `weekno` (Int)

