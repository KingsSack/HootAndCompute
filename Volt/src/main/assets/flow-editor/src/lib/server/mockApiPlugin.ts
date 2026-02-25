import type { Plugin, ViteDevServer } from 'vite';
import type { IncomingMessage, ServerResponse } from 'http';
import type { OpModeMetadata, RobotMetadata, ActionMetadata } from '../types';
import { randomUUID } from 'crypto';

// --- Mock Data ---

const mockActions: ActionMetadata[] = [
  {
    id: 'motor_forward',
    name: 'Move Motor Forward',
    description: 'Moves a DC motor forward at a specific power.',
    enableAITool: true,
    parameters: [
      { name: 'motorName', type: 'String', defaultValue: '"frontLeft"' },
      { name: 'power', type: 'double', defaultValue: '0.5' }
    ],
    declaringClass: 'com.example.robot.actions.MotorActions',
    accessPath: 'motorActions.forward'
  },
  {
    id: 'motor_stop',
    name: 'Stop Motor',
    description: 'Stops a DC motor.',
    enableAITool: true,
    parameters: [{ name: 'motorName', type: 'String', defaultValue: '"frontLeft"' }],
    declaringClass: 'com.example.robot.actions.MotorActions',
    accessPath: 'motorActions.stop'
  },
  {
    id: 'servo_set',
    name: 'Set Servo Position',
    description: 'Sets the position of a servo.',
    enableAITool: true,
    parameters: [
      { name: 'servoName', type: 'String', defaultValue: '"claw"' },
      { name: 'position', type: 'double', defaultValue: '0.0' }
    ],
    declaringClass: 'com.example.robot.actions.ServoActions',
    accessPath: 'servoActions.setPosition'
  },
  {
    id: 'wait',
    name: 'Wait',
    description: 'Pauses execution for a specified duration.',
    enableAITool: true,
    parameters: [{ name: 'milliseconds', type: 'long', defaultValue: '1000' }],
    declaringClass: 'com.example.robot.actions.UtilActions',
    accessPath: 'utils.wait'
  }
];

const mockRobots: RobotMetadata[] = [
  {
    simpleName: 'MecanumBot',
    qualifiedName: 'com.example.robot.MecanumBot',
    actions: mockActions,
    constructorParams: [],
    typeSignature: 'com.example.robot.MecanumBot',
    factoryExpression: 'new MecanumBot(hardwareMap)'
  },
  {
    simpleName: 'PushBot',
    qualifiedName: 'com.example.robot.PushBot',
    actions: mockActions.filter((a) => a.id.startsWith('motor')),
    constructorParams: [],
    typeSignature: 'com.example.robot.PushBot',
    factoryExpression: 'new PushBot(hardwareMap)'
  }
];

let mockOpModes: OpModeMetadata[] = [
  {
    id: randomUUID(),
    name: 'Sample Auto',
    type: 'AutonomousMode',
    robotId: 'MecanumBot',
    flowGraph: { nodes: [], connections: [] }
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
          return sendJson(mockRobots);
        }

        if (pathname.startsWith('/robots/') && method === 'GET') {
          const id = pathname.split('/')[2];
          const robot = mockRobots.find((r) => r.simpleName === id);
          if (robot) return sendJson(robot);
          return sendJson({ error: 'Robot not found' }, 404);
        }

        if (pathname === '/actions' && method === 'GET') {
          const robotId = url.searchParams.get('robotId');
          if (robotId) {
            const robot = mockRobots.find((r) => r.simpleName === robotId);
            if (robot) return sendJson(robot.actions);
            return sendJson([], 200);
          }
          return sendJson(mockActions);
        }

        if (pathname.startsWith('/actions/') && method === 'GET') {
          const actionClass = pathname.split('/')[2];
          const action = mockActions.find(
            (a) => a.id === actionClass || a.declaringClass === actionClass
          );
          if (action) return sendJson(action);
          return sendJson({ error: 'Action not found' }, 404);
        }

        if (pathname === '/opmodes' && method === 'GET') {
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

        if (pathname.startsWith('/opmodes/') && method === 'GET') {
          const id = pathname.split('/')[2];
          const opmode = mockOpModes.find((m) => m.id === id);
          if (opmode) return sendJson(opmode);
          return sendJson({ error: 'OpMode not found' }, 404);
        }

        if (pathname.startsWith('/opmodes/') && method === 'PUT') {
          const id = pathname.split('/')[2];
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

        if (pathname.startsWith('/opmodes/') && method === 'DELETE') {
          const id = pathname.split('/')[2];
          const index = mockOpModes.findIndex((m) => m.id === id);
          if (index !== -1) {
            mockOpModes.splice(index, 1);
            res.writeHead(204);
            res.end();
            return;
          }
          return sendJson({ error: 'OpMode not found' }, 404);
        }

        if (pathname === '/generate' && method === 'POST') {
          return readBody().then((graph) => {
            const mockCode = `// Autogenerated Mock Code\n// Nodes: ${graph?.nodes?.length || 0}\n// Connections: ${graph?.connections?.length || 0}\n\n@Autonomous(name="Mock Auto")\npublic class MockAuto extends LinearOpMode {\n    @Override\n    public void runOpMode() {\n        waitForStart();\n        // TODO: Implement flow\n    }\n}`;
            sendJson(mockCode);
          });
        }

        // If no mock route matched, you might want to call next() or return 404
        // We'll return 404 as we expect this to catch all /volt/api requests if proxy is off.
        sendJson({ error: 'Mock Route Not Found', path: pathname }, 404);
      });
    }
  };
}
