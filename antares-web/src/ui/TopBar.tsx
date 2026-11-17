import {
	ChevronDown,
	Download,
	FilePlus2,
	FolderOpen,
	Image,
	Keyboard,
	Moon,
	Pencil,
	Play,
	Redo2,
	Settings,
	Sun,
	Trash2,
	Undo2,
	Upload,
} from 'lucide-react'
import { engine } from '../core/engine'
import type { EditorState } from '../core/types'
import { command } from './commands'
import { formatShortcut } from './hooks'
import { MenuButton, type MenuEntry } from './Menu'
import { MenuBar, type MenuBarActions } from './MenuBar'
import { projectMenuActions } from './features/registry'
import { closeLibrary, exportLibrary, renameLibrary } from './features/library/actions'
import {
	deleteProject,
	exportCircuitImage,
	exportProject,
	importProject,
	newProject,
	renameItem,
	renameProject,
	kindTitle,
} from './projectActions'

interface TopBarProps {
	state: EditorState
	dark: boolean
	onToggleTheme: () => void
	onShowShortcuts: () => void
	onShowProjects: () => void
	onShowPreferences: () => void
	menuActions: MenuBarActions
}

const SAVE_LABELS = { saved: 'Saved', saving: 'Saving…', failed: 'Not saved' } as const

