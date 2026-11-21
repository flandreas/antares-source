// Typed access to the Kotlin `WebTruthTables` feature module.
import { engine } from '../../../core/engine'

/** A truth table, see `WebTruthTableJson.table`. Input values follow from the row index (input 0 is the MSB). */
export interface TableData {
	name: string
	description: string
	inputs: string[]
	outputs: Array<{
		name: string
		/** The name as written in the expression notation, e.g. "Y'" for "!Y" */
		label: string
		/** One character per row: 0, 1, X (don't care) or Z */
		values: string
	}>
	rows: number
	/** Values are only included for the first rows of very large tables */
	truncated: boolean
	stateColumnCount: number
}

export type CellValue = '0' | '1' | 'X'

export interface KarnaughRect {
	submap: number
	row: number
	column: number
	rowSpan: number
	columnSpan: number
}

export type KarnaughData =
	| { tooManyInputs: true; maxInputs: number }
	| {
			tooManyInputs: false
			rowVariables: string
			columnVariables: string
			submapVariables: string
			rowCodes: string[]
			columnCodes: string[]
			submapCodes: string[]
			/** cells[submap][row][column] is the truth table row of that cell */
			cells: number[][][]
			implicants: Array<{ text: string; rectangles: KarnaughRect[] }>
			expression: string
	  }

export interface ExpressionData {
	name: string
	text: string
	singleChar: boolean
}

export interface ExpressionCheck {
	error: { message: string; row: number; column: number } | null
	table: TableData | null
	expressions: string[]
}

export interface NotationSettings {
	notation: string
	omitAnd: boolean
	andParenthesis: boolean
	choices: Array<{ id: string; label: string }>
}

export interface SynthesisType {
	id: string
	label: string
	twoInputGatesOnly: boolean
}

export type AnalysisResult = { error: string } | { error: null; name: string; table: TableData; expressions: string[] }

/** Where synthesis and conversions take their truth table from. */
export type TableSource = 'truthTable' | 'expression' | 'analysis'

/** Column of a restructured truth table: `from` is the index of the existing input/output, or null for a new one. */
export interface ColumnSpec {
	name: string
	from: number | null
}

const NOTATION_KEY = 'antares.expressionNotation'
let notationRestored = false

const parse = <T,>(json: string): T => JSON.parse(json) as T

export const truthTableApi = {
	get kotlin() {
		return engine.api.truthTables
	},

	/** Applies the notation settings stored in this browser, once. */
	restoreNotation() {
		if (notationRestored) return
		notationRestored = true
		try {
			const stored = localStorage.getItem(NOTATION_KEY)
			if (!stored) return
			const { notation, omitAnd, andParenthesis } = JSON.parse(stored) as NotationSettings
			this.kotlin.setNotation(notation, omitAnd, andParenthesis)
		} catch {
			// Invalid or unavailable storage: keep the defaults
		}
	},

	notation(): NotationSettings {
		this.restoreNotation()
		return parse<NotationSettings>(this.kotlin.notation())
	},

	setNotation(notation: string, omitAnd: boolean, andParenthesis: boolean) {
		this.kotlin.setNotation(notation, omitAnd, andParenthesis)
		try {
			localStorage.setItem(NOTATION_KEY, JSON.stringify({ notation, omitAnd, andParenthesis }))
		} catch {
			// Storage unavailable
		}
		engine.refresh()
	},

	undo() {
		this.kotlin.undo()
		engine.refresh()
	},

	redo() {
		this.kotlin.redo()
		engine.refresh()
	},

	/** Returns an error message, or '' after creating and opening the truth table. */
	createTruthTable(name: string, inputs: string, outputs: string): string {
		const error = this.kotlin.createTruthTable(name, inputs, outputs)
		engine.refresh()
		return error
	},

	createExpression(name: string): string {
		const error = this.kotlin.createExpression(name)
		engine.refresh()
		return error
	},

	table(): TableData | null {
		this.restoreNotation()
		return parse<TableData | null>(this.kotlin.table())
	},

	expressions(): string[] {
		return parse<string[]>(this.kotlin.expressions())
	},

	karnaugh(column: number): KarnaughData | null {
		return parse<KarnaughData | null>(this.kotlin.karnaugh(column))
	},

	/** Sets output cells as one undo step; [column] counts inputs and outputs. */
	setCells(cells: Array<[row: number, column: number, value: CellValue]>) {
		if (cells.length === 0) return
		this.kotlin.setCells(JSON.stringify(cells))
		engine.refresh()
	},

	paste(row: number, column: number, text: string): string {
		const error = this.kotlin.paste(row, column, text)
		engine.refresh()
		return error
	},

	restructure(inputs: ColumnSpec[], outputs: ColumnSpec[]): string {
		const error = this.kotlin.restructure(JSON.stringify(inputs), JSON.stringify(outputs))
		engine.refresh()
		return error
	},

	setName(name: string) {
		this.kotlin.setName(name)
		engine.refresh()
	},

	setDescription(description: string) {
		this.kotlin.setDescription(description)
		engine.refresh()
	},

	importCsv(text: string, headers: boolean, inputColumns: boolean): string {
		const error = this.kotlin.importCsv(text, headers, inputColumns)
		engine.refresh()
		return error
	},

	exportCsv(): string {
		return this.kotlin.exportCsv()
	},

	expression(): ExpressionData | null {
		this.restoreNotation()
		return parse<ExpressionData | null>(this.kotlin.expression())
	},

	checkExpression(text: string, singleChar: boolean): ExpressionCheck | null {
		return parse<ExpressionCheck | null>(this.kotlin.checkExpression(text, singleChar))
	},

	setExpressionText(text: string) {
		this.kotlin.setExpressionText(text)
		engine.refresh()
	},

	setSingleChar(singleChar: boolean) {
		this.kotlin.setSingleChar(singleChar)
		engine.refresh()
	},

	synthesisTypes(source: TableSource): SynthesisType[] {
		return parse<SynthesisType[]>(this.kotlin.synthesisTypes(source))
	},

	/** Creates and opens a circuit; returns an error message or ''. */
	synthesize(source: TableSource, name: string, type: string, twoInputGatesOnly: boolean, createTestcase: boolean): string {
		const error = this.kotlin.synthesize(source, name, type, twoInputGatesOnly, createTestcase)
		engine.refresh()
		return error
	},

	/** Saves the truth table of [source] as truth table or as minimized expressions; returns an error message or ''. */
	saveAs(source: TableSource, kind: 'truthTable' | 'expression', name: string, open: boolean): string {
		const error = this.kotlin.saveAs(source, kind, name, open)
		engine.refresh()
		return error
	},

	analyse(): AnalysisResult | null {
		this.restoreNotation()
		return parse<AnalysisResult | null>(this.kotlin.analyse())
	},
}

/** The value of output [output] in [row] of [table]. */
export const outputValue = (table: TableData, output: number, row: number): string => table.outputs[output].values[row] ?? ''

/** The value of input [input] in [row]: input 0 is the most significant bit of the row index. */
export const inputValue = (table: TableData, input: number, row: number): string =>
	String((row >> (table.inputs.length - 1 - input)) & 1)

/** 0 → 1 → X → 0, as the desktop's Karnaugh map. */
export const nextValue = (value: string): CellValue => (value === '0' ? '1' : value === '1' ? 'X' : '0')
