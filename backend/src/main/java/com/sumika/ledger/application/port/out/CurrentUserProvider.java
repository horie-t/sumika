package com.sumika.ledger.application.port.out;

import com.sumika.ledger.domain.UserId;

/** 現在の認証済み利用者を取得する outgoing port。実装は認証基盤に依存する adapter 側が担う。 */
public interface CurrentUserProvider {

  UserId currentUserId();
}
