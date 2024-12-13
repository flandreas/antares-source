package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.dsl.assertAST
import kotlin.test.*

class RichTextParserTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	@Test
	fun shouldParseSimpleText() {
		assertAST(
			RichTextParser("This is a text").parse(), """
			Compound
			- Fragment
			-- -
			--- This is a text
		""".trimIndent())
	}

	@Test
	fun shouldParseSingleOverlineText() {
		assertAST(
			RichTextParser("!O").parse(), """
			Compound
			- Fragment
			-- -
			--- !(O)
		""".trimIndent())
	}

	@Test
	fun shouldParseMultipleOverlineText() {
		assertAST(
			RichTextParser("!(ABC)").parse(), """
			Compound
			- Fragment
			-- -
			--- !(ABC)
		""".trimIndent())
	}

	@Test
	fun shouldParseMixedOverlineText() {
		assertAST(
			RichTextParser("!AB!(CD)").parse(), """
			Compound
			- Fragment
			-- -
			--- !(A)
			- Fragment
			-- -
			--- B
			- Fragment
			-- -
			--- !(CD)
		""".trimIndent())
	}

	@Test
	fun shouldParseSingleSubscript() {
		assertAST(
			RichTextParser("A_1").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseMultipleSubscript() {
		assertAST(
			RichTextParser("A_(123)").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- 123
		""".trimIndent())
	}

	@Test
	fun shouldParseSingleSuperscript() {
		assertAST(
			RichTextParser("A^1").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- ^
			--- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseMultipleSuperscript() {
		assertAST(
			RichTextParser("A^(123)").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- ^
			--- 123
		""".trimIndent())
	}

	@Test
	fun shouldParseSubAndSuperscript() {
		assertAST(
			RichTextParser("A_1^2").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- 1
			-- ^
			--- 2
		""".trimIndent())
	}

	@Test
	fun shouldParseSuperscriptAndSubscript() {
		assertAST(
			RichTextParser("A^1_2").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- 2
			-- ^
			--- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseOverlineSingleSubscript() {
		assertAST(
			RichTextParser("A_!1").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- !(1)
		""".trimIndent())
	}

	@Test
	fun shouldParseOverlineMultipleSubscript() {
		assertAST(
			RichTextParser("A_!(123)").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- !(123)
		""".trimIndent())
	}

	@Test
	fun shouldParseOverlineMixedSubscript() {
		assertAST(
			RichTextParser("A_(1!(23))").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- 1
			--- !(23)
		""".trimIndent())
	}

	@Test
	fun shouldParseOverlineSingleSuperscript() {
		assertAST(
			RichTextParser("A^!1").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- ^
			--- !(1)
		""".trimIndent())
	}

	@Test
	fun shouldParseOverlineMultipleSuperscript() {
		assertAST(
			RichTextParser("A^!(123)").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- ^
			--- !(123)
		""".trimIndent())
	}

	@Test
	fun shouldParseNestedOverline() {
		val ast = RichTextParser("!(A !(!B) !C) !D").parse()
		assertAST(
			ast, """
			Compound
			- Fragment
			-- -
			--- !(A )
			--- !!!(B)
			--- !( )
			--- !!(C)
			- Fragment
			-- -
			---  
			- Fragment
			-- -
			--- !(D)
		""".trimIndent())
		assertEquals(ast.getMaxOverlineLevel(), 3)
	}

	@Test
	fun shouldParseOverlineSubAndSuperscript() {
		assertAST(
			RichTextParser("ABC_!1^!2").parse(), """
			Compound
			- Fragment
			-- -
			--- ABC
			-- _
			--- !(1)
			-- ^
			--- !(2)
		""".trimIndent())
	}

	@Test
	fun shouldParseComplexText() {
		assertAST(
			RichTextParser("A!BC!(DE)F_(12!(34)5)^(1!(23)45)").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			- Fragment
			-- -
			--- !(B)
			- Fragment
			-- -
			--- C
			- Fragment
			-- -
			--- !(DE)
			- Fragment
			-- -
			--- F
			-- _
			--- 12
			--- !(34)
			--- 5
			-- ^
			--- 1
			--- !(23)
			--- 45
		""".trimIndent())
	}

	@Test
	fun shouldParseMultipleFragments() {
		assertAST(
			RichTextParser("A_1B_1").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- 1
			- Fragment
			-- -
			--- B
			-- _
			--- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseSecondFragmentWithSingleChunk() {
		assertAST(
			RichTextParser("ABC_1DE").parse(), """
			Compound
			- Fragment
			-- -
			--- ABC
			-- _
			--- 1
			- Fragment
			-- -
			--- DE
		""".trimIndent())
	}

	@Test
	fun shouldEscapeInText() {
		assertAST(
			RichTextParser("\\(\\!\\_\\^\\\\\\)").parse(), """
			Compound
			- Fragment
			-- -
			--- (!_^\)
		""".trimIndent())
	}

	@Test
	fun shouldEscapeInSingleChar() {
		assertAST(
			RichTextParser("A_\\!").parse(), """
			Compound
			- Fragment
			-- -
			--- A
			-- _
			--- !
		""".trimIndent())
	}

	@Test
	fun shouldEscapeEscape() {
		assertAST(
			RichTextParser("\\\\").parse(), """
			Compound
			- Fragment
			-- -
			--- \
		""".trimIndent())
	}

	@Test
	fun shouldParseBold() {
		assertAST(
			RichTextParser("This is *(bold)").parse(), """
			Compound
			- Fragment
			-- -
			--- This is 
			- Fragment
			-- -
			--- *(bold)
		""".trimIndent())
	}

	@Test
	fun shouldParseBoldOverline() {
		assertAST(
			RichTextParser("This is *(bold !(overline))").parse(), """
			Compound
			- Fragment
			-- -
			--- This is 
			- Fragment
			-- -
			--- *(bold )
			- Fragment
			-- -
			--- *!(overline)
		""".trimIndent())
	}

	@Test
	fun shouldParseBoldFragment() {
		assertAST(
			RichTextParser("*(A_1^2)").parse(), """
				Compound
				- Fragment
				-- -
				--- *(A)
				-- _
				--- *(1)
				-- ^
				--- *(2)
			""".trimIndent())
	}

	@Test
	fun shouldParseItalic() {
		assertAST(
			RichTextParser("This is /(italic)").parse(), """
			Compound
			- Fragment
			-- -
			--- This is 
			- Fragment
			-- -
			--- /(italic)
		""".trimIndent())
	}

	@Test
	fun shouldParseBoldItalic() {
		assertAST(
			RichTextParser("This is *(/(great))").parse(), """
			Compound
			- Fragment
			-- -
			--- This is 
			- Fragment
			-- -
			--- */(great)
		""".trimIndent())
	}

	@Test
	fun shouldParsePortTooltip() {
		assertAST(
			RichTextParser("*(Input '!A_1')").parse(), """
			Compound
			- Fragment
			-- -
			--- *(Input ')
			- Fragment
			-- -
			--- *!(A)
			-- _
			--- *(1)
			- Fragment
			-- -
			--- *(')
		""".trimIndent())
	}

	@Test
	fun shouldParseParenWithoutOperator() {
		assertAST(
			RichTextParser("A(1)").parse(), """
			Compound
			- Fragment
			-- -
			--- A(1)
		""".trimIndent())
	}

	@Test
	fun shouldParseBoldParenWithoutOperator() {
		assertAST(
			RichTextParser("*(A(1))").parse(), """
			Compound
			- Fragment
			-- -
			--- *(A(1))
		""".trimIndent())
	}

	@Test
	fun shouldParseComplexFragments() {
		assertAST(
			RichTextParser("LED: *(A_(123)^(456)): Bla").parse(), """
			Compound
			- Fragment
			-- -
			--- LED: 
			- Fragment
			-- -
			--- *(A)
			-- _
			--- *(123)
			-- ^
			--- *(456)
			- Fragment
			-- -
			--- : Bla
		""".trimIndent())
	}

	@Test
	fun bug613() {
		assertAST(
			RichTextParser("*(Input\\/Output)").parse(), """
			Compound
			- Fragment
			-- -
			--- *(Input/Output)
		""".trimIndent())

		assertAST(
			RichTextParser("Input\\/Output").parse(), """
			Compound
			- Fragment
			-- -
			--- Input/Output
		""".trimIndent())
	}

	/** Regression test bug #850. */
	@Test
	fun shouldThrowSyntaxErrorWithTrailingExclamationMark() {
		assertFailsWith(SyntaxError::class) {
			RichTextParser("Hallo!").parse()
		}
	}
}