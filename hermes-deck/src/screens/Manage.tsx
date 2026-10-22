import {
  ActivityLogIcon,
  ChatBubbleIcon,
  CubeIcon,
  ExclamationTriangleIcon,
  FileTextIcon,
  PlayIcon,
  ReloadIcon,
  StopIcon,
  TrashIcon,
  UpdateIcon,
} from '@radix-ui/react-icons';
import { AlertDialog, Badge, Button, Callout, Card, Code, DataList, Flex, Grid, Heading, IconButton, Spinner, Text } from '@radix-ui/themes';
import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { AppShell } from '../components/AppShell';
import { CopyButton } from '../components/CopyButton';
import { LogView } from '../components/LogView';
import { StatusBadge } from '../components/StatusBadge';
import { errorMessage, useHealth } from '../hooks';
import { connectionFor, endpointFor, privilegeFor } from '../lib/agents';
import { createMarkerParser, explainFailure } from '../lib/deployMarkers';
import {
  asRoot,
  buildDockerUpdateScript,
  manageCommand,
  parseContainerStatus,
  runStagedScriptCommand,
  stageScriptCommand,
  type ContainerStatus,
  type ManageAction,
} from '../lib/deployScript';
import { listModels } from '../lib/hermesClient';
import type { AgentProfile } from '../lib/store';
import { termuxLogsScript, termuxStartScript, termuxStopScript } from '../lib/termuxScript';
import { Termux } from '../native/plugins';
import { navigate } from '../router';
import { useAgents } from '../state';

export function Manage({ id }: { id: string }) {
  const { profiles, ready } = useAgents();
  const profile = profiles.find((p) => p.id === id);
  if (!ready) return <AppShell back title="Agent">{null}</AppShell>;
  if (!profile) {
    return (
      <AppShell back title="Agent not found">
        <Text color="gray">This agent was removed.</Text>
      </AppShell>
    );
  }
  return <ManageAgent profile={profile} />;
}

