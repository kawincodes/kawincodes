# Appendix C — Layout inventory (all app layouts)

211 app-specific layouts (library layouts such as `abc_*`, `mtrl_*`, `material_*`, `ucrop_*`, `design_*` are omitted). For each layout: the classes that inflate it, layouts that `<include>` it, and the full widget tree with ids and labels (English / Tamil).

## `ac_complex.xml`
- Inflated by: `loananddepositanalyisreport`
- ViewPager `#pager`
  - PagerTitleStrip contentDescription="01-06-2001" visibility=gone

## `accledgers_row.xml`
- Inflated by: `accledgerssearch_adapter`, `collectionreportprintingmethods_adapter`
- RelativeLayout
  - TextView `#accledgers_ledgername` text="Acc Ledger Name" / ta "ledgername"
  - TextView `#accledgers_groupname` text="Group Name"

## `accountstranslist_row.xml`
- Inflated by: `accounttranslist_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#accounttranslistrow_transdateid` text="12-10-2020"
    - TextView `#accounttranslistrow_finnameid` text="TMC"
    - TextView `#accounttranslistrow_particularsid` text="SALARY EXPENSES"
    - TextView `#accounttranslistrow_transamountid` text="15000"

## `activity_acctranslist.xml`
- Inflated by: `accounttrans_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout `#lytdaterangeid`
      - TextView text="From"
      - TextView text=": "
      - TextView `#ds_fromdateid` text="06-06-2020"
      - TextView text="To"
      - TextView text=": "
      - TextView `#ds_todateid` text="06-Jun-2020"
      - ImageButton `#acctranslistrefreshid`
    - View
    - RecyclerView `#acctranslist_recycler_view`
  - ProgressBar `#acctranslist_pbar` visibility=gone
  - FloatingActionButton `#acctranslist_addaccounttransactionfabid` contentDescription="add new transaction" / ta "புதிய"

## `activity_allfinancedailypatti.xml`
- Inflated by: `allfinancedailypatti`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar` app:"FinSoft II" / ta "FinSoft"
  - ViewPager `#container`

## `activity_allfinancedailythittam.xml`
- Inflated by: `allfinancedailythittam`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar` app:"Daily Thittam" / ta "தினத்திட்டம்"
  - ViewPager `#container`
  - FloatingActionButton `#fab` visibility=gone

## `activity_archievedloandisplay.xml`
- Inflated by: `archievedloandisplay`
- ConstraintLayout `#main`
  - LinearLayout
    - TextView `#cd_arcpartynameid` text="PARTY NAME"
    - View
    - LinearLayout
      - LinearLayout
        - TextView text="Loan Amount"
        - TextView text="Interest Amount"
        - TextView text="No of Dues"
        - TextView text="No of Days Taken"
        - TextView text="Loan Date"
        - TextView text="Closing Date"
        - TextView text="Last Payment Date"
        - TextView text="Loan Collectable"
        - TextView text="Min Collection"
        - TextView text="Max Collection"
        - TextView text="Average Collection"
        - TextView text="Minimum Gap"
        - TextView text="Maximum Gap"
        - TextView text="Average Gap"
        - TextView text="Loan index"
      - LinearLayout
        - TextView `#ilc_arcloanamountid` text="Loan Amount"
        - TextView `#ilc_arcinterestamountid` text="Interest Amount"
        - TextView `#ilc_arcnoofduesid` text="Dues"
        - TextView `#ilc_arcnoofdaysid` text="No of days paid"
        - TextView `#ilc_arcloandateid` text="Loan Date"
        - TextView `#ilc_arcclosingdateid` text="Closing Date"
        - TextView `#ilc_arclastpaymentdateid` text="Last payment Date"
        - TextView `#ilc_arcloanamountcollectedid` text="Loan Collected"
        - TextView `#ilc_arcmincollectedamountid` text="Min collected"
        - TextView `#ilc_arcmaxcollectionid` text="Max Collection"
        - TextView `#ilc_arcavgcollectionid` text="Avg Collection"
        - TextView `#ilc_arcmingapid` text="Min Gap"
        - TextView `#ilc_arcmaxgapid` text="Max Gap"
        - TextView `#ilc_arcavggapid` text="Avg Gap"
        - TextView `#ilc_arcloanindexid` text="loan index"

## `activity_branchmobiledeviceaccesscontrol.xml`
- Inflated by: `branchmobiledeviceaccescontrol_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - TextView text="Android Device ID"
    - LinearLayout
      - EditText `#mdac_imeiid` hint="android device id"
      - Button `#mobiledeviceaccesscontrol_getGIbuttonid` text="GI"
    - TextView text="Employee"
    - EditText `#mdac_employeeetid`
    - CheckBox `#mdac_isbranchusercontrolallowedid` text="Is Branch User Control Allowed"
    - CheckBox `#mdac_activeid` text="Active"
    - TextView text="Branches Allowed" / ta "அனுமதிக்கப்பட்ட கிளைகள்"
    - RecyclerView `#recycler_view`
    - Button `#btnsavelinemobileaccess` text="SAVE"
    - Button `#btndeletelinemobileaccess` text="DELETE"
  - ProgressBar `#progressBar` visibility=gone

## `activity_cashbook.xml`
- Inflated by: `cashbook`
- RelativeLayout `#main`
  - LinearLayout
    - LinearLayout
      - TextView text="Date" / ta "தேதி"
      - TextView `#reportdatetvid` text="01-06-2001"
    - LinearLayout
      - TextView text="Ledger"
      - EditText `#bankledgers_id`
      - AppCompatButton `#btnPrepareandPrintcashbook` text="SHOW"
    - LinearLayout
      - RecyclerView `#recycler_view`
    - LinearLayout
      - TextView `#cashbook_emailstatusid`
      - AppCompatButton `#btncashbookverfiedid` text="Cash Book Verified" / ta "CASH BOOK VERIFID" visibility=gone
      - AppCompatButton `#btncashbookclearverificationnid` text="CLEAR VERIFICATION" visibility=gone
  - ProgressBar `#cashbook_pbar` visibility=gone
  - TextView `#cashbook_progresstexttvid` visibility=gone

## `activity_cashbookabstract.xml`
- Inflated by: `cashbookabstract`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - RecyclerView `#cashbookabstract_recycler_view`
  - ProgressBar `#cashbookabstract_pbar` visibility=gone

## `activity_cashreceiveandreturns.xml`
- Inflated by: `cashreceiveandreturns`
- ConstraintLayout `#main`
  - LinearLayoutCompat
    - TextView `#notesid` text="Enter the amount received from the manager by the person going for collection." / ta "வசூலுக்கு செல்பவர், மேனேஜரிடமிருந்து பெற்றுச் செல்லும் தொகையை இங்கு பதிவிடவும்."
    - TextView `#tvpaymentreceiptsheaderid` text="Amount Received" / ta "பெற்றுக் கொள்ளப்பட்ட தொகை"
    - EditText `#etreceptandpaymentamount` inputType=numberSigned
    - Button `#paymentreceiptssavebuttonid` text="SAVE"

## `activity_closed_remevedloanslist.xml`
- Inflated by: `closed_renewedloanslist_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout `#lytdaterangeid`
      - TextView text="From"
      - TextView text=": "
      - TextView `#ds_fromdateid` text="01-06-2001"
      - TextView text="To"
      - TextView text=": "
      - TextView `#ds_todateid` text="01-06-2001"
      - ImageButton `#acctranslistrefreshid` contentDescription="Refresh"
    - View
    - LinearLayout
      - LinearLayout
        - LinearLayout `#noofclosedloanslytid`
          - TextView `#noofclosedloanslabeltvid` text="No of Closed Loans" / ta "முடிந்த கடன்கள்"
          - TextView text=":"
          - TextView `#noofclosedloansvaluetvid` text="12"
        - LinearLayout `#newloangetpartieslytid`
          - TextView `#newloangetpartieslabeltvid` text="No of Parties get New Loans" / ta "மறு கடன் பெற்றவர்கள்"
          - TextView text=":"
          - TextView `#newloangetpartiesvaluetvid`
        - LinearLayout `#newloannotgetpartieslytid`
          - TextView `#newloannotgetpartieslabeltv` text="No of Parties not get New Loans" / ta "மறு கடன் பெறாதவர்கள்"
          - TextView text=":"
          - TextView `#newloannotgetpartiesvaluetv`
        - LinearLayout `#overamountnewloanslytid`
          - TextView `#overamountnewloanslabeltvid` text="No of Extra Amount Loans" / ta "கூடுதல் தொகை அளிக்கப்பட்ட மறு கடன்கள்"
          - TextView text=":"
          - TextView `#overamountnewloansvaluetvid`
        - LinearLayout `#lessamountnewloanslytid`
          - TextView `#lessamountnewloanslabletvid` text="No of Less Amount Loans" / ta "குறைவான தொகை மறுகடன் பெற்றவர்கள்"
          - TextView text=":"
          - TextView `#lessamountnewloansvaluetvid`
        - LinearLayout `#sameamountnewloanslytid`
          - TextView `#sameamountnewloanslabeltvid` text=""No of Same Amount Loans "" / ta "அதே தொகை மறு கடன்கள்"
          - TextView text=":"
          - TextView `#sameamountnewloansvaluetvid`
    - View
    - TextView `#titletvid`
    - include layout=@layout/customersearchlist

## `activity_closingsavingsaccount.xml`
- Inflated by: `closingsavingsaccount`
- ConstraintLayout `#main`
  - LinearLayout
    - TextView `#loanclose_partynameid` text="party"
    - LinearLayout
      - TextView text="Balance Available" / ta "இருப்புத் தொகை"
      - TextView text=":"
      - TextView `#closingsavingsaccount_balanceavaialabletv`
    - LinearLayout
      - TextView text="Interest" / ta "வட்டி"
      - TextView `#interestlbl` text=":"
      - EditText `#closingsavingsaccount_interestid` inputType=numberDecimal
    - LinearLayout
      - TextView text="Total" / ta "மொத்தம்"
      - TextView text=":"
      - TextView `#closingsavingsaccount_totalpaidid`
    - LinearLayout
      - TextView text="Date" / ta "நாள்"
      - TextView text=":"
      - EditText `#closingsavingsaccount_closingdateid` inputType=numberDecimal
    - Button `#closingsavingsaccount_savingsbuttonid` text="SAVE"

## `activity_collectdbinfofromweb.xml`
- Inflated by: `collectdbinfofromweb`
- LinearLayout
  - ImageView contentDescription="logo"
  - TextView text="Venus Solutions"
  - TextView text="Contact Nos 📱"
  - TextView text="+919655842840, +919790070227, +919487792840" / ta "Contact Number: +919655842840, +919790070227,+919487792840"
  - TextView text="Enter Your Registration Key"
  - TextView text="DEMO Registration Key"
  - TextView text="M M M M M M"
  - LinearLayout
    - EditText `#et_key_1` inputType=textCapCharacters
    - EditText `#et_key_2` inputType=textCapCharacters
    - EditText `#et_key_3` inputType=textCapCharacters
    - EditText `#et_key_4` inputType=textCapCharacters
    - EditText `#et_key_5` inputType=textCapCharacters
    - EditText `#et_key_6` inputType=textCapCharacters
  - Button `#btn_submit_key` text="📨 Submit"
  - ProgressBar `#collectdbpbarid` visibility=gone
  - LinearLayout
    - Button `#btn_watch_video` text="▶️ Watch Video"

## `activity_collectionareaarangment.xml`
- Inflated by: `collectionarearearrangeact`
- CustomListView `#listView1`

## `activity_collectionareasearch.xml`
- Inflated by: `collectionareassearchlist`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
    - LinearLayout `#filterlayoutid` visibility=gone
      - Spinner `#filterSpinner` entries=@array/filter_options
      - TextView `#selectedfiltervalue`
  - RecyclerView `#recycler_view`
  - FloatingActionButton `#addnewemployeeid` contentDescription="add new area"
  - ProgressBar `#capbarid` visibility=gone

## `activity_collectionedit_individualcollection.xml`
- Inflated by: `collectionedit_individualcollection`
- ConstraintLayout `#main`
  - LinearLayout
    - LinearLayout
      - LinearLayout
        - ImageView `#ilc_thumbnail`
        - LinearLayout
          - TextView `#ilc_editpartynameid` text="PARTY NAME"
    - LinearLayout
      - LinearLayout
        - TextView text="Loan Amount"
        - TextView text=":"
        - TextView `#ilc_editloanamountid` text="Loan Amount"
      - LinearLayout
        - TextView text="Balance"
        - TextView text=":"
        - TextView `#ilc_editbalanceid` text="balance"
      - LinearLayout
        - TextView text="Loan Date"
        - TextView text=":"
        - TextView `#ilc_editloandateid` text="Date"
      - LinearLayout `#tabbedindloancolentry_collectedamountlayoutid`
        - TextView text="Due"
        - TextView text=":"
        - TextView `#ilc_editdueamountid` text="Due"
      - LinearLayout
        - TextView text="Loan Due Collection"
        - EditText `#ilc_editLoanDuecollectionamountid` inputType=number
      - LinearLayout visibility=gone
        - TextView `#ilc_penaltylabeltv` text="Pen/Int Collection"
        - EditText inputType=number
      - LinearLayout visibility=gone
        - TextView `#ilc_savingslabeltv` text="Savings Collection"
        - EditText inputType=number
      - LinearLayout
        - TextView `#ilc_cashcollectedlabeltv` text="Cash Collection"
        - EditText `#ilc_editcashcollId` inputType=number
      - LinearLayout
        - TextView text="Bank Collection"
        - EditText `#ilc_editbankcollId` inputType=number
      - LinearLayout visibility=gone
        - TextView text="Collected as/ In"
        - EditText `#ilc_editcollTargetLedgerId` inputType=number

## `activity_collectionlistprints.xml`
- Inflated by: `collectionlistprints`
- ConstraintLayout `#main`
  - LinearLayout
    - LinearLayout
      - TextView `#tvRouteNameid` text="Line Name"
      - TextView text="Collection Date" / ta "வசூல் தேதி"
      - TextView `#dcl_collectiondateid` text="01-06-2001"
      - TextView `#dcl_collectiondayid` text="WEDNESDAY"
      - LinearLayout
        - TextView text="Collection Report Printing Method"
        - EditText `#reportprint_collectionreportprintingmethodid`
        - CheckBox `#chkincludecollectionid` text="Include Collection Details"
      - Button `#btnprint` text="Print"
  - ProgressBar `#collectionreportprint_pbarid` visibility=gone

## `activity_customerdetails.xml`
- Inflated by: `customerdetails_act`
- CoordinatorLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="Customer Name"
      - EditText `#customerdetails_etledgernameid` inputType=textCapWords
      - TextView text="Add Line 1"
      - EditText `#customerdetails_etaddline1id` inputType=textCapWords
      - TextView text="Add Line 2"
      - EditText `#customerdetails_etaddline2id` inputType=textCapWords
      - TextView text="City"
      - EditText `#customerdetails_etcityid` inputType=textCapWords
      - TextView text="Pincode"
      - EditText `#customerdetails_etpincodeid` inputType=number
      - TextView text="Mobile  No1"
      - EditText `#customerdetails_etmobileno1id` inputType=number
      - TextView text="Mobile No2"
      - EditText `#customerdetails_etmobileno2id` inputType=number
      - TextView text="EMail"
      - EditText `#customerdetails_etemailid` inputType=textEmailAddress
      - LinearLayoutCompat visibility=gone
        - TextView text="groupid"
        - EditText `#customerdetails_etgroupidid`
        - TextView text="isdefault"
        - EditText `#customerdetails_etisdefaultid`
        - TextView text="isreserved"
        - EditText `#customerdetails_etisreservedid`
        - TextView text="screentype"
        - EditText `#customerdetails_etscreentypeid`
      - CheckBox `#customerdetails_chkisactiveid` text="ACTIVE" visibility=gone
      - Button `#customerdetails_submitid` text="SAVE"
      - Button `#customerdetails_deleteid` text="DELETE"
  - ProgressBar `#customerdetails_pbar` visibility=gone

## `activity_customersearch.xml`
- Inflated by: `loanindexloanswiselist_act`, `partylistwithprevloansfornewloanselection`, `pattidetails_loans_act`, `prevloanslist_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - SwipeRefreshLayout `#refreshLayout`
    - RecyclerView `#recycler_view`
  - FloatingActionButton `#fab`
  - ProgressBar `#cs_pbarid` visibility=gone
  - TextView visibility=gone

## `activity_dailypattinew.xml`
- Inflated by: `dailypattinew_act`
- CoordinatorLayout `#main`
  - LinearLayout
    - TextView `#dailypattinew_financenameid` text="FINANCE NAME"
    - RecyclerView `#recycler_view`

## `activity_dailythittamforallowedlines.xml`
- Inflated by: `dailythittamforallowedlines`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar` app:"FinSoft II" / ta "FinSoft"
  - ViewPager `#container`

## `activity_daywisependingreport.xml`
- Inflated by: `daywisependingreport_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - include layout=@layout/customersearchlist

## `activity_deviceidentity.xml`
- Inflated by: `deviceidentity`
- ConstraintLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - ImageView
  - TextView `#onnametvid` text="OS Name"
  - TextView `#deviceidtvid` text="ID"
  - TextView text="VENUS SOLUTIONS"
  - TextView text="9790070227, 9655842840, 9487792840"
  - EditText `#applanguage_id` text="Choose Language"
  - Button `#btnraisebranchrequest` text="Raise Request for Branch Level Access"
  - TextView `#deviceinfo_licenseholder` text="This Licence valid upto "
  - TextView `#deviceinfo_appversion` text="APP Version 9.1 released on 10-10-2024"
  - TextView `#deviceinfo_dbidentification` text="User Reference No:6007/2019"
  - ProgressBar `#deviceidentity_pbarid` visibility=gone

