package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A [Style] is a named set of graphical properties used for drawing graphical objects.
 */
interface Style {
	val color: CompositeColor
	val stroke: Stroke
	val font: Font
	val shadow: Boolean

	companion object {

		/** The name of the default foreground [Color] in [Properties].*/
		const val PROP_FOREGROUND_COLOR = "draw.style.foregroundColor"

		/** The name of the default background [Color] in [Properties].*/
		const val PROP_BACKGROUND_COLOR = "draw.style.backgroundColor"

		/** The name of the default text [Color] in [Properties].*/
		const val PROP_TEXT_COLOR = "draw.style.textColor"

		/** The name of the default [Stroke] property in [Properties].*/
		const val PROP_STROKE = "draw.style.stroke"

		/** The name of the default [Font] property in [Properties].*/
		const val PROP_FONT = "draw.style.font"

		/** The name of the default shadow [Boolean] property in [Properties].*/
		const val PROP_SHADOW = "draw.style.shadow"
	}
}

open class BasicStyle(
	override val color: CompositeColor = CompositeColor(
		DrawModule.properties.getColor(Style.PROP_FOREGROUND_COLOR),
		DrawModule.properties.getColor(Style.PROP_BACKGROUND_COLOR),
		DrawModule.properties.getColor(Style.PROP_TEXT_COLOR)),
	override val stroke: Stroke = DrawModule.properties.getStroke(Style.PROP_STROKE),
	override val font: Font = DrawModule.properties.getFont(Style.PROP_FONT),
	shadow: Boolean = false
) : Style {

	override val shadow: Boolean = shadow && DrawModule.properties.getBoolean(Style.PROP_SHADOW)
}