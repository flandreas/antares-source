package io.antarescircuit.jabbah.draw.graphics

import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResourceImageJvmTest {

	@Test
	fun shouldLoadAndDrawAResourceImageWithoutSwing() {
		val image = ResourceImageJvm("/img/refresh.png")
		assertTrue(image.width > 0)
		assertTrue(image.height > 0)
		val target = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
		val graphics = target.createGraphics()
		try {
			Graphics2DJvm.drawImage(graphics, image, 0, 0)
		} finally {
			graphics.dispose()
		}
		for (y in 0 until image.height) {
			for (x in 0 until image.width) {
				assertEquals(image.jvmImage.getRGB(x, y), target.getRGB(x, y))
			}
		}
	}
}
