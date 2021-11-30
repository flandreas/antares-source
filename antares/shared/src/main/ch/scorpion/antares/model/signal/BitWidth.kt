package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.Translations

open class BitWidth(
	val width: Int,
	val size: String
) {
	companion object {

		const val BASE_KEY = "element.property.bitWidth"

		val BW_1 = BitWidth(1, "1")
		val BW_2 = BitWidth(2, "4")
		val BW_4 = BitWidth(4, "16")
		val BW_8 = BitWidth(8, "256")
		val BW_12 = BitWidth(12, "4K")
		val BW_16 = BitWidth(16, "64K")
		val BW_20 = BitWidth(20, "1M")
		val BW_24 = BitWidth(24, "16M")
		val BW_28 = BitWidth(28, "256M")
		val BW_32 = BitWidth(32, "4G")
		val BW_64 = BitWidth(64, "16E")

		val PREDEFINED = listOf(BW_1, BW_2, BW_4, BW_8, BW_12, BW_16, BW_20, BW_24, BW_28, BW_32, BW_64)

		fun of(width: Int): BitWidth =
			PREDEFINED.firstOrNull { it.width == width }
				?: throw IllegalArgumentException("Unsupported BitWidth of '$width'")

		fun withName(customName: String): BitWidth =
			PREDEFINED.firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("Unknown BitWidth '$customName'")
	}

	val maxValue: ULong = if (width == 64) {
		ULong.MAX_VALUE
	} else {
		BitOperation.power(this.width.toByte()) - 1UL
	}

	val customName: String get() = width.toString()

	override fun toString(): String = customName

	override fun equals(other: Any?): Boolean {
		if (other !is BitWidth || other is BitWidthExpression) {
			return false
		}
		return this.width == other.width
	}

	override fun hashCode(): Int = width

	fun max(other: BitWidth): BitWidth = of(kotlin.math.max(width, other.width))
}

class BitWidthExpression(
	val expression: String
) : BitWidth(2, "Expr") {

	override fun toString(): String = "${Translations.getString("graph.property.graphParams.expression")}: $expression"

	override fun equals(other: Any?): Boolean {
		if (other !is BitWidthExpression) {
			return false
		}
		return expression == this.expression
	}

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + expression.hashCode()
		return result
	}
}


