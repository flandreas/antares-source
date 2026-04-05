package io.antarescircuit.jabbah.graph.view.app

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.app.AbstractGraphViewCommand
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule


class ExtractMetaGraphCommand(
    private val graphName: TranslatableText,
    private val type: GraphType,
    drawingView: DrawingView<GraphView>,
    private val componentIds: Collection<Int>,
    private val libraryDirectory: LibraryDirectory
) : AbstractGraphViewCommand("graph.command.extractMetaGraph", drawingView) {

    private var uuid: UUID? = null

    override fun execute() {
        uuid = GraphViewModule.metaGraphService.extractMetaGraph(graphName, type, drawingView, componentIds, libraryDirectory)
    }

    override fun notifyUndo() {
        if (uuid != null) {
            with (libraryDirectory.library!!) {
                getContainerLibraryElement(this@ExtractMetaGraphCommand.uuid!!)?.let {
                    libraryService.removeLibraryItem(this, it)
                }
            }
        }
    }
}