export function TopBar({ state, dark, onToggleTheme, onShowShortcuts, onShowProjects, onShowPreferences, menuActions }: TopBarProps) {
	const simulating = state.mode === 'simulate'
	const project = state.project

	const isLibrary = project?.kind === 'library'

	const projectMenu = (): MenuEntry[] => {
		const others = engine.projects.filter((p) => p.uuid !== project?.uuid)
		return [
			{ label: 'New project…', icon: <FilePlus2 size={15} />, onSelect: () => void newProject() },
			{ label: 'Open project…', icon: <FolderOpen size={15} />, onSelect: onShowProjects },
			...(others.length > 0
				? ([{ heading: 'Recent' }, ...others.slice(0, 6).map((p) => ({ label: p.name, onSelect: () => engine.openProject(p.uuid) }))] as MenuEntry[])
				: []),
			'separator',
			...(isLibrary && project
				? ([
						{ label: 'Rename library…', icon: <Pencil size={15} />, onSelect: () => void renameLibrary(project.uuid, project.name) },
						{ label: 'Export library (.acl)', icon: <Download size={15} />, onSelect: () => exportLibrary(project.uuid, project.name) },
						{ label: 'Close library', icon: <FolderOpen size={15} />, onSelect: closeLibrary },
					] as MenuEntry[])
				: ([
						{ label: 'Rename project…', icon: <Pencil size={15} />, disabled: !project, onSelect: () => void renameProject() },
						{ label: 'Export project (.acp)', icon: <Download size={15} />, disabled: !project, onSelect: exportProject },
					] as MenuEntry[])),
			{ label: 'Import project (.acp)…', icon: <Upload size={15} />, onSelect: () => void importProject() },
			{ label: 'Export circuit image…', icon: <Image size={15} />, disabled: !state.circuit, onSelect: exportCircuitImage },
			...projectMenuActions.map((a): MenuEntry => ({ label: a.label, icon: a.icon, disabled: !a.enabled(state), onSelect: () => void a.run() })),
			'separator',
			...(isLibrary ? [] : ([{
				label: 'Delete project…',
				icon: <Trash2 size={15} />,
				danger: true,
				disabled: !project,
				onSelect: async () => {
					if (project && (await deleteProject(project.uuid, project.name))) onShowProjects()
				},
			}] as MenuEntry[])),
		]
	}

	const undo = command('undo')
	const redo = command('redo')

	return (
		<header className="topbar">
			<div className="topbar-left">
				<button type="button" className="brand" onClick={onShowProjects} title="All projects">
					<AntaresMark />
					<span>Antares</span>
				</button>
				<MenuBar state={state} actions={menuActions} />
				<span className="topbar-divider" />
				<MenuButton entries={projectMenu} className="btn btn-ghost project-btn" title="Project">
					<span className="truncate">{project?.name ?? 'No project'}</span>
					<ChevronDown size={14} />
				</MenuButton>
				{(state.circuit || state.document) && (
					<>
						<span className="crumb-sep">/</span>
						<button
							type="button"
							className="btn btn-ghost circuit-name"
							title="Rename"
							disabled={simulating}
							onClick={() => {
								const open = state.circuit
									? { uuid: state.circuit.uuid, name: state.circuit.name, kind: 'circuit' as const }
									: state.document
								if (open) void renameItem(open)
							}}
						>
							<span className="truncate">{state.circuit?.name ?? state.document?.name}</span>
						</button>
						{state.circuit?.type === 'Analog' && <span className="badge">Analog</span>}
						{state.document && <span className="badge">{kindTitle(state.document.kind)}</span>}
					</>
				)}
			</div>

			<div className="topbar-center">
				<div className="segmented" role="tablist" aria-label="Mode">
					<button
						type="button"
						role="tab"
						aria-selected={!simulating}
						className={!simulating ? 'selected' : ''}
						onClick={() => engine.setSimulating(false)}
						disabled={!state.circuit}
					>
						<Pencil size={14} /> <span className="seg-label">Edit</span>
					</button>
					<button
						type="button"
						role="tab"
						aria-selected={simulating}
						className={simulating ? 'selected run' : ''}
						onClick={() => engine.setSimulating(true)}
						disabled={!state.circuit}
						title={`Simulate (${formatShortcut('Mod+Enter')})`}
					>
						<Play size={14} /> <span className="seg-label">Simulate</span>
					</button>
				</div>
			</div>

			<div className="topbar-right">
				{state.project && (
					<span
						className={`save-status ${state.saveStatus}`}
						role="status"
						title={state.saveStatus === 'failed' ? 'The browser refused to store your changes. Export the project to keep a copy.' : undefined}
					>
						{SAVE_LABELS[state.saveStatus]}
					</span>
				)}
				<button
					type="button"
					className="icon-btn"
					disabled={!undo.enabled(state)}
					onClick={undo.run}
					title={`Undo${state.undoLabel ? ` ${state.undoLabel}` : ''} (${formatShortcut('Mod+Z')})`}
				>
					<Undo2 size={17} />
				</button>
				<button
					type="button"
					className="icon-btn"
					disabled={!redo.enabled(state)}
					onClick={redo.run}
					title={`Redo${state.redoLabel ? ` ${state.redoLabel}` : ''} (${formatShortcut('Mod+Shift+Z')})`}
				>
					<Redo2 size={17} />
				</button>
				<span className="topbar-divider" />
				<button type="button" className="icon-btn optional" onClick={onShowShortcuts} title="Keyboard shortcuts (?)">
					<Keyboard size={17} />
				</button>
				<button type="button" className="icon-btn optional" onClick={onToggleTheme} title={dark ? 'Light theme' : 'Dark theme'}>
					{dark ? <Sun size={17} /> : <Moon size={17} />}
				</button>
				<button type="button" className="icon-btn" onClick={onShowPreferences} title="Preferences">
					<Settings size={17} />
				</button>
			</div>
		</header>
	)
}

/** The Antares mark: a bright star with a soft halo. */
export function AntaresMark({ size = 20 }: { size?: number }) {
	return (
		<svg width={size} height={size} viewBox="0 0 24 24" aria-hidden="true" className="mark">
			<circle cx="12" cy="12" r="10" fill="var(--accent)" opacity="0.16" />
			<path d="M12 3.5 13.9 10.1 20.5 12 13.9 13.9 12 20.5 10.1 13.9 3.5 12 10.1 10.1Z" fill="var(--accent)" />
		</svg>
	)
}
