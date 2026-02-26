<script lang="ts">
  import { colors, icons } from '$lib';
  import { editorState } from '$lib/states/editor.svelte';
  import { sidebarState } from '$lib/states/sidebar.svelte';
  import type { NodeTemplate } from '$lib/types';

  let nodeSearch = $state('');

  function filterNodes(nodes: NodeTemplate[]) {
    if (!nodeSearch) return nodes;
    return nodes.filter(
      (n) =>
        n.label.toLowerCase().includes(nodeSearch.toLowerCase()) ||
        n.description.toLowerCase().includes(nodeSearch.toLowerCase())
    );
  }

  function handleDragStart(e: DragEvent, node: NodeTemplate) {
    if (e.dataTransfer) {
      e.dataTransfer.setData('application/json', JSON.stringify(node));
      e.dataTransfer.effectAllowed = 'copy';
    }
  }
</script>

<aside
  class="z-40 flex w-72 flex-col border-r border-gray-800 bg-gray-900 transition-all duration-300"
>
  <div class="border-b border-gray-800 p-4">
    <div class="relative">
      <input
        type="text"
        placeholder="Search nodes..."
        class="w-full rounded-lg border border-gray-700 bg-gray-800 py-2 pr-4 pl-9 text-sm outline-none select-text focus:border-transparent focus:ring-2 focus:ring-indigo-500"
        bind:value={nodeSearch}
      />
      <svg
        class="absolute top-2.5 left-3 h-4 w-4 text-gray-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
        />
      </svg>
    </div>
  </div>

  <div class="flex-1 space-y-6 overflow-y-auto p-4">
    {#if editorState.isLoading}
      <div class="flex items-center justify-center py-8">
        <svg class="h-6 w-6 animate-spin text-indigo-500" fill="none" viewBox="0 0 24 24">
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        <span class="ml-2 text-sm text-gray-400">Loading actions...</span>
      </div>
    {/if}

    {#each sidebarState.categories as category (category.name)}
      <div>
        <h3 class="mb-3 px-2 text-xs font-bold tracking-wider text-gray-500 uppercase">
          {category.name}
        </h3>

        <div class="space-y-2">
          {#each filterNodes(category.nodes) as node (node.label)}
            <!-- svelte-ignore a11y_no_static_element_interactions -->
            <div
              draggable="true"
              ondragstart={(e) => handleDragStart(e, node)}
              class="group flex cursor-grab items-center rounded-xl border border-gray-700 bg-gray-800 p-3 transition-colors select-none active:cursor-grabbing"
            >
              <div
                class="mr-3 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg {colors[
                  node.type
                ]}"
              >
                <svg
                  class="h-5 w-5 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  {@html icons[node.type]}
                </svg>
              </div>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium">{node.label}</p>
                <p class="truncate text-[10px] text-gray-500">{node.description}</p>
              </div>
            </div>
          {/each}
        </div>

        {#if filterNodes(category.nodes).length === 0 && nodeSearch}
          <div class="py-4 text-center text-xs text-gray-600 italic">No matching nodes</div>
        {/if}
      </div>
    {/each}
  </div>
</aside>
