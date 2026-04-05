package io.antarescircuit.jabbah.draw.graphics

import org.w3c.dom.Image

/**
 * Contains a JS Image object using the "data: URL" pattern containing the image data.
 * @param data the base64-encoded image data.
 */
class EmbeddedImageJs(
    type: ImageType,
    data: String
) : io.antarescircuit.jabbah.draw.graphics.Image {

    val image: Image = Image()

    init {
        image.src = "data:${type.mimeType};base64,$data"
    }

    override val width: Int get() = image.width

    override val height: Int get() = image.height
}