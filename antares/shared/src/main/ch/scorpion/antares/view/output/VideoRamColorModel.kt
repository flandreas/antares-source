package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.graphics.Color

enum class VideoRamColorModel(
	override val customName: String,
	val dataBitWidth: BitWidth,
	private val palette: Array<Color>
) : EnumProperty<VideoRamColorModel> {

	MONOCHROME("monochrome", BitWidth.BW_1, arrayOf(
		Color.BLACK,
		Color.WHITE
	)),

	CGA_16("cga16", BitWidth.BW_4, arrayOf(
		Color.BLACK,
		Color(0, 0, 170), // blue
		Color(0, 170, 0), // green
		Color(0, 170, 170), // cyan
		Color(170, 0, 0), // red
		Color(170, 0, 170), // magenta
		Color(170, 85, 0), // brown
		Color(170, 170, 170), // light gray
		Color(85, 85, 85), // dark gray
		Color(85, 85, 255), // light blue
		Color(85, 255, 85), // light green
		Color(85, 255, 255), // light cyan
		Color(255, 85, 85), // light red
		Color(255, 85, 255), // light magenta
		Color(255, 255, 85), // yellow
		Color(255, 255, 255), // white
	));

	companion object {
		const val BASE_KEY = "element.property.videoRamColorModel"

		fun withName(customName: String): VideoRamColorModel =
			VideoRamColorModel.entries.firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("Unknown PullDirection '$customName'")
	}

	override fun toString(): String =
		when (this) {
			CGA_16 -> Translations.getString("$BASE_KEY.cga16")
            MONOCHROME -> Translations.getString("$BASE_KEY.monochrome")
        }

	fun getColor(index: Int): Color =
		if (index >= palette.size) {
			palette[0]
		} else {
			palette[index]
		}
}