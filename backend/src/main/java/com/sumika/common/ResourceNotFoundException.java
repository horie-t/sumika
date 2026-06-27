package com.sumika.common;

/** 対象のリソースが存在しない場合にスローする例外（HTTP では 404 に対応）。 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
