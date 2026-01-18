package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.expression.assertAST
import ch.scorpion.antares.model.signal.CurrentDigitalSignalNotation
import ch.scorpion.antares.model.signal.DigitalSignalNotation
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestcaseParserTest {

	@BeforeTest
	fun setUp() {
		AntaresTestRule.configure()
		CurrentDigitalSignalNotation.notation = DigitalSignalNotation.PREFIX
	}

	@Test
	fun shouldParseNameRow() {
		val parser = TestcaseParser("""
			A B 'O o'
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,B,O o
		""".trimIndent())
	}

	@Test
	fun shouldParseTruthTable() {
		val parser = TestcaseParser("""
			A B O
			0 0 0
			0 1 1
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,B,O
			- TestVector
			-- 0
			-- 0
			-- 0
			- TestVector
			-- 0
			-- 1
			-- 1
		""".trimIndent())
	}

	@Test
	fun shouldSkipEmptyLines() {
		val parser = TestcaseParser("""
			A B O
			
			0 0 0
			
			0 1 0
			
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,B,O
			- TestVector
			-- 0
			-- 0
			-- 0
			- TestVector
			-- 0
			-- 1
			-- 0
		""".trimIndent())
	}

	@Test
	fun shouldParseDontCare() {
		val parser = TestcaseParser("""
			A B O
			0 0 X
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,B,O
			- TestVector
			-- 0
			-- 0
			-- X
		""".trimIndent())
	}

	@Test
	fun shouldParseUndefined() {
		val parser = TestcaseParser("""
			A B O
			0 Z z
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,B,O
			- TestVector
			-- 0
			-- Z
			-- Z
		""".trimIndent())
	}

	@Test
	fun shouldParseClocks() {
		val parser = TestcaseParser("""
			A B O
			^1 0 1
			^0 0 1
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,B,O
			- TestVector
			-- ^1
			-- 0
			-- 1
			- TestVector
			-- ^0
			-- 0
			-- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseHexValues() {
		val parser = TestcaseParser("""
			A B O
			0xFF 0x0 0xA
		""".trimIndent())

		// Prefix is only added if digits are not distinct
		assertAST(parser.parse(), """
			TestScript
			- A,B,O
			- TestVector
			-- 0xFF
			-- 0
			-- A
		""".trimIndent())
	}

	@Test
	fun shouldParseBinaryValues() {
		val parser = TestcaseParser("""
			A B O
			0b11 0b0100 0b11111111
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,B,O
			- TestVector
			-- 0b11
			-- 0b100
			-- 0b11111111
		""".trimIndent())
	}

	@Test
	fun shouldParseBinaryUndefined() {
		val parser = TestcaseParser("""
			A O
			0bZ1 0b0Z0
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- A,O
			- TestVector
			-- 0bZ1
			-- 0bZ0
		""".trimIndent())
	}

	@Test
	fun shouldParseRunBlock() {
		val parser = TestcaseParser("""
			I O
			1 0
			run {
				0 1
				1 1
			}
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- I,O
			- TestVector
			-- 1
			-- 0
			- Run
			-- TestVector
			--- 0
			--- 1
			-- TestVector
			--- 1
			--- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseInOutAsInput() {
		val parser = TestcaseParser("""
			I >IO O
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- I,>IO,O
		""".trimIndent())
	}

	@Test
	fun shouldParseInOutAsOutput() {
		val parser = TestcaseParser("""
			I <IO O
		""".trimIndent())

		assertAST(parser.parse(), """
			TestScript
			- I,<IO,O
		""".trimIndent())
	}
}