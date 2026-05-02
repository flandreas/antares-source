package io.antarescircuit.jabbah.graph.model.param

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

/**
 * A [Command] for changing a [GraphParamValue] of a [SubGraphVerticeRef].
 * Cannot use the reflection-based [Command] implementation on the JVM platform because
 * [GraphParamValues][GraphParamValue] are not individual properties in [SubGraphVerticeRef].
 */
class GraphParamValueCommand<V : Any>(
	private val paramDef: GraphParamDefinition<V>,
	editor: Editor,
	propertyBaseKey: String,
	baseKeyParams: Array<Any> = emptyArray(),
	beanProvider: BeanProvider,
	beanIds: Collection<String>,
	newValue: V?
) : AbstractPropertyCommand<V>(
	editor,
	propertyBaseKey,
	baseKeyParams,
	beanProvider,
	beanIds,
	newValue
) {

	@Suppress("UNCHECKED_CAST")
	override fun getValue(bean: Bean): V? =
		(bean as SubGraphVerticeViewImpl).model.paramValues.getValue(paramDef.name)?.value as V?

	override fun setValue(bean: Bean, value: V?) {
		value?.let {
			(bean as SubGraphVerticeViewImpl).model.setParamValue(paramDef.createValue(it))
		} ?: throw IllegalArgumentException("value must not be null")
	}
}