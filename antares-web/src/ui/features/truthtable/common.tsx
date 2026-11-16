import { useEffect } from 'react'
import { Redo2, Type, Undo2 } from 'lucide-react'
import type { EditorState } from '../../../core/types'
import { formatShortcut, isTypingTarget } from '../../hooks'
import { MenuButton, type MenuEntry } from '../../Menu'
import { truthTableApi } from './api'

/**
 * Undo/redo of document edits with Mod+Z / Mod+Shift+Z (and Mod+Y). The editor's own undo action is bound to the
 * circuit view, so documents handle the shortcuts themselves, ahead of the global keyboard handler.
 */
export function useDocumentUndo() {
	useEffect(() => {
		const onKey = (e: KeyboardEvent) => {
			if (!(e.metaKey || e.ctrlKey) || e.altKey || isTypingTarget(e.target) || document.querySelector('.dialog-backdrop')) return
			const key = e.key.toLowerCase()
			if (key !== 'z' && key !== 'y') return
			e.preventDefault()
			e.stopPropagation()
			if (key === 'y' || e.shiftKey) truthTableApi.redo()
			else truthTableApi.undo()
		}
		window.addEventListener('keydown', onKey, true)
		return () => window.removeEventListener('keydown', onKey, true)
	}, [])
}

/** Undo and redo buttons for the open document. */
export function UndoButtons({ state }: { state: EditorState }) {
	const editable = !!state.document?.editable
	return (
		<div className="tt-toolbar-group">
			<button
				type="button"
				className="icon-btn"
				disabled={!editable || !state.canUndo}
				onClick={() => truthTableApi.undo()}
				title={`Undo${state.undoLabel ? ` ${state.undoLabel}` : ''} (${formatShortcut('Mod+Z')})`}
				aria-label="Undo"
			>
				<Undo2 size={16} />
			</button>
			<button
				type="button"
				className="icon-btn"
				disabled={!editable || !state.canRedo}
				onClick={() => truthTableApi.redo()}
				title={`Redo${state.redoLabel ? ` ${state.redoLabel}` : ''} (${formatShortcut('Mod+Shift+Z')})`}
				aria-label="Redo"
			>
				<Redo2 size={16} />
			</button>
		</div>
	)
}

/** The notation of generated expressions (the desktop's expression preferences). */
export function NotationMenu() {
	const entries = (): MenuEntry[] => {
		const n = truthTableApi.notation()
		return [
			{ heading: 'Notation' },
			...n.choices.map(
				(c): MenuEntry => ({
					label: c.label,
					checked: c.id === n.notation,
					onSelect: () => truthTableApi.setNotation(c.id, n.omitAnd, n.andParenthesis),
				}),
			),
			'separator',
			{
				label: 'Omit AND for single-character names',
				checked: n.omitAnd,
				onSelect: () => truthTableApi.setNotation(n.notation, !n.omitAnd, n.andParenthesis),
			},
			{
				label: 'Put AND terms in parentheses',
				checked: n.andParenthesis,
				onSelect: () => truthTableApi.setNotation(n.notation, n.omitAnd, !n.andParenthesis),
			},
		]
	}
	return (
		<MenuButton entries={entries} className="btn btn-sm btn-ghost" title="Notation of minimized expressions" align="right">
			<Type size={14} /> Notation
		</MenuButton>
	)
}
