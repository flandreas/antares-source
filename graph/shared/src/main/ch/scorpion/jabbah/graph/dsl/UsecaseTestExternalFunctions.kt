package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.SignalUtil
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

	override fun defineIn(symbolTable: SymbolTable) {
		with(symbolTable) {
			define(ExternalFunctionSymbol("assertOutputAt", 3, ::assertOutputAtImpl))
		}
	}

	private fun assertOutputAtImpl(params: List<Any>): Any {
		assertOutputAt(
			longParam(0, params),
			stringParam(1, params),
			anyParam(2, params))
		return 0L
	}

	/**
	 * Asserts (checks) that an output pin has a particular value.
	 *
	 * @param time the simulation time (ns) at which the output pin is supposed to have value [value]
	 * @param outputName the name of the output pin
	 * @param value the expected value
	 */
	private fun assertOutputAt(time: Long, outputName: String, value: Any) {
		delegate.getOutputGraphPortView(outputName)?.let { graphPortView ->
			val expected = convertSignal(value)
			runner.assert(time, "Expected output '$outputName' to be '$expected'") {
				val actual = graphPortView.model.signal
				SignalUtil.equals(actual, expected)
			}
		}
	}

	protected open fun convertSignal(signal: Any?): Any? = signal
}