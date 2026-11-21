// The find bar over the circuit canvas (desktop: `SearchBarSwing`). Typing selects all matches; Enter and
// Shift+Enter (or the arrows) step through them one at a time and bring each into view.
import { useEffect, useRef, useState } from 'react'
import { ChevronDown, ChevronUp, Search, X } from 'lucide-react'
import type { DocumentEditorProps } from '../registry'
import { formatShortcut } from '../../hooks'
import { findApi, type FindMatch, type FindResult } from './api'
import { findBar, useFindBar } from './store'

const MATCH_LABELS: Record<FindMatch, string> = {
	contains: 'Contains',
	startsWith: 'Starts with',
	entireWord: 'Entire word',
}

const EMPTY: FindResult = { count: 0, current: 0 }

/** Moves keyboard focus back to the visible canvas. */
function focusCanvas() {
	document.querySelector<HTMLCanvasElement>('.editor-canvas:not(.hidden)')?.focus()
}

export function FindBar({ state }: DocumentEditorProps) {
	const focusRequest = useFindBar()
	if (!focusRequest) return null
	return <FindBarContent state={state} focusRequest={focusRequest} />
}

function FindBarContent({ state, focusRequest }: DocumentEditorProps & { focusRequest: number }) {
	const [text, setText] = useState('')
	const [match, setMatch] = useState<FindMatch>('contains')
	const [ignoreCase, setIgnoreCase] = useState(true)
	const [result, setResult] = useState<FindResult>(EMPTY)
	const input = useRef<HTMLInputElement>(null)
	/** Set by a preset query, whose first match is brought into view once found. */
	const revealFirst = useRef(false)

	useEffect(() => {
		const preset = findBar.takePreset()
		if (preset) {
			setText(preset.text)
			setMatch(preset.match)
			revealFirst.current = true
		}
		input.current?.focus()
		requestAnimationFrame(() => input.current?.select())
	}, [focusRequest])

	// Search again when the query changes or another drawing is shown (sub-circuit, symbol, other circuit)
	const drawing = `${state.circuit?.uuid}|${state.view}|${state.breadcrumbs.join('/')}`
	useEffect(() => {
		if (text.trim()) {
			const found = findApi.search(text, match, ignoreCase)
			if (revealFirst.current && found.count > 0) {
				revealFirst.current = false
				setResult(findApi.navigate(true))
			} else setResult(found)
		} else {
			findApi.reset()
			setResult(EMPTY)
		}
	}, [text, match, ignoreCase, drawing])

	useEffect(() => () => findApi.reset(), [])

	const navigate = (forward: boolean) => {
		if (result.count > 0) setResult(findApi.navigate(forward))
	}

	const close = () => {
		findBar.hide()
		focusCanvas()
	}

	const status = !text.trim()
		? ''
		: result.count === 0
			? 'No matches'
			: result.current > 0
				? `${result.current} of ${result.count}`
				: `${result.count} ${result.count === 1 ? 'match' : 'matches'}`

	return (
		<div className="canvas-overlay top-right find-overlay">
			<div
				className="find-bar"
				role="search"
				onKeyDown={(e) => {
					e.stopPropagation()
					if (e.key === 'Escape') {
						e.preventDefault()
						close()
					}
				}}
			>
				<Search size={14} className="find-icon" />
				<input
					ref={input}
					className="find-input"
					value={text}
					placeholder="Find components, labels, types…"
					aria-label="Find in circuit"
					spellCheck={false}
					onChange={(e) => setText(e.target.value)}
					onKeyDown={(e) => {
						if (e.key === 'Enter') {
							e.preventDefault()
							navigate(!e.shiftKey)
						} else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
							e.preventDefault()
							navigate(e.key === 'ArrowDown')
						}
					}}
				/>
				<span className={`find-status${text.trim() && result.count === 0 ? ' none' : ''}`} aria-live="polite">
					{status}
				</span>
				<div className="find-divider" />
				<button
					type="button"
					className={`find-toggle${ignoreCase ? '' : ' on'}`}
					aria-pressed={!ignoreCase}
					onClick={() => setIgnoreCase(!ignoreCase)}
					title="Match case"
				>
					Aa
				</button>
				<select
					className="find-match"
					value={match}
					onChange={(e) => setMatch(e.target.value as FindMatch)}
					aria-label="Match"
					title="How the text must match"
				>
					{(Object.keys(MATCH_LABELS) as FindMatch[]).map((m) => (
						<option key={m} value={m}>
							{MATCH_LABELS[m]}
						</option>
					))}
				</select>
				<div className="find-divider" />
				<button
					type="button"
					className="icon-btn small"
					disabled={result.count === 0}
					onClick={() => navigate(false)}
					title="Previous match (Shift+Enter)"
				>
					<ChevronUp size={15} />
				</button>
				<button
					type="button"
					className="icon-btn small"
					disabled={result.count === 0}
					onClick={() => navigate(true)}
					title="Next match (Enter)"
				>
					<ChevronDown size={15} />
				</button>
				<button type="button" className="icon-btn small" onClick={close} title={`Close (Esc) · reopen with ${formatShortcut('Mod+F')}`}>
					<X size={15} />
				</button>
			</div>
		</div>
	)
}
