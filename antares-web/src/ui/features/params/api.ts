// Typed access to the Kotlin `WebParams` API (parameter definitions of the open circuit).
import { engine } from '../../../core/engine'

export interface ParamChoice {
	id: string
	label: string
	color: string | null
}

export interface ParamType {
	/** Technical name, e.g. "Long" or "bitWidth" */
	id: string
	label: string
	/** Default value of a new definition */
	initial: string
	/** Selectable values (bit widths, light colors), or null for text input */
	choices: ParamChoice[] | null
}

export interface ParamDefinition {
	name: string
	type: string
	typeLabel: string
	/** The default value as ID, see `ParamType.choices` */
	value: string
	valueLabel: string
	/** Custom name of the semantic, or null */
	semantic: string | null
	semanticLabel: string
	description: string
}

export interface ParamDefinitions {
	editable: boolean
	types: ParamType[]
	semantics: { id: string; label: string }[]
	items: ParamDefinition[]
}

export interface ParamDraft {
	name: string
	type: string
	value: string
	semantic: string
	description: string
}

export const paramsApi = {
	/** Returns `null` if no circuit is open. */
	definitions(): ParamDefinitions | null {
		return JSON.parse(engine.api.hdl.params.definitions()) as ParamDefinitions | null
	},

	/** Adds (empty [originalName]) or replaces a definition; returns an error message or null. */
	save(originalName: string, d: ParamDraft): string | null {
		const error = engine.api.hdl.params.save(originalName, d.name, d.type, d.value, d.semantic, d.description)
		engine.refresh()
		return error || null
	},

	remove(name: string) {
		engine.api.hdl.params.remove(name)
		engine.refresh()
	},
}
