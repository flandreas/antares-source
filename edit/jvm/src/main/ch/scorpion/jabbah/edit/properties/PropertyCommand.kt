package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.componentBeanProvider
import org.apache.commons.beanutils.PropertyUtils

/**
 * A [Command] that reflects the undoable change of an object's property.
 * @param V the type of the property's value
 */
class PropertyCommand<V>(
	editor: Editor,
	private val propertyBaseKey: String,
	private val beanProvider: BeanProvider,
	private val beanIds: List<Int>,
	private val newValue: V?,
	getterPropertyName: String,
	private val setterPropertyName: String
) : AbstractCommand("edit.command.property", editor) {

	companion object {
		fun <V> forComponent(
			editor: Editor,
			propertyBaseKey: String,
			beanIds: List<Int>,
			newValue: V?,
			getterPropertyName: String,
			setterPropertyName: String
		) : PropertyCommand<V> {
			return PropertyCommand(editor, propertyBaseKey, componentBeanProvider, beanIds, newValue, getterPropertyName, setterPropertyName)
		}
	}

	private val bean get() = beanProvider.invoke(editor!!, beanIds)

	private fun isNested(name: String): Boolean = name.contains('.')

	private fun getValue(name: String): V? {
		return if (isNested(name)) {
			PropertyUtils.getNestedProperty(bean, name) as V?
		} else {
			PropertyUtils.getSimpleProperty(bean, name) as V?
		}
	}

	private fun setValue(value: V?) {
		if (isNested(setterPropertyName)) {
			PropertyUtils.setNestedProperty(bean, setterPropertyName, value)
		} else {
			PropertyUtils.setSimpleProperty(bean, setterPropertyName, value)
		}
	}

	val oldValue: V? = getValue(getterPropertyName)

    override fun getDescription(): String {
        return Translations.getString("$propertyBaseKey.name")
    }

    override fun execute() {
	    setValue(newValue)
    }

    override fun undo() {
	    setValue(oldValue)
    }
}