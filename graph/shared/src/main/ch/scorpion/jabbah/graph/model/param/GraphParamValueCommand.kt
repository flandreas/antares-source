package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

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

	override fun getValue(bean: Bean): V? =
		(bean as SubGraphVerticeViewImpl).model.paramValues.getValue(paramDef.name)?.value as V?

	override fun setValue(bean: Bean, value: V?) {
		value?.let {
			(bean as SubGraphVerticeViewImpl).model.setParamValue(paramDef.createValue(it))
		} ?: throw IllegalArgumentException("value must not be null")
	}
}