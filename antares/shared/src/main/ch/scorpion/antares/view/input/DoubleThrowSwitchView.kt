package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.DoubleThrowSwitch
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
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class DoubleThrowSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DoubleThrowSwitch = DoubleThrowSwitch()
) : AbstractRealSwitchView<DoubleThrowSwitch>(styleProvider, model) {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.RealSwitchView.iconPath"
		private const val WIDTH = 4 * SCALE
		private const val HEIGHT = 5 * SCALE
	}

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(DigitalPortView.LENGTH, -HEIGHT / 2, WIDTH, HEIGHT)
	}

	override fun modelExchanged(oldModel: DoubleThrowSwitch?) {
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
				x = DigitalPortView.LENGTH + WIDTH,
				y = -2 * SCALE,
				showBitWidthAnnotation = false
			)
		)
		addPortView(
			DigitalPortView(
				styleProvider,
				port = model.getPort(3),
				direction = Direction.EAST,
				x = DigitalPortView.LENGTH + WIDTH,
				y = 2 * SCALE,
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
			context.g.drawLine(bounds.minX + 0.5 * SCALE, 0.0, bounds.maxX - 0.5 * SCALE, -2.0 * SCALE)
		} else {
			context.g.drawLine(bounds.minX + 0.5 * SCALE, 0.0, bounds.maxX - 0.5 * SCALE, 2.0 * SCALE)
		}

		context.g.fillCircle(bounds.minX + 0.5 * SCALE, 0.0, circleRadius)

		context.g.color = transparent.applyTo(getPortColor(2, context))
		context.g.drawLine(bounds.maxX - 0.5 * SCALE, -2.0 * SCALE, bounds.maxX, -2.0 * SCALE)
		context.g.fillCircle(bounds.maxX - 0.5 * SCALE, -2.0 * SCALE, circleRadius)

		context.g.color = transparent.applyTo(getPortColor(3, context))
		context.g.drawLine(bounds.maxX - 0.5 * SCALE, 2.0 * SCALE, bounds.maxX,2.0 * SCALE)
		context.g.fillCircle(bounds.maxX - 0.5 * SCALE, 2.0 * SCALE, circleRadius)

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawFocus(context)
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (!toggle) {
			writer.writeBoolean("toggle", toggle)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("toggle")) {
			toggle = reader.readBoolean("toggle")
		}
	}
}