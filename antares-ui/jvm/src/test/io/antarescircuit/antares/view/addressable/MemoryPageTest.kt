package io.antarescircuit.antares.view.addressable

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MemoryPageTest {
	@Test
	fun shouldReachEveryAddressWithBoundedPagesInEveryLayout() {
		for (columns in listOf(1, 4, 8, 16)) {
			val page = MemoryPage.containing(65_535, 65_535, columns)
			assertEquals(1_024, page.rowCount)
			assertEquals(65_535, page.end)
			assertEquals(65_535, page.addressAt(1_023, columns - 1))
			assertEquals(page.start - 1_024 * columns, page.previous)
			assertNull(page.next)
		}
	}

	@Test
	fun shouldHandleTheLastIntAddressWithoutOverflow() {
		for (columns in listOf(1, 4, 8, 16)) {
			val page = MemoryPage.containing(Int.MAX_VALUE, Int.MAX_VALUE, columns)
			assertEquals(Int.MAX_VALUE, page.end)
			assertEquals(Int.MAX_VALUE, page.addressAt(1_023, columns - 1))
			assertNull(page.next)
		}
	}

	@Test
	fun shouldExposePartialRowsWithoutInventingAddresses() {
		val page = MemoryPage.containing(1, 1, 16)
		assertEquals(1, page.rowCount)
		assertEquals(0, page.addressAt(0, 0))
		assertEquals(1, page.addressAt(0, 1))
		assertNull(page.addressAt(0, 2))
		assertNull(page.addressAt(1, 0))
		assertNull(page.previous)
		assertNull(page.next)
	}

	@Test
	fun shouldNavigateAcrossPageBoundaries() {
		val page = MemoryPage.containing(1_024, 2_049, 1)
		assertEquals(1_024, page.start)
		assertEquals(0, page.previous)
		assertEquals(2_048, page.next)
		val finalPage = MemoryPage.containing(page.next!!, 2_049, 1)
		assertEquals(2, finalPage.rowCount)
		assertEquals(2_049, finalPage.end)
	}

	@Test
	fun shouldParseCurrentRadixAndExplicitPrefixes() {
		assertEquals(255, MemoryPage.parseAddress(" ff ", AddressableValueConverter.Hexadecimal, 255))
		assertEquals(10, MemoryPage.parseAddress("10", AddressableValueConverter.Decimal, 255))
		assertEquals(8, MemoryPage.parseAddress("10", AddressableValueConverter.Octal, 255))
		assertEquals(2, MemoryPage.parseAddress("10", AddressableValueConverter.Binary, 255))
		assertEquals(255, MemoryPage.parseAddress("0xFF", AddressableValueConverter.Decimal, 255))
		assertEquals(5, MemoryPage.parseAddress("0b101", AddressableValueConverter.Decimal, 255))
		assertEquals(100, MemoryPage.parseAddress("0d100", AddressableValueConverter.Octal, 255))
		// In hexadecimal mode, "0B" and "0D" are digits, as shown in padded row headers
		assertEquals(0xB00, MemoryPage.parseAddress("0B00", AddressableValueConverter.Hexadecimal, 0xFFFF))
		assertEquals(0xD10, MemoryPage.parseAddress("0d10", AddressableValueConverter.Hexadecimal, 0xFFFF))
		assertEquals(Int.MAX_VALUE, MemoryPage.parseAddress("7fffffff", AddressableValueConverter.Hexadecimal, Int.MAX_VALUE))
	}

	@Test
	fun shouldRejectInvalidOutOfRangeAndOverflowingAddresses() {
		for (text in listOf("", " ", "-1", "100", "0x100", "0x", "0b100", "oops", "ffffffffffffffff")) {
			assertNull(MemoryPage.parseAddress(text, AddressableValueConverter.Hexadecimal, 255), text)
		}
	}
}
