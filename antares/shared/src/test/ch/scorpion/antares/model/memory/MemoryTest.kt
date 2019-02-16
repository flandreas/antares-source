package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.*

/**
 * Unit tests for [Memory].
 */
class MemoryTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	/** ---- Management tests */

	@Test
	fun shouldClear() {
		val memory = Memory()
		memory.write(3, 4)
		memory.clear()
		assertEquals(0L, memory.read(3))
	}

	/** ---- Read/Write tests */

	@Test
	fun shouldWriteAndRead() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.write(4, 255)
		memory.write(8, 1)
		assertEquals(0L, memory.read(0))
		assertEquals(4L, memory.read(3))
		assertEquals(255L, memory.read(4))
		assertEquals(1L, memory.read(8))
		assertEquals(0L, memory.read(9))
	}

	@Test
	fun shouldReadZeroFromUnwrittenAddress() {
		val memory = Memory()
		assertEquals(0L, memory.read(1234))
	}

	@Test
	fun shouldWriteSuccessiveValues() {
		val memory = Memory()
		memory.write(0, 1, 17, 35)
		assertEquals(1L, memory.read(0))
		assertEquals(17L, memory.read(1))
		assertEquals(35L, memory.read(2))
	}

	@Test
	fun shouldWriteAndReadCrossingSegments() {
		val memory = Memory(8)

		for (i in 0 until 16) {
			memory.write(i, i.toLong())
		}
		for (i in 0 until 16) {
			assertEquals(i.toLong(), memory.read(i))
		}
	}

	@Test
	fun shouldWriteAndReadCommentedCrossingSegments() {
		val memory = Memory(8)

		for (i in 0 until 16) {
			memory.writeCommentedValue(i, i.toLong(), "Comment $i")
		}
		for (i in 0 until 16) {
			assertEquals(i.toLong(), memory.read(i))
			assertEquals("Comment $i", memory.readComment(i))
		}
	}


	/** ---- NonZeroIterator tests */

	@Test
	fun shouldNotIterateEmptyMemory() {
		val memory = Memory(8)
		val iter = memory.getNonZeroCells()
		assertFalse(iter.hasNext())
	}

	@Test
	fun shouldIteratorNonLeadingZero() {
		val memory = Memory(8)
		memory.write(0, 5)
		memory.write(1, 11)

		val iter = memory.getNonZeroCells()

		assertEquals(MemoryCell(0, 5L), iter.next())
		assertEquals(MemoryCell(1, 11L), iter.next())
		assertFalse(iter.hasNext())
	}

	@Test
	fun shouldIterateArbitraryCells() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.write(4, 255)
		memory.write(8, 1)

		val iter = memory.getNonZeroCells()

		assertTrue(iter.hasNext())
		assertEquals(MemoryCell(3, 4L), iter.next())
		assertEquals(MemoryCell(4, 255L), iter.next())
		assertEquals(MemoryCell(8, 1L), iter.next())
		assertFalse(iter.hasNext())
	}

	@Test
	fun shouldIterateFullMemory() {
		val memory = Memory(8)
		for (i in 0..216) {
			memory.write(i, 2 * i.toLong())
		}

		val iter = memory.getNonZeroCells()
		assertTrue(iter.hasNext())
		for (i in 1..216) {
			assertEquals(MemoryCell(i, 2 * i.toLong()), iter.next())
		}
		assertFalse(iter.hasNext())
	}

	/** ---- Comment tests */

	@Test
	fun shouldWriteCommentInValuelessCell() {
		val memory = Memory(8)
		memory.writeComment(3, "This is address 3")
		assertEquals("This is address 3", memory.readComment(3))
	}

	@Test
	fun shouldWriteCommentInValueCell() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		assertEquals(4L, memory.read(3))
		assertEquals("The value of 3 is 4", memory.readComment(3))
	}

	@Test
	fun shouldWriteWithComment() {
		val memory = Memory(8)
		memory.writeCommentedValue(3, 4, "The value of 3 is 4")
		assertEquals(4L, memory.read(3))
		assertEquals("The value of 3 is 4", memory.readComment(3))
	}

	@Test
	fun shouldOnlyChangeValue() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		memory.write(3, 5)
		assertEquals("The value of 3 is 4", memory.readComment(3))
	}

	@Test
	fun shouldOnlyChangeComment() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		assertEquals(4L, memory.read(3))
	}

	@Test
	fun shouldDeleteComment() {
		val memory = Memory(8)
		memory.write(3, 4)
		memory.writeComment(3, "The value of 3 is 4")
		memory.writeComment(3, null)
		assertNull(memory.readComment(3))
	}
}