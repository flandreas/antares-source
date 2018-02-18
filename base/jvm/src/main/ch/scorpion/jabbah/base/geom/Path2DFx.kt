package ch.scorpion.jabbah.base.geom

import javafx.scene.canvas.GraphicsContext
import javafx.scene.shape.*

/** Adapts a [javafx.scene.shape.Path] to the [Path] interface.*/
class Path2DFx : Path {

    val path = javafx.scene.shape.Path()

    /** ---- [Path] interface */

    override fun moveTo(x: Double, y: Double): Path {
        path.elements.add(MoveTo(x, y))
        return this
    }

    override fun lineTo(x: Double, y: Double): Path {
        path.elements.add(LineTo(x, y))
        return this
    }

    override fun quadTo(x1: Double, y1: Double, x2: Double, y2: Double): Path {
        path.elements.add(QuadCurveTo(x1, y1, x2, y2))
        return this
    }

    override fun curveTo(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): Path {
        path.elements.add(CubicCurveTo(x1, y1, x2, y2, x3, y3))
        return this
    }

    override fun close(): Path {
        path.elements.add(ClosePath())
        return this
    }

    override fun transform(transform: AffineTransform) {
        // TODO Not supported in JavaFX?
    }

    /** ---- [Shape] interface */

    override val boundingBox: RectangularShape
        get() {
            val b = path.boundsInLocal
            return Rectangle2D(b.minX, b.minY, b.width, b.height)
        }

    override fun contains(x: Double, y: Double): Boolean {
        // TEST BEGIN
        path.isPickOnBounds = true
        // TEST END
        return path.contains(x, y)
    }

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        return contains(x, y) && contains(x + width, y) && contains(x, y + height) && contains(x + width, y + height)
    }

    override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean {
        return path.intersects(x, y, w, h)
    }

    /** ---- [Path2DFx] */

    fun play(g: GraphicsContext) {
        path.elements.forEach {
            when(it) {
                is MoveTo -> g.moveTo(it.x, it.y)
                is LineTo -> g.lineTo(it.x, it.y)
                is QuadCurveTo -> g.quadraticCurveTo(it.controlX, it.controlY, it.x, it.y)
                is CubicCurveTo -> g.bezierCurveTo(it.controlX1, it.controlY1, it.controlX2, it.controlY2, it.x, it.y)
                is ClosePath -> g.closePath()
            }
        }
    }
}