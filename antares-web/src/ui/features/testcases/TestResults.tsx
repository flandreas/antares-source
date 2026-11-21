import { useState } from 'react'
import { CircleCheck, CircleSlash, CircleX, Cpu, FileCode } from 'lucide-react'
import { DialogFrame, dialogs } from '../../Dialogs'
import { engine } from '../../../core/engine'
import { statusOf, testResults, type TestResult, type TestRun, type TestStatus } from './api'

export function StatusIcon({ status, size = 14 }: { status: TestStatus | null; size?: number }) {
	if (status === 'passed') return <CircleCheck size={size} className="test-status passed" aria-label="Passed" />
	if (status === 'failed') return <CircleX size={size} className="test-status failed" aria-label="Failed" />
	if (status === 'ignored') return <CircleSlash size={size} className="test-status ignored" aria-label="Ignored" />
	return <span className="test-status none" />
}

/** "3 passed · 1 failed · 1 ignored" */
export function summarize(results: TestResult[]): { text: string; failed: number } {
	const count = (s: TestStatus) => results.filter((r) => statusOf(r) === s).length
	const failed = count('failed')
	const parts = [`${count('passed')} passed`]
	if (failed) parts.push(`${failed} failed`)
	if (count('ignored')) parts.push(`${count('ignored')} ignored`)
	return { text: parts.join(' · '), failed }
}

/** Stores the results and reports them: a toast if all passed, the results dialog otherwise. */
export function reportResults(results: TestResult[], circuitUuid: string | null, alwaysShow = false) {
	testResults.set({ results, circuitUuid })
	if (results.length === 0) {
		engine.message('info', 'No test cases', circuitUuid ? 'This circuit has no test cases yet.' : 'No circuit of this project has test cases.')
		return
	}
	const { text, failed } = summarize(results)
	if (failed === 0 && !alwaysShow) {
		engine.message('success', results.length === 1 ? 'Test passed' : 'All tests passed', text)
	} else {
		void showResults(results, failed > 0 ? results.find((r) => statusOf(r) === 'failed') : results[0])
	}
}

export function showResults(results: TestResult[], initial?: TestResult) {
	return dialogs.custom<null>((done) => <ResultsDialog results={results} initial={initial} onClose={() => done(null)} />)
}

const keyOf = (r: TestResult) => `${r.circuitUuid}:${r.testcaseId}`

function ResultsDialog({ results, initial, onClose }: { results: TestResult[]; initial?: TestResult; onClose: () => void }) {
	const [selected, setSelected] = useState(keyOf(initial ?? results[0]))
	const current = results.find((r) => keyOf(r) === selected) ?? results[0]
	const circuits = [...new Set(results.map((r) => r.circuitUuid))]
	const { text, failed } = summarize(results)

	return (
		<DialogFrame title="Test results" onCancel={onClose} wide>
			<div className="test-results">
				<nav className="test-results-list" aria-label="Test cases">
					<div className={`test-results-total ${failed ? 'failed' : 'passed'}`}>
						<StatusIcon status={failed ? 'failed' : 'passed'} />
						<span>{text}</span>
					</div>
					{circuits.map((uuid) => {
						const group = results.filter((r) => r.circuitUuid === uuid)
						return (
							<section key={uuid}>
								{circuits.length > 1 && <h4 className="truncate">{group[0].circuit}</h4>}
								{group.map((r) => (
									<button
										key={keyOf(r)}
										type="button"
										className={`test-results-item${keyOf(r) === keyOf(current) ? ' selected' : ''}`}
										onClick={() => setSelected(keyOf(r))}
									>
										<StatusIcon status={statusOf(r)} />
										<span className="truncate">{r.testcase}</span>
									</button>
								))}
							</section>
						)
					})}
				</nav>
				<ResultDetails key={keyOf(current)} result={current} />
			</div>
		</DialogFrame>
	)
}

