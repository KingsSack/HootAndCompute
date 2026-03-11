import { randomUUID } from 'crypto';
import type { IncomingMessage, ServerResponse } from 'http';
import type { Plugin, ViteDevServer } from 'vite';
import type { ActionMetadata, EventMetadata, OpModeDefinition, RobotMetadata } from '../types';

// --- Mock Data ---

const mockActions: ActionMetadata[] = [
  {
    id: 'motor_forward',
    name: 'Move Motor Forward',
    description: 'Moves a DC motor forward at a specific power.',
    enableAITool: true,
    parameters: [
      { name: 'motorName', type: 'String', defaultValue: 'frontLeft' },
      { name: 'power', type: 'Double', defaultValue: '0.5' }
    ],
    declaringClass: 'com.example.robot.actions.MotorActions',
    accessPath: 'motorActions.forward'
  },
  {
    id: 'motor_stop',
    name: 'Stop Motor',
    description: 'Stops a DC motor.',
    enableAITool: true,
    parameters: [{ name: 'motorName', type: 'String', defaultValue: 'frontLeft' }],
    declaringClass: 'com.example.robot.actions.MotorActions',
    accessPath: 'motorActions.stop'
  },
  {
    id: 'servo_set',
    name: 'Set Servo Position',
    description: 'Sets the position of a servo.',
    enableAITool: true,
    parameters: [
      { name: 'servoName', type: 'String', defaultValue: 'claw' },
      { name: 'position', type: 'Double', defaultValue: '0.0' }
    ],
    declaringClass: 'com.example.robot.actions.ServoActions',
    accessPath: 'servoActions.setPosition'
  }
];

const mockEvents: EventMetadata[] = [
  {
    id: 'com.example.event.Event.AutonomousEvent.Start',
    name: 'Start',
    opModeType: 'AutonomousMode',
    parameters: []
  },
  {
    id: 'com.example.event.Event.ManualEvent.Tap',
    name: 'Tap',
    opModeType: 'ManualMode',
    parameters: [
      {
        name: 'button',
        type: 'Button',
        defaultValue: ''
      }
    ]
  }
];

const mockRobots: RobotMetadata[] = [
  {
    id: 'com.example.robot.MecanumBot',
    name: 'MecanumBot',
    actions: mockActions,
    constructorParams: [],
    typeSignature: 'com.example.robot.MecanumBot',
    factoryExpression: 'MecanumBot(it)'
  },
  {
    id: 'com.example.robot.PushBot',
    name: 'PushBot',
    actions: mockActions.filter((a) => a.id.startsWith('motor')),
    constructorParams: [],
    typeSignature: 'com.example.robot.PushBot',
    factoryExpression: 'PushBot(it)'
  }
];

const mockOpModes: OpModeDefinition[] = [
  {
    id: randomUUID(),
    name: 'Sample Auto',
    type: 'AutonomousMode',
    robotId: 'com.example.robot.MecanumBot',
    flowGraph: { nodes: [], connections: [] },
    constructorParams: {}
  },
  {
    id: randomUUID(),
    name: 'Sample TeleOp',
    type: 'ManualMode',
    robotId: 'com.example.robot.MecanumBot',
    flowGraph: { nodes: [], connections: [] },
    constructorParams: {}
  }
];

// --- Plugin Implementation ---

