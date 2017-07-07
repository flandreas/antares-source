package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.editor.DropEvent
import ch.scorpion.jabbah.edit.select.DragEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import java.awt.datatransfer.DataFlavor
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
    private val flavour: DataFlavor
) : TransferHandler() {
    private val LOG by logger()

    init {
        (editor.view.canvas as JComponent).dropTarget = object : DropTarget() {
            override fun dragEnter(dtde: DropTargetDragEvent?) {
                super.dragEnter(dtde)
                try {
                    val transferData = dtde?.transferable?.getTransferData(flavour)
                    if (transferData is Component) {
                        setComponent(transferData, Point2D(dtde.location.x, dtde.location.y))
                    }
                } catch (e: Exception) {
                    LOG.error("Error in dragEnter: ${e.message}")
                }
            }

            override fun dragExit(dte: DropTargetEvent?) {
                super.dragExit(dte)
                editor.view.setDropComponent(null, null)
            }

            override fun dragOver(dtde: DropTargetDragEvent?) {
                super.dragOver(dtde)
                try {
                    val transferData = dtde?.transferable?.getTransferData(flavour)
                    if (transferData is Component) {
                        setComponent(transferData, Point2D(dtde.location.x, dtde.location.y))
                        eventBus.post(DragEvent(editor, listOf(transferData)))
                    }
                } catch (e: Exception) {
                    LOG.error("Error in dragOver: ${e.message}")
                }

            }

            override fun drop(dtde: DropTargetDropEvent?) {
                super.drop(dtde)
                try {
                    val dropComponent = editor.view.dropComponent
                    InvocationHandler.invoke(Runnable {
                        if (dropComponent != null && canImport(dropComponent)) {
                            SwingUtilities.invokeLater { importElement(dropComponent, eventBus) }
                        }
                        editor.view.setDropComponent(null, null)
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            private fun setComponent(component: Component, location: Point2D) {
                val x = editor.view.viewToModelX(location.x)
                val y = editor.view.viewToModelY(location.y)

                val snap = editor.snapManager.snap(x, y)

                editor.view.setDropComponent(component, Point2D(x + snap.x, y + snap.y))
            }
        }
    }

    override fun canImport(support: TransferSupport): Boolean {
        return support.isDataFlavorSupported(flavour)
    }

    /**
     * Checks whether the specified [Component] is allowed to be imported into the current [Editor]'s
     * [Drawing].
     * Sublclasses might overwrite this method with runtime-intensive logic, hence this method should be called
     * within a [InvocationHandler].
     */
    protected open fun canImport(dropComponent: Component): Boolean {
        return true
    }

    private fun importElement(elementView: Component, eventBus: EventBus) {
        LOG.debug("importData")
        try {
            val command = AddCommand(editor, elementView)
            editor.commandManager.beginTransaction(command)
            editor.commandManager.commitTransaction()
            eventBus.post(DropEvent(editor, elementView))
            editor.view.selectionManager.deselectAll()
            editor.view.selectionManager.select(elementView)
            editor.drawing.validate()
            (editor.view.canvas as JPanel).requestFocusInWindow()
        } catch (e: Exception) {
            LOG.error("Error in importing dropped Component: ${e.message}")
        }
    }
}