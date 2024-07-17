package ch.scorpion.antares.hdl.verilog

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.io.CodePrinter

abstract class AbstractVerilogCreator(
    protected val out: CodePrinter
) {

    companion object {

        fun value(value: ULong, bitWidth: BitWidth): String {
            if (bitWidth.width > 4) {
                val s = BitOperation.longToHex(value)
                return "${bitWidth.width}'h$s"
            } else {
                val s = BitOperation.longToBinaryPadded(value, bitWidth)
                return "${bitWidth.width}'b$s"
            }
        }

        fun getType(bitWidth: BitWidth): String =
            if (bitWidth.width == 1) {
                ""
            } else {
                "[${bitWidth.width - 1}:0]"
            }
    }
}