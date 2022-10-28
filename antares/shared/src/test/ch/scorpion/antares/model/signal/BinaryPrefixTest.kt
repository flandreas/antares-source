package ch.scorpion.antares.model.signal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BinaryPrefixTest {

	@Test
	fun shouldYield() {
		assertEquals("1", BinaryPrefix.of(0))
		assertEquals("2", BinaryPrefix.of(1))
		assertEquals("256", BinaryPrefix.of(8))
		assertEquals("4 Ki", BinaryPrefix.of(12))
		assertEquals("64 Ki", BinaryPrefix.of(16))
		assertEquals("1 Mi", BinaryPrefix.of(20))
		assertEquals("8 Mi", BinaryPrefix.of(23))
		assertEquals("256 Mi", BinaryPrefix.of(28))
		assertEquals("512 Mi", BinaryPrefix.of(29))
		assertEquals("1 Gi", BinaryPrefix.of(30))
		assertEquals("4 Gi", BinaryPrefix.of(32))
		assertEquals("1 Ti", BinaryPrefix.of(40))
		assertEquals("2 Ti", BinaryPrefix.of(41))
		assertEquals("1 Pi", BinaryPrefix.of(50))
		assertEquals("1 Ei", BinaryPrefix.of(60))
		assertEquals("16 Ei", BinaryPrefix.of(64))
	}

	@Test
	fun shouldNotYieldAboveMaxBitWidth() {
		assertFailsWith(IllegalArgumentException::class) {
			BinaryPrefix.of(BitWidth.MAX + 1)
		}
	}
}