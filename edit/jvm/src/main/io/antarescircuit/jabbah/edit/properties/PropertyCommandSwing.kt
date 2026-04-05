package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.*
import org.apache.commons.beanutils.PropertyUtils

/** An implementation of [AbstractPropertyCommand] for the JVM platform. */
open class PropertyCommandSwing<V>(
	editor: Editor,
	propertyBaseKey: String,
	beanProvider: BeanProvider,
	beanIds: Collection<String>,
	newValue: V?,
	private val getterPropertyName: String,
	private val setterPropertyName: String
) : AbstractPropertyCommand<V>(
	editor,
	propertyBaseKey,
	emptyArray(),
	beanProvider,
	beanIds,
	newValue
) {

	companion object {
		fun <V> forComponent(
			editor: Editor,
			propertyBaseKey: String,
			beanIds: Collection<String>,
			newValue: V?,
			getterPropertyName: String,
			setterPropertyName: String
		) : PropertyCommandSwing<V> {
			return PropertyCommandSwing(editor, propertyBaseKey, componentBeanProvider, beanIds, newValue, getterPropertyName, setterPropertyName)
		}
	}

	override fun getValue(bean: Bean): V? {
		return if (AbstractReflectionPropertySwing.isNested(getterPropertyName)) {
			PropertyUtils.getNestedProperty(bean, getterPropertyName) as V?
		} else {
			PropertyUtils.getSimpleProperty(bean, getterPropertyName) as V?
		}
	}

	override fun setValue(bean: Bean, value: V?) {
		if (AbstractReflectionPropertySwing.isNested(setterPropertyName)) {
			PropertyUtils.setNestedProperty(bean, setterPropertyName, value)
		} else {
			PropertyUtils.setSimpleProperty(bean, setterPropertyName, value)
		}
	}
}