package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
import ch.scorpion.jabbah.graph.dsl.GraphDslModule
import ch.scorpion.jabbah.graph.dsl.UsecaseTestExternalFunctions

object AntaresUsecaseTestExternalFunctions : UsecaseTestExternalFunctions(GraphDslModule.graphViewExternalFunctionsFactory()
) {

	private val cvDelegate: AntaresGraphViewExternalFunctions get() = delegate as AntaresGraphViewExternalFunctions

	override fun defineIn(symbolTable: ScopedSymbolTable) {
		super.defineIn(symbolTable)
		with(symbolTable) {
			define(ExternalFunctionSymbol("assertLedOnAt", 2, ::assertLedOnAt))
			define(ExternalFunctionSymbol("assertLedOffAt", 2, ::assertLedOffAt))
		}
	}

	private fun assertLedOnAt(params: List<Any>): Any {
		assertLedOnAtImpl(
			delegate.longParam(0, params),
			delegate.longParam(1, params).toInt())
		return 0L
	}

	private fun assertLedOnAtImpl(time: Long, id: Int) {
		cvDelegate.getLED(id)?.let {
			runner.assert(time, Translations.getString("antares.usecaseDSL.assertLedOn.text") ){
				it.model.isOn
			}
		}
	}

	private fun assertLedOffAt(params: List<Any>): Any {
		assertLedOffAtImpl(
			delegate.longParam(0, params),
			delegate.longParam(1, params).toInt())
		return 0L
	}

	private fun assertLedOffAtImpl(time: Long, id: Int) {
		cvDelegate.getLED(id)?.let {
			runner.assert(time, Translations.getString("antares.usecaseDSL.assertLedOff.text") ){
				it.model.isOn
			}
		}
	}

	override fun convertSignal(signal: Any?): Any? {
		return when (signal) {
			is Long -> DigitalSignalFactory.ofMinimalBitWidth(signal.toULong())
			is ULong -> DigitalSignalFactory.ofMinimalBitWidth(signal)
			else -> super.convertSignal(signal)
		}
	}
}