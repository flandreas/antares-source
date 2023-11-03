package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.StringUtils

object DigitalLiteral {

	fun parseBinary(text: String): DigitalSignal {
		val s = text.trim()

		if (StringUtils.isBlank(s)) {
			throw IllegalArgumentException("empty")
		}
		if (s.length > BitWidth.MAX) {
			throw IllegalArgumentException("length ${s.length} greater that supported max ${BitWidth.MAX}")
		}

		val bits = mutableListOf<Bit>()
		s.reversed().forEach { bits.add(Bit.of(it)) }

		return Word(bits)
	}

	fun parseHex(text: String): DigitalSignal {
		val s = text.trim()

		if (StringUtils.isBlank(s)) {
			throw IllegalArgumentException("empty")
		}

		if (s.length * 4 > BitWidth.MAX) {
			throw IllegalArgumentException("${s.length} too large to be represented with ${BitWidth.MAX} bits")
		}
		val bits = mutableListOf<Bit>()
		s.reversed().forEach { bits.addAll(BitOperation.hexDigitToBits(it)) }

		return Word(bits)
	}
}