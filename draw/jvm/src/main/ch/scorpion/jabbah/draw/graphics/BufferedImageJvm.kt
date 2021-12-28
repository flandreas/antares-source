package ch.scorpion.jabbah.draw.graphics

import java.awt.image.BufferedImage.TYPE_INT_RGB

class BufferedImageJvm(
	override val width: Int,
	override val height: Int
) : BufferedImage {

	val jvmImage = java.awt.image.BufferedImage(width, height, TYPE_INT_RGB)

	override fun setColor(x: Int, y: Int, color: Color) {
		if (x < width && y < height) {
			jvmImage.setRGB(x, y, color.red.shl(16).or(color.green.shl(8).or(color.blue)))
		}
	}
}