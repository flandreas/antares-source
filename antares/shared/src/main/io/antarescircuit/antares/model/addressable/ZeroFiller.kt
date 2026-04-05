package io.antarescircuit.antares.model.addressable

/**
 * Fills the gaps in the [MemoryCell] address space iterated by the [Iterator] which is returned by
 * [Memory] with zero value [MemoryCell]s. This is used to turn the fragmented and segmented memory space into
 * a continuous stream of [MemoryCell]s, independent of their values.
 *
 * @param iter the original [Iterator] that produces holes in the sequence of [MemoryCell], because it yields
 * only those [MemoryCell]s with a non-zero value.
 */
class ZeroFiller(private val iter: Iterator<MemoryCell>) : Iterator<MemoryCell> {

    private var nextCell: MemoryCell?
    private var lastAddress: Int = -1

    init {
	    nextCell = if (iter.hasNext()) {
		    iter.next()
	    } else {
		    null
	    }
    }

    override fun hasNext(): Boolean {
        return nextCell != null
    }

    override fun next(): MemoryCell {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        if (nextCell!!.address > lastAddress + 1) {
            return MemoryCell(++lastAddress, 0UL)
        }

        val result = nextCell
        lastAddress = result!!.address

        nextCell = if (iter.hasNext()) iter.next() else null

        return result
    }
}