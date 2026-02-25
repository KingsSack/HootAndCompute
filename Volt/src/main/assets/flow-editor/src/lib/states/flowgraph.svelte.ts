import { apiClient } from '$lib/services/api';
import type {
  FlowGraphConnection,
  FlowGraph,
  FlowGraphNode,
  Position,
  Data,
  ActiveConnection,
  NodeType,
  Parameter
} from '$lib/types';
import { generateId } from '$lib/utils/id';

class FlowGraphState {
  nodes = $state<FlowGraphNode[]>([]);
  connections = $state<FlowGraphConnection[]>([]);

  selectedNode = $state<FlowGraphNode | null>(null);
  selectedConnection = $state<FlowGraphConnection | null>(null);

  activeConnection = $state<ActiveConnection | null>(null);

  draggingNode = $state<string | null>(null);
  dragOffset = $state<{ x: number; y: number }>({ x: 0, y: 0 });

  loadGraph(graph?: FlowGraph) {
    this.nodes = graph?.nodes || [];
    this.connections = graph?.connections || [];
    this.clearSelection();
  }

  exportGraph(): FlowGraph {
    return $state.snapshot({
      nodes: this.nodes,
      connections: this.connections
    });
  }

  // === Node Management ===

  async addNode(type: NodeType, label: string, position: Position, actionClass?: string) {
    let initialData: Data;
    if (type === 'action' && actionClass) {
      const action = await apiClient.getActionDetails(actionClass);

      const parameters = action.parameters.reduce((acc: Parameter, p) => {
        acc[p.name] = p.defaultValue ?? '';
        return acc;
      }, {});

      initialData = {
        label: label,
        parameters
      };
    } else if (type === 'button_trigger' || type === 'analog_trigger' || type === 'while_pressed') {
      initialData = {
        label: label,
        parameters: {
          trigger: ''
        }
      };
    } else {
      initialData = {
        label: label,
        parameters: {}
      };
    }

    const newNode: FlowGraphNode = {
      id: generateId(),
      type,
      position,
      data: initialData,
      ports: {
        inputs: [],
        outputs: []
      }
    };
    this.nodes.push(newNode);
    console.log('Adding node...');
    return newNode;
  }

  updateNodePosition(id: string, x: number, y: number) {
    const node = this.nodes.find((n) => n.id === id);
    if (node) node.position = { x, y };
  }

  updateNodeData(id: string, dataUpdates: Data) {
    const node = this.nodes.find((n) => n.id === id);
    if (node) node.data = { ...node.data, ...dataUpdates };
  }

  deleteNode(id: string) {
    this.nodes = this.nodes.filter((n) => n.id !== id);

    this.connections = this.connections.filter(
      (connection) => connection.sourceNode !== id && connection.targetNode !== id
    );

    if (this.selectedNode?.id === id) this.clearSelection();
  }

  deleteSelectedNode() {
    this.nodes = this.nodes.filter((n) => n.id !== this.selectedNode?.id);
    this.clearSelection();
  }

  startDrag(id: string, offsetX: number, offsetY: number) {
    this.draggingNode = id;
    this.dragOffset = { x: offsetX, y: offsetY };
  }

  stopDrag() {
    this.draggingNode = null;
  }

  // === Connection Management ===

  addConnection(sourceId: string, targetId: string) {
    if (sourceId === targetId) return null;

    const exists = this.connections.some(
      (c) => c.sourceNode === sourceId && c.targetNode === targetId
    );
    if (exists) return null;

    const newConnection: FlowGraphConnection = {
      id: generateId(),
      sourceNode: sourceId,
      sourcePort: 'output_0',
      targetNode: targetId,
      targetPort: 'input_0'
    };

    this.connections.push(newConnection);
    return newConnection;
  }

  deleteConnection(id: string) {
    this.connections = this.connections.filter((connection) => connection.id !== id);
    if (this.selectedConnection?.id === id) this.selectedConnection = null;
  }

  // === Selection ===

  selectNode(id: string) {
    this.clearSelection();
    const node = this.nodes.find((node) => node.id === id);
    if (node) this.selectedNode = node;
  }

  selectConnection(id: string) {
    this.clearSelection();
    const connection = this.connections.find((connection) => connection.id === id);
    if (connection) this.selectedConnection = connection;
  }

  clearSelection() {
    this.selectedNode = null;
    this.selectedConnection = null;
  }
}

export const flowGraphState = new FlowGraphState();
