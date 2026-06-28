package com.sumika.ledger.adapter.out.persistence;

/** 月別集計クエリ（native）の interface projection。{@code type} は varchar のため String で受ける。 */
interface MonthlyAmountProjection {

  int getYear();

  int getMonth();

  String getType();

  long getTotal();
}
