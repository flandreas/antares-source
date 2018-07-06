package ch.scorpion.jabbah.draw.drawable

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.draw.DrawableListener
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertEquals

/**
 * Unit tests for [AbstractDrawable]
 */
class AbstractDrawableTest {

    private lateinit var drawable: TestDrawable
    private lateinit var listener: DrawableListener
    private lateinit var container: DrawableContainer<TestDrawable>

    @Before
    fun setup() {
        drawable = TestDrawable()
        listener  = mock()
        container = mock()
    }

    @Test
    fun shouldRemoveDrawableListener() {
        drawable.addDrawableListener(listener)
        drawable.removeDrawableListener(listener)
        drawable.invalidate()
        Mockito.verify(listener, Mockito.never()).drawableInvalidated(DrawableEvent(drawable))
    }

    @Test
    fun shouldSetParentWhenHandlingAdded() {
        drawable.handleAdded(container)
        Assert.assertTrue(drawable.parent === container)
    }

    @Test
    fun shouldResetParentWhenHandlingRemoved() {
        drawable.handleRemoved(container)
        Assert.assertNull(drawable.parent)
    }

    @Test
    fun shouldNotifyParentWhenInvalidated() {
        drawable.handleAdded(container)
        drawable.invalidate()
        Mockito.verify(container).handleDrawableInvalidated(drawable, drawable.boundingBox)
    }

    @Test
    fun shouldNotifyListenerWhenInvaliated() {
        drawable.addDrawableListener(listener)
        drawable.invalidate()
        argumentCaptor<DrawableEvent>().apply {
            verify(listener).drawableInvalidated(capture())
            assertEquals(drawable.boundingBox, firstValue.area)
        }
    }

    @Test
    fun shouldNotifyListenerWhenValiated() {
        drawable.addDrawableListener(listener)
        drawable.validate()
        argumentCaptor<DrawableEvent>().apply {
            verify(listener).drawableRequestRedraw(capture())
            assertEquals(drawable.boundingBox, firstValue.area)
        }
    }

    @Test
    fun shouldNotifyListenerWhenUpdated() {
        drawable.addDrawableListener(listener)
        drawable.setBounds(Rectangle2D(100.0, 100.0, 10.0, 10.0))
        argumentCaptor<DrawableEvent>().apply {
            verify(listener).drawableUpdated(capture())
            assertEquals(drawable.boundingBox, firstValue.area)
        }
    }

    @Test
    fun shouldNotifyParentWhenValidated() {
        drawable.handleAdded(container)
        drawable.validate()
        Mockito.verify(container).handleDrawableRequestRedraw(drawable)
    }

    @Test
    fun shouldNotifyParentWhenUpdated() {
        drawable.handleAdded(container)
        drawable.setBounds(Rectangle2D(100.0, 100.0, 10.0, 10.0))
        Mockito.verify(container).handleDrawableUpdated(drawable)
    }

    /**
     * A [Drawable] implementation used in unit tests for [AbstractDrawable].
     */
    private class TestDrawable : AbstractDrawable() {

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