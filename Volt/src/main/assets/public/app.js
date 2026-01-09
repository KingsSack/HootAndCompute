function app() {
    return {
        sidebarOpen: true,
        showOpModeModal: true,
        showCreateModal: false,
        nodeSearch: '',
        opModes: [
            { id: '1', name: 'Red Alliance Auto', type: 'AutonomousMode', robotId: 'robot1' },
            { id: '2', name: 'Blue Alliance Auto', type: 'AutonomousMode', robotId: 'robot1' },
            { id: '3', name: 'TeleOp Primary', type: 'ManualMode', robotId: 'robot1' }
        ],
        currentOpMode: null,
        nodes: [],
        connections: [],
        viewport: { x: 0, y: 0, zoom: 1.0 },
        isPanning: false,
        lastMousePos: { x: 0, y: 0 },
        activeConnection: null,
        draggedNode: null,
        toasts: [],

        get activeConnectionPath() {
            if (!this.activeConnection) return '';
            const start = this.activeConnection.fromPos;
            const end = this.activeConnection.to;
            return this.calculateBezier(start, end);
        },

        // Create OpMode modal state
        newOpModeName: '',
        newOpModeType: 'AutonomousMode',
        availableRobots: [],
        selectedRobot: '',

        // Dynamic actions state
        dynamicActions: [],
        actionsLoading: false,
        actionsError: null,

        categories: [
            {
                name: 'Action Nodes',
                nodes: [
                    { type: 'action', label: 'Move Forward', description: 'Moves the robot forward by a distance', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"/>', colorClass: 'bg-blue-600', parameters: { distance: 10, speed: 0.5 } },
                    { type: 'action', label: 'Turn', description: 'Turns the robot by an angle', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>', colorClass: 'bg-blue-600', parameters: { angle: 90, speed: 0.3 } },
                    { type: 'action', label: 'Lift Arm', description: 'Moves the arm to a height', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 11l5-5m0 0l5 5m-5-5v12"/>', colorClass: 'bg-indigo-600', parameters: { height: 5 } },
                    { type: 'action', label: 'Claw Control', description: 'Opens or closes the claw', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"/>', colorClass: 'bg-indigo-600', parameters: { state: 'closed' } }
                ]
            },
            {
                name: 'Control Flow',
                nodes: [
                    { type: 'control', label: 'Wait', description: 'Wait for a specified duration', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>', colorClass: 'bg-amber-600', parameters: { duration: 1.0 } },
                    { type: 'control', label: 'Parallel', description: 'Execute child nodes in parallel', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"/>', colorClass: 'bg-purple-600', parameters: {} }
                ]
            },
            {
                name: 'System',
                nodes: [
                    { type: 'start', label: 'Start', description: 'OpMode Entry Point', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>', colorClass: 'bg-green-600', parameters: {} },
                    { type: 'end', label: 'End', description: 'Terminate execution', icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z M9 9l6 6m0-6l-6 6"/>', colorClass: 'bg-red-600', parameters: {} }
                ]
            }
        ],

        init() {
            // Setup keyboard shortcuts
            window.addEventListener('keydown', (e) => {
                if (e.key === 'Delete' || e.key === 'Backspace') {
                    if (document.activeElement.tagName !== 'INPUT') {
                        this.deleteSelectedNodes();
                    }
                }
            });

            // Fetch robots and actions from API
            this.fetchRobots();
            this.fetchActions();
        },

        async fetchRobots() {
            try {
                const response = await fetch('/volt/api/robots');
                if (response.ok) {
                    this.availableRobots = await response.json();
                    if (this.availableRobots.length > 0) {
                        this.selectedRobot = this.availableRobots[0];
                    }
                }
            } catch (e) {
                console.log('Could not fetch robots (backend may be offline)');
                // Use fallback robots for development
                this.availableRobots = ['Robot'];
                this.selectedRobot = 'Robot';
            }
        },

        async fetchActions() {
            this.actionsLoading = true;
            this.actionsError = null;
            try {
                const response = await fetch('/volt/api/actions');
                if (response.ok) {
                    const actions = await response.json();
                    this.dynamicActions = actions.map(action => ({
                        type: 'action',
                        label: action.name,
                        description: action.description,
                        icon: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>',
                        colorClass: 'bg-cyan-600',
                        parameters: action.parameters.reduce((acc, param) => {
                            acc[param.name] = param.value;
                            return acc;
                        }, {})
                    }));
                }
            } catch (e) {
                console.log('Could not fetch actions (backend may be offline)');
                this.actionsError = 'Backend offline';
            } finally {
                this.actionsLoading = false;
            }
        },

        get allCategories() {
            const cats = [...this.categories];
            if (this.dynamicActions.length > 0) {
                cats.unshift({
                    name: 'Robot Actions',
                    nodes: this.dynamicActions
                });
            }
            return cats;
        },

        // --- Viewport Management ---
        get gridStyle() {
            const size = 40 * this.viewport.zoom;
            const x = this.viewport.x % size;
            const y = this.viewport.y % size;
            return `
                background-image: radial-gradient(circle, rgba(71, 85, 105, 0.2) 1.5px, transparent 1.5px);
                background-size: ${size}px ${size}px;
                background-position: ${x}px ${y}px;
            `;
        },

        get canvasStyle() {
            return `transform: translate(${this.viewport.x}px, ${this.viewport.y}px) scale(${this.viewport.zoom});`;
        },

        zoomIn() { this.viewport.zoom = Math.min(this.viewport.zoom + 0.1, 3); },
        zoomOut() { this.viewport.zoom = Math.max(this.viewport.zoom - 0.1, 0.2); },
        resetView() { this.viewport = { x: 0, y: 0, zoom: 1.0 }; },

        onCanvasMouseDown(e) {
            // Only pan if clicking on the background (the target is the canvas itself or the grid)
            if (e.target.closest('.pointer-events-auto')) return;

            if (e.button === 0) { // Left click
                e.preventDefault();
                this.isPanning = true;
                this.lastMousePos = { x: e.clientX, y: e.clientY };

                // Deselect all nodes
                this.nodes.forEach(n => n.selected = false);
            }
        },

        onCanvasMouseMove(e) {
            if (this.isPanning) {
                const dx = e.clientX - this.lastMousePos.x;
                const dy = e.clientY - this.lastMousePos.y;
                this.viewport.x += dx;
                this.viewport.y += dy;
                this.lastMousePos = { x: e.clientX, y: e.clientY };
            }

            if (this.draggedNode) {
                const dx = (e.clientX - this.lastMousePos.x) / this.viewport.zoom;
                const dy = (e.clientY - this.lastMousePos.y) / this.viewport.zoom;
                this.draggedNode.x += dx;
                this.draggedNode.y += dy;
                this.lastMousePos = { x: e.clientX, y: e.clientY };
            }

            if (this.activeConnection) {
                this.activeConnection.to = this.screenToCanvas(e.clientX, e.clientY);
            }
        },



        onCanvasWheel(e) {
            e.preventDefault();
            const delta = e.deltaY > 0 ? -0.1 : 0.1;
            const oldZoom = this.viewport.zoom;
            const newZoom = Math.max(0.2, Math.min(3, this.viewport.zoom + delta));

            if (newZoom !== oldZoom) {
                // Get canvas container bounds
                const container = this.$refs.canvasContainer;
                const rect = container.getBoundingClientRect();

                // Mouse position relative to canvas container
                const mouseX = e.clientX - rect.left;
                const mouseY = e.clientY - rect.top;

                // Point in world space under cursor (before zoom)
                const worldX = (mouseX - this.viewport.x) / oldZoom;
                const worldY = (mouseY - this.viewport.y) / oldZoom;

                // Adjust viewport so same world point stays under cursor after zoom
                this.viewport.x = mouseX - worldX * newZoom;
                this.viewport.y = mouseY - worldY * newZoom;
                this.viewport.zoom = newZoom;
            }
        },

        // --- Node Management ---
        selectOpMode(mode) {
            this.currentOpMode = mode;
            this.showOpModeModal = false;
            this.nodes = [];
            this.connections = [];
            this.addToast(`Loaded OpMode: ${mode.name}`, 'success');
        },

        openCreateModal() {
            this.showCreateModal = true;
            this.newOpModeName = '';
            this.newOpModeType = 'AutonomousMode';
        },

        closeCreateModal() {
            this.showCreateModal = false;
        },

        createNewOpMode() {
            if (!this.newOpModeName.trim()) {
                this.addToast('Please enter an OpMode name', 'error');
                return;
            }

            const newMode = {
                id: Date.now().toString(),
                name: this.newOpModeName.trim(),
                type: this.newOpModeType,
                robotId: this.selectedRobot || 'Robot'
            };

            this.opModes.push(newMode);
            this.addToast(`Created OpMode: ${newMode.name}`, 'success');
            this.showCreateModal = false;
            this.selectOpMode(newMode);
        },

        onDragStart(e, nodeTemplate) {
            e.dataTransfer.setData('nodeTemplate', JSON.stringify(nodeTemplate));
        },

        onDrop(e) {
            const data = e.dataTransfer.getData('nodeTemplate');
            if (data) {
                const template = JSON.parse(data);
                const pos = this.screenToCanvas(e.clientX, e.clientY);
                this.addNodeAt(template.type, pos.x, pos.y, template);
            }
        },

        addNodeAt(type, x, y, template = null) {
            const id = 'node-' + Math.random().toString(36).substr(2, 9);
            const newNode = {
                id: id,
                type: type,
                label: template ? template.label : 'New Node',
                icon: template ? template.icon : '',
                colorClass: template ? template.colorClass : 'bg-gray-600',
                x: x,
                y: y,
                width: 220,
                height: 120,
                data: {
                    parameters: template ? { ...template.parameters } : {}
                },
                selected: false,
                dragging: false
            };
            this.nodes.push(newNode);

            // Auto-select the new node
            this.nodes.forEach(n => n.selected = false);
            newNode.selected = true;

            return newNode;
        },

        onNodeMouseDown(e, node) {
            e.preventDefault();
            this.nodes.forEach(n => n.selected = false);
            node.selected = true;
            node.dragging = true;
            this.draggedNode = node;
            this.lastMousePos = { x: e.clientX, y: e.clientY };
        },

        deleteSelectedNodes() {
            this.nodes = this.nodes.filter(n => !n.selected || n.type === 'start');
            // Remove lingering connections
            const nodeIds = this.nodes.map(n => n.id);
            this.connections = this.connections.filter(c => nodeIds.includes(c.fromId) && nodeIds.includes(c.toId));
        },

        deleteNode(id) {
            const node = this.nodes.find(n => n.id === id);
            if (node && node.type === 'start') return;
            this.nodes = this.nodes.filter(n => n.id !== id);
            this.connections = this.connections.filter(c => c.fromId !== id && c.toId !== id);
        },

        // --- Connection Management ---
        onPortMouseDown(e, nodeId, type) {
            e.preventDefault();
            const portPos = this.getPointForPort(nodeId, type);
            this.activeConnection = {
                fromId: nodeId,
                fromType: type,
                fromPos: portPos,
                to: portPos
            };
        },

        onCanvasMouseUp(e) {
            if (this.activeConnection) {
                // Check if we dropped on a port
                const target = e.target.closest('[id^="port-"]');
                if (target) {
                    // Parse port ID: port-{in|out}-{nodeId}
                    // Node IDs can contain hyphens, so we need to rejoin after type
                    const parts = target.id.split('-');
                    const type = parts[1]; // 'in' or 'out'
                    const toId = parts.slice(2).join('-'); // Rejoin remaining parts for node ID

                    // Validate: different port types and different nodes
                    if (type !== this.activeConnection.fromType && toId !== this.activeConnection.fromId) {
                        // Determine source and target based on port types
                        if (this.activeConnection.fromType === 'output') {
                            this.createConnection(this.activeConnection.fromId, toId);
                        } else {
                            this.createConnection(toId, this.activeConnection.fromId);
                        }
                    }
                }
            }
            this.isPanning = false;
            if (this.draggedNode) {
                this.draggedNode.dragging = false;
                this.draggedNode = null;
            }
            this.activeConnection = null;
        },

        createConnection(fromId, toId) {
            // Avoid duplicates
            if (this.connections.find(c => c.fromId === fromId && c.toId === toId)) return;

            this.connections.push({
                fromId,
                toId
            });
        },

        getConnectionPath(conn) {
            const start = this.getPointForPort(conn.fromId, 'output');
            const end = this.getPointForPort(conn.toId, 'input');
            if (start && end) {
                return this.calculateBezier(start, end);
            }
            return '';
        },

        calculateBezier(start, end) {
            const dx = Math.abs(end.x - start.x) * 0.5;
            const curvature = Math.max(dx, 50);
            return `M ${start.x} ${start.y} C ${start.x + curvature} ${start.y}, ${end.x - curvature} ${end.y}, ${end.x} ${end.y}`;
        },

        getPointForPort(nodeId, type) {
            const node = this.nodes.find(n => n.id === nodeId);
            if (!node) return { x: 0, y: 0 };

            // Re-calculate based on node position and tracked dimensions
            // Input is on left (node.x), Output is on right (node.x + node.width)
            // Both are vertically centered (node.y + node.height / 2)
            const w = node.width || 220;
            const h = node.height || 120;

            return {
                x: type === 'output' ? node.x + w : node.x,
                y: node.y + h / 2
            };
        },

        // --- Helpers ---
        screenToCanvas(x, y) {
            // Get canvas container offset
            const container = this.$refs.canvasContainer;
            if (container) {
                const rect = container.getBoundingClientRect();
                x -= rect.left;
                y -= rect.top;
            }

            return {
                x: (x - this.viewport.x) / this.viewport.zoom,
                y: (y - this.viewport.y) / this.viewport.zoom
            };
        },

        filterNodes(nodes) {
            if (!this.nodeSearch) return nodes;
            return nodes.filter(n =>
                n.label.toLowerCase().includes(this.nodeSearch.toLowerCase()) ||
                n.description.toLowerCase().includes(this.nodeSearch.toLowerCase())
            );
        },

        addToast(message, type = 'success') {
            const id = Date.now();
            this.toasts.push({ id, message, type, show: true });
            setTimeout(() => {
                const toast = this.toasts.find(t => t.id === id);
                if (toast) toast.show = false;
            }, 3000);
        },

        saveOpMode() {
            this.addToast('OpMode saved successfully!', 'success');
            console.log('Saving:', {
                nodes: this.nodes,
                connections: this.connections
            });
        },

        deployOpMode() {
            this.addToast('Deploying to robot...', 'success');
            // Mock delay
            setTimeout(() => {
                this.addToast('Deployment complete!', 'success');
            }, 2000);
        }
    };
}
