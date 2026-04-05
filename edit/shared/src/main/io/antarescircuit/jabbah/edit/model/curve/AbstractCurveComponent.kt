package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.*
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

abstract class AbstractCurveComponent(
    points: List<Point2D>
) : AbstractComponent(), Transparent, Mirrorable {

    companion object {
        private const val CONTAINS_SENSITIVITY = 2.0
    }

    /** Contains the n points of this [AbstractCurveComponent] in absolute coordinate space.*/
    private var _points = mutableListOf<Point2D>()
        set(value) {
            if (field.size > 0) {
                invalidate()
            }
            field = value
            updatePath()
            invalidate()
            update()
        }

    /** Contains the [Path] representing `_points` in absolute coordinate space. */
    protected lateinit var path: Path

    var points: List<Point2D>
        get() = _points
        set(value) {
            check(value.size == pointsCount)
            setPointsImpl(value)
        }

    init {
        require(points.size == pointsCount) { "Points count must be $pointsCount, but is ${points.size}" }
        setPointsImpl(points)
    }

    /** ---- UI properties */

    @Suppress("MemberVisibilityCanBePrivate") // Reflection
    var isHorizontallyMirrored: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (!isReading) {
                    mirrorHorizontally(location.x)
                }
            }
        }

    @Suppress("MemberVisibilityCanBePrivate") // Reflection
    var isVerticallyMirrored: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (!isReading) {
                    mirrorVertically(location.y)
                }
            }
        }

    /** ---- [Locatable] */

    override var location: Point2D
        get() = _points[0]
        set(value) {
            if (location != value) {
                doInvalidating {
                    val dx = value.x - points[0].x
                    val dy = value.y - points[0].y
                    _points.toList().forEachIndexed { index, p -> _points[index] = p.add(dx, dy) }
                }
            }
        }

    /** ---- [Transparent] interface */

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) {
            transparent.transparency = value
        }

    /** ---- [Rotatable] interface */

    override fun isRotatableWith(selection: Collection<*>): Boolean = true

    override val useRotation: Boolean get() = false

    override fun rotate(direction: RotationDirection, pivot: Point2D?) {
        points = points.map { direction.rotation.rotatePointAround(points[0], it) }
    }

    /** ---- [Mirrorable] interface */

    override fun mirrorHorizontally(x: Double) {
        points = points.map { it.mirrorHorizontally(x) }
    }

    override fun mirrorVertically(y: Double) {
        points = points.map { it.mirrorVertically(y) }
    }

    /** ---- [Snappable] interface */

    override val snappableX: Array<SnappableX> get() = arrayOf(SnappableXCoordinate(points[0].x), SnappableXCoordinate(points[2].x))

    override val snappableY: Array<SnappableY> get() = arrayOf(SnappableYCoordinate(points[0].y), SnappableYCoordinate(points[2].y))

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        val p = mutableListOf<Point2D>()
        p.addAll(reader.readPoints("points"))
        _points = p
        if (reader.hasAttribute("mirrorH")) {
            isHorizontallyMirrored = reader.readBoolean("mirrorH")
        }
        if (reader.hasAttribute("mirrorV")) {
            isVerticallyMirrored = reader.readBoolean("mirrorV")
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writePoints("points", points)
        if (isHorizontallyMirrored) {
            writer.writeBoolean("mirrorH", isHorizontallyMirrored)
        }
        if (isVerticallyMirrored) {
            writer.writeBoolean("mirrorV", isVerticallyMirrored)
        }
    }

    /** ---- [Drawable] */

    override val boundingBox: RectangularShape get() = path.boundingBox

    override fun contains(x: Double, y: Double): Boolean =
        path.intersects(
            x - CONTAINS_SENSITIVITY,
            y - CONTAINS_SENSITIVITY,
            2 * CONTAINS_SENSITIVITY,
            2 * CONTAINS_SENSITIVITY)

    override fun draw(context: DrawContext) {
        if (context.useContextColors) {
            drawImpl(context, context.color!!.foregroundColor, if (filled) context.color!!.backgroundColor else null)
        } else {
            drawImpl(context, transparent.applyTo(foregroundColor), if (filled) transparent.applyTo(backgroundColor) else null)
        }
    }

    private fun drawImpl(context: DrawContext, lineColor: Color, fillColor: Color?) {
        val oldColor = context.g.color

        if (shadow && fillColor != null) {
            DropShadow.draw(context, transparency) {
                context.g.fill(path)
            }
        }

        if (fillColor != null) {
            context.g.color = fillColor
            context.g.fill(path)
        }
        context.g.color = lineColor
        context.g.stroke = stroke
        context.g.draw(path)

        context.g.color = oldColor
    }

    /** ---- [AbstractComponent] */

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.ABOVE
        set(value) {
            super.preferredSelectionDrawingStrategy = value
        }

    /** ---- [AbstractCurveComponent] */

    protected abstract val pointsCount: Int

    protected abstract fun updatePath()

    fun getPointAt(index: Int): Point2D = points[index]

    fun setPointAt(index: Int, location: Point2D) {
        require(index in 0 until pointsCount) { "index $index not in range 0..$pointsCount" }
        doInvalidating { _points[index] = location }
    }

    private fun doInvalidating(logic: () -> Unit) {
        if (points.size == pointsCount) {
            invalidate()
        }
        logic.invoke()
        updatePath()
        invalidate()
        update()
    }

    private fun setPointsImpl(newPoints: List<Point2D>) {
        val p = mutableListOf<Point2D>()
        p.addAll(newPoints)
        _points = p
    }
}