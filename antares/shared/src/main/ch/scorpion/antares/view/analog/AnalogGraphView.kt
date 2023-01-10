package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.analog.AnalogGraph
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.antares.model.analog.AnalogSignal

/**
 * A [GraphViewImpl] for [AnalogSignal] overridden to implement an animation of the
 * electrical current flowing along the [AnalogEdgeView]s of this [AnalogGraphView].
 */
class AnalogGraphView(
	graph: AnalogGraph,
	eventBus: EventBus = BaseModule.eventBus
) : GraphViewImpl(graph, eventBus) {

	companion object {
		private const val CURRENT_FLOW_ANIMATION_STEP = 30
	}

	/** Drives the current flow animation along the [AnalogEdgeView]s. */
	private val timer: Timer by lazy {
		val timer = System.createTimer()
		timer.initialize(CURRENT_FLOW_ANIMATION_STEP, repeats = true, ::timerTick)
		timer
	}

	@Suppress("unused") // Reflection
	constructor() : this(TranslatableText(Translations.getString("graph.name.unknown")))

	constructor(name: TranslatableText) : this(GraphModelModule.graphFactory.create(name, AntaresGraphTypes.Analog) as AnalogGraph)

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		DummyAnalogCircuitCalculator.calculate(this, signalHandler)
		timer.start()
	}

	override fun executionStop(signalHandler: SignalHandler) {
		timer.stop()
	}

	private fun timerTick(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
		getEdgeViews()
			.map { it as AnalogEdgeView }
			.forEach { it.currentFlowAnimationTick() }

		invalidate()
		validate()
	}
}