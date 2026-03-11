import type { ActionMetadata, EventMetadata, NodeTemplate, OpModeType } from '$lib/types';

class SidebarState {
  actions = $state<NodeTemplate[]>([]);
  events = $state<NodeTemplate[]>([]);

  initSidebar(opModeType: OpModeType, actions: ActionMetadata[], events: EventMetadata[]) {
    this.actions = actions.map(
      (action): NodeTemplate => ({
        id: action.id,
        label: action.name,
        description: action.description,
        type: 'Action'
      })
    );

    this.events = events
      .filter((event) => event.opModeType === opModeType)
      .map(
        (event): NodeTemplate => ({
          id: event.id,
          label: event.name,
          type: 'Event'
        })
      );
  }
}

export const sidebarState = new SidebarState();
