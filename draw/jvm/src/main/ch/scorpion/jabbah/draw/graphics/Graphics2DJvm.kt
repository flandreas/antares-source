package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.geom.Shape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.polyline.PolylineShapeJvm
import java.awt.*
import java.awt.geom.Area
import java.awt.geom.Point2D

/**
 * Adapts a [java.awt.Graphics2D] object to the [Graphics2D] interface.
 */
class Graphics2DJvm(var g: java.awt.Graphics2D) : Graphics2D {

    /** Buffer used in [getClipBounds]. */
    private val clipBounds: Rectangle = Rectangle()

    companion object {
        private val LOG by logger(Graphics2DJvm::class)

        private val LINE = java.awt.geom.Line2D.Double()
        private val RECT = java.awt.geom.Rectangle2D.Double()
        private val ELLIPSE = java.awt.geom.Ellipse2D.Double()
        private val ROUND_RECT = java.awt.geom.RoundRectangle2D.Double()

        val stack: Stack<java.awt.Graphics2D> by lazy { Stack() }

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

        fun toAwtColor(color: Color): java.awt.Color =
	        java.awt.Color(color.red, color.green, color.blue, color.alpha)

	    fun fromAwtColor(color: java.awt.Color): Color =
		    Color(color.red, color.green, color.blue, color.alpha)

        fun toAwtFont(font: Font): java.awt.Font {
            return Font(font.family.fontName, fromFontStyle(font), font.size)
        }

	    fun fromAwtFont(font: java.awt.Font): Font {
	    	return FontImpl(LogicalFontFamily.fromJavaName(font.fontName), toFontStyle(font), font.size)
	    }

	    fun toAwtStroke(stroke: Stroke): java.awt.Stroke {
		    return BasicStroke(
			    stroke.width,
			    fromLineCap(stroke.cap),
			    fromLineJoin(stroke.join),
			    stroke.miterLimit,
			    stroke.dash,
			    stroke.dashPhase ?: 0f)
	    }

	    private fun fromLineCap(cap: LineCap): Int {
		    return when (cap) {
			    LineCap.BUTT -> BasicStroke.CAP_BUTT
			    LineCap.ROUND -> BasicStroke.CAP_ROUND
			    LineCap.SQUARE -> BasicStroke.CAP_SQUARE
		    }
	    }

	    private fun fromLineJoin(join: LineJoin): Int {
		    return when (join) {
			    LineJoin.MITER -> BasicStroke.JOIN_MITER
			    LineJoin.ROUND -> BasicStroke.JOIN_ROUND
			    LineJoin.BEVEL -> BasicStroke.JOIN_BEVEL
		    }
	    }
    }

    /** ---- [Graphics2D] interface */

    override val supportClipping: Boolean get() = true

	override var rotationAngle: Double = 0.0
		private set

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

	override var paint: Paint
		get() = fromAwtPaint(g.paint)
		set(value) {
			g.paint = toAwtPaint(value)
		}

    override var font: Font
        get() {
            val f = g.font
            return FontImpl(LogicalFontFamily.fromJavaName(f.name), toFontStyle(f), f.size)
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
	        g.stroke = toAwtStroke(value)
        }

    override fun scale(sx: Double, sy: Double) = g.scale(sx, sy)

    override fun translate(tx: Double, ty: Double) = g.translate(tx, ty)

    override fun rotate(theta: Double) {
	    rotationAngle += theta
	    g.rotate(theta)
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        g.drawLine(x1, y1, x2, y2)
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        LINE.setLine(x1, y1, x2, y2)
        g.draw(LINE)
    }

    override fun drawRect(x: Int, y: Int, w: Int, h: Int) {
        g.drawRect(x, y, w, h)
    }

    override fun drawRect(x: Double, y: Double, w: Double, h: Double) {
        RECT.setFrame(x, y, w, h)
        g.draw(RECT)
    }

