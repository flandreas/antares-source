import { useCallback, useState } from 'react'
import { ArrowDown, ArrowUp, Copy, ListChecks, MoreHorizontal, Pencil, Play, Plus, Trash2 } from 'lucide-react'
import { dialogs } from '../../Dialogs'
import { MenuButton, type MenuEntry } from '../../Menu'
import type { DocumentEditorProps } from '../registry'
import { statusOf, testcasesApi, useTestResults, type TestcaseInfo, type TestResult } from './api'
import { ScriptEditor } from './ScriptEditor'
import { reportResults, showResults, StatusIcon, summarize } from './TestResults'
import { FieldRow, NumberField, Switch, TextArea } from './fields'

const SCRIPT_HELP = (
	<>
		<p>
			The first line names the circuit’s inputs and outputs (prefix a name with <code>&gt;</code> for an input or{' '}
			<code>&lt;</code> for an output if needed). Every following line is a test vector with one value per column.
		</p>
		<p>
			Values: <code>12</code>, <code>0x1F</code>, <code>0b101</code>, <code>X</code> (don’t care), <code>Z</code> (undefined),{' '}
			<code>^1</code> (clocked), <code>(i + 1)</code> (expression). <code>run {'{ … }'}</code> applies vectors in sequence without
			resetting the circuit, <code>loop i 0..7 {'{ … }'}</code> repeats vectors. Lines starting with <code>//</code> are comments.
		</p>
	</>
)

/** Test cases of the open digital circuit (desktop: "Test cases" pane and test results pane). */
export function TestsPanel({ state }: DocumentEditorProps) {
	const list = testcasesApi.list()
	const snapshot = useTestResults()
	const [selectedId, setSelectedId] = useState<number | null>(null)
	const [running, setRunning] = useState(false)

	const circuitUuid = state.circuit?.uuid ?? null
	// Results of this circuit, from a circuit or project run
	const results = (snapshot?.results ?? []).filter((r) => r.circuitUuid === circuitUuid)
	const resultOf = (t: TestcaseInfo) => results.find((r) => r.testcaseId === t.id)

	const items = list?.items ?? []
	const selected = items.find((t) => t.id === selectedId) ?? items[0] ?? null
	const editable = !!list?.editable
	const canRun = !!list?.digital && !list.simulating

	/** Runs tests after the UI has shown the busy state, since tests run synchronously. */
	const runLater = useCallback((run: () => TestResult[], scope: string | null) => {
		setRunning(true)
		window.setTimeout(() => {
			try {
				reportResults(run(), scope)
			} finally {
				setRunning(false)
			}
		}, 30)
	}, [])

	if (!list) return <div className="inspector-empty">Open a circuit to see its test cases.</div>
	if (!list.digital) {
		return (
			<div className="panel-scroll inspector-body">
				<div className="empty-small tests-empty">
					<ListChecks size={20} />
					<p>Test cases are available for digital circuits.</p>
				</div>
			</div>
		)
	}

	const add = async () => {
		const name = await dialogs.prompt({
			title: 'New test case',
			label: 'Name',
			initial: uniqueName('Test', items),
			confirmLabel: 'Create',
			validate: (v) => (v.trim() ? null : 'Please enter a name.'),
		})
		if (!name) return
		const id = testcasesApi.raw.add(name.trim())
		if (id >= 0) setSelectedId(id)
	}

	const rename = async (t: TestcaseInfo) => {
		const name = await dialogs.prompt({ title: 'Rename test case', label: 'Name', initial: t.name, confirmLabel: 'Rename' })
		if (name?.trim()) testcasesApi.raw.setProperty(t.id, 'name', name.trim())
	}

	const duplicate = async (t: TestcaseInfo) => {
		const name = await dialogs.prompt({
			title: 'Duplicate test case',
			label: 'Name of the copy',
			initial: uniqueName(`${t.name} copy`, items),
			confirmLabel: 'Duplicate',
		})
		if (!name?.trim()) return
		const id = testcasesApi.raw.duplicate(t.id, name.trim())
		if (id >= 0) setSelectedId(id)
	}

	const remove = async (t: TestcaseInfo) => {
		const ok = await dialogs.confirm({
			title: 'Delete test case',
			message: `Delete the test case “${t.name}”? You can undo this.`,
			confirmLabel: 'Delete',
			danger: true,
		})
		if (ok) testcasesApi.raw.delete(t.id)
	}

	const runOne = (t: TestcaseInfo) => runLater(() => testcasesApi.run([t.id]), circuitUuid)

	const itemMenu = (t: TestcaseInfo, index: number): MenuEntry[] => [
		{ label: 'Run', icon: <Play size={14} />, disabled: !canRun, onSelect: () => runOne(t) },
		'separator',
		{ label: 'Rename…', icon: <Pencil size={14} />, disabled: !editable, onSelect: () => void rename(t) },
		{ label: 'Duplicate…', icon: <Copy size={14} />, disabled: !editable, onSelect: () => void duplicate(t) },
		{ label: 'Move up', icon: <ArrowUp size={14} />, disabled: !editable || index === 0, onSelect: () => testcasesApi.raw.move(t.id, -1) },
		{
			label: 'Move down',
			icon: <ArrowDown size={14} />,
			disabled: !editable || index === items.length - 1,
			onSelect: () => testcasesApi.raw.move(t.id, 1),
		},
		'separator',
		{ label: 'Delete…', icon: <Trash2 size={14} />, danger: true, disabled: !editable, onSelect: () => void remove(t) },
	]

	const moreMenu = (): MenuEntry[] => [
		{
			label: 'Run all tests of the project',
			icon: <ListChecks size={14} />,
			disabled: !canRun,
			onSelect: () => runLater(() => testcasesApi.runProject(), null),
		},
		{
			label: 'Show last results',
			disabled: !snapshot || snapshot.results.length === 0,
			onSelect: () => snapshot && void showResults(snapshot.results),
		},
	]

	const summary = results.length > 0 ? summarize(results) : null

	return (
		<div className="panel-scroll inspector-body tests-panel">
			<div className="feature-toolbar">
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void add()}>
					<Plus size={14} /> New
				</button>
				<button
					type="button"
					className="btn btn-sm"
					disabled={!canRun || items.length === 0 || running}
					onClick={() => runLater(() => testcasesApi.runAll(), circuitUuid)}
					title="Run all test cases of this circuit"
				>
					{running ? <span className="spinner small" /> : <Play size={13} />} Run all
				</button>
				<span className="feature-toolbar-spacer" />
				<MenuButton entries={moreMenu} className="icon-btn small" title="More" align="right">
					<MoreHorizontal size={15} />
				</MenuButton>
			</div>

			{list.simulating && <div className="hint">Stop the simulation to edit or run test cases.</div>}

			{summary && (
				<button
					type="button"
					className={`tests-summary ${summary.failed ? 'failed' : 'passed'}`}
					onClick={() => void showResults(results)}
					title="Show the results"
				>
					<StatusIcon status={summary.failed ? 'failed' : 'passed'} />
					<span className="truncate">{summary.text}</span>
					<span className="tests-summary-link">Details</span>
				</button>
			)}

			{items.length === 0 ? (
				<div className="empty-small tests-empty">
					<ListChecks size={20} />
					<p>No test cases yet. A test case lists input values and the outputs the circuit should produce for them.</p>
				</div>
			) : (
				<div className="feature-list" role="listbox" aria-label="Test cases">
					{items.map((t, i) => {
						const result = resultOf(t)
						return (
							<div
								key={t.id}
								role="option"
								aria-selected={t.id === selected?.id}
								className={`feature-item${t.id === selected?.id ? ' selected' : ''}${t.ignored ? ' muted' : ''}`}
								onClick={() => setSelectedId(t.id)}
								onDoubleClick={() => editable && void rename(t)}
							>
								<StatusIcon status={result ? statusOf(result) : null} />
								<span className="truncate feature-item-name">{t.name || 'Unnamed'}</span>
								{t.ignored && <span className="badge">Ignored</span>}
								<button
									type="button"
									className="icon-btn small row-action"
									title="Run"
									disabled={!canRun || running}
									onClick={(e) => {
										e.stopPropagation()
										runOne(t)
									}}
								>
									<Play size={13} />
								</button>
								<span onClick={(e) => e.stopPropagation()}>
									<MenuButton entries={() => itemMenu(t, i)} className="icon-btn small row-action" title="More" align="right">
										<MoreHorizontal size={15} />
									</MenuButton>
								</span>
							</div>
						)
					})}
				</div>
			)}

			{selected && (
				<TestcaseDetails
					key={`${circuitUuid}:${selected.id}`}
					testcase={selected}
					result={resultOf(selected)}
					editable={editable}
				/>
			)}
		</div>
	)
}

