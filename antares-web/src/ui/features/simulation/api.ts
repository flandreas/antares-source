// Typed access to the Kotlin `WebSimulation` API (simulation options and view toggles).
import { engine } from '../../../core/engine'

export type SimulationOption =
	| 'deepSimulation'
	| 'pauseOnIssue'
	| 'softBreakpoints'
	| 'simulationTime'
	| 'interactiveDelay'
	| 'oscilloscope'
	| 'animation'
	| 'scenarioBreakpoints'

export interface Choice {
	id: string
	label: string
}

export interface SimulationState {
	options: Record<SimulationOption, { checked: boolean; enabled: boolean }>
	noise: string
	noiseChoices: Choice[]
	conflict: string
	conflictChoices: Choice[]
	scenarioMode: string
	scenarioModes: Choice[]
	/** An analog circuit is open (current flow animation) */
	analog: boolean
	currentFlow: { speed: number; turbo: boolean; min: number; max: number }
	/** The simulation time shown by the desktop's status bar, e.g. "1,250 ns", or null */
	time: string | null
	canResetTime: boolean
	/** Comma-separated IDs of the selected components */
	selectedIds: string
}

const api = () => engine.api.simulation

export const simulationApi = {
	state(): SimulationState | null {
		return JSON.parse(api().state()) as SimulationState | null
	},
	toggle: (option: SimulationOption) => api().toggle(option),
	setNoise: (id: string) => api().setNoise(id),
	setConflictBehaviour: (id: string) => api().setConflictBehaviour(id),
	setScenarioMode: (id: string) => api().setScenarioMode(id),
	setCurrentFlow: (speed: number, turbo: boolean) => api().setCurrentFlow(speed, turbo),
	/** Returns an error message, or an empty string on success. */
	resetExecutionTime: (): string => api().resetExecutionTime(),
}
