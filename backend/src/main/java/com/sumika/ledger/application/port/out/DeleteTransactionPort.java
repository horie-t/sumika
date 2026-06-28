package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.TransactionId;
import com.sumika.ledger.domain.UserId;

/** 収支記録の削除 outgoing port。利用者でスコープする。 */
public interface DeleteTransactionPort {

  void deleteTransaction(UserId userId, TransactionId id);
}
