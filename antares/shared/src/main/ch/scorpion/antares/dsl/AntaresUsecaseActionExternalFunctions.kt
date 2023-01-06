package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.base.dsl.longParam
import ch.scorpion.jabbah.base.dsl.stringParam
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.dsl.UsecaseActionExternalFunctions
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner

object AntaresUsecaseActionExternalFunctions : UsecaseActionExternalFunctions() {

	private val LOG by logger(AntaresUsecaseActionExternalFunctions::class)

	/**
	 * Maintains the names of [GraphInput]s to which a clock has been applied.
	 * Used for forbidding application of more than one clock to the same [CircuitInOutView].
	 */
	private val clockApplications = mutableListOf<String>()

	private val cvDelegate: AntaresGraphViewExternalFunctions get() = delegate as AntaresGraphViewExternalFunctions

	override fun bind(runner: UsecaseRunner, origin: String, context: String, eventBus: EventBus) {
		super.bind(runner, origin, context, eventBus)
		clockApplications.clear()
	}

	override fun convertSignal(signal: Any): Any {
		return when (signal) {
			is Long -> DigitalSignalFactory.ofMinimalBitWidth(signal.toULong())
			is ULong -> DigitalSignalFactory.ofMinimalBitWidth(signal)
			else -> super.convertSignal(signal)
		}
	}

	override fun defineIn(symbolTable: SymbolTable) {
		super.defineIn(symbolTable)
		with(symbolTable) {
			define(ExternalFunctionSymbol("pressButtonAt", 2, ::pressButtonAtImpl))
			define(ExternalFunctionSymbol("applyClock", 2, ::applyClockImpl))
		}
	}

	private fun pressButtonAtImpl(params: List<Any>): Any {
		pressButtonAt(
			longParam(0, params),
			longParam(1, params))
		return 0L
	}

	/**
	 * Press a button at a particular simulation time.
	 *
	 * @param time the simulation time (ns) at which the button is to be pressed
	 * @param buttonId the ID of the button to be pressed
	 */
	private fun pressButtonAt(time: Long, buttonId: Long) {
		LOG.trace("pressButton $buttonId at $time")
		cvDelegate.getButton(buttonId.toInt())?.let { button ->
			runner.executeAt(time) {
				button.model.toggle(runner.scheduler, null)
				if (!button.toggle) {
					// TODO BUG: This should not happen before visualization of first toggle has completed!
					runner.executeAt(time + button.model.propagationDelay) { button.model.toggle(runner.scheduler, null)}
				}
			}
		}
	}

	private fun applyClockImpl(params: List<Any>): Any {
		applyClock(
			stringParam(0, params),
			longParam(1, params))
		return 0L
	}

	/**
	 * Applies an oscillating clock signal to an input pin. Can be used in
	 * subcircuits that received a clock signal from the surrounding circuit.
	 *
	 * @param inputName the name of the input pin to apply the clock signal to
	 * @param period the period (ns) of the clock signal to apply
	 */
	private fun applyClock(inputName: String, period: Long) {
		LOG.trace("applyClock with period $period to input '$inputName'")
		if (clockApplications.contains(inputName)) {
			postMultipleClockIssue(inputName)
		}
		cvDelegate.getInputGraphPortView(inputName)?.let { input ->
			val cvInput = input as CircuitInOutView
			clockApplications.add(inputName)
			runner.applyOscillation(
				cvInput.model,
				DigitalSignalFactory.falseValue(cvInput.model.bitWidth),
				DigitalSignalFactory.trueValue(cvInput.model.bitWidth),
				period)
		}
	}

	private fun postMultipleClockIssue(name: String) = cvDelegate.postIssue(
		Translations.getString("antares.usecaseDSL.multipleClocks.name"),
		Translations.getString("antares.usecaseDSL.multipleClocks.text", name)
	)
}