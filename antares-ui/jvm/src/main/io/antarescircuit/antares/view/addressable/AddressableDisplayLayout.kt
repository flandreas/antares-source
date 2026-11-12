package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.AddressableReference
import io.antarescircuit.antares.model.addressable.Memory
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import javax.swing.JLabel
import javax.swing.table.TableModel

/**
 * Specified how the individual cells of an [Addressable] are to be represented as a [TableModel],
 * by for example specifying how many columns are to be displayed.
 **/
interface AddressableDisplayLayout {

	val cellsPerRow: Int

	var startAddress: Int

	/** Creates a [TableModel] that displays the specified [Memory] contents according to this specific layout. */
	fun createTableModel(): AbstractAddressableTableModel

	/** Returns the text alignment of the specified column in terms of [JLabel.RIGHT] or [JLabel.LEFT].*/
	fun columnAlignment(columnIndex: Int): Int

	fun getCellAddress(rowIndex: Int, columnIndex: Int): Int
}

class FixedWidthLayout(
	override val cellsPerRow: Int,
	private val addressableRef: AddressableReference,
	private val editable: () -> Boolean,
	private val converterProvider: () -> AddressableValueConverter,
	private val signalHandler: SignalHandler? = null
) : AddressableDisplayLayout  {

	override var startAddress: Int = 0

	private val maxAddress: Int get() = addressableRef.addressable.addressWidth.maxValue.coerceAtMost(Int.MAX_VALUE.toULong()).toInt()
	private val page: MemoryPage get() = MemoryPage(startAddress, maxAddress, cellsPerRow)

	private val rowCount: Int get() = page.rowCount

	override fun toString(): String =
		Translations.getString("antares.memory.layout.columns", cellsPerRow)

	override fun getCellAddress(rowIndex: Int, columnIndex: Int): Int =
		page.addressAt(rowIndex, if (cellsPerRow == 1) 0 else columnIndex) ?: -1

	override fun createTableModel(): AbstractAddressableTableModel =
		when (cellsPerRow) {
			1 -> SingleColumnTableModel(addressableRef, rowCount, editable, converterProvider, signalHandler, startAddress)
			else -> AddressableTableModel(cellsPerRow, addressableRef, rowCount, editable, converterProvider, signalHandler, startAddress)
		}

	override fun columnAlignment(columnIndex: Int): Int =
		when (cellsPerRow) {
			1 -> if (addressableRef.addressable.disassemblyWidth > 0) {
				when (columnIndex) {
					0 -> JLabel.RIGHT
					1 -> JLabel.LEFT
					2 -> JLabel.LEFT
					else -> throw IllegalArgumentException()
				}
			} else {
				when (columnIndex) {
					0 -> JLabel.RIGHT
					1 -> JLabel.LEFT
					else -> throw IllegalArgumentException()
				}
			}
			else -> JLabel.RIGHT
		}
}

