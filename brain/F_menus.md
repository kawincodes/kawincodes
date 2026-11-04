# Appendix F — Menu resources

Every app menu file with its items (id → title, icon, visibility) and the classes that inflate it.

## `activity_navigation_drawer.xml`
- Inflated by: — (navigation view / unused)
- `executive_trans` "Executive Trans"
  - `nav_sync` "Sync Resources" · icon @drawable/ic_menu_sync
  - `nav_selectline` "Select Line" · icon @drawable/ic_menu_login
  - `nav_selectbranch` "Select Branch" · icon @drawable/ic_action_branch
  - `nav_collectionareas` "Collection Areas" / ta "வசூல் ஏரியா" · icon @drawable/ic_menu_areas
  - `nav_receivemoney` "Amount Received" / ta "பணம் பெறுதல்" · icon @drawable/ic_action_receivemoney
  - `nav_returnmoney` "Amount Paid" / ta "பணம் செலுத்துதல்" · icon @drawable/ic_action_paymoney
- `nav_offlineexecutivehanlding` "Executive Entries"
  - `nav_newloans` "New Loans" / ta "புதிய கடன்கள்" · icon @drawable/ic_menu_newloan
  - `nav_newparties` "New Parties" / ta "புதிய பார்ட்டி" · icon @drawable/ic_menu_parties
  - `nav_partytransfer` "Party Transfer" / ta "ஊர் மாற்றம் செய்தல்" · icon @drawable/ic_action_transfer
  - `nav_SavingsClosures` "Savings Account Closures" / ta "சேமிப்பு திட்ட முடிப்பு" · icon @drawable/ic_action_savingsclosure
  - `nav_newcollection` "Today Collection" / ta "இன்றைய வசூல்" · icon @drawable/ic_action_collection
  - `nav_expensesentrlistnew` "Expenses" / ta "செலவுகள்" · icon @drawable/ic_menu_expenses
  - `nav_incomes` "Incomes" / ta "வரவுகள்" · icon @drawable/ic_action_incomes
  - `nav_import` "Download Data" · icon @drawable/ic_menu_download
  - `nav_UploadData` "Upload Data" · icon @drawable/ic_menu_upload
- `nav_onlineexecutivehanlding` "Executive Entries"
  - `nav_collectionentry` "Collection Entry" · icon @drawable/ic_collectionentry
  - `nav_loansfortoday` "Loans" / ta "கடன்கள் விவரம்" · icon @drawable/ic_action_loans
  - `nav_executivepayments` "Payments" / ta "செலவுகள்" · icon @drawable/ic_action_quickpayments
  - `nav_executivereceipts` "Incomes" / ta "வரவுகள்" · icon @drawable/ic_action_quickpayments
  - `nav_onlinepartytransfer` "Party Transfer" / ta "ஊர் மாற்றம் செய்தல்" · icon @drawable/ic_action_transfer
- `nav_inventorycontrols` "Inventory Master" · **hidden by default**
  - `nav_StockCat` "Stock Cats" · icon @drawable/ic_action_stockcat
  - `nav_StockGrp` "Stock Groups" · icon @drawable/ic_action_stockgrp
  - `nav_StockUOM` "Stock UOM" · icon @drawable/ic_action_stockuom
  - `nav_itemmaster` "Item Master" · icon @drawable/ic_action_itemmaster
  - `nav_supplier` "Suppliers" · icon @drawable/ic_action_supplier
  - `nav_stockpurchase` "Stock Purchase" · icon @drawable/ic_menu_download
  - `nav_StockinHand` "Stock in Hand" · icon @drawable/ic_action_stockinhand