## `activity_directcollectionentry.xml`
- Inflated by: `directcollectionentry`
- ConstraintLayout `#constraintLayout`
  - ScrollView
    - LinearLayout
      - LinearLayout
        - LinearLayout
          - ImageView `#dcol_thumbnail`
          - LinearLayout
            - TextView `#dcol_partynameid` text="Party Name" / ta "பார்ட்டி பெயர்"
            - TextView `#dcol_tagid` text="TAG"
            - TextView `#dcol_remarksid` text="REMARKS"
      - LinearLayout
        - LinearLayout
          - TextView text="Loan Amount"
          - TextView text=":"
          - TextView `#dcol_loanamountid`
        - LinearLayout
          - TextView text="Interest/Docoument"
          - TextView text=":"
          - LinearLayout
            - TextView `#dcol_interestamountid` text=" "
            - TextView `#dcol_documentcharge`
        - LinearLayout
          - TextView text="Date"
          - TextView text=":"
          - TextView `#dcol_loandateid`
        - LinearLayout
          - TextView text="Closing Date"
          - TextView text=":"
          - LinearLayout
            - TextView `#dcol_closingdateid`
            - TextView `#dcol_overduedays`
        - LinearLayout
          - TextView text="Due"
          - TextView text=":"
          - TextView `#dcol_dueamountid`
        - LinearLayout `#tabbedindloancolentry_collectedamountlayoutid`
          - TextView `#dcol_collectedamountlabel` text="Collected" / ta "வசூலிக்கப்பட்டது"
          - TextView text=":"
          - TextView `#dcol_prevcollectedamountid`
        - LinearLayout `#tabbedindloancolentry_pendingamountlayoutid`
          - TextView `#dcol_pendingamountlabel` text="Pending"
          - TextView text=":"
          - TextView `#dcol_balanceid`
        - LinearLayout `#tabbedindloancolentry_loancollectionstatuslayoutid`
          - TextView `#dcol_lblstatusid` text="Status"
          - TextView text=":"
          - TextView `#dcol_statusid`
          - TextView `#dcol_additionaldue`
        - LinearLayout `#dcol_productlytid` visibility=gone
          - TextView text="Products"
          - LinearLayout
            - TextView `#invtransdetailsrow_snoid` text="SN "
            - TextView text="Product"
            - TextView text="Qty"
            - TextView `#invtransdetailsrow_priceid` text="Price"
            - TextView `#invtransdetailsrow_totalamountid` text="Amount  "
          - LinearLayout
            - RelativeLayout
              - RecyclerView `#precycler_view`
        - LinearLayout `#newloancreation_datelayoutid`
          - TextView `#ds_fromdatelabelid` text="Date" / ta "தேதி"
          - TextView text=":"
          - TextView `#newloancreation_loandateid`
        - LinearLayout `#dcol_lytcurcollectionamountid`
          - TextView text="Loan Due Collection"
          - EditText `#dcol_curcollectionamountid` inputType=number
        - LinearLayout visibility=gone
          - TextView `#dcol_penaltylabeltv` text="Pen/Int Collection"
          - EditText `#dcol_curcollectionpenaltyamountid` inputType=number
        - LinearLayout visibility=gone
          - TextView `#dcol_savingslabeltv` text="Savings Collection"
          - EditText `#dcol_curcollectionsavingsamountid` inputType=number
        - LinearLayout visibility=gone
          - TextView text="Collected as/ In"
          - EditText `#dcol_curTargetLedgerId` inputType=number
        - LinearLayout visibility=visible
          - TextView `#dcol_cashcollectedlabeltv` text="Cash Collection"
          - EditText `#dcol_cashcollectionid` inputType=number
        - LinearLayout visibility=visible
          - TextView `#dcol_bankcolllabeltv` text="Bank Collection"
          - EditText `#dcol_bankcollectionid` inputType=number
      - LinearLayout visibility=visible
        - Button `#dcol_savebuttonid` text="SAVE"
        - Button text="PRINT"
      - TextView text="Collection Details"
      - LinearLayout `#row3`
        - TextView text="Date"
        - TextView text="Paid"
        - TextView text="Balance"
        - TextView `#selectedloancolllbl_savingscollectedamountid` text="Savings"
        - TextView `#selectedloancolllbl_interestcollectedamountid` text="Interest"
      - RecyclerView `#recyclerviewcollectiomndetailsid`
  - ProgressBar `#dcparid` visibility=gone

## `activity_documentsearchlist.xml`
- Inflated by: `documentsphotossearchlist`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - RecyclerView `#recycler_view`
  - ProgressBar `#progress_bar` visibility=gone
  - TextView `#status_text` visibility=gone

## `activity_documentupload.xml`
- Inflated by: `documentsupload`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - RelativeLayout
        - ImageView `#docupload_groupphotoimageviewid`
        - CircularImageView `#docupload_img_plus` visibility=visible
      - TextView text="Remarks"
      - EditText `#docupload_remarks` inputType=text
      - Button `#btnsavenewareaid` text="Save and Upload"
  - ProgressBar `#newcollarea_progressBar` visibility=gone
  - TextView `#collareacreation_txtPercentage` text="dfhfhf" visibility=gone

## `activity_downloaddata.xml`
- Inflated by: `downloaddata`
- LinearLayout
  - TextView `#tvRouteNameid` text="Line Name"
  - TextView text="Collection Executive-1" / ta "வசூலுக்கு செல்பவர்-1"
  - EditText `#downloaddata_etexecutive1id` inputType=none
  - TextView text="Collection Executive-2" / ta "வசூலுக்கு செல்பவர்-2"
  - EditText `#downloaddata_etexecutive2id` inputType=none
  - TextView text="Collection Date" / ta "வசூல் தேதி"
  - TextView `#dcl_collectiondateid` text="01-06-2001"
  - TextView `#dcl_collectiondayid` text="WEDNESDAY" / ta "புதன்"
  - CheckBox `#dcl_enableoverallcollectionlistid` text="Overall Collection List"
  - Button `#btnDownload` text="Download"

## `activity_dueschdule.xml`
- Inflated by: `dueschdule`
- ConstraintLayout `#main`
  - LinearLayout
    - LinearLayout
      - TextView `#duelistrow_duesnoid` text="Due No"
      - TextView `#duelistrow_duedateid` text="Due Date"
      - TextView `#duelistrow_totaldueamount` text="Due Amount"
    - RecyclerView `#duelistrecyclerviewid`

## `activity_editingcollections.xml`
- Inflated by: `editing_collections_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - include layout=@layout/customersearchlist
  - LinearLayout
    - TextView `#lastupdatelbl`
    - TextView text="Total: "
    - TextView `#editingcollection_totalid`
    - TextView `#editingcollection_penaltytotalid` text="1,00,000" visibility=gone
    - TextView `#editingcollection_savingstotalid` text="1,00,000" visibility=gone
  - FloatingActionButton `#editingcollection_updatecollection` visibility=gone
  - ProgressBar `#collectionentry_pbarid` visibility=gone
  - TextView `#collectionentry_progresstextid` visibility=gone

## `activity_editingfullcollections.xml`
- Inflated by: `editing_collectionswithfulloptions_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - include layout=@layout/customersearchlist
  - LinearLayout
    - TextView `#lastupdatelbl`
    - TextView text="Total:"
    - TextView `#editingcollection_totalid`
    - TextView `#editingcollection_penaltytotalid` text="1,00,000" visibility=gone
    - TextView `#editingcollection_savingstotalid` text="1,00,000" visibility=gone
  - FloatingActionButton `#editingcollection_updatecollection`
  - ProgressBar `#collectionentry_pbarid` visibility=gone
  - TextView `#collectionentry_progresstextid` visibility=gone

## `activity_employeesearch.xml`
- Inflated by: `accledgers_searchlist`, `branchemployeelist`, `branchemployeesaccessearchlist`, `branchemployeesearchlist`, `closableloanssearchlistact`, `closablesavings_list`, `collectionreportprintingmethods_searchlist`, `employeesaccessearchlist`, `employeesearchlist`, `expensesentry_searchlist`, `itemmaster_list`, `loanmethods_searchlist`, `partydetails_searchlist`, `stockUOM_list`, `stockcat_list`, `stockgrp_list`, `stockinhand_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - FrameLayout
    - include `#content_layout` layout=@layout/customersearchlist
    - FloatingActionButton `#addnewemployeeid`
    - FrameLayout `#progress_Overlay` visibility=gone
      - ProgressBar `#capbarid`

## `activity_expensesentry.xml`
- Inflated by: `expensesandreceipt_offlineentry`
- RelativeLayout `#main`
  - LinearLayout
    - TextView `#tvledgernamelabelid` text="Expenses Account" / ta "செலவு கணக்கு"
    - TextView `#tvledgernamedispid`
    - TextView `#tvamountlabelid` text="Exepenses Amount" / ta "செலவு தொகை"
    - EditText `#etamountid` inputType=number
    - TextView text="Notes" / ta "குறிப்பு"
    - EditText `#etnarratiobid` inputType=text
    - TextView text="Paid as / in"
    - EditText `#etsourceledgerid` inputType=text
    - Button `#btnsaveexpid` text="Update" / ta "சேமி"
    - Button `#btndeleteexpid` text="DELETE" / ta "நீக்கு"

## `activity_financelinesaddandedit.xml`
- Inflated by: `financelinesaddandedit`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="Line Name"
      - EditText `#financedetails_etfinnameid`
      - TextView text="Mailing Name"
      - EditText `#financedetails_etfinmailingnameid`
      - TextView text="Add Line1"
      - EditText `#financedetails_etaddline1id`
      - TextView text="Add Line2"
      - EditText `#financedetails_etaddline2id`
      - TextView text="City"
      - EditText `#financedetails_etcityid`
      - TextView text="Pincode"
      - EditText `#financedetails_etpincodeid`
      - TextView text="Registration No"
      - EditText `#financedetails_etregistrationnoid`
      - TextView text="Short Name"
      - EditText `#financedetails_etshortnameid`
      - TextView text="Admin Mobile Nos"
      - EditText `#financedetails_etadminmobilenosid`
      - TextView text="Default Loanmethod"
      - EditText `#financedetails_etdefaultloanmethodidid`
      - TextView text=" Default Bank Name"
      - EditText `#financedetails_etdefaultbankledgersid`
      - TextView text="Email Identifier"
      - EditText `#financedetails_etemailidentifierid`
      - TextView text="SMS Identifier"
      - EditText `#financedetails_etsmsidentifierid`
      - Button `#financedetails_savebuttonid` text="UPDATE"
  - ProgressBar `#financedetails_progressbarid` visibility=gone

## `activity_financewisepartners.xml`
- Inflated by: `financewisepartners_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - ScrollView
    - LinearLayout
      - LinearLayout
        - TextView text="Finance"
        - EditText `#financewisepartners_financenameid`
      - RecyclerView `#partieslist_recycler_view`
  - ProgressBar `#financewisepartners_pbarid` visibility=gone
  - Button `#financewisepartners_savebuttonid` text="UPDATE"

## `activity_fullimageview.xml`
- Inflated by: `fullimageview`
- ConstraintLayout `#main`
  - ZoomageView `#imgfacebook`

## `activity_handloanslist.xml`
- Inflated by: `handloans_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - LinearLayout `#partyledgerslist_columnheaderid`
        - TextView text="Party"
        - TextView `#handloansrow_finnameid` text="FIN"
        - TextView text="Receivable"
        - TextView text="Payable"
    - RecyclerView `#acctranslist_recycler_view`
  - ProgressBar `#acctranslist_pbar` visibility=gone

## `activity_image_picker.xml`
- Inflated by: `ImagePickerActivity`
- ConstraintLayout

## `activity_interestcalculationandpayments.xml`
- Inflated by: `stloaninterestcalculationandpayments`
- CoordinatorLayout
  - AppBarLayout
    - Toolbar `#toolbar`
  - ScrollView
    - LinearLayout
      - LinearLayout
        - TextView `#interestcalculationandpayments_partynameid` text="PARTY NAME"
        - TextView `#interestcalculationandpayments_addline1id`
        - TextView `#interestcalculationandpayments_addline2id`
      - LinearLayout `#interestcalculationandpayments_loandetailslayoutid`
        - TextView text="LOANS DETAILS"
        - LinearLayout
          - TextView text="FIN"
          - TextView text="No"
          - TextView text="Date"
          - TextView text="Amount"
          - TextView text="Repaid"
          - TextView text="Balance"
          - TextView text="Status"
        - RecyclerView `#recycler_view`
        - LinearLayout
          - TextView `#individualloanordepositdetailsrow_prevreceiptslinkid` text="PREV RECEIPTS"
          - TextView `#individualloanordepositdetailsrow_totalloanbalanceid` text="TOTAL"
      - LinearLayout `#interestcalculationandpayments_prevreceiptslayoutid`
        - TextView text="PREVIOUS RECEIPTS"
        - LinearLayout
          - TextView `#prevreceiptsrow_snoid` text="SNo"
          - TextView `#prevreceiptsrow_transdateid` text="Date"
          - TextView text="Principal"
          - TextView `#prevreceiptsrow_interestid` text="Interest"
        - RecyclerView `#previousreceipts_recycler_view`
        - LinearLayout
          - TextView `#individualloanordepositdetailsrow_loandetailslinkid` text="LOAN DETAILS"
      - TextView text="INTEREST COLLECTION"
      - LinearLayout
        - TextView text="Date : "
        - TextView `#interestcalculationandpayments_transdatetv` text="10-11-2020"
        - TextView text="Interest upto :"
        - TextView `#interestcalculationandpayments_interestcalculationuptodatetv` text="31-10-2020"
        - ImageView `#interestcalculationandpayments_interestcalcbuttonid`
      - LinearLayout
        - TextView text="act"
        - TextView text="PI"
        - TextView text="Loan"
        - TextView text="from"
        - TextView text="upto"
        - TextView text="days"
        - TextView text="int"
        - TextView text="Received"
      - RecyclerView `#interestcalculationandpayments_intcolid`
      - TextView `#individualloanordepositdetailsrow_totalcollectableid` text="TOTAL"
      - LinearLayout
        - Button `#interestcalculationandpayments_savebutton` text="UPDATE"
        - Button `#interestcalculationandpayments_printbutton` text="PRINT"
  - ProgressBar `#interestcalculationandpayments_pbarid` visibility=gone

## `activity_interestcalculationdetails.xml`
- Inflated by: `ofloan_interestcalculationdetails`, `stloan_interestcalculationdetails`
- ConstraintLayout
  - ScrollView
    - LinearLayout
      - LinearLayout
        - TextView text="Row Type"
        - EditText `#interestcollectiondetails_etptypeid`
      - LinearLayout
        - TextView text="Finance"
        - EditText `#interestcollectiondetails_etshortnameid`
      - LinearLayout
        - TextView text="Party"
        - EditText `#interestcollectiondetails_etledgernameid`
      - LinearLayout
        - TextView text="Loan No"
        - EditText `#interestcollectiondetails_etfwudstloanidid`
      - LinearLayout
        - TextView text="Loan Amount"
        - EditText `#interestcollectiondetails_etloanid`
      - LinearLayout `#interestcollectiondetails_principalamountlayoutid`
        - TextView text="Interest Paid for"
        - EditText `#interestcollectiondetails_interestpaid_etprincipalamountid` inputType=numberSigned
      - LinearLayout `#interestcollectiondetails_intratelayoutid`
        - TextView text="Int Rate"
        - EditText `#interestcollectiondetails_etintrateid` inputType=numberDecimal
      - LinearLayout visibility=gone
        - TextView text="S.No"
        - EditText `#interestcollectiondetails_etsnoid`
      - LinearLayout `#interestcollectiondetails_intfromdatetlayoutid`
        - TextView text="Int from"
        - EditText `#interestcollectiondetails_etintfromid`
      - LinearLayout `#interestcollectiondetails_intuptolayoutid`
        - TextView text="Int upto"
        - EditText `#interestcollectiondetails_etintuptoid`
      - LinearLayout `#interestcollectiondetails_dayslayoutid`
        - TextView text="Days"
        - EditText `#interestcollectiondetails_etdaysid`
      - LinearLayout `#interestcollectiondetails_calcintlayoutid`
        - TextView text="Calc Int"
        - EditText `#interestcollectiondetails_etcalcintid`
      - LinearLayout
        - TextView text="Paid"
        - LinearLayout
          - EditText `#interestcollectiondetails_etreceivedid` inputType=numberSigned
          - Button `#interestcollectiondetails_btnreversecalculationid` text="Reverse Calculation"
      - LinearLayout
        - TextView text="Paid as /in "
        - EditText `#interestcollectiondetails_ettargetledgeridid`
      - LinearLayout
        - TextView text="Narration "
        - EditText `#interestcollectiondetails_etnarrationid`
      - Button `#interestcollectiondetails_savebuttonid` text="SAVE"

## `activity_invtrans_rowdetails.xml`
- Inflated by: `invtrans_rowdetails`
- CoordinatorLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="S.No" visibility=gone
      - EditText `#boxdetails_etsnoid` visibility=gone
      - TextView text="Product"
      - EditText `#boxdetails_productnameetid` inputType=numberSigned
      - TextView text="Qty"
      - EditText `#boxdetails_etqtyid` inputType=numberSigned
      - TextView `#invrow_pricelabelid` text="Price"
      - EditText `#boxdetails_etpriceid` inputType=numberSigned
      - TextView `#invrow_totallabelid` text="Total Amount"
      - EditText `#boxdetails_ettotalamount` inputType=numberDecimal
      - Button `#boxdetails_savebuttonid` text="CHANGE"
      - Button `#boxdetails_deletebuttonid` text="REMOVE"

## `activity_invtransactnew.xml`
- Inflated by: `invtransactnew`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - LinearLayout
        - TextView `#receiptnote_invoicenoid` text="No"
        - EditText `#receiptnote_invoicenoetid` inputType=number
        - TextView `#invtransactions_transdateid` text="Date" / ta "தேதி"
      - TextView `#nametextid` text="Party"
      - EditText `#receiptnote_partynameid`
    - LinearLayout
      - include layout=@layout/invtransdetails_row
    - LinearLayout
      - RecyclerView `#recycler_view`
    - LinearLayout
      - LinearLayout
        - TextView text="Total" / ta "ஆக மொத்தம்"
        - TextView `#invtransdetails_totalamountid`
      - LinearLayoutCompat
        - Button `#invtransdetails_savebuttonid` text="Update" / ta "சேமி"
        - Button `#invtransdetails_deletebuttonid` text="DELETE" / ta "நீக்கு"
  - FloatingActionButton `#receiptnote_fab` contentDescription="add new item"
  - ProgressBar `#receiptnote_pbarid` visibility=gone

## `activity_itemmaster.xml`
- Inflated by: `itemmaster_activity`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="Item Name"
      - EditText `#stockcat_itemnameid`
      - TextView text="UOM"
      - EditText `#stockcat_stockuomid`
      - TextView text="Stock Group"
      - EditText `#stockcat_stockgrpid`
      - TextView text="Stock Cat"
      - EditText `#stockcat_Stockcatid`
      - TextView text="Reorder Level "
      - EditText `#stockcat_reorderid` inputType=numberSigned
      - TextView text="Minimum Order Qty "
      - EditText `#stockcat_minimumorderid` inputType=numberSigned
      - Button `#newaccled_submitid` text="UPDATE"
      - Button `#newaccled_deleteid` text="DELETE"
  - ProgressBar `#newaccled_pbarid` visibility=gone

## `activity_ledgerstatmentsdateselection.xml`
- Inflated by: `ledgerstatmentsdateselection`
- RelativeLayout `#main`
  - AppBarLayout `#appBar`
    - Toolbar `#toolbar`
  - LinearLayout `#newloancreation_datelayoutid`
    - TextView text="From Date" / ta "ஆரம்ப தேதி"
    - TextView text=":"
    - TextView `#ds_fromdateid` text="01-06-2001"
  - LinearLayout `#row2`
    - TextView text="To Date" / ta "நிறைவு தேதி"
    - TextView text=":"
    - TextView `#ds_todateid` text="01-06-2001"
  - TextView `#label` text="Accounts Ledgers" / ta "வரவு செலவு கணக்குகள்"
  - LinearLayout
    - include layout=@layout/customersearchlist

## `activity_loanclosoureentry.xml`
- Inflated by: `loanclosoureentry`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - MaterialCardView
        - LinearLayout `#partyandloandetailsid`
          - TextView text="PARTY DETAILS"
          - TextView `#loanclose_partynameid`
          - TextView `#loanclose_collectionareanameid`
      - MaterialCardView
        - LinearLayout
          - TextView text="LOAN DETAILS"
          - LinearLayout
            - LinearLayout
              - TextView text="Loan Date"
              - TextView `#loanclose_loandateid`
            - LinearLayout
              - TextView text="Loan Amount" / ta "கடன் தொகை"
              - TextView `#loanclose_collectableid`
      - MaterialCardView
        - LinearLayout
          - LinearLayout
            - TextView text="Collected Amount" / ta "வசூலான தொகை"
            - TextView text=":"
            - TextInputEditText `#loanclose_collectedid`
          - LinearLayout
            - TextView text="Extra Collection" / ta "கூடுதல் வசூல்"
            - TextView text=":"
            - TextInputEditText `#loanclose_extracollectedid` inputType=numberDecimal
          - LinearLayout
            - TextView text="Collection Loss" / ta "வசூல் குறைவு"
            - TextView text=":"
            - TextInputEditText `#loanclose_vasoolkuraivuid` inputType=numberDecimal
          - LinearLayout
            - TextView text="Closed Date" / ta "முடிவு தேதி"
            - TextView text=":"
            - TextInputEditText `#loanclose_closedondateid` inputType=date
      - Button `#loanclose_newbuttonid` text="NEW" visibility=gone
      - Button `#loanclose_savebuttonid` text="Update" / ta "சேமி"
      - Button `#loanclose_deletebuttonid` text="DELETE" / ta "நீக்கு"

