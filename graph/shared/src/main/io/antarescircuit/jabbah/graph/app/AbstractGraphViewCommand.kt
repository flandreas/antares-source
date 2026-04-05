package io.antarescircuit.jabbah.graph.app

import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.command.AbstractDrawingViewCommand
import io.antarescircuit.jabbah.graph.view.GraphView

abstract class AbstractGraphViewCommand(
    descriptionKey: String,
    view: DrawingView<GraphView>
) : AbstractDrawingViewCommand(descriptionKey, view as DrawingView<*>) {

    val drawingView: DrawingView<GraphView> get() = view as DrawingView<GraphView>
}