package ch.scorpion.antares.model.memory

import ch.scorpion.jabbah.base.Math
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth


/**
 * Represents a dump of a [Memory] in an Antares proprietary, compressed textual format.
 */
object CompressedMemoryDump {

    val CARDINALITY_DELIM = '*'

    /**
     * Writes a dump of a [Memory] to a new [String].
     */
    fun write(memory: Memory, bitWidth: BitWidth): String {
        val builder = StringBuilder()
        val mask: Long = BitOperation.power(bitWidth.width.toLong()) - 1L
        val length = Math.max(2, bitWidth.width / 4)

		val cellIter = ZeroFiller(memory.getNonZeroCells())
		if (!cellIter.hasNext()) {
			return ""
		}

		var lastWrittenCell: MemoryCell? = null
		var lastCell: MemoryCell? = null
		while (cellIter.hasNext()) {
			val cell = cellIter.next()
			var lastAddress = if (lastWrittenCell != null) lastWrittenCell.address else -1
			if (lastCell != null && cell.value != lastCell.value) {
				if (lastWrittenCell != null) {
                    builder.append(' ')
				}
				write(lastCell.value, cell.address - lastAddress - 1, mask, length, builder)
				lastWrittenCell = lastCell
			}

			lastAddress = if (lastWrittenCell != null) lastWrittenCell.address else -1
			if (cellIter.hasNext()) {
				lastCell = cell
			} else {
				if (lastWrittenCell != null) {
					builder.append(' ')
				}

				write(cell.value, cell.address - lastAddress - 1, mask, length, builder)
			}
		}

        return builder.toString()
    }

    private fun write(value: Long, count: Int, mask: Long, length: Int, builder: StringBuilder) {
        if (count > 1) {
            builder.append(count.toString())
            builder.append(CARDINALITY_DELIM)
        }
        writePaddedHex(value, mask, length, builder)
    }

    private fun writePaddedHex(value: Long, mask:Long, length: Int, builder: StringBuilder) {
        builder.append(BitOperation.longToHex(value and mask).toUpperCase().padStart(length, '0'))
    }

    /**
     * Reads a dump into a [Memory] from a [Readable].
     */
    fun read(memory: Memory, dump: String) {
        memory.clear()
        var address = 0
        for (value in dump.split(' ', '\n')) {
            address = read(value, address, memory)
        }
    }

    private fun read(token: String, address: Int, memory: Memory): Int {
        var lAddress = address
        val delimIndex = token.indexOf(CARDINALITY_DELIM)
        if (delimIndex == -1) {
            memory.write(lAddress++, BitOperation.hexToLong(token))
        } else {
            val count = token.substring(0, delimIndex).toInt()
            for (i in 0..count - 1) {
                memory.write(lAddress++, BitOperation.hexToLong(token.substring(delimIndex + 1)))
            }
        }
        return lAddress
    }
}