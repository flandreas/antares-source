package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.ClassRule
import org.junit.Test
import java.io.IOException


/**
 * Unit tests for [CompressedMemoryDump].
 */
class CompressedMemoryDumpTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    /** ---- Write 8 bit tests and common write functionality*/

    @Test
    fun shouldWriteEmptyData() {
        val memory = Memory()
        assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_8), `is`(""))
    }

    @Test
    fun shouldWriteArbitrary8BitData() {
        val memory = Memory(8)
        memory.write(0, 4)
        memory.write(1, 255)
        memory.write(2, 255)
        memory.write(8, 1)
        memory.write(10, 2)

        assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_8), `is`("04 2*FF 5*00 01 00 02"))
    }

	@Test
	fun shouldCombineLastValues() {
		val memory = Memory(8)
		memory.write(0, 4)
		memory.write(1, 5)
		memory.write(2, 5)

		assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_8), `is`("04 2*05"))
	}

	@Test
	fun shouldCombineAllValues() {
		val memory = Memory(8)
		memory.write(0, 5)
		memory.write(1, 5)
		memory.write(2, 5)

		assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_8), `is`("3*05"))
	}

	@Test
	fun shouldReadValueCrossingSegments() {
		val memory = Memory(8)

		CompressedMemoryDump.read(memory, "16*03")

		assertThat(memory.read(0), `is`(3L))
		assertThat(memory.read(7), `is`(3L))
		assertThat(memory.read(8), `is`(3L))
		assertThat(memory.read(15), `is`(3L))
	}

	/** ---- Write 16 bit tests */

    class Write16BitTests {

        @Test
        @Throws(IOException::class)
        fun shouldExportArbitraryData() {
            val memory = Memory(8)
            memory.write(0, 4)
            memory.write(1, 256)
            memory.write(2, 256)
            memory.write(8, 1)
            memory.write(10, 65535)

            assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_16), `is`("0004 2*0100 5*0000 0001 0000 FFFF"))
        }
    }

    /** ---- Read tests */

    @Test
    @Throws(IOException::class)
    fun shouldClearBeforeRead() {
        val memory = Memory()
        memory.write(10, 255)

        CompressedMemoryDump.read(memory, "01 02 03")

        assertThat(memory.read(10), `is`(0L))
    }

    @Test
    fun shouldImportArbitraryData() {
        val memory = Memory()

        CompressedMemoryDump.read(memory, "04 2*FF 5*00 01 00 02")

        assertThat(memory.read(0), `is`(4L))
        assertThat(memory.read(1), `is`(255L))
        assertThat(memory.read(2), `is`(255L))
        assertThat(memory.read(3), `is`(0L))
        assertThat(memory.read(4), `is`(0L))
        assertThat(memory.read(5), `is`(0L))
        assertThat(memory.read(6), `is`(0L))
        assertThat(memory.read(7), `is`(0L))
        assertThat(memory.read(8), `is`(1L))
        assertThat(memory.read(9), `is`(0L))
        assertThat(memory.read(10), `is`(2L))
        assertThat(memory.read(11), `is`(0L))
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

	    assertThat(buffer, `is`("00 11 25*00 38 8*00 FF"))

        assertThat(memory.read(0), `is`(0L))
        assertThat(memory.read(1), `is`(17L))
        assertThat(memory.read(27), `is`(56L))
        assertThat(memory.read(36), `is`(255L))
        assertThat(memory.read(37), `is`(0L))
    }

	/** ---- Comment tests */

	@Test
	fun shouldWriteComment() {
		val memory = Memory(8)
		memory.writeCommentedValue(0, 4, "Comment")

		assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_8), `is`("04:Comment"))
	}

	@Test
	fun shouldWriteCommentForEqualValues() {
		val memory = Memory(8)
		memory.writeCommentedValue(0, 4, "Comment0")
		memory.writeCommentedValue(1, 5, "Comment1")
		memory.writeCommentedValue(2, 5, "Comment2")

		assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_8), `is`("04:Comment0 05:Comment1 05:Comment2"))
	}

	@Test
	fun shouldCombineEqualValuesAndComments() {
		val memory = Memory(8)
		memory.writeCommentedValue(0, 4, "Comment0")
		memory.writeCommentedValue(1, 5, "Comment1")
		memory.writeCommentedValue(2, 5, "Comment1")

		assertThat(CompressedMemoryDump.write(memory, BitWidth.BW_8), `is`("04:Comment0 2*05:Comment1"))
	}

	@Test
	fun shouldReadComment() {
		val memory = Memory()
		CompressedMemoryDump.read(memory, "04:Comment")

		assertThat(memory.read(0), `is`(4L))
		assertThat(memory.readComment(0), `is`("Comment"))
	}

	@Test
	fun shouldReadCombinedComments() {
		val memory = Memory()

		CompressedMemoryDump.read(memory, "04:Comment0 2*05:Comment1")

		assertThat(memory.read(0), `is`(4L))
		assertThat(memory.readComment(0), `is`("Comment0"))
		assertThat(memory.read(1), `is`(5L))
		assertThat(memory.readComment(1), `is`("Comment1"))
		assertThat(memory.read(2), `is`(5L))
		assertThat(memory.readComment(2), `is`("Comment1"))
	}
}