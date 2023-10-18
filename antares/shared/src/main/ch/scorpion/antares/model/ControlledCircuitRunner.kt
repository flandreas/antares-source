package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.ManualSchedulerTask
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory

/**
 * Wraps a [Scheduler] with a [ControlledTimeService] in order to run isolated simulations of a [DigitalGraph],
 * e.g. to fill a truth table or to run user-provided tests on [DigitalGraph]s.
 *
 * TODO Use in CircuitAnalysisService
 */
class ControlledCircuitRunner(
	private val maxIteration: Int = DEF_MAX_ITERATIONS,
	private val iterationStepNs: Int = DEF_ITERATION_STEP_NS
) {

	companion object {
		private const val DEF_MAX_ITERATIONS = 1_000
		private const val DEF_ITERATION_STEP_NS = 100
	}

	class TooManyIterations : Exception()

	private val eventBus = EventBusImpl()
	private val timeService = ControlledTimeService()
	private val systemSpeedCategory = CurrentSystemSpeedCategory(SystemSpeed(SystemSpeed.MAX_SPEED, eventBus), eventBus)
	private val task = ManualSchedulerTask()
	private val noiseGeneratorHolder = NoiseGeneratorHolder(NoNoiseGenerator())

	private val scheduler = SchedulerImpl(
		systemSpeedCategory,
		timeService,
		eventBus,
		noiseGeneratorHolder,
		task
	)

	fun dispose() {
		scheduler.dispose()
		systemSpeedCategory.dispose()
	}

	/**
	 * Runs the simulation of [circuit] until the simulation queue is empty.
	 *
	 * @param prolog code to be executed after simulation start, but before execution of the [circuit].
	 * Can be used for setting input signals.
	 * @param epilogue code to be executed after simulation execution, but before the simulation is stopped.
	 * Can be used for reading output signals.
	 * @param context an optional context object to be passed into [prolog] and [epilogue]
	 * @throws TooManyIterations if the maximum iteration count is reached
	 */
	fun run(
		circuit: DigitalGraph,
		prolog: (signalHandler:SignalHandler, context:Any?) -> Unit = { _,_ -> },
		epilogue: (context:Any?) -> Unit = {},
		context: Any? = null
	) {
		try {
			timeService.reset()
			startSimulation(circuit)
			prolog(scheduler, context)
			proceedUntilQueueEmpty()
			epilogue(context)
		} finally {
			stopSimulation(circuit)
		}
	}

	private fun startSimulation(circuit: DigitalGraph) {
		scheduler.isActive = true
		circuit.formNet(scheduler)
		circuit.executionInitialize(scheduler)
		circuit.executionStart(scheduler, null)
	}

	fun proceedUntilQueueEmpty() {
		var iterationCont = 0
		while (!scheduler.isQueueEmpty) {
			iterationCont++
			if (iterationCont > maxIteration) {
				throw TooManyIterations()
			}
			timeService.setTimeNanos(timeService.nowNanos() + iterationStepNs)
			scheduler.execute()
		}
	}

	private fun stopSimulation(circuit: DigitalGraph) {
		scheduler.isActive = false
		circuit.executionStopped(scheduler)
	}
}