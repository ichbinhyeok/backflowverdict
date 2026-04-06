#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

APP_NAME="${APP_NAME:-backflowpath}"
APP_USER="${APP_USER:-backflow}"
APP_GROUP="${APP_GROUP:-backflow}"
APP_HOME="${APP_HOME:-/opt/backflowpath}"
APP_JAR_PATH="${APP_JAR_PATH:-}"
ENV_FILE_PATH="${ENV_FILE_PATH:-/etc/backflowpath/backflowpath.env}"
SERVICE_NAME="${SERVICE_NAME:-backflowpath}"
SYSTEMD_UNIT_PATH="/etc/systemd/system/${SERVICE_NAME}.service"

if [[ -z "${APP_JAR_PATH}" ]]; then
  APP_JAR_PATH="$(find "${REPO_ROOT}/build/libs" -maxdepth 1 -type f -name '*.jar' | head -n 1 || true)"
fi

if [[ -z "${APP_JAR_PATH}" || ! -f "${APP_JAR_PATH}" ]]; then
  echo "No boot jar found. Run ./gradlew bootJar first or set APP_JAR_PATH."
  exit 1
fi

if ! getent group "${APP_GROUP}" >/dev/null 2>&1; then
  groupadd --system "${APP_GROUP}"
fi

if ! id -u "${APP_USER}" >/dev/null 2>&1; then
  useradd --system --gid "${APP_GROUP}" --home "${APP_HOME}" --shell /sbin/nologin "${APP_USER}"
fi

mkdir -p "${APP_HOME}" "$(dirname "${ENV_FILE_PATH}")"
install -d -o "${APP_USER}" -g "${APP_GROUP}" -m 0755 "${APP_HOME}/logs"
install -o "${APP_USER}" -g "${APP_GROUP}" -m 0644 "${APP_JAR_PATH}" "${APP_HOME}/${APP_NAME}.jar"
install -o root -g root -m 0644 "${SCRIPT_DIR}/${SERVICE_NAME}.service" "${SYSTEMD_UNIT_PATH}"

install -d -o "${APP_USER}" -g "${APP_GROUP}" -m 0755 "${APP_HOME}/data" "${APP_HOME}/ops" "${APP_HOME}/storage" "${APP_HOME}/leads"
cp -an "${REPO_ROOT}/data/." "${APP_HOME}/data/"
cp -an "${REPO_ROOT}/storage/." "${APP_HOME}/storage/"
cp -an "${REPO_ROOT}/ops/." "${APP_HOME}/ops/"
chown -R "${APP_USER}:${APP_GROUP}" "${APP_HOME}/data" "${APP_HOME}/ops" "${APP_HOME}/storage" "${APP_HOME}/leads" "${APP_HOME}/logs"

if [[ ! -f "${ENV_FILE_PATH}" ]]; then
  install -o root -g root -m 0640 "${SCRIPT_DIR}/${SERVICE_NAME}.env.example" "${ENV_FILE_PATH}"
  echo "Created ${ENV_FILE_PATH} from example. Review it before starting the service."
fi

systemctl daemon-reload
systemctl enable "${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"
systemctl --no-pager --full status "${SERVICE_NAME}"
