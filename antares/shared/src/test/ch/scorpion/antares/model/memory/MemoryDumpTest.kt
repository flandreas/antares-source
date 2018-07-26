package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit tests for [MemoryDump].
 */
class MemoryDumpTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    /** ---- Write 8 bit tests */

    @Test
    fun shouldWriteEmptyData() {
        val memory = Memory()
        assertThat(MemoryDump.write(memory, BitWidth.BW_8), `is`(""))
    }

    @Test
    fun shouldWriteArbitrary8BitData() {
        val memory = Memory()
        memory.write(0, 4)
        memory.write(1, 255)
        memory.write(2, 255)
        memory.write(8, 1)
        memory.write(10, 2)

        assertThat(MemoryDump.write(memory, BitWidth.BW_8), `is`("04 FF FF 00 00 00 00 00 01 00 02"))
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

        assertThat(MemoryDump.write(memory, BitWidth.BW_16), `is`("0004 FFFF 00FF 0000 0000 0000 0000 0000 0001 0000 008A"))
    }

    /** ---- Read 8 bit data */

    @Test
    fun shouldClearBeforeRead() {
        val memory = Memory()
        memory.write(10, 255)

        MemoryDump.read(memory, "01 02 03")

        assertThat(memory.read(10), `is`(0L))
    }

    @Test
    fun shouldReadEmptyData() {
        val memory = Memory(8)
        MemoryDump.read(memory, "")
        assertThat(memory.read(0), `is`(0L))
    }

    @Test
    fun shouldReadArbitrary8BitData() {
        val memory = Memory()

        MemoryDump.read(memory, "04 00 FF 1A 00 00 08")

        assertThat(memory.read(0), `is`(4L))
        assertThat(memory.read(1), `is`(0L))
        assertThat(memory.read(2), `is`(255L))
        assertThat(memory.read(3), `is`(26L))
        assertThat(memory.read(4), `is`(0L))
        assertThat(memory.read(5), `is`(0L))
        assertThat(memory.read(6), `is`(8L))
        assertThat(memory.read(7), `is`(0L))
    }

    /** ---- Read 16 bit test */

    @Test
    fun shouldReadArbitrary16BitData() {
        val memory = Memory()

        MemoryDump.read(memory, "0004 FFFF 00FF 001A 0000 0000 0008")

        assertThat(memory.read(0), `is`(4L))
        assertThat(memory.read(1), `is`(65535L))
        assertThat(memory.read(2), `is`(255L))
        assertThat(memory.read(3), `is`(26L))
        assertThat(memory.read(4), `is`(0L))
        assertThat(memory.read(5), `is`(0L))
        assertThat(memory.read(6), `is`(8L))
        assertThat(memory.read(7), `is`(0L))
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

		assertThat(MemoryDump.write(memory, BitWidth.BW_8), `is`("04 FF:Comment1 08"))
	}

	@Test
	fun shouldReadComment() {
		val memory = Memory()

		MemoryDump.read(memory, "04 FF:Comment1 08")

		assertThat(memory.read(0), `is`(4L))
		assertThat(memory.read(1), `is`(255L))
		assertThat(memory.readComment(1), `is`("Comment1"))
		assertThat(memory.read(2), `is`(8L))
	}

	@Test
	fun shouldWriteEscapedComment() {
		val memory = Memory()
		memory.write(0, 4)
		memory.writeCommentedValue(1, 255, "Bla:Blu Bli")
		memory.write(2, 8)

		assertThat(MemoryDump.write(memory, BitWidth.BW_8), `is`("04 FF:Bla\\:Blu\\ Bli 08"))
	}

	@Test
	fun shouldReadEscapedComment() {
		val memory = Memory()

		MemoryDump.read(memory, "04 FF:Bla\\:Blu\\ Bli 08")

		assertThat(memory.read(0), `is`(4L))
		assertThat(memory.read(1), `is`(255L))
		assertThat(memory.readComment(1), `is`("Bla:Blu Bli"))
		assertThat(memory.read(2), `is`(8L))
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

		assertThat(memory.read(0), `is`("7000".toLong(16)))
		assertThat(memory.readComment(0), `is`("Initialize I at 0800"))
	}
}