package ch.scorpion.jabbah.graph.app

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand
import ch.scorpion.jabbah.graph.view.GraphView

abstract class AbstractGraphViewCommand(
    descriptionKey: String,
    view: DrawingView<GraphView>
) : AbstractDrawingViewCommand(descriptionKey, view as DrawingView<*>) {

    val drawingView: DrawingView<GraphView> get() = view as DrawingView<GraphView>
}