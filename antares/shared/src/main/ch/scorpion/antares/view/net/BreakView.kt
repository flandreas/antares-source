package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.net.Break
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class BreakView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Break = Break()
) : DigitalComponentView<Break>(styleProvider, model) {

	companion object {
		private const val SIZE = 4 * SCALE
		private val SYMBOL_PATH = System.createPath()
			.moveTo(1.5 * SCALE, -0.5 * SCALE)
			.lineTo(2.5 * SCALE, 0.5 * SCALE)
			.moveTo(1.5 * SCALE, 0.5 * SCALE)
			.lineTo(2.5 * SCALE, -0.5 * SCALE)
	}

	init {
		modelExchanged(null)
		setBounds(getInput().unconnectedLength, -SIZE / 2, SIZE, SIZE)
	}

	/** ---- UI properties] */

	var logic: Logic
		get() = model.logic
		set(value) {
			model.logic = value
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
		context.g.translate(DigitalPortView.LENGTH.toDouble(), 0.0)
		context.g.draw(SYMBOL_PATH)
		context.g.translate(-DigitalPortView.LENGTH.toDouble(), 0.0)
	}

	private fun getFillColor(context: DrawContext): Color =
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			transparent.applyTo(model.inputSignal!!.color.foregroundColor)
		} else {
			getApplicableBackgroundColor(context)
		}

	private fun getBorderColor(context: DrawContext): Color =
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			transparent.applyTo(model.inputSignal!!.color.backgroundColor)
		} else {
			getApplicableForegroundColor(context)
		}

	private fun getSymbolColor(context: DrawContext): Color =
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			transparent.applyTo(if (model.inputSignalSet) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor)
		} else {
			getApplicableForegroundColor(context)
		}
}