package com.sumika.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

/**
 * Command/Query 入力モデルの自己バリデーション基底。
 *
 * <p>サブクラスはコンストラクタ末尾で {@link #validateSelf()} を呼び、Bean Validation 制約を
 * 検証する。Tom Hombergs『Get Your Hands Dirty on Clean Architecture』の同名クラスに倣う。
 */
public abstract class SelfValidating<T> {

  private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
  private static final Validator VALIDATOR = FACTORY.getValidator();

  @SuppressWarnings("unchecked")
  protected void validateSelf() {
    Set<ConstraintViolation<T>> violations = VALIDATOR.validate((T) this);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
