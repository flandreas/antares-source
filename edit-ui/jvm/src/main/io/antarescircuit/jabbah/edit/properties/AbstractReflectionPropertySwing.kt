package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.AbstractProperty
import com.l2fprod.common.propertysheet.Property
import org.apache.commons.beanutils.PropertyUtils

/**
 * Base class for implementing editable properties on the JVM platform.
 * Uses reflection to read a property's value, but leaves writing property values up to subclasses;
 * which could also be done by using reflection, or by calling an application service, or by anything else.
 *
 * @param V the type of the property's value
 */
abstract class AbstractReflectionPropertySwing<V>(
	propertyName: String,
	protected val baseKey: String,
	private val valueClass: Class<*>,
	protected val getterPropertyName: String = propertyName,
	private var interactive: Boolean = false,
	private val displayName: String? = null,
	val supportMultiSelection: Boolean = true,
	protected val baseKeyParams: Array<Any> = emptyArray()
) : AbstractProperty() {

	companion object {
		fun isNested(name: String): Boolean = name.contains('.')
	}

	var editor: Editor? = null

	protected var beanIds: Collection<String> = listOf()

	var editable: Boolean = true
		private set

	var filter: (V) -> Boolean = { true }
		private set

	var optional: Boolean = false
		private set

	abstract fun writeToBeans(force: Boolean = false)

	fun bind(
		editor: Editor,
		beanIds: Collection<String>,
		editable: Boolean = true,
		filter: ((V) -> Boolean)? = null,
		optional: Boolean = false
	): AbstractReflectionPropertySwing<V> {
		this.editor = editor
		this.beanIds = beanIds
		this.editable = editable && editor.view.editable
		if (filter != null) {
			this.filter = filter
		}
		this.optional = optional
		return this
	}

	/** Primarily used for testing. */
	fun forceWriteToObject() {
		writeToBeans(force = true)
	}

	/** ---- [Property] interface */

	override fun getName(): String = getterPropertyName

	override fun getDisplayName(): String = displayName ?: Translations.getString("$baseKey.name", *baseKeyParams)

	override fun getType(): Class<*> = valueClass

	override fun isEditable(): Boolean = editable || interactive

	override fun getCategory(): String? = null

	override fun getShortDescription(): String? = Translations.getOptionalString("$baseKey.desc")

	override fun readFromObject(bean: Any?) {
		value = if (isNested(getterPropertyName)) {
			PropertyUtils.getNestedProperty(bean, getterPropertyName)
		} else {
			PropertyUtils.getSimpleProperty(bean, getterPropertyName)
		}
	}

	override fun writeToObject(p0: Any?) {
		writeToBeans()
	}
}