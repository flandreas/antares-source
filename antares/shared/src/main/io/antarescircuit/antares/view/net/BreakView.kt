package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.Break
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class BreakView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Break = Break()
) : LabeledRectangularVerticeView<Break>(styleProvider, model) {

	companion object {
		private const val SIZE = 4 * SCALE
		private val SYMBOL_PATH = System.createPath()
			.moveTo(1.5 * SCALE, -0.5 * SCALE)
			.lineTo(2.5 * SCALE, 0.5 * SCALE)
			.moveTo(1.5 * SCALE, 0.5 * SCALE)
			.lineTo(2.5 * SCALE, -0.5 * SCALE)
	}

	init {
		initExternalLabel()
		modelExchanged(null)
		setBounds(getInput().unconnectedLength, -SIZE / 2, SIZE, SIZE)
	}

	override val relativeExternalLabelLocation: Point2D get() = Point2D(SIZE + LENGTH + LABEL_DIST, 0)

	/** ---- UI properties] */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			invalidate()
			model.bitWidth = value
		}

	var value: Long
		get() = model.value.getValue().toLong()
		set(newValue) {
			invalidate()
			model.value = DigitalSignalFactory.of(bitWidth, newValue)
		}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: Break?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(),
			direction = Direction.WEST)
		portView.setLocation(portView.unconnectedLength, 0)
		addPortView(portView)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		drawShadow(context)
		drawBackground(context)
		drawBorder(context)
		drawSymbol(context)
	}

	/** ---- [BreakView] */

	private fun drawShadow(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillOval(xInt, yInt, SIZE, SIZE)
			}
		}
	}

	private fun drawBackground(context: DrawContext) {
		context.g.color = getFillColor(context)
		context.g.fillOval(xInt, yInt, SIZE, SIZE)
	}

	private fun drawBorder(context: DrawContext) {
		context.g.color = getBorderColor(context)
		context.g.stroke = stroke
		context.g.drawOval(xInt, yInt, SIZE, SIZE)
	}

	private fun drawSymbol(context: DrawContext) {
		context.g.color = getSymbolColor(context)
		context.translated(LENGTH.toDouble(), 0.0) {
			it.g.draw(SYMBOL_PATH)
		}
	}

	private fun getFillColor(context: DrawContext): Color =
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute && model.isTriggered) {
			transparent.applyTo(Themes.get<GraphTheme>().error.backgroundColor)
		} else {
			getApplicableBackgroundColor(context)
		}

	private fun getBorderColor(context: DrawContext): Color =
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute && model.isTriggered) {
			transparent.applyTo(Themes.get<GraphTheme>().error.foregroundColor)
		} else {
			getApplicableForegroundColor(context)
		}

	private fun getSymbolColor(context: DrawContext): Color =
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute && model.isTriggered) {
			transparent.applyTo(Themes.get<GraphTheme>().error.textColor)
		} else {
			getApplicableForegroundColor(context)
		}
}