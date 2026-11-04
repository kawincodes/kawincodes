# SQL text not linked to a single call site

[← work.md](../../work.md)

**42 SQL literals** that are not passed straight to a database call in the same method. Most are SQL prefixes kept as data and completed elsewhere: for example, the analysis-report procedure calls held in `defaulttables.fetchanalysisreportsettings*`, which the analysis fragments execute later with date parameters appended. The rest are statements whose text is assembled across branches or methods.

| Class | Method | Line | SQL text |
|---|---|---|---|
| `cashbook` | `doInBackground` | 807 | `delete from CASHBookVerification where FinId= ` |
| `defaulttables` | `fetchanalysisreportsettings` | 653 | `exec Rep_FinWiseSTLoans @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 656 | `exec Rep_ShowPartnerWiseSTLoanBalance @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 657 | `exec Rep_ShowPartnerFinanceWiseSTLoanBalance @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 659 | `exec RPT_PartywiseSTLoans @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 661 | `exec Rep_ShowSTLoanIntDistribution @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 663 | `exec [Rep_ShowSTLoanIntDistributionOverAll] @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 669 | `exec Rep_FinWiseOFLoans @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 670 | `exec Rep_ShowPartnerWiseOFLoanBalance @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 672 | `exec Rep_ShowPartnerFinanceWiseOFLoanBalance @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 674 | `exec RPT_PartywiseOFLoans @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 676 | `exec Rep_ShowOFLoanIntDistribution @RDate=` |
| `defaulttables` | `fetchanalysisreportsettings` | 678 | `exec [Rep_ShowOFLoanIntDistributionOverAll] @RDate=` |
| `defaulttables` | `fetchanalysisreportsettingsforofloans` | 689 | `exec Rep_FinWiseOFLoans @RDate=` |
| `defaulttables` | `fetchanalysisreportsettingsforofloans` | 690 | `exec Rep_ShowPartnerWiseOFLoanBalance @RDate=` |
| `defaulttables` | `fetchanalysisreportsettingsforofloans` | 692 | `exec Rep_ShowPartnerFinanceWiseOFLoanBalance @RDate=` |
| `defaulttables` | `fetchanalysisreportsettingsforofloans` | 694 | `exec RPT_PartywiseOFLoans @RDate=` |
| `defaulttables` | `fetchanalysisreportsettingsforofloans` | 696 | `exec Rep_ShowOFLoanIntDistribution @RDate=` |
| `defaulttables` | `fetchanalysisreportsettingsforofloans` | 698 | `exec [Rep_ShowOFLoanIntDistributionOverAll] @RDate=` |
| `directcollectionentry` | `doInBackground` | 864 | `insert into tblMCLoanDueCol (GColId,MCLDueColDate,MCLoanId,TTId,FinId,Paid,PenaltyCollected,SavingsCollected,BankCollection,CashCollection) values (` |
| `directcollectionentry` | `doInBackground` | 867 | ` Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `directcollectionentry` | `doInBackground` | 868 | `Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `directcollectionentry` | `doInBackground` | 871 | ` Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `editing_collections_act` | `doInBackground` | 1109 | `delete from tblTransactionsDetails where TransId=` |
| `editing_collections_act` | `doInBackground` | 1112 | ` Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `editing_collections_act` | `doInBackground` | 1112 | `Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `editing_collections_act` | `doInBackground` | 1117 | ` Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `editing_collectionswithfulloptions_act` | `doInBackground` | 1799 | `delete from tblTransactionsDetails where TransId=` |
| `editing_collectionswithfulloptions_act` | `doInBackground` | 1810 | ` Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `editing_collectionswithfulloptions_act` | `doInBackground` | 1810 | `Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `editing_collectionswithfulloptions_act` | `doInBackground` | 1818 | ` Insert into tblTransactionsDetails(TransId,LedgerId,LedgerAmount,LedgerStatus, AmtType, SNo, AgnstLedgerSNo) values(` |
| `newloancreation` | `doInBackground` | 1544 | `; insert into tblTransactionsDetails` |
| `newloancreation` | `doInBackground` | 1603 | `; insert into tblTransactionsDetails` |
| `uploadcollection` | `doInBackground` | 926 | `delete from tblTransactionsDetails where TransId=` |
| `uploadcollection` | `doInBackground` | 1224 | `; insert into tblTransactionsDetails` |
| `uploadcollection` | `doInBackground` | 1725 | `delete from tbltransactionsdetails where transid=` |
| `uploadcollection` | `doInBackground` | 1725 | `;insert into tblTransactionsDetails  Select ` |
| `uploadcollection` | `doInBackground` | 1727 | `delete from tbltransactionsdetails where transid=` |
| `uploadcollection` | `doInBackground` | 1727 | `;insert into tblTransactionsDetails  Select ` |
| `uploaddownloadcontroling` | `onOptionsItemSelected` | 121 | ` delete from ` |
| `uploaddownloadcontroling` | `onOptionsItemSelected` | 124 | `insert into ` |
| `varathittam` | `doInBackground` | 848 | `Insert into tempCollWeeks values (` |
