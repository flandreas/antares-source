package io.antarescircuit.jabbah.graph.model.graph

import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Platform-independent [Command] for changing the name of a [Graph].
 * When changed using the property mechanism on Java/Swing, the generic [Command] is used.
 */
class GraphNameCommand(
    private val view: DrawingView<GraphElementView<*>, GraphView>,
    private val oldName: Name,
    private val newName: Name,
) : AbstractCommand("graph.property.GraphViewImpl.name"), Undoable {

    override fun execute() {
        view.drawing.graph?.name = newName
    }

    override fun undo() {
        view.drawing.graph?.name = oldName
    }
}