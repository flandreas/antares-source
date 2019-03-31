package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.exception.IllegalStateException
import kotlin.test.Test
import kotlin.test.assertEquals
/**
 * Unit tests for [ReferenceColorSequenceImpl].
 */
class ReferenceColorSequenceImplTest {

    private val sequence = ReferenceColorSequenceImpl(listOf(
        CompositeColor(foregroundColor = Color.BLACK),
        CompositeColor(foregroundColor = Color.WHITE))
    )

    @Test
    fun shouldFetchNext() {
        assertEquals(Color.BLACK, sequence.next().foregroundColor)
	    assertEquals(Color.WHITE, sequence.next().foregroundColor)
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

    @Test(expected = IllegalStateException::class)
    fun shouldRefuseToFreeUnfetched() {
        sequence.free(CompositeColor(foregroundColor = Color.BLACK))
    }
}