// Small form controls committing on blur/Enter, styled like the inspector's property rows.
import { useEffect, useRef, useState, type ReactNode } from 'react'

export function FieldRow({
	label,
	children,
	inline,
	stacked,
	title,
}: {
	label: string
	children: ReactNode
	inline?: boolean
	stacked?: boolean
	title?: string
}) {
	return (
		<div className={`prop-row${inline ? ' inline' : ''}${stacked ? ' stacked' : ''}`} title={title}>
			<span className="prop-label">{label}</span>
			<div className="prop-control">{children}</div>
		</div>
	)
}

export function Switch({ checked, disabled, onChange }: { checked: boolean; disabled?: boolean; onChange: (value: boolean) => void }) {
	return (
		<label className="switch">
			<input type="checkbox" checked={checked} disabled={disabled} onChange={(e) => onChange(e.target.checked)} />
			<span className="switch-track" />
		</label>
	)
}

/** Keeps a local draft while focused; commits on blur, reverts on Escape. */
function useDraft(value: string, onCommit: (value: string) => void) {
	const [draft, setDraft] = useState(value)
	const [focused, setFocused] = useState(false)
	const reverting = useRef(false)
	useEffect(() => {
		if (!focused) setDraft(value)
	}, [value, focused])
	return {
		value: draft,
		onChange: (e: { target: { value: string } }) => setDraft(e.target.value),
		onFocus: () => setFocused(true),
		onBlur: () => {
			setFocused(false)
			if (reverting.current) {
				reverting.current = false
				setDraft(value)
			} else if (draft !== value) {
				onCommit(draft)
			}
		},
		revert: (el: HTMLElement) => {
			reverting.current = true
			el.blur()
		},
	}
}

export function TextField({ value, disabled, onCommit }: { value: string; disabled?: boolean; onCommit: (value: string) => void }) {
	const { revert, ...draft } = useDraft(value, onCommit)
	return (
		<input
			className="input"
			{...draft}
			readOnly={disabled}
			onKeyDown={(e) => {
				if (e.key === 'Enter') e.currentTarget.blur()
				if (e.key === 'Escape') revert(e.currentTarget)
			}}
		/>
	)
}

export function TextArea({
	value,
	disabled,
	onCommit,
	rows = 3,
	mono,
	placeholder,
}: {
	value: string
	disabled?: boolean
	onCommit: (value: string) => void
	rows?: number
	mono?: boolean
	placeholder?: string
}) {
	const { revert, ...draft } = useDraft(value, onCommit)
	return (
		<textarea
			className={`input${mono ? ' mono' : ''}`}
			{...draft}
			rows={rows}
			placeholder={placeholder}
			readOnly={disabled}
			spellCheck={!mono}
			onKeyDown={(e) => {
				if (e.key === 'Escape') revert(e.currentTarget)
				if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) e.currentTarget.blur()
			}}
		/>
	)
}

export function NumberField({
	value,
	min,
	disabled,
	onCommit,
}: {
	value: number
	min?: number
	disabled?: boolean
	onCommit: (value: number) => void
}) {
	const { revert, ...draft } = useDraft(String(value), (v) => {
		const n = Number(v)
		if (Number.isInteger(n) && (min === undefined || n >= min)) onCommit(n)
	})
	return (
		<input
			className="input"
			type="number"
			step={1}
			min={min}
			{...draft}
			readOnly={disabled}
			onKeyDown={(e) => {
				if (e.key === 'Enter') e.currentTarget.blur()
				if (e.key === 'Escape') revert(e.currentTarget)
			}}
		/>
	)
}
