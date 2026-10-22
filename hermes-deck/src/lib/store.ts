import { Vault } from '../native/plugins';
import type { ChatMessage } from './hermesClient';

export type AgentKind = 'ssh' | 'termux' | 'direct';

export interface SshConfig {
  host: string;
  port: number;
  username: string;
  auth: 'password' | 'key';
  password?: string;
  privateKey?: string;
  passphrase?: string;
  /** Only needed when the user is not root and sudo asks for a password different from the login one. */
  sudoPassword?: string;
  hostKeyType: string;
  hostKeyFingerprint: string;
}

export interface AgentProfile {
  id: string;
  name: string;
  kind: AgentKind;
  /** API_SERVER_KEY shared with the deployed agent. */
  apiKey: string;
  providerId?: string;
  model?: string;
  createdAt: number;
  deployedAt?: number;
  ssh?: SshConfig & { container: string; dataDir: string; hostPort: number; imageTag: 'latest' | 'main' };
  /** For `direct` agents: an HTTPS URL (or http://127.0.0.1 on this phone). */
  baseUrl?: string;
}

const PROFILES_KEY = 'profiles.v1';
const chatKey = (id: string) => `chat.v1.${id}`;
const MAX_CHAT_MESSAGES = 200;

export async function loadProfiles(): Promise<AgentProfile[]> {
  const { value } = await Vault.get({ key: PROFILES_KEY });
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export async function saveProfiles(profiles: AgentProfile[]): Promise<void> {
  await Vault.set({ key: PROFILES_KEY, value: JSON.stringify(profiles) });
}

export async function loadChat(id: string): Promise<ChatMessage[]> {
  const { value } = await Vault.get({ key: chatKey(id) });
  if (!value) return [];
  try {
    return JSON.parse(value);
  } catch {
    return [];
  }
}

export async function saveChat(id: string, messages: ChatMessage[]): Promise<void> {
  await Vault.set({ key: chatKey(id), value: JSON.stringify(messages.slice(-MAX_CHAT_MESSAGES)) });
}

export async function clearChat(id: string): Promise<void> {
  await Vault.remove({ key: chatKey(id) });
}

export function newId(): string {
  return crypto.randomUUID();
}
