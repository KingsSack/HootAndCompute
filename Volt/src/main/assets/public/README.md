# Volt Flow Editor - Technical Implementation Guide

The Volt Flow Editor is a visual programming tool that enables users to create and manage Volt OpModes through a
node-based infinite drag-and-drop canvas. This document provides a detailed technical specification for the ideal
implementation of the system.

## Architecture Overview

The Volt Flow Editor follows a client-server architecture where the frontend runs in a web browser and communicates with
a backend server hosted on the robot. The system is designed to integrate seamlessly with the FTC TeamCode app and
automatically discover available robots, actions, and OpModes.

**Access Point**: `http://<robot-ip>:8080/volt`

## Frontend Architecture

### Technology Stack

- **Alpine.js**: Reactive framework for UI interactivity and state management
- **Tailwind CSS**: Utility-first CSS framework for styling
- **Communication**: RESTful API calls to backend

**Note**: The frontend is a static HTML file that is served by the backend server.
The frontend is not a web application, but a web page. All assets must be available without an internet connection.

### Core Components

#### 1. OpMode List Page

The landing page serves as the entry point for the application.

**Features**:

- Display grid/list of existing OpModes with metadata (name, type, last modified)
- Search and filter functionality for OpMode discovery
- "Create New OpMode" button with type selection (Autonomous vs Manual) and robot selection discovered Robot definitions
  in TeamCode
- Delete and duplicate OpMode actions
- Real-time status indicators showing which OpModes are currently deployed

**Implementation Details**:

- Fetch OpMode list via `GET /api/opmodes`
- Handle OpMode creation via `POST /api/opmodes`
- Fetch available robots via `GET /api/robots`
- Navigate to the flow editor on OpMode selection

#### 2. Flow Editor Canvas

An infinite, zoomable, and pannable canvas for visual programming.

**Core Capabilities**:

- **Pan**: Click and drag on empty canvas space
- **Zoom**: Mouse wheel or pinch gestures (10% - 500% range)
- **Grid System**: Optional snap-to-grid with configurable spacing
- **Viewport Persistence**: Save and restore canvas position/zoom per OpMode

**Canvas State Management**:

```javascript
{
  nodes: [],           // Array of node objects
  connections: [],     // Array of connection objects
  viewport: {
    x: 0,
    y: 0,
    zoom: 1.0
  },
  selectedNodes: [],   // Currently selected node IDs
  clipboard: null      // Copy/paste buffer
}
```

#### 3. Node System

##### Node Types

**1. Start Node** (Single, Required)

- Entry point for OpMode execution
- Cannot be deleted
- Output port only

**2. Action Nodes**

- Represent individual volt actions
- Discovered from Volt action definitions in TeamCode
- Input and output ports for sequential flow
- Configurable parameters in the node

**3. Control Flow Nodes**

- **Sequence**: Execute multiple actions in order
- **Parallel**: Execute actions simultaneously
- **Conditional**: Branch based on sensor input or state
- **Repeat**: Repeat actions (repeat constructs)
- **Wait**: Delay execution for specified duration (the wait method from the VoltActionBuilder)

**4. End Node** (Optional, Multiple)

- Explicitly terminates execution flow
- Input port only

##### Node Structure

```javascript
{
  id: "node_uuid",
  type: "action|control|start|end",
  actionClass: "com.example.MyAction", // For action nodes
  position: { x: 100, y: 200 },
  data: {
    label: "Move Forward",
    parameters: {
      distance: 10.0,
      speed: 0.5
    }
  },
  ports: {
    inputs: ["input_1"],
    outputs: ["output_1", "output_2"] // Multiple for conditionals
  },
  metadata: {
    color: "#4F46E5",
    icon: "arrow-up",
    description: "Move robot forward"
  }
}
```

##### Connection Structure

```javascript
{
  id: "connection_uuid",
  sourceNode: "node_1_id",
  sourcePort: "output_1",
  targetNode: "node_2_id",
  targetPort: "input_1",
  metadata: {
    color: "#10B981"
  }
}
```

#### 4. Node Palette

A sidebar or drawer containing all available nodes.

**Organization**:

