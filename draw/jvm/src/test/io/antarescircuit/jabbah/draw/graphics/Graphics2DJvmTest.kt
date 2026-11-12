package io.antarescircuit.jabbah.draw.graphics

import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertSame

class Graphics2DJvmTest {

	@Test
	fun shouldKeepSaveStacksSeparateBetweenInstances() {
		val awtA = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
		val awtB = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
		val a = Graphics2DJvm(awtA)
		val b = Graphics2DJvm(awtB)

		a.save()
		b.save()
		a.restore()
		b.restore()

		assertSame(awtA, a.g)
		assertSame(awtB, b.g)
	}
}
