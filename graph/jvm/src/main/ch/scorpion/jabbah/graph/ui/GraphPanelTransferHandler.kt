package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentTransferHandler
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
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
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository
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
	    val targetUUID = (editor.drawing as GraphView).graph!!.uuid

        if (repository.graphContainsRecursively(dropVertice!!.graphUUID!!, targetUUID)) {
            LOG.debug("Prevent dropping '${dropVertice.name}' in order to prevent Graph cycle")
            JOptionPane.showMessageDialog(
                null,
                Translations.getString("graph.cycleError.msg"),
                Translations.getString("graph.action.addElementToGraph.name"),
                JOptionPane.ERROR_MESSAGE)
	        return false
        }

	    if (data.libraryElement.library == ProjectModule.projectHolder.project && data.libraryElement.library!!.getOptionalMetaGraph(targetUUID) == null) {
		    LOG.debug("Prevent dropping project component into library graph")
		    JOptionPane.showMessageDialog(
			    null,
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