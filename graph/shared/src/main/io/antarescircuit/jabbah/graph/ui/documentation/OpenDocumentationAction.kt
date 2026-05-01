package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

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
                viewManager.activeView!!.view as DrawingView<GraphElementView<*>, GraphView>,
                singleSelection as SubGraphVerticeView<*>,
                metaGraph.documentation!!,
                metaGraph.name
            ))
        }
    }

    override fun calculateEnabled(): Boolean =
        selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
}