- **Search**: Filter nodes by name or description
- **Favorites**: User-pinned frequently used nodes
- **Sort**: Alphabetically or recently used

**Interaction**:

- Drag from palette onto canvas to create a new node
- Double-click to add at canvas center
- Display description on hover

### User Interactions

#### Node Operations

- **Create**: Drag from palette or double-click
- **Select**: Click node (Cmd/Ctrl+Click for multi-select)
- **Move**: Click and drag (maintains connections)
- **Delete**: Delete/Backspace key, context menu, or icon button
- **Copy/Paste**: Standard keyboard shortcuts
- **Duplicate**: Opt/Alt+Drag

#### Connection Operations

- **Create**: Drag from output port to input port
- **Validation**: Prevent invalid connections (type mismatches, cycles)
- **Delete**: Click connection and press Delete, or right-click menu
- **Reroute**: Drag connection endpoint to new port

#### Canvas Navigation

- **Pan**: Space+Drag or Middle Mouse Button
- **Zoom**: Scroll wheel or touchpad pinch
- **Fit to View**: Button to center and zoom to show all nodes
- **Focus Node**: Double-click in mini-map or node list

### Keyboard Shortcuts

- `Ctrl/Cmd + S`: Save OpMode
- `Ctrl/Cmd + Z/Y`: Undo/Redo
- `Ctrl/Cmd + C/V/X`: Copy/Paste/Cut
- `Delete/Backspace`: Delete selected
- `Ctrl/Cmd + A`: Select all
- `Space`: Pan mode toggle
- `F`: Fit to view

## Backend Architecture

### Technology Stack

- **Language**: Kotlin
- **HTTP Server**: NanoHTTPD
- **Integration**: Runs on REV Control Hub alongside the FTCRobotController app

### API Endpoints

#### OpMode Management

```
GET    /api/opmodes
       Response: List of OpMode metadata
       
GET    /api/opmodes/:id
       Response: Full OpMode definition including flow graph
       
POST   /api/opmodes
       Body: { name, type, robotId }
       Response: Created OpMode with ID
       
PUT    /api/opmodes/:id
       Body: Complete OpMode definition
       Response: Updated OpMode
       
DELETE /api/opmodes/:id
       Response: Success status
```

#### Robot Discovery

```
GET    /api/robots
       Response: List of available robot configurations
       
GET    /api/robots/:id
       Response: Robot hardware configuration and capabilities
```

#### Action Discovery

```
GET    /api/actions
       Query: ?robotId=<id>
       Response: List of available actions for robot
       
GET    /api/actions/:class
       Response: Detailed action metadata (parameters, docs)
```

#### Validation

```
POST   /api/validate
       Body: OpMode flow graph
       Response: Validation errors and warnings
```

#### Code Generation

```
POST   /api/generate
       Body: OpMode flow graph
       Response: Generated Kotlin code
```

### Backend Components

#### 1. Action Discovery

Scans TeamCode at startup to discover all Volt actions.

**Process**:

1. Reflect on all methods annotated with `@VoltAction`
2. Extract metadata: name, parameter names, parameter types
3. Parse KDoc/JavaDoc for descriptions

**Action Metadata Structure**:

```kotlin
data class ActionMetadata(
    val name: String,
    val description: String,
    val parameters: List<ParameterMetadata>,
)
```

#### 2. Robot Discovery

Extracts robot hardware configurations from TeamCode.

**Process**:

1. Reflect on all classes extending `Robot`
2. Extracts metadata: name, paramater names, paramater types
3. Parse KDoc/JavaDoc for descriptions

**Robot Metadata Structure**:

```kotlin
data class RobotMetadata(
    val name: String,
    val description: String,
    val paramaters: List<ParameterMetadata>,
)
```

#### 3. Flow Graph Validator

Validates OpMode flow graphs before code generation.

**Validation Rules**:

- All action nodes must be reachable from Start node
- No cycles in sequential flow (except explicit loops)
- All required parameters must be set
- Action compatibility with selected robot
- Type checking for connections
- Warning for unreachable nodes

#### 4. Code Generator

Transforms flow graph into executable Kotlin OpMode code.

**Generation Strategy**:

