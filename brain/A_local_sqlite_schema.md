# Appendix A — Local SQLite schema (`venusmicrofinv005`)

Every `CREATE TABLE` statement found in the app code, grouped by table. The app creates tables ad hoc (`CREATE TABLE IF NOT EXISTS …`) just before it fills them, so the same table is sometimes defined in several classes — every distinct definition is listed with the classes that issue it. SQLite is dynamically typed, so declared types such as `String`, `Double`, `date` are only affinities.

**70 tables.** "Read/written by" lists classes whose SQLite calls (`rawQuery`/`execSQL`/`compileStatement`) mention the table.

## `BankLedgers`
- Read/written by (3): `defaulttables`, `navigationdrawactivity`, `refreshorreloadfinancelist`
- Definition 1 (created in `navigationdrawactivity`, `refreshorreloadfinancelist`):
  ```sql
  CREATE TABLE IF NOT EXISTS BankLedgers (
    LedgerId int,
    Ledgername String,
    FinId int
  )
  ```

## `BranchAccessAllowed`
- Read/written by (4): `branchemployeelist`, `branchemployeesaccessearchlist`, `branchmobiledeviceaccescontrol_act`, `refreshorreloadbrancheslist`
- Definition 1 (created in `branchemployeelist`, `branchemployeesaccessearchlist`, `refreshorreloadbrancheslist`):
  ```sql
  CREATE TABLE IF NOT EXISTS BranchAccessAllowed (
    IMEI String,
    BId int
  )
  ```

## `BranchConnStrs`
- Read/written by (5): `branchemployeelist`, `branchemployeesaccessearchlist`, `branchmobiledeviceaccescontrol_act`, `refreshorreloadbrancheslist`, `selectbranchandfinance`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS BranchConnStrs (
    BId int,
    BranchName String,
    ConStr String,
    FinCashLedgerId int,
    CollectionTeamCashLedgerId int,
    ServerName String,
    DatabaseName String,
    DBUserId String,
    DBPassword String,
    appconstr String,
    dbidentificationno String,
    ShowId int
  )
  ```

## `BranchEMPLedgers`
- Read/written by (3): `branchemployeelist`, `branchemployeesaccessearchlist`, `defaulttables`
- Definition 1 (created in `branchemployeelist`, `branchemployeesaccessearchlist`):
  ```sql
  CREATE TABLE IF NOT EXISTS BranchEMPLedgers (
    LedgerId Double,
    LedgerName String,
    GroupId Double,
    FinId int,
    Tag String,
    AddLine1 String,
    AddLine2 String,
    City String,
    PinCode String,
    MobileNo1 String,
    MobileNo2 String,
    EMail String,
    designationid int,
    desigstatusid int,
    ScreenType int
  )
  ```

## `BranchMobileDevlicesAllowed`
- Read/written by (2): `branchemployeelist`, `branchemployeesaccessearchlist`
- Definition 1 (created in `branchemployeelist`, `branchemployeesaccessearchlist`):
  ```sql
  CREATE TABLE IF NOT EXISTS BranchMobileDevlicesAllowed (
    MDAId int,
    IMEI String,
    LId Double,
    isActive Boolean,
    isbranchusercontrolallowed Boolean
  )
  ```

## `BranchSubAreaDetails`
- Read/written by (1): `partydetails_searchlist`
- Definition 1 (created in `partydetails_searchlist`):
  ```sql
  CREATE TABLE IF NOT EXISTS BranchSubAreaDetails (
    SubAreaNo Double,
    SubAreaName varchar,
    FinId int,
    IsVisible Boolean,
    ShowId Double
  )
  ```

## `CashBankLedgers`
- Read/written by (3): `defaulttables`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS CashBankLedgers (
    LedgerId int,
    GroupId int,
    LedgerName String,
    Finid int,
    Alloted boolean,
    LedgerType Int,
    istobeVerified boolean
  )
  ```

## `CashBook`
- Read/written by (2): `cashbook`, `navigationdrawactivity`
- Definition 1 (created in `cashbook`, `navigationdrawactivity`):
  ```sql
  CREATE TABLE IF NOT EXISTS CashBook (
    SNo int,
    Particulars varchar,
    debit int,
    credit int
  )
  ```

## `CashBookAbstract`
- Read/written by (1): `cashbook`
- Definition 1 (created in `cashbook`):
  ```sql
  CREATE TABLE IF NOT EXISTS CashBookAbstract (
    SNo int,
    Particulars varchar,
    debit int,
    credit int
  )
  ```

## `closableliveloans`
- Read/written by (5): `closableloanssearchlistact`, `loanclosoureentry`, `navigationdrawactivity`, `refreshorreloadfinancelist`, `selectbranchandfinance`
- Definition 1 (created in `closableloanssearchlistact`, `navigationdrawactivity`, `refreshorreloadfinancelist`, `selectbranchandfinance`):
  ```sql
  CREATE TABLE IF NOT EXISTS closableliveloans (
    MCLoanId Double,
    FWUDMCLoanId Double,
    Remarks String,
    PartyLedgerId Double,
    MCLoanDate date,
    ClosingDate String,
    CWLedgerId Double,
    UDLedgerId Int,
    PartyName String,
    SubAreaNo Double,
    LAmount Double,
    DuesCount Double,
    InsAmount Int,
    MCLoanAmountCollectable Double,
    PrevCollected Int,
    PaidDues Int,
    PassedDues Int,
    LoanMethodDesc String,
    ReportIdentification String,
    CollectionBreakString String,
    NoOfIns Double,
    CBCount Int,
    istobeWarned Int,
    IsNIP Int,
    LoanStatus Double,
    SubAreaName String
  )
  ```

