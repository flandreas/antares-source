package ch.scorpion.jabbah.edit

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * A builder for mocks of [Component].
 */
class ComponentMockBuilder {

    private val component = mock<Component>()

    init {
        whenever(component.boundingBox).thenReturn(Rectangle2D())
        whenever(component.visible).thenReturn(true)
    }

    fun withId(id: Int): ComponentMockBuilder {
        whenever(component.id).thenReturn(id)
        return this
    }

    fun build() = component
}