package ch.scorpion.antares.view.arithmetic

import ch.scorpion.antares.model.arithmetic.Random
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/** A view representation of a [Random].*/
class RandomView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Random = Random()
) : BoxGateView<Random>(styleProvider, "", model) {

	companion object {
		private const val DICE_SIZE = 18
		private const val DOT_SIZE_HALF = 2
	}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: Random?) {
		super.modelExchanged(oldModel)
		addPortView(createInputPortView(model.getInput()))
		addPortView(createOutputPortView(model.getOutput()))
		updateLayout()
	}

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				model.bitWidth = value
			}
		}

	/** [BoxGateView] */

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color
		super.drawImpl(context)

		context.g.color = getApplicableForegroundColor(context)
		context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke

		context.g.translate(-(AbstractAntaresPortView.LENGTH + bounds.width / 2 + DICE_SIZE / 2), - bounds.height / 3)
		drawDice(context)
		context.g.translate(+(AbstractAntaresPortView.LENGTH + bounds.width / 2 + DICE_SIZE / 2), + bounds.height / 3)

		context.g.color = oldColor
	}

	/** Draws a dice symbol with the upper-left corner at relative position 0,0.*/
	private fun drawDice(context: DrawContext) {
		context.g.drawRoundRect(0, 0, DICE_SIZE, DICE_SIZE, 5, 5)
		context.g.fillOval(0.5 * DICE_SIZE - DOT_SIZE_HALF, 0.5 * DICE_SIZE - DOT_SIZE_HALF, 2.0 * DOT_SIZE_HALF, 2.0 * DOT_SIZE_HALF)
		context.g.fillOval(0.25 * DICE_SIZE - DOT_SIZE_HALF, 0.75 * DICE_SIZE - DOT_SIZE_HALF, 2.0 * DOT_SIZE_HALF, 2.0 * DOT_SIZE_HALF)
		context.g.fillOval(0.75 * DICE_SIZE - DOT_SIZE_HALF, 0.25 * DICE_SIZE - DOT_SIZE_HALF, 2.0 * DOT_SIZE_HALF, 2.0 * DOT_SIZE_HALF)
	}
}