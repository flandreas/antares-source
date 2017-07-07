package ch.scorpion.antares.model.memory

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
		val format = "%${Math.max(2, bitWidth.width / 4)}s"

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
				write(lastCell.value, cell.address - lastAddress - 1, mask, format, builder)
				lastWrittenCell = lastCell
			}

			lastAddress = if (lastWrittenCell != null) lastWrittenCell.address else -1
			if (cellIter.hasNext()) {
				lastCell = cell
			} else {
				if (lastWrittenCell != null) {
					builder.append(' ')
				}

				write(cell.value, cell.address - lastAddress - 1, mask, format, builder)
			}
		}

        return builder.toString()
    }

    private fun write(value: Long, count: Int, mask: Long, format: String, builder: StringBuilder) {
        if (count > 1) {
            builder.append(Integer.toString(count))
            builder.append(CARDINALITY_DELIM)
        }
        writePaddedHex(value, mask, format, builder)
    }

    private fun writePaddedHex(value: Long, mask: Long, format: String, builder: StringBuilder) {
        builder.append(String.format(
                format, java.lang.Long.toHexString(value and mask).toUpperCase()).replace(' ', '0'))
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