## `activity_loanledgerslist.xml`
- Inflated by: `loanledgers_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - TextView text="FINANCE"
      - TextView text=":"
      - TextView `#partyledgerslist_financenamesid`
    - LinearLayout
      - TextView `#periodorloannoid` text="PERIOD"
      - TextView text=":"
      - TextView `#partyledgerslist_periodid`
    - LinearLayout
      - LinearLayout `#partyledgerslist_columnheaderid`
        - TextView text="S.No"
        - TextView text="Date" / ta "தேதி"
        - TextView `#partyledgersrow_interestid` text="Party/No"
        - TextView text="IR"
        - TextView text="Return"
        - TextView `#partyledgerlist_loanordepositcolumnheaderid` text="Balance"
    - RecyclerView `#acctranslist_recycler_view`
  - ProgressBar `#acctranslist_pbar` visibility=gone

## `activity_loanmethods.xml`
- Inflated by: `loanmethods`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="SNo" / ta "வரிசை எண்"
      - EditText `#loanmethods_etshowidid` inputType=numberSigned
      - TextView text="Loan Type" / ta "கடன் வகை"
      - EditText `#loanmethods_etloanmethoddescid`
      - TextView text="Loan Amount" / ta "கடன் தொகை"
      - EditText `#loanmethods_etloanamountid` inputType=numberSigned
      - TextView text="Loan Given in Hand" / ta "கையில் தரும் தொகை"
      - EditText `#loanmethods_etloanamountgivenid` inputType=numberSigned
      - TextView text="Interest Added" / ta "வட்டி கூடுதல்" visibility=gone
      - EditText `#loanmethods_etintadditionid` visibility=gone
      - TextView text="Interest Deduction" / ta "வட்டி பிடித்தம்"
      - EditText `#loanmethods_etintdeductionid` inputType=numberSigned
      - TextView text="Total Collectable" / ta "மொத்த வசூல் தொகை"
      - EditText `#loanmethods_ettotamountcollectableid` inputType=numberSigned
      - TextView text="Collection Break" / ta "வசூல் இடைவெளி"
      - EditText `#loanmethods_etcollectionbreakid`
      - TextView text="Collection Breaks to be added" / ta "கூட்ட வேண்டிய இடைவெளிகள்"
      - EditText `#loanmethods_etcbcountid` inputType=numberSigned
      - TextView text="Total Installments" / ta "மொத்த தவணைகள்"
      - EditText `#loanmethods_etnoofinsid` inputType=numberSigned
      - TextView text="Installment Amount" / ta "தவணை தொகை"
      - EditText `#loanmethods_etinsamountid` inputType=numberSigned
      - TextView text="Warning Days" / ta "எச்சரிக்கை நிறமாற்ற நாட்கள்"
      - EditText `#loanmethods_etwarningdaysid` inputType=numberSigned
      - TextView text="Grace Period" / ta "சலுகைக்காலம்"
      - EditText `#loanmethods_etgraceperiodid`
      - TextView text="Document Charges Fixation" / ta "எழுத்துக் கூலி நிர்ணய முறை"
      - EditText `#loanmethods_etdocchargesmethodid`
      - TextView text="Document Charge" / ta "எழுத்துக் கூலி"
      - EditText `#loanmethods_etdocchargesid` inputType=numberSigned
      - TextView text="Abstract" / ta "சுருக்கம்"
      - EditText `#loanmethods_etreportidentificationid`
      - CheckBox `#loanmethods_chkisvisibleid` text="Status" / ta "பயன்பாட்டு நிலை"
      - TextView text="Additional Due Amount" / ta "துணைத் தொகை"
      - EditText `#loanmethods_etduesupportamountid` inputType=numberSigned
      - Button `#loanmethods_savebuttonid` text="SAVE"
      - Button `#loanmethods_deletebuttonid` text="DELETE"
  - ProgressBar `#loanmethods_progressbarid` visibility=gone

## `activity_loanpartyledgers_parameters.xml`
- Inflated by: `partyledgers_parameters`
- ConstraintLayout `#main`
  - LinearLayout
    - TextView `#label` text="Select Finances"
    - RecyclerView `#partyledgersparameters_finanelistrecyclerviewid`
    - TextView `#partyledgersparameters_selectperiodlabelid` text="Select Period"
    - LinearLayout `#partyledgersparameters_fromdatelayoutid`
      - TextView `#partyledgersparameters_fromdatelabelid` text="ஆரம்ப தேதி"
      - TextView text=": "
      - EditText `#partyledgersparameters_fromdateid` text="06-Jun-2020"
    - LinearLayout `#partyledgersparameters_todatelayoutid`
      - TextView `#partyledgersparameters_todatelabelid` text="நிறைவு தேதி"
      - TextView text=": "
      - EditText `#partyledgersparameters_todateid` text="06-Jun-2020"
    - LinearLayout `#partyledgerparameters_partyselectionid`
      - TextView `#partyledgersparameters_partytypeid` text="Select Party"
      - EditText `#partyledgersparameters_LedgerNameid`
    - Button `#partyledgersparameters_showbuttonid` text="SHOW"
  - ProgressBar `#partyledgersparameters_pbarid` visibility=gone

## `activity_maps.xml`
- Inflated by: `maps_collectionareadisplay`
- RelativeLayout `#main`
  - fragment `#map`

## `activity_maps_partydisplay.xml`
- Inflated by: `maps_partydisplay`
- RelativeLayout `#main`
  - fragment `#map`

## `activity_meetingreport.xml`
- Inflated by: `meetingreport`
- ConstraintLayout `#main`
  - LinearLayout
    - TextView `#meetingreports_includedfinanceinfoid`
    - ExpandableListView `#lvExp`
    - TextView `#meetingreports_lastrefreshinfoid`
  - ProgressBar `#meetingreport_pbarid` visibility=gone
  - TextView `#meetingreport_progresstextid` text="progress" visibility=gone

## `activity_mobiledeviceaccesscontrol.xml`
- Inflated by: `mobiledeviceaccescontrol_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - FrameLayout
    - LinearLayout
      - LinearLayout
        - TextView text="Android Device Id"
        - LinearLayout
          - EditText `#mdac_imeiid`
          - Button `#mobiledeviceaccesscontrol_getGIbuttonid` text="GI"
        - TextView text="Employee"
        - EditText `#mdac_employeeetid`
        - TextView text="User Role"
        - EditText `#mdac_userroleetid`
        - CheckBox `#mdac_activeid` text="Active"
      - TextView text="Allowed Lines" / ta "அனுமதிக்கப்பட்ட பைனான்ஸ்கள்"
      - RelativeLayout
        - RecyclerView `#recycler_view`
        - Button `#btnsavelinemobileaccess` text="SAVE"
        - Button `#btndeletelinemobileaccess` text="DELETE"
    - LinearLayout `#progress_overlay` visibility=gone
      - ProgressBar `#progress_bar`
      - TextView text="Please wait..."

## `activity_nadapunippucollectionlist.xml`
- Inflated by: `dailypatti_kadanpatrulist_act`, `dailypatti_nadapunippucollectionlist_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - include layout=@layout/customersearchlist

## `activity_nadapunippuphakkilist.xml`
- Inflated by: `dailypatti_nadapunippuphakkilist_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - include layout=@layout/customersearchlist

## `activity_navigationdrawactivity.xml`
- Inflated by: `navigationdrawactivity`
- DrawerLayout `#drawer_layout`
  - CoordinatorLayout
    - AppBarLayout `#appbar`
      - include layout=@layout/addnewloanbar
    - RelativeLayout
      - LinearLayout
        - RecyclerView `#recycler_view`
        - TextView `#downloaddateidnew` text="Data Download time"
  - NavigationView `#nav_view`

## `activity_newacccountsledgers.xml`
- Inflated by: `accountledgerdetails`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="Ledger Name"
      - EditText `#newaccled_ledgernameid`
      - TextView text="Accounts Group"
      - EditText `#newaccled_undergroupnameid`
      - LinearLayout `#newaccled_financenamelyt`
        - TextView text="Ledger of the Finance"
        - EditText `#newaccled_financeledgerid`
      - CheckBox `#newaccountledgers_chktreatasdefaultexpebses` text="Treat as Executive level Ledger"
      - Button `#newaccled_submitid` text="Update" / ta "சேமி"
      - Button `#newaccled_deleteid` text="DELETE" / ta "நீக்கு"
  - ProgressBar `#newaccled_pbarid` visibility=gone

## `activity_newcollectionareacreation.xml`
- Inflated by: `collectionareadetails_act`
- RelativeLayout `#main`
  - ScrollView
    - LinearLayout
      - LinearLayout
        - TextView `#labelcollectionareanameid` text="Collection Area Name" / ta "வசூல் ஏரியா பெயர்"
        - EditText `#etareanameod`
        - LinearLayout `#collectiondaylayoutid`
          - TextView `#tcollectiondayslabelid` text="Collection Day" / ta "வசூல் நாள்"
          - EditText `#collectionareacreation_collectiondayid`
        - CheckBox `#chkcollectionarea_statusid` text="ACTIVE"
        - LinearLayout `#locationlayoutid`
          - Button `#btngetcoOrdid`
          - LinearLayout
            - TextView `#latid`
            - TextView `#lngid`
      - Button `#btnsavenewareaid` text="Update" / ta "சேமி"
      - Button `#btndeleteareaid` text="DELETE" / ta "நீக்கு"
  - ProgressBar `#newcollectionarea_pbarid` visibility=gone

## `activity_newemployee.xml`
- Inflated by: `branch_employee`, `employeedetails_act`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="Employee Name"
      - EditText `#newemp_employeenameid`
      - TextView text="Address Line 1"
      - EditText `#newemp_addline1id`
      - TextView text="Address Line 2"
      - EditText `#newemp_addline2id`
      - TextView text="City "
      - EditText `#newemp_cityid`
      - TextView text="Pincode "
      - EditText `#newemp_Pincodeid` inputType=number
      - TextView text="Mobile No 1 "
      - EditText `#newemp_mobileno1id`
      - TextView text="Mobile No 2 "
      - EditText `#newemp_mobileno2id` inputType=number
      - TextView text="EMail "
      - EditText `#newemp_emailid`
      - LinearLayout `#newemp_financenamelyt` visibility=gone
        - TextView text="Ledger for the finance"
        - EditText `#newaccled_finnameid`
      - CheckBox `#newemp_activecheckboxid` text="Active"
      - Button `#newemp_submitid` text="Update" / ta "சேமி"
      - Button `#newemp_deletebtnid` text="DELETE" / ta "நீக்கு"
  - ProgressBar `#newemp_pbarid` visibility=gone

## `activity_newloandetails.xml`
- Inflated by: `newloancreation`
- RelativeLayout `#main`
  - TextView `#tvpartyname` text="Party" / ta "பார்ட்டி"
  - TextView `#newloan_tvtagid` text="tag"
  - View
  - LinearLayout `#newloancreation_datelayoutid`
    - TextView `#ds_fromdatelabelid` text="Date" / ta "தேதி"
    - TextView text=":"
    - TextView `#newloancreation_loandateid`
  - LinearLayout `#loanmethodlayoutid`
    - LinearLayout
      - TextView `#tvloanmethod` text="Loan Method"
      - EditText `#newloancreation_loanmethodid` hint="Select Loan Method" inputType=none
    - LinearLayout `#newloancreation_collectiondaylayoutid`
      - TextView `#tcollectiondayslabelid` text="Collection Day" / ta "வசூல் நாள்"
      - EditText `#newloancreation_collectiondayid` inputType=none
    - Spinner visibility=gone
  - ScrollView
    - LinearLayout
      - TextView visibility=gone
      - LinearLayout `#newloan_lytloanindexid` visibility=gone
        - TextView text="Loan Index"
        - EditText `#newloan_etloanindexid` inputType=number
      - LinearLayout `#newloancreation_productlytid`
        - TextView text="Products"
        - LinearLayout
          - TextView `#invtransdetailsrow_snoid` text="SN "
          - TextView text="Product"
          - TextView text="Qty"
          - TextView `#invtransdetailsrow_priceid` text="Price"
          - TextView `#invtransdetailsrow_totalamountid` text="Amount  "
        - LinearLayout
          - RelativeLayout
            - RecyclerView `#recycler_view`
            - FloatingActionButton `#receiptnote_fab` contentDescription="add new item"
        - TextView `#totalamounttv` text="Total: " visibility=visible
        - LinearLayout
          - LinearLayout
            - TextView text="Advance"
            - EditText `#etadvanceamount` inputType=number
          - LinearLayout
            - TextView text="Balance"
            - EditText `#etbalanceamount` inputType=number
          - LinearLayout
            - TextView text="Int Rate"
            - EditText `#etinterestrate` inputType=numberDecimal
      - TextView text="Loan Amount" / ta "கடன் தொகை"
      - EditText `#etloanamountid` inputType=number
      - TextView text="Interest Amount" / ta "வட்டி"
      - EditText `#etinterestid` inputType=number
      - TextView text="Document Charges" / ta "ஆவண கட்டணம்"
      - EditText `#etdocchargesid` inputType=number
      - TextView text="Loan in Hand" / ta "கையில் தரும் தொகை"
      - EditText `#ethandamountid` inputType=number
      - TextView text="Total Collectable" / ta "வசூலிக்கப்படவேண்டிய தொகை"
      - EditText `#etmclcollectableid` inputType=number
      - TextView text="No Of Instalments" / ta "தவனைகள்"
      - LinearLayout
        - EditText `#etduesid` inputType=number
        - Button `#showschbtnid` text="Show Schedule" visibility=gone
      - TextView text="Instalment Amount" / ta "தவனை தொகை"
      - EditText `#etdueAmountid` inputType=number
      - TextView `#newloancreation_ondaycollectionlabelid` text="On Day Collection"
      - EditText `#etondaycollection` inputType=number
      - TextView `#newloancreation_ondaysavingslabelid` text="On Day Savings Collection" visibility=gone
      - EditText `#etondaysavings` inputType=number visibility=gone
      - TextView `#tvremarksid` text="Remarks" / ta "குறிப்பு"
      - EditText `#etremarksid`
      - TextView `#tvmobilenoid` text="Mobile No"
      - EditText `#newloancreation_etmobilenoid` inputType=numberSigned
      - TextView text="Paid as/In"
      - EditText `#etloanamountpaidasid` inputType=none
      - TextView `#paymentrefreshlabelid` text="Payment Reference"
      - EditText `#etloanreferencedid` inputType=text
      - Button `#relateddocumentsbuttonid` text="Related Documents"
      - Button `#saveloanbuttonid` text="Update" / ta "சேமி"
      - Button `#deleteloanbuttonid` text="DELETE" / ta "நீக்கு"
  - ProgressBar `#newloancreation_progressbarid` visibility=gone

## `activity_newloanscollectionlist.xml`
- Inflated by: `editing_collectionlist_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout `#lytdaterangeid`
      - TextView text="From"
      - TextView text=": "
      - TextView `#loanlist_fromdateid` text="06-06-2020"
      - TextView text="To"
      - TextView text=": "
      - TextView `#loanlist_todateid` text="06-Jun-2020"
      - ImageButton `#loanslistrefreshid`
    - View
    - include layout=@layout/collectiondetails_row
    - View
    - include layout=@layout/customersearchlist

## `activity_newloanslist.xml`
- Inflated by: `ledgerstatement_act`, `loanslist_act`, `newloanslist_act`, `newpartieslist_act`, `newpartiesnewloanslist_act`, `newtodaycollectionofflinedata_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout `#lytselectlineid` visibility=gone
      - TextView text="Line Name"
      - TextView text="": ""
      - TextView
    - LinearLayout `#lytdaterangeid`
      - TextView text="From"
      - TextView text="": ""
      - TextView `#loanlist_fromdateid` text="01-06-2001"
      - TextView text="To"
      - TextView text="": ""
      - TextView `#loanlist_todateid` text="01-06-2001"
      - ImageButton `#loanslistrefreshid` contentDescription="Refresh List"
    - View
    - RecyclerView `#loansslist_recycler_view`
    - View
    - LinearLayout `#lyttotallytid` visibility=gone
      - TextView text="Total Loan"
      - TextView text="": ""
      - TextView `#loanlist_totalloanamounttv`
      - TextView text="Interest"
      - TextView text="": ""
      - TextView `#loanlist_totalinterestid`
      - TextView text="DC"
      - TextView text="": ""
      - TextView `#loanlist_totaldcid`
    - View visibility=gone
    - LinearLayout `#lyttotalcollectionlytid` visibility=gone
      - TextView text="Count:"
      - TextView text="": ""
      - TextView `#loanlist_noofloanstv`
      - TextView text="Collection"
      - TextView text="": ""
      - TextView `#loanlist_totalcollectonid`
      - TextView text="Balance"
      - TextView text="": ""
      - TextView `#loanlist_totalbalanceid`
    - View
  - FloatingActionButton `#newloan_fab` contentDescription="Add New"

## `activity_newpartycreation.xml`
- Inflated by: `partydetails_act`
- RelativeLayout `#main`
  - TextView `#twAreaName` text="Area Name"
  - ScrollView
    - LinearLayout
      - include layout=@layout/layout_toolbar_profile
      - LinearLayout `#newloancreation_noofloansallowedlyt` visibility=gone
        - TextView text="No of Loans Allowed"
        - EditText `#newloancreation_loanindexid` inputType=numberSigned
      - LinearLayout `#newloancreation_maximumamtlytid` visibility=gone
        - TextView text="Maximum Amount Allowed"
        - EditText `#newloancreation_maximumamtid` inputType=numberSigned
      - TextView `#newloancreation_partynolabelid` text="No"
      - EditText `#etPartyNoid` inputType=numberSigned
      - TextView text="Party Name" / ta "பார்ட்டி பெயர்"
      - EditText `#etPartyNameid`
      - TextView text="Business" / ta "தொழில்"
      - EditText `#etTag`
      - TextView text="Spouse Name" / ta "மனைவி/கணவர்/தந்தை பெயர்"
      - EditText `#newpartycreation_etSpouseNameid`
      - TextView text="Family Name"
      - EditText `#newpartycreation_etfamilynameid`
      - TextView text="House No/Street"
      - EditText `#newpartycreation_etaddress`
      - TextView text="Mobile No 1"
      - EditText `#etmobileno1` inputType=numberSigned
      - TextView text="Mobile No 2"
      - EditText `#etmobileno2` inputType=numberSigned
      - CheckBox `#isactivechkid` text="ACTIVE"
      - LinearLayout
        - Button `#btngetcoOrdid`
        - LinearLayout
          - TextView `#latid`
          - TextView `#lngid`
      - LinearLayout `#newpartycreation_idproofIlayoutid`
        - Button `#btnidprooofid` text="ID Proof-1"
        - ImageView `#idproofimageviewid` contentDescription="Id Proof Image"
      - LinearLayout `#newpartycreation_idproofIIlayoutid`
        - Button `#btnidprooofIIid` text="ID Proof-2"
        - ImageView `#idproofimageviewIIid` contentDescription="Id Proof Image 2"
      - Button `#btnsavenewparty` text="Update" / ta "சேமி"
      - Button `#btndeletepartyid` text="DELETE" / ta "நீக்கு"
  - TextView `#txtPercentage` visibility=gone
  - ProgressBar `#progressBar` visibility=gone

## `activity_newsevendayscollection.xml`
- Inflated by: `newsevendayscollection`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar` app:"FinSoft II" / ta "FinSoft"
  - ViewPager `#container`
  - FloatingActionButton `#fab`

