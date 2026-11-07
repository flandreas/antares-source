package io.antarescircuit.jabbah.draw.graphics

import java.awt.image.BufferedImage
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ImageLoaderJvmTest {

	@Test
	fun shouldLoadAndDrawRasterImagesWithoutUi() {
		val file = Files.createTempFile("jabbah-raster-", ".png").toFile()
		try {
			val source = BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB)
			source.setRGB(1, 1, java.awt.Color.RED.rgb)
			ImageIO.write(source, "png", file)
			val image = ImageLoaderJvm().loadUserImage(file.path, ImageType.PNG)
			assertIs<RasterImageJvm>(image)
			assertEquals(3, image.width)
			assertEquals(2, image.height)
			val target = BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB)
			val graphics = target.createGraphics()
			try {
				Graphics2DJvm.drawImage(graphics, image, 0, 0)
				assertEquals(source.getRGB(1, 1), target.getRGB(1, 1))
			} finally {
				graphics.dispose()
			}
		} finally {
			file.delete()
		}
	}

	@Test
	fun shouldRejectVectorImagesWithoutUi() {
		assertFailsWith<UnsupportedOperationException> {
			ImageLoaderJvm().loadUserImage("image.svg", ImageType.SVG)
		}
	}
}
