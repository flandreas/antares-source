// Dialogs of the editing tools: a sub-circuit's symbol (desktop: EditSubGraphVerticeViewPanel), port renaming
// (GraphPortRenamingPanelSwing) and embedding a circuit in a web page (MetaGraphEmbedPanel).
import { useEffect, useMemo, useRef, useState } from 'react'
import { Copy, Redo2, Undo2 } from 'lucide-react'
import { engine } from '../../../core/engine'
import { matches } from '../../commands'
import { DialogFrame, dialogs } from '../../Dialogs'
import { formatShortcut, isTypingTarget, useEngineVersion, useStoredState } from '../../hooks'
import { toolsApi, type CircuitPort } from './api'
import { ToolStrip, useToolShortcuts } from './ToolStrip'

/** ---- Symbol of a single sub-circuit */

/**
 * Edits how the selected sub-circuit looks in this circuit only. Changes inside the dialog can be undone there;
 * applying them is a single undoable change of the circuit.
 */
export async function editSubCircuitSymbol() {
	const name = toolsApi.beginEditSymbol()
	if (!name) return
	await dialogs.custom<boolean>((done) => <SymbolDialog name={name} done={done} />)
}

function SymbolDialog({ name, done }: { name: string; done: (apply: boolean | null) => void }) {
	useEngineVersion()
	const canvas = useRef<HTMLCanvasElement>(null)
	const tools = toolsApi.state()
	const history = toolsApi.symbolHistory()

	useEffect(() => {
		toolsApi.bindSymbolCanvas(canvas.current!)
		// The canvas adapts its resolution on window resize; it has its final size once the dialog is laid out
		const frame = requestAnimationFrame(() => window.dispatchEvent(new Event('resize')))
		return () => cancelAnimationFrame(frame)
	}, [])

	const close = (apply: boolean) => {
		toolsApi.endEditSymbol(apply)
		done(apply)
	}

	useToolShortcuts(true, true)

	// The main window's shortcuts are inactive while a dialog is open; handle the ones that make sense here
	useEffect(() => {
		const onKey = (e: KeyboardEvent) => {
			if (isTypingTarget(e.target)) return
			if (matches(e, 'Mod+Z') || matches(e, 'Mod+Shift+Z') || matches(e, 'Mod+Y')) {
				e.preventDefault()
				toolsApi.undoSymbol(!matches(e, 'Mod+Z'))
			} else if (e.key === 'Delete' || e.key === 'Backspace') {
				e.preventDefault()
				engine.perform('delete')
			} else if (matches(e, 'Mod+A')) {
				e.preventDefault()
				engine.perform('selectAll')
			} else if (matches(e, 'R') || matches(e, 'Shift+R')) {
				e.preventDefault()
				engine.perform(e.shiftKey ? 'rotateLeft' : 'rotateRight')
			}
		}
		window.addEventListener('keydown', onKey)
		return () => window.removeEventListener('keydown', onKey)
	}, [])

	return (
		<DialogFrame title={`Symbol of “${name}”`} onCancel={() => close(false)} wide>
			<div className="symbol-dialog-body">
				{tools && <ToolStrip tools={tools} className="in-dialog" />}
				<div className="symbol-dialog-canvas">
					<canvas ref={canvas} aria-label="Symbol editor" />
				</div>
			</div>
			<div className="dialog-actions symbol-dialog-actions">
				<button
					type="button"
					className="icon-btn"
					disabled={!history?.canUndo}
					title={`Undo (${formatShortcut('Mod+Z')})`}
					onClick={() => toolsApi.undoSymbol(false)}
				>
					<Undo2 size={15} />
				</button>
				<button
					type="button"
					className="icon-btn"
					disabled={!history?.canRedo}
					title={`Redo (${formatShortcut('Mod+Shift+Z')})`}
					onClick={() => toolsApi.undoSymbol(true)}
				>
					<Redo2 size={15} />
				</button>
				<span className="tools-note symbol-dialog-hint">Changes apply only to this use of the sub-circuit.</span>
				<button type="button" className="btn" onClick={() => close(false)}>
					Cancel
				</button>
				<button type="button" className="btn btn-primary" onClick={() => close(true)}>
					Apply
				</button>
			</div>
		</DialogFrame>
	)
}

/** ---- Port renaming */

export async function renamePorts() {
	await dialogs.custom<void>((done) => <PortRenamingDialog done={() => done(null)} />)
}

