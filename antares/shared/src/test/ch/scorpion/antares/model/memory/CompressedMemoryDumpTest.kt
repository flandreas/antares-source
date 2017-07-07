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

    /** ---- Write 8 bit tests */

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

    /** ---- Roundtrip tests */

    @Test
    fun shouldWriteAndRead() {
        val memory = Memory(8)
        memory.write(1, 17)
        memory.write(27, 56)
        memory.write(36, 255)

        val buffer = CompressedMemoryDump.write(memory, BitWidth.BW_8)
        memory.clear()
        CompressedMemoryDump.read(memory, buffer)

        assertThat(memory.read(0), `is`(0L))
        assertThat(memory.read(1), `is`(17L))
        assertThat(memory.read(27), `is`(56L))
        assertThat(memory.read(36), `is`(255L))
        assertThat(memory.read(37), `is`(0L))
    }
}