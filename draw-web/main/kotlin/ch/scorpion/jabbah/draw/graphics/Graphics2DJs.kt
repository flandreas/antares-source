package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import org.w3c.dom.*

/**
 * Bridges [Graphics2D] methods to [CanvasRenderingContext2D] functionality.
 */
class Graphics2DJs(val ctx: CanvasRenderingContext2D) : Graphics2D {

    private val LOG by logger()

    val clip: Rectangle2D = Rectangle2D()

    companion object {

        fun toJsFontStyle(font: Font): String {
            val sb = StringBuilder()
            if (font.isBold()) {
                sb.append("bold ")
            }
            if (font.isItalic()) {
                sb.append("italic ")
            }
            return sb.toString()
        }

        fun toJsFont(font: Font): String {
            return "${toJsFontStyle(font)} ${font.size}px ${font.family.jsName}"
        }
    }

    init {
        // Initialize with identity
        ctx.setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
    }

    /** ---- [Graphics2D] interface */

    /** HTML canvas doesn't support to change the antialias property. */
    override var antialiasing: Boolean = true

    /**
     * HTML canvas doesn't provide access to the current transform yet, so keep a local
     * [AffineTransform] and forward all changes to the rendering context.
     */
    override var transform: AffineTransform = AffineTransformImpl()
        get() = AffineTransformImpl(field as AffineTransformImpl)
        set(value) {
            field = value
            forwardTransform()
        }

    override var color: Color = Color(0, 0, 0, 255)
        get() = field
        set(value) {
            field = value
            val alpha =
                    if (value.alpha == 255) 1.0
                    else if (value.alpha == 0) 0.0
                    else value.alpha / 255.0
            val rgba = "rgba(${value.red},${value.green},${value.blue},$alpha)"
            ctx.fillStyle = rgba
            ctx.strokeStyle = rgba
        }

    override var stroke: Stroke
        get() {
            return Stroke(
                    width = ctx.lineWidth.toFloat(),
                    cap = toLineCap(ctx.lineCap),
                    join = toLineJoin(ctx.lineJoin),
                    miterLimit = ctx.miterLimit.toFloat(),
                    dash = toDash(ctx.getLineDash()),
                    dashPhase = ctx.lineDashOffset.toFloat()
            )
        }
        set(value) {
            ctx.lineWidth = value.width.toDouble()
            ctx.lineCap = fromLineCap(value.cap)
            ctx.lineJoin = fromLineJoin(value.join)
            ctx.miterLimit = value.miterLimit.toDouble()
            ctx.setLineDash(fromDash(value.dash))
            ctx.lineDashOffset = value.dashPhase?.toDouble() ?: 0.0
        }

    private var _font: Font? = null
    override var font: Font
        get() = _font!!
        set(value) {
            _font = value
            ctx.font = toJsFont(value)
        }

    override fun save() = ctx.save()

    override fun restore() = ctx.restore()

    override fun scale(sx: Double, sy: Double) {
        transform.scale(sx, sy)
        ctx.scale(sx, sy)
    }

    override fun translate(tx: Double, ty: Double) {
        transform.translate(tx, ty)
        ctx.translate(tx, ty)
    }

    override fun rotate(theta: Double) {
        transform.rotate(theta)
        ctx.rotate(theta)
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        ctx.beginPath()
        ctx.moveTo(x1.toDouble(), y1.toDouble())
        ctx.lineTo(x2.toDouble(), y2.toDouble())
        ctx.stroke()
        ctx.closePath()
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        ctx.beginPath()
        ctx.moveTo(x1, y1)
        ctx.lineTo(x2, y2)
        ctx.stroke()
        ctx.closePath()
    }

    override fun drawRect(x: Int, y: Int, w: Int, h: Int) {
        ctx.strokeRect(x.toDouble() + 0.5, y.toDouble() + 0.5, w.toDouble(), h.toDouble())
    }

    override fun drawRect(x: Double, y: Double, w: Double, h: Double) {
        ctx.strokeRect(x, y, w, h)
    }

