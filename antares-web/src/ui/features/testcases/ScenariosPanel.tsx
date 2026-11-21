import { useCallback, useState } from 'react'
import {
	ArrowDown,
	ArrowUp,
	Circle,
	Copy,
	FlaskConical,
	Footprints,
	MoreHorizontal,
	Pencil,
	Play,
	Plus,
	Square,
	Trash2,
	Clapperboard,
	Milestone,
} from 'lucide-react'
import { DialogFrame, dialogs } from '../../Dialogs'
import { MenuButton, type MenuEntry } from '../../Menu'
import { useStoredState } from '../../hooks'
import type { DocumentEditorProps } from '../registry'
import { scenariosApi, type ScenarioInfo, type ScenarioState, type ScenarioStepInfo, type UsecaseInfo } from './api'
import { ScriptEditor } from './ScriptEditor'
import { FieldRow, NumberField, Switch, TextArea, TextField } from './fields'
import { uniqueName } from './TestsPanel'

const api = () => scenariosApi.raw

/** Scenarios, scenario steps and use cases of the open circuit (desktop: "Scenarios" and "Use cases" panes). */
export function ScenariosPanel(_props: DocumentEditorProps) {
	const state = scenariosApi.state()
	const [view, setView] = useStoredState<'scenarios' | 'usecases'>('antares.scenariosView', 'scenarios')
	if (!state) return <div className="inspector-empty">Open a circuit to see its scenarios.</div>
	return (
		<div className="panel-scroll inspector-body scenarios-panel">
			<div className="feature-switch">
				<div className="segmented small" role="tablist">
					<button type="button" role="tab" aria-selected={view === 'scenarios'} className={view === 'scenarios' ? 'selected' : ''} onClick={() => setView('scenarios')}>
						Scenarios
						{state.scenarios.length > 0 && <span className="count">{state.scenarios.length}</span>}
					</button>
					<button type="button" role="tab" aria-selected={view === 'usecases'} className={view === 'usecases' ? 'selected' : ''} onClick={() => setView('usecases')}>
						Use cases
						{state.usecases.length > 0 && <span className="count">{state.usecases.length}</span>}
					</button>
				</div>
			</div>
			{view === 'scenarios' ? <Scenarios state={state} /> : <Usecases state={state} />}
		</div>
	)
}

/** ---- Scenarios */

