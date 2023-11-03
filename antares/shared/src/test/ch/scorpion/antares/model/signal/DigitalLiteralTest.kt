package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.signal.DigitalLiteral.parseBinary
import ch.scorpion.antares.model.signal.DigitalLiteral.parseHex
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DigitalLiteralTest {

	@Test
	fun shouldParseBinary() {
		assertEquals(of(BitWidth.BW_1, 0), parseBinary("0"))
		assertEquals(of(BitWidth.BW_1, 1), parseBinary("1"))
		assertEquals(of(BitWidth.BW_4, 9), parseBinary("1001"))
		assertEquals(of(BitWidth.BW_8, 9), parseBinary("00001001"))

		assertNotEquals(of(BitWidth.BW_8, 10), parseBinary("00001001"))


		assertTrue(parseBinary("1Z01").isPartiallyUndefined)
		assertTrue(parseBinary("1X").hasError)
	}

	@Test
	fun shouldParseHex() {
		assertEquals(of(BitWidth.BW_4, 0), parseHex("0"))
		assertEquals(of(BitWidth.BW_4, 1), parseHex("1"))
		assertEquals(of(BitWidth.BW_4, 15), parseHex("F"))
		assertEquals(of(BitWidth.BW_8, 255), parseHex("FF"))

		assertTrue(parseHex("AZ").isPartiallyUndefined)
		assertTrue(parseHex("ZZ").isFullyUndefined)
		assertTrue(parseHex("X9").hasError)
	}
}