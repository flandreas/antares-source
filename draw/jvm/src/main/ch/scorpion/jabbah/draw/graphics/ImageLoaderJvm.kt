package ch.scorpion.jabbah.draw.graphics

import com.github.weisj.jsvg.parser.SVGLoader
import java.io.FileInputStream
import java.nio.file.Paths
import javax.imageio.ImageIO

/**
 * An [ImageLoader] implementation for the JVM platform that loads system [Image]s
 * from a JAR resource, and used [Image] from the local file system.
 */
class ImageLoaderJvm : ImageLoader {

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
            loadSVGImage(path)
        }

    private fun loadSVGImage(path: String): Image {
        val loader = SVGLoader()
        FileInputStream(path).use { fis ->
            val document = loader.load(fis)
            return SvgImageJvm(document!!)
        }
    }
}