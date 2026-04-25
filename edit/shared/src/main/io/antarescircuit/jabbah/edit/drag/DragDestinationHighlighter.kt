package io.antarescircuit.jabbah.edit.drag

import io.antarescircuit.jabbah.edit.*

object DragDestinationHighlighter : DragManagerDestinationPlugin {

	private var highlightedDestination: Component? = null
	private var highlight: DragDestinationHighlight? = null

	override fun handleDragged(editor: Editor, component: Component, destination: Component?) {
		if (destination != null && destination is DragDestination && destination.acceptDrag(component)) {
			if (highlight == null) {
				addHighlightIfAny(editor.view, destination)
			} else {
				if (highlightedDestination != destination) {
					removeHighlight(editor.view)
					addHighlightIfAny(editor.view, destination)
				}
			}
			highlight?.handleDragged(component, destination)
		} else {
			removeHighlight(editor.view)
		}
	}

	override fun handleDragged(editor: Editor, component: Component) {
		handleDragged(editor, component, null)
	}

	override fun handleDragFinished(editor: Editor, component: Component): Collection<Command> = emptySet()

	override fun handleDragTerminated(editor: Editor) {
		removeHighlight(editor.view)
	}

	private fun addHighlightIfAny(drawingView: DrawingView<*,*>, destination: DragDestination) {
		createHighlight(destination)?.let {
			highlight = it
			highlightedDestination = destination
			drawingView.animationContainer.add(it)
			it.validate()
		}
	}

	private fun removeHighlight(drawingView: DrawingView<*,*>) {
		if (highlight != null) {
			drawingView.animationContainer.remove(highlight!!)
			drawingView.drawing.validate()
			highlight = null
		}
	}

	private fun createHighlight(destination: DragDestination): DragDestinationHighlight? =
		EditDragModule.dragDestinationHighlightFactoryRegistry.create(destination)
}