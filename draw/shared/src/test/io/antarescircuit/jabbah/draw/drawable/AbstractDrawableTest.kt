package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.DrawableEvent
import io.antarescircuit.jabbah.draw.DrawableListener
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
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
        listener  = mock(MockMode.autofill)
        container = mock(MockMode.autofill)
    }

    @Test
    fun shouldRemoveDrawableListener() {
        drawable.addDrawableListener(listener)
        drawable.removeDrawableListener(listener)
        drawable.invalidate()
	    verify(exactly(0)) { listener.drawableInvalidated(DrawableEvent(drawable)) }
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

	    val slot = Capture.slot<DrawableEvent>()
        every { listener.drawableInvalidated(capture(slot)) } returns Unit

        drawable.invalidate()
	    assertEquals(drawable.boundingBox, slot.get().area)
    }

    @Test
    fun shouldNotifyListenerWhenValidated() {
        drawable.addDrawableListener(listener)

	    val slot = Capture.slot<DrawableEvent>()
	    every { listener.drawableRequestRedraw(capture(slot)) } returns Unit

        drawable.validate()
	    assertEquals(drawable.boundingBox, slot.get().area)
    }

    @Test
    fun shouldNotifyListenerWhenUpdated() {
        drawable.addDrawableListener(listener)

	    val slot = Capture.slot<DrawableEvent>()
	    every { listener.drawableUpdated(capture(slot)) } returns Unit

        drawable.setBounds(Rectangle2D(100.0, 100.0, 10.0, 10.0))
	    assertEquals(drawable.boundingBox, slot.get().area)
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
    class TestDrawable(bbox: Rectangle2D = Rectangle2D()) : AbstractDrawable() {

	    constructor(x: Int, y: Int, w: Int, h: Int): this(Rectangle2D(x, y, w, h))

        private val _boundingBox = bbox
        override val boundingBox: RectangularShape get() = _boundingBox

        /** ---- [Drawable] interface */

        override fun draw(context: DrawContext) {
            // empty
        }

        override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

        /** ---- [TestDrawable] */

        fun setBounds(bounds: Rectangle2D) {
            invalidate()
            _boundingBox.setFrame(bounds)
            invalidate()
            update()
        }
    }
}