package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * A non-editable text view with a fixed width that adjusts its height according to the text to display.
 *
 * The anchor of the surrounding [RectangularShape] is determined relative to an anchor point and
 * a facing direction. For example, when using [Direction.NORTH] as facing direction, the [RectangularShape]
 * is growing towards north, and the anchor point is at the middle of the lower edge of the [RectangularShape].
 * This can be used for placing a description above the contents of a [Drawable].
 */
class FlexibleTextView(
        text: String,
        anchor: Point2D,
        private val direction: Direction,
        private val width: Int = DEFAULT_WIDTH,
        private val isUnzoomable: Boolean = true,
        styleType: StyleType,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractStyledDrawable(styleType, styleProvider), Transparent, Unzoomable, Locatable {

    private companion object {

        private val LOG by logger(FlexibleTextView::class)

        /** The default width of a [FlexibleTextView] if none is specified upon construction.*/
        private val DEFAULT_WIDTH = 200

        /** The horizontal inset between the surrounding rectangle and the text.  */
        private val INSET_X = 10

        /** The vertical inset between the surrounding rectangle and the text.  */
        private val INSET_Y = 10
    }

    private val multilineText = MultilineText(text, font, width.toDouble())

    /** The shape representing the overall box (including insets) in model coordinates, but excluding stroke widths.*/
    private val shape = Rectangle2D()

    /** ---- [Locatable] */

    /** Represents the location of the anchor position in model coordinates. */
    override var location: Point2D = anchor
        set(value) {
            invalidate()
            updateGeometry()
            invalidate()
            update()
        }

    /** ---- [Drawable] */

    override val boundingBox: RectangularShape
        get() {
            if (isUnzoomable) {
                val p = calculateBoxCorner(location, 1 / zoomPan!!.zoomFactor)
                return Rectangle2D(
                        p.x - stroke.width,
                        p.y - stroke.width,
                        shape.width / zoomPan!!.zoomFactor + 2 * stroke.width,
                        shape.height / zoomPan!!.zoomFactor + 2 * stroke.width
                )
            }
            return Rectangle2D(
                    shape.x - stroke.width,
                    shape.y - stroke.width,
                    shape.width + 2 * stroke.width,
                    shape.height + 2 * stroke.width)
        }

    override fun draw(context: DrawContext) {
        LOG.debug("FlexibleTextView: draw")
        val r = if (isUnzoomable) getViewRectangle() else shape
        context.g.color = transparent.applyTo(backgroundColor)
        context.g.fillRoundRect(r.x.toInt(), r.y.toInt(), r.width.toInt(), r.height.toInt(), 20, 20)
        context.g.color = transparent.applyTo(foregroundColor)
        context.g.stroke = stroke
        context.g.drawRoundRect(r.x.toInt(), r.y.toInt(), r.width.toInt(), r.height.toInt(), 20, 20)

        context.g.font = font
        context.g.color = transparent.applyTo(textColor)

        context.g.translate(r.x + INSET_X, r.y + INSET_Y)
        multilineText.draw(context)
        context.g.translate(-(r.x + INSET_X), -(r.y + INSET_Y))

    }

    override fun contains(x: Double, y: Double): Boolean = shape.contains(x, y)

    /** ---- [Unzoomable] interface */

    override var zoomPan: ZoomPan? = ZoomPan()
        set(value) {
            if (field == value) {
                return
            }
            invalidate()
            field = value
            updateGeometry()
            invalidate()
            update()
        }

    /** ---- [Transparent] interface */

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) { transparent.transparency = value }

    /** ---- [FlexibleTextView] */

    init {
        updateGeometry()
    }

    /** Calculates the geometry of this [FlexibleTextView] in model coordinates and stores it in [shape]. */
    private fun updateGeometry() {
        val boxCorner = calculateBoxCorner(location, 1.0)
        shape.setFrame(boxCorner.x, boxCorner.y, boxWidth, boxHeight)
    }

    /** Calculates the upper-left corner of the surrounding box in view coordinates.*/
    private fun calculateBoxCorner(anchor: Point2D, f: Double): Point2D {
        return when(direction) {
            Direction.NORTH -> {
                Point2D(anchor.x - (width / 2 + INSET_X) * f, anchor.y - (multilineText.height + 2 * INSET_Y) * f)
            }
            Direction.SOUTH -> {
                Point2D(anchor.x - (width / 2 + INSET_X) * f, anchor.y)
            }
            Direction.WEST -> {
                Point2D(anchor.x - boxWidth * f,anchor.y - boxHeight / 2 * f)
            }
            Direction.EAST -> {
                Point2D(anchor.x, anchor.y - boxHeight / 2 * f)
            }
        }
    }

    /** Transform [shape] to view coordinates using the current [zoomPan]. */
    private fun getViewRectangle(): Rectangle2D {
        val anchorView = zoomPan!!.transform.modelToView(location)
        val p = calculateBoxCorner(anchorView, 1.0)
        return Rectangle2D(p.x, p.y, shape.width, shape.height)
    }

    private val boxWidth: Double get() = width + 2.0 * INSET_X

    private val boxHeight: Double get() = multilineText.height + 2.0 * INSET_Y

}