# KRS Chess Help

Android app (`com.krstech.chesshelp`, Kotlin) that watches a chess board on your screen, sends it to
Claude, and shows the recommended move in a floating overlay — with an arrow drawn over the real board.

After the first read it follows the game by itself: it notices when the board changes, works out which
move was played from the pixels, and only then asks Claude for the next recommendation.

> **Fair play.** This is for study, puzzles, analysis and games against bots. Using move assistance in
> live games against people breaks the rules of every chess site and can get your account closed.

## How it works

1. A foreground service captures the screen (Android MediaProjection — you grant this each session).
2. The board is found locally in the screenshot (no network), and each square is classified as empty,
   light piece or dark piece.
3. The cropped board image is sent to the Claude API. Claude reads the position; the app validates it
   (legal position, matches the pixels) and replies with the legal moves and a few lines from a small
   built-in engine; Claude then recommends a move, which the app checks is legal.
4. The move appears in the overlay panel (move, evaluation, short explanation, alternatives, mini board)
   and as an arrow over the board.
5. From then on, board changes are detected on-device. Your opponent's move is deduced without calling
   Claude; Claude is asked again only when it is your turn.

If Claude is unreachable or no API key is set, the built-in engine's move is shown, labelled "Local engine".

## Install

- **From CI:** every push builds a debug APK — open the repository's *Actions* tab, pick the latest
  "KRS Chess Help (Android)" run and download the `krs-chess-help-debug-apk` artifact.
- **From source:** JDK 17+ and the Android SDK (platform 36), then

  ```bash
  cd krs-chess-help
  ./gradlew :app:assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
  ```

The APK is debug-signed: allow "install unknown apps" for your file manager or browser to sideload it.
Requires Android 8.0 (API 26) or newer.

## Setup

1. Create an API key at <https://console.anthropic.com> and paste it into the app (it is stored encrypted
   with an Android Keystore key and never leaves the device except to call the API).
2. Grant **Display over other apps** and (Android 13+) notifications.
3. Tap **Start**, accept the screen-capture prompt (choose the entire screen), then open your chess app.
4. Tap the floating bubble to analyse the board.

### Settings

| Setting | What it does |
|---|---|
| Model | `claude-opus-5-5` (default), `claude-sonnet-5-5`, `claude-haiku-5-5`, `claude-fable-5-2` |
| Effort | How long Claude thinks: Low / Medium / High |
| Advise for | The side at the bottom of the screen (default), White, Black, or both |
| Auto-advise | Analyse automatically when the board changes |
| Draw arrow | Draw the move over the real board |
| Full-screen context | Also send a downscaled screenshot (helps Claude see clocks and whose turn it is) |
| Custom API base URL | For a proxy or gateway |

### Cost and privacy

Each analysis is a paid API call on your own key (an image read costs more than a follow-up move).
Only the board crop is sent, plus a downscaled screenshot if "Full-screen context" is on. Nothing is
sent anywhere else, and nothing is captured after you tap Stop.

## Project layout

| Module | Contents |
|---|---|
| `core` | Chess rules (bitboard move generator, FEN/SAN), position validation, move inference from square occupancy, alpha-beta engine |
| `vision` | Board location and per-square occupancy from raw pixels, change fingerprint |
| `claude` | Claude API client on the official Anthropic Java SDK: prompts, tools, the read-validate-recommend loop |
| `brain` | The analysis coordinator: capture → locate → read → track → advise |
| `capture` | MediaProjection screen capture and the images sent to Claude |
| `app` | Settings screen, foreground service, floating overlay |

`core`, `vision`, `claude` and `brain` are plain Kotlin and are tested on the JVM:

```bash
./gradlew test                       # all unit tests
./gradlew :app:lintDebug
```

`vision` has an optional test over real screenshots: set `KRS_VISION_FIXTURES` to a directory of PNGs
with a `truth.json` (not included in the repository because piece artwork is third-party licensed).

## Limits

- The board must be axis-aligned and two-toned. Heavily textured or decorative themes (stone, glass,
  neon, hand-drawn) can confuse the square reader; standard themes on lichess and chess.com work best.
- Analysis arrows drawn by the chess app itself can be mistaken for pieces — tap **Rescan** if the
  tracked position goes wrong.
- Castling rights and en-passant are inferred from the picture when the position is first read.
