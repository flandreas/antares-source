package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes

interface DrawableButtonRenderer {
	val dimension: Dimension2D
	fun draw(button: DrawableButton<*>, context: DrawContext)
}

abstract class AbstractIconDrawableButtonRenderer : DrawableButtonRenderer {

	/** Establishes in [DrawContext.color] the color for drawing [button] depending on its state (enabled, hovering). */
	fun establishColor(button: DrawableButton<*>, context: DrawContext) {
		if (button.isHovering) {
			context.color = Themes.get<DrawTheme>().hover
		} else if (!button.enabled) {
			context.color = button.buttonColor.withAlpha(128)
		} else {
			context.color = button.buttonColor
		}
	}
}

class IconDrawableButtonRenderer(val icon: Icon) : AbstractIconDrawableButtonRenderer() {

	override val dimension: Dimension2D get() = icon.dim

	override fun draw(button: DrawableButton<*>, context: DrawContext) {
		val oldUseContextColors = context.useContextColors
		context.useContextColors = true

		establishColor(button, context)

		context.g.stroke = button.style.stroke
		icon.draw(context, Point2D(button.x, button.y))

		context.useContextColors = oldUseContextColors
	}
}