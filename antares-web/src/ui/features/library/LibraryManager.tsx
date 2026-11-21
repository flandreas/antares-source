import { useState } from 'react'
import { BookMarked, Download, MoreHorizontal, Pencil, Plus, SquarePen, Trash2, Upload } from 'lucide-react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { useEngineVersion } from '../../hooks'
import { MenuButton } from '../../Menu'
import { libraryApi } from './api'
import { deleteLibrary, exportLibrary, importLibrary, openLibrary, renameLibrary } from './actions'

/** Shows the user's libraries (desktop: `ShowLibrariesDialogAction` with `LibraryPersistencePanel`). */
export function showLibraryManager(): Promise<null> {
	return dialogs.custom<null>((done) => <LibraryManager onClose={() => done(null)} />)
}

/**
 * Lists the user's own libraries: reusable circuits that projects can use like the built-in components.
 * Libraries can be created, edited (opened like a project), renamed, deleted, exported and imported as `.acl` files.
 */
function LibraryManager({ onClose }: { onClose: () => void }) {
	useEngineVersion()
	const libraries = libraryApi.list()
	const simulating = engine.state.mode === 'simulate'

	const create = async () => {
		const uuid = await createLibrary()
		if (uuid) {
			openLibrary(uuid)
			onClose()
		}
	}

	return (
		<DialogFrame title="Your libraries" onCancel={onClose} wide>
			<div className="dialog-body library-manager">
				<p className="subtle library-intro">
					Libraries hold circuits you want to reuse. Add a library to a project under{' '}
					<em>Component libraries</em> to place its circuits like built-in components. Libraries are stored in this
					browser and can be exported as .acl files for Antares Desktop.
				</p>
				<div className="library-manager-actions">
					<button type="button" className="btn btn-primary" onClick={() => void create()}>
						<Plus size={15} /> New library
					</button>
					<button type="button" className="btn" onClick={() => void importLibrary()}>
						<Upload size={15} /> Import library file
					</button>
				</div>
				{libraries.length === 0 ? (
					<div className="library-empty">
						<BookMarked size={22} />
						<p>No libraries yet.</p>
					</div>
				) : (
					<ul className="user-library-list">
						{libraries.map((lib) => (
							<li key={lib.uuid} className={lib.isOpen ? 'current' : ''}>
								<BookMarked size={16} className="user-library-icon" />
								<div className="library-text">
									<strong className="truncate">{lib.name}</strong>
									{lib.description && <span className="subtle clamp-2">{lib.description}</span>}
								</div>
								{lib.isOpen ? (
									<span className="pill">Open</span>
								) : (
									<button
										type="button"
										className="btn btn-sm"
										disabled={simulating}
										onClick={() => {
											openLibrary(lib.uuid)
											onClose()
										}}
										title="Open the library to edit its circuits"
									>
										<SquarePen size={14} /> Edit
									</button>
								)}
								<MenuButton
									className="icon-btn small"
									title="More"
									align="right"
									entries={() => [
										{ label: 'Rename…', icon: <Pencil size={15} />, onSelect: () => void renameLibrary(lib.uuid, lib.name) },
										{ label: 'Export (.acl)', icon: <Download size={15} />, onSelect: () => exportLibrary(lib.uuid, lib.name) },
										'separator',
										{
											label: 'Delete…',
											icon: <Trash2 size={15} />,
											danger: true,
											disabled: lib.isOpen,
											onSelect: () => void deleteLibrary(lib.uuid, lib.name),
										},
									]}
								>
									<MoreHorizontal size={15} />
								</MenuButton>
							</li>
						))}
					</ul>
				)}
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn" onClick={onClose}>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}

/** Asks for name, description and base library of a new library (desktop: `CreateLibraryPanel`). Returns its UUID. */
export async function createLibrary(): Promise<string | null> {
	const info = await dialogs.custom<{ name: string; description: string; base: string }>((done) => (
		<CreateLibraryDialog done={done} />
	))
	if (!info) return null
	return libraryApi.create(info.name, info.description, info.base) || null
}

function CreateLibraryDialog({ done }: { done: (info: { name: string; description: string; base: string } | null) => void }) {
	const existing = libraryApi.list().map((l) => l.name)
	const bases = libraryApi.imports()
	const [name, setName] = useState(() => {
		let candidate = 'My library'
		for (let i = 2; existing.includes(candidate); i++) candidate = `My library ${i}`
		return candidate
	})
	const [description, setDescription] = useState('')
	const [base, setBase] = useState(() => bases.find((b) => b.isDefault)?.uuid ?? '')
	const [touched, setTouched] = useState(false)
	const error = !name.trim() ? 'Please enter a name.' : existing.includes(name.trim()) ? 'A library with this name already exists.' : null
	return (
		<DialogFrame title="New library" onCancel={() => done(null)}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					setTouched(true)
					if (!error) done({ name: name.trim(), description, base })
				}}
			>
				<div className="dialog-body library-form">
					<label className="field">
						<span className="field-label">Name</span>
						<input className="input" value={name} autoFocus onFocus={(e) => e.target.select()} onChange={(e) => setName(e.target.value)} />
						{touched && error && <span className="field-error">{error}</span>}
					</label>
					<label className="field">
						<span className="field-label">Description</span>
						<textarea className="input" rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
					</label>
					<label className="field">
						<span className="field-label">Components from</span>
						<select className="input select" value={base} onChange={(e) => setBase(e.target.value)}>
							<option value="">None</option>
							{bases.map((b) => (
								<option key={b.uuid} value={b.uuid}>
									{b.name}
								</option>
							))}
						</select>
						<span className="field-hint">The library whose components the new library's circuits can use.</span>
					</label>
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={() => done(null)}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary">
						Create and edit
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}
