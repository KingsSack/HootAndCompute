// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

import markdoc from '@astrojs/markdoc';

// https://astro.build/config
export default defineConfig({
    integrations: [starlight({
        title: 'HootAndCompute Docs',
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
            	label: 'Reference',
            	autogenerate: { directory: 'reference' },
            },
            {
            	label: 'Volt API Reference',
                collapsed: true,
            	autogenerate: { directory: 'volt' },
            },
            {
            	label: 'TeamCode API Reference',
                collapsed: true,
            	autogenerate: { directory: 'teamcode' },
            },
        ],
        customCss: [
            './src/styles/theme.css',
        ],
		}), markdoc()],
});