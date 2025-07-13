package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.base.collection.EmptyIterator

/**
 * Represents a memory object consisting of [MemoryCell]s with an [Int] address and a [Long] value.
 * The memory space is lazy initialized. Segments are allocated on demand
 * @property segmentSize the size of a segment
 */
class Memory(private val segmentSize: Int) {
    constructor(): this(DEFAULT_SEGMENT_SIZE)

    companion object {
        private const val DEFAULT_SEGMENT_SIZE = 256
    }

	/** Maps the base address of a [Segment] to the [Segment]. */
    private val segments: MutableMap<Int, Segment> = mutableMapOf()

	/** Returns the number of cells needed to store all non-zero cells as an array.*/
	val nonZeroLength: Int get() =
		if (segments.isEmpty()) {
			0
		} else {
			val segment = segments[segments.keys.max()]!!
			segment.baseAddress + segment.maxNonZeroAddress + 1
		}

    fun read(address: Int): ULong {
        return getSegment(address).readValue(address % segmentSize)
    }

	fun readComment(address: Int): String? {
		return getSegment(address).readComment(address % segmentSize)
	}

    fun write(address: Int, vararg values: ULong) {
        var lAddress = address
        for (value in values) {
            writeSingleValue(lAddress++, value)
        }
    }

	fun writeComment(address: Int, comment: String?) {
		if (comment == null && !hasSegment(address)) {
			return
		}
		getSegment(address).writeComment(address % segmentSize, comment)
	}

	fun writeCommentedValue(address: Int, value: ULong, comment: String?) {
		if (value == 0UL && comment == null && !hasSegment(address)) {
			return
		}
		getSegment(address).writeCommentedValue(address % segmentSize, value, comment)
	}

    fun getNonZeroCells(): Iterator<MemoryCell> {
        return NonZeroCellIterator()
    }

    fun clear() {
		segments.clear()
	}

	fun addressWithValueLargerThan(value: ULong): Int? {
		val iter = getNonZeroCells()
		while (iter.hasNext()) {
			val cell = iter.next()
			if (cell.value > value) {
				return cell.address
			}
		}
		return null
	}

	private fun writeSingleValue(address: Int, value: ULong) {
		if (value == 0UL && !hasSegment(address)) {
			return
		}
		getSegment(address).writeValue(address % segmentSize, value)
	}

	private fun getSegment(address: Int): Segment {
		val segmentIdx = address / segmentSize
		val baseAddress = segmentIdx * segmentSize
		var segment = segments[segmentIdx]
		if (segment == null) {
			segment = Segment(baseAddress)
			segments[segmentIdx] = segment
		}
		return segment
	}

    private fun hasSegment(address: Int): Boolean {
		return segments[address / segmentSize] != null
	}

	/** Holds the internal data representation of an individual cell.*/
	private data class CellData(val value: ULong, val comment: String?)

    /**
     * Represents the unit of space allocation of a [Memory] object. It has a base
     * address and a fixed size. [Segment] stores only values that are different from 0 (zero).
     * @property baseAddress the base address of this [Segment]
     */
    private class Segment(val baseAddress: Int) {

        /** Maps the address relative to the base address to the stored [CellData] at that address. */
        private val data: MutableMap<Int,CellData> = mutableMapOf()

	    /** Returns the maximum relative address containing a non-zero value. */
	    val maxNonZeroAddress: Int get() = if (data.isEmpty()) {
		    0
	    } else {
		    data.keys.max()
	    }

 		/** Reads the value at a relative segment address. */
		fun readValue(address: Int): ULong {
            return data[address]?.value ?: 0UL
		}

	    /** Reads the comment at a relative segment address. */
	    fun readComment(address: Int): String? {
		    return data[address]?.comment
	    }

		/** Writes a value at a relative segment address. */
		fun writeValue(address: Int, value: ULong) {
			val cell = data[address]
			if (value == 0UL) {
				if (cell != null && cell.comment == null) {
					data.remove(address)
				} else {
					data[address] = CellData(value, cell?.comment)
				}
			} else {
				data[address] = CellData(value, cell?.comment)
			}
		}

	    /** Writes a comment at a relative segment address. */
	    fun writeComment(address: Int, comment: String?) {
		    val cell = data[address]
		    if (comment == null) {
			    if (cell != null && cell.value == 0UL) {
				    data.remove(address)
			    } else if (cell != null) {
				    data[address] = CellData(cell.value, comment)
			    }
		    } else {
			    data[address] = CellData(cell?.value ?: 0UL, comment)
		    }
	    }

	    fun writeCommentedValue(address: Int, value: ULong, comment: String?) {
		    val cell = data[address]
		    if (value == 0UL && comment == null) {
			    if (cell != null) {
				    data.remove(address)
			    } else {
				    data[address] = CellData(value, comment)
			    }
		    } else {
			    data[address] = CellData(value, comment)
		    }
	    }

        fun getNonZeroCellIterator(): Iterator<MemoryCell> {
            return NonZeroCellIterator()
        }

        /**
         * Returns only those [MemoryCell]s of this [Segment] that have a value different from 0
         * or a non-empty comment (i.e. cells that exist).
         */
        private inner class NonZeroCellIterator : Iterator<MemoryCell> {

            private val sortedAddresses: MutableList<Int> = mutableListOf()
            private var addressIndex: Int? = null

            init {
                sortedAddresses.addAll(data.keys)
                sortedAddresses.sort()
	            addressIndex = if (sortedAddresses.isEmpty()) {
		            null
	            } else {
		            0
	            }
            }

            override fun hasNext(): Boolean {
                return addressIndex != null
            }

            override fun next(): MemoryCell {
                if (addressIndex == null) {
                    throw NoSuchElementException()
                }
                val address = sortedAddresses[addressIndex!!]
                val currentValue = currentValue()
	            val currentComment = currentComment()
	            addressIndex = if (addressIndex!! < sortedAddresses.size - 1) {
		            addressIndex!! + 1
	            } else {
		            null
	            }
                return MemoryCell(baseAddress + address, currentValue, currentComment)
            }

            private fun currentValue(): ULong {
                return readValue(sortedAddresses[addressIndex!!])
            }

	        private fun currentComment(): String? {
		        return readComment(sortedAddresses[addressIndex!!])
	        }
        }
    }

    private inner class NonZeroCellIterator : Iterator<MemoryCell> {

        private val segmentAddresses: MutableList<Int> = mutableListOf()
        private var segmentIndex: Int? = null
        private var segmentCellIterator: Iterator<MemoryCell>

        init {
            segmentAddresses.addAll(segments.keys)
            segmentAddresses.sort()
            if (segmentAddresses.isEmpty()) {
                segmentIndex = null
                segmentCellIterator = EmptyIterator()
            } else {
                segmentIndex = 0
                segmentCellIterator = segments[segmentAddresses[segmentIndex!!]]!!.getNonZeroCellIterator()
            }

        }

        override fun hasNext(): Boolean {
            return segmentIndex != null && segmentCellIterator.hasNext()
        }

        override fun next(): MemoryCell {
            if (segmentIndex == null) {
				throw NoSuchElementException()
			}

			val value = segmentCellIterator.next()

			if (!segmentCellIterator.hasNext()) {
				if (segmentIndex!! < segmentAddresses.size - 1) {
					segmentIndex = segmentIndex!! + 1
                    segmentCellIterator = segments[segmentAddresses[segmentIndex!!]]!!.getNonZeroCellIterator()
				} else {
					segmentIndex = null
				}
			}
			return value
        }
    }
}