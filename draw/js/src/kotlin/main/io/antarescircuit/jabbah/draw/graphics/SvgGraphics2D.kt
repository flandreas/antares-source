package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.geom.*
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.*

/**
 * A [Graphics2D] that records all drawing operations as SVG elements, the browser's counterpart of the
 * desktop's Batik based SVG export. Draw into it like into a canvas, then call [toSvg].
 *
 * Mirrors [Graphics2DJs] (same font mapping, half-pixel offsets of integer rectangles, no clipping),
 * so exported drawings look like the editor's canvas.
 */
class SvgGraphics2D : AbstractGraphics2D() {

	/** The state saved and restored by [save] and [restore], like a canvas rendering context's state. */
	private class State(
		var matrix: DoubleArray = doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, 0.0),
		var paint: Paint = Color(0, 0, 0, 255),
		var stroke: Stroke = Stroke(),
		var font: Font? = null,
		var rotation: Double = 0.0,
	) {
		fun copy() = State(matrix.copyOf(), paint, stroke, font, rotation)
	}

	private var state = State()
	private val stack = ArrayDeque<State>()

	private val defs = StringBuilder()
	private val body = StringBuilder()
	private var gradientCount = 0

	/** The transform of the open `<g>` element, consecutive elements with the same transform share one group. */
	private var openGroup: String? = null

	/** The path built by [beginPath], [moveTo], … */
	private val path = StringBuilder()
	private var currentPoint: Point2D? = null
	private var subpathStart: Point2D? = null

	/**
	 * Returns the recorded drawing as SVG document showing the area ([x], [y], [width], [height])
	 * in drawing coordinates, filled with [background] unless it is `null`.
	 */
	fun toSvg(x: Double, y: Double, width: Double, height: Double, background: Color?): String {
		closeGroup()
		val sb = StringBuilder()
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
		sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\"")
		sb.append(" width=\"${n(width)}\" height=\"${n(height)}\" viewBox=\"${n(x)} ${n(y)} ${n(width)} ${n(height)}\">\n")
		if (defs.isNotEmpty()) sb.append("<defs>\n").append(defs).append("</defs>\n")
		if (background != null) {
			sb.append("<rect x=\"${n(x)}\" y=\"${n(y)}\" width=\"${n(width)}\" height=\"${n(height)}\"${paintAttributes("fill", background)}/>\n")
		}
		sb.append(body)
		sb.append("</svg>\n")
		return sb.toString()
	}

	/** ---- Path rendering methods */

	override fun beginPath() {
		path.clear()
		currentPoint = null
		subpathStart = null
	}

	override fun moveTo(x: Double, y: Double) {
		path.append("M${n(x)} ${n(y)}")
		currentPoint = Point2D(x, y)
		subpathStart = currentPoint
	}

	override fun lineTo(x: Double, y: Double) {
		if (currentPoint == null) return moveTo(x, y)
		path.append("L${n(x)} ${n(y)}")
		currentPoint = Point2D(x, y)
	}

	override fun quadraticCurveTo(xc: Double, yc: Double, x1: Double, y1: Double) {
		if (currentPoint == null) moveTo(xc, yc)
		path.append("Q${n(xc)} ${n(yc)} ${n(x1)} ${n(y1)}")
		currentPoint = Point2D(x1, y1)
	}

	override fun cubicCurveTo(xc1: Double, yc1: Double, xc2: Double, yc2: Double, x1: Double, y1: Double) {
		if (currentPoint == null) moveTo(xc1, yc1)
		path.append("C${n(xc1)} ${n(yc1)} ${n(xc2)} ${n(yc2)} ${n(x1)} ${n(y1)}")
		currentPoint = Point2D(x1, y1)
	}

	/** Like the canvas' `arc`: connects the current point with the arc's start and draws it in segments. */
	override fun arc(x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean) {
		val full = 2 * PI
		val sweep = if (!anticlockwise) {
			if (endAngle - startAngle >= full) full else (endAngle - startAngle).mod(full)
		} else {
			if (startAngle - endAngle >= full) -full else -((startAngle - endAngle).mod(full))
		}
		val sx = x + radius * cos(startAngle)
		val sy = y + radius * sin(startAngle)
		if (currentPoint == null) moveTo(sx, sy) else lineTo(sx, sy)
		val segments = max(1, ceil(abs(sweep) / (PI / 2)).toInt())
		val r = n(radius)
		val flag = if (sweep > 0) 1 else 0
		for (i in 1..segments) {
			val a = startAngle + sweep * i / segments
			val ex = x + radius * cos(a)
			val ey = y + radius * sin(a)
			path.append("A$r $r 0 0 $flag ${n(ex)} ${n(ey)}")
			currentPoint = Point2D(ex, ey)
		}
	}

	override fun strokePath() {
		if (path.isNotEmpty()) emit("<path d=\"$path\" fill=\"none\"${strokeAttributes()}/>")
	}

	override fun fillPath() {
		if (path.isNotEmpty()) emit("<path d=\"$path\"${paintAttributes("fill", state.paint)}/>")
	}

	override fun closePath() {
		if (currentPoint == null) return
		path.append("Z")
		currentPoint = subpathStart
	}

	/** ---- [Graphics2D] interface */

	override val supportClipping: Boolean get() = false

	override var antialiasing: Boolean = true

	override fun setClipBounds(x: Int, y: Int, w: Int, h: Int) {}

	override fun setClipBounds(r: Rectangle2D?) {}

	override fun clip(x: Int, y: Int, w: Int, h: Int) {}

	override fun getClipBounds(): Rectangle2D? = null

	override fun getClipBounds(r: Rectangle2D): Rectangle2D = r

	override var transform: AffineTransform
		get() = state.matrix.let { AffineTransformImpl(it[0], it[1], it[2], it[3], it[4], it[5]) }
		set(value) {
			state.matrix = value.getMatrix().copyOf()
		}

	override var color: Color
		get() = state.paint as? Color ?: Color(0, 0, 0, 255)
		set(value) {
			state.paint = value
		}

	override var paint: Paint
		get() = state.paint
		set(value) {
			state.paint = value
		}

	override var stroke: Stroke
		get() = state.stroke
		set(value) {
			state.stroke = value
		}

	override var font: Font
		get() = state.font!!
		set(value) {
			state.font = value
		}

	override fun save() {
		stack.addLast(state.copy().also { it.rotation = rotationAngle })
	}

	override fun restore() {
		stack.removeLastOrNull()?.let {
			state = it
			rotationAngle = it.rotation
		}
	}

	override fun scale(sx: Double, sy: Double) = multiply(sx, 0.0, 0.0, sy, 0.0, 0.0)

	override fun translate(tx: Double, ty: Double) = multiply(1.0, 0.0, 0.0, 1.0, tx, ty)

	override fun rotate(theta: Double) {
		rotationAngle += theta
		multiply(cos(theta), sin(theta), -sin(theta), cos(theta), 0.0, 0.0)
	}

	override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) =
		drawLine(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())

	override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
		emit("<path d=\"M${n(x1)} ${n(y1)}L${n(x2)} ${n(y2)}\" fill=\"none\"${strokeAttributes()}/>")
	}

	override fun drawRect(x: Int, y: Int, w: Int, h: Int) = drawRect(x + 0.5, y + 0.5, w.toDouble(), h.toDouble())

	override fun drawRect(x: Double, y: Double, w: Double, h: Double) {
		emit("<rect x=\"${n(x)}\" y=\"${n(y)}\" width=\"${n(w)}\" height=\"${n(h)}\" fill=\"none\"${strokeAttributes()}/>")
	}

	override fun drawRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) =
		drawRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())

	override fun fillRect(x: Int, y: Int, w: Int, h: Int) = fillRect(x + 0.5, y + 0.5, w.toDouble(), h.toDouble())

	override fun fillRect(x: Double, y: Double, w: Double, h: Double) {
		emit("<rect x=\"${n(x)}\" y=\"${n(y)}\" width=\"${n(w)}\" height=\"${n(h)}\"${paintAttributes("fill", state.paint)}/>")
	}

	override fun fillRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int) =
		fillRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())

	override fun drawOval(x: Int, y: Int, w: Int, h: Int) = drawOval(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())

	override fun drawOval(x: Double, y: Double, w: Double, h: Double) {
		emit("<ellipse cx=\"${n(x + w / 2)}\" cy=\"${n(y + h / 2)}\" rx=\"${n(w / 2)}\" ry=\"${n(h / 2)}\" fill=\"none\"${strokeAttributes()}/>")
	}

	override fun fillOval(x: Int, y: Int, w: Int, h: Int) = fillOval(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())

	override fun fillOval(x: Double, y: Double, w: Double, h: Double) {
		emit("<ellipse cx=\"${n(x + w / 2)}\" cy=\"${n(y + h / 2)}\" rx=\"${n(w / 2)}\" ry=\"${n(h / 2)}\"${paintAttributes("fill", state.paint)}/>")
	}

	override fun drawPolygon(x: IntArray, y: IntArray, n: Int) {
		if (n > 0) emit("<polygon points=\"${points(x, y, n)}\" fill=\"none\"${strokeAttributes()}/>")
	}

	override fun fillPolygon(x: IntArray, y: IntArray, n: Int) {
		if (n > 0) emit("<polygon points=\"${points(x, y, n)}\"${paintAttributes("fill", state.paint)}/>")
	}

	override fun drawDot(x: Int, y: Int) = fillRect(x, y, 1, 1)

	override fun drawString(s: String, x: Int, y: Int) {
		if (s.isEmpty()) return
		val font = state.font?.let { " style=\"font: ${escape(Graphics2DJs.toJsFont(it).trim())}\"" } ?: ""
		emit("<text x=\"$x\" y=\"$y\" xml:space=\"preserve\"$font${paintAttributes("fill", state.paint)}>${escape(s)}</text>")
	}

	override fun drawImage(image: Image, x: Int, y: Int) {
		val url = (image as? EmbeddedImageJs)?.let { dataUrlOf(it) } ?: return
		emit("<image x=\"$x\" y=\"$y\" width=\"${image.width}\" height=\"${image.height}\" preserveAspectRatio=\"none\" xlink:href=\"${escape(url)}\" href=\"${escape(url)}\"/>")
	}

	override fun draw(shape: Shape) {
		when (shape) {
			is Path2DJs -> {
				beginPath()
				shape.play(pathRecorder())
				strokePath()
			}
			else -> super.draw(shape)
		}
	}

	override fun fill(shape: Shape) {
		when (shape) {
			is Path2DJs -> {
				beginPath()
				shape.play(pathRecorder())
				fillPath()
			}
			else -> super.fill(shape)
		}
	}

	/** ---- [SvgGraphics2D] */

	/** Concatenates the current transform with the matrix (a, b, c, d, e, f) in canvas order. */
	private fun multiply(a: Double, b: Double, c: Double, d: Double, e: Double, f: Double) {
		val m = state.matrix
		state.matrix = doubleArrayOf(
			m[0] * a + m[2] * b,
			m[1] * a + m[3] * b,
			m[0] * c + m[2] * d,
			m[1] * c + m[3] * d,
			m[0] * e + m[2] * f + m[4],
			m[1] * e + m[3] * f + m[5],
		)
	}

	/** Appends [element] to the body, inside a group with the current transform. */
	private fun emit(element: String) {
		val m = state.matrix
		val transform = if (m[0] == 1.0 && m[1] == 0.0 && m[2] == 0.0 && m[3] == 1.0 && m[4] == 0.0 && m[5] == 0.0) {
			""
		} else {
			"matrix(${m.joinToString(" ") { n(it, 6) }})"
		}
		if (transform != (openGroup ?: "")) {
			closeGroup()
			if (transform.isNotEmpty()) {
				body.append("<g transform=\"$transform\">\n")
				openGroup = transform
			}
		}
		body.append(element).append('\n')
	}

	private fun closeGroup() {
		if (openGroup != null) body.append("</g>\n")
		openGroup = null
	}

	private fun strokeAttributes(): String {
		val s = state.stroke
		val sb = StringBuilder(paintAttributes("stroke", state.paint))
		sb.append(" stroke-width=\"${n(s.width.toDouble())}\"")
		sb.append(" stroke-linecap=\"${s.cap.name.lowercase()}\"")
		sb.append(" stroke-linejoin=\"${s.join.name.lowercase()}\"")
		if (s.join == LineJoin.MITER) sb.append(" stroke-miterlimit=\"${n(s.miterLimit.toDouble())}\"")
		s.dash?.takeIf { it.isNotEmpty() }?.let { dash ->
			sb.append(" stroke-dasharray=\"${dash.joinToString(" ") { n(it.toDouble()) }}\"")
			s.dashPhase?.let { sb.append(" stroke-dashoffset=\"${n(it.toDouble())}\"") }
		}
		return sb.toString()
	}

	/** Returns ` fill="…"` (or stroke) with opacity for [paint], defining gradients as needed. */
	private fun paintAttributes(attribute: String, paint: Paint): String = when (paint) {
		is Color -> {
			val opacity = if (paint.alpha < 255) " $attribute-opacity=\"${n(paint.alpha / 255.0)}\"" else ""
			" $attribute=\"rgb(${paint.red},${paint.green},${paint.blue})\"$opacity"
		}
		is LinearColorGradient -> {
			val id = "gradient${++gradientCount}"
			defs.append("<linearGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" x1=\"${n(paint.p1.x)}\" y1=\"${n(paint.p1.y)}\" x2=\"${n(paint.p2.x)}\" y2=\"${n(paint.p2.y)}\">")
			defs.append(stop(0.0, paint.color1)).append(stop(1.0, paint.color2)).append("</linearGradient>\n")
			" $attribute=\"url(#$id)\""
		}
		is RadialColorGradient -> {
			val id = "gradient${++gradientCount}"
			defs.append("<radialGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" cx=\"${n(paint.center.x)}\" cy=\"${n(paint.center.y)}\" r=\"${paint.radius}\">")
			defs.append(stop(0.0, paint.centerColor)).append(stop(1.0, paint.perimeterColor)).append("</radialGradient>\n")
			" $attribute=\"url(#$id)\""
		}
		else -> " $attribute=\"black\""
	}

	private fun stop(offset: Double, color: Color) =
		"<stop offset=\"$offset\" stop-color=\"rgb(${color.red},${color.green},${color.blue})\" stop-opacity=\"${n(color.alpha / 255.0)}\"/>"

	private fun points(x: IntArray, y: IntArray, n: Int) = (0 until n).joinToString(" ") { "${x[it]},${y[it]}" }

	/** Lets a [Path2DJs] play its commands into this graphics' path. */
	private fun pathRecorder(): CanvasRenderingContext2D {
		val recorder: dynamic = js("({})")
		recorder.moveTo = { x: Double, y: Double -> moveTo(x, y) }
		recorder.lineTo = { x: Double, y: Double -> lineTo(x, y) }
		recorder.quadraticCurveTo = { xc: Double, yc: Double, x1: Double, y1: Double -> quadraticCurveTo(xc, yc, x1, y1) }
		recorder.bezierCurveTo = { a: Double, b: Double, c: Double, d: Double, e: Double, f: Double -> cubicCurveTo(a, b, c, d, e, f) }
		return recorder.unsafeCast<CanvasRenderingContext2D>()
	}

	/** Returns the image as `data:` URL, so the SVG file is self-contained. */
	private fun dataUrlOf(image: EmbeddedImageJs): String? {
		val src = image.image.src
		if (src.startsWith("data:")) return src
		if (!image.isLoaded) return null
		return try {
			val canvas = document.createElement("canvas") as HTMLCanvasElement
			canvas.width = image.width
			canvas.height = image.height
			(canvas.getContext("2d") as CanvasRenderingContext2D).drawImage(image.image, 0.0, 0.0, image.width.toDouble(), image.height.toDouble())
			canvas.toDataURL("image/png")
		} catch (_: Throwable) {
			null
		}
	}

	companion object {
		/** Formats [v] with at most [decimals] decimals and without exponent notation. */
		private fun n(v: Double, decimals: Int = 3): String {
			if (!v.isFinite()) return "0"
			val factor = 10.0.pow(decimals)
			val rounded = round(v * factor) / factor
			if (rounded == rounded.toLong().toDouble()) return rounded.toLong().toString()
			return rounded.asDynamic().toFixed(decimals).unsafeCast<String>().trimEnd('0').trimEnd('.')
		}

		private fun escape(s: String) = s
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
	}
}
