import { useEffect, useState } from 'react'
import { AlertTriangle } from 'lucide-react'
import { engine } from '../core/engine'
import type { EditorState } from '../core/types'
import { CanvasArea } from './CanvasArea'
import { commands, matches } from './commands'
import { DialogFrame, DialogHost, dialogs } from './Dialogs'
import { Inspector } from './Inspector'
import { PreferencesDialog, storedConnectMethod } from './Preferences'
import { ProjectsScreen } from './Projects'
import { ShortcutsDialog } from './Shortcuts'
import { Sidebar } from './Sidebar'
import { Toasts } from './Toasts'
import { dialogViews } from './features/registry'
import { showIssues } from './features/analysis'
import { AntaresMark, TopBar } from './TopBar'
import { isTypingTarget, useEngineVersion, useStoredState } from './hooks'
import { kindLabel } from './projectActions'

engine.confirmHandler = (question) =>
	dialogs.confirm({ title: 'Antares', message: question, confirmLabel: 'Yes', cancelLabel: 'No' })

const prefersDark = () => window.matchMedia('(prefers-color-scheme: dark)').matches

export function App() {
	const [dark, setDark] = useStoredState('antares.dark', prefersDark())
	const [status, setStatus] = useState<'loading' | 'ready' | 'failed'>('loading')
	const [error, setError] = useState('')

	useEffect(() => {
		document.documentElement.dataset.theme = dark ? 'dark' : 'light'
	}, [dark])

	useEffect(() => {
		engine.start(dark).then(
			() => {
				engine.setConnectMethod(storedConnectMethod())
				setStatus('ready')
			},
			(e: unknown) => {
				console.error(e)
				setError(e instanceof Error ? e.message : String(e))
				setStatus('failed')
			},
		)
		// Theme changes after start are applied through setDark
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [])

	if (status === 'loading') return <Splash />
	if (status === 'failed') return <Failure message={error} />
	return (
		<Editor
			dark={dark}
			onToggleTheme={() => {
				// Switch the canvas theme first, so thumbnails re-render in the new theme
				engine.setDark(!dark)
				document.documentElement.dataset.theme = dark ? 'light' : 'dark'
				setDark(!dark)
			}}
		/>
	)
}

function Splash() {
	return (
		<div className="splash">
			<AntaresMark size={40} />
			<div className="splash-title">Antares</div>
			<div className="splash-bar">
				<span />
			</div>
		</div>
	)
}

function Failure({ message }: { message: string }) {
	return (
		<div className="splash">
			<AlertTriangle size={32} color="var(--danger)" />
			<div className="splash-title">The editor could not start</div>
			<p className="subtle">{message}</p>
			<button type="button" className="btn" onClick={() => location.reload()}>
				Reload
			</button>
		</div>
	)
}

function Editor({ dark, onToggleTheme }: { dark: boolean; onToggleTheme: () => void }) {
	useEngineVersion()
	const state = engine.state
	const [showProjects, setShowProjects] = useState(false)
	const [showShortcuts, setShowShortcuts] = useState(false)
	const [showPreferences, setShowPreferences] = useState(false)
	// While the last project is being reopened, the project picker must not flash up
	const [restoring, setRestoring] = useState(() => engine.lastProject !== null)
	const [connectMethod, setConnectMethod] = useState(storedConnectMethod)
	const [sidebar, setSidebar] = useStoredState('antares.showSidebar', true)
	const [inspector, setInspector] = useStoredState('antares.showInspector', true)

	// Reopen the most recent project. Runs after CanvasArea has bound the canvases (child effects run first).
	useEffect(() => {
		const last = engine.lastProject
		if (last) engine.openProject(last)
		setRestoring(false)
	}, [])

	useEffect(() => {
		const onKey = (e: KeyboardEvent) => {
			if (isTypingTarget(e.target) || document.querySelector('.dialog-backdrop')) return
			if (e.key === '?' && !e.metaKey && !e.ctrlKey) {
				e.preventDefault()
				setShowShortcuts(true)
				return
			}
			if (e.key === 'Escape' && engine.state.breadcrumbs.length > 1) {
				e.preventDefault()
				engine.navigateTo(engine.state.breadcrumbs.length - 2)
				return
			}
			if (matches(e, 'Mod+\\')) {
				e.preventDefault()
				setSidebar(!sidebar)
				return
			}
			// Clipboard shortcuts are handled by the copy/cut/paste events below
			if (matches(e, 'Mod+C') || matches(e, 'Mod+X') || matches(e, 'Mod+V')) return
			for (const c of commands) {
				const shortcuts = [c.shortcut, ...(c.altShortcuts ?? [])].filter(Boolean) as string[]
				if (shortcuts.some((s) => matches(e, s))) {
					e.preventDefault()
					if (c.enabled(engine.state)) c.run()
					return
				}
			}
		}
		const onCopy = (e: ClipboardEvent) => {
			if (isTypingTarget(e.target) || window.getSelection()?.toString()) return
			const action = e.type === 'cut' ? 'cut' : 'copy'
			if (engine.state.mode === 'edit' && engine.state.actions[action]) {
				e.preventDefault()
				engine.perform(action)
			}
		}
		const onPaste = (e: ClipboardEvent) => {
			if (isTypingTarget(e.target) || engine.state.mode !== 'edit') return
			const text = e.clipboardData?.getData('text/plain') ?? ''
			e.preventDefault()
			// Content copied in another tab or the desktop app arrives as XML through the system clipboard
			if (text.trimStart().startsWith('<')) engine.pasteText(text)
			else engine.perform('paste')
		}
		window.addEventListener('keydown', onKey)
		document.addEventListener('copy', onCopy)
		document.addEventListener('cut', onCopy)
		document.addEventListener('paste', onPaste)
		return () => {
			window.removeEventListener('keydown', onKey)
			document.removeEventListener('copy', onCopy)
			document.removeEventListener('cut', onCopy)
			document.removeEventListener('paste', onPaste)
		}
	}, [sidebar, setSidebar])

	// Warn before leaving while changes are still being written
	useEffect(() => {
		const onUnload = (e: BeforeUnloadEvent) => {
			engine.save()
			if (engine.state.saving) e.preventDefault()
		}
		window.addEventListener('beforeunload', onUnload)
		return () => window.removeEventListener('beforeunload', onUnload)
	}, [])

	const noProject = !state.project && !restoring

	return (
		<div className={`app${sidebar ? '' : ' no-sidebar'}${inspector ? '' : ' no-inspector'}`}>
			<TopBar
				state={state}
				dark={dark}
				onToggleTheme={onToggleTheme}
				onShowShortcuts={() => setShowShortcuts(true)}
				onShowProjects={() => setShowProjects(true)}
				onShowPreferences={() => setShowPreferences(true)}
				menuActions={{
					toggleSidebar: () => setSidebar(!sidebar),
					toggleInspector: () => setInspector(!inspector),
					toggleTheme: onToggleTheme,
					showShortcuts: () => setShowShortcuts(true),
					showPreferences: () => setShowPreferences(true),
					showAbout: () => void showAbout(),
				}}
			/>
			{sidebar && <Sidebar state={state} />}
			<CanvasArea state={state} />
			{inspector && <Inspector state={state} />}
			<StatusBar
				sidebar={sidebar}
				inspector={inspector}
				onToggleSidebar={() => setSidebar(!sidebar)}
				onToggleInspector={() => setInspector(!inspector)}
			/>
			{(showProjects || noProject) && <ProjectsScreen closable={!noProject} onClose={() => setShowProjects(false)} />}
			{showShortcuts && <ShortcutsDialog onClose={() => setShowShortcuts(false)} />}
			{showPreferences && (
				<PreferencesDialog
					dark={dark}
					onToggleTheme={onToggleTheme}
					onClose={() => setShowPreferences(false)}
					connectMethod={connectMethod}
					onConnectMethod={setConnectMethod}
				/>
			)}
			{state.dialog && <FeatureDialog state={state} />}
			<DialogHost />
			<Toasts />
		</div>
	)
}

/** Shows the document that the editor opened in a dialog, e.g. memory contents. */
function FeatureDialog({ state }: { state: EditorState }) {
	const View = state.dialog ? dialogViews[state.dialog.kind] : undefined
	if (!View) return null
	return <View state={state} onClose={() => engine.closeDialog()} />
}

/** Shows version and background information. */
function showAbout() {
	return dialogs.custom<null>((done) => (
		<DialogFrame title="About Antares" onCancel={() => done(null)}>
			<div className="dialog-body about">
				<AntaresMark size={36} />
				<p>
					<strong>Antares</strong> {__APP_VERSION__}
					<br />A digital and analog circuit editor and simulator for learning how computers work.
				</p>
				<p className="subtle">
					This web editor runs the same Kotlin core as Antares Desktop. Projects are stored in this browser; export them
					as .acp files to open them in the desktop application.
				</p>
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn btn-primary" onClick={() => done(null)}>
					Close
				</button>
			</div>
		</DialogFrame>
	))
}

function StatusBar({
	sidebar,
	inspector,
	onToggleSidebar,
	onToggleInspector,
}: {
	sidebar: boolean
	inspector: boolean
	onToggleSidebar: () => void
	onToggleInspector: () => void
}) {
	const state = engine.state
	const hint = (() => {
		if (state.document) return `Editing ${kindLabel(state.document.kind)} “${state.document.name}” · changes are saved automatically`
		if (!state.circuit) return 'No circuit open'
		if (state.mode === 'simulate')
			return state.paused
				? 'Simulation paused'
				: 'Simulating · click switches and buttons · double-click a sub-circuit to look inside'
		if (state.breadcrumbs.length > 1) return 'Viewing a sub-circuit — press Esc to go back'
		if (state.view === 'symbol') return 'Editing the symbol used when this circuit is placed in other circuits'
		if (state.selectionCount > 1) return `${state.selectionCount} components selected`
		if (state.selectionCount === 1) return 'R to rotate · Delete to remove · drag to move'
		return 'Drag components from the palette · drag from a pin to wire'
	})()
	return (
		<footer className="statusbar">
			<button type="button" className={`status-toggle${sidebar ? ' on' : ''}`} onClick={onToggleSidebar} title="Toggle sidebar">
				Sidebar
			</button>
			<span className="status-text truncate">{hint}</span>
			<span className="status-spacer" />
			{state.issues > 0 && (
				<button type="button" className="status-issues" title="Show the issues" onClick={showIssues}>
					<AlertTriangle size={12} /> {state.issues} {state.issues === 1 ? 'issue' : 'issues'}
				</button>
			)}
			{state.circuit && <span className="status-text">{Math.round((state.zoom * 100) / (window.devicePixelRatio || 1))}%</span>}
			<button
				type="button"
				className={`status-toggle${inspector ? ' on' : ''}`}
				onClick={onToggleInspector}
				title="Toggle inspector"
			>
				Inspector
			</button>
		</footer>
	)
}
