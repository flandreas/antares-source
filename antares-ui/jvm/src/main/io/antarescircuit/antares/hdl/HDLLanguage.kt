package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.verilog.VerilogGenerator
import io.antarescircuit.antares.hdl.verilog.VerilogRenaming
import io.antarescircuit.antares.hdl.vhdl.VHDLGenerator
import io.antarescircuit.antares.hdl.vhdl.VHDLRenaming
import io.antarescircuit.antares.model.DigitalGraph

/**
 * A hardware description language a [DigitalGraph] can be exported to.
 *
 * @property keyPrefix the prefix of language-specific translation keys, e.g. "antares.vhdl"
 * @property fileExtension the extension of exported files, including the dot
 * @property settingsPrefix the prefix of persistent settings of the export dialog
 */
enum class HDLLanguage(
	val keyPrefix: String,
	val fileExtension: String,
	val settingsPrefix: String,
) {
	VHDL("antares.vhdl", ".vhdl", "exportVHDL") {
		override fun createRenaming(): HDLRenaming = VHDLRenaming()

		override fun generate(params: HDLExportParams, circuit: DigitalGraph) {
			VHDLGenerator(params, generateComment = true).generate(circuit)
		}
	},

	VERILOG("antares.verilog", ".v", "exportVerilog") {
		override fun createRenaming(): HDLRenaming = VerilogRenaming()

		override fun generate(params: HDLExportParams, circuit: DigitalGraph) {
			VerilogGenerator(params, generateComment = true).generate(circuit)
		}
	};

	abstract fun createRenaming(): HDLRenaming

	/** Writes the HDL file and, if requested by [params], the test bench file for [circuit]. */
	abstract fun generate(params: HDLExportParams, circuit: DigitalGraph)
}
