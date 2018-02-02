package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.AffineTransformFx
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Shape
import javafx.scene.canvas.GraphicsContext
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight

/** Adapts the [javafx.scene.canvas.GraphicsContext] object to the [Graphics2D] interface.*/
class Graphics2DFx(var g: GraphicsContext) : AbstractGraphics2D() {

    init {
        g.transform.setToIdentity()
    }

    companion object {

        private fun toFxFontPosture(font: Font): FontPosture {
            if (font.isItalic()) {
                return FontPosture.ITALIC
            }
            return FontPosture.REGULAR
        }

        private fun toFxFontWeight(font: Font): FontWeight {
            if (font.isBold()) {
                return FontWeight.BOLD
            }
            return FontWeight.NORMAL
        }

        private fun toFxFont(font: Font): javafx.scene.text.Font {
            return javafx.scene.text.Font.font(font.family.javaName, toFxFontWeight(font), toFxFontPosture(font), font.size.toDouble())
        }

        private fun toLineCap(cap: StrokeLineCap): LineCap {
            return when (cap) {
                StrokeLineCap.BUTT -> LineCap.BUTT
                StrokeLineCap.ROUND -> LineCap.ROUND
                StrokeLineCap.SQUARE -> LineCap.SQUARE
            }
        }

        private fun fromLineCap(cap: LineCap): StrokeLineCap {
            return when (cap) {
                LineCap.BUTT -> StrokeLineCap.BUTT
                LineCap.ROUND -> StrokeLineCap.ROUND
                LineCap.SQUARE -> StrokeLineCap.SQUARE
            }
        }

        private fun toLineJoin(join: StrokeLineJoin): LineJoin {
            return when (join) {
                StrokeLineJoin.MITER -> LineJoin.MITER
                StrokeLineJoin.ROUND -> LineJoin.ROUND
                StrokeLineJoin.BEVEL -> LineJoin.BEVEL
            }
        }

        private fun fromLineJoin(join: LineJoin): StrokeLineJoin {
            return when (join) {
                LineJoin.MITER -> StrokeLineJoin.MITER
                LineJoin.ROUND -> StrokeLineJoin.ROUND
                LineJoin.BEVEL -> StrokeLineJoin.BEVEL
            }
        }

        private fun toDash(dash: Array<Double>): FloatArray {
            return Array(dash.size, { i -> dash[i].toFloat()}).toFloatArray()
        }

        private fun fromDash(dash: FloatArray?): Array<Double> {
            if (dash == null) {
                return doubleArrayOf().toTypedArray()
            }
            return Array(dash.size, { i -> dash[i].toDouble()})
        }
    }

    private val clipBounds = Rectangle2D()

    /** ---- Path rendering methods */

    override fun beginPath() {
        g.beginPath()
    }

    override fun moveTo(x: Double, y: Double) {
        g.moveTo(x, y)
    }

    override fun lineTo(x: Double, y: Double) {
        g.lineTo(x, y)
    }

    override fun quadraticCurveTo(xc: Double, yc: Double, x1: Double, y1: Double) {
        g.quadraticCurveTo(xc, yc, x1, y1)
    }

    override fun bezierCurveTo(xc1: Double, yc1: Double, xc2: Double, yc2: Double, x1: Double, y1: Double) {
        g.bezierCurveTo(xc1, yc1, xc2, yc2, x1, y1)
    }

    override fun arc(x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean) {
        // TODO JavaFX requires startAngle and length?!
        g.arc(x, y, radius, radius, startAngle, endAngle)
    }

    override fun strokePath() {
        g.stroke()
    }

    override fun fillPath() {
        g.fill()
    }

    override fun closePath() {
        g.closePath()
    }

    /** ---- [Graphics2D] interface */

    override var transform: AffineTransform
        get() = AffineTransformFx(g.transform)
        set(value) {
            if (value !is AffineTransformFx) {
                throw IllegalArgumentException("not a AffineTransformFx")
            }
            g.transform = value.affine
        }

    override var color: Color = Color(0, 0, 0, 255)
        set(value) {
            if (field != value) {
                field = value
                val alpha =
                        if (value.alpha == 255) 1.0
                        else if (value.alpha == 0) 0.0
                        else value.alpha / 255.0
                val fxColor = javafx.scene.paint.Color.rgb(value.red, value.green, value.blue, alpha)
                g.fill = fxColor
                g.stroke = fxColor
            }
        }

    override var stroke: Stroke
        get() = Stroke(
                width = g.lineWidth.toFloat(),
                cap = toLineCap(g.lineCap),
                join = toLineJoin(g.lineJoin),
                miterLimit = g.miterLimit.toFloat(),
                dash = toDash(g.lineDashes.toTypedArray()),
                dashPhase = g.lineDashOffset.toFloat())
        set(value) {
            g.lineWidth = value.width.toDouble()
            g.lineCap = fromLineCap(value.cap)
            g.lineJoin = fromLineJoin(value.join)
            g.miterLimit = value.miterLimit.toDouble()
            g.setLineDashes(*fromDash(value.dash).toDoubleArray())
            g.lineDashOffset = value.dashPhase?.toDouble() ?: 0.0
        }

    private var _font: Font? = null
    override var font: Font
        get() = _font!!
        set(value) {
            _font = value
            g.font = toFxFont(value)
        }

    /** In JavaFX, anti-aliasing is controlled in scenes and not in [GraphicsContext].*/
    override var antialiasing: Boolean = true

    override fun save() {
        g.save()
    }

    override fun restore() {
        g.restore()
    }

    override fun scale(sx: Double, sy: Double) {
        g.scale(sx, sy)
    }

    override fun translate(tx: Double, ty: Double) {
        g.translate(tx, ty)
    }

    override fun rotate(theta: Double) {
        g.rotate(theta)
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        g.strokeLine(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        g.strokeLine(x1, y1, x2, y2)
    }

    override fun drawRect(x: Int, y: Int, w: Int, h: Int) {
        g.strokeRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    }

    override fun drawRect(x: Double, y: Double, w: Double, h: Double) {
        g.strokeRect(x, y, w, h)
    }

    override fun drawRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        g.strokeRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int) {
        g.fillRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    }

    override fun fillRect(x: Double, y: Double, w: Double, h: Double) {
        g.fillRect(x, y, w, h)
    }

    override fun fillRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        g.fillRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
    }

    override fun drawOval(x: Int, y: Int, w: Int, h: Int) {
        g.strokeOval(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    }

    override fun drawOval(x: Double, y: Double, w: Double, h: Double) {
        g.strokeOval(x, y, w, h)
    }

    override fun fillOval(x: Int, y: Int, w: Int, h: Int) {
        g.fillOval(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
    }

    override fun fillOval(x: Double, y: Double, w: Double, h: Double) {
        g.fillOval(x, y, w, h)
    }

    override fun drawDot(x: Int, y: Int) {
        fillRect(x, y, 1, 1)
    }

    override fun drawString(s: String, x: Int, y: Int) {
        g.fillText(s, x.toDouble(), y.toDouble())
    }

    override fun drawText(s: String, x: Int, y: Int, w: Int) {
        // TODO Implement properly
        g.fillText(s, x.toDouble(), y.toDouble())
    }

    override fun drawImage(image: Image, x: Int, y: Int) {
        g.drawImage((image as ImageFx).image, x.toDouble(), y.toDouble())
    }

    override fun getClipBounds(): Rectangle2D {
        return clipBounds.copy()
    }

    override fun getClipBounds(r: Rectangle2D): Rectangle2D {
        r.setFrame(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height)
        return r
    }
}