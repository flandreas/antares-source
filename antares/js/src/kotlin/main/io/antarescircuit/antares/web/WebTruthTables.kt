package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.analysis.CircuitAnalysisError
import io.antarescircuit.antares.model.expression.BooleanExpressionLibraryItem
import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.expression.BooleanExpressionReference
import io.antarescircuit.antares.model.expression.BooleanExpressionStorable
import io.antarescircuit.antares.model.expression.BooleanExpressionsCommand
import io.antarescircuit.antares.model.expression.BooleanExpressionsSingleCharCommand
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableCellChange
import io.antarescircuit.antares.model.truthtable.TruthTableCellsCommand
import io.antarescircuit.antares.model.truthtable.TruthTableClipboard
import io.antarescircuit.antares.model.truthtable.TruthTableImportCSVCommand
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.antares.view.synthesis.CircuitFromTruthTableBuilderError
import io.antarescircuit.antares.view.synthesis.CircuitSynthesisType
import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItemSavable
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.library.LibraryItem
import io.antarescircuit.jabbah.graph.model.graph.FeedbackLoopChecker
import io.antarescircuit.jabbah.io.StorableCloner

/**
 * Truth tables, boolean expressions, circuit analysis and synthesis for the web editor, mirroring the desktop's
 * truth table and expression views, `CreateCircuitFromTruthTablePanel` and `AnalyseCircuitPanel`.
 *
 * Edits of the open document are undoable commands on the editor's command manager, so they are autosaved.
 * Methods taking a `source` work on the open truth table (`truthTable`), the truth table of the open
 * expressions (`expression`) or the result of the last [analyse] (`analysis`).
 */
@JsExport
class WebTruthTables internal constructor(private val ctx: WebEditorContext) {

	private val truthTableService get() = AntaresModelModule.truthTableService
	private val expressionService get() = AntaresModelModule.booleanExpressionService

	/** References used by the shared commands; bound to the open document's data. */
	private var truthTableRef: Pair<ApplicationData, TruthTableReference>? = null
	private var expressionRef: Pair<ApplicationData, BooleanExpressionReference>? = null

	/** The result of the last circuit analysis and the circuit it was made of. */
	private var analysis: TruthTable? = null
	private var analysedItem: LibraryItem? = null

	// References listen for content changes of the open document, so they must not outlive it
	private val dataHandler: EventHandler<ApplicationDataEvent> = { disposeReferences() }

	init {
		BaseModule.eventBus.register(ApplicationDataEvent::class, dataHandler)
		Translations.addKey("web.truthTable.columns", "Truth Table Columns")
		Translations.addKey("web.truthTable.name", "Name")
		Translations.addKey("web.truthTable.description", "Description")
		Translations.addKey("web.truthTable.stateColumns", "The columns of state tables can't be changed")
	}

	private fun disposeReferences() {
		truthTableRef?.second?.dispose()
		truthTableRef = null
		expressionRef?.second?.dispose()
		expressionRef = null
	}

	private val data: ApplicationData? get() = ctx.dataController().data

	private val openItem: LibraryItem? get() = (data?.savable as? AbstractLibraryItemSavable)?.item

	/** The open truth table document's content, if any. */
	private val truthTable: TruthTable? get() = data?.content as? TruthTable

	private val expressions: BooleanExpressionStorable? get() = data?.content as? BooleanExpressionStorable

	private val editable: Boolean
		get() = (data?.savable?.editable ?: false) && !ctx.frame().applicationModeHolder.currentMode.isExecute()

	private fun ref(): TruthTableReference {
		val data = data!!
		truthTableRef?.takeIf { it.first === data }?.let { return it.second }
		truthTableRef?.second?.dispose()
		val ref = TruthTableReference({ data.content as? TruthTable ?: TruthTable() })
		truthTableRef = data to ref
		return ref
	}

	private fun expressionRef(): BooleanExpressionReference {
		val data = data!!
		expressionRef?.takeIf { it.first === data }?.let { return it.second }
		expressionRef?.second?.dispose()
		val ref = BooleanExpressionReference(data.content as BooleanExpressionStorable)
		expressionRef = data to ref
		return ref
	}

