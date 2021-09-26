package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.io.Storable

/**
 * A [ScenarioStep] is an individual step of a [Scenario].
 *
 * TODO This interface has a signature similar to [Scenario]. Consider redesigning the structure.
 */
interface ScenarioStep : Namable, Describable, Storable {

	/** The identification of this [ScenarioStep] that is unique within a [Scenario].*/
	var id: Int

	/** The comma-separated, persistent list of [Component] IDs to be highlighted when this [ScenarioStep] is active*/
	var highlightIds: String?

	/**
	 * Returns the [List] of [Component] IDs to be highlighted when this [ScenarioStep] is active.
	 * @throws IllegalStateException if [highlightIds] contains data in an illegal format
	 */
	val highlightIdsAsInt: List<Int>

	/**
	 * Returns the condition that determines whether this [ScenarioStep] is triggered depending on the current state
	 * of a [DrawingView] and its GraphView.
	 *
	 * A [ScenarioStep] will only trigger if its owning [Scenario] also triggers, which is controlled by
	 * client classes that evaluate the return condition. Hence, it's not necessary that the returned condition contains
	 * terms that check the [Scenario] condition as well.
	 */
	val condition: (DrawingView<GraphView>) -> Boolean

	fun dispose()

	fun executionStart(graphView: GraphView, signalHandler: SignalHandler)

	/**
	 * Notifies this [ScenarioStep] that it has become the active [ScenarioStep] in a [GraphView]'s
	 * current [Scenario].
	 */
	fun activate(view: DrawingView<GraphView>)

	/**
	 * Notifies this [ScenarioStep] that it is no longer the active [ScenarioStep] in a [GraphView]'s
	 * current [Scenario].
	 */
	fun passivate(view: DrawingView<GraphView>)

}

/**
 * Signals that a particular [ScenarioStep] has been detected in a [GraphView].
 * The [ScenarioStep] is `null` if the [ScenarioStep] cannot be determined any more.
 */
data class ScenarioStepEvent(val graphView: GraphView, val oldStep: ScenarioStep?, val newStep: ScenarioStep?)