## `activity_ofloan.xml`
- Inflated by: `ofloans`
- ConstraintLayout
  - ScrollView `#stloans_mainScrollView`
    - LinearLayout
      - TextView text="Party Name"
      - TextView `#ofloan_LedgerNameid`
      - TextView `#ofloan_partyaddline1id`
      - TextView `#ofloan_partyaddline2id`
      - LinearLayout `#interestcalculationandpayments_loandetailslayoutid`
        - TextView text="LOANS LIST "
        - LinearLayout
          - TextView text="FIN"
          - TextView text="No"
          - TextView text="Date"
          - TextView text="Amount"
          - TextView text="Repaid"
          - TextView text="Balance"
          - TextView text="Status"
        - RecyclerView `#recycler_view`
        - LinearLayout
          - ImageView `#stloans_addnewloanid`
          - TextView `#stloans_totalloanbalanceid` text="TOTAL"
      - TextView text="LOANS DETAILS"
      - TextView text="Finance"
      - EditText `#ofloan_financeid` inputType=numberSigned
      - TextView text="Partner Recommendation"
      - EditText `#ofloan_RecPartnerNameid`
      - TextView text="OF Loan No"
      - EditText `#ofloan_FWUDSTLoanSNoid` inputType=numberSigned
      - TextView text="Date"
      - EditText `#ofloan_DepositDateid`
      - TextView text="Amount"
      - EditText `#ofloan_STLoanAmountid` inputType=numberSigned
      - TextView `#ofloan_STLoanAmountinwordid`
      - TextView text="Daily Interest Rate"
      - EditText `#ofloan_STLIntRateinDMid` inputType=numberDecimal
      - TextView text="Monthly Interest Rate"
      - EditText `#ofloan_STLIntRateinMMid` inputType=numberDecimal
      - TextView text="Document Charges"
      - EditText `#ofloan_DocChargesid` inputType=numberSigned
      - TextView text="Interest From"
      - EditText `#ofloan_IntUptoDateid`
      - TextView text="Remarks"
      - EditText `#ofloan_Remarksid`
      - TextView text="Received as/in"
      - EditText `#ofloan_Paidasid`
      - TextView `#ofloans_banktransactiontypelabelid` text="Bank Transaction Type"
      - EditText `#ofloan_banktransactiontypeid`
      - TextView `#ofloans_BankRefNolabelid` text="Bank Ref No"
      - EditText `#ofloan_BankRefNoid`
      - Button `#ofloan_submitbuttonid` text="SUBMIT"
      - Button `#ofloan_printbuttonid` text="PRINT" visibility=gone
      - Button `#ofloan_deletebuttonid` text="DELETE"
  - ProgressBar `#ofloan_pbarid` visibility=gone

## `activity_ofloans_interestcalculationandpayments.xml`
- Inflated by: `ofloans_interestcalculationandpayments`
- CoordinatorLayout
  - AppBarLayout
    - Toolbar `#toolbar`
  - ScrollView
    - LinearLayout
      - LinearLayout
        - TextView `#interestcalculationandpayments_partynameid` text="PARTY NAME"
        - TextView `#interestcalculationandpayments_addline1id`
        - TextView `#interestcalculationandpayments_addline2id`
      - LinearLayout `#interestcalculationandpayments_loandetailslayoutid`
        - TextView text="LOANS DETAILS"
        - LinearLayout
          - TextView text="No"
          - TextView text="Date"
          - TextView text="FIN"
          - TextView text="Amount"
          - TextView text="Repaid"
          - TextView text="Balance"
          - TextView text="Status"
        - RecyclerView `#recycler_view`
        - LinearLayout
          - TextView `#individualloanordepositdetailsrow_prevreceiptslinkid` text="PREV RECEIPTS"
          - TextView `#individualloanordepositdetailsrow_totalloanbalanceid` text="TOTAL"
      - LinearLayout `#interestcalculationandpayments_prevreceiptslayoutid`
        - TextView text="PREVIOUS RECEIPTS"
        - LinearLayout
          - TextView `#prevreceiptsrow_snoid` text="SNo"
          - TextView `#prevreceiptsrow_transdateid` text="Date"
          - TextView text="Principal "
          - TextView `#prevreceiptsrow_interestid` text="Interest"
        - RecyclerView `#previousreceipts_recycler_view`
        - LinearLayout
          - TextView `#individualloanordepositdetailsrow_loandetailslinkid` text="LOAN DETAILS"
      - TextView text="INTEREST COLLECTION"
      - LinearLayout
        - TextView text="Date : "
        - TextView `#interestcalculationandpayments_transdatetv` text="10-11-2020"
        - TextView text="Interest upto :"
        - TextView `#interestcalculationandpayments_interestcalculationuptodatetv` text="31-10-2020"
        - ImageView `#interestcalculationandpayments_interestcalcbuttonid`
      - LinearLayout
        - TextView text="act"
        - TextView text="PI"
        - TextView text="Loan"
        - TextView text="from"
        - TextView text="upto"
        - TextView text="days"
        - TextView text="int"
        - TextView text="Paid"
      - RecyclerView `#interestcalculationandpayments_intcolid`
      - TextView `#individualloanordepositdetailsrow_totalcollectableid` text="TOTAL"
      - LinearLayout
        - Button `#interestcalculationandpayments_savebutton` text="UPDATE"
        - Button `#interestcalculationandpayments_printbutton` text="PRINT"
  - ProgressBar `#interestcalculationandpayments_pbarid` visibility=gone

## `activity_otpverification.xml`
- Inflated by: `otpverificationact`
- LinearLayout
  - TextView `#tv_otp_appinsid` text="Customer Id"
  - TextView text="Enter the OTP sent to your "
  - TextView `#tv_otp_mobile` text="Mobile: 9790070226"
  - TextView `#tv_otp_email` text="Email:"
  - LinearLayout
    - EditText `#et_otp_1` inputType=number
    - EditText `#et_otp_2` inputType=number
    - EditText `#et_otp_3` inputType=number
    - EditText `#et_otp_4` inputType=number
  - Button `#btn_submit_otp` text="Submit"
  - TextView `#tv_resend_otp` text="Resend OTP"
  - ProgressBar `#loading` visibility=gone

## `activity_partieslist.xml`
- Inflated by: `customers_list`, `fintrans_list`, `parties_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - RecyclerView `#partieslist_recycler_view`
  - ProgressBar `#partieslist_pbar` visibility=gone
  - FloatingActionButton `#partieslist_addnewpartyfabid`

## `activity_partyandpartnerdetailsnew.xml`
- Inflated by: `partyandpartnerdetails`
- ConstraintLayout `#main`
  - TextView `#newpartycreation_tasktypeid` text="Create New Party" visibility=gone
  - ScrollView
    - LinearLayout
      - include visibility=gone layout=@layout/layout_toolbar_profile
      - TextView text="Name"
      - LinearLayout
        - EditText `#partydetails_ledgernameid` inputType=text
      - TextView text="Tag"
      - EditText `#partydetails_tagid`
      - TextView text="Guardian Type "
      - EditText `#partydetails_gtypeid`
      - TextView text="Guardian Name"
      - EditText `#partydetails_guardiannameid`
      - TextView text="Address Line 1"
      - EditText `#partydetails_addline1id`
      - TextView text="Address Line 2"
      - EditText `#partydetails_addline2id`
      - TextView text="City/Area"
      - EditText `#partydetails_cityid`
      - TextView text="Pincode"
      - EditText `#partydetails_pincodeid` inputType=numberSigned
      - TextView text="Mobile No 1"
      - EditText `#partydetails_mobileno1id` inputType=numberSigned
      - TextView text="Mobile No 2"
      - EditText `#partydetails_mobileno2id` inputType=numberSigned
      - LinearLayout `#partydetails_masterpartylayoutid`
        - TextView text="Master Party"
        - LinearLayout
          - EditText `#partydetails_masterpartyledgeridid`
          - ImageView `#partydetails_removemasterpartyrecid`
      - LinearLayout `#partydetails_additonaldetailslayout`
        - TextView text="Landmark"
        - EditText `#partydetails_landmarkid`
        - TextView text="Age"
        - EditText
        - TextView text="Marital Status"
        - Spinner
        - TextView text="Profession"
        - EditText `#partydetails_professionid`
        - TextView text="Monthly Income"
        - EditText `#partydetails_monthlyincomeid`
        - TextView text="PAN"
        - EditText `#partydetails_pannoid`
        - TextView text="Aadhaar No"
        - EditText `#partydetails_aadhaarnoid`
        - TextView text="Guardian Contact No 1"
        - EditText `#partydetails_guardianmobileno1id`
        - TextView text="Guardian Contact No 2"
        - EditText `#partydetails_guardianmobileno2id`
        - TextView text="Guardian Profession"
        - EditText `#partydetails_guardianprofessionid`
        - TextView text="Guardian Monthly Income"
        - EditText `#partydetails_guardianmonthlyincomeid`
      - Button `#btnsavenewparty` text="Update" visibility=gone
      - Button `#btndeletepartyid` text="Delete" visibility=gone
  - ProgressBar `#partydetails_progressBar` visibility=gone
  - TextView `#partydetails_txtPercentage` visibility=gone

## `activity_partyledgers_parameters.xml`
- Inflated by: `accountsreports_parameters`
- ConstraintLayout `#main`
  - LinearLayout
    - TextView `#label` text="Select Finances"
    - RecyclerView `#partyledgersparameters_finanelistrecyclerviewid`
    - TextView `#partyledgersparameters_selectfinancialyear` text="Financial Year :"
    - TextView `#partyledgersparameters_selectperiodlabelid` text="Select Report Date"
    - LinearLayout `#partyledgersparameters_fromdatelayoutid`
      - TextView `#partyledgersparameters_fromdatelabelid` text="Date" / ta "தேதி"
      - TextView text=": "
      - EditText `#partyledgersparameters_fromdateid` text="06-Jun-2020"
    - LinearLayout `#partyledgerparameters_partyselectionid`
    - Button `#partyledgersparameters_showbuttonid` text="SHOW"
  - ProgressBar `#partyledgersparameters_pbarid` visibility=gone

## `activity_partyledgerslist.xml`
- Inflated by: `partyledgers_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - TextView text="PARTY"
      - TextView text=":"
      - TextView `#partyledgerslist_partynameid`
    - LinearLayout
      - TextView text="FINANCE"
      - TextView text=":"
      - TextView `#partyledgerslist_financenamesid`
    - LinearLayout
      - TextView `#periodorloannoid` text="PERIOD"
      - TextView text=":"
      - TextView `#partyledgerslist_periodid`
      - Button `#partyledger_deleteloanpartybuttonid`
    - LinearLayout
      - LinearLayout `#partyledgerslist_columnheaderid`
        - LinearLayout
          - TextView text="Particulars"
        - TextView `#partyledgersrow_interestid` text="Interest"
        - TextView `#partyledgerlist_loanordepositcolumnheaderid` text="Loan"
        - TextView `#partyledgersrow_repaymentid` text="Return"
    - RecyclerView `#acctranslist_recycler_view`
  - ProgressBar `#acctranslist_pbar` visibility=gone

## `activity_partyrearrange.xml`
- Inflated by: `partyrearrange`
- CustomListView `#listView1`

## `activity_partytransfer.xml`
- Inflated by: `partytransfer`
- RelativeLayout `#main`
  - LinearLayout
    - TextView text="Current Collection Area" / ta "தற்போது இருக்கும் பகுதி"
    - EditText `#partytransfer_sourcesubareaid`
    - TextView text="Party" / ta "பார்ட்டி"
    - EditText `#partytransfer_selectespartyid`
    - TextView text="New Area" / ta "மாற்ற வேண்டிய பகுதி"
    - EditText `#partytransfer_targetsubareaid`
    - Button `#btnsavepartytranseferid` text="Update" / ta "சேமி"

## `activity_periodicloanreportlist.xml`
- Inflated by: `periodicloareportlist_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - include layout=@layout/customersearchlist

## `activity_playstoredemouserregistration.xml`
- Inflated by: `playstoredemouserregistrationact`
- ConstraintLayout `#constraintLayout`
  - LinearLayout
    - ImageView contentDescription="logo"
    - LinearLayout
      - TextView text="VENUS SOLUTIONS"
      - TextView text="WELCOMES YOU"
      - TextView text="Kindly Introduce Yourself"
  - ScrollView
    - LinearLayout
      - TextView text="Name"
      - EditText `#login_contactperson` inputType=text
      - TextView text="Mobile No"
      - EditText `#login_contactno` inputType=number
      - TextView text="Finance Name"
      - EditText `#login_financename` inputType=text
      - TextView text="City"
      - EditText `#login_cityname` inputType=text
      - TextView text="EMail"
      - EditText `#login_emailid` inputType=textEmailAddress
      - Button `#login_sendotp` text="Register and SEND OTP"
  - ProgressBar `#loading` visibility=gone
  - TextView `#loadingmsgid` text="Connectig....." visibility=gone

## `activity_prevloanclosure.xml`
- Inflated by: `prevloanclosure_list_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout `#lytdaterangeid`
      - TextView text="From"
      - TextView text=": "
      - TextView `#ds_fromdateid` text="06-06-2020"
      - TextView text="To"
      - TextView text=": "
      - TextView `#ds_todateid` text="06-Jun-2020"
      - ImageButton `#acctranslistrefreshid`
    - View
    - LinearLayout
      - LinearLayout `#partyledgerslist_columnheaderid`
        - TextView text="Party/SubArea"
        - TextView text="L.Date/Extra Coll"
        - TextView text="C.Date/Pending"
    - RecyclerView `#prevsavingsclosure_recycler_view`

## `activity_prevsavingclosure.xml`
- Inflated by: `prevsavingclosure_list_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - LinearLayout `#partyledgerslist_columnheaderid`
        - TextView text="Date"
        - TextView text="Party"
        - TextView text="Amount"
        - TextView text="Interest"
    - RecyclerView `#prevsavingsclosure_recycler_view`

## `activity_quickacccounttransactions.xml`
- Inflated by: `accounttransactions_act`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView `#qat_ttypeid` text="PAYMENT"
      - TextView text="Date" / ta "நாள்"
      - TextView `#qatl_transdateid` text="01-06-2001"
      - TextView text="Finance"
      - EditText `#qat_financeid` inputType=numberSigned
      - LinearLayout
        - TextView text="SNo" / ta "எண்"
        - TextView text=":"
        - TextView `#qat_CWVWUDTransIdDispid`
      - TextView text="Source Account" / ta "பணம் பெறப்படும் கணக்கு"
      - EditText `#qat_sourceaccountetid`
      - TextView text="Target Account" / ta "பணம் அளிக்கப்படும் கணக்கு"
      - EditText `#qat_targetaccountetid`
      - TextView text="Amount" / ta "தொகை"
      - EditText `#qat_amountid` inputType=numberDecimal
      - TextView text="Notes" / ta "குறிப்பு"
      - EditText `#qat_narrationid`
      - ImageView `#groupphotoimageviewid` contentDescription="transactionimage" visibility=gone
      - Button `#qat_savebuttonid` text="Update" / ta "சேமி"
      - Button `#qat_deletebuttonid` text="DELETE" / ta "நீக்கு"
      - Button `#qat_addnewbuttonid` text="NEW"
  - ProgressBar `#qat_progressBar` visibility=gone
  - TextView `#qat_txtPercentage` visibility=gone
  - CircularImageView `#shg_img_plus` visibility=gone

## `activity_refreshoreloadfinancelist.xml`
- Inflated by: `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`
- ConstraintLayout `#main`
  - LinearLayout
    - LinearLayout `#reportdatelayoutid` visibility=gone
      - LinearLayout visibility=visible
        - TextView `#ds_fromdatelabelid` text="Date" / ta "தேதி"
        - TextView text=":"
        - TextView `#reportdatetvid` text="01-Jan-2019"
      - View
    - RecyclerView `#financelistid`
  - ProgressBar `#lineselectionprogressbarid` visibility=gone

## `activity_selectbranchandfinance.xml`
- Inflated by: `selectbranchandfinance`
- ConstraintLayout `#main`
  - LinearLayout
    - LinearLayout
      - TextView `#tvbranchlabelid` text="Select Branch"
      - EditText `#branchnameet`
    - LinearLayout
      - TextView text="Select Line"
      - EditText `#linenameet`
    - LinearLayout `#branchandlineselection_fromdatelayoutid` visibility=gone
      - TextView `#ds_fromdatelabelid` text="Date" / ta "தேதி"
      - TextView text=":"
      - TextView `#ds_fromdateid` text="01-06-2001"
    - LinearLayout `#branchandlineselection_todatelayoutid` visibility=gone
      - TextView `#ds1_todatelabelid` text="Date" / ta "தேதி"
      - TextView text=":"
      - TextView `#ds1_todateid` text="01-06-2001"
    - LinearLayout `#lytsevendayscollectionid` visibility=gone
      - CheckBox `#chkshowdailyloans` text="Daily Collection"
      - CheckBox `#chkshowweeklyloans` text="Weekly Collection"
      - Spinner `#spcollectiondays`
      - CheckBox `#chkshowmonthlyloans` text="Monthly Collection"
      - Button `#btnshow7dayscollectionid` text="SHOW 7DC"
    - LinearLayout `#lyttodaycollectionabstract` visibility=gone
      - Button `#btnshowtodaycollectionabstract` text="SHOW"
    - LinearLayout `#lytweeklyanalysis` visibility=gone
      - Spinner `#spinner`
      - Button `#btnsAnalysis` text="Collection Analysis"
      - Button `#btnselectedweekthittam` text="Thittam"
      - Button `#btnselectedweekpatti` text="Patti"
      - Button `#btnprintselectedweekpattiandthittam` text="Print"
  - ProgressBar `#branchandfinanceselection_pbarid` visibility=gone
  - TextView `#branchandfinanceselection_progresstextid` visibility=gone

## `activity_selectdateforreport.xml`
- Inflated by: `selectdateforreport`
- ConstraintLayout `#main`
  - TextView `#seldate_dateid` text="01-12-2019"
  - Button `#seldate_showbuttonid` text="Show"
  - ProgressBar `#pbarid` visibility=gone
  - TextView `#txtprogressid` text="prgress" visibility=gone

## `activity_selectedloancollectiondetails.xml`
- Inflated by: `selectedloancollectiondetails`
- ConstraintLayout `#main`
  - LinearLayout
    - LinearLayout
      - TextView `#cd_partynameid` text="PARTY NAME"
      - LinearLayout
        - TextView `#cd_tagid` text="TAG"
        - TextView `#cd_remarksid` text="REMARKS"
    - LinearLayout
      - LinearLayout
        - TextView text="Loan Amount" / ta "கடன் தொகை"
        - TextView text="Interest Amount" / ta "வட்டி"
        - TextView text="Document Charges" / ta "ஆவண கட்டணம்"
        - TextView text="Loan Date"
        - TextView text="Closing Date"
        - TextView text="Collected" / ta "வசூலிக்கப்பட்டது" visibility=gone
        - TextView text="Collected" / ta "வசூலிக்கப்பட்டது"
        - TextView text="Balance" / ta "பாக்கி"
        - TextView `#ilc_lblstatusid` text="Status"
      - LinearLayout
        - TextView `#ilc_loanamountid` text="Loan Amount"
        - TextView `#ilc_interestamountid` text="Interest Amount"
        - TextView `#ilc_documentcharge` text="Doc Charges"
        - TextView `#ilc_loandateid` text="Loan Date"
        - TextView `#ilc_closingdateid` text="Closing Date"
        - TextView `#ilc_collectedid` text="Collected" visibility=gone
        - TextView `#ilc_collectedamountid` text="Collected"
        - TextView `#ilc_balanceid` text="Balance"
        - TextView `#ilc_statusid` text="Status"
    - TextView text="Collection Details"
    - LinearLayout `#row3`
      - TextView text="Date"
      - TextView text="Paid"
      - TextView text="Balance"
      - TextView `#selectedloancolllbl_savingscollectedamountid` text="Savings"
      - TextView `#selectedloancolllbl_interestcollectedamountid` text="Interest"
    - RecyclerView `#recyclerviewcollectiomndetailsid`

