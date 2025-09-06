package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

interface BitWidth {

	companion object {
		const val BASE_KEY = "element.property.bitWidth"

		const val MAX = 64

		val PREDEFINED: List<BitWidth> = let {
			val result = mutableListOf<BitWidth>()
			for (i in 1..MAX) {
				result.add(BitWidthImpl(i, BinaryPrefix.of(i)))
			}
			result
		}

		// Backward compatibility: A lot of old code (especially unit tests) use the former, restricted set
		val BW_1: BitWidth get() = PREDEFINED[0]
		val BW_2: BitWidth get() = PREDEFINED[1]
		val BW_3: BitWidth get() = PREDEFINED[2]
		val BW_4: BitWidth get() = PREDEFINED[3]
		val BW_5: BitWidth get() = PREDEFINED[4]
		val BW_6: BitWidth get() = PREDEFINED[5]
		val BW_8: BitWidth get() = PREDEFINED[7]
		val BW_12: BitWidth get() = PREDEFINED[11]
		val BW_16: BitWidth get() = PREDEFINED[15]
		val BW_24: BitWidth get() = PREDEFINED[23]
		val BW_28: BitWidth get() = PREDEFINED[27]
		val BW_32: BitWidth get() = PREDEFINED[31]

		val COMMON = listOf(
			PREDEFINED[0],
			PREDEFINED[1],
			PREDEFINED[3],
			PREDEFINED[7],
			PREDEFINED[11],
			PREDEFINED[15],
			PREDEFINED[19],
			PREDEFINED[23],
			PREDEFINED[31]
		)

		fun of(width: Int): BitWidth =
			PREDEFINED.firstOrNull { it.width == width }
				?: throw IllegalArgumentException("Unsupported bit width $width")

		fun withName(customName: String): BitWidth =
			PREDEFINED.firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("Unknown bit width '$customName'")

		fun read(name: String, reader: StoreReader): BitWidth {
			val value = reader.readString(name)
			val number = value.toIntOrNull()
			return number?.let {
				try {
					of(it)
				} catch (e: Throwable) {
					BitWidthExpression(value)
				}
			} ?: BitWidthExpression(value)
		}

		/** Returns the smallest [BitWidth] that can represent [value].*/
		fun smallest(value: ULong): BitWidth? {
			return PREDEFINED.firstOrNull { it.maxValue >= value }
		}
	}

	val width: Int
	val size: String

	val maxValue: ULong get() =  if (width == 64) {
		ULong.MAX_VALUE
	} else {
		BitOperation.power(this.width.toByte()) - 1UL
	}

	val customName: String get() = width.toString()

	fun max(other: BitWidth): BitWidth = of(kotlin.math.max(width, other.width))

	fun write(name: String, writer: StoreWriter) {
		writer.writeInt(name, width)
	}
}

class BitWidthImpl(
	override val width: Int,
	override val size: String
) : BitWidth {

	override fun toString(): String = customName

	override fun equals(other: Any?): Boolean {
		if (other !is BitWidth || other is BitWidthExpression) {
			return false
		}
		return this.width == other.width
	}

	override fun hashCode(): Int = width
}

/**
 * A [BitWidth] implementation whose concrete value can be calculated from an expression.
 * Note: Interface cannot be implemented using "BitWidth by value", because Kotlin doesn't support "var" delegates.
 */
class BitWidthExpression(
	var expression: String,
	val value: BitWidth = BitWidth.BW_1
) : BitWidth {

	override val width: Int get() = value.width

	override val size: String get() = value.size

	override fun write(name: String, writer: StoreWriter) {
		writer.writeString(name, expression)
	}

	override fun toString(): String =
		"${GraphParamType.EXPRESSION_OP}${StringUtils.limit(expression, 20)}"

	override fun equals(other: Any?): Boolean {
		if (other !is BitWidthExpression) {
			return false
		}
		return this.expression == other.expression && this.width == other.width
	}

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + expression.hashCode()
		return result
	}

	/**
	 * Evaluates this [BitWidthExpression] using the [GraphParamValues] of [graph]
	 * in order to check if evaluation results in a different value.
	 * @return the possibly different value, or `null` if evaluation results in the same value
	 * @throws DslError if evaluation results in an error
	 */
	fun evaluateIn(graph: Graph): BitWidth? {
		val newValue = BitWidthGraphParamType.evaluateIn(graph, this)
		if (newValue === this) {
			return null
		}
		return newValue
	}
}


