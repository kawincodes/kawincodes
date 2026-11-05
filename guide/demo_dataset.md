# FinSoft II guide — demo dataset (fictional; use exactly these values everywhere)

Working day: **Wednesday 04-11-2026** (dd-MM-yyyy). Financial year 2026-2027 (FYId 16: 01-04-2026 .. 31-03-2027).
Company: **Demo Finance Co.** (FinMailingName prefix "DEMO FINANCE"). Phone numbers are masked as 98XXXXX### — never invent real numbers.
Device ids are fake: Ravi's phone `3f9a2c1b7d4e8a01`, Senthil's `7b21e9c4a0d35f62`, Priya's `c0ffee12ab34cd56`.

## Branches (BranchConnStrs)
| BId | BranchName | Database |
|---|---|---|
| 1 | Madurai (demo) | DemoFin_MDU |
| 2 | Trichy (demo) | DemoFin_TRY |

## Lines / finances (FinanceMaster) — branch 1
| FinId | FinName | FinMailingName | Break | DefaultLoanMethodId | CashLedgerId (collection cash book used by executives) | FinanceCashBookLedgerId (office cash book) | AdminMobileNos |
|---|---|---|---|---|---|---|---|
| 1 | LINE A - DAILY | DEMO FINANCE - LINE A | D (daily, Mon-Sat) | 1 | 211 | 201 | 98XXXXX001 |
| 2 | LINE B - WEEKLY | DEMO FINANCE - LINE B | W (weekly, Wednesday, WeekDayId 4) | 2 | 212 | 202 | 98XXXXX001 |
| 3 | LINE C - MONTHLY | DEMO FINANCE - LINE C | M (monthly, day 4) | 3 | 213 | 203 | 98XXXXX001 |

## Collection areas (SubAreaDetails)
| SubAreaNo | SubAreaName | FinId | WeekdayId | ShowId |
|---|---|---|---|---|
| 11 | Gandhi Nagar | 1 | 0 (daily) | 1 |
| 12 | Anna Street | 1 | 0 | 2 |
| 13 | Market Road | 1 | 0 | 3 |
| 14 | Bus Stand | 1 | 0 | 4 |
| 21 | Kamaraj Colony | 2 | 4 (Wed) | 1 |
| 22 | Periyar Nagar | 2 | 4 (Wed) | 2 |
| 31 | Town Centre | 3 | 0 (month day 4) | 1 |

## Staff (employee ledgers, MobileDevlicesAllowed)
| LedgerId | Name | Role (UserRoles) | Device | Lines allowed |
|---|---|---|---|---|
| 501 | Ravi Kumar | Collection Executive (URId 3) | 3f9a2c1b7d4e8a01 | 1 |
| 502 | Senthil Murugan | Collection Executive (URId 3) | 7b21e9c4a0d35f62 | 2, 3 |
| 503 | Priya Sundar | Branch Manager, full access (URId 1) | c0ffee12ab34cd56 | 1, 2, 3 |
| 504 | Karthik Raja | Accountant (URId 2) | — (office) | 1, 2, 3 |

## Account ledgers (tblLedgers; groups: 5 bank, 6 cash, 18 indirect expenses, 19 indirect incomes)
| LedgerId | LedgerName | Group |
|---|---|---|
| 201 | Line A Office Cash | 6 (FinanceCashBookLedgerId of line 1) |
| 202 | Line B Office Cash | 6 |
| 203 | Line C Office Cash | 6 |
| 211 | Line A Collection Cash | 6 (CashLedgerId of line 1; Ravi works this book) |
| 212 | Line B Collection Cash | 6 (Senthil) |
| 213 | Line C Collection Cash | 6 (Senthil) |
| 221 | Indian Bank CA (demo) | 5 |
| 104 | Document Charges | 19 (app fixed ledger) |
| 108 | Interest Received | 19 (app fixed ledger) |
| 112 | Interest Paid on Savings | 18 (app fixed ledger) |
| 301 | Petrol | 18 |
| 302 | Tea and Snacks | 18 |
| 303 | Staff Salary | 18 |
| 304 | Office Rent | 18 |
| 311 | Penalty Received | 19 |
| 601 | Ganesan A (partner) | partner ledger |
| 602 | Mala Devi (partner) | partner ledger |

