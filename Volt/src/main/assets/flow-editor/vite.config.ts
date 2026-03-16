import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';
import { mockApi } from './src/lib/server/mockApiPlugin';

// Set to false to disable the mock API and use the proxy below
const USE_MOCK_API = true;

export default defineConfig({
  plugins: [tailwindcss(), sveltekit(), ...(USE_MOCK_API ? [mockApi()] : [])],
  server: {
    port: 3000,
    proxy: {
      '/volt/api': {
        target: 'http://192.168.43.1:8080',
        changeOrigin: true
      }
    }
  }
});
