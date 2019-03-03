package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentMockBuilder
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

/**
 * Unit tests for [ComponentContainerImpl].
 */
class ComponentContainerImplTest {

    @BeforeTest
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
        verify(exactly = 1) { c1.id = 1 }
        verify(exactly = 1) { c2.id = 2 }
    }

    @Test
    fun shouldGetWithId() {
        val c1 = ComponentMockBuilder().withId(1).build()
        val c2 = ComponentMockBuilder().withId(2).build()
        val container = ComponentContainerImpl<Component>()
        container.add(c1)
        container.add(c2)

        assertSame(c2, container.getWithId(2))
    }
}