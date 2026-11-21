// User-facing operations on the project tree and on user libraries, with their dialogs.
import { engine } from '../../../core/engine'
import { dialogs } from '../../Dialogs'
import { download, itemNames } from '../../projectActions'
import { libraryApi, type Translations, type TreeNode } from './api'
import { aclToLibrary, libraryToAcl } from './libraryFile'
import { FolderPropertiesDialog } from './FolderPropertiesDialog'

function uniqueName(base: string, existing: string[]): string {
	if (!existing.includes(base)) return base
	let i = 2
	while (existing.includes(`${base} ${i}`)) i++
	return `${base} ${i}`
}

const folderNames = (nodes: TreeNode[] | undefined) =>
	(nodes ?? []).filter((n) => n.kind === 'folder').map((n) => n.name)

/** Children of folder [path] ("" for the root). */
function childrenOf(path: string): TreeNode[] | undefined {
	let nodes = libraryApi.tree()?.children
	if (path === '') return nodes
	for (const index of path.split('.')) {
		const node = nodes?.[Number(index)]
		nodes = node?.kind === 'folder' ? node.children : undefined
	}
	return nodes
}

/** Asks for a name and adds a folder to folder [parent] (desktop: `AddLibraryFolderAction`). */
export async function newFolder(parent: string): Promise<void> {
	const names = folderNames(childrenOf(parent))
	const name = await dialogs.prompt({
		title: 'New folder',
		label: 'Folder name',
		initial: uniqueName('New folder', names),
		confirmLabel: 'Create folder',
		validate: (v) => (names.includes(v) ? 'A folder with this name already exists here.' : null),
	})
	if (name) libraryApi.addFolder(parent, name)
}

/** Renames folder [path] in the current language, keeping other translations. */
export async function renameFolder(path: string, current: string): Promise<void> {
	const parent = path.includes('.') ? path.slice(0, path.lastIndexOf('.')) : ''
	const names = folderNames(childrenOf(parent)).filter((n) => n !== current)
	const name = await dialogs.prompt({
		title: 'Rename folder',
		label: 'Folder name',
		initial: current,
		confirmLabel: 'Rename',
		validate: (v) => (names.includes(v) ? 'A folder with this name already exists here.' : null),
	})
	if (!name || name === current) return
	const info = libraryApi.folder(path)
	libraryApi.renameFolder(path, { ...(info?.name ?? {}), en: name })
}

/** Shows the properties of folder [path] (desktop: `LibraryFolderPropertiesAction`). */
export async function folderProperties(path: string, editable: boolean): Promise<void> {
	const info = libraryApi.folder(path)
	if (!info) return
	const name = await dialogs.custom<Translations>((done) => <FolderPropertiesDialog info={info} editable={editable} done={done} />)
	if (name) libraryApi.renameFolder(path, name)
}

export async function deleteFolder(path: string, name: string): Promise<void> {
	const ok = await dialogs.confirm({
		title: 'Delete folder',
		message: (
			<>
				Delete the folder <strong>{name}</strong>?
			</>
		),
		confirmLabel: 'Delete',
		danger: true,
	})
	if (ok) libraryApi.deleteFolder(path)
}

/** Asks for a name and creates a circuit in folder [folder]. */
export async function newCircuitIn(folder: string, type: 'digital' | 'analog'): Promise<void> {
	const names = itemNames('circuit')
	const name = await dialogs.prompt({
		title: type === 'analog' ? 'New analog circuit' : 'New circuit',
		label: 'Circuit name',
		initial: uniqueName(type === 'analog' ? 'Analog circuit' : 'Circuit', names),
		confirmLabel: 'Create circuit',
		validate: (v) => (names.includes(v) ? 'A circuit with this name already exists.' : null),
	})
	if (name) libraryApi.createCircuit(name, type, folder)
}

const itemUuids = (nodes: TreeNode[] | undefined, into = new Set<string>()): Set<string> => {
	nodes?.forEach((n) => (n.kind === 'folder' ? itemUuids(n.children, into) : into.add(n.uuid)))
	return into
}

/**
 * Runs a "New …" action of the project panel (which adds the item at the top level) and moves the created item
 * into folder [folder].
 */