function Scenarios({ state }: { state: ScenarioState }) {
	const { editable, simulating } = state
	const selectedScenario = state.scenarios.find((s) => s.id === state.selectedScenario) ?? null
	const selectedStep = selectedScenario?.steps.find((s) => s.id === state.selectedStep) ?? null

	const addScenario = async () => {
		const name = await askName('New scenario', uniqueName('Scenario', state.scenarios), 'Create')
		if (name) api().addScenario(name)
	}

	const addStep = async (s: ScenarioInfo) => {
		const name = await askName(`New step of “${s.name}”`, uniqueName('Step', s.steps), 'Add step')
		if (name) api().addStep(s.id, name)
	}

	const rename = async (s: ScenarioInfo, step?: ScenarioStepInfo) => {
		const name = await askName(step ? 'Rename step' : 'Rename scenario', (step ?? s).name, 'Rename')
		if (name) api().setProperty(s.id, step?.id ?? -1, 'name', name)
	}

	const remove = async (s: ScenarioInfo, step?: ScenarioStepInfo) => {
		const ok = await dialogs.confirm({
			title: step ? 'Delete step' : 'Delete scenario',
			message: step ? `Delete the step “${step.name}” of “${s.name}”?` : `Delete the scenario “${s.name}” and its ${s.steps.length} steps?`,
			confirmLabel: 'Delete',
			danger: true,
		})
		if (ok) api().delete(s.id, step?.id ?? -1)
	}

	const select = (s: ScenarioInfo, step?: ScenarioStepInfo) => {
		const isSelected = s.id === state.selectedScenario && (step?.id ?? null) === state.selectedStep
		if (isSelected) api().select(-1, -1)
		else api().select(s.id, step?.id ?? -1)
	}

	const scenarioMenu = (s: ScenarioInfo, index: number): MenuEntry[] => [
		{ label: 'Add step…', icon: <Plus size={14} />, disabled: !editable, onSelect: () => void addStep(s) },
		{ label: 'Rename…', icon: <Pencil size={14} />, disabled: !editable, onSelect: () => void rename(s) },
		{ label: 'Move up', icon: <ArrowUp size={14} />, disabled: !editable || index === 0, onSelect: () => api().move(s.id, -1, -1) },
		{ label: 'Move down', icon: <ArrowDown size={14} />, disabled: !editable || index === state.scenarios.length - 1, onSelect: () => api().move(s.id, -1, 1) },
		'separator',
		{ label: 'Delete…', icon: <Trash2 size={14} />, danger: true, disabled: !editable, onSelect: () => void remove(s) },
	]

	const stepMenu = (s: ScenarioInfo, step: ScenarioStepInfo, index: number): MenuEntry[] => [
		{ label: 'Rename…', icon: <Pencil size={14} />, disabled: !editable, onSelect: () => void rename(s, step) },
		{ label: 'Move up', icon: <ArrowUp size={14} />, disabled: !editable || index === 0, onSelect: () => api().move(s.id, step.id, -1) },
		{ label: 'Move down', icon: <ArrowDown size={14} />, disabled: !editable || index === s.steps.length - 1, onSelect: () => api().move(s.id, step.id, 1) },
		'separator',
		{ label: 'Delete…', icon: <Trash2 size={14} />, danger: true, disabled: !editable, onSelect: () => void remove(s, step) },
	]

	return (
		<>
			<div className="feature-toolbar">
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void addScenario()}>
					<Plus size={14} /> New scenario
				</button>
				{selectedScenario && !selectedStep && (
					<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void addStep(selectedScenario)}>
						<Plus size={14} /> Step
					</button>
				)}
			</div>

			{simulating && state.scenarios.length > 0 && (
				<div className="hint">The scenario and step detected in the running simulation are marked.</div>
			)}

			{state.scenarios.length === 0 ? (
				<div className="empty-small tests-empty">
					<Milestone size={20} />
					<p>
						No scenarios yet. A scenario explains a situation of the circuit, e.g. “Set” or “Reset” of a latch. While
						simulating, its description is shown when its condition holds; steps highlight parts of the circuit.
					</p>
				</div>
			) : (
				<div className={`feature-list${simulating ? ' simulating' : ''}`} role="tree" aria-label="Scenarios">
					{state.scenarios.map((s, i) => {
						const isSelected = s.id === state.selectedScenario && state.selectedStep === null
						const isActive = simulating && s.id === state.activeScenario
						return (
							<div key={s.id} role="group">
								<div
									role="treeitem"
									aria-selected={isSelected}
									className={`feature-item${isSelected ? ' selected' : ''}${isActive ? ' active' : ''}`}
									onClick={() => !simulating && select(s)}
									onDoubleClick={() => editable && void rename(s)}
								>
									<Milestone size={14} className="feature-item-icon" />
									<span className="truncate feature-item-name">{s.name || 'Unnamed'}</span>
									{isActive && <span className="active-dot" title="Active in the simulation" />}
									<span onClick={(e) => e.stopPropagation()}>
										<MenuButton entries={() => scenarioMenu(s, i)} className="icon-btn small row-action" title="More" align="right">
											<MoreHorizontal size={15} />
										</MenuButton>
									</span>
								</div>
								{s.steps.map((step, j) => {
									const stepSelected = s.id === state.selectedScenario && step.id === state.selectedStep
									const stepActive = simulating && isActive && step.id === state.activeStep
									return (
										<div
											key={step.id}
											role="treeitem"
											aria-selected={stepSelected}
											className={`feature-item step${stepSelected ? ' selected' : ''}${stepActive ? ' active' : ''}`}
											onClick={() => !simulating && select(s, step)}
											onDoubleClick={() => editable && void rename(s, step)}
										>
											<Footprints size={13} className="feature-item-icon" />
											<span className="truncate feature-item-name">{step.name || 'Unnamed'}</span>
											{stepActive && <span className="active-dot" title="Active in the simulation" />}
											<span onClick={(e) => e.stopPropagation()}>
												<MenuButton entries={() => stepMenu(s, step, j)} className="icon-btn small row-action" title="More" align="right">
													<MoreHorizontal size={15} />
												</MenuButton>
											</span>
										</div>
									)
								})}
							</div>
						)
					})}
				</div>
			)}

			{selectedScenario && !simulating && (
				<ScenarioDetails key={`${selectedScenario.id}.${selectedStep?.id ?? ''}`} scenario={selectedScenario} step={selectedStep} editable={editable} />
			)}

			<section className="prop-group feature-settings">
				<h3>During simulation</h3>
				<FieldRow label="Show texts" title="When scenario descriptions are shown in the running circuit">
					<select className="input select" value={state.mode} onChange={(e) => api().setMode(e.target.value)}>
						{state.modes.map((m) => (
							<option key={m.id} value={m.id}>
								{m.label}
							</option>
						))}
					</select>
				</FieldRow>
				<FieldRow label="Pause at steps" inline title="Pause the simulation whenever a scenario step starts">
					<Switch checked={state.breakpoints} onChange={(v) => api().setBreakpoints(v)} />
				</FieldRow>
			</section>
		</>
	)
}

