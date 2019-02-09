package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [AnimatorImpl].
 */
class AnimatorImplTest {

    lateinit var timeService: ControlledTimeService
    lateinit var animator: AnimatorImpl

    @BeforeTest
    fun init() {
        BaseModuleJvm.require()
        timeService = ControlledTimeService()
        animator = AnimatorImpl(ControlledTimer(timeService), 100)
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

    private fun createTask(duration: Double, size: Double): AnimationTask {
        val task = spyk<TestTask>()

	    every { task.duration } returns duration
	    every { task.size } returns size
	    every { task.dependsOnSystemSpeed } returns false

	    /*
        `doCallRealMethod`().`when`(task).start()
        `doCallRealMethod`().`when`(task).stop()
        `doCallRealMethod`().`when`(task).addListener(animator.taskListener)
		*/

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

		override fun addListener(listener: AnimationTaskListener) {
			this.listener = listener
		}
	}
}