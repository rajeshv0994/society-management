#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
run_id="$$"
network_name="society-e2e-${run_id}"
db_container="society-e2e-postgres-${run_id}"
api_container="society-e2e-backend-${run_id}"

cleanup() {
  docker rm -f "${api_container}" "${db_container}" >/dev/null 2>&1 || true
  docker network rm "${network_name}" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

cd "${repo_root}"
docker compose build backend
docker network create "${network_name}" >/dev/null
docker run -d \
  --name "${db_container}" \
  --network "${network_name}" \
  --network-alias postgres \
  -e POSTGRES_DB=societydb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  --health-cmd='pg_isready -U postgres -d societydb' \
  --health-interval=2s \
  --health-timeout=3s \
  --health-retries=30 \
  postgres:16 >/dev/null

for _ in $(seq 1 40); do
  if [[ "$(docker inspect -f '{{.State.Health.Status}}' "${db_container}")" == "healthy" ]]; then
    break
  fi
  sleep 1
done
if [[ "$(docker inspect -f '{{.State.Health.Status}}' "${db_container}")" != "healthy" ]]; then
  docker logs "${db_container}"
  exit 1
fi

docker run -d \
  --name "${api_container}" \
  --network "${network_name}" \
  -p 127.0.0.1::8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/societydb \
  -e SPRING_PROFILES_ACTIVE=local \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e JWT_SECRET=e2e-development-secret-key-must-be-at-least-32-bytes \
  -e SUPER_ADMIN_EMAIL=admin@society.local \
  -e SUPER_ADMIN_PASSWORD=Admin@12345 \
  -e SEED_DEMO_DATA=true \
  society_app-backend >/dev/null

published_port="$(docker port "${api_container}" 8080/tcp | sed -E 's/.*:([0-9]+)$/\1/' | head -1)"
base_url="http://127.0.0.1:${published_port}"
for _ in $(seq 1 60); do
  if curl -fsS "${base_url}/api/public/plans" >/dev/null 2>&1; then
    break
  fi
  if [[ "$(docker inspect -f '{{.State.Status}}' "${api_container}")" == "exited" ]]; then
    docker logs "${api_container}"
    exit 1
  fi
  sleep 1
done
if ! curl -fsS "${base_url}/api/public/plans" >/dev/null; then
  docker logs "${api_container}"
  exit 1
fi

python3 scripts/complete_app_flow_test.py \
  --base-url "${base_url}" \
  --include-mutations \
  --report build/reports/complete_app_flow_smoke.json
