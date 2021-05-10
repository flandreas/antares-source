package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.ui.AbstractPropertyPanelController
import ch.scorpion.jabbah.edit.ui.PropertyPanel
import com.l2fprod.common.propertysheet.PropertySheetPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.beans.PropertyDescriptor
import javax.swing.*
import kotlin.math.max


/**
 * A [JPanel] for editing the properties of a bean.
 */
abstract class AbstractPropertyPanelSwing(
	protected val controller: AbstractPropertyPanelController<*>,
	sheetFactory: PropertySheetPanelFactory
) : JPanel(), PropertyPanel {

	companion object {
		private val LOG by logger(AbstractPropertyPanelSwing::class)
	}

	/** Displays the properties of the selected [Component].*/
	private val sheet: PropertySheetPanel = sheetFactory.create()

	/** Displays the title that identifies the selected [Component].*/
	private val title: JLabel

	/** Used for displaying messages such as validation errors.*/
	private val messageTextArea: JTextArea

	private val messageTextScroll: JScrollPane

	/** The object whose properties are currently being edited.*/
	private var propertyObject: Any? = null

	private val propertyStorer: java.beans.PropertyChangeListener = object : java.beans.PropertyChangeListener {
		override fun propertyChange(evt: java.beans.PropertyChangeEvent?) {
			storeProperties()
		}
	}

	private val activeEditorListener = setupActiveEditorListener()

	private val drawingListener = setupDrawingListener()

	init {
		sheet.addPropertySheetChangeListener(propertyStorer)

		getTable().setShowGrid(true)

		title = JLabel()
		title.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)

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
		add(title, BorderLayout.NORTH)
		add(sheet, BorderLayout.CENTER)
	}

	override fun dispose() {
		controller.editor.removePropertyChangeListener(activeEditorListener)
		controller.editor.removePropertyChangeListener(drawingListener)
	}

	private fun setupActiveEditorListener(): PropertyChangeListener<Any> = controller.editor.addPropertyChangeListener { event ->
		if (event.name == Editor.PROP_ACTIVE) {
			if (controller.editor.active) {
				setupDefaultProperties()
			} else {
				clearProperties()
			}
		}
	}

	private fun setupDrawingListener(): PropertyChangeListener<Any> = controller.editor.view.addPropertyChangeListener { event ->
		if (event.name == DrawingView.PROP_DRAWING) {
			setupDefaultProperties()
		}
	}

	/** ---- [PropertyPanel] interface */

	override fun handleBeanReplaced() {
		clearProperties()
		controller.bean?.let { loadProperties(it) }
	}

	protected fun clearProperties() {
		sheet.table?.cellEditor?.stopCellEditing()
		sheet.setProperties(arrayOf<PropertyDescriptor>())
		propertyObject = null
		hideMessage()
		updateLabel()
	}

	protected fun loadProperties(bean: Any) {
		loadProperties(bean, bean.javaClass.name + "BeanInfo")
	}

	/** ---- [AbstractPropertyPanelSwing] */

	/**
	 * Fill with properties of the object to be displayed per default, for example when the [Editor]
	 * just has become active.
	 */
	protected abstract fun setupDefaultProperties()

	private fun showMessage(message: String) {
		messageTextArea.text = message
		add(messageTextScroll, BorderLayout.SOUTH)
		invalidate()
		revalidate()
		repaint()
	}

	private fun hideMessage() {
		messageTextArea.text = ""
		remove(messageTextScroll)
		revalidate()
		repaint()
	}

	private fun storeProperties() {
		if (propertyObject != null) {
			try {
				LOG.trace("storeProperties")
				sheet.writeToObject(propertyObject)
				controller.editor.view.drawing.validate()
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

	protected fun loadProperties(bean: Any, classPath: String) {
		try {
			val beanInfoClass = Class.forName(classPath)
			@Suppress("UNCHECKED_CAST")
			val beanInfo = beanInfoClass.getDeclaredConstructor().newInstance() as AbstractBeanInfo<Any>

			LOG.debug("updating properties for $beanInfoClass")

			sheet.properties = beanInfo.getProperties(bean, controller.editor)

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
		title.text = controller.title
	}
}