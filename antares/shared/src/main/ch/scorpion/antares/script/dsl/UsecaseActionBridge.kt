package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner

class UsecaseActionBridge(
	private val runner: UsecaseRunner,
	private val scheduler: Scheduler,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractDrawingViewBridge(runner.graphView, eventBus, runner.script.origin, runner.script.context) {

	companion object {
		private val LOG by logger(UsecaseActionBridge::class)
	}

	private var _errorHandled = false
	override val errorHandled: Boolean get() = _errorHandled

	/**
	 * Maintains the IDs of [CircuitInOutView]s to which a clock has been applied.
	 * Used for forbidding application of more than one clock to the same [CircuitInOutView].
	 */
	private val clockApplications = mutableListOf<Int>()

	@Suppress("unused")
	fun pressButtonAt(time: Long, buttonId: Int) {
		LOG.debug("pressButton $buttonId at $time")
		getButton(buttonId)?.let { button ->
			runner.executeAt(time) {
				button.model.toggle(scheduler)
				if (!button.toggle) {
					// TODO BUG: This should not happen before visualization of first toggle has completed!
					runner.executeAt(time + button.model.propagationDelay) { button.model.toggle(scheduler)}
				}
			}
		}
	}

	@Suppress("unused")
	fun setInputAt(time: Long, inputId: Int, hexValue: String) {
		LOG.debug("setInput of $inputId to '$hexValue' at $time")
		getInput(inputId)?.let {component ->
			runner.executeAt(time) { component.model.setIncomingSignal(Word.of(component.model.bitWidth, hexValue), scheduler) }
		}
	}

	@Suppress("unused")
	fun applyClock(inputId: Int, period: Long) {
		LOG.debug("applyClock with period $period to input $inputId")
		if (clockApplications.contains(inputId)) {
			postMultipleClockIssue(inputId)
		}
		getInput(inputId)?.let { component ->
			clockApplications.add(inputId)
			runner.applyOscillation(
				component.model,
				Word.falseValue(component.model.bitWidth),
				Word.trueValue(component.model.bitWidth),
				period)
		}
	}

	@Suppress("unused")
	fun pauseAt(time: Long) {
		LOG.debug("pause at $time")
		runner.executeAt(time) { scheduler.isPaused = true }
	}

	private fun postMultipleClockIssue(id: Int) {

		return postIssue(
			Translations.getString("antares.usecaseDSL.multipleClocks.name"),
			Translations.getString("antares.usecaseDSL.multipleClocks.text", id)
		)
	}
}