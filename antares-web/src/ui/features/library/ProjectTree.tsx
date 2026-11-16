import { useRef, useState, type DragEvent, type MouseEvent, type ReactNode } from 'react'
import {
	BookMarked,
	ChevronRight,
	ChevronsDownUp,
	ChevronsUpDown,
	Copy,
	Cpu,
	Folder,
	FolderOpen,
	FolderPlus,
	Image as ImageIcon,
	Library as LibraryIcon,
	MemoryStick,
	MoreHorizontal,
	Pencil,
	Plus,
	Settings2,
	Sigma,
	Star,
	Table2,
	Trash2,
	Waves,
	Workflow,
	X,
} from 'lucide-react'
import { dragType, engine } from '../../../core/engine'
import type { EditorState, ItemKind } from '../../../core/types'
import { Menu, MenuButton, type MenuEntry } from '../../Menu'
import { LibrariesDialog } from '../../Libraries'
import { useStoredState } from '../../hooks'
import { deleteItem, renameItem } from '../../projectActions'
import { newItemActions } from '../registry'
import { libraryApi, type Tree, type TreeFolder, type TreeItem, type TreeNode } from './api'
import {
	closeLibrary,
	deleteFolder,
	folderProperties,
	newCircuitIn,
	newFolder,
	newItemIn,
	renameFolder,
} from './actions'
import { showProperties } from './PropertiesDialog'
import { showLibraryManager } from './LibraryManager'

const itemIcons: Record<ItemKind, ReactNode> = {
	circuit: <Cpu size={15} />,
	truthTable: <Table2 size={15} />,
	fsm: <Workflow size={15} />,
	memory: <MemoryStick size={15} />,
	expression: <Sigma size={15} />,
	image: <ImageIcon size={15} />,
}

/** HTML drag data type for moving tree nodes; its value is the node's path. Not matched by the canvas drop target. */
const TREE_DRAG_TYPE = 'application/x-antares.tree'

type DropPosition = 'before' | 'after' | 'inside'

interface DropTarget {
	path: string
	position: DropPosition
}

const parentOf = (path: string) => (path.includes('.') ? path.slice(0, path.lastIndexOf('.')) : '')
const indexOf = (path: string) => Number(path.slice(path.lastIndexOf('.') + 1))

/** Folder and index a node dropped at [target] is moved to. */
function destination(target: DropTarget): { folder: string; index: number } {
	if (target.position === 'inside') return { folder: target.path, index: -1 }
	return { folder: parentOf(target.path), index: indexOf(target.path) + (target.position === 'after' ? 1 : 0) }
}

const countCircuits = (nodes: TreeNode[]): number =>
	nodes.reduce((n, c) => n + (c.kind === 'folder' ? countCircuits(c.children) : c.kind === 'circuit' ? 1 : 0), 0)

const folderKeys = (nodes: TreeNode[], prefix: string, into: string[] = []): string[] => {
	nodes.forEach((n) => {
		if (n.kind !== 'folder') return
		const key = `${prefix}/${n.name}`
		into.push(key)
		folderKeys(n.children, key, into)
	})
	return into
}

/**
 * The tree of the open project or library (desktop: `LibraryTreeViewSwing` with `DigitalLibraryTreeViewActionsSwing`):
 * folders that can be collapsed, created, renamed and deleted, items and folders that can be moved by drag and drop,
 * and circuits that can be dragged onto the canvas to use them as sub-circuits.
 */
