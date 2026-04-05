package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.net.PowerOnReset
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.graphics.LineCap
import io.antarescircuit.jabbah.draw.graphics.LineJoin
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy

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
		context.g.color = transparent.applyTo(context.chooseForeground(foregroundColor))
		context.g.stroke = stroke
		context.g.draw(bounds)
	}

	private fun drawBackground(context: DrawContext) {
		context.g.color = transparent.applyTo(context.chooseBackground(backgroundColor))
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