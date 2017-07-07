package ch.scorpion.jabbah.animation

import com.nhaarman.mockito_kotlin.whenever
import com.nhaarman.mockito_kotlin.mock
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.ControlledTimer
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for [AnimatorImpl].
 */
class AnimatorImplTest {

    lateinit var timeService: ControlledTimeService
    lateinit var animator: AnimatorImpl

    @Before
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

        `verify`(task, `never`()).animate(`anyDouble`())
    }

    @Test
    fun shouldAnimateAfterDelay() {
        val task = createTask(1000.0, 100.0)
        animator.schedule(task)
        task.start()
        timeService.setTimeMillis(150)

        `verify`(task, `times`(1)).animate(`anyDouble`())
    }

    @Test
    fun shouldAnimateRepeatedly() {
        val task = createTask(1000.0, 100.0)
        animator.schedule(task)
        task.start()

        timeService.setTimeMillis(150)
        `verify`(task, `times`(1)).animate(10.0)

        timeService.setTimeMillis(250)
        `verify`(task, `times`(2)).animate(10.0)

        timeService.setTimeMillis(350)
        `verify`(task, `times`(3)).animate(10.0)
    }

    @Test
    fun shouldStopTask() {
        val task = createTask(1000.0, 100.0)
        animator.schedule(task)
        task.start()

        assertThat(animator.taskCount, `is`(1))
        timeService.setTimeMillis(150)
        `verify`(task, `times`(1)).animate(10.0)

        task.stop()
        assertThat(animator.taskCount, `is`(0))
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

        assertThat(animator.taskCount, `is`(0))
    }

    private fun createTask(duration: Double, size: Double): AnimationTask {
        val task = mock<TestTask>()

        whenever(task.duration).thenReturn(duration)
        whenever(task.size).thenReturn(size)
        `doCallRealMethod`().`when`(task).start()
        `doCallRealMethod`().`when`(task).stop()
        `doCallRealMethod`().`when`(task).addListener(animator.taskListener)

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