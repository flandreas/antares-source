package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.model.signal.BitWidth
import kotlin.test.*

class LogisimMemoryDumpTest {

	@Test
	fun shouldApplyZeroOverwritesInAddressedImages() {
		val memory = Memory()
		LogisimMemoryDump.read(memory, "v3.0 hex words addressed\n0: ff aa\n0: 00")
		assertEquals(0UL, memory.read(0))
		assertEquals(0xaaUL, memory.read(1))
	}

	@Test
	fun shouldReadRawRunsWhitespaceAndComments() {
		val memory = Memory()
		LogisimMemoryDump.read(memory, "\uFEFFv2.0 raw\r\n02\t03 2*00 # gap\r\nff 3*a", 7, 255UL)
		assertEquals(listOf(2UL, 3UL, 0UL, 0UL, 255UL, 10UL, 10UL, 10UL), (0..7).map { memory.read(it) })
	}

	@Test
	fun shouldReadPlainAndAddressedWords() {
		val memory = Memory()
		LogisimMemoryDump.read(memory, "v3.0 hex words plain\n0x001 abc\nfff", 7, 4095UL)
		assertEquals(listOf(1UL, 0xabcUL, 0xfffUL), (0..2).map { memory.read(it) })
		LogisimMemoryDump.read(memory, "v3.0 hex words addressed\n10: 001 abc  comment\n20: fff", 255, 4095UL)
		assertEquals(0UL, memory.read(0))
		assertEquals(1UL, memory.read(16))
		assertEquals(0xabcUL, memory.read(17))
		assertEquals(0xfffUL, memory.read(32))
	}

	@Test
	fun shouldRoundTripSparseImagesWithoutExpandingGaps() {
		val memory = Memory().apply {
			write(0, 42UL, 42UL, 42UL)
			write(100_000_000, 255UL)
			writeComment(10, "only Antares preserves comments")
		}
		for (dump in listOf(LogisimMemoryDump.writeRaw(memory, BitWidth.BW_8), LogisimMemoryDump.writeAddressed(memory, BitWidth.BW_8))) {
			assertTrue(dump.length < 150)
			val restored = Memory()
			MemoryDump.read(restored, dump)
			assertEquals(42UL, restored.read(2))
			assertEquals(255UL, restored.read(100_000_000))
			assertEquals(0UL, restored.read(99_999_999))
			assertNull(restored.readComment(10))
		}
	}

	@Test
	fun shouldRejectInvalidImagesWithoutChangingMemory() {
		val memory = Memory().apply { writeCommentedValue(5, 99UL, "keep") }
		for (dump in listOf(
			"v2.0 raw\n01 invalid",
			"v2.0 raw\n9*00",
			"v2.0 raw\n-1*01",
			"v2.0 raw\n0*01",
			"v2.0 raw\n100",
			"v3.0 hex words addressed\n8: ff",
			"v3.0 hex words addressed\n0: ff\n1 ff",
			"v3.0 hex bytes plain\nff",
		)) {
			assertFailsWith<IllegalArgumentException>(dump) { LogisimMemoryDump.read(memory, dump, 7, 255UL) }
			assertEquals(MemoryCell(5, 99UL, "keep"), memory.getNonZeroCells().next())
		}
	}

	@Test
	fun shouldReadLargeZeroRunInConstantSpace() {
		val memory = Memory()
		LogisimMemoryDump.read(memory, "v2.0 raw\n1000000000*00 ff")
		assertEquals(listOf(MemoryCell(1_000_000_000, 255UL)), memory.getNonZeroCells().asSequence().toList())
	}
}
