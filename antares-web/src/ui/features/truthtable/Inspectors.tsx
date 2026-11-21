import { useEffect, useState } from 'react'
import { useEngineVersion } from '../../hooks'
import type { DocumentEditorProps } from '../registry'
import { truthTableApi } from './api'

/** A text field that commits on blur or Enter (Mod+Enter for multiline) and reverts on Escape. */
function CommitField({
	id,
	value: external,
	onCommit,
	readOnly,
	multiline,
}: {
	id: string
	value: string
	onCommit: (value: string) => void
	readOnly: boolean
	multiline?: boolean
}) {
	const [value, setValue] = useState(external)
	const [focused, setFocused] = useState(false)
	useEffect(() => {
		if (!focused) setValue(external)
	}, [external, focused])
	const props = {
		id,
		className: 'input',
		value,
		readOnly,
		onFocus: () => setFocused(true),
		onBlur: () => {
			setFocused(false)
			if (value !== external) onCommit(value)
		},
		onKeyDown: (e: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => {
			if (e.key === 'Escape') {
				setValue(external)
				// Blur after the reset has been rendered, so the old value isn't committed
				const target = e.currentTarget
				requestAnimationFrame(() => target.blur())
			}
			if (e.key === 'Enter' && (!multiline || e.metaKey || e.ctrlKey)) e.currentTarget.blur()
		},
	}
	return multiline ? (
		<textarea {...props} rows={4} onChange={(e) => setValue(e.target.value)} />
	) : (
		<input {...props} onChange={(e) => setValue(e.target.value)} />
	)
}

/** Properties of the open truth table (TruthTableBeanInfo: name and description) and its structure. */
export function TruthTableInspector({ state }: DocumentEditorProps) {
	useEngineVersion()
	const table = truthTableApi.table()
	if (!table) return null
	const readOnly = !state.document?.editable
	return (
		<>
			<section className="prop-group">
				<h3>Properties</h3>
				<div className="prop-row">
					<label className="prop-label" htmlFor="tt-name">
						Name
					</label>
					<div className="prop-control">
						<CommitField id="tt-name" value={table.name} readOnly={readOnly} onCommit={(v) => v.trim() && truthTableApi.setName(v.trim())} />
					</div>
				</div>
				<div className="prop-row stacked">
					<label className="prop-label" htmlFor="tt-description">
						Description
					</label>
					<div className="prop-control">
						<CommitField id="tt-description" value={table.description} readOnly={readOnly} multiline onCommit={(v) => truthTableApi.setDescription(v)} />
					</div>
				</div>
			</section>
			<section className="prop-group">
				<h3>Structure</h3>
				<dl className="tt-facts">
					<dt>Inputs</dt>
					<dd>{table.inputs.join(', ')}</dd>
					<dt>Outputs</dt>
					<dd>{table.outputs.map((o) => o.label).join(', ')}</dd>
					<dt>Rows</dt>
					<dd>{table.rows}</dd>
					{table.stateColumnCount > 0 && (
						<>
							<dt>State bits</dt>
							<dd>{table.stateColumnCount}</dd>
						</>
					)}
				</dl>
			</section>
			<section className="prop-group tips">
				<h3>Tips</h3>
				<ul>
					<li>X marks a don’t-care output; minimization may choose 0 or 1.</li>
					<li>Prefix an output name with ! to synthesize its inverted function.</li>
					<li>All edits can be undone with ⌘Z.</li>
				</ul>
			</section>
		</>
	)
}

/** Properties of the open boolean expressions (BooleanExpressionStorableBeanInfo: name). */
export function ExpressionInspector({ state }: DocumentEditorProps) {
	useEngineVersion()
	const expression = truthTableApi.expression()
	if (!expression) return null
	const readOnly = !state.document?.editable
	return (
		<>
			<section className="prop-group">
				<h3>Properties</h3>
				<div className="prop-row">
					<label className="prop-label" htmlFor="expr-name">
						Name
					</label>
					<div className="prop-control">
						<CommitField id="expr-name" value={expression.name} readOnly={readOnly} onCommit={(v) => v.trim() && truthTableApi.setName(v.trim())} />
					</div>
				</div>
				<div className="prop-row inline">
					<label className="prop-label" htmlFor="expr-single">
						One-character variables
					</label>
					<div className="prop-control">
						<label className="switch">
							<input
								id="expr-single"
								type="checkbox"
								checked={expression.singleChar}
								disabled={readOnly}
								onChange={(e) => truthTableApi.setSingleChar(e.target.checked)}
							/>
							<span className="switch-track" />
						</label>
					</div>
				</div>
			</section>
			<section className="prop-group tips">
				<h3>Syntax</h3>
				<ul>
					<li>One assignment per line, e.g. Y = A * B' + C.</li>
					<li>AND: * ∧ && AND, OR: + ∨ || OR, NOT: ' ¬ ! NOT.</li>
					<li>With one-character variables, AND can be omitted: Y = AB' + C.</li>
					<li>Constants: 0 and 1.</li>
				</ul>
			</section>
		</>
	)
}
