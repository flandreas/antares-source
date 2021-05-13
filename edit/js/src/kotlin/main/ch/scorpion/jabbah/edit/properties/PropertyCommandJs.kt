package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.*

/** Creates and submits a new [PropertyCommandJs] for the specified values.*/
fun <V> submitCommand(props: PropertyProps<V>, oldValue: V?, newValue: V?) {
	val command = PropertyCommandJs(props.editor, props.propertyBaseKey, props.beanProvider, props.beanIds, newValue, props.getter, props.setter)
	command.establishOldValue()

	if (newValue != oldValue) {
		props.editor.commandManager.apply {
			try {
				beginTransaction(command)
				commitTransaction()
			} catch (t: Throwable) {
				error("Error in submitCommand: $t")
				if (isInTransaction) {
					rollbackTransaction()
				}
			}
		}
	}
}

/** An implementation of [AbstractPropertyCommand] for the JS platform. */
class PropertyCommandJs<V>(
	editor: Editor,
	propertyBaseKey: String,
	beanProvider: BeanProvider,
	beanIds: List<Int>,
	newValue: V?,
	private val getter: PropertyGetter<V>,
	private val setter: PropertySetter<V>
) : AbstractPropertyCommand<V>(
	editor,
	propertyBaseKey,
	beanProvider,
	beanIds,
	newValue
) {

	override fun getValue(): V? = getter(bean)

	override fun setValue(value: V?) {
		setter(bean, value)
	}
}