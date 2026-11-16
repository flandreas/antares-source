// The memory contents editor (desktop: AddressableContentsPanel/AddressableDisplayPanel), used for memory items
// and for the contents of RAM/ROM/lookup table components. Rows are virtualized, so large memories stay fast.
import { useEffect, useLayoutEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { ChevronLeft, ChevronRight, Download, Eraser, Locate, PaintBucket, Upload } from 'lucide-react'
import { engine } from '../../../core/engine'
import { dialogs, DialogFrame } from '../../Dialogs'
import { Menu, type MenuEntry } from '../../Menu'
import { download } from '../../projectActions'
import { isMac, useEngineVersion, useStoredState } from '../../hooks'
import {
	formatAddress,
	formatLabels,
	memoryApi,
	parseAddress,
	valueChars,
	type DumpFormat,
	type MemoryInfo,
	type MemoryTarget,
	type ValueFormat,
} from './api'

const ROW_H = 24
const HEAD_H = 28
/** Rows per page; memories with more rows (e.g. 2^20 cells in one column) are shown page by page */
const PAGE_ROWS = 1 << 16
const OVERSCAN = 6
const LAYOUTS = [1, 4, 8, 16]
/** Most cells copied at once */
const MAX_COPY = 1 << 16

type Field = 'value' | 'comment'

interface Selection {
	anchor: number
	focus: number
	field: Field
}

interface Editing {
	address: number
	field: Field
	text: string
}

/** Shows the memory of [target], or [empty] if there is none. */
export function MemoryView({ target, empty, notice }: { target: MemoryTarget; empty?: ReactNode; notice?: (info: MemoryInfo) => ReactNode }) {
	const version = useEngineVersion()
	const info = useMemo(() => memoryApi.info(target), [target, version])
	if (!info) return <>{empty ?? null}</>
	return <MemoryEditor target={target} info={info} version={version} notice={notice?.(info)} />
}

function MemoryEditor({ target, info, version, notice }: { target: MemoryTarget; info: MemoryInfo; version: number; notice: ReactNode }) {
	const [format, setFormat] = useStoredState<ValueFormat>('antares.memory.format', 'hex')
	const [layout, setLayout] = useStoredState('antares.memory.columns', 16)
	const cols = LAYOUTS.includes(layout) ? layout : 16
	const single = cols === 1
	const { maxAddress, dataWidth, addressWidth, editable } = info
	const addressFormat = addressFormatFor(format)

	const [sel, setSel] = useState<Selection>({ anchor: 0, focus: 0, field: 'value' })
	const [editing, setEditing] = useState<Editing | null>(null)
	const [page, setPage] = useState(0)
	const [scrollTop, setScrollTop] = useState(0)
	const [height, setHeight] = useState(400)
	const [goTo, setGoTo] = useState('')
	const [exportMenu, setExportMenu] = useState<{ x: number; y: number } | null>(null)

	const scroller = useRef<HTMLDivElement>(null)
	const grid = useRef<HTMLDivElement>(null)
	const goToInput = useRef<HTMLInputElement>(null)
	const reveal = useRef<number | null>(null)
	const dragging = useRef(false)

	const totalRows = Math.floor(maxAddress / cols) + 1
	const pages = Math.ceil(totalRows / PAGE_ROWS)
	const pageStart = page * PAGE_ROWS
	const pageRows = Math.min(PAGE_ROWS, totalRows - pageStart)
	const bodyH = Math.max(0, height - HEAD_H)
	const visibleRows = Math.max(1, Math.floor(bodyH / ROW_H))

	// Keep the selection and page valid when the memory shrinks (e.g. a smaller address width)
	const clampAddress = (a: number) => Math.max(0, Math.min(maxAddress, a))
	useEffect(() => {
		if (sel.anchor > maxAddress || sel.focus > maxAddress) setSel((s) => ({ ...s, anchor: clampAddress(s.anchor), focus: clampAddress(s.focus) }))
		if (page >= pages) setPage(pages - 1)
		if (!single && sel.field === 'comment') setSel((s) => ({ ...s, field: 'value' }))
	}, [maxAddress, pages, single])

	useEffect(() => {
		const el = scroller.current!
		const observer = new ResizeObserver(() => setHeight(el.clientHeight))
		observer.observe(el)
		setHeight(el.clientHeight)
		return () => observer.disconnect()
	}, [])

	// Scrolls a requested address into view once its page is rendered
	useLayoutEffect(() => {
		const address = reveal.current
		const el = scroller.current
		if (address === null || !el) return
		const row = Math.floor(address / cols)
		const p = Math.floor(row / PAGE_ROWS)
		if (p !== page) {
			setPage(p)
			return
		}
		reveal.current = null
		const top = (row - pageStart) * ROW_H
		// Jumps to far away cells center them, small moves just keep them visible
		if (top + ROW_H < el.scrollTop - bodyH || top > el.scrollTop + 2 * bodyH) el.scrollTop = top - (bodyH - ROW_H) / 2
		else if (top < el.scrollTop) el.scrollTop = top
		else if (top + ROW_H > el.scrollTop + bodyH) el.scrollTop = top + ROW_H - bodyH
	})

	const select = (focus: number, extend: boolean, field: Field = sel.field) => {
		const f = clampAddress(focus)
		setSel({ anchor: extend ? sel.anchor : f, focus: f, field: single ? field : 'value' })
		reveal.current = f
	}

	const firstRow = Math.max(0, Math.floor(scrollTop / ROW_H) - OVERSCAN)
	const lastRow = Math.min(pageRows - 1, Math.ceil((scrollTop + bodyH) / ROW_H) + OVERSCAN)
	const fetchStart = (pageStart + firstRow) * cols
	const fetchCount = (lastRow - firstRow + 1) * cols
	const cells = useMemo(
		() => memoryApi.cells(target, fetchStart, fetchCount, format),
		// The version changes with every edit and while simulating
		[target, fetchStart, fetchCount, format, version, dataWidth],
	)

	const selFrom = Math.min(sel.anchor, sel.focus)
	const selTo = Math.max(sel.anchor, sel.focus)
	const selCount = selTo - selFrom + 1

	const focusGrid = () => grid.current?.focus({ preventScroll: true })

	/** ---- Editing */

	const startEdit = (address: number, field: Field, text?: string) => {
		if (!editable) return
		const current = valuesAt(address, 1)?.[field === 'comment' ? 'comments' : 'values'][0] ?? ''
		setEditing({ address, field, text: text ?? current })
		reveal.current = address
	}

	const valuesAt = (start: number, count: number) => memoryApi.cells(target, start, count, format)

	/** Commits the edited text; returns false if the value is invalid. */
	const commit = (e: Editing): boolean => {
		if (e.field === 'comment') {
			memoryApi.setComment(target, e.address, e.text)
			return true
		}
		if (e.text.trim() === '') return true
		const error = memoryApi.setValues(target, e.address, [e.text], format)
		if (error) {
			engine.message('error', 'Invalid value', error)
			return false
		}
		return true
	}

	const finishEdit = (move: number) => {
		if (!editing) return
		if (!commit(editing)) return
		setEditing(null)
		const next = clampAddress(editing.address + move)
		setSel({ anchor: next, focus: next, field: editing.field })
		reveal.current = next
		focusGrid()
	}

	const setRange = (from: number, texts: string[]) => {
		const error = memoryApi.setValues(target, from, texts, format)
		if (error) engine.message('error', 'Invalid value', error)
		return !error
	}

	/** ---- Keyboard, clipboard */

	const onKeyDown = (e: React.KeyboardEvent) => {
		if (editing) return
		const mod = isMac ? e.metaKey : e.ctrlKey
		const key = e.key.toLowerCase()
		const handled = () => {
			e.preventDefault()
			e.stopPropagation()
		}
		if (mod && (key === 'z' || key === 'y')) {
			handled()
			memoryApi.undo(target, key === 'y' || e.shiftKey)
			return
		}
		if (mod && key === 'g') {
			handled()
			goToInput.current?.select()
			return
		}
		if (mod && key === 'a') {
			handled()
			setSel({ anchor: 0, focus: maxAddress, field: sel.field })
			return
		}
		if (mod || e.altKey) return
		switch (e.key) {
			case 'ArrowLeft':
			case 'ArrowRight': {
				handled()
				const dir = e.key === 'ArrowLeft' ? -1 : 1
				if (single) select(sel.focus, e.shiftKey, dir < 0 ? 'value' : 'comment')
				else select(sel.focus + dir, e.shiftKey)
				return
			}
			case 'ArrowUp':
			case 'ArrowDown':
				handled()
				select(sel.focus + (e.key === 'ArrowUp' ? -cols : cols), e.shiftKey)
				return
			case 'PageUp':
			case 'PageDown':
				handled()
				select(sel.focus + (e.key === 'PageUp' ? -1 : 1) * visibleRows * cols, e.shiftKey)
				return
			case 'Home':
				handled()
				select(sel.focus - (sel.focus % cols), e.shiftKey)
				return
			case 'End':
				handled()
				select(sel.focus - (sel.focus % cols) + cols - 1, e.shiftKey)
				return
			case 'Enter':
			case 'F2':
				handled()
				startEdit(sel.focus, sel.field)
				return
			case 'Tab':
				handled()
				select(sel.focus + (e.shiftKey ? -1 : 1), false)
				return
			case 'Delete':
			case 'Backspace':
				handled()
				if (!editable) return
				if (sel.field === 'comment') {
					for (let a = selFrom; a <= Math.min(selTo, selFrom + 4095); a++) memoryApi.setComment(target, a, '')
				} else if (selCount > MAX_COPY) {
					memoryApi.fill(target, selFrom, selTo, '0', format)
				} else {
					setRange(selFrom, Array<string>(selCount).fill('0'))
				}
				return
		}
		if (e.key.length === 1 && editable && e.key !== ' ') {
			handled()
			startEdit(sel.focus, sel.field, e.key)
		}
	}

	const onCopy = (e: React.ClipboardEvent) => {
		if (editing) return
		e.preventDefault()
		e.stopPropagation()
		if (selCount > MAX_COPY) {
			engine.message('info', '', `At most ${MAX_COPY} cells can be copied at once.`)
			return
		}
		const data = valuesAt(selFrom, selCount)
		if (!data) return
		const list = sel.field === 'comment' ? data.comments : data.values
		const text =
			sel.field === 'comment' || single
				? list.join('\n')
				: list.map((v, i) => v + ((selFrom + i + 1) % cols === 0 ? '\n' : ' ')).join('').trimEnd()
		e.clipboardData.setData('text/plain', text)
	}

	const onPaste = (e: React.ClipboardEvent) => {
		if (editing) return
		e.preventDefault()
		e.stopPropagation()
		if (!editable) return
		const text = e.clipboardData.getData('text/plain')
		if (sel.field === 'comment') {
			text.split(/\r?\n/).forEach((line, i) => selFrom + i <= maxAddress && memoryApi.setComment(target, selFrom + i, line))
			return
		}
		const tokens = text.split(/[\s,;]+/).filter(Boolean)
		if (tokens.length === 0) return
		if (setRange(selFrom, tokens)) {
			const end = clampAddress(selFrom + tokens.length - 1)
			setSel({ anchor: selFrom, focus: end, field: 'value' })
		}
	}

	/** ---- Mouse */

	const cellAt = (t: EventTarget): { address: number; field: Field } | null => {
		const el = (t as HTMLElement).closest<HTMLElement>('[data-address]')
		if (!el) return null
		return { address: Number(el.dataset.address), field: (el.dataset.field as Field) ?? 'value' }
	}

	const onMouseDown = (e: React.MouseEvent) => {
		if (e.button !== 0 || (e.target as HTMLElement).tagName === 'INPUT') return
		const cell = cellAt(e.target)
		if (!cell) return
		e.preventDefault()
		// Blurs a cell being edited, which commits it
		focusGrid()
		dragging.current = true
		setSel({ anchor: e.shiftKey ? sel.anchor : cell.address, focus: cell.address, field: cell.field })
	}

	useEffect(() => {
		const up = () => (dragging.current = false)
		window.addEventListener('mouseup', up)
		return () => window.removeEventListener('mouseup', up)
	}, [])

	const onMouseOver = (e: React.MouseEvent) => {
		if (!dragging.current || e.buttons !== 1) return
		const cell = cellAt(e.target)
		if (cell && cell.field === sel.field && cell.address !== sel.focus) setSel({ ...sel, focus: cell.address })
	}

	const onDoubleClick = (e: React.MouseEvent) => {
		const cell = cellAt(e.target)
		if (cell) startEdit(cell.address, cell.field)
	}

	/** ---- Toolbar actions */

	const doGoTo = () => {
		const address = parseAddress(goTo, addressFormat, maxAddress)
		if (address === null) {
			engine.message('error', '', `Enter an address between 0 and ${formatAddress(maxAddress, addressWidth, addressFormat)} (${formatLabels[addressFormat].toLowerCase()}, or 0x… for hex).`)
			return
		}
		select(address, false, 'value')
		focusGrid()
	}

	const fill = async () => {
		const result = await askFill(info, format, selFrom, selCount > 1 ? selTo : maxAddress)
		if (!result) return
		const error = memoryApi.fill(target, result.from, result.to, result.value, format)
		if (error) engine.message('error', 'Fill failed', error)
		else setSel({ anchor: result.from, focus: result.to, field: 'value' })
	}

	const clear = async () => {
		if (!info.editable) return
		// RAM contents can't be restored by undo
		if (info.type === 'ram') {
			const ok = await dialogs.confirm({ title: 'Clear memory', message: 'Set all cells of this RAM to zero?', confirmLabel: 'Clear', danger: true })
			if (!ok) return
		}
		memoryApi.clear(target)
	}

	const importDump = () => {
		const input = document.createElement('input')
		input.type = 'file'
		input.accept = '.amd,.hex,.raw,.txt,.mem,.dat'
		input.onchange = async () => {
			const file = input.files?.[0]
			if (!file) return
			const error = memoryApi.importDump(target, await file.text(), file.name)
			if (error) engine.message('error', 'Import failed', error)
			else engine.message('success', '', `Imported ${file.name}`)
		}
		input.click()
	}

	const exportDump = (dumpFormat: DumpFormat) => {
		const text = memoryApi.exportDump(target, dumpFormat)
		const safe = info.title.replace(/[^\w\- ]+/g, '').trim() || 'memory'
		download(`${safe}.${dumpFormat}`, text, 'text/plain')
	}

	const exportEntries: MenuEntry[] = [
		{ label: 'Antares memory dump (.amd)', onSelect: () => exportDump('amd') },
		{ label: 'Logisim raw image (.raw)', onSelect: () => exportDump('raw') },
		{ label: 'Logisim addressed words (.hex)', onSelect: () => exportDump('hex') },
	]

	/** ---- Rendering */

	const radix = { hex: 16, dec: 10, oct: 8, bin: 2 }[addressFormat]
	const valueCh = Math.max(valueChars(dataWidth, format), single ? 5 : 2)
	const addressCh = Math.max(valueChars(addressWidth, addressFormat), 7)
	const cellCh = valueCh + 2
	const columnsTemplate = single
		? `${addressCh + 3}ch ${Math.max(cellCh + 1, 8)}ch ${cells?.disassembly ? 'minmax(18ch, 28ch) ' : ''}minmax(24ch, 1fr)`
		: `${addressCh + 3}ch repeat(${cols}, ${cellCh}ch) 1fr`
	const current = info.currentAddress

	const rows: ReactNode[] = []
	if (cells) {
		for (let r = firstRow; r <= lastRow; r++) {
			const rowAddress = (pageStart + r) * cols
			const items: ReactNode[] = []
			for (let c = 0; c < cols; c++) {
				const address = rowAddress + c
				if (address > maxAddress) {
					items.push(<div key={c} className="mem-cell void" />)
					continue
				}
				items.push(
					renderCell(address, 'value', cells.values[address - fetchStart] ?? '', c),
				)
			}
			if (single) {
				const i = rowAddress - fetchStart
				if (cells.disassembly) items.push(<div key="dis" className="mem-cell mem-dis">{cells.disassembly[i]}</div>)
				items.push(renderCell(rowAddress, 'comment', cells.comments[i] ?? '', 'comment'))
			} else {
				items.push(<div key="fill" />)
			}
			rows.push(
				<div key={rowAddress} className="mem-row" style={{ top: r * ROW_H, gridTemplateColumns: columnsTemplate }}>
					<div className="mem-address">{formatAddress(rowAddress, addressWidth, addressFormat)}</div>
					{items}
				</div>,
			)
		}
	}

	function renderCell(address: number, field: Field, text: string, key: string | number) {
		const isEditing = editing?.address === address && editing.field === field
		const selected = field === sel.field && address >= selFrom && address <= selTo
		const className = [
			'mem-cell',
			field === 'comment' ? 'mem-comment' : 'mem-value',
			selected ? 'selected' : '',
			selected && address === sel.focus ? 'focus' : '',
			field === 'value' && address === current ? 'current' : '',
			field === 'value' && /^0+$/.test(text) ? 'zero' : '',
		].join(' ')
		return (
			<div key={key} className={className} data-address={address} data-field={field}>
				{isEditing ? (
					<input
						className="mem-input"
						autoFocus
						spellCheck={false}
						value={editing.text}
						onFocus={(e) => (editing.text.length > 1 ? e.target.select() : undefined)}
						onChange={(e) => setEditing({ ...editing, text: e.target.value })}
						onBlur={() => {
							commit(editing)
							setEditing(null)
						}}
						onKeyDown={(e) => {
							e.stopPropagation()
							if (e.key === 'Enter') {
								e.preventDefault()
								finishEdit(e.shiftKey ? -1 : 1)
							} else if (e.key === 'Tab') {
								e.preventDefault()
								finishEdit(e.shiftKey ? -1 : 1)
							} else if (e.key === 'Escape') {
								e.preventDefault()
								setEditing(null)
								focusGrid()
							}
						}}
					/>
				) : (
					text
				)}
			</div>
		)
	}

	const header = single ? (
		<>
			<div className="mem-address">Address</div>
			<div className="mem-head-cell">Value</div>
			{cells?.disassembly && <div className="mem-head-cell left">Disassembly</div>}
			<div className="mem-head-cell left">Comment</div>
		</>
	) : (
		<>
			<div className="mem-address">Address</div>
			{Array.from({ length: cols }, (_, c) => (
				<div key={c} className="mem-head-cell">
					{c.toString(radix).toUpperCase()}
				</div>
			))}
			<div />
		</>
	)

	const selectionText =
		selCount > 1
			? `${formatAddress(selFrom, addressWidth, addressFormat)}–${formatAddress(selTo, addressWidth, addressFormat)} · ${selCount.toLocaleString()} cells`
			: `Address ${formatAddress(sel.focus, addressWidth, addressFormat)}`
	const readOnlyText = editable
		? null
		: info.simulating && info.type !== 'ram'
			? 'Read-only while simulating'
			: !info.simulating && info.type === 'ram'
				? 'Editable while simulating'
				: 'Read-only'

	return (
		<div className="memory-view">
			<div className="mem-toolbar">
				<div className="segmented small" role="radiogroup" aria-label="Cells per row">
					{LAYOUTS.map((n) => (
						<button
							key={n}
							type="button"
							className={n === cols ? 'selected' : ''}
							aria-pressed={n === cols}
							title={n === 1 ? 'One cell per row, with comments' : `${n} cells per row`}
							onClick={() => {
								setLayout(n)
								focusGrid()
							}}
						>
							{n === 1 ? 'List' : n}
						</button>
					))}
				</div>
				<div className="segmented small" role="radiogroup" aria-label="Number format">
					{(Object.keys(formatLabels) as ValueFormat[]).map((f) => (
						<button key={f} type="button" className={f === format ? 'selected' : ''} aria-pressed={f === format} onClick={() => {
							setFormat(f)
							focusGrid()
						}}>
							{formatLabels[f]}
						</button>
					))}
				</div>
				<form
					className="mem-goto"
					onSubmit={(e) => {
						e.preventDefault()
						doGoTo()
					}}
				>
					<input
						ref={goToInput}
						className="input"
						value={goTo}
						placeholder="Go to address"
						aria-label="Go to address"
						title={`Address in the selected format, or 0x… for hex (${isMac ? '⌘' : 'Ctrl+'}G)`}
						onChange={(e) => setGoTo(e.target.value)}
						onKeyDown={(e) => e.key === 'Escape' && (e.stopPropagation(), focusGrid())}
					/>
				</form>
				{info.simulating && (
					<button
						type="button"
						className="btn btn-sm"
						disabled={current < 0}
						title="Select the cell the simulation accesses"
						onClick={() => {
							select(current, false, 'value')
							focusGrid()
						}}
					>
						<Locate size={14} /> Current
					</button>
				)}
				<span className="mem-spacer" />
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void fill()} title="Fill a range of cells with a value">
					<PaintBucket size={14} /> Fill…
				</button>
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void clear()} title="Set all cells to zero and remove comments">
					<Eraser size={14} /> Clear
				</button>
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={importDump} title="Load an Antares or Logisim memory image">
					<Upload size={14} /> Import…
				</button>
				<button
					type="button"
					className={`btn btn-sm${exportMenu ? ' pressed' : ''}`}
					onClick={(e) => {
						const r = e.currentTarget.getBoundingClientRect()
						setExportMenu({ x: r.right - 240, y: r.bottom + 4 })
					}}
				>
					<Download size={14} /> Export
				</button>
			</div>
			{notice}
			<div ref={scroller} className="mem-scroller" onScroll={(e) => setScrollTop(e.currentTarget.scrollTop)}>
				<div
					ref={grid}
					className={`mem-grid${single ? ' single' : ''}${editable ? '' : ' readonly'}`}
					tabIndex={0}
					role="grid"
					aria-label="Memory cells"
					aria-readonly={!editable}
					onKeyDown={onKeyDown}
					onCopy={onCopy}
					onPaste={onPaste}
					onMouseDown={onMouseDown}
					onMouseOver={onMouseOver}
					onDoubleClick={onDoubleClick}
				>
					<div className="mem-head" style={{ gridTemplateColumns: columnsTemplate }}>
						{header}
					</div>
					<div className="mem-body" style={{ height: pageRows * ROW_H }}>
						{rows}
					</div>
				</div>
			</div>
			<div className="mem-status">
				<span className="mem-status-text">{selectionText}</span>
				{pages > 1 && (
					<span className="mem-pager">
						<button type="button" className="icon-btn small" disabled={page === 0} onClick={() => setPage(page - 1)} aria-label="Previous page">
							<ChevronLeft size={14} />
						</button>
						{formatAddress(pageStart * cols, addressWidth, addressFormat)}–{formatAddress(Math.min(maxAddress, (pageStart + pageRows) * cols - 1), addressWidth, addressFormat)}
						<button type="button" className="icon-btn small" disabled={page >= pages - 1} onClick={() => setPage(page + 1)} aria-label="Next page">
							<ChevronRight size={14} />
						</button>
					</span>
				)}
				<span className="mem-spacer" />
				{readOnlyText && <span className="mem-status-text">{readOnlyText}</span>}
				<span className="mem-status-text">
					{(maxAddress + 1).toLocaleString()} × {dataWidth} bit
				</span>
			</div>
			{exportMenu && <Menu entries={exportEntries} x={exportMenu.x} y={exportMenu.y} minWidth={240} onClose={() => setExportMenu(null)} />}
		</div>
	)
}

