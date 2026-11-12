package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.*
import io.antarescircuit.jabbah.base.Settings
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.FocusJTable
import io.antarescircuit.jabbah.base.swing.RowHeaderTable
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.ActionEvent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
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
	private val addressField = JTextField(10)
	private val goButton = JButton(Translations.getString("antares.memory.navigation.go"))
	private val previousPageButton = JButton("‹")
	private val nextPageButton = JButton("›")
	private val currentAddressButton = JButton(Translations.getString("antares.memory.navigation.current"))
	private val rangeLabel = JLabel()
	private val maxAddress: Int get() = addressableRef.addressable.addressWidth.maxValue.coerceAtMost(Int.MAX_VALUE.toULong()).toInt()
	private val currentPage: MemoryPage get() = MemoryPage(addressableDisplayLayout.startAddress, maxAddress, addressableDisplayLayout.cellsPerRow)
	private val selectedAddress: Int? get() = if (table.selectedRow >= 0 && table.selectedColumn >= 0) {
		(table.model as AbstractAddressableTableModel).addressAt(table.selectedRow, table.selectedColumn).takeIf { it >= 0 }
	} else null

	private var displayedAddressable: Addressable = addressableRef.addressable

	private val rowHeaderTable = RowHeaderTable(
			table,
			preferredWidth = 80,
			renderer = RowHeaderTable.RowHeaderRenderer(JLabel.RIGHT)
		) {
			converter.render((addressableDisplayLayout.startAddress.toLong() + it.toLong() * addressableDisplayLayout.cellsPerRow).toULong(), addressableRef.addressable.addressWidth)
		}

	private val addressableListener = object : AddressableListener {
		override fun dataChanged(event: AddressableDataEvent) {
			if (event.address == null) refresh()
			val address = event.address
			val oldValue = event.oldValue
			val newValue = event.newValue
			if (address != null && oldValue != null && newValue != null) {
				if (oldValue != newValue && addressableRef.addressable.storesCells) {
					LOG.debug("Data changed at $address from $oldValue to $newValue")
					consumeValueChange(AddressableCellChange(address, oldValue, newValue))
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

		override fun bitWidthChanged(event: AddressableBitWidthEvent) {
			if (event.isAddress) updateAddressWidth() else refresh()
		}
	}

    init {
		val cellsPerRow = settings.getInt(SETTING_LAYOUT, DEF_LAYOUT)
		layoutComboBox.selectedItem = layouts.firstOrNull { it.cellsPerRow == cellsPerRow } ?: layouts[0]

        buildUI()
	    layoutComboBox.addActionListener { updateMemoryDisplayLayout(addressableDisplayLayout) }
		converterComboBox.addActionListener {
			if (table.isEditing) {
				table.cellEditor.stopCellEditing()
			}
			updateColumnHeaders()
			updateNavigation()
			table.invalidate()
			revalidate()
			repaint()
		}
	    updateMemoryDisplayLayout(addressableDisplayLayout)

		addressableRef.addListener(addressableListener)
		configureNavigation()
    }

	fun dispose() {
		addressableRef.removeListener(addressableListener)
		storeSettings()
	}

	private val converter: AddressableValueConverter get() = converterComboBox.selectedItem as AddressableValueConverter

	fun refresh() {
		if (displayedAddressable !== addressableRef.addressable) {
			updateMemoryDisplayLayout(addressableDisplayLayout)
		}
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
			table.columnModel.getColumn(i).headerValue = table.model.getColumnName(i)
		}
		table.tableHeader.repaint()
		scrollPane.rowHeader?.repaint()
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

		val header = JPanel(BorderLayout())
		header.add(panel, BorderLayout.NORTH)
		header.add(buildNavigationPanel(), BorderLayout.SOUTH)
		return header
	}

	fun addViewActivationFocusListener(focusListener: FocusListener) {
		layoutComboBox.addFocusListener(focusListener)
		table.addFocusListener(focusListener)
		converterComboBox.addFocusListener(focusListener)
		addressField.addFocusListener(focusListener)
		goButton.addFocusListener(focusListener)
		currentAddressButton.addFocusListener(focusListener)
	}

	private fun updateMemoryDisplayLayout(addressableDisplayLayout: AddressableDisplayLayout) {
		if (table.isEditing) {
			if (displayedAddressable !== addressableRef.addressable) {
				// The editor still belongs to the state that was replaced by undo/redo.
				table.cellEditor.cancelCellEditing()
			} else {
				table.cellEditor.stopCellEditing()
			}
		}
		displayedAddressable = addressableRef.addressable
		val address = (selectedAddress ?: addressableDisplayLayout.startAddress).coerceIn(0, maxAddress)
		addressableDisplayLayout.startAddress = MemoryPage.containing(address, maxAddress, addressableDisplayLayout.cellsPerRow).start
		storeSettings()

		val tableModel = addressableDisplayLayout.createTableModel()
		table.model = tableModel


		scrollPane.setRowHeaderView(rowHeaderTable)
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)

		for (i in 0 until table.columnCount) {
			val column = table.columnModel.getColumn(i)

			if (tableModel.isCommentColumn(i)) {
				column.cellEditor = AddressableCommentEditor()
				column.preferredWidth = settings.getInt(SETTING_COMMENT_COLUMN_WIDTH, DEF_COMMENT_COLUMN_WIDTH)
			} else {
				column.cellEditor = AddressableValueEditor(addressableRef, ::converter).apply {
					font = table.font
				}
				column.preferredWidth = DEF_VALUE_COLUMN_WIDTH
			}

			val tableCellRenderer = AddressableCellRenderer(applicationContextHolder, addressableRef, addressableDisplayLayout)
			tableCellRenderer.horizontalAlignment = addressableDisplayLayout.columnAlignment(i)
			column.cellRenderer = tableCellRenderer
		}
		updateNavigation()
		selectAddress(address, requestFocus = false)
	}

	private fun buildNavigationPanel(): JPanel = JPanel().apply {
		border = BorderFactory.createEmptyBorder(0, 5, 5, 5)
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)
		val label = JLabel(Translations.getString("antares.memory.navigation.address"))
		label.labelFor = addressField
		add(label)
		add(Box.createHorizontalStrut(UIBasics.LABEL_GAP))
		addressField.maximumSize = addressField.preferredSize
		addressField.accessibleContext.accessibleName = label.text
		addressField.toolTipText = Translations.getString("antares.memory.navigation.hint")
		add(addressField)
		add(goButton)
		add(Box.createHorizontalStrut(10))
		add(currentAddressButton)
		add(Box.createHorizontalGlue())
		add(previousPageButton)
		add(rangeLabel)
		add(nextPageButton)
	}

	private fun configureNavigation() {
		fun jump() {
			val address = MemoryPage.parseAddress(addressField.text, converter, maxAddress) ?: return
			goToAddress(address)
		}
		addressField.addActionListener { jump() }
		goButton.addActionListener { jump() }
		previousPageButton.toolTipText = Translations.getString("antares.memory.navigation.previous")
		nextPageButton.toolTipText = Translations.getString("antares.memory.navigation.next")
		previousPageButton.accessibleContext.accessibleName = previousPageButton.toolTipText
		nextPageButton.accessibleContext.accessibleName = nextPageButton.toolTipText
		previousPageButton.addActionListener { currentPage.previous?.let { goToAddress(it) } }
		nextPageButton.addActionListener { currentPage.next?.let { goToAddress(it) } }
		currentAddressButton.addActionListener { goToAddress(addressableRef.addressable.currentAddress) }
		addressField.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) = validateAddress()
			override fun removeUpdate(e: DocumentEvent?) = validateAddress()
			override fun changedUpdate(e: DocumentEvent?) = validateAddress()
		})
		val shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx)
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(shortcut, "goToAddress")
		actionMap.put("goToAddress", object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				addressField.requestFocusInWindow()
				addressField.selectAll()
			}
		})
		for ((key, forward) in listOf(KeyEvent.VK_PAGE_UP to false, KeyEvent.VK_PAGE_DOWN to true)) {
			val name = "memoryPage$forward"
			getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key, java.awt.Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx), name)
			actionMap.put(name, object : AbstractAction() {
				override fun actionPerformed(e: ActionEvent?) {
					(if (forward) currentPage.next else currentPage.previous)?.let { goToAddress(it) }
				}
			})
		}
		updateNavigation()
	}

	private fun validateAddress() {
		val valid = MemoryPage.parseAddress(addressField.text, converter, maxAddress) != null
		goButton.isEnabled = valid
		addressField.putClientProperty("JComponent.outline", if (addressField.text.isNotBlank() && !valid) "error" else null)
		addressField.toolTipText = if (valid || addressField.text.isBlank()) {
			Translations.getString("antares.memory.navigation.hint")
		} else {
			Translations.getString("antares.memory.navigation.invalid", converter.render(maxAddress.toULong(), addressableRef.addressable.addressWidth))
		}
	}

	private fun updateNavigation() {
		val page = currentPage
		previousPageButton.isEnabled = page.previous != null
		nextPageButton.isEnabled = page.next != null
		currentAddressButton.isEnabled = addressableRef.addressable.currentAddress in 0..maxAddress
		rangeLabel.text = " ${converter.render(page.start.toULong(), addressableRef.addressable.addressWidth)}–${converter.render(page.end.toULong(), addressableRef.addressable.addressWidth)} "
		validateAddress()
	}

	/** Reveals any supported address, loading just one page even for very large memories. */
	internal fun goToAddress(address: Int): Boolean {
		if (address !in 0..maxAddress) return false
		if (table.isEditing && !table.cellEditor.stopCellEditing()) return false
		if (address !in currentPage.start..currentPage.end) {
			// Clear the old selection before replacing the model, so the requested page wins.
			table.clearSelection()
			addressableDisplayLayout.startAddress = MemoryPage.containing(address, maxAddress, addressableDisplayLayout.cellsPerRow).start
			updateMemoryDisplayLayout(addressableDisplayLayout)
		}
		selectAddress(address, requestFocus = true)
		return true
	}

	private fun selectAddress(address: Int, requestFocus: Boolean) {
		val offset = address - addressableDisplayLayout.startAddress
		val row = offset / addressableDisplayLayout.cellsPerRow
		val column = offset % addressableDisplayLayout.cellsPerRow
		if (row !in 0 until table.rowCount || column !in 0 until table.columnCount) return
		// setSelectionInterval avoids FocusJTable's automatic edit-on-navigation behavior.
		table.setRowSelectionInterval(row, row)
		table.setColumnSelectionInterval(column, column)
		table.scrollRectToVisible(table.getCellRect(row, column, true))
		if (requestFocus) table.requestFocusInWindow()
	}

	private fun consumeValueChange(change: AddressableCellChange) {
		cmdManager.register(AddressableCellChangeCommand(addressableRef.view, addressableRef.link, listOf(change)))
	}

	private fun consumeCommentChange(change: AddressableCommentChange) {
		cmdManager.register(AddressableCommentChangeCommand(addressableRef.view, addressableRef.link, listOf(change)))
	}

	private fun storeSettings() {
		val model = table.model as AbstractAddressableTableModel
		for (column in 0 until model.columnCount) {
			if (model.isCommentColumn(column)) {
				settings.set(SETTING_COMMENT_COLUMN_WIDTH, table.columnModel.getColumn(column).width)
			}
		}
		settings.set(SETTING_LAYOUT, addressableDisplayLayout.cellsPerRow)
	}
}
