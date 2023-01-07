package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.analog.AnalogGraph
import ch.scorpion.jabbah.animation.AbstractAnimationTask
import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.DoubleRange
import ch.scorpion.jabbah.animation.Repetition
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl

class AnalogGraphView(
	graph: AnalogGraph,
	eventBus: EventBus = BaseModule.eventBus
) : GraphViewImpl(graph, eventBus) {

	companion object {
		private val LOG by logger(AnalogGraphView::class)
	}

	private lateinit var currentAnimation: CurrentAnimation

	constructor() : this(TranslatableText(Translations.getString("graph.name.unknown")))
	constructor(name: TranslatableText) : this(GraphModelModule.graphFactory.create(name, AntaresGraphTypes.Analog) as AnalogGraph)

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		DummyAnalogCircuitCalculator.calculate(this, signalHandler)

		currentAnimation = CurrentAnimation(this)
		AnimationModule.constantSpeedAnimator.schedule(currentAnimation)
		currentAnimation.start()
	}

	override fun executionStop(signalHandler: SignalHandler) {
		super.executionStop(signalHandler)

		currentAnimation.stop()
	}

	private fun consumeCurrentAnimation(value: Double) {
		getEdgeViews()
			.map { it as AnalogEdgeView }
			.forEach { it.currentAnimationOffset = value }

		invalidate()
		validate()
	}

	private inner class CurrentAnimation(graphView: AnalogGraphView) : AbstractAnimationTask<Double>(
		target = graphView,
		::consumeCurrentAnimation,
		Repetition(DoubleRange(0.0, CurrentFlowVisualization.DISTANCE)),
		250.0
	)
}