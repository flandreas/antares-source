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
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.StoringGraphActorData
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView

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
		private const val DEF_PROPAGATION_DELAY = 10L
	}

	val isNonLinear: Boolean get() = (graph as AnalogGraph).isNonLinear

	/** Not set before [executionStart]. */
	private var analysis: AnalogCircuitAnalysis? = null

	private val calculationRequestHandler: EventHandler<AnalogCalculationRequest> = {
		if (this.graph!!.elements.contains(it.source)) {
			requestActing(it.signalHandler)
		}
	}

	private val actor: Actor = AnalogActor()

	override var overallPropagationDelay: Long?
		get() = super.overallPropagationDelay
		set(value) {
			require(value != null && value > 0) { "Propagation delay must be greater than 0" }
			super.overallPropagationDelay = value
		}

	@Suppress("unused") // Reflection
	constructor() : this(TranslatableText(Translations.getString("graph.name.unknown")))

	constructor(name: TranslatableText) : this(GraphModelModule.graphFactory.create(name, AntaresGraphTypes.Analog) as AnalogGraph)

	init {
		overallPropagationDelay = DEF_PROPAGATION_DELAY
		eventBus.register(AnalogCalculationRequest::class, calculationRequestHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(calculationRequestHandler)
	}

	/** ---- [GraphViewImpl] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		analysis = null
		requestActing(signalHandler)
		CurrentFlowAnimator.register(this, signalHandler.systemSpeedCategory)
	}

	override fun executionStop(signalHandler: SignalHandler) {
		CurrentFlowAnimator.unregister(this)
	}

	override fun checkDesign(signalHandler: SignalHandler, eventBus: EventBus): Boolean {
		if (!ensureFullyConnected()) {
			return false
		}

		try {
			ensureAnalysis()
		} catch (e: IllegalStateException) {
			eventBus.post(IssueImpl(
				severity = IssueSeverity.Error,
				name = Translations.getString("graph.designError.name"),
				description = e.message,
				origin = name.value,
				context = null
			))
			return false
		}

		return true
	}

	/** ---- [AnalogGraphView] */

	val analogElementViews: List<AnalogElement> get() =
		drawables.filterIsInstance<AnalogElement>()

	fun requireAnalysis() {
		analysis = null
	}

	fun currentFlowAnimationTick(systemSpeedCategory: CurrentSystemSpeedCategory) {
		getEdgeViews()
			.map { it as AnalogEdgeView }
			.forEach { it.currentFlowAnimationTick(systemSpeedCategory.systemSpeed) }

		invalidate()
		validate()
	}

	private fun ensureFullyConnected(): Boolean {
		if (getDrawables { it !is OscilloscopeView }.any { !it.isFullyConnected }) {
			eventBus.post(IssueImpl(
				IssueSeverity.Error,
				Translations.getString("antares.analogCalc.notFullyConnected.error.name"),
				Translations.getString("antares.analogCalc.notFullyConnected.error.desc"),
				name.value,
				"Simulation"
			))
			return false
		}
		return true
	}

	fun requestActing(signalHandler: SignalHandler) {
		signalHandler.requestActingAfter(actor, overallPropagationDelay ?: DEF_PROPAGATION_DELAY, createActorData())
	}

	private fun createActorData(): GraphActorData =
		StoringGraphActorData(null, null, graphView = this)

	/**
	 * Analyses this [AnalogGraphView] in case it is not already done.
	 * @throws IllegalStateException in case this [AnalogGraphView] is invalid
	 */
	fun ensureAnalysis(): AnalogCircuitAnalysis {
		if (analysis == null) {
			analysis = AntaresViewModule.analogCircuitCalculatorFactor.create<AnalogCircuitAnalysis>().analyse(this)
		}
		return analysis!!
	}

	private class AnalogActor : ActorImpl() {
		override fun act(signalHandler: SignalHandler, data: ActorData) {
			val graphView = (data as GraphActorData).graphView as AnalogGraphView
			try {
				AntaresViewModule.analogCircuitCalculatorFactor
					.create<AnalogCircuitAnalysis>()
					.calculate(graphView.ensureAnalysis(), signalHandler)
				super.act(signalHandler, data)
			} catch (e: Throwable) {
				LOG.debug("Error while analyzing: ${e.message}")
				BaseModule.eventBus.post(IssueImpl(
					IssueSeverity.Error,
					Translations.getString("antares.analogCalc.analyse.error.name"),
					Translations.getString("antares.analogCalc.analyse.error.desc", e.message ?: ""),
					graphView.name.value,
					"Simulation"
				))
			}
		}
	}
}