	private fun execute(command: Command) {
		ctx.editor.commandManager.execute(command)
		ctx.notifyChanged()
	}

	/** Runs [block], returning the message of a validation error, or an empty string on success. */
	private fun validated(block: () -> Unit): String = ctx.guarded("") {
		try {
			block()
			""
		} catch (e: IllegalArgumentException) {
			e.message ?: e.toString()
		} catch (e: IllegalStateException) {
			e.message ?: e.toString()
		} catch (e: CircuitFromTruthTableBuilderError) {
			e.message ?: e.toString()
		} catch (e: DslError) {
			e.toString()
		}
	}

	/** ---- Undo of document edits (the circuit editor's undo action requires an active drawing view) */

	fun undo() = ctx.guarded(Unit) {
		if (editable && ctx.editor.commandManager.canUndo()) ctx.editor.commandManager.undo()
		ctx.notifyChanged()
	}

	fun redo() = ctx.guarded(Unit) {
		if (editable && ctx.editor.commandManager.canRedo()) ctx.editor.commandManager.redo()
		ctx.notifyChanged()
	}

	/** ---- Notation of generated expressions (desktop preferences) */

	/** Returns the expression notation settings and the available notations as JSON. */
	fun notation(): String = ctx.guarded("null") {
		val properties = BaseModule.properties
		val notation = BooleanExpressionNotation.fromProperties().customName
		val omitAnd = properties.getBoolean(BooleanExpressionNotation.PROP_OMIT_AND)
		val andParenthesis = properties.getBoolean(BooleanExpressionNotation.PROP_AND_PARENTHESIS)
		val choices = BooleanExpressionNotation.entries.map { n ->
			val id = n.customName
			val label = n.toString()
			obj { this.id = id; this.label = label }
		}.toJsArray()
		toJson(obj {
			this.notation = notation
			this.omitAnd = omitAnd
			this.andParenthesis = andParenthesis
			this.choices = choices
		})
	}

	fun setNotation(notation: String, omitAnd: Boolean, andParenthesis: Boolean) = ctx.guarded(Unit) {
		val properties = BaseModule.properties
		properties.set(BooleanExpressionNotation.PROP_NOTATION, BooleanExpressionNotation.withName(notation).customName)
		properties.set(BooleanExpressionNotation.PROP_OMIT_AND, omitAnd)
		properties.set(BooleanExpressionNotation.PROP_AND_PARENTHESIS, andParenthesis)
		ctx.notifyChanged()
	}

	/** ---- New items */

	/**
	 * Creates a truth table with comma-separated [inputs] and [outputs] in the open project and opens it.
	 * Returns an error message if the names are invalid, otherwise an empty string.
	 */
	fun createTruthTable(name: String, inputs: String, outputs: String): String = validated {
		val table = truthTableService.createWithUserInput(name, inputs, outputs)
		val item = TruthTableLibraryItem(table)
		add(item, null)
		ctx.openItem(item.uuid.id)
	}

	/** Creates empty boolean expressions in the open project and opens them. */
	fun createExpression(name: String): String = validated {
		require(name.isNotBlank()) { Translations.getString("library.newTruthTable.error.nameEmpty") }
		val item = BooleanExpressionLibraryItem(TranslatableText(name))
		add(item, null)
		ctx.openItem(item.uuid.id)
	}

	/** Adds [item] to the project, next to [neighbor] if given. */
	private fun add(item: LibraryItem, neighbor: LibraryItem?) {
		val (library, directory) = locationNear(neighbor)
		library.libraryService.addLibraryItem(library, item, directory)
	}

	private fun locationNear(neighbor: LibraryItem?): Pair<Library, LibraryDirectory> {
		val library = neighbor?.library ?: ctx.project ?: error("No project")
		val directory = neighbor?.let { library.libraryService.getDirectoryOf(library, it) } ?: library
		return library to directory
	}

	/** ---- Truth table documents */

	/** Returns the open truth table as JSON (see [WebTruthTableJson.table]), or `null`. */
	fun table(): String = ctx.guarded("null") {
		truthTable?.let { toJson(WebTruthTableJson.table(it)) } ?: "null"
	}

