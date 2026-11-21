// Command search (desktop: `FindMenuAction` + `MenuCommandSearch`): lists all menu bar entries, the project menu
// and the project's items, filtered by the typed words; Enter runs the selected command.
import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { CornerDownLeft, FileText, Search } from 'lucide-react'
import { engine } from '../../../core/engine'
import type { EditorState, ProjectNode } from '../../../core/types'
import { dialogs } from '../../Dialogs'
import { formatShortcut } from '../../hooks'
import { menuBarActions, menuEntries } from '../../MenuBar'
import { appMenuContributions, projectMenuActions, type AppMenuId } from '../registry'
import { kindLabel } from '../../projectActions'

export interface PaletteCommand {
	id: string
	label: string
	/** Where the command lives, e.g. "Edit" or "Project › Folder" */
	path: string
	shortcut?: string
	icon?: ReactNode
	enabled: boolean
	run: () => void
}

/** The label of the palette's own menu entry, which is not listed. */
export const PALETTE_ENTRY_LABEL = 'Find command…'

const MENUS: Array<[AppMenuId, string]> = [
	['edit', 'Edit'],
	['view', 'View'],
	['simulate', 'Simulate'],
	['tools', 'Tools'],
	['help', 'Help'],
]

/** Collects the commands of all menus and the project items for the current state. */
function collect(state: EditorState): PaletteCommand[] {
	const result: PaletteCommand[] = []
	const actions = menuBarActions()
	for (const [id, title] of MENUS) {
		const entries = actions ? menuEntries(id, state, actions) : appMenuContributions[id].flatMap((c) => c.entries(state))
		let heading = ''
		for (const e of entries) {
			if (e === 'separator') continue
			if ('heading' in e) {
				heading = e.heading
				continue
			}
			if (e.label === PALETTE_ENTRY_LABEL) continue
			result.push({
				id: `${id}:${heading}:${e.label}`,
				label: e.label,
				path: heading ? `${title} › ${heading}` : title,
				shortcut: e.shortcut,
				icon: e.icon,
				enabled: !e.disabled,
				run: e.onSelect,
			})
		}
	}
	for (const a of projectMenuActions) {
		result.push({ id: `project:${a.label}`, label: a.label, path: 'Project', icon: a.icon, enabled: a.enabled(state), run: () => void a.run() })
	}
	const visit = (nodes: ProjectNode[], folder: string) => {
		for (const node of nodes) {
			if (node.kind === 'folder') {
				visit(node.children, folder ? `${folder} › ${node.name}` : node.name)
				continue
			}
			result.push({
				id: `item:${node.uuid}`,
				label: node.name,
				path: `Open ${kindLabel(node.kind)}${folder ? ` · ${folder}` : ''}`,
				icon: <FileText size={14} />,
				enabled: !node.isOpen,
				run: () => engine.openItem(node.uuid),
			})
		}
	}
	if (state.project) visit(engine.items ?? [], '')
	return result
}

const words = (text: string) => text.trim().toLowerCase().split(/\s+/).filter(Boolean)

/** Whether the letters of [word] occur in [text] in order. */
function isSubsequence(word: string, text: string): boolean {
	let i = 0
	for (const c of text) if (c === word[i] && ++i === word.length) return true
	return word.length === 0
}

/**
 * Filters like the desktop's `MenuCommandSearch` (all words in name or path, or the acronym), plus a fuzzy fallback
 * where the letters of each word occur in order; better matches come first.
 */
export function filterCommands(commands: PaletteCommand[], query: string): PaletteCommand[] {
	const ws = words(query)
	if (ws.length === 0) return commands
	const joined = ws.join(' ')
	const scored: Array<[PaletteCommand, number]> = []
	for (const c of commands) {
		const name = c.label.toLowerCase()
		const path = c.path.toLowerCase()
		const acronym = name
			.split(/[^\p{L}\p{N}]+/u)
			.filter(Boolean)
			.map((w) => w[0])
			.join('')
		let score: number
		if (ws.every((w) => name.includes(w) || path.includes(w) || acronym.startsWith(w))) {
			if (name === joined) score = 0
			else if (name.startsWith(joined)) score = 1
			else if (ws.every((w) => name.includes(w))) score = 2
			else if (ws.length === 1 && acronym.startsWith(ws[0])) score = 3
			else score = 4
		} else if (ws.every((w) => isSubsequence(w, name))) {
			score = 5
		} else continue
		// Disabled commands sink below enabled ones with the same score
		scored.push([c, score * 2 + (c.enabled ? 0 : 1)])
	}
	return scored.sort((a, b) => a[1] - b[1]).map(([c]) => c)
}

