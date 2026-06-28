// 現在のアクセストークンを保持する軽量ホルダ。
// axios インターセプタ（client.ts）と AuthProvider の双方から参照する。
// client.ts が keycloak-js を直接 import せずに済むよう分離している（テスト時の副作用回避）。
let accessToken: string | undefined

export function setAccessToken(token: string | undefined): void {
  accessToken = token
}

export function getAccessToken(): string | undefined {
  return accessToken
}
