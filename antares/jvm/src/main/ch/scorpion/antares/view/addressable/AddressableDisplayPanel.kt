package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.*
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.FocusJTable
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.FocusListener
import javax.swing.*

/**
 * Displays the value of the individual cells of an [Addressable] using a [JTable].
 *
 * Allows the user to choose from different table layouts.
 * Creates [AddressableCellChangeCommand]s when the user manually edits cell.
 */
class AddressableDisplayPanel(
	private val addressableRef: AddressableReference,
	editable: () -> Boolean,
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val cmdManager: CommandManager = EditModule.commandManager,
	private val settings: Settings = BaseModule.settings
) : JPanel() {

	companion object {
		private const val SETTING_COMMENT_COLUMN_WIDTH = "addressable.commentWidth"
		private const val DEF_VALUE_COLUMN_WIDTH = 50
		private const val DEF_COMMENT_COLUMN_WIDTH = 200
	}

	private val layouts = arrayOf<AddressableDisplayLayout>(
		FixedWidthLayout(1, addressableRef, editable, applicationContextHolder.scheduler),
		FixedWidthLayout(4, addressableRef, editable, applicationContextHolder.scheduler),
		FixedWidthLayout(8, addressableRef, editable, applicationContextHolder.scheduler),
		FixedWidthLayout(16, addressableRef, editable, applicationContextHolder.scheduler)
	)

    private val table = FocusJTable(layouts[1].createTableModel())
	private val scrollPane = JScrollPane(table)
	private val layoutComboBox = JComboBox(layouts)
	private val addressableDisplayLayout: AddressableDisplayLayout get() = layoutComboBox.selectedItem as AddressableDisplayLayout

    init {
        buildUI()
	    layoutComboBox.addActionListener { updateMemoryDisplayLayout(addressableDisplayLayout, exchange = true) }
	    updateMemoryDisplayLayout(addressableDisplayLayout)
    }

	fun dispose() {
		storeCommentColumnWidth()
	}

	fun refresh() {
		table.invalidate()
		table.revalidate()
		table.repaint()
	}

    private fun buildUI() {
        layout = BorderLayout()

	    table.font = Font("Monospaced", Font.PLAIN, table.font.size)
	    table.tableHeader.reorderingAllowed = false
	    table.autoResizeMode = JTable.AUTO_RESIZE_OFF

	    val memoryLayoutPanel = JPanel(FlowLayout(FlowLayout.LEFT))
	    memoryLayoutPanel.add(JLabel(Translations.getString("antares.memory.layout.selector.name")))
	    memoryLayoutPanel.add(layoutComboBox)

	    add(memoryLayoutPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

	fun addViewActivationFocusListener(focusListener: FocusListener) {
		layoutComboBox.addFocusListener(focusListener)
		table.addFocusListener(focusListener)
	}

	private fun updateMemoryDisplayLayout(addressableDisplayLayout: AddressableDisplayLayout, exchange: Boolean = false) {
		if (exchange) {
			storeCommentColumnWidth()
		}

		val tableModel = addressableDisplayLayout.createTableModel()
		table.model = tableModel

		val rowHeaderTable = RowHeaderTable(table) {
			BitOperation.longToHexPadded(it.toULong() * addressableDisplayLayout.cellsPerRow.toUInt(), addressableRef.addressable.addressWidth)
		}
		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)

		for (i in 0 until table.columnCount) {
			val column = table.columnModel.getColumn(i)

			if (tableModel.isCommentColumn(i)) {
				column.cellEditor = AddressableCommentEditor(addressableRef, addressableDisplayLayout, ::consumeCommentChange)
				column.preferredWidth = settings.getInt(SETTING_COMMENT_COLUMN_WIDTH, DEF_COMMENT_COLUMN_WIDTH)
			} else {
				column.cellEditor = AddressableValueEditor(addressableRef, addressableDisplayLayout, ::consumeValueChange)
				column.preferredWidth = DEF_VALUE_COLUMN_WIDTH
			}

			val tableCellRenderer = AddressableCellRenderer(applicationContextHolder, addressableRef, addressableDisplayLayout)
			tableCellRenderer.horizontalAlignment = addressableDisplayLayout.columnAlignment(i)
			column.cellRenderer = tableCellRenderer
		}
	}

	private fun consumeValueChange(change: AddressableCellChange) {
		cmdManager.register(AddressableCellChangeCommand(addressableRef.view, addressableRef.link, listOf(change)))
	}

	private fun consumeCommentChange(change: AddressableCommentChange) {
		cmdManager.register(AddressableCommentChangeCommand(addressableRef.view, addressableRef.link, listOf(change)))
	}

	private fun storeCommentColumnWidth() {
		if (table.model.columnCount == 2) {
			settings.set(SETTING_COMMENT_COLUMN_WIDTH, table.columnModel.getColumn(1).width)
		}
	}
}