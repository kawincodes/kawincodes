/**
 * Browser fallbacks used by `npm run dev` and UI tests. On Android the Java plugins replace all of
 * this. SSH and Termux are simulated (a browser cannot open SSH sockets); the simulated agent lives
 * at http://127.0.0.1:18642 so the real chat/SSE code path can be exercised end to end.
 */
import { WebPlugin } from '@capacitor/core';
import type {
  HermesHttpPlugin,
  HttpRequestOptions,
  SshConnectOptions,
  SshExecOptions,
  SshHostKey,
  SshPlugin,
  SshTarget,
  TermuxPlugin,
  TermuxRunResult,
  TermuxStatus,
  VaultPlugin,
} from './plugins';

export const PREVIEW_TUNNEL_PORT = 18642;
const PREVIEW_ORIGIN = `http://127.0.0.1:${PREVIEW_TUNNEL_PORT}`;
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export class SshWeb extends WebPlugin implements SshPlugin {
  private cancelled = new Set<string>();

  async probeHostKey(options: SshTarget): Promise<SshHostKey> {
    await sleep(400);
    if (!options.host) throw new Error('Host is required');
    return { type: 'ssh-ed25519', fingerprint: 'SHA256:preview0nly+NotARealKey/9f3kQ2bV1xY7cW4mZ8aL' };
  }

  async connect(options: SshConnectOptions): Promise<{ sessionId: string }> {
    await sleep(500);
    if (!options.password && !options.privateKey) throw new Error('Authentication failed: check the username, password or private key.');
    return { sessionId: `preview-${options.host}` };
  }

  async exec(options: SshExecOptions): Promise<{ exitCode: number }> {
    const out = (chunk: string, stream: 'stdout' | 'stderr' = 'stdout') =>
      this.notifyListeners('output', { execId: options.execId, stream, chunk });
    const cmd = options.command;
    if (cmd.includes('cat > ')) return { exitCode: 0 };
    if (cmd.includes('.hermes-deck/')) {
      const lines: [string, number][] = [
        ['::step::preflight\nOS: Ubuntu 24.04.3 LTS\nArchitecture: x86_64\n', 500],
        ['::step::docker\nDocker version 28.5.1, build e180ab8\n', 700],
        ['::step::config\nUpdated /opt/hermes-agent/.env (secrets are not printed).\n', 400],
        ['::step::pull\nlatest: Pulling from nousresearch/hermes-agent\nDigest: sha256:4be1…\nStatus: Downloaded newer image for nousresearch/hermes-agent:latest\n', 1200],
        ['::step::configure\nModel: openrouter / anthropic/claude-sonnet-5.5\n', 600],
        ['::step::start\nContainer hermes started; API server published on 127.0.0.1:8642 (reachable over SSH only).\n', 700],
        ['::step::health\nHermes API server is healthy.\n::ok::nousresearch/hermes-agent:latest\n', 900],
      ];
      for (const [text, delay] of lines) {
        await sleep(delay);
        if (this.cancelled.has(options.execId)) return { exitCode: 130 };
        out(text);
      }
      return { exitCode: 0 };
    }
    if (cmd.includes('docker inspect')) {
      out(`running|${new Date(Date.now() - 3_600_000).toISOString()}|nousresearch/hermes-agent:latest|0\n`);
      return { exitCode: 0 };
    }
    if (cmd.includes('docker logs')) {
      out('[API Server] API server listening on http://0.0.0.0:8642\n[gateway] platforms: api_server\n[gateway] ready\n');
      return { exitCode: 0 };
    }
    if (cmd.includes('hermes doctor')) {
      out('✓ Python 3.14\n✓ config.yaml\n✓ provider credentials\n✓ API server enabled\nAll checks passed.\n');
      return { exitCode: 0 };
    }
    if (cmd.startsWith('id -un')) {
      out('root\nx86_64\n/usr/bin/docker\n');
      return { exitCode: 0 };
    }
    await sleep(300);
    out('ok\n');
    return { exitCode: 0 };
  }

  async cancel(options: { execId: string }): Promise<void> {
    this.cancelled.add(options.execId);
  }

  async forward(): Promise<{ localPort: number }> {
    return { localPort: PREVIEW_TUNNEL_PORT };
  }

  async disconnect(): Promise<void> {}
}

