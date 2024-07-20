package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.Vertice
import korlibs.template.AutoEscapeMode
import korlibs.template.Template
import korlibs.template.TemplateConfig
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.IOUtils

abstract class AbstractHDLTemplate(private val entityName: String) {

    companion object {
        private val LOG by logger(AbstractHDLTemplate::class)

        /** The name of the [Entity] object passed to template scripts for calling functions.*/
        const val ATTR_ENTITY = "entity"

        /** The name of the [Vertice] object passed to template scripts for accessing properties.*/
        const val ATTR_VERTICE = "vertice"
    }

    /** Maps element names to the corresponding [Entity]. */
    private val entities = mutableMapOf<String, Entity>()

    abstract fun valueImpl(value: ULong, bitWidth: BitWidth): String

    abstract fun typeImpl(bitWidth: Int): String

    abstract fun zeroImpl(bitWidth: BitWidth): String

    abstract fun createFileName(name: String): String

    fun print(out: CodePrinter, node: BuiltInNode): String {
        val entity = getEntity(node.attributes)
        if (!entity.isWritten) {
            out.print(entity.code)
            entity.isWritten = true
        }
        return entity.name
    }

    private val template = runBlocking {
        val fileName = createFileName(entityName)
        this.javaClass.classLoader.getResourceAsStream(fileName).use {
            if (it == null) {
                // This is not an error. Certain Vertices like LED are not supported by HDL.
                // Will be handled and mapped to another exception by the caller. No need to I18N.
                val msg = "HDL template $fileName not found"
                LOG.debug(msg)
                throw HDLException(msg)
            } else {
                Template(IOUtils.toString(it, Charsets.UTF_8), TemplateConfig(autoEscapeMode = AutoEscapeMode.RAW))
            }
        }
    }


    protected fun getEntity(attributes: Map<String, Any>): Entity {
        val newGenerated: Entity = try {
            Entity(entityName, template, attributes)
        } catch (e: Throwable) {
            if (e.cause is HDLException) {
                // Exception thrown by the script calling error()
                throw e.cause!!
            }
            LOG.error("Error while executing template", e)
            throw HDLException(Translations.getString("antares.hdl.script.error.text", e.message ?: ""))
        }

        val entity = entities[newGenerated.name]
        return if (entity == null) {
            entities[newGenerated.name] = newGenerated
            newGenerated
        } else {
            if (entity.code != newGenerated.code) {
                val msg = "Multiple used HDL templates must be equal"
                LOG.error(msg)
                throw HDLException(msg)
            } else {
                entity
            }
        }
    }

    data class Generic(
        val name: String,
        val type: String,
        val value: Any? = null
    ) {
        fun format(value: Any): String {
            return when (type) {
                "integer" ->  (value as Int).toString()
                else -> throw IllegalArgumentException("Unsupported generic type '$type'")
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    inner class Entity(
        name: String,
        template: Template,
        attributes: Map<String, Any>
    ) {
        val generics = mutableListOf<Generic>()

        var name: String = name
            private set

        val code = runBlocking {
            template(attributes.toMutableMap().also { it[ATTR_ENTITY] = this@Entity })
        }

        var isWritten: Boolean = false

        /** ---- Methods to be called by template using reflection */

        fun value(value: Int, bitWidth: BitWidth): String =
            valueImpl(value.toULong(), bitWidth)

        @Suppress("unused")
        fun zero(bitWidth: BitWidth): String =
            zeroImpl(bitWidth)

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
            registerGeneric(name, "integer")
        }

        @Suppress("unused")
        fun registerGeneric(name: String, type: String) {
            generics.add(Generic(name, type))
        }

        @Suppress("unused")
        fun registerGenericValue(name: String, value: Int) {
            generics.add(Generic(name, "integer", value))
        }

        @Suppress("unused")
        fun type(bitWidth: Int): String = typeImpl(bitWidth)

        // TODO: This is VHDL
        @Suppress("unused")
        fun genericType(name: String, bitWidth: Int): String =
            if (bitWidth == 1) {
                "std_logic"
            } else {
                "std_logic_vector(($name - 1) downto 0)"
            }
    }
}