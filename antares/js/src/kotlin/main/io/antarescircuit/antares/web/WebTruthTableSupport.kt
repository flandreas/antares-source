package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.expression.StandardBooleanExpressionWriter
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.truthtable.KarnaughCell
import io.antarescircuit.antares.model.truthtable.KarnaughMap
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.io.Storable

/** Converts truth tables to the JSON structures read by the web application. */
internal object WebTruthTableJson {

	/** Tables with more rows are sent without values beyond this limit, see `truncated`. */
	const val MAX_ROWS = 4096

	/** The table's columns and output values, one string of `0`, `1`, `X` (don't care) or `Z` per output. */
	fun table(table: TruthTable): dynamic {
		val writer = StandardBooleanExpressionWriter.ofPropertiesNotation()
		val rows = minOf(table.rowsCount, MAX_ROWS)
		val inputs = (0 until table.inputColumnCount).map { table.getColumnName(it) }.toJsArray()
		val outputs = (table.inputColumnCount until table.columnCount).map { column ->
			val name = table.getColumnName(column)
			val label = writer.getOutput(table, column)
			val values = (0 until rows).joinToString("") { table.getValue(it, column).toString() }
			obj {
				this.name = name
				this.label = label
				this.values = values
			}
		}.toJsArray()
		val name = table.name.value
		val description = table.description.value ?: ""
		val rowsCount = table.rowsCount
		val truncated = table.rowsCount > MAX_ROWS
		val stateColumnCount = table.stateColumnCount
		return obj {
			this.name = name
			this.description = description
			this.inputs = inputs
			this.outputs = outputs
			this.rows = rowsCount
			this.truncated = truncated
			this.stateColumnCount = stateColumnCount
		}
	}

	/** The minimized expression of each output in the configured notation. */
	fun expressions(table: TruthTable): Array<String> {
		val notation = BooleanExpressionNotation.fromProperties()
		return (table.inputColumnCount until table.columnCount)
			.map { AntaresModelModule.truthTableService.generateExpression(table, it, notation) }
			.toJsArray()
	}

	/** The Karnaugh map of output [column] with the implicants of its minimized DNF, like the desktop's KarnaughMapView. */
	fun karnaugh(table: TruthTable, column: Int): dynamic {
		val variableCount = table.inputColumnCount
		if (variableCount !in 1..KarnaughMap.MAX_VARIABLES) {
			val max = KarnaughMap.MAX_VARIABLES
			return obj { this.tooManyInputs = true; this.maxInputs = max }
		}
		val map = KarnaughMap(variableCount)
		val service = AntaresModelModule.truthTableService
		val notation = BooleanExpressionNotation.fromProperties()
		val dnf = service.generateDnf(table, column)
		val names = { variables: List<Int> ->
			val list = variables.map { table.getColumnName(it) }
			list.joinToString(if (list.all { it.length == 1 }) "" else ",")
		}
		val code = { value: Int, bits: Int -> if (bits == 0) "" else value.toString(2).padStart(bits, '0') }

		// cells[submap][row][column] is the truth table row shown in that cell
		val cells = (0 until map.submapCount).map { s ->
			(0 until map.rowCount).map { r ->
				(0 until map.columnCount).map { c ->
					map.truthTableRow(KarnaughCell(s, r, c))
				}.toJsArray()
			}.toJsArray()
		}.toJsArray()
		val implicants = dnf.map { term ->
			val text = service.generateExpression(table, column, notation, listOf(term)).substringAfter(" = ")
			val rectangles = map.rectangles(term).map { rect ->
				val submap = rect.submap
				val row = rect.row
				val col = rect.column
				val rowSpan = rect.rowSpan
				val columnSpan = rect.columnSpan
				obj {
					this.submap = submap
					this.row = row
					this.column = col
					this.rowSpan = rowSpan
					this.columnSpan = columnSpan
				}
			}.toJsArray()
			obj {
				this.text = text
				this.rectangles = rectangles
			}
		}.toJsArray()
		val expression = service.generateExpression(table, column, notation, dnf)
		val rowVariables = names(map.rowVariables)
		val columnVariables = names(map.columnVariables)
		val submapVariables = names(map.submapVariables)
		val rowCodes = (0 until map.rowCount).map { code(KarnaughMap.grayCode(it), map.rowVariableCount) }.toJsArray()
		val columnCodes = (0 until map.columnCount).map { code(KarnaughMap.grayCode(it), map.columnVariableCount) }.toJsArray()
		val submapCodes = (0 until map.submapCount).map { code(KarnaughMap.grayCode(it), map.submapVariableCount) }.toJsArray()
		return obj {
			this.tooManyInputs = false
			this.rowVariables = rowVariables
			this.columnVariables = columnVariables
			this.submapVariables = submapVariables
			this.rowCodes = rowCodes
			this.columnCodes = columnCodes
			this.submapCodes = submapCodes
			this.cells = cells
			this.implicants = implicants
			this.expression = expression
		}
	}
}

/**
 * Replaces the content of the open document, e.g. a truth table with different columns, as one undo step.
 * [ApplicationData.content] notifies references such as `TruthTableReference` about the new instance.
 */
internal class WebReplaceContentCommand(
	private val data: ApplicationData,
	private val oldContent: Storable,
	private val newContent: Storable,
	descriptionKey: String,
) : AbstractCommand(descriptionKey), Undoable {

	override fun execute() {
		data.content = newContent
	}

	override fun undo() {
		data.content = oldContent
	}
}

