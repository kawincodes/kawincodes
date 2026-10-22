import { ExclamationTriangleIcon, Link2Icon } from '@radix-ui/react-icons';
import { Button, Callout, Flex, Spinner, Text, TextField } from '@radix-ui/themes';
import { useState } from 'react';
import { AppShell } from '../components/AppShell';
import { Field } from '../components/Field';
import { SecretField } from '../components/SecretField';
import { errorMessage } from '../hooks';
import { health, listModels } from '../lib/hermesClient';
import { newId } from '../lib/store';
import { navigate } from '../router';
import { useAgents } from '../state';

export function ConnectExisting() {
  const { upsert } = useAgents();
  const [name, setName] = useState('');
  const [url, setUrl] = useState('https://');
  const [apiKey, setApiKey] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const baseUrl = url.trim().replace(/\/+$/, '').replace(/\/v1$/, '');
  const local = /^http:\/\/(127\.0\.0\.1|localhost)(:\d+)?$/.test(baseUrl);
  const valid = (/^https:\/\/[^/\s]+/.test(baseUrl) || local) && apiKey.length >= 8;

  async function connect() {
    setBusy(true);
    setError(null);
    try {
      const ep = { baseUrl, apiKey };
      if (!(await health(ep))) throw new Error(`No Hermes API server answered at ${baseUrl}/health.`);
      await listModels(ep); // proves the key is accepted
      const id = newId();
      await upsert({ id, name: name.trim() || new URL(baseUrl).host, kind: 'direct', apiKey, baseUrl, createdAt: Date.now() });
      navigate(`/chat/${id}`, { replace: true });
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <AppShell back title="Connect an agent" subtitle="Any Hermes with API_SERVER_ENABLED=true">
      <Flex direction="column" gap="4">
        <Field label="Name">
          <TextField.Root size="3" value={name} placeholder="Home lab" onChange={(e) => setName(e.target.value)} />
        </Field>
        <Field label="API server URL" hint="HTTPS only (put it behind a reverse proxy), or http://127.0.0.1:8642 for an agent on this phone.">
          <TextField.Root
            size="3"
            value={url}
            inputMode="url"
            autoCapitalize="off"
            autoCorrect="off"
            spellCheck={false}
            onChange={(e) => setUrl(e.target.value)}
          />
        </Field>
        <Field label="API server key" hint={<>The agent's <code>API_SERVER_KEY</code>.</>}>
          <SecretField value={apiKey} onChange={(v) => setApiKey(v.trim())} />
        </Field>
        {error && (
          <Callout.Root color="red">
            <Callout.Icon>
              <ExclamationTriangleIcon />
            </Callout.Icon>
            <Callout.Text>{error}</Callout.Text>
          </Callout.Root>
        )}
        <Button size="4" disabled={!valid || busy} onClick={connect}>
          {busy ? <Spinner /> : <Link2Icon />} Test and save
        </Button>
        <Text size="1" color="gray">
          Exposing the API server to the internet without TLS and a strong key lets anyone drive your agent, including its terminal.
        </Text>
      </Flex>
    </AppShell>
  );
}
