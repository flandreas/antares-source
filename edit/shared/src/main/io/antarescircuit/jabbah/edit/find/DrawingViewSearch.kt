package io.antarescircuit.jabbah.edit.find

import io.antarescircuit.jabbah.draw.view.find.SearchMatch
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.text.Labeled

open class DrawingViewSearch {

	fun execute(view: DrawingView<*, *>, request: SearchRequest) {
		view.selectionManager.deselectAll()

		if (request.searchString.isNotBlank()) {
			view.selectionManager.select(find(view.drawing, request))
		}
	}

	private fun find(drawing: Drawing<*>, request: SearchRequest): Set<Component> =
		mutableSetOf<Component>().also {
			findImpl(drawing, request, it)
		}

	protected open fun findImpl(drawing: Drawing<*>, request: SearchRequest, result: MutableSet<Component>) {
		findMatchingId(drawing, request, result)
		findMatchingType(drawing, request, result)
		findLabeled(drawing, request, result)
	}

	protected fun compare(text: String?, request: SearchRequest): Boolean =
		when (request.match) {
			SearchMatch.StartsWidth -> text?.startsWith(request.searchString, request.ignoreCase) == true
			SearchMatch.Contains -> text?.contains(request.searchString, request.ignoreCase) == true
			SearchMatch.EntireWord -> text?.equals(request.searchString, request.ignoreCase) == true
	}

	private fun findMatchingId(drawing: Drawing<*>, request: SearchRequest, result: MutableSet<Component>) {
		request.searchString.toIntOrNull()?.let { id ->
			drawing.getWithId(id)?.let {
				result.add(it)
			}
		}
	}

	private fun findMatchingType(drawing: Drawing<*>, request: SearchRequest, result: MutableSet<Component>) {
		result.addAll(drawing.getDrawables { compare(it.type, request)  })
	}

	private fun findLabeled(drawing: Drawing<*>, request: SearchRequest, result: MutableSet<Component>) {
		result.addAll(drawing.getDrawables { it is Labeled && compare(it.label.text, request) })
	}
}