	/** Returns the minimized expression of each output of the open truth table as JSON array. */
	fun expressions(): String = ctx.guarded("[]") {
		truthTable?.let { toJson(WebTruthTableJson.expressions(it)) } ?: "[]"
	}

	/** Returns the Karnaugh map of the open truth table's [column] (an output column index) as JSON. */
	fun karnaugh(column: Int): String = ctx.guarded("null") {
		val table = truthTable ?: return@guarded "null"
		if (column !in table.inputColumnCount until table.columnCount) return@guarded "null"
		toJson(WebTruthTableJson.karnaugh(table, column))
	}

	/** Sets output cells, given as JSON array of `[row, column, value]` with value `0`, `1` or `X`, as one undo step. */
	fun setCells(json: String) = ctx.guarded(Unit) {
		val table = truthTable ?: return@guarded
		if (!editable) return@guarded
		val cells = JSON.parse<Array<Array<dynamic>>>(json)
		val changes = cells
			.map { TruthTableCellChange((it[0] as Number).toInt(), (it[1] as Number).toInt(), Bit.of((it[2] as String)[0])) }
			.distinctBy { it.row to it.column }
			.filter { it.column >= table.inputColumnCount && table.getValue(it.row, it.column) != it.value }
		if (changes.isNotEmpty()) execute(TruthTableCellsCommand(ref(), changes))
	}

	/**
	 * Pastes a block of 0/1/X values (TSV, CSV or space separated) with its top left at ([row], [column]).
	 * Returns an error message if the block doesn't fit into the output cells, otherwise an empty string.
	 */
	fun paste(row: Int, column: Int, text: String): String = validated {
		val table = truthTable ?: return@validated
		if (!editable) return@validated
		val values = TruthTableClipboard.parse(text)
		val error = Translations.getString("antares.truthTable.paste.error")
		require(column >= table.inputColumnCount && row >= 0) { error }
		require(values.size <= table.rowsCount - row && values[0].size <= table.columnCount - column) { error }
		val changes = values.flatMapIndexed { r, cells -> cells.mapIndexed { c, bit -> TruthTableCellChange(row + r, column + c, bit) } }
		execute(TruthTableCellsCommand(ref(), changes))
	}

	/**
	 * Changes the columns of the open truth table as one undo step. [inputs] and [outputs] are JSON arrays of
	 * `{name, from}`, where `from` is the index of the existing input/output the column is taken from, or `null`
	 * for a new column. Values of kept outputs are preserved; removed inputs keep the rows where they were 0.
	 * Returns an error message for invalid names, otherwise an empty string.
	 */
	fun restructure(inputs: String, outputs: String): String = validated {
		val data = data ?: return@validated
		val old = truthTable ?: return@validated
		if (!editable) return@validated
		require(old.stateColumnCount == 0) { Translations.getString("web.truthTable.stateColumns") }
		val newInputs = JSON.parse<Array<dynamic>>(inputs).map { it.name as String to (it.from as Number?)?.toInt() }
		val newOutputs = JSON.parse<Array<dynamic>>(outputs).map { it.name as String to (it.from as Number?)?.toInt() }

		val table = truthTableService.createWithUserInput(
			"-",
			newInputs.joinToString(",") { it.first },
			newOutputs.joinToString(",") { it.first },
		)
		table.name = Name(old.name.value)
		table.description = old.description

		val inputCount = newInputs.size
		for (row in 0 until table.rowsCount) {
			// The row of the old table with the same values of all kept inputs, removed inputs being 0
			val oldRow = (0 until old.inputColumnCount).fold(0) { acc, oldInput ->
				val input = newInputs.indexOfFirst { it.second == oldInput }
				val bit = if (input >= 0) (row shr (inputCount - 1 - input)) and 1 else 0
				(acc shl 1) or bit
			}
			newOutputs.forEachIndexed { output, (_, from) ->
				if (from != null) {
					table.setValue(row, inputCount + output, old.getValue(oldRow, old.inputColumnCount + from))
				}
			}
		}
		execute(WebReplaceContentCommand(data, old, table, "web.truthTable.columns"))
	}

