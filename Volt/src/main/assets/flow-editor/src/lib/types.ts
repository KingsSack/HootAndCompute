// === API Response Types ===

export interface OpModeDefinition {
  id: string;
  name: string;
  type: OpModeType;
  robotId: string;
  flowGraph?: FlowGraph;
  constructorParams: Parameter;
}

export interface RobotMetadata {
  id: string;
  name: string;
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

export interface EventMetadata {
  id: string;
  name: string;
  opModeType: OpModeType;
  parameters: ParameterMetadata[];
}

export interface ParameterMetadata {
  name: string;
  type: string;
  defaultValue: string | null;
}

export interface Capabilities {
  actions: ActionMetadata[];
  events: EventMetadata[];
}

export interface FlowGraph {
  nodes: Node[];
  connections: Connection[];
}

export interface Node {
  id: string;
  label: string;
  type: NodeType;
  actionId?: string;
  eventId?: string;
  parameters: Parameter;
  position: Position;
  ports: Ports;
}

export interface Connection {
  id: string;
  sourceNode: string;
  sourcePort: string;
  targetNode: string;
  targetPort: string;
}

export interface Ports {
  inputs: string[];
  outputs: string[];
}

export interface GeneratedCode {
  code: string;
}

// === UI Types ===

export interface NodeTemplate {
  id: string;
  label: string;
  description?: string;
  type: NodeType;
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

export type OpModeType = 'AutonomousMode' | 'ManualMode';

export type NodeType = 'Action' | 'Event';

export interface Position {
  x: number;
  y: number;
}

export type Parameter = Record<string, string | number | boolean>;
