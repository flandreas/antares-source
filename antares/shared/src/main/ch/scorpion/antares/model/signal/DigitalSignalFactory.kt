package ch.scorpion.antares.model.signal

object DigitalSignalFactory {

	fun of(bitValue: Boolean): DigitalSignal = Word.of(bitValue)

	fun of(bit: Bit): DigitalSignal = Word.of(bit)

	fun of(bitWidth: BitWidth, value: Long?): DigitalSignal = of(bitWidth, value?.toULong())

	fun of(bitWidth: BitWidth, value: ULong?): DigitalSignal = Word.of(bitWidth, value)

	fun of(words: List<DigitalSignal>): DigitalSignal = Word.of(words)

	fun of(bitWidth: BitWidth, hexValue: String): DigitalSignal = Word.of(bitWidth, hexValue)

	fun ofBits(bits: List<Bit>): DigitalSignal = Word(bits)

	fun undefined(bitWidth: BitWidth): DigitalSignal = Word.undefined(bitWidth)

	fun error(bitWidth: BitWidth): DigitalSignal = Word.error(bitWidth)

	fun falseValue(bitWidth: BitWidth): DigitalSignal = Word.falseValue(bitWidth)

	fun trueValue(bitWidth: BitWidth): DigitalSignal = Word.trueValue(bitWidth)

	fun allOf(bitWidth: BitWidth, bit: Bit): DigitalSignal = Word.allOf(bitWidth, bit)

	fun random(bitWidth: BitWidth): DigitalSignal = Word.random(bitWidth)
}