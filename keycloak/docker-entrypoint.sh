#!/bin/sh
set -e

IMPORT_FILE=/opt/keycloak/data/import/realm-sumika.json

# realm の redirect/webOrigins はローカル既定(http://localhost:5173)。
# 本番は SUMIKA_FRONTEND_URL（= CloudFront の URL）に書き換えてからインポートする。
if [ -n "${SUMIKA_FRONTEND_URL:-}" ]; then
  echo "Rewriting realm frontend URL -> ${SUMIKA_FRONTEND_URL}"
  sed -i "s#http://localhost:5173#${SUMIKA_FRONTEND_URL}#g" "${IMPORT_FILE}"
fi

# 最適化ビルド済みサーバを起動し、未作成なら realm をインポートする。
exec /opt/keycloak/bin/kc.sh start --optimized --import-realm
