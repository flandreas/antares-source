package io.antarescircuit.antares.model.addressable

/** Sparse memory; reads never allocate storage and empty segments are reclaimed on writes. */
class Memory(private val segmentSize: Int = 256) {

	init {
		require(segmentSize > 0) { "Segment size must be positive" }
	}

	/** Only nonzero or commented cells are stored, grouped by segment index. */
	private val segments = mutableMapOf<Int, MutableMap<Int, CellData>>()

	/** Array length through the last stored cell. Throws if the length cannot be represented as an Int. */
	val nonZeroLength: Int get() {
		val index = segments.keys.maxOrNull() ?: return 0
		val length = index.toLong() * segmentSize + segments.getValue(index).keys.max() + 1
		check(length <= Int.MAX_VALUE) { "Memory length exceeds the Int-sized array limit" }
		return length.toInt()
	}

	fun read(address: Int): ULong = cellAt(address)?.value ?: 0UL

	fun readComment(address: Int): String? = cellAt(address)?.comment

	fun write(address: Int, vararg values: ULong) {
		require(address >= 0 && values.size.toLong() <= Int.MAX_VALUE.toLong() - address + 1) {
			"Memory address out of range"
		}
		values.forEachIndexed { index, value ->
			val cellAddress = address + index
			store(cellAddress, value, readComment(cellAddress))
		}
	}

	fun writeComment(address: Int, comment: String?) {
		store(address, read(address), comment)
	}

	fun writeCommentedValue(address: Int, value: ULong, comment: String?) {
		store(address, value, comment)
	}

	/** Iterates stored cells in address order without walking unallocated address ranges. */
	fun getNonZeroCells(): Iterator<MemoryCell> = sequence {
		for (index in segments.keys.sorted()) {
			val segment = segments.getValue(index)
			for (offset in segment.keys.sorted()) {
				val cell = segment.getValue(offset)
				yield(MemoryCell(index * segmentSize + offset, cell.value, cell.comment))
			}
		}
	}.iterator()

	fun clear() {
		segments.clear()
	}

	/** Creates a sparse snapshot, including comments, for undo and validated imports. */
	fun copy(): Memory = Memory(segmentSize).also { it.replaceWith(this) }

	/** Replaces contents without expanding the implicit zeros between stored cells. */
	fun replaceWith(source: Memory) {
		if (source === this) return
		clear()
		if (source.segmentSize == segmentSize) {
			for ((index, cells) in source.segments) {
				segments[index] = cells.toMutableMap()
			}
		} else {
			val cells = source.getNonZeroCells()
			while (cells.hasNext()) {
				val cell = cells.next()
				store(cell.address, cell.value, cell.comment)
			}
		}
	}

	fun addressWithValueLargerThan(value: ULong): Int? {
		val cells = getNonZeroCells()
		while (cells.hasNext()) {
			val cell = cells.next()
			if (cell.value > value) return cell.address
		}
		return null
	}

	private fun cellAt(address: Int): CellData? {
		require(address >= 0) { "Memory address must be nonnegative" }
		return segments[address / segmentSize]?.get(address % segmentSize)
	}

	private fun store(address: Int, value: ULong, comment: String?) {
		require(address >= 0) { "Memory address must be nonnegative" }
		val index = address / segmentSize
		val offset = address % segmentSize
		if (value == 0UL && comment == null) {
			val segment = segments[index] ?: return
			segment.remove(offset)
			if (segment.isEmpty()) segments.remove(index)
		} else {
			segments.getOrPut(index) { mutableMapOf() }[offset] = CellData(value, comment)
		}
	}

	private data class CellData(val value: ULong, val comment: String?)
}
