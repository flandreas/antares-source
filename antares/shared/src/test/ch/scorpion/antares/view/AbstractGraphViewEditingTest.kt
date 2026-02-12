package ch.scorpion.antares.view

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CanvasMockBuilder
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

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
		editor = EditEditorModule.createEditor("", view as DrawingView<Drawing<Component>>)
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