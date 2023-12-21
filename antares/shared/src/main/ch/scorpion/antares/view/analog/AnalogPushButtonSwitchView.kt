package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSwitch
import ch.scorpion.antares.view.input.AbstractPushButtonSwitchView
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class AnalogPushButtonSwitchView(
	override var styleProvider: StyleProvider =  DrawStyleModule.styleProvider,
	model: AnalogSwitch = AnalogSwitch()
) : AbstractPushButtonSwitchView<AnalogSwitch>(styleProvider, model) {

	override val controlId: String get() = "analogSwitch:${model.id}"
}