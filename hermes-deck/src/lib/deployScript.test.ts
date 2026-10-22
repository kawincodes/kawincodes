import { describe, expect, it } from 'vitest';
import {
  asRoot,
  buildDockerDeployScript,
  manageCommand,
  parseContainerStatus,
  runStagedScriptCommand,
  type DockerDeployOptions,
} from './deployScript';
import { shq } from './shell';

export const sample: DockerDeployOptions = {
  providerEnv: 'OPENROUTER_API_KEY',
  providerKey: 'sk-or-v1-abc123',
  providerId: 'openrouter',
  model: 'anthropic/claude-sonnet-5.5',
  apiServerKey: 'a'.repeat(64),
  imageTag: 'latest',
  container: 'hermes',
  dataDir: '/opt/hermes-agent',
  hostPort: 8642,
};

describe('shq', () => {
  it('quotes single quotes safely', () => {
    expect(shq(`it's`)).toBe(`'it'\\''s'`);
  });
});

describe('buildDockerDeployScript', () => {
  it('binds the API only to the server loopback', () => {
    const s = buildDockerDeployScript(sample);
    expect(s).toContain(`-p "127.0.0.1:$HD_PORT:8642"`);
    expect(s).not.toMatch(/-p "?0\.0\.0\.0/);
  });

  it('puts secrets only inside the quoted heredoc', () => {
    const s = buildDockerDeployScript(sample);
    const heredoc = s.slice(s.indexOf("<<'HERMES_DECK_ENV'"), s.lastIndexOf('HERMES_DECK_ENV'));
    expect(heredoc).toContain(sample.providerKey);
    expect(s.split(sample.providerKey).length).toBe(2);
    expect(s.split(sample.apiServerKey).length).toBe(2);
  });

  it('rejects values that could break out of the env file or shell', () => {
    expect(() => buildDockerDeployScript({ ...sample, providerKey: 'abc\nEVIL=1' })).toThrow();
    expect(() => buildDockerDeployScript({ ...sample, providerKey: "abc'; rm -rf /" })).toThrow();
    expect(() => buildDockerDeployScript({ ...sample, model: 'x"; reboot; "' })).toThrow();
    expect(() => buildDockerDeployScript({ ...sample, container: 'a b' })).toThrow();
    expect(() => buildDockerDeployScript({ ...sample, apiServerKey: 'short' })).toThrow();
  });
});

describe('asRoot / runStagedScriptCommand', () => {
  it('sends the sudo password on stdin, never in the command', () => {
    const r = asRoot('docker ps', { kind: 'sudo', password: 'hunter2' });
    expect(r.command).not.toContain('hunter2');
    expect(r.stdin).toBe('hunter2\n');
    expect(r.command).toContain('exec </dev/null');
    const staged = runStagedScriptCommand('deploy.sh', { kind: 'sudo', password: 'hunter2' });
    expect(staged.command).not.toContain('hunter2');
    expect(staged.command).toContain('rm -f');
  });

  it('uses non-interactive sudo without a password and plain shell for root', () => {
    expect(asRoot('true', { kind: 'sudo' }).command).toMatch(/^sudo -n /);
    expect(asRoot('true', { kind: 'root' })).toEqual({ command: 'true' });
  });
});

describe('manage helpers', () => {
  it('parses docker inspect output', () => {
    expect(parseContainerStatus('running|2026-10-22T10:00:00Z|nousresearch/hermes-agent:latest|2\n')).toEqual({
      state: 'running',
      startedAt: '2026-10-22T10:00:00Z',
      image: 'nousresearch/hermes-agent:latest',
      restarts: 2,
    });
    expect(parseContainerStatus('missing|||').state).toBe('missing');
  });

  it('quotes the container name', () => {
    expect(manageCommand('logs', 'hermes')).toBe(`docker logs --tail 300 'hermes' 2>&1`);
    expect(() => manageCommand('logs', 'x;y')).toThrow();
  });
});
