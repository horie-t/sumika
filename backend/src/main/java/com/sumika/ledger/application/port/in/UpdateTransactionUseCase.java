package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.Transaction;

/** 収支記録の更新ユースケース。 */
public interface UpdateTransactionUseCase {

  Transaction updateTransaction(UpdateTransactionCommand command);
}
