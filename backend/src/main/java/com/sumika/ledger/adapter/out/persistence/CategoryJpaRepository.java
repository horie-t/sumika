package com.sumika.ledger.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

  List<CategoryJpaEntity> findByUserIdOrderById(String userId);

  Optional<CategoryJpaEntity> findByUserIdAndId(String userId, Long id);

  boolean existsByUserIdAndId(String userId, Long id);

  long deleteByUserIdAndId(String userId, Long id);
}
