package io.antarescircuit.jabbah.graph.draw

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.graphics.*

/**
 * An [ImageLoader] implementation for the JS platform that loads images from the URL given as "path"
 * parameter in the "load" methods: a remote REST endpoint or a `data:` URL (used by the web editor,
 * which keeps project images in its file store).
 *
 * The browser downloads and decodes images asynchronously. [Graphics2DJs] requests
 * a canvas repaint when an [EmbeddedImageJs] becomes ready to draw.
 */
class ImageLoaderJs : ImageLoader {

	companion object {
		private val LOG by logger(ImageLoaderJs::class)
	}

	override fun loadSystemImage(path: String, type: ImageType): Image {
		throw UnsupportedOperationException("not implemented, not yet needed")
	}

	override fun loadUserImage(path: String, type: ImageType): Image =
		EmbeddedImageJs(path) {
			LOG.error("Could not load image $path")
		}
}
