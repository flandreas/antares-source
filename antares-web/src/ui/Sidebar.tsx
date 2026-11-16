import { memo, useMemo, useState } from 'react'
import { ChevronRight, Library, Plus, Search, X } from 'lucide-react'
import { dragType, engine } from '../core/engine'
import type { EditorState, PaletteItem, PaletteNode } from '../core/types'
import { Menu } from './Menu'
import { LibrariesDialog } from './Libraries'
import { Thumbnail } from './Thumbnail'
import { useStoredState } from './hooks'
import { paletteHelpEntries } from './features/help'
import { ProjectTree } from './features/library/ProjectTree'

type Tab = 'components' | 'circuits'

export function Sidebar({ state }: { state: EditorState }) {
	const [tab, setTab] = useStoredState<Tab>('antares.sidebarTab', 'components')
	return (
		<aside className="sidebar">
			<div className="tabs" role="tablist">
				<button
					type="button"
					role="tab"
					aria-selected={tab === 'components'}
					className={tab === 'components' ? 'selected' : ''}
					onClick={() => setTab('components')}
				>
					Components
				</button>
				<button
					type="button"
					role="tab"
					aria-selected={tab === 'circuits'}
					className={tab === 'circuits' ? 'selected' : ''}
					onClick={() => setTab('circuits')}
				>
					{state.project?.kind === 'library' ? 'Library' : 'Project'}
				</button>
			</div>
			{tab === 'components' ? <Palette state={state} /> : <ProjectTree state={state} />}
		</aside>
	)
}

/** ---- Palette */

function Palette({ state }: { state: EditorState }) {
	const palette = engine.palette
	const [query, setQuery] = useState('')
	const [collapsed, setCollapsed] = useStoredState<Record<string, boolean>>('antares.paletteCollapsed', {})
	const canPlace = state.mode === 'edit' && state.editable && state.view === 'circuit'

	const [showLibraries, setShowLibraries] = useState(false)
	const circuitType = state.circuit?.type

	// Only components that fit the open circuit's type (digital or analog) are offered.
	// Libraries are top-level nodes; with a single library its folders are shown directly.
	const { roots, libraryRoots } = useMemo(() => {
		const fits = (nodes: PaletteNode[]): PaletteNode[] =>
			nodes.flatMap((n): PaletteNode[] => {
				if (n.kind !== 'folder') return !circuitType || n.type === circuitType ? [n] : []
				const children = fits(n.children)
				return children.length > 0 ? [{ ...n, children }] : []
			})
		const filtered = fits(palette)
		const [first] = filtered
		if (filtered.length === 1 && first.kind === 'folder') return { roots: first.children, libraryRoots: false }
		return { roots: filtered, libraryRoots: true }
	}, [palette, circuitType])

	const results = useMemo(() => {
		const q = query.trim().toLowerCase()
		if (!q) return null
		const found: Array<PaletteItem & { folder: string }> = []
		const seen = new Set<string>()
		const walk = (nodes: PaletteNode[], folder: string) =>
			nodes.forEach((n) => {
				if (n.kind === 'folder') walk(n.children, n.name)
				else if (n.name.toLowerCase().includes(q) && !seen.has(`${n.name}|${folder}`)) {
					seen.add(`${n.name}|${folder}`)
					found.push({ ...n, folder })
				}
			})
		walk(roots, '')
		return found.sort((a, b) => Number(!a.name.toLowerCase().startsWith(q)) - Number(!b.name.toLowerCase().startsWith(q)))
	}, [query, roots])

	return (
		<div className="panel-scroll palette">
			<div className="search">
				<Search size={14} />
				<input
					value={query}
					onChange={(e) => setQuery(e.target.value)}
					placeholder="Search components"
					aria-label="Search components"
					onKeyDown={(e) => {
						if (e.key === 'Escape') setQuery('')
						if (e.key === 'Enter' && results?.[0] && canPlace) engine.placeAtCenter(results[0].key)
					}}
				/>
				{query && (
					<button type="button" className="icon-btn small" onClick={() => setQuery('')} aria-label="Clear search">
						<X size={13} />
					</button>
				)}
			</div>
			{!state.project ? (
				<div className="hint">Open a project to add components.</div>
			) : !canPlace && (
				<div className="hint">
					{state.mode === 'simulate'
						? 'Stop the simulation to add components.'
						: state.view === 'symbol'
							? 'Switch to the circuit view to add components.'
							: 'This circuit can’t be edited here.'}
				</div>
			)}
			{results ? (
				results.length === 0 ? (
					<div className="empty-small">No components match “{query}”.</div>
				) : (
					<div className="palette-list">
						{results.map((item) => (
							<PaletteRow key={item.key} item={item} detail={item.folder} disabled={!canPlace} />
						))}
					</div>
				)
			) : (
				<>
				{roots.map((node, i) =>
					node.kind === 'folder' ? (
						<PaletteFolder
							key={`${node.name}-${i}`}
							node={node}
							path={node.name}
							collapsed={collapsed}
							setCollapsed={setCollapsed}
							disabled={!canPlace}
							depth={0}
							defaultOpen={libraryRoots}
						/>
					) : (
						<PaletteRow key={node.key} item={node} disabled={!canPlace} />
					),
				)}
				{state.project && roots.length === 0 && (
					<div className="empty-small">No components for {circuitType?.toLowerCase()} circuits yet. Add a library below.</div>
				)}
				</>
			)}
			{state.project && (
				<div className="palette-footer">
					<button type="button" className="link-btn" onClick={() => setShowLibraries(true)}>
						<Library size={14} /> Component libraries…
					</button>
				</div>
			)}
			{showLibraries && <LibrariesDialog onClose={() => setShowLibraries(false)} />}
		</div>
	)
}

