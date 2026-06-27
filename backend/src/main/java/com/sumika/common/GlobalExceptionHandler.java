package com.sumika.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * アプリ固有例外を RFC 7807 ({@link ProblemDetail}) の統一エラーレスポンスに変換する。
 *
 * <p>Spring MVC 標準例外（{@code MethodArgumentNotValidException} など）は
 * {@code spring.mvc.problemdetails.enabled=true} により Spring が ProblemDetail で返す。
 */
@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  ProblemDetail handleNotFound(ResourceNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
  ProblemDetail handleBadRequest(RuntimeException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
  }
}
