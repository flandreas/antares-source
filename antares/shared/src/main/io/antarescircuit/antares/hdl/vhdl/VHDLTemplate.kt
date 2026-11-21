package io.antarescircuit.antares.hdl.vhdl

import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.antares.hdl.BuiltInNode
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.io.CodePrinter
import io.antarescircuit.jabbah.base.io.Separator
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.antares.hdl.loadHDLTemplate
import io.antarescircuit.antares.hdl.runTemplateSync
import io.antarescircuit.antares.hdl.templateBitWidth
import io.antarescircuit.antares.hdl.templateInt
import korlibs.template.Template
import korlibs.template.dynamic.DynamicType

/** Reads a file containing VHDL code to create an [VHDLTemplate] */
class VHDLTemplate(name: String) {

	companion object {
		private val LOG by logger(VHDLTemplate::class)

		private const val PREFIX = "VHDL_"
		private const val EXTENSION = ".template"

		/** The name of the [Entity] object passed to template scripts for calling functions.*/
		const val ATTR_VHDL = "vhdl"

		/** The name of the [Vertice] object passed to template scripts for accessing properties.*/
		const val ATTR_VERTICE = "vertice"

		private fun createFileName(name: String): String = "vhdl/$name$EXTENSION"
	}

	data class Generic(
		val name: String,
		val type: String,
		val value: Any? = null
	) {
		fun format(value: Any): String {
			return when (type) {
				"integer" -> templateInt(value).toString()
				else -> throw IllegalArgumentException("Unsupported generic type '$type'")
			}
		}
	}

	private val entityName: String = "$PREFIX$name"

	/** Maps element names to the corresponding [Entity]. */
	private val entities = mutableMapOf<String, Entity>()

	private val template = try {
		loadHDLTemplate(createFileName(entityName))
	} catch (e: HDLException) {
		// This is not an error. Certain Vertices like LED are not supported by HDL.
		// Will be handled and mapped to another exception by the caller. No need to I18N.
		LOG.debug(e.message ?: "")
		throw e
	}

	fun print(out: CodePrinter, node: BuiltInNode): String {
		val entity = getEntity(node.attributes)
		if (!entity.isWritten) {
			out.print(entity.code)
			entity.isWritten = true
		}
		return entity.name
	}

	fun writeGenericMap(out: CodePrinter, node: BuiltInNode) {
		// Generic values are per instance, so they must not come from the cached entity of the first instance
		val entity = getEntity(node.attributes).let { cached -> if (cached.generics.isEmpty()) cached else generate(node.attributes) }
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

	private fun getEntity(attributes: Map<String, Any>): Entity {
		val newGenerated = generate(attributes)
		val entity = entities[newGenerated.name]
		return if (entity == null) {
			entities[newGenerated.name] = newGenerated
			newGenerated
		} else {
			if (entity.code != newGenerated.code) {
				val msg = "Multiple used VHDL templates must be equal"
				LOG.error(msg)
				throw HDLException(msg)
			} else {
				entity
			}
		}
	}

	private fun generate(attributes: Map<String, Any>): Entity =
		try {
			Entity(entityName, template, attributes)
		} catch (e: HDLException) {
			throw e
		} catch (e: Throwable) {
			if (e.cause is HDLException) {
				// Exception thrown by the script calling error()
				throw e.cause!!
			}
			LOG.error("Error while executing template", e)
			throw HDLException(Translations.getString("antares.vhdl.script.error.text", e.message ?: "-"))
		}

	/** The result of evaluating the template for a node. Its methods are called by the template script. */
	@Suppress("MemberVisibilityCanBePrivate")
	class Entity(
		name: String,
		template: Template,
		attributes: Map<String, Any>
	) : DynamicType<Entity> by DynamicType({
		register("value") { args -> value(templateInt(args[0]), templateBitWidth(args[1])) }
		register("zero") { args -> zero(templateBitWidth(args[0])) }
		register("error") { args ->
			if (args.size > 1) error(args[0].toString(), args[1].toString()) else error(args[0].toString())
		}
		register("setEntityName") { args -> setEntityName(args[0].toString()) }
		register("registerGeneric") { args ->
			if (args.size > 1) registerGeneric(args[0].toString(), args[1].toString()) else registerGeneric(args[0].toString())
			""
		}
		register("registerGenericValue") { args -> registerGenericValue(args[0].toString(), templateInt(args[1])); "" }
		register("type") { args -> type(templateInt(args[0])) }
		register("genericType") { args -> genericType(args[0].toString(), templateInt(args[1])) }
	}) {
		val generics = mutableListOf<Generic>()

		var name: String = name
			private set

		val code = runTemplateSync {
			template(attributes.toMutableMap().also { it[ATTR_VHDL] = this@Entity })
		}

		var isWritten: Boolean = false

		/** ---- Methods called by template scripts */

		fun value(value: Int, bitWidth: BitWidth): String =
			AbstractVHDLCreator.value(value.toULong(), bitWidth)

		fun zero(bitWidth: BitWidth): String =
			if (bitWidth == BitWidth.BW_1) {
				"'0'"
			} else {
				"(others => '0')"
			}

		fun error(textKey: String) {
			throw HDLException(Translations.getString(textKey))
		}

		fun error(textKey: String, arg: String) {
			throw HDLException(Translations.getString(textKey, arg))
		}

		fun setEntityName(name: String): String {
			this.name = name
			return name
		}

		fun registerGeneric(name: String) {
			registerGeneric(name, "integer")
		}

		fun registerGeneric(name: String, type: String) {
			generics.add(Generic(name, type))
		}

		fun registerGenericValue(name: String, value: Int) {
			generics.add(Generic(name, "integer", value))
		}

		fun type(bitWidth: Int): String =
			if (bitWidth == 1) {
				"std_logic"
			} else {
				"std_logic_vector(${bitWidth - 1} downto 0)"
			}

		fun genericType(name: String, bitWidth: Int): String =
			if (bitWidth == 1) {
				"std_logic"
			} else {
				"std_logic_vector(($name - 1) downto 0)"
			}
	}
}
