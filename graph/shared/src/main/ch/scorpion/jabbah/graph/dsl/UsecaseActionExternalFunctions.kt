package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
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

	override fun defineIn(symbolTable: ScopedSymbolTable) {
		with(symbolTable) {
			define(ExternalFunctionSymbol("setInputAt", 3, ::setInputAt))
			define(ExternalFunctionSymbol("pauseAt", 1, ::pauseAt))
		}
	}

	private fun setInputAt(params: List<Any>): Any {
		setInputAtImpl(
			delegate.longParam(0, params),
			delegate.stringParam(1, params),
			delegate.anyParam(2, params))
		return 0
	}

	private fun setInputAtImpl(time: Long, inputName: String, signal: Any) {
		val convertedSignal = convertSignal(signal)
		LOG.trace("setInput of '$inputName' to '$convertedSignal' at $time")
		delegate.getInputGraphPortView(inputName)?.let { graphPortView ->
			runner.executeAt(time) { graphPortView.model.setIncomingSignal(convertedSignal, runner.scheduler) }
		}
	}

	protected open fun convertSignal(signal: Any): Any = signal

	private fun pauseAt(params: List<Any>): Any {
		pauseAtImpl(delegate.longParam(0, params))
		return 0
	}

	private fun pauseAtImpl(time: Long) {
		LOG.trace("pause at $time")
		runner.executeAt(time) { runner.scheduler.isPaused = true }
	}
}