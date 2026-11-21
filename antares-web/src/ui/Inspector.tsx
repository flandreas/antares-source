import { useEffect, useRef, useState } from 'react'
import { engine } from '../core/engine'
import { circuitPanels, documentInspectors } from './features/registry'
import { useStoredState } from './hooks'
import { kindTitle } from './projectActions'
import type { EditorState, PropertyChoice, PropertyDescriptor } from '../core/types'

const groupLabels: Record<string, string> = {
	general: 'Properties',
	appearance: 'Appearance',
	ports: 'Ports',
	simulation: 'Simulation',
	documentation: 'Documentation',
}

const groupLabel = (g: string) => groupLabels[g] ?? g.charAt(0).toUpperCase() + g.slice(1)

export function Inspector({ state }: { state: EditorState }) {
	const [tab, setTab] = useStoredState('antares.inspectorTab', 'properties')

	if (state.document) {
		const DocumentInspector = documentInspectors[state.document.kind]
		return (
			<aside className="inspector">
				<header className="inspector-header">
					<div className="inspector-kind">{kindTitle(state.document.kind)}</div>
					<h2 className="truncate">{state.document.name}</h2>
				</header>
				<div className="panel-scroll inspector-body">
					{DocumentInspector ? <DocumentInspector state={state} /> : <div className="empty-small">No properties.</div>}
				</div>
			</aside>
		)
	}

	const panel = state.circuit ? circuitPanels.find((p) => p.id === tab) : undefined
	const tabs =
		state.circuit && circuitPanels.length > 0 ? (
			<div className="tabs inspector-tabs" role="tablist">
				{[{ id: 'properties', label: 'Properties' }, ...circuitPanels].map((t) => (
					<button
						key={t.id}
						type="button"
						role="tab"
						aria-selected={(panel?.id ?? 'properties') === t.id}
						className={(panel?.id ?? 'properties') === t.id ? 'selected' : ''}
						onClick={() => setTab(t.id)}
					>
						{t.label}
					</button>
				))}
			</div>
		) : null

	if (panel) {
		const Panel = panel.component
		return (
			<aside className="inspector">
				{tabs}
				<Panel state={state} />
			</aside>
		)
	}

	return <PropertiesInspector state={state} tabs={tabs} />
}

function PropertiesInspector({ state, tabs }: { state: EditorState; tabs: React.ReactNode }) {
	const sheet = state.circuit ? engine.properties : null

	if (!sheet) {
		return (
			<aside className="inspector">
				{tabs}
				<div className="inspector-empty">Select a component to edit its properties.</div>
			</aside>
		)
	}

	const editable = sheet.editable && state.mode === 'edit'
	const groups = new Map<string, PropertyDescriptor[]>()
	// The component ID is shown in the header; other read-only details go last in their group
	const ordered = sheet.properties
		.filter((p) => p.key !== 'id')
		.sort((a, b) => Number(a.type === 'readonly') - Number(b.type === 'readonly'))
	for (const p of ordered) {
		if (!groups.has(p.group)) groups.set(p.group, [])
		groups.get(p.group)!.push(p)
	}
	const beanKey = `${sheet.title}|${sheet.subtitle}`

	return (
		<aside className="inspector">
			{tabs}
			<header className="inspector-header">
				<div className="inspector-kind">{sheet.isCircuit ? 'Circuit' : state.selectionCount > 1 ? 'Selection' : 'Component'}</div>
				<h2 className="truncate" title={sheet.title}>
					{sheet.title}
				</h2>
				{sheet.subtitle && <span className="inspector-sub">{sheet.subtitle}</span>}
			</header>
			<div className="panel-scroll inspector-body">
				{state.mode === 'simulate' && <div className="hint">Stop the simulation to change properties.</div>}
				{ordered.length === 0 && (
					<div className="empty-small">
						{sheet.isCircuit ? 'Select a component to edit its properties.' : 'This component has no editable properties.'}
					</div>
				)}
				{[...groups].map(([group, props]) => (
					<section key={group} className="prop-group">
						<h3>{groupLabel(group)}</h3>
						{props.map((p) => (
							<PropertyRow key={`${beanKey}|${p.key}`} property={p} editable={editable} />
						))}
					</section>
				))}
				{sheet.isCircuit && <CircuitHelp />}
			</div>
		</aside>
	)
}

function CircuitHelp() {
	return (
		<section className="prop-group tips">
			<h3>Tips</h3>
			<ul>
				<li>Drag from a pin to another pin to connect them; drag from a wire to branch it.</li>
				<li>Use “Circuit Input” and “Circuit Output” to give this circuit pins when it’s used as a sub-circuit.</li>
				<li>Select several parts with a selection rectangle or Shift-click.</li>
			</ul>
		</section>
	)
}

