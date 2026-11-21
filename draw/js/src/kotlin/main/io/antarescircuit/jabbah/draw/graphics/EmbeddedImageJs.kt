package io.antarescircuit.jabbah.draw.graphics

import kotlinx.browser.window
import org.w3c.dom.Image

/**
 * A browser image loaded from a URL or a `data:` URL.
 * [whenLoaded] lets canvases repaint once the asynchronous download and decoding finish.
 *
 * For `data:` URLs the size is read from the image data right away, so components get their
 * final size before the browser has decoded the image (and SVGs without intrinsic size get one).
 */
class EmbeddedImageJs(
	path: String,
	onError: () -> Unit = {},
) : io.antarescircuit.jabbah.draw.graphics.Image {

	val image: Image = Image()

	private val loadListeners = mutableSetOf<() -> Unit>()
	private var failed = false
	private var knownSize: Pair<Int, Int>? = null

	// SVGs without intrinsic size report a natural width of 0 in some browsers
	val isLoaded: Boolean get() = image.complete && !failed && (image.naturalWidth > 0 || knownSize != null)

	init {
		// Preserve CORS checks and allow drawing cross-origin images to an exportable canvas.
		image.crossOrigin = "anonymous"
		image.addEventListener("load", {
			val listeners = loadListeners.toList()
			loadListeners.clear()
			listeners.forEach { it() }
		})
		image.addEventListener("error", {
			failed = true
			loadListeners.clear()
			onError()
		})
		load(path)
	}

	/** Replaces the displayed image, e.g. after the user replaced an image of a project. */
	fun load(path: String) {
		failed = false
		knownSize = if (path.startsWith("data:")) ImageSizes.ofDataUrl(path) else null
		image.src = path
	}

	fun whenLoaded(listener: () -> Unit) {
		if (isLoaded) {
			listener()
		} else if (!failed) {
			loadListeners.add(listener)
		}
	}

	// ImageComponent computes scale factors before drawing; keep them finite while loading.
	override val width: Int get() = knownSize?.first ?: image.naturalWidth.coerceAtLeast(1)

	override val height: Int get() = knownSize?.second ?: image.naturalHeight.coerceAtLeast(1)
}

/** Reads the pixel size of PNG, JPEG, GIF and SVG images from `data:` URLs without decoding them. */
internal object ImageSizes {

	private const val DEFAULT_SVG_SIZE = 100

	fun ofDataUrl(url: String): Pair<Int, Int>? {
		val comma = url.indexOf(',')
		if (comma < 0) return null
		val header = url.substring(5, comma)
		val payload = url.substring(comma + 1)
		return try {
			val data = if (header.endsWith(";base64")) {
				// The header of an image is enough; JPEG headers can follow larger metadata blocks
				window.atob(payload.take(256 * 1024).let { it.take(it.length / 4 * 4) })
			} else {
				js("decodeURIComponent")(payload) as String
			}
			when {
				header.startsWith("image/png") -> png(data)
				header.startsWith("image/gif") -> gif(data)
				header.startsWith("image/jpeg") -> jpeg(data)
				header.startsWith("image/svg") -> svg(data)
				else -> null
			}
		} catch (_: Throwable) {
			null
		}
	}

	private fun String.byte(i: Int): Int = this[i].code and 0xff

	private fun String.bigEndian(i: Int, n: Int): Int = (0 until n).fold(0) { acc, k -> (acc shl 8) or byte(i + k) }

	private fun png(d: String): Pair<Int, Int>? =
		if (d.length >= 24 && d.substring(12, 16) == "IHDR") d.bigEndian(16, 4) to d.bigEndian(20, 4) else null

	private fun gif(d: String): Pair<Int, Int>? =
		if (d.length >= 10 && d.startsWith("GIF")) (d.byte(6) or (d.byte(7) shl 8)) to (d.byte(8) or (d.byte(9) shl 8)) else null

	/** Finds the first start-of-frame segment (SOF0..SOF15 except DHT, JPG and DAC markers). */
	private fun jpeg(d: String): Pair<Int, Int>? {
		var i = 2
		while (i + 9 < d.length) {
			if (d.byte(i) != 0xff) return null
			val marker = d.byte(i + 1)
			if (marker in 0xc0..0xcf && marker != 0xc4 && marker != 0xc8 && marker != 0xcc) {
				return d.bigEndian(i + 7, 2) to d.bigEndian(i + 5, 2)
			}
			i += 2 + d.bigEndian(i + 2, 2)
		}
		return null
	}

	/** Uses `width`/`height` of the root element, falling back to its `viewBox`. */
	private fun svg(d: String): Pair<Int, Int> {
		val tag = Regex("<svg\\b[^>]*>", RegexOption.IGNORE_CASE).find(d)?.value ?: ""
		fun attribute(name: String) = Regex("\\s$name\\s*=\\s*[\"']([^\"']*)[\"']").find(tag)?.groupValues?.get(1)
		fun length(name: String) = attribute(name)?.trim()?.let { Regex("^([0-9.]+)(px)?$").find(it) }
			?.groupValues?.get(1)?.toDoubleOrNull()
		val viewBox = attribute("viewBox")?.trim()?.split(Regex("[\\s,]+"))?.mapNotNull { it.toDoubleOrNull() }
			?.takeIf { it.size == 4 && it[2] > 0 && it[3] > 0 }
		val w = length("width")
		val h = length("height")
		return when {
			w != null && h != null -> w to h
			viewBox != null && w != null -> w to w * viewBox[3] / viewBox[2]
			viewBox != null && h != null -> h * viewBox[2] / viewBox[3] to h
			viewBox != null -> viewBox[2] to viewBox[3]
			else -> DEFAULT_SVG_SIZE.toDouble() to DEFAULT_SVG_SIZE.toDouble()
		}.let { (a, b) -> a.toInt().coerceAtLeast(1) to b.toInt().coerceAtLeast(1) }
	}
}
