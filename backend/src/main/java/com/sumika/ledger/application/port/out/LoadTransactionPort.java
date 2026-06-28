package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import com.sumika.ledger.domain.UserId;
import java.util.List;
import java.util.Optional;

/** 収支記録の取得 outgoing port。すべて利用者でスコープする。 */
public interface LoadTransactionPort {

  Optional<Transaction> loadTransaction(UserId userId, TransactionId id);

  List<Transaction> findTransactions(UserId userId, TransactionSearchCriteria criteria);

  /** 指定カテゴリを参照する収支記録が（その利用者に）存在するか。 */
  boolean existsByCategory(UserId userId, CategoryId categoryId);
}
