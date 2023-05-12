package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.DoubleThrowSwitch
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
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
		private const val WIDTH = 6 * SCALE
		private const val HEIGHT = 5 * SCALE
	}

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(AbstractAntaresPortView.LENGTH, -HEIGHT / 2, WIDTH, HEIGHT)
	}

	override fun modelExchanged(oldModel: DoubleThrowSwitch?) {
		super.modelExchanged(oldModel)
		addPortView(
			DigitalPortView(
				styleProvider,
				port = model.getPort(1),
				direction = Direction.WEST,
				x = AbstractAntaresPortView.LENGTH,
				y = 0,
				showBitWidthAnnotation = false
			)
		)
		addPortView(
			DigitalPortView(
				styleProvider,
				port = model.getPort(2),
				direction = Direction.EAST,
				x = AbstractAntaresPortView.LENGTH + WIDTH,
				y = -2 * SCALE,
				showBitWidthAnnotation = false
			)
		)
		addPortView(
			DigitalPortView(
				styleProvider,
				port = model.getPort(3),
				direction = Direction.EAST,
				x = AbstractAntaresPortView.LENGTH + WIDTH,
				y = 2 * SCALE,
				showBitWidthAnnotation = false
			)
		)
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		(getPortView(model.getPort(1)) as DigitalPortView).prepareConnectionDrawContext(context)

		context.g.drawLine(bounds.minX, 0.0, bounds.minX + 1 * SCALE, 0.0)

		if (model.isOn) {
			context.g.drawLine(bounds.minX + 1 * SCALE, 0.0, bounds.maxX - 1 * SCALE, -2.0 * SCALE)
		} else {
			context.g.drawLine(bounds.minX + 1 * SCALE, 0.0, bounds.maxX - 1 * SCALE, 2.0 * SCALE)
		}

		context.g.fillCircle(bounds.minX + 1 * SCALE, 0.0, circleRadius)

		(getPortView(model.getPort(2)) as DigitalPortView).prepareConnectionDrawContext(context)

		context.g.drawLine(bounds.maxX - 1 * SCALE, -2.0 * SCALE, bounds.maxX, -2.0 * SCALE)
		context.g.fillCircle(bounds.maxX - 1 * SCALE, -2.0 * SCALE, circleRadius)

		(getPortView(model.getPort(3)) as DigitalPortView).prepareConnectionDrawContext(context)
		context.g.drawLine(bounds.maxX - 1 * SCALE, 2.0 * SCALE, bounds.maxX,2.0 * SCALE)
		context.g.fillCircle(bounds.maxX - 1 * SCALE, 2.0 * SCALE, circleRadius)

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