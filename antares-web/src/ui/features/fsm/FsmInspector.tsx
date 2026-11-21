import { useEffect, useRef, useState } from 'react'
import { engine } from '../../../core/engine'
import type { PropertyDescriptor } from '../../../core/types'
import { useEngineVersion } from '../../hooks'
import type { DocumentEditorProps } from '../registry'
import { fsmApi } from './api'

const groupLabels: Record<string, string> = { general: 'Properties', documentation: 'Documentation', appearance: 'Appearance' }

/** Properties of the selected states/transitions, or of the state machine (FSM*BeanInfo). */
export function FsmInspector({ state }: DocumentEditorProps) {
	useEngineVersion()
	const sheet = fsmApi.properties()
	const fsm = fsmApi.state()
	if (!sheet || !fsm) return null

	const editable = sheet.editable && !!state.document?.editable
	const ordered = sheet.properties.filter((p) => p.key !== 'id')
	const groups = new Map<string, PropertyDescriptor[]>()
	for (const p of ordered) {
		if (!groups.has(p.group)) groups.set(p.group, [])
		groups.get(p.group)!.push(p)
	}
	const beanKey = `${sheet.title}|${sheet.subtitle}`
	const kind = sheet.isCircuit ? 'State machine' : fsm.selectionCount > 1 ? 'Selection' : sheet.title

	return (
		<>
			<div className="fsm-inspector-title">
				<span>{kind}</span>
				{sheet.subtitle && <span className="inspector-sub">{sheet.subtitle}</span>}
			</div>
			{ordered.length === 0 && <div className="empty-small">No common properties.</div>}
			{[...groups].map(([group, props]) => (
				<section key={group} className="prop-group">
					<h3>{groupLabels[group] ?? group}</h3>
					{props.map((p) => (
						<FsmPropertyRow key={`${beanKey}|${p.key}`} property={p} editable={editable} />
					))}
				</section>
			))}
			{sheet.isCircuit && (
				<section className="prop-group tips">
					<h3>
						{fsm.states} {fsm.states === 1 ? 'state' : 'states'} · {fsm.transitions}{' '}
						{fsm.transitions === 1 ? 'transition' : 'transitions'}
					</h3>
					<ul>
						<li>Exactly one state must be the initial state, with state number 0.</li>
						<li>Conditions are input values like “1”, “I=1” or “I=0b10”.</li>
						<li>Outputs go on states (Moore) or transitions (Mealy), e.g. “1” or “A=0b10, B=0x2”.</li>
						<li>Drag the middle of a transition to bend it.</li>
					</ul>
				</section>
			)}
		</>
	)
}

function FsmPropertyRow({ property: p, editable }: { property: PropertyDescriptor; editable: boolean }) {
	const readOnly = !editable || p.type === 'readonly'
	const id = `fsm-prop-${p.key}`
	const control = (() => {
		if (p.type === 'choice' && p.choices && !readOnly) {
			return (
				<select
					id={id}
					className="input select"
					value={p.value === null ? '' : String(p.value)}
					onChange={(e) => fsmApi.setProperty(p.key, e.target.value)}
				>
					{p.choices.map((c) => (
						<option key={c.id} value={c.id}>
							{c.label}
						</option>
					))}
				</select>
			)
		}
		if (readOnly && p.type !== 'multiline' && p.type !== 'text') {
			return <div className="prop-static">{String(p.value ?? '—')}</div>
		}
		return <FsmTextField id={id} property={p} readOnly={readOnly} />
	})()

	return (
		<div className={`prop-row${p.type === 'multiline' ? ' stacked' : ''}`}>
			<label htmlFor={id} className="prop-label" title={p.label}>
				{p.label}
			</label>
			<div className="prop-control">{control}</div>
		</div>
	)
}

/** A text or integer field that commits on blur or Enter, and reverts on Escape. */
function FsmTextField({ id, property: p, readOnly }: { id: string; property: PropertyDescriptor; readOnly: boolean }) {
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
			const invalid =
				value.trim() === '' ||
				Number.isNaN(n) ||
				(p.type === 'integer' && !Number.isInteger(n)) ||
				(p.min !== null && n < p.min) ||
				(p.max !== null && n > p.max)
			if (invalid) {
				setValue(external)
				engine.message('warning', p.label, p.min === 0 ? 'Please enter a whole number of at least 0.' : 'Please enter a valid number.')
				return
			}
			fsmApi.setProperty(p.key, n)
		} else {
			fsmApi.setProperty(p.key, value)
		}
	}

	const onKeyDown = (e: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => {
		if (e.key === 'Escape') {
			reverting.current = true
			e.currentTarget.blur()
		}
		if (e.key === 'Enter' && (p.type !== 'multiline' || e.metaKey || e.ctrlKey)) e.currentTarget.blur()
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
		onKeyDown,
	}
	if (p.type === 'multiline') return <textarea {...common} rows={3} onChange={(e) => setValue(e.target.value)} />
	return (
		<input
			{...common}
			type={numeric ? 'number' : 'text'}
			min={p.min ?? undefined}
			step={p.type === 'integer' ? 1 : 'any'}
			spellCheck={false}
			onChange={(e) => setValue(e.target.value)}
		/>
	)
}
