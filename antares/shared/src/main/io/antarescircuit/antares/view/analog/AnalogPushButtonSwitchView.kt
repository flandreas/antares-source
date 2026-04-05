package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogSwitch
import io.antarescircuit.antares.view.input.AbstractPushButtonSwitchView
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider

class AnalogPushButtonSwitchView(
	styleProvider: StyleProvider =  DrawStyleModule.styleProvider,
	model: AnalogSwitch = AnalogSwitch()
) : AbstractPushButtonSwitchView<AnalogSwitch>(styleProvider, model) {

	override val controlId: String get() = "analogSwitch:${model.id}"
}