package io.antarescircuit.antares.hdl.verilog

import io.antarescircuit.antares.model.signal.BitOperation
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal

/** Verilog syntax helpers shared by the circuit, template and test bench creators. */
object Verilog {

	/** Time unit for delays ("#20") in generated files, matching the nanoseconds used by Antares. */
	const val TIMESCALE = "`timescale 1ns / 1ps"

	/** Returns the vector range declaration including a trailing blank (e.g. "[7:0] "), or "" for a single bit. */
	fun range(width: Int): String = if (width > 1) "[${width - 1}:0] " else ""

	fun range(bitWidth: BitWidth): String = range(bitWidth.width)

	/** Returns a sized binary literal, e.g. "4'b0101". */
	fun value(value: ULong, bitWidth: BitWidth): String =
		"${bitWidth.width}'b${BitOperation.longToBinaryPadded(value, bitWidth)}"

	fun value(value: DigitalSignal): String = "${value.bitWidth.width}'b${value.binaryString}"

	/**
	 * Returns a sized literal with all bits set to [bit] ('0', 'x' or 'z'), e.g. "8'bz".
	 * Verilog extends the single digit to the full width.
	 */
	fun fill(bitWidth: BitWidth, bit: Char): String = "${bitWidth.width}'b$bit"
}
