package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.polyline.PolylineShapeJvm
import java.awt.BasicStroke
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.geom.Area

/**
 * Adapts a [java.awt.Graphics2D] object to the [Graphics2D] interface.
 */
class Graphics2DJvm(var g: java.awt.Graphics2D) : Graphics2D {

    private val LOG by logger(Graphics2DJvm::class)

    /** Buffer used in [getClipBounds]. */
    private val clipBounds: Rectangle = Rectangle()

    companion object {
        val stack: Stack<java.awt.Graphics2D> by lazy { Stack<java.awt.Graphics2D>() }

        fun toFontStyle(awtFont: java.awt.Font): Int {
            var fontStyle = FontStyle.PLAIN.value
            if (awtFont.isBold) {
                fontStyle = fontStyle or FontStyle.BOLD.value
            }
            if (awtFont.isItalic) {
                fontStyle = fontStyle or FontStyle.ITALIC.value
            }
            return fontStyle
        }

        fun fromFontStyle(font: Font): Int {
            var fontStyle = java.awt.Font.PLAIN
            if (font.isBold()) {
                fontStyle = fontStyle or java.awt.Font.BOLD
            }
            if (font.isItalic()) {
                fontStyle = fontStyle or java.awt.Font.ITALIC
            }
            return fontStyle
        }

        fun toAwtColor(color: Color): java.awt.Color {
            return java.awt.Color(color.red, color.green, color.blue, color.alpha)
        }

        fun toAwtFont(font: Font): java.awt.Font {
            return java.awt.Font(font.family.javaName, fromFontStyle(font), font.size)
        }
    }

    /** ---- [Graphics2D] interface */

    override fun save() {
        val copy = g.create() as java.awt.Graphics2D
        stack.push(g)
        g = copy
    }

    override fun restore() {
        g.dispose()
        g = stack.pop()
    }

    override var transform: AffineTransform
        get() = AffineTransformJvm(g.transform)
        set(value) {
            if (value !is AffineTransformJvm) {
                throw IllegalArgumentException("not a java.awt.geom.AffineTransform")
            }
            g.transform = value.transform
        }

