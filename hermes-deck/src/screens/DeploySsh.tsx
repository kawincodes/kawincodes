import {
  CheckCircledIcon,
  ChatBubbleIcon,
  CrossCircledIcon,
  ExclamationTriangleIcon,
  GearIcon,
  LockClosedIcon,
  ReloadIcon,
  RocketIcon,
} from '@radix-ui/react-icons';
import {
  AlertDialog,
  Badge,
  Box,
  Button,
  Callout,
  Card,
  Code,
  DataList,
  Flex,
  Heading,
  Progress,
  SegmentedControl,
  Select,
  Separator,
  Spinner,
  Switch,
  Text,
  TextArea,
  TextField,
} from '@radix-ui/themes';
import { useMemo, useRef, useState } from 'react';
import { AppShell } from '../components/AppShell';
import { Field } from '../components/Field';
import { LogView } from '../components/LogView';
import { defaultModelChoice, ModelFields, type ModelChoice } from '../components/ModelFields';
import { SecretField } from '../components/SecretField';
import { errorMessage } from '../hooks';
import { privilegeFor, SshConnection } from '../lib/agents';
import { createMarkerParser, explainFailure } from '../lib/deployMarkers';
import {
  API_PORT,
  buildDockerDeployScript,
  DEPLOY_STEPS,
  runStagedScriptCommand,
  stageScriptCommand,
  type DeployStepId,
} from '../lib/deployScript';
import { providerById } from '../lib/providers';
import { randomToken } from '../lib/shell';
import { newId, type AgentProfile, type SshConfig } from '../lib/store';
import { Ssh, type SshHostKey } from '../native/plugins';
import { navigate } from '../router';
import { useAgents } from '../state';

type Stage = 'server' | 'model' | 'deploy';
type StepState = 'pending' | 'running' | 'done' | 'failed';

interface ServerForm {
  name: string;
  host: string;
  port: string;
  username: string;
  auth: 'password' | 'key';
  password: string;
  privateKey: string;
  passphrase: string;
  sudoPassword: string;
}

interface Advanced {
  container: string;
  dataDir: string;
  hostPort: string;
  imageTag: 'latest' | 'main';
}

interface LoginInfo {
  user: string;
  arch: string;
  docker: string | null;
}

const MAX_LOG = 200_000;

