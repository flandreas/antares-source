package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentTransferHandler
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.library.LibraryHolder
import java.awt.datatransfer.DataFlavor
import javax.swing.JOptionPane
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice



/**
 * A [ComponentTransferHandler] that prevents cyclic [Graph] structures.
 */
class GraphPanelTransferHandler(
    editor: Editor,
    eventBus: EventBus,
    flavour: DataFlavor,
    private val libraryHolder: LibraryHolder
) : ComponentTransferHandler(editor, eventBus, flavour) {

    private val LOG by logger(GraphPanelTransferHandler::class)

    override fun canImport(dropComponent: Component): Boolean {
        if (dropComponent !is GraphElementView<*>) {
            return super.canImport(dropComponent)
        }
        if (!editor.view.editable) {
            return false
        }

        if (dropComponent.model !is SubGraphVertice) {
            return super.canImport(dropComponent)
        }

        val dropVertice = dropComponent.model as SubGraphVertice?
        val dropGraph = libraryHolder.library.getMetaGraph(dropVertice!!.graphUUID!!).graph!!.model

        val canImport = !libraryHolder.library.graphContainsRecursively(
                dropGraph!!.uuid,
                (editor.drawing as GraphView<*>).graph!!.uuid)

        if (!canImport) {
            LOG.debug("Preventing dropping '${dropVertice.name}' in order to prevent Graph cycle")
            JOptionPane.showMessageDialog(
                null,
                Translations.getString("graph.cycleError.msg"),
                Translations.getString("graph.action.addElementToGraph.name"),
                JOptionPane.ERROR_MESSAGE)
        }

        return canImport
    }
}