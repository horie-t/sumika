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
import org.hibernate.annotations.CreationTimestamp;

/** {@code categories} テーブルの JPA エンティティ。 */
@Entity
@Table(name = "categories")
class CategoryJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, length = 64)
  private String userId;

  @Column(nullable = false, length = 50)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EntryType type;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected CategoryJpaEntity() {
    // for JPA
  }

  CategoryJpaEntity(Long id, String userId, String name, EntryType type) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.type = type;
  }

  Long getId() {
    return this.id;
  }

  String getUserId() {
    return this.userId;
  }

  String getName() {
    return this.name;
  }

  EntryType getType() {
    return this.type;
  }
}
