package ch.scorpion.jabbah.graph.model.param

interface GraphParamValueEditor {
	var value: Any
	var changeHandler: (() -> Unit)?
	var editorEnabled: Boolean
}

typealias GraphParamValueEditorFactory = () -> GraphParamValueEditor

object GraphParamValueEditorRegistry {

	private val factories = mutableMapOf<GraphParamType<*>, GraphParamValueEditorFactory>()

	fun register(type: GraphParamType<*>, factory: GraphParamValueEditorFactory) {
		if (factories.containsKey(type)) {
			throw IllegalArgumentException("factory already registered")
		}
		factories[type] = factory
	}

	fun create(type: GraphParamType<*>): GraphParamValueEditor =
		factories[type]?.invoke() ?: throw IllegalArgumentException("no factory for $type")
}