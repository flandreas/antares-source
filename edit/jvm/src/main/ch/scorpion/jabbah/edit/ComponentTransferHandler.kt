package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.drag.DropEvent
import ch.scorpion.jabbah.edit.module.EditModule
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.TransferHandler


/**
 * Handles drop gestures with [Component]s on [DrawingView]s.
 */
open class ComponentTransferHandler(
    protected val editor: Editor,
    private val eventBus: EventBus,
    private val flavour: DataFlavor,
    private val service: DrawingAppService = EditModule.drawingAppService
) : TransferHandler() {

	companion object {
        private val LOG by logger(ComponentTransferHandler::class)
	}

	/** Preserves the [Transferable] until the final drop action.*/
	private var transferable: Transferable? = null

    protected open fun extractTransferData (transferData: Any?): Any? = transferData

    init {
        (editor.view.canvas as JComponent).dropTarget = object : DropTarget() {
            override fun dragEnter(dtde: DropTargetDragEvent) {
                super.dragEnter(dtde)
                try {
                    val transferData = extractTransferData(dtde.transferable.getTransferData(flavour))
                    if (transferData is Component) {
	                    transferable = dtde.transferable
                        setComponent(transferData, Point2D(dtde.location.x, dtde.location.y))
                    }
                } catch (e: Exception) {
                    LOG.error("Error in dragEnter: ${e.message}")
                }
            }

            override fun dragExit(dte: DropTargetEvent?) {
                super.dragExit(dte)
	            editor.dragManager.dropComponent?.let {
	            	editor.dragManager.setDropComponent(null, null)
	            }
            }

            override fun dragOver(dtde: DropTargetDragEvent) {
                super.dragOver(dtde)
                val transferData = extractTransferData(dtde.transferable.getTransferData(flavour))
                if (transferData is Component) {
	                setComponent(transferData, Point2D(dtde.location.x, dtde.location.y))
                }
            }

            override fun drop(dtde: DropTargetDropEvent) {
                super.drop(dtde)
	            val dropComponent = editor.dragManager.dropComponent
                val localTransferable = transferable
                InvocationHandler.invoke {
                    if (dropComponent != null && canImport(dropComponent, localTransferable!!)) {
                        SwingUtilities.invokeLater { importElement(dropComponent, localTransferable, eventBus) }
                    }
	                editor.dragManager.setDropComponent(null, null)
                    transferable = null
                }
            }

            private fun setComponent(component: Component, location: Point2D) {
            	editor.dragManager.setDropComponent(component, editor.view.viewToModel(location))
            }
        }
    }

    override fun canImport(support: TransferSupport): Boolean {
        return support.isDataFlavorSupported(flavour)
    }

    /**
     * Checks whether the specified [Component] is allowed to be imported into the current [Editor]'s
     * [Drawing].
     * Subclasses might overwrite this method with runtime-intensive logic, hence this method should be called
     * within a [InvocationHandler].
     */
    protected open fun canImport(dropComponent: Component, transferable: Transferable): Boolean {
        return true
    }

    private fun importElement(dropComponent: Component, transferable: Transferable, eventBus: EventBus) {
        LOG.trace("importData")
        try {
	        val addedComponent = addComponent(dropComponent, transferable)
            eventBus.post(DropEvent(editor, addedComponent))
            editor.drawing.validate()
            (editor.view.canvas as JPanel).requestFocusInWindow()
        } catch (e: Exception) {
            LOG.error("Error in importing dropped Component: ${e.message}")
	        throw e
        }
    }

	protected open fun addComponent(dropComponent: Component, transferable: Transferable): Component {
		val component = service.add(dropComponent, editor.view)
		editor.dragManager.finishDrop(component)
		return component
	}
}