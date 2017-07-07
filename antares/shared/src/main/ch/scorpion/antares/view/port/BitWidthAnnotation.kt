package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.style.Themes


/**
 * A graphical annotation that displays the [BitWidth] of a [DigitalPortView].
 */
class BitWidthAnnotation(
    bitWidth: BitWidth,
    val direction: Direction
) : AbstractDrawable() {

    companion object {
        val LABEL_EDGE_DIST = 10.0
        val LINE_WIDTH_HALF = 3.0
        val LINE_HEIGHT_HALF = 5.0
        val LINE_POS_X_FACT = 0.75

        private fun getHorizontalLabelAlignment(direction: Direction): Label.HorizontalAlignment {
            when (direction) {
                Direction.WEST -> return Label.HorizontalAlignment.CENTER
                Direction.EAST -> return Label.HorizontalAlignment.CENTER
                Direction.NORTH -> return Label.HorizontalAlignment.LEFT
                Direction.SOUTH -> return Label.HorizontalAlignment.LEFT
                else -> throw IllegalStateException("unknown Direction " + direction)
            }
        }

        private fun getVerticalLabelAlignment(direction: Direction): Label.VerticalAlignment {
            when (direction) {
                Direction.WEST -> return Label.VerticalAlignment.TOP
                Direction.EAST -> return Label.VerticalAlignment.TOP
                Direction.NORTH -> return Label.VerticalAlignment.CENTER
                Direction.SOUTH -> return Label.VerticalAlignment.CENTER
                else -> throw IllegalStateException("unknown Direction " + direction)
            }
        }

        private fun getLabelLocation(direction: Direction): Point2D {
            when (direction) {
                Direction.WEST -> return Point2D(-2 * Look.SCALE * LINE_POS_X_FACT, LABEL_EDGE_DIST)
                Direction.EAST -> return Point2D(2 * Look.SCALE * LINE_POS_X_FACT, LABEL_EDGE_DIST)
                Direction.NORTH -> return Point2D(LABEL_EDGE_DIST, -2 * Look.SCALE * LINE_POS_X_FACT)
                Direction.SOUTH -> return Point2D(LABEL_EDGE_DIST, 2 * Look.SCALE * LINE_POS_X_FACT)
                else -> throw IllegalStateException("unknown Direction " + direction)
            }
        }

        private fun getLineStart(direction: Direction): Point2D {
            when (direction) {
                Direction.WEST -> return Point2D(LINE_POS_X_FACT * -2 * Look.SCALE + LINE_WIDTH_HALF, -LINE_HEIGHT_HALF)
                Direction.EAST -> return Point2D(LINE_POS_X_FACT * 2 * Look.SCALE + LINE_WIDTH_HALF, -LINE_HEIGHT_HALF)
                Direction.NORTH -> return Point2D(LINE_HEIGHT_HALF, -2 * Look.SCALE * LINE_POS_X_FACT - LINE_WIDTH_HALF)
                Direction.SOUTH -> return Point2D(LINE_HEIGHT_HALF, 2 * Look.SCALE * LINE_POS_X_FACT - LINE_WIDTH_HALF)
                else -> throw IllegalStateException("unknown Direction " + direction)
            }
        }
    }

    private val label: Label = Label(
        text = bitWidth.width.toString(),
        font = Themes.get<AntaresTheme>().annotation.font,
        horizontalAlignment = getHorizontalLabelAlignment(direction),
        verticalAlignment = getVerticalLabelAlignment(direction),
        location = getLabelLocation(direction)
    )

    /** ---- [Drawable] interface */

    override fun draw(context: DrawContext) {
        label.draw(context)
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		val lineStart = getLineStart(direction)
		context.g.drawLine(
			lineStart.x.toInt(), lineStart.y.toInt(),
            (lineStart.x - getLineBoxWidth(direction)).toInt(), (lineStart.y + getLineBoxHeight(direction)).toInt())
    }

    override val boundingBox: Rectangle2D get() = label.boundingBox

    override fun contains(x: Double, y: Double): Boolean {
        return label.contains(x, y)
    }

    /** ---- [BitWidthAnnotation] */

    private fun getLineBoxWidth(direction: Direction): Double {
        if (direction.isHorizontal()) {
            return 2 * LINE_WIDTH_HALF
        }
        return 2 * LINE_HEIGHT_HALF
    }

    private fun getLineBoxHeight(direction: Direction): Double {
        if (direction.isHorizontal()) {
            return 2 * LINE_HEIGHT_HALF
        }
        return 2 * LINE_WIDTH_HALF
    }
}