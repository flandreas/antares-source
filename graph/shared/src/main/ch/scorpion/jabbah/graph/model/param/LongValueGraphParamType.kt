package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Dsl
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord
import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

object LongValueGraphParamType : GraphParamType<LongValue> {

    override val name: String get() = "Long"

    override val displayableName: String by lazy { Translations.getString("graph.paramType.long.name") }

    override val valueClass: KClass<LongValue> get() = LongValue::class

    override fun toString(): String = displayableName

    override fun readValue(name: String, reader: StoreReader): LongValue = LongValueExpression.read(name, reader)

    override fun writeValue(name: String, value: LongValue, writer: StoreWriter) {
        LongValueExpression.write(name, value, writer)
    }

    override fun evaluateIn(graph: Graph, value: LongValue): LongValue =
        if (value is LongValueExpression) {
            if (value.expression.startsWith(GraphParamType.EXPRESSION_OP)) {
                evaluateIn(graph, value.expression.drop(1))
            } else {
                evaluateIn(graph, value.expression)
            }
        } else {
            value
        }

    override fun toDslValue(value: LongValue): Any = value.value

    override fun createValue(name: String, value: LongValue): GraphParamValue<LongValue> =
        GraphParamValue.create(name, this, value)

    fun parse(s: String): LongValue {
        val number = s.toLongOrNull()
        return if (number != null) {
            LongValueImpl(number)
        } else {
            if (s.trimStart().startsWith(GraphParamType.EXPRESSION_OP)) {
                LongValueExpression(s.trimStart().drop(1))
            } else {
                throw IllegalArgumentException("Illegal number expression")
            }
        }
    }

    private fun evaluateIn(graph: Graph, expression: String): LongValue {
        val newValue = Dsl.execute(expression, graph.symbolTable, Memory(GraphActivationRecord(graph)))
        if (newValue is Long) {
            return LongValueExpression(expression, LongValueImpl(newValue))
        } else {
            throw DslError(TextLocation.UNDEFINED, "Expecting Long as value of expression")
        }
    }
}