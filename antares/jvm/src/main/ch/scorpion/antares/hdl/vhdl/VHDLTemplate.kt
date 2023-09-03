package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.BuiltInNode
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.io.Separator
import ch.scorpion.jabbah.base.logger
import korlibs.template.Template
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.IOUtils
import java.lang.IllegalStateException

/** Reads a file containing VHDL code to create an [VHDLTemplate] */
class VHDLTemplate(name: String) {

	companion object {
		private val LOG by logger(VHDLTemplate::class)

		private const val PREFIX = "VHDL_"
		private const val EXTENSION = ".template"

		const val ATTR_VHDL = "vhdl"
		const val ATTR_BIT_WIDTH = "bitWidth"
		const val ATTR_NEGATIVE = "negative"
		const val ATTR_LABEL = "label"

		private fun createFileName(name: String): String = "vhdl/$name$EXTENSION"
	}

	data class Generic(val name: String, val type: String) {
		fun format(value: Any): String {
			return when (type) {
				"integer" ->  (value as Int).toString()
				else -> throw IllegalArgumentException("Unsupported generic type '$type'")
			}
		}
	}

	private val entityName: String = "$PREFIX$name"

	/** Maps element names to the corresponding [Entity]. */
	private val entities = mutableMapOf<String, Entity>()

	private val template = runBlocking {
		val fileName = createFileName(entityName)
		this.javaClass.classLoader.getResourceAsStream(fileName).use {
			if (it == null) {
				// This is not an error. Certain Vertices like LED are not supported by HDL.
				// Will be handled and mapped to another exception by the caller. No need to I18N.
				val msg = "VHDL template $fileName not found"
				LOG.debug(msg)
				throw HDLException(msg)
			} else {
				Template(IOUtils.toString(it, Charsets.UTF_8))
			}
		}
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
		val entity = getEntity(node.attributes)
		if (entity.generics.isNotEmpty()) {
			out.println("generic map (").inc()
			val sep = Separator(out, ",\n")
			for (gen in entity.generics) {
				sep.check()
				val value = node.attributes[gen.name] ?: throw IllegalStateException("Generic value not available")
				out.print(gen.name).print(" => ").print(gen.format(value))
			}
			out.println(")").dec()
		}
	}

	private fun getEntity(attributes: Map<String, Any>): Entity {
		val newGenerated: Entity = try {
			Entity(entityName, template, attributes)
		} catch (e: Throwable) {
			if (e.cause is HDLException) {
				// Exception thrown by the script calling error()
				throw e.cause!!
			}
			LOG.error("Error while executing template", e)
			throw HDLException("Error in VHDL script: ${e.message}")
		}

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

	class Entity(
		name: String,
		template: Template,
		attributes: Map<String, Any>
	) {
		val generics = mutableListOf<Generic>()

		var name: String = name
			private set

		val code = runBlocking {
			template(attributes.toMutableMap().also { it[ATTR_VHDL] = this@Entity })
		}

		var isWritten: Boolean = false

		/** ---- Methods to be called by template */

		@Suppress("unused")
		fun error(textKey: String) {
			throw HDLException(Translations.getString(textKey))
		}

		@Suppress("unused")
		fun error(textKey: String, arg: String) {
			throw HDLException(Translations.getString(textKey, arg))
		}

		@Suppress("unused")
		fun setEntityName(name: String): String {
			this.name = name
			return name
		}

		@Suppress("unused")
		fun registerGeneric(name: String) {
			generics.add(Generic(name, "integer"))
		}

		@Suppress("unused")
		fun genericType(bitWidth: Int): String =
			if (bitWidth == 1) {
				"std_logic"
			} else {
				"std_logic_vector(($ATTR_BIT_WIDTH - 1) downto 0)"
			}
	}
}