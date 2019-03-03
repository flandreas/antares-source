package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.EmptyActor
import ch.scorpion.jabbah.execution.actor.SimpleActorData
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerStateEvent
import ch.scorpion.jabbah.graph.ApplicationModeHolder
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
	val graphView: GraphView<*>,
	private val scheduler: Scheduler,
	private val appModeHolder: ApplicationModeHolder,
	private val gateway: ScriptGateway = ScriptModule.scriptGateway
) {

	companion object {
		private val LOG by logger(UsecaseRunner::class)
	}

	private var didRun = false

	/**
	 * Typically called by the UI to start the [Usecase] associated with this [UsecaseRunner].
	 * This method should only be called once.
	 */
	fun run() {
		checkState(!didRun, "Attempt to repeatedly run UsecaseRunner")

		LOG.debug("Running usecase '${usecase.name.value}'")
		appModeHolder.toggleMode() {
			val script = Script(usecase.executionScript, usecase.name.value, "Usecase")
			gateway.usecase(script, this, scheduler)
		}
		didRun = true
	}

	/** ---- Methods used by the DSL gateway */

	/**
	 * Request to execute the specified [action] after [time] nanoseconds.
	 * This method is typically called by [ScriptGateway.usecase].
	 */
	fun executeAt(time: Long, action: () -> Unit) {
		val delay = time - scheduler.executionTime
		scheduler.requestActingAfter(UsecaseActor(action), delay, SimpleActorData())
	}

	private class UsecaseActor(private val action: () -> Unit) : EmptyActor() {
		override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
			action.invoke()
			return super.act(signalHandler, data)
		}
	}
}