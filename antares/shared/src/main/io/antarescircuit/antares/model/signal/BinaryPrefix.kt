package io.antarescircuit.antares.model.signal

/**
 * Defines values with unit prefixes according to the IEC standard for all defined [BitWidths][BitWidth].
 * Examples: 8, 1 Ki, 16 Ki, 256 Mi.
 */
object BinaryPrefix {

	private val UNITS = arrayOf(
		"",
		"Ki",
		"Mi",
		"Gi",
		"Ti",
		"Pi",
		"Ei"
	)

	private val VALUES: Map<Int, String> by lazy {
		val result = mutableMapOf<Int, String>()
		for (i in 0..BitWidth.MAX) {
			val unit = UNITS[i / 10]
			val quantity = BitOperation.power(i.mod(10).toByte())
			result[i] = if (unit.isBlank()) {
				quantity.toString()
			} else {
				"$quantity $unit"
			}
		}
		result
	}

	fun of(size: Int): String =
		VALUES[size] ?: throw IllegalArgumentException("Size $size not supported")
}