package ch.scorpion.jabbah.base.time

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [ControlledTimeService].
 */
class ControlledTimeServiceTest {

    val service: ControlledTimeService
    var event: PropertyChangeEvent<Long>? = null

    init {
        BaseModuleJvm.require()
        service = ControlledTimeService()
        service.addPropertyChangeListener(object : PropertyChangeListener<Long> {
            override fun propertyChanged(e: PropertyChangeEvent<Long>) {
                event = e
            }
        })
    }

    @Test
    fun shouldSetTime() {
        service.setTimeMillis(100)
        assertEquals(100, service.nowMillis())
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldRejectPast() {
        service.setTimeMillis(100)
        service.setTimeMillis(50)
    }
}