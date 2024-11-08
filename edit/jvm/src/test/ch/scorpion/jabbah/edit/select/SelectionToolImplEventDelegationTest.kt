package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.tool.ToolTestUtil
import dev.mokkery.matcher.any
import org.junit.Test
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly

class SelectionToolImplEventDelegationTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val canvas = CanvasJvm(EditModule.drawingViewFactory.create(DrawingImpl(), null, false))
	private val editor = EditorImpl(canvas.view as DrawingView<Drawing<Component>>)
	private val toolUtil = ToolTestUtil(SelectionToolImpl(editor, RubberBandHandler(RectangularRubberBand()), BaseModule.eventBus), editor)
	private val handler = InputEventHandlerMockBuilder()
	private val component = ComponentMockBuilder()
		.withBoundingBox(Rectangle2D(100, 100, 100, 100))
		.withInteractionHandler(handler.build())

	init {
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