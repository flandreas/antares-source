package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.event.ActionEvent

/**
 * Opens the currently selected [SubGraphVerticeView] in a new [GraphNavigationPanel]
 * in the [GraphDesktop].
 */
class OpenGraphNavigationPanelAction(
        viewManager: ViewManager,
        eventBus: EventBus
) : AbstractSelectionAwareAction("graph.action.openSubGraph", viewManager, eventBus) {

    override fun actionPerformed(e: ActionEvent?) {
        eventBus.post(OpenSubGraphRequest(getSingleSelection() as SubGraphVerticeView<*>, quickMode = true))
    }

    override fun calculateEnabled(): Boolean {
        return getSelectionCount() == 1 && getSingleSelection() is SubGraphVerticeView<*>
    }
}