package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.dsl.SemanticAnalyser
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.text.description.Describable
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.io.Storable

/**
 * A [ScenarioStep] is an individual step of a [Scenario].
 *
 * TODO This interface has a signature similar to [Scenario]. Consider redesigning the structure.
 */
interface ScenarioStep : Namable, Describable, Storable {

	/** The identification of this [ScenarioStep] that is unique within a [Scenario].*/
	var id: Int

	/**
	 * The [GraphView] to which this [ScenarioStep] belongs.
	 * Maintained only to be able to create a [SemanticAnalyser] on [condition] that knows all
	 * [GraphPort]s of [GraphView].
	 */
	var graphView: GraphView?

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
	val condition: (SignalHandler, DrawingView<GraphView>) -> Boolean

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