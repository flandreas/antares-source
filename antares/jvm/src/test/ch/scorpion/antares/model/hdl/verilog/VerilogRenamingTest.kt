package ch.scorpion.antares.model.hdl.verilog

import ch.scorpion.antares.hdl.verilog.VerilogRenaming
import kotlin.test.Test
import kotlin.test.assertEquals

class VerilogRenamingTest {

    private val renaming = VerilogRenaming()

    @Test
    fun shouldNotAdjustValidName() {
        assertEquals("a", renaming.checkName("a"))
    }

    @Test
    fun shouldEscapeLeadingDigit() {
        assertEquals("\\0a ", renaming.checkName("0a"))
    }

    @Test
    fun shouldEscapeKeyword() {
        assertEquals("\\input ", renaming.checkName("input"))
        assertEquals("\\output ", renaming.checkName("output"))
    }

    @Test
    fun shouldReplaceBlanksWithUnderscoresIfNotEscaped() {
        assertEquals("Half_Adder", renaming.checkName("Half Adder"))
    }
}