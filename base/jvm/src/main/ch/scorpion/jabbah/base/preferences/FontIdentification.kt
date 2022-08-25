package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.Font

/**
 * An identification of a font, either a logical font or a physical font.
 *
 * Differs from [ch.scorpion.jabbah.draw.graphics.Font] insofar that that can only represent all
 * logical fonts available on the JVM platform.
 *
 * If any of the properties is blank or zero, this [FontIdentification] identifies the "default" system font,
 * which is mapped by the runtime platform to its default physical font
 */
class FontIdentification(
	val fontName: String,
	val style: Int,
	val size: Int
) {
	constructor(): this("", Font.PLAIN, 0)

	companion object {
		const val PROP_FONT_IDENTIFICATION = "base.preferences.font"
		val DEFAULT_VALUE = FontIdentification()

		private const val DELIMITER = ';'

		fun load(properties: Properties = BaseModule.properties): FontIdentification =
			parse(properties.getString(PROP_FONT_IDENTIFICATION))

		fun parse(s: String): FontIdentification {
			val elems = s.split(DELIMITER)
			if (elems.size < 3) {
				return FontIdentification()
			} else {
				val fontName = elems[0]
				return if (StringUtils.isBlank(fontName)) {
					FontIdentification()
				} else {
					try {
						val style = elems[1].toInt()
						val size = elems[2].toInt()
						FontIdentification(fontName, style, size)
					} catch (e: Throwable) {
						FontIdentification()
					}
				}
			}
		}
	}

	val isDefault: Boolean get() = StringUtils.isBlank(fontName) || size == 0

	fun externalize(): String =
		if (isDefault) {
			""
		} else {
			"$fontName$DELIMITER$style$DELIMITER$size"
		}

	/** ---- [Any] */

	override fun toString(): String =
		if (isDefault) {
			Translations.getString("base.font.default")
		} else {
			"$fontName, $size"
		}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as FontIdentification

		if (fontName != other.fontName) return false
		if (style != other.style) return false
		if (size != other.size) return false

		return true
	}

	override fun hashCode(): Int {
		var result = fontName.hashCode()
		result = 31 * result + style
		result = 31 * result + size
		return result
	}
}