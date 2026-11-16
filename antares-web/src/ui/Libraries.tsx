import { BookMarked, Settings2 } from 'lucide-react'
import { engine } from '../core/engine'
import { DialogFrame } from './Dialogs'
import { useEngineVersion } from './hooks'
import { libraryApi, type LibraryImport } from './features/library/api'
import { showLibraryManager } from './features/library/LibraryManager'

/**
 * Lets the user choose which libraries the open project (or library) uses: the built-in system libraries and
 * the user's own libraries (desktop: `AddLibraryToDesktopAction`, `RemoveLibraryAction`).
 */
export function LibrariesDialog({ onClose }: { onClose: () => void }) {
	useEngineVersion()
	const libraries = libraryApi.imports()
	const system = libraries.filter((l) => l.system)
	const own = libraries.filter((l) => !l.system)
	const noun = engine.state.project?.kind === 'library' ? 'library' : 'project'
	const editable = engine.state.mode === 'edit'
	return (
		<DialogFrame title="Component libraries" onCancel={onClose}>
			<div className="dialog-body">
				<p className="libraries-intro">Choose the libraries whose components are available in this {noun}.</p>
				<h3 className="library-section">Built-in</h3>
				<ul className="library-list">
					{system.map((lib) => (
						<LibraryRow key={lib.uuid} lib={lib} editable={editable} />
					))}
				</ul>
				<h3 className="library-section">
					Your libraries
					<button
						type="button"
						className="link-btn"
						onClick={() => {
							onClose()
							void showLibraryManager()
						}}
					>
						<Settings2 size={13} /> Manage…
					</button>
				</h3>
				{own.length === 0 ? (
					<p className="subtle library-none">
						<BookMarked size={14} /> Create your own libraries to reuse circuits across projects.
					</p>
				) : (
					<ul className="library-list">
						{own.map((lib) => (
							<LibraryRow key={lib.uuid} lib={lib} editable={editable} />
						))}
					</ul>
				)}
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn btn-primary" onClick={onClose}>
					Done
				</button>
			</div>
		</DialogFrame>
	)
}

function LibraryRow({ lib, editable }: { lib: LibraryImport; editable: boolean }) {
	const note = lib.indirect
		? 'Included through another library'
		: lib.cyclic
			? 'Uses this library, so it can’t be added here'
			: lib.description
	const locked = lib.indirect || lib.cyclic || !editable
	return (
		<li>
			<div className="library-text">
				<strong>{lib.name}</strong>
				{note && <span className="subtle">{note}</span>}
			</div>
			<label className="switch" title={lib.indirect || lib.cyclic ? note : undefined}>
				<input
					type="checkbox"
					checked={lib.imported || lib.indirect}
					disabled={locked}
					aria-label={`Use ${lib.name}`}
					onChange={(e) => {
						if (e.target.checked) libraryApi.addImport(lib.uuid)
						else libraryApi.removeImport(lib.uuid)
					}}
				/>
				<span className="switch-track" />
			</label>
		</li>
	)
}
