// Which tab of the bottom panel (issues, probe log) is shown over the canvas, if any.
import { useSyncExternalStore } from 'react'

export type BottomTab = 'issues' | 'log'

let tab: BottomTab | null = null
const listeners = new Set<() => void>()

export const bottomPanel = {
	show(t: BottomTab) {
		tab = t
		listeners.forEach((l) => l())
	},
	hide() {
		tab = null
		listeners.forEach((l) => l())
	},
	toggle(t: BottomTab) {
		if (tab === t) this.hide()
		else this.show(t)
	},
}

const subscribe = (l: () => void) => {
	listeners.add(l)
	return () => {
		listeners.delete(l)
	}
}

export function useBottomPanel(): BottomTab | null {
	return useSyncExternalStore(subscribe, () => tab)
}