function PaletteFolder({
	node,
	path,
	collapsed,
	setCollapsed,
	disabled,
	depth,
	defaultOpen = false,
}: {
	defaultOpen?: boolean
	node: Extract<PaletteNode, { kind: 'folder' }>
	path: string
	collapsed: Record<string, boolean>
	setCollapsed: (v: Record<string, boolean>) => void
	disabled: boolean
	depth: number
}) {
	// Library roots (shown when several libraries are used) and "Frequently used" start expanded
	const isCollapsed = collapsed[path] ?? !(defaultOpen || path.startsWith('Frequently'))
	return (
		<section className={`palette-folder depth-${depth}`}>
			<button
				type="button"
				className="folder-header"
				aria-expanded={!isCollapsed}
				onClick={() => setCollapsed({ ...collapsed, [path]: !isCollapsed })}
			>
				<ChevronRight size={14} className={`chevron${isCollapsed ? '' : ' open'}`} />
				<span>{node.name}</span>
				<span className="count">{countItems(node.children)}</span>
			</button>
			{!isCollapsed && (
				<div className="palette-list">
					{node.children.map((child, i) =>
						child.kind === 'folder' ? (
							<PaletteFolder
								key={`${child.name}-${i}`}
								node={child}
								path={`${path}/${child.name}`}
								collapsed={collapsed}
								setCollapsed={setCollapsed}
								disabled={disabled}
								depth={depth + 1}
							/>
						) : (
							<PaletteRow key={child.key} item={child} disabled={disabled} />
						),
					)}
				</div>
			)}
		</section>
	)
}

const countItems = (nodes: PaletteNode[]): number =>
	nodes.reduce((n, c) => n + (c.kind === 'folder' ? countItems(c.children) : 1), 0)

const PaletteRow = memo(function PaletteRow({
	item,
	detail,
	disabled,
}: {
	item: PaletteItem
	detail?: string
	disabled: boolean
}) {
	const [menu, setMenu] = useState<{ x: number; y: number } | null>(null)
	return (
		<>
			<button
				type="button"
				className="palette-item"
				draggable={!disabled}
				aria-disabled={disabled}
				title={disabled ? item.name : `${item.name} — click to add, or drag onto the circuit`}
				onClick={() => !disabled && engine.placeAtCenter(item.key)}
				onContextMenu={(e) => {
					e.preventDefault()
					setMenu({ x: e.clientX, y: e.clientY })
				}}
				onDragStart={(e) => {
					e.dataTransfer.setData(dragType(item.key), '1')
					e.dataTransfer.setData('text/plain', item.name)
					e.dataTransfer.effectAllowed = 'copy'
					const ghost = (e.currentTarget.querySelector('.thumb img') as HTMLImageElement | null) ?? undefined
					if (ghost) e.dataTransfer.setDragImage(ghost, 16, 12)
				}}
			>
				<Thumbnail itemKey={item.key} kind={item.kind} />
				<span className="item-name truncate">{item.name}</span>
				{detail && <span className="item-detail truncate">{detail}</span>}
			</button>
			{menu && (
				<Menu
					entries={[
						{ label: 'Add to circuit', icon: <Plus size={15} />, disabled, onSelect: () => engine.placeAtCenter(item.key) },
						'separator',
						...paletteHelpEntries(item.key),
					]}
					x={menu.x}
					y={menu.y}
					onClose={() => setMenu(null)}
				/>
			)}
		</>
	)
})

/** ---- Items of the project: see `features/library/ProjectTree.tsx` */
