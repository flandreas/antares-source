package ch.scorpion.jabbah.graph.ui.hierarchy

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.OpenHierarchySubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

interface GraphHierarchyView : UIView {
	fun refresh()
}

/**
 * Displays the hierarchy of [SubGraphVerticeViews][SubGraphVerticeView] of a [GraphView].
 */
class GraphHierarchyController(
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<GraphHierarchyView>() {

	val refreshAction: Action = RefreshAction()
	val openAction: Action = OpenAction()

	var rootGraphView: GraphView? = null
		private set

	var selectedSubGraphVerticeView: SubGraphVerticeView<*>? = null
		set(value) {
			field = value
			updateActions()
		}

	init {
		updateActions()
	}

	override fun dispose() {
		super.dispose()
		refreshAction.dispose()
	}

	fun setRootGraphView(graphView: GraphView?) {
		rootGraphView = graphView
		view.refresh()
		updateActions()
	}

	private fun updateActions() {
		refreshAction.enabled = rootGraphView != null
		openAction.enabled = selectedSubGraphVerticeView != null
	}

	private inner class RefreshAction : AbstractAction("graph.hierarchy.action.refresh") {
		override fun execute(event: ActionEvent) {
			view.refresh()
		}
	}

	/** Opens the currently selected [SubGraphVerticeView] in an additional [GraphDesktopViewItem]. */
	private inner class OpenAction : AbstractAction("file.action.open", "/img/openInPopup-20.png") {
		override fun execute(event: ActionEvent) {
			selectedSubGraphVerticeView?.let {
				eventBus.post(OpenHierarchySubGraphRequest(it, rootGraphView!!))
			}
		}
	}
}