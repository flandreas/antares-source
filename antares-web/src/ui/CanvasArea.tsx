import { useEffect, useRef, useState } from 'react'
import { ChevronLeft, ChevronRight, Maximize, Minus, Pause, Play, Plus, SkipForward, Square } from 'lucide-react'
import { DRAG_TYPE_PREFIX, engine } from '../core/engine'
import type { EditorState } from '../core/types'
import { command } from './commands'
import { formatShortcut } from './hooks'
import { Menu, type MenuEntry } from './Menu'
import { SideViews } from './SideViews'
import { canvasContextActions, canvasOverlays, documentEditors } from './features/registry'
import { documentationMenuEntries } from './features/docs'

export function CanvasArea({ state }: { state: EditorState }) {
	const host = useRef<HTMLDivElement>(null)
	const circuitCanvas = useRef<HTMLCanvasElement>(null)
	const symbolCanvas = useRef<HTMLCanvasElement>(null)
	const [menu, setMenu] = useState<{ x: number; y: number } | null>(null)
	const [dragOver, setDragOver] = useState(false)

	useEffect(() => {
		const c = circuitCanvas.current!
		const s = symbolCanvas.current!
		if (!c.dataset.bound) {
			c.dataset.bound = '1'
			engine.bindCanvas(c)
			engine.bindSymbolCanvas(s)
		}
		// The Kotlin canvases adapt their resolution on window resize; forward layout changes of the host
		let frame = 0
		const observer = new ResizeObserver(() => {
			cancelAnimationFrame(frame)
			frame = requestAnimationFrame(() => window.dispatchEvent(new Event('resize')))
		})
		observer.observe(host.current!)

		// Browser conventions for the wheel: scroll pans, pinch (ctrlKey) or ⌘/Ctrl + scroll zooms at the pointer.
		// Handled here (capture phase) instead of in the shared Kotlin code, which zooms on every wheel event.
		const onWheel = (e: WheelEvent) => {
			if (!(e.target instanceof HTMLCanvasElement)) return
			e.preventDefault()
			e.stopPropagation()
			const scale = e.deltaMode === 1 ? 16 : e.deltaMode === 2 ? 400 : 1
			const dx = e.deltaX * scale
			const dy = e.deltaY * scale
			if (e.ctrlKey || e.metaKey) {
				const rect = e.target.getBoundingClientRect()
				engine.zoomAt(Math.exp(-dy * 0.01), e.clientX - rect.left, e.clientY - rect.top)
			} else if (e.shiftKey && dx === 0) {
				engine.panBy(-dy, 0)
			} else {
				engine.panBy(-dx, -dy)
			}
		}
		const el = host.current!
		el.addEventListener('wheel', onWheel, { passive: false, capture: true })
		return () => {
			observer.disconnect()
			el.removeEventListener('wheel', onWheel, { capture: true })
		}
	}, [])

	const DocumentEditor = state.document ? documentEditors[state.document.kind] : undefined
	const documentShown = state.document !== null
	const symbol = state.view === 'symbol'
	const simulating = state.mode === 'simulate'
	const inSubCircuit = state.breadcrumbs.length > 1

	const contextMenu = (): MenuEntry[] => {
		const entry = (id: string): MenuEntry => {
			const c = command(id)
			return { label: c.label, shortcut: c.shortcut, disabled: !c.enabled(state), onSelect: c.run }
		}
		if (simulating) return [entry('simulate'), entry('pause'), entry('step'), 'separator', entry('zoomFit')]
		return [
			entry('cut'),
			entry('copy'),
			entry('paste'),
			entry('duplicate'),
			entry('delete'),
			'separator',
			entry('rotateRight'),
			entry('rotateLeft'),
			entry('toFront'),
			entry('toBack'),
			'separator',
			entry('selectAll'),
			...documentationMenuEntries(),
			...(state.canOpenInSideView
				? ([{ label: 'Open in separate view', onSelect: () => engine.api.openInSideView() }] as MenuEntry[])
				: []),
			...canvasContextActions.flatMap((a): MenuEntry[] => {
				const entries = a.entries(state)
				return entries.length > 0 ? ['separator', ...entries] : []
			}),
			'separator',
			entry('zoomFit'),
		]
	}

	return (
		<main
			ref={host}
			className={`canvas-area${simulating ? ' simulating' : ''}${dragOver ? ' drag-over' : ''}${state.sideViews.length > 0 && !documentShown ? ' has-side' : ''}`}
			onContextMenu={(e) => {
				if (!state.circuit) return
				e.preventDefault()
				setMenu({ x: e.clientX, y: e.clientY })
			}}
			onDragEnter={(e) => {
				if ([...e.dataTransfer.types].some((t) => t.startsWith(DRAG_TYPE_PREFIX))) setDragOver(true)
			}}
			onDragLeave={(e) => {
				if (!e.currentTarget.contains(e.relatedTarget as Node)) setDragOver(false)
			}}
			onDrop={() => setDragOver(false)}
		>
			<canvas
				ref={circuitCanvas}
				className={`editor-canvas${symbol || documentShown ? ' hidden' : ''}`}
				aria-label="Circuit editor"
			/>
			<canvas
				ref={symbolCanvas}
				className={`editor-canvas${symbol && !documentShown ? '' : ' hidden'}`}
				aria-label="Symbol editor"
			/>
			{state.document && (
				<div className="document-host">
					{DocumentEditor ? (
						<DocumentEditor state={state} />
					) : (
						<div className="canvas-empty">
							<div className="canvas-empty-card">
								<strong>{state.document.name}</strong>
								<span>This kind of item can’t be edited in the browser yet.</span>
							</div>
						</div>
					)}
				</div>
			)}

			{state.circuit && (
				<div className="canvas-overlay top-left">
					{inSubCircuit ? (
						<nav className="breadcrumbs" aria-label="Sub-circuit navigation">
							<button
								type="button"
								className="icon-btn small"
								onClick={() => engine.navigateTo(state.breadcrumbs.length - 2)}
								title="Back to the enclosing circuit (Esc)"
							>
								<ChevronLeft size={15} />
							</button>
							{state.breadcrumbs.map((name, i) => (
								<span key={i} className="crumb">
									{i > 0 && <ChevronRight size={12} className="crumb-chevron" />}
									{i < state.breadcrumbs.length - 1 ? (
										<button type="button" onClick={() => engine.navigateTo(i)}>
											{name}
										</button>
									) : (
										<span className="current">{name}</span>
									)}
								</span>
							))}
							{!simulating && <span className="pill">Read-only</span>}
						</nav>
					) : (
						!simulating && (
							<div className="segmented small" role="tablist" aria-label="View">
								<button
									type="button"
									role="tab"
									aria-selected={!symbol}
									className={!symbol ? 'selected' : ''}
									onClick={() => engine.showSymbol(false)}
									title="Edit the circuit"
								>
									Circuit
								</button>
								<button
									type="button"
									role="tab"
									aria-selected={symbol}
									className={symbol ? 'selected' : ''}
									onClick={() => engine.showSymbol(true)}
									title={`Edit the symbol shown when this circuit is used inside other circuits (${formatShortcut('Mod+Alt+S')})`}
								>
									Symbol
								</button>
							</div>
						)
					)}
				</div>
			)}

			{simulating && !documentShown && (
				<div className="canvas-overlay top-center">
					<div className="sim-bar" role="toolbar" aria-label="Simulation">
						<span className={`sim-run-state${state.paused ? ' paused' : ''}`}>
							<span className="pulse" />
							{state.paused ? 'Paused' : 'Running'}
						</span>
						<span className="sim-bar-divider" />
						<button
							type="button"
							className="icon-btn"
							onClick={() => engine.togglePause()}
							title={`${state.paused ? 'Resume' : 'Pause'} (${formatShortcut('Mod+P')})`}
						>
							{state.paused ? <Play size={16} /> : <Pause size={16} />}
						</button>
						<button
							type="button"
							className="icon-btn"
							onClick={() => engine.step()}
							title={`Single step (${formatShortcut('Mod+.')})`}
						>
							<SkipForward size={16} />
						</button>
						<button
							type="button"
							className="icon-btn"
							onClick={() => engine.setSimulating(false)}
							title={`Stop simulation (${formatShortcut('Mod+Enter')})`}
						>
							<Square size={14} />
						</button>
						<span className="sim-bar-divider" />
						<label className="speed" title="Simulation speed">
							<input
								type="range"
								min={0}
								max={100}
								value={state.speed}
								onChange={(e) => engine.setSpeed(Number(e.target.value))}
								aria-label="Simulation speed"
							/>
							<span>{state.speed <= 33 ? 'Explore' : state.speed <= 66 ? 'Observe' : 'Use'}</span>
						</label>
					</div>
				</div>
			)}

			{state.circuit && state.componentCount === 0 && !simulating && !symbol && (
				<div className="canvas-empty">
					<div className="canvas-empty-card">
						<strong>Start building</strong>
						<span>Drag components from the palette onto the canvas, or click one to add it here.</span>
						<span className="subtle">
							Connect parts by dragging from a pin to another pin. Press {formatShortcut('Mod+Enter')} to simulate.
						</span>
					</div>
				</div>
			)}

			{state.circuit && (
				<div className="canvas-overlay bottom-right zoom-controls">
					<button type="button" className="icon-btn" onClick={() => engine.zoomOut()} title={`Zoom out (${formatShortcut('Mod+-')})`}>
						<Minus size={15} />
					</button>
					<button
						type="button"
						className="zoom-value"
						onClick={() => engine.zoomToNormal()}
						title={`Reset to 100% (${formatShortcut('Mod+1')})`}
					>
						{Math.round((state.zoom * 100) / (window.devicePixelRatio || 1))}%
					</button>
					<button type="button" className="icon-btn" onClick={() => engine.zoomIn()} title={`Zoom in (${formatShortcut('Mod+=')})`}>
						<Plus size={15} />
					</button>
					<span className="zoom-divider" />
					<button type="button" className="icon-btn" onClick={() => engine.zoomToFit()} title={`Zoom to fit (${formatShortcut('Mod+0')})`}>
						<Maximize size={14} />
					</button>
				</div>
			)}

			{state.circuit &&
				!documentShown &&
				canvasOverlays.map(({ id, component: Overlay }) => <Overlay key={id} state={state} />)}
			{!documentShown && <SideViews state={state} />}
			{menu && <Menu entries={contextMenu()} x={menu.x} y={menu.y} onClose={() => setMenu(null)} />}
		</main>
	)
}
