package io.antarescircuit.antares.hdl.verilog

import io.antarescircuit.antares.hdl.BuiltInNode
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import korlibs.template.AutoEscapeMode
import korlibs.template.Template
import korlibs.template.TemplateConfig
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.IOUtils

/**
 * Reads a template file containing a Verilog module for a [BuiltInNode] type, e.g. a RAM.
 * The template is evaluated per node, as module name, code and parameter values may depend
 * on the node's properties.
 */
class VerilogTemplate(name: String) {

	companion object {
		private val LOG by logger(VerilogTemplate::class)

		private const val PREFIX = "Verilog_"
		private const val EXTENSION = ".template"

		/** The name of the [Module] object passed to template scripts for calling functions.*/
		const val ATTR_VERILOG = "verilog"

		private fun createFileName(name: String): String = "verilog/$name$EXTENSION"
	}

	data class Parameter(val name: String, val value: Int)

	private val defaultModuleName: String = "$PREFIX$name"

	/** The evaluated [Module] per node (identity based). */
	private val modules = mutableMapOf<BuiltInNode, Module>()

	private val template = runBlocking {
		val fileName = createFileName(defaultModuleName)
		this.javaClass.classLoader.getResourceAsStream(fileName).use {
			if (it == null) {
				// Not an error: certain Vertices like LED are not supported by HDL.
				// Mapped to a translated exception by the caller.
				val msg = "Verilog template $fileName not found"
				LOG.debug(msg)
				throw HDLException(msg)
			} else {
				Template(IOUtils.toString(it, Charsets.UTF_8), TemplateConfig(autoEscapeMode = AutoEscapeMode.RAW))
			}
		}
	}

	/** Returns the [Module] evaluated for [node]. */
	fun getModule(node: BuiltInNode): Module =
		modules.getOrPut(node) {
			try {
				Module(defaultModuleName, template, node.attributes)
			} catch (e: Throwable) {
				if (e.cause is HDLException) {
					// Exception thrown by the script calling error()
					throw e.cause!!
				}
				LOG.error("Error while executing template", e)
				throw HDLException(Translations.getString("antares.verilog.script.error.text", e.message ?: "-"))
			}
		}

	@Suppress("MemberVisibilityCanBePrivate")
	class Module(
		name: String,
		template: Template,
		attributes: Map<String, Any>,
	) {
		val parameters = mutableListOf<Parameter>()

		var name: String = name
			private set

		val code = runBlocking {
			template(attributes.toMutableMap().also { it[ATTR_VERILOG] = this@Module })
		}

		/** ---- Methods to be called by template using reflection */

		@Suppress("unused")
		fun error(textKey: String) {
			throw HDLException(Translations.getString(textKey))
		}

		@Suppress("unused")
		fun error(textKey: String, arg: String) {
			throw HDLException(Translations.getString(textKey, arg))
		}

		@Suppress("unused")
		fun setModuleName(name: String): String {
			this.name = name
			return name
		}

		/** Registers a module parameter whose [value] is assigned when instantiating the module. */
		@Suppress("unused")
		fun registerParameter(name: String, value: Int): String {
			parameters.add(Parameter(name, value))
			return ""
		}

		/** Legalizes [name], e.g. a component label, for use within a module name. */
		@Suppress("unused")
		fun identifier(name: String): String = VerilogRenaming().checkName(name)

		@Suppress("unused")
		fun range(width: Int): String = Verilog.range(width)

		@Suppress("unused")
		fun zero(bitWidth: BitWidth): String = Verilog.fill(bitWidth, '0')

		@Suppress("unused")
		fun highZ(bitWidth: BitWidth): String = Verilog.fill(bitWidth, 'z')
	}
}
