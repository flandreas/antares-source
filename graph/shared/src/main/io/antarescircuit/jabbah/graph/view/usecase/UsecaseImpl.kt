package io.antarescircuit.jabbah.graph.view.usecase

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.parser.Parser
import io.antarescircuit.jabbah.base.resettableLazy
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.description.*
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.dsl.GraphDslModule
import io.antarescircuit.jabbah.graph.model.graph.GraphActivationRecord
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.io.*

class UsecaseImpl(
	name: String = "",
	executionScript: String? = null,
	override var testScript: String? = null,
	graphView: GraphView? = null,
) : AbstractStorable(), Usecase, Bean {

	companion object {
		private val LOG by logger(UsecaseImpl::class)
		val SCRIPTING_HELP_ID = HelpId("graph.usecaseImpl.scripting")
	}

	@Suppress("MemberVisibilityCanBePrivate")
	override var executionScript: ScriptProperty = ScriptProperty(executionScript)
		set(value) {
			field = value
			executionScriptASTCache.reset()
			executionScriptInterpreter = null
		}

	private val executionScriptASTCache = resettableLazy {
		this.executionScript.script?.let {
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

	override fun toString(): String = name.value

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(name))

	override var description: Description by observableDescription(Description(""))

	/** ---- [Usecase] interface */

	override var id: Int = 0

	override var graphView: GraphView? = graphView

	override fun duplicate(newName: String): Usecase {
		val duplicate = StorableCloner.clone(this)
		duplicate.name = Name(newName)
		return duplicate
	}

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
		writer.writeString("exec", executionScript.script ?: "")
		writer.writeOptionalString("test", testScript)
	}

	override fun read(reader: StoreReader) {
		// Attribute 'id' was introduced after version 0.1
		if (reader.hasAttribute("id")) {
			id = reader.readInt("id")
		}
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		executionScript = ScriptProperty(reader.readString("exec"))
		testScript = reader.readOptionalString("test")
	}

	/** ---- [UsecaseImpl] */

	fun createParser(program: String, @Suppress("UNUSED_PARAMETER") semanticAnalyser: SemanticAnalyser?): Parser =
		BaseModule.parserFactory(program, BaseModule.semanticAnalyserFactory(createSymbolTable()))

	private fun createScriptInterpreter(graphView: GraphView, ast: Node): Interpreter =
		BaseModule.interpreterFactory(ast, Memory(GraphActivationRecord(graphView.graph!!)))

	private fun createSymbolTable(): SymbolTable {
		val portSymbolTable = graphView!!.graph!!.symbolTable
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