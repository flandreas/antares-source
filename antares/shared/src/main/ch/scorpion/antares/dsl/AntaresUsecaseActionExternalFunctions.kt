package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.ExternalFunctionSymbol
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
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

	override fun defineIn(symbolTable: ScopedSymbolTable) {
		super.defineIn(symbolTable)
		with(symbolTable) {
			define(ExternalFunctionSymbol("pressButtonAt", 2, ::pressButtonAt))
			define(ExternalFunctionSymbol("applyClock", 2, ::applyClock))
		}
	}

	private fun pressButtonAt(params: List<Any>): Any {
		pressButtonAtImpl(
			delegate.longParam(0, params),
			delegate.longParam(1, params))
		return 0L
	}

	private fun pressButtonAtImpl(time: Long, buttonId: Long) {
		LOG.trace("pressButton $buttonId at $time")
		cvDelegate.getButton(buttonId.toInt())?.let { button ->
			runner.executeAt(time) {
				button.model.toggle(runner.scheduler)
				if (!button.toggle) {
					// TODO BUG: This should not happen before visualization of first toggle has completed!
					runner.executeAt(time + button.model.propagationDelay) { button.model.toggle(runner.scheduler)}
				}
			}
		}
	}

	private fun applyClock(params: List<Any>): Any {
		applyClockImpl(
			cvDelegate.stringParam(0, params),
			cvDelegate.longParam(1, params))
		return 0L
	}

	private fun applyClockImpl(inputName: String, period: Long) {
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