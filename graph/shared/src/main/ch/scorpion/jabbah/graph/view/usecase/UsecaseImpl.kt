package ch.scorpion.jabbah.graph.view.usecase

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
	override var testScript: String? = null
) : Usecase, Namable, Describable, Bean {

	companion object {
		private val LOG by logger(UsecaseImpl::class)
	}

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
			createExecutionScriptParser(it).parseCatching(this.name.value, "Usecase Logic")
		}
	}

	var executionScriptInterpreter: Interpreter? = null
		private set

	var testScriptProperty: ScriptProperty
		get() = ScriptProperty(testScript)
		set(value) {
			testScript = value.script!!
		}

	/** ---- [Any] */

	override fun toString(): String = FormattedText.replaceNegation(name.value).textWithOverline

		/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(name))

	override var description: Description by observableDescription(Description(""))

	/** ---- [Usecase] interface */

	override var id: Int = 0

	override fun executionStart(graphView: GraphView, signalHandler: SignalHandler) {
		executionScriptASTCache.value?.let {
			executionScriptInterpreter = createExecutionScriptInterpreter(graphView, it)
		}
	}

	override fun run() {
		executionScriptInterpreter?.interpretCatching(name.value, "Usecase Logic")
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

	private fun createExecutionScriptParser(program: String): Parser =
		BaseModule.parserFactory.create(program, BaseModule.semanticAnalyserFactory.create(createExecutionScriptParserSymbolTable()))

	private fun createExecutionScriptParserSymbolTable(): ScopedSymbolTable =
		ScopedSymbolTable("Context", level = 0, enclosingScope = null).also {
			defineContextFunctions(it)
		}

	private fun defineContextFunctions(symbolTable: ScopedSymbolTable) {
		GraphDslModule.usecaseExternalFunctions.defineIn(symbolTable)
	}

	private fun createExecutionScriptInterpreter(graphView: GraphView, ast: Node): Interpreter =
		BaseModule.interpreterFactory(ast, Memory(GraphActivationRecord(graphView.graph!!)))
}