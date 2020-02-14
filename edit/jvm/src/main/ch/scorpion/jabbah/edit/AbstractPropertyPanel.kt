package ch.scorpion.jabbah.edit

import com.l2fprod.common.propertysheet.PropertySheetPanel
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.beans.PropertyDescriptor
import javax.swing.*
import kotlin.math.max


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

	private val messageTextArea: JTextArea

	private val messageTextScroll: JScrollPane

	/** The object whose properties are currently being edited.*/
	private var propertyObject: Any? = null

	private val propertyStorer: java.beans.PropertyChangeListener = object : java.beans.PropertyChangeListener {
		override fun propertyChange(evt: java.beans.PropertyChangeEvent?) {
			storeProperties()
		}
	}

	init {
		editor.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == Editor.PROP_ACTIVE) {
					if (editor.active) {
						setupDefaultProperties()
					} else {
						clearProperties()
					}
				}
			}
		})
		sheet.addPropertySheetChangeListener(propertyStorer)

		label = JLabel()
		label.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)

		messageTextArea = JTextArea()
		messageTextArea.rows = 3
		messageTextArea.isEditable = false
		messageTextArea.foreground = Color.RED
		messageTextArea.background = background
		messageTextArea.wrapStyleWord = true
		messageTextArea.lineWrap = true
		messageTextArea.border = null

		messageTextScroll = UiUtil.decorateTextArea(messageTextArea)
		messageTextScroll.border = BorderFactory.createEmptyBorder(2, 4, 2, 4)
		messageTextScroll.background = background

		layout = BorderLayout()
		add(label, BorderLayout.NORTH)
		add(sheet, BorderLayout.CENTER)
	}

	/** ---- [AbstractPropertyPanel] */

	/**
	 * Fill with properties of the object to be displayed per default, for example when the [Editor]
	 * just has become active.
	 */
	protected abstract fun setupDefaultProperties()

	/** Returns a displayable description of the selected bean object.*/
	protected abstract fun getDescription(bean: Any): String?

	protected fun clearProperties() {
		sheet.table?.cellEditor?.stopCellEditing()
		sheet.setProperties(arrayOf<PropertyDescriptor>())
		propertyObject = null
		hideMessage()
		updateLabel()
	}

	protected fun showMessage(message: String) {
		messageTextArea.text = message
		add(messageTextScroll, BorderLayout.SOUTH)
		invalidate()
		revalidate()
		repaint()
	}

	protected fun hideMessage() {
		messageTextArea.text = ""
		remove(messageTextScroll)
		revalidate()
		repaint()
	}

	private fun storeProperties() {
		if (propertyObject != null) {
			try {
				sheet.writeToObject(propertyObject)
				editor.view.drawing.validate()
				hideMessage()
			} catch (e: Throwable) {
				e.message?.let { showMessage(it) }
			}
			readBackCalculatedProperties()
		}
	}

	private fun readBackCalculatedProperties() {
		sheet.removePropertySheetChangeListener(propertyStorer)
		sheet.readFromObject(propertyObject)
		sheet.addPropertySheetChangeListener(propertyStorer)
	}

	protected fun loadProperties(bean: Any) {
		loadProperties(bean, bean.javaClass.name + "BeanInfo")
	}

	protected fun loadProperties(bean: Any, classPath: String) {
		try {
			val beanInfoClass = Class.forName(classPath)
			@Suppress("UNCHECKED_CAST")
			val beanInfo = beanInfoClass.getDeclaredConstructor().newInstance() as AbstractBeanInfo<Any>

			LOG.debug("updating properties for $beanInfoClass")

			sheet.properties = beanInfo.getProperties(bean, editor)

			// Avoid triggering property change login while bean properties are loaded
			propertyObject = null

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

				val label = table.getCellRenderer(row, 0) as JLabel
				label.verticalAlignment = JLabel.TOP

				for (column in 0 until table.columnCount) {
					val comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column)
					rowHeight = max(rowHeight, comp.preferredSize.height)
				}

				table.setRowHeight(row, rowHeight)
			}
		} catch (e: ClassCastException) {
			println("Exception while adjusting PropertySheetTable heights")
		}
	}

	protected fun getTable(): JTable {
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