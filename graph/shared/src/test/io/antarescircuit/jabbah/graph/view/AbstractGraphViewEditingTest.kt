package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.editor.EditEditorModule
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.EditorToolDriver
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppService
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

/**
 * Provides an [EditorToolDriver] in a [DrawingView] to support
 * tests of editing a [GraphView].
 */
abstract class AbstractGraphViewEditingTest(
	snapshotSize: Int = 2
) {

	protected val builder: GraphViewBuilder<Boolean>
	protected val view: DrawingView<GraphElementView<*>, GraphView>
	protected val editor: Editor
	protected val driver: EditorToolDriver
	protected val service : GraphViewAppService
	protected val canvasBuilder: CanvasMockBuilder

	init {
		GraphViewTestRule.configure()
		builder = GraphViewBuilder { builder -> view.setDrawing(builder.graphView) }
		view = EditModule.drawingViewFactory.create(builder.graphView, null, false, "")
		@Suppress("UNCHECKED_CAST")
		editor = EditEditorModule.createEditor("", view as DrawingView<Component, Drawing<Component>>)
		driver = EditorToolDriver(editor)
		service = GraphViewModule.graphViewAppService

		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, snapshotSize)

		canvasBuilder = CanvasMockBuilder()
			.withDimension(Dimension2D(1000, 1000))
			.withDevicePixelRatio(1.0)
			.withView(view)

		view.canvas = canvasBuilder.build()

		setupCircuit()

		GraphViewImpl.inputEventHandler = null
		editor.commandManager.bindDataHolder(builder)

	}

	protected abstract fun setupCircuit()
}