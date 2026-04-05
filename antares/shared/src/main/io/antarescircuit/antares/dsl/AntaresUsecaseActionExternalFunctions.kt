package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.ExternalFunctionSymbol
import io.antarescircuit.jabbah.base.dsl.SymbolTable
import io.antarescircuit.jabbah.base.dsl.longParam
import io.antarescircuit.jabbah.base.dsl.stringParam
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.dsl.UsecaseActionExternalFunctions
import io.antarescircuit.jabbah.graph.model.GraphInput
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseRunner

object AntaresUsecaseActionExternalFunctions : UsecaseActionExternalFunctions() {

	private val LOG by logger(AntaresUsecaseActionExternalFunctions::class)

	/**
	 * Maintains the names of [GraphInput]s to which a clock has been applied.
	 * Used for forbidding application of more than one clock to the same [DigitalCircuitInOutView].
	 */
	private val clockApplications = mutableListOf<String>()

	private val cvDelegate: AntaresGraphViewExternalFunctions get() = delegate as AntaresGraphViewExternalFunctions

	override fun bind(runner: UsecaseRunner, origin: String, context: String, eventBus: EventBus) {
		super.bind(runner, origin, context, eventBus)
		clockApplications.clear()
	}

	override fun defineIn(symbolTable: SymbolTable) {
		super.defineIn(symbolTable)
		with(symbolTable) {
			define(ExternalFunctionSymbol("pressButtonAt", 2, ::pressButtonAtImpl))
			define(ExternalFunctionSymbol("applyClock", 2, ::applyClockImpl))
		}
	}

	private fun pressButtonAtImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
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
			runner.executeAt("pressButtonAt/press", time) {
				button.model.toggle(runner.scheduler)
				if (!button.toggle) {
					// TODO BUG: This should not happen before visualization of first toggle has completed!
					runner.executeAt("pressButtonAt/release", time + button.model.propagationDelay.value) { button.model.toggle(runner.scheduler)}
				}
			}
		}
	}

	private fun applyClockImpl(params: List<Any>, @Suppress("UNUSED_PARAMETER") context: Any? = null): Any {
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
			val cvInput = input as DigitalCircuitInOutView
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