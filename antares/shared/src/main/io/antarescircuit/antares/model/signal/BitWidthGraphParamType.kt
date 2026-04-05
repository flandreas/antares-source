package io.antarescircuit.antares.model.signal

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.dsl.Dsl
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.dsl.Memory
import io.antarescircuit.jabbah.edit.semantic.Semantic
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.graph.GraphActivationRecord
import io.antarescircuit.jabbah.graph.model.param.GraphParamType
import io.antarescircuit.jabbah.graph.model.param.GraphParamValue
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.reflect.KClass

/**
 * Defines [BitWidth] as [GraphParamType].
 */
object BitWidthGraphParamType : GraphParamType<BitWidth> {

	/** ---- [GraphParamType] interface */

	override val name: String = "bitWidth"

	override val displayableName: String by lazy { Translations.getString("${BitWidth.BASE_KEY}.name") }

	override val valueClass: KClass<BitWidth> get() = BitWidth::class

	override fun toString(): String = displayableName

	override fun createValue(name: String, value: BitWidth, semantic: Semantic?): GraphParamValue<BitWidth> =
		GraphParamValue.create(name, this, value, semantic)

	override fun writeValue(name: String, value: BitWidth, writer: StoreWriter) {
		value.write(name, writer)
	}

	override fun readValue(name: String, reader: StoreReader): BitWidth =
		BitWidth.read(name, reader)

	override fun toDslValue(value: BitWidth): Any = value.width.toLong()

	override fun evaluateIn(graph: Graph, value: BitWidth): BitWidth =
		if (value is BitWidthExpression) {
			if (value.expression.startsWith(GraphParamType.EXPRESSION_OP)) {
				evaluateIn(graph, value.expression.drop(1))
			} else {
				evaluateIn(graph, value.expression)
			}
		} else {
			value
		}

	/** ---- [BitWidthGraphParamType] */

	fun parse(s: String, supportExpression: Boolean): BitWidth {
		val number = s.toIntOrNull()
		return if (number != null) {
			BitWidth.of(number)
		} else {
			if (!supportExpression) {
				throw IllegalArgumentException("Expressions not supported")
			}
			if (s.trimStart().startsWith(GraphParamType.EXPRESSION_OP)) {
				BitWidthExpression(s.trimStart().drop(1))
			} else {
				throw IllegalArgumentException("Illegal bit width expression")
			}
		}
	}

	private fun evaluateIn(graph: Graph, expression: String): BitWidth {
		val newValue = Dsl.execute(expression, graph.symbolTable, Memory(GraphActivationRecord(graph)))
		if (newValue is Long) {
			try {
				return BitWidthExpression(expression, BitWidth.of(newValue.toInt()))
			} catch (e: Throwable) {
				throw DslError(
					TextLocation.UNDEFINED,
					Translations.getString("antares.dsl.bitWidthResolution.msg", StringUtils.limit(expression, 10)))
			}
		} else {
			throw DslError(TextLocation.UNDEFINED, "Expecting Long as value of BitWidth expression")
		}
	}
}