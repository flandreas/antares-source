package io.antarescircuit.jabbah.base.geom

import java.awt.geom.GeneralPath

/**
 * Adapts a [GeneralPath] to the [Path] interface.
 */
class Path2DJvm(
	val path: GeneralPath = GeneralPath()
) : Path {

    /** ---- [Path] interface */

	override fun clone(): Path = Path2DJvm(path.clone() as GeneralPath)

    override fun moveTo(x: Double, y: Double): Path {
        path.moveTo(x, y)
        return this
    }

    override fun lineTo(x: Double, y: Double): Path {
        path.lineTo(x, y)
        return this
    }

    override fun quadTo(x1: Double, y1: Double, x2: Double, y2: Double): Path {
        path.quadTo(x1, y1, x2, y2)
        return this
    }

    override fun curveTo(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): Path {
        path.curveTo(x1, y1, x2, y2, x3, y3)
        return this
    }

    override fun close(): Path {
        path.closePath()
        return this
    }

    override fun transform(transform: AffineTransform) {
        path.transform((transform as AffineTransformJvm).transform)
    }

    /** ---- [Shape] */

    override val boundingBox: RectangularShape
        get() {
            val b = path.bounds2D
            return Rectangle2D(b.x, b.y, b.width, b.height)
        }

    override fun contains(x: Double, y: Double): Boolean {
        return path.contains(x, y)
    }

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        return path.contains(x, y, width, height)
    }

    override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean {
        return path.intersects(x, y, w, h)
    }
}