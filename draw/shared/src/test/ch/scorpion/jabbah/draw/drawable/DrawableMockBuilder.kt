package ch.scorpion.jabbah.draw.drawable

import com.nhaarman.mockito_kotlin.mock
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.base.geom.Rectangle2D
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

    fun build() = drawable
}