## Loan methods (LoanMethods template rows)
| LoanMethodId | LoanMethodDesc | LoanAmount | IntDeduction | IntAddition | TotAmountCollectable | NoOfIns | InsAmount | CollectionBreak | DocChargesMethod / DocCharges | LoanAmountGiven (in hand) | WarningDays |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | D100 Daily 10,000 x 100 days | 10000 | 1500 | 0 | 10000 | 100 | 100 | 1 (d, daily) | 2 fixed / 200 | 8300 | 10 |
| 2 | W25 Weekly 20,000 x 25 weeks | 20000 | 0 | 4000 | 24000 | 25 | 960 | 2 (w, weekly) | 1 proportional / 400 | 19600 | 14 |
| 3 | M10 Monthly 50,000 x 10 months | 50000 | 0 | 10000 | 60000 | 10 | 6000 | 4 (m, monthly) | 2 fixed / 500 | 49500 | 30 |

Scaling rules the app applies for a new loan of amount A (n = instalments):
- interest-deducted (method 1): Interest = A x 1500/10000; Collectable = A x 10000/10000; Loan in hand = A - (Interest + DocCharges); Instalment = Collectable / n.
- interest-added (methods 2, 3): Interest = A x IntAddition/LoanAmount; Collectable = A + Interest; Loan in hand = A - DocCharges.
- DocChargesMethod 2 = fixed DocCharges; 1 = DocCharges/LoanAmount x A.

## Parties (borrowers, tblLedgers ScreenType 3)
| LedgerId | UD No | CW No | Name | Business (Tag) | Area | Mobile | Guardian |
|---|---|---|---|---|---|---|---|
| 1001 | 101 | 1101 | Murugan K | Tea stall | Gandhi Nagar (11) | 98XXXXX101 | Kannan (father) |
| 1002 | 102 | 1102 | Lakshmi R | Vegetable shop | Gandhi Nagar (11) | 98XXXXX102 | Raman (husband) |
| 1003 | 103 | 1103 | Selvam P | Auto driver | Gandhi Nagar (11) | 98XXXXX103 | Pandian (father) |
| 1004 | 104 | 1104 | Meena S | Tailoring | Anna Street (12) | 98XXXXX104 | Sekar (husband) |
| 1005 | 105 | 1105 | Rajesh V | Mobile accessories | Anna Street (12) | 98XXXXX105 | Velayudham (father) |
| 1006 | 106 | 1106 | Kavitha M | Flower shop | Market Road (13) | 98XXXXX106 | Mani (husband) |
| 1007 | 107 | 1107 | Arumugam T | Fruit cart | Market Road (13) | 98XXXXX107 | Thangavel (father) |
| 1008 | 108 | 1108 | Saranya D | Provision store | Bus Stand (14) | 98XXXXX108 | Dinesh (husband) |
| 1009 | 109 | 1109 | Velu N | Bakery | Kamaraj Colony (21) | 98XXXXX109 | Natarajan (father) |
| 1010 | 110 | 1110 | Revathi G | Saree business | Periyar Nagar (22) | 98XXXXX110 | Gopal (husband) |
| 1011 | 111 | 1111 | Bala Murugan | Hardware shop | Town Centre (31) | 98XXXXX111 | Murugesan (father) |
| 1012 | 112 | 1112 | Deepa K | Beauty parlour | Town Centre (31) | 98XXXXX112 | Kumar (husband) |
| 1013 | 113 | 1113 | Jayanthi R | Idli shop | Anna Street (12) | 98XXXXX113 | Rajan (husband) — NEW party on 04-11-2026 |

