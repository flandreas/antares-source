package ch.scorpion.jabbah.graph.model.value

import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.graph.model.param.LongValueGraphParamType
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

interface LongValue {

    companion object {

        fun read(name: String, reader: StoreReader): LongValue {
            val value = reader.readString(name)
            val number = value.toLongOrNull()
            return number?.let { LongValueImpl(it) } ?: LongValueExpression(value)
        }
    }

    val value: Long

    fun write(name: String, writer: StoreWriter)
}

class LongValueImpl(
    override val value: Long
) : LongValue {

    override fun write(name: String, writer: StoreWriter) {
        writer.writeLong(name, value)
    }

    override fun toString(): String = value.toString()
}

data class LongValueExpression(
    var expression: String,
    var longValue: LongValue = LongValueImpl(0)
) : LongValue {

    override val value: Long get() = longValue.value

    override fun write(name: String, writer: StoreWriter) {
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
}