// Overview of the global tunnels in the project and its libraries (desktop: `GlobalTunnelPanel`). Choosing a
// circuit opens it and finds the tunnel there, like the desktop's "Open circuit" action.
import { useState } from 'react'
import { Copy, ExternalLink, Search, Waypoints } from 'lucide-react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { findBar } from '../find/store'
import { analysisApi, type GlobalTunnel, type TunnelUsage } from './api'

export function showGlobalTunnels() {
	// Collected before rendering, as collecting saves the open circuit, which notifies the UI
	const tunnels = analysisApi.globalTunnels()
	return dialogs.custom<null>((done) => <TunnelsDialog tunnels={tunnels} onClose={() => done(null)} />)
}

/** Resolves once the circuit [uuid] is shown, or after a timeout. */
function circuitShown(uuid: string): Promise<void> {
	return new Promise((resolve) => {
		const started = Date.now()
		const check = () => {
			if (engine.state.circuit?.uuid === uuid || Date.now() - started > 3000) setTimeout(resolve, 60)
			else setTimeout(check, 40)
		}
		check()
	})
}

/** Opens the circuit of [usage] (unless shown) and finds the tunnel [name] in it. */
async function showUsage(name: string, usage: TunnelUsage) {
	if (engine.state.circuit?.uuid !== usage.circuit) {
		engine.openItem(usage.circuit)
		await circuitShown(usage.circuit)
	} else if (engine.state.breadcrumbs.length > 1) {
		engine.navigateTo(0)
	}
	if (engine.state.view === 'symbol') engine.showSymbol(false)
	findBar.show({ text: name, match: 'entireWord' })
}

function directions(u: TunnelUsage): string[] {
	const result: string[] = []
	if (u.inFlow) result.push('In')
	if (u.outFlow) result.push('Out')
	if (u.inOut) result.push('In/Out')
	return result
}

function TunnelsDialog({ tunnels, onClose }: { tunnels: GlobalTunnel[]; onClose: () => void }) {
	const [filter, setFilter] = useState('')
	const [selected, setSelected] = useState<string | null>(tunnels[0]?.name ?? null)
	const shown = tunnels.filter((t) => t.name.toLowerCase().includes(filter.trim().toLowerCase()))
	const tunnel: GlobalTunnel | undefined = shown.find((t) => t.name === selected) ?? shown[0]

	return (
		<DialogFrame title="Global tunnels" onCancel={onClose} wide>
			{tunnels.length === 0 ? (
				<div className="dialog-body">
					<div className="analysis-empty-dialog">
						<Waypoints size={20} />
						<span>
							The project has no global tunnels. Tunnels marked as global connect to tunnels with the same name in all
							circuits.
						</span>
					</div>
				</div>
			) : (
				<div className="tunnels">
					<div className="tunnels-names">
						<label className="search">
							<Search size={14} />
							<input
								autoFocus
								value={filter}
								placeholder="Filter tunnels"
								aria-label="Filter tunnels"
								onChange={(e) => setFilter(e.target.value)}
							/>
						</label>
						<div className="tunnels-list" role="listbox" aria-label="Tunnels">
							{shown.length === 0 && <div className="empty-small">No matching tunnels</div>}
							{shown.map((t) => (
								<button
									key={t.name}
									type="button"
									role="option"
									aria-selected={t === tunnel}
									className={`tunnels-item${t === tunnel ? ' selected' : ''}`}
									onClick={() => setSelected(t.name)}
								>
									<span className="truncate">{t.name}</span>
									<span className={`tunnels-badge${t.usages.length < 2 ? ' lone' : ''}`} title="Circuits using the tunnel">
										{t.usages.length}
									</span>
								</button>
							))}
						</div>
					</div>
					<div className="tunnels-usages">
						{tunnel && (
							<>
								<div className="tunnels-usages-header">
									<strong className="truncate">{tunnel.name}</strong>
									<button
										type="button"
										className="btn btn-sm btn-ghost"
										onClick={() => {
											void navigator.clipboard?.writeText(tunnel.name)
											engine.message('success', '', `Copied “${tunnel.name}”`)
										}}
									>
										<Copy size={13} /> Copy name
									</button>
								</div>
								{tunnel.usages.length < 2 && (
									<p className="analysis-dialog-hint warn">Used in a single circuit only, so it connects nothing.</p>
								)}
								<div className="tunnels-usage-list">
									{tunnel.usages.map((u) => (
										<button
											key={u.circuit}
											type="button"
											className="tunnels-usage"
											title={`Open “${u.circuitName}” and find the tunnel`}
											onClick={() => {
												onClose()
												void showUsage(tunnel.name, u)
											}}
										>
											<span className="truncate">{u.circuitName}</span>
											<span className="tunnels-directions">
												{directions(u).map((d) => (
													<span key={d} className="pill">
														{d}
													</span>
												))}
											</span>
											<ExternalLink size={13} className="tunnels-open" />
										</button>
									))}
								</div>
							</>
						)}
					</div>
				</div>
			)}
			<div className="dialog-actions analysis-dialog-footer">
				<span className="subtle">
					{tunnels.length} {tunnels.length === 1 ? 'tunnel' : 'tunnels'} in the project and its libraries
				</span>
				<button type="button" className="btn btn-primary" onClick={onClose}>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}
