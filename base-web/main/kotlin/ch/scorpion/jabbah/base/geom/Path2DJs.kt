package ch.scorpion.jabbah.base.geom

import org.w3c.dom.CanvasRenderingContext2D

/**
 * A simple implementation of a [Path2D] to be rendered on a HTML canvas.
 */
class Path2DJs : Path {

    private val entries = mutableListOf<Entry>()

    /** ---- [Path2D] interface */

    override fun moveTo(x: Double, y: Double): Path {
        entries.add(Entry(Command.MoveTo, Point2D(x, y)))
        return this
    }

    override fun moveTo(x: Int, y: Int): Path {
        entries.add(Entry(Command.MoveTo, Point2D(x, y)))
        return this
    }

    override fun moveTo(x: Float, y: Float): Path {
        entries.add(Entry(Command.MoveTo, Point2D(x.toDouble(), y.toDouble())))
        return this
    }

    override fun lineTo(x: Double, y: Double): Path {
        entries.add(Entry(Command.LineTo, Point2D(x, y)))
        return this
    }

    override fun lineTo(x: Int, y: Int): Path {
        entries.add(Entry(Command.LineTo, Point2D(x, y)))
        return this
    }

    override fun lineTo(x: Float, y: Float): Path {
        entries.add(Entry(Command.LineTo, Point2D(x.toDouble(), y.toDouble())))
        return this
    }

    override fun quadTo(x1: Double, y1: Double, x2: Double, y2: Double): Path {
        throw UnsupportedOperationException("not implemented")
    }

    override fun quadTo(x1: Int, y1: Int, x2: Int, y2: Int): Path {
        throw UnsupportedOperationException("not implemented")
    }

    override fun close(): Path {
        entries.add(Entry(Command.ClosePath))
        return this
    }

    override fun transform(transform: AffineTransform) {
        entries.forEach { it.transform(transform) }
    }

    /** ---- [Shape] interface */

    override val boundingBox = Rectangle2D()

    override fun contains(x: Double, y: Double): Boolean {
        return false
    }

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        return false
    }

    override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean {
        return false
    }

    /** ---- [Path2DJs] */

    fun play(ctx: CanvasRenderingContext2D) {
        for (entry in entries) {
            entry.play(this, ctx)
        }
    }

    private fun addEntry(cmd: Command, p: Point2D?) {
        entries.add(Entry(cmd, p))
    }

    private fun lastMoveToEntry(): Entry? {
        return entries.last { it.command == Command.MoveTo }
    }

    enum class Command() {
        MoveTo() {
            override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D, p: Point2D?) {
                ctx.moveTo(p!!.x, p!!.y)
            }
        },
        LineTo {
            override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D, p: Point2D?) {
                ctx.lineTo(p!!.x, p!!.y)
            }
        },
        ClosePath {
            override fun play(path: Path2DJs, ctx: CanvasRenderingContext2D, p: Point2D?) {
                path.lastMoveToEntry()?.let {
                    ctx.lineTo(it.p!!.x, it.p!!.y)
                }
            }
        };

        abstract fun play(path: Path2DJs, ctx: CanvasRenderingContext2D, p: Point2D?)
    }

    private data class Entry (val command: Command, val p: Point2D? = null) {

        fun play(path: Path2DJs, ctx: CanvasRenderingContext2D) {
            command.play(path, ctx, p)
        }

        fun transform(transform: AffineTransform) {
            if (p != null) {
                transform.transform(p, p)
            }
        }
    }
}