export function ProjectTree({ state }: { state: EditorState }) {
	const tree = libraryApi.tree()
	const editing = state.mode === 'edit'
	const [collapsed, setCollapsed] = useStoredState<Record<string, boolean>>('antares.treeCollapsed', {})
	const [menu, setMenu] = useState<{ x: number; y: number; entries: MenuEntry[] } | null>(null)
	const [drop, setDrop] = useState<DropTarget | null>(null)
	const [showLibraries, setShowLibraries] = useState(false)
	const dragged = useRef<string | null>(null)
	const allowed = useRef(new Map<string, boolean>())
	const expandTimer = useRef<{ key: string; timer: number } | null>(null)

	if (!tree) return <div className="empty-small">Open a project to see its circuits.</div>

	const isLibrary = tree.kind === 'library'
	const rootKey = tree.uuid
	const circuitCount = countCircuits(tree.children)
	const allFolders = folderKeys(tree.children, rootKey)
	const anyExpanded = allFolders.some((k) => !collapsed[k])

	const setAll = (value: boolean) => {
		const next = { ...collapsed }
		allFolders.forEach((k) => {
			if (value) next[k] = true
			else delete next[k]
		})
		setCollapsed(next)
	}

	const newEntries = (folder: string): MenuEntry[] => [
		{ label: 'Circuit…', icon: <Cpu size={15} />, disabled: !editing, onSelect: () => void newCircuitIn(folder, 'digital') },
		{ label: 'Analog circuit…', icon: <Waves size={15} />, disabled: !editing, onSelect: () => void newCircuitIn(folder, 'analog') },
		{ label: 'Folder…', icon: <FolderPlus size={15} />, disabled: !editing, onSelect: () => void newFolder(folder) },
		...(newItemActions.length > 0 ? (['separator'] as MenuEntry[]) : []),
		...newItemActions.map(
			(a): MenuEntry => ({ label: a.label, icon: a.icon, disabled: !editing, onSelect: () => void newItemIn(folder, a.run) }),
		),
	]

	const rootMenu = (): MenuEntry[] => [
		{ heading: 'New' },
		...newEntries(''),
		'separator',
		{ label: 'Expand all', icon: <ChevronsUpDown size={15} />, onSelect: () => setAll(false) },
		{ label: 'Collapse all', icon: <ChevronsDownUp size={15} />, onSelect: () => setAll(true) },
		'separator',
		{ label: 'Component libraries…', icon: <LibraryIcon size={15} />, onSelect: () => setShowLibraries(true) },
		{ label: isLibrary ? 'Library properties…' : 'Project properties…', icon: <Settings2 size={15} />, onSelect: () => void showProperties() },
		...(isLibrary ? ([{ label: 'Close library', icon: <X size={15} />, onSelect: closeLibrary }] as MenuEntry[]) : []),
	]

	const folderMenu = (folder: TreeFolder, key: string): MenuEntry[] => {
		const isCollapsed = !!collapsed[key]
		return [
			{ heading: 'New in this folder' },
			...newEntries(folder.path),
			'separator',
			{
				label: isCollapsed ? 'Expand' : 'Collapse',
				icon: isCollapsed ? <ChevronsUpDown size={15} /> : <ChevronsDownUp size={15} />,
				onSelect: () => setCollapsed({ ...collapsed, [key]: !isCollapsed }),
			},
			{ label: 'Rename…', icon: <Pencil size={15} />, disabled: !editing, onSelect: () => void renameFolder(folder.path, folder.name) },
			{ label: 'Properties…', icon: <Settings2 size={15} />, onSelect: () => void folderProperties(folder.path, editing) },
			'separator',
			{
				label: folder.children.length > 0 ? 'Delete (folder not empty)' : 'Delete…',
				icon: <Trash2 size={15} />,
				danger: true,
				disabled: !editing || folder.children.length > 0,
				onSelect: () => void deleteFolder(folder.path, folder.name),
			},
		]
	}

	const itemMenu = (item: TreeItem): MenuEntry[] => [
		{ label: 'Open', icon: <FolderOpen size={15} />, disabled: item.isOpen, onSelect: () => engine.openItem(item.uuid) },
		{ label: 'Rename…', icon: <Pencil size={15} />, disabled: !editing, onSelect: () => void renameItem(item) },
		...(item.kind === 'circuit'
			? ([
					{ label: 'Duplicate', icon: <Copy size={15} />, disabled: !editing, onSelect: () => engine.duplicateCircuit(item.uuid) },
					{
						label: isLibrary ? 'Open first when editing' : 'Open on project start',
						icon: <Star size={15} />,
						checked: item.isDefault,
						disabled: item.isDefault,
						onSelect: () => engine.setDefaultCircuit(item.uuid),
					},
				] as MenuEntry[])
			: []),
		'separator',
		{
			label: 'Delete…',
			icon: <Trash2 size={15} />,
			danger: true,
			disabled: !editing || (!isLibrary && item.kind === 'circuit' && item.isOpen && circuitCount <= 1),
			onSelect: () => void deleteItem(item),
		},
	]

	const openMenu = (e: MouseEvent, entries: MenuEntry[]) => {
		e.preventDefault()
		e.stopPropagation()
		setMenu({ x: e.clientX, y: e.clientY, entries })
	}

	/** ---- Drag and drop (desktop: `LibraryTreeViewTransferHandler`) */

	const canDrop = (target: DropTarget): boolean => {
		const source = dragged.current
		if (source === null) return false
		const { folder } = destination(target)
		const cacheKey = `${source}>${folder}`
		let ok = allowed.current.get(cacheKey)
		if (ok === undefined) {
			ok = libraryApi.canMove(source, folder)
			allowed.current.set(cacheKey, ok)
		}
		return ok
	}

	const startDrag = (e: DragEvent, path: string, key: string | null) => {
		dragged.current = path
		allowed.current.clear()
		e.dataTransfer.setData(TREE_DRAG_TYPE, path)
		// Circuits can also be dropped onto the canvas as sub-circuits
		if (key) e.dataTransfer.setData(dragType(key), '1')
		e.dataTransfer.effectAllowed = key ? 'copyMove' : 'move'
	}

	const endDrag = () => {
		dragged.current = null
		setDrop(null)
		if (expandTimer.current) window.clearTimeout(expandTimer.current.timer)
		expandTimer.current = null
	}

	const dragOver = (e: DragEvent, node: TreeNode, key: string | null) => {
		if (dragged.current === null || !e.dataTransfer.types.includes(TREE_DRAG_TYPE)) return
		e.stopPropagation()
		const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
		const y = (e.clientY - rect.top) / rect.height
		// Folders accept drops inside; their top and (when closed or empty) bottom edges insert next to them
		let position: DropPosition
		if (node.kind !== 'folder') position = y < 0.5 ? 'before' : 'after'
		else if (y < 0.25) position = 'before'
		else if (y > 0.75 && ((key && collapsed[key]) || node.children.length === 0)) position = 'after'
		else position = 'inside'
		const target = { path: node.path, position }
		if (target.path === dragged.current || target.path.startsWith(`${dragged.current}.`) || !canDrop(target)) {
			if (drop) setDrop(null)
			return
		}
		e.preventDefault()
		e.dataTransfer.dropEffect = 'move'
		if (drop?.path !== target.path || drop.position !== target.position) setDrop(target)
		// Hovering over a collapsed folder opens it
		if (node.kind === 'folder' && key && collapsed[key] && position === 'inside') {
			if (expandTimer.current?.key !== key) {
				if (expandTimer.current) window.clearTimeout(expandTimer.current.timer)
				expandTimer.current = { key, timer: window.setTimeout(() => setCollapsed({ ...collapsed, [key]: false }), 600) }
			}
		} else if (expandTimer.current) {
			window.clearTimeout(expandTimer.current.timer)
			expandTimer.current = null
		}
	}

	const dropOn = (e: DragEvent) => {
		const source = dragged.current
		const target = drop
		endDrag()
		if (source === null || !target) return
		e.preventDefault()
		e.stopPropagation()
		const { folder, index } = destination(target)
		libraryApi.move(source, folder, index)
	}

	const rootDragOver = (e: DragEvent) => {
		if (dragged.current === null || !e.dataTransfer.types.includes(TREE_DRAG_TYPE)) return
		const target: DropTarget = { path: '', position: 'inside' }
		if (!canDrop(target)) return
		e.preventDefault()
		e.dataTransfer.dropEffect = 'move'
		if (drop?.path !== '' || drop.position !== 'inside') setDrop(target)
	}

	const dropClass = (path: string) => (drop?.path === path ? ` drop-${drop.position}` : '')

	/** ---- Rendering */

	const renderNodes = (nodes: TreeNode[], depth: number, prefix: string): ReactNode =>
		nodes.map((node) => {
			if (node.kind === 'folder') {
				const key = `${prefix}/${node.name}`
				const isCollapsed = !!collapsed[key]
				return (
					<div key={`folder-${node.path}`} role="group" className="tree-folder">
						<div
							className={`tree-row tree-folder-row${dropClass(node.path)}`}
							style={{ paddingLeft: 4 + depth * 14 }}
							draggable={editing}
							onDragStart={(e) => startDrag(e, node.path, null)}
							onDragEnd={endDrag}
							onDragOver={(e) => dragOver(e, node, key)}
							onDragLeave={(e) => {
								if (!(e.currentTarget as HTMLElement).contains(e.relatedTarget as Node)) setDrop(null)
							}}
							onDrop={dropOn}
							onContextMenu={(e) => openMenu(e, folderMenu(node, key))}
						>
							<button
								type="button"
								className="tree-open"
								aria-expanded={!isCollapsed}
								onClick={() => setCollapsed({ ...collapsed, [key]: !isCollapsed })}
								onDoubleClick={() => editing && void renameFolder(node.path, node.name)}
								title={node.name}
							>
								<ChevronRight size={13} className={`chevron${isCollapsed ? '' : ' open'}`} />
								<span className="tree-icon folder-icon">{isCollapsed ? <Folder size={15} /> : <FolderOpen size={15} />}</span>
								<span className="truncate">{node.name}</span>
								{isCollapsed && node.children.length > 0 && <span className="tree-count">{node.children.length}</span>}
							</button>
							<MenuButton entries={() => folderMenu(node, key)} className="icon-btn small row-menu" title="Folder actions" align="right">
								<MoreHorizontal size={15} />
							</MenuButton>
						</div>
						{!isCollapsed && (
							<>
								{renderNodes(node.children, depth + 1, key)}
								{node.children.length === 0 && (
									<div className="tree-empty" style={{ paddingLeft: 26 + (depth + 1) * 14 }}>
										Empty folder
									</div>
								)}
							</>
						)}
					</div>
				)
			}
			const canvasKey = node.key !== null && !node.isOpen ? node.key : null
			return (
				<div
					key={node.uuid}
					className={`tree-row circuit-item${node.isOpen ? ' open' : ''}${dropClass(node.path)}`}
					style={{ paddingLeft: 4 + depth * 14 + 17 }}
					draggable={editing}
					onDragStart={(e) => startDrag(e, node.path, canvasKey)}
					onDragEnd={endDrag}
					onDragOver={(e) => dragOver(e, node, null)}
					onDragLeave={(e) => {
						if (!(e.currentTarget as HTMLElement).contains(e.relatedTarget as Node)) setDrop(null)
					}}
					onDrop={dropOn}
					onContextMenu={(e) => openMenu(e, itemMenu(node))}
				>
					<button
						type="button"
						className="circuit-open"
						disabled={!editing && !node.isOpen}
						onClick={() => engine.openItem(node.uuid)}
						title={
							node.isOpen
								? 'Currently open'
								: node.kind === 'circuit'
									? `Open “${node.name}” — or drag it onto a circuit to use it as a sub-circuit`
									: `Open “${node.name}”`
						}
					>
						<span className="circuit-icon">
							{node.kind === 'circuit' && node.type === 'Analog' ? <Waves size={15} /> : itemIcons[node.kind]}
						</span>
						<span className="truncate">{node.name}</span>
						{node.isDefault && <Star size={12} className="default-star" aria-label="Opens on start" />}
					</button>
					<MenuButton entries={() => itemMenu(node)} className="icon-btn small row-menu" title="More" align="right">
						<MoreHorizontal size={15} />
					</MenuButton>
				</div>
			)
		})

	return (
		<div className="panel-scroll circuits project-tree">
			{isLibrary && <LibraryBanner tree={tree} />}
			<div className="panel-actions">
				<button type="button" className="btn btn-sm" disabled={!editing} onClick={() => void newCircuitIn('', 'digital')}>
					<Plus size={14} /> New circuit
				</button>
				<div className="panel-actions-group">
					<button type="button" className="icon-btn" title="New folder" disabled={!editing} onClick={() => void newFolder('')}>
						<FolderPlus size={16} />
					</button>
					<button
						type="button"
						className="icon-btn"
						title={anyExpanded ? 'Collapse all folders' : 'Expand all folders'}
						disabled={allFolders.length === 0}
						onClick={() => setAll(anyExpanded)}
					>
						{anyExpanded ? <ChevronsDownUp size={16} /> : <ChevronsUpDown size={16} />}
					</button>
					<MenuButton className="icon-btn" title={isLibrary ? 'Library actions' : 'Project actions'} align="right" entries={rootMenu}>
						<MoreHorizontal size={16} />
					</MenuButton>
				</div>
			</div>
			<div
				className={`circuit-list tree${drop?.path === '' ? ' drop-root' : ''}`}
				role="tree"
				onDragOver={rootDragOver}
				onDragLeave={(e) => {
					if (!(e.currentTarget as HTMLElement).contains(e.relatedTarget as Node)) setDrop(null)
				}}
				onDrop={dropOn}
				onContextMenu={(e) => openMenu(e, rootMenu())}
			>
				{renderNodes(tree.children, 0, rootKey)}
				{tree.children.length === 0 && (
					<div className="tree-placeholder">
						{isLibrary ? 'This library is empty. Create a circuit to reuse it in your projects.' : 'This project is empty.'}
					</div>
				)}
				<div className="tree-drop-tail" />
			</div>
			<p className="hint subtle">
				Drag circuits onto the canvas to use them as sub-circuits. Drag items onto folders to organize them.
			</p>
			{menu && <Menu entries={menu.entries} x={menu.x} y={menu.y} onClose={() => setMenu(null)} />}
			{showLibraries && <LibrariesDialog onClose={() => setShowLibraries(false)} />}
		</div>
	)
}

/** Shown while a user library is open instead of a project. */
function LibraryBanner({ tree }: { tree: Tree }) {
	return (
		<div className="library-banner">
			<BookMarked size={15} />
			<div className="library-banner-text">
				<span className="subtle">Editing library</span>
				<strong className="truncate">{tree.name}</strong>
			</div>
			<button type="button" className="btn btn-sm" onClick={() => void showLibraryManager()} title="Your libraries">
				Libraries
			</button>
			<button type="button" className="icon-btn small" onClick={closeLibrary} title="Close library and return to your project">
				<X size={14} />
			</button>
		</div>
	)
}
