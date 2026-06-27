# backend

Spring Boot (Spring Web) / Java で実装する API サーバー。

アーキテクチャは Tom Hombergs『Get Your Hands Dirty on Clean Architecture』(2nd Ed.) に準拠した
ヘキサゴナル（ポート&アダプタ）構成。bounded context 単位でパッケージを切り、各 context を
`domain` / `application`(port in,out, service) / `adapter`(in/web, out/persistence) に分割する。

> 初期化は Issue #2「Spring Boot プロジェクト初期化（ヘキサゴナル構成）」で行う。
