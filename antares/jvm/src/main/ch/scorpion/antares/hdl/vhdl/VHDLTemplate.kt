package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.jabbah.base.io.CodePrinter
import org.apache.commons.io.IOUtils

/** Reads a file containing VHDL code to create an [VHDLTemplate] */
class VHDLTemplate(name: String) {

	companion object {
		const val PREFIX = "VHDL_"
		private const val EXTENSION = ".template"

		private fun createFileName(name: String): String = "vhdl/$name$EXTENSION"
	}

	private val content = IOUtils.toString(this.javaClass.classLoader.getResourceAsStream(createFileName(name)), Charsets.UTF_8)

	fun print(out: CodePrinter) {
		out.print(content)
	}
}