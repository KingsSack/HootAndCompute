import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
  preprocess: vitePreprocess(),
  kit: {
    adapter: adapter({
      pages: '../public',
      assets: '../public',
      fallback: 'index.html',
      strict: false
    }),
    paths: {
      base: '/volt'
    },
    appDir: '_app'
  },
  compilerOptions: {
    experimental: {
      async: true
    }
  }
};

export default config;
