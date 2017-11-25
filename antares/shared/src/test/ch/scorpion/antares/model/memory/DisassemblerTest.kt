package ch.scorpion.antares.model.memory

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

/** Unit tests for [Disassembler].*/
class DisassemblerTest {

    companion object {

        /** The macro-architecture operations of Tannenbaum's "Structured Computer Organization".*/
        private val MAR_OPS = """
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

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun playground() {
        val regex = "0([A-F0-9]{3})".toRegex()
        assertThat(regex.matches("0FFF"), `is`(true))
        val disassembly = regex.replaceFirst("0FFF", "LODD $1")
        assertThat(disassembly, `is`("LODD FFF"))
    }

    @Test
    fun testPatternAndResult() {
        assertThat(Disassembler().operation("0([A-F0-9]{3})", "LODD $1").disassemble("0FFF"), `is`("LODD FFF"))
    }

    @Test
    fun testCompositeExpression() {
        assertThat(Disassembler().operation("0([A-F0-9]{3})=LODD $1").disassemble("0FFF"), `is`("LODD FFF"))
    }

    @Test
    fun testMAR() {
        val disassembler = Disassembler().operations(MAR_OPS)
        assertThat(disassembler.disassemble("0FFF"), `is`("LODD FFF"))
        assertThat(disassembler.disassemble("1ABC"), `is`("STOD ABC"))
        assertThat(disassembler.disassemble("203C"), `is`("ADDD 03C"))
        assertThat(disassembler.disassemble("3001"), `is`("SUBD 001"))
        assertThat(disassembler.disassemble("4102"), `is`("JPOS 102"))
        assertThat(disassembler.disassemble("5EEE"), `is`("JZER EEE"))
        assertThat(disassembler.disassemble("6900"), `is`("JUMP 900"))
        assertThat(disassembler.disassemble("7002"), `is`("LOCO 002"))
        assertThat(disassembler.disassemble("8003"), `is`("LODL 003"))
        assertThat(disassembler.disassemble("9004"), `is`("STOL 004"))
        assertThat(disassembler.disassemble("A005"), `is`("ADDL 005"))
        assertThat(disassembler.disassemble("B006"), `is`("SUBL 006"))
        assertThat(disassembler.disassemble("C007"), `is`("JNEG 007"))
        assertThat(disassembler.disassemble("D008"), `is`("JNZE 008"))
        assertThat(disassembler.disassemble("E111"), `is`("CALL 111"))
        assertThat(disassembler.disassemble("F000"), `is`("PSHI"))
        assertThat(disassembler.disassemble("F200"), `is`("POPI"))
        assertThat(disassembler.disassemble("F400"), `is`("PUSH"))
        assertThat(disassembler.disassemble("F600"), `is`("POP"))
        assertThat(disassembler.disassemble("F800"), `is`("RETN"))
        assertThat(disassembler.disassemble("FA00"), `is`("SWAP"))
        assertThat(disassembler.disassemble("FC12"), `is`("INSP 12"))
        assertThat(disassembler.disassemble("FE01"), `is`("DESP 01"))
        assertThat(disassembler.disassemble("FF00"), `is`("HALT"))
    }
}