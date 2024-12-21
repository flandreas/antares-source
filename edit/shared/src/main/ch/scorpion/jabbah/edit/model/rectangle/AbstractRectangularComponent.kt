package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Mirrorable
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * An abstract [Component] implementation that has a [RectangularShape].
 *
 * [AbstractRectangularComponent] provides a changeable rectangular geometry, but doesn't draw itself.
 * It can be used as a base class for implementing various rectangular [Component]s.
 */
abstract class AbstractRectangularComponent(
    styleType: StyleType = StyleType.FIGURE,
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    val shape: MutableRectangularShape = Rectangle2D()
) : AbstractComponent(styleProvider, styleType), MutableRectangularShape by shape, Mirrorable {

    companion object {
        fun createBoundingBox(c: Component, shape: RectangularShape): RectangularShape {
            val bb = Rectangle2D(shape.boundingBox)
            val lw = c.stroke.width
            bb.setFrame(
                bb.x - lw,
                bb.y - lw,
                bb.width + 2 * lw,
                bb.height + 2 * lw
            )
            if (c.shadow) {
                DropShadow.expand(bb, c.rotation)
            }
            return bb
        }
    }

    open val shapeToDraw: Shape get() = shape

    /**
     * If `true`, the ratio between width and length is maintained during interactive changes of dimension.
     * Not yet enforced in [setFrame].
     */
    open val maintainAspectRation: Boolean get() = false

    open fun drawText(context: DrawContext) {
        // empty
    }

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

    /** ---- [Locatable] interface */

    override var location: Point2D
        get() = Point2D(x, y)
        set(value) {
            setFrame(value.x, value.y, width, height)
        }

    /** ---- [Drawable] interface */

    override val boundingBox: RectangularShape get() = createBoundingBox(this, shape)

    override fun contains(x: Double, y: Double): Boolean = shape.contains(x, y)

    override fun contains(p: Point2D): Boolean = shape.contains(p)

    override fun intersects(rect: RectangularShape): Boolean = shape.intersects(rect)

    /** ---- [Mirrorable] */

    override fun mirrorHorizontally(x: Double) {
        setFrame(Point2D(this.x + width, this.y).mirrorHorizontally(x).x, this.y, width, height)
    }

    override fun mirrorVertically(y: Double) {
        setFrame(this.x, Point2D(this.x, this.y + height).mirrorVertically(y).y, width, height)
    }

    /** ---- [Snappable] interface */

    override val snappableX: Array<SnappableX>
        get() = arrayOf(
            SnappableXCoordinate(minX),
            SnappableXCoordinate(centerX),
            SnappableXCoordinate(maxX)
        )

    override val snappableY: Array<SnappableY>
        get() = arrayOf(
            SnappableYCoordinate(minY),
            SnappableYCoordinate(centerY),
            SnappableYCoordinate(maxY)
        )

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
}