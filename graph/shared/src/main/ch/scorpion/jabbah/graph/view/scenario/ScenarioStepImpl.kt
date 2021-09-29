package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.resettableLazy
import ch.scorpion.jabbah.base.text.FormattedText
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [ScenarioStep] interface.
 */
class ScenarioStepImpl(
	initialName: String = "",
	graphView: GraphView? = null
) : ScenarioStep, Namable, Describable, Bean {

	companion object {
		private val LOG by logger(ScenarioStepImpl::class)
	}

	/** The JavaScript predicate that determines whether this [ScenarioStep] is active. */
	private var conditionScript: String? = null
		set(value) {
			field = value
			conditionScriptASTCache.reset()
		}

	private val conditionScriptASTCache = resettableLazy {
		conditionScript?.let {
			LOG.trace("Parsing condition script of '${name.value}'")
			createParser(it, null)
				.parseCatching(ScriptMetaData(name.value, Translations.getString("graph.property.scenario.condition.name")))
		}
	}

	private var conditionInterpreter: Interpreter? = null

	/** The JavaScript expressions to be executed when this [ScenarioStep] is activated. */
	private var onEntryScript: String? = null
		set(value) {
			field = value
			onEntryScriptASTCache.reset()
		}

	private val onEntryScriptASTCache = resettableLazy {
		onEntryScript?.let {
			LOG.trace("Parsing onEntry script of '${name.value}'")
			createParser(it, null)
				.parseCatching(ScriptMetaData(name.value, Translations.getString("graph.property.scenario.onEntry.name")))
		}
	}

	private var onEntryInterpreter: Interpreter? = null

	/** The JavaScript expressions to be executed when this [ScenarioStep] is passivated. */
	private var onExitScript: String? = null

	private val onExitScriptASTCache = resettableLazy {
		onExitScript?.let {
			LOG.trace("Parsing onExit script of '${name.value}'")
			createParser(it, null)
				.parseCatching(ScriptMetaData(name.value, Translations.getString("graph.property.scenario.onExit.name")))
		}
	}

	private var onExitInterpreter: Interpreter? = null

	/** Caches the parsed highlight IDs of `highlightIds' as [Int].*/
	private var highlightIntIdsCache: List<Int>? = null

	/** ---- [Any] */

	override fun toString(): String = FormattedText.replaceNegation(name.value).textWithOverline

		/** ---- [Namable] interface */

	override var name: Name by observableName(Name(initialName))

	override var description: Description by observableDescription(Description(""))

	/** ---- UI editable properties */

	@Suppress("unused")
	var conditionProperty: ScriptProperty
		get() = ScriptProperty(conditionScript)
		set(value) {
			conditionScript = value.script
		}

	@Suppress("unused")
	var onEntryProperty: ScriptProperty
		get() = ScriptProperty(onEntryScript)
		set(value) {
			onEntryScript = value.script
		}

	@Suppress("unused")
	var onExitProperty: ScriptProperty
		get() = ScriptProperty(onExitScript)
		set(value) {
			onExitScript = value.script
		}

	/** ---- [ScenarioStep] interface */

	override var id: Int = 0

	override var graphView: GraphView? = graphView

	override var highlightIds: String? = null
		set(value) {
			if (field != value) {
				field = value
				highlightIntIdsCache = null
			}
		}

	override val condition: (DrawingView<GraphView>) -> Boolean get() = { view ->
		conditionInterpreter?.let {
			it.interpretCatching(
				ScriptMetaData(name.value, Translations.getString("graph.property.scenario.condition.name")),
				view.drawing.graph!!) != 0L
		} ?: false
	}

	override fun dispose() { }

	override fun executionStart(graphView: GraphView, signalHandler: SignalHandler) {
		conditionScriptASTCache.value?.let {
			conditionInterpreter = createInterpreter(graphView, it)
		}
		onEntryScriptASTCache.value?.let {
			onEntryInterpreter = createInterpreter(graphView, it)
		}
		onExitScriptASTCache.value?.let {
			onExitInterpreter = createInterpreter(graphView, it)
		}
	}

	override fun activate(view: DrawingView<GraphView>) {
		onEntryInterpreter?.interpretCatching(
			ScriptMetaData(name.value, Translations.getString("graph.property.scenario.onEntry.name")),
			view.drawing.graph)
	}

	override fun passivate(view: DrawingView<GraphView>) {
		onExitInterpreter?.interpretCatching(
			ScriptMetaData(name.value, Translations.getString("graph.property.scenario.onExit.name")),
			view.drawing.graph)
	}

	override val highlightIdsAsInt: List<Int>
		get() {
			if (highlightIntIdsCache == null) {
				highlightIntIdsCache = mutableListOf()
				if (StringUtils.isNotEmpty(highlightIds)) {
					highlightIds!!
						.split(delimiters = charArrayOf(','))
						.map { it.trim().toInt() }
						.forEach { (highlightIntIdsCache as MutableList<Int>).add(it) }
				}
			}
			return highlightIntIdsCache!!
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		name.write("name", writer)
		description.write("desc", writer)
		highlightIds?.let { writer.writeString("highlightIds", it) }
		conditionScript?.let { writer.writeString("condition", conditionScript!!) }
		onEntryScript?.let { writer.writeString("onEntry", onEntryScript!!) }
		onExitScript?.let { writer.writeString("onExit", onExitScript!!) }
	}

	override fun read(reader: StoreReader) {
		id = reader.readInt("id")
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		highlightIds = reader.readOptionalString("highlightIds")
		conditionScript = reader.readOptionalString("condition")
		onEntryScript = reader.readOptionalString("onEntry")
		onExitScript = reader.readOptionalString("onExit")
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	/** ---- [ScenarioStepImpl] */

	fun createParser(program: String, semanticAnalyser: SemanticAnalyser?): Parser =
		graphView?.createParser(program, semanticAnalyser)
			?: BaseModule.parserFactory.create(program, semanticAnalyser)

	private fun createInterpreter(graphView: GraphView, ast: Node): Interpreter =
		BaseModule.interpreterFactory(ast, Memory(GraphActivationRecord(graphView.graph!!)))
}