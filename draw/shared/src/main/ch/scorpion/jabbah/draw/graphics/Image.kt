package ch.scorpion.jabbah.draw.graphics

/**
 * Represents an image in a platform-transparent way.
 */
interface Image {

    /** The width of this [Image].*/
    val width: Int

    /** The height of this [Image].*/
    val height: Int
}

/**
 * An [Image] whose contents can be set using [setColor].
 */
interface BufferedImage : Image {

	fun setColor(x: Int, y: Int, color: Color)
}

/** Loads the [Image] from the location with the specified path.*/
typealias ImageLoader = (String) -> Image

typealias BufferedImageFactory = (w: Int, h: Int) -> BufferedImage