- `nav_admincontrols` "Admin"
  - `nav_uploadcontrol` "Upload Control" · icon @drawable/ic_action_uploadcontrol
  - `nav_downloadcontrol` "Download Control" · icon @drawable/ic_action_downloadcontrol
  - `nav_LoanClosures` "Loan Closures" / ta "கணக்கு முடித்தல்" · icon @drawable/ic_action_loancosoure
  - `nav_loanparties` "Loan Parties" / ta "கடன்தாரர்கள்" · icon @drawable/ic_action_loanparties
  - `nav_loans` "Loans" / ta "கடன்கள் விவரம்" · icon @drawable/ic_action_loans
  - `nav_loanscollection` "Loans Collection" / ta "கடன் வசூல் விவரம்" · icon @drawable/ic_action_loanscollection
  - `nav_branchemployee` "Branch Employee" · icon @drawable/ic_action_employee
  - `nav_branchaccesscontrol` "Branch Access Control" · icon @drawable/ic_action_branchaccesscontrol
  - `nav_employee` "Employee" · icon @drawable/ic_action_employee
  - `nav_accesscontrol` "Access Control" · icon @drawable/ic_action_accesscontrol
  - `nav_loanmethods` "Loan Methods" / ta "கடன் முறைகள்" · icon @drawable/ic_action_loanmethods
  - `nav_accledgers` "Account Ledgers" / ta "பேரேடுகள்" · icon @drawable/ic_action_accledgers
  - `nav_prevsavingclosure` "Prev Saving Closure" / ta "முடிக்கப்பட்ட சேமிப்பு க/கு" · icon @drawable/ic_action_prevsavingclosure
  - `nav_partnerdetails` "Partner details" · icon @drawable/ic_action_partners
  - `nav_finwisepartners` "Finwise partners" · icon @drawable/ic_action_finwisepartner
  - `nav_financedetalis` "Finance/Line Details" · icon @drawable/ic_action_financemaster
- `reports` "Reports"
  - `nav_cashbook` "Cash/Bank Book" / ta "ரொக்கம்/வங்கி பதிவேடு" · icon @drawable/ic_action_cashbook
  - `nav_cashbookabstract` "Cash Abstract" / ta "ரொக்க பட்டியல்" · icon @drawable/ic_action_cashbookabstract
  - `nav_handloansreport` "HandLoans" / ta "கைமாத்து நிலுவை" · icon @drawable/ic_action_hlreports
  - `nav_unpaidparties` "Unpaid Parties" / ta "கட்டாதவர்கள்" · icon @drawable/ic_action_unpaidparties
  - `nav_collectionreporstprinting` "Collection List Print" / ta "வசூல் பட்டியல் அச்சு" · icon @drawable/ic_collectionlistprint
  - `nav_closedandrenewedloans` "Parties to be Reviewed" / ta "முடிந்த கடன்கள்" · icon @drawable/ic_action_partiestoberenewed
  - `nav_newpartyloans` "New Party Loans" / ta "புதிய கடன்தாரர்கள்" · icon @drawable/ic_action_newpartynewloan
  - `nav_todaycollection` "Collection for a Day" / ta "நாளது வசூல்" · icon @drawable/ic_action_collection
  - `nav_todaycollectionabstract` "Collection Abstract for a Day" / ta "நாளது வசூல் சுருக்கம்" · icon @drawable/ic_action_todaycollectionabstract
  - `nav_sevendayscollection` "Seven Days Collection" / ta "ஏழுநாள் வசூல்" · icon @drawable/ic_action_sevendayscollection
  - `nav_allfinancedailythittam` "Daily Thittam" / ta "தினத்திட்டம்" · icon @drawable/ic_action_collection
  - `nav_allfinancedailypatti` "Daily Patti" / ta "தினப்பட்டி" · icon @drawable/ic_action_daiypatti
  - `nav_ledgerstatements` "Ledger Statements" / ta "பேரேட்டுக் கணக்கு" · icon @drawable/ic_action_ledgers
  - `nav_simplethittam` "Simple Thittam" / ta "திட்டம்" · icon @drawable/ic_action_simplethittam
  - `nav_Daywisepending` "Daywise Pending" / ta "நிலுவை(நாள் வரியாக)" · icon @drawable/hourglass_pause_24px
  - `nav_attendance` "Attendance" · icon @drawable/reduce_capacity_24px
  - `nav_varathittam` "Varathittam" · icon @drawable/fact_check_24px · **hidden by default**
  - `nav_varathittamanalysis` "Varathittam Analysis" / ta "வார திட்டம்" · icon @drawable/fact_check_24px
