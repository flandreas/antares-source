package ch.scorpion.jabbah.edit.find

import ch.scorpion.jabbah.draw.view.find.SearchRequest
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView

open class DrawingViewSearch {

	fun execute(view: DrawingView<Drawing<Component>>, request: SearchRequest) {
		view.selectionManager.deselectAll()

		if (request.searchString.isNotBlank()) {
			view.selectionManager.select(find(view.drawing, request))
		}
	}

	private fun find(drawing: Drawing<Component>, request: SearchRequest): Set<Component> =
		mutableSetOf<Component>().also {
			findImpl(drawing, request, it)
		}

	protected open fun findImpl(drawing: Drawing<Component>, request: SearchRequest, result: MutableSet<Component>) {
		findMatchingId(drawing, request, result)
		findMatchingType(drawing, request, result)
	}

	private fun findMatchingId(drawing: Drawing<Component>, request: SearchRequest, result: MutableSet<Component>) {
		request.searchString.toIntOrNull()?.let { id ->
			drawing.getWithId(id)?.let {
				result.add(it)
			}
		}
	}

	private fun findMatchingType(drawing: Drawing<Component>, request: SearchRequest, result: MutableSet<Component>) {
		result.addAll(
			drawing.getDrawables { it.type.contains(request.searchString, ignoreCase = true) }
		)
	}
}