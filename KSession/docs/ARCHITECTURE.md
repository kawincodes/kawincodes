# KSession architecture

KSession is a native Android app, written in Java, for running **Cloudflare Tunnels from a phone**. It ships
the official `cloudflared` engine, signs in to Cloudflare (browser login or API token), creates and routes
tunnels, and keeps them running in a foreground service. You drive it through a **chat console** or the
**radar dashboard**. All icons are [Lucide](https://lucide.dev).

## 1. Why the app is built this way

### 1.1 Running cloudflared on Android
* `cloudflared` release binaries are static Go executables (`GOOS=linux`, `CGO_ENABLED=0`). They run on Android,
  but Go's pure resolver reads `/etc/resolv.conf`, which **does not exist on Android**. Every DNS lookup made by
  the binary fails (`lookup X on [::1]:53: ... connection refused`).
* So the binary is used **only for the data plane** (`cloudflared tunnel run`). Everything that needs DNS or
  HTTP is done in Java, which uses Android's resolver:
  * **Edge discovery**: Java resolves `region1.v2.argotunnel.com` and `region2.v2.argotunnel.com` and passes the
    IPs with the hidden `--edge ip:7844` flag (repeatable). cloudflared's `StaticEdge` uses IP literals without
    DNS and alternates them into region 1/2, so pass them **interleaved** (r1, r2, r1, r2, ...). If Java
    resolution fails, use the built-in fallback lists (`EdgeResolver`).
  * **Quick tunnels** (TryCloudflare): Java does `POST https://api.trycloudflare.com/tunnel` (empty body,
    `Content-Type: application/json`, `User-Agent: cloudflared/<version>`). The response is
    `{"success":true,"result":{"id","name","hostname","account_tag","secret"},"errors":[]}` (`secret` is
    base64). The app writes a credentials file and runs it as a named tunnel; the public URL is
    `https://<hostname>`. This is exactly what `cloudflared tunnel --url` does internally.
  * **Browser login** (same as `cloudflared tunnel login`): generate 32 random bytes, base64url encode them
    *with padding* (`KEY`), open
    `https://dash.cloudflare.com/argotunnel?aud=&callback=` + urlencode(`https://login.cloudflareaccess.org/KEY`),
    then long-poll `GET https://login.cloudflareaccess.org/KEY` (60 s read timeout, `User-Agent:
    cloudflared/<version>`). A non-200 status below 500 means "still waiting"; 200 returns the cert PEM;
    5xx is an error. cloudflared polls 10 times.
    The PEM contains a block `-----BEGIN ARGO TUNNEL TOKEN-----` whose base64 body is JSON
    `{"zoneID","accountID","apiToken","endpoint"?}` (other blocks may be `PRIVATE KEY`/`CERTIFICATE`).
    `apiToken` is a Bearer token for the Cloudflare API scoped to that account and zone.
  * **Cloudflare API** (`https://api.cloudflare.com/client/v4`, `Authorization: Bearer <token>`):
    * `GET /user/tokens/verify` (user tokens) or `GET /accounts/{acc}/tokens/verify` (account tokens)
    * `GET /accounts` · `GET /zones?account.id=&per_page=50&page=` · `GET /zones/{zone}`
    * `GET /accounts/{acc}/cfd_tunnel?is_deleted=false&per_page=100&page=`
    * `POST /accounts/{acc}/cfd_tunnel` body `{"name","tunnel_secret":"<base64 32 bytes>","config_src":"local"}`
    * `GET /accounts/{acc}/cfd_tunnel/{id}/token` → `result` is the connector token string
    * `GET|DELETE /accounts/{acc}/cfd_tunnel/{id}/connections` · `DELETE /accounts/{acc}/cfd_tunnel/{id}`
    * DNS route (what `cloudflared tunnel route dns` uses; works with the browser-login token):
      `PUT /zones/{zone}/tunnels/{id}/routes` body `{"type":"dns","user_hostname":"h","overwrite_existing":false}`.
      Fallback: `POST /zones/{zone}/dns_records` `{"type":"CNAME","name":h,"content":"<id>.cfargotunnel.com","proxied":true,"comment":"KSession"}`.
    * Responses: `{"success":bool,"errors":[{"code","message"}],"result":...,"result_info":{"page","total_pages"}}`.
* Connector token (from the dashboard or `/token`): base64(JSON `{"a": accountTag, "t": tunnelId, "s": base64Secret, "e"?: endpoint}`).
* Credentials file (for `--credentials-file` / config `credentials-file:`):
  `{"AccountTag":"...","TunnelSecret":"<base64>","TunnelID":"<uuid>"}`.

### 1.2 Command lines (pinned cloudflared 2026.10.3)
```
<bin> tunnel --no-autoupdate --no-prechecks --loglevel info --metrics 127.0.0.1:<port>
      --protocol <auto|quic|http2> --ha-connections <n> --grace-period 5s [--post-quantum]
      --edge <ip:7844> --edge <ip:7844> ... --label ksession-<device>
      --config <workdir>/config.yml run                       # QUICK and MANAGED (local ingress)
      run --token-file <workdir>/token                         # TOKEN (remote-managed config)
```
* `config.yml` for QUICK/MANAGED: `tunnel: <id>`, `credentials-file: <path>`, `ingress:` list ending with a
  catch-all (`service: http_status:404` for MANAGED; QUICK has a single catch-all `service: <origin>`).
* Quick tunnels force `--ha-connections 1` and `--protocol quic` unless the user picked one.
* Process env: `HOME=<filesDir>/engine-home`, `TMPDIR=<cacheDir>`. Write secrets (token, credentials) to
  `<filesDir>/run/<profileId>/` with owner-only permissions, delete them when the tunnel stops.
* Origins: normalise `localhost` to `127.0.0.1` in generated configs (Go reads `/etc/hosts`, which exists on
  Android, but IPs avoid surprises).

Verified log lines (`<RFC3339> <LVL> <message> key=value key="quoted value"`, levels `DBG INF WRN ERR FTL`):
```
2026-10-23T04:44:56Z INF Starting tunnel tunnelID=1111...
2026-10-23T04:44:56Z INF Version 2026.10.3 (Checksum 053b...)
2026-10-23T04:44:56Z ERR Failed to fetch features, default to disable error="lookup cfd-features.argotunnel.com on [::1]:53: ..."   <- harmless on Android, show as warning
2026-10-23T04:44:56Z INF Initial protocol http2
INF Starting metrics server on 127.0.0.1:20241/metrics
INF Registered tunnel connection connIndex=0 connection=<uuid> event=0 ip=198.41.192.7 location=bom01 protocol=quic
INF Unregistered tunnel connection connIndex=0 event=0 ip=198.41.192.7
INF Lost connection with the edge connIndex=0
ERR Connection terminated error="..." connIndex=0
INF Retrying connection in up to 2s connIndex=0 event=0 ip=...
ERR Serve tunnel error error="..." connIndex=0 event=0 ip=...
INF Initiating graceful shutdown due to signal terminated ...
INF Tunnel server stopped
```
Metrics (Prometheus text on `--metrics`): `cloudflared_tunnel_total_requests`, `cloudflared_tunnel_request_errors`,
`cloudflared_tunnel_concurrent_requests_per_tunnel`, `cloudflared_tunnel_ha_connections`.

### 1.3 Installing the engine inside the app
Android 10+ blocks `exec()` of files an app wrote to its data dir when the app targets API 29+ (W^X).
* **store flavor** (targetSdk 36): `app/build.gradle` downloads the pinned release for every ABI, checks the
  SHA-256, and packs it as `lib/<abi>/libcloudflared.so` with legacy packaging, so the installer extracts it
  to `ApplicationInfo.nativeLibraryDir` (executable).
* **sideload flavor** (targetSdk 28): additionally lets the user download/update cloudflared **inside the
  app** from GitHub releases (`https://github.com/cloudflare/cloudflared/releases/download/<tag>/<asset>`,
  latest tag via `https://api.github.com/repos/cloudflare/cloudflared/releases/latest`, whose assets carry
  `"digest":"sha256:<hex>"`). It is verified, stored at `<filesDir>/engine/cloudflared`, chmod 700, and
  test-run with `--version`.
* ABI → asset: `arm64-v8a`→`cloudflared-linux-arm64`, `armeabi-v7a`→`cloudflared-linux-armhf`,
  `x86_64`→`cloudflared-linux-amd64`, `x86`→`cloudflared-linux-386`. Pinned checksums: `BuildConfig.ENGINE_SHA256`
  (`abi=hex;abi=hex`).

## 2. Code map and ownership

Package root `com.kawincodes.ksession` (`app/src/main/java/com/kawincodes/ksession`). The foundation (models,
contracts, theme, manifest, helpers) is shared and **must not be changed by module work**. Each module owns
the listed files: implement the stubbed bodies, keep every public signature, and add new classes only inside
the module's own packages. Resources: put strings in `res/values/strings_<module>.xml` and prefix layouts and
drawables with the module prefix.

| Module | Owns | Prefix |
|---|---|---|
| foundation | `KSessionApp`, `AppGraph`, `util/*`, `core/settings/*`, all models (`core/tunnel/{TunnelType,TunnelProfile,IngressRule,TunnelStatus,TunnelSnapshot,EdgeConnection,TunnelMetrics,TunnelEventListener}`, `core/process/{LogLine,LogLevel}`, `core/engine/{EngineInfo,EngineSource}`, `core/cloud/model/*`, `chat/model/*`), `ui/Navigator`, `ui/common/*`, manifest, `values/{colors,themes,styles,dimens,strings,ids}.xml`, `xml/*`, Lucide `ic_*` drawables | — |
| runtime | `core/engine/{EngineManager,EngineInstaller,EngineAbi}`, `core/process/*` (except models), `core/tunnel/{TunnelRepository,TunnelController,QuickTunnelClient,EdgeResolver,ConfigWriter,MetricsClient,PrometheusParser,LogBuffer}`, `service/*` | `runtime_` |
| cloud | `core/cloud/*` (except `model/`), `core/security/*`, `core/server/*` | `cloud_` |
| chat | `chat/*` (except `model/`), `ui/chat/*` | `chat_` |
| shell | `ui/MainActivity`, `ui/radar/*`, `ui/lock/*` | `shell_`, `radar_` |
| screens | `ui/tunnels/*`, `ui/cloud/*` | `tunnels_`, `cloud_ui_` |

Dependency direction: `ui/*` and `chat` → `AppGraph` services → models. Services never reference UI classes,
except `service/*` may start `ui/MainActivity` via a `PendingIntent`.

## 3. Conventions
* Java 17 source level, AndroidX, Material Components 3. No Kotlin, no extra libraries beyond `app/build.gradle`.
* Threading: blocking work on `AppExecutors.io()`. `Callback`s are delivered on the **main thread**.
  LiveData updates from background threads use `postValue`.
* JSON with `org.json`. HTTP through `util/HttpClient` (`AppGraph.http()`); unit tests inject fakes.
* Secrets only in `CredentialStore` (Android Keystore AES-GCM). Keys: `cf.api_token`, `cf.origin_cert`,
  `cf.account_id`, `tunnel.<profileId>.token`, `tunnel.<profileId>.credentials` (credentials JSON).
  Never log secrets; never put them in `TunnelProfile` JSON or command-line arguments.
* Pure logic (parsers, command builders, codecs) lives in classes with no Android dependencies so it is unit
  tested on the JVM (`app/src/test/java`).
* Icons: only the Lucide drawables `R.drawable.ic_*` (see `tools/lucide/icons.txt`; add names there and
  regenerate if a new one is needed). Tint them with theme colors.

## 4. Design language ("radical radar")
Dark, neon, instrument-panel. Background `#05070D` with a subtle radial glow, surfaces `#0D1117`/`#121A26`,
1 dp outlines `#1F2A3A`, radius 20 dp cards and 28 dp sheets. Accent teal `#00C9A7` (glow `#2CF5D0`), violet
`#7C3AED` (light `#A78BFA`), amber `#FFB020`, red `#FF4D6D`. Monospace for URLs, IDs, logs. Status colors come
from `ui/common/StatusStyle`. Motion: radar sweep ~4 s per turn, blips fade like phosphor after the sweep
passes, pulses on new connections. Bottom navigation: Chat · Radar · Tunnels · Cloud.
