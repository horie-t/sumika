package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.Transaction;

/** 収支記録の保存 outgoing port。永続化後（id 付き）の収支記録を返す。 */
public interface SaveTransactionPort {

  Transaction saveTransaction(Transaction transaction);
}
