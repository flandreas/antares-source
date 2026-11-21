// Typed access to the Kotlin `WebAnalysis` API (unconnected ports, issues, log, statistics, tunnels, hierarchy).
import { engine } from '../../../core/engine'

export type UnconnectedScope = 'circuit' | 'project'
export type UnconnectedType = 'inputs' | 'outputs' | 'all'

export interface Issue {
	index: number
	severity: 'warning' | 'error'
	name: string
	description: string | null
	origin: string
	context: string | null
	canOpen: boolean
}

export interface ProbeLog {
	columns: string[]
	rows: { time: number; values: string[] }[]
	/** Whether older rows were left out */
	truncated: boolean
}

export interface StatisticsEntry {
	name: string
	count: number
	subCircuit: boolean
	scripted: boolean
}

export interface Statistics {
	circuit: string
	/** Components placed directly in the circuit */
	immediate: StatisticsEntry[]
	/** Components of all levels, scripted sub-circuits counted as one */
	flat: StatisticsEntry[]
	/** Components of all levels, including the inside of scripted sub-circuits */
	deep: StatisticsEntry[]
}

export interface TunnelUsage {
	circuit: string
	circuitName: string
	inFlow: boolean
	outFlow: boolean
	inOut: boolean
}

export interface GlobalTunnel {
	name: string
	usages: TunnelUsage[]
}

export interface HierarchyNode {
	/** IDs of the sub-circuit components from the root, joined by "/" */
	path: string
	id: number
	/** The label of the sub-circuit component and the circuit's name */
	name: string
	circuit: string
	hasChildren: boolean
}

const api = () => engine.api.analysis

export const analysisApi = {
	/** Posts unconnected ports as issues; returns their number. */
	unconnected(scope: UnconnectedScope, type: UnconnectedType): number {
		return api().unconnected(scope, type)
	},

	issues(): Issue[] {
		return JSON.parse(api().issues()) as Issue[]
	},

	openIssue(index: number) {
		api().openIssue(index)
		engine.refresh()
	},

	clearIssues() {
		api().clearIssues()
	},

	log(): ProbeLog | null {
		return JSON.parse(api().log()) as ProbeLog | null
	},

	clearLog() {
		api().clearLog()
	},

	statistics(): Statistics | null {
		return JSON.parse(api().statistics()) as Statistics | null
	},

	/** The longest propagation delay through the open circuit, or -1 if undefined. */
	propagationDelay(): number {
		return api().propagationDelay()
	},

	globalTunnels(): GlobalTunnel[] {
		return JSON.parse(api().globalTunnels()) as GlobalTunnel[]
	},

	/** The sub-circuits in the node at [path] ("" for the circuit itself), or null without circuit. */
	hierarchy(path: string): HierarchyNode[] | null {
		return JSON.parse(api().hierarchy(path)) as HierarchyNode[] | null
	},

	hierarchyRootName(): string | null {
		return api().hierarchyRootName() ?? null
	},

	navigateHierarchy(path: string) {
		api().navigateHierarchy(path)
		engine.refresh()
	},

	openHierarchyInSideView(path: string) {
		api().openHierarchyInSideView(path)
		engine.refresh()
	},
}
