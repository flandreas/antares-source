package ch.scorpion.antares.hdl.verilog

import ch.scorpion.antares.hdl.AbstractHDLTemplate
import ch.scorpion.antares.hdl.BuiltInNode
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.io.Separator

class VerilogTemplate(name: String) : AbstractHDLTemplate("Verilog_$name") {

    companion object {
        private const val EXTENSION = ".template"
    }

    override fun createFileName(name: String): String = "verilog/$name$EXTENSION"

    override fun valueImpl(value: ULong, bitWidth: BitWidth): String =
        AbstractVerilogCreator.value(value, bitWidth)

    override fun typeImpl(bitWidth: Int): String =
        AbstractVerilogCreator.getType(BitWidth.of(bitWidth))

    override fun zeroImpl(bitWidth: BitWidth): String = valueImpl(0UL, bitWidth)

    fun writeGenericMap(out: CodePrinter, node: BuiltInNode) {
        val entity = getEntity(node.attributes)
        if (entity.generics.isNotEmpty()) {
            out.println("#(").inc()
            val sep = Separator(out, ",\n")
            for (gen in entity.generics) {
                sep.check()
                val value = gen.value
                    ?: node.attributes[gen.name]
                    ?: throw IllegalStateException("Generic value '${gen.name}' not available")
                out.print(".").print(gen.name).print("(").print(gen.format(value)).print(")")
            }
            out.dec().println().println(")")
        }
    }
}