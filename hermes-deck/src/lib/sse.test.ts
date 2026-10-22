import { describe, expect, it } from 'vitest';
import { SseParser, type SseEvent } from './sse';

function parse(chunks: string[]): SseEvent[] {
  const events: SseEvent[] = [];
  const p = new SseParser((e) => events.push(e));
  chunks.forEach((c) => p.push(c));
  p.end();
  return events;
}

describe('SseParser', () => {
  it('parses data frames split across arbitrary chunk boundaries', () => {
    const wire = 'data: {"a":1}\n\nevent: hermes.tool.progress\ndata: {"tool":"terminal"}\n\ndata: [DONE]\n\n';
    for (let size = 1; size <= wire.length; size++) {
      const chunks = wire.match(new RegExp(`[\\s\\S]{1,${size}}`, 'g'))!;
      expect(parse(chunks)).toEqual([
        { event: 'message', data: '{"a":1}' },
        { event: 'hermes.tool.progress', data: '{"tool":"terminal"}' },
        { event: 'message', data: '[DONE]' },
      ]);
    }
  });

  it('ignores keepalive comments and handles CRLF', () => {
    expect(parse([': keepalive\r\n\r\ndata: x\r', '\n\r\n'])).toEqual([{ event: 'message', data: 'x' }]);
  });

  it('joins multi-line data and flushes an unterminated final event', () => {
    expect(parse(['data: one\ndata: two\n\ndata: tail'])).toEqual([
      { event: 'message', data: 'one\ntwo' },
      { event: 'message', data: 'tail' },
    ]);
  });
});
