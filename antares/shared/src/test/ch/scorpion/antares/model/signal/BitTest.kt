package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import kotlin.test.*

/**
 * Unit tests for [Bit].
 */
class BitTest {

	@Test
	fun shouldGetNumericalValue() {
		assertEquals(1, Bit.of(true).numericalValue)
		assertEquals(0, Bit.of(false).numericalValue)
	}

	@Test
	fun shouldNotGetUndefinedValue() {
		assertFailsWith<NullPointerException> {
			Bit.Undefined.numericalValue
		}
	}

	@Test
	fun shouldProvideForBoolean() {
		assertEquals(Bit.True, Bit.of(true))
		assertEquals(Bit.False, Bit.of(false))
	}

	@Test
	fun shouldProvideForInteger() {
		assertEquals(Bit.False, Bit.of(0))
		assertEquals(Bit.True, Bit.of(1))
	}

	@Test
	fun shouldRejectInvalidInteger() {
		assertFailsWith<IllegalArgumentException> {
			Bit.of(2)
		}
	}

	@Test
	fun shouldBeEqual() {
		assertEquals(Bit.of(0), Bit.of(0))
		assertEquals(Bit.of(1), Bit.of(1))
	}

	@Test
	fun shouldConvertToBinaryString() {
		assertEquals("Z", Bit.Undefined.toBinaryString())
		assertEquals("X", Bit.Error.toBinaryString())
		assertEquals("0", Bit.of(0).toBinaryString())
		assertEquals("1", Bit.of(1).toBinaryString())
	}

	@Test
	fun shouldConvertToHexString() {
		assertEquals("Z", Bit.Undefined.toHexString())
		assertEquals("X", Bit.Error.toHexString())
		assertEquals("0", Bit.False.toHexString())
		assertEquals("1", Bit.True.toHexString())
	}

	@Test
	fun shouldCalculateNot() {
		assertEquals(Bit.of(false), Bit.of(true).not())
		assertEquals(Bit.of(true), Bit.of(false).not())
		assertEquals(Bit.Error, Bit.Error.not())
		assertEquals(Bit.Undefined, Bit.Undefined.not())
	}

	@Test
	fun shouldConvertIntToBitList() {
		val list: List<Bit> = Bit.listFromInt(5, 3)
		assertEquals(Bit.True, list[0])
		assertEquals(Bit.False, list[1])
		assertEquals(Bit.True, list[2])
	}

	@Test
	fun shouldBeConsistent() {
		assertTrue(Bit.True.isConsistentWith(Bit.True))
		assertTrue(Bit.False.isConsistentWith(Bit.False))
		assertTrue(Bit.True.isConsistentWith(Bit.Undefined))
		assertTrue(Bit.Undefined.isConsistentWith(Bit.True))
		assertTrue(Bit.Undefined.isConsistentWith(Bit.Undefined))
	}

	@Test
	fun shouldNotBeConsistent() {
		assertFalse(Bit.True.isConsistentWith(Bit.False))
		assertFalse(Bit.False.isConsistentWith(Bit.True))
	}
}