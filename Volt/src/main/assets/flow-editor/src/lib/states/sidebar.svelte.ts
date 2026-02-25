import type { ActionMetadata, NodeCategory, OpModeType } from '$lib/types';

class SidebarState {
  categories = $state<NodeCategory[]>([]);

  initSidebar(opModeType: OpModeType, actions: ActionMetadata[]) {
    this.categories = [
      {
        name: 'Control Flow',
        nodes: [
          {
            type: 'control',
            label: 'Wait',
            description: 'Wait for a specified duration'
          }
        ]
      }
    ];

    if (opModeType === 'ManualMode') {
      this.categories.push({
        name: 'Triggers',
        nodes: [
          {
            type: 'button_trigger',
            label: 'Button',
            description: 'Trigger on button press'
          },
          {
            type: 'analog_trigger',
            label: 'Analog',
            description: 'Trigger on axis change'
          },
          {
            type: 'while_pressed',
            label: 'While Pressed',
            description: 'Run while button held'
          }
        ]
      });
    } else {
      this.categories.push({
        name: 'System',
        nodes: [
          {
            type: 'start',
            label: 'Start',
            description: 'OpMode Entry Point'
          },
          {
            type: 'end',
            label: 'End',
            description: 'Terminate execution'
          }
        ]
      });
    }

    if (actions.length > 0) {
      this.categories.push({
        name: 'Robot Actions',
        nodes: actions.map((action) => ({
          type: 'action',
          label: action.name,
          description: action.description,
          actionClass: action.declaringClass
        }))
      });
    }
  }
}

export const sidebarState = new SidebarState();
