package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.graph.script.Script
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

	private val steps: MutableList<ScenarioStep> by lazy { mutableListOf() }

	private var isLoading: Boolean = false

	var conditionProperty: ScriptProperty
		get() = ScriptProperty(conditionScript)
		set(value) {
			conditionScript = value.script!!
		}

	/** ---- [Any] */

	override fun toString(): String = StringUtils.replaceNegation(name.value)

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

	override val condition: (DrawingView<GraphView>, ScriptGateway) -> Boolean
		get() = { view, sg ->
			sg.condition(
				Script(
					code = conditionScript,
					origin = "${Translations.getString("scenario.issueOrigin.name")} '$name'",
					context = Translations.getString("graph.property.scenario.condition.name")
				),
				view
			)
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

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		name.write("name", writer)
		description.write("desc", writer)
		writer.writeString("condition", conditionScript)
		writer.writeStorables("steps", getStorableChildren())
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

	override fun getStorableChildren(): Iterator<Storable> = steps.iterator()

	/** ---- [ScenariosImpl] */

	private fun getMaxId(): Int {
		if (steps.size == 0) {
			return 0
		}
		return steps.maxBy { it.id }!!.id
	}
}