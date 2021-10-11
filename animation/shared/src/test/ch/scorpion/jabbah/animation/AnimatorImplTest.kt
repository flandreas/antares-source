package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.time.SystemSpeed
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimatorImplTest {

	private lateinit var timeService: ControlledTimeService
	private lateinit var animator: AnimatorImpl

	@BeforeTest
	fun init() {
		BaseModule.require()
		timeService = ControlledTimeService()
		animator = AnimatorImpl(SystemSpeed(), ControlledTimer(timeService), 100)
	}

	@Test
	fun shouldNotAnimateBeforeDelay() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(50)

		verify(exactly = 0) { task.animate(any()) }
	}

	@Test
	fun shouldAnimateAfterDelay() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(150)

		verify(exactly = 1) { task.animate(any()) }
	}

	@Test
	fun shouldAnimateRepeatedly() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()

		timeService.setTimeMillis(150)
		verify(exactly = 1) { task.animate(10.0) }

		timeService.setTimeMillis(250)
		verify(exactly = 2) { task.animate(10.0) }

		timeService.setTimeMillis(350)
		verify(exactly = 3) { task.animate(10.0) }
	}

	@Test
	fun shouldStopTask() {
		val task = createTask(1000.0, 100.0)
		animator.schedule(task)
		task.start()

		assertEquals(1, animator.taskCount)
		timeService.setTimeMillis(150)
		verify(exactly = 1) { task.animate(10.0) }

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
		verify(exactly = 1) { task.animate(any()) }

		animator.systemSpeed.pause()
		timeService.setTimeMillis(250)

		verify(exactly = 1) { task.animate(any()) }
	}

	@Test
	fun shouldResumePausableTask() {
		val task = createTask(1000.0, 100.0, pausable = true)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(150)
		verify(exactly = 1) { task.animate(any()) }

		animator.systemSpeed.pause()
		timeService.setTimeMillis(250)

		animator.systemSpeed.resume()
		timeService.setTimeMillis(350)

		verify(exactly = 2) { task.animate(any()) }
	}

	@Test
	fun shouldNotPauseNonPausableTask() {
		val task = createTask(1000.0, 100.0, pausable = false)
		animator.schedule(task)
		task.start()
		timeService.setTimeMillis(150)
		verify(exactly = 1) { task.animate(any()) }

		animator.systemSpeed.pause()
		timeService.setTimeMillis(250)

		verify(exactly = 2) { task.animate(any()) }
	}

	private fun createTask(duration: Double, size: Double, pausable: Boolean = false): AnimationTask {
		val task = spyk<TestTask>()

		every { task.duration } returns duration
		every { task.size } returns size
		every { task.dependsOnSystemSpeed } returns false
		every { task.isPausable } returns pausable

		return task
	}

	abstract class TestTask : AnimationTask {
		var listener: AnimationTaskListener? = null

		override fun start() {
			listener!!.started(this)
		}

		override fun stop() {
			listener!!.ended(this)
		}

		override fun addListener(listener: AnimationTaskListener): AnimationTask {
			this.listener = listener
			return this
		}
	}
}