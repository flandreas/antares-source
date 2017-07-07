package ch.scorpion.jabbah.edit

import com.l2fprod.common.propertysheet.PropertySheetPanel
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.loggerFor
import java.awt.BorderLayout
import java.awt.Container
import java.beans.PropertyDescriptor
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable


/**
 * A [JPanel] for editing the properties of a bean.
 */
abstract class AbstractPropertyPanel(
    protected val editor: Editor,
    sheetFactory: PropertySheetPanelFactory
) : JPanel() {

    private val LOG by loggerFor(this)

    /** Displays the properties of the selected [Component].*/
    private val sheet: PropertySheetPanel

    /** Displays the title that identifies the selected [Component].*/
    private val label: JLabel

    /** The object whose properties are currently being edited.*/
    private var propertyObject: Any? = null

    init {
        sheet = sheetFactory.create()
        sheet.addPropertySheetChangeListener {
            if (propertyObject != null) {
                sheet.writeToObject(propertyObject)
                editor.view.drawing.validate()
            }
        }

        label = JLabel()
        label.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)

        layout = BorderLayout()
        add(label, BorderLayout.NORTH)
        add(sheet, BorderLayout.CENTER)
    }

    /** ---- [AbstractPropertyPanel] */

    /** Returns a displayable description of the selected bean object.*/
    protected abstract fun getDescription(bean: Any): String?

    protected fun clearProperties() {
        sheet.setProperties(arrayOf<PropertyDescriptor>())
        propertyObject = null
        updateLabel()
    }

    protected fun updateProperties(bean: Any) {
        try {
            val beanInfoClass = Class.forName(bean.javaClass.name + "BeanInfo")
            val beanInfo = beanInfoClass.newInstance() as AbstractBeanInfo<Any>

            sheet.setProperties(beanInfo.getProperties(bean, editor))
            sheet.readFromObject(bean)

            val table = getTable()
            adjustTableHeights(table)
            propertyObject = bean
            updateLabel()
        } catch (e: Throwable) {
            LOG.debug("Could not instantiate Properties for ${bean.javaClass.simpleName}: Exception ${e.toString()}")
            clearProperties()
        }
    }

    private fun adjustTableHeights(table: JTable) {
        try {
            for (row in 0..table.rowCount - 1) {
                var rowHeight = table.rowHeight

                for (column in 0..table.columnCount - 1) {
                    val comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column)
                    rowHeight = Math.max(rowHeight, comp.preferredSize.height)
                }

                table.setRowHeight(row, rowHeight)
            }
        } catch (e: ClassCastException) {
            println("Exception while adjusting PropertySheetTable heights")
        }
    }

    private fun getTable(): JTable {
        return ((sheet.getComponent(1) as Container).getComponent(0) as Container).getComponent(0) as JTable
    }

    private fun updateLabel() {
        // TODO I18N
        if (propertyObject == null) {
            label.text = ""
        } else {
            val sb = StringBuilder("Eigenschaften von \"")
            val description = getDescription(propertyObject!!)
            if (StringUtils.isEmpty(description)) {
                sb.append("<undefiniert>")
            } else {
                sb.append(description)
            }
            sb.append("\"")
            label.text = sb.toString()
        }
    }
}