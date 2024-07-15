package ch.scorpion.antares.hdl.verilog

import ch.scorpion.antares.hdl.BuiltInNode
import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.jabbah.base.logger

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
            val msg = "Circuit element '${node.translatedName}'\ndoesn't support Verilog."
            LOG.debug(msg)
            throw HDLException(msg)
        } catch (e: Throwable) {
            val msg = "Error while generating Verilog for '${node.translatedName}'"
            LOG.error("$msg: ${e::class.simpleName} ${e.message}")
            throw HDLException(msg)
        }
    }
}