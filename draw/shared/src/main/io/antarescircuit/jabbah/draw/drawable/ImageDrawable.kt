package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Image

/** A [RectangularDrawable] that displays an image.*/
class ImageDrawable(private val image: Image) : AbstractRectangle(Rectangle2D()) {

    /** ----  [RectangularDrawable] */

    override val lineWidth: Double get() = 0.0

    override fun draw(context: DrawContext) {
        context.g.drawImage(image, x.toInt(), y.toInt())
    }
}