package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractIconDrawableButtonRenderer
import ch.scorpion.jabbah.draw.drawable.DrawableButton
import ch.scorpion.jabbah.draw.style.Style

class TextDrawableButtonRenderer(
	text: String,
	override val dimension: Dimension2D,
	private val style: Style
) : AbstractIconDrawableButtonRenderer() {

	private val label = Label(
		text = text,
		font = style.font,
		color = null
	)

	override fun draw(button: DrawableButton<*>, context: DrawContext) {
		establishColor(button, context)

		context.g.color = context.color!!.foregroundColor
		context.g.stroke = style.stroke
		context.g.draw(button.bounds)

		context.g.translate(button.bounds.center)
		label.color = context.color!!.textColor
		label.draw(context)
		context.g.translate(button.bounds.center.negate)
	}
}