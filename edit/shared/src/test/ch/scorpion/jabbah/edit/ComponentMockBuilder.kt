package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Rectangle2D
import io.mockk.every
import io.mockk.mockk

/**
 * A builder for mocks of [Component].
 */
class ComponentMockBuilder {

    private val component = mockk<Component>(relaxed = true)

    init {
        every { component.boundingBox } returns Rectangle2D()
        every { component.visible } returns true
    }

    fun withId(id: Int): ComponentMockBuilder {
        every { component.id } returns id
        return this
    }

    fun build() = component
}