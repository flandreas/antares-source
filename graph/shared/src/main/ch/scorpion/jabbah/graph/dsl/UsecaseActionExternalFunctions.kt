package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner

object GraphUsecaseActionExternalFunctions : UsecaseActionExternalFunctions()

/**
 * External functions to be used by [Usecase] DSL scripts.
 */
open class UsecaseActionExternalFunctions(
	protected val delegate: GraphViewExternalFunctions = GraphDslModule.graphViewExternalFunctionsFactory()
) : DslExternalFunctions {

	companion object {
		private val LOG by logger(UsecaseActionExternalFunctions::class)
	}

	protected lateinit var runner: UsecaseRunner

	open fun bind(
		runner: UsecaseRunner,
		origin: String,
		context: String,
		eventBus: EventBus = BaseModule.eventBus
	) {
		delegate.bind(runner.graphView, origin, context, eventBus)
		this.runner = runner
	}

	override fun defineIn(symbolTable: SymbolTable) {
		with(symbolTable) {
			define(ExternalFunctionSymbol("setInputAt", 3, ::setInputAtImpl))
			define(ExternalFunctionSymbol("pauseAt", 1, ::pauseAtImpl))
			define(ExternalFunctionSymbol("clickMouseAt", 3, ::clickMouseAtImpl))
			define(ExternalFunctionSymbol("pressKeyAt", 2, ::pressKeyAtImpl))
		}
	}

	private fun setInputAtImpl(params: List<Any>): Any {
		setInputAt(
			longParam(0, params),
			stringParam(1, params),
			anyParam(2, params))
		return 0
	}

	/**
	 * Sets the signal of a particular input pin.
	 *
	 * @param time the simulation time (ns) at which the input is to be set
	 * @param inputName the name of the input pin whose signal is to be set
	 * @param signal the signal to set on the specified input pin
	 */
	private fun setInputAt(time: Long, inputName: String, signal: Any) {
		val convertedSignal = convertSignal(signal)
		LOG.trace("setInput of '$inputName' to '$convertedSignal' at $time")
		delegate.getInputGraphPortView(inputName)?.let { graphPortView ->
			runner.executeAt(time) { graphPortView.model.setIncomingSignal(convertedSignal, runner.scheduler) }
		}
	}

	protected open fun convertSignal(signal: Any): Any = signal

	private fun pauseAtImpl(params: List<Any>): Any {
		pauseAt(longParam(0, params))
		return 0
	}

	/**
	 * Pause simulation at a particular simulation time. Can be used to drive
	 * the simulation into a particular state and then pause the simulation
	 * to let the user take over.
	 *
	 * @param time the time (ns) at which the simulation is to be paused
	 */
	private fun pauseAt(time: Long) {
		LOG.trace("pause at $time")
		runner.executeAt(time) { runner.scheduler.isSingleStepMode = true }
	}

	private fun clickMouseAtImpl(params: List<Any>): Any {
		clickMouseAt(
			longParam(0, params),
			longParam(1, params).toInt(),
			longParam(2, params).toInt()
		)
		return 0
	}

	/**
	 * Click the left mouse button at a particular coordinate.
	 * @param time the simulation time (ns) at which the mouse click is to be done
	 * @param x the x coordinate of the click location  in model space
	 * @param y the y coordinate of the click location in model space
	 */
	private fun clickMouseAt(time: Long, x: Int, y: Int) {
		runner.executeAt(time) {
			runner.clickMouseAt(x, y)
		}
	}

	private fun pressKeyAtImpl(params: List<Any>): Any {
		pressKeyAt(
			longParam(0, params),
			longParam(1, params).toInt()
		)
		return 0
	}

	/**
	 * Press the key with the specified ASCII code at a particular simulation time.
	 * @param time the simulation time (ns) at which the key is to be pressed
	 * @param keyCode the ASCII code of the key
	 */
	private fun pressKeyAt(time: Long, keyCode: Int) {
		runner.executeAt(time) {
			runner.pressKey(keyCode)
		}
	}
}