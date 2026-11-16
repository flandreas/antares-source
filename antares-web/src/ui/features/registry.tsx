// Extension points for feature modules (memory, truth tables, state machines, test cases, …).
// Each feature lives in its own files under `src/ui/features/` and registers itself here.
import type { ComponentType, ReactNode } from 'react'
import type { EditorState, ItemKind } from '../../core/types'
import type { MenuEntry } from '../Menu'

export interface DocumentEditorProps {
	state: EditorState
}

export interface DialogViewProps {
	state: EditorState
	onClose: () => void
}

/** Editors for non-circuit project items, shown instead of the circuit canvas. */
export const documentEditors: Partial<Record<ItemKind, ComponentType<DocumentEditorProps>>> = {}

/** Views for documents shown in dialogs (`EditorState.dialog.kind`). */
export const dialogViews: Partial<Record<string, ComponentType<DialogViewProps>>> = {}

/** Entries of the "New" menu in the project panel. */
export interface NewItemAction {
	label: string
	icon: ReactNode
	/** Creates and opens the item, usually after asking for a name. */
	run: () => void | Promise<void>
}

export const newItemActions: NewItemAction[] = []

/** Additional panels for the open circuit (e.g. test cases), shown as tabs in the inspector. */
export interface CircuitPanel {
	id: string
	label: string
	component: ComponentType<DocumentEditorProps>
}

export const circuitPanels: CircuitPanel[] = []

/** Additional entries for the project menu in the top bar (e.g. HDL export). */
export interface ProjectMenuAction {
	label: string
	icon: ReactNode
	enabled: (state: EditorState) => boolean
	run: () => void | Promise<void>
}

export const projectMenuActions: ProjectMenuAction[] = []

/** Inspector contents for open documents (the inspector shows component properties for circuits). */
export const documentInspectors: Partial<Record<ItemKind, ComponentType<DocumentEditorProps>>> = {}

/**
 * Adds [entries] to a registry list, replacing entries with the same key, so that modules re-evaluated by
 * hot module replacement don't register twice.
 */
export function register<T>(list: T[], key: (entry: T) => string, ...entries: T[]) {
	for (const entry of entries) {
		const index = list.findIndex((e) => key(e) === key(entry))
		if (index >= 0) list[index] = entry
		else list.push(entry)
	}
}

/** Menus of the menu bar in the top bar. */
export type AppMenuId = 'edit' | 'view' | 'simulate' | 'tools' | 'help'

/**
 * Additional entries for the menu bar. Each contributor returns the entries to show for the current state
 * (use `'separator'` to start a group); contributions are shown in registration order after the built-in entries.
 */
export const appMenuContributions: Record<AppMenuId, Array<{ id: string; entries: (state: EditorState) => MenuEntry[] }>> = {
	edit: [],
	view: [],
	simulate: [],
	tools: [],
	help: [],
}

/** Additional entries for the canvas context menu (circuit view, edit mode), e.g. "Extract to sub-circuit". */
export const canvasContextActions: Array<{ id: string; entries: (state: EditorState) => MenuEntry[] }> = []

/** Additional overlays on the circuit canvas (e.g. a find bar or a drawing tools toolbar). */
export const canvasOverlays: Array<{ id: string; component: ComponentType<DocumentEditorProps> }> = []

/** Shortcuts of feature modules, listed in the keyboard shortcuts dialog. `keys` uses the "Mod+Shift+K" notation. */
export const shortcutDocs: Array<{ group: string; label: string; keys: string }> = []
