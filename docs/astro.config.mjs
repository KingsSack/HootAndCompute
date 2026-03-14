// @ts-check
import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";

import markdoc from "@astrojs/markdoc";

// https://astro.build/config
export default defineConfig({
  integrations: [
    starlight({
      title: "Volt",
      logo: {
        src: "./src/assets/favicon.svg",
      },
      social: [
        {
          icon: "github",
          label: "GitHub",
          href: "https://github.com/KingsSack/HootAndCompute",
        },
        {
          icon: "youtube",
          label: "YouTube",
          href: "https://www.youtube.com/@HootAndCompute",
        },
      ],
      tableOfContents: {
        minHeadingLevel: 2,
        maxHeadingLevel: 5,
      },
      editLink: {
        baseUrl:
          "https://github.com/KingsSack/HootAndCompute/edit/master/docs/",
      },
      sidebar: [
        {
          label: "Getting Started",
          autogenerate: { directory: "getting-started" },
        },
        {
          label: "Guides",
          autogenerate: { directory: "guides" },
        },
        {
          label: "Concepts",
          autogenerate: { directory: "concepts" },
        },
        {
          label: "Reference",
          autogenerate: { directory: "reference" },
        },
      ],
      customCss: ["./src/styles/theme.css", "./src/styles/landing.css"],
    }),
    markdoc(),
  ],
});
