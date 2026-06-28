package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.domain.EntryType;

/** カテゴリ別集計クエリの interface projection。 */
interface CategoryAmountProjection {

  Long getCategoryId();

  String getCategoryName();

  EntryType getType();

  long getTotal();
}
