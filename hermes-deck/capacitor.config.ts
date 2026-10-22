import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'dev.kawin.hermesdeck',
  appName: 'Hermes Deck',
  webDir: 'dist',
  android: {
    // Agent traffic goes through the native HermesHttp plugin, never the WebView,
    // so the WebView itself never needs cleartext or mixed content.
    allowMixedContent: false,
    webContentsDebuggingEnabled: false,
  },
};

export default config;
