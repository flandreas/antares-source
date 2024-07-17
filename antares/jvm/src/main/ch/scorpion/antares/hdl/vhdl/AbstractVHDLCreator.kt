package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.HDLCircuit
import ch.scorpion.antares.hdl.HDLPort
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.io.CodePrinter

abstract class AbstractVHDLCreator(
	protected val out: CodePrinter
) {

	companion object {

		@JvmStatic
		protected fun value(value: DigitalSignal): String =
			if (value.bitWidth.width > 1) {
				"\"${value.binaryString}\""
			} else {
				"\'${value.binaryString}\'"
			}

		fun value(value: ULong, bitWidth: BitWidth): String {
			var s = BitOperation.longToBinaryPadded(value, bitWidth)
			s = if (bitWidth == BitWidth.BW_1) {
				"'$s'"
			} else {
				"\"$s\""
			}
			return s
		}

		fun getType(bitWidth: BitWidth): String =
			if (bitWidth == BitWidth.BW_1) {
				"std_logic"
			} else {
				"std_logic_vector(${bitWidth.width - 1} downto 0)"
			}
	}

	protected fun printImports() {
		out
			.println("library ieee;")
			.println("use ieee.std_logic_1164.all;")
			.println("use ieee.numeric_std.all;")
			.println()
	}


	private fun getOutsidePortDirection(port: HDLPort): String =
		when (port.direction) {
			HDLPort.Direction.IN -> "out"
			HDLPort.Direction.OUT -> "in"
			HDLPort.Direction.INOUT -> "inout"
		}

	private fun printPort(port: HDLPort, isLast: Boolean) {
		out.print(port.name).print(": ").print(getOutsidePortDirection(port)).print(' ').print(getType(port.bitWidth))
		if (!isLast) {
			out.println(";")
		}
	}

	protected fun printEntityPorts(circuit: HDLCircuit) {
		var count = 0
		out.println("port (").inc()
		circuit.inputs.forEach {
			count++
			printPort(it, count == circuit.portsCount)
		}
		circuit.outputs.forEach {
			count++
			printPort(it, count == circuit.portsCount)
		}
		circuit.inOuts.forEach {
			count++
			printPort(it, count == circuit.portsCount)
		}
		out.println(");").dec()
	}
}