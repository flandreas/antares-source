package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.Icon
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.Themes

interface DrawableButtonRenderer {
	val dimension: Dimension2D
	fun draw(button: DrawableButton<*>, context: DrawContext)
}

abstract class AbstractDrawableButtonRenderer : DrawableButtonRenderer {

	protected fun effectiveColor(button: DrawableButton<*>): CompositeColor {
		return if (button.isHovering) {
			Themes.get<DrawTheme>().hover
		} else if (!button.enabled) {
			button.buttonColor.withAlpha(128)
		} else {
			button.buttonColor
		}
	}
}

class IconDrawableButtonRenderer(val icon: Icon) : AbstractDrawableButtonRenderer() {

	override val dimension: Dimension2D get() = icon.dim

	override fun draw(button: DrawableButton<*>, context: DrawContext) {
		val color = if (context.useContextColors) {
			context.color!!
		} else {
			effectiveColor(button)
		}
		context.g.stroke = button.style.stroke
		icon.draw(context, Point2D(button.x, button.y), color)
	}
}