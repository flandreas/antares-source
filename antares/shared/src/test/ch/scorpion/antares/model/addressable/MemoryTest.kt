package ch.scorpion.antares.model.addressable

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
		memory.write(3, 4UL)
		memory.clear()
		assertEquals(0UL, memory.read(3))
	}

	/** ---- Read/Write tests */

	@Test
	fun shouldWriteAndRead() {
		val memory = Memory(8)
		memory.write(3, 4UL)
		memory.write(4, 255UL)
		memory.write(8, 1UL)
		assertEquals(0UL, memory.read(0))
		assertEquals(4UL, memory.read(3))
		assertEquals(255UL, memory.read(4))
		assertEquals(1UL, memory.read(8))
		assertEquals(0UL, memory.read(9))
	}

	@Test
	fun shouldReadZeroFromUnwrittenAddress() {
		val memory = Memory()
		assertEquals(0UL, memory.read(1234))
	}

	@Test
	fun shouldWriteSuccessiveValues() {
		val memory = Memory()
		memory.write(0, 1UL, 17UL, 35UL)
		assertEquals(1UL, memory.read(0))
		assertEquals(17UL, memory.read(1))
		assertEquals(35UL, memory.read(2))
	}

	@Test
	fun shouldWriteAndReadCrossingSegments() {
		val memory = Memory(8)

		for (i in 0 until 16) {
			memory.write(i, i.toULong())
		}
		for (i in 0 until 16) {
			assertEquals(i.toULong(), memory.read(i))
		}
	}

	@Test
	fun shouldWriteAndReadCommentedCrossingSegments() {
		val memory = Memory(8)

		for (i in 0 until 16) {
			memory.writeCommentedValue(i, i.toULong(), "Comment $i")
		}
		for (i in 0 until 16) {
			assertEquals(i.toULong(), memory.read(i))
			assertEquals("Comment $i", memory.readComment(i))
		}
	}

	@Test
	fun shouldYieldNonZeroLength() {
		val memory = Memory()
		assertEquals(0, memory.nonZeroLength)
		memory.write(27, 99UL)
		assertEquals(28, memory.nonZeroLength)
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
		memory.write(0, 5UL)
		memory.write(1, 11UL)

		val iter = memory.getNonZeroCells()

		assertEquals(MemoryCell(0, 5UL), iter.next())
		assertEquals(MemoryCell(1, 11UL), iter.next())
		assertFalse(iter.hasNext())
	}

	@Test
	fun shouldIterateArbitraryCells() {
		val memory = Memory(8)
		memory.write(3, 4UL)
		memory.write(4, 255UL)
		memory.write(8, 1UL)

		val iter = memory.getNonZeroCells()

		assertTrue(iter.hasNext())
		assertEquals(MemoryCell(3, 4UL), iter.next())
		assertEquals(MemoryCell(4, 255UL), iter.next())
		assertEquals(MemoryCell(8, 1UL), iter.next())
		assertFalse(iter.hasNext())
	}

	@Test
	fun shouldIterateFullMemory() {
		val memory = Memory(8)
		for (i in 0..216) {
			memory.write(i, 2UL * i.toULong())
		}

		val iter = memory.getNonZeroCells()
		assertTrue(iter.hasNext())
		for (i in 1..216) {
			assertEquals(MemoryCell(i, 2UL * i.toULong()), iter.next())
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
		memory.write(3, 4UL)
		memory.writeComment(3, "The value of 3 is 4")
		assertEquals(4UL, memory.read(3))
		assertEquals("The value of 3 is 4", memory.readComment(3))
	}

	@Test
	fun shouldWriteWithComment() {
		val memory = Memory(8)
		memory.writeCommentedValue(3, 4UL, "The value of 3 is 4")
		assertEquals(4UL, memory.read(3))
		assertEquals("The value of 3 is 4", memory.readComment(3))
	}

	@Test
	fun shouldOnlyChangeValue() {
		val memory = Memory(8)
		memory.write(3, 4UL)
		memory.writeComment(3, "The value of 3 is 4")
		memory.write(3, 5UL)
		assertEquals("The value of 3 is 4", memory.readComment(3))
	}

	@Test
	fun shouldOnlyChangeComment() {
		val memory = Memory(8)
		memory.write(3, 4UL)
		memory.writeComment(3, "The value of 3 is 4")
		assertEquals(4UL, memory.read(3))
	}

	@Test
	fun shouldDeleteComment() {
		val memory = Memory(8)
		memory.write(3, 4UL)
		memory.writeComment(3, "The value of 3 is 4")
		memory.writeComment(3, null)
		assertNull(memory.readComment(3))
	}

	/** ---- Regression tests */

	@Test
	fun shouldNotDiscardZeros() {
		// test for flandreas/antares#715
		val memory = Memory(12)
		memory.write(2055, 2055UL)
		memory.write(2055, 0UL)
		assertEquals(0UL, memory.read(2055))
	}
}