import { apiClient } from '$lib/services/api';
import type {
  ActiveConnection,
  Connection,
  FlowGraph,
  Node,
  NodeType,
  Parameter,
  Position
} from '$lib/types';
import { generateId } from '$lib/utils/id';

class FlowGraphState {
  nodes = $state<Node[]>([]);
  connections = $state<Connection[]>([]);

  selectedNode = $state<Node | null>(null);
  selectedConnection = $state<Connection | null>(null);

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

  async addNode(type: NodeType, label: string, position: Position, id: string) {
    let newNode: Node;
    if (type === 'Action') {
      const action = await apiClient.getActionDetails(id);

      const parameters = action.parameters.reduce((acc: Parameter, p) => {
        acc[p.name] = p.defaultValue ?? '';
        return acc;
      }, {});

      newNode = {
        id: generateId(),
        label,
        type,
        actionId: id,
        parameters: parameters,
        position,
        ports: {
          inputs: [],
          outputs: []
        }
      };
    } else {
      const event = await apiClient.getEventDetails(id);

      const parameters = event.parameters.reduce((acc: Parameter, p) => {
        acc[p.name] = p.defaultValue ?? '';
        return acc;
      }, {});

      newNode = {
        id: generateId(),
        label,
        type,
        eventId: id,
        parameters: parameters,
        position,
        ports: {
          inputs: [],
          outputs: []
        }
      };
    }

    this.nodes.push(newNode);
    return newNode;
  }

  updateNodePosition(id: string, x: number, y: number) {
    const node = this.nodes.find((n) => n.id === id);
    if (node) node.position = { x, y };
  }

  updateNodeParams(id: string, paramUpdates: Parameter) {
    const node = this.nodes.find((n) => n.id === id);
    if (node) node.parameters = { ...node.parameters, ...paramUpdates };
  }

  deleteNode(id: string) {
    this.nodes = this.nodes.filter((n) => n.id !== id);

    this.connections = this.connections.filter(
      (connection) => connection.sourceNode !== id && connection.targetNode !== id
    );

    if (this.selectedNode?.id === id) this.clearSelection();
  }

  deleteSelectedNode() {
    if (!this.selectedNode) return;
    this.deleteNode(this.selectedNode.id);
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

    const newConnection: Connection = {
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
