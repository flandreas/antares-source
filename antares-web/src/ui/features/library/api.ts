// Typed access to the Kotlin `WebLibraries` API: the project tree (folders, moving items), project and library
// properties, and user libraries.
import { engine } from '../../../core/engine'
import type { ItemKind } from '../../../core/types'

const api = () => engine.api.libraryManager

/** Translations of a name or description by language code ("en", "de"). */
export type Translations = Record<string, string>

export interface TreeFolder {
	kind: 'folder'
	/** Dot-separated indexes from the root, e.g. "2.0" */
	path: string
	name: string
	children: TreeNode[]
}

export interface TreeItem {
	kind: ItemKind
	path: string
	uuid: string
	/** Palette key of circuits, for placing them as sub-circuits */
	key: string | null
	name: string
	type: string | null
	isDefault: boolean
	isOpen: boolean
}

export type TreeNode = TreeFolder | TreeItem

export interface Tree {
	kind: 'project' | 'library'
	uuid: string
	name: string
	children: TreeNode[]
}

export interface FolderInfo {
	name: Translations
	circuits: number
	items: number
	folders: number
}

export interface Preference {
	key: string
	label: string
	type: 'integer' | 'choice'
	value: string | number | null
	min: number | null
	max: number | null
	choices: { id: string; label: string }[] | null
}

export interface LibraryProperties {
	kind: 'project' | 'library'
	uuid: string
	name: Translations
	description: Translations
	visibility: string
	visibilities: { id: string; label: string; description: string }[]
	preferences: Preference[]
	circuits: number
	items: number
	folders: number
	/** Names of all libraries the project or library uses */
	imports: string[]
}

export interface PropertiesUpdate {
	name: Translations
	description: Translations
	visibility: string
	preferences: Record<string, string | number>
}

export interface UserLibrary {
	uuid: string
	name: string
	description: string
	isOpen: boolean
}

export interface LibraryImport {
	uuid: string
	name: string
	description: string
	system: boolean
	/** Imported directly by the open project or library */
	imported: boolean
	/** Available through another imported library */
	indirect: boolean
	/** Imports the open library, so it can't be imported by it */
	cyclic: boolean
	/** The standard library imported by new projects */
	isDefault: boolean
}

export type ImportStatus = 'ok' | 'exists' | 'nameExists' | 'staleReference' | 'open' | 'invalid'

const parse = <T,>(json: string, fallback: T): T => (json ? (JSON.parse(json) as T) : fallback)

/** Runs a mutation and refreshes the UI. */
function change<T>(run: () => T): T {
	const result = run()
	engine.refresh()
	return result
}

export const libraryApi = {
	tree: (): Tree | null => parse(api().tree(), null),
	addFolder: (parent: string, name: string): string => change(() => api().addFolder(parent, name)),
	folder: (path: string): FolderInfo | null => parse(api().folder(path), null),
	renameFolder: (path: string, name: Translations) => change(() => api().renameFolder(path, JSON.stringify(name))),
	deleteFolder: (path: string) => change(() => api().deleteFolder(path)),
	canMove: (source: string, target: string): boolean => api().canMove(source, target),
	move: (source: string, target: string, index: number) => change(() => api().move(source, target, index)),
	moveItem: (uuid: string, target: string) => change(() => api().moveItem(uuid, target)),
	createCircuit: (name: string, type: 'digital' | 'analog', folder: string): string =>
		change(() => api().createCircuit(name, type, folder)),

	properties: (): LibraryProperties | null => parse(api().properties(), null),
	setProperties: (update: PropertiesUpdate) => change(() => api().setProperties(JSON.stringify(update))),

	list: (): UserLibrary[] => parse(api().list(), []),
	create: (name: string, description: string, importUuid: string): string =>
		change(() => api().create(name, description, importUuid)),
	open: (uuid: string) => change(() => api().open(uuid)),
	close: () => change(() => api().close()),
	rename: (uuid: string, name: string) => change(() => api().rename(uuid, name)),
	usedBy: (uuid: string): string[] => parse(api().usedBy(uuid), []),
	delete: (uuid: string) => change(() => api().delete(uuid)),
	exportLibrary: (uuid: string): string => api().exportLibrary(uuid),
	importLibrary: (json: string, replace: boolean): { status: ImportStatus; uuid: string; name: string } =>
		change(() => parse(api().importLibrary(json, replace), { status: 'invalid' as ImportStatus, uuid: '', name: '' })),

	imports: (): LibraryImport[] => parse(api().imports(), []),
	addImport: (uuid: string) => change(() => api().addImport(uuid)),
	removeImport: (uuid: string) => change(() => api().removeImport(uuid)),
}
