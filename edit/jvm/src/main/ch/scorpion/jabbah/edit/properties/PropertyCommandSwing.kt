package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.*
import org.apache.commons.beanutils.PropertyUtils

/** An implementation of [AbstractPropertyCommand] for the JVM platform. */
open class PropertyCommandSwing<V>(
	editor: Editor,
	propertyBaseKey: String,
	beanProvider: BeanProvider,
	beanIds: List<Int>,
	newValue: V?,
	private val getterPropertyName: String,
	private val setterPropertyName: String
) : AbstractPropertyCommand<V>(
	editor,
	propertyBaseKey,
	beanProvider,
	beanIds,
	newValue
) {

	companion object {
		fun <V> forComponent(
			editor: Editor,
			propertyBaseKey: String,
			beanIds: List<Int>,
			newValue: V?,
			getterPropertyName: String,
			setterPropertyName: String
		) : PropertyCommandSwing<V> {
			return PropertyCommandSwing(editor, propertyBaseKey, componentBeanProvider, beanIds, newValue, getterPropertyName, setterPropertyName)
		}
	}

	private fun isNested(name: String): Boolean = name.contains('.')

	override fun getValue(): V? {
		return if (isNested(getterPropertyName)) {
			PropertyUtils.getNestedProperty(bean, getterPropertyName) as V?
		} else {
			PropertyUtils.getSimpleProperty(bean, getterPropertyName) as V?
		}
	}

	override fun setValue(value: V?) {
		if (isNested(setterPropertyName)) {
			PropertyUtils.setNestedProperty(bean, setterPropertyName, value)
		} else {
			PropertyUtils.setSimpleProperty(bean, setterPropertyName, value)
		}
	}
}