package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Opens the currently selected [SubGraphVerticeView] in a new [GraphNavigationPanel]
 * in the [GraphDesktop].
 */
class OpenGraphNavigationPanelAction(
        viewManager: ViewManager = DrawViewModule.viewManager,
        private val eventBus: EventBus = BaseModule.eventBus,
        var subGraphVerticeView: SubGraphVerticeView<*>? = null
) : AbstractSelectionAwareAction("graph.action.openSubGraph", eventBus, viewManager) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        eventBus.post(OpenSubGraphRequest(subGraphVerticeView ?: getSingleSelection() as SubGraphVerticeView<*>, quickMode = true))
    }

    override fun calculateEnabled(): Boolean {
        return if (subGraphVerticeView != null) true else getSelectionCount() == 1 && getSingleSelection() is SubGraphVerticeView<*>
    }
}