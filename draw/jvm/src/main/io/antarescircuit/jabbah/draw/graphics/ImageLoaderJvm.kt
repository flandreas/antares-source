package io.antarescircuit.jabbah.draw.graphics

import java.nio.file.Paths
import javax.imageio.ImageIO

/**
 * Loads raster images from JVM resources and the local file system.
 * UI implementations can add vector image loading.
 */
open class ImageLoaderJvm : ImageLoader {

    override fun loadSystemImage(path: String, type: ImageType): Image {
        if (type.isRaster) {
            return ResourceImageJvm(path)
        } else {
            throw IllegalArgumentException("non-raster system image not yet supported")
        }
    }

    override fun loadUserImage(path: String, type: ImageType): Image =
        if (type.isRaster) {
            RasterImageJvm(ImageIO.read(Paths.get(path).toFile()))
        } else {
            loadVectorImage(path)
        }

    protected open fun loadVectorImage(path: String): Image =
        throw UnsupportedOperationException("Vector image loading requires draw-ui initialization")
}