/** Binary addresses would be unreadably long, so they are shown in hex like in most hex editors. */
const addressFormatFor = (format: ValueFormat): ValueFormat => (format === 'bin' ? 'hex' : format)

/** Asks for the range and value to fill. */
function askFill(info: MemoryInfo, format: ValueFormat, from: number, to: number) {
	return dialogs.custom<{ from: number; to: number; value: string }>((done) => (
		<FillDialog info={info} format={format} from={from} to={to} done={done} />
	))
}

function FillDialog({
	info,
	format,
	from,
	to,
	done,
}: {
	info: MemoryInfo
	format: ValueFormat
	from: number
	to: number
	done: (value: { from: number; to: number; value: string } | null) => void
}) {
	const addressFormat = addressFormatFor(format)
	const fmt = (a: number) => formatAddress(a, info.addressWidth, addressFormat)
	const [start, setStart] = useState(fmt(from))
	const [end, setEnd] = useState(fmt(to))
	const [value, setValue] = useState('0')
	const a = parseAddress(start, addressFormat, info.maxAddress)
	const b = parseAddress(end, addressFormat, info.maxAddress)
	const error = a === null || b === null ? 'Enter valid addresses.' : a > b ? 'The first address must not be after the last.' : null
	return (
		<DialogFrame title="Fill cells" onCancel={() => done(null)}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					if (!error && a !== null && b !== null) done({ from: a, to: b, value })
				}}
			>
				<div className="dialog-body mem-fill">
					<label className="field">
						<span className="field-label">From address</span>
						<input className="input mono" value={start} onChange={(e) => setStart(e.target.value)} />
					</label>
					<label className="field">
						<span className="field-label">To address</span>
						<input className="input mono" value={end} onChange={(e) => setEnd(e.target.value)} />
					</label>
					<label className="field">
						<span className="field-label">Value ({formatLabels[format].toLowerCase()})</span>
						<input className="input mono" value={value} autoFocus onFocus={(e) => e.target.select()} onChange={(e) => setValue(e.target.value)} />
					</label>
					{error && <div className="field-error">{error}</div>}
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={() => done(null)}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary" disabled={!!error}>
						Fill
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}
