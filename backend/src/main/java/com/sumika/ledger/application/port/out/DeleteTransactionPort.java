package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.TransactionId;

/** 収支記録の削除 outgoing port。 */
public interface DeleteTransactionPort {

  void deleteTransaction(TransactionId id);
}
