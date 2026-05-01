package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.app.DisplayIdsAction
import io.antarescircuit.jabbah.edit.view.EditContextMenuProvider
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.container.ResetSubGraphVerticeViewAction
import io.antarescircuit.jabbah.graph.container.editsubgraph.EditSubGraphVerticeViewAction
import io.antarescircuit.jabbah.graph.ui.documentation.OpenDocumentationAction
import io.antarescircuit.jabbah.graph.ui.graphviewer.OpenSubGraphViewerAction
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.JPopupMenu

open class GraphContextMenuProvider(
	application: Application
) : EditContextMenuProvider() {

	companion object {
		private val openGraphAction by lazy { OpenGraphNavigationAction() }
		private val openGraphActionWrapper by lazy { ActionWrapperSwing(openGraphAction) }
		private val resetSubGraphAction by lazy { ActionWrapperSwing(ResetSubGraphVerticeViewAction()) }
		private val displayIdsAction by lazy { ActionWrapperSwing(DisplayIdsAction() { it !is NodeView<*>})  }
		private val openDocumentationAction by lazy { ActionWrapperSwing(OpenDocumentationAction()) }
	}

	private val extractMetaGraphAction = ActionWrapperSwing(ExtractMetaGraphAction(application.controller))

	private fun getGraphApplicationContextHolder(view: View<*>): GraphApplicationContextHolder? =
		view.applicationContextHolder as GraphApplicationContextHolder?

	override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		openGraphAction.subGraphVerticeView = null
		if (getGraphApplicationContextHolder(view)?.scheduler?.isActive == true) {
			addExecutionActions(view, x, y, menu)
		} else {
			super.fillContextMenu(view, x, y, menu)
		}
	}

	override fun addActions(view: View<*>, popupMenu: JPopupMenu) {
		super.addActions(view, popupMenu)
		popupMenu.addSeparator()
		popupMenu.add(displayIdsAction)
		if (view.name == GraphFrameController.MAIN_EDITOR_NAME) {
			popupMenu.add(openGraphActionWrapper)
			getGraphApplicationContextHolder(view)?.let {
				popupMenu.add(ActionWrapperSwing(OpenSubGraphViewerAction(applicationName, it)))
				popupMenu.add(ActionWrapperSwing(EditSubGraphVerticeViewAction(it)))
			}
			popupMenu.add(resetSubGraphAction)
			popupMenu.add(extractMetaGraphAction)
			addApplicationSpecificActions(view, popupMenu)
		}
		popupMenu.addSeparator()
		if (view.name == GraphFrameController.MAIN_EDITOR_NAME) {
			popupMenu.add(openDocumentationAction)
		}
		popupMenu.add(helpAction)
	}

	protected open fun addApplicationSpecificActions(view: View<*>, popupMenu: JPopupMenu) {
		// empty
	}

	private fun addExecutionActions(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
		menu.removeAll()
		val drawable = (view as DrawingView<*,*>).drawing.getDrawableAt(x, y)
		if (drawable is ActorView) {
			addExecutionActions(view, drawable, menu)
		}
	}

	private fun addExecutionActions(view: View<*>, actorView: ActorView, menu: JPopupMenu) {
		if (actorView is SubGraphVerticeView<*>) {
			openGraphAction.subGraphVerticeView = actorView
			openGraphAction.enabled = true
			menu.add(openGraphActionWrapper)
			getGraphApplicationContextHolder(view)?.let {
				val action = OpenSubGraphViewerAction(applicationName, it, actorView)
				action.enabled = true
				menu.add(ActionWrapperSwing(action))
			}
		}
	}
}