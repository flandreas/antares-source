package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.net.PowerOnReset
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.LineCap
import ch.scorpion.jabbah.draw.graphics.LineJoin
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy

class PowerOnResetView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: PowerOnReset = PowerOnReset()
) : OrientableRectangularVerticeView<PowerOnReset>(styleProvider, model) {

	companion object {
		private const val SIZE = 4 * SCALE

		private val POSITIVE_PATH = System.createPath()
			.moveTo(-0.5 * SCALE, -1.0 * SCALE)
			.lineTo(0.5 * SCALE, -1.0 * SCALE)
			.lineTo(0.5 * SCALE, 1.0 * SCALE)
			.lineTo(1.5 * SCALE, 1.0 * SCALE)

		private val NEGATIVE_PATH = System.createPath()
			.moveTo(-0.5 * SCALE, 1.0 * SCALE)
			.lineTo(0.5 * SCALE, 1.0 * SCALE)
			.lineTo(0.5 * SCALE, -1.0 * SCALE)
			.lineTo(1.5 * SCALE, -1.0 * SCALE)

		private val POWER_ON_STROKE = Stroke(1.0f, LineCap.BUTT, LineJoin.MITER, 5.0f, floatArrayOf(2.4f), 0.0f)
	}

	private val symbolPath: Path get() = when (logic) {
		Logic.POSITIVE -> POSITIVE_PATH
		Logic.NEGATIVE -> NEGATIVE_PATH
	}

	init {
		modelExchanged(null)
		setBounds(-AbstractAntaresPortView.LENGTH - SIZE, -SIZE/2, SIZE, SIZE)
		preferredSelectionDrawingStrategy = SelectionDrawingStrategy.REPLACE
	}

	override fun modelExchanged(oldModel: PowerOnReset?) {
		super.modelExchanged(oldModel)
		addPortView(DigitalPortView(
			styleProvider,
			port = model.getOutput(),
			-AbstractAntaresPortView.LENGTH, 0,
			direction = Direction.EAST))
	}

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != model.bitWidth) {
				invalidate()
				model.bitWidth = value
				invalidate()
				update()
			}
		}

	var logic: Logic
		get() = model.logic
		set(value) {
			if (value != model.logic) {
				invalidate()
				model.logic = value
				tooltip.reset()
				invalidate()
				update()
			}
		}

	/** ---- [AbstractDrawable] */

	override fun drawImpl(context: DrawContext) {
		drawImplBeforeBorder(context)
		drawShape(context)
		drawImplAfterBorder(context)
	}

	private fun drawShape(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}

		drawBackground(context)
		drawBorder(context)
		drawAnnotation(context)
	}

	private fun drawBorder(context: DrawContext) {
		context.g.color = transparent.applyTo(context.choose(color).foregroundColor)
		context.g.stroke = stroke
		context.g.draw(bounds)
	}

	private fun drawBackground(context: DrawContext) {
		context.g.color = transparent.applyTo(context.choose(color).backgroundColor)
		context.g.fill(bounds)
	}

	private fun drawAnnotation(context: DrawContext) {
		context.translated(-AbstractAntaresPortView.LENGTH - SIZE / 2.0, 0.0) {
			it.g.rotate(rotation.inverse().angle)
			it.g.stroke = POWER_ON_STROKE
			it.g.drawLine(-0.5 * SCALE, -1.5 * SCALE, -0.5 * SCALE, 1.5 * SCALE)
			it.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
			it.g.draw(symbolPath)
			it.g.rotate(rotation.angle)
		}
	}
}