package com.sumika.ledger.adapter.out.security;

import com.sumika.ledger.application.port.out.CurrentUserProvider;
import com.sumika.ledger.domain.UserId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Spring Security の SecurityContext から JWT の subject を読み、現在の利用者 ID を返す。 */
@Component
class SecurityContextCurrentUserProvider implements CurrentUserProvider {

  @Override
  public UserId currentUserId() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      return UserId.of(jwtAuth.getToken().getSubject());
    }
    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      return UserId.of(jwt.getSubject());
    }
    throw new IllegalStateException("認証済み利用者が存在しません");
  }
}