    override fun drawRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        ctx.beginPath()
        roundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
        ctx.stroke()
        ctx.closePath()
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int) {
        ctx.fillRect(x.toDouble() + 0.5, y.toDouble() + 0.5, w.toDouble(), h.toDouble())
    }

    override fun fillRect(x: Double, y: Double, w: Double, h: Double) {
        ctx.fillRect(x, y, w, h)
    }

    override fun fillRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        ctx.beginPath()
        roundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
        ctx.fill()
        ctx.closePath()
    }

    override fun drawOval(x: Double, y: Double, w: Double, h: Double) {
        ctx.beginPath()
        playEllipse(x, y, w, h)
        ctx.stroke()
        ctx.closePath()
    }

    override fun drawOval(x: Int, y: Int, w: Int, h: Int) {
        drawOval(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    }

    override fun fillOval(x: Double, y: Double, w: Double, h: Double) {
        ctx.beginPath()
        playEllipse(x, y, w, h)
        ctx.fill()
        ctx.closePath()
    }

    override fun fillOval(x: Int, y: Int, w: Int, h: Int) {
        fillOval(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    }

    override fun drawDot(x: Int, y: Int) {
        fillRect(x, y, 1, 1)
    }

    override fun draw(shape: Shape) {
        when (shape) {
            is Rectangle2D -> drawRect(shape)
            is Path2DJs -> drawPath(shape)
            is PolylineShape -> drawPolyline(shape)
            else -> {
                LOG.error("Unsupported shape $shape")
                throw IllegalArgumentException("Unsupported shape $shape")
            }
        }
    }

    override fun fill(shape: Shape) {
        when (shape) {
            is Rectangle2D -> fillRect(shape)
            is Path2DJs -> fillPath(shape)
            is PolylineShape -> fillPoyline(shape)
            else -> {
                LOG.error("Unsupported shape $shape")
                throw IllegalArgumentException("Unsupported shape $shape")
            }
        }
    }

    override fun getClipBounds(): Rectangle2D {
        return Rectangle2D(clip)
    }

    override fun getClipBounds(r: Rectangle2D): Rectangle2D {
        r.setFrame(clip.x, clip.y, clip.width, clip.height)
        return r
    }

    override fun drawString(s: String, x: Int, y: Int) {
        ctx.fillText(s, x.toDouble(), y.toDouble())
    }

    /** ---- [Graphics2DJs] */

    private fun forwardTransform() {
        val matrix = transform.getMatrix()
        ctx.setTransform(matrix[0],matrix[1],matrix[2],matrix[3],matrix[4],matrix[5])
    }

    private fun toLineCap(cap: CanvasLineCap): LineCap {
        return when (cap) {
            CanvasLineCap.Companion.BUTT -> LineCap.BUTT
            CanvasLineCap.Companion.ROUND -> LineCap.ROUND
            CanvasLineCap.Companion.SQUARE -> LineCap.SQUARE
            else -> throw IllegalArgumentException("unknown cap ${cap}")
        }
    }

    private fun fromLineCap(cap: LineCap): CanvasLineCap {
        return when (cap) {
            LineCap.BUTT -> CanvasLineCap.Companion.BUTT
            LineCap.ROUND -> CanvasLineCap.Companion.ROUND
            LineCap.SQUARE -> CanvasLineCap.Companion.SQUARE
        }
    }

    private fun toLineJoin(join: CanvasLineJoin): LineJoin {
        return when(join) {
            CanvasLineJoin.Companion.MITER -> LineJoin.MITER
            CanvasLineJoin.Companion.ROUND -> LineJoin.ROUND
            CanvasLineJoin.Companion.BEVEL -> LineJoin.BEVEL
            else -> throw IllegalArgumentException("unknown join ${join}")
        }
    }

    private fun fromLineJoin(join: LineJoin): CanvasLineJoin {
        return when (join) {
            LineJoin.MITER -> CanvasLineJoin.Companion.MITER
            LineJoin.ROUND -> CanvasLineJoin.Companion.ROUND
            LineJoin.BEVEL -> CanvasLineJoin.Companion.BEVEL
        }
    }

    private fun toDash(dash: Array<Double>): FloatArray {
        return Array<Float>(dash.size, {i -> dash[i].toFloat()}).toFloatArray()
    }

    private fun fromDash(dash: FloatArray?): Array<Double> {
        if (dash == null) {
            return doubleArrayOf().toTypedArray()
        }
        return Array<Double>(dash.size, {i -> dash[i].toDouble()})
    }

    private fun drawRect(rect: Rectangle2D) {
        drawRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
    }

    private fun fillRect(rect: Rectangle2D) {
        fillRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
    }

    private fun roundRect(x: Double, y: Double, w: Double, h: Double, arcW: Double, arcH: Double) {
        // Not yet implemented
        ctx.rect(x, y, w, h)
    }

    private fun drawPath(path: Path2DJs) {
        ctx.beginPath()
        path.play(ctx)
        ctx.stroke()
        ctx.closePath()
    }

    private fun fillPath(path: Path2DJs) {
        ctx.beginPath()
        path.play(ctx)
        ctx.fill()
        ctx.closePath()
    }

    private fun drawPolyline(polyline: PolylineShape) {
        ctx.beginPath()
        playPolyline(polyline)
        ctx.stroke()
        ctx.closePath()
    }

    private fun fillPoyline(polyline: PolylineShape) {
        ctx.beginPath()
        playPolyline(polyline)
        ctx.fill()
        ctx.closePath()
    }

    private fun playPolyline(polyline: PolylineShape) {
        if (polyline.pointsCount < 2) {
            return
        }
        ctx.moveTo(polyline.getPointAt(0).x, polyline.getPointAt(0).y)
        for (i in 1..polyline.pointsCount - 1) {
            ctx.lineTo(polyline.getPointAt(i).x, polyline.getPointAt(i).y)
        }
    }

    private fun playEllipse(x: Double, y: Double, w: Double, h: Double) {
        val kappa = 0.5522848
        val ox = (w / 2) * kappa
        val oy = (h / 2) * kappa
        val xe = x + w
        val ye = y + h
        val xm = x + w / 2
        val ym = y + h / 2

        ctx.moveTo(x, ym)
        ctx.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y)
        ctx.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym)
        ctx.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye)
        ctx.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym)
    }
}