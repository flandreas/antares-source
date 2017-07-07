package ch.scorpion.antares.model.memory

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.Math

/**
 * Represents a dump of a [Memory] in a single format consisting of a sequence of hexadecimal number, one for each
 * [Memory] address.
 */
object MemoryDump {

    /**
     * Writes the contents of the specified [Memory] into a new [String].
     * TODO This will probably only work on the JVM platform?
     */
    fun write(memory: Memory, bitWidth: BitWidth): String {
        val builder = StringBuilder()
        val mask = BitOperation.power(bitWidth.width.toLong()) - 1L
        val format = "%${Math.max(2, bitWidth.width / 4)}s"

        val cellIter = ZeroFiller(memory.getNonZeroCells())
        while (cellIter.hasNext()) {
            val cell = cellIter.next()
            writePaddedHex(cell.value, mask, format, builder)
            if (cellIter.hasNext()) {
                builder.append(' ')
            }
        }
        return builder.toString()
    }

    /**
     * Reads the dump from the specified [String] into a [Memory].
     */
    fun read(memory: Memory, dump: String) {
        memory.clear()
        var address: Int = 0
        for (value in dump.split(' ', '\n')) {
            memory.write(address++, BitOperation.hexToLong(value))
        }
    }

    private fun writePaddedHex(value: Long, mask: Long, format: String, target: StringBuilder) {
        target.append(String.format(format, BitOperation.longToHex(value and mask).toUpperCase()).replace(' ', '0'))
    }
}