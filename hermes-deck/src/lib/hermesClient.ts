import { HermesHttp } from '../native/plugins';
import { SseParser } from './sse';

export interface ChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

export interface ToolProgress {
  toolCallId: string;
  tool: string;
  emoji?: string;
  label?: string;
  status: 'running' | 'completed';
}

export interface StreamHandlers {
  onDelta(text: string): void;
  onReasoning?(text: string): void;
  onTool?(progress: ToolProgress): void;
}

export interface Endpoint {
  baseUrl: string;
  apiKey: string;
}

function headers(apiKey: string, extra: Record<string, string> = {}): Record<string, string> {
  return { Authorization: `Bearer ${apiKey}`, 'Content-Type': 'application/json', ...extra };
}

function errorText(status: number, body: string): string {
  try {
    const parsed = JSON.parse(body);
    const msg = parsed?.error?.message ?? parsed?.error ?? parsed?.detail;
    if (msg) return `HTTP ${status}: ${typeof msg === 'string' ? msg : JSON.stringify(msg)}`;
  } catch {
    // not JSON
  }
  if (status === 401) return 'HTTP 401: the API server key was rejected.';
  return `HTTP ${status}${body ? `: ${body.slice(0, 300)}` : ''}`;
}

export async function health(ep: Endpoint): Promise<boolean> {
  try {
    const res = await HermesHttp.request({ url: `${ep.baseUrl}/health`, timeoutMs: 6000 });
    return res.status === 200;
  } catch {
    return false;
  }
}

export async function listModels(ep: Endpoint): Promise<string[]> {
  const res = await HermesHttp.request({ url: `${ep.baseUrl}/v1/models`, headers: headers(ep.apiKey) });
  if (res.status !== 200) throw new Error(errorText(res.status, res.body));
  const parsed = JSON.parse(res.body) as { data?: { id: string }[] };
  return (parsed.data ?? []).map((m) => m.id);
}

let streamCounter = 0;

/**
 * Streams one Chat Completions turn. Text arrives via `onDelta`; Hermes' `hermes.tool.progress`
 * frames are surfaced through `onTool` so the UI can show what the agent is doing.
 */
export function streamChat(
  ep: Endpoint,
  messages: ChatMessage[],
  handlers: StreamHandlers,
  model = 'hermes-agent',
): { done: Promise<void>; cancel: () => void } {
  const streamId = `chat-${Date.now()}-${++streamCounter}`;
  let failure: string | null = null;
  const parser = new SseParser((e) => {
    if (e.data === '[DONE]') return;
    let payload: any;
    try {
      payload = JSON.parse(e.data);
    } catch {
      return;
    }
    if (e.event === 'hermes.tool.progress') {
      handlers.onTool?.(payload as ToolProgress);
      return;
    }
    const choice = payload?.choices?.[0];
    if (choice?.finish_reason === 'error' || payload?.error) {
      failure = payload?.error?.message ?? 'The agent reported an error.';
    }
    const delta = choice?.delta;
    if (delta?.reasoning_content) handlers.onReasoning?.(delta.reasoning_content);
    if (delta?.content) handlers.onDelta(delta.content);
  });

  const done = (async () => {
    const listener = await HermesHttp.addListener('chunk', (e) => {
      if (e.streamId === streamId) parser.push(e.data);
    });
    try {
      const res = await HermesHttp.stream({
        streamId,
        url: `${ep.baseUrl}/v1/chat/completions`,
        method: 'POST',
        headers: headers(ep.apiKey, { Accept: 'text/event-stream' }),
        body: JSON.stringify({ model, messages, stream: true }),
      });
      parser.end();
      if (res.status === 499) return;
      if (res.status >= 400) throw new Error(errorText(res.status, res.body));
      if (failure) throw new Error(failure);
    } finally {
      await listener.remove();
    }
  })();

  return { done, cancel: () => void HermesHttp.cancel({ streamId }) };
}
