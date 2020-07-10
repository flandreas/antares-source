package ch.scorpion.antares.model.addressable

/**
 * Represents a single cell in a [Memory] object. Designed to be immutable.
 * @property address the memory address
 * @property value the value stored at [address]
 * @property comment the optional comment stored at [address]
 */
data class MemoryCell(val address: Int, val value: Long, val comment: String? = null) {

    override fun toString(): String {
        return "adr=$address,val=$value" + if (comment != null) ",comment='$comment'" else ""
    }
}