import { commands } from './commands'
import { DialogFrame } from './Dialogs'
import { shortcutDocs } from './features/registry'
import { formatShortcut } from './hooks'

const extra: Array<{ group: string; label: string; keys: string }> = [
	{ group: 'Canvas', label: 'Pan', keys: 'Scroll or two-finger swipe' },
	{ group: 'Canvas', label: 'Zoom at pointer', keys: `Pinch or ${formatShortcut('Mod')} + scroll` },
	{ group: 'Canvas', label: 'Connect pins', keys: 'Drag from pin to pin' },
	{ group: 'Canvas', label: 'Move selection', keys: 'Arrow keys' },
	{ group: 'Canvas', label: 'Leave sub-circuit', keys: 'Esc' },
]

export function ShortcutsDialog({ onClose }: { onClose: () => void }) {
	const groups = new Map<string, Array<{ label: string; keys: string }>>()
	for (const c of commands) {
		if (!c.shortcut) continue
		if (!groups.has(c.group)) groups.set(c.group, [])
		groups.get(c.group)!.push({ label: c.label, keys: formatShortcut(c.shortcut) })
	}
	for (const e of shortcutDocs) {
		if (!groups.has(e.group)) groups.set(e.group, [])
		groups.get(e.group)!.push({ label: e.label, keys: formatShortcut(e.keys) })
	}
	for (const e of extra) {
		if (!groups.has(e.group)) groups.set(e.group, [])
		groups.get(e.group)!.push({ label: e.label, keys: e.keys })
	}
	return (
		<DialogFrame title="Keyboard shortcuts" onCancel={onClose} wide>
			<div className="shortcuts">
				{[...groups].map(([group, items]) => (
					<section key={group}>
						<h3>{group}</h3>
						<dl>
							{items.map((i) => (
								<div key={i.label} className="shortcut-row">
									<dt>{i.label}</dt>
									<dd>
										<kbd>{i.keys}</kbd>
									</dd>
								</div>
							))}
						</dl>
					</section>
				))}
			</div>
		</DialogFrame>
	)
}
