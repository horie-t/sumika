/**
 * {@code ledger}（家計簿台帳）bounded context。
 *
 * <p>Tom Hombergs『Get Your Hands Dirty on Clean Architecture』に倣ったヘキサゴナル
 * （ポート&アダプタ）構成。依存方向は常に内向き:
 * {@code adapter -> application -> domain}。{@code domain} は外部に依存しない。
 *
 * <ul>
 *   <li>{@code domain} — フレームワーク非依存のリッチドメインモデル</li>
 *   <li>{@code application.port.in} — incoming port（ユースケース IF + Command/Query）</li>
 *   <li>{@code application.port.out} — outgoing port（application が定義し adapter が実装）</li>
 *   <li>{@code application.service} — ユースケース実装</li>
 *   <li>{@code adapter.in.web} — incoming adapter（REST）</li>
 *   <li>{@code adapter.out.persistence} — outgoing adapter（JPA）</li>
 * </ul>
 */
package com.sumika.ledger;