/** Opens the command search. */
export function showCommandPalette() {
	if (document.querySelector('.cmd-palette')) return
	void dialogs.custom<null>((done) => <CommandPalette onClose={() => done(null)} />)
}

function CommandPalette({ onClose }: { onClose: () => void }) {
	const commands = useMemo(() => collect(engine.state), [])
	const [query, setQuery] = useState('')
	const [active, setActive] = useState(0)
	const list = useRef<HTMLDivElement>(null)
	const matches = useMemo(() => filterCommands(commands, query), [commands, query])

	useEffect(() => setActive(0), [query])
	useEffect(() => {
		list.current?.querySelector('.cmd-item.active')?.scrollIntoView({ block: 'nearest' })
	}, [active])

	const execute = (c: PaletteCommand | undefined) => {
		if (!c?.enabled) return
		onClose()
		// Let the dialog close before the command opens its own dialogs or moves focus
		requestAnimationFrame(() => c.run())
	}

	return (
		<div className="dialog-backdrop cmd-backdrop" onPointerDown={(e) => e.target === e.currentTarget && onClose()}>
			<div className="cmd-palette" role="dialog" aria-modal="true" aria-label="Find command">
				<div className="cmd-search">
					<Search size={16} />
					<input
						autoFocus
						value={query}
						placeholder="Type a command or the name of a project item…"
						aria-label="Find command"
						aria-controls="cmd-results"
						aria-activedescendant={matches[active] ? `cmd-${active}` : undefined}
						spellCheck={false}
						onChange={(e) => setQuery(e.target.value)}
						onKeyDown={(e) => {
							e.stopPropagation()
							if (e.key === 'Escape') {
								e.preventDefault()
								onClose()
							} else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
								e.preventDefault()
								if (matches.length === 0) return
								const delta = e.key === 'ArrowDown' ? 1 : -1
								setActive((active + delta + matches.length) % matches.length)
							} else if (e.key === 'PageDown' || e.key === 'PageUp') {
								e.preventDefault()
								const delta = e.key === 'PageDown' ? 8 : -8
								setActive(Math.max(0, Math.min(matches.length - 1, active + delta)))
							} else if (e.key === 'Enter') {
								e.preventDefault()
								execute(matches[active])
							}
						}}
					/>
				</div>
				<div className="cmd-results" id="cmd-results" role="listbox" ref={list}>
					{matches.length === 0 && <div className="empty-small">No matching commands</div>}
					{matches.map((c, i) => (
						<div
							key={c.id}
							id={`cmd-${i}`}
							role="option"
							aria-selected={i === active}
							aria-disabled={!c.enabled}
							className={`cmd-item${i === active ? ' active' : ''}${c.enabled ? '' : ' disabled'}`}
							onPointerMove={() => i !== active && setActive(i)}
							onClick={() => execute(c)}
						>
							<span className="cmd-icon">{c.icon}</span>
							<span className="cmd-text">
								<span className="cmd-label">{c.label}</span>
								<span className="cmd-path">{c.path}</span>
							</span>
							{c.shortcut && <kbd className="cmd-shortcut">{formatShortcut(c.shortcut)}</kbd>}
						</div>
					))}
				</div>
				<div className="cmd-footer">
					<span>
						<kbd>↑</kbd>
						<kbd>↓</kbd> to choose
					</span>
					<span>
						<kbd>
							<CornerDownLeft size={10} />
						</kbd>{' '}
						to run
					</span>
					<span>
						<kbd>Esc</kbd> to close
					</span>
					<span className="cmd-count">
						{matches.length} {matches.length === 1 ? 'result' : 'results'}
					</span>
				</div>
			</div>
		</div>
	)
}
