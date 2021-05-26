package ch.scorpion.jabbah.base.text

import ch.scorpion.jabbah.base.text.FormattedText.Companion.OVERLINE
import ch.scorpion.jabbah.base.text.FormattedText.Companion.replaceNegation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormattedTextTest {

	@Test
	fun shouldReplaceSingleNegation() {
		assertEquals("Q", replaceNegation("!Q").text)
		assertEquals("AQ${OVERLINE}B", replaceNegation("A!QB").text)
	}

	@Test
	fun shouldReplaceBlockNegation() {
		assertEquals("AB", replaceNegation("!(AB)").text)
		assertEquals("CA\u0305B\u0305", replaceNegation("C!(AB)").text)
		assertEquals("A\u0305B\u0305C", replaceNegation("!(AB)C").text)
	}

	@Test
	fun shouldNotReplaceBlockWithoutNegation() {
		assertEquals("Bla (Blu)", replaceNegation("Bla (Blu)").text)
	}

	@Test
	fun shouldCalculateAllNegated() {
		assertTrue(replaceNegation("!Q").allNegated)
		assertTrue(replaceNegation("!(ABC)").allNegated)

		assertFalse(replaceNegation("!AB").allNegated)
		assertFalse(replaceNegation("A!B").allNegated)
		assertFalse(replaceNegation("!(AB)C").allNegated)
		assertFalse(replaceNegation("!A(BC)").allNegated)
	}

	@Test
	fun shouldNotContainOverlineWhenAllNegated() {
		assertFalse(replaceNegation("!Q").text.contains(OVERLINE))
		assertFalse(replaceNegation("!(ABC)").text.contains(OVERLINE))
	}
}