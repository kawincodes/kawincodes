import { AppShell } from '../components/AppShell';
import { TargetList } from './Home';

export function NewAgent() {
  return (
    <AppShell back title="New agent" subtitle="Where should Hermes run?">
      <TargetList />
    </AppShell>
  );
}