- `nav_accounttransactions` "Accounts Trans"
  - `nav_quickpayments` "Payments" / ta "செலவுகள்" · icon @drawable/ic_action_quickpayments
  - `nav_quickreceipts` "Receipts" · icon @drawable/ic_action_quickreceipts
  - `nav_quickcontraentries` "Contra" / ta "பரிமாற்றம்" · icon @drawable/ic_action_contra
  - `nav_quickjournalentries` "Journal" / ta "சரிக்கட்டுப் பதிவுகள்" · icon @drawable/ic_action_journalentries
  - `nav_quicksalaryadvacepaymententries` "Salary Advance Payment" / ta "சம்பள முன்பணம்" · icon @drawable/ic_action_contraentries
  - `nav_quicksalaryadvancereceiptentries` "Salary Advance Receipt" / ta "முன்பண வரவு" · icon @drawable/ic_action_journalentries
  - `nav_capitalreceipts` "Capital Receipts" / ta "முதலீடு வரவு" · icon @drawable/ic_action_capitalreceipts
  - `nav_capitalreturns` "Capital Returns" / ta "முதலீடு பற்று" · icon @drawable/ic_action_capitalreturns
  - `nav_handloanspayment` "HL Payments" / ta "கைமாத்து செலவு" · icon @drawable/ic_action_handloanspayment
  - `nav_handloansreceipt` "HL Receipts" / ta "கைமாத்து வரவு" · icon @drawable/ic_action_handloansreceipt
- `nav_stloanfinancetransactions` "ST Loans" / ta ""ST Loans  ""
  - `nav_stloans` "ST Loans" · icon @drawable/ic_action_loans
  - `nav_stloanint` "ST Loans Int" · icon @drawable/ic_action_loans
  - `nav_PartyLedgersSTLoans` "ST Loans Parties" · icon @drawable/ic_menu_parties
  - `nav_STLoanAnalysis` "ST Loan Analysis" · icon @drawable/ic_action_loans
  - `nav_LoanLedgersSTLoans` "ST Loans Ledgers" · icon @drawable/ic_action_loans
- `nav_ofloanfinancetransactions` "OF Loans" / ta ""OF Loans  ""
  - `nav_ofloans` "OF Loans" · icon @drawable/ic_action_loans
  - `nav_ofloanint` "OF Loans Int" · icon @drawable/ic_action_loans
  - `nav_PartyLedgersOFLoans` "OF Loans Parties" · icon @drawable/ic_menu_parties
  - `nav_OFLoanAnalysis` "OF Loan Analysis" · icon @drawable/ic_action_loans
  - `nav_LoanLedgersOFLoans` "OF Loans Ledgers" · icon @drawable/ic_action_loans
- `nav_accountsreports` "Accounts Reports"
  - `nav_MonthwisePanL` "Monthwise P and L" · icon @drawable/ic_action_mpandl
  - `nav_TrailBalance` "Trail Balance" · icon @drawable/ic_action_trailbalance
  - `nav_Balancesheet` "Balancesheet" · icon @drawable/ic_action_balancesheet
  - `nav_ProfitandLossStatements` "Profit and Loss Statements" · icon @drawable/ic_action_pandl
- `settingsmenu` "Settings"
  - `nav_appearancesettings` "Settings" · icon @drawable/ic_action_appearancesettings
  - `nav_Andriodmenusettings` "Menu Access Control" · icon @drawable/ic_menuaccesscontrol · **hidden by default**
  - `nav_specialaccesssettings` "Special Access Control" · icon @drawable/ic_action_specilacess · **hidden by default**
- `navaboutus` "About us"
  - `nav_help` "Help" · icon @drawable/ic_action_aboutus
  - `nav_venuswebsite` "Venus Solutions" · icon @drawable/ic_action_aboutus
  - `nav_deviceid` "Device Identity" · icon @drawable/ic_action_deviceidentity
- `nav_exit` "Exit"
  - `nav_clearmydata` "Clear My Data" · icon @drawable/ic_action_clearmydata
  - `nav_closeapp` "Close APP" · icon @drawable/ic_action_close