function ScenarioDetails({ scenario: s, step, editable }: { scenario: ScenarioInfo; step: ScenarioStepInfo | null; editable: boolean }) {
	const stepId = step?.id ?? -1
	const set = (key: string, value: string) => api().setProperty(s.id, stepId, key, value)
	const check = useCallback((script: string) => scenariosApi.check(s.id, stepId, script), [s.id, stepId])
	const bean = step ?? s
	const conditionHelp = (
		<p>
			An expression over the circuit’s ports, e.g. <code>S == 1 and R == 0</code>. {step ? 'The step' : 'The scenario'} is active while
			it is true{step ? ' and its scenario is active' : ''}.
		</p>
	)
	return (
		<section className="prop-group feature-details">
			<h3 className="truncate">
				{step ? `${s.name} › ` : ''}
				{bean.name || 'Unnamed'}
			</h3>
			<div className="hint subtle">
				{step ? 'Selected steps highlight their components in the circuit.' : 'The selected scenario’s description is shown in the circuit.'}
			</div>
			<FieldRow label="Description" stacked>
				<TextArea value={bean.description} disabled={!editable} onCommit={(v) => set('description', v)} placeholder="Shown in the circuit while active" />
			</FieldRow>
			<ScriptEditor
				label="Condition"
				title={`Condition of “${bean.name}”`}
				value={bean.condition}
				readOnly={!editable}
				check={check}
				onCommit={(v) => set('condition', v)}
				placeholder="e.g. S == 1 and R == 0"
				help={conditionHelp}
				rows={3}
			/>
			{step && (
				<>
					<FieldRow label="Highlight IDs" title="Comma-separated IDs of the components to highlight (shown in the inspector header)">
						<TextField value={step.highlightIds} disabled={!editable} onCommit={(v) => set('highlightIds', v)} />
					</FieldRow>
					<ScriptEditor
						label="On entry"
						title={`On entry of “${step.name}”`}
						value={step.onEntry}
						readOnly={!editable}
						check={check}
						onCommit={(v) => set('onEntry', v)}
						rows={3}
					/>
					<ScriptEditor
						label="On exit"
						title={`On exit of “${step.name}”`}
						value={step.onExit}
						readOnly={!editable}
						check={check}
						onCommit={(v) => set('onExit', v)}
						rows={3}
					/>
				</>
			)}
		</section>
	)
}

/** ---- Use cases */

