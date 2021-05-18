package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * An [Addressable] is a [Vertice] that consists of addressable cells, each containing data content
 * of a particular [BitWidth].
 */
interface Addressable : Vertice {

	companion object {
		const val ADDRESS_PORT_NAME = "A"
		const val CHIP_SELECT_PORT_NAME = "CS"
		const val DATA_PORT_NAME = "D"
	}

	val memory: Memory

    /** Returns the current address.*/
    val currentAddress: Int

    /**
     * Returns the maximum (i.e. the largest) address of this [Addressable]. Note that the minimum (i.e. the smallest)
     * address is always 0. */
    val maxAddress: Int

    /** Returns the current data at [currentAddress].*/
    val data: Long

    /** Contains the width of the cell's addresses.*/
    var addressWidth: BitWidth

    /** Contains the width of the cell's data. */
    var dataWidth: BitWidth

    /** Returns the maximum number of characters of all disassembly values.*/
    val disassemblyWidth: Int

	/** Typically corresponds with the value of a "chip select (CS)" input.*/
	val isSelected: Boolean

	/** Determines whether this [Addressable] stores the cell values in [Vertice.write].*/
	val storesCells: Boolean

	/** Clears all content in this [Addressable].*/
	fun clear()

	/** Called if the underlying data of this [Addressable] has changed*/
	fun update()

    /** Returns the data at the specified address.*/
    fun dataAt(address: Int): Long

	fun setDataAt(address: Int, value: Long, signalHandler: SignalHandler?)

	/** Returns the comment at the specified address.*/
	fun commentAt(address: Int): String?

    /** Disassembles the data at the specified address and returns the result, or returns an empty
     * [String] if this [Addressable] doesn't support disassembling.
     */
    fun disassemblyAt(address: Int): String

	fun getAddressInput(): DigitalPort =
		getPort<DigitalSignal>(ADDRESS_PORT_NAME) as DigitalPort

	fun getChipSelectInput(): DigitalPort =
		getPort<DigitalSignal>(CHIP_SELECT_PORT_NAME) as DigitalPort

	fun getDataPort(): DigitalPort =
		getPort<DigitalPort>(DATA_PORT_NAME) as DigitalPort
}