package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * An [Addressable] is a [Vertice] that consists of addressable cells, each containing data content
 * of a particular [BitWidth].
 */
interface Addressable : Vertice {

	val memory: Memory

    /** Returns the current address.*/
    val currentAddress: Int

    /**
     * Returns the maximum (i.e. the largest) address of this [Addressable]. Note that the minimum (i.e. the smallest)
     * address is always 0. */
    val maxAddress: Int

    /** Returns the current data at [currentAddress].*/
    val data: Long

    /** Returns the width of the cell's addresses.*/
    val addressWidth: BitWidth

    /** Returns the width of the cell's data. */
    val dataWidth: BitWidth

    /** Returns the maximum number of characters of all disassembly values.*/
    val disassemblyWidth: Int

	/** Typically corresponds with the value of a "chip select (CS)" input.*/
	val isSelected: Boolean

	/** Clears all content in this [Addressable].*/
	fun clear()

	/** Called if the underlying data of this [Addressable] has changed*/
	fun update()

    /** Returns the data at the specified address.*/
    fun dataAt(address: Int): Long

	fun setDataAt(address: Int, value: Long)

	/** Returns the comment at the specified address.*/
	fun commentAt(address: Int): String?

    /** Disassembles the data at the specified address and returns the result, or returns an empty
     * [String] if this [Addressable] doesn't support disassembling.
     */
    fun disassemblyAt(address: Int): String
}