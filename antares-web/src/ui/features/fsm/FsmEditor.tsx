import { useEffect, useRef, type ReactNode } from 'react'
import { ArrowUpRight, Circle, Lock, Maximize, Minus, MousePointer2, Plus, Table2 } from 'lucide-react'
import { engine } from '../../../core/engine'
import { isTypingTarget, useEngineVersion, useThemeName } from '../../hooks'
import type { DocumentEditorProps } from '../registry'
import { fsmApi, type FsmTool } from './api'
import { createTruthTable } from './actions'

const tools: Array<{ id: FsmTool; label: string; key: string; icon: ReactNode }> = [
	{ id: 'select', label: 'Select and move', key: 'V', icon: <MousePointer2 size={15} /> },
	{ id: 'state', label: 'Add state: click on the canvas', key: 'S', icon: <Circle size={15} /> },
	{ id: 'transition', label: 'Add transition: drag from one state to another', key: 'T', icon: <ArrowUpRight size={15} /> },
]

/** Edits a finite state machine on its own canvas with the desktop's shared FSM editor. */
export function FsmEditor({ state }: DocumentEditorProps) {
	useEngineVersion()
	const host = useRef<HTMLDivElement>(null)
	const uuid = state.document?.uuid
	// Rebinding applies a new theme to the canvas background
	const theme = useThemeName()

	// Each binding gets a fresh canvas: the Kotlin canvas can't be detached, and a released editor must not
	// keep painting into the visible canvas (React runs effects twice in development)
	useEffect(() => {
		const canvas = document.createElement('canvas')
		canvas.className = 'editor-canvas'
		canvas.setAttribute('aria-label', 'State machine editor')
		host.current!.prepend(canvas)
		fsmApi.kotlin.bindCanvas(canvas)
		return () => {
			fsmApi.kotlin.unbind()
			canvas.remove()
		}
	}, [uuid, theme])

	const fsm = fsmApi.state()
	const editable = !!fsm?.editable && !!state.document?.editable

	// Tool shortcuts, unless the user is typing or a dialog is open
	useEffect(() => {
		if (!editable) return
		const onKey = (e: KeyboardEvent) => {
			if (e.metaKey || e.ctrlKey || e.altKey || isTypingTarget(e.target) || document.querySelector('.dialog-backdrop')) return
			const tool = e.key === 'Escape' ? 'select' : tools.find((t) => t.key.toLowerCase() === e.key.toLowerCase())?.id
			if (tool) {
				e.preventDefault()
				fsmApi.setTool(tool)
			}
		}
		window.addEventListener('keydown', onKey)
		return () => window.removeEventListener('keydown', onKey)
	}, [editable])

	return (
		<div className="fsm-editor" ref={host}>

			{fsm && editable && (
				<div className="canvas-overlay top-left">
					<div className="fsm-toolbar" role="toolbar" aria-label="State machine tools">
						{tools.map((t) => (
							<button
								key={t.id}
								type="button"
								className={`icon-btn${fsm.tool === t.id ? ' pressed' : ''}`}
								aria-pressed={fsm.tool === t.id}
								title={`${t.label} (${t.key})`}
								onClick={() => fsmApi.setTool(t.id)}
							>
								{t.icon}
							</button>
						))}
						<span className="zoom-divider" />
						<button
							type="button"
							className={`icon-btn${fsm.toolLock ? ' pressed' : ''}`}
							aria-pressed={fsm.toolLock}
							title="Keep the tool after use, to add several states or transitions in a row"
							onClick={() => fsmApi.setToolLock(!fsm.toolLock)}
						>
							<Lock size={14} />
						</button>
					</div>
					<button
						type="button"
						className="btn btn-sm fsm-action"
						onClick={() => void createTruthTable()}
						title="Create a truth table implementing this state machine"
					>
						<Table2 size={14} /> Create truth table
					</button>
				</div>
			)}

			{fsm?.status && (
				<div className="canvas-overlay bottom-left">
					<div className="fsm-status">{fsm.status}</div>
				</div>
			)}

			{fsm && fsm.states === 0 && (
				<div className="canvas-empty">
					<div className="canvas-empty-card">
						<strong>Empty state machine</strong>
						<span>
							{editable
								? 'Pick the state tool (S) and click the canvas to add states. Then drag from state to state with the transition tool (T).'
								: 'This state machine has no states.'}
						</span>
						{editable && (
							<span className="subtle">
								Select a state or transition to edit its name, conditions and outputs in the inspector.
							</span>
						)}
					</div>
				</div>
			)}

			{fsm && (
				<div className="canvas-overlay bottom-right zoom-controls">
					<button type="button" className="icon-btn" onClick={() => zoom(0.8)} title="Zoom out">
						<Minus size={15} />
					</button>
					<button
						type="button"
						className="zoom-value"
						onClick={() => {
							fsmApi.kotlin.zoomToNormal()
							engine.refresh()
						}}
						title="Reset to 100%"
					>
						{Math.round((fsm.zoom * 100) / (window.devicePixelRatio || 1))}%
					</button>
					<button type="button" className="icon-btn" onClick={() => zoom(1.25)} title="Zoom in">
						<Plus size={15} />
					</button>
					<span className="zoom-divider" />
					<button
						type="button"
						className="icon-btn"
						onClick={() => {
							fsmApi.kotlin.zoomToFit()
							engine.refresh()
						}}
						title="Zoom to fit"
					>
						<Maximize size={14} />
					</button>
				</div>
			)}
		</div>
	)
}

function zoom(factor: number) {
	fsmApi.kotlin.zoomBy(factor)
	engine.refresh()
}
