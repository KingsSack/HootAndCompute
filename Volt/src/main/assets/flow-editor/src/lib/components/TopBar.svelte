<script lang="ts">
  import { editorState } from '$lib/states/editor.svelte';
</script>

<header
  class="z-50 flex h-16 items-center justify-between border-b border-gray-800 bg-gray-900/50 px-6 backdrop-blur-md"
>
  <div class="flex items-center space-x-4">
    <div
      class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-indigo-600 shadow-lg shadow-indigo-500/20"
    >
      <svg class="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M13 10V3L4 14h7v7l9-11h-7z"
        />
      </svg>
    </div>
    <div class="min-w-0">
      <h1 class="truncate text-lg leading-tight font-bold">
        {editorState.currentOpMode?.name || 'Volt Flow Editor'}
      </h1>
      <p class="truncate text-xs text-gray-400">
        {editorState.currentOpMode?.type || 'Select or create an OpMode'}
      </p>
    </div>
  </div>

  <div class="flex items-center space-x-3">
    <button
      onclick={() => editorState.generateCode()}
      class="flex items-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-bold shadow-lg shadow-indigo-500/20 transition-all hover:bg-indigo-500 disabled:cursor-wait disabled:bg-indigo-800"
    >
      {#if !editorState.isGenerating}
        <svg class="mr-2 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
          <path d="M4.5 3.5v13L16 10l-11.5-6.5z" />
        </svg>
      {:else}
        <svg class="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
          ></circle>
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
      {/if}

      <span>{editorState.isGenerating ? 'Generating...' : 'Deploy'}</span>
    </button>
  </div>
</header>
