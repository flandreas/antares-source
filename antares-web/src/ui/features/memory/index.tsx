// Memory items and the contents of RAM/ROM/lookup table components.
import { useEffect, useState } from 'react'
import { ExternalLink, MemoryStick, X } from 'lucide-react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { itemNames } from '../../projectActions'
import { useEngineVersion } from '../../hooks'
import { dialogViews, documentEditors, documentInspectors, newItemActions, type DialogViewProps, register } from '../registry'
import { memoryApi, type MemoryInfo } from './api'
import { MemoryView } from './MemoryView'
import './memory.css'

/** Address widths offered for memory items; the simulator addresses at most 2^31 cells. */
const ADDRESS_WIDTHS = Array.from({ length: 31 }, (_, i) => i + 1)
const DATA_WIDTHS = Array.from({ length: 64 }, (_, i) => i + 1)

/** Describes a memory size, e.g. "256 cells × 8 bit (256 bytes)". */
function capacity(addressWidth: number, dataWidth: number): string {
	const cells = 2 ** addressWidth
	const bytes = (cells * dataWidth) / 8
	const units = ['bytes', 'KiB', 'MiB', 'GiB', 'TiB']
	let size = bytes
	let unit = 0
	while (size >= 1024 && unit < units.length - 1) {
		size /= 1024
		unit++
	}
	return `${cells.toLocaleString()} cells × ${dataWidth} bit (${Number.isInteger(size) ? size : size.toFixed(1)} ${units[unit]})`
}

function WidthSelect({ value, widths, onChange, label }: { value: number; widths: number[]; onChange: (w: number) => void; label: string }) {
	const options = widths.includes(value) ? widths : [...widths, value]
	return (
		<select className="input select" aria-label={label} value={value} onChange={(e) => onChange(Number(e.target.value))}>
			{options.map((w) => (
				<option key={w} value={w}>
					{w} {w === 1 ? 'bit' : 'bits'}
				</option>
			))}
		</select>
	)
}

/** Asks for name and widths of a new memory item (desktop: NewMemoryStorablePanel). */
async function newMemory() {
	const names = itemNames('memory')
	let base = 'Memory'
	for (let i = 2; names.includes(base); i++) base = `Memory ${i}`
	const result = await dialogs.custom<{ name: string; addressWidth: number; dataWidth: number }>((done) => (
		<NewMemoryDialog initial={base} names={names} done={done} />
	))
	if (result) memoryApi.create(result.name, result.addressWidth, result.dataWidth)
}

function NewMemoryDialog({
	initial,
	names,
	done,
}: {
	initial: string
	names: string[]
	done: (v: { name: string; addressWidth: number; dataWidth: number } | null) => void
}) {
	const [name, setName] = useState(initial)
	const [addressWidth, setAddressWidth] = useState(8)
	const [dataWidth, setDataWidth] = useState(8)
	const [touched, setTouched] = useState(false)
	const error = !name.trim() ? 'Please enter a name.' : names.includes(name.trim()) ? 'An item with this name already exists.' : null
	return (
		<DialogFrame title="New memory" onCancel={() => done(null)}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					setTouched(true)
					if (!error) done({ name: name.trim(), addressWidth, dataWidth })
				}}
			>
				<div className="dialog-body mem-new">
					<label className="field">
						<span className="field-label">Name</span>
						<input className="input" value={name} autoFocus onFocus={(e) => e.target.select()} onChange={(e) => setName(e.target.value)} />
					</label>
					<div className="mem-new-widths">
						<label className="field">
							<span className="field-label">Address width</span>
							<WidthSelect label="Address width" value={addressWidth} widths={ADDRESS_WIDTHS} onChange={setAddressWidth} />
						</label>
						<label className="field">
							<span className="field-label">Data width</span>
							<WidthSelect label="Data width" value={dataWidth} widths={DATA_WIDTHS} onChange={setDataWidth} />
						</label>
					</div>
					<div className="mem-capacity">{capacity(addressWidth, dataWidth)}</div>
					{touched && error && <div className="field-error">{error}</div>}
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={() => done(null)}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary">
						Create memory
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}

/** The open memory item, shown in the main area. */
function MemoryDocument() {
	return <MemoryView target="document" />
}

