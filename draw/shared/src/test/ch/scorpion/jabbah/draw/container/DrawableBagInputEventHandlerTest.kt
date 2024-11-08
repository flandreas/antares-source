package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventDriver
import ch.scorpion.jabbah.draw.InputEventHandlerMockBuilder
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import ch.scorpion.jabbah.draw.module.DrawModule
import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Testing [DrawableBagInputEventHandler] that is created by [DrawableContainerImpl] by default.
 */
class DrawableBagInputEventHandlerTest {

	companion object {
		init {
			DrawModule.require()
		}
	}

	private val container = DrawableContainerImpl<Drawable>(useLocation = true)
	private val driver = InputEventDriver(view = mock(MockMode.autofill), container)
	private val drawableHandler = InputEventHandlerMockBuilder()
	private val drawable = DrawableMockBuilder()
		.withBoundingBox(Rectangle2D(100, 100, 100, 100))
		.withInteractionHandler(drawableHandler.build())

	init {
		container.add(drawable.build())
	}

	@Test
	fun shouldKeepHoveringWithMouseMove() {
		drawableHandler.withMouseMoved(true)

		driver.moveMouseTo(150, 150)

		verify(exactly(1)) { drawableHandler.build().mouseMoved(any()) }
		assertEquals(Point2D(150, 150), drawableHandler.eventLocation)

		driver.moveMouseTo(160, 160)

		verify(exactly(1)) { drawableHandler.build().mouseMoved(any()) }
		assertEquals(Point2D(160, 160), drawableHandler.eventLocation)
	}

	@Test
	fun shouldKeepHoveringInLocatedContainer() {
		container.location = Point2D(1000, 1000)
		drawableHandler.withMouseMoved(true)

		driver.moveMouseTo(1150, 1150)

		verify(exactly(1)) { drawableHandler.build().mouseMoved(any()) }
		assertEquals(Point2D(150, 150), drawableHandler.eventLocation)

		driver.moveMouseTo(1160, 1160)

		verify(exactly (1)) { drawableHandler.build().mouseMoved(any()) }
		assertEquals(Point2D(160, 160), drawableHandler.eventLocation)
	}
}