function ManageAgent({ profile }: { profile: AgentProfile }) {
  const { remove } = useAgents();
  const [nonce, setNonce] = useState(0);
  const health = useHealth(profile, nonce);
  const [container, setContainer] = useState<ContainerStatus | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  const [output, setOutput] = useState('');
  const [error, setError] = useState<string | null>(null);

  const ssh = profile.ssh;

  /** Runs one management command as root over SSH and returns its combined output. */
  const runRemote = useCallback(
    async (action: ManageAction) => {
      if (!ssh) throw new Error('Not an SSH agent');
      const conn = await connectionFor(profile);
      const { command, stdin } = asRoot(manageCommand(action, ssh.container), privilegeFor(ssh));
      let out = '';
      const code = await conn.exec(command, { stdin, onOutput: (_s, c) => (out += c) });
      if (code !== 0 && action !== 'status') throw new Error(explainFailure(out) ?? (out.trim() || `exit code ${code}`));
      return out;
    },
    [profile, ssh],
  );

  const refreshContainer = useCallback(async () => {
    if (!ssh) return;
    try {
      setContainer(parseContainerStatus(await runRemote('status')));
    } catch (e) {
      setError(errorMessage(e));
    }
  }, [runRemote, ssh]);

  useEffect(() => {
    void refreshContainer();
  }, [refreshContainer]);

  async function act(label: string, fn: () => Promise<string | void>) {
    setBusy(label);
    setError(null);
    try {
      const out = await fn();
      if (typeof out === 'string') setOutput(out.trim() || `${label}: done`);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(null);
      await refreshContainer();
      setNonce((n) => n + 1);
    }
  }

  async function update() {
    if (!ssh) return '';
    const conn = await connectionFor(profile);
    const name = `update-${Date.now()}.sh`;
    const staged = await conn.exec(stageScriptCommand(name), { stdin: buildDockerUpdateScript(ssh) });
    if (staged !== 0) throw new Error('Could not upload the update script.');
    let log = '';
    let fail: string | null = null;
    const parser = createMarkerParser({
      onStep: (s) => setOutput((log += `\n▶ ${s}\n`)),
      onOk: (image) => setOutput((log += `\n✔ Running ${image}\n`)),
      onFail: (m) => (fail = m),
      onLine: (l) => setOutput((log += `${l}\n`)),
    });
    const { command, stdin } = runStagedScriptCommand(name, privilegeFor(ssh));
    const code = await conn.exec(command, { stdin, onOutput: (_s, c) => parser.push(c) });
    parser.flush();
    if (code !== 0) throw new Error(explainFailure(log) ?? fail ?? `Update failed (exit ${code}).`);
    return log;
  }

  const termux = (script: string) => async () => {
    const r = await Termux.run({ script, timeoutMs: 150000 });
    if (r.errmsg && r.exitCode !== 0) throw new Error(r.errmsg);
    return `${r.stdout}${r.stderr}`;
  };

  const running = container?.state === 'running';

  return (
    <AppShell
      back
      title={profile.name}
      subtitle={ssh ? `${ssh.username}@${ssh.host}` : profile.kind === 'termux' ? 'This phone · Termux' : profile.baseUrl}
      actions={
        <IconButton variant="ghost" color="gray" size="3" aria-label="Refresh" onClick={() => act('Refresh', async () => undefined)}>
          <ReloadIcon width="18" height="18" />
        </IconButton>
      }
    >
      <Flex direction="column" gap="4">
        <Card size="2">
          <Flex justify="between" align="center" mb="3">
            <Heading size="3">Status</Heading>
            <StatusBadge health={health} />
          </Flex>
          <DataList.Root size="2">
            {ssh && (
              <>
                <DataList.Item>
                  <DataList.Label>Container</DataList.Label>
                  <DataList.Value>
                    {container ? (
                      <Badge color={running ? 'green' : container.state === 'missing' ? 'red' : 'amber'}>{container.state}</Badge>
                    ) : (
                      <Spinner />
                    )}
                  </DataList.Value>
                </DataList.Item>
                {container?.image && (
                  <DataList.Item>
                    <DataList.Label>Image</DataList.Label>
                    <DataList.Value>
                      <Code size="1">{container.image}</Code>
                    </DataList.Value>
                  </DataList.Item>
                )}
                {running && container?.startedAt && (
                  <DataList.Item>
                    <DataList.Label>Up since</DataList.Label>
                    <DataList.Value>{new Date(container.startedAt).toLocaleString()}</DataList.Value>
                  </DataList.Item>
                )}
              </>
            )}
            {profile.model && (
              <DataList.Item>
                <DataList.Label>Model</DataList.Label>
                <DataList.Value>{profile.model}</DataList.Value>
              </DataList.Item>
            )}
          </DataList.Root>
        </Card>

        <Button size="4" onClick={() => navigate(`/chat/${encodeURIComponent(profile.id)}`)}>
          <ChatBubbleIcon /> Open chat
        </Button>

        {ssh && (
          <Grid columns="2" gap="3">
            <Action icon={<ReloadIcon />} label="Restart" busy={busy} onClick={() => act('Restart', () => runRemote('restart'))} />
            {running ? (
              <Action icon={<StopIcon />} label="Stop" busy={busy} onClick={() => act('Stop', () => runRemote('stop'))} />
            ) : (
              <Action icon={<PlayIcon />} label="Start" busy={busy} onClick={() => act('Start', () => runRemote('start'))} />
            )}
            <Action icon={<UpdateIcon />} label="Update" busy={busy} onClick={() => act('Update', update)} />
            <Action icon={<ActivityLogIcon />} label="Doctor" busy={busy} onClick={() => act('Doctor', () => runRemote('doctor'))} />
            <Action icon={<FileTextIcon />} label="Logs" busy={busy} onClick={() => act('Logs', () => runRemote('logs'))} />
            <Action
              icon={<CubeIcon />}
              label="Models"
              busy={busy}
              onClick={() => act('Models', async () => (await listModels(await endpointFor(profile))).join('\n'))}
            />
          </Grid>
        )}
        {profile.kind === 'termux' && (
          <Grid columns="2" gap="3">
            <Action icon={<PlayIcon />} label="Start" busy={busy} onClick={() => act('Start', termux(termuxStartScript()))} />
            <Action icon={<StopIcon />} label="Stop" busy={busy} onClick={() => act('Stop', termux(termuxStopScript()))} />
            <Action icon={<FileTextIcon />} label="Logs" busy={busy} onClick={() => act('Logs', termux(termuxLogsScript()))} />
          </Grid>
        )}
        {profile.kind === 'direct' && (
          <Action
            icon={<CubeIcon />}
            label="List models"
            busy={busy}
            onClick={() => act('Models', async () => (await listModels(await endpointFor(profile))).join('\n'))}
          />
        )}

        {error && (
          <Callout.Root color="red">
            <Callout.Icon>
              <ExclamationTriangleIcon />
            </Callout.Icon>
            <Callout.Text>{error}</Callout.Text>
          </Callout.Root>
        )}
        {(output || busy) && <LogView text={output} placeholder={busy ? `${busy}…` : undefined} />}

        <Card size="2">
          <Heading size="3" mb="3">
            Connection
          </Heading>
          <DataList.Root size="2" orientation={{ initial: 'vertical', xs: 'horizontal' }}>
            {ssh && (
              <>
                <DataList.Item>
                  <DataList.Label>Host key</DataList.Label>
                  <DataList.Value>
                    <Code size="1" className="wrap">
                      {ssh.hostKeyType} {ssh.hostKeyFingerprint}
                    </Code>
                  </DataList.Value>
                </DataList.Item>
                <DataList.Item>
                  <DataList.Label>Data dir</DataList.Label>
                  <DataList.Value>
                    <Code size="1">{ssh.dataDir}</Code>
                  </DataList.Value>
                </DataList.Item>
                <DataList.Item>
                  <DataList.Label>API</DataList.Label>
                  <DataList.Value>127.0.0.1:{ssh.hostPort} on the server, via SSH tunnel</DataList.Value>
                </DataList.Item>
              </>
            )}
            <DataList.Item>
              <DataList.Label>API server key</DataList.Label>
              <DataList.Value>
                <Flex gap="2" align="center">
                  <Code size="1">{profile.apiKey.slice(0, 6)}…{profile.apiKey.slice(-4)}</Code>
                  <CopyButton value={profile.apiKey} label="Copy API key" />
                </Flex>
              </DataList.Value>
            </DataList.Item>
          </DataList.Root>
        </Card>

        <Card size="2" className="danger">
          <Heading size="3" mb="1" color="red">
            Danger zone
          </Heading>
          <Text as="p" size="2" color="gray" mb="3">
            {ssh
              ? `Removing the container stops Hermes but keeps ${ssh.dataDir} (config, memories, sessions). Forgetting only removes it from this app.`
              : 'Forgetting removes this agent and its chat history from the app.'}
          </Text>
          <Flex gap="3" wrap="wrap">
            {ssh && (
              <Confirm
                title="Remove the Hermes container?"
                body={`docker rm -f ${ssh.container}. Your data directory stays on the server; "Update" or a new deploy brings it back.`}
                action="Remove container"
                onConfirm={() => act('Remove', () => runRemote('remove'))}
              />
            )}
            <Confirm
              title="Forget this agent?"
              body="Deletes the saved credentials and chat history from this phone. Nothing on the server changes."
              action="Forget agent"
              onConfirm={async () => {
                await remove(profile.id);
                navigate('/', { replace: true });
              }}
            />
          </Flex>
        </Card>
      </Flex>
    </AppShell>
  );
}

function Action({ icon, label, busy, onClick }: { icon: ReactNode; label: string; busy: string | null; onClick(): void }) {
  return (
    <Button size="3" variant="surface" color="gray" disabled={busy !== null} onClick={onClick}>
      {busy === label ? <Spinner /> : icon} {label}
    </Button>
  );
}

function Confirm({ title, body, action, onConfirm }: { title: string; body: string; action: string; onConfirm(): void }) {
  return (
    <AlertDialog.Root>
      <AlertDialog.Trigger>
        <Button variant="soft" color="red">
          <TrashIcon /> {action}
        </Button>
      </AlertDialog.Trigger>
      <AlertDialog.Content maxWidth="400px">
        <AlertDialog.Title>{title}</AlertDialog.Title>
        <AlertDialog.Description size="2">{body}</AlertDialog.Description>
        <Flex gap="3" mt="4" justify="end">
          <AlertDialog.Cancel>
            <Button variant="soft" color="gray">
              Cancel
            </Button>
          </AlertDialog.Cancel>
          <AlertDialog.Action>
            <Button color="red" onClick={onConfirm}>
              {action}
            </Button>
          </AlertDialog.Action>
        </Flex>
      </AlertDialog.Content>
    </AlertDialog.Root>
  );
}
