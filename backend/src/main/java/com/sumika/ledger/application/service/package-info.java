/**
 * ユースケース実装層。incoming port を実装し、out port を介して外部とやり取りする。
 * トランザクション境界（{@code @Transactional}）はここで定義する。
 */
package com.sumika.ledger.application.service;
