package io.antarescircuit.jabbah.draw.graphics

import java.awt.image.BufferedImage.TYPE_INT_RGB

class RasterImageJvm(
	val jvmImage: java.awt.image.BufferedImage
) : RasterImage {

	constructor(width: Int, height: Int) : this(
		java.awt.image.BufferedImage(width, height, TYPE_INT_RGB)
	)

	override val width: Int get() = jvmImage.width

	override val height: Int get() = jvmImage.height

	override fun setColor(x: Int, y: Int, color: Color) {
		if (x < width && y < height) {
			jvmImage.setRGB(x, y, color.red.shl(16).or(color.green.shl(8).or(color.blue)))
		}
	}
}