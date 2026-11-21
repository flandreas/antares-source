import { useEffect, useRef, useState, useSyncExternalStore, type ReactNode } from 'react'
import { X } from 'lucide-react'

// A small promise-based dialog system: `await dialogs.prompt(...)` from anywhere.

type Render = (close: (value: unknown) => void) => ReactNode

interface OpenDialog {
	id: number
	render: Render
	resolve: (value: unknown) => void
}

let dialogsOpen: OpenDialog[] = []
const listeners = new Set<() => void>()
let nextId = 0

function emit() {
	listeners.forEach((l) => l())
}

function open<T>(render: Render): Promise<T> {
	return new Promise<T>((resolve) => {
		const dialog: OpenDialog = { id: ++nextId, render, resolve: resolve as (v: unknown) => void }
		dialogsOpen = [...dialogsOpen, dialog]
		emit()
	})
}

function close(id: number, value: unknown) {
	const dialog = dialogsOpen.find((d) => d.id === id)
	dialogsOpen = dialogsOpen.filter((d) => d.id !== id)
	emit()
	dialog?.resolve(value)
}

export const dialogs = {
	/** Asks for a text value; resolves to `null` if cancelled. */
	prompt(options: {
		title: string
		label?: string
		initial?: string
		confirmLabel?: string
		placeholder?: string
		validate?: (value: string) => string | null
	}): Promise<string | null> {
		return open((done) => <PromptDialog {...options} done={done} />)
	},

	/** Asks for confirmation; resolves to `true` if confirmed. */
	confirm(options: {
		title: string
		message: ReactNode
		confirmLabel?: string
		cancelLabel?: string
		danger?: boolean
	}): Promise<boolean> {
		return open((done) => (
			<DialogFrame title={options.title} onCancel={() => done(false)}>
				<div className="dialog-body">{options.message}</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={() => done(false)}>
						{options.cancelLabel ?? 'Cancel'}
					</button>
					<button
						type="button"
						className={`btn ${options.danger ? 'btn-danger' : 'btn-primary'}`}
						autoFocus
						onClick={() => done(true)}
					>
						{options.confirmLabel ?? 'OK'}
					</button>
				</div>
			</DialogFrame>
		))
	},

	/** Shows custom content; the content calls `done(value)` to close. */
	custom<T>(render: (done: (value: T | null) => void) => ReactNode): Promise<T | null> {
		return open<T | null>((done) => render(done as (value: T | null) => void))
	},
}

export function DialogFrame({
	title,
	children,
	onCancel,
	wide,
}: {
	title: string
	children: ReactNode
	onCancel: () => void
	wide?: boolean
}) {
	useEffect(() => {
		const onKey = (e: KeyboardEvent) => {
			if (e.key === 'Escape') {
				e.stopPropagation()
				onCancel()
			}
		}
		window.addEventListener('keydown', onKey, true)
		return () => window.removeEventListener('keydown', onKey, true)
	}, [onCancel])
	return (
		<div className="dialog-backdrop" onPointerDown={(e) => e.target === e.currentTarget && onCancel()}>
			<div className={`dialog${wide ? ' dialog-wide' : ''}`} role="dialog" aria-modal="true" aria-label={title}>
				<div className="dialog-header">
					<h2>{title}</h2>
					<button type="button" className="icon-btn" onClick={onCancel} aria-label="Close">
						<X size={16} />
					</button>
				</div>
				{children}
			</div>
		</div>
	)
}

function PromptDialog({
	title,
	label,
	initial = '',
	confirmLabel = 'OK',
	placeholder,
	validate,
	done,
}: {
	title: string
	label?: string
	initial?: string
	confirmLabel?: string
	placeholder?: string
	validate?: (value: string) => string | null
	done: (value: unknown) => void
}) {
	const [value, setValue] = useState(initial)
	const [touched, setTouched] = useState(false)
	const input = useRef<HTMLInputElement>(null)
	useEffect(() => input.current?.select(), [])
	const error = value.trim() === '' ? 'Please enter a name.' : (validate?.(value.trim()) ?? null)
	const submit = () => {
		setTouched(true)
		if (!error) done(value.trim())
	}
	return (
		<DialogFrame title={title} onCancel={() => done(null)}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					submit()
				}}
			>
				<div className="dialog-body">
					<label className="field">
						{label && <span className="field-label">{label}</span>}
						<input
							ref={input}
							className="input"
							value={value}
							placeholder={placeholder}
							autoFocus
							onChange={(e) => setValue(e.target.value)}
						/>
					</label>
					{touched && error && <div className="field-error">{error}</div>}
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={() => done(null)}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary">
						{confirmLabel}
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}

/** Renders all open dialogs. Mount once near the app root. */
export function DialogHost() {
	const open = useSyncExternalStore(
		(l) => {
			listeners.add(l)
			return () => listeners.delete(l)
		},
		() => dialogsOpen,
	)
	return (
		<>
			{open.map((d) => (
				<div key={d.id}>{d.render((value) => close(d.id, value))}</div>
			))}
		</>
	)
}
