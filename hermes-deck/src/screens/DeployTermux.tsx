import { CheckCircledIcon, CircleIcon, ExclamationTriangleIcon, ExternalLinkIcon, MobileIcon, ReloadIcon } from '@radix-ui/react-icons';
import { Box, Button, Callout, Card, Code, Flex, Link, Spinner, Text, TextField } from '@radix-ui/themes';
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { AppShell } from '../components/AppShell';
import { CopyButton } from '../components/CopyButton';
import { Field } from '../components/Field';
import { defaultModelChoice, ModelFields, type ModelChoice } from '../components/ModelFields';
import { errorMessage } from '../hooks';
import { API_PORT } from '../lib/deployScript';
import { health } from '../lib/hermesClient';
import { providerById } from '../lib/providers';
import { randomToken } from '../lib/shell';
import { newId } from '../lib/store';
import { buildTermuxInstallScript } from '../lib/termuxScript';
import { Termux, type TermuxStatus } from '../native/plugins';
import { navigate } from '../router';
import { useAgents } from '../state';

const ALLOW_EXTERNAL = "mkdir -p ~/.termux && echo 'allow-external-apps=true' >> ~/.termux/termux.properties && termux-reload-settings";

export function DeployTermux() {
  const { upsert } = useAgents();
  const profileId = useMemo(newId, []);
  const apiServerKey = useMemo(() => randomToken(32), []);
  const [status, setStatus] = useState<TermuxStatus | null>(null);
  const [model, setModel] = useState<ModelChoice>(defaultModelChoice);
  const [name, setName] = useState('This phone');
  const [phase, setPhase] = useState<'setup' | 'installing' | 'checking' | 'done'>('setup');
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(() => Termux.status().then(setStatus).catch(() => setStatus({ installed: false, permissionGranted: false })), []);
  useEffect(() => {
    void refresh();
    const onResume = () => document.visibilityState === 'visible' && refresh();
    document.addEventListener('visibilitychange', onResume);
    return () => document.removeEventListener('visibilitychange', onResume);
  }, [refresh]);

  async function install() {
    setError(null);
    try {
      const provider = providerById(model.providerId);
      const script = buildTermuxInstallScript({
        providerEnv: provider.envVar,
        providerKey: model.apiKey,
        providerId: provider.id,
        model: model.model,
        apiServerKey,
      });
      await Termux.runInTerminal({ script });
      setPhase('installing');
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  async function verify() {
    setError(null);
    setPhase('checking');
    const ok = await health({ baseUrl: `http://127.0.0.1:${API_PORT}`, apiKey: apiServerKey });
    if (!ok) {
      setPhase('installing');
      setError('Hermes is not answering on 127.0.0.1:8642 yet. Wait for the Termux session to finish, then check again.');
      return;
    }
    await upsert({
      id: profileId,
      name: name.trim() || 'This phone',
      kind: 'termux',
      apiKey: apiServerKey,
      providerId: model.providerId,
      model: model.model,
      createdAt: Date.now(),
      deployedAt: Date.now(),
    });
    setPhase('done');
    navigate(`/chat/${profileId}`, { replace: true });
  }

  const ready = status?.installed && status.permissionGranted;

  return (
    <AppShell back title="Run on this phone" subtitle="Hermes Agent inside Termux">
      <Flex direction="column" gap="4">
        <Callout.Root color="amber">
          <Callout.Icon>
            <ExclamationTriangleIcon />
          </Callout.Icon>
          <Callout.Text>
            Experimental. Hermes' Termux package supports arm64 phones only, and its docs currently list it as broken while a fix ships.
            Android may also stop background processes; a server deployment is more reliable.
          </Callout.Text>
        </Callout.Root>

        <Card size="2">
          <Flex direction="column" gap="3">
            <Check done={!!status?.installed} title="Install Termux">
              <Text size="2" color="gray">
                Use the F-Droid or GitHub build; the Play Store one is outdated.{' '}
                <Link href="https://f-droid.org/packages/com.termux/" target="_blank" rel="noreferrer">
                  F-Droid <ExternalLinkIcon style={{ verticalAlign: 'middle' }} />
                </Link>
              </Text>
            </Check>
            <Check done={!!ready} title="Allow Hermes Deck to run commands">
              <Text as="p" size="2" color="gray">
                In Termux, run this once:
              </Text>
              <Flex gap="2" align="start" mt="1">
                <Code size="1" className="wrap" style={{ flex: 1 }}>
                  {ALLOW_EXTERNAL}
                </Code>
                <CopyButton value={ALLOW_EXTERNAL} />
              </Flex>
              <Button
                mt="2"
                size="2"
                variant="soft"
                disabled={!status?.installed || status.permissionGranted}
                onClick={() => Termux.requestPermission().then(setStatus).catch((e) => setError(errorMessage(e)))}
              >
                Grant permission
              </Button>
            </Check>
          </Flex>
        </Card>

        <Field label="Name">
          <TextField.Root size="3" value={name} onChange={(e) => setName(e.target.value)} />
        </Field>
        <ModelFields value={model} onChange={setModel} />

        {error && (
          <Callout.Root color="red">
            <Callout.Icon>
              <ExclamationTriangleIcon />
            </Callout.Icon>
            <Callout.Text>{error}</Callout.Text>
          </Callout.Root>
        )}

        {phase === 'setup' && (
          <Button size="4" disabled={!ready || !model.apiKey || !model.model} onClick={install}>
            <MobileIcon /> Install in Termux
          </Button>
        )}
        {(phase === 'installing' || phase === 'checking') && (
          <Flex direction="column" gap="2">
            <Text size="2" color="gray">
              The install runs in a Termux session. When it says "Done", come back and check.
            </Text>
            <Flex gap="3">
              <Button size="4" variant="soft" color="gray" onClick={install}>
                Run again
              </Button>
              <Button size="4" style={{ flex: 1 }} disabled={phase === 'checking'} onClick={verify}>
                {phase === 'checking' ? <Spinner /> : <ReloadIcon />} Check Hermes
              </Button>
            </Flex>
          </Flex>
        )}
      </Flex>
    </AppShell>
  );
}

function Check({ done, title, children }: { done: boolean; title: string; children: ReactNode }) {
  return (
    <Flex gap="3" align="start">
      <Box pt="1">{done ? <CheckCircledIcon color="var(--green-11)" width="18" height="18" /> : <CircleIcon color="var(--gray-9)" width="18" height="18" />}</Box>
      <Box flexGrow="1" minWidth="0">
        <Text as="div" weight="medium" size="3">
          {title}
        </Text>
        {children}
      </Box>
    </Flex>
  );
}
