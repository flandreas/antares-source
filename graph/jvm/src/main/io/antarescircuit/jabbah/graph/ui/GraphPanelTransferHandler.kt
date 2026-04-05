package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.drawable.Orientable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.ComponentTransferHandler
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
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
        if (dropComponent !is GraphElementView<*>
	        || transferable.getTransferData(GraphElementViewTransferable.FLAVOR) !is GraphElementViewTransferableData
        ) {
			return false
        }
	    val data = transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData

	    if (!checkEditability()) {
			return false
	    }

	    if (!checkGraphType(data.libraryElement)) {
			return false
	    }

        if (dropComponent.model !is SubGraphVertice) {
            return super.canImport(dropComponent, transferable)
        }

	    if (data.libraryElement !is ContainerLibraryElement) {
		    return super.canImport(dropComponent, transferable)
	    }

        val dropVertice = dropComponent.model as SubGraphVertice
	    val targetUUID = (editor.drawing as GraphView).graph!!.uuid

	    if (!checkNoGraphCycle(dropVertice, targetUUID)) {
			return false
	    }

	    val targetLibrary = repository.getContainingLibrary(targetUUID)!!
	    val sourceLibraryUUID = data.libraryElement.library!!.uuid
	    if (!checkImportingLibrary(dropVertice, targetLibrary, sourceLibraryUUID)) {
			return false
	    }

        return true
    }

	override fun addComponent(
		dropComponent: Component,
		transferable: Transferable
	): Component {
		val data = transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData
		val rotation = if (dropComponent.useRotation) {
			dropComponent.rotation
		} else if (dropComponent is Orientable && dropComponent.useOrientation) {
			dropComponent.orientation.rotation
		} else {
			Rotation.R0
		}
		return service.addGraphElementViewFromLibrary(data.libraryElement, dropComponent.location, rotation, editor, data.customizer)
	}

	private fun checkEditability(): Boolean {
		if (!editor.view.editable) {
			BaseModule.eventBus.post(ComponentMessage(
				ComponentMessageType.Error,
				null,
				"graph.readonly.cannotDrop.msg"))
			return false
		}
		return true
	}

	private fun checkNoGraphCycle(dropVertice: SubGraphVertice, targetUUID: UUID): Boolean {
		if (repository.graphContainsRecursively(dropVertice.graphUUID!!, targetUUID)) {
			LOG.trace("Prevent dropping '${dropVertice.name}' in order to prevent Graph cycle")
			JOptionPane.showMessageDialog(
				Frame.getFrames()[0],
				Translations.getString("graph.cycleError.msg"),
				Translations.getString("graph.action.addElementToGraph.name"),
				JOptionPane.ERROR_MESSAGE)
			return false
		}
		return true
	}

	private fun checkImportingLibrary(dropVertice: SubGraphVertice, targetLibrary: Library, sourceLibraryUUID: UUID): Boolean {
		if (!targetLibrary.expandedImports.contains(sourceLibraryUUID)) {
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

	private fun checkGraphType(sourceElement: LibraryElement): Boolean {
		val targetType = (editor.drawing as GraphView).graph!!.type
		return targetType.checkImport(sourceElement)?.let { msg ->
			JOptionPane.showMessageDialog(
				Frame.getFrames()[0],
				msg,
				Translations.getString("graph.action.addElementToGraph.name"),
				JOptionPane.ERROR_MESSAGE)
			false
		} ?: true
	}
}