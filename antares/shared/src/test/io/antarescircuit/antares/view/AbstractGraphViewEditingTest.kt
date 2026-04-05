package io.antarescircuit.antares.view

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.editor.EditEditorModule
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
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
	protected val view: DrawingView<Drawing<Component>>
	protected val editor: Editor
	protected val driver: EditorToolDriver
	protected val service: GraphViewAppService
	protected val canvasBuilder: CanvasMockBuilder

	init {
		AntaresTestRule.configure()

		builder = GraphViewBuilder {
				builder -> view.setDrawing(builder.graphView as Drawing<Component>)
		}
		view = EditModule.drawingViewFactory.create(builder.graphView as Drawing<Component>, null, false, "")
		editor = EditEditorModule.createEditor("", view)
		driver = EditorToolDriver(editor)
		service = GraphViewModule.graphViewAppService

		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, snapshotSize)

		canvasBuilder = CanvasMockBuilder()
		view.canvas = canvasBuilder.withView(view).build()

		setupCircuit()

		GraphViewImpl.inputEventHandler = null
		editor.commandManager.bindDataHolder(builder)
	}

	protected abstract fun setupCircuit()
}