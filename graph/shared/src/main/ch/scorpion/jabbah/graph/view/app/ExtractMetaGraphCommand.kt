package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.app.AbstractGraphViewCommand
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule


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