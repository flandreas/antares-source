package io.antarescircuit.jabbah.graph.draw

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.graphics.*
import io.antarescircuit.jabbah.graph.project.AkrabApiError
import io.antarescircuit.jabbah.graph.project.AkrabApiException
import org.w3c.xhr.XMLHttpRequest

/**
 * An [ImageLoader] implementation for the JS platform that loads images from
 * remote REST endpoints identified by the "path" parameter in the "load" methods.
 *
 * The "load" methods return a [EmbeddedImageJs] implementation that can be drawn by
 * [Graphics2DJs.drawImage].
 */
class ImageLoaderJs : ImageLoader {

    companion object {
        private val LOG by logger(ImageLoaderJs::class)
    }

    override fun loadSystemImage(path: String, type: ImageType): Image {
        throw UnsupportedOperationException("not implemented, not yet needed")
    }

    override fun loadUserImage(path: String, type: ImageType): Image {
        try {
            val request = XMLHttpRequest()
            request.open("GET", path, async = false)
            request.send()

            if (request.status != 200.toShort()) {
                LOG.error("Error in loadUserImage")
                throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load image $path"))
            }

            val imageType = request.getResponseHeader("content-type")
                ?.let { ImageType.withMimeType(it) }
                ?: throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Missing or invalid content-type header in image response"))

            return EmbeddedImageJs(imageType, request.responseText)
        } catch (e: Throwable) {
            LOG.error("Error in loading user image: ${e.message}", e)
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Could not load image $path: ${e.message}"))
        }
    }
}