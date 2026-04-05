package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.InputEventHandlerMockBuilder
import io.antarescircuit.jabbah.draw.view.CanvasJvm
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.editor.EditorImpl
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.ToolTestUtil
import dev.mokkery.matcher.any
import org.junit.Test
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly

class SelectionToolImplEventDelegationTest {

	private val canvas: CanvasJvm
	private val editor: EditorImpl
	private val toolUtil: ToolTestUtil
	private val handler = InputEventHandlerMockBuilder()
	private val component = ComponentMockBuilder()
		.withBoundingBox(Rectangle2D(100, 100, 100, 100))
		.withInteractionHandler(handler.build())

	init {
		EditTestRule.configure()
		canvas = CanvasJvm(EditModule.drawingViewFactory.create(DrawingImpl(), null, false, ""))
		editor = EditorImpl(canvas.view as DrawingView<Drawing<Component>>)
		toolUtil = ToolTestUtil(SelectionToolImpl(editor, RubberBandHandler(RectangularRubberBand()), BaseModule.eventBus), editor)

		editor.drawing.add(component.build())
		toolUtil.tool.activate()
	}

	@Test
	fun shouldKeepHoveringWithMouseMove() {
		handler.withMouseMoved(true)
		toolUtil.moveMouseTo(150, 150)

		handler.withMouseMoved(true)
		toolUtil.moveMouseTo(160, 150)

		verify(exactly(2)) { handler.build().mouseMoved(any()) }
	}

	@Test
	fun shouldStopHovering() {
		handler.withMouseMoved(true)
		toolUtil.moveMouseTo(150, 150)

		handler.withMouseMoved(false)
		toolUtil.moveMouseTo(300, 150)
		toolUtil.moveMouseTo(400, 150)

		verify(exactly(2)) { handler.build().mouseMoved(any()) }
	}

}