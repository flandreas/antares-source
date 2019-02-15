package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.draw.DrawableListener
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.*

/**
 * Unit tests for [AbstractDrawable]
 */
class AbstractDrawableTest {

    private lateinit var drawable: TestDrawable
    private lateinit var listener: DrawableListener
    private lateinit var container: DrawableContainer<TestDrawable>

    @BeforeTest
    fun setup() {
        drawable = TestDrawable()
        listener  = mockk(relaxed = true)
        container = mockk(relaxed = true)
    }

    @Test
    fun shouldRemoveDrawableListener() {
        drawable.addDrawableListener(listener)
        drawable.removeDrawableListener(listener)
        drawable.invalidate()
	    verify(exactly = 0) { listener.drawableInvalidated(DrawableEvent(drawable)) }
    }

    @Test
    fun shouldSetParentWhenHandlingAdded() {
        drawable.handleAdded(container)
        assertSame(drawable.parent, container)
    }

    @Test
    fun shouldResetParentWhenHandlingRemoved() {
        drawable.handleRemoved(container)
        assertNull(drawable.parent)
    }

    @Test
    fun shouldNotifyParentWhenInvalidated() {
        drawable.handleAdded(container)
        drawable.invalidate()
	    verify { container.handleDrawableInvalidated(any(), any()) }
    }

    @Test
    fun shouldNotifyListenerWhenInvalidated() {
        drawable.addDrawableListener(listener)
        drawable.invalidate()

	    val slot = slot<DrawableEvent>()
	    verify { listener.drawableInvalidated(capture(slot)) }
	    assertEquals(drawable.boundingBox, slot.captured.area)
    }

    @Test
    fun shouldNotifyListenerWhenValidated() {
        drawable.addDrawableListener(listener)
        drawable.validate()

	    val slot = slot<DrawableEvent>()
	    verify { listener.drawableRequestRedraw(capture(slot)) }
	    assertEquals(drawable.boundingBox, slot.captured.area)
    }

    @Test
    fun shouldNotifyListenerWhenUpdated() {
        drawable.addDrawableListener(listener)
        drawable.setBounds(Rectangle2D(100.0, 100.0, 10.0, 10.0))

	    val slot = slot<DrawableEvent>()
	    verify { listener.drawableUpdated(capture(slot)) }
	    assertEquals(drawable.boundingBox, slot.captured.area)
    }

    @Test
    fun shouldNotifyParentWhenValidated() {
        drawable.handleAdded(container)
        drawable.validate()
	    verify { container.handleDrawableRequestRedraw(drawable) }
    }

    @Test
    fun shouldNotifyParentWhenUpdated() {
        drawable.handleAdded(container)
        drawable.setBounds(Rectangle2D(100.0, 100.0, 10.0, 10.0))
	    verify { container.handleDrawableUpdated(drawable) }
    }

    /** A [Drawable] implementation used in unit tests for [AbstractDrawable].*/
    class TestDrawable : AbstractDrawable() {

        override val boundingBox: Rectangle2D = Rectangle2D()

        /** ---- [Drawable] interface */

        override fun draw(context: DrawContext) {
            // empty
        }

        override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

        /** ---- [TestDrawable] */

        fun setBounds(bounds: Rectangle2D) {
            invalidate()
            boundingBox.setFrame(bounds)
            invalidate()
            update()
        }
    }
}