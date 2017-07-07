package ch.scorpion.jabbah.draw.graphics

enum class FontStyle(val value: Int) {
    PLAIN(0),
    BOLD(1),
    ITALIC(2)
}
/**
 * The [Font] class represents fonts, which are used to render text in a visible way.
 */
interface Font {
    val name: String
    val style: Int
    val size: Int

    fun isBold(): Boolean
    fun isItalic(): Boolean

    fun deriveFont(style: FontStyle): Font
}

data class FontImpl (
        override val name: String = "SansSerif",
        override val style: Int = FontStyle.PLAIN.value,
        override val size: Int = 12
) : Font {

    override fun isBold(): Boolean = style and FontStyle.BOLD.value != 0
    override fun isItalic(): Boolean = style and FontStyle.ITALIC.value != 0

    override fun deriveFont(style: FontStyle): Font {
        return copy(style = style.value)
    }
}