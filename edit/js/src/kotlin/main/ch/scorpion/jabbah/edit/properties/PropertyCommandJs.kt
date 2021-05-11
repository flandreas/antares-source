package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.*

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