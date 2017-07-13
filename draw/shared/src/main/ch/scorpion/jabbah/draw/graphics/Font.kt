package ch.scorpion.jabbah.draw.graphics

enum class FontStyle(val value: Int) {
    PLAIN(0),
    BOLD(1),
    ITALIC(2)
}

enum class FontFamily(val javaName: String, val jsName: String) {
    SERIF("Serif", "serif"),
    SANS_SERIF("SansSerif", "sans-serif"),
    MONOSPACED("Monospaced", "monospace"),
    DIALOG("Dialog", "sans-serif");

    companion object {
        fun fromJavaName(name: String): FontFamily {
            FontFamily.values()
                    .filter { it.javaName == name }
                    .forEach { return it }
            throw IllegalArgumentException("unknown Java FontFamily name $name")
        }

        fun fromJsName(name: String): FontFamily {
            FontFamily.values()
                    .filter { it.jsName == name }
                    .forEach { return it }
            throw IllegalArgumentException("unknown JavaScript FontFamily name $name")
        }
    }
}

/**
 * The [Font] class represents fonts, which are used to render text in a visible way.
 */
interface Font {
    val family: FontFamily
    val style: Int
    val size: Int

    fun isBold(): Boolean
    fun isItalic(): Boolean

    fun deriveFont(style: FontStyle): Font
}

data class FontImpl (
        override val family: FontFamily = FontFamily.SANS_SERIF,
        override val style: Int = FontStyle.PLAIN.value,
        override val size: Int = 12
) : Font {

    override fun isBold(): Boolean = style and FontStyle.BOLD.value != 0
    override fun isItalic(): Boolean = style and FontStyle.ITALIC.value != 0

    override fun deriveFont(style: FontStyle): Font {
        return copy(style = style.value)
    }
}