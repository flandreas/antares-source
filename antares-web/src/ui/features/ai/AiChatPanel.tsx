// The chat panel of the circuit assistant (desktop: AiChatPanelSwing), docked at the right of the circuit canvas.
import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import { AlertCircle, ArrowUp, Eye, EyeOff, Loader2, MessageSquareText, Settings2, Square, SquarePen, X } from 'lucide-react'
import { markdownToHtml } from '../docs/markdown'
import { aiInfo, chat, useChat, type ChatEntry } from './chat'
import { aiSettings } from './openRouter'

export function AiChatPanel() {
	const { busy, entries } = useChat()
	const [settingsOpen, setSettingsOpen] = useState(() => !aiSettings.apiKey())
	const [draft, setDraft] = useState('')
	const [model, setModel] = useState(aiSettings.model)
	const input = useRef<HTMLTextAreaElement>(null)
	const transcript = useRef<HTMLDivElement>(null)
	const info = aiInfo()

	const focusInput = () => requestAnimationFrame(() => input.current?.focus())

	useEffect(() => {
		if (!busy) focusInput()
	}, [busy])

	// The circuit canvas gets narrower while the panel is shown; the Kotlin canvases adapt on window resize events
	useLayoutEffect(() => {
		window.dispatchEvent(new Event('resize'))
		return () => {
			requestAnimationFrame(() => window.dispatchEvent(new Event('resize')))
		}
	}, [])

	useLayoutEffect(() => {
		const el = transcript.current
		if (el) el.scrollTop = el.scrollHeight
	}, [entries, busy])

	// Grow the prompt field with its content, up to a limit
	useLayoutEffect(() => {
		const el = input.current
		if (!el) return
		el.style.height = 'auto'
		el.style.height = `${Math.min(el.scrollHeight, 160)}px`
	}, [draft])

	const send = () => {
		if (busy || !draft.trim()) return
		const text = draft
		setDraft('')
		void chat.send(text)
	}

	return (
		<section
			className="ai-panel"
			aria-label="AI Assistant"
			// Keep the browser's own menu for copying text instead of the canvas context menu
			onContextMenu={(e) => e.stopPropagation()}
		>
			<header className="ai-header">
				<MessageSquareText size={15} className="ai-header-icon" />
				<span className="ai-title">AI Assistant</span>
				<span className="ai-model truncate" title={`Model: ${model || info.defaultModel}`}>
					{model || info.defaultModel}
				</span>
				<span className="ai-spacer" />
				<button
					type="button"
					className="icon-btn small"
					title="New conversation – forget the conversation so far. The circuit is not changed."
					aria-label="New conversation"
					disabled={entries.length === 0 && !busy}
					onClick={() => {
						chat.clear()
						focusInput()
					}}
				>
					<SquarePen size={14} />
				</button>
				<button
					type="button"
					className={`icon-btn small${settingsOpen ? ' pressed' : ''}`}
					title="Assistant settings – OpenRouter API key and model"
					aria-label="Assistant settings"
					aria-expanded={settingsOpen}
					onClick={() => setSettingsOpen(!settingsOpen)}
				>
					<Settings2 size={14} />
				</button>
				<button type="button" className="icon-btn small" title="Close" aria-label="Close" onClick={() => chat.setOpen(false)}>
					<X size={15} />
				</button>
			</header>

			{settingsOpen && (
				<AiSettings
					model={model}
					defaultModel={info.defaultModel}
					onModel={(value) => {
						setModel(value)
						aiSettings.setModel(value)
					}}
					onDone={() => {
						setSettingsOpen(false)
						focusInput()
					}}
				/>
			)}

			<div ref={transcript} className="ai-transcript" aria-live="polite">
				{entries.length === 0 ? (
					<div className="ai-intro">
						<p>
							Ask about the open circuit, or describe what to build in it. The assistant can add, connect and delete
							components and change bit widths. Everything it changes can be undone with a single Undo.
						</p>
						<div className="ai-examples">
							{info.examples.map((example) => (
								<button
									key={example.name}
									type="button"
									className="ai-example"
									title={example.prompt}
									onClick={() => {
										setDraft(example.prompt)
										focusInput()
									}}
								>
									{example.name}
								</button>
							))}
						</div>
					</div>
				) : (
					entries.map((entry) => <Entry key={entry.id} entry={entry} />)
				)}
				{busy && (
					<div className="ai-thinking">
						<Loader2 size={14} className="ai-spin" /> Thinking…
					</div>
				)}
			</div>

			<form
				className="ai-composer"
				onSubmit={(e) => {
					e.preventDefault()
					send()
				}}
			>
				<textarea
					ref={input}
					className="ai-input"
					rows={2}
					value={draft}
					disabled={busy}
					placeholder="Describe what to build, or ask about the circuit"
					aria-label="Message to the assistant"
					onChange={(e) => setDraft(e.target.value)}
					onKeyDown={(e) => {
						if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
							e.preventDefault()
							send()
						}
					}}
				/>
				<div className="ai-composer-bar">
					<span className="ai-composer-hint">Sent to OpenRouter · Enter to send</span>
					{busy ? (
						<button type="button" className="btn btn-sm" onClick={() => chat.stop()} title="Cancel the running request">
							<Square size={11} fill="currentColor" /> Stop
						</button>
					) : (
						<button type="submit" className="btn btn-sm btn-primary" disabled={!draft.trim()} title="Send (Enter)">
							<ArrowUp size={14} /> Send
						</button>
					)}
				</div>
			</form>
		</section>
	)
}

