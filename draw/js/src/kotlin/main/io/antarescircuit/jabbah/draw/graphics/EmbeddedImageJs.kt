package io.antarescircuit.jabbah.draw.graphics

import org.w3c.dom.Image

/**
 * A browser image loaded directly from a binary image endpoint.
 * [whenLoaded] lets canvases repaint once the asynchronous download and decoding finish.
 */
class EmbeddedImageJs(
	path: String,
	onError: () -> Unit = {},
) : io.antarescircuit.jabbah.draw.graphics.Image {

	val image: Image = Image()

	private val loadListeners = mutableSetOf<() -> Unit>()
	private var failed = false

	val isLoaded: Boolean get() = image.complete && image.naturalWidth > 0

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
	override val width: Int get() = image.naturalWidth.coerceAtLeast(1)

	override val height: Int get() = image.naturalHeight.coerceAtLeast(1)
}
