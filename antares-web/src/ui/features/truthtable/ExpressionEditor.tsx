import { useEffect, useMemo, useRef, useState } from 'react'
import { CircleAlert, CircleCheck, Cpu, Table2 } from 'lucide-react'
import { useEngineVersion } from '../../hooks'
import type { DocumentEditorProps } from '../registry'
import { truthTableApi } from './api'
import { NotationMenu, UndoButtons, useDocumentUndo } from './common'
import { createCircuit, ExpressionList, saveAs } from './dialogs'
import { TruthTableGrid } from './TruthTableGrid'

/** Typing is stored as one undoable edit after this pause. */
const COMMIT_DELAY = 800

const EXAMPLES = [
	['Arithmetic', "Y = A * B' + A' * B"],
	['Logic', 'Y = (A ∧ ¬B) ∨ (¬A ∧ B)'],
	['Programming', 'Y = (A && !B) || (!A && B)'],
	['Verbose', 'Y = (A AND NOT B) OR (NOT A AND B)'],
]

/**
 * The boolean expressions document: expressions are checked while typing, and the resulting truth table and
 * minimized expressions are shown live, like the desktop's BooleanExpressionDesktopItemSwing.
 * Typing is committed on blur (e.g. before another item is opened) and after a pause.
 */
export function ExpressionEditor({ state }: DocumentEditorProps) {
	// A fresh editor per document, so pending typing never goes to another document
	return <ExpressionDocument key={state.document?.uuid} state={state} />
}

function ExpressionDocument({ state }: DocumentEditorProps) {
	useEngineVersion()
	useDocumentUndo()
	const stored = truthTableApi.expression()
	const editable = !!state.document?.editable
	const [draft, setDraft] = useState(stored?.text ?? '')
	const committed = useRef(stored?.text ?? '')
	const timer = useRef<number>()
	const gutter = useRef<HTMLDivElement>(null)

	// Adopt changes from undo/redo, unless they are the text just committed from here
	useEffect(() => {
		if (stored && stored.text !== committed.current) {
			committed.current = stored.text
			setDraft(stored.text)
		}
	}, [stored?.text])

	// A pending commit must not reach the next document
	useEffect(() => () => window.clearTimeout(timer.current), [])

	const commit = (text: string) => {
		window.clearTimeout(timer.current)
		if (text === committed.current) return
		committed.current = text
		truthTableApi.setExpressionText(text)
	}

	const singleChar = stored?.singleChar ?? false
	const check = useMemo(() => truthTableApi.checkExpression(draft, singleChar), [draft, singleChar])
	if (!stored) return null

	const lines = draft.split('\n').length
	const error = check?.error ?? null
	const table = check?.table ?? null
	const valid = !error && !!table
	const status = error
		? `Line ${error.row}, column ${error.column}: ${error.message}`
		: table
			? `${table.inputs.length} ${table.inputs.length === 1 ? 'input' : 'inputs'}, ${table.outputs.length} ${table.outputs.length === 1 ? 'output' : 'outputs'}`
			: 'Enter one assignment per line, e.g. Y = A * B'

	return (
		<div className="tt-editor">
			<div className="tt-toolbar">
				<UndoButtons state={state} />
				<label className="tt-check tt-toolbar-check">
					<span className="switch">
						<input type="checkbox" checked={singleChar} disabled={!editable} onChange={(e) => truthTableApi.setSingleChar(e.target.checked)} />
						<span className="switch-track" />
					</span>
					One-character variables
				</label>
				<span className="tt-spacer" />
				<button
					type="button"
					className="btn btn-sm"
					disabled={!valid}
					onClick={() => {
						commit(draft)
						void saveAs('expression', 'truthTable', `${stored.name} table`, true)
					}}
				>
					<Table2 size={14} /> Save as truth table…
				</button>
				<button
					type="button"
					className="btn btn-sm btn-primary"
					disabled={!valid}
					onClick={() => {
						commit(draft)
						void createCircuit('expression', stored.name)
					}}
				>
					<Cpu size={14} /> Create circuit…
				</button>
			</div>

			<div className="tt-body">
				<div className="tt-main expr-main">
					<div className={`expr-input${error ? ' has-error' : ''}`}>
						<div ref={gutter} className="expr-gutter" aria-hidden="true">
							{Array.from({ length: lines }, (_, i) => (
								<div key={i} className={error?.row === i + 1 ? 'error' : undefined}>
									{i + 1}
								</div>
							))}
						</div>
						<textarea
							className="expr-text"
							value={draft}
							readOnly={!editable}
							spellCheck={false}
							autoFocus
							aria-label="Boolean expressions"
							placeholder={"Y = A * B' + C\nZ = A + B"}
							onScroll={(e) => {
								if (gutter.current) gutter.current.scrollTop = e.currentTarget.scrollTop
							}}
							onChange={(e) => {
								const text = e.target.value
								setDraft(text)
								window.clearTimeout(timer.current)
								timer.current = window.setTimeout(() => commit(text), COMMIT_DELAY)
							}}
							onBlur={() => commit(draft)}
						/>
					</div>
					<div className={`expr-status${error ? ' error' : valid ? ' ok' : ''}`} role="status">
						{error ? <CircleAlert size={14} /> : valid ? <CircleCheck size={14} /> : null}
						<span>{status}</span>
					</div>

					<div className="tt-side-header">
						<h3 className="tt-section-title">Minimized expressions</h3>
						<NotationMenu />
					</div>
					{valid ? <ExpressionList expressions={check?.expressions ?? []} /> : <div className="empty-small">No valid expressions.</div>}

					<h3 className="tt-section-title">Examples</h3>
					<dl className="expr-examples">
						{EXAMPLES.map(([label, text]) => (
							<div key={label}>
								<dt>{label}</dt>
								<dd>
									<code>{text}</code>
								</dd>
							</div>
						))}
					</dl>
				</div>
				<aside className="tt-side expr-table">
					<h3 className="tt-section-title">Truth table</h3>
					{table ? <TruthTableGrid table={table} /> : <div className="empty-small">The truth table appears once the expressions are valid.</div>}
				</aside>
			</div>
		</div>
	)
}
