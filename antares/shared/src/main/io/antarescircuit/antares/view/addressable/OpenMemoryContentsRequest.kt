package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.Memory
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView

/**
 * Represents a request to open and display the contents of a [Memory].
 */
data class OpenMemoryContentsRequest(
	val drawingView: DrawingView<GraphElementView<*>, GraphView>,
	val verticeView: VerticeView<*>,
	val name: String,
	val link: ObjectLink<Addressable>,
	val newDesktopView: Boolean = false
)