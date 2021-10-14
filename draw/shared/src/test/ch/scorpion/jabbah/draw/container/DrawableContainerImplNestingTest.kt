package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import io.mockk.mockk
import kotlin.test.*

class DrawableContainerImplNestingTest {

	private lateinit var container: DrawableContainerImpl<Drawable>
	private lateinit var context: DrawContext

	@BeforeTest
	fun setup() {
		BaseModule.require()
		container = DrawableContainerImpl()
		context = mockk(relaxed = true)
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

		assertEquals("Test", container.getTooltip(125.0, 125.0)?.text?.asPlainText())
	}

	@Test
	fun shouldDispatchMouseMovedToNestedDrawable() {
		val view = mockk<View<InputEventContext>>()
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
		val view = mockk<View<InputEventContext>>()
		val context = InputEventContext(view = view, x = 125.0, y = 125.0)

		val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
		val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
		innerContainer.add(rect)
		container.add(innerContainer)

		container.getInputEventHandler(context).mousePressed(context)

		assertTrue(rect.mousePressed)
	}
}