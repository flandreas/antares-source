// The OpenRouter chat completions call of the circuit assistant (desktop: OpenRouterClient). The API key is kept
// only in this browser's localStorage and sent only to OpenRouter.

export interface ChatMessage {
	role: 'system' | 'user' | 'assistant'
	content: string
}

/** Connection defaults shared with the desktop, see `WebAi.info()`. */
export interface AiInfo {
	defaultModel: string
	endpoint: string
	referer: string
	title: string
	examples: { name: string; prompt: string }[]
}

/** A failure of the request with a message for the user. */
export class OpenRouterError extends Error {}

/** Generous timeout as on the desktop: model answers can take a while, and the user can stop the request. */
const TIMEOUT_MS = 180_000

const KEY_STORAGE = 'antares.ai.apiKey'
const MODEL_STORAGE = 'antares.ai.model'

function read(key: string): string {
	try {
		return localStorage.getItem(key) ?? ''
	} catch {
		return ''
	}
}

function write(key: string, value: string) {
	try {
		if (value) localStorage.setItem(key, value)
		else localStorage.removeItem(key)
	} catch {
		// Storage unavailable
	}
}

export const aiSettings = {
	apiKey: () => read(KEY_STORAGE).trim(),
	setApiKey: (value: string) => write(KEY_STORAGE, value.trim()),
	/** The configured model, empty for the default. */
	model: () => read(MODEL_STORAGE).trim(),
	setModel: (value: string) => write(MODEL_STORAGE, value.trim()),
}

interface ChatResponse {
	choices?: { message?: { content?: string | null } | null }[]
	error?: { message?: string } | null
}

/**
 * Sends [messages] to the model and returns the raw content of the first choice.
 * Rejects with an [OpenRouterError] on any failure, or with an `AbortError` if [signal] aborted the request.
 */
export async function complete(
	messages: ChatMessage[],
	options: { info: AiInfo; apiKey: string; model: string; signal: AbortSignal },
): Promise<string> {
	const { info, apiKey, model, signal } = options
	const controller = new AbortController()
	const abort = () => controller.abort()
	signal.addEventListener('abort', abort)
	let timedOut = false
	const timer = window.setTimeout(() => {
		timedOut = true
		controller.abort()
	}, TIMEOUT_MS)

	try {
		let response: Response
		try {
			response = await fetch(info.endpoint, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${apiKey}`,
					'HTTP-Referer': info.referer,
					'X-Title': info.title,
				},
				// No temperature: the GPT-5 model family rejects any value other than its default
				body: JSON.stringify({ model, messages, response_format: { type: 'json_object' } }),
				signal: controller.signal,
			})
		} catch (e) {
			if (signal.aborted) throw e
			const reason = timedOut ? 'the request timed out' : e instanceof Error ? e.message : String(e)
			throw new OpenRouterError(`The assistant could not be reached: ${reason}`)
		}

		if (!response.ok) throw new OpenRouterError(await describeFailure(response))

		let body: ChatResponse
		try {
			body = (await response.json()) as ChatResponse
		} catch (e) {
			if (signal.aborted) throw e
			throw new OpenRouterError('The assistant returned an unreadable response.')
		}
		if (body.error?.message) throw new OpenRouterError(body.error.message)
		const content = body.choices?.[0]?.message?.content
		if (!content) throw new OpenRouterError('The assistant returned an empty response.')
		return content
	} finally {
		window.clearTimeout(timer)
		signal.removeEventListener('abort', abort)
	}
}

async function describeFailure(response: Response): Promise<string> {
	let detail = ''
	try {
		const text = await response.text()
		try {
			detail = (JSON.parse(text) as ChatResponse).error?.message ?? text
		} catch {
			detail = text
		}
	} catch {
		// No body
	}
	detail = detail.slice(0, 500)
	switch (response.status) {
		case 401:
		case 402:
		case 403:
			return `OpenRouter rejected the API key (status ${response.status}). Check the key in the assistant settings.${detail ? `\n\n${detail}` : ''}`
		case 429:
			return 'OpenRouter rate limit reached. Please try again in a moment.'
		default:
			return `OpenRouter returned status ${response.status}${detail ? `: ${detail}` : '.'}`
	}
}