    override var antialiasing: Boolean
        get() = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON
        set(value) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, if(value) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF)
        }

    override var color: Color
        get() {
            val c = g.color
            return Color(c.red, c.green, c.blue, c.alpha)
        }
        set(value) {
            g.color = toAwtColor(value)
        }

    override var font: Font
        get() {
            val f = g.font
            return FontImpl(FontFamily.fromJavaName(f.name), toFontStyle(f), f.size)
        }
        set(value) {
            g.font = toAwtFont(value)
        }

    override var stroke: Stroke
        get() {
            val s = g.stroke as BasicStroke
            return Stroke(
                    width = s.lineWidth,
                    cap = toLineCap(s.endCap),
                    join = toLineJoin(s.lineJoin),
                    miterLimit = s.miterLimit,
                    dash = s.dashArray,
                    dashPhase = s.dashPhase
            )
        }
        set(value) {
            g.stroke = BasicStroke(
                    value.width,
                    fromLineCap(value.cap),
                    fromLineJoin(value.join),
                    value.miterLimit,
                    value.dash,
                    value.dashPhase ?: 0f
            )
        }

    override fun scale(sx: Double, sy: Double) = g.scale(sx, sy)

    override fun translate(tx: Double, ty: Double) = g.translate(tx, ty)

    override fun rotate(theta: Double) = g.rotate(theta)

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        g.drawLine(x1, y1, x2, y2)
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        g.drawLine(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
    }

    override fun drawRect(x: Int, y: Int, w: Int, h: Int) {
        g.drawRect(x, y, w, h)
    }

    override fun drawRect(x: Double, y: Double, w: Double, h: Double) {
        g.drawRect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    }

    override fun drawRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        g.drawRoundRect(x, y, w, h, arcW, arcH)
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int) {
        g.fillRect(x, y, w, h)
    }

    override fun fillRect(x: Double, y: Double, w: Double, h: Double) {
        g.fillRect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    }

    override fun fillRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        g.fillRoundRect(x, y, w, h, arcW, arcH)
    }

    override fun drawOval(x: Int, y: Int, w: Int, h: Int) {
        g.drawOval(x, y, w, h)
    }

    override fun drawOval(x: Double, y: Double, w: Double, h: Double) {
        g.drawOval(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    }

    override fun fillOval(x: Int, y: Int, w: Int, h: Int) {
        g.fillOval(x, y, w, h)
    }

    override fun fillOval(x: Double, y: Double, w: Double, h: Double) {
        g.fillOval(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    }

    override fun drawDot(x: Int, y: Int) {
        fillRect(x, y, 1, 1)
    }

    override fun drawString(s: String, x: Int, y: Int) {
        g.drawString(s, x, y)
    }

    override fun draw(shape: Shape) {
        when(shape) {
            is Rectangle2D -> drawRect(shape)
            is RoundRectangle2D -> drawRoundRect(shape)
            is Path2DJvm -> g.draw(shape.path)
            is PolylineShapeJvm -> g.draw(shape)
            is Ellipse2D -> drawEllipse(shape)
            is Ring2D -> drawRing(shape)
            else -> {
                LOG.error("Unsupported shape $shape")
                throw IllegalArgumentException("Unsupported shape " + shape.javaClass.name)
            }
        }
    }

    override fun fill(shape: Shape) {
        when(shape) {
            is Rectangle2D -> fillRect(shape)
            is RoundRectangle2D -> fillRoundRect(shape)
            is Path2DJvm -> g.fill(shape.path)
            is PolylineShapeJvm -> g.fill(shape)
            is Ellipse2D -> fillEllipse(shape)
            is Ring2D -> drawRing(shape)
            else -> {
                LOG.error("Unsupported shape $shape")
                throw IllegalArgumentException("Unsupported shape " + shape.javaClass.name)
            }
        }
    }

    override fun getClipBounds(): Rectangle2D {
        val b = g.clipBounds
        return Rectangle2D(b.x, b.y, b.width, b.height)
    }

    override fun getClipBounds(r: Rectangle2D): Rectangle2D {
        g.getClipBounds(clipBounds)
        r.setFrame(clipBounds.x.toDouble(), clipBounds.y.toDouble(), clipBounds.width.toDouble(), clipBounds.height.toDouble())
        return r
    }

    /** ---- [Graphics2DJvm] */

    private fun toLineCap(cap: Int): LineCap {
        return when (cap) {
            BasicStroke.CAP_BUTT -> LineCap.BUTT
            BasicStroke.CAP_ROUND -> LineCap.ROUND
            BasicStroke.CAP_SQUARE -> LineCap.SQUARE
            else -> throw IllegalArgumentException("unknown cap $cap")
        }
    }

    private fun fromLineCap(cap: LineCap): Int {
        return when (cap) {
            LineCap.BUTT -> BasicStroke.CAP_BUTT
            LineCap.ROUND -> BasicStroke.CAP_ROUND
            LineCap.SQUARE -> BasicStroke.CAP_SQUARE
        }
    }

    private fun toLineJoin(join: Int): LineJoin {
        return when(join) {
            BasicStroke.JOIN_MITER -> LineJoin.MITER
            BasicStroke.JOIN_ROUND -> LineJoin.ROUND
            BasicStroke.JOIN_BEVEL -> LineJoin.BEVEL
            else -> throw IllegalArgumentException("unknown join $join")
        }
    }

    private fun fromLineJoin(join: LineJoin): Int {
        return when (join) {
            LineJoin.MITER -> BasicStroke.JOIN_MITER
            LineJoin.ROUND -> BasicStroke.JOIN_ROUND
            LineJoin.BEVEL -> BasicStroke.JOIN_BEVEL
        }
    }

    private fun drawRect(rect: Rectangle2D) {
        drawRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
    }

    private fun fillRect(rect: Rectangle2D) {
        fillRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
    }

    private fun drawRoundRect(rect: RoundRectangle2D) {
        drawRoundRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt(), rect.arcW.toInt(), rect.arcH.toInt())
    }

    private fun fillRoundRect(rect: RoundRectangle2D) {
        fillRoundRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt(), rect.arcW.toInt(), rect.arcH.toInt())
    }

    private fun drawEllipse(e: Ellipse2D) {
        g.draw(java.awt.geom.Ellipse2D.Double(e.x, e.y, e.width, e.height))
    }

    private fun fillEllipse(e: Ellipse2D) {
        g.fill(java.awt.geom.Ellipse2D.Double(e.x, e.y, e.width, e.height))
    }

    private fun drawRing(r: Ring2D) {
        val outer = java.awt.geom.Ellipse2D.Double(r.x, r.y, r.width, r.height)
        val inner = java.awt.geom.Ellipse2D.Double(r.x + r.thickness, r.y + r.thickness, r.width - 2 * r.thickness, r.height - 2 * r.thickness)
        val area = Area(outer)
        area.subtract(Area(inner))
        g.fill(area)
    }
}