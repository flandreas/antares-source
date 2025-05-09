package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Opens the documentation [Document] of the currently selected [SubGraphVerticeView]'s [MetaGraph]
 * in a new [GraphDesktopViewItem]
 */
class OpenDocumentationAction(
    viewManager: ContentViewManager = DrawViewModule.viewManager,
    eventBus: EventBus = BaseModule.eventBus,
) : AbstractSelectionAwareAction("graph.action.openDocumentation", eventBus, viewManager) {

    init {
        updateEnabled()
    }

    override fun execute(event: ActionEvent) {
        val metaGraph = LibraryModule.libraryHolder
            .getMetaGraph((singleSelection as SubGraphVerticeView<*>).model.graphUUID!!)

        if (metaGraph.documentation == null) {
            eventBus.post(ComponentMessage(
                source = singleSelection!!,
                messageKey = "graph.action.noDocumentation.text"
            ))
        } else {
            eventBus.post(OpenDocumentationRequest(
                viewManager.activeView!!.view as DrawingView<GraphView>,
                singleSelection as SubGraphVerticeView<*>,
                metaGraph.documentation!!,
                metaGraph.name
            ))
        }
    }

    override fun calculateEnabled(): Boolean =
        selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
}