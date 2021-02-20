package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DragTargetHandler
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.editor.DropEvent
import ch.scorpion.jabbah.edit.module.EditModule
import org.w3c.dom.DragEvent

/** Handles drop gestures with [Component]s on [DrawingView]s in [CanvasJs].*/
open class ComponentDragTargetHandler(
	private val editor: Editor,
	private val service: DrawingAppService = EditModule.drawingAppService,
	private val eventBus: EventBus = BaseModule.eventBus
) : DragTargetHandler() {

	companion object {
		private val LOG by logger(ComponentDragTargetHandler::class)
	}

	/**
	 * The method [onDragOver] gets called hundreds of times each second even if the
	 * mouse is not moved. In order to limit the repainting load in the target [View],
	 * keep the snapped [Component] location and update the [Component]'s location
	 * only it has changed.
	 */
	private var snappedLocation = Point2D()

	override fun onDragEnter(event: DragEvent, viewLocation: Point2D) {
		val transferData = extractTransferData()
		if (transferData is Component) {
			event.preventDefault()
			setComponent(transferData, viewLocation)
		}
	}

	override fun onDragOver(event: DragEvent, viewLocation: Point2D) {
		val transferData = extractTransferData()
		if (transferData is Component) {
			event.preventDefault()
			setComponent(transferData, viewLocation)
			transferData.dragged(editor)
		}
	}

	override fun onDrop(event: DragEvent, viewLocation: Point2D) {
		super.onDrop(event, viewLocation)

		val dropComponent = editor.view.dropComponent
		if (dropComponent != null) {
			importElement(dropComponent)
		}
		editor.view.setDropComponent(null, null)
	}

	protected open fun extractTransferData(): Any? = DragAndDropDepo.data

	protected open fun addComponent(dropComponent: Component): Component {
		return service.add(dropComponent, editor.view)
	}

	private fun setComponent(component: Component, viewLocation: Point2D) {
		val modelLoc = editor.view.viewToModel(viewLocation)

		val newSnappedLocation = modelLoc.add(editor.snapManager.snap(modelLoc.x, modelLoc.y))

		if (newSnappedLocation != snappedLocation) {
			snappedLocation = newSnappedLocation
			editor.view.setDropComponent(component, newSnappedLocation)
		}
	}

	private fun importElement(dropComponent: Component) {
		try {
			val addedComponent = addComponent(dropComponent)
			eventBus.post(DropEvent(editor, addedComponent))
			editor.drawing.validate()
			// TODO Request focus in window
		} catch (e: Exception) {
			LOG.error("Error while importing dropped Component: ${e.message}")
		}
	}
}