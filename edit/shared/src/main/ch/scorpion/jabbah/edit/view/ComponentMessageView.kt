package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.drawable.AbstractStyledDrawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.MultilineText
import ch.scorpion.jabbah.edit.style.EditStyleType

class ComponentMessageView(
        text: String,
        private val yDist: Double,
        frame: RectangularShape,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractStyledDrawable(EditStyleType.MESSAGE, styleProvider), Transparent, Unzoomable {

    private companion object {

        /** The horizontal inset between the bounding box and the text.  */
        private val INSET_X = 10

        /** The vertical inset between the bounding box and the text.  */
        private val INSET_Y = 10
    }

    private val LOG by logger(ComponentMessageView::class)

    private val shape = Rectangle2D(frame.x, frame.y, frame.width, frame.height)

    private val multilineText = MultilineText(text, font, frame.width.toInt() - 2 * INSET_X, font.size)

    /** ---- [Transparent] interface */

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) { transparent.transparency = value }

    /** ---- [Unzoomable] interface */

    override var zoomPan: ZoomPan? = ZoomPan()

    /** ---- [Drawable] interface */

    override val boundingBox: RectangularShape
        get() {
            return Rectangle2D(
                    shape.x - stroke.width,
                    shape.y - stroke.width + yDist / zoomPan!!.zoomFactor,
                    shape.width / zoomPan!!.zoomFactor + 2 * stroke.width,
                    shape.height / zoomPan!!.zoomFactor + 2 * stroke.width
            )
        }

    override fun draw(context: DrawContext) {
        LOG.debug("ComponentMessageView: draw")
        val r = getViewRectangle()
        context.g.color = transparent.applyTo(backgroundColor)
        context.g.fillRoundRect(r.x.toInt(), r.y.toInt(), r.width.toInt(), r.height.toInt(), 20, 20)
        context.g.color = transparent.applyTo(foregroundColor)
        context.g.stroke = stroke
        context.g.drawRoundRect(r.x.toInt(), r.y.toInt(), r.width.toInt(), r.height.toInt(), 20, 20)

        context.g.font = font
        context.g.color = transparent.applyTo(textColor)
        multilineText.draw(context, r.x.toInt() + INSET_X, r.y.toInt() + INSET_Y)
    }

    override fun contains(x: Double, y: Double): Boolean {
        return shape.contains(x, y)
    }

    /** ---- [ComponentMessageView] */

    fun setFrame(x: Double, y: Double, w: Double, h: Double) {
        invalidate()
        shape.setFrame(x, y, w, h)
        invalidate()
        update()
    }

    fun setFrame(frame: RectangularShape) {
        setFrame(frame.x, frame.y, frame.width, frame.height)
    }

    private fun getViewRectangle(): Rectangle2D {
        val p = zoomPan!!.transform.modelToView(Point2D(shape.x, shape.y))
        return Rectangle2D(p.x, p.y + yDist, shape.width, shape.height)
    }
}