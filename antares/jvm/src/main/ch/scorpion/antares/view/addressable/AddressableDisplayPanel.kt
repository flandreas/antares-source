package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.FocusJTable
import ch.scorpion.jabbah.base.swing.RowHeaderTable
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import java.awt.BorderLayout
import java.awt.Dimension
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
		private val LOG by logger(AddressableDisplayPanel::class)
		private const val SETTING_COMMENT_COLUMN_WIDTH = "addressable.commentWidth"
		private const val DEF_VALUE_COLUMN_WIDTH = 50
		private const val DEF_COMMENT_COLUMN_WIDTH = 200
		private const val SETTING_LAYOUT = "addressable.layout"
		private const val DEF_LAYOUT = 1
	}

	private val layouts = arrayOf<AddressableDisplayLayout>(
		FixedWidthLayout(1, addressableRef, editable, ::converter, applicationContextHolder.scheduler),
		FixedWidthLayout(4, addressableRef, editable, ::converter, applicationContextHolder.scheduler),
		FixedWidthLayout(8, addressableRef, editable, ::converter, applicationContextHolder.scheduler),
		FixedWidthLayout(16, addressableRef, editable, ::converter, applicationContextHolder.scheduler)
	)

	private val layoutComboBox = JComboBox(layouts)
	private val addressableDisplayLayout: AddressableDisplayLayout get() = layoutComboBox.selectedItem as AddressableDisplayLayout
	private val converterComboBox = JComboBox(AddressableValueConverter.entries.toTypedArray())
	private val table = FocusJTable(layouts[1].createTableModel())
	private val scrollPane = JScrollPane(table)

	private val addressableListener = object : AddressableListener {
		override fun dataChanged(event: AddressableDataEvent) {
			if (event.address != null && event.oldValue != null && event.newValue != null) {
				if (event.oldValue != event.newValue && addressableRef.addressable.storesCells) {
					LOG.debug("Data changed at ${event.address} from ${event.oldValue} to ${event.newValue}")
					consumeValueChange(AddressableCellChange(event.address, event.oldValue, event.newValue))
				}
			}
		}

		override fun commentChanged(event: AddressableCommentEvent) {
			if (event.oldValue == null && StringUtils.isBlank(event.newValue)) {
				return
			}
			if (event.oldValue == event.newValue) {
				return
			}
			if (!addressableRef.addressable.storesCells) {
				return
			}
			LOG.debug("Comment changed at ${event.address} from '${event.oldValue}' to '${event.newValue}'")
			consumeCommentChange(AddressableCommentChange(event.address, event.oldValue, event.newValue))
		}

		override fun bitWidthChanged(event: AddressableBitWidthEvent) {}
	}

    init {
		val cellsPerRow = settings.getInt(SETTING_LAYOUT, DEF_LAYOUT)
		layouts.firstOrNull { it.cellsPerRow == cellsPerRow }.let {
			layoutComboBox.selectedItem = it
		}

        buildUI()
	    layoutComboBox.addActionListener { updateMemoryDisplayLayout(addressableDisplayLayout) }
		converterComboBox.addActionListener {
			if (table.isEditing) {
				table.cellEditor.stopCellEditing()
			}
			updateColumnHeaders()
			table.invalidate()
			revalidate()
			repaint()
		}
	    updateMemoryDisplayLayout(addressableDisplayLayout)

		addressableRef.addListener(addressableListener)
    }

	fun dispose() {
		addressableRef.removeListener(addressableListener)
		storeSettings()
	}

	private val converter: AddressableValueConverter get() = converterComboBox.selectedItem as AddressableValueConverter

	fun refresh() {
		table.invalidate()
		table.revalidate()
		table.repaint()
	}

	fun updateAddressWidth() {
		updateMemoryDisplayLayout(addressableDisplayLayout)
	}

    private fun buildUI() {
        layout = BorderLayout()

	    table.font = Font("Monospaced", Font.PLAIN, table.font.size)
	    table.tableHeader.reorderingAllowed = false
	    table.autoResizeMode = JTable.AUTO_RESIZE_OFF

	    add(buildHeaderPanel(), BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

	private fun updateColumnHeaders() {
		for (i in 0 until table.columnCount) {
			table.columnModel.getColumn(i).headerValue = converter.render(i.toULong(), BitWidth.BW_4)
		}
	}

	private fun buildHeaderPanel(): JPanel {
		val panel = JPanel()
		panel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		layoutComboBox.maximumSize = Dimension(layoutComboBox.preferredSize.width, layoutComboBox.preferredSize.height)
		panel.add(JLabel(Translations.getString("antares.memory.layout.selector.name") + ":"))
		panel.add(Box.createHorizontalStrut(UIBasics.LABEL_GAP))
		panel.add(layoutComboBox)

		panel.add(Box.createHorizontalStrut(15))

		converterComboBox.maximumSize = Dimension(converterComboBox.preferredSize.width, converterComboBox.preferredSize.height)
		panel.add(JLabel(Translations.getString("antares.addressableValueConverter.name") + ":"))
		panel.add(Box.createHorizontalStrut(UIBasics.LABEL_GAP))
		panel.add(converterComboBox)

		panel.add(Box.createHorizontalGlue())

		return panel
	}

	fun addViewActivationFocusListener(focusListener: FocusListener) {
		layoutComboBox.addFocusListener(focusListener)
		table.addFocusListener(focusListener)
	}

	private fun updateMemoryDisplayLayout(addressableDisplayLayout: AddressableDisplayLayout) {
		storeSettings()

		val tableModel = addressableDisplayLayout.createTableModel()
		table.model = tableModel

		val rowHeaderTable = RowHeaderTable(
			table,
			preferredWidth = 80,
			renderer = RowHeaderTable.RowHeaderRenderer(JLabel.RIGHT)
		) {
			converter.render(it.toULong() * addressableDisplayLayout.cellsPerRow.toUInt(), addressableRef.addressable.addressWidth)
		}
		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)

		for (i in 0 until table.columnCount) {
			val column = table.columnModel.getColumn(i)

			if (tableModel.isCommentColumn(i)) {
				column.cellEditor = AddressableCommentEditor()
				column.preferredWidth = settings.getInt(SETTING_COMMENT_COLUMN_WIDTH, DEF_COMMENT_COLUMN_WIDTH)
			} else {
				column.cellEditor = AddressableValueEditor(addressableRef, /*addressableDisplayLayout,*/ ::converter, /*::consumeValueChange*/).apply {
					font = table.font
				}
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

	private fun storeSettings() {
		if (table.model.columnCount == 2) {
			settings.set(SETTING_COMMENT_COLUMN_WIDTH, table.columnModel.getColumn(1).width)
		}
		settings.set(SETTING_LAYOUT, addressableDisplayLayout.cellsPerRow)
	}
}