	/** Sets the name of the open truth table or expressions. */
	fun setName(name: String) = ctx.guarded(Unit) {
		val data = data ?: return@guarded
		if (!editable || name.isBlank()) return@guarded
		val content = data.content
		val get = { (data.content as? TruthTable)?.name ?: (data.content as? BooleanExpressionStorable)?.name ?: Name("") }
		val set = { value: Name ->
			(data.content as? TruthTable)?.name = value
			(data.content as? BooleanExpressionStorable)?.name = value
		}
		if ((content is TruthTable || content is BooleanExpressionStorable) && get().value != name) {
			execute(WebValueCommand("web.truthTable.name", get, set, Name(name)))
		}
	}

	/** Sets the description of the open truth table. */
	fun setDescription(description: String) = ctx.guarded(Unit) {
		val data = data ?: return@guarded
		val table = truthTable ?: return@guarded
		if (!editable || (table.description.value ?: "") == description) return@guarded
		execute(WebValueCommand(
			"web.truthTable.description",
			{ (data.content as TruthTable).description },
			{ (data.content as TruthTable).description = it },
			Description(description),
		))
	}

	/**
	 * Imports output values from CSV [text] like the desktop's CSV import, as one undo step.
	 * Returns an error message if the CSV doesn't match the table, otherwise an empty string.
	 */
	fun importCsv(text: String, headers: Boolean, inputColumns: Boolean): String = validated {
		val table = truthTable ?: return@validated
		if (!editable) return@validated
		val imported = WebTruthTableCsv.import(table, text, headers, inputColumns)
		execute(TruthTableImportCSVCommand(ref(), table.doClone(), imported))
	}

	/** Returns all columns of the open truth table as CSV with a header line. */
	fun exportCsv(): String = ctx.guarded("") {
		truthTable?.let { WebTruthTableCsv.export(it) } ?: ""
	}

	/** ---- Boolean expression documents */

	/** Returns the open expressions as JSON: `{name, text, singleChar}`, or `null`. */
	fun expression(): String = ctx.guarded("null") {
		val storable = expressions ?: return@guarded "null"
		val name = storable.name.value
		val text = storable.expressions
		val singleChar = storable.singleCharIdentifier
		toJson(obj {
			this.name = name
			this.text = text
			this.singleChar = singleChar
		})
	}

	/**
	 * Parses expressions [text] and returns JSON `{error: {message, row, column} | null, table, expressions}`
	 * with the resulting truth table and its minimized expressions.
	 */
	fun checkExpression(text: String, singleChar: Boolean): String = ctx.guarded("null") {
		try {
			val table = parseExpressions(text, singleChar)
			val tableJson = if (table.outputColumnCount > 0) WebTruthTableJson.table(table) else null
			val minimized = if (table.outputColumnCount > 0) WebTruthTableJson.expressions(table) else emptyArray()
			toJson(obj {
				this.error = null
				this.table = tableJson
				this.expressions = minimized
			})
		} catch (e: DslError) {
			val message = e.message ?: e.toString()
			val row = e.location.row
			val column = e.location.column
			toJson(obj {
				this.error = obj { this.message = message; this.row = row; this.column = column }
				this.table = null
				this.expressions = emptyArray<String>()
			})
		}
	}

	private fun parseExpressions(text: String, singleChar: Boolean): TruthTable {
		val table = expressionService.createTruthTable(expressionService.parseExpressions(text, singleChar))
		openItem?.let { table.name = Name(it.name.value) }
		return table
	}

	/** Sets the text of the open expressions as one undo step. */
	fun setExpressionText(text: String) = ctx.guarded(Unit) {
		val storable = expressions ?: return@guarded
		if (editable && storable.expressions != text) execute(BooleanExpressionsCommand(expressionRef(), text))
	}

	/** Sets whether the open expressions use single-character variables, so AND operators can be omitted. */
	fun setSingleChar(singleChar: Boolean) = ctx.guarded(Unit) {
		val storable = expressions ?: return@guarded
		if (editable && storable.singleCharIdentifier != singleChar) {
			execute(BooleanExpressionsSingleCharCommand(expressionRef(), singleChar))
		}
	}

	/** ---- Synthesis and conversions */

	private fun sourceTable(source: String): TruthTable? = when (source) {
		"truthTable" -> truthTable
		"expression" -> expressions?.let { parseExpressions(it.expressions, it.singleCharIdentifier) }?.takeIf { it.outputColumnCount > 0 }
		"analysis" -> analysis
		else -> null
	}

