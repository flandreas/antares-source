package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.io.Storable

/**
 * A [Scenario] is a description of the current state of [GraphView],
 * for example determined by the current signals of the [InputPort]s.
 *
 * For example, an digital electronic SR Latch (implemented with NOR gates) supports the three scenarios "Set", "Reset"
 * and "Store". The "Set" scenario is triggered when the S input is 1 while R is 0. The "Reset" scenario is triggered
 * when the R input is 0 while S is 1. The "Store" scenario is triggered when both S and R are 0.
 *
 * The value of [Describable] is the text to be displayed above the explained [GraphView] when this [Scenario] is active.
 */
interface Scenario : Namable, Describable, Storable {

	/** Returns the identification of this [Scenario] that is unique within a [GraphView].*/
	var id: Int

	/** Returns the number of [ScenarioStep]s of this [Scenario].*/
	val stepCount: Int

	fun dispose()

	fun executionStart(graphView: GraphView, signalHandler: SignalHandler)

	/**
	 * Returns the condition that determines whether this [Scenario] is triggered depending on the current state
	 * of a [DrawingView] and its GraphView.
	 */
	val condition: (DrawingView<GraphView>) -> Boolean

	/** Returns the [ScenarioStep]s of this [Scenario].*/
	fun getScenarioSteps(): ImmutableList<ScenarioStep>

	/** Returns the [ScenarioStep] with the specified id.*/
	fun getStep(id: Int): ScenarioStep

	/** Adds the specified [ScenarioStep] to this [Scenario]. */
	fun addStep(step: ScenarioStep)

	/** Adds the specified [ScenarioStep] at index `index`.*/
	fun addStep(step: ScenarioStep, index: Int)

	/** Removes the specified [ScenarioStep] from this [Scenario].*/
	fun removeStep(step: ScenarioStep)

	/** Moves the specified [ScenarioStep] to a new position within this [Scenario].*/
	fun moveStep(step: ScenarioStep, newIndex: Int)

	/** Returns the index of `step` in this [Scenario], starting with 0.*/
	fun indexOf(step: ScenarioStep): Int
}

/**
 * Signals that a particular [Scenario] has been detected in a [GraphView].
 * The [Scenario] is `null`if the [Scenario] cannot be determined any more.
 */
data class ScenarioEvent(val graphView: GraphView, val scenario: Scenario?)