/** Details of one test case: summary, general checks and the test vectors per run (circuit/script). */
function ResultDetails({ result }: { result: TestResult }) {
	const [runIndex, setRunIndex] = useState(() => Math.max(0, result.runs.findIndex((r) => r.failedCount > 0 || r.error)))
	const run = result.runs[runIndex] as TestRun | undefined
	const status = statusOf(result)
	const summary = result.ignored
		? 'Ignored'
		: result.error
			? result.error
			: result.failedCount === 0
				? 'Passed'
				: `Failed: ${result.failedCount} ${result.failedCount === 1 ? 'test vector' : 'test vectors'}`

	return (
		<div className="test-results-detail">
			<header className="test-results-header">
				<StatusIcon status={status} size={16} />
				<div className="test-results-title">
					<h3 className="truncate">{result.testcase}</h3>
					<span className={`test-results-summary ${status}`}>{summary}</span>
				</div>
			</header>
			{result.general.length > 0 && (
				<ul className="test-general">
					{result.general.map((g, i) => (
						<li key={i}>
							<StatusIcon status={g.error ? 'failed' : 'passed'} size={13} />
							<span>{g.description}</span>
						</li>
					))}
				</ul>
			)}
			{result.runs.length > 1 && (
				<div className="segmented small test-run-tabs" role="tablist">
					{result.runs.map((r, i) => (
						<button
							key={r.type}
							type="button"
							role="tab"
							title={r.tooltip}
							aria-selected={i === runIndex}
							className={i === runIndex ? 'selected' : ''}
							onClick={() => setRunIndex(i)}
						>
							{r.type === 'circuit' ? <Cpu size={13} /> : <FileCode size={13} />}
							{r.label}
							<StatusIcon status={r.failedCount > 0 || r.error ? 'failed' : 'passed'} size={12} />
						</button>
					))}
				</div>
			)}
			{run && <RunTable run={run} single={result.runs.length === 1} />}
		</div>
	)
}

function RunTable({ run, single }: { run: TestRun; single: boolean }) {
	if (run.error) return <div className="test-run-error">{run.error}</div>
	const vectorText = (n: number) => `${n} ${n === 1 ? 'test vector' : 'test vectors'}`
	return (
		<>
			<div className="test-run-meta">
				{single && <span title={run.tooltip}>{run.label}</span>}
				<span>
					{run.failedCount === 0 ? `All ${vectorText(run.rows.length)} passed` : `${run.failedCount} of ${vectorText(run.rows.length)} failed`}
				</span>
				{run.duration > 0 && <span>Propagation {run.duration} ns</span>}
			</div>
			<div className="test-table-wrap">
				<table className="test-table">
					<thead>
						<tr>
							<th className="test-block" aria-label="Run block" />
							<th className="test-desc">Description</th>
							{run.columns.map((c, i) => (
								<th key={i} className={c.output ? 'output' : 'input'} title={c.output ? 'Output' : 'Input'}>
									{c.name}
								</th>
							))}
						</tr>
					</thead>
					<tbody>
						{run.rows.map((row, i) => {
							const failed = row.cells.some((c) => c.state === 'failed')
							return (
								<tr key={i} className={failed ? 'failed' : undefined}>
									<td className={`test-block ${row.kind}`} title={row.kind === 'top' ? undefined : 'Part of a run block'} />
									<td className="test-desc">{row.description}</td>
									{row.cells.map((c, j) => (
										<td key={j} className={`test-cell ${c.state}`}>
											{c.state === 'failed' ? (
												<span className="test-mismatch">
													<span className="expected" title="Expected">
														{c.expected}
													</span>
													<span className="actual" title="Actual">
														{c.text}
													</span>
												</span>
											) : c.state === 'passed' && c.dontCare ? (
												<span title={`Expected ${c.expected} (don’t care), actual ${c.text}`}>{c.text}</span>
											) : (
												c.text
											)}
										</td>
									))}
								</tr>
							)
						})}
					</tbody>
				</table>
			</div>
			{run.rows.length > 0 && run.columns.some((c) => c.output) && (
				<div className="test-legend">
					<span className="test-legend-item in">Input</span>
					<span className="test-legend-item passed">Output as expected</span>
					<span className="test-legend-item failed">
						<span className="expected">expected</span> <span className="actual">actual</span>
					</span>
				</div>
			)}
		</>
	)
}