function Entry({ entry }: { entry: ChatEntry }) {
	const html = useMemo(() => (entry.role === 'assistant' ? markdownToHtml(entry.text) : ''), [entry])
	switch (entry.role) {
		case 'user':
			return (
				<div className="ai-msg user">
					<div className="ai-bubble">{entry.text}</div>
				</div>
			)
		case 'assistant':
			return <div className="ai-msg assistant markdown" dangerouslySetInnerHTML={{ __html: html }} />
		case 'error':
			return (
				<div className="ai-msg error" role="alert">
					<AlertCircle size={14} className="ai-msg-icon" />
					<span>{entry.text}</span>
				</div>
			)
		case 'info':
			return <div className="ai-msg info">{entry.text}</div>
	}
}

/** The OpenRouter API key and model, stored only in this browser. */
function AiSettings({
	model,
	defaultModel,
	onModel,
	onDone,
}: {
	model: string
	defaultModel: string
	onModel: (value: string) => void
	onDone: () => void
}) {
	const [key, setKey] = useState(aiSettings.apiKey)
	const [visible, setVisible] = useState(false)
	return (
		<div className="ai-settings">
			<label className="ai-field">
				<span>OpenRouter API key</span>
				<div className="ai-key">
					<input
						className="input"
						type={visible ? 'text' : 'password'}
						value={key}
						placeholder="sk-or-…"
						autoComplete="off"
						spellCheck={false}
						autoFocus={!key}
						onChange={(e) => {
							setKey(e.target.value)
							aiSettings.setApiKey(e.target.value)
						}}
					/>
					<button
						type="button"
						className="icon-btn small"
						title={visible ? 'Hide key' : 'Show key'}
						aria-label={visible ? 'Hide key' : 'Show key'}
						onClick={() => setVisible(!visible)}
					>
						{visible ? <EyeOff size={14} /> : <Eye size={14} />}
					</button>
				</div>
			</label>
			<label className="ai-field">
				<span>Model</span>
				<input
					className="input"
					value={model}
					placeholder={defaultModel}
					spellCheck={false}
					onChange={(e) => onModel(e.target.value)}
				/>
			</label>
			<p className="ai-privacy">
				The key is stored only in this browser. Your messages and a description of the open circuit (its components
				and wires) are sent directly to OpenRouter.{' '}
				<a href="https://openrouter.ai/keys" target="_blank" rel="noopener noreferrer">
					Get a key
				</a>
			</p>
			<div className="ai-settings-actions">
				{key && (
					<button
						type="button"
						className="btn btn-sm btn-ghost"
						onClick={() => {
							setKey('')
							aiSettings.setApiKey('')
						}}
					>
						Remove key
					</button>
				)}
				<button type="button" className="btn btn-sm" onClick={onDone}>
					Done
				</button>
			</div>
		</div>
	)
}
