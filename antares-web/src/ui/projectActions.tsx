// User-facing project and circuit operations with their dialogs.
import { engine } from '../core/engine'
import { dialogs } from './Dialogs'
import { showImageExport } from './features/export/ImageExportDialog'
import { acpToProject, projectToAcp } from '../core/projectFile'
import type { ItemKind, ProjectNode } from '../core/types'

function uniqueName(base: string, existing: string[]): string {
	if (!existing.includes(base)) return base
	let i = 2
	while (existing.includes(`${base} ${i}`)) i++
	return `${base} ${i}`
}

export async function newProject(): Promise<void> {
	const names = engine.projects.map((p) => p.name)
	const name = await dialogs.prompt({
		title: 'New project',
		label: 'Project name',
		initial: uniqueName('My project', names),
		confirmLabel: 'Create project',
		validate: (v) => (names.includes(v) ? 'A project with this name already exists.' : null),
	})
	if (!name) return
	const uuid = engine.createProject(name)
	if (uuid) localStorage.setItem('antares.lastProject', uuid)
}

export async function renameProject(): Promise<void> {
	const project = engine.state.project
	if (!project) return
	const names = engine.projects.filter((p) => p.uuid !== project.uuid).map((p) => p.name)
	const name = await dialogs.prompt({
		title: 'Rename project',
		label: 'Project name',
		initial: project.name,
		confirmLabel: 'Rename',
		validate: (v) => (names.includes(v) ? 'A project with this name already exists.' : null),
	})
	if (name && name !== project.name) engine.renameProject(project.uuid, name)
}

export async function deleteProject(uuid: string, name: string): Promise<boolean> {
	const ok = await dialogs.confirm({
		title: 'Delete project',
		message: (
			<>
				Delete <strong>{name}</strong> and all its circuits from this browser? This cannot be undone. Export the
				project first if you want to keep a copy.
			</>
		),
		confirmLabel: 'Delete project',
		danger: true,
	})
	if (ok) engine.deleteProject(uuid)
	return ok
}

/** Lets the browser download [content] as file. */
export function download(filename: string, content: BlobPart, type: string) {
	const url = URL.createObjectURL(new Blob([content], { type }))
	const a = document.createElement('a')
	a.href = url
	a.download = filename
	a.click()
	window.setTimeout(() => URL.revokeObjectURL(url), 1000)
}

export function exportProject() {
	const project = engine.state.project
	if (!project) return
	const json = engine.exportProject()
	if (!json) return
	const safe = project.name.replace(/[^\w\- ]+/g, '').trim() || 'project'
	download(`${safe}.acp`, projectToAcp(json), 'application/zip')
	engine.message('success', '', `Exported “${project.name}”`)
}

/** Lets the user pick an exported project file, imports and opens it. */
export function importProject(): Promise<void> {
	return new Promise((resolve) => {
		const input = document.createElement('input')
		input.type = 'file'
		input.accept = '.acp'
		input.onchange = async () => {
			const file = input.files?.[0]
			if (!file) return resolve()
			try {
				const { json, name, uuid: projectUuid } = acpToProject(new Uint8Array(await file.arrayBuffer()))
				if (engine.projects.some((p) => p.uuid === projectUuid)) {
					const replace = await dialogs.confirm({
						title: 'Replace project?',
						message: (
							<>
								A project <strong>{name}</strong> already exists in this browser. Importing replaces it with
								the version from the file.
							</>
						),
						confirmLabel: 'Replace',
						danger: true,
					})
					if (!replace) return resolve()
				}
				const uuid = engine.importProject(json)
				if (uuid) {
					engine.openProject(uuid)
					engine.message('success', '', `Imported “${name}”`)
				}
			} catch {
				engine.message('error', 'Import failed', 'The file is not an Antares project (.acp).')
			}
			resolve()
		}
		input.click()
	})
}

export async function openSample(uuid: string): Promise<void> {
	try {
		const projectUuid = await engine.importSample(uuid)
		if (projectUuid) engine.openProject(projectUuid)
	} catch (e) {
		engine.message('error', 'Could not open example', String(e))
	}
}

/** Names of all project items of [kind], used to suggest unique names. */
export const itemNames = (kind?: ItemKind) => {
	const names: string[] = []
	const walk = (nodes: ProjectNode[] | null) =>
		nodes?.forEach((n) => (n.kind === 'folder' ? walk(n.children) : (!kind || n.kind === kind) && names.push(n.name)))
	walk(engine.items)
	return names
}

const circuitNames = () => itemNames('circuit')

/** Asks for the name of a new project item, suggesting a unique one based on [base]. */
export async function askNewItemName(title: string, kind: ItemKind, base: string, confirmLabel: string): Promise<string | null> {
	const names = itemNames(kind)
	return dialogs.prompt({
		title,
		label: 'Name',
		initial: uniqueName(base, names),
		confirmLabel,
		validate: (v) => (names.includes(v) ? 'An item with this name already exists.' : null),
	})
}

export async function newCircuit(type: 'digital' | 'analog' = 'digital'): Promise<void> {
	const names = circuitNames()
	const name = await dialogs.prompt({
		title: type === 'analog' ? 'New analog circuit' : 'New circuit',
		label: 'Circuit name',
		initial: uniqueName(type === 'analog' ? 'Analog circuit' : 'Circuit', names),
		confirmLabel: 'Create circuit',
		validate: (v) => (names.includes(v) ? 'A circuit with this name already exists.' : null),
	})
	if (name) engine.createCircuit(name, type)
}

export async function renameItem(item: { uuid: string; name: string; kind: ItemKind }): Promise<void> {
	const names = itemNames(item.kind).filter((n) => n !== item.name)
	const name = await dialogs.prompt({
		title: `Rename ${kindLabel(item.kind)}`,
		label: 'Name',
		initial: item.name,
		confirmLabel: 'Rename',
		validate: (v) => (names.includes(v) ? 'An item with this name already exists.' : null),
	})
	if (name && name !== item.name) engine.renameItem(item.uuid, name)
}

export async function deleteItem(item: { uuid: string; name: string; kind: ItemKind }): Promise<void> {
	const ok = await dialogs.confirm({
		title: `Delete ${kindLabel(item.kind)}`,
		message: (
			<>
				Delete <strong>{item.name}</strong> from this project? This cannot be undone.
			</>
		),
		confirmLabel: 'Delete',
		danger: true,
	})
	if (ok) engine.deleteItem(item.uuid)
}

/** Like [kindLabel], capitalized for headings and badges. */
export function kindTitle(kind: ItemKind): string {
	const label = kindLabel(kind)
	return label.charAt(0).toUpperCase() + label.slice(1)
}

export function kindLabel(kind: ItemKind): string {
	return {
		circuit: 'circuit',
		truthTable: 'truth table',
		fsm: 'state machine',
		memory: 'memory',
		expression: 'expression',
		image: 'image',
	}[kind]
}

/** Exports the shown circuit (or symbol) as PNG, JPEG or SVG image, asking for size and colors. */
export function exportCircuitImage() {
	void showImageExport()
}
