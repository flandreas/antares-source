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
	private val beanId: Int?,
	private val newValue: V?,
	private val getterPropertyName: String,
	private val setterPropertyName: String
) : AbstractCommand("edit.command.property", editor) {

	companion object {
		fun <V> forComponent(
			editor: Editor,
			propertyBaseKey: String,
			beanId: Int?,
			newValue: V?,
			getterPropertyName: String,
			setterPropertyName: String
		) : PropertyCommand<V> {
			return PropertyCommand(editor, propertyBaseKey, componentBeanProvider, beanId, newValue, getterPropertyName, setterPropertyName)
		}
	}

	private val bean get() = beanProvider.invoke(editor!!, beanId)

	val oldValue: V? = PropertyUtils.getSimpleProperty(bean, getterPropertyName) as V?

    override fun getDescription(): String {
        return Translations.getString("$propertyBaseKey.name")
    }

    override fun execute() {
        PropertyUtils.setSimpleProperty(bean, setterPropertyName, newValue)
    }

    override fun undo() {
	    PropertyUtils.setSimpleProperty(bean, setterPropertyName, oldValue)
    }
}