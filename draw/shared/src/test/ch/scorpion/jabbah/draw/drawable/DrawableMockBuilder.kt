package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.base.geom.Rectangle2D
import com.nhaarman.mockito_kotlin.*
import org.mockito.Mockito

/**
 * A builder for mocks of [Drawable].
 */
class DrawableMockBuilder {

    private val drawable: Drawable =mock<Drawable>()

    init {
        Mockito.`when`(drawable.boundingBox).thenReturn(Rectangle2D())
        Mockito.`when`(drawable.visible).thenReturn(true)
    }

    fun invisible(): DrawableMockBuilder {
        Mockito.`when`(drawable.visible).thenReturn(false)
        return this
    }

    fun contains(x: Double, y: Double): DrawableMockBuilder {
        whenever(drawable.contains(eq(x), eq(y))).thenReturn(true)
        return this
    }

    fun tooltip(s: String): DrawableMockBuilder {
        Mockito.`when`(drawable.getToolTipText(any(), any(), any())).thenReturn(s)
        return this
    }

    fun build() = drawable
}