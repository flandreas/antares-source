package ch.scorpion.jabbah.edit

import com.l2fprod.common.propertysheet.AbstractProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger


/**
 * A wrapper for a property of a bean that can be used with property editors, and that issues a [Command]
 * @param <V> the type of the value
 */
class PropertyImpl<V>(
    private val baseKey: String,
    private val valueClass: Class<V>
) : AbstractProperty() {

    companion object {
        val LOG by logger(PropertyImpl::class)
    }

    private var editor: Editor? = null
    private var getter: (() -> V?)? = null
    private var setter: ((V?) -> Unit)? = null
    private var editable: Boolean = false
    var filter: (V) -> Boolean = { true }
	var optional: Boolean = false

    /** ---- [PropertyImpl] */

    fun bind(editor: Editor, getter: () -> V?, setter: (V?) -> Unit): PropertyImpl<V> {
        bind(editor, getter, setter, true)
        return this
    }

    fun bind(editor: Editor, getter: () -> V?, setter: ((V?) -> Unit)?, editable: Boolean, filter: ((V) -> Boolean)? = null, optional: Boolean = false): PropertyImpl<V> {
        this.editor = editor
        this.getter = getter
        this.setter = setter
        this.editable = editable
        if (filter != null) {
            this.filter = filter
        }
	    this.optional = optional
        return this
    }

    /** ---- [AbstractProperty] */

    override fun getCategory(): String? {
        return null
    }

    override fun getDisplayName(): String {
        return Translations.getString("$baseKey.name")
    }

    override fun getName(): String? {
        return null
    }

    override fun getShortDescription(): String? {
        return Translations.getOptionalString("$baseKey.desc")
    }

    override fun getType(): Class<*> {
        return valueClass
    }

    override fun isEditable(): Boolean {
        return editable
    }

    override fun readFromObject(bean: Any) {
        value = getter!!.invoke()
    }

    override fun writeToObject(bean: Any) {
        writeToBean()
    }

    /** ---- [PropertyImpl] */

    @Suppress("UNCHECKED_CAST")
    fun writeToBean() {
        val oldValue = getter!!.invoke()
        val newValue = value as V
        if (oldValue != newValue) {
            try {
	            editor!!.commandManager.beginTransaction(PropertyCommand(editor!!, baseKey, getter!!, setter!!, newValue), register = true)
                setter!!.invoke(newValue)
	            editor!!.commandManager.commitTransaction()
            } catch (t: Throwable) {
                LOG.error("Error in invoking bean setter: ${t.message}")
                editor!!.commandManager.rollbackTransaction()
	            throw t
            }
        }
    }
}