## `activity_settings_appearance.xml`
- Inflated by: `settings_act`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="APP-Download and Upload Data"
      - View
      - LinearLayout
        - TextView text="1."
        - TextView text="Monitor on Download and Upload"
        - CheckBox `#settings_enablemoniterondownloadanduploadcheckbox`
      - View
      - LinearLayout
        - TextView text="2."
        - TextView text="BackDays Allowed for Download  Data"
        - EditText `#settings_noofbackdaysallowedfordownloaddataid` inputType=numberDecimal
      - View
      - LinearLayout
        - TextView text="3."
        - TextView text="Strict on Two Collection Executives"
        - CheckBox `#settings_strictontwocollectionexecutivescheckbox`
      - View
      - LinearLayout
        - TextView text="4."
        - TextView text="Allow Overall List Download"
        - CheckBox `#settings_allowoveralllistdownloadcheckbox`
      - View
      - LinearLayout
        - TextView text="5."
        - TextView text="Monitoring on Loan Closure"
        - CheckBox `#settings_moniteringonloanclosurecheckbox`
      - View
      - TextView text="APP-Collection List"
      - View
      - LinearLayout
        - TextView text="6."
        - TextView text="Show Party No in Collection List"
        - CheckBox `#settings_showpartynoincollectionlistcheckbox`
      - View
      - LinearLayout
        - TextView text="6a."
        - TextView text="Visit Checkup"
        - CheckBox `#settings_visitcheckupincollectionlistcheckbox`
      - View
      - LinearLayout
        - TextView text="7."
        - TextView text="Show Last 5 Days Collection in Area Wise Collection List"
        - CheckBox `#settings_showlast5dayscollectioninareawisecollectionlistcheckbox`
      - View
      - LinearLayout
        - TextView text="8."
        - LinearLayout
          - TextView text="Prev Collections Method"
          - EditText `#settings_fivedayscollectionsmethodet`
      - View
      - LinearLayout
        - TextView text="9."
        - TextView text="Show Loan Method in APP Collection List"
        - CheckBox `#settings_showloanmethodinappcollectionlistcheckbox`
      - View
      - LinearLayout
        - TextView text="10."
        - LinearLayout
          - TextView text="Loan Status Method"
          - EditText `#settings_loanstatusmethodid`
      - View
      - LinearLayout
        - TextView text="11."
        - TextView text="Enable Full Options in Edit Collection List"
        - CheckBox `#settings_enablefulloptionsincollctionlistcheckbox`
      - View
      - LinearLayout
        - TextView text="12."
        - TextView text=" Allow Penalty collection in Collection Screen"
        - CheckBox `#settings_enablepenaltyallowedincollectionscreen`
      - View
      - LinearLayout
        - TextView text="13."
        - TextView text=" Allow Collection Savings in Collection Screen"
        - CheckBox `#settings_enablesavingscollallowedincollectionscreen`
      - View
      - TextView text="REPORTS"
      - View
      - LinearLayout
        - TextView text="14."
        - TextView text="NIP Break for Weekly Thittam"
        - EditText `#settings_nipbreakforweeklythittamid` inputType=numberDecimal
      - View
      - LinearLayout
        - TextView text="15."
        - TextView text="NIP Break for Monthly Thittam"
        - EditText `#settings_nipbreakformonthlythittamid` inputType=numberDecimal
      - View
      - LinearLayout
        - TextView text="16."
        - TextView text="Divisible Value for Thittam"
        - EditText `#settings_divisiblevalueforthittamid` inputType=numberDecimal
      - View
      - LinearLayout
        - TextView text="17."
        - TextView text="No Of Days Allowed in Staff CashBook"
        - EditText `#settings_noofdaysallowedincashbookid` inputType=numberDecimal
      - View
      - LinearLayout
        - TextView text="18."
        - TextView text="Show Zero Parties subareas in 7 Days Collection Report"
        - CheckBox `#settings_showzeropartiessubareain7dayscollectionreportcheckbox`
      - View
      - LinearLayout
        - TextView text="19."
        - TextView text="Show Dashboard Cash Abstract"
        - CheckBox `#settings_showdashboardcashabstractcheckbox`
      - View
      - LinearLayout
        - TextView text="20."
        - LinearLayout
          - TextView text="Current Financial Year"
          - EditText `#settings_currentfianancialyearid`
      - View
      - LinearLayout
        - TextView text="21."
        - LinearLayout
          - TextView text="Collection Report Printing Method"
          - EditText `#settings_collectionreportprintingmethodid`
      - View
      - TextView text="APP- Loan Screen"
      - View
      - LinearLayout
        - TextView text="22."
        - TextView text="Restriction on Maximum Amount Allowed"
        - CheckBox `#settings_maximumamountallowedcheckbox`
      - View
      - LinearLayout
        - TextView text="23."
        - TextView `#lblmaxloanamountid` text="Maximum Loan Amount Allowed/\nFirst Loan Amount"
        - EditText `#settings_maximumamountallowedloanscreenid` inputType=numberDecimal
      - View
      - LinearLayout
        - TextView text="24."
        - TextView text="Individual Restriction on No of Loans Allowed"
        - CheckBox `#settings_noofloansallowedcheckbox`
      - View
      - LinearLayout
        - TextView text="25."
        - TextView `#lblnoofloansid` text="No of Loans Allowed/No of Loans Allowed "
        - EditText `#settings_noofloansallowedid` inputType=numberDecimal
      - View
      - LinearLayout
        - TextView text="26."
        - TextView text="Zero interest Loans Allowed "
        - CheckBox `#settings_ZerinterestLoansallowedcheckbox`
      - View
      - LinearLayout
        - TextView text="27."
        - TextView text="Enable OnDay Collection"
        - CheckBox `#settings_enableondaycollectioncheckbox`
      - View
      - LinearLayout
        - TextView text="28."
        - TextView text="Enable MobileNo in New Loans"
        - CheckBox `#settings_enablemobilenoinnewloanscheckbox`
      - View
      - LinearLayout
        - TextView text="29."
        - TextView text="Enable Remarks in New Loans"
        - CheckBox `#settings_enableremarksinnewloanscheckbox`
      - View
      - TextView text="APP- Party Creation Screen"
      - LinearLayout
        - TextView text="30."
        - TextView text="Enable User Defined CustomerNo in New Customer Creation Screen"
        - CheckBox `#settings_enableuserdefinedcustomernoinnewcustomercreationscreencheckbox`
      - View
      - LinearLayout
        - TextView text="31."
        - TextView text="Make all values necessary in New Party Creation Screen"
        - CheckBox `#settings_makeallvaluesnecessaryinnewpartycreationscreencheckbox`
      - View
      - TextView text="APP- SMS and Data Clearing"
      - View
      - LinearLayout
        - TextView text="32."
        - TextView text="SMS on Download Data"
        - CheckBox `#settings_enablesmsondownloaddatacheckbox`
      - View
      - LinearLayout
        - TextView text="33."
        - TextView text="SMS on upload Data"
        - CheckBox `#settings_enablemsmsonuploaddatacheckbox`
      - View
      - LinearLayout
        - TextView text="34."
        - TextView text="SMS on Cash Receipts"
        - CheckBox `#settings_enablesmsoncashreceiptscheckbox`
      - View
      - LinearLayout
        - TextView text="35."
        - TextView text="SMS on Today Collection Verification"
        - CheckBox `#settings_enablesmsontodaycollectionverificationcheckbox`
      - View
      - LinearLayout
        - TextView text="Enable Cash/Bank Book Verification"
        - CheckBox `#settings_enablecashbankbookverificationcheckbox`
      - View
      - LinearLayout
        - TextView text="36."
        - TextView text="SMS on Cash/Bank Book Verification"
        - CheckBox `#settings_enablesmsoncashbankbookverificationcheckbox`
      - View
      - LinearLayout
        - TextView text="37."
        - TextView text="Clear Data after C&B Verification"
        - CheckBox `#settings_enableclearaftercashbankbookverificationcheckbox`
      - View
      - LinearLayout
        - TextView text="38."
        - TextView text="Enable to Send SMS on Collection"
        - CheckBox `#settings_enablesendsmsoncollectioncheckbox`
      - View
      - Button `#settings_updatebuttonid` text="Update" / ta "சேமி" visibility=gone
  - ProgressBar `#settings_pbarid` visibility=gone

## `activity_sevendayscollection.xml`
- Inflated by: `sevendayscollectionforselline`
- CoordinatorLayout
  - AppBarLayout `#main`
    - Toolbar `#toolbar` app:"Sevendays Collection"
  - ViewPager `#container`

## `activity_sevendayscollectionsubarealist.xml`
- Inflated by: `collectionarealistforloanpartyandloans`, `collectionarealistforsevendayscollection`
- ConstraintLayout `#main`
  - include layout=@layout/customersearchlist

## `activity_simplethittamlist.xml`
- Inflated by: `simplethittamlist_act`, `singlefinancedailythittam_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - TextView `#simplethittamfinancenameid`
      - LinearLayout
        - TextView `#simplethittamrow_particularsid` text="Particulars"
        - TextView `#simplethittamrow_debitid` text="Credit"
        - TextView `#simplethittamrow_creditid` text="Debit"
      - RecyclerView `#recycler_view`

## `activity_specialaccesscontrol.xml`
- Inflated by: `specialaccesscontrolact`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - LinearLayout `#newloancreation_datelayoutid`
        - TextView `#ds_fromdatelabelid` text="User Level"
        - TextView `#specialuserlevels_id`
      - View
      - LinearLayout `#lytspac`
        - LinearLayout
          - TextView text="1."
          - TextView text="Allow to change Maximum Amount"
          - CheckBox `#special_allowedchangemaximumamount`
        - View
        - LinearLayout
          - TextView text=" 2."
          - TextView text="Allow to Change No of Loans"
          - CheckBox `#special_allowedchangenoofloans`
        - View
        - LinearLayout
          - TextView text=" 3."
          - TextView text="Show collected amount in Collection List"
          - CheckBox `#special_showloancollectedamountincolllist`
        - View
        - LinearLayout
          - TextView text=" 4."
          - TextView text="Show Loan Collection Status in Collection List"
          - CheckBox `#special_ShowLoanCollectionStatusinCollectionList`
        - View
        - LinearLayout
          - TextView text=" 5."
          - TextView text="Show Balance in Collection List"
          - CheckBox `#special_showbalanceincolllist`
        - View
        - LinearLayout
          - TextView text=" 6."
          - TextView text="Allow Unconditional Download"
          - CheckBox `#special_allowunconditionaldownload`
        - View
        - LinearLayout
          - TextView text=" 7."
          - TextView text="Allow change user role"
          - CheckBox `#special_allowchangeuserroleinmobaccess`
        - View
        - LinearLayout
          - TextView text=" 8."
          - TextView text="Allow to change weekday in loan screen"
          - CheckBox `#special_allowchangeweekdayinloanscreen`
        - View
        - LinearLayout
          - TextView text=" 9."
          - TextView text="Allow to see Prev Loans"
          - CheckBox `#special_allowtoseeprevloans`
        - View
        - LinearLayout
          - TextView text=" 10."
          - TextView text="Allow change loan closing date"
          - CheckBox `#special_allowchangeloanclosingdateinloanclosure`
        - View
        - LinearLayout
          - TextView text="11."
          - TextView text="Allow to change Exisiting Parties Details"
          - CheckBox `#special_allowtochangeexisitingpartiesdetails`
        - View
        - LinearLayout
          - TextView text="12."
          - TextView text="Allow to Add New Parties"
          - CheckBox `#special_allowtoaddNewParties`
        - View
        - LinearLayout
          - TextView text="13."
          - TextView text="Allow to Add New Loans"
          - CheckBox `#special_allowtoaddNewLoans`
        - View
        - LinearLayout
          - TextView text="14."
          - TextView text="Allow to Change Values in Loan Screen"
          - CheckBox `#special_allowtochangenewloandetails`
        - View
        - LinearLayout
          - TextView text="15."
          - TextView text="Allow to Check Complete Collection Details"
          - CheckBox `#special_allowtoCheckCompleteCollectionDetails`
        - View
        - LinearLayout
          - TextView text="16."
          - TextView text="Allow to Clear Cashbook Verification"
          - CheckBox `#special_allowtoclearcashbookverification`
        - View
        - LinearLayout
          - TextView text="17."
          - TextView text="Allow to View Compained Cash Book"
          - CheckBox `#special_allowtoviewcompainedcashbook`
        - View
        - LinearLayout
          - TextView text="18."
          - TextView text="Allow to Create New Loans against onday closed Loans_18"
          - CheckBox `#special_AllowtoCreateNewLoansagainstondayclosedLoans_18`
        - View
        - LinearLayout
          - TextView text="19."
          - TextView text="Allow Unconditional access in Acc Trans"
          - CheckBox `#special_AllowUnconditionalaccessinAccountTransactions_19`
        - View
        - LinearLayout
          - TextView text="20."
          - TextView text="Allow to Rearrange Areas and Parties"
          - CheckBox `#special_allowtorearranfeareasandparties`
        - View
        - Button `#updatebutton` text="Update" / ta "சேமி" visibility=gone
  - ProgressBar `#settings_pbarid` visibility=gone

## `activity_spinnersearchlist.xml`
- Inflated by: `spinnersearch_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - include layout=@layout/customersearchlist

## `activity_splash.xml`
- Inflated by: `splash`
- CoordinatorLayout
  - LinearLayout
    - TextView text="Venus Solutions"
    - TextView text="WELCOMES YOU"
    - ImageView `#imageView2`
    - LinearLayout visibility=gone
      - TextView text="Kindly Select Your APP Type"
      - Button text="Login with Registration Key"
      - Button text="Local Server Registration"
      - Button text="Demo Database"
  - ProgressBar visibility=gone
  - TextView `#pstatusid`

## `activity_sqlserverconnectionproperties.xml`
- Inflated by: `sqlserverconnectionproperties`
- ConstraintLayout `#main`
  - LinearLayout
    - TextView text="MS SQL Server IP Address"
    - EditText `#tvservernameid` text="192.168.0.100" inputType=text
    - TextView text="SQL Instance"
    - EditText `#tvinstancenameid` text="SQLExpress" inputType=text
    - TextView text="User Name"
    - EditText `#etusernameid` text="sa" inputType=text
    - TextView text="Password"
    - EditText `#etpasswordid` text="krr@123" inputType=textPassword
    - TextView text="Database Name"
    - EditText `#etdbnameid` text="mfdbonpremises" inputType=text
    - LinearLayout
      - Button `#btntestconnectionid` text="CONNECTION TEST"
      - Button `#btnnsaveproid` text="SAVE"
  - ProgressBar `#sqlserverconnectionproperties_pbarid` visibility=gone

## `activity_staffgpstrackmap01.xml`
- Inflated by: `staffgpstrackmap_act`
- RelativeLayout `#main`
  - LinearLayout `#sgt_headerid`
    - LinearLayout
      - TextView `#sgt_staffnameid`
      - TextView `#sgt_lastseenid`
    - TextView `#sgt_trackdateid`
  - LinearLayout `#sgt_summarybarid`
    - LinearLayout
      - TextView text="Distance"
      - TextView `#sgt_distanceid`
    - LinearLayout
      - TextView text="Points"
      - TextView `#sgt_pointcountid`
    - LinearLayout
      - TextView text="Collections"
      - TextView `#sgt_collectioncountid`
    - LinearLayout `#sgt_mockcolid` visibility=invisible
      - TextView text="Fake GPS"
      - TextView `#sgt_mockcountid`
  - FrameLayout
    - fragment `#sgt_mapid`
  - TextView `#sgt_nodataid` text="No location recorded for this day" visibility=gone
  - ProgressBar `#sgt_pbarid` visibility=gone

## `activity_stloan.xml`
- Inflated by: `stloans`
- ConstraintLayout
  - ScrollView `#stloans_mainScrollView`
    - LinearLayout
      - TextView text="Party Name"
      - TextView `#stloan_LedgerNameid`
      - TextView `#stloan_partyaddline1id`
      - TextView `#stloan_partyaddline2id`
      - LinearLayout `#interestcalculationandpayments_loandetailslayoutid`
        - TextView text="LOANS LIST "
        - LinearLayout
          - TextView text="FIN"
          - TextView text="No"
          - TextView text="Date"
          - TextView text="Amount"
          - TextView text="Repaid"
          - TextView text="Balance"
          - TextView text="Status"
        - RecyclerView `#recycler_view`
        - LinearLayout
          - ImageView `#stloans_addnewloanid`
          - TextView `#stloans_totalloanbalanceid` text="TOTAL"
      - TextView text="LOANS DETAILS"
      - TextView text="Finance"
      - EditText `#stloan_financeid` inputType=numberSigned
      - TextView text="Partner Recommendation"
      - EditText `#stloan_RecPartnerNameid`
      - TextView text="ST Loan No"
      - EditText `#stloan_FWUDSTLoanSNoid` inputType=numberSigned
      - TextView text="Date"
      - EditText `#stloan_DepositDateid`
      - TextView text="Amount"
      - EditText `#stloan_STLoanAmountid` inputType=numberSigned
      - TextView `#stloan_STLoanAmountinwordid`
      - TextView text="Daily Interest Rate"
      - EditText `#stloan_STLIntRateinDMid` inputType=numberDecimal
      - TextView text="Monthly Interest Rate"
      - EditText `#stloan_STLIntRateinMMid` inputType=numberDecimal
      - TextView text="Party Recommendation"
      - LinearLayout
        - EditText `#stloan_RecPartyNameid`
        - ImageView `#interestcalculationandpayments_removepartyrecid`
      - TextView text="Document Charges"
      - EditText `#stloan_DocChargesid` inputType=numberSigned
      - TextView text="DC Received As"
      - EditText `#stloan_dctargetledgeridid`
      - TextView text="Interest From"
      - EditText `#stloan_IntUptoDateid`
      - TextView text="Remarks"
      - EditText `#stloan_Remarksid`
      - TextView text="Paid From"
      - EditText `#stloan_Paidasid`
      - TextView `#stloans_banktransactiontypelabelid` text="Bank Transaction Type"
      - EditText `#stloan_banktransactiontypeid`
      - TextView `#stloans_BankRefNolabelid` text="Bank Ref No"
      - EditText `#stloan_BankRefNoid`
      - Button `#stloan_submitbuttonid` text="SUBMIT"
      - Button `#stloan_printbuttonid` text="PRINT"
      - Button `#stloan_deletebuttonid` text="DELETE"
      - ImageView `#imgfacebook` visibility=gone
  - ProgressBar `#stloan_pbarid` visibility=gone

## `activity_stockcat.xml`
- Inflated by: `stockUOM_act`, `stockcategories`
- ConstraintLayout `#main`
  - ScrollView
    - LinearLayout
      - TextView text="Stock Name"
      - EditText `#stockcat_categoriesnameid`
      - TextView text="Show Index"
      - EditText `#stockcat_Showindexd`
      - CheckBox `#stockcat_isActiveid` text="Active"
      - Button `#newaccled_submitid` text="UPDATE"
      - Button `#newaccled_deleteid` text="DELETE"
  - ProgressBar `#newaccled_pbarid` visibility=gone

## `activity_stockgroup.xml`
- Inflated by: `stockgroup_act`
- ConstraintLayout `#main`
  - LinearLayout
    - TextView text="Group Name"
    - EditText `#stockgroupname_id`
    - TextView text="HSN Code"
    - EditText `#stockgrphsncode_id` inputType=numberSigned
    - CheckBox `#stockgroup_chkisvisibleid` text="Active"
    - TextView text="Show Index"
    - EditText `#stockgroup_etshowidid` inputType=numberSigned
    - Button `#designationlevel_submitbuttonid` text="SAVE" visibility=gone
    - Button `#designationlevel_deletebuttonid` text="DELETE" / ta "நீக்கு" visibility=gone
  - ProgressBar `#profilequalifier_progressBar` visibility=gone
  - TextView visibility=gone

## `activity_syncresources.xml`
- Inflated by: `syncresources`
- ConstraintLayout `#main`
  - ImageView

## `activity_tabbedcollectionlist.xml`
- Inflated by: `tabbedcollectionlist`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#loanpartytoolbar`
  - ViewPager2 `#container`

