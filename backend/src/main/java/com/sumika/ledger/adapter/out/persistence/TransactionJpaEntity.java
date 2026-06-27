package com.sumika.ledger.adapter.out.persistence;

import com.sumika.ledger.domain.EntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** {@code transactions} テーブルの JPA エンティティ。 */
@Entity
@Table(name = "transactions")
class TransactionJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EntryType type;

  @Column(nullable = false)
  private Long amount;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @Column(name = "occurred_on", nullable = false)
  private LocalDate occurredOn;

  @Column(length = 255)
  private String memo;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected TransactionJpaEntity() {
    // for JPA
  }

  TransactionJpaEntity(
      Long id, EntryType type, Long amount, Long categoryId, LocalDate occurredOn, String memo) {
    this.id = id;
    this.type = type;
    this.amount = amount;
    this.categoryId = categoryId;
    this.occurredOn = occurredOn;
    this.memo = memo;
  }

  Long getId() {
    return this.id;
  }

  EntryType getType() {
    return this.type;
  }

  Long getAmount() {
    return this.amount;
  }

  Long getCategoryId() {
    return this.categoryId;
  }

  LocalDate getOccurredOn() {
    return this.occurredOn;
  }

  String getMemo() {
    return this.memo;
  }
}
