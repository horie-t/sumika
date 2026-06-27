package com.sumika.common;

/** 取引から参照中のカテゴリを削除しようとした場合の例外（HTTP では 409 に対応）。 */
public class CategoryInUseException extends RuntimeException {

  public CategoryInUseException(String message) {
    super(message);
  }
}
