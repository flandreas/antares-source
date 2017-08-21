package ch.scorpion.jabbah.draw.graphics

/**
 * Represents an image in a platform-transparent way.
 */
interface Image {

    /** The path from which the image data are loaded.*/
    val path: String

    /** The width of this [Image].*/
    val width: Int

    /** The height of this [Image].*/
    val height: Int
}

/** Loads the [Image] from the location with the specified path.*/
typealias ImageLoader = (String) -> Image