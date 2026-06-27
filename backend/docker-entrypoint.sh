#!/bin/sh
set -e

# ALB を省略する構成では、起動時にタスクの public IP を Route53 の A レコードへ反映する。
# NAT を使わない（タスクに public IP を直接付与する）構成なので、外向き送信元 IP =
# タスク ENI の public IP。checkip.amazonaws.com でその IP を取得する。
# ROUTE53_HOSTED_ZONE_ID / API_HOSTNAME 未設定（ローカル/CI）ではスキップして通常起動する。
if [ -n "${ROUTE53_HOSTED_ZONE_ID:-}" ] && [ -n "${API_HOSTNAME:-}" ]; then
  PUBLIC_IP=$(curl -fsS --max-time 5 https://checkip.amazonaws.com | tr -d '[:space:]' || true)
  if [ -n "${PUBLIC_IP}" ]; then
    echo "Updating Route53: ${API_HOSTNAME} -> ${PUBLIC_IP}"
    aws route53 change-resource-record-sets \
      --hosted-zone-id "${ROUTE53_HOSTED_ZONE_ID}" \
      --change-batch "{\"Changes\":[{\"Action\":\"UPSERT\",\"ResourceRecordSet\":{\"Name\":\"${API_HOSTNAME}\",\"Type\":\"A\",\"TTL\":60,\"ResourceRecords\":[{\"Value\":\"${PUBLIC_IP}\"}]}}]}" \
      || echo "WARN: Route53 update failed; continuing startup"
  else
    echo "WARN: could not determine public IP; skipping Route53 update"
  fi
else
  echo "Route53 update skipped (ROUTE53_HOSTED_ZONE_ID/API_HOSTNAME not set)"
fi

exec java ${JAVA_OPTS:-} -jar /app/app.jar
