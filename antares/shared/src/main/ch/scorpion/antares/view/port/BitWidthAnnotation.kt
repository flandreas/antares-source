package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment

/**
 * A graphical annotation that displays the [BitWidth] of a [DigitalPortView].
 * The origin location of the [BitWidthAnnotation] is at the origin of the containing [DigitalPortView].
 */
class BitWidthAnnotation(
    bitWidth: BitWidth,
    private val direction: Direction,
    private val centerLabel: Boolean,
    ownerRotation: Rotation = Rotation.R0,
	offsetX: Int = 0
) : AbstractDrawable() {

    companion object {
	    private const val LABEL_EDGE_DIST = 10.0
	    private const val LINE_WIDTH_HALF = 3.0
        private const val LINE_HEIGHT_HALF = 5.0
	    const val DIST = 0.75 * 2 * Look.SCALE
    }

    var offsetX: Int = offsetX
        set(value) {
            if (field != value) {
                field = value
                label.location = getLabelLocation()
                boundingBox = calculateBoundingBox()
            }
        }

    private val label: Label = Label(
        text = bitWidth.width.toString(),
        font = Themes.get<AntaresTheme>().annotation.font,
        horizontalAlignment = getHorizontalLabelAlignment(),
        verticalAlignment = getVerticalLabelAlignment(),
        location = getLabelLocation(),
	    rotationDisplayStrategy = RotationDisplayStrategy.ROTATE_HALF,
	    ownerRotation = ownerRotation,
        richText = false)

    /** ---- [Drawable] interface */

    override var boundingBox: Rectangle2D = calculateBoundingBox()
        private set

    override fun draw(context: DrawContext) {
        label.draw(context)
        if (!centerLabel) {
            context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
            val lineStart = getLineStart()
            val lineEnd = getLineEnd(lineStart)
            context.g.drawLine(
                lineStart.x.toInt(), lineStart.y.toInt(),
                lineEnd.x.toInt(), lineEnd.y.toInt())
        }
    }

    override fun contains(x: Double, y: Double): Boolean = label.contains(x, y)

    /** ---- [BitWidthAnnotation] */

    fun setOwnerRotation(rotation: Rotation) {
	    invalidate()
	    label.ownerRotation = rotation
        boundingBox = calculateBoundingBox()
	    invalidate()
	    update()
    }

    private fun calculateBoundingBox(): Rectangle2D {
        val bbox = Rectangle2D(label.boundingBox)
        val lineStart = getLineStart()
        bbox.add(lineStart)
        bbox.add(getLineEnd(lineStart))
        return bbox
    }

    private fun getLineBoxWidth(): Double {
        if (direction.isHorizontal()) {
            return 2 * LINE_WIDTH_HALF
        }
        return 2 * LINE_HEIGHT_HALF
    }

    private fun getLineBoxHeight(): Double {
        if (direction.isHorizontal()) {
            return 2 * LINE_HEIGHT_HALF
        }
        return 2 * LINE_WIDTH_HALF
    }

    private fun getLineEnd(lineStart: Point2D): Point2D =
		Point2D(lineStart.x - getLineBoxWidth(), lineStart.y + getLineBoxHeight())

    private fun getHorizontalLabelAlignment(): HorizontalAlignment =
        when (direction) {
            WEST, EAST -> HorizontalAlignment.CENTER
            NORTH, SOUTH -> if (centerLabel) HorizontalAlignment.CENTER else HorizontalAlignment.LEFT
        }

    private fun getVerticalLabelAlignment(): VerticalAlignment =
        when (direction) {
            WEST, EAST -> if (centerLabel) VerticalAlignment.CENTER else VerticalAlignment.TOP
            NORTH, SOUTH -> VerticalAlignment.CENTER
        }

    private fun getLabelLocation(): Point2D {
        val labelEdgeDist = if (centerLabel) 0.0 else LABEL_EDGE_DIST
        return when (direction) {
            WEST -> Point2D(-DIST - offsetX, labelEdgeDist)
            EAST -> Point2D(DIST + offsetX, labelEdgeDist)
            NORTH -> Point2D(labelEdgeDist, -DIST - offsetX)
            SOUTH -> Point2D(labelEdgeDist, DIST + offsetX)
        }
    }

    private fun getLineStart(): Point2D =
        when (direction) {
            WEST -> Point2D(-DIST - offsetX + LINE_WIDTH_HALF, -LINE_HEIGHT_HALF)
            EAST -> Point2D(DIST + offsetX + LINE_WIDTH_HALF, -LINE_HEIGHT_HALF)
            NORTH -> Point2D(LINE_HEIGHT_HALF, -DIST - offsetX - LINE_WIDTH_HALF)
            SOUTH -> Point2D(LINE_HEIGHT_HALF, DIST + offsetX - LINE_WIDTH_HALF)
        }
}