// Typed access to the Kotlin `WebFind` API (find in the displayed circuit or symbol).
import { engine } from '../../../core/engine'

export type FindMatch = 'contains' | 'startsWith' | 'entireWord'

export interface FindResult {
	count: number
	/** One-based index of the selected match, or 0 while all matches are selected */
	current: number
}

export const findApi = {
	/** Selects all components matching [text] (by ID, type or label). */
	search(text: string, match: FindMatch, ignoreCase: boolean): FindResult {
		return JSON.parse(engine.api.find.search(text, match, ignoreCase)) as FindResult
	},

	/** Selects the next or previous match and brings it into view. */
	navigate(forward: boolean): FindResult {
		return JSON.parse(engine.api.find.navigate(forward)) as FindResult
	},

	reset() {
		engine.api.find.reset()
	},
}
