/**
 * Executes the generated deploy script in real bash with fake `docker`/`curl` binaries, to check
 * the parts unit tests can't: .env merging and permissions, progress markers, and that no secret
 * reaches the log.
 */
import { execFileSync, spawnSync } from 'node:child_process';
import { chmodSync, mkdtempSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import { buildDockerDeployScript, buildDockerUpdateScript } from './deployScript';
import { buildTermuxInstallScript } from './termuxScript';
import { sample } from './deployScript.test';

const hasBash = spawnSync('bash', ['--version']).status === 0;

function sandbox() {
  const dir = mkdtempSync(join(tmpdir(), 'hermes-deck-'));
  const bin = join(dir, 'bin');
  execFileSync('mkdir', ['-p', bin]);
  const log = join(dir, 'docker.log');
  const shim = (name: string, body: string) => {
    writeFileSync(join(bin, name), `#!/usr/bin/env bash\n${body}\n`);
    chmodSync(join(bin, name), 0o755);
  };
  shim(
    'docker',
    `echo "$*" >> ${JSON.stringify(log)}
case "$1" in
  --version) echo "Docker version 28.5.1" ;;
  inspect) case "$*" in *State.Running*) echo true ;; *) echo nousresearch/hermes-agent:latest ;; esac ;;
esac
exit 0`,
  );
  shim('curl', `case "$*" in *get.docker.com*) exit 22 ;; *) echo '{"status":"ok"}' ;; esac`);
  shim('systemctl', 'exit 0');
  // The script insists on root; pretend so the test also runs on unprivileged CI runners.
  shim('id', 'if [ "$1" = "-u" ]; then echo 0; else exec /usr/bin/id "$@"; fi');
  shim('sleep', 'exit 0');
  return { dir, bin, log, dataDir: join(dir, 'data') };
}

function run(script: string, bin: string) {
  // Same as the app: the script is staged to a file and executed from there.
  const file = join(bin, '..', `script-${Math.random().toString(36).slice(2)}.sh`);
  writeFileSync(file, script, { mode: 0o700 });
  return spawnSync('bash', [file], { encoding: 'utf8', env: { ...process.env, PATH: `${bin}:${process.env.PATH}` } });
}

describe.runIf(hasBash)('generated deploy script (bash)', () => {
  it('deploys, merges .env and never prints secrets', () => {
    const sb = sandbox();
    execFileSync('mkdir', ['-p', sb.dataDir]);
    writeFileSync(join(sb.dataDir, '.env'), 'TELEGRAM_BOT_TOKEN=keep-me\nAPI_SERVER_KEY=old\n');
    const opts = { ...sample, dataDir: sb.dataDir };

    const first = run(buildDockerDeployScript(opts), sb.bin);
    expect(first.status, first.stdout + first.stderr).toBe(0);
    const steps = [...first.stdout.matchAll(/::step::(\w+)/g)].map((m) => m[1]);
    expect(steps).toEqual(['preflight', 'docker', 'config', 'pull', 'configure', 'start', 'health']);
    expect(first.stdout).toContain('::ok::nousresearch/hermes-agent:latest');
    for (const secret of [opts.providerKey, opts.apiServerKey]) {
      expect(first.stdout + first.stderr).not.toContain(secret);
    }

    // Re-running must not duplicate managed keys and must keep the user's own settings.
    expect(run(buildDockerDeployScript(opts), sb.bin).status).toBe(0);
    const env = readFileSync(join(sb.dataDir, '.env'), 'utf8');
    expect(env).toContain('TELEGRAM_BOT_TOKEN=keep-me');
    expect(env.match(/^API_SERVER_KEY=/gm)).toHaveLength(1);
    expect(env).toContain(`API_SERVER_KEY=${opts.apiServerKey}`);
    expect(env).toContain(`OPENROUTER_API_KEY=${opts.providerKey}`);
    expect(statSync(join(sb.dataDir, '.env')).mode & 0o777).toBe(0o600);

    const docker = readFileSync(sb.log, 'utf8');
    expect(docker).toContain(`run -d --name hermes --restart unless-stopped -v ${sb.dataDir}:/opt/data -p 127.0.0.1:8642:8642 nousresearch/hermes-agent:latest gateway run`);
    expect(docker).toContain('config set model.default anthropic/claude-sonnet-5.5');
    expect(docker).toContain('config set model.provider openrouter');
  });

  it('reports a clean failure marker when the container dies', () => {
    const sb = sandbox();
    writeFileSync(join(sb.bin, 'docker'), `#!/usr/bin/env bash\ncase "$1" in inspect) echo false ;; logs) echo 'boom: provider key rejected' ;; esac\nexit 0\n`);
    writeFileSync(join(sb.bin, 'curl'), '#!/usr/bin/env bash\nexit 7\n');
    const r = run(buildDockerUpdateScript({ ...sample, dataDir: sb.dataDir }), sb.bin);
    expect(r.status).toBe(1);
    expect(r.stdout).toContain('::fail::The container stopped during startup');
    expect(r.stdout).toContain('boom: provider key rejected');
  });

  it('generates syntactically valid Termux scripts', () => {
    const script = buildTermuxInstallScript({
      providerEnv: 'ANTHROPIC_API_KEY',
      providerKey: 'sk-ant-xyz',
      providerId: 'anthropic',
      model: 'claude-sonnet-5',
      apiServerKey: 'b'.repeat(64),
    });
    const r = spawnSync('bash', ['-n'], { input: script, encoding: 'utf8' });
    expect(r.status, r.stderr).toBe(0);
  });
});

const hasShellcheck = spawnSync('shellcheck', ['--version']).status === 0;

describe.runIf(hasShellcheck)('generated scripts pass shellcheck', () => {
  const termuxOpts = { providerEnv: 'GOOGLE_API_KEY', providerKey: 'AIza-x', providerId: 'gemini', model: 'gemini-3.8-flash', apiServerKey: 'c'.repeat(64) };
  const cases: [string, string][] = [
    ['docker deploy', buildDockerDeployScript(sample)],
    ['docker update', buildDockerUpdateScript(sample)],
    ['termux install', `#!/usr/bin/env bash\n${buildTermuxInstallScript(termuxOpts)}`],
  ];
  it.each(cases)('%s', (_name, script) => {
    // SC1091: /etc/os-release is not available to follow statically.
    const r = spawnSync('shellcheck', ['-s', 'bash', '-S', 'warning', '-e', 'SC1091', '-'], { input: script, encoding: 'utf8' });
    expect(r.status, r.stdout).toBe(0);
  });
});
