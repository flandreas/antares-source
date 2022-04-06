package ch.scorpion.jabbah.base.swing

import java.awt.Component
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableColumn
import javax.swing.JViewport


/**
 *	Use a [JTable] as a renderer for row numbers of a given main table.
 *  This table must be added to the row header of the [JScrollPane] that
 *  contains the main table.
 */
class RowHeaderTable(
    private val main: JTable,
    private val rowHeaderTextProvider: (Int) -> String
) : JTable(), ChangeListener, PropertyChangeListener {

    constructor(main: JTable): this(main, { (it + 1).toString() })

    init {
        main.addPropertyChangeListener(this)
        main.model.addTableModelListener(this)

        isFocusable = false
        setAutoCreateColumnsFromModel(false)
        setSelectionModel(main.selectionModel)

        val column = TableColumn()
        column.headerValue = " "
        addColumn(column)
        column.cellRenderer = RowHeaderRenderer()

        getColumnModel().getColumn(0).preferredWidth = 50
        preferredScrollableViewportSize = preferredSize
    }

    /** ---- [JTable] */

    override fun addNotify() {
        super.addNotify()
        val c = parent

        // Keep scrolling of the row table in sync with the main table.
        if (c is JViewport) {
            c.addChangeListener(this)
        }
    }

    override fun getRowCount(): Int = main.rowCount

    override fun getRowHeight(row: Int): Int {
        val rowHeight = main.getRowHeight(row)
        if (rowHeight != super.getRowHeight(row)) {
            super.setRowHeight(row, rowHeight)
        }
        return rowHeight
    }

    /** No model is being used for this table so just use the row number as the value of the cell. */
    override fun getValueAt(row: Int, column: Int): Any {
        return rowHeaderTextProvider.invoke(row)
    }

    override fun isCellEditable(row: Int, column: Int): Boolean = false

    override fun setValueAt(aValue: Any?, row: Int, column: Int) {
        // empty
    }

    /** ---- [ChangeListener] */

    override fun stateChanged(e: ChangeEvent) {
        // Keep the scrolling of the row table in sync with main table
        val viewport = e.source as JViewport
        val scrollPane = viewport.parent as JScrollPane
        scrollPane.verticalScrollBar.value = viewport.viewPosition.y
    }

    /** ---- [PropertyChangeListener] */

    override fun propertyChange(e: PropertyChangeEvent) {
        // Keep the row table in sync with the main table

        if ("selectionModel" == e.propertyName) {
            setSelectionModel(main.selectionModel)
        }

        if ("rowHeight" == e.propertyName) {
            repaint()
        }

        if ("model" == e.propertyName) {
            main.model.addTableModelListener(this)
            revalidate()
        }
    }

    /** ---- [TableModelListener] */

    override fun tableChanged(e: TableModelEvent?) {
        revalidate()
    }

    private class RowHeaderRenderer : DefaultTableCellRenderer() {

        init {
            horizontalAlignment = JLabel.CENTER
        }

        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            if (table != null) {
				val header = table.tableHeader

				if (header != null) {
					foreground = header.foreground
					background = header.background
					font = header.font
				}
			}

			if (isSelected) {
				font = font.deriveFont(Font.BOLD)
			}

            text = value?.toString() ?: ""
			border = UIManager.getBorder("TableHeader.cellBorder")

			return this
        }
    }
}