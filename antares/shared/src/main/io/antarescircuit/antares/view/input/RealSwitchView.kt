package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.RealSwitch
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class RealSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: RealSwitch = RealSwitch()
) : AbstractRealSwitchView<RealSwitch>(styleProvider, model) {

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(AbstractAntaresPortView.LENGTH, -REAL_SWITCH_HEIGHT_ABOVE, REAL_SWITCH_WIDTH, REAL_SWITCH_HEIGHT_ABOVE + REAL_SWITCH_HEIGHT_BELOW)
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(AbstractAntaresPortView.LENGTH + REAL_SWITCH_WIDTH / 2, -REAL_SWITCH_HEIGHT_ABOVE - LABEL_DIST)

	override fun modelExchanged(oldModel: RealSwitch?) {
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
				x = AbstractAntaresPortView.LENGTH + REAL_SWITCH_WIDTH,
				y = 0,
				showBitWidthAnnotation = false
			)
		)
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		drawTwoPortRealSwitchShape(context)
	}
}