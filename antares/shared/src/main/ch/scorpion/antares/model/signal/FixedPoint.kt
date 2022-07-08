package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.io.*

/**
 * A configuration for [FixedPoint].
 * Setters must be public for reflective access by property editors on JVM.
 */
class FixedPointConfig(
	var fractionSize: Int = 0,
	var signed: Boolean = false
) : AbstractStorable() {

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeInt("fraction", fractionSize)
		writer.writeBoolean("signed", signed)
	}

	override fun read(reader: StoreReader) {
		fractionSize = reader.readInt("fraction")
		signed = reader.readBoolean("signed")
	}
}

/**
 * A signed fixed-point value consisting of bits from a [DigitalSignal], where
 * the most-left [FixedPointConfig.fractionSize] bits are interpreted as fraction value.
 * If the most-significant bit is 1, the overall number is negative.
 *
 * If [signal] has an [Bit.Undefined] or [Bit.Error], the corresponding [isUndefined] or [isError]
 * is set, and [value] is 0.0.
 */
class FixedPoint(
	config: FixedPointConfig,
	val signal: DigitalSignal
) {
	companion object {

		private fun calculateValue(config: FixedPointConfig, signal: DigitalSignal): Double =
			if (config.signed && signal.msb.isSet) {
				// Negative
				val v = BitOperation.power((signal.bitWidth.width - 1).toByte())
				-(signal.getValue() - v).toDouble() / (BitOperation.power(config.fractionSize.toByte()).toDouble())
			} else {
				// Positive
				signal.getValue().toDouble() / (BitOperation.power(config.fractionSize.toByte()).toDouble())
			}

		/**
		 * @throws IllegalArgumentException
		 */
		private fun validate(config: FixedPointConfig, value: DigitalSignal) {
			require(config.fractionSize <= value.bitWidth.width)
		}
	}

	val isError: Boolean

	val isUndefined: Boolean

	init {
		validate(config, signal)
		isError = signal.hasError
		isUndefined = signal.isPartiallyUndefined
	}

	val value: Double = if (isError || isUndefined) 0.0 else calculateValue(config, signal)

	override fun toString(): String {
		return if (isError) {
			"E"
		} else if (isUndefined) {
			"Z"
		} else {
			value.toString()
		}
	}
}