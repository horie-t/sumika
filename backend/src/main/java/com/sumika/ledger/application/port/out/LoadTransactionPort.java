package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import java.util.List;
import java.util.Optional;

/** 収支記録の取得 outgoing port。 */
public interface LoadTransactionPort {

  Optional<Transaction> loadTransaction(TransactionId id);

  List<Transaction> findTransactions(TransactionSearchCriteria criteria);
}