export function DeploySsh() {
  const { upsert } = useAgents();
  const profileId = useMemo(newId, []);
  const apiServerKey = useMemo(() => randomToken(32), []);

  const [stage, setStage] = useState<Stage>('server');
  const [form, setForm] = useState<ServerForm>({
    name: '',
    host: '',
    port: '22',
    username: 'root',
    auth: 'password',
    password: '',
    privateKey: '',
    passphrase: '',
    sudoPassword: '',
  });
  const [trusted, setTrusted] = useState<SshHostKey | null>(null);
  const [pendingKey, setPendingKey] = useState<SshHostKey | null>(null);
  const [busy, setBusy] = useState<'probe' | 'login' | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);
  const [login, setLogin] = useState<LoginInfo | null>(null);

  const [model, setModel] = useState<ModelChoice>(defaultModelChoice);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [advanced, setAdvanced] = useState<Advanced>({ container: 'hermes', dataDir: '/opt/hermes-agent', hostPort: String(API_PORT), imageTag: 'latest' });

  const [steps, setSteps] = useState<Record<string, StepState>>({});
  const [log, setLog] = useState('');
  const [runState, setRunState] = useState<'idle' | 'running' | 'ok' | 'failed'>('idle');
  const [runError, setRunError] = useState<string | null>(null);
  const abort = useRef<AbortController | null>(null);

  const set = (patch: Partial<ServerForm>) => {
    // Changing where we connect invalidates the pinned key and the login check.
    if (patch.host !== undefined || patch.port !== undefined) {
      setTrusted(null);
      setLogin(null);
    }
    if (patch.username !== undefined || patch.password !== undefined || patch.privateKey !== undefined || patch.auth !== undefined) {
      setLogin(null);
    }
    setForm((f) => ({ ...f, ...patch }));
  };

  const port = Number(form.port);
  const serverValid =
    form.host.trim() !== '' &&
    Number.isInteger(port) &&
    port > 0 &&
    port < 65536 &&
    form.username.trim() !== '' &&
    (form.auth === 'password' ? form.password !== '' : form.privateKey.includes('PRIVATE KEY'));

  const sshConfig = (key: SshHostKey): SshConfig => ({
    host: form.host.trim(),
    port,
    username: form.username.trim(),
    auth: form.auth,
    password: form.auth === 'password' ? form.password : undefined,
    privateKey: form.auth === 'key' ? form.privateKey : undefined,
    passphrase: form.auth === 'key' && form.passphrase ? form.passphrase : undefined,
    sudoPassword: form.sudoPassword || undefined,
    hostKeyType: key.type,
    hostKeyFingerprint: key.fingerprint,
  });

  async function probe() {
    setServerError(null);
    setBusy('probe');
    try {
      setPendingKey(await Ssh.probeHostKey({ host: form.host.trim(), port }));
    } catch (e) {
      setServerError(errorMessage(e));
    } finally {
      setBusy(null);
    }
  }

  async function testLogin(key: SshHostKey) {
    setServerError(null);
    setBusy('login');
    let conn: SshConnection | null = null;
    try {
      conn = await SshConnection.open(sshConfig(key));
      const { out } = await conn.run(`id -un; uname -m; command -v docker || echo -`);
      const [user = '?', arch = '?', docker = '-'] = out.trim().split('\n');
      setLogin({ user, arch, docker: docker === '-' ? null : docker });
    } catch (e) {
      setServerError(errorMessage(e));
    } finally {
      setBusy(null);
      await conn?.close().catch(() => undefined);
    }
  }

  async function deploy() {
    if (!trusted) return;
    const provider = providerById(model.providerId);
    const ssh = sshConfig(trusted);
    const opts = {
      providerEnv: provider.envVar,
      providerKey: model.apiKey,
      providerId: provider.id,
      model: model.model,
      apiServerKey,
      imageTag: advanced.imageTag,
      container: advanced.container.trim(),
      dataDir: advanced.dataDir.trim(),
      hostPort: Number(advanced.hostPort),
    };
    let script: string;
    try {
      script = buildDockerDeployScript(opts);
    } catch (e) {
      setRunError(errorMessage(e));
      setRunState('failed');
      return;
    }

    setRunState('running');
    setRunError(null);
    setLog('');
    setSteps(Object.fromEntries(DEPLOY_STEPS.map((s) => [s.id, 'pending'])));
    abort.current = new AbortController();

    let current: DeployStepId | null = null;
    let ok = false;
    let failMessage: string | null = null;
    let fullLog = '';
    const append = (line: string) => {
      fullLog += `${line}\n`;
      setLog((l) => (l.length > MAX_LOG ? l.slice(-MAX_LOG / 2) : l) + `${line}\n`);
    };
    // State updaters run later, so each one gets the step ids it needs captured now.
    const parser = createMarkerParser({
      onStep: (id) => {
        const prev = current;
        current = id as DeployStepId;
        setSteps((s) => ({ ...s, ...(prev ? { [prev]: 'done' } : {}), [id]: 'running' }));
        append(`\n▶ ${DEPLOY_STEPS.find((s) => s.id === id)?.label ?? id}`);
      },
      onOk: () => {
        ok = true;
        const prev = current;
        if (prev) setSteps((s) => ({ ...s, [prev]: 'done' }));
      },
      onFail: (msg) => {
        failMessage = msg;
        const prev = current;
        if (prev) setSteps((s) => ({ ...s, [prev]: 'failed' }));
      },
      onLine: append,
    });

    let conn: SshConnection | null = null;
    try {
      append(`Connecting to ${ssh.username}@${ssh.host}:${ssh.port} (host key ${ssh.hostKeyFingerprint})`);
      conn = await SshConnection.open(ssh);
      const name = `deploy-${Date.now()}.sh`;
      const staged = await conn.exec(stageScriptCommand(name), { stdin: script });
      if (staged !== 0) throw new Error('Could not upload the deploy script to the server.');
      const { command, stdin } = runStagedScriptCommand(name, privilegeFor(ssh));
      const code = await conn.exec(command, {
        stdin,
        signal: abort.current.signal,
        onOutput: (_stream, chunk) => parser.push(chunk),
      });
      parser.flush();
      if (abort.current.signal.aborted) throw new Error('Deployment cancelled.');
      if (code !== 0 || !ok) {
        throw new Error(explainFailure(fullLog) ?? failMessage ?? `The deploy script exited with code ${code}.`);
      }
      const profile: AgentProfile = {
        id: profileId,
        name: form.name.trim() || form.host.trim(),
        kind: 'ssh',
        apiKey: apiServerKey,
        providerId: provider.id,
        model: model.model,
        createdAt: Date.now(),
        deployedAt: Date.now(),
        ssh: { ...ssh, container: opts.container, dataDir: opts.dataDir, hostPort: opts.hostPort, imageTag: opts.imageTag },
      };
      await upsert(profile);
      setRunState('ok');
    } catch (e) {
      const msg = errorMessage(e);
      append(`\n✖ ${msg}`);
      setRunError(msg);
      setRunState('failed');
      const failed = current as DeployStepId | null;
      if (failed) setSteps((s) => ({ ...s, [failed]: s[failed] === 'done' ? 'done' : 'failed' }));
    } finally {
      await conn?.close().catch(() => undefined);
    }
  }

  const doneCount = Object.values(steps).filter((s) => s === 'done').length;
  const modelValid = model.apiKey.length > 0 && model.model.length > 0;

  return (
    <AppShell back title="Deploy to a server" subtitle="SSH · Docker · nousresearch/hermes-agent">
      <Stepper stage={stage} />

      {stage === 'server' && (
        <Flex direction="column" gap="4" mt="4">
          <Field label="Name" hint="Shown in the app only.">
            <TextField.Root size="3" value={form.name} placeholder="My VPS" onChange={(e) => set({ name: e.target.value })} />
          </Field>
          <Flex gap="3">
            <Box flexGrow="1">
              <Field label="Host">
                <TextField.Root
                  size="3"
                  value={form.host}
                  placeholder="203.0.113.10 or vps.example.com"
                  autoCapitalize="off"
                  autoCorrect="off"
                  spellCheck={false}
                  inputMode="url"
                  onChange={(e) => set({ host: e.target.value })}
                />
              </Field>
            </Box>
            <Box width="88px">
              <Field label="Port">
                <TextField.Root size="3" value={form.port} inputMode="numeric" onChange={(e) => set({ port: e.target.value.replace(/\D/g, '') })} />
              </Field>
            </Box>
          </Flex>
          <Field label="Username" hint={form.username.trim() !== 'root' ? 'Non-root users need sudo; the app runs the installer through it.' : undefined}>
            <TextField.Root size="3" value={form.username} autoCapitalize="off" autoCorrect="off" onChange={(e) => set({ username: e.target.value })} />
          </Field>
          <SegmentedControl.Root size="3" value={form.auth} onValueChange={(v) => set({ auth: v as ServerForm['auth'] })}>
            <SegmentedControl.Item value="password">Password</SegmentedControl.Item>
            <SegmentedControl.Item value="key">Private key</SegmentedControl.Item>
          </SegmentedControl.Root>
          {form.auth === 'password' ? (
            <Field label="Password">
              <SecretField value={form.password} onChange={(password) => set({ password })} autoComplete="current-password" />
            </Field>
          ) : (
            <>
              <Field label="Private key" hint="OpenSSH or PEM format (Ed25519, ECDSA or RSA). Stored encrypted on this device.">
                <TextArea
                  size="3"
                  rows={5}
                  className="mono"
                  value={form.privateKey}
                  placeholder="-----BEGIN OPENSSH PRIVATE KEY-----"
                  spellCheck={false}
                  autoCapitalize="off"
                  onChange={(e) => set({ privateKey: e.target.value })}
                />
              </Field>
              <Field label="Key passphrase (optional)">
                <SecretField value={form.passphrase} onChange={(passphrase) => set({ passphrase })} />
              </Field>
            </>
          )}
          {form.username.trim() !== 'root' && (
            <Field label="Sudo password (optional)" hint={form.auth === 'password' ? 'Leave empty to reuse the login password.' : 'Leave empty if sudo is passwordless.'}>
              <SecretField value={form.sudoPassword} onChange={(sudoPassword) => set({ sudoPassword })} />
            </Field>
          )}

          {serverError && (
            <Callout.Root color="red">
              <Callout.Icon>
                <ExclamationTriangleIcon />
              </Callout.Icon>
              <Callout.Text>{serverError}</Callout.Text>
            </Callout.Root>
          )}

          {trusted && (
            <Card>
              <Flex direction="column" gap="2">
                <Flex align="center" gap="2">
                  <LockClosedIcon />
                  <Text size="2" weight="medium">
                    Host key pinned
                  </Text>
                  <Badge color="green">{trusted.type}</Badge>
                </Flex>
                <Code size="1" className="wrap">
                  {trusted.fingerprint}
                </Code>
                {login && (
                  <DataList.Root size="2" mt="2">
                    <DataList.Item>
                      <DataList.Label>Logged in as</DataList.Label>
                      <DataList.Value>{login.user}</DataList.Value>
                    </DataList.Item>
                    <DataList.Item>
                      <DataList.Label>Architecture</DataList.Label>
                      <DataList.Value>
                        {login.arch}
                        {!/^(x86_64|amd64|aarch64|arm64)$/.test(login.arch) && (
                          <Badge color="red" ml="2">
                            unsupported
                          </Badge>
                        )}
                      </DataList.Value>
                    </DataList.Item>
                    <DataList.Item>
                      <DataList.Label>Docker</DataList.Label>
                      <DataList.Value>{login.docker ? <Badge color="green">installed</Badge> : <Badge color="amber">will be installed</Badge>}</DataList.Value>
                    </DataList.Item>
                  </DataList.Root>
                )}
              </Flex>
            </Card>
          )}

          {!trusted ? (
            <Button size="4" disabled={!serverValid || busy !== null} onClick={probe}>
              {busy === 'probe' ? <Spinner /> : <LockClosedIcon />} Verify server
            </Button>
          ) : !login ? (
            <Button size="4" disabled={!serverValid || busy !== null} onClick={() => testLogin(trusted)}>
              {busy === 'login' ? <Spinner /> : <ReloadIcon />} Test login
            </Button>
          ) : (
            <Button size="4" onClick={() => setStage('model')}>
              Continue
            </Button>
          )}

          <AlertDialog.Root open={pendingKey !== null} onOpenChange={(open) => !open && setPendingKey(null)}>
            <AlertDialog.Content maxWidth="420px">
              <AlertDialog.Title>Verify the server's identity</AlertDialog.Title>
              <AlertDialog.Description size="2">
                First connection to <strong>{form.host}</strong>. Check that this fingerprint matches the server, for example with{' '}
                <Code size="1">ssh-keygen -lf /etc/ssh/ssh_host_{pendingKey?.type.replace('ssh-', '').replace(/-.*/, '')}_key.pub</Code>
              </AlertDialog.Description>
              <Card mt="3">
                <Text as="div" size="1" color="gray">
                  {pendingKey?.type}
                </Text>
                <Code size="2" className="wrap">
                  {pendingKey?.fingerprint}
                </Code>
              </Card>
              <Flex gap="3" mt="4" justify="end">
                <AlertDialog.Cancel>
                  <Button variant="soft" color="gray">
                    Cancel
                  </Button>
                </AlertDialog.Cancel>
                <AlertDialog.Action>
                  <Button
                    onClick={() => {
                      const key = pendingKey!;
                      setTrusted(key);
                      setPendingKey(null);
                      void testLogin(key);
                    }}
                  >
                    Trust and log in
                  </Button>
                </AlertDialog.Action>
              </Flex>
            </AlertDialog.Content>
          </AlertDialog.Root>
        </Flex>
      )}

      {stage === 'model' && (
        <Flex direction="column" gap="4" mt="4">
          <ModelFields value={model} onChange={setModel} />
          <Separator size="4" />
          <Text as="label" size="2">
            <Flex gap="2" align="center">
              <Switch checked={showAdvanced} onCheckedChange={setShowAdvanced} /> Advanced settings
            </Flex>
          </Text>
          {showAdvanced && (
            <Flex direction="column" gap="3">
              <Field label="Container name">
                <TextField.Root value={advanced.container} onChange={(e) => setAdvanced({ ...advanced, container: e.target.value })} />
              </Field>
              <Field label="Data directory on the server" hint="Holds config, sessions, memories and skills. Kept across updates.">
                <TextField.Root className="mono" value={advanced.dataDir} onChange={(e) => setAdvanced({ ...advanced, dataDir: e.target.value })} />
              </Field>
              <Field label="API port on server loopback">
                <TextField.Root value={advanced.hostPort} inputMode="numeric" onChange={(e) => setAdvanced({ ...advanced, hostPort: e.target.value.replace(/\D/g, '') })} />
              </Field>
              <Field label="Image channel">
                <Select.Root value={advanced.imageTag} onValueChange={(v) => setAdvanced({ ...advanced, imageTag: v as Advanced['imageTag'] })}>
                  <Select.Trigger />
                  <Select.Content position="popper">
                    <Select.Item value="latest">Stable (latest)</Select.Item>
                    <Select.Item value="main">Development (main)</Select.Item>
                  </Select.Content>
                </Select.Root>
              </Field>
            </Flex>
          )}
          <Flex gap="3">
            <Button size="4" variant="soft" color="gray" onClick={() => setStage('server')}>
              Back
            </Button>
            <Button size="4" style={{ flex: 1 }} disabled={!modelValid} onClick={() => setStage('deploy')}>
              Review
            </Button>
          </Flex>
        </Flex>
      )}

      {stage === 'deploy' && trusted && (
        <Flex direction="column" gap="4" mt="4">
          {runState === 'idle' && (
            <>
              <Card size="2">
                <DataList.Root size="2">
                  <DataList.Item>
                    <DataList.Label>Server</DataList.Label>
                    <DataList.Value>
                      {form.username}@{form.host}:{form.port}
                    </DataList.Value>
                  </DataList.Item>
                  <DataList.Item>
                    <DataList.Label>Model</DataList.Label>
                    <DataList.Value>
                      {providerById(model.providerId).label} · {model.model}
                    </DataList.Value>
                  </DataList.Item>
                  <DataList.Item>
                    <DataList.Label>Image</DataList.Label>
                    <DataList.Value>
                      <Code>nousresearch/hermes-agent:{advanced.imageTag}</Code>
                    </DataList.Value>
                  </DataList.Item>
                  <DataList.Item>
                    <DataList.Label>Data</DataList.Label>
                    <DataList.Value>
                      <Code>{advanced.dataDir}</Code>
                    </DataList.Value>
                  </DataList.Item>
                  <DataList.Item>
                    <DataList.Label>API server</DataList.Label>
                    <DataList.Value>127.0.0.1:{advanced.hostPort} on the server, reached through SSH</DataList.Value>
                  </DataList.Item>
                </DataList.Root>
              </Card>
              <Callout.Root color="gray" variant="surface">
                <Callout.Icon>
                  <LockClosedIcon />
                </Callout.Icon>
                <Callout.Text>
                  The API port is never opened to the internet. Keys are written to the server's <Code>.env</Code> (mode 600) and are not
                  printed in the log.
                </Callout.Text>
              </Callout.Root>
              <Flex gap="3">
                <Button size="4" variant="soft" color="gray" onClick={() => setStage('model')}>
                  Back
                </Button>
                <Button size="4" style={{ flex: 1 }} onClick={deploy}>
                  <RocketIcon /> Deploy Hermes
                </Button>
              </Flex>
            </>
          )}

          {runState !== 'idle' && (
            <>
              <Card size="2">
                <Flex direction="column" gap="3">
                  <Flex justify="between" align="center">
                    <Heading size="3">{runState === 'running' ? 'Deploying…' : runState === 'ok' ? 'Hermes is live' : 'Deployment failed'}</Heading>
                    <Text size="2" color="gray">
                      {doneCount}/{DEPLOY_STEPS.length}
                    </Text>
                  </Flex>
                  <Progress value={(doneCount / DEPLOY_STEPS.length) * 100} color={runState === 'failed' ? 'red' : undefined} />
                  <Flex direction="column" gap="2">
                    {DEPLOY_STEPS.map((s) => (
                      <StepRow key={s.id} label={s.label} state={steps[s.id] ?? 'pending'} />
                    ))}
                  </Flex>
                </Flex>
              </Card>
              <LogView text={log} placeholder="Waiting for the server…" />
            </>
          )}

          {runState === 'running' && (
            <Button size="3" variant="soft" color="red" onClick={() => abort.current?.abort()}>
              Cancel
            </Button>
          )}

          {runState === 'failed' && (
            <>
              <Callout.Root color="red">
                <Callout.Icon>
                  <ExclamationTriangleIcon />
                </Callout.Icon>
                <Callout.Text>{runError}</Callout.Text>
              </Callout.Root>
              <Flex gap="3">
                <Button size="4" variant="soft" color="gray" onClick={() => setRunState('idle')}>
                  Back
                </Button>
                <Button size="4" style={{ flex: 1 }} onClick={deploy}>
                  <ReloadIcon /> Retry
                </Button>
              </Flex>
            </>
          )}

          {runState === 'ok' && (
            <Flex gap="3">
              <Button size="4" style={{ flex: 1 }} onClick={() => navigate(`/chat/${profileId}`, { replace: true })}>
                <ChatBubbleIcon /> Open chat
              </Button>
              <Button size="4" variant="soft" color="gray" onClick={() => navigate(`/agent/${profileId}`, { replace: true })}>
                <GearIcon /> Manage
              </Button>
            </Flex>
          )}
        </Flex>
      )}
    </AppShell>
  );
}

