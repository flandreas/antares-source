package ch.scorpion.jabbah.draw.graphics

/**
 * Represents an image in a platform-transparent way.
 * Can be drawn using [Graphics2D.drawImage].
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
interface RasterImage : Image {

	fun setColor(x: Int, y: Int, color: Color)
}

typealias RasterImageFactory = (w: Int, h: Int) -> RasterImage