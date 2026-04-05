package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * A request to open a sub [MetaGraph] of a [SubGraphVerticeView].
 *
 * @property subGraphVerticeView the [SubGraphVerticeView] from where the request originates
 * @property newView `true` if the user wishes that the [MetaGraph] is opened in a new, additional view
 * @property quickMode `true` if the user wishes that the resulting view changes happen quickly, for example
 *      without time-consuming animations.
 */
data class OpenSubGraphRequest(
	val subGraphVerticeView: SubGraphVerticeView<*>,
	val newView: Boolean,
	val quickMode: Boolean
) {
	/**
	 * Checks if [subGraphVerticeView] has a broken reference, and if so, post an info message
	 * and return `true`.
	 */
	fun notifyIfBroken(eventBus: EventBus): Boolean {
		if ((subGraphVerticeView.model as SubGraphVerticeRef?)?.hasDesignError == true) {
			eventBus.post(ComponentMessage(ComponentMessageType.Info, subGraphVerticeView, "graph.element.brokenRef.cannotOpen.msg"))
			return true
		}
		return false
	}
}

data class OpenHierarchySubGraphRequest(
	val subGraphVerticeView: SubGraphVerticeView<*>,
	val rootGraphView: GraphView)