package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ui.AbstractPropertyPanelController
import ch.scorpion.jabbah.edit.ui.PropertyPanel
import com.l2fprod.common.propertysheet.PropertySheetPanel
import java.awt.BorderLayout
import java.awt.Color
import java.beans.PropertyDescriptor
import javax.swing.*
import kotlin.math.max


/**
 * A [JPanel] for editing the properties of a bean.
 *
 * @property scope the scope name used to distinguish different [Settings] values
 * of instances of this class
 */
abstract class AbstractPropertyPanelSwing(
	protected val controller: AbstractPropertyPanelController<*>,
	private val scope: String,
	sheetFactory: PropertySheetPanelFactory
) : JPanel(), PropertyPanel {

	companion object {
		private val LOG by logger(AbstractPropertyPanelSwing::class)

		/** The base name of the [Boolean] value in [Settings] that determines whether the description area is open.*/
		private const val PROP_DESC_OPEN_BASE = "edit.propertyPanel.descOpen"

		/** The base name of the [Int] value in [Settings] that determines the location of the description area's split location. */
		private const val PROP_DESC_SPLIT_LOCATION_BASE = "edit.propertyPanel.descSplitLocation"
	}

	private val descriptionOpenPropertyName: String get() = "$PROP_DESC_OPEN_BASE.$scope"

	private val descriptionSplitLocationPropertyName: String get() = "$PROP_DESC_SPLIT_LOCATION_BASE.$scope"

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
			if (evt?.source is AbstractReflectionPropertySwing<*>) {
				storeProperty(evt.source as AbstractReflectionPropertySwing<*>)
			} else {
				storeProperties()
			}
		}
	}

	init {
		sheet.addPropertySheetChangeListener(propertyStorer)

		sheet.table.setShowGrid(true)
		SwingUtilities.invokeLater {
			sheet.splitterLocation = BaseModule.settings.getInt(descriptionSplitLocationPropertyName, 0)
			sheet.isDescriptionVisible = BaseModule.settings.getBoolean(descriptionOpenPropertyName, true)
		}

		title = JLabel(controller.title)
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
		BaseModule.settings.set(descriptionOpenPropertyName, sheet.isDescriptionVisible)
		BaseModule.settings.set(descriptionSplitLocationPropertyName, sheet.splitterLocation)
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

	private fun storeProperty(property: AbstractReflectionPropertySwing<*>) {
		try {
			LOG.trace("storeProperty")
			property.writeToBean()
			controller.editor.view.drawing.validate()
			hideMessage()
		} catch (e: Throwable) {
			e.message?.let { showMessage(it) }
		}
		readBackCalculatedProperties()
	}

	private fun readBackCalculatedProperties() {
		sheet.removePropertySheetChangeListener(propertyStorer)
		propertyObject?.let { loadProperties(it) }
		sheet.addPropertySheetChangeListener(propertyStorer)
	}

	protected fun loadProperties(bean: Any, classPath: String) {
		try {
			val beanInfoClass = Class.forName(classPath)
			@Suppress("UNCHECKED_CAST")
			val beanInfo = beanInfoClass.getDeclaredConstructor().newInstance() as AbstractBeanInfo<Any>

			LOG.trace("updating properties for $beanInfoClass")

			sheet.properties = beanInfo.getProperties(bean, controller.editor)

			// Avoid triggering property change login while bean properties are loaded
			propertyObject = null

			sheet.readFromObject(bean)

			adjustTableHeights(sheet.table)
			propertyObject = bean
			updateLabel()
		} catch (e: Throwable) {
			LOG.warn("Could not instantiate Properties for $classPath: Exception $e")
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

	private fun updateLabel() {
		title.text = controller.title
	}
}