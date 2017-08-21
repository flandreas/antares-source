package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Image

/** A [RectangularDrawable] that displays an image.*/
class ImageDrawable(private val image: Image) : AbstractRectangle(Rectangle2D()) {

    /** ----  [RectangularDrawable] */

    override val lineWidth: Double get() = 0.0

    override fun draw(context: DrawContext) {
        context.g.drawImage(image, x.toInt(), y.toInt())
    }
}