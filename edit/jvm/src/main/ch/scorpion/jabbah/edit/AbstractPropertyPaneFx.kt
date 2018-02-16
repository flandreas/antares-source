package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import com.l2fprod.common.propertysheet.Property
import javafx.beans.value.ObservableValue
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.controlsfx.control.PropertySheet
import java.util.*

/** A [Pane] for editing the properties of a bean.*/
abstract class AbstractPropertyPaneFx(
	protected val editor: Editor
) : VBox() {

	companion object {
		private val LOG by logger(AbstractPropertyPaneFx::class)
	}

	/** Displays the properties of the current bean.*/
	private val sheet = PropertySheet()

	/** Displays the title that identifies the current bean.*/
	private val label = Label()

	/** The object whose properties are currently being edited.*/
	private var propertyObject: Any? = null

	init {
		children.add(label)
		children.add(sheet)
	}

	/** ---- [AbstractPropertyPaneFx] */

	/** Returns a displayable description of the selected bean object. */
	abstract fun getDescription(bean: Any): String?

	protected fun clearProperties() {
		sheet.items.clear()
		propertyObject = null
		updateLabel()
	}

	protected fun updateProperties(bean: Any) {
		try {
			val beanInfoClass = Class.forName(bean.javaClass.name + "BeanInfo")
			val beanInfo = beanInfoClass.newInstance() as AbstractBeanInfo<Any>

			sheet.items.setAll(beanInfo.getProperties(bean, editor).map { PropertyWrapper(it, bean) })
			propertyObject = bean
			updateLabel()
		} catch (e: Throwable) {
			LOG.debug("Could not instantiate Properties for ${bean.javaClass.simpleName}: Exception $e")
			clearProperties()
		}
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

	private inner class PropertyWrapper(private val property: Property, private val bean: Any) : PropertySheet.Item {

		override fun setValue(value: Any?) {
			property.value = value
			property.writeToObject(bean)
			editor.view.drawing.validate()
		}

		override fun getValue(): Any? {
			property.readFromObject(bean)
			return property.value
		}

		override fun getName(): String = property.displayName

		override fun getDescription(): String? = property.shortDescription

		override fun getType(): Class<*> = property.type

		override fun getObservableValue(): Optional<ObservableValue<out Any>> = Optional.empty()

		override fun getCategory(): String? = property.category
	}
}