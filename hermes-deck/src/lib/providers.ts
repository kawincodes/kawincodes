export interface Provider {
  /** Value Hermes expects in `model.provider`. */
  id: string;
  label: string;
  /** Environment variable Hermes reads the key from. */
  envVar: string;
  keyUrl: string;
  models: string[];
}

// Model ids mirror Hermes' curated catalog (hermes_cli/models_catalog_static.py).
export const PROVIDERS: Provider[] = [
  {
    id: 'openrouter',
    label: 'OpenRouter',
    envVar: 'OPENROUTER_API_KEY',
    keyUrl: 'https://openrouter.ai/keys',
    models: [
      'anthropic/claude-sonnet-5.5',
      'anthropic/claude-opus-5.5',
      'openai/gpt-6-astra',
      'google/gemini-3.8-flash',
      'moonshotai/kimi-k3',
      'z-ai/glm-5.3',
    ],
  },
  {
    id: 'anthropic',
    label: 'Anthropic',
    envVar: 'ANTHROPIC_API_KEY',
    keyUrl: 'https://console.anthropic.com/',
    models: ['claude-sonnet-5', 'claude-opus-5-5', 'claude-fable-5.1', 'claude-haiku-4-5-20251001'],
  },
  {
    id: 'openai-api',
    label: 'OpenAI',
    envVar: 'OPENAI_API_KEY',
    keyUrl: 'https://platform.openai.com/api-keys',
    models: ['gpt-6.1-sol', 'gpt-6-luna', 'gpt-5.5', 'gpt-5.4-mini'],
  },
  {
    id: 'gemini',
    label: 'Google AI Studio',
    envVar: 'GOOGLE_API_KEY',
    keyUrl: 'https://aistudio.google.com/app/apikey',
    models: ['gemini-3.8-flash', 'gemini-4-argon', 'gemini-3.1-pro-preview'],
  },
  {
    id: 'deepseek',
    label: 'DeepSeek',
    envVar: 'DEEPSEEK_API_KEY',
    keyUrl: 'https://platform.deepseek.com/api_keys',
    models: ['deepseek-v4-pro', 'deepseek-flash'],
  },
];

export function providerById(id: string): Provider {
  const p = PROVIDERS.find((x) => x.id === id);
  if (!p) throw new Error(`Unknown provider ${id}`);
  return p;
}
