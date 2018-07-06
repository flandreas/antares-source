package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.Tooltip
import com.nhaarman.mockitokotlin2.*
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
        Mockito.`when`(drawable.getTooltip(any(), any())).thenReturn(Tooltip(s, 0.0, 0.0))
        return this
    }

    fun build() = drawable
}