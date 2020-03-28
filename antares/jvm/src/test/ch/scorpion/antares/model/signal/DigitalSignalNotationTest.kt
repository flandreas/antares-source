package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.*
import org.junit.Assert.*
import kotlin.test.Test

class DigitalSignalNotationTest {

	private val value = Word.of(BitWidth.BW_4, 15)

	@Test
	fun shouldNotateWithPrefix() {
		assertEquals("0b1111", DigitalSignalNotation.PREFIX.notate(value, BINARY))
		assertEquals("0xF", DigitalSignalNotation.PREFIX.notate(value, HEXADECIMAL))
	}

	@Test
	fun shouldNotateWithBaseSubscript() {
		assertEquals("1111\u2082", DigitalSignalNotation.BASE_SUBSCRIPT.notate(value, BINARY))
		assertEquals("F\u2081\u2086", DigitalSignalNotation.BASE_SUBSCRIPT.notate(value, HEXADECIMAL))
	}

	@Test
	fun shouldNotateWithSuffix() {
		assertEquals("1111b", DigitalSignalNotation.SUFFIX.notate(value, BINARY))
		assertEquals("Fh", DigitalSignalNotation.SUFFIX.notate(value, HEXADECIMAL))
	}
}