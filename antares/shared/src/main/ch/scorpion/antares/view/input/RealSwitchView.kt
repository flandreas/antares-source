package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.RealSwitch
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class RealSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: RealSwitch = RealSwitch()
) : AbstractRealSwitchView<RealSwitch>(styleProvider, model) {

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(AbstractAntaresPortView.LENGTH, -REAL_SWITCH_HEIGHT_ABOVE, REAL_SWITCH_WIDTH, REAL_SWITCH_HEIGHT_ABOVE + REAL_SWITCH_HEIGHT_BELOW)
	}

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