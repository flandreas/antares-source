package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.exception.IllegalStateException
import org.junit.Assert.*
import org.junit.Test
import org.hamcrest.CoreMatchers.`is`

/**
 * Unit tests for [ReferenceColorSequence]
 */
class ReferenceColorSequenceTest {

    private val sequence = ReferenceColorSequence(listOf(
        CompositeColor(foregroundColor = Color.BLACK),
        CompositeColor(foregroundColor = Color.WHITE))
    )

    @Test
    fun shouldFetchNext() {
        assertThat(sequence.next().foregroundColor, `is`(Color.BLACK))
        assertThat(sequence.next().foregroundColor, `is`(Color.WHITE))
    }

    @Test
    fun shouldFree() {
        val color = sequence.next()
        sequence.free(color)
        assertThat(sequence.next(), `is`(color))
    }

    @Test
    fun shouldWrapAround() {
        val firstColor = sequence.next()
        sequence.next()
        assertThat(sequence.next(), `is`(firstColor))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldRefuseToFreeUnfetched() {
        sequence.free(CompositeColor(foregroundColor = Color.BLACK))
    }
}