    override fun drawRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        g.drawRoundRect(x, y, w, h, arcW, arcH)
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int) {
        g.fillRect(x, y, w, h)
    }

    override fun fillRect(x: Double, y: Double, w: Double, h: Double) {
        RECT.setFrame(x, y, w, h)
        g.fill(RECT)
    }

    override fun fillRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
        g.fillRoundRect(x, y, w, h, arcW, arcH)
    }

    override fun drawOval(x: Int, y: Int, w: Int, h: Int) {
        g.drawOval(x, y, w, h)
    }

    override fun drawOval(x: Double, y: Double, w: Double, h: Double) {
        ELLIPSE.setFrame(x, y, w, h)
        g.draw(ELLIPSE)
    }

    override fun fillOval(x: Int, y: Int, w: Int, h: Int) {
        g.fillOval(x, y, w, h)
    }

    override fun fillOval(x: Double, y: Double, w: Double, h: Double) {
        ELLIPSE.setFrame(x, y, w, h)
        g.fill(ELLIPSE)
    }

	override fun drawPolygon(x: IntArray, y: IntArray, n: Int) {
		g.drawPolygon(x, y, n)
	}

	override fun fillPolygon(x: IntArray, y: IntArray, n: Int) {
		g.fillPolygon(x, y, n)
	}

    override fun drawDot(x: Int, y: Int) {
        fillRect(x.toDouble(), y.toDouble(), 2.0, 2.0)
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

	override fun clip(x: Int, y: Int, w: Int, h: Int) {
		if (supportClipping) {
			g.clipRect(x, y, w, h)
		}
	}

	override fun setClipBounds(x: Int, y: Int, w: Int, h: Int) {
		if (supportClipping) {
			g.setClip(x, y, w, h)
		}
	}

	override fun setClipBounds(r: Rectangle2D?) {
		if (r == null) {
			g.clip = null
		} else {
			setClipBounds(r.x.toInt(), r.y.toInt(), r.widthInt, r.heightInt)
		}
	}

    override fun getClipBounds(): Rectangle2D? =
		if (supportClipping) {
			g.clipBounds?.let {
				Rectangle2D(it.x, it.y, it.width, it.height)
			}
		} else null

    override fun getClipBounds(r: Rectangle2D): Rectangle2D {
        g.getClipBounds(clipBounds)
        r.setFrame(clipBounds.x.toDouble(), clipBounds.y.toDouble(), clipBounds.width.toDouble(), clipBounds.height.toDouble())
        return r
    }

    override fun drawImage(image: Image, x: Int, y: Int) {
	    when (image) {
			is ResourceImageJvm -> g.drawImage(image.imageIcon.image, x, y, null)
		    is BufferedImageJvm -> g.drawImage(image.jvmImage, x, y, null)
		    else -> throw IllegalArgumentException("unsupported image type ${image::class.simpleName}")
		}
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

    private fun toLineJoin(join: Int): LineJoin {
        return when(join) {
            BasicStroke.JOIN_MITER -> LineJoin.MITER
            BasicStroke.JOIN_ROUND -> LineJoin.ROUND
            BasicStroke.JOIN_BEVEL -> LineJoin.BEVEL
            else -> throw IllegalArgumentException("unknown join $join")
        }
    }

    private fun drawRect(rect: Rectangle2D) {
        RECT.setFrame(rect.x, rect.y, rect.width, rect.height)
        g.draw(RECT)
    }

    private fun fillRect(rect: Rectangle2D) {
        RECT.setFrame(rect.x, rect.y, rect.width, rect.height)
        g.fill(RECT)
    }

    private fun drawRoundRect(rect: RoundRectangle2D) {
        ROUND_RECT.setRoundRect(rect.x, rect.y, rect.width, rect.height, rect.arcW, rect.arcH)
        g.draw(ROUND_RECT)
    }

    private fun fillRoundRect(rect: RoundRectangle2D) {
        ROUND_RECT.setRoundRect(rect.x, rect.y, rect.width, rect.height, rect.arcW, rect.arcH)
        g.fill(ROUND_RECT)
    }

    private fun drawEllipse(e: Ellipse2D) {
        ELLIPSE.setFrame(e.x, e.y, e.width, e.height)
        g.draw(ELLIPSE)
    }

    private fun fillEllipse(e: Ellipse2D) {
        ELLIPSE.setFrame(e.x, e.y, e.width, e.height)
        g.fill(ELLIPSE)
    }

    private fun drawRing(r: Ring2D) {
        val outer = java.awt.geom.Ellipse2D.Double(r.x, r.y, r.width, r.height)
        val inner = java.awt.geom.Ellipse2D.Double(r.x + r.thickness, r.y + r.thickness, r.width - 2 * r.thickness, r.height - 2 * r.thickness)
        val area = Area(outer)
        area.subtract(Area(inner))
        g.fill(area)
    }

	private fun toAwtPaint(paint: Paint): java.awt.Paint {
		return when (paint) {
			is Color -> toAwtColor(paint)
			is LinearColorGradient -> GradientPaint(
				Point2D.Float(paint.p1.x.toFloat(), paint.p1.y.toFloat()),
				toAwtColor(paint.color1),
				Point2D.Float(paint.p2.x.toFloat(), paint.p2.y.toFloat()),
				toAwtColor(paint.color2))
			else -> throw IllegalArgumentException("unsupported paint ${paint::class.simpleName}")
		}
	}

	private fun fromAwtPaint(paint: java.awt.Paint): Paint {
		return when (paint) {
			is java.awt.Color -> fromAwtColor(paint)
			is GradientPaint -> LinearColorGradient(
				p1 = Point2D(paint.point1.x, paint.point1.y),
				color1 = fromAwtColor(paint.color1),
				p2 = Point2D(paint.point2.x, paint.point2.y),
				color2 = fromAwtColor(paint.color2))
			else -> throw java.lang.IllegalArgumentException("unsupported AWT paint ${paint::class.simpleName}")
		}
	}
}