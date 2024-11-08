package ch.scorpion.jabbah.draw.graphics

enum class ImageType(
    val customName: String,
    val fileExtension: String,
    val mimeType: String,
    val isRaster: Boolean
) {
    SVG("SVG", "svg", "image/svg+xml", false),
    JPG("JPG", "jpg", "image/jpeg",true),
    GIF("GIF", "gif", "image/gif", true),
    PNG("PNG", "png", "image/png", true);

    companion object {

        val allFileExtensions: Set<String> get() = entries.map { it.fileExtension }.toSet()

        val allFileExtensionsDesc: String get() = entries.joinToString(", ") { "*.${it.fileExtension}" }

        fun withName(customName: String): ImageType =
            entries.firstOrNull { it.customName == customName } ?:
                throw IllegalArgumentException("Unknown ImageType '$customName'")

        fun withExtension(extension: String): ImageType? =
            entries.firstOrNull { it.fileExtension == extension.lowercase() }

        fun withMimeType(mimeType: String): ImageType? =
            entries.firstOrNull { it.mimeType == mimeType }
    }

    override fun toString(): String = customName
}