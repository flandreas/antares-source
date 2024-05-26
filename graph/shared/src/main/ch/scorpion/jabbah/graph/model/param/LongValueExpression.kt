package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class LongValueExpression(
    var expression: String,
    private val longValue: LongValue = LongValueImpl.ZERO
) : LongValue {

    companion object {
        fun read(name: String, reader: StoreReader): LongValue {
            val value = reader.readString(name)
            val number = value.toLongOrNull()
            return number?.let { LongValueImpl(it) } ?: LongValueExpression(value)
        }

        fun write(name: String, value: LongValue, writer: StoreWriter) {
            when (value) {
                is LongValueExpression -> value.write(name, writer)
                else -> writer.writeLong(name, value.value)
            }
        }
    }

    override val value: Long get() = longValue.value

    override fun toString(): String =
        "${GraphParamType.EXPRESSION_OP}${StringUtils.limit(expression, 10)}"

    fun write(name: String, writer: StoreWriter) {
        writer.writeString(name, expression)
    }

    /**
     * Evaluates this [LongValueExpression] using the [GraphParamValues] of [graph]
     * in order to check if evaluation results in a different value.
     * @return the possibly different value, or `null` if evaluation results in the same value
     * @throws DslError if evaluation results in an error
     */
    fun evaluateIn(graph: Graph): LongValue? {
        val newValue = LongValueGraphParamType.evaluateIn(graph, this)
        if (newValue === this) {
            return null
        }
        return newValue
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LongValueExpression

        return expression == other.expression
    }

    override fun hashCode(): Int {
        return expression.hashCode()
    }
}