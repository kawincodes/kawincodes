import { useEffect, useState } from 'react';

export type Route =
  | { name: 'home' }
  | { name: 'new' }
  | { name: 'deploy-ssh' }
  | { name: 'deploy-termux' }
  | { name: 'connect' }
  | { name: 'agent'; id: string }
  | { name: 'chat'; id: string };

export function parseRoute(hash: string): Route {
  const parts = hash.replace(/^#\/?/, '').split('/').filter(Boolean);
  switch (parts[0]) {
    case 'new':
      return { name: 'new' };
    case 'deploy':
      return parts[1] === 'termux' ? { name: 'deploy-termux' } : { name: 'deploy-ssh' };
    case 'connect':
      return { name: 'connect' };
    case 'agent':
      return parts[1] ? { name: 'agent', id: decodeURIComponent(parts[1]) } : { name: 'home' };
    case 'chat':
      return parts[1] ? { name: 'chat', id: decodeURIComponent(parts[1]) } : { name: 'home' };
    default:
      return { name: 'home' };
  }
}

/** Hash routes give every screen a history entry, so Android's back gesture works naturally. */
export function navigate(path: string, opts: { replace?: boolean } = {}): void {
  const hash = `#${path}`;
  if (opts.replace) {
    history.replaceState(null, '', hash);
    window.dispatchEvent(new HashChangeEvent('hashchange'));
  } else {
    window.location.hash = path;
  }
}

export function goBack(fallback = '/'): void {
  if (history.length > 1) history.back();
  else navigate(fallback, { replace: true });
}

export function useRoute(): Route {
  const [route, setRoute] = useState(() => parseRoute(window.location.hash));
  useEffect(() => {
    const onChange = () => {
      setRoute(parseRoute(window.location.hash));
      window.scrollTo(0, 0);
    };
    window.addEventListener('hashchange', onChange);
    return () => window.removeEventListener('hashchange', onChange);
  }, []);
  return route;
}
