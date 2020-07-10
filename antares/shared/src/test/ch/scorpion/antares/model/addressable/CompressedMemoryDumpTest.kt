package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Unit tests for [CompressedMemoryDump].
 */
class CompressedMemoryDumpTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    /** ---- Write 8 bit tests and common write functionality*/

    @Test
    fun shouldWriteEmptyData() {
        val memory = Memory()
        assertEquals("", CompressedMemoryDump.write(memory, BitWidth.BW_8))
    }

    @Test
    fun shouldWriteArbitrary8BitData() {
        val memory = Memory(8)
        memory.write(0, 4)
        memory.write(1, 255)
        memory.write(2, 255)
        memory.write(8, 1)
        memory.write(10, 2)

        assertEquals("04 2*FF 5*00 01 00 02", CompressedMemoryDump.write(memory, BitWidth.BW_8))
    }

	@Test
	fun shouldCombineLastValues() {
		val memory = Memory(8)
		memory.write(0, 4)
		memory.write(1, 5)
		memory.write(2, 5)

		assertEquals("04 2*05", CompressedMemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldCombineAllValues() {
		val memory = Memory(8)
		memory.write(0, 5)
		memory.write(1, 5)
		memory.write(2, 5)

		assertEquals("3*05", CompressedMemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldReadValueCrossingSegments() {
		val memory = Memory(8)

		CompressedMemoryDump.read(memory, "16*03")

		assertEquals(3L, memory.read(0))
		assertEquals(3L, memory.read(7))
		assertEquals(3L, memory.read(8))
		assertEquals(3L, memory.read(15))
	}

	/** ---- Write 16 bit tests */

    class Write16BitTests {

        @Test
        //@Throws(IOException::class)
        fun shouldExportArbitraryData() {
            val memory = Memory(8)
            memory.write(0, 4)
            memory.write(1, 256)
            memory.write(2, 256)
            memory.write(8, 1)
            memory.write(10, 65535)

            assertEquals("0004 2*0100 5*0000 0001 0000 FFFF", CompressedMemoryDump.write(memory, BitWidth.BW_16))
        }
    }

    /** ---- Read tests */

    @Test
    //@Throws(IOException::class)
    fun shouldClearBeforeRead() {
        val memory = Memory()
        memory.write(10, 255)

        CompressedMemoryDump.read(memory, "01 02 03")

        assertEquals(0L, memory.read(10))
    }

    @Test
    fun shouldImportArbitraryData() {
        val memory = Memory()

        CompressedMemoryDump.read(memory, "04 2*FF 5*00 01 00 02")

        assertEquals(4L, memory.read(0))
        assertEquals(255L, memory.read(1))
        assertEquals(255L, memory.read(2))
        assertEquals(0L, memory.read(3))
        assertEquals(0L, memory.read(4))
        assertEquals(0L, memory.read(5))
        assertEquals(0L, memory.read(6))
        assertEquals(0L, memory.read(7))
        assertEquals(1L, memory.read(8))
        assertEquals(0L, memory.read(9))
        assertEquals(2L, memory.read(10))
        assertEquals(0L, memory.read(11))
    }

    /** ---- Round-trip tests */

    @Test
    fun shouldWriteAndRead() {
        val memory = Memory(32)
        memory.write(1, 17)
        memory.write(27, 56)
        memory.write(36, 255)

        val buffer = CompressedMemoryDump.write(memory, BitWidth.BW_8)
        memory.clear()
        CompressedMemoryDump.read(memory, buffer)

	    assertEquals("00 11 25*00 38 8*00 FF", buffer)

        assertEquals(0L, memory.read(0))
        assertEquals(17L, memory.read(1))
        assertEquals(56L, memory.read(27))
        assertEquals(255L, memory.read(36))
        assertEquals(0L, memory.read(37))
    }

	/** ---- Comment tests */

	@Test
	fun shouldWriteComment() {
		val memory = Memory(8)
		memory.writeCommentedValue(0, 4, "Comment")

		assertEquals("04:Comment", CompressedMemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldWriteCommentForEqualValues() {
		val memory = Memory(8)
		memory.writeCommentedValue(0, 4, "Comment0")
		memory.writeCommentedValue(1, 5, "Comment1")
		memory.writeCommentedValue(2, 5, "Comment2")

		assertEquals("04:Comment0 05:Comment1 05:Comment2", CompressedMemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldCombineEqualValuesAndComments() {
		val memory = Memory(8)
		memory.writeCommentedValue(0, 4, "Comment0")
		memory.writeCommentedValue(1, 5, "Comment1")
		memory.writeCommentedValue(2, 5, "Comment1")

		assertEquals("04:Comment0 2*05:Comment1", CompressedMemoryDump.write(memory, BitWidth.BW_8))
	}

	@Test
	fun shouldReadComment() {
		val memory = Memory()
		CompressedMemoryDump.read(memory, "04:Comment")

		assertEquals(4L, memory.read(0))
		assertEquals("Comment", memory.readComment(0))
	}

	@Test
	fun shouldReadCombinedComments() {
		val memory = Memory()

		CompressedMemoryDump.read(memory, "04:Comment0 2*05:Comment1")

		assertEquals(4L, memory.read(0))
		assertEquals("Comment0", memory.readComment(0))
		assertEquals(5L, memory.read(1))
		assertEquals("Comment1", memory.readComment(1))
		assertEquals(5L, memory.read(2))
		assertEquals("Comment1", memory.readComment(2))
	}
}