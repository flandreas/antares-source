package ch.scorpion.jabbah.graph.view.app.oscilloscope

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView

internal class AddOscilloscopeRowCommand(
	private val drawingView: DrawingView<*>,
	private val oscilloscopeViewId: Int
) : AbstractCommand("graph.command.addOscilloscopeRow"), Undoable {

	private val oscilloscopeView get() = drawingView.drawing.getWithId(oscilloscopeViewId) as OscilloscopeView

	override fun execute() {
		oscilloscopeView.addRow()
	}

	override fun undo() {
		oscilloscopeView.removeLastRow()
	}
}

internal class RemoveOscilloscopeRowCommand(
	private val drawingView: DrawingView<*>,
	private val name: String,
	private val oscilloscopeViewId: Int
) : AbstractCommand("graph.command.removeOscilloscopeRow"), Undoable {

	private val oscilloscopeView get() = drawingView.drawing.getWithId(oscilloscopeViewId) as OscilloscopeView

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