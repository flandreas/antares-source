// Typed access to the Kotlin `WebFsm` feature module.
import { engine } from '../../../core/engine'
import type { PropertySheet } from '../../../core/types'

export type FsmTool = 'select' | 'state' | 'transition'

/** State of the FSM editor, see `WebFsm.state()`. */
export interface FsmEditorState {
	tool: FsmTool
	toolLock: boolean
	/** Instructions of the active tool, e.g. "Drag inside target state" */
	status: string | null
	zoom: number
	states: number
	transitions: number
	selectionCount: number
	editable: boolean
}

export const fsmApi = {
	get kotlin() {
		return engine.api.fsm
	},

	/** Creates a new state machine in the open project and opens it. */
	create(name: string) {
		this.kotlin.create(name)
		engine.refresh()
	},

	state(): FsmEditorState | null {
		return JSON.parse(this.kotlin.state()) as FsmEditorState | null
	},

	properties(): PropertySheet | null {
		return JSON.parse(this.kotlin.properties()) as PropertySheet | null
	},

	setProperty(key: string, value: unknown) {
		this.kotlin.setProperty(key, JSON.stringify(value))
	},

	setTool(tool: FsmTool) {
		this.kotlin.setTool(tool)
		engine.refresh()
	},

	setToolLock(lock: boolean) {
		this.kotlin.setToolLock(lock)
		engine.refresh()
	},
}