export function mockApi(): Plugin {
  return {
    name: 'mock-volt-api',
    configureServer(server: ViteDevServer) {
      server.middlewares.use((req: IncomingMessage, res: ServerResponse, next) => {
        if (!req.url?.startsWith('/volt/api/')) {
          return next();
        }

        const url = new URL(req.url, `http://${req.headers.host}`);
        const pathname = url.pathname.replace('/volt/api', '');
        const method = req.method;

        // Helper to return JSON
        const sendJson = (data: any, status = 200) => {
          res.setHeader('Content-Type', 'application/json');
          res.writeHead(status);
          res.end(JSON.stringify(data));
        };

        // Helper to read JSON body
        const readBody = async (): Promise<any> => {
          return new Promise((resolve, reject) => {
            let body = '';
            req.on('data', (chunk) => {
              body += chunk.toString();
            });
            req.on('end', () => {
              try {
                resolve(body ? JSON.parse(body) : null);
              } catch (e) {
                reject(e);
              }
            });
          });
        };

        // --- Routes ---

        if (pathname === '/robots' && method === 'GET') {
          const id = url.searchParams.get('id');
          if (id) {
            const robot = mockRobots.find((r) => r.id === id);
            if (robot) return sendJson(robot);
            return sendJson({ error: 'Robot not found' }, 404);
          }

          return sendJson(mockRobots);
        }

        if (pathname === '/robots/actions' && method === 'GET') {
          const id = url.searchParams.get('id');
          if (!id) return sendJson({ error: 'Missing id' }, 400);

          const robot = mockRobots.find((r) => r.id === id);
          if (robot) return sendJson(robot.actions);
          return sendJson({ error: 'Robot not found' }, 404);
        }

        if (pathname === '/editor-capabilities' && method === 'GET') {
          const opModeType = url.searchParams.get('opModeType');
          if (!opModeType) return sendJson({ error: 'Missing opModeType' });

          const robotId = url.searchParams.get('robotId');
          if (!robotId) return sendJson({ error: 'Missing robotId' });

          const robot = mockRobots.find((robot) => robot.id === robotId);
          if (!robot) return sendJson({ error: 'Robot not found' });

          const events = mockEvents.filter((event) => event.opModeType === opModeType);

          return sendJson({ actions: robot.actions, events });
        }

        if (pathname === '/actions' && method === 'GET') {
          const id = url.searchParams.get('id');
          if (!id) return sendJson({ error: 'Missing id' }, 400);

          const action = mockActions.find((a) => a.id === id);
          if (action) return sendJson(action);
          return sendJson({ error: 'Action not found' }, 404);
        }

        if (pathname === '/events' && method === 'GET') {
          const id = url.searchParams.get('id');
          if (!id) return sendJson({ error: 'Missing id' }, 400);

          const event = mockEvents.find((e) => e.id === id);
          if (event) return sendJson(event);
          return sendJson({ error: 'Event not found' }, 404);
        }

        if (pathname === '/opmodes' && method === 'GET') {
          const id = url.searchParams.get('id');
          if (id) {
            const opmode = mockOpModes.find((m) => m.id === id);
            if (opmode) return sendJson(opmode);
            return sendJson({ error: 'OpMode not found' }, 404);
          }

          return sendJson(mockOpModes.map((m) => ({ ...m, flowGraph: undefined })));
        }

        if (pathname === '/opmodes' && method === 'POST') {
          return readBody().then((data) => {
            const newId = randomUUID();
            mockOpModes.push({
              ...data,
              id: newId,
              flowGraph: { nodes: [], connections: [] }
            });
            sendJson({ id: newId }, 201);
          });
        }

        if (pathname === '/opmodes' && method === 'PUT') {
          const id = url.searchParams.get('id');
          if (!id) return sendJson({ error: 'Missing id' }, 400);

          return readBody().then((data) => {
            const index = mockOpModes.findIndex((m) => m.id === id);
            if (index !== -1) {
              mockOpModes[index] = { ...mockOpModes[index], ...data };
              sendJson(mockOpModes[index]);
            } else {
              sendJson({ error: 'OpMode not found' }, 404);
            }
          });
        }

        if (pathname === '/opmodes' && method === 'DELETE') {
          const id = url.searchParams.get('id');
          if (!id) return sendJson({ error: 'Missing id' }, 400);

          const index = mockOpModes.findIndex((m) => m.id === id);
          if (index !== -1) {
            mockOpModes.splice(index, 1);
            return sendJson({ success: true });
          }
          return sendJson({ error: 'OpMode not found' }, 404);
        }

        if (pathname === '/generate' && method === 'POST') {
          return readBody().then((_graph) => {
            const mockCode = `HHEHEHE`;
            sendJson({ code: mockCode });
          });
        }

        sendJson({ error: 'Mock Route Not Found', path: pathname }, 404);
      });
    }
  };
}
