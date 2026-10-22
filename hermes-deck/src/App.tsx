import { Theme } from '@radix-ui/themes';
import { useEffect, useState } from 'react';
import { useRoute } from './router';
import { Chat } from './screens/Chat';
import { ConnectExisting } from './screens/ConnectExisting';
import { DeploySsh } from './screens/DeploySsh';
import { DeployTermux } from './screens/DeployTermux';
import { Home } from './screens/Home';
import { Manage } from './screens/Manage';
import { NewAgent } from './screens/NewAgent';
import { AgentsProvider } from './state';

function useSystemAppearance(): 'light' | 'dark' {
  const query = '(prefers-color-scheme: light)';
  const [light, setLight] = useState(() => window.matchMedia?.(query).matches ?? false);
  useEffect(() => {
    const mq = window.matchMedia?.(query);
    if (!mq) return;
    const onChange = () => setLight(mq.matches);
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, []);
  return light ? 'light' : 'dark';
}

function Screen() {
  const route = useRoute();
  switch (route.name) {
    case 'home':
      return <Home />;
    case 'new':
      return <NewAgent />;
    case 'deploy-ssh':
      return <DeploySsh />;
    case 'deploy-termux':
      return <DeployTermux />;
    case 'connect':
      return <ConnectExisting />;
    case 'agent':
      return <Manage key={route.id} id={route.id} />;
    case 'chat':
      return <Chat key={route.id} id={route.id} />;
  }
}

export function App() {
  const appearance = useSystemAppearance();
  return (
    <Theme appearance={appearance} accentColor="teal" grayColor="slate" radius="large" scaling="100%" panelBackground="solid">
      <AgentsProvider>
        <Screen />
      </AgentsProvider>
    </Theme>
  );
}
