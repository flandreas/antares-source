package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner

object GraphUsecaseTestExternalFunctions : UsecaseTestExternalFunctions()

open class UsecaseTestExternalFunctions(
	protected val delegate: GraphViewExternalFunctions = GraphDslModule.graphViewExternalFunctionsFactory()
) : DslExternalFunctions {

	protected lateinit var runner: UsecaseTestRunner

	open fun bind(
		runner: UsecaseTestRunner,
		origin: String,
		context: String,
		eventBus: EventBus = BaseModule.eventBus
	) {
		delegate.bind(runner.graphView, origin, context, eventBus)
		this.runner = runner
	}

	override fun defineIn(symbolTable: ScopedSymbolTable) {
		with(symbolTable) {
			define(ExternalFunctionSymbol("assertOutputAt", 3, ::assertOutputAt))
		}
	}

	private fun assertOutputAt(params: List<Any>): Any {
		assertOutputAtImpl(
			delegate.longParam(0, params),
			delegate.stringParam(1, params),
			delegate.anyParam(2, params))
		return 0L
	}

	private fun assertOutputAtImpl(time: Long, outputName: String, signal: Any) {
		delegate.getOutputGraphPortView(outputName)?.let { graphPortView ->
			runner.assert(time, "Expected value of output '$outputName' to be '$signal'") {
				signal == convertSignal(graphPortView.model.signal)
			}
		}
	}

	protected open fun convertSignal(signal: Any?): Any? = signal
}