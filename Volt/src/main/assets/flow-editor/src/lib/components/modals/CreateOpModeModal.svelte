<script lang="ts">
  import { editorState } from '$lib/states/editor.svelte';
  import { opModeState } from '$lib/states/opmode.svelte';
  import { uiState } from '$lib/states/ui.svelte';
  import type { OpModeType, Parameter } from '$lib/types';
  import { fly } from 'svelte/transition';

  let newOpModeName = $state('');
  let newOpModeType = $state<OpModeType>('AutonomousMode');
  let selectedRobot = $state('');
  const constructorParamValues = $state<Parameter>({});

  let selectedRobotMetadata = $derived.by(() => {
    const metadata = editorState.availableRobots.find(
      (robot) => robot.simpleName === selectedRobot
    );
    if (metadata) return metadata;
    else return null;
  });
</script>

{#if uiState.showCreateModal}
  <div
    class="fixed inset-0 z-110 flex items-center justify-center bg-black/80 backdrop-blur-sm"
    transition:fly
  >
    <div
      class="w-full max-w-md overflow-hidden rounded-3xl border border-gray-800 bg-gray-900 shadow-2xl"
    >
      <div class="border-b border-gray-800 bg-linear-to-r from-indigo-600/20 to-purple-600/20 p-6">
        <h2 class="text-xl font-bold">Create New OpMode</h2>
        <p class="mt-1 text-sm text-gray-400">Configure your new operation mode</p>
      </div>

      <div class="space-y-5 p-6">
        <!-- OpMode Name -->
        <div class="space-y-2">
          <!-- svelte-ignore a11y_label_has_associated_control -->
          <label class="text-xs font-bold tracking-wider text-gray-400 uppercase">
            OpMode Name
          </label>
          <input
            type="text"
            placeholder="e.g., Red Alliance Auto"
            class="w-full rounded-xl border border-gray-700 bg-gray-800 px-4 py-3 text-sm transition-all outline-none focus:border-transparent focus:ring-2 focus:ring-indigo-500"
            bind:value={newOpModeName}
          />
        </div>

        <!-- OpMode Type -->
        <div class="space-y-2">
          <!-- svelte-ignore a11y_label_has_associated_control -->
          <label class="text-xs font-bold tracking-wider text-gray-400 uppercase">Type</label>
          <div class="grid grid-cols-2 gap-3">
            <button
              onclick={() => (newOpModeType = 'AutonomousMode')}
              class="rounded-xl border-2 p-4 text-left transition-all"
              class:classname={newOpModeType === 'AutonomousMode'
                ? 'border-indigo-500 bg-indigo-500/10'
                : 'border-gray-700 hover:border-gray-600'}
            >
              <div class="mb-2 flex h-8 w-8 items-center justify-center rounded-lg bg-green-600">
                <svg
                  class="h-5 w-5 text-white"
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
              <p class="text-sm font-bold">Autonomous</p>
              <p class="text-[10px] text-gray-500">Self-driving sequence</p>
            </button>
            <button
              onclick={() => (newOpModeType = 'ManualMode')}
              class="rounded-xl border-2 p-4 text-left transition-all {newOpModeType ===
              'ManualMode'
                ? 'border-indigo-500 bg-indigo-500/10'
                : 'border-gray-700 hover:border-gray-600'}"
            >
              <div class="mb-2 flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600">
                <svg
                  class="h-5 w-5 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122"
                  />
                </svg>
              </div>
              <p class="text-sm font-bold">TeleOp</p>
              <p class="text-[10px] text-gray-500">Driver-controlled</p>
            </button>
          </div>
        </div>

        <!-- Robot Selection -->
        {#if editorState.availableRobots.length > 0}
          <div class="space-y-2">
            <!-- svelte-ignore a11y_label_has_associated_control -->
            <label class="text-xs font-bold tracking-wider text-gray-400 uppercase">Robot</label>
            <select
              class="w-full rounded-xl border border-gray-700 bg-gray-800 px-4 py-3 text-sm transition-all outline-none focus:border-transparent focus:ring-2 focus:ring-indigo-500"
              bind:value={selectedRobot}
            >
              {#each editorState.availableRobots as robot (robot.simpleName)}
                <option value={robot.simpleName}>{robot.simpleName}</option>
              {/each}
            </select>
          </div>
        {/if}

        <!-- Constructor Parameters -->
        {#if selectedRobotMetadata && selectedRobotMetadata.constructorParams && selectedRobotMetadata.constructorParams.length > 0}
          <div class="space-y-3 border-t border-gray-800 pt-2">
            <!-- svelte-ignore a11y_label_has_associated_control -->
            <label class="text-xs font-bold tracking-wider text-gray-400 uppercase">
              Robot Configuration
            </label>
            {#each selectedRobotMetadata?.constructorParams as param (param.name)}
              <div class="space-y-1">
                <!-- svelte-ignore a11y_label_has_associated_control -->
                <label class="text-[10px] text-gray-500 uppercase">{param.name}</label>
                <input
                  type="text"
                  placeholder={param.defaultValue || ''}
                  bind:value={constructorParamValues[param.name]}
                  class="w-full rounded-lg border border-gray-700 bg-gray-800 px-3 py-2 text-sm transition-all outline-none focus:border-transparent focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            {/each}
          </div>
        {/if}
      </div>

      <div class="flex justify-end space-x-3 border-t border-gray-800 bg-gray-900/50 p-6">
        <button
          onclick={() => uiState.closeCreateModal()}
          class="rounded-xl bg-gray-800 px-5 py-2.5 text-sm font-medium transition-all hover:bg-gray-700"
        >
          Cancel
        </button>
        <button
          onclick={() => opModeState.createOpMode(newOpModeName, newOpModeType, selectedRobot)}
          class="rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-bold shadow-lg shadow-indigo-500/20 transition-all hover:bg-indigo-500"
        >
          Create OpMode
        </button>
      </div>
    </div>
  </div>
{/if}
