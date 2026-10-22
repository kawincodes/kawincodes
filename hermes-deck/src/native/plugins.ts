import { registerPlugin, type PluginListenerHandle } from '@capacitor/core';

/* ---------- SSH (JSch) ---------- */

export interface SshTarget {
  host: string;
  port: number;
}

export interface SshHostKey {
  /** e.g. `ssh-ed25519` */
  type: string;
  /** OpenSSH style `SHA256:<base64>` */
  fingerprint: string;
}

export interface SshConnectOptions extends SshTarget {
  username: string;
  password?: string;
  privateKey?: string;
  passphrase?: string;
  /** Pinned fingerprint. The native side refuses the connection before authenticating if it differs. */
  expectedFingerprint: string;
}

export interface SshExecOptions {
  sessionId: string;
  execId: string;
  command: string;
  stdin?: string;
  timeoutMs?: number;
}

export interface SshOutputEvent {
  execId: string;
  stream: 'stdout' | 'stderr';
  chunk: string;
}

export interface SshPlugin {
  probeHostKey(options: SshTarget): Promise<SshHostKey>;
  connect(options: SshConnectOptions): Promise<{ sessionId: string }>;
  exec(options: SshExecOptions): Promise<{ exitCode: number }>;
  cancel(options: { execId: string }): Promise<void>;
  forward(options: { sessionId: string; remotePort: number }): Promise<{ localPort: number }>;
  disconnect(options: { sessionId: string }): Promise<void>;
  addListener(event: 'output', fn: (e: SshOutputEvent) => void): Promise<PluginListenerHandle>;
}

/* ---------- HTTP (native, so no CORS and cleartext only to loopback) ---------- */

export interface HttpRequestOptions {
  url: string;
  method?: string;
  headers?: Record<string, string>;
  body?: string;
  timeoutMs?: number;
}

export interface HttpStreamEvent {
  streamId: string;
  data: string;
}

export interface HermesHttpPlugin {
  request(options: HttpRequestOptions): Promise<{ status: number; body: string }>;
  /** Resolves once the body ends; body bytes arrive as `chunk` events while it is open. */
  stream(options: HttpRequestOptions & { streamId: string }): Promise<{ status: number; body: string }>;
  cancel(options: { streamId: string }): Promise<void>;
  addListener(event: 'chunk', fn: (e: HttpStreamEvent) => void): Promise<PluginListenerHandle>;
}

/* ---------- Termux (on-device Hermes) ---------- */

export interface TermuxStatus {
  installed: boolean;
  permissionGranted: boolean;
}

export interface TermuxRunResult {
  stdout: string;
  stderr: string;
  exitCode: number;
  errmsg: string;
}

export interface TermuxPlugin {
  status(): Promise<TermuxStatus>;
  requestPermission(): Promise<TermuxStatus>;
  /** Background command whose output comes back when it finishes. */
  run(options: { script: string; timeoutMs?: number }): Promise<TermuxRunResult>;
  /** Opens a visible Termux session running the script, so long installs show their progress. */
  runInTerminal(options: { script: string }): Promise<void>;
}

/* ---------- Vault (Android Keystore AES-GCM) ---------- */

export interface VaultPlugin {
  get(options: { key: string }): Promise<{ value: string | null }>;
  set(options: { key: string; value: string }): Promise<void>;
  remove(options: { key: string }): Promise<void>;
}

const web = () => import('./web');

export const Ssh = registerPlugin<SshPlugin>('Ssh', { web: () => web().then((m) => new m.SshWeb()) });
export const HermesHttp = registerPlugin<HermesHttpPlugin>('HermesHttp', {
  web: () => web().then((m) => new m.HermesHttpWeb()),
});
export const Termux = registerPlugin<TermuxPlugin>('Termux', { web: () => web().then((m) => new m.TermuxWeb()) });
export const Vault = registerPlugin<VaultPlugin>('Vault', { web: () => web().then((m) => new m.VaultWeb()) });