## `activity_tabbedcollectionlistforviewpager1.xml`
- Inflated by: `tabbedcollectionlistmviewpager`
- CoordinatorLayout
  - AppBarLayout
    - Toolbar `#loanpartytoolbar`
  - ViewPager `#container`

## `activity_tabbedindividualcollectionentry.xml`
- Inflated by: `tabbedindividualcollectionentry`
- CoordinatorLayout
  - AppBarLayout
    - Toolbar `#toolbar` app:"FinSoft II" / ta "FinSoft"
  - ViewPager `#container`

## `activity_tbbspl.xml`
- Inflated by: `tbbspl_list`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - TextView `#tbbspl_financenamesid` text="FIN"
    - LinearLayout
      - TextView `#tbbspl_reportdateid` text="FIN"
      - TextView `#tbbspl_accountperiodtvid` text="FIN"
    - RecyclerView `#tbbspl__recyclerviewid`
  - ProgressBar `#tbbspl_pbar` visibility=gone

## `activity_tbbspl_particulars.xml`
- Inflated by: `tbbspl_particulars`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - RecyclerView `#acctranslist_recycler_view`
  - ProgressBar `#acctranslist_pbar` visibility=gone

## `activity_todaycollectionlist.xml`
- Inflated by: `todaycollectionabstractlist`, `todaycollectionlist`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#customersarchtoolbar`
  - LinearLayout
    - LinearLayout `#lytdaterangeid`
      - TextView text="Line"
      - TextView text=":"
      - TextView `#ds_linename`
      - TextView text="Date" / ta "தேதி"
      - TextView text=":"
      - TextView `#ds_fromdateid` text="01-06-2001"
      - ImageButton `#acctranslistrefreshid` contentDescription="Refresh"
    - View
    - LinearLayout `#lytselectionfilter` visibility=gone
      - LinearLayout
        - Button `#btnpaidparties` text="Paid"
        - TextView `#paidpartiescounttvid` text="..."
      - LinearLayout
        - Button `#btncashpaidparties` text="Cash"
        - TextView `#cashpartiescounttvid` text="..."
      - LinearLayout
        - Button `#btnbankpaidparties` text="Bank"
        - TextView `#bankpartiescounttvid` text="..."
      - LinearLayout
        - Button `#btnunpaidparties` text="Unpaid"
        - TextView `#unpaidpartiescounttvid` text="..."
    - View
    - include layout=@layout/customersearchlist
  - ProgressBar `#todaycollectionlist_progressbarid` visibility=gone

## `activity_transactionslist.xml`
- Inflated by: `inventorytransactionslist_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout `#lytdaterangeid`
      - TextView text="From"
      - TextView text=": "
      - TextView `#ds_fromdateid` text="06-06-2020"
      - TextView text="To"
      - TextView text=": "
      - TextView `#ds_todateid` text="06-Jun-2020"
      - ImageButton `#acctranslistrefreshid`
    - View
    - LinearLayout
      - TextView text="S.No" visibility=gone
      - TextView text="No"
      - TextView text="Colour" visibility=gone
      - TextView text="Date"
      - TextView text="Type" visibility=gone
      - TextView text="Party"
      - TextView text="Amount"
    - RecyclerView `#recycler_view`
  - FloatingActionButton `#transactionslist_fab`

## `activity_unpaidpartiesselectedday.xml`
- Inflated by: `unpaidpartiesonselectedday`
- LinearLayout `#main`
  - ExpandableListView `#lv`

## `activity_uploadalloweddetails.xml`
- Inflated by: `uploaddownloadcontroling`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar` app:"Upload Controling"
  - ViewPager `#container`
  - include layout=@layout/customersearchlist

## `activity_uploadcollection.xml`
- Inflated by: `uploadcollection`
- RelativeLayout `#main`
  - LinearLayout
    - TextView text="Upload Collection Details "
    - TextView `#tvCollectionRoute` text="Route Name"
    - TextView `#ucl_collectiondateid` text="01-06-2001"
    - LinearLayout
      - TextView text="Collection Executive-1" / ta "வசூலுக்கு செல்பவர்-1"
      - TextView text=":"
      - TextView `#ucl_collectionstaff1id` text="Staff Name 1"
    - LinearLayout
      - TextView text="Collection Executive-2" / ta "வசூலுக்கு செல்பவர்-2"
      - TextView text=":"
      - TextView `#ucl_collectionstaff2id` text="Staff Name 2"
    - TextView text="Estimaated Thittam" / ta "உத்தேச திட்டம்"
    - RecyclerView `#recycler_view`
    - LinearLayout
      - TextView text="Cash Collection:"
      - TextView `#tvcashcollection` text="2500"
      - TextView text="Bank Collection:"
      - TextView `#tvbankcollection` text="3500"
    - TextView text="Abstract" / ta "சுருக்கம்"
    - TextView `#tvNewLoanAmount` text="New Loans Given:"
    - TextView `#collectedamouttv` text="Collection:"
    - TextView `#tvlastsubmitdate` text="not submited yet"
    - TextView `#tvlastsubmitamount` text="Prev Collection Upload Amount:-" / ta "முன்னதாக அப்லோட் செய்யப்பட்ட வசூல் தொகை:-"
    - CheckBox `#ucl_mailsendcheckbox` text="Send EMail to Administrator"
    - TextView `#mailaddressid`
    - Button `#btnsavecollection` text="Update"
  - ProgressBar `#fileupload_progressBar` visibility=gone
  - TextView `#fileupload_txtPercentage` text="uploading file" visibility=gone

## `activity_userprevillages.xml`
- Inflated by: `userroleprevilages_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout `#newloancreation_datelayoutid`
      - TextView `#ds_fromdatelabelid` text="User Level"
      - TextView text=":"
      - TextView `#userlevels_id`
    - View
    - RecyclerView `#recycler_view`
  - Button `#savebuttonid` text="SAVE" visibility=gone

## `activity_varakadanillapulist.xml`
- Inflated by: `dailypatti_varakadanillapulist_act`
- CoordinatorLayout
  - AppBarLayout
    - Toolbar `#toolbar`
  - include layout=@layout/customersearchlist

## `activity_varathittam.xml`
- Inflated by: `varathittam`
- ConstraintLayout `#main`
  - Spinner `#spinner` visibility=gone
  - Button `#btnPrepareandPrint` text="SHOW" visibility=gone
  - Button `#btnsendmaiiid` text="SEND EMAIL" visibility=gone
  - ImageView `#pdfImageView`

## `activity_webview.xml`
- Inflated by: `financesoftwaredontinwebview`
- RelativeLayout
  - WebView `#webView`

## `activity_weeklycollanalysis.xml`
- Inflated by: `weeklycollectionanalysis_act`
- CoordinatorLayout `#main`
  - AppBarLayout
    - Toolbar `#toolbar`
  - LinearLayout
    - LinearLayout
      - TextView `#weeklyanalysis_weeknoid` text="எண்"
      - TextView `#weeklyanalysis_weekdateid` text="வாரம்"
      - TextView `#weeklyanalysis_collectionid` text="வசூல்"
      - TextView `#weeklyanalysis_balanceid` text="நிலுவை"
    - include layout=@layout/customersearchlist

## `addnewloanbar.xml`
- Inflated by: — (not referenced via `R.layout`); included by `activity_navigationdrawactivity`
- CoordinatorLayout
  - AppBarLayout
    - Toolbar `#toolbar`

## `allfinancedailypattirow.xml`
- Inflated by: `allfinancedailypattiadapter`, `allfinancedailypattinew_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#afdp_particularsid` text="Particulars"
    - TextView `#afdp_leftsideid` text="Leftside"
    - TextView `#afdp_rightsideid` text="right"
    - TextView `#afdp_countid` text="Count"

## `allfinancedailythittamrow.xml`
- Inflated by: `allfinancedailuthittamadapter`
- RelativeLayout
  - LinearLayout
    - TextView `#afdailythittamledgernameid` text="Ledger"
    - TextView `#afdailythittamparticularsid` text="Particulars"
    - TextView `#afdailythittamdebitid` text="Debit"
    - TextView `#afdailythittamcreditid` text="Credit"

## `anarep_finace_partnerwiseabs_row.xml`
- Inflated by: `anarep_fin_partwiseabstrat_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#anarep_fin_partnerwiseabsrow_partnernameid` text="Partner"
    - TextView `#anarep_fin_partnerwiseabsrow_financenameid` text="Finance"
    - TextView `#anarep_fin_partnerwiseabsrow_amountid` text="Marked"

## `anarep_finacewiseabs_row.xml`
- Inflated by: `anarep_finwiseabstrct_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#anarep_finwiseabsrow_financenameid` text="LedgerName"
    - TextView `#anarep_finwiseabsrow_amountid` text="Marked"

## `anarep_interestratedist_row.xml`
- Inflated by: `anarep_interestratedist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#anarep_interestratedist_financenameid` text="Finance"
      - TextView `#anarep_interestratedist_countid` text="Int"
      - TextView `#anarep_interestratedist_interestrateid` text="Int"
      - TextView `#anarep_interestratedist_totalid` text="Total"
    - LinearLayout
      - TextView text="MAX: "
      - TextView `#anarep_interestratedist_minimumamountid` text="Finance"
      - TextView text="MIN :"
      - TextView `#anarep_interestratedist_maximumamountid` text="Int"
      - TextView text="AVG :"
      - TextView `#anarep_interestratedist_averageamountid` text="Total"

## `anarep_overallinterestratedist_row.xml`
- Inflated by: `anarep_overallinterestratedist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#anarep_interestratedist_countid` text="Int"
      - TextView `#anarep_interestratedist_interestrateid` text="Int"
      - TextView `#anarep_interestratedist_totalid` text="Total"
    - LinearLayout
      - TextView text="MIN: "
      - TextView `#anarep_interestratedist_minimumamountid` text="Finance"
      - TextView text="MAX :"
      - TextView `#anarep_interestratedist_maximumamountid` text="Int"
      - TextView text="AVG :"
      - TextView `#anarep_interestratedist_averageamountid` text="Total"

## `anarep_partywiseabs_row.xml`
- Inflated by: `anarep_partywiseabstrat_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#anarep_fin_partywiseabsrow_partynameid` text="Party"
      - LinearLayout
        - TextView `#anarep_fin_partywiseabsrow_partnernameid` text="Partner"
        - TextView `#anarep_fin_partywiseabsrow_intrateid` text="Int"
        - TextView `#anarep_fin_partywiseabsrow_financenameid` text="Fin"
        - TextView `#anarep_fin_partywiseabsrow_amountid` text="Marked"

## `areawisecollectionlistrow.xml`
- Inflated by: `areawisecollectionlistadapter`, `collectionarealistadapterforloanpartiesandloans`
- LinearLayout
  - LinearLayout `#awcl_rowcardid`
    - LinearLayout
      - TextView `#awcl_areanameid`
      - LinearLayout
        - LinearLayout
          - TextView text="Parties"
          - TextView `#awcl_NoOfPartiesId`
        - LinearLayout `#awcl_markedcolid`
          - TextView text="Marked"
          - TextView `#awcl_NoOfmarkedPartiesId`
        - LinearLayout `#awcl_nipcolid`
          - TextView `#niplabelid` text="NIP"
          - TextView `#awcl_NoOfNipPartiesId`
        - LinearLayout `#awcl_warncolid`
          - TextView text="Warn"
          - TextView `#awcl_NoOfWarningPartiesId`
    - LinearLayout
      - TextView `#awcl_collectedamountId`
      - View visibility=gone
      - TextView `#awcl_collectableamountId` visibility=gone

## `btp_main.xml`
- Inflated by: `BluetoothPrinterMain`
- FrameLayout `#drawer_layout`
  - FrameLayout `#container`
  - TextView `#tvStatus` text="Status"

## `cashbookabstract_row.xml`
- Inflated by: `cashbookabstract_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#cashbookabstractrow_ledgernameid` text="LedgerName"
    - TextView `#cashbookabstractrow_amountid` text="Marked"

## `cashbookverifyrow.xml`
- Inflated by: `cashbook_adapter`
- LinearLayout
  - TextView `#afdailythittamledgernameid` text="Ledger" visibility=gone
  - TextView `#afdailythittamparticularsid` text="Particulars"
  - TextView `#afdailythittamdebitid` text="Debit"
  - TextView `#afdailythittamcreditid` text="Credit"

## `closablesavings_row.xml`
- Inflated by: `closablesavings_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#loanclosurelistrow_partynameid` text="Party Name" / ta "பார்ட்டி பெயர்"
      - TextView `#loanclosurelistrow_tagid` text="Business" / ta "தொழில்"
      - TextView `#loanclosurelistrow_subareanameid` text="Subarea"
    - LinearLayout
      - TextView `#loanclosurelistrow_savingsamount` text="123"

## `closed_renewedloansrow.xml`
- Inflated by: `closed_renewedloanslist_adapter`
- RelativeLayout
  - LinearLayout `#flabels`
    - TextView `#crloanlistpartynameid` text="ACCESS DENIED"
    - TextView `#crloanlistareanameid` text="DENIED"
  - LinearLayout `#labels`
    - TextView text="முந்தைய கடன்"
    - TextView text="கடைசி வசூல்"
    - TextView text="புதிய கடன்"
  - LinearLayout
    - TextView `#prevloandateandamounttvid`
    - TextView `#prevloanlastcollectiondateandamounttvid`
    - TextView `#newloandateandamounttvid`

## `clubsandgroupsmarked_row.xml`
- Inflated by: `selectablelist_adapter`
- RelativeLayout
  - LinearLayout
    - CheckBox `#cgm_ismarkedid`
    - TextView `#cgm_clubandgroupnameid` text="Club and Group Name" visibility=gone

## `collectionarea_row.xml`
- Inflated by: `collectionarea_adapter`
- RelativeLayout
  - TextView `#collarea_collectionareanameid` text="Collection Area Name" / ta "வசூல் பகு"
  - Button `#collarea_editbuttonid` text="EDIT" / ta "திருத்து"

## `collectionarearearangelist_row.xml`
- Inflated by: `collectionarearearrangeAdapter`
- RelativeLayout `#lytPattern`
  - LinearLayout `#lytPatternColor`
    - LinearLayout `#lytPatternColorDraw`
  - LinearLayout
    - TextView `#textPrimaryDisplayMemberId` text="TextView"
    - TextView `#textSecondaryDisplayMemberId` text="TextView" visibility=gone
  - LinearLayout `#lytGrab`
    - ImageView `#imageViewGrab`

## `collectiondetails_row.xml`
- Inflated by: `collectionlist_adapter`; included by `activity_newloanscollectionlist`
- RelativeLayout
  - LinearLayout `#row3`
    - TextView `#collectionlist_snoid` text="S.No"
    - TextView `#collectionlist_transdateid` text="Date"
    - TextView `#collectionlist_collectedamountid` text="Collection" / ta "collection"
    - TextView `#collectionlist_penaltycollectedamountid` text="Penalty"
    - TextView `#collectionlist_Savingscollectedamountid` text="Savings"

## `content_main2.xml`
- Inflated by: — (not referenced via `R.layout`)
- RelativeLayout

## `customersearchlist.xml`
- Inflated by: — (not referenced via `R.layout`); included by `activity_closed_remevedloanslist`, `activity_daywisependingreport`, `activity_editingcollections`, `activity_editingfullcollections`, `activity_employeesearch`, `activity_ledgerstatmentsdateselection`, `activity_nadapunippucollectionlist`, `activity_nadapunippuphakkilist`, `activity_newloanscollectionlist`, `activity_periodicloanreportlist`, `activity_sevendayscollectionsubarealist`, `activity_spinnersearchlist`, `activity_todaycollectionlist`, `activity_uploadalloweddetails`, `activity_varakadanillapulist`, `activity_weeklycollanalysis`, `fragment_allfinancedailythittam`, `fragment_sevendayscollection`, `fragment_tabbedcollectionlist`, `fragment_uploadalloweddetails`
- RelativeLayout
  - RecyclerView `#recycler_view`

## `dailypattinew_kadanpatru_row.xml`
- Inflated by: `dailypatti_kadanpatru_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#dtnew_kadanpatrurow_partyid` text="Party"
      - TextView `#dtnew_kadanpatrurow_loanid` text="Loan"
      - TextView `#dtnew_kadanpatrurow_duesid` text="Dues"
      - TextView `#dtnew_kadanpatrurow_interestid` text="Interest"
      - TextView `#dtnew_kadanpatrurow_mahimaiid` text="Mahimai"
    - LinearLayout
      - TextView `#dtnew_kadanpatrurow_areaid` text="Area"
      - TextView `#dtnew_kadanpatrurow_loanmethodid` text="Loan Method"
      - TextView `#dtnew_kadanpatrurow_loandateid` text="LDate"

## `dailypattinew_nadapunippucollection_row.xml`
- Inflated by: `dailypatti_nadapunippucollection_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#dtnew_nadapunippucollectionrow_partyid` text="Party"
      - TextView `#dtnew_nadapunippucollectionrow_loanid` text="Loan"
      - TextView `#dtnew_nadapunippucollectionrow_prevcollectionid` text="Prev Collection"
      - TextView `#dtnew_nadapunippucollectionrow_collectionid` text="Collection"
    - LinearLayout
      - TextView `#dtnew_nadapunippucollectionrow_areaid` text="Area"
      - TextView `#dtnew_nadapunippucollectionrow_loanmethodid` text="Method"
      - TextView `#dtnew_nadapunippucollectionrow_loandateid` text="LDate/CDate"

## `dailypattinew_nadapunippuphakki_row.xml`
- Inflated by: `dailypatti_nadapunippuphakki_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#dtnew_nadapunippuphakkirow_partyid` text="Party"
      - TextView `#dtnew_nadapunippuphakkirow_loanid` text="Loan"
      - TextView `#dtnew_nadapunippuphakkirow_balanceid` text="Balance"
      - TextView `#dtnew_nadapunippuphakkirow_collectedid` text="Collected"
    - LinearLayout
      - TextView `#dtnew_nadapunippuphakkirow_areaid` text="Area"
      - TextView `#dtnew_nadapunippuphakkirow_loanmethodid` text="Loan Method"
      - TextView `#dtnew_nadapunippuphakkirow_loandateid` text="LDate/CDate"

## `dailypattinew_varakadanillapu_row.xml`
- Inflated by: `dailypatti_varakadanillapu_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#dtnew_varakadanillapurow_partyid` text="Party"
      - TextView `#dtnew_varakadanillapurow_loanid` text="Loan"
      - TextView `#dtnew_varakadanillapurow_Collectedid` text="Collected"
      - TextView `#dtnew_varakadanillapurow_pendingid` text="Pending"
    - LinearLayout
      - TextView `#dtnew_varakadanillapurow_areaid` text="Area"
      - TextView `#dtnew_varakadanillapurow_loanmethodid` text="Loan Method"
      - TextView `#dtnew_varakadanillapurow_loandateid` text="LDate/CDate/Closed on"

## `daywisependingreport_row.xml`
- Inflated by: `daywisependingreport_adapter`
- RelativeLayout
  - TextView `#newloanlistpartynameid` text="Party Name"
  - LinearLayout `#row2`
    - TextView `#newloanlistareanameid` text="areaname"
  - LinearLayout `#row3`
    - TextView text="Loan:" / ta "கடன்:"
    - TextView `#newloanlist_loanamountid` text="1000"
    - TextView text="Ins:" / ta "தவணை:"
    - TextView `#newloanlist_dues1id` text="1000"
    - TextView text="Dues:"
    - TextView `#newloanlist_duesid` text="1000"
  - LinearLayout
    - TextView text="Date:" / ta "தேதி:"
    - TextView `#newloanlist_loandateid` text="1000"
    - TextView text="/"
    - TextView `#newloanlist_Closeloandateid` text="1000"
    - TextView text="Balance:"
    - TextView `#newloanlist_balanceid` text="1000"
    - LinearLayout `#newloanlist_ondaycollectionlayoutid`
      - TextView text="Coll:"
      - TextView `#newloanlistcollectionamountid` text="1000"
  - TextView `#newloanlist_loanmethodid` text="100D"
  - ImageView `#newloanlist_collectiondetailsid` visibility=gone

