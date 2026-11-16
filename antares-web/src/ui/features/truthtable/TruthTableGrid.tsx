import { useEffect, useRef, useState, type KeyboardEvent, type PointerEvent } from 'react'
import { engine } from '../../../core/engine'
import { truthTableApi, inputValue, nextValue, outputValue, type CellValue, type TableData } from './api'

/** A rectangular range of output cells; columns are output indices. */
interface Range {
	row1: number
	col1: number
	row2: number
	col2: number
}

const norm = (r: Range) => ({
	top: Math.min(r.row1, r.row2),
	bottom: Math.max(r.row1, r.row2),
	left: Math.min(r.col1, r.col2),
	right: Math.max(r.col1, r.col2),
})

/**
 * The truth table grid: inputs (read-only, derived from the row) and outputs. When [editable], clicking an output
 * cycles 0 → 1 → X, dragging or Shift-clicking selects a block, and the keys 0, 1, X, Space, arrows, Mod+C/V work
 * on the selection like in the desktop's table.
 */
export function TruthTableGrid({ table, editable = false }: { table: TableData; editable?: boolean }) {
	const [sel, setSel] = useState<Range | null>(null)
	const drag = useRef<{ row: number; col: number; moved: boolean } | null>(null)
	const grid = useRef<HTMLDivElement>(null)
	const inputs = table.inputs.length
	const outputs = table.outputs.length
	const rows = table.truncated ? Math.min(table.rows, table.outputs[0]?.values.length ?? 0) : table.rows

	// Keep the selection inside the table when columns change
	useEffect(() => {
		if (sel && (sel.row1 >= rows || sel.row2 >= rows || sel.col1 >= outputs || sel.col2 >= outputs)) setSel(null)
	}, [rows, outputs, sel])

	const selected = (row: number, col: number) => {
		if (!sel) return false
		const r = norm(sel)
		return row >= r.top && row <= r.bottom && col >= r.left && col <= r.right
	}

	const scrollTo = (row: number, col: number) =>
		grid.current?.querySelector(`[data-cell="${row}:${col}"]`)?.scrollIntoView({ block: 'nearest', inline: 'nearest' })

	const select = (r: Range) => {
		setSel(r)
		scrollTo(r.row2, r.col2)
	}

	/** Fills the selection with [value]; a single cell then advances like the desktop's table. */
	const fill = (value: CellValue) => {
		if (!sel) return
		const r = norm(sel)
		const cells: Array<[number, number, CellValue]> = []
		for (let row = r.top; row <= r.bottom; row++)
			for (let col = r.left; col <= r.right; col++) cells.push([row, inputs + col, value])
		truthTableApi.setCells(cells)
		if (r.top === r.bottom && r.left === r.right) {
			const next = r.left + 1 < outputs ? { row: r.top, col: r.left + 1 } : { row: (r.top + 1) % rows, col: 0 }
			select({ row1: next.row, col1: next.col, row2: next.row, col2: next.col })
		}
	}

	const cycle = (row: number, col: number) =>
		truthTableApi.setCells([[row, inputs + col, nextValue(outputValue(table, col, row))]])

	const selectionText = () => {
		if (!sel) return ''
		const r = norm(sel)
		const lines: string[] = []
		for (let row = r.top; row <= r.bottom; row++) {
			const cells: string[] = []
			for (let col = r.left; col <= r.right; col++) cells.push(outputValue(table, col, row))
			lines.push(cells.join('\t'))
		}
		return lines.join('\n')
	}

	const onKeyDown = (e: KeyboardEvent) => {
		if (!sel) return
		const key = e.key.toLowerCase()
		const mod = e.metaKey || e.ctrlKey
		const move = (dr: number, dc: number) => {
			e.preventDefault()
			const row = Math.max(0, Math.min(rows - 1, sel.row2 + dr))
			const col = Math.max(0, Math.min(outputs - 1, sel.col2 + dc))
			select(e.shiftKey ? { ...sel, row2: row, col2: col } : { row1: row, col1: col, row2: row, col2: col })
		}
		if (key === 'arrowdown') return move(1, 0)
		if (key === 'arrowup') return move(-1, 0)
		if (key === 'arrowleft') return move(0, -1)
		if (key === 'arrowright') return move(0, 1)
		if (key === 'escape') {
			e.stopPropagation()
			return setSel(null)
		}
		if (mod || !editable) return
		if (key === '0' || key === '1' || key === 'x') {
			e.preventDefault()
			fill(key.toUpperCase() as CellValue)
		} else if (key === ' ' || key === 'enter') {
			e.preventDefault()
			const r = norm(sel)
			if (r.top === r.bottom && r.left === r.right) cycle(r.top, r.left)
		}
	}

	// Clipboard via the browser's events, so the system clipboard is used
	useEffect(() => {
		const el = grid.current
		if (!el) return
		const onCopy = (e: ClipboardEvent) => {
			if (!sel) return
			e.preventDefault()
			e.stopPropagation()
			e.clipboardData?.setData('text/plain', selectionText())
		}
		const onPaste = (e: ClipboardEvent) => {
			e.preventDefault()
			e.stopPropagation()
			if (!sel || !editable) return
			const r = norm(sel)
			const text = e.clipboardData?.getData('text/plain') ?? ''
			// A single value fills the whole selection
			if (/^\s*[01xX]\s*$/.test(text)) return fill(text.trim().toUpperCase() as CellValue)
			const error = truthTableApi.paste(r.top, inputs + r.left, text)
			if (error) engine.message('error', 'Paste', error)
		}
		el.addEventListener('copy', onCopy)
		el.addEventListener('paste', onPaste)
		return () => {
			el.removeEventListener('copy', onCopy)
			el.removeEventListener('paste', onPaste)
		}
	})

	const onPointerDown = (e: PointerEvent, row: number, col: number) => {
		if (e.button !== 0) return
		grid.current?.focus({ preventScroll: true })
		if (e.shiftKey && sel) {
			setSel({ ...sel, row2: row, col2: col })
			return
		}
		drag.current = { row, col, moved: false }
		setSel({ row1: row, col1: col, row2: row, col2: col })
	}

	const onPointerEnter = (row: number, col: number) => {
		const d = drag.current
		if (!d) return
		if (row !== d.row || col !== d.col) d.moved = true
		setSel({ row1: d.row, col1: d.col, row2: row, col2: col })
	}

	useEffect(() => {
		const up = () => {
			const d = drag.current
			drag.current = null
			if (d && !d.moved && editable) cycle(d.row, d.col)
		}
		window.addEventListener('pointerup', up)
		return () => window.removeEventListener('pointerup', up)
	})

	const rowIndices = Array.from({ length: rows }, (_, i) => i)
	return (
		<div
			ref={grid}
			className={`tt-grid${editable ? ' editable' : ''}`}
			tabIndex={0}
			onKeyDown={onKeyDown}
			role="grid"
			aria-label="Truth table"
			aria-readonly={!editable}
		>
			<table>
				<thead>
					<tr>
						<th className="tt-index">#</th>
						{table.inputs.map((name, i) => (
							<th key={`i${i}`} className={`tt-in${i === inputs - 1 ? ' tt-last-in' : ''}`} title={name}>
								{name}
							</th>
						))}
						{table.outputs.map((o, i) => (
							<th key={`o${i}`} className="tt-out" title={o.name}>
								{o.label}
							</th>
						))}
					</tr>
				</thead>
				<tbody>
					{rowIndices.map((row) => (
						<tr key={row} className={(row & 4) !== 0 ? 'tt-band' : undefined}>
							<td className="tt-index">{row}</td>
							{table.inputs.map((_, i) => (
								<td key={`i${i}`} className={`tt-in${i === inputs - 1 ? ' tt-last-in' : ''}`}>
									{inputValue(table, i, row)}
								</td>
							))}
							{table.outputs.map((_, col) => {
								const v = outputValue(table, col, row)
								return (
									<td
										key={`o${col}`}
										data-cell={`${row}:${col}`}
										className={`tt-out v${v}${selected(row, col) ? ' selected' : ''}`}
										onPointerDown={(e) => onPointerDown(e, row, col)}
										onPointerEnter={() => onPointerEnter(row, col)}
										aria-selected={selected(row, col)}
									>
										{v}
									</td>
								)
							})}
						</tr>
					))}
				</tbody>
			</table>
			{table.truncated && <div className="hint subtle">Showing the first {rows} of {table.rows} rows.</div>}
		</div>
	)
}
