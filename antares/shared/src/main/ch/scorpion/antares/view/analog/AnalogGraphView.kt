package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.analog.AnalogCalculationRequest
import ch.scorpion.antares.model.analog.AnalogGraph
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl

/**
 * A [GraphViewImpl] for [AnalogSignal] overridden to implement an animation of the
 * electrical current flowing along the [AnalogEdgeView]s of this [AnalogGraphView].
 */
class AnalogGraphView(
	graph: AnalogGraph,
	eventBus: EventBus = BaseModule.eventBus
) : GraphViewImpl(graph, eventBus) {

	companion object {
		private val LOG by logger(AnalogGraphView::class)
	}

	/** Not set before [executionStart]. */
	private var analysis: AnalogCircuitAnalysis? = null

	private val calculationRequestHandler: EventHandler<AnalogCalculationRequest> = {
		if (this.graph!!.elements.contains(it.source)) {
			recalculate(it.signalHandler)
		}
	}

	init {
		eventBus.register(AnalogCalculationRequest::class, calculationRequestHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(calculationRequestHandler)
	}

	@Suppress("unused") // Reflection
	constructor() : this(TranslatableText(Translations.getString("graph.name.unknown")))

	constructor(name: TranslatableText) : this(GraphModelModule.graphFactory.create(name, AntaresGraphTypes.Analog) as AnalogGraph)

	/** ---- [GraphViewImpl] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		checkDesign(signalHandler)
		AntaresViewModule.analogCircuitCalculator.calculate(ensureAnalysis(), signalHandler)
		CurrentFlowAnimator.register(this, signalHandler.systemSpeedCategory)
	}

	override fun executionStop(signalHandler: SignalHandler) {
		CurrentFlowAnimator.unregister(this)
	}

	override fun checkDesign(signalHandler: SignalHandler): Boolean =
		ensureFullyConnected() && analyzeAndCalculate(signalHandler)

	/** ---- [AnalogGraphView] */

	fun currentFlowAnimationTick(systemSpeedCategory: CurrentSystemSpeedCategory) {
		getEdgeViews()
			.map { it as AnalogEdgeView }
			.forEach { it.currentFlowAnimationTick(systemSpeedCategory.systemSpeed) }

		invalidate()
		validate()
	}

	private fun ensureFullyConnected(): Boolean {
		if (getDrawables { it is GraphElementView<*> }.any { !it.isFullyConnected }) {
			eventBus.post(IssueImpl(
				IssueSeverity.Error,
				Translations.getString("antares.analogCalc.notFullyConnected.error.name"),
				Translations.getString("antares.analogCalc.notFullyConnected.error.desc"),
				name,
				"Simulation"
			))
			return false
		}
		return true
	}

	private fun analyzeAndCalculate(signalHandler: SignalHandler): Boolean {
		return try {
			AntaresViewModule.analogCircuitCalculator.calculate(ensureAnalysis(), signalHandler)
			true
		} catch (e: Throwable) {
			LOG.error("Error while analyzing: ${e.message}")
			eventBus.post(IssueImpl(
				IssueSeverity.Error,
				Translations.getString("antares.analogCalc.analyse.error.name"),
				Translations.getString("antares.analogCalc.analyse.error.desc", e.message ?: ""),
				name,
				"Simulation"
			))
			false
		}
	}

	fun ensureAnalysis(): AnalogCircuitAnalysis {
		analysis = AntaresViewModule.analogCircuitCalculator.analyse(this)
		return  analysis!!
	}

	fun recalculate(signalHandler: SignalHandler) {
		AntaresViewModule.analogCircuitCalculator.calculate(ensureAnalysis(), signalHandler)
	}
}