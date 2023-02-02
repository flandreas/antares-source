package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogTransistor
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.net.AbstractTransistorView
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition.HIDE

class AnalogTransistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogTransistor = AnalogTransistor(),
	handedness: Handedness = DEFAULT_HANDEDNESS
) : AbstractTransistorView<AnalogTransistor>(styleProvider, model, handedness), AnalogBranchVerticeView<AnalogTransistor> {

	constructor(type: TransistorType): this(model = AnalogTransistor(type), handedness = DEFAULT_HANDEDNESS)

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: AnalogTransistor?) {
		super.modelExchanged(oldModel)

		addPortView(AnalogPortView(styleProvider, model.gatePort, 0, 0, WEST).also { it.portLabelPosition = HIDE })
		addPortView(AnalogPortView(styleProvider, model.sourcePort, 0, 0, NORTH).also { it.portLabelPosition = HIDE })
		addPortView(AnalogPortView(styleProvider, model.drainPort, 0, 0, SOUTH).also { it.portLabelPosition = HIDE })

		updateGeometry()
	}

	/** ---- [AnalogBranchVerticeView] */

	override fun getOppositeBranchPortView(portView: AnalogPortView): AnalogPortView? =
		if (portView === getPortView(model.sourcePort)) {
			getPortView(model.drainPort) as AnalogPortView
		} else if (portView === getPortView(model.drainPort)) {
			getPortView(model.sourcePort) as AnalogPortView
		} else {
			null
		}
}