import { describe, expect, it } from 'vitest';
import { createMarkerParser, explainFailure } from './deployMarkers';

describe('createMarkerParser', () => {
  it('routes markers and plain lines across chunk splits', () => {
    const seen: string[] = [];
    const p = createMarkerParser({
      onStep: (s) => seen.push(`step:${s}`),
      onOk: (d) => seen.push(`ok:${d}`),
      onFail: (m) => seen.push(`fail:${m}`),
      onLine: (l) => seen.push(`line:${l}`),
    });
    p.push('::st');
    p.push('ep::docker\r\nDocker version 28\n::fa');
    p.push('il::boom\n::ok::img:latest');
    p.flush();
    expect(seen).toEqual(['step:docker', 'line:Docker version 28', 'fail:boom', 'ok:img:latest']);
  });
});

describe('explainFailure', () => {
  it('recognises sudo problems', () => {
    expect(explainFailure('sudo: a password is required')).toMatch(/sudo needs a password/);
    expect(explainFailure('all good')).toBeNull();
  });
});