function TestcaseDetails({ testcase: t, result, editable }: { testcase: TestcaseInfo; result?: TestResult; editable: boolean }) {
	const set = (key: string, value: string | boolean | number) => testcasesApi.raw.setProperty(t.id, key, String(value))
	const check = useCallback((script: string) => testcasesApi.check(t.id, script), [t.id])
	return (
		<section className="prop-group feature-details">
			<h3 className="truncate">{t.name || 'Unnamed'}</h3>
			<ScriptEditor
				label="Test vectors"
				title={`Test vectors of “${t.name}”`}
				value={t.script}
				readOnly={!editable}
				check={check}
				onCommit={(s) => set('script', s)}
				placeholder={'A  B  Y\n0  0  0\n1  1  1'}
				help={SCRIPT_HELP}
				rows={9}
			/>
			{result && statusOf(result) === 'failed' && (
				<button type="button" className="test-inline-result" onClick={() => void showResults([result], result)}>
					<StatusIcon status="failed" size={13} />
					<span className="truncate">{result.error ?? `${result.failedCount} failed – show details`}</span>
				</button>
			)}
			<FieldRow label="Ignore" inline>
				<Switch checked={t.ignored} disabled={!editable} onChange={(v) => set('ignored', v)} />
			</FieldRow>
			<FieldRow label="Skip delay check" inline title="Skip comparing the measured propagation delay with the circuit’s configured delay">
				<Switch checked={t.skipPropDelayCheck} disabled={!editable} onChange={(v) => set('skipPropDelayCheck', v)} />
			</FieldRow>
			<FieldRow label="Max. iterations" title="Simulation steps per test vector before the test fails">
				<NumberField value={t.iterations} min={1} disabled={!editable} onCommit={(n) => set('iterations', n)} />
			</FieldRow>
			<FieldRow label="Description" stacked>
				<TextArea value={t.description} disabled={!editable} onCommit={(v) => set('description', v)} />
			</FieldRow>
		</section>
	)
}

/** Returns [base], or [base] with the lowest free number appended. */
export function uniqueName(base: string, items: Array<{ name: string }>): string {
	const names = new Set(items.map((i) => i.name))
	if (!names.has(base)) return base
	for (let n = 2; ; n++) if (!names.has(`${base} ${n}`)) return `${base} ${n}`
}
