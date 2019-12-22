package ch.scorpion.jabbah.base.geom

import org.w3c.dom.CanvasRenderingContext2D

/**
 * A simple implementation of a [Path2 to be rendered on a HTML canvas.
 */
class Path2DJs : Path {

    private val entries = mutableListOf<Entry>()

    /** ---- [Path] interface */

    override fun moveTo(x: Double, y: Double): Path {
        boundingBox.add(x, y)
        entries.add(Entry(MoveTo(Point2D(x, y))))
        return this
    }

    override fun moveTo(x: Int, y: Int): Path {
        boundingBox.add(x.toDouble(), y.toDouble())
        entries.add(Entry(MoveTo(Point2D(x, y))))
        return this
    }

    override fun moveTo(x: Float, y: Float): Path {
        boundingBox.add(x.toDouble(), y.toDouble())
        entries.add(Entry(MoveTo(Point2D(x.toDouble(), y.toDouble()))))
        return this
    }

    override fun lineTo(x: Double, y: Double): Path {
        boundingBox.add(x, y)
        entries.add(Entry(LineTo(Point2D(x, y))))
        return this
    }

    override fun lineTo(x: Int, y: Int): Path {
        boundingBox.add(x.toDouble(), y.toDouble())
        entries.add(Entry(LineTo(Point2D(x, y))))
        return this
    }

    override fun lineTo(x: Float, y: Float): Path {
        boundingBox.add(x.toDouble(), y.toDouble())
        entries.add(Entry(LineTo(Point2D(x.toDouble(), y.toDouble()))))
        return this
    }

    override fun quadTo(x1: Double, y1: Double, x2: Double, y2: Double): Path {
        boundingBox.add(x1, y1)
        boundingBox.add(x2, y2)
        entries.add(Entry(QuadTo(Point2D(x1, y1), Point2D(x2, y2))))
        return this
    }

    override fun curveTo(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): Path {
        boundingBox.add(x1, y1)
        boundingBox.add(x2, y2)
        boundingBox.add(x3, y3)
        entries.add(Entry(CurveTo(Point2D(x1, y1), Point2D(x2, y2), Point2D(x3, y3))))
        return this
    }

    override fun close(): Path {
        entries.add(Entry(ClosePath()))
        return this
    }

    override fun transform(transform: AffineTransform) {
        entries.forEach { it.transform(transform) }
    }

    /** ---- [Shape] interface */

    override val boundingBox = Rectangle2D()

    override fun contains(x: Double, y: Double): Boolean {
        return boundingBox.contains(x, y)
    }

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        return boundingBox.contains(x, y, width, height)
    }

    override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean {
        return boundingBox.intersects(x, y, w, h)
    }

    /** ---- [Path2DJs] */

    fun play(ctx: CanvasRenderingContext2D) {
        for (entry in entries) {
            entry.play(this, ctx)
        }
    }

    private fun lastMoveToEntry(): Entry? {
        return entries.last { it.command is MoveTo }
    }

    private interface Command {
        fun play(path: Path2DJs, ctx: CanvasRenderingContext2D)
        fun transform(transform: AffineTransform)
    }

    private class MoveTo(var p: Point2D) : Command {
        override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D) {
            ctx.moveTo(p.x, p.y)
        }
        override fun transform(transform: AffineTransform) {
            p = transform.transform(p)
        }
    }

    private class LineTo(var p: Point2D): Command {
        override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D) {
            ctx.lineTo(p.x, p.y)
        }
        override fun transform(transform: AffineTransform) {
            p = transform.transform(p)
        }
    }

    private class QuadTo(private var controlPoint: Point2D, private var endPoint: Point2D) : Command {
        override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D) {
            ctx.quadraticCurveTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)
        }
        override fun transform(transform: AffineTransform) {
            controlPoint = transform.transform(controlPoint)
            endPoint = transform.transform(endPoint)
        }
    }

    private class CurveTo(private var cp1: Point2D, private var cp2: Point2D, private var endPoint: Point2D) : Command {
        override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D) {
            ctx.bezierCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, endPoint.x, endPoint.y)
        }

        override fun transform(transform: AffineTransform) {
            cp1 = transform.transform(cp1)
            cp2 = transform.transform(cp2)
            endPoint = transform.transform(endPoint)
        }
    }

    private class ClosePath : Command {
        override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D) {
            path.lastMoveToEntry()?.let {
                ctx.lineTo((it.command as MoveTo).p.x, it.command.p.y)
            }
        }

        override fun transform(transform: AffineTransform) {
            // empty
        }
    }

    private data class Entry (val command: Command) {

        fun play(path: Path2DJs, ctx: CanvasRenderingContext2D) {
            command.play(path, ctx)
        }

        fun transform(transform: AffineTransform) {
            command.transform(transform)
        }
    }
}