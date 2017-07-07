package ch.scorpion.antares.model.memory

/**
 * Represents a single cell in a [Memory] object. Designed to be immutable.
 * @property address the memory address
 * @property value the value that is stored at [address]
 */
data class MemoryCell(val address: Int, val value: Long) {

    override fun toString(): String {
        return "adr=$address,val=$value"
    }
}