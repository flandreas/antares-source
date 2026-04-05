package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.BitWidth
import kotlin.test.*

class MemoryDumpTest {

	init {
		AntaresTestRule.configure()
	}

	/** ---- File version tests */

	@Test
	fun shouldReadDataInNewlineVersion() {
		val data = """
			|#amd-nl-0.1
			|40:Comment 1
			|F7
			|0A:Comment 2
		""".trimMargin()

		val memory = Memory()

		MemoryDump.read(memory, data)

		assertEquals("40".toULong(16), memory.read(0))
		assertEquals("Comment 1", memory.readComment(0))
		assertEquals("F7".toULong(16), memory.read(1))
		assertEquals("0A".toULong(16), memory.read(2))
		assertEquals("Comment 2", memory.readComment(2))
	}

	@Test
	fun shouldReadDataInDefaultVersion() {
		val data = """
			|#amd-df-0.1
			|40:Comment\ 1 F7 0A:Comment\ 2
		""".trimMargin()

		val memory = Memory()

		MemoryDump.read(memory, data)

		assertEquals("40".toULong(16), memory.read(0))
		assertEquals("Comment 1", memory.readComment(0))
		assertEquals("F7".toULong(16), memory.read(1))
		assertEquals("0A".toULong(16), memory.read(2))
		assertEquals("Comment 2", memory.readComment(2))
	}


	/** ---- Write 8-bit tests */

    @Test
    fun shouldWriteEmptyData() {
        val memory = Memory()
        assertEquals("", MemoryDump.write(memory, BitWidth.BW_8))
    }

    @Test
    fun shouldWriteArbitrary8BitData() {
        val memory = Memory()
        memory.write(0, 4UL)
        memory.write(1, 255UL)
        memory.write(2, 255UL)
        memory.write(8, 1UL)
        memory.write(10, 2UL)

        assertEquals("04 FF FF 00 00 00 00 00 01 00 02", MemoryDump.write(memory, BitWidth.BW_8))
    }

    /** ---- Write 16-bit tests */

    @Test
    fun shouldWriteArbitrary16BitData() {
        val memory = Memory()
        memory.write(0, 4UL)
        memory.write(1, 65535UL)
        memory.write(2, 255UL)
        memory.write(8, 1UL)
        memory.write(10, 138UL)

        assertEquals("0004 FFFF 00FF 0000 0000 0000 0000 0000 0001 0000 008A", MemoryDump.write(memory, BitWidth.BW_16))
    }

    /** ---- Read 8-bit data */

    @Test
    fun shouldClearBeforeRead() {
        val memory = Memory()
        memory.write(10, 255UL)

        MemoryDump.read(memory, "01 02 03")

        assertEquals(0UL, memory.read(10))
    }

    @Test
    fun shouldReadEmptyData() {
        val memory = Memory(8)
        MemoryDump.read(memory, "")
        assertEquals(0UL, memory.read(0))
    }

    @Test
    fun shouldReadArbitrary8BitData() {
        val memory = Memory()

        MemoryDump.read(memory, "04 00 FF 1A 00 00 08")

        assertEquals(4UL, memory.read(0))
        assertEquals(0UL, memory.read(1))
        assertEquals(255UL, memory.read(2))
        assertEquals(26UL, memory.read(3))
        assertEquals(0UL, memory.read(4))
        assertEquals(0UL, memory.read(5))
        assertEquals(8UL, memory.read(6))
        assertEquals(0UL, memory.read(7))
    }

	@Test
	fun shouldReadMultipleAddressesPerLine() {
		val memory = Memory()

		MemoryDump.read(memory, "00 01 02 03\n04 05 06 07")

		for (index in 0..7) {
			assertEquals(index, memory.read(index).toInt())
		}
	}

    /** ---- Read 16-bit test */

