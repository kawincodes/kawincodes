import { beforeEach, describe, expect, it, vi } from 'vitest';

type ChunkListener = (e: { streamId: string; data: string }) => void;
const state: { frames: string[]; status: number; body: string; listeners: ChunkListener[]; lastRequest?: any } = {
  frames: [],
  status: 200,
  body: '',
  listeners: [],
};

vi.mock('../native/plugins', () => ({
  HermesHttp: {
    addListener: async (_event: string, fn: ChunkListener) => {
      state.listeners.push(fn);
      return { remove: async () => void (state.listeners = state.listeners.filter((l) => l !== fn)) };
    },
    stream: async (req: any) => {
      state.lastRequest = req;
      // Deliver frames in awkward slices, like a real socket would.
      const wire = state.frames.join('');
      for (let i = 0; i < wire.length; i += 7) state.listeners.forEach((l) => l({ streamId: req.streamId, data: wire.slice(i, i + 7) }));
      return { status: state.status, body: state.body };
    },
    cancel: async () => undefined,
  },
}));

const { streamChat } = await import('./hermesClient');

// Frame shapes copied from gateway/platforms/api_server_openai_routes.py (_write_sse_chat_completion).
const chunk = (delta: object, finish: string | null = null, extra = {}) =>
  `data: ${JSON.stringify({ id: 'c1', object: 'chat.completion.chunk', created: 1, model: 'hermes-agent', choices: [{ index: 0, delta, finish_reason: finish }], ...extra })}\n\n`;

describe('streamChat', () => {
  beforeEach(() => {
    state.frames = [];
    state.status = 200;
    state.body = '';
  });

  it('assembles text, reasoning and tool progress from a Hermes stream', async () => {
    state.frames = [
      chunk({ role: 'assistant' }),
      'event: hermes.tool.progress\ndata: {"tool":"terminal","emoji":"💻","label":"df -h","toolCallId":"t1","status":"running"}\n\n',
      ': keepalive\n\n',
      'event: hermes.tool.progress\ndata: {"tool":"terminal","toolCallId":"t1","status":"completed"}\n\n',
      chunk({ reasoning_content: 'checking disk' }),
      chunk({ content: 'Disk is ' }),
      chunk({ content: '31% full ✅' }),
      chunk({}, 'stop'),
      'data: [DONE]\n\n',
    ];
    let text = '';
    let reasoning = '';
    const tools: any[] = [];
    const { done } = streamChat({ baseUrl: 'http://127.0.0.1:1', apiKey: 'k'.repeat(16) }, [{ role: 'user', content: 'disk?' }], {
      onDelta: (t) => (text += t),
      onReasoning: (t) => (reasoning += t),
      onTool: (p) => tools.push(p),
    });
    await done;
    expect(text).toBe('Disk is 31% full ✅');
    expect(reasoning).toBe('checking disk');
    expect(tools.map((t) => `${t.toolCallId}:${t.status}`)).toEqual(['t1:running', 't1:completed']);
    expect(state.lastRequest.url).toBe('http://127.0.0.1:1/v1/chat/completions');
    expect(state.lastRequest.headers.Authorization).toBe(`Bearer ${'k'.repeat(16)}`);
    expect(JSON.parse(state.lastRequest.body)).toMatchObject({ model: 'hermes-agent', stream: true });
    expect(state.listeners).toHaveLength(0);
  });

  it('surfaces an in-stream error chunk (HTTP 200 already sent)', async () => {
    state.frames = [chunk({}, 'error', { error: { message: 'admission_conflict' } }), 'data: [DONE]\n\n'];
    await expect(streamChat({ baseUrl: 'x', apiKey: 'k' }, [], { onDelta: () => undefined }).done).rejects.toThrow('admission_conflict');
  });

  it('reports HTTP errors with the server message', async () => {
    state.status = 401;
    state.body = JSON.stringify({ error: { message: 'Invalid API key' } });
    await expect(streamChat({ baseUrl: 'x', apiKey: 'k' }, [], { onDelta: () => undefined }).done).rejects.toThrow('HTTP 401: Invalid API key');
  });
});
