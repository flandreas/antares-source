package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractReflectionPropertySwing

interface GraphParamValuePropertyFactory {
	fun create(
		def: GraphParamDefinition<*>,
		editor: Editor,
		beanProvider: BeanProvider
	): AbstractReflectionPropertySwing<*>
}

object GraphParamValuePropertyFactoryRegistry {

	private val factories = mutableMapOf<GraphParamType<*>,GraphParamValuePropertyFactory>()

	fun register(paramType: GraphParamType<*>, factory: GraphParamValuePropertyFactory) {
		factories[paramType] = factory
	}

	fun createProperty(
		def: GraphParamDefinition<*>,
		editor: Editor,
		beanProvider: BeanProvider
	): AbstractReflectionPropertySwing<*> =
		factories[def.type]?.create(def, editor, beanProvider)
			?: throw IllegalStateException("no factory for GraphParamType '${def.type.name}'")
}