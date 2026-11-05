# FinSoft II — Technical Guide with demo data

The guide itself is a Claude Doc: **[FinSoft II — Complete Technical Guide: Demo-Data Screens, Algorithms & SQL](https://claude.ai/artifact/Lws78Bzp1meyR1A2LsS1Te)**. It runs to over 55,000 words in 25 sections, with 33 two-screen figures and 2 diagrams.

This folder keeps the guide's figures and its demo dataset next to the decoded spec ([brain.md](../brain.md)) and the extraction ([work.md](../work.md)).

| Part | Sections |
|---|---|
| Overview | 1 About · 2 Architecture · 3 Demo dataset · 4 Navigation, menus and roles · 5 A working day with demo data |
| Screens | 6 Start-up, home, settings · 7 Collection · 8 Parties and new loans · 9 Loan lists, closures, transfers · 10 Collection reports · 11 Patti and Thittam · 12 Accounts · 13 ST and OF loans · 14 Admin, inventory, printing |
| Algorithms | 15 Loan methods and the due schedule · 16 Download, upload and sync · 17 Patti, Thittam and the 7-day matrix · 18 Interest, cash book, closures and access |
| SQL | 19 Local SQLite DDL · 20 SQL Server tables and procedures · 21 Demo INSERT scripts · 22 Key queries with demo results |
| Reference | 23 Data dictionary · 24 Reference tables · 25 Glossary and screen index |

## Demo data

[demo_dataset.md](demo_dataset.md) is the single fictional dataset every section uses: DEMO FINANCE, branch Madurai (demo), Lines A–C, 13 parties, 13 loans and the working day Wednesday 04-11-2026. Names are invented and phone numbers are masked (98XXXXX###).

## Figures

Each figure shows two of the app's own layouts, rendered with Paparazzi (Android layoutlib) and filled with the demo values that its section quotes. Vendor phone numbers printed in the app are masked.

| Figure | Left | Right | File |
|---|---|---|---|
| 6.1 | Registration key (MMMMMM) (`collectdbinfofromweb`) | OTP verification (`otpverificationact`) | [fig_6.1.png](figures/fig_6.1.png) |
| 6.2 | Home dashboard, Line A (`navigationdrawactivity`) | Navigation drawer (`navigation_drawer_open`) | [fig_6.2.png](figures/fig_6.2.png) |
| 6.3 | Select Line (`refreshorreloadfinancelist`) | Sync Resources (`syncresources`) | [fig_6.3.png](figures/fig_6.3.png) |
| 6.4 | Settings (`settings_act`) | Device and APP Info (`deviceidentity`) | [fig_6.4.png](figures/fig_6.4.png) |
| 7.1 | Download Data (`downloaddata`) | Area-wise list, Gandhi Nagar (`tabbedcollectionlistmviewpager`) | [fig_7.1.png](figures/fig_7.1.png) |
| 7.2 | Collection entry, English (`tabbedindividualcollectionentry`) | Collection entry, Tamil (`tabbedindividualcollectionentry`, Tamil) | [fig_7.2.png](figures/fig_7.2.png) |
| 7.3 | Admin edit of 04-11-2026 (`editing_collectionswithfulloptions_act`) | Collection for a Day (offline) (`newtodaycollectionofflinedata_list`) | [fig_7.3.png](figures/fig_7.3.png) |
| 7.4 | Upload Data (`uploadcollection`) | Loan 5002 history (`selectedloancollectiondetails`) | [fig_7.4.png](figures/fig_7.4.png) |
| 8.1 | Party list, LINE A - DAILY (`partydetails_searchlist`) | Create party: Jayanthi R (`partydetails_act`) | [fig_8.1.png](figures/fig_8.1.png) |
| 8.2 | New loan 5009 (`newloancreation`) | Due schedule, loan 6001 (`dueschdule`) | [fig_8.2.png](figures/fig_8.2.png) |
| 8.3 | Today New Loans (`newloanslist_act`) | Today New Parties (`newpartieslist_act`) | [fig_8.3.png](figures/fig_8.3.png) |
| 9.1 | Loans list, Line A (`loanslist_act`) | Loan closure, loan 5003 (`loanclosoureentry`) | [fig_9.1.png](figures/fig_9.1.png) |
| 9.2 | Live saving accounts (`closablesavings_list`) | Savings closure, Murugan K (`closingsavingsaccount`) | [fig_9.2.png](figures/fig_9.2.png) |
| 9.3 | Party transfer, Arumugam T (`partytransfer`) | Rearrange, Gandhi Nagar (`partyrearrange`) | [fig_9.3.png](figures/fig_9.3.png) |
| 10.1 | Collection for a Day (`todaycollectionlist`) | Collection Abstract (`todaycollectionabstractlist`) | [fig_10.1.png](figures/fig_10.1.png) |
| 10.2 | 7-days area list (`collectionarealistforsevendayscollection`) | 7-days page, Gandhi Nagar (`sevendayscollectionforselline`) | [fig_10.2.png](figures/fig_10.2.png) |
| 10.3 | Unpaid parties (`unpaidpartiesonselectedday`) | Day-wise pending (`daywisependingreport_act`) | [fig_10.3.png](figures/fig_10.3.png) |
| 11.1 | Daily Patti (`dailypattinew_act`) | Live Loans Balance (`dailypatti_nadapunippuphakkilist_act`) | [fig_11.1.png](figures/fig_11.1.png) |
| 11.2 | Simple Thittam (`simplethittamlist_act`) | Daily Thittam (`dailythittamforallowedlines`) | [fig_11.2.png](figures/fig_11.2.png) |
| 11.3 | Report picker, code 304 (`selectbranchandfinance`) | Weekly collection analysis (`weeklycollectionanalysis_act`) | [fig_11.3.png](figures/fig_11.3.png) |
| 12.1 | Payment voucher, Petrol (`accounttransactions_act`) | Payments list (`accounttrans_list`) | [fig_12.1.png](figures/fig_12.1.png) |
| 12.2 | Cash book 211 (`cashbook`) | Cash abstract (`cashbookabstract`) | [fig_12.2.png](figures/fig_12.2.png) |
| 12.3 | Amount Received (`cashreceiveandreturns`) | Today Expenses (`expensesentry_searchlist`) | [fig_12.3.png](figures/fig_12.3.png) |
| 12.4 | Account ledgers (`accledgers_searchlist`) | Ledger statement, 201 (`ledgerstatement_act`) | [fig_12.4.png](figures/fig_12.4.png) |
| 12.5 | Report parameters (`accountsreports_parameters`) | Trial balance (`tbbspl_list`) | [fig_12.5.png](figures/fig_12.5.png) |
| 13.1 | ST loan STL-1 (`stloans`) | ST interest to 04-11-2026 (`stloaninterestcalculationandpayments`) | [fig_13.1.png](figures/fig_13.1.png) |
| 13.2 | OF loan OFL-1 (`ofloans`) | OF loans list (`fintrans_list`) | [fig_13.2.png](figures/fig_13.2.png) |
| 13.3 | ST loan account (`partyledgers_list`) | Analysis, finance-wise (`loananddepositanalyisreport`) | [fig_13.3.png](figures/fig_13.3.png) |
| 14.1 | Employees (`employeesearchlist`) | Employee: Ravi Kumar (`employeedetails_act`) | [fig_14.1.png](figures/fig_14.1.png) |
| 14.2 | Access Setup (`mobiledeviceaccescontrol_act`) | User privileges (`userroleprevilages_act`) | [fig_14.2.png](figures/fig_14.2.png) |
| 14.3 | Loan methods (`loanmethods_searchlist`) | Loan method D100 (`loanmethods`) | [fig_14.3.png](figures/fig_14.3.png) |
| 14.4 | Collection areas, Line A (`collectionareassearchlist`) | Collection List Print (`collectionlistprints`) | [fig_14.4.png](figures/fig_14.4.png) |
| 14.5 | Items (`itemmaster_list`) | New purchase note 101 (`invtransactnew`) | [fig_14.5.png](figures/fig_14.5.png) |
