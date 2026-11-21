// Editor commands with their shortcuts, shared by menus, the keyboard handler and the shortcut overview.
import { engine } from '../core/engine'
import type { ActionName, EditorState } from '../core/types'
import { isMac } from './hooks'

export interface Command {
	id: string
	label: string
	group: 'Edit' | 'Arrange' | 'View' | 'Simulation' | 'File'
	/** e.g. "Mod+Shift+Z"; "Mod" is ⌘ on macOS and Ctrl elsewhere. */
	shortcut?: string
	/** Additional shortcuts that trigger the command but are not displayed. */
	altShortcuts?: string[]
	enabled: (s: EditorState) => boolean
	run: () => void
}

const action = (
	id: ActionName,
	label: string,
	group: Command['group'],
	shortcut?: string,
	altShortcuts?: string[],
): Command => ({
	id,
	label,
	group,
	shortcut,
	altShortcuts,
	enabled: (s) => s.mode === 'edit' && s.actions[id],
	run: () => engine.perform(id),
})

const editing = (s: EditorState) => s.mode === 'edit' && !!s.circuit

export const commands: Command[] = [
	{
		id: 'undo',
		label: 'Undo',
		group: 'Edit',
		shortcut: 'Mod+Z',
		// Documents (truth tables, …) have no drawing view, so the editor's command manager decides
		enabled: (s) => s.mode === 'edit' && (s.document ? s.canUndo : s.actions.undo),
		run: () => engine.api.undo(),
	},
	{
		id: 'redo',
		label: 'Redo',
		group: 'Edit',
		shortcut: 'Mod+Shift+Z',
		altShortcuts: ['Mod+Y'],
		enabled: (s) => s.mode === 'edit' && (s.document ? s.canRedo : s.actions.redo),
		run: () => engine.api.redo(),
	},
	action('cut', 'Cut', 'Edit', 'Mod+X'),
	action('copy', 'Copy', 'Edit', 'Mod+C'),
	action('paste', 'Paste', 'Edit', 'Mod+V'),
	action('duplicate', 'Duplicate', 'Edit', 'Mod+D'),
	action('delete', 'Delete', 'Edit', isMac ? 'Backspace' : 'Delete', ['Delete', 'Backspace']),
	action('selectAll', 'Select all', 'Edit', 'Mod+A'),
	action('rotateRight', 'Rotate clockwise', 'Arrange', 'R'),
	action('rotateLeft', 'Rotate counter-clockwise', 'Arrange', 'Shift+R'),
	action('toFront', 'Bring to front', 'Arrange', 'Mod+]'),
	action('toBack', 'Send to back', 'Arrange', 'Mod+['),
	action('group', 'Group', 'Arrange', 'Mod+G'),
	action('ungroup', 'Ungroup', 'Arrange', 'Mod+Shift+G'),
	{
		id: 'zoomIn',
		label: 'Zoom in',
		group: 'View',
		shortcut: 'Mod+=',
		altShortcuts: ['Mod++'],
		enabled: (s) => !!s.circuit,
		run: () => engine.zoomIn(),
	},
	{
		id: 'zoomOut',
		label: 'Zoom out',
		group: 'View',
		shortcut: 'Mod+-',
		enabled: (s) => !!s.circuit,
		run: () => engine.zoomOut(),
	},
	{
		id: 'zoomFit',
		label: 'Zoom to fit',
		group: 'View',
		shortcut: 'Mod+0',
		enabled: (s) => !!s.circuit,
		run: () => engine.zoomToFit(),
	},
	{
		id: 'zoomNormal',
		label: 'Zoom to 100%',
		group: 'View',
		shortcut: 'Mod+1',
		enabled: (s) => !!s.circuit,
		run: () => engine.zoomToNormal(),
	},
	{
		id: 'toggleSymbol',
		label: 'Toggle circuit / symbol',
		group: 'View',
		shortcut: 'Mod+Alt+S',
		enabled: editing,
		run: () => engine.showSymbol(engine.state.view !== 'symbol'),
	},
	{
		id: 'simulate',
		label: 'Start / stop simulation',
		group: 'Simulation',
		shortcut: 'Mod+Enter',
		altShortcuts: ['Ctrl+R'],
		enabled: (s) => !!s.circuit,
		run: () => engine.setSimulating(engine.state.mode !== 'simulate'),
	},
	{
		id: 'pause',
		label: 'Pause / resume',
		group: 'Simulation',
		shortcut: 'Mod+P',
		enabled: (s) => s.mode === 'simulate',
		run: () => engine.togglePause(),
	},
	{
		id: 'step',
		label: 'Single step',
		group: 'Simulation',
		shortcut: 'Mod+.',
		enabled: (s) => s.mode === 'simulate',
		run: () => engine.step(),
	},
	{
		id: 'save',
		label: 'Save',
		group: 'File',
		shortcut: 'Mod+S',
		enabled: () => true,
		run: () => {
			engine.save()
			engine.message('success', '', 'All changes saved in this browser')
		},
	},
]

export const command = (id: string): Command => commands.find((c) => c.id === id)!

/** Returns whether a keyboard event matches a shortcut like "Mod+Shift+Z" ("Mod" is ⌘ on macOS, Ctrl elsewhere). */
export function matches(e: KeyboardEvent, shortcut: string): boolean {
	const parts = shortcut.split('+')
	let key = parts.pop()!
	if (key === '') {
		// "Mod++" means the "+" key
		parts.pop()
		key = '+'
	}
	const wantMod = parts.includes('Mod')
	const wantCtrl = parts.includes('Ctrl')
	if (wantMod !== (isMac ? e.metaKey : e.ctrlKey)) return false
	if (isMac && wantCtrl !== e.ctrlKey) return false
	if (!isMac && wantCtrl && !e.ctrlKey) return false
	if (parts.includes('Alt') !== e.altKey) return false
	// Shift is part of symbols like "+" or "]" on many layouts, so it only matters for letters and named keys
	const isSymbol = key.length === 1 && !/[a-z0-9]/i.test(key)
	if (!isSymbol && parts.includes('Shift') !== e.shiftKey) return false
	const pressed = e.code.startsWith('Key') ? e.code.slice(3) : e.code.startsWith('Digit') ? e.code.slice(5) : e.key
	return pressed.toLowerCase() === key.toLowerCase() || e.key === key
}
