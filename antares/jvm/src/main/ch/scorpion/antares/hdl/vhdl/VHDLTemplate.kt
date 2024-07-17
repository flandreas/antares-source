package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.AbstractHDLTemplate
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.antares.hdl.BuiltInNode
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.io.Separator
import ch.scorpion.jabbah.base.logger
import java.lang.IllegalStateException

/** Reads a file containing VHDL code to create an [VHDLTemplate] */
class VHDLTemplate(name: String) : AbstractHDLTemplate("VHDL_$name") {

	companion object {
		private const val EXTENSION = ".template"
	}

	override fun createFileName(name: String): String = "vhdl/$name$EXTENSION"

	override fun valueImpl(value: ULong, bitWidth: BitWidth): String =
		AbstractVHDLCreator.value(value, bitWidth)

	override fun typeImpl(bitWidth: Int): String = AbstractVHDLCreator.getType(BitWidth.of(bitWidth))

	fun writeGenericMap(out: CodePrinter, node: BuiltInNode) {
		val entity = getEntity(node.attributes)
		if (entity.generics.isNotEmpty()) {
			out.println("generic map (").inc()
			val sep = Separator(out, ",\n")
			for (gen in entity.generics) {
				sep.check()
				val value = gen.value
					?: node.attributes[gen.name]
					?: throw IllegalStateException("Generic value '${gen.name}' not available")
				out.print(gen.name).print(" => ").print(gen.format(value))
			}
			out.println(")").dec()
		}
	}
}