package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
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
        font: Font,
        var color: Color? = null,
        horizontalAlignment: HorizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT,
        verticalAlignment: VerticalAlignment = DEFAULT_VERTICAL_ALIGNMENT,
        location:Point2D = Point2D.ZERO,
        rotationDisplayStrategy: RotationDisplayStrategy = Label.RotationDisplayStrategy.IGNORE,
        val rotation: Rotation = Rotation.R0
) : AbstractDrawable() {

    companion object {
        val LOG by logger(Label::class)
        val DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER
        val DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER
        const val BOUNDS_INSET = 1
    }

    var text: String = text ?: ""
        set(value) {
            invalidate()
            field = value
            displayableText = calculateDisplayableText()
            updateGeometry()
        }

    var font: Font = font
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                updateGeometry()
            }
        }

    var location: Point2D = location
        get() = Point2D(field)
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                updateGeometry()
            }
        }

    private var _horizontalAlignment: HorizontalAlignment = horizontalAlignment
    var horizontalAlignment: HorizontalAlignment
        get() = _horizontalAlignment
        set(value) {
            if (_horizontalAlignment != value) {
                invalidate()
                _horizontalAlignment = value
                updateGeometry()
            }
        }

    private var _verticalAlignment: VerticalAlignment = verticalAlignment
    var verticalAlignment: VerticalAlignment
        get() = _verticalAlignment
        set(value) {
            if (verticalAlignment != value) {
                invalidate()
                _verticalAlignment = value
                updateGeometry()
            }
        }

    var alignment: Alignment
        get() = Alignment(horizontalAlignment, verticalAlignment)
        set(value) {
            if (alignment != value) {
                invalidate()
                _horizontalAlignment = value.horizontal
                _verticalAlignment = value.vertical
                updateGeometry()
            }
        }

    var rotationDisplayStrategy: RotationDisplayStrategy = rotationDisplayStrategy
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    /** Only used for unrotating the drawn text if the rotation angle is 180 degrees */
    var ownerRotation: Rotation = Rotation.R0

    /** The displayable text after convertion of negated representation. */
    private var displayableText: String = ""

    /** The [Rectangle2D] that contains the text entirely.*/
    private val bounds = Rectangle2D()

    /** The point at which the text's baseline starts relative to the location.*/
    private var baselinePoint = Point2D.ZERO

    init {
        displayableText = calculateDisplayableText()
        updateGeometry()
    }

    /** Defines how a [Label] reacts to a [Rotation] when drawing itself. */
    enum class RotationDisplayStrategy {

        /** Doesn't react to owner rotation, i.e. the label text is fully rotated.*/
        IGNORE {
            override fun beforeDraw(context: DrawContext, label: Label) {
                // empty
            }

            override fun afterDraw(context: DrawContext, label: Label) {
                // empty
            }
        },

        /**
         * Rotates the label text so that it is horizontal (when rotated 0 or 180 degrees),
         * or so that it is written from upwards and can be read from left (when rotated 90 or 270 degrees)
         */
        ROTATE_HALF {
            override fun beforeDraw(context: DrawContext, label: Label) {
                context.g.translate(label.bounds.centerX, label.bounds.centerY)
                context.g.rotate(calculateRotation(label).angle)
                context.g.translate(-label.bounds.centerX, -label.bounds.centerY)
            }

            override fun afterDraw(context: DrawContext, label: Label) {
                context.g.translate(label.bounds.centerX, label.bounds.centerY)
                context.g.rotate(calculateRotation(label).angle)
                context.g.translate(-label.bounds.centerX, -label.bounds.centerY)
            }

            private fun calculateRotation(label: Label): Rotation {
                return when (label.ownerRotation) {
                    Rotation.R0 -> Rotation.R0
                    Rotation.R180 -> Rotation.R180
                    Rotation.R90 -> Rotation.R0
                    Rotation.R270 -> Rotation.R180
                }
            }
        },

        /** Keeps the label text always horizontal.*/
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
        horizontalAlignment = horizontalAlignment.opposite()
        updateGeometry()
    }

    override fun mirrorVertically(y: Double) {
        location = location.mirrorVertically(y)
        verticalAlignment = verticalAlignment.opposite()
        updateGeometry()
    }

    override fun draw(context: DrawContext) {
        if (StringUtils.isBlank(text)) {
            return
        }

        val oldColor = context.g.color

	    DrawModule.drawDebugBoundingBox(this, context.g, Color.GRAY)

        context.g.color = if (context.useContextColors) {
            context.color!!.textColor
        } else {
	        color  ?: context.g.color
        }

        context.g.font = font

        rotationDisplayStrategy.beforeDraw(context, this)

        context.g.translate(location.x, location.y)
        context.g.rotate(rotation.angle)
        context.g.translate(-location.x, -location.y)

        context.g.drawString(displayableText, baselinePoint.x.toInt(), baselinePoint.y.toInt())

        context.g.translate(location.x, location.y)
        context.g.rotate(-rotation.angle)
        context.g.translate(-location.x, -location.y)

        rotationDisplayStrategy.afterDraw(context, this)

        context.g.color = oldColor
    }

    /** ---- [Label] */

    private fun updateGeometry() {
        val textRenderInfo = DrawModule.textRenderInfoFactory.measureSingleLineText(displayableText, font)

        bounds.setFrame(
            location.x + horizontalAlignment.getX(textRenderInfo.textBounds) - BOUNDS_INSET,
            location.y - verticalAlignment.getY(textRenderInfo.textBounds) - BOUNDS_INSET,
            textRenderInfo.textBounds.width + 2 * BOUNDS_INSET,
            textRenderInfo.textBounds.height + 2 * BOUNDS_INSET
        )

        baselinePoint = Point2D(bounds.x + BOUNDS_INSET, bounds.y + textRenderInfo.ascent)

        invalidate()
        update()
        validate()
    }

    private fun calculateDisplayableText(): String = StringUtils.replaceNegation(text)
}