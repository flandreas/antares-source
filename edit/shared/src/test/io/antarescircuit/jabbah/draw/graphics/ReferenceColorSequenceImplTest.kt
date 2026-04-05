package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.edit.EditTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReferenceColorSequenceImplTest {

	private val sequence: ReferenceColorSequenceImpl

	init {
		EditTestRule.configure()
		sequence = ReferenceColorSequenceImpl(listOf(
			ReferenceColor(CompositeColor(foregroundColor = Color.BLACK)),
			ReferenceColor(CompositeColor(foregroundColor = Color.WHITE)))
		)
	}

	@Test
	fun shouldFetchNext() {
		assertEquals(Color.BLACK, sequence.next().onBackground.foregroundColor)
		assertEquals(Color.WHITE, sequence.next().onBackground.foregroundColor)
	}

	@Test
	fun shouldFree() {
		val color = sequence.next()
		sequence.free(color)
		assertEquals(color, sequence.next())
	}

	@Test
	fun shouldWrapAround() {
		val firstColor = sequence.next()
		sequence.next()
		assertEquals(firstColor, sequence.next())
	}

	@Test
	fun shouldRefuseToFreeUnfetched() {
		assertFailsWith<IllegalStateException> {
			sequence.free(ReferenceColor(CompositeColor(foregroundColor = Color.BLACK)))
		}
	}
}