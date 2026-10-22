import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { dropConnection } from './lib/agents';
import { clearChat, loadProfiles, saveProfiles, type AgentProfile } from './lib/store';

interface AgentsState {
  ready: boolean;
  profiles: AgentProfile[];
  upsert(profile: AgentProfile): Promise<void>;
  remove(id: string): Promise<void>;
}

const AgentsContext = createContext<AgentsState | null>(null);

export function AgentsProvider({ children }: { children: ReactNode }) {
  const [profiles, setProfiles] = useState<AgentProfile[]>([]);
  const [ready, setReady] = useState(false);
  // Source of truth for writes; React state updaters run later, so they can't feed saveProfiles.
  const current = useRef<AgentProfile[]>([]);

  useEffect(() => {
    loadProfiles()
      .then((loaded) => {
        current.current = loaded;
        setProfiles(loaded);
      })
      .finally(() => setReady(true));
  }, []);

  const commit = useCallback(async (next: AgentProfile[]) => {
    current.current = next;
    setProfiles(next);
    await saveProfiles(next);
  }, []);

  const upsert = useCallback(
    async (profile: AgentProfile) => {
      const prev = current.current;
      await commit(prev.some((p) => p.id === profile.id) ? prev.map((p) => (p.id === profile.id ? profile : p)) : [...prev, profile]);
    },
    [commit],
  );

  const remove = useCallback(
    async (id: string) => {
      await commit(current.current.filter((p) => p.id !== id));
      await clearChat(id);
      await dropConnection(id);
    },
    [commit],
  );

  const value = useMemo(() => ({ ready, profiles, upsert, remove }), [ready, profiles, upsert, remove]);
  return <AgentsContext.Provider value={value}>{children}</AgentsContext.Provider>;
}

export function useAgents(): AgentsState {
  const ctx = useContext(AgentsContext);
  if (!ctx) throw new Error('useAgents must be used inside AgentsProvider');
  return ctx;
}
