import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // The bundle ships inside the APK and loads from disk, so one ~500 kB chunk is fine.
  build: { outDir: 'dist', target: 'es2022', sourcemap: false, chunkSizeWarningLimit: 1024 },
  test: { environment: 'node', include: ['src/**/*.test.ts'] },
});
