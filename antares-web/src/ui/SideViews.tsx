import { useEffect, useRef } from 'react'
import { X } from 'lucide-react'
import { engine } from '../core/engine'
import type { EditorState, SideView } from '../core/types'

/** Sub-circuits opened in their own views, stacked next to the circuit (like the desktop's side panels). */
export function SideViews({ state }: { state: EditorState }) {
	// The main canvas gets narrower; the Kotlin canvases adapt on window resize events
	const count = state.sideViews.length
	useEffect(() => {
		window.dispatchEvent(new Event('resize'))
	}, [count])

	if (count === 0) return null
	return (
		<div className="side-views">
			{state.sideViews.map((view) => (
				<SideViewPanel key={view.id} view={view} />
			))}
		</div>
	)
}

function SideViewPanel({ view }: { view: SideView }) {
	const canvas = useRef<HTMLCanvasElement>(null)
	useEffect(() => {
		engine.api.bindSideCanvas(view.id, canvas.current!)
	}, [view.id])
	return (
		<section className="side-view" style={view.color ? { borderColor: view.color } : undefined}>
			<header>
				<span className="side-view-dot" style={view.color ? { background: view.color } : undefined} />
				<span className="truncate">{view.title}</span>
				<button type="button" className="icon-btn small" title="Close view" onClick={() => engine.api.closeSideView(view.id)}>
					<X size={14} />
				</button>
			</header>
			<canvas ref={canvas} className="side-view-canvas" />
		</section>
	)
}
