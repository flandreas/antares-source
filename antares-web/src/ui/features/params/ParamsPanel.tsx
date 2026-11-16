import { useState } from 'react'
import { Plus, SlidersHorizontal, Trash2 } from 'lucide-react'
import { dialogs } from '../../Dialogs'
import type { DocumentEditorProps } from '../registry'
import { paramsApi, type ParamDefinition, type ParamDefinitions, type ParamDraft } from './api'

/**
 * Parameter definitions of the open circuit (desktop: "Parameter Definitions" dialog of the circuit's
 * "Parameters" property). Each saved change is undoable.
 */
export function ParamsPanel(_: DocumentEditorProps) {
	const defs = paramsApi.definitions()
	const [selectedName, setSelectedName] = useState<string | null>(null)
	const [adding, setAdding] = useState(false)

	if (!defs) return <div className="inspector-empty">Open a circuit to define its parameters.</div>

	const selected = adding ? null : (defs.items.find((d) => d.name === selectedName) ?? null)
	const editable = defs.editable

	const remove = async (d: ParamDefinition) => {
		const ok = await dialogs.confirm({
			title: 'Remove parameter',
			message: `Remove the parameter “${d.name}”? Expressions using it will no longer evaluate. You can undo this.`,
			confirmLabel: 'Remove',
			danger: true,
		})
		if (!ok) return
		paramsApi.remove(d.name)
		setSelectedName(null)
	}

	return (
		<div className="panel-scroll inspector-body">
			<section className="prop-group">
				<div className="params-head">
					<h3>Parameter definitions</h3>
					<button
						type="button"
						className="btn btn-sm"
						disabled={!editable}
						onClick={() => {
							setAdding(true)
							setSelectedName(null)
						}}
					>
						<Plus size={14} /> Add
					</button>
				</div>
				{!editable && <div className="hint params-hint">{readOnlyReason()}</div>}
				{defs.items.length === 0 && !adding ? (
					<div className="params-empty">
						<SlidersHorizontal size={18} />
						<p>
							Parameters make this circuit generic. Define e.g. a bit width <code>n</code> and use it as <code>=n</code> in
							properties of its parts; each placed instance can then set its own value.
						</p>
					</div>
				) : (
					<ul className="params-list" role="listbox" aria-label="Parameters">
						{defs.items.map((d) => (
							<li key={d.name}>
								<button
									type="button"
									role="option"
									aria-selected={selected?.name === d.name}
									className={`params-item${selected?.name === d.name ? ' selected' : ''}`}
									onClick={() => {
										setAdding(false)
										setSelectedName(selected?.name === d.name ? null : d.name)
									}}
								>
									<span className="params-name">{d.name}</span>
									<span className="params-meta">
										{d.typeLabel} · {d.valueLabel || '""'}
										{d.semantic && ` · ${d.semanticLabel}`}
									</span>
								</button>
							</li>
						))}
						{adding && (
							<li>
								<div className="params-item selected params-new">New parameter</div>
							</li>
						)}
					</ul>
				)}
			</section>
			{(adding || selected) && (
				<section className="prop-group">
					<h3>{adding ? 'New parameter' : `Parameter “${selected!.name}”`}</h3>
					<ParamForm
						// Reset the draft when the definition changes, e.g. by undo
						key={adding ? '+' : JSON.stringify(selected)}
						defs={defs}
						definition={selected}
						editable={editable}
						onSaved={(name) => {
							setAdding(false)
							setSelectedName(name)
						}}
						onCancel={() => {
							setAdding(false)
							setSelectedName(null)
						}}
						onRemove={selected ? () => void remove(selected) : undefined}
					/>
				</section>
			)}
		</div>
	)
}

function readOnlyReason() {
	return 'Parameters can only be changed in edit mode, for circuits of the project.'
}

function uniqueName(defs: ParamDefinitions) {
	for (const base of ['n', 'm', 'k']) {
		if (!defs.items.some((d) => d.name === base)) return base
	}
	let i = 1
	while (defs.items.some((d) => d.name === `p${i}`)) i++
	return `p${i}`
}

