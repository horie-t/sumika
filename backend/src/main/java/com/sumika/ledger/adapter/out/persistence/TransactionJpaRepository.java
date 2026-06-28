package com.sumika.ledger.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TransactionJpaRepository
    extends JpaRepository<TransactionJpaEntity, Long>,
        JpaSpecificationExecutor<TransactionJpaEntity> {

  boolean existsByCategoryId(Long categoryId);

  /** 期間内の収支をカテゴリ別に合計する（カテゴリ名・種別はアドホック結合で取得）。 */
  @Query(
      """
      SELECT t.categoryId AS categoryId, c.name AS categoryName, c.type AS type, SUM(t.amount) AS total
      FROM TransactionJpaEntity t
      JOIN CategoryJpaEntity c ON c.id = t.categoryId
      WHERE t.occurredOn BETWEEN :from AND :to
      GROUP BY t.categoryId, c.name, c.type
      """)
  List<CategoryAmountProjection> aggregateByCategory(
      @Param("from") LocalDate from, @Param("to") LocalDate to);

  /** 期間内の収支を年月・種別ごとに合計する（Postgres の EXTRACT を使う native クエリ）。 */
  @Query(
      value =
          """
          SELECT EXTRACT(YEAR FROM occurred_on)::int AS year,
                 EXTRACT(MONTH FROM occurred_on)::int AS month,
                 type AS type,
                 SUM(amount)::bigint AS total
          FROM transactions
          WHERE occurred_on BETWEEN :from AND :to
          GROUP BY EXTRACT(YEAR FROM occurred_on), EXTRACT(MONTH FROM occurred_on), type
          """,
      nativeQuery = true)
  List<MonthlyAmountProjection> aggregateByMonth(
      @Param("from") LocalDate from, @Param("to") LocalDate to);
}
