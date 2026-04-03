package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.DoubleThrowSwitch
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class DoubleThrowSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DoubleThrowSwitch = DoubleThrowSwitch()
) : AbstractRealSwitchView<DoubleThrowSwitch>(styleProvider, model) {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.RealSwitchView.iconPath"
		const val WIDTH = 6 * SCALE
		const val HEIGHT = 6 * SCALE
	}

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(AbstractAntaresPortView.LENGTH, h(-3.5).toInt(), WIDTH, HEIGHT)
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(AbstractAntaresPortView.LENGTH + WIDTH / 2, -HEIGHT / 2 - LABEL_DIST)

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