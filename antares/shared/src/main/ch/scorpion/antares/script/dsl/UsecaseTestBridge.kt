package ch.scorpion.antares.script.dsl

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
}
