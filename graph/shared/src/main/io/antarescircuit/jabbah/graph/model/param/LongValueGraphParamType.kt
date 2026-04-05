package io.antarescircuit.jabbah.graph.model.param

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.Dsl
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.dsl.Memory
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.graph.GraphActivationRecord
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.edit.semantic.Semantic
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
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

    override fun createValue(name: String, value: LongValue, semantic: Semantic?): GraphParamValue<LongValue> =
        GraphParamValue.create(name, this, value, semantic)

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