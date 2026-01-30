package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.execution.actor.SimpleActorData
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.dsl.GraphDslModule
import ch.scorpion.jabbah.graph.dsl.UsecaseTestExternalFunctions
import ch.scorpion.jabbah.graph.model.GraphInput
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
	val scheduler: Scheduler,
	private val applicationModeHolder: ApplicationModeHolder,
	private val viewManager: ContentViewManager = DrawViewModule.viewManager
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
		check(!didRun) { "Attempt to repeatedly run UsecaseRunner" }

		LOG.userTrail("Running usecase '${usecase.name.value}'")
		applicationModeHolder.setMode(ApplicationMode.EXEC_USECASE) {
			GraphDslModule.usecaseActionExternalFunctions.bind(this, usecase.name.value, "Usecase logic")
			usecase.run()
		}

		didRun = true
		applicationModeHolder.setMode(ApplicationMode.EXECUTE)
	}

	/** ---- Methods used by the DSL gateway */

	/**
	 * Request to execute the specified [action] after [time] nanoseconds.
	 * This method is typically called by [UsecaseTestExternalFunctions].
	 */
	fun executeAt(name: String, time: Long, action: () -> Unit) {
		scheduler.requestActingAfter(UsecaseActor(name, time, action), delay(time), SimpleActorData())
	}

	fun <T : Any> applyOscillation(input: GraphInput<T>, firstValue: T, secondValue: T, period: Long) {
		scheduler.requestActingAfter(UsecaseClock(input, firstValue, secondValue, period), 1, SimpleActorData())
	}

	fun pressMouseAt(x: Int, y: Int) {
		if (viewManager.activeView?.view is DrawingView<*>) {
			val view = viewManager.activeView!!.view!!
			view.dispatchEvent(
				MouseEventImpl(
					MouseEventType.PRESSED,
					x = view.modelToViewX(x.toDouble()).toInt(),
					y = view.modelToViewY(y.toDouble()).toInt(),
					button = Button.BUTTON1,
					clickCount = 1
				)
			)
		}
	}

	fun releaseMouseAt(x: Int, y: Int) {
		if (viewManager.activeView?.view is DrawingView<*>) {
			val view = viewManager.activeView!!.view!!
			view.dispatchEvent(MouseEventImpl(
				MouseEventType.RELEASED,
				x = view.modelToViewX(x.toDouble()).toInt(),
				y = view.modelToViewY(y.toDouble()).toInt(),
				button = Button.BUTTON1,
				clickCount = 1
			))
		}
	}

	fun pressKey(keyCode: Int) {
		if (viewManager.activeView?.view is DrawingView<*>) {
			val view = viewManager.activeView!!.view!!
			view.dispatchEvent(KeyEventImpl(
				KeyEventType.PRESSED,
				key = keyCode,
				keyChar = ' '
			))
		}
	}

	fun releaseKey(keyCode: Int) {
		if (viewManager.activeView?.view is DrawingView<*>) {
			val view = viewManager.activeView!!.view!!
			view.dispatchEvent(KeyEventImpl(
				KeyEventType.RELEASED,
				key = keyCode,
				keyChar = ' '
			))
		}
	}

	private fun delay(time: Long): Long {
		return time - scheduler.executionTime
	}

	private class UsecaseActor(
		private val name: String,
		private val time: Long,
		private val action: () -> Unit
	) : ActorImpl() {
		override fun act(signalHandler: SignalHandler, data: ActorData) {
			try {
				action.invoke()
				super.act(signalHandler, data)
			} catch (e: Exception) {
				LOG.error("Error in use case execution", e)
				BaseModule.eventBus.post(IssueImpl(
					IssueSeverity.Error,
					name = Translations.getString("usecase.runError.name"),
					description = e.message ?: e.toString(),
					origin = name,
					context = Translations.getString("usecase.runError.time", time),
				))
			}
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