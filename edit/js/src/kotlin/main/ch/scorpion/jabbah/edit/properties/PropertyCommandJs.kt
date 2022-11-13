package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.*

fun <V> propertyCommandFactory(props: PropertyProps<V>, newValue: V?): PropertyCommandJs<V> =
	PropertyCommandJs(props.editor, props.propertyBaseKey, props.beanProvider, props.beanIds, newValue, props.getter, props.setter)

/** Creates and submits a new [PropertyCommandJs] for the specified values.*/
fun <V> submitCommand(
	props: PropertyProps<V>,
	newValue: V?,
	cmdFactory: (PropertyProps<V>, V?) -> PropertyCommandJs<V> = { p, _ -> propertyCommandFactory(p, newValue) }
) {
	val command = cmdFactory(props, newValue)
	command.establishOldValues()

	if (command.valueChanged) {
		props.editor.commandManager.apply {
			try {
				beginTransaction(command)
				commitTransaction()
			} catch (t: Throwable) {
				if (isInTransaction) {
					rollbackTransaction()
				}
				error("Error in submitCommand: $t")
			}
		}
	}
}

/** An implementation of [AbstractPropertyCommand] for the JS platform. */
open class PropertyCommandJs<V>(
	editor: Editor,
	propertyBaseKey: String,
	beanProvider: BeanProvider,
	beanIds: Collection<String>,
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

	override fun getValue(bean: Bean): V? = getter(bean)

	override fun setValue(bean: Bean, value: V?) {
		setter(bean, value)
	}
}