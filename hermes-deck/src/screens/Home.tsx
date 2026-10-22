import { ChatBubbleIcon, DesktopIcon, GearIcon, Link2Icon, MobileIcon, PlusIcon, RocketIcon } from '@radix-ui/react-icons';
import { Avatar, Badge, Box, Button, Card, Flex, Heading, IconButton, Skeleton, Text } from '@radix-ui/themes';
import { AppShell } from '../components/AppShell';
import { StatusBadge } from '../components/StatusBadge';
import { useHealth } from '../hooks';
import type { AgentProfile } from '../lib/store';
import { navigate } from '../router';
import { useAgents } from '../state';

export function Home() {
  const { ready, profiles } = useAgents();
  return (
    <AppShell
      title={
        <Flex align="center" gap="2">
          <Avatar size="2" radius="full" fallback="H" color="teal" variant="solid" />
          Hermes Deck
        </Flex>
      }
      subtitle="Deploy and drive Nous Research's Hermes Agent"
      actions={
        profiles.length > 0 && (
          <IconButton size="3" radius="full" aria-label="New agent" onClick={() => navigate('/new')}>
            <PlusIcon width="20" height="20" />
          </IconButton>
        )
      }
    >
      {!ready ? (
        <Flex direction="column" gap="3">
          <Skeleton height="120px" />
          <Skeleton height="120px" />
        </Flex>
      ) : profiles.length === 0 ? (
        <Welcome />
      ) : (
        <Flex direction="column" gap="3">
          {profiles.map((p) => (
            <AgentCard key={p.id} profile={p} />
          ))}
        </Flex>
      )}
    </AppShell>
  );
}

function Welcome() {
  return (
    <Flex direction="column" gap="5">
      <Card size="3" className="hero">
        <Flex direction="column" gap="3">
          <Badge color="teal" variant="soft" radius="full" style={{ alignSelf: 'flex-start' }}>
            <RocketIcon /> Self-hosted AI agent
          </Badge>
          <Heading size="7">Deploy Hermes Agent from your phone</Heading>
          <Text color="gray" size="3">
            Hermes is an open-source agent with terminal, web, memory and skills. Hermes Deck installs it on a server you control,
            keeps its API private behind SSH, and gives you a chat client for it.
          </Text>
          <Button size="4" onClick={() => navigate('/deploy/ssh')}>
            <RocketIcon /> Deploy to my server
          </Button>
        </Flex>
      </Card>
      <TargetList />
    </Flex>
  );
}

export function TargetList() {
  return (
    <Flex direction="column" gap="3">
      <TargetCard
        icon={<DesktopIcon width="22" height="22" />}
        title="Linux server or VPS"
        badge="Recommended"
        text="Over SSH: installs Docker if needed and runs the official nousresearch/hermes-agent image. Its API stays on the server's loopback."
        onClick={() => navigate('/deploy/ssh')}
      />
      <TargetCard
        icon={<MobileIcon width="22" height="22" />}
        title="This phone (Termux)"
        badge="Experimental"
        badgeColor="amber"
        text="Installs Hermes from its signed Termux package and runs it on-device. arm64 phones only."
        onClick={() => navigate('/deploy/termux')}
      />
      <TargetCard
        icon={<Link2Icon width="22" height="22" />}
        title="Connect an existing agent"
        text="Already running Hermes with its API server enabled? Add its HTTPS URL and API key."
        onClick={() => navigate('/connect')}
      />
    </Flex>
  );
}

function TargetCard(props: { icon: React.ReactNode; title: string; text: string; badge?: string; badgeColor?: 'amber' | 'teal'; onClick(): void }) {
  return (
    <Card asChild size="2">
      <button type="button" className="card-button" onClick={props.onClick}>
        <Flex gap="3" align="start">
          <Box className="icon-tile">{props.icon}</Box>
          <Box flexGrow="1">
            <Flex align="center" gap="2" wrap="wrap">
              <Text weight="bold" size="3">
                {props.title}
              </Text>
              {props.badge && (
                <Badge color={props.badgeColor ?? 'teal'} variant="soft">
                  {props.badge}
                </Badge>
              )}
            </Flex>
            <Text as="p" size="2" color="gray" mt="1">
              {props.text}
            </Text>
          </Box>
        </Flex>
      </button>
    </Card>
  );
}

function describe(p: AgentProfile): string {
  if (p.kind === 'ssh' && p.ssh) return `${p.ssh.username}@${p.ssh.host}`;
  if (p.kind === 'termux') return 'This phone · Termux';
  return p.baseUrl ?? '';
}

function AgentCard({ profile }: { profile: AgentProfile }) {
  const health = useHealth(profile);
  return (
    <Card size="2">
      <Flex direction="column" gap="3">
        <Flex justify="between" align="start" gap="2">
          <Box minWidth="0">
            <Text as="div" weight="bold" size="4" truncate>
              {profile.name}
            </Text>
            <Text as="div" size="2" color="gray" truncate>
              {describe(profile)}
            </Text>
          </Box>
          <StatusBadge health={health} />
        </Flex>
        {profile.model && (
          <Text size="1" color="gray">
            Model <Badge variant="outline" color="gray">{profile.model}</Badge>
          </Text>
        )}
        <Flex gap="2">
          <Button size="3" style={{ flex: 1 }} onClick={() => navigate(`/chat/${encodeURIComponent(profile.id)}`)}>
            <ChatBubbleIcon /> Chat
          </Button>
          <Button size="3" variant="soft" color="gray" onClick={() => navigate(`/agent/${encodeURIComponent(profile.id)}`)}>
            <GearIcon /> Manage
          </Button>
        </Flex>
      </Flex>
    </Card>
  );
}