function PropertyRow({ property: p, editable }: { property: PropertyDescriptor; editable: boolean }) {
	const readOnly = !editable || p.type === 'readonly'
	const id = `prop-${p.key}`
	const control = (() => {
		if (p.type === 'boolean') {
			return (
				<label className="switch">
					<input
						id={id}
						type="checkbox"
						checked={p.value === true}
						disabled={readOnly}
						onChange={(e) => engine.setProperty(p.key, e.target.checked)}
					/>
					<span className="switch-track" />
				</label>
			)
		}
		if ((p.type === 'choice' || p.type === 'color') && p.choices) {
			return (
				<div className="select-wrap">
					{p.type === 'color' && <ColorDot choice={p.choices.find((c) => c.id === p.value)} />}
					<select
						id={id}
						className="input select"
						value={p.value === null ? '' : String(p.value)}
						disabled={readOnly}
						onChange={(e) => engine.setProperty(p.key, e.target.value)}
					>
						{p.value === null && !p.choices.some((c) => c.id === '') && <option value="">Default</option>}
						{p.choices.map((c) => (
							<option key={c.id} value={c.id}>
								{c.label}
							</option>
						))}
					</select>
				</div>
			)
		}
		if (p.type === 'readonly' && typeof p.value !== 'object') {
			const text = typeof p.value === 'boolean' ? (p.value ? 'Yes' : 'No') : String(p.value ?? '—')
			return <div className="prop-static">{text}</div>
		}
		return <TextField id={id} property={p} readOnly={readOnly} />
	})()

	return (
		<div className={`prop-row${p.type === 'multiline' ? ' stacked' : ''}${p.type === 'boolean' ? ' inline' : ''}`}>
			<label htmlFor={id} className="prop-label" title={p.label}>
				{p.label}
			</label>
			<div className="prop-control">{control}</div>
		</div>
	)
}

function ColorDot({ choice }: { choice?: PropertyChoice }) {
	return <span className="color-dot" style={{ background: choice?.color ?? 'transparent' }} />
}

/** A text or number field that commits on blur or Enter, and reverts on Escape. */
function TextField({ id, property: p, readOnly }: { id: string; property: PropertyDescriptor; readOnly: boolean }) {
	const external = p.value === null || p.value === undefined ? '' : String(p.value)
	const [value, setValue] = useState(external)
	const [focused, setFocused] = useState(false)
	const reverting = useRef(false)
	useEffect(() => {
		if (!focused) setValue(external)
	}, [external, focused])

	const numeric = p.type === 'integer' || p.type === 'number'
	const commit = () => {
		if (reverting.current) {
			reverting.current = false
			setValue(external)
			return
		}
		if (value === external) return
		if (numeric) {
			const n = Number(value)
			if (value.trim() === '' || Number.isNaN(n) || (p.type === 'integer' && !Number.isInteger(n))) {
				setValue(external)
				engine.message('warning', p.label, p.type === 'integer' ? 'Please enter a whole number.' : 'Please enter a number.')
				return
			}
			if ((p.min !== null && n < p.min) || (p.max !== null && n > p.max)) {
				setValue(external)
				engine.message('warning', p.label, `Please enter a value between ${p.min ?? '−∞'} and ${p.max ?? '∞'}.`)
				return
			}
			engine.setProperty(p.key, n)
		} else {
			engine.setProperty(p.key, value)
		}
	}

	const common = {
		id,
		className: 'input',
		value,
		readOnly,
		onFocus: () => setFocused(true),
		onBlur: () => {
			setFocused(false)
			commit()
		},
	}

	if (p.type === 'multiline') {
		return (
			<textarea
				{...common}
				rows={3}
				onChange={(e) => setValue(e.target.value)}
				onKeyDown={(e) => {
					if (e.key === 'Escape') {
						reverting.current = true
						;(e.target as HTMLTextAreaElement).blur()
					}
					if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) (e.target as HTMLTextAreaElement).blur()
				}}
			/>
		)
	}
	return (
		<input
			{...common}
			type={numeric ? 'number' : 'text'}
			min={p.min ?? undefined}
			max={p.max ?? undefined}
			step={p.type === 'integer' ? 1 : 'any'}
			inputMode={numeric ? 'decimal' : undefined}
			onChange={(e) => setValue(e.target.value)}
			onKeyDown={(e) => {
				if (e.key === 'Enter') (e.target as HTMLInputElement).blur()
				if (e.key === 'Escape') {
					reverting.current = true
					;(e.target as HTMLInputElement).blur()
				}
			}}
		/>
	)
}
