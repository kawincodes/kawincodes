import { CheckIcon, DotsVerticalIcon, ExclamationTriangleIcon, GearIcon, PaperPlaneIcon, StopIcon, TrashIcon } from '@radix-ui/react-icons';
import { Badge, Box, Button, Callout, Card, DropdownMenu, Flex, IconButton, Spinner, Text, TextArea } from '@radix-ui/themes';
import { Fragment, useEffect, useRef, useState } from 'react';
import { AppShell } from '../components/AppShell';
import { StatusBadge } from '../components/StatusBadge';
import { errorMessage, useHealth } from '../hooks';
import { dropConnection, endpointFor } from '../lib/agents';
import { streamChat, type ChatMessage, type ToolProgress } from '../lib/hermesClient';
import { clearChat, loadChat, saveChat, type AgentProfile } from '../lib/store';
import { navigate } from '../router';
import { useAgents } from '../state';

const SUGGESTIONS = [
  'What can you do, and which tools do you have?',
  'Check disk, memory and uptime on your machine.',
  'Search the web for today’s top AI news and summarise it.',
];

export function Chat({ id }: { id: string }) {
  const { profiles, ready } = useAgents();
  const profile = profiles.find((p) => p.id === id);
  if (!ready) return <AppShell back title="Chat">{null}</AppShell>;
  if (!profile) {
    return (
      <AppShell back title="Agent not found">
        <Text color="gray">This agent was removed.</Text>
      </AppShell>
    );
  }
  return <ChatView profile={profile} />;
}

interface LiveTurn {
  text: string;
  tools: ToolProgress[];
}

