package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.edit.app.CloseViewAction
import ch.scorpion.jabbah.graph.ui.GraphMenuBarBuilder
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class GraphViewerMenuBar(
	controller: GraphViewerController
) : JMenuBar() {

	private val closeViewAction = CloseViewAction(controller.drawingView)
	private val closeViewWrapper = ActionWrapperSwing(closeViewAction)

	private val toggleApplicationModeWrapper = ActionWrapperSwing(controller.toggleApplicationModeAction)
	private val pauseOrResumeWrapper = ActionWrapperSwing(controller.pauseOrResumeAction)

	init {
		add(fillFileMenu(MenuBarBuilder.createFileMenu()))
		add(fillViewMenu(MenuBarBuilder.createViewMenu()))
		add(fillExecutionMenu(GraphMenuBarBuilder.createExecutionMenu()))
	}

	fun dispose() {
		closeViewWrapper.dispose()
		closeViewAction.dispose()
		toggleApplicationModeWrapper.dispose()
		pauseOrResumeWrapper.dispose()
	}

	private fun fillFileMenu(menu: JMenu): JMenu {
		menu.add(JMenuItem(closeViewWrapper))
		return menu
	}

	private fun fillViewMenu(menu: JMenu): JMenu {
		MenuBarBuilder.addZoomActions(menu)
		return menu
	}

	private fun fillExecutionMenu(menu: JMenu): JMenu {
		menu.add(JCheckBoxMenuItem(toggleApplicationModeWrapper))
		menu.add(JMenuItem(pauseOrResumeWrapper))
		return menu
	}
}