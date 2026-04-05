package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component


/**
 * A [Component] with multiline text that can be interactively edited by the user.
 */
interface TextComponent : Component, RectangularShape {

	var text: Translatable

	var horizontalAlignment: HorizontalAlignment

	// Requested by the compiler due to multiple inheritance problem
	override fun contains(x: Double, y: Double): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	// Requested by the compiler due to multiple inheritance problem
	override fun contains(p: Point2D): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	// Requested by the compiler due to multiple inheritance problem
	override fun intersects(rect: RectangularShape): Boolean {
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
		location: Point2D = Point2D.ZERO,
		styleType: StyleType = StyleType.FIGURE,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider
	): TextComponent
}

class UndefinedTextComponentFactory : TextComponentFactory {
	override fun create(text: TranslatableText, location: Point2D, styleType: StyleType, styleProvider: StyleProvider): TextComponent {
		throw UnsupportedOperationException("not implemented")
	}
}
