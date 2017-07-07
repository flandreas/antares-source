package ch.scorpion.antares.model.memory

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * Represents a memory object consisting of [MemoryCell]s with an [Int] address and a [Long] value.
 * The memory space is lazy initialized. Segments are allocated on demand
 * @property segmentSize the size of a segment
 */
class Memory(val segmentSize: Int) {
    constructor(): this(DEFAULT_SEGMENT_SIZE)

    companion object {
        private val DEFAULT_SEGMENT_SIZE = 256
    }

    private val segments: MutableMap<Int, Segment> = mutableMapOf()

    fun read(address: Int): Long {
        return getSegment(address).read(address % segmentSize)
    }

    fun write(address: Int, vararg values: Long) {
        var lAddress = address
        for (value in values) {
            writeSingleValue(lAddress++, value)
        }
    }

    fun writeSingleValue(address: Int, value: Long) {
        if (value == 0L && !hasSegment(address)) {
            return
        }
        getSegment(address).write(address % segmentSize, value)
    }

    fun getNonZeroCells(): Iterator<MemoryCell> {
        return NonZeroCellIterator()
    }

    fun clear() {
		segments.clear()
	}

    private fun getSegment(address: Int): Segment {
		val baseAddress = address / segmentSize * segmentSize
		var segment = segments[baseAddress]
		if (segment == null) {
			segment = Segment(baseAddress)
			segments.put(baseAddress, segment)
		}
		return segment
	}

    private fun hasSegment(address: Int): Boolean {
		return segments[address / segmentSize] != null
	}

    /**
     * Represents the unit of space allocation of a [Memory] object. It has a base
     * address and a fixed size. [Segment] stores only values that are different from 0 (zero).
     * @property baseAddress the base address of this [Segment]
     */
    private class Segment(val baseAddress: Int) {

        /** Maps the address relative to the base address to the stored value at that address. */
        private val data: MutableMap<Int,Long> = mutableMapOf()

 		/** Reads the value at a relative segment address. */
		fun read(address: Int): Long {
            return data[address] ?: 0
		}

		/** Writes a value at at relative sgement address. */
		fun write(address: Int, value: Long) {
			if (value == 0L) {
				if (data[address] != null) {
					data.remove(address)
				}
			} else {
				data[address] = value
			}
		}

        fun getNonZeroCellIterator(): Iterator<MemoryCell> {
            return NonZeroCellIterator()
        }

        /** Returns only those [MemoryCell]s of this [Segment] that have a value different from 0 (i.e. that exist).*/
        private inner class NonZeroCellIterator : Iterator<MemoryCell> {

            private val sortedAddresses: MutableList<Int> = mutableListOf()
            private var addressIndex: Int? = null

            init {
                sortedAddresses.addAll(data.keys)
                sortedAddresses.sort()
                if (sortedAddresses.isEmpty()) {
                    addressIndex = null
                } else {
                    addressIndex = 0
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
                if (addressIndex!! < sortedAddresses.size - 1) {
                    addressIndex = addressIndex!! + 1
                } else {
                    addressIndex = null
                }
                return MemoryCell(baseAddress + address, currentValue)
            }

            private fun currentValue(): Long {
                return read(sortedAddresses[addressIndex!!])
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