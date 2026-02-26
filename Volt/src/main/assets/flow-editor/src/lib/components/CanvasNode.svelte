<script lang="ts">
  import { colors, icons } from '$lib';
  import { flowGraphState } from '$lib/states/flowgraph.svelte';
  import type { FlowGraphNode, Position } from '$lib/types';
  import ParameterEditor from './ParameterEditor.svelte';
  import PortHandle from './PortHandle.svelte';

  function handleMouseDown(e: MouseEvent) {
    if (e.button !== 0) return;
    e.stopPropagation();
    onselect(node.id);
    onstartdrag(node.id, node.position, e);
  }

  let {
    node,
    ondelete,
    onselect,
    onstartdrag,
    onportdrag
  }: {
    node: FlowGraphNode;
    ondelete: (id: string) => void;
    onselect: (id: string) => void;
    onstartdrag: (id: string, nodePos: Position, e: MouseEvent) => void;
    onportdrag: (node: FlowGraphNode, portType: 'input' | 'output', e: MouseEvent) => void;
  } = $props();
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
  class="pointer-events-auto absolute min-w-55 rounded-2xl border-2 bg-gray-900/80 shadow-2xl backdrop-blur-xl select-none hover:shadow-indigo-500/10 {flowGraphState.selectedNode ===
  node
    ? 'border-indigo-500 ring-4 ring-indigo-500/10'
    : 'border-gray-800 hover:border-gray-700'}"
  style={`left: ${node.position.x}px; top: ${node.position.y}px; transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s, ring 0.2s;`}
  onmousedown={handleMouseDown}
>
  <!-- Node Header -->
  <div class="flex items-center border-b border-gray-800/50 bg-gray-800/20 p-3.5">
    <div
      class="mr-3 flex h-7 w-7 shrink-0 items-center justify-center rounded-lg shadow-inner {colors[
        node.type
      ]}"
    >
      <!-- <div class="mr-3 flex h-7 w-7 shrink-0 items-center justify-center rounded-lg shadow-inner"> -->
      <svg class="h-4 w-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        {@html icons[node.type]}
      </svg>
      <!-- </div> -->
    </div>
    <span class="min-w-0 flex-1 truncate text-sm font-bold tracking-tight">
      {node.data.label}
    </span>
    <button
      onclick={() => ondelete(node.id)}
      class="rounded-lg p-1.5 text-gray-500 transition-all hover:bg-red-500/10 hover:text-red-400"
      aria-label="Delete Node"
    >
      <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
        />
      </svg>
    </button>
  </div>

  {#if node.type === 'control' || node.type === 'action' || node.type === 'end'}
    <PortHandle
      {node}
      portType="input"
      onportdrag={(node, portType, e) => onportdrag(node, portType, e)}
    />
  {/if}

  {#if node.type === 'control' || node.type === 'action' || node.type === 'start' || node.type === 'button_trigger' || node.type === 'analog_trigger' || node.type === 'while_pressed'}
    <PortHandle
      {node}
      portType="output"
      onportdrag={(node, portType, e) => onportdrag(node, portType, e)}
    />
  {/if}

  <ParameterEditor {node} />
</div>
