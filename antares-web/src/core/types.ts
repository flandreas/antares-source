// Shapes of the JSON data returned by the Kotlin `AntaresWebEditor`.

export type Mode = 'edit' | 'simulate'

export type ActionName =
	| 'undo'
	| 'redo'
	| 'cut'
	| 'copy'
	| 'paste'
	| 'duplicate'
	| 'delete'
	| 'selectAll'
	| 'rotateLeft'
	| 'rotateRight'
	| 'toFront'
	| 'toBack'
	| 'group'
	| 'ungroup'

export interface EditorState {
	mode: Mode
	paused: boolean
	speed: number
	speedCategory: string
	view: 'circuit' | 'symbol'
	canUndo: boolean
	canRedo: boolean
	undoLabel: string | null
	redoLabel: string | null
	saving: boolean
	/** The open project, or the user library being edited (kind 'library') */
	project: { uuid: string; name: string; kind: 'project' | 'library' } | null
	circuit: { uuid: string; name: string; type: string; editable: boolean } | null
	breadcrumbs: string[]
	editable: boolean
	selectionCount: number
	componentCount: number
	zoom: number
	actions: Record<ActionName, boolean>
	issues: number
	/** Sub-circuits opened in their own views next to the circuit */
	sideViews: SideView[]
	/** Whether the selection is a sub-circuit that can be opened in its own view */
	canOpenInSideView: boolean
	/** The non-circuit item shown in the main area, if any */
	document: OpenDocument | null
	/** A document shown in a dialog, e.g. memory contents or sub-circuit documentation */
	dialog: { kind: 'memoryContents' | 'documentation' } | null
}

export interface PaletteFolder {
	kind: 'folder'
	name: string
	children: PaletteNode[]
}

export interface PaletteItem {
	kind: 'component' | 'circuit'
	key: string
	name: string
	type: string
}

export type PaletteNode = PaletteFolder | PaletteItem

/** Kinds of project items. */
export type ItemKind = 'circuit' | 'truthTable' | 'fsm' | 'memory' | 'expression' | 'image'

export interface ProjectItem {
	kind: ItemKind
	/** Palette key for items that can be placed in circuits (circuits), otherwise null */
	key: string | null
	uuid: string
	name: string
	/** Graph type for circuits ("Digital" or "Analog") */
	type: string | null
	isDefault: boolean
	isOpen: boolean
}

export interface ProjectFolder {
	kind: 'folder'
	name: string
	children: ProjectNode[]
}

export type ProjectNode = ProjectItem | ProjectFolder

export interface SideView {
	id: number
	title: string
	/** CSS color linking the view to its sub-circuit in the circuit */
	color: string | null
}

/** A non-circuit project item shown in the main area. */
export interface OpenDocument {
	kind: ItemKind
	uuid: string
	name: string
	editable: boolean
}

export interface ProjectEntry {
	uuid: string
	name: string
}

export type PropertyType = 'text' | 'multiline' | 'boolean' | 'integer' | 'number' | 'choice' | 'color' | 'readonly'

export interface PropertyChoice {
	id: string
	label: string
	/** CSS color for color choices */
	color?: string | null
}

export interface PropertyDescriptor {
	key: string
	label: string
	type: PropertyType
	group: string
	value: string | number | boolean | null
	choices: PropertyChoice[] | null
	min: number | null
	max: number | null
}

export interface PropertySheet {
	title: string
	subtitle: string
	isCircuit: boolean
	editable: boolean
	properties: PropertyDescriptor[]
}

export interface Message {
	id: number
	type: 'info' | 'warning' | 'error' | 'success'
	title: string
	text: string
}
