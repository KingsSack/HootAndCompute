<script lang="ts">
  import { fly } from 'svelte/transition';
  import { editorState } from '$lib/states/editor.svelte';
  import { uiState } from '$lib/states/ui.svelte';
</script>

{#if uiState.showOpModeModal}
  <div
    class="fixed inset-0 z-100 flex items-center justify-center bg-black/80 backdrop-blur-sm"
    transition:fly
  >
    <div
      class="flex max-h-[80vh] w-full max-w-2xl flex-col overflow-hidden rounded-3xl border border-gray-800 bg-gray-900 shadow-2xl"
    >
      <div class="flex items-center justify-between border-b border-gray-800 bg-indigo-600/10 p-8">
        <div>
          <h2 class="text-2xl font-bold">Select OpMode</h2>
          <p class="mt-1 text-sm text-gray-400">Choose an existing flow or create a new one.</p>
        </div>
        <div class="flex space-x-2">
          <button
            onclick={() => uiState.openCreateModal()}
            class="flex items-center rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-bold shadow-xl shadow-indigo-500/20 transition-all hover:bg-indigo-500"
          >
            <svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4v16m8-8H4"
              />
            </svg>
            New OpMode
          </button>
        </div>
      </div>

      <div class="grid flex-1 grid-cols-2 gap-4 overflow-y-auto p-4">
        {#each editorState.availableOpModes as mode (mode.id)}
          <button
            onclick={() => {
              editorState.loadOpMode(mode.id);
              uiState.closeOpModeModal();
            }}
            class="group relative flex flex-col overflow-hidden rounded-2xl border border-gray-700 bg-gray-800/50 p-5 text-left transition-all hover:border-indigo-500/50 hover:bg-gray-800"
            aria-label="Select OpMode"
          >
            <div
              class="absolute top-0 right-0 p-3 opacity-0 transition-opacity group-hover:opacity-100"
            >
              <svg class="h-5 w-5 text-indigo-400" fill="currentColor" viewBox="0 0 20 20">
                <path
                  d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                />
              </svg>
            </div>
            <div
              class="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-gray-700 transition-colors group-hover:bg-indigo-600"
            >
              <svg
                class="h-6 w-6 text-gray-400 group-hover:text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                />
              </svg>
            </div>
            <h3 class="mb-1 text-lg font-bold">{mode.name}</h3>
            <span class="text-xs text-gray-500">{mode.type}</span>
          </button>
        {/each}
      </div>
    </div>
  </div>
{/if}
