package ch.scorpion.antares.view.net.tunnel

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.app.ComponentCustomizer
import ch.scorpion.jabbah.graph.library.BaseLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryTreeViewTransferHandler
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferable
import java.awt.Point
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.TransferHandler

/**
 * Used for dragging a global [TunnelView] into an edited circuit.
 */
class GlobalTunnelTransferHandler(
    private val controller: GlobalTunnelPanelController
) : TransferHandler() {

    override fun getSourceActions(c: JComponent?): Int = COPY

    override fun canImport(support: TransferSupport?): Boolean = false

    override fun createTransferable(c: JComponent?): Transferable? {
        return findTunnelViewLibraryElement()?.let {
            dragImage = LibraryTreeViewTransferHandler.getIcon(it)
            dragImageOffset = Point(0, 0)
            val instance = it.getNewInstance<GraphElement>()
            (instance as TunnelView).name = controller.selectedTunnelName!!
            (instance as TunnelView).isGlobal = true
            GraphElementViewTransferable.of(instance, it, TunnelCustomizer(controller.selectedTunnelName!!))
        }
    }

    private fun findTunnelViewLibraryElement(): LibraryElement? {
        LibraryModule.libraryHolder.library.expandedImports.libraries.forEach { lib ->
            val elem = lib.firstLocalItemOrNull { elem ->
               elem is BaseLibraryElement && elem.id == AntaresViewModule.TUNNEL
            }
            if (elem != null) {
                return elem as LibraryElement
            }
        }
        return null
    }

    /**
     * Decouples [ComponentCustomizer] from [GlobalTunnelPanelController.selectedTunnelName]
     * to support selection changes during undo/redo operations.
     */
    private class TunnelCustomizer(
        private val tunnelName: String
    ) : ComponentCustomizer {

        override fun customizeAddedComponent(component: Component, drawing: Drawing<*>) {
            (component as TunnelView).name = tunnelName
            component.isGlobal = true
        }
    }
}