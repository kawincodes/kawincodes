import { useEffect, useState } from 'react';
import type { Health } from './components/StatusBadge';
import { dropConnection, endpointFor } from './lib/agents';
import { health } from './lib/hermesClient';
import type { AgentProfile } from './lib/store';

export async function checkHealth(profile: AgentProfile): Promise<Health> {
  try {
    if (await health(await endpointFor(profile))) return 'online';
  } catch {
    // reported as offline below
  }
  // A stale SSH session (server rebooted, network changed) would keep failing; reconnect next time.
  if (profile.kind === 'ssh') await dropConnection(profile.id);
  return 'offline';
}

/** Health of an agent, re-checked whenever `nonce` changes. */
export function useHealth(profile: AgentProfile | undefined, nonce = 0): Health {
  const [state, setState] = useState<Health>('checking');
  useEffect(() => {
    if (!profile) return;
    let alive = true;
    setState('checking');
    checkHealth(profile).then((h) => alive && setState(h));
    return () => {
      alive = false;
    };
  }, [profile, nonce]);
  return state;
}

export function errorMessage(e: unknown): string {
  if (e instanceof Error) return e.message;
  if (typeof e === 'object' && e && 'message' in e) return String((e as { message: unknown }).message);
  return String(e);
}
