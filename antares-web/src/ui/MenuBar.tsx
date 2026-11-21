import { useRef, useState } from 'react'
import type { EditorState } from '../core/types'
import { command } from './commands'
import { appMenuContributions, type AppMenuId } from './features/registry'
import { Menu, type MenuEntry } from './Menu'

export interface MenuBarActions {
	toggleSidebar: () => void
	toggleInspector: () => void
	toggleTheme: () => void
	showShortcuts: () => void
	showPreferences: () => void
	showAbout: () => void
}

const titles: Record<AppMenuId, string> = {
	edit: 'Edit',
	view: 'View',
	simulate: 'Simulate',
	tools: 'Tools',
	help: 'Help',
}

/** Turns a command into a menu entry for the current state. */
const entry = (state: EditorState, id: string, label?: string): MenuEntry => {
	const c = command(id)
	return { label: label ?? c.label, shortcut: c.shortcut, disabled: !c.enabled(state), onSelect: c.run }
}

function builtIn(menu: AppMenuId, state: EditorState, actions: MenuBarActions): MenuEntry[] {
	switch (menu) {
		case 'edit':
			return [
				entry(state, 'undo'),
				entry(state, 'redo'),
				'separator',
				entry(state, 'cut'),
				entry(state, 'copy'),
				entry(state, 'paste'),
				entry(state, 'duplicate'),
				entry(state, 'delete'),
				'separator',
				entry(state, 'selectAll'),
				'separator',
				entry(state, 'rotateRight'),
				entry(state, 'rotateLeft'),
				entry(state, 'toFront'),
				entry(state, 'toBack'),
				entry(state, 'group'),
				entry(state, 'ungroup'),
			]
		case 'view':
			return [
				entry(state, 'zoomIn'),
				entry(state, 'zoomOut'),
				entry(state, 'zoomFit'),
				entry(state, 'zoomNormal'),
				'separator',
				entry(state, 'toggleSymbol', state.view === 'symbol' ? 'Show circuit' : 'Show symbol'),
				'separator',
				{ label: 'Toggle sidebar', shortcut: 'Mod+\\', onSelect: actions.toggleSidebar },
				{ label: 'Toggle inspector', onSelect: actions.toggleInspector },
				{ label: 'Toggle dark theme', onSelect: actions.toggleTheme },
			]
		case 'simulate':
			return [
				entry(state, 'simulate', state.mode === 'simulate' ? 'Stop simulation' : 'Start simulation'),
				entry(state, 'pause', state.paused ? 'Resume' : 'Pause'),
				entry(state, 'step'),
			]
		case 'tools':
			return [{ label: 'Preferences…', onSelect: actions.showPreferences }]
		case 'help':
			return [
				{ label: 'Keyboard shortcuts', shortcut: '?', onSelect: actions.showShortcuts },
				{ label: 'Antares website', onSelect: () => window.open('https://www.antarescircuit.io', '_blank', 'noopener') },
				'separator',
				{ label: 'About Antares', onSelect: actions.showAbout },
			]
	}
}

/** Combines built-in entries with feature contributions (separators are normalized by [Menu]). */
export function menuEntries(menu: AppMenuId, state: EditorState, actions: MenuBarActions): MenuEntry[] {
	const groups = [builtIn(menu, state, actions), ...appMenuContributions[menu].map((c) => c.entries(state))]
	const result: MenuEntry[] = []
	for (const group of groups) {
		if (group.length === 0) continue
		if (result.length > 0) result.push('separator')
		result.push(...group)
	}
	return result
}

let currentActions: MenuBarActions | null = null

/** The actions of the mounted menu bar, for features that list all menu entries (e.g. the command search). */
export const menuBarActions = (): MenuBarActions | null => currentActions

/** The application menus (Edit, View, Simulate, Tools, Help). Hovering switches between open menus. */
export function MenuBar({ state, actions }: { state: EditorState; actions: MenuBarActions }) {
	currentActions = actions
	const [open, setOpen] = useState<{ id: AppMenuId; x: number; y: number } | null>(null)
	const buttons = useRef<Partial<Record<AppMenuId, HTMLButtonElement | null>>>({})

	const openMenu = (id: AppMenuId) => {
		const r = buttons.current[id]!.getBoundingClientRect()
		setOpen({ id, x: r.left, y: r.bottom + 4 })
	}

	return (
		<nav className="menubar" aria-label="Main menu">
			{(Object.keys(titles) as AppMenuId[]).map((id) => (
				<button
					key={id}
					ref={(el) => {
						buttons.current[id] = el
					}}
					type="button"
					className={`menubar-item${open?.id === id ? ' pressed' : ''}`}
					aria-haspopup="menu"
					aria-expanded={open?.id === id}
					onClick={() => (open?.id === id ? setOpen(null) : openMenu(id))}
					onPointerEnter={() => open && open.id !== id && openMenu(id)}
				>
					{titles[id]}
				</button>
			))}
			{open && (
				<Menu
					key={open.id}
					entries={menuEntries(open.id, state, actions)}
					x={open.x}
					y={open.y}
					minWidth={230}
					onClose={() => setOpen(null)}
				/>
			)}
		</nav>
	)
}