## `CollectionExecutives`
- Read/written by (6): `downloaddata`, `editing_collectionswithfulloptions_act`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`, `uploadcollection`
- Definition 1 (created in `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS CollectionExecutives (
    LedgerId Double,
    LedgerName VarChar,
    FinId int
  )
  ```
- Definition 2 (created in `refreshorreloadbrancheslist`):
  ```sql
  CREATE TABLE IF NOT EXISTS CollectionExecutives (
    LedgerId Double,
    LedgerName VarChar
  )
  ```

## `deptempPartyLevelPending`
- Read/written by (0): —
- Definition 1 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS deptempPartyLevelPending (
    PartyLedgerId Double,
    Ledgername String,
    Balance Double,
    Interest Double
  )
  ```

## `EMPLedgers`
- Read/written by (4): `branchemployeesearchlist`, `defaulttables`, `employeesaccessearchlist`, `employeesearchlist`
- Definition 1 (created in `branchemployeesearchlist`, `employeesaccessearchlist`, `employeesearchlist`):
  ```sql
  CREATE TABLE IF NOT EXISTS EMPLedgers (
    LedgerId Double,
    LedgerName String,
    GroupId Double,
    FinId int,
    Tag String,
    AddLine1 String,
    AddLine2 String,
    City String,
    PinCode String,
    MobileNo1 String,
    MobileNo2 String,
    EMail String,
    designationid int,
    desigstatusid int,
    ScreenType int
  )
  ```

## `ExpensesEntries`
- Read/written by (10): `cashbook`, `downloaddata`, `editing_collectionswithfulloptions_act`, `expensesandreceipt_offlineentry`, `expensesentry_searchlist`, `navigationdrawactivity`, `otpverificationact`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`, `uploadcollection`
- Definition 1 (created in `downloaddata`, `editing_collectionswithfulloptions_act`, `expensesandreceipt_offlineentry`, `otpverificationact`):
  ```sql
  CREATE TABLE IF NOT EXISTS ExpensesEntries (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    FinId Double,
    LedgerId Double,
    Amount Double,
    ServerGenTransId Double,
    TTId int,
    SourceLedgerId int,
    Narration varchar
  )
  ```
- Definition 2 (created in `downloaddata`):
  ```sql
  CREATE TABLE IF NOT EXISTS ExpensesEntries (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    FinId Double,
    LedgerId Double,
    Amount Double,
    ServerGenTransId Double,
    TTId int,
    Narration varchar
  )
  ```

## `ExpensesoftheWeek`
- Read/written by (1): `varathittam`
- Definition 1 (created in `varathittam`):
  ```sql
  CREATE TABLE IF NOT EXISTS ExpensesoftheWeek (
    LedgerName varchar,
    Amount varchar,
    LedgerAmount double
  )
  ```

## `ExpHeaders`
- Read/written by (4): `defaulttables`, `downloaddata`, `editing_collectionswithfulloptions_act`, `otpverificationact`
- Definition 1 (created in `defaulttables`, `downloaddata`, `editing_collectionswithfulloptions_act`, `otpverificationact`):
  ```sql
  CREATE TABLE IF NOT EXISTS ExpHeaders (
    LedgerId Double,
    LedgerName nvarchar,
    TTId int
  )
  ```

## `FinanceAccessAllowed`
- Read/written by (4): `branchemployeesearchlist`, `employeesaccessearchlist`, `employeesearchlist`, `mobiledeviceaccescontrol_act`
- Definition 1 (created in `branchemployeesearchlist`, `employeesaccessearchlist`, `employeesearchlist`):
  ```sql
  CREATE TABLE IF NOT EXISTS FinanceAccessAllowed (
    IMEI String,
    FinId int
  )
  ```

## `FinanceDetails`
- Read/written by (12): `accountsreports_parameters`, `allfinancedailypatti`, `allfinancedailythittam`, `defaulttables`, `financelinesaddandedit`, `mobiledeviceaccescontrol_act`, `navigationdrawactivity`, `otpverificationact`, `partyledgers_parameters`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`, `stloans`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS FinanceDetails (
    FinId int,
    CollectionBreakString String,
    FinName String,
    FinMailingName String,
    AddLine1 String,
    AddLine2 String,
    City String,
    PINcode String,
    RegistrationNo String,
    CashLedgerId Double,
    PhoneNo String,
    ShortName String,
    MobileNo String,
    Showid Double,
    IsVisible Boolean,
    AdminMobileNos String,
    DefaultLoanMethodId int,
    FinanceCashBookLedgerId Double,
    SMSIdentifier String,
    EmailIdentifier String,
    YearFrom date,
    BooksFrom date,
    BankLedgerid int,
    HLinecolor String,
    LoanNoSuffix String,
    SCopyImage String,
    FCopyImage String,
    LicenseDate date,
    RecentRenewal date,
    ExpiryDate date,
    RenewalAmount int,
    RenewalNoticeDate date,
    IsWeeklyFinance boolean,
    enableinventory boolean
  )
  ```

## `IncomesoftheWeek`
- Read/written by (1): `varathittam`
- Definition 1 (created in `varathittam`):
  ```sql
  CREATE TABLE IF NOT EXISTS IncomesoftheWeek (
    LedgerName varchar,
    Amount varchar,
    LedgerAmount double
  )
  ```

## `ItemMaster`
- Read/written by (4): `defaulttables`, `downloaddata`, `itemmaster_list`, `syncresources`
- Definition 1 (created in `downloaddata`, `itemmaster_list`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS ItemMaster (
    ItemCode Int,
    ItemName String,
    PriUOMid Int,
    Catid Int,
    Grpid Int,
    MinimumOrderIdQty Int,
    ReOrderLevel Int
  )
  ```