function ParamForm({
	defs,
	definition,
	editable,
	onSaved,
	onCancel,
	onRemove,
}: {
	defs: ParamDefinitions
	definition: ParamDefinition | null
	editable: boolean
	onSaved: (name: string) => void
	onCancel: () => void
	onRemove?: () => void
}) {
	const firstType = defs.types[0]
	const [draft, setDraft] = useState<ParamDraft>(() =>
		definition
			? {
					name: definition.name,
					type: definition.type,
					value: definition.value,
					semantic: definition.semantic ?? '',
					description: definition.description,
				}
			: { name: uniqueName(defs), type: firstType?.id ?? '', value: firstType?.initial ?? '', semantic: '', description: '' },
	)
	const [error, setError] = useState<string | null>(null)
	const type = defs.types.find((t) => t.id === draft.type)
	const dirty =
		!definition ||
		draft.name !== definition.name ||
		draft.type !== definition.type ||
		draft.value !== definition.value ||
		draft.semantic !== (definition.semantic ?? '') ||
		draft.description !== definition.description

	const update = (patch: Partial<ParamDraft>) => {
		setDraft((d) => ({ ...d, ...patch }))
		setError(null)
	}

	const save = () => {
		const err = paramsApi.save(definition?.name ?? '', draft)
		if (err) setError(err)
		else onSaved(draft.name.trim())
	}

	return (
		<form
			className="params-form"
			onSubmit={(e) => {
				e.preventDefault()
				if (editable && dirty) save()
			}}
		>
			<Row label="Name">
				<input
					className="input params-mono"
					value={draft.name}
					disabled={!editable}
					spellCheck={false}
					autoFocus={!definition}
					onChange={(e) => update({ name: e.target.value })}
				/>
			</Row>
			<Row label="Type">
				<select
					className="input select"
					value={draft.type}
					disabled={!editable}
					onChange={(e) => {
						const t = defs.types.find((x) => x.id === e.target.value)
						update({ type: e.target.value, value: t?.initial ?? '' })
					}}
				>
					{defs.types.map((t) => (
						<option key={t.id} value={t.id}>
							{t.label}
						</option>
					))}
				</select>
			</Row>
			<Row label="Default value">
				{type?.choices ? (
					<div className="select-wrap">
						{type.choices.some((c) => c.color) && (
							<span className="color-dot" style={{ background: type.choices.find((c) => c.id === draft.value)?.color ?? 'transparent' }} />
						)}
						<select className="input select" value={draft.value} disabled={!editable} onChange={(e) => update({ value: e.target.value })}>
							{type.choices.map((c) => (
								<option key={c.id} value={c.id}>
									{c.label}
								</option>
							))}
						</select>
					</div>
				) : (
					<input
						className="input"
						value={draft.value}
						disabled={!editable}
						inputMode={draft.type === 'Long' ? 'numeric' : undefined}
						onChange={(e) => update({ value: e.target.value })}
					/>
				)}
			</Row>
			<Row label="Semantic">
				<select className="input select" value={draft.semantic} disabled={!editable} onChange={(e) => update({ semantic: e.target.value })}>
					<option value="">None</option>
					{defs.semantics.map((s) => (
						<option key={s.id} value={s.id}>
							{s.label}
						</option>
					))}
				</select>
			</Row>
			<div className="prop-row stacked">
				<span className="prop-label">Description</span>
				<textarea
					className="input params-desc"
					rows={3}
					value={draft.description}
					disabled={!editable}
					onChange={(e) => update({ description: e.target.value })}
				/>
			</div>
			{error && <div className="field-error">{error}</div>}
			{editable && (
				<div className="params-actions">
					{onRemove && (
						<button type="button" className="icon-btn params-remove" onClick={onRemove} title="Remove parameter" aria-label="Remove parameter">
							<Trash2 size={15} />
						</button>
					)}
					<span className="params-spacer" />
					<button type="button" className="btn btn-sm" onClick={onCancel}>
						Cancel
					</button>
					<button type="submit" className="btn btn-sm btn-primary" disabled={!dirty}>
						{definition ? 'Apply' : 'Add'}
					</button>
				</div>
			)}
		</form>
	)
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
	return (
		<label className="prop-row">
			<span className="prop-label">{label}</span>
			<span className="prop-control">{children}</span>
		</label>
	)
}
