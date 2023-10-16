package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.expression.assertAST
import kotlin.test.Test

class TestcaseParserTest {

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
}