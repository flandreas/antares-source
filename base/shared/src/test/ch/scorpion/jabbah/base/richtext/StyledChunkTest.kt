package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.parser.TextLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StyledChunkTest {

	@Test
	fun shouldSplitWords() {
		val chunk = StyledChunk(TextLocation(0, 0, 0), "This is a text.", TextStyle(overlineLevel = 1, bold = true, italic = false))
		val words = chunk.splitWords()

		assertEquals(4, words.size)
		assertEquals("This ", words[0].text)
		assertEquals("is ", words[1].text)
		assertEquals("a ", words[2].text)
		assertEquals("text.", words[3].text)
		assertTrue(words.all { it.style.bold && it.style.overlineLevel == 1 })
	}
}