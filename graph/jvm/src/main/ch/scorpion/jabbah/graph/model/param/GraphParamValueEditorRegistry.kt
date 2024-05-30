package ch.scorpion.jabbah.graph.model.param

import javax.swing.JComponent

interface GraphParamValueEditor {

	/** Allows to set and get the value of [editor]. */
	var paramValue: Any

	/** The handler to be called by this [GraphParamValueEditor] when the user has changed its value.*/
	var changeHandler: (() -> Unit)?

	/** Controls enabledness of [editor].*/
	var editorEnabled: Boolean

	/** The UI representation of this [GraphParamValueEditor]. */
	val editor: JComponent

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