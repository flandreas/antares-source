package ch.scorpion.antares.model.hdl.vhdl

import ch.scorpion.antares.hdl.vhdl.VHDLRenaming
import kotlin.test.Test
import kotlin.test.assertEquals

class VHDLRenamingTest {

	private val renaming = VHDLRenaming()

	@Test
	fun shouldNotAdjustValidName() {
		assertEquals("a", renaming.checkName("a"))
	}

	@Test
	fun shouldAdjustLeadingDigit() {
		assertEquals("n0a", renaming.checkName("0a"))
	}

	@Test
	fun shouldAdjustKeyword() {
		assertEquals("p_in", renaming.checkName("in"))
		assertEquals("p_architecture", renaming.checkName("architecture"))
		assertEquals("p_xor", renaming.checkName("xor"))
	}

	@Test
	fun shouldReplaceBlanksWithUnderscores() {
		assertEquals("a_b", renaming.checkName("a b"))
	}

	@Test
	fun shouldRemoveQuotes() {
		assertEquals("a", renaming.checkName("\"a\""))
		assertEquals("a", renaming.checkName("'a'"))
	}

	@Test
	fun shouldReplaceNegation() {
		assertEquals("nota", renaming.checkName("!a"))
	}

	@Test
	fun shouldRemoveRichTextQualifiers() {
		assertEquals("a_1", renaming.checkName("a_1"))
		assertEquals("a_1", renaming.checkName("a/1"))
		assertEquals("a_1", renaming.checkName("a^1"))
	}

	@Test
	fun shouldReplaceComparisons() {
		assertEquals("a_eq_b", renaming.checkName("a=b"))
		assertEquals("a_gt_b", renaming.checkName("a>b"))
		assertEquals("a_lt_b", renaming.checkName("a<b"))
	}
}