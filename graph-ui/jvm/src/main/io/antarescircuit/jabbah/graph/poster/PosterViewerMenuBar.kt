package io.antarescircuit.jabbah.graph.poster

import io.antarescircuit.jabbah.app.MenuBarBuilder
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Disposable
import io.antarescircuit.jabbah.edit.app.CloseViewAction
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class PosterViewerMenuBar(
    controller: PosterViewerController
) : JMenuBar(), Disposable {

    private val closeViewAction = CloseViewAction(controller.drawingView)
    private val closeViewWrapper = ActionWrapperSwing(closeViewAction)

    init {
        add(fillFileMenu(MenuBarBuilder.createFileMenu()))
        add(fillViewMenu(MenuBarBuilder.createViewMenu()))
    }

    override fun dispose() {
        closeViewWrapper.dispose()
        closeViewAction.dispose()

    }

    private fun fillFileMenu(menu: JMenu): JMenu {
        menu.add(JMenuItem(closeViewWrapper))
        return menu
    }

    private fun fillViewMenu(menu: JMenu): JMenu {
        MenuBarBuilder.addZoomActions(menu)
        return menu
    }
}