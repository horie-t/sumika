package com.sumika.ledger.application.port.in;

import com.sumika.ledger.domain.Transaction;

/** 収支記録の登録ユースケース。 */
public interface RegisterTransactionUseCase {

  Transaction registerTransaction(RegisterTransactionCommand command);
}
