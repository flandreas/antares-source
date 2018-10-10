package ch.scorpion.jabbah.edit

import com.l2fprod.common.propertysheet.PropertySheetPanel
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
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

    companion object {
        private val LOG by logger(AbstractPropertyPanel::class)
    }

    /** Displays the properties of the selected [Component].*/
    private val sheet: PropertySheetPanel = sheetFactory.create()

    /** Displays the title that identifies the selected [Component].*/
    private val label: JLabel

    /** The object whose properties are currently being edited.*/
    private var propertyObject: Any? = null

    init {
	    editor.addPropertyChangeListener(object : PropertyChangeListener<Any> {
		    override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			    if (e.name == Editor.PROP_ACTIVE) {
				    if (editor.active) {
					    updateProperties(editor.drawing)
				    } else {
					    clearProperties()
				    }
			    }
		    }
	    })
        sheet.addPropertySheetChangeListener {
            if (propertyObject != null) {
                sheet.writeToObject(propertyObject)
                editor.view.drawing.validate()
	            // Read back object to account for calculated properties
	            sheet.readFromObject(propertyObject)
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
	    updateProperties(bean, bean.javaClass.name + "BeanInfo")
    }

    protected fun updateProperties(bean: Any, classPath: String) {
        try {
            val beanInfoClass = Class.forName(classPath)
            val beanInfo = beanInfoClass.newInstance() as AbstractBeanInfo<Any>

	        LOG.debug("PropertyPanel: updating properties for ${beanInfoClass}")

            sheet.properties = beanInfo.getProperties(bean, editor)
            sheet.readFromObject(bean)

            val table = getTable()
            adjustTableHeights(table)
            propertyObject = bean
            updateLabel()
        } catch (e: Throwable) {
            LOG.debug("Could not instantiate Properties for $classPath: Exception $e")
            clearProperties()
        }
    }

    private fun adjustTableHeights(table: JTable) {
        try {
            for (row in 0 until table.rowCount) {
                var rowHeight = table.rowHeight

                for (column in 0 until table.columnCount) {
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
        if (propertyObject == null) {
            label.text = ""
        } else {
            val description = getDescription(propertyObject!!)
            val beanDescription = if (StringUtils.isEmpty(description)) {
                Translations.getString("edit.property.bean.undefined")
            } else {
                StringUtils.replaceNegation(description!!)
            }
            label.text = Translations.getString("edit.property.bean", beanDescription)
        }
    }
}