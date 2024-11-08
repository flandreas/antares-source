package ch.scorpion.jabbah.graph.view.app.oscilloscope

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand
import ch.scorpion.jabbah.graph.app.AbstractGraphViewCommand
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView

internal class AddOscilloscopeRowCommand(
	drawingView: DrawingView<*>,
	private val oscilloscopeViewId: Int
) : AbstractDrawingViewCommand("graph.command.addOscilloscopeRow", drawingView), Undoable {

	private val oscilloscopeView get() = view.drawing.getWithId(oscilloscopeViewId) as OscilloscopeView

	override fun execute() {
		oscilloscopeView.addRow()
	}

	override fun undo() {
		oscilloscopeView.removeLastRow()
	}
}

internal class RemoveOscilloscopeRowCommand(
	drawingView: DrawingView<*>,
	private val name: String,
	private val oscilloscopeViewId: Int
) : AbstractDrawingViewCommand("graph.command.removeOscilloscopeRow", drawingView), Undoable {

	private val oscilloscopeView get() = view.drawing.getWithId(oscilloscopeViewId) as OscilloscopeView

	override fun execute() {
		oscilloscopeView.removeRow(name)
	}

	override fun undo() {
		// TODO Should add the new row at the old index!
		oscilloscopeView.addRow()
	}
}

internal class OscilloscopeVisibilityCommand(
	private val drawingView: DrawingView<GraphView>,
	private val visible: Boolean,
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService
) : AbstractCommand("graph.command.oscilloscopeVisibility"), Undoable {

	override fun execute() {
		service.setOscilloscopeViewVisibility(drawingView, visible)
	}

	override fun undo() {
		service.setOscilloscopeViewVisibility(drawingView, !visible)
	}
}

internal class DropOscilloscopeProbeCommand<T : Any>(
	drawingView: DrawingView<GraphView>,
	private var name: String,
	private val location: Point2D,
	private val probeVerticeViewId: Int?,
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService
) : AbstractGraphViewCommand("graph.command.dropOscilloscopeProbe", drawingView) {

	override fun execute() {
		service.dropProbe<T>(drawingView, name, location, probeVerticeViewId)
	}
}