## `bluetooth_printer_main.xml`
- Inflated by: `BluetoothPrinterMain`
- `action_clear_device` "clear_device" · showAsAction never
- `action_connect_device` "connect_device" · showAsAction never
- `action_unpair_device` "unpair_device" · showAsAction never

## `menu_cashbook.xml`
- Inflated by: `cashbook`
- `action_detailedcashbook` "Detailed" · showAsAction never
- `action_abstractcashbook` "Abstract" · showAsAction never
- `action_printcashbookid` "Print Cash Book" · showAsAction never

## `menu_collectionediting.xml`
- Inflated by: `editing_collections_act`, `editing_collectionswithfulloptions_act`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction ifRoom
- `collectionediting_save` "Save Collections" · showAsAction never
- `collectionediting_delete` "Delete Collections" · showAsAction never
- `collectionediting_finish` "Finish this Collection" · showAsAction never
- `action_printandshareCollectionlist` "Print Share Options" · showAsAction never

## `menu_expensesheaders.xml`
- Inflated by: `accounttransactions_act`
- `action_settings` "Refresh Headers" · showAsAction never
- `translistid` "LIST" · icon @drawable/ic_action_translist · showAsAction always

## `menu_listingicononly.xml`
- Inflated by: `invtransactnew`, `loanclosoureentry`
- `translistid` "LIST" · icon @drawable/ic_action_translist · showAsAction always

## `menu_meetingreports.xml`
- Inflated by: `meetingreport`
- `action_refreshlist` "Refresh List" · showAsAction never
- `action_downloadlist` "Download List" · showAsAction never
- `action_printlist` "Print List" · showAsAction never

## `menu_newsevendayscollection.xml`
- Inflated by: `newsevendayscollection`
- `action_settings` "Cash Book" · showAsAction never

## `menu_partyrearrange.xml`
- Inflated by: `collectionarearearrangeact`, `partyrearrange`
- `action_saveandsync` "Save and Sync" · showAsAction never

## `menu_reloadrefreshbranches.xml`
- Inflated by: `refreshorreloadbrancheslist`
- `action_Refresh` "Refresh/Load Branch list" · showAsAction never

## `menu_routelogin.xml`
- Inflated by: `refreshorreloadfinancelist`
- `action_Refresh` "Refresh/Load Financelist" · showAsAction never

## `menu_selectedloancollecteddetails.xml`
- Inflated by: `selectedloancollectiondetails`
- `(no id)` "Close Loan" · **hidden by default** · showAsAction never
- `action_printandshareCollectionlist` "Print and Share Options" · showAsAction never

## `menu_sevendayscollection.xml`
- Inflated by: `sevendayscollectionforselline`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `(no id)` "Prev Week" · **hidden by default** · showAsAction never
- `(no id)` "Next Week" · **hidden by default** · showAsAction never

## `menu_todaycollection_sort.xml`
- Inflated by: `todaycollectionlist`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `sort_party_no_asc` "Party No ↑"
- `sort_party_no_desc` "Party No ↓"
- `sort_ud_pos_asc` "Regular"
- `sort_loandate_asc` "Loan Date ↑"
- `sort_loandate_desc` "Loan Date ↓"

## `menu_uploadalloweddetails.xml`
- Inflated by: `uploaddownloadcontroling`
- `action_saveuploadcontrol` "Save" · showAsAction never

## `menucollectionareasearchlist.xml`
- Inflated by: `closed_renewedloanslist_act`, `collectionareassearchlist`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `action_rearrangeareas` "Rearrange Areas"
- `action_showareainmap` "Show in Map"

## `menucustomersearchlist.xml`
- Inflated by: `closablesavings_list`, `dailypatti_kadanpatrulist_act`, `dailypatti_nadapunippucollectionlist_act`, `dailypatti_varakadanillapulist_act`, `fintrans_list`, `parties_list`, `partylistwithprevloansfornewloanselection`, `pattidetails_loans_act`, `prevloanclosure_list_act`, `prevloanslist_act`, `tabbedcollectionlist`, `tabbedcollectionlistmviewpager`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `partyrearrangemenu` "Rearrange Parties"
- `showpartiesinmapmenu` "Show Parties in MAP"

