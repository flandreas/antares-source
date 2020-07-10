package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Unit tests for [Disassembler].*/
class DisassemblerTest {

	companion object {

		/** The macro-architecture operations of Tannenbaum's "Structured Computer Organization".*/
		private const val MAR_OPS = """
            0([A-F0-9]{3})=LODD ${'$'}1
            1([A-F0-9]{3})=STOD ${'$'}1
            2([A-F0-9]{3})=ADDD ${'$'}1
            3([A-F0-9]{3})=SUBD ${'$'}1
            4([A-F0-9]{3})=JPOS ${'$'}1
            5([A-F0-9]{3})=JZER ${'$'}1
            6([A-F0-9]{3})=JUMP ${'$'}1
            7([A-F0-9]{3})=LOCO ${'$'}1
            8([A-F0-9]{3})=LODL ${'$'}1
            9([A-F0-9]{3})=STOL ${'$'}1
            A([A-F0-9]{3})=ADDL ${'$'}1
            B([A-F0-9]{3})=SUBL ${'$'}1
            C([A-F0-9]{3})=JNEG ${'$'}1
            D([A-F0-9]{3})=JNZE ${'$'}1
            E([A-F0-9]{3})=CALL ${'$'}1
            F0([A-F0-9]{2})=PSHI
            F2([A-F0-9]{2})=POPI
            F4([A-F0-9]{2})=PUSH
            F6([A-F0-9]{2})=POP
            F8([A-F0-9]{2})=RETN
            FA([A-F0-9]{2})=SWAP
            FC([A-F0-9]{2})=INSP ${'$'}1
            FE([A-F0-9]{2})=DESP ${'$'}1
            FF([A-F0-9]{2})=HALT
        """
	}

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	@Test
	fun playground() {
		val regex = "0([A-F0-9]{3})".toRegex()
		assertTrue(regex.matches("0FFF"))
		val disassembly = regex.replaceFirst("0FFF", "LODD $1")
		assertEquals("LODD FFF", disassembly)
	}

	@Test
	fun testPatternAndResult() {
		assertEquals("LODD FFF", Disassembler().operation("0([A-F0-9]{3})", "LODD $1").disassemble("0FFF"))
	}

	@Test
	fun testCompositeExpression() {
		assertEquals("LODD FFF", Disassembler().operation("0([A-F0-9]{3})=LODD $1").disassemble("0FFF"))
	}

	@Test
	fun testMAR() {
		val disassembler = Disassembler().operations(MAR_OPS)
		assertEquals("LODD FFF", disassembler.disassemble("0FFF"))
		assertEquals("STOD ABC", disassembler.disassemble("1ABC"))
		assertEquals("ADDD 03C", disassembler.disassemble("203C"))
		assertEquals("SUBD 001", disassembler.disassemble("3001"))
		assertEquals("JPOS 102", disassembler.disassemble("4102"))
		assertEquals("JZER EEE", disassembler.disassemble("5EEE"))
		assertEquals("JUMP 900", disassembler.disassemble("6900"))
		assertEquals("LOCO 002", disassembler.disassemble("7002"))
		assertEquals("LODL 003", disassembler.disassemble("8003"))
		assertEquals("STOL 004", disassembler.disassemble("9004"))
		assertEquals("ADDL 005", disassembler.disassemble("A005"))
		assertEquals("SUBL 006", disassembler.disassemble("B006"))
		assertEquals("JNEG 007", disassembler.disassemble("C007"))
		assertEquals("JNZE 008", disassembler.disassemble("D008"))
		assertEquals("CALL 111", disassembler.disassemble("E111"))
		assertEquals("PSHI", disassembler.disassemble("F000"))
		assertEquals("POPI", disassembler.disassemble("F200"))
		assertEquals("PUSH", disassembler.disassemble("F400"))
		assertEquals("POP", disassembler.disassemble("F600"))
		assertEquals("RETN", disassembler.disassemble("F800"))
		assertEquals("SWAP", disassembler.disassemble("FA00"))
		assertEquals("INSP 12", disassembler.disassemble("FC12"))
		assertEquals("DESP 01", disassembler.disassemble("FE01"))
		assertEquals("HALT", disassembler.disassemble("FF00"))
	}
}