    @Test
    fun shouldReadArbitrary16BitData() {
        val memory = Memory()

        MemoryDump.read(memory, "0004 FFFF 00FF 001A 0000 0000 0008")

        assertEquals(4UL, memory.read(0))
        assertEquals(65535UL, memory.read(1))
        assertEquals(255UL, memory.read(2))
        assertEquals(26UL, memory.read(3))
        assertEquals(0UL, memory.read(4))
        assertEquals(0UL, memory.read(5))
        assertEquals(8UL, memory.read(6))
        assertEquals(0UL, memory.read(7))
    }

    /** ---- Read 32-bit tests */

    @Test
    fun shouldReadNewlineSeparatedData() {
        val memory = Memory(8)

        MemoryDump.read(memory, "10C00000\n00106000\nB013001C\n24143313\n3414040B\n30000409\n10C03000\n10400000\nF0110000")
    }

	/** ---- Comment tests */

	@Test
	fun shouldSkipLineComment() {
		val memory = Memory()

		MemoryDump.read(memory, "00 01 02 03\n# Comment\n04 05 06 07")

		for (index in 0..7) {
			assertEquals(index, memory.read(index).toInt())
		}
	}

	@Test
	fun shouldWriteCellComment() {
		val memory = Memory()
		memory.write(0, 4UL)
		memory.writeCommentedValue(1, 255UL, "Comment1")
		memory.write(2, 8UL)

		assertEquals("04 FF:Comment1 08", MemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldReadCellComment() {
		val memory = Memory()

		MemoryDump.read(memory, "04 FF:Comment1 08")

		assertEquals(4UL, memory.read(0))
		assertEquals(255UL, memory.read(1))
		assertEquals("Comment1", memory.readComment(1))
		assertEquals(8UL, memory.read(2))
	}

	@Test
	fun shouldWriteEscapedCellComment() {
		val memory = Memory()
		memory.write(0, 4UL)
		memory.writeCommentedValue(1, 255UL, "Bla:Blu Bli")
		memory.write(2, 8UL)

		assertEquals("04 FF:Bla\\:Blu\\ Bli 08", MemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldReadEscapedCellComment() {
		val memory = Memory()

		MemoryDump.read(memory, "04 FF:Bla\\:Blu\\ Bli 08")

		assertEquals(4UL, memory.read(0))
		assertEquals(255UL, memory.read(1))
		assertEquals("Bla:Blu Bli", memory.readComment(1))
		assertEquals(8UL, memory.read(2))
	}

	@Test
	fun shouldReadExtendedExample() {
		val data = """
			|#amd-nl-0.1
			|7000:Initialize I at 0800
			|1800
			|7065:Initialize L at 0801
			|1801
			|7000:Initialize S at 0802
			|1802
			|7001:Initialize ONE at 0803
			|1803
			|0801:IF (I == L) THEN GOTO END
			|3800
			|5013
			|0802:S = S + I
			|2800
			|1802
			|1FFE:Output S
			|0800:I = I + 1
			|2803
			|1800
			|6008:GOTO LOOP
			|FF00:Stop program
		""".trimMargin()

		val memory = Memory()

		MemoryDump.read(memory, data)

		assertEquals("7000".toULong(16), memory.read(0))
		assertEquals("Initialize I at 0800", memory.readComment(0))
	}

	/** ---- Exception handling tests */

	@Test
	fun shouldThrowWithIllegalSyntax() {
		val e = assertFails {
			MemoryDump.read(Memory(), "00 FF:Comment:Hallo")
		}
		assertIs<IllegalArgumentException>(e)
		assertEquals("Illegal syntax in 'FF:Comment:Hallo' at address 1", e.message)
	}

	@Test
	fun shouldThrowWithIllegalValue() {
		val e = assertFails {
			MemoryDump.read(Memory(), "00 XY")
		}
		assertIs<IllegalArgumentException>(e)
		assertEquals("Illegal hex number 'XY' at address 1", e.message)
	}
}