package io.antarescircuit.antares.hdl.verilog

import io.antarescircuit.antares.hdl.BuiltInNode
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.antares.hdl.loadHDLTemplate
import io.antarescircuit.antares.hdl.runTemplateSync
import io.antarescircuit.antares.hdl.templateBitWidth
import io.antarescircuit.antares.hdl.templateInt
import korlibs.template.Template
import korlibs.template.dynamic.DynamicType

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

	private val template = try {
		loadHDLTemplate(createFileName(defaultModuleName))
	} catch (e: HDLException) {
		// Not an error: certain Vertices like LED are not supported by HDL.
		// Mapped to a translated exception by the caller.
		LOG.debug(e.message ?: "")
		throw e
	}

	/** Returns the [Module] evaluated for [node]. */
	fun getModule(node: BuiltInNode): Module =
		modules.getOrPut(node) {
			try {
				Module(defaultModuleName, template, node.attributes)
			} catch (e: HDLException) {
				throw e
			} catch (e: Throwable) {
				if (e.cause is HDLException) {
					// Exception thrown by the script calling error()
					throw e.cause!!
				}
				LOG.error("Error while executing template", e)
				throw HDLException(Translations.getString("antares.verilog.script.error.text", e.message ?: "-"))
			}
		}

	/** The result of evaluating the template for a node. Its methods are called by the template script. */
	@Suppress("MemberVisibilityCanBePrivate")
	class Module(
		name: String,
		template: Template,
		attributes: Map<String, Any>,
	) : DynamicType<Module> by DynamicType({
		register("error") { args ->
			if (args.size > 1) error(args[0].toString(), args[1].toString()) else error(args[0].toString())
		}
		register("setModuleName") { args -> setModuleName(args[0].toString()) }
		register("registerParameter") { args -> registerParameter(args[0].toString(), templateInt(args[1])) }
		register("identifier") { args -> identifier(args[0].toString()) }
		register("range") { args -> range(templateInt(args[0])) }
		register("zero") { args -> zero(templateBitWidth(args[0])) }
		register("highZ") { args -> highZ(templateBitWidth(args[0])) }
	}) {
		val parameters = mutableListOf<Parameter>()

		var name: String = name
			private set

		val code = runTemplateSync {
			template(attributes.toMutableMap().also { it[ATTR_VERILOG] = this@Module })
		}

		/** ---- Methods called by template scripts */

		fun error(textKey: String) {
			throw HDLException(Translations.getString(textKey))
		}

		fun error(textKey: String, arg: String) {
			throw HDLException(Translations.getString(textKey, arg))
		}

		fun setModuleName(name: String): String {
			this.name = name
			return name
		}

		/** Registers a module parameter whose [value] is assigned when instantiating the module. */
		fun registerParameter(name: String, value: Int): String {
			parameters.add(Parameter(name, value))
			return ""
		}

		/** Legalizes [name], e.g. a component label, for use within a module name. */
		fun identifier(name: String): String = VerilogRenaming().checkName(name)

		fun range(width: Int): String = Verilog.range(width)

		fun zero(bitWidth: BitWidth): String = Verilog.fill(bitWidth, '0')

		fun highZ(bitWidth: BitWidth): String = Verilog.fill(bitWidth, 'z')
	}
}
