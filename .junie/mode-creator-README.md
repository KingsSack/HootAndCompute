# Volt Mode Creator

The Volt Mode Creator is a web interface that allows non-coders to create autonomous and manual modes for FTC robots. It provides a user-friendly way to configure robot actions and control mappings without writing code.

## Overview

The Mode Creator consists of:

1. A web interface for creating and managing modes
2. API endpoints for interacting with the robot
3. Backend logic for creating and registering modes

## Web Interface

The web interface is accessible at `http://192.168.43.1:8080/volt/mode-creator.html` when connected to the Robot Controller's WiFi network. It provides tabs for creating autonomous modes, manual modes, and viewing saved modes.

### Creating Autonomous Modes

To create an autonomous mode:

1. Go to the "Autonomous Mode" tab
2. Enter a name for the mode
3. Select a robot type
4. Add actions to the sequence by clicking on them in the "Available Actions" section
5. Configure the parameters for each action
6. Reorder actions using the up/down arrows
7. Click "Save Mode" to save the mode

### Creating Manual Modes

To create a manual mode:

1. Go to the "Manual Mode" tab
2. Enter a name for the mode
3. Select a robot type
4. Add control mappings by selecting an action and configuring its control
5. Click "Save Mode" to save the mode

## API Endpoints

The Mode Creator provides the following API endpoints:

- `GET /volt/api/robot-types`: Get a list of available robot types
- `GET /volt/api/actions?robotType=<type>`: Get a list of available actions for a robot type
- `GET /volt/api/autonomous-modes`: Get a list of saved autonomous modes
- `POST /volt/api/autonomous-modes`: Create a new autonomous mode
- `GET /volt/api/manual-modes`: Get a list of saved manual modes
- `POST /volt/api/manual-modes`: Create a new manual mode

## Implementation Details

### ModeCreatorHandler

The `ModeCreatorHandler` class handles API requests and responses. It provides methods for getting robot types, getting available actions, and creating and retrieving autonomous and manual modes.

### ModeCreator

The `ModeCreator` class provides utility methods for creating and registering autonomous and manual modes based on configurations received from the web interface. It includes methods for creating robots, autonomous modes, manual modes, and actions based on the configurations.

## Adding New Robot Types

To add a new robot type:

1. Create a new class that extends `Robot`
2. Add the robot type to the `handleGetRobotTypes` method in `ModeCreatorHandler`
3. Add a case for the robot type in the `handleGetActions` method in `ModeCreatorHandler`
4. Add a case for the robot type in the `createRobot` method in `ModeCreator`

## Adding New Actions

To add a new action:

1. Add the action to the appropriate method in `ModeCreatorHandler` (e.g., `getRobotWithMecanumDriveActions`)
2. Add a case for the action in the `createAction` method in `ModeCreator`

## Example Usage

### Creating an Autonomous Mode

```json
{
  "name": "Sample Autonomous",
  "robotType": "RobotWithMecanumDrive",
  "sequence": [
    {
      "id": "pathTo",
      "name": "Path To",
      "description": "Move the robot to a specific position",
      "parameters": [
        { "name": "x", "type": "number", "value": 10 },
        { "name": "y", "type": "number", "value": 20 },
        { "name": "heading", "type": "number", "value": 90 }
      ]
    },
    {
      "id": "wait",
      "name": "Wait",
      "description": "Wait for a specified time",
      "parameters": [
        { "name": "seconds", "type": "number", "value": 2 }
      ]
    }
  ]
}
```

### Creating a Manual Mode

```json
{
  "name": "Sample Manual",
  "robotType": "RobotWithMecanumDrive",
  "mappings": [
    {
      "action": {
        "id": "pathTo",
        "name": "Path To",
        "description": "Move the robot to a specific position",
        "parameters": [
          { "name": "x", "type": "number", "value": 10 },
          { "name": "y", "type": "number", "value": 20 },
          { "name": "heading", "type": "number", "value": 90 }
        ]
      },
      "controlType": "button",
      "control": "a1"
    }
  ]
}
```