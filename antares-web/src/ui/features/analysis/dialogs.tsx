// Dialogs of the Tools menu: find unconnected ports (desktop: `FindUnconnectedPortsPanel`), circuit statistics
// (`GraphStatisticsPanel`) and the propagation delay (`CalculatePropagationDelayAction`).
import { useMemo, useState } from 'react'
import { Boxes, TriangleAlert } from 'lucide-react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { analysisApi, type Statistics, type UnconnectedScope, type UnconnectedType } from './api'
import { bottomPanel } from './store'

const TYPE_STORAGE_KEY = 'antares.unconnectedType'

const TYPES: Array<{ id: UnconnectedType; label: string; description: string }> = [
	{ id: 'inputs', label: 'Inputs', description: 'Component inputs without a wire.' },
	{ id: 'outputs', label: 'Outputs', description: 'Component outputs without a wire.' },
	{
		id: 'all',
		label: 'All',
		description: 'All unconnected pins, plus tunnels without counterpart or name and wires with an open end.',
	},
]

function storedType(): UnconnectedType {
	try {
		const t = localStorage.getItem(TYPE_STORAGE_KEY)
		return TYPES.some((x) => x.id === t) ? (t as UnconnectedType) : 'inputs'
	} catch {
		return 'inputs'
	}
}

/** Asks what to look for, then lists the results in the issues panel like the desktop. */
export function showFindUnconnected(initialScope: UnconnectedScope = 'circuit') {
	return dialogs.custom<null>((done) => <FindUnconnectedDialog initialScope={initialScope} onClose={() => done(null)} />)
}

function FindUnconnectedDialog({ initialScope, onClose }: { initialScope: UnconnectedScope; onClose: () => void }) {
	const hasCircuit = !!engine.state.circuit
	const [scope, setScope] = useState<UnconnectedScope>(hasCircuit ? initialScope : 'project')
	const [type, setType] = useState<UnconnectedType>(storedType)
	const [nothing, setNothing] = useState(false)

	const run = () => {
		try {
			localStorage.setItem(TYPE_STORAGE_KEY, type)
		} catch {
			// Storage unavailable
		}
		const count = analysisApi.unconnected(scope, type)
		if (count > 0) {
			onClose()
			bottomPanel.show('issues')
		} else setNothing(true)
	}

	return (
		<DialogFrame title="Find unconnected ports" onCancel={onClose}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					run()
				}}
			>
				<div className="dialog-body analysis-form">
					<div className="field">
						<span className="field-label">Search in</span>
						<div className="segmented" role="radiogroup" aria-label="Search in">
							{(
								[
									['circuit', `Circuit “${engine.state.circuit?.name ?? ''}”`],
									['project', 'Whole project'],
								] as const
							).map(([id, label]) => (
								<button
									key={id}
									type="button"
									role="radio"
									aria-checked={scope === id}
									className={scope === id ? 'selected' : ''}
									disabled={id === 'circuit' && !hasCircuit}
									onClick={() => {
										setScope(id)
										setNothing(false)
									}}
								>
									<span className="truncate">{label}</span>
								</button>
							))}
						</div>
					</div>
					<div className="field">
						<span className="field-label">Look for</span>
						<div className="analysis-options" role="radiogroup" aria-label="Look for">
							{TYPES.map((t) => (
								<label key={t.id} className={`analysis-option${type === t.id ? ' selected' : ''}`}>
									<input
										type="radio"
										name="unconnected-type"
										checked={type === t.id}
										onChange={() => {
											setType(t.id)
											setNothing(false)
										}}
									/>
									<span>
										<strong>{t.label}</strong>
										<span>{t.description}</span>
									</span>
								</label>
							))}
						</div>
					</div>
					{nothing && (
						<div className="analysis-result-ok" role="status">
							Nothing found – {scope === 'project' ? 'all circuits are' : 'the circuit is'} fully connected.
						</div>
					)}
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={onClose}>
						Close
					</button>
					<button type="submit" className="btn btn-primary" autoFocus>
						Find
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}

