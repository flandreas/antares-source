package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.ManualSchedulerTask
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory

/**
 * Wraps a [Scheduler] with a [ControlledTimeService] in order to run isolated simulations of a [DigitalGraph],
 * e.g. to fill a truth table or to run user-provided tests on [DigitalGraph]s.
 */
class ControlledCircuitRunner(
	val maxIteration: Int = DEF_MAX_ITERATIONS,
	private val iterationStepNs: Int = DEF_ITERATION_STEP_NS
) {

	companion object {
		private val LOG by logger(ControlledCircuitRunner::class)
		private const val DEF_MAX_ITERATIONS = 1_000
		private const val DEF_ITERATION_STEP_NS = 100
	}

	class TooManyIterations : Exception()

	private val eventBus = EventBusImpl()
	private val timeService = ControlledTimeService()
	private val systemSpeedCategory = CurrentSystemSpeedCategory(SystemSpeed(SystemSpeed.MAX_SPEED, eventBus), eventBus)
	private val task = ManualSchedulerTask()
	private val noiseGeneratorHolder = NoiseGeneratorHolder(NoNoiseGenerator())

	val scheduler = SchedulerImpl(
		systemSpeedCategory,
		timeService,
		eventBus,
		noiseGeneratorHolder,
		task,
		isDeepExecution = true
	)

	fun dispose() {
		scheduler.dispose()
		systemSpeedCategory.dispose()
	}

	/**
	 * Starts the simulation of [circuit], runs it until the simulation queue is empty,
	 * and then stops the simulation. Throws a [TooManyIterations] if the maximum iteration count is exceeded.
	 *
	 * @param prolog code to be executed after simulation start, but before execution of the [circuit].
	 * Can be used for setting input signals. Returns the execution duration.
	 * @param epilogue code to be executed after simulation execution, but before the simulation is stopped.
	 * Can be used for reading output signals.
	 * @param context an optional context object to be passed into [prolog] and [epilogue]
	 * @return the simulation time (in ns) it took to run the [circuit]
	 * @throws TooManyIterations if the maximum iteration count is reached
	 */
	fun run(
		circuit: DigitalGraph,
		prolog: (context:Any?) -> Long = { 0L },
		epilogue: (context:Any?) -> Unit = {},
		context: Any? = null
	): Long {
		try {
			return runImpl(circuit, prolog, epilogue, context, doStart = true, doStop = true)
		} catch (e: TooManyIterations) {
			throw e
		} catch (e: Throwable) {
			LOG.error("Unexpected error", e)
			throw e
		} finally {
			stopSimulation(circuit)
		}
	}

	fun runStart(
		circuit: DigitalGraph,
		prolog: (context:Any?) -> Long = { 0L },
		epilogue: (context:Any?) -> Unit = {},
		context: Any? = null
	): Long {
		try {
			return runImpl(circuit, prolog, epilogue, context, doStart = true, doStop = false)
		} catch (e: Throwable) {
			stopSimulation(circuit)
			return 0
		}
	}

	fun runContinue(
		circuit: DigitalGraph,
		prolog: (context:Any?) -> Long = { 0L },
		epilogue: (context:Any?) -> Unit = {},
		context: Any? = null
	): Long {
		try {
			return runImpl(circuit, prolog, epilogue, context, doStart = false, doStop = false)
		} catch (e: Throwable) {
			stopSimulation(circuit)
			return 0
		}
	}

	fun runStop(
		circuit: DigitalGraph,
		prolog: (context:Any?) -> Long = { 0L },
		epilogue: (context:Any?) -> Unit = {},
		context: Any? = null
	): Long {
		try {
			return runImpl(circuit, prolog, epilogue, context, doStart = false, doStop = true)
		} catch (e: Throwable) {
			stopSimulation(circuit)
			return 0
		}
	}

	private fun runImpl(
		circuit: DigitalGraph,
		prolog: (context:Any?) -> Long = { 0L },
		epilogue: (context:Any?) -> Unit = {},
		context: Any? = null,
		doStart: Boolean,
		doStop: Boolean
	): Long {
		if (doStart) {
			timeService.reset()
			startSimulation(circuit)
		}

		val prologTime = prolog(context)

		val startExecutionTime = scheduler.executionTime
		proceedUntilQueueEmpty()

		epilogue(context)

		val duration = prologTime + (scheduler.executionTime - startExecutionTime)

		if (doStop) {
			stopSimulation(circuit)
		}

		return duration
	}

	private fun startSimulation(circuit: DigitalGraph) {
		scheduler.isActive = true
		circuit.formNet(scheduler)
		circuit.executionInitialize(scheduler)
		circuit.executionStart(scheduler, null)
		proceedUntilQueueEmpty()
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