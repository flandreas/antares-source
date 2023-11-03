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
		} catch (e: HDLException) {
			val msg = "Circuit element '$name'\ndoesn't support HDL."
			LOG.debug(msg)
			throw HDLException(msg)
		} catch (e: Throwable) {
			val msg = "Error while generating VHDL for '$name'"
			LOG.error("$msg: ${e::class.simpleName} ${e.message}")
			throw HDLException(msg)
		}
	}
}