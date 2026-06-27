package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.CategoryId;
import java.time.LocalDate;

/**
 * 収支記録の検索条件。各フィールドは {@code null} で「絞り込みなし」を表す。
 *
 * @param from 発生日の下限（含む）
 * @param to 発生日の上限（含む）
 * @param categoryId カテゴリ
 */
public record TransactionSearchCriteria(LocalDate from, LocalDate to, CategoryId categoryId) {

  public static TransactionSearchCriteria all() {
    return new TransactionSearchCriteria(null, null, null);
  }
}
