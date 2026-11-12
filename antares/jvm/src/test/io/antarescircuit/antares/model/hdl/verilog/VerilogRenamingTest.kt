package io.antarescircuit.antares.model.hdl.verilog

import io.antarescircuit.antares.hdl.verilog.VerilogRenaming
import kotlin.test.Test
import kotlin.test.assertEquals

class VerilogRenamingTest {

	private val renaming = VerilogRenaming()

	@Test
	fun shouldNotAdjustValidName() {
		assertEquals("a_1", renaming.checkName("a_1"))
	}

	@Test
	fun shouldAdjustLeadingDigit() {
		assertEquals("n0a", renaming.checkName("0a"))
	}

	@Test
	fun shouldAdjustKeyword() {
		assertEquals("p_module", renaming.checkName("module"))
		assertEquals("p_wire", renaming.checkName("wire"))
		assertEquals("p_uwire", renaming.checkName("uwire"))
	}

	@Test
	fun shouldMatchKeywordsCaseSensitively() {
		assertEquals("Input", renaming.checkName("Input"))
		assertEquals("WIRE", renaming.checkName("WIRE"))
	}

	@Test
	fun shouldKeepVHDLKeywords() {
		assertEquals("entity", renaming.checkName("entity"))
		assertEquals("signal", renaming.checkName("signal"))
	}

	@Test
	fun shouldAdjustKeywordResultingFromCleanup() {
		assertEquals("p_reg", renaming.checkName("reg "))
	}

	@Test
	fun shouldReplaceIllegalCharacters() {
		assertEquals("a_b", renaming.checkName("a b"))
		assertEquals("a", renaming.checkName("a$"))
		assertEquals("nota", renaming.checkName("!a"))
		assertEquals("a_eq_b", renaming.checkName("a=b"))
	}
}
