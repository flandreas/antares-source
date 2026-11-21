// Find in the circuit (Mod+F, find bar over the canvas) and command search (Mod+K, desktop: Mod+Shift+A).
import { Search, TextSearch } from 'lucide-react'
import { engine } from '../../../core/engine'
import type { EditorState } from '../../../core/types'
import { matches } from '../../commands'
import { appMenuContributions, canvasOverlays, register } from '../registry'
import { PALETTE_ENTRY_LABEL, showCommandPalette } from './CommandPalette'
import { FindBar } from './FindBar'
import { findBar } from './store'
import './find.css'

const FIND_SHORTCUT = 'Mod+F'
const PALETTE_SHORTCUT = 'Mod+K'
const PALETTE_ALT_SHORTCUT = 'Mod+Shift+A'

/** Find needs a drawing: a circuit (or its symbol) shown instead of a document. */
const canFind = (s: EditorState) => !!s.circuit && !s.document

register(canvasOverlays, (o) => o.id, { id: 'find', component: FindBar })

register(appMenuContributions.edit, (c) => c.id, {
	id: 'find',
	entries: (state) => [
		{ label: 'Find…', icon: <Search size={15} />, shortcut: FIND_SHORTCUT, disabled: !canFind(state), onSelect: () => findBar.show() },
	],
})

register(appMenuContributions.help, (c) => c.id, {
	id: 'find',
	entries: () => [
		{ label: PALETTE_ENTRY_LABEL, icon: <TextSearch size={15} />, shortcut: PALETTE_SHORTCUT, onSelect: showCommandPalette },
	],
})

function onKeyDown(e: KeyboardEvent) {
	if (document.querySelector('.dialog-backdrop')) return
	if (matches(e, FIND_SHORTCUT)) {
		// Keep the browser's own find for documents and the project screen
		if (!canFind(engine.state)) return
		e.preventDefault()
		findBar.show()
	} else if (matches(e, PALETTE_SHORTCUT) || matches(e, PALETTE_ALT_SHORTCUT)) {
		e.preventDefault()
		showCommandPalette()
	}
}

window.addEventListener('keydown', onKeyDown)
import.meta.hot?.dispose(() => window.removeEventListener('keydown', onKeyDown))
