package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.view.CanvasFx
import ch.scorpion.jabbah.edit.editor.AddCommand
import ch.scorpion.jabbah.edit.editor.DropEvent
import ch.scorpion.jabbah.edit.select.DragEvent
import ch.scorpion.jabbah.io.IOModule
import javafx.application.Platform
import javafx.scene.canvas.Canvas
import javafx.scene.input.TransferMode

/**
 * Handles drop gestures with [Component]s on a [Canvas].
 */
open class ComponentDropHandlerFx(
	protected val editor: Editor,
	private val eventBus: EventBus
) {

	companion object {
		private val LOG by logger(ComponentDropHandlerFx::class)
	}

	private var component: Component? = null

	init {
		(editor.view.canvas as CanvasFx).canvas.setOnDragEntered {
			LOG.debug("onDragEntered")
			val transferData = it.dragboard.getContent(ComponentDataFormat)
			if (transferData != null && transferData is String) {
				component = extractComponent(transferData)
				forwardComponent(Point2D(it.x, it.y))
			}
			it.consume()
		}

		(editor.view.canvas as CanvasFx).canvas.setOnDragExited {
			editor.view.setDropComponent(null, null)
			component = null
			it.consume()
		}

		(editor.view.canvas as CanvasFx).canvas.setOnDragOver {
			if (component != null) {
				it.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE)
				forwardComponent(Point2D(it.x, it.y))
				eventBus.post(DragEvent(editor, listOf(component!!)))
			}
			it.consume()
		}

		(editor.view.canvas as CanvasFx).canvas.setOnDragDropped {
			LOG.debug("onDragDropped")
			val dropComponent = editor.view.dropComponent
			if (dropComponent != null && canImport(dropComponent)) {
				it.isDropCompleted = true
				Platform.runLater { importElement(dropComponent, eventBus) }
			}
			it.consume()
		}
	}

	protected open fun extractComponent(transferData: String): Component {
		return IOModule.storableClonerProvider.invoke().deserialize(transferData) as Component
	}

	protected open fun canImport(dropComponent: Component): Boolean {
		return true
	}

	private fun forwardComponent(location: Point2D) {
		val x = editor.view.viewToModelX(location.x)
		val y = editor.view.viewToModelY(location.y)

		val snap = editor.snapManager.snap(x, y)

		editor.view.setDropComponent(component, Point2D(x + snap.x, y + snap.y))
	}

	private fun importElement(elementView: Component, eventBus: EventBus) {
		LOG.debug("importData")
		try {
			val command = AddCommand(editor.view, elementView)
			editor.commandManager.execute(command)
			eventBus.post(DropEvent(editor, elementView))
			editor.view.selectionManager.deselectAll()
			editor.view.selectionManager.select(elementView)
			editor.drawing.validate()
			(editor.view.canvas as CanvasFx).canvas.requestFocus()
		} catch (e: Exception) {
			LOG.error("Error in importing dropped Component: ${e.message}")
		}
	}
}