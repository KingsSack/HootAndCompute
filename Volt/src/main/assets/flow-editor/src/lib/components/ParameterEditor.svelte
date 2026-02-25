<script lang="ts">
  import { editorState } from '$lib/states/editor.svelte';
  import type { FlowGraphNode } from '$lib/types';

  let { node }: { node: FlowGraphNode } = $props();

  function getParamDef(paramName: string) {
    if (node.type === 'action') {
      const action = editorState.availableActions.find((a) => a.name === node.data.label);
      const type = action?.parameters.find((p) => p.name === paramName)?.type;
      if (type) return { type: type };
    }
    if (node.type === 'control') {
      if (paramName === 'duration') return { type: 'Double' };
    }
    if (node.type.endsWith('_trigger') || node.type === 'while_pressed') {
      if (paramName === 'button') return { type: 'GamepadButton' };
      if (paramName === 'input') return { type: 'GamepadAnalogInput' };
      if (paramName === 'event') return { type: 'Enum' }; // onTap, etc.
      if (paramName === 'threshold') return { type: 'Float' };
    }
    return null;
  }

  function getInputType(paramName: string) {
    const def = getParamDef(paramName);
    if (!def) return 'text';

    switch (def.type) {
      case 'Boolean':
        return 'checkbox';
      case 'Int':
      case 'Long':
        return 'number';
      case 'Double':
      case 'Float':
        return 'number';
      case 'GamepadButton':
        return 'select'; // Special select
      case 'GamepadAnalogInput':
        return 'select';
      case 'Enum':
        return 'select'; // Events
      default:
        return 'text';
    }
  }

  function getStep(paramName: string) {
    const def = getParamDef(paramName);
    if (!def) return 'any';
    if (def.type === 'Int' || def.type === 'Long') return '1';
    return '0.1';
  }

  function getOptions(paramName: string) {
    const def = getParamDef(paramName);
    if (!def) return [];

    if (def.type === 'GamepadButton') {
      return [
        'A1',
        'B1',
        'X1',
        'Y1',
        'DPAD_UP1',
        'DPAD_DOWN1',
        'DPAD_LEFT1',
        'DPAD_RIGHT1',
        'LEFT_BUMPER1',
        'RIGHT_BUMPER1'
      ];
    }
    if (def.type === 'GamepadAnalogInput') {
      return [
        'LEFT_STICK_X1',
        'LEFT_STICK_Y1',
        'RIGHT_STICK_X1',
        'RIGHT_STICK_Y1',
        'LEFT_TRIGGER1',
        'RIGHT_TRIGGER1'
      ];
    }
    if (def.type === 'Enum' && paramName === 'event') {
      if (node.type === 'button_trigger') return ['onTap', 'onRelease', 'onDoubleTap', 'onHold'];
      if (node.type === 'analog_trigger') return ['onChange', 'whenAbove', 'whenBelow'];
    }
    return [];
  }
</script>

<div class="space-y-4 p-4">
  {#each Object.entries(node.data.parameters) as [key] (key)}
    <div class="space-y-1.5">
      <p class="pl-1 text-[10px] font-bold tracking-widest text-gray-500 uppercase">
        {key}
      </p>

      {#if getInputType(key) === 'checkbox'}
        <div class="flex items-center space-x-2">
          <input
            type="checkbox"
            class="h-4 w-4 rounded border-gray-700 bg-gray-900 text-indigo-600 focus:ring-indigo-500"
            bind:checked={node.data.parameters[key]}
          />
          <span class="text-xs text-gray-400">Enabled</span>
        </div>
      {:else if getInputType(key) === 'select'}
        <select
          class="w-full rounded-lg border border-gray-800 bg-gray-950/50 px-3 py-1.5 font-mono text-xs text-indigo-100 transition-all outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/50"
          bind:value={node.data.parameters[key]}
        >
          {#each getOptions(key) as opt (opt)}
            <option value={opt}>{opt}</option>
          {/each}
        </select>
      {:else}
        <input
          type={getInputType(key)}
          step={getStep(key)}
          class="w-full rounded-lg border border-gray-800 bg-gray-950/50 px-3 py-1.5 font-mono text-xs text-indigo-100 placeholder-gray-700 transition-all outline-none select-text focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/50"
          bind:value={node.data.parameters[key]}
        />
      {/if}
    </div>
  {/each}

  {#if Object.keys(node.data.parameters || {}).length === 0}
    <div class="py-2 text-center text-[10px] text-gray-600 italic">No configuration required</div>
  {/if}
</div>
