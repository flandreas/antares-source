// Whether the find bar is shown; requests to show it focus its input again and may preset the query.
import { useSyncExternalStore } from 'react'
import type { FindMatch } from './api'

export interface FindPreset {
	text: string
	match: FindMatch
}

let open = false
/** Increments with every request to show the bar, so an open bar can refocus its input. */
let focusRequest = 0
let preset: FindPreset | null = null
const listeners = new Set<() => void>()
const emit = () => listeners.forEach((l) => l())

export const findBar = {
	/** Shows the bar, searching for [query] if given (e.g. a tunnel name from the global tunnels overview). */
	show(query?: FindPreset) {
		open = true
		focusRequest++
		preset = query ?? null
		emit()
	},
	hide() {
		open = false
		emit()
	},
	/** Returns and clears the query passed to the last [show]. */
	takePreset(): FindPreset | null {
		const p = preset
		preset = null
		return p
	},
}

const subscribe = (l: () => void) => {
	listeners.add(l)
	return () => {
		listeners.delete(l)
	}
}

/** Returns the request counter while the find bar is open, otherwise 0. */
export function useFindBar(): number {
	return useSyncExternalStore(subscribe, () => (open ? focusRequest : 0))
}
