import type { NodeType } from './types';

export const icons: Record<NodeType, string> = {
  Event:
    '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>',
  Action:
    '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>'
};

export const colors: Record<NodeType, string> = {
  Event: 'bg-green-600',
  Action: 'bg-blue-600'
};