function PortRenamingDialog({ done }: { done: () => void }) {
	const version = useEngineVersion()
	const ports = useMemo(() => toolsApi.ports(), [version])
	const editable = toolsApi.canEditPorts()
	const [zoom, setZoom] = useStoredState('antares.portRenaming.zoomToPort', true)
	const [error, setError] = useState('')
	const [selected, setSelected] = useState<number | null>(null)

	const select = (id: number) => {
		if (selected === id) return
		setSelected(id)
		toolsApi.focusPort(id, zoom)
	}

	return (
		<DialogFrame title="Rename inputs and outputs" onCancel={done}>
			<div className="dialog-body port-renaming">
				<label className="port-renaming-zoom">
					<input type="checkbox" checked={zoom} onChange={(e) => setZoom(e.target.checked)} />
					Zoom to the selected port
				</label>
				{ports.length === 0 ? (
					<div className="empty-small">This circuit has no inputs or outputs.</div>
				) : (
					<div className="port-renaming-table" role="table">
						<div className="port-renaming-row header" role="row">
							<span role="columnheader">Name</span>
							<span role="columnheader">Type</span>
						</div>
						{ports.map((port) => (
							<PortRow
								key={port.id}
								port={port}
								editable={editable}
								selected={selected === port.id}
								onFocus={() => select(port.id)}
								onError={setError}
							/>
						))}
					</div>
				)}
				<div className={`port-renaming-error${error ? '' : ' empty'}`} role="alert">
					{error}
				</div>
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn btn-primary" onClick={done}>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}

function PortRow({
	port,
	editable,
	selected,
	onFocus,
	onError,
}: {
	port: CircuitPort
	editable: boolean
	selected: boolean
	onFocus: () => void
	onError: (error: string) => void
}) {
	const [draft, setDraft] = useState(port.name)
	const editing = useRef(false)
	useEffect(() => {
		if (!editing.current) setDraft(port.name)
	}, [port.name])
	const commit = () => {
		editing.current = false
		if (draft === port.name) return
		const error = toolsApi.renamePort(port.id, draft)
		onError(error)
		if (error) setDraft(port.name)
	}
	return (
		<div className={`port-renaming-row${selected ? ' selected' : ''}`} role="row">
			<input
				className="input"
				value={draft}
				readOnly={!editable}
				aria-label={`Name of ${port.name}`}
				onFocus={(e) => {
					onFocus()
					e.target.select()
				}}
				onChange={(e) => {
					editing.current = true
					setDraft(e.target.value)
				}}
				onBlur={commit}
				onKeyDown={(e) => {
					if (e.key === 'Enter') {
						e.preventDefault()
						commit()
						const inputs = [...document.querySelectorAll<HTMLInputElement>('.port-renaming-row input')]
						inputs[inputs.indexOf(e.currentTarget) + 1]?.focus()
					} else if (e.key === 'Escape' && draft !== port.name) {
						// Revert the field instead of closing the dialog
						e.preventDefault()
						e.nativeEvent.stopImmediatePropagation()
						editing.current = false
						setDraft(port.name)
					}
				}}
			/>
			<span className={`port-type ${port.input ? 'in' : 'out'}`}>{port.type}</span>
		</div>
	)
}

/** ---- Embedding */

export async function showEmbedCode() {
	await dialogs.custom<void>((done) => <EmbedDialog done={() => done(null)} />)
}

function EmbedDialog({ done }: { done: () => void }) {
	const themes = useMemo(() => toolsApi.embedThemes(), [])
	const [theme, setTheme] = useStoredState('antares.embed.theme', themes[0] ?? '')
	const code = toolsApi.embedCode(themes.includes(theme) ? theme : (themes[0] ?? ''))
	const copy = async () => {
		await navigator.clipboard.writeText(code)
		engine.message('success', '', 'Embed code copied')
	}
	return (
		<DialogFrame title="Embed in a web page" onCancel={done} wide>
			<div className="dialog-body embed-dialog">
				<label className="embed-theme">
					<span>Theme</span>
					<select className="input select" value={theme} onChange={(e) => setTheme(e.target.value)}>
						{themes.map((t) => (
							<option key={t} value={t}>
								{t}
							</option>
						))}
					</select>
				</label>
				<textarea className="input embed-code" readOnly value={code} rows={8} spellCheck={false} onFocus={(e) => e.target.select()} />
				<p className="tools-note">
					The HTML shows this circuit in the online viewer. It only works for projects that are published online,
					not for projects stored only in this browser.
				</p>
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn" onClick={copy}>
					<Copy size={14} /> Copy
				</button>
				<button type="button" className="btn btn-primary" onClick={done}>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}
