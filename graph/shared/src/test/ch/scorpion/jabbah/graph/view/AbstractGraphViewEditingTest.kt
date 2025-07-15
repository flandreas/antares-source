package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.EditorToolDriver
import ch.scorpion.jabbah.graph.CanvasMockBuilder
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Provides an [EditorToolDriver] in a [DrawingView] to support
 * tests of editing a [GraphView].
 */
abstract class AbstractGraphViewEditingTest(
	snapshotSize: Int = 2
) {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	protected val builder: GraphViewBuilder<Boolean> = GraphViewBuilder {
		builder -> view.setDrawing(builder.graphView)
	}
	protected val view = EditModule.drawingViewFactory.create(builder.graphView as Drawing<Component>, null, false, "")
		as DrawingView<GraphView>
	protected val editor: Editor = EditEditorModule.createEditor("", view as DrawingView<Drawing<Component>>)
	protected val driver = EditorToolDriver(editor)
	protected val service = GraphViewModule.graphViewAppService

	init {
		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, snapshotSize)

		view.canvas = CanvasMockBuilder()
			.withDimension(Dimension2D(1000, 1000))
			.withDevicePixelRatio(1.0)
			.withView(view)
			.build()

		setupCircuit()

		GraphViewImpl.inputEventHandler = null
		editor.commandManager.bindDataHolder(builder)

	}

	protected abstract fun setupCircuit()
}