export async function newItemIn(folder: string, run: () => void | Promise<void>): Promise<void> {
	const before = itemUuids(libraryApi.tree()?.children)
	await run()
	if (folder === '') return
	for (const uuid of itemUuids(libraryApi.tree()?.children)) {
		if (!before.has(uuid)) libraryApi.moveItem(uuid, folder)
	}
}

/** ---- User libraries */

/** Opens user library [uuid] for editing, replacing the open project. */
export function openLibrary(uuid: string) {
	libraryApi.open(uuid)
}

/** Closes the open library and reopens the most recent project, if any. */
export function closeLibrary() {
	libraryApi.close()
	const last = engine.lastProject
	if (last) engine.openProject(last)
}

export async function renameLibrary(uuid: string, current: string): Promise<void> {
	const names = libraryApi.list().filter((l) => l.uuid !== uuid).map((l) => l.name)
	const name = await dialogs.prompt({
		title: 'Rename library',
		label: 'Library name',
		initial: current,
		confirmLabel: 'Rename',
		validate: (v) => (names.includes(v) ? 'A library with this name already exists.' : null),
	})
	if (name && name !== current) libraryApi.rename(uuid, name)
}

export async function deleteLibrary(uuid: string, name: string): Promise<boolean> {
	const users = libraryApi.usedBy(uuid)
	const ok = await dialogs.confirm({
		title: 'Delete library',
		message: (
			<>
				<p className="dialog-p">
					Delete the library <strong>{name}</strong> and all its circuits from this browser? This cannot be undone.
				</p>
				{users.length > 0 && (
					<p className="dialog-p library-warning">
						Used by {users.join(', ')}. Circuits there that use components of this library will no longer work.
					</p>
				)}
			</>
		),
		confirmLabel: 'Delete library',
		danger: true,
	})
	if (ok) libraryApi.delete(uuid)
	return ok
}

const safeFileName = (name: string, fallback: string) => name.replace(/[^\w\- ]+/g, '').trim() || fallback

export function exportLibrary(uuid: string, name: string) {
	const json = libraryApi.exportLibrary(uuid)
	if (!json) return
	download(`${safeFileName(name, 'library')}.acl`, libraryToAcl(json), 'application/zip')
	engine.message('success', '', `Exported “${name}”`)
}

/** Lets the user pick a `.acl` library file and imports it (desktop: `LibraryImportProcess`). */
export function importLibrary(): Promise<void> {
	return new Promise((resolve) => {
		const input = document.createElement('input')
		input.type = 'file'
		input.accept = '.acl'
		input.onchange = async () => {
			const file = input.files?.[0]
			if (!file) return resolve()
			let parsed: { json: string; name: string }
			try {
				parsed = aclToLibrary(new Uint8Array(await file.arrayBuffer()))
			} catch {
				engine.message('error', 'Import failed', 'The file is not an Antares library (.acl).')
				return resolve()
			}
			let result = libraryApi.importLibrary(parsed.json, false)
			if (result.status === 'exists') {
				const replace = await dialogs.confirm({
					title: 'Replace library?',
					message: (
						<>
							The library <strong>{result.name}</strong> already exists in this browser. Importing replaces it with
							the version from the file.
						</>
					),
					confirmLabel: 'Replace',
					danger: true,
				})
				if (!replace) return resolve()
				result = libraryApi.importLibrary(parsed.json, true)
			}
			const messages: Record<typeof result.status, [string, string]> = {
				ok: ['success', `Imported “${result.name}”`],
				exists: ['error', ''],
				open: ['error', `“${result.name}” is open. Close it before replacing it.`],
				nameExists: ['error', `A different library named “${result.name}” already exists. Rename it first.`],
				staleReference: ['error', `“${result.name}” uses a library that doesn't exist in this browser. Import that library first.`],
				invalid: ['error', 'The file is not a valid Antares library.'],
			}
			const [type, text] = messages[result.status]
			if (text) engine.message(type as 'success' | 'error', result.status === 'ok' ? '' : 'Import failed', text)
			resolve()
		}
		input.click()
	})
}
