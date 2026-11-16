import { useEffect, useState, useSyncExternalStore } from 'react'
import { engine } from '../core/engine'

/** Re-renders the component whenever the editor reports a change. */
export function useEngineVersion(): number {
	return useSyncExternalStore(engine.subscribe, () => engine.version)
}

/** Whether the platform uses ⌘ (macOS) rather than Ctrl as primary modifier. */
export const isMac = /Mac|iPhone|iPad/.test(navigator.platform)

/** Formats a shortcut like "Mod+Shift+Z" for display. */
export function formatShortcut(shortcut: string): string {
	return shortcut
		.split('+')
		.map((part) => {
			if (part === 'Mod') return isMac ? '⌘' : 'Ctrl'
			if (part === 'Shift') return isMac ? '⇧' : 'Shift'
			if (part === 'Alt') return isMac ? '⌥' : 'Alt'
			if (part === 'Enter') return isMac ? '↩' : 'Enter'
			if (part === 'Backspace') return isMac ? '⌫' : 'Backspace'
			return part
		})
		.join(isMac ? '' : '+')
}

/** Persists a small piece of UI state in localStorage. */
export function useStoredState<T>(key: string, initial: T): [T, (value: T) => void] {
	const [value, setValue] = useState<T>(() => {
		try {
			const stored = localStorage.getItem(key)
			return stored === null ? initial : (JSON.parse(stored) as T)
		} catch {
			return initial
		}
	})
	useEffect(() => {
		try {
			localStorage.setItem(key, JSON.stringify(value))
		} catch {
			// Storage unavailable
		}
	}, [key, value])
	return [value, setValue]
}

/** Returns whether the user is typing into a form field, where editor shortcuts must not apply. */
export function isTypingTarget(target: EventTarget | null): boolean {
	if (!(target instanceof HTMLElement)) return false
	return target.isContentEditable || ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)
}

/** The current UI theme ("light" or "dark"), updated when `data-theme` on the root element changes. */
export function useThemeName(): string {
	return useSyncExternalStore(
		(onChange) => {
			const observer = new MutationObserver(onChange)
			observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
			return () => observer.disconnect()
		},
		() => document.documentElement.dataset.theme ?? 'light',
	)
}