function Usecases({ state }: { state: ScenarioState }) {
	const { editable, simulating } = state
	const selected = state.usecases.find((u) => u.id === state.selectedUsecase) ?? null
	const recording = state.recording
	const canRun = !simulating && !recording

	const add = async () => {
		const name = await askName('New use case', uniqueName('Use case', state.usecases), 'Create')
		if (name) api().addUsecase(name)
	}

	const rename = async (u: UsecaseInfo) => {
		const name = await askName('Rename use case', u.name, 'Rename')
		if (name) api().setUsecaseProperty(u.id, 'name', name)
	}

	const duplicate = async (u: UsecaseInfo) => {
		const name = await askName('Duplicate use case', uniqueName(`${u.name} copy`, state.usecases), 'Duplicate')
		if (name) api().duplicateUsecase(u.id, name)
	}

	const remove = async (u: UsecaseInfo) => {
		const ok = await dialogs.confirm({ title: 'Delete use case', message: `Delete the use case “${u.name}”?`, confirmLabel: 'Delete', danger: true })
		if (ok) api().deleteUsecase(u.id)
	}

	const record = async (u: UsecaseInfo) => {
		const [delay, between] = api().recordingDefaults()
		const options = await dialogs.custom<RecordOptions>((done) => <RecordDialog usecase={u} defaults={{ realtime: false, delay, between }} done={done} />)
		if (options) api().startRecording(u.id, options.realtime, options.delay, options.between)
	}

	const menu = (u: UsecaseInfo): MenuEntry[] => [
		{ label: 'Play', icon: <Play size={14} />, disabled: !canRun, onSelect: () => api().runUsecase(u.id) },
		{ label: 'Run test', icon: <FlaskConical size={14} />, disabled: !canRun || !u.hasTest, onSelect: () => api().runUsecaseTests(u.id) },
		{ label: 'Record…', icon: <Circle size={14} />, disabled: !editable || !!recording, onSelect: () => void record(u) },
		'separator',
		{ label: 'Rename…', icon: <Pencil size={14} />, disabled: !editable, onSelect: () => void rename(u) },
		{ label: 'Duplicate…', icon: <Copy size={14} />, disabled: !editable, onSelect: () => void duplicate(u) },
		'separator',
		{ label: 'Delete…', icon: <Trash2 size={14} />, danger: true, disabled: !editable, onSelect: () => void remove(u) },
	]

	return (
		<>
			<div className="feature-toolbar">
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void add()}>
					<Plus size={14} /> New use case
				</button>
				<button
					type="button"
					className="btn btn-sm"
					disabled={!canRun || !state.usecases.some((u) => u.hasTest)}
					onClick={() => api().runUsecaseTests(-1)}
					title="Run the tests of all use cases"
				>
					<FlaskConical size={13} /> Test all
				</button>
			</div>

			{recording && <RecordingCard state={state} />}

			{state.usecases.length === 0 ? (
				<div className="empty-small tests-empty">
					<Clapperboard size={20} />
					<p>
						No use cases yet. A use case plays a recorded or scripted sequence of clicks and inputs in the simulation, and can
						check the circuit’s reaction with a test script.
					</p>
				</div>
			) : (
				<div className="feature-list" role="listbox" aria-label="Use cases">
					{state.usecases.map((u) => (
						<div
							key={u.id}
							role="option"
							aria-selected={u.id === selected?.id}
							className={`feature-item${u.id === selected?.id ? ' selected' : ''}`}
							onClick={() => api().selectUsecase(u.id === selected?.id ? -1 : u.id)}
							onDoubleClick={() => editable && void rename(u)}
						>
							<Clapperboard size={14} className="feature-item-icon" />
							<span className="truncate feature-item-name">{u.name || 'Unnamed'}</span>
							{u.hasTest && <FlaskConical size={12} className="feature-item-icon" aria-label="Has a test" />}
							<button
								type="button"
								className="icon-btn small row-action"
								title="Play in the simulation"
								disabled={!canRun}
								onClick={(e) => {
									e.stopPropagation()
									api().runUsecase(u.id)
								}}
							>
								<Play size={13} />
							</button>
							<span onClick={(e) => e.stopPropagation()}>
								<MenuButton entries={() => menu(u)} className="icon-btn small row-action" title="More" align="right">
									<MoreHorizontal size={15} />
								</MenuButton>
							</span>
						</div>
					))}
				</div>
			)}

			{selected && <UsecaseDetails key={selected.id} usecase={selected} editable={editable} onRecord={() => void record(selected)} canRecord={editable && !recording} />}
		</>
	)
}

