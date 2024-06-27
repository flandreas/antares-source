package ch.scorpion.jabbah.draw.rasterimg

import ch.scorpion.jabbah.draw.MainContent
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.ImageType
import ch.scorpion.jabbah.draw.module.DrawModule
import java.awt.RenderingHints
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.abs

object RasterImageExporter {

    private const val IMAGE_INSET = 20
    private const val DEF_WIDTH = 800
    private const val DEF_HEIGHT = 800

    fun exportToFile(
        content: MainContent,
        imageType: ImageType,
        path: String,
        width: Int = DEF_WIDTH,
        height: Int = DEF_HEIGHT,
        inset: Int = IMAGE_INSET
    ) {
        saveToFile(drawToImage(content, width, height, inset), imageType, path)
    }

    fun exportToByteArray(
        content: MainContent,
        imageType: ImageType,
        width: Int = DEF_WIDTH,
        height: Int = DEF_HEIGHT,
        inset: Int = IMAGE_INSET
    ) : ByteArray {
        return saveToByteArray(drawToImage(content, width, height, inset), imageType)
    }

    private fun drawToImage(content: MainContent, width: Int, height: Int, inset: Int): BufferedImage {
        val bbox = content.drawable.boundingBox
        // If bbox is empty, quotients are infinite, and zoomFactor is 1
        val zoomFactor = listOf(
            1.0,
            (width - 2 * inset) / bbox.width,
            (height - 2 * inset) / bbox.height
        ).min()

        val dx = abs(width - zoomFactor * bbox.width) / 2 / zoomFactor
        val dy = abs(height - zoomFactor * bbox.height) / 2 / zoomFactor

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2 = image.createGraphics()
        val context = DrawModule.drawContextFactory(Graphics2DJvm(g2), null, null)
        g2.setRenderingHint(KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw background
        g2.color = Graphics2DJvm.toAwtColor(content.background)
        g2.fillRect(0, 0, width, height)

        // Draw content
        g2.scale(zoomFactor, zoomFactor)
        g2.translate(-bbox.x + dx, -bbox.y + dy)
        content.drawable.draw(context)

        g2.dispose()

        return image
    }

    private fun saveToFile(image: BufferedImage, imageType: ImageType, path: String) {
        ImageIO.write(
            image,
            imageType.customName.lowercase(),
            Path.of(path).toFile()
        )
    }

    private fun saveToByteArray(image: BufferedImage, imageType: ImageType): ByteArray {
        return ByteArrayOutputStream().use {
            ImageIO.write(
                image,
                imageType.customName.lowercase(),
                it
            )
            it
        }.toByteArray()
    }
}