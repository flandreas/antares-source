package ch.scorpion.jabbah.graph.view.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
import ch.scorpion.jabbah.io.module.IOModuleJs
import ch.scorpion.jabbah.module.DrawModuleJs

object GraphViewModuleJs : AbstractModule() {

	override fun initialize() {
		IOModuleJs.require()
		DrawModuleJs.require()
		GraphViewModule.graphEditorFactory = { canvasId,eventBus -> createGraphEditor(canvasId,eventBus) }

		GraphViewModule.require()
	}

	private fun createGraphEditor(canvasId: String, eventBus: EventBus): GraphEditor {
		val graphView = GraphViewModule.graphViewFactory.invoke(null) as Drawing<Component>
		val graphCanvas = CanvasJs(canvasId, { EditModule.drawingViewFactory.invoke(graphView, it) }, StyleRepository.INSTANCE)
		return GraphEditor(graphCanvas.view as DrawingView<Drawing<Component>>, eventBus)
	}
}