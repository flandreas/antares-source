package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory

/**
 * [Timer]-based animator for driving [CurrentFlowVisualization] in all
 * registered [AnalogGraphView]s.
 * The speed of the animation depends on the [CurrentSystemSpeedCategory].
 */
object CurrentFlowAnimator {

	private const val STEP = 30

	private val registrations = mutableSetOf<Registration>()

	/** Drives the current flow animation along the [AnalogEdgeView]s. */
	private val timer: Timer by lazy {
		val timer = System.createTimer()
		timer.initialize(STEP, repeats = true, ::timerTick)
		timer
	}

	fun register(graphView: AnalogGraphView, systemSpeedCategory: CurrentSystemSpeedCategory) {
		registrations.add(Registration(graphView, systemSpeedCategory))
		start()
	}

	fun unregister(graphView: AnalogGraphView) {
		registrations.removeAll { it.graphView === graphView }
		if (registrations.isEmpty()) {
			stop()
		}
	}

	private fun start() {
		if (!timer.isRunning()) {
			timer.start()
		}
	}

	private fun stop() {
		if (timer.isRunning()) {
			timer.stop()
		}
	}

	private fun timerTick(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
		registrations.forEach { it.graphView.currentFlowAnimationTick(it.systemSpeedCategory) }
	}

	private data class Registration(
		val graphView: AnalogGraphView,
		val systemSpeedCategory: CurrentSystemSpeedCategory
	)
}