/** Name and widths of the open memory item (desktop: MemoryStorableBeanInfo). */
function MemoryInspector() {
	const version = useEngineVersion()
	const info = memoryApi.info('document')
	const [name, setName] = useState(info?.title ?? '')
	useEffect(() => setName(info?.title ?? ''), [info?.title, version])
	if (!info) return null
	const editable = info.editable
	const commitName = () => {
		const trimmed = name.trim()
		if (!trimmed || trimmed === info.title) setName(info.title)
		else if (itemNames('memory').includes(trimmed)) {
			engine.message('error', '', 'An item with this name already exists.')
			setName(info.title)
		} else memoryApi.setName(trimmed)
	}
	return (
		<>
			<section className="prop-group">
				<h3>Memory</h3>
				<div className="prop-row">
					<span className="prop-label">Name</span>
					<div className="prop-control">
						<input
							className="input"
							value={name}
							readOnly={!editable}
							onChange={(e) => setName(e.target.value)}
							onBlur={commitName}
							onKeyDown={(e) => e.key === 'Enter' && e.currentTarget.blur()}
						/>
					</div>
				</div>
				<div className="prop-row">
					<span className="prop-label">Address width</span>
					<div className="prop-control">
						{editable ? (
							<WidthSelect label="Address width" value={info.addressWidth} widths={ADDRESS_WIDTHS} onChange={(w) => memoryApi.setAddressWidth(w)} />
						) : (
							<span className="prop-static">{info.addressWidth} bits</span>
						)}
					</div>
				</div>
				<div className="prop-row">
					<span className="prop-label">Data width</span>
					<div className="prop-control">
						{editable ? (
							<WidthSelect label="Data width" value={info.dataWidth} widths={DATA_WIDTHS} onChange={(w) => memoryApi.setDataWidth(w)} />
						) : (
							<span className="prop-static">{info.dataWidth} bits</span>
						)}
					</div>
				</div>
				<div className="prop-row">
					<span className="prop-label">Size</span>
					<span className="prop-static">{capacity(info.addressWidth, info.dataWidth)}</span>
				</div>
			</section>
			<section className="prop-group">
				<div className="hint subtle">
					To use this memory in a circuit, select a ROM and choose it as <em>Referenced memory contents</em>. The ROM loads
					these contents whenever the simulation starts.
				</div>
			</section>
		</>
	)
}

const typeLabels: Record<MemoryInfo['type'], string> = { memory: 'Memory', rom: 'ROM', ram: 'RAM', lookupTable: 'Lookup table' }

/** Notes about where the contents come from, shown above the cells. */
function contentsNotice(info: MemoryInfo, onClose: () => void) {
	if (info.type === 'ram' && !info.simulating) {
		return <div className="mem-notice">RAM contents exist only while simulating. Start the simulation to view and edit them.</div>
	}
	if (info.linkedMemory) {
		const linked = info.linkedMemory
		return (
			<div className="mem-notice">
				<span>
					Loaded from memory <strong>{linked.name}</strong> whenever the simulation starts.
				</span>
				{!info.simulating && (
					<button
						type="button"
						className="btn btn-sm btn-ghost"
						onClick={() => {
							onClose()
							engine.openItem(linked.uuid)
						}}
					>
						<ExternalLink size={14} /> Open memory
					</button>
				)}
			</div>
		)
	}
	return null
}

/** The contents of a RAM, ROM or lookup table, opened by double-clicking it (desktop: AddressableContentsPanel dialog). */
function MemoryContentsDialog({ onClose }: DialogViewProps) {
	const version = useEngineVersion()
	const info = memoryApi.info('dialog')
	// Close when the component no longer exists, e.g. after deleting it or opening another circuit
	useEffect(() => {
		if (!info) onClose()
	}, [info === null, version])
	// Escape closes the dialog unless a field inside handled it (e.g. cancelling a cell edit)
	useEffect(() => {
		const onKey = (e: KeyboardEvent) => {
			if (e.key === 'Escape' && !e.defaultPrevented) onClose()
		}
		window.addEventListener('keydown', onKey)
		return () => window.removeEventListener('keydown', onKey)
	}, [onClose])
	if (!info) return null
	const kind = typeLabels[info.type]
	return (
		<div className="dialog-backdrop" onPointerDown={(e) => e.target === e.currentTarget && onClose()}>
			<div
				className="dialog memory-dialog"
				role="dialog"
				aria-modal="true"
				aria-label={info.title}
			>
				<div className="dialog-header">
					<h2 className="truncate">
						{!info.title.startsWith(kind) && <span className="mem-dialog-kind">{kind}</span>}
						{info.title}
					</h2>
					<button type="button" className="icon-btn" onClick={onClose} aria-label="Close">
						<X size={16} />
					</button>
				</div>
				<MemoryView target="dialog" notice={(i) => contentsNotice(i, onClose)} />
			</div>
		</div>
	)
}

register(newItemActions, (a) => a.label, { label: 'Memory…', icon: <MemoryStick size={15} />, run: newMemory })
documentEditors.memory = MemoryDocument
documentInspectors.memory = MemoryInspector
dialogViews.memoryContents = MemoryContentsDialog
