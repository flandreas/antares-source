package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.AddressableReference
import ch.scorpion.antares.model.addressable.Memory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import javax.swing.JLabel
import javax.swing.table.TableModel
import kotlin.math.ceil
import kotlin.math.min

/**
 * Specified how the individual cells of an [Addressable] are to be represented as a [TableModel],
 * by for example specifying how many columns are to be displayed.
 **/
interface AddressableDisplayLayout {

	val cellsPerRow: Int

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

	companion object {
		private const val MAX_ROWS = 1_024
	}

	private val rowCount: Int get() = min(
		MAX_ROWS,
		ceil(((addressableRef.addressable.addressWidth.maxValue + 1UL) / cellsPerRow.toULong()).toDouble()).toInt())

	override fun toString(): String =
		Translations.getString("antares.memory.layout.columns", cellsPerRow)

	override fun getCellAddress(rowIndex: Int, columnIndex: Int): Int =
		when (cellsPerRow) {
			1 -> rowIndex
			else -> rowIndex * cellsPerRow + columnIndex
		}

	override fun createTableModel(): AbstractAddressableTableModel =
		when (cellsPerRow) {
			1 -> SingleColumnTableModel(addressableRef, rowCount, editable, converterProvider, signalHandler)
			else -> AddressableTableModel(cellsPerRow, addressableRef, rowCount, editable, converterProvider,  signalHandler)
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

