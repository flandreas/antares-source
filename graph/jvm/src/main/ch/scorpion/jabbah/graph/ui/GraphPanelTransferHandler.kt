package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentTransferHandler
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.JOptionPane

/**
 * A [ComponentTransferHandler] that prevents cyclic [Graph] structures.
 */
class GraphPanelTransferHandler(
	private val service: GraphViewAppService = GraphViewModule.graphViewAppService,
	editor: Editor,
	eventBus: EventBus,
	flavour: DataFlavor,
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder
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
			return false
        }
	    val data = transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData
        if (!editor.view.editable) {
			BaseModule.eventBus.post(ComponentMessage(
				ComponentMessageType.Error,
				null,
				"graph.readonly.cannotDrop.msg"))
            return false
        }

        if (dropComponent.model !is SubGraphVertice) {
            return super.canImport(dropComponent, transferable)
        }

	    if (data.libraryElement !is ContainerLibraryElement) {
		    return super.canImport(dropComponent, transferable)
	    }

        val dropVertice = dropComponent.model as SubGraphVertice?
	    val targetUUID = (editor.drawing as GraphView).graph!!.uuid

        if (repository.graphContainsRecursively(dropVertice!!.graphUUID!!, targetUUID)) {
            LOG.trace("Prevent dropping '${dropVertice.name}' in order to prevent Graph cycle")
            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                Translations.getString("graph.cycleError.msg"),
                Translations.getString("graph.action.addElementToGraph.name"),
                JOptionPane.ERROR_MESSAGE)
	        return false
        }

	    val targetLibrary = repository.getContainingLibrary(targetUUID)!!
	    if (!targetLibrary.expandedImports.contains(data.libraryElement.library!!.uuid)) {
			LOG.trace("Prevent dropping '${dropVertice.name}' from non-importing Library")
		    JOptionPane.showMessageDialog(
			    Frame.getFrames()[0],
			    Translations.getString("graph.dependencyError.msg"),
			    Translations.getString("graph.action.addElementToGraph.name"),
			    JOptionPane.ERROR_MESSAGE)
		    return false
	    }

        return true
    }

	override fun addComponent(dropComponent: Component, transferable: Transferable): Component {
		val data = transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData
		return service.addGraphElementViewFromLibrary(data.libraryElement, dropComponent.location, editor)
	}
}