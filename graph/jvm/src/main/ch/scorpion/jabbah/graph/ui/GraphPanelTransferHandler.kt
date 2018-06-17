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
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import java.awt.datatransfer.Transferable


/**
 * A [ComponentTransferHandler] that prevents cyclic [Graph] structures.
 */
class GraphPanelTransferHandler(
    editor: Editor,
    eventBus: EventBus,
    flavour: DataFlavor
) : ComponentTransferHandler(editor, eventBus, flavour) {

	companion object {
        private val LOG by logger(GraphPanelTransferHandler::class)
	}

	override fun extractTransferData(transferData: Any?): Any? {
		if (transferData is GraphElementViewTransferableData) {
			return transferData.graphElementView
		}
		return transferData
	}

    override fun canImport(dropComponent: Component, transferable: Transferable): Boolean {
        if (dropComponent !is GraphElementView<*> || transferable.getTransferData(GraphElementViewTransferable.FLAVOR) !is GraphElementViewTransferableData) {
            return super.canImport(dropComponent, transferable)
        }
	    val data = transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData
        if (!editor.view.editable) {
            return false
        }

        if (dropComponent.model !is SubGraphVertice) {
            return super.canImport(dropComponent, transferable)
        }

	    if (data.libraryElement !is ContainerLibraryElement) {
		    return super.canImport(dropComponent, transferable)
	    }

        val dropVertice = dropComponent.model as SubGraphVertice?
	    val dropGraph = data.libraryElement.library!!.getMetaGraph(dropVertice!!.graphUUID!!).graph!!.model

        val canImport = !data.libraryElement.library!!.graphContainsRecursively(
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