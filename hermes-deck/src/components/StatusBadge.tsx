import { Badge, Spinner } from '@radix-ui/themes';

export type Health = 'checking' | 'online' | 'offline' | 'unknown';

export function StatusBadge({ health }: { health: Health }) {
  switch (health) {
    case 'checking':
      return (
        <Badge color="gray" variant="soft" radius="full">
          <Spinner size="1" /> Checking
        </Badge>
      );
    case 'online':
      return (
        <Badge color="green" variant="soft" radius="full">
          <span className="dot dot-green" /> Online
        </Badge>
      );
    case 'offline':
      return (
        <Badge color="red" variant="soft" radius="full">
          <span className="dot dot-red" /> Offline
        </Badge>
      );
    default:
      return (
        <Badge color="gray" variant="soft" radius="full">
          Unknown
        </Badge>
      );
  }
}
