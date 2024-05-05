package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

class GraphParamValuePropertySwing<V : Any>(
	private val paramDefinition: GraphParamDefinition<V>,
	propertyName: String,
	baseKey: String,
	valueClass: Class<V>,
	beanProvider: BeanProvider,
	interactive: Boolean = false,
	displayName: String? = null,
	baseKeyParams: Array<Any> = emptyArray()
): CommandPropertySwing<V>(propertyName, baseKey, valueClass, beanProvider, propertyName, propertyName, interactive, displayName, baseKeyParams = baseKeyParams) {

	override fun readFromObject(bean: Any?) {
		val subGraphVerticeView = bean as SubGraphVerticeViewImpl?
		value = subGraphVerticeView?.model?.paramValues?.getValue(paramDefinition.name)?.value
	}

	override fun createCommand(newValue: V?): AbstractPropertyCommand<V> =
		GraphParamValueCommand(paramDefinition, editor!!, baseKey, baseKeyParams, beanProvider, beanIds, newValue)
}