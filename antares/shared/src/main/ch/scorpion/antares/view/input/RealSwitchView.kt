package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.RealSwitch
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class RealSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: RealSwitch = RealSwitch()
) : AbstractRealSwitchView<RealSwitch>(styleProvider, model) {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.RealSwitchView.iconPath"
		private const val SIZE = 4 * SCALE
	}

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(DigitalPortView.LENGTH, -SIZE / 2, SIZE, SIZE)
	}

	override fun modelExchanged(oldModel: RealSwitch?) {
		super.modelExchanged(oldModel)
		addPortView(
			DigitalPortView(
				styleProvider,
				port = model.getPort(1),
				direction = Direction.WEST,
				x = DigitalPortView.LENGTH,
				y = 0,
				showBitWidthAnnotation = false
			)
		)
		addPortView(
			DigitalPortView(
				styleProvider,
				port = model.getPort(2),
				direction = Direction.EAST,
				x = DigitalPortView.LENGTH + SIZE,
				y = 0,
				showBitWidthAnnotation = false
			)
		)
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		var circleRadius = 2.0
		if (model.bitWidth.width > 1) {
			context.g.stroke = Themes.get<AntaresTheme>().edge.busStroke
			circleRadius = 3.0
		} else {
			context.g.stroke = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				Themes.get<AntaresTheme>().edge.executionStroke
			} else {
				Themes.get<AntaresTheme>().edge.stroke
			}
		}

		context.g.color = transparent.applyTo(getPortColor(1, context))
		context.g.drawLine(bounds.minX, 0.0, bounds.minX + 0.5 * SCALE, 0.0)

		if (model.isOn) {
			context.g.drawLine(bounds.minX + 0.5 * SCALE, 0.0, bounds.maxX - 0.5 * SCALE, 0.0)
		} else {
			context.g.drawLine(bounds.minX + 0.5 * SCALE, 0.0, bounds.maxX - 1.0 * SCALE, -1.5 * SCALE)
		}

		context.g.fillCircle(bounds.minX + 0.5 * SCALE, 0.0, circleRadius)


		context.g.color = transparent.applyTo(getPortColor(2, context))
		context.g.drawLine(bounds.maxX - 0.5 * SCALE, 0.0, bounds.maxX, 0.0)
		context.g.fillCircle(bounds.maxX - 0.5 * SCALE, 0.0, circleRadius)

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawFocus(context)
		}
	}
}