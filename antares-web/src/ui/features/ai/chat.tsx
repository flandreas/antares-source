// The conversation with the circuit assistant (desktop: AiChatController). The Kotlin `WebAi` builds the requests and
// applies the returned plans; this module sends them to OpenRouter and keeps the transcript shown in the panel.
import { useSyncExternalStore } from 'react'
import { engine } from '../../../core/engine'
import { dialogs } from '../../Dialogs'
import { aiSettings, complete, OpenRouterError, type AiInfo, type ChatMessage } from './openRouter'

export type EntryRole = 'user' | 'assistant' | 'error' | 'info'

export interface ChatEntry {
	id: number
	role: EntryRole
	text: string
}

interface ChatState {
	open: boolean
	busy: boolean
	entries: ChatEntry[]
}

/** The outcome of `WebAi.complete` and `WebAi.applyPending`. */
interface AiResult {
	entries: { role: EntryRole; text: string }[]
	/** A question for the user before a destructive plan is applied. */
	confirm?: string | null
}

const OPEN_STORAGE = 'antares.ai.open'

let state: ChatState = {
	open: (() => {
		try {
			return localStorage.getItem(OPEN_STORAGE) === 'true'
		} catch {
			return false
		}
	})(),
	busy: false,
	entries: [],
}
let nextId = 1
let request: AbortController | null = null
const listeners = new Set<() => void>()

function update(changes: Partial<ChatState>) {
	state = { ...state, ...changes }
	listeners.forEach((l) => l())
}

function add(role: EntryRole, text: string) {
	update({ entries: [...state.entries, { id: nextId++, role, text: text.trim() }] })
}

/** The chat state; components using it re-render on changes. */
export function useChat(): ChatState {
	return useSyncExternalStore(
		(l) => {
			listeners.add(l)
			return () => listeners.delete(l)
		},
		() => state,
	)
}

export const chat = {
	get state() {
		return state
	},

	setOpen(open: boolean) {
		try {
			localStorage.setItem(OPEN_STORAGE, String(open))
		} catch {
			// Storage unavailable
		}
		update({ open })
	},

	toggle() {
		chat.setOpen(!state.open)
	},

	/** Forgets the conversation. The circuit is not changed. */
	clear() {
		request?.abort()
		engine.api.ai.clear()
		update({ entries: [] })
	},

	/** Cancels the running request. */
	stop() {
		request?.abort()
	},

	/** Sends [prompt] to the model and applies the returned plan to the open circuit. */
	async send(prompt: string) {
		const text = prompt.trim()
		if (!text || state.busy) return
		add('user', text)

		const apiKey = aiSettings.apiKey()
		if (!apiKey) {
			add('error', 'No OpenRouter API key configured. Enter your key in the assistant settings.')
			return
		}
		const prepared = JSON.parse(engine.api.ai.prepare(text)) as
			| { messages: ChatMessage[]; circuitId: string }
			| { error: string }
		if ('error' in prepared) {
			add('error', prepared.error)
			return
		}

		const controller = new AbortController()
		request = controller
		update({ busy: true })
		try {
			const raw = await complete(prepared.messages, {
				info: aiInfo(),
				apiKey,
				model: aiSettings.model() || aiInfo().defaultModel,
				signal: controller.signal,
			})
			const result = JSON.parse(engine.api.ai.complete(text, raw, prepared.circuitId)) as AiResult
			result.entries.forEach((e) => add(e.role, e.text))
			if (result.confirm) {
				const confirmed = await dialogs.confirm({
					title: 'AI Assistant',
					message: <span className="ai-confirm">{result.confirm}</span>,
					confirmLabel: 'Apply',
					cancelLabel: 'Don’t apply',
					danger: true,
				})
				const applied = JSON.parse(engine.api.ai.applyPending(confirmed)) as AiResult
				applied.entries.forEach((e) => add(e.role, e.text))
			}
		} catch (e) {
			if (controller.signal.aborted) add('info', 'Request cancelled.')
			else if (e instanceof OpenRouterError) add('error', e.message)
			else {
				console.error(e)
				add('error', 'Unexpected error while talking to the assistant.')
			}
		} finally {
			if (request === controller) request = null
			update({ busy: false })
		}
	},
}

let info: AiInfo | null = null

/** Connection defaults and example prompts from the Kotlin side (the same as on the desktop). */
export function aiInfo(): AiInfo {
	info ??= JSON.parse(engine.api.ai.info()) as AiInfo
	return info
}
