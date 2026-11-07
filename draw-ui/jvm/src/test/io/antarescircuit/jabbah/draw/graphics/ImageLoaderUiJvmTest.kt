package io.antarescircuit.jabbah.draw.graphics

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ImageLoaderUiJvmTest {

	@Test
	fun shouldLoadSvgAndDrawItThroughCoreGraphics() {
		val file = Files.createTempFile("jabbah-vector-", ".svg").toFile()
		try {
			file.writeText("""
				<svg xmlns="http://www.w3.org/2000/svg" width="12" height="8">
					<rect width="12" height="8" fill="#ff0000"/>
				</svg>
			""".trimIndent())
			val image = ImageLoaderUiJvm().loadUserImage(file.path, ImageType.SVG)
			assertIs<SvgImageJvm>(image)
			assertEquals(12, image.width)
			assertEquals(8, image.height)
			val target = BufferedImage(12, 8, BufferedImage.TYPE_INT_ARGB)
			val graphics = target.createGraphics()
			try {
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
				Graphics2DJvm.drawImage(graphics, image, 0, 0)
				assertEquals(java.awt.Color.RED.rgb, target.getRGB(6, 4))
				assertEquals(RenderingHints.VALUE_ANTIALIAS_OFF, graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING))
			} finally {
				graphics.dispose()
			}
		} finally {
			file.delete()
		}
	}
}
