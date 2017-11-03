package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.base.geom.*

/**
 * A special [LineTerminator] representing an arrow head.
 * The location of the [LineTerminator] is at the end of the [Polyline] to which it is connected.
 */
class ArrowHead(
    val width: Int,
    val length: Int,
    val compactness: Float,
    val filled: Boolean
) : AbstractDrawable(), LineTerminator {

    constructor(): this(DEF_WIDTH, DEF_LENGTH, DEF_COMPACTNESS, DEF_FILLED)

    companion object {

        /** The default width. */
        val DEF_WIDTH = 4

        /** The default length. */
        val DEF_LENGTH = 10

        /** The default compactness. */
        val DEF_COMPACTNESS = 1.0f

        /** The default 'filled' attribute. */
        val DEF_FILLED = true

        val TRANSFORM = System.SYSTEM!!.createAffineTransform()
    }

    val LOG by logger(ArrowHead::class)

    /** Holds the location of the arrow head peek.*/
    private var location = Point2D()

    /** Holds the location at which the attached [Polyline] segment should end.*/
    private var lineEnd = Point2D()

    /** Holds the rotation angle in radians.*/
    private var rotation: Double = 0.0

    private val shape: Path = createShape()

    /** ---- [Drawable] */

    override val boundingBox: RectangularShape
        get() = shape.boundingBox

    override fun draw(context: DrawContext) {
        context.g.draw(shape)
        if (filled) {
            context.g.fill(shape)
        }
    }

    override fun contains(x: Double, y: Double): Boolean {
        return shape.contains(x, y)
    }

    /** ---- [LineTerminator] */

    override fun setLocation(location: Point2D) {
        invalidate()
        setLocationImpl(location.x, location.y)
        invalidate()
        update()
    }

    override fun setLocation(location: Point2D, orientation: Point2D) {
        invalidate()
        setLocationImpl(location.x, location.y)
        TRANSFORM.setToIdentity()
        val rot = Geometry.angle(orientation, location)
        TRANSFORM.setToRotation(rotation - rot, location.x, location.y)
        lineEnd = TRANSFORM.transform(lineEnd)
        shape.transform(TRANSFORM)
        rotation = rot
        invalidate()
        update()
    }

    override fun getLineEnd(): Point2D {
        return lineEnd
    }

    /** ---- [ArrowHead] */

    private fun setLocationImpl(x: Double, y: Double) {
        TRANSFORM.setToTranslation(x - location.x, y - location.y)
        location = Point2D(x, y)
        shape.transform(TRANSFORM)
        lineEnd = TRANSFORM.transform(lineEnd)
    }

    private fun createShape(): Path {
        val path = System.SYSTEM!!.createPath()
        path.moveTo(0, 0)
        path.lineTo(-length, -width)
        if (compactness == 1.0f) {
            path.lineTo(-length, width)
            lineEnd = Point2D(-length, 0)
        } else {
            val innerLength = length * compactness
            path.lineTo(-innerLength.toDouble(), 0.0)
            path.lineTo(-length, width)
            lineEnd = Point2D(-innerLength.toDouble(), 0.0)
        }
        path.close()
        return path
    }
}