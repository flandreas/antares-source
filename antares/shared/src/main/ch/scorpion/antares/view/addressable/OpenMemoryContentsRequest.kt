package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.Memory
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Represents a request to open and display the contents of a [Memory].
 */
data class OpenMemoryContentsRequest(
	val drawingView: DrawingView<GraphView>,
	val verticeView: VerticeView<*>,
	val name: String,
	val addressable: Addressable,
	val readonly: Boolean,
	val newDesktopView: Boolean = false
)