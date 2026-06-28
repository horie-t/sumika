# syntax=docker/dockerfile:1
# 本番（AWS/ECS）向け Keycloak イメージ。realm を同梱し Postgres 向けに最適化ビルドする。
# Route53 更新は ECS タスクの sidecar（aws-cli）が行うため、本イメージには awscli を入れない。

# ---- build stage: Postgres 向けに最適化ビルド ----
# ビルド時オプション（db / health / relative-path）はここで焼き込む（start --optimized と整合）。
FROM quay.io/keycloak/keycloak:26.0 AS builder
ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ENV KC_HTTP_RELATIVE_PATH=/auth
COPY realm-sumika.json /opt/keycloak/data/import/realm-sumika.json
RUN /opt/keycloak/bin/kc.sh build

# ---- runtime stage ----
FROM quay.io/keycloak/keycloak:26.0
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY docker-entrypoint.sh /opt/keycloak/docker-entrypoint.sh
USER root
RUN chmod +x /opt/keycloak/docker-entrypoint.sh \
    && chmod -R g+rwX /opt/keycloak/data/import
USER 1000
ENTRYPOINT ["/opt/keycloak/docker-entrypoint.sh"]