## `LastUpload`
- Read/written by (10): `MyIntentService`, `MyService_updatecollection`, `cashbook`, `downloaddata`, `editing_collectionswithfulloptions_act`, `navigationdrawactivity`, `otpverificationact`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`, `uploadcollection`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS LastUpload (
    MCLGCollId Double,
    FWGColId Double,
    TransId Double,
    SubmitAmount Double,
    SubmitedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    CollStaff2LedgerId double,
    CollStaffLedgerId double
  )
  ```

## `loanindex`
- Read/written by (3): `defaulttables`, `downloaddata`, `editing_collectionswithfulloptions_act`
- Definition 1 (created in `downloaddata`, `editing_collectionswithfulloptions_act`):
  ```sql
  CREATE TABLE IF NOT EXISTS loanindex (
    LedgerId Int,
    LoanIndex Int,
    MCLoanId Int,
    MCLoanAmountCollectable Int,
    collected Int,
    Balance Int
  )
  ```

## `LoanMethods`
- Read/written by (6): `defaulttables`, `loanmethods_searchlist`, `otpverificationact`, `refreshorreloadbrancheslist`, `selectbranchandfinance`, `syncresources`
- Definition 1 (created in `loanmethods_searchlist`, `otpverificationact`, `refreshorreloadbrancheslist`, `selectbranchandfinance`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS LoanMethods (
    LoanMethodId Double,
    LoanMethodDesc String,
    LoanAmount Double,
    LoanAmountGiven Double,
    IntDeduction Double,
    IntAddition Double,
    TotAmountCollectable Double,
    CollectionBreak String,
    CollectionBreakString String,
    NoOfIns Double,
    InsAmount Double,
    GracePeriod Double,
    DocChargesMethod Double,
    DocCharges Double,
    ReportIdentification String,
    CBCount int,
    IsVisible Boolean,
    ShowId Double,
    WarningDays int,
    DueSupportAmount int
  )
  ```

## `LoansAllowed`
- Read/written by (4): `defaulttables`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS LoansAllowed (
    LId Int,
    LoanIndex Int
  )
  ```

## `MobileAppCollData`
- Read/written by (27): `MyIntentService`, `MyService_updatecollection`, `cashbook`, `dailypatti_kadanpatrulist_act`, `dailypatti_varakadanillapulist_act`, `defaulttables`, `directcollectionentry`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `maps_partydisplay`, `navigationdrawactivity`, `newtodaycollectionofflinedata_list`, `otpverificationact`, `partylistwithprevloansfornewloanselection`, `partyrearrange`, `partytransfer`, `periodicloareportlist_act`, `prevloanslist_act`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`, `tabbedcollectionlist`, `tabbedcollectionlistadapter`, `tabbedcollectionlistadaptermviewpager`, `tabbedcollectionlistmviewpager`, `tabbedindividualcollectionentry`, `todaycollectionlist`, `uploadcollection`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS MobileAppCollData (
    MCLoanId int,
    FWUDMCLoanId int,
    Remarks varchar,
    PartyLedgerId int,
    MCLoanDate Date,
    ClosingDate\tDate,
    CWLedgerId int,
    \tUDLedgerId int,
    PartyName Varchar,
    \tSubAreaNo int\t,
    LAmount\t int,
    DuesCount int,
    \tInsAmount\t int,
    MCLoanAmountCollectable int\t,
    Collected int\t,
    PaidDues int,
    \tPassedDues \tint,
    LoanMethodDesc Varchar,
    \tCollectionBreakString Varchar,
    \tNoOfIns int,
    \tCBCount int,
    \tistobeWarned boolean,
    \tIsNIP boolean,
    \tLoanStatus Varchar,
    IsMarked boolean,
    PrevCollected Int,
    IsVisited boolean,
    InterestAmount int,
    PenaltyCollected int,
    SavingsCollected int,
    SubAreaName string,
    MCLDueCollId int,
    lng varchar,
    lat varchar,
    islocallyfixed boolean,
    TargetLedgerId int,
    LoanIndex int,
    NoofLoansAllowed int,
    CashCollection int,
    BankCollection int,
    DocCharges int,
    MaximumAmountAllowed int,
    IsAttentionRequired boolean,
    ShowId int
  )
  ```

## `MobileDevlicesAllowed`
- Read/written by (3): `branchemployeesearchlist`, `employeesaccessearchlist`, `employeesearchlist`
- Definition 1 (created in `branchemployeesearchlist`, `employeesaccessearchlist`, `employeesearchlist`):
  ```sql
  CREATE TABLE IF NOT EXISTS MobileDevlicesAllowed (
    MDAId int,
    IMEI String,
    LId Double,
    isActive Boolean,
    Userroleid int
  )
  ```

## `NewItems`
- Read/written by (4): `downloaddata`, `editing_collectionswithfulloptions_act`, `newloancreation`, `uploadcollection`
- Definition 1 (created in `downloaddata`, `editing_collectionswithfulloptions_act`):
  ```sql
  CREATE TABLE IF NOT EXISTS NewItems (
    SNo int,
    ItemCode int,
    Qty int,
    MCLoanid Double,
    PRate int,
    TotalAmt int
  )
  ```

## `NewMCLoans`
- Read/written by (13): `cashbook`, `defaulttables`, `downloaddata`, `editing_collectionswithfulloptions_act`, `navigationdrawactivity`, `newloancreation`, `newloanslist_act`, `otpverificationact`, `partydetails_act`, `partylistwithprevloansfornewloanselection`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`, `uploadcollection`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS NewMCLoans (
    MCLoanId int,
    MCLoanDate date,
    FinId int,
    PartyLedgerId int,
    MCLoanAmount int,
    TotalMonths int,
    DocCharges int,
    LoanMethodId int,
    InterestAmount int,
    MCLoanAmountCollectable Double,
    InsAmount Double,
    MCLoanAmountEstimated int,
    SubAreaNo int,
    ServerGenMCLoanId int,
    ServerGenLedgerId int,
    Remarks nvarchar,
    ServerGenTransId int,
    WeekdayId int,
    MonthdayId int,
    ondaycollection int,
    mobileno string,
    Sourceledgerid int,
    PartyReferenced string,
    LoanIndex int,
    ProductValue int,
    AdvanceAmount int,
    IntRate double
  )
  ```

## `NewParties`
- Read/written by (12): `cashbook`, `defaulttables`, `downloaddata`, `editing_collectionswithfulloptions_act`, `navigationdrawactivity`, `newpartieslist_act`, `otpverificationact`, `partydetails_act`, `partylistwithprevloansfornewloanselection`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`, `uploadcollection`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS NewParties (
    LedgerId int,
    CWLedgerId int,
    LedgerName Varchar,
    Tag VarChar,
    AreaNo int,
    SubAreaNo int,
    UDLedgerId int,
    MobileNo1 varchar,
    MobileNo2 Varchar,
    ServerGenLedgerId Double,
    AddLine1 varchar,
    SpouseName varchar,
    FamilyName varchar,
    ImagefileName varchar,
    IdProoffileName varchar,
    lat varchar,
    lng varchar,
    IdProofFileNameII varchar,
    NoOfLoansAllowed int,
    MaximumAmountAllowed int,
    IsActive boolean
  )
  ```

## `OFLoanStatusforAPP`
- Read/written by (2): `fintrans_list`, `otpverificationact`
- Definition 1 (created in `otpverificationact`):
  ```sql
  CREATE TABLE IF NOT EXISTS OFLoanStatusforAPP (
    OFLoanId Double,
    Repaid Double,
    Balance Double,
    Status String
  )
  ```

## `OFLtempPartyLevelPending`
- Read/written by (1): `meetingreport`
- Definition 1 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS OFLtempPartyLevelPending (
    PartyLedgerId Double,
    Ledgername String,
    Balance Double,
    Interest Double
  )
  ```

## `Parties`
- Read/written by (18): `dailypatti_kadanpatrulist_act`, `dailypatti_varakadanillapulist_act`, `daywisependingreport_act`, `defaulttables`, `downloaddata`, `editing_collectionswithfulloptions_act`, `loanslist_act`, `maps_partydisplay`, `newpartiesnewloanslist_act`, `otpverificationact`, `partydetails_act`, `partylistwithprevloansfornewloanselection`, `partyrearrange`, `partytransfer`, `periodicloareportlist_act`, `prevloanslist_act`, `tabbedindividualcollectionentry`, `todaycollectionlist`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS Parties (
    LedgerId int,
    CWLedgerId int,
    LedgerName Varchar,
    Tag VarChar,
    SubAreaNo int,
    UDLedgerId int,
    MobileNo1 varchar,
    MobileNo2 Varchar,
    AddLine1 varchar,
    SpouseName varchar,
    FamilyName varchar,
    ImagefileName varchar,
    IdProofFileName varchar,
    lat varchar,
    lng varchar,
    IdProofFileNameII varchar,
    NoOfLoansAllowed int,
    MaximumAmountAllowed int,
    IsActive boolean
  )
  ```

## `PrevCollections`
- Read/written by (7): `downloaddata`, `editing_collectionsadapter`, `editing_collectionswithfulloptions_act`, `otpverificationact`, `tabbedcollectionlistadapter`, `tabbedcollectionlistadaptermviewpager`, `tabbedindividualcollectionentry`
- Definition 1 (created in `downloaddata`, `editing_collectionswithfulloptions_act`, `otpverificationact`):
  ```sql
  CREATE TABLE IF NOT EXISTS PrevCollections (
    RowNo int,
    MCLoanId Double,
    CollDate varchar,
    Paid Double
  )
  ```

## `PrevLoansListTable`
- Read/written by (1): `prevloanslist_act`
- Definition 1 (created in `prevloanslist_act`):
  ```sql
  CREATE TABLE IF NOT EXISTS PrevLoansListTable (
    MCLoanId int,
    LoanNo int,
    LoanDate Date,
    LoanAmount int,
    Paid int,
    Status String,
    Days int
  )
  ```

## `PrevLocations`
- Read/written by (2): `editing_collectionswithfulloptions_act`, `tabbedindividualcollectionentry`
- Definition 1 (created in `editing_collectionswithfulloptions_act`, `tabbedindividualcollectionentry`):
  ```sql
  CREATE TABLE IF NOT EXISTS PrevLocations (
    MCLoanId int,
    MCLDueColDate date,
    lat nvarchar,
    lng nvarchar
  )
  ```

## `qatledgers`
- Read/written by (3): `accounttransactions_act`, `defaulttables`, `refreshorreloadbrancheslist`
- Definition 1 (created in `accounttransactions_act`, `defaulttables`, `refreshorreloadbrancheslist`):
  ```sql
  CREATE TABLE IF NOT EXISTS qatledgers (
    FinId int,
    TTId int,
    AType int,
    LedgerId Double,
    LedgerName String,
    isExecutiveLevelExpensesLedger bit,
    isExecutiveLevelReceiptsLedger bit
  )
  ```

## `RecentPartieswithContractVehicles`
- Read/written by (1): `invtransactnew`
- Definition 1 (created in `invtransactnew`):
  ```sql
  CREATE TABLE IF NOT EXISTS RecentPartieswithContractVehicles (
    Transid Double,
    VId Double,
    PartyLedgerId Double
  )
  ```

## `RelatedDocCollection`
- Read/written by (5): `downloaddata`, `editing_collectionswithfulloptions_act`, `newloancreation`, `syncresources`, `uploadcollection`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS RelatedDocCollection (
    DCUId int,
    ServerGenMCLoanId int,
    MCLoanId int,
    DUID int,
    Remarks String,
    ImagePath String
  )
  ```

## `RelatedDocmenttypes`
- Read/written by (2): `newloancreation`, `syncresources`
- Definition 1 (created in `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS RelatedDocmenttypes (
    DUID Int,
    DocDesc String,
    isvisible Boolean,
    showid Int,
    isImageAllowed Boolean
  )
  ```

## `SelectedBranchFinanceDetails`
- Read/written by (7): `defaulttables`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `newloancreation`, `otpverificationact`, `refreshorreloadfinancelist`, `selectbranchandfinance`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS SelectedBranchFinanceDetails (
    FinId int,
    CollectionBreakString String,
    FinName String,
    FinMailingName String,
    AddLine1 String,
    AddLine2 String,
    City String,
    PINcode String,
    RegistrationNo String,
    CashLedgerId Double,
    PhoneNo String,
    ShortName String,
    MobileNo String,
    Showid Double,
    IsVisible Boolean,
    AdminMobileNos String,
    DefaultLoanMethodId int,
    FinanceCashBookLedgerId Double,
    SMSIdentifier String,
    EmailIdentifier String,
    YearFrom date,
    BooksFrom date,
    BankLedgerid int,
    HLinecolor String,
    LoanNoSuffix String,
    SCopyImage String,
    FCopyImage String,
    LicenseDate date,
    RecentRenewal date,
    ExpiryDate date,
    RenewalAmount int,
    RenewalNoticeDate date,
    IsWeeklyFinance boolean,
    enableinventory boolean
  )
  ```

## `selectedcollectiondetails`
- Read/written by (1): `editing_collectionswithfulloptions_act`
- Definition 1 (created in `editing_collectionswithfulloptions_act`):
  ```sql
  CREATE TABLE IF NOT EXISTS selectedcollectiondetails (
    MCLDueCollId Double,
    MCLoanId Double,
    GColId Double,
    Paid Double,
    PenaltyCollected Double,
    SavingsCollected Double,
    CashCollection Double,
    BankCollection Double
  )
  ```

## `SelectedFinanceSubAreaDetails`
- Read/written by (4): `collectionarealistforloanpartyandloans`, `collectionarealistforsevendayscollection`, `selectbranchandfinance`, `sevendayscollectionforselline`
- Definition 1 (created in `selectbranchandfinance`):
  ```sql
  CREATE TABLE IF NOT EXISTS SelectedFinanceSubAreaDetails (
    FinId int,
    SubAreaId int,
    SubAreaName Varchar
  )
  ```

## `SelectedFinanceSubAreaDetailsALL`
- Read/written by (1): `selectbranchandfinance`
- Definition 1 (created in `selectbranchandfinance`):
  ```sql
  CREATE TABLE IF NOT EXISTS SelectedFinanceSubAreaDetailsALL (
    FinId int,
    SubAreaId int,
    SubAreaName Varchar
  )
  ```

## `SelectedLoanCollectionDetails`
- Read/written by (0): —
- Definition 1 (created in `selectedloancollectiondetails`):
  ```sql
  CREATE TABLE IF NOT EXISTS SelectedLoanCollectionDetails (
    SNo int,
    ColDate varchar,
    Amount int,
    Balance int
  )
  ```

## `SMSGateWay`
- Read/written by (0): —
- Definition 1 (created in `editing_collections_act`, `editing_collectionswithfulloptions_act`, `newloancreation`):
  ```sql
  CREATE TABLE IF NOT EXISTS SMSGateWay (
    SMSGId int,
    SMSAPI varchar,
    APIKey varchar,
    SMSSenderId varchar,
    SMSRoute int,
    SMSType varchar
  )
  ```

## `SMSTemplates`
- Read/written by (8): `defaulttables`, `editing_collections_act`, `editing_collectionswithfulloptions_act`, `meetingreport`, `newloancreation`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `editing_collections_act`, `editing_collectionswithfulloptions_act`, `newloancreation`):
  ```sql
  CREATE TABLE IF NOT EXISTS SMSTemplates (
    STId int,
    SMSTitle varchar,
    SMSDesc varchar
  )
  ```