function ChatView({ profile }: { profile: AgentProfile }) {
  const health = useHealth(profile);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [live, setLive] = useState<LiveTurn | null>(null);
  const [error, setError] = useState<string | null>(null);
  const cancelRef = useRef<(() => void) | null>(null);
  const bottom = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadChat(profile.id).then(setMessages);
  }, [profile.id]);

  useEffect(() => {
    bottom.current?.scrollIntoView({ block: 'end' });
  }, [messages, live]);

  useEffect(() => () => cancelRef.current?.(), []);

  async function send(text: string) {
    const content = text.trim();
    if (!content || live) return;
    setError(null);
    setDraft('');
    const history: ChatMessage[] = [...messages, { role: 'user', content }];
    setMessages(history);
    void saveChat(profile.id, history);
    const turn: LiveTurn = { text: '', tools: [] };
    setLive({ ...turn });
    try {
      const ep = await endpointFor(profile);
      const { done, cancel } = streamChat(ep, history, {
        onDelta: (t) => {
          turn.text += t;
          setLive({ text: turn.text, tools: turn.tools });
        },
        onTool: (p) => {
          const existing = turn.tools.find((x) => x.toolCallId === p.toolCallId);
          turn.tools = existing ? turn.tools.map((x) => (x.toolCallId === p.toolCallId ? { ...x, ...p } : x)) : [...turn.tools, p];
          setLive({ text: turn.text, tools: turn.tools });
        },
      });
      cancelRef.current = cancel;
      await done;
    } catch (e) {
      setError(errorMessage(e));
      if (profile.kind === 'ssh') void dropConnection(profile.id);
    } finally {
      cancelRef.current = null;
      setLive(null);
      if (turn.text) {
        const next: ChatMessage[] = [...history, { role: 'assistant', content: turn.text }];
        setMessages(next);
        void saveChat(profile.id, next);
      }
    }
  }

  return (
    <AppShell
      back
      title={profile.name}
      subtitle={
        <Flex align="center" gap="2" asChild>
          <span>
            <StatusBadge health={health} /> {profile.model ?? 'hermes-agent'}
          </span>
        </Flex>
      }
      actions={
        <DropdownMenu.Root>
          <DropdownMenu.Trigger>
            <IconButton variant="ghost" color="gray" size="3" aria-label="Chat menu">
              <DotsVerticalIcon width="18" height="18" />
            </IconButton>
          </DropdownMenu.Trigger>
          <DropdownMenu.Content align="end">
            <DropdownMenu.Item onSelect={() => navigate(`/agent/${encodeURIComponent(profile.id)}`)}>
              <GearIcon /> Manage agent
            </DropdownMenu.Item>
            <DropdownMenu.Separator />
            <DropdownMenu.Item
              color="red"
              disabled={!!live}
              onSelect={async () => {
                await clearChat(profile.id);
                setMessages([]);
              }}
            >
              <TrashIcon /> New conversation
            </DropdownMenu.Item>
          </DropdownMenu.Content>
        </DropdownMenu.Root>
      }
      footer={
        <Flex gap="2" align="end" p="3" className="composer">
          <Box flexGrow="1">
            <TextArea
              size="3"
              rows={1}
              value={draft}
              placeholder="Message Hermes…"
              className="composer-input"
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey && !('ontouchstart' in window)) {
                  e.preventDefault();
                  void send(draft);
                }
              }}
            />
          </Box>
          {live ? (
            <IconButton size="4" color="red" variant="soft" aria-label="Stop" onClick={() => cancelRef.current?.()}>
              <StopIcon width="20" height="20" />
            </IconButton>
          ) : (
            <IconButton size="4" aria-label="Send" disabled={!draft.trim()} onClick={() => send(draft)}>
              <PaperPlaneIcon width="20" height="20" />
            </IconButton>
          )}
        </Flex>
      }
    >
      <Flex direction="column" gap="3" className="messages">
        {messages.length === 0 && !live && (
          <Flex direction="column" gap="3" align="stretch" py="6">
            <Text align="center" size="5" weight="bold">
              Talk to your agent
            </Text>
            <Text align="center" size="2" color="gray">
              Hermes runs tools on its own machine: terminal, files, web, memory and skills.
            </Text>
            {SUGGESTIONS.map((s) => (
              <Button key={s} variant="surface" color="gray" size="3" className="suggestion" onClick={() => send(s)}>
                {s}
              </Button>
            ))}
          </Flex>
        )}
        {messages.map((m, i) => (
          <Bubble key={i} role={m.role} text={m.content} />
        ))}
        {live && (
          <Flex direction="column" gap="2">
            {live.tools.length > 0 && (
              <Flex gap="2" wrap="wrap">
                {live.tools.map((t) => (
                  <Badge key={t.toolCallId} size="2" color={t.status === 'completed' ? 'green' : 'teal'} variant="soft" className="tool-chip">
                    {t.status === 'completed' ? <CheckIcon /> : <Spinner size="1" />}
                    <span>{t.emoji ?? '🛠️'}</span>
                    <span className="tool-label">{t.label ?? t.tool}</span>
                  </Badge>
                ))}
              </Flex>
            )}
            {live.text ? (
              <Bubble role="assistant" text={live.text} streaming />
            ) : (
              <Flex align="center" gap="2">
                <Spinner />
                <Text size="2" color="gray">
                  {live.tools.some((t) => t.status === 'running') ? 'Working…' : 'Thinking…'}
                </Text>
              </Flex>
            )}
          </Flex>
        )}
        {error && (
          <Callout.Root color="red">
            <Callout.Icon>
              <ExclamationTriangleIcon />
            </Callout.Icon>
            <Callout.Text>{error}</Callout.Text>
          </Callout.Root>
        )}
        <div ref={bottom} />
      </Flex>
    </AppShell>
  );
}

function Bubble({ role, text, streaming }: { role: ChatMessage['role']; text: string; streaming?: boolean }) {
  if (role === 'user') {
    return (
      <Box className="bubble bubble-user">
        <Text size="3">{text}</Text>
      </Box>
    );
  }
  return (
    <Card size="2" className="bubble bubble-agent">
      <RichText text={text} />
      {streaming && <span className="cursor" />}
    </Card>
  );
}

/** Minimal Markdown: fenced code blocks, inline code and **bold**. Everything is rendered as text, never HTML. */
function RichText({ text }: { text: string }) {
  const parts = text.split(/```[^\n]*\n?/);
  return (
    <>
      {parts.map((part, i) =>
        i % 2 === 1 ? (
          <pre key={i} className="code-block">
            {part.replace(/\n$/, '')}
          </pre>
        ) : (
          part.trim() && (
            <Text key={i} as="div" size="3" className="prose">
              {inline(part.replace(/^\n+|\n+$/g, ''))}
            </Text>
          )
        ),
      )}
    </>
  );
}

function inline(text: string) {
  return text.split(/(`[^`\n]+`|\*\*[^*\n]+\*\*)/).map((seg, i) => {
    if (seg.startsWith('`') && seg.endsWith('`') && seg.length > 2) return <code key={i}>{seg.slice(1, -1)}</code>;
    if (seg.startsWith('**') && seg.endsWith('**') && seg.length > 4) return <strong key={i}>{seg.slice(2, -2)}</strong>;
    return <Fragment key={i}>{seg}</Fragment>;
  });
}
