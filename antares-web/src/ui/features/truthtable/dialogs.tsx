// Dialogs of the truth table feature, mirroring the desktop's NewTruthTablePanel, ImportCSVPanel,
// CreateCircuitFromTruthTablePanel, AnalyseCircuitPanel and SaveTruthTablePanel.
import { useEffect, useRef, useState, type FormEvent, type ReactNode } from 'react'
import { Plus, Save, Sigma, Trash2 } from 'lucide-react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { askNewItemName, download, itemNames } from '../../projectActions'
import { TruthTableGrid } from './TruthTableGrid'
import { truthTableApi, type ColumnSpec, type TableData, type TableSource } from './api'

function uniqueName(base: string, existing: string[]): string {
	if (!existing.includes(base)) return base
	let i = 2
	while (existing.includes(`${base} ${i}`)) i++
	return `${base} ${i}`
}

const safeFileName = (name: string, fallback: string) => name.replace(/[^\w\- ]+/g, '').trim() || fallback

/** A form dialog with an error line and Cancel/confirm buttons; [submit] returns an error message or ''. */
function FormDialog({
	title,
	confirmLabel,
	onCancel,
	submit,
	children,
	wide,
	disabled,
}: {
	title: string
	confirmLabel: string
	onCancel: () => void
	submit: () => string | Promise<string>
	children: ReactNode
	wide?: boolean
	disabled?: boolean
}) {
	const [error, setError] = useState('')
	const onSubmit = async (e: FormEvent) => {
		e.preventDefault()
		setError(await submit())
	}
	return (
		<DialogFrame title={title} onCancel={onCancel} wide={wide}>
			<form onSubmit={(e) => void onSubmit(e)}>
				<div className="dialog-body tt-form">
					{children}
					{error && (
						<div className="field-error" role="alert">
							{error}
						</div>
					)}
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={onCancel}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary" disabled={disabled}>
						{confirmLabel}
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}

function Check({ label, checked, onChange, disabled }: { label: string; checked: boolean; onChange: (v: boolean) => void; disabled?: boolean }) {
	return (
		<label className={`tt-check${disabled ? ' disabled' : ''}`}>
			<input type="checkbox" checked={checked} disabled={disabled} onChange={(e) => onChange(e.target.checked)} />
			<span>{label}</span>
		</label>
	)
}

/** ---- New truth table / expressions */

export async function newTruthTable(): Promise<void> {
	await dialogs.custom<boolean>((done) => <NewTruthTableDialog done={done} />)
}

function NewTruthTableDialog({ done }: { done: (v: boolean | null) => void }) {
	const [name, setName] = useState(() => uniqueName('Truth table', itemNames('truthTable')))
	const [inputs, setInputs] = useState('A, B')
	const [outputs, setOutputs] = useState('Y')
	const nameInput = useRef<HTMLInputElement>(null)
	useEffect(() => nameInput.current?.select(), [])
	return (
		<FormDialog
			title="New truth table"
			confirmLabel="Create truth table"
			onCancel={() => done(null)}
			submit={() => {
				if (itemNames('truthTable').includes(name.trim())) return 'An item with this name already exists.'
				const error = truthTableApi.createTruthTable(name.trim(), inputs, outputs)
				if (!error) done(true)
				return error
			}}
		>
			<label className="field">
				<span className="field-label">Name</span>
				<input ref={nameInput} className="input" value={name} autoFocus onChange={(e) => setName(e.target.value)} />
			</label>
			<label className="field">
				<span className="field-label">Inputs (comma-separated, up to 8)</span>
				<input className="input mono" value={inputs} onChange={(e) => setInputs(e.target.value)} placeholder="A, B, C" />
			</label>
			<label className="field">
				<span className="field-label">Outputs (comma-separated, prefix ! for inverted)</span>
				<input className="input mono" value={outputs} onChange={(e) => setOutputs(e.target.value)} placeholder="Y, !Z" />
			</label>
		</FormDialog>
	)
}

export async function newExpression(): Promise<void> {
	const name = await askNewItemName('New boolean expressions', 'expression', 'Expressions', 'Create expressions')
	if (!name) return
	const error = truthTableApi.createExpression(name)
	if (error) engine.message('error', 'New boolean expressions', error)
}

/** ---- Columns of a truth table */

interface ColumnRow extends ColumnSpec {
	key: number
}

export async function editColumns(table: TableData): Promise<void> {
	await dialogs.custom<boolean>((done) => <ColumnsDialog table={table} done={done} />)
}

function ColumnsDialog({ table, done }: { table: TableData; done: (v: boolean | null) => void }) {
	const key = useRef(0)
	const rows = (names: string[]): ColumnRow[] => names.map((name, i) => ({ name, from: i, key: key.current++ }))
	const [inputs, setInputs] = useState(() => rows(table.inputs))
	const [outputs, setOutputs] = useState(() => rows(table.outputs.map((o) => o.name)))

	const list = (title: string, items: ColumnRow[], set: (v: ColumnRow[]) => void, base: string, max: number) => {
		const free = () => {
			const used = new Set([...inputs, ...outputs].map((c) => c.name))
			let i = items.length
			while (used.has(`${base}${i}`)) i++
			return `${base}${i}`
		}
		return (
			<section className="tt-columns">
				<header>
					<h3>{title}</h3>
					<button
						type="button"
						className="btn btn-sm"
						disabled={items.length >= max}
						onClick={() => set([...items, { name: free(), from: null, key: key.current++ }])}
					>
						<Plus size={14} /> Add
					</button>
				</header>
				{items.map((c, i) => (
					<div key={c.key} className="tt-column-row">
						<input
							className="input mono"
							value={c.name}
							aria-label={`${title} ${i + 1}`}
							onChange={(e) => set(items.map((x) => (x.key === c.key ? { ...x, name: e.target.value } : x)))}
						/>
						<span className="tt-column-hint">{c.from === null ? 'new' : ''}</span>
						<button
							type="button"
							className="icon-btn small"
							aria-label={`Remove ${c.name}`}
							title="Remove"
							disabled={items.length <= 1}
							onClick={() => set(items.filter((x) => x.key !== c.key))}
						>
							<Trash2 size={14} />
						</button>
					</div>
				))}
			</section>
		)
	}

	const removedInputs = table.inputs.length - inputs.filter((c) => c.from !== null).length
	return (
		<FormDialog
			title="Inputs and outputs"
			confirmLabel="Apply"
			onCancel={() => done(null)}
			submit={() => {
				const strip = (c: ColumnRow): ColumnSpec => ({ name: c.name.trim(), from: c.from })
				const error = truthTableApi.restructure(inputs.map(strip), outputs.map(strip))
				if (!error) done(true)
				return error
			}}
		>
			<div className="tt-columns-grid">
				{list('Inputs', inputs, setInputs, 'I', 8)}
				{list('Outputs', outputs, setOutputs, 'O', 8)}
			</div>
			<p className="tt-note">
				Values of existing outputs are kept.
				{removedInputs > 0 && ' Removed inputs keep the rows where they were 0.'} The change can be undone.
			</p>
		</FormDialog>
	)
}

/** ---- CSV import and export */

const CSV_KEY = 'antares.truthTableCsv'

export async function importCsv(): Promise<void> {
	await dialogs.custom<boolean>((done) => <CsvImportDialog done={done} />)
}

function CsvImportDialog({ done }: { done: (v: boolean | null) => void }) {
	const stored = (() => {
		try {
			return JSON.parse(localStorage.getItem(CSV_KEY) ?? '{}') as { headers?: boolean; inputColumns?: boolean }
		} catch {
			return {}
		}
	})()
	const [headers, setHeaders] = useState(stored.headers ?? true)
	const [inputColumns, setInputColumns] = useState(stored.inputColumns ?? false)
	const [file, setFile] = useState<File | null>(null)
	return (
		<FormDialog
			title="Import CSV"
			confirmLabel="Import"
			disabled={!file}
			onCancel={() => done(null)}
			submit={async () => {
				if (!file) return ''
				try {
					localStorage.setItem(CSV_KEY, JSON.stringify({ headers, inputColumns }))
				} catch {
					// Storage unavailable
				}
				const error = truthTableApi.importCsv(await file.text(), headers, inputColumns)
				if (!error) {
					done(true)
					engine.message('success', '', `Imported ${file.name}`)
				}
				return error
			}}
		>
			<label className="field">
				<span className="field-label">File</span>
				<input className="tt-file" type="file" accept=".csv,text/csv" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
			</label>
			<Check label="First line contains column names" checked={headers} onChange={setHeaders} />
			<Check label="Lines contain the input values" checked={inputColumns} onChange={setInputColumns} />
			<p className="tt-note">
				Values are 0, 1 or X. {inputColumns ? 'Rows are matched by their input values.' : 'Lines fill the rows in order.'}{' '}
				{headers ? 'Columns are matched by name.' : inputColumns ? 'Inputs come first, then outputs.' : 'Each line holds the outputs only.'}
			</p>
		</FormDialog>
	)
}

export function exportCsv(name: string) {
	download(`${safeFileName(name, 'truth-table')}.csv`, truthTableApi.exportCsv(), 'text/csv')
}

/** ---- Synthesis */

export async function createCircuit(source: TableSource, suggestedName: string, sequential = false): Promise<void> {
	await dialogs.custom<boolean>((done) => (
		<SynthesisDialog source={source} suggestedName={suggestedName} sequential={sequential} done={done} />
	))
}

function SynthesisDialog({
	source,
	suggestedName,
	sequential,
	done,
}: {
	source: TableSource
	suggestedName: string
	sequential: boolean
	done: (v: boolean | null) => void
}) {
	const types = truthTableApi.synthesisTypes(source)
	const [name, setName] = useState(() => uniqueName(suggestedName, itemNames('circuit')))
	const [type, setType] = useState(types[0]?.id ?? '')
	const [twoInput, setTwoInput] = useState(false)
	const [testcase, setTestcase] = useState(true)
	const supportsTwoInput = types.find((t) => t.id === type)?.twoInputGatesOnly ?? false
	return (
		<FormDialog
			title="Circuit from truth table"
			confirmLabel="Create circuit"
			disabled={!name.trim()}
			onCancel={() => done(null)}
			submit={() => {
				if (itemNames('circuit').includes(name.trim())) return 'A circuit with this name already exists.'
				const error = truthTableApi.synthesize(source, name, type, supportsTwoInput && twoInput, !sequential && testcase)
				if (!error) done(true)
				return error
			}}
		>
			<label className="field">
				<span className="field-label">Circuit name</span>
				<input className="input" value={name} autoFocus onChange={(e) => setName(e.target.value)} />
			</label>
			<label className="field">
				<span className="field-label">Circuit type</span>
				<select className="input select" value={type} onChange={(e) => setType(e.target.value)}>
					{types.map((t) => (
						<option key={t.id} value={t.id}>
							{t.label}
						</option>
					))}
				</select>
			</label>
			<Check label="Two-input gates only" checked={supportsTwoInput && twoInput} disabled={!supportsTwoInput} onChange={setTwoInput} />
			{!sequential && <Check label="Create test case from truth table" checked={testcase} onChange={setTestcase} />}
		</FormDialog>
	)
}

/** ---- Saving a truth table or its expressions as new project items */

export async function saveAs(source: TableSource, kind: 'truthTable' | 'expression', base: string, open = false): Promise<boolean> {
	const title = kind === 'truthTable' ? 'Save truth table' : 'Save expressions'
	const name = await askNewItemName(title, kind, base, 'Save')
	if (!name) return false
	const error = truthTableApi.saveAs(source, kind, name, open)
	if (error) {
		engine.message('error', title, error)
		return false
	}
	engine.message('success', '', `Saved “${name}”`)
	return true
}

/** ---- Circuit analysis */

export function analyseCircuit() {
	const result = truthTableApi.analyse()
	if (!result) return
	if (result.error !== null) {
		const message = result.error
		void dialogs.custom<boolean>((done) => (
			<DialogFrame title="Analyse circuit" onCancel={() => done(null)}>
				<div className="dialog-body tt-prewrap">{message}</div>
				<div className="dialog-actions">
					<button type="button" className="btn btn-primary" autoFocus onClick={() => done(null)}>
						OK
					</button>
				</div>
			</DialogFrame>
		))
		return
	}
	void dialogs.custom<boolean>((done) => (
		<AnalysisDialog name={result.name} table={result.table} expressions={result.expressions} done={done} />
	))
}

function AnalysisDialog({
	name,
	table,
	expressions,
	done,
}: {
	name: string
	table: TableData
	expressions: string[]
	done: (v: boolean | null) => void
}) {
	return (
		<DialogFrame title={`Analysis of ${name}`} onCancel={() => done(null)} wide>
			<div className="dialog-body tt-analysis">
				<p className="tt-note">
					{table.inputs.length} {table.inputs.length === 1 ? 'input' : 'inputs'}, {table.outputs.length}{' '}
					{table.outputs.length === 1 ? 'output' : 'outputs'}, {table.rows} rows, computed by simulating every input combination.
				</p>
				<div className="tt-analysis-table">
					<TruthTableGrid table={table} />
				</div>
				<h3 className="tt-section-title">Minimized expressions</h3>
				<ExpressionList expressions={expressions} />
			</div>
			<div className="dialog-actions tt-analysis-actions">
				<button type="button" className="btn" onClick={() => void saveAs('analysis', 'truthTable', `${name} table`, true).then((ok) => ok && done(true))}>
					<Save size={14} /> Save truth table…
				</button>
				<button type="button" className="btn" onClick={() => void saveAs('analysis', 'expression', `${name} expressions`, true).then((ok) => ok && done(true))}>
					<Sigma size={14} /> Save expressions…
				</button>
				<span className="status-spacer" />
				<button type="button" className="btn btn-primary" autoFocus onClick={() => done(null)}>
					Close
				</button>
			</div>
		</DialogFrame>
	)
}

/** Minimized expressions, one per line, selectable for copying. */
export function ExpressionList({ expressions }: { expressions: string[] }) {
	if (expressions.length === 0) return <div className="empty-small">No outputs.</div>
	return (
		<pre className="tt-expressions">
			{expressions.map((e, i) => (
				<code key={i}>{e}</code>
			))}
		</pre>
	)
}
