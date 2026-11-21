import { useState } from 'react'
import { useThemeName } from '../../hooks'
import { truthTableApi, inputValue, nextValue, outputValue, type TableData } from './api'

const CELL = 48
const MARGIN = 12
const SUBMAP_GAP = 32
const LABEL_LINE = 18

// Implicant colors of the desktop's KarnaughMapView, chosen for contrast in each theme
const LIGHT = ['#d62728', '#1f77b4', '#2ca02c', '#e67e00', '#9467bd', '#8c564b', '#e377c2', '#17a2b8']
const DARK = ['#ff6b68', '#6cb4ff', '#5fd35f', '#ffb454', '#c39bff', '#d9a27a', '#ff8fd0', '#4fd8e8']

/**
 * The Karnaugh map of one output with the implicants of its minimized function, like the desktop's
 * KarnaughMapView. Clicking a cell cycles its value 0 → 1 → X as an undoable edit.
 */
export function KarnaughMap({ table, editable }: { table: TableData; editable: boolean }) {
	const [output, setOutput] = useState(0)
	const [hover, setHover] = useState<number | null>(null)
	const dark = useThemeName() === 'dark'
	const palette = dark ? DARK : LIGHT
	const column = Math.min(output, table.outputs.length - 1)
	const map = truthTableApi.karnaugh(table.inputs.length + column)

	if (!map) return null
	if (map.tooManyInputs) {
		return <div className="empty-small">Karnaugh maps are available for truth tables with up to {map.maxInputs} inputs.</div>
	}

	const rowCount = map.rowCodes.length
	const columnCount = map.columnCodes.length
	const submaps = map.submapCodes.length
	const headerWidth = Math.max(56, 14 + 8 * Math.max(map.rowCodes[0].length, map.rowVariables.length + map.columnVariables.length))
	const titleHeight = submaps > 1 ? LABEL_LINE + 6 : 0
	const headerHeight = 2 * LABEL_LINE + 6
	const submapWidth = headerWidth + columnCount * CELL
	const width = 2 * MARGIN + submaps * submapWidth + (submaps - 1) * SUBMAP_GAP
	const height = 2 * MARGIN + titleHeight + headerHeight + rowCount * CELL
	const gridX = (s: number) => MARGIN + s * (submapWidth + SUBMAP_GAP) + headerWidth
	const gridY = MARGIN + titleHeight + headerHeight

	const cycle = (row: number) => {
		if (!editable) return
		truthTableApi.setCells([[row, table.inputs.length + column, nextValue(outputValue(table, column, row))]])
	}

	const assignment = (row: number) => table.inputs.map((name, i) => `${name}=${inputValue(table, i, row)}`).join(', ')

	return (
		<div className="kmap">
			<div className="kmap-header">
				{table.outputs.length > 1 && (
					<div className="segmented small" role="tablist" aria-label="Output">
						{table.outputs.map((o, i) => (
							<button
								key={i}
								type="button"
								role="tab"
								aria-selected={i === column}
								className={i === column ? 'selected' : ''}
								onClick={() => setOutput(i)}
							>
								{o.label}
							</button>
						))}
					</div>
				)}
				<span className="kmap-tip">
					{editable ? 'Click a cell to cycle its value through 0, 1 and X.' : 'Read-only.'}
					{hover !== null && <> · Row {hover}: {assignment(hover)}</>}
				</span>
			</div>
			<div className="kmap-scroll">
				<svg width={width} height={height} viewBox={`0 0 ${width} ${height}`} className="kmap-svg" role="img" aria-label="Karnaugh map">
					{map.submapCodes.map((code, s) => {
						const x0 = gridX(s)
						const left = x0 - headerWidth
						const top = gridY - headerHeight
						return (
							<g key={s}>
								{submaps > 1 && (
									<text x={x0 + (columnCount * CELL) / 2} y={top - 8} className="kmap-label" textAnchor="middle">
										{map.submapVariables} = {code}
									</text>
								)}
								<line x1={left + 4} y1={top + 4} x2={x0} y2={gridY} className="kmap-diagonal" />
								<text x={x0 - 6} y={top + 13} className="kmap-var" textAnchor="end">
									{map.columnVariables}
								</text>
								<text x={left + 4} y={gridY - 6} className="kmap-var">
									{map.rowVariables}
								</text>
								{map.columnCodes.map((c, i) => (
									<text key={i} x={x0 + i * CELL + CELL / 2} y={top + LABEL_LINE + 14} className="kmap-label" textAnchor="middle">
										{c}
									</text>
								))}
								{map.rowCodes.map((c, i) => (
									<text key={i} x={left + headerWidth / 2} y={gridY + i * CELL + CELL / 2 + 4} className="kmap-label" textAnchor="middle">
										{c}
									</text>
								))}
								{map.cells[s].map((cells, r) =>
									cells.map((row, c) => {
										const v = outputValue(table, column, row)
										return (
											<g
												key={`${r}:${c}`}
												className={`kmap-cell${editable ? ' editable' : ''}`}
												onClick={() => cycle(row)}
												onPointerEnter={() => setHover(row)}
												onPointerLeave={() => setHover(null)}
											>
												<rect x={x0 + c * CELL} y={gridY + r * CELL} width={CELL} height={CELL} />
												<text x={x0 + c * CELL + CELL / 2} y={gridY + r * CELL + CELL / 2 + 6} textAnchor="middle" className={`kmap-value v${v}`}>
													{v}
												</text>
												<text x={x0 + c * CELL + CELL - 4} y={gridY + r * CELL + CELL - 4} textAnchor="end" className="kmap-index">
													{row}
												</text>
											</g>
										)
									}),
								)}
							</g>
						)
					})}
					{map.implicants.map((imp, i) => (
						<Implicant
							key={i}
							index={i}
							color={palette[i % palette.length]}
							rects={imp.rectangles}
							gridX={gridX}
							gridY={gridY}
							rowCount={rowCount}
							columnCount={columnCount}
						/>
					))}
				</svg>
			</div>
			<div className="kmap-result">
				<code>{map.expression}</code>
				<ul className="kmap-legend">
					{map.implicants.map((imp, i) => (
						<li key={i}>
							<span className="kmap-swatch" style={{ background: palette[i % palette.length] }} />
							<code>{imp.text}</code>
						</li>
					))}
				</ul>
			</div>
		</div>
	)
}

