package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component


/**
 * A [Component] with multiline text that can be interactively edited by the user.
 */
interface TextComponent : Component, RectangularShape {

	var text: TranslatableText

	var horizontalAlignment: HorizontalAlignment

	// Requested by the compiler due to multiple inheritance problem
	override fun contains(x: Double, y: Double): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	// Requested by the compiler due to multiple inheritance problem
	override fun contains(p: Point2D): Boolean {
		throw UnsupportedOperationException("not implemented")
	}
}

interface TextComponentFactory {

	/**
	 * Creates a new platform-specific [TextComponent] implementation.
	 * @param text the text to be displayed
	 * @param location the location of the text baseline point
	 */
	fun create(
		text: TranslatableText,
		location: Point2D,
		styleType: StyleType,
		styleProvider: StyleProvider
	): TextComponent
}