function Stepper({ stage }: { stage: Stage }) {
  const stages: { id: Stage; label: string }[] = [
    { id: 'server', label: 'Server' },
    { id: 'model', label: 'Model' },
    { id: 'deploy', label: 'Deploy' },
  ];
  const index = stages.findIndex((s) => s.id === stage);
  return (
    <Flex gap="2" align="center">
      {stages.map((s, i) => (
        <Flex key={s.id} align="center" gap="2" style={{ flex: i < stages.length - 1 ? 1 : undefined }}>
          <Badge size="2" radius="full" variant={i <= index ? 'solid' : 'soft'} color={i <= index ? 'teal' : 'gray'}>
            {i + 1}
          </Badge>
          <Text size="2" weight={i === index ? 'bold' : 'regular'} color={i === index ? undefined : 'gray'}>
            {s.label}
          </Text>
          {i < stages.length - 1 && <Box flexGrow="1" className={i < index ? 'step-line step-line-done' : 'step-line'} />}
        </Flex>
      ))}
    </Flex>
  );
}

function StepRow({ label, state }: { label: string; state: StepState }) {
  return (
    <Flex align="center" gap="2">
      <Flex width="18px" justify="center">
        {state === 'running' ? (
          <Spinner size="2" />
        ) : state === 'done' ? (
          <CheckCircledIcon color="var(--green-11)" width="18" height="18" />
        ) : state === 'failed' ? (
          <CrossCircledIcon color="var(--red-11)" width="18" height="18" />
        ) : (
          <Box className="dot dot-gray" />
        )}
      </Flex>
      <Text size="2" color={state === 'pending' ? 'gray' : undefined} weight={state === 'running' ? 'medium' : 'regular'}>
        {label}
      </Text>
    </Flex>
  );
}