## Loans (tblMCLoans) — balances BEFORE today's collection
| MCLoanId | Party | Method | Loan date | A | Interest | DocCh | Collectable | Ins | Collected before today | Balance before today | Status | Today 04-11-2026 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 5001 | Murugan K | 1 | 20-08-2026 | 10000 | 1500 | 200 | 10000 | 100 | 6900 | 3100 | Live | cash 100 |
| 5002 | Lakshmi R | 1 | 05-09-2026 | 15000 | 2250 | 200 | 15000 | 150 | 7950 | 7050 | Live | cash 150 |
| 5003 | Selvam P | 1 | 01-07-2026 | 10000 | 1500 | 200 | 10000 | 100 | 8500 | 1500 | NIPPU (term ended 08-10-2026), to be warned | cash 300 |
| 5004 | Meena S | 1 | 10-10-2026 | 20000 | 3000 | 200 | 20000 | 200 | 4200 | 15800 | Live | cash 200 |
| 5005 | Rajesh V | 1 | 25-10-2026 | 10000 | 1500 | 200 | 10000 | 100 | 900 | 9100 | Live | 0 — not paid ("shop closed") |
| 5006 | Kavitha M | 1 | 15-09-2026 | 12000 | 1800 | 200 | 12000 | 120 | 5280 | 6720 | Live | bank (UPI) 120 |
| 5007 | Arumugam T | 1 | 12-08-2026 | 10000 | 1500 | 200 | 10000 | 100 | 7900 | 2100 | Live | cash 100 |
| 5008 | Saranya D | 1 | 01-09-2026 | 25000 | 3750 | 200 | 25000 | 250 | 13000 | 12000 | Live | cash 250 + penalty 20 |
| 5009 | Jayanthi R (new) | 1 | 04-11-2026 | 15000 | 2250 | 200 | 15000 | 150 | 0 | 15000 | Live, created today offline | on-day collection 0 |
| 6001 | Velu N | 2 | 02-09-2026 (Wed) | 20000 | 4000 | 400 | 24000 | 960 | 8640 (9 weeks) | 15360 | Live | cash 960 |
| 6002 | Revathi G | 2 | 07-10-2026 (Wed) | 30000 | 6000 | 600 | 36000 | 1440 | 7200 (5 weeks) | 28800 | Live | cash 1440 |
| 7001 | Bala Murugan | 3 | 04-06-2026 | 50000 | 10000 | 500 | 60000 | 6000 | 24000 (dues 04-07 .. 04-10) | 36000 | Live | due 5 of 10 on 04-11-2026, cash 6000 |
| 7002 | Deepa K | 3 | 04-09-2026 | 40000 | 8000 | 500 | 48000 | 4800 | 4800 (due 04-10) | 43200 | Live | due 2 of 10 on 04-11-2026, not paid |

## Line A day totals, Wednesday 04-11-2026 (executive Ravi Kumar, collection cash book 211)
- Due collection: 100+150+300+200+0+120+100+250 = **1,220** (cash 1,100 · bank 120); penalty 20.
- Parties: 8 in list, 7 paid, 1 unpaid (Rajesh V).
- New party Jayanthi R + new loan 5009: A 15,000, interest 2,250 deducted, doc charges 200, **loan in hand 12,550**, ins 150 x 100.
- Expenses: Petrol 150, Tea and Snacks 60 = 210.
- Amount received from office (office cash 201 -> collection cash 211): 15,000.
- Collection cash book 211: Dr (in) 15,000 received + 1,100 cash collection + 20 penalty + 2,250 interest + 200 doc charges = 18,570; Cr (out) 15,000 new loan (collectable) + 150 petrol + 60 tea + **3,360 amount paid back to office (211 -> 201)** = 18,570; closing 0.
- Daily Patti (Line A): opening live 55,870 (sum of live balances before today) + new loans 15,000 - live collection 920 = **closing live 69,950**; opening nippu 1,500 - nippu collection 300 = **closing nippu 1,200**; total outstanding 71,150.
- Thittam (expected collection for today, Line A live loans' instalments): 100+150+200+100+120+100+250 = 1,020 live + Selvam's nippu target.

## ST and OF loans (interest-bearing books, line 1)
| Book | No | Party / partner | Date | Amount | Rate | Notes |
|---|---|---|---|---|---|---|
| ST loan (money given out, TTID 3) | STL-1 | Sundaram Traders (demo party) | 01-08-2026 | 100000 | 2.10 % per month (0.07 per 100 per day) | interest collected TTID 4 |
| OF loan (money borrowed, TTID 5) | OFL-1 | Mala Devi (partner 602) | 15-07-2026 | 200000 | 1.50 % per month (0.05 per 100 per day) | interest paid TTID 6 |

## Savings and closures (demo)
- Savings account of Murugan K: balance 2,400, interest 120 at closure on 04-11-2026.
- Loan 5003 (Selvam P) closure planned when balance reaches 0.
