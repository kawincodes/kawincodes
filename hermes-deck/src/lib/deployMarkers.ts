export interface MarkerHandlers {
  onStep(id: string): void;
  onOk(detail: string): void;
  onFail(message: string): void;
  onLine(line: string): void;
}

/**
 * Splits streamed script output into lines and routes the `::step::`, `::ok::` and `::fail::`
 * markers emitted by the generated deploy scripts; everything else is plain log output.
 */
export function createMarkerParser(h: MarkerHandlers): { push(chunk: string): void; flush(): void } {
  let pending = '';
  const handle = (raw: string) => {
    const line = raw.replace(/\r$/, '');
    if (line.startsWith('::step::')) h.onStep(line.slice(8).trim());
    else if (line.startsWith('::ok::')) h.onOk(line.slice(6).trim());
    else if (line.startsWith('::fail::')) h.onFail(line.slice(8).trim());
    else h.onLine(line);
  };
  return {
    push(chunk: string) {
      pending += chunk;
      const lines = pending.split('\n');
      pending = lines.pop() ?? '';
      lines.forEach(handle);
    },
    flush() {
      if (pending) handle(pending);
      pending = '';
    },
  };
}

/** Turns common sudo/SSH failures in stderr into an actionable message. */
export function explainFailure(log: string): string | null {
  if (/sudo: a password is required|sudo: no tty present/i.test(log)) {
    return 'sudo needs a password. Add the sudo password in the server settings, or log in as root.';
  }
  if (/incorrect password attempt|Sorry, try again/i.test(log)) return 'The sudo password was rejected.';
  if (/is not in the sudoers file|not allowed to execute/i.test(log)) return 'This user is not allowed to use sudo.';
  if (/Cannot connect to the Docker daemon/i.test(log)) return 'Docker is installed but its daemon is not running.';
  if (/no space left on device/i.test(log)) return 'The server ran out of disk space.';
  return null;
}
