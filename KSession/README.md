# KSession

**Cloudflare Tunnels in your pocket.** KSession is a native Android app (Java) that installs and runs the
official Cloudflare Tunnel engine (`cloudflared`) on your phone, signs in to your Cloudflare account, and
publishes services from the phone or your LAN to the internet. You drive it by chatting with **Kay**, the
built-in assistant, or from a live **radar** of your tunnels and the Cloudflare data centres they connect to.
All icons are [Lucide](https://lucide.dev).

| Chat | Radar | Tunnels | Cloud |
|---|---|---|---|
| Type "expose port 3000" or `/quick 3000` and get a public URL with a QR code | Animated radar: tunnels as blips, edge data centres (BOM, SIN, …) on the outer ring, live request and uptime stats | Start, stop, restart, logs, QR, share, edit, auto-start, delete | Cloudflare login, engine install and update, Pocket Server, settings |

## Features

- **Engine inside the app**: cloudflared 2026.10.3 ships in the APK for arm64-v8a, armeabi-v7a, x86_64 and x86.
  It is downloaded at build time and checked against pinned SHA-256 hashes. The **sideload** build can also
  download, verify and update cloudflared from GitHub releases inside the app.
- **Four ways to tunnel**
  - **Quick**: a TryCloudflare tunnel with no account and a random `*.trycloudflare.com` URL.
  - **Token**: paste a connector token, or the whole `cloudflared service install …` command, from the Zero
    Trust dashboard.
  - **Managed**: KSession creates a named tunnel in your account, routes your hostnames in DNS and keeps the
    ingress rules on the phone.
  - **Import**: pick an existing tunnel from your account and run it from the phone.
- **Two sign-in methods**
  - **Browser login**: the same flow as `cloudflared tunnel login`. You authorise one zone in the Cloudflare
    dashboard and KSession receives the account certificate.
  - **API token**: paste a scoped API token. KSession then lists your accounts, zones and tunnels.
- **Chat assistant "Kay"**: slash commands and plain English ("start a quick tunnel on 8080",
  "stop everything", "who am i", "install cloudflared"). Replies include live tunnel cards, links, QR codes
  and action chips.
- **Radar dashboard**: a phosphor-style sweep with a blip per tunnel and per edge connection, plus online
  count, edge links, requests and uptime.
- **Always on**: a foreground service keeps tunnels running. KSession restarts tunnels after a crash with
  backoff, reconnects after network changes, and can start tunnels on boot, keep the device awake and stop
  everything from a Quick Settings tile.
- **Live logs**: a colour-coded cloudflared console with filter, search, share and copy.
- **Metrics**: requests, errors and connection counts are scraped from cloudflared's Prometheus endpoint.
- **Pocket Server**: a built-in web server on `127.0.0.1` with a demo page, a status API and file sharing
  through unguessable links, so a tunnel works out of the box.
- **Security**: secrets are encrypted with an Android Keystore AES-GCM key. They are never written to logs,
  command lines or backups. Optional biometric or device-credential app lock.

## How it works on Android

`cloudflared` is a static Go binary. Go's DNS resolver reads `/etc/resolv.conf`, which Android does not have,
so every lookup the binary makes fails. KSession splits the work:

| Task | Done by |
|---|---|
| Edge discovery (`region1/2.v2.argotunnel.com`) | Java, passed to cloudflared as `--edge ip:7844` |
| Quick tunnel provisioning (`api.trycloudflare.com`) | Java, then run as a named tunnel |
| Browser login (`dash.cloudflare.com/argotunnel` and `login.cloudflareaccess.org`) | Java |
| Cloudflare API (tunnels, tokens, DNS routes) | Java |
| Data plane (QUIC or HTTP/2 to the edge, proxying to your origin) | cloudflared |

Android 10+ refuses to execute files an app writes into its own storage (W^X). The **store** flavor
(targetSdk 36) therefore packs the engine as `lib/<abi>/libcloudflared.so`, which the system extracts to an
executable directory. The **sideload** flavor targets API 28, where downloaded binaries may still be
executed, so the engine can be installed and updated inside the app.

Details: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Build

Requirements: JDK 17+ and an Android SDK with platform 36 (set `sdk.dir` in `local.properties` or
`ANDROID_HOME`).

```bash
cd KSession
./gradlew assembleStoreDebug          # Play-style build, engine bundled
./gradlew assembleSideloadDebug       # sideload build, engine also installable in-app
./gradlew testStoreDebugUnitTest      # JVM unit tests
./gradlew lintStoreDebug
```

The APKs are written to `app/build/outputs/apk/<flavor>/debug/`, one per ABI plus a universal APK.

| Gradle property | Default | Purpose |
|---|---|---|
| `ksession.bundleEngine` | `true` | Download, verify and pack cloudflared into the APK |
| `ksession.engineVersion` | `2026.10.3` | Engine version to bundle (needs pinned hashes in `app/build.gradle`) |
| `ksession.abis` | `arm64-v8a,armeabi-v7a,x86_64,x86` | ABIs to bundle and split |

For example, `./gradlew assembleStoreDebug -Pksession.bundleEngine=false` builds without the engine. CI
(`.github/workflows/ksession-android.yml`) runs tests and lint, then uploads the APKs as artifacts.

## API token permissions

Create a token at <https://dash.cloudflare.com/profile/api-tokens> with:

- Account › Cloudflare Tunnel › Edit
- Account › Account Settings › Read
- Zone › Zone › Read
- Zone › DNS › Edit

## Chat commands

| Command | Example |
|---|---|
| `/quick [port\|url] [name]` | `/quick 3000`, or "expose localhost:5173" |
| `/start`, `/stop`, `/restart <name\|all>` | `/stop all` |
| `/create <name> <hostname> [service]` | `/create blog blog.example.com localhost:2368` |
| `/token <token> [name]` | paste a connector token or the install command |
| `/list`, `/status`, `/logs <name>`, `/share <name>` | |
| `/login`, `/apitoken <token>`, `/logout`, `/account`, `/zones`, `/remote`, `/import <name>` | |
| `/engine`, `/install [version]` | |
| `/server [start\|stop] [port]`, `/protocol auto\|quic\|http2` | |
| `/radar`, `/tunnels`, `/cloud`, `/clear`, `/help` | |

## Notes and limits

- TryCloudflare quick tunnels are for testing. They have no uptime guarantee and get a new URL on every start.
- Android may still stop background work on aggressive OEM builds. Allow KSession to ignore battery
  optimisation (Cloud › Settings) for long-running tunnels.
- Origins on your LAN must be reachable from the phone. Use IP addresses, because `.local` names cannot be
  resolved by cloudflared on Android.

## Licenses

KSession by P Kawin Raju. Bundled cloudflared is © Cloudflare, Apache-2.0. Lucide icons are ISC
(`tools/lucide/LICENSE-lucide.txt`).
