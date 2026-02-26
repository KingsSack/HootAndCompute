import type { RobotMetadata, ActionMetadata, FlowGraph, OpModeMetadata } from '$lib/types';

const API_BASE = '/volt/api';

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
  getOpModes: () => fetcher<OpModeMetadata[]>('/opmodes'),
  getOpMode: (id: string) => fetcher<OpModeMetadata>(`/opmodes/${id}`),
  createOpMode: (data: Omit<OpModeMetadata, 'id' | 'flowGraph'>) =>
    fetcher<{ id: string }>('/opmodes', { method: 'POST', body: JSON.stringify(data) }),
  updateOpMode: (id: string, data: OpModeMetadata) =>
    fetcher<OpModeMetadata>(`/opmodes/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteOpMode: (id: string) => fetcher<void>(`/opmodes/${id}`, { method: 'DELETE' }),

  getRobots: () => fetcher<RobotMetadata[]>('/robots'),
  getRobot: (id: string) => fetcher<RobotMetadata>(`/robots/${id}`),
  getActions: (robotId: string) => fetcher<ActionMetadata[]>(`/actions?robotId=${robotId}`),
  getActionDetails: (actionClass: string) => fetcher<ActionMetadata>(`/actions/${actionClass}`),

  // validateGraph: (graph: FlowGraph) =>
  //   fetcher<ValidationResult>('/validate', { method: 'POST', body: JSON.stringify(graph) }),
  generateCode: (graph: FlowGraph) =>
    fetcher<string>('/generate', { method: 'POST', body: JSON.stringify(graph) })
};
