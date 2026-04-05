package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.DoubleThrowSwitch
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class DoubleThrowSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DoubleThrowSwitch = DoubleThrowSwitch()
) : AbstractRealSwitchView<DoubleThrowSwitch>(styleProvider, model) {

	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.input.RealSwitchView.iconPath"
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