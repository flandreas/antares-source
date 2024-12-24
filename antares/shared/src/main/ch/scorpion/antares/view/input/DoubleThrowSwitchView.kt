package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.DoubleThrowSwitch
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable

class DoubleThrowSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DoubleThrowSwitch = DoubleThrowSwitch()
) : AbstractRealSwitchView<DoubleThrowSwitch>(styleProvider, model) {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.RealSwitchView.iconPath"
		const val WIDTH = 6 * SCALE
		const val HEIGHT = 5 * SCALE
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
		drawThreePortRealSwitchShape(context)
	}
}