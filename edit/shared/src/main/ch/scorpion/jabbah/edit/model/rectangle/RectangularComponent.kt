package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Snappable
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.draw.graphics.Color

/**
 * A [RectangularComponent] is a [Component] with a [RectangularShape].
 */
open class RectangularComponent(
        styleType: StyleType = StyleType.FIGURE,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        val shape: RectangularShape = Rectangle2D(0.0, 0.0, 0.0, 0.0)
) : AbstractComponent(styleProvider, styleType), RectangularShape by shape {

    constructor(x: Double, y: Double, w: Double, h: Double): this(shape = Rectangle2D(x, y, w, h))

    /** ---- [RectangularShape] */

    override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        invalidate()
        shape.setFrame(x, y, width, height)
        invalidate()
        update()
    }

    override fun setFrame(rect: RectangularShape) {
        this.setFrame(rect.x, rect.y, rect.width, rect.height)
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeDouble("x", x)
        writer.writeDouble("y", y)
        writer.writeDouble("w", width)
        writer.writeDouble("h", height)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        setFrame(
            reader.readDouble("x"),
            reader.readDouble("y"),
            reader.readDouble("w"),
            reader.readDouble("h")
        )
    }

    /** ---- [Component] interface */

    override val type: String? get() = "edit.component.rectangle"

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy? = SelectionDrawingStrategy.ABOVE

    /** ---- [Locatable] interface */

    override var location: Point2D
        get() = Point2D(x, y)
        set(value) {
            setFrame(value.x, value.y, width, height)
        }

    /** ---- [Drawable] interface */

    override val boundingBox: Rectangle2D
        get() {
            val bb = Rectangle2D(shape.boundingBox)
            val lw = stroke.width
            bb.setFrame(
                bb.x - lw,
                bb.y - lw,
                bb.width + 2 * lw,
                bb.height + 2 * lw
            )
            return bb
        }

    override fun contains(x: Double, y: Double): Boolean {
        return shape.contains(x, y)
    }

    override val canMirror: Boolean = true

    override fun mirrorHorizontally(x: Double) {
        setFrame(Point2D(this.x + width, this.y).mirrorHorizontally(x).x, this.y, width, height)
    }

    override fun mirrorVertically(y: Double) {
        setFrame(this.x, Point2D(this.x, this.y + height).mirrorVertically(y).y, width, height)
    }

    override fun draw(context: DrawContext) {
        if (context.useContextColors) {
            drawImpl(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
        } else {
            drawImpl(context, foregroundColor, if (filled) backgroundColor else null)
        }
    }

    private fun drawImpl(context: DrawContext, lineColor: Color, fillColor: Color?) {
        val oldColor = context.g.color
        drawFill(context, shape, fillColor)
        drawStroke(context, shape, lineColor, stroke)
        context.g.color = oldColor
    }

    /** ---- [Snappable] interface */

    override val snappableX: DoubleArray get() = doubleArrayOf(minX, centerX, maxX)

    override val snappableY: DoubleArray get() = doubleArrayOf(minY, centerY, maxY)
}