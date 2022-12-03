package ch.scorpion.antares.model.signal

object DigitalSignalFactory {

	fun of(bitValue: Boolean): DigitalSignal = DefinedWord.of(bitValue)

	fun of(bit: Bit): DigitalSignal = if (bit.isDefined) DefinedWord.of(bit.isSet) else Word.of(bit)

	fun of(bitWidth: BitWidth, value: Long?): DigitalSignal =
		if (value != null) DefinedWord.of(bitWidth, value.toULong()) else Word.of(bitWidth, value)

	fun of(bitWidth: BitWidth, value: ULong?): DigitalSignal =
		if (value != null) DefinedWord.of(bitWidth, value) else Word.of(bitWidth, value)

	fun of(words: List<DigitalSignal>): DigitalSignal = Word.of(words)

	fun of(bitWidth: BitWidth, hexValue: String): DigitalSignal = Word.of(bitWidth, hexValue)

	fun ofBits(bits: List<Bit>): DigitalSignal = Word(bits)

	fun ofMinimalBitWidth(value: ULong): DigitalSignal = Word.ofMinimalBitWidth(value)

	fun undefined(bitWidth: BitWidth): DigitalSignal = Word.undefined(bitWidth)

	fun error(bitWidth: BitWidth): DigitalSignal = Word.error(bitWidth)

	fun falseValue(bitWidth: BitWidth): DigitalSignal = DefinedWord.of(bitWidth, 0UL)

	fun trueValue(bitWidth: BitWidth): DigitalSignal = DefinedWord.of(bitWidth, bitWidth.maxValue)

	fun allOf(bitWidth: BitWidth, bit: Bit): DigitalSignal =
		if (bit.isDefined) {
			if (bit.isSet) DefinedWord.of(bitWidth, bitWidth.maxValue) else DefinedWord.of(bitWidth, 0UL)
		} else {
			Word.allOf(bitWidth, bit)
		}

	fun random(bitWidth: BitWidth): DigitalSignal = DefinedWord.random(bitWidth)
}