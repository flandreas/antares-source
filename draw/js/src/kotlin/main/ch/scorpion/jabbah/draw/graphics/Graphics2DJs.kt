package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.*
import org.w3c.dom.*

/**
 * Bridges [Graphics2D] methods to [CanvasRenderingContext2D] functionality.
 */
class Graphics2DJs(
	private val ctx: CanvasRenderingContext2D
) : AbstractGraphics2D() {

	val clip: Rectangle2D = Rectangle2D()

	companion object {

		private fun toJsFontStyle(font: Font): String {
			val sb = StringBuilder()
			if (font.isBold()) {
				sb.append("bold ")
			}
			if (font.isItalic()) {
				sb.append("italic ")
			}
			return sb.toString()
		}

		private fun toJsFontName(font: Font): String {
			return LogicalFontFamily.values()
				.firstOrNull { it.name == font.family.fontName }
				?.javaName
				?: LogicalFontFamily.SANS_SERIF.jsName
		}

		fun toJsFont(font: Font): String {
			return "${toJsFontStyle(font)} ${font.size}px ${toJsFontName(font)}"
		}
	}

	init {
		ctx.setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
	}

	/** ---- Path rendering methods */

	override fun beginPath() {
		ctx.beginPath()
	}

	override fun moveTo(x: Double, y: Double) {
		ctx.moveTo(x, y)
	}

	override fun lineTo(x: Double, y: Double) {
		ctx.lineTo(x, y)
	}

	override fun quadraticCurveTo(xc: Double, yc: Double, x1: Double, y1: Double) {
		ctx.quadraticCurveTo(xc, yc, x1, y1)
	}

	override fun cubicCurveTo(xc1: Double, yc1: Double, xc2: Double, yc2: Double, x1: Double, y1: Double) {
		ctx.bezierCurveTo(xc1, yc1, xc2, yc2, x1, y1)
	}

	override fun arc(x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean) {
		ctx.arc(x, y, radius, startAngle, endAngle, anticlockwise)
	}

	override fun strokePath() {
		ctx.stroke()
	}

	override fun fillPath() {
		ctx.fill()
	}

	override fun closePath() {
		ctx.closePath()
	}

	/** ---- [Graphics2D] interface */

	override val supportClipping: Boolean get() = false

	/** HTML canvas doesn't support to change the antialias property. */
	override var antialiasing: Boolean = true

	override fun setClipBounds(x: Int, y: Int, w: Int, h: Int) {
		// Not yet implemented
	}

	override fun setClipBounds(r: Rectangle2D?) {
		// Not yet implemented
	}

	override fun clip(x: Int, y: Int, w: Int, h: Int) {
		// Not yet implemented
	}

	/**
	 * HTML canvas doesn't provide access to the current transform yet, so keep a local
	 * [AffineTransform] and forward all changes to the rendering context.
	 */
	override var transform: AffineTransform = AffineTransformImpl(m00 = 1.0, m11 = 1.0)
		get() = AffineTransformImpl(field as AffineTransformImpl)
		set(value) {
			field = value
			forwardTransform()
		}

	override var color: Color = Color(0, 0, 0, 255)
		set(value) {
			field = value
			toJsColor(value).also {
				ctx.fillStyle = it
				ctx.strokeStyle = it
			}
		}

	override var paint: Paint = LinearColorGradient(Point2D.ZERO, Color.BLACK, Point2D.ZERO, Color.BLACK)
		set(value) {
			when (value) {
				is Color -> color = value
				is LinearColorGradient -> {
					field = value
					toJsGradient(value).also {
						ctx.fillStyle = it
						ctx.strokeStyle = it
					}
				}
				else -> throw IllegalArgumentException("unsupported Paint implementation")
			}
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
		rotationAngle += theta
		transform.rotate(theta)
		ctx.rotate(theta)
	}

	override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
		beginPath()
		moveTo(x1.toDouble(), y1.toDouble())
		lineTo(x2.toDouble(), y2.toDouble())
		strokePath()
		closePath()
	}

	override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
		beginPath()
		moveTo(x1, y1)
		lineTo(x2, y2)
		strokePath()
		closePath()
	}

	override fun drawRect(x: Int, y: Int, w: Int, h: Int) {
		ctx.strokeRect(x.toDouble() + 0.5, y.toDouble() + 0.5, w.toDouble(), h.toDouble())
	}

	override fun drawRect(x: Double, y: Double, w: Double, h: Double) {
		ctx.strokeRect(x, y, w, h)
	}

	override fun drawRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
		drawRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
	}

	override fun fillRect(x: Int, y: Int, w: Int, h: Int) {
		ctx.fillRect(x.toDouble() + 0.5, y.toDouble() + 0.5, w.toDouble(), h.toDouble())
	}

	override fun fillRect(x: Double, y: Double, w: Double, h: Double) {
		ctx.fillRect(x, y, w, h)
	}

	override fun fillRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) {
		fillRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
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

	override fun drawPolygon(x: IntArray, y: IntArray, n: Int) {
		ctx.beginPath()
		playPolygon(x, y, n)
		ctx.stroke()
	}

	override fun fillPolygon(x: IntArray, y: IntArray, n: Int) {
		ctx.beginPath()
		playPolygon(x, y, n)
		ctx.fill()
	}

	private fun playPolygon(x: IntArray, y: IntArray, n: Int) {
		ctx.moveTo(x[0].toDouble(), y[0].toDouble())
		for (i in 1 until n) {
			ctx.lineTo(x[i].toDouble(), y[i].toDouble())
		}
		ctx.closePath()
	}

	override fun drawDot(x: Int, y: Int) {
		fillRect(x, y, 1, 1)
	}

	override fun draw(shape: Shape) {
		when (shape) {
			is Path2DJs -> drawPath(shape)
			else -> super.draw(shape)
		}
	}

	override fun fill(shape: Shape) {
		when (shape) {
			is Path2DJs -> fillPath(shape)
			else -> super.fill(shape)
		}
	}

	override fun getClipBounds(): Rectangle2D? {
		return if (supportClipping) {
			Rectangle2D(clip)
		} else null
	}

	override fun getClipBounds(r: Rectangle2D): Rectangle2D {
		r.setFrame(clip.x, clip.y, clip.width, clip.height)
		return r
	}

	override fun drawString(s: String, x: Int, y: Int) {
		ctx.fillText(s, x.toDouble(), y.toDouble())
	}

	override fun drawImage(image: Image, x: Int, y: Int) {
		when (image) {
			is EmbeddedImageJs -> ctx.drawImage(image.image, x.toDouble(), y.toDouble())
			else -> throw IllegalArgumentException("unsupported image type ${image::class.simpleName}")
		}
	}

	/** ---- [Graphics2DJs] */

	private fun toJsGradient(gradient: LinearColorGradient): CanvasGradient {
		val jsGradient = ctx.createLinearGradient(gradient.p1.x, gradient.p1.y, gradient.p2.x, gradient.p2.y)
		jsGradient.addColorStop(0.0, toJsColor(gradient.color1))
		jsGradient.addColorStop(1.0, toJsColor(gradient.color2))
		return jsGradient
	}

	private fun forwardTransform() {
		val matrix = transform.getMatrix()
		ctx.setTransform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5])
	}

	private fun toLineCap(cap: CanvasLineCap): LineCap {
		return when (cap) {
			CanvasLineCap.Companion.BUTT -> LineCap.BUTT
			CanvasLineCap.Companion.ROUND -> LineCap.ROUND
			CanvasLineCap.Companion.SQUARE -> LineCap.SQUARE
			else -> throw IllegalArgumentException("unknown cap $cap")
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
		return when (join) {
			CanvasLineJoin.Companion.MITER -> LineJoin.MITER
			CanvasLineJoin.Companion.ROUND -> LineJoin.ROUND
			CanvasLineJoin.Companion.BEVEL -> LineJoin.BEVEL
			else -> throw IllegalArgumentException("unknown join $join")
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
		return Array(dash.size, { i -> dash[i].toFloat() }).toFloatArray()
	}

	private fun fromDash(dash: FloatArray?): Array<Double> {
		if (dash == null) {
			return doubleArrayOf().toTypedArray()
		}
		return Array(dash.size, { i -> dash[i].toDouble() })
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
}