1. Parse flow graph into execution tree
2. Generate OpMode class extending appropriate base
3. Convert nodes to Volt DSL commands
4. Handle control flow (loops, conditionals, parallel)
5. Add error handling and telemetry
6. Format code and add documentation comments

**Example AutonomousMode Output**:

```kotlin
@Autonomous(name = "Generated OpMode", group = "Volt")
class GeneratedOpMode : AutonomousMode<Robot>({ hardwareMap -> Robot(hardwareMap)}) {    override fun sequence() = execute {
        parallel {
            +robot.arm.liftTo(5.0)
            +robot.claw.close()
            +robot.drivetrain.pathTo(robot.drivetrain.follower.pathBuilder().addPath()
                .build()
        }
    
        +robot.drivetrain.pathTo(robot.drivetrain.follower.pathBuilder().addPath()
            .build()
        )
    }
}
```

**Example ManualMode Output**:

```kotlin
@TeleOp(name = "Generated OpMode", group = "Volt")
class GeneratedOpMode : ManualMode<Robot>({ hardwareMap -> Robot(hardwareMap)}) {
    init {
        onButtonTapped(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.enable() }
        onButtonReleased(GamepadButton.RIGHT_BUMPER2) { +robot.launcher.disable() }
    }
}
```

#### 5. OpMode Persistence

Stores OpMode definitions as JSON files.

**Storage Location**: `TeamCode/src/main/assets/opmodes/`

**Format**:

```json
{
  "id": "opmode_uuid",
  "name": "My Autonomous",
  "type": "AutonomousMode",
  "robot": "Robot",
  "version": "1.0",
  "lastModified": "2024-01-15T10:30:00Z",
  "flowGraph": {
    "nodes": [...],
    "connections": [...]
  },
  "generatedCode": "...",
  "metadata": {
    "author": "Team 12345",
    "description": "Autonomous routine for red alliance"
  }
}
```

## Data Flow

### OpMode Creation Flow

1. User clicks "Create New OpMode"
2. Frontend displays modal for name, type, robot selection
3. User submits form → `POST /api/opmodes`
4. Backend creates OpMode with Start node
5. Backend returns OpMode ID
6. Frontend navigates to the flow editor with a new OpMode

### OpMode Editing Flow

1. User drags an action node from palette to canvas
2. Frontend creates a new node at the drop position
3. User configures parameters in nodes
4. User connects nodes by dragging between ports
5. Frontend maintains local state, debounced auto-save
6. Periodic `PUT /api/opmodes/:id` saves to backend

### OpMode Deployment Flow

1. User clicks the "Deploy" button
2. Frontend sends `POST /api/generate` with flow graph
3. Backend validates graph
4. Backend generates Kotlin code
5. Frontend displays generated code

## Advanced Features

### Undo/Redo System

Implement a command pattern for all mutations:

- Track history stack (last 50 operations)
- Serialize state changes as commands
- Enable time-travel debugging

### Version Control

- Save OpMode snapshots
- Compare versions (visual diff)
- Revert to previous versions

## Error Handling

### Frontend

- Graceful degradation if backend unavailable
- Local storage fallback for unsaved changes
- User-friendly error messages with recovery actions
- Validation feedback before save/deploy

### Backend

- Exception handling with detailed error responses
- Request validation and sanitization
- Rate limiting for API endpoints
- Logging for debugging and monitoring

## Performance Considerations

### Frontend

- Virtual scrolling for large node palettes
- Canvas rendering optimization (only render visible nodes)
- Debounced auto-save (2-second delay)
- Lazy loading of action metadata

### Backend

- Cache action registry (refresh only on TeamCode changes)
- Efficient graph traversal algorithms
- Pagination for large OpMode lists
- Async code generation (return immediately, notify on completion)

## Security

- Input validation on all API endpoints
- Sanitize generated code to prevent injection
- Rate limiting to prevent abuse
- CORS configuration for local network only
- No authentication required (trusted local network)

## Deployment

The Volt Flow Editor is packaged as static assets within the Volt library:

1. Frontend assets located in `Volt/src/main/assets/public/`
2. Backend server starts automatically when the TeamCode app launches
3. Server binds to port 8080 on all network interfaces
4. Assets served from `/volt` path prefix