## `dialog.xml`
- Inflated by: — (not referenced via `R.layout`)
- LinearLayout `#saveFileDialog`
  - ListView `#fileList`
  - LinearLayout `#fileLinearLayout2`
    - TextView `#fileTextView1` text="enterFileName"
    - EditText `#fileName` contentDescription="enterFileName" inputType=text
    - LinearLayout `#fileLinearLayout3`
      - Button `#fileSaveLoad` text="saveButtonText"
      - Button `#fileCancel` text="cancelButtonText"
      - Button `#newFolder` text="newFolderButtonText"
    - Spinner `#fileFilter`

## `documents_row.xml`
- Inflated by: `documentsphotossearchlist_adapter`
- RelativeLayout
  - ImageView `#doc_documentimageview`
  - TextView `#doc_documenttype` text="Document Name"
  - TextView `#doc_remarks` text="Remarks"
  - ImageButton `#doc_remove`

## `documentsdisplay_row.xml`
- Inflated by: `documentsphotossearchlist_adapter`
- RelativeLayout
  - ImageView `#doc_documentimageview`
  - TextView `#doc_documenttype` text="Document Name"
  - TextView `#doc_remarks` text="Remarks"
  - ImageButton `#doc_remove` visibility=gone

## `duelist_row.xml`
- Inflated by: `duelist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#duelistrow_duesnoid` text="1"
      - TextView `#duelistrow_duedateid` text="12-10-2020"
      - TextView `#duelistrow_totaldueamount` text="15000"

## `employeesearchrow.xml`
- Inflated by: `branchemployeesaccessearch_adapter`, `employeesaccessearchadapter`, `employeesearchadapter`
- RelativeLayout
  - LinearLayout
    - TextView `#empsarch_partynameid` text="Party Name" / ta "பார்ட்டி பெயர்"
    - TextView `#empsearch_tagid` text="Business" / ta "தொழில்"
    - TextView `#empsearch_userrolename` text="User Role" visibility=gone

## `expenseslist01_row.xml`
- Inflated by: `expensesentrylist_adapter`
- LinearLayout
  - TextView `#expenseslistrow_ledgername` text="Acc Ledger Name" / ta "ledgername"
  - TextView `#expenseslistrow_amount`

## `file_selector_main.xml`
- Inflated by: — (not referenced via `R.layout`)
- LinearLayout
  - Button `#button_load` text="loadButtonText"
  - Button `#button_save` text="saveButtonText"

## `financelist_withselectablecheckbox_row.xml`
- Inflated by: `financelist_selectable_adabpter`
- RelativeLayout
  - CheckBox `#financelistwithselectablecheckbox_checkboxid` text="Finance Name"

## `financelistrow.xml`
- Inflated by: `branchlistadabpter`, `financelistadabpter`
- RelativeLayout
  - LinearLayout
    - TextView `#financelist_financenameid` text="Finance Name"
    - ImageView `#financelist_editimageviewid` visibility=gone

## `financewisepartners_row.xml`
- Inflated by: `financewisepartners_adabpter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - CheckBox `#financewisepartnersrow_partnerselectedornotid` text="Murugesan. R"
      - CheckBox `#financewisepartnersrow_isactiveid` text="Active"

## `fr_image_list.xml`
- Inflated by: `analysisrep_financewiseabstract_fragment`, `analysisrep_partnerwiseabstract_fragment`, `analysisrepfin_interestratedist_fragment`, `analysisrepfin_overallinterestratedist_fragment`, `analysisrepfin_partnerwiseabstract_fragment`, `analysisrepfin_partyrwiseabstract_fragment`
- CoordinatorLayout
  - LinearLayout
    - TextView `#fragmenttitleid` text="Fragment Title"
    - RecyclerView `#@android:id/list`
  - ProgressBar `#fragment_progressbarid` visibility=gone

## `fr_monthlypandl.xml`
- Inflated by: `monthlypandl_fragment`
- CoordinatorLayout
  - LinearLayout
    - TextView `#fragmenttitleid` text="Fragment Title"
    - RecyclerView `#@android:id/list`
  - ProgressBar `#fragment_progressbarid` visibility=gone

## `fragment_allfinancedailypatti.xml`
- Inflated by: `allfinancedailypatti`
- ConstraintLayout `#constraintLayout`
  - LinearLayout
    - TextView `#section_label` text="asdf"
    - RecyclerView `#afdp_recycler_view`

## `fragment_allfinancedailythittam.xml`
- Inflated by: `allfinancedailythittam`
- ConstraintLayout `#constraintLayout`
  - LinearLayout
    - TextView `#section_label` text="Finance Name"
    - LinearLayout
      - include layout=@layout/customersearchlist
  - ProgressBar `#listprogressbarid` visibility=gone

## `fragment_allowedfinancedailythittam.xml`
- Inflated by: `dailythittamforallowedlines`
- RelativeLayout
  - LinearLayout
    - TextView `#loading_label` text="Loading......." visibility=gone
    - ProgressBar `#progress_bar`
    - TextView `#section_label`
    - LinearLayout
      - LinearLayout
        - TextView `#simplethittamrow_ledgernameid` text="Ledger"
        - TextView `#simplethittamrow_particularsid` text="Particulars"
        - TextView `#simplethittamrow_debitid` text="Credit"
        - TextView `#simplethittamrow_creditid` text="Debit"
      - RecyclerView `#recycler_view`

## `fragment_newsevendayscollection.xml`
- Inflated by: `newsevendayscollection`
- ConstraintLayout `#constraintLayout`
  - TextView `#section_label`

## `fragment_partnerwiseloansandperfomance.xml`
- Inflated by: `partnerwiseloansandperformance_fragment`
- ConstraintLayout
  - LinearLayout
    - LinearLayout
      - TextView `#meetingreports_includedfinanceinfoid` text="SNF, SNF I, KSF"
    - ExpandableListView `#lvExp`
  - ProgressBar `#meetingreport_pbarid` visibility=gone
  - TextView `#meetingreport_progresstextid` text="progress" visibility=gone

## `fragment_sevendayscollection.xml`
- Inflated by: `sevendayscollectionforselline`
- RelativeLayout
  - LinearLayout
    - TextView `#section_label` text="Finance Name"
    - include layout=@layout/customersearchlist

## `fragment_tabbedcollectionlist.xml`
- Inflated by: `tabbedcollectionlist`, `tabbedcollectionlistmviewpager`
- ConstraintLayout `#constraintLayout`
  - LinearLayout
    - LinearLayout
      - TextView `#section_label` text="Collection Area Name" / ta "வசூல் ஏரியா பெயர்"
    - include layout=@layout/customersearchlist
  - TextView `#tcl_tvtotalcollectionamountid` text="Total Collection"
  - FloatingActionButton `#tcl_fab` contentDescription="TODO"

## `fragment_tabbedindividualcollectionentry.xml`
- Inflated by: `tabbedindividualcollectionentry`
- ConstraintLayout `#constraintLayout`
  - TextView `#section_label` visibility=gone
  - ScrollView
    - LinearLayout
      - LinearLayout
        - ImageView `#ilc_thumbnail`
        - LinearLayout
          - TextView `#ilc_partynameid` text="Party Name" / ta "பார்ட்டி பெயர்"
          - TextView `#ilc_tagid` text="TAG"
          - TextView `#ilc_remarksid` text="REMARKS"
      - LinearLayout
        - LinearLayout
          - TextView text="Loan Amount"
          - TextView text=":"
          - TextView `#ilc_loanamountid` text="Loan Amount"
        - LinearLayout
          - TextView text="Interest/Document"
          - TextView text=":"
          - LinearLayout
            - TextView `#ilc_interestamountid` text="Interest"
            - TextView `#ilc_documentcharge` text="Doc"
        - LinearLayout
          - TextView text="Date"
          - TextView text=":"
          - TextView `#ilc_loandateid` text="Date"
        - LinearLayout
          - TextView text="Closing Date"
          - TextView text=":"
          - LinearLayout
            - TextView `#ilc_closingdateid` text="Closing Date"
            - TextView `#ilc_overduedays` text="21"
        - LinearLayout
          - TextView text="Due"
          - TextView text=":"
          - TextView `#ilc_dueamountid` text="Due"
        - LinearLayout `#tabbedindloancolentry_collectedamountlayoutid`
          - TextView `#ilc_collectedamountlabel` text="Collected" / ta "வசூலிக்கப்பட்டது"
          - TextView text=":"
          - TextView `#ilc_prevcollectedamountid` text="Collected Amount"
        - LinearLayout `#tabbedindloancolentry_pendingamountlayoutid`
          - TextView `#ilc_pendingamountlabel` text="Pending"
          - TextView text=":"
          - TextView `#ilc_balanceid` text="Pending"
        - LinearLayout `#tabbedindloancolentry_loancollectionstatuslayoutid`
          - TextView `#ilc_lblstatusid` text="Status"
          - TextView text=":"
          - TextView `#ilc_statusid` text="Status"
          - TextView `#ilc_additionaldue` text="0"
      - TextView text="Collection Entry"
      - LinearLayout
        - LinearLayout `#ilc_lytcurcollectionamountid`
          - TextView text="Loan Due Collection"
          - EditText `#ilc_curcollectionamountid` inputType=number
        - LinearLayout
          - TextView `#ilc_penaltylabeltv` text="Pen/Int Collection"
          - EditText `#ilc_curcollectionpenaltyamountid` inputType=number
        - LinearLayout
          - TextView `#ilc_savingslabeltv` text="Savings Collection"
          - EditText `#ilc_curcollectionsavingsamountid` inputType=number
        - LinearLayout visibility=gone
          - TextView text="Collected as/ In"
          - EditText `#ilc_curTargetLedgerId` inputType=number
        - View
        - LinearLayout
          - TextView `#ilc_cashcollectedlabeltv` text="Cash Collection"
          - EditText `#ilc_cashcollectionid` inputType=number
        - LinearLayout
          - TextView `#ilc_bankcolllabeltv` text="Bank Collection"
          - EditText `#ilc_bankcollectionid` inputType=number
      - LinearLayout `#inventorylayoutid` visibility=gone
        - TextView text="Products"
        - LinearLayout
          - TextView `#invtransdetailsrow_snoid` text="SN "
          - TextView text="Product"
          - TextView text="Qty"
          - TextView `#invtransdetailsrow_priceheaderid` text="Price"
          - TextView `#invtransdetailsrow_totalamountheaderid` text="Amount"
        - LinearLayout
          - ConstraintLayout
            - RecyclerView `#recycler_view`
            - ProgressBar `#itemdetailspbarid`
      - LinearLayout
        - LinearLayout
          - TextView `#ilc_mobileno1id` text="\ 9655842840"
          - LinearLayout
            - ImageButton `#ilc_callmobileno1id` contentDescription="Call"
            - ImageButton `#ilc_msgmobileno1id` contentDescription="Message"
        - LinearLayout
          - TextView `#ilc_mobileno2id` text="\ 9655842840"
          - LinearLayout
            - ImageButton `#ilc_callmobileno2id` contentDescription="Call"
            - ImageButton `#ilc_msgmobileno2id` contentDescription="Message"
        - ImageButton `#ilc_googlemaplocatorid`
        - ImageButton `#ilc_relateddocuments`
        - ImageButton `#ilc_printreceipt`
      - LinearLayout visibility=gone
        - Button text="No Not Found"
        - Button text="No Not Found"
        - Button text="Locate"
      - TextView text="Prev 5 Collections"
      - LinearLayout
        - LinearLayoutCompat
          - ScrollView
            - TableLayout `#tblpcolid`
      - TextView `#prev5locationstextviewid` text="Prev 5 Locations"
      - LinearLayout
        - LinearLayoutCompat
          - ScrollView
            - TableLayout `#tblprevloationsid`
  - FloatingActionButton `#ilc_fab`

## `fragment_uploadalloweddetails.xml`
- Inflated by: `uploaddownloadcontroling`
- ConstraintLayout `#constraintLayout`
  - LinearLayout
    - TextView `#section_label` text="Heading"
    - include layout=@layout/customersearchlist

## `handloans_row.xml`
- Inflated by: `handloans_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#handloansrow_ledgernameid` text="LedgerName"
      - TextView `#handloansrow_finnameid` text="SKM"
      - TextView `#handloansrow_receivableid` text="Receivables"
      - TextView `#handloansrow_payableid` text="Payables"

## `individualloanordepositdetails_row.xml`
- Inflated by: `individualloansanddepositsstatuslist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#individualloanordepositdetailsrow_finid` text="FIN"
      - TextView `#individualloanordepositdetailsrow_noid` text="No"
      - TextView `#individualloanordepositdetailsrow_dateid` text="Date"
      - TextView `#individualloanordepositdetailsrow_amountid` text="Amount"
      - TextView `#individualloanordepositdetailsrow_repaidid` text="Repaid"
      - TextView `#individualloanordepositdetailsrow_balanceid` text="Balance"
      - TextView `#individualloanordepositdetailsrow_statusid` text="Status"

## `interestcalculation_row.xml`
- Inflated by: `ofloans_interestcalculationlist_adapter`, `stloansinterestcalculationlist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout `#interestcalculationrow_layoutid`
      - ImageView `#interestcalculationlist_row_actionid`
      - TextView `#interestcalculationlist_row_typeid` text="Type"
      - TextView `#interestcalculationlist_row_loanamountid` text="Loan"
      - TextView `#interestcalculationlist_row_intfromid` text="From"
      - TextView `#interestcalculationlist_row_intuptoid` text="upto"
      - TextView `#interestcalculationlist_row_daysid` text="days"
      - TextView `#interestcalculationlist_row_calcintid` text="calcint"
      - TextView `#interestcalculationlist_row_receivedid` text="Received"

## `invtransdetails_row.xml`
- Inflated by: `invtransdetails_adapter`, `invtransdetailsdisplay_adapter`; included by `activity_invtransactnew`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - LinearLayout
        - TextView `#invtransdetailsrow_snoid` text="S.No"
        - TextView `#invtransdetailsrow_proudctid` text="Product"
        - TextView `#invtransdetailsrow_qtyid` text="Qty"
        - TextView `#invtransdetailsrow_priceid` text="Price"
        - TextView `#invtransdetailsrow_totalamountid` text="Amount"
    - ImageView visibility=gone

## `item_collectionareaunpaid.xml`
- Inflated by: `unpaidpartiesonselectedday_adapter`
- RelativeLayout
  - TextView `#uparea_showid` text="01. "
  - TextView `#tv`
  - CheckBox `#cb` visibility=gone

## `item_collectiondetailsunpaid.xml`
- Inflated by: `unpaidpartiesonselectedday_adapter`
- RelativeLayout
  - TextView `#tv`
  - CheckBox `#cb` visibility=gone

## `item_partiesunpaid.xml`
- Inflated by: `unpaidpartiesonselectedday_adapter`
- RelativeLayout
  - TextView `#tv`
  - CheckBox `#cb` visibility=gone

## `itemmaster_row.xml`
- Inflated by: `itemmaster_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#itemmaster_nameid` text="itemname"

## `layout_toolbar_profile.xml`
- Inflated by: — (not referenced via `R.layout`); included by `activity_newpartycreation`, `activity_partyandpartnerdetailsnew`
- LinearLayout
  - RelativeLayout
    - CircularImageView `#memberimageid`
    - CircularImageView `#img_plus` visibility=visible
  - TextView text="Member Name" visibility=gone
  - TextView text="Son" visibility=gone

## `ledgersstatementlist_row.xml`
- Inflated by: `ledgerstatementselecteledger_adapter`
- RelativeLayout
  - TextView `#ls_ledgernameid` text="Ledger Name"

## `ledgerstatment_row.xml`
- Inflated by: `ledgerstatement_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#lsmt_TransDateid` text="Date"
    - TextView `#lsmt_Particularsid` text="Particulars"
    - TextView `#lsmt_Debitid`
    - TextView `#lsmt_Creditid`

## `loanclosurelistrow.xml`
- Inflated by: `loanclosureloanlist_adapter`
- LinearLayout
  - LinearLayout
    - TextView `#loanclosurerow_partynameid` text="Party Name" / ta "பார்ட்டி பெயர்"
    - TextView `#loanclosurerow_tagid` text="Business" / ta "தொழில்"
  - LinearLayout
    - TextView `#loanclosurerow_loandate` text="loandate"
    - TextView `#loanclosurerow_loanamount` text="loanAmount"

## `loanindexloanwielistrow.xml`
- Inflated by: `loanindexloanswiselist_adapter`
- RelativeLayout
  - LinearLayout `#lnrid`
    - LinearLayout
      - TextView text="Loan Index"
      - TextView text="Loan Amount"
      - TextView `#ploanamountid` text="Balance"
    - LinearLayout
      - TextView `#loanindex_id` text="1"
      - TextView `#prevloanamount_id` text="days"
      - TextView `#loanbal_id` text="Bal"
    - LinearLayout
      - Button `#showprevloansbutton` text="Prev\nLoans"
  - TextView `#newloanlabel` text="New Loan" visibility=gone
  - TextView `#todayclosedlabel` text="Today Closed" visibility=gone

## `loanledgers_row.xml`
- Inflated by: `loanledgers_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#loanledgersrow_snoid` text="SNo"
      - TextView `#loanledgersrow_dateid` text="12-12-2020"
      - TextView `#loanledgersrow_partyid` text="Party"
    - LinearLayout
      - TextView `#loanledgersrow_noid` text="No"
      - TextView `#loanledgersrow_irid` text="IR"
      - TextView `#loanledgersrow_returnid` text="Return"
      - TextView `#loanledgersrow_balanceid` text="Balance"

## `loanmethods_row.xml`
- Inflated by: `loanmethods_adapter`
- RelativeLayout
  - TextView `#loanmethodsrow_loanmethodnameid` text="100 days col"
  - TextView `#loanmethodsrow_shortnameid` text="Group Name"
  - TextView `#tvcollectedamountnewid` text="2500" visibility=gone

## `m3expressive_alert_dialog.xml`
- Inflated by: — (not referenced via `R.layout`)
- AlertDialogLayout `#parentPanel`
  - include layout=@layout/m3expressive_alert_dialog_title
  - FrameLayout `#contentPanel`
    - ImageView `#scrollIndicatorUp` contentDescription=@null visibility=gone
    - NestedScrollView `#scrollView`
      - LinearLayout
        - Space `#textSpacerNoTitle` visibility=gone
        - TextView `#@android:id/message`
        - Space `#textSpacerNoButtons` visibility=gone
    - ImageView `#scrollIndicatorDown` contentDescription=@null visibility=gone
  - FrameLayout `#customPanel`
    - FrameLayout `#custom`
  - include layout=@layout/m3_alert_dialog_actions

## `m3expressive_alert_dialog_title.xml`
- Inflated by: — (not referenced via `R.layout`); included by `m3expressive_alert_dialog`
- LinearLayout `#topPanel`
  - LinearLayout `#title_template`
    - ImageView `#@android:id/icon`
    - TextView `#alertTitle`
  - Space `#titleDividerNoCustom` visibility=gone

## `meetingreport_loanlevel_row.xml`
- Inflated by: `meetingreports_expandablelist_adapter`
- LinearLayout
  - TextView `#meetingreportloanlevelrow_finnameid` text="FIN"
  - TextView `#meetingreportloanlevelrow_loanamountid` text="2,00,000"
  - TextView `#meetingreportloanlevelrow_fromdateid` text="01-10-20"
  - TextView `#meetingreportloanlevelrow_todateid` text="31-10-20"
  - TextView `#meetingreportloanlevelrow_daysid` text="31"
  - TextView `#meetingreportloanlevelrow_interestamountid` text="500"

