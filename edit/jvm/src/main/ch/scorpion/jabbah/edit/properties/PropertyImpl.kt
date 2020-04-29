package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import com.l2fprod.common.propertysheet.AbstractProperty
import com.l2fprod.common.propertysheet.Property
import org.apache.commons.beanutils.PropertyUtils

class PropertyImpl<V>(
	propertyName: String,
	private val baseKey: String,
	private val valueClass: Class<V>,
	private val beanProvider: BeanProvider,
	private val setterPropertyName: String = propertyName,
	private val getterPropertyName: String = propertyName
) : AbstractProperty() {

	companion object {

		private val LOG by logger(PropertyImpl::class)

		fun <V> componentProperty(propertyName: String, baseKey: String, valueClass: Class<V>): PropertyImpl<V> =
			PropertyImpl(propertyName, baseKey, valueClass, componentBeanProvider)
	}

	private var editor: Editor? = null
	private var beanId: Int? = null
	private var editable: Boolean = false
	var filter: (V) -> Boolean = { true }
	var optional: Boolean = false

	fun bind(
		editor: Editor,
		beanId: Int?,
		editable: Boolean = true,
		filter: ((V) -> Boolean)? = null,
		optional: Boolean = false
	): PropertyImpl<V> {
		this.editor = editor
		this.beanId = beanId
		this.editable = editable
		if (filter != null) {
			this.filter = filter
		}
		this.optional = optional
		return this
	}


	/** ---- [Property] interface */

	override fun getName(): String = getterPropertyName

	override fun getDisplayName(): String = Translations.getString("$baseKey.name")

	override fun getType(): Class<*> = valueClass

	override fun isEditable(): Boolean = editable

	override fun getCategory(): String? = null

	override fun getShortDescription(): String? = Translations.getOptionalString("$baseKey.desc")

	override fun readFromObject(bean: Any?) {
		value = PropertyUtils.getSimpleProperty(bean, getterPropertyName)
	}

	override fun writeToObject(p0: Any?) {
		writeToBean()
	}

	private fun writeToBean() {
		val newValue = value as V?
		val command = PropertyCommand<V>(editor!!, baseKey, beanProvider, beanId, newValue, getterPropertyName, setterPropertyName)

		if (newValue != command.oldValue) {
			try {
				editor!!.commandManager.beginTransaction(command)
				editor!!.commandManager.commitTransaction()
			} catch (t: Throwable) {
				LOG.debug("Error in invoking bean setter '$setterPropertyName': ${t.message}")
				editor!!.commandManager.rollbackTransaction()
				throw t
			}
		}
	}
}