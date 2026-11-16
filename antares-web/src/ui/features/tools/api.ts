// Typed access to the Kotlin `WebTools` API (drawing tools, grid/snap, gate types, sub-circuits, port renaming).
import { engine } from '../../../core/engine'

export type ToolId = 'select' | 'text' | 'label' | 'rectangle' | 'ellipse' | 'polyline' | 'quadCurve' | 'cubicCurve' | 'dilCase'

export interface GateTypeOption {
	id: string
	label: string
	current: boolean
}

export interface ToolsState {
	/** The editor the tools apply to: the circuit, its symbol, or a sub-circuit's symbol edited in a dialog. */
	target: 'circuit' | 'symbol' | 'instance'
	tools: ToolId[]
	tool: ToolId
	toolsEnabled: boolean
	grid: boolean
	gridSnap: boolean
	componentSnap: boolean
	gateMnemonic: boolean
	gateTypes: GateTypeOption[]
	selectNext: boolean
	selectPrevious: boolean
	oneUp: boolean
	oneDown: boolean
	extract: boolean
	editSymbol: boolean
	resetSymbol: boolean
	renamePorts: boolean
	embed: boolean
}

export interface CircuitPort {
	id: number
	name: string
	type: string
	input: boolean
}

export type ToolAction = 'selectNext' | 'selectPrevious' | 'oneUp' | 'oneDown'

const api = () => engine.api.tools

export const toolsApi = {
	state: () => JSON.parse(api().state()) as ToolsState | null,
	setTool: (id: ToolId) => api().setTool(id),
	toggleGrid: () => api().toggleGrid(),
	toggleGridSnap: () => api().toggleGridSnap(),
	toggleComponentSnap: () => api().toggleComponentSnap(),
	toggleGateMnemonic: () => api().toggleGateMnemonic(),
	perform: (action: ToolAction) => api().perform(action),
	changeGateType: (id: string) => api().changeGateType(id),
	extractProblem: () => api().extractProblem(),
	extract: (name: string) => api().extract(name),
	beginEditSymbol: () => api().beginEditSymbol(),
	bindSymbolCanvas: (canvas: HTMLCanvasElement) => api().bindSymbolCanvas(canvas),
	undoSymbol: (redo: boolean) => api().undoSymbol(redo),
	symbolHistory: () => JSON.parse(api().symbolHistory()) as { canUndo: boolean; canRedo: boolean } | null,
	endEditSymbol: (apply: boolean) => api().endEditSymbol(apply),
	resetSymbol: () => api().resetSymbol(),
	ports: () => JSON.parse(api().ports()) as CircuitPort[],
	canEditPorts: () => api().canEditPorts(),
	renamePort: (id: number, name: string) => api().renamePort(id, name),
	focusPort: (id: number, zoom: boolean) => api().focusPort(id, zoom),
	embedThemes: () => JSON.parse(api().embedThemes()) as string[],
	embedCode: (theme: string) => api().embedCode(theme),
}