/** ---- Statistics */

type StatisticsTab = 'immediate' | 'flat' | 'deep'

const STATISTICS_TABS: Array<{ id: StatisticsTab; label: string; description: string }> = [
	{ id: 'immediate', label: 'Immediate', description: 'Components placed directly in this circuit.' },
	{
		id: 'flat',
		label: 'Flat',
		description: 'Components on all levels of sub-circuits; sub-circuits that are implemented by a script count as one component.',
	},
	{ id: 'deep', label: 'Deep', description: 'Components on all levels, including the insides of scripted sub-circuits.' },
]

/** Counts the components of the displayed (sub-)circuit. */
export function showStatistics() {
	const statistics = analysisApi.statistics()
	if (!statistics) return
	return dialogs.custom<null>((done) => <StatisticsDialog statistics={statistics} onClose={() => done(null)} />)
}

function StatisticsDialog({ statistics, onClose }: { statistics: Statistics; onClose: () => void }) {
	const [tab, setTab] = useState<StatisticsTab>('immediate')
	const entries = statistics[tab]
	const total = useMemo(() => entries.reduce((sum, e) => sum + e.count, 0), [entries])
	const info = STATISTICS_TABS.find((t) => t.id === tab)!
	return (
		<DialogFrame title={`Statistics of “${statistics.circuit}”`} onCancel={onClose}>
			<div className="tabs analysis-dialog-tabs" role="tablist">
				{STATISTICS_TABS.map((t) => (
					<button
						key={t.id}
						type="button"
						role="tab"
						aria-selected={tab === t.id}
						className={tab === t.id ? 'selected' : ''}
						onClick={() => setTab(t.id)}
					>
						{t.label}
					</button>
				))}
			</div>
			<p className="analysis-dialog-hint">{info.description}</p>
			<div className="statistics-list" role="table" aria-label={`${info.label} statistics`}>
				{entries.length === 0 && <div className="empty-small">The circuit is empty.</div>}
				{entries.map((e) => (
					<div key={`${e.subCircuit}:${e.name}`} className="statistics-row" role="row">
						<span className="statistics-count" role="cell">
							{e.count}
						</span>
						<span className={`statistics-name${e.scripted ? ' scripted' : ''}`} role="cell">
							{e.subCircuit && <Boxes size={13} aria-label="Sub-circuit" />}
							<span className="truncate">{e.name}</span>
						</span>
					</div>
				))}
			</div>
			<div className="dialog-actions analysis-dialog-footer">
				<span className="subtle">
					{total} {total === 1 ? 'component' : 'components'} · {entries.length} {entries.length === 1 ? 'kind' : 'kinds'}
				</span>
				<button type="button" className="btn btn-primary" onClick={onClose} autoFocus>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}

/** ---- Propagation delay */

/** Calculates the longest propagation delay through the open circuit. */
export function showPropagationDelay() {
	const delay = analysisApi.propagationDelay()
	const circuit = engine.state.circuit?.name ?? ''
	return dialogs.custom<null>((done) => (
		<DialogFrame title="Propagation delay" onCancel={() => done(null)}>
			<div className="dialog-body">
				{delay < 0 ? (
					<div className="analysis-delay undefined">
						<TriangleAlert size={18} />
						<span>The propagation delay of “{circuit}” cannot be calculated, because it has no connected inputs or outputs.</span>
					</div>
				) : (
					<div className="analysis-delay">
						<span className="analysis-delay-value">
							{delay} <small>ns</small>
						</span>
						<span>
							The longest time a change at an input of “{circuit}” needs to reach an output, summed up over the
							components on the way.
						</span>
					</div>
				)}
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn btn-primary" onClick={() => done(null)} autoFocus>
					Close
				</button>
			</div>
		</DialogFrame>
	))
}
