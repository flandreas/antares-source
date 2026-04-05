package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawableButtonRenderer
import io.antarescircuit.jabbah.draw.drawable.DrawableButton
import io.antarescircuit.jabbah.draw.style.Style

class TextDrawableButtonRenderer(
	text: String,
	override val dimension: Dimension2D,
	private val style: Style
) : AbstractDrawableButtonRenderer() {

	private val label = Label(
		text = text,
		font = style.font,
		color = null
	)

	override fun draw(button: DrawableButton<*>, context: DrawContext) {
		val color = if (context.useContextColors) {
			context.color!!
		} else {
			effectiveColor(button)
		}

		context.g.color = color.foregroundColor
		context.g.stroke = style.stroke
		context.g.draw(button.bounds)

		context.g.translate(button.bounds.center)
		label.color = color.textColor
		label.draw(context)
		context.g.translate(button.bounds.center.negate)
	}
}