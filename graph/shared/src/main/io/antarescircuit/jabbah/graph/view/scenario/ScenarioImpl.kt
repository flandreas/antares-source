package io.antarescircuit.jabbah.graph.view.scenario

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.parser.Parser
import io.antarescircuit.jabbah.base.resettableLazy
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.description.*
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.dsl.GraphDslModule
import io.antarescircuit.jabbah.graph.model.graph.GraphActivationRecord
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep
import io.antarescircuit.jabbah.io.*

/**
 * Standard implementation of the [Scenario] interface.
 */
class ScenarioImpl(
	initialName: String = "",
	graphView: GraphView? = null,
	private var conditionScript: String = ""
) : AbstractStorable(), Scenario, Namable, Describable, Bean {

	companion object {
		private val LOG by logger(ScenarioImpl::class)
		val CONDITION_HELP_ID = HelpId("graph.scenario.condition")
	}

	private val steps: MutableList<ScenarioStep> by lazy { mutableListOf() }

	private var isLoading: Boolean = false

	@Suppress("unused")
	var conditionProperty: ScriptProperty
		get() = ScriptProperty(conditionScript)
		set(value) {
			conditionScript = value.script!!
			conditionScriptASTCache.reset()
		}

	private val conditionScriptASTCache = resettableLazy {
		LOG.trace("Parsing condition script of '${name.value}'")
		createParser(conditionScript, null).parseCatching(scriptMetaData.value)
	}

	private val scriptMetaData = resettableLazy {
		ScriptMetaData(
			Translations.getString("scenario.issueOrigin.name", name.value),
			Translations.getString("graph.property.scenario.condition.name")
		)
	}

	private var interpreter: Interpreter? = null

	/** ---- [Any] */

	override fun toString(): String = name.value

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(initialName)) {
		// MetaData depends on name
		scriptMetaData.reset()
	}

	override var description: Description by observableDescription(Description(""))

	/** ---- [Scenario] interface */

	override var id: Int = 0

	override var graphView: GraphView? = graphView
		set(value) {
			field = value
			steps.forEach { it.graphView = value }
		}

	override val stepCount: Int get() = steps.size

	override fun dispose() {
		steps.forEach { it.dispose() }
		steps.clear()
	}

	override fun executionStart(graphView: GraphView, signalHandler: SignalHandler) {
		steps.forEach { it.executionStart(graphView, signalHandler) }
		conditionScriptASTCache.value?.let {
			interpreter = createInterpreter(graphView, it)
		}
	}

	override val condition: (SignalHandler, DrawingView<GraphElementView<*>, GraphView>) -> Boolean get() = { signalHandler, view ->
		val scriptMetaData = ScriptMetaData(
			Translations.getString("scenario.issueOrigin.name", name.value),
			Translations.getString("graph.property.scenario.condition.name")
		)
		GraphDslModule.scenarioExternalFunctions.bind(signalHandler, view.drawing, scriptMetaData.origin, scriptMetaData.context)
		interpreter?.let {
			it.interpretCatching(scriptMetaData, view.drawing.graph!!) != 0L
		} ?: false
	}

	override fun getScenarioSteps(): ImmutableList<ScenarioStep> = steps.toImmutableList()

	override fun getStep(id: Int): ScenarioStep = steps.first { it.id == id }

	override fun addStep(step: ScenarioStep) {
		addStep(step, steps.size)
	}

	override fun addStep(step: ScenarioStep, index: Int) {
		if (!steps.contains(step)) {
			if (!isLoading) {
				step.id = getMaxId() + 1
			}
			step.graphView = graphView
			steps.add(index, step)
		}
	}

	override fun removeStep(step: ScenarioStep) {
		step.graphView = null
		steps.remove(step)
	}

	override fun moveStep(step: ScenarioStep, newIndex: Int) {
		val oldIndex = indexOf(step)
		if (oldIndex < 0 || newIndex == oldIndex) {
			return
		}
		val effIndex = if (oldIndex <= newIndex) newIndex - 1 else newIndex
		steps.remove(step)
		steps.add(effIndex, step)
	}

	override fun indexOf(step: ScenarioStep): Int = steps.indexOf(step)

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		name.write("name", writer)
		description.write("desc", writer)
		writer.writeString("condition", conditionScript)
		writer.writeStorables("steps", steps.iterator())
	}

	override fun read(reader: StoreReader) {
		try {
			isLoading = true
			id = reader.readInt("id")
			name = Name.read("name", reader)
			description = Description.read("desc", reader)
			conditionScript = reader.readString("condition")
			for (step in reader.readStorables<ScenarioStep>("steps")) {
				addStep(step)
			}
		} finally {
			isLoading = false
		}
	}

	/** ---- [ScenariosImpl] */

	@Suppress("UNUSED_PARAMETER")
	fun createParser(program: String, semanticAnalyser: SemanticAnalyser?): Parser =
		BaseModule.parserFactory(program, BaseModule.semanticAnalyserFactory(createSymbolTable()))

	private fun getMaxId(): Int {
		if (steps.size == 0) {
			return 0
		}
		return steps.maxByOrNull { it.id }!!.id
	}

	private fun createInterpreter(graphView: GraphView, ast: Node): Interpreter =
		BaseModule.interpreterFactory(ast, Memory(GraphActivationRecord(graphView.graph!!)))

	private fun createSymbolTable(): SymbolTable {
		val portSymbolTable = graphView!!.graph!!.symbolTable
		return ScopedSymbolTable(
			name = "ExternalFunctions",
			scopeLevel = portSymbolTable.scopeLevel,
			enclosingScope = portSymbolTable
		).also {
			GraphDslModule.scenarioExternalFunctions.defineIn(it)
		}
	}
}