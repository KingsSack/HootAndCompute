import type { Viewport, RobotMetadata, OpModeMetadata, ActionMetadata } from '$lib/types';
import { apiClient } from '$lib/services/api';
import { flowGraphState } from './flowgraph.svelte';
import { sidebarState } from './sidebar.svelte';
import { uiState } from './ui.svelte';

class EditorState {
  currentOpMode = $state<OpModeMetadata | null>(null);
  availableOpModes = $state<OpModeMetadata[]>([]);
  availableRobots = $state<RobotMetadata[]>([]);
  availableActions = $state<ActionMetadata[]>([]);

  isLoading = $state(false);
  isGenerating = $state(false);

  viewport = $state<Viewport>({ panX: 0, panY: 0, zoom: 1 });

  generatedCode = $state<string | null>(null);

  async initWorkspace() {
    this.isLoading = true;
    try {
      this.availableOpModes = await apiClient.getOpModes();
      this.availableRobots = await apiClient.getRobots();
    } catch (error) {
      console.error('Failed to load OpModes and Robots:', error);
    } finally {
      this.isLoading = false;
    }
  }

  async loadOpMode(id: string) {
    this.isLoading = true;
    try {
      this.currentOpMode = await apiClient.getOpMode(id);
      if (this.currentOpMode.id) {
        this.availableActions = await apiClient.getActions(this.currentOpMode.robotId);
      }

      sidebarState.initSidebar(this.currentOpMode?.type, this.availableActions);

      flowGraphState.loadGraph(this.currentOpMode?.flowGraph);
    } catch (error) {
      console.error(`Failed to load OpMode ${id}:`, error);
    } finally {
      this.isLoading = false;
    }
  }

  zoomIn() {
    this.viewport.zoom = Math.min(3, this.viewport.zoom + 0.1);
  }
  zoomOut() {
    this.viewport.zoom = Math.max(0.2, this.viewport.zoom - 0.1);
  }
  resetView() {
    this.viewport = { panX: 0, panY: 0, zoom: 1 };
  }

  async generateCode() {
    const currentGraph = flowGraphState.exportGraph();
    if (currentGraph.nodes.length === 0) {
      uiState.addToast('Cannot generate code for an empty flow', 'error');
      return;
    }

    this.isGenerating = true;
    try {
      this.generatedCode = await apiClient.generateCode(currentGraph);
      uiState.openCodeModal();
    } catch (error) {
      console.error('Code generation failed:', error);
      uiState.addToast('Code generation failed', 'error');
    } finally {
      this.isGenerating = false;
    }
  }
}

export const editorState = new EditorState();
