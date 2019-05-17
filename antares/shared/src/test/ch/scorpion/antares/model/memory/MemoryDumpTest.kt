package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [MemoryDump].
 */
class MemoryDumpTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    /** ---- Write 8 bit tests */

    @Test
    fun shouldWriteEmptyData() {
        val memory = Memory()
        assertEquals("", MemoryDump.write(memory, BitWidth.BW_8))
    }

    @Test
    fun shouldWriteArbitrary8BitData() {
        val memory = Memory()
        memory.write(0, 4)
        memory.write(1, 255)
        memory.write(2, 255)
        memory.write(8, 1)
        memory.write(10, 2)

        assertEquals("04 FF FF 00 00 00 00 00 01 00 02", MemoryDump.write(memory, BitWidth.BW_8))
    }

    /** ---- Write 16 bit tests */

    @Test
    fun shouldWriteArbitrary16BitData() {
        val memory = Memory()
        memory.write(0, 4)
        memory.write(1, 65535)
        memory.write(2, 255)
        memory.write(8, 1)
        memory.write(10, 138)

        assertEquals("0004 FFFF 00FF 0000 0000 0000 0000 0000 0001 0000 008A", MemoryDump.write(memory, BitWidth.BW_16))
    }

    /** ---- Read 8 bit data */

    @Test
    fun shouldClearBeforeRead() {
        val memory = Memory()
        memory.write(10, 255)

        MemoryDump.read(memory, "01 02 03")

        assertEquals(0L, memory.read(10))
    }

    @Test
    fun shouldReadEmptyData() {
        val memory = Memory(8)
        MemoryDump.read(memory, "")
        assertEquals(0L, memory.read(0))
    }

    @Test
    fun shouldReadArbitrary8BitData() {
        val memory = Memory()

        MemoryDump.read(memory, "04 00 FF 1A 00 00 08")

        assertEquals(4L, memory.read(0))
        assertEquals(0L, memory.read(1))
        assertEquals(255L, memory.read(2))
        assertEquals(26L, memory.read(3))
        assertEquals(0L, memory.read(4))
        assertEquals(0L, memory.read(5))
        assertEquals(8L, memory.read(6))
        assertEquals(0L, memory.read(7))
    }

	@Test
	fun shouldReadMultipleAddressesPerLine() {
		val memory = Memory()

		MemoryDump.readNewlineSeparated(memory, "00 01 02 03\n04 05 06 07")

		for (index in 0..7) {
			assertEquals(index, memory.read(index).toInt())
		}
	}

    /** ---- Read 16 bit test */

    @Test
    fun shouldReadArbitrary16BitData() {
        val memory = Memory()

        MemoryDump.read(memory, "0004 FFFF 00FF 001A 0000 0000 0008")

        assertEquals(4L, memory.read(0))
        assertEquals(65535L, memory.read(1))
        assertEquals(255L, memory.read(2))
        assertEquals(26L, memory.read(3))
        assertEquals(0L, memory.read(4))
        assertEquals(0L, memory.read(5))
        assertEquals(8L, memory.read(6))
        assertEquals(0L, memory.read(7))
    }

    /** ---- Read 32 bit tests */

    @Test
    fun shouldReadNewlineSeparatedData() {
        val memory = Memory(8)

        MemoryDump.read(memory, "10C00000\n00106000\nB013001C\n24143313\n3414040B\n30000409\n10C03000\n10400000\nF0110000")
    }

	/** ---- Comment tests */

	@Test
	fun shouldWriteComment() {
		val memory = Memory()
		memory.write(0, 4)
		memory.writeCommentedValue(1, 255, "Comment1")
		memory.write(2, 8)

		assertEquals("04 FF:Comment1 08", MemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldReadComment() {
		val memory = Memory()

		MemoryDump.read(memory, "04 FF:Comment1 08")

		assertEquals(4L, memory.read(0))
		assertEquals(255L, memory.read(1))
		assertEquals("Comment1", memory.readComment(1))
		assertEquals(8L, memory.read(2))
	}

	@Test
	fun shouldWriteEscapedComment() {
		val memory = Memory()
		memory.write(0, 4)
		memory.writeCommentedValue(1, 255, "Bla:Blu Bli")
		memory.write(2, 8)

		assertEquals("04 FF:Bla\\:Blu\\ Bli 08", MemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldReadEscapedComment() {
		val memory = Memory()

		MemoryDump.read(memory, "04 FF:Bla\\:Blu\\ Bli 08")

		assertEquals(4L, memory.read(0))
		assertEquals(255L, memory.read(1))
		assertEquals("Bla:Blu Bli", memory.readComment(1))
		assertEquals(8L, memory.read(2))
	}

	@Test
	fun shouldReadExtendedExample() {
		val data = """
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

		MemoryDump.readNewlineSeparated(memory, data)

		assertEquals("7000".toLong(16), memory.read(0))
		assertEquals("Initialize I at 0800", memory.readComment(0))
	}
}