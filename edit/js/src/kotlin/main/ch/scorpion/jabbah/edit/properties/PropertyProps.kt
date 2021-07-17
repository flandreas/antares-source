package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyGetter
import ch.scorpion.jabbah.edit.PropertySetter
import react.RProps

external interface PropertyProps<T> : RProps {
	var editor: Editor
	var propertyBaseKey: String
	var beanProvider: BeanProvider
	var beanIds: List<Int>
	var getter: PropertyGetter<T>
	var setter: PropertySetter<T>
	var disabled: Boolean
	var filter: ((T) -> Boolean)?
}