import Keycloak from 'keycloak-js'

/**
 * Keycloak クライアントのシングルトン。接続先は環境変数で上書きできる
 * （既定はローカル docker-compose の Keycloak）。`init()` までネットワークアクセスは発生しない。
 */
export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8081',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'sumika',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'sumika-frontend',
})
