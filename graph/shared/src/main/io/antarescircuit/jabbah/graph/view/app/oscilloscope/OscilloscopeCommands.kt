package io.antarescircuit.jabbah.graph.view.app.oscilloscope

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.command.AbstractDrawingViewCommand
import io.antarescircuit.jabbah.graph.app.AbstractGraphViewCommand
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView

internal class AddOscilloscopeRowCommand(
	drawingView: DrawingView<*,*>,
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
	drawingView: DrawingView<*,*>,
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
	private val drawingView: DrawingView<GraphElementView<*>, GraphView>,
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

internal class DropOscilloscopeProbeCommand(
	drawingView: DrawingView<GraphElementView<*>, GraphView>,
	private var name: String,
	private val location: Point2D,
	private val probeVerticeViewId: Int?,
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService
) : AbstractGraphViewCommand("graph.command.dropOscilloscopeProbe", drawingView) {

	/**
	 * The ID of the [EdgeView] to which the [OscilloscopeProbeVerticeView] was connected, if any.
	 */
	var connectedEdgeViewId: Int? = null

	override fun execute() {
		connectedEdgeViewId = service.dropProbe(drawingView, name, location, probeVerticeViewId)
	}
}