/** Sets a value through [set] as one undo step, restoring the value [get] returned before. */
internal class WebValueCommand<T>(
	descriptionKey: String,
	private val get: () -> T,
	private val set: (T) -> Unit,
	private val value: T,
) : AbstractCommand(descriptionKey), Undoable {

	private val oldValue = get()

	override fun execute() = set(value)

	override fun undo() = set(oldValue)
}

/**
 * Reads and writes the CSV files of the desktop's truth table import (`TruthTableServiceJvm`), which is
 * JVM-only because it uses Apache Commons CSV.
 */
internal object WebTruthTableCsv {

	private const val ILLEGAL_VALUE = "antares.truthTable.csv.illegalValue.msg"
	private const val WRONG_NUMBER_OF_COLUMNS = "antares.truthTable.csv.wrongNumberOfColumns.msg"
	private const val TOO_MANY_ROWS = "antares.truthTable.csv.tooManyRows.msg"
	private const val UNKNOWN_COLUMN_NAME = "antares.truthTable.csv.unknownColumnName.msg"

	/** Writes all columns of [table] with a header line, as the import reads with headers and input columns. */
	fun export(table: TruthTable): String = buildString {
		appendLine((0 until table.columnCount).joinToString(",") { quote(table.getColumnName(it)) })
		for (row in 0 until table.rowsCount) {
			appendLine((0 until table.columnCount).joinToString(",") { table.getValue(row, it).toString() })
		}
	}

	private fun quote(value: String): String =
		if (value.any { it == ',' || it == '"' || it == '\n' }) "\"${value.replace("\"", "\"\"")}\"" else value

	/**
	 * Returns a copy of [table] with the output values read from CSV [text].
	 * @param headers the first line names the columns, so columns are matched by name
	 * @param inputColumns the lines contain the input values, which select the row, before the outputs
	 * @throws IllegalArgumentException with a translated message if the CSV doesn't match the table
	 */
	fun import(table: TruthTable, text: String, headers: Boolean, inputColumns: Boolean): TruthTable {
		val result = table.doClone()
		val lines = parse(text)
		val header = if (headers) lines.firstOrNull()?.map { it.trim() } ?: emptyList() else emptyList()
		val records = if (headers) lines.drop(1) else lines

		val expectedColumns = if (inputColumns) table.columnCount else table.outputColumnCount
		records.firstOrNull()?.size?.let { size ->
			if (size != expectedColumns) fail(WRONG_NUMBER_OF_COLUMNS, size, expectedColumns)
		}
		if (records.size > table.rowsCount) fail(TOO_MANY_ROWS, records.size, table.rowsCount)

		// The CSV column holding each truth table column
		val csvColumn = { column: Int ->
			if (headers) {
				val name = table.getColumnName(column)
				header.indexOf(name).takeIf { it >= 0 } ?: fail(UNKNOWN_COLUMN_NAME, name)
			} else if (inputColumns) {
				column
			} else {
				column - table.inputColumnCount
			}
		}

		records.forEachIndexed { index, record ->
			val csvRow = index + 1
			val cell = { column: Int ->
				val c = csvColumn(column)
				val value = record.getOrNull(c)?.trim() ?: fail(WRONG_NUMBER_OF_COLUMNS, record.size, expectedColumns)
				parseCell(value, csvRow, c + 1)
			}
			val row = if (inputColumns) {
				(0 until table.inputColumnCount).fold(0) { row, column ->
					val bit = cell(column)
					if (!bit.isDefined) fail(ILLEGAL_VALUE, bit.toString(), csvRow, csvColumn(column) + 1)
					(row shl 1) or (if (bit.isSet) 1 else 0)
				}
			} else {
				index
			}
			for (column in table.inputColumnCount until table.columnCount) {
				result.setValue(row, column, cell(column))
			}
		}
		return result
	}

	private fun parseCell(value: String, csvRow: Int, csvColumn: Int): Bit {
		if (value.length != 1) fail(ILLEGAL_VALUE, value, csvRow, csvColumn)
		return try {
			Bit.of(value.uppercase()[0])
		} catch (_: Throwable) {
			fail(ILLEGAL_VALUE, value, csvRow, csvColumn)
		}
	}

	private fun fail(key: String, vararg params: Any): Nothing =
		throw IllegalArgumentException(Translations.getString(key, *params))

	/** Splits CSV [text] into records of fields, supporting quoted fields; blank lines are skipped. */
	private fun parse(text: String): List<List<String>> {
		val records = mutableListOf<List<String>>()
		var fields = mutableListOf<String>()
		val field = StringBuilder()
		var quoted = false
		var i = 0
		fun endRecord() {
			fields.add(field.toString())
			field.clear()
			if (fields.size > 1 || fields[0].isNotBlank()) records.add(fields)
			fields = mutableListOf()
		}
		while (i < text.length) {
			val c = text[i]
			when {
				quoted && c == '"' && text.getOrNull(i + 1) == '"' -> { field.append('"'); i++ }
				c == '"' -> quoted = !quoted
				!quoted && c == ',' -> { fields.add(field.toString()); field.clear() }
				!quoted && (c == '\n' || c == '\r') -> {
					if (c == '\r' && text.getOrNull(i + 1) == '\n') i++
					endRecord()
				}
				else -> field.append(c)
			}
			i++
		}
		if (field.isNotEmpty() || fields.isNotEmpty()) endRecord()
		return records
	}
}
