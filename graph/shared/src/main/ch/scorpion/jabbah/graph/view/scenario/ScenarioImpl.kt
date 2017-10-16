package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TextProperty
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
        override var name: String = "",
        private var conditionScript: String = ""
) : Scenario {

    private val steps: MutableList<ScenarioStep> by lazy { mutableListOf<ScenarioStep>() }

    private var isLoading: Boolean = false

    var conditionProperty: TextProperty
        get() = TextProperty(conditionScript)
        set(value) { conditionScript = value.text!! }

    /** ---- [Any] */

    override fun toString(): String = name

    /** ---- [Scenario] interface */

    override var id: Int = 0

    override var description: TextProperty = TextProperty(null)

    override val stepCount: Int get() = steps.size

    override fun dispose() {
        steps.forEach { it.dispose() }
        steps.clear()
    }

    override val condition: (DrawingView<GraphView<GraphElementView<*>>>, ScriptGateway) -> Boolean
        get() = { v,sg -> sg.condition(Script(code = conditionScript, origin = "Scenario '$name'", context = "Condition"), v) }

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
        description.text?.let { writer.writeString("desc", description.text!!) }
        writer.writeString("condition", conditionScript)
        writer.writeStorables("steps", getStorableChildren())
    }

    override fun read(reader: StoreReader) {
        isLoading = true
        id = reader.readInt("id")
        name = reader.readString("name")
        description = TextProperty(reader.readOptionalString("desc"))
        conditionScript = reader.readString("condition")
        for (step in reader.readStorables("steps")) {
            addStep(step as ScenarioStep)
        }
        isLoading = false
    }

    override fun getStorableChildren(): Iterator<Storable> = steps.iterator()
}