import type {
  ActionMetadata,
  Capabilities,
  EventMetadata,
  FlowGraph,
  GeneratedCode,
  OpModeDefinition,
  OpModeType,
  RobotMetadata
} from '$lib/types';

const API_BASE = '/volt/api';

function withQuery(endpoint: string, params: Record<string, string>): string {
  const searchParams = new URLSearchParams(params);
  return `${endpoint}?${searchParams.toString()}`;
}

async function fetcher<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers
    }
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status} - ${response.statusText}`);
  }

  if (response.status === 204) return null as T;
  return response.json();
}

export const apiClient = {
  getOpModes: () => fetcher<OpModeDefinition[]>('/opmodes'),
  getOpMode: (id: string) => fetcher<OpModeDefinition>(withQuery('/opmodes', { id })),
  createOpMode: (data: Omit<OpModeDefinition, 'id' | 'flowGraph'>) =>
    fetcher<{ id: string }>('/opmodes', { method: 'POST', body: JSON.stringify(data) }),
  updateOpMode: (id: string, data: OpModeDefinition) =>
    fetcher<OpModeDefinition>(withQuery('/opmodes', { id }), {
      method: 'PUT',
      body: JSON.stringify(data)
    }),
  deleteOpMode: (id: string) => fetcher<void>(withQuery('/opmodes', { id }), { method: 'DELETE' }),

  getRobots: () => fetcher<RobotMetadata[]>('/robots'),
  getRobot: (id: string) => fetcher<RobotMetadata>(withQuery('/robots', { id })),

  getEditorCapabilities: (opModeType: OpModeType, robotId: string) =>
    fetcher<Capabilities>(withQuery('/editor-capabilities', { opModeType, robotId })),

  getActionDetails: (id: string) => fetcher<ActionMetadata>(withQuery('/actions', { id })),
  getEventDetails: (id: string) => fetcher<EventMetadata>(withQuery('/events', { id })),

  // validateGraph: (graph: FlowGraph) =>
  //   fetcher<ValidationResult>('/validate', { method: 'POST', body: JSON.stringify(graph) }),
  generateCode: (graph: FlowGraph, opModeId: string) =>
    fetcher<GeneratedCode>(withQuery('/generate', { id: opModeId }), {
      method: 'POST',
      body: JSON.stringify(graph)
    })
};
