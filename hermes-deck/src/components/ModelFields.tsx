import { ExternalLinkIcon } from '@radix-ui/react-icons';
import { Flex, Link, Select, TextField } from '@radix-ui/themes';
import { PROVIDERS, providerById } from '../lib/providers';
import { Field } from './Field';
import { SecretField } from './SecretField';

export interface ModelChoice {
  providerId: string;
  apiKey: string;
  model: string;
}

const CUSTOM = '__custom__';

export function defaultModelChoice(): ModelChoice {
  return { providerId: PROVIDERS[0].id, apiKey: '', model: PROVIDERS[0].models[0] };
}

export function ModelFields({ value, onChange }: { value: ModelChoice; onChange(v: ModelChoice): void }) {
  const provider = providerById(value.providerId);
  const isCustom = !provider.models.includes(value.model);
  return (
    <Flex direction="column" gap="4">
      <Field label="Model provider">
        <Select.Root
          size="3"
          value={value.providerId}
          onValueChange={(id) => onChange({ providerId: id, apiKey: '', model: providerById(id).models[0] })}
        >
          <Select.Trigger />
          <Select.Content position="popper">
            {PROVIDERS.map((p) => (
              <Select.Item key={p.id} value={p.id}>
                {p.label}
              </Select.Item>
            ))}
          </Select.Content>
        </Select.Root>
      </Field>
      <Field
        label={`${provider.label} API key`}
        hint={
          <>
            Saved as <code>{provider.envVar}</code> in the agent's <code>.env</code>.{' '}
            <Link href={provider.keyUrl} target="_blank" rel="noreferrer">
              Get a key <ExternalLinkIcon style={{ verticalAlign: 'middle' }} />
            </Link>
          </>
        }
      >
        <SecretField value={value.apiKey} onChange={(apiKey) => onChange({ ...value, apiKey: apiKey.trim() })} placeholder="Paste your key" />
      </Field>
      <Field label="Model">
        <Select.Root size="3" value={isCustom ? CUSTOM : value.model} onValueChange={(m) => onChange({ ...value, model: m === CUSTOM ? '' : m })}>
          <Select.Trigger />
          <Select.Content position="popper">
            {provider.models.map((m) => (
              <Select.Item key={m} value={m}>
                {m}
              </Select.Item>
            ))}
            <Select.Separator />
            <Select.Item value={CUSTOM}>Other model id…</Select.Item>
          </Select.Content>
        </Select.Root>
        {isCustom && (
          <TextField.Root
            size="3"
            mt="2"
            value={value.model}
            placeholder="e.g. anthropic/claude-opus-5.5"
            autoCapitalize="off"
            autoCorrect="off"
            spellCheck={false}
            onChange={(e) => onChange({ ...value, model: e.target.value.trim() })}
          />
        )}
      </Field>
    </Flex>
  );
}
