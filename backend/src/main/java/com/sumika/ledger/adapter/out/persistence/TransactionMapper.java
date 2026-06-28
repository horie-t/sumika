package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.Money;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import com.sumika.ledger.domain.UserId;
import org.springframework.stereotype.Component;

/** ドメイン {@link Transaction} と {@link TransactionJpaEntity} の相互変換。 */
@Component
class TransactionMapper {

  TransactionJpaEntity toJpaEntity(UserId userId, Transaction transaction) {
    return new TransactionJpaEntity(
        transaction.id().map(TransactionId::value).orElse(null),
        userId.value(),
        transaction.type(),
        transaction.amount().amount().longValueExact(),
        transaction.categoryId().value(),
        transaction.occurredOn(),
        transaction.memo().orElse(null));
  }

  Transaction toDomain(TransactionJpaEntity entity) {
    return Transaction.of(
        new TransactionId(entity.getId()),
        entity.getType(),
        Money.of(entity.getAmount()),
        new CategoryId(entity.getCategoryId()),
        entity.getOccurredOn(),
        entity.getMemo());
  }
}
