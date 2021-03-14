package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentMockBuilder
import ch.scorpion.jabbah.edit.module.EditModule
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Unit tests for [ComponentContainerImpl].
 */
class ComponentContainerImplTest {

	private lateinit var container: ComponentContainerImpl<Component>

	@BeforeTest
	fun setup() {
		EditModule.require()
		container = ComponentContainerImpl()
	}

	@Test
	fun shouldSetMaxIdWhenAdded() {
		val c1 = ComponentMockBuilder().withId(1).build()
		val c2 = ComponentMockBuilder().withId(2).build()

		container.add(c1)
		container.add(c2)
		verify(exactly = 1) { c1.id = 1 }
		verify(exactly = 1) { c2.id = 2 }
	}

	@Test
	fun shouldGetWithId() {
		val c1 = ComponentMockBuilder().withId(1).build()
		val c2 = ComponentMockBuilder().withId(2).build()
		container.add(c1)
		container.add(c2)

		assertSame(c2, container.getWithId(2))
	}

	// Stacking order

	@Test
	fun shouldSetStackingOrderPositionLower() {
		val d1 = ComponentMockBuilder().withId(3).build()
		val d2 = ComponentMockBuilder().withId(2).build()
		val d3 = ComponentMockBuilder().withId(1).build()
		container.add(d3).add(d2).add(d1)

		container.setStackingOrderPosition(1, d3.id)

		assertEquals(0, container.getStackingOrderPosition(d1.id))
		assertEquals(1, container.getStackingOrderPosition(d3.id))
		assertEquals(2, container.getStackingOrderPosition(d2.id))
	}

	@Test
	fun shouldSetStackingOrderPositionHigher() {
		val d1 = ComponentMockBuilder().withId(3).build()
		val d2 = ComponentMockBuilder().withId(2).build()
		val d3 = ComponentMockBuilder().withId(1).build()
		container.add(d3).add(d2).add(d1)

		container.setStackingOrderPosition(2, d2.id)

		assertEquals(0, container.getStackingOrderPosition(d1.id))
		assertEquals(1, container.getStackingOrderPosition(d3.id))
		assertEquals(2, container.getStackingOrderPosition(d2.id))
	}

	@Test
	fun shouldSetStackingOrderPositionUnchanged() {
		val d1 = ComponentMockBuilder().withId(3).build()
		val d2 = ComponentMockBuilder().withId(2).build()
		val d3 = ComponentMockBuilder().withId(1).build()
		container.add(d3).add(d2).add(d1)

		container.setStackingOrderPosition(1, d2.id)

		assertEquals(0, container.getStackingOrderPosition(d1.id))
		assertEquals(1, container.getStackingOrderPosition(d2.id))
		assertEquals(2, container.getStackingOrderPosition(d3.id))
	}
}