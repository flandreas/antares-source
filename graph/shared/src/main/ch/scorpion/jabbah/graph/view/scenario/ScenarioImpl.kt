package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [Scenario] interface.
 */
class ScenarioImpl(
	name: String = "",
	private var conditionScript: String = ""
) : Scenario {

	companion object {
		private val LOG by logger(ScenarioImpl::class)
	}

	private val steps: MutableList<ScenarioStep> by lazy { mutableListOf<ScenarioStep>() }

	private var isLoading: Boolean = false

	var conditionProperty: TextProperty
		get() = TextProperty(conditionScript)
		set(value) {
			conditionScript = value.text!!
		}

	/** ---- [Any] */

	override fun toString(): String = StringUtils.replaceNegation(name)

	/** ---- [Scenario] interface */

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

	override val stepCount: Int get() = steps.size

	override fun dispose() {
		steps.forEach { it.dispose() }
		steps.clear()
	}

	override val condition: (DrawingView<GraphView<GraphElementView<*>>>, ScriptGateway) -> Boolean
		get() = { v, sg -> sg.condition(Script(code = conditionScript, origin = "Scenario '$name'", context = "Condition"), v) }

	override fun getScenarioSteps(): ImmutableList<ScenarioStep> = steps.toImmutableList()

	override fun getStep(id: Int): ScenarioStep = steps.first { it.id == id }

	override fun addStep(step: ScenarioStep) {
		addStep(step, steps.size)
	}

	override fun addStep(step: ScenarioStep, index: Int) {
		if (!steps.contains(step)) {
			if (!isLoading) {
				step.id = steps.size + 1
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

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		writer.writeString("name", name)
		writer.writeStorables("name", translatableName.allTranslations())
		if (!translatableDescription.isEmpty) {
			writer.writeStorables("desc", translatableDescription.allTranslations())
		}
		writer.writeString("condition", conditionScript)
		writer.writeStorables("steps", getStorableChildren())
	}

	override fun read(reader: StoreReader) {
		isLoading = true
		id = reader.readInt("id")
		if (reader.hasAttribute("name")) {
			// backward compatibility
			name = reader.readString("name")
		}
		if (reader.hasElement("name")) {
			translatableName = TranslatableText(reader.readStorables("name").map { it as Translation })
		}
		if (reader.hasAttribute("desc")) {
			// backward compatibility
			description = reader.readString("desc")
		}
		if (reader.hasElement("desc")) {
			translatableDescription = TranslatableText(reader.readStorables("desc").map { it as Translation })
		}

		conditionScript = reader.readString("condition")
		for (step in reader.readStorables("steps")) {
			addStep(step as ScenarioStep)
		}
		isLoading = false
	}

	override fun getStorableChildren(): Iterator<Storable> = steps.iterator()
}