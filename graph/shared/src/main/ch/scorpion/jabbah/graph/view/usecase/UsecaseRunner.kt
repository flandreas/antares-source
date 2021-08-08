package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.execution.actor.SimpleActorData
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase

/**
 * Runs a [Usecase] by starting the [Scheduler] and executing the JavaScrip code associated with the [Usecase].
 * </p>
 * In every method that provides support for the DSL gateway, an [Actor] is registered with the [Scheduler]
 * for the requested delay time. After this time has elapsed, the action associated with the [Actor] is executed,
 * which lead to the desired change in the running [GraphView].
 * </p>
 * A [UsecaseRunner] is instantiated for every single run of a [Usecase].
 */
class UsecaseRunner(
	private val usecase: Usecase,
	val graphView: GraphView,
	private val scheduler: Scheduler,
	private val applicationModeHolder: ApplicationModeHolder,
	private val gateway: ScriptGateway = ScriptModule.scriptGateway,
) {

	companion object {
		private val LOG by logger(UsecaseRunner::class)
	}

	val script = Script(usecase.executionScript, usecase.name.value, Translations.getString("usecases.issueContext.name"))

	private var didRun = false

	/**
	 * Typically called by the UI to start the [Usecase] associated with this [UsecaseRunner].
	 * This method should only be called once.
	 */
	fun run() {
		checkState(!didRun, "Attempt to repeatedly run UsecaseRunner")

		LOG.debug("Running usecase '${usecase.name.value}'")
		applicationModeHolder.setMode(ApplicationMode.EXEC_USECASE) {
			gateway.usecaseAction(script, this, scheduler)
		}
		didRun = true
	}

	/** ---- Methods used by the DSL gateway */

	/**
	 * Request to execute the specified [action] after [time] nanoseconds.
	 * This method is typically called by [ScriptGateway.usecaseAction].
	 */
	fun executeAt(time: Long, action: () -> Unit) {
		scheduler.requestActingAfter(UsecaseActor(action), delay(time), SimpleActorData())
	}

	fun <T : Any> applyOscillation(input: GraphInput<T>, firstValue: T, secondValue: T, period: Long) {
		scheduler.requestActingAfter(UsecaseClock<T>(input, firstValue, secondValue, period), 1, SimpleActorData())
	}

	private fun delay(time: Long): Long {
		return time - scheduler.executionTime
	}

	private class UsecaseActor(private val action: () -> Unit) : ActorImpl() {
		override fun act(signalHandler: SignalHandler, data: ActorData) {
			action.invoke()
			super.act(signalHandler, data)
		}
	}

	private class UsecaseClock<T : Any>(
		private val input: GraphInput<T>,
		private val firstValue: T,
		private val secondValue: T,
		private val period: Long
	) : ActorImpl() {

		private var currentValue = firstValue

		override fun act(signalHandler: SignalHandler, data: ActorData) {
			toggleCurrentValue()
			input.setIncomingSignal(currentValue, signalHandler)
			signalHandler.requestActingAfter(this, period / 2, SimpleActorData())
			super.act(signalHandler, data)
		}

		private fun toggleCurrentValue() {
			currentValue = if (currentValue == firstValue) secondValue else firstValue
		}
	}
}