package ch.scorpion.antares.model.memory

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal

interface Addressable {

    /** Returns the current address.*/
    val currentAddress: Int

    val maxAddress: Int

    /** Returns the current data at [currentAddress].*/
    val data: Long

    val addressWidth: BitWidth

    val dataWidth: BitWidth

    fun dataAt(address: Int): Long

    fun disassemblyAt(address: Int): String
}