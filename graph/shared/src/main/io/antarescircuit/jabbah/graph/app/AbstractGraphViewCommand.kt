package io.antarescircuit.jabbah.graph.app

import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.command.AbstractDrawingViewCommand
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

abstract class AbstractGraphViewCommand(
    descriptionKey: String,
    view: DrawingView<GraphElementView<*>, GraphView>
) : AbstractDrawingViewCommand(descriptionKey, view as DrawingView<*,*>) {

    @Suppress("UNCHECKED_CAST") // Ensured by constructor
    val drawingView: DrawingView<GraphElementView<*>, GraphView> get() = view as DrawingView<GraphElementView<*>, GraphView>
}