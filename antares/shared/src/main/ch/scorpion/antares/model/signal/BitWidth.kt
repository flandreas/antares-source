package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

interface BitWidth {

	companion object {
		const val BASE_KEY = "element.property.bitWidth"

		val BW_1 = BitWidthImpl(1, "1")
		val BW_2 = BitWidthImpl(2, "4")
		val BW_3 = BitWidthImpl(3, "8")
		val BW_4 = BitWidthImpl(4, "16")
		val BW_5 = BitWidthImpl(5, "32")
		val BW_8 = BitWidthImpl(8, "256")
		val BW_12 = BitWidthImpl(12, "4K")
		val BW_16 = BitWidthImpl(16, "64K")
		val BW_20 = BitWidthImpl(20, "1M")
		val BW_24 = BitWidthImpl(24, "16M")
		val BW_28 = BitWidthImpl(28, "256M")
		val BW_32 = BitWidthImpl(32, "4G")
		val BW_64 = BitWidthImpl(64, "16E")

		val PREDEFINED: List<BitWidth> = listOf(BW_1, BW_2, BW_3, BW_4, BW_5, BW_8, BW_12, BW_16, BW_20, BW_24, BW_28, BW_32, BW_64)

		fun of(width: Int): BitWidth =
			PREDEFINED.firstOrNull { it.width == width }
				?: throw IllegalArgumentException("Unsupported BitWidth of '$width'")

		fun withName(customName: String): BitWidth =
			PREDEFINED.firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("Unknown BitWidth '$customName'")

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

open class BitWidthImpl(
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
	var value: BitWidth = BitWidth.BW_1
) : BitWidth {

	override val width: Int get() = value.width

	override val size: String get() = value.size

	override fun write(name: String, writer: StoreWriter) {
		writer.writeString(name, expression)
	}

	override fun toString(): String = "${Translations.getString("graph.property.graphParams.expression")}: ${StringUtils.limit(expression, 10)}"

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

	fun evaluateIn(graph: Graph): BitWidth? {
		try {
			val newValue = BitWidthGraphParamType.evaluateIn(graph, this)
			if (newValue === this) {
				return null
			}
			return newValue
		} catch (e: Throwable) {
			return null
		}
	}
}


