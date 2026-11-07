package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.logger
import javax.imageio.ImageIO

/**
 * An [Image] implementation whose data is loaded from resources.
 * @param path the path from which the image data are loaded
 */
class ResourceImageJvm(val path: String) : Image {

    companion object {
        private val LOG by logger(ResourceImageJvm::class)
    }

    val jvmImage: java.awt.image.BufferedImage

    init {
        val resource = ResourceImageJvm::class.java.getResource(path)
        if (resource == null) {
            LOG.error("Image '$path' not found")
            throw IllegalArgumentException("Image '$path' not found")
        }
        jvmImage = ImageIO.read(resource) ?: throw IllegalArgumentException("Unsupported image '$path'")
    }

    override val width: Int get() = jvmImage.width

    override val height: Int get() = jvmImage.height
}
