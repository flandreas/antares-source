package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSwitch
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.input.AbstractSwitchView
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class AnalogSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogSwitch = AnalogSwitch(),
	private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : AbstractSwitchView<AnalogSwitch>(styleProvider, model),
	AnalogBranchVerticeView<AnalogSwitch>,
	AnalogElement by analogElement
{

	companion object {
		private const val SIZE = 6 * Look.SCALE
	}

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(LENGTH, -SIZE / 2, SIZE, SIZE)
	}

	override fun modelExchanged(oldModel: AnalogSwitch?) {
		super.modelExchanged(oldModel)
		analogElement.bind(model)

		addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
	}

	override fun updateLabels() { }

	override val circleRadius: Double get() = DEF_CIRCLE_RADIUS

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		drawTwoPortRealSwitchShape(context)
	}
}