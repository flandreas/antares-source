package io.antarescircuit.antares.hdl.verilog

import io.antarescircuit.antares.hdl.BuiltInNode
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger

/**
 * Loads and manages [VerilogTemplate]s.
 * Makes sure that [VerilogTemplate]s are only loaded once.
 */
class VerilogLibrary {

	companion object {
		private val LOG by logger(VerilogLibrary::class)
	}

	private val templates = mutableMapOf<String, VerilogTemplate>()

	fun getTemplate(node: BuiltInNode): VerilogTemplate {
		val name = node.elementName
		try {
			return templates.computeIfAbsent(name) {
				VerilogTemplate(name)
			}
		} catch (e: HDLException) {
			LOG.debug("No Verilog template for '${node.translatedName}'")
			throw HDLException(Translations.getString("antares.vhdl.elementNotSupported.error.txt", node.translatedName))
		} catch (e: Throwable) {
			LOG.error("Error while generating Verilog for '${node.translatedName}': ${e::class.simpleName} ${e.message}")
			throw HDLException(Translations.getString("antares.verilog.script.error.text", node.translatedName))
		}
	}
}
