// A panel docked to the bottom of the canvas with the simulation's issues (desktop: `IssuesViewSwing`) and the
// values logged by probes (desktop: `LogViewSwing`). It stays open while working, so issues can be visited in turn.
import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import { CircleAlert, ScrollText, Trash2, TriangleAlert, X } from 'lucide-react'
import type { DocumentEditorProps } from '../registry'
import { analysisApi, type Issue } from './api'
import { bottomPanel, useBottomPanel, type BottomTab } from './store'

export function BottomPanel({ state }: DocumentEditorProps) {
	const tab = useBottomPanel()
	if (!tab) return null
	return <BottomPanelContent state={state} tab={tab} />
}

function BottomPanelContent({ state, tab }: DocumentEditorProps & { tab: BottomTab }) {
	const issues = analysisApi.issues()
	const errors = issues.filter((i) => i.severity === 'error').length
	return (
		<section
			className="canvas-overlay bottom-left analysis-panel"
			aria-label={tab === 'issues' ? 'Issues' : 'Probe log'}
			onKeyDown={(e) => {
				if (e.key === 'Escape') {
					e.stopPropagation()
					bottomPanel.hide()
				}
			}}
		>
			<div className="analysis-panel-card">
				<header className="analysis-panel-header">
					<div className="tabs analysis-tabs" role="tablist">
						<button
							type="button"
							role="tab"
							aria-selected={tab === 'issues'}
							className={tab === 'issues' ? 'selected' : ''}
							onClick={() => bottomPanel.show('issues')}
						>
							Issues
							{issues.length > 0 && <span className={`analysis-count${errors > 0 ? ' error' : ''}`}>{issues.length}</span>}
						</button>
						<button
							type="button"
							role="tab"
							aria-selected={tab === 'log'}
							className={tab === 'log' ? 'selected' : ''}
							onClick={() => bottomPanel.show('log')}
						>
							Probe log
						</button>
					</div>
					<span className="analysis-panel-spacer" />
					<button
						type="button"
						className="icon-btn small"
						title={tab === 'issues' ? 'Clear issues' : 'Clear log'}
						onClick={() => (tab === 'issues' ? analysisApi.clearIssues() : analysisApi.clearLog())}
					>
						<Trash2 size={14} />
					</button>
					<button type="button" className="icon-btn small" title="Close (Esc)" onClick={() => bottomPanel.hide()}>
						<X size={15} />
					</button>
				</header>
				{tab === 'issues' ? <IssuesList issues={issues} /> : <LogTable simulating={state.mode === 'simulate'} />}
			</div>
		</section>
	)
}

function IssuesList({ issues }: { issues: Issue[] }) {
	const [selected, setSelected] = useState<number | null>(null)
	if (issues.length === 0) {
		return (
			<div className="analysis-empty">
				No issues. Problems found while simulating, and the results of <em>Find unconnected ports</em>, are listed
				here.
			</div>
		)
	}
	return (
		<div className="analysis-scroll" role="listbox" aria-label="Issues">
			{issues.map((issue) => (
				<button
					key={issue.index}
					type="button"
					role="option"
					aria-selected={selected === issue.index}
					className={`issue-row${selected === issue.index ? ' selected' : ''}`}
					title={issue.canOpen ? 'Show where the issue occurs' : undefined}
					onClick={() => {
						setSelected(issue.index)
						analysisApi.openIssue(issue.index)
					}}
				>
					{issue.severity === 'error' ? (
						<CircleAlert size={14} className="issue-icon error" aria-label="Error" />
					) : (
						<TriangleAlert size={14} className="issue-icon warning" aria-label="Warning" />
					)}
					<span className="issue-text">
						<span className="issue-name">{issue.name}</span>
						{issue.description && <span className="issue-description">{issue.description}</span>}
					</span>
					<span className="issue-origin">
						<span className="truncate">{issue.origin}</span>
						{issue.context && <span className="issue-context">{issue.context}</span>}
					</span>
				</button>
			))}
		</div>
	)
}

function LogTable({ simulating }: { simulating: boolean }) {
	const log = analysisApi.log()
	const scroller = useRef<HTMLDivElement>(null)
	const stick = useRef(true)
	const rows = log?.rows.length ?? 0

	// Follow new rows while scrolled to the bottom
	useLayoutEffect(() => {
		const el = scroller.current
		if (el && stick.current) el.scrollTop = el.scrollHeight
	}, [rows])
	useEffect(() => {
		stick.current = true
	}, [simulating])

	if (!log || log.columns.length === 0) {
		return (
			<div className="analysis-empty">
				<ScrollText size={14} /> Probes with logging enabled record their values here while the simulation runs.
			</div>
		)
	}
	return (
		<div
			className="analysis-scroll"
			ref={scroller}
			onScroll={(e) => {
				const el = e.currentTarget
				stick.current = el.scrollTop + el.clientHeight >= el.scrollHeight - 4
			}}
		>
			<table className="log-table">
				<thead>
					<tr>
						<th>Time</th>
						{log.columns.map((c, i) => (
							<th key={i}>{c}</th>
						))}
					</tr>
				</thead>
				<tbody>
					{log.truncated && (
						<tr>
							<td colSpan={log.columns.length + 1} className="log-truncated">
								Older entries are not shown
							</td>
						</tr>
					)}
					{log.rows.map((r, i) => (
						<tr key={i}>
							<td>{r.time}</td>
							{r.values.map((v, j) => (
								<td key={j}>{v}</td>
							))}
						</tr>
					))}
				</tbody>
			</table>
		</div>
	)
}
