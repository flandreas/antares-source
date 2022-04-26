package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.edit.EditTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Unit tests for [ReferenceColorSequenceImpl].
 */
class ReferenceColorSequenceImplTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val sequence = ReferenceColorSequenceImpl(listOf(
		ReferenceColor(CompositeColor(foregroundColor = Color.BLACK)),
		ReferenceColor(CompositeColor(foregroundColor = Color.WHITE)))
	)

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