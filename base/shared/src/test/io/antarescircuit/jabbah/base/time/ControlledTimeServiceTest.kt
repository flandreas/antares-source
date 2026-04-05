package io.antarescircuit.jabbah.base.time

import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Unit tests for [ControlledTimeService].*/
class ControlledTimeServiceTest {

    val service: ControlledTimeService
    var event: PropertyChangeEvent<Long>? = null

    init {
        BaseModule.require()
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

    @Test
    fun shouldRejectPast() {
	    assertFailsWith<IllegalArgumentException> {
	        service.setTimeMillis(100)
	        service.setTimeMillis(50)
	    }
    }
}