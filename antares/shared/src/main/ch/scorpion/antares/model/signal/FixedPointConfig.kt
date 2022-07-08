package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.io.*

/**
 * A configuration for [FixedPoint].
 * Setters must be public for reflective access by property editors on JVM.
 */
class FixedPointConfig(
	fractionSize: Int = 0,
	signed: Boolean = false
) : AbstractStorable() {

	var fractionSize: Int = fractionSize
		private set

	var signed: Boolean = signed
		private set

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeInt("fraction", fractionSize)
		writer.writeBoolean("signed", signed)
	}

	override fun read(reader: StoreReader) {
		fractionSize = reader.readInt("fraction")
		signed = reader.readBoolean("signed")
	}

	/** ---- [FixedPointConfig] */

	fun withFractionSize(fractionSize: Int): FixedPointConfig =
		FixedPointConfig(fractionSize, this.signed)

	fun withSigned(signed: Boolean): FixedPointConfig =
		FixedPointConfig(this.fractionSize, signed)

	fun minValue(bitWidth: BitWidth): Double {
		require(fractionSize <= bitWidth.width)
		return if (signed) {
			-BitOperation.power(integerSize(bitWidth)).toDouble()
		} else {
			0.0
		}
	}

	fun maxValue(bitWidth: BitWidth): Double {
		require(fractionSize <= bitWidth.width)
		return BitOperation.power(integerSize(bitWidth)).toDouble() - 1 / BitOperation.power(fractionSize.toByte()).toDouble()
	}

	/**
	 * A rough estimate of the numbers of digits (including minus sign) used to
	 * calculate the space required to display a signal with this [FixedPointConfig] and
	 * the specified [bitWidth].
	 */
	fun decimalDigitCount(bitWidth: BitWidth): Int {
		val p: Int = BitOperation.power(integerSize(bitWidth)).toInt()
		val q: Double = 1.0 /  BitOperation.power(fractionSize.toByte()).toDouble()
		return 1 + p.toString().length + q.toString().length
	}

	private fun integerSize(bitWidth: BitWidth): Byte =
		if (signed) {
			bitWidth.width - fractionSize - 1
		} else {
			bitWidth.width - fractionSize
		}.toByte()
}