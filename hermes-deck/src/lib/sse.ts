export interface SseEvent {
  event: string;
  data: string;
}

/**
 * Incremental Server-Sent Events parser. Feed it arbitrary text chunks; it emits complete events.
 * Comment lines (`: keepalive`, which Hermes sends every 10s) are ignored.
 */
export class SseParser {
  private buffer = '';
  private event = '';
  private data: string[] = [];

  constructor(private readonly onEvent: (e: SseEvent) => void) {}

  push(chunk: string): void {
    this.buffer += chunk;
    let idx: number;
    while ((idx = this.buffer.search(/\r\n|\n|\r/)) !== -1) {
      const line = this.buffer.slice(0, idx);
      const sepLen = this.buffer.startsWith('\r\n', idx) ? 2 : 1;
      // A lone trailing '\r' may be the first half of '\r\n': wait for more input.
      if (sepLen === 1 && this.buffer[idx] === '\r' && idx === this.buffer.length - 1) break;
      this.buffer = this.buffer.slice(idx + sepLen);
      this.line(line);
    }
  }

  /** Flush a final event that was not followed by a blank line. */
  end(): void {
    if (this.buffer) this.line(this.buffer);
    this.buffer = '';
    this.dispatch();
  }

  private line(line: string): void {
    if (line === '') {
      this.dispatch();
      return;
    }
    if (line.startsWith(':')) return;
    const colon = line.indexOf(':');
    const field = colon === -1 ? line : line.slice(0, colon);
    let value = colon === -1 ? '' : line.slice(colon + 1);
    if (value.startsWith(' ')) value = value.slice(1);
    if (field === 'event') this.event = value;
    else if (field === 'data') this.data.push(value);
  }

  private dispatch(): void {
    if (this.data.length > 0) {
      this.onEvent({ event: this.event || 'message', data: this.data.join('\n') });
    }
    this.event = '';
    this.data = [];
  }
}
