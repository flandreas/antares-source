package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.base.text.FormattedText
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.dsl.GraphDslModule
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.io.*

class UsecaseImpl(
	name: String = "",
	override var executionScript: String = "",
	override var testScript: String? = null,
	graphView: GraphView? = null,
) : Usecase, Namable, Describable, Bean {

	companion object {
		private val LOG by logger(UsecaseImpl::class)
	}

	@Suppress("MemberVisibilityCanBePrivate")
	var executionScriptProperty: ScriptProperty
		get() = ScriptProperty(executionScript)
		set(value) {
			executionScript = value.script!!
			executionScriptASTCache.reset()
			executionScriptInterpreter = null
		}

	private val executionScriptASTCache = resettableLazy {
		executionScriptProperty.script?.let {
			LOG.trace("Parsing execution script of Usecase '${this.name.value}'")
			createParser(it, null)
				.parseCatching(ScriptMetaData(this.name.value, Translations.getString("graph.property.usecase.execScript.name")))
		}
	}

	private var executionScriptInterpreter: Interpreter? = null

	@Suppress("MemberVisibilityCanBePrivate")
	var testScriptProperty: ScriptProperty
		get() = ScriptProperty(testScript)
		set(value) {
			testScript = value.script!!
			testScriptASTCache.reset()
			testScriptInterpreter = null
		}

	private val testScriptASTCache = resettableLazy {
		testScriptProperty.script?.let {
			LOG.trace("Parsing test script of Usecase '${this.name.value}'")
			createParser(it, null)
				.parseCatching(ScriptMetaData(this.name.value, Translations.getString("graph.property.usecase.testScript.name")))
		}
	}

	private var testScriptInterpreter: Interpreter? = null

	/** ---- [Any] */

	override fun toString(): String = FormattedText.replaceNegation(name.value).textWithOverline

		/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(name))

	override var description: Description by observableDescription(Description(""))

	/** ---- [Usecase] interface */

	override var id: Int = 0

	override var graphView: GraphView? = graphView

	override fun executionStart(graphView: GraphView, signalHandler: SignalHandler) {
		executionScriptASTCache.value?.let {
			executionScriptInterpreter = createScriptInterpreter(graphView, it)
		}
		testScriptASTCache.value?.let {
			testScriptInterpreter = createScriptInterpreter(graphView, it)
		}
	}

	override fun run() {
		executionScriptInterpreter?.interpretCatching(ScriptMetaData(name.value, Translations.getString("graph.property.usecase.execScript.name")))
	}

	override fun runTest() {
		testScriptInterpreter?.interpretCatching(ScriptMetaData(name.value, Translations.getString("graph.property.usecase.testScript.name")))
	}

	override fun dispose() {}

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		name.write("name", writer)
		description.write("desc", writer)
		writer.writeString("exec", executionScript)
		writer.writeOptionalString("test", testScript)
	}

	override fun read(reader: StoreReader) {
		// Attribute 'id' was introduced after version 0.1
		if (reader.hasAttribute("id")) {
			id = reader.readInt("id")
		}
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		executionScript = reader.readString("exec")
		testScript = reader.readOptionalString("test")
	}

	/** ---- [UsecaseImpl] */

	fun createParser(program: String, semanticAnalyser: SemanticAnalyser?): Parser =
		BaseModule.parserFactory.create(program, BaseModule.semanticAnalyserFactory.create(createSymbolTable()))

	private fun createScriptInterpreter(graphView: GraphView, ast: Node): Interpreter =
		BaseModule.interpreterFactory(ast, Memory(GraphActivationRecord(graphView.graph!!)))

	private fun createSymbolTable(): SymbolTable {
		val portSymbolTable = graphView!!.portSymbolTable
		return ScopedSymbolTable(
			name = "ExternalFunctions",
			scopeLevel = portSymbolTable.scopeLevel,
			enclosingScope = portSymbolTable
		).also {
			GraphDslModule.usecaseActionExternalFunctions.defineIn(it)
			GraphDslModule.usecaseTestExternalFunctions.defineIn(it)
		}
	}
}