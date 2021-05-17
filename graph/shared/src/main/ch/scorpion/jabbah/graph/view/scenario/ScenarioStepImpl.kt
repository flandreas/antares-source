package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [ScenarioStep] interface.
 */
class ScenarioStepImpl(
	private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway,
	initialName: String = ""

) : ScenarioStep, Namable, Describable, Bean {

	companion object {
		private val LOG by logger(ScenarioStepImpl::class)
	}

	/** The JavaScript predicate that determines whether this [ScenarioStep] is active. */
	private var conditionScript: String? = null

	/** The JavaScript expressions to be executed when this [ScenarioStep] is activated. */
	private var onEntryScript: String? = null

	/** The JavaScript expressions to be executed when this [ScenarioStep] is passivated. */
	private var onExitScript: String? = null

	/** Caches the parsed highlight IDs of `highlightIds' as [Int].*/
	private var highlightIntIdsCache: List<Int>? = null

	/** ---- [Any] */

	override fun toString(): String = StringUtils.replaceNegation(name.value)

	/** ---- [Namable] interface */

	override var name: Name by observableName(Name(initialName))

	override var description: Description by observableDescription(Description(""))

	/** ---- UI editable properties */

	var conditionProperty: ScriptProperty
		get() = ScriptProperty(conditionScript)
		set(value) {
			conditionScript = value.script
		}

	var onEntryProperty: ScriptProperty
		get() = ScriptProperty(onEntryScript)
		set(value) {
			onEntryScript = value.script
		}

	var onExitProperty: ScriptProperty
		get() = ScriptProperty(onExitScript)
		set(value) {
			onExitScript = value.script
		}

	/** ---- [ScenarioStep] interface */

	override var id: Int = 0

	override var highlightIds: String? = null
		set(value) {
			if (field != value) {
				field = value
				highlightIntIdsCache = null
			}
		}

	override val condition: (DrawingView<GraphView>, ScriptGateway) -> Boolean
		get() = { v, sg ->
			if (StringUtils.isNotEmpty(conditionScript)) {
				sg.condition(wrappedConditionScript, v)
			} else {
				false
			}
		}

	private val wrappedConditionScript: Script
		get() =
			Script(
				code = conditionScript!!,
				origin = "${Translations.getString("scenarioStep.issueOrigin.name")} '$name'",
				context = Translations.getString("graph.property.scenario.condition.name"))

	override fun dispose() {
		// empty
	}

	override fun activate(view: DrawingView<GraphView>) {
		if (StringUtils.isNotEmpty(onEntryScript)) {
			try {
				LOG.trace("Activate ScenarioStep '$name'")
				scriptGateway.exec(wrappedOnEntryScript, view)
			} catch (e: Throwable) {
				LOG.error("Error in onEntry script of ScenarioStep '$name'")
			}
		}
	}

	private val wrappedOnEntryScript: Script
		get() =
			Script(
				code = onEntryScript!!,
				origin = "${Translations.getString("scenarioStep.issueOrigin.name")} '$name'",
				context = Translations.getString("graph.property.scenario.onEntry.name"))

	override fun passivate(view: DrawingView<GraphView>) {
		if (StringUtils.isNotEmpty(onExitScript)) {
			try {
				LOG.trace("Passivate ScenarioStep '$name'")
				scriptGateway.exec(wrappendOnExitScript, view)
			} catch (e: Throwable) {
				LOG.error("Error in onExit script of ScenarioStep '$name'")
			}
		}
	}

	private val wrappendOnExitScript: Script
		get() =
			Script(
				code = onExitScript!!,
				origin = "${Translations.getString("scenarioStep.issueOrigin.name")} '$name'",
				context = Translations.getString("graph.property.scenario.onExit.name"))

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

	override var storableId: Int = 0

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

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
}