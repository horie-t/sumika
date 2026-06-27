package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.application.port.out.DeleteTransactionPort;
import com.sumika.ledger.application.port.out.LoadTransactionPort;
import com.sumika.ledger.application.port.out.SaveTransactionPort;
import com.sumika.ledger.application.port.out.TransactionSearchCriteria;
import com.sumika.ledger.domain.CategoryId;
import com.sumika.ledger.domain.Transaction;
import com.sumika.ledger.domain.TransactionId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/** 収支記録の out ポート群を JPA で実装する永続化アダプタ。 */
@Component
class TransactionPersistenceAdapter
    implements LoadTransactionPort, SaveTransactionPort, DeleteTransactionPort {

  private final TransactionJpaRepository repository;
  private final TransactionMapper mapper;

  TransactionPersistenceAdapter(TransactionJpaRepository repository, TransactionMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Transaction> loadTransaction(TransactionId id) {
    return this.repository.findById(id.value()).map(this.mapper::toDomain);
  }

  @Override
  public boolean existsByCategory(CategoryId categoryId) {
    return this.repository.existsByCategoryId(categoryId.value());
  }

  @Override
  public List<Transaction> findTransactions(TransactionSearchCriteria criteria) {
    List<Specification<TransactionJpaEntity>> specs = new ArrayList<>();
    if (criteria.from() != null) {
      LocalDate from = criteria.from();
      specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("occurredOn"), from));
    }
    if (criteria.to() != null) {
      LocalDate to = criteria.to();
      specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("occurredOn"), to));
    }
    if (criteria.categoryId() != null) {
      Long categoryId = criteria.categoryId().value();
      specs.add((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
    }
    Sort sort = Sort.by(Sort.Order.desc("occurredOn"), Sort.Order.desc("id"));
    return this.repository.findAll(Specification.allOf(specs), sort).stream()
        .map(this.mapper::toDomain)
        .toList();
  }

  @Override
  public Transaction saveTransaction(Transaction transaction) {
    TransactionJpaEntity saved = this.repository.save(this.mapper.toJpaEntity(transaction));
    return this.mapper.toDomain(saved);
  }

  @Override
  public void deleteTransaction(TransactionId id) {
    this.repository.deleteById(id.value());
  }
}
