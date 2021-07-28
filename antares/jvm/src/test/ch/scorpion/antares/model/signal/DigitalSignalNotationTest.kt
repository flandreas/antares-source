package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.*
import org.junit.Assert.*
import kotlin.test.Test

class DigitalSignalNotationTest {

	private val value = DigitalSignalFactory.of(BitWidth.BW_8, 17)
	private val singleDigitValue = DigitalSignalFactory.of(BitWidth.BW_1, 1)

	@Test
	fun shouldNotateWithPrefix() {
		assertEquals("0b00010001", DigitalSignalNotation.PREFIX.notate(value, BINARY))
		assertEquals("0x11", DigitalSignalNotation.PREFIX.notate(value, HEXADECIMAL))
	}

	@Test
	fun shouldOmitPrefixForSingleDigit() {
		assertEquals("1", DigitalSignalNotation.PREFIX.notate(DigitalSignalFactory.of(BitWidth.BW_1, 1), BINARY))
		assertEquals("F", DigitalSignalNotation.PREFIX.notate(DigitalSignalFactory.of(BitWidth.BW_4, 15), HEXADECIMAL))
	}

	@Test
	fun shouldNotateWithBaseSubscript() {
		assertEquals("00010001\u2082", DigitalSignalNotation.BASE_SUBSCRIPT.notate(value, BINARY))
		assertEquals("11\u2081\u2086", DigitalSignalNotation.BASE_SUBSCRIPT.notate(value, HEXADECIMAL))
	}

	@Test
	fun shouldOmitSubscriptForSingleDigit() {
		assertEquals("1", DigitalSignalNotation.BASE_SUBSCRIPT.notate(DigitalSignalFactory.of(BitWidth.BW_1, 1), BINARY))
		assertEquals("F", DigitalSignalNotation.BASE_SUBSCRIPT.notate(DigitalSignalFactory.of(BitWidth.BW_4, 15), HEXADECIMAL))
	}

	@Test
	fun shouldNotateWithSuffix() {
		assertEquals("00010001b", DigitalSignalNotation.SUFFIX.notate(value, BINARY))
		assertEquals("11h", DigitalSignalNotation.SUFFIX.notate(value, HEXADECIMAL))
	}

	@Test
	fun shouldNotateWithSuffixUppercase() {
		assertEquals("00010001B", DigitalSignalNotation.SUFFIX_UPPERCASE.notate(value, BINARY))
		assertEquals("11H", DigitalSignalNotation.SUFFIX_UPPERCASE.notate(value, HEXADECIMAL))
	}

	@Test
	fun shouldOmitSuffixForSingleDigit() {
		assertEquals("1", DigitalSignalNotation.SUFFIX.notate(DigitalSignalFactory.of(BitWidth.BW_1, 1), BINARY))
		assertEquals("F", DigitalSignalNotation.SUFFIX.notate(DigitalSignalFactory.of(BitWidth.BW_4, 15), HEXADECIMAL))
	}
}