// The AI circuit assistant (desktop: ShowAiChatAction, AiChatPanelSwing): a chat panel next to the circuit,
// toggled from the Tools menu or with Mod+Alt+A.
import { MessageSquareText } from 'lucide-react'
import { matches } from '../../commands'
import { appMenuContributions, canvasOverlays, register, shortcutDocs } from '../registry'
import { AiChatPanel } from './AiChatPanel'
import { chat, useChat } from './chat'
import './ai.css'

const SHORTCUT = 'Mod+Alt+A'

function AiOverlay() {
	const { open } = useChat()
	return open ? <AiChatPanel /> : null
}

register(appMenuContributions.tools, (c) => c.id, {
	id: 'ai',
	entries: () => [
		{
			label: chat.state.open ? 'Hide AI Assistant' : 'AI Assistant',
			icon: <MessageSquareText size={15} />,
			shortcut: SHORTCUT,
			onSelect: () => chat.toggle(),
		},
	],
})

register(canvasOverlays, (o) => o.id, { id: 'ai', component: AiOverlay })

const onKey = (e: KeyboardEvent) => {
	if (matches(e, SHORTCUT) && !document.querySelector('.dialog-backdrop')) {
		e.preventDefault()
		chat.toggle()
	}
}
window.addEventListener('keydown', onKey)
import.meta.hot?.dispose(() => window.removeEventListener('keydown', onKey))

register(shortcutDocs, (d) => d.label, { group: 'Tools', label: 'Toggle AI assistant', keys: SHORTCUT })
