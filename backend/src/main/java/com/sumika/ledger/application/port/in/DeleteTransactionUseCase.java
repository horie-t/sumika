package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.TransactionId;

/** 収支記録の削除ユースケース。 */
public interface DeleteTransactionUseCase {

  void deleteTransaction(TransactionId id);
}
