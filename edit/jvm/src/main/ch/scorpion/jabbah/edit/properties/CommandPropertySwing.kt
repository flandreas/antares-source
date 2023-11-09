package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import com.l2fprod.common.propertysheet.Property
import java.lang.reflect.InvocationTargetException

/**
 * An implementation of a [Property] that uses reflection to read the properties and that creates
 * a [PropertyCommandSwing] for every changed property.
 *
 * @param V the type of the property's value
 */
open class CommandPropertySwing<V>(
	propertyName: String,
	baseKey: String,
	valueClass: Class<V>,
	protected val beanProvider: BeanProvider,
	protected val setterPropertyName: String = propertyName,
	getterPropertyName: String = propertyName,
	interactive: Boolean = false,
	displayName: String? = null,
	supportMultiSelection: Boolean = true,
	baseKeyParams: Array<Any> = emptyArray()
) : AbstractReflectionPropertySwing<V>(propertyName, baseKey, valueClass, getterPropertyName, interactive, displayName, supportMultiSelection, baseKeyParams) {

	companion object {
		private val LOG by logger(CommandPropertySwing::class)
	}

	override fun writeToBeans(force: Boolean) {
		@Suppress("UNCHECKED_CAST")
		val newValue = value as V?

		val command = createCommand(newValue)
		command.establishOldValues()

		if (force || command.valueChanged) {
			try {
				LOG.userTrail("Change property '${command.getDescription()}' of component ${beanIds.firstOrNull()} to '$newValue'")
				editor!!.commandManager.beginTransaction(command)
				editor!!.commandManager.commitTransaction()
			} catch (t: InvocationTargetException) {
				LOG.error("Error in invoking bean setter '$setterPropertyName': ${t.targetException.message}")
				if (editor!!.commandManager.isInTransaction) {
					editor!!.commandManager.rollbackTransaction()
				}
				throw t.targetException
			} catch (t: Throwable) {
				LOG.error("Error in invoking bean setter '$setterPropertyName': ${t.message}")
				if (editor!!.commandManager.isInTransaction) {
					editor!!.commandManager.rollbackTransaction()
				}
				throw t
			}
		}
	}

	protected open fun createCommand(newValue: V?): AbstractPropertyCommand<V> =
		PropertyCommandSwing(editor!!, baseKey, beanProvider, beanIds, newValue, getterPropertyName, setterPropertyName)

	protected val beans get() = beanProvider(editor!!, beanIds)
}