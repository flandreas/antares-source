package ch.scorpion.jabbah.base.math

import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun assertNear(actual: Float, expected: Float) {
	assertTrue(actual.near(expected), "$actual expected to be near $expected")
}

class MathTest {

	@BeforeTest
	fun setup() {
		BaseModule.require()
	}

	@Test
	fun shouldFormatRounded() {
		assertEquals("5.123", 5.123001.formatRounded())
		assertEquals("5.124", 5.1239.formatRounded())
		assertEquals("5.123", 5.123.formatRounded())
		assertEquals("0.0", 0.000.formatRounded())
	}
}