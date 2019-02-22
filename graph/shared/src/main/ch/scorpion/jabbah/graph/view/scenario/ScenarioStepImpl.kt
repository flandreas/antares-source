package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [ScenarioStep] interface.
 */
class ScenarioStepImpl(
	private val scriptGateway: ScriptGateway,
	name: String
) : ScenarioStep {

	@Suppress("unused")
	constructor() : this(ScriptModule.scriptGateway, "")

	companion object {
		private val LOG by logger(ScenarioStepImpl::class)
	}

	/** The JavaScript predicate that determines whether this [ScenarioStep] is active. */
	private var conditionScript: String? = null

	/** The JavaScript expressions to be executed when this [ScenarioStep] is activated. */
	private var onEntryScript: String? = null

	/** The JavaScript expressions to be executed when this {@link ScenarioStep} is passivated. */
	private var onExitScript: String? = null

	/** Caches the parsed highlight IDs of `highlightIds' as [Int].*/
	private var highlightIntIdsCache: List<Int>? = null

	/** ---- [Any] */

	override fun toString(): String = StringUtils.replaceNegation(name)

	/** ---- UI editable properties */

	var conditionProperty: TextProperty
		get() = TextProperty(conditionScript)
		set(value) {
			conditionScript = value.text
		}

	var onEntryProperty: TextProperty
		get() = TextProperty(onEntryScript)
		set(value) {
			onEntryScript = value.text
		}

	var onExitProperty: TextProperty
		get() = TextProperty(onExitScript)
		set(value) {
			onExitScript = value.text
		}

	/** ---- [ScenarioStep] interface */

	override var id: Int = 0

	override var name: String
		get() = translatableName.getTranslation()
		set(value) {
			if (name != value) {
				translatableName = translatableName.withTranslation(value)
			}
		}

	override var translatableName: TranslatableText = TranslatableText(name)
		set(value) {
			if (field != value) {
				field = value
			}
		}

	override var description: String?
		get() = translatableDescription.getOptionalTranslation()
		set(value) {
			if (description != null && value != null) {
				translatableDescription = translatableDescription.withTranslation(value)
			}
		}

	override var translatableDescription: TranslatableText = TranslatableText()
		set(value) {
			if (field != value) {
				field = value
			}
		}

	override var highlightIds: String? = null
		set(value) {
			if (field != value) {
				field = value
				highlightIntIdsCache = null
			}
		}

	override val condition: (DrawingView<GraphView<GraphElementView<*>>>, ScriptGateway) -> Boolean
		get() = { v, sg ->
			if (StringUtils.isNotEmpty(conditionScript)) sg.condition(
				Script(code = conditionScript!!, origin = "ScenarioStep '$name'", context = "condition"), v) else false
		}

	override fun dispose() {
		// empty
	}

	override fun activate(view: DrawingView<GraphView<GraphElementView<*>>>) {
		if (StringUtils.isNotEmpty(onEntryScript)) {
			try {
				LOG.debug("Activate ScenarioStep '$name'")
				scriptGateway.exec(Script(code = onEntryScript!!, origin = "ScenarioStep '$name'", context = "onEntry"), view)
			} catch (e: Throwable) {
				LOG.error("Error in onEntry script of ScenarioStep '$name'")
			}
		}
	}

	override fun passivate(view: DrawingView<GraphView<GraphElementView<*>>>) {
		if (StringUtils.isNotEmpty(onExitScript)) {
			try {
				LOG.debug("Passivate ScenarioStep '$name'")
				scriptGateway.exec(Script(code = onExitScript!!, origin = "ScenarioStep '$name'", context = "onExit"), view)
			} catch (e: Throwable) {
				LOG.error("Error in onExit script of ScenarioStep '$name'")
			}
		}
	}

	override val highlightIdsAsInt: List<Int>
		get() {
			if (highlightIntIdsCache == null) {
				highlightIntIdsCache = mutableListOf()
				if (StringUtils.isNotEmpty(highlightIds)) {
					highlightIds!!
						.split(delimiters = *charArrayOf(','))
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
		writer.writeStorables("name", translatableName.allTranslations())
		if (!translatableDescription.isEmpty) {
			writer.writeStorables("desc", translatableDescription.allTranslations())
		}
		highlightIds?.let { writer.writeString("highlightIds", it) }
		conditionScript?.let { writer.writeString("condition", conditionScript!!) }
		onEntryScript?.let { writer.writeString("onEntry", onEntryScript!!) }
		onExitScript?.let { writer.writeString("onExit", onExitScript!!) }
	}

	override fun read(reader: StoreReader) {
		id = reader.readInt("id")
		if (reader.hasAttribute("name")) {
			// backward compatibility
			name = reader.readString("name")
		}
		if (reader.hasElement("name")) {
			translatableName = TranslatableText(reader.readStorables("name"))
		}
		if (reader.hasAttribute("desc")) {
			// backward compatibility
			description = reader.readString("desc")
		}
		if (reader.hasElement("desc")) {
			translatableDescription = TranslatableText(reader.readStorables("desc"))
		}

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