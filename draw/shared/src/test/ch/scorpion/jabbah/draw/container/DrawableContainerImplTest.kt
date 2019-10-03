package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.*

/**
 * Unit tests for [DrawableContainerImpl].
 */
class DrawableContainerImplTest {

    private lateinit var container: DrawableContainerImpl<Drawable>
    private lateinit var context: DrawContext

    @BeforeTest
    fun setup() {
        BaseModuleJvm.require()
        container = DrawableContainerImpl()
        context = mockk()
    }

    @Test
    fun shouldAddDrawable() {
        val drawable = DrawableMockBuilder().build()
        container.add(drawable)
        assertTrue(container.contains(drawable))
    }

    @Test
    fun shouldNotContainUnaddedDrawable() {
        val drawable = DrawableMockBuilder().build()
        assertFalse(container.contains(drawable))
    }

    @Test
    fun shouldDrawVisibleDrawables() {
        val drawable = DrawableMockBuilder().build()
        container.add(drawable)
        container.draw(context)
	    verify(atLeast = 1) { drawable.draw(context) }
    }

    @Test
    fun shouldNotDrawInvisibleDrawables() {
        val drawable = DrawableMockBuilder().invisible().build()
        container.add(drawable)
        container.draw(context)
	    verify(exactly = 0) { drawable.draw(context) }
    }

    @Test
    fun shouldIterateBackToFront() {
        val drawable1 = DrawableMockBuilder().build()
        val drawable2 = DrawableMockBuilder().build()
        val drawable3 = DrawableMockBuilder().build()
        container.add(drawable1).add(drawable2).add(drawable3)

        val iter = container.backToFrontIterator()

        assertTrue(iter.hasNext())
        assertSame(iter.next(), drawable1)
	    assertSame(iter.next(), drawable2)
	    assertSame(iter.next(), drawable3)
    }

    // Stacking order

    @Test
    fun shouldSetStackingOrderPositionLower() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(1, d3)

        assertEquals(0, container.getStackingOrderPosition(d1))
        assertEquals(1, container.getStackingOrderPosition(d3))
        assertEquals(2, container.getStackingOrderPosition(d2))
    }

    @Test
    fun shouldSetStackingOrderPositionHigher() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(2, d2)

        assertEquals(0, container.getStackingOrderPosition(d1))
        assertEquals(1, container.getStackingOrderPosition(d3))
        assertEquals(2, container.getStackingOrderPosition(d2))
    }

    @Test
    fun shouldSetStackingOrderPositionUnchanged() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(1, d2)

        assertEquals(0, container.getStackingOrderPosition(d1))
        assertEquals(1, container.getStackingOrderPosition(d2))
        assertEquals(2, container.getStackingOrderPosition(d3))
    }

    @Test
    fun shouldBringToFront() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.toFront(listOf(d3))

        assertEquals(0, container.getStackingOrderPosition(d3))
        assertEquals(1, container.getStackingOrderPosition(d1))
        assertEquals(2, container.getStackingOrderPosition(d2))
    }

    @Test
    fun shouldBringToBack() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.toBack(listOf(d1, d2))

        assertEquals(0, container.getStackingOrderPosition(d3))
        assertEquals(1, container.getStackingOrderPosition(d1))
        assertEquals(2, container.getStackingOrderPosition(d2))
    }

    // Direct containment tests

    @Test
    fun shouldDirectlyContainAt() {
        container.add(TestRectangle(Rectangle2D(100, 100, 10, 10)))
        assertTrue(container.contains(105.0, 105.0))
        assertFalse(container.contains(5.0, 5.0))
    }

    @Test
    fun shouldGetDrawableAt() {
        val rect = TestRectangle(Rectangle2D(100, 100, 10, 10))
        container.add(rect)
        assertEquals(rect, container.getDrawableAt(105.0, 105.0) as TestRectangle)
    }

    // Composite DrawableContainerImpl

    @Test
    fun shouldContainNestedContainerAtDrawable() {
        val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
        innerContainer.add(TestRectangle(Rectangle2D(20, 20, 10, 10)))
        container.add(innerContainer)

        assertTrue(container.contains(125.0, 125.0))
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

        assertEquals("Test", container.getTooltip(125.0, 125.0)?.text)
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

    private class TestRectangle(shape: RectangularShape) : AbstractRectangle(shape) {
        var mouseMoved = false
        var mousePressed = false
        private val handler = Handler()

        override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> = handler
        override fun draw(context: DrawContext) { }
        override val lineWidth: Double get() = 0.0
        override fun getTooltip(x: Double, y: Double): Tooltip = Tooltip("Test", x, y)

        private inner class Handler : InputEventHandlerAdapter<InputEventContext>() {
            override fun mouseMoved(context: InputEventContext): InputEventHandler<InputEventContext>? {
                if(this@TestRectangle.contains(context.x, context.y)) {
                    mouseMoved = true
                    return this
                }
                return null
            }

            override fun mousePressed(context: InputEventContext): InputEventHandler<InputEventContext>? {
                if(this@TestRectangle.contains(context.x, context.y)) {
                    mousePressed = true
                    return this
                }
                return null
            }
        }
    }
}