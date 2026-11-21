// Markdown documentation of circuits: the "Docs" inspector tab of the open circuit (desktop: DocumentationPanelSwing)
// and the documentation of a sub-circuit, shown from the canvas context menu (desktop: OpenDocumentationAction).
import { useEffect, useMemo, useRef, useState } from 'react'
import { BookOpen, Maximize2 } from 'lucide-react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import type { MenuEntry } from '../../Menu'
import { useEngineVersion, useStoredState } from '../../hooks'
import { circuitPanels, dialogViews, type DialogViewProps, register } from '../registry'
import { markdownToHtml } from './markdown'
import './docs.css'

interface CircuitDocs {
	text: string
	editable: boolean
}

const docsApi = {
	circuit: () => JSON.parse(engine.api.docs.circuit()) as CircuitDocs | null,
	setCircuitText: (text: string) => engine.api.docs.setCircuitText(text),
	dialog: () => JSON.parse(engine.api.docs.dialog()) as { title: string; text: string } | null,
}

/** Typing pauses after which edits are stored as one undoable change. */
const COMMIT_DELAY = 800

/** Context menu entries for the canvas: "Show documentation" while a single sub-circuit is selected. */
export function documentationMenuEntries(): MenuEntry[] {
	if (!engine.api.docs.canShowSelected()) return []
	return [{ label: 'Show documentation', icon: <BookOpen size={15} />, onSelect: () => engine.api.docs.showSelected() }]
}

/** Rendered Markdown. */
export function MarkdownView({ text, empty }: { text: string; empty: string }) {
	const html = useMemo(() => markdownToHtml(text), [text])
	if (!text.trim()) return <div className="empty-small">{empty}</div>
	return <div className="markdown" dangerouslySetInnerHTML={{ __html: html }} />
}

/** The documentation of a sub-circuit (desktop: DocumentationDesktopViewItemSwing). */
function DocumentationDialog({ state }: DialogViewProps) {
	const version = useEngineVersion()
	const doc = useMemo(() => docsApi.dialog(), [version, state.dialog])
	const close = () => engine.api.docs.closeDialog()
	if (!doc) return null
	return (
		<DialogFrame title={`Documentation of “${doc.title}”`} onCancel={close} wide>
			<div className="dialog-body docs-dialog-body">
				<MarkdownView text={doc.text} empty="This circuit has no documentation." />
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn btn-primary" onClick={close} autoFocus>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}

/**
 * Edits [text], committing changes as undoable change after a pause in typing and when leaving the field,
 * and follows external changes (e.g. undo) while not being edited.
 */
function useDocsDraft(text: string) {
	const [draft, setDraft] = useState(text)
	const editing = useRef(false)
	const timer = useRef<number | undefined>(undefined)
	useEffect(() => {
		if (!editing.current) setDraft(text)
	}, [text])
	const commit = (value = draft) => {
		window.clearTimeout(timer.current)
		timer.current = undefined
		if (value !== text) docsApi.setCircuitText(value)
	}
	useEffect(() => () => window.clearTimeout(timer.current), [])
	return {
		draft,
		change(value: string) {
			editing.current = true
			setDraft(value)
			window.clearTimeout(timer.current)
			timer.current = window.setTimeout(() => commit(value), COMMIT_DELAY)
		},
		blur() {
			editing.current = false
			commit()
		},
	}
}

type Mode = 'write' | 'preview'

/** The documentation of the open circuit, as inspector tab. */
function DocsPanel() {
	const version = useEngineVersion()
	const docs = useMemo(() => docsApi.circuit(), [version])
	const [mode, setMode] = useStoredState<Mode>('antares.docs.mode', 'preview')
	const { draft, change, blur } = useDocsDraft(docs?.text ?? '')
	if (!docs) return <div className="inspector-empty">Open a circuit to see its documentation.</div>
	const editable = docs.editable
	const shown: Mode = editable ? mode : 'preview'
	return (
		<div className="docs-panel">
			<div className="docs-toolbar">
				{editable ? (
					<div className="segmented small" role="tablist" aria-label="Documentation mode">
						{(['write', 'preview'] as const).map((m) => (
							<button key={m} type="button" role="tab" aria-selected={shown === m} className={shown === m ? 'selected' : ''} onClick={() => setMode(m)}>
								{m === 'write' ? 'Write' : 'Preview'}
							</button>
						))}
					</div>
				) : (
					<span className="docs-readonly">Read-only</span>
				)}
				<span className="docs-spacer" />
				<button type="button" className="icon-btn small" title="Open in a larger editor" onClick={() => void openLargeEditor()}>
					<Maximize2 size={14} />
				</button>
			</div>
			{shown === 'write' ? (
				<textarea
					className="docs-textarea"
					value={draft}
					spellCheck
					placeholder={'Describe what this circuit does, its inputs and outputs…\n\nMarkdown is supported: # Heading, **bold**, *italic*, `code`, - lists, | tables |.'}
					onChange={(e) => change(e.target.value)}
					onBlur={blur}
					aria-label="Documentation (Markdown)"
				/>
			) : (
				<div className="docs-preview">
					<MarkdownView
						text={draft}
						empty={editable ? 'No documentation yet. Switch to Write to describe this circuit.' : 'This circuit has no documentation.'}
					/>
				</div>
			)}
		</div>
	)
}

/** Edits the documentation of the open circuit side by side with its preview (desktop: "Edit & Preview"). */
function openLargeEditor() {
	return dialogs.custom<void>((done) => <LargeEditor onClose={() => done(null)} />)
}

function LargeEditor({ onClose }: { onClose: () => void }) {
	const version = useEngineVersion()
	const docs = useMemo(() => docsApi.circuit(), [version])
	const { draft, change, blur } = useDocsDraft(docs?.text ?? '')
	const close = () => {
		blur()
		onClose()
	}
	return (
		<DialogFrame title={`Documentation of “${engine.state.circuit?.name ?? ''}”`} onCancel={close} wide>
			<div className={`docs-large${docs?.editable ? '' : ' readonly'}`}>
				{docs?.editable && (
					<textarea
						className="docs-textarea"
						value={draft}
						autoFocus
						spellCheck
						onChange={(e) => change(e.target.value)}
						onBlur={blur}
						aria-label="Documentation (Markdown)"
					/>
				)}
				<div className="docs-preview">
					<MarkdownView text={draft} empty="Nothing to preview yet." />
				</div>
			</div>
			<div className="dialog-actions">
				<span className="hint subtle docs-hint">Markdown · changes are saved automatically</span>
				<button type="button" className="btn btn-primary" onClick={close}>
					Done
				</button>
			</div>
		</DialogFrame>
	)
}

dialogViews.documentation = DocumentationDialog
register(circuitPanels, (p) => p.id, { id: 'docs', label: 'Docs', component: DocsPanel })
