package ch.scorpion.jabbah.graph.ui.portrenaming

import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.GraphPortNameCommand
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.GraphView

/** Cannot use [GraphPortView] because that one's instances are replaced in undo/redo's snapshots. */
data class GraphPortViewItem(
    val id: Int,
    var name: String,
    val type: PortType
) {
    override fun toString(): String = name
}

interface GraphPortRenamingPanel : UIView {
    fun setErrorText(text: String)
}

class GraphPortRenamingController(
    val editor: Editor,
) : AbstractUIController<GraphPortRenamingPanel>() {

    val items: List<GraphPortViewItem> = loadItems()

    val graphView: GraphView get() = editor.drawing as GraphView

    fun updateName(index: Int, newName: String) {
        try {
            graphView.getWithId(items[index].id)?.let {
                val oldName = (it.model as GraphPort<*>).name
                (it.model as GraphPort<*>).name = newName
                items[index].name = newName
                editor.commandManager.register(GraphPortNameCommand(editor, it.model.id, oldName, newName))
                view.setErrorText("")
            }
        } catch (e: Exception) {
            view.setErrorText(e.message ?: "Error")
        }
    }

    private fun loadItems(): List<GraphPortViewItem> =
        graphView.getGraphPortViews()
            .map { GraphPortViewItem(it.id, it.model.name!!, it.model.portType) }
            .sortedWith(compareBy(GraphPortViewItem::type, GraphPortViewItem::name))
}