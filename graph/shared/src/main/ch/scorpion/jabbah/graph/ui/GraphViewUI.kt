package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewExecutionController
import ch.scorpion.jabbah.execution.scheduler.Scheduler

/**
 * Represents methods to be implemented by a class that displays and simulates
 * a [GraphView], as needed by [GraphViewExecutionController]
 */
interface GraphViewUI {

	/** The [DrawingView] that displays the [GraphView].*/
	val drawingView: DrawingView<GraphView>

	/**
	 * Determines if the [GraphView] is basically editable. This property is not necessarily supposed to
	 * also incorporate the [Scheduler]'s state; this is done by the calling [GraphViewExecutionController].
	 * */
	val isEditable: Boolean

	/**
	 * Determines whether the displayed [GraphView] is detached, i.e. whether it doesn't show accurate
	 * signal states due to shallow execution.
	 */
	val isDetached: Boolean

	/**
	 * Deselects all [Component]s' in the [DrawingView], including those possibly selected in
	 * sub-views.
	 */
	fun deselectAll()
}