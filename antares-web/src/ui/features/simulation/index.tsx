// Simulation options (desktop: "Simulation" menu of GraphMenuBarBuilder/AntaresMenuBarBuilder), view toggles
// (oscilloscope, animation), component IDs, the simulation time and the analog current flow speed.
import { useState } from 'react'
import { Clock3, Hash } from 'lucide-react'
import { engine } from '../../../core/engine'
import type { EditorState } from '../../../core/types'
import { DialogFrame, dialogs } from '../../Dialogs'
import type { MenuEntry } from '../../Menu'
import { appMenuContributions, canvasContextActions, canvasOverlays, register, type DocumentEditorProps } from '../registry'
import { simulationApi, type Choice, type SimulationOption, type SimulationState } from './api'
import './simulation.css'

const circuitOpen = (state: EditorState) => !!state.circuit && !state.document

/** A checkable menu entry for a simulation option. */
function option(sim: SimulationState, id: SimulationOption, label: string): MenuEntry {
	const o = sim.options[id]
	return { label, checked: o.checked, disabled: !o.enabled, onSelect: () => simulationApi.toggle(id) }
}

/** A heading followed by one checkable entry per choice, like a desktop radio button menu. */
function choices(heading: string, list: Choice[], current: string, select: (id: string) => void, label = (c: Choice) => c.label): MenuEntry[] {
	return ['separator', { heading }, ...list.map((c): MenuEntry => ({ label: label(c), checked: c.id === current, onSelect: () => select(c.id) }))]
}

const noiseLabels: Record<string, string> = {
	'execution.noiseGenerator.none': 'No noise',
	'execution.noiseGenerator.random': 'Random noise',
}

async function resetExecutionTime() {
	const error = simulationApi.resetExecutionTime()
	if (error) engine.message('info', 'Reset execution time', error)
}

register(appMenuContributions.simulate, (c) => c.id, {
	id: 'simulation-options',
	entries: (state) => {
		const sim = circuitOpen(state) ? simulationApi.state() : null
		if (!sim) return []
		return [
			option(sim, 'deepSimulation', 'Deep simulation'),
			option(sim, 'pauseOnIssue', 'Pause on issue'),
			option(sim, 'softBreakpoints', 'Soft breakpoints'),
			option(sim, 'scenarioBreakpoints', 'Scenario breakpoints'),
			...choices('On signal conflict', sim.conflictChoices, sim.conflict, simulationApi.setConflictBehaviour),
			...choices('Noise', sim.noiseChoices, sim.noise, simulationApi.setNoise, (c) => noiseLabels[c.id] ?? c.label),
			...choices('Show scenario texts', sim.scenarioModes, sim.scenarioMode, simulationApi.setScenarioMode),
			'separator',
			{ heading: 'Analysis tools' },
			option(sim, 'simulationTime', 'Show simulation time'),
			option(sim, 'interactiveDelay', 'Interactive propagation delay'),
			{ label: 'Reset execution time', icon: <Clock3 size={15} />, disabled: !sim.canResetTime, onSelect: () => void resetExecutionTime() },
		]
	},
})

register(appMenuContributions.view, (c) => c.id, {
	id: 'simulation-view',
	entries: (state) => {
		const sim = circuitOpen(state) ? simulationApi.state() : null
		if (!sim) return []
		return [
			option(sim, 'oscilloscope', 'Oscilloscope'),
			option(sim, 'animation', 'Animate signal changes'),
			{ label: 'Show component IDs…', icon: <Hash size={15} />, disabled: !sim.selectedIds, onSelect: () => void showIds() },
		]
	},
})

register(canvasContextActions, (c) => c.id, {
	id: 'display-ids',
	entries: (state) =>
		state.selectionCount > 0 ? [{ label: 'Show component IDs…', icon: <Hash size={15} />, onSelect: () => void showIds() }] : [],
})

/** Shows the IDs of the selected components (desktop: `DisplayIdsAction`). */
function showIds() {
	const ids = simulationApi.state()?.selectedIds
	if (!ids) return
	return dialogs.custom<null>((done) => <IdsDialog ids={ids} onClose={() => done(null)} />)
}

function IdsDialog({ ids, onClose }: { ids: string; onClose: () => void }) {
	const [copied, setCopied] = useState(false)
	const copy = async () => {
		await navigator.clipboard.writeText(ids)
		setCopied(true)
		window.setTimeout(onClose, 400)
	}
	return (
		<DialogFrame title="Selected component IDs" onCancel={onClose}>
			<div className="dialog-body">
				<textarea className="input sim-ids" readOnly value={ids} rows={4} onFocus={(e) => e.currentTarget.select()} />
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn" onClick={() => void copy()}>
					{copied ? 'Copied' : 'Copy to clipboard'}
				</button>
				<button type="button" className="btn btn-primary" autoFocus onClick={onClose}>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}

/** Simulation time and analog current flow speed, at the bottom left of the canvas while simulating. */
function SimulationStatus({ state }: DocumentEditorProps) {
	if (state.mode !== 'simulate' || !circuitOpen(state)) return null
	const sim = simulationApi.state()
	if (!sim) return null
	const showTime = sim.options.simulationTime.checked && !!sim.time
	if (!showTime && !sim.analog) return null
	const flow = sim.currentFlow
	return (
		<div className="canvas-overlay sim-status-overlay">
			{showTime && (
				<div className="sim-status" title="Simulation time">
					<Clock3 size={13} />
					<span className="sim-time">{sim.time?.replace(/_/g, '\u2009')}</span>
				</div>
			)}
			{sim.analog && (
				<div className="sim-status">
					<label className="sim-flow">
						<span>Current speed</span>
						<input
							type="range"
							min={flow.min}
							max={flow.max}
							value={Math.min(flow.speed, flow.max)}
							onChange={(e) => simulationApi.setCurrentFlow(Number(e.target.value), flow.turbo)}
						/>
					</label>
					<label className="sim-turbo" title="× 1,000 for small currents">
						<input type="checkbox" checked={flow.turbo} onChange={(e) => simulationApi.setCurrentFlow(flow.speed, e.target.checked)} />
						Turbo
					</label>
				</div>
			)}
		</div>
	)
}

register(canvasOverlays, (o) => o.id, { id: 'simulation-status', component: SimulationStatus })
