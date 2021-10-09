package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractIconDrawableButtonRenderer
import ch.scorpion.jabbah.draw.drawable.DrawableButton
import ch.scorpion.jabbah.draw.style.Style

class CharacterDrawableButtonRenderer(
	character: Char,
	size: Int,
	private val style: Style
) : AbstractIconDrawableButtonRenderer() {

	private val label = Label(
		text = character.toString(),
		font = style.font,
		color = null
	)

	override val dimension: Dimension2D = Dimension2D(size, size)

	override fun draw(button: DrawableButton<*>, context: DrawContext) {
		//val color = determineColor(button)
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