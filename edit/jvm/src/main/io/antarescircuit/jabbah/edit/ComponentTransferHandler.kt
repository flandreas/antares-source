package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.app.RotateAction
import io.antarescircuit.jabbah.edit.drag.DropEvent
import io.antarescircuit.jabbah.edit.module.EditModule
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.KeyStroke
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
		private val ROTATE_KEY_STROKE = KeyStroke.getKeyStroke(Translations.getString(io.antarescircuit.jabbah.base.System.getActionAcceleratorKey(RotateAction.ACTION_KEY)))
	}

	/** Preserves the [Transferable] until the final drop action.*/
	private var transferable: Transferable? = null

	private val rotationKeyEventDispatcher = RotationKeyEventDispatcher()

    protected open fun extractTransferData (transferData: Any?): Any? = transferData

    init {
        (editor.view.canvas as JComponent).dropTarget = object : DropTarget() {
            override fun dragEnter(dtde: DropTargetDragEvent) {
                super.dragEnter(dtde)
	            if (!dtde.isDataFlavorSupported(flavour)) {
	            	return
	            }
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
					resetComponent()
	            }
            }

            override fun dragOver(dtde: DropTargetDragEvent) {
                super.dragOver(dtde)
	            if (dtde.isDataFlavorSupported(flavour)) {
		            val transferData = extractTransferData(dtde.transferable.getTransferData(flavour))
		            if (transferData is Component) {
			            setComponent(transferData, Point2D(dtde.location.x, dtde.location.y))
		            }
	            }
            }

            override fun drop(dtde: DropTargetDropEvent) {
	            val dropComponent = editor.dragManager.dropComponent
                val localTransferable = transferable
                InvocationHandler.invoke {
                    if (dropComponent != null && canImport(dropComponent, localTransferable!!)) {
                        SwingUtilities.invokeLater { importElement(dropComponent, localTransferable, eventBus) }
                    }
	                resetComponent()
                    transferable = null
                }
	            dtde.dropComplete(true)
            }

            private fun setComponent(component: Component, location: Point2D) {
	            if (editor.dragManager.dropComponent !== component) {
	                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(rotationKeyEventDispatcher)
				}
				editor.dragManager.setDropComponent(component, editor.view.viewToModel(location))
			}

	        private fun resetComponent() {
		        editor.dragManager.setDropComponent(null, null)
		        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(rotationKeyEventDispatcher)
			}
        }
    }

	override fun canImport(support: TransferSupport): Boolean = support.isDataFlavorSupported(flavour)

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

	protected open fun addComponent(
		dropComponent: Component,
		transferable: Transferable
	): Component {
		val component = service.add(dropComponent, editor.view)
		editor.dragManager.finishDrop(component)
		return component
	}

	private inner class RotationKeyEventDispatcher : KeyEventDispatcher {
		override fun dispatchKeyEvent(e: KeyEvent?): Boolean {
			if (e != null) {
				val keyStroke = KeyStroke.getKeyStrokeForEvent(e)
				if ((e.id == KeyEvent.KEY_RELEASED || e.id == KeyEvent.KEY_PRESSED)
					&& !e.isConsumed
					&& keyStroke.keyCode == ROTATE_KEY_STROKE.keyCode
					&& keyStroke.modifiers == ROTATE_KEY_STROKE.modifiers
				) {
					LOG.userTrail("Rotate Component while dragging into Drawing")
					editor.dragManager.dropComponent?.rotate(RotationDirection.CounterClockwise)
					editor.view.repaint()
					e.consume()
					return true
				}
			}
			return false
		}
	}
}