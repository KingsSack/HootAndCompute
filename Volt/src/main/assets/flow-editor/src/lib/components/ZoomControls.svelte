<script lang="ts">
  import { editorState } from '$lib/states/editor.svelte';

  let {
    zoomIn,
    zoomOut,
    resetView
  }: {
    zoomIn: () => void;
    zoomOut: () => void;
    resetView: () => void;
  } = $props();
</script>

<div class="absolute right-6 bottom-6 z-50 flex flex-col space-y-2">
  <div
    class="flex flex-col overflow-hidden rounded-xl border border-gray-800 bg-gray-900 p-1 shadow-2xl"
  >
    <button
      onclick={zoomIn}
      class="p-2 text-gray-400 transition-colors hover:bg-gray-800 hover:text-white"
      aria-label="Zoom In"
    >
      <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
    </button>
    <div class="h-px bg-gray-800"></div>
    <button
      onclick={zoomOut}
      class="p-2 text-gray-400 transition-colors hover:bg-gray-800 hover:text-white"
      aria-label="Zoom Out"
    >
      <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
      </svg>
    </button>
    <div class="h-px bg-gray-800"></div>
    <button
      onclick={resetView}
      class="p-2 text-gray-400 transition-colors hover:bg-gray-800 hover:text-white"
      title="Fit to screen"
    >
      <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"
        />
      </svg>
    </button>
  </div>
  <div
    class="rounded-xl border border-gray-800 bg-gray-900 px-3 py-1 font-mono text-[10px] text-gray-500 shadow-2xl"
  >
    <span>{Math.round(editorState.viewport.zoom * 100) + '%'}</span>
  </div>
</div>
