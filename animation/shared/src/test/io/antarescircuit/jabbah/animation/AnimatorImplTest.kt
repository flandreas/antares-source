package io.antarescircuit.jabbah.animation

import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.ControlledTimeService
import io.antarescircuit.jabbah.base.time.ControlledTimer
import io.antarescircuit.jabbah.base.time.SystemSpeed
import dev.mokkery.matcher.any
import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimatorImplTest {

	private lateinit var timeService: io.antarescircuit.jabbah.base.time.ControlledTimeService
	private lateinit var animator: io.antarescircuit.jabbah.animation.AnimatorImpl

	@BeforeTest
	fun init() {
		_root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.require()
		timeService = _root_ide_package_.io.antarescircuit.jabbah.base.time.ControlledTimeService()
		animator = _root_ide_package_.io.antarescircuit.jabbah.animation.AnimatorImpl(
            _root_ide_package_.io.antarescircuit.jabbah.base.time.SystemSpeed(),
            _root_ide_package_.io.antarescircuit.jabbah.base.time.ControlledTimer(timeService),
            100
        )
	}

	@Test
	fun shouldNotAnimateBeforeDelay() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(50)

		verify(exactly(0)) { task.animate(any()) }
	}

	@Test
	fun shouldAnimateAfterDelay() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(150)

		verify(exactly(1)) { task.animate(any()) }
	}

	@Test
	fun shouldAnimateRepeatedly() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()

		timeService.setTimeMillis(150)
		verify(exactly(1)) { task.animate(10.0) }

		timeService.setTimeMillis(250)
		verify(exactly(1)) { task.animate(10.0) }

		timeService.setTimeMillis(350)
		verify(exactly(1)) { task.animate(10.0) }
	}

	@Test
	fun shouldStopTask() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()

		assertEquals(1, animator.taskCount)
		timeService.setTimeMillis(150)
		verify(exactly(1)) { task.animate(10.0) }

		task.stop()
		assertEquals(0, animator.taskCount)
	}

	@Test
	fun shouldStopAllTasks() {
		val task1 = createTask(1000.0, 100.0)
		val task2 = createTask(1000.0, 100.0)
		animator.schedule(task1)
		animator.schedule(task2)
		task1.start()
		task2.start()

		animator.stopAllTasks()

		assertEquals(0, animator.taskCount)
	}

	@Test
	fun shouldPausePausableTask() {
		val task = createTask(1000.0, 100.0, pausable = true)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(150)
		verify(exactly(1)) { task.animate(any()) }

		animator.systemSpeed.pause()
		timeService.setTimeMillis(250)

		verify(exactly(0)) { task.animate(any()) }
	}

	@Test
	fun shouldResumePausableTask() {
		val task = createTask(1000.0, 100.0, pausable = true)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(150)
		verify(exactly(1)) { task.animate(any()) }

		animator.systemSpeed.pause()
		timeService.setTimeMillis(250)

		animator.systemSpeed.resume()
		timeService.setTimeMillis(350)

		verify(exactly(1)) { task.animate(any()) }
	}

	@Test
	fun shouldNotPauseNonPausableTask() {
		val task = createTask(1000.0, 100.0, pausable = false)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(150)
		verify(exactly(1)) { task.animate(any()) }

		animator.systemSpeed.pause()
		timeService.setTimeMillis(250)

		verify(exactly(1)) { task.animate(any()) }
	}

	private fun createTask(duration: Double, size: Double, pausable: Boolean = false): io.antarescircuit.jabbah.animation.AnimationTask {
		val spiedObject = TestTask(duration, size, pausable)
		val task = spy<io.antarescircuit.jabbah.animation.AnimationTask>(spiedObject)
		spiedObject.outerTask = task

		return task
	}

	private class TestTask(
		override val duration: Double,
		override val size: Double,
		override val isPausable: Boolean
	) : io.antarescircuit.jabbah.animation.AnimationTask {
		lateinit var outerTask: io.antarescircuit.jabbah.animation.AnimationTask
		var listener: io.antarescircuit.jabbah.animation.AnimationTaskListener? = null

		override fun start() {
			listener!!.started(outerTask)
		}

		override fun stop() {
			listener!!.ended(outerTask)
		}

		override fun cancel() {
			listener!!.ended(outerTask, true)
		}

		override fun addListener(listener: io.antarescircuit.jabbah.animation.AnimationTaskListener): io.antarescircuit.jabbah.animation.AnimationTask {
			this.listener = listener
			return this
		}

		override fun removeListener(listener: io.antarescircuit.jabbah.animation.AnimationTaskListener) {
			this.listener = null
		}

		override fun animate(distance: Double) {}
		override fun scheduled() {}
		override val target: Any get() = outerTask
		override val dependsOnSystemSpeed: Boolean get() = false
		override val key: String? get() = null
	}
}