function UsecaseDetails({ usecase: u, editable, onRecord, canRecord }: { usecase: UsecaseInfo; editable: boolean; onRecord: () => void; canRecord: boolean }) {
	const set = (key: string, value: string) => api().setUsecaseProperty(u.id, key, value)
	const check = useCallback((script: string) => scenariosApi.checkUsecase(u.id, script), [u.id])
	return (
		<section className="prop-group feature-details">
			<h3 className="truncate">{u.name || 'Unnamed'}</h3>
			<FieldRow label="Description" stacked>
				<TextArea value={u.description} disabled={!editable} onCommit={(v) => set('description', v)} />
			</FieldRow>
			<ScriptEditor
				label="Actions"
				title={`Actions of “${u.name}”`}
				value={u.execScript}
				readOnly={!editable}
				check={check}
				onCommit={(v) => set('execScript', v)}
				placeholder="Record the actions, or script them"
				help={<p>Statements executed while the use case plays, e.g. clicks recorded with “Record”.</p>}
				rows={6}
			/>
			<button type="button" className="btn btn-sm record-btn" disabled={!canRecord} onClick={onRecord}>
				<Circle size={12} className="record-dot" /> Record actions…
			</button>
			<ScriptEditor
				label="Test"
				title={`Test of “${u.name}”`}
				value={u.testScript}
				readOnly={!editable}
				check={check}
				onCommit={(v) => set('testScript', v)}
				placeholder="Assertions checked after playing"
				rows={4}
			/>
		</section>
	)
}

interface RecordOptions {
	realtime: boolean
	delay: number
	between: number
}

/** Recording options (desktop: `RecordUsecasePanel`). */
function RecordDialog({ usecase, defaults, done }: { usecase: UsecaseInfo; defaults: RecordOptions; done: (o: RecordOptions | null) => void }) {
	const [options, setOptions] = useState(defaults)
	return (
		<DialogFrame title={`Record “${usecase.name}”`} onCancel={() => done(null)}>
			<div className="dialog-body record-dialog">
				<p className="record-intro">
					The simulation starts and your clicks and key presses in the circuit are recorded. Stop the recording in the inspector, then
					save it as the use case’s actions.
				</p>
				<FieldRow label="Realtime" inline title="Keep the real time between your actions">
					<Switch checked={options.realtime} onChange={(realtime) => setOptions({ ...options, realtime })} />
				</FieldRow>
				<FieldRow label="Press/release delay (ms)">
					<NumberField value={options.delay} min={1} onCommit={(delay) => setOptions({ ...options, delay })} />
				</FieldRow>
				<FieldRow label="Time between actions (ms)">
					<NumberField value={options.between} min={1} disabled={options.realtime} onCommit={(between) => setOptions({ ...options, between })} />
				</FieldRow>
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn" onClick={() => done(null)}>
					Cancel
				</button>
				<button type="button" className="btn btn-primary" autoFocus onClick={() => done(options)}>
					<Circle size={12} className="record-dot" /> Start recording
				</button>
			</div>
		</DialogFrame>
	)
}

function RecordingCard({ state }: { state: ScenarioState }) {
	const recording = state.recording!
	const usecase = state.usecases.find((u) => u.id === recording.usecaseId)
	const statements = recording.log ? recording.log.split('\n').length : 0
	return (
		<div className={`recording-card${recording.active ? ' active' : ''}`}>
			<div className="recording-head">
				<span className={`record-dot big${recording.active ? ' blinking' : ''}`} />
				<div className="recording-title">
					<strong className="truncate">{recording.active ? 'Recording' : 'Recording stopped'}</strong>
					<span className="truncate">
						{usecase?.name} · {statements} {statements === 1 ? 'action' : 'actions'}
					</span>
				</div>
				{recording.active && (
					<button type="button" className="btn btn-sm" onClick={() => api().stopRecording()}>
						<Square size={11} /> Stop
					</button>
				)}
			</div>
			{recording.active && statements === 0 && <div className="recording-tip">Click inputs in the circuit to record them.</div>}
			{recording.log && <pre className="recording-log">{recording.log}</pre>}
			{!recording.active && (
				<div className="recording-actions">
					<button type="button" className="btn btn-sm" onClick={() => api().discardRecording()}>
						Discard
					</button>
					<button type="button" className="btn btn-sm btn-primary" disabled={statements === 0} onClick={() => api().saveRecording()}>
						Save as actions
					</button>
				</div>
			)}
		</div>
	)
}

function askName(title: string, initial: string, confirmLabel: string): Promise<string | null> {
	return dialogs
		.prompt({ title, label: 'Name', initial, confirmLabel, validate: (v) => (v.trim() ? null : 'Please enter a name.') })
		.then((v) => v?.trim() || null)
}
