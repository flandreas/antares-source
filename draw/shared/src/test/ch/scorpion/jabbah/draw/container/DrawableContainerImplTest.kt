package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * Unit tests for [DrawableContainerImpl].
 */
class DrawableContainerImplTest {

    private lateinit var container: DrawableContainerImpl<Drawable>
    private lateinit var context: DrawContext

    @Before
    fun setup() {
        BaseModuleJvm.require()
        container = DrawableContainerImpl()
        context = Mockito.mock(DrawContext::class.java)
    }

    @Test
    fun shouldAddDrawable() {
        val drawable = DrawableMockBuilder().build()
        container.add(drawable)
        Assert.assertTrue(container.contains(drawable))
    }

    @Test
    fun shouldNotContainUnaddedDrawable() {
        val drawable = DrawableMockBuilder().build()
        Assert.assertFalse(container.contains(drawable))
    }

    @Test
    fun shouldDrawVisibleDrawables() {
        val drawable = DrawableMockBuilder().build()
        container.add(drawable)
        container.draw(context)
        Mockito.verify(drawable, Mockito.atLeastOnce()).draw(context)
    }

    @Test
    fun shouldNotDrawInvisibleDrawables() {
        val drawable = DrawableMockBuilder().invisible().build()
        container.add(drawable)
        container.draw(context)
        Mockito.verify(drawable, Mockito.never()).draw(context)
    }

    @Test
    fun shouldIterateBackToFront() {
        val drawable1 = DrawableMockBuilder().build()
        val drawable2 = DrawableMockBuilder().build()
        val drawable3 = DrawableMockBuilder().build()
        container.add(drawable1).add(drawable2).add(drawable3)

        val iter = container.backToFrontIterator()

        assertThat(iter.hasNext(), `is`(true))
        assertThat(iter.next(), `is`(`sameInstance`(drawable1)))
        assertThat(iter.next(), `is`(`sameInstance`(drawable2)))
        assertThat(iter.next(), `is`(`sameInstance`(drawable3)))
    }

    // Stacking order

    @Test
    fun shouldSetStackingOrderPositionLower() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(1, d3)

        assertThat(container.getStackingOrderPosition(d1), `is`(0))
        assertThat(container.getStackingOrderPosition(d3), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }

    @Test
    fun shouldSetStackingOrderPositionHigher() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(2, d2)

        assertThat(container.getStackingOrderPosition(d1), `is`(0))
        assertThat(container.getStackingOrderPosition(d3), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }

    @Test
    fun shouldSetStackingOrderPositionUnchanged() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.setStackingOrderPosition(1, d2)

        assertThat(container.getStackingOrderPosition(d1), `is`(0))
        assertThat(container.getStackingOrderPosition(d2), `is`(1))
        assertThat(container.getStackingOrderPosition(d3), `is`(2))
    }

    @Test
    fun shouldBringToFront() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.toFront(listOf(d3))

        assertThat(container.getStackingOrderPosition(d3), `is`(0))
        assertThat(container.getStackingOrderPosition(d1), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }

    @Test
    fun shouldBringToBack() {
        val d1 = DrawableMockBuilder().build()
        val d2 = DrawableMockBuilder().build()
        val d3 = DrawableMockBuilder().build()
        container.add(d3).add(d2).add(d1)

        container.toBack(listOf(d1, d2))

        assertThat(container.getStackingOrderPosition(d3), `is`(0))
        assertThat(container.getStackingOrderPosition(d1), `is`(1))
        assertThat(container.getStackingOrderPosition(d2), `is`(2))
    }

    // Direct containment tests

    @Test
    fun shouldDirectlyContainAt() {
        container.add(TestRectangle(Rectangle2D(100, 100, 10, 10)))
        assertThat(container.contains(105.0, 105.0), `is`(true))
        assertThat(container.contains(5.0, 5.0), `is`(false))
    }

    @Test
    fun shouldGetDrawableAt() {
        val rect = TestRectangle(Rectangle2D(100, 100, 10, 10))
        container.add(rect)
        assertThat(container.getDrawableAt(105.0, 105.0) as TestRectangle, `is`(rect))
    }

    // Composite DrawableContainerImpl

    @Test
    fun shouldContainNestedContainerAtDrawable() {
        val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
        innerContainer.add(TestRectangle(Rectangle2D(20, 20, 10, 10)))
        container.add(innerContainer)

        assertThat(container.contains(125.0, 125.0), `is`(true))
    }

    @Test
    fun shouldNotContainContainerBackground() {
        val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
        innerContainer.add(TestRectangle(Rectangle2D(20, 20, 10, 10)))
        container.add(innerContainer)

        assertThat(container.contains(110.0, 110.0), `is`(false))
    }

    @Test
    fun shouldNotGetNestedDrawable() {
        val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
        val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
        innerContainer.add(rect)
        container.add(innerContainer)

        assertThat(container.getDrawableAt(125.0, 125.0) as DrawableContainerImpl<Drawable>, `is`(innerContainer))
    }

    @Test
    fun shouldGetDrawableOfNestedContainer() {
        val rect = TestRectangle(Rectangle2D(20, 20, 10, 10))
        val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
        innerContainer.add(rect)
        container.add(innerContainer)

        assertThat(innerContainer.getDrawableAt(125.0, 125.0) as TestRectangle, `is`(rect))
    }

    @Test
    fun shouldYieldInnerTooltip() {
        val innerContainer = DrawableContainerImpl<Drawable>(location = Point2D(100, 100), useLocation = true)
        innerContainer.add(TestRectangle(Rectangle2D(20, 20, 10, 10)))

        container.add(innerContainer)

        assertThat(container.getToolTipText(125.0, 125.0, 1), `is`("Test"))
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

        assertThat(rect.mouseMoved, `is`(true))
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

        assertThat(rect.mousePressed, `is`(true))
    }


    private class TestRectangle(shape: RectangularShape) : AbstractRectangle(shape) {
        var mouseMoved = false
        var mousePressed = false
        private val handler = Handler()

        override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> = handler
        override fun draw(context: DrawContext) { }
        override val lineWidth: Double get() = 0.0
        override fun getToolTipText(x: Double, y: Double, width: Int?): String? = "Test"

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