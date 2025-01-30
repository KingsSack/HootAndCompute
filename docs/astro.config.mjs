// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

import markdoc from '@astrojs/markdoc';

// https://astro.build/config
export default defineConfig({
    integrations: [starlight({
        title: 'HootAndCompute Docs',
        logo: {
            src: './src/assets/favicon.svg',
        },
        social: {
            github: 'https://github.com/KingsSack/HootAndCompute',
            youtube: 'https://www.youtube.com/@HootAndCompute',
        },
        tableOfContents: {
            minHeadingLevel: 2,
            maxHeadingLevel: 5,
        },
        editLink: {
            baseUrl: 'https://github.com/KingsSack/HootAndCompute/edit/master/docs/',
        },
        sidebar: [
            {
                label: 'Getting Started',
                autogenerate: { directory: 'getting-started' },
            },
            {
                label: 'Guides',
                autogenerate: { directory: 'guides' },
            },
            {
                label: 'Simple Attachments',
                collapsed: true,
                autogenerate: { directory: 'simple-attachments' },
            },
            {
                label: 'Simple Robots',
                collapsed: true,
                autogenerate: { directory: 'simple-robots' },
            },
            {
                label: 'Simple Autonomous Modes',
                collapsed: true,
                autogenerate: { directory: 'simple-autonomous-modes' },
            },
            {
                label: 'Simple Manual Modes',
                collapsed: true,
                autogenerate: { directory: 'simple-manual-modes' },
            },
            {
            	label: 'Reference',
            	autogenerate: { directory: 'reference' },
            },
        ],
        customCss: [
            './src/styles/theme.css',
            './src/styles/landing.css',
        ],
		}), markdoc()],
});