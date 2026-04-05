package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.logger
import javax.swing.ImageIcon

/**
 * An [Image] implementation whose data is loaded from resources.
 * @param path the path from which the image data are loaded
 */
class ResourceImageJvm(val path: String) : Image {

    companion object {
        private val LOG by logger(ResourceImageJvm::class)
    }

    val imageIcon: ImageIcon

    init {
        val resource = ResourceImageJvm::class.java.getResource(path)
        if (resource == null) {
            LOG.error("Image '$path' not found")
            throw IllegalArgumentException("Image '$path' not found")
        }
        imageIcon = ImageIcon(resource)
    }

    override val width: Int get() = imageIcon.iconWidth

    override val height: Int get() = imageIcon.iconHeight
}
