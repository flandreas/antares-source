package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner

class UsecaseTestBridge(
	private val runner: UsecaseTestRunner,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractDrawingViewBridge(runner.graphView, eventBus, runner.script.origin, runner.script.context) {

	companion object {
		private val LOG by logger(UsecaseTestBridge::class)
	}

	@Suppress("unused")
	fun assertLedOnAt(time: Long, id: Int) {
		// TODO I18N
		runner.assert(time, { getLED(id)?.model!!.isOn }, "LED $id should be on")
	}

	@Suppress("unused")
	fun assertOutputAt(time: Long, id: Int, hexValue: String) {
		getOutput(id)?.let { component ->
			runner.assert(
				time,
				{
					val value = component.model!!.getInput<DigitalSignal>().getIncomingSignal()
					value!!.toHexString() == hexValue
				},
				"Expected value of output $id to be '$hexValue'")
		}
	}
}
