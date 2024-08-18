package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.base.dsl.longParam
import ch.scorpion.jabbah.graph.dsl.GraphDslModule
import ch.scorpion.jabbah.graph.dsl.UsecaseTestExternalFunctions

object AntaresUsecaseTestExternalFunctions : UsecaseTestExternalFunctions(GraphDslModule.graphViewExternalFunctionsFactory()
) {

	private val cvDelegate: AntaresGraphViewExternalFunctions get() = delegate as AntaresGraphViewExternalFunctions

	override fun defineIn(symbolTable: SymbolTable) {
		super.defineIn(symbolTable)
		with(symbolTable) {
			define(ExternalFunctionSymbol("assertLedOnAt", 2, ::assertLedOnAtImpl))
			define(ExternalFunctionSymbol("assertLedOffAt", 2, ::assertLedOffAtImpl))
		}
	}

	private fun assertLedOnAtImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
		assertLedOnAt(
			longParam(0, params),
			longParam(1, params).toInt())
		return 0L
	}

	/**
	 * Checks if an LED is on.
	 *
	 * @param time the simulation time (ns) at which the LED must be on
	 * @param id the ID of the LED to check
	 */
	private fun assertLedOnAt(time: Long, id: Int) {
		cvDelegate.getLED(id)?.let {
			runner.assert(time, Translations.getString("antares.usecaseDSL.assertLedOn.text") ){
				it.model.isOn
			}
		}
	}

	private fun assertLedOffAtImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
		assertLedOffAt(
			longParam(0, params),
			longParam(1, params).toInt())
		return 0L
	}

	/**
	 * Checks if an LED is off.
	 *
	 * @param time the simulation time (ns) at which the LED must be off
	 * @param id the ID of the LED to check
	 */
	private fun assertLedOffAt(time: Long, id: Int) {
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