export class HermesHttpWeb extends WebPlugin implements HermesHttpPlugin {
  private controllers = new Map<string, AbortController>();
  private cancelled = new Set<string>();

  async request(o: HttpRequestOptions): Promise<{ status: number; body: string }> {
    if (o.url.startsWith(PREVIEW_ORIGIN)) {
      if (o.url.endsWith('/health')) return { status: 200, body: '{"status":"ok"}' };
      if (o.url.endsWith('/v1/models')) return { status: 200, body: '{"data":[{"id":"hermes-agent"}]}' };
      return { status: 404, body: '' };
    }
    const res = await fetch(o.url, { method: o.method ?? 'GET', headers: o.headers, body: o.body });
    return { status: res.status, body: await res.text() };
  }

  async stream(o: HttpRequestOptions & { streamId: string }): Promise<{ status: number; body: string }> {
    const emit = (data: string) => this.notifyListeners('chunk', { streamId: o.streamId, data });
    if (o.url.startsWith(PREVIEW_ORIGIN)) {
      const frames = [
        'event: hermes.tool.progress\ndata: {"tool":"terminal","emoji":"💻","label":"uptime && df -h /","toolCallId":"t1","status":"running"}\n\n',
        'event: hermes.tool.progress\ndata: {"tool":"terminal","toolCallId":"t1","status":"completed"}\n\n',
        ...'I checked the server for you. It has been up for **12 days**, load is low (0.08), and the root disk is 31% full.\n\n```\n/dev/vda1  80G  24G  53G  31% /\n```\n\nAnything else you want me to look at?'
          .match(/.{1,12}/gs)!
          .map((t) => `data: ${JSON.stringify({ choices: [{ delta: { content: t } }] })}\n\n`),
        'data: [DONE]\n\n',
      ];
      for (const frame of frames) {
        await sleep(frame.includes('tool.progress') ? 700 : 35);
        if (this.cancelled.delete(o.streamId)) return { status: 499, body: '' };
        emit(frame);
      }
      return { status: 200, body: '' };
    }
    const controller = new AbortController();
    this.controllers.set(o.streamId, controller);
    try {
      const res = await fetch(o.url, { method: o.method ?? 'GET', headers: o.headers, body: o.body, signal: controller.signal });
      if (res.status >= 400 || !res.body) return { status: res.status, body: await res.text() };
      const reader = res.body.pipeThrough(new TextDecoderStream()).getReader();
      for (;;) {
        const { value, done } = await reader.read();
        if (done) break;
        emit(value);
      }
      return { status: res.status, body: '' };
    } catch (e) {
      if (controller.signal.aborted) return { status: 499, body: '' };
      throw e;
    } finally {
      this.controllers.delete(o.streamId);
    }
  }

  async cancel(options: { streamId: string }): Promise<void> {
    this.cancelled.add(options.streamId);
    this.controllers.get(options.streamId)?.abort();
  }
}

export class TermuxWeb extends WebPlugin implements TermuxPlugin {
  async status(): Promise<TermuxStatus> {
    return { installed: false, permissionGranted: false };
  }
  async requestPermission(): Promise<TermuxStatus> {
    return this.status();
  }
  async run(): Promise<TermuxRunResult> {
    throw this.unavailable('Termux is only available on Android.');
  }
  async runInTerminal(): Promise<void> {
    throw this.unavailable('Termux is only available on Android.');
  }
}

/** Preview only: browser storage is NOT encrypted. The Android build uses the Keystore-backed Java plugin. */
export class VaultWeb extends WebPlugin implements VaultPlugin {
  private prefix = 'hermes-deck-preview:';
  async get(o: { key: string }): Promise<{ value: string | null }> {
    try {
      return { value: localStorage.getItem(this.prefix + o.key) };
    } catch {
      return { value: null };
    }
  }
  async set(o: { key: string; value: string }): Promise<void> {
    try {
      localStorage.setItem(this.prefix + o.key, o.value);
    } catch {
      // storage unavailable (private mode): keep running without persistence
    }
  }
  async remove(o: { key: string }): Promise<void> {
    try {
      localStorage.removeItem(this.prefix + o.key);
    } catch {
      // ignore
    }
  }
}
