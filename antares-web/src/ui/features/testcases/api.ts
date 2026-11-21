// Typed access to the Kotlin `WebTestcases` / `WebScenarios` APIs (JSON results parsed here).
import { useSyncExternalStore } from 'react'
import { engine } from '../../../core/engine'

export interface ScriptError {
	message: string
	/** 1-based; 0 if unknown */
	row: number
	column: number
}

export interface TestcaseInfo {
	id: number
	name: string
	description: string
	script: string
	ignored: boolean
	skipPropDelayCheck: boolean
	iterations: number
}

export interface TestcaseList {
	digital: boolean
	editable: boolean
	simulating: boolean
	items: TestcaseInfo[]
}

export interface ResultCell {
	text: string
	state: 'normal' | 'passed' | 'failed'
	expected: string | null
	dontCare: boolean
}

export interface ResultRow {
	/** Position in a `run` block: a single vector, or the first/middle/last vector of a block */
	kind: 'top' | 'first' | 'line' | 'last'
	description: string
	cells: ResultCell[]
}

/** The result of running a test case on the circuit or on its execution script. */
export interface TestRun {
	type: 'circuit' | 'script'
	label: string
	tooltip: string
	error: string | null
	failedCount: number
	duration: number
	columns: Array<{ name: string; output: boolean }>
	rows: ResultRow[]
}

export interface TestResult {
	circuit: string
	circuitUuid: string
	testcaseId: number
	testcase: string
	ignored: boolean
	error: string | null
	failedCount: number
	general: Array<{ error: boolean; description: string }>
	runs: TestRun[]
}

export type TestStatus = 'passed' | 'failed' | 'ignored'

export const statusOf = (r: TestResult): TestStatus => (r.ignored ? 'ignored' : r.failedCount > 0 || r.error ? 'failed' : 'passed')

export interface ScenarioStepInfo {
	id: number
	name: string
	description: string
	condition: string
	highlightIds: string
	onEntry: string
	onExit: string
}

export interface ScenarioInfo {
	id: number
	name: string
	description: string
	condition: string
	steps: ScenarioStepInfo[]
}

export interface UsecaseInfo {
	id: number
	name: string
	description: string
	execScript: string
	testScript: string
	hasTest: boolean
}

export interface ScenarioState {
	editable: boolean
	simulating: boolean
	mode: string
	modes: Array<{ id: string; label: string }>
	breakpoints: boolean
	scenarios: ScenarioInfo[]
	selectedScenario: number | null
	selectedStep: number | null
	/** The scenario and step detected during simulation */
	activeScenario: number | null
	activeStep: number | null
	usecases: UsecaseInfo[]
	selectedUsecase: number | null
	recording: { usecaseId: number; active: boolean; log: string } | null
}

const parse = <T,>(json: string): T => JSON.parse(json) as T

export const testcasesApi = {
	get raw() {
		return engine.api.testcases
	},
	list: () => parse<TestcaseList | null>(engine.api.testcases.list()),
	check: (id: number, script: string) => parse<ScriptError | null>(engine.api.testcases.check(id, script)),
	run: (ids: number[]) => parse<TestResult[]>(engine.api.testcases.run(ids)),
	runAll: () => parse<TestResult[]>(engine.api.testcases.runAll()),
	runProject: () => parse<TestResult[]>(engine.api.testcases.runProject()),
}

export const scenariosApi = {
	get raw() {
		return engine.api.testcases.scenarios
	},
	state: () => parse<ScenarioState | null>(engine.api.testcases.scenarios.state()),
	check: (scenarioId: number, stepId: number, script: string) =>
		parse<ScriptError | null>(engine.api.testcases.scenarios.check(scenarioId, stepId, script)),
	checkUsecase: (id: number, script: string) => parse<ScriptError | null>(engine.api.testcases.scenarios.checkUsecase(id, script)),
}

/** ---- Last test results, kept while the UI switches tabs (desktop: test results pane) */

export interface ResultsSnapshot {
	results: TestResult[]
	/** The circuit whose tests were run, or null for a project-wide run */
	circuitUuid: string | null
}

let snapshot: ResultsSnapshot | null = null
const listeners = new Set<() => void>()

export const testResults = {
	get: () => snapshot,
	set(value: ResultsSnapshot | null) {
		snapshot = value
		listeners.forEach((l) => l())
	},
	subscribe(listener: () => void) {
		listeners.add(listener)
		return () => listeners.delete(listener)
	},
}

export function useTestResults(): ResultsSnapshot | null {
	return useSyncExternalStore(testResults.subscribe, testResults.get)
}