## `menuemployeesearchlist.xml`
- Inflated by: `branchemployeelist`, `branchemployeesearchlist`, `employeesearchlist`, `ofloans_interestcalculationandpayments`, `stloaninterestcalculationandpayments`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `actionmenu_showallemployees` "Show all Employees"
- `actionmenu_showactiveemployees` "Show Active Employees"

## `menuemptylist.xml`
- Inflated by: `financewisepartners_list`
- `action_search` "Search" · icon @drawable/ic_action_search · **hidden by default** · showAsAction always

## `menuemptylistwithsearchbutton.xml`
- Inflated by: `customers_list`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always

## `menuonlywithsearchoption.xml`
- Inflated by: `accledgers_searchlist`, `accounttrans_list`, `cashbookabstract`, `collectionreportprintingmethods_searchlist`, `daywisependingreport_act`, `documentsphotossearchlist`, `editing_collectionlist_act`, `expensesentry_searchlist`, `inventorytransactionslist_act`, `itemmaster_list`, `loanmethods_searchlist`, `loanslist_act`, `newloanslist_act`, `newpartieslist_act`, `newpartiesnewloanslist_act`, `newtodaycollectionofflinedata_list`, `partydetails_searchlist`, `periodicloareportlist_act`, `prevsavingclosure_list_act`, `spinnersearch_list`, `stockUOM_list`, `stockcat_list`, `stockgrp_list`, `stockinhand_list`, `tbbspl_particulars`, `todaycollectionabstractlist`, `weeklycollectionanalysis_act`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always

## `menuwithoutsearchandprintoptions.xml`
- Inflated by: `dailypatti_nadapunippuphakkilist_act`, `dailypattinew_act`, `dailythittamforallowedlines`, `simplethittamlist_act`, `singlefinancedailythittam_act`
- `action_search` "Search" · icon @drawable/ic_action_search · **hidden by default** · showAsAction always
- `menuwithprintingoptions_directprint` "Direct Print"
- `menuwithprintingoptions_showaspdf` "Show as PDF"
- `menuwithprintingoptions_shareaspdf` "Share"
- `menuwithprintingoptions_refreshheaders` "Refresh Ledgers"

## `menuwithsearchandorderbyoptions.xml`
- Inflated by: `closableloanssearchlistact`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `ordersbycolumn_cwledgerid` "Order by Party No"
- `ordersbycolumn_loandate` "Order by Loan Date"
- `ordersbycolumn_balance` "Order by Balance"

## `menuwithsearchandprintoptions.xml`
- Inflated by: `handloans_list`, `partyledgers_list`, `tbbspl_list`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `menuwithprintingoptions_directprint` "Direct Print"
- `menuwithprintingoptions_showaspdf` "Show as PDF"
- `menuwithprintingoptions_shareaspdf` "Share"

## `menuwithsearchandprintoptionsforloanledgers.xml`
- Inflated by: `loanledgers_list`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `menuwithprintingoptions_directprint` "Direct Print"
- `menuwithprintingoptions_showaspdf` "Show as PDF"
- `menuwithprintingoptions_shareaspdf` "Share"
- `menuwithprintingoptions_showloans` "Show Loans only"
- `menuwithprintingoptions_showreturns` "Show Returns only"
- `menuwithprintingoptions_showall` "Show Loans and Returns"

## `mnu_loanindexwiseloans.xml`
- Inflated by: `loanindexloanswiselist_act`
- `action_search` "Search" · icon @drawable/ic_action_search · showAsAction always
- `mnurefreshparties` "Refresh" · icon @drawable/ic_action_rearange

## `navigationdrawer.xml`
- Inflated by: `navigationdrawactivity`

## `partnerwiseperformance_menu.xml`
- Inflated by: `BaseFragment`
- `partnerwiseperformancemenu_directprint` "Direct Print" · **hidden by default**
- `partnerwiseperformancemenu_showaspdf` "Show as PDF"
- `partnerwiseperformancemenu_shareaspdf` "Share"

## `simplemenu.xml`
- Inflated by: `employeesaccessearchlist`, `mobiledeviceaccescontrol_act`
- `action_search` "Search" · icon @drawable/ic_action_search · **hidden by default** · showAsAction always

