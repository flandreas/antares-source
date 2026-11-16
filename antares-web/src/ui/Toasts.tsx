import { useEffect, useState } from 'react'
import { AlertTriangle, CheckCircle2, Info, X, XCircle } from 'lucide-react'
import { engine } from '../core/engine'
import type { Message } from '../core/types'

const icons = {
	info: <Info size={16} />,
	success: <CheckCircle2 size={16} />,
	warning: <AlertTriangle size={16} />,
	error: <XCircle size={16} />,
}

/** Shows messages reported by the editor as dismissible toasts. */
export function Toasts() {
	const [messages, setMessages] = useState<Message[]>([])

	useEffect(
		() =>
			engine.onMessage((m) => {
				if (!m.text && !m.title) return
				setMessages((list) => [...list.filter((x) => x.text !== m.text).slice(-3), m])
				window.setTimeout(
					() => setMessages((list) => list.filter((x) => x.id !== m.id)),
					m.type === 'error' ? 8000 : 4000,
				)
			}),
		[],
	)

	return (
		<div className="toasts" aria-live="polite">
			{messages.map((m) => (
				<div key={m.id} className={`toast toast-${m.type}`} role={m.type === 'error' ? 'alert' : 'status'}>
					<span className="toast-icon">{icons[m.type] ?? icons.info}</span>
					<div className="toast-text">
						{m.title && <strong>{m.title}</strong>}
						<span>{m.text}</span>
					</div>
					<button
						type="button"
						className="icon-btn"
						aria-label="Dismiss"
						onClick={() => setMessages((list) => list.filter((x) => x.id !== m.id))}
					>
						<X size={14} />
					</button>
				</div>
			))}
		</div>
	)
}
