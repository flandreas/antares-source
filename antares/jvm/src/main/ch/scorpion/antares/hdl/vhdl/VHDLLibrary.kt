package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.BuiltInNode
import ch.scorpion.jabbah.base.logger

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
		} catch (e: Throwable) {
			val msg = "Could not load VHDL template for $name"
			LOG.info("$msg: ${e::class.simpleName} ${e.message}")
			throw HDLException(msg)
		}
	}
}