package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
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
	beanProvider: BeanProvider,
	beanIds: List<Int>,
	newValue: V?
) : AbstractPropertyCommand<V>(
	editor,
	propertyBaseKey,
	beanProvider,
	beanIds,
	newValue
) {
	private val subGraphVerticeView: SubGraphVerticeViewImpl get() = bean as SubGraphVerticeViewImpl

	override fun getValue(): V? =
		subGraphVerticeView.model.paramValues.withName(paramDef.name)?.value as V?

	override fun setValue(value: V?) {
		value?.let {
			subGraphVerticeView.model.paramValues.addOrReplace(paramDef.createValue(it))
		} ?: throw IllegalArgumentException("value must not be null")
	}
}