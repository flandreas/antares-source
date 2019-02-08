package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/** Unit tests for [Thousands].*/
class ThousandsTest {

	@BeforeTest
	fun setup() {
		BaseModuleJvm.require()
	}

	@Test
	fun shouldRound() {
		assertEquals("0.9", Thousands.round(0.9, 2))
		assertEquals("1", Thousands.round(1.0, 2))
		assertEquals("1.2", Thousands.round(1.2, 2))
		assertEquals("1.23", Thousands.round(1.23, 2))
		assertEquals("1.23", Thousands.round(1.234, 2))
	}

	@Test
	fun shouldConvertHundreds() {
		assertEquals("4", Thousands.convert(4))
		assertEquals("38", Thousands.convert(38))
		assertEquals("999", Thousands.convert(999))
	}

	@Test
	fun shouldConvertThousands() {
		assertEquals("1K", Thousands.convert(1_000))
		assertEquals("1K", Thousands.convert(1_001))
		assertEquals("1.01K", Thousands.convert(1_010))
		assertEquals("1.1K", Thousands.convert(1_100))
		assertEquals("1.11K", Thousands.convert(1_111))
		assertEquals("11.1K", Thousands.convert(11_111))
		assertEquals("99.9K", Thousands.convert(99_999))
		assertEquals("100K", Thousands.convert(100_000))
		assertEquals("999K", Thousands.convert(999_999))
	}

	@Test
	fun shouldConvertMillions() {
		assertEquals("1M", Thousands.convert(1_000_000))
		assertEquals("13.3M", Thousands.convert(13_333_424))
	}

	@Test
	fun shouldConvertGigas() {
		assertEquals("1G", Thousands.convert(1_000_000_000))
		assertEquals("54.1G", Thousands.convert(54_123_456_789))
	}

	@Test
	fun shouldConvertTeras() {
		assertEquals("1T", Thousands.convert(1_000_000_000_000))
		assertEquals("99T", Thousands.convert(99_010_000_000_000))
		assertEquals("123T", Thousands.convert(123_456_123_777_998))
	}
}