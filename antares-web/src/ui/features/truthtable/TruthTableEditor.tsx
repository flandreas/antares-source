import { Columns3, Cpu, FileDown, FileUp, Grid2x2, Sigma, Table2 } from 'lucide-react'
import { useEngineVersion, useStoredState } from '../../hooks'
import type { DocumentEditorProps } from '../registry'
import { truthTableApi } from './api'
import { NotationMenu, UndoButtons, useDocumentUndo } from './common'
import { createCircuit, editColumns, exportCsv, ExpressionList, importCsv, saveAs } from './dialogs'
import { KarnaughMap } from './KarnaughMap'
import { TruthTableGrid } from './TruthTableGrid'

/** The truth table document: table and Karnaugh map views, minimized expressions and synthesis. */
export function TruthTableEditor({ state }: DocumentEditorProps) {
	useEngineVersion()
	useDocumentUndo()
	const [view, setView] = useStoredState<'table' | 'karnaugh'>('antares.truthTableView', 'table')
	const table = truthTableApi.table()
	if (!table) return null
	const editable = !!state.document?.editable
	const sequential = table.stateColumnCount > 0
	const expressions = truthTableApi.expressions()

	return (
		<div className="tt-editor">
			<div className="tt-toolbar">
				<div className="segmented small" role="tablist" aria-label="View">
					<button type="button" role="tab" aria-selected={view === 'table'} className={view === 'table' ? 'selected' : ''} onClick={() => setView('table')}>
						<Table2 size={14} /> Table
					</button>
					<button
						type="button"
						role="tab"
						aria-selected={view === 'karnaugh'}
						className={view === 'karnaugh' ? 'selected' : ''}
						onClick={() => setView('karnaugh')}
					>
						<Grid2x2 size={14} /> Karnaugh map
					</button>
				</div>
				<UndoButtons state={state} />
				<span className="tt-spacer" />
				<button
					type="button"
					className="btn btn-sm"
					disabled={!editable || sequential}
					onClick={() => void editColumns(table)}
					title={sequential ? 'The columns of state tables can’t be changed' : 'Add, remove or rename inputs and outputs'}
				>
					<Columns3 size={14} /> Columns…
				</button>
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void importCsv()}>
					<FileUp size={14} /> Import CSV…
				</button>
				<button type="button" className="btn btn-sm" onClick={() => exportCsv(table.name)}>
					<FileDown size={14} /> Export CSV
				</button>
				<button type="button" className="btn btn-sm btn-primary" onClick={() => void createCircuit('truthTable', table.name, sequential)}>
					<Cpu size={14} /> Create circuit…
				</button>
			</div>

			<div className="tt-body">
				<div className="tt-main">
					{view === 'table' ? (
						<>
							<p className="tt-tip">
								{editable
									? 'Click an output to cycle 0, 1 and X (don’t care). Drag to select; type 0, 1 or X to fill, ⌘C/⌘V to copy and paste.'
									: 'Read-only.'}
							</p>
							<TruthTableGrid table={table} editable={editable} />
						</>
					) : (
						<KarnaughMap table={table} editable={editable} />
					)}
				</div>
				<aside className="tt-side">
					<div className="tt-side-header">
						<h3 className="tt-section-title">Minimized expressions</h3>
						<NotationMenu />
					</div>
					<ExpressionList expressions={expressions} />
					<button type="button" className="btn btn-sm" onClick={() => void saveAs('truthTable', 'expression', `${table.name} expressions`, true)}>
						<Sigma size={14} /> Save as expressions…
					</button>
				</aside>
			</div>
		</div>
	)
}
