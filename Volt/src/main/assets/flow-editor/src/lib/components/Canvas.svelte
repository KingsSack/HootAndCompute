<script lang="ts">
  import { editorState } from '$lib/states/editor.svelte';
  import { flowGraphState } from '$lib/states/flowgraph.svelte';
  import type { NodeTemplate, Position } from '$lib/types';
  import { getPortPosition, screenToCanvas } from '$lib/utils/geometry';
  import CanvasNode from './CanvasNode.svelte';
  import ConnectionsLayer from './ConnectionsLayer.svelte';
  import ZoomControls from './ZoomControls.svelte';

  let canvasContainer: HTMLDivElement;

  const gridStyle = $derived.by(() => {
    const size = 20 * editorState.viewport.zoom;
    const ox = editorState.viewport.panX % size;
    const oy = editorState.viewport.panY % size;
    return `background-image: radial-gradient(circle, rgba(255,255,255,0.08) 1px, transparent 1px); background-size: ${size}px ${size}px; background-position: ${ox}px ${oy}px;`;
  });

  const canvasStyle = $derived(
    `transform: translate(${editorState.viewport.panX}px, ${editorState.viewport.panY}px) scale(${editorState.viewport.zoom});`
  );

  function handleDragOver(e: DragEvent) {
    e.preventDefault();
    if (e.dataTransfer) e.dataTransfer.dropEffect = 'copy';
  }

  function handleDrop(e: DragEvent) {
    e.preventDefault();

    if (e.dataTransfer) {
      const data = e.dataTransfer.getData('application/json');
      if (!canvasContainer) return;

      const template: NodeTemplate = JSON.parse(data);
      const pos = screenToCanvas(e.clientX, e.clientY, canvasContainer, editorState.viewport);

      flowGraphState.addNode(template.type, template.label, pos, template.actionClass);
    }
  }

  let isPanning = $state(false);
  let panStart = $state({ x: 0, y: 0 });

  function handleMouseDown(e: MouseEvent) {
    if (e.button === 1 || (e.button === 0 && e.target === canvasContainer)) {
      isPanning = true;
      panStart = {
        x: e.clientX - editorState.viewport.panX,
        y: e.clientY - editorState.viewport.panY
      };
      e.preventDefault();
    }
  }

  function handleMouseMove(e: MouseEvent) {
    if (isPanning) {
      editorState.viewport.panX = e.clientX - panStart.x;
      editorState.viewport.panY = e.clientY - panStart.y;
    }

    if (flowGraphState.activeConnection && canvasContainer) {
      const pos = screenToCanvas(e.clientX, e.clientY, canvasContainer, editorState.viewport);

      flowGraphState.activeConnection = {
        ...flowGraphState.activeConnection,
        to: pos
      };
    }

    if (flowGraphState.draggingNode) {
      const pos = screenToCanvas(e.clientX, e.clientY, canvasContainer, editorState.viewport);
      flowGraphState.updateNodePosition(
        flowGraphState.draggingNode,
        pos.x - flowGraphState.dragOffset.x,
        pos.y - flowGraphState.dragOffset.y
      );
    }
  }

  function handleMouseUp(e: MouseEvent) {
    if (isPanning) {
      isPanning = false;
    }

    if (flowGraphState.activeConnection) {
      const pos = screenToCanvas(e.clientX, e.clientY, canvasContainer, editorState.viewport);
      const targetNode = flowGraphState.nodes.find((n) => {
        const dx = pos.x - n.position.x;
        const dy = pos.y - (n.position.y + 60);
        return dx >= -20 && dx <= 10 && dy >= -20 && dy <= 20;
      });

      if (targetNode && targetNode.id !== flowGraphState.activeConnection.fromId) {
        flowGraphState.addConnection(flowGraphState.activeConnection.fromId, targetNode.id);
      }
      flowGraphState.activeConnection = null;
    }

    if (flowGraphState.draggingNode) {
      flowGraphState.stopDrag();
    }
  }

  function handleWheel(e: WheelEvent) {
    e.preventDefault();
    const delta = e.deltaY > 0 ? -0.05 : 0.05;
    const newZoom = Math.min(3, Math.max(0.2, editorState.viewport.zoom + delta));
    editorState.viewport.zoom = newZoom;
  }

  function startDraggingNode(id: string, nodePos: Position, e: MouseEvent) {
    const pos = screenToCanvas(e.clientX, e.clientY, canvasContainer, editorState.viewport);
    flowGraphState.startDrag(id, pos.x - nodePos.x, pos.y - nodePos.y);
  }

  function onPortDrag(id: string, nodePos: Position, portType: 'input' | 'output', e: MouseEvent) {
    if (portType === 'input') {
      const existingConn = flowGraphState.connections.find((c) => c.sourceNode === id);
      if (existingConn) {
        flowGraphState.activeConnection = {
          fromId: existingConn.sourceNode,
          fromPos: getPortPosition(nodePos, 'input'),
          to: screenToCanvas(e.clientX, e.clientY, canvasContainer, editorState.viewport)
        };
        flowGraphState.deleteConnection(existingConn.id);
        return;
      }
    }

    const portPos = getPortPosition(nodePos, portType);
    flowGraphState.activeConnection = {
      fromId: id,
      fromPos: portPos,
      to: portPos
    };
  }
</script>

<div
  class="relative flex-1 cursor-crosshair overflow-hidden bg-gray-950 select-none"
  ondragover={handleDragOver}
  ondrop={handleDrop}
  onmousedown={handleMouseDown}
  onmousemove={handleMouseMove}
  onmouseup={handleMouseUp}
  onwheel={handleWheel}
  bind:this={canvasContainer}
>
  <div class="pointer-events-none absolute inset-0" style={gridStyle}></div>

  <div class="pointer-events-none absolute inset-0 origin-top-left" style={canvasStyle}>
    <ConnectionsLayer />

    {#each flowGraphState.nodes as node (node.id)}
      <CanvasNode
        {node}
        ondelete={(id) => flowGraphState.deleteNode(id)}
        onselect={(id) => flowGraphState.selectNode(id)}
        onstartdrag={(id, nodePos, e) => startDraggingNode(id, nodePos, e)}
        onportdrag={(id, nodePos, portType, e) => onPortDrag(id, nodePos, portType, e)}
      />
    {/each}
  </div>

  <ZoomControls
    zoomIn={() => editorState.zoomIn()}
    zoomOut={() => editorState.zoomOut()}
    resetView={() => editorState.resetView()}
  />
</div>
