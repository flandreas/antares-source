package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.io.Storable

/**
 * A [ScenarioStep] is an individual step of a [Scenario].
 *
 * TODO This interface has a signature similar to [Scenario]. Consider redesigning the structure.
 */
interface ScenarioStep : Storable {

    /** The identification of this [ScenarioStep] that is unique within a [Scenario].*/
    var id: Int

    /**
     * The displayable name of this [ScenarioStep]. Note that this name can be internationalized and should
     * not be used for technical identifications.
     */
    var name: String

    /** The text to be displayed above the explained [GraphView] when this [ScenarioStep] is active.*/
    var description: TextProperty

    /**
     * Returns the condition that determines whether this {@link ScenarioStep} is triggered depending on the current state
     * of a [DrawingView] and its GraphView.
     *
     * A [ScenarioStep] will only trigger if its owning [Scenario] also triggers, which is controlled by
     * client classes that evaluate the return condition. Hence, it's not necessary that the returned condition contains
     * terms that check the [Scenario] condition as well.
     */
    val condition: (DrawingView<GraphView<GraphElementView<*>>>, ScriptGateway) -> Boolean

    fun dispose()

    /**
     * Notifies this [ScenarioStep] that it has become the active [ScenarioStep] in a [GraphView]'s
     * current [Scenario].
     */
    fun activate(view: DrawingView<GraphView<GraphElementView<*>>>)

    /**
     * Notifies this [ScenarioStep] that it is no longer the active [ScenarioStep] in a [GraphView]'s
     * current [Scenario].
     */
    fun passivate(view: DrawingView<GraphView<GraphElementView<*>>>)
}

/**
 * Signals that a particular [ScenarioStep] has been detected in a [GraphView].
 * The [ScenarioStep] is `null` if the [ScenarioStep] cannot be determined any more.
 */
data class ScenarioStepEvent(val graphView: GraphView<*>, val oldStep: ScenarioStep?, val newStep: ScenarioStep?)