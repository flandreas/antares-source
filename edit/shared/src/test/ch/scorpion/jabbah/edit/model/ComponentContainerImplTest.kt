package ch.scorpion.jabbah.edit.model

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentMockBuilder
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ComponentContainerImpl].
 */
class ComponentContainerImplTest {

    @Before
    fun setup() {
        EditModuleJvm.require()
    }

    @Test
    fun shouldSetMaxIdWhenAdded() {
        val c1 = ComponentMockBuilder().withId(1).build()
        val c2 = ComponentMockBuilder().build()
        val container = ComponentContainerImpl<Component>()
        container.add(c1)
        container.add(c2)
        verify(c1, times(1)).id = 1
        verify(c2, times(1)).id = 2
    }

    @Test
    fun shouldGetWithId() {
        val c1 = ComponentMockBuilder().withId(1).build()
        val c2 = ComponentMockBuilder().withId(2).build()
        val container = ComponentContainerImpl<Component>()
        container.add(c1)
        container.add(c2)

        assertThat(container.getWidthId(2), `is`(`sameInstance`(c2)))
    }
}