	private fun sourceItem(source: String): LibraryItem? = if (source == "analysis") analysedItem else openItem

	/** Returns the circuit types for synthesis from [source] as JSON: `[{id, label, twoInputGatesOnly}]`. */
	fun synthesisTypes(source: String): String = ctx.guarded("[]") {
		val table = sourceTable(source)
		toJson(WebSynthesis.types(table).map { type ->
			val id = type.name
			val label = type.toString()
			val twoInputGatesOnly = type.supportTwoInputGatesOnly
			obj {
				this.id = id
				this.label = label
				this.twoInputGatesOnly = twoInputGatesOnly
			}
		}.toJsArray())
	}

	/**
	 * Creates a circuit of [type] (a `CircuitSynthesisType` name) from the truth table of [source] next to its
	 * project item and opens it. Returns an error message on failure, otherwise an empty string.
	 */
	fun synthesize(source: String, name: String, type: String, twoInputGatesOnly: Boolean, createTestcase: Boolean): String =
		validated {
			val table = requireNotNull(sourceTable(source)) { "No truth table" }
			val (library, directory) = locationNear(sourceItem(source))
			ctx.saveNow()
			val element = WebSynthesis.create(
				table,
				library,
				directory,
				name.trim(),
				CircuitSynthesisType.valueOf(type),
				twoInputGatesOnly,
				createTestcase,
			)
			ctx.openItem(element.uuid.id)
		}

	/**
	 * Saves the truth table of [source] as new truth table item ([kind] `truthTable`), or its minimized expressions
	 * as new expressions item ([kind] `expression`). Opens the new item if [open] is set.
	 * Returns an error message on failure, otherwise an empty string.
	 */
	fun saveAs(source: String, kind: String, name: String, open: Boolean): String = validated {
		require(name.isNotBlank()) { Translations.getString("library.newTruthTable.error.nameEmpty") }
		val table = requireNotNull(sourceTable(source)) { "No truth table" }
		val (item, uuid) = if (kind == "truthTable") {
			val copy = table.doClone()
			copy.name = Name(name)
			TruthTableLibraryItem(copy).let { it to it.uuid.id }
		} else {
			val text = truthTableService.generateExpressions(table, BooleanExpressionNotation.fromProperties())
			BooleanExpressionLibraryItem(TranslatableText(name), text, table.allNamesAreSingleChar).let { it to it.uuid.id }
		}
		add(item, sourceItem(source))
		if (open) ctx.openItem(uuid)
	}

	/** ---- Circuit analysis */

	/**
	 * Computes the truth table of the open digital circuit from its inputs and outputs, like the desktop's
	 * "Analyse Circuit". Returns JSON `{error}` or `{name, table, expressions}`; the result is kept as source
	 * `analysis` for [saveAs] and [synthesize].
	 */
	fun analyse(): String = ctx.guarded("null") {
		val metaGraph = data?.content as? MetaGraph
		val error = { message: String -> toJson(obj { this.error = message }) }
		val model = metaGraph?.graph?.model
		if (metaGraph == null || model == null || model.type != AntaresGraphTypes.Digital) {
			return@guarded error(Translations.getString("antares.circuitAnalysis.onlyDigital.msg"))
		}
		if (FeedbackLoopChecker.hasFeedbackLoop(model)) {
			return@guarded error(Translations.getString("antares.circuitAnalysis.cycles.msg"))
		}
		try {
			// Analyse a clone, so the simulation doesn't affect the edited circuit and its views
			val clone = StorableCloner.clone(metaGraph)
			val table = AntaresModelModule.circuitAnalysisService.analyse(clone.graph.model as DigitalGraph)
			val name = RichText.stripToPlainText(metaGraph.name)
			table.name = Name(name)
			analysis = table
			analysedItem = openItem
			val tableJson = WebTruthTableJson.table(table)
			val minimized = WebTruthTableJson.expressions(table)
			toJson(obj {
				this.error = null
				this.name = name
				this.table = tableJson
				this.expressions = minimized
			})
		} catch (e: CircuitAnalysisError) {
			error(e.message ?: e.toString())
		}
	}
}
