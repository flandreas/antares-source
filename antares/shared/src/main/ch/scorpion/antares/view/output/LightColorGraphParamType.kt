package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Dsl
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.edit.semantic.Semantic
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord
import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

object LightColorGraphParamType : GraphParamType<LightColor> {

    /** ---- [GraphParamType] interface */

    override val name: String = "lightColor"

    override val displayableName: String by lazy { Translations.getString("element.property.LEDColor.name") }

    override val valueClass: KClass<LightColor> get() = LightColor::class

    override fun toString(): String = displayableName

    override fun writeValue(name: String, value: LightColor, writer: StoreWriter) {
        writer.writeString(name, value.customName)
    }

    override fun readValue(name: String, reader: StoreReader): LightColor =
        LightColor.withName(reader.readString(name))

    override fun createValue(name: String, value: LightColor, semantic: Semantic?): GraphParamValue<LightColor> =
        GraphParamValue.create(name, this, value, semantic)

    override fun toDslValue(value: LightColor): Any = value.ordinal.toLong()

    override fun evaluateIn(graph: Graph, value: LightColor): LightColor =
        if (value is LightColorExpression) {
            if (value.expression.startsWith(GraphParamType.EXPRESSION_OP)) {
                evaluateIn(graph, value.expression.drop(1))
            } else {
                evaluateIn(graph, value.expression)
            }
        } else {
            value
        }

    /** ---- [LightColorGraphParamType] */

    fun parse(s: String, supportExpression: Boolean): LightColor {
        val number = s.toIntOrNull()
        return if (number != null) {
            LightColor.withOrdinal(number)
        } else {
            if (!supportExpression) {
                throw IllegalArgumentException("Expressions not supported")
            }
            if (s.trimStart().startsWith(GraphParamType.EXPRESSION_OP)) {
                LightColorExpression(s.trimStart().drop(1))
            } else {
                throw IllegalArgumentException("Illegal light color expression")
            }
        }
    }

    private fun evaluateIn(graph: Graph, expression: String): LightColor {
        val newValue = Dsl.execute(expression, graph.symbolTable, Memory(GraphActivationRecord(graph)))
        if (newValue is Long) {
            try {
                return LightColorExpression(expression, LightColor.withOrdinal(newValue.toInt()))
            }  catch (_: Throwable) {
                throw DslError(
                    TextLocation.UNDEFINED,
                    Translations.getString("antares.dsl.lightColorResolution.msg", StringUtils.limit(expression, 10)))
            }
        } else {
            throw DslError(TextLocation.UNDEFINED, "Expecting Long as value of LightColor expression")
        }
    }
}