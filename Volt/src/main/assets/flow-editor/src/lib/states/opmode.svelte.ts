import { apiClient } from '$lib/services/api';
import type { OpModeType } from '$lib/types';
import { editorState } from './editor.svelte';
import { uiState } from './ui.svelte';

class OpModeState {
  async createOpMode(name: string, type: OpModeType, robotId: string) {
    if (!name.trim()) return;
    try {
      const result = await apiClient.createOpMode({
        name: name.trim(),
        type,
        robotId
      });
      uiState.closeCreateModal();
      await editorState.loadOpMode(result.id);
      uiState.closeOpModeModal();
    } catch (error) {
      console.error('Failed to create OpMode:', error);
      uiState.addToast('Failed to create OpMode', 'error');
    }
  }
}

export const opModeState = new OpModeState();
