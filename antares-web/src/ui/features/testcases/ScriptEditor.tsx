import { useEffect, useMemo, useRef, useState } from 'react'
import { Maximize2 } from 'lucide-react'
import { DialogFrame, dialogs } from '../../Dialogs'
import type { ScriptError } from './api'

interface ScriptEditorProps {
	/** The stored script */
	value: string
	/** Stores the edited script (called on blur, Ctrl/Cmd+Enter or when the large editor is saved) */
	onCommit: (script: string) => void
	/** Parses the script, returning the first error */
	check?: (script: string) => ScriptError | null
	readOnly?: boolean
	/** Visible lines */
	rows?: number
	placeholder?: string
	/** Title of the large editor dialog */
	title: string
	/** Help shown below the large editor */
	help?: React.ReactNode
	label?: string
}

/** A monospace script editor with line numbers and live error reporting, committing like other property fields. */
export function ScriptEditor(props: ScriptEditorProps) {
	const { value, onCommit, readOnly, title } = props
	const [text, setText] = useState(value)
	const [focused, setFocused] = useState(false)
	const reverted = useRef(false)
	useEffect(() => {
		if (!focused) setText(value)
	}, [value, focused])

	const commit = (script: string) => {
		if (script !== value) onCommit(script)
	}

	const openLarge = async () => {
		const result = await dialogs.custom<string>((done) => <LargeScriptEditor {...props} initial={text} done={done} />)
		if (result !== null) {
			setText(result)
			commit(result)
		}
	}

	return (
		<div className="script-field">
			<div className="script-field-header">
				{props.label && <span className="prop-label">{props.label}</span>}
				<button type="button" className="icon-btn small" title={`Open “${title}” in a larger editor`} onClick={() => void openLarge()}>
					<Maximize2 size={13} />
				</button>
			</div>
			<CodeArea
				text={text}
				onChange={setText}
				check={props.check}
				readOnly={readOnly}
				rows={props.rows ?? 8}
				placeholder={props.placeholder}
				onFocus={() => setFocused(true)}
				onBlur={() => {
					setFocused(false)
					if (reverted.current) reverted.current = false
					else commit(text)
				}}
				onRevert={() => {
					reverted.current = true
					setText(value)
				}}
			/>
		</div>
	)
}

function LargeScriptEditor(props: ScriptEditorProps & { initial: string; done: (value: string | null) => void }) {
	const [text, setText] = useState(props.initial)
	return (
		<DialogFrame title={props.title} onCancel={() => props.done(null)} wide>
			<div className="dialog-body script-dialog-body">
				<CodeArea text={text} onChange={setText} check={props.check} readOnly={props.readOnly} rows={22} placeholder={props.placeholder} autoFocus />
				{props.help && <div className="script-help">{props.help}</div>}
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn" onClick={() => props.done(null)}>
					Cancel
				</button>
				<button type="button" className="btn btn-primary" disabled={props.readOnly} onClick={() => props.done(text)}>
					Apply
				</button>
			</div>
		</DialogFrame>
	)
}

interface CodeAreaProps {
	text: string
	onChange: (text: string) => void
	check?: (script: string) => ScriptError | null
	readOnly?: boolean
	rows: number
	placeholder?: string
	autoFocus?: boolean
	onFocus?: () => void
	onBlur?: () => void
	onRevert?: () => void
}

/** A textarea with a line number gutter; marks the line of the first syntax error. */
function CodeArea({ text, onChange, check, readOnly, rows, placeholder, autoFocus, onFocus, onBlur, onRevert }: CodeAreaProps) {
	const area = useRef<HTMLTextAreaElement>(null)
	const gutter = useRef<HTMLDivElement>(null)
	const reverting = useRef(false)
	const error = useMemo(() => (check && text.trim() ? check(text) : null), [check, text])
	const lines = Math.max(text.split('\n').length, rows)

	const syncScroll = () => {
		if (gutter.current && area.current) gutter.current.scrollTop = area.current.scrollTop
	}

	const goToError = () => {
		const el = area.current
		if (!el || !error || error.row < 1) return
		const offsets = text.split('\n').slice(0, error.row - 1)
		const pos = offsets.reduce((sum, l) => sum + l.length + 1, 0) + Math.max(0, error.column - 1)
		el.focus()
		el.setSelectionRange(pos, pos)
	}

	return (
		<div className={`code-area${error ? ' has-error' : ''}${readOnly ? ' readonly' : ''}`}>
			<div className="code-editor" style={{ height: `calc(${rows} * var(--code-line) + 10px)` }}>
				<div className="code-gutter" ref={gutter} aria-hidden="true">
					{Array.from({ length: lines }, (_, i) => (
						<div key={i} className={error?.row === i + 1 ? 'error-line' : undefined}>
							{i + 1}
						</div>
					))}
				</div>
				<textarea
					ref={area}
					className="code-text"
					value={text}
					readOnly={readOnly}
					spellCheck={false}
					autoCapitalize="off"
					autoComplete="off"
					wrap="off"
					placeholder={placeholder}
					autoFocus={autoFocus}
					onScroll={syncScroll}
					onChange={(e) => onChange(e.target.value)}
					onFocus={onFocus}
					onBlur={() => {
						if (reverting.current) {
							reverting.current = false
							onRevert?.()
						}
						onBlur?.()
					}}
					onKeyDown={(e) => {
						const el = e.currentTarget
						if (e.key === 'Tab' && !e.shiftKey && !readOnly) {
							e.preventDefault()
							const { selectionStart: s, selectionEnd: end } = el
							onChange(text.slice(0, s) + '\t' + text.slice(end))
							requestAnimationFrame(() => el.setSelectionRange(s + 1, s + 1))
						}
						if (e.key === 'Escape' && onRevert) {
							e.stopPropagation()
							reverting.current = true
							el.blur()
						}
						if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) el.blur()
					}}
				/>
			</div>
			{error && (
				<button type="button" className="code-error" onClick={goToError} title="Go to error">
					{error.row > 0 && <span className="code-error-pos">Line {error.row}</span>}
					<span>{error.message}</span>
				</button>
			)}
		</div>
	)
}
