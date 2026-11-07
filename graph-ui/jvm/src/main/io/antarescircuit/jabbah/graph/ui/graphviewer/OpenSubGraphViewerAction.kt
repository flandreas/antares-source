package io.antarescircuit.jabbah.graph.ui.graphviewer

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Opens the currently selected [SubGraphVerticeView] in a new [SubGraphViewerViewSwing],
 * or the specified [subGraphVerticeView] during execution, when there is no selection.
 */
class OpenSubGraphViewerAction(
	private val applicationName: String,
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val subGraphVerticeView: SubGraphVerticeView<*>? = null
) : AbstractSelectionAwareAction("graph.action.newGraphViewer") {

	companion object {
		private val LOG by logger(OpenSubGraphViewerAction::class)
	}

	override val opensDialog: Boolean get() = true

	init {
		updateEnabled()
	}

	override fun execute(event: ActionEvent) {
		val vv = subGraphVerticeView ?: (singleSelection as SubGraphVerticeView<*>)
		val graphView = vv.createSubGraphView(applicationContextHolder.scheduler)
		LOG.userTrail("Open SubGraph ${graphView.graph?.name} in separate viewer")
		SubGraphViewerViewSwing(applicationName, graphView, applicationContextHolder)
	}

	override fun calculateEnabled(): Boolean =
		subGraphVerticeView != null || selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
}