import { assertEnvEntry, shq } from './shell';
import { API_PORT } from './deployScript';

/** Fingerprint of the key that signs Hermes' Termux APT repository (from the official docs). */
export const TERMUX_REPO_FINGERPRINT = 'C572B5FDD1A29CCFA9A912B6840B0848E139156D';
const REPO = 'https://hermes-assets.nousresearch.com/releases/termux/stable';

export interface TermuxDeployOptions {
  providerEnv: string;
  providerKey: string;
  providerId: string;
  model: string;
  apiServerKey: string;
}

const HERMES_HOME = '"${HERMES_HOME:-$HOME/.hermes}"';

function startGateway(): string {
  return `H=${HERMES_HOME}
mkdir -p "$H/logs"
pkill -f 'hermes gateway run' 2>/dev/null || true
termux-wake-lock 2>/dev/null || true
nohup setsid hermes gateway run >> "$H/logs/gateway.log" 2>&1 &
for _ in $(seq 1 60); do
  if curl -fsS --max-time 5 http://127.0.0.1:${API_PORT}/health >/dev/null 2>&1; then
    echo "Hermes is running on 127.0.0.1:${API_PORT}."
    exit 0
  fi
  sleep 2
done
tail -n 40 "$H/logs/gateway.log" || true
echo "Hermes did not become healthy. See $H/logs/gateway.log"
exit 1`;
}

/**
 * Installs Hermes from its signed Termux APT repository (aarch64 only), writes the API server and
 * provider keys, then starts the gateway in the background. Runs in a visible Termux session.
 */
export function buildTermuxInstallScript(o: TermuxDeployOptions): string {
  assertEnvEntry(o.providerEnv, o.providerKey);
  assertEnvEntry('API_SERVER_KEY', o.apiServerKey);
  const managed = ['API_SERVER_ENABLED', 'API_SERVER_HOST', 'API_SERVER_PORT', 'API_SERVER_KEY', o.providerEnv];
  return `set -e
trap 'echo; echo "Install failed. Press Enter to close."; read -r _' ERR
echo "== Hermes Deck: installing Hermes Agent in Termux =="
[ "$(uname -m)" = "aarch64" ] || { echo "Hermes for Termux supports aarch64 phones only."; exit 1; }
pkg install -y curl gnupg
mkdir -p "$PREFIX/etc/apt/keyrings"
curl -fsSL ${REPO}/key.asc -o "$PREFIX/etc/apt/keyrings/hermes-agent.asc"
FPR="$(gpg --show-keys --with-colons --with-fingerprint "$PREFIX/etc/apt/keyrings/hermes-agent.asc" | awk -F: '/^fpr:/ {print $10; exit}')"
if [ "$FPR" != "${TERMUX_REPO_FINGERPRINT}" ]; then
  echo "Repository key fingerprint mismatch ($FPR). Aborting."
  false
fi
printf '%s\\n' "deb [signed-by=$PREFIX/etc/apt/keyrings/hermes-agent.asc] ${REPO} hermes-stable main" > "$PREFIX/etc/apt/sources.list.d/hermes-agent.list"
apt-get update
apt-get install -y hermes-agent
H=${HERMES_HOME}
mkdir -p "$H"
chmod 700 "$H"
touch "$H/.env"
chmod 600 "$H/.env"
merged="$(mktemp)"
grep -vE '^(${managed.join('|')})=' "$H/.env" > "$merged" || true
cat >> "$merged" <<'HERMES_DECK_ENV'
API_SERVER_ENABLED=true
API_SERVER_HOST=127.0.0.1
API_SERVER_PORT=${API_PORT}
API_SERVER_KEY=${o.apiServerKey}
${o.providerEnv}=${o.providerKey}
HERMES_DECK_ENV
cat "$merged" > "$H/.env"
rm -f "$merged"
hermes config set model.provider ${shq(o.providerId)}
hermes config set model.default ${shq(o.model)}
trap - ERR
(
${startGateway()}
)
echo
echo "Done. Switch back to Hermes Deck. Press Enter to close this session."
read -r _
`;
}

export function termuxStartScript(): string {
  return startGateway();
}

export function termuxStopScript(): string {
  return `pkill -f 'hermes gateway run' && echo stopped || echo 'not running'
termux-wake-unlock 2>/dev/null || true`;
}

export function termuxLogsScript(): string {
  return `tail -n 200 ${HERMES_HOME}/logs/gateway.log 2>&1 || echo 'No gateway log yet.'`;
}
