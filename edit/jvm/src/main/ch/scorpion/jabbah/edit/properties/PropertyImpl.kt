package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.AbstractProperty
import com.l2fprod.common.propertysheet.Property
import org.apache.commons.beanutils.PropertyUtils

/** An implementation of a [Property] that created a [PropertyCommand] for every changed property. */
class PropertyImpl<V>(
	propertyName: String,
	private val baseKey: String,
	private val valueClass: Class<V>,
	private val beanProvider: BeanProvider,
	private val setterPropertyName: String = propertyName,
	private val getterPropertyName: String = propertyName,
	private var interactive: Boolean = false
) : AbstractProperty() {

	companion object {
		private val LOG by logger(PropertyImpl::class)
	}

	private var editor: Editor? = null

	private var beanIds: List<Int> = listOf()

	var editable: Boolean = true
		private set

	var filter: (V) -> Boolean = { true }
		private set

	var optional: Boolean = false
		private set

	fun bind(
		editor: Editor,
		beanIds: List<Int>,
		editable: Boolean = true,
		filter: ((V) -> Boolean)? = null,
		optional: Boolean = false
	): PropertyImpl<V> {
		this.editor = editor
		this.beanIds = beanIds
		this.editable = editable && editor.active
		if (filter != null) {
			this.filter = filter
		}
		this.optional = optional
		return this
	}

	fun bind(
		editor: Editor,
		beanId: Int,
		editable: Boolean = true,
		filter: ((V) -> Boolean)? = null,
		optional: Boolean = false
	): PropertyImpl<V> {
		return bind(editor, listOf(beanId), editable, filter, optional)
	}

	/** ---- [Property] interface */

	override fun getName(): String = getterPropertyName

	override fun getDisplayName(): String = Translations.getString("$baseKey.name")

	override fun getType(): Class<*> = valueClass

	override fun isEditable(): Boolean = editable || interactive

	override fun getCategory(): String? = null

	override fun getShortDescription(): String? = Translations.getOptionalString("$baseKey.desc")

	override fun readFromObject(bean: Any?) {
		value = if (getterPropertyName.contains('.')) {
			PropertyUtils.getNestedProperty(bean, getterPropertyName)
		} else {
			PropertyUtils.getSimpleProperty(bean, getterPropertyName)
		}
	}

	override fun writeToObject(p0: Any?) {
		writeToBean()
	}

	private fun writeToBean() {
		@Suppress("UNCHECKED_CAST")
		val newValue = value as V?

		val command = PropertyCommand(editor!!, baseKey, beanProvider, beanIds, newValue, getterPropertyName, setterPropertyName)

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