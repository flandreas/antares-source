package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.module.DrawModule


/**
 * A [Drawable] that displays a simple, single line text.
 *
 * @property text the original text
 * @property font The [Font] in which this [Label] is rendered
 * @property location The location at which this [Label] is rendered. The interpretation of this location depends on the
 *      horizontal and vertical orientation.
 */
class Label(
        text: String?,
        var font: Font,
        var color: Color? = null,
        horizontalAlignment: HorizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT,
        verticalAlignment: VerticalAlignment = DEFAULT_VERTICAL_ALIGNMENT,
        location:Point2D = Point2D(),
        private val rotationDisplayStrategy: RotationDisplayStrategy = Label.RotationDisplayStrategy.IGNORE,
        val rotation: Rotation = Rotation.R0
) : AbstractDrawable() {

    companion object {
        val LOG by logger(Label::class)
        val DEBUG_GFX = false
        val NEGATION_SIGN = "!"
        val NEGATION_STROKE = Stroke(1.4f)
        val DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER
        val DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER
        val BOUNDS_INSET = 1
    }

    var text: String = text ?: ""
        set(value) {
            invalidate()
            field = value
            displayableText = calculateDisplayableText()
            updateGeometry()
        }

    var location: Point2D = location
        get() = Point2D(field)
        set(value) {
            invalidate()
            field = value
            updateGeometry()
        }

    private var _horizontalAlignment: HorizontalAlignment = horizontalAlignment
    var horizontalAligment: HorizontalAlignment
        get() = _horizontalAlignment
        set(value) {
            invalidate()
            _horizontalAlignment = value
            updateGeometry()
        }

    private var _verticalAlignment: VerticalAlignment = verticalAlignment
    var verticalAligment: VerticalAlignment
        get() = _verticalAlignment
        set(value) {
            invalidate()
            _verticalAlignment = value
            updateGeometry()
        }

    var alignment: Alignment
        get() = Alignment(horizontalAligment, verticalAligment)
        set(value) {
            invalidate()
            _horizontalAlignment = value.horizontal
            _verticalAlignment = value.vertical
            updateGeometry()
        }

    /** Only used for unrotating the drawn text if the rotation angle is 180 degrees */
    var ownerRotation: Rotation = Rotation.R0

    /** The displayable text after convertion of negated representation. */
    private var displayableText: String = ""

    /** The [Rectangle2D] that contains the text entirely.*/
    private val bounds = Rectangle2D()

    /** The point at which the text's baseline starts relative to the location.*/
    private val baselinePoint = Point2D()

    init {
        displayableText = calculateDisplayableText()
        updateGeometry()
    }

    enum class HorizontalAlignment {
        LEFT {
            override fun opposite(): HorizontalAlignment = RIGHT
            override fun getX(baselineRect: Rectangle2D): Double = baselineRect.x
        },

        CENTER {
            override fun opposite() = CENTER
            override fun getX(baselineRect: Rectangle2D): Double = baselineRect.x - baselineRect.width / 2
        },

        RIGHT {
            override fun opposite(): HorizontalAlignment = LEFT
            override fun getX(baselineRect: Rectangle2D): Double = baselineRect.x - baselineRect.width
        };

        abstract fun opposite(): HorizontalAlignment

        /**
         * Returns the x-coordinate of the text shape relative to the [Label]'s location.
         * @param baselineRect the text's shape relative to the baseline.
         * @return the x-coordinate of the text shape.
         */
        abstract fun getX(baselineRect: Rectangle2D): Double
    }

    enum class VerticalAlignment {
        BOTTOM {
            override fun opposite(): VerticalAlignment = TOP
            override fun getY(baselineRect: Rectangle2D): Double = baselineRect.height
        },

        CENTER {
            override fun opposite(): VerticalAlignment = CENTER
            override fun getY(baselineRect: Rectangle2D): Double = baselineRect.height / 2
        },

        TOP {
            override fun opposite(): VerticalAlignment = BOTTOM
            override fun getY(baselineRect: Rectangle2D): Double = 0.0
        };

        internal abstract fun opposite(): VerticalAlignment

        /**
         * Returns the y-coordinate of the text shape relative to the [Label]'s location.
         * @param baselineRect the text's shape relative to the baseline.
         * @return the y-coordinate of the text shape.
         */
        internal abstract fun getY(baselineRect: Rectangle2D): Double
    }

    /** Used to update horizontal and vertical alignment at once. */
    data class Alignment(val horizontal: HorizontalAlignment, val vertical: VerticalAlignment)

    /** Defines how a [Label] reacts to a [Rotation] when drawing itself. */
    enum class RotationDisplayStrategy {

        IGNORE {
            override fun beforeDraw(context: DrawContext, label: Label) {
                // empty
            }

            override fun afterDraw(context: DrawContext, label: Label) {
                // empty
            }
        },

        KEEP_HORIZONTAL {
            override fun beforeDraw(context: DrawContext, label: Label) {
                context.g.translate(label.bounds.centerX, label.bounds.centerY)
                context.g.rotate(-label.ownerRotation.angle)
                context.g.translate(-label.bounds.centerX, -label.bounds.centerY)
            }

            override fun afterDraw(context: DrawContext, label: Label) {
                context.g.translate(label.bounds.centerX, label.bounds.centerY)
                context.g.rotate(label.ownerRotation.angle)
                context.g.translate(-label.bounds.centerX, -label.bounds.centerY)
            }
        };

        internal abstract fun beforeDraw(context: DrawContext, label: Label)
        internal abstract fun afterDraw(context: DrawContext, label: Label)

    }

    /** ---- [Drawable] */

    override val boundingBox: Rectangle2D
        get() = rotation.rotateRectangleAround(location, bounds)

    override fun contains(x: Double, y: Double): Boolean {
        return bounds.contains(x, y)
    }

    override val canMirror: Boolean get() = true

    override fun mirrorHorizontally(x: Double) {
        location = location.mirrorHorizontally(x)
        horizontalAligment = horizontalAligment.opposite()
        updateGeometry()
    }

    override fun mirrorVertically(y: Double) {
        location = location.mirrorVertically(y)
        verticalAligment = verticalAligment.opposite()
        updateGeometry()
    }

    override fun draw(context: DrawContext) {
        if (StringUtils.isBlank(text)) {
            return
        }

        val oldColor = context.g.color

        if (DEBUG_GFX) {
            context.g.color = Color.GRAY
            context.g.draw(boundingBox)
            context.g.color = Color.RED
            context.g.fillOval((location.x - 2).toInt(), (location.y - 2).toInt(), 4, 4)
        }

        if (!context.useContextColors && color != null) {
            context.g.color = color!!
        }
        context.g.font = font

        rotationDisplayStrategy.beforeDraw(context, this)

        context.g.translate(location.x, location.y)
        context.g.rotate(rotation.angle)
        context.g.translate(-location.x, -location.y)

        context.g.drawString(displayableText, baselinePoint.x.toInt(), baselinePoint.y.toInt())

        if (text.startsWith(NEGATION_SIGN)) {
            val oldStroke = context.g.stroke
            context.g.drawLine(
                bounds.x.toInt(), bounds.y.toInt(),
                bounds.maxX.toInt(), bounds.y.toInt()
            )
            context.g.stroke = oldStroke
        }

        context.g.translate(location.x, location.y)
        context.g.rotate(-rotation.angle)
        context.g.translate(-location.x, -location.y)

        rotationDisplayStrategy.afterDraw(context, this)

        context.g.color = oldColor
    }

    /** ---- [Label] */

    private fun updateGeometry() {
        val textRenderInfo = DrawModule.textRenderInfoFactory.invoke(displayableText, font)

        bounds.setFrame(
            location.x + horizontalAligment.getX(textRenderInfo.textBounds) - BOUNDS_INSET,
            location.y - verticalAligment.getY(textRenderInfo.textBounds) - BOUNDS_INSET,
            textRenderInfo.textBounds.width + 2 * BOUNDS_INSET,
            textRenderInfo.textBounds.height + 2 * BOUNDS_INSET
        )

        baselinePoint.setLocation(bounds.x + BOUNDS_INSET, bounds.y + textRenderInfo.ascent)

        validate()
        update()
    }

    private fun calculateDisplayableText(): String {
        if (text.startsWith(NEGATION_SIGN)) {
            return text.substring(1)
        }
        return text
    }

}