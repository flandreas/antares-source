package io.antarescircuit.jabbah.graph.ui.hierarchy

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.graph.library.CurrentLibraryEvent
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.editor.SubGraphVerticeViewEvent
import io.antarescircuit.jabbah.graph.view.vertice.OpenHierarchySubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

interface GraphHierarchyView : UIView {
	fun refresh()
	fun handleRemove(subGraphVerticeView: SubGraphVerticeView<*>)
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

	private val deleteHandler: EventHandler<SubGraphVerticeViewEvent> = {
		if (it.type == SubGraphVerticeViewEvent.Type.REMOVE) {
			view.handleRemove(it.subGraphVerticeView)
		}
	}

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = {
		setRootGraphView(null)
	}

	init {
		eventBus.register(SubGraphVerticeViewEvent::class, deleteHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
		updateActions()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(deleteHandler)
		eventBus.unregister(currentLibraryHandler)
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

	private inner class RefreshAction : AbstractAction("graph.hierarchy.action.refresh", "/img/refresh.png") {
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