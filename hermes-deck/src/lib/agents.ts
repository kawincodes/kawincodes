import type { PluginListenerHandle } from '@capacitor/core';
import { Ssh, type SshOutputEvent } from '../native/plugins';
import { API_PORT, type Privilege } from './deployScript';
import { shq } from './shell';
import type { Endpoint } from './hermesClient';
import type { AgentProfile, SshConfig } from './store';

export function privilegeFor(ssh: SshConfig): Privilege {
  if (ssh.username === 'root') return { kind: 'root' };
  const password = ssh.sudoPassword || (ssh.auth === 'password' ? ssh.password : undefined);
  return { kind: 'sudo', password };
}

type OutputHandler = (stream: 'stdout' | 'stderr', chunk: string) => void;

const handlers = new Map<string, OutputHandler>();
let listener: Promise<PluginListenerHandle> | null = null;

function ensureListener(): Promise<PluginListenerHandle> {
  listener ??= Ssh.addListener('output', (e: SshOutputEvent) => handlers.get(e.execId)?.(e.stream, e.chunk));
  return listener;
}

let execCounter = 0;

export class SshConnection {
  private tunnelPort: number | null = null;
  private closed = false;

  private constructor(readonly sessionId: string) {}

  static async open(ssh: SshConfig): Promise<SshConnection> {
    await ensureListener();
    const { sessionId } = await Ssh.connect({
      host: ssh.host,
      port: ssh.port,
      username: ssh.username,
      password: ssh.auth === 'password' ? ssh.password : undefined,
      privateKey: ssh.auth === 'key' ? ssh.privateKey : undefined,
      passphrase: ssh.auth === 'key' ? ssh.passphrase : undefined,
      expectedFingerprint: ssh.hostKeyFingerprint,
    });
    return new SshConnection(sessionId);
  }

  get isClosed(): boolean {
    return this.closed;
  }

  /** Runs a command, streaming its output to `onOutput`. Resolves with the exit code. */
  exec(
    command: string,
    opts: { stdin?: string; onOutput?: OutputHandler; signal?: AbortSignal } = {},
  ): Promise<number> {
    const execId = `exec-${Date.now()}-${++execCounter}`;
    if (opts.onOutput) handlers.set(execId, opts.onOutput);
    const onAbort = () => void Ssh.cancel({ execId });
    opts.signal?.addEventListener('abort', onAbort);
    // Run through sh so POSIX syntax works whatever the user's login shell is (fish, csh, ...).
    return Ssh.exec({ sessionId: this.sessionId, execId, command: `sh -c ${shq(command)}`, stdin: opts.stdin })
      .then((r) => r.exitCode)
      .catch((e) => {
        if (String(e?.message ?? e).includes('not connected')) this.closed = true;
        throw e;
      })
      .finally(() => {
        handlers.delete(execId);
        opts.signal?.removeEventListener('abort', onAbort);
      });
  }

  /** Runs a command and collects its output. */
  async run(command: string, stdin?: string): Promise<{ code: number; out: string }> {
    let out = '';
    const code = await this.exec(command, { stdin, onOutput: (_s, c) => (out += c) });
    return { code, out };
  }

  async tunnel(remotePort = API_PORT): Promise<number> {
    if (this.tunnelPort === null) {
      const { localPort } = await Ssh.forward({ sessionId: this.sessionId, remotePort });
      this.tunnelPort = localPort;
    }
    return this.tunnelPort;
  }

  async close(): Promise<void> {
    this.closed = true;
    await Ssh.disconnect({ sessionId: this.sessionId });
  }
}

const connections = new Map<string, Promise<SshConnection>>();

/** One SSH session per agent, reopened transparently if it dropped. */
export async function connectionFor(profile: AgentProfile): Promise<SshConnection> {
  if (!profile.ssh) throw new Error('This agent is not managed over SSH.');
  const existing = connections.get(profile.id);
  if (existing) {
    try {
      const conn = await existing;
      if (!conn.isClosed) return conn;
    } catch {
      // fall through and reconnect
    }
  }
  const fresh = SshConnection.open(profile.ssh);
  connections.set(profile.id, fresh);
  fresh.catch(() => connections.delete(profile.id));
  return fresh;
}

export async function dropConnection(profileId: string): Promise<void> {
  const existing = connections.get(profileId);
  connections.delete(profileId);
  if (existing) {
    try {
      await (await existing).close();
    } catch {
      // already gone
    }
  }
}

/** Where the app reaches the agent's API server. SSH agents are only ever reached through a tunnel. */
export async function endpointFor(profile: AgentProfile): Promise<Endpoint> {
  switch (profile.kind) {
    case 'ssh': {
      const conn = await connectionFor(profile);
      const port = await conn.tunnel(profile.ssh?.hostPort ?? API_PORT);
      return { baseUrl: `http://127.0.0.1:${port}`, apiKey: profile.apiKey };
    }
    case 'termux':
      return { baseUrl: `http://127.0.0.1:${API_PORT}`, apiKey: profile.apiKey };
    case 'direct':
      return { baseUrl: (profile.baseUrl ?? '').replace(/\/+$/, ''), apiKey: profile.apiKey };
  }
}
