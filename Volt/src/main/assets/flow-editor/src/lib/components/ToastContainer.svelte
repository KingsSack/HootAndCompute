<script lang="ts">
  import { uiState } from '$lib/states/ui.svelte';
  import { fade } from 'svelte/transition';
</script>

<div class="pointer-events-none fixed bottom-6 left-1/2 z-200 -translate-x-1/2 space-y-2">
  {#each uiState.toasts as toast (toast.id)}
    {#if toast.show}
      <div
        class="pointer-events-auto flex min-w-75 items-center space-x-3 rounded-2xl border border-gray-800 bg-gray-900 px-6 py-3 shadow-2xl"
        transition:fade
      >
        <div
          class="flex h-8 w-8 items-center justify-center rounded-full"
          class:classname={toast.type === 'success'
            ? 'bg-green-500/20 text-green-500'
            : 'bg-red-500/20 text-red-500'}
        >
          {#if toast.type === 'success'}
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 13l4 4L19 7"
              />
            </svg>
          {:else}
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          {/if}
        </div>
        <p class="text-sm font-medium">{toast.message}</p>
      </div>
    {/if}
  {/each}
</div>
