package ch.scorpion.antares.view.arithmetic

import ch.scorpion.antares.model.arithmetic.BitExtender
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class BitExtenderView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: BitExtender = BitExtender()
) : OrientableRectangularVerticeView<BitExtender>(styleProvider, model)
{
	companion object {

		private const val SIZE = 4 * Look.SCALE

		const val INPUT_BIT_WIDTH_BASE_KEY = "element.property.inputBitWidth"
		const val OUTPUT_BIT_WIDTH_BASE_KEY = "element.property.outputBitWidth"

		private val SHAPE = System.createPath()
			.moveTo(0.0, h(-1))
			.lineTo(w(2.5), h(-1))
			.lineTo(SIZE.toDouble(), h(-2))
			.lineTo(SIZE.toDouble(), h(2))
			.lineTo(w(2.5), h(1))
			.lineTo(0.0, h(1))
			.close()
	}

	init {
		modelExchanged(null)
		setBounds(getInput().unconnectedLength, -SIZE / 2, SIZE, SIZE)
	}

	override fun modelExchanged(oldModel: BitExtender?) {
		super.modelExchanged(oldModel)
		addPortView(DigitalPortView(
			styleProvider,
			model.getInput(),
			x = LENGTH,
			y = 0,
			direction = Direction.WEST))
		addPortView(DigitalPortView(
			styleProvider,
			model.getOutput(),
			x = LENGTH + SIZE,
			y = 0,
			direction = Direction.EAST))
	}

	/** ---- UI properties */

	@Suppress("MemberVisibilityCanBePrivate")
	var inputBitWidth: BitWidth
		get() = model.inputBitWidth
		set(value) {
			if (value != inputBitWidth) {
				invalidate()
				model.inputBitWidth = value
				invalidate()
				validate()
			}
		}

	@Suppress("MemberVisibilityCanBePrivate")
	var outputBitWidth: BitWidth
		get() = model.outputBitWidth
		set(value) {
			if (value != outputBitWidth) {
				invalidate()
				model.outputBitWidth = value
				invalidate()
				validate()
			}
		}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		drawShadow(context)
		drawShape(context)
	}

	private fun drawShadow(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.translated(LENGTH.toDouble(), 0.0) { it.g.fill(SHAPE) }
			}
		}
	}

	private fun drawShape(context: DrawContext) {
		context.translated(LENGTH.toDouble(), 0.0) {
			it.g.color = getApplicableBackgroundColor(it)
			it.g.fill(SHAPE)
			it.g.color = getApplicableForegroundColor(it)
			it.g.stroke = stroke
			it.g.draw(SHAPE)
		}
	}
}