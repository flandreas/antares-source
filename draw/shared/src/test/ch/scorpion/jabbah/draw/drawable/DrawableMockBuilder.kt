package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.Drawable
import io.mockk.every
import io.mockk.mockk

/**
 * A builder for mocks of [Drawable].
 */
class DrawableMockBuilder {

    private val drawable: Drawable = mockk(relaxed = true)

    init {
	    every { drawable.boundingBox } returns Rectangle2D()
	    every { drawable.visible } returns true
    }

    fun invisible(): DrawableMockBuilder {
	    every { drawable.visible } returns false
        return this
    }

    fun contains(x: Double, y: Double): DrawableMockBuilder {
	    every { drawable.contains(eq(x), eq(y)) } returns true
        return this
    }

    fun tooltip(s: String): DrawableMockBuilder {
	    every { drawable.getTooltip(any(), any()) } returns Tooltip(s, 0.0, 0.0)
        return this
    }

    fun build() = drawable
}