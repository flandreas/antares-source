package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner

class UsecaseTestBridge(
	private val runner: UsecaseTestRunner,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractDrawingViewBridge(runner.graphView, eventBus, runner.script.origin, runner.script.context) {

	@Suppress("unused")
	fun assertLedOnAt(time: Long, id: Int) {
		runner.assert(time, { getLED(id)?.model!!.isOn }, Translations.getString("antares.usecaseDSL.assertLedOn.text", id))
	}

	@Suppress("unused")
	fun assertLedOffAt(time: Long, id: Int) {
		runner.assert(time, { !getLED(id)?.model!!.isOn }, Translations.getString("antares.usecaseDSL.assertLedOff.text", id))
	}

	@Suppress("unused")
	fun assertOutputAt(time: Long, id: Int, hexValue: String) {
		getOutput(id)?.let { component ->
			runner.assert(
				time, {
					val value = component.model.getInput<DigitalSignal>().getIncomingSignal()
					value!!.toHexString() == hexValue },
				"Expected value of output $id to be '$hexValue'")
		}
	}
}
