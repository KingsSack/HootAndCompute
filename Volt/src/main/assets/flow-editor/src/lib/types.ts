// === API Response Types ===

export interface OpModeMetadata {
  id: string;
  name: string;
  type: OpModeType;
  robotId: string;
  flowGraph?: FlowGraph;
}

export interface RobotMetadata {
  simpleName: string;
  qualifiedName: string;
  actions: ActionMetadata[];
  constructorParams: ParameterMetadata[];
  typeSignature: string;
  factoryExpression: string;
}

export interface ActionMetadata {
  id: string;
  name: string;
  description: string;
  enableAITool: boolean;
  parameters: ParameterMetadata[];
  declaringClass: string;
  accessPath: string;
}

export interface ParameterMetadata {
  name: string;
  type: string;
  defaultValue: string | null;
}

export interface FlowGraph {
  nodes: FlowGraphNode[];
  connections: FlowGraphConnection[];
}

export interface FlowGraphNode {
  id: string;
  type: NodeType;
  actionClass?: string;
  position: Position;
  data: Data;
  ports: {
    inputs: string[];
    outputs: string[];
  };
}

export interface FlowGraphConnection {
  id: string;
  sourceNode: string;
  sourcePort: string;
  targetNode: string;
  targetPort: string;
}

// === UI Types ===

export interface NodeCategory {
  name: string;
  nodes: NodeTemplate[];
}

export interface NodeTemplate {
  type: NodeType;
  label: string;
  description: string;
  actionClass?: string;
}

export interface Viewport {
  panX: number;
  panY: number;
  zoom: number;
}

export interface ActiveConnection {
  fromId: string;
  fromPos: { x: number; y: number };
  to: { x: number; y: number };
}

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error';
  show: boolean;
}

// === Other ===

export type NodeType =
  | 'start'
  | 'end'
  | 'action'
  | 'control'
  | 'button_trigger'
  | 'analog_trigger'
  | 'while_pressed';

export type OpModeType = 'AutonomousMode' | 'ManualMode';

export interface Position {
  x: number;
  y: number;
}

export interface Data {
  label: string;
  parameters: Parameter;
}

export type Parameter = Record<string, string | number | boolean>;
