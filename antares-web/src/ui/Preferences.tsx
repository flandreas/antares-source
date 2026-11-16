import { engine, type ConnectMethod } from '../core/engine'
import { DialogFrame } from './Dialogs'

const CONNECT_KEY = 'antares.connectMethod'

/** The stored wire drawing method; dragging from pin to pin is the default in the browser. */
export function storedConnectMethod(): ConnectMethod {
	return localStorage.getItem(CONNECT_KEY) === 'setPoints' ? 'setPoints' : 'autoLayout'
}

export function PreferencesDialog({
	dark,
	onToggleTheme,
	onClose,
	connectMethod,
	onConnectMethod,
}: {
	dark: boolean
	onToggleTheme: () => void
	onClose: () => void
	connectMethod: ConnectMethod
	onConnectMethod: (m: ConnectMethod) => void
}) {
	const setMethod = (m: ConnectMethod) => {
		localStorage.setItem(CONNECT_KEY, m)
		engine.setConnectMethod(m)
		onConnectMethod(m)
	}
	return (
		<DialogFrame title="Preferences" onCancel={onClose}>
			<div className="dialog-body prefs">
				<fieldset>
					<legend>Drawing wires</legend>
					<label className="radio">
						<input type="radio" checked={connectMethod === 'autoLayout'} onChange={() => setMethod('autoLayout')} />
						<span>
							<strong>Drag from pin to pin</strong>
							<span className="subtle">Wires are routed automatically. Hold Alt to place waypoints instead.</span>
						</span>
					</label>
					<label className="radio">
						<input type="radio" checked={connectMethod === 'setPoints'} onChange={() => setMethod('setPoints')} />
						<span>
							<strong>Click pins and waypoints</strong>
							<span className="subtle">Click a pin, click to add corners, click the target pin. Hold Alt to drag instead.</span>
						</span>
					</label>
				</fieldset>
				<fieldset>
					<legend>Appearance</legend>
					<label className="radio">
						<input type="radio" checked={!dark} onChange={() => dark && onToggleTheme()} />
						<span>
							<strong>Light</strong>
						</span>
					</label>
					<label className="radio">
						<input type="radio" checked={dark} onChange={() => !dark && onToggleTheme()} />
						<span>
							<strong>Dark</strong>
						</span>
					</label>
				</fieldset>
			</div>
			<div className="dialog-actions">
				<button type="button" className="btn btn-primary" onClick={onClose}>
					Done
				</button>
			</div>
		</DialogFrame>
	)
}
