package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase

/**
 * Posted by [UsecaseView] on its [EventBus] when the user defines the current [Usecase]
 * by changing the selection in the [UsecaseView].
 */
data class UsecaseSelectionEvent(
	val graphView: GraphView,
	val usecase: Usecase?
)

/**
 * Displays the [Usecase]s of a [GraphView] and allows the user to inspect, add, remove and edit them.
 */
interface UsecaseView : UIView {

	/** The [GraphView] whose [Usecase]s are displayed. */
	var graphView: GraphView?
}

class UsecaseViewController : AbstractUIController<UsecaseView>() {

	/** The [GraphView] whose [Usecase]s. */
	var graphView: GraphView? = null
		set(value) {
			if (field !== value) {
				field = value
				view.graphView = value
			}
		}
}