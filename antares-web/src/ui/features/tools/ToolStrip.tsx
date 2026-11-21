// The drawing tools strip (desktop: the drawing toolbar of GraphPanelViewSwing and ContainerToolBarBuilder) with
// the grid and snap toggles of the desktop's settings toolbar, plus the tools' keyboard shortcuts.
import { useEffect, type ReactNode } from 'react'
import { Circle, Grid3x3, Magnet, Microchip, MousePointer2, Square, Type } from 'lucide-react'
import { engine } from '../../../core/engine'
import { matches } from '../../commands'
import { formatShortcut, isTypingTarget } from '../../hooks'
import { toolsApi, type ToolId, type ToolsState } from './api'

/** A line icon in the style of lucide's icons. */
function LineIcon({ d }: { d: string }) {
	return (
		<svg width={16} height={16} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden>
			<path d={d} />
		</svg>
	)
}

interface ToolInfo {
	label: string
	shortcut: string
	icon: ReactNode
}

export const toolInfo: Record<ToolId, ToolInfo> = {
	select: { label: 'Select', shortcut: 'V', icon: <MousePointer2 size={16} /> },
	text: { label: 'Text', shortcut: 'T', icon: <Type size={16} /> },
	label: { label: 'Label', shortcut: 'T', icon: <Type size={16} /> },
	rectangle: { label: 'Rectangle', shortcut: 'B', icon: <Square size={16} /> },
	ellipse: { label: 'Ellipse', shortcut: 'O', icon: <Circle size={16} /> },
	polyline: { label: 'Polyline', shortcut: 'L', icon: <LineIcon d="M3 18 9 7l6 9 6-11" /> },
	quadCurve: { label: 'Quadratic curve', shortcut: 'C', icon: <LineIcon d="M3 19Q12-3 21 19" /> },
	cubicCurve: { label: 'Cubic curve', shortcut: 'Shift+C', icon: <LineIcon d="M3 18C8 0 16 24 21 6" /> },
	dilCase: { label: 'DIL case', shortcut: 'D', icon: <Microchip size={16} /> },
}

/** Hints shown while a tool is active, since the tools are used differently (click, drag, or click points). */
const toolHints: Partial<Record<ToolId, string>> = {
	text: 'Click to place a text, then edit it in the inspector',
	label: 'Click to place a label, then edit it in the inspector',
	rectangle: 'Drag to draw a rectangle',
	ellipse: 'Drag to draw an ellipse',
	polyline: 'Click to add points, double-click to finish',
	quadCurve: 'Drag from start to end, then click to set the bend',
	cubicCurve: 'Drag from start to end, then click twice to set the bends',
	dilCase: 'Click to place a DIL case',
}

/**
 * Handles the tool shortcuts (letters without modifiers, Escape for selection) and select next/previous for the
 * editor the tools apply to. [inDialog] enables them while the sub-circuit symbol dialog is open.
 */
export function useToolShortcuts(enabled: boolean, inDialog = false) {
	useEffect(() => {
		if (!enabled) return
		const onKey = (e: KeyboardEvent) => {
			if (e.defaultPrevented || isTypingTarget(e.target)) return
			if (!inDialog && document.querySelector('.dialog-backdrop')) return
			if (engine.state.mode !== 'edit') return
			const s = toolsApi.state()
			if (!s) return
			if (matches(e, 'Mod+ArrowRight') || matches(e, 'Mod+ArrowLeft')) {
				e.preventDefault()
				toolsApi.perform(e.key === 'ArrowRight' ? 'selectNext' : 'selectPrevious')
				return
			}
			if (!s.toolsEnabled) return
			if (e.key === 'Escape' && s.tool !== 'select') {
				e.preventDefault()
				e.stopImmediatePropagation()
				toolsApi.setTool('select')
				return
			}
			const tool = s.tools.find((t) => matches(e, toolInfo[t].shortcut))
			if (tool) {
				e.preventDefault()
				toolsApi.setTool(tool)
			}
		}
		// Capture phase, so that Escape returns to selection before dialogs close on it
		window.addEventListener('keydown', onKey, true)
		return () => window.removeEventListener('keydown', onKey, true)
	}, [enabled, inDialog])
}

/** The vertical strip of drawing tools and snap toggles for the editor described by [tools]. */
export function ToolStrip({ tools, className = '' }: { tools: ToolsState; className?: string }) {
	const disabled = !tools.toolsEnabled
	const hint = toolHints[tools.tool]
	return (
		<div className={`tool-strip ${className}`} role="toolbar" aria-label="Drawing tools" aria-orientation="vertical">
			{tools.tools.map((id) => {
				const info = toolInfo[id]
				const active = tools.tool === id
				return (
					<button
						key={id}
						type="button"
						className={`icon-btn${active ? ' active' : ''}`}
						aria-pressed={active}
						disabled={disabled}
						title={`${info.label} (${formatShortcut(info.shortcut)})`}
						onClick={() => toolsApi.setTool(id)}
					>
						{info.icon}
					</button>
				)
			})}
			<span className="tool-strip-divider" />
			<button
				type="button"
				className={`icon-btn${tools.gridSnap ? ' active' : ''}`}
				aria-pressed={tools.gridSnap}
				disabled={disabled}
				title="Snap to grid"
				onClick={() => toolsApi.toggleGridSnap()}
			>
				<Grid3x3 size={16} />
			</button>
			<button
				type="button"
				className={`icon-btn${tools.componentSnap ? ' active' : ''}`}
				aria-pressed={tools.componentSnap}
				disabled={disabled}
				title="Snap to components (align)"
				onClick={() => toolsApi.toggleComponentSnap()}
			>
				<Magnet size={16} />
			</button>
			{hint && !disabled && <div className="tool-strip-hint">{hint}</div>}
		</div>
	)
}
