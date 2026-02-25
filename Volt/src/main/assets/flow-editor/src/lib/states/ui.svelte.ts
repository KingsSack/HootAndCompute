import type { Toast } from '$lib/types';

class UIState {
  sidebarOpen = $state(true);
  showOpModeModal = $state(true);
  showCreateModal = $state(false);
  showCodeModal = $state(false);
  isGenerating = $state(false);
  toasts = $state<Toast[]>([]);

  toastCounter = $state(0);

  addToast(message: string, type: 'success' | 'error' = 'success') {
    const id = ++this.toastCounter;
    const toast: Toast = { id, message, type, show: true };
    this.toasts.push(toast);
    setTimeout(() => {
      toast.show = false;
      setTimeout(() => {
        this.toasts = this.toasts.filter((t) => t.id !== id);
      }, 300);
    }, 3000);
  }

  openCreateModal() {
    this.showCreateModal = true;
  }

  closeCreateModal() {
    this.showCreateModal = false;
  }

  openCodeModal() {
    this.showCodeModal = true;
  }

  closeCodeModal() {
    this.showCodeModal = false;
  }

  closeOpModeModal() {
    this.showOpModeModal = false;
  }
}

export const uiState = new UIState();