- Definition 2 (created in `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS SMSTemplates (
    STId int,
    SMSTitle String,
    SMSDesc String
  )
  ```

## `SpecialAccessAllowed`
- Read/written by (5): `downloaddata`, `navigationdrawactivity`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `navigationdrawactivity`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS SpecialAccessAllowed (
    SAAId Int,
    UserLevelId Int,
    SARId Int,
    bitvalue Boolean
  )
  ```

## `SpecialAccessRules`
- Read/written by (2): `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS SpecialAccessRules (
    SARId Int,
    SAName String,
    ValueType Int,
    isAllowed Boolean
  )
  ```

## `StaffLocationQueue`
- Read/written by (1): `staffgpstracking_service`
- Definition 1 (created in `downloaddata`):
  ```sql
  CREATE TABLE IF NOT EXISTS StaffLocationQueue (
    QId INTEGER PRIMARY KEY AUTOINCREMENT,
    FinId int,
    StaffLedgerId int,
    TrackDate varchar,
    Lat varchar,
    Lng varchar,
    Accuracy int,
    Speed int,
    BatteryLevel int,
    IsMockLocation int,
    DeviceTime varchar
  )
  ```

## `STLoanStatusforAPP`
- Read/written by (2): `fintrans_list`, `otpverificationact`
- Definition 1 (created in `otpverificationact`):
  ```sql
  CREATE TABLE IF NOT EXISTS STLoanStatusforAPP (
    STLoanId Double,
    Repaid Double,
    Balance Double,
    Status String
  )
  ```

## `Stockcat`
- Read/written by (4): `defaulttables`, `downloaddata`, `stockcat_list`, `syncresources`
- Definition 1 (created in `downloaddata`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS Stockcat (
    CatId Double,
    CatDesc String,
    DisplayIndex Int,
    IsActive Boolean
  )
  ```
- Definition 2 (created in `stockcat_list`):
  ```sql
  CREATE TABLE IF NOT EXISTS Stockcat (
    CatId Double,
    CatDesc String,
    DisplayIndex Double,
    IsActive Boolean
  )
  ```

## `StockGrp`
- Read/written by (4): `defaulttables`, `downloaddata`, `stockgrp_list`, `syncresources`
- Definition 1 (created in `downloaddata`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS StockGrp (
    GrpId int,
    GrpDesc String,
    HSNSACCode String,
    IsActive Boolean,
    DisplayIndex int
  )
  ```
- Definition 2 (created in `stockgrp_list`):
  ```sql
  CREATE TABLE IF NOT EXISTS StockGrp (
    GrpId int,
    GrpDesc String,
    HSNSACCode int,
    IsActive Boolean,
    DisplayIndex int
  )
  ```

## `StockUOM`
- Read/written by (4): `defaulttables`, `downloaddata`, `stockUOM_list`, `syncresources`
- Definition 1 (created in `downloaddata`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS StockUOM (
    UOMId Int,
    UOMDesc String,
    DisplayIndex Int,
    IsActive Boolean
  )
  ```
- Definition 2 (created in `stockUOM_list`):
  ```sql
  CREATE TABLE IF NOT EXISTS StockUOM (
    UOMId Double,
    UOMDesc String,
    DisplayIndex Double,
    IsActive Boolean
  )
  ```

## `SubAreaDetails`
- Read/written by (10): `collectionarearearrangeact`, `collectionareassearchlist`, `defaulttables`, `downloaddata`, `editing_collectionswithfulloptions_act`, `maps_collectionareadisplay`, `navigationdrawactivity`, `otpverificationact`, `refreshorreloadbrancheslist`, `refreshorreloadfinancelist`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS SubAreaDetails (
    SubAreaNo Double,
    SubAreaName varchar,
    FinId int,
    WeekdayId int,
    IsVisible Boolean,
    ShowId Double,
    Latitude REAL,
    Longitude REAL
  )
  ```

## `Suppliers`
- Read/written by (3): `customerdetails_act`, `customers_list`, `defaulttables`
- Definition 1 (created in `customers_list`):
  ```sql
  CREATE TABLE IF NOT EXISTS Suppliers (
    ledgerid Double,
    ledgername String,
    AddLine1 String,
    AddLine2 String,
    City String,
    PinCode String,
    MobileNo1 String,
    MobileNo2 String,
    EMail String,
    GroupId Double,
    IsDefault Double,
    IsReserved Double,
    ScreenType Double
  )
  ```

## `tblFinwisePartners`
- Read/written by (4): `otpverificationact`, `parties_list`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS tblFinwisePartners (
    UID Double,
    PartnerLedgerId Double,
    FinId Double,
    CapitalReceiptTransId Double,
    ListSNo Double,
    isActive Boolean
  )
  ```

## `tblGroups`
- Read/written by (4): `defaulttables`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS tblGroups (
    GroupId Double,
    GroupName String,
    GroupUnder String,
    ShowId Decimal,
    isShowDetails String,
    isVisible String,
    ANId int
  )
  ```

## `tblLedgers`
- Read/written by (11): `defaulttables`, `meetingreport`, `ofloans`, `ofloans_interestcalculationandpayments`, `otpverificationact`, `parties_list`, `partyandpartnerdetails`, `refreshorreloadbrancheslist`, `stloaninterestcalculationandpayments`, `stloans`, `syncresources`
- Definition 1 (created in `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS tblLedgers (
    LedgerId Double,
    LedgerName String,
    GroupId Double,
    OpeningBalance Double,
    OpBalType String,
    FinId Double,
    Title String,
    Tag String,
    GType String,
    GuardianName String,
    AddLine1 String,
    AddLine2 String,
    City String,
    PinCode String,
    MobileNo1 String,
    MobileNo2 String,
    EMail String,
    MasterPartyLedgerId Double,
    ScreenType Double,
    PartyGroupId Double,
    IsDefault Double,
    IsReserved Double,
    ListSNo Double,
    PanNo String,
    isActive Boolean,
    DesignationId int,
    StatusId int,
    AndroidDeviceId String,
    AadhaarNo String,
    Imagefilename String,
    lat String,
    lng String,
    Age int,
    MartialStatus Boolean,
    Qualification String,
    Profession String,
    MonthlyIncome int,
    GuardianMonthlyIncome int,
    GuardianMobileNo1 String,
    GuardianMobileNo2 String,
    GuardianProfession String,
    LandMark String,
    SpouseName String,
    FamilyName String,
    desigstatusid int
  )
  ```

## `tblOFLoans`
- Read/written by (5): `fintrans_list`, `ofloans`, `otpverificationact`, `parties_list`, `refreshorreloadbrancheslist`
- Definition 1 (created in `otpverificationact`, `refreshorreloadbrancheslist`):
  ```sql
  CREATE TABLE IF NOT EXISTS tblOFLoans (
    TransId Double,
    TransDate date,
    OFLoanId Double,
    FWOFLoanId Double,
    FWUDOFLoanId Double,
    PartyLedgerId Double,
    OFLoanAmount Double,
    OFLIntRateinDM Double,
    OFLIntRateinMM Double,
    OFLIntRateinYM Double,
    OFLPriIntRateinDM Double,
    OFLSecRateinDM Double,
    OFLTriRateinDM Double,
    RecPartnerId Double,
    FinId Double,
    IntCalcStartOn date,
    IntCalcStartOnSYSSET date,
    CurStat Boolean,
    Remarks String,
    BankRefId int,
    BankRefNo String,
    DocCharges Double,
    SourceLedgerId Double
  )
  ```

## `tblSalesDetailsforAndroid`
- Read/written by (4): `navigationdrawactivity`, `otpverificationact`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `defaulttables`):
  ```sql
  CREATE TABLE IF NOT EXISTS tblSalesDetailsforAndroid (
    mnuname string,
    mnudisplayname string,
    UserLevelId Int,
    mnuId Int,
    showid int,
    ShowState Boolean,
    ViewState Boolean,
    UpdateState Boolean,
    AddState Boolean,
    DeleteState Boolean,
    mnustyle int
  )
  ```

## `tblSTLoans`
- Read/written by (6): `fintrans_list`, `otpverificationact`, `parties_list`, `partyledgers_list`, `refreshorreloadbrancheslist`, `stloans`
- Definition 1 (created in `otpverificationact`, `refreshorreloadbrancheslist`):
  ```sql
  CREATE TABLE IF NOT EXISTS tblSTLoans (
    TransId Double,
    TransDate date,
    STLoanId Double,
    FWSTLoanId Double,
    FWUDSTLoanId Double,
    PartyLedgerId Double,
    STLoanAmount Double,
    STLIntRateinDM Double,
    STLIntRateinMM Double,
    STLPriIntRateinDM Double,
    STLSecIntRateinDM Double,
    STLTriIntRateinDM Double,
    RecPartnerId Double,
    RecPartyId Double,
    FinId Double,
    DocCharges Double,
    Remarks String,
    IntCalcStartOn date,
    IntCalcStartOnSYSSET date,
    CurStat Boolean,
    BankRefId int,
    BankRefNo String,
    SourceLedgerId Double,
    DCTargetLedgerId int
  )
  ```

## `tempCollWeeks`
- Read/written by (1): `varathittam`
- Definition 1 (created in `varathittam`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempCollWeeks (
    WeekNo Integer,
    WDate varchar,
    CollAmount double,
    PendingAmount double,
    ColWeekFromDate date,
    ColWeekToDate date,
    RSDate date,
    REDate date
  )
  ```

## `tempdepIntColPenidnglistnew`
- Read/written by (0): —
- Definition 1 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempdepIntColPenidnglistnew (
    typeid int,
    FinId Double,
    DepositId Double,
    RecPartnerId Double,
    PartyLedgerId Double,
    IntRate Double,
    LoanDate date,
    DepositAmountPaid Double,
    LedgerName String,
    MobileNo1 String,
    ShortName String,
    LoanNo Double,
    DepositAmount Double,
    Balance Double,
    IntFrom date,
    Intupto date,
    Days int,
    CInt Double,
    PaidInt Double,
    Paidon date,
    Receno Double
  )
  ```

## `tempLoanClosure`
- Read/written by (0): —
- Definition 1 (created in `closableloanssearchlistact`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempLoanClosure (
    MCLoanId int,
    PartyLedgerId int,
    LedgerName String,
    SubAreaName String,
    MCLoanAmountCollectable int,
    MCLoanDate string,
    ReportIdentification String
  )
  ```

## `tempoflIntColPenidnglistnew`
- Read/written by (1): `meetingreport`
- Definition 1 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempoflIntColPenidnglistnew (
    typeid int,
    FinId Double,
    ofloanId Double,
    RecPartnerId Double,
    PartyLedgerId Double,
    IntRate Double,
    LoanDate date,
    ofloanAmountPaid Double,
    LedgerName String,
    MobileNo1 String,
    ShortName String,
    LoanNo Double,
    ofloanAmount Double,
    Balance Double,
    IntFrom date,
    Intupto date,
    Days int,
    CInt Double,
    PaidInt Double,
    Paidon date,
    Receno Double
  )
  ```

## `tempofllIntColPenidnglistnew`
- Read/written by (0): —
- Definition 1 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempofllIntColPenidnglistnew (
    typeid int,
    FinId Double,
    ofloanId Double,
    RecPartnerId Double,
    PartyLedgerId Double,
    IntRate Double,
    LoanDate date,
    ofloanAmountPaid Double,
    LedgerName String,
    MobileNo1 String,
    ShortName String,
    LoanNo Double,
    ofloanAmount Double,
    Balance Double,
    IntFrom date,
    Intupto date,
    Days int,
    CInt Double,
    PaidInt Double,
    Paidon date,
    Receno Double
  )
  ```

## `tempPartyLevelPending`
- Read/written by (1): `meetingreport`
- Definition 1 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempPartyLevelPending (
    PartyLedgerId Double,
    Ledgername String,
    Balance Double,
    Interest Double
  )
  ```
- Definition 2 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempPartyLevelPending (
    PartyLedgerId Double,
    Ledgername String,
    Balance Double,
    Interest Double,
    MobileNo1 String,
    intupto Date,
    intFrom Date
  )
  ```

## `tempstlIntColPenidnglistnew`
- Read/written by (1): `meetingreport`
- Definition 1 (created in `meetingreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS tempstlIntColPenidnglistnew (
    typeid int,
    FinId Double,
    STLoanId Double,
    RecPartnerId Double,
    PartyLedgerId Double,
    IntRate Double,
    LoanDate date,
    STLoanAmountPaid Double,
    LedgerName String,
    MobileNo1 String,
    ShortName String,
    LoanNo Double,
    STLoanAmount Double,
    Balance Double,
    IntFrom date,
    Intupto date,
    Days int,
    CInt Double,
    PaidInt Double,
    Paidon date,
    Receno Double
  )
  ```

## `UnpaidParties`
- Read/written by (2): `selectdateforreport`, `unpaidpartiesonselectedday`
- Definition 1 (created in `selectdateforreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS UnpaidParties (
    MCLoanId Double,
    FWUDMCLoanId Double,
    Remarks String,
    PartyLedgerId Double,
    MCLoanDate String,
    ClosingDate String,
    CWLedgerId Double,
    UDLedgerId int,
    PartyName String,
    SubAreaNo Double,
    LAmount Double,
    DuesCount Double,
    InsAmount int,
    MCLoanAmountCollectable Double,
    PrevCollected Double,
    PaidDues int,
    PassedDues int,
    LoanMethodDesc String,
    CollectionBreakString String,
    NoOfIns Double,
    CBCount int,
    istobeWarned int,
    IsNIP int,
    LoanStatus Double
  )
  ```
- Definition 2 (created in `selectdateforreport`):
  ```sql
  CREATE TABLE IF NOT EXISTS UnpaidParties (
    MCLoanId Double,
    FWUDMCLoanId Double,
    Remarks String,
    PartyLedgerId Double,
    MCLoanDate String,
    ClosingDate String,
    CWLedgerId Double,
    UDLedgerId int,
    PartyName String,
    SubAreaNo Double,
    SubAreaName String,
    ShowId int,
    LAmount Double,
    DuesCount Double,
    InsAmount int,
    MCLoanAmountCollectable Double,
    PrevCollected Double,
    PaidDues int,
    PassedDues int,
    LoanMethodDesc String,
    CollectionBreakString String,
    NoOfIns Double,
    CBCount int,
    istobeWarned int,
    IsNIP int,
    LoanStatus Double
  )
  ```

## `userroles`
- Read/written by (3): `defaulttables`, `refreshorreloadbrancheslist`, `syncresources`
- Definition 1 (created in `refreshorreloadbrancheslist`, `syncresources`):
  ```sql
  CREATE TABLE IF NOT EXISTS userroles (
    URId Int,
    URName String,
    isFullAccessAllowed Boolean,
    isvisible Boolean,
    DisplayIndex Int
  )
  ```

## Local tables referenced without a CREATE statement in app code
- `collection` — used by `uploadcollection`
- `financemaster` — used by `ofloans`, `ofloans_interestcalculationandpayments`, `stloaninterestcalculationandpayments`
- `partyvisitstatus` — used by `uploadcollection`
- `stockitems` — used by `defaulttables`
- `tblmcloanduecol` — used by `uploadcollection`
- `tbltransactionsdetails` — used by `uploadcollection`
