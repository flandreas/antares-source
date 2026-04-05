package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.Themes
import kotlin.math.ceil

/**
 * A [CompositeColor] is a defined set of harmonic colors to be used for drawing graphical objects.
 *
 * @param foregroundColor the [Color] for drawing the border of a graphical object
 * @param backgroundColor the [Color] for drawing the background of a graphical object
 * @param textColor the [Color] for drawing text above the interior of a graphical object
 */
data class CompositeColor(
	val foregroundColor: Color = Themes.get<DrawTheme>().figure.color.foregroundColor,
	val backgroundColor: Color = Themes.get<DrawTheme>().background.color.backgroundColor,
	val textColor: Color = foregroundColor
) {

	companion object {
		private const val BACKGROUND_DERIVATION_FACTOR = 1 / 12f
		private const val TEXT_DERIVATION_FACTOR = 0.6f

		fun withBrighterText(
			foregroundColor: Color,
			backgroundColor: Color
		): CompositeColor {
			return CompositeColor(foregroundColor, backgroundColor, foregroundColor.brighter().brighter())
		}

		fun withDarkerText(
			foregroundColor: Color,
			backgroundColor: Color
		): CompositeColor {
			return CompositeColor(foregroundColor, backgroundColor, foregroundColor.darker())
		}
	}

	val disabledTextColor: Color
		get() = Color(
			ceil((backgroundColor.red + textColor.red) / 2.0).toInt(),
			ceil((backgroundColor.green + textColor.green) / 2.0).toInt(),
			ceil((backgroundColor.blue + textColor.blue) / 2.0).toInt()
		)


	/**
	 * Creates a new [CompositeColor] by exchanging [foregroundColor] and [backgroundColor] of this [CompositeColor],
	 * and setting the [textColor] from the new [foregroundColor].
	 */
	fun exchange(): CompositeColor = CompositeColor(backgroundColor, foregroundColor, backgroundColor)

	/** Creates a new [CompositeColor] having the same color than this one, except the specified foreground color.*/
	fun withForeground(color: Color): CompositeColor = CompositeColor(color, backgroundColor, textColor)

	/** Creates a new [CompositeColor] having the same color than this one, except the specified background color.*/
	fun withBackground(color: Color): CompositeColor = CompositeColor(foregroundColor, color, textColor)

	/** Creates a new [CompositeColor] having the same color than this one, except the specified text color.*/
	fun withTextColor(color: Color): CompositeColor = CompositeColor(foregroundColor, backgroundColor, color)

	fun withAlpha(alpha: Int): CompositeColor = CompositeColor(foregroundColor.withAlpha(alpha), backgroundColor.withAlpha(alpha), textColor.withAlpha(alpha))

	fun withForegroundLikeBackground(): CompositeColor = CompositeColor(backgroundColor, backgroundColor, textColor)

	fun deriveBackgroundTowardsForegroundColor(): CompositeColor =
		CompositeColor(
			foregroundColor = foregroundColor,
			backgroundColor = backgroundColor.between(foregroundColor, BACKGROUND_DERIVATION_FACTOR),
			textColor = textColor)

	fun deriveBackgroundTowardsTextColor(): CompositeColor =
		CompositeColor(
			foregroundColor = foregroundColor,
			backgroundColor = backgroundColor.between(textColor, BACKGROUND_DERIVATION_FACTOR),
			textColor = textColor)

	fun deriveTextTowardsBackgroundColor(): CompositeColor =
		CompositeColor(
			foregroundColor = foregroundColor,
			backgroundColor = backgroundColor,
			textColor = textColor.between(backgroundColor, TEXT_DERIVATION_FACTOR)
		)

	fun darker(): CompositeColor = CompositeColor(
		foregroundColor.darker(), backgroundColor.darker(), textColor.darker()
	)
}