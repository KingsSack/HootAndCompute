<script lang="ts">
  import { editorState } from '$lib/states/editor.svelte';
  import { flowGraphState } from '$lib/states/flowgraph.svelte';
  import type { FlowGraphConnection } from '$lib/types';
  import { calculateBezier, getPortPosition } from '$lib/utils/geometry';

  function getConnectionPath(conn: FlowGraphConnection) {
    const sourceNode = flowGraphState.nodes.find((n) => n.id === conn.sourceNode);
    const targetNode = flowGraphState.nodes.find((n) => n.id === conn.targetNode);
    if (!sourceNode || !targetNode) return '';
    const from = getPortPosition(sourceNode, 'output');
    const to = getPortPosition(targetNode, 'input');
    return calculateBezier(from.x, from.y, to.x, to.y);
  }
</script>

<!-- Connections Layer -->
<div class="pointer-events-none absolute inset-0 overflow-visible">
  {#each flowGraphState.connections as conn (`${editorState.currentOpMode?.id}-${conn.sourceNode}-${conn.targetNode}`)}
    <svg class="pointer-events-none absolute inset-0 h-full w-full overflow-visible">
      <g
        class="pointer-events-auto cursor-pointer"
        onclick={() => flowGraphState.deleteConnection(conn.id)}
      >
        <!-- Invisible wider hit-area for easier clicking -->
        <path
          d={getConnectionPath(conn)}
          stroke="transparent"
          stroke-width="16"
          fill="none"
          vector-effect="non-scaling-stroke"
        />
        <!-- Visible connection path -->
        <path
          d={getConnectionPath(conn)}
          stroke="rgba(99, 102, 241, 0.6)"
          stroke-width="3"
          fill="none"
          vector-effect="non-scaling-stroke"
          class="transition-all hover:stroke-indigo-400"
        />
      </g>
    </svg>
  {/each}
</div>

<!-- Active Connections Layer -->
<svg class="pointer-events-none absolute inset-0 h-full w-full overflow-visible">
  {#if flowGraphState.activeConnection}
    {@const conn = flowGraphState.activeConnection}
    <path
      d={calculateBezier(conn.fromPos.x, conn.fromPos.y, conn.to.x, conn.to.y)}
      stroke="rgba(99, 102, 241, 0.5)"
      stroke-width="3"
      fill="none"
      stroke-dasharray="5,5"
      class="animate-dash"
      vector-effect="non-scaling-stroke"
    />
  {/if}
</svg>
