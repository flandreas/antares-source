package ch.scorpion.jabbah.graph.uifx

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ToolBarBuilderFx
import ch.scorpion.jabbah.base.fx.ResizableCanvasFx
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.CanvasFx
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphView
import javafx.scene.Node
import javafx.scene.control.ToolBar

class GraphDesktopFx(
	private val viewManager: ViewManager = DrawViewModule.viewManager
) {

	companion object {
		private val LOG by logger(GraphDesktopFx::class)
	}

	private var graphPaneFx: GraphPaneFx
	val node: Node get() = graphPaneFx.node

	val toolbars: List<ToolBar> = mutableListOf()

	private var editor: Editor

	init {
		val canvas = ResizableCanvasFx()
		val canvasFx = CanvasFx(canvas, {
			EditModule.drawingViewFactory.invoke(DrawingImpl<Component>(), it)
		})
		canvas.repaintCallback = {
			LOG.debug("AntaresFX: repaintCallback")
			canvasFx.repaint()
		}
		editor = EditEditorModule.createEditor(canvasFx.view as DrawingView<Drawing<Component>>)

		BaseModule.eventBus.register(ApplicationDataEvent::class, {
			(editor.view as DrawingView<GraphView<GraphElementView<*>>>).drawing = (it.newData as MetaGraph).graph.graphView as GraphView<GraphElementView<*>>
		})

		// TODO How to establish ComponentViewDrawer?
		graphPaneFx = GraphPaneFx(editor)
		(toolbars as MutableList<ToolBar>).add(ToolBarBuilderFx(editor, { GraphElementViewWrapper<Vertice>(it)} ).build())
	}

	fun activated() {
		viewManager.activeView = editor.view
	}

	fun dispose() {
		graphPaneFx.dispose()
	}
}