/** Outlines the rectangles of one implicant; pieces of a term wrapping around an edge are drawn open towards it. */
function Implicant({
	index,
	color,
	rects,
	gridX,
	gridY,
	rowCount,
	columnCount,
}: {
	index: number
	color: string
	rects: Array<{ submap: number; row: number; column: number; rowSpan: number; columnSpan: number }>
	gridX: (submap: number) => number
	gridY: number
	rowCount: number
	columnCount: number
}) {
	const inset = 4 + (index % 4) * 3
	const overhang = CELL / 2
	const bySubmap = new Map<number, typeof rects>()
	rects.forEach((r) => bySubmap.set(r.submap, [...(bySubmap.get(r.submap) ?? []), r]))
	return (
		<g pointerEvents="none">
			{[...bySubmap].map(([submap, pieces]) => {
				const wrapsRows = new Set(pieces.map((p) => p.row)).size > 1
				const wrapsColumns = new Set(pieces.map((p) => p.column)).size > 1
				const x0 = gridX(submap)
				const clipId = `kmap-clip-${index}-${submap}`
				return (
					<g key={submap}>
						<clipPath id={clipId}>
							<rect x={x0} y={gridY} width={columnCount * CELL} height={rowCount * CELL} />
						</clipPath>
						{pieces.map((p, i) => {
							let x = x0 + p.column * CELL + inset
							let y = gridY + p.row * CELL + inset
							let w = p.columnSpan * CELL - 2 * inset
							let h = p.rowSpan * CELL - 2 * inset
							if (wrapsColumns && p.column === 0) {
								x -= overhang
								w += overhang
							}
							if (wrapsColumns && p.column + p.columnSpan === columnCount) w += overhang
							if (wrapsRows && p.row === 0) {
								y -= overhang
								h += overhang
							}
							if (wrapsRows && p.row + p.rowSpan === rowCount) h += overhang
							return (
								<rect
									key={i}
									x={x}
									y={y}
									width={w}
									height={h}
									rx={9}
									clipPath={`url(#${clipId})`}
									fill={color}
									fillOpacity={0.12}
									stroke={color}
									strokeWidth={2.25}
								/>
							)
						})}
					</g>
				)
			})}
		</g>
	)
}
