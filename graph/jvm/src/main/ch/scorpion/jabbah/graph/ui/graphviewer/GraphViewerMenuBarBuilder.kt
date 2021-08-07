package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.app.MenuBarBuilder
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.edit.app.CloseViewAction
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class GraphViewerMenuBarBuilder {

	private val menuBar = JMenuBar()

	private val fileMenu = MenuBarBuilder.createFileMenu()
	private val viewMenu = MenuBarBuilder.createViewMenu()

	init {
		fillFileMenu(fileMenu)
		fillViewMenu(viewMenu)

		menuBar.add(fileMenu)
		menuBar.add(viewMenu)
	}

	fun build(): JMenuBar = menuBar

	private fun fillFileMenu(menu: JMenu) {
		menu.add(JMenuItem(ActionWrapperSwing(CloseViewAction())))
	}

	private fun fillViewMenu(menu: JMenu) {
		MenuBarBuilder.addZoomActions(menu)
	}
}