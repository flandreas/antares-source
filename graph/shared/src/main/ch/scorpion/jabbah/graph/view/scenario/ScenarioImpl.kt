package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.base.text.FormattedText
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.issue.IssueImpl
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [Scenario] interface.
 */
class ScenarioImpl(
	initialName: String = "",
	private var conditionScript: String = ""
) : Scenario, Namable, Describable, Bean {

	companion object {
		private val LOG by logger(ScenarioImpl::class)
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
		try {
			LOG.trace("Parsing condition script of '${name.value}'")
			BaseModule.parserFactory.create(conditionScript, null).parse()
		} catch (e: DslError) {
			BaseModule.eventBus.post(IssueImpl(
				severity = IssueSeverity.Error,
				name = "Parse Error",
				description = e.message,
				origin = name.value,
				context = "Scenario Condition"))
			null
		}
	}

	private var interpreter: Interpreter? = null

	/** ---- [Any] */

	override fun toString(): String = FormattedText.replaceNegation(name.value).textWithOverline

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(initialName))

	override var description: Description by observableDescription(Description(""))

	/** ---- [Scenario] interface */

	override var id: Int = 0

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

	override val condition: (DrawingView<GraphView>, ScriptGateway) -> Boolean get() = { view, _ ->
		interpreter?.let { it.interpret(view.drawing.graph!!) != 0L } ?: false
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
			steps.add(index, step)
		}
	}

	override fun removeStep(step: ScenarioStep) {
		steps.remove(step)
	}

	override fun moveStep(step: ScenarioStep, newIndex: Int) {
		val oldIndex = indexOf(step)
		if (oldIndex < 0 || newIndex == oldIndex) {
			return
		}
		steps.remove(step)
		steps.add(newIndex, step)
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

	private fun getMaxId(): Int {
		if (steps.size == 0) {
			return 0
		}
		return steps.maxByOrNull { it.id }!!.id
	}

	private fun createInterpreter(graphView: GraphView, ast: Node): Interpreter =
		BaseModule.interpreterFactory(ast, Memory(GraphActivationRecord(graphView.graph!!)))
}