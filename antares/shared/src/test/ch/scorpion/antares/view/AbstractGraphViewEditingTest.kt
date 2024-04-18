package ch.scorpion.antares.view

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.every
import io.mockk.mockk

/**
 * Provides an [EditorToolDriver] in a [DrawingView] to support
 * tests of editing a [GraphView].
 */
abstract class AbstractGraphViewEditingTest(
	snapshotSize: Int = 2
) {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	protected val builder: GraphViewBuilder<Boolean> = GraphViewBuilder {
			builder -> view.setDrawing(builder.graphView as Drawing<Component>)
	}
	protected val view = EditModule.drawingViewFactory.create(builder.graphView as Drawing<Component>, null, false)
	protected val editor: Editor = EditEditorModule.createEditor(view as DrawingView<Drawing<Component>>)
	protected val driver = EditorToolDriver(editor)
	protected val service = GraphViewModule.graphViewAppService

	init {
		BaseModule.properties.set(SourcingCommandManager.PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT, snapshotSize)

		val canvas = mockk<Canvas>(relaxed = true)
		every { canvas.dimension } returns Dimension2D(1000, 1000)
		every { canvas.devicePixelRatio } returns 1
		view.canvas = canvas

		setupCircuit()

		GraphViewImpl.inputEventHandler = null
		editor.commandManager.bindDataHolder(builder)

	}

	protected abstract fun setupCircuit()
}