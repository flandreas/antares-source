package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Platform-independent [Command] for changing the name of a [Graph].
 * When changed using the property mechanism on Java/Swing, the generic [Command] is used.
 */
class GraphNameCommand(
    private val view: DrawingView<GraphView>,
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