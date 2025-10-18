package ch.scorpion.jabbah.draw.rasterimg

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Margin
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.MainContent
import ch.scorpion.jabbah.draw.drawable.Page
import ch.scorpion.jabbah.draw.drawable.PageOrientation
import ch.scorpion.jabbah.draw.drawable.PageSize
import ch.scorpion.jabbah.draw.drawable.Resolution
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
import kotlin.math.floor

object RasterImageExporter {

    const val IMAGE_INSET = 20
    private const val DEF_WIDTH = 800
    private const val DEF_HEIGHT = 800

    private val DEF_PAGE = Page(
        PageSize("any", Dimension2D(DEF_WIDTH, DEF_HEIGHT)),
        PageOrientation.LANDSCAPE,
        Margin.allOf(IMAGE_INSET)
    )

    fun exportToFile(
        content: MainContent,
        imageType: ImageType,
        path: String,
        page: Page = DEF_PAGE,
        resolution: Resolution? = null
    ) {
        saveToFile(drawToImage(content, page, resolution), imageType, path)
    }

    @Suppress("unused") // Used by Akrab
    fun exportToByteArray(
        content: MainContent,
        imageType: ImageType,
        page: Page = DEF_PAGE,
        resolution: Resolution? = null
    ) : ByteArray {
        return saveToByteArray(drawToImage(content, page, resolution), imageType)
    }

    private fun drawToImage(content: MainContent, page: Page, resolution: Resolution? = null): BufferedImage {
        require(content.drawable is DrawableContainer<*>)

        val widthInPixel = resolution?.millimeterToPixel(page.width) ?: page.width
        val heightInPixel = resolution?.millimeterToPixel(page.height) ?: page.height

        val usableWidthInPixel = resolution?.millimeterToPixel(floor(page.usableRectangle.width).toInt()) ?: page.width
        val usableHeightInPixel = resolution?.millimeterToPixel(floor(page.usableRectangle.height).toInt()) ?: page.height

        val bbox = content.drawable.boundingBox
        // If bbox is empty, quotients are infinite, and zoomFactor is 1
        val zoomFactor = if (resolution == null) {
            listOf(
                1.0,
                usableWidthInPixel / bbox.width,
                usableHeightInPixel / bbox.height
            ).min()
        } else {
            listOf(
                usableWidthInPixel / bbox.width,
                usableHeightInPixel / bbox.height
            ).min()
        }

        val dx = abs(widthInPixel - zoomFactor * bbox.width) / 2 / zoomFactor
        val dy = abs(heightInPixel - zoomFactor * bbox.height) / 2 / zoomFactor

        val image = BufferedImage(widthInPixel, heightInPixel, BufferedImage.TYPE_INT_RGB)

        val g2 = image.createGraphics()
        val context = DrawModule.drawContextFactory(Graphics2DJvm(g2), null, null)
        g2.setRenderingHint(KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw background
        g2.color = Graphics2DJvm.toAwtColor(content.background)
        g2.fillRect(0, 0, widthInPixel, heightInPixel)

        // Draw content
        g2.scale(zoomFactor, zoomFactor)
        g2.translate(-bbox.x + dx, -bbox.y + dy)
        content.drawable.drawStandalone(context)

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