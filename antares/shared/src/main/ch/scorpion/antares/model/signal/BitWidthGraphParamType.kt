package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.dsl.Dsl
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord
import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
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

	override fun createValue(name: String, value: BitWidth): GraphParamValue<BitWidth> =
		GraphParamValue.create(name, this, value)

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

	fun parse(s: String): BitWidth {
		val number = s.toIntOrNull()
		return if (number != null) {
			BitWidth.of(number)
		} else {
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