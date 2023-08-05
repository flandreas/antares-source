package ch.scorpion.jabbah.draw.graphics

enum class FontStyle(val value: Int) {
	PLAIN(0),
	BOLD(1),
	ITALIC(2)
}

interface FontFamily {
	val fontName: String
}

class PhysicalFontFamily(override val fontName: String) : FontFamily

enum class LogicalFontFamily(val javaName: String, val jsName: String): FontFamily {
	SERIF("Serif", "serif"),
	SANS_SERIF("SansSerif", "sans-serif"),
	MONOSPACED("Monospaced", "monospace"),
	DIALOG("Dialog", "sans-serif");

	companion object {

		fun fromJavaName(name: String): FontFamily =
			values()
				.firstOrNull { name.startsWith(it.javaName, ignoreCase = true)}
				?: throw IllegalArgumentException("unknown Java FontFamily name $name")

		fun fromJsName(name: String): FontFamily =
			values()
				.firstOrNull { it.jsName == name }
				?: throw IllegalArgumentException("unknown JavaScript FontFamily name $name")
	}

	override val fontName: String get() = javaName
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
	fun deriveFont(size: Int): Font
	fun deriveFont(family: FontFamily): Font
	fun scale(factor: Int): Font
}

data class FontImpl(
	override val family: FontFamily = LogicalFontFamily.SANS_SERIF,
	override val style: Int = FontStyle.PLAIN.value,
	override val size: Int = 12
) : Font {

	override fun isBold(): Boolean = style and FontStyle.BOLD.value != 0
	override fun isItalic(): Boolean = style and FontStyle.ITALIC.value != 0

	override fun deriveFont(style: FontStyle): Font =
		if (style == FontStyle.PLAIN) {
			copy(style = FontStyle.PLAIN.value)
		} else {
			copy(style = this.style or style.value)
		}

	override fun deriveFont(size: Int): Font =
		copy(size = size)

	override fun deriveFont(family: FontFamily): Font =
		copy(family = family)

	override fun scale(factor: Int): Font =
		if (factor == 1) {
			this
		} else {
			deriveFont(size * factor)
		}
}