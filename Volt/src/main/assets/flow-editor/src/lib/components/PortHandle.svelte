<script lang="ts">
  import type { FlowGraphNode, Position } from '$lib/types';

  let {
    node,
    portType,
    onportdrag
  }: {
    node: FlowGraphNode;
    portType: 'input' | 'output';
    onportdrag: (
      id: string,
      nodePos: Position,
      portType: 'input' | 'output',
      e: MouseEvent
    ) => void;
  } = $props();
</script>

<div class="pointer-events-none absolute inset-0">
  <button
    class={portType === 'input'
      ? 'group pointer-events-auto absolute top-1/2 -left-3 flex h-8 w-8 -translate-y-1/2 cursor-crosshair items-center justify-center'
      : 'group pointer-events-auto absolute top-1/2 -right-3 flex h-8 w-8 -translate-y-1/2 cursor-crosshair items-center justify-center'}
    onmousedown={(e) => onportdrag(node.id, node.position, portType, e)}
    aria-label="Input Port"
  >
    <div
      class="h-4 w-4 rounded-full border-2 border-indigo-500 bg-gray-800 shadow-lg shadow-indigo-500/20 transition-all group-hover:bg-indigo-500"
      id={portType === 'input' ? `port-in-${node.id}` : `port-out-${node.id}`}
    ></div>
  </button>
</div>
