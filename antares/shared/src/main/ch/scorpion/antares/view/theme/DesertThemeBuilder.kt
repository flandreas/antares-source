package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColor
import ch.scorpion.jabbah.draw.style.DrawTheme

object DesertThemeBuilder : AbstractAntaresLightThemeBuilder("Desert") {

	private val DESERT_YELLOW = CompositeColor(
		foregroundColor = Color(160, 132, 118),
		backgroundColor = Color(246, 239, 210)
	)

	private val BACKGROUND_COLOR = CompositeColor(
		foregroundColor = Color(249, 244, 223),
		backgroundColor = Color(255, 255, 253),
		textColor = Color(67, 56, 28)
	)

	override fun build(): AntaresTheme {
		val refColors = DrawTheme.DEF_REF_COLORS.toMutableList()
		val yellow = refColors.removeAt(0)
		refColors.add(yellow)

		return standardForColor(DESERT_YELLOW, BACKGROUND_COLOR, referenceColors = referenceColorsWithLowPrioYellow())
	}

	private fun referenceColorsWithLowPrioYellow(): List<ReferenceColor> {
		val refColors = DrawTheme.DEF_REF_COLORS.toMutableList()
		val yellow = refColors.removeAt(0)
		refColors.add(yellow)
		return refColors
	}
}