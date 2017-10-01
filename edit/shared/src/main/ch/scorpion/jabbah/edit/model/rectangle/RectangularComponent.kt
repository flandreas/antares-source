package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.*

/**
 * A [RectangularComponent] is a [Component] with a [RectangularShape].
 */
abstract class RectangularComponent(
        styleType: StyleType = StyleType.FIGURE,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        val shape: RectangularShape
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

    override fun contains(p: Point2D): Boolean {
        return shape.contains(p)
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

    override val snappableX: Array<SnappableX> get() = arrayOf<SnappableX>(
            SnappableXCoordinate(minX),
            SnappableXCoordinate(centerX),
            SnappableXCoordinate(maxX))

    override val snappableY: Array<SnappableY> get() = arrayOf<SnappableY>(
            SnappableYCoordinate(minY),
            SnappableYCoordinate(centerY),
            SnappableYCoordinate(maxY))
}

open class RectangleComponent(
        styleType: StyleType = StyleType.FIGURE,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        shape: Rectangle2D = Rectangle2D(0.0, 0.0, 0.0, 0.0)
) : RectangularComponent(styleType, styleProvider, shape) {

    constructor(x: Double, y: Double, w: Double, h: Double): this(shape = Rectangle2D(x, y, w, h))

    override val type: String? get() = Translations.getString("edit.component.rectangle")
}

class RoundRectangleComponent(
        styleType: StyleType = StyleType.FIGURE,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        shape: RoundRectangle2D = RoundRectangle2D(0.0, 0.0, 0.0, 0.0, 10.0, 10.0)
) : RectangularComponent(styleType, styleProvider, shape) {

    constructor(x: Double, y: Double, w: Double, h: Double, arcW: Double, arcH: Double): this(shape = RoundRectangle2D(x, y, w, h, arcW, arcH))

    override val type: String? get() = Translations.getString("edit.component.roundrect")
}


class EllipseComponent(
        styleType: StyleType = StyleType.FIGURE,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        shape: Ellipse2D = Ellipse2D(0.0, 0.0, 0.0, 0.0)
) : RectangularComponent(styleType, styleProvider, shape) {

    constructor(x: Double, y: Double, w: Double, h: Double): this(shape = Ellipse2D(x, y, w, h))

    override val type: String? get() = Translations.getString("edit.component.ellipse")
}