package io.antarescircuit.antares.hdl.vhdl

import io.antarescircuit.antares.hdl.BuiltInNode
import io.antarescircuit.jabbah.base.logger

/**
 * Loads and manages [VHDLTemplate]s.
 * Makes sure that [VHDLTemplate]s are only loaded once.
 */
class VHDLLibrary {

	companion object {
		private val LOG by logger(VHDLLibrary::class)
	}

	private val templates = mutableMapOf<String, VHDLTemplate>()

	fun getTemplate(node: BuiltInNode): VHDLTemplate {
		val name = node.elementName
		try {
			return templates.computeIfAbsent(name) {
				VHDLTemplate(name)
			}
		} catch (e: HDLException) {
			val msg = "Circuit element '${node.translatedName}'\ndoesn't support HDL."
			LOG.debug(msg)
			throw HDLException(msg)
		} catch (e: Throwable) {
			val msg = "Error while generating VHDL for '${node.translatedName}'"
			LOG.error("$msg: ${e::class.simpleName} ${e.message}")
			throw HDLException(msg)
		}
	}
}