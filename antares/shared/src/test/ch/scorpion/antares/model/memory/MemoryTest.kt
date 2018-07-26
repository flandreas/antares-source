package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [Memory].
 */
class MemoryTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    /** ---- Management tests */

    @Test
    fun shouldClear() {
        val memory = Memory()
        memory.write(3, 4)
        memory.clear()
        assertThat(memory.read(3), `is`(0L))
    }

    /** ---- Read/Write tests */

    @Test
    fun shouldWriteAndRead() {
        val memory = Memory(8)
        memory.write(3, 4)
        memory.write(4, 255)
        memory.write(8, 1)
        assertThat(memory.read(0), `is`(0L))
        assertThat(memory.read(3), `is`(4L))
        assertThat(memory.read(4), `is`(255L))
        assertThat(memory.read(8), `is`(1L))
        assertThat(memory.read(9), `is`(0L))
    }

    @Test
    fun shouldReadZeroFromUnwrittenAddress() {
        val memory = Memory()
        assertThat(memory.read(1234), `is`(0L))
    }

    @Test
    fun shouldWriteSuccessiveValues() {
        val memory = Memory()
        memory.write(0, 1, 17, 35)
        assertThat(memory.read(0), `is`(1L))
        assertThat(memory.read(1), `is`(17L))
        assertThat(memory.read(2), `is`(35L))
    }

	@Test
	fun shouldWriteAndReadCrossingSegments() {
		val memory = Memory(8)

		for (i in 0 until 16) {
			memory.write(i, i.toLong())
		}
		for (i in 0 until 16) {
			assertThat(memory.read(i), `is`(i.toLong()))
		}
	}

	@Test
	fun shouldWriteAndReadCommentedCrossingSegments() {
		val memory = Memory(8)

		for (i in 0 until 16) {
			memory.writeCommentedValue(i, i.toLong(), "Comment $i")
		}
		for (i in 0 until 16) {
			assertThat(memory.read(i), `is`(i.toLong()))
			assertThat(memory.readComment(i), `is`("Comment $i"))
		}
	}


	/** ---- NonZeroIterator tests */

    @Test
    fun shouldNotIterateEmptyMemory() {
        val memory = Memory(8)
        val iter = memory.getNonZeroCells()
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldIteratorNonLeadingZero() {
        val memory = Memory(8)
        memory.write(0, 5)
        memory.write(1, 11)

        val iter = memory.getNonZeroCells()

        assertThat(iter.next(), `is`(MemoryCell(0, 5L)))
        assertThat(iter.next(), `is`(MemoryCell(1, 11L)))
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldIterateArbitraryCells() {
        val memory = Memory(8)
        memory.write(3, 4)
        memory.write(4, 255)
        memory.write(8, 1)

        val iter = memory.getNonZeroCells()

        assertThat(iter.hasNext(), `is`(true))
        assertThat(iter.next(), `is`(MemoryCell(3, 4L)))
        assertThat(iter.next(), `is`(MemoryCell(4, 255L)))
        assertThat(iter.next(), `is`(MemoryCell(8, 1L)))
        assertThat(iter.hasNext(), `is`(false))
    }

    @Test
    fun shouldIterateFullMemory() {
        val memory = Memory(8)
        for (i in 0..216) {
            memory.write(i, 2 * i.toLong())
        }

        val iter = memory.getNonZeroCells()
        assertThat(iter.hasNext(), `is`(true))
        for (i in 1..216) {
            assertThat(iter.next(), `is`(MemoryCell(i, 2 * i.toLong())))
        }
        assertThat(iter.hasNext(), `is`(false))
    }

	/** ---- Comment tests */

	@Test
	fun shouldWriteCommentInValuelessCell() {
		val memory = Memory(8)
		memory.writeComment(3, "This is address 3")
		assertThat(memory.readComment(3), `is`("This is address 3"))
	}

	@Test
	fun shouldWriteCommentInValueCell() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		assertThat(memory.read(3), `is`(4L))
		assertThat(memory.readComment(3), `is`("The value of 3 is 4"))
	}

	@Test
	fun shouldWriteWithComment() {
		val memory = Memory(8)
		memory.writeCommentedValue(3, 4, "The value of 3 is 4")
		assertThat(memory.read(3), `is`(4L))
		assertThat(memory.readComment(3), `is`("The value of 3 is 4"))
	}

	@Test
	fun shouldOnlyChangeValue() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		memory.write(3, 5)
		assertThat(memory.readComment(3), `is`("The value of 3 is 4"))
	}

	@Test
	fun shouldOnlyChangeComment() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		assertThat(memory.read(3), `is`(4L))
	}

	@Test
	fun shouldDeleteComment() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		memory.writeComment(3, null)
		assertThat(memory.readComment(3), nullValue())
	}
}