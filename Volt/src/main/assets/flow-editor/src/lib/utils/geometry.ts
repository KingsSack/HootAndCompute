import type { FlowGraphNode, Position, Viewport } from '$lib/types';

export function calculateBezier(x1: number, y1: number, x2: number, y2: number): string {
  const dx = Math.abs(x2 - x1) * 0.5;
  return `M ${x1} ${y1} C ${x1 + dx} ${y1}, ${x2 - dx} ${y2}, ${x2} ${y2}`;
}

export function screenToCanvas(
  screenX: number,
  screenY: number,
  container: HTMLElement,
  viewport: Viewport
): { x: number; y: number } {
  const rect = container.getBoundingClientRect();
  return {
    x: (screenX - rect.left - viewport.panX) / viewport.zoom,
    y: (screenY - rect.top - viewport.panY) / viewport.zoom
  };
}

export function getPortPosition(node: FlowGraphNode, portType: 'input' | 'output'): Position {
  const x = portType === 'output' ? node.position.x + 220 : node.position.x;

  const paramCount = Object.keys(node.data.parameters || {}).length;
  const height = paramCount === 0 ? 120 : 73 + 67 * paramCount;

  const y = node.position.y + height / 2;
  return { x, y };
}