## `mettingreport_partylevel_row.xml`
- Inflated by: `meetingreports_expandablelist_adapter`
- LinearLayout
  - TextView `#mettingreportpartyrow_partyname` text="PARTY"
  - TextView `#mettingreportpartyrow_totalloanid` text="2,50,000"
  - TextView `#mettingreportpartyrow_totalinterestid` text="3,570"

## `monthwisepl_home.xml`
- Inflated by: `monthwisepandl`
- ViewPager `#pager`
  - PagerTitleStrip visibility=gone

## `nav_header_main2.xml`
- Inflated by: — (not referenced via `R.layout`)
- LinearLayout
  - ImageView
  - TextView `#txtFinanaceGroupNameId` text="financegrouname"
  - TextView `#txtFinanceNameid` text="financename"
  - LinearLayout
    - TextView `#txtLoggedUserNameid` text="username"
    - TextView `#txtuserrolenameid` text="admin"

## `newloanslistrow.xml`
- Inflated by: `newloanslist_adapter`
- RelativeLayout
  - TextView `#newloanlistpartynameid` text="Party Name"
  - LinearLayout `#row2`
    - TextView `#newloanlistareanameid` text="areaname"
  - LinearLayout `#row3`
    - TextView text="Loan:" / ta "கடன்:"
    - TextView `#newloanlist_loanamountid` text="1000"
    - TextView text="Ins:" / ta "தவணை:"
    - TextView `#newloanlist_duesid` text="1000"
    - TextView text="Int:" / ta "வட்டி:"
    - TextView `#newloanlist_intid` text="1000"
  - LinearLayout
    - TextView text="Date:" / ta "தேதி:"
    - TextView `#newloanlist_loandateid`
    - TextView text="DC:" / ta "மகிமை :"
    - TextView `#newloanlist_balanceid` text="1000"
    - LinearLayout `#newloanlist_ondaycollectionlayoutid`
      - TextView `#collectionlabeltvid` text="Onday Coll :" / ta "க.நாள் வசூல் :"
      - TextView `#newloanlistcollectionamountid`
  - ImageView `#newloanlist_collectiondetailsid`

## `newpartieslistrow.xml`
- Inflated by: `newpartieslist_adapter`
- RelativeLayout
  - TextView `#newpartylist_partynameid` text="Party Name"
  - TextView `#newpartylist_areanameid` text="areaname"

## `partieslistrow.xml`
- Inflated by: `partieslist_adapter`
- RelativeLayout
  - TextView `#partylist_partynameid` text="Party Name"
  - TextView `#partylist_tagid` text="tag"
  - TextView `#partylist_addline2` text="tag"
  - LinearLayout
    - TextView `#partylist_city` text="tag"
    - TextView `#partylist_mobileno` text="tag"

## `partnerwiseloanandperformancehome.xml`
- Inflated by: `partnerwiseloansandperformancehome`
- ViewPager `#pager`
  - PagerTitleStrip visibility=gone

## `partydetailssingle_row.xml`
- Inflated by: `partydetails_adapter`
- RelativeLayout
  - ImageView `#partysingle_thumbnail`
  - TextView `#partysingle_partynoid` text="No"
  - TextView `#partysingle_partynoseparaterid` text="."
  - TextView `#partysingle_partyname` text="Party Name"
  - TextView `#partysingle_collectionareaname` text="Collection Name"
  - TextView `#tvcollectedamountnewid` text="2500" visibility=gone

## `partyledgers_row.xml`
- Inflated by: `partyledgers_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#partyledgersrow_transdateid` text="12-02-2020"
    - LinearLayout
      - LinearLayout
        - TextView `#partyledgersrow_particularsid` text="NEW LOAN"
      - TextView `#partyledgersrow_interestid` text="Total"
      - TextView `#partyledgersrow_loanid` text="Total"
      - TextView `#partyledgersrow_repaymentid` text="Total"

## `partyrearrangelist_row.xml`
- Inflated by: — (not referenced via `R.layout`)
- RelativeLayout `#lytPattern`
  - LinearLayout `#lytPatternColor`
    - LinearLayout `#lytPatternColorDraw`
  - LinearLayout
    - LinearLayout visibility=gone
      - TextView `#textPrimaryDisplayNoId` text="No"
      - TextView text=". "
    - TextView `#textPrimaryDisplayMemberId` text="TextView"
    - TextView `#textSecondaryDisplayMemberId` text="TextView"
  - LinearLayout `#lytGrab`
    - ImageView `#imageViewGrab`

## `partysearchrowfornewloan.xml`
- Inflated by: `customersearchadapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - LinearLayout
        - TextView `#customersearchlist_partynoid` text="No"
        - TextView text="."
        - TextView `#partynameid` text="Party Name" / ta "பார்ட்டி பெயர்"
      - TextView `#tagid` text="Business" / ta "தொழில்"
      - LinearLayout
        - TextView `#partysearchfornewloanrow_mobileno1id` text="mobileno1"
        - TextView `#partysearchfornewloanrow_mobileno2id` text="mobileno2"
    - LinearLayout visibility=gone
      - Button `#showprevloansbutton` text="Prev Loans"

## `pattidetails_loans_row.xml`
- Inflated by: `pattidetails_loans_adapter`
- RelativeLayout
  - LinearLayout
    - ImageView `#pattidetailsloansrow_thumbnail`
    - LinearLayout
      - LinearLayout
        - TextView `#pattidetailsloansrow_partynoid` text="No"
        - TextView `#pattidetailsloansrow_partynoseparaterid` text="."
        - TextView `#pattidetailsloansrow_partynameid` text="Party name"
      - LinearLayout
        - TextView `#pattidetailsloansrow_subareaname` text="Gandhigramam"
      - LinearLayout
        - TextView `#pattidetailsloansrow_loanamountId` text="Loan"
        - TextView `#pattidetailsloansrow_balanceId` text="balance"
        - TextView `#pattidetailsloansrow_statusId` text="Status"
        - TextView `#pattidetailsloansrow_collectedamountId` text="TC"
    - LinearLayout
      - TextView `#pattidetailsloansrow_dueamountId` text="due"

## `periodicloanreport_row.xml`
- Inflated by: `periodicloanreport_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#periodicloanreportrow_partyid` text="Party"
      - TextView `#periodicloanreportrow_loanid` text="Loan"
      - TextView `#periodicloanreportrow_duesid` text="Dues"
      - TextView `#periodicloanreportrow_interestid` text="Interest"
      - TextView `#periodicloanreportrow_mahimaiid` text="Mahimai"
    - LinearLayout
      - TextView `#periodicloanreportrow_areaid` text="Area"
      - TextView `#periodicloanreportrow_loanmethodid`
      - TextView `#periodicloanreportrow_balanceid` text="Balance"
      - TextView `#periodicloanreportrow_loandateid`

## `prevloanclosure_row.xml`
- Inflated by: `prevloanclosure_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#prevloanclosure_partynoid` text="No"
      - TextView `#prevloanclosure_partynameid` text="Party Name"
      - TextView `#prevloanclosure_mcloandateid` text="Loan Date"
      - TextView `#prevloanclosure_mccloseddateid` text="Closed Date"
    - LinearLayout
      - TextView `#prevloanclosure_subareanameid` text="Area"
      - TextView `#prevloanclosure_extracollectionid` text="Extra Coll"
      - TextView `#prevloanclosure_pendingid` text="Pending"
    - LinearLayout visibility=visible
      - TextView text="Loan Date" visibility=gone
      - TextView text="days" visibility=gone

## `prevloanlistrow.xml`
- Inflated by: `prevloanslist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#ploandateid` text="loandate"
      - TextView `#ploannoid` text="loanno"
      - TextView `#ploanamountid` text="loanamount"
    - LinearLayout
      - TextView `#ploanpaidid` text="paid"
      - TextView `#ploandaysid` text="days"
    - LinearLayout
    - TextView `#ploanstatusid` text="Status"

## `prevreceipts_row.xml`
- Inflated by: `prevreceiptslist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#prevreceiptsrow_snoid` text="SNo"
      - TextView `#prevreceiptsrow_transdateid` text="Date"
      - TextView `#prevreceiptsrow_principleid` text="Pri"
      - TextView `#prevreceiptsrow_interestid` text="Interest"

## `prevsavingclosure_row.xml`
- Inflated by: `prevsavingclosure_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#prevsavingclosure_transdateid` text="Date"
      - TextView `#prevsavingclosure_partynameid` text="Name"
      - TextView `#prevsavingclosure_amountid` text="Amount"
      - TextView `#prevsavingclosure_addinterestid` text="Interest"
    - LinearLayout
      - TextView `#prevsavingclosure_subareanameid` text="Sub Area Name"

## `proposedcashbookrow.xml`
- Inflated by: `proposedcashbook_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#ppcb_particularsid` text="Party Name"
    - TextView `#ppcb_debitid` text="debit"
    - TextView `#ppcb_creditid` text="credit"
  - TextView `#tvcollectedamountnewid` text="2500" visibility=gone

## `selectedloancollection_row.xml`
- Inflated by: `selectedloancollectionadapter`
- RelativeLayout
  - LinearLayout `#row3`
    - TextView `#selectedloancoll_transdateid` text="Date"
    - TextView `#selectedloancoll_collectedamountid` text="Paid"
    - TextView `#selectedloancoll_Balanceamountid` text="Balance"
    - TextView `#selectedloancoll_savingscollectedamountid` text="Savings"
    - TextView `#selectedloancoll_interestcollectedamountid` text="Interest"

## `sevendayscollectionrow.xml`
- Inflated by: `sevendaycollectionlistadapter`
- RelativeLayout
  - LinearLayout `#firstrowid`
    - CheckBox `#isattentionrequiredcheckboxid`
    - TextView `#sevendayscollectionpartynameid` text="Party Name"
    - TextView `#sevendayscollectionreportidentificatonid` text="LM"
  - LinearLayout `#lytloandetailsid`
    - TextView `#sdcloanamountid` text="loan"
    - TextView `#sdccollectedamountid` text="collec"
    - TextView `#sdcloandateid` text="ldate"
    - TextView `#sdcloanclosingdateid` text="Cdate"
    - TextView `#sdcloanstatusidd` text="status"
  - LinearLayout `#lytdatedetails`
    - TextView `#day1dateid` text="12-10-20"
    - TextView `#day2dateid` text="12-10-20"
    - TextView `#day3dateid` text="12-10-20"
    - TextView `#day4dateid` text="12-10-20"
    - TextView `#day5dateid` text="12-10-20"
    - TextView `#day6dateid` text="12-10-20"
    - TextView `#day7dateid` text="12-10-20"
  - LinearLayout
    - TextView `#day1id` text="||"
    - TextView `#day2id` text="||"
    - TextView `#day3id` text="||"
    - TextView `#day4id` text="||"
    - TextView `#day5id` text="||"
    - TextView `#day6id` text="||"
    - TextView `#day7id` text="||"
  - TextView `#tvcollectedamountnewid` text="2500" visibility=gone

## `simplethittam_row.xml`
- Inflated by: `simplethittam_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#simplethittamrow_particularsid` text="Particulars"
      - TextView `#simplethittamrow_ledgernameid` text="Ledger"
    - TextView `#simplethittamrow_debitid` text="Debit"
    - TextView `#simplethittamrow_creditid` text="Credit"

## `spinner_item.xml`
- Inflated by: `selectbranchandfinance`
- TextView

## `spinnersearch_row.xml`
- Inflated by: `spinnersearch_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#spinnersearchrow_displaymemberid` text="Party Name" / ta "பார்ட்டி பெயர்"
      - TextView `#spinnersearchrow_valuememberid` visibility=gone

## `staffgpstrackmap.xml`
- Inflated by: — (not referenced via `R.layout`)
- RelativeLayout `#main`
  - LinearLayout `#sgt_headerid`
    - LinearLayout
      - TextView `#sgt_staffnameid`
      - TextView `#sgt_lastseenid`
    - TextView `#sgt_trackdateid`
  - LinearLayout `#sgt_summarybarid`
    - LinearLayout
      - TextView text="Distance"
      - TextView `#sgt_distanceid`
    - LinearLayout
      - TextView text="Points"
      - TextView `#sgt_pointcountid`
    - LinearLayout
      - TextView text="Collections"
      - TextView `#sgt_collectioncountid`
    - LinearLayout `#sgt_mockcolid` visibility=invisible
      - TextView text="Fake GPS"
      - TextView `#sgt_mockcountid`
  - FrameLayout
    - fragment `#sgt_mapid`
  - TextView `#sgt_nodataid` text="No location recorded for this day" visibility=gone
  - ProgressBar `#sgt_pbarid` visibility=gone

## `stloanslist_row.xml`
- Inflated by: `fintranslist_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#stloanslist_ledgernameid` text="Acc Ledger Name"
      - TextView `#stloanslist_addressid` text="Address"
      - LinearLayout
        - TextView `#stloanslist_finnameid` text="fin"
        - TextView `#stloanslist_loanstatusid` text="Status"
    - LinearLayout
      - TextView `#stloanslist_loandateid` text="Date"
      - TextView `#stloanslist_loanamountid` text="Amount"
      - TextView `#stloanslist_intrateid` text="Int Rate"

## `stockcat_row.xml`
- Inflated by: `stockUOM_adapter`, `stockcat_adapter`, `stockgrp_adapter`
- RelativeLayout
  - TextView `#accledgers_ledgername` text="Acc Ledger Name"
  - TextView `#tvcollectedamountnewid` text="2500" visibility=gone

## `stockinhand_row.xml`
- Inflated by: `stockinhand_adapter`
- RelativeLayout
  - TextView `#accledgers_ledgername` text="Acc Ledger Name"
  - TextView `#accledgers_itembalance` text="Item Balance"

## `subarealistforsevendayscollection_row.xml`
- Inflated by: `collectionaarealistadapterforsevendayscollection`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#subarealistforsevendayscollection_areanameid` text="Area Name"
      - TextView `#subarealistforsevendayscollection_noofpartiesid` text="Collected"

## `tabbedcollectionlistrow.xml`
- Inflated by: `editing_collectionsadapter`, `tabbedcollectionlistadapter`, `tabbedcollectionlistadaptermviewpager`
- RelativeLayout
  - LinearLayout
    - TextView `#tcl_subareanameid` text="SubAreaName" visibility=gone
    - LinearLayout `#tcl_collectiondetailslnrlayout`
      - ImageView `#collectionlistrow_thumbnail`
      - LinearLayout
        - LinearLayout
          - TextView `#tcl_partynoid` text="No"
          - TextView `#tcl_partynoseparaterid` text=":"
          - TextView `#tcl_partynameid` text="Party name"
        - LinearLayout
          - TextView `#tcl_loanamountId` text="Loan"
          - TextView `#tcl_collectedamtId` text="Coll"
          - TextView `#tcl_balanceId` text="bal"
          - TextView `#tcl_statusId` text="Stas"
          - CheckBox `#tcl_isvisitedid`
          - EditText `#tcl_collectedamountId` text="100" inputType=number
      - LinearLayout
        - TextView `#tcl_loanmethodid` text="100d"
        - TextView `#tcl_dueamountId` text="DUE"
    - LinearLayout `#tcl_prevcollectiondetilslnrlayout`
      - LinearLayout
        - TextView `#tcl_firstdaylabelid` text="12-2"
        - TextView `#tcl_firstdayvalueid` text="100"
      - LinearLayout
        - TextView `#tcl_seconddaylabelid` text="13-2"
        - TextView `#tcl_seconddayvalueid` text="100"
      - LinearLayout
        - TextView `#tcl_thirddaylabelid` text="14-2"
        - TextView `#tcl_thirddayvalueid` text="100"
      - LinearLayout
        - TextView `#tcl_fourthdaylabelid` text="15-2"
        - TextView `#tcl_fourthdayvalueid` text="100"
      - LinearLayout
        - TextView `#tcl_fifthdaylabelid` text="16-2"
        - TextView `#tcl_fifthdayvalueid` text="100"
    - LinearLayout
      - LinearLayout `#tcl_savingslnrlayout` visibility=gone
        - TextView text="Savings"
        - EditText `#tcl_savingsetid` text="100" inputType=numberSigned
      - LinearLayout `#tcl_penaltylnrlayout` visibility=gone
        - TextView text="Penalty"
        - EditText `#tcl_penaltyetid` text="100" inputType=numberSigned

## `tbbspl_particulars_row.xml`
- Inflated by: `tbbspl_particulars_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#tbbsplpartrow_particularsid` text="SALARY EXPENSES"
    - TextView `#tbbsplpartrow_debitid`
    - TextView `#tbbsplpartrow_creditid`

## `tbbspl_row.xml`
- Inflated by: `tbbspl_adapter`
- RelativeLayout
  - TextView `#tbbsplrow_ledgername` text="Acc Ledger Name"
  - LinearLayout
    - TextView `#tbbsplrow_crvalueid`
    - TextView `#tbbsplrow_drvalueid`

## `todaycollectionabstract_row.xml`
- Inflated by: `todaycollectionabstract_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#todaycollectionabstractrow_collectiontypeid` text="100 days col"
    - TextView `#todaycollectionabstractrow_partiescountid` text="250"
    - TextView `#todaycollectionabstractrow_collectedamountid` text="20050"

## `todaycollectionlist_row.xml`
- Inflated by: `todaycollectionlistadapter`
- ConstraintLayout
  - TextView `#partynoid`
  - TextView `#partynameid` text="Party Name"
  - TextView `#loannoloandateloanamountid` text="LoanNo,LoanDate,Collected"
  - TextView `#tagid` text="subarea"
  - LinearLayout `#extraamtcalc` visibility=gone
    - TextView `#tvtotalcollection` text="000" visibility=gone
    - TextView `#tvcurdue` text="000" visibility=gone
    - TextView `#tvextracollected` text="000" visibility=gone
  - LinearLayout `#amountGroup2`
    - TextView `#tvcollectedamountnewid` text="000"
    - TextView `#tvbankcollectionid` text="000"

## `translist_row.xml`
- Inflated by: `transist_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#translistrow_snoid` text="S No: " visibility=gone
    - TextView `#translistrow_billnoid` text="Bill No "
    - TextView `#translistrow_udlotnoid` text="Lot No: " visibility=gone
    - TextView `#translistrow_dateid` text="Date"
    - TextView `#translistrow_typeid` text="Type" visibility=gone
    - TextView `#translistrow_particulars` text="Particulars"
    - TextView `#translistrow_transvalue` text="Value"

## `uploadcontrolingrow.xml`
- Inflated by: `uploadcontrolingadabter`
- RelativeLayout
  - LinearLayout
    - TextView `#uploaddetailsfinancenameid` text="Finance Name"
    - TextView `#finidid` text="finid" visibility=gone
    - SwitchCompat `#swallowupload`

## `userroleprevilages_row.xml`
- Inflated by: `userroleprevilages_adapter`
- RelativeLayout
  - LinearLayout
    - LinearLayout
      - TextView `#userroleprevilagesrow_activitynametv` text="Customer Details"
    - LinearLayout
      - CheckBox `#userroleprevilagesrow_showstatechk` text="Show"
      - CheckBox `#userroleprevilagesrow_addtatechk` text="Add"
      - CheckBox `#userroleprevilagesrow_viewstatechk` text="View"
      - CheckBox `#userroleprevilagesrow_updatestatechk` text="Up"
      - CheckBox `#userroleprevilagesrow_deletestatechk` text="Del"

## `weekday_selection_sheet.xml`
- Inflated by: `collectionareassearchlist`
- LinearLayout
  - TextView text="Select a Weekday"
  - ListView `#weekdayListView`

## `weeklycollectionanalysis_row.xml`
- Inflated by: `weeklycollectionanalysis_adapter`
- RelativeLayout
  - LinearLayout
    - TextView `#weeklyanalysis_weeknoid` text="எண்"
    - TextView `#weeklyanalysis_weekdateid` text="வாரம்"
    - TextView `#weeklyanalysis_collectionid` text="வசூல்"
    - TextView `#weeklyanalysis_balanceid` text="நிலுவை"

