package io.antarescircuit.jabbah.draw.container

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.View
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.*

class DrawableContainerImplNestingTest {

	private lateinit var container: DrawableContainerImpl<Drawable>
	private lateinit var context: DrawContext

	@BeforeTest
	fun setup() {
		BaseModule.require()
		container = DrawableContainerImpl()
		context = DrawContext(mock(MockMode.autofill))
	}

	@Test
	fun shouldContainNestedContainerAtDrawable() {
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(TestRectangle(Rectangle2D(20, 20, 10, 10)))
		container.add(innerContainer)

		assertTrue(container.contains(125.0, 125.0))
	}

	@Test
	fun shouldGetDrawableInLocatedContainer() {
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
		innerContainer.add(rect)

		assertEquals(rect, innerContainer.getDrawableAt(125.0, 125.0))
	}

	@Test
	fun shouldNotContainContainerBackground() {
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(TestRectangle(Rectangle2D(20, 20, 10, 10)))
		container.add(innerContainer)

		assertFalse(container.contains(110.0, 110.0))
	}

	@Test
	fun shouldNotGetNestedDrawable() {
		val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(rect)
		container.add(innerContainer)

		assertEquals(innerContainer, container.getDrawableAt(125.0, 125.0) as DrawableContainerImpl<*>)
	}

	@Test
	fun shouldGetDrawableOfNestedContainer() {
		val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(rect)
		container.add(innerContainer)

		assertEquals(rect, innerContainer.getDrawableAt(125.0, 125.0) as TestRectangle)
	}

	@Test
	fun shouldYieldInnerTooltip() {
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(TestRectangle(Rectangle2D(20, 20, 10, 10)))

		container.add(innerContainer)

		assertEquals("Test", container.getTooltip(InputEventContext(mock(), x = 125.0, y = 125.0))?.text)
	}

	@Test
	fun shouldDispatchMouseMovedToNestedDrawable() {
		val view = mock<View<InputEventContext>>()
		val context = InputEventContext(view = view, x = 125.0, y = 125.0)

		val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(rect)
		container.add(innerContainer)

		container.getInputEventHandler(context).mouseMoved(context)

		assertTrue(rect.mouseMoved)
	}

	@Test
	fun shouldDispatchMousePressedToNestedDrawable() {
		val view = mock<View<InputEventContext>>()
		val context = InputEventContext(view = view, x = 125.0, y = 125.0)

		val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(rect)
		container.add(innerContainer)

		container.getInputEventHandler(context).mousePressed(context)

		assertTrue(rect.mousePressed)
	}

	@Test
	fun shouldClip() {
		val rect1 = spy<Drawable>(TestRectangle(Rectangle2D(0, 0, 10, 10)))
		val rect2 = spy<Drawable>(TestRectangle(Rectangle2D(90, 90, 10, 10)))
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(rect1)
		innerContainer.add(rect2)
		container.add(innerContainer)
		context.modelClip = Rectangle2D(100, 100, 50, 50)

		container.draw(context)

		verify(exactly(1)) { rect1.draw(context) }
		verify(exactly(0)) { rect2.draw(context) }
	}

	@Test
	fun shouldClipRecursively() {
		val deepContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		val rect = spy<Drawable>(TestRectangle(Rectangle2D(10, 10, 10, 10)))
		deepContainer.add(rect)
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(deepContainer)
		container.add(innerContainer)

		context.modelClip = Rectangle2D(100, 100, 20, 20)
		container.draw(context)
		verify(exactly(0)) { rect.draw(context) }
	}

	@Test
	fun shouldDrawRecursively() {
		val deepContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		val rect = spy<Drawable>(TestRectangle(Rectangle2D(10, 10, 10, 10)))
		deepContainer.add(rect)
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(deepContainer)
		container.add(innerContainer)

		context.modelClip = Rectangle2D(200, 200, 20, 20)
		container.draw(context)
		verify(exactly(1)) { rect.draw(context) }
	}
}