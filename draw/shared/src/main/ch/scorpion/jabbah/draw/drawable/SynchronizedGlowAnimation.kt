package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.animation.AbstractAnimationTask
import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.DoubleRange
import ch.scorpion.jabbah.animation.SequenceType
import ch.scorpion.jabbah.base.logger

/**
 * Features a single [AbstractAnimationTask] to produce a glow effect on multiple [Transparent]s,
 * thereby making sure that their glowing is synchronized in order to avoid too much visual disturbance.
 */
object SynchronizedGlowAnimation {

	private val LOG by logger(SynchronizedGlowAnimation::class)

	private class GlowTask : AbstractAnimationTask<Double>(
		SynchronizedGlowAnimation,
		SynchronizedGlowAnimation::consume,
		DoubleRange(Transparent.FULLY_OPAQUE.toDouble(), 16.0, SequenceType.OSCILLATION),
		300.0,
		key = "SynchronizedGlow"
	)

	private val task = GlowTask()

	private var transparency: Double = Transparent.FULLY_OPAQUE.toDouble()

	private var isRunning = false

	private val transparentList = mutableListOf<Transparent>()

	fun add(transparent: Transparent) {
		if (!transparentList.contains(transparent)) {
			LOG.trace("Add glowing object")
			transparentList.add(transparent)
		}
		startIfNeeded()
	}

	fun remove(transparent: Transparent) {
		LOG.trace("Remove glowing object")
		transparentList.remove(transparent)
		stopIfNeeded()
		transparent.transparency = Transparent.FULLY_OPAQUE
		transparent.validate()
	}

	fun removeAll() {
		LOG.trace("Remove all glowing object")
		transparentList.forEach {
			it.transparency = Transparent.FULLY_OPAQUE
			it.validate()
		}
		transparentList.clear()
		stopIfNeeded()
	}

	fun consume(value: Double) {
		transparency = value
		transparentList.forEach {
			it.transparency = transparency.toInt()
			it.validate()
		}
	}

	private fun startIfNeeded() {
		if (!isRunning) {
			LOG.trace("Start GlowAnimation task")
			if (AnimationModule.animator.getTasksForTarget(SynchronizedGlowAnimation).isEmpty()) {
				AnimationModule.animator.schedule(task)
			}
			task.start()
			isRunning = true
		}
	}

	private fun stopIfNeeded() {
		if (transparentList.isEmpty()) {
			LOG.trace("Stop GlowAnimation task")
			task.stop()
			isRunning = false
		}
	}
}