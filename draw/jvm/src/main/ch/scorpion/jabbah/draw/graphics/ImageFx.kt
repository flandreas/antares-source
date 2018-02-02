package ch.scorpion.jabbah.draw.graphics

class ImageFx(override val path: String) : Image {

    val image = javafx.scene.image.Image(path)

    override val width: Int get() = image.width.toInt()

    override val height: Int get() = image.height.toInt()
}