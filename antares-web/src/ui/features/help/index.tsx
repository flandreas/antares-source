// Help pages of the online user manual (desktop: HelpComponentAction, HelpLibraryItemAction, DocumentationAction).
import { BookOpen, CircleHelp } from 'lucide-react'
import { engine } from '../../../core/engine'
import type { MenuEntry } from '../../Menu'
import { appMenuContributions, canvasContextActions, register, shortcutDocs } from '../registry'

const SHORTCUT = 'F1'

function openPage(url: string) {
	window.open(url, '_blank', 'noopener,noreferrer')
}

/** The help page of the single selected component, if any. */
const selectionUrl = () => (engine.state.circuit ? (engine.api.help.selectionUrl() ?? null) : null)

/** Context menu entries for a palette element: its help page, if it has one. */
export function paletteHelpEntries(key: string): MenuEntry[] {
	const url = engine.api.help.paletteUrl(key) ?? null
	return [
		{
			label: url ? 'Help for this component' : 'No help available',
			icon: <CircleHelp size={15} />,
			disabled: !url,
			onSelect: () => url && openPage(url),
		},
	]
}

register(canvasContextActions, (a) => a.id, {
	id: 'help',
	entries: () => {
		const url = selectionUrl()
		return url
			? [{ label: 'Help for this component', icon: <CircleHelp size={15} />, shortcut: SHORTCUT, onSelect: () => openPage(url) }]
			: []
	},
})

register(appMenuContributions.help, (c) => c.id, {
	id: 'help',
	entries: () => {
		const url = selectionUrl()
		return [
			{ label: 'User manual', icon: <BookOpen size={15} />, onSelect: () => openPage(engine.api.help.manualUrl()) },
			{
				label: 'Help for selected component',
				icon: <CircleHelp size={15} />,
				shortcut: SHORTCUT,
				disabled: !url,
				onSelect: () => url && openPage(url),
			},
		]
	},
})

// F1 opens the help of the selected component, or the user manual
const onKey = (e: KeyboardEvent) => {
	if (e.key !== 'F1' || e.metaKey || e.ctrlKey || e.altKey || document.querySelector('.dialog-backdrop')) return
	e.preventDefault()
	openPage(selectionUrl() ?? engine.api.help.manualUrl())
}
window.addEventListener('keydown', onKey)
import.meta.hot?.dispose(() => window.removeEventListener('keydown', onKey))

register(shortcutDocs, (d) => d.label, { group: 'Help', label: 'Help for selected component', keys: SHORTCUT })
